/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmdeployer.client.AsmDeployerComponentStatus;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class DeviceConfigurationJobTest {

    private DeviceConfigurationJob deviceConfigurationJob;

    private JobExecutionContext jobContext;
    private JobDetail jobDetail;
    private JobDataMap jobDataMap;
    private JobKey jobKey;

    private IJobManager jobManager;
    private IJobHistoryManager historyMgr;
    private LocalizableMessageService logService;
    private DeviceInventoryDAO deviceInventoryDAO;
    private ServiceTemplateUtil serviceTemplateUtil;
    private FirmwareUtil firmwareUtil;
    private IAsmDeployerService asmDeployerService;


    @Before
    public void setUp() throws Exception {

        deviceConfigurationJob = new DeviceConfigurationJob();

        jobManager = mock(IJobManager.class);
        historyMgr = mock(IJobHistoryManager.class);
        logService = mock(LocalizableMessageService.class);
        deviceInventoryDAO = mock(DeviceInventoryDAO.class);
        serviceTemplateUtil = mock(ServiceTemplateUtil.class);
        firmwareUtil = mock(FirmwareUtil.class);
        asmDeployerService = mock(IAsmDeployerService.class);

        deviceConfigurationJob.setAsmDeployerService(asmDeployerService);
        deviceConfigurationJob.setFirmwareUtil(firmwareUtil);
        deviceConfigurationJob.setJobManager(jobManager);
        deviceConfigurationJob.setJobHistoryManager(historyMgr);
        deviceConfigurationJob.setLogService(logService);
        deviceConfigurationJob.setDeviceInventoryDAO(deviceInventoryDAO);
        deviceConfigurationJob.setServiceTemplateUtil(serviceTemplateUtil);

        jobKey = new JobKey("DeviceConfigurationJobKey", "DeviceConfigurationJobGroup");

        //setup JobExecutionContext
        jobDataMap = mock(JobDataMap.class);
        when(jobDataMap.getString(JobManager.JM_JOB_HISTORY_JOBNAME)).thenReturn("TestDeviceConfigurationJob");

        jobDetail = mock(JobDetail.class);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(jobDetail.getKey()).thenReturn(jobKey);

        jobContext = mock(JobExecutionContext.class);
        when(jobContext.get(JobManager.JM_EXEC_HISTORY_ID)).thenReturn(10L);
        when(jobContext.getJobDetail()).thenReturn(jobDetail);


    }

    @Test
    public void testDeviceConfigurationJobUpdatesDeviceStatusIndividually() throws IOException, JobExecutionException, AsmManagerCheckedException {

            URL url = Resources.getResource("util/testDeviceConfigurationJobDeployment1.json");
            String json = Resources.toString(url, Charsets.UTF_8);
            when(jobDataMap.getString(DeviceConfigurationJob.ServiceDeploymentJob_SERVICE_KEY_DATA)).thenReturn(json);

            //Initialize Mock Create Status
            AsmDeployerStatus createStatus = new AsmDeployerStatus();
            createStatus.setStatus(DeploymentStatusType.IN_PROGRESS);
            List<AsmDeployerComponentStatus> createStatusList = new ArrayList<>();
            createStatus.setComponents(createStatusList);

            AsmDeployerComponentStatus chassisStatus = new AsmDeployerComponentStatus();
            chassisStatus.setId("Chassis1");
            chassisStatus.setStatus(DeploymentStatusType.PENDING);
            createStatus.getComponents().add(chassisStatus);

            AsmDeployerComponentStatus iomStatus = new AsmDeployerComponentStatus();
            iomStatus.setId("IOM1");
            iomStatus.setStatus(DeploymentStatusType.PENDING);
            createStatus.getComponents().add(iomStatus);

            AsmDeployerComponentStatus bladeStatus = new AsmDeployerComponentStatus();
            bladeStatus.setId("Blade1");
            bladeStatus.setStatus(DeploymentStatusType.PENDING);
            createStatus.getComponents().add(bladeStatus);
            when(asmDeployerService.createDeployment(any(Deployment.class))).thenReturn(createStatus);

            //Initialize Mock deploymentStatus
            AsmDeployerStatus deploymentStatus = new AsmDeployerStatus();
            deploymentStatus.setStatus(DeploymentStatusType.ERROR);
            List<AsmDeployerComponentStatus> deploymentStatusList = new ArrayList<>();
            deploymentStatus.setComponents(deploymentStatusList);

            AsmDeployerComponentStatus chassisStatus2 = new AsmDeployerComponentStatus();
            chassisStatus2.setId("Chassis1");
            chassisStatus2.setStatus(DeploymentStatusType.COMPLETE);
            deploymentStatus.getComponents().add(chassisStatus2);

            AsmDeployerComponentStatus iomStatus2 = new AsmDeployerComponentStatus();
            iomStatus2.setId("IOM1");
            iomStatus2.setStatus(DeploymentStatusType.COMPLETE);
            deploymentStatus.getComponents().add(iomStatus2);

            AsmDeployerComponentStatus bladeStatus2 = new AsmDeployerComponentStatus();
            bladeStatus2.setId("Blade1");
            bladeStatus2.setStatus(DeploymentStatusType.ERROR);
            deploymentStatus.getComponents().add(bladeStatus2);

            when(asmDeployerService.getDeploymentStatus(any(String.class))).thenReturn(deploymentStatus);

            DeviceInventoryEntity chassis = new DeviceInventoryEntity();
            chassis.setRefId("Chassis1");
            chassis.setDeviceType(DeviceType.Chassis);
            chassis.setManagedState(ManagedState.MANAGED);
            when(deviceInventoryDAO.getDeviceInventory("Chassis1")).thenReturn(chassis);

            DeviceInventoryEntity iom = new DeviceInventoryEntity();
            iom.setRefId("IOM1");
            iom.setDeviceType(DeviceType.dellswitch);
            iom.setManagedState(ManagedState.MANAGED);
            when(deviceInventoryDAO.getDeviceInventory("IOM1")).thenReturn(iom);

            DeviceInventoryEntity blade = new DeviceInventoryEntity();
            blade.setRefId("Blade1");
            blade.setDeviceType(DeviceType.BladeServer);
            blade.setManagedState(ManagedState.MANAGED);
            when(deviceInventoryDAO.getDeviceInventory("Blade1")).thenReturn(blade);

            deviceConfigurationJob.execute(jobContext);

            ArgumentCaptor<DeviceInventoryEntity> deviceInventoryEntityArgumentCaptor = ArgumentCaptor.forClass(DeviceInventoryEntity.class);
            verify(deviceInventoryDAO, times(7)).updateDeviceInventory(deviceInventoryEntityArgumentCaptor.capture());
            List<DeviceInventoryEntity> entities = deviceInventoryEntityArgumentCaptor.getAllValues();
            for (DeviceInventoryEntity entity : entities) {
                switch (entity.getRefId()) {
                case "Chassis1":
                    assertTrue(DeviceState.CONFIGURATION_ERROR.equals(entity.getState()));
                    break;
                case "IOM1":
                    assertTrue(DeviceState.READY.equals(entity.getState()));
                    break;
                default:
                }
            }
    }
}