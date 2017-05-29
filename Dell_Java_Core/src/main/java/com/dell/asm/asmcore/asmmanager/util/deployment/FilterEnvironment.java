/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FilterEnvironment {
    private static final Logger logger = Logger.getLogger(FilterEnvironment.class);

    public FilterEnvironment() {
    }

    public void initEnvironment(ServiceTemplateComponent component) {
        // By default require non-minimal server to be firmware compliant.
        compliantState = CompliantState.COMPLIANT;

        if (component == null) return;
        setMinimalServer(component.checkMinimalServerComponent());

        if (component.hasWindowsOS()) {
            setWindowsOS(true);
        }

        for (ServiceTemplateCategory resource : component.getResources()) {
            if (resource.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE)) {
                List<ServiceTemplateSetting> parameters = resource.getParameters();

                setClonedModelString(parameters);

                for (ServiceTemplateSetting param : parameters) {
                    //check if the target boot device is SD
                    if (param.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID))
                    {
                        String targetBootDevice = param.getValue();
                        if (targetBootDevice.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD) ||
                                targetBootDevice.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN) ||
                                targetBootDevice.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID))
                            setSDBoot(true);

                        else if (targetBootDevice.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN))
                            setLocalDiskVSANBoot(true);
                    } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID.equals(param.getId())) {
                        // make sure boot type is HD/None
                        ServiceTemplateSetting depTarget = resource.getParameter(param.getDependencyTarget());
                        if (depTarget == null || StringUtils.isEmpty(depTarget.getValue()) || Arrays.asList(param.getDependencyValue().split(",")).contains(depTarget.getValue())) {
                            if (StringUtils.isBlank(param.getValue())) {
                                logger.error("Raid configuration set but no configuration value found");
                            } else {
                                try {
                                    String configString = "{ \"templateRaidConfiguration\" : " + param.getValue() + "}";

                                    TemplateRaidConfiguration configuration = MarshalUtil.fromJSON(TemplateRaidConfiguration.class, configString);
                                    setRaidConfiguration(configuration);
                                } catch (IllegalStateException e) {
                                    logger.error("Raid configuration set but has invalid value: " + param.getValue());
                                }
                            }
                        }
                    }
                }
            }else if (resource.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID)) {
                List<ServiceTemplateSetting> parameters = resource.getParameters();
                setClonedModelString(parameters);

                for (ServiceTemplateSetting parama : parameters) {
                    if (parama.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID)) {
                        NetworkConfiguration configuration = null;
                        if (StringUtils.isBlank(parama.getValue())) {
                            logger.error("Network configuration is blank");
                        } else {
                            configuration = ServiceTemplateUtil.deserializeNetwork(parama.getValue());
                        }

                        if (configuration!=null) {
                            List<Fabric> cards = configuration.getInterfaces();
                            if (cards != null) {

                                for (int i = 0; i < cards.size(); i++) {
                                    int tempInterfaceValue = i;
                                    int tempI = ++tempInterfaceValue;
                                    List<String> ports = new ArrayList<String>();

                                    if (Fabric.FC_TYPE.equals(cards.get(i).getFabrictype())) {
                                        addFCCard(makeCardKey(tempI ,cards.get(i).getId())); // store # of the FC card in template
                                    }

                                    if (cards.get(i) != null && cards.get(i).getInterfaces() != null) {
                                        List<Interface> innerInterfaces = cards.get(i).getInterfaces();
                                        if (cards.get(i).isPartitioned())
                                            addPartitionedInterface(tempI, cards.get(i).getId());

                                        for (int j = 0; j < cards.get(i).getNPorts(); j++) {
                                            int tempInnerInterfaceValue = j;
                                            if (innerInterfaces.get(j).getPartitions() != null
                                                    && !innerInterfaces.get(j).getPartitions().isEmpty()) {

                                                ports.add(String.valueOf(++tempInnerInterfaceValue));

                                                // walk through partitions and analyse networks
                                                for (Partition p : innerInterfaces.get(j).getPartitions()) {
                                                    if (p.getNetworks() != null) {
                                                        for (String networkId : p.getNetworks()) {
                                                            com.dell.pg.asm.identitypool.api.network.model.Network network =
                                                                    getNetworkProxy().getNetwork(networkId);

                                                            if (network != null) {
                                                                if (NetworkType.STORAGE_ISCSI_SAN.equals(network.getType())) {
                                                                    addIscsiInterface(tempI, cards.get(i).getId());
                                                                } else if ( (NetworkType.PXE.equals(network.getType())) && network.isStatic()) {
                                                                    addStaticOSInstallInterface(tempI, cards.get(i).getId());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!ports.isEmpty()) {
                                            addPortMapping(tempI, cards.get(i).getId(), ports);
                                            if (Interface.NIC_2_X_10GB_2_X_1GB.equals(cards.get(i).getNictypeSource())) {
                                                addTwoByTwoInterface(tempI, cards.get(i).getId());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE.equals(resource.getId())) {
                ServiceTemplateSetting param = resource.getParameter(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID);
                if (param != null &&
                        ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH.equals(param.getValue()) &&
                        ServiceTemplateUtil.checkDependency(component, param)) {
                        setAllFlash(true);
                }

                param = resource.getParameter(ServiceTemplateSettingIDs.LOCAL_STORAGE_ID);
                if (param != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TRUE_VALUE.equals(param.getValue())) {
                    setLocalStorageForVSANEnabled(true);
                }
            }
        }

        ServiceTemplateSetting serverOsType = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
        if(serverOsType != null && serverOsType.getValue() != null && serverOsType.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERV_VALUE)){
            setHyperVUsed(true);
        }

        ServiceTemplateSetting iscsiInitiator = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ISCSI_ID);

        if (iscsiInitiator != null && iscsiInitiator.getValue() != null) {
            ServiceTemplateSetting depTarget = component.getTemplateSetting(iscsiInitiator.getDependencyTarget());

            if ((depTarget != null && StringUtils.contains(iscsiInitiator.getDependencyValue(), depTarget.getValue())) &&
                    iscsiInitiator.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ISCSI_HARDWARE_ID)) {
                setHardwareISCSI(true);
            }
        }

    }

    private void addIscsiInterface(int i, String id) {
        if (!iscsiInterfaces.contains(makeCardKey(i,id)))
            iscsiInterfaces.add(makeCardKey(i,id));
    }

    private void addPartitionedInterface(int i, String id) {
        if (!partitionedInterfaces.contains(makeCardKey(i,id)))
            partitionedInterfaces.add(makeCardKey(i,id));
    }

    private String makeCardKey(int idx, String id) {
        return idx + ":" + id;
    }

    public List<String> getFCCards() {
        return fcCards;
    }

    public Map<String, List<String>> getPortUIMap() {
        return portUIMap;
    }

    public boolean isMinimalServer() {
        return minimalServer;
    }

    public void setMinimalServer(boolean minimalServer) {
        this.minimalServer = minimalServer;
    }

    public INetworkService getNetworkProxy() {
        if (networkProxy == null) {
            networkProxy = ProxyUtil.getNetworkProxy();
        }
        return networkProxy;
    }

    public void setNetworkProxy(INetworkService networkProxy) {
        this.networkProxy = networkProxy;
    }

    private INetworkService networkProxy;
    private boolean minimalServer;
    private List<String> fcCards = new ArrayList<String>();
    private Map<String, List<String>> portUIMap = new HashMap<String, List<String>>();
    private boolean isSDBoot;
    private boolean isWindowsOS = false;

    public boolean isAllFlash() {
        return isAllFlash;
    }

    public void setAllFlash(boolean allFlash) {
        isAllFlash = allFlash;
    }

    private boolean isAllFlash;

    public boolean isLocalDiskVSANBoot() {
        return isLocalDiskVSANBoot;
    }

    public void setLocalDiskVSANBoot(boolean localDiskVSANBoot) {
        isLocalDiskVSANBoot = localDiskVSANBoot;
    }

    private boolean isLocalDiskVSANBoot;
    private CompliantState compliantState;
    private boolean isHyperVUsed = false;

    public boolean isLocalStorageForVSANEnabled() {
        return isLocalStorageForVSANEnabled;
    }

    public void setLocalStorageForVSANEnabled(boolean localStorageForVSANEnabled) {
        isLocalStorageForVSANEnabled = localStorageForVSANEnabled;
    }

    private boolean isLocalStorageForVSANEnabled = false;

    public boolean isHardwareISCSI() {
        return isHardwareISCSI;
    }

    public void setHardwareISCSI(boolean isHardwareISCSI) {
        this.isHardwareISCSI = isHardwareISCSI;
    }

    private boolean isHardwareISCSI = false;

    public String getClonedModelString() {
        return clonedModelString;
    }

    private void setClonedModelString(final List<ServiceTemplateSetting> parameters) {
        this.clonedModelString = getModelString(parameters);
    }

    String clonedModelString;


    /**
     * List of interfaces of NIC type 2x10GB,2x1GB
     * List inlcudes interface numbers i.e. 1,2,etc - same as a key for portUIMap
     * @return
     */
    public List<String> getTwoByTwoInterfaces() {
        return twoByTwoInterfaces;
    }

    List<String> twoByTwoInterfaces = new ArrayList<>();

    public List<String> getIscsiInterfaces() {
        return iscsiInterfaces;
    }

    public List<String> getPartitionedInterfaces() {
        return partitionedInterfaces;
    }

    List<String> iscsiInterfaces = new ArrayList<>();

    List<String> partitionedInterfaces = new ArrayList<>();

    // List of NICS which support static network OS install
    List<String> staticOSInstallInterfaces = new ArrayList<>();

    /**
     * Returns the list of interfaces which support static network OS install
     * @return the list
     */
     public List<String> getStaticOSInstallInterfaces() {
        return staticOSInstallInterfaces;
     }

     /**
      * Add an static network capable interface to the list
      * @param i interface number
      * @param id card id
     */
     private void addStaticOSInstallInterface(int i, String id) {
        if (!staticOSInstallInterfaces.contains(makeCardKey(i,id)))
            staticOSInstallInterfaces.add(makeCardKey(i,id));
     }

    public CompliantState getCompliantState() { return compliantState; }
    public void setCompliantState(CompliantState compliantState) { this.compliantState = compliantState; }

    public boolean isWindowsOS() { return isWindowsOS; }

    public void setWindowsOS(boolean isWindowsOS) {
        this.isWindowsOS = isWindowsOS;
    }

    public boolean isSDBoot() {
		return isSDBoot;
	}

	public void setSDBoot(boolean isSDBoot) {
		this.isSDBoot = isSDBoot;
	}

	public TemplateRaidConfiguration getRaidConfiguration() {
        return raidConfiguration;
    }

    public void setRaidConfiguration(TemplateRaidConfiguration raidConfiguration) {
        this.raidConfiguration = raidConfiguration;
    }

    TemplateRaidConfiguration raidConfiguration;

    public void addTwoByTwoInterface(int i, String id) {
        if (!twoByTwoInterfaces.contains(makeCardKey(i,id)))
            twoByTwoInterfaces.add(makeCardKey(i,id));
    }

    private void addFCCard(String value) {
        fcCards.add(value);
    }

    private void addPortMapping(int i, String id, List<String> ports) {
        portUIMap.put(makeCardKey(i,id), ports);
    }

    public String hash() {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder total = new StringBuilder();
        total.append(isMinimalServer());
        total.append(isSDBoot());
        total.append(isAllFlash());
        total.append(isHardwareISCSI());
        total.append(isHyperVUsed());
        total.append(isLocalDiskVSANBoot());
        total.append(isLocalStorageForVSANEnabled());
        total.append(isWindowsOS());
        try {
            if (getRaidConfiguration() != null) {
               total.append(mapper.writeValueAsString(getRaidConfiguration()));
            }
            if (getFCCards() != null) {
               total.append(mapper.writeValueAsString(getFCCards()));
            }
            if (getPortUIMap() != null) {
                total.append(mapper.writeValueAsString(getPortUIMap()));
            }
            if (getTwoByTwoInterfaces() != null) {
                total.append(mapper.writeValueAsString(getTwoByTwoInterfaces()));
            }
            if (getIscsiInterfaces() != null) {
                total.append(mapper.writeValueAsString(getIscsiInterfaces()));
            }
            if (getPartitionedInterfaces() != null) {
                total.append(mapper.writeValueAsString(getPartitionedInterfaces()));
            }
            if (getStaticOSInstallInterfaces() != null) {
                total.append(mapper.writeValueAsString(getStaticOSInstallInterfaces()));
            }
        } catch (JsonProcessingException e) {
            logger.warn("FilterEnvironment hash function failed: ", e);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return DatatypeConverter.printHexBinary(md.digest(total.toString().getBytes()));
        } catch (NoSuchAlgorithmException e) {
            return total.toString();
        }
    }

    public boolean isHyperVUsed() {
        return isHyperVUsed;
    }

    public void setHyperVUsed(boolean isHyperVUsed) {
        this.isHyperVUsed = isHyperVUsed;
    }

    /**
     * Only will return a value if the server in question has been cloned, otherwise just return null.  Additionally only returns the 2nd part of the model name i.e.: M420
     * @param parameters:  A list of parameters for the top resource that contains the server_pool parameter.  There is a 2nd related parameter that contians the information for a cloned server
     * @return The model number of the cloned server or null if not cloned/not parse-able
     */
    private String getModelString(final List<ServiceTemplateSetting> parameters)
    {
        String configXMLString = getConfigXMLValueFromParameters(parameters);
        Document doc = loadXML(configXMLString);
        String modelName = extractModelFromDocument(doc);


        return extractModelNumberFromString(modelName);
    }

    /**
     * Iterates through all the parameters of this resource that also conatins the pool parameter.
     * @param parameters
     * @return String value of the config_xml parameter
     */
    private String getConfigXMLValueFromParameters(final List<ServiceTemplateSetting> parameters)
    {
        String configXMLString = null;
        if (parameters != null)
        {
            for (ServiceTemplateSetting setting : parameters)
            {
                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_CONFIG_XML.equals(setting.getId()))
                {
                    configXMLString = setting.getValue();
                    logger.debug("Found " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_CONFIG_XML + " Value was: " + configXMLString);
                }
            }
        }
        return configXMLString;
    }

    /**
     * A helper method to simply encapsulate all of the exceptions that can happen with loading and parsing an xml string.
     * @param configXMLString:  The xml string from the value of the config_xml parameter.
     * @return xml document representation of the afforementioned string or null
     */
    private Document loadXML(final String configXMLString)
    {
        Document doc = null;
        if (configXMLString != null)
        {
            String xmlString = StringEscapeUtils.unescapeHtml(configXMLString);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            try
            {
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(xmlString));
                doc = builder.parse(is);

                //optional, but recommended
                //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();
                logger.debug("Sucessfully loaded the xml");
            }
            catch (ParserConfigurationException | SAXException | IOException e)
            {
                logger.error(e.getMessage(),e);
            }
        }

        return doc;
    }


    /**
     * Returns the Model attribute of the root element of the xml document passed into it.
     * @param doc: Xml doc representation of a configuration that comes from cloning a server
     * @return The value of the model name attribute value of the root level or null
     */
    private String extractModelFromDocument(final Document doc)
    {
        String model = null;
        if (doc != null)
        {
            model = doc.getDocumentElement().getAttribute("Model");
        }

        logger.debug("Extracting model from document, found: " + model);
        return model;
    }

    /**
     * We only care about the 2nd part of the model name for comparison's sake.  So we expect something like PowerEdge M620 and we only want the 2nd half.
     * @param modelName Complete model name "PowerEdge M620"
     * @return The model number "M620"
     */
    public static String extractModelNumberFromString(String modelName)
    {
        String modelNumber = null;
        if (modelName != null) {
            String[] splitModelName = modelName.split("\\s+");
            if (splitModelName != null && splitModelName.length > 1)
                modelNumber = splitModelName[1];
            else {
                // NPE safety in case model is a single word
                modelNumber = modelName;
            }
        }
        return modelNumber;
    }

}