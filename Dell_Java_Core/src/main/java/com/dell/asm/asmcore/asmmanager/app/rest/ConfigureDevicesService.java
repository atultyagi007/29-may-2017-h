package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.asmcore.asmmanager.client.configure.ConfigurableIOM;
import com.dell.asm.asmcore.asmmanager.client.configure.ConfigurationResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.configure.AddressingMode;
import com.dell.asm.asmcore.asmmanager.client.configure.ConfigurationRequest;
import com.dell.asm.asmcore.asmmanager.client.configure.IConfigureDevicesService;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryResult;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.BaseDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.tasks.DeviceConfigurationJob;
import com.dell.asm.asmcore.asmmanager.tasks.InitialConfigurationJob;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IOM;
import com.dell.pg.asm.chassis.client.device.Server;
import com.dell.pg.asm.identitypoolmgr.common.IdentityPoolMgrMessageCode;
import com.dell.pg.asm.identitypoolmgr.network.IIPAddressPoolMgr;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.PingUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;

@Path("/Configure")
public class ConfigureDevicesService implements IConfigureDevicesService {

    private static final Logger logger = Logger.getLogger(ConfigureDevicesService.class);
    private static final int RESERVATION_PERIOD = 10;

    // For Audit log message creation
    private static LocalizableMessageService logService = LocalizableMessageService.getInstance();

    private PingUtil pingUtil;

    public ConfigureDevicesService() {
        pingUtil = new PingUtil();
    }

    /**
     * Reserve IPs, NOT assign. This will create a configuration but not start the job yet.
     *
     * @param requests
     * @return
     */
    @Override
    public Response processInitialConfiguration(ConfigurationRequest requests) {
        Chassis chassisFromChassisRA = null;
        com.dell.pg.asm.server.client.device.Server serverFromServerRA = null;

        if (requests.getDevices() != null) {
            for (String deviceId : requests.getDevices()) {
                try {
                    chassisFromChassisRA = ProxyUtil.getDeviceChassisProxy().getChassis(deviceId);
                }catch(WebApplicationException nfe1) {
                    try {
                        serverFromServerRA = ProxyUtil.getDeviceServerProxy().getServer(deviceId);
                    }catch(WebApplicationException nfe2) {
                        logger.error("Cannot find neither chassis nor rack by refId: " + deviceId);
                        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                    }
                }

                if (chassisFromChassisRA != null) {
                    createConfigurationTemplate(chassisFromChassisRA, requests, false);
                }else{
                    createServerConfigurationTemplate(serverFromServerRA, requests, false);
                }
            }
        }

        return Response.noContent().build();
    }

    /**
     * Pick IP, check if available, assign. Device ID is actually a service tag here.
     *
     * @param network
     * @param deviceId
     * @param ipAddressPoolMgr
     * @return
     * @throws WebApplicationException
     */
    public static Network assignIP(com.dell.pg.asm.identitypool.api.network.model.Network network,
                             String deviceId, PingUtil pingUtil,
                             IIPAddressPoolMgr ipAddressPoolMgr, boolean permanent) throws WebApplicationException {
        try {

            if (network.isStatic()) {
                Network ret = new Network(network);

                String ip = null;
                ip = ipAddressPoolMgr.reserveIPAddresses(network.getId(), deviceId, Calendar.MINUTE, RESERVATION_PERIOD);
                final Integer[] portsToPing =  AsmManagerApp.getPortsToPing();
                while (pingUtil.isReachable(ip, portsToPing)) {
                    // do not release! otherwise it will be dead loop in case when IP is taken

                    logService.logMsg(AsmManagerMessages.ipAddressInUse(ip),
                                      LogMessage.LogSeverity.WARNING,
                                      LogMessage.LogCategory.DEPLOYMENT);

                    ip = ipAddressPoolMgr.reserveIPAddresses(network.getId(), deviceId, Calendar.MINUTE, RESERVATION_PERIOD);
                }
                ret.getStaticNetworkConfiguration().setIpAddress(ip);
                ret.setId(ret.getId() + "-" + deviceId); // make unique network ID by adding device service tag


                if (permanent) {
                    ipAddressPoolMgr.convertReservedToAssigned(deviceId, ret.getStaticNetworkConfiguration().getIpAddress());
                }

                return ret;
            }
            return null;

        } catch (AsmRuntimeException e) {
            logger.error("Exception while reserving IPs.", e);
            if (e.getEEMILocalizableMessage().getDisplayMessage().getMessageCode() == IdentityPoolMgrMessageCode.IPPOOL_ENOUGH_IPADRESS_NOT_AVAILABLE.getCode()||
                    e.getEEMILocalizableMessage().getDisplayMessage().getMessageCode() == IdentityPoolMgrMessageCode.IPPOOL_NO_IPADDRESS_AVAILABLE.getCode()) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.notEnoughIPs(network.getName()));
            } else {
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
            }

        } catch (Exception e) {
            logger.error("Exception while reserving IPs.", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    private static void confirmReservation(ServiceTemplate template) {
        IIPAddressPoolMgr ipAddressPoolMgr = IPAddressPoolMgr.getInstance();

        for (ServiceTemplateComponent component: template.getComponents()) {
            for (ServiceTemplateCategory category: component.getResources()) {
                for (ServiceTemplateSetting setting: category.getParameters()){
                    if (setting.getType().equals(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION)
                            && setting.getNetworks()!=null) {

                        for (Network n: setting.getNetworks()) {
                            if (n.getId()!=null && n.getStaticNetworkConfiguration()!=null) {
                                String[] arr = n.getId().split("-");
                                ipAddressPoolMgr.convertReservedToAssigned(arr[1], n.getStaticNetworkConfiguration().getIpAddress());
                                logger.debug("Converted RESERVED to ASSIGNED for network IP=" + n.getStaticNetworkConfiguration().getIpAddress());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void releaseNetworksById(List<String> assignedNetworks) {
        try {
            if (assignedNetworks != null && assignedNetworks.size() > 0) {
                for (String usageId : assignedNetworks) {
                    String[] arr = usageId.split("-");
                    IPAddressPoolMgr.getInstance().releaseIPAddressesByUsageId(arr[1]);
                }
            }
        } catch (Exception e) {
            logger.error("Release IP addresses by usage ID failed", e);
        }
    }

    /**
     * Actually starts Chassis initial config job.
     * @param requests
     * @return
     */
    @Override
    public ConfigurationResponse configureAndDiscoverChassis(ConfigurationRequest requests) {
        ServiceTemplate configTemplate = null;
        ConfigurationResponse response = new ConfigurationResponse();
        if (requests.getDevices() != null) {
            for (String deviceId : requests.getDevices()) {
                Chassis chassisFromChassisRA = null;
                com.dell.pg.asm.server.client.device.Server serverFromServerRA = null;

                try {
                    chassisFromChassisRA = ProxyUtil.getDeviceChassisProxy().getChassis(deviceId);
                }catch(WebApplicationException nfe1) {
                    try {
                        serverFromServerRA = ProxyUtil.getDeviceServerProxy().getServer(deviceId);
                    }catch(WebApplicationException nfe2) {
                        logger.error("Cannot find neither chassis nor rack by refId: " + deviceId);
                        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                    }
                }

                DiscoveryResultEntity entity = DiscoveryJobUtils.getDiscoveryResultEntity(deviceId);
                if (entity == null || entity.getConfig() == null) {
                    if (chassisFromChassisRA != null) {
                        configTemplate = createConfigurationTemplate(chassisFromChassisRA, requests, true);
                    }else if (serverFromServerRA != null){
                        configTemplate = createServerConfigurationTemplate(serverFromServerRA, requests, true);
                    }else{
                        logger.error("Cannot find neither chassis nor rack by refId: " + deviceId);
                        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                    }
                } else {
                    configTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, entity.getConfig());
                    confirmReservation(configTemplate);
                }

                response.getJobNames().add(initialConfigureChassis(configTemplate));
            }
        }
        return response;
    }

    /**
     * Assumes one device per template.
     * We don't call this end point from ASMUI.
     * @param configTemplate
     * @return
     */
    @Override
    public String initialConfigureChassis(ServiceTemplate configTemplate) {
        String deploymentId = null;
        String tags = "";
        String jobName = "";

        try {

            // it can only have one Chassis or Server component
            if (configTemplate.getComponents() == null) {
                logger.error("Invalid initial configuration template - no components");
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.internalError());
            }

            if (configTemplate.getComponents().size() != 1) {
                logger.error("Invalid initial configuration template - number of components not exactly 1");
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.internalError());
            }

            tags = configTemplate.getComponents().get(0).getComponentID();

            JobDetail job = null;
            IJobManager jm = JobManager.getInstance();
            SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

            job = jm.createNamedJob(InitialConfigurationJob.class);

            DeploymentEntity deploymentEntity = new DeploymentEntity();
            deploymentEntity.setId(UUID.randomUUID().toString());
            deploymentEntity.setName(deploymentEntity.getId());
            deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(configTemplate));
            deploymentEntity.setCreatedDate(new GregorianCalendar());
            deploymentEntity.setCreatedBy(BaseDAO.getInstance().extractUserFromRequest());

            //DeploymentEntity deploymentEntityCreated = DeploymentDAO.getInstance().createDeployment(deploymentEntity);
            deploymentId = deploymentEntity.getId();

         // Return the job name.
            jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
            
            Deployment deployment = new Deployment();
            deployment.setId(jobName);
            deployment.setDeploymentName(jobName);
            deployment.setCreatedBy(deploymentEntity.getCreatedBy());
            deployment.setDeploymentDescription(deploymentEntity.getDeploymentDesc());
            deployment.setCreatedDate(deploymentEntity.getCreatedDate());
            deployment.setCreatedBy(deploymentEntity.getCreatedBy());
            deployment.setUpdatedDate(deploymentEntity.getUpdatedDate());
            deployment.setUpdatedBy(deploymentEntity.getUpdatedBy());
            deployment.setServiceTemplate(configTemplate);
            deployment.setOwner(deploymentEntity.getCreatedBy());

            String jsonData = DeploymentService.toJson(deployment);
            job.getJobDataMap().put(InitialConfigurationJob.ServiceDeploymentJob_SERVICE_KEY_DATA, jsonData);

            // Create a trigger and associate it with the schedule, job,
            // and some arbitrary information. The boolean means "start now".
            Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

            jm.scheduleJob(job, trigger);

            if (configTemplate.getComponents().get(0).hasCMCConfigJob()) {
                logService.logMsg(AsmManagerMessages.startedConfigurationJob(tags, jobName).getDisplayMessage(),
                        LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }else{
                logService.logMsg(AsmManagerMessages.serverConfigurationJobStarted(tags, jobName).getDisplayMessage(),
                        LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            }

        } catch (Exception e) {
            rollbackConfiguration(configTemplate);
            logger.error("Exception while creating deployment job for configuration", e);
            if (StringUtils.isNotEmpty(deploymentId)) {
                ProxyUtil.getDeploymentProxy().deleteDeployment(deploymentId);
            }
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
        return jobName;
    }

    @Override
    public String configureChassis(ServiceTemplate configTemplate) {
        String deploymentId = null;
        String tags = "";
        String jobName = "";
        try {

            for (ServiceTemplateComponent component : configTemplate.getComponents()) {
                if (DeviceType.isChassis(component.getPuppetCertName())) {
                    if (tags.length() > 0)
                        tags += ",";
                    tags += component.getComponentID();
                }
            }

            if (tags.length()==0) {
                logger.debug("No chassis found in configuration request.");
                return "";
            }

            JobDetail job = null;
            IJobManager jm = JobManager.getInstance();
            SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

            job = jm.createNamedJob(DeviceConfigurationJob.class);

            DeploymentEntity deploymentEntity = new DeploymentEntity();
            deploymentEntity.setId(UUID.randomUUID().toString());
            deploymentEntity.setName(deploymentEntity.getId());
            deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(configTemplate));
            deploymentEntity.setCreatedDate(new GregorianCalendar());
            deploymentEntity.setCreatedBy(BaseDAO.getInstance().extractUserFromRequest());

            //DeploymentEntity deploymentEntityCreated = DeploymentDAO.getInstance().createDeployment(deploymentEntity);
            deploymentId = deploymentEntity.getId();
            
            // Return the job name.
            jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);

            Deployment deployment = new Deployment();
            deployment.setId(jobName);
            deployment.setDeploymentName(jobName);
            deployment.setCreatedBy(deploymentEntity.getCreatedBy());
            deployment.setDeploymentDescription(deploymentEntity.getDeploymentDesc());
            deployment.setCreatedDate(deploymentEntity.getCreatedDate());
            deployment.setCreatedBy(deploymentEntity.getCreatedBy());
            deployment.setUpdatedDate(deploymentEntity.getUpdatedDate());
            deployment.setUpdatedBy(deploymentEntity.getUpdatedBy());
            deployment.setServiceTemplate(configTemplate);
            deployment.setOwner(deploymentEntity.getCreatedBy());

            String jsonData = DeploymentService.toJson(deployment);
            job.getJobDataMap().put(DeviceConfigurationJob.ServiceDeploymentJob_SERVICE_KEY_DATA, jsonData);

            // Create a trigger and associate it with the schedule, job,
            // and some arbitrary information. The boolean means "start now".
            Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

            jm.scheduleJob(job, trigger);

            deploymentEntity.setJobId(jobName);
            //DeploymentDAO.getInstance().updateDeployment(deploymentEntity);

            logService.logMsg(AsmManagerMessages.startedConfigurationJob(tags, jobName).getDisplayMessage(),
                    LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
        } catch (Exception e) {
            logger.error("Exception while creating deployment job for configuration", e);
            if (StringUtils.isNotEmpty(deploymentId)) {
                ProxyUtil.getDeploymentProxy().deleteDeployment(deploymentId);
            }
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
        return jobName;
    }

    /**
     * Release IP addresses and rollback other config related changes.
     *
     * @param configTemplate
     */
    public static void rollbackConfiguration(ServiceTemplate configTemplate) {
        if (configTemplate == null) return;

        List<String> assignedNetworks = new ArrayList<>();

        for (ServiceTemplateComponent component : configTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION) {
                for (ServiceTemplateCategory category : component.getResources()) {
                    for (ServiceTemplateSetting setting : category.getParameters()) {
                        if (setting.getNetworks() != null && setting.getNetworks().size() > 0) {
                            for (Network network : setting.getNetworks()) {
                                assignedNetworks.add(network.getId()); // ID here would be device service tag
                            }
                        }
                    }
                }
                DiscoveryResultEntity entity = DiscoveryJobUtils.getDiscoveryResultEntity(component.getId());
                if (entity != null) {
                    ProxyUtil.getDiscoveryProxy().deleteDiscoveryResult(entity.getJobId());
                }
            }
        }

        releaseNetworksById(assignedNetworks);
    }

    /**
     * Create a ServiceTemplate for each chassis included in request. Store template with discoveryresults.
     *
     * @param requests
     * @return
     * @throws LocalizedWebApplicationException
     */
    private ServiceTemplate createConfigurationTemplate(Chassis chassisFromChassisRA, ConfigurationRequest requests, boolean assignMode)
            throws LocalizedWebApplicationException {
        ServiceTemplateSetting setting = null;
        List<String> assignedNetworks = new ArrayList<>();
        com.dell.pg.asm.identitypool.api.network.model.Network chassisNet = null;
        com.dell.pg.asm.identitypool.api.network.model.Network serverNet = null;
        com.dell.pg.asm.identitypool.api.network.model.Network iomNet = null;

        try {

            if (requests.getConfiguration().getChassisNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                if (requests.getConfiguration().getChassisNetworkIdentity().getNetworkId() == null) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.networkIdMissed());
                }

                chassisNet = ProxyUtil.getNetworkProxy().getNetwork(requests.getConfiguration().getChassisNetworkIdentity().getNetworkId());
            }

            Network aNet = null;

            ServiceTemplateSetting cmcNetworkssetting = null;
            ServiceTemplateSetting idracNetworkssetting = null;
            ServiceTemplateSetting iomNetworkssetting = null;
            ServiceTemplateSetting iomSlots = null;
            ServiceTemplateSetting idracSlots = null;

            ServiceTemplate configuration = new ServiceTemplate();

            DiscoveryResult result = DiscoveryJobUtils.getDiscoveryResult(chassisFromChassisRA.getRefId());

            ServiceTemplateComponent comp = new ServiceTemplateComponent();
            configuration.getComponents().add(comp);
            comp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
            comp.setPuppetCertName(PuppetModuleUtil.toCertificateName(result.getDiscoverDeviceType(),result.getDeviceType(), chassisFromChassisRA.getServiceTag()));
            comp.setIP(chassisFromChassisRA.getManagementIP());
            comp.setId(chassisFromChassisRA.getRefId());
            comp.setComponentID(chassisFromChassisRA.getServiceTag());
            comp.setName(chassisFromChassisRA.getServiceTag());


            ServiceTemplateCategory category = new ServiceTemplateCategory();

            category.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID);
            comp.getResources().add(category);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_CRED);
            setting.setValue(requests.getConfiguration().getChassisCredentialId());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED);
            setting.setValue(requests.getConfiguration().getBladeCredentialId());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_CRED);
            setting.setValue(requests.getConfiguration().getIomCredentialId());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            // reserve IP
            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORK_TYPE);
            setting.setValue(requests.getConfiguration().getChassisNetworkIdentity().getAddressingMode().getValue());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            cmcNetworkssetting = new ServiceTemplateSetting();
            cmcNetworkssetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORKS);
            cmcNetworkssetting.setValue("");
            cmcNetworkssetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
            category.getParameters().add(cmcNetworkssetting);
            cmcNetworkssetting.setNetworks(new ArrayList<Network>());

            if (requests.getConfiguration().getChassisNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                aNet = assignIP(chassisNet, chassisFromChassisRA.getServiceTag(), pingUtil, IPAddressPoolMgr.getInstance(), assignMode);
                if (aNet != null) {
                    assignedNetworks.add(chassisFromChassisRA.getServiceTag());
                    cmcNetworkssetting.getNetworks().add(aNet);
                }
            }

            setting = new ServiceTemplateSetting();
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORK_TYPE);
            setting.setValue(requests.getConfiguration().getBladeNetworkIdentity().getAddressingMode().getValue());
            category.getParameters().add(setting);

            idracNetworkssetting = new ServiceTemplateSetting();
            idracNetworkssetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
            idracNetworkssetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORKS);
            idracNetworkssetting.setValue("");
            category.getParameters().add(idracNetworkssetting);
            idracNetworkssetting.setNetworks(new ArrayList<Network>());

            idracSlots = new ServiceTemplateSetting();
            idracSlots.setType(ServiceTemplateSetting.ServiceTemplateSettingType.LIST);
            idracSlots.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_SLOTS);
            category.getParameters().add(idracSlots);
            StringBuilder slots = new StringBuilder();

            if (requests.getConfiguration().getBladeNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                if (requests.getConfiguration().getBladeNetworkIdentity().getNetworkId() == null)
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.networkIdMissed());
                serverNet = ProxyUtil.getNetworkProxy().getNetwork(requests.getConfiguration().getBladeNetworkIdentity().getNetworkId());
            }

            for (Server server : chassisFromChassisRA.getServers()) {
                if (slots.length() > 0)
                    slots.append(',');
                slots.append(server.getSlot());

                if (requests.getConfiguration().getBladeNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                    aNet = assignIP(serverNet, server.getServiceTag(), pingUtil, IPAddressPoolMgr.getInstance(), assignMode);
                    if (aNet != null) {
                        assignedNetworks.add(server.getServiceTag());
                        idracNetworkssetting.getNetworks().add(aNet);
                    }
                }
            }
            idracSlots.setValue(slots.toString());


            setting = new ServiceTemplateSetting();
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORK_TYPE);
            setting.setValue(requests.getConfiguration().getIomNetworkIdentity().getAddressingMode().getValue());
            category.getParameters().add(setting);

            iomNetworkssetting = new ServiceTemplateSetting();
            iomNetworkssetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
            iomNetworkssetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORKS);
            iomNetworkssetting.setValue("");
            category.getParameters().add(iomNetworkssetting);
            iomNetworkssetting.setNetworks(new ArrayList<Network>());

            iomSlots = new ServiceTemplateSetting();
            iomSlots.setType(ServiceTemplateSetting.ServiceTemplateSettingType.LIST);
            iomSlots.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_SLOTS);
            category.getParameters().add(iomSlots);
            slots = new StringBuilder();

            if (requests.getConfiguration().getIomNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                if (requests.getConfiguration().getIomNetworkIdentity().getNetworkId() == null)
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.networkIdMissed());

                iomNet = ProxyUtil.getNetworkProxy().getNetwork(requests.getConfiguration().getIomNetworkIdentity().getNetworkId());
            }

            for (IOM iom : chassisFromChassisRA.getIOMs()) {
                if (!isIOMConfigurable(iom)) continue;

                if (slots.length() > 0)
                    slots.append(',');
                slots.append(iom.getSlot());

                if (requests.getConfiguration().getIomNetworkIdentity().getAddressingMode() == AddressingMode.Static) {

                    aNet = assignIP(iomNet, iom.getServiceTag(), pingUtil, IPAddressPoolMgr.getInstance(), assignMode);
                    if (aNet != null) {
                        assignedNetworks.add(iom.getServiceTag());
                        iomNetworkssetting.getNetworks().add(aNet);
                    }
                }
            }
            iomSlots.setValue(slots.toString());

            String title = comp.getPuppetCertName();
            ServiceTemplateSetting titleParam = new ServiceTemplateSetting();
            titleParam.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
            titleParam.setValue(title);
            titleParam.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            titleParam.setRequired(false);
            titleParam.setHideFromTemplate(true);
            category.getParameters().add(titleParam);

            String configXml = MarshalUtil.marshal(configuration);
            DiscoveryResultEntity entity = DiscoveryJobUtils.getDiscoveryResultEntity(chassisFromChassisRA.getRefId());
            entity.setConfig(configXml);
            DiscoveryResultDAO.getInstance().createOrUpdateDiscoveryResult(entity);

            return configuration;

        } catch (LocalizedWebApplicationException wex) {
            releaseNetworksById(assignedNetworks);
            // pass through our exceptions
            throw wex;
        } catch (Exception e) {
            releaseNetworksById(assignedNetworks);
            logger.error("processConfiguration failed", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    private boolean isIOMConfigurable(IOM iom) {
         return ConfigurableIOM.containsModel(iom.getModel());
    }

    /**
     * Create a ServiceTemplate for each server included in request. Store template with discoveryresults.
     *
     * @param requests
     * @return
     * @throws LocalizedWebApplicationException
     */
    private ServiceTemplate createServerConfigurationTemplate(com.dell.pg.asm.server.client.device.Server server, ConfigurationRequest requests, boolean assignMode)
            throws LocalizedWebApplicationException {
        ServiceTemplateSetting setting = null;
        List<String> assignedNetworks = new ArrayList<>();
        com.dell.pg.asm.identitypool.api.network.model.Network serverNet = null;

        try {

            if (requests.getConfiguration().getServerNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                if (requests.getConfiguration().getServerNetworkIdentity().getNetworkId() == null) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.networkIdMissed());
                }

                serverNet = ProxyUtil.getNetworkProxy().getNetwork(requests.getConfiguration().getServerNetworkIdentity().getNetworkId());
            }


            ServiceTemplate configuration = new ServiceTemplate();

            DiscoveryResult result = DiscoveryJobUtils.getDiscoveryResult(server.getRefId());

            ServiceTemplateComponent comp = new ServiceTemplateComponent();
            configuration.getComponents().add(comp);
            comp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
            comp.setPuppetCertName(PuppetModuleUtil.toCertificateName(result.getDiscoverDeviceType(),result.getDeviceType(),server.getServiceTag()));
            comp.setIP(server.getManagementIP());
            comp.setId(server.getRefId());
            comp.setComponentID(server.getServiceTag());
            comp.setName(server.getServiceTag());

            ServiceTemplateCategory category = new ServiceTemplateCategory();

            category.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_SERVER_ID);
            comp.getResources().add(category);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED);
            setting.setValue(requests.getConfiguration().getServerCredentialId());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            // reserve IP
            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORK_TYPE);
            setting.setValue(requests.getConfiguration().getServerNetworkIdentity().getAddressingMode().getValue());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORKS);
            setting.setValue("");
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
            category.getParameters().add(setting);
            setting.setNetworks(new ArrayList<Network>());

            if (requests.getConfiguration().getServerNetworkIdentity().getAddressingMode() == AddressingMode.Static) {
                Network aNet = assignIP(serverNet, server.getServiceTag(), pingUtil, IPAddressPoolMgr.getInstance(), assignMode);
                if (aNet != null) {
                    assignedNetworks.add(server.getServiceTag());
                    setting.getNetworks().add(aNet);
                }
            }

            String title = comp.getPuppetCertName();
            ServiceTemplateSetting titleParam = new ServiceTemplateSetting();
            titleParam.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
            titleParam.setValue(title);
            titleParam.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            titleParam.setRequired(false);
            titleParam.setHideFromTemplate(true);
            category.getParameters().add(titleParam);

            String configXml = MarshalUtil.marshal(configuration);
            DiscoveryResultEntity entity = DiscoveryJobUtils.getDiscoveryResultEntity(server.getRefId());
            if (entity != null) {
                // initialize children for hibernate
                entity.getFirmwareList().clear();
                entity.setConfig(configXml);
                DiscoveryResultDAO.getInstance().createOrUpdateDiscoveryResult(entity);
            }
            return configuration;

        } catch (LocalizedWebApplicationException wex) {
            releaseNetworksById(assignedNetworks);
            // pass through our exceptions
            throw wex;
        } catch (Exception e) {
            releaseNetworksById(assignedNetworks);
            logger.error("processConfiguration failed", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

}