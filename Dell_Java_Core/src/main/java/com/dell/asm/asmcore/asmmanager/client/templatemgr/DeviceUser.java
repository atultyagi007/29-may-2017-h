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
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.pg.asm.chassis.client.ClientUtils;
import com.dell.pg.orion.common.print.Dump;

/**
 * Represents chassis user policy for servers, a part of management template. 
 * @author Prabhat_Tripathi
 *
 */
@XmlRootElement(name = "DeviceUser")
public class DeviceUser {

	private static final long serialVersionUID = 1L;
	
	public static final String KEY_USERNAME = "CHASSIS_USERNAME";
	public static final String KEY_PASSWORD = "CHASSIS_PASSWORD";
	public static final String KEY_USER_ROLE = "USER_ROLE";
	public static final String KEY_ENABLED = "ENABLED";

	private boolean enabled;
	private String userName;
	private String password;
	private String userRole;
	

	public DeviceUser() {
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String iDracUserName) {
		this.userName = iDracUserName;
	}

	public String getPassword() {
		return (password);
	}

	public void setPassword(String iDracPassword) {
		this.password = iDracPassword;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	

	
}
