/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.common.xml.adapters.model;

import java.util.ArrayList;
import java.util.List;

public class StringMapEntry {

    private String key;

    private StringListEntry value = new StringListEntry();

    public StringMapEntry() {
    }

    public StringMapEntry(String key, StringListEntry value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public StringListEntry getValue() {
        return value;
    }

    public void setValue(StringListEntry value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringMapEntry that = (StringMapEntry) o;

        return !(getKey() != null ? !getKey().equals(that.getKey()) : that.getKey() != null);

    }

    @Override
    public int hashCode() {
        return getKey() != null ? getKey().hashCode() : 0;
    }
}
