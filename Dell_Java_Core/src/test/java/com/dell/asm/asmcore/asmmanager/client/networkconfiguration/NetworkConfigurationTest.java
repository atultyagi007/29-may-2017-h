/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.networkconfiguration;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class NetworkConfigurationTest {

    private NetworkConfiguration config;

    @Before
    public void setUp() throws IOException {
        URL url = Resources.getResource("NetworkConfiguration.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        config = MarshalUtil.fromJSON(NetworkConfiguration.class, json);
    }

    private void assertExpectedNetworkConfiguration(NetworkConfiguration config) {
        assertNotNull(config.getId());
        assertNotNull(config.getInterfaces());
        assertEquals(1, config.getInterfaces().size());

        Fabric fabric1 = config.getInterfaces().get(0);
        assertNotNull(fabric1.getId());
        assertNotNull(fabric1.getName());
        assertNotNull(fabric1.getInterfaces());
        assertEquals(4, fabric1.getInterfaces().size());
        assertEquals(2, fabric1.getNPorts());
        assertEquals(Interface.NIC_2_X_10GB, fabric1.getNictype());

        Interface i1 = fabric1.getInterfaces().get(0);
        assertNotNull(i1.getId());
        assertNotNull(i1.getName());
        assertNotNull(i1.getPartitions());
        assertEquals(4, i1.getPartitions().size());

        Partition partition = i1.getPartitions().get(0);
        assertNotNull(partition.getId());
        assertNotNull(partition.getName());
        assertEquals(0, partition.getMinimum());
        assertEquals(100, partition.getMaximum());
        assertNotNull(partition.getNetworks());
        assertEquals(1, partition.getNetworks().size());
    }

    @Test
    public void testFromJSON() {
        assertExpectedNetworkConfiguration(config);
    }

    @Test
    public void testToJSON() {
        String json = MarshalUtil.toJSON(config);
        NetworkConfiguration configuration = MarshalUtil.fromJSON(NetworkConfiguration.class, json);
        assertExpectedNetworkConfiguration(configuration);
    }

    @Test
    public void testMarshal() {
        String xml = MarshalUtil.marshal(config, MarshalUtil.FORMATTED_OUTPUT);
        NetworkConfiguration unmarshal = MarshalUtil.unmarshal(NetworkConfiguration.class, xml);
        assertExpectedNetworkConfiguration(unmarshal);
    }
}
