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

public class SwitchBrocade extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(SwitchBrocade.class);

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setDeviceType(getInventoryDeviceType());

        result.setServiceTag(factLabelToValue.get("Serial Number")); // This is what Chassis would show in IOM list as a service tag
        result.setDisplayName(factLabelToValue.get("Switch Name"));
        if(factLabelToValue.get("Model") != null) {
            result.setModel(factLabelToValue.get("Model"));
        }
        else {
            result.setModel("NOT FOUND");
        }
        
        if (factLabelToValue.get("Fabric Os") != null) {
            fwPuppetInv.setVersion(factLabelToValue.get("Fabric Os"));
        }
        
        if (result.getModel().contains(result.getVendor())) {
        	result.setModel(result.getModel().replace(result.getVendor(), ""));
        }

        setFirmwareComponentIDSettable(true);
        genericPuppetInvDetails(fwPuppetInv, result);
    }
}
