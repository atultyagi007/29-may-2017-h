package com.dell.asm.asmdeployer.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class AsmDeployerStatusTest {
    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }

    @Test
    public void marshalTest() throws IOException {
        URL url = Resources.getResource("sampleAsmDeployerStatus.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        AsmDeployerStatus status = OBJECT_MAPPER.readValue(json, AsmDeployerStatus.class);

        assertNotNull(status);
        assertEquals(DeploymentStatusType.COMPLETE, status.getStatus());

        assertNotNull(status.getComponents());
        assertEquals(1, status.getComponents().size());
        AsmDeployerComponentStatus component = status.getComponents().get(0);
        assertEquals(ServiceTemplateComponentType.STORAGE, component.getType());
        assertEquals(DeploymentStatusType.COMPLETE, component.getStatus());
    }
}
