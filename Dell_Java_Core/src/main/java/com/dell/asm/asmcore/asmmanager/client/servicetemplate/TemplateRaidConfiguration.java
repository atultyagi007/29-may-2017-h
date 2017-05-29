/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "raidtype", "basicraidlevel", "virtualdisks", "enableglobalhotspares", "globalhotspares", "minimumssd", "enableglobalhotsparesexternal", "globalhotsparesexternal" ,"minimumssdexternal", "externalvirtualdisks"})
public class TemplateRaidConfiguration {

    private RaidTypeUI raidtype;

    private VirtualDiskConfiguration.UIRaidLevel basicraidlevel;

    private List<VirtualDiskConfiguration> virtualdisks;

    private List<VirtualDiskConfiguration> externalvirtualdisks;

    private boolean enableglobalhotspares;

    private int globalhotspares;

    private int minimumssd;

    private boolean enableglobalhotsparesexternal;

    private int globalhotsparesexternal;

    private int minimumssdexternal;


    public TemplateRaidConfiguration() {
        virtualdisks = new ArrayList();
        externalvirtualdisks = new ArrayList();
    }

    public RaidTypeUI getRaidtype() {
        return raidtype;
    }


    public void setRaidtype(RaidTypeUI raidtype) {
        this.raidtype = raidtype;
    }

    public VirtualDiskConfiguration.UIRaidLevel getBasicraidlevel() {
        return basicraidlevel;
    }

    public void setBasicraidlevel(VirtualDiskConfiguration.UIRaidLevel basicraidlevel) {
        this.basicraidlevel = basicraidlevel;
    }

    public List<VirtualDiskConfiguration> getVirtualdisks() {
        if (virtualdisks==null)
            virtualdisks = new ArrayList<>();
        return virtualdisks;
    }

    public void setVirtualdisks(List<VirtualDiskConfiguration> virtualdisks) {
        this.virtualdisks = virtualdisks;
    }
    
    public List<VirtualDiskConfiguration> getExternalvirtualdisks() {
        if (externalvirtualdisks==null)
            externalvirtualdisks = new ArrayList<>();
        return externalvirtualdisks;
    }

    public void setExternalvirtualdisks(List<VirtualDiskConfiguration> virtualdisks) {
        this.externalvirtualdisks = virtualdisks;
    }

    public void setEnableglobalhotsparesexternal(boolean enableglobalhotspares) {
        this.enableglobalhotsparesexternal = enableglobalhotspares;
    }
    
    public boolean isEnableglobalhotsparesexternal() {
        return enableglobalhotsparesexternal;
    }

    public void setEnableglobalhotspares(boolean enableglobalhotspares) {
        this.enableglobalhotspares = enableglobalhotspares;
    }
    
    public boolean isEnableglobalhotspares() {
        return enableglobalhotspares;
    }

    public int getGlobalhotspares() {
        return globalhotspares;
    }

    public void setGlobalhotspares(int globalhotspares) {
        this.globalhotspares = globalhotspares;
    }
    
    public int getGlobalhotsparesexternal() {
        return globalhotsparesexternal;
    }

    public void setGlobalhotsparesexternal(int globalhotspares) {
        this.globalhotsparesexternal = globalhotspares;
    }

    public int getMinimumssd() {
        return minimumssd;
    }

    public void setMinimumssd(int minimumssd) {
        this.minimumssd = minimumssd;
    }
    
    public int getMinimumssdexternal() {
        return minimumssdexternal;
    }

    public void setMinimumssdexternal(int minimumssd) {
        this.minimumssdexternal = minimumssd;
    }

    public enum RaidTypeUI {
        basic,
        advanced
    }
}
