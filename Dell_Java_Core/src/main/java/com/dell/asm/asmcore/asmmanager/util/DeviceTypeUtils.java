/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;


public final class DeviceTypeUtils {
    private DeviceTypeUtils() {}

    /**
     * Check if given device type is of resource adaptor type.
     * @param type the resource adaptor type.
     * @param deviceType the device type.
     * @return true if device type is of resource adaptor type, otherwise, false.
     */
    public static boolean isOfDeviceType(String type, DeviceType deviceType) {
        return deviceType.getValue().toLowerCase().indexOf(type.toLowerCase()) >= 0;
    }
    
    /**
     * Return the resource adaptor type with given device type.
     * @param deviceType the device type.
     * @return the resource adaptor type for given device type.
     */
    public static String getResourceAdaptorType(DeviceType deviceType) {
    	if (DeviceType.isChassis(deviceType)) {
    		return com.dell.pg.asm.chassis.client.ClientUtils.DEVICE_TYPE;
    	} else if (DeviceType.isRAServer(deviceType)) {
    		return com.dell.pg.asm.server.client.ClientUtils.DEVICE_TYPE;
        } else {
        	return "unknown";
        }
    }

}
