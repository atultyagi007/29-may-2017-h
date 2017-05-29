/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import java.util.Map;
import org.apache.log4j.Logger;

public class SwitchCiscoNexus extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(SwitchCiscoNexus.class);

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setDeviceType(getInventoryDeviceType());
        result.setServiceTag(factLabelToValue.get("chassisserialnumber"));
        if(factLabelToValue.get("hostname") != null)
            result.setDisplayName(factLabelToValue.get("hostname"));
        else
            result.setDisplayName("Unknown");

        if(factLabelToValue.get("model") != null)
            result.setModel(factLabelToValue.get("model"));
        else
            result.setModel("NOT FOUND");
        result.setVendor("CISCO");
        genericPuppetInvDetails(fwPuppetInv, result);
    }
}
