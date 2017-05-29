/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.util.discovery.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.dell.pg.orion.common.utilities.MarshalUtil;

import junit.framework.Assert;

public class DeviceTypeCheckIT {
    private static final boolean QUIET = System.getProperty("QUIET") == null
            ? true : Boolean.valueOf(System.getProperty("QUIET"));
    public static final String TEST_SAMPLE_IP = "192.168.1.100";

    @Test
    public void testDeviceType() 
    {
        InfrastructureDevice device = new InfrastructureDevice();
        device.setIpAddress("192.168.113.20");
        device.setChassisCredentialId("abcde");
        DiscoverDeviceType deviceType = DeviceTypeCheckUtil.checkDeviceType(device);
        Assert.assertEquals(deviceType.name(), "CMC");
    }

    @Test
    public void testPingSweepVcenter() {
        InfrastructureDevice device = new InfrastructureDevice();
        device.setIpAddress("10.255.7.134");
        device.setvCenterCredentialId("abcde");
        DiscoverDeviceType deviceType = DeviceTypeCheckUtil.checkDeviceType(device);
        assertEquals(DiscoverDeviceType.VCENTER, deviceType);
    }
}
