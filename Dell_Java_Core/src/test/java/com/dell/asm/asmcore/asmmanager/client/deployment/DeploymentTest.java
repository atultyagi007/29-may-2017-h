/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;

public class DeploymentTest {
    private static final boolean QUIET = Boolean.valueOf(System.getProperty("QUIET", "true"));

    @Test
    public void testMarshalDeployment() throws IOException {
        Deployment deployment = new Deployment();
        deployment.setId("999");
        deployment.setDeploymentName("My deployment");
        deployment.setDeploymentDescription("This is the deployment description.");
        deployment.setCreatedBy("admin");
        deployment.setUpdatedBy("admin");
        URL resource = this.getClass().getClassLoader().getResource("DefaultServiceTemplate.json");
        String text = IOUtils.toString(resource, Charsets.UTF_8);
        ServiceTemplate serviceTemplate = MarshalUtil.fromJSON(ServiceTemplate.class, text);
        deployment.setServiceTemplate(serviceTemplate);
        String json = MarshalUtil.toJSON(deployment);
        if (!QUIET) {
            System.out.println(json);
        }
        Deployment deployment1 = MarshalUtil.fromJSON(Deployment.class, json);
        assertNotNull(deployment1);
        assertEquals(deployment.getDeploymentName(), deployment1.getDeploymentName());
    }

    @Test
    public void testMarshalServerNetworkObjects() throws IOException {
        URL url = Resources.getResource("ServerNetworkObjects.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        ServerNetworkObjects config = MarshalUtil.fromJSON(ServerNetworkObjects.class, json);
        assertNotNull(config);
    }
}
