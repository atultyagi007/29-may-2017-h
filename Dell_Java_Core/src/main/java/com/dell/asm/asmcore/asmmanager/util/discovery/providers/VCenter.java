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
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.common.utilities.ASMCommonsUtils;

import java.util.Map;
import org.apache.log4j.Logger;

public class VCenter extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(VCenter.class);


    @Override
    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setServiceTag(factLabelToValue.get("vcenter_name"));
        result.setStatus(DiscoveryStatus.CONNECTED);
        result.setVendor(ASMCommonsUtils.VENDOR_NAME_VMWARE);
        result.setModel("vCenter");
    }
}
