/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.brownfield;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.BrownfieldStatus;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentDevice;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deployment.EsxiServiceDefinition;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedServer;
import com.dell.asm.asmcore.asmmanager.client.deployment.ServiceDefinition;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingDef;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEquallogicDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetNetappDevice;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ClusterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatacenterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatastoreDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.HostDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ManagedObjectDTO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.client.util.VcenterInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

/**
 * The BrownfieldUtil is a Singleton class that provides all of the necessary functionality for discovering and 
 * updating Brownfield Services that's exposed as part of the Rest interface in the DeploymentService. 
 */
public class BrownfieldUtil {
    
    // Class Variables
    public static final String NOT_FOUND = "Not Found";
    
    private static final Logger logger = Logger.getLogger(DeploymentService.class);
    private static final BrownfieldUtil brownfieldUtil = new BrownfieldUtil();
    private static final String UNAVAILABLE_STORAGE = "Unavailable Storage ";
    private static final String UNAVAILABLE_SERVER = "Unavailable Server ";
    private static final String SERVER = "Server ";
    private static final String STORAGE = "Storage ";
    
    // Member Variables
    private DeviceInventoryDAO deviceInventoryDAO;
    
    // Default constructor for the class
    private BrownfieldUtil() {}
    
    /**
     * Returns the instance of the BrownfieldUtil
     * 
     * @return the instance of the BrownfieldUtil.
     */
    public static BrownfieldUtil getInstance() {
        return BrownfieldUtil.brownfieldUtil;
    }
    
    /**
     * Sets the deviceInvetoryDAO used by the class.
     * 
     * @param newDeviceInventoryDAO the new deviceInventoryDAO used by the class.
     */
    public void setDeviceInventoryDAO(DeviceInventoryDAO newDeviceInventoryDAO) {
        this.deviceInventoryDAO = newDeviceInventoryDAO;
    }
    
    
    /**
     * Returns a Deployment that is populated based on the data requested in the ServiceDefinition from a brownfield
     * environment.
     * 
     * @param serviceDefinition the definition of the service that will be queried and created (if possible)
     */
    public Deployment defineService(ServiceDefinition serviceDefinition) {

        Deployment deployment = null;

        if (EsxiServiceDefinition.class.equals(serviceDefinition.getClass())) {
            EsxiServiceDefinition esxiServiceDefinition = (EsxiServiceDefinition)serviceDefinition;
            deployment = this.defineEsxiService(esxiServiceDefinition.getClusterComponentName(), 
                    esxiServiceDefinition.getVcenterRefId(), esxiServiceDefinition.getDatacenterName(), 
                    esxiServiceDefinition.getClusterName()); 
        }
        
        deployment.renumberAndRenameServiceTemplateComponents(true);
        
        return deployment;
    }
    
    
    // processes and returns a deployment for an esxi service
    private Deployment defineEsxiService(String clusterComponentName, 
                                         String vcenterRefId, 
                                         String datacenterName, 
                                         String clusterName) throws WebApplicationException {
        
        // Validate Parameters
        if (clusterComponentName == null ||  clusterComponentName.trim().length() < 1) throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.invalidParam("clusterComponentName"));
        if (vcenterRefId == null ||  vcenterRefId.trim().length() < 1) throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.invalidParam("vcenterRefId"));
        if (datacenterName == null ||  datacenterName.trim().length() < 1) throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.invalidParam("datacenterName"));
        if (clusterName == null ||  clusterName.trim().length() < 1) throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.invalidParam("clusterName"));;
        
        // Business Logic
        Deployment deployment = getBrownfieldStartingDeployment();
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        
        // Find the VCenter
        ManagedObjectDTO vcenter = null;
        DeviceInventoryEntity vcenterDevice = null;
        try {
            Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(vcenterRefId);
            vcenter = VcenterInventoryUtils.convertPuppetDeviceDetailsToDto(deviceDetails);
            vcenterDevice = this.deviceInventoryDAO.getDeviceInventory(vcenterRefId);
         } catch (Exception vcenterDetailsException) {
            logger.error("Could not find vcenter details for " + vcenterRefId, vcenterDetailsException);
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noVcenterFoundForRefId(vcenterRefId)); 
        }
        
        // See if the VCenter and Cluster Exists (throw an exception if either does not exist)
        if (vcenter == null || vcenterDevice == null) {
            logger.error("Could not find a vcenter with id of " + vcenterRefId);
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noVcenterFoundForRefId(vcenterRefId)); 
        }
        DatacenterDTO datacenterDTO = vcenter.getDatacenter(datacenterName);
        ClusterDTO clusterDto = datacenterDTO.getCluster(clusterName);
        if (clusterDto == null) {
            logger.error("Could not find a cluster with id of " + clusterName);
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noVcenterFoundForRefId(vcenterRefId)); 
        }     
        
        // Load the EqualLogic from the DeviceInventory in ASM (Java side)       
        List<DeviceInventoryEntity> equallogicDevices = this.deviceInventoryDAO.getAllDeviceInventoryByDeviceType(DeviceType.equallogic);
        List<PuppetEquallogicDevice> equallogicPuppetDevices = new ArrayList<PuppetEquallogicDevice>();
        
        // Load the PuppetDevice from Puppet with EqualLogic RefIds && Try to match ManagementIp Address from Cluster's EqualLogic attributes
        if (equallogicDevices != null) {
            for (DeviceInventoryEntity equalLogicDeviceEntity : equallogicDevices) {
                try {
                    PuppetEquallogicDevice equalLogicPuppetDevice = PuppetModuleUtil.getPuppetEquallogicDevice(equalLogicDeviceEntity.getRefId());
                    equallogicPuppetDevices.add(equalLogicPuppetDevice);
                } catch(Exception e) {
                    logger.warn("Could not find puppet device for equallogic device with refId of " + equalLogicDeviceEntity.getRefId(), e);
                }
            }
        }
        
        // Load the Compellent from the DeviceInventory in ASM (Java side)       
        List<DeviceInventoryEntity> compellentDevices = this.deviceInventoryDAO.getAllDeviceInventoryByDeviceType(DeviceType.compellent);
        List<PuppetCompellentDevice> compellentPuppetDevices = new ArrayList<PuppetCompellentDevice>();
        
        // Load the PuppetDevice from Puppet with Compellent RefIds && Try to match ManagementIp Address from Cluster's EqualLogic attributes
        if (compellentDevices != null) {
            for (DeviceInventoryEntity compellentDeviceEntity : compellentDevices) {
                try {
                    PuppetCompellentDevice compellentPuppetDevice = PuppetModuleUtil.getPuppetCompellentDevice(compellentDeviceEntity.getRefId());
                    compellentPuppetDevices.add(compellentPuppetDevice);
                } catch(Exception e) {
                    logger.warn("Could not find puppet device for compellent device with refId of " + compellentDeviceEntity.getRefId(), e);
                }
            }
        }
        
        // Load the NetApps from the DeviceInventory in ASM (Java side)       
        List<DeviceInventoryEntity> netappDevices = this.deviceInventoryDAO.getAllDeviceInventoryByDeviceType(DeviceType.netapp);
        List<PuppetNetappDevice> netappPuppetDevices = new ArrayList<PuppetNetappDevice>();
        
        // Load the PuppetDevice from Puppet with NetApps RefIds && Try to match 
        if (netappDevices != null) {
            for (DeviceInventoryEntity netAppDeviceEntity : netappDevices) {
                try {
                    PuppetNetappDevice puppetNetappDevice = PuppetModuleUtil.getPuppetNetappDevice(netAppDeviceEntity.getRefId());
                    netappPuppetDevices.add(puppetNetappDevice);
                } catch(Exception e) {
                    logger.warn("Could not find puppet device for netapp device with refId of " + netAppDeviceEntity.getRefId(), e);
                }
            }
        }
        
        // Determine vds setting
        boolean vdsEnabled = clusterDto.isVdsEnabled();
        
        // Process the Cluster itself
        ServiceTemplateComponent clusterComponent = this.getEsxiClusterComponent(vcenterDevice, datacenterName, clusterName, clusterComponentName, vdsEnabled); 
        serviceTemplate.addComponent(clusterComponent);
        deployment.getDeploymentDevice().add(this.getBrownfieldDeploymentDeviceFromDeviceInventory(vcenterDevice, clusterComponent.getId(), ServiceTemplateComponentType.CLUSTER));
        
        // If Cluster has Servers process Servers
        List<HostDTO> hostDtos = clusterDto.getHosts();
        int serverCount = 1;
        int storageCount = 1;
        int unavailableServerCount = 1;
        int unavailableStorageCount = 1;
        for (HostDTO hostDto : hostDtos) {
            @SuppressWarnings("unchecked")
            List<String> serviceTagsList  = (ArrayList<String>)hostDto.getAttribute("service_tags");
            ServiceTemplateComponent serverComponent = null;  
            BrownfieldStatus serverBrownfieldStatus  = BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY;

            // The HostName returned by vcenter may be the osIpAddress or the hostName.  
            // We don't know which, so must test it and set it accordingly.
            String hostName = NOT_FOUND;
            String ipAddress = null; 
            if (ASMCommonsUtils.isValidIp(hostDto.getName())) {
                ipAddress = hostDto.getName();
            }
            else hostName = hostDto.getName();
            
            // There 'may' be multiple service tags due to VCenter API returning the service tag of the Server and of the Chassis the server belongs to
            // Look up Servers by the Service Tag, if they exist they are part of the Service
            List<DeviceInventoryEntity> deviceInvEntities = this.deviceInventoryDAO.getDevicesByServiceTags(serviceTagsList);
            DeviceInventoryEntity foundServerEntity = null;
            for (DeviceInventoryEntity deviceInvEntity : deviceInvEntities) {
                if (deviceInvEntity.getDeviceType() != null && deviceInvEntity.getDeviceType().isServer()) {
                    foundServerEntity = deviceInvEntity;
                    break; // no need to look further, should only be one server with an asset tag
                }
            }     
            // If a ServerEntity was found we need to add it to the ServiceTemplate
            if (foundServerEntity != null) {
                // Get Hostname
                ManagedDevice foundServerManagedDevice = DeviceInventoryUtils.toDTO(foundServerEntity, false);
                if (foundServerManagedDevice.getHostname() != null && !foundServerManagedDevice.getHostname().trim().isEmpty()) {
                    hostName = foundServerManagedDevice.getHostname();
                }
 
                serverComponent = this.getServerServiceTemplateComponent(foundServerEntity, SERVER + serverCount++, hostName, ipAddress); 
                serviceTemplate.addComponent(serverComponent);
                DeploymentDevice foundServerDeploymentDevice = this.getBrownfieldDeploymentDeviceFromDeviceInventory(foundServerEntity, serverComponent.getId(), ServiceTemplateComponentType.SERVER);
                serverBrownfieldStatus = foundServerDeploymentDevice.getBrownfieldStatus();
                // If it's not available rename it
                if (!BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                    serverCount--;
                    serverComponent.setName(UNAVAILABLE_SERVER + unavailableServerCount++);
                }
                
                deployment.getDeploymentDevice().add(foundServerDeploymentDevice);
                
                // Add related components
                clusterComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                serverComponent.addAssociatedComponentName(clusterComponent.getId(), clusterComponent.getName());
            }
            else { // If a ServerEntity was not found we need to add a fake DeviceEntity to the ServiceTemplate
                DeviceInventoryEntity notFoundDevice = this.getDeviceInventoryEntityForDeviceNotFound(ServiceTemplateComponentType.SERVER, DeviceType.Server, "", getServiceTag(serviceTagsList));
                ServiceTemplateComponent notFoundServerComponent = this.getServerServiceTemplateComponent(notFoundDevice, UNAVAILABLE_SERVER + unavailableServerCount++, hostName, ipAddress);
                serviceTemplate.addComponent(notFoundServerComponent);
                DeploymentDevice notFoundDeploymentDevice = this.getBrownfieldDeploymentDeviceForDeviceNotInInventory(notFoundDevice, notFoundServerComponent.getId(), ServiceTemplateComponentType.SERVER);
                deployment.getDeploymentDevice().add(notFoundDeploymentDevice);
            }
            
            // If Cluster has Datastores, then process them and add them to Deployment accordingly
            List<DatastoreDTO> datastoreDtos = hostDto.getDatastores();
            for (DatastoreDTO datastoreDTO : datastoreDtos) {
                String vendor = (String)datastoreDTO.getAttribute("vendor");
                Double capacityDouble = ((Double)datastoreDTO.getAttribute("capacity"));
                String capacity = this.formatWithNoDecimals(capacityDouble);
                if (vendor == null || vendor.trim().isEmpty()) {
                   vendor = "NETAPP"; 
                }
                else {
                    vendor = vendor.trim();
                }
                
                if ("EQLOGIC".equals(vendor)) {
                    // iscsiGroupIp is the IP VCenter uses for the storage, it is not the management ip and thus not applicable to ASM
                    // String iscsiGroupIp = (String)datastoreDTO.getAttribute("iscsi_group_ip"); 
                    String iscsiIqn = (String)datastoreDTO.getAttribute("iscsi_iqn"); 
                    String volumeName = (String)datastoreDTO.getAttribute("volume_name");
                    
                    // Determine if we it in Puppet Inventory
                    PuppetEquallogicDevice equalLogicPuppetDevice = this.findEqualLogicDeviceWithIqn(equallogicPuppetDevices, iscsiIqn);  
                    
                    if (equalLogicPuppetDevice != null) {
                        // If ServerComponent was not found then still dealing with an unavailable Storage component
                        if (serverComponent == null || !BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                            unavailableStorageCount = 
                                    this.processFoundEquallogicBrownfield(deployment, 
                                                                          datastoreDTO, 
                                                                          equalLogicPuppetDevice, 
                                                                          iscsiIqn, 
                                                                          serverComponent, 
                                                                          equallogicDevices, 
                                                                          unavailableStorageCount, 
                                                                          serverBrownfieldStatus, 
                                                                          capacity);
                        }
                        else {
                            storageCount = this.processFoundEquallogicBrownfield(deployment, 
                                                                                 datastoreDTO, 
                                                                                 equalLogicPuppetDevice, 
                                                                                 iscsiIqn, 
                                                                                 serverComponent, 
                                                                                 equallogicDevices, 
                                                                                 storageCount, 
                                                                                 serverBrownfieldStatus, 
                                                                                 capacity);
                        }
                    }
                    else {   
                        // Since the iscsiGroupIp is not a valid management ip, we are going to set the ip to an empty string for now
                        unavailableStorageCount = 
                                this.processMissingEquallogicBrownfield(deployment, 
                                                                        "", 
                                                                        volumeName, 
                                                                        unavailableStorageCount, 
                                                                        serverBrownfieldStatus, 
                                                                        capacity);
                    }
                }
                else if ("COMPELNT".equals(vendor)) {
                    // iscsiGroupIp is the IP VCenter uses for the storage, it is not the management ip and thus not applicable to ASM
                    // String iscsiGroupIp = (String)datastoreDTO.getAttribute("iscsi_group_ip");
                    // The fcoeWwpn is a port identifier that VCenter assigns to Compellent it cannot be found in Compellent facts
                    // String fcoeWwpn = (String)datastoreDTO.getAttribute("fcoe_wwpn");
                    String volumeName = (String)datastoreDTO.getAttribute("volume_name");
                    String iscsiIqn = (String)datastoreDTO.getAttribute("iscsi_iqn");
                    String scsiDeviceId = (String)datastoreDTO.getAttribute("scsi_device_id");
                    
                    PuppetCompellentDevice puppetCompellentDevice = null;
                    boolean isFiberChannel = false;
                    
                    if(iscsiIqn == null) {
                        isFiberChannel = true;
                    }
                        
                    if (scsiDeviceId != null && scsiDeviceId.length() > 4) {
                        int index = scsiDeviceId.lastIndexOf("naa.");
                        scsiDeviceId = scsiDeviceId.substring(index+4);
                        puppetCompellentDevice = findCompellentDeviceWithIscsiDeviceId(compellentPuppetDevices, scsiDeviceId);
                    }
                        
                    if (puppetCompellentDevice != null) {
                        // If ServerComponent was not found then still dealing with an unavailable Storage component
                        if (serverComponent == null || !BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                            unavailableStorageCount = 
                                    this.processFoundCompellentBrownfield(deployment, 
                                                                          datastoreDTO, 
                                                                          puppetCompellentDevice, 
                                                                          scsiDeviceId, 
                                                                          isFiberChannel, 
                                                                          serverComponent, 
                                                                          compellentDevices, 
                                                                          unavailableStorageCount, 
                                                                          serverBrownfieldStatus, 
                                                                          capacity);
                        }
                        else {
                            storageCount = this.processFoundCompellentBrownfield(deployment, 
                                                                                 datastoreDTO, 
                                                                                 puppetCompellentDevice, 
                                                                                 scsiDeviceId, 
                                                                                 isFiberChannel, 
                                                                                 serverComponent, 
                                                                                 compellentDevices, 
                                                                                 storageCount, 
                                                                                 serverBrownfieldStatus, 
                                                                                 capacity);
                        }
                    }
                    else {   
                        unavailableStorageCount = 
                                this.processMissingCompellentBrownfield(deployment, 
                                                                        "", 
                                                                        volumeName, 
                                                                        unavailableStorageCount, 
                                                                        serverBrownfieldStatus, 
                                                                        capacity);
                    }
                }
                else if ("NETAPP".equals(vendor)) {
                    String nfsHost = (String)datastoreDTO.getAttribute("nfs_host");
                    String nfsPath = (String)datastoreDTO.getAttribute("nfs_path");
                    
                    if(nfsHost != null && !nfsHost.trim().isEmpty() && nfsPath != null && !nfsPath.trim().isEmpty()) {
                        nfsHost = nfsHost.trim();
                        String volume = nfsPath.substring(nfsPath.lastIndexOf("/")+1);
                        
                        // PROCESS NETAPP
                        PuppetNetappDevice puppetNetappDevice = findNetappDeviceWithNfsHostAndVolumeName(netappPuppetDevices, nfsHost, volume);
                        
                        if (puppetNetappDevice != null) {
                            // If ServerComponent was not found then still dealing with an unavailable Storage component
                            if (serverComponent == null || !BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                                unavailableStorageCount = 
                                        this.processFoundNetappBrownfield(deployment, 
                                                                          datastoreDTO, 
                                                                          puppetNetappDevice, 
                                                                          volume, 
                                                                          serverComponent, 
                                                                          netappDevices, 
                                                                          unavailableStorageCount, 
                                                                          serverBrownfieldStatus, 
                                                                          capacity);
                            }
                            else {
                                storageCount = this.processFoundNetappBrownfield(deployment, 
                                                                                 datastoreDTO, 
                                                                                 puppetNetappDevice, 
                                                                                 volume, 
                                                                                 serverComponent, 
                                                                                 netappDevices, 
                                                                                 storageCount, 
                                                                                 serverBrownfieldStatus, 
                                                                                 capacity);
                            }
                        }
                        else {   
                            // Since the iscsiGroupIp is not a valid management ip, we are going to set the ip to an empty string for now
                            unavailableStorageCount = 
                                    this.processMissingNetappBrownfield(deployment, 
                                                                        nfsHost, 
                                                                        volume, 
                                                                        unavailableStorageCount, 
                                                                        serverBrownfieldStatus, 
                                                                        capacity);
                        }
                    }
                    
                }
            }
        }
        
        // Set Deployment Values
        deployment.setServiceTemplate(serviceTemplate);
        

        return deployment;
    }
    
    // Searches the equalLogicPuppet devices for the given iscsiGroupIp and returns the refid of the puppet device 
    // if a match is found
    private PuppetEquallogicDevice findEqualLogicDeviceWithIqn(List<PuppetEquallogicDevice> equallogicPuppetDevices,
                                                               String iscsiIqn) {
        PuppetEquallogicDevice puppetEquallogicDevice = null;

        for (PuppetEquallogicDevice equalLogicPuppetDevice : equallogicPuppetDevices) {
            PuppetEquallogicDevice.VolumeProperties volumeProperties = equalLogicPuppetDevice
                    .getVolumePropertiesByIscsiIqn(iscsiIqn);
            if (volumeProperties != null) { // Will be null if it does not contain the iscsi_iqn 
                puppetEquallogicDevice = equalLogicPuppetDevice;
                break; // We found it, no need to continue looking
            }
        }

        return puppetEquallogicDevice;
    }

    // Returns the 2nd Service Tag, or the first, or an empty String if one cannot be found.
    private String getServiceTag(List<String> serviceTagsList) {
        String serviceTag = "";
        
        if (serviceTagsList != null) {
            if (serviceTagsList.size() > 1) {
                serviceTag = serviceTagsList.get(1);
            } else if (serviceTagsList.size() == 1) {
                serviceTag = serviceTagsList.get(0);
            }
        }

        return serviceTag;
    }

    // Returns the PuppetCompellentDevice with the given iscsiIqn or null if one cannot be found
    private PuppetCompellentDevice findCompellentDeviceWithIscsiDeviceId(List<PuppetCompellentDevice> puppetCompellentDevices, String iscsiDeviceId) {
        PuppetCompellentDevice puppetCompellentDevice = null;
        
        findCompellentDeviceLoop:
        for (PuppetCompellentDevice pcDevice : puppetCompellentDevices) {
            for (PuppetCompellentDevice.Volume volume : pcDevice.getVolumes()) {
                if(iscsiDeviceId.equalsIgnoreCase(volume.getDeviceId())) {
                    puppetCompellentDevice = pcDevice;
                    break findCompellentDeviceLoop;
                }
            }
        }
        
        return puppetCompellentDevice;
    }    
    
    // Returns the PuppetNetappDevice with the given iscsiIqn or null if one cannot be found
    private PuppetNetappDevice findNetappDeviceWithNfsHostAndVolumeName(List<PuppetNetappDevice> puppetNetappDevices, 
                                                                        String nfsHost, 
                                                                        String volumeName) {
        PuppetNetappDevice puppetNetappDevice = null;
        
        findNetappDeviceLoop:
        for (PuppetNetappDevice netappDevice : puppetNetappDevices) {
            if(nfsHost.equals(netappDevice.getIpaddress())) {
                for(PuppetNetappDevice.VolumeData volumeData : netappDevice.getVolumeDatas()) {
                    if(volumeName.equals(volumeData.getName())) {
                        puppetNetappDevice = netappDevice;
                        break findNetappDeviceLoop;
                    }
                }
            }
        }
        
        return puppetNetappDevice;
    }
    
    // Creates a ServiceTemplateComponent to be added to a ServiceTemplate from an existing device in inventory for 
    // Brownfield
    private ServiceTemplateComponent getServiceTemplateComponent(DeviceInventoryEntity deviceInventoryEntity,
            String componentName, ServiceTemplateComponent.ServiceTemplateComponentType serviceTemplateComponentType) {
        ServiceTemplateComponent serviceTemplateComponent = new ServiceTemplateComponent();
        serviceTemplateComponent.setAsmGUID(deviceInventoryEntity.getRefId());
        serviceTemplateComponent.setId(UUID.randomUUID().toString());
        serviceTemplateComponent.setName(componentName);
        serviceTemplateComponent.setType(serviceTemplateComponentType);
        // WARNING - DO NOT SET this refId as it is the Id & IpAddress of the Reference Server when importing 
        //           settings in a template from a Reference Server.  
        //        serviceTemplateComponent.setRefId(deviceInventoryEntity.getRefId());
        //        serviceTemplateComponent.setIP(deviceInventoryEntity.getIpAddress());
        serviceTemplateComponent.setConfigFile(deviceInventoryEntity.getConfig());
        serviceTemplateComponent.setCloned(false);
        serviceTemplateComponent.setClonedFromId(null);
        serviceTemplateComponent.setBrownfield(true);
        serviceTemplateComponent.setPuppetCertName(PuppetModuleUtil.toCertificateName(deviceInventoryEntity));

        return serviceTemplateComponent;
    }

    // Returns a ServiceTemplateComponent for a Server 
    private ServiceTemplateComponent getServerServiceTemplateComponent(DeviceInventoryEntity deviceInventoryEntity,
            String componentName, String hostName, String osIpAddress) {
        ServiceTemplateComponent serviceTemplateComponent = this.getServiceTemplateComponent(deviceInventoryEntity,
                componentName, ServiceTemplateComponentType.SERVER);
        serviceTemplateComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MINIMIAL_COMP_ID);
        
        // SERVER CATEGORY
        ServiceTemplateCategory osHostNameCategory = new ServiceTemplateCategory(
                ServiceTemplateSettingDef.SERVER_OS_RESOURCE);
        
        ServiceTemplateSetting hostnameSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.SERVER_OS_HOSTNAME_ID, hostName, ServiceTemplateSettingType.STRING);
        
        ServiceTemplateSetting osImageType = new ServiceTemplateSetting(ServiceTemplateSettingDef.VM_OS_TYPE_ID, 
                                                                        "vmware_esxi", 
                                                                        ServiceTemplateSettingType.STRING);
        osHostNameCategory.getParameters().add(hostnameSetting);
        osHostNameCategory.getParameters().add(osImageType);
        
        ServiceTemplateSetting ensureSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
        ensureSetting.setRequired(false);
        ensureSetting.setRequiredAtDeployment(false);
        ensureSetting.setHideFromTemplate(true);
        osHostNameCategory.getParameters().add(ensureSetting);
        
        serviceTemplateComponent.getResources().add(osHostNameCategory);

        // Only set the hostId via a fake Network if the Deployment is being displayed in the UI during discovery.  
        // If a fake / temp Network is set on the Deployment used to create  a New Deployment then it will result 
        // in a null pointer and other issues.
        if (osIpAddress != null) {
            // SERVER NETWORK CATEGORY
            ServiceTemplateCategory networkTemplateCategory = new ServiceTemplateCategory(
                    ServiceTemplateSettingDef.SERVER_NETWORKING_COMP_ID);

            ServiceTemplateSetting networkTemplateSetting = new ServiceTemplateSetting(
                    ServiceTemplateSettingDef.SERVER_HYPERVISOR_NETWORK_ID, osIpAddress,
                    ServiceTemplateSettingType.ENUMERATED);
            Network network = new Network();
            StaticNetworkConfiguration staticConfiguration = new StaticNetworkConfiguration();
            staticConfiguration.setIpAddress(osIpAddress);
            network.setStaticNetworkConfiguration(staticConfiguration);
            network.setStatic(true);
            ArrayList<Network> networks = new ArrayList<Network>();
            networks.add(network);
            networkTemplateSetting.setHideFromTemplate(true);
            networkTemplateSetting.setNetworks(networks);
            networkTemplateCategory.getParameters().add(networkTemplateSetting);
            
            ensureSetting = new ServiceTemplateSetting(
                    ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
            ensureSetting.setRequired(false);
            ensureSetting.setRequiredAtDeployment(false);
            ensureSetting.setHideFromTemplate(true);
            networkTemplateCategory.getParameters().add(ensureSetting);
            
            serviceTemplateComponent.getResources().add(networkTemplateCategory);
        }

        // SERVER IDRAC CATEGORY
        ServiceTemplateCategory serverIdracCategory = new ServiceTemplateCategory(
                ServiceTemplateSettingDef.SERVER_IDRAC_RESOURCE);

        ServiceTemplateSetting serverSourceSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.SERVER_SOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL, ServiceTemplateSettingType.ENUMERATED);
        serverSourceSetting.setHideFromTemplate(true);
        serverIdracCategory.getParameters().add(serverSourceSetting);

        ServiceTemplateSetting serverManualSelectionSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.SERVER_MANUAL_SELECTION, deviceInventoryEntity.getRefId(),
                ServiceTemplateSettingType.STRING);
        serverManualSelectionSetting.setHideFromTemplate(true);
        serverIdracCategory.getParameters().add(serverManualSelectionSetting);
        
        ensureSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
        ensureSetting.setRequired(false);
        ensureSetting.setRequiredAtDeployment(false);
        ensureSetting.setHideFromTemplate(true);
        serverIdracCategory.getParameters().add(ensureSetting);

        serviceTemplateComponent.getResources().add(serverIdracCategory);
        
        // SERVER BIOS CATEGORY
        ServiceTemplateCategory serverBiosCategory = new ServiceTemplateCategory(
                ServiceTemplateSettingDef.SERVER_BIOS_RESOURCE);
        ensureSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
        ensureSetting.setRequired(false);
        ensureSetting.setRequiredAtDeployment(false);
        ensureSetting.setHideFromTemplate(true);
        serverBiosCategory.getParameters().add(ensureSetting);

        serviceTemplateComponent.getResources().add(serverBiosCategory);

        return serviceTemplateComponent;
    }

    // Searches through the DeviceInventoryEntityList for a DeviceInventoryEntity with the given refId
    private DeviceInventoryEntity getDeviceInventoryEntityWithRefId(
            List<DeviceInventoryEntity> deviceInventoryEntities, String refId) {
        DeviceInventoryEntity deviceInventoryEntityReturn = null;

        for (DeviceInventoryEntity deviceInventoryEntity : deviceInventoryEntities) {
            if (deviceInventoryEntity.getRefId() != null && deviceInventoryEntity.getRefId().equals(refId)) {
                deviceInventoryEntityReturn = deviceInventoryEntity;
                break;
            }
        }

        return deviceInventoryEntityReturn;
    }

    // Returns a dummy DeviceInventoryEntity for brownfield devices that are not found in inventory
    private DeviceInventoryEntity getDeviceInventoryEntityForDeviceNotFound(ServiceTemplateComponentType type,
            DeviceType deviceType, String ipAddress, String serviceTag) {
        DeviceInventoryEntity deviceInventoryEntity = new DeviceInventoryEntity();
        deviceInventoryEntity.setRefId(UUID.randomUUID().toString());
        deviceInventoryEntity.setCompliant(CompliantState.UNKNOWN.getValue());
        deviceInventoryEntity.setDeviceType(deviceType);
        deviceInventoryEntity.setHealth(DeviceHealth.UNKNOWN);
        deviceInventoryEntity.setHealthMessage("Uknown");
        deviceInventoryEntity.setIpAddress(ipAddress);
        deviceInventoryEntity.setRefType(type.name());
        deviceInventoryEntity.setServiceTag(serviceTag);
        // By setting it to unmanaged we are ensuring it will not be available for deployment as only devices with a 
        // state of DISCOVERED (managed) and RESERVED are available for deployment in Brownfield
        deviceInventoryEntity.setManagedState(ManagedState.UNMANAGED);
        return deviceInventoryEntity;
    }

    // Returns a DeploymentDevice for a device that's not in inventory
    private DeploymentDevice getBrownfieldDeploymentDeviceForDeviceNotInInventory(DeviceInventoryEntity deviceInventoryEntity, String componentId, ServiceTemplateComponentType type) {
        DeploymentDevice deploymentDevice = this.getBrownfieldDeploymentDeviceFromDeviceInventory(deviceInventoryEntity, componentId, type);
        deploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY);
        return deploymentDevice;
    }
    
    // Returns a DeeploymentDevice based on the deviceInventory for brownfield 
    private DeploymentDevice getBrownfieldDeploymentDeviceFromDeviceInventory(
            DeviceInventoryEntity deviceInventoryEntity, String componentId, ServiceTemplateComponentType type) {
        DeploymentDevice deploymentDevice = new DeploymentDevice();
        deploymentDevice.setBrownfield(true);
        deploymentDevice.setComponentId(componentId);
        deploymentDevice.setCompliantState(CompliantState.fromValue(deviceInventoryEntity.getCompliant()));
        deploymentDevice.setDeviceType(deviceInventoryEntity.getDeviceType());
        deploymentDevice.setDeviceHealth(deviceInventoryEntity.getHealth());
        deploymentDevice.setHealthMessage(deviceInventoryEntity.getHealthMessage());
        deploymentDevice.setIpAddress(deviceInventoryEntity.getIpAddress());
        deploymentDevice.setRefId(deviceInventoryEntity.getRefId());
        deploymentDevice.setRefType(type.name());
        deploymentDevice.setServiceTag(deviceInventoryEntity.getServiceTag());
        deploymentDevice.setStatus(DeploymentStatusType.COMPLETE);

        if ((deviceInventoryEntity.getManagedState() == ManagedState.MANAGED ||
                deviceInventoryEntity.getManagedState() == ManagedState.RESERVED) &&
                (deviceInventoryEntity.getDeploymentCount() == 0 ||
                deviceInventoryEntity.getDeviceType().isSharedDevice())) {
            deploymentDevice.setBrownfieldStatus(BrownfieldStatus.AVAILABLE);
        } 
        else if ((deviceInventoryEntity.getManagedState() == ManagedState.MANAGED ||
                deviceInventoryEntity.getManagedState() == ManagedState.RESERVED) &&
                deviceInventoryEntity.getDeploymentCount() > 0 &&
                !deviceInventoryEntity.getDeviceType().isSharedDevice()) {
            deploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_IN_EXISTING_SERVICE);
        }
        else {
            deploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_NOT_MANAGED_OR_RESERVED);
        }

        return deploymentDevice;
    }

    // Returns the Esxi ClusterComponent for use on a ServiceTemplate in brownfield 
    private ServiceTemplateComponent getEsxiClusterComponent(DeviceInventoryEntity vcenterDeviceEntity,
            String datacenterName, String clusterName, String clusterComponentName, boolean vdsEnabled) {
        ServiceTemplateComponent clusterComponent = this.getServiceTemplateComponent(vcenterDeviceEntity,
                clusterComponentName, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER);
        clusterComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID);
        
        ServiceTemplateCategory clusterSettingsCategory = new ServiceTemplateCategory(ServiceTemplateSettingDef.ESX_CLUSTER_COMP_ID);

        ServiceTemplateSetting asmGuidSetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.ASM_GUID,
                clusterComponent.getAsmGUID(), ServiceTemplateSettingType.ENUMERATED);
        clusterSettingsCategory.getParameters().add(asmGuidSetting);

        ServiceTemplateSetting datacenterSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.CLUSTER_DATACENTER_ID, datacenterName, ServiceTemplateSettingType.ENUMERATED);
        clusterSettingsCategory.getParameters().add(datacenterSetting);

        ServiceTemplateSetting clusterSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.CLUSTER_CLUSTER_ID, clusterName, ServiceTemplateSettingType.ENUMERATED);
        clusterSettingsCategory.getParameters().add(clusterSetting);

        ServiceTemplateSetting haConfigSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.CLUSTER_CLUSTER_HA_ID, "false", ServiceTemplateSettingType.BOOLEAN);
        haConfigSetting.setDependencyTarget("cluster");
        haConfigSetting.setDependencyValue("$new$");
        clusterSettingsCategory.getParameters().add(haConfigSetting);

        ServiceTemplateSetting drsConfigSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.CLUSTER_CLUSTER_DRS_ID, "false", ServiceTemplateSettingType.BOOLEAN);
        haConfigSetting.setDependencyTarget("cluster");
        haConfigSetting.setDependencyValue("$new$");
        clusterSettingsCategory.getParameters().add(drsConfigSetting);

        String vdsValue = "standard";
        if (vdsEnabled) {
        	vdsValue = "distributed";
        }
        
        ServiceTemplateSetting vdsConfigSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.CLUSTER_CLUSTER_VDS_ID, vdsValue, ServiceTemplateSettingType.RADIO);
        haConfigSetting.setDependencyTarget("cluster");
        haConfigSetting.getOptions().add(new ServiceTemplateOption("Standard",ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_STD_ID, null, null));
        haConfigSetting.getOptions().add(new ServiceTemplateOption("Distributed",ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID, null, null));
        haConfigSetting.setDependencyValue("$new$");
        clusterSettingsCategory.getParameters().add(vdsConfigSetting);

        ServiceTemplateSetting ensureSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
        ensureSetting.setRequired(false);
        ensureSetting.setRequiredAtDeployment(false);
        ensureSetting.setHideFromTemplate(true);
        clusterSettingsCategory.getParameters().add(ensureSetting);
        
        clusterComponent.getResources().add(clusterSettingsCategory);

        return clusterComponent;
    }
    

    /**
     * Returns a list of DeploymentDevices obtained from the browfieldDeployment that also exist in the database. Any
     * DeploymentDevice/Server not found to exist in the database will be removed from the list.
     * 
     * @param brownfieldDeployment
     *            whose DeploymentDevices will be checked to exist in the database and returned.
     * @param deploymentEntity
     *            used to verify the DeploymentDevices exist in the database.
     * @return a list of DeploymentDevices from the brownfieldDeployment that also exist in the database.
     */
    public List<DeploymentDevice> getDeploymentDevicesForBrownfieldDeployment(Deployment brownfieldDeployment,
            DeploymentEntity deploymentEntity) {
        ArrayList<DeploymentDevice> deploymentDevices = new ArrayList<DeploymentDevice>();

        for (ServiceTemplateComponent serviceTemplateComponent : brownfieldDeployment.getServiceTemplate()
                .getComponents()) {

            if (serviceTemplateComponent.getRefId() != null) {
                // To support database clean up only add DeploymentDevice if  entity in database exists for server
                if ((ServiceTemplateComponentType.SERVER.equals(serviceTemplateComponent.getType()) &&
                        deploymentEntity.containsDeviceInventoryEntity(serviceTemplateComponent.getRefId())) ||
                        !ServiceTemplateComponentType.SERVER.equals(serviceTemplateComponent.getType())) {
                    DeploymentDevice newDevice = new DeploymentDevice();
                    newDevice.setRefId(serviceTemplateComponent.getAsmGUID());
                    newDevice.setRefType(serviceTemplateComponent.getType().name());
                    newDevice.setComponentId(serviceTemplateComponent.getId());
                    newDevice.setStatus(DeploymentStatusType.COMPLETE);
                    newDevice.setStatusMessage(serviceTemplateComponent.getName() + " deployment complete");
                    newDevice.setIpAddress(serviceTemplateComponent.getIP());
                    newDevice.setRefId(serviceTemplateComponent.getRefId());
                    DeviceInventoryEntity deviceInventoryEntity = this.deviceInventoryDAO
                            .getDeviceInventory(serviceTemplateComponent.getRefId());
                    newDevice.setServiceTag(deviceInventoryEntity.getServiceTag());
                    newDevice.setDeviceType(deviceInventoryEntity.getDeviceType());
                    newDevice.setDeviceHealth(deviceInventoryEntity.getHealth());
                    newDevice.setHealthMessage(deviceInventoryEntity.getHealthMessage());
                    newDevice.setCompliantState(CompliantState.fromValue(deviceInventoryEntity.getCompliant()));

                    deploymentDevices.add(newDevice);
                }
            }
        }

        return deploymentDevices;
    }

    /**
     * Removes the deployment devices and components from the service template that are not in ASM inventory
     *  
     * @param deployment whose devices and components in the service template will be removed if they are not in ASM
     *      inventory.
     * @return an updated Deployment whose devices and components have been removed if they do not exist in ASM 
     *      inventory.
     */
    public Deployment getDeploymentWithOnlyDevicesInInventory(Deployment deployment) {

        ArrayList<DeploymentDevice> deploymentDevicesToRemove = new ArrayList<DeploymentDevice>();
        for (DeploymentDevice deploymentDevice : deployment.getDeploymentDevice()) {
            if (!(BrownfieldStatus.AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) || 
                  BrownfieldStatus.NEWLY_AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) || 
                  BrownfieldStatus.CURRENTLY_DEPLOYED_IN_BROWNFIELD.equals(deploymentDevice.getBrownfieldStatus()) ||
                  BrownfieldStatus.NOT_APPLICABLE.equals(deploymentDevice.getBrownfieldStatus()))) {
                deploymentDevicesToRemove.add(deploymentDevice);
                deployment.getServiceTemplate().removeComponentWithId(deploymentDevice.getComponentId());
            }
        }

        deployment.getDeploymentDevice().removeAll(deploymentDevicesToRemove);

        return deployment;
    }

    // Creates the initial / starting Deployment with a base ServiceTemplate for the Brownfield discovery process
    private Deployment getBrownfieldStartingDeployment() {
        Deployment deployment = new Deployment();
        deployment.setBrownfield(true);
        deployment.setDeploymentName("");
        deployment.setCanCancel(false);
        deployment.setCanDelete(true);
        deployment.setCanDeleteResources(true);
        deployment.setCanEdit(false);
        deployment.setCanMigrate(false);
        deployment.setCanRetry(false);
        deployment.setCanScaleupApplication(false);
        deployment.setCanScaleupNetwork(false);
        deployment.setCanScaleupServer(false);
        deployment.setCanScaleupStorage(false);
        deployment.setCanScaleupApplication(false);
        deployment.setCanScaleupCluster(false);
        deployment.setCanScaleupNetwork(false);
        deployment.setCanScaleupServer(false);
        deployment.setCanScaleupStorage(false);
        deployment.setCanScaleupVM(false);
        deployment.setStatus(DeploymentStatusType.PENDING);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.getComponents().clear();
        serviceTemplate.setDraft(false);
        serviceTemplate.setEnableCluster(true);
        serviceTemplate.setEnableServer(true);
        serviceTemplate.setEnableStorage(true);
        serviceTemplate.setTemplateName("User Generated Template");
        serviceTemplate.setId(UUID.randomUUID().toString());

        deployment.setServiceTemplate(serviceTemplate);

        return deployment;
    }
    
    // It's not found in inventory so generate a fake one and add it (there will be no volume info in this case due to how volume is processed on UI side)
    // ONLY add it if it has not been added
    private int processMissingEquallogicBrownfield(Deployment deployment, 
                                                   String iscsiGroupIp, 
                                                   String volumeName, 
                                                   int unavailableStorageCount, 
                                                   BrownfieldStatus serverBrownfieldStatus,
                                                   String capacity) {
        
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        
        if (!serviceTemplate.doesComponentWithSettingValueExist(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, volumeName)) {
            String ipaddress = "";
            if (iscsiGroupIp != null && iscsiGroupIp.trim().length() > 6) {
                ipaddress = iscsiGroupIp;
            }

            DeviceInventoryEntity notFoundEquallogic = this.getDeviceInventoryEntityForDeviceNotFound(ServiceTemplateComponentType.STORAGE, DeviceType.equallogic, ipaddress, "Not Found");  // leave ip and asset tag empty since it can't be found
            // ServiceTemplateComponent notFoundEquallogicComponent = this.getServiceTemplateComponent(notFoundEquallogic, STORAGE + storageCount++, volumeName, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);
            ServiceTemplateComponent notFoundEquallogicComponent = this.getServiceTemplateComponent(notFoundEquallogic, UNAVAILABLE_STORAGE + unavailableStorageCount++, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);
            notFoundEquallogicComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_EQL_COMP_ID);
            
            ServiceTemplateCategory storageEqlCompId = new ServiceTemplateCategory(ServiceTemplateSettingDef.STORAGE_EQL_COMP_ID);
            
            ServiceTemplateSetting templateTitleId = new ServiceTemplateSetting(ServiceTemplateSettingDef.TITLE_ID, volumeName, ServiceTemplateSettingType.STRING);
            storageEqlCompId.getParameters().add(templateTitleId);
            
            ServiceTemplateSetting capacitySetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_SIZE, capacity, ServiceTemplateSettingType.STRING);
            storageEqlCompId.getParameters().add(capacitySetting);
            
            notFoundEquallogicComponent.getResources().add(storageEqlCompId);

            // new to 8.3.1 ASM-7291
            ServiceTemplateUtil.convertStorageTitleToExistingVolume(storageEqlCompId);

            serviceTemplate.addComponent(notFoundEquallogicComponent);
            DeploymentDevice notFoundEquallogicDeploymentDevice = this.getBrownfieldDeploymentDeviceForDeviceNotInInventory(notFoundEquallogic, notFoundEquallogicComponent.getId(), ServiceTemplateComponentType.STORAGE);
            if (BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY.equals(serverBrownfieldStatus)) {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_IN_INVENTORY);
            }
            else if (BrownfieldStatus.UNAVAILABLE_IN_EXISTING_SERVICE.equals(serverBrownfieldStatus)) {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_IN_EXISTING_SERVICE);
            }
            else if(BrownfieldStatus.UNAVAILABLE_NOT_MANAGED_OR_RESERVED.equals(serverBrownfieldStatus)) {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED);
            }
            else {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY);
            }
            
            deployment.getDeploymentDevice().add(notFoundEquallogicDeploymentDevice);
        }
        
        return unavailableStorageCount;
    }
    
    // It's not found in inventory so generate a fake one and add it (there will be no volume info in this case due to how volume is processed on UI side)
    // ONLY add it if it has not been added
    private int processMissingCompellentBrownfield(Deployment deployment, 
                                                   String iscsiGroupIp, 
                                                   String volumeName, 
                                                   int unavailableStorageCount, 
                                                   BrownfieldStatus serverBrownfieldStatus,
                                                   String capacity) {
        
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        
        if (!serviceTemplate.doesComponentWithSettingValueExist(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, volumeName)) {
            String ipaddress = "";
            if (iscsiGroupIp != null && iscsiGroupIp.trim().length() > 6) {
                ipaddress = iscsiGroupIp;
            }

            DeviceInventoryEntity notFoundCompellent = this.getDeviceInventoryEntityForDeviceNotFound(ServiceTemplateComponentType.STORAGE, DeviceType.compellent, ipaddress, "Not Found");  // leave ip and asset tag empty since it can't be found
            ServiceTemplateComponent notFoundCompellentComponent = this.getServiceTemplateComponent(notFoundCompellent, UNAVAILABLE_STORAGE + unavailableStorageCount++, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);
            notFoundCompellentComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID);
            
            ServiceTemplateCategory storageCompellentCompId = new ServiceTemplateCategory(ServiceTemplateSettingDef.STORAGE_COMPELLENT_COMP_ID);
            
            ServiceTemplateSetting templateTitleId = new ServiceTemplateSetting(ServiceTemplateSettingDef.TITLE_ID, volumeName, ServiceTemplateSettingType.STRING);
            storageCompellentCompId.getParameters().add(templateTitleId);
            
            ServiceTemplateSetting capacitySetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_SIZE, capacity, ServiceTemplateSettingType.STRING);
            storageCompellentCompId.getParameters().add(capacitySetting);
            
            notFoundCompellentComponent.getResources().add(storageCompellentCompId);

            // new to 8.3.1 ASM-7291
            ServiceTemplateUtil.convertStorageTitleToExistingVolume(storageCompellentCompId);

            serviceTemplate.addComponent(notFoundCompellentComponent);
            DeploymentDevice notFoundEquallogicDeploymentDevice = this.getBrownfieldDeploymentDeviceForDeviceNotInInventory(notFoundCompellent, notFoundCompellentComponent.getId(), ServiceTemplateComponentType.STORAGE);
            if (BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY.equals(serverBrownfieldStatus)) {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_IN_INVENTORY);
            }
            else if (BrownfieldStatus.UNAVAILABLE_IN_EXISTING_SERVICE.equals(serverBrownfieldStatus)) {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_IN_EXISTING_SERVICE);
            }
            else if(BrownfieldStatus.UNAVAILABLE_NOT_MANAGED_OR_RESERVED.equals(serverBrownfieldStatus)) {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED);
            }
            else {
                notFoundEquallogicDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY);
            }
            
            deployment.getDeploymentDevice().add(notFoundEquallogicDeploymentDevice);
        }
        
        return unavailableStorageCount;
    }
    
    // It's not found in inventory so generate a fake one and add it (there will be no volume info in this case due 
    // to how volume is processed on UI side) and ONLY add it if it has not been added
    private int processMissingNetappBrownfield(Deployment deployment, 
                                               String nfsHost, 
                                               String volumeName, 
                                               int unavailableStorageCount, 
                                               BrownfieldStatus serverBrownfieldStatus,
                                               String capacity) {
        
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        
        if (!serviceTemplate.doesComponentWithSettingValueExist(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, volumeName)) {

            DeviceInventoryEntity notFoundNetapp = this.getDeviceInventoryEntityForDeviceNotFound(ServiceTemplateComponentType.STORAGE, DeviceType.netapp, nfsHost, "Not Found");  // leave ip and asset tag empty since it can't be found
            ServiceTemplateComponent notFoundNetappComponent = this.getServiceTemplateComponent(notFoundNetapp, UNAVAILABLE_STORAGE + unavailableStorageCount++, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);
            
            ServiceTemplateCategory storageNetappCompId = new ServiceTemplateCategory(ServiceTemplateSettingDef.STORAGE_NETAPP_COMP_ID);
            
            ServiceTemplateSetting templateTitleId = new ServiceTemplateSetting(ServiceTemplateSettingDef.TITLE_ID, volumeName, ServiceTemplateSettingType.STRING);
            storageNetappCompId.getParameters().add(templateTitleId);
            
            ServiceTemplateSetting capacitySetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_SIZE, capacity, ServiceTemplateSettingType.STRING);
            storageNetappCompId.getParameters().add(capacitySetting);
            
            notFoundNetappComponent.getResources().add(storageNetappCompId);

            // new to 8.3.1 ASM-7291
            ServiceTemplateUtil.convertStorageTitleToExistingVolume(storageNetappCompId);

            serviceTemplate.addComponent(notFoundNetappComponent);
            DeploymentDevice notFoundNetappDeploymentDevice = this.getBrownfieldDeploymentDeviceForDeviceNotInInventory(notFoundNetapp, notFoundNetappComponent.getId(), ServiceTemplateComponentType.STORAGE);
            if (BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY.equals(serverBrownfieldStatus)) {
                notFoundNetappDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_IN_INVENTORY);
            }
            else if (BrownfieldStatus.UNAVAILABLE_IN_EXISTING_SERVICE.equals(serverBrownfieldStatus)) {
                notFoundNetappDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_IN_EXISTING_SERVICE);
            }
            else if(BrownfieldStatus.UNAVAILABLE_NOT_MANAGED_OR_RESERVED.equals(serverBrownfieldStatus)) {
                notFoundNetappDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED);
            }
            else {
                notFoundNetappDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY);
            }
            
            deployment.getDeploymentDevice().add(notFoundNetappDeploymentDevice);
        }
        
        return unavailableStorageCount;
    }
    
    private int processFoundEquallogicBrownfield(Deployment deployment, 
                                                 DatastoreDTO datastoreDTO, 
                                                 PuppetEquallogicDevice equalLogicPuppetDevice,
                                                 String iscsiIqn, 
                                                 ServiceTemplateComponent serverComponent, 
                                                 List<DeviceInventoryEntity> equallogicDevices, 
                                                 int storageCount, 
                                                 BrownfieldStatus serverBrownfieldStatus,
                                                 String capacity) {

        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();

        if (equalLogicPuppetDevice != null) {  
         // If it's in puppet inventory then we need to see if it's in DeviceInventory (should be!) and process
            PuppetEquallogicDevice.VolumeProperties volumeProperties = equalLogicPuppetDevice.getVolumePropertiesByIscsiIqn(iscsiIqn);    
            DeviceInventoryEntity equallogicDeviceInventoryEntity = this.getDeviceInventoryEntityWithRefId(equallogicDevices, equalLogicPuppetDevice.getCertName());
            
            String storageComponentName = ""; // name will be defined later
            ServiceTemplateComponent equallogicComponent = this.getServiceTemplateComponent(equallogicDeviceInventoryEntity, storageComponentName, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE); 
            equallogicComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_EQL_COMP_ID);
            
            // ONLY Add it if we have a volume for it and if it has not already been added previously (we don't want duplicates!)
            if (volumeProperties.getName()!= null && volumeProperties.getName().trim().length() > 0) { 
                ServiceTemplateComponent existingStorageComponent = 
                        serviceTemplate.findComponentBySettingAndValue(
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, 
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, equallogicDeviceInventoryEntity.getRefId(),
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING, volumeProperties.getName());
                
                if (existingStorageComponent == null) {
                    ServiceTemplateCategory storageEqlCompIdCategory = new ServiceTemplateCategory(ServiceTemplateSettingDef.STORAGE_EQL_COMP_ID);
                    
                    ServiceTemplateSetting asmGuidSetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.ASM_GUID,
                            equallogicComponent.getAsmGUID(), ServiceTemplateSettingType.ENUMERATED);
                    ServiceTemplateOption asmGuidSettingOption = new ServiceTemplateOption();
                    asmGuidSettingOption.setName(equallogicDeviceInventoryEntity.getServiceTag());
                    asmGuidSettingOption.setValue(equallogicComponent.getAsmGUID());
                    asmGuidSetting.getOptions().add(asmGuidSettingOption);
                    storageEqlCompIdCategory.getParameters().add(asmGuidSetting);
                    
                    // Target EqualLogic
                    ServiceTemplateSetting templateTitleId = new ServiceTemplateSetting(ServiceTemplateSettingDef.TITLE_ID, volumeProperties.getName(), ServiceTemplateSettingType.STRING);
                    storageEqlCompIdCategory.getParameters().add(templateTitleId);
                    
                    ServiceTemplateSetting capacitySetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_SIZE, capacity, ServiceTemplateSettingType.STRING);
                    storageEqlCompIdCategory.getParameters().add(capacitySetting);
    
                    // Uniquely identifying information
                    ServiceTemplateSetting iqnId = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_ISCSI_IQN, iscsiIqn, ServiceTemplateSettingType.STRING); 
                    storageEqlCompIdCategory.getParameters().add(iqnId);
                    
                    ServiceTemplateSetting ensureSetting = new ServiceTemplateSetting(
                            ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
                    ensureSetting.setRequired(false);
                    ensureSetting.setRequiredAtDeployment(false);
                    ensureSetting.setHideFromTemplate(true);
                    storageEqlCompIdCategory.getParameters().add(ensureSetting);
    
                    equallogicComponent.getResources().add(storageEqlCompIdCategory);

                    // new to 8.3.1 ASM-7291
                    ServiceTemplateUtil.convertStorageTitleToExistingVolume(storageEqlCompIdCategory);

                    serviceTemplate.addComponent(equallogicComponent);

                    DeploymentDevice storageEqlDeploymentDevice = this.getBrownfieldDeploymentDeviceFromDeviceInventory(equallogicDeviceInventoryEntity, equallogicComponent.getId(), ServiceTemplateComponentType.STORAGE);
                    storageCount = this.setComponentNameAndDeploymentDeviceBrownfieldStatus(equallogicComponent, 
                            equallogicDeviceInventoryEntity, 
                            storageEqlDeploymentDevice, 
                            serverBrownfieldStatus, 
                            storageCount);

                    deployment.getDeploymentDevice().add(storageEqlDeploymentDevice);
                    
                    // Add related components
                    if (serverComponent != null) {
                        serverComponent.addAssociatedComponentName(equallogicComponent.getId(), equallogicComponent.getName());
                        equallogicComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                    }
                }
                else { // component exists so we only need to add the relationship
                    // Add related components
                    if (serverComponent != null) {
                        serverComponent.addAssociatedComponentName(existingStorageComponent.getId(), equallogicComponent.getName());
                        equallogicComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                        
                        // Update Storage Status if the Server is available (storage may be available on one, but not all servers)
                        if (BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                            if (volumeProperties.getName()!= null && volumeProperties.getName().trim().length() > 0) { 
//                                ServiceTemplateComponent existingStorageComp = serviceTemplate.findComponentBySettingAndValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, iscsiIqn);
                                DeploymentDevice equallogicDevice = deployment.getDeploymentDevice(existingStorageComponent.getId());
                                if (equallogicDevice != null && equallogicDevice.getBrownfieldStatus().isAttachedDeviceAvailable()) {
                                    equallogicDevice.setBrownfieldStatus(BrownfieldStatus.AVAILABLE);
                                }
                            }
                        }
                    }
                }
            }
        } 
           
        return storageCount;
    }
    
    private int processFoundCompellentBrownfield(Deployment deployment, 
                                                 DatastoreDTO datastoreDTO, 
                                                 PuppetCompellentDevice compellentPuppetDevice,
                                                 String iscsiDeviceId, 
                                                 boolean isFiberChannel,
                                                 ServiceTemplateComponent serverComponent, 
                                                 List<DeviceInventoryEntity> compellentDevices, 
                                                 int storageCount, 
                                                 BrownfieldStatus serverBrownfieldStatus,
                                                 String capacity) {

        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();

        if (compellentPuppetDevice != null) {  
         // If it's in puppet inventory then we need to see if it's in DeviceInventory (should be!) and process
            PuppetCompellentDevice.Volume volume = compellentPuppetDevice.getVolumeWithIscsiDeviceId(iscsiDeviceId);
            DeviceInventoryEntity compellentDeviceInventoryEntity = this.getDeviceInventoryEntityWithRefId(compellentDevices, compellentPuppetDevice.getCertName());
            
            String storageComponentName = ""; // name will be defined later
            ServiceTemplateComponent compellentComponent = this.getServiceTemplateComponent(compellentDeviceInventoryEntity, storageComponentName, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE); 
            compellentComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID);
            
            // ONLY Add it if we have a volume for it and if it has not already been added previously (we don't want duplicates!)
            if (volume != null && volume.getName()!= null && volume.getName().trim().length() > 0) { 
                ServiceTemplateComponent existingStorageComponent = 
                        serviceTemplate.findComponentBySettingAndValue(
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, 
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, compellentDeviceInventoryEntity.getRefId(),
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING, volume.getName());
                
                if (existingStorageComponent == null) {
                    ServiceTemplateCategory storageCompellentCompIdCategory = new ServiceTemplateCategory(ServiceTemplateSettingDef.STORAGE_COMPELLENT_COMP_ID);
                    
                    ServiceTemplateSetting asmGuidSetting = new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, 
                            compellentComponent.getAsmGUID(), "Target Compellent", ServiceTemplateSettingType.ENUMERATED);
                    ServiceTemplateOption asmGuidSettingOption = new ServiceTemplateOption();
                    asmGuidSettingOption.setName(compellentDeviceInventoryEntity.getServiceTag());
                    asmGuidSettingOption.setValue(compellentComponent.getAsmGUID());
                    asmGuidSetting.getOptions().add(asmGuidSettingOption);
                    
                    // asmGuidSetting.setOptions(options);
                    storageCompellentCompIdCategory.getParameters().add(asmGuidSetting);
                    
                    // Target Compellent
                    ServiceTemplateSetting templateTitleId = new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID,
                            volume.getName(), "Storage Volume Name", ServiceTemplateSettingType.STRING);
                    storageCompellentCompIdCategory.getParameters().add(templateTitleId);

                    ServiceTemplateSetting capacitySetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_SIZE, capacity, ServiceTemplateSettingType.STRING);
                    storageCompellentCompIdCategory.getParameters().add(capacitySetting);
                    
                    // Set the Port Type
                    ServiceTemplateSetting portTypeSetting = null;
                    if(isFiberChannel) {
                        portTypeSetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.COMPELLENT_PORTTYPE_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_FIBRE_CHANNEL, ServiceTemplateSettingType.STRING); 
                    }
                    else {
                       portTypeSetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.COMPELLENT_PORTTYPE_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ISCSI, ServiceTemplateSettingType.STRING);
                    }
                    storageCompellentCompIdCategory.getParameters().add(portTypeSetting);
                    
                    // Uniquely identifying information
                    ServiceTemplateSetting iqnId = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_ISCSI_IQN, iscsiDeviceId, ServiceTemplateSettingType.STRING); 
                    iqnId.setHideFromTemplate(true);
                    storageCompellentCompIdCategory.getParameters().add(iqnId);

                    
                    ServiceTemplateSetting ensureSetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
                    ensureSetting.setRequired(false);
                    ensureSetting.setRequiredAtDeployment(false);
                    ensureSetting.setHideFromTemplate(true);
                    storageCompellentCompIdCategory.getParameters().add(ensureSetting);
    
                    compellentComponent.getResources().add(storageCompellentCompIdCategory);

                    // new to 8.3.1 ASM-7291
                    ServiceTemplateUtil.convertStorageTitleToExistingVolume(storageCompellentCompIdCategory);
                    
                    serviceTemplate.addComponent(compellentComponent);

                    DeploymentDevice storageCompellentDeploymentDevice = this.getBrownfieldDeploymentDeviceFromDeviceInventory(compellentDeviceInventoryEntity, compellentComponent.getId(), ServiceTemplateComponentType.STORAGE);
                    storageCount = this.setComponentNameAndDeploymentDeviceBrownfieldStatus(compellentComponent, 
                                        compellentDeviceInventoryEntity, 
                                        storageCompellentDeploymentDevice, 
                                        serverBrownfieldStatus, 
                                        storageCount);

                    deployment.getDeploymentDevice().add(storageCompellentDeploymentDevice);
                    
                    // Add related components
                    if (serverComponent != null) {
                        serverComponent.addAssociatedComponentName(compellentComponent.getId(), compellentComponent.getName());
                        compellentComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                    }
                }
                else { // component exists so we only need to add the relationship
                    // Add related components
                    if (serverComponent != null) {
                        serverComponent.addAssociatedComponentName(existingStorageComponent.getId(), compellentComponent.getName());
                        compellentComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                        
                        // Update Storage Status if the Server is available (storage may be available on one, but not all servers)
                        if (BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                            if (volume.getName()!= null && volume.getName().trim().length() > 0) { 
//                                ServiceTemplateComponent existingStorageComp = serviceTemplate.findComponentBySettingAndValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, iscsiDeviceId);
                                DeploymentDevice compellentDevice = deployment.getDeploymentDevice(existingStorageComponent.getId());
                                if (compellentDevice != null && compellentDevice.getBrownfieldStatus().isAttachedDeviceAvailable()) {
                                    compellentDevice.setBrownfieldStatus(BrownfieldStatus.AVAILABLE);
                                }
                            }
                        }
                    }
                }
            }
        } 
           
        return storageCount;
    }
    
    
    /**
     * The Servers found in brownfield deployment must be marked as deployed as part of the brownfield deployment 
     * process.  This is primarily used for the brownfield deployment process.
     *  
     * @param deployment whose servers will be marked as deployed as part of the brownfield deployment.
     * @throws AsmManagerCheckedException if there is an issue calling to the database.
     */
    public void updateBrownfieldServersToDeployedState(Deployment deployment) throws AsmManagerCheckedException {

        for (ServiceTemplateComponent serviceTemplateComponent : deployment.getServiceTemplate().getComponents()) {
            if (ServiceTemplateComponentType.SERVER.equals(serviceTemplateComponent.getType())) {
                DeviceInventoryEntity serverDeviceInventoryEntity = this.deviceInventoryDAO
                        .getDeviceInventory(serviceTemplateComponent.getRefId());
                serverDeviceInventoryEntity.setState(DeviceState.DEPLOYED);
                this.deviceInventoryDAO.updateDeviceInventory(serverDeviceInventoryEntity);
            }
        }
    }

    /**
     * Returns the Servers available for brownfield deployment as a set of SelectedServers.
     * 
     * @param serviceTemplate from which the SelectedServers will be created.
     * @return the Servers available for brownfield deployment as a set of SelectedServers.
     */
    public List<SelectedServer> getSelectedServersForBrownfield(ServiceTemplate serviceTemplate) {
        ArrayList<SelectedServer> selectedServers = new ArrayList<SelectedServer>();

        for (ServiceTemplateComponent serviceTemplateComponent : serviceTemplate
                .getServiceTemplateComponentsByType(ServiceTemplateComponentType.SERVER)) {
            SelectedServer selectedServer = new SelectedServer();
            selectedServer.setComponentId(serviceTemplateComponent.getId());
            selectedServer.setIpAddress(serviceTemplateComponent.getIP());
            selectedServer.setRefId(serviceTemplateComponent.getAsmGUID());
            // selectedServer.setServiceTag(); No service tag available 
            selectedServers.add(selectedServer);
        }

        return selectedServers;
    }
    

    /**
     * Identifies the differences between the existing service in ASM and a new brownfield discovery and returns a 
     * deployment that captures the diff.  New components are added to the template, and their na
     */
    public Deployment defineServiceDiff(Deployment currentService) throws WebApplicationException {
        
        // Should ONLY be one Cluster!
        ServiceTemplateComponent clusterComponent = currentService.getServiceTemplate().getServiceTemplateComponentsByType(ServiceTemplateComponentType.CLUSTER).get(0);
        
        EsxiServiceDefinition serviceDefinition = new EsxiServiceDefinition();   
        serviceDefinition.setClusterComponentName(clusterComponent.getName());
        serviceDefinition.setClusterName(clusterComponent.getParameterValue(ServiceTemplateSettingDef.ESX_CLUSTER_COMP_ID.getId(), ServiceTemplateSettingDef.CLUSTER_CLUSTER_ID.getId()));
        serviceDefinition.setDatacenterName(clusterComponent.getParameterValue(ServiceTemplateSettingDef.ESX_CLUSTER_COMP_ID.getId(), ServiceTemplateSettingDef.CLUSTER_DATACENTER_ID.getId()));
        serviceDefinition.setVcenterRefId(clusterComponent.getAsmGUID());
        
        Deployment newDeployment = this.defineService(serviceDefinition);
        newDeployment.setDeploymentName(currentService.getDeploymentName());
        
        // Set all DeploymentDevices of existing service to isAvailableForBrownfieldDeployemnt for now
        currentService.setAllDeploymentDevicesBrownfieldStatus(BrownfieldStatus.CURRENTLY_DEPLOYED_IN_BROWNFIELD);

        int newlyAvailableServerCount = this.getLastNumberForComponentType(currentService.getServiceTemplate(), ServiceTemplateComponentType.SERVER) + 1;
        int newlyAvailableStorageCount = this.getLastNumberForComponentType(currentService.getServiceTemplate(), ServiceTemplateComponentType.STORAGE) + 1;
        int unavailableServerCount = 1;
        int unavailableStorageCount = 1;
        
        // We MUST process Servers first so their status can be updated properly in order to be accessible to determine
        // the appropriate status of Storages that are attached to them.  These loops MUST NOT be combined.
        for (ServiceTemplateComponent serviceTemplateComponent: newDeployment.getServiceTemplate().getComponents()) {
            if (ServiceTemplateComponentType.SERVER.equals(serviceTemplateComponent.getType())) {
                ServiceTemplateComponent matchingServer = currentService.getServiceTemplate().findComponentByGUID(serviceTemplateComponent.getAsmGUID());

                if (matchingServer == null) {
                
                    DeploymentDevice deploymentDevice = newDeployment.getDeploymentDevice(serviceTemplateComponent.getId());
                    // If it's available in the newDeployment, but it's not found in the exist service, then it means
                    // it will be 'newly' available for the update of the inventory
                    if (deploymentDevice != null && BrownfieldStatus.AVAILABLE.equals(deploymentDevice.getBrownfieldStatus())) {
                        serviceTemplateComponent.setName(SERVER + newlyAvailableServerCount++);
                        deploymentDevice.setBrownfieldStatus(BrownfieldStatus.NEWLY_AVAILABLE);
                        
                        // Add related components
                        clusterComponent.addAssociatedComponentName(serviceTemplateComponent.getId(), serviceTemplateComponent.getName());
                        serviceTemplateComponent.addAssociatedComponentName(clusterComponent.getId(), clusterComponent.getName());
                        
                        // Add related Storages from existing service so relationships are displayed properly
                        List<ServiceTemplateComponent> storageComps = this.findMatchingStorageComponent(currentService.getServiceTemplate(), newDeployment.getServiceTemplate(), serviceTemplateComponent);
                        for (ServiceTemplateComponent relatedStorageComp : storageComps) {
                            serviceTemplateComponent.addAssociatedComponentName(relatedStorageComp.getId(), relatedStorageComp.getName());
                            relatedStorageComp.addAssociatedComponentName(serviceTemplateComponent.getId(), serviceTemplateComponent.getName());
                        }
                    }
                    else { // If it exists in the other service and is 'in use', then it can be used here as well  
                        serviceTemplateComponent.setName(UNAVAILABLE_SERVER + unavailableServerCount++);
                    }
                
                    currentService.getServiceTemplate().getComponents().add(serviceTemplateComponent);
                    if (deploymentDevice != null) {
                        currentService.getDeploymentDevice().add(deploymentDevice);
                    }
                }
            }
        }
        
        for (ServiceTemplateComponent serviceTemplateComponent: newDeployment.getServiceTemplate().getComponents()) {
            if (ServiceTemplateComponentType.STORAGE.equals(serviceTemplateComponent.getType())) {
                ServiceTemplateComponent matchingEquallogicComponent = this.findExistingStorageComponent(currentService.getServiceTemplate(), serviceTemplateComponent);
            
                if (matchingEquallogicComponent == null) {
                    DeploymentDevice deploymentDevice = newDeployment.getDeploymentDevice(serviceTemplateComponent.getId());

                    
                    currentService.getServiceTemplate().getComponents().add(serviceTemplateComponent);
                    if (deploymentDevice != null) {
                        currentService.getDeploymentDevice().add(deploymentDevice);
                    }
                    
                    // Add related Storages from existing service so relationships are displayed properly
                    List<ServiceTemplateComponent> storageComps = this.findMatchingServerComponent(currentService.getServiceTemplate(), newDeployment.getServiceTemplate(), serviceTemplateComponent);
                    boolean relatedServerAvailable = false;
                    for (ServiceTemplateComponent relatedServerComp : storageComps) {
                        serviceTemplateComponent.addAssociatedComponentName(relatedServerComp.getId(), relatedServerComp.getName());
                        relatedServerComp.addAssociatedComponentName(serviceTemplateComponent.getId(), serviceTemplateComponent.getName());
                        DeploymentDevice relatedServerDeploymentDevice = currentService.getDeploymentDeviceByRefId(relatedServerComp.getAsmGUID());
                        if(relatedServerDeploymentDevice != null && relatedServerDeploymentDevice.getBrownfieldStatus() != null && 
                                BrownfieldStatus.CURRENTLY_DEPLOYED_IN_BROWNFIELD.equals(relatedServerDeploymentDevice.getBrownfieldStatus())) {
                            relatedServerAvailable = true;
                        }
                    }
                    
                    // If it's available in the newDeployment, but it's not found in the exist service, then it means
                    // it will be 'newly' available for the update of the inventory
                    if (deploymentDevice != null && 
                            (BrownfieldStatus.AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) 
                            || (BrownfieldStatus.UNAVAILABLE_RELATED_SERVER_IN_EXISTING_SERVICE.equals(deploymentDevice.getBrownfieldStatus()) && relatedServerAvailable) 
                            || BrownfieldStatus.AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()))) {
                        serviceTemplateComponent.setName(STORAGE + newlyAvailableStorageCount++);
                        deploymentDevice.setBrownfieldStatus(BrownfieldStatus.NEWLY_AVAILABLE);
                    } 
                    else {
                        // It's unavailable and we need the names to be listed in order, so rename here
                        serviceTemplateComponent.setName(UNAVAILABLE_STORAGE + unavailableStorageCount++);
                    }
                }
            }
        }
        
        // Don't renumber the Available or it will mess up the component names of the existing servers in the Service
        currentService.renumberAndRenameServiceTemplateComponents(false);
        
        return currentService;
    }
    
    // Parses the name of all of the components of the given type and returns the last number used
    private int getLastNumberForComponentType(ServiceTemplate serviceTemplate, 
                                              ServiceTemplateComponentType componentType) {
        
        int lastNumberUsed = 1;
        
        for (ServiceTemplateComponent serviceTemplateComponent : serviceTemplate.getComponents()) {
            if (componentType.equals(serviceTemplateComponent.getType())) {
                String[] nameParts = StringUtils.split(serviceTemplateComponent.getName(), ' ');
                if (nameParts.length > 0) {
                    int componentNumber =  Integer.parseInt(nameParts[nameParts.length - 1]); 
                    if (componentNumber > lastNumberUsed) {
                        lastNumberUsed = componentNumber;
                    }
                }
            }
        }
        
        return lastNumberUsed;
    }    
    
    // Returns a list of ServiceTemplateComponents from the currentSerrviceTemplate for the given server component
    private List<ServiceTemplateComponent> findMatchingStorageComponent(ServiceTemplate currentServiceTemplate, 
                                                                        ServiceTemplate diffServiceTemplate, 
                                                                        ServiceTemplateComponent serverComponent) {
        
        ArrayList<ServiceTemplateComponent> storageComponents = new ArrayList<ServiceTemplateComponent>();
        
        for (String componentId : serverComponent.getAssociatedComponents().keySet()) {
            ServiceTemplateComponent relatedComponent = diffServiceTemplate.findComponentById(componentId);
            if (relatedComponent != null) {
                if (ServiceTemplateComponentType.STORAGE.equals(relatedComponent.getType())) {
                    // Try Equallogic First
                    String scsiIqn = relatedComponent.getParameterValue(
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, 
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN);
                    if (scsiIqn != null) {
                        ServiceTemplateComponent matchingStorageComponent = 
                                currentServiceTemplate.findComponentBySettingAndValue(
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, 
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, scsiIqn);
                        if (matchingStorageComponent != null) {
                            storageComponents.add(matchingStorageComponent);
                        }
                    }
                    else { // Try Compellent Second
                        scsiIqn = relatedComponent.getParameterValue(
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, 
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN);
                        if (scsiIqn != null) {
                            ServiceTemplateComponent matchingStorageComponent = 
                                    currentServiceTemplate.findComponentBySettingAndValue(
                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, 
                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, scsiIqn);
                            if (matchingStorageComponent != null) {
                                storageComponents.add(matchingStorageComponent);
                            }
                        }
                        else { // Finally Try Netapp last
                            String asmGuid = relatedComponent.getParameterValue(
                                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, 
                                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID); 
                            String volumeName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(
                                    relatedComponent.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID));
                            if(asmGuid != null && volumeName != null) {
                                ServiceTemplateComponent matchingStorageComponent = 
                                        currentServiceTemplate.findComponentBySettingAndValue(
                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID,
                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, asmGuid,
                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING, volumeName);
                                if (matchingStorageComponent != null) {
                                    storageComponents.add(matchingStorageComponent);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return storageComponents;
    }
    
    
    // Returns a list of ServiceTemplateComponents from the currentSerrviceTemplate for the given storage component
    private List<ServiceTemplateComponent> findMatchingServerComponent(ServiceTemplate currentServiceTemplate, 
                                                                       ServiceTemplate diffServiceTemplate, 
                                                                       ServiceTemplateComponent storageComponent) {

        ArrayList<ServiceTemplateComponent> serverComponents = new ArrayList<ServiceTemplateComponent>();
        
        for (String componentId : storageComponent.getAssociatedComponents().keySet()) {
            ServiceTemplateComponent relatedComponent = diffServiceTemplate.findComponentById(componentId);
            if (relatedComponent != null) {
                if (ServiceTemplateComponentType.SERVER.equals(relatedComponent.getType())) {
                    String ipAddress = 
                            relatedComponent.getParameterValue(
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID, 
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID);
                    if (ipAddress != null) {
                        ServiceTemplateComponent matchingStorageComponent = 
                                currentServiceTemplate.findComponentBySettingAndValue(
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID, 
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID, 
                                        ipAddress); 
                        if (matchingStorageComponent != null) {
                            serverComponents.add(matchingStorageComponent);
                        }
                    }
                    
                }
            }
        }
        
        return serverComponents;
    }
    
    
    private int processFoundNetappBrownfield(Deployment deployment, 
                                             DatastoreDTO datastoreDTO, 
                                             PuppetNetappDevice netappPuppetDevice,
                                             String volumeName, 
                                             ServiceTemplateComponent serverComponent, 
                                             List<DeviceInventoryEntity> netappDevices, 
                                             int storageCount, 
                                             BrownfieldStatus serverBrownfieldStatus,
                                             String capacity) {

        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();

        if (netappPuppetDevice != null) {  
         // If it's in puppet inventory then we need to see if it's in DeviceInventory (should be!) and process
            PuppetNetappDevice.VolumeData volumeData = netappPuppetDevice.getVolumeDataByVolumeName(volumeName);
            DeviceInventoryEntity netappDeviceInventoryEntity = this.getDeviceInventoryEntityWithRefId(netappDevices, netappPuppetDevice.getClientCert());
            
            String storageComponentName = ""; // name will be defined later
            ServiceTemplateComponent netappComponent = this.getServiceTemplateComponent(netappDeviceInventoryEntity, storageComponentName, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE); 
            netappComponent.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_NETAPP_COMP_ID);
            
            // ONLY Add it if we have a volume for it and if it has not already been added previously (we don't want duplicates!)
            if (volumeData != null && volumeData.getName() != null && volumeData.getName().trim().length() > 0) { 
                ServiceTemplateComponent existingStorageComponent = 
                        serviceTemplate.findComponentBySettingAndValue(
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID,
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, netappDeviceInventoryEntity.getRefId(),
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING, volumeData.getName());
                
                if (existingStorageComponent == null) {
                    ServiceTemplateCategory storageNetappCompIdCategory = new ServiceTemplateCategory(ServiceTemplateSettingDef.STORAGE_NETAPP_COMP_ID);
                    
                    ServiceTemplateSetting asmGuidSetting = new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID,
                            netappComponent.getAsmGUID(), "Target NetApp", ServiceTemplateSettingType.ENUMERATED);
                    ServiceTemplateOption asmGuidSettingOption = new ServiceTemplateOption();
                    asmGuidSettingOption.setName(netappDeviceInventoryEntity.getServiceTag());
                    asmGuidSettingOption.setValue(netappComponent.getAsmGUID());
                    asmGuidSetting.getOptions().add(asmGuidSettingOption);

                    storageNetappCompIdCategory.getParameters().add(asmGuidSetting);
                    
                    // Target Netapp
                    ServiceTemplateSetting templateTitleId = new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, volumeData.getName(), "Storage Volume Name", ServiceTemplateSettingType.STRING);
                    ServiceTemplateOption volumeSettingOption = new ServiceTemplateOption();
                    volumeSettingOption.setName(volumeName);
                    volumeSettingOption.setValue(volumeName);
                    templateTitleId.getOptions().add(volumeSettingOption);

                    storageNetappCompIdCategory.getParameters().add(templateTitleId);
                    
                    ServiceTemplateSetting capacitySetting = new ServiceTemplateSetting(ServiceTemplateSettingDef.STORAGE_SIZE, capacity, ServiceTemplateSettingType.STRING);
                    storageNetappCompIdCategory.getParameters().add(capacitySetting);
    
                    ServiceTemplateSetting ensureSetting = new ServiceTemplateSetting(
                            ServiceTemplateSettingDef.ENSURE, "present", ServiceTemplateSettingType.STRING);
                    ensureSetting.setRequired(false);
                    ensureSetting.setRequiredAtDeployment(false);
                    ensureSetting.setHideFromTemplate(true);
                    storageNetappCompIdCategory.getParameters().add(ensureSetting);
    
                    netappComponent.getResources().add(storageNetappCompIdCategory);

                    // new to 8.3.1 ASM-7291
                    ServiceTemplateUtil.convertStorageTitleToExistingVolume(storageNetappCompIdCategory);

                    serviceTemplate.addComponent(netappComponent);

                    DeploymentDevice storageNetappDeploymentDevice = this.getBrownfieldDeploymentDeviceFromDeviceInventory(netappDeviceInventoryEntity, netappComponent.getId(), ServiceTemplateComponentType.STORAGE);
                    storageCount = this.setComponentNameAndDeploymentDeviceBrownfieldStatus(netappComponent, 
                            netappDeviceInventoryEntity, 
                                        storageNetappDeploymentDevice, 
                                        serverBrownfieldStatus, 
                                        storageCount);

                    deployment.getDeploymentDevice().add(storageNetappDeploymentDevice);
                    
                    // Add related components
                    if (serverComponent != null) {
                        serverComponent.addAssociatedComponentName(netappComponent.getId(), netappComponent.getName());
                        netappComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                    }
                }
                else { // component exists so we only need to add the relationship
                    // Add related components
                    if (serverComponent != null) {
                        serverComponent.addAssociatedComponentName(existingStorageComponent.getId(), netappComponent.getName());
                        netappComponent.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                        
                        // Update Storage Status if the Server is available (storage may be available on one, but not all servers)
                        if (BrownfieldStatus.AVAILABLE.equals(serverBrownfieldStatus)) {
                            if (volumeData.getName()!= null && volumeData.getName().trim().length() > 0) { 
//                                ServiceTemplateComponent existingStorageComp = serviceTemplate.findComponentBySettingAndValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, iscsiIqn);
                                DeploymentDevice netappDevice = deployment.getDeploymentDevice(existingStorageComponent.getId());
                                if (netappDevice != null && netappDevice.getBrownfieldStatus().isAttachedDeviceAvailable()) {
                                    netappDevice.setBrownfieldStatus(BrownfieldStatus.AVAILABLE);
                                }
                            }
                        }
                    }
                }
            }
        } 
           
        return storageCount;
    }
   
    // Sets the components name and the brownfield status for the deployment deivce 
    private int setComponentNameAndDeploymentDeviceBrownfieldStatus(ServiceTemplateComponent storageComponent, 
                                                                    DeviceInventoryEntity storageEntity, 
                                                                    DeploymentDevice storageDeploymentDevice, 
                                                                    BrownfieldStatus serverBrownfieldStatus, 
                                                                    int storageCount) {
        
        if(BrownfieldStatus.UNAVAILABLE_NOT_MANAGED_OR_RESERVED.equals(serverBrownfieldStatus) && !storageEntity.getState().isAvailable()) {
            storageComponent.setName(UNAVAILABLE_STORAGE + storageCount++);
            storageDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED);
        }
        else if (BrownfieldStatus.UNAVAILABLE_IN_EXISTING_SERVICE.equals(serverBrownfieldStatus) && storageEntity.getState().isAvailable()) {
            storageComponent.setName(UNAVAILABLE_STORAGE + storageCount++);
            storageDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_RELATED_SERVER_IN_EXISTING_SERVICE);
        }
        else if (BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY.equals(serverBrownfieldStatus) && storageEntity.getState().isAvailable()) {
            storageComponent.setName(UNAVAILABLE_STORAGE + storageCount++);
            storageDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_RELATED_SERVER_NOT_IN_INVENTORY);
        }
        else if(BrownfieldStatus.UNAVAILABLE_NOT_MANAGED_OR_RESERVED.equals(serverBrownfieldStatus) && storageEntity.getState().isAvailable()) {
            storageComponent.setName(UNAVAILABLE_STORAGE + storageCount++);
            storageDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_RELATED_SERVER_NOT_MANAGED_OR_RESERVED);
        }
        else if(!storageEntity.getState().isAvailable() && BrownfieldStatus.UNAVAILABLE_NOT_IN_INVENTORY.equals(serverBrownfieldStatus)) {
            storageComponent.setName(UNAVAILABLE_STORAGE + storageCount++);
            storageDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_MANAGED_OR_RESERVED_AND_RELATED_SERVER_NOT_IN_INVENTORY);
        }
        else if(!storageEntity.getState().isAvailable() && BrownfieldStatus.UNAVAILABLE_IN_EXISTING_SERVICE.equals(serverBrownfieldStatus)) {
            storageComponent.setName(UNAVAILABLE_STORAGE + storageCount++);
            storageDeploymentDevice.setBrownfieldStatus(BrownfieldStatus.UNAVAILABLE_THIS_DEVICE_NOT_MANAGED_OR_RESERVED_AND_RELATED_SERVER_IN_EXISTING_SERVICE);
        }
        else {
            storageComponent.setName(STORAGE + storageCount++);
        }
        
        return storageCount;
    }
    
    // Returns the matching component for the Storage ServiceTemplateComponent or null if no match can be found
    private ServiceTemplateComponent findExistingStorageComponent(ServiceTemplate serviceTemplate, ServiceTemplateComponent storageTemplateComponent) {
        ServiceTemplateComponent matchingComponent = null;

        if (storageTemplateComponent.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID) != null) {
            // Process Equallogic
            String iscsiIqn = storageTemplateComponent.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN).getValue();
            matchingComponent = serviceTemplate.findComponentBySettingAndValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, iscsiIqn);
        }
        else if (storageTemplateComponent.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID) != null) {
            // Process Compellent
            String scsiIqn = storageTemplateComponent.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN).getValue();            
            matchingComponent = serviceTemplate.findComponentBySettingAndValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, scsiIqn);
        }
        else if (storageTemplateComponent.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID) != null) {
            // Process Netapp
            String volumeName = storageTemplateComponent.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
            matchingComponent = serviceTemplate.findComponentBySettingAndValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, storageTemplateComponent.getAsmGUID(), ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, volumeName);
        }
        
        return matchingComponent;
    }
    
    private String formatWithNoDecimals(double capacity) {
        String formattedCapacity = null;
            
        if(capacity > 10000) {
            DecimalFormat df = new DecimalFormat("#"); 
            formattedCapacity = df.format(capacity/1000) + "TB";
        }
        else if (capacity < 10) {
            DecimalFormat df = new DecimalFormat("#"); 
            formattedCapacity = df.format(capacity*1000) + "MB";
        }
        else {
            DecimalFormat df = new DecimalFormat("#"); 
            formattedCapacity = df.format(capacity) + "GB";
        }
        
        return formattedCapacity;
    }
    
}
