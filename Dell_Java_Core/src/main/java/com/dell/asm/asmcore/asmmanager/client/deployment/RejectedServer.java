/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RejectedServer")
public class RejectedServer {

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    private String refId;
    private String reason;
    private String serviceTag;
    private String ipaddress;

    public RejectedServer() {}

    public RejectedServer(String refId, String ipaddress, String reason) {
        this.refId = refId;
        this.ipaddress = ipaddress;
        this.reason = reason;
    }
    
    public enum Reason {
        RAID("Raid Configuration MisMatch"),
        BAD_STATE("Server is not in an available state"),
        UNMANAGED_STATE("Server is not Managed"),
        UPDATING_STATE("Server is updating firmware"),
        PENDING_STATE("Server is pending updates"),
        NIC_CAPS("Network Configuration MisMatch"),
        SD_NOT_PRESENT("SD card is required on the Server"),
        BAD_MODEL("Server not found with the model chosen in template"),
        ALREADY_DEPLOYED("Cannot deploy to a deployed server"),
        RESERVED_STATE("Cannot deploy to a reserved server"),
        CLONED_MODEL_MISMATCH("Import From Reference Server configuration mismatch"),
        FIRMWARE_NOT_COMPLIANT("Server firmware is not compliant"),
        OTHER("Server cannot be deployed"),
        VSAN_NOT_SUPPORTED("Local storage for VMware vSAN is not supported on the Server");

        String enumValue;
        Reason(String value) {
                enumValue = value;
        }
         @Override
         public String toString() {
             return enumValue;
         }
    }
        


    @Override
    public boolean equals(Object o1) {
        if (!(o1 instanceof RejectedServer))
            return false;
        RejectedServer that = (RejectedServer) o1;
        if (that.getRefId()==null && this.getRefId()==null)
            return true;
        if (this.getRefId()!=null)
            return this.getRefId().equals(that.getRefId());

        return false;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

}
