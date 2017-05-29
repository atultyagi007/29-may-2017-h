package com.dell.asm.asmcore.asmmanager.util.firmwarerepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.shiro.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReport;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReportComponent;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.asm.asmcore.asmmanager.client.firmware.SourceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;

import junit.framework.Assert;

public class FirmwareUtilTest {
    
    private static final Logger LOG = Logger.getLogger(FirmwareUtilTest.class);
    
    private static GenericDAO genericDAO;
    private FirmwareUtil firmwareUtil = null;
    FirmwareRepositoryDAO firmwareRepositoryDAO = null;
    DeviceInventoryDAO deviceInventoryDAO = null;
    DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = null;
    DeploymentDAO deploymentDAO = null;
    IJobManager jobManager = null;
    IJobHistoryManager jobHistoryManager = null;

    
    @Before
    public void setUp() {
        
        // this call will create a static instance of FirmwareUpdateUtil with a mock generic dao object
        firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
        deviceInventoryDAO = mock(DeviceInventoryDAO.class);
        deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
        deploymentDAO = mock(DeploymentDAO.class);
        genericDAO = mock(GenericDAO.class);
        jobManager = mock(IJobManager.class);
        jobHistoryManager = mock(IJobHistoryManager.class);
        
        firmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
                                        deviceInventoryDAO,
                                        deviceInventoryComplianceDAO,
                                        deploymentDAO,
                                        genericDAO,
                                        jobManager,
                                        jobHistoryManager);
    }

    @Test
    public void testCreateFirmwareUpdateComponent() {
        final List<SoftwareComponentEntity> mockSoftwareComponents = new ArrayList<SoftwareComponentEntity>();
        final SoftwareComponentEntity mockSoftwareComponent = mock(SoftwareComponentEntity.class);
        when(mockSoftwareComponent.getVendorVersion()).thenReturn("1.0");
        when(mockSoftwareComponent.getPath()).thenReturn("/path/to/upgrade");
        when(mockSoftwareComponent.getSoftwareComponent()).thenReturn(mock(SoftwareComponent.class));
        mockSoftwareComponents.add(mockSoftwareComponent);
        // return no software components
        when(genericDAO.getForEquals(anyMapOf(String.class,Object.class), anyString()))
                .thenReturn(null) // first call to default should return null
                .thenReturn(mockSoftwareComponents); // second call with embedded repo should return one software component

        final FirmwareDeviceInventoryEntity mockFdi = mock(FirmwareDeviceInventoryEntity.class);
        when(mockFdi.getId()).thenReturn(UUID.randomUUID().toString());
        when(mockFdi.getDeviceID()).thenReturn("1");
        final List<FirmwareDeviceInventoryEntity> outOfComplianceFirmware = 
                new ArrayList<FirmwareDeviceInventoryEntity>();
        outOfComplianceFirmware.add(mockFdi);
        
        ServiceTemplateComponent templateComponent = null;
        final boolean forceRestart = true;
        
        // if device and firmwareRepository are null then return null
        ServiceTemplateComponent softwareComponent = firmwareUtil.createFirmwareUpdateComponent("", null, 
                outOfComplianceFirmware, null, templateComponent,  null, forceRestart);
        assertNull(softwareComponent);

        // if device is not null but firmwareRepository is null return null
        final DeviceInventoryEntity mockDeviceInventory = mock(DeviceInventoryEntity.class);
        when(mockDeviceInventory.getDiscoverDeviceType()).thenReturn(DiscoverDeviceType.FORCE10_S4810);
        when(mockDeviceInventory.getDeviceType()).thenReturn(DeviceType.dellswitch);
        when(mockDeviceInventory.getServiceTag()).thenReturn("dell_ftos-172.17.5.14");
        when(mockDeviceInventory.getModel()).thenReturn("S4810");

        softwareComponent = firmwareUtil.createFirmwareUpdateComponent("", mockDeviceInventory, outOfComplianceFirmware, null, 
                templateComponent,  null, forceRestart);
        assertNull(softwareComponent);

        //create template component settings
        templateComponent = new ServiceTemplateComponent();
        ServiceTemplateCategory osCategory = new ServiceTemplateCategory();
        osCategory.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        templateComponent.getResources().add(osCategory);

        // create esxi image type
        ServiceTemplateSetting osImageType = new ServiceTemplateSetting();
        osImageType.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
        osImageType.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE);
        osCategory.getParameters().add(osImageType);

        //TODO Add Test data to validate vcenter cluster settings

        ServiceTemplateSetting osPassword = new ServiceTemplateSetting();
        osPassword.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
        osPassword.setValue("P@ssw0rd");
        osCategory.getParameters().add(osPassword);

        // if device is null but firmwareRepository is not null then return null
        final FirmwareRepositoryEntity mockDefaultFirmwareRepository = mock(FirmwareRepositoryEntity.class);
        
        final String componentName = null;
        when(this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(mockFdi.getDeviceID())).thenReturn(new HashSet<FirmwareDeviceInventoryEntity>(outOfComplianceFirmware));

        final List<FirmwareRepositoryEntity> repos = new ArrayList<FirmwareRepositoryEntity>();
        final FirmwareRepositoryEntity mockEmbeddedFirmwareRepository = mock(FirmwareRepositoryEntity.class);
        when(mockEmbeddedFirmwareRepository.isDefault()).thenReturn(false);
        when(mockEmbeddedFirmwareRepository.isEmbedded()).thenReturn(true);
        repos.add(mockEmbeddedFirmwareRepository);
        when(genericDAO.getForEquals(anyMapOf(String.class,Object.class), any(Class.class))).thenReturn((List)repos);
        
        try {
            softwareComponent = firmwareUtil.createFirmwareUpdateComponent(componentName, mockDeviceInventory,
                                                                           outOfComplianceFirmware, mockDefaultFirmwareRepository, templateComponent, null, forceRestart);
            fail("Exception should be thrown for not having ip address or hostname");
        } catch (Exception e) {
            assertTrue(e instanceof LocalizedWebApplicationException);
        }

        ServiceTemplateSetting osHostName = new ServiceTemplateSetting();
        osHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
        osHostName.setValue("OS_HOST_NAME");
        osCategory.getParameters().add(osHostName);

        try {
            softwareComponent = firmwareUtil.createFirmwareUpdateComponent(componentName, mockDeviceInventory,
                                                                           outOfComplianceFirmware, mockDefaultFirmwareRepository, templateComponent, null, forceRestart);
            verifySoftwareComponent(softwareComponent, osPassword.getValue(), osHostName.getValue());
        } catch (Exception e) {
            fail("Exception should not be thrown for not having ip address or hostname");
        }

        ServiceTemplateCategory networkConfigurationCategory = new ServiceTemplateCategory();
        networkConfigurationCategory.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);

        ServiceTemplateSetting networkConfigSetting = new ServiceTemplateSetting();
        networkConfigSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID);
        Network network = new Network();
        StaticNetworkConfiguration staticConfig = new StaticNetworkConfiguration();
        staticConfig.setIpAddress("172.18.2.100");
        network.setStatic(true);
        network.setStaticNetworkConfiguration(staticConfig);
        List<Network> networks = new ArrayList<>();
        networkConfigSetting.setNetworks(networks);
        networks.add(network);
        networkConfigurationCategory.getParameters().add(networkConfigSetting);

        templateComponent.getResources().add(networkConfigurationCategory);

        try {
            softwareComponent = firmwareUtil.createFirmwareUpdateComponent(componentName, mockDeviceInventory,
                                                                           outOfComplianceFirmware, mockDefaultFirmwareRepository, templateComponent,  null, forceRestart);
            verifySoftwareComponent(softwareComponent, osPassword.getValue(), "172.18.2.100");
        } catch (Exception e) {
            fail("Exception should not be thrown for not having ip address or hostname");
        }
    }

    @Test
    public void testGetSoftwareComponents() {
        final List<SoftwareComponentEntity> components = new ArrayList<SoftwareComponentEntity>();
        components.add(mock(SoftwareComponentEntity.class));
        when(genericDAO.getForEquals(anyMapOf(String.class,Object.class), anyString())).thenReturn(components);
        
        final FirmwareRepositoryEntity mockFirmwareRepository = mock(FirmwareRepositoryEntity.class);
        when(mockFirmwareRepository.getId()).thenReturn(UUID.randomUUID().toString());
        when(mockFirmwareRepository.getName()).thenReturn("Firmware Repo name");
        
        // if there are no params and a systemId is present then we should get results
        List<SoftwareComponent> softwareComps = 
                firmwareUtil.getSoftwareComponents(null, null, null, null, null, mockFirmwareRepository, "1", null, null, false);
        assertNotNull(softwareComps);
        assertFalse(CollectionUtils.isEmpty(softwareComps));
        softwareComps = firmwareUtil.getSoftwareComponents("", null, null, null, null, mockFirmwareRepository, "1", null, null, false);
        assertNotNull(softwareComps);

        // if there are no params and null or empty systemId then null should be returned
        softwareComps = firmwareUtil.getSoftwareComponents(null, null, null, null, null, mockFirmwareRepository, "", null, null, false);
        assertNull(softwareComps);
        softwareComps = firmwareUtil.getSoftwareComponents(null, null, null, null, null, mockFirmwareRepository, null, null, null, false);
        assertNull(softwareComps);
        softwareComps = firmwareUtil.getSoftwareComponents("", null, null, null, null, mockFirmwareRepository, "", null, null, false);
        assertNull(softwareComps);
        softwareComps = firmwareUtil.getSoftwareComponents("", null, null, null, null, mockFirmwareRepository, null, null, null, false);
        assertNull(softwareComps);
        
        // if there are params and null or empty systemId then we should get results
        softwareComps = firmwareUtil.getSoftwareComponents("1", null, null, null, null, mockFirmwareRepository, "", null, null, false);
        assertNotNull(softwareComps);
        softwareComps = firmwareUtil.getSoftwareComponents("1", null, null, null, null, mockFirmwareRepository, null, null, null, false);
        assertNotNull(softwareComps); 
        
        // if there are params and systemId then we should get results
        softwareComps = firmwareUtil.getSoftwareComponents("1", null, null, null, null, mockFirmwareRepository, "1", null, null, false);
        assertNotNull(softwareComps);
        
        softwareComps = firmwareUtil.getSoftwareComponents("1", null, null, null, null, null, "1", null, null, false);
        assertNotNull(softwareComps);
        assertTrue(CollectionUtils.isEmpty(softwareComps));
        
        
    }
    
    @Test
    public void testToJson() {
        List<FirmwareUpdateInfo> updates = new ArrayList<>();
        FirmwareUpdateInfo update = new FirmwareUpdateInfo();
        update.setComponentId("1234");
        update.setInstanceId("Integrated.Nothing-1");
        update.setUriPath("/foo/bar/baz.txt");
        update.setComponentId("4312");
        update.setInstanceId("Integrated.Nothing-2");
        update.setUriPath("/foo/bar/quux.txt");
        update.setVersion("Version1");
        updates.add(update);
        String s = FirmwareUtil.toJson(updates);
        assertEquals("[{\"instance_id\":\"Integrated.Nothing-2\",\"component_id\":\"4312\",\"uri_path\":" 
                + "\"/foo/bar/quux.txt\",\"version\":\"Version1\"}]", s);
    }

    private void verifySoftwareComponent(final ServiceTemplateComponent softwareComponent, final String osPassword, final String esxHostname) {
        assertNotNull(softwareComponent);
        assertNotNull(softwareComponent.getResources());
        assertEquals(softwareComponent.getResources().size(),1);
        for (ServiceTemplateCategory category : softwareComponent.getResources()) {
            for (ServiceTemplateSetting setting : category.getParameters()) {
                switch (setting.getId()) {
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_ESX_PASSWORD:
                    assertEquals(osPassword,setting.getValue());
                    break;
                case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_ESX_HOSTNAME:
                    assertEquals(esxHostname,setting.getValue());
                    break;
                default:
                }
            }
        }
    }

    @Test
    public void testUnmanageFirmware() {
        DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setName("unmanagedTest");
        deploymentEntity.setManageFirmware(true);
        deploymentEntity.setUseDefaultCatalog(true);
        FirmwareRepositoryEntity fre = new FirmwareRepositoryEntity();
        fre.getDeployments().add(deploymentEntity);
        deploymentEntity.setFirmwareRepositoryEntity(fre);
        when(firmwareRepositoryDAO.saveOrUpdate(fre)).thenReturn(fre);
        firmwareUtil.unmanageFirmware(deploymentEntity);
        assertFalse(deploymentEntity.isManageFirmware());
        assertFalse(deploymentEntity.isUseDefaultCatalog());
        assertNull(deploymentEntity.getFirmwareRepositoryEntity());
    }

    @Test
    public void testManageServiceTemplateFirmware() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        ServiceTemplateEntity entity = new ServiceTemplateEntity();
        serviceTemplate.setManageFirmware(true);
        FirmwareRepositoryEntity fre = new FirmwareRepositoryEntity();
        fre.setId("FirmwareRepositoryEntity");
        FirmwareRepository firmwareRepository = new FirmwareRepository();
        firmwareRepository.setId("FirmwareRepository");
        serviceTemplate.setFirmwareRepository(firmwareRepository);
        when(firmwareRepositoryDAO.getCompleteFirmware(firmwareRepository.getId(), true, true)).thenReturn(fre);
        firmwareUtil.manageServiceTemplateFirmware(serviceTemplate, entity);
        assertFalse(entity.isUseDefaultCatalog());
        assertEquals(entity.getFirmwareRepositoryEntity().getId(), fre.getId());

        serviceTemplate.setUseDefaultCatalog(true);
        firmwareUtil.manageServiceTemplateFirmware(serviceTemplate, entity);
        assertTrue(entity.isUseDefaultCatalog());
        assertNull(entity.getFirmwareRepositoryEntity());

        serviceTemplate.setManageFirmware(false);
        firmwareUtil.manageServiceTemplateFirmware(serviceTemplate, entity);
        assertFalse(entity.isUseDefaultCatalog());
        assertNull(entity.getFirmwareRepositoryEntity());

    }

    @Test
    public void testManageDeploymentFirmware() {
        Deployment deployment = new Deployment();
        DeploymentEntity entity = new DeploymentEntity();
        firmwareUtil.manageDeploymentFirmware(deployment, entity);
        assertFalse(deployment.isUpdateServerFirmware());
        assertFalse(deployment.isUseDefaultCatalog());
        assertNull(deployment.getFirmwareRepository());
        assertNull(deployment.getFirmwareRepositoryId());
        assertFalse(entity.isManageFirmware());
        assertFalse(entity.isUseDefaultCatalog());
        assertNull(entity.getFirmwareRepositoryEntity());

        FirmwareRepositoryEntity fre = new FirmwareRepositoryEntity();
        fre.setId("FirmwareRepositoryEntity");
        FirmwareRepository firmwareRepository = new FirmwareRepository();
        firmwareRepository.setId("FirmwareRepository");
        deployment.setUpdateServerFirmware(true);
        deployment.setFirmwareRepositoryId(firmwareRepository.getId());
        deployment.setFirmwareRepository(firmwareRepository);
        when(firmwareRepositoryDAO.get(firmwareRepository.getId())).thenReturn(fre);
        firmwareUtil.manageDeploymentFirmware(deployment, entity);
        assertFalse(deployment.isUseDefaultCatalog());
        assertEquals(deployment.getFirmwareRepository().getId(), fre.getId());
        assertFalse(entity.isUseDefaultCatalog());
        assertEquals(entity.getFirmwareRepositoryEntity().getId(), fre.getId());
    }
    
    
   @Test
   public void testUpdateFirmwareComplianceForDevices() throws Exception {
           
       List<DeviceInventoryEntity> deviceInventoryEnts = new ArrayList<DeviceInventoryEntity>(); 
       FirmwareRepositoryEntity serviceRepo = new FirmwareRepositoryEntity();
       serviceRepo.setName("My Service Repo");
       serviceRepo.setId("serviceRepoId1234");
    
       FirmwareRepositoryEntity defaultRepo = new FirmwareRepositoryEntity();
       defaultRepo.setName("My Default Repo");
       defaultRepo.setId("defaultRepoId1234");
       List<FirmwareRepositoryEntity> defaultRepos = new ArrayList<FirmwareRepositoryEntity>();
       defaultRepos.add(defaultRepo);
       
       FirmwareRepositoryEntity embeddedRepo = new FirmwareRepositoryEntity();
       embeddedRepo.setName("My Embedded Repo");
       embeddedRepo.setId("embeddedRepoId1234");
       List<FirmwareRepositoryEntity> embeddedRepos = new ArrayList<FirmwareRepositoryEntity>();
       embeddedRepos.add(embeddedRepo);
       
       DeviceInventoryEntity serverInvEntity = new DeviceInventoryEntity();
       serverInvEntity.setDeviceType(DeviceType.BladeServer);
       serverInvEntity.setRefId("1234");
       deviceInventoryEnts.add(serverInvEntity);
       
       HashMap<String, Object> embeddedAttributeMap = new HashMap<String, Object>();
       embeddedAttributeMap.put("isEmbedded", Boolean.TRUE);
       
       HashMap<String, Object> defaultAttributeMap = new HashMap<String, Object>();
       defaultAttributeMap.put("isDefault", Boolean.TRUE);

       DeviceInventoryComplianceEntity serverDevInvCom = new DeviceInventoryComplianceEntity();
       serverDevInvCom.setCompliance(CompliantState.COMPLIANT);
       
       DeviceInventoryComplianceEntity defaultDevInvCom = new DeviceInventoryComplianceEntity();
       defaultDevInvCom.setCompliance(CompliantState.NONCOMPLIANT);
       
       DeviceInventoryComplianceEntity embeddedDevInvCom = new DeviceInventoryComplianceEntity();
       embeddedDevInvCom.setCompliance(CompliantState.NONCOMPLIANT);

       when(genericDAO.getForEquals(embeddedAttributeMap, FirmwareRepositoryEntity.class)).thenReturn(embeddedRepos);
       when(genericDAO.getForEquals(defaultAttributeMap, FirmwareRepositoryEntity.class)).thenReturn(defaultRepos);
       
       when(deviceInventoryComplianceDAO.get(serverInvEntity, embeddedRepo)).thenReturn(embeddedDevInvCom);
       when(deviceInventoryComplianceDAO.get(serverInvEntity, defaultRepo)).thenReturn(defaultDevInvCom);
       when(deviceInventoryComplianceDAO.get(serverInvEntity, serviceRepo)).thenReturn(serverDevInvCom);

       // Verify it's taking the Service compliance accordingly
       doAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               Object[] args = invocation.getArguments();
               DeviceInventoryEntity deviceInvEnt = (DeviceInventoryEntity)args[0];
               Assert.assertNotNull("DeviceInventoryEntity cannot be null!", deviceInvEnt);
               Assert.assertTrue("Compliance for the Service Repo should be compliant!", CompliantState.COMPLIANT.equals(CompliantState.valueOf(deviceInvEnt.getCompliant())));
               return null;
           }
       }).when(deviceInventoryDAO).updateDeviceInventory(serverInvEntity);
       
       this.firmwareUtil.updateFirmwareComplianceForDevices(deviceInventoryEnts, serviceRepo);
       
       // Verify it's taking the default accordingly when there is no Service Catalog
       doAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               Object[] args = invocation.getArguments();
               DeviceInventoryEntity deviceInvEnt = (DeviceInventoryEntity)args[0];
               Assert.assertNotNull("DeviceInventoryEntity cannot be null!", deviceInvEnt);
               Assert.assertTrue("Compliance for the Default Repo should be Non-Compliant!", CompliantState.NONCOMPLIANT.equals(CompliantState.valueOf(deviceInvEnt.getCompliant())));
               return null;
           }
       }).when(deviceInventoryDAO).updateDeviceInventory(serverInvEntity);
       
       this.firmwareUtil.updateFirmwareComplianceForDevices(deviceInventoryEnts, null);
       
       // Verify it's taking the embedded accordingly when there is no Service Catalog & no Default Catalog
       doAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               Object[] args = invocation.getArguments();
               DeviceInventoryEntity deviceInvEnt = (DeviceInventoryEntity)args[0];
               Assert.assertNotNull("DeviceInventoryEntity cannot be null!", deviceInvEnt);
               Assert.assertTrue("Compliance for the Embedded Repo should be UpdateRequired!", CompliantState.UPDATEREQUIRED.equals(CompliantState.valueOf(deviceInvEnt.getCompliant())));
               return null;
           }
       }).when(deviceInventoryDAO).updateDeviceInventory(serverInvEntity);
       
       embeddedDevInvCom.setCompliance(CompliantState.UPDATEREQUIRED);
       
       this.firmwareUtil.updateFirmwareComplianceForDevices(deviceInventoryEnts, null);
   }
       
   @Test
   public void testUpdateComplianceMapForRepo() throws Exception {
       
       FirmwareUtil testFirmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
               deviceInventoryDAO,
               deviceInventoryComplianceDAO,
               deploymentDAO,
               genericDAO,
               jobManager,
               jobHistoryManager) {
           
           public CompliantState getFirmwareCompliance(final DeviceInventoryEntity deviceInventory,
                   final FirmwareRepositoryEntity firmwareRepository,
                   final FirmwareDeviceInventoryEntity fdi) {
               return CompliantState.NONCOMPLIANT;
           }
       };
       
       FirmwareRepositoryEntity serviceRepo = new FirmwareRepositoryEntity();
       serviceRepo.setId("serviceRepoId-1234");
       
       DeviceInventoryEntity deviceInventory = new DeviceInventoryEntity();
       deviceInventory.setRefId("deviceInventoryRefId-1234");
       
       // Setup get all device inventory call
       ArrayList<DeviceInventoryEntity> die = new ArrayList<DeviceInventoryEntity>();
       die.add(deviceInventory);
       when(this.deviceInventoryDAO.getAllDeviceInventory()).thenReturn(die);
       
       Set<FirmwareDeviceInventoryEntity> firmDevInvs = new HashSet<FirmwareDeviceInventoryEntity>();
       
       // Non-Compliant Catalog
       FirmwareDeviceInventoryEntity nonCompliantFirmDevInvEnt1 = new FirmwareDeviceInventoryEntity();
       nonCompliantFirmDevInvEnt1.setName("Non-CompliantCatalog");
       nonCompliantFirmDevInvEnt1.setSource(SourceType.Catalog.getValue());
       firmDevInvs.add(nonCompliantFirmDevInvEnt1);
       
       when(deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(deviceInventory.getRefId())).thenReturn(firmDevInvs);
       
       // Verify we are handling the Catalog items that don't exist on a server properly
       doAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               Object[] args = invocation.getArguments();
               DeviceInventoryComplianceEntity dice = (DeviceInventoryComplianceEntity)args[0];
               Assert.assertNotNull("DeviceInventoryComplianceEntity cannot be null!", dice);
               Assert.assertTrue("Compliance for a Catalog that does not exist on Server should be Non-Compliant!", CompliantState.NONCOMPLIANT.equals(dice.getCompliance()));
               System.out.println("Compliance check for Non-Compliant / Missing Catalog Device success!");
               return null;
           }
       }).when(deviceInventoryComplianceDAO).saveOrUpdate(any(DeviceInventoryComplianceEntity.class));

       // Test 
       testFirmwareUtil.updateComplianceMapForRepo(serviceRepo);
       
       // Non-Compliant Devices 
       nonCompliantFirmDevInvEnt1.setName("Non-CompliantDevice");
       nonCompliantFirmDevInvEnt1.setSource(SourceType.Device.getValue());
       
       // Verify we are handling non-compliant Devices properly
       doAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               Object[] args = invocation.getArguments();
               DeviceInventoryComplianceEntity dice = (DeviceInventoryComplianceEntity)args[0];
               Assert.assertNotNull("DeviceInventoryComplianceEntity cannot be null!", dice);
               Assert.assertTrue("Compliance for a Non-Compliant device should be Non-Compliant!", CompliantState.NONCOMPLIANT.equals(dice.getCompliance()));
               System.out.println("Compliance check for Non-Compliant Device success!");
               return null;
           }
       }).when(deviceInventoryComplianceDAO).saveOrUpdate(any(DeviceInventoryComplianceEntity.class));

       // Test
       testFirmwareUtil.updateComplianceMapForRepo(serviceRepo);
       
       FirmwareDeviceInventoryEntity compliantFirmDevInvEnt1 = new FirmwareDeviceInventoryEntity();
       compliantFirmDevInvEnt1.setName("CompliantDevice");
       compliantFirmDevInvEnt1.setSource(SourceType.Device.getValue());
       firmDevInvs.clear();
       firmDevInvs.add(compliantFirmDevInvEnt1);
       
       // Reset to use COMPLIANT
       testFirmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
               deviceInventoryDAO,
               deviceInventoryComplianceDAO,
               deploymentDAO,
               genericDAO,
               jobManager,
               jobHistoryManager) {
           
           public CompliantState getFirmwareCompliance(final DeviceInventoryEntity deviceInventory,
                   final FirmwareRepositoryEntity firmwareRepository,
                   final FirmwareDeviceInventoryEntity fdi) {
               return CompliantState.COMPLIANT;
           }
       };       
       
       // Verify a Compliant Device
       doAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               Object[] args = invocation.getArguments();
               DeviceInventoryComplianceEntity dice = (DeviceInventoryComplianceEntity)args[0];
               Assert.assertNotNull("DeviceInventoryComplianceEntity cannot be null!", dice);
               Assert.assertTrue("Compliance for a Compliant device should be Compliant!", CompliantState.COMPLIANT.equals(dice.getCompliance()));
               System.out.println("Compliance check for Compliant Device success!");
               return null;
           }
       }).when(deviceInventoryComplianceDAO).saveOrUpdate(any(DeviceInventoryComplianceEntity.class));

       // Test
       testFirmwareUtil.updateComplianceMapForRepo(serviceRepo);
   }

    @Test
    public void testGetFirmwareComplianceReportForDeviceWithDefaultRepo() {

        DeviceInventoryEntity serverInvEntity = new DeviceInventoryEntity();
        serverInvEntity.setDeviceType(DeviceType.BladeServer);
        serverInvEntity.setRefId("1234");
        serverInvEntity.setServiceTag("ABCD");
        serverInvEntity.setIpAddress("172.22.5.100");
        serverInvEntity.setModel("Server 10000");
        serverInvEntity.setManagedState(ManagedState.MANAGED);
        serverInvEntity.setState(DeviceState.READY);

        FirmwareUtil firmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
                                                     deviceInventoryDAO,
                                                     deviceInventoryComplianceDAO,
                                                     deploymentDAO,
                                                     genericDAO,
                                                     jobManager,
                                                     jobHistoryManager) {
            @Override
            public FirmwareRepositoryEntity getDefaultRepo() {
                FirmwareRepositoryEntity defaultRepo = new FirmwareRepositoryEntity();
                defaultRepo.setName("My Default Repo");
                defaultRepo.setId("defaultRepoId1234");
                return defaultRepo;
            }

            @Override
            public FirmwareRepositoryEntity getEmbeddedRepo() {
                return null;
            }

            @Override
            public List<FirmwareComplianceReportComponent> getComponentsForDefaultAndEmbeddedRepos(DeviceInventoryEntity deviceInventoryEntity,
                                                                                                    FirmwareRepositoryEntity embeddedRepoEntity,
                                                                                                    FirmwareRepositoryEntity defaultRepoEntity) {
                ArrayList<FirmwareComplianceReportComponent> firmwareComplianceReportComponents = new ArrayList<>();
                return firmwareComplianceReportComponents;
            }
        };

        FirmwareComplianceReport report = firmwareUtil.getFirmwareComplianceReportForDevice(serverInvEntity);
        assertNotNull(report);
        assertTrue(serverInvEntity.getServiceTag().equals(report.getServiceTag()));
        assertTrue(serverInvEntity.getIpAddress().equals(report.getIpAddress()));
        assertTrue(report.getFirmwareRepositoryName().equals("Default Catalog - My Default Repo"));
        assertTrue(report.isCompliant());
        assertTrue(serverInvEntity.getState().equals(report.getDeviceState()));
        assertTrue(serverInvEntity.getManagedState().equals(report.getManagedState()));
        assertTrue(serverInvEntity.getDeviceType().equals(report.getDeviceType()));
        assertTrue(report.isAvailable());
        assertFalse(report.isEmbededRepo());

    }

    @Test
    public void testGetFirmwareComplianceReportForDeviceWithEmbeddedRepo() {

        DeviceInventoryEntity serverInvEntity = new DeviceInventoryEntity();
        serverInvEntity.setDeviceType(DeviceType.BladeServer);
        serverInvEntity.setRefId("1234");
        serverInvEntity.setServiceTag("ABCD");
        serverInvEntity.setIpAddress("172.22.5.100");
        serverInvEntity.setModel("Server 10000");
        serverInvEntity.setManagedState(ManagedState.MANAGED);
        serverInvEntity.setState(DeviceState.READY);

        FirmwareUtil firmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
                                                     deviceInventoryDAO,
                                                     deviceInventoryComplianceDAO,
                                                     deploymentDAO,
                                                     genericDAO,
                                                     jobManager,
                                                     jobHistoryManager) {
            @Override
            public FirmwareRepositoryEntity getDefaultRepo() {
                return null;
            }

            @Override
            public FirmwareRepositoryEntity getEmbeddedRepo() {
                FirmwareRepositoryEntity embeddedRepo = new FirmwareRepositoryEntity();
                embeddedRepo.setName("My Embedded Repo");
                embeddedRepo.setId("embeddedRepoId1234");
                embeddedRepo.setEmbedded(true);
                return embeddedRepo;
            }

            @Override
            public List<FirmwareComplianceReportComponent> getComponentsForDefaultAndEmbeddedRepos(DeviceInventoryEntity deviceInventoryEntity,
                                                                                                   FirmwareRepositoryEntity embeddedRepoEntity,
                                                                                                   FirmwareRepositoryEntity defaultRepoEntity) {
                ArrayList<FirmwareComplianceReportComponent> firmwareComplianceReportComponents = new ArrayList<>();
                return firmwareComplianceReportComponents;
            }
        };

        FirmwareComplianceReport report = firmwareUtil.getFirmwareComplianceReportForDevice(serverInvEntity);
        assertNotNull(report);
        assertTrue(serverInvEntity.getServiceTag().equals(report.getServiceTag()));
        assertTrue(serverInvEntity.getIpAddress().equals(report.getIpAddress()));
        assertTrue(report.getFirmwareRepositoryName().equals("Embedded Catalog - My Embedded Repo"));
        assertTrue(report.isCompliant());
        assertTrue(serverInvEntity.getState().equals(report.getDeviceState()));
        assertTrue(serverInvEntity.getManagedState().equals(report.getManagedState()));
        assertTrue(serverInvEntity.getDeviceType().equals(report.getDeviceType()));
        assertTrue(report.isAvailable());
        assertTrue(report.isEmbededRepo());

    }

    @Test
    public void testGetFirmwareComplianceReportForDeviceWithException() {

        DeviceInventoryEntity serverInvEntity = new DeviceInventoryEntity();
        serverInvEntity.setDeviceType(DeviceType.BladeServer);
        serverInvEntity.setRefId("1234");
        serverInvEntity.setServiceTag("ABCD");
        serverInvEntity.setIpAddress("172.22.5.100");
        serverInvEntity.setModel("Server 10000");
        serverInvEntity.setManagedState(ManagedState.MANAGED);
        serverInvEntity.setState(DeviceState.READY);

        FirmwareUtil firmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
                                                     deviceInventoryDAO,
                                                     deviceInventoryComplianceDAO,
                                                     deploymentDAO,
                                                     genericDAO,
                                                     jobManager,
                                                     jobHistoryManager) {
            @Override
            public FirmwareRepositoryEntity getDefaultRepo() {
                return null;
            }

            @Override
            public FirmwareRepositoryEntity getEmbeddedRepo() {
                return null;
            }

            @Override
            public List<FirmwareComplianceReportComponent> getComponentsForDefaultAndEmbeddedRepos(DeviceInventoryEntity deviceInventoryEntity,
                                                                                                   FirmwareRepositoryEntity embeddedRepoEntity,
                                                                                                   FirmwareRepositoryEntity defaultRepoEntity) {
                ArrayList<FirmwareComplianceReportComponent> firmwareComplianceReportComponents = new ArrayList<>();
                return firmwareComplianceReportComponents;
            }
        };

        try {
            FirmwareComplianceReport report = firmwareUtil.getFirmwareComplianceReportForDevice(serverInvEntity);
        } catch (IllegalArgumentException iae) {
            assertNotNull(iae);
        } catch (Exception e) {
            fail("Should not throw exception!");
        }

    }
    
}
