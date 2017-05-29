
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
 * Represents time settings, a part of infrastructure template. 
 * @author Bapu Patil
 *
 */
@XmlRootElement(name = "TimeSettings")
public class TimeSettings {
    
    private int timeZoneID;
    private boolean enableNTP;
    private String primaryNTPServer;
    private String secondaryNTPServer;
    
    public TimeSettings() { }
    
    public int getTimeZoneID() {
        return timeZoneID;
    }

    public void setTimeZoneID(int timeZoneID) {
        this.timeZoneID = timeZoneID;
    }

    public boolean isEnableNTP() {
        return enableNTP;
    }

    public void setEnableNTP(boolean enableNTP) {
        this.enableNTP = enableNTP;
    }

    public String getPrimaryNTPServer() {
        return primaryNTPServer;
    }

    public void setPrimaryNTPServer(String primaryNTPServer) {
        this.primaryNTPServer = primaryNTPServer;
    }

    public String getSecondaryNTPServer() {
        return secondaryNTPServer;
    }

    public void setSecondaryNTPServer(String secondaryNTPServer) {
        this.secondaryNTPServer = secondaryNTPServer;
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
