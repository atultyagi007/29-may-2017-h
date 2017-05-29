/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import com.dell.asm.asmcore.asmmanager.app.rest.FirmwareRepositoryService;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareRepositoryFileUtil;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobStatus;

/**
 * FirmwareRepoSyncJob is a background job to download all binaries from a firmware repository
 * onto the appliance.
 *
 * This job should be considered a first draft at this stage, only light testing on NFS catalogs
 * has been performed.
 *
 * TODO:
 *
 * - Integrate with user activity log. Log overall status and all user-facing failures.
 *
 * - Add status information to repository. Block firmware jobs when the repository is not
 *   fully downloaded.
 */
public class FirmwareRepoSyncJob extends AsmDefaultJob {
    public static final String FIRMWARE_REPO_ID_KEY = "firmware_repo.ref_id";
    public static final String JOB_KEY_NAME = "JobKey.name";
    public static final String JOB_KEY_GROUP = "JobKey.group";
    public static final String IS_DEFAULT_CATALOG_KEY = "is.default.catalog";
    
    private static final Logger LOGGER = Logger.getLogger(FirmwareUpdateJob.class);

    private FirmwareRepositoryFileUtil firmwareRepositoryFileUtil;
    private final IJobHistoryManager historyMgr;

    
    private long execHistoryId = 0L;
    private String jobName = "UNKNOWN JOB";
    private FirmwareRepositoryService firmwareRepositoryService = new FirmwareRepositoryService();

    public FirmwareRepoSyncJob(IJobHistoryManager historyMgr) {
        this.historyMgr = historyMgr;
    }

    // no-arg constructor for Quartz
    public FirmwareRepoSyncJob() {
        this(JobManager.getInstance().getJobHistoryManager());
    }

    @Override
    protected void executeSafely(JobExecutionContext context) {
    	String firmwareRepoId = "";
    	
    	try {
            initializeFromJobContext(context);

            JobKey jobKey = context.getJobDetail().getKey();
            addJobDetail(JOB_KEY_GROUP, jobKey.getGroup());
            addJobDetail(JOB_KEY_NAME, jobKey.getName());

            firmwareRepoId = (String) context.getJobDetail().getJobDataMap().get(FIRMWARE_REPO_ID_KEY);
            boolean isDefaultCatalog = ((Boolean) context.getJobDetail().getJobDataMap().get(IS_DEFAULT_CATALOG_KEY));
            
            setJobStatus(JobStatus.IN_PROGRESS);
            getFirmwareRepositoryFileUtil().syncFirmwareRepository(firmwareRepoId);
            if (isDefaultCatalog) {
            	this.firmwareRepositoryService.changeToDefaultCatalog(firmwareRepoId, true);
            }
            setJobStatus(JobStatus.SUCCESSFUL);
        } catch (Exception e) {
            LOGGER.error("FirmwareRepoSyncJob failed for firmware id " + firmwareRepoId, e);
            try{
            	setJobStatus(JobStatus.FAILED);
            }catch(Exception e2){
            	LOGGER.error("FirmwareRepoSyncJob failed for firmware id " + firmwareRepoId, e);
            }
        }
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    public FirmwareRepositoryFileUtil getFirmwareRepositoryFileUtil() {
        if (firmwareRepositoryFileUtil == null) {
            firmwareRepositoryFileUtil = new FirmwareRepositoryFileUtil();
        }
        return firmwareRepositoryFileUtil;
    }

    public void setFirmwareRepositoryFileUtil(FirmwareRepositoryFileUtil firmwareRepositoryFileUtil) {
        this.firmwareRepositoryFileUtil = firmwareRepositoryFileUtil;
    }
}
