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
 *  A List of keys that can be used to get information from the Puppet Facts returned for an IDrac Server.
 */
public interface PuppetIDracServerKeys {

    
    public static interface VibsInfo {
        public static final String ACCEPTANCE_LEVEL = "AcceptanceLevel";
        public static final String CONFLICTS = "Conflicts";
        public static final String CREATION_DATE = "CreationDate";
        public static final String DEPENDS = "Depends"; 
        public static final String DESCRIPTION = "Broadcom NetXtreme II CNIC Registration Agent";
        public static final String HARDWARE_PLATFORMS_REQUIRED = "HardwarePlatformsRequire";
        public static final String ID = "ID";
        public static final String INSTALL_DATE = "InstallDate";
        public static final String LIVE_INSTALL_ALLOWED = "LiveInstallAllowed";
        public static final String LIVE_REMOVE_ALLOWED = "LiveRemoveAllowed";
        public static final String MAINTENANCE_MODE_REQUIRED = "MaintenanceModeRequired";
        public static final String NAME = "Name";
        public static final String OVERLAY = "Overlay";
        public static final String PAYLOADS = "Payloads"; 
        public static final String PROVIDES = "Provides";
        public static final String REFERENCE_URLS = "ReferenceURLs";
        public static final String REPLACES = "Replaces";
        public static final String STATELESS_READY = "StatelessReady";
        public static final String STATUS = "Status";
        public static final String SUMMARY = "Summary";
        public static final String TAGS = "Tags";
        public static final String TYPE = "Type";
        public static final String VENDOR = "Vendor";
        public static final String VERSION = "Version";
    }
    
}
