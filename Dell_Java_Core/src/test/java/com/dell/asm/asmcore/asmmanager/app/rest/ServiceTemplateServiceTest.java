package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplate;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.util.AsmManagerUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidator;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.Network;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

public class ServiceTemplateServiceTest {

    private ServiceTemplate schemaTemplate;
    private ServiceTemplate userTemplate;

    @Before
    public void setup() throws IOException {
        URL url = ServiceTemplateServiceTest.class.getClassLoader().getResource("ServiceTemplateServiceTest/defaulttemplate.xml");
        String xml = Resources.toString(url, Charsets.UTF_8);
        schemaTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, xml);
        URL url1 = ServiceTemplateServiceTest.class.getClassLoader().getResource("ServiceTemplateServiceTest/test_user_template.xml");
        String xml1 = Resources.toString(url1, Charsets.UTF_8);
        userTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, xml1);
    }

    // This test makes sure every parameter value that is not an option in the default template is left blank
    @Test
    public void testEmptyValues() {
        ServiceTemplateService.fillMissingParams(schemaTemplate,userTemplate);
        for (ServiceTemplateComponent component : userTemplate.getComponents()) {
            for (ServiceTemplateCategory category : component.getResources()) {
                for (ServiceTemplateSetting setting : category.getParameters()) {
                    ServiceTemplateSetting defaultParam = schemaTemplate.getTemplateSetting(component.getComponentID(),category.getId(),setting.getId());
                    List<String> optionValues = new ArrayList<>();
                    switch (setting.getType()) {
                    case LIST:
                    case ENUMERATED:
                        for (ServiceTemplateOption option : defaultParam.getOptions()) {
                            optionValues.add(option.getValue());
                        }
                        break;
                    default:
                        break;
                    }
                    if (optionValues.size() != 0) {
                        if (!optionValues.contains(setting.getValue())) {
                            assertEquals("",setting.getValue());
                        }
                    }
                }
            }
        }
    }
    
    // Test to make sure new parameters are being added to the user template
    @Test
    public void testNewParameters() {
        ServiceTemplateService.fillMissingParams(schemaTemplate,userTemplate);
        for (ServiceTemplateComponent component : userTemplate.getComponents()) {
            for (ServiceTemplateCategory category : component.getResources()) {
                if (StringUtils.equals(category.getId(),"asm::server")) {
                    List<String> paramsList = new ArrayList<>();
                    for (ServiceTemplateSetting setting : category.getParameters()) {
                        paramsList.add(setting.getId());
                    }
                    assertTrue(paramsList.contains("generate_host_name"));
                }
            }
        }
    }

    @Test
    public void testMetadataUpdated() {
        ServiceTemplateComponent server1 = null;
        for (ServiceTemplateComponent component : userTemplate.getComponents()) {
            if ("Server 1".equals(component.getName())) {
                server1 = component;
                break;
            }
        }
        assertNotNull("Could not find Server 1", server1);
        ServiceTemplateSetting setting = server1.getParameter(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
        setting.setDisplayName("Not the real display name");
        setting.setToolTip("Not the real tooltip");
        setting.setRequired(false);
        setting.setRequiredAtDeployment(false);
        setting.setHideFromTemplate(false);
        setting.setMin(42);
        setting.setMax(88);
        setting.setMaxLength(4096);
        setting.setReadOnly(true);
        setting.setInfoIcon(true);
        setting.setStep(99);

        ServiceTemplateService.fillMissingParams(schemaTemplate, userTemplate);

        ServiceTemplateComponent defaultServer = null;
        for (ServiceTemplateComponent component : schemaTemplate.getComponents()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL.equals(component.getId())) {
                defaultServer = component;
                break;
            }
        }
        assertNotNull("Could not find default server component", defaultServer);
        ServiceTemplateSetting origSetting = defaultServer.getParameter(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
        assertEquals(origSetting.getDisplayName(), setting.getDisplayName());
        assertEquals(origSetting.getToolTip(), setting.getToolTip());
        assertEquals(origSetting.isRequired(), setting.isRequired());
        assertEquals(origSetting.isRequiredAtDeployment(), setting.isRequiredAtDeployment());
        assertEquals(origSetting.isHideFromTemplate(), setting.isHideFromTemplate());
        assertEquals(origSetting.getMin(), setting.getMin());
        assertEquals(origSetting.getMax(), setting.getMax());
        assertEquals(origSetting.getMaxLength(), setting.getMaxLength());
        assertEquals(origSetting.isReadOnly(), setting.isReadOnly());
        assertEquals(origSetting.isInfoIcon(), setting.isInfoIcon());
        assertEquals(origSetting.getStep(), setting.getStep());
    }

    // Test to make sure missing properties are being added to the user template
    @Test
    public void testMissingProperties() {
        ServiceTemplateService.fillMissingParams(schemaTemplate,userTemplate);
        for (ServiceTemplateComponent component : userTemplate.getComponents()) {
            if (StringUtils.equals(component.getId(),"component-cluster-vcenter-1")) {
                ServiceTemplateCategory asmCluster = schemaTemplate.getTemplateResource(component,"asm::cluster");
                assertEquals("Cluster Settings",asmCluster.getDisplayName());
            }
        }
    }

    @Test
    public void testMissingPropertyOrdering() {
        ServiceTemplateService.fillMissingParams(schemaTemplate,userTemplate);
        for (ServiceTemplateComponent userComponent : userTemplate.getComponents()) {
            ServiceTemplateComponent schemaComponent = schemaTemplate.findComponentById(userComponent.getComponentID());
            List<String> schemaResourceOrder = new ArrayList<>();
            for (ServiceTemplateCategory resource : schemaComponent.getResources()) {
                schemaResourceOrder.add(resource.getId());
            }
            List<String> userResourceOrder = new ArrayList<>();
            for (ServiceTemplateCategory resource : userComponent.getResources()) {
                userResourceOrder.add(resource.getId());
            }
            assertEquals(schemaResourceOrder, userResourceOrder);

            for (ServiceTemplateCategory userResource : userComponent.getResources()) {
                ServiceTemplateCategory schemaResource = schemaComponent.getTemplateResource(userResource.getId());
                List<String> schemaParameterOrder = new ArrayList<>();
                for (ServiceTemplateSetting parameter : schemaResource.getParameters()) {
                    schemaParameterOrder.add(parameter.getId());
                }
                List<String> userParameterOrder = new ArrayList<>();
                for (ServiceTemplateSetting parameter : userResource.getParameters()) {
                    userParameterOrder.add(parameter.getId());
                }
                assertEquals(schemaParameterOrder, userParameterOrder);
            }
        }
    }

    @Test
    public void testAddApplyConfigurationServiceTemplateWithEquallogicStorage() {
        ServiceTemplate equallogicTemplate = null;
        try {
            URL url = ServiceTemplateServiceTest.class.getClassLoader().getResource("ServiceTemplateServiceTest/equallogicServiceTemplate.xml");
            assert url != null;
            String xml = Resources.toString(url, Charsets.UTF_8);
            equallogicTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, xml);
            equallogicTemplate.setTemplateType(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM);
        } catch (Exception e) {
            //eat it
        }
        testServiceTemplateConfiguration(equallogicTemplate);
    }

    private void testServiceTemplateConfiguration(ServiceTemplate serviceTemplate) {
        ServiceTemplateService serviceTemplateService = mockServiceTemplateService();
        serviceTemplateService.addServiceTemplateConfiguration(serviceTemplate);
        assertNotNull(serviceTemplate.getConfiguration());
        ConfigureTemplate configuration = serviceTemplate.getConfiguration();
        assertEquals("Configuration id should be asm::configuration", configuration.getId(), ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_ID);
        assertEquals(configuration.getCategories().size(), 10);
        for (ConfigureTemplateCategory category : configuration.getCategories()) {
            switch (category.getId()) {
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_NETWORKING_RESOURCE:
                assertEquals(category.getParameters().size(), 5);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    if (setting.getDisplayName() != null && setting.getDisplayName().equalsIgnoreCase("workload")) {
                        assertEquals(setting.getOptions().size(), 2);
                    } else {
                        assertEquals(setting.getOptions().size(), 1);
                    }
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                            setting.setValue(option.getId());
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_NETWORKING_ASSOCIATIONS_RESOURCE:
                assertEquals(category.getParameters().size(), 5);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    Set<String> names = new HashSet<>();
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        assertTrue(names.add(option.getName()));
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_RESOURCE:
                assertEquals(category.getParameters().size(), 2);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    assertEquals(setting.getOptions().size(), 1);
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        setting.setValue(option.getId());
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_ASSOCIATIONS_RESOURCE:
                assertEquals(category.getParameters().size(), 2);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    Set<String> names = new HashSet<>();
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        assertTrue(names.add(option.getName()));
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_RESOURCE:
                assertEquals(category.getParameters().size(), 1);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    assertEquals(setting.getOptions().size(), 1);
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        setting.setValue(option.getId());
                    }
                }
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_ASSOCIATIONS_RESOURCE:
                assertEquals(category.getParameters().size(), 1);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    Set<String> names = new HashSet<>();
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        assertTrue(names.add(option.getName()));
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_POOL_RESOURCE:
                assertEquals(category.getParameters().size(), 1);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    assertEquals(setting.getOptions().size(), 2);
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        setting.setValue(option.getId());
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_POOL_ASSOCIATIONS_RESOURCE:
                assertEquals(category.getParameters().size(), 1);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    Set<String> names = new HashSet<>();
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        assertTrue(names.add(option.getName()));
                    }
                }
                break;
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_RESOURCE:
                assertEquals(category.getParameters().size(), 1);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    assertEquals(setting.getOptions().size(), 1);
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        setting.setValue(option.getId());
                    }
                }
            case ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_ASSOCIATIONS_RESOURCE:
                assertEquals(category.getParameters().size(), 1);
                for (ConfigureTemplateSetting setting : category.getParameters()) {
                    Set<String> names = new HashSet<>();
                    for (ConfigureTemplateOption option : setting.getOptions()) {
                        assertTrue(names.add(option.getId()));
                    }
                }
                break;
            default:
                break;
            }
        }

        // Add OS Password Setting
        ConfigureTemplateCategory osPasswordCategory = new ConfigureTemplateCategory();
        osPasswordCategory.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_PASSWORD_RESOURCE);
        osPasswordCategory.setDisplayName("OS Password Settings");
        ConfigureTemplateSetting passwordSetting = new ConfigureTemplateSetting();
        passwordSetting.setId(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_ADMINISTRATOR_PASSWORD);
        passwordSetting.setDisplayName("OS Password");
        passwordSetting.setValue("P@ssw0rd");
        osPasswordCategory.getParameters().add(passwordSetting);
        serviceTemplate.getConfiguration().getCategories().add(osPasswordCategory);

        serviceTemplateService.applyServiceTemplateConfiguration(serviceTemplate);
        assertNull(serviceTemplate.getConfiguration());
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            String componentId = component.getComponentID() != null ? component.getComponentID() : component.getId();
            switch (component.getType()) {
            case SERVER:
                for (ServiceTemplateCategory serverCategory : component.getResources()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE.equals(serverCategory.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE.equals(serverCategory.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(serverCategory.getId())) {
                        for (ServiceTemplateSetting serverSetting : serverCategory.getParameters()) {
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID.equals(serverSetting.getId())) {
                                // Check for server pool setting and update value
                                assertEquals(serverSetting.getValue(), "100");
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(serverSetting.getId())) {
                                // Check for networking configuration and update network values
                                com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfiguration = serverSetting.getNetworkConfiguration();
                                if (networkConfiguration != null) {
                                    for (Fabric fabric : networkConfiguration.getInterfaces()) {
                                        for (Interface interfaces : fabric.getInterfaces()) {
                                            for (Partition partition : interfaces.getPartitions()) {
                                                if (partition.getNetworkObjects() != null) {
                                                    assertNull(partition.getNetworkObjects());
                                                    assertEquals(partition.getNetworks().size(), 1);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals(serverSetting.getId())) {
                                assertNull(serverSetting.getNetworks());
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID.equals(serverSetting.getId())) {
                                assertEquals(serverSetting.getValue(), "RAZOR_IMAGE");
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID.equals(serverSetting.getId()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID.equals(serverSetting.getId())) {
                                assertEquals(serverSetting.getValue(), "P@ssw0rd");
                            }
                        }
                    }
                }
                break;
            case VIRTUALMACHINE:
                for (ServiceTemplateCategory category : component.getResources()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE.equals(category.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE.equals(category.getId()) ||
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE.equals(category.getId())) {
                        for (ServiceTemplateSetting setting : category.getParameters()) {
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_IMAGE_ID.equals(setting.getId())) {
                                assertEquals(setting.getValue(), "RAZOR_IMAGE");
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID.equals(setting.getId()) ||
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID.equals(setting.getId())) {
                                assertEquals(setting.getValue(), "P@ssw0rd");
                            } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID.equals(setting.getId())) {
                                assertNull(setting.getNetworks());
                            }
                        }
                    }
                }
                break;
            case STORAGE:
                switch (componentId) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID:
                    ServiceTemplateSetting compellentStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    assertEquals(compellentStorageSetting.getValue(), "compellent");
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_EQL_COMP_ID:
                    ServiceTemplateSetting equallogicSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    assertEquals(equallogicSetting.getValue(), "equallogic");
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_NETAPP_COMP_ID:
                    ServiceTemplateSetting netappStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    assertEquals(netappStorageSetting.getValue(), "netapp");
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VNX_COMP_ID:
                    ServiceTemplateSetting vnxStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    assertEquals(vnxStorageSetting.getValue(), "emcvnx");
                    break;
                default:
                    break;
                }
                break;
            case CLUSTER:
                ServiceTemplateSetting clusterSetting;
                switch (componentId) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID:
                    clusterSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    assertEquals(clusterSetting.getValue(), "REF_ID");
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID:
                    clusterSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    assertEquals(clusterSetting.getValue(), "REF_ID");
                    break;
                default:
                    break;
                }
                break;
            }
        }
    }

    private static List<FilterParamParser.FilterInfo> getVCenterEntitiesFilterForTest() {
        List<String> filter = new ArrayList<>();
        filter.add("eq,deviceType,vcenter");
        filter.add("eq,state,READY");
        filter.add("eq,managedState,MANAGED");
        FilterParamParser filterParser = new FilterParamParser(filter,
                DeviceInventoryService.validFilterColumns);
        return filterParser.parse();
    }

    private static List<FilterParamParser.FilterInfo> getHypervEntitiesFilterForTest() {
        List<String> filter = new ArrayList<>();
        filter.add("eq,deviceType,scvmm");
        filter.add("eq,state,READY");
        filter.add("eq,managedState,MANAGED");
        FilterParamParser filterParser = new FilterParamParser(filter,
                DeviceInventoryService.validFilterColumns);
        return filterParser.parse();
    }

    private static List<FilterParamParser.FilterInfo>  getStorageDevicesFilterForTest() {
        List<String> filter = new ArrayList<>();
        filter.add("eq,deviceType,compellent,equallogic,netapp,emcvnx");
        filter.add("eq,state,READY");
        filter.add("eq,managedState,MANAGED");
        FilterParamParser filterParser = new FilterParamParser(
                filter, DeviceInventoryService.validFilterColumns);
        return filterParser.parse();
    }

    public static ServiceTemplateService mockServiceTemplateService() {
        ServiceTemplateUtil serviceTemplateUtil = mock(ServiceTemplateUtil.class);
        List<DeviceInventoryEntity> vCenterEntities = new ArrayList<>();
        DeviceInventoryEntity entity = new DeviceInventoryEntity();
        entity.setServiceTag("SERVICE_TAG");
        entity.setRefId("REF_ID");
        vCenterEntities.add(entity);
        when(serviceTemplateUtil.getVCenterEntities(false)).thenReturn(vCenterEntities);

        List<DeviceInventoryEntity> hypervEntities = new ArrayList<>();
        entity = new DeviceInventoryEntity();
        entity.setServiceTag("SERVICE_TAG");
        entity.setRefId("REF_ID");
        hypervEntities.add(entity);
        when(serviceTemplateUtil.getHypervEntities()).thenReturn(hypervEntities);

        Map<String, List<DeviceInventoryEntity>> storageMap = new HashMap<>();
        List<DeviceInventoryEntity> storageEntities = new ArrayList<>();
        DeviceInventoryEntity equallogic = new DeviceInventoryEntity();
        equallogic.setServiceTag("equallogic");
        equallogic.setRefId("equallogic");
        equallogic.setDeviceType(DeviceType.equallogic);
        storageEntities.add(equallogic);
        storageMap.put(DeviceType.equallogic.name(),storageEntities);

        storageEntities = new ArrayList<>();
        DeviceInventoryEntity compellent = new DeviceInventoryEntity();
        compellent.setServiceTag("compellent");
        compellent.setRefId("compellent");
        compellent.setDeviceType(DeviceType.compellent);
        storageEntities.add(compellent);
        storageMap.put(DeviceType.compellent.name(),storageEntities);

        storageEntities = new ArrayList<>();
        DeviceInventoryEntity netapp = new DeviceInventoryEntity();
        netapp.setServiceTag("netapp");
        netapp.setRefId("netapp");
        netapp.setDeviceType(DeviceType.netapp);
        storageEntities.add(netapp);
        storageMap.put(DeviceType.netapp.name(),storageEntities);

        storageEntities = new ArrayList<>();
        DeviceInventoryEntity emcvnx = new DeviceInventoryEntity();
        emcvnx.setServiceTag("emcvnx");
        emcvnx.setRefId("emcvnx");
        emcvnx.setDeviceType(DeviceType.emcvnx);
        storageEntities.add(emcvnx);
        storageMap.put(DeviceType.emcvnx.name(),storageEntities);

        when(serviceTemplateUtil.getStorageDevicesMap()).thenReturn(storageMap);

        DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
        Set<Long> users = new HashSet<>();
        users.add(0L);
        users.add(1L);
        users.add(2L);
        List<DeviceGroupEntity> deviceGroups = new ArrayList<>();
        DeviceGroupEntity group = new DeviceGroupEntity();
        group.setName("SERVER_POOL_1");
        group.setSeqId(100L);
        group.setGroupsUsers(users);
        deviceGroups.add(group);
        try {
            when(deviceGroupDAO.getAllDeviceGroup(null, null,null)).thenReturn(deviceGroups);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OSRepositoryUtil osRepositoryUtil = mock(OSRepositoryUtil.class);
        RazorRepo repo = new RazorRepo();
        repo.setName("RAZOR_IMAGE");
        List<RazorRepo> images = new ArrayList<>();
        images.add(repo);

        when(osRepositoryUtil.getRazorOSImages(false)).thenReturn(images);
        Map<String,String> repoNames = new HashMap<>();
        when(osRepositoryUtil.mapRazorRepoNamesToAsmRepoNames()).thenReturn(repoNames);

        INetworkService networkService = mock(INetworkService.class);
        Network[] networks = new Network[6];

        Network pxe = new Network();
        pxe.setId("PXE");
        pxe.setName("PXE");
        pxe.setType(NetworkType.PXE);
        networks[0] = pxe;

        Network hypervisor = new Network();
        hypervisor.setId("HYPERVISOR_MANAGEMENT");
        hypervisor.setName("HYPERVISOR_MANAGEMENT");
        hypervisor.setType(NetworkType.HYPERVISOR_MANAGEMENT);
        networks[1] = hypervisor;

        Network san = new Network();
        san.setId("STORAGE_ISCSI_SAN");
        san.setName("STORAGE_ISCSI_SAN");
        san.setType(NetworkType.STORAGE_ISCSI_SAN);
        networks[2] = san;

        Network vmotion = new Network();
        vmotion.setId("HYPERVISOR_MIGRATION");
        vmotion.setName("HYPERVISOR_MIGRATION");
        vmotion.setType(NetworkType.HYPERVISOR_MIGRATION);
        networks[3] = vmotion;

        Network workload = new Network();
        workload.setId("PRIVATE_LAN");
        workload.setName("PRIVATE_LAN");
        workload.setType(NetworkType.PRIVATE_LAN);
        networks[4] = workload;

        workload = new Network();
        workload.setId("PUBLIC_LAN");
        workload.setName("PUBLIC_LAN");
        workload.setType(NetworkType.PUBLIC_LAN);
        networks[5] = workload;
        when(networkService.getNetworks("name", null, null, null)).thenReturn(networks);

        FirmwareUtil firmwareUtil = mock(FirmwareUtil.class);

        AsmManagerUtil asmManagerUtil = mock(AsmManagerUtil.class);
        return new ServiceTemplateService(mock(DeviceInventoryDAO.class),
                                          mock(ServiceTemplateDAO.class),
                                          serviceTemplateUtil,
                                          mock(AddOnModuleComponentsDAO.class),
                                          mock(ServiceTemplateValidator.class),
                                          mock(LocalizableMessageService.class),
                                          networkService,
                                          mock(FirmwareRepositoryDAO.class),
                                          asmManagerUtil,
                                          deviceGroupDAO,
                                          osRepositoryUtil,
                                          firmwareUtil);
    }
}
