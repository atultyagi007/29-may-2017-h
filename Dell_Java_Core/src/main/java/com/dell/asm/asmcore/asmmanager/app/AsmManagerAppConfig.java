/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.SettingEntity;

public class AsmManagerAppConfig {

    private static final Logger _logger = Logger.getLogger(AsmManagerAppConfig.class);

    public Integer[] getPortsToPing() {
        List<Integer> results = new ArrayList<>();

        SettingEntity portsToPing = GenericDAO.getInstance().getByName(AsmManagerApp.ASM_PORTS_TO_PING, SettingEntity.class);
        String portsValue = null;
        if (portsToPing == null || portsToPing.getValue() == null) {
            portsValue = "22,80,135";
        } else {
            portsValue = portsToPing.getValue();
        }
        List<String> items = Arrays.asList(portsValue.split(","));
        if (items != null && items.size() > 0) {
            for (String item : items) {
                try {
                    results.add(Integer.valueOf(item.trim()));
                } catch (NumberFormatException e) {
                    _logger.warn("Invalid port " + item);
                }
            }
        }
        return results.toArray(new Integer[results.size()]);
    }
}
