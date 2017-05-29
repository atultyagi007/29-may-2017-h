/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
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
import com.dell.asm.asmcore.asmmanager.client.util.PuppetDbUtil;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEmcDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEmcKeys;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;

public class StorageEmcVnx extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(StorageEmcVnx.class);

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        
        // Add debug log if enabled
        if (logger.isDebugEnabled()) {
            logger.debug("\nBegin Logging Puppet EMC Facts: ");
            for(String key : factLabelToValue.keySet()) {
                logger.debug("Key: " + key);
                logger.debug("Value: " + factLabelToValue.get(key));
            }
        }

        // Set Firmware Info
        this.setFirmwareComponentIDSettable(true);
        try {
            PuppetEmcDevice puppetEmcDevice = PuppetDbUtil.convertToPuppetEmcDevice(factLabelToValue);
            fwPuppetInv.setVersion(puppetEmcDevice.getVnxBlockOperatingEnvironmentSoftwareRevision());
        } catch (Exception e) {
            logger.warn("Error converting puppet facts into puppetEmcDevice.", e);
        }
        
        result.setDeviceType(getInventoryDeviceType());
        result.setServiceTag(factLabelToValue.get(PuppetEmcKeys.SERIAL_NUMBER));
        result.setModel(factLabelToValue.get(PuppetEmcKeys.MODEL_NUMBER));
        result.setVendor("EMC VNX");
        result.setDisplayName(factLabelToValue.get(PuppetEmcKeys.NAME)); // "hostname"));
        genericPuppetInvDetails(fwPuppetInv, result);
    }
}
