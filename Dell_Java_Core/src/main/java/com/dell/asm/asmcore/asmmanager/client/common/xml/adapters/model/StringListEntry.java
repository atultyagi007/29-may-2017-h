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

public class StringListEntry {

    private List<StringEntry> entry = new ArrayList<StringEntry>();

    public StringListEntry() {
    }

    public StringListEntry(List<StringEntry> entry) {
        this.entry = entry;
    }

    public List<StringEntry> getEntry() {
        return entry;
    }

    public void setEntry(List<StringEntry> entry) {
        this.entry = entry;
    }

}
