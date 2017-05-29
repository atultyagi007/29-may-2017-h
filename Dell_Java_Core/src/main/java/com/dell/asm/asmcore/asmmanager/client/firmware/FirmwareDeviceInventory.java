package com.dell.asm.asmcore.asmmanager.client.firmware;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceComponent;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryResult;
import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "FirmwareDeviceInventory")
@ApiModel()
public class FirmwareDeviceInventory {
	
	    private String id;
	    private String version;
	    private String ipaddress;
	    private String servicetag;
	    private String parent_job_id;
	    private String jobId;
	    private String name;
	    private Date lastUpdateTime;
	    private String fqdd;
	    private String componentID;
	    private String componentType;
	    private String deviceID;
	    private String vendorID;
	    private String subdeviceID;
	    private String subvendorID;	    
		private ManagedDevice deviceInventory;			
		private DiscoveryResult discoveryResult;	
		private String systemId;
	    private FirmwareComplianceComponent firmwareComplianceComponents;
		private String operatingSystem;
		private SourceType sourceType;

    public FirmwareComplianceComponent getFirmwareComplianceComponents() {
        return firmwareComplianceComponents;
    }

    public void setFirmwareComplianceComponents(FirmwareComplianceComponent firmwareComplianceComponents) {
        this.firmwareComplianceComponents = firmwareComplianceComponents;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getServicetag() {
        return servicetag;
    }

    public void setServicetag(String servicetag) {
        this.servicetag = servicetag;
    }

    public String getParent_job_id() {
        return parent_job_id;
    }

    public void setParent_job_id(String parent_job_id) {
        this.parent_job_id = parent_job_id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getFqdd() {
        return fqdd;
    }

    public void setFqdd(String fqdd) {
        this.fqdd = fqdd;
    }

    public String getComponentID() {
        return componentID;
    }

    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getVendorID() {
        return vendorID;
    }

    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }

    public String getSubdeviceID() {
        return subdeviceID;
    }

    public void setSubdeviceID(String subdeviceID) {
        this.subdeviceID = subdeviceID;
    }

    public String getSubvendorID() {
        return subvendorID;
    }

    public void setSubvendorID(String subvendorID) {
        this.subvendorID = subvendorID;
    }

    public ManagedDevice getDeviceInventory() {
        return deviceInventory;
    }

    public void setDeviceInventory(ManagedDevice deviceInventory) {
        this.deviceInventory = deviceInventory;
    }

    public DiscoveryResult getDiscoveryResult() {
        return discoveryResult;
    }

    public void setDiscoveryResult(DiscoveryResult discoveryResult) {
        this.discoveryResult = discoveryResult;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }	
    
    /**
     * Quick and dirty equals test, that tests the fields that match between the software_component table and the 
     * firmware_deviceinventory table, primarily to ensure uniqueness in a Set.  This cannot be a complete test as it
     * is using FirmwareDeviceInventory created from Devices and FirmwareDeviceInventory created from a Catalog.  If
     * a true equals is ever needed, to test ALL fields, then move this logic into the IDracServer's 
     * getFirmwareDeviceInventory method.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FirmwareDeviceInventory == false){
          return false;
        }
        if (this == obj) {
           return true;
        }
        
        final FirmwareDeviceInventory firmDevInv = (FirmwareDeviceInventory)obj;

        return new EqualsBuilder()
           .append(this.getId(), firmDevInv.getId())
           .append(this.getComponentID(), firmDevInv.getComponentID())
           .append(this.getComponentType(), firmDevInv.getComponentType())
           .append(this.getVendorID(), firmDevInv.getVendorID())
           .append(this.getVersion(), firmDevInv.getVersion())
           .append(this.getServicetag(), firmDevInv.getServicetag())
           .append(this.getIpaddress(), firmDevInv.getIpaddress())
           .append(this.getOperatingSystem(), firmDevInv.getOperatingSystem())
           .isEquals();
    }

    @Override
    public int hashCode() {
       return new HashCodeBuilder()
          .append(this.getId())
          .append(this.getComponentID())
          .append(this.getComponentType())
          .append(this.getVendorID())
          .append(this.getVersion())
          .append(this.getServicetag())
          .append(this.getIpaddress())
          .append(this.getOperatingSystem())
          .toHashCode();
    }
}
