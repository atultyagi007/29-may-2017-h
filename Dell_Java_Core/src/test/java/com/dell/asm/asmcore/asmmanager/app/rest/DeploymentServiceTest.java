package com.dell.asm.asmcore.asmmanager.app.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtilTest;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidatorTest;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.util.RestUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerAppConfig;
import com.dell.asm.asmcore.asmmanager.app.TestAsmManagerAppConfig;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentDevice;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentFilterResponse;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentHealthStatusType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentValid;
import com.dell.asm.asmcore.asmmanager.client.deployment.PuppetLogEntry;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedServer;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.hardware.RAIDConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentNamesRefDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.tasks.ServiceDeploymentJob;
import com.dell.asm.asmcore.asmmanager.util.AsmManagerUtil;
import com.dell.asm.asmcore.asmmanager.util.DeploymentValidator;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidator;
import com.dell.asm.asmcore.asmmanager.util.deployment.DeploymentEnvironment;
import com.dell.asm.asmcore.asmmanager.util.deployment.DnsUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.FilterEnvironment;
import com.dell.asm.asmcore.asmmanager.util.deployment.HostnameUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.MigrationDeviceUtils;
import com.dell.asm.asmcore.asmmanager.util.deployment.NetworkingUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServerFilteringUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.asmdeployer.client.AsmDeployerComponentStatus;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.pg.asm.identitypool.api.common.model.Link;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypoolmgr.ioidentity.IIOIdentityMgr;
import com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentity;
import com.dell.pg.asm.identitypoolmgr.ioidentity.impl.IOIdentityMgr;
import com.dell.pg.asm.identitypoolmgr.network.IIPAddressPoolMgr;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.common.context.ServiceContext;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.PingUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.google.common.base.Charsets;

@RunWith(Enclosed.class)
public class DeploymentServiceTest {

    protected static void setupSecurityContext(DeploymentService service) {
        Deployment[] deployments = service.getDeployments(null, null, 0, 9999, false);
        ServiceContext.Context sc = ServiceContext.get();
        sc.setUserId((long)2);
        sc.setUserName("system");
        sc.setApiKey("unittests");
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

    public static <T> T loadXmlResource(Class<T> klazz, String path) throws IOException {
        URL resource = DeploymentServiceTest.class.getClassLoader().getResource(path);
        assertNotNull("Failed to load resource at " + path, resource);
        String text = IOUtils.toString(resource, Charsets.UTF_8);
        return MarshalUtil.unmarshal(klazz, text);
    }

    public static ServiceTemplateComponent findComponentByRefId(Deployment target, String refId) {
        for (ServiceTemplateComponent component : target.getServiceTemplate().getComponents()) {
            if (refId.equals(component.getAsmGUID())) {
                return component;
            }
        }
        return null;
    }

    public static ServiceTemplateComponent findComponentById(Deployment target, String componentId) {
        for (ServiceTemplateComponent component : target.getServiceTemplate().getComponents()) {
            if (componentId.equals(component.getId())) {
                return component;
            }
        }
        return null;
    }

    public static FilterEnvironment makeEnvironment(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        ServiceTemplateComponent component = (ServiceTemplateComponent)args[0];

        FilterEnvironment fe = new FilterEnvironment();
        fe.setNetworkProxy(mock(INetworkService.class));
        fe.initEnvironment(component);
        return fe;
    }
    
    public static class DeploymentServiceApiTests{
        
        private Deployment deploymentRuby;
        private IIOIdentityMgr ioIdentityMgr;
        private INetworkService networkProxy;
        private com.dell.pg.asm.identitypool.api.network.model.Network fetchNetworkSettingsResponsePXE;
        private com.dell.pg.asm.identitypool.api.network.model.Network fetchNetworkSettingsResponseISCSI;
        private com.dell.pg.asm.identitypool.api.network.model.Network fetchNetworkSettingsResponseHypervisorManagement;
        private com.dell.pg.asm.identitypool.api.network.model.Network fetchNetworkSettingsResponseWorkload;
        private com.dell.pg.asm.identitypool.api.network.model.Network fetchNetworkSettingsResponseVMotion;
        private NetworkConfiguration netConfig;
        private NetworkingUtil networkingUtil;


        @Before
    	public void setUp() throws Exception {

			AsmManagerApp asmManagerApp = new AsmManagerApp();
			asmManagerApp.setServiceTemplateValidator(ServiceTemplateValidatorTest.mockServiceTemplateValidator());
			asmManagerApp.setServiceTemplateService(ServiceTemplateServiceTest.mockServiceTemplateService());
			asmManagerApp.setAddOnModuleService(AddOnModuleServiceTest.mockAddOnModuleService());
			asmManagerApp.setTemplateDao(mock(ServiceTemplateDAO.class));
			asmManagerApp.setDeploymentDao(mock(DeploymentDAO.class));
			asmManagerApp.setDeviceInventoryDAO(mock(DeviceInventoryDAO.class));
			asmManagerApp.setFirmwareRepositoryDAO(mock(FirmwareRepositoryDAO.class));
			asmManagerApp.setAddOnModuleDAO(mock(AddOnModuleDAO.class));
			asmManagerApp.setAddOnModuleComponentsDAO(mock(AddOnModuleComponentsDAO.class));
			asmManagerApp.setOsRepositoryUtil(mock(OSRepositoryUtil.class));
			AsmManagerApp.setAsmManagerAppConfig(new TestAsmManagerAppConfig());

    		fetchNetworkSettingsResponsePXE = new com.dell.pg.asm.identitypool.api.network.model.Network();
    		fetchNetworkSettingsResponsePXE.setId("ff808081452c813b01452cefa56c00cd");
    		fetchNetworkSettingsResponsePXE.setName("PXE");
    		fetchNetworkSettingsResponsePXE.setDescription("");
    		fetchNetworkSettingsResponsePXE.setType(NetworkType.PRIVATE_LAN);
    		fetchNetworkSettingsResponsePXE.setVlanId(25);
    		fetchNetworkSettingsResponsePXE.setCreatedDate(new Date());
    		fetchNetworkSettingsResponsePXE.setCreatedBy("admin");
    		Link linkPXE = new Link();
    		linkPXE.setTitle("PXE");
    		linkPXE.setHref("http://localhost:9080/VirtualServices/Network/ff808081452c813b01452cefa56c00cd");
    		linkPXE.setRel("self");
    		fetchNetworkSettingsResponsePXE.setLink(linkPXE);
    		fetchNetworkSettingsResponsePXE.setStatic(false);
    
    		fetchNetworkSettingsResponseISCSI = new com.dell.pg.asm.identitypool.api.network.model.Network();
    		fetchNetworkSettingsResponseISCSI.setId("ff808081452c813b01452cf1876d00ce");
    		fetchNetworkSettingsResponseISCSI.setName("iSCSI");
    		fetchNetworkSettingsResponseISCSI.setDescription("");
    		fetchNetworkSettingsResponseISCSI.setType(NetworkType.STORAGE_ISCSI_SAN);
    		fetchNetworkSettingsResponseISCSI.setVlanId(16);
    		com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration staticNetworkConfigurationISCSI = new com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration();
    		staticNetworkConfigurationISCSI.setGateway("172.16.0.1");
    		staticNetworkConfigurationISCSI.setSubnet("255.255.0.0");
    		List<com.dell.pg.asm.identitypool.api.network.model.IpRange> ipRangeListISCSI = staticNetworkConfigurationISCSI.getIpRange();
    		ipRangeListISCSI.clear();
    		com.dell.pg.asm.identitypool.api.network.model.IpRange ipRangeISCSI = new com.dell.pg.asm.identitypool.api.network.model.IpRange();
    		ipRangeISCSI.setId("ff808081452c813b01452cf1876d00cf");
    		ipRangeISCSI.setStartingIp("172.16.119.1");
    		ipRangeISCSI.setEndingIp("172.16.119.100");
    		ipRangeListISCSI.add(ipRangeISCSI);
    		fetchNetworkSettingsResponseISCSI.setStaticNetworkConfiguration(staticNetworkConfigurationISCSI);
    		fetchNetworkSettingsResponseISCSI.setCreatedDate(new Date());
    		fetchNetworkSettingsResponseISCSI.setCreatedBy("admin");
    		Link linkISCSI = new Link();
    		linkISCSI.setTitle("iSCSI");
    		linkISCSI.setHref("http://localhost:9080/VirtualServices/Network/ff808081452c813b01452cf1876d00ce");
    		linkISCSI.setRel("self");
    		fetchNetworkSettingsResponseISCSI.setLink(linkISCSI);
    		fetchNetworkSettingsResponseISCSI.setStatic(true);
    
    		fetchNetworkSettingsResponseHypervisorManagement = new com.dell.pg.asm.identitypool.api.network.model.Network();
    		fetchNetworkSettingsResponseHypervisorManagement.setId("ff808081452c813b01452cebf05d0000");
    		fetchNetworkSettingsResponseHypervisorManagement.setName("Hypervisor Management");
    		fetchNetworkSettingsResponseHypervisorManagement.setDescription("");
    		fetchNetworkSettingsResponseHypervisorManagement.setType(NetworkType.HYPERVISOR_MANAGEMENT);
    		fetchNetworkSettingsResponseHypervisorManagement.setVlanId(28);
    		com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration staticNetworkConfigurationHypervisorManagement = new com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration();
    		staticNetworkConfigurationHypervisorManagement.setGateway("172.28.0.1");
    		staticNetworkConfigurationHypervisorManagement.setSubnet("255.255.0.0");
    		List<com.dell.pg.asm.identitypool.api.network.model.IpRange> ipRangeListHypervisorManagement = staticNetworkConfigurationHypervisorManagement.getIpRange();
    		ipRangeListHypervisorManagement.clear();
    		com.dell.pg.asm.identitypool.api.network.model.IpRange ipRangeHypervisorManagement = new com.dell.pg.asm.identitypool.api.network.model.IpRange();
    		ipRangeHypervisorManagement.setId("ff808081452c813b01452cebf05e0001");
    		ipRangeHypervisorManagement.setStartingIp("172.28.119.1");
    		ipRangeHypervisorManagement.setEndingIp("172.28.119.100");
    		ipRangeListHypervisorManagement.add(ipRangeHypervisorManagement);
    		fetchNetworkSettingsResponseHypervisorManagement.setStaticNetworkConfiguration(staticNetworkConfigurationHypervisorManagement);
    		fetchNetworkSettingsResponseHypervisorManagement.setCreatedDate(new Date());
    		fetchNetworkSettingsResponseHypervisorManagement.setCreatedBy("admin");
    		Link linkHypervisorManagement = new Link();
    		linkHypervisorManagement.setTitle("Hypervisor Management");
    		linkHypervisorManagement.setHref("http://localhost:9080/VirtualServices/Network/ff808081452c813b01452cebf05d0000");
    		linkHypervisorManagement.setRel("self");
    		fetchNetworkSettingsResponseHypervisorManagement.setLink(linkHypervisorManagement);
    		fetchNetworkSettingsResponseHypervisorManagement.setStatic(true);
    
    		fetchNetworkSettingsResponseWorkload = new com.dell.pg.asm.identitypool.api.network.model.Network();
    		fetchNetworkSettingsResponseWorkload.setId("ff808081452c813b01452ceecdce00cc");
    		fetchNetworkSettingsResponseWorkload.setName("Workload");
    		fetchNetworkSettingsResponseWorkload.setDescription("");
    		fetchNetworkSettingsResponseWorkload.setType(NetworkType.PRIVATE_LAN);
    		fetchNetworkSettingsResponseWorkload.setVlanId(20);
    		fetchNetworkSettingsResponseWorkload.setCreatedDate(new Date());
    		fetchNetworkSettingsResponseWorkload.setCreatedBy("admin");
    		Link linkWorkload = new Link();
    		linkWorkload.setTitle("Workload");
    		linkWorkload.setHref("http://localhost:9080/VirtualServices/Network/ff808081452c813b01452ceecdce00cc");
    		linkWorkload.setRel("self");
    		fetchNetworkSettingsResponseWorkload.setLink(linkWorkload);
    		fetchNetworkSettingsResponseWorkload.setStatic(false);
    
    		fetchNetworkSettingsResponseVMotion = new com.dell.pg.asm.identitypool.api.network.model.Network();
    		fetchNetworkSettingsResponseVMotion.setId("ff808081452c813b01452cee4a3f0066");
    		fetchNetworkSettingsResponseVMotion.setName("vMotion");
    		fetchNetworkSettingsResponseVMotion.setDescription("");
    		fetchNetworkSettingsResponseVMotion.setType(NetworkType.HYPERVISOR_MANAGEMENT);
    		fetchNetworkSettingsResponseVMotion.setVlanId(23);
    		com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration staticNetworkConfigurationVMotion = new com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration();
    		staticNetworkConfigurationVMotion.setGateway("172.23.0.1");
    		staticNetworkConfigurationVMotion.setSubnet("255.255.0.0");
    		List<com.dell.pg.asm.identitypool.api.network.model.IpRange> ipRangeListVMotion = staticNetworkConfigurationVMotion.getIpRange();
    		ipRangeListVMotion.clear();
    		com.dell.pg.asm.identitypool.api.network.model.IpRange ipRangeVMotion = new com.dell.pg.asm.identitypool.api.network.model.IpRange();
    		ipRangeVMotion.setId("ff808081452c813b01452cee4a460067");
    		ipRangeVMotion.setStartingIp("172.23.119.1");
    		ipRangeVMotion.setEndingIp("172.23.119.100");
    		ipRangeListVMotion.add(ipRangeVMotion);
    		fetchNetworkSettingsResponseVMotion.setStaticNetworkConfiguration(staticNetworkConfigurationVMotion);
    		fetchNetworkSettingsResponseVMotion.setCreatedDate(new Date());
    		fetchNetworkSettingsResponseVMotion.setCreatedBy("admin");
    		Link linkVMotion = new Link();
    		linkVMotion.setTitle("vMotion");
    		linkVMotion.setHref("http://localhost:9080/VirtualServices/Network/ff808081452c813b01452cee4a3f0066");
    		linkVMotion.setRel("self");
    		fetchNetworkSettingsResponseVMotion.setLink(linkVMotion);
    		fetchNetworkSettingsResponseVMotion.setStatic(true);
    		
    		Set<String> s = new HashSet<String>();
    		
    		Set<String> reserveNetworkIpsResponse1 = new HashSet<String>();
    		reserveNetworkIpsResponse1.add("172.23.119.2");
    
    		Set<String> reserveNetworkIpsResponse2 = new HashSet<String>();
    		reserveNetworkIpsResponse2.add("172.23.119.5");
    		reserveNetworkIpsResponse2.add("172.23.119.4");
    
    		Set<String> reserveNetworkIpsResponse4 = new HashSet<String>();
    		reserveNetworkIpsResponse4.add("172.23.119.9");
    		reserveNetworkIpsResponse4.add("172.23.119.8");
    		reserveNetworkIpsResponse4.add("172.23.119.7");
    		reserveNetworkIpsResponse4.add("172.23.119.6");
    
    		Set<String> reserveNetworkIpsResponse1a = new HashSet<String>();
    		reserveNetworkIpsResponse1a.add("172.23.119.10");
    
    		URL resourceRuby = this.getClass().getClassLoader().getResource("DeploymentExampleRubyValues.json");
    		String textRuby = IOUtils.toString(resourceRuby, Charsets.UTF_8);
    		deploymentRuby = MarshalUtil.fromJSON(Deployment.class, textRuby);
    
    		//DeploymentService deploymentService = mock(DeploymentService.class);
    		//DeploymentService deploymentService = new DeploymentService();
    
    		networkProxy = mock(INetworkService.class);
    		IIPAddressPoolMgr ipAddressPoolMgr = mock(IIPAddressPoolMgr.class);
    
    		when(networkProxy.getNetwork("ff808081452c813b01452cefa56c00cd")).thenReturn(fetchNetworkSettingsResponsePXE);
    		when(networkProxy.getNetwork("ff808081452c813b01452cf1876d00ce")).thenReturn(fetchNetworkSettingsResponseISCSI);
    		when(networkProxy.getNetwork("ff808081452c813b01452cebf05d0000")).thenReturn(fetchNetworkSettingsResponseHypervisorManagement);
    		when(networkProxy.getNetwork("ff808081452c813b01452ceecdce00cc")).thenReturn(fetchNetworkSettingsResponseWorkload);
    		when(networkProxy.getNetwork("ff808081452c813b01452cee4a3f0066")).thenReturn(fetchNetworkSettingsResponseVMotion);
    
    		String usageGuid = NetworkingUtil.buildUsageGuid(deploymentRuby.getId());
    		when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cf1876d00ce", usageGuid, 4)).thenReturn(reserveNetworkIpsResponse4);
    		when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cebf05d0000", usageGuid,1)).thenReturn(reserveNetworkIpsResponse1);
    		when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cee4a3f0066", usageGuid,1)).thenReturn(reserveNetworkIpsResponse1a);
    		when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cee4a3f0066", usageGuid,2)).thenReturn(reserveNetworkIpsResponse2);
    
    
            PingUtil pingUtil = mock(PingUtil.class);
            LocalizableMessageService logService = mock(LocalizableMessageService.class);
            DnsUtil dnsUtil = mock(DnsUtil.class);
            networkingUtil = new NetworkingUtil(pingUtil, logService, dnsUtil);
            networkingUtil.massageNetworks(deploymentRuby.getServiceTemplate().getComponents(), networkProxy);
    
            setUpVI();
        }
    	
        private void setUpVI() throws Exception, IOException {
            URL resourceRuby = this.getClass().getClassLoader().getResource("deploymentVI.json");
            String textRuby = IOUtils.toString(resourceRuby, Charsets.UTF_8);
            Deployment deployment = MarshalUtil.fromJSON(Deployment.class, textRuby);

            upgradeNetworks (deployment.getServiceTemplate());
            ioIdentityMgr = mock(IIOIdentityMgr.class);
    
            //String[] mac1 = new String[]{"id:01-23-45-67-89-ab"};
            List<IOIdentity> m1 = new ArrayList<IOIdentity>();
            IOIdentity ioIdentity = new IOIdentity();
            ioIdentity.setId("id:01-23-45-67-89-ab");
            ioIdentity.setValue("01-23-45-67-89-ab");
            m1.add(ioIdentity);
    
            //String[] iqn1 = new String[]{"id:eui.0123456789ABCDEF"};
            List<IOIdentity> i1 = new ArrayList<IOIdentity>();
            ioIdentity = new IOIdentity();
            ioIdentity.setId("id:eui.0123456789ABCDEF");
            ioIdentity.setValue("eui.0123456789ABCDEF");
            i1.add(ioIdentity);
    
            //String[] mac2 = new String[]{"id:03-23-45-67-89-ab","id:04-23-45-67-89-ab"};
            List<IOIdentity> m2 = new ArrayList<IOIdentity>();
            ioIdentity = new IOIdentity();
            ioIdentity.setId("id:03-23-45-67-89-ab");
            ioIdentity.setValue("03-23-45-67-89-ab");
            m2.add(ioIdentity);
            ioIdentity = new IOIdentity();
            ioIdentity.setId("id:04-23-45-67-89-ab");
            ioIdentity.setValue("04-23-45-67-89-ab");
            m2.add(ioIdentity);
    
    		String usageGuid = NetworkingUtil.buildUsageGuid(deployment.getId());
    		when(ioIdentityMgr.reserveIdentities(com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.MAC, usageGuid, 1, 1, 1, "-1")).thenReturn(m1);
            when(ioIdentityMgr.reserveIdentities(com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.IQN, usageGuid, 1, 1, 1, "-1")).thenReturn(i1);
            when(ioIdentityMgr.reserveIdentities(com.dell.pg.asm.identitypoolmgr.ioidentity.entity.IOIdentityType.MAC, usageGuid, 2, 1, 1, "-1")).thenReturn(m2);
    
            when(networkProxy.getNetwork("ff80808145b4230c0145b42d0022002f")).thenReturn(fetchNetworkSettingsResponsePXE);
            when(networkProxy.getNetwork("ff80808145b4230c0145b42cfcc30018")).thenReturn(fetchNetworkSettingsResponseISCSI);
            when(networkProxy.getNetwork("ff80808145b4230c0145b42cf42e0000")).thenReturn(fetchNetworkSettingsResponseHypervisorManagement);
            when(networkProxy.getNetwork("ff80808145b4230c0145b42d032d0030")).thenReturn(fetchNetworkSettingsResponseWorkload);
            when(networkProxy.getNetwork("ff80808145b4230c0145b42cfb770017")).thenReturn(fetchNetworkSettingsResponseVMotion);
    
            IIPAddressPoolMgr ipAddressPoolMgr = mock(IIPAddressPoolMgr.class);
    
            when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cefa56c00cd", usageGuid, 1)).thenReturn(new HashSet<String>(Arrays.asList("172.1.119.1")));
            when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cf1876d00ce", usageGuid, 1))
            				.thenReturn(new HashSet<String>(Arrays.asList("172.2.119.1")))
            				.thenReturn(new HashSet<String>(Arrays.asList("172.2.119.2")));
            
            when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cebf05d0000", usageGuid, 1)).thenReturn(new HashSet<String>(Arrays.asList("172.3.119.1")));
            when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452ceecdce00cc", usageGuid, 1)).thenReturn(new HashSet<String>(Arrays.asList("172.4.119.1")));
            when(ipAddressPoolMgr.assignIPAddresses("ff808081452c813b01452cee4a3f0066", usageGuid, 1)).thenReturn(new HashSet<String>(Arrays.asList("172.5.119.1")));
    
            PingUtil pingUtil = mock(PingUtil.class);
            LocalizableMessageService logService = mock(LocalizableMessageService.class);
            DnsUtil dnsUtil = mock(DnsUtil.class);
            networkingUtil = new NetworkingUtil(pingUtil, logService, dnsUtil);
            when(pingUtil.isReachable(anyString())).thenReturn(false);

			networkingUtil.massageNetworks(deployment.getServiceTemplate().getComponents(), networkProxy);
			List<Network> reservedNetworks = new ArrayList<>();
            networkingUtil.massageVirtualIdentities(deployment, ioIdentityMgr, networkProxy, ipAddressPoolMgr, reservedNetworks, false);
			networkingUtil.reserveVirtualIdentitiesForServers(deployment.getId(),deployment.getServiceTemplate().getComponents(),reservedNetworks,ioIdentityMgr);
            netConfig = deployment.getServiceTemplate().getComponents().get(0).getResources().get(2).getParameters().get(7).getNetworkConfiguration();
            //ServiceTemplateUtil.upgradeNetworkConfiguration(netConfig);
        }
    
        @Test
        public void viHypervisorPXETest() throws IOException {
            Partition partition = netConfig.getInterfaces().get(0).getInterfaces().get(0).getPartitions().get(0);
    		assertEquals("01-23-45-67-89-ab", partition.getLanMacAddress());
    		assertEquals("172.3.119.1", partition.getNetworkObjects().get(0).getStaticNetworkConfiguration().getIpAddress());
        }

        @Test
        public void viWorkloadTest() throws IOException {
            Partition partition = netConfig.getInterfaces().get(0).getInterfaces().get(0).getPartitions().get(2);
    		assertEquals("01-23-45-67-89-ab", partition.getLanMacAddress());
        }
    
        @Test
        public void viISCSITest() throws IOException {
        	 Partition partition1 = netConfig.getInterfaces().get(0).getInterfaces().get(0).getPartitions().get(3);
        	 assertEquals(1, partition1.getNetworkObjects().size());
        	 assertEquals(partition1.getIscsiMacAddress(),"03-23-45-67-89-ab");
        	 assertEquals(partition1.getLanMacAddress(),"04-23-45-67-89-ab");
        	 assertEquals(partition1.getIscsiIQN(),"eui.0123456789ABCDEF");
        	 assertEquals(partition1.getNetworkObjects().get(0).getStaticNetworkConfiguration().getIpAddress(),"172.2.119.1");
        	 Partition partition2 = netConfig.getInterfaces().get(0).getInterfaces().get(1).getPartitions().get(3);
        	 assertEquals(1, partition2.getNetworkObjects().size());
        	 assertEquals(partition2.getIscsiMacAddress(),"03-23-45-67-89-ab");
        	 assertEquals(partition2.getLanMacAddress(), "04-23-45-67-89-ab");
        	 assertEquals(partition2.getIscsiIQN(), "eui.0123456789ABCDEF");
        	 assertEquals("172.2.119.2", partition2.getNetworkObjects().get(0).getStaticNetworkConfiguration().getIpAddress());
        }
    
        @Test
        public void redundancyISCSITest() throws IOException {
            Partition partition = netConfig.getInterfaces().get(0).getInterfaces().get(1).getPartitions().get(3);
            assertEquals(partition.getIscsiMacAddress(), "03-23-45-67-89-ab");
            assertEquals(partition.getLanMacAddress(), "04-23-45-67-89-ab");
            assertEquals(partition.getIscsiIQN(),"eui.0123456789ABCDEF");
            assertEquals("172.2.119.2", partition.getNetworkObjects().get(0).getStaticNetworkConfiguration().getIpAddress());
         }
    
        @Test
        public void redundancyISCSIQuadTest() throws IOException {
            URL resourceRuby = this.getClass().getClassLoader().getResource("netconfquad.json");
            String textRuby = IOUtils.toString(resourceRuby, Charsets.UTF_8);
            NetworkConfiguration net_conf = MarshalUtil.fromJSON(NetworkConfiguration.class, textRuby);
            ServiceTemplateUtil.upgradeNetworkConfiguration(net_conf);

            net_conf.getInterfaces().get(0).getInterfaces().get(0).getPartitions().get(0).setNetworks(Arrays.asList("PixieNetwork"));
            net_conf.getInterfaces().get(0).getInterfaces().get(2).getPartitions().get(0).setNetworks(Arrays.asList("FiberChannelzNetwork"));
            NetworkingUtil.redundancyCheck(net_conf.getInterfaces().get(0));
            assertEquals(net_conf.getInterfaces().get(0).getInterfaces().get(1).getPartitions().get(0).getNetworks(), Arrays.asList("PixieNetwork"));
            assertEquals(net_conf.getInterfaces().get(0).getInterfaces().get(3).getPartitions().get(0).getNetworks(), Arrays.asList("FiberChannelzNetwork"));
        }
    
        @Test
        public void testJsonRoundTrip() throws IOException {
            String json = DeploymentService.toJson(deploymentRuby);
            assertNotNull(json);
            System.out.println(json);
    
            Deployment deployment = DeploymentService.fromJson(json);
            assertNotNull(deployment);
        }
    
        @Test
        public void testNetworkConfiguration() throws IOException {
            URL resource = this.getClass().getClassLoader().getResource("deploymentRack.json");
            String text = IOUtils.toString(resource, Charsets.UTF_8);
            NetworkConfiguration net_conf = MarshalUtil.fromJSON(NetworkConfiguration.class, text);
            ServiceTemplateUtil.upgradeNetworkConfiguration(net_conf);
            assertEquals(net_conf.getInterfaces().get(0).getInterfaces().get(0).getPartitions().get(0).getNetworks().size(), 2);
        }
    }
    

	public static class MigrateDeploymentComponentsTests {
		Deployment deployment;
		DeploymentService service;
		DeploymentEntity entity;
		JobDataMap jobDataMap;
		ManagedDevice matchingServer;
        ServiceTemplateService serviceTemplateService;
        User userMock;

		@Before
		public void setUp() throws IOException {
			// Build test DeploymentService
			INetworkService networkService = mock(INetworkService.class);
			EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);
			ServiceTemplateUtil serviceTemplateUtil = ServiceTemplateUtilTest.mockServiceTemplateUtil(encryptionDAO, networkService);

			DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
			DeviceInventoryDAO deviceInventoryDAO = mock(DeviceInventoryDAO.class);
			DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
			LocalizableMessageService logService = mock(LocalizableMessageService.class);
			IPAddressPoolMgr ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
            ServerFilteringUtil filteringUtil = mock(ServerFilteringUtil.class);
            MigrationDeviceUtils migrationUtils = mock(MigrationDeviceUtils.class);
			serviceTemplateService = mock(ServiceTemplateService.class);

			GenericDAO genericDAO = mock(GenericDAO.class);
			AddOnModuleComponentsDAO addOnModuleDAO = mock(AddOnModuleComponentsDAO.class);
			IOIdentityMgr ioMgr = mock(IOIdentityMgr.class);
			ServiceTemplateValidator validator = mock(ServiceTemplateValidator.class);
			ServiceTemplateDAO serviceTemplateDAO = mock(ServiceTemplateDAO.class);
			FirmwareRepositoryDAO firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
			AsmManagerUtil asmManagerUtil = mock(AsmManagerUtil.class);
			DeploymentValidator deploymentValidator = mock(DeploymentValidator.class);
            userMock = mock(User.class);
            when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
            when(userMock.getUserName()).thenReturn("admin");
            when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(userMock);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);
	        FirmwareUtil firmwareUtil = mock(FirmwareUtil.class);

			DeploymentNamesRefDAO deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);

	        FirmwareRepository firmwareRepoMock = mock(FirmwareRepository.class);
	        when(firmwareUtil.entityToDto(any(FirmwareRepositoryEntity.class), any(Boolean.class))).thenReturn(firmwareRepoMock);
			
	        ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);

			service = new DeploymentService(deploymentDAO,
					deviceInventoryDAO,
					deviceInventoryComplianceDAO,
					serviceTemplateUtil,
					logService,
					ipAddressPoolMgr,
					ioMgr,
					networkService,
					serviceTemplateService,
					genericDAO,
					addOnModuleDAO,
					validator,
					firmwareUtil,
					serviceTemplateDAO,
					firmwareRepositoryDAO,
					asmManagerUtil,
					serviceDeploymentUtil,
					deploymentValidator,
					deploymentNamesRefDAO);

			IUserResource adminProxy = mock(IUserResource.class);
            User adminUser = new User();
            adminUser.setUserName("AdminUserName");
            adminUser.setEnabled(true);
            adminUser.setUserSeqId(7);
            adminUser.setRole(AsmConstants.USERROLE_ADMINISTRATOR);
            when(adminProxy.getUser(1)).thenReturn(adminUser);
            when(adminProxy.getUser(2)).thenReturn(adminUser);

            service.setMigrationDeviceUtils(migrationUtils);
			service.setAdminProxy(adminProxy);
			IAsmDeployerService asmDeployerProxy = mock(IAsmDeployerService.class);
			service.setAsmDeployerProxy(asmDeployerProxy);
			DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
			service.setDeviceGroupDAO(deviceGroupDAO);
			service.setFilteringUtil(filteringUtil);

			IJobManager jobManager = mock(IJobManager.class);
			JobDetail mockJob = mock(JobDetail.class);
			jobDataMap = new JobDataMap();
			when(mockJob.getJobDataMap()).thenReturn(jobDataMap);
			when(jobManager.createNamedJob(ServiceDeploymentJob.class)).thenReturn(mockJob);
			Scheduler mockScheduler = mock(Scheduler.class);
			when(jobManager.getScheduler()).thenReturn(mockScheduler);
			service.setJobManager(jobManager);

			// Build mock deployment data. Referenced cluster deployment contains two servers
			deployment = loadXmlResource(Deployment.class, "DeploymentServiceTest/cluster-deployment.xml");
			ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(deployment.getServiceTemplate());

			upgradeNetworks(deployment.getServiceTemplate());

			// Load inventory data for servers in the deployment
			ManagedDevice blade1 = loadXmlResource(ManagedDevice.class, "DeploymentServiceTest/bladeserver-gp181y1.xml");
			ManagedDevice blade2 = loadXmlResource(ManagedDevice.class, "DeploymentServiceTest/bladeserver-hv7qqv1.xml");

			// Load inventory data for another server which will be migrated to
			matchingServer = loadXmlResource(ManagedDevice.class, "DeploymentServiceTest/bladeserver-6d4qqv1.xml");

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			entity = new DeploymentEntity();
			entity.setId(deployment.getId());
			entity.setName(deployment.getDeploymentName());
			entity.setStatus(deployment.getStatus());
			entity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			entity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(blade1, false));
			entity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(blade2, false));
			when(deploymentDAO.getDeployment(eq(entity.getId()), any(Integer.class))).thenReturn(entity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getComponentId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(entity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(entity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                doAnswer(new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        return makeEnvironment(invocation);
                    }
                }).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			// Mock server matching filtering, so matchingServer can be used in migration
			final String matchingServerId = "ff80808150199d19015019f166670194";
			doAnswer(new Answer() {
				public Object answer(InvocationOnMock invocation) {
					Object[] args = invocation.getArguments();
					DeploymentFilterResponse response = (DeploymentFilterResponse)args[2];
					List<SelectedServer> selectedServers = new ArrayList<>();
					SelectedServer selectedServer = new SelectedServer(matchingServerId,
							"6D4QQV1", "", null, new RAIDConfiguration());
					selectedServers.add(selectedServer);
					response.setSelectedServers(selectedServers);
					return null;
				}
			}).when(filteringUtil).getAvailableServerFromPool(any(String.class), any(DeploymentEnvironment.class),
					any(DeploymentFilterResponse.class), any(FilterEnvironment.class),
					anyInt());

			when(deviceInventoryDAO.getDeviceInventory(any(String.class)))
					.thenReturn(DeviceInventoryUtils.toEntity(matchingServer, true));

			when(deploymentDAO.updateDeploymentStatusToInProgress(entity.getId())).thenReturn(entity);

            Map<String, DeviceInventoryEntity> filteredServer = new HashMap<String, DeviceInventoryEntity>();
            filteredServer.put(MigrationDeviceUtils.MigrateMatch.EXACT.name(), DeviceInventoryUtils.toEntity(matchingServer, true));
            when(migrationUtils.migrateFilterServer(any(DeviceInventoryEntity.class), any(String.class), any(List.class))).thenReturn(filteredServer);

            setupSecurityContext(service);
		}

        @Test
		public void RequiresExistingDeploymentId() throws IOException {
			try {
				service.migrateDeployment("bad-id", null, null);
				fail("migrateDeploymentComponents succeeded with non-existent id");
			} catch (LocalizedWebApplicationException e) {
				// This should be a more specific error, but just testing existing code for now
				assertEquals(AsmManagerMessages.internalError().getMessageCode(),
						e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
			}
		}

		@Test
		public void RequiresServerInDeployment() throws IOException {
			try {
				service.migrateDeployment(entity.getId(), "bad-server-id", "42");
				fail("migrateDeploymentComponents succeeded with non-existent server");
			} catch (LocalizedWebApplicationException e) {
				// This should be a more specific error, but just testing existing code for now
				assertEquals(AsmManagerMessages.internalError().getMessageCode(),
						e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
			}
		}

        @Test
        public void testMigrateServer() throws IOException {
            // Add first deployed device to migrate list
            String componentId = "024764E2-CEE1-4A0A-84EF-6D4398C70899";
            String oldServerId = deployment.getServiceTemplate().findComponentById(componentId).getAsmGUID();
            Deployment deployment = service.migrateServerComponent(entity.getId(), componentId);
            assertNotNull("migrateServer returned null",deployment);
            String newServerId = deployment.getServiceTemplate().findComponentById(componentId).getAsmGUID();
            assertNotSame("Old server and new server", oldServerId, newServerId);
            assertEquals("Expected migration target", matchingServer.getRefId(), newServerId);
        }

        @Test
		public void ShouldAddBaseServerResource() throws IOException {
			// Add first deployed device to migrate list
			DeviceInventoryEntity serverToMigrate = entity.getDeployedDevices().iterator().next();
            ServiceTemplateComponent componentToMigrate = findComponentByRefId(deployment, serverToMigrate.getRefId());

			service.migrateDeployment(entity.getId(), componentToMigrate.getId(), "42");

			// Find component to migrate
			assertNotNull("Could not find component being migrated", componentToMigrate);
			String hostnameTemplate = componentToMigrate.getParameterValue(
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID);
			assertNotNull("Could not find hostname template", hostnameTemplate);
			String origHostname = componentToMigrate.getParameterValue(
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
			assertNotNull("Could not find hostname", origHostname);

			String json = (String)jobDataMap.get(ServiceDeploymentJob.ServiceDeploymentJob_SERVICE_KEY_DATA);
			assertNotNull(json);
			Deployment scheduled = DeploymentService.fromJson(json);
			assertEquals("Scheduled migration job has different number of template components",
					deployment.getServiceTemplate().getComponents().size(),
					scheduled.getServiceTemplate().getComponents().size());

			ServiceTemplateComponent migratedComponent = findComponentById(scheduled, componentToMigrate.getId());
			assertNotNull("Could not find migrated component in scheduled data", migratedComponent);

			String serviceTag = migratedComponent.getParameterValue(
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_BASE_SERVER_ID, "serialnumber");
			assertEquals("Incorrect service tag on asm::baseserver", serverToMigrate.getServiceTag(), serviceTag);

			String certName = migratedComponent.getParameterValue(
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_BASE_SERVER_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
			assertEquals("Incorrect service tag on asm::baseserver",
					PuppetModuleUtil.toCertificateName(serverToMigrate), certName);

			String hostname = migratedComponent.getParameterValue(
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_BASE_SERVER_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
			assertEquals("Incorrect hostname on asm::baseserver", origHostname, hostname);

			String expected = new HostnameUtil().generateHostname(hostnameTemplate, componentToMigrate,
					DeviceInventoryUtils.toEntity(matchingServer, false), new HashSet<String>());
			String migratedHostname = migratedComponent.getParameterValue(
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
			assertEquals("Generated hostname did not match original hostname template", expected, migratedHostname);
        }

        @Test
        public void testMigrateWithNumPattern() throws IOException {
            // test with num pattern - generated value must not change
            DeviceInventoryEntity serverToMigrate = entity.getDeployedDevices().iterator().next();
            ServiceTemplateComponent componentToMigrate = findComponentByRefId(deployment, serverToMigrate.getRefId());

            deployment.getServiceTemplate().getTemplateSetting(componentToMigrate.getId(),ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID).setValue("server_${num}");
            entity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));

            String originalHostname = componentToMigrate.getParameterValue(
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);

            service.migrateDeployment(entity.getId(), componentToMigrate.getId(), "42");
            String migratedHostname = componentToMigrate.getParameterValue(
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);

            assertEquals("Generated hostname did not match original hostname template", originalHostname, migratedHostname);

        }

		@Test
		public void testEmptyHostname() throws Exception {
			String componentId = "024764E2-CEE1-4A0A-84EF-6D4398C70899";
			Deployment deployment = service.migrateServerComponent(entity.getId(), componentId);

			ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
			ServiceTemplateSetting hostNameParam = null;
			for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
				if (component.getType() != ServiceTemplateComponent.ServiceTemplateComponentType.SERVER &&
						component.getType() != ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE)
					continue;
				for (ServiceTemplateCategory resource : component.getResources()) {
					for (ServiceTemplateSetting param : resource.getParameters()) {
						if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID.equals(param.getId())) {
							param.setValue(null);
							hostNameParam = param;
						}
					}
				}
			}

			try {
				service.updateDeployment(entity.getId(), deployment);
				fail("Update deployment passed with hostname = null");
			} catch (LocalizedWebApplicationException wax) {
				AsmDetailedMessageList list = RestUtil.unwrapExceptionToAsmDetailedMessageList(wax);
				assertTrue(list.getMessages().get(0).getDisplayMessage().contains("host name"));
			}
			hostNameParam.setValue("");
			try {
				service.updateDeployment(entity.getId(), deployment);
				fail("Update deployment passed with empty hostname");
			} catch (LocalizedWebApplicationException wax) {
				AsmDetailedMessageList list = RestUtil.unwrapExceptionToAsmDetailedMessageList(wax);
				assertTrue(list.getMessages().get(0).getDisplayMessage().contains("host name"));
			}
		}
    }


        public static class DeploymentService4xServers2xStorageDeploymentTest {
	    
	    DeploymentService deploymentService;
	    Deployment deployment;
	    IAsmDeployerService asmDeployerProxy;
	    DeviceInventoryDAO deviceInventoryDAO;
	    DeploymentDAO deploymentDAO;
	    ServerFilteringUtil filteringUtil;
	    JobDataMap jobDataMap;
	    ManagedDevice matchingServer;
	    FirmwareUtil firmwareUtil;
	    User userMock;
		ServiceTemplateDAO serviceTemplateDAO;

	    private static enum SetupSetting{
	        SERVER1_COMPLIANT,
	        SERVER2_COMPLIANT,
	        SERVER3_COMPLIANT,
	        SERVER4_COMPLIANT,
	        SERVER1_NON_COMPLIANT,
	        SERVER2_NON_COMPLIANT,
	        SERVER3_NON_COMPLIANT,
	        SERVER4_NON_COMPLIANT,
	        SERVER1_HEALTH_GREEN,
	        SERVER2_HEALTH_GREEN,
	        SERVER3_HEALTH_GREEN,
	        SERVER4_HEALTH_GREEN,
	        SERVER1_HEALTH_YELLOW,
	        SERVER2_HEALTH_YELLOW,
	        SERVER3_HEALTH_YELLOW,
	        SERVER4_HEALTH_YELLOW,
	        SERVER1_HEALTH_RED,
	        SERVER2_HEALTH_RED,
	        SERVER3_HEALTH_RED,
	        SERVER4_HEALTH_RED,
	        STORAGE_ARRAY1_COMPLIANT,
	        STORAGE_ARRAY1_NON_COMPLIANT;  // STORAGE ARRAY DOES NOT CURRENTLY HAVE A GREEN, YELLOW, RED STATUS
	    }

	    @Before
	    public void setUp() throws IOException {
	        // Build test DeploymentService
			INetworkService networkService = mock(INetworkService.class);
	        EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);
	        ServiceTemplateUtil serviceTemplateUtil = ServiceTemplateUtilTest.mockServiceTemplateUtil(encryptionDAO, networkService);

	        deploymentDAO = mock(DeploymentDAO.class);
	        deviceInventoryDAO = mock(DeviceInventoryDAO.class);
	        DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
	        LocalizableMessageService logService = mock(LocalizableMessageService.class);
	        IPAddressPoolMgr ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
	        ServiceTemplateService serviceTemplateService = mock(ServiceTemplateService.class);
            MigrationDeviceUtils migrationUtils = mock(MigrationDeviceUtils.class);
            GenericDAO genericDAO = mock(GenericDAO.class);
			AddOnModuleComponentsDAO addOnModuleDAO = mock(AddOnModuleComponentsDAO.class);
			FirmwareRepositoryDAO firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
	        IOIdentityMgr ioMgr = mock(IOIdentityMgr.class);

	        ServiceTemplateValidator validator = mock(ServiceTemplateValidator.class);
	        firmwareUtil = mock(FirmwareUtil.class);
			serviceTemplateDAO = mock(ServiceTemplateDAO.class);
	        AsmManagerUtil asmManagerUtil = mock(AsmManagerUtil.class);
			DeploymentValidator deploymentValidator = mock(DeploymentValidator.class);
            userMock = mock(User.class);
            when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
            when(userMock.getUserName()).thenReturn("admin");
	        when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(userMock);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentNamesRefDAO deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);

			ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);
			deploymentService = new DeploymentService(deploymentDAO,
	                deviceInventoryDAO,
	                deviceInventoryComplianceDAO,
	                serviceTemplateUtil,
	                logService,
	                ipAddressPoolMgr,
	                ioMgr,
	                networkService,
	                serviceTemplateService,
	                genericDAO,
					addOnModuleDAO,
					validator,
					firmwareUtil,
					serviceTemplateDAO,
					firmwareRepositoryDAO,
					asmManagerUtil,
					serviceDeploymentUtil,
					deploymentValidator,
					deploymentNamesRefDAO);

	        IUserResource adminProxy = mock(IUserResource.class);
	        when(adminProxy.getUser(any(Long.class))).thenReturn(userMock);
	        deploymentService.setMigrationDeviceUtils(migrationUtils);
	        deploymentService.setAdminProxy(adminProxy);
	        asmDeployerProxy = mock(IAsmDeployerService.class);
	        deploymentService.setAsmDeployerProxy(asmDeployerProxy);
	        DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
	        deploymentService.setDeviceGroupDAO(deviceGroupDAO);
	        filteringUtil = mock(ServerFilteringUtil.class);
	        deploymentService.setFilteringUtil(filteringUtil);

	        IJobManager jobManager = mock(IJobManager.class);
	        JobDetail mockJob = mock(JobDetail.class);
	        jobDataMap = new JobDataMap();
	        when(mockJob.getJobDataMap()).thenReturn(jobDataMap);
	        when(jobManager.createNamedJob(ServiceDeploymentJob.class)).thenReturn(mockJob);
	        Scheduler mockScheduler = mock(Scheduler.class);
	        when(jobManager.getScheduler()).thenReturn(mockScheduler);
	        deploymentService.setJobManager(jobManager);
	    }


	    @Test
	    public void test4xServer2xArrayDeploymentHealthStatusForGreen() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
	        deployment.setCompliant(true);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of  4x Servers and 2x Arrays deployment that is all green and all compliant should be GREEN!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.GREEN));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithOneServerYellow() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_YELLOW);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
            // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(true);
	        
	        this.setupDeployment(setupSettings);

	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has one server Yellow and the rest Green should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithOneServerNonCompliant() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
            // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(false);
            
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has one server non compliant and the rest Green/compliant should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithTwoServersNonCompliant() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(false);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has two servers non compliant and the rest Green/compliant should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithThreeServersNonCompliant() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(false);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has three servers non compliant and the rest Green/compliant should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithAllServersAndStorageNonCompliant() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_NON_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_NON_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(false);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has all devices non compliant and the rest Green Health should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithOneServerRed() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(true);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has one server Red and the rest Green/compliant should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithTwoServersRed() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(true);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that has two servers Red and the rest Green/compliant should be a DeploymentHealthStatus of YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    public void test4xServer2xArrayDeploymentHealthWithTwoServersRedAndOneServerYellow() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_YELLOW);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(true);
            
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment that only has 1 Server GREEN and the other servers Yellow/Red/compliant should be a DeploymentHealthStatus of RED!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.RED));
	    }
	    
	    @Test
	    public void test4xServer2xArrayDeploymentHealthWithThreeServersRed() throws Exception {
	        ArrayList<SetupSetting> setupSettings = new ArrayList<SetupSetting>();
	        setupSettings.add(SetupSetting.SERVER1_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER2_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER3_HEALTH_RED);
	        setupSettings.add(SetupSetting.SERVER4_HEALTH_GREEN);
	        setupSettings.add(SetupSetting.SERVER1_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER2_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER3_COMPLIANT);
	        setupSettings.add(SetupSetting.SERVER4_COMPLIANT);
	        setupSettings.add(SetupSetting.STORAGE_ARRAY1_COMPLIANT);
	        
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
            deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_4xServersAnd2xArrays.xml");
            deployment.setCompliant(true);
	        
	        this.setupDeployment(setupSettings);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501aabb701501c0066ba0431");
	        assertNotNull("deployment of 4x Servers and 2x Arrays Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of  4x Servers and 2x Arrays Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of 4x Servers and 2x Arrays deployment only has 1 server GREEN and the rest Yellow or Red should be a DeploymentHealthStatus of RED!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.RED));
	    }
	    
	    
	 
	    
	    
	    
	    private void setupDeployment(ArrayList<SetupSetting> setupSettings) throws Exception{
	        
	        // Load inventory data for servers in the deployment
	        ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
	        ManagedDevice bladeServer1 = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/bladeserver-4L4V5Y1.xml");
	        ManagedDevice bladeServer2 = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/bladeserver-CK4V5Y1.xml");
	        ManagedDevice bladeServer3 = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/bladeserver-2L4V5Y1.xml");
	        ManagedDevice bladeServer4 = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/bladeserver-9W9G942.xml");

	        DeviceInventoryEntity storageArrayEntity = DeviceInventoryUtils.toEntity(storageArray, true);
	        DeviceInventoryEntity bladeServer1Entity = DeviceInventoryUtils.toEntity(bladeServer1, true);
	        DeviceInventoryEntity bladeServer2Entity = DeviceInventoryUtils.toEntity(bladeServer2, true);
	        DeviceInventoryEntity bladeServer3Entity = DeviceInventoryUtils.toEntity(bladeServer3, true);
	        DeviceInventoryEntity bladeServer4Entity = DeviceInventoryUtils.toEntity(bladeServer4, true);
	        
	        if(setupSettings.contains(SetupSetting.SERVER1_HEALTH_RED)) bladeServer1Entity.setHealth(DeviceHealth.RED);
	        else if(setupSettings.contains(SetupSetting.SERVER1_HEALTH_YELLOW)) bladeServer1Entity.setHealth(DeviceHealth.YELLOW);
	        else if(setupSettings.contains(SetupSetting.SERVER1_HEALTH_GREEN)) bladeServer1Entity.setHealth(DeviceHealth.GREEN);
	        
	        if(setupSettings.contains(SetupSetting.SERVER1_COMPLIANT)) {
	        	bladeServer1.setCompliance(CompliantState.COMPLIANT);
	        	bladeServer1Entity.setCompliant(CompliantState.COMPLIANT.name());
	        }
	        else if(setupSettings.contains(SetupSetting.SERVER1_NON_COMPLIANT)) {
	        	bladeServer1.setCompliance(CompliantState.NONCOMPLIANT);
	        	bladeServer1Entity.setCompliant(CompliantState.NONCOMPLIANT.name());
	        }
	            
	        if(setupSettings.contains(SetupSetting.SERVER2_HEALTH_RED)) bladeServer2Entity.setHealth(DeviceHealth.RED);
	        else if(setupSettings.contains(SetupSetting.SERVER2_HEALTH_YELLOW)) bladeServer2Entity.setHealth(DeviceHealth.YELLOW);
	        else if(setupSettings.contains(SetupSetting.SERVER2_HEALTH_GREEN)) bladeServer2Entity.setHealth(DeviceHealth.GREEN);
	        
	        if(setupSettings.contains(SetupSetting.SERVER2_COMPLIANT)) {
	        	bladeServer2.setCompliance(CompliantState.COMPLIANT);
	        	bladeServer2Entity.setCompliant(CompliantState.COMPLIANT.name());
	        }
	        else if(setupSettings.contains(SetupSetting.SERVER2_NON_COMPLIANT)) {
	        	bladeServer2.setCompliance(CompliantState.NONCOMPLIANT);
	        	bladeServer2Entity.setCompliant(CompliantState.NONCOMPLIANT.name());
	        }
	        
	        if(setupSettings.contains(SetupSetting.SERVER3_HEALTH_RED)) bladeServer3Entity.setHealth(DeviceHealth.RED);
	        else if(setupSettings.contains(SetupSetting.SERVER3_HEALTH_YELLOW)) bladeServer3Entity.setHealth(DeviceHealth.YELLOW);
	        else if(setupSettings.contains(SetupSetting.SERVER3_HEALTH_GREEN)) bladeServer3Entity.setHealth(DeviceHealth.GREEN);
	        
	        if(setupSettings.contains(SetupSetting.SERVER3_COMPLIANT)){
	        	bladeServer3.setCompliance(CompliantState.COMPLIANT);
	        	bladeServer3Entity.setCompliant(CompliantState.COMPLIANT.name());
	        }
	        else if(setupSettings.contains(SetupSetting.SERVER3_NON_COMPLIANT)) {
	        	bladeServer3.setCompliance(CompliantState.NONCOMPLIANT);
	        	bladeServer3Entity.setCompliant(CompliantState.NONCOMPLIANT.name());
	        }
	  
	        if(setupSettings.contains(SetupSetting.SERVER4_HEALTH_RED)) bladeServer4Entity.setHealth(DeviceHealth.RED);
	        else if(setupSettings.contains(SetupSetting.SERVER4_HEALTH_YELLOW)) bladeServer4Entity.setHealth(DeviceHealth.YELLOW);
	        else if(setupSettings.contains(SetupSetting.SERVER4_HEALTH_GREEN)) bladeServer4Entity.setHealth(DeviceHealth.GREEN);
	        
	        if(setupSettings.contains(SetupSetting.SERVER4_COMPLIANT)) {
	        	bladeServer4.setCompliance(CompliantState.COMPLIANT);
	        	bladeServer4Entity.setCompliant(CompliantState.COMPLIANT.name());
	        }
	        else if(setupSettings.contains(SetupSetting.SERVER4_NON_COMPLIANT)) {
	        	bladeServer4.setCompliance(CompliantState.NONCOMPLIANT);
	        	bladeServer3Entity.setCompliant(CompliantState.NONCOMPLIANT.name());
	        }
	  
	        storageArrayEntity.setHealth(DeviceHealth.GREEN);
	        
	        if(setupSettings.contains(SetupSetting.STORAGE_ARRAY1_COMPLIANT)){
	        	storageArray.setCompliance(CompliantState.COMPLIANT);
	        	storageArrayEntity.setCompliant(CompliantState.COMPLIANT.name());
	        }
	        else if(setupSettings.contains(SetupSetting.STORAGE_ARRAY1_NON_COMPLIANT)){
	        	storageArray.setCompliance(CompliantState.NONCOMPLIANT);
	        	storageArrayEntity.setCompliant(CompliantState.NONCOMPLIANT.name());
	        }
	  
	        
	        // Build matching DeploymentEntity, to be returned by deploymentDAO
	        DeploymentEntity deploymentEntity = new DeploymentEntity();
	        deploymentEntity.setManageFirmware(true);
	        deploymentEntity.setId(deployment.getId());
	        deploymentEntity.setName(deployment.getDeploymentName());
	        deploymentEntity.setStatus(deployment.getStatus());
	        deploymentEntity.setCompliant(deployment.isCompliant());
	        deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
	        FirmwareRepositoryEntity fre = new FirmwareRepositoryEntity();
	        fre.setId("frid");
	        deploymentEntity.setFirmwareRepositoryEntity(fre);
	        deploymentEntity.getDeployedDevices().add(storageArrayEntity);
	        deploymentEntity.getDeployedDevices().add(bladeServer1Entity);
	        deploymentEntity.getDeployedDevices().add(bladeServer2Entity);
	        deploymentEntity.getDeployedDevices().add(bladeServer3Entity);
	        deploymentEntity.getDeployedDevices().add(bladeServer4Entity);
	        when(deploymentDAO.getDeployment(eq(deployment.getId()),any(Integer.class))).thenReturn(deploymentEntity);
//	        when(deploymentDAO.getDeployment(deployment.getId())).thenReturn(deploymentEntity);

	        when(this.deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);
	        when(this.deviceInventoryDAO.getDeviceInventory(bladeServer1Entity.getRefId())).thenReturn(bladeServer1Entity);
	        when(this.deviceInventoryDAO.getDeviceInventory(bladeServer2Entity.getRefId())).thenReturn(bladeServer2Entity);
	        when(this.deviceInventoryDAO.getDeviceInventory(bladeServer3Entity.getRefId())).thenReturn(bladeServer3Entity);
	        when(this.deviceInventoryDAO.getDeviceInventory(bladeServer4Entity.getRefId())).thenReturn(bladeServer4Entity);
	        
	        // Mock out asm-deployer status info for deployment
	        List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
	        for (DeploymentDevice device : deployment.getDeploymentDevice()) {
	            AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
	            cs.setId(device.getComponentId());
	            cs.setAsmGuid(device.getRefId());
	            cs.setStatus(device.getStatus());
	            cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
	            cs.setMessage(device.getStatusMessage());
	            componentStatuses.add(cs);
	        }

	        AsmDeployerStatus status = new AsmDeployerStatus();
	        status.setId(deploymentEntity.getId());
	        status.setStatus(DeploymentStatusType.COMPLETE);
	        status.setComponents(componentStatuses);

	        when(asmDeployerProxy.getDeploymentStatus(deploymentEntity.getId())).thenReturn(status);
	        
	        // Mock ServerFilteringUtil methods
	        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                doAnswer(new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        return makeEnvironment(invocation);
                    }
                }).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
	        }
	    }
	}
	
	
	public static class DeploymentServiceSingleArrayDeploymentTest {
	    
	    DeploymentService deploymentService;
	    Deployment deployment;
	    IAsmDeployerService asmDeployerProxy;
	    DeviceInventoryDAO deviceInventoryDAO;
	    DeploymentDAO deploymentDAO;
	    ServerFilteringUtil filteringUtil;
	    JobDataMap jobDataMap;
	    FirmwareUtil firmwareUtil;
	    User userMock;
		ServiceTemplateDAO serviceTemplateDAO;
		FirmwareRepositoryDAO firmwareRepositoryDAO;
		DeviceInventoryComplianceDAO deviceInventoryComplianceDAO;

	    @Before
	    public void setUp() throws IOException {
	        // Build test DeploymentService
			INetworkService networkService = mock(INetworkService.class);
	        EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);
	        ServiceTemplateUtil serviceTemplateUtil = ServiceTemplateUtilTest.mockServiceTemplateUtil(encryptionDAO, networkService);

	        deploymentDAO = mock(DeploymentDAO.class);
	        deviceInventoryDAO = mock(DeviceInventoryDAO.class);
	        deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
	        LocalizableMessageService logService = mock(LocalizableMessageService.class);
	        IPAddressPoolMgr ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
            MigrationDeviceUtils migrationUtils = mock(MigrationDeviceUtils.class);

            ServiceTemplateService serviceTemplateService = mock(ServiceTemplateService.class);
	        GenericDAO genericDAO = mock(GenericDAO.class);
			AddOnModuleComponentsDAO addOnModuleDAO = mock(AddOnModuleComponentsDAO.class);
	        IOIdentityMgr ioMgr = mock(IOIdentityMgr.class);

	        ServiceTemplateValidator validator = mock(ServiceTemplateValidator.class);
//	        FirmwareUpdateUtil firmwareUpdateUtil = mock(FirmwareUpdateUtil.class);
	        firmwareUtil = mock(FirmwareUtil.class);
	        
	        FirmwareRepository firmwareRepoMock = mock(FirmwareRepository.class);
	        when(firmwareUtil.entityToDto(any(FirmwareRepositoryEntity.class), any(Boolean.class))).thenReturn(firmwareRepoMock);
			serviceTemplateDAO = mock(ServiceTemplateDAO.class);
			firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
			AsmManagerUtil asmManagerUtil = mock(AsmManagerUtil.class);
			DeploymentValidator deploymentValidator = mock(DeploymentValidator.class);
            userMock = mock(User.class);
            when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
            when(userMock.getUserName()).thenReturn("admin");
            when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(userMock);
			ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);

			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentNamesRefDAO deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);

			deploymentService = new DeploymentService(deploymentDAO,
	                deviceInventoryDAO,
	                deviceInventoryComplianceDAO,
	                serviceTemplateUtil,
	                logService,
	                ipAddressPoolMgr,
	                ioMgr,
	                networkService,
	                serviceTemplateService,
	                genericDAO,
					addOnModuleDAO,
					validator,
					firmwareUtil,
					serviceTemplateDAO,
					firmwareRepositoryDAO,
					asmManagerUtil,
					serviceDeploymentUtil,
					deploymentValidator,
					deploymentNamesRefDAO);

	        IUserResource adminProxy = mock(IUserResource.class);
	        userMock = mock(User.class);
	        when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
	        when(userMock.getUserName()).thenReturn("admin");
	        when(adminProxy.getUser(any(Long.class))).thenReturn(userMock);
            deploymentService.setMigrationDeviceUtils(migrationUtils);
	        deploymentService.setAdminProxy(adminProxy);
	        asmDeployerProxy = mock(IAsmDeployerService.class);
	        deploymentService.setAsmDeployerProxy(asmDeployerProxy);
	        DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
	        deploymentService.setDeviceGroupDAO(deviceGroupDAO);
	        filteringUtil = mock(ServerFilteringUtil.class);
	        deploymentService.setFilteringUtil(filteringUtil);

	        IJobManager jobManager = mock(IJobManager.class);
	        JobDetail mockJob = mock(JobDetail.class);
	        jobDataMap = new JobDataMap();
	        when(mockJob.getJobDataMap()).thenReturn(jobDataMap);
	        when(jobManager.createNamedJob(ServiceDeploymentJob.class)).thenReturn(mockJob);
	        Scheduler mockScheduler = mock(Scheduler.class);
	        when(jobManager.getScheduler()).thenReturn(mockScheduler);
	        deploymentService.setJobManager(jobManager);
	    }


	    @Test
	    public void testSingleArrayDeploymentHealthStatusForGreen() throws Exception {
	        this.setupDeployment(true);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501a7c4e01501a8622000082");
	        
	        assertNotNull("deployment of single array Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of single array Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of single array deployment that is compliant should be GREEN, but is " + deployment.getDeploymentHealthStatusType(), deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.GREEN));
	    }
	    
	    @Test
	    public void testSingleArrayDeploymentHealthStatusForYellow() throws Exception {
	        this.setupDeployment(false);
	
	        Deployment deployment = deploymentService.getDeployment("ff808081501a7c4e01501a8622000082");
	        assertNotNull("deployment of single array Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of single array Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of single array deployment that is non-compliant should be YELLOW!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    private void setupDeployment(boolean compliant) throws Exception{
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
	        deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_singleArray.xml");
	        deployment.setCompliant(true);
            deployment.setFirmwareRepositoryId("id");

	        // Load inventory data for servers in the deployment
	        ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
	        DeviceInventoryEntity storageArrayEntity = DeviceInventoryUtils.toEntity(storageArray, true);
	        DeviceInventoryComplianceEntity dice = new DeviceInventoryComplianceEntity(); // mock(DeviceInventoryComplianceEntity.class); 
	        if(!compliant) {
	        	storageArray.setCompliance(CompliantState.NONCOMPLIANT);
	        	storageArrayEntity.setCompliant(CompliantState.NONCOMPLIANT.name());
	        	dice.setCompliance(CompliantState.NONCOMPLIANT);
	        	when(this.deviceInventoryComplianceDAO.findDeviceInventoryCompliance(any(String.class), any(String.class))).thenReturn(dice);
	        }
	        else {
	        	storageArray.setCompliance(CompliantState.COMPLIANT);
	        	storageArrayEntity.setCompliant(CompliantState.COMPLIANT.name());
	        	dice.setCompliance(CompliantState.COMPLIANT);
	            when(this.deviceInventoryComplianceDAO.findDeviceInventoryCompliance(any(String.class), any(String.class))).thenReturn(dice);
	        }
	        
	        // Build matching DeploymentEntity, to be returned by deploymentDAO
	        DeploymentEntity singleArrayDeploymentEntity = new DeploymentEntity();
	        singleArrayDeploymentEntity.setManageFirmware(true);
	        singleArrayDeploymentEntity.setId(deployment.getId());
	        singleArrayDeploymentEntity.setName(deployment.getDeploymentName());
	        singleArrayDeploymentEntity.setCompliant(deployment.isCompliant());
	        singleArrayDeploymentEntity.setStatus(deployment.getStatus());
	        singleArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
	        singleArrayDeploymentEntity.getDeployedDevices().add(storageArrayEntity);
	        FirmwareRepositoryEntity fre = new FirmwareRepositoryEntity();
	        fre.setId("frid");
	        singleArrayDeploymentEntity.setFirmwareRepositoryEntity(fre);
	        when(deploymentDAO.getDeployment(eq(singleArrayDeploymentEntity.getId()), any(Integer.class))).thenReturn(singleArrayDeploymentEntity);
//	        when(deploymentDAO.getDeployment(singleArrayDeploymentEntity.getId())).thenReturn(singleArrayDeploymentEntity);
	        when(this.deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);


	        // Mock out asm-deployer status info for deployment
	        List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
	        for (DeploymentDevice device : deployment.getDeploymentDevice()) {
	            AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
	            cs.setId(device.getComponentId());
	            cs.setAsmGuid(device.getRefId());
	            cs.setStatus(device.getStatus());
	            cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
	            cs.setMessage(device.getStatusMessage());
	            componentStatuses.add(cs);
	        }

	        AsmDeployerStatus status = new AsmDeployerStatus();
	        status.setId(singleArrayDeploymentEntity.getId());
	        status.setStatus(DeploymentStatusType.COMPLETE);
	        status.setComponents(componentStatuses);

	        when(asmDeployerProxy.getDeploymentStatus(singleArrayDeploymentEntity.getId())).thenReturn(status);

	        // Mock ServerFilteringUtil methods
	        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                doAnswer(new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        return makeEnvironment(invocation);
                    }
                }).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
	        }
	    }
	}
	
	public static class DeploymentServiceTwoArrayDeploymentTest {
	    
	    DeploymentService deploymentService;
	    Deployment deployment;
	    IAsmDeployerService asmDeployerProxy;
	    DeviceInventoryDAO deviceInventoryDAO;
	    DeploymentDAO deploymentDAO;
	    ServerFilteringUtil filteringUtil;
	    JobDataMap jobDataMap;
	    ManagedDevice matchingServer;
	    FirmwareUtil firmwareUtil;
		ServiceTemplateDAO serviceTemplateDAO;
		FirmwareRepositoryDAO firmwareRepositoryDAO;
		User userMock;

	    @Before
	    public void setUp() throws IOException {
	        // Build test DeploymentService
	        EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);
			INetworkService networkService = mock(INetworkService.class);
	        ServiceTemplateUtil serviceTemplateUtil = ServiceTemplateUtilTest.mockServiceTemplateUtil(encryptionDAO, networkService);

	        deploymentDAO = mock(DeploymentDAO.class);
	        deviceInventoryDAO = mock(DeviceInventoryDAO.class);
	        DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
	        LocalizableMessageService logService = mock(LocalizableMessageService.class);
	        IPAddressPoolMgr ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
            MigrationDeviceUtils migrationUtils = mock(MigrationDeviceUtils.class);
	        ServiceTemplateService serviceTemplateService = mock(ServiceTemplateService.class);
	        GenericDAO genericDAO = mock(GenericDAO.class);
			AddOnModuleComponentsDAO addOnModuleDAO = mock(AddOnModuleComponentsDAO.class);
	        IOIdentityMgr ioMgr = mock(IOIdentityMgr.class);
	        ServiceTemplateValidator validator = mock(ServiceTemplateValidator.class);
	        firmwareUtil = mock(FirmwareUtil.class);
	        FirmwareRepository firmwareRepoMock = mock(FirmwareRepository.class);
	        when(firmwareUtil.entityToDto(any(FirmwareRepositoryEntity.class), any(Boolean.class))).thenReturn(firmwareRepoMock);
			serviceTemplateDAO = mock(ServiceTemplateDAO.class);
			firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
			AsmManagerUtil asmManagerUtil = mock(AsmManagerUtil.class);
			DeploymentValidator deploymentValidator = mock(DeploymentValidator.class);
            userMock = mock(User.class);
            when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
            when(userMock.getUserName()).thenReturn("admin");
            when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(userMock);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentNamesRefDAO deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);

            ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);
            
	        deploymentService = new DeploymentService(deploymentDAO,
	                deviceInventoryDAO,
	                deviceInventoryComplianceDAO,
	                serviceTemplateUtil,
	                logService,
	                ipAddressPoolMgr,
	                ioMgr,
	                networkService,
	                serviceTemplateService,
	                genericDAO,addOnModuleDAO,
	                validator,
	                firmwareUtil,
					serviceTemplateDAO,
					firmwareRepositoryDAO,
					asmManagerUtil,
					serviceDeploymentUtil,
					deploymentValidator,
					deploymentNamesRefDAO);

	        IUserResource adminProxy = mock(IUserResource.class);
	        User userMock = mock(User.class);
	        when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
	        when(userMock.getUserName()).thenReturn("admin");
	        when(adminProxy.getUser(any(Long.class))).thenReturn(userMock);
	        
            deploymentService.setMigrationDeviceUtils(migrationUtils);
	        deploymentService.setAdminProxy(adminProxy);
	        asmDeployerProxy = mock(IAsmDeployerService.class);
	        deploymentService.setAsmDeployerProxy(asmDeployerProxy);
	        DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
	        deploymentService.setDeviceGroupDAO(deviceGroupDAO);
	        filteringUtil = mock(ServerFilteringUtil.class);
	        deploymentService.setFilteringUtil(filteringUtil);

	        IJobManager jobManager = mock(IJobManager.class);
	        JobDetail mockJob = mock(JobDetail.class);
	        jobDataMap = new JobDataMap();
	        when(mockJob.getJobDataMap()).thenReturn(jobDataMap);
	        when(jobManager.createNamedJob(ServiceDeploymentJob.class)).thenReturn(mockJob);
	        Scheduler mockScheduler = mock(Scheduler.class);
	        when(jobManager.getScheduler()).thenReturn(mockScheduler);
	        deploymentService.setJobManager(jobManager);
	    }


	    @Test
	    public void testTwoArrayDeploymentHealthStatusForGreen() throws Exception {
	        this.setupDeployment(true);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501a7c4e01501a890ac30088");
	        assertNotNull("deployment of two array Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of two array Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of two array deployment that is compliant should be GREEN!", deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.GREEN));
	    }
	    
	    @Test
	    public void testTwoArrayDeploymentHealthStatusForYellow() throws Exception {
	        this.setupDeployment(false);
	        
	        Deployment deployment = deploymentService.getDeployment("ff808081501a7c4e01501a890ac30088");
	        assertNotNull("deployment of two array Service cannot be null!", deployment);
	        
	        assertNotNull("deployment's DeploymentHealthStatusType of two array Service cannot be null!", deployment.getDeploymentHealthStatusType());
	        assertTrue("deployment DeploymentHealthStatusType of two array deployment that is non-compliant should be Yellow but is " + deployment.getDeploymentHealthStatusType().name(), deployment.getDeploymentHealthStatusType().equals(DeploymentHealthStatusType.YELLOW));
	    }
	    
	    private void setupDeployment(boolean healthGreen) throws Exception{
	        // Build mock deployemnt data. Referenced cluster deployment contains two servers
	        deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
	        deployment.setCompliant(true);
            deployment.setFirmwareRepositoryId("id");
	        
	        // Load inventory data for servers in the deployment
	        ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
	        storageArray.setCompliance(CompliantState.COMPLIANT);
//          	when(firmwareUtil.getCompliantState(any(ManagedDevice.class), any(DeviceInventoryEntity.class), any(User.class))).thenReturn(CompliantState.COMPLIANT);
//          	when(firmwareUtil.getCompliantState(any(ManagedDevice.class), any(DeviceInventoryEntity.class), any(String.class), any(User.class))).thenReturn(CompliantState.COMPLIANT); 
	        
	        if(!healthGreen) {
	        	storageArray.setHealth(DeviceHealth.YELLOW);
	        }
	        else {
	        	storageArray.setHealth(DeviceHealth.GREEN);
	        }
	        
	        // Build matching DeploymentEntity, to be returned by deploymentDAO
	        DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
	        twoArrayDeploymentEntity.setId(deployment.getId());
	        twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
	        twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
	        twoArrayDeploymentEntity.setStatus(deployment.getStatus());
	        twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
	        twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
            twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
	        when(deploymentDAO.getDeployment(eq(twoArrayDeploymentEntity.getId()), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);
//	        when(deploymentDAO.updateDeploymentStatusToInProgress(deploymentId)).thenReturn(twoArrayDeploymentEntity);
	        
	        // Mock out asm-deployer status info for deployment
	        List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
	        for (DeploymentDevice device : deployment.getDeploymentDevice()) {
	            AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
	            cs.setId(device.getComponentId());
	            cs.setAsmGuid(device.getRefId());
	            cs.setStatus(device.getStatus());
	            cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
	            cs.setMessage(device.getStatusMessage());
	            componentStatuses.add(cs);
	        }

	        AsmDeployerStatus status = new AsmDeployerStatus();
	        status.setId(twoArrayDeploymentEntity.getId());
	        status.setStatus(DeploymentStatusType.COMPLETE);
	        status.setComponents(componentStatuses);

	        when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

	        // Mock ServerFilteringUtil methods
	        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                doAnswer(new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        return makeEnvironment(invocation);
                    }
                }).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
	        }
	    }
	}
	
    public static class DeploymentServiceSecurityTests {

		DeploymentService deploymentService;
		Deployment deployment;
		IAsmDeployerService asmDeployerProxy;
		DeviceInventoryDAO deviceInventoryDAO;
		DeploymentDAO deploymentDAO;
		ServerFilteringUtil filteringUtil;
		JobDataMap jobDataMap;
		ManagedDevice matchingServer;
		ServiceTemplateUtil serviceTemplateUtil;
		INetworkService networkService;
		User adminUser;
		User operatorUser;
		User readOnlyUser;
		IUserResource adminProxy;
		ServiceTemplateDAO serviceTemplateDAO;
		FirmwareRepositoryDAO firmwareRepositoryDAO;
		User userMock;
		AsmManagerUtil asmManagerUtil;
		DeploymentValidator deploymentValidator;

		@Before
		public void setUp() throws IOException {

			// Build test DeploymentService
			EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);
//            ServiceTemplateUtil serviceTemplateUtil = new ServiceTemplateUtil(encryptionDAO) {
//                @Override
//                public String findTask(String repoName) {
//                    return "junit-task-" + repoName;
//                }
//            };

			serviceTemplateUtil = mock(ServiceTemplateUtil.class);
//            when(serviceTemplateUtil.setHiddenValues(any(ServiceTemplate.class))).doNothing();

			deploymentDAO = mock(DeploymentDAO.class);
			deviceInventoryDAO = mock(DeviceInventoryDAO.class);
			DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
			LocalizableMessageService logService = mock(LocalizableMessageService.class);
			IPAddressPoolMgr ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
			MigrationDeviceUtils migrationUtils = mock(MigrationDeviceUtils.class);
			ServiceTemplateService serviceTemplateService = mock(ServiceTemplateService.class);
			GenericDAO genericDAO = mock(GenericDAO.class);
			AddOnModuleComponentsDAO addOnModuleDAO = mock(AddOnModuleComponentsDAO.class);
			IOIdentityMgr ioMgr = mock(IOIdentityMgr.class);
			networkService = mock(INetworkService.class);
			ServiceTemplateValidator validator = mock(ServiceTemplateValidator.class);

			FirmwareUtil firmwareUtil = mock(FirmwareUtil.class);

			serviceTemplateDAO = mock(ServiceTemplateDAO.class);
			firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
			asmManagerUtil = mock(AsmManagerUtil.class);
			deploymentValidator = mock(DeploymentValidator.class);
			userMock = mock(User.class);
			when(userMock.getRole()).thenReturn(AsmConstants.USERROLE_ADMINISTRATOR);
			when(userMock.getUserName()).thenReturn("admin");
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(userMock);
			ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);
			DeploymentNamesRefDAO deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);

			deploymentService = new DeploymentService(deploymentDAO,
					deviceInventoryDAO,
					deviceInventoryComplianceDAO,
					serviceTemplateUtil,
					logService,
					ipAddressPoolMgr,
					ioMgr,
					networkService,
					serviceTemplateService,
					genericDAO,
					addOnModuleDAO,
					validator,
					firmwareUtil,
					serviceTemplateDAO,
					firmwareRepositoryDAO,
					asmManagerUtil,
					serviceDeploymentUtil,
					deploymentValidator,
					deploymentNamesRefDAO);

			adminProxy = mock(IUserResource.class);
			adminUser = new User();
			adminUser.setUserName("AdminUserName");
			adminUser.setEnabled(true);
			adminUser.setUserSeqId(7);
			adminUser.setRole(AsmConstants.USERROLE_ADMINISTRATOR);

			operatorUser = new User();
			operatorUser.setUserName("OperatorUserName");
			operatorUser.setEnabled(true);
			operatorUser.setUserSeqId(3);
			operatorUser.setRole(AsmConstants.USERROLE_OPERATOR);

			readOnlyUser = new User();
			readOnlyUser.setUserName("ReadOnlyUserName");
			readOnlyUser.setEnabled(true);
			readOnlyUser.setUserSeqId(1);
			readOnlyUser.setRole(AsmConstants.USERROLE_READONLY);

			when(adminProxy.getUser(7)).thenReturn(adminUser);
			when(adminProxy.getUser(3)).thenReturn(operatorUser);
			when(adminProxy.getUser(1)).thenReturn(readOnlyUser);


			deploymentService.setMigrationDeviceUtils(migrationUtils);
			deploymentService.setAdminProxy(adminProxy);
			asmDeployerProxy = mock(IAsmDeployerService.class);
			deploymentService.setAsmDeployerProxy(asmDeployerProxy);
			DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
			deploymentService.setDeviceGroupDAO(deviceGroupDAO);
			filteringUtil = mock(ServerFilteringUtil.class);
			deploymentService.setFilteringUtil(filteringUtil);

			IJobManager jobManager = mock(IJobManager.class);
			JobDetail mockJob = mock(JobDetail.class);
			jobDataMap = new JobDataMap();
			when(mockJob.getJobDataMap()).thenReturn(jobDataMap);
			when(jobManager.createNamedJob(ServiceDeploymentJob.class)).thenReturn(mockJob);
			Scheduler mockScheduler = mock(Scheduler.class);
			when(jobManager.getScheduler()).thenReturn(mockScheduler);
			deploymentService.setJobManager(jobManager);
			setupSecurityContext(deploymentService);
		}

		@Test
		public void testGetDeploymentWithAdminUser() throws Exception {
//            ServiceContext.Context context = mock(ServiceContext.Context.class);
//            when(context.getApiKey()).thenReturn("someAPIKey");
//            when(context.getMergedPrivileges()).thenReturn(7L);
//            when(context.getUserId()).thenReturn(adminUser.getUserSeqId());
//            ServiceContext.bind(context);
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop");
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);

			try {
				this.deploymentService.getDeployment("abcdefghijklmnop");
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.equals(lwe.getResponse().getStatus())) {
					fail("CheckPermissions failed for the AdminUser!");
				}
			}
		}

		@Test
		public void testGetDeploymentWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getCreatedBy()).thenReturn(adminUser.getUserName());  // adminUser so it should fail
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);

			try {
				this.deploymentService.getDeployment("abcdefghijklmnop");
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should have failed for the OperatorUser who is not Authorized!");
				}
			}
		}

		@Test
		public void testGetDeploymentWithReadOnlyUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(readOnlyUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(readOnlyUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop");
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);

			try {
				this.deploymentService.getDeployment("abcdefghijklmnop");
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.equals(lwe.getResponse().getStatus())) {
					fail("CheckPermissions failed for the ReadOnlyUser!");
				}
			}
		}

		@Test
		public void testGetDeploymentsWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop");

			DeploymentEntity deploymentEntity2 = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop2");

			DeploymentEntity deploymentEntity3 = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop3");

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop2"), any(Integer.class))).thenReturn(deploymentEntity2);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop3"), any(Integer.class))).thenReturn(deploymentEntity3);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);
			deploymentEntities.add(deploymentEntity2);
			deploymentEntities.add(deploymentEntity3);

			String sort = null; // Use columen name with -column for descending order
			ArrayList<String> filter = null; // new ArrayList<String>();
			// ?filter=eq,name,foobar where name if the value to filter on and foobar is the name it should equal
			int offset = 0;
			int limit = 50;
			boolean fullTemplates = false;

			when(this.deploymentDAO.getAllDeployments(any(List.class), any(List.class), any(PaginationInfo.class), any(Integer.class))).thenReturn(deploymentEntities);

			Deployment[] deployments = this.deploymentService.getDeployments(sort, filter, offset, limit, fullTemplates);

			assertNotNull("deployments cannot be null!", deployments);
			assertTrue("There should be 3 number of deployments, but there are only " + deployments.length + " deployments!", deployments.length == 3);
		}

		@Test
		public void testGetDeploymentsWithReadOnlyUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(readOnlyUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(readOnlyUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop");

			DeploymentEntity deploymentEntity2 = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop2");

			DeploymentEntity deploymentEntity3 = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop3");

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop2"), any(Integer.class))).thenReturn(deploymentEntity2);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop3"), any(Integer.class))).thenReturn(deploymentEntity3);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);
			deploymentEntities.add(deploymentEntity2);
			deploymentEntities.add(deploymentEntity3);

			String sort = null; // Use columen name with -column for descending order
			ArrayList<String> filter = null; // new ArrayList<String>();
			// ?filter=eq,name,foobar where name if the value to filter on and foobar is the name it should equal
			int offset = 0;
			int limit = 50;
			boolean fullTemplates = false;

			when(this.deploymentDAO.getAllDeployments(any(List.class), any(List.class), any(PaginationInfo.class), any(Integer.class))).thenReturn(deploymentEntities);

			Deployment[] deployments = this.deploymentService.getDeployments(sort, filter, offset, limit, fullTemplates);

			assertNotNull("deployments cannot be null!", deployments);
			assertTrue("There should be 3 number of deployments, but there are only " + deployments.length + " deployments!", deployments.length == 3);
		}


		@Test
		public void testGetDeploymentsWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop");

			DeploymentEntity deploymentEntity2 = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop2");

			DeploymentEntity deploymentEntity3 = mock(DeploymentEntity.class);
			deploymentEntity.setId("abcdefghijklmnop3");

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop2"), any(Integer.class))).thenReturn(deploymentEntity2);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop3"), any(Integer.class))).thenReturn(deploymentEntity3);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);
			deploymentEntities.add(deploymentEntity2);
			deploymentEntities.add(deploymentEntity3);

			String sort = null; // Use columen name with -column for descending order
			ArrayList<String> filter = null; // new ArrayList<String>();
			// ?filter=eq,name,foobar where name if the value to filter on and foobar is the name it should equal
			int offset = 0;
			int limit = 50;
			boolean fullTemplates = false;

			when(this.deploymentDAO.getAllDeployments(any(List.class), any(List.class), any(PaginationInfo.class), any(Integer.class))).thenReturn(deploymentEntities);

			Deployment[] deployments = this.deploymentService.getDeployments(sort, filter, offset, limit, fullTemplates);

			assertNotNull("deployments cannot be null!", deployments);
			assertTrue("There should be 0 number of deployments, but there are " + deployments.length + " deployments!", deployments.length == 0);
		}


		@Test
		public void testGetDeploymentsFromDeviceIdWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			DeploymentEntity deploymentEntity2 = mock(DeploymentEntity.class);
			when(deploymentEntity2.getId()).thenReturn("abcdefghijklmnop2");
			when(deploymentEntity2.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			DeploymentEntity deploymentEntity3 = mock(DeploymentEntity.class);
			when(deploymentEntity3.getId()).thenReturn("abcdefghijklmnop3");
			when(deploymentEntity3.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop2"), any(Integer.class))).thenReturn(deploymentEntity2);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop3"), any(Integer.class))).thenReturn(deploymentEntity3);

			AsmDeployerStatus asmDeployerStatus = mock(AsmDeployerStatus.class);
			when(this.asmDeployerProxy.getDeploymentStatus(any(String.class))).thenReturn(asmDeployerStatus);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);
			deploymentEntities.add(deploymentEntity2);
			deploymentEntities.add(deploymentEntity3);

			String deviceId = "somefancydeviceId123";

			DeviceInventoryEntity deviceInventoryEntity = new DeviceInventoryEntity();
			deviceInventoryEntity.setRefId(deviceId);
			deviceInventoryEntity.setDeployments(deploymentEntities);

			when(this.deviceInventoryDAO.getDeviceInventory(deviceId)).thenReturn(deviceInventoryEntity);

			Deployment[] deployments = this.deploymentService.getDeploymentsFromDeviceId(deviceId);

			assertNotNull("deployments cannot be null!", deployments);
			assertTrue("There should be 3 number of deployments, but there are only " + deployments.length + " deployments!", deployments.length == 3);
		}

		@Test
		public void testGetDeploymentsFromDeviceIdWithReadOnlyUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(readOnlyUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(readOnlyUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			DeploymentEntity deploymentEntity2 = mock(DeploymentEntity.class);
			when(deploymentEntity2.getId()).thenReturn("abcdefghijklmnop2");
			when(deploymentEntity2.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			DeploymentEntity deploymentEntity3 = mock(DeploymentEntity.class);
			when(deploymentEntity3.getId()).thenReturn("abcdefghijklmnop3");
			when(deploymentEntity3.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop2"), any(Integer.class))).thenReturn(deploymentEntity2);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop3"), any(Integer.class))).thenReturn(deploymentEntity3);

			AsmDeployerStatus asmDeployerStatus = mock(AsmDeployerStatus.class);
			when(this.asmDeployerProxy.getDeploymentStatus(any(String.class))).thenReturn(asmDeployerStatus);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);
			deploymentEntities.add(deploymentEntity2);
			deploymentEntities.add(deploymentEntity3);

			String deviceId = "somefancydeviceId123";

			DeviceInventoryEntity deviceInventoryEntity = new DeviceInventoryEntity();
			deviceInventoryEntity.setRefId(deviceId);
			deviceInventoryEntity.setDeployments(deploymentEntities);

			when(this.deviceInventoryDAO.getDeviceInventory(deviceId)).thenReturn(deviceInventoryEntity);

			Deployment[] deployments = this.deploymentService.getDeploymentsFromDeviceId(deviceId);

			assertNotNull("deployments cannot be null!", deployments);
			assertTrue("There should be 3 number of deployments, but there are only " + deployments.length + " deployments!", deployments.length == 3);
		}

		@Test
		public void testGetDeploymentsFromDeviceIdWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			DeploymentEntity deploymentEntity2 = mock(DeploymentEntity.class);
			when(deploymentEntity2.getId()).thenReturn("abcdefghijklmnop2");
			when(deploymentEntity2.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			DeploymentEntity deploymentEntity3 = mock(DeploymentEntity.class);
			when(deploymentEntity3.getId()).thenReturn("abcdefghijklmnop3");
			when(deploymentEntity3.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop2"), any(Integer.class))).thenReturn(deploymentEntity2);
			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop3"), any(Integer.class))).thenReturn(deploymentEntity3);

			AsmDeployerStatus asmDeployerStatus = mock(AsmDeployerStatus.class);
			when(this.asmDeployerProxy.getDeploymentStatus(any(String.class))).thenReturn(asmDeployerStatus);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);
			deploymentEntities.add(deploymentEntity2);
			deploymentEntities.add(deploymentEntity3);

			String deviceId = "somefancydeviceId123";

			DeviceInventoryEntity deviceInventoryEntity = new DeviceInventoryEntity();
			deviceInventoryEntity.setRefId(deviceId);
			deviceInventoryEntity.setDeployments(deploymentEntities);

			when(this.deviceInventoryDAO.getDeviceInventory(deviceId)).thenReturn(deviceInventoryEntity);

			Deployment[] deployments = this.deploymentService.getDeploymentsFromDeviceId(deviceId);

			assertNotNull("deployments cannot be null!", deployments);
			assertTrue("There should be 0 number of deployments, but there are only " + deployments.length + " deployments!", deployments.length == 0);
		}


		@Test
		public void testGetPuppetLogsWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);

			AsmDeployerStatus asmDeployerStatus = mock(AsmDeployerStatus.class);
			when(this.asmDeployerProxy.getDeploymentStatus(any(String.class))).thenReturn(asmDeployerStatus);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);

			String certName = "certNameId-1";
			List<String> filter = null;

			List<PuppetLogEntry> puppetLogsToReturn = new ArrayList<PuppetLogEntry>();
			PuppetLogEntry puppetLogEntry = new PuppetLogEntry();
			puppetLogEntry.setDate(new Date().toString());

			PuppetLogEntry puppetLogEntry2 = new PuppetLogEntry();
			puppetLogEntry2.setDate(new Date().toString());

			PuppetLogEntry puppetLogEntry3 = new PuppetLogEntry();
			puppetLogEntry3.setDate(new Date().toString());

			puppetLogsToReturn.add(puppetLogEntry);
			puppetLogsToReturn.add(puppetLogEntry2);
			puppetLogsToReturn.add(puppetLogEntry3);

			when(this.asmDeployerProxy.getAsmPuppetLogs("abcdefghijklmnop", certName)).thenReturn(puppetLogsToReturn);

			List<PuppetLogEntry> puppetLogs = this.deploymentService.getPuppetLogs("abcdefghijklmnop", certName, filter);

			assertNotNull("puppetLogs cannot be null!", puppetLogs);
			assertTrue("There should be 3 PuppetLogEntries, but there are only " + puppetLogs.size() + " PuppetLogEntries!", puppetLogs.size() == 3);
		}

		@Test
		public void testGetPuppetLogsWithReadOnlyUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(readOnlyUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(readOnlyUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);

			AsmDeployerStatus asmDeployerStatus = mock(AsmDeployerStatus.class);
			when(this.asmDeployerProxy.getDeploymentStatus(any(String.class))).thenReturn(asmDeployerStatus);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);

			String certName = "certNameId-1";
			List<String> filter = null;

			List<PuppetLogEntry> puppetLogsToReturn = new ArrayList<PuppetLogEntry>();
			PuppetLogEntry puppetLogEntry = new PuppetLogEntry();
			puppetLogEntry.setDate(new Date().toString());

			PuppetLogEntry puppetLogEntry2 = new PuppetLogEntry();
			puppetLogEntry2.setDate(new Date().toString());

			PuppetLogEntry puppetLogEntry3 = new PuppetLogEntry();
			puppetLogEntry3.setDate(new Date().toString());

			puppetLogsToReturn.add(puppetLogEntry);
			puppetLogsToReturn.add(puppetLogEntry2);
			puppetLogsToReturn.add(puppetLogEntry3);

			when(this.asmDeployerProxy.getAsmPuppetLogs("abcdefghijklmnop", certName)).thenReturn(puppetLogsToReturn);

			List<PuppetLogEntry> puppetLogs = this.deploymentService.getPuppetLogs("abcdefghijklmnop", certName, filter);

			assertNotNull("puppetLogs cannot be null!", puppetLogs);
			assertTrue("There should be 3 PuppetLogEntries, but there are only " + puppetLogs.size() + " PuppetLogEntries!", puppetLogs.size() == 3);
		}

		@Test
		public void testGetPuppetLogsWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
			when(deploymentEntity.getId()).thenReturn("abcdefghijklmnop");
			when(deploymentEntity.getStatus()).thenReturn(DeploymentStatusType.COMPLETE);

			when(this.deploymentDAO.getDeployment(eq("abcdefghijklmnop"), any(Integer.class))).thenReturn(deploymentEntity);

			AsmDeployerStatus asmDeployerStatus = mock(AsmDeployerStatus.class);
			when(this.asmDeployerProxy.getDeploymentStatus(any(String.class))).thenReturn(asmDeployerStatus);

			ArrayList<DeploymentEntity> deploymentEntities = new ArrayList<DeploymentEntity>();
			deploymentEntities.add(deploymentEntity);

			String certName = "certNameId-1";
			List<String> filter = null;

			List<PuppetLogEntry> puppetLogsToReturn = new ArrayList<PuppetLogEntry>();
			PuppetLogEntry puppetLogEntry = new PuppetLogEntry();
			puppetLogEntry.setDate(new Date().toString());

			PuppetLogEntry puppetLogEntry2 = new PuppetLogEntry();
			puppetLogEntry2.setDate(new Date().toString());

			PuppetLogEntry puppetLogEntry3 = new PuppetLogEntry();
			puppetLogEntry3.setDate(new Date().toString());

			puppetLogsToReturn.add(puppetLogEntry);
			puppetLogsToReturn.add(puppetLogEntry2);
			puppetLogsToReturn.add(puppetLogEntry3);

			when(this.asmDeployerProxy.getAsmPuppetLogs("abcdefghijklmnop", certName)).thenReturn(puppetLogsToReturn);

			try {
				List<PuppetLogEntry> puppetLogs = this.deploymentService.getPuppetLogs("abcdefghijklmnop", certName, filter);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should have failed for the Operator User who is not Authorized to getPuppetLogs for that deployment!");
				}
			}
		}

		@Test
		public void testUpdateDeploymentWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);
			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			DeviceInventoryEntity storageArrayEntity = new DeviceInventoryEntity();
			storageArrayEntity.setChassisId(storageArray.getChassisId());
			storageArrayEntity.setDeviceType(storageArray.getDeviceType());
			storageArrayEntity.setRefId(storageArray.getRefId());
			storageArrayEntity.setRefType(storageArray.getRefType());

			when(deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);
			when(deploymentDAO.updateDeploymentStatusToInProgress(deploymentId)).thenReturn(twoArrayDeploymentEntity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getRefId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(twoArrayDeploymentEntity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation) {
						return makeEnvironment(invocation);
					}
				}).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			Deployment updatedDeployment = this.deploymentService.updateDeployment(deploymentId, deployment);

			assertNotNull("updatedDeployment cannot be null!", updatedDeployment);
		}

		@Test
		public void testUpdateDeploymentWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			DeviceInventoryEntity storageArrayEntity = new DeviceInventoryEntity();
			storageArrayEntity.setChassisId(storageArray.getChassisId());
			storageArrayEntity.setDeviceType(storageArray.getDeviceType());
			storageArrayEntity.setRefId(storageArray.getRefId());
			storageArrayEntity.setRefType(storageArray.getRefType());

			when(deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);
			when(deploymentDAO.updateDeploymentStatusToInProgress(deploymentId)).thenReturn(twoArrayDeploymentEntity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getRefId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(twoArrayDeploymentEntity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation) {
						return makeEnvironment(invocation);
					}
				}).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			try {
				Deployment updatedDeployment = this.deploymentService.updateDeployment(deploymentId, deployment);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should have failed for the OperatorUser who is not Authorized to updateDeployment!");
				}
			}
		}


		@Test
		public void testDeleteDeploymentWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);
			doNothing().when(deploymentDAO).deleteDeployment(deploymentId);

			this.deploymentService.deleteDeployment(deploymentId);
		}

		@Test
		public void testDeleteDeploymentWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);
			doNothing().when(deploymentDAO).deleteDeployment(deploymentId);

			try {
				this.deploymentService.deleteDeployment(deploymentId);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should have failed for the Operator User who is not Authorized to delete a deployment!");
				}
			}
		}

		@Test
		public void testDeleteDeploymentWithReadOnlyUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(readOnlyUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(readOnlyUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);
			doNothing().when(deploymentDAO).deleteDeployment(deploymentId);

			try {
				this.deploymentService.deleteDeployment(deploymentId);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should have failed for the ReadOnly User who is not Authorized to delete a deployment!");
				}
			}
		}

		@Test
		public void testMigrateServerWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			DeviceInventoryEntity storageArrayEntity = new DeviceInventoryEntity();
			storageArrayEntity.setChassisId(storageArray.getChassisId());
			storageArrayEntity.setDeviceType(storageArray.getDeviceType());
			storageArrayEntity.setRefId(storageArray.getRefId());
			storageArrayEntity.setRefType(storageArray.getRefType());

			when(deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getRefId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(twoArrayDeploymentEntity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation) {
						return makeEnvironment(invocation);
					}
				}).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			String serverComponentId = "someComponentId-1";
			String serverPoolId = "somePoolId-1";

			try {
				Deployment migratedDeployment = this.deploymentService.migrateDeployment(deploymentId, serverComponentId, serverPoolId);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() == lwe.getResponse().getStatus()) {
					fail("CheckPermissions should not have failed for an Admin User when migrating a Deployment!");
				}
				// Else Ignore as we only process to the point of security access
			}
		}


		@Test
		public void testMigrateServerWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			DeviceInventoryEntity storageArrayEntity = new DeviceInventoryEntity();
			storageArrayEntity.setChassisId(storageArray.getChassisId());
			storageArrayEntity.setDeviceType(storageArray.getDeviceType());
			storageArrayEntity.setRefId(storageArray.getRefId());
			storageArrayEntity.setRefType(storageArray.getRefType());

			when(deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getRefId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(twoArrayDeploymentEntity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation) {
						return makeEnvironment(invocation);
					}
				}).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			String serverComponentId = "someComponentId-1";
			String serverPoolId = "somePoolId-1";

			try {
				Deployment migratedDeployment = this.deploymentService.migrateDeployment(deploymentId, serverComponentId, serverPoolId);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should fail for an Operator User when migrating a Deployment!");
				}
				// Else Ignore as we only process to the point of security access
			}
		}


		@Test
		public void testMigrateServerComponentWithAdminUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(adminUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(adminUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			DeviceInventoryEntity storageArrayEntity = new DeviceInventoryEntity();
			storageArrayEntity.setChassisId(storageArray.getChassisId());
			storageArrayEntity.setDeviceType(storageArray.getDeviceType());
			storageArrayEntity.setRefId(storageArray.getRefId());
			storageArrayEntity.setRefType(storageArray.getRefType());

			when(deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getRefId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(twoArrayDeploymentEntity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation) {
						return makeEnvironment(invocation);
					}
				}).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			String serverComponentId = "someComponentId-1";

			try {
				Deployment migratedDeployment = this.deploymentService.migrateServerComponent(deploymentId, serverComponentId);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() == lwe.getResponse().getStatus()) {
					fail("CheckPermissions should not have failed for an Admin User when migrating a a Server Component!");
				}
				// Else Ignore as we only process to the point of security access
			}
		}


		@Test
		public void testMigrateServerComponentWithOperatorUser() throws Exception {
			when(this.adminProxy.getUser(any(Long.class))).thenReturn(operatorUser);
			when(asmManagerUtil.getCurrentUser(any(HttpServletRequest.class))).thenReturn(operatorUser);
			DeploymentValid deploymentValid = DeploymentValid.getDefaultInstance();
			when(deploymentValidator.validateDeployment(any(Deployment.class), anyBoolean())).thenReturn(deploymentValid);

			String deploymentId = "ff808081501a7c4e01501a890ac30088";

			// Build mock deployment data. Referenced cluster deployment contains two servers
			Deployment deployment = DeploymentServiceTest.loadXmlResource(Deployment.class, "DeploymentHealthStatusTest/deployment_2xArrays.xml");
			deployment.setCompliant(true);
			deployment.setFirmwareRepositoryId("id");

			// Load inventory data for servers in the deployment
			ManagedDevice storageArray = DeploymentServiceTest.loadXmlResource(ManagedDevice.class, "DeploymentHealthStatusTest/equallogic-AS800-Env04.xml");
			storageArray.setCompliance(CompliantState.COMPLIANT);

			DeviceInventoryEntity storageArrayEntity = new DeviceInventoryEntity();
			storageArrayEntity.setChassisId(storageArray.getChassisId());
			storageArrayEntity.setDeviceType(storageArray.getDeviceType());
			storageArrayEntity.setRefId(storageArray.getRefId());
			storageArrayEntity.setRefType(storageArray.getRefType());

			when(deviceInventoryDAO.getDeviceInventory(storageArray.getRefId())).thenReturn(storageArrayEntity);

			// Build matching DeploymentEntity, to be returned by deploymentDAO
			DeploymentEntity twoArrayDeploymentEntity = new DeploymentEntity();
			twoArrayDeploymentEntity.setId(deployment.getId());
			twoArrayDeploymentEntity.setName(deployment.getDeploymentName());
			twoArrayDeploymentEntity.setCompliant(deployment.isCompliant());
			twoArrayDeploymentEntity.setStatus(deployment.getStatus());
			twoArrayDeploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(deployment.getServiceTemplate()));
			twoArrayDeploymentEntity.getDeployedDevices().add(DeviceInventoryUtils.toEntity(storageArray, false));
			twoArrayDeploymentEntity.setFirmwareRepositoryEntity(new FirmwareRepositoryEntity());
			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(twoArrayDeploymentEntity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getRefId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(twoArrayDeploymentEntity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(twoArrayDeploymentEntity.getId())).thenReturn(status);

			// Mock ServerFilteringUtil methods
			for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation) {
						return makeEnvironment(invocation);
					}
				}).when(filteringUtil).initFilterEnvironment(eq(component), any(HttpServletRequest.class));
			}

			String serverComponentId = "someComponentId-1";

			try {
				Deployment migratedDeployment = this.deploymentService.migrateServerComponent(deploymentId, serverComponentId);
			} catch (LocalizedWebApplicationException lwe) {
				if (Response.Status.NOT_FOUND.getStatusCode() != lwe.getResponse().getStatus()) {
					fail("CheckPermissions should fail for an Operator User when migrating a Server Component Deployment!");
				}
				// Else Ignore as we only process to the point of security access
			}
		}


	}

	public static class DeploymentServiceScaleUpNetworkTests {

		Deployment deployment;
		DeploymentService service;
		DeploymentDAO deploymentDAO;
		IAsmDeployerService asmDeployerProxy;
		DeploymentEntity entity;
		JobDataMap jobDataMap;
		ManagedDevice matchingServer;
		ServiceTemplateService serviceTemplateService;
		ServiceTemplateUtil serviceTemplateUtil;

		@Before
		public void setUp() throws IOException {
			// Build test DeploymentService
			EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);
			serviceTemplateUtil = mock(ServiceTemplateUtil.class);

			deploymentDAO = mock(DeploymentDAO.class);
			DeviceInventoryDAO deviceInventoryDAO = mock(DeviceInventoryDAO.class);
			DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
			LocalizableMessageService logService = mock(LocalizableMessageService.class);
			IPAddressPoolMgr ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
			ServerFilteringUtil filteringUtil = mock(ServerFilteringUtil.class);
			MigrationDeviceUtils migrationUtils = mock(MigrationDeviceUtils.class);
			serviceTemplateService = mock(ServiceTemplateService.class);
			INetworkService networkService = mock(INetworkService.class);
			GenericDAO genericDAO = mock(GenericDAO.class);
			AddOnModuleComponentsDAO addOnModuleDAO = mock(AddOnModuleComponentsDAO.class);
			IOIdentityMgr ioMgr = mock(IOIdentityMgr.class);
			ServiceTemplateValidator validator = mock(ServiceTemplateValidator.class);
			ServiceTemplateDAO serviceTemplateDAO = mock(ServiceTemplateDAO.class);
			FirmwareRepositoryDAO firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
			FirmwareUtil firmwareUtil = mock(FirmwareUtil.class);
			//when(firmwareUtil.getComplianceState((any(ManagedDevice.class)), any(DeviceInventoryEntity.class), any(User.class))).thenReturn(CompliantState.COMPLIANT);
			//when(firmwareUtil.getCompliantState((any(ManagedDevice.class)), any(DeviceInventoryEntity.class), any(String.class), any(HttpServletRequest.class), any(User.class))).thenReturn(CompliantState.COMPLIANT);
            
			FirmwareRepository firmwareRepoMock = mock(FirmwareRepository.class);
			when(firmwareUtil.entityToDto(any(FirmwareRepositoryEntity.class), any(Boolean.class))).thenReturn(firmwareRepoMock);

			Set<String> ipaddresses = new HashSet<>();
			ipaddresses.add("172.20.2.140");
			when(ipAddressPoolMgr.assignIPAddresses("ff80808153673475015367582bf10003","ff80808153cfb0930153d17db5e6028c",1)).thenReturn(ipaddresses);

			AsmManagerUtil asmManagerUtil = mock(AsmManagerUtil.class);
			ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);
			DeploymentValidator deploymentValidator = mock(DeploymentValidator.class);
			DeploymentNamesRefDAO deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);
			
			service = new DeploymentService(deploymentDAO,
					deviceInventoryDAO,
					deviceInventoryComplianceDAO,
					serviceTemplateUtil,
					logService,
					ipAddressPoolMgr,
					ioMgr,
					networkService,
					serviceTemplateService,
					genericDAO,
					addOnModuleDAO,
					validator,
					firmwareUtil,
					serviceTemplateDAO,
					firmwareRepositoryDAO, 
					asmManagerUtil,
					serviceDeploymentUtil,
					deploymentValidator,
					deploymentNamesRefDAO);

			IUserResource adminProxy = mock(IUserResource.class);
			User adminUser = new User();
			adminUser.setUserName("AdminUserName");
			adminUser.setEnabled(true);
			adminUser.setUserSeqId(1);
			adminUser.setRole(AsmConstants.USERROLE_ADMINISTRATOR);
			when(adminProxy.getUser(1)).thenReturn(adminUser);
			when(adminProxy.getUser(2)).thenReturn(adminUser);

			service.setMigrationDeviceUtils(migrationUtils);
			service.setAdminProxy(adminProxy);
			asmDeployerProxy = mock(IAsmDeployerService.class);
			service.setAsmDeployerProxy(asmDeployerProxy);
			DeviceGroupDAO deviceGroupDAO = mock(DeviceGroupDAO.class);
			service.setDeviceGroupDAO(deviceGroupDAO);
			service.setFilteringUtil(filteringUtil);

			IJobManager jobManager = mock(IJobManager.class);
			JobDetail mockJob = mock(JobDetail.class);
			jobDataMap = new JobDataMap();
			when(mockJob.getJobDataMap()).thenReturn(jobDataMap);
			when(jobManager.createNamedJob(ServiceDeploymentJob.class)).thenReturn(mockJob);
			Scheduler mockScheduler = mock(Scheduler.class);
			when(jobManager.getScheduler()).thenReturn(mockScheduler);
			service.setJobManager(jobManager);

			URL deploymentJson = this.getClass().getClassLoader().getResource("DeploymentServiceTest/NetworkScaleupDeployment.json");
			String textJson = IOUtils.toString(deploymentJson, Charsets.UTF_8);
			deployment = MarshalUtil.fromJSON(Deployment.class, textJson);


			// Build matching DeploymentEntity, to be returned by deploymentDAO
			entity = new DeploymentEntity();
			entity.setId(deployment.getId());
			entity.setName(deployment.getDeploymentName());
			entity.setStatus(deployment.getStatus());
			URL resource = DeploymentServiceTest.class.getClassLoader().getResource("DeploymentServiceTest/NetworkScaleupInitialServiceTemplate.xml");
			String text = IOUtils.toString(resource, Charsets.UTF_8);
			entity.setMarshalledTemplateData(text);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getComponentId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(entity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(entity.getId())).thenReturn(status);

			setupSecurityContext(service);

			// server asmguid  = ff80808153a054de0153a077df1e025e
		}

		@Test
		public void testScaleUpNetworkSuccess() throws Exception {

			String deploymentId = "ff80808153cfb0930153d17db5e6028c";

			when(deploymentDAO.getDeployment(eq(deploymentId), any(Integer.class))).thenReturn(entity);
			when(deploymentDAO.updateDeploymentStatusToInProgress(deploymentId)).thenReturn(entity);

			// Mock out asm-deployer status info for deployment
			List<AsmDeployerComponentStatus> componentStatuses = new ArrayList<>();
			for (DeploymentDevice device : deployment.getDeploymentDevice()) {
				AsmDeployerComponentStatus cs = new AsmDeployerComponentStatus();
				cs.setId(device.getComponentId());
				cs.setAsmGuid(device.getRefId());
				cs.setStatus(device.getStatus());
				cs.setType(ServiceTemplateComponent.ServiceTemplateComponentType.valueOf(device.getRefType()));
				cs.setMessage(device.getStatusMessage());
				componentStatuses.add(cs);
			}

			AsmDeployerStatus status = new AsmDeployerStatus();
			status.setId(entity.getId());
			status.setStatus(DeploymentStatusType.COMPLETE);
			status.setComponents(componentStatuses);

			when(asmDeployerProxy.getDeploymentStatus(entity.getId())).thenReturn(status);

			try {
				service.updateDeployment(deploymentId, deployment);
			} catch (LocalizedWebApplicationException lwe) {
				//WORKING ON THE UNIT TEST
			}
		}
	}



}
