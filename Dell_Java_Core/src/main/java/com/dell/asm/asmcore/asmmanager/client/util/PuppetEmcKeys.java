/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

/**
 * The Keys used to parse the Puppet Facts for an EMC Storage device.
 */
public class PuppetEmcKeys {

    public static final String ID = "Id";
    public static final String NAME = "Name";
    public static final String TYPE = "Type";
    public static final String WWN = "WWN";
    public static final String SERIAL_NUMBER = "SerialNumber";
    public static final String EMC_PART_NUMBER = "EMCPartNumber";
    public static final String MODEL_NUMBER = "ModelNumber";
    public static final String HIGH_WATERMARK = "HighWatermark";
    public static final String LOW_WATERMARK = "LowWatermark";
    public static final String PAGE_SIZE = "PageSize";
    public static final String PHYSICAL_NODE = "PhysicalNode";
    public static final String UUID_BASE_ADDRESS = "UUIDBaseAddress";
    public static final String CURRENT_SYSTEM_TYPE = "CurrentSystemType";
    public static final String CONFIGURED_SYSTEM_TYPE = "ConfiguredSystemType";
    public static final String CLASSIC_CLI_STATE = "ClassicCLIState";
    public static final String HW_SYSTEM_TYPE = "HwSystemType";
    public static final String HW_TYPE = "HwType";
    public static final String NTP = "NTP";

    public static final String SOFTWARES_DATA = "softwares_data";    
    public static final String SOFTWARES = "Softwares";
    public interface Softwares {
        public static final String NAME = "Name";
        public static final String REVISION = "Revision";
        public static final String DESCRIPTION = "Description";
        public static final String IS_ACTIVE = "IsActive";
        public static final String COMMIT_REQUIRED = "CommitRequired";
        public static final String REVERT_POSSIBLE = "RevertPossible";
        public static final String REQUIRED_PACKAGES = "RequiredPackages";
        public static final String IS_INSTALLATION_COMPLETE = "IsInstallationComplete";
        public static final String IS_SYSTEM_SOFTWARE = "IsSystemSoftware";
    }
    
    public static final String HBA_INFO_DATA = "hbainfo_data";
    public static final String HBA_INFO = "HBAInfo";
    public interface HbaInfo {
        public static final String NUMBER_OF_HBA_PORTS = "NumberOfHBAPorts";
        public static final String HOST_LOGIN_STATUS = "HostLoginStatus";
        public static final String HOST_MANAGEMENT_STATUS = "HostManagementStatus";
        public static final String IS_ATTACHED_HOST = "IsAttachedHost";
        public static final String HBA_PORTS_INFO = "hba_ports_info";
        
        public interface HbaPortsInfo {
            public static final String WWN = "WWN";
            public static final String VENDOR_DESCRIPTION = "VendorDescription";
            public static final String NUMBER_OF_SP_PORTS = "NumberOfSPPorts";
        }
    }
    
    public static final String CONTROLLERS_DATA = "controllers_data";
    public static final String CONTROLLERS = "controllers";
    public interface Controllers {
        public static final String HOST_NAME = "HostName";
        public static final String HOST_IP_ADDRESS = "HostIPAddress";
        public static final String HOST_ID = "HostID";
        public static final String WWN = "WWN";
        public static final String OS_NAME = "OSName";
        public static final String IS_MANUALLY_REGISTERED = "IsManuallyRegistered";
        public static final String IS_EXPANDABLE = "IsExpandable";
        public static final String POLL_TYPE = "PollType";
        public static final String IF_SERVER_SUPPORT_SMART_POLL = "IfServerSupportSmartPoll";
        public static final String NUMBER_OF_LUNS = "NumberOfLUNs";
        public static final String IS_MANAGED = "IsManaged";
        public static final String OS_VERSION_AS_STRING = "OSVersionAsString";
        public static final String HBA_INFO = "HBAInfo";
        public static final String LUN_USAGE_INFO = "LunUsageInfo";
    }
    
    public static final String DISK_INFO_DATA = "disk_info_data";
    public static final String DISK_INFO = "disk_info";
    public interface DiskInfo {
        public static final String NAME = "Name";
        public static final String BUS = "Bus";
        public static final String ENCLOSURE = "Enclosure";
        public static final String SLOT = "Slot";
        public static final String BANK = "Bank";
        public static final String BANK_AS_INTEGER = "BankAsInteger";
        public static final String STATE = "State";
        public static final String DISK_STATE = "DiskState";
        public static final String CAPACITY_IN_MBS = "CapacityInMBs";
        public static final String NUMBER_OF_USER_SECTORS = "NumberOfUserSectors";
        public static final String TYPE = "Type";
        public static final String WRITES = "Writes";
        public static final String READS = "Reads";
        public static final String BLOCKS_READ = "BlocksRead";
        public static final String BLOCKS_WRITTEN = "BlocksWritten";
        public static final String WRITE_RETRIES = "WriteRetries";
        public static final String READ_RETRIES = "ReadRetries";
        public static final String IDLE_TICKS = "IdleTicks";
        public static final String BUSY_TICKS = "BusyTicks";
        public static final String ZERO_MARK = "ZeroMark";
        public static final String CURRENT_SPEED = "CurrentSpeed";
        public static final String MAXIMUM_SPEED = "MaximumSpeed";
        public static final String REPLACING = "Replacing";
        public static final String HARD_READ_ERRORS = "HardReadErrors";
        public static final String HARD_WRITE_ERRORS = "HardWriteErrors";
        public static final String SOFT_READ_ERRORS = "SoftReadErrors";
        public static final String SOFT_WRITE_ERRORS = "SoftWriteErrors";
        public static final String IS_HW_SPIN_DOWN_QUALIFIED = "IsHWSpinDownQualified";
        public static final String IS_HW_SPIN_DOWN_CAPABLE = "IsHWSpinDownCapable";
        public static final String IS_VALUT_DRIVE = "IsVaultDrive";
        public static final String POWER_SAVING_STATE = "PowerSavingState";
        public static final String DISK_DRIVE_CATEGORY = "DiskDriveCategory";
    }
    
    public static final String RAID_GROUPS_DATA = "raid_groups_data";
    public static final String RAID_GROUPS = "raid_groups";
    public interface RaidGroups {
        public static final String ID = "ID";
        public static final String CAPACITY = "Capacity";
        public static final String FREE_SPACE = "FreeSpace";
        public static final String LARGEST_UNBOUND_SEGMENT_SIZE = "LargestUnboundSegmentSize";
        public static final String RG_RAID_TYPE = "RGRaidType";
        public static final String TYPE = "Type";
        public static final String OPERATION_PRIORITY = "OperationPriority";
        public static final String WILL_BE_REMOVED = "WillBeRemoved";
        public static final String IS_PRIVATE = "IsPrivate";
        public static final String STATE = "State";
        public static final String MAX_LUNS = "MaxLuns";
        public static final String RAW_CAPACITY_BLOCKS = "RawCapacityBlocks";
        public static final String PERCENT_DEGRAGMENTED = "PercentDefragmented";
        public static final String PERCENT_EXPANDED = "PercentExpanded";
        public static final String LUN_EXPANSION_ENABLED = "LunExpansionEnabled";
        public static final String LEGAL_RAID_TYPES = "LegalRaidTypes";
        public static final String RG_USER_DEFINED_POWER_SAVING = "RGUserDefinedPowerSaving";
        public static final String RG_USER_DEFINED_LATENCY_TOLERANCE = "RGUserDefinedLatencyTolerance";
        public static final String IS_RG_HARDWARE_POWER_SAVING_ELIGIBLE = "IsRGHardwarePowerSavingEligible";
        public static final String IS_RG_POWER_SAVING_ELIGIBLE = "IsRGPowerSavingEligible";
        public static final String IS_RG_IN_STAND_BY_STATE = "IsRGInStandByState";
        public static final String ELEMENT_SIZE = "ElementSize";
        public static final String DISKS = "disks";
        
        public interface Disks {
            public static final String NAME = "Name";
            public static final String BUS = "Bus";
            public static final String ENCLOSURE = "Enclosure";
            public static final String SLOT = "Slot";
            public static final String BANK = "Bank";
            public static final String BANK_AS_INTEGER = "BankAsInteger";
        }
    }
    
    public static final String DISK_POOLS_DATA = "disk_pools_data";
    public static final String DISK_POOLS = "disk_pools";
    public interface DiskPools {
        public static final String KEY = "Key";
        public static final String NAME = "Name";
        public static final String NUMBER = "Number";
        public static final String RAW_CAPACITY = "RawCapacity";
        public static final String USER_CAPACITY = "UserCapacity";
        public static final String RG_RAW_CAPACITY = "RGRawCapacity";
        public static final String RG_USER_CAPACITY = "RGUserCapacity";
        public static final String RG_FREE_CAPACITY = "RGFreeCapacity";
        public static final String POOLS = "Pools";
        public static final String DISKS = "Disks";
        
        public interface Disks {
            public static final String NAME = "Name";
            public static final String BUS = "Bus";
            public static final String ENCLOSURE = "Enclosure";
            public static final String SLOT = "Slot";
            public static final String BANK = "Bank";
            public static final String BANK_AS_INTEGER = "BankAsInteger";
        }
        
        public static final String RAID_GROUPS = "RAIDGroups";
    }
    
    public static final String POOLS_DATA = "pools_data";
    public static final String POOLS = "pools";
    public interface Pools {
        public static final String KEY = "Key";
        public static final String NAME = "Name";
        public static final String ID = "ID";
        public static final String RAID_TYPE = "RAIDType";
        public static final String LIBRARY_NAME = "LibraryName";
        public static final String CREATION_TIME = "CreationTime";
        public static final String STATE = "State";
        public static final String STATUS = "Status";
        public static final String RAW_CAPACITY = "RawCapacity";
        public static final String USER_CAPACITY = "UserCapacity";
        public static final String ALLOCATED_CAPACITY = "AllocatedCapacity";
        public static final String SHARED_CAP_ALERT_LEVEL_PRCNT = "SharedCapAlertLevelPrcnt";
        public static final String COMPRESSION_PAUSE_PRCNT = "CompressionPausePrcnt";
        public static final String COMPRESSION_HALT_PRCNT = "CompressionHaltPrcnt";
        public static final String ACTIVE_OPERATION = "ActiveOperation";
        public static final String ACTIVE_OPERATION_STATE = "ActiveOperationState";
        public static final String ACTIVE_OPERATION_COMPLETE_PRCNT = "ActiveOperationCompletePrcnt";
        public static final String IS_FAULTED = "IsFaulted";
        public static final String EFD_CACHE_STATE = "EFDCacheState";
        public static final String EFD_CACHE_CURRENT_STATE = "EFDCacheCurrentState";
        public static final String SNAP_HARVEST_HIGH_THRESHOLD = "SnapHarvestHighThreshold";
        public static final String SNAP_HARVEST_LOW_THRESHOLD = "SnapHarvestLowThreshold";
        public static final String POOL_HARVEST_HIGH_THRESHOLD = "PoolHarvestHighThreshold";
        public static final String POOL_HARVEST_LOW_THRESHOLD = "PoolHarvestLowThreshold";
        public static final String SNAP_HARVESTING_ENABLED = "SnapHarvestingEnabled";
        public static final String POOL_HARVESTING_ENABLED = "PoolHarvestingEnabled";
        public static final String SNAP_HAREVESTING_STATE = "SnapHarvestingState";
        public static final String POOL_HARVESTING_STATE = "PoolHarvestingState";
        public static final String TOTAL_SUBSCRIBED_CAPACITY = "TotalSubscribedCapacity";
        public static final String PRIMARY_SUBSCRIBED_CAPACITY = "PrimarySubscribedCapacity";
        public static final String SECONDARY_SUBSCRIBED_CAPACITY = "SecondarySubscribedCapacity";
        public static final String META_DATA_SUBSCRIBED_CAPACITY = "MetaDataSubscribedCapacity";
        public static final String PRIMARY_DATA_CONSUMED_CAPACITY = "PrimaryDataConsumedCapacity";
        public static final String SECONDARY_DATA_CONSUMED_CAPACITY = "SecondaryDataConsumedCapacity";
        public static final String META_DATA_CONSUMED_CAPACITY = "MetaDataConsumedCapacity";
        public static final String COMPRESSION_SAVINGS = "CompressionSavings";
        public static final String DISK_POOl_KEYS = "DiskPoolKeys";
        public static final String RAID_GROUPS = "RAIDGroups";
        public static final String MLUS = "MLUs";
        
        public interface Mlus {
            public static final String WWN = "WWN";
            public static final String OBJECT_ID = "ObjectID";
            public static final String POOL_KEY = "PoolKey";
            public static final String LU_TYPE = "LUType";
            public static final String NAME = "Name";
            public static final String NUMBER = "Number";
            public static final String CREATION_TIME = "CreationTime";
            public static final String CURRENT_OWNER = "CurrentOwner";
            public static final String DEFAULT_OWNER = "DefaultOwner";
            public static final String ALLOCATION_OWNER = "AllocationOwner";
            public static final String USER_CAPACITY = "UserCapacity";
            public static final String CONSUMED_CAPACITY = "ConsumedCapacity";
            public static final String SUBSCRIBED_CAPACITY = "SubscribedCapacity";
            public static final String IS_FAULTED = "IsFaulted";
            public static final String IS_TRANSITIONING = "IsTransitioning";
            public static final String STATE = "State";
            public static final String INTERNAL_STATE = "InternalState";
            public static final String CREATING_DRIVER_NAME = "CreatingDriverName";
            public static final String CONSUMING_DRIVER_NAME = "ConsumingDriverName";
            public static final String IS_AUTO_ASSIGNED_ENABLED = "IsAutoAssignEnabled";
            public static final String IS_AUTO_TRESPASS_ENABLED = "IsAutoTrespassEnabled";
            public static final String OPERATION_TYPE = "OperationType";
            public static final String OPERATION_COMPLETE_PRCNT = "OperationCompletePrcnt";
            public static final String OPERATION_STATUS = "OperationStatus";
            public static final String ALIGNMENT_OFFSET = "AlignmentOffset";
            public static final String RAID_TYPE = "RAIDType";
            public static final String HOST_BLOCKS_READ_SPA = "HostBlocksReadSPA";
            public static final String HOST_BLOCKS_READ_SPB = "HostBlocksReadSPB";
            public static final String HOST_BLOCKS_WRITTEN_SPA = "HostBlocksWrittenSPA";
            public static final String HOST_BLOCKS_WRITTEN_SPB = "HostBlocksWrittentSPB";
            public static final String HOST_READ_REQUESTS_SPA = "HostReadRequestsSPA";
            public static final String HOST_READ_REQUESTS_SPB = "HostReadRequestsSPB";
            public static final String HOST_WRITE_REQUESTS_SPA = "HostWriteRequestsSPA";
            public static final String HOST_WRITE_REQUESTS_SPB = "HostWriteRequestsSPB";
            public static final String BUSY_TICKS_SPA = "BusyTicksSPA";
            public static final String BUSY_TICKS_SPB = "BusyTicksSPB";
            public static final String IDLE_TICKS_SPA = "IdleTicksSPA";
            public static final String IDLE_TICKS_SPB = "IdleTicksSPB";
            public static final String EXPLICIT_TRESPASSES_SPA = "ExplicitTrespassesSPA";
            public static final String EXPLICIT_TRESPASSES_SPB = "ExplicitTrespassesSPB";
            public static final String IMPLICIT_TRESPASSES_SPA = "ImplicitTrespassesSPA";
            public static final String IMPLICIT_TRESPASSES_SPB = "ImplicitTrespassesSPB";
            public static final String NON_ZERO_REQ_CNT_ARRIVALS_SPA = "NonZeroReqCntArrivalsSPA";
            public static final String NON_ZERO_REQ_CNT_ARRIVALS_SPB = "NonZeroReqCntArrivalsSPB";
            public static final String SUM_OUTSTANDING_REQS_SPA = "SumOutstandingReqsSPA";
            public static final String SUM_OUTSTANDING_REQS_SPB = "SumOutstandingReqsSPB";
            public static final String RECOVERY_STATE = "RecoveryState";
            public static final String DATA_STATE = "DataState";
            public static final String IS_INTIALIZING = "IsInitializing";
            public static final String THROTTLE_RATE = "ThrottleRate";
            public static final String ROOT_SLICE_ID = "RootSliceID";
            public static final String ROOT_SLICE_OFFSET = "RootSliceOffset";
            public static final String ROOT_SLICE_LENGTH = "RootSliceLength";
            public static final String ROOT_SLICE_POSITION = "RootSlicePosition";
            public static final String CBFS_FILE_SYSTEM_ID = "CBFSFileSystemID";
            public static final String FIRST_SLICE_ID = "FirstSliceID";
            public static final String FIRST_SLICE_OFFSET = "FirstSliceOffset";
            public static final String FIRST_SLICE_LENGTH = "FirstSliceLength";
            public static final String FIRST_SLICE_POSITION = "FirstSlicePosition";
            public static final String CBFS_FILE_INODE_NUMBER = "CBFSFileInodeNumber";
            public static final String CBFS_FILE_GENERATION_NUMBER = "CBFSFileGenerationNumber";
            public static final String FILE_SYSTEM_OBJECT_ID = "FileSystemObjectID";
            public static final String FILE_SYSTEM_ID = "FileSystemID";
            public static final String MLU_OBJECT_ID = "MLUObjectID";
            public static final String FILE_OBJECT_ID = "FileObjectID";
            public static final String IS_COMPRESSED = "IsCompressed";
            public static final String TIER_PLACEMENT_PREFERENCE = "TierPlacementPreference";
            public static final String AUTO_TIERING_POLICTY = "AutoTieringPolicy";
            public static final String SECOND_SLICE_ID = "SecondSliceID";
            public static final String SECOND_SLICE_OFFSET = "SecondSliceOffset";
            public static final String SECOND_SLICE_LENGTH = "SecondSliceLength";
            public static final String SECOND_SLICE_POSITION = "SecondSlicePosition";
            public static final String IS_INTERNAL = "IsInternal";
            public static final String HARVEST_PRIORITY = "HarvestPriority";
            public static final String SNAP_COUNT = "SnapCount";
            public static final String SNAP_LUN_COUNT = "SnapLunCount";
            public static final String IS_ADVSNAP_ATTACHED_ALLOWED = "IsAdvsnapAttachedAllowed";
            public static final String SECONDARY_CONSUMED_CAPACITY = "SecondaryConsumedCapacity";
            public static final String PRIMARY_CONSUMED_CAPACITY = "PrimaryConsumedCapacity";
            public static final String UNCOMMITTED_CONSUMPTION = "UncommittedConsumption";
            public static final String META_DATA_CONSUMED_CAPACITY = "MetaDataConsumedCapacity";
            public static final String SECONDARY_SUBSCRIBED_CAPACITY = "SecondarySubscribedCapacity";
            public static final String META_DATA_SUBSCRIBED_CAPACITY = "MetaDataSubscribedCapacity";
            public static final String COMPRESSION_SAVINGS = "CompressionSavings";
            public static final String IO_DISPOSITION = "IODisposition";
            public static final String FILE_MODE = "FileMode";
            public static final String COMP_REV_ID = "CompRevId";
            public static final String ALLOCATION_POLICY = "AllocationPolicy";
            public static final String AUTO_TIERING_TIER_INFOS = "AutoTieringTierInfos";
        }
     
        public static final String INTERNAL_LUNS = "InternalLUNs"; 
        public static final String AUTO_TIERING_TIER_INFOS = "AutoTieringTierInfos";
    }

    public static final String ADDONS_DATA = "addons_data";
    public static final String ADDONS = "addons";
    public interface Addons {
        public static final String THIN = "thin";
        public static final String NONTHIN = "nonthin";
        public static final String COMPRESSION = "compression";
        public static final String SNAP = "snap";
    }

    public static final String FREE_STORAGE_POOL_SPACE = "Free Storage Pool Space";
    public static final String CONSUMED_DISK_SPACE = "Consumed Disk Space";
    public static final String FREE_SPACE_FOR_FILE = "Free Space for File";
    public static final String HOT_SPARE_DISKS = "HotspareDisks";
    public static final String RAW_DISK_SPACE = "Raw Disk Space";
    public static final String HOT_SPARE_DISK_SPACE = "HotspareDiskSpace";
    
    public static final String POOL_LIST = "pool_list";
    public static final String POOL = "pool";
}
