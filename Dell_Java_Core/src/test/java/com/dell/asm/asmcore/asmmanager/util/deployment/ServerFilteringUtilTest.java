package com.dell.asm.asmcore.asmmanager.util.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dell.asm.asmcore.asmmanager.app.rest.FirmwareRepositoryService;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentFilterResponse;
import com.dell.asm.asmcore.asmmanager.client.deployment.RejectedServer;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedServer;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.server.app.rest.ServerDeviceService;
import com.dell.pg.asm.chassis.app.rest.ChassisService;
import com.dell.pg.asm.chassis.client.device.IChassisService;
import com.dell.pg.asm.server.client.device.*;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;

public class ServerFilteringUtilTest {
	private ServerFilteringUtil serverUtil;

    private DeviceGroupDAO groupDao;
    private DeviceInventoryDAO inventoryDao;
    private NetworkingUtil networkUtil;
    private HardwareUtil hardwareUtil;
    private LocalizableMessageService logService;
    private FirmwareRepositoryService firmwareRepositoryService;
    private IChassisService chassisService;
    private IServerDeviceService serverDeviceService;

    private int numberOfServers = 0;


    @Before
	public void setUp() {
        groupDao = mock(DeviceGroupDAO.class);
        inventoryDao = mock(DeviceInventoryDAO.class);
        networkUtil = mock(NetworkingUtil.class);

        logService = mock(LocalizableMessageService.class);

        //hardwareUtil = mock(HardwareUtil.class);
        // We actually want to use the real one here:
        hardwareUtil = new HardwareUtil();


        firmwareRepositoryService = mock(FirmwareRepositoryService.class);
        chassisService = mock(ChassisService.class);
        serverDeviceService = mock(ServerDeviceService.class);

        ServerFilteringUtil serverFilteringUtil =
                new ServerFilteringUtil(groupDao, inventoryDao, hardwareUtil, logService,
                        firmwareRepositoryService, serverDeviceService);
        setServerUtil(serverFilteringUtil);
	}

	public ServerFilteringUtil getServerUtil() {
		return serverUtil;
	}

	public void setServerUtil(ServerFilteringUtil serverUtil) {
		this.serverUtil = serverUtil;
	}

	@Test
	public void testClonePath() throws IOException {

		String cloneR = "R620";
		List<DeviceInventoryEntity> servers = entityInv();

		List<DeviceInventoryEntity> serversRSort = ServerFilteringUtil
				.sortServersByHealth(servers, cloneR);
		List<DeviceInventoryEntity> exp = expectedR630();
		assertTrue(checkEqual(serversRSort, exp));

	}

    @Test
    public void testIsNICEnabled() throws Exception {
        String bios = "[{\"attributeId\":0,\"serviceTag\":null,\"lastRetrievalTime\":null,\"attributeDisplayName\":" +
                "\"Integrated Network Card 1\",\"attributeName\":\"IntegratedNetwork1\",\"caption\":null,\"currentValue\":\"[\\\"Disabled\\\"]\"," +
                "\"defaultValue\":\"[]\",\"dependency\":null,\"description\":null,\"displayOrder\":1404,\"elementName\":null,\"fqdd\":\"BIOS.Setup.1-1\"," +
                "\"groupDisplayName\":\"Integrated Devices\",\"groupID\":\"IntegratedDevices\",\"instanceID\":" +
                "\"BIOS.Setup.1-1:IntegratedNetwork1\",\"pendingValue\":\"[]\",\"possibleValues\":\"[\\\"Enabled\\\",\\\"DisabledOs\\\"]\"," +
                "\"possibleValuesDescription\":\"[\\\"Enabled\\\",\\\"Disabled (OS)\\\"]\",\"any\":[],\"otherAttributes\":{}," +
                "\"orderedList\":false,\"readOnly\":false},\n" +
                "{\"attributeId\":0,\"serviceTag\":null,\"lastRetrievalTime\":null,\"attributeDisplayName\":" +
                "\"Mezzanine Slot 1C\",\"attributeName\":\"Slot1\",\"caption\":null,\"currentValue\":\"[\\\"Disabled\\\"]\"," +
                "\"defaultValue\":\"[]\",\"dependency\":null,\"description\":null,\"displayOrder\":1500,\"elementName\":null," +
                "\"fqdd\":\"BIOS.Setup.1-1\",\"groupDisplayName\":\"Mezzanine Slot Disablement\",\"groupID\":\"SlotDisablement\"," +
                "\"instanceID\":\"BIOS.Setup.1-1:Slot1\",\"pendingValue\":\"[]\",\"possibleValues\":\"[\\\"Enabled\\\"," +
                "\\\"Disabled\\\",\\\"BootDriverDisabled\\\"]\",\"possibleValuesDescription\":\"[\\\"Enabled\\\"," +
                "\\\"Disabled\\\",\\\"Boot Driver Disabled\\\"]\",\"any\":[],\"otherAttributes\":{}," +
                "\"orderedList\":false,\"readOnly\":false},{\"attributeId\":0,\"serviceTag\":null,\"lastRetrievalTime\":null," +
                "\"attributeDisplayName\":\"Mezzanine Slot 2B\",\"attributeName\":\"Slot2\",\"caption\":null,\"currentValue\":" +
                "\"[\\\"Enabled\\\"]\",\"defaultValue\":\"[]\",\"dependency\":null,\"description\":null,\"displayOrder\":1501," +
                "\"elementName\":null,\"fqdd\":\"BIOS.Setup.1-1\",\"groupDisplayName\":\"Mezzanine Slot Disablement\"," +
                "\"groupID\":\"SlotDisablement\",\"instanceID\":\"BIOS.Setup.1-1:Slot2\",\"pendingValue\":\"[]\"," +
                "\"possibleValues\":\"[\\\"Enabled\\\",\\\"Disabled\\\",\\\"BootDriverDisabled\\\"]\"," +
                "\"possibleValuesDescription\":\"[\\\"Enabled\\\",\\\"Disabled\\\",\\\"Boot Driver Disabled\\\"]\"," +
                "\"any\":[],\"otherAttributes\":{},\"orderedList\":false,\"readOnly\":false}]";
        LogicalNetworkInterface nic = new LogicalNetworkInterface();
        nic.setFqdd("NIC.Mezzanine.2B-1-1");
        nic.setProductName("Broadcom NetXtreme Gigabit Ethernet");
        nic.setIdentityList(new HashSet<LogicalNetworkIdentityInventory>());
        nic.getIdentityList().add(new LogicalNetworkIdentityInventory());

        assertTrue("2B was not enabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setFqdd("NIC.Mezzanine.1C-1-1");
        assertFalse("1C was not disabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setFqdd("NIC.Integrated.1-1-1");
        assertFalse("Integrated was not disabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setProductName("Broadcom NetXtreme Gigabit Ethernet 5720");
        nic.setFqdd("NIC.Mezzanine.2B-1-1");
        assertFalse("5720 was not disabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setProductName("Broadcom NetXtreme Gigabit Ethernet 57810");
        nic.setFqdd("NIC.Embedded.2B-1-1");
        assertTrue("Embedded & 57810 2B was not enabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setProductName("Broadcom NetXtreme Gigabit Ethernet 57800");
        nic.setFqdd("NIC.Embedded.2B-1-1");
        assertFalse("Embedded & 57800 2B was not disabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setProductName("Intel(R) Ethernet 10G X520 LOM");
        nic.setFqdd("NIC.Embedded.1-1-1");
        assertTrue("Embedded & X520 was not enabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

        nic.setProductName("Intel(R) Ethernet 10G X540");
        nic.setFqdd("NIC.Embedded.1-1-1");
        assertFalse("Embedded & X540 was enabled", serverUtil.isNICEnabled(nic, bios, new HashMap<String, List<Map>>()));

    }

	private boolean checkEqual(List<DeviceInventoryEntity> serversRSort,
			List<DeviceInventoryEntity> exp) {
		boolean isMatch = true;
		for (int i = 0; i < serversRSort.size(); i++) {
			if (!serversRSort.get(i).getRefId().equals(exp.get(i).getRefId()))
				return false;
		}
		return isMatch;
	}

	public List<DeviceInventoryEntity> entityInv() {

		List<DeviceInventoryEntity> servers = new ArrayList<DeviceInventoryEntity>();
	
		DeviceInventoryEntity entity1 = new DeviceInventoryEntity();
		entity1.setRefId("2");
		entity1.setHealth(DeviceHealth.GREEN);
        entity1.setManagedState(ManagedState.MANAGED);
		entity1.setFailuresCount(0);
		entity1.setModel("PowerEdge M820");
		servers.add(entity1);
		DeviceInventoryEntity entity = new DeviceInventoryEntity();
		entity.setRefId("1");
		entity.setHealth(DeviceHealth.GREEN);
        entity.setManagedState(ManagedState.MANAGED);
		entity.setFailuresCount(3);
		entity.setModel("PowerEdge M620");
		servers.add(entity);
		DeviceInventoryEntity entity2 = new DeviceInventoryEntity();
		entity2.setRefId("3");
		entity2.setHealth(DeviceHealth.GREEN);
        entity2.setManagedState(ManagedState.MANAGED);
		entity2.setFailuresCount(0);
		entity2.setModel("PowerEdge M620");
		servers.add(entity2);
		DeviceInventoryEntity entity3 = new DeviceInventoryEntity();
		entity3.setRefId("4");
		entity3.setHealth(DeviceHealth.YELLOW);
        entity3.setManagedState(ManagedState.MANAGED);
		entity3.setFailuresCount(1);
		entity3.setModel("PowerEdge M620");
		servers.add(entity3);
		DeviceInventoryEntity entity4 = new DeviceInventoryEntity();
		entity4.setRefId("5");
		entity4.setHealth(DeviceHealth.YELLOW);
        entity4.setManagedState(ManagedState.MANAGED);
		entity4.setFailuresCount(1);
		entity4.setModel("PowerEdge R730");
		servers.add(entity4);
		DeviceInventoryEntity entity5 = new DeviceInventoryEntity();
		entity5.setRefId("6");
		entity5.setHealth(DeviceHealth.GREEN);
        entity5.setManagedState(ManagedState.MANAGED);
		entity5.setFailuresCount(0);
		entity5.setModel("PowerEdge R620");
		servers.add(entity5);
		DeviceInventoryEntity entity6 = new DeviceInventoryEntity();
		entity6.setRefId("7");
		entity6.setHealth(DeviceHealth.RED);
        entity6.setManagedState(ManagedState.MANAGED);
		entity6.setFailuresCount(1);
		entity6.setModel("PowerEdge R630");
		servers.add(entity6);
		DeviceInventoryEntity entity7 = new DeviceInventoryEntity();
		entity7.setRefId("8");
		entity7.setHealth(DeviceHealth.RED);
        entity7.setManagedState(ManagedState.MANAGED);
		entity7.setFailuresCount(0);
		entity7.setModel("PowerEdge R630");
		servers.add(entity7);
		return servers;
	}

	public List<DeviceInventoryEntity> expectedR630() {

		List<DeviceInventoryEntity> servers = new ArrayList<DeviceInventoryEntity>();
		DeviceInventoryEntity entity5 = new DeviceInventoryEntity();
		entity5.setRefId("6");
		entity5.setHealth(DeviceHealth.GREEN);
		entity5.setFailuresCount(0);
		entity5.setModel("PowerEdge R620");
		servers.add(entity5);
		DeviceInventoryEntity entity7 = new DeviceInventoryEntity();
		entity7.setRefId("8");
		entity7.setHealth(DeviceHealth.RED);
		entity7.setFailuresCount(0);
		entity7.setModel("PowerEdge R630");
		servers.add(entity7);
		DeviceInventoryEntity entity1 = new DeviceInventoryEntity();
		entity1.setRefId("2");
		entity1.setHealth(DeviceHealth.GREEN);
		entity1.setFailuresCount(0);
		entity1.setModel("PowerEdge M820");
		servers.add(entity1);
		DeviceInventoryEntity entity2 = new DeviceInventoryEntity();
		entity2.setRefId("3");
		entity2.setHealth(DeviceHealth.GREEN);
		entity2.setFailuresCount(0);
		entity2.setModel("PowerEdge M620");
		servers.add(entity2);
		DeviceInventoryEntity entity4 = new DeviceInventoryEntity();
		entity4.setRefId("5");
		entity4.setHealth(DeviceHealth.YELLOW);
		entity4.setFailuresCount(1);
		entity4.setModel("PowerEdge R730");
		servers.add(entity4);
		DeviceInventoryEntity entity6 = new DeviceInventoryEntity();
		entity6.setRefId("7");
		entity6.setHealth(DeviceHealth.RED);
		entity6.setFailuresCount(1);
		entity6.setModel("PowerEdge R630");
		servers.add(entity6);
		DeviceInventoryEntity entity3 = new DeviceInventoryEntity();
		entity3.setRefId("4");
		entity3.setHealth(DeviceHealth.YELLOW);
		entity3.setFailuresCount(1);
		entity3.setModel("PowerEdge M620");
		servers.add(entity3);
		DeviceInventoryEntity entity = new DeviceInventoryEntity();
		entity.setRefId("1");
		entity.setHealth(DeviceHealth.GREEN);
		entity.setFailuresCount(3);
		entity.setModel("PowerEdge M620");
		servers.add(entity);

		return servers;
	}

    @Test
    public void sampleTestMock() throws IOException {
        String[] svcTags = new String[]{"64WF3W1","JV7QQV1","FM7X6X1","7WSV6X1","8WSV6X1","3PQW6X1","1TKJQV1","9W4H942","2SPJ382"};
        String[] refIds = new String[]{"ff8080814d9c4382014da0f5e11f40a2","ff8080814faeef51014fcba878a5076d","ff8080814fcbba8a014fd0db649f0462",
                "ff8080814fcbba8a014fd0dbdded0528","ff8080814fcbba8a014fd0db5a2803e9","ff8080814fd0eb76014fd1f7d8220087",
                "ff8080814fd0eb76014fd1f9665900f1","ff8080815255fd7b0152560e710301e6","ff808081575807740157581ebe1c0084"};

        numberOfServers = svcTags.length;

        String chassisId = "ff8080814f742b7a014f74a928160002";
        String chassisTag = "ENV05C1";

        ObjectMapper mapper = new ObjectMapper();

        List<DeviceGroupDAO.BriefServerInfo> briefServerInfos = new ArrayList<>();
        List<DeviceInventoryEntity> inventory = new ArrayList<>();

        for (int i=0; i< refIds.length; i++) {
            DeviceGroupDAO.BriefServerInfo info = new DeviceGroupDAO.BriefServerInfo(refIds[i], svcTags[i], DeviceState.READY);
            briefServerInfos.add(info);

            URL resource = this.getClass().getClassLoader().getResource("managed_device_compliance_"+svcTags[i]+".json");
            String text = IOUtils.toString(resource, Charsets.UTF_8);
            ManagedDevice managedDevice = mapper.readValue(text, ManagedDevice.class);

            DeviceInventoryEntity deviceInventoryEntity = DeviceInventoryUtils.toEntity(managedDevice,true);
            inventory.add(deviceInventoryEntity);

            resource = this.getClass().getClassLoader().getResource("server_"+svcTags[i]+".json");
            text = IOUtils.toString(resource, Charsets.UTF_8);
            Server server = mapper.readValue(text, Server.class);
            when(serverDeviceService.getServer(refIds[i])).thenReturn(server);
        }
        when(groupDao.getAccessiblePoolServers(1, "-1")).thenReturn(briefServerInfos);

        List<String> refIdList = new ArrayList<>(Arrays.asList(refIds));
        when(inventoryDao.getDevicesByIds(refIdList)).thenReturn(inventory);

        //chassis
        URL resource = this.getClass().getClassLoader().getResource("chassis_" + chassisTag + ".json");
        String text = IOUtils.toString(resource, Charsets.UTF_8);
        Chassis chassis = mapper.readValue(text, Chassis.class);
        when(chassisService.getChassis(chassisId)).thenReturn(chassis);

        List<Chassis> chassisList = new ArrayList<>();
        chassisList.add(chassis);

        when(chassisService.getChassises(null, null, 0, 0)).thenReturn(chassisList);
    }

    private String getRaidConfigString() {
        String basicRaid1 = "{\"raidtype\":\"basic\",\"basicraidlevel\":\"raid1\",\"enableglobalhotspares\":true,\"globalhotspares\":\"1\",\"minimumssd\":\"0\",\"virtualdisks\":[]," +
                "\"enableglobalhotsparesexternal\":\"true\", \"globalhotsparesexternal\":\"1\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}";
        return basicRaid1;
    }
    private String getRaidConfigNoHotsparesString() {
        String basicRaid1 = "{\"raidtype\":\"basic\",\"basicraidlevel\":\"raid1\",\"enableglobalhotspares\":false,\"globalhotspares\":\"0\",\"minimumssd\":\"0\",\"virtualdisks\":[]," +
                "\"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}";
        return basicRaid1;
    }

    private FilterEnvironment buildFilterEnvironment(String raidConfiguration) throws IOException{
        String templateName = "template_blade_a_2by10g.json";
        return buildFilterEnvironment(raidConfiguration, templateName);
    }

    private FilterEnvironment buildFilterEnvironment(String raidConfiguration, String templateName) throws IOException{
        ServiceTemplate template = readTemplate(templateName);
        return buildFilterEnvironment(raidConfiguration, template);
    }

    private ServiceTemplate readTemplate(String templateName) throws IOException {
        URL uri = this.getClass().getClassLoader().getResource(templateName);
        String text = IOUtils.toString(uri, Charsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(text, ServiceTemplate.class);
    }

    private FilterEnvironment buildFilterEnvironment(String raidConfiguration, ServiceTemplate template) throws IOException{

        upgradeNetworks(template);
        FilterEnvironment environment = new FilterEnvironment();
        environment.setNetworkProxy(mock(INetworkService.class));
        environment.initEnvironment(template.getComponents().get(0));

        // Disable check on firmwareCompliance
        environment.setCompliantState(CompliantState.UNKNOWN);
        environment.setNetworkProxy(mock(INetworkService.class));

        String configString = "{ \"templateRaidConfiguration\" : " + raidConfiguration + "}";
        TemplateRaidConfiguration configuration = MarshalUtil.fromJSON(TemplateRaidConfiguration.class, configString);
        environment.setRaidConfiguration(configuration);
        return environment;
    }

    @Test
    public void testServerRaidRejection() throws IOException {
        int numberOfInstances = 1;
        String poolId = "-1";
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigString() );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();
        responseLocal.setSelectedServers(new ArrayList<SelectedServer>());
        responseLocal.setRejectedServers(new ArrayList<RejectedServer>());

        serverUtil.getAvailableServerFromPool(poolId,deploymentEnvironment,responseLocal,filterEnv,numberOfInstances);

        assertEquals(numberOfServers, responseLocal.getRejectedServers().size());
        assertEquals(RejectedServer.Reason.RAID.toString(),responseLocal.getRejectedServers().get(0).getReason());
        assertEquals(RejectedServer.Reason.RAID.toString(),responseLocal.getRejectedServers().get(1).getReason());

    }

    @Test
    public void testServersHealthSort() throws IOException {
        int numberOfInstances = 2;
        String poolId = "-1";
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString() );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();
        responseLocal.setSelectedServers(new ArrayList<SelectedServer>());
        responseLocal.setRejectedServers(new ArrayList<RejectedServer>());

        serverUtil.getAvailableServerFromPool(poolId,deploymentEnvironment,responseLocal,filterEnv,numberOfInstances);
        assertEquals(2, responseLocal.getRejectedServers().size());
        assertEquals(2, responseLocal.getSelectedServers().size());

        // We expect the first server to have GREEN health if they were sorted properly
        String refId = responseLocal.getSelectedServers().get(0).getRefId();
        Server server = serverDeviceService.getServer(refId);
        assertEquals(Health.GREEN, server.getHealth());
    }

    @Test
    public void testFilterBlade2by10Ports() throws IOException {
        int numberOfInstances = -1;
        String poolId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID;
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString() );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(poolId,deploymentEnvironment,responseLocal,filterEnv,numberOfInstances);
        assertEquals(2, responseLocal.getRejectedServers().size());
        assertEquals(numberOfServers - 2, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testFilterServer2by10Ports() throws IOException {
        int numberOfInstances = -1;
        String poolId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID;
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString(), "template_server_2by10g.json" );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(poolId,deploymentEnvironment,responseLocal,filterEnv,numberOfInstances);
        assertEquals(2, responseLocal.getRejectedServers().size());
        assertEquals(numberOfServers - 2, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testFilterServer4by10Ports() throws IOException {
        int numberOfInstances = -1;
        String poolId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID;
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString(), "template_server_4by10g.json" );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(poolId,deploymentEnvironment,responseLocal,filterEnv,numberOfInstances);
        assertEquals(numberOfServers-1, responseLocal.getRejectedServers().size());
        assertEquals(1, responseLocal.getSelectedServers().size());
        assertEquals("9W4H942", responseLocal.getSelectedServers().get(0).getServiceTag());
    }

    @Test
    public void testFilterServer2by10and2by1Ports() throws IOException {
        int numberOfInstances = -1;
        String poolId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID;
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString(), "template_server_2x10_2x1.json" );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(poolId, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);
        assertEquals(numberOfServers - 4, responseLocal.getRejectedServers().size());
        assertEquals(4, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testFilterServer2x10and2x1and2x10Ports() throws IOException {
        int numberOfInstances = -1;
        String poolId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID;
        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString(), "template_server_2x10_2x1_2x10.json" );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(poolId, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);
        assertEquals(numberOfServers - 3, responseLocal.getRejectedServers().size());
        assertEquals(3, responseLocal.getSelectedServers().size());
    }

    protected static void upgradeNetworks(ServiceTemplate serviceTemplate) {
        for (ServiceTemplateComponent component: serviceTemplate.getComponents()) {
            for (ServiceTemplateCategory resource: component.getResources()) {
                ServiceTemplateSetting networkingSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                if (networkingSetting!=null)
                    ServiceTemplateUtil.upgradeParameter(networkingSetting, serviceTemplate.getTemplateVersion());
            }
        }
    }

    @Test
    public void testFilterServerSATADOM() throws IOException {
        int numberOfInstances = -1;

        long userId=1;

        setUp();
        sampleTestMock();

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigString(), "template_server_2by10g_esx.json" );
        filterEnv.setRaidConfiguration(null); // don't need RAID for this template

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);

        assertEquals(numberOfServers - 1, responseLocal.getRejectedServers().size());
        assertEquals(1, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testFilterServerHybridVSANPass() throws IOException {
        int numberOfInstances = -1;

        long userId=1;

        setUp();
        sampleTestMock();

        Server srv = serverDeviceService.getServer("ff808081575807740157581ebe1c0084");
        // clear satadom references
        srv.setBios(srv.getBios().replaceAll("SATADOM","xxx"));

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigString(), "template_server_2by10g_esx.json" );
        filterEnv.setRaidConfiguration(null); // don't need RAID for this template

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);
        // one should pass for Hybrid VSAN
        assertEquals(numberOfServers - 1, responseLocal.getRejectedServers().size());
        assertEquals(1, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testFilterServerHybrydVSANFail() throws IOException {
        int numberOfInstances = -1;

        long userId=1;

        setUp();
        sampleTestMock();

        Server srv = serverDeviceService.getServer("ff808081575807740157581ebe1c0084");
        // clear satadom references
        srv.setBios(srv.getBios().replaceAll("SATADOM","xxx"));
        // set all drives to HDD
        for (Controller ctr: srv.getControllers()) {
            if (ctr.getProductName().contains("H730")) {
                for (PhysicalDisk pd: ctr.getPhysicalDisks()) {
                    pd.setMediaType(PhysicalDisk.PhysicalMediaType.HDD);
                }
            }
        }

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigString(), "template_server_2by10g_esx.json" );
        filterEnv.setRaidConfiguration(null); // don't need RAID for this template

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);
        // all should fail
        assertEquals(numberOfServers, responseLocal.getRejectedServers().size());
    }

    @Test
    public void testFilterServerVSANAllFlash() throws IOException {
        int numberOfInstances = -1;

        long userId = 1;

        setUp();
        sampleTestMock();

        Server srv = serverDeviceService.getServer("ff808081575807740157581ebe1c0084");
        // clear satadom references
        srv.setBios(srv.getBios().replaceAll("SATADOM", "xxx"));
        // set all drives to SSD
        for (Controller ctr : srv.getControllers()) {
            if (ctr.getProductName().contains("H730")) {
                for (PhysicalDisk pd : ctr.getPhysicalDisks()) {
                    pd.setMediaType(PhysicalDisk.PhysicalMediaType.SSD);
                }
            }
        }

        FilterEnvironment filterEnv = buildFilterEnvironment(getRaidConfigString(), "template_server_2by10g_esx.json");
        filterEnv.setRaidConfiguration(null); // don't need RAID for this template
        filterEnv.setAllFlash(true); // we want all SSDs

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);
        // one should pass
        assertEquals(numberOfServers - 1, responseLocal.getRejectedServers().size());
        assertEquals(1, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testUnsupportedNICs() throws IOException {
        int numberOfInstances = -1;
        String poolId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID;
        long userId=1;

        setUp();
        sampleTestMock();

        Server server = serverDeviceService.getServer("ff8080814d9c4382014da0f5e11f40a2");

        for (LogicalNetworkInterface itf:  server.getNetworkInterfaceList()) {
            String pn = itf.getProductName();
            itf.setProductName(pn.replaceAll("57810","5719"));
        }

        server = serverDeviceService.getServer("ff8080814faeef51014fcba878a5076d");

        for (LogicalNetworkInterface itf:  server.getNetworkInterfaceList()) {
            String pn = itf.getProductName();
            itf.setProductName(pn.replaceAll("57810","5720"));
        }

        FilterEnvironment filterEnv = buildFilterEnvironment( getRaidConfigNoHotsparesString(), "template_server_2by10g.json" );

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(poolId,deploymentEnvironment,responseLocal,filterEnv,numberOfInstances);
        assertEquals(4, responseLocal.getRejectedServers().size());
        assertEquals(numberOfServers - 4, responseLocal.getSelectedServers().size());
    }

    @Test
    public void testFilterServerVSANBroadcomQuad() throws IOException {
        int numberOfInstances = -1;

        long userId=1;

        setUp();
        sampleTestMock();

        // get the quad port from this server
        Server serverGoodNIC = serverDeviceService.getServer("ff8080815255fd7b0152560e710301e6");
        // and move here
        Server serverGoodStorage = serverDeviceService.getServer("ff808081575807740157581ebe1c0084");
        serverGoodStorage.getNetworkInterfaceList().clear();
        serverGoodStorage.getNetworkInterfaceList().addAll(serverGoodNIC.getNetworkInterfaceList());

        ServiceTemplate template = readTemplate("template_server_4by10g.json");

        ServiceTemplate templateVSAN = readTemplate("template_server_2by10g_esx.json");

        ServiceTemplateSetting netSet4x10 = template.getTemplateSetting("C8073E9A-F448-4684-82B0-7E11F960000E",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID,ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);

        ServiceTemplateSetting netSetVSAN = templateVSAN.getTemplateSetting("C5B03B98-8925-49C9-B370-15100849BD6E",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID,ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);

        // for VSAN template set networking required 4x10 ports
        netSetVSAN.setValue(netSet4x10.getValue());

        FilterEnvironment filterEnv = buildFilterEnvironment(getRaidConfigString(), templateVSAN);
        filterEnv.setRaidConfiguration(null); // don't need RAID for this templateVSAN

        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setUserID(userId);

        DeploymentFilterResponse responseLocal = new DeploymentFilterResponse();

        serverUtil.getAvailableServerFromPool(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID, deploymentEnvironment, responseLocal, filterEnv, numberOfInstances);

        assertEquals(numberOfServers - 1, responseLocal.getRejectedServers().size());
        assertEquals(1, responseLocal.getSelectedServers().size());
    }
}
