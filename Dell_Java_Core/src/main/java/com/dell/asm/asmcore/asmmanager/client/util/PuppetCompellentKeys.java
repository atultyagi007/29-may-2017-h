/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

/**
 * @author jimmy_jarrett
 *
 */
public interface PuppetCompellentKeys {

    public static final String MODEL = "model";
    public static final String NAME = "name";
    public static final String CERT_NAME = "certname";
    public static final String DEVICE_TYPE = "device_type";
    public static final String UPDATE_TIME = "update_time";
    public static final String SYSTEM_SERIAL_NUMBER = "system_SerialNumber";
    public static final String SYSTEM_NAME = "system_Name";
    public static final String SYSTEM_MANAGEMENT_IP = "system_ManagementIP";
    public static final String SYSTEM_VERSION = "system_Version";
    public static final String SYSTEM_OPERATION_MODE = "system_OperationMode";
    public static final String SYSTEM_PORTS_BALANCED = "system_PortsBalanced";
    public static final String SYSTEM_MAIL_SERVER = "system_MailServer";
    public static final String SYSTEM_BACKUP_MAIL_SERVER = "system_BackupMailServer";
    // NOTE: Controller is not used at this time, but may be later, so leaving as this is first drop of code
//    public static final String CONTROLLER = "controller_"; // format of controller_1_XXX
//        public static final String CONTROLLER_CONTROLLER_INDEX = "_ControllerIndex"; // controller_1_ControllerIndex
//        public static final String CONTROLLER_NAME = "_Name"; // controller_1_Name
//        public static final String CONTROLLER_LEADER = "_Leader";  // controller_1_Leader
//        public static final String CONTROLLER_STATUS = "_Status";  // controller_1_Status
//        public static final String CONTROLLER_LOCAL_PORT_CONDITION = "_LocalPortCondition";  // controller_1_LocalPortCondition
//        public static final String CONTROLLER_VERSION = "_Version";  // controller_1_Version
//        public static final String CONTROLLER_DOMAIN_NAME = "_DomainName";  // controller_1_DomainName
//        public static final String CONTROLLER_PRIMARY_DNS = "_PrimaryDNS";  // controller_1_PrimaryDNS
//        public static final String CONTROLLER_CONTROLLER_IP_ADDRESS = "_ControllerIPAddress";  // controller_1_ControllerIPAddress
//        public static final String CONTROLLER_CONTROLLER_IP_GATEWAY = "_ControllerIPGateway";  // controller_1_ControllerIPGateway
//        public static final String CONTROLLER_CONTROLLER_IP_MASK = "_ControllerIPMask";  // controller_1_ControllerIPMask
//        public static final String CONTROLLER_IPC_IP_ADDRESS = "_IpcIPAddress";  // controller_1_IpcIPAddress
//        public static final String CONTROLLER_IPC_IP_GATEWAY = "_IpcIPGateway";  // controller_1_IpcIPGateway
//        public static final String CONTROLLER_IPC_IP_MASK = "_IpcIPMask";  // controller_1_IpcIPMask
//        public static final String CONTROLLER_LAST_BOOT_TIME = "_LastBootTime";  // controller_1_LastBootTime
    
    public interface DiskFolder {
        public static final String DISK_FOLDER_DATA = "diskfolder_data";  // format of diskfolder_1_XXX 
        public static final String DISK_FOLDER = "diskfolder";  // format of diskfolder_1_XXX
        public static final String INDEX = "Index"; // diskfolder_1_Index
        public static final String NAME = "Name"; //diskfolder_1_Name
        public static final String NUM_MANAGED = "NumManaged"; // diskfolder_1_NumManaged
        public static final String NUM_SPARE = "NumSpare"; // diskfolder_1_NumSpare
        public static final String NUM_STORAGE_TYPES = "NumStorageTypes"; // diskfolder_1_NumStorageTypes
        public static final String TOTAL_AVAILABLE_SPACE = "TotalAvailableSpace"; //diskfolder_1_TotalAvailableSpace
        public static final String AVAILABLE_SPACE_BLOCKS = "AvailableSpaceBlocks"; // diskfolder_1_AvailableSpaceBlocks
        public static final String ALLOCATED_SPACE = "AllocatedSpace"; // diskfolder_1_AllocatedSpace
        public static final String ALLOCATED_SPACE_BLOCKS = "AllocatedSpaceBlocks"; // diskfolder_1_AllocatedSpaceBlocks
        public static final String STORAGE_TYPES = "Storage_Types";    
        public static final String STORAGE_TYPE = "StorageType";  // diskfolder_1_StorageType_1_Index
        
        public interface StorageType {
            public static final String INDEX = "Index"; // diskfolder_1_StorageType_1_Index
            public static final String NAME = "Name"; // diskfolder_1_StorageType_1_Name
            public static final String REDUNDANCY = "Redundancy"; // diskfolder_1_StorageType_1_Redundancy
            public static final String PAGE_SIZE = "PageSize"; // diskfolder_1_StorageType_1_PageSize
            public static final String PAGE_SIZE_BLOCKS = "PageSizeBlocks"; //diskfolder_1_StorageType_1_PageSizeBlocks
            public static final String SPACE_USED = "SpaceUsed"; //diskfolder_1_StorageType_1_SpaceUsed
            public static final String SPACE_USED_BLOCKS = "SpaceUsedBlocks"; //diskfolder_1_StorageType_1_SpaceUsedBlocks
            public static final String SPACE_ALLOCATED = "SpaceAllocated"; //diskfolder_1_StorageType_1_SpaceAllocated
            public static final String SPACE_ALLOCATED_BLOCKS = "SpaceAllocatedBlocks"; // diskfolder_1_StorageType_1_SpaceAllocatedBlocks
        }
    }    
    
    public interface SystemData {
        public static final String SYSTEM_DATA = "system_data";
        public static final String SYSTEM = "system";
        public static final String SERIAL_NUMBER = "SerialNumber";
        public static final String NAME = "Name";
        public static final String MANAGEMENT_IP = "ManagementIP";
        public static final String VERSION = "Version";
        public static final String OPERATION_MODE = "OperationMode";
        public static final String PORTS_BALANCED = "PortsBalanced";
        public static final String MAIL_SERVER = "MailServer";
        public static final String BACKUP_MAIL_SERVER = "BackupMailServer";
    }
    
    public interface ReplayProfile {
        public static final String REPLAY_PROFILE_DATA = "replayprofile_data";
        public static final String REPLAY_PROFILE = "replayprofile";
        public static final String INDEX = "Index";
        public static final String NAME = "Name";
        public static final String TYPE = "Type";
        public static final String NUM_RULES = "NumRules";
        public static final String NUM_VOLUMES = "NumVolumes";
        public static final String SCHEDULE = "Schedule";
        public static final String VOLUMES = "Volumes";
        public static final String VOLUME = "Volume";
        
        public interface Volume {
            public static final String INDEX = "Index";
            public static final String NAME = "Name";
            public static final String DEVICE_ID = "DeviceID";
            public static final String SERIAL_NUMBER = "SerialNumber";
        }
    }
            
    public interface ServerData {
    
        public static final String SERVER_DATA = "server_data";
        public static final String SERVER = "server";
        public static final String INDEX = "Index";
        public static final String NAME = "Name";
        public static final String FOLDER = "Folder";
        public static final String OS_INDEX = "OsIndex";
        public static final String OS = "OS";
        public static final String TYPE = "Type";
        public static final String PARENT_INDEX = "ParentIndex";
        public static final String PARENT = "Parent";
        public static final String WWN_LIST = "WWN_List";
        public static final String FOLDER_INDEX = "FolderIndex";
        public static final String CONNECT_STATUS = "ConnectStatus";
        public static final String TRANSPORT_TYPE = "TransportType";
        public static final String MAPPINGS = "Mappings";
        public static final String MAPPING = "mapping";
        
        public interface Mapping {
            public static final String INDEX = "Index";
            public static final String VOLUME = "Volume";
            public static final String DEVICE_ID = "DeviceID";
            public static final String SERIAL_NUMBER = "SerialNumber";
            public static final String LOCAL_PORT = "LocalPort";
            public static final String REMOTE_PORT = "RemotePort";
            public static final String LUN = "Lun";
        }
    }
    
    public interface StorageProfile { 
                
        public static final String STORAGE_PROFILE_DATA = "storageprofile_data";
        public static final String STORAGE_PROFILE = "storageprofile";
        public static final String INDEX = "Index";
        public static final String NAME = "Name";
        public static final String NUM_VOLUMES = "NumVolumes";
        public static final String REDUNDANT_WRITABLE = "RedundantWritable";
        public static final String REDUNDANT_HISTORICAL = "RedundantHistorical";
        public static final String NON_REDUNDANT_WRITABLE = "NonRedundantWritable";
        public static final String NON_REDUNDANT_HISTORICAL = "NonRedundantHistorical";
        public static final String DUAL_REDUNDANT_WRITABLE = "DualRedundantWritable";
        public static final String DUAL_HISTORICAL = "DualHistorical";
        public static final String VOLUMES = "Volumes";
        public static final String VOLUME = "Volume";
        
        public interface Volume { 
           public static final String INDEX = "Index";
           public static final String NAME = "Name";
           public static final String DEVICE_ID = "DeviceID";
           public static final String SERIAL_NUMBER = "SerialNumber";
        }
    }
                 
    public interface Volume {
         public static final String VOLUME_DATA = "volume_data";
         public static final String VOLUME = "volume";
         public static final String INDEX = "Index";
         public static final String NAME = "Name";
         public static final String STATUS = "Status";
         public static final String CONFIG_SIZE = "ConfigSize";
         public static final String ACTIVE_SIZE = "ActiveSize";
         public static final String REPLAY_SIZE = "ReplaySize";
         public static final String FOLDER = "Folder";
         public static final String STORAGE_PROFILE = "StorageProfile";
         public static final String DEVICE_ID = "DeviceID";
         public static final String SERIAL_NUMBER = "SerialNumber";
         public static final String CONFIG_SIZE_BLOCKS = "ConfigSizeBlocks";
         public static final String ACTIVE_SIZE_BLOCKS = "ActiveSizeBlocks";
         public static final String REPLAY_SIZE_BLOCKS = "ReplaySizeBlocks";
         public static final String MAX_WRITE_SIZE_BLOCKS = "MaxWriteSizeBlocks";
         public static final String READ_CACHE = "ReadCache";
         public static final String WRITE_CACHE = "WriteCache";
         public static final String MAPPINGS = "Mappings";
         
         public interface Mapping {
             public static final String INDEX =  "Index";
             public static final String SERVER = "Server";
             public static final String LOCAL_PORT = "LocalPort";
             public static final String REMOTE_PORT = "RemotePort";
             public static final String LUN = "Lun";
         }
    }
                 
}