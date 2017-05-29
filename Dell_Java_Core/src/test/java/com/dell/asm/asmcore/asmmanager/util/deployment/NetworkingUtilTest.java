/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dell.asm.asmcore.asmmanager.app.rest.AddOnModuleServiceTest;
import com.dell.asm.asmcore.asmmanager.app.rest.ServiceTemplateServiceTest;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidator;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidatorTest;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.app.TestAsmManagerAppConfig;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.IpRange;
import com.dell.pg.asm.identitypool.api.network.model.StaticNetworkConfiguration;
import com.dell.pg.asm.identitypoolmgr.network.IIPAddressPoolMgr;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.PingUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class NetworkingUtilTest {
    private static final String TEST_MANAGEMENT_IP = "172.28.2.130";

    private static final String TEST_GUID = "8888";
    private PingUtil pingUtil;
    private IIPAddressPoolMgr ipAddressPoolMgr;
    private DnsUtil dnsUtil;
    private NetworkingUtil networkingUtil;
    private INetworkService networkService;
    private ServiceTemplateComponent server;
    private Map<Network,String> networkIpMap;

    @Before
    public void setUp() throws IOException {
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
        pingUtil = mock(PingUtil.class);
        LocalizableMessageService logService = mock(LocalizableMessageService.class);
        ipAddressPoolMgr = mock(IIPAddressPoolMgr.class);
        dnsUtil = mock(DnsUtil.class);
        networkingUtil = new NetworkingUtil(pingUtil, logService, dnsUtil);
        networkService = mock(INetworkService.class);
        URL url1 = Resources.getResource("util/networkingUtilComponent.xml");
        String xml1 = Resources.toString(url1, Charsets.UTF_8);
        server = MarshalUtil.unmarshal(ServiceTemplateComponent.class, xml1);
        networkIpMap = new HashMap<>();
        for (Network network : ServiceTemplateClientUtil.findStaticNetworks(server)) {
            if (network.getName().equals("HypervisorManagement")) {
                networkIpMap.put(network,"172.28.2.130");
            } else if (network.getName().equals("iSCSI IP Partition 1")) {
                networkIpMap.put(network,"172.16.2.130");
            } else if (network.getName().equals("iSCSI IP Partition 2")) {
                networkIpMap.put(network,"172.16.2.131");
            }
        }
    }

    private void buildServiceTemplateComponent(String value) throws IOException {

        ServiceTemplateCategory osResource = server.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        ServiceTemplateSetting ipSource = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
        if (ipSource != null) {
            ipSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL);
        }
        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
            // Set ip management source parameter
            ServiceTemplateSetting sourceParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + entry.getKey().getName());
            assertNotNull(sourceParam);
            sourceParam.setValue(value);

            // Set hostname parameter
            ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + entry.getKey().getName());
            assertNotNull(ipParam);
            ipParam.setValue(entry.getValue());

        }
    }

    private void buildDnsTestComponent() throws IOException {
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
        ServiceTemplateCategory osResource = server.getTemplateResource(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        ServiceTemplateSetting ipSource = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
        if (ipSource != null) {
            ipSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL);
        }
        int i = 0;
        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
                assertTrue(entry.getKey().isStatic());
                assertNotNull(entry.getKey().getStaticNetworkConfiguration());
                entry.getKey().getStaticNetworkConfiguration().setPrimaryDns("dns-test-1");
                entry.getKey().getStaticNetworkConfiguration().setSecondaryDns("dns-test-2");
                entry.getKey().getStaticNetworkConfiguration().setDnsSuffix(null);
                // Set ip management source parameter
                ServiceTemplateSetting sourceParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + entry.getKey().getName());
                assertNotNull(sourceParam);
                sourceParam.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_DNS);

                // Set hostname parameter
                ServiceTemplateSetting hostnameParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                assertNotNull(hostnameParam);
                hostnameParam.setValue("test-server");
        }
    }

    @Test
    public void testMassageNetworks() throws IOException {
        URL url1 = Resources.getResource("util/networkingUtilMassageNetworks.xml");
        String xml1 = Resources.toString(url1, Charsets.UTF_8);
        ServiceTemplate massageNetworksServiceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, xml1);

        com.dell.pg.asm.identitypool.api.network.model.Network hm = new com.dell.pg.asm.identitypool.api.network.model.Network();
        hm.setId("ff808081501988a201501d56dfb90000");
        hm.setName("Hypervisor Management");
        hm.setType(NetworkType.HYPERVISOR_MANAGEMENT);
        hm.setVlanId(28);
        hm.setStatic(true);
        StaticNetworkConfiguration staticConfig = new StaticNetworkConfiguration();
        hm.setStaticNetworkConfiguration(staticConfig);
        staticConfig.setGateway("172.28.0.1");
        staticConfig.setSubnet("255.255.0.0");
        staticConfig.setDnsSuffix("aidev.com");
        staticConfig.setPrimaryDns("172.20.0.8");
        IpRange range = new IpRange();
        range.setId("ff808081501988a201501d56dfba0001");
        range.setStartingIp("172.28.5.115");
        range.setEndingIp("172.28.5.145");
        staticConfig.getIpRange().add(range);
        when(networkService.getNetwork("ff808081501988a201501d56dfb90000")).then(new Returns(hm));

        com.dell.pg.asm.identitypool.api.network.model.Network iscsi = new com.dell.pg.asm.identitypool.api.network.model.Network();
        iscsi.setId("ff808081501988a201501d56e3790045");
        iscsi.setName("iSCSI");
        iscsi.setType(NetworkType.STORAGE_ISCSI_SAN);
        iscsi.setVlanId(16);
        iscsi.setStatic(true);
        staticConfig = new StaticNetworkConfiguration();
        iscsi.setStaticNetworkConfiguration(staticConfig);
        staticConfig.setGateway("172.16.0.1");
        staticConfig.setSubnet("255.255.0.0");
        staticConfig.setDnsSuffix("aidev.com");
        staticConfig.setPrimaryDns("172.20.0.8");
        range = new IpRange();
        range.setId("ff808081501988a201501d56e3790046");
        range.setStartingIp("172.16.5.115");
        range.setEndingIp("172.16.5.145");
        staticConfig.getIpRange().add(range);
        when(networkService.getNetwork("ff808081501988a201501d56e3790045")).then(new Returns(iscsi));

        com.dell.pg.asm.identitypool.api.network.model.Network pxe = new com.dell.pg.asm.identitypool.api.network.model.Network();
        pxe.setId("ff808081501988a201501d56e4a50066");
        pxe.setName("PXE");
        pxe.setType(NetworkType.PXE);
        pxe.setVlanId(22);
        pxe.setStatic(false);
        when(networkService.getNetwork("ff808081501988a201501d56e4a50066")).then(new Returns(pxe));

        com.dell.pg.asm.identitypool.api.network.model.Network work = new com.dell.pg.asm.identitypool.api.network.model.Network();
        work.setId("ff808081501988a201501d56e5790067");
        work.setName("Workload");
        work.setType(NetworkType.PRIVATE_LAN);
        work.setVlanId(20);
        work.setStatic(false);
        when(networkService.getNetwork("ff808081501988a201501d56e5790067")).then(new Returns(work));

        try {
            networkingUtil.massageNetworks(massageNetworksServiceTemplate.getComponents(),networkService);
            for (ServiceTemplateComponent component : massageNetworksServiceTemplate.getComponents()) {
                // if server component or vm component
                if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                        ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                    for (ServiceTemplateCategory resource : component.getResources()) {
                        // if server networking category or
                        // esxi vm category or
                        // hyperv vm category
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(resource.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE.equals(resource.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE.equals(resource.getId())) {
                            for (ServiceTemplateSetting param : resource.getParameters()) {
                                // if server networking configuration parameter
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(param.getId())) {
                                    NetworkConfiguration configuration = param.getNetworkConfiguration();
                                    assertNotNull(configuration);
                                    assertEquals(1,configuration.getInterfaces().size());
                                    for (Fabric fabric : configuration.getInterfaces()) {
                                        assertEquals(4,fabric.getInterfaces().size());
                                        for (Interface entry: fabric.getInterfaces()) {
                                            assertEquals(4,entry.getPartitions().size());
                                            for (Partition partition : entry.getPartitions()) {
                                                for (int i=0; i < partition.getNetworks().size(); i++) {
                                                    Network net = partition.getNetworkObjects().get(i);
                                                    assertNotNull(net);
                                                    assertEquals(partition.getNetworks().get(i), net.getId());
                                                    switch(net.getId()) {
                                                    case "ff808081501988a201501d56dfb90000":
                                                        assertEquals(net.getId(),hm.getId());
                                                        assertEquals(net.getName(),hm.getName());
                                                        assertEquals(net.getVlanId().intValue(),hm.getVlanId().intValue());
                                                        assertEquals(net.getType(),hm.getType());
                                                        assertEquals(net.isStatic(),hm.isStatic());
                                                        break;
                                                    case "ff808081501988a201501d56e3790045":
                                                        assertEquals(net.getId(),iscsi.getId());
                                                        assertTrue(net.getName().startsWith("iSCSI IP Partition "));
                                                        assertEquals(net.getVlanId().intValue(),iscsi.getVlanId().intValue());
                                                        assertEquals(net.getType(),iscsi.getType());
                                                        assertEquals(net.isStatic(),iscsi.isStatic());
                                                        break;
                                                    case "ff808081501988a201501d56e5790067":
                                                        assertEquals(net.getId(),work.getId());
                                                        assertEquals(net.getName(),work.getName());
                                                        assertEquals(net.getVlanId().intValue(),work.getVlanId().intValue());
                                                        assertEquals(net.getType(),work.getType());
                                                        assertEquals(net.isStatic(),work.isStatic());
                                                        break;
                                                    case "ff808081501988a201501d56e4a50066":
                                                        assertEquals(net.getId(),pxe.getId());
                                                        assertEquals(net.getName(),pxe.getName());
                                                        assertEquals(net.getVlanId().intValue(),pxe.getVlanId().intValue());
                                                        assertEquals(net.getType(),pxe.getType());
                                                        assertEquals(net.isStatic(),pxe.isStatic());
                                                        break;
                                                    default:
                                                        fail("Only certain networks should be in partitions");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals((param.getId())) ||
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID.equals(param.getId())) {
                                    List<Network> networks = param.getNetworks();
                                    assertNotNull(networks);
                                    assertEquals(1, networks.size());
                                    Network net = networks.get(0);
                                    assertEquals(net.getId(),work.getId());
                                    assertEquals(net.getName(),work.getName());
                                    assertEquals(net.getVlanId().intValue(),work.getVlanId().intValue());
                                    assertEquals(net.getType(),work.getType());
                                    assertEquals(net.isStatic(),work.isStatic());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("massageNetworks should not throw an exception!");
        }

    }

    @Test
    public void testReplaceManagementIpHelperRejectsUsedIps() throws IOException {
        when(pingUtil.isReachable(TEST_MANAGEMENT_IP, new Integer[0])).thenReturn(true);
        try {
            buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
            List<Network> networks = ServiceTemplateClientUtil.findStaticNetworks(server);
            Set<String> ipAddresses = new HashSet<>();
            ipAddresses.add(TEST_MANAGEMENT_IP);
            networkingUtil.replaceManagementIp(TEST_GUID, networks.get(0), ipAddresses, server, ipAddressPoolMgr, new ArrayList<Network>());
            fail("replaceManagementIp succeeded when specified ip reachable");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00222.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }


    @Test
    public void testUpdateManagementIp() throws IOException {
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);

        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
                HashSet<String> addressesToReserve = new HashSet<>();
                addressesToReserve.add(entry.getValue());
                when(ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(entry.getKey().getId(), TEST_GUID,
                        addressesToReserve)).thenReturn(addressesToReserve);
        }

        List<ServiceTemplateComponent> components = new ArrayList<>();
        components.add(server);
        networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());

        // Check IP is set
        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
            Set<String> ipAddresses = new HashSet<>();
            ipAddresses.add(entry.getValue());
            verify(ipAddressPoolMgr).assignIPAddressesCreateIfNeeded(entry.getKey().getId(), TEST_GUID, ipAddresses);
            assertNotNull(entry.getKey().getStaticNetworkConfiguration());
            assertEquals(entry.getValue(), entry.getKey().getStaticNetworkConfiguration().getIpAddress());
        }
    }

    @Test
    public void testUpdateManagementIpWithMinimalComponent() throws IOException {
        URL url1 = Resources.getResource("util/networkingUtilSimpleServer.xml");
        String xml1 = Resources.toString(url1, Charsets.UTF_8);
        server = MarshalUtil.unmarshal(ServiceTemplateComponent.class, xml1);
        Set<Network> networks = new HashSet<>(ServiceTemplateClientUtil.findStaticNetworks(server));
        ServiceTemplateCategory osResource = server.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        ServiceTemplateSetting ipSource = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
        if (ipSource != null) {
            ipSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL);
        }
        for (Network network : networks) {
            ServiceTemplateSetting sourceParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
            if (sourceParam != null) {
                sourceParam.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
            }
            ServiceTemplateSetting manageIp = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
            if (manageIp != null) {
                manageIp.setValue("172.28.2.130");
            }
            HashSet<String> addressesToReserve = new HashSet<>(Arrays.asList("172.28.2.130"));
            when(ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(network.getId(), TEST_GUID, addressesToReserve))
                    .thenReturn(addressesToReserve);
        }

        List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
        networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());

        // Check IP is set
        for (Network network : networks) {
            HashSet<String> addressesToRelease = new HashSet<>(Arrays.asList("172.28.2.130"));
            verify(ipAddressPoolMgr).assignIPAddressesCreateIfNeeded(network.getId(), TEST_GUID, addressesToRelease);
            assertNotNull(network.getStaticNetworkConfiguration());
            assertEquals("172.28.2.130", network.getStaticNetworkConfiguration().getIpAddress());
        }
    }

    @Test
    public void testUpdateManagementIpCanReorderIps() throws IOException {
        URL url1 = Resources.getResource("util/networkingUtilComponent.xml");
        String xml1 = Resources.toString(url1, Charsets.UTF_8);
        ServiceTemplateComponent server1 = MarshalUtil.unmarshal(ServiceTemplateComponent.class, xml1);
        ServiceTemplateCategory osResource = server1.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        ServiceTemplateSetting ipSource = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
        if (ipSource != null) {
            ipSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL);
        }
        for (Network network : new HashSet<>(ServiceTemplateClientUtil.findStaticNetworks(server1))) {
            // Set ip management source parameter
            ServiceTemplateSetting sourceParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
            assertNotNull(sourceParam);
            sourceParam.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
            if (network.getName().equals("HypervisorManagement")) {
                // Set hostname parameter
                ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                assertNotNull(ipParam);
                ipParam.setValue("172.28.2.130");
                network.getStaticNetworkConfiguration().setIpAddress("172.28.2.129");
            } else if (network.getName().equals("iSCSI IP Partition 1")) {
                // Set hostname parameter
                ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                assertNotNull(ipParam);
                ipParam.setValue("172.16.2.130");
                network.getStaticNetworkConfiguration().setIpAddress("172.16.2.128");
            } else if (network.getName().equals("iSCSI IP Partition 2")) {
                // Set hostname parameter
                ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                assertNotNull(ipParam);
                ipParam.setValue("172.16.2.131");
                network.getStaticNetworkConfiguration().setIpAddress("172.16.2.129");
            }
        }

        ServiceTemplateComponent server2 = MarshalUtil.unmarshal(ServiceTemplateComponent.class, xml1);
        osResource = server2.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        ipSource = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
        if (ipSource != null) {
            ipSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL);
        }
        for (Network network : new HashSet<>(ServiceTemplateClientUtil.findStaticNetworks(server2))) {
            // Set ip management source parameter
            ServiceTemplateSetting sourceParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
            assertNotNull(sourceParam);
            sourceParam.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
            if (network.getName().equals("HypervisorManagement")) {
                // Set hostname parameter
                ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                assertNotNull(ipParam);
                ipParam.setValue("172.28.2.129");
                network.getStaticNetworkConfiguration().setIpAddress("172.28.2.130");
            } else if (network.getName().equals("iSCSI IP Partition 1")) {
                // Set hostname parameter
                ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                assertNotNull(ipParam);
                ipParam.setValue("172.16.2.128");
                network.getStaticNetworkConfiguration().setIpAddress("172.16.2.130");
            } else if (network.getName().equals("iSCSI IP Partition 2")) {
                // Set hostname parameter
                ServiceTemplateSetting ipParam = osResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                assertNotNull(ipParam);
                ipParam.setValue("172.16.2.129");
                network.getStaticNetworkConfiguration().setIpAddress("172.16.2.131");
            }
        }


        // Set up mock ipAddressManager to have both of the existing ips "reserved"
        final Set<String> reservedIps = new HashSet<>(Arrays.asList("172.28.2.129", "172.16.2.128", "172.16.2.129"));
        when(ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(anyString(), anyString(), anySetOf(String.class))).thenAnswer(new Answer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                Set<String> ipsToAssign = (Set<String>) arguments[2];
                for (String ip : ipsToAssign) {
                    if (!reservedIps.add(ip)) {
                        throw new AsmManagerRuntimeException("IP " + ip + " was already reserved");
                    }
                }
                return ipsToAssign;
            }
        });

        doAnswer(new Answer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                Set<String> ipsToRelease = (Set<String>) arguments[0];
                for (String ip : ipsToRelease) {
                    reservedIps.remove(ip);
                }
                return Void.TYPE;
            }
        }).when(ipAddressPoolMgr).releaseIPAddresses(anySetOf(String.class), anyString());


        List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server1, server2));
        networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());

        Set<Network> networks = new HashSet<>(ServiceTemplateClientUtil.findStaticNetworks(server1));
        for (Network network : networks) {
            Set<String> addressesToRelease = new HashSet<>();
            if (network.getName().equals("HypervisorManagement")) {
                addressesToRelease.add("172.28.2.129");
            } else if (network.getName().startsWith("iSCSI IP Partition")) {
                addressesToRelease.add("172.16.2.129");
                addressesToRelease.add("172.16.2.128");
            }
            verify(ipAddressPoolMgr).releaseIPAddresses(addressesToRelease, network.getId());
            Set<String> addressesToReserve = new HashSet<>();
            addressesToReserve.add(network.getStaticNetworkConfiguration().getIpAddress());
            verify(ipAddressPoolMgr).assignIPAddressesCreateIfNeeded(network.getId(), TEST_GUID,
                    addressesToReserve);
            if (NetworkType.HYPERVISOR_MANAGEMENT.equals(network.getType())) {
                assertNotNull(network.getStaticNetworkConfiguration());
                assertEquals("172.28.2.130", network.getStaticNetworkConfiguration().getIpAddress());
            } else {
                String iScsi1 = "172.16.2.130";
                String iScsi2 = "172.16.2.131";
                assertNotNull(network.getStaticNetworkConfiguration());
                if (network.getName().endsWith("1")) {
                    assertEquals(iScsi1, network.getStaticNetworkConfiguration().getIpAddress());
                } else {
                    assertEquals(iScsi2, network.getStaticNetworkConfiguration().getIpAddress());
                }
            }
        }

        networks = new HashSet<>(ServiceTemplateClientUtil.findStaticNetworks(server2));
        for (Network network : networks) {
            Set<String> addressesToRelease = new HashSet<>();
            if (network.getName().equals("HypervisorManagement")) {
                addressesToRelease.add("172.28.2.130");
            } else if (network.getName().startsWith("iSCSI IP Partition")) {
                addressesToRelease.add("172.16.2.131");
                addressesToRelease.add("172.16.2.130");
            }
            verify(ipAddressPoolMgr).releaseIPAddresses(addressesToRelease, network.getId());
            Set<String> addressesToReserve = new HashSet<>();
            addressesToReserve.add(network.getStaticNetworkConfiguration().getIpAddress());
            verify(ipAddressPoolMgr).assignIPAddressesCreateIfNeeded(network.getId(), TEST_GUID,
                    addressesToReserve);
            if (NetworkType.HYPERVISOR_MANAGEMENT.equals(network.getType())) {
                assertNotNull(network.getStaticNetworkConfiguration());
                assertEquals("172.28.2.129", network.getStaticNetworkConfiguration().getIpAddress());
            } else {
                String iScsi1 = "172.16.2.128";
                String iScsi2 = "172.16.2.129";
                assertNotNull(network.getStaticNetworkConfiguration());
                if (network.getName().endsWith("1")) {
                    assertEquals(iScsi1, network.getStaticNetworkConfiguration().getIpAddress());
                } else {
                    assertEquals(iScsi2, network.getStaticNetworkConfiguration().getIpAddress());
                }
            }
        }
    }

    @Test
    public void testUpdateManagementIpFailsIfIpNotAvailable() throws IOException {
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
        when(ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(anyString(), anyString(), anySetOf(String.class)))
                .thenThrow(new AsmRuntimeException(AsmManagerMessages.internalError()));
        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
            fail("updateStaticIpsIfNeeded succeeded when ip not available");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00223.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }

    @Test
    public void testUpdateManagementIpRequiresValidIp() throws IOException {
        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
            entry.setValue("Invalid");
        }
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
            fail("testUpdateManagementIpRequiresCorrectSubnet succeeded when invalid ip specified");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00219.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }

    @Test
    public void testUpdateManagementIpRequiresCorrectSubnet() throws IOException {
        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
            entry.setValue("192.168.1.102");
        }
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);

        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
            fail("updateManagementIpRequiresCorrectSubnet succeeded when ip not on management subnet");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00221.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }

    @Test
    public void testUpdateManagementIpDoesNotValidateMissingGateway() throws IOException {
        for (Map.Entry<Network,String> entry : networkIpMap.entrySet()) {
            Network network = entry.getKey();
            if (network.getStaticNetworkConfiguration() != null) {
                network.getStaticNetworkConfiguration().setGateway(null);
            }
        }
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);

        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
        } catch (LocalizedWebApplicationException e) {
           if (e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode().equals("ASM00221")) {
               fail("testUpdateManagementIpDoesNotValidateMissingGateway succeeded when gateway value null");
           }
        }
    }

    @Test
    public void testUpdateManagementIpRequiresManagementNetwork() throws IOException {
        buildServiceTemplateComponent(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
        //Remove Networking Configuration for the server
        for (ServiceTemplateCategory resource : server.getResources()) {
            Iterator<ServiceTemplateSetting> iterator = resource.getParameters().iterator();
            while (iterator.hasNext()) {
                ServiceTemplateSetting param = iterator.next();
                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(param.getId())) {
                    iterator.remove();
                }
            }
        }

        // should exit gracefully
        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
        } catch (LocalizedWebApplicationException e) {
            fail("updateStaticIpsIfNeeded succeeded without management network");
        }
    }



    @Test
    public void testUpdateManagementIpsFromDns() throws IOException {
        networkIpMap = new HashMap<>();
        Set<Network> networks = new HashSet<>(ServiceTemplateClientUtil.findManagementNetworks(server));
        for (Network network : networks) {
            networkIpMap.put(network, "172.28.2.130");
        }
        buildDnsTestComponent();
        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(server);
        ServiceTemplateUtil.upgradeNetworkConfiguration(networkConfiguration);
        for (Network network : ServiceTemplateClientUtil.findStaticNetworks(server)) {
            assertTrue(network.isStatic());
            assertNotNull(network.getStaticNetworkConfiguration());
            network.getStaticNetworkConfiguration().setDnsSuffix(null);
        }
        when(dnsUtil.lookup(anyString(), anyString(), anyString())).thenReturn("172.28.2.130");
        when(ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(anyString(), anyString(), anySetOf(String.class)))
                .thenReturn(new HashSet<>(Arrays.asList("172.28.2.130")));

        List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
        networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());

        // Check IP is set
        networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(server);
        ServiceTemplateUtil.upgradeNetworkConfiguration(networkConfiguration);
        for (Network network : networkConfiguration.getNetworks(NetworkType.HYPERVISOR_MANAGEMENT)) {
            assertNotNull(network.getStaticNetworkConfiguration());
            assertEquals("172.28.2.130", network.getStaticNetworkConfiguration().getIpAddress());
        }
    }

    @Test
    public void testUpdateManagementIpsFromDnsWithSuffix() throws IOException {
        networkIpMap = new HashMap<>();
        Set<Network> networks = new HashSet<>(ServiceTemplateClientUtil.findManagementNetworks(server));
        for (Network network : networks) {
            networkIpMap.put(network, "172.28.2.130");
        }
        buildDnsTestComponent();
        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(server);
        ServiceTemplateUtil.upgradeNetworkConfiguration(networkConfiguration);
        for (Network network : networkConfiguration.getNetworks(NetworkType.HYPERVISOR_MANAGEMENT)) {
            network.getStaticNetworkConfiguration().setDnsSuffix("aidev.com");
        }

        // Respond to "test-server.aidev.com" but not "test-server"
        when(dnsUtil.lookup(eq("test-server.aidev.com"), anyString(), anyString())).thenReturn("172.28.2.130");

        when(ipAddressPoolMgr.assignIPAddressesCreateIfNeeded(anyString(), anyString(), anySetOf(String.class)))
                .thenReturn(new HashSet<>(Arrays.asList("172.28.2.130")));

        List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
        networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());

        // Check IP is set
        networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(server);
        for (Network network : networkConfiguration.getNetworks(NetworkType.HYPERVISOR_MANAGEMENT)) {
            assertNotNull(network.getStaticNetworkConfiguration());
            assertEquals("172.28.2.130", network.getStaticNetworkConfiguration().getIpAddress());
        }
    }

    @Test
    public void testUpdateManagementIpsFromDnsWithoutDnsServers() throws IOException {
        networkIpMap = new HashMap<>();
        Set<Network> networks = new HashSet<>(ServiceTemplateClientUtil.findManagementNetworks(server));
        for (Network network : networks) {
            networkIpMap.put(network, "172.28.2.130");
        }
        buildDnsTestComponent();
        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(server);
        ServiceTemplateUtil.upgradeNetworkConfiguration(networkConfiguration);
        for (Network network : ServiceTemplateClientUtil.findStaticNetworks(server)) {
            assertTrue(network.isStatic());
            assertNotNull(network.getStaticNetworkConfiguration());
            network.getStaticNetworkConfiguration().setPrimaryDns(null);
            network.getStaticNetworkConfiguration().setSecondaryDns(null);
            network.getStaticNetworkConfiguration().setDnsSuffix(null);
        }

        // Set dnsUtil to fail to lookup all hostnames (return null)
        when(dnsUtil.lookup(anyString(), anyString(), anyString())).thenReturn(null);

        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
            fail("Update management IPs from dns succeeded when dns failed");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00225.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }

    @Test
    public void testUpdateManagementIpsFromDnsWhichFails() throws IOException {
        networkIpMap = new HashMap<>();
        Set<Network> networks = new HashSet<>(ServiceTemplateClientUtil.findManagementNetworks(server));
        for (Network network : networks) {
            networkIpMap.put(network, "172.28.2.130");
        }
        buildDnsTestComponent();

        // Set dnsUtil to fail to lookup all hostnames (return null)
        when(dnsUtil.lookup(anyString(), anyString(), anyString())).thenReturn(null);

        try {
            List<ServiceTemplateComponent> components = new ArrayList<>(Arrays.asList(server));
            networkingUtil.updateStaticIpsIfNeeded(TEST_GUID, components, ipAddressPoolMgr, new ArrayList<Network>());
            fail("Update management IPs from dns succeeded when dns failed");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00226.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }
}
