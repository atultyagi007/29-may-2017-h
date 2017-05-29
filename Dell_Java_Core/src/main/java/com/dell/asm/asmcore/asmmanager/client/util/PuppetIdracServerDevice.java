/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.util.List;

/**
 * Represents an IDrac Server as represented by Puppet.
 */
public class PuppetIdracServerDevice {
    
    // Member Variables
    private List<VibsInfo> vibsInfos;
    private String esxVersion;
    
    // Default constructor for the class
    public PuppetIdracServerDevice() { }
    
    public String getEsxVersion() {
        return esxVersion;
    }

    public void setEsxVersion(String esxVersion) {
        this.esxVersion = esxVersion;
    }

    public List<VibsInfo> getVibsInfos() {
        return vibsInfos;
    }

    public void setVibsInfos(List<VibsInfo> vibsInfos) {
        this.vibsInfos = vibsInfos;
    }


    public static class VibsInfo {
        
        // Member Variables
        private String acceptanceLevel; 
        private List<String> conflicts;
        private String creationDate;
        private List<String> depends;
        private String description;
        private String hardwarePlatformsRequired;
        private String id;
        private String installDate;
        private String liveInstallAllowed;
        private String liveRemoveAllowed;
        private String maintenanceModeRequuired;
        private String name;
        private String overlay;
        private List<String> payloads;
        private List<String> provides;
        private List<String> referenceUrls;
        private List<String> replaces;
        private String statelessReady;
        private String status;
        private String summary;
        private List<String> tags;
        private String type;
        private String vendor;
        private String version;
        
        // Default constructor;
        public VibsInfo() { }

        public String getAcceptanceLevel() {
            return acceptanceLevel;
        }

        public void setAcceptanceLevel(String acceptanceLevel) {
            this.acceptanceLevel = acceptanceLevel;
        }

        public List<String> getConflicts() {
            return conflicts;
        }

        public void setConflicts(List<String> conflicts) {
            this.conflicts = conflicts;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(String creationDate) {
            this.creationDate = creationDate;
        }

        public List<String> getDepends() {
            return depends;
        }

        public void setDepends(List<String> depends) {
            this.depends = depends;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getHardwarePlatformsRequired() {
            return hardwarePlatformsRequired;
        }

        public void setHardwarePlatformsRequired(String hardwarePlatformsRequired) {
            this.hardwarePlatformsRequired = hardwarePlatformsRequired;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getInstallDate() {
            return installDate;
        }

        public void setInstallDate(String installDate) {
            this.installDate = installDate;
        }

        public String getLiveInstallAllowed() {
            return liveInstallAllowed;
        }

        public void setLiveInstallAllowed(String liveInstallAllowed) {
            this.liveInstallAllowed = liveInstallAllowed;
        }

        public String getLiveRemoveAllowed() {
            return liveRemoveAllowed;
        }

        public void setLiveRemoveAllowed(String liveRemoveAllowed) {
            this.liveRemoveAllowed = liveRemoveAllowed;
        }

        public String getMaintenanceModeRequuired() {
            return maintenanceModeRequuired;
        }

        public void setMaintenanceModeRequuired(String maintenanceModeRequuired) {
            this.maintenanceModeRequuired = maintenanceModeRequuired;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOverlay() {
            return overlay;
        }

        public void setOverlay(String overlay) {
            this.overlay = overlay;
        }

        public List<String> getPayloads() {
            return payloads;
        }

        public void setPayloads(List<String> payloads) {
            this.payloads = payloads;
        }

        public List<String> getProvides() {
            return provides;
        }

        public void setProvides(List<String> provides) {
            this.provides = provides;
        }

        public List<String> getReferenceUrls() {
            return referenceUrls;
        }

        public void setReferenceUrls(List<String> referenceUrls) {
            this.referenceUrls = referenceUrls;
        }

        public List<String> getReplaces() {
            return replaces;
        }

        public void setReplaces(List<String> replaces) {
            this.replaces = replaces;
        }

        public String getStatelessReady() {
            return statelessReady;
        }

        public void setStatelessReady(String statelessReady) {
            this.statelessReady = statelessReady;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
    
}
