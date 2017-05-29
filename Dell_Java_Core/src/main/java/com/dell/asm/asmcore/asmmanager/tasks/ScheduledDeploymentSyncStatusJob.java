package com.dell.asm.asmcore.asmmanager.tasks;


import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deployment.IDeploymentService;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

public class ScheduledDeploymentSyncStatusJob extends AsmDefaultJob {

    public static final String RECURRING_CHECK_SCHEDULED_DEPLOYMENT_SYNC_JOB_KEY_NAME = "ScheduledDeploymentSyncStatusJob.JobKey.name";
    public static final String RECURRING_CHECK_SCHEDULED_DEPLOYMENT_SYNC_JOB_KEY_GROUP = "ScheduledDeploymentSyncStatusJob.JobKey.group";

    public static int RESCHEDULE_DELAY = 10 * 60;

    private static final Logger logger = Logger.getLogger(ScheduledDeploymentSyncStatusJob.class);

    private DeploymentDAO deploymentDAO;
    private FirmwareRepositoryDAO firmwareRepositoryDAO;
    private ServiceDeploymentUtil serviceDeploymentUtil;
    private FirmwareUtil firmwareUtil;

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.info(" Executing ScheduledDeploymentSyncStatusJob");
        initializeFromJobContext(context);

        ScheduledDeploymentSyncStatusJob.class.getClassLoader();

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(RECURRING_CHECK_SCHEDULED_DEPLOYMENT_SYNC_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(RECURRING_CHECK_SCHEDULED_DEPLOYMENT_SYNC_JOB_KEY_NAME, jobKey.getName());
        setJobStatus(JobStatus.IN_PROGRESS);
        try {
            List<? extends Trigger> triggers = getJobManager().getTriggersOfJob(jobKey);
            if (triggers != null && triggers.size() > 0) {
                logger.info("Time taken for the next ScheduledDeploymentSyncStatusJob: " + triggers.get(0).getNextFireTime());
            }
        } catch (SchedulerException e1) {
            logger.error("Unable to get job info for ScheduledDeploymentSyncStatusJob", e1);
        }

        try {
            List<DeploymentEntity> deployments = getDeploymentDAO().getAllDeployment(DeploymentDAO.ALL_ENTITIES);
            IAsmDeployerService asmDeployerService = ProxyUtil.getAsmDeployerProxy();
            for (DeploymentEntity deploymentEntity : deployments) {
                AsmDeployerStatus status = null;
                try {
                    status = asmDeployerService.getDeploymentStatus(deploymentEntity.getId());
                    if (status == null) {
                        logger.warn("null returned from asmDeployer when trying to retrieve status for deployment with id of " + deploymentEntity.getId());
                    }
                } catch (Exception e) {
                    // IGNORE this exception so ALL deployments are processed
                    logger.warn("Unable to retrieve status from asmDeployer for deployment with id of " + deploymentEntity.getId(), e);
                }
                if (status == null) {
                    //get up to date entity
                    deploymentEntity = getDeploymentDAO().getDeployment(deploymentEntity.getId(), DeploymentDAO.DEVICE_INVENTORY_ENTITIES + DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
                    if (DeploymentStatusType.IN_PROGRESS.equals(deploymentEntity.getStatus()) ||
                            DeploymentStatusType.PENDING.equals(deploymentEntity.getStatus())) {
                        deploymentEntity.setStatus(DeploymentStatusType.ERROR);
                        getDeploymentDAO().updateDeployment(deploymentEntity);
                    }
                } else {
                    DeploymentStatusType newStatus = status.getStatus();
                    if (newStatus != deploymentEntity.getStatus()) {
                        // Status changed
                        deploymentEntity = getDeploymentDAO().getDeployment(deploymentEntity.getId(), DeploymentDAO.DEVICE_INVENTORY_ENTITIES + DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
                        deploymentEntity.setStatus(newStatus);
                        getDeploymentDAO().updateDeployment(deploymentEntity);
                    }

                    if (deploymentEntity.isManageFirmware()) {
                        FirmwareRepositoryEntity fw = null;
                        if (deploymentEntity.isUseDefaultCatalog()) {
                            fw = getFirmwareUtil().getDefaultRepo();
                        } else if (deploymentEntity.getFirmwareRepositoryEntity() != null) {
                            fw = getFirmwareRepositoryDAO().get(deploymentEntity.getFirmwareRepositoryEntity().getId());
                        }
                        deploymentEntity.setFirmwareRepositoryEntity(fw);
                    } else {
                        getFirmwareUtil().unmanageFirmware(deploymentEntity);
                    }

                    // Assumes the compliance_map table is in a proper state and recalculates the deployment compliance
                    getServiceDeploymentUtil().runServiceComplianceCheck(deploymentEntity, true);

                    if (DeploymentStatusType.PENDING.equals(newStatus) || DeploymentStatusType.IN_PROGRESS.equals(newStatus)) {
                        // if newStatus is PENDING or IN PROGRESS then restart the job
                        IDeploymentService deploymentService = ProxyUtil.getDeploymentProxy();
                        Deployment deployment = deploymentService.getDeployment(deploymentEntity.getId());
                        try {
                            this.scheduleJobRestart(deployment);
                        } catch (Exception e) {
                            logger.error("Error restarting deployment for deployment with id of: " + deployment.getId(), e);
                            // Swallow/Ignore this error so all of the Deployments are processed
                        }
                    }
                }
            }
            setJobStatus(JobStatus.SUCCESSFUL);

            EEMILocalizableMessage msg = AsmManagerMessages.deploymentStatusSyncCompleted();
            LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);

        } catch (LocalizedWebApplicationException wex) {
            logger.error("Unable to sync deployment status with asm_deployer", wex);
            setJobStatus(JobStatus.FAILED);

            EEMILocalizableMessage msg = AsmManagerMessages.deploymentStatusSyncFailed();
            LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.DEPLOYMENT);

            // asm_deployer was not found - reschedule the job in 10 minutes
            AsmManagerApp.createScheduledDeploymentStatusSyncJob(RESCHEDULE_DELAY);

        } catch (InvocationTargetException | IllegalAccessException | AsmManagerCheckedException e) {
            logger.error("Unable to sync deployment status with asm_deployer", e);
            setJobStatus(JobStatus.FAILED);

            EEMILocalizableMessage msg = AsmManagerMessages.deploymentStatusSyncFailed();
            LocalizableMessageService.getInstance().logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.DEPLOYMENT);
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private void scheduleJobRestart(Deployment deployment) throws SchedulerException {
        IJobManager jm = JobManager.getInstance();
        JobDetail job = jm.createNamedJob(ServiceDeploymentJob.class);

        String jsonData = DeploymentService.toJson(deployment);
        job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_SERVICE_KEY_DATA, jsonData);
        job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_IS_RESTART_DATA, true);

        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
        Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

        jm.scheduleJob(job, trigger);
        logger.info("checking and starting the scheduler");
        if (!jm.getScheduler().isStarted()) {
            jm.getScheduler().start();
            logger.info("scheduler started");
        }
        // Return the job name.
        setJobName(job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME));
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

    public ServiceDeploymentUtil getServiceDeploymentUtil() {
        if (serviceDeploymentUtil == null) {
            serviceDeploymentUtil = new ServiceDeploymentUtil();
        }
        return serviceDeploymentUtil;
    }

    public void setServiceDeploymentUtil(ServiceDeploymentUtil serviceDeploymentUtil) {
        this.serviceDeploymentUtil = serviceDeploymentUtil;
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
}
