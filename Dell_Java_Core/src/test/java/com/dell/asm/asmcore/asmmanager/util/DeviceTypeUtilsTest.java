/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

/*
 * @author Praharsh_Shah
 * 
 * ASM core Template validator Test
 * 
 */

package com.dell.asm.asmcore.asmmanager.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;

public class DeviceTypeUtilsTest {

    @Test
    public void testIsOfDeviceType() {
        assertTrue(DeviceTypeUtils.isOfDeviceType("IOM", DeviceType.AggregatorIOM));
        assertTrue(DeviceTypeUtils.isOfDeviceType("iom", DeviceType.MXLIOM));
        assertTrue(DeviceTypeUtils.isOfDeviceType("server", DeviceType.RackServer));
        assertTrue(DeviceTypeUtils.isOfDeviceType("SERVER", DeviceType.BladeServer));
        assertTrue(DeviceTypeUtils.isOfDeviceType("chassis", DeviceType.ChassisM1000e));
        assertTrue(DeviceTypeUtils.isOfDeviceType("CHASSIS", DeviceType.ChassisVRTX));
        assertTrue(DeviceTypeUtils.isOfDeviceType("server", DeviceType.TowerServer));
    }    
}
