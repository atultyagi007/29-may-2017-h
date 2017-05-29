/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.util.Collection;

/**
 * Represents an EMC Device as represented by Puppet.
 */
public class PuppetEmcDevice {
    
    // Class Variables
    public static final String MANAGEMENT_CONTROLLER_OS_NAME = "VNX Operating Environment for File";
    public static final String VNX_BLOCK_OPERATING_ENVIRONMENT = "VNX-Block-Operating-Environment";

    // Member Variables
    private String id; 
    private String name; 
    private String type; 
    private String wwn; 
    private String serialNumber; 
    private String emcPartNumber; 
    private String modelNumber; 
    private String highWatermark;
    private String lowWatermark;
    private String pageSize; 
    private String physicalNode; 
    private String uuidBaseAddress; 
    private String currentSystemType; 
    private String configuredSystemType; 
    private String classicCliState; 
    private String hwSystemType; 
    private String hwType; 
    private String ntp; 
    private String freeStoragePoolSpace;
    private String freeSpaceForFile;
    private String consumedDiskSpace;
    private String hotSpareDisks;
    private String rawDiskSpace;
    private String hotSpareDiskSpace;
    private Collection<Software> softwares;

    public Addons getAddons() {
        return addons;
    }

    public void setAddons(Addons addons) {
        this.addons = addons;
    }

    private Addons addons;
    
    public static class Software {

        // Member Variables
        private String name; 
        private String revision; 
        private String description; 
        private String isActive; 
        private String commitRequired; 
        private String revertPossible; 
        private String requiredPackages; 
        private String isInstallationComplete; 
        private String isSystemSoftare; 
        
        // Default constructor for the class.
        public Software() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRevision() {
            return revision;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIsActive() {
            return isActive;
        }

        public void setIsActive(String isActive) {
            this.isActive = isActive;
        }

        public String getCommitRequired() {
            return commitRequired;
        }

        public void setCommitRequired(String commitRequired) {
            this.commitRequired = commitRequired;
        }

        public String getRevertPossible() {
            return revertPossible;
        }

        public void setRevertPossible(String revertPossible) {
            this.revertPossible = revertPossible;
        }

        public String getRequiredPackages() {
            return requiredPackages;
        }

        public void setRequiredPackages(String requiredPackages) {
            this.requiredPackages = requiredPackages;
        }

        public String getIsInstallationComplete() {
            return isInstallationComplete;
        }

        public void setIsInstallationComplete(String isInstallationComplete) {
            this.isInstallationComplete = isInstallationComplete;
        }

        public String getIsSystemSoftare() {
            return isSystemSoftare;
        }

        public void setIsSystemSoftare(String isSystemSoftare) {
            this.isSystemSoftare = isSystemSoftare;
        }
    }
    
    private HbaInfo hbaInfo;
    
    public static class HbaInfo {
   
        // Member Variables
        private String numberOfHbaPorts; 
        private String hostLoginStatus; 
        private String hostManagementStatus; 
        private String isAttachedHost;
        private String hbaPortsInfo; 
        private Collection<HbaPortsInfo> hbaPortsInfos;
        
        // Default constructor for the class;
        public HbaInfo() { }
        
        public String getNumberOfHbaPorts() {
            return numberOfHbaPorts;
        }

        public void setNumberOfHbaPorts(String numberOfHbaPorts) {
            this.numberOfHbaPorts = numberOfHbaPorts;
        }

        public String getHostLoginStatus() {
            return hostLoginStatus;
        }

        public void setHostLoginStatus(String hostLoginStatus) {
            this.hostLoginStatus = hostLoginStatus;
        }

        public String getHostManagementStatus() {
            return hostManagementStatus;
        }

        public void setHostManagementStatus(String hostManagementStatus) {
            this.hostManagementStatus = hostManagementStatus;
        }

        public String getIsAttachedHost() {
            return isAttachedHost;
        }

        public void setIsAttachedHost(String isAttachedHost) {
            this.isAttachedHost = isAttachedHost;
        }

        public String getHbaPortsInfo() {
            return hbaPortsInfo;
        }

        public void setHbaPortsInfo(String hbaPortsInfo) {
            this.hbaPortsInfo = hbaPortsInfo;
        }

        public Collection<HbaPortsInfo> getHbaPortsInfos() {
            return hbaPortsInfos;
        }

        public void setHbaPortsInfos(Collection<HbaPortsInfo> hbaPortsInfos) {
            this.hbaPortsInfos = hbaPortsInfos;
        }


        public static class HbaPortsInfo {
           
            // Member Variables
            private String wwn; 
            private String vendorDescription; 
            private String numberOfSpPorts; 
            
            // Default constructor for the class;
            public HbaPortsInfo() {}

            public String getWwn() {
                return wwn;
            }

            public void setWwn(String wwn) {
                this.wwn = wwn;
            }

            public String getVendorDescription() {
                return vendorDescription;
            }

            public void setVendorDescription(String vendorDescription) {
                this.vendorDescription = vendorDescription;
            }

            public String getNumberOfSpPorts() {
                return numberOfSpPorts;
            }

            public void setNumberOfSpPorts(String numberOfSpPorts) {
                this.numberOfSpPorts = numberOfSpPorts;
            }
        }
    }
    
    
    private Collection<Controller> controllers;
    
    public static class Controller {
        
        // Member Variables
        private String hostName; 
        private String hostIpAddress; 
        private String hostId; 
        private String wwn; 
        private String osName; 
        private String isManuallyRegistered; 
        private String isExpandable; 
        private String pollType; 
        private String ifServerSupportSmartPoll;
        private String numberOfLuns;
        private String isManaged;
        private String osVersionAsString;
        private String hbaInfo; 
        private String lunUsageInfo;
        
        // Default constructor for the class.
        public Controller() {}

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getHostIpAddress() {
            return hostIpAddress;
        }

        public void setHostIpAddress(String hostIpAddress) {
            this.hostIpAddress = hostIpAddress;
        }

        public String getHostId() {
            return hostId;
        }

        public void setHostId(String hostId) {
            this.hostId = hostId;
        }

        public String getWwn() {
            return wwn;
        }

        public void setWwn(String wwn) {
            this.wwn = wwn;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getIsManuallyRegistered() {
            return isManuallyRegistered;
        }

        public void setIsManuallyRegistered(String isManuallyRegistered) {
            this.isManuallyRegistered = isManuallyRegistered;
        }

        public String getIsExpandable() {
            return isExpandable;
        }

        public void setIsExpandable(String isExpandable) {
            this.isExpandable = isExpandable;
        }

        public String getPollType() {
            return pollType;
        }

        public void setPollType(String pollType) {
            this.pollType = pollType;
        }

        public String getIfServerSupportSmartPoll() {
            return ifServerSupportSmartPoll;
        }

        public void setIfServerSupportSmartPoll(String ifServerSupportSmartPoll) {
            this.ifServerSupportSmartPoll = ifServerSupportSmartPoll;
        }

        public String getNumberOfLuns() {
            return numberOfLuns;
        }

        public void setNumberOfLuns(String numberOfLuns) {
            this.numberOfLuns = numberOfLuns;
        }

        public String getIsManaged() {
            return isManaged;
        }

        public void setIsManaged(String isManaged) {
            this.isManaged = isManaged;
        }

        public String getOsVersionAsString() {
            return osVersionAsString;
        }

        public void setOsVersionAsString(String osVersionAsString) {
            this.osVersionAsString = osVersionAsString;
        }

        public String getHbaInfo() {
            return hbaInfo;
        }

        public void setHbaInfo(String hbaInfo) {
            this.hbaInfo = hbaInfo;
        }

        public String getLunUsageInfo() {
            return lunUsageInfo;
        }

        public void setLunUsageInfo(String lunUsageInfo) {
            this.lunUsageInfo = lunUsageInfo;
        }
    }
    
    private Collection<DiskInfo> diskInfos; 
    
    public static class DiskInfo {
        
        // Member Variables
        private String name;
        private String bus; 
        private String enclosure;
        private String slot; 
        private String bank; 
        private String bankAsInteger; 
        private String state; 
        private String diskState; 
        private String capacityInMBs;
        private String numberOfUserSectors;
        private String type;
        private String writes;
        private String reads;
        private String blocksRead;
        private String bocksWritten;
        private String witeRetries;
        private String readRetries;
        private String idleTicks;
        private String busyTicks;
        private String zeroMark;
        private String currentSpeed;
        private String maximumSpeed;
        private String replacing;
        private String hardReadErrors;
        private String hardWriteErrors;
        private String softReadErrors;
        private String softWriteErrors;
        private String isHwSpinDownQualified;
        private String isHwSpinDownCapable;
        private String isVaultDrive;
        private String powerSavingState;
        private String diskDriveCategory;
        
        // Default constructor for the class.
        public DiskInfo() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBus() {
            return bus;
        }

        public void setBus(String bus) {
            this.bus = bus;
        }

        public String getEnclosure() {
            return enclosure;
        }

        public void setEnclosure(String enclosure) {
            this.enclosure = enclosure;
        }

        public String getSlot() {
            return slot;
        }

        public void setSlot(String slot) {
            this.slot = slot;
        }

        public String getBank() {
            return bank;
        }

        public void setBank(String bank) {
            this.bank = bank;
        }

        public String getBankAsInteger() {
            return bankAsInteger;
        }

        public void setBankAsInteger(String bankAsInteger) {
            this.bankAsInteger = bankAsInteger;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getDiskState() {
            return diskState;
        }

        public void setDiskState(String diskState) {
            this.diskState = diskState;
        }

        public String getCapacityInMBs() {
            return capacityInMBs;
        }

        public void setCapacityInMBs(String capacityInMBs) {
            this.capacityInMBs = capacityInMBs;
        }

        public String getNumberOfUserSectors() {
            return numberOfUserSectors;
        }

        public void setNumberOfUserSectors(String numberOfUserSectors) {
            this.numberOfUserSectors = numberOfUserSectors;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getWrites() {
            return writes;
        }

        public void setWrites(String writes) {
            this.writes = writes;
        }

        public String getReads() {
            return reads;
        }

        public void setReads(String reads) {
            this.reads = reads;
        }

        public String getBlocksRead() {
            return blocksRead;
        }

        public void setBlocksRead(String blocksRead) {
            this.blocksRead = blocksRead;
        }

        public String getBocksWritten() {
            return bocksWritten;
        }

        public void setBocksWritten(String bocksWritten) {
            this.bocksWritten = bocksWritten;
        }

        public String getWiteRetries() {
            return witeRetries;
        }

        public void setWiteRetries(String witeRetries) {
            this.witeRetries = witeRetries;
        }

        public String getReadRetries() {
            return readRetries;
        }

        public void setReadRetries(String readRetries) {
            this.readRetries = readRetries;
        }

        public String getIdleTicks() {
            return idleTicks;
        }

        public void setIdleTicks(String idleTicks) {
            this.idleTicks = idleTicks;
        }

        public String getBusyTicks() {
            return busyTicks;
        }

        public void setBusyTicks(String busyTicks) {
            this.busyTicks = busyTicks;
        }

        public String getZeroMark() {
            return zeroMark;
        }

        public void setZeroMark(String zeroMark) {
            this.zeroMark = zeroMark;
        }

        public String getCurrentSpeed() {
            return currentSpeed;
        }

        public void setCurrentSpeed(String currentSpeed) {
            this.currentSpeed = currentSpeed;
        }

        public String getMaximumSpeed() {
            return maximumSpeed;
        }

        public void setMaximumSpeed(String maximumSpeed) {
            this.maximumSpeed = maximumSpeed;
        }

        public String getReplacing() {
            return replacing;
        }

        public void setReplacing(String replacing) {
            this.replacing = replacing;
        }

        public String getHardReadErrors() {
            return hardReadErrors;
        }

        public void setHardReadErrors(String hardReadErrors) {
            this.hardReadErrors = hardReadErrors;
        }

        public String getHardWriteErrors() {
            return hardWriteErrors;
        }

        public void setHardWriteErrors(String hardWriteErrors) {
            this.hardWriteErrors = hardWriteErrors;
        }

        public String getSoftReadErrors() {
            return softReadErrors;
        }

        public void setSoftReadErrors(String softReadErrors) {
            this.softReadErrors = softReadErrors;
        }

        public String getSoftWriteErrors() {
            return softWriteErrors;
        }

        public void setSoftWriteErrors(String softWriteErrors) {
            this.softWriteErrors = softWriteErrors;
        }

        public String getIsHwSpinDownQualified() {
            return isHwSpinDownQualified;
        }

        public void setIsHwSpinDownQualified(String isHwSpinDownQualified) {
            this.isHwSpinDownQualified = isHwSpinDownQualified;
        }

        public String getIsHwSpinDownCapable() {
            return isHwSpinDownCapable;
        }

        public void setIsHwSpinDownCapable(String isHwSpinDownCapable) {
            this.isHwSpinDownCapable = isHwSpinDownCapable;
        }

        public String getIsVaultDrive() {
            return isVaultDrive;
        }

        public void setIsVaultDrive(String isVaultDrive) {
            this.isVaultDrive = isVaultDrive;
        }

        public String getPowerSavingState() {
            return powerSavingState;
        }

        public void setPowerSavingState(String powerSavingState) {
            this.powerSavingState = powerSavingState;
        }

        public String getDiskDriveCategory() {
            return diskDriveCategory;
        }

        public void setDiskDriveCategory(String diskDriveCategory) {
            this.diskDriveCategory = diskDriveCategory;
        }
    }
    
    private Collection<RaidGroup> raidGroups; 
    
    public static class RaidGroup {
        
        // Member Variables
        private String id; 
        private String capacity;
        private String freeSpace;
        private String largestUnboundSegmentSize;
        private String rgRaidType;
        private String type; 
        private String operationPriority;
        private String willBeRemoved;
        private String isPrivate;
        private String state; 
        private String maxLuns;
        private String rawCapacityBlocks;
        private String percentDefragmented;
        private String percentExpanded;
        private String lunExpansionEnabled;
        private String legalRaidTypes;
        private String rgUserDefinedPowerSaving;
        private String rgUserDefinedLatencyTolerance;
        private String isRgHardwarePowerSavingEligible;
        private String isRgPowerSavingEligible;
        private String isRgInStandByState;
        private String elementSize;
        private Collection<Disk> disks; 
        
        // Default constructor for the class;
        public RaidGroup() { }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCapacity() {
            return capacity;
        }

        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }

        public String getFreeSpace() {
            return freeSpace;
        }

        public void setFreeSpace(String freeSpace) {
            this.freeSpace = freeSpace;
        }

        public String getLargestUnboundSegmentSize() {
            return largestUnboundSegmentSize;
        }

        public void setLargestUnboundSegmentSize(String largestUnboundSegmentSize) {
            this.largestUnboundSegmentSize = largestUnboundSegmentSize;
        }

        public String getRgRaidType() {
            return rgRaidType;
        }

        public void setRgRaidType(String rgRaidType) {
            this.rgRaidType = rgRaidType;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOperationPriority() {
            return operationPriority;
        }

        public void setOperationPriority(String operationPriority) {
            this.operationPriority = operationPriority;
        }

        public String getWillBeRemoved() {
            return willBeRemoved;
        }

        public void setWillBeRemoved(String willBeRemoved) {
            this.willBeRemoved = willBeRemoved;
        }

        public String getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(String isPrivate) {
            this.isPrivate = isPrivate;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getMaxLuns() {
            return maxLuns;
        }

        public void setMaxLuns(String maxLuns) {
            this.maxLuns = maxLuns;
        }

        public String getRawCapacityBlocks() {
            return rawCapacityBlocks;
        }

        public void setRawCapacityBlocks(String rawCapacityBlocks) {
            this.rawCapacityBlocks = rawCapacityBlocks;
        }

        public String getPercentDefragmented() {
            return percentDefragmented;
        }

        public void setPercentDefragmented(String percentDefragmented) {
            this.percentDefragmented = percentDefragmented;
        }

        public String getPercentExpanded() {
            return percentExpanded;
        }

        public void setPercentExpanded(String percentExpanded) {
            this.percentExpanded = percentExpanded;
        }

        public String getLunExpansionEnabled() {
            return lunExpansionEnabled;
        }

        public void setLunExpansionEnabled(String lunExpansionEnabled) {
            this.lunExpansionEnabled = lunExpansionEnabled;
        }

        public String getLegalRaidTypes() {
            return legalRaidTypes;
        }

        public void setLegalRaidTypes(String legalRaidTypes) {
            this.legalRaidTypes = legalRaidTypes;
        }

        public String getRgUserDefinedPowerSaving() {
            return rgUserDefinedPowerSaving;
        }

        public void setRgUserDefinedPowerSaving(String rgUserDefinedPowerSaving) {
            this.rgUserDefinedPowerSaving = rgUserDefinedPowerSaving;
        }

        public String getRgUserDefinedLatencyTolerance() {
            return rgUserDefinedLatencyTolerance;
        }

        public void setRgUserDefinedLatencyTolerance(String rgUserDefinedLatencyTolerance) {
            this.rgUserDefinedLatencyTolerance = rgUserDefinedLatencyTolerance;
        }

        public String getIsRgHardwarePowerSavingEligible() {
            return isRgHardwarePowerSavingEligible;
        }

        public void setIsRgHardwarePowerSavingEligible(String isRgHardwarePowerSavingEligible) {
            this.isRgHardwarePowerSavingEligible = isRgHardwarePowerSavingEligible;
        }

        public String getIsRgPowerSavingEligible() {
            return isRgPowerSavingEligible;
        }

        public void setIsRgPowerSavingEligible(String isRgPowerSavingEligible) {
            this.isRgPowerSavingEligible = isRgPowerSavingEligible;
        }

        public String getIsRgInStandByState() {
            return isRgInStandByState;
        }

        public void setIsRgInStandByState(String isRgInStandByState) {
            this.isRgInStandByState = isRgInStandByState;
        }

        public String getElementSize() {
            return elementSize;
        }

        public void setElementSize(String elementSize) {
            this.elementSize = elementSize;
        }

        public Collection<Disk> getDisks() {
            return disks;
        }

        public void setDisks(Collection<Disk> disks) {
            this.disks = disks;
        }
    }
    
    public static class Disk { 
        
        // Member Variables
        private String name; 
        private String bus; 
        private String enclosure;
        private String slot; 
        private String bank; 
        private String bankAsInteger;
        
        // Default constructor for the class
        public Disk() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBus() {
            return bus;
        }

        public void setBus(String bus) {
            this.bus = bus;
        }

        public String getEnclosure() {
            return enclosure;
        }

        public void setEnclosure(String enclosure) {
            this.enclosure = enclosure;
        }

        public String getSlot() {
            return slot;
        }

        public void setSlot(String slot) {
            this.slot = slot;
        }

        public String getBank() {
            return bank;
        }

        public void setBank(String bank) {
            this.bank = bank;
        }

        public String getBankAsInteger() {
            return bankAsInteger;
        }

        public void setBankAsInteger(String bankAsInteger) {
            this.bankAsInteger = bankAsInteger;
        }
    }
    
    public Collection<DiskPool> diskPools; 
    
    public static class DiskPool {
        
        // Member Variables
        private String key; 
        private String name; 
        private String number; 
        private String rawCapacity;
        private String userCapacity;
        private String rgRawCapacity;
        private String rgUserCapacity;
        private String rgFreeCapacity;
        private String pools; 
        private Collection<Disk> disks; 
        private String raidGroups;
        
        // Default constructor for the class.
        public DiskPool() {}

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getRawCapacity() {
            return rawCapacity;
        }

        public void setRawCapacity(String rawCapacity) {
            this.rawCapacity = rawCapacity;
        }

        public String getUserCapacity() {
            return userCapacity;
        }

        public void setUserCapacity(String userCapacity) {
            this.userCapacity = userCapacity;
        }

        public String getRgRawCapacity() {
            return rgRawCapacity;
        }

        public void setRgRawCapacity(String rgRawCapacity) {
            this.rgRawCapacity = rgRawCapacity;
        }

        public String getRgUserCapacity() {
            return rgUserCapacity;
        }

        public void setRgUserCapacity(String rgUserCapacity) {
            this.rgUserCapacity = rgUserCapacity;
        }

        public String getRgFreeCapacity() {
            return rgFreeCapacity;
        }

        public void setRgFreeCapacity(String rgFreeCapacity) {
            this.rgFreeCapacity = rgFreeCapacity;
        }

        public String getPools() {
            return pools;
        }

        public void setPools(String pools) {
            this.pools = pools;
        }

        public Collection<Disk> getDisks() {
            return disks;
        }

        public void setDisks(Collection<Disk> disks) {
            this.disks = disks;
        }

        public String getRaidGroups() {
            return raidGroups;
        }

        public void setRaidGroups(String raidGroups) {
            this.raidGroups = raidGroups;
        }
    }
    
    public Collection<Pool> pools; 
    
    public static class Pool {
        
        // Member Variables
        private String key;
        private String name; 
        private String id; 
        private String raidType; 
        private String libraryName;
        private String creationTime;
        private String state;
        private String status; 
        private String rawCapacity;
        private String userCapacity;
        private String allocatedCapacity;
        private String sharedCapAlertLevelPrcnt;
        private String compressionPausePrcnt;
        private String compressionHaltPrcnt;
        private String activeOperation;
        private String activeOperationState;
        private String activeOperationCompletePrcnt;
        private String isFaulted;
        private String efdCacheState;
        private String efdCacheCurrentState;
        private String snapHarvestHighThreshold;
        private String snapHarvestLowThreshold;
        private String poolHarvestHighThreshold;
        private String poolHarvestLowThreshold;
        private String snapHarvestingEnabled;
        private String poolHarvestingEnabled;
        private String snapHarvestingState;
        private String poolHarvestingState;
        private String totalSubscribedCapacity;
        private String primarySubscribedCapacity;
        private String secondarySubscribedCapacity;
        private String metaDataSubscribedCapacity;
        private String primaryDataConsumedCapacity;
        private String secondaryDataConsumedCapacity;
        private String metaDataConsumedCapacity;
        private String compressionSavings;
        private String diskPoolKeys;
        private String raidGroups;
        private Collection<Mlu> mlus; 
        private String internalLuns; 
        private String autoTieringTierInfos;
        private String availableSpaceInGB;

        // Default constructor for the class.
        public Pool() {}        
        
        public String getAvailableSpaceInGB() {
            return availableSpaceInGB;
        }

        public void setAvailableSpaceInGB(String availableSpaceInGB) {
            this.availableSpaceInGB = availableSpaceInGB;
        }

        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
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
        public String getRaidType() {
            return raidType;
        }
        public void setRaidType(String raidType) {
            this.raidType = raidType;
        }
        public String getLibraryName() {
            return libraryName;
        }
        public void setLibraryName(String libraryName) {
            this.libraryName = libraryName;
        }
        public String getCreationTime() {
            return creationTime;
        }
        public void setCreationTime(String creationTime) {
            this.creationTime = creationTime;
        }
        public String getState() {
            return state;
        }
        public void setState(String state) {
            this.state = state;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public String getRawCapacity() {
            return rawCapacity;
        }
        public void setRawCapacity(String rawCapacity) {
            this.rawCapacity = rawCapacity;
        }
        public String getUserCapacity() {
            return userCapacity;
        }
        public void setUserCapacity(String userCapacity) {
            this.userCapacity = userCapacity;
        }
        public String getAllocatedCapacity() {
            return allocatedCapacity;
        }
        public void setAllocatedCapacity(String allocatedCapacity) {
            this.allocatedCapacity = allocatedCapacity;
        }
        public String getSharedCapAlertLevelPrcnt() {
            return sharedCapAlertLevelPrcnt;
        }
        public void setSharedCapAlertLevelPrcnt(String sharedCapAlertLevelPrcnt) {
            this.sharedCapAlertLevelPrcnt = sharedCapAlertLevelPrcnt;
        }
        public String getCompressionPausePrcnt() {
            return compressionPausePrcnt;
        }
        public void setCompressionPausePrcnt(String compressionPausePrcnt) {
            this.compressionPausePrcnt = compressionPausePrcnt;
        }
        public String getCompressionHaltPrcnt() {
            return compressionHaltPrcnt;
        }
        public void setCompressionHaltPrcnt(String compressionHaltPrcnt) {
            this.compressionHaltPrcnt = compressionHaltPrcnt;
        }
        public String getActiveOperation() {
            return activeOperation;
        }
        public void setActiveOperation(String activeOperation) {
            this.activeOperation = activeOperation;
        }
        public String getActiveOperationState() {
            return activeOperationState;
        }
        public void setActiveOperationState(String activeOperationState) {
            this.activeOperationState = activeOperationState;
        }
        public String getActiveOperationCompletePrcnt() {
            return activeOperationCompletePrcnt;
        }
        public void setActiveOperationCompletePrcnt(String activeOperationCompletePrcnt) {
            this.activeOperationCompletePrcnt = activeOperationCompletePrcnt;
        }
        public String getIsFaulted() {
            return isFaulted;
        }
        public void setIsFaulted(String isFaulted) {
            this.isFaulted = isFaulted;
        }
        public String getEfdCacheState() {
            return efdCacheState;
        }
        public void setEfdCacheState(String efdCacheState) {
            this.efdCacheState = efdCacheState;
        }
        public String getEfdCacheCurrentState() {
            return efdCacheCurrentState;
        }
        public void setEfdCacheCurrentState(String efdCacheCurrentState) {
            this.efdCacheCurrentState = efdCacheCurrentState;
        }
        public String getSnapHarvestHighThreshold() {
            return snapHarvestHighThreshold;
        }
        public void setSnapHarvestHighThreshold(String snapHarvestHighThreshold) {
            this.snapHarvestHighThreshold = snapHarvestHighThreshold;
        }
        public String getSnapHarvestLowThreshold() {
            return snapHarvestLowThreshold;
        }
        public void setSnapHarvestLowThreshold(String snapHarvestLowThreshold) {
            this.snapHarvestLowThreshold = snapHarvestLowThreshold;
        }
        public String getPoolHarvestHighThreshold() {
            return poolHarvestHighThreshold;
        }
        public void setPoolHarvestHighThreshold(String poolHarvestHighThreshold) {
            this.poolHarvestHighThreshold = poolHarvestHighThreshold;
        }
        public String getPoolHarvestLowThreshold() {
            return poolHarvestLowThreshold;
        }
        public void setPoolHarvestLowThreshold(String poolHarvestLowThreshold) {
            this.poolHarvestLowThreshold = poolHarvestLowThreshold;
        }
        public String getSnapHarvestingEnabled() {
            return snapHarvestingEnabled;
        }
        public void setSnapHarvestingEnabled(String snapHarvestingEnabled) {
            this.snapHarvestingEnabled = snapHarvestingEnabled;
        }
        public String getPoolHarvestingEnabled() {
            return poolHarvestingEnabled;
        }
        public void setPoolHarvestingEnabled(String poolHarvestingEnabled) {
            this.poolHarvestingEnabled = poolHarvestingEnabled;
        }
        public String getSnapHarvestingState() {
            return snapHarvestingState;
        }
        public void setSnapHarvestingState(String snapHarvestingState) {
            this.snapHarvestingState = snapHarvestingState;
        }
        public String getPoolHarvestingState() {
            return poolHarvestingState;
        }
        public void setPoolHarvestingState(String poolHarvestingState) {
            this.poolHarvestingState = poolHarvestingState;
        }
        public String getTotalSubscribedCapacity() {
            return totalSubscribedCapacity;
        }
        public void setTotalSubscribedCapacity(String totalSubscribedCapacity) {
            this.totalSubscribedCapacity = totalSubscribedCapacity;
        }
        public String getPrimarySubscribedCapacity() {
            return primarySubscribedCapacity;
        }
        public void setPrimarySubscribedCapacity(String primarySubscribedCapacity) {
            this.primarySubscribedCapacity = primarySubscribedCapacity;
        }
        public String getSecondarySubscribedCapacity() {
            return secondarySubscribedCapacity;
        }
        public void setSecondarySubscribedCapacity(String secondarySubscribedCapacity) {
            this.secondarySubscribedCapacity = secondarySubscribedCapacity;
        }
        public String getMetaDataSubscribedCapacity() {
            return metaDataSubscribedCapacity;
        }
        public void setMetaDataSubscribedCapacity(String metaDataSubscribedCapacity) {
            this.metaDataSubscribedCapacity = metaDataSubscribedCapacity;
        }
        public String getPrimaryDataConsumedCapacity() {
            return primaryDataConsumedCapacity;
        }
        public void setPrimaryDataConsumedCapacity(String primaryDataConsumedCapacity) {
            this.primaryDataConsumedCapacity = primaryDataConsumedCapacity;
        }
        public String getSecondaryDataConsumedCapacity() {
            return secondaryDataConsumedCapacity;
        }
        public void setSecondaryDataConsumedCapacity(String secondaryDataConsumedCapacity) {
            this.secondaryDataConsumedCapacity = secondaryDataConsumedCapacity;
        }
        public String getMetaDataConsumedCapacity() {
            return metaDataConsumedCapacity;
        }
        public void setMetaDataConsumedCapacity(String metaDataConsumedCapacity) {
            this.metaDataConsumedCapacity = metaDataConsumedCapacity;
        }
        public String getCompressionSavings() {
            return compressionSavings;
        }
        public void setCompressionSavings(String compressionSavings) {
            this.compressionSavings = compressionSavings;
        }
        public String getDiskPoolKeys() {
            return diskPoolKeys;
        }
        public void setDiskPoolKeys(String diskPoolKeys) {
            this.diskPoolKeys = diskPoolKeys;
        }
        public String getRaidGroups() {
            return raidGroups;
        }
        public void setRaidGroups(String raidGroups) {
            this.raidGroups = raidGroups;
        }
        public Collection<Mlu> getMlus() {
            return mlus;
        }
        public void setMlus(Collection<Mlu> mlus) {
            this.mlus = mlus;
        }
        public String getInternalLuns() {
            return internalLuns;
        }
        public void setInternalLuns(String internalLuns) {
            this.internalLuns = internalLuns;
        }
        public String getAutoTieringTierInfos() {
            return autoTieringTierInfos;
        }
        public void setAutoTieringTierInfos(String autoTieringTierInfos) {
            this.autoTieringTierInfos = autoTieringTierInfos;
        }

        public static class Mlu { 
            
            public static final String STATE_READY = "2";
            
            // Member variables
            private String wwn; 
            private String objectID;
            private String poolKey;
            private String luType;
            private String name; 
            private String number;
            private String creationTime;
            private String currentOwner;
            private String defaultOwner;
            private String allocationOwner;
            private String userCapacity;
            private String consumedCapacity;
            private String subscribedCapacity;
            private String isFaulted;
            private String isTransitioning;
            private String state;  
            private String internalState;
            private String creatingDriverName;
            private String consumingDriverName;
            private String isAutoAssignEnabled;
            private String isAutoTrespassEnabled;
            private String operationType;
            private String operationCompletePrcnt;
            private String operationStatus;
            private String alignmentOffset;
            private String raidType;
            private String hostBlocksReadSPA;
            private String hostBlocksReadSPB;
            private String hostBlocksWrittenSPA;
            private String hostBlocksWrittentSPB;
            private String hostReadRequestsSPA;
            private String HostReadRequestsSPB;
            private String HostWriteRequestsSPA;
            private String HostWriteRequestsSPB;
            private String busyTicksSPA;
            private String busyTicksSPB;
            private String idleTicksSPA;
            private String idleTicksSPB;
            private String explicitTrespassesSPA;
            private String explicitTrespassesSPB;
            private String implicitTrespassesSPA;
            private String implicitTrespassesSPB;
            private String nonZeroReqCntArrivalsSPA;
            private String nonZeroReqCntArrivalsSPB;
            private String sumOutstandingReqsSPA;
            private String sumOutstandingReqsSPB;
            private String recoveryState;
            private String dataState;
            private String isInitializing;
            private String throttleRate;
            private String rootSliceID;
            private String rootSliceOffset;
            private String rootSliceLength;
            private String rootSlicePosition;
            private String cbfsFileSystemId;
            private String firstSliceId;
            private String firstSliceOffset;
            private String firstSliceLength;
            private String firstSlicePosition;
            private String cbfsFileInodeNumber;
            private String cbfsFileGenerationNumber;
            private String fileSystemObjectId;
            private String fileSystemId;
            private String mluObjectId;
            private String fileObjectId;
            private String isCompressed;
            private String tierPlacementPreference;
            private String autoTieringPolicy;
            private String secondSliceId;
            private String secondSliceOffset;
            private String secondSliceLength;
            private String secondSlicePosition;
            private String isInternal;
            private String harvestPriority;
            private String snapCount;
            private String snapLunCount;
            private String isAdvsnapAttachedAllowed;
            private String secondaryConsumedCapacity;
            private String primaryConsumedCapacity;
            private String uncommittedConsumption;
            private String metaDataConsumedCapacity;
            private String secondarySubscribedCapacity;
            private String metaDataSubscribedCapacity;
            private String compressionSavings;
            private String ioDisposition;
            private String fileMode;  
            private String compRevId;
            private String allocationPolicy;
            private String autoTieringTierInfos;
            
            // Default Constructor for the class
            public Mlu() {}

            public String getWwn() {
                return wwn;
            }

            public void setWwn(String wwn) {
                this.wwn = wwn;
            }

            public String getObjectID() {
                return objectID;
            }

            public void setObjectID(String objectID) {
                this.objectID = objectID;
            }

            public String getPoolKey() {
                return poolKey;
            }

            public void setPoolKey(String poolKey) {
                this.poolKey = poolKey;
            }

            public String getLuType() {
                return luType;
            }

            public void setLuType(String luType) {
                this.luType = luType;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getNumber() {
                return number;
            }

            public void setNumber(String number) {
                this.number = number;
            }

            public String getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(String creationTime) {
                this.creationTime = creationTime;
            }

            public String getCurrentOwner() {
                return currentOwner;
            }

            public void setCurrentOwner(String currentOwner) {
                this.currentOwner = currentOwner;
            }

            public String getDefaultOwner() {
                return defaultOwner;
            }

            public void setDefaultOwner(String defaultOwner) {
                this.defaultOwner = defaultOwner;
            }

            public String getAllocationOwner() {
                return allocationOwner;
            }

            public void setAllocationOwner(String allocationOwner) {
                this.allocationOwner = allocationOwner;
            }

            public String getUserCapacity() {
                return userCapacity;
            }

            public void setUserCapacity(String userCapacity) {
                this.userCapacity = userCapacity;
            }

            public String getConsumedCapacity() {
                return consumedCapacity;
            }

            public void setConsumedCapacity(String consumedCapacity) {
                this.consumedCapacity = consumedCapacity;
            }

            public String getSubscribedCapacity() {
                return subscribedCapacity;
            }

            public void setSubscribedCapacity(String subscribedCapacity) {
                this.subscribedCapacity = subscribedCapacity;
            }

            public String getIsFaulted() {
                return isFaulted;
            }

            public void setIsFaulted(String isFaulted) {
                this.isFaulted = isFaulted;
            }

            public String getIsTransitioning() {
                return isTransitioning;
            }

            public void setIsTransitioning(String isTransitioning) {
                this.isTransitioning = isTransitioning;
            }

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public String getInternalState() {
                return internalState;
            }

            public void setInternalState(String internalState) {
                this.internalState = internalState;
            }

            public String getCreatingDriverName() {
                return creatingDriverName;
            }

            public void setCreatingDriverName(String creatingDriverName) {
                this.creatingDriverName = creatingDriverName;
            }

            public String getConsumingDriverName() {
                return consumingDriverName;
            }

            public void setConsumingDriverName(String consumingDriverName) {
                this.consumingDriverName = consumingDriverName;
            }

            public String getIsAutoAssignEnabled() {
                return isAutoAssignEnabled;
            }

            public void setIsAutoAssignEnabled(String isAutoAssignEnabled) {
                this.isAutoAssignEnabled = isAutoAssignEnabled;
            }

            public String getIsAutoTrespassEnabled() {
                return isAutoTrespassEnabled;
            }

            public void setIsAutoTrespassEnabled(String isAutoTrespassEnabled) {
                this.isAutoTrespassEnabled = isAutoTrespassEnabled;
            }

            public String getOperationType() {
                return operationType;
            }

            public void setOperationType(String operationType) {
                this.operationType = operationType;
            }

            public String getOperationCompletePrcnt() {
                return operationCompletePrcnt;
            }

            public void setOperationCompletePrcnt(String operationCompletePrcnt) {
                this.operationCompletePrcnt = operationCompletePrcnt;
            }

            public String getOperationStatus() {
                return operationStatus;
            }

            public void setOperationStatus(String operationStatus) {
                this.operationStatus = operationStatus;
            }

            public String getAlignmentOffset() {
                return alignmentOffset;
            }

            public void setAlignmentOffset(String alignmentOffset) {
                this.alignmentOffset = alignmentOffset;
            }

            public String getRaidType() {
                return raidType;
            }

            public void setRaidType(String raidType) {
                this.raidType = raidType;
            }

            public String getHostBlocksReadSPA() {
                return hostBlocksReadSPA;
            }

            public void setHostBlocksReadSPA(String hostBlocksReadSPA) {
                this.hostBlocksReadSPA = hostBlocksReadSPA;
            }

            public String getHostBlocksReadSPB() {
                return hostBlocksReadSPB;
            }

            public void setHostBlocksReadSPB(String hostBlocksReadSPB) {
                this.hostBlocksReadSPB = hostBlocksReadSPB;
            }

            public String getHostBlocksWrittenSPA() {
                return hostBlocksWrittenSPA;
            }

            public void setHostBlocksWrittenSPA(String hostBlocksWrittenSPA) {
                this.hostBlocksWrittenSPA = hostBlocksWrittenSPA;
            }

            public String getHostBlocksWrittentSPB() {
                return hostBlocksWrittentSPB;
            }

            public void setHostBlocksWrittentSPB(String hostBlocksWrittentSPB) {
                this.hostBlocksWrittentSPB = hostBlocksWrittentSPB;
            }

            public String getHostReadRequestsSPA() {
                return hostReadRequestsSPA;
            }

            public void setHostReadRequestsSPA(String hostReadRequestsSPA) {
                this.hostReadRequestsSPA = hostReadRequestsSPA;
            }

            public String getHostReadRequestsSPB() {
                return HostReadRequestsSPB;
            }

            public void setHostReadRequestsSPB(String hostReadRequestsSPB) {
                HostReadRequestsSPB = hostReadRequestsSPB;
            }

            public String getHostWriteRequestsSPA() {
                return HostWriteRequestsSPA;
            }

            public void setHostWriteRequestsSPA(String hostWriteRequestsSPA) {
                HostWriteRequestsSPA = hostWriteRequestsSPA;
            }

            public String getHostWriteRequestsSPB() {
                return HostWriteRequestsSPB;
            }

            public void setHostWriteRequestsSPB(String hostWriteRequestsSPB) {
                HostWriteRequestsSPB = hostWriteRequestsSPB;
            }

            public String getBusyTicksSPA() {
                return busyTicksSPA;
            }

            public void setBusyTicksSPA(String busyTicksSPA) {
                this.busyTicksSPA = busyTicksSPA;
            }

            public String getBusyTicksSPB() {
                return busyTicksSPB;
            }

            public void setBusyTicksSPB(String busyTicksSPB) {
                this.busyTicksSPB = busyTicksSPB;
            }

            public String getIdleTicksSPA() {
                return idleTicksSPA;
            }

            public void setIdleTicksSPA(String idleTicksSPA) {
                this.idleTicksSPA = idleTicksSPA;
            }

            public String getIdleTicksSPB() {
                return idleTicksSPB;
            }

            public void setIdleTicksSPB(String idleTicksSPB) {
                this.idleTicksSPB = idleTicksSPB;
            }

            public String getExplicitTrespassesSPA() {
                return explicitTrespassesSPA;
            }

            public void setExplicitTrespassesSPA(String explicitTrespassesSPA) {
                this.explicitTrespassesSPA = explicitTrespassesSPA;
            }

            public String getExplicitTrespassesSPB() {
                return explicitTrespassesSPB;
            }

            public void setExplicitTrespassesSPB(String explicitTrespassesSPB) {
                this.explicitTrespassesSPB = explicitTrespassesSPB;
            }

            public String getImplicitTrespassesSPA() {
                return implicitTrespassesSPA;
            }

            public void setImplicitTrespassesSPA(String implicitTrespassesSPA) {
                this.implicitTrespassesSPA = implicitTrespassesSPA;
            }

            public String getImplicitTrespassesSPB() {
                return implicitTrespassesSPB;
            }

            public void setImplicitTrespassesSPB(String implicitTrespassesSPB) {
                this.implicitTrespassesSPB = implicitTrespassesSPB;
            }

            public String getNonZeroReqCntArrivalsSPA() {
                return nonZeroReqCntArrivalsSPA;
            }

            public void setNonZeroReqCntArrivalsSPA(String nonZeroReqCntArrivalsSPA) {
                this.nonZeroReqCntArrivalsSPA = nonZeroReqCntArrivalsSPA;
            }

            public String getNonZeroReqCntArrivalsSPB() {
                return nonZeroReqCntArrivalsSPB;
            }

            public void setNonZeroReqCntArrivalsSPB(String nonZeroReqCntArrivalsSPB) {
                this.nonZeroReqCntArrivalsSPB = nonZeroReqCntArrivalsSPB;
            }

            public String getSumOutstandingReqsSPA() {
                return sumOutstandingReqsSPA;
            }

            public void setSumOutstandingReqsSPA(String sumOutstandingReqsSPA) {
                this.sumOutstandingReqsSPA = sumOutstandingReqsSPA;
            }

            public String getSumOutstandingReqsSPB() {
                return sumOutstandingReqsSPB;
            }

            public void setSumOutstandingReqsSPB(String sumOutstandingReqsSPB) {
                this.sumOutstandingReqsSPB = sumOutstandingReqsSPB;
            }

            public String getRecoveryState() {
                return recoveryState;
            }

            public void setRecoveryState(String recoveryState) {
                this.recoveryState = recoveryState;
            }

            public String getDataState() {
                return dataState;
            }

            public void setDataState(String dataState) {
                this.dataState = dataState;
            }

            public String getIsInitializing() {
                return isInitializing;
            }

            public void setIsInitializing(String isInitializing) {
                this.isInitializing = isInitializing;
            }

            public String getThrottleRate() {
                return throttleRate;
            }

            public void setThrottleRate(String throttleRate) {
                this.throttleRate = throttleRate;
            }

            public String getRootSliceID() {
                return rootSliceID;
            }

            public void setRootSliceID(String rootSliceID) {
                this.rootSliceID = rootSliceID;
            }

            public String getRootSliceOffset() {
                return rootSliceOffset;
            }

            public void setRootSliceOffset(String rootSliceOffset) {
                this.rootSliceOffset = rootSliceOffset;
            }

            public String getRootSliceLength() {
                return rootSliceLength;
            }

            public void setRootSliceLength(String rootSliceLength) {
                this.rootSliceLength = rootSliceLength;
            }

            public String getRootSlicePosition() {
                return rootSlicePosition;
            }

            public void setRootSlicePosition(String rootSlicePosition) {
                this.rootSlicePosition = rootSlicePosition;
            }

            public String getCbfsFileSystemId() {
                return cbfsFileSystemId;
            }

            public void setCbfsFileSystemId(String cbfsFileSystemId) {
                this.cbfsFileSystemId = cbfsFileSystemId;
            }

            public String getFirstSliceId() {
                return firstSliceId;
            }

            public void setFirstSliceId(String firstSliceId) {
                this.firstSliceId = firstSliceId;
            }

            public String getFirstSliceOffset() {
                return firstSliceOffset;
            }

            public void setFirstSliceOffset(String firstSliceOffset) {
                this.firstSliceOffset = firstSliceOffset;
            }

            public String getFirstSliceLength() {
                return firstSliceLength;
            }

            public void setFirstSliceLength(String firstSliceLength) {
                this.firstSliceLength = firstSliceLength;
            }

            public String getFirstSlicePosition() {
                return firstSlicePosition;
            }

            public void setFirstSlicePosition(String firstSlicePosition) {
                this.firstSlicePosition = firstSlicePosition;
            }

            public String getCbfsFileInodeNumber() {
                return cbfsFileInodeNumber;
            }

            public void setCbfsFileInodeNumber(String cbfsFileInodeNumber) {
                this.cbfsFileInodeNumber = cbfsFileInodeNumber;
            }

            public String getCbfsFileGenerationNumber() {
                return cbfsFileGenerationNumber;
            }

            public void setCbfsFileGenerationNumber(String cbfsFileGenerationNumber) {
                this.cbfsFileGenerationNumber = cbfsFileGenerationNumber;
            }

            public String getFileSystemObjectId() {
                return fileSystemObjectId;
            }

            public void setFileSystemObjectId(String fileSystemObjectId) {
                this.fileSystemObjectId = fileSystemObjectId;
            }

            public String getFileSystemId() {
                return fileSystemId;
            }

            public void setFileSystemId(String fileSystemId) {
                this.fileSystemId = fileSystemId;
            }

            public String getMluObjectId() {
                return mluObjectId;
            }

            public void setMluObjectId(String mluObjectId) {
                this.mluObjectId = mluObjectId;
            }

            public String getFileObjectId() {
                return fileObjectId;
            }

            public void setFileObjectId(String fileObjectId) {
                this.fileObjectId = fileObjectId;
            }

            public String getIsCompressed() {
                return isCompressed;
            }

            public void setIsCompressed(String isCompressed) {
                this.isCompressed = isCompressed;
            }

            public String getTierPlacementPreference() {
                return tierPlacementPreference;
            }

            public void setTierPlacementPreference(String tierPlacementPreference) {
                this.tierPlacementPreference = tierPlacementPreference;
            }

            public String getAutoTieringPolicy() {
                return autoTieringPolicy;
            }

            public void setAutoTieringPolicy(String autoTieringPolicy) {
                this.autoTieringPolicy = autoTieringPolicy;
            }

            public String getSecondSliceId() {
                return secondSliceId;
            }

            public void setSecondSliceId(String secondSliceId) {
                this.secondSliceId = secondSliceId;
            }

            public String getSecondSliceOffset() {
                return secondSliceOffset;
            }

            public void setSecondSliceOffset(String secondSliceOffset) {
                this.secondSliceOffset = secondSliceOffset;
            }

            public String getSecondSliceLength() {
                return secondSliceLength;
            }

            public void setSecondSliceLength(String secondSliceLength) {
                this.secondSliceLength = secondSliceLength;
            }

            public String getSecondSlicePosition() {
                return secondSlicePosition;
            }

            public void setSecondSlicePosition(String secondSlicePosition) {
                this.secondSlicePosition = secondSlicePosition;
            }

            public String getIsInternal() {
                return isInternal;
            }

            public void setIsInternal(String isInternal) {
                this.isInternal = isInternal;
            }

            public String getHarvestPriority() {
                return harvestPriority;
            }

            public void setHarvestPriority(String harvestPriority) {
                this.harvestPriority = harvestPriority;
            }

            public String getSnapCount() {
                return snapCount;
            }

            public void setSnapCount(String snapCount) {
                this.snapCount = snapCount;
            }

            public String getSnapLunCount() {
                return snapLunCount;
            }

            public void setSnapLunCount(String snapLunCount) {
                this.snapLunCount = snapLunCount;
            }

            public String getIsAdvsnapAttachedAllowed() {
                return isAdvsnapAttachedAllowed;
            }

            public void setIsAdvsnapAttachedAllowed(String isAdvsnapAttachedAllowed) {
                this.isAdvsnapAttachedAllowed = isAdvsnapAttachedAllowed;
            }

            public String getSecondaryConsumedCapacity() {
                return secondaryConsumedCapacity;
            }

            public void setSecondaryConsumedCapacity(String secondaryConsumedCapacity) {
                this.secondaryConsumedCapacity = secondaryConsumedCapacity;
            }

            public String getPrimaryConsumedCapacity() {
                return primaryConsumedCapacity;
            }

            public void setPrimaryConsumedCapacity(String primaryConsumedCapacity) {
                this.primaryConsumedCapacity = primaryConsumedCapacity;
            }

            public String getUncommittedConsumption() {
                return uncommittedConsumption;
            }

            public void setUncommittedConsumption(String uncommittedConsumption) {
                this.uncommittedConsumption = uncommittedConsumption;
            }

            public String getMetaDataConsumedCapacity() {
                return metaDataConsumedCapacity;
            }

            public void setMetaDataConsumedCapacity(String metaDataConsumedCapacity) {
                this.metaDataConsumedCapacity = metaDataConsumedCapacity;
            }

            public String getSecondarySubscribedCapacity() {
                return secondarySubscribedCapacity;
            }

            public void setSecondarySubscribedCapacity(String secondarySubscribedCapacity) {
                this.secondarySubscribedCapacity = secondarySubscribedCapacity;
            }

            public String getMetaDataSubscribedCapacity() {
                return metaDataSubscribedCapacity;
            }

            public void setMetaDataSubscribedCapacity(String metaDataSubscribedCapacity) {
                this.metaDataSubscribedCapacity = metaDataSubscribedCapacity;
            }

            public String getCompressionSavings() {
                return compressionSavings;
            }

            public void setCompressionSavings(String compressionSavings) {
                this.compressionSavings = compressionSavings;
            }

            public String getIoDisposition() {
                return ioDisposition;
            }

            public void setIoDisposition(String ioDisposition) {
                this.ioDisposition = ioDisposition;
            }

            public String getFileMode() {
                return fileMode;
            }

            public void setFileMode(String fileMode) {
                this.fileMode = fileMode;
            }

            public String getCompRevId() {
                return compRevId;
            }

            public void setCompRevId(String compRevId) {
                this.compRevId = compRevId;
            }

            public String getAllocationPolicy() {
                return allocationPolicy;
            }

            public void setAllocationPolicy(String allocationPolicy) {
                this.allocationPolicy = allocationPolicy;
            }

            public String getAutoTieringTierInfos() {
                return autoTieringTierInfos;
            }

            public void setAutoTieringTierInfos(String autoTieringTierInfos) {
                this.autoTieringTierInfos = autoTieringTierInfos;
            }
        }
    }
    
    /**
     * Default constructor for the class.
     */
    public PuppetEmcDevice() { }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWwn() {
        return wwn;
    }

    public void setWwn(String wwn) {
        this.wwn = wwn;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getEmcPartNumber() {
        return emcPartNumber;
    }

    public void setEmcPartNumber(String emcPartNumber) {
        this.emcPartNumber = emcPartNumber;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getHighWatermark() {
        return highWatermark;
    }

    public void setHighWatermark(String highWatermark) {
        this.highWatermark = highWatermark;
    }

    public String getLowWatermark() {
        return lowWatermark;
    }

    public void setLowWatermark(String lowWatermark) {
        this.lowWatermark = lowWatermark;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getPhysicalNode() {
        return physicalNode;
    }

    public void setPhysicalNode(String physicalNode) {
        this.physicalNode = physicalNode;
    }

    public String getUuidBaseAddress() {
        return uuidBaseAddress;
    }

    public void setUuidBaseAddress(String uuidBaseAddress) {
        this.uuidBaseAddress = uuidBaseAddress;
    }

    public String getCurrentSystemType() {
        return currentSystemType;
    }

    public void setCurrentSystemType(String currentSystemType) {
        this.currentSystemType = currentSystemType;
    }

    public String getConfiguredSystemType() {
        return configuredSystemType;
    }

    public void setConfiguredSystemType(String configuredSystemType) {
        this.configuredSystemType = configuredSystemType;
    }

    public String getClassicCliState() {
        return classicCliState;
    }

    public void setClassicCliState(String classicCliState) {
        this.classicCliState = classicCliState;
    }

    public String getHwSystemType() {
        return hwSystemType;
    }

    public void setHwSystemType(String hwSystemType) {
        this.hwSystemType = hwSystemType;
    }

    public String getHwType() {
        return hwType;
    }

    public void setHwType(String hwType) {
        this.hwType = hwType;
    }

    public String getNtp() {
        return ntp;
    }

    public void setNtp(String ntp) {
        this.ntp = ntp;
    }

    public Collection<Software> getSoftwares() {
        return softwares;
    }

    public void setSoftwares(Collection<Software> softwares) {
        this.softwares = softwares;
    }

    public HbaInfo getHbaInfo() {
        return hbaInfo;
    }

    public void setHbaInfo(HbaInfo hbaInfo) {
        this.hbaInfo = hbaInfo;
    }

    public Collection<Controller> getControllers() {
        return controllers;
    }

    public void setControllers(Collection<Controller> controllers) {
        this.controllers = controllers;
    }

    public Collection<DiskInfo> getDiskInfos() {
        return diskInfos;
    }

    public void setDiskInfos(Collection<DiskInfo> diskInfos) {
        this.diskInfos = diskInfos;
    }

    public Collection<RaidGroup> getRaidGroups() {
        return raidGroups;
    }

    public void setRaidGroups(Collection<RaidGroup> raidGroups) {
        this.raidGroups = raidGroups;
    }

    public Collection<DiskPool> getDiskPools() {
        return diskPools;
    }

    public void setDiskPools(Collection<DiskPool> diskPools) {
        this.diskPools = diskPools;
    }

    public Collection<Pool> getPools() {
        return pools;
    }

    public void setPools(Collection<Pool> pools) {
        this.pools = pools;
    }
    

    public String getFreeStoragePoolSpace() {
        return freeStoragePoolSpace;
    }

    public void setFreeStoragePoolSpace(String freeStoragePoolSpace) {
        this.freeStoragePoolSpace = freeStoragePoolSpace;
    }

    public String getConsumedDiskSpace() {
        return consumedDiskSpace;
    }

    public void setConsumedDiskSpace(String consumedDiskSpace) {
        this.consumedDiskSpace = consumedDiskSpace;
    }

    public String getFreeSpaceForFile() {
        return freeSpaceForFile;
    }

    public void setFreeSpaceForFile(String freeSpaceForFile) {
        this.freeSpaceForFile = freeSpaceForFile;
    }
    
    public String getHotSpareDisks() {
        return hotSpareDisks;
    }

    public void setHotSpareDisks(String hotSpareDisks) {
        this.hotSpareDisks = hotSpareDisks;
    }

    public String getRawDiskSpace() {
        return rawDiskSpace;
    }

    public void setRawDiskSpace(String rawDiskSpace) {
        this.rawDiskSpace = rawDiskSpace;
    }

    public String getHotSpareDiskSpace() {
        return hotSpareDiskSpace;
    }

    public void setHotSpareDiskSpace(String hotSpareDiskSpace) {
        this.hotSpareDiskSpace = hotSpareDiskSpace;
    }

    public static class Addons {
        public boolean isThinEnabled() {
            return thinEnabled;
        }

        public void setThinEnabled(boolean thinEnabled) {
            this.thinEnabled = thinEnabled;
        }

        public boolean isCompressionEnabled() {
            return compressionEnabled;
        }

        public void setCompressionEnabled(boolean compressionEnabled) {
            this.compressionEnabled = compressionEnabled;
        }

        public boolean isSnapEnabled() {
            return snapEnabled;
        }

        public void setSnapEnabled(boolean snapEnabled) {
            this.snapEnabled = snapEnabled;
        }

        private boolean thinEnabled;

        public boolean isNonthinEnabled() {
            return nonthinEnabled;
        }

        public void setNonthinEnabled(boolean nonthinEnabled) {
            this.nonthinEnabled = nonthinEnabled;
        }

        private boolean nonthinEnabled;
        private boolean compressionEnabled;
        private boolean snapEnabled;

    }


    /**
     * Returns the Total Raid Group free space. 
     */
    public double getTotalRaidGroupFreeSpace() {

        double totalFreeSpace = 0;
        if (this.getRaidGroups() != null) { 
            for (RaidGroup raidGroup : this.getRaidGroups()) {
                double freeSpace = Double.parseDouble(raidGroup.getFreeSpace());
                totalFreeSpace+=freeSpace;
            }
        }
        
        return totalFreeSpace;
    }
    
    /**
     * Returns the Total Disk Pool Free Space. 
     */
    public double getTotalDiskPoolFreeSpace() {

        double totalFreeSpace = 0;
        if (this.getDiskPools() != null) { 
            for (DiskPool diskPool : this.getDiskPools()) {
                totalFreeSpace+= Double.parseDouble(diskPool.getRgFreeCapacity());
            }
        }
        
        return totalFreeSpace;
    }
    
    /**
     * Returns the Pools total allocated capacity. 
     */
    public double getTotalPoolAllocatedCapacity() {
        
        double allocatedCapacityTotal = 0;
        
        if (this.getPools() != null) {
            for (Pool pool : this.getPools()) {
                allocatedCapacityTotal+= Double.parseDouble(pool.getAllocatedCapacity());
            }
        }
        
        return allocatedCapacityTotal;
    }
    
    /**
     * Returns the Pools Total User Capacity. 
     */
    public double getTotalPoolUserCapacity() {
        
        double userCapacityTotal = 0;
        
        if (this.getPools() != null) {
            for (Pool pool : this.getPools()) {
                userCapacityTotal+= Double.parseDouble(pool.getUserCapacity());
            }
        }
        
        return userCapacityTotal;
    }    
    

    public double getTotalPoolRawCapacity() {
       
        double rawCapacityTotal = 0;
        
        if (this.getPools() != null) {
            for (Pool pool : this.getPools()) {
                rawCapacityTotal+= Double.parseDouble(pool.getRawCapacity());
            }
        }
        
        return rawCapacityTotal;        
    }
    
    /**
     * Returns the Controller for the spa Host. 
     */
    public Controller getControllerForHostSPA() {
        Controller spaController = null;
        
        if (this.getControllers() != null) {
            for (Controller controller : this.getControllers()) {
                if("SPA".equals(controller.getHostName())) {
                    spaController = controller;
                    break;
                }
            }
        }
        
        return spaController;
    }
    
    /**
     * Returns the Controller for the spb Host. 
     */
    public Controller getControllerForHostSPB() {
        Controller spbController = null;
        
        if (this.getControllers() != null) {
            for (Controller controller : this.getControllers()) {
                if("SPB".equals(controller.getHostName())) {
                    spbController = controller;
                    break;
                }
            }
        }
        
        return spbController;
    }    
    
    /**
     * Identifies the Management Controller by the Controller with the MANAGEMENT_CONTROLLER_OS_NAME and returns
     * that Controller. 
     */
    public Controller getManagementController() {
        Controller managementController = null;
        
        if (this.getControllers() != null) {
            for (Controller controller : this.getControllers()) {
                if(MANAGEMENT_CONTROLLER_OS_NAME.equals(controller.getOsName())) {
                    managementController = controller;
                    break;
                }
            }
        }
        
        return managementController;
    }
    
    /**
     * Returns the VNX Block Operating Environment Software Revision number.
     */
    public String getVnxBlockOperatingEnvironmentSoftwareRevision() {
        String vnxBlockOperatingEnvironmentVersion = null;
        if (this.getSoftwares() != null) {
            for (Software software : this.getSoftwares()) {
                if (PuppetEmcDevice.VNX_BLOCK_OPERATING_ENVIRONMENT.equals(software.getName())) {
                    vnxBlockOperatingEnvironmentVersion = software.getRevision();
                    break;
                }
            }
        }
        
        return vnxBlockOperatingEnvironmentVersion;
    }
    
    /**
     * Returns the number of Luns.
     */
    public int getNumberOfLuns() {
        int numOfLuns = 0;
        
        for (PuppetEmcDevice.Pool pool : this.getPools()) {
            if (pool.getMlus() != null) {
                numOfLuns+= pool.getMlus().size();
            }
        }
        
        return numOfLuns;
    }
    
    /**
     * Returns the MLUs User Capacity (MLU's are the LUNs in EMC VNX).  
     */
    public double getTotalPoolsMluUserCapacity() {
        double mluUserCapacity = 0;
        
        if (this.getPools() != null) {
            for (Pool pool : this.getPools()) {
                for (Pool.Mlu mlu : pool.getMlus()) {
                    mluUserCapacity += Double.parseDouble(mlu.getUserCapacity());
                }
            }
        }
        
       return mluUserCapacity;
    }
    
    /**
     * Returns the Pools Raw Capacity total. 
     */
    public double getTotalPoolsRawCapacity() {
        double poolsRawCapacity = 0;
        
        if (this.getPools() != null) {
            for (Pool pool : this.getPools()) {
                poolsRawCapacity += Double.parseDouble(pool.getRawCapacity());
            }
        }
        
       return poolsRawCapacity;
    }    

    //  Free Storage Pool = Free storage capacity (in GB) in all storage pools and non-empty RAID Groups.
    public double getSummaryFreeStoragePool() {
        return this.getTotalPoolRawCapacity() - this.getTotalPoolAllocatedCapacity();
    }
    
    //  Free Raw Disk = Physical capacity (in GB) of all unused disks, including disks in empty RAID Groups. 
    public double getSummaryFreeRawDisk() {
        double freeRawDisk = 0;
        
        double diskCapacityInMBs = 0; // In MBs!!!
        if (this.getDiskInfos() != null) {
            for (DiskInfo diskInfo : this.getDiskInfos()) {
                diskCapacityInMBs+= Double.parseDouble(diskInfo.getCapacityInMBs());
            }
        }
        
        return freeRawDisk;
    }

    /**
     * Calculated by adding the free storage pool space and the free file storage space.
     */
    public double getFreeDiskSpace() {
        return Double.parseDouble(this.getFreeSpaceForFile()) + Double.parseDouble(this.getFreeStoragePoolSpace());
    }
    
    /**
     * Returns the Pool with the given name.
     */
    public PuppetEmcDevice.Pool getPoolByName(String name) {
        PuppetEmcDevice.Pool poolWithName = null;
        if (name != null && this.getPools() != null && !this.getPools().isEmpty()) {
            for (PuppetEmcDevice.Pool pool : this.getPools()) {
                if (name.equals(pool.getName())) {
                    poolWithName = pool;
                    break;
                }
            }
        }
        return poolWithName;
    }
    
    /**
     * If name is not null and a matching Mlu name can be found it will return the first Mlu with the given name that 
     * is found. Note a Mlu is the equivelant of a Lun or a volume in ASM.
     * 
     * @param name the name of the Mlu to return.  
     * @return the Mlu whose name matches the name.
     */
    public PuppetEmcDevice.Pool.Mlu getMluByName(String name) {
        PuppetEmcDevice.Pool.Mlu foundMlu = null;
        
        if (name != null && this.getPools() != null && !this.getPools().isEmpty()) {
            for (PuppetEmcDevice.Pool pool : this.getPools()) {
                if (pool.getMlus() != null && !pool.getMlus().isEmpty()) {
                    for (PuppetEmcDevice.Pool.Mlu mlu : pool.getMlus()) {
                        if (name.equals(mlu.getName())) {
                            foundMlu = mlu;
                            break;
                        }
                    }
                }
            }
        }
        
        return foundMlu;
    }
    
    /**
     * Returns the software with the given name. 
     */
    public PuppetEmcDevice.Software getSoftwareByName(String name) {
        if(name != null && this.getSoftwares() != null && !this.getSoftwares().isEmpty()) {
            for (PuppetEmcDevice.Software software : this.getSoftwares()) {
                if (name.equals(software.getName())) {
                    return software;
                }
            }
        }
        return null;
    }
    
}
