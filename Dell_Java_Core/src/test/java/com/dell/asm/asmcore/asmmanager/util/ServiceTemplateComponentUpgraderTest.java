package com.dell.asm.asmcore.asmmanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.AbstractAsmManagerTest;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;

public class ServiceTemplateComponentUpgraderTest extends AbstractAsmManagerTest {

    private ComponentBuilder component_7602338; // from ASM version 7.6.0.2333
    private ComponentBuilder component_8003222; // from ASM version 8.0.0.3222
    private ComponentBuilder component_8013760; // from ASM version 8.0.1.3760
    private ComponentBuilder component_8101424; // from ASM version 8.1.0.1424

    @Before
    public void setUp() {
        // component-compellent-1
        component_7602338 = new ComponentBuilder("component_7602338")
                .addResource(new ResourceBuilder("compellent::createvol"));

        // component-server-1
        component_8003222 = new ComponentBuilder("component_8003222")
                .addResource(new ResourceBuilder("asm::idrac"))
                .addResource(new ResourceBuilder("asm::server"))
                .addResource(new ResourceBuilder("asm::esxiscsiconfig"));

        // component-virtualmachine-clonehyperv-1
        component_8013760 = new ComponentBuilder("component_8013760")
                .addResource(new ResourceBuilder("asm::vm::scvmm"));

        // component-bmc-1
        component_8101424 = new ComponentBuilder("component_8101424")
                .addResource(new ResourceBuilder("bmc"));
    }

    @Test
    public void testAssignMissingComponentId() {
        final ServiceTemplate svcTemplate = new ServiceTemplateBuilder("TemplateComponentIdMissing")
                .addComponent(component_7602338)
                .addComponent(component_8003222)
                .addComponent(component_8013760)
                .addComponent(component_8101424).build();

        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.assignOriginatingComponentId(component);
        }

        assertNotNull(svcTemplate);
        assertEquals("component-compellent-1", svcTemplate.findComponentById("component_7602338").getComponentID());
        assertEquals("component-server-1", svcTemplate.findComponentById("component_8003222").getComponentID());
        assertEquals("component-virtualmachine-clonehyperv-1",
                svcTemplate.findComponentById("component_8013760").getComponentID());
        assertEquals("component-bmc-1", svcTemplate.findComponentById("component_8101424").getComponentID());
    }

    @Test
    public void testAssignMissingComponentIdWhenNotMissing() {
        final ServiceTemplate svcTemplate = new ServiceTemplateBuilder("TemplateComponentIdNotMissing")
                .addComponent(component_7602338
                        .addComponentId("component-compellent-1"))
                .addComponent(component_8003222
                        .addComponentId("component-server-1"))
                .addComponent(component_8013760
                        .addComponentId("component-virtualmachine-clonehyperv-1"))
                .addComponent(component_8101424
                        .addComponentId("component-bmc-1")).build();

        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.assignOriginatingComponentId(component);
        }

        assertNotNull(svcTemplate);
        assertEquals("component-compellent-1", svcTemplate.findComponentById("component_7602338").getComponentID());
        assertEquals("component-server-1", svcTemplate.findComponentById("component_8003222").getComponentID());
        assertEquals("component-virtualmachine-clonehyperv-1",
                svcTemplate.findComponentById("component_8013760").getComponentID());
        assertEquals("component-bmc-1", svcTemplate.findComponentById("component_8101424").getComponentID());
    }

    @Test
    public void testAssignNotFoundComponentIdWhenNotMissing() {
        final ServiceTemplate svcTemplate = new ServiceTemplateBuilder("TemplateComponentIdNotMissing")
                .addComponent(component_7602338
                        .addComponentId("NotFound-component-compellent-1"))
                .addComponent(component_8003222
                        .addComponentId("NotFound-component-server-1"))
                .addComponent(component_8013760
                        .addComponentId(null))
                .addComponent(component_8101424
                        .addComponentId(StringUtils.EMPTY)).build();

        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.assignOriginatingComponentId(component);
        }

        assertNotNull(svcTemplate);
        assertEquals("component-compellent-1", svcTemplate.findComponentById("component_7602338").getComponentID());
        assertEquals("component-server-1", svcTemplate.findComponentById("component_8003222").getComponentID());
        assertEquals("component-virtualmachine-clonehyperv-1",
                svcTemplate.findComponentById("component_8013760").getComponentID());
        assertEquals("component-bmc-1", svcTemplate.findComponentById("component_8101424").getComponentID());
    }

    @Test
    public void testComponentIdMissingOrUnknown() {
        assertFalse(com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(
                component_7602338.addComponentId("component-compellent-1").build()));
        assertFalse(com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(
                component_7602338.addComponentId("component-virtualmachine-clonehyperv-1").build()));
        assertTrue(com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(
                component_7602338.addComponentId("component-compellent-1NotInTheList").build()));
        assertTrue(com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(
                component_7602338.addComponentId("NotInListcomponent-virtualmachine-clonehyperv-1").build()));
        assertTrue(com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(
                component_7602338.addComponentId(StringUtils.EMPTY).build()));
        assertTrue(com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(
                component_7602338.addComponentId(null).build()));
    }

    @Test
    public void testIpgradeStorageVolumeSettings() throws IOException {
        URL url = Resources.getResource("ServiceTemplateServiceTest/equallogicServiceTemplate.xml");
        String serviceTemplateXml = Resources.toString(url, Charsets.UTF_8);
        ServiceTemplate serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                serviceTemplateXml);
        String storageCompId1 = "EAE56A2A-3558-4534-95ED-E4266743219F";
        String storageCompId2 = "9C6F1F28-99D5-4715-AF54-56612873024E";

        ServiceTemplateSetting settingNew1 = serviceTemplate.getTemplateSetting(storageCompId1,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX +
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);

        ServiceTemplateSetting settingNew2 = serviceTemplate.getTemplateSetting(storageCompId2,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        settingNew2.setValue("amy820cluster");

        ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(serviceTemplate);

        ServiceTemplateSetting settingNew1u = serviceTemplate.getTemplateSetting(storageCompId1,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW);
        assertNotNull(settingNew1u);
        assertEquals("Volume names -new- don't match", settingNew1.getValue(), settingNew1u.getValue());

        ServiceTemplateSetting settingNew2u = serviceTemplate.getTemplateSetting(storageCompId2,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING);
        assertNotNull(settingNew2u);
        assertEquals("Volume names -existing- don't match", "amy820cluster", settingNew2u.getValue());
        assertEquals("Setting -title- not set to option_existing", ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING
                , settingNew2.getValue());

    }
}