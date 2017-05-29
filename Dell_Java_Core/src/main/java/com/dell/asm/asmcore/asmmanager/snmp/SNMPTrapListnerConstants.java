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
 * This is interface providing SNMP trap listener constants.
 */
public interface SNMPTrapListnerConstants {

    String DEST_NAME = "SNMPTrapManager.trap";

    String SNMP_PROPERTIES_FILE = "snmpListener.properties";
    /*
     * int PORT = 162; SNMPTrapListenerProtocolEnum PROTOCOL = SNMPTrapListenerProtocolEnum.UDP; String HOST = "127.0.0.1";
     */
    String PROP_HOST = "listener.host";
    String PROP_PORT = "listener.port";
    String PROP_PROTOCOL = "listener.protocol";
    String PROP_SNMP_V1 = "listener.snmp.v1";
    String PROP_SNMP_V2 = "listener.snmp.v2";
    String PROP_SNMP_V3 = "listener.snmp.v3";

}
