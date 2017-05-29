/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import com.dell.asm.server.client.policy.RaidLevel;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "id", "raidlevel", "comparator", "numberofdisks", "disktype"})
public class VirtualDiskConfiguration {
    private String id;

    private DiskMediaType disktype;

    private ComparatorValue comparator;

    private int numberofdisks;

    private UIRaidLevel raidlevel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DiskMediaType getDisktype() {
        return disktype;
    }

    public void setDisktype(DiskMediaType mediatype) {
        this.disktype = mediatype;
    }

    public ComparatorValue getComparator() {
        return comparator;
    }

    public void setComparator(ComparatorValue comparator) {
        this.comparator = comparator;
    }

    public int getNumberofdisks() {
        return numberofdisks;
    }

    public void setNumberofdisks(int numberofdisks) {
        this.numberofdisks = numberofdisks;
    }

    public UIRaidLevel getRaidlevel() {
        return raidlevel;
    }

    public void setRaidlevel(UIRaidLevel raidlevel) {
        this.raidlevel = raidlevel;
    }

    public enum ComparatorValue {
        minimum,
        maximum,
        exact
    }

    public enum DiskMediaType {
        requiressd ("SSD"),
        requirehdd ("HDD"),
        first ("First"),
        last ("Last"),
        any ("ANY");
        String configValue;
        DiskMediaType(String value) { configValue = value; }
        public static DiskMediaType fromServerValue(String value) {
            for (DiskMediaType type : DiskMediaType.values()) {
                if (type.configValue.equals(String.valueOf(value)))
                    return type;
            }
            throw new IllegalArgumentException("Wrong value for DiskMediaType: " + value + ". Supported values are: SSD, HDD, ANY");
        }

        public boolean isPhysiclaDisdType() {
            return (requiressd.equals(this) || requirehdd.equals(this) || any.equals(this));
        }
    }

    public enum UIRaidLevel {
        raid0 ("RAID 0"),
        raid1 ("RAID 1"),
        raid5 ("RAID 5"),
        raid6 ("RAID 6"),
        raid10 ("RAID 10"),
        raid50 ("RAID 50"),
        raid60 ("RAID 60"),
        nonraid ("NONRAID");

        UIRaidLevel(String value) {
            configValue = value;
        }

        String configValue;

        public static UIRaidLevel fromConfigValue(String value) {
            for (UIRaidLevel type : UIRaidLevel.values()) {
                if (type.configValue.equals(String.valueOf(value)))
                    return type;
            }
            throw new IllegalArgumentException("Wrong value for UIRaidLevel: " + value + ". Supported values are: RAID 0,RAID 1,RAID 5,RAID 6,RAID 10,RAID 50,RAID 60");
        }
    }
}
