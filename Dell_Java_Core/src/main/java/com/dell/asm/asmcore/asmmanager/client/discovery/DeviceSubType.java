/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.discovery;

/**
 * The DeviceType here is defined to be used by UI, which is different from IDeviceType, that
 * each RA defines ("chassis", "server", "iom").
 * Mapping utilities between this DeviceType and IDeviceType is provided by DeviceTypeUtils.java. 
 *
 */
public enum DeviceSubType {
    ChassisM1000e("Chassis M1000e"),
    ChassisVRTX("Chassis VRTX"),
    RackServer("Rack Server"),
    BladeServer("Blade Server"),
    AggregatorIOM("Aggregator IOM"),
    MXLIOM("MXL IOM"),
    TOR("TOR"),
    unknown("Unknown");

    private String _label;

    private DeviceSubType(String label) {
        _label = label;
    }

    public String getLabel() {
        return _label;
    }

    public String getValue() {
        return name();
    }

    @Override
    public String toString() {
        return _label;
    }
}
