/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import java.util.Map;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;

public class SCVMM extends PuppetDevice {
    @Override
    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setDisplayName(device.getIpAddress());
        result.setVendor(factLabelToValue.get("vendor"));
        result.setServiceTag(factLabelToValue.get("serial_number"));
        result.setModel("SCVMM");
        genericPuppetInvDetails(fwPuppetInv, result);
    }
}
