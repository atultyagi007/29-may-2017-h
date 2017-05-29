/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

/**
 * @author Mike_Orr
 * 
 */
public enum AsmIpTypeEnum {
	
    USE_STATIC_IP("USE_STATIC"), USE_DHCP("USE_DHCP"), USE_EXISTING_IP("USE_EXISTING");
    private final String value;

    private AsmIpTypeEnum(String newValue) {
        value = newValue;
    }

    public String value() {
        return value;
    }

    public static AsmIpTypeEnum fromValue(String newValue) {
        for (AsmIpTypeEnum candidate : AsmIpTypeEnum.values()) {
            if (candidate.value.equals(newValue)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(newValue);
    }

}
