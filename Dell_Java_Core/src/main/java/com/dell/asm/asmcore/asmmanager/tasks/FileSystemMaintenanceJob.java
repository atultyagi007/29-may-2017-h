/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.files.DeleteRepoDownloadDirectoryVisitor;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

public class FileSystemMaintenanceJob extends AsmDefaultJob {

    private static final Logger logger = Logger.getLogger(FileSystemMaintenanceJob.class);

    private static final String ASM_TEMP_FILE_DIRECTORY = "/opt/Dell/ASM/temp/";

    public static final String RECURRING_CHECK_FILE_SYSTEM_MAINTENANCE_JOB_KEY_NAME = "FileSystemMaintenance.JobKey.name";
    public static final String RECURRING_CHECK_FILE_SYSTEM_MAINTENANCE_JOB_KEY_GROUP = "FileSystemMaintenance.JobKey.group";

    private ServiceTemplateDAO serviceTemplateDAO;

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.debug("entering FileSystemMaintenanceJob : executeSafely method");
        initializeFromJobContext(context);

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(RECURRING_CHECK_FILE_SYSTEM_MAINTENANCE_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(RECURRING_CHECK_FILE_SYSTEM_MAINTENANCE_JOB_KEY_NAME, jobKey.getName());
        setJobStatus(JobStatus.IN_PROGRESS);

        // Cleanup attachments for unused templates
        try {
            File temmplatesDirectory = new File(ServiceTemplateUtil.TEMPLATE_ATTACHMENT_DIR);
            if (temmplatesDirectory.isDirectory()) {
                File[] templateDirectories = temmplatesDirectory.listFiles();
                if (templateDirectories != null) {
                    for (File directory : templateDirectories) {
                        if (directory.isDirectory()) {
                            String directoryId = directory.getCanonicalPath().substring(ServiceTemplateUtil.TEMPLATE_ATTACHMENT_DIR.length());
                            ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateDAO().getTemplateById(directoryId);
                            if (serviceTemplateEntity == null) {
                                ServiceTemplateUtil.deleteAttachments(directoryId);
                            }
                        }
                    }
                }
            }

            // Cleanup Temp Files Directories
            Path tempFileDirectory = Paths.get(ASM_TEMP_FILE_DIRECTORY);
            if (tempFileDirectory != null && Files.exists(tempFileDirectory)) {
                Files.walkFileTree(tempFileDirectory, new DeleteRepoDownloadDirectoryVisitor());
            }
        } catch (Exception e) {
            logger.error("Exception occurred cleaning up unused Files!", e);
            setJobStatus(JobStatus.FAILED);
        }
        setJobStatus(JobStatus.SUCCESSFUL);
        logger.debug("leaving FileSystemMaintenanceJob : executeSafely method");
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public ServiceTemplateDAO getServiceTemplateDAO() {
        if (serviceTemplateDAO == null) {
            serviceTemplateDAO = ServiceTemplateDAO.getInstance();
        }
        return serviceTemplateDAO;
    }

    public void setServiceTemplateDAO(ServiceTemplateDAO serviceTemplateDAO) {
        this.serviceTemplateDAO = serviceTemplateDAO;
    }
}
