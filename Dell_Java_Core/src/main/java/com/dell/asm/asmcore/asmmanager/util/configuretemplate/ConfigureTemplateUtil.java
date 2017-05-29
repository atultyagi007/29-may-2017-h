/**************************************************************************
 * Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.configuretemplate;

import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplate;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.client.util.VcenterInventoryUtils;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ClusterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatacenterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ManagedObjectDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.PortGroupDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VDSDTO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.util.AsmManagerUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ConfigureTemplateUtil {

    private static final Logger logger = Logger.getLogger(ConfigureTemplateUtil.class);

    private static final Pattern STORAGE_SIZE_PATTERN = Pattern.compile("[0-9]+|[A-z]+");

    public static ConfigureTemplate parseServiceTemplate(final ServiceTemplate serviceTemplate,
                                                         final Map<NetworkType, Set<ConfigureTemplateOption>> networkTypesMap,
                                                         final List<RazorRepo> razorImages,
                                                         final Map<String, String> repoNames,
                                                         final List<DeviceInventoryEntity> vCenterEntities,
                                                         final List<DeviceInventoryEntity> hypervEntities,
                                                         final List<DeviceGroupEntity> deviceGroups,
                                                         final Map<String, List<DeviceInventoryEntity>> storageDeviceMap) {
        ConfigureTemplate vsConfiguration = null;
        if (serviceTemplate != null) {
            final Map<String, ConfigureTemplateSetting> networkSettingsMap = new HashMap<>();
            final Map<String, Map<String, String>> networkAssociationsMap = new HashMap<>();
            Map<String, ConfigureTemplateSetting> osRepositorySettingsMap = null;
            Map<String, Map<String, String>> osRepositoryAssociationsMap = null;
            Map<String, ConfigureTemplateSetting> clusterSettingsMap = null;
            Map<String, Map<String, String>> clusterAssociationsMap = null;
            Map<String, ConfigureTemplateSetting> clusterDetailsSettingsMap = null;
            Map<String, ConfigureTemplateSetting> serverPoolSettingsMap = null;
            Map<String, Map<String, String>> serverPoolAssociationsMap = null;
            Map<String, ConfigureTemplateSetting> storageSettingsMap = null;
            Map<String, Map<String, String>> storageAssociationsMap = null;
            Map<String, ConfigureTemplateSetting> serverSettingsMap = null;
            Map<String, ConfigureTemplateCategory> storageDetailsCategoryMap = null;

            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                String componentId = component.getComponentID() != null ? component.getComponentID() : component.getId();
                switch (component.getType()) {
                    case SERVER:
                        switch (serviceTemplate.getTemplateType()) {
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM:
                                // Server Pools are only on Servers so fallthrough to Virtualmachine
                                ServiceTemplateCategory serverPoolCategory = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE);
                                if (serverPoolCategory == null) {
                                    serverPoolCategory = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                                }
                                if (serverPoolCategory != null) {
                                    if (serverPoolSettingsMap == null) {
                                        serverPoolSettingsMap = new HashMap<>();
                                    }
                                    if (serverPoolAssociationsMap == null) {
                                        serverPoolAssociationsMap = new HashMap<>();
                                    }
                                    createServerPoolSetting(component,
                                                            serverPoolCategory,
                                                            serverPoolSettingsMap,
                                                            serverPoolAssociationsMap,
                                                            deviceGroups);
                                }
                                break;
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_VALIDATED_SYSTEMS:
                                if (serverSettingsMap == null) {
                                    serverSettingsMap = new HashMap<>();
                                }
                                createValidatedSystemServerConfigurationSetting(component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE),
                                                                                serverSettingsMap,
                                                                                deviceGroups,
                                                                                razorImages,
                                                                                repoNames);
                                break;
                            default:
                                break;
                        }

                        //ALWAYS FALL THROUGH ON PURPOSE
                    case VIRTUALMACHINE:
                        //Process Networking Information
                        addConfigureTemplateNetworkSettings(component, networkSettingsMap, networkAssociationsMap, networkTypesMap);

                        switch (serviceTemplate.getTemplateType()) {
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM:
                                //Process OS Settings
                                ServiceTemplateCategory osSettingsCategory = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                                if (osSettingsCategory != null) {
                                    boolean found = false;
                                    for (ServiceTemplateSetting setting : osSettingsCategory.getParameters()) {
                                        switch (setting.getId()) {
                                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID:
                                                if (osRepositorySettingsMap == null) {
                                                    osRepositorySettingsMap = new HashMap<>();
                                                }
                                                if (osRepositoryAssociationsMap == null) {
                                                    osRepositoryAssociationsMap = new HashMap<>();
                                                }
                                                if (StringUtils.isNotBlank(setting.getValue())) {
                                                    for (ServiceTemplateOption option : setting.getOptions()) {
                                                        if (setting.getValue().equals(option.getValue())) {
                                                            ConfigureTemplateSetting osImageSetting = osRepositorySettingsMap.get(
                                                                    option.getValue());
                                                            if (osImageSetting == null) {
                                                                osImageSetting = createOSImageConfigureTemplateSetting(option.getValue(),
                                                                                                                       option.getName(),
                                                                                                                       ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT_ID,
                                                                                                                       razorImages,
                                                                                                                       repoNames,
                                                                                                                       false);
                                                                osRepositorySettingsMap.put(option.getValue(), osImageSetting);
                                                            }
                                                            // Add associations to component
                                                            Map<String, String> osRepoOptions = osRepositoryAssociationsMap.get(
                                                                    option.getValue());
                                                            if (osRepoOptions == null) {
                                                                osRepoOptions = new LinkedHashMap<>();
                                                                osRepositoryAssociationsMap.put(option.getValue(), osRepoOptions);
                                                            }
                                                            osRepoOptions.put(component.getType().name(), component.getName());
                                                            break;
                                                        }
                                                    }
                                                }
                                                found = true;
                                                break;
                                        }
                                        if (found) {
                                            break;
                                        }
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case STORAGE:
                        switch (serviceTemplate.getTemplateType()) {
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM:
                                //Process Storage
                                if (storageSettingsMap == null) {
                                    storageSettingsMap = new HashMap<>();
                                }
                                if (storageAssociationsMap == null) {
                                    storageAssociationsMap = new HashMap<>();
                                }
                                createStorageSetting(component,
                                                     componentId,
                                                     storageSettingsMap,
                                                     storageAssociationsMap,
                                                     storageDeviceMap);
                                break;
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_VALIDATED_SYSTEMS:
                                if (storageDetailsCategoryMap == null) {
                                    storageDetailsCategoryMap = new HashMap<>();
                                }
                                createValidatedSystemStorageCategories(component,
                                                                       componentId,
                                                                       storageDetailsCategoryMap,
                                                                       storageDeviceMap);
                                break;
                            default:
                                break;
                        }
                        break;
                    case CLUSTER:
                        ServiceTemplateSetting clusterSetting;
                        switch (componentId) {
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID:
                                switch (serviceTemplate.getTemplateType()) {
                                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM:
                                        clusterSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                                                                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                                        if (clusterSetting != null) {
                                            if (clusterSettingsMap == null) {
                                                clusterSettingsMap = new HashMap<>();
                                            }
                                            if (clusterAssociationsMap == null) {
                                                clusterAssociationsMap = new HashMap<>();
                                            }
                                            createConfigurationSettings(component,
                                                                        clusterSetting,
                                                                        DeviceType.vcenter.name(),
                                                                        vCenterEntities,
                                                                        clusterSettingsMap,
                                                                        clusterAssociationsMap);
                                        }
                                        break;
                                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_VALIDATED_SYSTEMS:
                                        if (clusterDetailsSettingsMap == null) {
                                            clusterDetailsSettingsMap = new LinkedHashMap<>();
                                        }
                                        createValidatedSystemClusterConfigurationSetting(component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID),
                                                                                         vCenterEntities,
                                                                                         clusterDetailsSettingsMap);
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID:
                                clusterSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID,
                                                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                                if (clusterSetting != null) {
                                    if (clusterSettingsMap == null) {
                                        clusterSettingsMap = new HashMap<>();
                                    }
                                    if (clusterAssociationsMap == null) {
                                        clusterAssociationsMap = new HashMap<>();
                                    }
                                    createConfigurationSettings(component,
                                                                clusterSetting,
                                                                DeviceType.scvmm.name(),
                                                                hypervEntities,
                                                                clusterSettingsMap,
                                                                clusterAssociationsMap);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        continue;
                }
            }

            //BUILD CONFIGURATION
            ConfigureTemplate configuration = new ConfigureTemplate();
            configuration.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_ID);

            if (networkSettingsMap != null && networkSettingsMap.size() > 0) {
                addConfigurationAssociationCategory(configuration,
                                                    networkSettingsMap,
                                                    networkAssociationsMap,
                                                    ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_NETWORKING_RESOURCE,
                                                    ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_NETWORKING_ASSOCIATIONS_RESOURCE,
                                                    "Network Settings");
            }

            switch (serviceTemplate.getTemplateType()) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM:
                    if (osRepositorySettingsMap != null && osRepositorySettingsMap.size() > 0) {
                        addConfigurationAssociationCategory(configuration,
                                                            osRepositorySettingsMap,
                                                            osRepositoryAssociationsMap,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_RESOURCE,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_ASSOCIATIONS_RESOURCE,
                                                            "OS Settings");
                    }

                    if (serverPoolSettingsMap != null && serverPoolSettingsMap.size() > 0) {
                        addConfigurationAssociationCategory(configuration,
                                                            serverPoolSettingsMap,
                                                            serverPoolAssociationsMap,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_POOL_RESOURCE,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_POOL_ASSOCIATIONS_RESOURCE,
                                                            "Server Pool Settings");
                    }
                    if (clusterSettingsMap != null && clusterSettingsMap.size() > 0) {
                        addConfigurationAssociationCategory(configuration,
                                                            clusterSettingsMap,
                                                            clusterAssociationsMap,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_RESOURCE,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_ASSOCIATIONS_RESOURCE,
                                                            "Cluster Settings");
                    }
                    if (storageSettingsMap != null && storageSettingsMap.size() > 0) {
                        addConfigurationAssociationCategory(configuration,
                                                            storageSettingsMap,
                                                            storageAssociationsMap,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_RESOURCE,
                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_ASSOCIATIONS_RESOURCE,
                                                            "Storage Settings");
                    }
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_VALIDATED_SYSTEMS:
                    if (serverSettingsMap != null && serverSettingsMap.size() > 0) {
                        addConfigurationCategory(configuration,
                                                 serverSettingsMap.values(),
                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_RESOURCE,
                                                 "Server Settings");
                    }

                    if (clusterDetailsSettingsMap != null && clusterDetailsSettingsMap.size() > 0) {
                        addConfigurationCategory(configuration,
                                                 clusterDetailsSettingsMap.values(),
                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_RESOURCE,
                                                 "Cluster Details Settings");
                    }
                    if (storageDetailsCategoryMap != null && storageDetailsCategoryMap.size() > 0) {
                        for (ConfigureTemplateCategory category : storageDetailsCategoryMap.values()) {
                            configuration.getCategories().add(category);
                        }
                    }
                    break;
                default:
                    break;
            }

            if (configuration.getCategories() != null && configuration.getCategories().size() > 0) {
                serviceTemplate.setConfiguration(configuration);
            }
        }

        return vsConfiguration;
    }

    public static Map<NetworkType, Set<ConfigureTemplateOption>> getNetworksMap(final INetworkService networkService) {
        Map<NetworkType, Set<ConfigureTemplateOption>> networkTypesMap = new LinkedHashMap<>();
        if (networkService != null) {
            com.dell.pg.asm.identitypool.api.network.model.Network[] networks = networkService.getNetworks("name", null, null, null);
            if (networks != null && networks.length > 0) {
                for (com.dell.pg.asm.identitypool.api.network.model.Network network : networks) {
                    Set<ConfigureTemplateOption> typeNetworkOptionsSet = networkTypesMap.get(network.getType());
                    if (typeNetworkOptionsSet == null) {
                        typeNetworkOptionsSet = new LinkedHashSet<>();
                        networkTypesMap.put(network.getType(), typeNetworkOptionsSet);
                    }
                    typeNetworkOptionsSet.add(new ConfigureTemplateOption(network.getId(), network.getName()));
                }
            }
        }
        return networkTypesMap;
    }

    public static void createConfigurationSettings(ServiceTemplateComponent component,
                                                   ServiceTemplateSetting setting,
                                                   String type,
                                                   List<DeviceInventoryEntity> entities,
                                                   Map<String, ConfigureTemplateSetting> settingsMap,
                                                   Map<String, Map<String, String>> associationsMap) {
        if (StringUtils.isNotBlank(setting.getValue())) {
            for (ServiceTemplateOption option : setting.getOptions()) {
                if (setting.getValue().equals(option.getValue())) {
                    ConfigureTemplateSetting newSetting = settingsMap.get(setting.getValue());
                    if (newSetting == null) {
                        newSetting = new ConfigureTemplateSetting();
                        newSetting.setId(option.getValue());
                        newSetting.setDisplayName(option.getName());
                        newSetting.setDeviceType(type);
                        newSetting.setValue(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SELECT_ID);
                        if (entities != null && entities.size() > 0) {
                            for (DeviceInventoryEntity entity : entities) {
                                newSetting.getOptions().add(new ConfigureTemplateOption(entity.getRefId(), entity.getServiceTag()));
                            }
                        }
                        settingsMap.put(setting.getValue(), newSetting);
                    }
                    // Add associations to component
                    Map<String, String> associationOptions = associationsMap.get(option.getValue());
                    if (associationOptions == null) {
                        associationOptions = new HashMap<>();
                        associationsMap.put(option.getValue(), associationOptions);
                    }
                    associationOptions.put(component.getType().name(), component.getName());
                    break;
                }
            }
        }
    }


    public static ConfigureTemplateSetting createOSImageConfigureTemplateSetting(final String id,
                                                                                 final String name,
                                                                                 final String value,
                                                                                 final List<RazorRepo> razorImages,
                                                                                 final Map<String, String> repoNames,
                                                                                 boolean eSXIOnly) {
        ConfigureTemplateSetting osImageSetting = new ConfigureTemplateSetting();
        osImageSetting.setId(id);
        osImageSetting.setDisplayName(name);
        osImageSetting.setValue(value);
        for (RazorRepo image : razorImages) {
            if (!eSXIOnly ||
                    (eSXIOnly && image.getName().contains("esxi"))) {
                String displayName = (repoNames.get(
                        image.getName()) == null) ? image.getName() : repoNames.get(
                        image.getName());
                osImageSetting.getOptions().add(new ConfigureTemplateOption(image.getName(), displayName));
            }
        }
        return osImageSetting;
    }

    public static void createServerPoolSetting(final ServiceTemplateComponent component,
                                               final ServiceTemplateCategory serverPoolCategory,
                                               final Map<String, ConfigureTemplateSetting> serverPoolSettingsMap,
                                               final Map<String, Map<String, String>> serverPoolAssociationsMap,
                                               final List<DeviceGroupEntity> deviceGroups) {
        if (serverPoolCategory != null) {
            ServiceTemplateSetting serverPoolSetting = serverPoolCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);
            if (serverPoolSetting != null && StringUtils.isNotBlank(serverPoolSetting.getValue())) {
                for (ServiceTemplateOption option : serverPoolSetting.getOptions()) {
                    if (serverPoolSetting.getValue().equals(option.getValue())) {
                        ConfigureTemplateSetting spSetting = serverPoolSettingsMap.get(option.getValue());
                        if (spSetting == null) {
                            spSetting = createServerPoolConfigureTemplateSetting(option.getValue(),
                                                                                 option.getName(),
                                                                                 deviceGroups);
                            serverPoolSettingsMap.put(option.getValue(), spSetting);
                        }
                        // Add associations to component
                        Map<String, String> serverPoolOptions = serverPoolAssociationsMap.get(option.getValue());
                        if (serverPoolOptions == null) {
                            serverPoolOptions = new LinkedHashMap<>();
                            serverPoolAssociationsMap.put(option.getValue(), serverPoolOptions);
                        }
                        serverPoolOptions.put(component.getType().name(), component.getName());
                        break;
                    }
                }
            }
        }
    }

    public static ConfigureTemplateSetting createServerPoolConfigureTemplateSetting(final String id,
                                                                                    final String name,
                                                                                    final List<DeviceGroupEntity> deviceGroups) {
        ConfigureTemplateSetting spSetting = new ConfigureTemplateSetting();
        spSetting.setId(id);
        spSetting.setDisplayName(name);
        spSetting.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID);
        spSetting.getOptions().add(new ConfigureTemplateOption(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID,
                                                               ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME));
        Long userId = AsmManagerUtil.getUserId();
        if (deviceGroups != null && deviceGroups.size() > 0) {
            for (DeviceGroupEntity group : deviceGroups) {
                if (group.getGroupsUsers() != null && group.getGroupsUsers().size() > 0) {
                    if (group.getGroupsUsers().contains(userId)) {
                        if (group.getSeqId() != null) {
                            spSetting.getOptions().add(new ConfigureTemplateOption(Long.toString(group.getSeqId()), group.getName()));
                        }
                    }
                }
            }
        }
        return spSetting;
    }

    public static void createValidatedSystemClusterConfigurationSetting(final ServiceTemplateCategory category,
                                                                        final List<DeviceInventoryEntity> entities,
                                                                        final Map<String, ConfigureTemplateSetting> settingsMap) {
        Set<ConfigureTemplateOption> vCentersSet = new LinkedHashSet<>();
        Map<String, ConfigureTemplateOption> dataCentersMap = new HashMap<>();
        Map<String, ConfigureTemplateOption> clustersMap = new HashMap<>();
        Map<String, ConfigureTemplateOption> vdsMap = new HashMap<>();
        Map<String, ConfigureTemplateOption> portGroupMap = new HashMap<>();
        if (entities != null && entities.size() > 0) {

            for (DeviceInventoryEntity vCenterEntity : entities) {
                vCentersSet.add(new ConfigureTemplateOption(vCenterEntity.getRefId(), vCenterEntity.getServiceTag()));

                ManagedObjectDTO vCenter = null;
                try {
                    Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(vCenterEntity.getRefId());
                    vCenter = VcenterInventoryUtils.convertPuppetDeviceDetailsToDto(deviceDetails);
                } catch (Exception e1) {
                    logger.error("Could not find deviceDetails for " + vCenterEntity.getRefId(), e1);
                    continue;
                }
                List<DatacenterDTO> dcSet = vCenter.getDatacenters();
                for (DatacenterDTO dc : dcSet) {
                    String path = dc.getName();
                    ConfigureTemplateOption dataCenterOption = dataCentersMap.get(path);
                    if (dataCenterOption == null) {
                        dataCentersMap.put(path, new ConfigureTemplateOption(path,
                                                                             path,
                                                                             ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VCENTER_ID,
                                                                             vCenterEntity.getRefId()));

                    } else {
                        String dependency = dataCenterOption.getDependencyValue() + "," + vCenterEntity.getRefId();
                        dataCenterOption.setDependencyValue(dependency);
                    }


                    if (dc.getClusters() != null && dc.getClusters().size() > 0) {
                        for (ClusterDTO cdto : dc.getClusters()) {
                            String clusterName = cdto.getName();
                            ConfigureTemplateOption clusterOption = clustersMap.get(clusterName);
                            if (clusterOption == null) {
                                clusterOption = new ConfigureTemplateOption(clusterName,
                                                                            clusterName,
                                                                            ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DATACENTER_ID,
                                                                            path);
                                clustersMap.put(clusterName, clusterOption);
                            } else {
                                String dependency = clusterOption.getDependencyValue() + "," + path;
                                clusterOption.setDependencyValue(dependency);
                            }
                        }
                    }

                    if (dc.getVDSs() != null && dc.getVDSs().size() > 0) {
                        for (VDSDTO vds : dc.getVDSs()) {
                            String name = vds.getName();
                            ConfigureTemplateOption vdsOption = vdsMap.get(name);
                            if (vdsOption == null) {
                                vdsOption = new ConfigureTemplateOption(name,
                                                                        name,
                                                                        ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DATACENTER_ID,
                                                                        path);
                                vdsMap.put(name, vdsOption);
                            } else {
                                String dependency = vdsOption.getDependencyValue() + "," + path;
                                vdsOption.setDependencyValue(dependency);
                            }
                            if (vds.getPortGroups() != null && vds.getPortGroups().size() > 0) {
                                for (PortGroupDTO pdto : vds.getPortGroups()) {
                                    String pname = pdto.getName();
                                    ConfigureTemplateOption portGroupOption = portGroupMap.get(pname);
                                    if (portGroupOption == null) {
                                        portGroupOption = new ConfigureTemplateOption(pname,
                                                                                      pname,
                                                                                      "",
                                                                                      name);
                                        if (pdto.getAttributes() != null && pdto.getAttribute("vlan_id") != null) {
                                            portGroupOption.getAttributes().put("vlan_id", String.valueOf(pdto.getAttribute("vlan_id")));
                                        }
                                        portGroupMap.put(name, portGroupOption);
                                    } else {
                                        String dependency = portGroupOption.getDependencyValue() + "," + name;
                                        portGroupOption.setDependencyValue(dependency);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (category != null && category.getParameters() != null && category.getParameters().size() > 0) {
            for (ServiceTemplateSetting setting : category.getParameters()) {
                switch (setting.getId()) {
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VCENTER_ID) == null) {
                            ConfigureTemplateSetting vCenterSetting = new ConfigureTemplateSetting();
                            vCenterSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VCENTER_ID);
                            vCenterSetting.setDisplayName("Which vCenter in your environment do you want to use?");
                            vCenterSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.ENUMERATED);
                            if (vCentersSet.size() > 0) {
                                vCenterSetting.setOptions(vCentersSet);
                            }
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VCENTER_ID, vCenterSetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DATACENTER_ID) == null) {
                            ConfigureTemplateSetting datacenterSetting = new ConfigureTemplateSetting();
                            datacenterSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DATACENTER_ID);
                            datacenterSetting.setDisplayName("Select a Data Center");
                            datacenterSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.ENUMERATED);
                            datacenterSetting.setValue(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX);
                            datacenterSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX, "Create New Datacenter..."));
                            if (dataCentersMap.size() > 0) {
                                datacenterSetting.getOptions().addAll(dataCentersMap.values());
                            }
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DATACENTER_ID, datacenterSetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_NEW_DATACENTER_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_DATACENTER_ID) == null) {
                            ConfigureTemplateSetting newDatacenterSetting = new ConfigureTemplateSetting();
                            newDatacenterSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_DATACENTER_ID);
                            newDatacenterSetting.setDisplayName("New Datacenter Name");
                            newDatacenterSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.STRING);
                            newDatacenterSetting.setValue(setting.getValue());
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_DATACENTER_ID, newDatacenterSetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_CLUSTER_ID) == null) {
                            ConfigureTemplateSetting clusterSetting = new ConfigureTemplateSetting();
                            clusterSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_CLUSTER_ID);
                            clusterSetting.setDisplayName("Select a Cluster");
                            clusterSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.ENUMERATED);
                            clusterSetting.setValue(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX);
                            clusterSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX, "Create New..."));
                            if (clustersMap.size() > 0) {
                                clusterSetting.getOptions().addAll(clustersMap.values());
                            }
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_CLUSTER_ID, clusterSetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_NEW_CLUSTER_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_CLUSTER_ID) == null) {
                            ConfigureTemplateSetting newDatacenterSetting = new ConfigureTemplateSetting();
                            newDatacenterSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_CLUSTER_ID);
                            newDatacenterSetting.setDisplayName("Enter Cluster Name");
                            newDatacenterSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.STRING);
                            newDatacenterSetting.setValue(setting.getValue());
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_CLUSTER_ID, newDatacenterSetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_OPTIONS_ID) == null) {
                            if (vdsMap.size() > 0) {
                                ConfigureTemplateSetting vdsSetting = new ConfigureTemplateSetting();
                                vdsSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_OPTIONS_ID);
                                vdsSetting.setDisplayName("VDS Setting");
                                vdsSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.ENUMERATED);
                                vdsSetting.getOptions().addAll(vdsMap.values());
                                settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_OPTIONS_ID, vdsSetting);
                            }
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID:
                        if (portGroupMap.size() > 0) {
                            if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_PORT_GROUPS_ID) == null) {
                                ConfigureTemplateSetting portGroupsSetting = new ConfigureTemplateSetting();
                                portGroupsSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_PORT_GROUPS_ID);
                                portGroupsSetting.setDisplayName("Port Groups");
                                portGroupsSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.ENUMERATED);
                                portGroupsSetting.getOptions().addAll(portGroupMap.values());
                                settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_PORT_GROUPS_ID, portGroupsSetting);
                            }
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_HA_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_HA_ID) == null) {
                            ConfigureTemplateSetting clusterHASetting = new ConfigureTemplateSetting();
                            clusterHASetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_HA_ID);
                            clusterHASetting.setDisplayName("Select the VMware capabilities to be used in this cluster");
                            clusterHASetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.BOOLEAN);
                            clusterHASetting.setValue(setting.getValue());
                            clusterHASetting.setDependencyTarget(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_CLUSTER_ID);
                            clusterHASetting.setDependencyValue(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX);
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_HA_ID, clusterHASetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DRS_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DRS_ID) == null) {
                            ConfigureTemplateSetting clusterDRSSetting = new ConfigureTemplateSetting();
                            clusterDRSSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DRS_ID);
                            clusterDRSSetting.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.BOOLEAN);
                            clusterDRSSetting.setValue(setting.getValue());
                            clusterDRSSetting.setDependencyTarget(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_CLUSTER_ID);
                            clusterDRSSetting.setDependencyValue(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX);
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DRS_ID, clusterDRSSetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID:
                        if (settingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_ENABLE_VSAN_ID) == null) {
                            ConfigureTemplateSetting enableVSAN = new ConfigureTemplateSetting();
                            enableVSAN.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_ENABLE_VSAN_ID);
                            enableVSAN.setType(ConfigureTemplateSetting.ConfigureTemplateSettingType.BOOLEAN);
                            enableVSAN.setValue(setting.getValue());
                            settingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_DETAILS_ENABLE_VSAN_ID, enableVSAN);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static void createValidatedSystemServerConfigurationSetting(final ServiceTemplateCategory category,
                                                                       Map<String, ConfigureTemplateSetting> serverSettingsMap,
                                                                       final List<DeviceGroupEntity> deviceGroups,
                                                                       final List<RazorRepo> razorImages,
                                                                       final Map<String, String> repoNames) {
        if (category != null && category.getParameters() != null && category.getParameters().size() > 0) {
            if (serverSettingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NUMBER_OF_ID) == null) {
                ConfigureTemplateSetting numberOfSetting = new ConfigureTemplateSetting();
                numberOfSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NUMBER_OF_ID);
                numberOfSetting.setDisplayName("Number of Servers / Hosts");
                numberOfSetting.setValue("2");
                for (int i = 2; i <= 24; i++) {
                    numberOfSetting.getOptions().add(new ConfigureTemplateOption(Integer.toString(i), Integer.toString(i)));
                }
                serverSettingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NUMBER_OF_ID, numberOfSetting);
            }
            if (serverSettingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_SERVER_POOL_ID) == null) {
                ConfigureTemplateSetting spSetting = createServerPoolConfigureTemplateSetting(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_SERVER_POOL_ID,
                                                                                              "Which Server Pool should ASM use\nto pull servers?",
                                                                                              deviceGroups);
                serverSettingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_SERVER_POOL_ID, spSetting);
            }
            for (ServiceTemplateSetting setting : category.getParameters()) {
                switch (setting.getId()) {
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID:
                        String displayValue = "";
                        if (StringUtils.isNotBlank(setting.getValue())) {
                            for (ServiceTemplateOption option : setting.getOptions()) {
                                if (option.getValue().equals(setting.getValue())) {
                                    displayValue = option.getName();
                                    if (serverSettingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_OS_IMAGE_ID) == null) {
                                        ConfigureTemplateSetting osImageSetting = createOSImageConfigureTemplateSetting(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_OS_IMAGE_ID,
                                                                                                                        option.getName(),
                                                                                                                        setting.getValue(),
                                                                                                                        razorImages,
                                                                                                                        repoNames,
                                                                                                                        true);
                                        serverSettingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_OS_IMAGE_ID, osImageSetting);
                                    }
                                    break;
                                }
                            }
                        }
                        if (serverSettingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_RECOMMENDED_ESXI_ID) == null) {
                            ConfigureTemplateSetting recommendedESXISetting = new ConfigureTemplateSetting();
                            recommendedESXISetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_RECOMMENDED_ESXI_ID);
                            recommendedESXISetting.setDisplayName("ASM recommends " + displayValue);
                            recommendedESXISetting.setValue(displayValue);
                            serverSettingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_RECOMMENDED_ESXI_ID, recommendedESXISetting);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID:
                        String template = StringUtils.isNotBlank(setting.getValue()) ? setting.getValue() : ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_DEFAULT_TEMPLATE;
                        String prefix;
                        String suffix = ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_NUMERIC_ID;
                        int index = template.indexOf("$");
                        if (index >= 0) {
                            prefix = template.substring(0, index);
                            suffix = template.substring(index);
                        } else {
                            prefix = template;
                        }
                        if (serverSettingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_PREFIX_ID) == null) {
                            ConfigureTemplateSetting serverNamePrefixSetting = new ConfigureTemplateSetting();
                            serverNamePrefixSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_PREFIX_ID);
                            serverNamePrefixSetting.setDisplayName("Server Name Template Prefix");
                            serverNamePrefixSetting.setValue(prefix);
                            serverSettingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_PREFIX_ID, serverNamePrefixSetting);
                        }

                        if (serverSettingsMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_ID) == null) {
                            ConfigureTemplateSetting serverNameSuffixSetting = new ConfigureTemplateSetting();
                            serverNameSuffixSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_ID);
                            serverNameSuffixSetting.setDisplayName("Server Name Template Prefix");
                            serverNameSuffixSetting.setValue(suffix);
                            serverNameSuffixSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_NUMERIC_ID,
                                                                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_NUMERIC_ID));
                            serverNameSuffixSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_DNS_ID,
                                                                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_DNS_ID));
                            serverNameSuffixSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_SERVICE_TAG_ID,
                                                                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_SERVICE_TAG_ID));
                            serverSettingsMap.put(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_ID, serverNameSuffixSetting);
                        }
                        break;
                }
            }
        }
    }

    public static void createStorageSetting(final ServiceTemplateComponent component,
                                            final String componentId,
                                            final Map<String, ConfigureTemplateSetting> storageSettingsMap,
                                            final Map<String, Map<String, String>> storageAssociationsMap,
                                            final Map<String, List<DeviceInventoryEntity>> storageDeviceMap) {
        String type = null;
        switch (componentId) {
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID:
                ServiceTemplateSetting compellentStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID,
                                                                                         ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                if (compellentStorageSetting != null && StringUtils.isNotBlank(compellentStorageSetting.getValue())) {
                    type = DeviceType.compellent.name();
                    List<DeviceInventoryEntity> compellentStorageEntities = storageDeviceMap.get(DeviceType.compellent.name());
                    createConfigurationSettings(component,
                                                compellentStorageSetting,
                                                type,
                                                compellentStorageEntities,
                                                storageSettingsMap,
                                                storageAssociationsMap);
                }
                break;
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_EQL_COMP_ID:
                ServiceTemplateSetting equallogicSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                                                                                  ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                if (equallogicSetting != null && StringUtils.isNotBlank(equallogicSetting.getValue())) {
                    type = DeviceType.equallogic.name();
                    List<DeviceInventoryEntity> equallogicStorageEntities = storageDeviceMap.get(DeviceType.equallogic.name());
                    createConfigurationSettings(component,
                                                equallogicSetting,
                                                type,
                                                equallogicStorageEntities,
                                                storageSettingsMap,
                                                storageAssociationsMap);
                }
                break;
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_NETAPP_COMP_ID:
                ServiceTemplateSetting netappStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID,
                                                                                     ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                if (netappStorageSetting != null && StringUtils.isNotBlank(netappStorageSetting.getValue())) {
                    type = DeviceType.netapp.name();
                    List<DeviceInventoryEntity> netappStorageEntities = storageDeviceMap.get(DeviceType.netapp.name());
                    createConfigurationSettings(component,
                                                netappStorageSetting,
                                                type,
                                                netappStorageEntities,
                                                storageSettingsMap,
                                                storageAssociationsMap);
                }
                break;
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VNX_COMP_ID:
                ServiceTemplateSetting vnxStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID,
                                                                                  ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                if (vnxStorageSetting != null && StringUtils.isNotBlank(vnxStorageSetting.getValue())) {
                    type = DeviceType.emcvnx.name();
                    List<DeviceInventoryEntity> vnxStorageEntities = storageDeviceMap.get(DeviceType.emcvnx.name());
                    createConfigurationSettings(component,
                                                vnxStorageSetting,
                                                type,
                                                vnxStorageEntities,
                                                storageSettingsMap,
                                                storageAssociationsMap);
                }
                break;
            default:
                break;
        }
    }

    public static void createValidatedSystemStorageCategories(final ServiceTemplateComponent component,
                                                              final String componentId,
                                                              final Map<String, ConfigureTemplateCategory> storageCategoriesMap,
                                                              final Map<String, List<DeviceInventoryEntity>> storageDeviceMap) {

        String type = null;
        if (component != null) {
            switch (componentId) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID:
                    ServiceTemplateCategory compellentCategory = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID);
                    if (compellentCategory != null && compellentCategory.getParameters() != null && compellentCategory.getParameters().size() > 0) {
                        ServiceTemplateSetting compellentStorageSetting = compellentCategory.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (compellentStorageSetting != null) {
                            String storageId = StringUtils.isNotBlank(compellentStorageSetting.getValue()) ? compellentStorageSetting.getValue() : compellentStorageSetting.getDisplayName();
                            type = DeviceType.compellent.name();
                            List<DeviceInventoryEntity> compellentStorageEntities = storageDeviceMap.get(DeviceType.compellent.name());
                            createValidatedSystemConfigureTemplateCategory(compellentCategory,
                                                                           storageId,
                                                                           type,
                                                                           compellentStorageEntities,
                                                                           storageCategoriesMap);
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void createValidatedSystemConfigureTemplateCategory(final ServiceTemplateCategory category,
                                                                      final String categoryId,
                                                                      final String type,
                                                                      final List<DeviceInventoryEntity> entities,
                                                                      final Map<String, ConfigureTemplateCategory> storageCategoryMap) {
        if (category != null && category.getParameters() != null && category.getParameters().size() > 0) {
            ConfigureTemplateCategory compellentCategory = storageCategoryMap.get(categoryId);
            if (compellentCategory == null) {
                compellentCategory = new ConfigureTemplateCategory();
                compellentCategory.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_RESOURCE);
                compellentCategory.setDisplayName(categoryId);
                compellentCategory.setDeviceType(type);
                storageCategoryMap.put(categoryId, compellentCategory);
            }
            Map<String, ConfigureTemplateSetting> parametersMap = compellentCategory.getParameterMap();
            for (ServiceTemplateSetting setting : category.getParameters()) {
                switch (setting.getId()) {
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID:
                        ConfigureTemplateSetting volumeCountSetting = parametersMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_COUNT_ID);
                        if (volumeCountSetting == null) {
                            ConfigureTemplateSetting numberOfSetting = new ConfigureTemplateSetting();
                            numberOfSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_COUNT_ID);
                            numberOfSetting.setDisplayName("Storage Volumes to be Created");
                            numberOfSetting.setValue("1");
                            for (int i = 1; i <= 64; i++) {
                                numberOfSetting.getOptions().add(new ConfigureTemplateOption(Integer.toString(i), Integer.toString(i)));
                            }
                            compellentCategory.getParameters().add(numberOfSetting);
                        } else {
                            if (StringUtils.isNotBlank(volumeCountSetting.getValue())) {
                                Integer count = Integer.parseInt(volumeCountSetting.getValue());
                                volumeCountSetting.setValue(Integer.toString(count + 1));
                            }
                        }
                        if (parametersMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_STORAGE_ARRAY_ID) == null) {
                            ConfigureTemplateSetting storeageArraySetiing = new ConfigureTemplateSetting();
                            storeageArraySetiing.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_STORAGE_ARRAY_ID);
                            storeageArraySetiing.setDisplayName("Which of your storage arrays do you want to use?");
                            if (entities != null && entities.size() > 0) {
                                for (DeviceInventoryEntity deviceInventoryEntity : entities) {
                                    storeageArraySetiing.getOptions().add(new ConfigureTemplateOption(deviceInventoryEntity.getRefId(), deviceInventoryEntity.getServiceTag()));
                                }
                            }
                            compellentCategory.getParameters().add(storeageArraySetiing);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_SIZE:
                        if (parametersMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_ID) == null ||
                                parametersMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_ID) == null) {
                            String size = null;
                            if (StringUtils.isNotBlank(setting.getValue())) {
                                size = setting.getValue();
                            }
                            String prefix = "0";
                            String suffix = ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_MB;
                            if (size != null) {
                                List<String> parts = new LinkedList<>();
                                Matcher matcher = STORAGE_SIZE_PATTERN.matcher(size);
                                while (matcher.find()) {
                                    parts.add(matcher.group());
                                }
                                prefix = parts.get(0);
                                if (parts.size() == 2) {
                                    suffix = parts.get(1);
                                }
                            }
                            ConfigureTemplateSetting storageSizePrefixSetting = new ConfigureTemplateSetting();
                            storageSizePrefixSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_ID);
                            storageSizePrefixSetting.setDisplayName("Size of Storage Volumes");
                            storageSizePrefixSetting.setValue(prefix);
                            compellentCategory.getParameters().add(storageSizePrefixSetting);

                            ConfigureTemplateSetting storageSizeSuffixSetting = new ConfigureTemplateSetting();
                            storageSizeSuffixSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_ID);
                            storageSizeSuffixSetting.setValue(suffix);
                            storageSizeSuffixSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_MB,
                                                                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_MB));
                            storageSizeSuffixSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_GB,
                                                                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_GB));
                            storageSizeSuffixSetting.getOptions().add(new ConfigureTemplateOption(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_TB,
                                                                                                 ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_TB));
                            compellentCategory.getParameters().add(storageSizeSuffixSetting);
                        }
                        break;
                }
            }
            if (parametersMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_NAME_TEMPLATE_ID) == null) {
                ConfigureTemplateSetting volumeNameTemplateSetting = new ConfigureTemplateSetting();
                volumeNameTemplateSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_NAME_TEMPLATE_ID);
                volumeNameTemplateSetting.setDisplayName("Volume Name Template");
                volumeNameTemplateSetting.setValue("volume");
                compellentCategory.getParameters().add(volumeNameTemplateSetting);
            }

        }
    }


    public static void addConfigurationCategory(ConfigureTemplate configuration,
                                                Collection<ConfigureTemplateSetting> settings,
                                                String settingsCategoryId,
                                                String categoryName) {
        if (configuration != null) {
            ConfigureTemplateCategory category = new ConfigureTemplateCategory();
            category.setId(settingsCategoryId);
            category.setDisplayName(categoryName);
            category.getParameters().addAll(settings);
            configuration.getCategories().add(category);
        }
    }


    public static void addConfigurationAssociationCategory(ConfigureTemplate configuration,
                                                           Map<String, ConfigureTemplateSetting> settingsMap,
                                                           Map<String, Map<String, String>> associationsMap,
                                                           String settingsCategoryId,
                                                           String associationsCategoryId,
                                                           String categoryName) {
        if (configuration != null) {
            ConfigureTemplateCategory category = new ConfigureTemplateCategory();
            category.setId(settingsCategoryId);
            category.setDisplayName(categoryName);
            category.getParameters().addAll(settingsMap.values());
            configuration.getCategories().add(category);

            category = new ConfigureTemplateCategory();
            category.setId(associationsCategoryId);
            category.setDisplayName(categoryName + " Associations");
            for (Map.Entry<String, Map<String, String>> entry : associationsMap.entrySet()) {
                ConfigureTemplateSetting spAssociationSetting = new ConfigureTemplateSetting();
                spAssociationSetting.setId(entry.getKey());
                for (Map.Entry<String, String> association : entry.getValue().entrySet()) {
                    spAssociationSetting.getOptions().add(new ConfigureTemplateOption(association.getKey(), association.getValue()));
                }
                category.getParameters().add(spAssociationSetting);
            }
            configuration.getCategories().add(category);
        }
    }

    public static void addConfigureTemplateNetworkSettings(final ServiceTemplateComponent component,
                                                           Map<String, ConfigureTemplateSetting> networkSettingsMap,
                                                           Map<String, Map<String, String>> networkAssociationsMap,
                                                           final Map<NetworkType, Set<ConfigureTemplateOption>> networkTypesMap) {
        Set<Network> componentNetworks = new HashSet<>(ServiceTemplateClientUtil.findNetworks(component));
        if (componentNetworks != null && componentNetworks.size() > 0) {
            for (com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network network : componentNetworks) {
                ConfigureTemplateSetting networkSetting = networkSettingsMap.get(network.getId());
                if (networkSetting == null) {
                    networkSetting = new ConfigureTemplateSetting();
                    networkSetting.setId(network.getId());
                    networkSetting.setDisplayName(network.getName());
                    Set<ConfigureTemplateOption> networkListOptions = new LinkedHashSet<>();
                    Set<ConfigureTemplateOption> optionSet;
                    switch (network.getType()) {
                        case PRIVATE_LAN:
                        case PUBLIC_LAN:
                            optionSet = networkTypesMap.get(NetworkType.PRIVATE_LAN);
                            if (optionSet != null) {
                                networkListOptions.addAll(optionSet);
                            }
                            optionSet = networkTypesMap.get(NetworkType.PUBLIC_LAN);
                            if (optionSet != null) {
                                networkListOptions.addAll(optionSet);
                            }
                            break;
                        default:
                            optionSet = networkTypesMap.get(network.getType());
                            if (optionSet != null) {
                                networkListOptions.addAll(optionSet);
                            }
                            break;
                    }
                    networkSetting.setOptions(networkListOptions);
                    networkSetting.setValue(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SELECT_ID);
                    Set<com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network> networkList = new LinkedHashSet<>();
                    networkList.add(network);
                    networkSetting.setNetworks(networkList);
                    networkSettingsMap.put(network.getId(), networkSetting);
                }
                // Add Associations to components
                Map<String, String> networkOptions = networkAssociationsMap.get(network.getId());
                if (networkOptions == null) {
                    networkOptions = new LinkedHashMap<>();
                    networkAssociationsMap.put(network.getId(), networkOptions);
                }
                networkOptions.put(component.getType().name(), component.getName());
            }
        }
    }
}
