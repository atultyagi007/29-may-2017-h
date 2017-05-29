/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.vsphere;

import java.util.ArrayList;
import java.util.List;

import com.dell.pg.orion.common.utilities.DBUtils;

public final class VcenterUtils {
    private VcenterUtils() {}

    // The device type this RA supports.
    public static final String DEVICE_TYPE = "vcenter";

    // Device reference types (kinds of servers).
    public static final String VCENTER_REF_TYPE = "vcenterRefType";

    // Identity reference types (kinds of identities).
    public static final String VCENTER_IDENTITY_REF_TYPE = "vcenterIdentityRefType";

    // Discovery request reference types.
    public static final String DISCOVERY_IP_REF_TYPE = "discoveryIpRefType";

    // name of the default credential
    public static final String DEFAULT_CREDENTIAL_LABEL = "Dell vcenter default";
    

    public static String encodeEnumArrayAsString(Enum<?>[] array) {
        List<String> valueStrings = new ArrayList<>(array.length);
        for (Object o : array)
            valueStrings.add(o.toString());
        return DBUtils.encodeStringList(valueStrings);
    }
}
