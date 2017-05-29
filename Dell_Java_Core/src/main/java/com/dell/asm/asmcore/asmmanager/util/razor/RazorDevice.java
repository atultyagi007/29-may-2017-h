/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.razor;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlRootElement
@XmlType(propOrder = {
        "id",
        "status",
        "ipAddress",
        "macAddress",
        "manufacturer",
        "facts",
})
public class RazorDevice {
    private String id;
    private RazorDeviceStatus status;
    private String ipAddress;
    private String macAddress;
    private String manufacturer;
    private String uuid;
    
    private Map<String, String> facts = new HashMap<>();
    private Map<String, String> policy = new HashMap<>(); // if this device is connected to a policy
	private Map<String, String> tags = new HashMap<>(); // if this device is connected to a policy with tags

    public Map<String, String> getPolicy() {
		return policy;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setPolicy(Map<String, String> policy) {
		this.policy = policy;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}
	
    public Map<String, String> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, String> facts) {
        this.facts = facts;
    }

    public RazorDeviceStatus getStatus() {
        return status;
    }

    public void setStatus(RazorDeviceStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("status", status)
                .append("ipAddress", ipAddress)
                .append("macAddress", macAddress)
                .append("manufacturer", manufacturer)
                .toString();
    }
}
