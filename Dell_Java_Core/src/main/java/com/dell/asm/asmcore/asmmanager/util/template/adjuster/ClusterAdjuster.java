/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.template.adjuster;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Queue;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ClusterAdjuster implements IComponentAdjuster {

    private static final Logger LOGGER = Logger.getLogger(ClusterAdjuster.class);
    private static ClusterAdjuster instance;

    public INetworkService getNetworkProxy() {
        if (networkProxy == null)
            networkProxy = ProxyUtil.getNetworkProxy();
        return networkProxy;
    }

    public void setNetworkProxy(INetworkService networkProxy) {
        this.networkProxy = networkProxy;
    }

    private INetworkService networkProxy;

    /**
     * Private constructor.
     */
    private ClusterAdjuster() {

    }

    public static ClusterAdjuster getInstance() {
        if (instance == null)
            instance = new ClusterAdjuster();
        return instance;
    }


    @Override
    public void refine (ServiceTemplateComponent refineComponent, ServiceTemplate referencedTemplate) {
        List<PartitionNetworks> networks = new ArrayList<>();

        final Map<String, ServiceTemplateComponent> relComps = referencedTemplate.fetchComponentsMap();
        boolean hasServer = false;

        Map<String, String> featureSet = new HashMap<>();

        if (CollectionUtils.isNotEmpty(refineComponent.getAssociatedComponents().keySet())) {
            for (String key : refineComponent.getAssociatedComponents().keySet()) {
                final ServiceTemplateComponent relComp = relComps.get(key);
                if (relComp != null && ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(relComp.getType())) {
                    ServiceTemplateSetting storageType = relComp.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                            ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID);
                    // only interested if set to All Flash
                    if (storageType != null &&
                            ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH.equals(storageType.getValue()) &&
                            ServiceTemplateUtil.checkDependency(relComp, storageType)) {
                        featureSet.put(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID, ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH);
                    }

                    hasServer = true;
                    List<PartitionNetworks> serverNets = getUniqueNetworkList(relComp, referencedTemplate.isTemplateLocked());
                    if (serverNets != null) {
                        for (PartitionNetworks pn : serverNets) {
                            int idx = networks.indexOf(pn);
                            if (idx < 0)
                                networks.add(pn);
                            else {
                                networks.get(idx).increment();
                                if (pn.getIscsiNetworks().size() > 0) {
                                    for (NetworkObject networkObject : pn.getIscsiNetworks()) {
                                        if (!networks.get(idx).getIscsiNetworks().contains(networkObject))
                                            networks.get(idx).addIscsiNetwork(networkObject);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        enableFeatures(refineComponent, featureSet);
        // for scale up case the cluster might be connected to some server
        refineClusterByServerNetworks(refineComponent, networks, hasServer);
        refineClusterForSDRS(refineComponent, referencedTemplate);
    }

    /**
     * Update cluster template with storage DRS settings and options
     * @param cluster Cluster ServiceTemplateComponent
     * @param referencedTemplate referenced ServiceTemplate
     */
    private void refineClusterForSDRS(ServiceTemplateComponent cluster, ServiceTemplate referencedTemplate) {
        List<ServiceTemplateComponent> storageComponents = ServiceTemplateUtil.getAssociatedStorageComponentsFromCluster(cluster, referencedTemplate);
        ServiceTemplateSetting sdrsEnable = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID);
        ServiceTemplateSetting sdrsName = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID);
        ServiceTemplateSetting dsNames = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID);

        if (storageComponents.size() < 2) {
            if (sdrsEnable != null) {
                sdrsEnable.setValue("false");
                sdrsEnable.setHideFromTemplate(true);
                sdrsName.setHideFromTemplate(true);
                sdrsName.setValue(null);
                dsNames.setHideFromTemplate(true);
                dsNames.setValue(null);
            }
        } else {
            if (sdrsEnable != null){
                sdrsEnable.setHideFromTemplate(false);
                sdrsName.setHideFromTemplate(false);
                dsNames.setHideFromTemplate(false);
                for (ServiceTemplateComponent dsComponent : storageComponents) {
                    for (String storageType : ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_RESOURCE_LIST) {
                        ServiceTemplateCategory storageCat = dsComponent.getTemplateResource(storageType);
                        if (storageCat != null) {
                            // for all otions but existing volume
                            if (ServiceTemplateClientUtil.isNewStorageVolume(storageCat, true)) {
                                dsNames.getOptions().add(new ServiceTemplateOption(
                                        dsComponent.getName(),
                                        dsComponent.getId(), null, null));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Enable / disable cluster features based on server attributes.
     * @param cluster
     * @param featureSet
     */
    private void enableFeatures(ServiceTemplateComponent cluster, Map<String, String> featureSet) {
        boolean allFlash = false;

        if (featureSet.containsKey(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID)) {
            if (ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH.equals(
                    featureSet.get(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID))) {
                allFlash = true;
            }
        }
        ServiceTemplateCategory resource = cluster.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID);
        if (resource != null) {
        	for (ServiceTemplateSetting setting : resource.getParameters()) {
        		if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID.equals(setting.getId()) ||
        				ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID.equals(setting.getId()) ||
        				ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID.equals(setting.getId())) {
        			setting.setHideFromTemplate(!allFlash);
        			if (setting.isHideFromTemplate()
        					&& ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID.equals(setting.getId())) {
        				setting.setValue("false");
        			}
        		}
        	}
        }

    }

    /**
     * If has NO server, create predefined VDS and port group:
     * PXE VDS - [User selects from VDS available in the datacenter]
     * PXE Port Group - [ User selects from available port groups on the PXE VDS]
     * Workload VDS - [ User selects from VDS available in the datacenter]
     *
     * @param cluster
     * @param allNetworks
     * @param hasServer
     */
    private void refineClusterByServerNetworks(ServiceTemplateComponent cluster, List<PartitionNetworks> allNetworks, boolean hasServer) {
        // check if it is vCenter cluster
        ServiceTemplateCategory vdsCategory = cluster.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID);
        if (vdsCategory == null)
            return;

        int v = 1;
        ServiceTemplateSetting vdsNameZero = cluster.getParameter(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID);

        // newly added VDS members
        List<ServiceTemplateSetting> vdsAdded = new ArrayList<>();

        ServiceTemplateSetting vdsNew = null;
 
        if (hasServer) {
            
            //Restore option Enable VMWare vSAN if server is associated with the cluster          
            ServiceTemplateSetting enableVmwareVsan = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID);
            if (enableVmwareVsan != null) {
                enableVmwareVsan.setHideFromTemplate(false);
            }
            
            // first need to count some networks
            List <Network> iscsiNets = new ArrayList<>();
            List <Network> vsanNets = new ArrayList<>();

            for (PartitionNetworks pn : allNetworks) {
                for (Network nConfig : pn.getNetworks() ) {
                    if (NetworkType.STORAGE_ISCSI_SAN.equals(nConfig.getType())) {
                        // replace "iscsi" in the network ID by combination of sorted ISCSI net IDs
                        List<String> sortedNetIDs = pn.sortISCSINetworks();
                        nConfig.setId(StringUtils.join(sortedNetIDs,"-"));

                        // will need to count later
                        if (!iscsiNets.contains(nConfig)) {
                            iscsiNets.add(nConfig);
                        }
                    }
                }
            }

            for (PartitionNetworks pn : allNetworks) {
                pn.sortById();

                ServiceTemplateSetting vdsName = cluster.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                        ServiceTemplateClientUtil.createVDSID(pn.getId()));

                String uiGroupName = "VDS " + v;
                if (vdsName == null) {
                    vdsName = ServiceTemplateClientUtil.createVDSNameSetting(cluster, vdsCategory, ServiceTemplateClientUtil.createVDSID(pn.getId()),
                            "VDS Name", uiGroupName, ServiceTemplateClientUtil.copyOptions(vdsNameZero.getOptions(), null));
                }else{
                    // upgrade options only
                    vdsName.setOptions(ServiceTemplateClientUtil.copyOptions(vdsNameZero.getOptions(), null));
                }

                // hard reset for UI group
                vdsName.setGroup(uiGroupName);

                vdsName.setHideFromTemplate(false);
                vdsAdded.add(vdsName);

                // $new$
                vdsNew = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vdsName.getId());
                if (vdsNew!=null) {
                    vdsNew.setGroup(uiGroupName);
                    vdsAdded.add(vdsNew);
                }

                if (pn.hasManagementNetwork()) {
                    vdsName.setRequired(true);
                    if (vdsNew != null)
                        vdsNew.setRequired(true);
                }

                // for each network find or create PG
                Queue<NetworkObject> iscsiNetworkFIFO = new LinkedList<>();
                iscsiNetworkFIFO.addAll(pn.getIscsiNetworks());

                for (Network nConfig : pn.getNetworks()) {
                    Queue<NetworkObject> currentQueue = null;
                    String portGroupName = nConfig.getName() + " Port Group";
                    int cnt = 1;
                    if (NetworkType.STORAGE_ISCSI_SAN.equals(nConfig.getType())) {
                        currentQueue = iscsiNetworkFIFO;
                        if (iscsiNets.size() == 1) {
                            cnt = 2; // 2 PG but only if we have one ISCSI network.
                        }
                    }

                    boolean incrementPortGroup = (cnt > 1 && currentQueue.size() == 1);
                    // multiple PGs for certain networks
                    for (int j = 1; j <= cnt; j++) {
                        String currGroupName = portGroupName;
                        String portGroupSufix = "";
                        if (incrementPortGroup) {
                            portGroupSufix = " " + j;
                        }

                        String pgNetworkID = nConfig.getId();
                        // can be only 1 or 2 ISCSI.
                        // But we always need 2 port groups for such networks.
                        // Names and IDs have to be picked from dedicated list
                        if (pgNetworkID.contains("-") && currentQueue != null) {
                            NetworkObject networkObject = currentQueue.remove();
                            if (networkObject != null) {
                                pgNetworkID = networkObject.getId();
                                currGroupName = networkObject.getName() + " Port Group";
                            }
                        }

                        currGroupName += portGroupSufix;


                        ServiceTemplateSetting vdsPG = ServiceTemplateClientUtil.getPortGroup(cluster, pn.getId(), currGroupName, pgNetworkID, j, true);
                        if (vdsPG == null) {
                            // unexpected...
                            LOGGER.error("getPortGroup returned null for VDS ID=" + pn.getId() + ", PG=" + currGroupName);
                            throw new LocalizedWebApplicationException(
                                    Response.Status.INTERNAL_SERVER_ERROR,
                                    AsmManagerMessages.internalError());
                        }
                        vdsPG.setDisplayName(currGroupName);
                        vdsPG.setHideFromTemplate(false);
                        vdsPG.setGroup(uiGroupName);

                        vdsAdded.add(vdsPG);
                        // $new$
                        vdsNew = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vdsPG.getId());
                        if (vdsNew != null) {
                            vdsNew.setGroup(uiGroupName);
                            vdsAdded.add(vdsNew);
                        }

                        if (NetworkType.PXE.equals(nConfig.getType()) ||
                                NetworkType.HYPERVISOR_MANAGEMENT.equals(nConfig.getType())) {
                            vdsPG.setRequired(true);
                            if (vdsNew != null)
                                vdsNew.setRequired(true);
                        }

                    }

                }

                v++;
            }

        }else {
            
          //Remove option Enable VMWare vSAN if server is not associated with the cluster          
            ServiceTemplateSetting enableVmwareVsan = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID);
            if (enableVmwareVsan != null) {
                enableVmwareVsan.setHideFromTemplate(true);
            }
            
            ServiceTemplateSetting vdsName = cluster.getParameter(
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                    ServiceTemplateClientUtil.createVDSID("pxe"));

            if (vdsName == null) {
                vdsName = ServiceTemplateClientUtil.createVDSNameSetting(cluster, vdsCategory,
                        ServiceTemplateClientUtil.createVDSID("pxe"),
                        "VDS Name", "PXE VDS", ServiceTemplateClientUtil.copyOptions(vdsNameZero.getOptions(), null));

                vdsName.setHideFromTemplate(false);
            }else{
                vdsName.setOptions(ServiceTemplateClientUtil.copyOptions(vdsNameZero.getOptions(), null));
            }

            vdsAdded.add(vdsName);
            vdsNew = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vdsName.getId());
            if (vdsNew != null) {
                vdsAdded.add(vdsNew);
            }

            // PXE Port Group
            ServiceTemplateSetting vdsPG = ServiceTemplateClientUtil.getPortGroup(cluster, "pxe", "PXE Port Group", "pxe", 1, true);
            if (vdsPG == null) {
                // unexpected...
                LOGGER.error("getPortGroup returned null for VDS ID=pxe" + ", PG=PXE Port Group");
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.internalError());
            }
            vdsPG.setDisplayName("PXE Port Group");
            vdsPG.setHideFromTemplate(false);

            vdsAdded.add(vdsPG);
            vdsNew = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vdsPG.getId());
            if (vdsNew != null) {
                vdsAdded.add(vdsNew);
            }

            vdsName = cluster.getParameter(
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                    ServiceTemplateClientUtil.createVDSID("workload"));

            if (vdsName == null) {
                vdsName = ServiceTemplateClientUtil.createVDSNameSetting(cluster, vdsCategory,
                        ServiceTemplateClientUtil.createVDSID("workload"),
                        "VDS Name", "Workload VDS", ServiceTemplateClientUtil.copyOptions(vdsNameZero.getOptions(), null));

                vdsName.setHideFromTemplate(false);
                vdsName.setRequired(true);
            } else {
                vdsName.setOptions(ServiceTemplateClientUtil.copyOptions(vdsNameZero.getOptions(), null));
            }

            vdsAdded.add(vdsName);
            vdsNew = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vdsName.getId());
            if (vdsNew != null) {
                vdsNew.setRequired(true);
                vdsAdded.add(vdsNew);
            }

        }

        // remove old VDS names / PGs
        List <ServiceTemplateSetting> toRemove = new ArrayList<>();
        for (ServiceTemplateSetting vdsName : vdsCategory.getParameters()) {
            if (!vdsName.getId().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID + "::") &&
                    !vdsName.getId().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID + "::"))
                continue;

            toRemove.add(vdsName);
        }
        vdsCategory.getParameters().removeAll(toRemove);

        // re-add new parameters
        vdsCategory.getParameters().addAll(vdsAdded);

    }

    /**
     * Walk through all interfaces, all partitions and create a list of unique network combinations per partition.
     * @param component
     * @param ignoreErrors For sample template do not report network proxy erros
     * @return
     */
    private List<PartitionNetworks> getUniqueNetworkList(ServiceTemplateComponent component, boolean ignoreErrors) {
        List<PartitionNetworks> ret = new ArrayList<>();

        ServiceTemplateSetting networking = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        if (networking == null) return null;
        com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = ServiceTemplateUtil.deserializeNetwork(networking.getValue());
        if (networkConfig == null) {
            // xml screwed up?
            LOGGER.error("Cannot parse network configuration from string: " + networking.getValue());
            return ret;
        }

        networkConfig.clearUnusedNetworkSettings();

        List<Interface> interfaces = networkConfig.getUsedInterfaces();

        for (Interface interfaceObject : interfaces){
            List<Partition> partitions = interfaceObject.getPartitions();
            for (Partition partition : partitions) {
                List<String> networkIds = partition.getNetworks();
                PartitionNetworks pNets = new PartitionNetworks();
                if (CollectionUtils.isNotEmpty(networkIds)) {
                    for (String networkId : networkIds) {
                        Network networkObject = null;
                        try {
                           networkObject = getNetworkProxy().getNetwork(networkId);
                        } catch (Exception e) {
                            if (!ignoreErrors) {
                                LOGGER.error("Failed to retrieve network: " + networkId, e);
                            }
                        }
                        if(networkObject == null){ // Means it's been deleted!
                            continue;
                        }

                        if (NetworkType.STORAGE_FCOE_SAN.equals(networkObject.getType()) ||
                                NetworkType.FIP_SNOOPING.equals(networkObject.getType())) {
                            continue;
                        }
                        if (NetworkType.STORAGE_ISCSI_SAN.equals(networkObject.getType())) {
                            pNets.addIscsiNetwork(networkObject);
                            networkObject.setId("iscsi"); // this will be replaced later with a list of all ISCSI IDs
                        }

                        pNets.addNetwork(networkObject);
                    }
                    pNets.sortById();

                    // we need to keep unique set of networks but also remember the counter for each
                    // ignore empty structures (FCoE / FIPS only)
                    if (pNets.getNetworks().size() > 0) {
                        int idx = ret.indexOf(pNets);
                        if (idx < 0) {
                            ret.add(pNets);
                        } else {
                            ret.get(idx).increment();
                            // append iscsi network IDs
                            if (pNets.getIscsiNetworks().size() > 0) {
                                for (NetworkObject networkObject : pNets.getIscsiNetworks()) {
                                    if (!ret.get(idx).getIscsiNetworks().contains(networkObject))
                                        ret.get(idx).addIscsiNetwork(networkObject);
                                }
                            }
                        }
                    }
                }
                // for not partitioned just use the first one
                if (!interfaceObject.isPartitioned()) break;

                // Different nic types have different number of partitions but data may include more that should be ignored
                if (partition.getName().equals(Integer.toString(interfaceObject.getMaxPartitions()))) {
                    break;
                }
            }
        }

        return ret;
    }

    private class PartitionNetworks implements Comparable<PartitionNetworks>{
        /**
         * returns unsorted list!
         * @return
         */
        public final List<Network> getNetworks() {
            return networks;
        }

        private List<String> getNetworkIds() {
            List<String> ret = new ArrayList<>();
            for (Network nConfig : networks ) {
                ret.add(nConfig.getId());
            }
            return ret;
        }

        private List<Network> networks = new ArrayList<>();

        void addIscsiNetwork(Network network) {
            NetworkObject newNetwork = new NetworkObject();
            newNetwork.setId(network.getId());
            newNetwork.setName(network.getName());
            if (!iscsiNetworks.contains(newNetwork))
                iscsiNetworks.add(newNetwork);
        }

        void addIscsiNetwork(NetworkObject network) {
            if (!iscsiNetworks.contains(network))
                iscsiNetworks.add(network);
        }

        final List<NetworkObject> getIscsiNetworks() {
            return iscsiNetworks;
        }

        private List<NetworkObject> iscsiNetworks = new ArrayList<>();

        private List<NetworkObject> vsanNetworks = new ArrayList<>();

        public int getCount() {
            return count;
        }

        void increment() {
            this.count++;
        }

        public String getId() {
            return StringUtils.join(getNetworkIds(),":");
        }

        private int count = 1;

        void addNetwork(Network network) {
            if (!networks.contains(network))
                networks.add(network);
        }

        void sortById() {
            Collections.sort(networks, new NetworkIDsComparator());
        }

        List<String> sortISCSINetworks() {
            Collections.sort(iscsiNetworks, new NetworkObjectIDsComparator());
            List<String> ret = new ArrayList<>();
            for (NetworkObject nc : iscsiNetworks) {
                ret.add(nc.getId());
            }
            return ret;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PartitionNetworks) {
                PartitionNetworks pn = (PartitionNetworks) o;
                return pn.getId().equals(this.getId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.getId().hashCode();
        }

        public int compareTo(PartitionNetworks y) {
            String netIdsX = this.getId();
            String netIdsY = y.getId();
            return netIdsX.compareTo(netIdsY);
        }

        public boolean hasManagementNetwork() {
            for (Network nConfig : networks ) {
                if (NetworkType.HYPERVISOR_MANAGEMENT.equals(nConfig.getType())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class NetworkIDsComparator implements java.util.Comparator<Network> {
        @Override
        public int compare(Network x, Network y) {
            return x.getId().compareTo(y.getId());
        }
    }

    private class NetworkObjectIDsComparator implements java.util.Comparator<NetworkObject> {
        @Override
        public int compare(NetworkObject x, NetworkObject y) {
            return x.getId().compareTo(y.getId());
        }
    }

    private class NetworkObject {
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        String id;
        String name;

        @Override
        public boolean equals(Object o) {
            if (o instanceof NetworkObject) {
                NetworkObject pn = (NetworkObject) o;
                return pn.getId().equals(this.getId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.getId().hashCode();
        }

    }
}
