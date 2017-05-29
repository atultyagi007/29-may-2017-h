/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configuretemplate;


public class ConfigureTemplateSettingIDs {
    // The default value for enumerated values is the empty string;
    // the GUI will require that the user select a different value.
    public static final String CONFIGURE_TEMPLATE_SELECT_ID = "";
    public static final String CONFIGURE_TEMPLATE_SELECT = "Select...";
    public static final String CONFIGURE_TEMPLATE_CREATE_NEW_PREFIX = "$new$";

    // The default value for server pool is -1 which means the global pool
    public static final String CONFIGURE_TEMPLATE_SERVER_POOL_GLOBAL_ID = "-1";

    //Configuration Setting IDS
    public static final String CONFIGURE_TEMPLATE_ID = "asm::configuration";

    //networking resources
    public static final String CONFIGURE_TEMPLATE_NETWORKING_RESOURCE = "asm::configuration::networking";
    public static final String CONFIGURE_TEMPLATE_NETWORKING_ASSOCIATIONS_RESOURCE = "asm::configuration::networking_associations";

    //os resources
    public static final String CONFIGURE_TEMPLATE_OS_RESOURCE = "asm::configuration::os";
    public static final String CONFIGURE_TEMPLATE_OS_ASSOCIATIONS_RESOURCE = "asm::configuration::os_associations";

    //cluster resources
    public static final String CONFIGURE_TEMPLATE_CLUSTER_RESOURCE = "asm::configuration::cluster";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_ASSOCIATIONS_RESOURCE = "asm::configuration::cluster_associations";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_RESOURCE = "asm::configuration::cluster_details";

    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VCENTER_ID = "asm::configuration::cluster_details::vcenter";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DATACENTER_ID = "asm::configuration::cluster_details::datacenter";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_DATACENTER_ID = "asm::configuration::cluster_details::new_datacenter";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_CLUSTER_ID = "asm::configuration::cluster_details::cluster";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_NEW_CLUSTER_ID = "asm::configuration::cluster_details::new_cluster";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_HA_ID = "asm::configuration::cluster_details::ha";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_DRS_ID = "asm::configuration::cluster_details::drs";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_ENABLE_VSAN_ID = "asm::configuration::cluster_details::enable_vsan";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_ID = "asm::configuration::cluster_details::vds";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_OPTIONS_ID = "asm::configuration::cluster_details::vds_options";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_PORT_GROUPS_ID = "asm::configuration::cluster_details::port_groups";

    public static final String CONFIGURE_TEMPLATE_CLUSTER_VCENTER = "vcenter";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_SCVMM = "scvmm";

    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_STANDARD_ID = "standard";
    public static final String CONFIGURE_TEMPLATE_CLUSTER_DETAILS_VDS_DISTRIBUTED_ID = "distributed";

    //server pool resources
    public static final String CONFIGURE_TEMPLATE_SERVER_POOL_RESOURCE = "asm::configuration::server_pool";
    public static final String CONFIGURE_TEMPLATE_SERVER_POOL_ASSOCIATIONS_RESOURCE = "asm::configuration::server_pool_associations";

    //storage resources
    public static final String CONFIGURE_TEMPLATE_STORAGE_RESOURCE = "asm::configuration::storage";
    public static final String CONFIGURE_TEMPLATE_STORAGE_ASSOCIATIONS_RESOURCE = "asm::configuration::storage_associations";

    public static final String CONFIGURE_TEMPLATE_STORAGE_COMPELLENT = "compellent";
    public static final String CONFIGURE_TEMPLATE_STORAGE_EQUALLOGIC = "equallogic";
    public static final String CONFIGURE_TEMPLATE_STORAGE_NETAPP = "netapp";
    public static final String CONFIGURE_TEMPLATE_STORAGE_EMCVNX = "emcvnx";

    //storage settings resources
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_RESOURCE = "asm::configuration::storage_details";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_STORAGE_ARRAY_ID = "asm::configuration::storage_details::storage_array";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_COUNT_ID = "asm::configuration::storage_details::volume_count";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_ID = "asm::configuration::storage_details::volume_size";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_ID = "asm::configuration::storage_details::volume_size_measure";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_NAME_TEMPLATE_ID = "asm::configuration::storage_details::volume_name_template";

    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_MB = "MB";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_GB = "GB";
    public static final String CONFIGURE_TEMPLATE_STORAGE_DETAILS_VOLUME_SIZE_MEASURE_TB = "TB";

    //os password resources
    public static final String CONFIGURE_TEMPLATE_OS_PASSWORD_RESOURCE = "asm::configuration::os_password";
    public static final String CONFIGURE_TEMPLATE_OS_ADMINISTRATOR_PASSWORD = "asm::configuration::os_administrator_password";

    // server setting resources
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_RESOURCE = "asm::configuration::server_settings";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_NUMBER_OF_ID = "asm::configuration::server_settings::number_of";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_OS_IMAGE_ID = "asm::configuration::server_settings::razor_image";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_RECOMMENDED_ESXI_ID = "asm::configuration::server_settings::recommended_esxi";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_SERVER_POOL_ID = "asm::configuration::server_settings::server_pool";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_PREFIX_ID = "asm::configuration::server_settings::name_prefix";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_ID = "asm::configuration::server_settings::name_suffix";

    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_NUMERIC_ID = "$(num)";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_DNS_ID = "$(dns)";
    public static final String CONFIGURE_TEMPLATE_SERVER_SETTINGS_NAME_SUFFIX_SERVICE_TAG_ID = "$(service_tag)";



}
