/**************************************************************************
 *   Copyright (c) 2014 - 2017 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentNamesType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentNamesRefDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.listener.FirmwareUpdateJobListener;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.brownfield.BrownfieldUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareRepositoryFileUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil.DriverType;
import com.dell.asm.asmdeployer.client.AsmDeployerComponentStatus;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.JobCreateSpec;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.hibernate.HibernateException;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;

import static com.dell.asm.asmcore.asmmanager.db.DeploymentDAO.DEVICE_INVENTORY_ENTITIES;
import static com.dell.asm.asmcore.asmmanager.db.DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY;

public class ServiceDeploymentJob extends AsmDefaultJob {
    public static final String ServiceDeploymentJob_JOB_KEY_NAME = "ServiceDeployment.JobKey.name";
    public static final String ServiceDeploymentJob_JOB_KEY_GROUP = "ServiceDeployment.JobKey.group";
    public static final String ServiceDeploymentJob_SERVICE_KEY_DATA = "ServiceDeployment";
    public static final String ServiceDeploymentJob_IS_MIGRATE_DATA = "ServiceDeployment.isMigrate";
    public static final String ServiceDeploymentJob_IS_RESTART_DATA = "ServiceDeployment.isRestart";
    public static final String ServiceDeploymentJob_INDIVIDUAL_TEARDOWN = "ServiceDeployment.individualTeardown";
    public static final String ServiceDeploymentJob_IS_SCALE_UP = "ServiceDeployment.isScaleUp";

    private static final long MAX_DEPLOYMENT_POLL_MILLIS = (2 * 60 +  5 * 45) * 60 * 1000; // 5.75 hours + time to cover max retries
    private static final int MAX_DEPLOYMENT_STATUS_CHECK_FAILURES = 10;
    private static final int MAX_UPDATE_DAO_FAILURES = 10;

    // Poll Firmware Update Jobs every 30 seconds
    private static final long SLEEP_FIRMWARE_POLL = 30000;

    private static final Logger logger = Logger.getLogger(ServiceDeploymentJob.class);

    //DO NOT ACCESS MEMBERS DIRECTLY ALWAYS USE GETTERS
    private DeploymentDAO deploymentDAO;
    private FirmwareRepositoryDAO firmwareRepositoryDAO;
    private DeviceInventoryDAO deviceInventoryDAO;
    private DeploymentNamesRefDAO deploymentNamesRefDAO;
    private IAsmDeployerService asmDeployerService;
    private ServiceTemplateUtil serviceTemplateUtil ;
    private IPAddressPoolMgr ipAddressPoolMgr;
    private FirmwareUtil firmwareUtil;
    private ServiceDeploymentUtil serviceDeploymentUtil;

    // mutable instance attributes
    private boolean isMigrate;
    private boolean isDeployRestart;
    private boolean individualTeardown;
    private Deployment currentJobDeployment;
    private boolean isScaleUp;
    private List<ServiceTemplateComponent> scaledUpServerComponents = null;

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Initialize various member attributes from Job Context
     *
     * @param context JobExecutionContext passed in from Quartz
     */
    public void initializeServiceDeploymentJob(JobExecutionContext context) {
        logger.info("Initializing JobContext for ServiceDeploymentJob.");
        initializeFromJobContext(context);

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(ServiceDeploymentJob_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(ServiceDeploymentJob_JOB_KEY_NAME, jobKey.getName());

        String jsonData = context.getJobDetail().getJobDataMap().getString(ServiceDeploymentJob_SERVICE_KEY_DATA);
        setCurrentJobDeployment(DeploymentService.fromJson(jsonData));

        setScaleUp(context.getJobDetail().getJobDataMap().getBoolean(ServiceDeploymentJob_IS_SCALE_UP));
        setMigrate(context.getJobDetail().getJobDataMap().getBoolean(ServiceDeploymentJob_IS_MIGRATE_DATA));
        setDeployRestart(context.getJobDetail().getJobDataMap().getBoolean(ServiceDeploymentJob_IS_RESTART_DATA));
        // If we are tearing down individual components of a service
        setIndividualTeardown(context.getJobDetail().getJobDataMap().getBoolean(ServiceDeploymentJob_INDIVIDUAL_TEARDOWN));
    }

    /**
     * Allow firmware repository time to finish copying in case it hasn't finished yet
     *
     * @param firmwareRepository FirmwareRepositoryEntity to wait for download status
     */
    protected void waitForRepositoryQuiescence(FirmwareRepositoryEntity firmwareRepository) {
        try {
            RepositoryStatus downloadStatus = firmwareRepository.getDownloadStatus();
            switch (downloadStatus) {
                case PENDING:
                case COPYING:
                    // Firmware sync job is in progress (or will be soon); wait for completion
                    logger.info("Waiting for " + firmwareRepository.getName()
                            + " to become available, current state is " + downloadStatus);
                    new FirmwareRepositoryFileUtil().blockUntilAvailable(firmwareRepository.getId(),
                            FirmwareUpdateJob.MAX_REPO_DOWNLOAD_WAIT_MILLIS);
                    break;
                case ERROR:
                    // Previous firmware sync job failed; kick it off again, maybe it will succeed
                    logger.error("Attempting to re-sync " + firmwareRepository.getName()
                            + " binaries, current status is error");
                    new FirmwareRepositoryFileUtil().syncFirmwareRepository(firmwareRepository.getId());
                    break;
            }
        } catch (Exception e) {
            // Continue on with the deployment even though we may be missing required firmware
            // binaries. Otherwise we will not create the asm-deployer deployment and the
            // service will get stuck "In Progress".
            logger.warn("Failed to download binaries for " + firmwareRepository.getName(), e);
        }
    }

    /**
     * Execute razor deployment.  Manages the final Deployment status.
     *
     * @param deployment the deployment that will be actively deployed.
     * @param updatedDevices devices targeted for firmware update in the scope of this deployment
     */
    protected DeploymentEntity executeRazorDeployment(Deployment deployment,
                                          List<DeviceInventoryEntity> updatedDevices) {

        DeploymentEntity deploymentEntity = null;

        logger.info("Executing Razor Deployment for Deployment " + deployment.getDeploymentName() + " in ServiceDeploymentJob.");

        AsmDeployerStatus status=null;
        String deploymentId = deployment.getId();
        try {

            if (isDeployRestart()) { // TomCat was Restarted, so only need to query the ASMDeployer status
                status = getAsmDeployerService().getDeploymentStatus(deploymentId);
                // this status should NEVER be null as a restart should ONLY be kicked off if status exists in asmDeployer
                if (status == null) {  // If the status is null we need to kick out of this Job and log the error
                    logger.error("after a TomcatRestart, in ServiceDeploymentJob, asmDeployer did not return a status for deployment with id of " + deploymentId);
                    throw new IllegalStateException("after a TomcatRestart, in ServiceDeploymentJob, asmDeployer did not return a status for deployment with id of " + deploymentId);
                }
            }
            else if (deployment.isTeardown() || deployment.isRetry()) {
                status = getAsmDeployerService().retryDeployment(deploymentId, deployment);
            } else if (isMigrate()) {
                status = getAsmDeployerService().migrateDeployment(deployment);
            } else {
                status = getAsmDeployerService().createDeployment(deployment);
            }

            deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DeploymentDAO.NONE);
            deploymentEntity.setStatus(status.getStatus());
            // Only initialize this once, the first time the deployment gets here.
            if (deploymentEntity.getDeploymentStartedDate() == null) {
                deploymentEntity.setDeploymentStartedDate(new GregorianCalendar());
            }
            getDeploymentDAO().updateDeployment(deploymentEntity);

            long start = new Date().getTime();
            long elapsed = 0L;
            int failures = 0;
            while (DeploymentStatusType.IN_PROGRESS.equals(status.getStatus())
                    && elapsed < MAX_DEPLOYMENT_POLL_MILLIS && failures < MAX_DEPLOYMENT_STATUS_CHECK_FAILURES) {
                // Don't update device status on teardown: ASM-7063
                if (!deployment.isTeardown()) {
                    updateDeviceStatuses(status, false);
                }
                Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                AsmDeployerStatus newStatus;
                try {
                    newStatus = getAsmDeployerService().getDeploymentStatus(deploymentId);
                }catch(WebApplicationException wex) {
                    failures++;
                    logger.error("asm_deployer get status failed, failures counter=" + failures, wex);
                    continue;
                }

                // deployment record can be changed outside of this thread i.e. by updateDeployment
                deploymentEntity = getDeploymentDAO().getDeployment(deploymentId,DeploymentDAO.NONE);
                if (deploymentEntity.getStatus() != newStatus.getStatus()) {
                    deploymentEntity.setStatus(newStatus.getStatus());
                    getDeploymentDAO().updateDeployment(deploymentEntity);
                }
                status = newStatus;
                elapsed = new Date().getTime() - start;
            }

            // if we quit the job by timeout - change status from in-progress to failed
            boolean endOfJob = !deployment.isTeardown();
            if (DeploymentStatusType.IN_PROGRESS.equals(status.getStatus())) {
                logger.error("Deployment id=" + deploymentId + ", job=" + getJobName() + " timed out. Setting status to error.");
                status.setStatus(DeploymentStatusType.ERROR);
                deploymentEntity.setStatus(status.getStatus());
                getDeploymentDAO().updateDeployment(deploymentEntity);
                endOfJob = true;
                // overwrite asm-deployer status for all components -
                // because deployment status might still be in-progress or pending
                // but we quite deployment job and must set terminal status for all devices - for future call updateDeviceStatuses()
                List<AsmDeployerComponentStatus> asmDeployerComponents = status.getComponents();
                if (asmDeployerComponents != null) {
                    for (AsmDeployerComponentStatus cs : asmDeployerComponents) {
                        cs.setStatus(DeploymentStatusType.ERROR);
                    }
                }
            }

            updateDeviceStatuses(status, endOfJob);

            if (!DeploymentStatusType.COMPLETE.equals(status.getStatus())) {
                if (deployment.isTeardown()) {
                    logger.warn("Deployment " + deploymentId + " final teardown status was " + status.getStatus());
                } else {
                    setDeploymentFailed(DeploymentStatusType.ERROR);
                    return deploymentEntity;
                }
            }

            updateOsDeviceInventory(status, deployment);

            if (deployment.isTeardown()) {
                deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
                // must use unmodified template as this call will save it to DAO. We don't want any settings be stripped out and saved this way.
                ServiceTemplate templateFromDB = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentEntity.getMarshalledTemplateData());
                removeTeardownComponents(deploymentEntity, templateFromDB);
                if (!individualTeardown) {
                    ProxyUtil.getDeploymentProxy().deleteDeployment(deploymentId);
                }
            }

            logger.debug("Deployment " + deploymentId + " final status before job exit was " + deploymentEntity.getStatus());
            deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
            getFirmwareUtil().updateComponentStatus(deployment, false);

        } catch (Exception e) {
            logger.error("ServiceDeploymentJob.execute error for deployment " + deployment.getId(), e);

            setJobStatus(JobStatus.FAILED);
            EEMILocalizableMessage msg = AsmManagerMessages.deploymentFailed(deployment.getServiceTemplate().getTemplateName(), deployment.getDeploymentName());
            LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.DEPLOYMENT);

            try {
                // Status changed
                deploymentEntity = getDeploymentDAO().getDeployment(deployment.getId(), DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
                // check if the service still exists
                if (deploymentEntity!=null) {
                    deploymentEntity.setStatus(DeploymentStatusType.ERROR);
                    cleanupTeardownComponentsOnFailure(deploymentEntity,deployment);
                    getDeploymentDAO().updateDeployment(deploymentEntity);
                }

                getFirmwareUtil().updateComponentStatus(deployment, false);
			} catch (AsmManagerCheckedException | IllegalAccessException | InvocationTargetException e1) {
				logger.error("Exception updating status of " + deployment.getId(), e1);
            }
            return deploymentEntity;
        } finally {
            logger.info("Finally cleaning up execute razor deployment.");
            // This means asm-deployer threw an exception, for example it doesn't know about the deployment.
            if (status == null) {
                // To avoid zombie services that can't be removed.
                if (deployment.isTeardown()) {
                    deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
                    // must use unmodified template as this call will save it to DAO. We don't want any settings be stripped out and saved this way.
                    ServiceTemplate templateFromDB = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentEntity.getMarshalledTemplateData());
                    try {
                        logger.warn("Razor returned null status on teardown request.  Freeing resources from deployment.");
                        removeTeardownComponents(deploymentEntity, templateFromDB);
                        if (!individualTeardown) {
                            ProxyUtil.getDeploymentProxy().deleteDeployment(deploymentId);
                        }
                    } catch (Exception e) {
                        logger.warn("Caught exception while freeing resources from deployment.");
                    }
                }
            }
        }

        // all done
        setJobStatus(JobStatus.SUCCESSFUL);
        logger.info("ServiceDeploymentJob for the job name:" + getJobName());
        EEMILocalizableMessage msg = AsmManagerMessages.deploymentCompleted(deployment.getServiceTemplate().getTemplateName(), deployment.getDeploymentName());
        LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);
        return deploymentEntity;
    }

    protected void setDeploymentFailed(DeploymentStatusType status) {
        String deploymentId = null;
        DeploymentEntity deploymentEntity;

        EEMILocalizableMessage msg = AsmManagerMessages.deploymentFailed(getCurrentJobDeployment().getServiceTemplate().getTemplateName(), getCurrentJobDeployment().getDeploymentName());
        LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.DEPLOYMENT);

        try {
            deploymentId = getCurrentJobDeployment().getId();
            deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
            if (deploymentEntity != null) {
                deploymentEntity.setStatus(status);
                getDeploymentDAO().updateDeployment(deploymentEntity);
            }
            getFirmwareUtil().updateComponentStatus(getCurrentJobDeployment(), false);
            setJobStatus(JobStatus.FAILED);
        } catch (AsmManagerCheckedException | IllegalAccessException | InvocationTargetException e1) {
            if (deploymentId == null) deploymentId = "";
            logger.error("Exception updating status of " + deploymentId, e1);
        }
    }

    protected void updateOsDeviceInventory(AsmDeployerStatus status, Deployment deployment) {
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                ServiceTemplateCategory osResource = null;
                ServiceTemplateCategory networkingResource = null;

                for (ServiceTemplateCategory resource : component.getResources()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE.equals(resource.getId())) {
                        osResource = resource;
                    } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(resource.getId())) {
                        networkingResource = resource;
                    }
                }

                String osImageType = null;
                String osRepo = null;
                String osAdminPassword = null;
                String osHostName = null;
                if (osResource != null) {
                    ServiceTemplateSetting osRepoSetting = ServiceTemplateUtil.findParameter(osResource,
                                                                                             ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                    if (osRepoSetting != null) {
                        osRepo = osRepoSetting.getValue();
                    }
                    if (osRepo != null) {
                        osImageType = getServiceTemplateUtil().findTask(osRepo);
                    }
                    ServiceTemplateSetting osAdminPasswordSetting = ServiceTemplateUtil.findParameter(osResource,
                                                                                                      ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
                    if (osAdminPasswordSetting != null) {
                        osAdminPassword = osAdminPasswordSetting.getValue();
                    }
                    ServiceTemplateSetting osHostNameSetting = ServiceTemplateUtil.findParameter(osResource,
                                                                                                 ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                    if (osHostNameSetting != null) {
                        osHostName = osHostNameSetting.getValue();
                    }
                }
                String osIpAddress = null;

                boolean setOsAttributes = (osResource!=null) && !component.isTeardown() && DeploymentStatusType.COMPLETE.equals(status.getStatus());

                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE.equals(osImageType)) {
                    // ESXi requires a static management address contained in the network configuration
                    if (networkingResource != null) {
                        for (ServiceTemplateSetting param : networkingResource.getParameters()) {
                            String ip = getHypervisorManagementIp(param);
                            if (ip != null) {
                                osIpAddress = ip;
                                break;
                            }
                        }
                    }
                } else if (setOsAttributes) {
                    // All other OS types have a puppet agent installed and puppetdb should have their ip
                    try {
                        Map<String, String> facts = PuppetModuleUtil.getPuppetAgentFacts(osHostName);
                        osIpAddress = facts.get("ipaddress");
                    } catch (IOException | AsmManagerRuntimeException e) {
                        logger.error("Failed to retrieve agent facts for " + osHostName);
                    }
                }

                if (osIpAddress == null && setOsAttributes) {
                    logger.error("Unable to find IP address for deployment " + deployment.getId() +
                            " server " + osHostName + " os " + osImageType);
                }

                String asmGUID = component.getAsmGUID();
                DeviceInventoryEntity device = getDeviceInventoryDAO().getDeviceInventory(asmGUID);
                boolean dirty = false;
                if (device != null) {
                    if (setOsAttributes) {
                        // Only set these attributes if the deployment was a success;
                        // otherwise they may not be valid.
                        device.setOsIpAddress(osIpAddress);
                        device.setOsAdminPassword(osAdminPassword);
                        device.setOsImageType(osImageType);
                        device.setState(DeviceState.DEPLOYED);
                        dirty = true;
                    }

                    // in success reset failures counter
                    if (DeploymentStatusType.COMPLETE.equals(status.getStatus())) {
                        device.setFailuresCount(0);
                        dirty = true;
                    }

                    if (dirty) {
                        try {

                            getDeviceInventoryDAO().updateDeviceInventory(device);
                        } catch (AsmManagerCheckedException e) {
                            logger.error("Failed to update " + device, e);
                        }
                    }
                }
            }
        }
    }

    private String getHypervisorManagementIp(ServiceTemplateSetting param) {
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals(param.getId())) {
            List<Network> networks = param.getNetworks();
            if (networks != null && !networks.isEmpty()) {
                Network osManagementNetwork = networks.get(0);
                StaticNetworkConfiguration staticInfo = osManagementNetwork.getStaticNetworkConfiguration();
                if (staticInfo != null && staticInfo.getIpAddress() != null) {
                    return staticInfo.getIpAddress();
                }
            }
        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(param.getId())) {
            NetworkConfiguration networkConfig = param.getNetworkConfiguration();
            if (networkConfig != null) {
                List<Interface> interfaces = networkConfig.getUsedInterfaces();
                if (interfaces != null && interfaces.size() > 0) {
                    return getFirmwareUtil().fetchIpFromInterfaces(interfaces);
                }
            }
        }
        return null;
    }

    /**
     *  Reflect the device states as reported by asm-deployer for each DeviceInventory SERVER in the deployment.
     *  By default this is 'DEPLOYING' unless asm-deployer reports one of [PENDING,COMPLETE,CANCELLED,ERROR]
     *
     *  If end of job, reset device failure count to 0, but only if state in [DEPLOYED,DEPLYMENT_ERROR]
     *
     * @param status AsmDeployerStatus to be used
     * @param endOfJob is end of job processing
     */
    protected void updateDeviceStatuses(AsmDeployerStatus status, boolean endOfJob) {
        // find corresponding device
        String deploymentId = status.getId();
        DeploymentEntity deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DEVICE_INVENTORY_ENTITIES);
        List<AsmDeployerComponentStatus> asmDeployerComponents = status.getComponents();
        if (asmDeployerComponents != null) {
            for (AsmDeployerComponentStatus cs : asmDeployerComponents) {
                if (deploymentEntity.getDeployedDevices() != null) {
                    for (DeviceInventoryEntity currentDevice : deploymentEntity.getDeployedDevices()) {
                        if (currentDevice.getRefId().equals(cs.getAsmGuid())) {
                            if (DeviceType.isServer(currentDevice.getDeviceType())) {
                                DeviceState asmDeviceState = DeviceState.DEPLOYING;
                                switch (cs.getStatus()) {
                                case PENDING:
                                    asmDeviceState = DeviceState.PENDING;
                                    break;
                                case COMPLETE:
                                    asmDeviceState = DeviceState.DEPLOYED;
                                    break;
                                case CANCELLED:
                                case ERROR:
                                    asmDeviceState = DeviceState.DEPLOYMENT_ERROR;
                                    break;
                                }
                                if (!currentDevice.getState().equals(asmDeviceState)) {
                                    //call to get device inventory will load all the fw inventory data.
                                    DeviceInventoryEntity currDevice = getDeviceInventoryDAO().getDeviceInventory(currentDevice.getRefId());
                                    this.updateDeviceSafelyWithRetries(currDevice, asmDeviceState);
                                }
                                if (endOfJob &&
                                        (DeviceState.DEPLOYED.equals(asmDeviceState) ||
                                                DeviceState.DEPLOYMENT_ERROR.equals(asmDeviceState))) {
                                    DeviceInventoryEntity currDevice = null;
                                    try {
                                        currDevice = getDeviceInventoryDAO().getDeviceInventory(currentDevice.getRefId());
                                        currDevice.setFailuresCount(0);
                                        getDeviceInventoryDAO().updateDeviceInventory(currDevice);
                                    } catch (AsmManagerCheckedException e) {
                                        logger.warn("failed to update failures count on device " + currDevice.getServiceTag());
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes a list of {@code components} from the specified {@code serviceTemplate} and
     * {@code deployment}. This includes:
     *
     * <ol>
     *     <li>Removes the components from the specified template</li>
     *     <li>Removes the components from the the deployment's deployed devices map</li>
     *     <li>Sets any device corresponding to the component's status back to DISCOVERED (available).</li>
     *     <li>Releases any static network IP reservations</li>
     *     <li>Releases any virtual identity reservations</li>
     *     <li>Deletes any encryption ids associated with the component</li>
     * </ol>
     *
     * @param deploymentEntity The deployment database entity object
     * @param serviceTemplate The service template. In theory this should be the same as that
     *                        contained in the passed deployment, but there may be cases where
     *                        they differ.
     */
    public void removeTeardownComponents(DeploymentEntity deploymentEntity,
                                         ServiceTemplate serviceTemplate) throws InterruptedException {
        List<ServiceTemplateComponent> teardownComponents = new ArrayList<>();
        Set<String> teardownComponentsIds = new HashSet<>();
        Set<String> nonTeardownComponentGUIDs = new HashSet<>();
        Iterator<ServiceTemplateComponent> iterator = serviceTemplate.getComponents().iterator();

        // remove from template
        while (iterator.hasNext()) {
            ServiceTemplateComponent component = iterator.next();
            if (component.isTeardown()) {
                // Remove from template
                teardownComponents.add(component);
                teardownComponentsIds.add(component.getId());
                iterator.remove();
            } else {
                nonTeardownComponentGUIDs.add(component.getAsmGUID());
            }
        }

        // remove from relatedComponents
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            component.removeAllAssociatedComponents(teardownComponentsIds);
        }

        // remove from getDeployedDevices
        List<String> removedAsmGuids = new ArrayList<>();
        Set<String> removedEncryptionIds = new HashSet<>();

        for (ServiceTemplateComponent component : teardownComponents) {
            String asmGuid = component.getAsmGUID();
            if (!StringUtils.isEmpty(asmGuid) && !nonTeardownComponentGUIDs.contains(asmGuid)) {
                removedAsmGuids.add(asmGuid);
            }
            removedEncryptionIds.addAll(getServiceTemplateUtil().getEncryptionIds(component));

            if (ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                    ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                releaseIdentities(component);
                releaseDeploymentNamesRefs(component);
                if (ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                    continue;
                }

                for (int i=0; i< MAX_UPDATE_DAO_FAILURES; i++) {
                    try {
                        DeviceInventoryEntity device = getDeviceInventoryDAO().getDeviceInventory(asmGuid);
                        device.setOsIpAddress(null);
                        device.setOsAdminPassword(null);
                        device.setOsImageType(null);
                        device.setState(DeviceState.READY);

                        getDeviceInventoryDAO().updateDeviceInventory(device);
                        logger.info("Marked server " + device.getServiceTag() + " as available");
                        break;
                    } catch (AsmManagerCheckedException | AsmManagerInternalErrorException | HibernateException e) {
                        // catch all possible exceptions because it is critical to release as many devices as possible
                        logger.error("Failed to mark " + asmGuid + " as available, attempt #" + i, e);
                    }
                    Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                }
            }
        }

        // remove from deployed devices map and set state back to READY state, formerly known as DISCOVERED (available).
        Iterator<DeviceInventoryEntity> iterator2 = deploymentEntity.getDeployedDevices().iterator();
        while (iterator2.hasNext()) {
            DeviceInventoryEntity device = iterator2.next();
            if (removedAsmGuids.contains(device.getRefId())) {
                getDeviceInventoryDAO().setDeviceState(device, DeviceState.READY, true);
                iterator2.remove();
            }
        }

        // Remove encryption ids
        getServiceTemplateUtil().deleteEncryptionIds(removedEncryptionIds);

        deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(serviceTemplate));
        try {
            // to prevent stale object update error, read current record and re-create object
            // we update only template attribute and deployed devices
            // this is dangerous as we can lose changes made by other players, but since it is a tear-down process
            // we assume there could be no other changes to template and deployed devices except by this thread only.

            DeploymentEntity currentEntity = getDeploymentDAO().getDeployment(deploymentEntity.getId(), DEVICE_INVENTORY_ENTITIES);
            if (currentEntity != null) {

                currentEntity.getDeployedDevices().clear();
                currentEntity.getDeployedDevices().addAll(deploymentEntity.getDeployedDevices());
                currentEntity.setMarshalledTemplateData(deploymentEntity.getMarshalledTemplateData());

            }else{
                logger.error("No deployment found: " + deploymentEntity.getId());
                // further update will probably generate SQL error but it is beyond the scope of this method anyways
                currentEntity = deploymentEntity;
            }

            getDeploymentDAO().updateDeployment(currentEntity);
        } catch (AsmManagerCheckedException | InvocationTargetException
                | IllegalAccessException e) {
            logger.error("Failed to update deployment entity " + deploymentEntity, e);
        }
    }

    private void releaseIdentities(ServiceTemplateComponent component) {
        // Gather map of network ids and their assigned static ips
        Map<String, Set<String>> networkIdsToIpsMap = new HashMap<>();
        final List<Network> networks = ServiceTemplateClientUtil.findStaticNetworks(component);
        if (networks != null && networks.size() > 0) {
            for (Network network : networks) {
                if (network.isStatic()) {
                    StaticNetworkConfiguration staticConfig = network.getStaticNetworkConfiguration();
                    if (staticConfig != null && staticConfig.getIpAddress() != null) {
                        Set<String> ipAddressSet = networkIdsToIpsMap.get(network.getId());
                        if (ipAddressSet == null) {
                            ipAddressSet = new HashSet<>();
                            networkIdsToIpsMap.put(network.getId(), ipAddressSet);
                        }
                        ipAddressSet.add(staticConfig.getIpAddress());
                    }
                }
            }
        }

        for (Map.Entry<String, Set<String>> entry : networkIdsToIpsMap.entrySet()) {
            String networkId = entry.getKey();
            Set<String> ipAddresses = entry.getValue();
            try {
                getIpAddressPoolMgr().releaseIPAddresses(ipAddresses, networkId);
            } catch (Exception e) {
                logger.error("Unable to release IPs " + ipAddresses + " from network " + networkId, e);
            }
        }

        // TODO: virtual identities (mac address, wwn, etc.) can only be released when the service
        // is deleted. At the moment those identities are only reserved for boot from SAN scenarios
        // which are not very common and don't make much sense to scale up and scale down.
    }

    private void releaseDeploymentNamesRefs(final ServiceTemplateComponent component) {
        switch (component.getType()) {
        case VIRTUALMACHINE:
            ServiceTemplateSetting vmNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                                                                          ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
            if (vmNameSetting != null) {
                if (StringUtils.isNotBlank(vmNameSetting.getValue())) {
                    try {
                        getDeploymentNamesRefDAO().deleteDeploymentNamesRef(vmNameSetting.getValue(), DeploymentNamesType.VM_NAME);
                    } catch (Exception e) {
                        logger.error("Unable to release deployment name ref for vm name:" + vmNameSetting.getValue());
                    }
                }
                // found the setting so we should break;
                break;
            }
            //Setting was not found so we should fall through.
        case SERVER:
            ServiceTemplateSetting osHostNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                                                                              ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
            if (osHostNameSetting != null && StringUtils.isNotBlank(osHostNameSetting.getValue()) && !BrownfieldUtil.NOT_FOUND.equals(osHostNameSetting.getValue())) {
                try {
                    getDeploymentNamesRefDAO().deleteDeploymentNamesRef(osHostNameSetting.getValue(), DeploymentNamesType.OS_HOST_NAME);
                } catch (Exception e) {
                    logger.error("Unable to release deployment name ref for os host name:" + osHostNameSetting.getValue());
                }
            }
        default:
            break;
        }

        getDeploymentNamesRefDAO().deleteOrphanNamesRef();
    }

    // due to transaction collissions we retry updating the status of a device multiple times before failing
    // This method has been added to specifically address the issue found in Jira ASM-4575
    protected DeviceInventoryEntity updateDeviceSafelyWithRetries(DeviceInventoryEntity deviceInventoryEntity, DeviceState deviceState){
        DeviceInventoryEntity updatedDeviceEntity = deviceInventoryEntity;
        int maxRetries = 5;
        int retries = 0;
        boolean success = false;
        while(!success && retries < maxRetries){
            try{
                this.getDeviceInventoryDAO().setDeviceState(updatedDeviceEntity, deviceState, true);
                updatedDeviceEntity = getDeviceInventoryDAO().getDeviceInventory(updatedDeviceEntity.getRefId());
                logger.debug("Updated device state for " + updatedDeviceEntity.getServiceTag() + " to " + deviceState);
                success = true;
            } catch (Exception e) {
                logger.warn("failed to update device state for " + updatedDeviceEntity.getServiceTag());
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException ie){
                    logger.warn("Error sleeping while waiting to reload a Device in updateDeviceSafelyWithRetries method!");
                }
                // if we are still failing, then reload the device Inventory and try to update again
                if(retries < maxRetries){
                    updatedDeviceEntity = getDeviceInventoryDAO().getDeviceInventory(updatedDeviceEntity.getRefId());
                    retries++;
                } // If we are still failure and have retried 5 times then go ahead and throw the exception
                else throw e;
            }
        }

        return updatedDeviceEntity;
    }

    protected void cleanupTeardownComponentsOnFailure(DeploymentEntity deploymentEntity, Deployment deployment) {
        if (deployment != null && getCurrentJobDeployment().isTeardown()) {
            if (deploymentEntity != null) {
                boolean updated = false;
                ServiceTemplate teardownServiceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,deploymentEntity.getMarshalledTemplateData());
                if (teardownServiceTemplate != null) {
                    for (ServiceTemplateComponent component : teardownServiceTemplate.getComponents()) {
                        if (component.isTeardown()) {
                            component.setTeardown(false);
                            updated = true;
                        }
                    }
                }
                if (updated) {
                    deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(teardownServiceTemplate));
                }
            }
        }
    }

    /**
     * Create one FirmwareUpdateJob per device and wait for all of them to complete
     * @param updatedDevices List of DeviceInventoryEntities to update
     * @param componentsToUpdate Components that need to be updated
     * @param firmwareRepository FirmwareRepositoryEntity to use to update
     * @param updateType Update Type Key
     * @throws SchedulerException
     */
    private void createAndRunFirmwareUpdateJobs(List<DeviceInventoryEntity> updatedDevices,
                                                String componentsToUpdate,
                                                FirmwareRepositoryEntity firmwareRepository,
                                                String updateType)
            throws SchedulerException {
        JobDetail job;
        String groupName = getUniqueJobGroupName("FirmwareUpdate");

        for (DeviceInventoryEntity device : updatedDevices) {
            JobCreateSpec jobspec = new JobCreateSpec(FirmwareUpdateJob.class);
            jobspec.setDescription(groupName);
            jobspec.setSelector(groupName);
            // Setting true will allow us to search jobs by groupName
            job = getJobManager().createNamedJob(jobspec, true);
            job.getJobDataMap().put(FirmwareUpdateJob.COMPONENTS_TO_UPDATE, componentsToUpdate);
            job.getJobDataMap().put(FirmwareUpdateJob.UPDATE_KEY, device.getRefId());
            job.getJobDataMap().put(FirmwareUpdateJob.EXIT_MAINTANACE, false);
            job.getJobDataMap().put(FirmwareUpdateJob.NEXT_REBOOT, false);
            job.getJobDataMap().put(FirmwareUpdateJob.UPDATE_TYPE_KEY, updateType);
            if (firmwareRepository != null) {
                job.getJobDataMap().put(FirmwareUpdateJob.FIRMWARE_REPOSITORY, firmwareRepository.getId());
            }
            getJobManager().createAndStartTrigger(job);
        }

        // Wait for FirmwareUpdate jobs to complete
        waitForFirmwareUpdate(groupName);
    }

    /**
     * Create and run a NoOp Job to set up the deployment in asm-deployer
     */
    private void createAndRunFirmwareNoOpJob() {
        boolean newService = false;
        try {
            getAsmDeployerService().getDeploymentStatus(getCurrentJobDeployment().getId());
        } catch (WebApplicationException e) {
            // service does not exist or asm-deploter is down
            if (e.getResponse().getStatus() == HttpStatus.NOT_FOUND_404) {
                newService = true;
            } else {
                throw e;
            }
        }

        if (newService) {
            getCurrentJobDeployment().setNoOp(true);
            getCurrentJobDeployment().setFirmwareInit(true);
            getAsmDeployerService().createDeployment(getCurrentJobDeployment());
        }
        getCurrentJobDeployment().setNoOp(false);
        getCurrentJobDeployment().setFirmwareInit(false);
    }

    private void waitForFirmwareUpdate(String groupName) {
        long start = new Date().getTime();
        long elapsed = 0L;
        try {
            Thread.sleep(SLEEP_FIRMWARE_POLL);

            List<JobExecutionContext> jobs = getJobManager().getCurrentlyExecutingJobs();
            logger.info(String.format("Polling for firmware completion amongst %d Jobs", jobs.size()));

            Set<JobKey> runningSet = getJobManager().getScheduler().getJobKeys(GroupMatcher.jobGroupContains(groupName));

            while (runningSet != null && runningSet.size() > 0 && elapsed < FirmwareUpdateJob.MAX_FIRMWARE_APPLY_MILLIS) {
                String msg = String.format("%d firmware jobs remaining", runningSet.size());
                logger.info(msg);
                Thread.sleep(SLEEP_FIRMWARE_POLL);
                runningSet = getJobManager().getScheduler().getJobKeys(GroupMatcher.jobGroupContains(groupName));
                elapsed = new Date().getTime() - start;
            }
        } catch (Exception e) {
            logger.warn("Error while waiting for firmware to update but ignoring it", e);
        }
    }

    private synchronized String getUniqueJobGroupName(String prefix) {
        return prefix + ":" + getCurrentJobDeployment().getId();
    }

    protected List<DeviceInventoryEntity> getServersInDeployment() {
        List<DeviceInventoryEntity> servers = new ArrayList<>();
        String deploymentId = getCurrentJobDeployment().getId();
        DeploymentEntity deploymentEntity = getDeploymentDAO().getDeployment(deploymentId, DEVICE_INVENTORY_ENTITIES);
        if (deploymentEntity != null) {
            Set<DeviceInventoryEntity> devices = deploymentEntity.getDeployedDevices();
            if (devices != null && devices.size() > 0) {
                for (DeviceInventoryEntity device : devices) {
                    if (DeviceType.isServer(device.getDeviceType())) {
                        servers.add(device);
                    }
                }
            }
        }
        return servers;
    }

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.info("Executing ServiceDeploymentJob's executeServiceDeployment method!");
        List<DeviceInventoryEntity> uncomplyingDevices = null;
        FirmwareRepositoryEntity firmwareRepository = null;
        try {
            // Initialize member data from Job context
            initializeServiceDeploymentJob(context);

            setJobStatus(JobStatus.IN_PROGRESS);

            // need to clean up template for deployment
            ServiceTemplate serviceTemplate = getCurrentJobDeployment().getServiceTemplate();
            if (serviceTemplate == null) {
                throw new AsmManagerRuntimeException("No service template for deployment " + getCurrentJobDeployment().getId());
            }

            ServiceDeploymentUtil.prepareTemplateForDeployment(serviceTemplate);

            DeploymentEntity deploymentEntity = getDeploymentDAO().getDeployment(getCurrentJobDeployment().getId(),
                    DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);

            // Must capture the DeploymentInventoryEntity here, before modifications in order to update their compliance
            // properly at the end of the run (in case of a teardown of the deployment).  Once teardown is complete
            // and the Deployment is removed, there will be no way to determine the devices that were part of it.
            List<DeviceInventoryEntity> devInvEntityList = new ArrayList<>(deploymentEntity.getDeployedDevices());

            // Sets initial state of the compliance so the Deployment's compliance is represented properly during job run
            if (deploymentEntity.getFirmwareRepositoryEntity() != null) {
                // Assumes compliance_map table is in a proper state
                getServiceDeploymentUtil().runServiceComplianceCheck(deploymentEntity, true);
            }

            try { // Brownfield Deployments should not initiate Firmware Upgrades
                if (!getCurrentJobDeployment().isTeardown() && !getCurrentJobDeployment().isBrownfield() &&
                        deploymentEntity.getFirmwareRepositoryEntity() != null) {
                    firmwareRepository = getFirmwareRepositoryDAO().get(deploymentEntity.getFirmwareRepositoryEntity().getId());
                    // Get devices we should update firmware on
                    uncomplyingDevices = this.getFirmwareUpdateDevicesInDeployment(firmwareRepository, getCurrentJobDeployment(), DriverType.FIRMWARE);
                }
            } catch (AsmManagerCheckedException e) {
                logger.error("Exception seen while generating firmware for template", e);
            }

            // Give repository time to settle into an available state
            if (firmwareRepository != null) {
                waitForRepositoryQuiescence(firmwareRepository);
            }

            // Create Firmware Jobs and wait for them to finish
            if (uncomplyingDevices != null && uncomplyingDevices.size() > 0 && firmwareRepository != null) {
                createAndRunFirmwareNoOpJob();
                createAndRunFirmwareUpdateJobs(uncomplyingDevices,
                        FirmwareUpdateJob.COMPONENTS_FIRMWARE,
                        firmwareRepository,
                        FirmwareUpdateJob.UPDATE_TYPE_DEPLOYMENT_DEVICE);

                // Recompute the list of devices that need updated.
                // This list should now contain 0 devices if all firmware updates were effective.
                uncomplyingDevices = this.getFirmwareUpdateDevicesInDeployment(firmwareRepository, getCurrentJobDeployment(), DriverType.FIRMWARE);
                getCurrentJobDeployment().setRetry(true);
            }

            if (uncomplyingDevices != null && !uncomplyingDevices.isEmpty()) {
                logger.warn(uncomplyingDevices.size() + " uncomplying devices were found after running Firmware Updates!");
                logger.warn("After Firmware updates device with refid " + uncomplyingDevices.get(0).getRefId() + " is still non-compliant!");

                // Update the Server's Software inventory compliance as it's only updated on an InventoryJob run otherwise
                deploymentEntity = this.getDeploymentDAO().getDeployment(getCurrentJobDeployment().getId(), DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
                getFirmwareUtil().updateServersSoftwareDeviceInventory(deploymentEntity);
                this.getServiceDeploymentUtil().runServiceComplianceCheck(deploymentEntity, true);

                // Check that devices states reflect firmware update failed
                for (DeviceInventoryEntity currDevice : uncomplyingDevices) {
                    if (!DeviceState.UPDATE_FAILED.equals(currDevice.getState())) {
                        updateDeviceSafelyWithRetries(currDevice, DeviceState.UPDATE_FAILED);
                        logger.warn("Expected device state to be UPDATE_FAILED");
                    }
                }
            }

            // Attempt to do the deployment regardless of firmware updates success or failures
            deploymentEntity = executeRazorDeployment(getCurrentJobDeployment(), uncomplyingDevices);

            // Must save the last known state so we can set it once Software updates are complete
            DeploymentStatusType lastDeploymentStatus = DeploymentStatusType.ERROR;
            if (deploymentEntity != null) {
                lastDeploymentStatus = deploymentEntity.getStatus();  // Will be Complete or Error
            }

            // Update the Deployment Status to complete again now that software updates are finished
            deploymentEntity = this.getDeploymentDAO().getDeployment(getCurrentJobDeployment().getId(),
                    DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);

            if (deploymentEntity != null) {
                if (getCurrentJobDeployment().isTeardown() || getCurrentJobDeployment().isIndividualTeardown()) { // It's a teardown of an individual component
                    List<DeviceInventoryEntity> dies = this.getRemovedDeploymentEntity(devInvEntityList, new ArrayList<>(deploymentEntity.getDeployedDevices()));
                    if (dies != null && !dies.isEmpty()) {
                        ArrayList<DeviceInventoryEntity> diesRemoved = new ArrayList<>();
                        for (DeviceInventoryEntity die : dies) {
                            this.getDeviceInventoryDAO().deleteFirmwareDeviceInventoryForDeviceWithOperatingSystem(die.getRefId());
                            DeviceInventoryEntity freshDie = getDeviceInventoryDAO().getDeviceInventory(die.getRefId());
                            diesRemoved.add(freshDie);
                        }
                        this.getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(diesRemoved);
                    }
                } else {  // It's a software update
                    this.runSoftwareUpdates(serviceTemplate, firmwareRepository);
                }

                // Running software updates changes the deploymentEntity,  must reload or there will be a stale
                // Hibernate exception here
                deploymentEntity = this.getDeploymentDAO().getDeployment(getCurrentJobDeployment().getId(),
                        DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);

                deploymentEntity.setStatus(lastDeploymentStatus);
                getDeploymentDAO().updateDeployment(deploymentEntity);

                // Regardless of whether everything is compliant run check for deployment one last time & update status
                // Make sure the software inventory is up to date
                this.getFirmwareUtil().updateServersSoftwareDeviceInventory(deploymentEntity);
                // Make sure the compliance map is recalculated after updating the software inventory
                this.getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(new ArrayList<>(deploymentEntity.getDeployedDevices()));
                // Make sure the deployment compliance is updated after updating the device compliances
                this.getServiceDeploymentUtil().runServiceComplianceCheck(deploymentEntity, true);
            } else { // If it's a teardown of the deployment which was deleted, need to update the devices with the devices with default/embedded
                ArrayList<DeviceInventoryEntity> diesRemoved = new ArrayList<>();
                for (DeviceInventoryEntity die : devInvEntityList) { // Remove the software inventory for all devices
                    this.getDeviceInventoryDAO().deleteFirmwareDeviceInventoryForDeviceWithOperatingSystem(die.getRefId());
                    DeviceInventoryEntity freshDie = getDeviceInventoryDAO().getDeviceInventory(die.getRefId());
                    diesRemoved.add(freshDie);
                }
                this.getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(diesRemoved);
            }
        } catch (Exception e) {
            logger.fatal("Unexpected exception while executing ServiceDeploymentJob!", e);
            setDeploymentFailed(DeploymentStatusType.ERROR);

            // Also need to set the status for devices that succeeded firmware update
            List<DeviceInventoryEntity> servers = getServersInDeployment();
            if (servers != null && servers.size() > 0) {
                if (uncomplyingDevices != null && uncomplyingDevices.size() > 0) {
                    servers.removeAll(uncomplyingDevices);
                }
                for (DeviceInventoryEntity server : servers) {
                    updateDeviceSafelyWithRetries(server, DeviceState.READY);
                }
            }
            setJobStatus(JobStatus.FAILED);
        } finally {
            logger.info("ServiceDeploymentJob has completed for " + this.getJobName());
        }
    }

    private void runSoftwareUpdates(ServiceTemplate serviceTemplate, FirmwareRepositoryEntity firmwareRepository)
            throws AsmManagerCheckedException,
            InvocationTargetException,
            IllegalAccessException,
            SchedulerException {

        // Try to process the Software Firmware regardless of whether the deployment was a success
        DeploymentEntity deploymentEntity = this.getDeploymentDAO().getDeployment(getCurrentJobDeployment().getId(),
                DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);

        logger.info("Deployment " + getCurrentJobDeployment().getId() + " status is " + getCurrentJobDeployment().getStatus() + " prior to starting Software Updates.");

        // Update the Server's Software inventory compliance as it's only updated on an InventoryJob run otherwise
        getFirmwareUtil().updateServersSoftwareDeviceInventory(deploymentEntity);
        // Now that the inventory has changed, the compliance_map's must be updated for all repositories
        // as the compliance_map table will no longer be accurate
        List<DeviceInventoryEntity> devices = new ArrayList<>(deploymentEntity.getDeployedDevices());
        this.getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(devices);
        // Now that compliance_maps and device_inventory compliance are updated, we need to update the deployment's compliance
        this.getServiceDeploymentUtil().runServiceComplianceCheck(deploymentEntity, false);

        // Put it back in progress while we do the firmware upgrades & add retry due to Hibernate / transaction issue
        boolean isCompliant = deploymentEntity.isCompliant();
        for (int count = 0; count < MAX_UPDATE_DAO_FAILURES; count++) {
            try {
                deploymentEntity = this.getDeploymentDAO().getDeployment(getCurrentJobDeployment().getId(),
                        DeploymentDAO.DEVICE_INVENTORY_ENTITIES + DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
                deploymentEntity.setStatus(DeploymentStatusType.POST_DEPLOYMENT_SOFTWARE_UPDATING);
                deploymentEntity.setCompliant(isCompliant);
                this.getDeploymentDAO().updateDeployment(deploymentEntity);
                break;
            } catch (AsmManagerCheckedException | AsmManagerInternalErrorException | HibernateException e) {
                logger.warn("Error when saving Deployment with id " + deploymentEntity.getId() +
                        " in runSoftwareUpdates, attempting to save...", e);
                if (count + 1 == MAX_UPDATE_DAO_FAILURES) { // Need to let Exception propogate if it was never updated...
                    throw e;
                }
            }
            try {
                Thread.sleep(ProxyUtil.POLLING_INTERVAL);
            } catch (Exception e) { // Ignore
            }
        }

        // Update the non-compliant devices after having updated the software inventory compliance
        List<DeviceInventoryEntity> uncomplyingDevices = this.getFirmwareUpdateDevicesInDeployment(firmwareRepository, getCurrentJobDeployment(), DriverType.SOFTWARE);

        // Create Firmware Jobs to update the software wait for them to finish
        if (uncomplyingDevices != null) {
            logger.info("Number of Uncomplying Devices for SoftwareUpdates = " + uncomplyingDevices.size());
            if (uncomplyingDevices.size() > 0 && firmwareRepository != null && serviceTemplate != null) {
                if (!serviceTemplate.hasClusterComponentType()) {
                    createAndRunFirmwareUpdateJobs(uncomplyingDevices,
                            FirmwareUpdateJob.COMPONENTS_SOFTWARE,
                            firmwareRepository,
                            FirmwareUpdateJob.UPDATE_TYPE_DEVICE);
                } else {
                    JobDetail job = FirmwareUtil.getUpdateFirmwareJob(getJobManager(),
                            deploymentEntity.getId(),
                            false,
                            false,
                            FirmwareUpdateJob.UPDATE_TYPE_SERVICE,
                            FirmwareUpdateJob.COMPONENTS_SOFTWARE,
                            firmwareRepository.getId());
                    FirmwareUpdateJobListener jobListener = new FirmwareUpdateJobListener(job.getKey());
                    getJobManager().getScheduler().getListenerManager().addJobListener(jobListener, KeyMatcher.keyEquals(job.getKey()));
                    getJobManager().createAndStartTrigger(job);
                    long start = new Date().getTime();
                    long elapsed = 0L;
                    boolean running = true;
                    try {
                        Thread.sleep(SLEEP_FIRMWARE_POLL);
                        while (running &&
                                elapsed < FirmwareUpdateJob.MAX_FIRMWARE_APPLY_MILLIS) {
                            switch (jobListener.getStatus()) {
                                case CANCELLED:
                                case COMPLETE:
                                    running = false;
                                    break;
                                default:
                                    Thread.sleep(SLEEP_FIRMWARE_POLL);
                                    elapsed = new Date().getTime() - start;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception occurred running Software Update on " + getCurrentJobDeployment().getDeploymentName(), e);
                    }
                }
                // Recompute the list of devices that need updated.
                // This list should now contain 0 devices if all firmware updates were effective.
                uncomplyingDevices = this.getFirmwareUpdateDevicesInDeployment(firmwareRepository, getCurrentJobDeployment(), DriverType.SOFTWARE);
                logger.info(uncomplyingDevices.size() +
                        " non-compliant devices after Software update is complete for deploymentId : " +
                        deploymentEntity.getId());
            }
        }

        // Log info to analyze upgrade process
        if (uncomplyingDevices != null && !uncomplyingDevices.isEmpty()) {
            logger.warn(uncomplyingDevices.size() + " uncomplying devices being found after running Software Updates!");
            logger.warn("After Software updates device with refid " + uncomplyingDevices.get(0).getRefId() + " is still non-compliant!");
        }

        // Finally try to update the inventory one last time
        this.getFirmwareUtil().updateServersSoftwareDeviceInventory(deploymentEntity);
    }

    private List<DeviceInventoryEntity> getFirmwareUpdateDevicesInDeployment(
            final FirmwareRepositoryEntity firmwareRepository,
            final Deployment deployment, final DriverType driverType)
            throws AsmManagerCheckedException {
        List<DeviceInventoryEntity> firmwareUpdateDevices;

        if (this.isScaleUp()) {
            // We 'only' get this list once, at the start of a deployment, as it will change (method will not be able
            // to find the 'scaledUpServerComponents' after the devices are actually deployed.
            if (scaledUpServerComponents == null) {
                scaledUpServerComponents = getServiceDeploymentUtil().getScaledUpServerComponents(deployment);
            }
            firmwareUpdateDevices = getFirmwareUtil().getFirmwareUpdateDevices(this.scaledUpServerComponents, firmwareRepository, true, driverType);
        } else {
            firmwareUpdateDevices = getFirmwareUtil().getFirmwareUpdateDevicesInDeployment(firmwareRepository, deployment, true, driverType);
        }

        return firmwareUpdateDevices;
    }

    // Returns the Device that is no longer part of the deployment
    private List<DeviceInventoryEntity> getRemovedDeploymentEntity(List<DeviceInventoryEntity> dieBeforeDeployment,
                                                                   List<DeviceInventoryEntity> dieAfterDeployment) {
        List<DeviceInventoryEntity> removedDies = new ArrayList<>();
        for (DeviceInventoryEntity die : dieBeforeDeployment) {
            boolean found = false;
            for (DeviceInventoryEntity die2 : dieAfterDeployment) {
                if (die.getRefId().equals(die2.getRefId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removedDies.add(die);
            }
        }
        return removedDies;
    }

    public boolean isMigrate() {
        return isMigrate;
    }

    public void setMigrate(boolean migrate) {
        isMigrate = migrate;
    }

    public boolean isDeployRestart() {
        return isDeployRestart;
    }

    public void setDeployRestart(boolean deployRestart) {
        isDeployRestart = deployRestart;
    }

    public boolean isIndividualTeardown() {
        return individualTeardown;
    }

    public void setIndividualTeardown(boolean individualTeardown) {
        this.individualTeardown = individualTeardown;
    }

    public Deployment getCurrentJobDeployment() {
        return currentJobDeployment;
    }

    public void setCurrentJobDeployment(Deployment deployment) {
        this.currentJobDeployment = deployment;
    }

    public boolean isScaleUp() {
        return isScaleUp;
    }

    public void setScaleUp(boolean scaleUp) {
        isScaleUp = scaleUp;
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

    public FirmwareRepositoryDAO getFirmwareRepositoryDAO() {
        if (firmwareRepositoryDAO == null) {
            firmwareRepositoryDAO = FirmwareRepositoryDAO.getInstance();
        }
        return firmwareRepositoryDAO;
    }

    public void setFirmwareRepositoryDAO(FirmwareRepositoryDAO firmwareRepositoryDAO) {
        this.firmwareRepositoryDAO = firmwareRepositoryDAO;
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

    public DeploymentNamesRefDAO getDeploymentNamesRefDAO() {
        if (deploymentNamesRefDAO == null) {
            deploymentNamesRefDAO = new DeploymentNamesRefDAO();
        }
        return deploymentNamesRefDAO;
    }

    public void setDeploymentNamesRefDAO(DeploymentNamesRefDAO deploymentNamesRefDAO) {
        this.deploymentNamesRefDAO = deploymentNamesRefDAO;
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

    public IPAddressPoolMgr getIpAddressPoolMgr() {
        if (ipAddressPoolMgr == null) {
            ipAddressPoolMgr = (IPAddressPoolMgr) IPAddressPoolMgr.getInstance();
        }
        return ipAddressPoolMgr;
    }

    public void setIpAddressPoolMgr(IPAddressPoolMgr ipAddressPoolMgr) {
        this.ipAddressPoolMgr = ipAddressPoolMgr;
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
}
