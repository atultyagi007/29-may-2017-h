/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReport;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareUpdateRequest;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareUpdateRequest.UpdateType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.UpdateDeviceInventoryResponse;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentUserRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.tasks.DeleteDeviceJob;
import com.dell.asm.asmcore.asmmanager.tasks.DeviceInventoryJob;
import com.dell.asm.asmcore.asmmanager.tasks.FirmwareUpdateJob;
import com.dell.asm.asmcore.asmmanager.util.AsmManagerUtil;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage.LogCategory;
import com.dell.asm.localizablelogger.LogMessage.LogSeverity;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.model.Link;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.RestUtil;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IOM;
import com.dell.pg.asm.chassis.client.device.Server;
import com.dell.pg.asm.server.client.device.IServerDeviceService;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;

/**
 * Implementation of Device Inventory REST Service for ASM Manager.
 * 
 */
@Path("/ManagedDevice")
public class DeviceInventoryService implements IDeviceInventoryService {
    // Logger.
    private static final Logger logger = Logger.getLogger(DeviceInventoryService.class);

    public static final String DEVICE_RA_ID = "deviceID";
    public static final String RELATED_DEVICE_RA_IDS = "relatedDeviceIDs";
    public static final String DEVICE_FORCE_DELETE = "forceDelete";

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    @Autowired
    private IServerDeviceService serverDeviceService;

    private PuppetModuleUtil puppetModuleUtil = null;
    private DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();
    private final DeploymentDAO deploymentDAO = DeploymentDAO.getInstance();
    private FirmwareUtil firmwareUtil = null;
    private AsmManagerUtil asmManagerUtil = null;
    private DeviceInventoryUtils deviceInventoryUtils = null;
    
    public DeviceInventoryService(AsmManagerUtil asmManagerUtil,
                                  FirmwareUtil firmwareUtil) {
        this.asmManagerUtil = asmManagerUtil;
        this.firmwareUtil = firmwareUtil;
    }
    
    public DeviceInventoryService() {
        this(new AsmManagerUtil(),
             new FirmwareUtil());
    }
    
    private static final Set<String> validSortColumns = new HashSet<>();

    static {
        validSortColumns.add("displayName");
        validSortColumns.add("serviceTag");
        validSortColumns.add("refId");
        validSortColumns.add("health");
        validSortColumns.add("refType");
        validSortColumns.add("deviceType");
        validSortColumns.add("ipAddress");
        validSortColumns.add("managedState");
        validSortColumns.add("state");
        validSortColumns.add("model");
        validSortColumns.add("statusMessage");
        validSortColumns.add("createdDate");
        validSortColumns.add("createdBy");
        validSortColumns.add("updatedDate");
        validSortColumns.add("updatedBy");
        validSortColumns.add("healthMessage");
        validSortColumns.add("compliant");
        validSortColumns.add("infraTemplateDate");
        validSortColumns.add("infraTemplateId");
        validSortColumns.add("serverTemplateDate");
        validSortColumns.add("serverTemplateId");
        validSortColumns.add("inventoryDate");
        validSortColumns.add("complianceCheckDate");
        validSortColumns.add("discoveredDate");
        validSortColumns.add("identityRef");
        validSortColumns.add("vendor");
        validSortColumns.add("chassisId");
        validSortColumns.add("resourceType");
    }

    public static final Set<String> validFilterColumns = new HashSet<>();

    static {
        validFilterColumns.add("displayName");
        validFilterColumns.add("serviceTag");
        validFilterColumns.add("refId");
        validFilterColumns.add("health");
        validFilterColumns.add("refType");
        validFilterColumns.add("deviceType");  // also referneced from ServiceTemplateService.java
        validFilterColumns.add("ipAddress");
        validFilterColumns.add("managedState");
        validFilterColumns.add("state");
        validFilterColumns.add("model");
        validFilterColumns.add("statusMessage");
        validFilterColumns.add("createdDate");
        validFilterColumns.add("createdBy");
        validFilterColumns.add("updatedDate");
        validFilterColumns.add("updatedBy");
        validFilterColumns.add("healthMessage");
        validFilterColumns.add("compliant");
        validFilterColumns.add("infraTemplateDate");
        validFilterColumns.add("infraTemplateId");
        validFilterColumns.add("serverTemplateDate");
        validFilterColumns.add("serverTemplateId");
        validFilterColumns.add("inventoryDate");
        validFilterColumns.add("complianceCheckDate");
        validFilterColumns.add("discoveredDate");
        validFilterColumns.add("identityRef");
        validFilterColumns.add("vendor");
        validFilterColumns.add("credId");
        validFilterColumns.add("service");
        validFilterColumns.add("serverpool");
        validFilterColumns.add("chassisId");
        validFilterColumns.add("resourceType");
    }

    /**
     * @see com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService#createDeviceInventory(com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice[])
     */
    @Override
    public ManagedDevice[] createDeviceInventory(ManagedDevice[] devices) {
        for (ManagedDevice device : devices) {
            if (device.getRefId() == null || device.getRefId().trim().length() == 0) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.missingRequired("refId"));
            }
            if (device.getDeviceType() == null) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.missingRequired("deviceType"));
            }
            if (device.getServiceTag() == null || device.getServiceTag().trim().length() == 0) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.missingRequired("serviceTag"));
            }
            if (device.getIpAddress() == null || device.getIpAddress().trim().length() == 0) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.missingRequired("ipAddress"));
            }
        }

        // Check if enough licenses are available
        // MD commenting out for ASM 7.5 as per Angela's email
        //checkLicenseAvailability(devices);

        try {

            List<DeviceInventoryEntity> deviceEntities = deviceInventoryDAO.createDeviceInventory(DeviceInventoryUtils.toEntities(
                    Arrays.asList(devices), true));

            ManagedDevice[] results = DeviceInventoryUtils.toDTOs(deviceEntities, true).toArray(new ManagedDevice[deviceEntities.size()]);

            if (servletResponse!=null) {
                servletResponse.setStatus(Response.Status.CREATED.getStatusCode());
                for (ManagedDevice result : results) {
                    result.setDetailLink(ProxyUtil.buildDeviceDetailLink(result.getDeviceType(), result.getRefId(), result.getRefId(), servletRequest,
                            uriInfo, httpHeaders));
                }
            }

            for (ManagedDevice currEntity : devices) {
                getPuppetModuleUtil().saveDeviceConfigFile(currEntity);
            }

            return results;
        } catch (AsmManagerCheckedException adex) {
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                throw new LocalizedWebApplicationException(Response.Status.CONFLICT, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.DUPLICATE_SERVICETAG) {
                throw new LocalizedWebApplicationException(Response.Status.CONFLICT, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD) {
                throw new LocalizedWebApplicationException(Response.Status.CONFLICT, adex.getEEMILocalizableMessage());
            }

            String msg = "Create Device" + adex.getMessage();
            logger.error(msg, adex);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    /**
     * @see com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService#getDeviceInventory(java.lang.String)
     */
    @Override
    public ManagedDevice getDeviceInventory(String refId) {
        ManagedDevice device = null;
        DeviceInventoryEntity entity = deviceInventoryDAO.getDeviceInventory(refId);
        User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
        if (entity == null || !checkUserPermissions(entity, thisUser)) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(refId));
        }
        device = DeviceInventoryUtils.toDTO(entity, true);

        if (httpHeaders!=null)
            device.setDetailLink(ProxyUtil.buildDeviceDetailLink(device.getDeviceType(), device.getRefId(), device.getRefId(), servletRequest, uriInfo,
                httpHeaders));
        return device;
    }

    private void createAndStartTrigger(IJobManager jm, JobDetail job, FirmwareUpdateRequest request) throws SchedulerException {
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = null;
        if ("updatenow".equals(request.getScheduleType()) || "nextreboot".equals(request.getScheduleType())) {
            trigger = jm.createNamedTrigger(schedBuilder, job, true);
        } else if ("schedule".equals(request.getScheduleType())) {
            SimpleTriggerImpl t = new SimpleTriggerImpl();
            t.setRepeatCount(0);
            t.setName(UUID.randomUUID().toString());
            t.setGroup(FirmwareUpdateJob.class.getSimpleName()); // Matches the above trigger creation
            t.setStartTime(request.getScheduleDate());
            trigger = t;
        }

        if (trigger != null) {
            jm.scheduleJob(job, trigger);

            logger.info("checking and starting the scheduler");
            if (!jm.getScheduler().isStarted()) {
                jm.getScheduler().start();
                logger.info("scheduler started");
            }
        }
    }

    /**
     * @see com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService#updateDeviceFirmware(com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareUpdateRequest)
     */
    @Override
    public Response updateDeviceFirmware(FirmwareUpdateRequest request) {

        // Assert the default repository, if set, is not in error state
        failIfDefaultRepositoryInError();

        if ("schedule".equals(request.getScheduleType())) {
            Date now = new Date();
            Date scheduleDate = request.getScheduleDate();
            if (scheduleDate == null || now.after(scheduleDate)) {
                logger.info("Requested schedule date of " + scheduleDate
                        + " is before current date of " + now);
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.scheduleDateIsPast());
            }
        }

        try {
            IJobManager jm = JobManager.getInstance();
            List<String> ids = request.getIdList();

            if (ids != null)
                for (String id : ids) {
                    DeploymentEntity deployment = null;
                    ServiceTemplate serviceTemplate = null;
                    // Set all device states to PENDING so that scheduled firmware jobs are reflected in deployment status.
                    if (UpdateType.SERVICE.equals(request.getUpdateType())) {
                        deployment = deploymentDAO.getDeployment(id, DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
                        if (deployment.getMarshalledTemplateData() != null) {
                            serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, deployment.getMarshalledTemplateData());
                            if (serviceTemplate.getComponents() != null) {
                                for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                                    if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                                        String refId;
                                        // Is this check necessary?  If so, why is it not used in FirmwareUpdateJob::getJobDevices() ?
                                        if (component.getAsmGUID() != null) {
                                            refId = component.getAsmGUID();
                                        } else {
                                            refId = component.getId();
                                        }
                                        deviceInventoryDAO.setDeviceState(refId, DeviceState.PENDING, false);
                                    }
                                }
                            }
                        }
                    } else {
                        deviceInventoryDAO.setDeviceState(id, DeviceState.PENDING, false);
                    }
                    // If there is no cluster we create a separate Job for each device in the deployment/Service
                    // so that the jobs can run in parallel.  Cluster jobs need to be run sequentially.
                    if (deployment != null && serviceTemplate!= null && !serviceTemplate.hasClusterComponentType()) {
                        if (serviceTemplate != null && serviceTemplate.getComponents() != null) {
                            boolean nextReboot = "nextreboot".equals(request.getScheduleType());
                            String firmwareId = null;
                            if (deployment != null &&
                                    deployment.getFirmwareRepositoryEntity() != null &&
                                    deployment.getFirmwareRepositoryEntity().getId() != null) {
                                firmwareId = deployment.getFirmwareRepositoryEntity().getId();
                            }
                            // Get all the servers in the service and run a firmware update job for each.
                            String groupName = "FirmwareUpdate:" + deployment.getId();
                            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                                if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                                    String refId;
                                    if (component.getAsmGUID() != null) {
                                        refId = component.getAsmGUID();
                                    } else {
                                        refId = component.getId();
                                    }
                                    JobDetail job = FirmwareUtil.getUpdateFirmwareJob(jm,
                                            refId,
                                            request.isExitMaintenanceMode(),
                                            nextReboot,
                                            FirmwareUpdateJob.UPDATE_TYPE_DEVICE,
                                            null,
                                            firmwareId,
                                            groupName);
                                    job.getJobDataMap().put(FirmwareUpdateJob.DEPLOYMENT_STATE_KEY, deployment.getStatus().getValue());
                                    createAndStartTrigger(jm, job, request);
                                }
                            }
                        }
                    } else {
                        // Individual devices or service with cluster
                        String firmwareId = null;
                        if (deployment != null &&
                                deployment.getFirmwareRepositoryEntity() != null &&
                                deployment.getFirmwareRepositoryEntity().getId() != null) {
                            firmwareId = deployment.getFirmwareRepositoryEntity().getId();
                        }
                        JobDetail job = FirmwareUtil.getUpdateFirmwareJob(jm,
                                                                          id,
                                                                          request.isExitMaintenanceMode(),
                                                                          "nextreboot".equals(request.getScheduleType()),
                                                                          UpdateType.SERVICE.equals(request.getUpdateType()) ? FirmwareUpdateJob.UPDATE_TYPE_SERVICE : FirmwareUpdateJob.UPDATE_TYPE_DEVICE,
                                                                          null,
                                                                          firmwareId);

                        if (deployment != null) {
                            job.getJobDataMap().put(FirmwareUpdateJob.DEPLOYMENT_STATE_KEY, deployment.getStatus().getValue());
                        }
                        createAndStartTrigger(jm, job, request);
                    }
                }
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Exception while updating inventory", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    /**
     * Assert that the default repository, if set, is not in an error state.
     * Otherwise, throw an exception.
     * @return true on success
     */
    private boolean failIfDefaultRepositoryInError() {
        GenericDAO genericDAO = GenericDAO.getInstance();
        final FirmwareRepositoryEntity defaultRepo = firmwareUtil.getDefaultRepo();
        if (defaultRepo != null) {
            if ( ! defaultRepo.getState().equals(RepositoryState.AVAILABLE) &&
                    ! defaultRepo.getState().equals(RepositoryState.COPYING)) {
                logger.error("#### Default Repository is not available. Aborting Update");
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.firmwareRepositoryNotAvailable("Default"));
            }
        }
        return true;
    }

    /**
     * @see com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService#updateDeviceInventory(java.lang.String,
     * com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice)
     */
    @Override
    public UpdateDeviceInventoryResponse updateDeviceInventory(String refId, ManagedDevice newDeviceInventory) {
        if (!refId.equals(newDeviceInventory.getRefId())) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.mismatchRefId(refId,
                    newDeviceInventory.getRefId()));
        }

        UpdateDeviceInventoryResponse response = new UpdateDeviceInventoryResponse();

        DeviceInventoryEntity origDevice = deviceInventoryDAO.getDeviceInventory(refId);
        User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
        if (origDevice == null) {
            EEMILocalizableMessage msg = AsmManagerMessages.notFound(refId);
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, msg);
        }
        if (!checkUserPermissions(origDevice, thisUser)) {
            EEMILocalizableMessage msg = AsmManagerMessages.userDoesNotHaveAccessToPool(thisUser.getUserName());
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, msg);
        }

        DeviceInventoryEntity entity = DeviceInventoryUtils.toEntity(newDeviceInventory, true);

        // Don't allow some fields to be overwritten
        entity.setOsIpAddress(origDevice.getOsIpAddress());
        entity.setOsImageType(origDevice.getOsImageType());
        entity.setOsAdminPassword(origDevice.getOsAdminPassword());

        try {
            deviceInventoryDAO.updateDeviceInventory(entity);
        } catch (AsmManagerCheckedException adex) {
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, adex.getEEMILocalizableMessage());
            } else {
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, adex.getEEMILocalizableMessage());
            }
        } catch (Exception e) {
            logger.error("Exception while updating inventory", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        return response;
    }

    public JobDetail scheduleDeviceInventoryJob(ManagedDevice managedDevice, IJobManager jobManager) throws SchedulerException {

        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
        JobDetail job = jobManager.createNamedJob(DeviceInventoryJob.class);
        String xmlData = MarshalUtil.marshal(managedDevice);
        job.getJobDataMap().put(DeviceInventoryJob.DEVICEINVENTORY_KEY_DATA, xmlData);

        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = jobManager.createNamedTrigger(schedBuilder, job, true);

        jobManager.scheduleJob(job, trigger);

        LocalizableMessageService.getInstance().logMsg(
                AsmManagerMessages.inventoryStartedOnHostMsg(managedDevice.getServiceTag()),
                LogSeverity.INFO, LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
        return job;

    }

    /**
     * @see com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService#getAllDeviceInventory(java.lang.String, java.util.List,
     * java.lang.Integer, java.lang.Integer)
     */
    @Override
    public ManagedDevice[] getAllDeviceInventory(String sort, List<String> filter, Integer offset, Integer limit) {
        return getAllDeviceInventoryInternal(sort, filter, offset, limit, false);
    }

    /**
     * This call not only sets "compliant" column but also device.getFirmwareDeviceInventories().setFirmwareComplianceComponents()
     * for default repository. It creates significant overhead on execution. Use it only when you _really_ need
     * ManagedDevice.FirmwareDeviceInventory.FirmwareComplianceComponents to be populated.
     * Overall Compliance status is _always_ set by discovery/ inventory run and doesn't need this call.
     */
    @Override
    public ManagedDevice[] getAllDeviceInventoryWithComplianceCheck(String sort, List<String> filter, Integer offset, Integer limit) {
        return getAllDeviceInventoryInternal(sort, filter, offset, limit, true);
    }

    private ManagedDevice[] getAllDeviceInventoryInternal(String sort, List<String> filter, Integer offset, Integer limit, boolean complianceCheck) {

        List<ManagedDevice> devices = new ArrayList<ManagedDevice>();

        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        // Build filter list from filter params ( comprehensive )
        FilterParamParser filterParser = new FilterParamParser(filter, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        int totalRecords = 0;

        List<DeviceInventoryEntity> entities = deviceInventoryDAO.getAllDeviceInventory(sortInfos, filterInfos, null);
        User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
        if (entities != null) {
            for (DeviceInventoryEntity entity : entities) {
                if (checkUserPermissions(entity, thisUser)){
                    totalRecords++;
	                ManagedDevice md = DeviceInventoryUtils.toDTO(entity, true, true);
	                devices.add(md);
                }
            }
        }

        if (totalRecords > 0) { // get paginated results only if there are any records
            PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest, servletResponse, httpHeaders, uriInfo);

            int pageOffSet;
            if (offset == null || offset > totalRecords || offset < 0) {
                pageOffSet = 0;
            } else {
                pageOffSet = offset;
            }

            int pageLimit;
            if (limit == null) {
                pageLimit = paginationParamParser.getMaxLimit();
            } else {
                pageLimit = limit;
            }

            PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, totalRecords);

            devices = this.getPaginatedManagedDevices(devices, paginationInfo);

            if (httpHeaders!=null) {
                UriBuilder linkBuilder = RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders);

                linkBuilder.replaceQuery(null);
                linkBuilder.path("device");
                if (sort != null) {
                    linkBuilder.queryParam(AsmConstants.QUERY_PARAM_SORT, sort);
                }

                if (filterInfos != null) {
                    for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
                        if (filterInfo.isSimpleFilter()) {
                            // Simple filter take only one value.
                            linkBuilder.queryParam(filterInfo.getColumnName(), filterInfo.buildValueString());
                        } else {
                            linkBuilder.queryParam(AsmConstants.QUERY_PARAM_FILTER, filterInfo.buildValueString());
                        }
                    }
                }

                // Common library to add link headers in response headers
                paginationParamParser.addLinkHeaders(paginationInfo, linkBuilder);
            }
        }

        ManagedDevice[] results = devices.toArray(new ManagedDevice[devices.size()]);
        if (servletResponse!=null) {
            servletResponse.setHeader(AsmConstants.DELL_TOTAL_COUNT_HEADER, String.valueOf(totalRecords));
            for (ManagedDevice result : results) {
                result.setDetailLink(ProxyUtil.buildDeviceDetailLink(result.getDeviceType(), result.getRefId(), result.getRefId(), servletRequest,
                        uriInfo, httpHeaders));
            }
        }
        return results;
    }
    
    @Override
    public Response deleteDeviceInventory(String refId, boolean forceDelete) {
        logger.debug("in method: deleteDeviceInventory");

        try {
            // Check for the state of the device, if the state is PENDING_<OPERATION> then this delete request shouldn't be accepted
            if (getDeviceInventoryUtils().validateDeviceDeleteRequest(refId, forceDelete)) {
                logger.debug("Delete device request is valid");
                JobDetail job = null;
                String jobName = "";

                IJobManager jm = JobManager.getInstance();
                SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
                job = jm.createNamedJob(DeleteDeviceJob.class);
                List<String> listOfDeviceIds = getRelatedDeviceIds(refId);
                job.getJobDataMap().put(DEVICE_RA_ID, refId);
                job.getJobDataMap().put(RELATED_DEVICE_RA_IDS, listOfDeviceIds);
                job.getJobDataMap().put(DEVICE_FORCE_DELETE, forceDelete);
                // Create a trigger and associate it with the schedule, job,
                // and some arbitrary information. The boolean means "start now".
                Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);
                logger.debug("created DeleteDeviceJob, about to run");

                // Update the device status in the DB.
                // By setting state to PENDING_DELETE , no other operation will be placed on this device.
                // It closes the small time window that can cause a racing condition.
                ManagedDevice manageDevice = getDeviceInventory(refId);
                manageDevice.setState(DeviceState.PENDING_DELETE);
                DeviceInventoryEntity entity = DeviceInventoryUtils.toEntity(manageDevice, true);
                deviceInventoryDAO.updateDeviceInventory(entity);


                // Schedule our job using our trigger.
                jm.scheduleJob(job, trigger);
                logger.info("checking and starting the scheduler");
                if (!jm.getScheduler().isStarted()) {
                    jm.getScheduler().start();
                    logger.info("scheduler started");
                }
                // Return the job name.
                jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
                logger.debug("Delete Device operation for" + refId + ", job name:" + jobName);

                if (servletResponse!=null) {
                    Link jobStatusLink = buildJobLink("Device Delete Job", jobName);
                    servletResponse.setHeader("Link", RestUtil.toLinkHeaderString(jobStatusLink));
                }
            }
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException occurred for deleteDeviceInventory REST request: ", e);
            throw e;
        } catch (SchedulerException e) {
            logger.error("SchedulerException occurred for deleteDeviceInventory REST request: ", e);
            throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(AsmManagerMessages.internalError())
                    .build());
        } catch (Exception e) {
            logger.error("Exception occurred for deleteDeviceInventory REST request: ", e);
            throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(AsmManagerMessages.internalError())
                    .build());
        }
        if (servletResponse!=null)
            servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private Link buildJobLink(String title, String jobName) {
        UriBuilder newBuilder = RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders);
        newBuilder.replacePath("/JRAF/jobhistory/" + jobName + "/status");
        return new Link(title, newBuilder.build().toString(), Link.RelationType.MONITOR);
    }

    @Override
    public ManagedDevice getDeviceInventoryByCertName(String certName) {
        ManagedDevice device = null;
        logger.debug("getDeviceInventoryByCertName() - will look for device by certificate name: " + certName);

        if (certName == null || certName.indexOf("-") < 0) {
            logger.error("getDeviceInventoryByCertName() - Invalid or null certificate name: " + certName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        String serviceTag = certName.substring(certName.indexOf('-') + 1);

        logger.debug("getDeviceInventoryByCertName() - looking for device by service tag: " + serviceTag);

        device = DeviceInventoryUtils.toDTO(deviceInventoryDAO.getDeviceInventoryByServiceTag(serviceTag), true);
        if (device == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(serviceTag));
        }

        if (httpHeaders!=null)
            device.setDetailLink(ProxyUtil.buildDeviceDetailLink(device.getDeviceType(), device.getRefId(), device.getRefId(), servletRequest, uriInfo,
                    httpHeaders));
        return device;
    }

    private List<ManagedDevice> getPaginatedManagedDevices(List<ManagedDevice> managedDevices, PaginationParamParser.PaginationInfo paginationInfo){
    	
    	List<ManagedDevice> paginatedManagedDevices = new ArrayList<ManagedDevice>();
    	if(paginationInfo != null && managedDevices != null){
    		int offset = paginationInfo.getOffset();
			
			if(paginationInfo.getOffset() + paginationInfo.getLimit() < paginationInfo.getTotalRecords()){ 
    			paginatedManagedDevices.addAll(managedDevices.subList(offset, offset + paginationInfo.getLimit()));  // We're on middle pages
    		}
    		else { 
    			if(offset == 0 && paginationInfo.getTotalRecords() <= paginationInfo.getLimit()){ // There is ONLY one page of data
    				paginatedManagedDevices.addAll(managedDevices);
    			}
    			else { // we're on the last page now
    				if(offset == paginationInfo.getTotalRecords() - 1){ // There is only ONE item in the list and ArrayList.sublist will not work
    					paginatedManagedDevices.add(managedDevices.get(offset));
    				}
    				else{ // There is more than one item left on the last page
    					paginatedManagedDevices.addAll(managedDevices.subList(offset, paginationInfo.getTotalRecords()));
    				}
    			}
    		}
    	}
    	
    	return paginatedManagedDevices;
    }
    
    @Override
    public UpdateDeviceInventoryResponse updateDeviceInventoryByCertName(String certName, ManagedDevice newDeviceInventory) {
        logger.debug("updateDeviceInventoryByCertName() - will look for device by certificate name: " + certName);

        ManagedDevice device = getDeviceInventoryByCertName(certName); // cannot be null
        return this.updateDeviceInventory(device.getRefId(), newDeviceInventory);
    }

    private List<String> getRelatedDeviceIds(String refId) throws AsmManagerCheckedException {
        DeviceInventoryEntity device = this.deviceInventoryDAO.getDeviceInventory(refId);
        List<String> listOfServiceTags = new ArrayList<>();
        try {
            // In case of chassis need to fetch the associated server and switch device ids
            if (DeviceType.isChassis(device.getDeviceType())) {
                List<Server> chassisServers = null;
                List<IOM> chassisIoms = null;

                Chassis chassisDevice = ProxyUtil.getDeviceChassisProxyWithHeaderSet().getChassis(device.getRefId());
                if (chassisDevice != null) {
                    chassisServers = chassisDevice.getServers();
                    chassisIoms = chassisDevice.getIOMs();

                    if (chassisServers != null) {
                        for (Server server : chassisServers) {
                            listOfServiceTags.add(server.getServiceTag());
                        }
                    }
                    if (chassisIoms != null) {
                        for (IOM iom : chassisIoms) {
                            listOfServiceTags.add(iom.getServiceTag());
                        }
                    }
                }
            }
            List<DeviceInventoryEntity> devices = deviceInventoryDAO.getDevicesByServiceTags(listOfServiceTags);
            List<String> deviceRefIds = new ArrayList<>();
            for (DeviceInventoryEntity deviceEntity : devices) {
                deviceRefIds.add(deviceEntity.getRefId());
            }
            return deviceRefIds;
        } catch (Exception e) {
            logger.error("Error occurred in getRelatedDeviceIds method:" + e.getMessage());
            throw new AsmRuntimeException(AsmManagerMessages.internalError(), e);
        }
    }

    // TODO: methods below should be moved to a utility class

    /**
     * Check if the currently logged in user is allowed to access this device.
     *
     * @param entity The device entity
     * @return True if the user is allowed access to the device.
     */
    public static boolean checkUserPermissions(DeviceInventoryEntity entity, User thisUser) {
        if (thisUser.getRole().equals(AsmConstants.USERROLE_READONLY)) {
            return true;
        }
        if (thisUser.getRole().equals(AsmConstants.USERROLE_ADMINISTRATOR)) {
            return true;
        }

        if (entity.getDeviceGroupList() != null && entity.getDeviceGroupList().size() > 0) {
            for (DeviceGroupEntity dg : entity.getDeviceGroupList()) {
                if (dg.getGroupsUsers() != null) {
                    for (Long ref : dg.getGroupsUsers()) {
                        if (thisUser.getUserSeqId() == ref) {
                            return true;
                        }
                    }
                }
            }
        } else {
            // no server pool
            return true;
        }

        // allow user and/or user group to see device details if the access is granted to the service
        List<DeploymentEntity> deployments = entity.getDeployments();
        if (deployments != null && !deployments.isEmpty()) {
            for (DeploymentEntity deployment : deployments) {
                if (deployment.isAllUsersAllowed()) {
                    return true;
                }
                Set<DeploymentUserRefEntity> assignedUsers = deployment.getAssignedUserList();
                if (assignedUsers != null && !assignedUsers.isEmpty()) {
                    for (DeploymentUserRefEntity user : assignedUsers) {
                        if (user.getUserId() == thisUser.getUserSeqId()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Integer getDeviceTotalCount(List<String> filter) {
        FilterParamParser filterParser = new FilterParamParser(filter, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        int totalRecords = 0;
        List<DeviceInventoryEntity> entities = deviceInventoryDAO.getAllDeviceInventory(null, filterInfos, null);
        User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
        if (entities != null) {
            for (DeviceInventoryEntity entity : entities) {
                if (checkUserPermissions(entity, thisUser))
                    totalRecords++;
            }
        }
        return totalRecords;
    }
    
    @Override
    public Response exportAllDevices() throws WebApplicationException {
        // Requested to be sorted by IpAddress initially
        ManagedDevice[] managedDevices = this.getAllDeviceInventory("ipAddress",  null,  0,  Integer.MAX_VALUE);
        return Response
                .ok(new StreamingCSVDeviceOutput(managedDevices), MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = devices.csv")
                .build();
    }
    
 private class StreamingCSVDeviceOutput implements StreamingOutput {
        
        private static final int BATCH_FETCH_SIZE = 100;
        private static final String DEVICE_HEALTH = "Health";
        private static final String DEVICE_STATE = "State";
        private static final String DEVICE_NAME = "Resource Name";
        private static final String DEVICE_IP_ADDRESS = "IP Address";
        private static final String DEVICE_OS_HOST_NAME ="OS Hostname";
        private static final String DEVICE_SERVICE_TAG = "Asset/Service Tag";
        private static final String DEVICE_MANUFACTURER = "Manufacturer";
        private static final String DEVICE_MODEL = "Model";
        private static final String DEVICE_RESOURCE_TYPE = "Resource Type";
        private static final String DEVICE_FIRMWARE_STATUS = "Firmware Status";
        private static final String DEVICE_ROW_TEMPLATE = 
                "\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",\"{5}\",\"{6}\",\"{7}\",\"{8}\",\"{9}\"\n";
        
        private ManagedDevice[] managedDevices;
        public StreamingCSVDeviceOutput(ManagedDevice[] managedDevices) {
            this.managedDevices = managedDevices;
        }
        
        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            final PrintWriter writer = new PrintWriter(outputStream);
            
            try {
                writer.write(MessageFormat.format(DEVICE_ROW_TEMPLATE, DEVICE_HEALTH, DEVICE_STATE, DEVICE_NAME, 
                        DEVICE_IP_ADDRESS, DEVICE_OS_HOST_NAME, DEVICE_SERVICE_TAG, DEVICE_MANUFACTURER, DEVICE_MODEL, 
                        DEVICE_RESOURCE_TYPE, DEVICE_FIRMWARE_STATUS));

                for (ManagedDevice managedDevice : this.managedDevices) {
                    // add the csv row headers
                    writer.write(MessageFormat.format(DEVICE_ROW_TEMPLATE,
                    (managedDevice.getHealth() != null) ? managedDevice.getHealth().name() : StringUtils.EMPTY,
                    (managedDevice.getState() != null) ? managedDevice.getState().name() : StringUtils.EMPTY,
                    (managedDevice.getDisplayName() != null) ? managedDevice.getDisplayName() : StringUtils.EMPTY,
                    (managedDevice.getIpAddress() != null) ? managedDevice.getIpAddress() : StringUtils.EMPTY,
                    (managedDevice.getHostname() != null) ? managedDevice.getHostname() : StringUtils.EMPTY,
                    (managedDevice.getServiceTag() != null) ? managedDevice.getServiceTag() : StringUtils.EMPTY,
                    (managedDevice.getManufacturer() != null) ? managedDevice.getManufacturer() : StringUtils.EMPTY,
                    (managedDevice.getModel() != null) ? managedDevice.getModel() : StringUtils.EMPTY,
                    (managedDevice.getDeviceType() != null) ? managedDevice.getDeviceType().name() : StringUtils.EMPTY,
                    (managedDevice.getCompliance() != null) ? managedDevice.getCompliance().name() : StringUtils.EMPTY));
                    writer.flush();
                }
            } catch (Exception e) {
                logger.warn("Exception caught writing CSV file for resources", e);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception exception) {
                    logger.warn("Exception caught cleaning up print writer for resources export", exception);
                }
            }
        }
    };    
    
    public FirmwareComplianceReport getFirmwareComplianceReportForDevice(String refId) {
        FirmwareComplianceReport firmwareComplianceReport = null;
        
        if(refId == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(refId));
        }
        
        DeviceInventoryEntity deviceEntity = deviceInventoryDAO.getDeviceInventory(refId);
        User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
        if (deviceEntity == null || !checkUserPermissions(deviceEntity, thisUser)) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(refId));
        }

        firmwareComplianceReport = this.firmwareUtil.getFirmwareComplianceReportForDevice(deviceEntity);
        
        return firmwareComplianceReport;
    }

    @Override
    public UpdateDeviceInventoryResponse[] updateDeviceInventories(List<String> deviceIds) {
        boolean inventoryAll = false;
        List<DeviceInventoryEntity> deviceInventoryEntities = null;
        List<UpdateDeviceInventoryResponse> responses = new ArrayList<>();
        try {
            if (deviceIds != null && deviceIds.size() > 0) {
                deviceInventoryEntities = deviceInventoryDAO.getDevicesByIds(deviceIds);
            } else {
                deviceInventoryEntities = deviceInventoryDAO.getAllDeviceInventory();
                inventoryAll = true;
            }
            User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
            IJobManager jobManager = JobManager.getInstance();
            for (DeviceInventoryEntity deviceInventoryEntity : deviceInventoryEntities) {
                if (inventoryAll
                        && (DeviceType.isBlade(deviceInventoryEntity.getDeviceType())
                        || (DiscoverDeviceType.isIOM(deviceInventoryEntity.getDiscoverDeviceType())
                            && getDeviceInventoryUtils().isChassisDiscoveryDevice(deviceInventoryEntity))))  {
                    continue;
                }
                if (checkUserPermissions(deviceInventoryEntity, thisUser)) {
                    deviceInventoryEntity.setInventoryDate(new GregorianCalendar());
                    ManagedDevice device = DeviceInventoryUtils.toDTO(deviceInventoryEntity, true);
                    JobDetail job = scheduleDeviceInventoryJob(device,jobManager);
                    responses.add(new UpdateDeviceInventoryResponse(job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME)));
                }
            }
            if (!jobManager.getScheduler().isStarted()) {
                jobManager.getScheduler().start();
                logger.info("Job Scheduler has been started");
            }
        } catch (Exception e) {
            logger.error("Exception while updating inventory", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        return (UpdateDeviceInventoryResponse[])responses.toArray(new UpdateDeviceInventoryResponse[responses.size()]);
    }

    
    public FirmwareDeviceInventory[] getFirmwareDeviceInventory(String refId) {

        FirmwareDeviceInventory[] firmwareDeviceInventory = null;
        
        // Load the data
        Set<FirmwareDeviceInventoryEntity> frmDevSet = this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(refId);
        ArrayList<FirmwareDeviceInventoryEntity> frmDevList = new ArrayList<FirmwareDeviceInventoryEntity>(frmDevSet);
        
        // Return the list sorted by Name
        Collections.sort(frmDevList, new Comparator<FirmwareDeviceInventoryEntity>(){
            public int compare(FirmwareDeviceInventoryEntity obj1, FirmwareDeviceInventoryEntity obj2) {
                if (obj1 != null && obj2 == null) return 1;
                else if(obj2 != null && obj1 == null) return -1;
                else return obj1.getName().compareTo(obj2.getName()); 
            }
        });
        
        // Convert to FirmwareDeviceInventory
        firmwareDeviceInventory = new FirmwareDeviceInventory[frmDevList.size()];
        
        for (int x = 0; x < frmDevList.size(); x++) {
            firmwareDeviceInventory[x] = frmDevList.get(x).getFirmwareDeviceInventory();
        }
        
        return firmwareDeviceInventory;
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

    public PuppetModuleUtil getPuppetModuleUtil() {
        if (puppetModuleUtil == null) {
            puppetModuleUtil = new PuppetModuleUtil();
        }
        return puppetModuleUtil;
    }

    public void setPuppetModuleUtil(PuppetModuleUtil puppetModuleUtil) {
        this.puppetModuleUtil = puppetModuleUtil;
    }

}
