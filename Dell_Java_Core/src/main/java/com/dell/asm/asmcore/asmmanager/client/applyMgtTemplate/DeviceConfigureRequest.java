package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.rest.common.model.Link;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@XmlRootElement(name = "DeviceConfigureRequest")
@ApiModel(value="Device configure Request",
description="Configures the devices with the given device guid and device Identity refs")
public class DeviceConfigureRequest {

    @ApiModelProperty(value="Template Guid", required=true)
    String templateGuid;

    @ApiModelProperty(value="deviceType", required=true)
    DeviceType deviceType;

    @ApiModelProperty(value="device Indentity", required=true)
    //@XmlElement(required=true, name="DeviceIdentity")
    Set<DeviceIdentity> deviceIdentities;
    
    @ApiModelProperty(value="Request ID",required=true)
    private String id;

    @ApiModelProperty(value="Status")
    private ConfigureStatus jobStatus;

    @ApiModelProperty(value="Status Message")
    private String jobStatusMessage;

    
    @ApiModelProperty(value="Manage Device List")
    private List<ManagedDevice> devices;   
    
    private GregorianCalendar createdDate;
    private String createdBy;
    private GregorianCalendar updatedDate;
    private String updatedBy;
    
    private Link link;

    public DeviceConfigureRequest() {   }
    
    public DeviceConfigureRequest(Set<DeviceIdentity> reqs) {
        for (DeviceIdentity req : reqs) {
            getDeviceIndentities().add(new DeviceIdentity(req));
        }
    }
    
    public Set<DeviceIdentity> getDeviceIndentities() {
        if (deviceIdentities == null)
            deviceIdentities = new HashSet<DeviceIdentity>();
        return deviceIdentities;
    }

    public String getTemplateGuid() {
        return templateGuid;
    }

    public void setTemplateGuid(String templateGuid) {
        this.templateGuid = templateGuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConfigureStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(ConfigureStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobStatusMessage() {
        return jobStatusMessage;
    }

    public void setJobStatusMessage(String jobStatusMessage) {
        this.jobStatusMessage = jobStatusMessage;
    }

    public List<ManagedDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<ManagedDevice> devices) {
        this.devices = devices;
    }

    public Set<DeviceIdentity> getDeviceIdentities() {
        return deviceIdentities;
    }

    public void setDeviceIdentities(Set<DeviceIdentity> deviceIdentities) {
        this.deviceIdentities = deviceIdentities;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public GregorianCalendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GregorianCalendar getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }


}
