/**************************************************************************
 *   Copyright (c) 2013-2015 Dell Inc. All rights reserved.               *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xbill.DNS.TextParseException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.IpRange;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.common.utilities.ValidatedInet4Address;
import com.dell.asm.common.utilities.ValidatedInet4SubnetMask;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypoolmgr.common.IdentityPoolMgrMessageCode;
import com.dell.pg.asm.identitypoolmgr.ioidentity.IIOIdentityMgr;
import com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentity;
import com.dell.pg.asm.identitypoolmgr.network.IIPAddressPoolMgr;
import com.dell.pg.orion.common.utilities.PingUtil;

/**
 * Performs massage networks and virtual identities
 */
public class NetworkingUtil {
    private static final Logger logger = Logger.getLogger(NetworkingUtil.class);

    private final PingUtil pingUtil;
    private final LocalizableMessageService logService;
    private final DnsUtil dnsUtil;

    public NetworkingUtil(PingUtil pingUtil, LocalizableMessageService logService, DnsUtil dnsUtil) {
        this.pingUtil = pingUtil;
        this.logService = logService;;
        this.dnsUtil = dnsUtil;
    }
    
	/**
	 * Cycles through the components that are Servers and VMs and updates the
	 * existing Network Templates with new values from the existing Networks.
	 * This will update all values of the Networks except for the assigned ip
	 * address.
	 *
	 * @param components
	 *            the components that will be cycled over and whose template's
	 *            will be updated.
	 * @param networkProxy
	 *            the Network service used to access Network data in the system.
	 * @throws Exception
	 *             if something goes wrong...
	 */
	public void updateExistingNetworkTemplates(List<ServiceTemplateComponent> components,INetworkService networkProxy) 
		throws Exception {
		
		List<ServiceTemplateComponent> serverServiceTemplatecomponents = this.getServerServiceTemplateComponents(components);
		for (ServiceTemplateComponent component : serverServiceTemplatecomponents) {
			List<ServiceTemplateCategory> resources = component.getResources();
			for (ServiceTemplateCategory resource : resources) {
				List<ServiceTemplateSetting> parameters = resource
				        .getParameters();
				for (ServiceTemplateSetting param : parameters) {
					this.updateNetworkIfItExistsInNetworkList(param,networkProxy);
				}
			}
		}
	}

    /**
     * Cycles through the components that are Servers and VMs and configures and updates the Network template settings.
     *
     * @param components the components that will be cycled over and whose template's will be updated.
     * @param networkProxy the Network service used to access Network data in the system.
     * @throws Exception if something goes wrong...
     */
    public void massageNetworks(List<ServiceTemplateComponent> components, INetworkService networkProxy) throws Exception {

        // Store Retrieved Networks to keep from retrieving again and prevent unforeseen network changes messing up data.
        Map<String, Network> networksMap = new HashMap<>();
        List<String> allNames = new ArrayList<>();
        for (ServiceTemplateComponent component : components) {
            massageComponentNetworks(component,networkProxy,allNames,networksMap);
        }
    }

    public void massageComponentNetworks(ServiceTemplateComponent component, INetworkService networkProxy, List<String> allNames, Map<String,Network> networksMap) {
        // if server component or vm component
        if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                // if server networking category or
                // esxi vm category or
                // hyperv vm category
                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(resource.getId()) ||
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE.equals(resource.getId()) ||
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE.equals(resource.getId())) {
                    for (ServiceTemplateSetting param : resource.getParameters()) {
                        // if server networking configuration parameter
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(param.getId())) {
                            NetworkConfiguration configuration = ServiceTemplateUtil.deserializeNetwork(param.getValue());
                            setupVirtualIdentities(configuration, networkProxy, networksMap);
                            param.setNetworkConfiguration(configuration);
                        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals((param.getId())) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID.equals(param.getId())) {
                            List<Network> networks = new ArrayList<>();
                            if (!(param.getValue().isEmpty() || param.getValue().equals("-1"))) {
                                // value may be a comma-separated list, but if that is the case it will lead with a comma,
                                // e.g. ,1,2,3.  The reject below gets rid of the initial empty element
                                Map<String,String> existingNetworksIpsMap = new HashMap<>();
                                if (param.getNetworks() != null) {
                                    for (Network network : param.getNetworks()) {
                                        if (network.isStatic() && network.getStaticNetworkConfiguration().getIpAddress() != null) {
                                            existingNetworksIpsMap.put(network.getId(), network.getStaticNetworkConfiguration().getIpAddress());
                                        }
                                    }
                                }
                                for (String guid : param.getValue().split(",")) {
                                    if (!guid.isEmpty()) {
                                        Network network = null;
                                        if (networksMap.get(guid) == null) {
                                            com.dell.pg.asm.identitypool.api.network.model.Network identityNetwork = networkProxy.getNetwork(guid);
                                            if (identityNetwork != null) {
                                                network = new Network(identityNetwork);
                                                networksMap.put(network.getId(), network);
                                            }
                                            if (network != null && network.isStatic()) {
                                                String currentIpAddress = existingNetworksIpsMap.get(network.getId());
                                                if (currentIpAddress != null) {
                                                    network.getStaticNetworkConfiguration().setIpAddress(currentIpAddress);
                                                }
                                            }
                                        } else {
                                            network = new Network(networksMap.get(guid));
                                        }
                                        if (network != null) {
                                            if (network.getName().equals("Management Network")) {
                                                String replacement = null;
                                                int i = 1;
                                                do {
                                                    replacement = network.getName() + " " + i;
                                                    i++;
                                                } while (allNames.contains(replacement));
                                                allNames.add(replacement);
                                                network.setName(replacement);
                                            }
                                            networks.add(network);
                                        }
                                    }
                                }
                            }
                            param.setNetworks(networks);
                        }
                    }
                }
            }
        }
    }

    // Returns a List of ServiceTemplateCompoenents that are either Servers or Virtual Machines
    private ArrayList<ServiceTemplateComponent> getServerServiceTemplateComponents(List<ServiceTemplateComponent> components)
    {
    	ArrayList<ServiceTemplateComponent> serverServiceTemplateComponents = new ArrayList<>();
    	
    	for (ServiceTemplateComponent component : components){
            if(component.getType().equals(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER) ||
                    component.getType().equals(ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE)){
            	serverServiceTemplateComponents.add(component);
            }
        }
    	
    	return serverServiceTemplateComponents;
    }    
	
    public static String buildUsageGuid(String deploymentId) {
        return deploymentId;
    }

    /**
     *  Cycles through the components that are Servers and VMs and reserves any static Ip's needed for deployment.
     *  This should always be called after calling massageNetworks which sets up the networks on the component.
     * @param deployment - deployment identities should be reserved on
     * @param ioIdentityService - IdentityManager Service instance
     * @param networkService - NetworkService instance
     * @param ipAddressPoolMgr - Identity Pool Manager service instance
     */
    public void massageVirtualIdentities(Deployment deployment,
                                         IIOIdentityMgr ioIdentityService,
                                         INetworkService networkService,
                                         IIPAddressPoolMgr ipAddressPoolMgr,
                                         List<Network> reservedNetworks,
                                         boolean skipDHCP) {
        String usageGuid = buildUsageGuid(deployment.getId());

        // at this time only for Server and VCenter bare metal VM
        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
            if (component.getType().equals(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER) ||
                    (component.getType().equals(ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE))) {
                Map<String,Network> processedNetworks = new HashMap<>();

                //Stores networks names which use manually assigned static ips
                Set<String> manualStaticIpSet = new HashSet<>();
                // can not wait to loop through categories because we can not guarantee the order of the categories.
                ServiceTemplateCategory osSettings = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                if (osSettings != null) {
                    ServiceTemplateSetting ipSource = osSettings.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
                    if (ipSource != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL.equals(ipSource.getValue())) {
                        for (ServiceTemplateSetting setting : osSettings.getParameters()) {
                            // if Management IP Source
                            if (setting.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE)) {
                                //if not automatic then add network to manual set
                                if (!ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_AUTOMATIC.equals(setting.getValue())) {
                                    String networkName = setting.getId().substring(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE.length());
                                    manualStaticIpSet.add(networkName);
                                }
                            }
                        }
                    }
                }

                for (ServiceTemplateCategory category : component.getResources()) {
                    if (category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID) ||
                            category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE) ||
                            category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE)) {
                        String identityPoolId = getIdentityPoolId(category);
                        for (ServiceTemplateSetting param : category.getParameters()) {
                            if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID)) {
                                NetworkConfiguration configuration = param.getNetworkConfiguration();
                                if (configuration == null) {
                                   configuration = ServiceTemplateUtil.deserializeNetwork(param.getValue());
                                   setupVirtualIdentities(configuration,networkService,new HashMap<String, Network>());
                                }
                                massageFabrics(component, usageGuid, configuration, identityPoolId, ioIdentityService, ipAddressPoolMgr, manualStaticIpSet, processedNetworks, reservedNetworks, skipDHCP);
                                param.setNetworkConfiguration(configuration);
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals(param.getId()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID.equals(param.getId())) {
                                reserveStaticIps(component, usageGuid, ipAddressPoolMgr, param.getNetworks(), manualStaticIpSet, processedNetworks, reservedNetworks, skipDHCP);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getIdentityPoolId(ServiceTemplateCategory category) {
        String identityPoolId = null;
        for(ServiceTemplateSetting param: category.getParameters()){
            if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDENTITYPOOL_ID.equals(param.getId())){
                identityPoolId = param.getValue();
            }
        }
        return identityPoolId;
    }

    private void massageFabrics(ServiceTemplateComponent component,
                                          String usageGuid,
                                          NetworkConfiguration configuration,
                                          String identityPoolId,
                                          IIOIdentityMgr ioIdentityService,
                                          IIPAddressPoolMgr ipAddressPoolMgr,
                                          Set<String> manualStaticIpSet,
                                          Map<String,Network> processedNetworks,
                                          List<Network> reservedNetworks,
                                            boolean skipDHCP) {
        for (Fabric fabric : configuration.getInterfaces()) {
            for (Interface iface : fabric.getInterfaces()) {
                massageInterface(component, usageGuid, iface, identityPoolId, ioIdentityService, ipAddressPoolMgr, manualStaticIpSet, processedNetworks, reservedNetworks, skipDHCP);
            }
        }
    }

    private void massageInterface(ServiceTemplateComponent component,
                                  String usageGuid,
                                  Interface iface,
                                  String identityPoolId,
                                  IIOIdentityMgr ioIdentityService,
                                  IIPAddressPoolMgr ipAddressPoolMgr,
                                  Set<String> manualStaticIpSet,
                                  Map<String,Network> processedNetworks,
                                  List<Network> reservedNetworks,
                                  boolean skipDHCP) {
        if (iface.getPartitions() != null) {
            for (Partition partition : iface.getPartitions())
                massagePartition(component, usageGuid, partition, identityPoolId, ioIdentityService, ipAddressPoolMgr, manualStaticIpSet, processedNetworks, reservedNetworks, skipDHCP);
        }
    }

    private void massagePartition(ServiceTemplateComponent component,
                                  String usageGuid,
                                  Partition partition,
                                  String identityPoolId,
                                  IIOIdentityMgr ioIdentityMgr,
                                  IIPAddressPoolMgr ipAddressPoolMgr,
                                  Set<String> manualStaticIpSet,
                                  Map<String,Network> processedNetworks,
                                  List<Network> reservedNetworks,
                                  boolean skipDHCP) {
        // reserve static Ips for any static network which does not have an IP set
        reserveStaticIps(component, usageGuid, ipAddressPoolMgr, partition.getNetworkObjects(), manualStaticIpSet, processedNetworks,reservedNetworks,skipDHCP);
    }

    private List<IOIdentity> reserveAndAssignIdentities(String usageGuid,
                                                        com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType itype,
                                                        int number, String identityPoolId,
                                                        IIOIdentityMgr ioIdentityMgr) {
        List<IOIdentity> ioidlist = ioIdentityMgr.reserveIdentities(itype, usageGuid, number, 1, 1, identityPoolId);
        List<String> identities = new ArrayList<>();
        for (IOIdentity identity : ioidlist) {
            identities.add(identity.getId());
        }
        ioIdentityMgr.assignIdentities(identities, usageGuid);
        return ioidlist;
    }

    /**
     * Reserve static IPs for the passed network guids using the usage ids contained in the
     * networkGuids list.
     *
     * @param usageGuid
     * @param ipAddressPoolMgr
     * @param networks
     * @param manualStaticIpSet
     */
    private void reserveStaticIps(ServiceTemplateComponent component,
                                  String usageGuid,
                                  IIPAddressPoolMgr ipAddressPoolMgr,
                                  List<Network> networks,
                                  Set<String> manualStaticIpSet,
                                  Map<String,Network> processedNetworks,
                                  List<Network> reservedNetworks,
                                  boolean skipDHCP) {
        if (networks != null) {
            final Integer[] portsToPing =  AsmManagerApp.getPortsToPing();
            for (Network currentNetwork : networks) {
                //If HyperVisor Server and a workload network
                if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) &&
                        ServiceTemplateComponent.ServiceTemplateComponentSubType.HYPERVISOR.equals(component.getSubType()) &&
                        (NetworkType.PRIVATE_LAN.equals(currentNetwork.getType()) ||
                                NetworkType.PUBLIC_LAN.equals((currentNetwork.getType())))) {
                    //skip
                    continue;
                }
                if (currentNetwork != null && !manualStaticIpSet.contains(currentNetwork.getName())) {
                    reserveIP(usageGuid, currentNetwork, ipAddressPoolMgr, processedNetworks, reservedNetworks, skipDHCP, portsToPing);
                }
            }
        }
    }

    private void reserveIP(String usageGuid,
                           Network network,
                           IIPAddressPoolMgr ipAddressPoolMgr,
                           Map<String, Network> processedNetworks,
                           List<Network> reservedNetworks,
                           boolean skipDHCP,
                           final Integer[] portsToPing) throws WebApplicationException {
        // Track the ips that are occupied by other machiens and clear them from the db at the end
        Set<String> ipsToBeReleased = new HashSet<>();

        try {
            if (network.isStatic() &&
                    network.getStaticNetworkConfiguration() != null &&
                    StringUtils.isEmpty(network.getStaticNetworkConfiguration().getIpAddress())) {
                Set<String> ips;
                if (processedNetworks.get(network.getName()) == null) {
                    ips = ipAddressPoolMgr.assignIPAddresses(network.getId(), usageGuid, 1);
                    if (ips == null || ips.size() != 1) {
                        throw new AsmManagerRuntimeException("Failed to assign ip address for network " +
                                network.getId() + " usageGuid = " + usageGuid);
                    }
                    String ip = ips.toArray(new String[1])[0];
                    // Storage networks are not expected to be routable, so we do not attempt to connect to those IPs
                    if (!NetworkType.STORAGE_ISCSI_SAN.equals(network.getType())
                            && !NetworkType.STORAGE_FCOE_SAN.equals(network.getType())) {
                        while (pingUtil.isReachable(ip, portsToPing)) {
                            logger.warn("Tried to reserve " + ip + ", but it is occupied by another machine.");
                            logService.logMsg(AsmManagerMessages.ipAddressInUse(ip),
                                    LogMessage.LogSeverity.WARNING,
                                    LogMessage.LogCategory.DEPLOYMENT);
                            ipsToBeReleased.add(ip);
                            ips = ipAddressPoolMgr.assignIPAddresses(network.getId(), usageGuid, 1);
                            if (ips == null || ips.size() != 1) {
                                throw new AsmManagerRuntimeException("Failed to assign ip address for network " +
                                        network.getId() + " usageGuid = " + usageGuid);
                            }
                            ip = ips.toArray(new String[1])[0];
                        }
                    }
                    network.getStaticNetworkConfiguration().setIpAddress(ip);
                    processedNetworks.put(network.getName(), network);
                    reservedNetworks.add(network);
                } else {
                    Network reservedNetwork = processedNetworks.get(network.getName());
                    network.getStaticNetworkConfiguration().setIpAddress(reservedNetwork.getStaticNetworkConfiguration().getIpAddress());
                }
            } else if (!skipDHCP) {
                // skip dchp networks for network scale up
                reservedNetworks.add(network);
            }
        } catch (AsmRuntimeException e) {
            logger.error("Exception while reserving IPs.", e);
            if (IdentityPoolMgrMessageCode.IPPOOL_ENOUGH_IPADRESS_NOT_AVAILABLE.getCode().equals(e.getEEMILocalizableMessage().getDisplayMessage().getMessageCode())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.notEnoughIPs(network.getName()));
            } else {
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
            }

        } catch (Exception e) {
            logger.error("Exception while reserving IPs.", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        } finally {
            if (ipsToBeReleased.size() > 0) {
                logger.debug("Releasing the following IP addresses from the db: " + ipsToBeReleased.toString());
                ipAddressPoolMgr.releaseIPAddresses(ipsToBeReleased, network.getId());
            }
        }
    }

    private void reserveAndAssignPartitionVIds(List<IOIdentity> virtualIdentities, Partition partition, Network network, com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType itype) {
        for(IOIdentity vid : virtualIdentities){
            switch(itype){
                case MAC:
                    if(network.getType().equals(NetworkType.STORAGE_FCOE_SAN) || network.getType().equals(NetworkType.STORAGE_ISCSI_SAN)) {
                        if (StringUtils.isEmpty(partition.getIscsiMacAddress())) {
                            partition.setIscsiMacAddress(vid.getValue());
                        } else {
                            partition.setLanMacAddress(vid.getValue());
                        }
                    }else
                        partition.setLanMacAddress(vid.getValue());
                    break;
                case IQN:
                    partition.setIscsiIQN(vid.getValue());
                    break;
                case WWPN:
                    partition.setWwpn(vid.getValue());
                    break;
                case WWNN:
                    partition.setWwnn(vid.getValue());
                    break;
            }
        }
    }

    public void validateStaticIpsIfNeeded(List<ServiceTemplateComponent> components) {

        Set<String> assignedIps = new HashSet<>();
        for (ServiceTemplateComponent component : components) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                    ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals((component.getType()))) {
                boolean dnsLookupFound = false;

                // get the os settings category
                ServiceTemplateCategory osResource = component.getTemplateResource(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);

                if (osResource != null && osResource.getParameters() != null) {
                    //loop through settings saving off the static ip source settings.
                    for (ServiceTemplateSetting param : osResource.getParameters()) {
                        if (param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE)) {
                            String name = param.getId().substring(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE.length());
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_DNS.equals(param.getValue())) {
                                if (dnsLookupFound) {
                                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                            AsmManagerMessages.dnsLookupAlreadySelected(component.getName(), name));
                                } else {
                                    dnsLookupFound = true;
                                }
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL.equals(param.getValue())) {
                                ServiceTemplateSetting staticIpValue = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + name);
                                if (staticIpValue != null &&
                                        StringUtils.isNotBlank(staticIpValue.getValue())) {
                                    String ipAddress = staticIpValue.getValue();
                                    if (assignedIps.contains(ipAddress)) {
                                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                AsmManagerMessages.managementIpNotAvailable(component.getName(),
                                                        name, ipAddress));
                                    } else {
                                        assignedIps.add(ipAddress);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Find all components in the template that have requested an override for the default-assigned
     * static management IP address. Replace those IPs with the requested one.
     *
     * The original management IPs will be released from ipAddressPoolMgr. The requested IP will be
     * reserved if it is part of one of the management network's static IP ranges.
     *
     * @param usageGuid The usage guid to reserve the IP with
     * @param components The components to update
     * @param ipAddressPoolMgr The ip address pool manager
     */
    public void updateStaticIpsIfNeeded(String usageGuid,
                                        List<ServiceTemplateComponent> components,
                                        IIPAddressPoolMgr ipAddressPoolMgr,
                                        List<Network> reservedNetworks) {
        Set<String> requestedIpSet = new HashSet<>();
        for (ServiceTemplateComponent component : components) {
            // If server
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                    ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals((component.getType()))) {
                Map<Network,Set<String>> networkingMap = new HashMap<>();
                Map<String,Set<String>> releaseNetworkingMap = new HashMap<>();
                // Map of all static managment Ip source settings.
                Map<String, ServiceTemplateSetting> settingsMap = new HashMap<>();
                // find all static networks on the component
                List<Network> networks = ServiceTemplateClientUtil.findStaticNetworks(component);
                // if not empty
                if (CollectionUtils.isNotEmpty(networks)) {
                    // get the os settings category
                    ServiceTemplateCategory osResource = component.getTemplateResource(
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);

                    if (osResource != null && osResource.getParameters() != null) {
                        ServiceTemplateSetting ipSource = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);

                        if (ipSource != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL.equals(ipSource.getValue())) {
                            //loop through settings saving off the static ip source settings.
                            for (ServiceTemplateSetting param : osResource.getParameters()) {
                                if (param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE) ||
                                        param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE)) {
                                    settingsMap.put(param.getId(), param);
                                }
                            }
                        }
                    }

                    ServiceTemplateSetting generateParam = null;
                    ServiceTemplateSetting templateParam = null;
                    ServiceTemplateSetting param = null;

                    if (settingsMap != null && settingsMap.size() > 0) {
                        //Loop through static networks
                        for (Network network : networks) {
                            if (networkingMap.get(network) == null) {

                                //Get the network management IP source setting
                                ServiceTemplateSetting networkParam = settingsMap.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
                                if (networkParam != null) {
                                    Set<String> ipAddresses = networkingMap.get(network);
                                    if (ipAddresses == null) {
                                        ipAddresses = new HashSet<>();
                                    }
                                    // if static ip source set to manual
                                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL.equals(networkParam.getValue())) {
                                        ServiceTemplateSetting manualIp = osResource.getParameter(
                                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                                        String ipAddress = manualIp.getValue();
                                        if (!ASMCommonsUtils.isValidIp(ipAddress)) {
                                            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                    AsmManagerMessages.invalidManagementIp(ipAddress));
                                        } else {
                                            ipAddresses.add(ipAddress);
                                        }
                                        //else if static ip source set to dns server
                                    } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_DNS.equals(networkParam.getValue())) {
                                        Set<String> dnsServers = new HashSet<>();
                                        Set<String> dnsSuffixes = new HashSet<>();
                                        StaticNetworkConfiguration staticConfig = network.getStaticNetworkConfiguration();
                                        if (network.isStatic() && staticConfig != null) {
                                            if (!StringUtils.isBlank(staticConfig.getPrimaryDns())) {
                                                dnsServers.add(staticConfig.getPrimaryDns());
                                            }
                                            if (!StringUtils.isBlank(staticConfig.getSecondaryDns())) {
                                                dnsServers.add(staticConfig.getSecondaryDns());
                                            }
                                            if (!StringUtils.isBlank(staticConfig.getDnsSuffix())) {
                                                dnsSuffixes.add(staticConfig.getDnsSuffix());
                                            }
                                        }

                                        if (dnsServers.size() < 1) {
                                            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                    AsmManagerMessages.noDnsServersForManagementNetwork(component.getName()));
                                        }

                                        if (generateParam == null) {
                                            generateParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
                                        }
                                        if (generateParam != null && "true".equals(generateParam.getValue())) {
                                            if (templateParam == null) {
                                                templateParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID);
                                            }
                                            if (templateParam != null && HostnameUtil.containsDnsPattern(templateParam.getValue())) {
                                                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                        AsmManagerMessages.dnsHostnameAndDnsIpOptionsConflict(component.getName()));
                                            }
                                        }

                                        if (param == null) {
                                            param = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                                        }
                                        if (param == null || !HostnameUtil.isValidHostName(param.getValue(), component)) {
                                            // A valid hostname is required for deployment, so not having one is an internal error
                                            throw new AsmManagerRuntimeException("Valid hostname not set");
                                        }
                                        String hostName = param.getValue();
                                        try {
                                            String[] dnsServersArray = dnsServers.toArray(new String[dnsServers.size()]);
                                            String ipAddress = dnsUtil.lookup(hostName, dnsServersArray);
                                            Iterator<String> suffixesIterator = dnsSuffixes.iterator();
                                            while (ipAddress == null && suffixesIterator.hasNext()) {
                                                ipAddress = dnsUtil.lookup(hostName + "." + suffixesIterator.next(), dnsServersArray);
                                            }
                                            if (ipAddress == null) {
                                                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                        AsmManagerMessages.dnsLookupFailed(hostName, dnsServers.iterator().next()));
                                            }
                                            ipAddresses.add(ipAddress);
                                        } catch (TextParseException e) {
                                            // A valid hostname is required for deployment, so not having one is an internal error
                                            throw new AsmManagerRuntimeException("Invalid hostname", e);
                                        }
                                    }
                                    if (CollectionUtils.isNotEmpty(ipAddresses)) {
                                        if (requestedIpSet.contains(ipAddresses)) {
                                            String[] ipArray = ipAddresses.toArray(new String[ipAddresses.size()]);
                                            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                    AsmManagerMessages.managementIpNotAvailable(component.getName(),
                                                            network.getName(), ipArray[0]));
                                        }
                                        StaticNetworkConfiguration staticConfig = network.getStaticNetworkConfiguration();
                                        if (staticConfig != null) {
                                            if (staticConfig.getGateway() != null && staticConfig.getSubnet() != null) {
                                                // validate the subnet and gateway for the ip addresses being reserved.
                                                for (String ipToAssign : ipAddresses) {
                                                    if (!isValidSubnetForIp(ipToAssign, staticConfig.getGateway(), staticConfig.getSubnet())) {
                                                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                                AsmManagerMessages.invalidManagementIpForNetwork(component.getName(), network.getName(), ipToAssign));
                                                    }
                                                }
                                            }
                                            if (staticConfig.getIpAddress() != null) {
                                                Set<String> releaseIps = releaseNetworkingMap.get(network.getId());
                                                if (releaseIps == null) {
                                                    releaseIps = new HashSet<>();
                                                    releaseNetworkingMap.put(network.getId(), releaseIps);
                                                }
                                                releaseIps.add(staticConfig.getIpAddress());
                                            }
                                        }
                                        requestedIpSet.addAll(ipAddresses);
                                        networkingMap.put(network, ipAddresses);
                                    }
                                }
                            } else {
                                Set<String> ipAddresses = networkingMap.get(network);
                                if (CollectionUtils.isNotEmpty(ipAddresses)) {
                                    for (String ipAddress : ipAddresses) {
                                        network.getStaticNetworkConfiguration().setIpAddress(ipAddress);
                                    }
                                }
                            }
                        }
                    }
                    for (Map.Entry<String,Set<String>> entry : releaseNetworkingMap.entrySet()) {
                        // Release old management IPs. It is important to release all IPs before assigning any
                        // new ones to support cases where the user is just re-ordering the assigned IPs.
                        ipAddressPoolMgr.releaseIPAddresses(entry.getValue(), entry.getKey());
                    }
                    for (Map.Entry<Network,Set<String>> entry : networkingMap.entrySet()) {
                        // Reserve new IPs and set on management networks
                        replaceManagementIp(usageGuid, entry.getKey(), entry.getValue(), component, ipAddressPoolMgr, reservedNetworks);
                    }
                }
            }
        }
    }

    void replaceManagementIp(String usageGuid,
                             Network network,
                             Set<String> ipAddresses,
                             ServiceTemplateComponent component,
                             IIPAddressPoolMgr ipAddressPoolMgr,
                             List<Network> reservedNetworks) {
        final Integer[] portsToPing =  AsmManagerApp.getPortsToPing();
        for (String ipAddress : ipAddresses) {
            if (!NetworkType.STORAGE_ISCSI_SAN.equals(network.getType())
                    && !NetworkType.STORAGE_FCOE_SAN.equals(network.getType())) {
                if (pingUtil.isReachable(ipAddress, portsToPing)) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.managementIpInUse(ipAddress));
                }
            }
            // Reserve the new one
            try {
                Set<String> reservedIps = ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(network.getId(), usageGuid,
                        new HashSet<>(Arrays.asList(ipAddress)));
                if (!reservedIps.contains(ipAddress)) {
                    // This should never occur, ipAddressPoolMgr should throw an exception if it
                    // can't assign existing IPs or create temporary ones.
                    logger.error("ipAddressPoolMgr failed to create temporary ip " + ipAddress);
                    throw new AsmRuntimeException(AsmManagerMessages.internalError());
                }
            } catch (AsmRuntimeException e) {
                // This is the case where the ip was already reserved
                logger.info("Failed to reserve ip " + ipAddress + " from " + network.getName(), e);
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.managementIpNotAvailable(component.getName(),
                                network.getName(), ipAddress));
            }
            network.getStaticNetworkConfiguration().setIpAddress(ipAddress);
            reservedNetworks.add(network);
        }
    }

    // WARNING: this is copy-paste from identitypoolmgr's ValidatedStaticNetworkProperties
    private boolean ipAddressesOnSameSubnet(List<ValidatedInet4Address> addressesToCheck, ValidatedInet4SubnetMask mask) {
        boolean result = true;
        ValidatedInet4Address comparisonBase = null;
        for (ValidatedInet4Address address : addressesToCheck) {
            if (null == comparisonBase) {
                comparisonBase = address.getNetworkPrefix(mask);
                continue;
            }
            if (!comparisonBase.equals(address.getNetworkPrefix(mask))) {
                result = false;
                break;
            }
        }
        return result;
    }

    private boolean isValidSubnetForIp(String ipAddress, String gateway, String subnet) {
        if (gateway == null || subnet == null) {
            return true;
        }
        ValidatedInet4SubnetMask mask = new ValidatedInet4SubnetMask(subnet);
        List<ValidatedInet4Address> ips = new ArrayList<>();
        ips.add(new ValidatedInet4Address(gateway));
        ips.add(new ValidatedInet4Address(ipAddress));
        return ipAddressesOnSameSubnet(ips, mask);
    }
    
    // Updates Networks for the Params by loading the Networks latest data from the database 
    private void updateNetworkIfItExistsInNetworkList(ServiceTemplateSetting param, INetworkService networkProxy){
    	if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals(param.getId())) {
            List<Network> networks = param.getNetworks();
            if (networks != null && !networks.isEmpty()) {
                for(Network network: networks){
                	this.refreshNetwork(network, networkProxy);
                }
            }
        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(param.getId())) {
            NetworkConfiguration networkConfig = param.getNetworkConfiguration();
            List<Network> networks = networkConfig.getNetworks();
            if (networks != null && !networks.isEmpty()) {
	            for(Network network: networks){
	                this.refreshNetwork(network, networkProxy);
	            }
	        }
        }
    }
    
    
    // Looks up the Network from the database and updates the Network passed in with the data from the db
    private void refreshNetwork(Network network, INetworkService networkProxy){
    	if(network != null && network.getId() != null){
    		Network networkFromDB = new Network(networkProxy.getNetwork(network.getId()));
    		this.updateNetwork(network, networkFromDB);
    	}    	
    }
    
    
    // Updates all of the network data except for the ip address of the Network  
    // WARNING:  Do NOT change this to update the ipAddress, this is used for refreshing Network settings due to drift 
    private void updateNetwork(Network networkToUpdate, Network network){
    	if(networkToUpdate != null && network != null){

    		networkToUpdate.setName(network.getName());
    		networkToUpdate.setDescription(network.getDescription());
    		networkToUpdate.setType(network.getType());
    		networkToUpdate.setVlanId(network.getVlanId());
    		networkToUpdate.setStatic(network.isStatic());
    		// networkToUpdate.setStaticNetworkConfiguration(network.getStaticNetworkConfiguration());
	
	        if(network.isStatic() && network.getStaticNetworkConfiguration() != null){
	            //add ip class
	        	if(networkToUpdate.getStaticNetworkConfiguration() == null) networkToUpdate.setStaticNetworkConfiguration(new StaticNetworkConfiguration());
	        	networkToUpdate.getStaticNetworkConfiguration().setDnsSuffix(network.getStaticNetworkConfiguration().getDnsSuffix());
	        	networkToUpdate.getStaticNetworkConfiguration().setGateway(network.getStaticNetworkConfiguration().getGateway());
	        	networkToUpdate.getStaticNetworkConfiguration().setPrimaryDns(network.getStaticNetworkConfiguration().getPrimaryDns());
	        	networkToUpdate.getStaticNetworkConfiguration().setSecondaryDns(network.getStaticNetworkConfiguration().getSecondaryDns());
	        	networkToUpdate.getStaticNetworkConfiguration().setSubnet(network.getStaticNetworkConfiguration().getSubnet());
	        	List<IpRange> ipRange = networkToUpdate.getStaticNetworkConfiguration().getIpRange();
	            ipRange.clear();
	            ipRange.addAll(network.getStaticNetworkConfiguration().getIpRange());
	        }
    	}
    }   
    
    /**
     * Loops through the List of Networks and checks to see if any of them are configured to be static.  Returns true
     * if all networks are configured statically or false if one is not configured to be static.
     * 
     * @param networks a list of Networks whose configuration will be checked.
     * @return true if all networks are configured statically or false if one is not configured to be static.
     */
    public static boolean areAllNetworksConfiguredToBeStatic(List<com.dell.pg.asm.identitypool.api.network.model.Network> networks){
    	boolean configuredStatic = true;
    	for(com.dell.pg.asm.identitypool.api.network.model.Network network : networks){
    		if(!network.isStatic()){
    			configuredStatic = false;
    			break; // no need to continue looking
    		}
    	}
    	return configuredStatic;
    }

    private void setupVirtualIdentities(NetworkConfiguration configuration,
                                        INetworkService networkService,
                                        Map<String,Network> networksMap) {
        NetworkNames networkNames = new NetworkNames();

        if (configuration != null) {
            for (Fabric fabric : configuration.getInterfaces()) {
                redundancyCheck(fabric);
                for (Interface iface : fabric.getInterfaces()) {
                    setupInterface(iface, networkService, networksMap, networkNames);
                }
            }

            // rollback network names - remove "IP Partition N" when number of such networks exceeds number of ports
            for (Fabric fabric : configuration.getInterfaces()) {
                for (Interface iface : fabric.getInterfaces()) {
                    if (iface.getPartitions() != null) {
                        for (Partition partition : iface.getPartitions()) {
                            for (Network network: partition.getNetworkObjects()) {
                                // restore names
                                if (networkNames.getIscsiNames().size() > networkNames.getIscsiPorts()
                                                && NetworkType.STORAGE_ISCSI_SAN.equals(network.getType())) {
                                    network.setName(networksMap.get(network.getId()).getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupInterface(Interface iface,
                                INetworkService networkService,
                                Map<String,Network> networksMap,
                                   NetworkNames networkNames) {
        if (iface.getPartitions() != null) {
            boolean hasISCSI = false;
            for (Partition partition : iface.getPartitions()) {
                setupPartition(partition, networkService, networksMap, networkNames);
                // scan for special networks i.e/ ISCSI
                for (Network network: partition.getNetworkObjects()) {
                    if (NetworkType.STORAGE_ISCSI_SAN.equals(network.getType())) {
                        hasISCSI = true;
                    }
                }
            }
            if (hasISCSI) {
                networkNames.incrementIscsiPorts();
            }
        }
    }

    public static void redundancyCheck(Fabric fabric) {
        if (fabric.isRedundancy()) {
            if (Interface.NIC_4_X_10GB.equals(fabric.getNictype())) {
                cloneInterface(fabric.getInterfaces().get(0), fabric.getInterfaces().get(1));
                cloneInterface(fabric.getInterfaces().get(2), fabric.getInterfaces().get(3));
            } else {
                cloneInterface(fabric.getInterfaces().get(0), fabric.getInterfaces().get(1));
            }
        }
    }

    private static void cloneInterface(Interface a1, Interface a2) {
        // Check if the interface has already been cloned.
        // We are reusing network configurations for certain scaleups which
        // means the interface may have already been cloned.
        if (a1.isIdenticalInterface(a2)) {
            return;
        }
        a2.setPartitioned(a1.isPartitioned());
        a2.setPartitions(new ArrayList<Partition>());
        for (Partition partition : a1.getPartitions()) {
            Partition np = new Partition();
            np.setId(partition.getId());
            np.setName(partition.getName());
            np.setMinimum(partition.getMinimum());
            np.setMaximum(partition.getMaximum());
            np.setNetworks(new ArrayList<String>());
            if (partition.getNetworks() != null) {
                for (String network : partition.getNetworks()) {
                    np.getNetworks().add(network);
                }
            }
            a2.getPartitions().add(np);
        }
    }

    /**
     * Massage networks on partition
     * @param partition
     * @param networkService
     * @param networksMap
     * @param networkNames
     * @return
     */
    private void setupPartition(Partition partition,
                                INetworkService networkService,
                                Map<String,Network> networksMap,
                                   NetworkNames networkNames) {
        Map<String,Network> currentNetworkIdsMap = new HashMap<>();
        if (partition != null && partition.getNetworkObjects() != null) {
            for (Network network : partition.getNetworkObjects()) {
                currentNetworkIdsMap.put(network.getName(), network);
            }
        }
        List<Network> networksReserved = setupPartitionNetworks(partition.getNetworks(), currentNetworkIdsMap, networkService, networksMap, networkNames);
        if (networksReserved != null) {
            partition.setNetworkObjects(networksReserved);
        }
    }

    private List<Network> setupPartitionNetworks(List<String> networkGuids,
                                                Map<String, Network> currentNetworkIdsMap,
                                                INetworkService networkService,
                                                Map<String, Network> networksMap,
                                                NetworkNames networkNames) {
        List<Network> networkObjects = new ArrayList<>();
        if (networkGuids != null) {
            for (String networkId : networkGuids) {
                Network updatedNetwork = networksMap.get(networkId);
                if (updatedNetwork != null) {
                    Network newNetwork = new Network(updatedNetwork);
                    if (NetworkType.STORAGE_ISCSI_SAN.equals(newNetwork.getType())) {
                        ensureUniqueISCSIName(networkNames.getIscsiNames(),newNetwork);
                    }

                    Network currentNetwork = currentNetworkIdsMap.get(newNetwork.getName());
                    if (currentNetwork != null) {
                        if (newNetwork.isStatic() && currentNetwork.isStatic() && currentNetwork.getStaticNetworkConfiguration().getIpAddress() != null) {
                            newNetwork.getStaticNetworkConfiguration().setIpAddress(currentNetwork.getStaticNetworkConfiguration().getIpAddress());
                        }
                    }
                    networkObjects.add(newNetwork);
                } else {
                    com.dell.pg.asm.identitypool.api.network.model.Network net = networkService.getNetwork(networkId);
                    if (net != null) {
                        Network network = new Network(net);
                        networksMap.put(network.getId(), network);
                        // special case for ISCSI: unique name per partition unsures new static IP
                        if (NetworkType.STORAGE_ISCSI_SAN.equals(network.getType())) {
                            network = new Network(network);
                            ensureUniqueISCSIName(networkNames.getIscsiNames(),network);
                        }
                        Network currentNetwork = currentNetworkIdsMap.get(network.getName());
                        if (currentNetwork != null) {
                            if (network.isStatic() && currentNetwork.isStatic() && currentNetwork.getStaticNetworkConfiguration().getIpAddress() != null) {
                                network.getStaticNetworkConfiguration().setIpAddress(currentNetwork.getStaticNetworkConfiguration().getIpAddress());
                            }
                        }
                        networkObjects.add(network);
                    }
                }
            }
        }
        return networkObjects;
    }

    private void ensureUniqueISCSIName(Set<String> iscsiNames, Network network) {
        int i = 0;
        String name = network.getName() + " IP Partition ";
        String replacement = null;
        do {
            i++;
            replacement = name + i;
        } while (iscsiNames.contains(replacement));
        network.setName(replacement);
        iscsiNames.add(replacement);
    }

    public void releaseReservedComponentIPAddresses(List<Network> reservedNetworks, IIPAddressPoolMgr ipAddressPoolMgr) {
        Map<String, Set<String>> networkIdsToIpsMap = new HashMap<>();
        if (reservedNetworks != null && reservedNetworks.size() > 0) {
            for (Network network : reservedNetworks) {
                if (network.isStatic()) {
                    StaticNetworkConfiguration staticConfig = network.getStaticNetworkConfiguration();
                    if (staticConfig != null && staticConfig.getIpAddress() != null) {
                        Set<String> ipAddresses = networkIdsToIpsMap.get(network.getId());
                        if (ipAddresses == null) {
                            ipAddresses = new HashSet<>();
                            networkIdsToIpsMap.put(network.getId(), ipAddresses);
                        }
                        ipAddresses.add(staticConfig.getIpAddress());
                    }
                }
            }
        }

        for (Map.Entry<String, Set<String>> entry : networkIdsToIpsMap.entrySet()) {
            String networkId = entry.getKey();
            Set<String> ipAddresses = entry.getValue();
            try {
                ipAddressPoolMgr.releaseIPAddresses(ipAddresses, networkId);
            } catch (Exception e) {
                logger.warn("Error releasing IpAddresses of " + StringUtils.join(ipAddresses, "','")
                        + " for Network " + networkId, e);
                // SWALLOW and Ignore this exception as it does not affect overall process flow;
            }
        }
    }

    /**
     * Reserve Virtual Identities for reserved static networks for the server components on this deployment
     * @param usageGuid
     * @param components
     * @param reservedNetworks
     * @param ioIdentityMgr
     */
    public void reserveVirtualIdentitiesForServers(String usageGuid,
                                                  List<ServiceTemplateComponent> components,
                                                  List<Network> reservedNetworks,
                                                  IIOIdentityMgr ioIdentityMgr) {
        for (ServiceTemplateComponent component : components) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                ServiceTemplateCategory networkResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);
                if (networkResource != null) {
                    String identityPoolId = null;
                    ServiceTemplateSetting identityPoolSetting = networkResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDENTITYPOOL_ID);
                    if (identityPoolSetting != null) {
                        identityPoolId = identityPoolSetting.getValue();
                    }
                    // Check the network configuration widget
                    ServiceTemplateSetting serverNetworking = networkResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                    if (serverNetworking != null) {
                        Set<Network> networkSet = new HashSet<>(reservedNetworks);
                        NetworkConfiguration networkConfiguration = serverNetworking.getNetworkConfiguration();
                        if (networkConfiguration != null) {
                            for (Fabric fabric : networkConfiguration.getInterfaces()) {
                                for (Interface iface : fabric.getInterfaces()) {
                                    if (iface.getPartitions() != null) {
                                        List<Partition> partitions = iface.getPartitions();
                                        if (!iface.isPartitioned()) {
                                            // Discard all except first partition
                                            partitions = new ArrayList<>(Arrays.asList(partitions.get(0)));
                                        }
                                        for (Partition partition : partitions) {
                                            if (partition.getNetworkObjects() != null) {
                                                for (Network network : partition.getNetworkObjects()) {
                                                    if (networkSet.contains(network)) {
                                                        reserveVirtualIdentitiesForNetwork(network, usageGuid, identityPoolId, partition, ioIdentityMgr);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void reserveVirtualIdentitiesForNetwork(Network network, String usageGuid, String identityPoolId, Partition partition, IIOIdentityMgr ioIdentityMgr) {
        switch (network.getType()) {
        case PUBLIC_LAN:
        case PRIVATE_LAN:
            reserveAndAssignPartitionVIds(reserveAndAssignIdentities(usageGuid,
                    com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.MAC,
                    1, identityPoolId, ioIdentityMgr),
                    partition,
                    network,
                    com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.MAC);
            break;
        case STORAGE_ISCSI_SAN:
        case STORAGE_FCOE_SAN:
            reserveAndAssignPartitionVIds(reserveAndAssignIdentities(usageGuid, com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.IQN, 1, identityPoolId, ioIdentityMgr),
                    partition,
                    network,
                    com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.IQN);
            reserveAndAssignPartitionVIds(reserveAndAssignIdentities(usageGuid, com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.MAC, 2, identityPoolId, ioIdentityMgr),
                    partition,
                    network,
                    com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.MAC);
            break;
        case OOB_OR_INFRASTRUCTURE_MANAGEMENT:
            break;
        case HYPERVISOR_MANAGEMENT:
            break;
        }
    }

    /**
     * Container for ISCSI, VSAN etc unique network names. Used by massage networks.
     * Note: VSAN currently removed by ASM-8602
     */
    private class NetworkNames {
        public Set<String> getIscsiNames() {
            return iscsiNames;
        }

        public void setIscsiNames(Set<String> iscsiNames) {
            this.iscsiNames = iscsiNames;
        }

        private Set<String> iscsiNames = new HashSet<>();

        public int getIscsiPorts() {
            return iscsiPorts;
        }

        public void incrementIscsiPorts() {
            this.iscsiPorts++;
        }

        private int iscsiPorts = 0;

    }
}
