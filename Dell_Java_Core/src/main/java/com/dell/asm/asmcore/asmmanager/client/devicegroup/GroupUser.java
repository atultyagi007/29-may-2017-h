/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.client.devicegroup;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.pg.orion.common.print.Dump;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * User DTO class.
 */
@XmlRootElement(name = "GroupUser")
@XmlType(name = "GroupUser", propOrder = { "userSeqId", "userName", "firstName", "lastName", "role", "enabled" })
@ApiModel()
public class GroupUser implements Serializable {

    private static final long serialVersionUID = 7436617284922757909L;
	
    private long userSeqId;

    private String userName;

    private String firstName;

    private String lastName;
    
    private String role;

    private boolean enabled;

    /**
     * @return the userSeqId
     */
    @ApiModelProperty(value = "User Seq Id", required = true)
    public long getUserSeqId() {
        return userSeqId;
    }

    /**
     * @param userSeqId
     *            the userSeqId to set
     */
    public void setUserSeqId(long userSeqId) {
        this.userSeqId = userSeqId;
    }

    /**
     * @return the userName
     */
    @ApiModelProperty(value = "User Name", required = false)
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the firstName
     */
    @ApiModelProperty(value = "User First Name", required = false)
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    @ApiModelProperty(value = "User Last Name", required = false)
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * @return the role
     */
    @ApiModelProperty(value = "User Role", required = true)
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return true if user enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Dump.toString(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
