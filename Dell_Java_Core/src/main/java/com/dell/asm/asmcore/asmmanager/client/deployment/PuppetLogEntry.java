/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.id.GUIDGenerator;

@XmlRootElement(name = "puppetLogEntry")
public class PuppetLogEntry {

    @XmlElement(name = "level")
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @XmlElement(name = "message")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "time")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getId() {
        return id;
    }
    public void setId() {
        UUID guidId = UUID.randomUUID();
        this.id = guidId.toString();
    }

    public String getCategory() {
        return category;
    }

    public String getUser() {
        return user;
    }

    private String description;
    private String date;
    private List<String> tags;
    private String source;
    private String severity;
    private String file;
    private String line;
    private String id;
    private String category = "DEPLOYMENT";
    private String user = "Admin";

    @Override
    public String toString() {
        return "Date: " + getDate() + "; Category: " + getCategory() + "; Message: " + getDescription();
    }
}
