/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.configure.AddressingMode;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroup;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.IDeviceGroupService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInvalidCredentialException;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ChassisDeviceState;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;
import com.dell.asm.asmcore.asmmanager.util.tasks.JobUtils;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.TagType;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;


/**
 *
 * given an IP or IP range with credentials Discover the list of devices either of Server or Chassis type.
 * Job scheduled by the JRAF core Job Manager
 * @author Bapu_Patil
 *
 */

public class DiscoverIpRangeJob extends ASMInventoryJob {

    private static final String DISCOVER_IP_RANGE_JOB_KEY_NAME = "DiscoverIpRage.JobKey.name";
    private static final String DISCOVER_IP_RANGE_JOB_KEY_GROUP = "DiscoverIpRage.JobKey.group";
    private static final String DISCOVER_IP_RANGE_JOB_STATUS_MSG = "DiscoverIpRage.status.msg";

    public static final String DISCOVERIPRANGE_SERVICE_KEY_DATA = "DiscoverIpRange";
    public static final String REQUEST_FROM_INVENTORY_JOB = "RequestFromInventoryJob";
    public static final String REQUEST_FROM_INITIAL_CONFIG_JOB = "RequestFromInitialConfig";
    public static final String DISCOVER_IP_RANGE_PARENT_DEVICE_TYPE = "DiscoverIpRage.parentDeviceType";
    public static final String DISCOVER_IP_RANGE_PARENT_DEVICE_ID = "DiscoverIpRage.parentDeviceId";

    private static final Logger logger = Logger.getLogger(DiscoverIpRangeJob.class);

    private boolean fromInventoryJob = false;
    private boolean fromInitialConfig = false;
    ThreadPoolExecutor threadPoolExecutor;

    DiscoverIPRangeDeviceRequests discoverIpRangeRequests = null;

    @Override
    protected void executeSafely(JobExecutionContext context){
        initializeFromJobContext(context);

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(DISCOVER_IP_RANGE_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(DISCOVER_IP_RANGE_JOB_KEY_NAME, jobKey.getName());
        String xmlData = context.getJobDetail().getJobDataMap().getString(DISCOVERIPRANGE_SERVICE_KEY_DATA);
        String runByInventory = context.getJobDetail().getJobDataMap().getString(REQUEST_FROM_INVENTORY_JOB);
        String runByInitialConfig = context.getJobDetail().getJobDataMap().getString(REQUEST_FROM_INITIAL_CONFIG_JOB);
        String parentDeviceType = context.getJobDetail().getJobDataMap().getString(DISCOVER_IP_RANGE_PARENT_DEVICE_TYPE);
        String parentDeviceId = context.getJobDetail().getJobDataMap().getString(DISCOVER_IP_RANGE_PARENT_DEVICE_ID);

        fromInventoryJob = (runByInventory!=null && runByInventory.equals("true"));
        fromInitialConfig = (runByInitialConfig!=null && runByInitialConfig.equals("true"));

        logger.info("Discovery data:" + xmlData);
        logger.info("Executing DiscoverIpRangeJob name =" + getJobName() + ", from inventory: " +
                fromInventoryJob + ", from initial config=" + fromInitialConfig);

        getLogService().logMsg(AsmManagerMessages.discoveryJobStarted(getJobName()), LogMessage.LogSeverity.INFO,
                LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

        discoverIpRangeRequests = MarshalUtil.unmarshal(DiscoverIPRangeDeviceRequests.class, xmlData);
        setJobStatus(JobStatus.IN_PROGRESS);

        threadPoolExecutor = DiscoverySingleTonThreadPool.getDiscoverySingleTonThreadPool();

        List<Future<DiscoveredDevices>> list = new ArrayList<>();

        boolean success = true;

        //  for each device check the range and the credentials
        for (DiscoverIPRangeDeviceRequest deviceInfo : discoverIpRangeRequests.getDiscoverIpRangeDeviceRequests())
        {
            try {
                List<String> expandedIpRange = null;
                try {
                    expandedIpRange = DiscoverIPRangeDeviceRequest.expandIpAddresses(deviceInfo);
                }catch(IllegalArgumentException iae) {
                    logger.error("Invalid IP range", iae);
                }

                if (expandedIpRange != null && !expandedIpRange.isEmpty())
                {
                    for (String ipAddress : expandedIpRange)
                    {
                        InfrastructureDevice device = new InfrastructureDevice(ipAddress,
                                deviceInfo.getDeviceChassisCredRef(), deviceInfo.getDeviceServerCredRef(),
                                deviceInfo.getDeviceSwitchCredRef(), deviceInfo.getDeviceVCenterCredRef(),
                                deviceInfo.getDeviceStorageCredRef(), deviceInfo.getDeviceSCVMMCredRef(),
                                deviceInfo.getDeviceEMCredRef());

                        device.setUnmanaged(deviceInfo.isUnmanaged());
                        device.setReserved(deviceInfo.isReserved());
                        device.setServerPoolId(deviceInfo.getServerPoolId());
                        device.setConfig(deviceInfo.getConfig());
                        device.setFromInventoryJob(fromInventoryJob);
                        device.setFromInitialConfigJob(fromInitialConfig);
                        device.setParentDeviceType(DiscoverDeviceType.fromName(parentDeviceType));
                        device.setParentDeviceId(parentDeviceId);

                        // limit discovered device to certain type
                        if (deviceInfo.getDeviceType()!=null) {
                            device.setRequestedDeviceType(deviceInfo.getDeviceType());
                        }

                        DiscoverDeviceCallable worker = new DiscoverDeviceCallable(device, getJobName());

                        Future<DiscoveredDevices> submit = threadPoolExecutor.submit(worker);
                        list.add(submit);
                    }
                }
                else
                {
                    // invalid IP range
                    logger.debug("invalid IP range for starting Ip "
                            + deviceInfo.getDeviceStartIp() + " ending ip"
                            + deviceInfo.getDeviceEndIp());
                    // update job status
                    addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "Invalid IP or range " + deviceInfo.getDeviceStartIp() + " " + deviceInfo.getDeviceEndIp());
                    LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError("Invalid or empty IP range for device " + deviceInfo.getDeviceType()), LogMessage.LogSeverity.ERROR,
                            LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
                }
            }
            catch (AsmManagerInvalidCredentialException ace)
            {
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG + "deviceInfo.getDeviceStartIp()", "Invalid credential for IP or range " + deviceInfo.getDeviceStartIp() + " " + deviceInfo.getDeviceEndIp());
                success = false;
            }
        }

        // wait for discover to finish
        for (Future<DiscoveredDevices> future : list) {
            DiscoveredDevices deviceInfo = null;
            try {
                deviceInfo = future.get();

                if (deviceInfo.getStatus() == DiscoveryStatus.IGNORE) {
                    // skip this device
                    logger.debug("Discovery returned ignorable device at " + deviceInfo.getIpAddress() + ", jobId=" + deviceInfo.getJobId());
                    continue;
                }else if (deviceInfo.getStatus() == DiscoveryStatus.ERROR ||
                        deviceInfo.getStatus() == DiscoveryStatus.FAILED ||
                        deviceInfo.getStatus() == DiscoveryStatus.UNSUPPORTED) {

                    String msg = "Resource IP:" + deviceInfo.getIpAddress();
                    if (StringUtils.isNotEmpty(deviceInfo.getStatusMessage()))
                        msg = deviceInfo.getStatusMessage() + ". " + msg;

                    logger.error("Discovery by IP range failed for device: serviceTag: " + deviceInfo.getServiceTag() +
                            ", ipAddress: "+deviceInfo.getIpAddress()+", message: " + deviceInfo.getStatusMessage() + ", jobId=" + deviceInfo.getJobId());
                    LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError(msg), LogMessage.LogSeverity.ERROR,
                            LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

                    // make sure we didn't leave assigned IP behind. We can only trace this device by IP and chassis config
                    releaseIP(deviceInfo);

                    continue;
                }

                if (deviceInfo.getDeviceRefId()==null) {
                    // cannot proceed with such device
                    String msg = "Discovery by IP range returned unknown device at IP=" + deviceInfo.getIpAddress() + ", jobId=" + deviceInfo.getJobId();
                    logger.error(msg);

                    if (deviceInfo.getStatusMessage()!=null)
                        msg = deviceInfo.getStatusMessage() + ". Resource IP:" + deviceInfo.getIpAddress();

                    LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError(msg), LogMessage.LogSeverity.ERROR,
                            LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

                    // make sure we didn't leave assigned IP behind
                    releaseIP(deviceInfo);

                    continue;
                }

                // if Device Type is not unknown i.e. CMC or Server, check for error message
                if (deviceInfo.getJobId()!=null && deviceInfo.getDeviceType() != null && deviceInfo.getDeviceType() != DeviceType.unknown) {
                    if (StringUtils.isNotEmpty(deviceInfo.getStatusMessage())) {
                        addJobDetail("DISCOVERED_" + deviceInfo.getIpAddress(), deviceInfo.getStatusMessage());
                    }

                    if (DeviceType.isChassis(deviceInfo.getDeviceType())) {
                        addJobDetail("DISCOVERED_" + deviceInfo.getIpAddress(), deviceInfo.getIpAddress() + " " + deviceInfo.getDeviceType());
                    }

                    // this will update existing or add new, as well as run additional config
                    logger.debug("discover completed successfully for jobId=" + deviceInfo.getJobId());
                    updateInventory(deviceInfo);
                }
                else {
                    logger.warn("Ignoring bogus device:" + deviceInfo.getDeviceType() + " refId =" + deviceInfo.getDeviceRefId() + ", jobId=" + deviceInfo.getJobId());
                }
            } catch (InterruptedException e) {
                logger.error("Discover by IP failed", e);
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "discovery failed - job interrupted");
                success = false;
            } catch (ExecutionException e) {
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "discovery failed - execution failed");
                logger.error("Discover by IP failed", e);
                success = false;
            } catch (AsmManagerCheckedException e) {
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "discovery failed - ASM error: " + e.getMessage());
                logger.error("Discover by IP failed" + ", jobId=" + deviceInfo.getJobId(), e);
                success = false;
            } catch (Throwable t) {
                addJobDetail(DISCOVER_IP_RANGE_JOB_STATUS_MSG, "discovery failed - internal error: " + t.getMessage());
                logger.error("Discover by IP failed" + ", jobId=" + ((deviceInfo != null)?deviceInfo.getJobId():"none"), t);
                success = false;
            }
        }

        // all done
        if (success) {
            setJobStatus(JobStatus.SUCCESSFUL);
            getLogService().logMsg(AsmManagerMessages.discoveryJobCompleted(getJobName()), LogMessage.LogSeverity.INFO,
                    LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

        }
        else {
            setJobStatus(JobStatus.FAILED);
            getLogService().logMsg(AsmManagerMessages.discoveryJobFailedLog(getJobName()), LogMessage.LogSeverity.INFO,
                    LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
        }

        logger.info("discovery job completed for the job name:" + getJobName());

        // delete job
        getDiscoveryResultDAO().deleteDiscoveryResult(getJobName());
        getDeviceDiscoverDAO().deleteDiscoveryResult(getJobName());

        // this will not affect unfinished tasks
        threadPoolExecutor.purge();
    }

    private void releaseIP(DiscoveredDevices deviceInfo) {
        if (deviceInfo.getIpAddress()!=null && deviceInfo.getConfig()!=null) {
            logger.debug("Trying to release IP " + deviceInfo.getIpAddress());
            String usageId = getUsageFromConfig(deviceInfo);
            if (usageId!=null) {
                logger.debug("Found usage ID (service tag)=" + usageId);
                IPAddressPoolMgr.getInstance().releaseIPAddressesByUsageId(usageId);
            }
        }
    }

    /**
     * Initial config has the list of networks with assigned static IPs. Try to match IP from failed device to such network.
     * The ID of the network will have coded service tag, which is the usage GUID for ip pool manager.
     * @param deviceInfo
     * @return
     */
    private String getUsageFromConfig(DiscoveredDevices deviceInfo) {
        if (deviceInfo == null)
            return null;

        String config = deviceInfo.getConfig();
        if (StringUtils.isNotEmpty(config)) {
            ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, config);
            for (ServiceTemplateComponent component : template.getComponents()) {
                for (ServiceTemplateCategory category : component.getResources()) {
                    for (ServiceTemplateSetting setting : category.getParameters()) {
                        if (setting.getNetworks() != null) {
                            for (Network network : setting.getNetworks()) {
                                if (network.getStaticNetworkConfiguration() != null &&
                                        network.getStaticNetworkConfiguration().getIpAddress() != null &&
                                        network.getStaticNetworkConfiguration().getIpAddress().equals(deviceInfo.getIpAddress())) {
                                    String[] ids = network.getId().split("-");
                                    if (ids.length == 2)
                                        return ids[1];
                                    else
                                        return null;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Save discovery results in the inventory
     * @param deviceInfo
     */
    private ManagedDevice saveInventory(DiscoveredDevices deviceInfo) throws AsmManagerCheckedException {
        try {
            final String msg = deviceInfo.getDiscoverDeviceType().name() + ", IP=" + deviceInfo.getIpAddress()
                    + " , serviceTag=" + deviceInfo.getServiceTag();

            final ManagedDevice managedDevice = createManagedDevice(deviceInfo);

            // TODO: remove this (at least for puppet devices) when ASM-1510 is fixed
            getPuppetModuleUtil().saveDeviceConfigFile(managedDevice);

                logger.debug("Adding device to inventory: " + msg);

                if (managedDevice.getConfig()==null &&
                        (managedDevice.getDiscoverDeviceType() == DiscoverDeviceType.CMC ||
                                managedDevice.getDiscoverDeviceType() == DiscoverDeviceType.CMC_FX2 ||
                                managedDevice.getDiscoverDeviceType() == DiscoverDeviceType.VRTX)) {
                    // chassis was discovered without configuration job
                    // create config from job details

                    managedDevice.setConfig(createConfigurationTemplate(deviceInfo));
                }

                ManagedDevice[] devices = new ManagedDevice[1];
                devices[0] = managedDevice;

                getDeviceInventoryUtils().updateChassisId(deviceInfo, devices[0]);

                // this call will also calculate compliance
                devices = ProxyUtil.getInventoryProxy().createDeviceInventory(devices);
                if (devices == null || devices.length != 1) {
                    logger.error("DeviceInventoryDAO failed to create device inventory for: " + msg);
                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                }

                //  Need to Create these for the first time now
                if (CollectionUtils.isNotEmpty(deviceInfo.getFirmwareDeviceInventories())) {
                    for (FirmwareDeviceInventory fdi : deviceInfo.getFirmwareDeviceInventories()) {
                        fdi.setId(null);
                        fdi.setDiscoveryResult(null);
                        FirmwareDeviceInventoryEntity fdiEntity = new FirmwareDeviceInventoryEntity(fdi);
                        fdiEntity.setDeviceInventoryId(managedDevice.getRefId());
                        getDeviceInventoryDAO().createFirmwareDeviceInventory(fdiEntity);
                    }
                }

                DeviceInventoryEntity devInv = getDeviceInventoryDAO().getDeviceInventory(managedDevice.getRefId());

                // run device compliance check for a new device
                getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(devInv);


                logger.debug("Device " + msg + " was successfully added to inventory");
                LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.resourceAdded(msg), LogMessage.LogSeverity.INFO,
                        LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

                if (DeviceType.isServer(managedDevice.getDeviceType()) && StringUtils.isNotEmpty(deviceInfo.getServerPoolId())) {
                    addToServerPool(managedDevice, deviceInfo.getServerPoolId());
                }

                return devices[0];

        }catch(Throwable t) {
            String msg = "Discovery job: save/update inventory failed for device: " + deviceInfo.getDeviceRefId() + ", IP=" + deviceInfo.getIpAddress();
            logger.error(msg, t);
            LocalizableMessageService.getInstance().logMsg(AsmManagerMessages.discoveryError(msg), LogMessage.LogSeverity.ERROR,
                    LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);

            return null;
        }
    }

    private ManagedDevice createManagedDevice(DiscoveredDevices deviceInfo) {
        ManagedDevice device = new ManagedDevice();

        device.setDeviceType(deviceInfo.getDeviceType());

        device.setIpAddress(deviceInfo.getIpAddress());
        device.setModel(deviceInfo.getModel());
        device.setRefId(deviceInfo.getDeviceRefId());
        device.setServiceTag(deviceInfo.getServiceTag());
        device.setSystemId(deviceInfo.getSystemId());
        if (deviceInfo.isUnmanaged()) {
            device.setManagedState(ManagedState.UNMANAGED);
        } else if (deviceInfo.isReserved()) {
            device.setManagedState(ManagedState.RESERVED);
        } else {
            device.setManagedState(ManagedState.MANAGED);
        }

        device.setState(DeviceState.READY);

        device.setManufacturer(deviceInfo.getVendor());
        device.setCredId(deviceInfo.getCredId());
        device.setFacts(deviceInfo.getFacts());
        device.setConfig(deviceInfo.getConfig());
        device.setDisplayName(deviceInfo.getDisplayName());

        if (deviceInfo.getHealthState()!=null) {
            device.setHealth(DeviceHealth.valueOf(deviceInfo.getHealthState()));
        } else {
            device.setHealth(DeviceHealth.UNKNOWN);
        }
        
        device.setDiscoverDeviceType(deviceInfo.getDiscoverDeviceType());
        device.setCompliance(CompliantState.UNKNOWN);

        return device;
    }

    private static void addToServerPool(ManagedDevice device, String serverPoolId) {
        IDeviceGroupService service = ProxyUtil.getServerPoolProxy();
        if (StringUtils.isNotEmpty(serverPoolId) &&
                !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID.equals(serverPoolId)) {

            DeviceGroup group = service.getDeviceGroup(serverPoolId);
            if (group != null) {
                group.getManagedDeviceList().getManagedDevices().add(device);
                service.updateDeviceGroup(String.valueOf(group.getGroupSeqId()), group);
            } else {
                logger.warn("No such server pool: " + serverPoolId);
            }
        }
    }

    /**
     * For existing inventory update firmware from discovery results
     * @param deviceInfo
     * @throws AsmManagerCheckedException
     */
    private void updateInventory(DiscoveredDevices deviceInfo) throws AsmManagerCheckedException {
        DeviceInventoryEntity entity = getDeviceInventoryDAO().getDeviceInventory(deviceInfo.getDeviceRefId());
        if (entity == null) {
            // It could be IOM with updated IP address,
            // try to locate it by service tag
            entity = getDeviceInventoryDAO().getDeviceInventoryByServiceTag(deviceInfo.getServiceTag());
        }

        if (entity != null) {
            getDeviceInventoryUtils().updateInventory(deviceInfo);
            ManagedDevice managedDevice = this.createManagedDevice(deviceInfo);
            getPuppetModuleUtil().saveDeviceConfigFile(managedDevice);
        }else{
            ManagedDevice md = saveInventory(deviceInfo);
            if (fromInitialConfig && md!=null) {
                // check if we have additional config and apply it
                // we must be sure this is applied to new devices only!
                applyAdditionalConfig(md);
            }
        }
    }

    private void applyAdditionalConfig(ManagedDevice managedDevice) {

        String status = JobStatus.SUCCESSFUL.getValue();
        Chassis chassis;
        ServiceTemplate template;

        if (DeviceType.isBlade(managedDevice.getDeviceType())) {
            try {
                chassis = ProxyUtil.getDeviceChassisProxyWithHeaderSet().getChassisByServiceTag(managedDevice.getServiceTag(), TagType.SERVER.value());
                DeviceInventoryEntity chassisEntity = this.getDeviceInventoryDAO().getDeviceInventory(chassis.getRefId());

                if (chassisEntity != null) {
                    String sConfig = chassisEntity.getConfig();
                    if (sConfig != null) {
                        template = MarshalUtil.unmarshal(ServiceTemplate.class, sConfig);

                        String jobName = applyBladeConfig(managedDevice, template, chassis);
                        if (jobName!=null && jobName.length()>0) {
                            status = pollForCompletion(jobName);
                        }

                        if (!status.equals(JobStatus.SUCCESSFUL.getValue())) {
                            logger.error("Additional configuration job failed on blade run for chassis " + chassis.getServiceTag());
                            List <ChassisDeviceState> components = new ArrayList<>();
                            for (ServiceTemplateComponent component : template.getComponents()) {
                                ChassisDeviceState cds = new ChassisDeviceState(component.getId(), DeviceState.CONFIGURATION_ERROR);
                                components.add(cds);
                            }
                            JobUtils.updateChassisStatus(components, DeviceState.CONFIGURATION_ERROR, getDeviceInventoryDAO());

                            getLogService().logMsg(AsmManagerMessages.applyConfigurationFailed(jobName).getDisplayMessage(),
                                    LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);

                            throw new LocalizedWebApplicationException(
                                    Response.Status.INTERNAL_SERVER_ERROR,
                                    AsmManagerMessages.internalError());
                        }
                    }
                }

            }catch(LocalizedWebApplicationException lwx) {
                throw lwx;
            }catch(WebApplicationException wex) {
                logger.error("Cannot find chassis for server with service tag: " + managedDevice.getServiceTag(), wex);
            }

        }else if (managedDevice.getDeviceType()==DeviceType.dellswitch) {
            try {
                chassis = ProxyUtil.getDeviceChassisProxyWithHeaderSet().getChassisByServiceTag(managedDevice.getServiceTag(), TagType.IOM.value());
                DeviceInventoryEntity chassisEntity = this.getDeviceInventoryDAO().getDeviceInventory(chassis.getRefId());

                if (chassisEntity != null) {
                    String sConfig = chassisEntity.getConfig();
                    if (sConfig != null) {
                        template = MarshalUtil.unmarshal(ServiceTemplate.class, sConfig);
                        String jobName = applyIOMConfig(managedDevice, template, chassis);
                        if (jobName!=null && jobName.length()>0) {
                            status = pollForCompletion(jobName);
                        }

                        if (!status.equals(JobStatus.SUCCESSFUL.getValue())) {
                            logger.error("Additional configuration job failed on IOM run for chassis " + chassis.getServiceTag());
                            List <ChassisDeviceState> components = new ArrayList<>();
                            for (ServiceTemplateComponent component : template.getComponents()) {
                                ChassisDeviceState cds = new ChassisDeviceState(component.getId(), DeviceState.CONFIGURATION_ERROR);
                                components.add(cds);
                            }
                            JobUtils.updateChassisStatus(components, DeviceState.CONFIGURATION_ERROR, getDeviceInventoryDAO());

                            getLogService().logMsg(AsmManagerMessages.applyConfigurationFailed(jobName).getDisplayMessage(),
                                    LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);

                            throw new LocalizedWebApplicationException(
                                    Response.Status.INTERNAL_SERVER_ERROR,
                                    AsmManagerMessages.internalError());
                        }
                    }
                }

            }catch(LocalizedWebApplicationException lwx) {
                throw lwx;
            }catch(WebApplicationException wex) {
                logger.error("Cannot find chassis for IOM with service tag: " + managedDevice.getServiceTag(), wex);
            }
        }
    }


    private String applyBladeConfig(ManagedDevice managedDevice, ServiceTemplate serviceTemplate, Chassis chassis) {
        ServiceTemplate newTemplate = new ServiceTemplate();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getId().equals(chassis.getRefId())) {
                ServiceTemplateComponent newComp = new ServiceTemplateComponent();
                newTemplate.getComponents().add(newComp);
                newComp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
                newComp.setPuppetCertName(component.getPuppetCertName());
                newComp.setIP(component.getIP());
                newComp.setId(component.getId());
                newComp.setComponentID(component.getComponentID());
                newComp.setName(component.getName());

                for (ServiceTemplateCategory resource: component.getResources()) {
                    if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_RESOURCE_ID)) {
                        // new component for server
                        ServiceTemplateComponent serverComp = new ServiceTemplateComponent();
                        newTemplate.getComponents().add(serverComp);
                        serverComp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
                        serverComp.setPuppetCertName(PuppetModuleUtil.toCertificateName(managedDevice));
                        serverComp.setIP(managedDevice.getIpAddress());
                        serverComp.setId(managedDevice.getRefId());
                        serverComp.setComponentID(managedDevice.getServiceTag());
                        serverComp.setName(managedDevice.getServiceTag());

                        if (createIdracConfigFromChassis(resource, serverComp)) {
                            return ProxyUtil.getDeviceConfigurationProxy().configureChassis(newTemplate);
                        }
                    }
                }

            }
        }
        return null;
    }

    /**
     * return true when any non-trivial configuration parameters have been set
     * @param resource
     * @param component
     * @return
     */
    private boolean createIdracConfigFromChassis(ServiceTemplateCategory resource, ServiceTemplateComponent component) {
        ServiceTemplateCategory category = new ServiceTemplateCategory();
        category.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_BLADE_CONFIG_RESOURCE_ID);

        for (ServiceTemplateSetting oldSet: resource.getParameters()) {
            if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_REGISTER_IDRAC_DNS)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_NTP_ENABLED)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_NTP_PREF)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_NTP_SEC)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_ALERT_DEST)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_TIME_ZONE)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_EMAIL_DEST)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_USERS) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_PROVIDER) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_BLADE_IPMI) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_NAME) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_DATACENTER) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_AISLE) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_RACK) ||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_RACKSLOT)) {

                category.getParameters().add(oldSet);
            }
        }

        String title = component.getPuppetCertName();
        ServiceTemplateSetting titleParam = new ServiceTemplateSetting();
        titleParam.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        titleParam.setValue(title);
        titleParam.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        titleParam.setRequired(false);
        titleParam.setHideFromTemplate(false);
        category.getParameters().add(titleParam);

        component.getResources().add(category);
        return category.getParameters().size()>1; // all but title
    }

    private String applyIOMConfig(ManagedDevice managedDevice, ServiceTemplate serviceTemplate, Chassis chassis) {
        ServiceTemplate newTemplate = new ServiceTemplate();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getId().equals(chassis.getRefId())) {
                ServiceTemplateComponent newComp = new ServiceTemplateComponent();
                newTemplate.getComponents().add(newComp);
                newComp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
                newComp.setPuppetCertName(component.getPuppetCertName());
                newComp.setIP(component.getIP());
                newComp.setId(component.getId());
                newComp.setComponentID(component.getComponentID());
                newComp.setName(component.getName());

                for (ServiceTemplateCategory resource: component.getResources()) {
                    if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_RESOURCE_ID)) {
                        // new component for server
                        ServiceTemplateComponent serverComp = new ServiceTemplateComponent();
                        newTemplate.getComponents().add(serverComp);
                        serverComp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
                        serverComp.setPuppetCertName(PuppetModuleUtil.toCertificateName(managedDevice));
                        serverComp.setIP(managedDevice.getIpAddress());
                        serverComp.setId(managedDevice.getRefId());
                        serverComp.setComponentID(managedDevice.getServiceTag());
                        serverComp.setName(managedDevice.getServiceTag());

                        if (createIOMConfigFromChassis(resource, serverComp)) {
                            return ProxyUtil.getDeviceConfigurationProxy().configureChassis(newTemplate);
                        }
                    }
                }

            }
        }
        return null;
    }

    private boolean createIOMConfigFromChassis(ServiceTemplateCategory resource, ServiceTemplateComponent component) {
        ServiceTemplateCategory category = new ServiceTemplateCategory();
        category.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_IOM_CONFIG_RESOURCE_ID);

        for (ServiceTemplateSetting oldSet: resource.getParameters()) {
            if (oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_SYSLOG)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_NTP1)||
                    oldSet.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CHASSIS_CONFIG_IOM_NTP2)) {

                category.getParameters().add(oldSet);
            }
        }

        String title = component.getPuppetCertName();
        ServiceTemplateSetting titleParam = new ServiceTemplateSetting();
        titleParam.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        titleParam.setValue(title);
        titleParam.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        titleParam.setRequired(false);
        titleParam.setHideFromTemplate(false);
        category.getParameters().add(titleParam);

        component.getResources().add(category);
        return category.getParameters().size()>1; // all counts except title
    }

    /**
     * Poll for Job completion
     *
     * @param jobName name of Job
     * @return Job completion status
     */
    protected String pollForCompletion(String jobName) {
        String status;
        while (true) {
            try {
                status = getJobHistoryManager().getExecHistoryStatus(jobName);
            } catch (JobManagerException e) {
                logger.error(e.getMessage());
                return JobStatus.FAILED.getValue();
            }
            if (JobStatus.isTerminal(status))
                return status;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                return JobStatus.FAILED.getValue();
            }
        }
    }

    /**
     * Create a ServiceTemplate for given device inventory.
     *
     * @param chassis
     * @return
     */
    private String createConfigurationTemplate(DiscoveredDevices chassis)
            throws LocalizedWebApplicationException {
        ServiceTemplateSetting setting;

        ServiceTemplateSetting cmcNetworkssetting;
        ServiceTemplateSetting idracNetworkssetting;
        ServiceTemplateSetting iomNetworkssetting;
        ServiceTemplateSetting iomSlots;
        ServiceTemplateSetting idracSlots;

        ServiceTemplate configuration = new ServiceTemplate();

        ServiceTemplateComponent comp = new ServiceTemplateComponent();
        configuration.getComponents().add(comp);
        comp.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
        comp.setPuppetCertName(PuppetModuleUtil.toCertificateName(chassis.getDiscoverDeviceType(),chassis.getDeviceType(), chassis.getServiceTag()));
        comp.setIP(chassis.getIpAddress());
        // do not set ID - leaving ID empty helps NOT running configuration job on new blades/ioms

        DiscoverIPRangeDeviceRequest request = findRequestByIP(chassis.getIpAddress());

        ServiceTemplateCategory category = new ServiceTemplateCategory();

        category.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID);
        comp.getResources().add(category);

        if (request != null) {
            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_CRED);
            setting.setValue(request.getDeviceChassisCredRef());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED);
            setting.setValue(request.getDeviceServerCredRef());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);

            setting = new ServiceTemplateSetting();
            setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_CRED);
            setting.setValue(request.getDeviceSwitchCredRef());
            setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            category.getParameters().add(setting);
        }

        // reserve IP
        setting = new ServiceTemplateSetting();
        setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORK_TYPE);
        setting.setValue(AddressingMode.Existing.getValue());
        setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        category.getParameters().add(setting);

        cmcNetworkssetting = new ServiceTemplateSetting();
        cmcNetworkssetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_NETWORKS);
        cmcNetworkssetting.setValue("");
        cmcNetworkssetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
        category.getParameters().add(cmcNetworkssetting);
        List<Network> networks = new ArrayList<>();
        cmcNetworkssetting.setNetworks(networks);

        setting = new ServiceTemplateSetting();
        setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORK_TYPE);
        setting.setValue(AddressingMode.Existing.getValue());
        category.getParameters().add(setting);

        idracNetworkssetting = new ServiceTemplateSetting();
        idracNetworkssetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
        idracNetworkssetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_NETWORKS);
        idracNetworkssetting.setValue("");
        category.getParameters().add(idracNetworkssetting);
        networks = new ArrayList<>();
        idracNetworkssetting.setNetworks(networks);

        idracSlots = new ServiceTemplateSetting();
        idracSlots.setType(ServiceTemplateSetting.ServiceTemplateSettingType.LIST);
        idracSlots.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_SLOTS);
        category.getParameters().add(idracSlots);
        idracSlots.setValue("");

        setting = new ServiceTemplateSetting();
        setting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        setting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORK_TYPE);
        setting.setValue(AddressingMode.Existing.getValue());
        category.getParameters().add(setting);

        iomNetworkssetting = new ServiceTemplateSetting();
        iomNetworkssetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.NETWORKCONFIGURATION);
        iomNetworkssetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_NETWORKS);
        iomNetworkssetting.setValue("");
        category.getParameters().add(iomNetworkssetting);
        networks = new ArrayList<>();
        iomNetworkssetting.setNetworks(networks);

        iomSlots = new ServiceTemplateSetting();
        iomSlots.setType(ServiceTemplateSetting.ServiceTemplateSettingType.LIST);
        iomSlots.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_SLOTS);
        category.getParameters().add(iomSlots);
        iomSlots.setValue("");

        String title = comp.getPuppetCertName();
        ServiceTemplateSetting titleParam = new ServiceTemplateSetting();
        titleParam.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        titleParam.setValue(title);
        titleParam.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        titleParam.setRequired(false);
        titleParam.setHideFromTemplate(true);
        category.getParameters().add(titleParam);

        String configXml = MarshalUtil.marshal(configuration);
        return configXml;
    }

    private DiscoverIPRangeDeviceRequest findRequestByIP(String ip) {
        for (DiscoverIPRangeDeviceRequest deviceInfo : discoverIpRangeRequests.getDiscoverIpRangeDeviceRequests()) {
            List<String> expandedIpRange = DiscoverIPRangeDeviceRequest.expandIpAddresses(deviceInfo);
            if (expandedIpRange != null && !expandedIpRange.isEmpty()) {
                for (String ipAddress : expandedIpRange) {
                    if (ipAddress.equals(ip)) {
                        return deviceInfo;
                    }
                }
            }
        }
        return null;
    }

}
