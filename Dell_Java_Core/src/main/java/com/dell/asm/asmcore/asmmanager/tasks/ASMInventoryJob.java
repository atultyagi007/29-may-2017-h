/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.client.discovery.IDiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.localizablelogger.LocalizableMessageService;

/**
 * Class should be extended by ONLY discovery/inventory jobs.
 * Do not propagate this as a general abstract class everything extends from.
 */
abstract class ASMInventoryJob extends AsmDefaultJob {
    private PuppetModuleUtil puppetModuleUtil;
    private DeviceInventoryUtils deviceInventoryUtils;
    private DeviceInventoryDAO deviceInventoryDAO;
    private LocalizableMessageService logService;
    private FirmwareUtil firmwareUtil;
    private IDiscoverIPRangeDevicesService discoveryService;
    private DiscoveryResultDAO discoveryResultDAO;
    private DeviceDiscoverDAO deviceDiscoverDAO;

    PuppetModuleUtil getPuppetModuleUtil() {
        if (puppetModuleUtil == null) {
            puppetModuleUtil = new PuppetModuleUtil();
        }
        return puppetModuleUtil;
    }

    public void setPuppetModuleUtil(PuppetModuleUtil puppetModuleUtil) {
        this.puppetModuleUtil = puppetModuleUtil;
    }

    DeviceInventoryUtils getDeviceInventoryUtils() {
        if (deviceInventoryUtils == null) {
            deviceInventoryUtils = new DeviceInventoryUtils();
        }
        return deviceInventoryUtils;
    }

    public void setDeviceInventoryUtils(DeviceInventoryUtils deviceInventoryUtils) {
        this.deviceInventoryUtils = deviceInventoryUtils;
    }

    DeviceInventoryDAO getDeviceInventoryDAO() {
        if (deviceInventoryDAO == null) {
            deviceInventoryDAO = new DeviceInventoryDAO();
        }
        return deviceInventoryDAO;
    }

    public void setDeviceInventoryDAO(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO = deviceInventoryDAO;
    }

    LocalizableMessageService getLogService() {
        if (logService == null) {
            logService = LocalizableMessageService.getInstance();
        }
        return logService;
    }

    public void setLogService(LocalizableMessageService logService) {
        this.logService = logService;
    }

    FirmwareUtil getFirmwareUtil() {
        if (firmwareUtil == null) {
            firmwareUtil = new FirmwareUtil();
        }
        return firmwareUtil;
    }

    public void setFirmwareUtil(FirmwareUtil firmwareUtil) {
        this.firmwareUtil = firmwareUtil;
    }

    DiscoveryResultDAO getDiscoveryResultDAO() {
        if (discoveryResultDAO == null) {
            discoveryResultDAO = DiscoveryResultDAO.getInstance();
        }
        return discoveryResultDAO;
    }

    public void setDiscoveryResultDAO(DiscoveryResultDAO discoveryResultDAO) {
        this.discoveryResultDAO = discoveryResultDAO;
    }

    IDiscoverIPRangeDevicesService getDiscoveryService() {
        if (discoveryService == null) {
            discoveryService = ProxyUtil.getDiscoveryProxy();
        }
        return discoveryService;
    }

    public void setDiscoveryService(IDiscoverIPRangeDevicesService discoveryService) {
        this.discoveryService = discoveryService;
    }

    DeviceDiscoverDAO getDeviceDiscoverDAO() {
        if (deviceDiscoverDAO == null) {
            deviceDiscoverDAO = DeviceDiscoverDAO.getInstance();
        }
        return deviceDiscoverDAO;
    }

}
