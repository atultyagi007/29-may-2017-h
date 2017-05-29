/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.hardware;

import com.dell.pg.asm.server.client.device.PhysicalDisk;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "virtualDisks", "hddHotSpares", "ssdHotSpares", "externalVirtualDisks", "externalHddHotSpares", "externalSsdHotSpares" })
public class RAIDConfiguration {

    public List<VirtualDisk> getVirtualDisks() {
        if (virtualDisks==null)
            virtualDisks = new ArrayList<>();
        return virtualDisks;
    }
    
    public List<VirtualDisk> getExternalVirtualDisks() {
        if (externalVirtualDisks==null)
            externalVirtualDisks = new ArrayList<>();
        return externalVirtualDisks;
    }
    
    public void setExternalVirtualDisks(List<VirtualDisk> virtualDisks) {
        this.externalVirtualDisks = virtualDisks;
    }

    public void setVirtualDisks(List<VirtualDisk> virtualDisks) {
        this.virtualDisks = virtualDisks;
    }

    public List<String> getHddHotSpares() {
        if (hddHotSpares==null)
            hddHotSpares = new ArrayList<>();
        return hddHotSpares;
    }
    
    public List<String> getExternalHddHotSpares() {
        if (externalHddHotSpares==null)
            externalHddHotSpares = new ArrayList<>();
        return externalHddHotSpares;
    }

    public void setHddHotSpares(List<String> totalHotSpares) {
        this.hddHotSpares = totalHotSpares;
    }
    
    public void setExternalHddHotSpares(List<String> totalHotSpares) {
        this.externalHddHotSpares = totalHotSpares;
    }

    public List<String> getSsdHotSpares() {
        if (ssdHotSpares==null)
            ssdHotSpares = new ArrayList<>();
        return ssdHotSpares;
    }
    
    public List<String> getExternalSsdHotSpares() {
        if (externalSsdHotSpares==null)
            externalSsdHotSpares = new ArrayList<>();
        return externalSsdHotSpares;
    }

    public void setSsdHotSpares(List<String> ssdHotSpares) {
        this.ssdHotSpares = ssdHotSpares;
    }
    
    public void setExternalSsdHotSpares(List<String> ssdHotSpares) {
        this.externalSsdHotSpares = ssdHotSpares;
    }

    private List<VirtualDisk> virtualDisks;
    private List<VirtualDisk> externalVirtualDisks;
    private List<String> hddHotSpares;
    private List<String> ssdHotSpares;
    private List<String> externalHddHotSpares;
    private List<String> externalSsdHotSpares;

    /**
     * Clear all findings.
     */
    public void reset(boolean forExternal) {
        if(forExternal){
            if (externalVirtualDisks!=null)
                externalVirtualDisks.clear();
            if (externalHddHotSpares!=null)
                externalHddHotSpares.clear();
            if (externalSsdHotSpares!=null)
                externalSsdHotSpares.clear();
        }else{
            if (virtualDisks!=null)
                virtualDisks.clear();
            if (hddHotSpares!=null)
                hddHotSpares.clear();
            if (ssdHotSpares!=null)
                ssdHotSpares.clear();
        }
    }

    public void combineExternal(RAIDConfiguration externalConfig){
        if(externalConfig.getExternalHddHotSpares().size() != 0){
            this.externalHddHotSpares = externalConfig.externalHddHotSpares;
        }
    }

}
