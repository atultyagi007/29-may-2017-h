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
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInvalidCredentialException;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

/**
 *
 * given an IP or IP range with credentials Discover the list of Chassis type.
 * Job scheduled by the JRAF core Job Manager
 * @author Bapu_Patil
 *
 */

public class DiscoverIpRangeForChassisJob extends ASMInventoryJob {
    private static final String DISCOVER_IP_RANGE_JOB_KEY_NAME = "DiscoverIpRangeForChassis.JobKey.name";
    private static final String DISCOVER_IP_RANGE_JOB_KEY_GROUP = "DiscoverIpRangeForChassis.JobKey.group";
    private static final String DISCOVER_IP_RANGE_JOB_STATUS_MSG = "DiscoverIpRangeForChassis.status.msg";
    public static final String DISCOVERIPRANGE_SERVICE_KEY_DATA = "DiscoverIpRangeForChassis";

    private static final Logger logger = Logger.getLogger(DiscoverIpRangeForChassisJob.class);

    ThreadPoolExecutor threadPoolExecutor;

    @Override
    protected void executeSafely(JobExecutionContext context){
    	logger.info(" Executing DiscoverIpRangeJob");
        initializeFromJobContext(context);
        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(DISCOVER_IP_RANGE_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(DISCOVER_IP_RANGE_JOB_KEY_NAME, jobKey.getName());
        String xmlData = context.getJobDetail().getJobDataMap().getString(DISCOVERIPRANGE_SERVICE_KEY_DATA);

        logger.info("Discovery data:" + xmlData);

        DiscoverIPRangeDeviceRequests discoverIpRangeRequests;
        discoverIpRangeRequests = MarshalUtil.unmarshal(DiscoverIPRangeDeviceRequests.class, xmlData);
        setJobStatus(JobStatus.IN_PROGRESS);

        threadPoolExecutor = DiscoverySingleTonThreadPool.getDiscoverySingleTonThreadPool();

        List<Future<DiscoveredDevices>> list = new ArrayList<>();

        logger.info("Discovery size:" + discoverIpRangeRequests.getDiscoverIpRangeDeviceRequests().size());

        //  for each device check the range and the credentials
        for (DiscoverIPRangeDeviceRequest deviceInfo : discoverIpRangeRequests.getDiscoverIpRangeDeviceRequests())
        {
            try {
                List<String> expandedIpRange = null;
                try {
                    expandedIpRange = DiscoverIPRangeDeviceRequest.expandIpAddresses(deviceInfo);
                }catch(IllegalArgumentException iae) {
                    logger.error("Invalid IP range", iae);
                }
                if (expandedIpRange != null && !expandedIpRange.isEmpty())
                {
                    for (String ipAddress : expandedIpRange)
                    {
                        InfrastructureDevice device = new InfrastructureDevice(ipAddress,
                                deviceInfo.getDeviceChassisCredRef(), deviceInfo.getDeviceServerCredRef(),
                                deviceInfo.getDeviceSwitchCredRef(), deviceInfo.getDeviceVCenterCredRef(),
                                deviceInfo.getDeviceStorageCredRef(), deviceInfo.getDeviceSCVMMCredRef(),
                                deviceInfo.getDeviceEMCredRef());

                        device.setUnmanaged(deviceInfo.isUnmanaged());
                        device.setReserved(deviceInfo.isReserved());
                        device.setServerPoolId(deviceInfo.getServerPoolId());
                        device.setRequestedDeviceType(deviceInfo.getDeviceType());
                        device.setQuickDiscovery(true);
                        DiscoverDeviceCallable worker = new DiscoverDeviceCallable(device, getJobName());

                        Future<DiscoveredDevices> submit = threadPoolExecutor.submit(worker);
                        list.add(submit);
                    }
                }
                else
                {
                    // invalid IP range
                    logger.debug("invalid IP range for starting Ip "
                            + deviceInfo.getDeviceStartIp() + " ending ip"
                            + deviceInfo.getDeviceEndIp());
                    // update job status
                    addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "Invalid IP or range " + deviceInfo.getDeviceStartIp() + " " + deviceInfo.getDeviceEndIp());
                    LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError("Invalid or empty IP range for device " + deviceInfo.getDeviceType()), LogMessage.LogSeverity.ERROR,
                            LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
                }
            }
            catch (AsmManagerInvalidCredentialException ace)
            {
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG + "deviceInfo.getDeviceStartIp()", "Invalid credential for IP or range " + deviceInfo.getDeviceStartIp() + " " + deviceInfo.getDeviceEndIp());
            }
        }

        // wait for discover to finish. At least one device must be in state CONNECTED for job success
        boolean success= false;
        for (Future<DiscoveredDevices> future : list) {
            try {
                DiscoveredDevices deviceInfo = future.get();
                logger.debug("Discovered (quick): " + deviceInfo.getIpAddress() + " status: " + deviceInfo.getStatus());
                success = success || deviceInfo.getStatus() == DiscoveryStatus.CONNECTED;

                if (deviceInfo.getDeviceType()!=null &&
                        deviceInfo.getServiceTag()!=null &&
                        deviceInfo.getIpAddress()!=null &&
                        deviceInfo.getStatus() == DiscoveryStatus.CONNECTED) {
                    ManagedDevice devInfo = new ManagedDevice();
                    devInfo.setDeviceType(deviceInfo.getDeviceType());
                    devInfo.setServiceTag(deviceInfo.getServiceTag());
                    devInfo.setIpAddress(deviceInfo.getIpAddress());
                    devInfo.setCredId(deviceInfo.getCredId());
                    devInfo.setDiscoverDeviceType(deviceInfo.getDiscoverDeviceType());

                    getPuppetModuleUtil().saveDeviceConfigFile(devInfo);
                    logger.debug("Discovered (quick): " + deviceInfo.getIpAddress());
                }

            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "discovery failed");
                success = false;
            } catch (ExecutionException e) {
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "discovery failed");
                logger.error(e.getMessage(), e);
                success = false;
            }
        }

        // all done
        if (!success)
            setJobStatus(JobStatus.FAILED);
        else
            setJobStatus(JobStatus.SUCCESSFUL);
        logger.info("discovery job completed for the job name:" + getJobName());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
