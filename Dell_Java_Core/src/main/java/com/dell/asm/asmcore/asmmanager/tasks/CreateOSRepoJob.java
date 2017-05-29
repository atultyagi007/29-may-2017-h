package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.OSRepositoryService;
import com.dell.asm.asmcore.asmmanager.client.osrepository.OSRepository;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.OSRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.orion.jobmgr.JobStatus;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

public class CreateOSRepoJob extends AsmDefaultJob {
    private static final Logger logger = Logger.getLogger(CreateOSRepoJob.class);

    public static final String CreateOSRepoJob_SERVICE_KEY_DATA = "CreateOSRepo";

    private OSRepositoryUtil osRepoFileUtil;
    private GenericDAO genericDAO;

    @Override
    protected void executeSafely(JobExecutionContext context){
        logger.debug("Entering CreateOSRepoJob : execute method");
        initializeFromJobContext(context);
        setJobStatus(JobStatus.IN_PROGRESS);
        String jsonData = context.getJobDetail().getJobDataMap().getString(CreateOSRepoJob_SERVICE_KEY_DATA);
        OSRepository osRepo = OSRepositoryService.fromJson(jsonData);
        OSRepositoryEntity entity = getGenericDAO().get(osRepo.getId(), OSRepositoryEntity.class);
        osRepo.setRazorName(entity.getRazorName());
        try {
            entity.setState("copying");
            getOsRepoFileUtil().createRazorRepo(osRepo);
            entity.setState("available");
            setJobStatus(JobStatus.SUCCESSFUL);
        }catch (LocalizedWebApplicationException e) {
            entity.setState("errors");
            setJobStatus(JobStatus.FAILED);
            logger.error(
                    "LocalizedWebApplicationException while creating OS Repository  "
                            + osRepo.getName(), e);
            throw e;
        } catch (Exception e) {
            entity.setState("errors");
            setJobStatus(JobStatus.FAILED);
            logger.error("Exception while creating OS Repository "
                    + osRepo.getName(), e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        finally {
            getGenericDAO().update(entity);
        }   	
    }

    public OSRepositoryUtil getOsRepoFileUtil() {
        if (osRepoFileUtil == null) {
            osRepoFileUtil = new OSRepositoryUtil();
        }
        return osRepoFileUtil;
    }

    public void setOsRepoFileUtil(OSRepositoryUtil osRepoFileUtil) {
        this.osRepoFileUtil = osRepoFileUtil;
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

    @Override
    public Logger getLogger() {
        return logger;
    }
}
