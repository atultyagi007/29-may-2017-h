/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.firmware;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.wordnik.swagger.annotations.ApiModel;

@ApiModel()
@XmlRootElement(name = "FirmwareRepository")
public class FirmwareRepository {

    public final static String ASM_REPOSITORY_LOCATION = "asm_repo_location";
    public final static String DISK = "disk";

    protected String id;
    protected String name;
    protected String sourceLocation;
    protected String sourceType;
    protected String diskLocation;
    protected String filename;
    protected String md5Hash;
    protected String username;
    protected String password;
    protected RepositoryStatus downloadStatus;
    protected Date createdDate;
    protected String createdBy;
    protected Date updatedDate;
    protected String updatedBy;
    protected boolean defaultCatalog;
    protected boolean embedded;
    protected RepositoryState state;
    protected Set<SoftwareComponent> softwareComponents;
    protected Set<SoftwareBundle> softwareBundles;
    protected Set<Deployment> deployments;
    protected int bundleCount;
    protected int componentCount;
    protected int userBundleCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDiskLocation() {
        return diskLocation;
    }

    public void setDiskLocation(String diskLocation) {
        this.diskLocation = diskLocation;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RepositoryStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(RepositoryStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
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

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<SoftwareComponent> getSoftwareComponents() {
        if (this.softwareComponents == null)
            this.softwareComponents = new HashSet<>();
        return softwareComponents;
    }

    public void setSoftwareComponents(Set<SoftwareComponent> softwareComponents) {
        this.softwareComponents = softwareComponents;
    }

    public Set<SoftwareBundle> getSoftwareBundles() {
        if (this.softwareBundles == null)
            this.softwareBundles = new HashSet<>();
        return softwareBundles;
    }

    public void setSoftwareBundles(Set<SoftwareBundle> softwareBundles) {
        this.softwareBundles = softwareBundles;
    }

    public Set<Deployment> getDeployments() {
        if (deployments == null)
            this.deployments = new HashSet<>();
        return deployments;
    }

    public void setDeployments(Set<Deployment> deployments) {
        this.deployments = deployments;
    }

    public int getBundleCount() {
        return bundleCount;
    }

    public void setBundleCount(int bundleCount) {
        this.bundleCount = bundleCount;
    }

    public int getUserBundleCount() {
        return userBundleCount;
    }

    public void setUserBundleCount(int bundleCount) {
        this.userBundleCount = bundleCount;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(int componentCount) {
        this.componentCount = componentCount;
    }

    public boolean isDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(boolean defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public RepositoryState getState() {
        return state;
    }

    public void setState(RepositoryState state) {
        this.state = state;
    }
}
