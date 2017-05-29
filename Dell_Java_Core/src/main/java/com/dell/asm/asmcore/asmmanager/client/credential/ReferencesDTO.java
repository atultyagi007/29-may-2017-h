/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.credential;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "references")
public class ReferencesDTO {
    private int devices;
    private int policies;

    public static void incrementNDevices(Map<String, ReferencesDTO> map, String credentialRefId) {
        if (!map.containsKey(credentialRefId)) {
            map.put(credentialRefId, new ReferencesDTO());
        }
        ReferencesDTO refs = map.get(credentialRefId);
        refs.setDevices(refs.getDevices() + 1);
    }

    public static void incrementNPolicies(Map<String, ReferencesDTO> map, String credentialRefId) {
        if (!map.containsKey(credentialRefId)) {
            map.put(credentialRefId, new ReferencesDTO());
        }
        ReferencesDTO refs = map.get(credentialRefId);
        refs.setPolicies(refs.getPolicies() + 1);
    }

    public int getDevices() {
        return devices;
    }

    public void setDevices(int devices) {
        this.devices = devices;
    }

    public int getPolicies() {
        return policies;
    }

    public void setPolicies(int policies) {
        this.policies = policies;
    }

    public int getTotalReferences() {
        return policies + devices;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("numReferencingPolicies", policies)
                .add("numReferencingDevices", devices)
                .toString();
    }

    public void addReferences(ReferencesDTO that) {
        if (that != null) {
            this.devices += that.devices;
            this.policies += that.policies;
        }
    }
}
