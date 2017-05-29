/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.dell.pg.orion.common.utilities.MarshalUtil;

// Simple marshalling test
public class ServiceTemplateTest {
    private static final boolean QUIET = Boolean.valueOf(System.getProperty("QUIET", "true"));

    @Test
    public void testMarshalServiceTemplate() {
        ServiceTemplateSetting parameter = new ServiceTemplateSetting();
        parameter.setId("key");
        parameter.setDisplayName("Key name");
        parameter.setValue("5");
        parameter.setRequired(false);
        parameter.setHideFromTemplate(true);
        parameter.setRequiredAtDeployment(true);

        ServiceTemplateCategory resource = new ServiceTemplateCategory();
        resource.setId("foo::bar");
        resource.setDisplayName("Resource name");
        resource.getParameters().add(parameter);

        ServiceTemplateComponent component = new ServiceTemplateComponent();
        component.setId("component id");
        component.setName("My Name");
        component.setType(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER);
        component.setSerialNumber("XXXXX");
        component.getResources().add(resource);


        ServiceTemplate template = new ServiceTemplate();
        template.setId("1000");
        template.setTemplateName("name");
        template.setCreatedBy("me");
        template.setUpdatedBy("you");
        template.getComponents().add(component);
        String xml = MarshalUtil.marshal(template, MarshalUtil.FORMATTED_OUTPUT);
        if (!QUIET) {
            System.out.println(xml);
        }
    }

    @Test
    public void testCollections() {
        ServiceTemplateSetting parameter = new ServiceTemplateSetting();
        parameter.setId("key1");
        parameter.setDisplayName("Key name");
        parameter.setValue("5");
        parameter.setRequired(false);
        parameter.setHideFromTemplate(true);
        parameter.setRequiredAtDeployment(true);

        ServiceTemplateSetting parameter2 = new ServiceTemplateSetting();
        parameter2.setId("key2");
        parameter2.setDisplayName("Key name2");
        parameter2.setValue("6");
        parameter2.setRequired(false);
        parameter2.setHideFromTemplate(true);
        parameter2.setRequiredAtDeployment(true);

        ServiceTemplateCategory resource = new ServiceTemplateCategory();
        resource.setId("foo::bar1");
        resource.setDisplayName("Resource name1");
        resource.getParameters().add(parameter);
        resource.getParameters().add(parameter2);

        ServiceTemplateCategory resource2 = new ServiceTemplateCategory();
        resource2.setId("foo::bar2");
        resource2.setDisplayName("Resource name2");
        resource2.getParameters().add(parameter);

        ServiceTemplateCategory resource3 = new ServiceTemplateCategory();
        resource2.setId("foo::bar3");
        resource2.setDisplayName("Resource name3");
        resource2.getParameters().add(parameter);

        ServiceTemplateComponent component = new ServiceTemplateComponent();
        component.setId("component id");
        component.setName("My Name");
        component.setType(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER);
        component.setSerialNumber("XXXXX");
        component.getResources().add(resource);
        component.getResources().add(resource2);

        assertTrue("Contains on Category failed positive check", component.getResources().contains(resource));
        assertFalse("Contains on Category failed negative check", component.getResources().contains(resource3));

        assertTrue("Contains on Settings failed positive check", resource.getParameters().contains(parameter2));
        assertFalse("Contains on Settings failed negative check", resource2.getParameters().contains(parameter2));

        Map<ServiceTemplateCategory, String> map = new HashMap<>();

        map.put(resource, resource.getDisplayName());
        map.put(resource2, resource2.getDisplayName());

        assertEquals("HasCode for Resource failed positive check",map.get(resource2), resource2.getDisplayName());
        assertNull("HasCode for Resource failed negative check",map.get(resource3));

        Map<ServiceTemplateSetting, String> map2 = new HashMap<>();
        map2.put(parameter, parameter.getDisplayName());

        assertEquals("HasCode for Setting failed positive check",map2.get(parameter), parameter.getDisplayName());
        assertNull("HasCode for Setting failed negative check",map2.get(parameter2));

    }
}
