package com.dell.asm.asmcore.asmmanager.client.deployment;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;

/**
 * This is a Device that has been deployed as a Service in ASM.  A device may be of any type from server, to storage, 
 * and so forth.  While not referenced by this class, take a look at the DeviceType to get an idea of the types of 
 * Devices that are supported in ASM.<br>
 * <br>
 * A DeploymentDevice references a com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice through the 
 * refId. <br>
 * <br>
 * @see com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType
 */
public class DeploymentDevice {

    private String refId;

    private String refType;

    private String logDump;

    private DeploymentStatusType status;

    private String statusEndTime;

    private String statusStartTime;

    private DeviceHealth deviceHealth;

    private String healthMessage;

    private CompliantState compliantState;

    private boolean isBrownfield;

    private BrownfieldStatus brownfieldStatus = BrownfieldStatus.NOT_APPLICABLE;

    // to optimize performance on "getDeployment" call we keep few inventory related items here
    private DeviceType deviceType;

    private String ipAddress;

    private String serviceTag;

    private String componentId;

    private String statusMessage;

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getStatusEndTime() {
        return statusEndTime;
    }

    public void setStatusEndTime(String statusEndTime) {
        this.statusEndTime = statusEndTime;
    }

    public String getStatusStartTime() {
        return statusStartTime;
    }

    public void setStatusStartTime(String statusStartTime) {
        this.statusStartTime = statusStartTime;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public DeploymentStatusType getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatusType status) {
        this.status = status;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getLogDump() {
        return logDump;
    }

    public void setLogDump(String logDump) {
        this.logDump = logDump;
    }

    public DeviceHealth getDeviceHealth() {
        return deviceHealth;
    }

    public void setDeviceHealth(DeviceHealth deviceHealth) {
        this.deviceHealth = deviceHealth;
    }

    public String getHealthMessage() {
        return healthMessage;
    }

    public void setHealthMessage(String healthMessage) {
        this.healthMessage = healthMessage;
    }

    public CompliantState getCompliantState() {
        return compliantState;
    }

    public void setCompliantState(CompliantState compliantState) {
        this.compliantState = compliantState;
    }

    public boolean isBrownfield() {
        return this.isBrownfield;
    }

    public void setBrownfield(boolean brownfield) {
        this.isBrownfield = brownfield;
    }

    /**
     * Returns the BrownfieldStatus for the DeployedDevice.  Will return BrownfieldStatus.NOT_APPLICABLE if not 
     * explicitly set.
     *
     * @return the BrownfieldStatus for the DeployedDevice.  Will return BrownfieldStatus.NOT_APPLICABLE if not 
     *      explicitly set.
     */
    public BrownfieldStatus getBrownfieldStatus() {
        return brownfieldStatus;
    }

    /**
     * Sets the state of the BrownfieldStatus.  Primarily used in Brownfield flows and not applicable in any other 
     * scenario.
     *
     * @param brownfieldStatus the new BrownfieldStatus for the DeployedDevice.  
     */
    public void setBrownfieldStatus(BrownfieldStatus brownfieldStatus) {
        this.brownfieldStatus = brownfieldStatus;
    }

    /**
     * Simple equals test that looks to see if the refId or the service tag is the same.  If either are the same, then
     * it's considered the same device.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeploymentDevice) {
            DeploymentDevice dd = (DeploymentDevice)obj;
            if (this.getRefId() != null && this.getRefId().equals(dd.getRefId())) {
                return true;
            }
            if (this.getServiceTag() != null && this.getServiceTag().equals(dd.getServiceTag())) {
                return true;
            }
            if (this.getComponentId() != null && this.getComponentId().equals(dd.getComponentId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
       return new HashCodeBuilder()
          .append(this.getRefId())
          .append(this.getServiceTag())
          .append(this.getComponentId())
          .toHashCode();
    }
}

