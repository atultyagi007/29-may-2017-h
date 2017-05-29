/**************************************************************************
 * Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks.listener;

import com.dell.asm.asmcore.asmmanager.tasks.FirmwareUpdateJob;
import com.dell.pg.orion.jobmgr.JobManager;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * This implements both Scheduler Listeners and Job Listeners on the same class for data sharing.
 *
 * For every Job that gets scheduled, this listener caches the associated JobDetail so that
 * it's later available if the Job ends or is cancelled.   This way, if FirmwareUpdateJobs are
 * cancelled, this listener gets a chance to reset the Device state so they aren't left in incorrect
 * states, such as PENDING.
 *
 * Only one instance of this class gets created during app init and attached to the global scheduler.
 * Unlike Job listeners, there is no way to specify a Job filter so the listener is global to each Scheduler.
 * Any filtering happens inside the callbacks.
 */
public class FirmwareUpdateScheduleListener implements SchedulerListener, JobListener {

    private static final Logger logger = Logger.getLogger(FirmwareUpdateJobListener.class);

    public static final String LISTENER_NAME = "FirmwareUpdateScheduleListener";

    /*
     * Store JobDetail indexed by the TriggerKey used to schedule this Job.
     */
    private Map<TriggerKey, JobDetail> jobs;

    /*
     * Map JobKey to TriggerKey, necessary for JobListener interface.
     *
     * Technically, a Job can have multiple triggers, which would make this ill-defined,
     * but we can get away with it because we only use one trigger per Job.
     */
    private Map<JobKey,TriggerKey> triggers;

    /*
     *  We create an instance of FirmwareUpdateJob so we can call some utility methods
     *  that are not static.
     */
    private FirmwareUpdateJob firmwareUpdateJob;

    public FirmwareUpdateScheduleListener() {
        logger.info("JobListener " + LISTENER_NAME + " created");
        this.triggers = new HashMap<>();
        this.jobs = new HashMap<>();
        this.firmwareUpdateJob = new FirmwareUpdateJob();
    }

    /**
     * Every Job that gets scheduled is going to call this method.  So, we
     * compare TriggerKey.Group to the Job classname to only cache FirmwareUpdateJobs
     * the assumption is that all our triggers will use the Job classname as the group.
     *
     * @param trigger
     */
    @Override
    public void jobScheduled(Trigger trigger) {
        try {
            logger.debug("Job " + trigger.getJobKey().getName() + " was SCHEDULED");
            TriggerKey triggerKey = trigger.getKey();
            String group = triggerKey.getGroup();
            if (group.equals(FirmwareUpdateJob.class.getSimpleName())) {
                logger.debug("Job " + trigger.getJobKey().getName() + " was CACHED in FirmwareUpdateScheduleListener");
                JobKey jobKey = trigger.getJobKey();
                JobDetail job = JobManager.getInstance().getJobDetail(jobKey);
                this.triggers.put(jobKey, triggerKey);
                this.jobs.put(triggerKey, job);
            }
        } catch (Exception e) {
            logger.error("Caught exception.  Unable to store data for schedule listener for FirmwareUpdateJob");
        }
    }

    /**
     * If the associated FirmwareUpdateJob is cancelled, call the cancel method
     * on the FirmwareUpdateJob class to perform whatever cleanup.
     *
     * It occurs to me we might have been able to implement the ScheduleListener
     * interface directly on the FirmwareUpdateJob class itself.
     *
     * @param triggerKey
     */
    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        logger.debug("Job " + triggerKey.getName() + " was UNSCHEDULED");
        try {
            JobDetail job = jobs.get(triggerKey);
            if (job!=null) {
                firmwareUpdateJob.cancel(job);
                jobs.remove(triggerKey);
            } else {
                logger.info("Oddity: Unable to find FirmwareUpdateJob cached in ScheduleListener");
            }
        } catch (Exception e) {
            logger.error("Caught exception.  Unable to cleanup deleted FirmwareUpdateJob");
        }
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {

    }

    @Override
    public void triggersPaused(String triggerGroup) {

    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {

    }

    @Override
    public void triggersResumed(String triggerGroup) {

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
    }

    /**
     * Cleanup any cached data associated with the Job
     *
     * @param jobKey
     */
    @Override
    public void jobDeleted(JobKey jobKey) {
        logger.debug("Job " + jobKey.toString() + " was DELETED");
        TriggerKey triggerKey = triggers.get(jobKey);
        if (triggerKey!=null) {
            if (jobs.containsKey(triggerKey)) {
                jobs.remove(triggerKey);
            }
        }
        if (triggers.containsKey(jobKey)) {
            triggers.remove(jobKey);
        }
    }

    @Override
    public void jobPaused(JobKey jobKey) {

    }

    @Override
    public void jobsPaused(String jobGroup) {

    }

    @Override
    public void jobResumed(JobKey jobKey) {

    }

    @Override
    public void jobsResumed(String jobGroup) {

    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {

    }

    @Override
    public void schedulerInStandbyMode() {

    }

    @Override
    public void schedulerStarted() {

    }

    @Override
    public void schedulerShutdown() {

    }

    @Override
    public void schedulerShuttingdown() {

    }

    @Override
    public void schedulingDataCleared() {

    }

    //
    // Methods below here implement the JobListener interface
    //
    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        logger.debug("Job " + getName() + "is starting!");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.debug("Job " + getName() + "was cancelled!");
    }

    /**
     *  Release any cached data for Jobs that complete without being cancelled.
     *
     * @param context
     * @param jobException
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        logger.debug("Job " + getName() + "has finished!");
        JobKey jobKey = context.getJobDetail().getKey();
        TriggerKey triggerKey = context.getTrigger().getKey();

        if (triggerKey!=null) {
            if (jobs.containsKey(triggerKey)) {
                jobs.remove(triggerKey);
            }
        }
        if (jobKey!=null) {
            if (triggers.containsKey(jobKey)) {
                triggers.remove(jobKey);
            }
        }
    }

}
