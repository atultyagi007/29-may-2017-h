package com.dell.asm.asmcore.asmmanager.util.template.adjuster;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.AbstractAsmManagerTest;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;

/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
public class StorageAdjusterTest extends AbstractAsmManagerTest {

    private ComponentBuilder component_cluster;
    private ComponentBuilder component_server;
    private ComponentBuilder component_storage;

    @Before
    public void setUp() {

        component_cluster = new ComponentBuilder("component_cluster")
                .addComponentType(ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER)
                .addAssociatedComponent("component_server","")
                .addResource(new ResourceBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID)
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID).addValue("true"))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID).addValue("TestPod"))
                );

        component_server = new ComponentBuilder("component_server")
                .addComponentType(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER)
                .addAssociatedComponent("component_cluster","")
                .addAssociatedComponent("component_storage", "");

        component_storage = new ComponentBuilder("component_storage")
                .addComponentType(ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE)
                .addAssociatedComponent("component_server", "")
                .addResource(new ResourceBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID)
                    .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ADD_TO_SDRS_ID)
                        .addHideFromTemplate(true))
                );

    }

    @Test
    public void testRefineStorageForSDRS() {
        final ServiceTemplate template = new ServiceTemplateBuilder("TestTemplate")
                .addComponent(component_cluster)
                .addComponent(component_server)
                .addComponent(component_storage)
                .build();

        ServiceTemplateUtil.refineComponents(template, null, ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE.getLabel());

        ServiceTemplateSetting addToDRS = template.findComponentById("component_storage")
                .getResources().get(0).getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ADD_TO_SDRS_ID);

        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ADD_TO_SDRS_ID + " is hidden",
                addToDRS.isHideFromTemplate());

        List<String> wantedOptions = new ArrayList<>();
        wantedOptions.add("TestPod");
        List<String> foundOptions = new ArrayList<>();

        for(ServiceTemplateOption option : addToDRS.getOptions()) {
            foundOptions.add(option.getValue());
        }

        assertEquals("Setting add_to_sdrs has incorrect options", wantedOptions, foundOptions);
    }
}