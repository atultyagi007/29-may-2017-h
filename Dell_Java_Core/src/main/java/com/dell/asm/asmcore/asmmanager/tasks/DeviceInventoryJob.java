/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;

import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.localizablelogger.LogMessage.LogCategory;
import com.dell.asm.localizablelogger.LogMessage.LogSeverity;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.JobStatus;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

/**
 * 
 * given an IP or IP range with credentials Discover the list of devices either
 * of Server or Chassis type. Job scheduled by the JRAF core Job Manager
 * 
 */

public class DeviceInventoryJob extends ASMInventoryJob {
    
    public static final String DEVICEINVENTORY_KEY_DATA = "DeviceData";
    public static final String INVALID_DISCOVER_IP_OR_RANGE = "Missing IP Address or credentials";
    private static final String INVENTORY_JOB_STATUS_MSG = "DeviceData.status.msg";

    private static final Logger logger = Logger.getLogger(DeviceInventoryJob.class);

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.info("Executing DeviceInventoryJob");
        initializeFromJobContext(context);
        String xmlData = context.getJobDetail().getJobDataMap().getString(DEVICEINVENTORY_KEY_DATA);
        logger.info("Discovery data:" + xmlData);
        ManagedDevice managedDevice;
        managedDevice = MarshalUtil.unmarshal(ManagedDevice.class, xmlData);
        setJobStatus(JobStatus.IN_PROGRESS);

        boolean success = true;

        if (managedDevice != null 
                && StringUtils.isNotBlank(managedDevice.getIpAddress())
                && StringUtils.isNotBlank(managedDevice.getCredId())) {

            List<DiscoverDeviceCallable> list = new ArrayList<>();

            InfrastructureDevice device = new InfrastructureDevice(managedDevice.getIpAddress(),
                    managedDevice.getCredId(), managedDevice.getCredId(), managedDevice.getCredId(),
                    managedDevice.getCredId(), managedDevice.getCredId(), managedDevice.getCredId(),
                    managedDevice.getCredId());

            device.setUnmanaged(managedDevice.getManagedState() == ManagedState.UNMANAGED);
            device.setConfig(managedDevice.getConfig());
            device.setRequestedDeviceType(managedDevice.getDeviceType());
            device.setFromInventoryJob(true);
            device.setExistingRefId(managedDevice.getRefId());

            DiscoverDeviceCallable worker = new DiscoverDeviceCallable(device, getJobName());
            worker.setDeviceType(managedDevice.getDiscoverDeviceType());

            list.add(worker);

            // wait for discover to finish
            for (DiscoverDeviceCallable callable : list) {
                DiscoveredDevices deviceInfo = null;
                try {
                    deviceInfo = callable.call();

                    if (deviceInfo.getStatus() == DiscoveryStatus.IGNORE) {
                        // skip this device
                        logger.debug("Discovery returned ignorable device at " + deviceInfo.getIpAddress() + ", jobId=" + deviceInfo.getJobId());
                        continue;
                    }else if (deviceInfo.getStatus() == DiscoveryStatus.ERROR
                            || deviceInfo.getStatus() == DiscoveryStatus.FAILED
                            || deviceInfo.getStatus() == DiscoveryStatus.UNSUPPORTED) {

                        logger.error("Discovery by IP range failed for device: serviceTag: "
                                + deviceInfo.getServiceTag() + ", ipAddress: " + deviceInfo.getIpAddress()
                                + ", message: " + deviceInfo.getStatusMessage() + ", jobId=" + deviceInfo.getJobId());

                        LocalizableMessageService.getInstance().logMsg(
                                AsmManagerMessages.discoveryError(deviceInfo.getStatusMessage()), LogMessage.LogSeverity.ERROR,
                                LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

                        continue;
                    }

                    if (deviceInfo.getDeviceRefId() == null) {
                        // cannot proceed with such device
                        String msg = "Discovery by IP range return device with refId=null. ServiceTag: "
                                + deviceInfo.getServiceTag() + ", ipAddress: " + deviceInfo.getIpAddress()
                                + ", message: " + deviceInfo.getStatusMessage() + ", jobId=" + deviceInfo.getJobId();
                        logger.error(msg);

                        LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError(deviceInfo.getStatusMessage()),
                                LogMessage.LogSeverity.ERROR,
                                LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

                        continue;
                    }

                    if (deviceInfo.getJobId() != null 
                            && deviceInfo.getDeviceType() != null
                            && deviceInfo.getDeviceType() != DeviceType.unknown) {
                        getPuppetModuleUtil().saveDeviceConfigFile(managedDevice);
                        logger.debug("inventory completed successfully for jobId=" + deviceInfo.getJobId());
                        getDeviceInventoryUtils().updateInventory(deviceInfo);
                    } else {
                        logger.warn("Ignoring bogus device:" + deviceInfo.getDeviceType() + " refId ="
                                + deviceInfo.getDeviceRefId() + ", jobId=" + deviceInfo.getJobId());
                    }
                } catch (Exception e) {
                    addJobDetail(INVENTORY_JOB_STATUS_MSG,
                                 "discovery failed");
                    logger.error(e.getMessage()+ ", jobId=" + ((deviceInfo != null)?deviceInfo.getJobId():"none"), e);
                    success = false;
                } finally {
                    if (deviceInfo != null && deviceInfo.getJobId() != null)
                        getDiscoveryService().deleteDiscoveryResult(deviceInfo.getJobId());
                }
            }
        } else {
            // invalid IP range
            success = false;
            if (null != managedDevice) {
                logger.debug("Missing IP address " + managedDevice.getRefId());
                addJobDetail(INVALID_DISCOVER_IP_OR_RANGE,
                             "Missing IP address " + managedDevice.getRefId());
            }
        }

        // all done
        if (success)
            setJobStatus(JobStatus.SUCCESSFUL);
        else
            setJobStatus(JobStatus.FAILED);

        logger.info("discovery job completed for the job name:" + getJobName());

        // delete job
        getDiscoveryResultDAO().deleteDiscoveryResult(getJobName());
        getDeviceDiscoverDAO().deleteDiscoveryResult(getJobName());

        String serviceTag = "unknown";
        if (null != managedDevice) {
            serviceTag = managedDevice.getServiceTag();
        }
        LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.inventoryCompletedOnHostMsg(serviceTag),
                LogSeverity.INFO, LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
