/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configure;

public enum AddressingMode {
    Static("static"),
    Existing("existing"),
    Dhcp("dhcp");

    private String _label;

    private AddressingMode(String label) {
        _label = label;
    }

    public String getLabel() {
        return _label;
    }

    public String getValue() {
        return name();
    }

    public static AddressingMode fromLabel(String label) {
        if (label.equals(Static.getLabel()))
            return Static;
        else if (label.equals(Existing.getLabel()))
            return Existing;
        else if (label.equals(Dhcp.getLabel()))
            return Dhcp;

        return null;
    }

    @Override
    public String toString() {
        return _label;
    }
}
