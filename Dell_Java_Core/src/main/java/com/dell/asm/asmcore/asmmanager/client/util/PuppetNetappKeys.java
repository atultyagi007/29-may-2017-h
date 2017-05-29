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
 * Keys used to access puppet facts for a Netapp device.
 */
public interface PuppetNetappKeys {

    public final String AGGREGATE_DATA = "aggregate_data";

    public interface AggregateData {
        public final String NAME = "Name";
        public final String STATE = "State";
        public final String DISK_COUNT = "disk-count";
        public final String VOLUME_COUNT = "Volume-count";
        public final String SIZE_TOTAL = "size-total";
        public final String SIZE_USED = "size-used";
        public final String SIZE_PERCENTAGE_USED = "size-percentage-used";
        public final String SIZE_AVAILABLE = "size-available";
    }
        
    public final String CLIENT_CERT = "clientcert";
    public final String CLIENT_NOOP = "clientnoop";
    public final String CLIENT_VERSION = "clientversion";
    public final String DISK_DATA = "disk_data";

    public interface DiskData {
        public final String NAME = "Name";
        public final String SERIAL_NUMBER = "serial-number";
        public final String DISK_MODEL = "disk-model"; 
    }

    public final String DOMAIN = "domain";
    public final String FQDN = "fqdn";
    public final String HARDWARE_ISA = "hardwareisa";
    public final String HOST_NAME = "hostname";
    public final String INTERFACE_IPS = "interface_ips";
    public final String INTERFACES = "interfaces";
    public final String IPADDRESS = "ipaddress";
    public final String IPADDRESS_C0A = "ipaddress_c0a";
    public final String IPADDRESS_C0B = "ipaddress_c0b";
    public final String IPADDRESS_EOM = "ipaddress_e0M";
    public final String IPADDRESS_EOP = "ipaddress_e0P";
    public final String IPADDRESS_FCOE_CIFS_NFS = "ipaddress_FCoE_CIFS_NFS";
    public final String IPADDRESS_ONE_GB_VIF = "ipaddress_OneGBvif";
    public final String IS_CLUSTERED = "is_clustered";
    public final String LUN_DATA = "lun_data";
    
    public interface LunData {
        public final String PATH = "path";
        public final String SIZE = "size";
        public final String SIZE_USED = "size-used";
        public final String MAPPED = "mapped";
        public final String STATE = "state";
        public final String READ_ONLY = "read-only";
        public final String SPACE_RESERVE_ENABLED = "spacereserve-enabled";
    }
    
    public final String MAC_ADDRESS = "macaddress";
    public final String MAC_ADDRESS_C0A = "macaddress_c0a";
    public final String MAC_ADDRESS_C0B = "macaddress_c0b";
    public final String MAC_ADDRESS_E0A = "macaddress_e0a";
    public final String MAC_ADDRESS_E0B = "macaddress_e0b";
    public final String MAC_ADDRESS_E0M = "macaddress_e0M";
    public final String MAC_ADDRESS_E0P = "macaddress_e0P";
    public final String MAC_ADDRESS_E1A = "macaddress_e1a";
    public final String MAC_ADDRESS_E1B = "macaddress_e1b";
    public final String MAC_ADDRESS_FCOE_CIFS_NFS = "macaddress_FCoE_CIFS_NFS";
    public final String MAC_ADDRESS_ONE_GB_VIF = "macaddress_OneGBvif";
    public final String MANUFACTURER = "manufacturer";
    public final String MEMORY_SIZE = "memorysize";
    public final String MEMORY_SIZE_MB = "memorysize_mb";
    public final String MTU_C0A = "mtu_c0a";
    public final String MTU_C0B = "mtu_c0b";
    public final String NET_MASK = "netmask";
    public final String NET_MASK_C0A = "netmask_c0a";
    public final String NET_MASK_C0B = "netmask_c0b";
    public final String NET_MASK_E0M = "netmask_e0M";
    public final String NET_MASK_E0P = "netmask_e0P";
    public final String NET_MASK_FCOE_CIFS_NFS = "netmask_FCoE_CIFS_NFS";
    public final String NET_MASK_ONE_GB_VIF = "netmask_OneGBvif";
    public final String OPERATING_SYSTEM = "operatingsystem";
    public final String OPERATING_SYSTEM_RELEASE = "operatingsystemrelease";
    public final String PARTNER_SERIAL_NUMBER = "partner_serial_number";
    public final String PARTNER_SYSTEM_ID = "partner_system_id";
    public final String PROCESSOR_COUNT = "processorcount";
    public final String PRODUCT_NAME = "productname";
    public final String SERIAL_NUMBER = "serialnumber";
    public final String SYSTEM_MACHINE_TYPE = "system_machine_type";
    public final String SYSTEM_REVISION = "system_revision";
    public final String TOTAL_AGGREGATES = "total_aggregates";
    public final String TOTAL_DISKS = "total_disks";
    public final String TOTAL_LUNS = "total_luns";
    public final String TOTAL_VOLUMES = "total_volumes";
    public final String UNIQUE_ID = "uniqueid";
    public final String VERSION = "version";
    public final String NAME = "name";
    public final String VOLUME_DATA = "volume_data";
    
    public interface VolumeData {
        public final String NAME = "name";
        public final String SIZE_TOTAL = "size-total";
        public final String SIZE_AVAILABLE = "size-available";
        public final String SIZE_USED = "size-used";
        public final String TYPE = "type";
        public final String STATE = "state";
        public final String SPACE_RESERVE_ENABLED = "spacereserve-enabled"; 
    }
}
