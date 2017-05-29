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
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.common.threadpool.ThreadPoolManager;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

/**
 * Each device delete has its own job and monitors jobs for each servers/IOAs.
 * 
 */
public class DeleteDeviceJob extends AsmDefaultJob {

    public static final String DELETE_DEVICE_JOB_KEY_NAME = "DeleteDevice.JobKey.name";
    public static final String DELETE_DEVICE_JOB_KEY_GROUP = "DeleteDevice.JobKey.group";
    public static final String DELETE_DEVICE_JOB_DEVICE_REF_ID = "DeleteDevice.Job.Device.Reference.ID";
    public static final String DELETE_DEVICE_JOB_FORCE_DELETE_OPTION = "DeleteDevice.Job.forceDelete.option";

    // adding sub device failed status detail message
    public static final String DELETE_DEVICE_JOB_FAILED_FOR_ID = "DeleteDevice.Job.Failed.For.ID";
    public static final String DELETE_DEVICE_JOB_SUCCEEDED_FOR_ID = "DeleteDevice.Job.Succeeded.For.ID";

    private static final String DEVICE_RA_ID = "deviceID";
    private static final String RELATED_DEVICE_RA_IDS = "relatedDeviceIDs";
    private static final String DEVICE_FORCE_DELETE = "forceDelete";

    private static final Logger logger = Logger.getLogger(DeleteDeviceJob.class);

    // For Audit log message creation
    private LocalizableMessageService logService;
    private ServiceTemplateUtil serviceTemplateUtil;
    private DeviceInventoryDAO deviceInventoryDAO;

    @Override
    protected void executeSafely(JobExecutionContext context){
    	logger.debug("in DeleteDeviceJob : executeSafely method");
        // final String refId = getMainDeviceId(context);
        final DeviceInventoryEntity deviceEntity = getServerInventoryEntity(context);
        final List<DeviceInventoryEntity> relatedDeviceEntities = getServerInventoryEntities(context);
        final boolean forceDelete = getForceDeleteOption(context);
        try {
            initializeFromJobContext(context);
            JobKey jobKey = context.getJobDetail().getKey();
            addJobDetail(DELETE_DEVICE_JOB_KEY_GROUP, jobKey.getGroup());
            addJobDetail(DELETE_DEVICE_JOB_KEY_NAME, jobKey.getName());
            addJobDetail(DELETE_DEVICE_JOB_DEVICE_REF_ID, deviceEntity.getRefId());
            addJobDetail(DELETE_DEVICE_JOB_FORCE_DELETE_OPTION, forceDelete ? "TRUE" : "FALSE");

            getLogService().logMsg(AsmManagerMessages.deleteDeviceJobStarted(deviceEntity.getServiceTag()), LogMessage.LogSeverity.INFO,
                    LogMessage.LogCategory.MISCELLANEOUS);
            setJobStatus(JobStatus.IN_PROGRESS);

            logger.debug("calling deleteDevice");
            boolean isDeleteJobSuccessful = deleteDevice(deviceEntity, relatedDeviceEntities, forceDelete);

            if (!isDeleteJobSuccessful) {
                setJobStatus(JobStatus.FAILED);
                getLogService().logMsg(AsmManagerMessages.deleteDeviceJobFailed(deviceEntity.getServiceTag()), LogMessage.LogSeverity.INFO,
                        LogMessage.LogCategory.MISCELLANEOUS);
            } else {
                setJobStatus(JobStatus.SUCCESSFUL);
                getLogService().logMsg(AsmManagerMessages.deleteDeviceJobCompleted(deviceEntity.getServiceTag()), LogMessage.LogSeverity.INFO,
                        LogMessage.LogCategory.MISCELLANEOUS);
            }

        } catch (Exception e1) {
            setJobStatus(JobStatus.FAILED);
            getLogService().logMsg(AsmManagerMessages.deleteDeviceJobFailed(deviceEntity.getServiceTag()), LogMessage.LogSeverity.INFO,
                    LogMessage.LogCategory.MISCELLANEOUS);
            logger.error("AsmManagerCheckedException or error scheduling job:" + e1);
        }
    }

    private boolean deleteDevice(DeviceInventoryEntity mainDevice, List<DeviceInventoryEntity> relatedDevices, boolean forceDelete) throws AsmManagerCheckedException {
        logger.debug("deleteDevice entered");
        List<DeviceInventoryEntity> deviceEntitiesToDelete = new ArrayList<>();
        if(relatedDevices != null && relatedDevices.size() != 0) {
            for (DeviceInventoryEntity deviceEntity : relatedDevices) {
                deviceEntity.setState(DeviceState.PENDING_DELETE);
                getDeviceInventoryDAO().updateDeviceInventory(deviceEntity);
                //refresh the object
                deviceEntitiesToDelete.add(getDeviceInventoryDAO().getDeviceInventory(deviceEntity.getRefId()));
            }
        }
        else{
            deviceEntitiesToDelete.add(mainDevice);
        }

        boolean isDeleteDeviceJobFailed = false;
        try {

            logger.debug("Delete Device list size" + deviceEntitiesToDelete.size());

            if (deviceEntitiesToDelete.size() > 0) {

                ThreadPoolManager mgr = ThreadPoolManager.getInstance();
                ExecutorService exec = mgr.getExecutorService();

                List<Future<DeviceInventoryEntity>> listOfDeviceJob = new ArrayList<>();
                logger.debug("For each device, starting thread with DeleteDeviceCallable ");
                for (DeviceInventoryEntity die : deviceEntitiesToDelete) {
                    DeleteDeviceCallable worker = new DeleteDeviceCallable(die);
                    Future<DeviceInventoryEntity> submit = exec.submit(worker);
                    listOfDeviceJob.add(submit);
                }

                logger.debug("Wait for all callables to finish. count:" + listOfDeviceJob.size());

                for (int i = 0; i < ProxyUtil.MAX_POLL_ITER; ++i) {
                    try {
                        Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                    } catch (InterruptedException e) {
                        logger.info("Future job poll sleep interrupted", e);
                    }
                    for (int j = listOfDeviceJob.size() - 1; j >= 0; --j) {
                        Future<DeviceInventoryEntity> future = listOfDeviceJob.get(j);
                        if (future.isDone()) {
                            try {
                                DeviceInventoryEntity deviceEntity = future.get();
                                logger.debug("Future got the device:" + deviceEntity.getIpAddress() + " State:" + deviceEntity.getState());
                                if (deviceEntity.getState() == DeviceState.DELETE_FAILED) {

                                    logger.debug("Delete operation failed for device(or associated device) id:" + deviceEntity.getRefId() + " State:"
                                            + deviceEntity.getState());

                                    // TODO: need to do audit log?
                                    addJobDetail(DELETE_DEVICE_JOB_FAILED_FOR_ID + "." + deviceEntity.getRefId(), deviceEntity.getStatusMessage());

                                    // Reverting back status message details in entity to null
                                    deviceEntity.setStatusMessage(null);

                                    // Update device record with state DELETE_FAILED
                                    getDeviceInventoryDAO().updateDeviceInventory(deviceEntity);

                                    isDeleteDeviceJobFailed = true;// To stop or cancel all other device delete operation threads
                                    if (!forceDelete) {
                                        break;
                                    }
                                } else {
                                    addJobDetail(DELETE_DEVICE_JOB_SUCCEEDED_FOR_ID + "." + deviceEntity.getRefId(), "Device Successfully deleted");
                                    listOfDeviceJob.remove(j);
                                    logger.debug("Removed thread for device ip:" + deviceEntity.getIpAddress());
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                logger.error("Error deleting device", e);
                            }
                        } else if (future.isCancelled()) {
                            logger.debug("Remove Cancelled Job.");
                            listOfDeviceJob.remove(j);
                        }
                    }

                    // If any of the device delete operation FAILED and forceDelete option is false; then cancel all other threads
                    if (isDeleteDeviceJobFailed && !forceDelete) {
                        logger.debug("Delete operation failed and so cancelling the other parallel delete device threads");
                        for (int j = listOfDeviceJob.size() - 1; j >= 0; --j) {
                            Future<DeviceInventoryEntity> future = listOfDeviceJob.get(j);
                            try {
                                DeviceInventoryEntity deviceEntity = future.get();
                                // Update device record with state DELETE_FAILED for remaining jobs
                                // TODO: Actually the status should be DELETE_CANCELLED
                                getDeviceInventoryDAO().updateDeviceInventory(deviceEntity);
                            } catch (InterruptedException | ExecutionException e) {
                                logger.error("Error while getting entity from future: " + e);
                            }
                            future.cancel(true);

                            listOfDeviceJob.remove(j);
                        }
                        // stop the polling and so return the failure
                        break;
                    }
                    if (listOfDeviceJob.size() < 1)
                        break;
                }
            }

            // Incase of Chassis
            if (DeviceType.isChassis(mainDevice.getDeviceType())) {

                if (isDeleteDeviceJobFailed && !forceDelete) {
                    logger.debug("updating Chasis with state DELETE_FAILED");
                    mainDevice.setState(DeviceState.DELETE_FAILED);
                    getDeviceInventoryDAO().updateDeviceInventory(mainDevice);
                } else {
                    // If all associated device delete operation succeeded (or) forceDelete option is selected
                    logger.debug("deleting Chasis");
                    ProxyUtil.getDeviceChassisProxyWithHeaderSet().deleteChassis(mainDevice.getRefId());
                    logger.debug("deleting chasis device inventory with device id:" + mainDevice.getRefId());
                    getDeviceInventoryDAO().deleteDeviceInventory(mainDevice);
                    getLogService().logMsg(AsmManagerMessages.deletedChassisSuccessfully(mainDevice.getServiceTag()), LogMessage.LogSeverity.INFO,
                            LogMessage.LogCategory.MISCELLANEOUS);

                    // check if we assigned any IP and release it
                    if (mainDevice.getServiceTag()!=null)
                        IPAddressPoolMgr.getInstance().releaseIPAddressesByUsageId(mainDevice.getServiceTag());

                    getServiceTemplateUtil().decryptPasswordsInConfig(MarshalUtil.unmarshal(ServiceTemplate.class, mainDevice.getConfig()));

                    // delete all associated discovery history
                    try {
                        List<DiscoveryResultEntity> results = DiscoveryJobUtils.getDiscoveryResultEntities(mainDevice.getRefId());
                        if (results != null && results.size() > 0) {
                            for (DiscoveryResultEntity entity : results) {
                                DiscoveryResultDAO.getInstance().deleteDiscoveryResult(entity.getParentJobId());
                                DeviceDiscoverDAO.getInstance().deleteDiscoveryResult(entity.getParentJobId());
                            }
                        }
                    }catch(Exception e) {
                        logger.error("Delete discovery data failed for device: " + mainDevice.getRefId() + ", IP=" + mainDevice.getIpAddress(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred in deleteDevice method", e);
            throw new AsmRuntimeException(AsmManagerMessages.internalError(), e);
        }

        return !isDeleteDeviceJobFailed;

    }

    /**
     * Get the id for the device this job applies to. This value is saved in the Job Data map.
     * 
     * @param context
     *            Execution context for the device's job.
     * @return refId string for the device
     */
    public String getMainDeviceId(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(DEVICE_RA_ID);
    }

    public List<String> getRelatedDeviceIds(JobExecutionContext context){
        return (List<String>) context.getJobDetail().getJobDataMap().get(RELATED_DEVICE_RA_IDS);
    }

    public boolean getForceDeleteOption(JobExecutionContext context) {
        return (boolean) context.getJobDetail().getJobDataMap().get(DEVICE_FORCE_DELETE);
    }

    /**
     * Get the DeviceInventoryEntity for the device this job is running on.
     * 
     * @param context
     *            Execution context for the device's job.
     * @return DeviceInventoryEntity for the device
     */
    public List<DeviceInventoryEntity> getServerInventoryEntities(JobExecutionContext context) {
        final List<String> deviceRefIds = getRelatedDeviceIds(context);
        return this.getDeviceInventoryDAO().getDevicesByIds(deviceRefIds);
    }
    /**
     * Get the DeviceInventoryEntity for the device this job is running on.
     *
     * @param context
     *            Execution context for the device's job.
     * @return DeviceInventoryEntity for the device
     */
    public DeviceInventoryEntity getServerInventoryEntity(JobExecutionContext context) {
        final String deviceRefId = getMainDeviceId(context);
        return this.getDeviceInventoryDAO().getDeviceInventory(deviceRefId);
    }

    public LocalizableMessageService getLogService() {
        if (logService == null) {
            logService = LocalizableMessageService.getInstance();
        }
        return logService;
    }

    public void setLogService(LocalizableMessageService logService) {
        this.logService = logService;
    }

    public ServiceTemplateUtil getServiceTemplateUtil() {
        if (serviceTemplateUtil == null) {
            serviceTemplateUtil = new ServiceTemplateUtil();
        }
        return serviceTemplateUtil;
    }

    public void setServiceTemplateUtil(ServiceTemplateUtil serviceTemplateUtil) {
        this.serviceTemplateUtil = serviceTemplateUtil;
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

    @Override
    public Logger getLogger() {
        return logger;
    }
}
