/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentNamesType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentValid;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeploymentNamesRefDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentNamesRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.util.brownfield.BrownfieldUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;

public class DeploymentValidator {


    private static final Logger logger = Logger.getLogger(DeploymentValidator.class);

    private static DeploymentValidator INSTANCE = null;

    private DeploymentNamesRefDAO deploymentNamesRefDAO;

    private FirmwareUtil firmwareUtil;

    /**
     * Default Constructor for the class.
     */
    private DeploymentValidator() {
    }

    public static synchronized DeploymentValidator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeploymentValidator();
        }
        return INSTANCE;
    }

    public synchronized DeploymentValid validateDeployment(Deployment deployment, boolean updatingDeployment) {

        //NOTE: Purpose of this is to limit the amount of looping through the deployments service template
        // Map saving components by their Ids
        Map<String, ServiceTemplateComponent> componentsMap = new HashMap<>();
        // Map saving component ids based on the component type
        Map<ServiceTemplateComponentType,Set<String>> componentTypeMap = new HashMap<>();
        // loop through components building the two maps
        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
            componentsMap.put(component.getId(), component);
            addComponentTypeToMap(componentTypeMap, component.getType(), component.getId());
        }

        DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();

        checkForDefaultFirmwareCatalog(deploymentValid, deployment);
        checkDuplicateOSHostNames(deploymentValid, componentsMap, componentTypeMap, deployment);
        checkDuplicateVMNames(deploymentValid, componentsMap, componentTypeMap, deployment);
        checkDuplicateVolumeNames(deploymentValid, componentsMap, componentTypeMap, deployment);
        if (!updatingDeployment) {
            checkScheduledDate(deploymentValid, deployment);
            validateStorageComponent(deploymentValid, componentsMap, componentTypeMap, deployment.isBrownfield());
            checkForMultipleDeployments(deploymentValid, componentsMap, componentTypeMap, deployment);
        }
        return deploymentValid;
    }

    private void checkForDefaultFirmwareCatalog(DeploymentValid deploymentValid, Deployment deployment) {
        if (deployment != null) {
            if (deployment.isUpdateServerFirmware() && deployment.isUseDefaultCatalog()) {
                FirmwareRepositoryEntity firmwareRepositoryEntity = getFirmwareUtil().getDefaultRepo();
                if (firmwareRepositoryEntity == null) {
                    logger.error("A Default Firmware Repository Catalog must be set in order to use the Default Catalog in a Deployment");
                    deploymentValid.setValid(false);
                    deploymentValid.addMessage(AsmManagerMessages.needDefaultFirmwareCatalog());
                }
            }
        }
    }

    private void checkDuplicateOSHostNames(DeploymentValid deploymentValid, Map<String, ServiceTemplateComponent> componentMap, Map<ServiceTemplateComponentType, Set<String>> componentTypeMap, Deployment deployment) {
        Set<String> duplicates = new HashSet<>();
        Set<String> currentHostNames = new HashSet<>();
        Set<String> componentIds = componentTypeMap.get(ServiceTemplateComponentType.SERVER);
        if (componentIds != null && componentIds.size() > 0) {
            for (String componentId : componentIds) {
                ServiceTemplateComponent component = componentMap.get(componentId);
                if (component != null) {
                    ServiceTemplateSetting osHostNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                    if (osHostNameSetting != null && StringUtils.isNotBlank(osHostNameSetting.getValue()) && !BrownfieldUtil.NOT_FOUND.equals(osHostNameSetting.getValue())) {
                        if (!currentHostNames.add(osHostNameSetting.getValue())) {
                            // keep track of duplicates
                            duplicates.add(osHostNameSetting.getValue());
                        }
                    }
                }
            }
        }

        List<DeploymentNamesRefEntity> entities = getDeploymentNamesRefDAO().getAllDeploymentNamesRefsByType(DeploymentNamesType.OS_HOST_NAME);
        if (entities != null && entities.size() > 0) {
            String thisDeploymentId = deployment.getId();
            for (DeploymentNamesRefEntity entity : entities) {
                if (entity.getDeploymentId() != null && !entity.getDeploymentId().equals(thisDeploymentId)) {
                    if (currentHostNames.contains(entity.getName())) {
                        duplicates.add(entity.getName());
                    }
                }
            }
        }
        if (!duplicates.isEmpty()) {
            logger.error("Duplicate hostnames found for Deployment " + deployment.getDeploymentName() + ". Found " + duplicates.size() + " number of duplicate names.");
            deploymentValid.setValid(false);
            deploymentValid.addMessage(AsmManagerMessages.duplicateHostname(duplicates.toString()));
        }
    }

    private void checkDuplicateVMNames(DeploymentValid deploymentValid, Map<String, ServiceTemplateComponent> componentMap, Map<ServiceTemplateComponentType, Set<String>> componentTypeMap, Deployment deployment) {
        Set<String> duplicates = new HashSet<>();
        Set<String> currentVMNames = new HashSet<>();
        Set<String> componentIds = componentTypeMap.get(ServiceTemplateComponentType.VIRTUALMACHINE);
        if (componentIds != null && componentIds.size() > 0) {
            for (String componentId : componentIds) {
                ServiceTemplateComponent component = componentMap.get(componentId);
                if (component != null) {
                    ServiceTemplateSetting vmNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
                    if (vmNameSetting != null && StringUtils.isNotBlank(vmNameSetting.getValue())) {
                        // keep track of duplicates
                        if (!currentVMNames.add(vmNameSetting.getValue())) {
                            duplicates.add(vmNameSetting.getValue());
                        }
                    }
                    vmNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                    if (vmNameSetting != null && StringUtils.isNotBlank(vmNameSetting.getValue())) {
                        // keep track of duplicates
                        if (!currentVMNames.add(vmNameSetting.getValue())) {
                            duplicates.add(vmNameSetting.getValue());
                        }
                    }
                }
            }
        }

        List<DeploymentNamesRefEntity> entities =  getDeploymentNamesRefDAO().getAllDeploymentNamesRefsByType(DeploymentNamesType.VM_NAME);
        if (entities != null && entities.size() > 0) {
            String thisDeploymentId = deployment.getId();
            for (DeploymentNamesRefEntity entity : entities) {
                if (entity.getDeploymentId() != null && !entity.getDeploymentId().equals(thisDeploymentId)) {
                    if (currentVMNames.contains(entity.getName())) {
                        duplicates.add(entity.getName());
                    }
                }
            }
        }
        if (!duplicates.isEmpty()) {
            logger.error("Duplicate vm names found for Deployment " + deployment.getDeploymentName() + ". Found " + duplicates.size() + " number of duplicate names.");
            deploymentValid.setValid(false);
            deploymentValid.addMessage(AsmManagerMessages.duplicateVMNameDeployed(duplicates.toString()));
        }

    }

    private void checkDuplicateVolumeNames(DeploymentValid deploymentValid, Map<String, ServiceTemplateComponent> componentMap, Map<ServiceTemplateComponentType, Set<String>> componentTypeMap, Deployment deployment) {
        Set<String> duplicates = new HashSet<>();
        Set<String> currentVolumeNames = new HashSet<>();
        Set<String> componentIds = componentTypeMap.get(ServiceTemplateComponentType.STORAGE);
        if (componentIds != null && componentIds.size() > 0) {
            for (String componentId : componentIds) {
                ServiceTemplateComponent component = componentMap.get(componentId);
                if (component != null) {
                    for (ServiceTemplateCategory category : component.getResources()) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_RESOURCE_LIST.contains(category.getId())) {
                            findVolumeNames(currentVolumeNames, duplicates, category, deployment.isScaleUp());
                        }
                    }
                }
            }
        }
        List<DeploymentNamesRefEntity> entities =  getDeploymentNamesRefDAO().getAllDeploymentNamesRefsByType(DeploymentNamesType.STORAGE_VOLUME_NAME);
        if (entities != null && entities.size() > 0) {
            String thisDeploymentId = deployment.getId();
            for (DeploymentNamesRefEntity entity : entities) {
                if (entity.getDeploymentId() != null && !entity.getDeploymentId().equals(thisDeploymentId)) {
                    if (currentVolumeNames.contains(entity.getName())) {
                        duplicates.add(entity.getName());
                    }
                }
            }
        }
        if (!duplicates.isEmpty()) {
            logger.error("Duplicate volume names found for deployment " + deployment.getId() + ". Found " + duplicates.size() + " number of duplicate names.");
            deploymentValid.setValid(false);
            deploymentValid.addMessage(AsmManagerMessages.duplicateVolumeName(duplicates.iterator().next()));
        }
    }

    private void checkScheduledDate(DeploymentValid deploymentValid, Deployment deployment) {
        // check for schedule date
        Date scheduleDate = deployment.getScheduleDate();
        if (scheduleDate != null) {
            Date now = new Date();
            if (now.after(scheduleDate)) {
                logger.error("Requested schedule date of " + scheduleDate
                        + " is before current date of " + now + " for Deployment " + deployment.getDeploymentName());
                deploymentValid.setValid(false);
                deploymentValid.addMessage(AsmManagerMessages.scheduleDateIsPast());
            }
        }
    }

    private void validateStorageComponent(DeploymentValid deploymentValid, 
                                          Map<String, ServiceTemplateComponent> componentMap, 
                                          Map<ServiceTemplateComponentType, 
                                          Set<String>> componentTypeMap,
                                          boolean isBrownfield) {
        Set<String> componentIds = componentTypeMap.get(ServiceTemplateComponentType.STORAGE);
        if (componentIds != null && componentIds.size() > 0) {
            for (String componentId : componentIds) {
                ServiceTemplateComponent component = componentMap.get(componentId);
                if (component != null) {
                    Set<ServiceTemplateComponent> serverComponents = new HashSet<>();
                    if (component.getAssociatedComponents() != null && component.getAssociatedComponents().size() > 0) {
                        for (String associatedComponentId : component.getAssociatedComponents().keySet()) {
                            ServiceTemplateComponent associatedComponent = componentMap.get(associatedComponentId);
                            if (associatedComponent != null && ServiceTemplateComponentType.SERVER.equals(associatedComponent.getType())) {
                                serverComponents.add(associatedComponent);
                            }
                        }
                    }

                    // case: no server, IQN auth. IQN is required
                    // Equallogic only
                    if (component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID) != null
                            && serverComponents.isEmpty()) {
                        String val = null;
                        ServiceTemplateSetting valSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                                                                                   ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID);
                        if (valSetting != null) {
                            val = valSetting.getValue();
                        }

                        ServiceTemplateSetting volumeTitleSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                                                                                           ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                        boolean newVolume = ServiceTemplateClientUtil.isNewStorageVolume(volumeTitleSetting, true);

                        String iqn = null;
                        ServiceTemplateSetting iqnSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                                                                                   ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_IQNORIP_ID);
                        if (iqnSetting != null) {
                            iqn = iqnSetting.getValue();
                        }
                        if (StringUtils.isNotEmpty(val)
                                && newVolume
                                && val.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_IQNIP_ID)
                                && StringUtils.isEmpty(iqn)) {

                            logger.error("No IQN/IP in EQL storage " + component.getName() + " with IQN auth type and templates has no related server component.");
                            deploymentValid.setValid(false);
                            deploymentValid.addMessage(AsmManagerMessages.noIQNorIP());
                        }
                    }

                    // validate fc volume has associated fc interfaces for associated servers
                    if (!isBrownfield) {
                        boolean fcValidation = false;
                        String volumeName = null;
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID.equals(component.getComponentID())) {
                            ServiceTemplateSetting portTypeSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID,
                                                                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID);
                            if (portTypeSetting != null &&
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_FIBRE_CHANNEL.equals(portTypeSetting.getValue())) {
                                fcValidation = true;
                                volumeName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(component.getTemplateResource(
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID));

                            }
                        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VNX_COMP_ID.equals(component.getComponentID())) {
                            fcValidation = true;
                            volumeName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(component.getTemplateResource(
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID));
                        }
                        if (fcValidation) {
                            boolean missing = false;
                            if (!serverComponents.isEmpty()) {
                                for (ServiceTemplateComponent server : serverComponents) {
                                    boolean found = false;
                                    ServiceTemplateSetting networkingSetting = server.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID,
                                                                                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                                    if (networkingSetting != null && StringUtils.isNotBlank(networkingSetting.getValue())) {
                                        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.deserializeNetwork(networkingSetting.getValue());
                                        if (networkConfiguration != null) {
                                            if (networkConfiguration.getInterfaces() != null && networkConfiguration.getInterfaces().size() > 0) {
                                                for (Fabric fabric : networkConfiguration.getInterfaces()) {
                                                    if (Fabric.FC_TYPE.equals(fabric.getFabrictype())) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!found) {
                                        missing = true;
                                        break;
                                    }
                                }
                            }
                            if (missing) {
                                logger.error("Fibre Channel Interface was not found on each Server attached to Fibre Channel volume " + volumeName + ".");
                                deploymentValid.setValid(false);
                                deploymentValid.addMessage(AsmManagerMessages.fibreChannelInterfaceRequired(volumeName));
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkForMultipleDeployments(DeploymentValid deploymentValid, Map<String, ServiceTemplateComponent> componentMap,  Map<ServiceTemplateComponentType, Set<String>> componentTypeMap, Deployment deployment) {
        // check if there are non-server components
        if (deployment.getNumberOfDeployments() > 1) {
            if (componentTypeMap != null && componentTypeMap.size() > 0) {
                for (ServiceTemplateComponentType type : componentTypeMap.keySet()) {
                    if (!ServiceTemplateComponentType.SERVER.equals(type)) {
                        logger.error("Non-server component detected for number of deployments > 1: " + deployment.getNumberOfDeployments());
                        deploymentValid.setValid(false);
                        deploymentValid.addMessage(AsmManagerMessages.nonServerComponentsDetected());
                        break;
                    }
                }
            }
            Set<String> componentIds = componentTypeMap.get(ServiceTemplateComponentType.SERVER);
            if (componentIds != null) {
                for (String componentId : componentIds) {
                    ServiceTemplateComponent component = componentMap.get(componentId);
                    if (component != null) {
                        ServiceTemplateSetting sourceSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE,
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
                        if (sourceSetting == null) {
                            sourceSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
                        }
                        if (sourceSetting != null &&
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL.equals(sourceSetting.getValue())) {
                            logger.error("For multiple deployments server source should not be Manual");
                            deploymentValid.setValid(false);
                            deploymentValid.addMessage(AsmManagerMessages.manualServerError());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addComponentTypeToMap(Map<ServiceTemplateComponentType,Set<String>> componentTypesMap,ServiceTemplateComponentType type, String id) {
        if (componentTypesMap != null && type != null && id != null) {
            Set<String> ids = componentTypesMap.get(type);
            if (ids == null) {
                ids = new HashSet<>();
                componentTypesMap.put(type,ids);
            }
            ids.add(id);
        }
    }

    /**
     * For scale up case get all volume names from template.
     * For non-scale up (new deployment) get only "new" volumes, including those generated.
     * @param currentVolumeNames
     * @param duplicates
     * @param category
     * @param isScaleUp
     */
    private void findVolumeNames(Set<String> currentVolumeNames, Set<String> duplicates,
                                 final ServiceTemplateCategory category, boolean isScaleUp) {
        if (category != null && (isScaleUp || ServiceTemplateClientUtil.isNewStorageVolume(category, true))) {
            String volumeName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(category);
            if (StringUtils.isNotBlank(volumeName)) {
                if (!currentVolumeNames.add(volumeName)) {
                    duplicates.add(volumeName);
                }
            }
        }
    }

    public DeploymentNamesRefDAO getDeploymentNamesRefDAO() {
        if (deploymentNamesRefDAO == null) {
            deploymentNamesRefDAO = new DeploymentNamesRefDAO();
        }
        return deploymentNamesRefDAO;
    }

    /*
            For Testing Purposes Only
         */
    public void setDeploymentNamesRefDAO(DeploymentNamesRefDAO deploymentNamesRefDAO) {
        this.deploymentNamesRefDAO = deploymentNamesRefDAO;
    }

    public FirmwareUtil getFirmwareUtil() {
        if (firmwareUtil == null) {
            firmwareUtil = new FirmwareUtil();
        }
        return firmwareUtil;
    }

    public void setFirmwareUtil(FirmwareUtil firmwareUtil) {
        this.firmwareUtil = firmwareUtil;
    }
}

