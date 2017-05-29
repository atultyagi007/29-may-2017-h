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
 * A List of keys that can be used to get information from the Puppet Facts returned for an EqualLogic device.
 */
public interface PuppetEqualLogicKeys {
    
    public static final String CERTNAME = "certname";
    public static final String DEVICE_TYPE = "device_type";
    public static final String UPDATE_TIME = "update_time";
    public static final String GROUP_NAME = "Group Name";
    public static final String MANAGEMENT_IP = "Management IP";
    public static final String MEMBER_MODEL = "Member Model";
    public static final String STATUS = "Status";
    public static final String VERSION = "Version";
    public static final String GENERAL_SETTINGS = "General Settings";

    public static final String STATUS_ONLINE = "online";

    public interface GeneralSettings {
        public static final String GROUP_NAME = "Group Name";
        public static final String IP_ADDRESS = "IP Address";
        public static final String LOCATION = "Location";
    }
    
    public static final String STORAGE_POOLS = "Storage Pools";
    public static final String MEMBERS = "Members";
    public static final String TOTAL = "Total";
    public static final String COLLECTIONS = "Collections";
    
    public interface Collections {
        public static final String VOLUME_COLLECTIONS = "Volume Collections";
        public static final String CUSTOM_SNAPSHOT_COLLECTIONS = "Custom Snapshot Collections";
        public static final String SNAPSHOT_COLLECTIONS = "Snapshot Collections";
    }
    
    public static final String MODEL = "Model";
    public static final String GROUP_DISK_SPACE = "Group Disk Space";
    
    public interface GroupDiskSpace {
        public static final String SNAPSHOT_RESERVE = "Snapshot Reserve";
        public static final String REPLICATION_RESERVE = "Replication Reserve";
        public static final String DELEGATED = "Delegated";
        public static final String FREE = "Free";
        public static final String VOLUME_RESERVE = "Volume Reserve";
    }
    
    public static final String FREE_GROUP_SPACE = "Free Group Space";
    public static final String VOLUME_IQN_INFORMATION = "Volume_IQN_Information";
    
    public interface VolumeIqnInformation {
        public static final String TARGET_ISCSI_NAME = "TargetIscsiName";
        public static final String SIZE = "Size";
        public static final String SNAPSHOTS = "Snapshots";
        public static final String STATUS = "Status";
        public static final String PERMISSION = "Permission";
        public static final String TP = "TP";
        public static final String TEMPLATE = "Template";
    }
    
    public static final String VOLUMES_INFO = "VolumesInfo";
    
    public interface VolumesInfo {
        public static final String TOTAL_VOLUMES = "Total Volumes";
        public static final String ONLINE = "Online";
        public static final String ISCSI_CONNECTIONS = "ISCSI Connections";
        public static final String IN_USE = "In Use";
    }
    
    public static final String VOLUMES = "Volumes";
    public static final String SNAPSHOTS = "Snapshots";
    public static final String GROUPMEMBERS = "Group Members";
    public static final String SNAPSHOTS_INFO = "SnapshotsInfo";
    
    public interface Snapshots {
        public static final String SNAPSHOTS_INFO_ONLINE = "Online";
        public static final String SNAPSHOTS_INFO_IN_USE = "In Use";
    }
    
    public static final String NAME = "name";
    public static final String FW_VERSION = "fwversion";
    public static final String VOLUMES_PROPERTIES = "VolumesProperties";
    
    public interface VolumesProperties {
        public static final String STORAGE_POOL = "Storage Pool";
        public static final String REPORTED_SIZE = "Reported Size";
        public static final String VOLUME_RESERVE = "Volume Reserve";
        public static final String SNAPSHOT_RESERVE = "Snapshot Reserve";
        public static final String BORROWED_SPACE = "Borrowed Space";
        public static final String VOLUME_STATUS = "Volume Status";
        public static final String VOLUME_USE = "Volume Use";
        public static final String REPLICATION_PARTNER = "Replication Partner";
        public static final String SYNCREP_STATUS = "SyncRep Status";
        public static final String NUMBER_OF_SNSAPSHOT = "Number of Snapshot";
        public static final String NUMBER_OF_ONLINE_SNAPSHOTS = "Number of Online Snapshots";
        public static final String ISCSI_CONNECTIONS = "ISCSI Connections";
        public static final String SNAPSHOTS_IN_USE = "Snapshots in Use";
        public static final String TARGET_ISCSI_NAME = "TargetIscsiName";
    }
    
}
