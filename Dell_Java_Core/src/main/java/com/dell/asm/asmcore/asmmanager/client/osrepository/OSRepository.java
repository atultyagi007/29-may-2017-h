package com.dell.asm.asmcore.asmmanager.client.osrepository;

/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
import com.wordnik.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Represents a repository containing the images of OS's that may be deployed in ASM. 
 */
@XmlRootElement(name = "OSRepository")
@ApiModel()
public class OSRepository {

    // Class Variables
    public static final String STATE_AVAILABLE = "available";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_ERRORS = "errors";
    public static final String STATE_COPYING = "copying";

    // Member Variables
    protected String id;
    protected String name;
    protected String sourcePath;
    protected String state;
    protected String imageType;
    protected String username;
    protected String password;
    protected Date createdDate;
    protected String createdBy;
    protected String razorName;
    protected Boolean inUse;

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name;}
    public String getSourcePath() { return sourcePath;}
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getState() {
        return state;
    }
    public void setState(String md5Hash) {
        this.state = md5Hash;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Date getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public String getRazorName() { return razorName; }
    public void setRazorName(String razorName) { this.razorName = razorName; }
    public Boolean getInUse() { return this.inUse; }
    public void setInUse(Boolean inUse) { this.inUse = inUse; }

}
