/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.common.xml.adapters;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.dell.asm.asmcore.asmmanager.client.common.xml.adapters.model.AdaptedStringMap;
import com.dell.asm.asmcore.asmmanager.client.common.xml.adapters.model.StringEntry;
import com.dell.asm.asmcore.asmmanager.client.common.xml.adapters.model.StringListEntry;
import com.dell.asm.asmcore.asmmanager.client.common.xml.adapters.model.StringMapEntry;


public final class ComplexStringMapAdapter extends XmlAdapter<AdaptedStringMap, Map<String, Map<String, String>>> {

    @Override
    public Map<String, Map<String, String>> unmarshal(AdaptedStringMap v) throws Exception {
        Map<String, Map<String, String>> map = new HashMap<>();
        for (StringMapEntry entry : v.getEntry()) {
            Map<String, String> valuesMap = new HashMap<>();
            for (StringEntry stringEntry : entry.getValue().getEntry()) {
                valuesMap.put(stringEntry.getKey(), stringEntry.getValue());
            }
            map.put(entry.getKey(), valuesMap);
        }
        return map;
    }

    @Override
    public AdaptedStringMap marshal(Map<String, Map<String, String>> v) throws Exception {
        AdaptedStringMap adaptedStringMap = new AdaptedStringMap();
        for (Map.Entry<String, Map<String, String>> mapEntry : v.entrySet()) {
            StringMapEntry sme = new StringMapEntry();
            sme.setKey(mapEntry.getKey());
            StringListEntry stringListEntry = new StringListEntry();
            sme.setValue(stringListEntry);
            for (Map.Entry<String, String> valueMapEntry : mapEntry.getValue().entrySet()) {
                stringListEntry.getEntry().add(new StringEntry(valueMapEntry.getKey(), valueMapEntry.getValue()));
            }

            adaptedStringMap.getEntry().add(sme);
        }
        return adaptedStringMap;
    }
}
