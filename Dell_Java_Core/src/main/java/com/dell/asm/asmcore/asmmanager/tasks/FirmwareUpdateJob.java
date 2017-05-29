package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareRepositoryFileUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil.DriverType;
import com.dell.asm.asmdeployer.client.AsmDeployerDeploymentLogEntry;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.impl.matchers.GroupMatcher;


public class FirmwareUpdateJob extends AsmDefaultJob {

    private static final Logger logger = Logger.getLogger(FirmwareUpdateJob.class);

    /**
     * The UPDATE_KEY will either represent a deploymentId or a deviceRefId, depending on whether the update firmware
     * upgrade is occurring on a Service or an individual device. 
     */
    public static final String UPDATE_KEY = "refId";// List of ref_id
    public static final String UPDATE_TYPE_KEY = "update.Type";

    /**
     *  UPDATE_TYPE_DEPLOYMENT_DEVICE - used for updating devices in parallel during an ongoing deployment
     *  UPDATE_TYPE_DEVICE - used for updating devices in parallel
     *  UPDATE_TYPE_SERVICE - used to update devices in serial for a deployment.
     */
    public static final String UPDATE_TYPE_DEPLOYMENT_DEVICE = "deploymentDevice";
    public static final String UPDATE_TYPE_DEVICE = "device";
    public static final String UPDATE_TYPE_SERVICE = "service";

    public static final String FIRMWARE_REPOSITORY = "update.Repo";
    public static final String EXIT_MAINTANACE = "exitMaint";
    public static final String NEXT_REBOOT = "nextReboot";
    public static final String JOB_KEY_NAME = "FirmwareUpdateJob.JobKey.name";
    public static final String JOB_KEY_GROUP = "FirmwareUpdateJob.JobKey.group";

    // For job control of parallel jobs
    public static final String GROUP_SELECTOR_KEY  = "FirmwareUpdateJob.JobKey.groupSelector";
    public static final String DEPLOYMENT_STATE_KEY  = "FirmwareUpdateJob.JobKey.deploymentState";

    public static final String COMPONENTS_TO_UPDATE = "components.to.update";
    public static final String COMPONENTS_SOFTWARE = "software";
    public static final String COMPONENTS_FIRMWARE = "firmware";

    public static final int MAX_REPO_DOWNLOAD_WAIT_MILLIS = 30 * 60 * 1000; // 30 minutes
    public static final long MAX_FIRMWARE_APPLY_MILLIS = 90 * 60 * 1000; // 1.5 hours

    private static final int MAX_UPDATE_ATTEMPTS = 3;

    private String updateType = null;
    private String componentsToUpdate;

    // Used to indicate this job is part of a group of parallel jobs
    // identified by their common groupSelector.  These are passed
    // into this job except for the deployment entity itself.
    private String groupSelector = null;
    private String deploymentId = null;
    private DeploymentStatusType initialDeploymentState = null;
    private DeploymentEntity deployment = null;
    private List<String> clusterServerList = new ArrayList<>();
    
    private GenericDAO genericDAO;
    private DeviceInventoryDAO deviceInventoryDAO;
    private DeviceInventoryComplianceDAO deviceInventoryComplianceDAO;
    private DeploymentDAO deploymentDAO;
    private FirmwareUtil firmwareUtil;
    private ServiceDeploymentUtil serviceDeploymentUtil;
    private IAsmDeployerService asmDeployerService;

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.info(" Executing FirmwareUpdateJob");
        initializeFromJobContext(context);

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(JOB_KEY_NAME, jobKey.getName());

        componentsToUpdate = context.getJobDetail().getJobDataMap().getString(FirmwareUpdateJob.COMPONENTS_TO_UPDATE);

        String refID = (String)context.getJobDetail().getJobDataMap().get(FirmwareUpdateJob.UPDATE_KEY);
        
        // this.deployment is initialized in the initializeDeploymentForLogging call
        this.initializeDeploymentForLogging(refID);
        this.initializeServerMaitenanceList();
        
        boolean nextReboot = (Boolean)context.getJobDetail().getJobDataMap().get(FirmwareUpdateJob.NEXT_REBOOT);
        updateType = (String)context.getJobDetail().getJobDataMap().get(FirmwareUpdateJob.UPDATE_TYPE_KEY);

        // For parallel jobs
        groupSelector = (String)context.getJobDetail().getJobDataMap().get(FirmwareUpdateJob.GROUP_SELECTOR_KEY);

        String stateStr = (String)context.getJobDetail().getJobDataMap().get(FirmwareUpdateJob.DEPLOYMENT_STATE_KEY);

        if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(componentsToUpdate)) {
            this.addLogMessage(deploymentId, "Updating Firmware components for deployment " + deploymentId);
        }
        else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(componentsToUpdate)) {
            this.addLogMessage(deploymentId, "Updating Software components for deployment " + deploymentId);
        } 
        else {
            this.addLogMessage(deploymentId, "Updating all Resource components for deployment " + deploymentId);
        }
        
        // For any deployments or devices in deployments we expect the initial deployment status to be passed in.
        // This is also to accomodate parallel firmware jobs
        initialDeploymentState = stateStr != null ? DeploymentStatusType.valueOf(stateStr) : null;

        String fwId = context.getJobDetail().getJobDataMap().getString(FIRMWARE_REPOSITORY);

        FirmwareRepositoryEntity firmwareRepository = !StringUtils.isEmpty(fwId) ? getGenericDAO().get(fwId, FirmwareRepositoryEntity.class) : null;
        boolean updatesFailed = false;

        //If we are not supplying a repo to manage (via service level assignment)
        if (firmwareRepository == null)
        {
            logger.debug("Getting repo from default or embedded");
            //get the default catalog
            HashMap<String, Object> attributeMap = new HashMap<>();
            attributeMap.put("isDefault", true);
            List<FirmwareRepositoryEntity> results = getGenericDAO().getForEquals(attributeMap, FirmwareRepositoryEntity.class, true);

            firmwareRepository = (results != null && results.size() > 0) ? results.get(0) : null;

            //If there is no default then we fall back onto embedded.
            if (firmwareRepository == null)
            {
                attributeMap.clear();
                attributeMap.put("isEmbedded", true);
                results = getGenericDAO().getForEquals(attributeMap, FirmwareRepositoryEntity.class, true);

                firmwareRepository = (results != null && results.size() > 0) ? results.get(0) : null;
            }
        }

        if (firmwareRepository != null) {
            // getJobDevices ensures to return only server devices when firmware update is for a service
            final List<DeviceInventoryEntity> devices = getJobDevices(refID, updateType);
            if (CollectionUtils.isEmpty(devices)) {
                logger.error("No current device(s) found for id: " + refID);
                setJobStatus(JobStatus.FAILED);
                return;
            }

            setJobStatus(JobStatus.IN_PROGRESS);
            try {
                RepositoryStatus downloadStatus = firmwareRepository.getDownloadStatus();
                switch (downloadStatus) {
                case PENDING:
                case COPYING:
                    // Firmware sync job is in progress (or will be soon); wait for completion
                    logger.info("Waiting for " + firmwareRepository.getName()
                            + " to become available, current state is " + downloadStatus);
                    new FirmwareRepositoryFileUtil().blockUntilAvailable(firmwareRepository.getId(),
                            MAX_REPO_DOWNLOAD_WAIT_MILLIS);
                    break;
                case ERROR:
                    // Previous firmware sync job failed; kick it off again, maybe it will succeed
                    logger.info("Attempting to re-sync " + firmwareRepository.getName()
                            + " binaries, current status is error");
                    new FirmwareRepositoryFileUtil().syncFirmwareRepository(firmwareRepository.getId());
                    break;
                case AVAILABLE:
                    logger.info("Repository " + firmwareRepository.getName() + " available");
                    break;
                }

                if (initialDeploymentState != null) {
                    // set deployment state
                    setDeploymentStatus(DeploymentStatusType.FIRMWARE_UPDATING);
                }
                int jobNo = 0;
                for (final DeviceInventoryEntity device : devices) {
                    try {
	                	if (!updateDeviceWithRetry(device, jobNo++, firmwareRepository, nextReboot)) {
	                        setJobStatus(JobStatus.FAILED); // Set to Error and continue with processing...
	                        logger.error("FirmwareUpdateJob.execute failed for " + device);
	                        updatesFailed = true;
	                    }
                    } catch (Exception err) {
                    	logger.error("Unhandled Exception thrown causing FirmwareUpdateJob.execute failure for " + device, err);
                    	setJobStatus(JobStatus.FAILED); // Set to Error and continue with processing...
                    	updatesFailed = true;
                    }
                }
            } catch (Exception e) {
                setJobStatus(JobStatus.FAILED);
                logger.error("FirmwareUpdateJob.execute error", e);

                // rerun inventory since we cannot be sure where failure occurred and what updated/failed
                getFirmwareUtil().blockUntilInventoryUpdatesAreComplete(devices);
                return;
            } finally {
                if (initialDeploymentState != null) {
                    // restore deployment state
                    restoreDeploymentStatus(initialDeploymentState);
                }
            }
        } else {
            logger.warn("Unable to apply firmware update because no default firmware repository");
        }

        // If this is a Service (versus individual device update) then try to update the Deployments compliance data
        if (deployment != null) {
            try {
                this.updateDeploymentsFirmwareCompliance(deployment.getId());
            }catch (Exception e) {
                logger.warn("Error when attempting to update a Deployment's compliance status at end of FirmwareUpdateJob for deploymentId " + deployment.getId(), e);
            }
        }
        else {
            DeviceInventoryEntity die = this.getDeviceInventoryDAO().getDeviceInventory(refID);
            if (die.getDeployments() != null && !die.getDeployments().isEmpty()) {
                for (DeploymentEntity dEntity : die.getDeployments()) {
                    // Must reload the depoymentEntity here so it has all the data necessary for compliance calculation
                    try {
                        this.updateDeploymentsFirmwareCompliance(dEntity.getId());
                    } catch (Exception e) {
                        logger.warn("Error attempting to update compliance for device with RefId " + refID + 
                                    " on Deployment with Id " + dEntity.getId() + 
                                    " at end of FirmwareUpdateJob.", e);
                    }
                }
            }
        }

        setJobStatus(JobStatus.SUCCESSFUL);
        
        if(updatesFailed) {
            if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(componentsToUpdate)) {
                this.addLogMessage(deploymentId, "Failed to update all Firmware components for deployment " + deploymentId);
            }
            else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(componentsToUpdate)) {
                this.addLogMessage(deploymentId, "Failed to update all Software components for deployment " + deploymentId);
            } 
            else {
                this.addLogMessage(deploymentId, "Failed to update all Resource components for deployment " + deploymentId);
            }
        } else {
            if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(componentsToUpdate)) {
                this.addLogMessage(deploymentId, "Successfully updated all Firmware components for deployment " + deploymentId);
            }
            else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(componentsToUpdate)) {
                this.addLogMessage(deploymentId, "Successfully updated all Software components for deployment " + deploymentId);
            } 
            else {
                this.addLogMessage(deploymentId, "Successfully updated all Resource components for deployment " + deploymentId);
            }
        }
      
        logger.info("FirmwareUpdateJob for the job name: " + jobKey.getName());
    }

    /**
     * Set status of the parent deployment under certain conditions
     */
    private void setDeploymentStatus(DeploymentStatusType status) {

        // If part of a deployment set the deployment state to FIRMWARE_UPDATING
        // but only if not being managed by a deployment job already
        if (deployment != null && !UPDATE_TYPE_DEPLOYMENT_DEVICE.equals(updateType)) {

            // The only way it should be IN_PROGRESS if it's being managed by a deployment
            if (DeploymentStatusType.IN_PROGRESS.equals(deployment.getStatus())) {
                throw new AsmManagerRuntimeException("Invalid starting state for "
                        + deployment.getName() + " (#" + deployment.getId() + "): " + deployment.getStatus());
            }

            logger.info("Setting deployment " + deployment.getName() + " (#" + deployment.getId()
                    + ") status to: " + status + " from: " + deployment.getStatus() + " for job " + getJobName());

            final DeploymentEntity newDeployment = getDeploymentDAO().getDeployment(deployment.getId(), DeploymentDAO.NONE);
            newDeployment.setStatus(status);
            try {
                getDeploymentDAO().updateDeployment(newDeployment);
            } catch (Exception e) {
                if (groupSelector != null) {
                    logger.error("Unexpected exception while setting Deployment state: " + e.getMessage());
                } else {
                    logger.error("Not unexpected exception while setting Deployment state for parallel jobs: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Restore status of the parent deployment under certain conditions
     */
    private void restoreDeploymentStatus(DeploymentStatusType status) {
        // For UPDATE_TYPE_DEPLOYMENT_DEVICE the deployment state is managed by the parent
        // deployment job so don't try to change it's state here.
        if (deployment != null && !UPDATE_TYPE_DEPLOYMENT_DEVICE.equals(updateType)) {
            try {
                // Set back to original state regardless of current state
                // But only if this is the last job in the group for parallel jobs
                if (groupSelector != null && !lastJobInGroup()) {
                    return;
                }

                final DeploymentEntity newDeployment = getDeploymentDAO().getDeployment(deployment.getId(), DeploymentDAO.NONE);
                logger.info("Deployment status was left in UPDATING for "
                        + newDeployment.getName() + " (#" + newDeployment.getId() + "), job " + getJobName()
                        + "; setting back to " + status);
                newDeployment.setStatus(status);
                getDeploymentDAO().updateDeployment(newDeployment);

            } catch (Exception e) {
                logger.error("Failed to update " + deployment + " state back to " + status, e);
            }
        }
    }

    /**
     * Updates the firmware for a device, retrying up to {@code MAX_UPDATE_ATTEMPTS} times.
     * Will set the resource state to UPDATING and the deployment state of any deployment
     * the resource is in to FIRMWARE_UPDATING, and guarantee that they are reset back to
     * their original state on completion (success or error) of the firmware update.
     *
     * @param device The device to update
     * @param jobNo A job index, i.e. 0 for first server, 1 for second, etc.
     * @param firmwareRepository The firmware repository
     * @param nextReboot Whether to apply the firmware immediately or just stage it so that it
     *                   will be installed when the customer next reboots the server.
     * @return True if the update was successful, false otherwise. If nextReboot is true success
     *         is defined as the last firmware update job result. Otherwsie success is whether
     *         the server is now compliant with the firmware repository.
     */
    private boolean updateDeviceWithRetry(DeviceInventoryEntity device,
                                          int jobNo,
                                          FirmwareRepositoryEntity firmwareRepository,
                                          boolean nextReboot)
            throws InvocationTargetException, 
            	   IllegalAccessException, 
            	   JobManagerException, 
            	   InterruptedException, 
            	   AsmManagerCheckedException {
        
        // Refresh device
        device = getDeviceInventoryDAO().getDeviceInventory(device.getRefId());
        boolean updateSuccess = false;

        final DeviceState originalDeviceState = device.getState();
        if (DeviceState.UPDATING.equals(originalDeviceState)) {
            logger.info("Attempting to update device " + device
                    + " that is already currently updating firmware.  Skipping.");
            return true;
        }

        DeviceInventoryComplianceEntity compliance = getDeviceInventoryComplianceDAO().get(device, firmwareRepository);
        if (!needsFirmwareUpdate(compliance.getCompliance())) {
            logger.info("Attempting to update device " + device
                    + " that is already compliant.  Skipping.");
            // For scheduled jobs where device state was pending, set it back to ready.
            if (device.getState() == DeviceState.PENDING) {
                updateDeviceState(deviceInventoryDAO, DeviceState.READY, device);
            }
            return true;
        }

        try {
            logger.info("Setting " + device + " status to UPDATING for job " + getJobName());
            updateDeviceState(deviceInventoryDAO, DeviceState.UPDATING, device);

            // For nextReboot case we are only staging the firmware to be applied when the server
            // next reboots. In that case there is no need to retry as the server compliance
            // will not change after the job is executed.
            int max_attempts = nextReboot ? 1 : MAX_UPDATE_ATTEMPTS;

            // create an update job and attempt to run firmware updates
            for (int attempts = 0; !updateSuccess && attempts < max_attempts; ++attempts) {
                // Refresh device
                device = getDeviceInventoryDAO().getDeviceInventory(device.getRefId());
                
                String newJobName = String.valueOf(this.getJobName()) + "-" + jobNo + "-" + attempts;
                logger.info("Beginning firmware update job " + newJobName + " for " + device);
                
                // Update Logs that update is starting
                if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(componentsToUpdate)) {
                    this.addLogMessage(deploymentId, "Beginning attempt " + (attempts + 1) + " update for Firmware components in job " + newJobName + " for device with service tag " + device.getServiceTag());
                }
                else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(componentsToUpdate)) {
                    this.addLogMessage(deploymentId, "Beginning attempt " + (attempts + 1) + " update for Software components in job " + newJobName + " for device with service tag " + device.getServiceTag());
                } 
                else {
                    this.addLogMessage(deploymentId, "Beginning attempt " + (attempts + 1) + " update for all Resource components in job " + newJobName + " for device with service tag " + device.getServiceTag());
                }
                
                JobStatus jobStatus = updateADevice(genericDAO, deviceInventoryDAO, deviceInventoryComplianceDAO,
                        device, firmwareRepository, nextReboot, newJobName);
                logger.info("Firmware update job " + newJobName + " status for " + device + " was " + jobStatus);

                boolean needsFirmwareUpdate = false;
                if (!nextReboot) {
                    // rerun inventory
                    logger.info("Re-running inventory for " + device);
                    getFirmwareUtil().runInventoryJobAndBlockUntilComplete(device);

                    // Make sure the firmware is update for this device
                    this.getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(device);
                    
                    // compliant. If not then try the firmware update again if there are attempts left to be made.
                    compliance = getDeviceInventoryComplianceDAO().get(device, firmwareRepository);
                    needsFirmwareUpdate = needsFirmwareUpdate(compliance.getCompliance());
                    logger.info("Compliance status for " + device + " after firmware update job " + newJobName + " is " + compliance.getCompliance());
                }

                // If the puppet job was not successful we want to retry if there are any retries left to be made. In
                // this case updateSuccess should be false. Otherwise if the Job was successful then mark updateSuccess
                // to true only if nextReboot is true or needsFirmwareUpdate is false.
                updateSuccess = !JobStatus.SUCCESSFUL.equals(jobStatus) ? false : (nextReboot || !needsFirmwareUpdate);
                if (!updateSuccess) {
                    // Update Logs that update has failed
                    if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(componentsToUpdate)) {
                        this.addLogMessage(deploymentId, "Update attempt " + (attempts + 1) + " for Firmware components in job " + newJobName + " for device with service tag " + device.getServiceTag() + " has failed!");
                    }
                    else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(componentsToUpdate)) {
                        this.addLogMessage(deploymentId, "Update attempt " + (attempts + 1) + " for Software components in job " + newJobName + " for device with service tag " + device.getServiceTag() + " has failed!");
                    } 
                    else {
                        this.addLogMessage(deploymentId, "Update attempt " + (attempts + 1) + " for all Resource components in job " + newJobName + " for device with service tag " + device.getServiceTag() + " has failed!");
                    }
                } 
                else {
                    // Update Logs that update has succeeded
                    if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(componentsToUpdate)) {
                        this.addLogMessage(deploymentId, "Update attempt " + (attempts + 1) + " for Firmware components in job " + newJobName + " for device with service tag " + device.getServiceTag() + " has succeeded!");
                    }
                    else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(componentsToUpdate)) {
                        this.addLogMessage(deploymentId, "Update attempt " + (attempts + 1) + " for Software components in job " + newJobName + " for device with service tag " + device.getServiceTag() + " has succeeded!");
                    } 
                    else {
                        this.addLogMessage(deploymentId, "Update attempt " + (attempts + 1) + " for all Resource components in job " + newJobName + " for device with service tag " + device.getServiceTag() + " has succeeded!");
                    }
                }
            }

            return updateSuccess;
        } finally {
            DeviceState finalState = null;
            try {
                if (updateSuccess) {
                    /**
                     *  Previously this would return the device to its starting state after a firmware update.
                     *  However, we are now setting the device state to PENDING outside of this job, so the
                     *  previous state will always be PENDING when the job starts.  Leaving this logic here
                     *  in case we pass in the return state with a new job variable in the future.
                     */
                    switch (originalDeviceState) {
                        case CONFIGURATION_ERROR:
                        case DEPLOYMENT_ERROR:
                        case DELETE_FAILED:
                        case DEPLOYED:
                            finalState = originalDeviceState;
                            break;
                        default:
                            finalState = DeviceState.READY;
                            break;
                    }
                    logger.info("Firmware update success.  Setting " + device + " status to "
                            + finalState + " in job " + getJobName());
                    // Log success
                    EEMILocalizableMessage msg = AsmManagerMessages.firmwareUpdateCompleted(device.getServiceTag());
                    LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(),
                                                                   LogMessage.LogSeverity.INFO,
                                                                   LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
                } else {
                    finalState = DeviceState.UPDATE_FAILED;
                    logger.info("Firmware update failed.  Setting " + device + " state to "
                            + finalState + " in job " + getJobName());
                    // Log failure
                    EEMILocalizableMessage msg = AsmManagerMessages.firmwareUpdateFailed(device.getServiceTag());
                    LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(),
                                                                   LogMessage.LogSeverity.ERROR,
                                                                   LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
                }
                updateDeviceState(deviceInventoryDAO, finalState, device);

            } catch (Exception e) {
                logger.error("Failed to update " + device + " final state to " + finalState, e);
            }
        }
    }

    private boolean lastJobInGroup() {
        try {
            IJobManager jm = JobManager.getInstance();
            if (groupSelector == null) {
                return true;
            }
            // There is a race condition here
            Set<JobKey> runningSet = jm.getScheduler().getJobKeys(GroupMatcher.jobGroupContains(groupSelector));
            if (runningSet.size() <= 1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.warn("Error while checking for running jobs in group ", e);
        }
        return false;
    }

    private boolean needsFirmwareUpdate(final CompliantState compliance) {
        return (compliance != null) &&
                (CompliantState.NONCOMPLIANT.equals(compliance)
                        || CompliantState.UNKNOWN.equals(compliance)
                        || CompliantState.UPDATEREQUIRED.equals(compliance));
    }

    /**
     * Get the devices that need updating for this job.  In the case of a service update we will update the devices in sequence.
     * In the other case when updating devices we're only really expecting a single device to update.
     * Multiple device updates will be scheduled in seperate jobs.
     * @param refId
     * @param updateType
     * @return
     */
    private List<DeviceInventoryEntity> getJobDevices(String refId, String updateType) {
        final List<DeviceInventoryEntity> devices = new ArrayList<>();
        if (UPDATE_TYPE_SERVICE.equals(updateType)) {
            final DeploymentEntity deployment = getDeploymentDAO().getDeployment(refId, DeploymentDAO.NONE);
            final ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class,
                    deployment.getMarshalledTemplateData());
            if (template != null && CollectionUtils.isNotEmpty(template.getComponents())) {
                for (final ServiceTemplateComponent component : template.getComponents()) {
                    // Only update servers in a service
                    if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                        final DeviceInventoryEntity device = getDeviceInventoryDAO().getDeviceInventory(component.getAsmGUID());
                        if (device != null) {
                            devices.add(device);
                        }
                    }
                }
            }
        } else {
            DeviceInventoryEntity device = getDeviceInventoryDAO().getDeviceInventory(refId);
            if (device != null) {
                devices.add(device);
            }
        }
        return devices;
    }

    /**
     * The method that applies the update to a single device
     * @param device
     * @param firmwareRepository
     * @param nextReboot
     * @param jobName
     * @throws InterruptedException
     */
    private JobStatus updateADevice(GenericDAO genericDAO, DeviceInventoryDAO deviceInventoryDAO,
            DeviceInventoryComplianceDAO deviceInventoryComplianceDAO, DeviceInventoryEntity device,
            FirmwareRepositoryEntity firmwareRepository, boolean nextReboot, String jobName)
                    throws InterruptedException {

        String templateId = UUID.randomUUID().toString();

        //create the stack
        Deployment deployment = new Deployment();
        ServiceTemplate template = new ServiceTemplate();
        deployment.setServiceTemplate(template);

        boolean forceRestart = !nextReboot;

        List<FirmwareDeviceInventoryEntity> nonCompliantFirmware;
        if (FirmwareUpdateJob.COMPONENTS_FIRMWARE.equals(this.componentsToUpdate)) {
            nonCompliantFirmware = getFirmwareUtil().getNonCompliantFirmware(device, firmwareRepository, true, DriverType.FIRMWARE);
        }
        else if (FirmwareUpdateJob.COMPONENTS_SOFTWARE.equals(this.componentsToUpdate)) {
            nonCompliantFirmware = getFirmwareUtil().getNonCompliantFirmware(device, firmwareRepository, true, DriverType.SOFTWARE);
        } else {
            nonCompliantFirmware = getFirmwareUtil().getNonCompliantFirmware(device, firmwareRepository, true, DriverType.ALL);
        }
        getFirmwareUtil().setFirmwareUpdateTime(nonCompliantFirmware);
        if (nonCompliantFirmware == null || nonCompliantFirmware.isEmpty()) {
            return JobStatus.SUCCESSFUL;
        }

        // Is this different if invoked from a deploy vs a direct firmware update request.
        ServiceTemplateComponent deviceComponent = null;
        if ( !UPDATE_TYPE_DEPLOYMENT_DEVICE.equals(updateType) ) {
            deviceComponent = getServerComponent(device);
        }

        ServiceTemplateComponent updateComponent;
        if (deviceComponent != null) {
            updateComponent = getFirmwareUtil().createFirmwareUpdateComponent(deviceComponent.getName(), device, nonCompliantFirmware, firmwareRepository, deviceComponent, this.clusterServerList, forceRestart);
        }
        else {
            updateComponent = getFirmwareUtil().createFirmwareUpdateComponent(device, nonCompliantFirmware, firmwareRepository, this.clusterServerList, forceRestart);
        }

        if (updateComponent != null) {
            template.getComponents().add(updateComponent);
        }

        deployment.setId(jobName);
        deployment.setDeploymentName("FirmwareUpdateJob" + jobName);

        template.setId(templateId);
        template.setTemplateName("FirmwareUpdateJob" + " Template " + templateId);
        template.setCategory("firmware");

        logger.debug("Successfully constructed deployment message " + DeploymentService.toJson(deployment));


        // call ruby rest endpoint
        IAsmDeployerService proxy = ProxyUtil.getAsmDeployerProxy();

        /**
         *  Set device state to UPDATING.  Not sure if this is still necessary here.
         */
        updateDeviceState(deviceInventoryDAO, DeviceState.UPDATING, device);

        // Note the PUT / POST below use the jsonData string rather than
        // allowing WebClient to marshal the deployment. The format looks
        // incorrect when WebClient is marshalling it; it exhibits
        // the problems seen when the jettison library is used, see
        // DeploymentService.toJson for more details.
        AsmDeployerStatus status = proxy.createDeployment(deployment);

        long elapsed = 0L;
        long start = new Date().getTime();
        logger.debug("Polling for firmware update job started at " + start);
        while (DeploymentStatusType.IN_PROGRESS.equals(status.getStatus()) && elapsed < MAX_FIRMWARE_APPLY_MILLIS) {
            Thread.sleep(ProxyUtil.POLLING_INTERVAL);
            status = proxy.getDeploymentStatus(jobName);
            elapsed = new Date().getTime() - start;
        }
        logger.debug("Polling for firmware update job finished at " + new Date().getTime() + " elapsed " + elapsed);
        if (DeploymentStatusType.IN_PROGRESS.equals(status.getStatus()) && (elapsed >= MAX_FIRMWARE_APPLY_MILLIS)) {
            throw new AsmManagerRuntimeException("Failed to update a device due to timeout. Aborting rest of update."
                    + " Failed on: " + device.getRefId());
        }

        if (!DeploymentStatusType.COMPLETE.equals(status.getStatus())) {
            logger.error("Deployment " + jobName + " final status was " + status.getStatus());
            return JobStatus.FAILED;
        }



        return JobStatus.SUCCESSFUL;
    }

    private static ServiceTemplateComponent getServerComponent (DeviceInventoryEntity device){
        if (DeviceType.isServer(device.getDeviceType())) {
            if (device.getDeployments() != null) {
                for (DeploymentEntity originalDeployment : device.getDeployments()) {
                    ServiceTemplate oldTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, originalDeployment.getMarshalledTemplateData());
                    for (ServiceTemplateComponent component : oldTemplate.getComponents()) {
                        String puppetCertName = component.getPuppetCertName();
                        if (puppetCertName.toLowerCase().contains(device.getServiceTag().toLowerCase())) {
                            return component;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void updateDeviceState(final DeviceInventoryDAO deviceInventoryDAO, final DeviceState state,
                                          final DeviceInventoryEntity device) {
        try {
            final DeviceInventoryEntity newDevice = deviceInventoryDAO.getDeviceInventory(device.getRefId());
            if (newDevice == null) {
                logger.error("Cannot update state on device. Device not found for id " + device.getRefId());
                return;
            }

            if (state == null) {
                logger.error("Couldn't get device state so defaulting to discovered.  May not be original state.");
            }
            newDevice.setState((state == null) ? DeviceState.READY : state);
            deviceInventoryDAO.updateDeviceInventory(newDevice);
        } catch (AsmManagerCheckedException e) {
            logger.error("BAD error while updating firmware for device " + device.getRefId(), e);
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private void addLogMessage(String deploymentId, String logMessage) {
        if (deploymentId != null) {
            AsmDeployerDeploymentLogEntry addLogEntry = new AsmDeployerDeploymentLogEntry();
            addLogEntry.setComponentId("");
            addLogEntry.setLevel("info");
            addLogEntry.setLogMessage(logMessage);
            this.getAsmDeployerService().addLogMessage(deploymentId, addLogEntry);
            logger.debug("Deployment " + deploymentId + " " + logMessage);
        }
        else {
            logger.debug(logMessage);
        }
    }

    // Updates the servers software inventory, the compliance map, and the deployment's compliance
    private void updateDeploymentsFirmwareCompliance(String deploymentId) 
            throws AsmManagerCheckedException, 
                   InvocationTargetException, 
                   IllegalAccessException{
        DeploymentEntity deploymentEntity = this.getDeploymentDAO().getDeployment(deploymentId,
                DeploymentDAO.DEVICE_INVENTORY_ENTITIES + DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
        if (deploymentEntity != null) {
            this.getFirmwareUtil().updateServersSoftwareDeviceInventory(deploymentEntity);
            // Now that the inventory has changed, the compliance_map's must be updated for all repositories 
            // as the compliance_map table will no longer be accurate
            this.getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(new ArrayList<>(deploymentEntity.getDeployedDevices()));
            // Now that compliance_maps and device_inventory compliance are updated, we need to update the deployment's compliance
            this.getServiceDeploymentUtil().runServiceComplianceCheck(deploymentEntity, true);
        }
    }
    
    // Initialize the deploymentId (determine 'if' it is a deployment) for logging purposes
    private void initializeDeploymentForLogging(String refId) {
        try {
            deployment =  getDeploymentDAO().getDeployment(refId, DeploymentDAO.NONE);
            deploymentId = deployment.getId();
        } catch (Exception e) {
            logger.warn("Deployment Not Found in FirmwareUpdateJob, must be updating a Device instead for : " + refId, e);
            logger.warn("Attempting to find deployment for device with refId: " + refId);
            try {
                DeviceInventoryEntity die = this.getDeviceInventoryDAO().getDeviceInventory(refId);
                if (die.getDeployments() != null && !die.getDeployments().isEmpty()) {
                    deploymentId = die.getDeployments().get(0).getId();
                }
            } catch (Exception er) {
                logger.warn("DeviceNotFound when initializing the deploymentId for logging in FirmwareUpdateJob for refId: " + refId, er);
            }
        }
    }
    
    // Ensures the clusterServerList is full/available if necessary
    private void initializeServerMaitenanceList() {
    	// Identify the ServerList required for the Server Update
    	if (deployment != null && deployment.getMarshalledTemplateData() != null) { 
    		ServiceTemplate serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, deployment.getMarshalledTemplateData());
    		if(serviceTemplate != null && serviceTemplate.hasClusterComponentType()) {
    			for (ServiceTemplateComponent comp : serviceTemplate.getComponents()) {
    				if (ServiceTemplateComponentType.SERVER.equals(comp.getType())) {
    					String osIpAddress = this.getFirmwareUtil().getHypervisorIp(comp);
    					if (osIpAddress != null) {
    						this.clusterServerList.add(osIpAddress);
    					}
    				}
    			}
    			
    		}
        }

    }

    /**
     * Method to respond to a job getting cancelled.  In particular, this
     * sets device states that were PENDING back to READY.
     *
     * @param job
     */
    public void cancel(JobDetail job) {

        String updateId = job.getJobDataMap().getString(FirmwareUpdateJob.UPDATE_KEY);
        String updateType = job.getJobDataMap().getString(FirmwareUpdateJob.UPDATE_TYPE_KEY);
        try {
            if (updateId!=null && updateType!=null) {
                // Return device(s) to a ready state.
                List<DeviceInventoryEntity> devices = getJobDevices(updateId, updateType);
                for (DeviceInventoryEntity device : devices) {
                    getDeviceInventoryDAO().setDeviceState(device.getRefId(), DeviceState.READY);
                }
                logger.info("Cancelling FirmwareUpdateJob for " + updateType +  "[ " + updateId + " ]");
            }
        } catch (Exception e) {
            logger.error("Caught exception.  Unable to cleanup deleted FirmwareUpdateJob");
        }
    }

    public GenericDAO getGenericDAO() {
        if (genericDAO == null) {
            genericDAO = GenericDAO.getInstance();
        }
        return genericDAO;
    }

    public void setGenericDAO(GenericDAO genericDAO) {
        this.genericDAO = genericDAO;
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

    public DeviceInventoryComplianceDAO getDeviceInventoryComplianceDAO() {
        if (deviceInventoryComplianceDAO == null) {
            deviceInventoryComplianceDAO = DeviceInventoryComplianceDAO.getInstance();
        }
        return deviceInventoryComplianceDAO;
    }

    public void setDeviceInventoryComplianceDAO(DeviceInventoryComplianceDAO deviceInventoryComplianceDAO) {
        this.deviceInventoryComplianceDAO = deviceInventoryComplianceDAO;
    }

    public DeploymentDAO getDeploymentDAO() {
        if (deploymentDAO == null) {
            deploymentDAO = DeploymentDAO.getInstance();
        }
        return deploymentDAO;
    }

    public void setDeploymentDAO(DeploymentDAO deploymentDAO) {
        this.deploymentDAO = deploymentDAO;
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

    public ServiceDeploymentUtil getServiceDeploymentUtil() {
        if (serviceDeploymentUtil == null) {
            serviceDeploymentUtil = new ServiceDeploymentUtil();
        }
        return serviceDeploymentUtil;
    }

    public void setServiceDeploymentUtil(ServiceDeploymentUtil serviceDeploymentUtil) {
        this.serviceDeploymentUtil = serviceDeploymentUtil;
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
}
