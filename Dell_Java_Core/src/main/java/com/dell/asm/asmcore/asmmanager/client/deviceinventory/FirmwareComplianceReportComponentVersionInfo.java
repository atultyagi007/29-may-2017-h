/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

/**
 * Indicates the Firmware component level information along with the level of service firmware it was acquired.  
 */
@XmlRootElement(name = "FirmwareComplianceReportComponentVersionInfo")
@ApiModel()
public class FirmwareComplianceReportComponentVersionInfo {
    
    public static final String FIRMWARE_LEVEL_DEFAULT = "default";
    public static final String FIRMWARE_LEVEL_EMBEDDED = "embedded";
    public static final String FIRMWARE_LEVEL_SERVICE = "service"; 
    
    // Member Variables
    private String id;
    private String firmwareName;
    private String firmwareType;
    private String firmwareVersion;
    private Date firmwareLastUpdateTime;
    private String firmwareLevel;

    /**
     * Default constructor for the class.
     */
    public FirmwareComplianceReportComponentVersionInfo() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirmwareName() {
        return firmwareName;
    }

    public void setFirmwareName(String firmwareName) {
        this.firmwareName = firmwareName;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Date getFirmwareLastUpdateTime() {
        return firmwareLastUpdateTime;
    }

    public void setFirmwareLastUpdateTime(Date firmwareLastUpdateTime) {
        this.firmwareLastUpdateTime = firmwareLastUpdateTime;
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public String getFirmwareLevel() {
        return firmwareLevel;
    }

    public void setFirmwareLevel(String firmwareLevel) {
        this.firmwareLevel = firmwareLevel;
    }

    /**
     * Returns a deep clone of the existing object. 
     */
    public FirmwareComplianceReportComponentVersionInfo deepClone() {
        FirmwareComplianceReportComponentVersionInfo versionInfo = new FirmwareComplianceReportComponentVersionInfo();
        versionInfo.setFirmwareLastUpdateTime(this.getFirmwareLastUpdateTime());
        versionInfo.setFirmwareLevel(this.getFirmwareLevel());
        versionInfo.setFirmwareName(this.getFirmwareName());
        versionInfo.setFirmwareType(this.getFirmwareType());
        versionInfo.setFirmwareVersion(this.getFirmwareVersion());
        versionInfo.setId(this.getId());
        return versionInfo;
    }
}
