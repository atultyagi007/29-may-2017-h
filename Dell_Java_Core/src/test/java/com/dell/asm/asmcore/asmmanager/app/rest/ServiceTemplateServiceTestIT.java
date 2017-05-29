/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

public class ServiceTemplateServiceTestIT {
    @BeforeClass
    public static void setUp() {
        AsmManagerApp.razorApiUrl = "http://localhost:8082/api";

    }

    @Test
    public void testBuildStorageComponents() {
        List<ServiceTemplateComponent> serviceTemplateComponents
                = new ServiceTemplateService().buildDefaultStorageComponents();
        assertNotNull(serviceTemplateComponents);
        assertEquals(3, serviceTemplateComponents.size());
    }

    @Test
    public void testGetAllTemplates() {
        ServiceTemplateService service = new ServiceTemplateService();
        ServiceTemplate[] allTemplates = service.getAllTemplates();
        System.out.println("Found " + allTemplates.length + " templates");
    }

    @Test
    public void testCrud() {
        ServiceTemplateService service = new ServiceTemplateService();
        ServiceTemplate template = service.getTemplate("1000", false);
        assertNotNull(template);

        // Strip out just one component
        template.setId(null);
        String templateName = "Test-" + new Date().getTime();
        template.setTemplateName(templateName);
        template.setCategory("ServiceTemplateTestID");
        Iterator<ServiceTemplateComponent> iterator = template.getComponents().iterator();
        while (iterator.hasNext()) {
            ServiceTemplateComponent component = iterator.next();
            if (!component.getId().equals("component-equallogic-chap-1")) {
                iterator.remove();
            }
        }
        ServiceTemplate created = service.createTemplate(template);
        assertNotNull(created);
        assertFalse(created.getTemplateValid().isValid());

        // Confirm template is listed in getAllTemplates
        ServiceTemplate[] allTemplates = service.getAllTemplates();
        boolean found = false;
        for (ServiceTemplate t : allTemplates) {
            if (templateName.equals(t.getTemplateName())) {
                found = true;
                break;
            }
        }
        assertTrue("Created template not listed in getAllTemplates", found);

        ServiceTemplateComponent storageComponent = created.getComponents().get(0);
        ServiceTemplateCategory resource = storageComponent.getResources().get(0);

        // Set target equallogic
        ServiceTemplateSetting targetEquallogic = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
        targetEquallogic.setValue(targetEquallogic.getOptions().get(1).getValue());

        // Set volume name to "Create New Volume" (first option in drop-down)
        ServiceTemplateSetting volume = resource.getParameter("title");
        volume.setValue(volume.getOptions().get(1).getValue());

        // Set new volume name
        ServiceTemplateSetting volumeName = resource.getParameter("$new$title");
        volumeName.setValue("gs1vol1");

        // Set volume size
        resource.getParameter("size").setValue("200GB");

        // Publish it. Should fail since iqnorip field is required if storage not attached to server
        created.setDraft(false);
        try {
            service.updateTemplate(created.getId(), created);
            fail("Template publish should have failed due to missing iqnorip");
        } catch (LocalizedWebApplicationException e) {
            System.out.println(e);
        }
        ServiceTemplate updated = service.getTemplate(created.getId(), false);
        assertFalse(updated.getTemplateValid().isValid());

        // Set iqnorip
        resource = updated.getComponents().get(0).getResources().get(0);
        ServiceTemplateSetting iqnorip = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_IQNORIP_ID);
        iqnorip.setValue("127.0.0.1");

        // Publish it
        updated.setDraft(false);
        service.updateTemplate(updated.getId(), updated);
        ServiceTemplate got = service.getTemplate(updated.getId(), false);
        assertFalse(got.isDraft());
        assertTrue(got.getTemplateValid().isValid());

        service.deleteTemplate(updated.getId());
    }
}
