/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;

public class FirmwareComplianceReportTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSortByTypeComplianceAndIPAddressComparator() {
        FirmwareComplianceReport[] reports = new FirmwareComplianceReport[6];

        //Server 1 - Compliant
        FirmwareComplianceReport report1 = new FirmwareComplianceReport();
        report1.setDeviceType(DeviceType.BladeServer);
        report1.setCompliant(true);
        report1.setIpAddress("192.168.1.200");
        reports[0] = report1;

        //Storage 1 - Compliant
        FirmwareComplianceReport report2 = new FirmwareComplianceReport();
        report2.setDeviceType(DeviceType.equallogic);
        report2.setCompliant(true);
        report2.setIpAddress("172.16.2.200");
        reports[1] = report2;

        // Server 2 - Non Compliant
        FirmwareComplianceReport report3 = new FirmwareComplianceReport();
        report3.setDeviceType(DeviceType.BladeServer);
        report3.setCompliant(false);
        report3.setIpAddress("192.168.1.201");
        reports[2] = report3;

        //Storage 2 - Non Compliant
        FirmwareComplianceReport report4 = new FirmwareComplianceReport();
        report4.setDeviceType(DeviceType.equallogic);
        report4.setCompliant(false);
        report4.setIpAddress("172.16.2.201");
        reports[3] = report4;

        // Server 3 - Non Compliant
        FirmwareComplianceReport report5 = new FirmwareComplianceReport();
        report5.setDeviceType(DeviceType.BladeServer);
        report5.setCompliant(false);
        report5.setIpAddress("192.168.1.199");
        reports[4] = report5;

        //Storage 2 - Non Compliant
        FirmwareComplianceReport report6 = new FirmwareComplianceReport();
        report6.setDeviceType(DeviceType.equallogic);
        report6.setCompliant(false);
        report6.setIpAddress("172.16.2.199");
        reports[5] = report6;

        Arrays.sort(reports, FirmwareComplianceReport.SORT_BY_TYPE_COMPLIANCE_AND_IP_ADDRESS);

        assertTrue(reports.length == 6);
        assertTrue(reports[0] == report5);
        assertTrue(reports[1] == report3);
        assertTrue(reports[2] == report1);
        assertTrue(reports[3] == report6);
        assertTrue(reports[4] == report4);
        assertTrue(reports[5] == report2);
    }
}
