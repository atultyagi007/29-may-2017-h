/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.test.utils.UtilityClassTester;
import com.dell.pg.asm.chassis.app.rest.ChassisService;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IOM;
import com.dell.pg.asm.chassis.client.device.TagType;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Alan_Cooper on 10/19/13.
 */
public class DeviceInventoryUtilsTest {
    private DeviceInventoryUtils deviceInventoryUtils;

    static String CHASSIS_ID = "ff8080814f742b7a014f74a928160002";
    static String CHASSIS_TAG = "ENV05C1";
    static String IOM_TAG = "BCQPTS1";
    static String templateXml;

    @BeforeClass
    public static void setUpOnce() throws IOException {
        URL url = Resources.getResource("util/configTemplate.xml");
        templateXml = Resources.toString(url, Charsets.UTF_8);
    }

    @Before
    public void setUp() throws IOException {
        deviceInventoryUtils = mockDeviceInventoryUtils();
    }

    private DeviceInventoryUtils mockDeviceInventoryUtils() throws IOException {
        GenericDAO genericDAO = mock(GenericDAO.class);
        FirmwareRepositoryDAO firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
        DeviceInventoryDAO deviceInventoryDAO = mock(DeviceInventoryDAO.class);
        DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = mock(DeviceInventoryComplianceDAO.class);
        DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
        IJobManager jobManager = mock(IJobManager.class);
        IJobHistoryManager jobHistoryManager = mock(IJobHistoryManager.class);
        ObjectMapper mapper = new ObjectMapper();

        FirmwareUtil firmwareUtil = new FirmwareUtil(firmwareRepositoryDAO,
                deviceInventoryDAO,
                deviceInventoryComplianceDAO,
                deploymentDAO,
                genericDAO,
                jobManager,
                jobHistoryManager);

        DeviceInventoryUtils utils = new DeviceInventoryUtils();
        utils.setDeviceInventoryDAO(deviceInventoryDAO);
        utils.setFirmwareUtil(firmwareUtil);

        URL resource = this.getClass().getClassLoader().getResource("chassis_" + CHASSIS_TAG + ".json");
        String text = IOUtils.toString(resource, Charsets.UTF_8);
        Chassis chassis = mapper.readValue(text, Chassis.class);

        ChassisService chassisService = mock(ChassisService.class);
        when(chassisService.getChassis(CHASSIS_ID)).thenReturn(chassis);
        when(chassisService.getChassisByServiceTag(IOM_TAG, TagType.IOM.value())).thenReturn(chassis);
        utils.setChassisService(chassisService);

        DeviceInventoryEntity iom = new DeviceInventoryEntity();
        iom.setServiceTag(IOM_TAG);
        when(deviceInventoryDAO.getDeviceInventoryByServiceTag(IOM_TAG)).thenReturn(iom);

        DeviceInventoryEntity chs = new DeviceInventoryEntity();
        chs.setConfig(templateXml);
        iom.setServiceTag(CHASSIS_TAG);
        when(deviceInventoryDAO.getDeviceInventoryByServiceTag(CHASSIS_TAG)).thenReturn(chs);

        ServiceDeploymentUtil serviceDeploymentUtil = new ServiceDeploymentUtil(firmwareUtil, deploymentDAO, deviceInventoryDAO, deviceInventoryComplianceDAO);
        utils.setServiceDeploymentUtil(serviceDeploymentUtil);
        return utils;
    }

    @Test
    public void testChassisIdForDeviceInventoryEntity() throws AsmManagerCheckedException {
        DiscoveredDevices deviceInfo = new DiscoveredDevices();
        DeviceInventoryEntity devInv = new DeviceInventoryEntity();

        deviceInfo.setChassisId(CHASSIS_ID);
        deviceInventoryUtils.updateChassisId(deviceInfo, devInv);
        assertEquals("ChassiId is not set", CHASSIS_ID, devInv.getChassisId());

        deviceInfo.setChassisId(null);
        deviceInfo.setDeviceType(DeviceType.dellswitch);
        deviceInfo.setServiceTag(IOM_TAG);
        devInv.setChassisId(null);
        deviceInventoryUtils.updateChassisId(deviceInfo, devInv);
        assertEquals("ChassiId is not set", CHASSIS_ID, devInv.getChassisId());

        deviceInfo.setChassisId(null);
        deviceInfo.setDeviceRefId(CHASSIS_ID);
        deviceInfo.setDeviceType(DeviceType.ChassisFX);
        deviceInfo.setServiceTag(CHASSIS_TAG);

        deviceInventoryUtils.updateChassisId(deviceInfo, devInv);
        DeviceInventoryEntity iom = deviceInventoryUtils.getDeviceInventoryDAO().getDeviceInventoryByServiceTag(IOM_TAG);
        assertEquals("ChassiId is not set", CHASSIS_ID, iom.getChassisId());
    }

    @Test
    public void testChassisIdForManagedDevice() throws AsmManagerCheckedException {
        DiscoveredDevices deviceInfo = new DiscoveredDevices();
        ManagedDevice devInv = new ManagedDevice();

        deviceInfo.setChassisId(CHASSIS_ID);
        deviceInventoryUtils.updateChassisId(deviceInfo, devInv);
        assertEquals("ChassiId is not set", CHASSIS_ID, devInv.getChassisId());

        deviceInfo.setChassisId(null);
        deviceInfo.setDeviceType(DeviceType.dellswitch);
        deviceInfo.setServiceTag(IOM_TAG);
        devInv.setChassisId(null);
        deviceInventoryUtils.updateChassisId(deviceInfo, devInv);
        assertEquals("ChassiId is not set", CHASSIS_ID, devInv.getChassisId());

        deviceInfo.setChassisId(null);
        deviceInfo.setDeviceRefId(CHASSIS_ID);
        deviceInfo.setDeviceType(DeviceType.ChassisFX);
        deviceInfo.setServiceTag(CHASSIS_TAG);

        deviceInventoryUtils.updateChassisId(deviceInfo, devInv);
        DeviceInventoryEntity iom = deviceInventoryUtils.getDeviceInventoryDAO().getDeviceInventoryByServiceTag(IOM_TAG);
        assertEquals("ChassiId is not set", CHASSIS_ID, iom.getChassisId());
    }

    @Test
    public void testGetCredentialsFromChassisConfig() {

        DeviceInventoryEntity chassis = deviceInventoryUtils.findDeviceInventoryByServiceTag(CHASSIS_TAG);
        ServiceTemplate configTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                templateXml);
        
        String chassisCred = deviceInventoryUtils.getChassisCredentialFromConfigurationTemplate(configTemplate.getComponents().get(0));
        assertEquals("Chassis credentials", "CHASSIS_CRED", chassisCred);

        String iomCred = deviceInventoryUtils.findIOMCredentialFromChassis(chassis);
        assertEquals("IOM credentials", "IOM_CRED", iomCred);

        String bladeCred = deviceInventoryUtils.findBladeCredentialFromChassis(chassis);
        assertEquals("Blade credentials", "IDRAC_CRED", bladeCred);

        String rackCred = deviceInventoryUtils.getRackServerCredentialFromConfigurationTemplate(configTemplate.getComponents().get(0));
        assertEquals("Rack credentials", "IDRAC_CRED", rackCred);
    }

}
