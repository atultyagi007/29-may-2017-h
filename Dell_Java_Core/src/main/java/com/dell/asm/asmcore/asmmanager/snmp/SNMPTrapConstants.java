/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.snmp;

/**
 * This is interface providing SNMP trap constants.
 */
public interface SNMPTrapConstants {

    String SNMPTRAP_CONSTANT_PROPERTIES_FILE = "snmptrapConstant.properties";

    String SERVER_INSERT_IN_CHASSIS = "SERVER_INSERT_IN_CHASSIS";
    String SERVER_REMOVAL_FROM_CHASSIS = "SERVER_REMOVAL_FROM_CHASSIS";
    String IOM_INSERT_IN_CHASSIS = "IOM_INSERT_IN_CHASSIS";
    String IOM_REMOVAL_IN_CHASSIS = "IOM_REMOVAL_IN_CHASSIS";
    String SERVER_POWER_ON = "SERVER_POWER_ON";
    String SERVER_POWER_OFF = "SERVER_POWER_OFF";

}
