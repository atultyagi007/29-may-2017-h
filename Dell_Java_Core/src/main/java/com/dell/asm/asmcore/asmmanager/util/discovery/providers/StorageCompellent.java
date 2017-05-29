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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class StorageCompellent extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(StorageCompellent.class);

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setDeviceType(getInventoryDeviceType());
        result.setServiceTag(factLabelToValue.get("system_SerialNumber"));
        result.setModel("NOT FOUND");
        String modelStuff = factLabelToValue.get("model");
        if(factLabelToValue.get("system_Version") != null)
        {
            fwPuppetInv.setVersion(factLabelToValue.get("system_Version"));
            setFirmwareComponentIDSettable(true);
        }

        if (modelStuff != null) {
            result.setModel(modelStuff);
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String objectStr = factLabelToValue.get("system_data");
            if (objectStr != null && objectStr.length() > 0) {
                Map<String, List<Map<String, List<String>>>> parsedJson = mapper.readValue(objectStr, Map.class);
                List<Map<String, List<String>>> objectList = parsedJson.get("system");
                if (objectList != null && objectList.size() > 0) {
                    Map<String, List<String>> systemMap = objectList.get(0);
                    result.setDisplayName(systemMap.get("Name").get(0));
                }
            }
        }catch(IOException ioe) {
            logger.error("Can't find data in compellent facts", ioe);
        }
        genericPuppetInvDetails(fwPuppetInv, result);
    }
}
