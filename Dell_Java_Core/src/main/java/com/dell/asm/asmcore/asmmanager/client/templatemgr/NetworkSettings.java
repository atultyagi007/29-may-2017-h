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
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.pg.orion.common.print.Dump;

/**
 * Represents Network Settings, a part of infrastructure template.
 * 
 * @author Bapu Patil
 * 
 */
@XmlRootElement(name = "NetworkSettings")
public class NetworkSettings {

    protected boolean registerChassisOnDNS;
    protected boolean registerIDracOnDNS;
    protected boolean enableIpmoOverLan;

    protected boolean enableTelnet;
    protected boolean enableSSH;

    public NetworkSettings() {
    }

    public boolean isRegisterIDracOnDNS() {
        return registerIDracOnDNS;
    }

    public void setRegisterIDracOnDNS(boolean registerIDracOnDNS) {
        this.registerIDracOnDNS = registerIDracOnDNS;
    }

    @XmlElement(name = "enableIpmiOverLan")
    public boolean isEnableIpmoOverLan() {
        return enableIpmoOverLan;
    }

    public void setEnableIpmoOverLan(boolean enableIpmoOverLan) {
        this.enableIpmoOverLan = enableIpmoOverLan;
    }

    public boolean isRegisterChassisOnDNS() {
        return registerChassisOnDNS;
    }

    public void setRegisterChassisOnDNS(boolean registerChassisOnDNS) {
        this.registerChassisOnDNS = registerChassisOnDNS;
    }

    public boolean isEnableTelnet() {
        return enableTelnet;
    }

    public void setEnableTelnet(boolean enableTelnet) {
        this.enableTelnet = enableTelnet;
    }

    public boolean isEnableSSH() {
        return enableSSH;
    }

    public void setEnableSSH(boolean enableSSH) {
        this.enableSSH = enableSSH;
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
