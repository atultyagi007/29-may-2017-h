/**************************************************************************
 *   Copyright (c) 2013 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.app.rest.DeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.app.rest.ServiceTemplateService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
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
import com.dell.asm.asmcore.asmmanager.client.util.PuppetDbUtil;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEmcDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEquallogicDevice;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateUserRefEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.files.CopyFileVisitor;
import com.dell.asm.asmcore.asmmanager.util.files.DeleteDirectoryVisitor;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.asmcore.asmmanager.util.template.adjuster.AdjusterFactory;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.Network;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.dell.pg.orion.security.encryption.IEncryptedString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

public class ServiceTemplateUtil {

    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateUtil.class);

    private static final String CURRENT_TEMPLATE_VERSION = AsmManagerUtil.getAsmVersion();
    private static final String SERVER_TYPE_BLADE = "blade";
    private static final ObjectMapper BACKEND_MAPPER = buildBackendObjectMapper();

    public static final String TEMPLATE_ATTACHMENT_DIR = "/opt/Dell/ASM/templates/";

    private EncryptionDAO encryptionDAO;
    private INetworkService networkService;
    private OSRepositoryUtil osRepositoryUtil;
    private DeviceInventoryDAO deviceInventoryDAO;

    private static ObjectMapper buildBackendObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static String getCurrentTemplateVersion() {
        return CURRENT_TEMPLATE_VERSION;
    }

    /**
     * Default constructor. Don't init variables here - use getters/setters!
     */
    public ServiceTemplateUtil() {
    }

    public static void stripPasswords(ServiceTemplate template, String newValue) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSetting.ServiceTemplateSettingType.PASSWORD.equals(param.getType())) {
                        if (StringUtils.isNotEmpty(param.getValue())) {
                            param.setValue(newValue);
                        }
                    }
                }
            }
        }
    }

    public static List<ServiceTemplateCategory> findResources(ServiceTemplate template,
                                                              ServiceTemplateComponent.ServiceTemplateComponentType componentType,
                                                              String resourceId) {
        if (template == null) {
            throw new IllegalArgumentException("Template is required");
        }
        if (componentType == null) {
            throw new IllegalArgumentException("Component type is required");
        }
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource id is required");
        }

        List<ServiceTemplateCategory> ret = new ArrayList<>();
        for (ServiceTemplateComponent component : template.getComponents()) {
            if (componentType.equals(component.getType())) {
                for (ServiceTemplateCategory resource : component.getResources()) {
                    if (resourceId.equals(resource.getId())) {
                        ret.add(resource);
                    }
                }
            }
        }
        return ret;
    }

    public static ServiceTemplateSetting findParameter(ServiceTemplateCategory resource, String parameterId) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is required");
        }
        if (parameterId == null) {
            throw new IllegalArgumentException("Parameter id is required");
        }
        for (ServiceTemplateSetting parameter : resource.getParameters()) {
            if (parameterId.equals(parameter.getId())) {
                return parameter;
            }
        }
        return null;
    }

    public static Map<String, String> resourceToMap(ServiceTemplateCategory resource) {
        Map<String, String> ret = new HashMap<>();
        for (ServiceTemplateSetting parameter : resource.getParameters()) {
            ret.put(parameter.getId(), parameter.getValue());
        }
        return ret;
    }

    public static String findParameterValue(ServiceTemplate template,
                                            String componentId,
                                            String resourceId,
                                            String parameterId) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            if (StringUtils.equals(componentId, component.getId())) {
                for (ServiceTemplateCategory resource : component.getResources()) {
                    if (StringUtils.equals(resourceId, resource.getId())) {
                        for (ServiceTemplateSetting param : resource.getParameters()) {
                            if (StringUtils.equals(parameterId, param.getId())) {
                                return param.getValue();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String setParameterValue(ServiceTemplate template,
                                    String componentId,
                                    String resourceId,
                                    String parameterId,
                                    String value) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            if (StringUtils.equals(componentId, component.getId())) {
                for (ServiceTemplateCategory resource : component.getResources()) {
                    if (StringUtils.equals(resourceId, resource.getId())) {
                        for (ServiceTemplateSetting param : resource.getParameters()) {
                            if (StringUtils.equals(parameterId, param.getId())) {
                                param.setValue(value);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Searches the specified {@code template} for password values. If non-null, those values
     * are replaced with encrypted string ids. When null the corresponding {@code origTemplate}
     * value is used.
     *
     * @param template The template whose password values should be encrypted.
     * @param origTemplate The template whose password values will be used in place of null
     *                     password values in @{code template}.
     */
    public void encryptPasswords(ServiceTemplate template, ServiceTemplate origTemplate) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSetting.ServiceTemplateSettingType.PASSWORD.equals(param.getType())) {
                        String plaintext = param.getValue();
                        if (!StringUtils.isEmpty(plaintext) && 
                                !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE.equals(plaintext)) {
                            // Replace the plaintext value with an encryption id
                            IEncryptedString encryptedString = getEncryptionDAO().encryptAndSave(plaintext);
                            param.setValue(encryptedString.getId());
                        } else if (origTemplate != null) {
                            // Use the original encryption id, if any
                            if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE.equals(plaintext) || 
                                    (plaintext != null && plaintext.trim().isEmpty())) {
                                param.setValue("");
                            }
                            else {
                                String encryptionId = findParameterValue(origTemplate,
                                        component.getId(), resource.getId(), param.getId());
                                String clonedFromId = component.getClonedFromId();
                                if (encryptionId == null && clonedFromId != null) {
                                    encryptionId = findParameterValue(origTemplate, clonedFromId, resource.getId(),
                                            param.getId());
                                    if (encryptionId != null) {
                                        // Duplicate the string so that the new component has it's own copy;
                                        // otherwise there may be problems when the encryption ids are deleted
                                        // when the service is torn down.
                                        IEncryptedString encryptedString = getEncryptionDAO().findEncryptedStringById(encryptionId);
                                        if (encryptedString == null) {
                                            LOGGER.warn("No encrypted string found for " + encryptionId +
                                                    " from component " + clonedFromId + " resource " +
                                                    resource.getDisplayName() + " parameter " +
                                                    param.getDisplayName());
                                        } else {
                                            plaintext = encryptedString.getString();
                                            if(!ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE.equals(plaintext)) {
                                                IEncryptedString copy = getEncryptionDAO().encryptAndSave(plaintext);
                                                encryptionId = copy.getId();
                                            }
                                        }
                                    }
                                }
                                if (encryptionId != null && 
                                        encryptionId != ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE) {
                                    param.setValue(encryptionId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void encryptPasswords(ServiceTemplate template) {
        encryptPasswords(template, null);
    }

    /**
     * Assuming the specified template has encrypted password values, decrypt them in place.
     *
     * @param template the template
     */
    public void decryptPasswords(ServiceTemplate template) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSetting.ServiceTemplateSettingType.PASSWORD.equals(param.getType())) {
                        String passwordId = param.getValue();
                        IEncryptedString encryptedString = getEncryptionDAO().findEncryptedStringById(passwordId);
                        if (encryptedString!=null)
                            param.setValue(encryptedString.getString());
                    }
                }
            }
        }
    }

    public Set<String> getEncryptionIds(ServiceTemplateComponent component) {
        Set<String> ret = new HashSet<>();
        for (ServiceTemplateCategory resource : component.getResources()) {
            for (ServiceTemplateSetting param : resource.getParameters()) {
                if (ServiceTemplateSetting.ServiceTemplateSettingType.PASSWORD.equals(param.getType())) {
                    if (!StringUtils.isEmpty(param.getValue())) {
                        ret.add(param.getValue());
                    }
                }
            }
        }
        return ret;
    }

    public Set<String> getEncryptionIds(ServiceTemplate template) {
        Set<String> ret = new HashSet<>();
        for (ServiceTemplateComponent component : template.getComponents()) {
            ret.addAll(getEncryptionIds(component));
        }
        return ret;
    }

    /**
     * Assuming all non-null password values are encryption ids, replaces those encryption ids
     * with new values that decrypt to the same plaintext value. Useful when copying a template
     * where we want the new template to have the same passwords, but not point to the same
     * encryption record.
     *
     * @param template The template
     */
    public void duplicatePasswords(ServiceTemplate template) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSetting.ServiceTemplateSettingType.PASSWORD.equals(param.getType())
                            && !StringUtils.isEmpty(param.getValue())) {
                        IEncryptedString encryptedString
                                = getEncryptionDAO().findEncryptedStringById(param.getValue());
                        if (encryptedString!=null) {
                            String plaintext = encryptedString.getString();
                            IEncryptedString copy = getEncryptionDAO().encryptAndSave(plaintext);
                            param.setValue(copy.getId());
                        }
                    }
                }
            }
        }
    }

    public void deleteRemovedEncryptionIds(ServiceTemplate oldTemplate,
                                           ServiceTemplate newTemplate) {
        Set<String> encryptionIdsToDelete = getEncryptionIds(oldTemplate);
        encryptionIdsToDelete.removeAll(getEncryptionIds(newTemplate));
        for (String id : encryptionIdsToDelete) {
            IEncryptedString encryptedString = getEncryptionDAO().findEncryptedStringById(id);
            if (encryptedString!=null)
                getEncryptionDAO().delete(encryptedString);
        }
    }

    public void deleteEncryptionIds(Set<String> encryptionIds) {
        for (String id : encryptionIds) {
            IEncryptedString encryptedString = getEncryptionDAO().findEncryptedStringById(id);
            if (encryptedString!=null)
                getEncryptionDAO().delete(encryptedString);
        }
    }

    public void deleteEncryptionIds(ServiceTemplate template) {
        deleteEncryptionIds(getEncryptionIds(template));
    }

    /**
     * For each setting trim value from whitespaces.
     * @param template
     */
    public static void trimSpaces(ServiceTemplate template) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (param.getValue()!=null) {
                        param.setValue(param.getValue().trim());
                    }
                }
            }
        }
    }

    public static void addDefaultSelectOption(ServiceTemplateSetting setting) {
        ServiceTemplateClientUtil.addDefaultSelectOption(setting);
    }

    public static void addBooleanSelectOption(ServiceTemplateSetting setting) {
        addDefaultSelectOption(setting);

        setting.getOptions().add(new ServiceTemplateOption(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TRUE_NAME,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TRUE_VALUE, null, null));
        setting.getOptions().add(new ServiceTemplateOption(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FALSE_NAME,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FALSE_VALUE, null, null));
    }

    public static void addDeviceInventoryOptions(List<DeviceInventoryEntity> entities, ServiceTemplateSetting setting) {
        if (entities != null && setting != null) {
            for (DeviceInventoryEntity entity : entities) {
                setting.getOptions().add(new ServiceTemplateOption(entity.getServiceTag(),
                                                                   entity.getRefId(),
                                                                   null,
                                                                   null));
            }
        }
    }

    public static void addServerPoolOptions(List<DeviceGroupEntity> deviceGroups, ServiceTemplateSetting serverPool) {
        serverPool.getOptions().add(new ServiceTemplateOption(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID, null, null));
        Long userId = AsmManagerUtil.getUserId();
        if (deviceGroups != null && deviceGroups.size() > 0) {
            for (DeviceGroupEntity group : deviceGroups) {
                if (group.getGroupsUsers() != null && group.getGroupsUsers().size() > 0) {
                    if (group.getGroupsUsers().contains(userId)) {
                        if (group.getSeqId() != null) {
                            serverPool.getOptions().add(
                                    new ServiceTemplateOption(group.getName(), Long.toString(group.getSeqId()), null, null));
                        }
                    }
                }
            }
        }
        serverPool.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID); // set it to the first pool which is the
    }

    /**
     * Find puppet device, return list of volumes.
     *
     * @param refId
     * @return
     */
    public List<ServiceTemplateOption> getCompellentVolumes(String refId) {
        List<ServiceTemplateOption> ret = new ArrayList<>();

        try {
            Map<String, String> data = PuppetModuleUtil.getPuppetDevice(refId);
            ObjectMapper mapper = new ObjectMapper();
            String objectStr = data.get("volume_data");
            if(objectStr != null && objectStr.length() > 0 )
            {
                Map<String, List<Map<String, List<String>>>> parsedJson = null;
                try {
                    parsedJson = mapper.readValue(objectStr, Map.class);
                } catch (IOException e) {
                    LOGGER.error("Cannot get volume data for compellent device id=" + refId, e);
                    return ret;
                }
                List<Map<String, List<String>>> objectList = parsedJson.get("volume");
                if(objectList != null && objectList.size() > 0)
                {
                    //  Missing facts for type, statusinformation, volumetype, storagetype
                    for (Map <String, List<String>>volumeMap : objectList) {
                        String volumeName = volumeMap.get("Name").get(0);
                        ServiceTemplateOption opt = new ServiceTemplateOption(volumeName, volumeName, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, refId);
                        ret.add(opt);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return ret;
    }

    /**
     * Find puppet device, return list of volumes.
     * @param refId
     * @return
     */
    public List<ServiceTemplateOption> getEqlVolumes(String refId) {
        List<ServiceTemplateOption> ret = new ArrayList<>();

        try {
            Map<String, String> data = PuppetModuleUtil.getPuppetDevice(refId);
            PuppetEquallogicDevice eqlDevice = PuppetDbUtil.convertToPuppetEquallogicDevice(data);
            if (eqlDevice == null)
                return ret;


            if (CollectionUtils.isNotEmpty(eqlDevice.getVolumesProperties())) {
                for (PuppetEquallogicDevice.VolumeProperties volumeProp : eqlDevice.getVolumesProperties()) {
                    if (PuppetEquallogicDevice.isVolumeInRecoveryBin(volumeProp)) {
                        LOGGER.warn("Skipped EQL volume since it is 0GB and offline: " + volumeProp.getName());
                    }else{
                        ServiceTemplateOption opt = new ServiceTemplateOption(volumeProp.getName(), volumeProp.getName(), ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, refId);
                        ret.add(opt);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return ret;
    }

    /**
     * Find VNX puppet device, return list of volumes.
     * @param entity
     */
    public void getVNXDetails(DeviceInventoryEntity entity, List<ServiceTemplateOption> pools,
                                                     List<ServiceTemplateOption> volums,
                                    Map<String, ServiceTemplateOption> typesOptMap) {

        if (pools == null || volums == null)
            return;

        try {
            PuppetEmcDevice vnx = PuppetModuleUtil.getPuppetEmcDevice(entity.getRefId());

            if (CollectionUtils.isNotEmpty(vnx.getPools())) {
                for (PuppetEmcDevice.Pool pool : vnx.getPools()) {
                    String poolNnameStr = pool.getName();
                    ServiceTemplateOption opt = new ServiceTemplateOption(poolNnameStr, poolNnameStr,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, entity.getRefId());
                    pools.add(opt);

                    if (CollectionUtils.isNotEmpty(pool.getMlus())) {
                        for (PuppetEmcDevice.Pool.Mlu mlu : pool.getMlus()) {
                            String nameStr = mlu.getName();
                            opt = new ServiceTemplateOption(nameStr, nameStr, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_POOL_ID, poolNnameStr);
                            volums.add(opt);
                        }
                    }
                }
            }

            if (vnx.getAddons() != null) {
                if (vnx.getAddons().isCompressionEnabled()) {
                    ServiceTemplateOption opt = typesOptMap.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_TYPE_COMPRESSED);
                    addToDependencyValue(opt, entity.getRefId());
                }
                if (vnx.getAddons().isThinEnabled()) {
                    ServiceTemplateOption opt = typesOptMap.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_TYPE_THIN);
                    addToDependencyValue(opt, entity.getRefId());
                }
                if (vnx.getAddons().isNonthinEnabled()) {
                    ServiceTemplateOption opt = typesOptMap.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_TYPE_NONTHIN);
                    addToDependencyValue(opt, entity.getRefId());
                }
                if (vnx.getAddons().isSnapEnabled()) {
                    ServiceTemplateOption opt = typesOptMap.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_TYPE_SNAP);
                    addToDependencyValue(opt, entity.getRefId());
                }
            }

        } catch (Exception e) {
            LOGGER.error("Cannot get VNX details for " + entity.getRefId(),e);
        }
    }

    public static void addToDependencyValue(ServiceTemplateOption opt, String value) {
        if (opt == null)
            return;

        if (StringUtils.isNotEmpty(opt.getDependencyValue())) {
            opt.setDependencyValue(opt.getDependencyValue() + "," + value);
        }else {
            opt.setDependencyValue(value);
        }
    }

    /**
     * Populate device list, networks and aggregates for Netapp entry.
     * @param deviceInventoryEntity
     * @param deviceDropdown
     * @param nfsSet
     * @param aggrSet
     */
    public void processNetappData(DeviceInventoryEntity deviceInventoryEntity, ServiceTemplateSetting deviceDropdown,
                                  ServiceTemplateSetting nfsSet, ServiceTemplateSetting aggrSet,
                                  List<ServiceTemplateOption> volumes) {
        String refId = deviceInventoryEntity.getRefId();

        ServiceTemplateOption selOptionDev = new ServiceTemplateOption();
        selOptionDev.setValue(refId);
        selOptionDev.setName(deviceInventoryEntity.getServiceTag());
        deviceDropdown.getOptions().add(selOptionDev);

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(refId);

            String ips = deviceDetails.get("interface_ips");
            if (ips!=null) {
                String[] ipsArr = ips.split(",");
                for (String ip: ipsArr) {
                    ServiceTemplateOption option = new ServiceTemplateOption(ip, ip,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, refId);

                    // devices might have same options, UI needs the list with unique IDs
                    if (nfsSet.getOptions().contains(option)) {
                        ServiceTemplateOption optionOrigin = nfsSet.getOptions().get(nfsSet.getOptions().indexOf(option));
                        optionOrigin.setDependencyValue(optionOrigin.getDependencyValue().concat("," + refId));
                    }else{
                        nfsSet.getOptions().add(option);
                    }
                }
            }

            String aggrs = deviceDetails.get("aggregate_data");
            if (aggrs!=null) {
                Map<String, String> parsedJson = null;
                parsedJson = mapper.readValue(aggrs, Map.class);
                for (String key: parsedJson.keySet()) {
                    String strVolumeData =  parsedJson.get(key);
                    Map<String, String> volumeData = mapper.readValue(strVolumeData, Map.class);
                    String name= volumeData.get("Name");

                    ServiceTemplateOption option = new ServiceTemplateOption(name, name,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, refId);

                    // devices might have same options, UI needs the list with unique IDs
                    if (aggrSet.getOptions().contains(option)) {
                        ServiceTemplateOption optionOrigin = aggrSet.getOptions().get(aggrSet.getOptions().indexOf(option));
                        optionOrigin.setDependencyValue(optionOrigin.getDependencyValue().concat("," + refId));
                    }else{
                        aggrSet.getOptions().add(option);
                    }

                }
            }

            String vols = deviceDetails.get("volume_data");
            if (vols!=null) {
                Map<String, String> parsedJson = null;
                parsedJson = mapper.readValue(vols, Map.class);
                for (String key: parsedJson.keySet()) {
                    String strVolumeData =  parsedJson.get(key);
                    Map<String, String> volumeData = mapper.readValue(strVolumeData, Map.class);
                    String name= volumeData.get("name");

                    ServiceTemplateOption option = new ServiceTemplateOption(name, name,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, refId);

                    // devices might have same options, UI needs the list with unique IDs
                    if (volumes.contains(option)) {
                        ServiceTemplateOption optionOrigin = volumes.get(volumes.indexOf(option));
                        optionOrigin.setDependencyValue(optionOrigin.getDependencyValue().concat("," + refId));
                    }else{
                        volumes.add(option);
                    }

                }
            }

        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Helper to add a setting "Create new ..."
     *
     * @param component component to process
     * @param options list of options to select from
     * @param setId ID of target param  (i.e. volume, datacenter, cluster etc)
     * @param labelCreateNew label in drop down "Create new ..."
     * @param labelNew label in front of new input field "new xxx"
     */
    public static ServiceTemplateSetting processNewSetting(ServiceTemplateComponent component,
                                  Collection<ServiceTemplateOption> options,
                                  String setId,
                                  String labelCreateNew,
                                  String labelNew,
                                  boolean hideFromTemplate) {

        return ServiceTemplateClientUtil.processNewSetting(component, options, setId, labelCreateNew, labelNew, hideFromTemplate);
    }

    /**
     * For templates with drop down "select existing or create new" hide settings if user selects existing resource.
     * @param component
     * @param setId
     */
    public void setDependencyOnCreateNew(ServiceTemplateComponent component, String setId, List<String> exceptions) {
        for (ServiceTemplateSetting set: component.getResources().get(0).getParameters()) {
            if (exceptions!=null && exceptions.contains(set.getId())) continue;

            if (!set.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID) &&
                    !set.getId().equals(setId)){
                if (set.getDependencyTarget()==null) {
                    set.setDependencyTarget(setId);
                    set.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW + "," +
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT + "," +
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE);
                }
            }
        }
    }

    /**
     * Returns server network configuration of specified type.
     * @param component server component
     * @param types List of NetworkType
     * @return
     */
    public List<Network> getServerNetworkByType(ServiceTemplateComponent component, List<NetworkType> types) {
        List<Network> ret = new ArrayList<>();

        if (CollectionUtils.isEmpty(types))
            return ret;

        ServiceTemplateSetting networking = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        if (networking == null) return null;
        com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = deserializeNetwork(networking.getValue());
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
                if (networkIds != null) {
                    for (String networkId : networkIds) {

                        Network networkObject = getNetworkService().getNetwork(networkId);

                        if(networkObject == null){ // Means it's been deleted!
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.networkDoesNotExist(networkId));
                        }

                        for (NetworkType type: types) {
                            if (networkObject.getType().equals(type)) {
                                ret.add(networkObject);
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

    /**
     * Returns server network configuration of specified type.
     * @param component server component
     * @param type NetworkType
     * @return
     */
    public List<Network> getServerNetworkByType(ServiceTemplateComponent component, NetworkType type) {
        List<NetworkType> types= new ArrayList<>();
        types.add(type);
        return getServerNetworkByType(component, types);
    }

    public void encryptPasswordsInConfig(ServiceTemplate template) {
        if (template == null)
            return;

        for (ServiceTemplateComponent component: template.getComponents()) {
            for (ServiceTemplateCategory resource: component.getResources()) {
                for (ServiceTemplateSetting param: resource.getParameters()) {
                    if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_USERS)) {
                        if (param.getValue()!=null) {

                            List<Map<String, String>> users = (List) fromJSON(List.class, param.getValue());
                            if (users != null) {
                                for (Map<String, String> user : users) {
                                    for (String key: user.keySet()) {
                                        if (key.equals("password")) {
                                            IEncryptedString eS = getEncryptionDAO().findEncryptedStringById(user.get(key));
                                            if (eS == null) {
                                                eS = getEncryptionDAO().encryptAndSave(user.get(key));
                                                user.put(key, eS.getId());
                                            }
                                        }
                                    }
                                }
                                param.setValue(toJSON(users));
                            }
                        }
                    }else if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_ALERT_DEST)) {
                        if (param.getValue()!=null) {

                            List<Map<String, String>> traps = (List) fromJSON(List.class, param.getValue());
                            if (traps != null) {
                                for (Map<String, String> trap : traps) {
                                    for (String key: trap.keySet()) {
                                        if (key.equals("communityString")) {
                                            IEncryptedString eS = getEncryptionDAO().findEncryptedStringById(trap.get(key));
                                            if (eS == null) {
                                                eS = getEncryptionDAO().encryptAndSave(trap.get(key));
                                                trap.put(key, eS.getId());
                                            }
                                        }
                                    }
                                }
                                param.setValue(toJSON(traps));
                            }
                        }
                    }
                }
            }
        }
    }

    public void decryptPasswordsInConfig(ServiceTemplate template) {
        if (template == null)
            return;

        for (ServiceTemplateComponent component: template.getComponents()) {
            for (ServiceTemplateCategory resource: component.getResources()) {
                for (ServiceTemplateSetting param: resource.getParameters()) {
                    if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_USERS)) {
                        if (param.getValue()!=null) {

                            List<Map<String, String>> users = (List) fromJSON(List.class, param.getValue());
                            if (users != null) {
                                for (Map<String, String> user : users) {
                                    for (String key: user.keySet()) {
                                        if (key.equals("password")) {
                                            IEncryptedString eS = getEncryptionDAO().findEncryptedStringById(user.get(key));
                                            if (eS != null) {
                                                user.put(key, eS.getString());
                                                getEncryptionDAO().delete(eS);
                                            }
                                        }
                                    }
                                }
                                param.setValue(toJSON(users));
                            }
                        }
                    }else if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_ALERT_DEST)) {
                        if (param.getValue()!=null) {

                            List<Map<String, String>> traps = (List) fromJSON(List.class, param.getValue());
                            if (traps != null) {
                                for (Map<String, String> trap : traps) {
                                    for (String key: trap.keySet()) {
                                        if (key.equals("communityString")) {
                                            IEncryptedString eS = getEncryptionDAO().findEncryptedStringById(trap.get(key));
                                            if (eS != null) {
                                                trap.put(key, eS.getString());
                                                getEncryptionDAO().delete(eS);
                                            }
                                        }
                                    }
                                }
                                param.setValue(toJSON(traps));
                            }
                        }
                    }
                }
            }
        }
    }

    private Object fromJSON(Class claz, String source) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(source, claz);
        } catch (IOException e) {
            return null;
        }
    }

    private String toJSON(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * Zip attachments and template content, encrypt zip file and return base64 encoded.
     * @param template
     * @param password
     * @return file name
     * @throws AsmManagerRuntimeException
     */
    public String exportTemplate(ServiceTemplate template, String password) throws AsmManagerRuntimeException {
        String stringTemplate = MarshalUtil.marshal(template);

        ensureTemplateAttachmentsFolderExists();

        // This is the tar file name.
        long currenttime = System.currentTimeMillis();

        String filename = "template.tmp";
        String foldername = "/tmp/template_" + currenttime;
        String zipFilename = foldername + ".zip";
        String[] command;
        CommandResponse cmdresponse = null;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();

        Path tempFolder = Paths.get(foldername);
        Path zipFilePath = Paths.get(zipFilename);

        if (Files.exists(tempFolder)) {
            try {
                Files.delete(tempFolder);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot delete directory: " + tempFolder);
            }
        }

        try {
            Files.createDirectory(tempFolder);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new AsmManagerRuntimeException("Cannot create directory: " + tempFolder);
        }

        writeToFile(foldername, filename, stringTemplate);

        // zip all attachments
        File attachPath = new File(TEMPLATE_ATTACHMENT_DIR + template.getId());
        if (attachPath.exists() && attachPath.isDirectory() && attachPath.list().length>0) {
            try {
                command = new String[] { "/bin/bash", "-c", "/bin/cp -r " + attachPath.getAbsolutePath() + "/* " + tempFolder};
                cmdresponse = cmdRunner.runCommandWithConsoleOutput(command);
            } catch (Exception e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot copy attachments to temp folder: " + tempFolder);
            }
            if (!cmdresponse.getReturnCode().equals("0")) {
                throw new AsmManagerRuntimeException("Copying of template attachments failed");
            }
        }

        command = new String[] { "/bin/bash", "-c", "/usr/bin/zip -j " + zipFilename  + " " + tempFolder + "/*" };

        try {
            cmdresponse = cmdRunner.runCommandWithConsoleOutput(command);
        } catch (Exception e) {
            LOGGER.error("Command execution failed: " + command, e);
            throw new AsmManagerRuntimeException(e);
        } finally {
            try {
                FileUtils.deleteDirectory(new File(tempFolder.toString()));
            } catch (IOException e) {
                LOGGER.error("Cannot delete temp folder: " + tempFolder, e);
            }
        }
        if (!cmdresponse.getReturnCode().equals("0")) {
            throw new AsmManagerRuntimeException("Zipping of template attachments failed");
        }

        command = new String[] { "/bin/bash", "-c", "echo " + ExecuteSystemCommands.sanitizeShellArgument(password) + " | gpg --batch -q --passphrase-fd 0 -c " + ExecuteSystemCommands.sanitizeShellArgument(zipFilename) };
        try {
            cmdresponse = cmdRunner.callCommand(command);
        } catch (Exception e) {
            LOGGER.error("Command execution failed: " + command, e);
            throw new AsmManagerRuntimeException(e);
        } finally {
            try {
                Files.delete(zipFilePath);
            } catch (IOException e) {
                LOGGER.error("Cannot delete zip file: " + zipFilePath, e);
            }
        }

        LOGGER.debug("GPG return code: " + cmdresponse.getReturnCode() + " Return message: " + cmdresponse.getReturnMessage());
        if (!cmdresponse.getReturnCode().equals("0")) {
            throw new AsmManagerRuntimeException("Encryption of template failed");
        }

        return zipFilename + ".gpg";
    }

    private static void ensureTemplateAttachmentsFolderExists() {
        Path tempFolder = Paths.get(TEMPLATE_ATTACHMENT_DIR);

        if (!Files.exists(tempFolder)) {
            try {
                Files.createDirectory(tempFolder);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot create directory: " + tempFolder);
            }
        }

    }

    /**
     * Writes the script to a file.
     *
     * @param scriptPath script path
     * @param scriptFilename script filename
     * @param data script
     * @return script path and filename
     */
    public static String writeToFile(String scriptPath, String scriptFilename, Object data) throws AsmManagerRuntimeException {
        StringBuilder builder = new StringBuilder();
        builder.append(scriptPath).append(System.getProperty("file.separator")).append(scriptFilename);

        // Write the data in script file.
        File file = new File(builder.toString());

        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);
            if (data instanceof String) {
                IOUtils.write((String) data, dos);
            }else if (data instanceof byte[]) {
                IOUtils.write((byte[])data, dos);
            }else{
                throw new AsmManagerRuntimeException("Write file: Data must be string or byte[]");
            }

            ExecuteSystemCommands cmd = ExecuteSystemCommands.getInstance();
            // Change the permission of the script.
            cmd.callCommand(new String[] { "/bin/chmod", "0770", builder.toString() });
        }catch(Exception e) {
            throw new AsmManagerRuntimeException(e);
        } finally {
            try {
                closeStreamQuitely(fos);
                closeStreamQuitely(dos);
            } catch (Exception e) {
                LOGGER.warn("Unable to close the file stream.");
            }
        }

        return builder.toString();
    }

    /**
     * Decrypt, unzip and copy attachments to template folder.
     * @param encryptedTemplateFileData encrypted template file data
     * @param password
     * @return
     * @throws AsmManagerRuntimeException
     */
    public static ServiceTemplate importTemplate(byte[] encryptedTemplateFileData, String password) throws AsmManagerRuntimeException {

        ServiceTemplate svc = null;

        if (encryptedTemplateFileData != null && encryptedTemplateFileData.length > 0) {
            ensureTemplateAttachmentsFolderExists();

            // This is the tar file name.
            long currenttime = System.currentTimeMillis();

            String filename = "template_" + currenttime + ".gpg";
            String restoredFile = "template_" + currenttime + ".zip";
            String tempFolder = "/tmp/template_" + currenttime;

            writeToFile("/tmp", filename, encryptedTemplateFileData);

            CommandResponse cmdresponse = null;
            ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();

            String[] command = new String[] { "/bin/bash", "-c", "echo " + ExecuteSystemCommands.sanitizeShellArgument(password) + " | gpg --batch -q --utf8-strings --output " + ExecuteSystemCommands.sanitizeShellArgument("/tmp/" + restoredFile) + " --passphrase-fd 0 --decrypt " + ExecuteSystemCommands.sanitizeShellArgument("/tmp/" + filename) };

            try {
                cmdresponse = cmdRunner.callCommand(command);
            } catch (Exception e) {
                LOGGER.error("GPG execution failed", e);
                throw new AsmManagerRuntimeException(e);
            } finally {
                File file = new File("/tmp/" + filename);
                file.delete();
            }

            LOGGER.debug("GPG return code: " + cmdresponse.getReturnCode() + " Return message: " + cmdresponse.getReturnMessage());
            if (!cmdresponse.getReturnCode().equals("0")) {
                throw new AsmManagerRuntimeException("Decryption of template failed, invalid file or password");
            }

            String encFil = "/tmp/" + restoredFile;

            // unzip all attachments
            command = new String[] { "/usr/bin/unzip", encFil, "-d" + tempFolder };

            try {
                cmdresponse = cmdRunner.runCommandWithConsoleOutput(command);
            } catch (Exception e) {
                LOGGER.error("Command execution failed: " + command, e);
                throw new AsmManagerRuntimeException(e);
            }

            if (!cmdresponse.getReturnCode().equals("0")) {
                throw new AsmManagerRuntimeException("UnZipping of template attachments failed");
            }

            try {
                Files.delete(Paths.get(encFil));
            } catch (IOException e) {
                LOGGER.error("Cannot delete file " + encFil, e);
            }

            // read template content
            File file = new File(tempFolder + "/template.tmp");
            String content = null;
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                content = IOUtils.toString(is);
            } catch (IOException e) {
                throw new AsmManagerRuntimeException(e);
            } finally {
                if (is != null)
                    IOUtils.closeQuietly(is);
            }
            file.delete();

            svc = MarshalUtil.unmarshal(ServiceTemplate.class, content);
            if (svc == null) {
                throw new AsmManagerRuntimeException("Template content is null. Bad import file.");
            }

            // copy
            Path attachPath = Paths.get(TEMPLATE_ATTACHMENT_DIR + svc.getId());
            if (Files.exists(attachPath)) {
                svc.setId(UUID.randomUUID().toString());
                attachPath = Paths.get(TEMPLATE_ATTACHMENT_DIR + svc.getId());
            }
            try {
                Files.createDirectory(attachPath);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot create directory: " + attachPath);
            }

            File tmpDir = new File(tempFolder);
            if (tmpDir.exists() && tmpDir.isDirectory()) {
                if (tmpDir.list().length > 0) {
                    try {
                        Files.walkFileTree(tmpDir.toPath(), new CopyFileVisitor(attachPath));
                    } catch (Exception e) {
                        LOGGER.error(e);
                        throw new AsmManagerRuntimeException("Cannot copy attachments to template folder: " + attachPath);
                    }
                }

                // cleanup
                try {
                    FileUtils.deleteDirectory(tmpDir);
                } catch (IOException e) {
                    LOGGER.error("Cannot delete temp folder: " + tempFolder, e);
                }
            }
        }

        return svc;
    }

    /**
     * Utility method to close the input/output stream
     *
     * @param cs
     */
    public static void closeStreamQuitely(Closeable cs) {
        try {
            if (cs != null) {
                cs.close();
            }
        } catch (Exception e) {
            LOGGER.info("Can't close Stream");
        }
    }

    public boolean hasNetworks(ServiceTemplateSetting setting) {
        if (setting.getValue()==null) {
            return false;
        }
        try {

            com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = deserializeNetwork(setting.getValue());
            if (networkConfig == null) {
                return false;
            }

            List<Interface> interfaces = networkConfig.getUsedInterfaces();
            for (Interface interfaceObject : interfaces) {
                List<Partition> partitions = interfaceObject.getPartitions();
                for (Partition partition : partitions) {
                    if (partition.getNetworks()!=null && partition.getNetworks().size()>0)
                        return true;
                }
            }


        }catch(Exception e) {
            LOGGER.error("Check for networks failed", e);
            return true;
        }
        return false;
    }

    public static void copyAttachments(String oldId, String id) throws AsmManagerRuntimeException, IOException {
        validateId(oldId);
        validateId(id);

        Path newAttachPath = Paths.get(TEMPLATE_ATTACHMENT_DIR + id);
        if (!Files.exists(newAttachPath)) {
            try {
                Files.createDirectory(newAttachPath);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot create directory: " + newAttachPath.toString());
            }
        }

        File oldDir = new File(TEMPLATE_ATTACHMENT_DIR + oldId);
        if (oldDir.isDirectory() && oldDir.list().length > 0) {
            try {
                Files.walkFileTree(oldDir.toPath(), new CopyFileVisitor(newAttachPath));
            } catch (Exception e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot copy attachments to template folder: " + newAttachPath.toString());
            }
        }
    }

    /**
     * Move template attachments from one location to another
     * @param oldId
     * @param id
     */
    public static void moveAttachments(String oldId, String id) throws AsmManagerRuntimeException, IOException {
        validateId(oldId);
        validateId(id);

        Path attachPath = Paths.get(TEMPLATE_ATTACHMENT_DIR + id);
        if (!Files.exists(attachPath)) {
            try {
                Files.createDirectory(attachPath);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot create directory: " + attachPath);
            }
        }

        File oldDir = new File(TEMPLATE_ATTACHMENT_DIR + oldId);
        if (oldDir.isDirectory() && oldDir.list().length > 0) {
            try {
                Files.walkFileTree(oldDir.toPath(), new CopyFileVisitor(attachPath));
            } catch (Exception e) {
                LOGGER.error(e);
                throw new AsmManagerRuntimeException("Cannot move attachments to template folder: " + attachPath);
            }
        }

        // cleanup
        try {
            FileUtils.deleteDirectory(oldDir);
        } catch (IOException e) {
            LOGGER.error("Cannot delete template folder: " + TEMPLATE_ATTACHMENT_DIR + oldId, e);
        }

    }

    public static void deleteAttachments(String id) {
        if (id != null) {
            validateId(id);

            ensureTemplateAttachmentsFolderExists();

            try {
                Path directory = Paths.get(TEMPLATE_ATTACHMENT_DIR + id);
                if (Files.exists(directory) && Files.isDirectory(directory)) {
                    Files.walkFileTree(directory, new DeleteDirectoryVisitor());
                }
            } catch (Exception e) {
                LOGGER.error("Exception occurred deleting directory during cleanup.", e);
            }
        }
    }

    /**
     * return the task associated with a repo
     * @param repoName
     */
    public String findTask(String repoName){
        try {
            ObjectMapper mapper = new ObjectMapper();
            WebClient client = WebClient.create(AsmManagerApp.razorApiUrl);
            client.accept("application/json");
            String json = client.path("collections/repos").get(String.class);
            String repoJson = client.replacePath("/collections/repos/" + repoName).get(String.class);
            Map<String, Object> repoData = (HashMap<String, Object>) mapper.readValue(repoJson, HashMap.class);
            String task = (String) ((LinkedHashMap) repoData.get("task")).get("name");
            return task;
        }
        catch (IOException e) {
            LOGGER.error(e);
            throw new AsmManagerRuntimeException("Could not find task attached to repo " + repoName);
        }

    }

    //Helper function that just makes it easier for us to know what task a repo is assigned to, without needing to make rest calls for each server component
    //Used for many of the checks, such as hasESX.
    public Map<String, String> mapReposToTasks(){
        List<RazorRepo> repos = getOsRepositoryUtil().getRazorOSImages(true);
        Map<String, String> repoMap = new HashMap<String, String>();
        for(RazorRepo r : repos) {
            repoMap.put(r.getName(), r.getTask());
        }
        return repoMap;
    }

    /**
     * Populates the OS image drop down, and returns back a map of the dependency target strings for each OS.
     * @param razorImages List of RazorRepo images
     * @param bareMetal True exclude esxi values
     * @return map of OS image task to OS image names
     */
    public static Map<String, String> processRazorImagesTargets(List<RazorRepo> razorImages, boolean bareMetal) {
        Map<String, String> imageTargets = new HashMap<>();
        if (razorImages != null && razorImages.size() > 0) {
            Map<String, List<String>> tempTargets = new HashMap<>();
            for( RazorRepo image : razorImages) {
                //This check may not be necessary to have, but shouldn't hurt anything. Just ensures no unsupported repo/tasks are attempted to be accessed in the map.
                switch(image.getTask()) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE:
                    //Fallthrough to add to tempTargets if baremetal is false
                    if (bareMetal) {
                        break;
                    }
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE:
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE:
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE11_VALUE:
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE12_VALUE:
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE:
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE:
                    List<String> names = tempTargets.get(image.getTask());
                    if (names == null) {
                        names = new ArrayList<>();
                        tempTargets.put(image.getTask(),names);
                    }
                    names.add(image.getName());
                    break;
                default:
                    break;
                }
            }
            //Populate the map with the comma-separated lists of the targets for easier use later.
            for(Map.Entry<String,List<String>> entry : tempTargets.entrySet()) {
                if (entry.getValue() != null && entry.getValue().size() > 0) {
                    String targets = Joiner.on(",").join(entry.getValue());
                    imageTargets.put(entry.getKey(), targets);
                }
            }
        }
        return imageTargets;
    }

    /**
     * Returns back a map of the dependency target strings for Linux-type OS's (excludes esxi).
     * @param razorImages List of RazorRepo images
     * @return map of OS image task to OS image names
     */
    public static Map<String, String> getLinuxRazorImageTargets(List<RazorRepo> razorImages) {
        Map<String, String> imageTargets = new HashMap<>();
        Map<String, ArrayList<String>> tempTargets = new HashMap<>();
        for (RazorRepo image : razorImages) {
            switch(image.getTask()) {
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE:
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE:
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE11_VALUE:
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE12_VALUE:
                ArrayList<String> names = tempTargets.get(image.getTask());
                if (names == null) {
                    names = new ArrayList<>();
                    tempTargets.put(image.getTask(), names);
                }
                names.add(image.getName());
                break;
            default:
                break;
            }
        }
        for(Map.Entry<String, ArrayList<String>> entry : tempTargets.entrySet()) {
            if (entry.getValue() != null && entry.getValue().size() > 0) {
                String targets = Joiner.on(",").join(entry.getValue());
                imageTargets.put(entry.getKey(), targets);
            }
        }
        return imageTargets;
    }

    public static void addOSImagesAsOptions(final List<RazorRepo> razorImages, final Map<String, String> repoNames, ServiceTemplateSetting setting) {
        if (setting != null) {
            for (RazorRepo image : razorImages) {
                String displayName = (repoNames.get(image.getName()) == null) ? image.getName() : repoNames.get(image.getName());
                setting.getOptions().add(new ServiceTemplateOption(displayName, image.getName(), null, null));
            }
        }
    }

    //Called when getting a template, to ensure the os image dropdowns/dependencies are up to date.
    public void updateRazorTargets(ServiceTemplate svc){
        List<RazorRepo> razorImages = getOsRepositoryUtil().getRazorOSImages(true);
        Map<String, String> imageTargets = processRazorImagesTargets(razorImages, false);
        String windowsValues = StringUtils.defaultIfBlank(
                Joiner.on(",").skipNulls().join(
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE)),
                null);
        String allLinuxValues = StringUtils.defaultIfBlank(
                Joiner.on(",").skipNulls().join(
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE11_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE12_VALUE)),
                null
        );

        String windows2012Values = imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE);
        String ntpValues = Joiner.on(",").skipNulls().join(allLinuxValues, windowsValues, imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE ));

        for(ServiceTemplateComponent comp : svc.getComponents()){
            if(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(comp.getType()) || ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(comp.getType())){
                if(windowsValues != null){
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_VERSION_ID, windowsValues);
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_PROD_KEY_ID, windowsValues);
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_ADMIN_LOGIN_ID, windowsValues);
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID, windowsValues);
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID, windowsValues);
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_DN_ID, windowsValues);
                }
                if(allLinuxValues != null){
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_TIMEZONE_ID, allLinuxValues);
                }
                if(windows2012Values != null){
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL, windows2012Values);
                }
                if(ntpValues != null){
                    setDependencyValue(comp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID, ntpValues);
                }
            }
        }
    }

    /**
     * method that just holds logic for updating certain values that may be hidden away from the user, but need to be passed through.
     * @param svcTemplate
     */
    public void setHiddenValues(ServiceTemplate svcTemplate) {
        //Update os_image_type to reflect the task associated with the repo selected.
        Map<String, String> tasks = mapReposToTasks();
        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                    ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                
                // determine if esxi or hyperV and set internal flag
                component.isHypervisor(tasks);

                ServiceTemplateCategory templateResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                if (templateResource != null) {
                    ServiceTemplateSetting repo = templateResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                    ServiceTemplateSetting hypervInstall = templateResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
                    boolean hvValid = (hypervInstall != null) && requiredDependenciesSatisfied(component, hypervInstall);

                    ServiceTemplateSetting imageType = templateResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
                    if (imageType != null) {
                        if (hvValid && "true".equals(hypervInstall.getValue()))
                            imageType.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERV_VALUE);
                        else {
                            if (repo != null) {
                                String task = tasks.get(repo.getValue());
                                imageType.setValue(task);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<String> getAttemptedServers(ServiceTemplateComponent component) {
        List<String> servers = new ArrayList<>();
        ServiceTemplateSetting setting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS);
        if (setting!=null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (StringUtils.isNotEmpty(setting.getValue()))
                    servers = mapper.readValue(setting.getValue(), List.class);
            } catch (IOException e) {
                LOGGER.error("Cannot parse SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS value: " + setting.getValue(), e);
            }
        }

        return servers;
    }

    public List<String> addAttemptedServers(ServiceTemplateComponent component, String refId) {
        List<String> servers = new ArrayList<>();
        ServiceTemplateSetting setting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS);
        if (setting==null) {
            ServiceTemplateCategory category = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE);
            if (category == null)
                category = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
            setting = ServiceTemplateService.addDeploymentAttemptedSetting(category);
            if (setting==null) {
                throw new AsmManagerRuntimeException("No category SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE or SERVICE_TEMPLATE_SERVER_OS_RESOURCE in provided template component");
            }

        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (StringUtils.isNotEmpty(setting.getValue()))
                servers = mapper.readValue(setting.getValue(), List.class);

            // ignore the same server - retry use case
            if (!servers.contains(refId))
                servers.add(refId);
            setting.setValue(mapper.writeValueAsString(servers));
        } catch (IOException e) {
            LOGGER.error("Cannot parse SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS value: " + setting.getValue(), e);
            throw new AsmManagerRuntimeException("failed to add to attempted servers");
        }
        return servers;
    }

    public static com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration findNetworkConfiguration(ServiceTemplateComponent component) {
        ServiceTemplateCategory networkingResource = component.getTemplateResource(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);
        if (networkingResource != null) {
            ServiceTemplateSetting parameter = networkingResource.getParameter(
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
            if (parameter != null) {
                return parameter.getNetworkConfiguration();
            }
        }
        return null;
    }

    private void setDependencyValue(ServiceTemplateComponent comp, String settingId, String value){
        ServiceTemplateSetting setting = comp.getTemplateSetting(settingId);
        if(setting != null)
            setting.setDependencyValue(value);
    }

    public static boolean isConfirmPassword(String parameterId) {
        return ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID.equals(parameterId)
                || ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_DOMAIN_CONFIRM_PASSWORD_ID.equals(parameterId)
                || ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID.equals(parameterId);
    }

    public void stripConfirmPasswordParameters(ServiceTemplate template) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                Iterator<ServiceTemplateSetting> iterator = resource.getParameters().iterator();
                while (iterator.hasNext()) {
                    ServiceTemplateSetting param = iterator.next();
                    if (ServiceTemplateUtil.isConfirmPassword(param.getId())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public static Pair<ServiceTemplateCategory, String> getServerSourceValue(ServiceTemplateComponent component, String serverSource) {
        // find SERVICE_TEMPLATE_SERVER_SOURCE; may be in either asm::idrac or asm::server
        for (ServiceTemplateCategory resource : component.getResources()) {
            ServiceTemplateSetting param = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
            if (param != null && StringUtils.equals(serverSource, param.getValue())) {
                ServiceTemplateSetting valueParam = null;
                switch (serverSource) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL:
                    valueParam = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL:
                    valueParam = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION);
                    break;
                default:
                    LOGGER.warn("Unknown server source parameter " + serverSource
                            + " for component " + component);
                    valueParam = null;
                    break;
                }

                if (valueParam == null || StringUtils.isBlank(valueParam.getValue())) {
                    return null;
                } else {
                    return Pair.of(resource, valueParam.getValue());
                }
            }
        }
        return null;
    }

    public static List<ServiceTemplateComponent> getServersFromSource(List<ServiceTemplateComponent> components,
                                                                      String serverSource) {
        List<ServiceTemplateComponent> ret = new ArrayList<>();
        for (ServiceTemplateComponent component : components) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                Pair<ServiceTemplateCategory, String> pair = getServerSourceValue(component, serverSource);
                if (pair != null) {
                    ret.add(component);
                }
            }
        }
        return ret;
    }

    /**
     * Ensure all service template components have associations with components that have associations
     * with themselves
     *
     * @param svcTemplate
     */
    public static void ensureRelatedComponents(ServiceTemplate svcTemplate) {
        //build component map for easy access to all components on template
        Map<String, ServiceTemplateComponent> componentsMap = svcTemplate.fetchComponentsMap();

        // loop through map confirming all associated components have the same relationship
        for (Map.Entry<String, ServiceTemplateComponent> entry : componentsMap.entrySet()) {
            ServiceTemplateComponent currentComponent = entry.getValue();
            if (currentComponent != null && !currentComponent.getAssociatedComponents().isEmpty()) {
                Set<String> unmappedComponents = new HashSet<>();
                // loop through associated component entries and cofirm they have association with current component
                for (String associatedId : currentComponent.getAssociatedComponents().keySet()) {
                    ServiceTemplateComponent associatedComponent = componentsMap.get(associatedId);
                    if (associatedComponent != null) {
                        // if current component id not found in associated component's associated component keyset then add association
                        if (!associatedComponent.getAssociatedComponents().keySet().contains(currentComponent.getId())) {
                            LOGGER.warn("Adding missed associated component link " + currentComponent.getId() + " -> " + associatedComponent.getId());
                            associatedComponent.addAssociatedComponentName(currentComponent.getId(), currentComponent.getName());
                        }
                    } else {
                        unmappedComponents.add(associatedId);
                    }
                }
                if (!unmappedComponents.isEmpty()) {
                    for (String removeId : unmappedComponents) {
                        currentComponent.removeAssociatedComponent(removeId);
                    }
                }
            }
        }
    }

    public void setOtherDependencies(ServiceTemplateComponent compellent) {

        //here we set dependencies for fw version and for configure SAN support

        Double version = null;
        List<String> compRefIdList =  new ArrayList<String>();
        List<ServiceTemplateCategory> compellentResources = compellent.getResources();
        DeviceInventoryDAO devInv = new DeviceInventoryDAO();
        boolean noCompellentInList = false;       

        for (ServiceTemplateCategory resource : compellentResources) {
            List<ServiceTemplateSetting> params = resource.getParameters();
            for (ServiceTemplateSetting parameter : params) {
                if (parameter.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID)) {
                    List<ServiceTemplateOption> compList = parameter.getOptions();
                    if (compList.size() < 2) {
                        noCompellentInList = true;
                    } else {                      
                        for (ServiceTemplateOption comp : compList){                           
                            if (!(comp.getName().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT))){
                                Set<FirmwareDeviceInventoryEntity> compFwSet = devInv.getFirmwareDeviceInventoryByRefId(comp.getValue());
                                for (FirmwareDeviceInventoryEntity compFw : compFwSet) {
                                    version = new Double(compFw.getVersion().substring(0, 3));
                                    if (version != null 
                                            && version >= ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_ESX6_TARGET_VERSION) {
                                        compRefIdList.add(comp.getValue());
                                    }                            
                                }
                            }                       
                        }
                    }
                }
                if (parameter.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_OS_ID)) {
                    List<ServiceTemplateOption> compList = parameter.getOptions();
                    for (ServiceTemplateOption comp : compList){
                        if (comp.getName().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_ESX6_NAME) && !noCompellentInList){
                            comp.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                            comp.setDependencyValue(compRefIdList.size() == 0 ? "--" : Joiner.on(",").skipNulls().join(compRefIdList));
                        }
                    }
                }
                if (parameter.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CONFIGURE_SAN)) {
                    parameter.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID);
                    parameter.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_FIBRE_CHANNEL);
                }
            }
        } 
    }

    /**
     * For encoded JSON param s upgrade or convert parameter values to latest version.
     * @param parameter
     * @param templateVersion reserved for future usage
     */
    public static void upgradeParameter(ServiceTemplateSetting parameter, String templateVersion) {
        if (parameter.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID)) {
            com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration configuration = null;
            if (StringUtils.isBlank(parameter.getValue())) {
                LOGGER.warn("upgradeParameter: Network configuration is blank, nothing to convert");
            } else {
                try {
                    configuration = deserializeNetwork(parameter.getValue());
                    upgradeNetworkConfiguration(configuration);
                    parameter.setValue(BACKEND_MAPPER.writeValueAsString(configuration));
                } catch (IOException e) {
                    LOGGER.error("upgradeParameter: Network configuration has invalid value: " + parameter.getValue(), e);
                }
            }
        }
    }

    /**
     * Upgrade newtork config - move fabrics to interfaces, set servertype to null.
     * @param configuration
     */
    public static void upgradeNetworkConfiguration(com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration configuration) {

        if (configuration!=null) {
            List<Fabric> interfaces = new ArrayList<Fabric>();

            if (StringUtils.isNotBlank(configuration.getServertype())) {
                if (SERVER_TYPE_BLADE.equals(configuration.getServertype())) {
                    for (Fabric f : configuration.getFabrics()) {
                        if (f.isUsedforfc())
                            f.setFabrictype(Fabric.FC_TYPE);
                        else
                            f.setFabrictype(Fabric.ETHERNET_TYPE);
                        if (f.isEnabled())
                            interfaces.add(f);
                    }
                } else {
                    for (Fabric f : configuration.getInterfaces()) {
                        if (f.isUsedforfc())
                            f.setFabrictype(Fabric.FC_TYPE);
                        else
                            f.setFabrictype(Fabric.ETHERNET_TYPE);
                        if (f.isEnabled())
                            interfaces.add(f);
                    }
                }

                configuration.setServertype(null);
                configuration.getFabrics().clear();
                configuration.setInterfaces(interfaces);
            }
        }
    }

    /**
     * This method will remove any unused Networks that may have been set and left before saving the template.
     * @param serviceTemplate
     * @throws JsonProcessingException
     */
    public static void clearUnusedNetworks(ServiceTemplate serviceTemplate) throws JsonProcessingException{
        if(serviceTemplate.getComponents() != null){
            for(ServiceTemplateComponent component : serviceTemplate.getComponents()){
                if(component.getType() == ServiceTemplateComponentType.SERVER) {
                    for (ServiceTemplateCategory resource: component.getResources()) {
                        ServiceTemplateSetting networkingSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                        if (networkingSetting != null) {
                            com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = deserializeNetwork(networkingSetting.getValue());
                            if ( networkConfig != null) {
                                networkConfig.clearUnusedNetworkSettings();
                                String json = serializeNetwork(networkConfig);
                                networkingSetting.setValue(json);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Currently uses ObjectMapper. Will NOT throw exception but return null if json is wrong.
     * It does NOT require adding "networkConfiguration:" to JSON as for MarshalUtil.fromJSON
     * @param value
     * @return
     */
    public static com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration deserializeNetwork(String value) {
        try {
            return BACKEND_MAPPER.readValue(value, com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration.class);
            //String configString = "{ \"networkConfiguration\" : " + value + "}";
            //return MarshalUtil.fromJSON(com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration.class, configString);
        } catch (IOException e) {
            LOGGER.error("Network configuration has invalid value: " + value);
        }
        return null;
    }

    /**
     * Serialize the object hierarchy and set it as the value for the network configuration setting
     * @param networkConfiguration
     * @return
     */
    public static String serializeNetwork(com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfiguration){
        String serializedNetwork = null;
        try
        {
            StringWriter sw = new StringWriter();
            BACKEND_MAPPER.writeValue(sw, networkConfiguration);
            serializedNetwork = sw.toString();
        }
        catch (IOException e)
        {
            LOGGER.error("Exception seen while marshaling network configuration. ", e);
        }
        return serializedNetwork;
    }

    /**
     * If dependency target is not null, verify dependency value satisfies template.
     * If target is null returns true (no dependency, setting is actual).
     * @param component
     * @param setting
     * @return
     */
    public static boolean checkDependency(ServiceTemplateComponent component, ServiceTemplateSetting setting  )
    {
        if (StringUtils.isNotEmpty(setting.getDependencyTarget())) {
            ServiceTemplateSetting depTarget = component.getTemplateSetting(setting.getDependencyTarget());
            return (depTarget != null && StringUtils.contains(setting.getDependencyValue(), depTarget.getValue()));
        }
        return true;
    }


    /**
     * Update referenced template components with
     * @param refTmpl Referenced template
     * @param defaultTmpl Default template
     * @param componentType component type
     */
    public static void refineComponents(ServiceTemplate refTmpl, ServiceTemplate defaultTmpl, String componentType) {
        if (refTmpl == null)
            return;

        // two cases, if default template is null - we are editing existing template
        if (defaultTmpl == null) {
            defaultTmpl = refTmpl;
        }
        for (ServiceTemplateComponent refineComponent : defaultTmpl.getComponents()) {
            if (componentType == null || (refineComponent.getType() != null && (componentType.equalsIgnoreCase(refineComponent.getType().getLabel())))) {
                AdjusterFactory.getRefiner(refineComponent.getType()).refine(refineComponent, refTmpl);
            }
        }
    }

    /**
     * Get the storage components associated with a cluster component.
     * Cluster must have associated server components.
     * @param component Cluster component
     * @param referencedTemplate Referenced Template
     * @return Set<ServiceTemplateComponent>
     */
    public static List<ServiceTemplateComponent> getAssociatedStorageComponentsFromCluster(ServiceTemplateComponent component, ServiceTemplate referencedTemplate) {
        Set<ServiceTemplateComponent> foundComponents = new HashSet<>();

        if (!ServiceTemplateComponentType.CLUSTER.equals(component.getType())) {
            return new ArrayList<>();
        }

        final Set<String> relClusterComps = component.getAssociatedComponents().keySet();

        if (CollectionUtils.isNotEmpty(relClusterComps)) {
            final Map<String, ServiceTemplateComponent> componentMap = referencedTemplate.fetchComponentsMap();
            for (String key : relClusterComps) {
                ServiceTemplateComponent relClusterComp = componentMap.get(key);
                if (relClusterComp != null && ServiceTemplateComponentType.SERVER.equals(relClusterComp.getType())) {
                    Set<String> relServerComps = relClusterComp.getAssociatedComponents().keySet();
                    if (CollectionUtils.isNotEmpty(relServerComps)) {
                        for (String serverKey : relServerComps) {
                            ServiceTemplateComponent relServerComp = componentMap.get(serverKey);
                            if (relServerComp != null && ServiceTemplateComponentType.STORAGE.equals(relServerComp.getType())) {
                                foundComponents.add(relServerComp);
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(foundComponents);
    }

    /**
     * Get the cluster component associated with a storage component.
     * Cluster must have associated server components.
     * @param storageComponent Cluster component
     * @param componentMap Map<String, ServiceTemplateComponent>
     * @return Set<ServiceTemplateComponent>
     */
    public static ServiceTemplateComponent getAssociatedClusterComponentFromStorage(ServiceTemplateComponent storageComponent, Map<String, ServiceTemplateComponent> componentMap) {
        if (!ServiceTemplateComponentType.STORAGE.equals(storageComponent.getType())) {
            return null;
        }

        final Set<String> relServerCompKeys = storageComponent.getAssociatedComponents().keySet();

        if (CollectionUtils.isNotEmpty(relServerCompKeys)) {
            for (String key : relServerCompKeys) {
                ServiceTemplateComponent relServerComp = componentMap.get(key);
                if (relServerComp != null && ServiceTemplateComponentType.SERVER.equals(relServerComp.getType())) {
                    Set<String> relClusterCompKeys = relServerComp.getAssociatedComponents().keySet();
                    if (CollectionUtils.isNotEmpty(relClusterCompKeys)) {
                        for (String clusterKey : relClusterCompKeys) {
                            ServiceTemplateComponent relClusterComp = componentMap.get(clusterKey);
                            if (relClusterComp != null && ServiceTemplateComponentType.CLUSTER.equals(relClusterComp.getType())) {
                                return relClusterComp;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void validateId(String id) {
        if (id != null) {
            int fileSeparatorIndex = id.indexOf(File.separator);
            if (fileSeparatorIndex >= 0) {
                throw new IllegalArgumentException("Service Template Id can not include file paths");
            }
        }
    }

    /**
     * Returns list of directory entries.
     * @param templateId
     * @return
     */
    public static List<String> getAttachments(String templateId){
        List<String> attachments = new ArrayList<String>();
        File templateDir = new File(TEMPLATE_ATTACHMENT_DIR + templateId);
        if(templateDir.exists()) {
            attachments = Arrays.asList(templateDir.list());
        }
        return attachments;
    }

    /**
     * Copy attachment to template directory
     * @param templateId
     * @param attachmentFile
     */
    public static void addOrReplaceAttachment(String templateId, File attachmentFile) {
        ensureTemplateAttachmentsFolderExists();

        Path tempFolder = Paths.get(TEMPLATE_ATTACHMENT_DIR + templateId);
        if (!Files.exists(tempFolder)) {
            try {
                Files.createDirectory(tempFolder);
            } catch (IOException e) {
                LOGGER.error("Cannot create attachment directory for template: " + templateId,e);
                throw new AsmManagerRuntimeException("Cannot create directory: " + tempFolder);
            }
        }

        Path existingAttachment = Paths.get(TEMPLATE_ATTACHMENT_DIR + templateId + File.separator + attachmentFile.getName());
        if (Files.exists(existingAttachment)) {
            FileUtils.deleteQuietly(existingAttachment.toFile());
        }

        try {
            FileUtils.copyFileToDirectory(attachmentFile, tempFolder.toFile());
        } catch (IOException e) {
            LOGGER.error("Cannot copy attachment " + attachmentFile.getName() + " to the directory for template: " + templateId, e);
            throw new AsmManagerRuntimeException("Cannot copy template attachment " + attachmentFile.getName() + " to " + tempFolder);
        }
    }

    public static void addAssignedUsers(ServiceTemplate serviceTemplate, ServiceTemplateEntity serviceTemplateEntity) {
        if (serviceTemplate != null && serviceTemplateEntity != null) {
            if (serviceTemplate.getAssignedUsers() != null) {
                for (User user : serviceTemplate.getAssignedUsers()) {
                    TemplateUserRefEntity de = new TemplateUserRefEntity();
                    de.setUserId(user.getUserSeqId());
                    de.setTemplateId(serviceTemplate.getId());
                    de.setId(UUID.randomUUID().toString());
                    serviceTemplateEntity.getAssignedUserList().add(de);
                }
            }
        }
    }

    /**
     * Helper to add a setting "Create new ..." / "Autogenerate"
     * @param resource Resource to process
     * @param options list of options to select from
     */
    public static void processStorageVolumeName(ServiceTemplateCategory resource,
                                                           List<ServiceTemplateOption> options) {

        // check if the storage resource was already upgraded
        ServiceTemplateSetting setTitle = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(setTitle.getValue()) ||
            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING.equals(setTitle.getValue()) ||
            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW.equals(setTitle.getValue()) ||
            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT.equals(setTitle.getValue())) {
            return;
        }


        for (int i = 0; i < resource.getParameters().size(); i++) {
            ServiceTemplateSetting set = resource.getParameters().get(i);
            if (set.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID)) {

                set.getOptions().clear();
                //addDefaultSelectOption(set);
                set.setType(ServiceTemplateSetting.ServiceTemplateSettingType.ENUMERATED);
                set.getOptions().add(new ServiceTemplateOption("Auto-generate storage volume name",
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE, null, null));
                set.getOptions().add(new ServiceTemplateOption("Select an existing storage volume",
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING, null, null));
                set.getOptions().add(new ServiceTemplateOption("Specify a new storage volume name now",
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW, null, null));
                set.getOptions().add(new ServiceTemplateOption("Specify storage volume name at time of deployment",
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT, null, null));
                set.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE);
                set.setRequiredAtDeployment(false);
                set.setOptionsSortable(false);

                ServiceTemplateSetting setNameExisting = new ServiceTemplateSetting();
                setNameExisting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setNameExisting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING);
                setNameExisting.setDisplayName("Existing Volume Name");
                setNameExisting.setRequired(true);
                setNameExisting.setRequiredAtDeployment(false);
                setNameExisting.setHideFromTemplate(false);
                setNameExisting.setGroup(set.getGroup());
                setNameExisting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                setNameExisting.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING);
                addDefaultSelectOption(setNameExisting);
                if (options!=null) {
                    setNameExisting.getOptions().addAll(options);
                }
                resource.getParameters().add(i + 1, setNameExisting);

                ServiceTemplateSetting setNameTemplate = new ServiceTemplateSetting();
                setNameTemplate.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setNameTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_TEMPLATE);
                setNameTemplate.setDisplayName("Volume Name Template");
                setNameTemplate.setRequired(true);
                setNameTemplate.setRequiredAtDeployment(false);
                setNameTemplate.setHideFromTemplate(false);
                setNameTemplate.setGroup(set.getGroup());
                setNameTemplate.setValue("storage${num}");
                setNameTemplate.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                setNameTemplate.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE);
                setNameTemplate.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_AUTOGEN_VOLUME_TOOLTIP);
                resource.getParameters().add(i + 1, setNameTemplate);

                ServiceTemplateSetting setGenerated = new ServiceTemplateSetting();
                setGenerated.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setGenerated.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_GENERATED);
                setGenerated.setDisplayName("Generated Volume Name");
                setGenerated.setRequired(false);
                setGenerated.setRequiredAtDeployment(false);
                setGenerated.setHideFromTemplate(true);
                setGenerated.setGroup(set.getGroup());
                setGenerated.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                setGenerated.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE);
                resource.getParameters().add(i + 1, setGenerated);

                ServiceTemplateSetting setNew = new ServiceTemplateSetting();
                setNew.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setNew.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW);
                setNew.setDisplayName("New Volume Name");
                setNew.setRequired(true);
                setNew.setRequiredAtDeployment(false);
                setNew.setHideFromTemplate(false);
                setNew.setGroup(set.getGroup());
                setNew.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                setNew.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW);
                resource.getParameters().add(i + 1, setNew);

                ServiceTemplateSetting setNewAtDeployment = new ServiceTemplateSetting();
                setNewAtDeployment.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setNewAtDeployment.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW_AT_DEPLOYMENT);
                setNewAtDeployment.setDisplayName("New Volume Name");
                setNewAtDeployment.setRequired(true);
                setNewAtDeployment.setRequiredAtDeployment(true);
                setNewAtDeployment.setHideFromTemplate(true);
                setNewAtDeployment.setGroup(set.getGroup());
                setNewAtDeployment.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                setNewAtDeployment.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT);
                resource.getParameters().add(i + 1, setNewAtDeployment);

                break;
            }
        }
    }

    /**
     * For legacy storage components convert "title" setting to the settings correspoding to "existing" or "new" volume
     * @param resource
     */
    public static void convertOldStorageVolume(ServiceTemplateCategory resource) {
        ServiceTemplateSetting setTitle = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        if (setTitle != null) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX.equals(setTitle.getValue())) {
                convertStorageTitleToNewVolume(resource);
            }else{
                convertStorageTitleToExistingVolume(resource);
            }
        }
    }

    /**
     * For legacy storage components convert "title" setting to the settings correspoding to "existing" volume
     * @param resource
     */
    public static void convertStorageTitleToExistingVolume(ServiceTemplateCategory resource) {
        ServiceTemplateSetting setTitle = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(setTitle.getValue()) ||
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING.equals(setTitle.getValue()) ||
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW.equals(setTitle.getValue()) ||
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT.equals(setTitle.getValue())) {
            return;
        }

        String volume = setTitle.getValue();
        List<ServiceTemplateOption> options = new ArrayList<>();
        options.addAll(setTitle.getOptions());
        processStorageVolumeName(resource, options);
        setTitle.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING);
        ServiceTemplateSetting setVolume = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING);
        setVolume.setValue(volume);

        LOGGER.debug("Storage volume upgraded, existing value = " + setVolume.getValue());
    }

    /**
     * For legacy storage components convert "title" setting to the settings correspoding to "new" volume
     * @param resource
     */
    public static void convertStorageTitleToNewVolume(ServiceTemplateCategory resource) {
        ServiceTemplateSetting setTitle = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(setTitle.getValue()) ||
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING.equals(setTitle.getValue()) ||
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW.equals(setTitle.getValue()) ||
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT.equals(setTitle.getValue())) {
            return;
        }
        String volume = setTitle.getValue();
        // safety check - value must be set to $new%
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX.equals(volume)) {
            ServiceTemplateSetting setNewTitle = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX +
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
            if (setNewTitle == null) {
                LOGGER.error("Invalid template: no setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX +
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                return;
            }

            List<ServiceTemplateOption> options = new ArrayList<>();
            options.addAll(setTitle.getOptions());

            processStorageVolumeName(resource, options);
            setTitle.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW);
            ServiceTemplateSetting setVolume = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW);
            setVolume.setValue(setNewTitle.getValue());

            LOGGER.debug("Storage volume upgraded, new value = " + setNewTitle.getValue());
        }
    }

    /**
     * For given template resource get existing volume list.
     * @param category
     * @return HashSet of unique volumes
     */
    public static Set<String> getVolumeSet(ServiceTemplateCategory category) {
        ServiceTemplateSetting existingVolume = category.
                getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING);

        if (existingVolume == null) {
            return null;
        }
        Set<String> volumes = new HashSet<>();
        List<ServiceTemplateOption> storageTemplateOptions = existingVolume.getOptions();
        for (ServiceTemplateOption option : storageTemplateOptions) {
            if (StringUtils.isNotEmpty(option.getValue())) {
                volumes.add(option.getValue());
            }
        }
        return volumes;
    }

    /**
     * We need only "title" in deployment JSON. There are few auxilary parameters related to storage name that has to be
     * transformed into a single value, and removed afterwards
     * @param serviceTemplate
     * @return Map of storage component ID matching the storage volume. Needed for cluster processing.
     */
    public static Map<String,String> processStoragesForDeployment(ServiceTemplate serviceTemplate) {
        Map<String,String> storageMap = new HashMap<>();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (!ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE.equals(component.getType())) continue;

            for (ServiceTemplateCategory resource : component.getResources()) {
                Iterator<ServiceTemplateSetting> it = resource.getParameters().iterator();
                while (it.hasNext()) {
                    ServiceTemplateSetting param = it.next();
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId())) {

                        String volumeName = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(resource);
                        if (volumeName == null) {
                            String err = "Cannot find volume name setting in the service template for component = " +
                                    component.getAsmGUID() + ", volume mode: " + param.getId();
                            LOGGER.error(err);
                            break;
                        }

                        param.setValue(volumeName);
                        param.getOptions().clear();
                        storageMap.put(component.getId(), volumeName);
                    }

                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_GENERATED.equals(param.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING.equals(param.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW.equals(param.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW_AT_DEPLOYMENT.equals(param.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_TEMPLATE.equals(param.getId()))
                        it.remove();
                }
            }
        }
        return storageMap;
    }

    /**
     * For SDRS memebers in cluster settings we store storage comp IDs.
     * asm-deployer expects storage volumes - hence the conversion.
     * @param serviceTemplate
     * @param storageMap    Storage component ID -> storage volume
     */
    public static void processClustersForDeployment(ServiceTemplate serviceTemplate, Map<String,String> storageMap) {
        for (ServiceTemplateComponent cluster : serviceTemplate.getComponents()) {
            if (!ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.equals(cluster.getType())) continue;

            ServiceTemplateSetting dsNames = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID);

            if (dsNames != null && StringUtils.isNotEmpty(dsNames.getValue())) {
                // value is a CSV list of storage component IDs
                String[] compIDs = dsNames.getValue().trim().split(",");
                StringBuilder volumes = new StringBuilder();
                for (String compID: compIDs) {
                    String volume = storageMap.get(compID);
                    if (StringUtils.isNotEmpty(volume)) {
                        if (volumes.length()>0) {
                            volumes.append(",");
                        }
                        volumes.append(volume);
                    }
                }
                dsNames.setValue(volumes.toString());
            }
        }
    }

    public boolean requiredDependenciesSatisfied(final ServiceTemplateComponent component,
                                                 final ServiceTemplateSetting parameter) {
        if (StringUtils.isBlank(parameter.getDependencyTarget())) {
            // there are no further dependencies
            return Boolean.TRUE;
        } else {
            final ServiceTemplateSetting dependencyTarget =
                    component.getTemplateSetting(parameter.getDependencyTarget());
            if (dependencyTarget != null) {
                final String dependencyValue = dependencyTarget.getValue();
                for (final String value : StringUtils.split(parameter.getDependencyValue(), ",")) {
                    if (StringUtils.equals(dependencyValue, value)) {
                        LOGGER.info("Found required dependency " + dependencyTarget.getId() + ": " + value);
                        // NOTE[fcarta] Its understood there is a possibility for circular dependencies. In the
                        // event an error like this should occur we are ok with a StackOverflowException being
                        // thrown. This would indicate a programmatic error in a default template. Currently users dont
                        // have the ability to modify default templates.
                        return requiredDependenciesSatisfied(component,dependencyTarget);
                    }
                }
            }
            // dependency not found
            return Boolean.FALSE;
        }
    }

    public List<DeviceInventoryEntity> getVCenterEntities(boolean includeBrownfieldVmMangers) {
        List<String> filter = new ArrayList<>();
        filter.add("eq,deviceType,vcenter");
        if (includeBrownfieldVmMangers) {
            filter.add("eq,state," + DeviceState.READY.getValue());
            filter.add("eq,managedState," + ManagedState.MANAGED.getValue() + "," + ManagedState.RESERVED.getValue());
        } else {
            filter.add("eq,state," + DeviceState.READY.getValue());
            filter.add("eq,managedState," + ManagedState.MANAGED.getValue());
        }
        FilterParamParser filterParser = new FilterParamParser(filter,
                DeviceInventoryService.validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();
        List<DeviceInventoryEntity> vCenterEntities = getDeviceInventoryDAO().getAllDeviceInventory(null, filterInfos, null);
        return vCenterEntities;
    }

    public List<DeviceInventoryEntity> getHypervEntities() {
        List<String> filter = new ArrayList<>();
        filter.add("eq,deviceType,scvmm");
        filter.add("eq,state," + DeviceState.READY.getValue());
        filter.add("eq,managedState," + ManagedState.MANAGED.getValue());
        FilterParamParser filterParser = new FilterParamParser(filter,
                DeviceInventoryService.validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();
        List<DeviceInventoryEntity> hypervEntities = getDeviceInventoryDAO().getAllDeviceInventory(null, filterInfos, null);
        return hypervEntities;
    }

    public Map<String, List<DeviceInventoryEntity>> getStorageDevicesMap() {
        Map<String, List<DeviceInventoryEntity>> storageDeviceMap = new HashMap<>();

        List<String> filter = new ArrayList<String>();
        filter.add("eq,deviceType,compellent,equallogic,netapp,emcvnx");
        filter.add("eq,state," + DeviceState.READY.getValue());
        filter.add("eq,managedState," + ManagedState.MANAGED.getValue());
        FilterParamParser filterParser = new FilterParamParser(
                filter, DeviceInventoryService.validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        List<DeviceInventoryEntity> storageEntities = getDeviceInventoryDAO().getAllDeviceInventory(null, filterInfos, null);
        if (storageEntities != null && storageEntities.size() > 0) {
            for (DeviceInventoryEntity device : storageEntities) {
                if (device.getDeviceType() != null) {
                    List<DeviceInventoryEntity> deviceList = storageDeviceMap.get(device.getDeviceType().name());
                    if (deviceList == null) {
                        deviceList = new ArrayList<>();
                        storageDeviceMap.put(device.getDeviceType().name(), deviceList);
                    }
                    deviceList.add(device);
                }
            }
        }
        return storageDeviceMap;
    }

    public EncryptionDAO getEncryptionDAO() {
        if (encryptionDAO == null) {
            encryptionDAO = EncryptionDAO.getInstance();
        }
        return encryptionDAO;
    }

    public void setEncryptionDAO(EncryptionDAO encryptionDAO) {
        this.encryptionDAO = encryptionDAO;
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

    public OSRepositoryUtil getOsRepositoryUtil() {
        if (osRepositoryUtil == null) {
            osRepositoryUtil = new OSRepositoryUtil();
        }
        return osRepositoryUtil;
    }

    public void setOsRepositoryUtil(OSRepositoryUtil osRepositoryUtil) {
        this.osRepositoryUtil = osRepositoryUtil;
    }

    public DeviceInventoryDAO getDeviceInventoryDAO() {
        if (deviceInventoryDAO == null) {
            deviceInventoryDAO = new DeviceInventoryDAO();
        }
        return deviceInventoryDAO;
    }

    public void setDeviceInventoryDAO(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO = deviceInventoryDAO;
    }
}
