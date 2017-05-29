/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

/**
 * Provides an enum containing the ServiceTemplateSettingId and it's corresponding Display value.  This should be used
 * when dynamically creating ServiceTemplateSettings to ensure the display values are consistent.
 */
public enum ServiceTemplateSettingDef {
    
    SERVER_OS_RESOURCE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, "OS Settings"),
    SERVER_NETWORKING_COMP_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID, "Network Settings"),
    ASM_GUID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, "Target Virtual Machine Manager"),
    ESX_CLUSTER_COMP_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID, "Cluster Settings"),
    VM_RESOURCE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE, "Vcenter Settings"),
    CLUSTER_DATACENTER_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID, "Data Center Name"),
    CLUSTER_CLUSTER_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID, "Cluster Name"),
    CLUSTER_CLUSTER_HA_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_HA_ID, "Cluster HA Enabled"),
    CLUSTER_CLUSTER_DRS_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DRS_ID, "Cluster DRS Enabled"),
    CLUSTER_CLUSTER_VDS_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID, "Switch Type"),
    SERVER_IDRAC_RESOURCE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE, "Hardware Settings"),
    SERVER_SOURCE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE, "Server Source"),
    SERVER_MANUAL_SELECTION(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION, "Choose Server"),
    SERVER_OS_HOSTNAME_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID, "Host Name"),
    VM_OS_TYPE_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_TYPE_ID, "Os Type"), 
    SERVER_HYPERVISOR_NETWORK_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID, "Hypervisor Management Network"),
    STORAGE_EQL_COMP_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, "EqualLogic Storage Settings"),
    STORAGE_NETAPP_COMP_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, "Netapp Storage Settings"),
    STORAGE_COMPELLENT_COMP_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, "Compellent Storage Settings"),
    COMPELLENT_PORTTYPE_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID, "Port Type"),
    STORAGE_ISCSI_IQN(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ISCSI_IQN, "IQN Id"),
    TITLE_ID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, "Target EqualLogic"),
    STORAGE_SIZE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_SIZE, "Storage Size e.g 100GB"),
    ENSURE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ENSURE, "Ensure"),
    SERVER_BIOS_RESOURCE(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE, "Bios Settings");
    
    
    
    // Member variables
    private String id;
    private String displayName;
    
    // Default constructuor for the class
    private ServiceTemplateSettingDef(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
}
