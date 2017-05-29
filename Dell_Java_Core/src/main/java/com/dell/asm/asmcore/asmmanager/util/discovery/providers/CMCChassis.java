/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.ConfigureDevicesService;
import com.dell.asm.asmcore.asmmanager.app.rest.DeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.client.configure.AddressingMode;
import com.dell.asm.asmcore.asmmanager.client.configure.ConfigurableIOM;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.tasks.DiscoverIpRangeJob;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.pg.asm.chassis.client.ClientUtils;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IChassisService;
import com.dell.pg.asm.chassis.client.device.IOM;
import com.dell.pg.asm.chassis.client.device.Server;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.asm.server.client.device.IServerDeviceService;
import com.dell.pg.jraf.client.jobmgr.JrafJobExecStatus;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.PingUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

/**
 * The Dell Chassis Management Controller (CMC) is a systems management component designed to manage one or more Dell 
 * PowerEdge systems containing blade servers. Unlike a tower or rack server, a blade server cannot run by itself; it 
 * requires a compatible blade enclosure. What makes it compelling to many customers is that blade servers are 
 * typically optimized to minimize the use of physical space and energy while still providing ample computing power.
 * <br><br>
 * The blade enclosure, which can hold multiple blade servers, provides power, cooling, networking, various 
 * interconnects and additional systems management capabilities. <br>
 * <br>
 * It is worth pointing out that the term 'blade' or 'blades' is largely interchangeable with the term 'server node' 
 * when referring to the Dell PowerEdge VRTX platform, though the blades themselves are not interchangeable between 
 * different types of enclosures. Thus a server node from Dell PowerEdge VRTX is not physically compatible with the 
 * M1000e due to slightly different physical connections. Regardless of enclosure, Dell's CMC, a hot-pluggable module 
 * within a Dell blade platform like the M1000e or the previously mentioned VRTX, provides a secure browser-based 
 * interface that enables an IT administrator to take inventory, perform configuration and monitoring tasks, remotely 
 * power on/off blade servers and enable alerts for events on servers and components in the blade chassis. This ability 
 * is embedded into the module and requires no additional software installations.<br>
 * <br>
 * One of CMC's more interesting features is multi-chassis management. This capability (which was introduced in CMC 
 * version 3.1) can monitor up to 9 fully loaded chassis with no additional cabling via a single web interface.<br>
 * <br>
 * The CMC interface integrates with each blade or server node's iDRAC module, so administrators can perform 
 * server-specific iDRAC functions such as performing updates, changing settings, or opening a remote console session 
 * from the CMC interface. Click here for more information on iDRAC-related management.<br>
 * <br>
 * Additionally, CMC allows you to back up and replicate settings on the chassis, and save or apply BIOS profiles for 
 * individual blade servers so that adding new blades or chassis to your environment is easier and more automated. With 
 * newer versions of CMC (4.4), it is even possible to assign settings to an empty slot, so that the settings will be 
 * applied when a blade is inserted at some point in the future!<br>
 * <br>
 * You can also capture a complete Chassis Inventory across all of your chassis that will return detailed information 
 * on all of the blades, IO modules, iDRAC cards, etc in your environment.<br>
 * <br>
 * For more information on CMC please visit
 * <a href="http://en.community.dell.com/techcenter/systems-management/w/wiki/1987.dell-chassis-management-controller">
 * this site</a>
 * 
 */
public class CMCChassis extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(CMCChassis.class);

    private PingUtil pingUtil = new PingUtil();
    private DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();
    private DeviceInventoryUtils deviceInventoryUtils;

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setServerPoolId(device.getServerPoolId());
        result.setUnmanaged(device.isUnmanaged());
        result.setReserved(device.isReserved());
        result.setConfig(device.getConfig());
        result.setDiscoverDeviceType(this.getDiscoverDeviceType());
        result.setIpAddress(device.getIpAddress());
        result.setParentJobId(device.getParentJob()); // must set, not-null
        result.setRefType(ClientUtils.DISCOVERY_IP_REF_TYPE);
        result.setDeviceType(getInventoryDeviceType());
        result.setServerType("");

        setFirmwareComponentIDSettable(true);
        //genericPuppetInvDetails(fwPuppetInv, result);

        if (factLabelToValue != null) {
            String refId = factLabelToValue.get("refId");
            if (StringUtils.isNotEmpty(refId)) {
                processSuccess(refId);
            }
        }

        if (StringUtils.isEmpty(result.getDeviceRefId())){
            result.setStatus(DiscoveryStatus.ERROR);
            EEMILocalizableMessage eemiMessage = AsmManagerMessages.discoveryServiceException(device.getIpAddress());
            result.setStatusMessage(eemiMessage.getDisplayMessage().localize());
            logger.warn("error discovering device with IP:" + device.getIpAddress() +", no facts returned by asm-deployer");
            DiscoveryJobUtils.updateDiscoveryResult(result);
        }
    }

    private void processSuccess(String deviceRefId) {

        Chassis chassis = ProxyUtil.getDeviceChassisProxyWithHeaderSet().getChassis(deviceRefId);
        result.setDeviceRefId(deviceRefId);
        result.setStatus(DiscoveryStatus.CONNECTED);
        result.setModel(chassis.getModel());
        result.setServiceTag(chassis.getServiceTag());
        logger.info("chassis servercount: " + chassis.getServerCount());
        result.setServerCount(chassis.getServerCount());
        result.setIomCount(chassis.getIomCount());
        result.setServerType("");
        result.setVendor(ASMCommonsUtils.VENDOR_NAME_DELL);
        result.setHealthState(chassis.getHealth().value());
        result.setDisplayName(chassis.getName());

        Set<FirmwareDeviceInventoryEntity> currentFirmwareInventory;
        currentFirmwareInventory = DiscoveryJobUtils.firmwareChassisDeviceEntity(chassis, result);
        logger.info("Dell: chassis serviceTag: " + chassis.getServiceTag() + " IOMcount: " + chassis.getIomCount() + " :entity value:" + result.getIomCount());

        try {
            if (!device.isQuickDiscovery())
                createAndScheduleJobToDiscoverBladesIOAs(chassis, result);
        } catch (LocalizedWebApplicationException wex) {
            String msg = "Failed to discover blades/IOAs for chassis " + chassis.getServiceTag() + " because of " + wex.getLocalizedMessage();
            logger.error(msg, wex);
            LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError(msg), LogMessage.LogSeverity.ERROR,
                    LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
        } catch (Exception e) {
            String msg = "Failed to discover blades/IOAs for chassis " + chassis.getServiceTag() + " because of " + e.getMessage();
            logger.error(msg, e);
            LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError(msg), LogMessage.LogSeverity.ERROR,
                    LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
        }

        if (!device.isQuickDiscovery())
            DiscoveryJobUtils.updateFirmwareDiscoveryResult(result, currentFirmwareInventory);
        else {
            // for quick discovery do not touch firmware or it might mess up with existing chassis
            DiscoveryJobUtils.updateDiscoveryResult(result);
        }

    }

    private void createAndScheduleJobToDiscoverBladesIOAs( Chassis chassis, DiscoveredDevices result)
    {

        DiscoveryRequest dReq = new DiscoveryRequest();

        DiscoverIPRangeDeviceRequests requests = new DiscoverIPRangeDeviceRequests();
        Set<DiscoverIPRangeDeviceRequest> reqs = requests.getDiscoverIpRangeDeviceRequests();
        DiscoverIPRangeDeviceRequest req;

        JobDetail job;
        String jobName = "";

        // Add Servers to the job.
        if (device.isFromInventoryJob()) {
            // get credentials from initial config
            DeviceInventoryEntity entity = this.deviceInventoryDAO.getDeviceInventory(chassis.getRefId());
            if (entity!=null) {
                device.setServerCredentialId(getDeviceInventoryUtils().findBladeCredentialFromChassis(entity));
                device.setSwitchCredentiallId(getDeviceInventoryUtils().findIOMCredentialFromChassis(entity));
            }
        }

        List<Server> newServers = new ArrayList<>();
        List<com.dell.pg.asm.chassis.client.device.IOM> newIoms = new ArrayList<>();

        logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Check for Blades for Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
        if (chassis.getServers() != null && device.getServerCredentialId() != null && device.getServerCredentialId().length() > 0) {

            logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Attempting Blades on the Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
            for (com.dell.pg.asm.chassis.client.device.Server server : chassis.getServers()) {
                // find server in inventory
                if (device.isFromInventoryJob() && !device.isFromInitialConfigJob() &&
                        getDeviceInventoryUtils().findDeviceInventoryByServiceTag(server.getServiceTag()) == null) {
                    newServers.add(server);
                    continue;
                }

                if(StringUtils.isNotBlank(server.getManagementIP()) && !StringUtils.equals("0.0.0.0", server.getManagementIP())) {
                    req = new DiscoverIPRangeDeviceRequest();
                    req.setDeviceStartIp(server.getManagementIP());
                    req.setDeviceServerCredRef(device.getServerCredentialId());
                    req.setDeviceType(DeviceType.Server);
                    req.setConfig(device.getConfig());
                    req.setUnmanaged(result.isUnmanaged());
                    req.setReserved(result.isReserved());
                    req.setServerPoolId(device.getServerPoolId());
                    logger.info("createAndScheduleJobToDiscoverBladesIOAs: Adding Blade: IP Address: " + server.getManagementIP() + " on the Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
                    reqs.add(req);
                }
            }
        }

        // Add IOMs to the job.
        logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Check for IOMs for Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
        if(CollectionUtils.isNotEmpty(chassis.getIOMs()) && StringUtils.isNotEmpty(device.getSwitchCredentiallId())) {

            logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Attempting IOMs on the Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
            for(com.dell.pg.asm.chassis.client.device.IOM iom : chassis.getIOMs()) {
                // find IOM in inventory
                DeviceInventoryEntity asmIOM = getDeviceInventoryUtils().findDeviceInventoryByServiceTag(iom.getServiceTag());

                if (device.isFromInventoryJob() && !device.isFromInitialConfigJob() && asmIOM == null) {
                    newIoms.add(iom);
                    continue;
                }

                // not all IOMs have valid credentials in chassis. Exclude those to eliminate errors in logs
                if(StringUtils.isNotBlank(iom.getManagementIP()) && !StringUtils.equals("0.0.0.0", iom.getManagementIP())
                        && (asmIOM != null && getDeviceInventoryUtils().isChassisDiscoveryDevice(asmIOM)) || asmIOM == null) {
                    req = new DiscoverIPRangeDeviceRequest();
                    req.setDeviceStartIp(iom.getManagementIP());
                    req.setDeviceSwitchCredRef(device.getSwitchCredentiallId());
                    req.setDeviceType(DeviceType.genericswitch);
                    req.setConfig(device.getConfig());
                    req.setUnmanaged(result.isUnmanaged());
                    req.setReserved(result.isReserved());
                    logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Adding IOM: IP Address: " + iom.getManagementIP() + " on the Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
                    reqs.add(req);
                }
            }
        }


        // for new servers and ioms start initial config job
        if (device.isFromInventoryJob() && !device.isFromInitialConfigJob() && (newServers.size()>0 || newIoms.size()>0)) {
            if (applyInitialConfiguration(chassis, newServers, newIoms)) {
                // should continue with other devices, because configuration job may fail and devices won't be updated
                if (reqs.size()==0)
                    return;
            }else{
                // add servers and ioms to discovery requests
                for (com.dell.pg.asm.chassis.client.device.Server server : newServers) {
                    req = new DiscoverIPRangeDeviceRequest();
                    req.setDeviceStartIp(server.getManagementIP());
                    req.setDeviceServerCredRef(device.getServerCredentialId());
                    req.setDeviceType(DeviceType.Server);
                    req.setConfig(device.getConfig());
                    req.setUnmanaged(result.isUnmanaged());
                    req.setReserved(result.isReserved());

                    req.setServerPoolId(device.getServerPoolId());
                    logger.info("createAndScheduleJobToDiscoverBladesIOAs: Adding New Blade: IP Address: " + server.getManagementIP() + " on the Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
                    reqs.add(req);
                }
                for (com.dell.pg.asm.chassis.client.device.IOM iom : newIoms) {
                    req = new DiscoverIPRangeDeviceRequest();
                    req.setDeviceStartIp(iom.getManagementIP());
                    req.setDeviceSwitchCredRef(device.getSwitchCredentiallId());
                    req.setDeviceType(DeviceType.genericswitch);
                    req.setConfig(device.getConfig());
                    req.setUnmanaged(result.isUnmanaged());
                    req.setReserved(result.isReserved());

                    logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Adding New IOM: IP Address: " + iom.getManagementIP() + " on the Chassis: Servicetag: " + chassis.getServiceTag() + " IP Address: " + chassis.getManagementIP());
                    reqs.add(req);
                }
            }
        }

        dReq.setDiscoveryRequestList(requests);
        if( reqs.size() != 0) {

            DiscoverIPRangeDeviceRequests discoveryRequestList = dReq.getDiscoveryRequestList();

            IJobManager jm = JobManager.getInstance();
            SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

            job = jm.createNamedJob(DiscoverIpRangeJob.class);

            String xmlRequest = MarshalUtil.marshal(dReq);
            String xmlData = MarshalUtil.marshal(discoveryRequestList);
            logger.debug("createAndScheduleJobToDiscoverBladesIOAs: XML Data: " + xmlData);
            job.getJobDataMap().put(DiscoverIpRangeJob.DISCOVERIPRANGE_SERVICE_KEY_DATA, xmlData);
            job.getJobDataMap().put(DiscoverIpRangeJob.REQUEST_FROM_INVENTORY_JOB, device.isFromInventoryJob() ? "true" : "false");
            job.getJobDataMap().put(DiscoverIpRangeJob.REQUEST_FROM_INITIAL_CONFIG_JOB, device.isFromInitialConfigJob() ? "true" : "false");
            job.getJobDataMap().put(DiscoverIpRangeJob.DISCOVER_IP_RANGE_PARENT_DEVICE_TYPE, getDiscoverDeviceType().name());
            job.getJobDataMap().put(DiscoverIpRangeJob.DISCOVER_IP_RANGE_PARENT_DEVICE_ID, chassis.getRefId());

            // Create a trigger and associate it with the schedule, job,
            // and some arbitrary information. The boolean means "start now".
            Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

            // Schedule our job using our trigger.
            try {
                jm.scheduleJob(job, trigger);
                if (!jm.getScheduler().isStarted()) {
                    jm.getScheduler().start();
                }
                // Return the job name.
                jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
                logger.debug("createAndScheduleJobToDiscoverBladesIOAs: Job Name/Id: " + jobName);
                dReq.setId(jobName);
                dReq.setStatus(DiscoveryStatus.INPROGRESS);

                DeviceDiscoverEntity discoverDeviceEntity = null;
                try {
                    DeviceDiscoverEntity deviceDiscoverEntity = new DeviceDiscoverEntity();
                    deviceDiscoverEntity.setId(jobName);
                    deviceDiscoverEntity.setStatus(DiscoveryStatus.INPROGRESS);
                    deviceDiscoverEntity.setMarshalledDeviceDiscoverData(xmlRequest);

                    discoverDeviceEntity = DeviceDiscoverDAO.getInstance().createDeviceDiscover(deviceDiscoverEntity);
                } catch (AsmManagerCheckedException e) {
                    // DB update failed...
                    logger.error("Error in creating device discovery request in database", e);
                }


                // Now wait for this job to finish.
                for (int iter = 0; iter < ProxyUtil.MAX_POLL_ITER; ++iter) {
                    Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                    JrafJobExecStatus status = ProxyUtil.getHistoryProxy().pollExecStatus(jobName);
                    if (!status.isTerminal()) {
                        logger.info("Waiting for blades/IOMs to discover for chassis " + chassis.getServiceTag());
                        continue;
                    } else {
                        break;
                    }
                }

                if (discoverDeviceEntity != null) {
                    DeviceDiscoverDAO.getInstance().deleteDiscoveryResult(discoverDeviceEntity.getId());
                }

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

        if ((device.isFromInventoryJob() || device.isFromInitialConfigJob()) &&
                device.getExistingRefId() != null) {
            compareContentOfChassisWithDeviceInventory(device.getExistingRefId());
        }

    }

    /**
     * Make a template with initial config for the listed servers and ioms, start the job, do NOT wait for completion.
     * Returns true if configuration job was run
     * @param chassis
     * @param newServers
     * @param newIoms
     */
    private boolean applyInitialConfiguration(Chassis chassis, List<com.dell.pg.asm.chassis.client.device.Server> newServers, List<IOM> newIoms) {
        DeviceInventoryEntity chassisEntity = this.deviceInventoryDAO.getDeviceInventory(chassis.getRefId());
        String sConfig = chassisEntity.getConfig();
        if (sConfig == null) {
            // this is a run from inventory, must have config already
            logger.error("Cannot find initial config for chassis: " + chassis.getManagementIP());
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, sConfig);

        return runInitialConfig(template, chassis, newServers, newIoms);
    }


    /**
     * Returns true if configuration job was run
     * @param serviceTemplate
     * @param chassis
     * @param newServers
     * @param newIoms
     * @return
     */
    private boolean runInitialConfig(ServiceTemplate serviceTemplate, Chassis chassis, List<com.dell.pg.asm.chassis.client.device.Server> newServers, List<IOM> newIoms) {
        ServiceTemplate newTemplate = new ServiceTemplate();
        String serverSlots = "";
        for (com.dell.pg.asm.chassis.client.device.Server server : newServers) {
            if (serverSlots.length()>0) {
                serverSlots += ",";
            }
            serverSlots += server.getSlot();
        }

        String iomSlots = "";
        for (IOM iom : newIoms) {
            if (!isIOMConfigurable(iom)) continue;
            if (iomSlots.length()>0) {
                iomSlots += ",";
            }
            iomSlots += iom.getSlot();
        }

        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getId()!=null && component.getId().equals(chassis.getRefId())){
                ServiceTemplateComponent newComp = new ServiceTemplateComponent();
                newTemplate.getComponents().add(newComp);
                newComp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
                newComp.setPuppetCertName(component.getPuppetCertName());
                newComp.setIP(component.getIP());
                newComp.setId(component.getId());
                newComp.setComponentID(component.getComponentID());
                newComp.setName(component.getName());

                for (ServiceTemplateCategory resource: component.getResources()) {
                    if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID)) {

                        boolean idracStatic = false;
                        boolean iomStatic = false;

                        ServiceTemplateCategory newCat = new ServiceTemplateCategory();
                        newCat.setId(resource.getId());
                        newComp.getResources().add(newCat);

                        for (ServiceTemplateSetting oldSet: resource.getParameters()) {
                            if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_CRED) ||
                                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_CRED) ||
                                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED) ||
                                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID)) {
                                newCat.getParameters().add(oldSet);
                            }else if(oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORK_TYPE)) {
                                if (oldSet.getValue().equals(AddressingMode.Static.getValue()))
                                    idracStatic = true;

                                newCat.getParameters().add(oldSet);
                            }else if(oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORK_TYPE)) {
                                if (oldSet.getValue().equals(AddressingMode.Static.getValue()))
                                    iomStatic = true;

                                newCat.getParameters().add(oldSet);
                            }else if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORK_TYPE)) {
                                oldSet.setValue(AddressingMode.Existing.getValue());
                                newCat.getParameters().add(oldSet);
                            }else if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORKS)) {
                                // if netType = static need to grab new IPs
                                if (idracStatic) {
                                    Network aNet = null;
                                    if (oldSet.getNetworks()!=null && oldSet.getNetworks().size()>0) {
                                        String netId = oldSet.getNetworks().get(0).getId();
                                        String[] arr = netId.split("-");
                                        oldSet.getNetworks().clear();

                                        if (arr != null && arr.length > 1) {
                                            com.dell.pg.asm.identitypool.api.network.model.Network serverNet =
                                                    ProxyUtil.getNetworkProxy().getNetwork(arr[0]);

                                            for (com.dell.pg.asm.chassis.client.device.Server server : newServers) {
                                                aNet = ConfigureDevicesService.assignIP(serverNet, server.getServiceTag(), pingUtil,
                                                        IPAddressPoolMgr.getInstance(), true);

                                                if (aNet != null) {
                                                    oldSet.getNetworks().add(aNet);
                                                } else {
                                                    logger.error("Cannot identify server network from initial template. Chassis tag=" + chassis.getServiceTag());
                                                    throw new LocalizedWebApplicationException(
                                                            Response.Status.INTERNAL_SERVER_ERROR,
                                                            AsmManagerMessages.internalError());
                                                }
                                            }
                                        }
                                    }
                                }

                                newCat.getParameters().add(oldSet);
                            }else if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORKS)) {

                                if (iomStatic) {
                                    Network aNet = null;
                                    if (oldSet.getNetworks()!=null && oldSet.getNetworks().size()>0) {
                                        String netId = oldSet.getNetworks().get(0).getId();
                                        String[] arr = netId.split("-");
                                        oldSet.getNetworks().clear();

                                        if (arr != null && arr.length > 1) {
                                            com.dell.pg.asm.identitypool.api.network.model.Network serverNet =
                                                    ProxyUtil.getNetworkProxy().getNetwork(arr[0]);

                                            for (IOM iom : newIoms) {
                                                aNet = ConfigureDevicesService.assignIP(serverNet, iom.getServiceTag(), pingUtil,
                                                        IPAddressPoolMgr.getInstance(), true);

                                                if (aNet != null) {
                                                    oldSet.getNetworks().add(aNet);
                                                } else {
                                                    logger.error("Cannot identify IOM network from initial template. Chassis tag=" + chassis.getServiceTag());
                                                    throw new LocalizedWebApplicationException(
                                                            Response.Status.INTERNAL_SERVER_ERROR,
                                                            AsmManagerMessages.internalError());
                                                }
                                            }
                                        }
                                    }
                                }

                                newCat.getParameters().add(oldSet);
                            }else if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_SLOTS)) {
                                oldSet.setValue(serverSlots);
                                newCat.getParameters().add(oldSet);
                            }else if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_SLOTS)) {
                                oldSet.setValue(iomSlots);
                                newCat.getParameters().add(oldSet);
                            }
                        }

                        logger.debug("Apply initial config for servers in slots= " + serverSlots + " and IOMs in slots = " + iomSlots + " on chassis " + chassis.getManagementIP());
                        String jobName = ProxyUtil.getDeviceConfigurationProxy().initialConfigureChassis(newTemplate);
                        return jobName != null;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Only perform the check for this exact chassis. Do not touch other devices - the inventory might be still in progress!
     * @param chassisId
     */
    private void compareContentOfChassisWithDeviceInventory(String chassisId) {
        logger.debug("Check for inventory integrity started");

        //for server
        List<String> filter = new ArrayList<String>();
        filter.add("eq,deviceType,BladeServer,FXServer");
        filter.add("eq,chassisId," + chassisId);
        FilterParamParser filterParser = new FilterParamParser(filter, DeviceInventoryService.validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();
        //for ioas
        List<String> filterIOA = new ArrayList<String>();
        filterIOA.add("eq,deviceType,dellswitch,genericswitch");
        filterIOA.add("eq,chassisId," + chassisId);
        FilterParamParser filterIOAParser = new FilterParamParser(filterIOA, DeviceInventoryService.validFilterColumns);
        List<FilterParamParser.FilterInfo> filterIOAInfos = filterIOAParser.parse();


        try {
            //get from serverRA inventory as well as there is a huge discrepency in getting servers in chassis RA and serverRA.
            IServerDeviceService serverDeviceProxy = ProxyUtil.getDeviceServerProxyWithHeaderSet();
            List<DeviceInventoryEntity> servers = this.deviceInventoryDAO.getAllDeviceInventory(null, filterInfos, null);
            logger.debug("Validate " + servers.size() + " blades/sleds for chassis presence");
            List<DeviceInventoryEntity> ioas = this.deviceInventoryDAO.getAllDeviceInventory(null, filterIOAInfos, null);
            logger.debug("Validate " + ioas.size() + " IOMs for chassis presence");
            List<String> serviceTagOfDevInvServer = new ArrayList<String>();
            List<String> serviceTagOfChassisServer = new ArrayList<String>();
            List<String> serviceTagOfDevInvIOA = new ArrayList<String>();
            List<String> serviceTagOfChassisIOA = new ArrayList<String>();

            for (DeviceInventoryEntity serverOfDevInv : servers)
                serviceTagOfDevInvServer.add(serverOfDevInv.getServiceTag());
            for (DeviceInventoryEntity ioaOfDevInv : ioas)
                serviceTagOfDevInvIOA.add(ioaOfDevInv.getServiceTag());
            IChassisService chassisDeviceProxy = ProxyUtil.getDeviceChassisProxyWithHeaderSet();


            Chassis chassisSingle = chassisDeviceProxy.getChassis(chassisId);
            if (chassisSingle.getServerCount() > 0) {
                for (com.dell.pg.asm.chassis.client.device.Server serverOfChassis : chassisSingle.getServers())
                    serviceTagOfChassisServer.add(serverOfChassis.getServiceTag());
            }
            if (chassisSingle.getIomCount() > 0) {
                for (IOM iomOfChassis : chassisSingle.getIOMs())
                    serviceTagOfChassisIOA.add(iomOfChassis.getServiceTag());
            }


            // remove servers from device inventory if cant find such in chassis RA: probably blade was removed
            if (!serviceTagOfDevInvServer.isEmpty() && !serviceTagOfChassisServer.isEmpty()) {
                for (String servTag : serviceTagOfDevInvServer) {
                    if (!serviceTagOfChassisServer.contains(servTag)) {
                        // we need to remove this server from device inventory
                        String refId = this.deviceInventoryDAO.getRefIdOfDevice(servTag);
                        if (canDeleteServer(refId)) {
                            IPAddressPoolMgr.getInstance().releaseIPAddressesByUsageId(servTag);
                            this.deviceInventoryDAO.deleteDeviceInventory(refId);
                            LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.deletedServerSuccessfully(servTag), LogMessage.LogSeverity.INFO,
                                    LogMessage.LogCategory.MISCELLANEOUS);
                            logger.debug("Check for inventory integrity: deleted server from inventory , id=" + refId + " service tag=" + servTag);
                        }
                    }
                }
            }

            // remove IOMs from device inventory if cant find such in chassis RA
            if (!serviceTagOfDevInvIOA.isEmpty() && !serviceTagOfChassisIOA.isEmpty()) {
                for (String servTag : serviceTagOfDevInvIOA) {
                    if (!serviceTagOfChassisIOA.contains(servTag)) {
                        // we need to remove the switches from device inventory (only find blade I/O modules, not ToR switches)
                        try {
                            DeviceInventoryEntity dev = this.deviceInventoryDAO.getDeviceInventoryByServiceTag(servTag);

                            if (dev!=null && (dev.getDiscoverDeviceType() == DiscoverDeviceType.FORCE10IOM ||
                                    dev.getDiscoverDeviceType() == DiscoverDeviceType.DELL_IOM_84 ||
                                    dev.getDiscoverDeviceType() == DiscoverDeviceType.FX2_IOM)) {
                                IPAddressPoolMgr.getInstance().releaseIPAddressesByUsageId(servTag);
                                this.deviceInventoryDAO.deleteDeviceInventory(dev.getRefId());
                                logger.debug("Check for inventory integrity: deleted IOM from inventory , id=" + dev.getRefId() + " service tag=" + servTag);
                            }
                        }catch(Exception de) {
                            // not found or can't delete
                            logger.error("Error while assessing IOM for removal: " + servTag, de);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred in compareContentOfChassisWithDeviceInventory method", e);
            throw new AsmRuntimeException(AsmManagerMessages.internalError(), e);
        }
    }

    /**
     * Check if server is not involved in any deployment
     * @param refId
     * @return
     */
    private boolean canDeleteServer(String refId) {
        DeviceInventoryEntity entity = this.deviceInventoryDAO.getDeviceInventory(refId);
        if(entity!=null && entity.getDeployments() != null && entity.getDeployments().size() > 0) {
            logger.error("Refused to delete server: " + entity.getServiceTag() + " as it is currently in use by deployment(s)");
            return false;
        }
        return true;
    }

    private boolean isIOMConfigurable(IOM iom) {
        return ConfigurableIOM.containsModel(iom.getModel());
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
