package com.dell.asm.asmcore.asmmanager.client.firmware;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "SoftwareComponent")
@ApiModel()
public class SoftwareComponent {
	
	private String id;
    private String packageId;
	private String dellVersion;	
	private String vendorVersion;	
	private String componentId;	
	private String deviceId;	
	private String subDeviceId;	
	private String vendorId;	
	private String subVendorId;
    protected Date createdDate;
    protected String createdBy;
    protected Date updatedDate;
    protected String updatedBy;
    protected String path;
    protected String hashMd5;
    protected String name;
    protected String category;
    protected String componentType;
    private String operatingSystem;
    
    protected List<String> systemIDs;

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getDellVersion() {
		return dellVersion;
	}
	public void setDellVersion(String dellVersion) {
		this.dellVersion = dellVersion;
	}
	public String getVendorVersion() {
		return vendorVersion;
	}
	public void setVendorVersion(String vendorVersion) {
		this.vendorVersion = vendorVersion;
	}
	public String getComponentId() {
		return componentId;
	}
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getSubDeviceId() {
		return subDeviceId;
	}
	public void setSubDeviceId(String subDeviceId) {
		this.subDeviceId = subDeviceId;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getSubVendorId() {
		return subVendorId;
	}
	public void setSubVendorId(String subVendorId) {
		this.subVendorId = subVendorId;
	}
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
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

    public String getHashMd5() {
        return hashMd5;
    }

    public void setHashMd5(String hashMd5) {
        this.hashMd5 = hashMd5;
    }

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getComponentType() {
		return componentType;
	}
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public List<String> getSystemIDs() {
		if (this.systemIDs == null)
			this.systemIDs = new ArrayList<String>();
		return systemIDs;
	}

	public void setSystemIDs(List<String> systemIDs) {
		this.systemIDs = systemIDs;
	}

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

}
