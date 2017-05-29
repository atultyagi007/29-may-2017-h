/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.firmware;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Device class to return the firmware list
 * 
 * @author Bapu_Patil
 * 
 */
@XmlRootElement(name = "Device")
@ApiModel()
public class Device extends ManagedDevice {

	@ApiModelProperty(value = "Firmware Version", required = true)
	protected String firmwareVersion;

	@ApiModelProperty(value = "Component List")
	protected List<Component> components;

	@ApiModelProperty(value = "Device List")
	protected List<Device> devices;

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public List<Component> getComponent() {
		return components;
	}

	public void setComponent(List<Component> components) {
		this.components = components;
	}

	public List<Device> getDevice() {
		return devices;
	}

	public void setDevice(List<Device> devices) {
		this.devices = devices;
	}

}
