/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import static org.junit.Assert.assertEquals;

public class ApplicationModuleTest {
    // Run with -DDUMP_OUTPUT=true to print sample JSON
    static final boolean DUMP_OUTPUT = Boolean.valueOf(System.getProperty("DUMP_OUTPUT", "false"));

    static ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    public static String toJson(ApplicationModule module) throws IOException, JsonParseException, JsonMappingException {
        StringWriter sw = new StringWriter();
        OBJECT_MAPPER.writeValue(sw, module);
        return sw.toString();
    }

    public static ApplicationModule fromJson(String json) throws IOException, JsonParseException, JsonMappingException {
        return OBJECT_MAPPER.readValue(json, ApplicationModule.class);
    }

    ServiceTemplateSetting buildSetting(String id, String displayName, ServiceTemplateSetting.ServiceTemplateSettingType type) {
        ServiceTemplateSetting ret = new ServiceTemplateSetting();
        ret.setId(id);
        ret.setDisplayName(displayName);
        ret.setType(type);
        return ret;
    }

    @Test
    public void roundTrip() throws IOException {
        ApplicationModule module = new ApplicationModule();
        module.setName("asm-linux_postinstall");
        module.setVersion("0.1.0");

        module.getRequirements().add(new ModuleRequirement("pe", ">= 3.0.0 < 2015.3.0"));
        module.getRequirements().add(new ModuleRequirement("puppet", ">= 3.0.0 < 5.0.0"));

        module.getOperatingSystemSupport().add(new OsReleaseInfo("RedHat", "5", "6", "7"));
        module.getOperatingSystemSupport().add(new OsReleaseInfo("CentOS", "5", "6", "7"));

        ServiceTemplateComponent component = new ServiceTemplateComponent();
        component.setId("component-linux_postinstall-1");
        component.setName("linux_postinstall");
        component.setType(ServiceTemplateComponent.ServiceTemplateComponentType.SERVICE); // TODO: should be auto-generated
        module.getClasses().add(component);

        ServiceTemplateCategory resource = new ServiceTemplateCategory();
        resource.setId("linux_postinstall");
        resource.setDisplayName("Application Settings");
        component.getResources().add(resource);

        resource.getParameters().add(buildSetting("install_packages", "Install Packages", ServiceTemplateSettingType.STRING));
        resource.getParameters().add(buildSetting("upload_share", "Upload Share", ServiceTemplateSettingType.STRING));
        resource.getParameters().add(buildSetting("upload_file", "Upload File", ServiceTemplateSettingType.STRING));
        resource.getParameters().add(buildSetting("upload_recursive", "Upload Recursive", ServiceTemplateSettingType.BOOLEAN));
        resource.getParameters().add(buildSetting("execute_file_command", "Execute File Command", ServiceTemplateSettingType.STRING));
        resource.getParameters().add(buildSetting("yum_proxy", "Yum Proxy", ServiceTemplateSettingType.STRING));

        String json = toJson(module);
        ApplicationModule fromJson = fromJson(json);
        assertEquals(module.getName(), fromJson.getName());

        if (DUMP_OUTPUT) {
            System.out.println(json);
        }
    }
}
