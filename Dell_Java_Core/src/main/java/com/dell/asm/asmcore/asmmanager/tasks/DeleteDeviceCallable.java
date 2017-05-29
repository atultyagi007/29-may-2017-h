/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import java.util.List;
import java.util.concurrent.Callable;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

public class DeleteDeviceCallable implements Callable<DeviceInventoryEntity> {

    private static final Logger logger = Logger.getLogger(DeleteDeviceCallable.class);
    // For Audit log message creation
    private LocalizableMessageService logService = LocalizableMessageService.getInstance();
    private DeviceInventoryEntity device = null;
    private DeviceInventoryDAO deviceInventoryDAO;

    @Override
    public DeviceInventoryEntity call() {
        logger.debug("DeleteDeviceCallable: in call method");
        DeviceInventoryEntity deviceEntity = new DeviceInventoryEntity();
        try {
            deviceEntity = deleteDevice();
        } catch (LocalizedWebApplicationException lwae) {
            AsmDetailedMessageList asmdml = lwae.getEEMILocalizedMessageList();
            if (asmdml != null) {
                List<AsmDetailedMessage> lstAsmDM = lwae.getEEMILocalizedMessageList().getMessages();
                if (lstAsmDM.size() > 0 && lstAsmDM.get(0) != null) {
                    logger.debug("DeleteDeviceCallable call error:" + lstAsmDM.get(0).getDisplayMessage());
                    deviceEntity.setStatusMessage(lstAsmDM.get(0).getDisplayMessage());
                }
            }
            deviceEntity.setState(DeviceState.DELETE_FAILED);
        }

        // Set status as DELETED
        deviceEntity.setState(DeviceState.DELETED);
        return deviceEntity;
    }

    public DeleteDeviceCallable(DeviceInventoryEntity device) {
        this.device = device;
    }

    private DeviceInventoryEntity deleteDevice() {
        logger.debug("DeleteDeviceCallable: deleting device IP=" + device.getIpAddress() +", type=" +
                device.getDeviceType().toString() + ", tag=" + device.getServiceTag());

        try {
            // check if we assigned any IP and release it
            if (device.getServiceTag()!=null)
                IPAddressPoolMgr.getInstance().releaseIPAddressesByUsageId(device.getServiceTag());

            try {
                if (DeviceType.isRAServer(device.getDeviceType())) {
                    logger.debug("deleting RA server");
                    ProxyUtil.getDeviceServerProxyWithHeaderSet().deleteServer(device.getRefId());
                    logService.logMsg(AsmManagerMessages.deletedServerSuccessfully(device.getServiceTag()), LogMessage.LogSeverity.INFO,
                            LogMessage.LogCategory.MISCELLANEOUS);
                }
            }catch(Exception se) {
                logger.error("Attempt to delete server failed (already deleted?)", se);
            }

            logger.debug("deleting device inventory with device id:" + device.getRefId());
            getDeviceInventoryDAO().deleteDeviceInventory(device);

            // delete all associated discovery history
            try {
                List<DiscoveryResultEntity> results = DiscoveryJobUtils.getDiscoveryResultEntities(device.getRefId());
                if (results != null && results.size() > 0) {
                    for (DiscoveryResultEntity entity : results) {
                        DiscoveryResultDAO.getInstance().deleteDiscoveryResult(entity.getParentJobId());
                        DeviceDiscoverDAO.getInstance().deleteDiscoveryResult(entity.getParentJobId());
                    }
                }
            }catch(Exception e) {
                logger.error("Delete discovery data failed for device: " + device.getRefId() + ", IP=" + device.getIpAddress(), e);
            }

            // Make the call to asm-deployer to delete puppet device config/cert data
            logger.debug("deleting device inventory with device id:" + device.getRefId());
            try {
                ProxyUtil.getAsmDeployerProxy().deleteDevice(device.getRefId());
            }catch(Exception e) {
                logger.warn("No deployments found or failed to delete, device: " + device.getRefId() + ", IP=" + device.getIpAddress());
            }
        } catch (Exception e) {
            logger.error("Cannot delete device refId=" + device.getRefId(), e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        return device;
    }

    public DeviceInventoryDAO getDeviceInventoryDAO() {
        if (deviceInventoryDAO == null) {
            deviceInventoryDAO = new DeviceInventoryDAO();
        }
        return deviceInventoryDAO;
    }

    public void setDeviceInventoryDAO(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO = deviceInventoryDAO;
    }
}
