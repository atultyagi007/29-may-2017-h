package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.ConfigureDevicesService;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.client.configure.AddressingMode;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobStatus;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.apache.shiro.util.CollectionUtils;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;


public class InitialConfigurationJob extends AsmDefaultJob {
    public static final String ServiceDeploymentJob_JOB_KEY_NAME = "Configuration.JobKey.name";
    public static final String ServiceDeploymentJob_JOB_KEY_GROUP = "Configuration.JobKey.group";
    public static final String ServiceDeploymentJob_SERVICE_KEY_DATA = "Configuration";

    private static final Logger logger = Logger.getLogger(InitialConfigurationJob.class);

    private LocalizableMessageService logService;
    private DiscoveryResultDAO discoveryResultDAO;
    private DeviceDiscoverDAO discoverRequestDAO;
    private DeviceInventoryUtils deviceInventoryUtils;

    @Override
    protected void executeSafely(JobExecutionContext context){
    	logger.info("Executing Initial Configuration And Discovery Job");
        initializeFromJobContext(context);

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(ServiceDeploymentJob_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(ServiceDeploymentJob_JOB_KEY_NAME, jobKey.getName());
        String jsonData = context.getJobDetail().getJobDataMap().getString(ServiceDeploymentJob_SERVICE_KEY_DATA);

        Deployment deployment = DeploymentService.fromJson(jsonData);

        boolean success = false;
        String tags = "";
        setJobStatus(JobStatus.IN_PROGRESS);

        // call ruby rest endpoint
        IAsmDeployerService proxy = ProxyUtil.getAsmDeployerProxy();
        try {
            AsmDeployerStatus status;
            String deploymentId = deployment.getId();
            status = proxy.createDeployment(deployment);

            int i = 0;
            while (DeploymentStatusType.IN_PROGRESS.equals(status.getStatus())
                    && ProxyUtil.MAX_POLL_ITER > i++) {
                Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                status = proxy.getDeploymentStatus(deploymentId);
            }

            if (DeploymentStatusType.COMPLETE.equals(status.getStatus())) {
                // configuration completed - proceed with discovery
                DiscoveryRequest discoveryRequest = new DiscoveryRequest();
                discoveryRequest.setDiscoveryRequestList(new DiscoverIPRangeDeviceRequests());

                for (ServiceTemplateComponent component: deployment.getServiceTemplate().getComponents()) {
                    if (tags.length() > 0)
                        tags += ",";

                    tags += component.getComponentID();

                    DiscoverIPRangeDeviceRequest request = new DiscoverIPRangeDeviceRequest();
                    request.setDeviceStartIp(getIPFromConfigurationTemplate(component));
                    request.setDeviceEndIp(request.getDeviceStartIp());
                    request.setUnmanaged(false);
                    if (component.hasCMCConfigJob()) {
                        request.setDeviceChassisCredRef(getDeviceInventoryUtils().getChassisCredentialFromConfigurationTemplate(component));
                        request.setDeviceServerCredRef(getDeviceInventoryUtils().getServerCredentialFromConfigurationTemplate(component));
                        request.setDeviceSwitchCredRef(getDeviceInventoryUtils().getIOMCredentialFromConfigurationTemplate(component));
                        request.setDeviceType(DeviceType.Chassis);
                    }else {
                        request.setDeviceServerCredRef(getDeviceInventoryUtils().getRackServerCredentialFromConfigurationTemplate(component));
                        request.setDeviceType(DeviceType.Server);
                    }

                    // we should have discovery results for this chassis
                    DiscoveryResultEntity entity = DiscoveryJobUtils.getDiscoveryResultEntity(component.getId());
                    if (entity!=null) {
                        request.setServerPoolId(entity.getServerPoolId());
                        request.setUnmanaged(entity.isUnmanaged());
                        request.setReserved(entity.isReserved());
                        // can't remove by parent job - there are might be other pending jobs with the same parent ID
                        getDiscoveryResultDAO().deleteDiscoveryResultByRefId(entity.getRefId());
                        if (CollectionUtils.isEmpty(getDiscoveryResultDAO().getDiscoveryResult(entity.getParentJobId()))) {
                            // all devices processed - we can delete record from devicediscover as well
                            getDiscoverRequestDAO().deleteDiscoveryResult(entity.getParentJobId());
                        }
                    }

                    // save initial configuration to pass it to inventory
                    ServiceTemplate configuration = new ServiceTemplate();
                    configuration.getComponents().add(component);
                    request.setConfig(MarshalUtil.marshal(configuration));

                    discoveryRequest.getDiscoveryRequestList().getDiscoverIpRangeDeviceRequests().add(request);

                }

                startDiscoverByIPJob(discoveryRequest);

                success = true;
            }

        } catch (Exception e) {
            logger.error("InitialConfigurationJob.execute error",e);
        }

        if (success) {
            setJobStatus(JobStatus.SUCCESSFUL);
            if (deployment.getServiceTemplate().getComponents().get(0).hasCMCConfigJob()) {
                getLogService().logMsg(AsmManagerMessages.completedConfigurationJob(tags, getJobName()).getDisplayMessage(),
                        LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }else{
                getLogService().logMsg(AsmManagerMessages.serverConfigurationJobCompleted(tags, getJobName()).getDisplayMessage(),
                        LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }
        }
        else {
            setJobStatus(JobStatus.FAILED);
            ConfigureDevicesService.rollbackConfiguration(deployment.getServiceTemplate());
            if (deployment.getServiceTemplate().getComponents().get(0).hasCMCConfigJob()) {
                getLogService().logMsg(AsmManagerMessages.applyConfigurationFailed(getJobName()).getDisplayMessage(),
                        LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }else{
                getLogService().logMsg(AsmManagerMessages.serverConfigurationJobFailed(getJobName()).getDisplayMessage(),
                        LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }
        }

        logger.info("InitialConfigurationJob completed for the job name:" + getJobName() + ", status =" + success);
    }

    /**
     * If chassis networking mode == existing, use IP from component IP field.
     * If chassis networking mode == static, use IP from reservation
     * If chassis networking mode == dhcp, use ???
     * @param component
     * @return
     */
    private String getIPFromConfigurationTemplate(ServiceTemplateComponent component) {
        boolean modeStatic = false;
        for (ServiceTemplateCategory category: component.getResources()) {
            if (category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID)) {
                for (ServiceTemplateSetting setting: category.getParameters()) {
                    if (setting.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORK_TYPE)) {
                        if (setting.getValue().equals(AddressingMode.Existing.getValue())) {
                            return component.getIP();
                        }else if (setting.getValue().equals(AddressingMode.Static.getValue())) {
                            modeStatic = true;
                        }else{
                            // TODO: what to do with DHCP? where do I get IPs?

                            logger.error("Not supported: DHCP network IP for CMC chosen for service tag:" + component.getComponentID());
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());

                        }
                    }else if (modeStatic && setting.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORKS)) {
                        if (setting.getNetworks() != null && setting.getNetworks().size() == 1) {
                            return setting.getNetworks().get(0).getStaticNetworkConfiguration().getIpAddress();
                        } else {
                            logger.error("Invalid configuration template: no static network IP for CMC, service tag:" + component.getComponentID());
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                        }
                    }
                }
            } else if (category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_SERVER_ID)) {
                for (ServiceTemplateSetting setting: category.getParameters()) {
                    if (setting.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORK_TYPE)) {
                        if (setting.getValue().equals(AddressingMode.Existing.getValue())) {
                            return component.getIP();
                        }else if (setting.getValue().equals(AddressingMode.Static.getValue())) {
                            modeStatic = true;
                        }else{
                            logger.error("Not supported: DHCP mode IP address for IDRAC, service tag:" + component.getComponentID());
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                        }
                    }else if (modeStatic && setting.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORKS)) {
                        if (setting.getNetworks() != null && setting.getNetworks().size() == 1) {
                            return setting.getNetworks().get(0).getStaticNetworkConfiguration().getIpAddress();
                        } else {
                            logger.error("Invalid configuration template: no static network IP for IDRAC, service tag:" + component.getComponentID());
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                        }
                    }
                }
            }
        }
        return null;
    }

    private void startDiscoverByIPJob(DiscoveryRequest discoveryRequest) {

        String jobName = "";

        DiscoverIPRangeDeviceRequests discoveryRequestList = discoveryRequest.getDiscoveryRequestList();
        DiscoveryJobUtils.validateDiscoveryRequest(discoveryRequestList);

        IJobManager jm = JobManager.getInstance();
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

        JobDetail job = jm.createNamedJob(DiscoverIpRangeJob.class);

        String xmlRequest = MarshalUtil.marshal(discoveryRequest);
        String xmlData = MarshalUtil.marshal(discoveryRequestList);
        job.getJobDataMap().put(DiscoverIpRangeJob.DISCOVERIPRANGE_SERVICE_KEY_DATA, xmlData);
        job.getJobDataMap().put(DiscoverIpRangeJob.REQUEST_FROM_INITIAL_CONFIG_JOB, "true");

        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

        // Schedule our job using our trigger.
        try {
            jm.scheduleJob(job, trigger);
            logger.info("checking and starting the scheduler");
            if (!jm.getScheduler().isStarted()) {
                jm.getScheduler().start();
                logger.info("scheduler started");
            }
            // Return the job name.
            jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
            discoveryRequest.setId(jobName);
            discoveryRequest.setStatus(DiscoveryStatus.INPROGRESS);

            try {
                logger.debug("create device discovery request entity.");
                DeviceDiscoverEntity deviceDiscoverEntity = new DeviceDiscoverEntity();
                deviceDiscoverEntity.setId(jobName);
                deviceDiscoverEntity.setStatus(DiscoveryStatus.INPROGRESS);
                deviceDiscoverEntity.setMarshalledDeviceDiscoverData(xmlRequest);

                getDiscoverRequestDAO().createDeviceDiscover(deviceDiscoverEntity);
            } catch (AsmManagerCheckedException e) {
                // DB update failed...
                logger.error("Error in creating device discovery request in database", e);
            }

            getLogService().logMsg(AsmManagerMessages.discoveryJobCompleted(jobName), LogMessage.LogSeverity.INFO,
                    LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);


        } catch (SchedulerException e) {
            logger.error("SchedulerException: device discovery job " + jobName + " failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.discoveryJobFailed(jobName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        } catch (Exception e) {
            logger.error("Exception: device discovery job " + jobName + " failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.discoveryJobFailed(jobName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public LocalizableMessageService getLogService() {
        if (logService == null) {
            logService = LocalizableMessageService.getInstance();
        }
        return logService;
    }

    public void setLogService(LocalizableMessageService logService) {
        this.logService = logService;
    }

    public DeviceDiscoverDAO getDiscoverRequestDAO() {
        if (discoverRequestDAO == null) {
            discoverRequestDAO = DeviceDiscoverDAO.getInstance();
        }
        return discoverRequestDAO;
    }

    public void setDiscoverRequestDAO(DeviceDiscoverDAO discoverRequestDAO) {
        this.discoverRequestDAO = discoverRequestDAO;
    }

    public DiscoveryResultDAO getDiscoveryResultDAO() {
        if (discoveryResultDAO == null) {
            discoveryResultDAO = DiscoveryResultDAO.getInstance();
        }
        return discoveryResultDAO;
    }

    public void setDiscoveryResultDAO(DiscoveryResultDAO discoveryResultDAO) {
        this.discoveryResultDAO = discoveryResultDAO;
    }

    public DeviceInventoryUtils getDeviceInventoryUtils() {
        if (deviceInventoryUtils == null) {
            deviceInventoryUtils = new DeviceInventoryUtils();
        }
        return deviceInventoryUtils;
    }

    public void setDeviceInventoryUtils(DeviceInventoryUtils deviceInventoryUtils) {
        this.deviceInventoryUtils = deviceInventoryUtils;
    }

}
