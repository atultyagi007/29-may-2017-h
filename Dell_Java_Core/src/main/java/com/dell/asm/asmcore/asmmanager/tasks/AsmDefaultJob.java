package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AsmDefaultJob implements Job {

    private IJobManager jobManager;
    private IJobHistoryManager jobHistoryManager;
    
    private long execHistoryId;
    private String jobName;

    public abstract Logger getLogger();

    protected abstract void executeSafely(JobExecutionContext context);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try{
            this.executeSafely(context);
        }catch(Exception e){
            this.getLogger().fatal("Unexpected exception while executing " + this.getClass(), e);
        }
    }

    public void initializeFromJobContext(JobExecutionContext context) {
        this.getLogger().info("Initializing JobContext for AsmDefaultJob.");
        setExecHistoryId((Long) context.get(JobManager.JM_EXEC_HISTORY_ID));
        setJobName(context.getJobDetail().getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME));
    }

    /**
     * set the job status message
     * @param status  JobStatus
     */
    protected void setJobStatus(JobStatus status) {
        setJobStatus(status, null, null);
    }

    /**
     * Set the job status message with key and value
     *
     * @param status status
     * @param key    key
     * @param msg    message
     */
    public void setJobStatus(final JobStatus status,
                             final String key,
                             final String msg) {
        try {
            this.getJobHistoryManager().setExecHistoryStatus(this.getExecHistoryId(), status);
            this.getLogger().info("setting status for job " + this.getJobName() + " to " + status);
            if (msg != null) {
                if (JobStatus.FAILED.equals(status))
                    this.getLogger().error(msg);
                else
                    this.getLogger().info(msg);
                addJobDetail(key, msg);
            }
        } catch (JobManagerException e) {
            String errMsg = "Unable to save execution status for job " + this.getJobName() + "-" + this.getExecHistoryId();
            this.getLogger().error(errMsg, e);
        }
    }

    /**
     * Add job details for the job
     *
     * @param key message key
     * @param msg message
     */
    public void addJobDetail(final String key,
                             final String msg) {
        try {
            getJobHistoryManager().setExecDetail(this.getExecHistoryId(), key, msg);
            this.getLogger().info("detail for job " + this.getJobName() + ": " + key + " = " + msg);
        } catch (JobManagerException e) {
            String errMsg = "Unable to add execution detail to job " + this.getJobName() + "-" + this.getExecHistoryId();
            this.getLogger().error(errMsg, e);
        }
    }

    public long getExecHistoryId() {
        return execHistoryId;
    }

    public void setExecHistoryId(long execHistoryId) {
        this.execHistoryId = execHistoryId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public IJobManager getJobManager() {
        if (jobManager == null) {
            jobManager = JobManager.getInstance();
        }
        return jobManager;
    }

    public void setJobManager(IJobManager jobManager) {
        this.jobManager = jobManager;
    }

    public IJobHistoryManager getJobHistoryManager() {
        if (jobHistoryManager == null) {
            jobHistoryManager = getJobManager().getJobHistoryManager();
        }
        return jobHistoryManager;
    }

    public void setJobHistoryManager(IJobHistoryManager jobHistoryManager) {
        this.jobHistoryManager = jobHistoryManager;
    }
}
