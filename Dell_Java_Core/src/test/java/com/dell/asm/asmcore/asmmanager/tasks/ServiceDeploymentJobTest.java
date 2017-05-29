package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentNamesRefDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceDeploymentJobTest {

    private ServiceDeploymentJob serviceDeploymentJob;

    private IJobManager jobManager;
    private IJobHistoryManager jobHistoryManager;
    private DeploymentDAO deploymentDAO;
    private FirmwareRepositoryDAO firmwareRepositoryDAO;
    private DeviceInventoryDAO deviceInventoryDAO;
    private DeploymentNamesRefDAO deploymentNamesRefDAO;
    private IAsmDeployerService asmDeployerService;
    private ServiceTemplateUtil serviceTemplateUtil;
    private IPAddressPoolMgr ipAddressPoolMgr;
    private FirmwareUtil firmwareUtil;
    private ServiceDeploymentUtil serviceDeploymentUtil;

    private JobExecutionContext jobContext;
    private JobDetail jobDetail;
    private JobDataMap jobDataMap;
    private JobKey jobKey;

    private String jsonData;
    private boolean isScaleUp;
    private boolean isMigrate;
    private boolean isDeployRestart;
    private boolean isIndividualTeardown;

    @Before
    public void setUp() throws Exception {

        jobManager = mock(IJobManager.class);
        jobHistoryManager = mock(IJobHistoryManager.class);
        deploymentDAO = mock(DeploymentDAO.class);
        firmwareRepositoryDAO = mock(FirmwareRepositoryDAO.class);
        deviceInventoryDAO = mock(DeviceInventoryDAO.class);
        deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);
        asmDeployerService = mock(IAsmDeployerService.class);
        serviceTemplateUtil = mock(ServiceTemplateUtil.class);
        ipAddressPoolMgr = mock(IPAddressPoolMgr.class);
        firmwareUtil = mock(FirmwareUtil.class);
        serviceDeploymentUtil = mock(ServiceDeploymentUtil.class);

        serviceDeploymentJob = new ServiceDeploymentJob();
        serviceDeploymentJob.setJobManager(jobManager);
        serviceDeploymentJob.setJobHistoryManager(jobHistoryManager);
        serviceDeploymentJob.setDeploymentDAO(deploymentDAO);
        serviceDeploymentJob.setFirmwareRepositoryDAO(firmwareRepositoryDAO);
        serviceDeploymentJob.setDeviceInventoryDAO(deviceInventoryDAO);
        serviceDeploymentJob.setAsmDeployerService(asmDeployerService);
        serviceDeploymentJob.setServiceTemplateUtil(serviceTemplateUtil);
        serviceDeploymentJob.setIpAddressPoolMgr(ipAddressPoolMgr);
        serviceDeploymentJob.setFirmwareUtil(firmwareUtil);
        serviceDeploymentJob.setServiceDeploymentUtil(serviceDeploymentUtil);

        jobKey = new JobKey("ServiceDeploymentJobKey", "ServiceDeploymentJobGroup");

        //setup JobExecutionContext
        jobDataMap = mock(JobDataMap.class);
        when(jobDataMap.getString(JobManager.JM_JOB_HISTORY_JOBNAME)).thenReturn("TestServiceDeploymentJob");

        jobDetail = mock(JobDetail.class);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(jobDetail.getKey()).thenReturn(jobKey);

        jobContext = mock(JobExecutionContext.class);
        when(jobContext.get(JobManager.JM_EXEC_HISTORY_ID)).thenReturn(10L);
        when(jobContext.getJobDetail()).thenReturn(jobDetail);


    }

    @Test
    public void testInitializeFromJobContext() throws Exception {
        Deployment deployment = new Deployment();
        deployment.setId("deploymentId");
        deployment.setDeploymentName("Test Deployment Name");
        setJsonData(DeploymentService.toJson(deployment));
        setScaleUp(true);
        setMigrate(true);
        setDeployRestart(true);
        setIndividualTeardown(true);

        initializedJobContext();
        serviceDeploymentJob.initializeServiceDeploymentJob(jobContext);

        Deployment marshalled = serviceDeploymentJob.getCurrentJobDeployment();
        assertTrue(marshalled != null);
        assertTrue(marshalled.getId().equals("deploymentId"));
        assertTrue(marshalled.getDeploymentName().equals("Test Deployment Name"));
        assertTrue(serviceDeploymentJob.isScaleUp());
        assertTrue(serviceDeploymentJob.isMigrate());
        assertTrue(serviceDeploymentJob.isDeployRestart());
        assertTrue(serviceDeploymentJob.isIndividualTeardown());
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public boolean isScaleUp() {
        return isScaleUp;
    }

    public void setScaleUp(boolean scaleUp) {
        isScaleUp = scaleUp;
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
        return isIndividualTeardown;
    }

    public void setIndividualTeardown(boolean individualTeardown) {
        isIndividualTeardown = individualTeardown;
    }

    public void initializedJobContext() {
        when(jobDataMap.getString(ServiceDeploymentJob.ServiceDeploymentJob_SERVICE_KEY_DATA)).thenReturn(getJsonData());
        when(jobDataMap.getBoolean(ServiceDeploymentJob.ServiceDeploymentJob_IS_SCALE_UP)).thenReturn(isScaleUp());
        when(jobDataMap.getBoolean(ServiceDeploymentJob.ServiceDeploymentJob_IS_MIGRATE_DATA)).thenReturn(isMigrate());
        when(jobDataMap.getBoolean(ServiceDeploymentJob.ServiceDeploymentJob_IS_RESTART_DATA)).thenReturn(isDeployRestart());
        when(jobDataMap.getBoolean(ServiceDeploymentJob.ServiceDeploymentJob_INDIVIDUAL_TEARDOWN)).thenReturn(isIndividualTeardown());
    }
}
