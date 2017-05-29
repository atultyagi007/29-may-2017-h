
/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.pg.orion.common.print.Dump;

/**
 * Represents Firmware Settings, a part of infrastructure template. 
 * @author Bapu Patil
 *
 */
@XmlRootElement(name = "FirmwareSettings")
public class FirmwareSettings
{
    private boolean selected;
    private String repositoryId;
    private boolean downgradeFWOnDevice;
    private boolean useLatestBundle;

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public boolean isDowngradeFWOnDevice() {
        return downgradeFWOnDevice;
    }

    public void setDowngradeFWOnDevice(boolean downgradeFWOnDevice) {
        this.downgradeFWOnDevice = downgradeFWOnDevice;
    }

    public boolean isUseLatestBundle() {
        return useLatestBundle;
    }

    public void setUseLatestBundle(boolean useLatestBundle) {
        this.useLatestBundle = useLatestBundle;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // Dump contents.
    @Override
    public String toString() {
            return Dump.toString(this);
    }

    @Override
    public boolean equals(Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
    }
}
