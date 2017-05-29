/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.discovery.IDiscoveryProvider;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;
import com.dell.pg.jraf.api.ref.IDeviceRef;
import com.dell.pg.jraf.client.common.JrafAsyncResponse;
import com.dell.pg.jraf.client.discoverymgr.JrafDiscoveryResult;
import com.dell.pg.jraf.client.jobmgr.JrafJobExecStatus;
import org.apache.log4j.Logger;

public abstract class JRAFDevice implements IDiscoveryProvider {
    private static final Logger logger = Logger.getLogger(JRAFDevice.class);

    protected InfrastructureDevice device;
    protected DiscoveredDevices result;

    private DeviceType inventoryDeviceType = DeviceType.unknown;

    @Override
    public DeviceType getInventoryDeviceType() {
        return inventoryDeviceType;
    }

    @Override
    public void setInventoryDeviceType(DeviceType inventoryDeviceType) {
        this.inventoryDeviceType = inventoryDeviceType;
    }

    private String discoveryPattern = "";

    @Override
    public void setDiscoverDeviceType(DiscoverDeviceType deviceType) {
        this.discoverDeviceType = deviceType;
    }

    private DiscoverDeviceType discoverDeviceType = DiscoverDeviceType.UNKNOWN;

    @Override
    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    @Override
    public String getFirmwareComponentID() {
        return firmwareComponentID;
    }

    @Override
    public void setFirmwareComponentID(String firmwareComponentID) {
        this.firmwareComponentID = firmwareComponentID;
    }

    private String firmwareComponentID = "";

    /**
     * Poll for completion of a discovery job, then verify results
     *
     * @param resp response from the job (including job name)
     * @throws InterruptedException
     */
    protected void pollForJrafJobCompletion(JrafAsyncResponse resp)
            throws InterruptedException {
        String jobName = resp.getJobName();
        logger.debug("Discovery Device Child jobName " + jobName);
        JrafJobExecStatus status = null;
        result.setJobId(jobName);
        for (int iter = 0; iter < ProxyUtil.MAX_POLL_ITER; ++iter) {
            Thread.sleep(ProxyUtil.POLLING_INTERVAL);
            status = ProxyUtil.getHistoryProxy().pollExecStatus(jobName);
            result.setStatus(DiscoveryStatus.PENDING);
            DiscoveryJobUtils.updateDiscoveryResult(result);
            if (status.isTerminal())
                break;
        }
        if (status != JrafJobExecStatus.SUCCESSFUL) {
            logger.error("discovery job failed with status " + status);
            result.setStatus(DiscoveryStatus.FAILED);
            DiscoveryJobUtils.updateDiscoveryResult(result);
        }else {
            JrafDiscoveryResult discoveryResult = ProxyUtil.getDmProxy().getDiscoveredDevices(jobName);
            for (String deviceKey : discoveryResult.deviceKeys()) {
                if (discoveryResult.isSuccessful(deviceKey)) {
                    processSuccess(discoveryResult.getDeviceRef(deviceKey));
                }
            }
        }
    }

    /**
     * To implement by extending class.
     * @param deviceRef
     */
    abstract void processSuccess(IDeviceRef deviceRef);

}
