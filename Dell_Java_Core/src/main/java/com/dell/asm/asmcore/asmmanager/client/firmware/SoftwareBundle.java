package com.dell.asm.asmcore.asmmanager.client.firmware;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.rest.common.model.Link;
import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "SoftwareBundle")
@ApiModel()
public class SoftwareBundle {
	
    private String id;
    private String name;
    private String version;
    private Date bundleDate;
    protected Date createdDate;
    protected String createdBy;
    protected Date updatedDate;
    protected String updatedBy;
    protected String description;
    protected boolean userBundle;
    protected String userBundlePath;
    protected String userBundleHashMd5;
    protected String deviceType;
    protected String deviceModel;
    protected String criticality;
    protected String fwRepositoryId;
    private Link link;
    private BundleType bundleType;
    
    /**
     * Default constructor for the class.
     */
    public SoftwareBundle() {
        super();
    }
   
    public boolean getUserBundle() {
        return userBundle;
    }

    public void setUserBundle(boolean userBundle) {
        this.userBundle = userBundle;
    }

    public String getUserBundlePath() {
        return userBundlePath;
    }

    public void setUserBundlePath(String path) {
        this.userBundlePath = path;
    }

    public String getUserBundleHashMd5() {
        return userBundleHashMd5;
    }

    public void setUserBundleHashMd5(String hashMd5) {
        this.userBundleHashMd5 = hashMd5;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public String getFwRepositoryId() {
        return fwRepositoryId;
    }

    public void setFwRepositoryId(String fwRepositoryId) {
        this.fwRepositoryId = fwRepositoryId;
    }

    protected Set<SoftwareComponent> softwareComponents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getBundleDate() {
        return bundleDate;
    }

    public void setBundleDate(Date bundleDate) {
        this.bundleDate = bundleDate;
    }

    public Set<SoftwareComponent> getSoftwareComponents() {
        if (this.softwareComponents == null)
            this.softwareComponents = new HashSet<SoftwareComponent>();
        return softwareComponents;
    }

    public void setSoftwareComponents(Set<SoftwareComponent> softwareComponents) {
        this.softwareComponents = softwareComponents;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public BundleType getBundleType() {
        return bundleType;
    }

    public void setBundleType(BundleType bundleType) {
        this.bundleType = bundleType;
    }
}
