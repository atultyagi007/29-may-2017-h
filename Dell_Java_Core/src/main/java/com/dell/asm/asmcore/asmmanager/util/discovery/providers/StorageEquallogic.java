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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class StorageEquallogic extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(StorageEquallogic.class);
    private static final Pattern EQL_VERSION_PATTERN = Pattern.compile("[^:]*[:][^0-9.]*([0-9.]+).*");

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setDeviceType(getInventoryDeviceType());
        result.setServiceTag(factLabelToValue.get("Group Name"));
        result.setModel(factLabelToValue.get("Model"));
        if (factLabelToValue.get("fwversion") != null) {
            String fwVersion = parseEquallogicVersion(factLabelToValue.get("fwversion"));
            if (fwVersion != null) {
                fwPuppetInv.setVersion(fwVersion);
                setFirmwareComponentIDSettable(true);
            }
        }
        result.setDisplayName(factLabelToValue.get("Group Name"));

        genericPuppetInvDetails(fwPuppetInv, result);
    }

    String parseEquallogicVersion(String versionFact) {
        versionFact = versionFact.replaceAll("\n", "");
        Matcher m = EQL_VERSION_PATTERN.matcher(versionFact);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }
}
