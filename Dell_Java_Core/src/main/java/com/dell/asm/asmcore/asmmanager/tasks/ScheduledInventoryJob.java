package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.util.List;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public class ScheduledInventoryJob extends ASMInventoryJob {
    public static final String RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_NAME = "ScheduledInventory.JobKey.name";
    public static final String RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_GROUP = "ScheduledInventory.JobKey.group";

    private static final Logger logger = Logger.getLogger(ScheduledInventoryJob.class);

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.info(" Executing SchedulingInventoryJob");
        initializeFromJobContext(context);

        ScheduledInventoryJob.class.getClassLoader();
        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_NAME, jobKey.getName());
        setJobStatus(JobStatus.IN_PROGRESS);
        try {
            List<? extends Trigger> triggers = getJobManager().getTriggersOfJob(jobKey);
            if (triggers != null && triggers.size() > 0) {
                logger.info("Time taken for the next ScheduledJob Inventory: " + triggers.get(0).getNextFireTime());
            }
        } catch (SchedulerException e1) {
            logger.error("Unable to get job info for scheduled inventory job", e1);
        }
        try {
            getLogService().logMsg(AsmManagerMessages.startedScheduledInventoryJob("All devices", getJobName()).getDisplayMessage(),
                    LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            ProxyUtil.getInventoryProxy().updateDeviceInventories(null);
            setJobStatus(JobStatus.SUCCESSFUL);
            getLogService().logMsg(AsmManagerMessages.completedScheduledInventoryJob("All devices", getJobName()).getDisplayMessage(),
                    LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            logger.debug("ScheduledInventory job was succesful:" + getJobName());
        } catch (Throwable t) {
            setJobStatus(JobStatus.FAILED);
            logger.error("Scheduled Inventory job: scheduled inventory job failed");
            getLogService().logMsg(AsmManagerMessages.errorScheduledInventoryJob("All devices", getJobName()).getDisplayMessage(),
                    LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);

        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
