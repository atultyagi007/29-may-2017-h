package com.dell.asm.asmcore.asmmanager.util.discovery;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;

public class InfrastructureDevice {

    DiscoverDeviceType deviceType = DiscoverDeviceType.UNKNOWN;
    boolean fromInventoryJob = false;
    boolean fromInitialConfigJob = false;
    boolean quickDiscovery = false;
    DeviceType requestedDeviceType;
	private String ipAddress;
    private String existingRefId;
    private boolean unmanaged = false;
    private String errorMessage;
    private String discoveryResponse;
    private String config;
    private String parentJob;
    private DiscoverDeviceType parentDeviceType;
    private String parentDeviceId;
    private String user;
    private String password;
    private String chassisCredentialId;
    private String serverCredentialId;
    private String switchCredentiallId;
    private String vCenterCredentialId;
    private String storageCredentialId;
    private String scvmmCredentialId;
    private String emCredentialId;

    public InfrastructureDevice(String ip, String chassisCredentialId, String serverCredentialId,
                                String switchCredentiallId, String vCenterCredentialId,
                                String storageCredentialId, String scvmmCredentialId,
                                String emCredentialId) {
        this.ipAddress = ip;
        this.chassisCredentialId = chassisCredentialId;
        this.serverCredentialId = serverCredentialId;
        this.switchCredentiallId = switchCredentiallId;
        this.vCenterCredentialId = vCenterCredentialId;
        this.storageCredentialId = storageCredentialId;
        this.scvmmCredentialId = scvmmCredentialId;
        this.emCredentialId = emCredentialId;
    }

    public InfrastructureDevice() {
        // TODO Auto-generated constructor stub
    }

    public DiscoverDeviceType getDeviceType() { return deviceType; }

    public void setDeviceType(DiscoverDeviceType dt) { this.deviceType = dt;  }

    public void setFromInventoryJob(boolean fromInventoryJob) { this.fromInventoryJob = fromInventoryJob; }

    public boolean isFromInventoryJob() { return fromInventoryJob; }

    public boolean isFromInitialConfigJob() {
        return fromInitialConfigJob;
    }

    public void setFromInitialConfigJob(boolean fromInitialConfigJob) { this.fromInitialConfigJob = fromInitialConfigJob; }

    public boolean isQuickDiscovery() {
        return quickDiscovery;
    }

    public void setQuickDiscovery(boolean quickDiscovery) {
        this.quickDiscovery = quickDiscovery;
    }

    public String getIpAddress() { return ipAddress; }

    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getExistingRefId() { return existingRefId; }

    public void setExistingRefId(String existingRefId) { this.existingRefId = existingRefId; }

    public boolean isUnmanaged() { return unmanaged;}

    public void setUnmanaged(boolean unmanaged) { this.unmanaged = unmanaged; }

    public DeviceType getRequestedDeviceType() {
        return requestedDeviceType;
    }

    public void setRequestedDeviceType(DeviceType requestedDeviceType) { this.requestedDeviceType = requestedDeviceType; }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDiscoveryResponse() { return discoveryResponse; }

    public void setDiscoveryResponse(String discoveryResponse) { this.discoveryResponse = discoveryResponse; }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getParentJob() {
        return parentJob;
    }

    public void setParentJob(String parentJob) { this.parentJob = parentJob; }

    public void setParentDeviceType(DiscoverDeviceType parentDeviceType) { this.parentDeviceType = parentDeviceType; }

    public DiscoverDeviceType getParentDeviceType() { return parentDeviceType; }

    public String getParentDeviceId() { return parentDeviceId; }

    public void setParentDeviceId(String parentDeviceId) { this.parentDeviceId = parentDeviceId; }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    private boolean reserved = false;

    public String getServerPoolId() {
        return serverPoolId;
    }

    public void setServerPoolId(String serverPoolId) {
        this.serverPoolId = serverPoolId;
    }

    private String serverPoolId;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public String getChassisCredentialId() {
        return chassisCredentialId;
    }

    public void setChassisCredentialId(String chassisCredentialId) {
        this.chassisCredentialId = chassisCredentialId;
    }

    public String getServerCredentialId() {
        return serverCredentialId;
    }

    public void setServerCredentialId(String serverCredentialId) {
        this.serverCredentialId = serverCredentialId;
    }

    public String getSwitchCredentiallId() {
        return switchCredentiallId;
    }

    public void setSwitchCredentiallId(String switchCredentiallId) {
        this.switchCredentiallId = switchCredentiallId;
    }

    public String getvCenterCredentialId() {
        return vCenterCredentialId;
    }

    public void setvCenterCredentialId(String vCenterCredentialId) {
        this.vCenterCredentialId = vCenterCredentialId;
    }

    public String getStorageCredentialId() {
        return storageCredentialId;
    }

    public void setStorageCredentialId(String storageCredentialId) {
        this.storageCredentialId = storageCredentialId;
    }
    public String getScvmmCredentialId() {
        return scvmmCredentialId;
    }

    public void setScvmmCredentialId(String scvmmCredentialId) {
        this.scvmmCredentialId = scvmmCredentialId;
    }

    public String getEmCredentialId() { return emCredentialId; }

    public void setEmCredentialId(String emCredentialId) { this.emCredentialId = emCredentialId; }

  
}
