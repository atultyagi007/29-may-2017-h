
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
 * Represents chassis power settings, a part of infrastructure template. 
 * @author Bapu Patil
 *
 */
@XmlRootElement(name = "PowerSettings")
public class PowerSettings 
{
    private RedundancyPolicy redundancyPolicy;
    private boolean serverPerformanceOverRedundancy;
    private boolean enableDynamicPSUEngagement;

    public enum RedundancyPolicy {
        NO_REDUNDANCY,
        PSU_REDUNDANCY,
        AC_REDUNDANCY
    }
    
    public PowerSettings() { }
    
    public RedundancyPolicy getRedundancyPolicy() {
        return redundancyPolicy;
    }

    public void setRedundancyPolicy(RedundancyPolicy redundancyPolicy) {
        this.redundancyPolicy = redundancyPolicy;
    }

    public boolean isServerPerformanceOverRedundancy() {
        return serverPerformanceOverRedundancy;
    }

    public void setServerPerformanceOverRedundancy(boolean serverPerformanceOverRedundancy) {
        this.serverPerformanceOverRedundancy = serverPerformanceOverRedundancy;
    }

    public boolean isEnableDynamicPSUEngagement() {
        return enableDynamicPSUEngagement;
    }

    public void setEnableDynamicPSUEngagement(boolean enableDynamicPSUEngagement) {
        this.enableDynamicPSUEngagement = enableDynamicPSUEngagement;
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
