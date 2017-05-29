/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.common.utilities.Inet4ConverterValidator;
import com.wordnik.swagger.annotations.ApiModel;

/**
 * Represents the Firmware Compliance Report for all of the components of a Device that have firmware which can be
 * managed in ASM.
 */
@XmlRootElement(name = "FirmwareComplianceReport")
@ApiModel()
public class FirmwareComplianceReport {

    // Static Variables
    private static Comparator<FirmwareComplianceReportComponent> SORT_BY_NAME_AND_COMPLIANCE_COMPARATOR =
            new Comparator<FirmwareComplianceReportComponent>() {

                @Override
                public int compare(FirmwareComplianceReportComponent f1, FirmwareComplianceReportComponent f2) {
                    // Ensure no Nulls before sorting
                    if (f1 != null && f2 == null) return 1;
                    if (f2 != null && f1 == null) return -1;
                    if (f2 == null && f1 == null) return 0;

                    // Sort by Compliance First
                    if (!f1.isCompliant() && f2.isCompliant()) return -1;
                    if (!f2.isCompliant() && f1.isCompliant()) return 1;

                    // Sort by Name second
                    if (f1.getName() != null && f2.getName() == null) return 1;
                    if (f2.getName() != null && f1.getName() == null) return -1;
                    if (f1.getName() == null && f2.getName() == null) return 0;

                    return f1.getName().compareTo(f2.getName());
                }
            };

    public static Comparator<FirmwareComplianceReport> SORT_BY_TYPE_COMPLIANCE_AND_IP_ADDRESS =
            new Comparator<FirmwareComplianceReport>() {
                @Override
                public int compare(FirmwareComplianceReport f1, FirmwareComplianceReport f2) {
                    // Ensure no Nulls before sorting
                    if (f1 != null && f2 == null) return 1;
                    if (f2 != null && f1 == null) return -1;
                    if (f2 == null && f1 == null) return 0;

                    if (DeviceType.isStorage(f1.getDeviceType()) && !DeviceType.isStorage(f2.getDeviceType())) return 1;
                    if (!DeviceType.isStorage(f1.getDeviceType()) && DeviceType.isStorage(f2.getDeviceType())) return -1;

                    if (DeviceType.isServer(f1.getDeviceType()) && !DeviceType.isServer(f2.getDeviceType())) return 1;
                    if (!DeviceType.isServer(f1.getDeviceType()) && DeviceType.isServer(f2.getDeviceType())) return -1;

                    // Sort by Compliance First
                    if (!f2.isCompliant() && f1.isCompliant()) return 1;
                    if (!f1.isCompliant() && f2.isCompliant()) return -1;

                    // Sort by Ip Address
                    if (f1.getIpAddress() != null && f2.getIpAddress() == null) return 1;
                    if (f1.getIpAddress() == null && f2.getIpAddress() != null) return -1;
                    if (f1.getIpAddress() == null && f2.getIpAddress() == null) return 0;

                    final Long f1IpAddress = Inet4ConverterValidator.convertIpStringToLong(f1.getIpAddress());
                    final Long f2IpAddress = Inet4ConverterValidator.convertIpStringToLong(f2.getIpAddress());
                    return f1IpAddress.compareTo(f2IpAddress);
                }
            };

    // Member Variables
    private String serviceTag;
    private String ipAddress;
    private String firmwareRepositoryName;
    private List<FirmwareComplianceReportComponent> firmwareComplianceReportComponents;
    private boolean isCompliant;
    private DeviceType deviceType;
    private String model;
    private boolean available;
    private ManagedState managedState;
    private boolean embededRepo;
    private DeviceState deviceState;

    // Default constructor for the class.
    public FirmwareComplianceReport() {
        this.firmwareComplianceReportComponents = new ArrayList<FirmwareComplianceReportComponent>();
        this.embededRepo = false;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<FirmwareComplianceReportComponent> getFirmwareComplianceReportComponents() {
        return firmwareComplianceReportComponents;
    }

    public void setFirmwareComplianceReportComponents(
            List<FirmwareComplianceReportComponent> firmwareComplianceReportComponents) {
        this.firmwareComplianceReportComponents = firmwareComplianceReportComponents;
    }

    public boolean isCompliant() {
        return isCompliant;
    }

    public void setCompliant(boolean isCompliant) {
        this.isCompliant = isCompliant;
    }

    public String getFirmwareRepositoryName() {
        return firmwareRepositoryName;
    }

    public void setFirmwareRepositoryName(String firmwareRepositoryName) {
        this.firmwareRepositoryName = firmwareRepositoryName;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public ManagedState getManagedState() {
        return managedState;
    }

    public void setManagedState(ManagedState managedState) {
        this.managedState = managedState;
    }

    public boolean isEmbededRepo() {
        return embededRepo;
    }

    public void setEmbededRepo(boolean embededRepo) {
        this.embededRepo = embededRepo;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    /**
     * Sorts this classes FirmwareComplianceReportComponents by name if they exist (or does nothing if empty).
     */
    public void sortFirmwareComplianceReportComponentsByNameAndStatus() {
        
        if (this.getFirmwareComplianceReportComponents() != null && 
                !this.getFirmwareComplianceReportComponents().isEmpty()) {
            Collections.sort(this.getFirmwareComplianceReportComponents(), SORT_BY_NAME_AND_COMPLIANCE_COMPARATOR);
        }
    }

}
