/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configure;

/**
 * All IOM models that are configurable by ASM puppet module.
 * Add new supported device here.
 */
public enum ConfigurableIOM {
    MXL("MXL"),
    FN2210S("2210S"),
    FN410T("410T"),
    FN410S("410S"),
    IOA("I/O Aggregator");

    private String label;
    private ConfigurableIOM(String val) {
        this.label = val;
    }

    public static boolean containsModel (String value) {
        for (ConfigurableIOM deviceType : ConfigurableIOM.values()) {
            if (value.contains(deviceType.label)) return true;
        }
        return false;
    }
}
