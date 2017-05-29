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

import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.Network;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringEscapeUtils;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateValid;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.VirtualDiskConfiguration;
import com.dell.asm.asmcore.asmmanager.util.deployment.HostnameUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.NetworkingUtil;
import com.dell.asm.common.utilities.Validator;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.i18n2.exception.AsmValidationException;
import com.dell.asm.server.client.policy.RaidLevel;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.orion.common.utilities.MarshalUtil;


public class ServiceTemplateValidator {

    private INetworkService networkService;
    private ServiceTemplateUtil serviceTemplateUtil;
    private OSRepositoryUtil osRepositoryUtil;

    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateValidator.class);
    private static final String ERROR_DUPLICATE_NETWORKS = "networksDuplicate";
    private static final String ERROR_DUPLICATE_NETWORK_TYPE = "networkTypeDuplicate";
    private static final String ERROR_WORKLOAD_NETS_NOT_SAME = "workloadNetworksNotSame";

    /**
     * Default constructor. Don't init variables here - use getters/setters!
     */
    public ServiceTemplateValidator() {
    }

    /**
     * Validate all components.
     * @param svcTemplate Template to validate
     * @param options Options: deployment time, check for unique names, EM presence
     */
    public void validateTemplate(ServiceTemplate svcTemplate, ValidationOptions options) {
        validateTemplate(svcTemplate, options, null);
    }

    /**
     * Validate all components.
     * @param svcTemplate Template to validate
     * @param options Options: deployment time, check for unique names, EM presence
     * @param existingComponents existing components on scale up call. Used by storage volume validation
     */
    public void validateTemplate(ServiceTemplate svcTemplate, ValidationOptions options, List<String> existingComponents) {
        if (svcTemplate == null) {
            return;
        }
        // clear/reset template and component status to default - We assume innocent until proven guilty :)
        clearAllServiceTemplateValidations(svcTemplate);

        String tempXml = MarshalUtil.marshal(svcTemplate);
        ServiceTemplate templateToValidate = MarshalUtil.unmarshal(ServiceTemplate.class, tempXml);

        List<ServiceTemplateComponent> components = templateToValidate.getComponents();
        for (ServiceTemplateComponent component : components) {
            if (component.getComponentID()!=null){
                List<ServiceTemplateCategory> resources = component.getResources();
                for (ServiceTemplateCategory resource : resources){
                    if (resource.getId()!=null){
                        List<ServiceTemplateSetting> parameters = resource.getParameters();
                        Iterator<ServiceTemplateSetting> iterator = parameters.iterator();
                        while (iterator.hasNext()) {
                            ServiceTemplateSetting next = iterator.next();
                            if(!getServiceTemplateUtil().requiredDependenciesSatisfied(component,next)){
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }

        // only validate the next level if previous validation passes
        // we may want to change this eventually but leave as is for now since this is how previous validation worked
        // and then throwing web exceptions to exit the validation 
        // we are now deferring the exceptions to the caller but want validation to execute in the same manner as before
        validateTemplateFields(templateToValidate);

        //retrieve the repos to task map once for all validation
        final Map<String, String> repoToTaskMap = getServiceTemplateUtil().mapReposToTasks();

        if (templateToValidate.getTemplateValid().isValid()) {
            validateRequiredParameters(templateToValidate);
            validateStorageComponentFields(templateToValidate,options.isInventoryContainsEM(),
                    options.isCheckForUniqueness(), svcTemplate, existingComponents);
            validateStorageAuthorizationMatchesServer(templateToValidate);
            validatePasswords(templateToValidate);
            validateClusters(templateToValidate);
            validateServerComponents(templateToValidate, repoToTaskMap);
            validateComponentDependencies(templateToValidate, repoToTaskMap);
            validateVMs(templateToValidate, options.isDeployment());
        }
        copyAllServiceTemplateValidations(templateToValidate, svcTemplate);
    }

    public void validateTemplateFields(final ServiceTemplate svcTemplate) {
        final ServiceTemplateValid serviceTemplateValid = svcTemplate.getTemplateValid();

        // validate template name
        String templateName = svcTemplate.getTemplateName();
        if (GenericValidator.isBlankOrNull(templateName)) {
            serviceTemplateValid.addMessage(AsmManagerMessages.InvalidTemplateName(templateName));
        } else {
            // TODO[fcarta] find out if we need this doesnt look like it really does anything
            templateName = templateName.trim();
            Validator.isLocalisedTemplateNameValid(templateName);
        }
        if (!GenericValidator.isInRange(templateName.length(), Validator.NAME_MIN_SIZE, Validator.NAME_MAX_SIZE)) {
            serviceTemplateValid.addMessage(AsmManagerMessages.InvalidTemplateNameLength(templateName));
        }
        svcTemplate.setTemplateName(templateName.trim());
        
        // validate template description
        String description = svcTemplate.getTemplateDescription();
        try {
            Validator.validateDescription(description);
        } catch (AsmValidationException ex) {
            serviceTemplateValid.addMessage(ex.getEEMILocalizableMessage());
        }
        
        // if there are any validation error messages then template is invalid
        if (CollectionUtils.isNotEmpty(serviceTemplateValid.getMessages())) {
            serviceTemplateValid.setValid(Boolean.FALSE);
        }        
    }

    /**
     * Validates the storage components within a ServiceTemplate.
     * 
     * @param svcTemplate the service template to be validated, with non-required options removed
     * @param inventoryContainsEM
     * @param checkForUniqueVolume true if volume names are to be checked for uniqueness, or false if that
     *      validation is to be skipped.
     * @param originalTemplate the service template to be validated, as it is stored in DB
     * @param existingComponents Skip volume validation for these components
     */
    public void validateStorageComponentFields(ServiceTemplate svcTemplate, 
                                                      boolean inventoryContainsEM,
                                                      boolean checkForUniqueVolume,
                                                ServiceTemplate originalTemplate,
                                               List<String> existingComponents) {
        List<String> chapUserNames = new ArrayList<>();
        Boolean isChapValidationExecuted;
        isChapValidationExecuted = false;

        Map<String, Set<String>> uniqueStorageNames = getStorageVolumeMap(originalTemplate);

        List<String> volNames = new ArrayList<>();
        
        for (ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
            boolean validateComponentVolume =
                existingComponents == null || !existingComponents.contains(component.getId());

            if (component.getType() == ServiceTemplateComponentType.STORAGE) {
                final ServiceTemplateValid componentValid = component.getComponentValid();
                final ServiceTemplateSetting titleSet = 
                        svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                
                if (titleSet==null || StringUtils.isEmpty(titleSet.getValue())) {
                    // bug in UI or controller
                    componentValid.addMessage(AsmManagerMessages.internalError());
                }
                
                boolean isChapEverUsed = false;
                for (ServiceTemplateCategory category : safeList(component.getResources())) {
                    boolean useChap = false;

                    if (validateComponentVolume && checkForUniqueVolume && uniqueStorageNames != null && !uniqueStorageNames.isEmpty()) {
                        String storageName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(category);
                        Set<String> existingStorageVolumes = new HashSet<>();
                        ServiceTemplateSetting targetSetting = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (targetSetting != null) {
                            String targetName = targetSetting.getValue();
                            if (targetName != null) {
                                existingStorageVolumes = uniqueStorageNames.get(targetName);
                            }
                        }
                        // Verify the storagename is unique
                        if (ServiceTemplateClientUtil.isNewStorageVolume(category, false)) {
                            if (StringUtils.isNotEmpty(storageName)) {
                                if(!existingStorageVolumes.add(storageName)) {
                                    duplicateVolNamesError(componentValid, storageName);
                                }
                            }
                        } else if (ServiceTemplateClientUtil.isExistingVolume(category)) {
                            // for existing volumes check those still exists
                            if (!existingStorageVolumes.contains(storageName)) {
                                LOGGER.error("Volume name do not exists: " + storageName);
                                componentValid.addMessage(AsmManagerMessages.nonexistentVolumeName(storageName));
                                break;
                            }
                        }
                    }

                    for (ServiceTemplateSetting setting : safeList(category.getParameters())) {

                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equalsIgnoreCase(setting.getId())) {
                            String volumeNameInTemplate = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(category);
                            // if name should be generated, emulate it now
                            if (StringUtils.isEmpty(volumeNameInTemplate) &&
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(setting.getValue())) {
                                volumeNameInTemplate = HostnameUtil.generateNameFromNumTemplate(
                                        category.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_TEMPLATE).getValue(),new HashSet<String>());
                            }
                            // skip dup test for autogen storage volume
                            if (StringUtils.isNotEmpty(volumeNameInTemplate) &&
                                    ServiceTemplateClientUtil.isNewStorageVolume(setting, false)) {
                                volNames.add(volumeNameInTemplate);
                                Set<String> volNameSet = new HashSet<>(volNames);
                                if (volNames.size() != 0
                                        && (volNameSet.size() != volNames.size())) {
                                    duplicateVolNamesError(componentValid, volumeNameInTemplate);
                                }
                            }

                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID.equals(category.getId())) {
                                // volume name may be empty only if it is publish time and option is
                                // "create at deployment". We ignore it. We check all other cases.
                                if(StringUtils.isNotEmpty(volumeNameInTemplate) && ServiceTemplateClientUtil.isNewStorageVolume(setting, true)
                                        && !isEqlVolNameValid(volumeNameInTemplate)) {
                                    LOGGER.error("Invalid volume name " + volumeNameInTemplate);
                                    componentValid.addMessage(AsmManagerMessages.invalidEqlVolName(StringEscapeUtils.escapeHtml4(volumeNameInTemplate)));
                                    break;
                                }
                            }                           
                            
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID.equals(category.getId())) {                            	
                                List<ServiceTemplateSetting> params = category.getParameters();
                                for (ServiceTemplateSetting param : params) {
                                    if(param.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID) 
                                            && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ISCSI.equalsIgnoreCase(param.getValue())
                                            && !inventoryContainsEM) {
                                        LOGGER.error("No EM in inventory while CMPL exists in Template");
                                        componentValid.addMessage(AsmManagerMessages.noEmInInventory());
                                        break;
                                    }

                                    Map<String, ServiceTemplateComponent> map = svcTemplate.fetchComponentsMap();
                                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_BOOT_VOLUME_ID.equalsIgnoreCase(param.getId())){
                                        for (String key : component.getAssociatedComponents().keySet()){
                                            if (map.get(key) != null && map.get(key).getType() == ServiceTemplateComponentType.SERVER){
                                                for (ServiceTemplateCategory serverCategory : safeList(svcTemplate.findComponentById(key).getResources())) {
                                                    for (ServiceTemplateSetting serverParam : safeList(serverCategory.getParameters())) {
                                                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID.equals(serverParam.getId())) {                                                                                                                    
                                                            if (StringUtils.equals(param.getValue(), "true")
                                                                    && !(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI.equals(serverParam.getValue())
                                                                            || ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC.equals(serverParam.getValue()))) {
                                                                LOGGER.error("For a storage with Boot Volume option checked - the Target Boot Device of associated server component should be either Boot from SAN (FC) or Boot from SAN (iSCSI)");
                                                                componentValid.addMessage(AsmManagerMessages.bootVolumeChecked());
                                                            } else if (StringUtils.equals(param.getValue(), "false")
                                                                    && (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI.equals(serverParam.getValue())
                                                                            || ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC.equals(serverParam.getValue()))) {
                                                                LOGGER.error("For a storage with Boot Volume option not checked - the Target Boot Device of associated server component can not be Boot from SAN (FC) or Boot from SAN (iSCSI)");
                                                                componentValid.addMessage(AsmManagerMessages.bootVolumeNotChecked());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID.equalsIgnoreCase(param.getId())
                                            && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_FIBRE_CHANNEL.equalsIgnoreCase(param.getValue())){                                                                                
                                        if (!isFcStorageWithFcServer(component, svcTemplate)
                                        		&& component.getAssociatedComponents().size() > 0){
                                            LOGGER.error("Server component attached with FC Storage component must have FC interface...");
                                            componentValid.addMessage(AsmManagerMessages.fcStorageWithServer());
                                            break;
                                        }
                                    }
                                }                                                                                                                                                  
                            }
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID.equals(category.getId())) {
                                if (!isFcStorageWithFcServer(component, svcTemplate)
                                		&& component.getAssociatedComponents().size() > 0){
                                    LOGGER.error("Server component attached with FC Storage component must have FC interface...");
                                    componentValid.addMessage(AsmManagerMessages.fcStorageWithServer());
                                    break;
                                }                            
                            }
                        } else if (setting.getId().equalsIgnoreCase("size")
                                && ServiceTemplateClientUtil.isNewStorageVolume(titleSet, true)) {
                            if (category.getId().contains("compellent")) {
                                if (!Validator.isCompellentVolumeSizeValid(setting.getValue())) {
                                    LOGGER.error("Invalid volume size " + setting.getValue());
                                    componentValid.addMessage(
                                            AsmManagerMessages.invalidVolumeSizeCmpl(setting.getValue()));
                                }
                            } else if (category.getId().contains("equallogic") ||
                                    category.getId().contains("netapp") ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID.equals(category.getId())) {
                                if (!Validator.isEquallogicVolumeSizeValid(setting.getValue())) {
                                    LOGGER.error("Invalid volume size " + setting.getValue());
                                    componentValid.addMessage(
                                            AsmManagerMessages.invalidVolumeSizeEql(setting.getValue()));
                                }
                            }
                        } else if (setting.getId().equalsIgnoreCase("snapreserve")
                                || setting.getId().equalsIgnoreCase("thinminreserve")
                                        || setting.getId().equalsIgnoreCase("thingrowthwarn")
                                                || setting.getId().equalsIgnoreCase("thingrowthmax")
                                                        || setting.getId().equalsIgnoreCase("thinwarnsoftthres")
                                                                || setting.getId().equalsIgnoreCase("thinwarnhardthres")) {
                            if (!Validator.isValidPercentage(setting.getValue())) {
                                LOGGER.error("Invalid percentage value" + setting.getValue());
                                componentValid.addMessage(
                                        AsmManagerMessages.invalidPercentageFormat(setting.getValue()));
                            }
                        } else if (setting.getId().equalsIgnoreCase(
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID)) {
                                if (setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_CHAP_ID)) {
                                    useChap = true;
                                    isChapEverUsed = true;
                                }
                        } else if (useChap && setting.getId().equalsIgnoreCase("chap_user_name")) {
                            chapUserNames.add(setting.getValue());
                            if (!Validator.isValidCHAPUsername(setting.getValue())) {
                                LOGGER.error("Invalid chap username" + setting.getValue());
                                componentValid.addMessage(
                                        AsmManagerMessages.invalidCHAPUserName(setting.getValue()));
                            }
                            Set<String> chapUserNameSet = new HashSet<>(chapUserNames);
                            if (chapUserNameSet.size() > 1 && !isChapValidationExecuted) {
                                isChapValidationExecuted = true;
                                chapUserNameError(componentValid);
                            }
                        } else if (useChap && setting.getId().equalsIgnoreCase("passwd")) {
                            if (!Validator.isValidCHAPPassword(setting.getValue())) {
                                LOGGER.error("Invalid chap password" + setting.getValue());
                                componentValid.addMessage(AsmManagerMessages.invalidCHAPPassword());
                            }
                        } else if (setting.getId().equalsIgnoreCase("volumefolder")     
                                || setting.getId().equalsIgnoreCase("serverfolder")) {
                            if (!Validator.isValidFolderName(setting.getValue())) {
                                LOGGER.error("Invalid folder name" + setting.getValue());
                                componentValid.addMessage(
                                        AsmManagerMessages.invalidFolderName(setting.getValue()));
                            }
                        } else if (setting.getId().equalsIgnoreCase("wwn")) {
                            if (!Validator.isValidServerWWN(setting.getValue())) {
                                LOGGER.error("Invalid server wwn" + setting.getValue());
                                componentValid.addMessage(
                                        AsmManagerMessages.invalidServerWWN(setting.getValue()));
                            }
                        } else if (setting.getId().equalsIgnoreCase("iqnOrIP") && 
                                StringUtils.isNotEmpty(setting.getValue())) {
                            String[] vals;
                            if (setting.getValue().indexOf(',')>0) {
                                vals = setting.getValue().split(",");
                            } else {
                                vals = new String[1];
                                vals[0] = setting.getValue();
                            }
                            for (String s: vals) {
                                if (!Validator.isValidIPAddressWildcard(s) && !Validator.isValidIQN(s)) {
                                    LOGGER.error("Invalid server iqnOrIP: " + s);
                                    componentValid.addMessage(AsmManagerMessages.invalidServerIPorIQN(s));
                                }
                            }
                        } else if (setting.getId().equalsIgnoreCase(
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NFS_NETWORK_ID) && 
                                    StringUtils.isNotEmpty(setting.getValue())) {
                            if (!Validator.isValidIPAddress(setting.getValue())) {
                                LOGGER.error("Invalid NFS/CIFS IP: " + setting.getValue());
                                componentValid.addMessage(
                                        AsmManagerMessages.invalidIPforNFS(setting.getValue()));
                            }
                        }else if ((setting.getId().equalsIgnoreCase("volume_notes")
                                || setting.getId().equalsIgnoreCase("server_notes"))&&
                                StringUtils.isNotEmpty(setting.getValue())) {
                            if (!Validator.isLocalisedDisplayNameValid(setting.getValue())) {
                                LOGGER.error("Invalid notes " + setting.getValue());
                                componentValid.addMessage(
                                        AsmManagerMessages.invalidNotes(setting.getValue()));
                            }
                        }
                    }
                }
                
                // Validate ICQN/IP vs Chap Settings for related Servers
                final Set<String> relCompsKeys = component.getAssociatedComponents().keySet();
                if (relCompsKeys.size() > 0) {
                    // map to keep count of the number of times a server is added to the same cluster
                    // used to make sure a server is not added to the same cluster more than once
                    for (String key: relCompsKeys) {
                        final ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                        // Loop through the related components and check for any servers that are added twice
                        if (relComp != null && relComp.getType() == ServiceTemplateComponentType.SERVER)
                        {
                        	for (ServiceTemplateCategory category : safeList(relComp.getResources())) {
                                for (ServiceTemplateSetting setting : safeList(category.getParameters())) {
                                    if ((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID) 
                                            && setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI))
                                            && isChapEverUsed) {
                                    	 LOGGER.warn("Invalid use of CHAP on Target Boot Device - Boot from SAN (ISCSI).");
                                         componentValid.addMessage(AsmManagerMessages.invalidAuthenticationForStorage());
                                    }
                                }
                        	}
                        }
                    }
                }
                
                
                // if there are any validation error messages then template is invalid
                if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                }    
            }
        }
    }

	/**
     * Create a map of ASMGUID to volumes for each storage in template
     * @param template
     * @return
     */
    private Map<String, Set<String>> getStorageVolumeMap(ServiceTemplate template) {
        Map<String, Set<String>> uniqueStorageNames = new HashMap<>();
        for (ServiceTemplateComponent component : safeList(template.getComponents())) {
            if (component.getType() == ServiceTemplateComponentType.STORAGE) {
                for (ServiceTemplateCategory category : safeList(component.getResources())) {
                    ServiceTemplateSetting targetSetting = template.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    if (targetSetting != null) {
                        String targetName = targetSetting.getValue();
                        Set<String> storageNames = getServiceTemplateUtil().getVolumeSet(category);
                        if (storageNames != null) {
                            uniqueStorageNames.put(targetName, storageNames);
                        }
                    }
                }
            }
        }
        return uniqueStorageNames;
    }

    boolean isFcStorageWithFcServer(ServiceTemplateComponent component, ServiceTemplate svcTemplate) {
        Map<String, ServiceTemplateComponent> map = svcTemplate.fetchComponentsMap();
        for (String key : component.getAssociatedComponents().keySet()) {
            if (map.get(key) != null && map.get(key).getType() == ServiceTemplateComponentType.SERVER) {
                com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration network_config = getServiceTemplateUtil().deserializeNetwork(svcTemplate.findComponentById(key).getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID));
                if (network_config != null) {
                    for (Fabric each_interface : network_config.getInterfaces()) {
                        if (Fabric.FC_TYPE.equalsIgnoreCase(each_interface.getFabrictype())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
  
    boolean isEqlVolNameValid(String volumeNameInTemplate) {
        if(!Validator.isValidEqlVolumeName(volumeNameInTemplate) || !Validator.isValidEqlVolumeNameSize(volumeNameInTemplate)) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    private static void chapUserNameError(ServiceTemplateValid componentValid) {
        LOGGER.error("Chap usernames are not same");
        componentValid.addMessage(AsmManagerMessages.sameChapUsernames());     
    }
    
    private static void duplicateVolNamesError(ServiceTemplateValid componentValid, String volumeNameInTemplate) {
    	LOGGER.error("Volume name, " + volumeNameInTemplate + ", already exists");
    	componentValid.addMessage(AsmManagerMessages.storageVolumeNameAlreadyExists(volumeNameInTemplate));
    }
    
    public void validateClusters(final ServiceTemplate svcTemplate) {
        // map to keep count of the number of times a particular server is added to any/all clusters
        // used to ensure a server is not added to multiple clusters
        final Map<String,Integer> clusterCount = new HashMap<String,Integer>(); 
        final Map<ServiceTemplateComponent,Integer> intraClusterServerCount = 
                new HashMap<ServiceTemplateComponent,Integer>();
        for (ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
            if (component.getType() == ServiceTemplateComponentType.CLUSTER) {
                final ServiceTemplateValid componentValid = component.getComponentValid();
                // ensure same cluster is not added more than once
                final String clusterKey = generateClusterKey(svcTemplate, component);
                if (!clusterCount.containsKey(clusterKey)) {
                    clusterCount.put(clusterKey, Integer.valueOf(0));
                }
                Integer clusterOccurrence = clusterCount.get(clusterKey);
                clusterCount.put(clusterKey, ++clusterOccurrence);
                if (clusterOccurrence > 1) {
                    componentValid.addMessage(AsmManagerMessages.clusterDuplicate(component.getName()));
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                    continue;
                }

                ServiceTemplateSetting failureToleranceMethodSetting = null;
                ServiceTemplateSetting numberFailuresSet = null;

                for (ServiceTemplateCategory resource : safeList(component.getResources())) {
                    for (ServiceTemplateSetting parameter : safeList(resource.getParameters())) {
                        if (parameter.getId().toLowerCase().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + "name") 
                                && parameter.getValue().toLowerCase().contains(" ") 
                                && resource.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID)) {
                            componentValid.addMessage(AsmManagerMessages.hyperVClusterNamesWithSpaces());
                            componentValid.setValid(Boolean.FALSE);
                            svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID.equals(parameter.getId())
                                && !parameter.isHideFromTemplate()) {
                            failureToleranceMethodSetting = parameter;
                        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID.equals(parameter.getId())
                                && !parameter.isHideFromTemplate()) {
                            numberFailuresSet = parameter;
                        }
                    }
                }

                int numFailuresToTolerate = -1;

                if (failureToleranceMethodSetting != null &&
                        !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT.equals(failureToleranceMethodSetting.getValue()) &&
                        numberFailuresSet != null && numberFailuresSet.getValue() != null &&
                        !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT.equals(numberFailuresSet.getValue())) {
                    try {
                        numFailuresToTolerate = Integer.parseInt(numberFailuresSet.getValue());
                    }catch (NumberFormatException nfe) {
                        LOGGER.error("Invalid value for " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID +
                                " : " + numberFailuresSet.getValue());
                        componentValid.addMessage(AsmManagerMessages.internalError());
                        componentValid.setValid(Boolean.FALSE);
                        svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                    }
                }

                List<ServiceTemplateComponent> storageComponents = getServiceTemplateUtil().getAssociatedStorageComponentsFromCluster(component, svcTemplate);
                String podValue = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID);
                for (ServiceTemplateComponent dsComponent : storageComponents) {
                    ServiceTemplateCategory resource = dsComponent.getResources().get(0);
                    String storageName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(resource);
                        if (storageName != null 
                        		&& !storageName.isEmpty() 
                        		&& podValue != null 
                        		&& podValue.equalsIgnoreCase(storageName)) {
                            LOGGER.error("Duplicate Storage Cluster Name And Volume Name");
                            componentValid.addMessage(AsmManagerMessages.duplicatePodAndVolName(storageName));
                            break;
                        }
                    }               

                final Set<String> relCompsKeys = component.getAssociatedComponents().keySet();
                if (relCompsKeys.size() > 0) {
                    // map to keep count of the number of times a server is added to the same cluster
                    // used to make sure a server is not added to the same cluster more than once
                    final Map<ServiceTemplateComponent,Integer> interClusterServerCount = 
                            new HashMap<ServiceTemplateComponent,Integer>();
                    for (String key: relCompsKeys) {
                        final ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                        // Loop through the related components and check for any servers that are added twice
                        if (relComp != null && relComp.getType() == ServiceTemplateComponentType.SERVER
                        		&&  relComp.isTeardown() == false) {
                            // check the number of occurrences of server in current cluster is not more than one
                            if (!interClusterServerCount.containsKey(relComp)) {
                                interClusterServerCount.put(relComp, Integer.valueOf(0));
                            }
                            Integer serverOccurrencesInSameCluster = interClusterServerCount.get(relComp);
                            interClusterServerCount.put(relComp, ++serverOccurrencesInSameCluster);
                            if (serverOccurrencesInSameCluster > 1) {
                                componentValid.addMessage(AsmManagerMessages.serverIsInMoreThan1Cluster());
                                componentValid.setValid(Boolean.FALSE);
                                svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                                continue;
                            }
                            
                            // check occurrences of server between clusters is not more than one
                            if (!intraClusterServerCount.containsKey(relComp)) {
                                intraClusterServerCount.put(relComp, Integer.valueOf(0));
                            }
                            Integer serverOccurrencesBetweenClusters = intraClusterServerCount.get(relComp);
                            intraClusterServerCount.put(relComp, ++serverOccurrencesBetweenClusters);
                            if (serverOccurrencesBetweenClusters > 1) {
                                componentValid.addMessage(AsmManagerMessages.serverIsInMoreThan1Cluster());
                                componentValid.setValid(Boolean.FALSE);
                                svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                            }
                            
                        }
                    }
                }

                int requiredNumberOfHosts = 0;
				String vsanParamValue = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
						ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID);
				if (vsanParamValue != null
						&& vsanParamValue.equalsIgnoreCase("true")
						&& failureToleranceMethodSetting == null
						&& numberFailuresSet == null) {
					requiredNumberOfHosts = 3;	
				}
                               
                if (failureToleranceMethodSetting != null &&
                        numFailuresToTolerate >= 0) {

                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID1_ID.equals(failureToleranceMethodSetting.getValue())) {
                        switch (numFailuresToTolerate) {
                            case 0:
                            case 1:
                                requiredNumberOfHosts = 3;
                                break;
                            case 2:
                                requiredNumberOfHosts = 5;
                                break;
                            case 3:
                                requiredNumberOfHosts = 7;
                        }

                    } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID5_ID.equals(failureToleranceMethodSetting.getValue())) {
                        switch (numFailuresToTolerate) {
                            case 1:
                                requiredNumberOfHosts = 4;
                                break;
                            case 2:
                                requiredNumberOfHosts = 6;
                        }
                    }
                    
                }
                
                if (intraClusterServerCount.size() < requiredNumberOfHosts) {
                    componentValid.addMessage(AsmManagerMessages.insufficientNumberOfHostsPerCluster(intraClusterServerCount.size(), requiredNumberOfHosts));
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                }

                // vsan port names
                ServiceTemplateSetting vdsSet = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID);
                if (vdsSet != null && !vdsSet.isHideFromTemplate() &&
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID.equals(vdsSet.getValue())) {
                    ServiceTemplateCategory vdsCategory = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID);
                    if (vdsCategory != null) {
                        List <String> vdsNames = new ArrayList<>();
                        List <String> pgNames = new ArrayList<>();
                        for (ServiceTemplateSetting setting: vdsCategory.getParameters()) {
                            if (setting.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID)) {
                                String vdsName = null;
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX.equals(setting.getValue())) {
                                    ServiceTemplateSetting newSetting = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + setting.getId());
                                    if (newSetting != null && StringUtils.isNotEmpty(newSetting.getValue())) {
                                        vdsName = newSetting.getValue();
                                    }
                                } else {
                                    vdsName = setting.getValue();
                                }

                                if (StringUtils.isNotEmpty(vdsName)) {
                                    if (vdsNames.contains(vdsName)) {
                                        componentValid.addMessage(AsmManagerMessages.duplicateVDSName(vdsName));
                                    } else {
                                        vdsNames.add(vdsName);
                                    }
                                }
                            } else if (setting.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID)) {
                                String pgName = null;
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX.equals(setting.getValue())) {
                                    ServiceTemplateSetting newSetting = vdsCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + setting.getId());
                                    if (newSetting != null && StringUtils.isNotEmpty(newSetting.getValue())) {
                                        pgName = newSetting.getValue();
                                    }
                                } else {
                                    pgName = setting.getValue();
                                }

                                if (StringUtils.isNotEmpty(pgName)) {
                                    if (pgNames.contains(pgName)) {
                                        componentValid.addMessage(AsmManagerMessages.duplicatePortGroupName(pgName));
                                    } else {
                                        pgNames.add(pgName);
                                    }
                                }
                            }
                        }
                    }
                }


                // if there are any validation error messages then template is invalid
                if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                }
            }
        }
    }

    private static String generateClusterKey(final ServiceTemplate svcTemplate,
                                             final ServiceTemplateComponent clusterComponent) {
        if (clusterComponent != null && clusterComponent.getType() == ServiceTemplateComponentType.CLUSTER) {
            String asmGuid = getSafeTemplateSettingValue(svcTemplate, clusterComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
            String clusterName = asmGuid + "_";
            if (clusterComponent.getComponentID().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID)) {
                final String newClusterId = getSafeTemplateSettingValue(svcTemplate, clusterComponent,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX
                                + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID);
                final String clusterId = getSafeTemplateSettingValue(svcTemplate, clusterComponent,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID);
                clusterName += StringUtils.isNotBlank(newClusterId) ? newClusterId : clusterId;
            } else if (clusterComponent.getComponentID().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID)) {
                final String newClusterId = getSafeTemplateSettingValue(svcTemplate, clusterComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX
                        + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID);
                final String clusterId = getSafeTemplateSettingValue(svcTemplate, clusterComponent,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID);
                clusterName += StringUtils.isNotBlank(newClusterId) ? newClusterId : clusterId;
            }
            return clusterName;
        } else {
            LOGGER.error(String.format("Component: %s is not of cluster type.", clusterComponent));
            throw new IllegalArgumentException(String.format("Component: %s is not of cluster type.", clusterComponent));
        }
    }

    private static String getSafeTemplateSettingValue(final ServiceTemplate svcTemplate, 
            final ServiceTemplateComponent component, final String templateSettingId) {
        final ServiceTemplateSetting templateSetting = svcTemplate.getTemplateSetting(component, templateSettingId);
        return (templateSetting != null) ? templateSetting.getValue() : StringUtils.EMPTY;
    }

    public void validateServerComponents(ServiceTemplate svcTemplate, final  Map<String, String> repoToTaskMap) {
        for (ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
            boolean installHyperVChecked = false,
                    domainSettingsMissing = false,
                    domainSettingsFilled = false,
                    rhelImage = false,
                    windows2008 = false,
                    isHypervChapValidationExecuted = false;

            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                final ServiceTemplateValid componentValid = component.getComponentValid();
                ServiceTemplateSetting templateImageType = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
                if (templateImageType != null && StringUtils.isNotEmpty(templateImageType.getValue())) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE.equals(templateImageType.getValue())
                                || ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE.equals(templateImageType.getValue())) {
                            rhelImage = true;
                        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE.equals(templateImageType.getValue())) {
                            windows2008 = true;
                        }
                }                                             
                for (ServiceTemplateCategory category : safeList(component.getResources())) {
                    for (ServiceTemplateSetting setting : safeList(category.getParameters())) {
                        if (!rhelImage && setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID)) {
                            if (StringUtils.isNotEmpty(setting.getValue()) 
                                    && setting.getValue().toLowerCase().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ADVANCED_NON_RAID_ID)) {
                                LOGGER.error("non-RAID configuration not allowed for non-RHEL images: " + setting.getValue());
                                componentValid.addMessage(AsmManagerMessages.invalidNonRaidConfiguration());
                            }
                        } 
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE.equalsIgnoreCase(setting.getValue()) 
                                || ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE.equalsIgnoreCase(setting.getValue())) {
                            Map<String, ServiceTemplateComponent> map = svcTemplate.fetchComponentsMap();
                            for (String key : component.getAssociatedComponents().keySet()){
                                if (map.get(key) != null && map.get(key).getType() == ServiceTemplateComponentType.CLUSTER){
                                    ServiceTemplateCategory cat = svcTemplate.getTemplateResource(map.get(key), ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID);
                                    if (cat == null) {
                                        componentValid.addMessage(AsmManagerMessages.windowsServerWithVMWareCluster());
                                    }
                                }
                            }
                        }                        
                        if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID)) {
                        	if (StringUtils.isNotEmpty(setting.getValue()) && !HostnameUtil.isValidHostName(setting.getValue(), component)) {
                        		ServiceTemplateSetting imageType = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
                        		if (imageType!=null && imageType.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE)) {
                        			LOGGER.error("Invalid hostname for ESX: " + setting.getValue());
                        			componentValid.addMessage(AsmManagerMessages.invalidEsxHostname(setting.getValue()));
                        		} else {
                        			LOGGER.error("Invalid hostname: " + setting.getValue());
                        			componentValid.addMessage(AsmManagerMessages.invalidHostname(setting.getValue()));
                        		}
                        	}
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_VIRTUALIZATION_ID) 
                                && ((component.hasHyperV() || component.hasESX(repoToTaskMap)) && !(setting.getValue().equals("Enabled")))) {
                            LOGGER.error("BIOS VT must be set for ES or HyperV for server : " + component.getAsmGUID());
                            componentValid.addMessage(AsmManagerMessages.mustSetBIOSForHypervisor());                            
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_EXECUTE_DISABLE_ID)) {
                            // must be checked for ESX and HyperV
                            if ((component.hasHyperV() || component.hasESX(repoToTaskMap))
                                    && !(setting.getValue().equals("Enabled"))) {
                                LOGGER.error("BIOS Execute Disable must be set for ES or HyperV for server : " + component.getAsmGUID());
                                componentValid.addMessage(AsmManagerMessages.execDisableCheck());
                            }
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID)) {
                            if (!svcTemplate.isDraft()) {
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL.equals(component.getComponentID()) &&
                                        component.hasDiskBoot() && (setting.getValue() == null || setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT_ID) || setting.getValue().equals(""))) {
                                    LOGGER.error("Missed OS image for server : " + component.getAsmGUID());
                                    componentValid.addMessage(AsmManagerMessages.serverMustHaveOsImage());
                                }

                                // Validate the external image is actually present
                                if (!getOsRepositoryUtil().isOSRepositoryValid(setting.getValue())) {
                                    LOGGER.error("OS Repository in Error state : " + component.getAsmGUID());
                                    componentValid.addMessage(AsmManagerMessages.osRepositoryInvalid(setting.getValue()));
                                }
                            }
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID)) {
                        	if (StringUtils.isNotEmpty(setting.getValue()) && !HostnameUtil.isValidHostNameTemplate(setting.getValue(), component)) {
                        		ServiceTemplateSetting imageType = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
                        		if (imageType!=null && imageType.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE)) {
                        			LOGGER.error("Invalid hostname template for ESX: " + setting.getValue());
                        			componentValid.addMessage(AsmManagerMessages.invalidEsxHostnameTemplate(setting.getValue()));
                        		} else {
                        			LOGGER.error("Invalid hostname template: " + setting.getValue());
                        			componentValid.addMessage(AsmManagerMessages.invalidHostnameTemplate(setting.getValue()));
                        		}
                        	}
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID)) {
                            if (StringUtils.isNotEmpty(setting.getValue()) && !validateRAID(setting.getValue())) {
                                LOGGER.error("Invalid RAID configuration: " + setting.getValue());
                                componentValid.addMessage(AsmManagerMessages.invalidRaidConfiguration());
                            }
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID)) {
                            // NTP server is required for HyperV
                            if (component.hasHyperV() && StringUtils.isEmpty(setting.getValue())) {
                                LOGGER.error("NTP not set for HyperV for server : " + component.getAsmGUID());
                                componentValid.addMessage(AsmManagerMessages.mustSetNtpForHypervisor());
                            }
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID)){

                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL.equals(component.getComponentID()) &&
                                    (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD.equals(setting.getValue()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN.equals(setting.getValue()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID.equals(setting.getValue())) &&
                                    !component.hasESX(repoToTaskMap)){
                                LOGGER.error("OS Image must be ESX for SD boot types, server : " + component.getAsmGUID());
                                componentValid.addMessage(AsmManagerMessages.mustHaveESXForSD());
                            }
                            if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL.equals(component.getComponentID()) &&
                                    (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD.equals(setting.getValue()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID.equals(setting.getValue()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN.equals(setting.getValue()) ||
                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN.equals(setting.getValue()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD.equals(setting.getValue()))){
                                
                                List<Network> networks =
                                        getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.PXE);
                                if (networks.size() == 0) { // Then a PXE Network does NOT exist!
                                    LOGGER.error("Server uses a SD Card or a Local Hard Drive but does not have a PXE Network configured.");
                                    componentValid.addMessage(AsmManagerMessages.serverMissingPxeNetworkForBootDevice());
                                }
                            }
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_DEFAULT_GATEWAY_ID)){
                            if (StringUtils.isNotEmpty(setting.getValue()) && !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_DHCP_NETWORK_ID.equals(setting.getValue())) {
                                List<NetworkType> types = new ArrayList<>();
                                types.add(NetworkType.PUBLIC_LAN);
                                types.add(NetworkType.PRIVATE_LAN);
                                List<Network> networks =
                                        getServiceTemplateUtil().getServerNetworkByType(component, types);
                                boolean found = false;
                                for (Network network: networks) {
                                    if (setting.getValue().equals(network.getId())) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    LOGGER.error("Selected default gateway network is not included in the network configuration.");
                                    componentValid.addMessage(AsmManagerMessages.invalidDefaultGatewayNetwork());
                                }
                            }
                        } else if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.LOCAL_STORAGE_ID) &&
                                !setting.isHideFromTemplate() && "true".equals(setting.getValue())){

                            List<Network> netwVSAN = serviceTemplateUtil.getServerNetworkByType(component, NetworkType.VSAN);
                            // normally we would use HashSet<NetworkConfiguration> for duplicate check
                            // but hashCode of NetworkConfiguration has issues (recursive inclusion in iPAddressRanges) and will yeld
                            // different result for same object. I don't want to fix it at this point as it potentially affects large amount of code.
                            // all we need here to ensure unique vlan ID for VSAN networks

                            HashSet<String> duplicateNetworkCheck = new HashSet<>();
                            for (Network ncfg: netwVSAN) {
                                duplicateNetworkCheck.add(String.valueOf(ncfg.getVlanId()));
                            }
                            if (duplicateNetworkCheck.size() != 1) {
                                LOGGER.error("Number of VSAN networks != 1: " + duplicateNetworkCheck.size());
                                componentValid.addMessage(AsmManagerMessages.incorrectNumberOfVSANNetworks());
                            }
                        }
                        if(setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID) 
                                && (!setting.getValue().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE) 
                                        && !setting.getValue().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID))) {                            
                            List<ServiceTemplateCategory> compList = component.getResources();                            
                            for (ServiceTemplateCategory comp : compList) {
                                if (comp.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE)) {
                                    List<ServiceTemplateSetting> paramList = comp.getParameters();
                                    for (ServiceTemplateSetting param : paramList) {
                                        if ((param.getId().equals("BootMode") && !param.getValue().equalsIgnoreCase("bios"))){
                                            LOGGER.error("If Target boot device is not None or None with RAID, BootMode must be  BIOS");
                                            componentValid.addMessage(AsmManagerMessages.bootModeCheck());  
                                        }
                                    }
                                }
                            }                            
                        }
                        if ((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID) 
                                && setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD))) {                            
                            List<ServiceTemplateCategory> compList = component.getResources();                            
                            for (ServiceTemplateCategory comp : compList) {
                                if (comp.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE)) {
                                    List<ServiceTemplateSetting> paramList = comp.getParameters();
                                    for (ServiceTemplateSetting param : paramList) {
                                        if ((param.getId().equals("IntegratedRaid") && param.getValue().equals("Enabled")) || (param.getId().equals("InternalSdCard") && !param.getValue().equals("On"))){
                                            LOGGER.error("If - Target boot device = SD, IntegratedRaid must not be enabled and IntegratedSd must be On");
                                            componentValid.addMessage(AsmManagerMessages.integratedRaidCheck());  
                                        }
                                    }
                                }
                            }                            
                        }
                        if ((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID) 
                                && setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD))) {                            
                            List<ServiceTemplateCategory> compList = component.getResources();                            
                            for (ServiceTemplateCategory comp : compList) {
                                if (comp.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE)) {
                                    List<ServiceTemplateSetting> paramList = comp.getParameters();
                                    for (ServiceTemplateSetting param : paramList) {
                                        if ((param.getId().equals("InternalSdCard") && param.getValue().equals("On")) || (param.getId().equals("InternalSdCardRedundancy") && !param.getValue().equals("n/a")) || (param.getId().equals("InternalSdCardPrimaryCard") && !param.getValue().equals("n/a"))){
                                            LOGGER.error("If - Target boot device = HD, IntegratedSd must be Off, InternalSdCardRedundancy and InternalSdCardPrimaryCard must be NA");
                                            componentValid.addMessage(AsmManagerMessages.integratedSdCheck());  
                                        }
                                    }
                                }
                            }                            
                        }                        
                        if ((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL) 
                                && setting.getValue().equals("true"))) {
                        	installHyperVChecked = true;
                        }
                        if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_DN_ID)
                        		|| setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_ADMIN_LOGIN_ID)
                        		|| setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_FQDN_ID)
                        		|| setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID)
                        		|| setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID)) {
                        	if (StringUtils.isEmpty(setting.getValue())) {
                                domainSettingsMissing = true;
                            }else{
                                // at least one domain field is not empty
                                domainSettingsFilled = true;
                            }
                        }
                        if (!windows2008
                        		&& ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_DN_ID.equalsIgnoreCase(setting.getId())
                        		&& !StringUtils.isBlank(setting.getValue())
                        		&& !setting.isHideFromTemplate()) {                        	
                        	String domain_login = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, 
                        			ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_ADMIN_LOGIN_ID);
                        	String domain_pw = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, 
                        			ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID);                 	
                        	if (StringUtils.isBlank(domain_login)
                        			|| StringUtils.isBlank(domain_pw)) {
                        		LOGGER.error("If domain name is specified for a server component domain admin and domain admin password are required");
                        		componentValid.addMessage(AsmManagerMessages.winDomainFieldsCheck());  
                        	}
                        } 
                    }
                }
                if (windows2008
                        && domainSettingsFilled) {
                    LOGGER.error("Windows 2008 used with Domain settings");
                    componentValid.addMessage(AsmManagerMessages.windows2008WithDomainSettings());
                }
                if (installHyperVChecked
                		&& domainSettingsMissing) {
                	LOGGER.error("Install HyperV checked while DomainName or Username or Password or Conf Password is empty");
                    componentValid.addMessage(AsmManagerMessages.hyperVDomainSettingsMissing());                      	
                }
                if (installHyperVChecked
                        && !isHypervChapValidationExecuted) {                   
                    if (isHyperVWithChapStorage(svcTemplate, component, isHypervChapValidationExecuted)) {
                        LOGGER.error("For Install HyperV case Equallogic volumes can not have CHAP authentication");
                        componentValid.addMessage(AsmManagerMessages.hypervEqlChapValidation());
                    }
                }
                this.validateServerWithEqualLogicStorageIsNotUsingStaticIscsiNetwork(svcTemplate, component);
                this.validateServerComponentsWithStorageHaveTwoPartitionsWithIsciNetworks(svcTemplate, component);

                if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                }
            }
        }
    }

    public boolean isHyperVWithChapStorage(ServiceTemplate svcTemplate, ServiceTemplateComponent component, boolean isHypervChapValidationExecuted) {
        Map<String, ServiceTemplateComponent> map = svcTemplate.fetchComponentsMap();
        boolean validationFailed = false;
        for (String key : component.getAssociatedComponents().keySet()){
            if (map.get(key) != null && map.get(key).getType() == ServiceTemplateComponentType.STORAGE){
                for (ServiceTemplateCategory category : safeList(svcTemplate.findComponentById(key).getResources())) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID.equals(category.getId())) {
                        for (ServiceTemplateSetting setting : safeList(category.getParameters())) {
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID.equals(setting.getId())) {
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_CHAP_ID.equals(setting.getValue())
                                        && !isHypervChapValidationExecuted) {
                                    isHypervChapValidationExecuted = true;
                                    validationFailed = true;
                                }
                            }
                        }                                                                                                                                                       
                    }
                }
            }  
        }
        return validationFailed;
    }


    public void validateComponentDependencies(ServiceTemplate svcTemplate, final Map<String, String> repoToTaskMap) {
        boolean clusterPresent = templateContainsComponentOfType(svcTemplate, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER);
        boolean vmPresent = templateContainsComponentOfType(svcTemplate, ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE);
        boolean applicationPresent = templateContainsComponentOfType(svcTemplate, ServiceTemplateComponent.ServiceTemplateComponentType.SERVICE);
        boolean serverPresent = templateContainsComponentOfType(svcTemplate, ServiceTemplateComponentType.SERVER);
        boolean storagePresent = templateContainsComponentOfType(svcTemplate, ServiceTemplateComponentType.STORAGE);


        final ServiceTemplateValid serviceTemplateValid = svcTemplate.getTemplateValid();
        if (applicationPresent && !serverPresent && !vmPresent) {
            LOGGER.warn("Template has an application, but no virtual machine or server.");
            serviceTemplateValid.addMessage(AsmManagerMessages.templateHasApplicationWithoutVM());
        }
        if (vmPresent && !clusterPresent) {
            LOGGER.warn("Template has a virtual machine, but no cluster.");
            serviceTemplateValid.addMessage(AsmManagerMessages.templateHasVMWithoutCluster());
        }
        if (CollectionUtils.isNotEmpty(serviceTemplateValid.getMessages())) {
            serviceTemplateValid.setValid(Boolean.FALSE);
            return; // if the template is invalid dont proceed with checking the components 
        }

        if (serverPresent) {
            if(!svcTemplate.isDraft()) {
                for (ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
                    if (component.getType() == ServiceTemplateComponentType.SERVER) {
                        validateNetworks(component, repoToTaskMap);
                        if (!component.getComponentValid().isValid()) {
                            svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                            continue; // network validation failed
                        }

                        final ServiceTemplateValid componentValid = component.getComponentValid();
                        List<Network> netwStorageISCSI = getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.STORAGE_ISCSI_SAN);
                        List<Network> netwStorageFC = getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.STORAGE_FCOE_SAN);
                        List<Network> netwHypMgmt = getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.HYPERVISOR_MANAGEMENT);
                        List<Network> netwHypMgr = getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.HYPERVISOR_MIGRATION);
                        List<Network> netwPXE = getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.PXE);
                        List<Network> netwFile = getServiceTemplateUtil().getServerNetworkByType(component, NetworkType.FILESHARE);
                        final Set<String> relCompsKeys = component.getAssociatedComponents().keySet();
    
                        if (component.hasHyperV()) {
                            // must have 2 storages of EQL type, with at lest 512m size volume
                            int numStorageComp = 0;
                            if (relCompsKeys.size() > 0) {
                                for (String key : relCompsKeys) {
                                    ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                                    if (relComp != null && relComp.getType() == ServiceTemplateComponentType.STORAGE) {
                                        ServiceTemplateSetting volume = svcTemplate.getTemplateSetting(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                                        numStorageComp++;
                                        ServiceTemplateSetting storageSize = svcTemplate.getTemplateSetting(relComp, "size");
                                        // check only for new volumes
                                        if (ServiceTemplateClientUtil.isNewStorageVolume(volume, true) &&
                                                (storageSize == null || getStorageSizeMB(storageSize.getValue()) < 512)) {
                                            componentValid.addMessage(AsmManagerMessages.storageSizeLess512());
                                        }
                                    } else if (relComp != null && relComp.getType() == ServiceTemplateComponentType.CLUSTER) {
                                        ServiceTemplateCategory cat = svcTemplate.getTemplateResource(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID);
                                        if (cat == null) {
                                            componentValid.addMessage(AsmManagerMessages.clusterMustBeHyperV());
                                        }
                                    }
                                }
                                if (storagePresent && numStorageComp < 2) {
                                    componentValid.addMessage(AsmManagerMessages.storageMustHave2EQL());
                                }
                            }
    
                        } else if (component.hasESX(repoToTaskMap)) {
                            ServiceTemplateSetting stgMEM = svcTemplate.getTemplateSetting(component,
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_EQL_MEM_ID);
    
                            // Throw a validation error if EqlLogic MEM is enbaled with a DHCP storage network.
                            if (storagePresent && stgMEM != null && stgMEM.getValue() != null && stgMEM.getValue().equals("true")) {
                                if (!(netwStorageISCSI.size() > 0 && netwStorageISCSI.get(0).isStatic()
                                        || netwStorageFC.size() > 0 && netwStorageFC.get(0).isStatic())) {
                                    componentValid.addMessage(AsmManagerMessages.memMustHaveStatic());
                                }
    
                                // if there is a related compellent storage, porttype must be FibreChannel
                                if (relCompsKeys.size() > 0) {
                                    for (String key : relCompsKeys) {
                                        ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                                        if (relComp != null && relComp.getType() == ServiceTemplateComponentType.STORAGE) {
                                            ServiceTemplateCategory eqlStorage = svcTemplate.getTemplateResource(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);
                                            if (eqlStorage == null) {
                                                componentValid.addMessage(AsmManagerMessages.eqlMemUsedWithWrongStorage());
                                            }
                                        }
                                    }
                                }
                            }
    
                            if (clusterPresent) {
                                // Check for 2 conditions
                                // 1. Throw an error if a server is related to more than one cluster
                                // 2. Throw an error if HA is enable with less than 2 storage components
    
                                int numStorageComp = 0;
                                int numClusterComp = 0;
                                boolean isHA = false;
                                boolean hasEQL = false;
                                boolean isVSAN = false;
    
                                if (relCompsKeys.size() > 0) {
                                    for (String key : relCompsKeys) {
                                        ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                                        if (relComp != null) {
                                            if (relComp.getType() == ServiceTemplateComponentType.STORAGE) {
                                                numStorageComp++;
                                            }
                                            if (relComp.getType() == ServiceTemplateComponentType.CLUSTER) {
                                                ServiceTemplateSetting stgHA = svcTemplate.getTemplateSetting(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_HA_ID);
                                                if (stgHA != null && stgHA.getValue() != null && stgHA.getValue().equals("true")) {
                                                    isHA = true;
                                                }
                                                numClusterComp++;
                                                if (numClusterComp > 1) {
                                                    componentValid.addMessage(AsmManagerMessages.serverIsInMoreThan1Cluster());
                                                }

                                                // check for VDS cluster
                                                if (netwHypMgmt.size() < 2) {
                                                    ServiceTemplateSetting vdsSet = relComp.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID);
                                                    if (vdsSet != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID.equals(vdsSet.getValue()))
                                                        componentValid.addMessage(AsmManagerMessages.serverMustHave2HVMNetworks());
                                                }
                                            }
                                            if (storagePresent && svcTemplate.getTemplateResource(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID) != null) {
                                                hasEQL = true;
                                            }
                                        } else {
                                            // we don't have default related components any more. If we are here, something must be wrong with
                                            // template processing. Keep it for debugging
                                            LOGGER.error("Cannot find related component by key: " + key + ", service template: " + MarshalUtil.marshal(svcTemplate));
                                        }
                                    }
                                }
                                
                                if (checkForMinStorageComponents(component, isHA, storagePresent, numStorageComp)) {
                                    componentValid.addMessage(AsmManagerMessages.haMustHave2StorComponents());
                                }
    
                                // We need a minimum of 2 iscsi networks selected for an end to end esx deployment with a cluster
                                if (hasEQL && netwStorageISCSI.size() != 2) {
                                    componentValid.addMessage(AsmManagerMessages.serverMustHave2ISCSI());
                                }
                            }else if (storagePresent) {
                                // ESX, storage and no cluster
                                componentValid.addMessage(AsmManagerMessages.esxTemplateMustHaveCluster());
                            }
                        }
                        if (storagePresent && component.hasDiskBoot()) {
                            boolean hasNetApp = false;
                            boolean hasCompellent = false;
                            boolean hasEQL = false;
                            if (relCompsKeys.size() > 0) {
                                for (String key : relCompsKeys) {
                                    ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                                    if (relComp != null && relComp.getType() == ServiceTemplateComponentType.STORAGE) {
                                        if (svcTemplate.getTemplateResource(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID) != null) {
                                            hasNetApp = true;
                                        }
                                        if (svcTemplate.getTemplateResource(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID) != null) {
                                            hasCompellent = true;
                                        }
                                        if (svcTemplate.getTemplateResource(relComp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID) != null) {
                                            hasEQL = true;
                                        }
                                    }
                                }
                            }
                            // SAN (iSCSI) network is required in the case if the template include the EQL storage as datastore
                            if (netwStorageISCSI.size() == 0 && hasEQL) {
                                componentValid.addMessage(AsmManagerMessages.serverMustHaveStorageNetwork());
                            }
                            // ISCSI and CMPL = error
                            /*if (netwStorageISCSI.size() >0 && hasCompellent) {
                                componentValid.addMessage(AsmManagerMessages.storageCompellentAndISCSI());
                            }*/
                            // In the case for NetApp, make sure required networks are selected. - Hypervisor Mgmt, Hypervisor Migration, PXE must be partition 1, and Fileshare network.
                            if (hasNetApp && (netwHypMgmt.size() == 0 || netwHypMgr.size() == 0 || netwPXE.size() == 0 || netwFile.size() == 0)) {
                                componentValid.addMessage(AsmManagerMessages.serverWithNetappMustHaveRequiredNetwork());
                            }
                        }
    
                        if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                            componentValid.setValid(Boolean.FALSE);
                            svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                        }
                        // end of server type
                    } else if (component.getType() == ServiceTemplateComponentType.CLUSTER) {
                        final ServiceTemplateValid componentValid = component.getComponentValid();
                        // hyperV cluster must be coupled with hyperv server
                        if (component.hasHyperV()) {
                            final Set<String> relCompsKeys = component.getAssociatedComponents().keySet();
                            int numStorageComp = 0;
                            if (relCompsKeys.size() > 0) {
                                for (String key : relCompsKeys) {
                                    ServiceTemplateComponent relComp = svcTemplate.findComponentById(key);
                                    if (relComp != null && relComp.getType() == ServiceTemplateComponentType.SERVER) {
                                        if (!relComp.hasHyperV())
                                            componentValid.addMessage(AsmManagerMessages.serverMustBeHyperV());
                                    }
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                            componentValid.setValid(Boolean.FALSE);
                            svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                        }
                    } // end of cluster type
                } // end of component loop
            }
            else { // Means we are in draft mode
                for (ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
                    if (component.getType() == ServiceTemplateComponentType.SERVER) {
                        validateNetworks(component, repoToTaskMap);
                    }
                }
            }
        }
    }    
    
    
    boolean checkForMinStorageComponents(ServiceTemplateComponent component, boolean isHA, boolean storagePresent, int numStorageComp) {
        String vsan_param = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE_VSAN);
        boolean isVSAN = false;
        if (vsan_param != null && vsan_param.equals("true")) {
            isVSAN = true;
        }
        if (isHA && storagePresent && numStorageComp < 2 && !isVSAN) {
            return true;   
        } else {
            return false;
        }           
    }


    /**
     * Validate server network configuration
     * @param component
     */
    public void validateNetworks(ServiceTemplateComponent component, final Map<String, String> repoToTaskMap) {
        // for use with partition mask
        int M_PXE   = 0x0000001;
        int M_HMGMT = 0x0000010;
        int M_HMGRN = 0x0000100;
        int M_HCLST = 0x0001000;
        int M_ISCSI = 0x0010000;
        int M_FILE  = 0x0100000;
        int M_OTHER = 0x1000000;

        ServiceTemplateSetting networking = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        if (networking == null) {
            // some server components may not include networking configuration
            return;
        }

        // Skip network validation of hardware only configuration
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_HW.equals(component.getComponentID())) {
            return;
        }

        com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = getServiceTemplateUtil().deserializeNetwork(networking.getValue());

        if (networkConfig==null) {
            LOGGER.warn("No networking configuration on server component: " + component.getAsmGUID());
            return;
        }
        boolean isSanBoot = component.hasSanBoot();
        boolean isNoneBoot = component.hasNoneBoot();
        boolean hasESX = component.hasESX(repoToTaskMap);
        boolean hasHyperV = component.hasHyperV();
        boolean hasBareMetalOS = component.hasBareMetal(repoToTaskMap);
        boolean isISCSIBoot = component.hasSanISCSIBoot();

        List<String> vMotionNetworks = new ArrayList<String>();
        List<String> pxeNetworks = new ArrayList<String>();       
        List<String> hypManangementNetworks = new ArrayList<String>();
        List<String> vsanNetworks = new ArrayList<String>();
        List<String> fipsNetworks = new ArrayList<String>();

        boolean hasPXE = false;
        boolean hasStaticPXE = false;
        boolean hasPXEOnPartNot1 = false;
        boolean hasHypervisorMgmt = false;
        boolean hasHMOnPart1 = false;
        boolean hasHypervisorMigration = false;
        boolean hasHypervisorCluster = false;
        boolean hasISCSI = false;

        boolean hasHypervisorMgmtStatic = false;
        boolean hasHypervisorMigrationStatic = false;
        boolean hasHypervisorClusterStatic = false;
        boolean hasISCSIStatic = false;
        boolean hasInvalidPartitionNetwork = false;

        boolean hasOtherStatic = false;

        boolean componentInvalidForEsx = false;
        String errorCase = null;

        List<Interface> interfaces = networkConfig.getUsedInterfaces();

        HashSet<String> partitionNetworkTypes = new HashSet<String>();
        List<List<String>> workloadNetworksCheck = new ArrayList<List<String>>();
        List<String> bareMetalOsNetworkCheck = new ArrayList<String>();

        boolean iscsiOnPort1 = false;
        boolean foundSingleISCSIOnPort1 = false;
        
        boolean isBootDeviceFc = false;
        boolean fcNetworkPresent = false;

        final ServiceTemplateValid componentValid = component.getComponentValid();
        
        for (ServiceTemplateCategory category : safeList(component.getResources())) {
            for (ServiceTemplateSetting setting : safeList(category.getParameters())) {
                if ((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID) 
                        && setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC))) {
                    isBootDeviceFc = true;
                }
                if (((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID)
                        && setting.getValue().toLowerCase().contains("\"usedforfc\":true"))) ||
                        ((setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID)
                        && setting.getValue().toLowerCase().contains("\"fabrictype\":\"fc\"")))) {
                    fcNetworkPresent = true;
                }
            }       
        }       
        
        if (isBootDeviceFc && !fcNetworkPresent){
            LOGGER.error("fcNetworksError");
            componentValid.addMessage(AsmManagerMessages.fcNwtworksValidation());
            componentValid.setValid(Boolean.FALSE);            
        }
        
        for (Interface interfaceObject : interfaces) {         

            if (interfaceObject.isPartitioned() && hasHyperV) {
                // stop vaidation here and return
                componentValid.addMessage(AsmManagerMessages.serverWithHyperVPartitioned());
                componentValid.setValid(Boolean.FALSE);
                return;
            }

            List<Partition> partitions = interfaceObject.getPartitions();

            for (Partition partition : partitions) {                                                
                
                partitionNetworkTypes.clear();
                bareMetalOsNetworkCheck.clear();

                List<String> networkIds = partition.getNetworks();
                Integer curMask = 0;

                if (networkIds != null) {
                    for (String networkId : networkIds) {

                        Network networkObject = getNetworkService().getNetwork(networkId);

                        // In general networkObject should never be null, but ran into cases with
                        // invalid default templates where it was.
                        String networkType = networkObject == null ? "" : networkObject.getType().value();

                        boolean isStatic = networkObject != null && networkObject.isStatic();
                        if (networkType.equals(NetworkType.STORAGE_ISCSI_SAN.value())) {
                            hasISCSI = true;
                            curMask |= M_ISCSI;
                            hasISCSIStatic = isStatic;

                            if (isSanBoot && !isStatic){
                                // iSCSI must be static fo SAN boot iSCSI
                                componentValid.addMessage(AsmManagerMessages.iscsiMustHaveStatic());
                            }
                        }else if (networkType.equals(NetworkType.PXE.value())){
                            hasPXE = true;
                            curMask |= M_PXE;
                            if (networkObject.isStatic()) {
                                hasStaticPXE = true;
                            }
                            if (!partition.getName().equals("1"))
                                hasPXEOnPartNot1 = true;

                        }else if (networkType.equals(NetworkType.HYPERVISOR_MANAGEMENT.value())){
                            hasHypervisorMgmt = true;
                            curMask |= M_HMGMT;
                            if (partition.getName().equals("1"))
                                hasHMOnPart1 = true;

                            hasHypervisorMgmtStatic = isStatic;

                        }else if (networkType.equals(NetworkType.HYPERVISOR_CLUSTER_PRIVATE.value())){
                            hasHypervisorCluster = true;
                            hasHypervisorClusterStatic = isStatic;
                            curMask |= M_HCLST;
                        }else if (networkType.equals(NetworkType.HYPERVISOR_MIGRATION.value())){
                            hasHypervisorMigration = true;
                            hasHypervisorMigrationStatic = isStatic;
                            curMask |= M_HMGRN;
                        }else if (networkType.equals(NetworkType.FILESHARE.value())){
                            curMask |= M_FILE;
                        }else {
                            curMask |= M_OTHER;

                            if (isStatic) {
                                hasOtherStatic = true;
                            }
                        }

                        if (hasESX) {
                            if (networkType.equals(NetworkType.HYPERVISOR_MIGRATION.value())) {                               
                                vMotionNetworks.add(networkId);
                            }
                            
                            if (networkType.equals(NetworkType.PXE.value())) {                               
                                pxeNetworks.add(networkId);
                            }
                            
                            if (networkType.equals(NetworkType.VSAN.value())) {
                                vsanNetworks.add(networkId);
                            }

                            if (networkType.equals(NetworkType.FIP_SNOOPING.value())) {
                                fipsNetworks.add(networkId); 
                            }

                            if (networkType.equals(NetworkType.HYPERVISOR_MANAGEMENT.value())) {
                                hypManangementNetworks.add(networkId); 
                            }

                            if (networkType.equals(NetworkType.PRIVATE_LAN.value()) || networkType.equals(NetworkType.PUBLIC_LAN.value())) {           				
                				List<String> netTemp = new ArrayList<String>();
                				for (String netId : networkIds) {
                					if (NetworkType.PRIVATE_LAN.equals(getNetworkService().getNetwork(netId).getType())
                							|| NetworkType.PUBLIC_LAN.equals(getNetworkService().getNetwork(netId).getType())) {
                						netTemp.add(netId);
                					}
                				}                				
                				workloadNetworksCheck.add(netTemp);
                			}
                            partitionNetworkTypes.add(networkType);                            
                        }

                        if (hasBareMetalOS) {
                            if (!hasInvalidPartitionNetwork) {
                                // partition index, number, etc. is always 0; use the name to identify the partition index
                                if (component.hasLinuxOS() && !partition.getName().equals("1")) {
                                    hasInvalidPartitionNetwork = true;
                                    componentValid.addMessage(AsmManagerMessages.invalidPartitionForNetwork(component.getName()));
                                }
                            }
                            bareMetalOsNetworkCheck.add(networkType);
                        }
                    }
                }

                if (hasBareMetalOS 
                		&& bareMetalOsNetworkCheck.size() != 0) {
                    boolean validNets = true;
                    for (String netType : bareMetalOsNetworkCheck) {
                        if (!netType.equals(NetworkType.PXE.value())
                                && !netType.equals(NetworkType.PRIVATE_LAN.value())
                                && !netType.equals(NetworkType.PUBLIC_LAN.value())
                                && !netType.equals(NetworkType.STORAGE_FCOE_SAN.value())
                                && !netType.equals(NetworkType.FIP_SNOOPING.value())
                                && !netType.equals(NetworkType.FILESHARE.value())
                                && !netType.equals(NetworkType.STORAGE_ISCSI_SAN.value())) {
                            validNets = false;
                            LOGGER.error("incorrectNetworksOnBareMetalOs");
                            componentValid.addMessage(AsmManagerMessages.incorrectNetworkConfForBareMetalAndLinux2());
                            break;
                        }
                    }
                    if (!validNets)
                        break;
                }                
               
                if (hasESX) {
                	if (partitionNetworkTypes.contains(NetworkType.FIP_SNOOPING.value()) 
                			&& !(partitionNetworkTypes.contains(NetworkType.FIP_SNOOPING.value()) 
                					&& partitionNetworkTypes.contains(NetworkType.STORAGE_FCOE_SAN.value()))) {    	
                		componentInvalidForEsx = true;
                		errorCase = ERROR_DUPLICATE_NETWORK_TYPE;
                		LOGGER.error("networkTypeDuplicateOnSamePartition - FIP Snooping FCOE Networks Partitions Error");
                	}
                }
               
                // checks per port
                // Bare metal OS
                if (hasBareMetalOS) {
                    if (interfaceObject.getName().equals("Port 1")) {

                        //  In the case of boot from iSCSI, iSCSI network must be selected for one of the NIC port1. And no other network can be seleced on the same port.
                        if (isISCSIBoot && hasISCSI) {
                            iscsiOnPort1 = true;
                        }

                        if (isISCSIBoot && hasISCSI && curMask == M_ISCSI) {
                            foundSingleISCSIOnPort1 = true;
                        }
                    }
                }

                // ignore all but first partitions
                if (!interfaceObject.isPartitioned()) break;
                // Different nic types have different number of partitions but data may include more that should be ignored
                if (partition.getName().equals(Integer.toString(interfaceObject.getMaxPartitions()))) {
                    break;
                }

            }
        }

        if (hasBareMetalOS) {
            //  In the case of boot from iSCSI, iSCSI network must be selected for one of the NIC port1. And no other network can be seleced on the same port.
            if (isISCSIBoot && !iscsiOnPort1) {
                componentValid.addMessage(AsmManagerMessages.iscsiMustBeOnPort1());
            }
            if (isISCSIBoot && hasISCSI && !foundSingleISCSIOnPort1) {
                componentValid.addMessage(AsmManagerMessages.iscsiMustBeTheOnlyNetwork());
            }
        }

        // For any cases that requires ASM to deploy OS, need to make sure PXE network is selected.
        // this is required for full server component
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL.equals(component.getComponentID())) {
            if (!isSanBoot && !isNoneBoot && !hasPXE) {
                componentValid.addMessage(AsmManagerMessages.serverMustHavePXE());
            } else {
                // Installing Hyper-V or Windows using a static OS Installation network is not currently supported
                if (hasStaticPXE) {
                    ServiceTemplateSetting osVersion = component.getParameter(
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_VERSION_ID);
                }

                if (hasBareMetalOS) {
                    if (hasPXE && hasPXEOnPartNot1)
                        componentValid.addMessage(AsmManagerMessages.wrongPartition(NetworkType.PXE.value(), 1));
                }

                // In the case for Hyper-v, make sure required networks are selected.
                // Hypervisor Mgmt, Hypervisor Migration, Hypervisor Cluster private, PXE, iSCSI
                // no partitions
                if (hasHyperV) {
                    if (!hasPXE)
                        componentValid.addMessage(AsmManagerMessages.serverMissedNetwork("Hyper-V", NetworkType.PXE.value()));
                    else if (hasPXE && hasPXEOnPartNot1)
                        componentValid.addMessage(AsmManagerMessages.wrongPartition(NetworkType.PXE.value(), 1));
                    else if (!hasHypervisorMgmt)
                        componentValid.addMessage(AsmManagerMessages.serverMissedNetwork("Hyper-V", NetworkType.HYPERVISOR_MANAGEMENT.value()));
                    else if (hasHypervisorMgmt && !hasHMOnPart1)
                        componentValid.addMessage(AsmManagerMessages.wrongPartition(NetworkType.HYPERVISOR_MANAGEMENT.value(), 1));
                    else if (!hasHypervisorMigration)
                        componentValid.addMessage(AsmManagerMessages.serverMissedNetwork("Hyper-V", NetworkType.HYPERVISOR_MIGRATION.value()));
                    else if (!hasHypervisorCluster)
                        componentValid.addMessage(AsmManagerMessages.serverMissedNetwork("Hyper-V", NetworkType.HYPERVISOR_CLUSTER_PRIVATE.value()));

                    if (hasISCSI && !hasISCSIStatic) {
                        componentValid.addMessage(AsmManagerMessages.hypervRequiresStatic(NetworkType.STORAGE_ISCSI_SAN.value()));
                    } else if (!hasHypervisorMgmtStatic) {
                        componentValid.addMessage(AsmManagerMessages.hypervRequiresStatic(NetworkType.HYPERVISOR_MANAGEMENT.value()));
                    } else if (!hasHypervisorMigrationStatic) {
                        componentValid.addMessage(AsmManagerMessages.hypervRequiresStatic(NetworkType.HYPERVISOR_MIGRATION.value()));
                    } else if (!hasHypervisorClusterStatic) {
                        componentValid.addMessage(AsmManagerMessages.hypervRequiresStatic(NetworkType.HYPERVISOR_CLUSTER_PRIVATE.value()));
                    }
                }

                // In the case for ESXi, make sure required networks are selected.
                // Hypervisor Mgmt, Hypervisor Migration, Hypervisor Cluster private, PXE must be partition 1
                if (hasESX) {

                    HashSet<String> duplicateNetworkCheck;

                    if (!hasPXE)
                        componentValid.addMessage(AsmManagerMessages.serverMissedNetwork("ESX", NetworkType.PXE.value()));
                    else if (hasPXE && hasPXEOnPartNot1)
                        componentValid.addMessage(AsmManagerMessages.wrongPartition(NetworkType.PXE.value(), 1));
                    else if (!hasHypervisorMgmt)
                        componentValid.addMessage(AsmManagerMessages.serverMissedNetwork("ESX", NetworkType.HYPERVISOR_MANAGEMENT.value()));

                    if (workloadNetworksCheck.size() > 1) {
                    	for (List<String> partitionNetworks : workloadNetworksCheck) {
                    		if (!(partitionNetworks.containsAll(workloadNetworksCheck.get(1))
                    				&& workloadNetworksCheck.get(1).containsAll(partitionNetworks))) { 
                    			componentInvalidForEsx = true;
                    			errorCase = ERROR_WORKLOAD_NETS_NOT_SAME;
                    			LOGGER.error(ERROR_WORKLOAD_NETS_NOT_SAME);
                    		}
                    	}
                    }

                    duplicateNetworkCheck = new HashSet<>(pxeNetworks);
                    if (duplicateNetworkCheck.size() > 1) {
                        componentInvalidForEsx = true;
                        errorCase = ERROR_DUPLICATE_NETWORKS;
                        LOGGER.error("duplicateNetworkCheck - PXE");
                    }
                                        
                    duplicateNetworkCheck = new HashSet<>(vMotionNetworks);
                    if (duplicateNetworkCheck.size() > 1) {
                        componentInvalidForEsx = true;
                        errorCase = ERROR_DUPLICATE_NETWORKS;
                        LOGGER.error("duplicateNetworkCheck - vMotionNetworks");
                    }

                    duplicateNetworkCheck = new HashSet<>(hypManangementNetworks);
                    if (duplicateNetworkCheck.size() > 1) {
                        componentInvalidForEsx = true;
                        errorCase = ERROR_DUPLICATE_NETWORKS;
                        LOGGER.error("duplicateNetworkCheck - hypManangementNetworks");
                    }

                    duplicateNetworkCheck = new HashSet<>(fipsNetworks);
                    if (duplicateNetworkCheck.size() > 1) {
                        componentInvalidForEsx = true;
                        errorCase = ERROR_DUPLICATE_NETWORKS;
                        LOGGER.error("duplicateNetworkCheck - fipsNetworks");
                    }

                    if (componentInvalidForEsx) {
                        if (ERROR_DUPLICATE_NETWORKS.equals(errorCase))
                            componentValid.addMessage(AsmManagerMessages.networksDuplicate());

                        if (ERROR_DUPLICATE_NETWORK_TYPE.equals(errorCase))
                            componentValid.addMessage(AsmManagerMessages.networkTypeDuplicate());

                        if (ERROR_WORKLOAD_NETS_NOT_SAME.equals(errorCase))
                            componentValid.addMessage(AsmManagerMessages.workloadNetworksNotSame());
                    }
                }
            }
        }



        if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
            componentValid.setValid(Boolean.FALSE);
        }
    }    

    public void validateVMs(ServiceTemplate svcTemplate, boolean isDeployment) {
        final List<String> names = new ArrayList<>();
        for (ServiceTemplateComponent component :  safeList(svcTemplate.getComponents())) {
            if (component.getType() == ServiceTemplateComponentType.VIRTUALMACHINE) {
                final ServiceTemplateValid componentValid = component.getComponentValid();
                ServiceTemplateSetting nameSet = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
                if (nameSet!=null) {
                    if (names.contains(nameSet.getValue())) {
                        if (isDeployment) {
                            LOGGER.error("Duplicate name for VM: " + nameSet.getValue());
                            componentValid.addMessage(AsmManagerMessages.duplicateVMName(nameSet.getValue()));
                        }
                    } else {
                        names.add(nameSet.getValue());
                    }
                }

                ServiceTemplateSetting setting = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                if (setting == null) {
                    setting = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
                }
                if (setting!=null && StringUtils.isNotEmpty(setting.getValue()) && !HostnameUtil.isValidHostName(setting.getValue(), component)) {
                    LOGGER.error("Invalid hostname: " + setting.getValue());
                    componentValid.addMessage(AsmManagerMessages.invalidHostname(setting.getValue()));
                }

                setting = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID);
                if ((setting != null) && StringUtils.isNotEmpty(setting.getValue()) && !HostnameUtil.isValidHostNameTemplateForVMs(setting.getValue(), component)) {
                    LOGGER.error("Invalid hostname template: " + setting.getValue());
                    componentValid.addMessage(AsmManagerMessages.invalidHostnameTemplateForVMs());
                }
                
                setting = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_TEMPLATE_ID);
                if ((setting != null) && StringUtils.isNotEmpty(setting.getValue()) && !HostnameUtil.isValidHostNameTemplateForVMs(setting.getValue(), component)) {
                    LOGGER.error("Invalid hostname template: " + setting.getValue());
                    componentValid.addMessage(AsmManagerMessages.invalidHostnameTemplateForVMs());
                }

                ServiceTemplateSetting setNetwork = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID);
                if (setNetwork==null || StringUtils.isEmpty(setNetwork.getValue())) {
                    LOGGER.error("VM must have workload network");
                    componentValid.addMessage(AsmManagerMessages.vmMustHaveNetwork(component.getName()));
                }

                int numClusters = countAssociatedClusters(svcTemplate, component);
                if (numClusters != 1){
                    componentValid.addMessage(AsmManagerMessages.invalidNumberOfClustersForVM());
                }
                // ensure the VM type is valid against the Cluster type
                validateAssociatedClusterType(svcTemplate, component, componentValid);

                if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                }
            }
        }
    }

    private void validateAssociatedClusterType(ServiceTemplate svcTemplate,
                                                      ServiceTemplateComponent component,
                                                      final ServiceTemplateValid componentValid) {
        Set<String> related = component.getAssociatedComponents().keySet();
        ServiceTemplateComponent cluster = null;
        if (related != null && related.size() > 0) { 
            // Match required is no longer completely valid due to VMs having Applications
            // A cluster may not actually exist for the VM, but an Application may there
            boolean matchRequired = false; 
            if (related.size() > 1) { 
                matchRequired = true;
            }
            for (ServiceTemplateComponent c : safeList(svcTemplate.getComponents())) {
                if (c.getType() == ServiceTemplateComponentType.CLUSTER) {
                    if (matchRequired && !related.contains(c.getId())) {
                        continue;
                    } else {
                        cluster = c;
                        break;
                    }
                }
            }
            if (cluster != null // Cluster may not be there if it's been removed
                    && (component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE) != null
                    && cluster.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID) != null)
                    || (component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE) != null
                    && cluster.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID) != null)) {
                LOGGER.warn("VM type and the cluster type do not match!");
                componentValid.addMessage(AsmManagerMessages.invalidVMAssociation());
            }
        }
    }

    private int countAssociatedClusters(ServiceTemplate svcTemplate,
            ServiceTemplateComponent component) {
        int count = 0;
        Set<String> related = component.getAssociatedComponents().keySet();
        if (related != null && related.size() > 0){
            for (ServiceTemplateComponent c :  safeList(svcTemplate.getComponents())){
                if (c.getType() == ServiceTemplateComponentType.CLUSTER && related.contains(c.getId())){
                    ++count;
                }
            }
        }
        return count;
    }

    public void validateRequiredParameters(final ServiceTemplate svcTemplate) {
        // Loop through all the parameters since we dont have a good way to look them up right now
        // if they are marked required ensure a value is set otherwise log message and set template to invalid
        for (final ServiceTemplateComponent component : svcTemplate.getComponents()) {
            final ServiceTemplateValid componentValid = component.getComponentValid();
            for (final ServiceTemplateCategory resource : component.getResources()) {
                for (final ServiceTemplateSetting parameter : resource.getParameters()) {
                    if (isVisibleRequiredParameter(parameter) 
                            && getServiceTemplateUtil().requiredDependenciesSatisfied(component,parameter)
                            && StringUtils.isBlank(parameter.getValue())) {
                        LOGGER.error("Component id: " + component.getId() +
                                " resource id: " + resource.getId() +
                                " is missing a required parameter value for " + parameter.getDisplayName());
                        componentValid.addMessage(AsmManagerMessages.missingTemplateRequired(component.getName(), parameter.getDisplayName()));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                componentValid.setValid(Boolean.FALSE);
                svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
            }
        }
    }    
    
    private static boolean isVisibleRequiredParameter(final ServiceTemplateSetting parameter) {
        if (parameter != null) {
            return !parameter.isHideFromTemplate() && parameter.isRequired();
        }
        return Boolean.FALSE;
    }

    public void validatePasswords(ServiceTemplate svcTemplate) {
        for (ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
            if (component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.SERVER ||
                    component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE ) {
                final ServiceTemplateValid componentValid = component.getComponentValid();
                ServiceTemplateSetting targetBoot = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
                for (ServiceTemplateCategory category : component.getResources()) {
                    if (category.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE)) {
                        ServiceTemplateSetting osPass = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
                        ServiceTemplateSetting osPassConfirm = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID);
                        ServiceTemplateSetting osDomainPass = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID);
                        ServiceTemplateSetting osDomainPassConfirm = svcTemplate.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID);

                        //ignore password check for San Boot types
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL.equals(component.getComponentID()) && targetBoot!=null &&
                                (targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD) ||
                                        targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID) ||
                                        targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN) ||
                                        targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD) ||
                                        targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN)) ||
                                        targetBoot==null) {
                            if (osPass!=null && StringUtils.isEmpty(osPass.getValue())) {
                                componentValid.addMessage(AsmManagerMessages.PasswordNullEmpty(osPass.getDisplayName()));
                                continue;
                            } else if(osPassConfirm != null && StringUtils.isEmpty(osPassConfirm.getValue())) {
                                componentValid.addMessage(AsmManagerMessages.PasswordNullEmpty(osPassConfirm.getDisplayName()));
                                continue;
                            }
                            
                            if(!confirmPasswords(osPass, osPassConfirm)){
                                LOGGER.warn("Passwords don't match.");
                                componentValid.addMessage(AsmManagerMessages.passwordMismatch());
                            }
                        }
                        
                        if(!confirmPasswords(osDomainPass, osDomainPassConfirm)) {
                            LOGGER.warn("Passwords don't match.");
                            componentValid.addMessage(AsmManagerMessages.passwordMismatch());
                        }                     
                    }
                }
            
                if (CollectionUtils.isNotEmpty(componentValid.getMessages())) {
                    componentValid.setValid(Boolean.FALSE);
                    svcTemplate.getTemplateValid().setValid(Boolean.FALSE);
                }
            }
        }
    }
    
    /**
     * Makes sure number of physical disks conforms to RAID level requirements.
     * @param value
     * @return
     */
    public boolean validateRAID(String value) {
        if (StringUtils.isNotEmpty(value)) {
            String configString = "{ \"templateRaidConfiguration\" : " + value + "}";
            TemplateRaidConfiguration configuration = MarshalUtil.fromJSON(TemplateRaidConfiguration.class, configString);
            if (configuration.getRaidtype() == TemplateRaidConfiguration.RaidTypeUI.advanced) {
                for (VirtualDiskConfiguration tvd : configuration.getVirtualdisks()) {
                    int disksRequired = RaidLevel.fromUIValue(tvd.getRaidlevel().name()).getMinDisksRequired();
                    if (disksRequired > tvd.getNumberofdisks()) {
                        return Boolean.FALSE;
                    }
                }
            }
        }

        return Boolean.TRUE;
    }   
    
    /******************************************************************************************************************
     * VALIDATOR helper methods
     ******************************************************************************************************************/   
    
    /**
     * Clear all the validation toggles and messages for the given service template and its associated components
     * @param svcTemplate
     */
    public void clearAllServiceTemplateValidations(final ServiceTemplate svcTemplate) {
        svcTemplate.setTemplateValid(ServiceTemplateValid.getDefaultInstance());
        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            component.setComponentValid(ServiceTemplateValid.getDefaultInstance());
        }
    }
    
    /**
     * Sometimes we need to copy over the validation from temp templates that are used just for validation. This 
     * is usually done when the temp templates are copies with decrypted passwords.
     * @param srcTemplate
     */
    public void copyAllServiceTemplateValidations(final ServiceTemplate srcTemplate, 
            final ServiceTemplate destTemplate) {
        destTemplate.setTemplateValid(srcTemplate.getTemplateValid());
        for (final ServiceTemplateComponent srcComponent : safeList(srcTemplate.getComponents())) {
            final ServiceTemplateComponent destComponent = destTemplate.findComponentById(srcComponent.getId());
            destComponent.setComponentValid(srcComponent.getComponentValid());
        }
    }
    
    public AsmDetailedMessageList getAllServiceTemplateValidationMessages(final ServiceTemplate svcTemplate) {
        final List<AsmDetailedMessage> validationMessages = new ArrayList<AsmDetailedMessage>();
        validationMessages.addAll(svcTemplate.getTemplateValid().getMessages());
        for (final ServiceTemplateComponent component : safeList(svcTemplate.getComponents())) {
            validationMessages.addAll(component.getComponentValid().getMessages());
        }
        return new AsmDetailedMessageList(validationMessages);
    }
    
    /**
     * Parse string like "512m" and return numeric value
     * @param value
     * @return
     */
    private static int getStorageSizeMB(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0;
        }

        String sValue = value.trim();
        int ret = 0;
        try {
            if (sValue.endsWith("MB")) {
                ret = Integer.parseInt(sValue.split("MB")[0]);
            }
            else if (sValue.endsWith("GB")) {
                ret = Integer.parseInt(sValue.substring(0, sValue.indexOf("GB"))) * 1024;
            }
            else if (sValue.endsWith("TB")) {
                ret = Integer.parseInt(sValue.substring(0, sValue.indexOf("TB"))) * 1024 * 1024;
            }
        } catch (NumberFormatException nfe) {
            LOGGER.error("Invalid value for storage size: " + sValue, nfe);
            return 0;
        }

        return ret;
    }
    
    private static boolean templateContainsComponentOfType(ServiceTemplate svcTemplate,
            ServiceTemplateComponentType targetComponentType) {
        List<ServiceTemplateComponent> components = svcTemplate.getComponents();
        for (ServiceTemplateComponent component : components) {
            if (component.getType() == targetComponentType) {
                return true;
            }
        }
        return false;
    }

    private static boolean confirmPasswords(ServiceTemplateSetting pass, ServiceTemplateSetting confirmPass) {
        if (pass == null || confirmPass == null || pass.getValue()==null || confirmPass.getValue() == null) {
            return true;
        }

        return StringUtils.equals(pass.getValue(), confirmPass.getValue()); 
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> safeList(final List<T> list) {
        return CollectionUtils.isEmpty(list) ? Collections.EMPTY_LIST : list;
    }
    
    /**
     *  Identifies groups of Storages that all relate to the same Server and then validates that the group of Storages
     *  are all using the same Authentication (either all using IQN/IP or all using CHAP).
     */ 
    private void validateStorageAuthorizationMatchesServer(ServiceTemplate serviceTemplate){
    	for(ServiceTemplateComponent component : serviceTemplate.getComponents()){
    		// If it's Storage we need to see if it's related to a Server and make sure Authorization is same as other Storages
    		if (component.getType() == ServiceTemplateComponentType.SERVER) {   
    			ArrayList<ServiceTemplateComponent> relatedStorageComponents = ServiceTemplateValidator.getRelatedStoragesForComponentServer(component, serviceTemplate);
    			this.validateStorageAuthorizationMatches(serviceTemplate, relatedStorageComponents);
    		}
    	}
    }

    // Method assumes component passed in is of type Server 
    private static ArrayList<ServiceTemplateComponent> getRelatedStoragesForComponentServer(ServiceTemplateComponent storageComponent, ServiceTemplate serviceTemplate) {
        ArrayList<ServiceTemplateComponent> relatedStorages = new ArrayList<ServiceTemplateComponent>();
        Set<String> associatedComponentKeys = storageComponent.getAssociatedComponents().keySet();
        if (associatedComponentKeys.size() > 0) {
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                if (component.getType() == ServiceTemplateComponentType.STORAGE && associatedComponentKeys.contains(component.getId())) {
                    relatedStorages.add(component);
                }
            }
        }
        return relatedStorages;
    }
    
	/**
	 * Validates whether the Storages for a single Server all use the same Authentication.  All Storage components 
	 * attached to a Service must either use IQN/IP authentication or CHAP authentication.  Storge components attached
	 * to a Server cannot use both IQN/IP and CHAP.
	 * 
	 * If the Storages are not using the same Authentication, then the component where the first discrepancy is found
	 * will be marked as invalid.  Note, only ONE Storage component will be marked as invalid, not all of them.
	 * 
	 * @param serviceTemplate the ServiceTemplate that is being validated.
	 * @param relatedStorageComponents Storages that are all related to the same Server.
	 */
    private void validateStorageAuthorizationMatches(ServiceTemplate serviceTemplate, ArrayList<ServiceTemplateComponent> relatedStorageComponents){
    	
    	// Will assume all of the items in the related/original list are the same, and thus only check the first one.
    	if(relatedStorageComponents.size() > 0 ){
    		ServiceTemplateComponent originalStorageComponent = relatedStorageComponents.get(0);
    		
    		// Check to see what the new Storage component uses for Authentication
    		boolean storageUsesChap = false;
            boolean storageUsesIqn = false;
            for(ServiceTemplateComponent component : relatedStorageComponents){
	            for (ServiceTemplateCategory category : safeList(component.getResources())) {
	                for (ServiceTemplateSetting setting : safeList(category.getParameters())) {
	                	if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID)) {
	                        if (setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_CHAP_ID)) {
	                        	storageUsesChap = true; 
	                        }
	                        else if (setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_IQNIP_ID)) {
	                        	storageUsesIqn = true; 
	                        }
	                        
	                        if(storageUsesChap && storageUsesIqn){
	                        	LOGGER.error("Newly added Storage is configured to use IQN but the related Server's Storage component(s) is not configured to use CHAP!");
	                            
	                        	final ServiceTemplateValid componentValid = component.getComponentValid();
	                        	componentValid.addMessage(AsmManagerMessages.invalidMixedAuthenticationForStorage());
	                            componentValid.setValid(Boolean.FALSE);
	                            serviceTemplate.getTemplateValid().setValid(Boolean.FALSE); 
	                        }
	                	}
	                }
	            }
            }   
    	}
    }
    
    // If a Server has an EqualLogic Related Component that uses IQN/IP Authentication, 
    // then the Server�s Network cannot be a DHCP iSCSI
    // Assumes the component passed in is of type Server.
    private void validateServerWithEqualLogicStorageIsNotUsingStaticIscsiNetwork(ServiceTemplate serviceTemplate, ServiceTemplateComponent serverComponent){
    	
        for (String serverRelCompId : serverComponent.getAssociatedComponents().keySet()) {
            for (ServiceTemplateComponent serverRelatedComponent : serviceTemplate.getComponents()) {
                if (serverRelatedComponent.getId().equals(serverRelCompId)
                        && serverRelatedComponent.getType() == ServiceTemplateComponentType.STORAGE
                        && serviceTemplate.getTemplateResource(serverRelatedComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID) != null
                        && this.doesStorageComponentUseIqnAuthentication(serverRelatedComponent)) {
                    List<Network> networks =
                            getServiceTemplateUtil().getServerNetworkByType(serverComponent, NetworkType.STORAGE_ISCSI_SAN);
                    boolean areIsciNetworksAllStatic = NetworkingUtil.areAllNetworksConfiguredToBeStatic(networks);
                    if (!areIsciNetworksAllStatic) {
                        LOGGER.error("Cannot use a DHCP iSCSI network on a Server component that is attached to an EqualLogic Storage component that uses IQN/IP authentication. IQN/IP requires a static IP in order to set up the authentication rules properly.");

                        final ServiceTemplateValid componentValid = serverComponent.getComponentValid();
                        componentValid.addMessage(AsmManagerMessages.invalidIscsiNetworkConfigurationForServerWithEqualLogicStorageUsingIqnIp());
                        componentValid.setValid(Boolean.FALSE);
                        serviceTemplate.getTemplateValid().setValid(Boolean.FALSE);
                    }
                }
            }
        }
    }
    
    // Returns a boolean indicating if the Storage component uses IQN Authentication
    private boolean doesStorageComponentUseIqnAuthentication(ServiceTemplateComponent storageComponent){
    	boolean usesIqn = false;
    	for (ServiceTemplateCategory category : safeList(storageComponent.getResources())) {
            for (ServiceTemplateSetting setting : safeList(category.getParameters())) {
        	    if (setting.getId().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID) &&
        	    		setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_IQNIP_ID)) {
        	    	usesIqn = true; 
                }
            }
        }
    	return usesIqn;
    }
    
    // Checks to see if a Server with an EqualLogic Storage has at least 2 iscsi Networks assigned
    private void validateServerComponentsWithStorageHaveTwoPartitionsWithIsciNetworks(ServiceTemplate serviceTemplate, ServiceTemplateComponent serverComponent){
              
        boolean isBootFromSanTemplate = false;              
        
        for (ServiceTemplateComponent serverComponent1 : serviceTemplate.getComponents()) {
            for (ServiceTemplateCategory resource : serverComponent1.getResources()) {
                if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE)) {
                    for (ServiceTemplateSetting param : resource.getParameters()) {
                        if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID) 
                                && param.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI)) {
                            isBootFromSanTemplate = true;  
                        }
                    }
                }                  
            } 
        }
        
        for (String serverRelCompId : serverComponent.getAssociatedComponents().keySet()) {
            for (ServiceTemplateComponent serverRelatedComponent : serviceTemplate.getComponents()) {                                            
                
                if (serverRelatedComponent.getId().equals(serverRelCompId)
                        && serverRelatedComponent.getType() == ServiceTemplateComponentType.STORAGE
                        && (serviceTemplate.getTemplateResource(serverRelatedComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID) != null 
                             || (serviceTemplate.getTemplateResource(serverRelatedComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID) != null
                                 && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ISCSI.equals(serviceTemplate.getTemplateSetting(serverRelatedComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID).getValue())))) {
                    List<Network> networks =
                            getServiceTemplateUtil().getServerNetworkByType(serverComponent, NetworkType.STORAGE_ISCSI_SAN);
                    boolean areThereTwoOrMoreNetworks = networks.size() >= 2;
                    boolean atleastOneIscsiNetwork = networks.size() >= 1;
                    if (!areThereTwoOrMoreNetworks && !isBootFromSanTemplate) {
                        LOGGER.error("Storage must have at least 2 iSCSI Networks configured on 2 separate partitions! Only " + networks.size() + " are configured!");

                        final ServiceTemplateValid componentValid = serverComponent.getComponentValid();
                        componentValid.addMessage(AsmManagerMessages.insufficientNumberOfIsciNetworksForStorageComponent());
                        componentValid.setValid(Boolean.FALSE);
                        serviceTemplate.getTemplateValid().setValid(Boolean.FALSE);
                    }
                    if (!atleastOneIscsiNetwork && isBootFromSanTemplate) {
                        LOGGER.error("Storage must have at least 1 iSCSI Network configured for Boot From SAN template");

                        final ServiceTemplateValid componentValid = serverComponent.getComponentValid();
                        componentValid.addMessage(AsmManagerMessages.insufficientNumberOfIsciNetworksForStorageComponentBootFromSanTemplate());
                        componentValid.setValid(Boolean.FALSE);
                        serviceTemplate.getTemplateValid().setValid(Boolean.FALSE);
                    }
                }
            }
        }
    }
    
    // Gets the unique names for the given storage template
    private static Set<String> getStorageNameList(ServiceTemplateSetting storageTitleSetting) {
        if (storageTitleSetting == null)
            return null;

        HashSet<String> names = new HashSet<String>();
        
        List<ServiceTemplateOption> storageTemplateOptions = storageTitleSetting.getOptions();
        for (ServiceTemplateOption option : storageTemplateOptions) {
            names.add(option.getValue());
        }
        
        return names;
    }

    /**
     * Options for validation.
     */
    public static class ValidationOptions {
        private boolean isDeployment;

        public boolean isCheckForUniqueness() {
            return checkForUniqueness;
        }

        public void setCheckForUniqueness(boolean checkForUniqueness) {
            this.checkForUniqueness = checkForUniqueness;
        }

        private boolean checkForUniqueness;
        private boolean inventoryContainsEM;

        public boolean isDeployment() {
            return isDeployment;
        }

        public void setDeployment(boolean deployment) {
            isDeployment = deployment;
        }

        public boolean isInventoryContainsEM() {
            return inventoryContainsEM;
        }

        public void setInventoryContainsEM(boolean inventoryContainsEM) {
            this.inventoryContainsEM = inventoryContainsEM;
        }

        /**
         * Constructor for options.
         * @param isDeployment  Deployment time
         * @param checkForUniqueness   Check volumes for uniqueness - new deployment or scale down
         * @param inventoryContainsEM   ASM inventory has Element Manager
         */
        public ValidationOptions (boolean isDeployment, boolean checkForUniqueness, boolean inventoryContainsEM) {
            this.isDeployment = isDeployment;
            this.checkForUniqueness = checkForUniqueness;
            this.inventoryContainsEM = inventoryContainsEM;
        }
    }

    public INetworkService getNetworkService() {
        if (networkService == null) {
            networkService = ProxyUtil.getNetworkProxy(); 
        }
        return networkService;
    }

    public void setNetworkService(INetworkService networkService) {
        this.networkService = networkService;
    }

    public ServiceTemplateUtil getServiceTemplateUtil() {
        if (serviceTemplateUtil == null) {
            serviceTemplateUtil = new ServiceTemplateUtil();
        }
        return serviceTemplateUtil;
    }

    public void setServiceTemplateUtil(ServiceTemplateUtil serviceTemplateUtil) {
        this.serviceTemplateUtil = serviceTemplateUtil;
    }

    public OSRepositoryUtil getOsRepositoryUtil() {
        if (osRepositoryUtil == null) {
            osRepositoryUtil = new OSRepositoryUtil();
        }
        return osRepositoryUtil;
    }

    public void setOsRepositoryUtil(OSRepositoryUtil osRepositoryUtil) {
        this.osRepositoryUtil = osRepositoryUtil;
    }


}
