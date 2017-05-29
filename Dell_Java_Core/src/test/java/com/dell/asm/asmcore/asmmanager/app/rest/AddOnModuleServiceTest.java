/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ApplicationModule;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleOperatingSystemVersionDAO;
import com.dell.asm.asmcore.asmmanager.db.OperatingSystemVersionDAO;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AddOnModuleServiceTest {

    private static String invalidInputJson;

    private AddOnModuleService addOnModuleService;
    private ApplicationModule invalidApplicationModule;

    @BeforeClass
    public static void setUpOnce() throws IOException {
        URL url = Resources.getResource("add_on_module_invalid_asm_input.json");
        invalidInputJson = Resources.toString(url, Charsets.UTF_8);

    }

    @Before
    public void setUp() throws Exception {
        addOnModuleService = mockAddOnModuleService();
        invalidApplicationModule = AddOnModuleService.OBJECT_MAPPER.readValue(invalidInputJson, ApplicationModule.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    public static AddOnModuleService mockAddOnModuleService() {
        AddOnModuleDAO addOnModuleDAO = mock(AddOnModuleDAO.class);
        AddOnModuleComponentsDAO addOnModuleComponentsDAO = mock(AddOnModuleComponentsDAO.class);
        OperatingSystemVersionDAO operatingSystemVersionDAO = mock(OperatingSystemVersionDAO.class);
        AddOnModuleOperatingSystemVersionDAO addOnModuleOperationSystemVersionDAO = mock(AddOnModuleOperatingSystemVersionDAO.class);
        DeploymentService deploymentService = mock(DeploymentService.class);
        ServiceTemplateService serviceTemplateService = mock(ServiceTemplateService.class);
        AddOnModuleService addOnModuleService = new AddOnModuleService();
        addOnModuleService.setAddOnModuleDAO(addOnModuleDAO);
        addOnModuleService.setAddOnModuleComponentsDAO(addOnModuleComponentsDAO);
        addOnModuleService.setOperatingSystemVersionDAO(operatingSystemVersionDAO);
        addOnModuleService.setAddOnModuleOperatingSystemVersionDAO(addOnModuleOperationSystemVersionDAO);
        addOnModuleService.setDeploymentService(deploymentService);
        addOnModuleService.setServiceTemplateService(serviceTemplateService);
        return addOnModuleService;
    }

    @Test
    public void testInvalidApplicationModule() throws Exception {
        try {
            addOnModuleService.validateApplicationModule(invalidApplicationModule, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}