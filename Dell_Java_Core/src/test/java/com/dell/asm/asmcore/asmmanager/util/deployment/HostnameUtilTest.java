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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;

public class HostnameUtilTest {
    private HostnameUtil hostnameUtil;
    private DnsUtil dnsUtil;
    private ServiceTemplateComponent serverComponent;

    static Deployment loadEsxiDeployment() throws IOException {
        // Set up some mock component data
        // Get some deployment data
        URL uri = HostnameUtilTest.class.getClassLoader().getResource("esxi_deployment.json");
        assertNotNull("Failed to load esxi_deployment.json", uri);
        String json = IOUtils.toString(uri, Charsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, Deployment.class);
    }

    static ServiceTemplateComponent loadEsxiComponent() throws IOException {
        Deployment deployment = loadEsxiDeployment();
        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
            if (component.getType().equals(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER)) {
                return component;
            }
        }
        return null;
    }

    @Before
    public void setUp() throws IOException {
        // Set up HostNameUtil
        dnsUtil = mock(DnsUtil.class);
        hostnameUtil = new HostnameUtil(dnsUtil);

        // Find a server
        serverComponent = loadEsxiComponent();
        assertNotNull(serverComponent);

        // Set static config we expect
        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(serverComponent);
        ServiceTemplateUtil.upgradeNetworkConfiguration(networkConfiguration);
        for (StaticNetworkConfiguration staticConfig : networkConfiguration.getStaticNetworkConfigurations(NetworkType.HYPERVISOR_MANAGEMENT)) {
            staticConfig.setIpAddress("172.18.2.10");
            staticConfig.setPrimaryDns("172.18.2.11");
            staticConfig.setSecondaryDns(null);
            staticConfig.setDnsSuffix(null);
        }
    }

    @Test
    public void testStripDnsSuffix() {
        String[][] expectations = new String[][] {
                new String[] { "foo", "foo.aidev.com", "aidev.com" },
                new String[] { "foo", "foo.aidev.com.", "aidev.com" },
                new String[] { "foo", "foo.aidev.com.", "aidev.com." },
                new String[] { "foo", "foo.aidev.com.", "aidev.com." },
                new String[] { "foo.aidev.com", "foo.aidev.com.", "aidev" },
                new String[] { "foo.aidev.com", "foo.aidev.com.", null },
        };
        for (String[] strings : expectations) {
            String expected = strings[0];
            String hostname = strings[1];
            String suffix = strings[2];
            assertEquals(expected, hostnameUtil.stripDnsSuffix(hostname, suffix));
        }
    }

    @Test
    public void testDnsPattern() throws IOException {
        when(dnsUtil.reverseLookup("172.18.2.10", "172.18.2.11", null)).thenReturn("marquez.aidev.com.");
        String hostname = hostnameUtil.replaceDnsPattern("${dns}", serverComponent);
        assertEquals("marquez.aidev.com", hostname);
    }

    @Test
    public void testDnsPatternWithSuffix() throws IOException {
        // Add DNS suffix
        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(serverComponent);
        List<StaticNetworkConfiguration> staticConfigs = networkConfiguration.getStaticNetworkConfigurations(NetworkType.HYPERVISOR_MANAGEMENT);
        for (StaticNetworkConfiguration staticConfig : staticConfigs) {
            staticConfig.setDnsSuffix("aidev.com");
        }

        when(dnsUtil.reverseLookup("172.18.2.10", "172.18.2.11", null)).thenReturn("marquez.aidev.com.");
        String hostname = hostnameUtil.replaceDnsPattern("${dns}", serverComponent);
        assertEquals("marquez", hostname);
    }

    @Test
    public void testDnsPatternThrowsLookupFailure() throws IOException {
        when(dnsUtil.reverseLookup("172.18.2.10", "172.18.2.11", null)).thenReturn(null);
        try {
            hostnameUtil.replaceDnsPattern("${dns}", serverComponent);
            fail("replaceDnsPattern succeeded when lookup failed");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00216.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }

    @Test
    public void testDnsPatternThrowsMissingIpFailure() throws IOException {
        // Clear out the static IPs in our server component
        NetworkConfiguration networkConfiguration = ServiceTemplateUtil.findNetworkConfiguration(serverComponent);
        List<Network> networks = networkConfiguration.getNetworks(NetworkType.HYPERVISOR_MANAGEMENT);
        for (Network network : networks) {
            network.setStatic(false);
        }

        try {
            hostnameUtil.replaceDnsPattern("${dns}", serverComponent);
            fail("replaceDnsPattern succeeded without static IP");
        } catch (LocalizedWebApplicationException e) {
            assertEquals(AsmManagerMessages.MsgCodes.ASM00217.toString(),
                    e.getEEMILocalizedMessageList().getMessages().get(0).getMessageCode());
        }
    }

    @Test
    public void testDnsPatternWithMinimalComponent() throws IOException {
        ServiceTemplateComponent minimalComponent = new ServiceTemplateComponent();
        ServiceTemplateCategory resource = new ServiceTemplateCategory();
        resource.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);
        resource.setDisplayName("Server (O/S Installation Only)");

        ServiceTemplateSetting managementNetwork = new ServiceTemplateSetting(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID,
                "test-network-id", ServiceTemplateSetting.ServiceTemplateSettingType.ENUMERATED);
        Network network = new Network();
        network.setStatic(true);
        StaticNetworkConfiguration staticConfig = new StaticNetworkConfiguration();
        staticConfig.setIpAddress("172.18.2.100");
        staticConfig.setSubnet("255.255.0.0");
        staticConfig.setPrimaryDns("172.18.2.10");
        staticConfig.setSecondaryDns("172.18.2.11");
        network.setStaticNetworkConfiguration(staticConfig);

        managementNetwork.setNetworks(Arrays.asList(network));
        resource.setParameters(Arrays.asList(managementNetwork));
        minimalComponent.setResources(Arrays.asList(resource));
        when(dnsUtil.reverseLookup("172.18.2.100", "172.18.2.10", "172.18.2.11")).thenReturn("marquez.aidev.com.");
        String hostname = hostnameUtil.replaceDnsPattern("${dns}", minimalComponent);
        assertEquals("marquez.aidev.com", hostname);
    }

    @Test
    public void testReplaceNumPattern() {
        String[][] expectations = new String[][] {
                new String[] {"server1", "server${num}"},
                new String[] {"server1", "server${num}", "server-b"},
                new String[] {"server2", "server${num}", "server1", "server3"},
                new String[] {"server4", "server${num}", "server3", "server2", "server1"},
        };
        for (String[] strings : expectations) {
            String expected = strings[0];
            String template = strings[1];
            String[] hostnames = Arrays.copyOfRange(strings, 2, strings.length);
            HashSet<String> allHostnames = new HashSet<String>(Arrays.asList(hostnames));
            assertEquals(expected, hostnameUtil.replaceNumPattern(template, allHostnames));
        }
    }
}