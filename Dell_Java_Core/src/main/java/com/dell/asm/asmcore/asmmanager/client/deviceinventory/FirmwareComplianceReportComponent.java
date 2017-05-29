/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

/**
 * Represents an individual component on device and it's compliance against a given FirmwareRepository.
 */
@XmlRootElement(name = "FirmwareComplianceReportComponent")
@ApiModel()
public class FirmwareComplianceReportComponent {
    
    // Member Variables
    private String id;
    private String name;
    private boolean isSoftware;
    private FirmwareComplianceReportComponentVersionInfo currentVersion;
    private FirmwareComplianceReportComponentVersionInfo targetVersion; 
    private boolean isCompliant;
    private String vendor;
    
    /**
     * Default constructor for the class.
     */
    public FirmwareComplianceReportComponent() { }

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

    public FirmwareComplianceReportComponentVersionInfo getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(FirmwareComplianceReportComponentVersionInfo currentVersion) {
        this.currentVersion = currentVersion;
    }

    public FirmwareComplianceReportComponentVersionInfo getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(FirmwareComplianceReportComponentVersionInfo targetVersion) {
        this.targetVersion = targetVersion;
    }

    public boolean isCompliant() {
        return isCompliant;
    }

    public void setCompliant(boolean isCompliant) {
        this.isCompliant = isCompliant;
    }

    public boolean isSoftware() {
        return isSoftware;
    }

    public void setSoftware(boolean isSoftware) {
        this.isSoftware = isSoftware;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
}
