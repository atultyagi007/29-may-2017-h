package com.dell.asm.asmcore.asmmanager.client.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.AbstractAsmManagerClientTest;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class ServiceTemplateClientUtilTest extends AbstractAsmManagerClientTest {

    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateClientUtilTest.class);

    private static String invalidDeploymentText;
    private static String validDeploymentText;
    private static String testMissingChildDependencyXml;
    private static String vdsClusterText;

    static {
        try {
            final URL invalidDeploymentResource =
                    ServiceTemplateClientUtilTest.class.getClassLoader().getResource("deploymentVI_Invalid.json");
            invalidDeploymentText = IOUtils.toString(invalidDeploymentResource, Charsets.UTF_8);

            final URL validDeploymentResource =
                    ServiceTemplateClientUtilTest.class.getClassLoader().getResource("deploymentVI.json");
            validDeploymentText = IOUtils.toString(validDeploymentResource, Charsets.UTF_8);
            final URL testMissingResource = Resources.getResource("testMissingChildDependencies.xml");
            testMissingChildDependencyXml = Resources.toString(testMissingResource, Charsets.UTF_8);

            final URL vdsClusterResource = ServiceTemplateClientUtilTest.class.getClassLoader().getResource("clusterComponent.json");
            vdsClusterText = IOUtils.toString(vdsClusterResource, Charsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException("Tests failed to run", e);
        }
    }

    private Deployment invalidDeployment;
    private Deployment validDeployment;
    private Deployment testMissingDeployment;

    @Before
    public void setUp() {
        invalidDeployment = MarshalUtil.fromJSON(Deployment.class, invalidDeploymentText);
        validDeployment = MarshalUtil.fromJSON(Deployment.class, validDeploymentText);
        testMissingDeployment = MarshalUtil.unmarshal(Deployment.class, testMissingChildDependencyXml);
    }

    @Test
    public void testFilterServiceTemplateForOnlyUpgradableSettings() {
        ServiceTemplateClientUtil.filterServiceTemplateForOnlyUpgradableSettings(
                invalidDeployment.getServiceTemplate());
        assertNotNull(invalidDeployment);
        assertNotNull(invalidDeployment.getServiceTemplate());
        assertNotNull(invalidDeployment.getServiceTemplate().getTemplateValid());
        assertFalse(invalidDeployment.getServiceTemplate().getTemplateValid().isValid());
        assertTrue(CollectionUtils.isNotEmpty(invalidDeployment.getServiceTemplate().getComponents()));
        assertTrue(invalidDeployment.getServiceTemplate().getComponents().size() == 1);
        final ServiceTemplateComponent upgradableInvalidComponent =
                invalidDeployment.getServiceTemplate().getComponents().get(0);
        assertNotNull(upgradableInvalidComponent);
        assertNotNull(upgradableInvalidComponent.getComponentValid());
        assertFalse(upgradableInvalidComponent.getComponentValid().isValid());
        assertTrue(CollectionUtils.isNotEmpty(upgradableInvalidComponent.getComponentValid().getMessages()));
        assertTrue(upgradableInvalidComponent.getComponentValid().getMessages().size() == 1);
        final AsmDetailedMessage upgradeErrorMessage =
                upgradableInvalidComponent.getComponentValid().getMessages().get(0);
        assertNotNull(upgradeErrorMessage);
        assertEquals("0a12b6f3-65b4-44b2-a489-b2b30d13b793", upgradableInvalidComponent.getId());
        LOGGER.info("size " + upgradableInvalidComponent.getResources().size());
        for (ServiceTemplateCategory resource : upgradableInvalidComponent.getResources()) {
            for (ServiceTemplateSetting parameter : resource.getParameters()) {
                LOGGER.info("Resource " + resource.getId() + " name:" + parameter.getId() + " " + parameter.getValue());
            }
        }

        assertTrue(upgradableInvalidComponent.getResources().size() == 1);
        final ServiceTemplateCategory upgradeableInvalidResource =
                upgradableInvalidComponent.getResources().get(0);
        assertNotNull(upgradeableInvalidResource);
        assertEquals("asm::server", upgradeableInvalidResource.getId());
        assertTrue(upgradeableInvalidResource.getParameters().size() == 1);
        final ServiceTemplateSetting upgradeableInvalidParameter =
                upgradeableInvalidResource.getParameters().get(0);
        assertNotNull(upgradeableInvalidParameter);
        assertEquals("razor_image", upgradeableInvalidParameter.getId());

        ServiceTemplateClientUtil.filterServiceTemplateForOnlyUpgradableSettings(
                validDeployment.getServiceTemplate());
        assertNotNull(validDeployment);
        assertNotNull(validDeployment.getServiceTemplate());
        assertNotNull(validDeployment.getServiceTemplate().getTemplateValid());
        assertTrue(validDeployment.getServiceTemplate().getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(validDeployment.getServiceTemplate().getComponents()));
    }

    @Test
    public void testFilterServiceTemplateForOnlyUpgradableSettingsThatHaveCrossResourceDependencies() {

        final ServiceTemplate serviceTemplate =
                new ServiceTemplateBuilder("0a12b6f3-65b4-44b2-a489-b2b30d131111")
                        .addTemplateValid(Boolean.FALSE)
                        .addComponent(new ComponentBuilder("0a12b6f3-65b4-44b2-a489-b2b30d132222")
                                .addComponentValid(Boolean.FALSE)
                                .addResource(new ResourceBuilder("asm::idrac")
                                        .addParameter(new ParameterBuilder("target_boot_device")
                                                .addValue("HD")
                                                .addRequired(Boolean.TRUE)
                                                .addHideFromTemplate(Boolean.FALSE))
                                        .addParameter(new ParameterBuilder("raid_configuration")
                                                .addValue(StringUtils.EMPTY)
                                                .addRequired(Boolean.TRUE)
                                                .addHideFromTemplate(Boolean.FALSE)
                                                .addDependency("target_boot_device", "HD")))
                                .addResource(new ResourceBuilder("asm::server")
                                        .addParameter(new ParameterBuilder("razor_image")
                                                .addValue("esxi-5.5")
                                                .addRequired(Boolean.TRUE)
                                                .addHideFromTemplate(Boolean.FALSE)
                                                .addDependency("target_boot_device", "SD,HD"))
                                        .addParameter(new ParameterBuilder("admin_password")
                                                .addValue(null)
                                                .addType(ServiceTemplateSettingType.PASSWORD)
                                                .addRequired(Boolean.TRUE)
                                                .addHideFromTemplate(Boolean.FALSE)
                                                .addDependency("target_boot_device", "SD,HD"))
                                        .addParameter(new ParameterBuilder("esx_mem")
                                                .addValue(StringUtils.EMPTY)
                                                .addRequired(Boolean.TRUE)
                                                .addHideFromTemplate(Boolean.FALSE)
                                                .addDependency("razor_image", "esxi-5.1,esxi-5.5"))))
                        .build();

        ServiceTemplateClientUtil.filterServiceTemplateForOnlyUpgradableSettings(serviceTemplate);

        assertNotNull(serviceTemplate);
        assertEquals("0a12b6f3-65b4-44b2-a489-b2b30d131111", serviceTemplate.getId());
        assertNotNull(serviceTemplate.getTemplateValid());
        assertFalse(serviceTemplate.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isNotEmpty(serviceTemplate.getComponents()));
        assertTrue(serviceTemplate.getComponents().size() == 1);

        final ServiceTemplateComponent upgradableInvalidComponent = serviceTemplate.getComponents().get(0);
        assertNotNull(upgradableInvalidComponent);
        assertNotNull(upgradableInvalidComponent.getComponentValid());
        assertFalse(upgradableInvalidComponent.getComponentValid().isValid());
        assertEquals("0a12b6f3-65b4-44b2-a489-b2b30d132222", upgradableInvalidComponent.getId());
        assertTrue(CollectionUtils.isNotEmpty(upgradableInvalidComponent.getResources()));
        assertTrue(upgradableInvalidComponent.getResources().size() == 2);

        final ServiceTemplateCategory upgradeableInvalidResource1 =
                upgradableInvalidComponent.getResources().get(0);
        assertNotNull(upgradeableInvalidResource1);
        assertEquals("asm::idrac", upgradeableInvalidResource1.getId());
        assertTrue(CollectionUtils.isNotEmpty(upgradeableInvalidResource1.getParameters()));
        assertTrue(upgradeableInvalidResource1.getParameters().size() == 2);

        final ServiceTemplateSetting upgradeableInvalidParameter1 =
                upgradeableInvalidResource1.getParameters().get(0);
        assertNotNull(upgradeableInvalidParameter1);
        assertEquals("target_boot_device", upgradeableInvalidParameter1.getId());

        final ServiceTemplateCategory upgradeableInvalidResource2 =
                upgradableInvalidComponent.getResources().get(1);
        assertNotNull(upgradeableInvalidResource2);
        assertEquals("asm::server", upgradeableInvalidResource2.getId());
        assertTrue(CollectionUtils.isNotEmpty(upgradeableInvalidResource2.getParameters()));
        assertTrue(upgradeableInvalidResource2.getParameters().size() == 3);

        final ServiceTemplateSetting upgradeableInvalidParameter2 =
                upgradeableInvalidResource2.getParameters().get(0);
        assertNotNull(upgradeableInvalidParameter2);
        assertEquals("razor_image", upgradeableInvalidParameter2.getId());
    }

    @Test
    public void testUpdateServiceTemplateWithUpgradedSettings() {
        ServiceTemplateClientUtil.updateServiceTemplateWithUpgradedSettings(invalidDeployment.getServiceTemplate(),
                validDeployment.getServiceTemplate().getComponents());
        assertNotNull(invalidDeployment);
        assertNotNull(invalidDeployment.getServiceTemplate());
        assertNotNull(invalidDeployment.getServiceTemplate().getTemplateValid());
        assertFalse(invalidDeployment.getServiceTemplate().getTemplateValid().isValid());
        assertTrue(CollectionUtils.isNotEmpty(invalidDeployment.getServiceTemplate().getComponents()));
        assertTrue(invalidDeployment.getServiceTemplate().getComponents().size() == 1);
        final ServiceTemplateComponent upgradableInvalidComponent =
                invalidDeployment.getServiceTemplate().getComponents().get(0);
        assertNotNull(upgradableInvalidComponent);
        assertNotNull(upgradableInvalidComponent.getComponentValid());
        assertFalse(upgradableInvalidComponent.getComponentValid().isValid());
        assertFalse(CollectionUtils.isEmpty(upgradableInvalidComponent.getComponentValid().getMessages()));
        assertEquals("0a12b6f3-65b4-44b2-a489-b2b30d13b793", upgradableInvalidComponent.getId());
        final ServiceTemplateCategory upgradeableInvalidResource =
                upgradableInvalidComponent.getResources().get(0);
        assertNotNull(upgradeableInvalidResource);
        assertEquals("asm::server", upgradeableInvalidResource.getId());
        final ServiceTemplateSetting upgradeableInvalidParameter =
                upgradeableInvalidResource.getParameters().get(2);
        assertNotNull(upgradeableInvalidParameter);
        assertEquals("razor_image", upgradeableInvalidParameter.getId());
        assertEquals("esxi-5.1", upgradeableInvalidParameter.getValue());

    }

    @Test
    public void testMissingChildDependencyTest() {
        ServiceTemplate serviceTemplate = testMissingDeployment.getServiceTemplate();
        ServiceTemplateClientUtil.filterServiceTemplateForOnlyUpgradableSettings(serviceTemplate);
        assertNotNull(serviceTemplate);
        assertNotNull(serviceTemplate.getTemplateValid());
        assertFalse(serviceTemplate.getTemplateValid().isValid());
        assertNotNull(serviceTemplate.getComponents());
        assertEquals(1, serviceTemplate.getComponents().size());
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            assertNotNull(component.getResources());
            assertEquals(1, component.getResources().size());
            for (ServiceTemplateCategory category : component.getResources()) {
                assertNotNull(category.getParameters());
                assertEquals(7, category.getParameters().size());

            }
        }
    }

    @Test
    public void testVDSPortGroup() {
        ServiceTemplateComponent cluster = MarshalUtil.fromJSON(ServiceTemplateComponent.class, vdsClusterText);
        assertNotNull(cluster);

        String networkId = "ff808081526ad980015278b5e9700dbd";
        String networksID = "ff8080815255fd7b0152560baca90138:" + networkId;
        String vdsID = "vds_name::" + networksID;
        String pgID = "vds_pg::" + networksID + "::ff8080815255fd7b0152560baca90138::1";

        assertTrue("pgID in vdsID", ServiceTemplateClientUtil.isVDSGroup(vdsID, pgID));
        assertEquals("networksID in pgID", networksID, ServiceTemplateClientUtil.extractNetworksID(pgID));

        ServiceTemplateSetting set = ServiceTemplateClientUtil.getPortGroup(cluster, networksID, "", networkId, 1, false);
        assertNotNull("Port Group doesn not exists", set);

        set = ServiceTemplateClientUtil.getPortGroup(cluster, networksID, "", networkId, 3, false);
        assertNull("Port Group exists", set);

        set = ServiceTemplateClientUtil.getPortGroup(cluster, networksID, "Port Group 3", networkId, 3, true);
        assertNotNull(set);
        assertEquals("PG display name is wrong", "Port Group 3", set.getDisplayName());

        List<String> netList = new ArrayList<String>(Arrays.asList(networksID.split(":")));

        boolean result = ServiceTemplateClientUtil.scaleupNetworkPortGroups(cluster, "WKL100 PG", true, netList, "xxx", "WKL100", 1);
        assertTrue("PG 1 NOT added", result);
        result = ServiceTemplateClientUtil.scaleupNetworkPortGroups(cluster, "WKL100 PG", true, netList, "xxx", "WKL100", 2);
        assertTrue("PG 2 NOT added", result);
        set = ServiceTemplateClientUtil.getPortGroup(cluster, networksID + ":xxx", "", "xxx", 2, false);
        assertNotNull("Port Group 2 does not exists", set);
    }

    @Test
    public void testCopyOptions() {
        ServiceTemplateComponent cluster = MarshalUtil.fromJSON(ServiceTemplateComponent.class, vdsClusterText);
        String networkId = "ff808081526ad980015278b5e9700dbd";
        String networksID = "ff8080815255fd7b0152560baca90138:" + networkId;
        ServiceTemplateSetting set = ServiceTemplateClientUtil.getPortGroup(cluster, networksID, "", networkId, 1, false);

        for (ServiceTemplateOption option: set.getOptions()) {
            option.getAttributes().put("key1","value1");
        }

        List<ServiceTemplateOption> newOptions = ServiceTemplateClientUtil.copyOptions(set.getOptions(), "vds1");
        assertNotNull(newOptions);

        for (ServiceTemplateOption option: newOptions) {
            assertEquals("VDS name", "vds1", option.getDependencyTarget());
            assertEquals("Attributes size", 1, option.getAttributes().size());
        }
    }
}
