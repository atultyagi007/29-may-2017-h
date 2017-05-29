/**************************************************************************
 *   Copyright (c) 2013 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;


import java.util.Arrays;
import java.util.List;

public class ServiceTemplateSettingIDs {
    // The default value for enumerated values is the empty string;
    // the GUI will require that the user select a different value.
    public static final String SERVICE_TEMPLATE_SELECT_ID = "";
    public static final String SERVICE_TEMPLATE_SELECT = "Select...";

    // The default value for server pool is -1 which means the global pool
    public static final String SERVICE_TEMPLATE_SERVER_POOL_ALL_ID = "-2";
    public static final String SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID = "-1";
    public static final String SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME = "Global";

    public static final String SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE = "redhat";
    public static final String SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE = "windows2008";
    public static final String SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE = "windows2012";
    public static final String SERVICE_TEMPLATE_SERVER_ESXI_VALUE = "vmware_esxi";
    public static final String SERVICE_TEMPLATE_SERVER_HYPERV_VALUE = "hyperv";
    public static final String SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE = "redhat7";
    public static final String SERVICE_TEMPLATE_SERVER_SUSE11_VALUE = "suse11";
    public static final String SERVICE_TEMPLATE_SERVER_SUSE12_VALUE = "suse12";

    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID = "target_boot_device";
    public static final String SERVICE_TEMPLATE_SERVER_BIOS_CONFIG_ID = "bios_configuration";
    public static final String SERVICE_TEMPLATE_SERVER_SYSTEM_PROFILE_ID = "SysProfile";
    public static final String SERVICE_TEMPLATE_SERVER_USB_PORTS_ID = "UsbPorts";
    public static final String SERVICE_TEMPLATE_SERVER_NUM_PROCS_ID = "ProcCores";
    public static final String SERVICE_TEMPLATE_SERVER_VIRTUALIZATION_ID = "ProcVirtualization";
    public static final String SERVICE_TEMPLATE_SERVER_LOGICAL_PROC_ID = "LogicalProc";
    public static final String SERVICE_TEMPLATE_SERVER_MEM_NODE_INTERLEAVE_ID = "NodeInterleave";
    public static final String SERVICE_TEMPLATE_SERVER_EXECUTE_DISABLE_ID = "ProcExecuteDisable";
    public static final String SERVICE_TEMPLATE_SERVER_RAID_ID = "raid_configuration"; // to use with server hardware settings
    public static final String SERVICE_TEMPLATE_SERVER_ADVANCED_NON_RAID_ID = "nonraid";
    
    public static final String SERVICE_TEMPLATE_SERVER_IDENTITYPOOL_ID = "identity_pool";
    public static final String SERVICE_TEMPLATE_SERVER_NFS_NETWORK_ID = "nfs_network"; // used by storage
    public static final String SERVICE_TEMPLATE_SERVER_MIGRATE_ON_FAIL_ID = "migrate_on_failure";
    public static final String SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS = "attempted_servers";
    public static final String SERVICE_TEMPLATE_SERVER_SOURCE = "server_source";
    public static final String SERVICE_TEMPLATE_SERVER_SOURCE_POOL = "pool";
    public static final String SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL = "manual";

    public static final String SERVICE_TEMPLATE_SERVER_POOL_ID = "server_pool";
    public static final String SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION = "server_select";

    public static final String SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID = "admin_password";
    public static final String SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID = "admin_confirm_password";
    public static final String SERVICE_TEMPLATE_SERVER_OS_CUSTOM_SCRIPT_ID = "custom_script";
    public static final String SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID = "razor_image";
    public static final String SERVICE_TEMPLATE_SERVER_OS_TYPE_ID = "os_image_type";
    public static final String SERVICE_TEMPLATE_SERVER_OS_VERSION_ID = "os_image_version";
    public static final String SERVICE_TEMPLATE_SERVER_EQL_MEM_ID = "esx_mem";

    public static final String SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID = "network_configuration";
    public static final String SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID = "generate_host_name";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID = "os_host_name";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID = "os_host_name_template";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_DEFAULT_TEMPLATE = "server${num}";
    public static final String SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE = "ip_source";
    public static final String SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_AUTOMATIC = "automatic";
    public static final String SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL = "manual";
    public static final String SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE = "static_ip_source:";
    public static final String SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_AUTOMATIC = "automatic";
    public static final String SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_DNS = "dns";
    public static final String SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL = "manual";
    public static final String SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE = "static_ip_value:";
    public static final String SERVICE_TEMPLATE_SERVER_NETWORK_DEFAULT_GATEWAY_ID = "default_gateway";
    public static final String SERVICE_TEMPLATE_SERVER_DHCP_NETWORK_ID = "dhcp_workload";
    public static final String SERVICE_TEMPLATE_SERVER_DHCP_NETWORK_NAME = "DHCP / No Gateway";
    public static final String SERVICE_TEMPLATE_SERVER_MTU_ID = "mtu";

    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL = "hyperv_install";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_PROD_KEY_ID = "product_key";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_TIMEZONE_ID = "timezone";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_LANG_ID = "language";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_KEYBOARD_ID = "keyboard";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_DN_ID = "domain_name";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_FQDN_ID = "fqdn";
    public static final String SERVICE_TEMPLATE_SERVER_OS_HV_ADMIN_LOGIN_ID = "domain_admin_user";
    public static final String SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID = "domain_admin_password";
    public static final String SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID = "domain_admin_password_confirm";

    public static final String SERVICE_TEMPLATE_SERVER_OS_LINUX_TIMEZONE_ID = "time_zone";
    public static final String SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID = "ntp_server";
    public static final String SERVICE_TEMPLATE_SERVER_OS_ISCSI_ID = "iscsi_initiator";
    public static final String SERVICE_TEMPLATE_SERVER_OS_ISCSI_SOFTWARE_ID = "software";
    public static final String SERVICE_TEMPLATE_SERVER_OS_ISCSI_HARDWARE_ID = "hardware";

    public static final String SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID = "datacenter";
    public static final String SERVICE_TEMPLATE_CLUSTER_NEW_DATACENTER_ID = "$new$datacenter";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID = "cluster";
    public static final String SERVICE_TEMPLATE_CLUSTER_NEW_CLUSTER_ID = "$new$cluster";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_HA_ID = "ha_config";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_DRS_ID = "drs_config";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID = "sdrs_config";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID = "sdrs_name";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID = "sdrs_members";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID = "vds_enabled";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID = "vsan_enabled";
    public static final String SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID = "compression_enabled";
    public static final String SERVICE_TEMPLATE_CLUSTER_FAILURE_ID = "failure_tolerance_method";
    public static final String SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID1_ID = "failure_tolerance_raid1";
    public static final String SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID5_ID = "failure_tolerance_raid5";
    public static final String SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID = "failures_number";

    public static final String SERVICE_TEMPLATE_CLUSTER_FAILURE_TOOLTIP = "RAID-1 (Mirroring) in Virtual SAN employs a 2n+1 host or fault domain algorithm, " +
            "where n is the number of failures to tolerate.<br/><br/>RAID-5/6 (Erasure Coding) in Virtual SAN " +
            "employs a 3+1 or 4+2 host or fault domain requirement, depending on 1 or 2 failures to tolerate " +
            "respectively. RAID-5/6 (Erasure Coding) does not support 3 failures to tolerate";

    public static final String SERVICE_TEMPLATE_ASM_GUID = "asm_guid";
    public static final String SERVICE_TEMPLATE_CONFIGURE_SAN = "configuresan";

    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_STD_ID = "standard";
    public static final String SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID = "distributed";

    public static final String SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID = "vds_name";
    public static final String SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID = "vds_pg";


    public static final String SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID = "component-cluster-hyperv-1";
    public static final String SERVICE_TEMPLATE_SCVMM_CLUSTER_HOSTGROUP_ID = "hostgroup";
    public static final String SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID = "name";
    public static final String SERVICE_TEMPLATE_SCVMM_CLUSTER_IPADDRESS_ID = "ipaddress";
    public static final String SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID = "asm::cluster::scvmm";

    public static final String SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID = "component-cluster-vcenter-1";
    public static final String SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID = "asm::cluster";
    public static final String SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID = "asm::cluster::vds";

    public static final String SERVICE_TEMPLATE_VM_VC_COMPONENT_ID = "component-virtualmachine-vcenter-1";
    public static final String SERVICE_TEMPLATE_VM_VC_CLONE_COMPONENT_ID = "component-virtualmachine-clonevcenter-1";
    public static final String SERVICE_TEMPLATE_VM_RESOURCE = "asm::vm::vcenter";
    public static final String SERVICE_TEMPLATE_VM_OS_IMAGE_ID = "razor_image";
    public static final String SERVICE_TEMPLATE_VM_OS_TYPE_ID = "os_image_type";
    public static final String SERVICE_TEMPLATE_VM_OS_CUSTOM_SCRIPT_ID = "custom_script";
    public static final String SERVICE_TEMPLATE_VM_NUMBER_OF_CPU_ID = "cpu_count";
    public static final String SERVICE_TEMPLATE_VM_SIZE = "disksize_in_gb";
    public static final String SERVICE_TEMPLATE_VM_DISK = "vmvirtualdisk_configuration";
    public static final String SERVICE_TEMPLATE_VM_MEMORY_ID = "memory_in_mb";
    public static final String SERVICE_TEMPLATE_VM_NETWORK_ID = "network_interfaces";
    public static final String SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE = "source";
    public static final String SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE_DC = "source_datacenter";
    public static final String SERVICE_TEMPLATE_VCENTER_VM_CLONE_TYPE = "clone_type";
    public static final String SERVICE_TEMPLATE_VCENTER_VM_CUSTOM_SPEC = "vm_custom_spec";
    public static final String SERVICE_TEMPLATE_VM_OS_HOSTNAME_DEFAULT_TEMPLATE = "vm${num}";
    public static final String SERVICE_TEMPLATE_VCENTER_VM_ID_PREFIX = "component-virtualmachine-vcenter";
    public static final String SERVICE_TEMPLATE_VCENTER_HA_CONFIG_ID = "vcenter_ha_config";

    public static final String SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID = "auth_type";
    public static final String SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_CHAP_ID = "chap";
    public static final String SERVICE_TEMPLATE_STORAGE_CHAP_USER_NAME_ID = "chap_user_name";
    public static final String SERVICE_TEMPLATE_STORAGE_CHAP_SECRET_ID = "passwd";
    public static final String SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_IQNIP_ID = "iqnip";
    public static final String SERVICE_TEMPLATE_STORAGE_IQNORIP_ID = "iqnOrIP";
    public static final String SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID = "asm::volume::equallogic";
    public static final String SERVICE_TEMPLATE_STORAGE_ISCSI_IQN = "iqn_id";
    public static final String SERVICE_TEMPLATE_STORAGE_ADD_TO_SDRS_ID = "add_to_sdrs";

    public static final String SERVICE_TEMPLATE_SERVER_COMPID_ALL = "component-server-1";
    public static final String SERVICE_TEMPLATE_SERVER_COMPID_OS = "component-serverminimal-1";
    public static final String SERVICE_TEMPLATE_SERVER_COMPID_HW = "component-serverminimal-hw-1";
    public static final String SERVICE_TEMPLATE_SERVER_OS_RESOURCE = "asm::server";
    public static final String SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE = "asm::idrac";
    public static final String SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE = "asm::bios";
    public static final String SERVICE_TEMPLATE_SERVER_CONFIG_XML = "config_xml";
    public static final String SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID = "asm::esxiscsiconfig";
    public static final String SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID = "hypervisor_network";    

    public static final String SERVICE_TEMPLATE_HV_VM_CLONE_COMPONENT = "component-virtualmachine-clonehyperv-1";
    public static final String SERVICE_TEMPLATE_HV_VM_RESOURCE = "asm::vm::scvmm";
    public static final String SERVICE_TEMPLATE_VM_NAME = "hostname";
    public static final String SERVICE_TEMPLATE_VM_GENERATE_NAME_ID = "vm_generate_name_id";
    public static final String SERVICE_TEMPLATE_VM_NAME_TEMPLATE_ID = "vm_name_template";
    public static final String SERVICE_TEMPLATE_VM_NAME_DEFAULT_TEMPLATE = "vm${num}";
    public static final String SERVICE_TEMPLATE_HV_VM_DESCRIPTION = "description";
    public static final String SERVICE_TEMPLATE_HV_VM_TEMPLATE = "template";
    public static final String SERVICE_TEMPLATE_HV_VM_PATH = "path";
    public static final String SERVICE_TEMPLATE_HV_VM_OPT = "block_dynamic_optimization";
    public static final String SERVICE_TEMPLATE_HV_VM_START_ACTION = "start_action";
    public static final String SERVICE_TEMPLATE_HV_VM_STOP_ACTION = "stop_action";
    public static final String SERVICE_TEMPLATE_HV_VM_HIGHLY_AVAIL = "highly_available";
    
    public static final String SERVICE_TEMPLATE_HV_VM_MEMORY_ID = "memory_mb";
    public static final String SERVICE_TEMPLATE_HV_VM_OS_DN_ID = "domain";
    public static final String SERVICE_TEMPLATE_VM_OS_HV_ADMIN_LOGIN_ID = "domain_username";
    public static final String SERVICE_TEMPLATE_VM_OS_DOMAIN_PASSWORD_ID = "domain_password";
    public static final String SERVICE_TEMPLATE_VM_OS_DOMAIN_CONFIRM_PASSWORD_ID = "domain_password_confirm";

    public static final String SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID = "netapp::create_nfs_export";
    public static final String SERVICE_TEMPLATE_STORAGE_NETAPP_AGGR_ID = "aggr";
    public static final String SERVICE_TEMPLATE_TRUE_NAME = "True";
    public static final String SERVICE_TEMPLATE_FALSE_NAME = "False";
    public static final String SERVICE_TEMPLATE_TRUE_VALUE = "true";
    public static final String SERVICE_TEMPLATE_FALSE_VALUE = "false";

    public static final String SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID = "asm::volume::compellent";
    public static final String SERVICE_TEMPLATE_STORAGE_COMPELLENT_OS_ID = "operatingsystem";
    public static final String SERVICE_TEMPLATE_STORAGE_COMPELLENT_ESX6_NAME = "VMware ESX 6.0";
    public static final Double SERVICE_TEMPLATE_STORAGE_COMPELLENT_ESX6_TARGET_VERSION = 6.6;

    public static final String SERVICE_TEMPLATE_STORAGE_VNX_POOL_ID = "pool";
    public static final String SERVICE_TEMPLATE_STORAGE_VNX_TYPE_ID = "type";
    public static final String SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID = "asm::volume::vnx";
    public static final String SERVICE_TEMPLATE_STORAGE_VNX_TYPE_COMPRESSED = "compressed";
    public static final String SERVICE_TEMPLATE_STORAGE_VNX_TYPE_THIN = "thin";
    public static final String SERVICE_TEMPLATE_STORAGE_VNX_TYPE_NONTHIN = "nonthin";
    public static final String SERVICE_TEMPLATE_STORAGE_VNX_TYPE_SNAP = "compressed";

    public static final String SERVICE_TEMPLATE_SERVER_NTP_TOOLTIP = "ESXi, Windows, and Linux services support multiple NTP servers. NTP addresses must be separated by commas.";
    
    public static final String SERVICE_TEMPLATE_CREATE_NEW_PREFIX = "$new$";
    public static final String SERVICE_TEMPLATE_TITLE_ID = "title";

    // options for "title"
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE = "option_autogenerate";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING = "option_existing";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW = "option_create_now";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT = "option_create_at_deployment";

    public static final String SERVICE_TEMPLATE_VOLUME_NAME_TEMPLATE = "volume_template";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_EXISTING = "volume_existing";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_GENERATED = "volume_generated";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_NEW = "volume_new";
    public static final String SERVICE_TEMPLATE_VOLUME_NAME_NEW_AT_DEPLOYMENT = "volume_new_at_deployment";

    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID = "SD_WITH_RAID";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN = "SD_WITH_RAID_VSAN";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD = "SD";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD = "HD"; // Local Hard Drive
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI = "iSCSI";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC = "FC";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE = "NONE";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID = "NONE_WITH_RAID";
    public static final String SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN = "AHCI_VSAN";
    public static final String SERVICE_TEMPLATE_SERVER_OS_RESOURCE_VSAN = "local_storage_vsan";

    public static final String SERVICE_TEMPLATE_BASE_SERVER_ID = "asm::baseserver";

    public static final String SERVICE_TEMPLATE_STORAGE_TOOLTIP = "Device where volume will be created";
    public static final String SERVICE_TEMPLATE_SERVER_POOL_TOOLTIP = "Pool from which servers are selected during deployment";
    public static final String SERVICE_TEMPLATE_OS_IMAGE_TOOLTIP = "Location of OS image installation files";
    public static final String SERVICE_TEMPLATE_ADM_PASSWORD_TOOLTIP = "OS administrator password set on the installed OS";
    public static final String SERVICE_TEMPLATE_ID_POOL_TOOLTIP = "Pool from which virtual identities are selected during deployment";
    public static final String SERVICE_TEMPLATE_VM_NETWORKS_TOOLTIP = "Networks associated with the VM";
    public static final String SERVICE_TEMPLATE_VM_MEMORY_SIZE_TOOLTIP = "Memory size should be in multiples of 4";
    public static final String SERVICE_TEMPLATE_MIGRATE_ON_FAIL_TOOLTIP = "If selected server fails, retry on another available server";

    public static final String SERVICE_TEMPLATE_BOOT_TARGET_TOOLTIP = "Local Hard Drive - ASM creates a RAID with the local HDDs and installs the OS to this RAID array.<br/><br/>" +
            "SD w/ RAID enabled - Enables SD, sets boot the SD, creates the specified RAID config which will not be the boot device.<br/><br/>" +
            "SD w/ RAID disabled - Enables SD, sets boot the SD, disables RAID controller.<br/><br/>" +
            "None - will not configure the boot device or boot order.<br/><br/>" +
            "None (w/ RAID Configuration) - will not configure the boot device or boot order, but will create the RAID virtual disk.";

    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_ID = "asm::chassis::initial_config";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_SERVER_ID = "asm::idrac::initial_config";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORK_TYPE = "cmc_network_type";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORKS = "cmc_network";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORK_TYPE = "idrac_network_type";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORKS = "idrac_networks";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_SLOTS = "idrac_slots";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORK_TYPE = "iom_network_type";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORKS = "iom_networks";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_SLOTS = "iom_slots";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_CRED = "cmc_cred_id";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED = "idrac_cred_id";
    public static final String SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_CRED = "iom_cred_id";

    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_RESOURCE_ID = "chassis_settings";
    public static final String SERVICE_TEMPLATE_BLADE_CONFIG_RESOURCE_ID = "asm::idrac::config";
    public static final String SERVICE_TEMPLATE_IOM_CONFIG_RESOURCE_ID = "force10_settings";
    public static final String SERVICE_TEMPLATE_UPLINK_CONFIG_RESOURCE_ID = "asm::iom::uplink";

    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_NAME = "chassis_name";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_DNS_NAME = "dns_name";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_REGISTER_CMC_DNS = "register_dns";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_REGISTER_IDRAC_DNS = "register_dns";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_DATACENTER = "datacenter";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_AISLE = "aisle";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_RACK = "rack";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_RACKSLOT = "rackslot";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_RED_POLICY = "redundancy_policy";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_PERF = "perf_over_redundancy";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_DYNAMIC_POWER = "dynamic_power_engage";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_USERS = "users";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_ALERT_DEST = "alert_destinations";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_NTP_ENABLED = "ntp_enabled";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_TIME_ZONE = "time_zone";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_POWER_CAP = "power_cap";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_POWER_CAP_TYPE = "power_cap_type";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_NTP_PREF = "ntp_preferred";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_NTP_SEC = "ntp_secondary";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_EMAIL_DEST = "email_destinations";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_SMTP_SERVER = "smtp_server";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_BLADE_IPMI = "ipmi_over_lan";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_HOSTNAME = "hostname";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_SYSLOG = "syslog_destination";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_NTP1 = "ntp_server1";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_NTP2 = "ntp_server2";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_QUAD = "quadportmode";
    public static final String SERVICE_TEMPLATE_CHASSIS_PROVIDER = "provider";
    public static final String SERVICE_TEMPLATE_CHASSIS_PROVIDER_ASM_DECRYPT = "asm_decrypt";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_VLT = "vlt_enabled";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_SPANNING_TREE_MODE = "spanning_tree_mode";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_STASH_MODE = "stash_mode";

    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_UPLINK="uplinks";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_UPLINK_FILE="config_file";

    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_CONTROLS_CONFIGIOMMODE = "CONFIGIOMMODE";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_CONTROLS_CONFIGALLIOM = "CONFIGALLIOM";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_CONTROLS_UPLINKDEFINITIONS = "UPLINKDEFINITIONS";
    public static final String SERVICE_TEMPLATE_CHASSIS_CONFIG_CONTROLS_CONFIGUPLINKS = "CONFIGUPLINKS";

    public static final String SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID = "porttype";
    public static final String SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ISCSI = "iSCSI";
    public static final String SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_FIBRE_CHANNEL = "FibreChannel";
    public static final String SERVICE_TEMPLATE_COMPELLENT_BOOT_VOLUME_ID = "boot";

    public static final String SERVICE_TEMPLATE_COMPELLENT_COMP_ID = "component-compellent-1";
    public static final String SERVICE_TEMPLATE_EQL_COMP_ID = "component-equallogic-chap-1";
    public static final String SERVICE_TEMPLATE_NETAPP_COMP_ID = "component-netapp-1";
    public static final String SERVICE_TEMPLATE_VNX_COMP_ID = "component-vnx-1";
    public static final String SERVICE_TEMPLATE_SERVER_MINIMIAL_COMP_ID = "component-serverminimal-1";
    
    public static final String SERVICE_TEMPLATE_ENSURE = "ensure";

    public static final String SERVICE_TEMPLATE_COMPONENT_NAME = "name";
    public static final String SERVICE_TEMPLATE_COMPONENT_INSTALL_ORDER = "install_order";
    public static final String SERVICE_TEMPLATE_COMPONENT_SERVICE_TYPE = "service_type";
    
    public static final String SERVICE_TEMPLATE_STORAGE_SIZE = "size";
    public static final String SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE = "$DEFAULT_TO_REMOVE$";

    public static final String LOCAL_STORAGE_ID = "local_storage_vsan";
    public static final String LOCAL_STORAGE_TYPE_ID = "local_storage_vsan_type";
    public static final String LOCAL_STORAGE_TYPE_FLASH = "flash";
    public static final String LOCAL_STORAGE_TYPE_HYBRID = "hybrid";
    public static final String LOCAL_STORAGE_TOOLTIP = "RAID controller will be put in the appropriate mode and automatically " +
            "configured based on VMware vSAN best practices.";
    public static final String VMWARE_FEATURES_GROUP = "Enable VMWare Features";
    
    
    public static final String SERVICE_TEMPLATE_FIRMWARE_COMP_ID = "asm::firmware";
    public static final String SERVICE_TEMPLATE_FIRMWARE_SERVER_COMP_ID = "asm::server_update";
    public static final String SERVICE_TEMPLATE_FIRMWARE_PRODUCT = "product";
    public static final String SERVICE_TEMPLATE_FIRMWARE_TITLE = "title";
    public static final String SERVICE_TEMPLATE_FIRMWARE_PATH = "path";
    public static final String SERVICE_TEMPLATE_FIRMWARE_SOFTWARE_VERSION = "version";
    public static final String SERVICE_TEMPLATE_FIRMWARE_SOFTWARE_PATH = "path";
    public static final String SERVICE_TEMPLATE_FIRMWARE_VCENTER_CERT = "vcenter_cert";
    public static final String SERVICE_TEMPLATE_FIRMWARE_ESX_PASSWORD = "esx_password";
    public static final String SERVICE_TEMPLATE_FIRMWARE_ESX_HOSTNAME = "esx_hostname";
    public static final String SERVICE_TEMPLATE_FIRMWARE_FORCE_RESTART = "force_restart";
    public static final String SERVICE_TEMPLATE_FIRMWARE_ASM_HOSTNAME = "asm_hostname";    
    public static final String SERVICE_TEMPLATE_FIRMWARE_SERVER_FIRMWARE = "server_firmware";
    public static final String SERVICE_TEMPLATE_FIRMWARE_SERVER_SOFTWARE = "server_software";
    public static final String SERVICE_TEMPLATE_FIRMWARE_VCENTER_SERVER_LIST = "vcenter_server_list";

    public static final List<String> SERVICE_TEMPLATE_STORAGE_RESOURCE_LIST = Arrays.asList(
            SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
            SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID,
            SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID,
            SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID
    );

    public static final String HYPERVISOR_IP_ADDRESS = "hypervisor_ip_address";

    public static Boolean isHyperVComponent(String componentName) {
        if (SERVICE_TEMPLATE_HV_VM_CLONE_COMPONENT.equals(componentName)) {
            return true;
        }
        return false;
    }

    public static final String SERVICE_TEMPLATE_AUTOGEN_VOLUME_TOOLTIP = "Select this option to auto-generate storage volume names at deployment time. <br>" +
            " Specify the format that will be used to generate storage volume names at deployment time. <br>" +
            " Must contain variables that will produce a unique storage volume name. Allowed variable is ${num}. <br>" +
            "For example, ‘Storage${num}, where ${num} is an auto-generated unique number";

    //List Service Template Types Here
    public static final String SERVICE_TEMPLATE_TYPE_ASM = "ASM";
    public static final String SERVICE_TEMPLATE_TYPE_VALIDATED_SYSTEMS = "ValidatedSystems";
}
