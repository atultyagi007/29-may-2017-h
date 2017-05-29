package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.IDiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ChassisDeviceState;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil.DriverType;
import com.dell.asm.asmcore.asmmanager.util.tasks.JobUtils;
import com.dell.asm.asmdeployer.client.AsmDeployerComponentStatus;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.JobCreateSpec;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

public class DeviceConfigurationJob extends AsmDefaultJob {

    public static final String ServiceDeploymentJob_JOB_KEY_NAME = "DeviceConfiguration.JobKey.name";
    public static final String ServiceDeploymentJob_JOB_KEY_GROUP = "DeviceConfiguration.JobKey.group";
    public static final String ServiceDeploymentJob_SERVICE_KEY_DATA = "DeviceConfiguration";

    private static final long SLEEP_UPDATE_INVENTORY_POLL = 30 * 1000; // 30 seconds
    private static final long MAX_FIRMWARE_APPLY_MILLIS = 300 * 60 * 1000; // 5 hours

    private static final Logger logger = Logger.getLogger(DeviceConfigurationJob.class);

    private DeviceInventoryUtils deviceInventoryUtils;
    private FirmwareUtil firmwareUtil;
    private LocalizableMessageService logService;
    private DeviceInventoryDAO deviceInventoryDAO;
    private IAsmDeployerService asmDeployerService;
    private ServiceTemplateUtil serviceTemplateUtil;

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.info("Executing Configuration Job");
        initializeFromJobContext(context);
        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(ServiceDeploymentJob_JOB_KEY_GROUP,
                              jobKey.getGroup());
        addJobDetail(ServiceDeploymentJob_JOB_KEY_NAME,
                     jobKey.getName());
        String jsonData = context.getJobDetail().getJobDataMap().getString(ServiceDeploymentJob_SERVICE_KEY_DATA);

        logger.debug("DeviceConfigurationJob " + getJobName() + "-" + getExecHistoryId() + " JSON data is: " + jsonData);

        setJobStatus(JobStatus.IN_PROGRESS);

        String tags = "";

        try {
            AsmDeployerStatus status;
            Deployment deployment = DeploymentService.fromJson(jsonData);
            FirmwareRepositoryEntity repo = null;

            getServiceTemplateUtil().encryptPasswordsInConfig(deployment.getServiceTemplate());
            saveConfiguration(deployment.getServiceTemplate());

            if (deployment.getServiceTemplate() != null) {
                if (deployment.getServiceTemplate().getComponents() != null) {
                    // get the catalog
                    repo = getFirmwareUtil().getDefaultRepo();
                    if (repo == null) {
                        repo = getFirmwareUtil().getEmbeddedRepo();
                    }
                    if (repo != null) {
                        // This should be adding firmware components that need to be updated on each
                        // server that is marked as manageFirmware=true
                        getFirmwareUtil().addFirmwareUpdateComponentsToDeployment(repo, deployment, false);
                    }
                }
            }

            for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                if (DeviceType.isChassis(component.getPuppetCertName())) {
                    if (tags.length() > 0)
                        tags += ",";
                    tags += component.getComponentID();
                }
            }

            deployment.getServiceTemplate().removeHiddenValues();

            String deploymentId = deployment.getId();
            status = getAsmDeployerService().createDeployment(deployment);
            logger.debug("Deployment " + deploymentId + " created with in Asm Deployer at " + new Date().getTime());

            List<ChassisDeviceState> components = new ArrayList<>();
            List<String> deviceIds = new ArrayList<>();
            for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                ChassisDeviceState cds = new ChassisDeviceState(component.getId(), DeviceState.UPDATING);
                components.add(cds);
                if (component.getAsmGUID() != null) {
                    deviceIds.add(component.getAsmGUID());
                } else if (component.getId() != null) {
                    deviceIds.add(component.getId());
                }
            }
            JobUtils.updateChassisStatus(components, null, getDeviceInventoryDAO());

            long elapsed = 0L;
            long start = new Date().getTime();
            logger.debug("Polling for Deployment " + deploymentId + " for device configuration job started at " + start);
            while (DeploymentStatusType.IN_PROGRESS.equals(status.getStatus()) && elapsed < MAX_FIRMWARE_APPLY_MILLIS) {
                Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                status = getAsmDeployerService().getDeploymentStatus(deploymentId);
                elapsed = new Date().getTime() - start;
            }

            logger.debug("Polling for Deployment " + deploymentId + " for device configuration job finished at: " + new Date().getTime() + " elapsed: " + elapsed);
            if (DeploymentStatusType.IN_PROGRESS == status.getStatus()) {
                logger.error("Could not complete configuration job " + deploymentId + " in " + (MAX_FIRMWARE_APPLY_MILLIS / 1000 / 60) + " minutes");
                status.setStatus(DeploymentStatusType.CANCELLED);
            }

            // Update Device Inventories to reflect firmware updates
            if (CollectionUtils.isNotEmpty(deviceIds)) {
                List<DeviceInventoryEntity> deviceInventoryEntities = getDeviceInventoryDAO().getDevicesByIds(deviceIds);
                if (CollectionUtils.isNotEmpty(deviceInventoryEntities)) {
                    Map<String,DeviceInventoryEntity> updateDevicesMap = new HashMap<>();
                    Set<DeviceInventoryEntity> removeChassisSet = new HashSet<>();
                    // loop through and add all chassis to the updateDeviceMap first
                    for (DeviceInventoryEntity deviceInventoryEntity : deviceInventoryEntities) {
                        if (DeviceType.isChassis(deviceInventoryEntity.getDeviceType())) {
                            updateDevicesMap.put(deviceInventoryEntity.getRefId(),deviceInventoryEntity);
                            // cleanup set of already added devices.
                            removeChassisSet.add(deviceInventoryEntity);
                            logger.debug("Run Device Inventory on Chassis Service Tag " + deviceInventoryEntity.getServiceTag() + " Id " + deviceInventoryEntity.getRefId());
                        }
                    }
                    if (removeChassisSet.size() > 0) {
                        deviceInventoryEntities.removeAll(removeChassisSet);
                    }
                    // loop through again adding all necessary
                    for (DeviceInventoryEntity deviceEntity : deviceInventoryEntities) {
                        // check to see if device is part of chassis and chassis added to run inventory on
                        if (deviceEntity.getChassisId() != null &&
                                updateDevicesMap.get(deviceEntity.getChassisId()) != null) {
                            // if chassis already added to list ignore device as chassis will inventory devices in chassis
                            logger.debug("Skipping Device Inventory for Service Tag " + deviceEntity.getServiceTag() + " Type " + deviceEntity.getDeviceType().getLabel() + " Id " + deviceEntity.getRefId() + " in Chassis " + deviceEntity.getChassisId());
                            continue;
                        }
                        logger.debug("Run Device Inventory on Service Tag " + deviceEntity.getServiceTag() + " Type " + deviceEntity.getDeviceType().getLabel() + " Id " + deviceEntity.getRefId());
                        updateDevicesMap.put(deviceEntity.getRefId(),deviceEntity);
                    }
                    runInventoryJobsAndBlockUntilComplete(updateDevicesMap.values());
                }
            }

            components.clear();
            for (AsmDeployerComponentStatus componentStatus : status.getComponents()) {
                logger.debug("AsmDeployerStatus for Id " + componentStatus.getId() + " Name " + componentStatus.getName());
                if (componentStatus.getStatus() != null) {
                    logger.debug("AsmDeployerStatus status " + componentStatus.getStatus().getLabel());
                }
                if (componentStatus.getMessage() != null) {
                    logger.debug("AsmDeployerStatus message " + componentStatus.getMessage());
                }
                //default value should be READY
                DeviceState ds = DeviceState.READY;
                // Used switch for future expansion
                switch (componentStatus.getStatus()) {
                case ERROR:
                    ds = DeviceState.CONFIGURATION_ERROR;
                    break;
                default:
                    break;
                }
                components.add(new ChassisDeviceState(componentStatus.getId(), ds));
            }

            if (DeploymentStatusType.COMPLETE != status.getStatus()) {
                setJobStatus(JobStatus.FAILED);
                // by passing in CONFIGURATION_ERROR chassis state will be updated to configuration error
                JobUtils.updateChassisStatus(components, DeviceState.CONFIGURATION_ERROR, getDeviceInventoryDAO());

                logger.error("Configuration " + deploymentId + " final status was " + status.getStatus());
                getLogService().logMsg(AsmManagerMessages.applyConfigurationFailed(getJobName()).getDisplayMessage(),
                                       LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);

            } else {
                setJobStatus(JobStatus.SUCCESSFUL);
                // by passing in READY chassis state will be updated to ready
                JobUtils.updateChassisStatus(components, DeviceState.READY, getDeviceInventoryDAO());

                logger.info("DeviceConfiguration for the job name:" + getJobName());
                getLogService().logMsg(AsmManagerMessages.completedConfigurationJob(tags, getJobName()).getDisplayMessage(),
                                       LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }

            // Set server components back from updating to ready
            getFirmwareUtil().updateComponentStatus(deployment, false);

            // Set still uncompliant servers state to UPDATE_FAILED
            updateServerFirmwareStatus(repo, deployment, false);

        } catch (Exception e) {
            setJobStatus(JobStatus.FAILED);

            logger.error("DeviceConfiguration.execute error :", e);
            getLogService().logMsg(AsmManagerMessages.applyConfigurationFailed(getJobName()).getDisplayMessage(),
                                   LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            return;
        }
    }

    private void runInventoryJobsAndBlockUntilComplete(final Collection<DeviceInventoryEntity> deviceEntities) throws JobManagerException,
            InterruptedException, SchedulerException {
        if (deviceEntities != null && deviceEntities.size() > 0) {
            JobDetail job;
            String groupName = getUniqueJobGroupName("UpdateInventory");
            for (DeviceInventoryEntity deviceEntity : deviceEntities) {

                JobCreateSpec jobSpec = new JobCreateSpec(DeviceInventoryJob.class);
                jobSpec.setDescription(groupName);
                jobSpec.setSelector(groupName);

                // Setting true will allow us to search jobs by groupName
                job = getJobManager().createNamedJob(jobSpec, true);

                ManagedDevice device = getDeviceInventoryUtils().toDTO(deviceEntity, true);
                job.getJobDataMap().put(DeviceInventoryJob.DEVICEINVENTORY_KEY_DATA, MarshalUtil.marshal(device));

                logger.debug("Creating Device Inventory Job for Device " + device.getServiceTag() + " type " + device.getDeviceType().getLabel());
                getJobManager().createAndStartTrigger(job);
            }
            waitForInventoryUpdateJobs(groupName);
        }
    }

    /**
     * Wait for a set of jobs to complete given the group name that they share.
     *
     * @param groupName
     */
    private void waitForInventoryUpdateJobs(String groupName) {

        long start = new Date().getTime();
        long elapsed = 0L;

        try {
            Thread.sleep(SLEEP_UPDATE_INVENTORY_POLL);
            Set<JobKey> runningSet = getJobManager().getScheduler().getJobKeys(GroupMatcher.jobGroupContains(groupName));

            while (runningSet != null && runningSet.size() > 0 && elapsed < FirmwareUpdateJob.MAX_FIRMWARE_APPLY_MILLIS) {
                logger.debug(runningSet.size() + " device inventory jobs remaining");
                Thread.sleep(SLEEP_UPDATE_INVENTORY_POLL);
                runningSet = getJobManager().getScheduler().getJobKeys(GroupMatcher.jobGroupContains(groupName));
                elapsed = new Date().getTime() - start;
            }
            if (runningSet != null && runningSet.size() > 0) {
                logger.warn(String.format("Timeout waiting for %d Inventory Update Jobs", runningSet.size()));
            }
        } catch (Exception e) {
            logger.warn("Error while waiting Inventory Update jobs but ignoring it", e);
        }
    }

    private synchronized String getUniqueJobGroupName(String prefix) {
        return prefix + ":" + getJobName();
    }

    private void saveConfiguration(ServiceTemplate serviceTemplate) {
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {

            DeviceInventoryEntity devInv = getDeviceInventoryDAO().getDeviceInventory(component.getId());
            if (devInv == null) {
                logger.error("Attempt to save configuration when device is not in DB: " + component.getId() + ", certname=" + component.getPuppetCertName());
                continue;
            }

            String config = devInv.getConfig();
            ServiceTemplate templateFromDB;
            if (config != null) {
                templateFromDB = MarshalUtil.unmarshal(ServiceTemplate.class, config);

                // for existing config template replace resources with the same from deployment, or add
                for (ServiceTemplateCategory deplResource : component.getResources()) {
                    for (ServiceTemplateComponent templateFromDBComponent : templateFromDB.getComponents()) {
                        if (templateFromDBComponent.getPuppetCertName().equals(component.getPuppetCertName())) {
                            Iterator<ServiceTemplateCategory> it = templateFromDBComponent.getResources().iterator();
                            while (it.hasNext()) {
                                ServiceTemplateCategory resource = it.next();
                                if (resource.getId().equals(deplResource.getId())) {
                                    it.remove();
                                    break;
                                }
                            }
                            templateFromDBComponent.getResources().add(deplResource);
                        }
                    }
                }
            } else {
                templateFromDB = new ServiceTemplate();
                templateFromDB.getComponents().add(component); // save component with device
            }

            devInv.setConfig(MarshalUtil.marshal(templateFromDB));
            try {
                getDeviceInventoryDAO().updateDeviceInventory(devInv);
            } catch (AsmManagerCheckedException e) {
                logger.error("Cannot update device_inventory for device: " + component.getId(), e);
            }
        }
    }

    /** Get servers that were selected for firmware updates that are still not compliant
     *  and set their state to UPDATE_FAILED
     *
     *  @param repo The repository to check compliance against
     *  @param deployment The deployment to find servers
     *  @param allServers Update all servers or only those marked in the deployment to manage firmware
     */
    private void updateServerFirmwareStatus(FirmwareRepositoryEntity repo, Deployment deployment, boolean allServers) {
        try {
            List<DeviceInventoryEntity> uncompliant_servers = getFirmwareUtil().getFirmwareUpdateDevicesInDeployment(repo, deployment, allServers, DriverType.ALL);
            for (DeviceInventoryEntity device : uncompliant_servers) {
                logger.debug("Failed to Update Firmware on Server " + device.getServiceTag() + " Id " + device.getRefId());
                device.setState(DeviceState.UPDATE_FAILED);
                getDeviceInventoryDAO().updateDeviceInventory(device);
            }
        } catch (Exception e) {
            logger.error("Failed to update server firmware compliance status");
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public void setDeviceInventoryUtils(DeviceInventoryUtils deviceInventoryUtils) {
        this.deviceInventoryUtils = deviceInventoryUtils;
    }

    protected DeviceInventoryUtils getDeviceInventoryUtils() {
        if (deviceInventoryUtils == null) {
            deviceInventoryUtils = new DeviceInventoryUtils();
        }
        return deviceInventoryUtils;
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

    public LocalizableMessageService getLogService() {
        if (logService == null) {
            logService = LocalizableMessageService.getInstance();
        }
        return logService;
    }

    public void setLogService(LocalizableMessageService logService) {
        this.logService = logService;
    }

    public FirmwareUtil getFirmwareUtil() {
        if (firmwareUtil == null) {
            firmwareUtil = new FirmwareUtil();
        }
        return firmwareUtil;
    }

    public void setFirmwareUtil(FirmwareUtil firmwareUtil) {
        this.firmwareUtil = firmwareUtil;
    }

    public IAsmDeployerService getAsmDeployerService() {
        if (asmDeployerService == null) {
            asmDeployerService = ProxyUtil.getAsmDeployerProxy();
        }
        return asmDeployerService;
    }

    public void setAsmDeployerService(IAsmDeployerService asmDeployerService) {
        this.asmDeployerService = asmDeployerService;
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
}

