/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an EqualLogic Device from the Puppet Inventory.
 */
public class PuppetEquallogicDevice {

    // Member Variables
    private String certName;
    private String deviceType;
    private String updateTime;
    private String groupName;
    private String managementIp;
    private String memberModel;
    private String status;
    private String version;
    private GeneralSettings generalSettings;
    private String members;
    private Collections collections;
    private String model;
    private GroupDiskSpace groupDiskSpace;
    private String freeGroupSpace;
    private List<VolumeIqnInformation> volumeIqnInformations;
    private VolumesInfo volumesInfo;
    private String volumes;
    private SnapshotInfo snapshotInfo;
    private String name;
    private String fwVersion;
    private List<VolumeProperties> volumesProperties;
    private String snapshots;

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    private String groupMembers;

    public void setStoragePools(List<StoragePool> storagePools) {
        this.storagePools = storagePools;
    }

    public List<StoragePool> getStoragePools() {
        return storagePools;
    }

    private List<StoragePool> storagePools = new ArrayList<>();

    // Default Constructor
    public PuppetEquallogicDevice() {
    }

    public String getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(String snapshots) {
        this.snapshots = snapshots;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(String managementIp) {
        this.managementIp = managementIp;
    }

    public String getMemberModel() {
        return memberModel;
    }

    public void setMemberModel(String memberModel) {
        this.memberModel = memberModel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(GeneralSettings generalSettings) {
        this.generalSettings = generalSettings;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public Collections getCollections() {
        return collections;
    }

    public void setCollections(Collections collections) {
        this.collections = collections;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public GroupDiskSpace getGroupDiskSpace() {
        return groupDiskSpace;
    }

    public void setGroupDiskSpace(GroupDiskSpace groupDiskSpace) {
        this.groupDiskSpace = groupDiskSpace;
    }

    public String getFreeGroupSpace() {
        return freeGroupSpace;
    }

    public void setFreeGroupSpace(String freeGroupSpace) {
        this.freeGroupSpace = freeGroupSpace;
    }

    public List<VolumeIqnInformation> getVolumeIqnInformation() {
        return volumeIqnInformations;
    }

    public void setVolumeIqnInformations(List<VolumeIqnInformation> volumeIqnInformations) {
        this.volumeIqnInformations = volumeIqnInformations;
    }

    public VolumesInfo getVolumesInfo() {
        return volumesInfo;
    }

    public void setVolumesInfo(VolumesInfo volumesInfo) {
        this.volumesInfo = volumesInfo;
    }

    public String getVolumes() {
        return volumes;
    }

    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }

    public SnapshotInfo getSnapshotInfo() {
        return snapshotInfo;
    }

    public void setSnapshotInfo(SnapshotInfo snapshotInfo) {
        this.snapshotInfo = snapshotInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public List<VolumeProperties> getVolumesProperties() {
        return volumesProperties;
    }

    public void setVolumesProperties(List<VolumeProperties> volumesProperties) {
        this.volumesProperties = volumesProperties;
    }

    public static class GeneralSettings {
        private String groupName;
        private String ipAddress;
        private String location;

        // Default Constructor
        public GeneralSettings() {
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class Collections {
        private String volumeCollections;
        private String customSnapshotCollections;
        private String snapshotCollections;

        // Default Constructor
        public Collections() {
        }

        public String getVolumeCollections() {
            return volumeCollections;
        }

        public void setVolumeCollections(String volumeCollections) {
            this.volumeCollections = volumeCollections;
        }

        public String getCustomSnapshotCollections() {
            return customSnapshotCollections;
        }

        public void setCustomSnapshotCollections(String customSnapshotCollections) {
            this.customSnapshotCollections = customSnapshotCollections;
        }

        public String getSnapshotCollections() {
            return snapshotCollections;
        }

        public void setSnapshotCollections(String snapshotCollections) {
            this.snapshotCollections = snapshotCollections;
        }
    }

    public static class GroupDiskSpace {
        private String snapshotReserve;
        private String replicationReserve;
        private String delegated;
        private String free;
        private String volumeReserve;

        // Default Constructor
        public GroupDiskSpace() {
        }

        public String getSnapshotReserve() {
            return snapshotReserve;
        }

        public void setSnapshotReserve(String snapshotReserve) {
            this.snapshotReserve = snapshotReserve;
        }

        public String getReplicationReserve() {
            return replicationReserve;
        }

        public void setReplicationReserve(String replicationReserve) {
            this.replicationReserve = replicationReserve;
        }

        public String getDelegated() {
            return delegated;
        }

        public void setDelegated(String delegated) {
            this.delegated = delegated;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }

        public String getVolumeReserve() {
            return volumeReserve;
        }

        public void setVolumeReserve(String volumeReserve) {
            this.volumeReserve = volumeReserve;
        }
    }

    public static class VolumesInfo {
        private String totalVolumes;
        private String online;
        private String iscsiConnections;
        private String inUse;

        // Default Constructor
        public VolumesInfo() {
        }

        public String getTotalVolumes() {
            return totalVolumes;
        }

        public void setTotalVolumes(String totalVolumes) {
            this.totalVolumes = totalVolumes;
        }

        public String getOnline() {
            return online;
        }

        public void setOnline(String online) {
            this.online = online;
        }

        public String getIscsiConnections() {
            return iscsiConnections;
        }

        public void setIscsiConnections(String iscsiConnections) {
            this.iscsiConnections = iscsiConnections;
        }

        public String getInUse() {
            return inUse;
        }

        public void setInUse(String inUse) {
            this.inUse = inUse;
        }
    }

    public static class SnapshotInfo {
        private String online;
        private String inUse;

        // Default Constructor
        public SnapshotInfo() {
        }

        public String getOnline() {
            return online;
        }

        public void setOnline(String online) {
            this.online = online;
        }

        public String getInUse() {
            return inUse;
        }

        public void setInUse(String inUse) {
            this.inUse = inUse;
        }
    }

    public static class VolumeProperties {
        private String name;
        private String storagePool;
        private String reportedSize;
        private String volumeReserve;
        private String snapshotReserve;
        private String borrowedSpace;
        private String volumeStatus;
        private String volumeUse;
        private String replicationPartner;
        private String syncrepStatus;
        private String numberOfSnapshots;
        private String numberOfOnlineSnapshots;
        private String iscsiConnections;
        private String snapshotsInUse;
        private String targetIscsiName;

        // Default Constructor
        public VolumeProperties() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStoragePool() {
            return storagePool;
        }

        public void setStoragePool(String storagePool) {
            this.storagePool = storagePool;
        }

        public String getReportedSize() {
            return reportedSize;
        }

        public void setReportedSize(String reportedSize) {
            this.reportedSize = reportedSize;
        }

        public String getVolumeReserve() {
            return volumeReserve;
        }

        public void setVolumeReserve(String volumeReserve) {
            this.volumeReserve = volumeReserve;
        }

        public String getSnapshotReserve() {
            return snapshotReserve;
        }

        public void setSnapshotReserve(String snapshotReserve) {
            this.snapshotReserve = snapshotReserve;
        }

        public String getBorrowedSpace() {
            return borrowedSpace;
        }

        public void setBorrowedSpace(String borrowedSpace) {
            this.borrowedSpace = borrowedSpace;
        }

        public String getVolumeStatus() {
            return volumeStatus;
        }

        public void setVolumeStatus(String volumeStatus) {
            this.volumeStatus = volumeStatus;
        }

        public String getVolumeUse() {
            return volumeUse;
        }

        public void setVolumeUse(String volumeUse) {
            this.volumeUse = volumeUse;
        }

        public String getReplicationPartner() {
            return replicationPartner;
        }

        public void setReplicationPartner(String replicationPartner) {
            this.replicationPartner = replicationPartner;
        }

        public String getSyncrepStatus() {
            return syncrepStatus;
        }

        public void setSyncrepStatus(String syncrepStatus) {
            this.syncrepStatus = syncrepStatus;
        }

        public String getNumberOfSnapshots() {
            return numberOfSnapshots;
        }

        public void setNumberOfSnapshots(String numberOfSnapshots) {
            this.numberOfSnapshots = numberOfSnapshots;
        }

        public String getNumberOfOnlineSnapshots() {
            return numberOfOnlineSnapshots;
        }

        public void setNumberOfOnlineSnapshots(String numberOfOnlineSnapshots) {
            this.numberOfOnlineSnapshots = numberOfOnlineSnapshots;
        }

        public String getIscsiConnections() {
            return iscsiConnections;
        }

        public void setIscsiConnections(String iscsiConnections) {
            this.iscsiConnections = iscsiConnections;
        }

        public String getSnapshotsInUse() {
            return snapshotsInUse;
        }

        public void setSnapshotsInUse(String snapshotsInUse) {
            this.snapshotsInUse = snapshotsInUse;
        }

        public String getTargetIscsiName() {
            return targetIscsiName;
        }

        public void setTargetIscsiName(String targetIscsiName) {
            this.targetIscsiName = targetIscsiName;
        }
    }

    public static class VolumeIqnInformation {

        private String targetIscsiName;
        private String size;
        private String snapshots;
        private String status;
        private String permissions;
        private String tp;
        private String template;

        // Default Constructor
        public VolumeIqnInformation() {
        }

        public String getTargetIscsiName() {
            return targetIscsiName;
        }

        public void setTargetIscsiName(String targetIscsiName) {
            this.targetIscsiName = targetIscsiName;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPermissions() {
            return permissions;
        }

        public void setPermissions(String permissions) {
            this.permissions = permissions;
        }

        public String getTp() {
            return tp;
        }

        public void setTp(String tp) {
            this.tp = tp;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }
    }

    public static class StoragePool {
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getMembers() {
            return members;
        }

        public void setMembers(String members) {
            this.members = members;
        }

        private String id;
        private String name;
        private String size;
        private String members;
    }

    /**
     * Returns the Volume Properties for the given IQN information or null if it is not found.
     * 
     * @param iscsiIqn used to identify the volume properties.
     * @return the Volume Properties for the given IQN information or null if it is not found.
     */
    public PuppetEquallogicDevice.VolumeProperties getVolumePropertiesByIscsiIqn(String iscsiIqn) {
        PuppetEquallogicDevice.VolumeProperties volumeProperties = null;

        if (this.getVolumesProperties() != null && iscsiIqn != null) {
            for (PuppetEquallogicDevice.VolumeProperties volumeProps : this
                    .getVolumesProperties()) {
                if (volumeProps.getTargetIscsiName() != null
                        && volumeProps.getTargetIscsiName().equals(iscsiIqn)) {
                    volumeProperties = volumeProps;
                    break;
                }
            }
        }

        return volumeProperties;
    }

    /**
     * True if volume was deleted but still in recovery bin
     * @param volumeProp
     * @return
     */
    public static boolean isVolumeInRecoveryBin(PuppetEquallogicDevice.VolumeProperties volumeProp) {
        return (volumeProp != null && "0.0GB".equals(volumeProp.getVolumeReserve()) &&
                !PuppetEqualLogicKeys.STATUS_ONLINE.equals(volumeProp.getVolumeStatus()));
    }
}
