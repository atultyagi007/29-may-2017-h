/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.util.List;

/**
 * Represents a Compellent device from the Puppet Inventory.
 */
public class PuppetCompellentDevice {

    // Member Variables
    private String certName;
    private String deviceType;
    private String updateTime;
    private String systemSerialNumber;
    private String systemName;
    private String systemManagementIp;
    private String systemVersion;
    private String systemOperationMode;
    private String systemPortsBalanced;
    private String systemMailServer;
    private String systemBackupMailServer;
    private SystemData systemData;
    private List<DiskFolder> diskFolders;
    private List<Volume> volumes; // volume_data
    private List<Server> servers; // server_data
    private List<ReplayProfile> replayProfiles; // replayprofile_data
    private List<StorageProfile> storageProfiles; // storageprofile_data
    private String model;
    private String name;

    // Default constructor for the class
    public PuppetCompellentDevice() {
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

    public String getSystemSerialNumber() {
        return systemSerialNumber;
    }

    public void setSystemSerialNumber(String systemSerialNumber) {
        this.systemSerialNumber = systemSerialNumber;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemManagementIp() {
        return systemManagementIp;
    }

    public void setSystemManagementIp(String systemManagementIp) {
        this.systemManagementIp = systemManagementIp;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getSystemOperationMode() {
        return systemOperationMode;
    }

    public void setSystemOperationMode(String systemOperationMode) {
        this.systemOperationMode = systemOperationMode;
    }

    public String getSystemPortsBalanced() {
        return systemPortsBalanced;
    }

    public void setSystemPortsBalanced(String systemPortsBalanced) {
        this.systemPortsBalanced = systemPortsBalanced;
    }

    public String getSystemMailServer() {
        return systemMailServer;
    }

    public void setSystemMailServer(String systemMailServer) {
        this.systemMailServer = systemMailServer;
    }

    public String getSystemBackupMailServer() {
        return systemBackupMailServer;
    }

    public void setSystemBackupMailServer(String systemBackupMailServer) {
        this.systemBackupMailServer = systemBackupMailServer;
    }

    public SystemData getSystemData() {
        return systemData;
    }

    public void setSystemData(SystemData systemData) {
        this.systemData = systemData;
    }

    public List<DiskFolder> getDiskFolders() {
        return diskFolders;
    }

    public void setDiskFolders(List<DiskFolder> diskFolders) {
        this.diskFolders = diskFolders;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public List<ReplayProfile> getReplayProfiles() {
        return replayProfiles;
    }

    public void setReplayProfiles(List<ReplayProfile> replayProfiles) {
        this.replayProfiles = replayProfiles;
    }

    public List<StorageProfile> getStorageProfiles() {
        return storageProfiles;
    }

    public void setStorageProfiles(List<StorageProfile> storageProfiles) {
        this.storageProfiles = storageProfiles;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class DiskFolder {

        // Member Variables
        private String index;
        private String name;
        private String numManaged;
        private String numSpare;
        private String numStorageTypes;
        private String totalAvailableSpace;
        private String availableSpaceBlocks;
        private String allocatedSpace;
        private String allocatedSpaceBlocks;
        private List<StorageType> storageTypes;

        public DiskFolder() {
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumManaged() {
            return numManaged;
        }

        public void setNumManaged(String numManaged) {
            this.numManaged = numManaged;
        }

        public String getNumSpare() {
            return numSpare;
        }

        public void setNumSpare(String numSpare) {
            this.numSpare = numSpare;
        }

        public String getNumStorageTypes() {
            return numStorageTypes;
        }

        public void setNumStorageTypes(String numStorageTypes) {
            this.numStorageTypes = numStorageTypes;
        }

        public String getTotalAvailableSpace() {
            return totalAvailableSpace;
        }

        public void setTotalAvailableSpace(String totalAvailableSpace) {
            this.totalAvailableSpace = totalAvailableSpace;
        }

        public String getAvailableSpaceBlocks() {
            return availableSpaceBlocks;
        }

        public void setAvailableSpaceBlocks(String availableSpaceBlocks) {
            this.availableSpaceBlocks = availableSpaceBlocks;
        }

        public String getAllocatedSpace() {
            return allocatedSpace;
        }

        public void setAllocatedSpace(String allocatedSpace) {
            this.allocatedSpace = allocatedSpace;
        }

        public String getAllocatedSpaceBlocks() {
            return allocatedSpaceBlocks;
        }

        public void setAllocatedSpaceBlocks(String allocatedSpaceBlocks) {
            this.allocatedSpaceBlocks = allocatedSpaceBlocks;
        }

        public List<StorageType> getStorageTypes() {
            return storageTypes;
        }

        public void setStorageTypes(List<StorageType> storageTypes) {
            this.storageTypes = storageTypes;
        }

        public static class StorageType {

            // Member Variables
            private String index;
            private String name;
            private String redundancy;
            private String pageSize;
            private String pageSizeBlocks;
            private String spaceUsed;
            private String spaceUsedBlocks;
            private String spaceAllocatted;
            private String spaceAllocatedBlocks;

            public StorageType() {
            }

            public String getIndex() {
                return index;
            }

            public void setIndex(String index) {
                this.index = index;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getRedundancy() {
                return redundancy;
            }

            public void setRedundancy(String redundancy) {
                this.redundancy = redundancy;
            }

            public String getPageSize() {
                return pageSize;
            }

            public void setPageSize(String pageSize) {
                this.pageSize = pageSize;
            }

            public String getPageSizeBlocks() {
                return pageSizeBlocks;
            }

            public void setPageSizeBlocks(String pageSizeBlocks) {
                this.pageSizeBlocks = pageSizeBlocks;
            }

            public String getSpaceUsed() {
                return spaceUsed;
            }

            public void setSpaceUsed(String spaceUsed) {
                this.spaceUsed = spaceUsed;
            }

            public String getSpaceUsedBlocks() {
                return spaceUsedBlocks;
            }

            public void setSpaceUsedBlocks(String spaceUsedBlocks) {
                this.spaceUsedBlocks = spaceUsedBlocks;
            }

            public String getSpaceAllocatted() {
                return spaceAllocatted;
            }

            public void setSpaceAllocatted(String spaceAllocatted) {
                this.spaceAllocatted = spaceAllocatted;
            }

            public String getSpaceAllocatedBlocks() {
                return spaceAllocatedBlocks;
            }

            public void setSpaceAllocatedBlocks(String spaceAllocatedBlocks) {
                this.spaceAllocatedBlocks = spaceAllocatedBlocks;
            }
        }

    }

    public static class SystemData {

        // Member Variables
        private String serialNumber;
        private String name;
        private String managementIp;
        private String version;
        private String operationMode;
        private String portsBalanced;
        private String mailServer;
        private String backupMailServer;

        public SystemData() {
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getManagementIp() {
            return managementIp;
        }

        public void setManagementIp(String managementIp) {
            this.managementIp = managementIp;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getOperationMode() {
            return operationMode;
        }

        public void setOperationMode(String operationMode) {
            this.operationMode = operationMode;
        }

        public String getPortsBalanced() {
            return portsBalanced;
        }

        public void setPortsBalanced(String portsBalanced) {
            this.portsBalanced = portsBalanced;
        }

        public String getMailServer() {
            return mailServer;
        }

        public void setMailServer(String mailServer) {
            this.mailServer = mailServer;
        }

        public String getBackupMailServer() {
            return backupMailServer;
        }

        public void setBackupMailServer(String backupMailServer) {
            this.backupMailServer = backupMailServer;
        }
    }

    public static class Volume {

        // Member Variables
        private String index;
        private String name;
        private String status;
        private String configSize;
        private String activeSize;
        private String replaySize;
        private String folder;
        private String storageProfile;
        private String deviceId;
        private String serialNumber;
        private String configSizeBlocks;
        private String activeSizeBlocks;
        private String replaySizeBlocks;
        private String maxWriteSizeBlocks;
        private String readCache;
        private String writeCache;
        private List<Mapping> mappings;

        public Volume() {
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getConfigSize() {
            return configSize;
        }

        public void setConfigSize(String configSize) {
            this.configSize = configSize;
        }

        public String getActiveSize() {
            return activeSize;
        }

        public void setActiveSize(String activeSize) {
            this.activeSize = activeSize;
        }

        public String getReplaySize() {
            return replaySize;
        }

        public void setReplaySize(String replaySize) {
            this.replaySize = replaySize;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getStorageProfile() {
            return storageProfile;
        }

        public void setStorageProfile(String storageProfile) {
            this.storageProfile = storageProfile;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getConfigSizeBlocks() {
            return configSizeBlocks;
        }

        public void setConfigSizeBlocks(String configSizeBlocks) {
            this.configSizeBlocks = configSizeBlocks;
        }

        public String getActiveSizeBlocks() {
            return activeSizeBlocks;
        }

        public void setActiveSizeBlocks(String activeSizeBlocks) {
            this.activeSizeBlocks = activeSizeBlocks;
        }

        public String getReplaySizeBlocks() {
            return replaySizeBlocks;
        }

        public void setReplaySizeBlocks(String replaySizeBlocks) {
            this.replaySizeBlocks = replaySizeBlocks;
        }

        public String getMaxWriteSizeBlocks() {
            return maxWriteSizeBlocks;
        }

        public void setMaxWriteSizeBlocks(String maxWriteSizeBlocks) {
            this.maxWriteSizeBlocks = maxWriteSizeBlocks;
        }

        public String getReadCache() {
            return readCache;
        }

        public void setReadCache(String readCache) {
            this.readCache = readCache;
        }

        public String getWriteCache() {
            return writeCache;
        }

        public void setWriteCache(String writeCache) {
            this.writeCache = writeCache;
        }

        public List<Mapping> getMappings() {
            return mappings;
        }

        public void setMappings(List<Mapping> mappings) {
            this.mappings = mappings;
        }

        public static class Mapping {

            // Member Variables
            private String index;
            private String server;
            private String localPort;
            private String remotePort;
            private String lun;

            public Mapping() {
            }

            public String getIndex() {
                return index;
            }

            public void setIndex(String index) {
                this.index = index;
            }

            public String getServer() {
                return server;
            }

            public void setServer(String server) {
                this.server = server;
            }

            public String getLocalPort() {
                return localPort;
            }

            public void setLocalPort(String localPort) {
                this.localPort = localPort;
            }

            public String getRemotePort() {
                return remotePort;
            }

            public void setRemotePort(String remotePort) {
                this.remotePort = remotePort;
            }

            public String getLun() {
                return lun;
            }

            public void setLun(String lun) {
                this.lun = lun;
            }

        }

    }

    public static class Server {

        // Member Variables
        private String index;
        private String name;
        private String folder;
        private String osIndex;
        private String os;
        private String type;
        private String parentIndex;
        private String parent;
        private String wwnList;
        private String folderIndex;
        private String connectionStatus;
        private String transportType;
        private List<Mapping> mappings;

        public Server() {
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getOsIndex() {
            return osIndex;
        }

        public void setOsIndex(String osIndex) {
            this.osIndex = osIndex;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getParentIndex() {
            return parentIndex;
        }

        public void setParentIndex(String parentIndex) {
            this.parentIndex = parentIndex;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public String getWwnList() {
            return wwnList;
        }

        public void setWwnList(String wwnList) {
            this.wwnList = wwnList;
        }

        public String getFolderIndex() {
            return folderIndex;
        }

        public void setFolderIndex(String folderIndex) {
            this.folderIndex = folderIndex;
        }

        public String getConnectionStatus() {
            return connectionStatus;
        }

        public void setConnectionStatus(String connectionStatus) {
            this.connectionStatus = connectionStatus;
        }

        public String getTransportType() {
            return transportType;
        }

        public void setTransportType(String transportType) {
            this.transportType = transportType;
        }

        public List<Mapping> getMappings() {
            return mappings;
        }

        public void setMappings(List<Mapping> mappings) {
            this.mappings = mappings;
        }

        public static class Mapping {

            // Member Variables
            private String index;
            private String volume;
            private String deviceId;
            private String serialNumber;
            private String localPort;
            private String remotePort;
            private String lun;

            public Mapping() {
            }

            public String getIndex() {
                return index;
            }

            public void setIndex(String index) {
                this.index = index;
            }

            public String getVolume() {
                return volume;
            }

            public void setVolume(String volume) {
                this.volume = volume;
            }

            public String getDeviceId() {
                return deviceId;
            }

            public void setDeviceId(String deviceId) {
                this.deviceId = deviceId;
            }

            public String getSerialNumber() {
                return serialNumber;
            }

            public void setSerialNumber(String serialNumber) {
                this.serialNumber = serialNumber;
            }

            public String getLocalPort() {
                return localPort;
            }

            public void setLocalPort(String localPort) {
                this.localPort = localPort;
            }

            public String getRemotePort() {
                return remotePort;
            }

            public void setRemotePort(String remotePort) {
                this.remotePort = remotePort;
            }

            public String getLun() {
                return lun;
            }

            public void setLun(String lun) {
                this.lun = lun;
            }
        }

    }

    public static class ReplayProfile {

        // Member Variables
        private String index;
        private String name;
        private String type;
        private String numRules;
        private String numVolumes;
        private String schedule;
        private List<Volume> volumes;

        public ReplayProfile() {
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getNumRules() {
            return numRules;
        }

        public void setNumRules(String numRules) {
            this.numRules = numRules;
        }

        public String getNumVolumes() {
            return numVolumes;
        }

        public void setNumVolumes(String numVolumes) {
            this.numVolumes = numVolumes;
        }

        public String getSchedule() {
            return schedule;
        }

        public void setSchedule(String schedule) {
            this.schedule = schedule;
        }

        public List<Volume> getVolumes() {
            return volumes;
        }

        public void setVolumes(List<Volume> volumes) {
            this.volumes = volumes;
        }

        public static class Volume {

            // Member Variables
            private String index;
            private String name;
            private String deviceId;
            private String serialNumber;

            public Volume() {
            }

            public String getIndex() {
                return index;
            }

            public void setIndex(String index) {
                this.index = index;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDeviceId() {
                return deviceId;
            }

            public void setDeviceId(String deviceId) {
                this.deviceId = deviceId;
            }

            public String getSerialNumber() {
                return serialNumber;
            }

            public void setSerialNumber(String serialNumber) {
                this.serialNumber = serialNumber;
            }
        }
    }

    public static class StorageProfile {

        // Member Variables
        private String index;
        private String name;
        private String numVolumes;
        private String redundantWriteable;
        private String redundantHistorical;
        private String nonRedundantWritable;
        private String nonRedundantHistorical;
        private String dualRedundantWritable;
        private String dualHistorical;
        private List<Volume> volumes;

        public StorageProfile() {
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumVolumes() {
            return numVolumes;
        }

        public void setNumVolumes(String numVolumes) {
            this.numVolumes = numVolumes;
        }

        public String getRedundantWriteable() {
            return redundantWriteable;
        }

        public void setRedundantWriteable(String redundantWriteable) {
            this.redundantWriteable = redundantWriteable;
        }

        public String getRedundantHistorical() {
            return redundantHistorical;
        }

        public void setRedundantHistorical(String redundantHistorical) {
            this.redundantHistorical = redundantHistorical;
        }

        public String getNonRedundantWritable() {
            return nonRedundantWritable;
        }

        public void setNonRedundantWritable(String nonRedundantWritable) {
            this.nonRedundantWritable = nonRedundantWritable;
        }

        public String getNonRedundantHistorical() {
            return nonRedundantHistorical;
        }

        public void setNonRedundantHistorical(String nonRedundantHistorical) {
            this.nonRedundantHistorical = nonRedundantHistorical;
        }

        public String getDualRedundantWritable() {
            return dualRedundantWritable;
        }

        public void setDualRedundantWritable(String dualRedundantWritable) {
            this.dualRedundantWritable = dualRedundantWritable;
        }

        public String getDualHistorical() {
            return dualHistorical;
        }

        public void setDualHistorical(String dualHistorical) {
            this.dualHistorical = dualHistorical;
        }

        public List<Volume> getVolumes() {
            return volumes;
        }

        public void setVolumes(List<Volume> volumes) {
            this.volumes = volumes;
        }

        public static class Volume {

            // Member Variables
            private String index;
            private String name;
            private String deviceId;
            private String serialNumber;

            public Volume() {
            }

            public String getIndex() {
                return index;
            }

            public void setIndex(String index) {
                this.index = index;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDeviceId() {
                return deviceId;
            }

            public void setDeviceId(String deviceId) {
                this.deviceId = deviceId;
            }

            public String getSerialNumber() {
                return serialNumber;
            }

            public void setSerialNumber(String serialNumber) {
                this.serialNumber = serialNumber;
            }

        }
    }
    
    /**
     * Returns the Volume that has the iqn on the LocalPort or null if a matching Volume cannot be found.
     * 
     * @param iqn the unique id that should exist on the matching Volume's LocalPort.
     * @return the Volume that has the iqn on the LocalPort or null if a matching Volume cannot be found.
     */
    public Volume getVolumeWithIsciIqn(String iqn) {
        Volume foundVolume = null;
        
        if(iqn != null && iqn.trim().length() > 0) {
            volumeLoop:
            for(Volume volume : this.getVolumes()) {
                for(Volume.Mapping mapping : volume.getMappings()) {
                    if(iqn.equalsIgnoreCase(mapping.getLocalPort())) {
                        foundVolume = volume;
                        break volumeLoop;
                    }
                }
            }
        }
        
        return foundVolume;
    }
    
    /**
     * Returns the Volume with the matching iscsiDeviceId or null if no match can be found.
     * 
     * @param iscsiDeviceId the iscsi device id the matching Volume must contain.
     * @return the Volume with the matching iscsiDeviceId or null if no match can be found.
     */
    public Volume getVolumeWithIscsiDeviceId(String iscsiDeviceId) {
        Volume volumeFound = null;
        
        for (PuppetCompellentDevice.Volume volume : this.getVolumes()) {
            if(iscsiDeviceId.equalsIgnoreCase(volume.getDeviceId())) {
                volumeFound = volume;
                break;
            }
        }
        
        return volumeFound;
    }

}
