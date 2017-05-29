/**************************************************************************
 *   Copyright (c) 2013 - 2016 Dell Inc. All rights reserved.             *
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
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import com.dell.asm.alcm.client.model.WizardStatus;
import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.deployment.AsmDeployerLogEntry;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentDevice;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentFilterResponse;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentHealthStatusType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentNamesType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentValid;
import com.dell.asm.asmcore.asmmanager.client.deployment.IDeploymentService;
import com.dell.asm.asmcore.asmmanager.client.deployment.PuppetLogEntry;
import com.dell.asm.asmcore.asmmanager.client.deployment.RejectedServer;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedNIC;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedServer;
import com.dell.asm.asmcore.asmmanager.client.deployment.ServerNetworkObjects;
import com.dell.asm.asmcore.asmmanager.client.deployment.ServiceDefinition;
import com.dell.asm.asmcore.asmmanager.client.deployment.VM;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReport;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentNamesRefDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentNamesRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentUserRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.VMRefEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerNoServerException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.FirmwareUpdateJob;
import com.dell.asm.asmcore.asmmanager.tasks.ServiceDeploymentJob;
import com.dell.asm.asmcore.asmmanager.util.AsmManagerUtil;
import com.dell.asm.asmcore.asmmanager.util.DeploymentValidator;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidator;
import com.dell.asm.asmcore.asmmanager.util.brownfield.BrownfieldUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.DeploymentEnvironment;
import com.dell.asm.asmcore.asmmanager.util.deployment.DnsUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.FilterEnvironment;
import com.dell.asm.asmcore.asmmanager.util.deployment.HostnameUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.MigrationDeviceUtils;
import com.dell.asm.asmcore.asmmanager.util.deployment.NetworkingUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServerFilteringUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.asmdeployer.client.AsmDeployerComponentStatus;
import com.dell.asm.asmdeployer.client.AsmDeployerStatus;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.model.Link;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.RestUtil;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypoolmgr.ioidentity.impl.IOIdentityMgr;
import com.dell.pg.asm.identitypoolmgr.network.impl.IPAddressPoolMgr;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.PingUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Implementation of Device Inventory REST Service for ASM Manager.
 */
@Path("/Deployment")
public class DeploymentService implements IDeploymentService {

    private static final Logger logger = Logger.getLogger(DeploymentService.class);
    private static final Set<String> validSortColumns = new HashSet<>();
    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();
    private static final String DEFAULT_DEPLOYMENT_ID = "1000";
    private static final String SEVERITY_COLUMN = "severity";

    public static String VM_NAMES = "vm_names";
    public static String OS_HOST_NAMES = "os_host_names";
    public static String STORAGE_NAMES = "storage_volumes";

    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }
 
    static {
        validSortColumns.add("createdDate");
        validSortColumns.add("createdBy");
        validSortColumns.add("updatedDate");
        validSortColumns.add("updatedBy");
        validSortColumns.add("deploymentDesc");
        validSortColumns.add("expirationDate");
        validSortColumns.add("marshalledTemplateData");
        validSortColumns.add("name");
        validSortColumns.add("health");
        validSortColumns.add("server");
    }

    public static final Set<String> validFilterColumns = new HashSet<>();

    static {
        validFilterColumns.add("id");
        validFilterColumns.add("createdDate");
        validFilterColumns.add("createdBy");
        validFilterColumns.add("updatedDate");
        validFilterColumns.add("updatedBy");
        validFilterColumns.add("deploymentDesc");
        validFilterColumns.add("expirationDate");
        validFilterColumns.add("marshalledTemplateData");
        validFilterColumns.add("name");
        validFilterColumns.add("health");
        validFilterColumns.add("server");
    }

    public static final Set<String> validPuppetLogFilterColumns = new HashSet<>();

    static {
        validPuppetLogFilterColumns.add("severity");
    }

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    private final DeploymentDAO deploymentDAO;
    private final DeploymentNamesRefDAO deploymentNamesRefDAO;
    private final DeviceInventoryDAO deviceInventoryDAO;
    private final DeviceInventoryComplianceDAO deviceInventoryComplianceDAO;
    private final ServiceTemplateUtil serviceTemplateUtil ;
    private final GenericDAO genericDAO;
    private final ServiceTemplateDAO serviceTemplateDAO;
    private final AddOnModuleComponentsDAO addOnModuleComponentsDAO;
    private final FirmwareRepositoryDAO firmwareRepositoryDAO;
    private final BrownfieldUtil brownfieldUtil = BrownfieldUtil.getInstance();
    private final FirmwareUtil firmwareUtil;
    private final ServiceDeploymentUtil serviceDeploymentUtil;
    private MigrationDeviceUtils migrationDeviceUtils;

    // For Audit log message creation
    private LocalizableMessageService logService;

    private IPAddressPoolMgr ipAddressPoolMgr;
    private IOIdentityMgr ioIdentityMgr;

    private ServiceTemplateService serviceTemplateService;

    private NetworkingUtil networkingUtil;
    private ServerFilteringUtil filteringUtil;

    private IUserResource adminProxy;
    private IAsmDeployerService asmDeployerProxy;

    private DeviceGroupDAO deviceGroupDAO;
    private INetworkService networkService;

    private IJobManager jobManager;
    private ServiceTemplateValidator serviceTemplateValidator;
    private AsmManagerUtil asmManagerUtil;
    private DeploymentValidator deploymentValidator;


    // Constructor for testing with mocks
    public DeploymentService(DeploymentDAO deploymentDAO,
                             DeviceInventoryDAO deviceInventoryDAO,
                             DeviceInventoryComplianceDAO deviceInventoryComplianceDAO,
                             ServiceTemplateUtil serviceTemplateUtil,
                             LocalizableMessageService logService,
                             IPAddressPoolMgr ipAddressPoolMgr,
                             IOIdentityMgr ioIdentityMgr,
                             INetworkService networkService,
                             ServiceTemplateService serviceTemplateService,
                             GenericDAO genericDAO,
                             AddOnModuleComponentsDAO addOnModuleComponentsDAO,
                             ServiceTemplateValidator validator, 
                             FirmwareUtil firmwareUtil,
                             ServiceTemplateDAO serviceTemplateDAO,
                             FirmwareRepositoryDAO firmwareRepositoryDAO,
                             AsmManagerUtil asmManagerUtil,
                             ServiceDeploymentUtil serviceDeploymentUtil,
                             DeploymentValidator deploymentValidator,
                             DeploymentNamesRefDAO deploymentNamesRefDAO) {
        this.deploymentDAO = deploymentDAO;
        this.deviceInventoryDAO = deviceInventoryDAO;
        this.deviceInventoryComplianceDAO = deviceInventoryComplianceDAO;
        this.serviceTemplateUtil = serviceTemplateUtil;
        this.logService = logService;
        this.ipAddressPoolMgr = ipAddressPoolMgr;
        this.ioIdentityMgr = ioIdentityMgr;
        this.genericDAO = genericDAO;
        this.addOnModuleComponentsDAO = addOnModuleComponentsDAO;
        this.networkingUtil = new NetworkingUtil(new PingUtil(), logService, new DnsUtil());
        this.serviceTemplateService = serviceTemplateService;
        this.brownfieldUtil.setDeviceInventoryDAO(this.deviceInventoryDAO);
        this.networkService = networkService;
        this.serviceTemplateValidator = validator;
        this.firmwareUtil = firmwareUtil;
        this.serviceTemplateDAO = serviceTemplateDAO;
        this.firmwareRepositoryDAO = firmwareRepositoryDAO;
        this.asmManagerUtil = asmManagerUtil;
        this.serviceDeploymentUtil = serviceDeploymentUtil;
        this.deploymentValidator = deploymentValidator;
        this.deploymentNamesRefDAO = deploymentNamesRefDAO;
    }

    public DeploymentService() {
        this(DeploymentDAO.getInstance(),
                new DeviceInventoryDAO(),
                DeviceInventoryComplianceDAO.getInstance(),
                new ServiceTemplateUtil(),
                LocalizableMessageService.getInstance(),
                (IPAddressPoolMgr) IPAddressPoolMgr.getInstance(),
                (IOIdentityMgr) IOIdentityMgr.getInstance(),
                ProxyUtil.getNetworkProxy(),
                new ServiceTemplateService(),
                GenericDAO.getInstance(),
                AddOnModuleComponentsDAO.getInstance(),
                new ServiceTemplateValidator(),
                new FirmwareUtil(),
                ServiceTemplateDAO.getInstance(),
                FirmwareRepositoryDAO.getInstance(),
                new AsmManagerUtil(),
                new ServiceDeploymentUtil(),
                DeploymentValidator.getInstance(),
                new DeploymentNamesRefDAO());

        setMigrationDeviceUtils(new MigrationDeviceUtils());
    }

    public IUserResource getAdminProxy() {
        if (adminProxy == null) {
            setAdminProxy(ProxyUtil.getAdminProxy());
        }
        return adminProxy;
    }

    public void setAdminProxy(IUserResource adminProxy) {
        this.adminProxy = adminProxy;
    }

    public IAsmDeployerService getAsmDeployerProxy() {
        if (asmDeployerProxy == null) {
            setAsmDeployerProxy(ProxyUtil.getAsmDeployerProxy());
        }
        return asmDeployerProxy;
    }

    public void setAsmDeployerProxy(IAsmDeployerService asmDeployerProxy) {
        this.asmDeployerProxy = asmDeployerProxy;
    }

    public DeviceGroupDAO getDeviceGroupDAO() {
        if (deviceGroupDAO == null) {
            setDeviceGroupDAO(DeviceGroupDAO.getInstance());
        }
        return deviceGroupDAO;
    }

    public void setDeviceGroupDAO(DeviceGroupDAO deviceGroupDAO) {
        this.deviceGroupDAO = deviceGroupDAO;
    }

    public IJobManager getJobManager() {
        if (jobManager == null) {
            setJobManager(JobManager.getInstance());
        }
        return jobManager;
    }

    public void setJobManager(IJobManager jobManager) {
        this.jobManager = jobManager;
    }

    public ServerFilteringUtil getFilteringUtil() {
        if (filteringUtil == null) {
            setFilteringUtil(new ServerFilteringUtil());
        }
        return filteringUtil;
    }

    public void setFilteringUtil(ServerFilteringUtil filteringUtil) {
        this.filteringUtil = filteringUtil;
    }

    public static void setProxyHeaders(Object ret, HttpServletRequest servletRequest) {
        if (servletRequest != null) {
            ProxyUtil.setProxyHeaders(ret, servletRequest);
        }
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @Override
    public Deployment getDeployment(String deploymentId) throws WebApplicationException {
        logger.debug("Get Deployment Entered for ID: " + deploymentId);
        Deployment deployment = null;
        if (DEFAULT_DEPLOYMENT_ID.equalsIgnoreCase(deploymentId)) // this helps in testing.
                                                                  // UI has no need for this.
        {
            deployment = new Deployment();
            ServiceTemplate svc = serviceTemplateService.getTemplate(DEFAULT_DEPLOYMENT_ID, false);
            deployment.setServiceTemplate(svc);
            deployment.setDeploymentName("Default Deployment");
            deployment.setId("1000");
            deployment.setCreatedBy("admin");
            deployment.setUpdatedBy("admin");
            return deployment;
        } else {
            try {
                DeploymentEntity deploymentEntity = deploymentDAO.getDeployment(deploymentId,DeploymentDAO.DEVICE_INVENTORY_ENTITIES + DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
                if (deploymentEntity == null) {
                    EEMILocalizableMessage msg = AsmManagerMessages.deploymentNotFound(deploymentId);
                    throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, msg);
                }
                User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
                if (!checkUserPermissions(deploymentEntity, currentUser)) {
                    logger.debug("Refused access to deployment ID=" + deploymentId + " for user " + asmManagerUtil.getUserId() + " because of lack of permissions");
                    throw new LocalizedWebApplicationException(
                            Response.Status.NOT_FOUND,
                            AsmManagerMessages.deploymentNotFound(deploymentId));
                }

                deployment = entityToView(deploymentEntity, currentUser);
                

                if (StringUtils.isNotEmpty(deploymentEntity.getMarshalledTemplateData())) {
                    ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentEntity.getMarshalledTemplateData());
                    deployment.setServiceTemplate(template);
                }
            } catch (LocalizedWebApplicationException e) {
                logger.error("LocalizedWebApplicationException while getting Deployment with ID " + deploymentId, e);
                throw e;
            } catch (Exception e) {
                logger.error("Exception while getting Deployment with ID " + deploymentId, e);
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
            }
        }
        logger.debug("Get Deployment Finished for ID: " + deploymentId);
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        if (serviceTemplate != null) {
            ServiceTemplateUtil.stripPasswords(serviceTemplate, null);
        }
        return deployment;
    }

    @Override
    public Deployment[] getDeploymentsFromDeviceId(String deviceId) {
        logger.debug("Get Deployments for DeviceID: " + deviceId);
        List<Deployment> deployments = new ArrayList<Deployment>();
        DeviceInventoryEntity deviceInventoryEntity = deviceInventoryDAO.getDeviceInventory(deviceId);
        User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
        if (deviceInventoryEntity != null && deviceInventoryEntity.getDeployments() != null) {
            List<DeploymentEntity> deploymentEntities = deviceInventoryEntity.getDeployments();
            if (deploymentEntities != null) {
                for (DeploymentEntity entity : deploymentEntities) {
                    if (checkUserPermissions(entity, currentUser)) { // Only let the user view it if they have access
                        Deployment deployment = entityToView(entity);
                        deployments.add(deployment);
                    }
                }
            }
        }
        return deployments.toArray(new Deployment[deployments.size()]);
    }

    @Override
    public Deployment[] getDeployments(String sort, List<String> filter, Integer offset, Integer limit, Boolean fullTemplates) {

        List<Deployment> deployments = new ArrayList<>();

        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        // Build filter list from filter params ( comprehensive )
        FilterParamParser filterParser = new FilterParamParser(filter, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest, servletResponse, httpHeaders, uriInfo);

        int pageOffSet;
        if (offset == null) {
            pageOffSet = 0;
        } else {
            pageOffSet = offset;
        }

        int totalRecords = 0;
        int pageLimit;
        if (limit == null) {
            pageLimit = paginationParamParser.getMaxLimit();
        } else {
            pageLimit = limit;
        }

        List<DeploymentEntity> entities = deploymentDAO.getAllDeployments(sortInfos, filterInfos, null, DeploymentDAO.DEVICE_INVENTORY_ENTITIES + DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY );

        if (entities != null) {
            if (pageLimit <= 0) {
                pageLimit = entities.size();
            }
            int index = 0;
            int startIndex = pageOffSet * pageLimit;
            int endIndex = startIndex + pageLimit;

            User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
            for (DeploymentEntity entity : entities) {
                if (checkUserPermissions(entity, currentUser)) {
                    totalRecords++;
                    if (index >= startIndex && index <= endIndex) {
                        Deployment deployment = new Deployment();

                        deployment.setId(entity.getId());
                        deployment.setDeploymentName(entity.getName());
                        deployment.setDeploymentDescription(entity.getDeploymentDesc());
                        deployment.setCreatedDate(entity.getCreatedDate());
                        deployment.setCreatedBy(entity.getCreatedBy());
                        deployment.setUpdatedDate(entity.getUpdatedDate());
                        deployment.setUpdatedBy(entity.getUpdatedBy());
                        deployment.setStatus(entity.getStatus());
                        deployment.setCompliant(entity.isCompliant());
                        deployment.setUpdateServerFirmware(entity.isManageFirmware());
                        deployment.setUseDefaultCatalog(entity.isUseDefaultCatalog());

                        FirmwareRepositoryEntity repoEntity = entity.getFirmwareRepositoryEntity();
                        if (repoEntity != null) {
                            deployment.setFirmwareRepositoryId(repoEntity.getId());
                            deployment.setFirmwareRepository(repoEntity.getSimpleFirmwareRepository());
                        }

                        // need service template
                        if (StringUtils.isNotEmpty(entity.getMarshalledTemplateData())) {
                            ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, entity.getMarshalledTemplateData());
                            Map<String, DeviceInventoryEntity> devicesMap = new HashMap<>();
                            for (DeviceInventoryEntity device : entity.getDeployedDevices()) {
                                devicesMap.put(device.getRefId(), device);
                            }
                            // strip heavy weighted items
                            for (ServiceTemplateComponent component : template.getComponents()) {
                                DeviceInventoryEntity deviceEntity = devicesMap.get(component.getAsmGUID());
                                if (deviceEntity != null) {
                                    DeploymentDevice newDevice = new DeploymentDevice();
                                    newDevice.setRefId(deviceEntity.getRefId());
                                    newDevice.setIpAddress(deviceEntity.getIpAddress());
                                    newDevice.setServiceTag(deviceEntity.getServiceTag());
                                    newDevice.setDeviceType(deviceEntity.getDeviceType());
                                    newDevice.setDeviceHealth(deviceEntity.getHealth());
                                    newDevice.setHealthMessage(deviceEntity.getHealthMessage());
                                    newDevice.setCompliantState(this.getCompliantState(entity, deviceEntity));

                                    if (!CompliantState.COMPLIANT.equals(newDevice.getCompliantState())) {
                                        deployment.setCompliant(false);
                                    }
                                    deployment.getDeploymentDevice().add(newDevice);
                                }

                                if (component.getResources() != null && !fullTemplates) {
                                    component.getResources().clear();
                                }

                            }
                            deployment.setServiceTemplate(template);

                        }
                        deployment.setDeploymentHealthStatusType(this.calculateDeploymentHealthStatusType(deployment));

                        deployments.add(deployment);
                    }
                    index++;
                }
            }
        }

        if (httpHeaders != null) {
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

            PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, totalRecords);
            // Common library to add link headers in response headers
            paginationParamParser.addLinkHeaders(paginationInfo, linkBuilder);

            servletResponse.setHeader(AsmConstants.DELL_TOTAL_COUNT_HEADER, String.valueOf(totalRecords));
        }
        return deployments.toArray(new Deployment[deployments.size()]);
    }

    public Deployment entityToView(DeploymentEntity deploymentEntity) {
        User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
        return this.entityToView(deploymentEntity, currentUser);
    }

    private Deployment entityToView(DeploymentEntity deploymentEntity, User thisUser) {
        logger.debug("entityToView " + deploymentEntity.getId() + " " + deploymentEntity.getJobId());
        Deployment deployment = new Deployment();
        deployment.setId(deploymentEntity.getId());
        deployment.setBrownfield(deploymentEntity.isBrownfield());
        deployment.setCreatedBy(deploymentEntity.getCreatedBy());
        deployment.setUpdateServerFirmware(deploymentEntity.isManageFirmware());
        deployment.setUseDefaultCatalog(deploymentEntity.isUseDefaultCatalog());

        if (Hibernate.isInitialized(deploymentEntity.getFirmwareRepositoryEntity())) {
            FirmwareRepositoryEntity repoEntity = deploymentEntity.getFirmwareRepositoryEntity();
            if (repoEntity != null) {
                deployment.setFirmwareRepositoryId(repoEntity.getId());
                deployment.setFirmwareRepository(repoEntity.getSimpleFirmwareRepository());
            }
        }

        deployment.setDeploymentName(deploymentEntity.getName());
        deployment.setDeploymentDescription(deploymentEntity.getDeploymentDesc());

        deployment.setCreatedDate(deploymentEntity.getCreatedDate());
        deployment.setCreatedBy(deploymentEntity.getCreatedBy());
        deployment.setUpdatedDate(deploymentEntity.getUpdatedDate());
        deployment.setUpdatedBy(deploymentEntity.getUpdatedBy());

        deployment.setDeploymentStartedDate(deploymentEntity.getDeploymentStartedDate());
        deployment.setDeploymentFinishedDate(deploymentEntity.getDeploymentFinishedDate());

        boolean powerUser = false;
        if (thisUser == null) {
            // Normally shouldn't be allowed to not have a valid user, but there is no harm in this
            // case as it just changes some of the return values and it makes testing easier.
            powerUser = false;
        } else {
            boolean isAdmin = thisUser.getRole().equals(AsmConstants.USERROLE_ADMINISTRATOR);
            boolean isOwner = thisUser.getUserName().equals(deploymentEntity.getCreatedBy());
            powerUser = isAdmin || isOwner;
        }
        deployment.setOwner(deploymentEntity.getCreatedBy());

        deployment.setCanCancel(powerUser);
        deployment.setCanDelete(powerUser);
        deployment.setCanEdit(powerUser);
        deployment.setCanRetry(powerUser);
        deployment.setCanDeleteResources(powerUser);

        deployment.setCanMigrate(false);
        deployment.setCanScaleupServer(powerUser);
        deployment.setCanScaleupStorage(powerUser);
        deployment.setCanScaleupVM(powerUser);
        deployment.setCanScaleupApplication(powerUser);
        deployment.setCanScaleupCluster(false);
        deployment.setCanScaleupNetwork(powerUser);

        deployment.setAllUsersAllowed(deploymentEntity.isAllUsersAllowed());
        deployment.setAssignedUsers(new HashSet<User>());

        deployment.setTemplateValid(deploymentEntity.isTemplateValid());
        deployment.setStatus(deploymentEntity.getStatus());
        deployment.setCompliant(deploymentEntity.isCompliant());
        
        if (deploymentEntity.getAssignedUserList()!=null) {
            for (DeploymentUserRefEntity ref: deploymentEntity.getAssignedUserList()) {
                try {
                    User user = getAdminProxy().getUser(ref.getUserId());
                    deployment.getAssignedUsers().add(user);
                } catch (LocalizedWebApplicationException lwae) {
                    logger.warn("Could not find User in the Users database for user sequence id: " + ref.getUserId(), lwae);
                }
            }
        }

        if (deploymentEntity.getVmList() != null && deploymentEntity.getVmList().size() > 0) {
            deployment.setVms(new HashSet<VM>());
            for (VMRefEntity currDevice : deploymentEntity.getVmList()) {
                VM newVm = new VM();
                newVm.setCertificateName(currDevice.getVmId());
                newVm.setVmIpaddress(currDevice.getVmIpaddress());
                newVm.setVmManufacturer(currDevice.getVmManufacturer());
                newVm.setVmModel(currDevice.getVmModel());
                newVm.setVmServiceTag(currDevice.getVmServiceTag());
                deployment.getVms().add(newVm);
            }
        }
        
        ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentEntity.getMarshalledTemplateData());
        deployment.setServiceTemplate(template);
        
        // get the status of this job from Ruby service ( i.e. asm-deployer )
        List<AsmDeployerComponentStatus> asmDeployerComponentStatuses = null;

        // If status is pending, deployment has not yet been sent to asm-deployer
        if (DeploymentStatusType.PENDING != deployment.getStatus()) {
            try {
                AsmDeployerStatus dStatus = getAsmDeployerProxy().getDeploymentStatus(deploymentEntity.getId());
                // do not update DB record with this status, as it can be different because of components/firmware compliance
                asmDeployerComponentStatuses = dStatus.getComponents();
            } catch (WebApplicationException e) {
                logger.error("Failed to retrieve deployment " + deploymentEntity.getId() + " status", e);
            }
        }

        if (Hibernate.isInitialized(deploymentEntity.getDeployedDevices())) {

            //Check if any of the associated inventory is currently having its firmware updated
            if (deployment.getStatus() == DeploymentStatusType.COMPLETE) {
                for (DeviceInventoryEntity device : deploymentEntity.getDeployedDevices()) {
                    if (device.getState() == DeviceState.UPDATING) {
                        deployment.setStatus(DeploymentStatusType.IN_PROGRESS);
                        break;
                    }
                }
            }

            if (asmDeployerComponentStatuses != null) {
                deployment.setDeploymentDevice(new HashSet<DeploymentDevice>());
                deployment.setCompliant(deploymentEntity.isCompliant());

                for (AsmDeployerComponentStatus cs : asmDeployerComponentStatuses) {
                    DeploymentDevice newDevice = new DeploymentDevice();
                    newDevice.setRefId(cs.getAsmGuid());
                    newDevice.setRefType(cs.getType().name());
                    newDevice.setComponentId(cs.getId());
                    newDevice.setStatus(cs.getStatus());
                    newDevice.setStatusMessage(cs.getMessage());
                    newDevice.setStatusStartTime(cs.getStartTime());
                    newDevice.setStatusEndTime(cs.getEndTime());

                    // need some data from inventory but don't want to make expensive call to DAO
                    for (DeviceInventoryEntity device : deploymentEntity.getDeployedDevices()) {
                        if (device.getRefId().equals(cs.getAsmGuid())) {
                            newDevice.setIpAddress(device.getIpAddress());
                            newDevice.setServiceTag(device.getServiceTag());
                            newDevice.setDeviceType(device.getDeviceType());
                            newDevice.setDeviceHealth(device.getHealth());
                            newDevice.setHealthMessage(device.getHealthMessage());
                            newDevice.setCompliantState(this.getCompliantState(deploymentEntity, device));
                            
                            if (!CompliantState.COMPLIANT.equals(newDevice.getCompliantState())) {
                                deployment.setCompliant(false);
                            }
                        }
                    }

                    ServiceTemplateComponent templateComponent = (template == null) ?
                            null : template.findComponentById(cs.getId());

                    if (templateComponent != null) {
                        deployment.getDeploymentDevice().add(newDevice);

                        if (ServiceTemplateComponentType.SERVER.equals(cs.getType())) {
                            // Service can migrate if at least one device can migrate.
                            // Service can scale up servers in all cases, we now support scaleUp from scratch. Except Boot from SAN
                            // service can scale up storage if it is not HyperV swim lane and Not Boot from SAN
                            if (templateComponent.hasSanBoot()) {
                                deployment.setCanMigrate(powerUser);
                                deployment.setCanScaleupStorage(false);
                                deployment.setCanScaleupServer(false);
                            }
                            deployment.setVDS(ServiceDeploymentUtil.isVDSService(template, templateComponent));
                        }
                    }
                }
            }
            else {  // null possibly due to Pending status
                if (DeploymentStatusType.PENDING == deployment.getStatus()) {
                    Set<DeviceInventoryEntity> deviceInventoryEntities = deploymentEntity.getDeployedDevices();
                    ArrayList<AsmDeployerComponentStatus> fakeAsmDeployerComponentStatuses = new ArrayList<AsmDeployerComponentStatus>();

                    for (DeviceInventoryEntity deviceInventoryEntity : deviceInventoryEntities) {
                        DeploymentDevice newDevice = new DeploymentDevice();
                        newDevice.setRefId(deviceInventoryEntity.getRefId());
                        newDevice.setStatus(DeploymentStatusType.PENDING);  // Set to Pending since they are scheduled/pending
                        newDevice.setIpAddress(deviceInventoryEntity.getIpAddress());
                        newDevice.setServiceTag(deviceInventoryEntity.getServiceTag());
                        newDevice.setDeviceType(deviceInventoryEntity.getDeviceType());
                        newDevice.setDeviceHealth(deviceInventoryEntity.getHealth());
                        newDevice.setHealthMessage(deviceInventoryEntity.getHealthMessage());
                        newDevice.setCompliantState(CompliantState.fromValue(deviceInventoryEntity.getCompliant()));

                        ServiceTemplateComponent templateComponent = (template == null) ?
                                null : template.findComponentByGUID(deviceInventoryEntity.getRefId());

                        // newDevice.setStatusEndTime(statusEndTime);
                        // newDevice.setStatusMessage(statusMessage);
                        // newDevice.setStatusStartTime(statusStartTime);

                        if (templateComponent != null) {
                            newDevice.setComponentId(templateComponent.getId());
                            newDevice.setRefType(templateComponent.getType().name());
                            deployment.getDeploymentDevice().add(newDevice);

                            if (ServiceTemplateComponentType.SERVER.equals(templateComponent.getType())) {
                                // find corresponding device
                                // Service can migrate if at least one device can migrate.
                                // Service can scale up servers in all cases, we now support scaleUp from scratch. Except Boot from SAN
                                // service can scale up storage if it is not HyperV swim lane and Not Boot from SAN
                                if (templateComponent.hasSanBoot()) {
                                    deployment.setCanMigrate(powerUser);
                                    deployment.setCanScaleupStorage(false);
                                    deployment.setCanScaleupServer(false);
                                }

                            }
                        }
                    }
                }
            }
        }
        
        // get the detailed logs for this job from Ruby service ( i.e. asm-deployer )
        try {
            List<AsmDeployerLogEntry> dLogs = getAsmDeployerProxy().getDeploymentLogs(deploymentEntity.getId());
            deployment.setJobDetails(dLogs);
        } catch(Exception logErr) {
            logger.warn("asm-deployer log service returned exception", logErr);
        }
        
        deployment.setDeploymentHealthStatusType(this.calculateDeploymentHealthStatusType(deployment));

        return deployment;
    }

    @Override
    public List<PuppetLogEntry> getPuppetLogs(String deploymentId, String certName, List<String> filter) {

        // Ensure the User has access to the deployment information
        this.checkUserPermission(deploymentId);
        
        // Return the Puppet Logs
        FilterParamParser filterParser = new FilterParamParser(filter, validPuppetLogFilterColumns);
        final List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        List<PuppetLogEntry> puppetLogs = getAsmDeployerProxy().getAsmPuppetLogs(deploymentId, certName);
        CollectionUtils.filter( puppetLogs, new Predicate(){
            public boolean evaluate( Object input ) {
                if (CollectionUtils.isEmpty(filterInfos)) return true;
                if (!(input instanceof PuppetLogEntry)) return false;

                PuppetLogEntry entry = (PuppetLogEntry) input;
                for (FilterParamParser.FilterInfo filter: filterInfos) {
                    // only care about severity at this time
                    if (filter.getColumnName().equals(SEVERITY_COLUMN)) {
                        if (CollectionUtils.isNotEmpty(filter.getColumnValue())) {
                            return filter.getColumnValue().contains(entry.getSeverity());
                        }else{
                            return true; // any ?
                        }
                    }
                }
                logger.debug("no category column? filtering out puppet log record:" + entry.toString());
                return false;
            }
        } );
        return puppetLogs;
    }
    
    /**
     * Utility method because we need to do this same operation for both a new deployment and do the same thing to an 
     * updated servicetemplate on adjust service call to add new resources.
     * 
     * @param serviceTemplate
     * @param deploymentEntity
     * @param  selectedServers
     * @throws LocalizedWebApplicationException
     */
    private List<SelectedServer> updateFromComponentValues(ServiceTemplate serviceTemplate, DeploymentEntity deploymentEntity,
                                           List<SelectedServer> selectedServers, List<ServiceTemplateComponent> newComponents)  throws LocalizedWebApplicationException
    {
        Map<String, Set<String>> serverRefIdsToComponentsMap = new HashMap<>();
        Map<String, DeviceInventoryEntity> serverInventoryCache = new HashMap<>();
        List<SelectedServer> processedServers = new ArrayList<>();

        try {
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {

                //Used if we are updating a service template and only want to touch newly added components
                if (newComponents != null)
                {
                    boolean isNew = false;
                    for (ServiceTemplateComponent comp : newComponents)
                    {
                        if (comp.getId().equals(component.getId()))
                            isNew = true;
                    }
                    if (!isNew) {
                        if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                            String refId = component.getAsmGUID();
                            if (refId == null) {
                                logger.error("Pre-existing server component does not have a ref id: " +
                                        component.getName() + ", " + component);
                            } else {
                                Set<String> componentNames = serverRefIdsToComponentsMap.get(component.getAsmGUID());
                                if (componentNames == null) {
                                    componentNames = new HashSet<>();
                                    serverRefIdsToComponentsMap.put(component.getAsmGUID(), componentNames);
                                }
                                componentNames.add(component.getName());
                            }
                        }
                        continue;
                    }
                }

                String puppetCertName = null;
                for (ServiceTemplateCategory resource : component.getResources()) {
                    Iterator<ServiceTemplateSetting> iterator = resource.getParameters().iterator();
                    while (iterator.hasNext()) {
                        ServiceTemplateSetting param = iterator.next();
                        logger.trace("Resource: " + resource.getId() + " parameter: " + param.getId());

                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID.equals(param.getId())) {
                            DeviceInventoryEntity theDevice = deviceInventoryDAO.getDeviceInventory(param.getValue());

                            if (theDevice == null || theDevice.getDeviceType() == null || theDevice.getServiceTag() == null) {
                                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                        AsmManagerMessages.missingDeviceInfo(serviceTemplate.getTemplateName(), component.getType().toString()));
                            }

                            deploymentEntity.getDeployedDevices().add(theDevice);

                            if ( DeviceType.isServer(theDevice.getDeviceType()) ) {
                                deviceInventoryDAO.setDeviceState(theDevice, DeviceState.fromDeploymentStatusType(deploymentEntity.getStatus()), true);
                            }

                            component.setAsmGUID(param.getValue());
                            puppetCertName = PuppetModuleUtil.toCertificateName(theDevice);

                        } else {
                            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE.equals(param.getId())) {
                                DeviceInventoryEntity server = null;
                                SelectedServer selectedServer = null;

                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL.equals(param.getValue())) {
                                    ServiceTemplateSetting poolParam = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);

                                    Iterator<SelectedServer> ssI = selectedServers.iterator();
                                    while (ssI.hasNext()) {
                                        SelectedServer ss = ssI.next();
                                        if (ss.getComponentId().equals(component.getId())) {
                                            server = deviceInventoryDAO.getDeviceInventory(ss.getRefId());
                                            processedServers.add(ss);
                                            selectedServer = ss;
                                            ssI.remove(); // must remove so we don't take it again next time, in case we have multiple service instances
                                            break;
                                        }
                                    }
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL.equals(param.getValue())) {
                                    ServiceTemplateSetting serverParam = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION);
                                    server = deviceInventoryDAO.getDeviceInventory(serverParam.getValue());

                                    Iterator<SelectedServer> ssI = selectedServers.iterator();
                                    while (ssI.hasNext()) {
                                        SelectedServer ss = ssI.next();
                                        if (ss.getRefId().equals(server.getRefId())) {
                                            processedServers.add(ss);
                                            selectedServer = ss;
                                            ssI.remove(); // must remove so we don't take it again next time, in case we have multiple service instances
                                            break;
                                        }
                                    }
                                }

                                if (server == null) {
                                    // ups. something got wrong, we should have exact match for selected servers and components
                                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                                }

                                deploymentEntity.getDeployedDevices().add(server);

                                if (DeviceType.isServer(server.getDeviceType())) {
                                    deviceInventoryDAO.setDeviceState(server, DeviceState.fromDeploymentStatusType(deploymentEntity.getStatus()),true);
                                }

                                component.setAsmGUID(server.getRefId());
                                addNICsToNetworkConfiguration(component, selectedServer, false);
                                puppetCertName = PuppetModuleUtil.toCertificateName(server);

                                Set<String> componentNames = serverRefIdsToComponentsMap.get(server.getRefId());
                                if (componentNames == null) {
                                    componentNames = new HashSet<>();
                                    serverRefIdsToComponentsMap.put(server.getRefId(), componentNames);
                                }
                                componentNames.add(component.getName());
                                serverInventoryCache.put(server.getRefId(), server);
                            } else if (ServiceTemplateUtil.isConfirmPassword(param.getId())) {
                                // Remove confirm password fields before passing to backend
                                iterator.remove();
                            }
                        }
                    }

                }

                if (puppetCertName != null) {
                    component.setPuppetCertName(puppetCertName);
                } else if (StringUtils.isEmpty(component.getPuppetCertName())) {
                    // Some cases do not have cert names, such as the case of an application
                    // component attached to multiple VMs or servers.
                    component.setPuppetCertName(component.getId());
                }
          }
            // Ensure duplicate servers haven't been specified
            for (Map.Entry<String, Set<String>> entry : serverRefIdsToComponentsMap.entrySet()) {
                Set<String> componentNames = entry.getValue();
                if (componentNames.size() > 1) {
                    String refId = entry.getKey();
                    DeviceInventoryEntity serverInventory = serverInventoryCache.get(refId);
                    if (serverInventory == null) {
                        serverInventory = deviceInventoryDAO.getDeviceInventory(refId);
                    }
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.duplicateServersChosen(componentNames, serverInventory.getServiceTag()));
                }
            }

            return processedServers;
        }catch(LocalizedWebApplicationException lwe) {
            // rollback attached servers
            logger.error("Device Resevation for deployment " + deploymentEntity.getId() + " failed. Rolling back attached devices and their states");
            for (DeviceInventoryEntity theDevice: deploymentEntity.getDeployedDevices()) {
		    if ( DeviceType.isServer(theDevice.getDeviceType()) ) {
                    deviceInventoryDAO.setDeviceState(theDevice, DeviceState.READY, true);
                }
            }
            deploymentEntity.getDeployedDevices().clear();
            throw lwe;
        }
    }

    /**
     * This will add list of FQDDs from SelectedServer to NetworkConfiguration
     * @param component
     * @param selectedServer
     */
    private void addNICsToNetworkConfiguration(ServiceTemplateComponent component, SelectedServer selectedServer, boolean isMigrate) {

        if (selectedServer.getNics() == null) return;

        if(component.getType() == ServiceTemplateComponentType.SERVER) {
            for (ServiceTemplateCategory resource: component.getResources()) {
                ServiceTemplateSetting networkingSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                if (networkingSetting != null) {
                    com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = null;
                    if (isMigrate) {
                        networkConfig = networkingSetting.getNetworkConfiguration();
                    }else{
                        networkConfig = ServiceTemplateUtil.deserializeNetwork(networkingSetting.getValue());
                    }

                    if (networkConfig != null && networkConfig.getInterfaces() != null) {

                    for (Fabric f : networkConfig.getInterfaces()) {
                        for (SelectedNIC nic : selectedServer.getNics()) {
                            int portNumNic = nic.getPortNumber();
                            if (f.getId().equals(nic.getId())) {
                                for (Interface ifc : f.getInterfaces()) {
                                    String[] ikeys = ifc.getName().split(" ");
                                    if (ikeys.length > 1) {
                                        try {
                                            int portNum = Integer.parseInt(ikeys[1]);
                                            if (portNum == portNumNic) {
                                                ifc.setFqdd(nic.getFqdd());
                                            }
                                        } catch (NumberFormatException e) {
                                            logger.warn("Bad name for interface: " + ifc.getName() + ". Cannot set fqdd.");
                                        }
                                    } else {
                                        logger.warn("Bad name for interface: " + ifc.getName() + ". Cannot set fqdd.");
                                    }
                                }
                            }
                        }
                    }
                    String json = ServiceTemplateUtil.serializeNetwork(networkConfig);
                    networkingSetting.setValue(json);

                    }

                }else{
                    logger.debug("Cannot set FQDDS in template: NetworkConfiguration not found on server component " + component.getId());
                }
            }
        }
    }

    /**
     * Marshal deployment to JSON. Note that this method is used instead of
     * MarshalUtil.toJSON because that method uses the jettison JSON library
     * which has some quirks such as rendering single-element lists as just
     * the element, rather than a list of one element. This method uses
     * the jackson library which is also used by the TomEE container for
     * marshalling JAX-RS calls and does not have that quirk.
     *
     * At some point MarshalUtil should be changed to use jackson and this
     * could be removed.
     *
     * @param deployment The deployment object to marshal
     * @return The JSON-formatted deployment
     */
    public static String toJson(Deployment deployment) {
        try {
            StringWriter sw = new StringWriter();
            OBJECT_MAPPER.writeValue(sw, deployment);
            return sw.toString();
        } catch (IOException e) {
            throw new AsmManagerRuntimeException("Failed to marshal deployment", e);
        }
    }

    /**
     * Unmarshal deployment json data. This is used instead of
     * MarshalUtil.fromJSON because that method uses the jettison JSON library
     * which may not be able to parse the JSON generated by
     * {@link #toJson(com.dell.asm.asmcore.asmmanager.client.deployment.Deployment)}
     * which uses the jackson library. See that method for more details on why
     * that is used.
     *
     * @param json The json text
     * @return The deployment object
     */
    public static Deployment fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Deployment.class);
        } catch (IOException e) {
            throw new AsmManagerRuntimeException("Failed to unmarshal deployment json", e);
        }
    }

    /**
     * Create a Deployment, persist it to the database and initiate a deployment job which invokes asm-deployer on the back-end via a REST service.
     * 
     * <p>
     * The deployment contains a ServiceTemplate. The ServiceTemplate should have been created via ServiceTemplateService and will be persisted.
     * However, the deployment template may contain values that are not identical to the values contained in the persisted template. This is the case
     * when the user enters parameter values at deployment time. As such, the template needs to be re-validated.
     * 
     * @param deployment
     *            The deployment data
     * @return The deployment data including its assigned id
     * @throws WebApplicationException
     *             If any error occurs
     */
    @Override
    public Deployment createDeployment(Deployment deployment) throws WebApplicationException {
        logger.info("Entering createDeployment(Deployment)");

        // Validation
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        if (serviceTemplate == null || serviceTemplate.getId() == null) {
            logger.error("No Service Template found.");
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noServiceTemplateFoundForDeployment());
        }

        ServiceTemplateEntity templateById = serviceTemplateDAO.getTemplateById(serviceTemplate.getId());
        String templateName = "brownfield";

        if(!deployment.isBrownfield()){
            templateName = templateById.getName();
        }
        else{ // It is brownfield so remove components not in inventory as a precaution
            deployment = brownfieldUtil.getDeploymentWithOnlyDevicesInInventory(deployment);
            if(!deployment.getServiceTemplate().containsServiceTemplateComponentOfType(ServiceTemplateComponentType.SERVER)){
                logger.warn("Create deployment failed as there are no servers available for " + deployment.getDeploymentName());
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noServersAvailableForDeployment(deployment.getDeploymentName()));
            }
        }

        if (!deployment.isBrownfield() && templateById == null) {
            logger.error("No Service Template found for id " + templateById + ".");
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noServiceTemplateFoundForDeployment());
        }

        DeploymentEntity duplicateCheck = deploymentDAO.getDeploymentByName(deployment.getDeploymentName(), DeploymentDAO.NONE);
        if (duplicateCheck != null) {
            logger.warn("Create deployment failed, the name " + deployment.getDeploymentName() + " is already in use.");
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.renameToDuplicateName(deployment.getDeploymentName()));
        }

        DeploymentValid deploymentValid = deploymentValidator.validateDeployment(deployment, false);
        if (!deploymentValid.isValid()) {
            logger.error("Validation failed for create Deployment");
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                    new AsmDetailedMessageList(deploymentValid.getMessages()));
        }

        String deploymentId = null;
        DeploymentEntity deploymentEntity = null;

        try
        {
            if(!deployment.isBrownfield()){
                // Replace the passed service template with one from db. The passed template
                // may not contain password values.
                String xmlBlob = templateById.getMarshalledTemplateData();
                ServiceTemplate origTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, xmlBlob);

                // Copy passwords with new ids so that if the service template gets deleted
                // we still have copies
                serviceTemplateUtil.duplicatePasswords(origTemplate);

                // Populate deployment template with passwords from the original template and encrypt
                // any new passwords passed into the deployment template
                serviceTemplateUtil.encryptPasswords(serviceTemplate, origTemplate);

                // Delete out any passwords that may have been overwritten by changes in serviceTemplate
                serviceTemplateUtil.deleteRemovedEncryptionIds(origTemplate, serviceTemplate);

                GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                templateById.setLastDeployedDate(now);
                serviceTemplateDAO.updateTemplate(templateById);

                // Build copy of service template to decrypt the passwords in and use for validation.
                // Otherwise validation against passwords may fail.
                String tempXml = MarshalUtil.marshal(serviceTemplate);
                ServiceTemplate templateToValidate = MarshalUtil.unmarshal(ServiceTemplate.class, tempXml);
                serviceTemplateUtil.decryptPasswords(templateToValidate);

                // to update template options with latest values from inventory
                ServiceTemplateService.fillMissingParams(serviceTemplateService.getDefaultTemplate(), templateToValidate);

                serviceTemplateValidator.validateTemplate(templateToValidate,
                        new ServiceTemplateValidator.ValidationOptions(true, true, true));
                if (!templateToValidate.getTemplateValid().isValid()) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            serviceTemplateValidator.getAllServiceTemplateValidationMessages(templateToValidate));
                }
            }

            JobDetail job = null;
            String jobName = "";
            SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

            int dIx = 1;
            String originalName = deployment.getDeploymentName();
            String origDeploymentXml = MarshalUtil.marshal(deployment);
            Deployment firstDeployment = deployment;
            Date scheduleDate = deployment.getScheduleDate();

            DeploymentFilterResponse filterResponse = null;
            List <SelectedServer> selectedServers = new ArrayList<>();
            if (!deployment.isBrownfield()) {
                filterResponse = filterAvailableServers(serviceTemplate, deployment.getNumberOfDeployments(), true);
                if (filterResponse.getNumberRequestedServers()> filterResponse.getNumberSelectedServers()) {
                    Map<String, String> rejectServersReason = new HashMap<String, String> ();
                    sortRejectServerResponse(rejectServersReason, filterResponse.getRejectedServers(), filterResponse.getNumberSelectedServers());
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.serverRejectForDeployment(rejectServersReason));
                }

                // the list of servers for RAID configuration. The list will be modified: one server taken for each deployment instance
                selectedServers.addAll(filterResponse.getSelectedServers());
            }
            else {
                selectedServers.addAll(brownfieldUtil.getSelectedServersForBrownfield(serviceTemplate));
            }

            for (int i = 0; i< deployment.getNumberOfDeployments(); i++) {
                deploymentId = null; // reset or possible exception will delete wrong deployment
                deploymentEntity = null;

                if (i>0) {
                    String nextName = originalName + " ("+dIx+")";
                    duplicateCheck = deploymentDAO.getDeploymentByName(nextName, DeploymentDAO.NONE);
                    while (duplicateCheck!=null) {
                        dIx++;
                        nextName = originalName + " ("+dIx+")";
                        duplicateCheck = deploymentDAO.getDeploymentByName(nextName, DeploymentDAO.NONE);
                    }

                    // Ensure that each deployment is a unique object so that methods below (such
                    // as ensureResourceHasTitle) won't impact subsequent deployments
                    deployment = MarshalUtil.unmarshal(Deployment.class, origDeploymentXml);
                    serviceTemplate = deployment.getServiceTemplate();
                    deployment.setDeploymentName(nextName);

                    // Subsequent deployments should be staggered.
                    int staggerSecs = AsmManagerApp.getMultiDeploymentsStaggerSecs();
                    if (staggerSecs > 0) {
                        if (scheduleDate == null) {
                            scheduleDate = new Date();
                        }
                        scheduleDate = DateUtils.addSeconds(scheduleDate, staggerSecs);
                    }
                }

                deploymentEntity = new DeploymentEntity();
                deploymentEntity.setName(deployment.getDeploymentName());
                deploymentEntity.setDeploymentDesc(deployment.getDeploymentDescription());
                deploymentEntity.setId(UUID.randomUUID().toString());
                deploymentEntity.setAllUsersAllowed(deployment.isAllUsersAllowed());
                deploymentEntity.setCompliant(false);
                deploymentEntity.setBrownfield(deployment.isBrownfield());

                logger.debug("create the deployment in DB " + deploymentEntity.getId());

                // update deployments table with storage devices in the deployment parameter.
                // also set the component ASMGUID
                List <SelectedServer> processedServers = null;


                try {
                    processedServers = updateFromComponentValues(serviceTemplate, deploymentEntity, selectedServers, null);
                }catch(AsmManagerNoServerException nse) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                          AsmManagerMessages.serversNotAvailableForDeployment(filterResponse.getFailedPoolName()));
                }

                // fill raid config from filter response. Use server list created by step above
                if (raidIsRequired(serviceTemplate)) {
                    setRAIDConfiguration(serviceTemplate.getComponents(), processedServers);
                }

                // Create empty deployment so that we get a deployment id to assign IPs to
                DeploymentEntity deploymentEntityCreated = deploymentDAO.createDeployment(deploymentEntity);
                deploymentId = deploymentEntityCreated.getId();
                deployment.setId(deploymentId);


                if(!deployment.isBrownfield()){ // There is no Network so this will fail for Brownfield
                    processComponentNetworksAndHostnames(deployment, null);
                    processVmNames(deployment.getServiceTemplate(), deployment);
                    processStorageVolumes(deployment);
                    String tempXml = MarshalUtil.marshal(serviceTemplate);
                    ServiceTemplate templateToValidate = MarshalUtil.unmarshal(ServiceTemplate.class, tempXml);
                    serviceTemplateUtil.decryptPasswords(templateToValidate);
                    serviceTemplateValidator.validateTemplate(templateToValidate,
                            new ServiceTemplateValidator.ValidationOptions(true, true, true));
                    if (!templateToValidate.getTemplateValid().isValid()) {
                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                serviceTemplateValidator.getAllServiceTemplateValidationMessages(templateToValidate));
                    }
                }

                // add title if needed
                ensureResourceHasTitle(serviceTemplate);
                ServiceTemplateUtil.ensureRelatedComponents(serviceTemplate);

                //If we're managing the firmware and have a firmware selected, set it.
                boolean manageFirmware = firmwareUtil.manageDeploymentFirmware(deployment, deploymentEntity);

                // now add references
                deploymentEntity.setAssignedUserList(new HashSet<DeploymentUserRefEntity>());
                if (deployment.getAssignedUsers() != null) {
                    for (User user : deployment.getAssignedUsers()) {
                        DeploymentUserRefEntity de = new DeploymentUserRefEntity();
                        de.setUserId(user.getUserSeqId());
                        de.setDeploymentId(deploymentId);
                        de.setId(UUID.randomUUID().toString());
                        deploymentEntity.getAssignedUserList().add(de);
                    }
                }

                List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(true);
                updateAddOnMoulesOnDeployment(addOnModuleComponentEntities,serviceTemplate,deploymentEntity);

                // Parse the ServiceTemplate for Host Names and VM Names
                Map<String, Set<String>> usedDeploymentNames = parseUsedDeploymentNames(deployment.getServiceTemplate());
                updateDeploymentNameRefsOnDeployment(deploymentEntity, usedDeploymentNames);

                deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(serviceTemplate));

                String jsonData = toJson(deployment);

                IJobManager jm = getJobManager();

                /**
                 *  This is a new feature.  Firmware jobs are run as children of the Deployment job.
                 */
                job = jm.createNamedJob(ServiceDeploymentJob.class);
                job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_SERVICE_KEY_DATA, jsonData);

                // Create a trigger and associate it with the schedule, job,
                // and some arbitrary information. The boolean means "start now".
                Trigger trigger;
                if (scheduleDate == null) {
                    logger.info("Scheduling deployment " + deployment.getDeploymentName()
                            + " to begin at " + scheduleDate);
                    trigger = jm.createNamedTrigger(schedBuilder, job, true);
                } else {
                    SimpleTriggerImpl t = new SimpleTriggerImpl();
                    t.setRepeatCount(0);
                    t.setName(UUID.randomUUID().toString());
                    t.setGroup(FirmwareUpdateJob.class.getSimpleName()); // Matches the above trigger creation
                    t.setStartTime(scheduleDate);
                    trigger = t;
                }

                jm.scheduleJob(job, trigger);
                logger.info("checking and starting the scheduler");
                if (!jm.getScheduler().isStarted()) {
                    jm.getScheduler().start();
                    logger.info("scheduler started");
                }
                // Return the job name.
                jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
                // discoveryRequest.setId(jobName);
                // discoveryRequest.setStatus(DiscoveryStatus.INPROGRESS);

                logger.debug("update the deployment in DB if job was successfully created.");
                deploymentEntity.setJobId(jobName);

                logService.logMsg(AsmManagerMessages.deployedServiceTemplate(serviceTemplate.getTemplateName(), deploymentEntity.getName()).getDisplayMessage(),
                        LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);

                deploymentDAO.updateDeployment(deploymentEntity);

                // deployment created. Update appliance status for Getting Started calls.
                WizardStatus ws = ProxyUtil.getAlcmStatusProxy().getWizardStatus();
                if (!ws.getIsDeploymentCompleted()) {
                    ws.setIsDeploymentCompleted(true);
                    ProxyUtil.getAlcmStatusProxy().updateWizardStatus(ws);
                }
            }

            if (servletResponse!=null) {
                servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
                Link jobStatusLink = ProxyUtil.buildJobLink("Deployment", jobName, servletRequest, uriInfo, httpHeaders);
                servletResponse.setHeader("Link", RestUtil.toLinkHeaderString(jobStatusLink));
            }

            return firstDeployment;
        } catch (Exception e) {
            logger.error("Exception while creating deployment", e);
            if (StringUtils.isNotEmpty(deploymentId)) {
                deleteDeployment(deploymentId);
            } else if (deploymentEntity != null && deploymentEntity.getDeployedDevices() != null) {
                // Deployment has not yet been saved to db, but we need to return servers to Available
                for (DeviceInventoryEntity server : deploymentEntity.getDeployedDevices()) {
                    if (DeviceType.isServer(server.getDeviceType())) {
                        deviceInventoryDAO.setDeviceState(server, DeviceState.READY, true);
                    }
                }
            }
            if (e instanceof LocalizedWebApplicationException) {
                throw (LocalizedWebApplicationException) e;
            } else {
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
            }
        }
    }

    public static void updateAddOnMoulesOnDeployment(List<AddOnModuleComponentEntity> addOnModuleComponentEntities, ServiceTemplate serviceTemplate, DeploymentEntity deployment) {
        //remove all addOnModules to clean up the list.
        Map<String, AddOnModuleEntity> currentModuleMap = new HashMap<>();
        for (AddOnModuleEntity addOnModuleEntity : deployment.getAddOnModules()) {
            currentModuleMap.put(addOnModuleEntity.getName(), addOnModuleEntity);
        }
        Set<AddOnModuleEntity> updatedEntites = new HashSet<>();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType().equals(ServiceTemplateComponentType.SERVICE)) {
                for (AddOnModuleComponentEntity aOMComponent : addOnModuleComponentEntities) {
                    if (component.getName().equals(aOMComponent.getName())) {
                        AddOnModuleEntity currentEntity = currentModuleMap.get(aOMComponent.getAddOnModuleEntity().getName());
                        if (currentEntity == null) {
                            currentEntity = aOMComponent.getAddOnModuleEntity();
                        }
                        updatedEntites.add(currentEntity);
                        break;
                    }
                }
            }
        }
        deployment.getAddOnModules().clear();
        deployment.getAddOnModules().addAll(updatedEntites);
    }

    /**
     * return true if any of components require raid configuration
     * @param serviceTemplate
     * @return
     */
    private boolean raidIsRequired(ServiceTemplate serviceTemplate) {
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                for (ServiceTemplateCategory resource: component.getResources()) {
                    ServiceTemplateSetting raidConfig = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID);
                    if (raidConfig!=null && StringUtils.isNotEmpty(raidConfig.getValue())) {
                        String depTarget = raidConfig.getDependencyTarget();
                        String depValue = raidConfig.getDependencyValue();
                        if (StringUtils.isNotEmpty(depTarget) && StringUtils.isNotEmpty(depValue)) {
                            ServiceTemplateSetting target = resource.getParameter(depTarget);
                            if (target != null && Arrays.asList(depValue.split(",")).contains(target.getValue())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * For each server template component with RAID required Find corresponding server from filter response and it's RAID config.
     * @param components
     * @param selectedServers   Warning - this list will be modified!
     */
    private void setRAIDConfiguration(List<ServiceTemplateComponent> components, List <SelectedServer> selectedServers) {
        for (ServiceTemplateComponent component : components) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                for (ServiceTemplateCategory resource : component.getResources()) {
                    ServiceTemplateSetting raidConfig = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID);
                    if (raidConfig != null && StringUtils.isNotEmpty(raidConfig.getValue())) {
                        Iterator <SelectedServer> iterator = selectedServers.iterator();
                        while (iterator.hasNext()) {
                            SelectedServer ss = iterator.next();
                            if (component.getId().equals(ss.getComponentId())) {
                                raidConfig.setRaidConfiguration(ss.getRaidConfiguration());
                                iterator.remove();
                                break;
                            }
                        }
                        if (raidConfig.getRaidConfiguration()==null) {
                            logger.error("RAID Configuration was not set for deployment template component ID=" + component.getId() + ", IP=" + component.getIP());
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                        }
                    }
                }
            }
        }
    }

    private DeploymentEnvironment initCachedEnvironment() {
        DeploymentEnvironment deploymentEnvironment = new DeploymentEnvironment();
        deploymentEnvironment.setDeployments(deploymentDAO.getAllDeployment(DeploymentDAO.DEVICE_INVENTORY_ENTITIES));

        // service context, needed to find out if current user has access to the pool
        deploymentEnvironment.setUserID(asmManagerUtil.getCurrentUser(this.servletRequest).getUserSeqId());

        return deploymentEnvironment;
    }

    /**
     * Auto generate host names if needed
     * Allowed values are ${num} (an auto-generated unique number), ${service_tag}, ${model} and ${vendor}.
     *
     * @param serviceTemplate
     */
    private void processHostnames(ServiceTemplate serviceTemplate, Deployment deployment, String componentId, boolean isMigrate) {
        List<String> reservedHostNames = deploymentNamesRefDAO.getAllNamesByType(DeploymentNamesType.OS_HOST_NAME);
        Set<String> allHostnames = new HashSet<>();
        if (reservedHostNames != null) {
            allHostnames.addAll(reservedHostNames);
        }
        HostnameUtil hostnameUtil = new HostnameUtil();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType() != ServiceTemplateComponentType.SERVER &&
                    component.getType() != ServiceTemplateComponentType.VIRTUALMACHINE) continue;

            if (componentId != null && !componentId.equals(component.getId())) continue;

            DeviceInventoryEntity server = null;
            if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                server =deviceInventoryDAO.getDeviceInventory(component.getAsmGUID());
                if (server == null) {
                    logger.error("Cannot find server in the inventory by refId = " + component.getAsmGUID());
                    throw new AsmManagerRuntimeException("Cannot find server in the inventory by refId = " + component.getAsmGUID());
                }
            }

            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID.equals(param.getId())) {
                        if (param.getValue() != null && param.getValue().equals("true")) {
                            ServiceTemplateSetting htSet = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID);
                            String hostnameTemplate = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_DEFAULT_TEMPLATE;
                            ServiceTemplateSetting hostnameSet = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                            if (hostnameSet == null) {
                                logger.error("Cannot find hostname setting in the service template for component = " + component.getAsmGUID());
                                throw new AsmManagerRuntimeException("Cannot find hostname setting in the service template for component = " + component.getAsmGUID());
                            }
                            if (htSet != null && StringUtils.isNotEmpty(htSet.getValue())) {
                                hostnameTemplate = htSet.getValue();
                            }

                            if (isMigrate && HostnameUtil.mustRegenerateHostname(hostnameTemplate) ) {
                                hostnameSet.setValue(null);
                            }

                            //hostnameSet will be empty string if it's never been set. The hostname will be set already for templates when the deployment was created/updated last.
                            //Want to prevent resetting the hostname.
                            if (StringUtils.isEmpty(hostnameSet.getValue())) {
                                hostnameTemplate = hostnameUtil.generateHostname(hostnameTemplate, component, server, allHostnames);

                                // only check for dups
                                if (!allHostnames.add(hostnameTemplate)) {
                                    logger.error("Duplicate generated hostname: " + hostnameSet.getValue());
                                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                                               AsmManagerMessages.duplicateHostname(hostnameTemplate));
                                } else {
                                    hostnameSet.setValue(hostnameTemplate);
                                    allHostnames.add(hostnameTemplate);
                                }
                            }

                            // last minute check
                            if (!HostnameUtil.isValidHostName(hostnameSet.getValue(), component)) {
                                logger.error("Invalid generated hostname: " + hostnameSet.getValue());
                                throw new LocalizedWebApplicationException(
                                        Response.Status.BAD_REQUEST,
                                        AsmManagerMessages
                                                .invalidHostname(hostnameSet.getValue()));
                            }

                        } else {
                            if (deployment.getNumberOfDeployments() > 1) {
                                logger.error("Auto generate hostname set to false and asked number of deployments > 1: " + deployment.getNumberOfDeployments());
                                throw new LocalizedWebApplicationException(
                                        Response.Status.BAD_REQUEST,
                                        AsmManagerMessages
                                                .mustSetAutoGenerateHostname());

                            }
                        }
                    }
                }
            }
            validateHostname(component);
        }
    }

    /**
     * Validate the hostname set for a component, server or VM, except VM clones.
     * @param component
     */
    private void validateHostname(ServiceTemplateComponent component) {
    	if (component != null 
                && !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_HW.equals(component.getComponentID())
                && !component.hasSanISCSIBoot()) {
    		ServiceTemplateSetting hostnameSetting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
    		if (hostnameSetting != null) {
    			if (StringUtils.isNotEmpty(hostnameSetting.getValue())) {
    				ServiceTemplateSetting repoSetting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
    				if (repoSetting != null && StringUtils.isNotEmpty(repoSetting.getValue())) {
    					String repoName = repoSetting.getValue();
    					String osType = serviceTemplateUtil.findTask(repoName);
    					String hostname = hostnameSetting.getValue(); // will not be null, checked above
    					if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE.equals(osType) ||
    							ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE.equals(osType)) {
    						if (hostname.length() > 15) {
    							throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.hostnameTooLong(hostname));
    						}
    					}
    				}
    			}else{
    				logger.error("Attempt to deploy a server or VM with empty host name: component ID=" + component.getId() + ", GUID=" + component.getAsmGUID());
    				throw new LocalizedWebApplicationException(
    						Response.Status.BAD_REQUEST,
    						AsmManagerMessages
    						.invalidHostname("<empty>"));
    			}
    		}
    	}
    }

    /**
     * Now ensure we have a title for every resource and replace related component id with the new ids
     * @param serviceTemplate
     */
    private void ensureResourceHasTitle(ServiceTemplate serviceTemplate) {
        Map<String, Set<String>> resourceTypeTitles = new HashMap<>();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                boolean foundTitle = false;
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (param.getValue() != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId()) && !param.getValue().isEmpty()) {
                        foundTitle = true;
                        break;
                    }
                }

                if (!foundTitle) {
                    String title = component.getPuppetCertName();
                    String resourceType = resource.getId();
                    if (resourceTypeTitles.get(resourceType) == null) {
                        resourceTypeTitles.put(resourceType, new HashSet<String>());
                    }
                    resourceTypeTitles.get(resourceType).add(title);
                    ServiceTemplateSetting titleParam = new ServiceTemplateSetting();
                    titleParam.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                    titleParam.setValue(title);
                    titleParam.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                    titleParam.setRequired(false);
                    titleParam.setHideFromTemplate(true);
                    resource.getParameters().add(titleParam);
                }
            }
        }
    }

    /**
     *
     * @param deployment
     * @param deploymentToUpdate
     */
    private void updateServiceTemplateServers(Deployment deployment, DeploymentEntity deploymentToUpdate)
    {    	
    	ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
    	
    	ServiceTemplate serviceTemplateOriginal = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentToUpdate.getMarshalledTemplateData());   
    	List<ServiceTemplateComponent> newComponents = getNewComponents(serviceTemplateOriginal, serviceTemplate);
        ServiceTemplate newTemplate = new ServiceTemplate();
        newTemplate.setComponents(newComponents);

        if(!deployment.isBrownfield()) {
            DeploymentFilterResponse filterResponse = filterAvailableServers(newTemplate, 1, true);
            if (filterResponse.getNumberRequestedServers() > filterResponse.getNumberSelectedServers()) {
                Map<String, String> rejectServersReason = new HashMap<String, String> ();
                sortRejectServerResponse(rejectServersReason, filterResponse.getRejectedServers(), filterResponse.getNumberSelectedServers());
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.serverRejectForDeployment(rejectServersReason));
            }
    
            try {
                // this list will mutate. We need it later unchanged.
                List <SelectedServer> processedServers = updateFromComponentValues(serviceTemplate, deploymentToUpdate, filterResponse.getSelectedServers(), newComponents);
                // fill raid config from filter response
                if (raidIsRequired(newTemplate)) {
                    setRAIDConfiguration(newTemplate.getComponents(), processedServers);
                }
    
            }catch(AsmManagerNoServerException nse) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                      AsmManagerMessages.serversNotAvailableForDeployment(filterResponse.getFailedPoolName()));
            }
        }
        else{ // Filtering will not find the required servers (they will show as deployed) so must create for Brownfield
            List<SelectedServer> selectedServers = new ArrayList<SelectedServer>();
            selectedServers.addAll(brownfieldUtil.getSelectedServersForBrownfield(serviceTemplate));
            List <SelectedServer> processedServers = updateFromComponentValues(serviceTemplate, deploymentToUpdate, selectedServers, newComponents);
        }
            

		//After the update some title values of parameters will still not be correct, however they should match the puppetcertname
		for (ServiceTemplateComponent component : newComponents)
		{
			if (ServiceTemplateComponentType.SERVER == component.getType() || ServiceTemplateComponentType.VIRTUALMACHINE == component.getType())
			{
				for (ServiceTemplateCategory resource : component.getResources()) 
				{
					for (ServiceTemplateSetting parameter : resource.getParameters())
					{
						if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(parameter.getId()))
						{ 
							logger.debug("Updating title for asmguid: " + component.getAsmGUID() + " Settgin parameter value to: " + component.getPuppetCertName());
							parameter.setValue(component.getPuppetCertName());
						}
					}
				}
			}
		}

    }
    
    private void sortRejectServerResponse(Map<String, String> rejectServersReason, List<RejectedServer> rejectedServers, int numOfAvailableServers) {
        Collections.sort(rejectedServers, new SortByReason());
        for ( RejectedServer rs : rejectedServers)
            rejectServersReason.put(rs.getIpaddress(), rs.getReason());
        //add overall count of servers failed in the end
        rejectServersReason.put("", rejectedServers.size() + " servers could not be chosen for deployment. Number of available servers: " + numOfAvailableServers);
        
    }
    
    /**
     * Identifies and returns components in the new ServiceTemplate that do not exist in the original ServiceTemplate.
     * 
     * @param originalServiceTemplate the ServiceTemplate typically from the original deployment.
     * @return newServiceTemplate the ServiceTemplate when a Service is redeployed (possibly due to scale up feature) and 
     * 		which may contain new Components that were not in the original deployment.
     */
    private List<ServiceTemplateComponent> getNewComponents(ServiceTemplate originalServiceTemplate, ServiceTemplate newServiceTemplate)
    {
        List<ServiceTemplateComponent> newComponents =  new ArrayList<ServiceTemplateComponent>();
        Iterator<ServiceTemplateComponent> iterator = newServiceTemplate.getComponents().iterator();
        while (iterator.hasNext())
        {
        	ServiceTemplateComponent stc = iterator.next();
        	boolean inOrig = false;
        	
        	for (ServiceTemplateComponent stcOrig : originalServiceTemplate.getComponents())
        	{
        		logger.trace("Comparing persisted id: " + stcOrig.getId() + " Against newer: " + stc.getId());        		
        		if (stcOrig.getId().equals(stc.getId()))
        		{
        			inOrig = true;
        			break;
        		}
        	}
        	
        	if (!inOrig)
        		newComponents.add(stc);
        }
        
        logger.trace("ServiceTemplateOriginal component count: " + originalServiceTemplate.getComponents().size() +
                " Deployment component count: " + newServiceTemplate.getComponents().size() +
                " After removal count: " + newComponents.size());
        
        return newComponents;
    }

    @Override
    public Deployment updateDeployment(String deploymentId, Deployment deployment) throws WebApplicationException {
        logger.debug("update Deployment Entered for deploymentId: " + deploymentId);

        DeploymentEntity deploymentFromDb = null;
        DeploymentStatusType originalDeploymentStatus = null;
        try {
            // WARNING: The call to update the Update the Status to In_Progress MUST be first as it ensures no other 
            //          calls to update a Deployment can be made concurrently.  This call MUST be first in the this
            //          method.  Do not remove it from the start/top of the method call.
            deploymentFromDb = deploymentDAO.getDeployment(deploymentId,DeploymentDAO.ALL_ENTITIES);
            originalDeploymentStatus = deploymentFromDb.getStatus();
            deploymentFromDb = deploymentDAO.updateDeploymentStatusToInProgress(deploymentId);

            // Set the name and description
            deploymentFromDb.setName(deployment.getDeploymentName());
            deploymentFromDb.setDeploymentDesc(deployment.getDeploymentDescription());

            /*
             *  Catalog or service compliance flag changed. No other work is needed.
             */
            if (isFirmwareStatusUpdateCall(deployment, deploymentFromDb)) {
                deploymentFromDb.setStatus(originalDeploymentStatus);
                // Determine whether the firmware is being managed and which repository is used
                firmwareUtil.manageDeploymentFirmware(deployment, deploymentFromDb);

                // We must now save, or the Firmware Repository will not be available later during processing
                deploymentDAO.updateDeployment(deploymentFromDb);

                // Now process the new status
                this.serviceDeploymentUtil.updateDeploymentComplianceAfterCatalogChange(deploymentFromDb);
                
                deployment.setCompliant(deploymentFromDb.isCompliant());
                deployment.setDeploymentHealthStatusType(calculateDeploymentHealthStatusType(deployment));
                logger.debug("Update Deployment Finished for deploymentId: " + deploymentId);
                return deployment;
            }

            DeploymentEntity duplicateCheck = deploymentDAO.getDeploymentByName(deployment.getDeploymentName(), DeploymentDAO.NONE);
            if (duplicateCheck != null && !duplicateCheck.getId().equals(deployment.getId())) {
                logger.warn("Update deployment failed, the name " + deployment.getDeploymentName()
                        + "is already in use.");
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.renameToDuplicateName(deployment.getDeploymentName()));
            }

            ServiceTemplate serviceTemplate = deployment.getServiceTemplate();

            if (!deployment.isTeardown()) {
                DeploymentValid deploymentValid = deploymentValidator.validateDeployment(deployment, true);
                if (deploymentValid != null && !deploymentValid.isValid()) {
                    logger.error("Validation failed for create Deployment");
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            new AsmDetailedMessageList(deploymentValid.getMessages()));
                }
            }

            // If teardown specified, ensure servers are being torn down. That is currently required
            // because servers may be using pool resources, e.g. mac addresses or other virtual
            // identities
            if (deployment.isTeardown() && !deployment.isIndividualTeardown()) {
                if (serviceTemplate != null && serviceTemplate.getComponents() != null) {
                    for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                        if (ServiceTemplateComponentType.SERVER == component.getType()) {
                            if (!component.isTeardown()) {
                                logger.warn("Teardown requested for deployment " + deploymentId
                                        + " but some servers did not have teardown set");
                                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.deploymentTeardownMustIncludeServers());
                            }
                        }
                    }
                }
            }

            // If teardown is requested, ensure that resources marked for teardown are not used in other services
            // CLUSTER and STORAGE types are checked
            if(deployment.isTeardown()){
                validateTeardownRequest(deploymentId, serviceTemplate);
            }

            if(deployment.isBrownfield()) {
                deployment = brownfieldUtil.getDeploymentWithOnlyDevicesInInventory(deployment);
                if(!deployment.getServiceTemplate().containsServiceTemplateComponentOfType(ServiceTemplateComponentType.SERVER)){
                    logger.warn("Update deployment failed as there are no servers available for " + deployment.getDeploymentName());
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noServersAvailableForDeployment(deployment.getDeploymentName()));
                }
            }
            User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);

            if (!checkUserPermissions(deploymentFromDb, currentUser)) {
                logger.debug("Refused access to deployment ID=" + deploymentId + " for user " + asmManagerUtil.getUserId() + " because of lack of permissions");
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.deploymentNotFound(deploymentId));
            }

            for (DeviceInventoryEntity dev : deploymentFromDb.getDeployedDevices()) {
                dev = deviceInventoryDAO.getDeviceInventory(dev.getRefId());
                if (DeviceType.isServer(dev.getDeviceType())) {
                    deviceInventoryDAO.setDeviceState(dev, DeviceState.READY, true);
                }
            }
            deploymentFromDb.getDeployedDevices().clear();
            for (DeploymentDevice currInvDevice : deployment.getDeploymentDevice()) {
                DeviceInventoryEntity theDevice = deviceInventoryDAO.getDeviceInventory(currInvDevice.getRefId());

                if (theDevice != null) {
                    deploymentFromDb.getDeployedDevices().add(theDevice);
                    if (DeviceType.isServer(theDevice.getDeviceType())) {
                        deviceInventoryDAO.setDeviceState(theDevice, DeviceState.fromDeploymentStatusType(deployment.getStatus()), true);
                    }
                }
            }

            //We can't choose new servers from the ui when updating services that include servers.  We need to do it now.
            updateServiceTemplateServers(deployment, deploymentFromDb);

            // this must be followed by updateServiceTemplateServers - as title will be empty at this time, no servers selected yet.
            ensureResourceHasTitle(serviceTemplate);
            ServiceTemplateUtil.ensureRelatedComponents(serviceTemplate);

            //Now encrypt new passwords and keep the ones already present
            ServiceTemplate origTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentFromDb.getMarshalledTemplateData());

            // HACK: strip confirm password fields from original template. Password / confirm
            // password validation really only needs to happen for new passwords passed in, not
            // on saved data. In general these shouldn't be present in the saved template data
            // because they are stripped out as part of updateFromComponentValues, but
            // previous code didn't strip out e.g. the domain confirm password so we might have
            // pre-existing services that still have those fields.
            serviceTemplateUtil.stripConfirmPasswordParameters(origTemplate);
            serviceTemplateUtil.encryptPasswords(serviceTemplate, origTemplate);
            serviceTemplateUtil.setHiddenValues(serviceTemplate);

            if(!deployment.isTeardown() && !deployment.isBrownfield()){
                processComponentNetworksAndHostnames(deployment, origTemplate);
                processVmNames(deployment.getServiceTemplate(), deployment);
                processStorageVolumes(deployment);
            }

            // now that we have updated the service template we need to rerun validation
            if(!deployment.isBrownfield()){
                // we have to ignore existing components and validate only new
                List<String> existingComponents = new ArrayList<>();
                List<ServiceTemplateComponent> newComponents = getNewComponents(origTemplate, serviceTemplate);
                for (ServiceTemplateComponent componentToCheck: serviceTemplate.getComponents()) {
                    if (!newComponents.contains(componentToCheck)) {
                        existingComponents.add(componentToCheck.getId());
                    }
                }

                serviceTemplateValidator.validateTemplate(serviceTemplate,
                        new ServiceTemplateValidator.ValidationOptions(true, !deployment.isConfigurationChange() &&
                                        (deployment.isScaleUp() || (!deployment.isRetry() && !deployment.isTeardown())), true),
                        existingComponents);
            }

            boolean templateValid = serviceTemplate.getTemplateValid().isValid();
            if (!templateValid) {
                if(!deployment.isTeardown() 
                		|| deployment.isIndividualTeardown()) {
                    // service template is invalid dont save and throw error
                    logger.error("Validation failed for deployment service template");
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            serviceTemplateValidator.getAllServiceTemplateValidationMessages(serviceTemplate));
                }
            }

            // reset list of attempted servers
            if (deployment.isRetry()) {
                for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                    if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                        ServiceTemplateSetting setting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS);
                        if (setting!=null)
                            setting.setValue(null);
                    }
                }

                // Update the Components Network Templates with values from the Database for Networks
                ProxyUtil.setProxyHeaders(networkService, servletRequest);

                networkingUtil.updateExistingNetworkTemplates(deployment.getServiceTemplate().getComponents(), networkService);
            }

            // validation passed so update
            if (templateValid) {
                deploymentFromDb.setTemplateValid(Boolean.TRUE);
            }
            deploymentFromDb.setMarshalledTemplateData(MarshalUtil.marshal(serviceTemplate));

            if (deploymentFromDb.getVmList() != null)
            {
                deploymentFromDb.getVmList().clear();
                if (deployment.getVms() != null)
                    for (VM currVm : deployment.getVms()) {
                        VMRefEntity newRefEntity = new VMRefEntity();
                        newRefEntity.setVmId(currVm.getCertificateName());
                        newRefEntity.setDeploymentId(deploymentFromDb.getId());
                        newRefEntity.setVmIpaddress(currVm.getVmIpaddress());
                        newRefEntity.setVmManufacturer(currVm.getVmIpaddress());
                        newRefEntity.setVmModel(currVm.getVmModel());
                        deploymentFromDb.getVmList().add(newRefEntity);
                    }
            }

            deploymentFromDb.setAllUsersAllowed(deployment.isAllUsersAllowed());
            if (deploymentFromDb.getAssignedUserList()!= null) {
                deploymentFromDb.getAssignedUserList().clear();
            }
            else {
                deploymentFromDb.setAssignedUserList(new HashSet<DeploymentUserRefEntity>());
            }

            if (deployment.getAssignedUsers()!= null) {
                for (User user: deployment.getAssignedUsers()) {
                    DeploymentUserRefEntity de = new DeploymentUserRefEntity();
                    de.setUserId(user.getUserSeqId());
                    de.setDeploymentId(deploymentFromDb.getId());
                    de.setId(UUID.randomUUID().toString());
                    deploymentFromDb.getAssignedUserList().add(de);
                }
            }

            List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(true);
            updateAddOnMoulesOnDeployment(addOnModuleComponentEntities,serviceTemplate,deploymentFromDb);

            if (!deployment.isTeardown()) {
                // Parse the new ServiceTemplate Once for Validating Deployment for Host Names, VM Names, and Volume Names
                Map<String,Set<String>> usedDeploymentNames = parseUsedDeploymentNames(deployment.getServiceTemplate());
                updateDeploymentNameRefsOnDeployment(deploymentFromDb, usedDeploymentNames);
            }

            if (deployment.isRetry() || deployment.isTeardown()) {

                JobDetail job;
                String jobName;
                IJobManager jm = getJobManager();
                SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

                job = jm.createNamedJob(ServiceDeploymentJob.class);
                if (deployment.isScaleUp()) {
                    job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_IS_SCALE_UP, true);
                }                 
                
                String jsonData = toJson(deployment);

                job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_SERVICE_KEY_DATA, jsonData);
                job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_INDIVIDUAL_TEARDOWN, deployment.isIndividualTeardown());
                
                // Create a trigger and associate it with the schedule, job,
                // and some arbitrary information. The boolean means "start now".
                Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);
                jm.scheduleJob(job, trigger);
                logger.info("checking and starting the scheduler");
                if (!jm.getScheduler().isStarted()) {
                    jm.getScheduler().start();
                    logger.info("scheduler started");
                }

                // Return the job name.
                jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);

                logger.debug("update the deployment in DB if job was successfully created.");

                if (!deployment.isTeardown()) {
                    // confusing message for actual delete operation
                    EEMILocalizableMessage msg = AsmManagerMessages.deployedServiceTemplate(serviceTemplate.getTemplateName(), deployment.getDeploymentName());
                    logService.logMsg(msg.getDisplayMessage(), LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);
                }

                if (servletResponse != null) {
                    servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
                    Link jobStatusLink = ProxyUtil.buildJobLink("Deployment", jobName, servletRequest, uriInfo, httpHeaders);
                    servletResponse.setHeader("Link", RestUtil.toLinkHeaderString(jobStatusLink));
                }

            }else{
                deploymentFromDb.setStatus(originalDeploymentStatus);
            }

            deploymentDAO.updateDeployment(deploymentFromDb);

            deployment.setDeploymentHealthStatusType(calculateDeploymentHealthStatusType(deployment));

            logger.debug("Update Deployment Finished for deploymentId: " + deploymentId);
            return deployment;
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while updating Deployment " + deploymentId, e);
            try {
                deploymentFromDb = deploymentDAO.getDeployment(deployment.getId(), DeploymentDAO.DEVICE_INVENTORY_ENTITIES);
                // return new servers to Available state
                boolean modified = restoreServersStateOnError(deployment,deploymentFromDb);
                if (deploymentFromDb != null && originalDeploymentStatus != null &&
                        DeploymentStatusType.IN_PROGRESS != originalDeploymentStatus) {
                    deploymentFromDb.setStatus(originalDeploymentStatus);
                    modified = true;
                }
                if (modified) {
                    deploymentDAO.updateDeployment(deploymentFromDb);
                }
            } catch (Exception statusException) {
                logger.warn("Error restoring deploymentFromDb of " + deploymentFromDb.getName() + 
                        " back to originalDeploymentState of " + originalDeploymentStatus.getValue(), statusException);
            }
            
            throw e;
        } catch (Exception e) {
            logger.error("Exception while updating Deployment " + deploymentId, e);
            try {
                deploymentFromDb = deploymentDAO.getDeployment(deployment.getId(), DeploymentDAO.DEVICE_INVENTORY_ENTITIES);
                // return new servers to Available state
                boolean modified = restoreServersStateOnError(deployment,deploymentFromDb);
                if (deploymentFromDb != null && originalDeploymentStatus != null &&
                        DeploymentStatusType.IN_PROGRESS != originalDeploymentStatus) {
                    deploymentFromDb.setStatus(originalDeploymentStatus);
                    modified = true;
                }
                if (modified) {
                    deploymentDAO.updateDeployment(deploymentFromDb);
                }
            } catch (Exception statusException) {
                logger.warn("Error restoring deploymentFromDb of " + deploymentFromDb.getName() + 
                        " back to originalDeploymentState of " + originalDeploymentStatus.getValue(), statusException);
            }
            
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    /**
     * Checks the deployment against the current deploymentEntity.
     *   Verifies:
     *   Change of manage firmware flags
     *   Change of Use Default Catalog flags
     *   Change of firmware repository being managed
     *
     * @param deployment
     * @param deploymentEntity
     * @return
     */
    private boolean isFirmwareStatusUpdateCall(Deployment deployment, DeploymentEntity deploymentEntity) {
        if (deployment != null && deploymentEntity != null) {
            if (deployment.isUpdateServerFirmware() != deploymentEntity.isManageFirmware()) {
                return true;
            }
            if (deployment.isUseDefaultCatalog() != deploymentEntity.isUseDefaultCatalog()) {
                return true;
            }
            final String currentFirmwareId = deploymentEntity.getFirmwareRepositoryEntity() != null ? deploymentEntity.getFirmwareRepositoryEntity().getId() : null;
            if (deployment.getFirmwareRepositoryId() != null) {
                if (!deployment.getFirmwareRepositoryId().equals(currentFirmwareId)) {
                    return true;
                }
            } else if (deployment.getFirmwareRepository() != null) {
                if (!deployment.getFirmwareRepository().getId().equals(currentFirmwareId)) {
                    return true;
                }
            } else if (currentFirmwareId != null) {
                return true;
            }
        } else {
            // if either deployment or deployment entity null then return true because something is wrong.
            return true;
        }
        return false;
    }

    private void validateTeardownRequest(String deploymentId,
            ServiceTemplate serviceTemplate) {
        logger.info("Validating teardown request - checking for shared resources");
        if (serviceTemplate != null && serviceTemplate.getComponents() != null) {
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                if (component.isTeardown()){
                    if (isSharedComponent(deploymentId, component)){
                        logger.warn("Teardown requested for deployment " + deploymentId
                                + " but the component, " + component.getId() + ", is shared");
                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, 
                                AsmManagerMessages.resourceIsSharedAcrossServices(component.getName()));
                    }
                }
            }
        }
    }

    private boolean isSharedComponent(String deploymentId, ServiceTemplateComponent component) {
        ServiceTemplateComponentType componentType = component.getType();
        logger.info(componentType.toString() + " component (" + component.getId() + ") is being scanned across Services");
        if (componentType != ServiceTemplateComponentType.CLUSTER && componentType != ServiceTemplateComponentType.STORAGE){
            return false;
        }
        String asmGuid = null;
        ServiceTemplateCategory resource = null;
        if (component.getResources() != null){
            for (ServiceTemplateCategory compRes : component.getResources()){
                ServiceTemplateSetting asmGuidSetting = compRes.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                if(asmGuidSetting != null){
                    asmGuid = asmGuidSetting.getValue();
                    resource = compRes;
                    break;
                }
            }
        }
        if (resource == null){
            logger.warn(componentType.toString() + " resource could not be found from the deployment, " + deploymentId + ", for teardown validation");
                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, 
                            AsmManagerMessages.internalError());
        }
        String resName = getResourceName(asmGuid, resource, componentType);
        if (resName == null){
            logger.warn(componentType.toString() + " resource name could not be found from the deployment, " + deploymentId + ", for teardown validation");
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, 
                    AsmManagerMessages.internalError());
        }
        logger.debug("Resource to be checked: asm_guid = " + asmGuid + "; resource name = " + resName);
        Deployment[] deployments = null;
        deployments = getDeployments(null, null, null, new Integer(9999), Boolean.TRUE);
        if (deployments != null && deployments.length > 0){
            for (Deployment otherDeployment : deployments){
                logger.debug("Now checking the deployment - " + otherDeployment.getId());
                if (!otherDeployment.getId().equals(deploymentId) && otherDeployment.getServiceTemplate() != null
                        && otherDeployment.getServiceTemplate().getComponents() != null){
                    for (ServiceTemplateComponent c : otherDeployment.getServiceTemplate().getComponents()){
                        logger.debug("Checking component, " + c.getName() + ", in " + otherDeployment.getDeploymentName());
                        if (c.getType() == componentType){
                            List<ServiceTemplateCategory> resources = c.getResources();
                            if (resources != null){
                                for (ServiceTemplateCategory otherResource : resources){
                                    if (otherResource.getId().equals(resource.getId())){
                                        logger.debug("Checking resource, " + otherResource.getId() + ", in component, " + c.getId());
                                        String otherResourceName = getResourceName(asmGuid, otherResource, componentType);
                                        logger.info("Volume name found for " + c.getName() + " in Service, " + otherDeployment.getDeploymentName() + ": " + otherResourceName);
                                        if (resName.equals(otherResourceName)){
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // This private method is helper method for isSharedComponent(), and it checks the resource name only for CLUSTER and STORAGE types
    private String getResourceName(String asmGuid, ServiceTemplateCategory resource, ServiceTemplateComponentType compType) {

        String resourceParamNewNameId = null;
        if (compType == ServiceTemplateComponentType.CLUSTER &&
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID.equals(resource.getId())){
            resourceParamNewNameId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID;
        }else if (compType == ServiceTemplateComponentType.CLUSTER &&
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID.equals(resource.getId())){
            resourceParamNewNameId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID;
        }else if (compType == ServiceTemplateComponentType.STORAGE){
            resourceParamNewNameId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID;
        } else {
            return null;
        }

        String resourceParamExistingNameId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + resourceParamNewNameId;


        if (resource.getParameters() != null && resource.getParameters().size() > 0){
            ServiceTemplateSetting asmGuidSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
            if (asmGuidSetting == null) return null; // not all resources have asm_guid parameter
            if (!asmGuidSetting.getValue().equals(asmGuid)){
                return null;
            }

            for (ServiceTemplateSetting param : resource.getParameters()){
                if (param.getId().equals(resourceParamNewNameId)) {
                    if (compType == ServiceTemplateComponentType.STORAGE){
                        return ServiceTemplateClientUtil.getVolumeNameForStorageComponent(resource);
                    }else if (!ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX.equals(param.getValue())){
                        // don't return "$new"
                        return param.getValue();
                    }
                } else if (param.getId().equals(resourceParamExistingNameId)){
                    return param.getValue();
                }
            }
        }
        return null;
    }
            
    private boolean restoreServersStateOnError(Deployment deployment, DeploymentEntity deploymentFromDb) {
        boolean modified = false;
        try {
            ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
            ServiceTemplate serviceTemplateOriginal = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentFromDb.getMarshalledTemplateData());
            List<ServiceTemplateComponent> newComponents = getNewComponents(serviceTemplateOriginal, serviceTemplate);
            for (ServiceTemplateComponent component : newComponents) {
                if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                    DeviceInventoryEntity entity = deviceInventoryDAO.getDeviceInventory(component.getAsmGUID());
                    if (entity!=null) {
                        deviceInventoryDAO.setDeviceState(entity, DeviceState.READY, true);
                        deploymentFromDb.getDeployedDevices().remove(entity);
                        modified = true;
                    }
                }
            }
        }catch(Exception e) {
            logger.error("updateDeployment: Cannot restore servers to state Available",e);
            // we do not throw error here as we already in exception handler from updateDeployemnt method.
        }
        return modified;
    }

    private void processComponentNetworksAndHostnames(Deployment deployment, ServiceTemplate oldTemplate) throws Exception {
        Deployment deploymentDiff;
        boolean networkScaleUp = false;

        //Always perform manually assigned static ip check
        networkingUtil.validateStaticIpsIfNeeded(deployment.getServiceTemplate().getComponents());

        if (oldTemplate == null) {
            deploymentDiff = deployment;
        } else {
            Map<String, ServiceTemplateComponent> oldComponentsMap = oldTemplate.fetchComponentsMap();
            deploymentDiff = new Deployment();
            deploymentDiff.setId(deployment.getId());
            deploymentDiff.setServiceTemplate(new ServiceTemplate());
            deploymentDiff.getServiceTemplate().setId(deployment.getServiceTemplate().getId());
            Set<String> oldComponentsKeys = oldComponentsMap.keySet();
            for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
                if (!oldComponentsKeys.contains(component.getId())) {
                    deploymentDiff.getServiceTemplate().getComponents().add(component);
                } else {
                    // scale up network case: look for empty static configuration
                    if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                            ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                        ServiceTemplateComponent oldComponent = oldComponentsMap.get(component.getId());
                        if (oldComponent != null) {
                            List<Network> oldNetworks = ServiceTemplateClientUtil.findStaticNetworks(oldComponent);
                            List<Network> newNetworks = ServiceTemplateClientUtil.findStaticNetworks(component);
                            if (oldNetworks.size() != newNetworks.size()) {
                                deploymentDiff.getServiceTemplate().getComponents().add(component);
                                networkScaleUp = true;
                            }
                        }
                    }
                }
            }
        }
        setProxyHeaders(networkService, servletRequest);
        List<Network> reservedNetworks = new ArrayList<>();
        try {
            if (!networkScaleUp) {
                // networking scaleup has the netwrok configuration already updated from the serviceController.addNetworkToService() method
                // therefore, we do not need to massage networks or updateStatic IPs
                networkingUtil.massageNetworks(deploymentDiff.getServiceTemplate().getComponents(), networkService);
                // reserve any manually set static ips and virtual identities - MUST DO THIS BEFORE massageVirtualIdentities
                networkingUtil.updateStaticIpsIfNeeded(deployment.getId(),
                        deploymentDiff.getServiceTemplate().getComponents(),
                        ipAddressPoolMgr,
                        reservedNetworks);
            }
            networkingUtil.massageVirtualIdentities(deploymentDiff, ioIdentityMgr, networkService, ipAddressPoolMgr, reservedNetworks, networkScaleUp);
            networkingUtil.reserveVirtualIdentitiesForServers(deployment.getId(),
                    deploymentDiff.getServiceTemplate().getComponents(),
                    reservedNetworks,
                    ioIdentityMgr);
        } catch (Exception e) {
            // release all ip addresses from new components.
            logger.warn("Releasing all reserved ipaddresses during deployment setup",e);
            networkingUtil.releaseReservedComponentIPAddresses(reservedNetworks ,ipAddressPoolMgr);
            throw e;
        }

        processHostnames(deployment.getServiceTemplate(), deployment, null, false);

    }

    @Override
    public Response deleteDeployment(String deploymentId) throws WebApplicationException {
        // TODO: fail if job is in progress
        logger.debug("Delete Deployment Entered for deploymentId: " + deploymentId);

        try {
            DeploymentEntity deploymentEntity = null;
            try {
                deploymentEntity = deploymentDAO.getDeployment(deploymentId, DeploymentDAO.DEVICE_INVENTORY_ENTITIES);
            } catch (Exception e) {
                logger.warn("Deployment with id " + deploymentId + " does not exist.");
            }
            
            if (deploymentEntity != null) {
                User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
                if (!checkUserPermissions(deploymentEntity, currentUser)) {
                    logger.debug("Refused access to deployment ID=" + deploymentId + " for user " + asmManagerUtil.getUserId() + " because of lack of permissions");
                    throw new LocalizedWebApplicationException(
                            Response.Status.NOT_FOUND,
                            AsmManagerMessages.deploymentNotFound(deploymentId));
                }
                
                try {
                    getAsmDeployerProxy().deleteDeployment(deploymentId);
                } catch (Exception e) {
                    logger.error(" Unable to delete cert names ", e);
                }

                
                // Release any IP/identities reserved at the service level as opposed to the
                // component level; those are released in ServiceDeploymentJob during teardown.
                //
                // This exists to release Hyper-V cluster IPs which are reserved by asm-deployer
                // during cluster creation. These calls may also be useful for customers who have
                // upgraded from an 8.0.1 or older version of ASM where the identity reservations
                // were tied solely to the service id.
                String usageGuid = deploymentEntity.getId();
                try {
                    ipAddressPoolMgr.releaseIPAddressesByUsageId(usageGuid);
                } catch (Exception e) {
                    logger.error("Unable to release IPs for deployment " + usageGuid, e);
                }
                try {
                    ioIdentityMgr.releaseIdentitiesByUsageId(usageGuid);
                } catch (Exception e) {
                    logger.error("Unable to release Identities for deployment " + usageGuid, e);
                }

                // Generally devices are removed during teardown in ServiceDeploymentJob. But
                // some cases such as a deployment that fails to create will have devices that
                // need to be cleaned up here.
                Iterator<DeviceInventoryEntity> iterator = deploymentEntity.getDeployedDevices().iterator();
                while (iterator.hasNext()) {
                    DeviceInventoryEntity device = iterator.next();
                    if (DeviceType.isServer(device.getDeviceType())) {
                        deviceInventoryDAO.setDeviceState(device, DeviceState.READY, true);
                    }
                    iterator.remove();
                }

                // remove this deployment from DB.
                deploymentDAO.deleteDeployment(deploymentId);

                logService.logMsg(AsmManagerMessages.deletedDeployment(deploymentEntity.getName()).getDisplayMessage(), LogMessage.LogSeverity.INFO,
                        LogMessage.LogCategory.DEPLOYMENT);
            }
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while deleting service template " + deploymentId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while deleting service template " + deploymentId, e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        logger.debug("Delete Template Finished for deploymentId: " + deploymentId);
        return Response.noContent().build();

    }

    /**
     * Used to transfer pool name in exception message.
     * @param sPoolID
     * @param deviceGroups
     * @return
     */
    private String getPoolName(String sPoolID, List<DeviceGroupEntity> deviceGroups) {
        if (sPoolID == null) {
            return "No pool specified";
        } else if (sPoolID.compareToIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID) == 0) {
            return ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME;
        } else {
            if (deviceGroups != null) {
                for (int i = 0; i < deviceGroups.size(); i++) {
                    String sID = "" + deviceGroups.get(i).getSeqId();
                    if (sID.compareToIgnoreCase(sPoolID) == 0) {
                        return deviceGroups.get(i).getName();
                    }
                }
            }
        }

        logger.error("getPoolName: no pool found with ID" + sPoolID);
        return "";

    }

    /**
     * Migrate single server comnponent to another server. That will also start new Deployment Job
     * @param deploymentId
     * @param serverComponentId
     * @param serverPoolId
     * @return
     * @throws WebApplicationException
     */
    @Override
    public Deployment migrateDeployment(String deploymentId, String serverComponentId, String serverPoolId) throws WebApplicationException {
        return migrateDeploymentComponent(deploymentId, null, serverComponentId, serverPoolId, true);
    }

    private Deployment migrateDeploymentComponent(String deploymentId, String serverId, String componentId, String serverPoolId, boolean startService) throws WebApplicationException {
        if ((serverId == null && componentId == null) || deploymentId == null) {
            logger.error("Deployment parameters are missed.");
            throw new LocalizedWebApplicationException(
                    Response.Status.BAD_REQUEST,
                    AsmManagerMessages.internalError());
        }

        Deployment deployment = null;
        ServiceTemplate template = null;

        try {
            DeploymentEntity deploymentEntity = null;
            deploymentEntity = deploymentDAO.getDeployment(deploymentId,DeploymentDAO.ALL_ENTITIES);
            
            if (deploymentEntity == null) {
                logger.error("Deployment with id " + deploymentId + " does not exist.");
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.internalError());
            }
            
            User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
            if (!checkUserPermissions(deploymentEntity, currentUser)) {
                logger.debug("Refused access to deployment ID=" + deploymentId + " for user " + asmManagerUtil.getUserId() + " because of lack of permissions");
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.deploymentNotFound(deploymentId));
            }

            deployment = entityToView(deploymentEntity, currentUser);
            logger.trace("Migrate Deployment Entered for deploymentId: " + deploymentId + ", name=" + deployment.getDeploymentName());
            template = MarshalUtil.unmarshal(ServiceTemplate.class, deploymentEntity.getMarshalledTemplateData());
            ensureResourceHasTitle(template);
            deployment.setServiceTemplate(template);

            String serversToMigrate = "";
            String newServersList = "";

            List<String> migrationServers = new ArrayList<>();

                DeviceInventoryEntity oldServer = null;
                DeviceInventoryEntity newServer = null;
                String oldServerRefId = null;
                if (componentId != null) {
                    ServiceTemplateComponent sComp = template.findComponentById(componentId);
                    if (sComp != null)
                        oldServerRefId = sComp.getAsmGUID();
                }
                else {
                    oldServerRefId = serverId;
                }
                if (deploymentEntity.getDeployedDevices() != null && deploymentEntity.getDeployedDevices().size() > 0) {
                    for (DeviceInventoryEntity currDevice : deploymentEntity.getDeployedDevices()) {
                        if (currDevice.getRefId().equals(oldServerRefId)) {
                            oldServer = currDevice;
                            break;
                        }
                    }
                }

                if (oldServer == null) {
                    logger.error("Cannot find server : " + serverId + " in deployment " + deploymentEntity.getId());
                    throw new LocalizedWebApplicationException(
                            Response.Status.BAD_REQUEST,
                            AsmManagerMessages.internalError());
                }
                logger.trace("Migrating FROM server  " + oldServerRefId + "/" +  oldServer.getIpAddress() + "/" + oldServer.getServiceTag() + " in the pool " + serverPoolId);

                if (serversToMigrate.length()>0)
                    serversToMigrate += ",";

                serversToMigrate += oldServer.getServiceTag();

                // find component in template
                ServiceTemplateComponent sComponent = null;
                String puppetCertName = PuppetModuleUtil.toCertificateName(oldServer);
                for (ServiceTemplateComponent component : template.getComponents()) {
                    if (component.getType() == ServiceTemplateComponentType.SERVER) {
                        for (ServiceTemplateCategory resource : component.getResources()) {
                            if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE) ||
                                    resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE)) {
                                ServiceTemplateSetting titleSet = template.getTemplateSetting(resource, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                                if (titleSet.getValue() != null && titleSet.getValue().equals(puppetCertName)) {
                                    sComponent = component;
                                    break;
                                }
                                if (sComponent != null)
                                    break;
                            }
                        }
                    }
                }
                if (sComponent == null) {
                    logger.error("No component with server certname " + puppetCertName + " in ServiceTemplate " + template.getId());
                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                }

                // will need this for error handling
                // Get all pools
                List<DeviceGroupEntity> deviceGroups = null;
                try {
                    deviceGroups = getDeviceGroupDAO().getAllDeviceGroup(null, null, null);
                } catch (AsmManagerCheckedException e) {
                    logger.error("Unable to get device groups", e);
                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                }

                // this will store failed server in attempted servers. We update DB later in this section.
                List<String> attemptedServers = serviceTemplateUtil.addAttemptedServers(sComponent, oldServerRefId);

                // find new server. This will throw an exception if server cannot be found.
                Map<String, DeviceInventoryEntity> newServers = new HashMap<>();
                Map<String, SelectedServer> selectedServersMap = new HashMap<>();
                if (serverPoolId == null) {
                    // must use filtering logic for standard deployment
                    ServiceTemplate newTemplate = new ServiceTemplate();
                    newTemplate.setComponents(Arrays.asList(sComponent));

                    // find max available servers
                    DeploymentFilterResponse filterResponse = filterAvailableServers(newTemplate, -1, true);
                    if (filterResponse.getNumberSelectedServers()>0) {
                        for (SelectedServer ss : filterResponse.getSelectedServers()) {
                            if (attemptedServers.contains(ss.getRefId())) {
                                logger.trace("Migration: ignoring server " + ss.getRefId() + " as it is in attempted servers");
                            }
                            else {
                                newServers.put(ss.getRefId(), deviceInventoryDAO.getDeviceInventory(ss.getRefId()));
                                selectedServersMap.put(ss.getRefId(), ss);
                            }
                        }

                        // fill raid config from filter response
                        if (raidIsRequired(newTemplate)) {
                            List <SelectedServer> selectedServers = new ArrayList<>();
                            selectedServers.addAll(filterResponse.getSelectedServers());
                            setRAIDConfiguration(newTemplate.getComponents(), selectedServers);
                        }
                    }
                } else {
                    // TODO: remove migrationDeviceUtils usage
                    newServers = migrationDeviceUtils.migrateFilterServer(oldServer, serverPoolId, attemptedServers);
                }

                if (newServers == null || newServers.size() == 0) {
                    logger.warn("No server found for this deployment with pool ID " + serverPoolId);
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.serversNotAvailableForDeployment(getPoolName(serverPoolId, deviceGroups)));
                }

                logger.trace("Migration:found " + newServers.values().size() + " servers available");
                for (DeviceInventoryEntity ns: newServers.values()) {
                    if (!migrationServers.contains(ns.getRefId())) {
                        newServer = ns;
                        break;
                    }
                }

                if (newServer == null) {
                    if (newServers.size() > 0) {
                        logger.warn("Cannot migrate deployment " + deploymentId + " - all servers in the pool have been tried out");
                    }
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.serversNotAvailableForDeployment(getPoolName(serverPoolId, deviceGroups)));
                }

                // this will prevent usage of the same server in case we have multiple migration in request
                migrationServers.add(newServer.getRefId());

                if (newServersList.length()>0)
                    newServersList += ",";

                newServersList += newServer.getServiceTag();

                logger.debug("Migrating TO server  " + newServer.getRefId() + "/" +  newServer.getIpAddress() + "/" + newServer.getServiceTag() + " in the pool " + serverPoolId);

                if (DeviceType.isServer(oldServer.getDeviceType())) {
                    deviceInventoryDAO.setDeviceState(oldServer, DeviceState.READY, true); // Q: should it rather go to ERROR?
                }

                String puppetNewCertName = PuppetModuleUtil.toCertificateName(newServer);

                // replace server cert name in all "title" settings of the component
                for (ServiceTemplateCategory resource : sComponent.getResources()) {
                    for (ServiceTemplateSetting setting : resource.getParameters()) {
                        if (setting.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID)) {
                            if (setting.getValue() != null && setting.getValue().equals(puppetCertName)) {
                                setting.setValue(puppetNewCertName);
                            }
                        }
                    }
                }

                // update component certificate
                sComponent.setPuppetCertName(puppetNewCertName);
                sComponent.setAsmGUID(newServer.getRefId());

                String origHostname = sComponent.getParameterValue(
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);

                // required update FQDD only - do not touch IP addresses!
                // this does not apply to servers picked by old migration code - those have identical NICs
                if (selectedServersMap.containsKey(newServer.getRefId())) {
                    addNICsToNetworkConfiguration(sComponent, selectedServersMap.get(newServer.getRefId()), true);
                }
                processHostnames(template, deployment, sComponent.getId(), true);

                deploymentEntity.getDeployedDevices().remove(oldServer);
                deploymentEntity.getDeployedDevices().add(newServer);

            // serialize template. We have to save it before we add "resources" asm::baseserver
                deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(template));

                // add asm::baseserver resource with information about original server
                ServiceTemplateCategory baseServerResource = new ServiceTemplateCategory();
                baseServerResource.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_BASE_SERVER_ID);
                baseServerResource.setDisplayName("Base server for migration");

                ServiceTemplateSetting setId = new ServiceTemplateSetting();
                setId.setId("serialnumber");
                setId.setValue(oldServer.getServiceTag());
                setId.setRequired(false);
                setId.setHideFromTemplate(true);
                setId.setRequiredAtDeployment(false);
                setId.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                baseServerResource.getParameters().add(setId);

                ServiceTemplateSetting setTitle = new ServiceTemplateSetting();
                setTitle.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                setTitle.setValue(puppetCertName);
                setTitle.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setTitle.setRequired(false);
                setTitle.setHideFromTemplate(true);
                setTitle.setRequiredAtDeployment(false);
                baseServerResource.getParameters().add(setTitle);

                if (!StringUtils.isEmpty(origHostname)) {
                    ServiceTemplateSetting setHostname = new ServiceTemplateSetting();
                    setHostname.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                    setHostname.setValue(origHostname);
                    setHostname.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                    setHostname.setRequired(false);
                    setHostname.setHideFromTemplate(true);
                    setHostname.setRequiredAtDeployment(false);
                    baseServerResource.getParameters().add(setHostname);
                }

                sComponent.getResources().add(0, baseServerResource);

            List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(true);
            updateAddOnMoulesOnDeployment(addOnModuleComponentEntities,deployment.getServiceTemplate(),deploymentEntity);

            // Parse the ServiceTemplate for Host Names and VM Names
            Map<String, Set<String>> usedDeploymentNames = parseUsedDeploymentNames(deployment.getServiceTemplate());
            updateDeploymentNameRefsOnDeployment(deploymentEntity, usedDeploymentNames);

            // create a job
            String jobName = deploymentEntity.getJobId();

            if (startService) {
                JobDetail job = null;
                IJobManager jm = getJobManager();
                SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

                job = jm.createNamedJob(ServiceDeploymentJob.class);

                String jsonData = toJson(deployment);

                job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_SERVICE_KEY_DATA, jsonData);
                job.getJobDataMap().put(ServiceDeploymentJob.ServiceDeploymentJob_IS_MIGRATE_DATA, true);

                // Create a trigger and associate it with the schedule, job,
                // and some arbitrary information. The boolean means "start now".
                Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

                jm.scheduleJob(job, trigger);
                logger.info("checking and starting the scheduler");
                if (!jm.getScheduler().isStarted()) {
                    jm.getScheduler().start();
                    logger.info("scheduler started");
                }
                // Return the job name.
                jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);

                logger.debug("update the deployment in DB if job was successfully created.");
                deploymentEntity.setJobId(jobName);
            }else {
                // strip settings not used by asm-deployer. This is normally called by ServiceDeploymentJob
                // note - we don't want to save it in ASM DB
                ServiceDeploymentUtil.prepareTemplateForDeployment(deployment.getServiceTemplate());
            }



            // save updated template along with deployment
            deploymentDAO.updateDeployment(deploymentEntity);

            logService.logMsg(AsmManagerMessages.migratedServer(deployment.getDeploymentName(), serversToMigrate, newServersList).getDisplayMessage(),
                    LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);

            if (servletResponse!=null) {
                servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
                Link jobStatusLink = ProxyUtil.buildJobLink("Deployment", jobName, servletRequest, uriInfo, httpHeaders);
                servletResponse.setHeader("Link", RestUtil.toLinkHeaderString(jobStatusLink));
            }


            return deployment;

        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while migrating server in deployment " + deploymentId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while migrating server in deployment " + deploymentId, e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    /**
     * Check is logged user is allowed to access this deployment.
     * @param entity
     * @return
     */
    private boolean checkUserPermissions(DeploymentEntity entity, User thisUser) {

        if (thisUser.getRole().equals(AsmConstants.USERROLE_READONLY)) return true;

        if (thisUser.getRole().equals(AsmConstants.USERROLE_ADMINISTRATOR)) return true;

        if (entity.isAllUsersAllowed()) return true;

        if (entity.getCreatedBy() != null && entity.getCreatedBy().equals(thisUser.getUserName())) return true;

        for (DeploymentUserRefEntity ref: entity.getAssignedUserList()) {
            if (thisUser.getUserSeqId() ==  ref.getUserId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Filtering service. Returns list of available servers per template components, plus list of rejected servers along with
     * rejection reasons.
     * @param serviceTemplate
     * @param numOfDeployments
     * @return
     * @throws WebApplicationException
     */
    @Override
    public DeploymentFilterResponse filterAvailableServers(ServiceTemplate serviceTemplate,
                                                           int numOfDeployments,
                                                           boolean requireUnique) throws WebApplicationException {

        if (serviceTemplate == null || serviceTemplate.getComponents() == null) {
            logger.error("getAvailableServers called with empty component list or non-positive number of deployments");
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.badParametersForFiltering());
        } else if (serviceTemplate.getComponents().size() < 1) {
            // Exit early to avoid the overhead of the following calls
            return new DeploymentFilterResponse();
        }

        DeploymentEnvironment deploymentEnvironment = initCachedEnvironment();
        deploymentEnvironment.setRequireUnique(requireUnique);

        DeploymentFilterResponse response = new DeploymentFilterResponse();
        response.setSelectedServers(new ArrayList<SelectedServer>());
        response.setRejectedServers(new ArrayList<RejectedServer>());

        // the call to getAvailableServers is very expensive. If we have a service with large number of server components
        // and asking for global pool, or pool with many servers, the UI will time out on template preparation
        // therefore we need some caching for same type of server components
        Map <String, DeploymentFilterResponse> cachedResponse = new HashMap<>();

        String[] serverSources = { ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL };
        int requestedServers = 0;
        serviceTemplateUtil.setHiddenValues(serviceTemplate);
        for (String serverSource : serverSources) {
            List<ServiceTemplateComponent> servers = ServiceTemplateUtil.getServersFromSource(
                    serviceTemplate.getComponents(), serverSource);
            requestedServers += servers.size();
            for (ServiceTemplateComponent server : servers) {
                filterAvailableServersHelper(numOfDeployments, requireUnique, deploymentEnvironment,
                        response, cachedResponse, server, serverSource);
            }
        }

        response.setNumberRequestedServers(requestedServers * numOfDeployments);
        logger.debug("Filtering service: asked " + requestedServers + " in " + numOfDeployments + " deployments, found: " + response.getNumberSelectedServers());


        boolean needLogging = requireUnique;

        Collections.sort(response.getRejectedServers(), new SortByReason());
        String reason = null;
        StringBuilder serversPerReason = new StringBuilder();
        for (RejectedServer rs: response.getRejectedServers()) {
            if (needLogging && !rs.getReason().equals(reason)) {
                if (reason != null) {
                    logService.logMsg(AsmManagerMessages.rejectedServers(serversPerReason.toString(), reason).getDisplayMessage(),
                            LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);
                }
                reason = rs.getReason();
                serversPerReason.setLength(0);
            }
            DeviceInventoryEntity entity = this.deviceInventoryDAO.getDeviceInventory(rs.getRefId());
            String serverStr = (entity!=null)?entity.getIpAddress() + "/" + entity.getServiceTag(): rs.getRefId();
            logger.debug("Rejected server: " + serverStr + " for " + rs.getReason());
            if (serversPerReason.length()>0)
                serversPerReason.append(',');
            serversPerReason.append(serverStr);
        }
        if (needLogging && reason != null) {
            logService.logMsg(AsmManagerMessages.rejectedServers(serversPerReason.toString(), reason).getDisplayMessage(),
                    LogMessage.LogSeverity.INFO, LogMessage.LogCategory.DEPLOYMENT);
        }

        return response;
    }

    private void filterAvailableServersHelper(int numOfDeployments,
                                              boolean requireUnique,
                                              DeploymentEnvironment deploymentEnvironment,
                                              DeploymentFilterResponse response,
                                              Map<String, DeploymentFilterResponse> cachedResponse,
                                              ServiceTemplateComponent component,
                                              String serverSource) {
        FilterEnvironment filterEnvironment = getFilteringUtil().initFilterEnvironment(component, servletRequest);

        Pair<ServiceTemplateCategory, String> source = ServiceTemplateUtil.getServerSourceValue(component, serverSource);
        if (source == null) {
            logger.warn("Cannot filter server without a source: " + component);
            return;
        }

        List<ServiceTemplateSetting> sourceParameters = source.getLeft().getParameters();
        String sourceValue = source.getRight();

        String hashKey = sourceValue + filterEnvironment.hash();
        DeploymentFilterResponse responseLocal = null;
        // we can reuse the same servers. It is the case where we get full server list for manual selection
        if (!requireUnique) {
            responseLocal = cachedResponse.get(hashKey);
        }
        if (responseLocal == null) {
            responseLocal = new DeploymentFilterResponse();
            responseLocal.setSelectedServers(new ArrayList<SelectedServer>());
            responseLocal.setRejectedServers(new ArrayList<RejectedServer>());
            switch (serverSource) {
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL:
                getFilteringUtil().getAvailableServerFromPool(sourceValue, deploymentEnvironment,
                        responseLocal, filterEnvironment, numOfDeployments);
                break;
            case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL:
                getFilteringUtil().getAvailableServers(sourceValue, deploymentEnvironment,
                        responseLocal, filterEnvironment, 1);
                break;
            default:
                logger.warn("Unknown server source " + serverSource + " for " + component);
                break;
            }
            if (!requireUnique) {
                cachedResponse.put(hashKey, responseLocal);
            }
        } else {
            // we got these servers from cache: clone them
            List<SelectedServer> ssList = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            for (SelectedServer s : responseLocal.getSelectedServers()) {
                try {
                    String serverString = mapper.writeValueAsString(s);
                    SelectedServer newSS = mapper.readValue(serverString, SelectedServer.class);
                    ssList.add(newSS);
                } catch (IOException e) {
                    logger.error("Exception while converting filtering response", e);
                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                }
            }
            responseLocal.setSelectedServers(ssList);
        }

        for (SelectedServer s : responseLocal.getSelectedServers()) {
            s.setComponentId(component.getId());
        }

        // if errors, capture server pool ID/name for user message
        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL.equals(serverSource)) {
            if ((responseLocal.getRejectedServers().size() > 0 || responseLocal.getSelectedServers().size() < numOfDeployments)
                    && responseLocal.getFailedPoolId() == null) {
                responseLocal.setFailedPoolId(sourceValue);
                responseLocal.setFailedPoolName(getDeviceGroupDAO().getPoolName(sourceValue));
            }
        }

        // copy response servers
        response.getSelectedServers().addAll(responseLocal.getSelectedServers());
        for (RejectedServer rejectedServer : responseLocal.getRejectedServers()) {
            if (!response.getRejectedServers().contains(rejectedServer)) {
                response.getRejectedServers().add(rejectedServer);
            }
        }

        if (responseLocal.getFailedPoolId() != null) {
            response.setFailedPoolId(responseLocal.getFailedPoolId());
            response.setFailedPoolName(responseLocal.getFailedPoolName());
        }
    }

    private class SortByReason implements java.util.Comparator<RejectedServer> {
        @Override
        public int compare(RejectedServer x, RejectedServer y) {
            return x.getReason().toString().compareTo(y.getReason().toString());
        }
    }
    
    
    /**
     * // Services with only 1x Server (and no other storage / nics / etc)
     *      o Service reflects that Servers Health Status  
     * 
     * // Services with only 1x Server and 2x or more Arrays
     *      o Yellow Status if any Server is out of Compliance
     *      o Red Status if any component is Red  
     * 
     * // Services with More than 1 Server 
     *      o Yellow Status 
     *         - if 2x Servers are Green and Any Server goes Yellow or Red
     *     o Red Status 
     *         - if 1x Server is Green and rest are Yellow
     *         - if 1x server is Green or Yellow, everything else are Red
     *         - if all Servers are Red 
     *     
     * // Services with More than 1 Server and More than 1 Array
     *      o Yellow Status
     *         - if 2x Servers are Green or Yellow and any array goes to Yellow or Red
     *      o Red Status 
     *         - if 1x array or 1x server is Green or Yellow, and everything else are Red, we mark the service Red
     *         - if All Arrays are Red
     * 
     * // Services with only 1x Storage Array
     *      o Service reflects that Arrays' Health Status 
     *    
     * 
     * Firmware compliance status will add to above service health status.  
     * Out of compliance will result in a Yellow status for a component. 
     */
    private DeploymentHealthStatusType calculateDeploymentHealthStatusType(Deployment deployment){
        
        DeploymentHealthStatusType deploymentHealthStatusType = DeploymentHealthStatusType.GREEN;
        
        ArrayList<DeploymentDevice> serverDevices = new ArrayList<DeploymentDevice>();
        Set<DeploymentDevice> storageDevicesSet = new HashSet<DeploymentDevice>();
        Set<String> storageRefIDs = new HashSet<>();
        ArrayList<DeploymentDevice> otherDevices = new ArrayList<DeploymentDevice>();
        ArrayList<DeploymentDevice> allDevices = new ArrayList<DeploymentDevice>();
        
        for (DeploymentDevice deploymentDevice : deployment.getDeploymentDevice()){
            if(deploymentDevice.getDeviceType() != null && 
                    deploymentDevice.getDeviceType().isCalculatedForResourceHealth()) {
                if (DeviceType.isServer(deploymentDevice.getDeviceType())) serverDevices.add(deploymentDevice);
                else if (DeviceType.isStorage(deploymentDevice.getDeviceType())) {
                    if (storageRefIDs.add(deploymentDevice.getRefId())) {
                        storageDevicesSet.add(deploymentDevice);
                    }
                }
                else otherDevices.add(deploymentDevice);

                allDevices.add(deploymentDevice);
            }
        }
        
        // Storage Devices must be unique, so we get a set and then add them to a collection for testing
        ArrayList<DeploymentDevice> storageDevices = new ArrayList<DeploymentDevice>();
        storageDevices.addAll(storageDevicesSet);

        // Any non-Compliance will turn the Service Yellow, ignore if not managed by a firmware repository
        if(!deployment.isCompliant() && deployment.getFirmwareRepositoryId() != null) {
            deploymentHealthStatusType = DeploymentHealthStatusType.YELLOW;
        }
        
        // TODO: ASM-5825 fix for stale compliance value fix once Deployment is updating properly
        for (DeploymentDevice deploymentDevice : allDevices) {
            if (deploymentDevice.getDeviceType().isFirmwareComplianceManaged() && 
                    CompliantState.NONCOMPLIANT.getValue().equals(deploymentDevice.getCompliantState())) {
                deploymentHealthStatusType = DeploymentHealthStatusType.YELLOW;
                break;
            }
        }

//        Scenarios
//        0x Server and 0x Array - CHECK 1
//        0x Server and 1x Array - CHECK 2 
//        0x Server and 2x Array - CHECK 2        
//        0x Server and 2+ Array - CHECK 3 
//
//        1x Server and 0x Array - CHECK 2 
//        1x Server and 1x Array - CHECK 2 
//        1x Server and 2x Array - CHECK 2        
//        1x Server and 2+ Array - CHECK 3
//         
//        2x Server and 0x Array - CHECK 2        
//        2x Server and 1x Array - CHECK 2
//        2x Server and 2x Array - CHECK 2
//        2x Server and 2+ Array - CHECK 5        
//        
//        2+ Server and 0x Array - CHECK 4 
//        2+ Server and 1x Array - CHECK 4 
//        2+ Server and 2x Array - CHECK 5          
//        2+ Server and 2+ Array - CHECK 5 
        
        int serversGreen = this.getDevicesWithHealthStatusCount(serverDevices, DeviceHealth.GREEN);
        int serversYellow = this.getDevicesWithHealthStatusCount(serverDevices, DeviceHealth.YELLOW);
        int serversRed = this.getDevicesWithHealthStatusCount(serverDevices, DeviceHealth.RED);
        int storagesGreen = this.getDevicesWithHealthStatusCount(storageDevices, DeviceHealth.GREEN);
        int storagesYellow = this.getDevicesWithHealthStatusCount(storageDevices, DeviceHealth.YELLOW);
        int storagesRed = this.getDevicesWithHealthStatusCount(storageDevices, DeviceHealth.RED);
        
        int serversUknown = this.getDevicesWithHealthStatusCount(serverDevices, DeviceHealth.UNKNOWN);
        int storagesUknown = this.getDevicesWithHealthStatusCount(storageDevices, DeviceHealth.UNKNOWN);
        
        // UKNOWN status will result in a Yellow status, so add them to number of Yellows for each.
        serversYellow+= serversUknown;
        storagesYellow+= storagesUknown;
        
        // CHECK 1
        if(serverDevices.size() == 0 && storageDevices.size() == 0){
            // Do nothing, we'll just use the compliance for allDevices check above to set the DeploymentHealthStatusType
        }
        // CHECK 2
        else if(serverDevices.size() <= 1 && storageDevices.size() <= 1 ){
            if(this.areAnyDevicesWithHealthStatus(allDevices, DeviceHealth.RED)) deploymentHealthStatusType = DeploymentHealthStatusType.RED;
            else if(this.areAnyDevicesWithHealthStatus(allDevices, DeviceHealth.YELLOW) || 
                    this.areAnyDevicesWithHealthStatus(allDevices, DeviceHealth.UNKNOWN)) {
                deploymentHealthStatusType = DeploymentHealthStatusType.YELLOW;
            }
//            * // Services with only 1x Server (and no other storage / nics / etc)
//            *      o Service reflects that Servers Health Status  
        }
        // CHECK 3
        else if(serverDevices.size() <= 1 && storageDevices.size() >= 2 ){
            if(serversRed > 0 || storagesGreen < 2) deploymentHealthStatusType = DeploymentHealthStatusType.RED;
            else if(storagesGreen >= 2 && (storagesYellow > 0 || storagesRed > 0)) deploymentHealthStatusType = DeploymentHealthStatusType.YELLOW;
//            * // Services with only 1x Server and 2x or more Arrays
//            *      o Yellow Status if any component is out of Compliance
//            *      o Red Status if any component is Red 
        }
        // CHECK 4
        else if(serverDevices.size() >= 2 && storageDevices.size() <= 1){
            if(serversGreen >= 2 && (serversRed > 0 || serversYellow > 0)) deploymentHealthStatusType = DeploymentHealthStatusType.YELLOW;
            else if((serversGreen == 1 && (serversGreen < serverDevices.size())) || serversGreen == 0) deploymentHealthStatusType = DeploymentHealthStatusType.RED;
//            * // Services with More than 1 Server 
//            *      o Yellow Status 
//            *         - if 2x Servers are Green and Any Server goes Yellow or Red
//            *     o Red Status 
//            *         - if 1x Server is Green and rest are Yellow
//            *         - if 1x server is Green or Yellow, everything else are Red
//            *         - if all Servers are Red  
        }
        // CHECK 5
        else if(serverDevices.size() >= 2 && storageDevices.size() >= 2){
            if(serversGreen <= 1 || storagesGreen <= 1) deploymentHealthStatusType = DeploymentHealthStatusType.RED;
            else if((serversGreen >= 2 && storagesGreen >= 2) && 
                    ((serversYellow > 0 || storagesYellow > 0) || (serversRed > 0 || storagesRed > 0))) {
                deploymentHealthStatusType = DeploymentHealthStatusType.YELLOW;
            }
//            *  // Services with More than 1 Server and More than 1 Array
//            *      o Yellow Status
//            *         - if 2x Servers are Green or Yellow and any array goes to Yellow or Red
//            *      o Red Status 
//            *         - if 1x array (or Server) is Green or Yellow, and everything else are Red, we mark the service Red
//            *         - if All Arrays are Red
        }
          
        if(DeploymentStatusType.ERROR.equals(deployment.getStatus())) deploymentHealthStatusType = DeploymentHealthStatusType.RED;
        
        return deploymentHealthStatusType; 
    }
    
    private boolean areAllDevicesCompliant(List<DeploymentDevice> deploymentDevices) {
        boolean isCompliant = true;
        
        for (DeploymentDevice deploymentDevice : deploymentDevices) {
            if (CompliantState.NONCOMPLIANT.getValue().equals(deploymentDevice.getCompliantState())) {
                isCompliant = false;
                break;
            }
        }
        return isCompliant;
    }
    
    // Returns a boolean indicating if ANY device is found with the given DeviceHatlhStatus
    private boolean areAnyDevicesWithHealthStatus(List<DeploymentDevice> deploymentDevices, DeviceHealth deviceHealth) {
        boolean withHealthStatus = false;
        
        for (DeploymentDevice deploymentDevice : deploymentDevices) {
            if (deviceHealth.equals(deploymentDevice.getDeviceHealth())) {
                withHealthStatus = true;
                break;
            }
        }
        return withHealthStatus;
    }
    
    // Returns the number of devices with the given DeviceHealth status type
    private int getDevicesWithHealthStatusCount(List<DeploymentDevice> deploymentDevices, DeviceHealth deviceHealth) {
        int matchingHealthStatusCount = 0;
        
        for (DeploymentDevice deploymentDevice : deploymentDevices) {
            if (deviceHealth.equals(deploymentDevice.getDeviceHealth())) {
                matchingHealthStatusCount++;
            }
        }
        return matchingHealthStatusCount;
    }

    @Override
    public Response exportAllDeployments() throws WebApplicationException {
        Deployment[] deployments = getDeployments("name", ListUtils.EMPTY_LIST, 0, Integer.MAX_VALUE, false);
        StreamingCSVDeploymentOutput csvOutputStream = new StreamingCSVDeploymentOutput(deployments);

        return Response
                .ok(csvOutputStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = deployments.csv")
                .build();
    }

    /*
     * Private utility class to return an output stream which produces a
     * csv formatted response.
     *
     * NOTE: There are no permissions checks done in this class. It assumes
     *       that the deployments passed in have already been filtered based
     *       on user permissions.
     */
    private class StreamingCSVDeploymentOutput implements StreamingOutput {

        private static final String DEPLOYMENT_NAME = "Name";
        private static final String DEPLOYMENT_STATUS = "Status";
        private static final String DEPLOYMENT_BY = "Deployed By";
        private static final String DEPLOYMENT_ON = "Deployed On";
        private static final String DEPLOYMENT_ROW_TEMPLATE = "\"{0}\",\"{1}\",\"{2}\",\"{3}\"\n";

        // Deployments to export
        private Deployment[] deployments;

        public StreamingCSVDeploymentOutput(Deployment[] deployments) {
            this.deployments = deployments;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            final PrintWriter writer = new PrintWriter(outputStream);
            final DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                // add the csv row headers
                writer.write(MessageFormat.format(DEPLOYMENT_ROW_TEMPLATE, DEPLOYMENT_NAME, DEPLOYMENT_STATUS,
                        DEPLOYMENT_BY, DEPLOYMENT_ON));

                // Add detail rows
                if (!ArrayUtils.isEmpty(deployments)) {
                    for (Deployment deployment : deployments) {
                        writer.write(MessageFormat.format(DEPLOYMENT_ROW_TEMPLATE,
                                (!StringUtils.isEmpty(deployment.getDeploymentName()) ? deployment.getDeploymentName() : StringUtils.EMPTY),
                                (deployment.getStatus() != null ? deployment.getStatus() : StringUtils.EMPTY),
                                (!StringUtils.isEmpty(deployment.getCreatedBy()) ? deployment.getCreatedBy() : StringUtils.EMPTY),
                                (deployment.getCreatedDate() != null ? formatter.format(new Timestamp(deployment.getCreatedDate().getTimeInMillis())) : StringUtils.EMPTY)));
                        writer.flush();
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception caught writing CSV service file", e);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception exception) {
                    logger.debug("Exception caught closing print writer", exception);
                }
            }
        }
    }

    private void processVmNames(ServiceTemplate serviceTemplate, Deployment deployment) {
        List<String> reservedVMNames = deploymentNamesRefDAO.getAllNamesByType(DeploymentNamesType.VM_NAME);
        Set<String> allVmNames = new HashSet<>();
        if (reservedVMNames != null) {
            allVmNames.addAll(reservedVMNames);
        }

        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType() != ServiceTemplateComponentType.VIRTUALMACHINE) continue;

            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID.equals(param.getId())) {
                        if (param.getValue() != null && param.getValue().equals("true")) {
                            ServiceTemplateSetting vmNameTemplateSetting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_TEMPLATE_ID);
                            String vmNameTemplate;
                            ServiceTemplateSetting vmNameSetting = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
                            if (vmNameSetting == null) {
                                logger.error("Cannot find VM Name setting in the service template for component = " + component.getAsmGUID());
                                throw new AsmManagerRuntimeException("Cannot find VM Name setting in the service template for component = " + component.getAsmGUID());
                            }

                            if (StringUtils.isEmpty(vmNameSetting.getValue())) {
                                if(vmNameTemplateSetting != null && StringUtils.isNotEmpty(vmNameTemplateSetting.getValue())){
                                    vmNameTemplate = vmNameTemplateSetting.getValue();
                                }
                                else{
                                    logger.error("Cannot find VM Name template setting in the service template for component = " + component.getAsmGUID());
                                    throw new AsmManagerRuntimeException("Cannot find VM Name template setting in the service template for component = " + component.getAsmGUID());
                               
                                }

                                vmNameTemplate = HostnameUtil.generateNameFromNumTemplate(vmNameTemplate, allVmNames);
      
                                // only check for dups
                                if (!allVmNames.add(vmNameTemplate)) {
                                    logger.error("Duplicate generated hostname: " + vmNameTemplateSetting.getValue());
                                    throw new LocalizedWebApplicationException(
                                            Response.Status.BAD_REQUEST,
                                            AsmManagerMessages
                                                    .duplicateHostname(vmNameTemplate)
                                    );
                                } else {
                                    vmNameSetting.setValue(vmNameTemplate);
                                    allVmNames.add(vmNameTemplate);
                                }
                            }

                            // last minute check
                            if (!ASMCommonsUtils.isValidVmName(vmNameSetting.getValue())) {
                                logger.error("Invalid generated vmname: " + vmNameSetting.getValue());
                                throw new LocalizedWebApplicationException(
                                        Response.Status.BAD_REQUEST,
                                        AsmManagerMessages
                                                .invalidVmName(vmNameSetting.getValue()));
                            } 
                        }
                    }
                    
                }
            }
        }
    }

    private void processStorageVolumes(Deployment deployment) {
        ServiceTemplate serviceTemplate = deployment.getServiceTemplate();
        List<String> reservedNames = deploymentNamesRefDAO.getAllNamesByType(DeploymentNamesType.STORAGE_VOLUME_NAME);
        Set<String> allNames = new HashSet<>();
        if (reservedNames != null) {
            allNames.addAll(reservedNames);
        }

        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (!ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE.equals(component.getType())) continue;

            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId())) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(param.getValue())) {
                            ServiceTemplateSetting nameTemplateSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_TEMPLATE);
                            String nameTemplate;
                            ServiceTemplateSetting nameSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_GENERATED);
                            // only generate if empty
                            if (StringUtils.isEmpty(nameSetting.getValue())) {
                                if(nameTemplateSetting != null && StringUtils.isNotEmpty(nameTemplateSetting.getValue())){
                                    nameTemplate = nameTemplateSetting.getValue();
                                }
                                else{
                                    String err = "Cannot find volume name template setting in the service template for component = " + component.getAsmGUID();
                                    logger.error(err);
                                    throw new AsmManagerRuntimeException(err);
                                }

                                // same logic as for VM names autogenerator
                                nameTemplate = HostnameUtil.generateNameFromNumTemplate(nameTemplate, allNames);

                                // only check for dups
                                if (!allNames.add(nameTemplate)) {
                                    logger.error("Duplicate generated volume name: " + nameTemplateSetting.getValue());
                                    throw new LocalizedWebApplicationException(
                                            Response.Status.BAD_REQUEST,
                                            AsmManagerMessages
                                                    .duplicateVolumeName(nameTemplate)
                                    );
                                } else {
                                    nameSetting.setValue(nameTemplate);
                                    nameSetting.setHideFromTemplate(false);
                                    allNames.add(nameTemplate);
                                }
                            }
                        }else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT.equals(param.getValue())) {
                            // change visibility for deployent time parameters - otherwise they won't show up in service details
                            ServiceTemplateSetting nameSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW_AT_DEPLOYMENT);
                            nameSetting.setHideFromTemplate(false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Deployment migrateServerComponent(String serviceId, String componentId) throws WebApplicationException {
        return migrateDeploymentComponent(serviceId, null, componentId, null, false);
    }
    
    /**
     * Returns a Deployment that is populated based on the data requested in the ServiceDefinition from a brownfield
     * environment.
     * 
     * @param serviceDefinition definition of the service that will be queried and created (if possible)
     */
    @Override
    public Deployment defineService(ServiceDefinition serviceDefinition) {
        return BrownfieldUtil.getInstance().defineService(serviceDefinition);
    }
    
    /**
     * Identifies the differences between the existing service in ASM and a new Brownfield discovery and returns a 
     * deployment that captures the differences.  New components are added to the template.  All of the Devices will
     * have a BrownfieldStatus that accurately represents the status with relation of the new components and components
     * that are part of the existing service in ASM. 
     * 
     * @see com.dell.asm.asmcore.asmmanager.client.deployment.BrownfieldStatus
     * 
     * @param serviceId the id of the existing Brownfield service in ASM.
     */
    @Override
    public Deployment defineServiceDiff(String serviceId) throws WebApplicationException {

        Deployment currentService = this.getDeployment(serviceId);
        return BrownfieldUtil.getInstance().defineServiceDiff(currentService);
    }

    /**
     * Wrapper for asm_depployer call to get)_server_info.
     * Returns Port View topology for given service and server.
     * @param serviceId
     * @param serverComponentId
     * @return
     * @throws WebApplicationException
     */
    @Override
    public ServerNetworkObjects getServerNetworkObjects (String serviceId, String serverComponentId) throws WebApplicationException {

        // Verify the User hass access
        this.checkUserPermission(serviceId);
        
        try {
            return getAsmDeployerProxy().getServerNetworkObjects(serviceId, serverComponentId);
        }catch(WebApplicationException wex) {
            logger.error("asm_deployer get_server_info failed", wex);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    // Checks to see if the user has access to the given deployment and throws an error if the user does not have access
    private void checkUserPermission(String deploymentId) {
        try {
            DeploymentEntity deploymentEntity = deploymentDAO.getDeployment(deploymentId,DeploymentDAO.NONE);
            if (deploymentEntity == null) {
                EEMILocalizableMessage msg = AsmManagerMessages.deploymentNotFound(deploymentId);
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, msg);
            }

            this.checkUserPermission(deploymentEntity);
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while getting Deployment with ID " + deploymentId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while getting Deployment with ID " + deploymentId, e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
    }

    private void checkUserPermission(DeploymentEntity deploymentEntity) {

        User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
        if (!checkUserPermissions(deploymentEntity, currentUser)) {
            logger.debug("Refused access to deployment ID=" + deploymentEntity.getId() + " for user " + asmManagerUtil.getUserId() + " because of lack of permissions");
            throw new LocalizedWebApplicationException(
                    Response.Status.NOT_FOUND,
                    AsmManagerMessages.deploymentNotFound(deploymentEntity.getId()));
        }
    }

    /* (non-Javadoc)
     * @see com.dell.asm.asmcore.asmmanager.client.deployment.IDeploymentService#getDeploymentsForNetworkId(java.lang.String)
     */
    @Override
    public Deployment[] getDeploymentsForNetworkId(String networkId) throws WebApplicationException {
        logger.debug("Get Deployments for NetworkId: " + networkId);
        ArrayList<Deployment> deploymentsList = new ArrayList<Deployment>();
        
        if(networkId == null) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, 
                    AsmManagerMessages.invalidParam(networkId)); 
        }
        
        List<DeploymentEntity> deploymentEntities = this.deploymentDAO.getAllDeployment(DeploymentDAO.NONE);
        User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
        
        deploymentLoop:
        for(DeploymentEntity deploymentEntity : deploymentEntities) {
            ServiceTemplate serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, 
                                                                    deploymentEntity.getMarshalledTemplateData());
            Set<String> networkIds = ServiceTemplateClientUtil.getNetworkIds(serviceTemplate);
            
            for(String networkIdToMatch : networkIds) {
                if(networkId.equals(networkIdToMatch)) {
                    deploymentsList.add(this.entityToView(deploymentEntity, currentUser));
                    continue deploymentLoop;
                }
            }
        }
        
        return deploymentsList.toArray(new Deployment[deploymentsList.size()]);
    }

    @Override
    public Response deleteUsers(List<String> userIds) throws WebApplicationException {
        if (userIds == null || userIds.size() <= 0) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                    AsmManagerMessages.invalidParam("userIds"));
        }
        Set<String> userIdsSet = new HashSet<>(userIds);
        List<DeploymentEntity> deploymentEntities = deploymentDAO.getDeploymentsForUserIds(userIdsSet);
        if (deploymentEntities != null && deploymentEntities.size() > 0) {
            for (DeploymentEntity entity : deploymentEntities) {
                if (entity.getAssignedUserList() != null && entity.getAssignedUserList().size() > 0) {
                    boolean updateEntity = false;
                    Set<DeploymentUserRefEntity> updatedUsers = new HashSet<>();
                    for (DeploymentUserRefEntity user : entity.getAssignedUserList()) {
                        if (userIdsSet.contains(Long.toString(user.getUserId()))) {
                            updateEntity = true;
                        } else {
                            updatedUsers.add(user);
                        }
                    }
                    if (updateEntity) {
                        entity.setAssignedUserList(updatedUsers);
                        try {
                            deploymentDAO.updateDeployment(entity);
                        } catch (LocalizedWebApplicationException e) {
                            logger.error("LocalizedWebApplicationException while updating Deployment " + entity.getId(), e);
                            throw e;
                        } catch (Exception e) {
                            logger.error("Exception while updating Deployment " + entity.getId(), e);
                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                        }
                    }
                }
            }
        }
        return Response.noContent().build();
    }

    public static Map<String, Set<String>> parseUsedDeploymentNames(ServiceTemplate serviceTemplate) {
        Map<String, Set<String>> usedDeploymentNames = new HashMap<>();

        Set<String> vmNames = new HashSet<>();
        Set<String> hostNames = new HashSet<>();
        Set<String> storageNames = new HashSet<>();

        if (serviceTemplate.getComponents() != null) {
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                switch (component.getType()) {
                case VIRTUALMACHINE:
                    ServiceTemplateSetting vmNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                                                                                  ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
                    if (vmNameSetting == null) { // means there is no vm for vcenter, so try hyperv now
                        vmNameSetting = 
                                component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE,
                                                       ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
                    }
                    
                    if (vmNameSetting != null) {
                        if (StringUtils.isNotBlank(vmNameSetting.getValue()))
                        {
                            // keep track of duplicates
                            vmNames.add(vmNameSetting.getValue());
                        }
                        // we found the setting so we should break
                        break;
                    }
                    //Setting was not found so fall through to look for hostname.
                case SERVER:
                    ServiceTemplateSetting osHostNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                                                                                      ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                    if (osHostNameSetting != null && StringUtils.isNotBlank(osHostNameSetting.getValue()) && !BrownfieldUtil.NOT_FOUND.equals(osHostNameSetting.getValue())) {
                        hostNames.add(osHostNameSetting.getValue());
                    }
                    break;
                case STORAGE:
                    for (String storageType : ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_RESOURCE_LIST) {
                        ServiceTemplateCategory storageCat = component.getTemplateResource(storageType);
                        if (storageCat != null && ServiceTemplateClientUtil.isNewStorageVolume(storageCat, true)) {
                            String volume = ServiceTemplateClientUtil.getVolumeNameForStorageComponent(storageCat);
                            if (StringUtils.isNotBlank(volume)
                                    && !BrownfieldUtil.NOT_FOUND.equals(volume)) {
                                storageNames.add(volume);
                                break; // we can have only one volume per component
                            }
                        }
                    }
                    break;

                default:
                    break;
                }
            }
        }

        usedDeploymentNames.put(OS_HOST_NAMES, hostNames);
        usedDeploymentNames.put(VM_NAMES, vmNames);
        usedDeploymentNames.put(STORAGE_NAMES, storageNames);
        return usedDeploymentNames;
    }

    public static void updateDeploymentNameRefsOnDeployment(DeploymentEntity deploymentEntity, Map<String, Set<String>> usedDeploymentNames) {
        if (usedDeploymentNames != null) {
            Set<String> hostNames = usedDeploymentNames.get(OS_HOST_NAMES);
            Set<String> vmNames = usedDeploymentNames.get(VM_NAMES);
            Set<String> storageNames = usedDeploymentNames.get(STORAGE_NAMES);

            if ((hostNames != null && !hostNames.isEmpty()) ||
                    (vmNames != null  && !vmNames.isEmpty()) ||
                    (storageNames != null  && !storageNames.isEmpty())) {
                Map<String, DeploymentNamesRefEntity> usedHostNamesMap = new HashMap<>();
                Map<String, DeploymentNamesRefEntity> usedVMNamesMap = new HashMap<>();
                Map<String, DeploymentNamesRefEntity> usedVolumesMap = new HashMap<>();
                if (deploymentEntity.getNamesRefs() != null) {
                    for (DeploymentNamesRefEntity namesRefEntity : deploymentEntity.getNamesRefs()) {
                        switch (namesRefEntity.getType()) {
                            case VM_NAME:
                                usedVMNamesMap.put(namesRefEntity.getName(), namesRefEntity);
                                break;
                            case OS_HOST_NAME:
                                usedHostNamesMap.put(namesRefEntity.getName(), namesRefEntity);
                                break;
                            case STORAGE_VOLUME_NAME:
                                usedVolumesMap.put(namesRefEntity.getName(), namesRefEntity);
                                break;
                        }
                    }
                }

                Set<DeploymentNamesRefEntity> updatedNamesRefSet = new HashSet<>();

                if (hostNames != null && !hostNames.isEmpty()) {
                    final Set<String> usedHostNames = usedHostNamesMap.keySet();
                    for (String hostName : hostNames) {
                        if (usedHostNames.contains(hostName)) {
                            updatedNamesRefSet.add(usedHostNamesMap.get(hostName));
                        } else {
                            DeploymentNamesRefEntity entity = new DeploymentNamesRefEntity();
                            entity.setId(UUID.randomUUID().toString());
                            entity.setDeploymentId(deploymentEntity.getId());
                            entity.setType(DeploymentNamesType.OS_HOST_NAME);
                            entity.setName(hostName);
                            updatedNamesRefSet.add(entity);
                        }
                    }
                }

                if (vmNames != null  && !vmNames.isEmpty()) {
                    final Set<String> usedVMNames = usedVMNamesMap.keySet();
                    for (String vmName : vmNames) {
                        if (usedVMNames.contains(vmName)) {
                            updatedNamesRefSet.add(usedVMNamesMap.get(vmName));
                        } else {
                            DeploymentNamesRefEntity entity = new DeploymentNamesRefEntity();
                            entity.setId(UUID.randomUUID().toString());
                            entity.setDeploymentId(deploymentEntity.getId());
                            entity.setType(DeploymentNamesType.VM_NAME);
                            entity.setName(vmName);
                            updatedNamesRefSet.add(entity);
                        }
                    }
                }

                if (storageNames != null  && !storageNames.isEmpty()) {
                    final Set<String> usedVolumes = usedVolumesMap.keySet();
                    for (String volume : storageNames) {
                        if (usedVolumes.contains(volume)) {
                            updatedNamesRefSet.add(usedVolumesMap.get(volume));
                        } else {
                            DeploymentNamesRefEntity entity = new DeploymentNamesRefEntity();
                            entity.setId(UUID.randomUUID().toString());
                            entity.setDeploymentId(deploymentEntity.getId());
                            entity.setType(DeploymentNamesType.STORAGE_VOLUME_NAME);
                            entity.setName(volume);
                            updatedNamesRefSet.add(entity);
                        }
                    }
                }
                deploymentEntity.setNamesRefs(updatedNamesRefSet);
            }
        }
    }

    public MigrationDeviceUtils getMigrationDeviceUtils() {
        return migrationDeviceUtils;
    }

    public void setMigrationDeviceUtils(MigrationDeviceUtils migrationDeviceUtils) {
        this.migrationDeviceUtils = migrationDeviceUtils;
    }


    /**
     * Returns the FirmwareComplianceReport for the given deployment/serviceId.
     */
    @Override
    public FirmwareComplianceReport[] getFirmwareComplianceReportsForDevicesInDeployment (String deploymentId)
            throws WebApplicationException {

        FirmwareComplianceReport[] firmwareComplianceReports = null;

        // Load a Deployment throw error if not found
        DeploymentEntity deploymentEntity;
        try {
             deploymentEntity = deploymentDAO.getDeployment(deploymentId,DeploymentDAO.ALL_ENTITIES);
            if (deploymentEntity == null) {
                EEMILocalizableMessage msg = AsmManagerMessages.deploymentNotFound(deploymentId);
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, msg);
            }
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while getting Deployment with ID " + deploymentId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while getting Deployment with ID " + deploymentId, e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                                                       AsmManagerMessages.internalError());
        }

        // Validate Security Access
        this.checkUserPermission(deploymentEntity);

        // Generate the Firmware Compliance Report
        try {
            firmwareComplianceReports = this.firmwareUtil.getFirmwareComplianceReportsForDeployment(deploymentEntity);
        } catch(Exception ace) {
            logger.error("Error trying to generate firmware compliance reports for deployment with id: " +
                    deploymentEntity.getId(), ace);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        return firmwareComplianceReports;
    }
    
    private CompliantState getCompliantState(DeploymentEntity deploymentEntity, DeviceInventoryEntity device) {
        CompliantState compliantState = CompliantState.COMPLIANT;
        
        // Compliance for shared Device's is not stored in the device_inventory table, instead it
        // must be loaded from the device_inventory_compliance_map table
        if (deploymentEntity.isManageFirmware() && 
                device.getDeviceType().isSharedDevice() && 
                deploymentEntity.getFirmwareRepositoryEntity() != null) {
            // Load the device's compliance status
            DeviceInventoryComplianceEntity dice = 
                    this.deviceInventoryComplianceDAO.findDeviceInventoryCompliance(device.getRefId(), deploymentEntity.getFirmwareRepositoryEntity().getId());
            if (dice != null) {
                compliantState = dice.getCompliance();
            }
        }
        else {
            compliantState = CompliantState.fromValue(device.getCompliant());
        }
        
        return compliantState;
    }

}
