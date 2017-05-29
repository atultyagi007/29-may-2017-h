/**************************************************************************
 *   Copyright (c) 2013 - 2016 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

/*
 * @author Praharsh_Shah, Ferdinand silva
 * 
 * ASM core Template REST for infrastructure Template
 * 
 */

import com.dell.asm.alcm.client.model.DatabaseBackupSettings;
import com.dell.asm.alcm.client.model.NTPSetting;
import com.dell.asm.alcm.client.model.WizardStatus;
import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplate;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentFilterResponse;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedServer;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.IServiceTemplateService;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateUploadRequest;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateValid;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.VirtualDiskConfiguration;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.client.util.VcenterInventoryUtils;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ClusterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatacenterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ManagedObjectDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.PortGroupDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VDSDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VMTemplateDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VirtualMachineDTO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateUserRefEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.asmcore.asmmanager.util.AsmManagerUtil;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidator;
import com.dell.asm.asmcore.asmmanager.util.configuretemplate.ConfigureTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.DnsUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.NetworkingUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.business.timezonemanager.TimeZoneConfigurationMgr;
import com.dell.asm.common.model.TimeZoneInfoModel;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.EEMILocalizableMessage.EEMISeverity;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage.LogCategory;
import com.dell.asm.localizablelogger.LogMessage.LogSeverity;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.RestUtil;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypoolmgr.iopool.IIOPoolMgr;
import com.dell.pg.asm.identitypoolmgr.iopool.entity.IOPool;
import com.dell.pg.asm.identitypoolmgr.iopool.impl.IOPoolMgr;
import com.dell.pg.asm.identitypoolmgr.network.entity.NetworkConfiguration;
import com.dell.pg.asm.identitypoolmgr.network.impl.NetworkConfigurationMgr;
import com.dell.pg.asm.server.client.device.Controller;
import com.dell.pg.asm.server.client.device.LogicalNetworkInterface;
import com.dell.pg.asm.server.client.device.PhysicalDisk;
import com.dell.pg.asm.server.client.device.Server;
import com.dell.pg.asm.server.client.device.VirtualDisk;
import com.dell.pg.orion.common.utilities.ConfigurationUtils;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.PingUtil;
import com.dell.pg.orion.wsman.WSManException;
import com.dell.wsman.xmlconfig.Attribute;
import com.dell.wsman.xmlconfig.Component;
import com.dell.wsman.xmlconfig.SystemConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import com.wordnik.swagger.annotations.ApiParam;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@Path("/ServiceTemplate")
public class ServiceTemplateService implements IServiceTemplateService {

    public static final String PROP_DEFAULT_TEMPLATE_PATH = "com.dell.asm.asmcore.default.template";

    public static final String DEFAULT_TEMPLATE_ID = "1000";

    public static final String MS_LOCALE_FILENAME = "locales.txt";

    public static final String MS_LAYOUT_FILENAME = "keyboardLayouts.txt";

    private static final String RACK_DISCARD_BROADCOM_QUAD = "57800";

    public static final Set<String> validSortColumns = new HashSet<>();

    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateService.class);

    public static final String NO_MATCHING_VALUE = " ";

    private static final int MAX_ENUMERATED_LENGTH = 100;

    private static final Map<String, Map<String, String>> OLD_BIOS_TEMPLATE_MAP;

    static {
        List<String> list = new ArrayList<>();
        list.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SYSTEM_PROFILE_ID);
    }

    static {
        Map<String, Map<String, String>> map = new HashMap<>();
        Map<String, String> systemProfile = new HashMap<String, String>() {{
            put("remap", "false");
            put("name", "system_profile");
        }};
        Map<String, String> usbPorts = new HashMap<String, String>() {{
            put("remap", "false");
            put("name", "user_usb_ports");
        }};
        Map<String, String> coreProc = new HashMap<String, String>() {{
            put("remap", "false");
            put("name", "number_core_processors");
        }};
        Map<String, String> virtualizationTech = new HashMap<String, String>() {{
            put("remap", "true");
            put("true", "Enabled");
            put("false", "Disabled");
            put("name", "virtualization_technology");
        }};
        Map<String, String> logicalProc = new HashMap<String, String>() {{
            put("remap", "true");
            put("name", "logical_processor");
            put("true", "Enabled");
            put("false", "Disabled");
        }};
        Map<String, String> nodeInterleave = new HashMap<String, String>() {{
            put("remap", "true");
            put("true", "Enabled");
            put("false", "Disabled");
            put("name", "memory_node_interleave");
        }};
        Map<String, String> executeDisable = new HashMap<String, String>() {{
            put("remap", "true");
            put("true", "Enabled");
            put("false", "Disabled");
            put("name", "execute_disable");
        }};
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SYSTEM_PROFILE_ID, systemProfile);
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_USB_PORTS_ID, usbPorts);
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NUM_PROCS_ID, coreProc);
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_VIRTUALIZATION_ID, virtualizationTech);
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_LOGICAL_PROC_ID, logicalProc);
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MEM_NODE_INTERLEAVE_ID, nodeInterleave);
        map.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_EXECUTE_DISABLE_ID, executeDisable);

        OLD_BIOS_TEMPLATE_MAP = Collections.unmodifiableMap(map);
    }

    static {
        validSortColumns.add("name");
        validSortColumns.add("createdDate");
        validSortColumns.add("createdBy");
        validSortColumns.add("updatedDate");
        validSortColumns.add("updatedBy");
    }

    public static final Set<String> validFilterColumns = new HashSet<>();

    static {
        validFilterColumns.add("name");
        validFilterColumns.add("createdDate");
        validFilterColumns.add("createdBy");
        validFilterColumns.add("updatedDate");
        validFilterColumns.add("updatedBy");
        validFilterColumns.add("draft");
    }

    private final DeviceInventoryDAO deviceInventoryDAO;

    private final ServiceTemplateDAO templateDao;

    private final AddOnModuleComponentsDAO addOnModuleComponentsDAO;

    private final FirmwareRepositoryDAO firmwareRepositoryDAO;

    private final DeviceGroupDAO deviceGroupDAO;

    private final ServiceTemplateUtil serviceTemplateUtil;

    private final LocalizableMessageService localizableMessageService;

    private final INetworkService networkService;

    private ServiceTemplateValidator serviceTemplateValidator;

    private NetworkingUtil networkingUtil;

    private AsmManagerUtil asmManagerUtil;

    private OSRepositoryUtil osRepositoryUtil;

    private FirmwareUtil firmwareUtil;

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public ServiceTemplateService(DeviceInventoryDAO deviceInventoryDAO,
                                  ServiceTemplateDAO templateDao,
                                  ServiceTemplateUtil serviceTemplateUtil,
                                  AddOnModuleComponentsDAO addOnModuleComponentsDAO,
                                  ServiceTemplateValidator serviceTempValidator,
                                  LocalizableMessageService localizableMessageService,
                                  INetworkService networkService,
                                  FirmwareRepositoryDAO firmwareRepositoryDAO,
                                  AsmManagerUtil asmManagerUtil,
                                  DeviceGroupDAO deviceGroupDAO,
                                  OSRepositoryUtil osRepositoryUtil,
                                  FirmwareUtil firmwareUtil) {
        this.deviceInventoryDAO = deviceInventoryDAO;
        this.templateDao = templateDao;
        this.serviceTemplateUtil = serviceTemplateUtil;
        this.addOnModuleComponentsDAO = addOnModuleComponentsDAO;
        this.serviceTemplateValidator = serviceTempValidator;
        this.localizableMessageService = localizableMessageService;
        this.networkService = networkService;
        this.networkingUtil = new NetworkingUtil(new PingUtil(), this.localizableMessageService, new DnsUtil());
        this.firmwareRepositoryDAO = firmwareRepositoryDAO;
        this.asmManagerUtil = asmManagerUtil;
        this.deviceGroupDAO = deviceGroupDAO;
        this.osRepositoryUtil = osRepositoryUtil;
        this.firmwareUtil = firmwareUtil;
    }

    // no-arg constructor for jax-rs
    public ServiceTemplateService() {
        this(new DeviceInventoryDAO(),
             ServiceTemplateDAO.getInstance(),
             new ServiceTemplateUtil(),
             AddOnModuleComponentsDAO.getInstance(),
             new ServiceTemplateValidator(),
             LocalizableMessageService.getInstance(),
             ProxyUtil.getNetworkProxy(),
             FirmwareRepositoryDAO.getInstance(),
             new AsmManagerUtil(),
             DeviceGroupDAO.getInstance(),
             new OSRepositoryUtil(),
             new FirmwareUtil());
    }

    @Override
    public ServiceTemplate getTemplate(String templateId, Boolean includeBrownfieldVmMangers)
            throws WebApplicationException {
        LOGGER.debug("Get Template Entered for ID: " + templateId);

        ServiceTemplate svc = null;
        try {
            if (DEFAULT_TEMPLATE_ID.equalsIgnoreCase(templateId)) {
                svc = getDefaultTemplate(null, includeBrownfieldVmMangers);
                return svc;
            } else {
                ServiceTemplateEntity te = templateDao
                        .getTemplateById(templateId);
                if (te == null) {
                    throw new LocalizedWebApplicationException(
                            Response.Status.NOT_FOUND,
                            AsmManagerMessages.templateNotFound(templateId));
                }

                // check permissions
                if (!checkUserPermissions(te, null, null)) {
                    LOGGER.info("Refused access to template ID=" + templateId + " for user " + AsmManagerUtil.getUserId() + " because of lack of permissions");
                    throw new LocalizedWebApplicationException(
                            Response.Status.NOT_FOUND,
                            AsmManagerMessages.templateNotFound(templateId));
                }

                svc = createTemplateDTO(te, servletRequest, false);
                ServiceTemplate defaultTemplate = this.getDefaultTemplate(null, includeBrownfieldVmMangers);
                // do not renew options on sample templates
                if (!svc.isTemplateLocked()) {
                    svc.fillPossibleValues(defaultTemplate);
                }

                ServiceTemplateUtil.stripPasswords(svc, null);
                serviceTemplateUtil.updateRazorTargets(svc);
                filterOutResources(svc);

                // Sort ServiceTemplate Options
                svc.sortCategorySettingOptionsByValue();
            }
        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while getting service template with ID "
                            + templateId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while getting service template with ID "
                    + templateId, e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        LOGGER.debug("Get Template Finished for ID: " + templateId);
        ServiceTemplateUtil.stripPasswords(svc, null);
        svc.setAttachments(ServiceTemplateUtil.getAttachments(svc.getId()));
        return svc;
    }

    /**
     * Add any needed deploy-time parameter options to the passed template. Currently this is just
     * the applicable servers for the manual server selection list.
     *
     * @param template The template
     * @return Template with deploy-time parameter options added
     * @throws WebApplicationException if any error occurs
     */
    @Override
    public ServiceTemplate updateParameters(ServiceTemplate template) throws WebApplicationException {
        updateDeploymentTimeOptions(template);
        // some options might be added like nw server pool
        ServiceTemplate defaultTemplate = getDefaultTemplate();
        template.fillPossibleValues(defaultTemplate);
        resetStaticIPValues(template);
        return template;
    }

    @Override
    public ServiceTemplate createTemplate(ServiceTemplate svcTemplate) throws WebApplicationException {

        LOGGER.debug("Create Template Entered for template: " + svcTemplate.getTemplateName());

        try {

            if (svcTemplate != null && svcTemplate.getConfiguration() != null) {
                applyServiceTemplateConfiguration(svcTemplate);
            }

            // Compare template against the default, update if needed
            fillMissingParams(getDefaultTemplate(), svcTemplate);

            // validate the template if validation errors are found make sure it is in draft mode
            serviceTemplateValidator.validateTemplate(svcTemplate,
                    new ServiceTemplateValidator.ValidationOptions(false, true, true));
            // template will probably always be in draft mode on create but just to make sure
            if (!svcTemplate.getTemplateValid().isValid()) {
                svcTemplate.setDraft(Boolean.TRUE);
            }

            ServiceTemplateEntity checkDuplicateNameTemplate =
                    templateDao.getTemplateByName(svcTemplate.getTemplateName());
            if (checkDuplicateNameTemplate != null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.CONFLICT,
                        AsmManagerMessages.duplicateTemplateName(svcTemplate
                                .getTemplateName()));
            }

            svcTemplate.assignUniqueIDs();
            serviceTemplateUtil.encryptPasswords(svcTemplate);
            svcTemplate.setTemplateVersion(ServiceTemplateUtil.getCurrentTemplateVersion());
            ServiceTemplateEntity entity = createTemplateEntity(svcTemplate);
            ServiceTemplateEntity persisted = templateDao.createTemplate(entity);
            svcTemplate.setId(persisted.getTemplateId());

            firmwareUtil.manageServiceTemplateFirmware(svcTemplate, entity);

            // now we have ID and can add user map
            serviceTemplateUtil.addAssignedUsers(svcTemplate, entity);

            List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(true);
            // update add on module references.
            updateAddOnModulesOnTemplate(addOnModuleComponentEntities,svcTemplate,entity);

            templateDao.updateTemplate(entity);

            // Sort the Order of Options
            svcTemplate.sortCategorySettingOptionsByValue();

            logLocalizableMessage(AsmManagerMessages.serviceTemplateCreated(svcTemplate.getTemplateName()));
        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while creating service template "
                            + svcTemplate.getTemplateName(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while creating service template "
                    + svcTemplate.getTemplateName(), e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        LOGGER.debug("Create Template Done for template: "
                + svcTemplate.getTemplateName() + ". ID = "
                + svcTemplate.getId());

        return svcTemplate;
    }

    private void updateDeploymentTimeOptions(ServiceTemplate svcTemplate) {
        Map<String, ServiceTemplateCategory> componentIdToSetting = new HashMap<>();
        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                ServiceTemplateCategory resource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE);
                if (resource == null) {
                    resource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                }
                if (resource != null) {
                    ServiceTemplateSetting param = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION);
                    if (param != null) {
                        componentIdToSetting.put(component.getId(), resource);
                        param.getOptions().clear();
                    }
                }
            }
        }

        if (componentIdToSetting.size() > 0) {
            Map<String, String> originalSourceMap = new HashMap<>();
            Map<String, String> originalPoolIdMap = new HashMap<>();
            for (Map.Entry<String, ServiceTemplateCategory> entry : componentIdToSetting.entrySet()) {
                String componentId = entry.getKey();
                ServiceTemplateCategory resource = entry.getValue();

                // Save current source value and set to pool
                ServiceTemplateSetting sourceParam = resource.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
                originalSourceMap.put(componentId, sourceParam.getValue());
                sourceParam.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL);

                // Set the pool selection to "all" so that filtering can include all servers,
                // even those in pools
                ServiceTemplateSetting poolParam = resource.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);
                originalPoolIdMap.put(componentId, poolParam.getValue());
                poolParam.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID);

                // Clear the manual selection options, will be populated subsequently
                ServiceTemplateSetting param = resource.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION);
                ServiceTemplateUtil.addDefaultSelectOption(param);
            }

            DeploymentService deploymentService = new DeploymentService();
            deploymentService.setServletRequest(this.servletRequest);
            DeploymentFilterResponse response = deploymentService
                    .filterAvailableServers(svcTemplate, -1, false);

            Collections.sort(response.getSelectedServers());

            // Populate the drop-downs
            int n = Math.min(response.getSelectedServers().size(), MAX_ENUMERATED_LENGTH);
            for (int i = 0; i < n; ++i) {
                SelectedServer selected = response.getSelectedServers().get(i);
                ServiceTemplateCategory resource = componentIdToSetting.get(selected.getComponentId());
                ServiceTemplateSetting param = resource.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION);
                param.getOptions().add(new ServiceTemplateOption(
                        selected.getServiceTag(), selected.getRefId(), null, null));
            }

            // Restore original values
            for (Map.Entry<String, ServiceTemplateCategory> entry : componentIdToSetting.entrySet()) {
                String componentId = entry.getKey();
                ServiceTemplateCategory resource = entry.getValue();

                ServiceTemplateSetting sourceParam = resource.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
                sourceParam.setValue(originalSourceMap.get(componentId));

                ServiceTemplateSetting poolParam = resource.getParameter(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);
                poolParam.setValue(originalPoolIdMap.get(componentId));
            }
        }
    }

    /**
     * Updates the service template.
     *
     * @param templateId           The template id, error if it does not match the service template id
     * @param svcTemplate          The service template object
     * @param rejectInvalidPublish If true, throws an exception an exception if the template is not
     *                             draft and it does not pass validation. If false just sets draft
     *                             back to true and saves it anyway.
     * @throws WebApplicationException If there is a validation error on the template.
     */
    private void updateTemplate(String templateId,
                                ServiceTemplate svcTemplate,
                                boolean rejectInvalidPublish,
                                final ServiceTemplate defaultTemplate) throws WebApplicationException {
        LOGGER.debug("Update Template Entered for template: " + svcTemplate.getTemplateName());

        // check for dup templatename
        ServiceTemplate origTemplate = null;
        boolean updated = false;
        try {
            ServiceTemplateEntity templateEntity = templateDao.getTemplateById(templateId); // find
            // the template in DB.
            if (templateEntity == null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.templateNotFound(templateId));
            }

            // Compare template against the default, update if needed
            fillMissingParams(defaultTemplate, svcTemplate);

            ServiceTemplateUtil.trimSpaces(svcTemplate);
            serviceTemplateUtil.setHiddenValues(svcTemplate);

            // Encrypt password values. NULL password values should be used in the REST payload
            // to indicate that the password value should not be changed.
            origTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                    templateEntity.getMarshalledTemplateData());
            serviceTemplateUtil.encryptPasswords(svcTemplate, origTemplate);

            if (!svcTemplate.isDraft()) {
                // e.g. equallogic iqnorip gets set to required if it is not attached to a server
                updateRequired(svcTemplate);
            }

            // Build copy of service template to decrypt the passwords in and use for validation.
            // Otherwise validation against passwords may fail.
            String tempXml = MarshalUtil.marshal(svcTemplate);
            ServiceTemplate templateToValidate = MarshalUtil.unmarshal(ServiceTemplate.class, tempXml);
            serviceTemplateUtil.decryptPasswords(templateToValidate);

            // Remove any Unused Networks that may have accidently been left set
            ServiceTemplateUtil.clearUnusedNetworks(svcTemplate);
            ServiceTemplateUtil.clearUnusedNetworks(templateToValidate);

            boolean inventoryContainsEM = false;
            List<DeviceInventoryEntity> emDevices = deviceInventoryDAO.getAllDeviceInventoryByDeviceType(DeviceType.em);
            if (emDevices != null && !emDevices.isEmpty()) {
                inventoryContainsEM = true;
            }

            if (!svcTemplate.isTemplateLocked()) {
                serviceTemplateValidator.validateTemplate(templateToValidate,
                        new ServiceTemplateValidator.ValidationOptions(false, true, inventoryContainsEM));
                // copy the validation info
                serviceTemplateValidator.copyAllServiceTemplateValidations(templateToValidate, svcTemplate);
            }

            // if validation fails then check if saving as draft or trying to publish
            if (!templateToValidate.getTemplateValid().isValid()) {
                // if the template is being published
                if (!svcTemplate.isDraft()) {
                    // ensure it is set back to draft
                    svcTemplate.setDraft(Boolean.TRUE);

                    if (rejectInvalidPublish) {
                        LOGGER.error("Validation failed for publish template");
                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                serviceTemplateValidator.getAllServiceTemplateValidationMessages(templateToValidate));
                    }
                }
            }

            ServiceTemplateEntity checkDuplicateNameAndIdTemplate =
                    templateDao.getTemplateByName(svcTemplate.getTemplateName());
            // make sure on update we are not changing the name to something that already exists
            if (checkDuplicateNameAndIdTemplate != null &&
                    checkDuplicateNameAndIdTemplate.getTemplateId().compareToIgnoreCase(templateId) != 0) {
                throw new LocalizedWebApplicationException(
                        Response.Status.CONFLICT,
                        AsmManagerMessages.duplicateTemplateName(svcTemplate.getTemplateName()));
            }

            //svcTemplate.removePossibleValues();
            svcTemplate.assignUniqueIDs();

            // update required attributes based on template related components
            if (!svcTemplate.isDraft()) {
                // template published. Update appliance status for Getting Started calls.
                WizardStatus ws = ProxyUtil.getAlcmStatusProxy().getWizardStatus();
                if (!ws.getIsTemplateCompleted()) {
                    ws.setIsTemplateCompleted(true);
                    ProxyUtil.getAlcmStatusProxy().updateWizardStatus(ws);
                }

                // do not touch networks for sample templates
                if (!svcTemplate.isTemplateLocked()) {
                    updateStaticNetworkSettings(svcTemplate);
                }
            }

            svcTemplate.setTemplateVersion(StringUtils.isNotBlank(svcTemplate.getTemplateVersion()) ?
                    svcTemplate.getTemplateVersion() : ServiceTemplateUtil.getCurrentTemplateVersion());

            templateEntity.setName(svcTemplate.getTemplateName());
            templateEntity.setTemplateDesc(svcTemplate.getTemplateDescription());
            templateEntity.setTemplateVersion(svcTemplate.getTemplateVersion());
            templateEntity.setTemplateValid(svcTemplate.getTemplateValid().isValid());
            templateEntity.setMarshalledTemplateData(MarshalUtil.marshal(svcTemplate));
            templateEntity.setManageFirmware(svcTemplate.isManageFirmware());
            templateEntity.setUseDefaultCatalog(svcTemplate.isUseDefaultCatalog());
            templateEntity.setDraft(svcTemplate.isDraft());

            firmwareUtil.manageServiceTemplateFirmware(svcTemplate, templateEntity);

            templateEntity.setAllUsersAllowed(svcTemplate.isAllUsersAllowed());

            templateEntity.getAssignedUserList().clear();
            serviceTemplateUtil.addAssignedUsers(svcTemplate, templateEntity);

            List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(true);
            // update add on module references.
            updateAddOnModulesOnTemplate(addOnModuleComponentEntities,svcTemplate,templateEntity);

            templateDao.updateTemplate(templateEntity);
            updated = true;
            logLocalizableMessage(AsmManagerMessages.serviceTemplateUpdated(svcTemplate.getTemplateName()));
        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while creating service template "
                            + svcTemplate.getTemplateName(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while creating service template "
                    + svcTemplate.getTemplateName(), e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        } finally {
            if (updated && origTemplate != null) {
                // Delete encryption ids that have been replaced
                serviceTemplateUtil.deleteRemovedEncryptionIds(origTemplate, svcTemplate);
            }
        }

        LOGGER.debug("Update Template Done for template: "
                + svcTemplate.getTemplateName() + ". ID = "
                + svcTemplate.getId());
    }

    /**
     * Updates the template in the database. If the template is not valid draft will be set to
     * false but it will be saved anyway.
     *
     * @param svcTemplate The template to update
     * @throws WebApplicationException If any validation error occurs.
     */
    public void updateTemplate(ServiceTemplate svcTemplate, final ServiceTemplate defaultTemplate) throws WebApplicationException {
        updateTemplate(svcTemplate.getId(), svcTemplate, false, defaultTemplate);
    }

    /**
     * Updates the template in the database. If the template is not valid and draft is false will
     * throw an exception.
     *
     * @param svcTemplate The template to update
     * @throws WebApplicationException If any validation error occurs.
     */
    @Override
    public Response updateTemplate(String templateId, ServiceTemplate svcTemplate) throws WebApplicationException {
        updateTemplate(templateId, svcTemplate, true, getDefaultTemplate());
        return Response.noContent().build();
    }

    /**
     * Get default template customized for selected device.
     *
     * @param deviceId - device id of device to get template for
     * @return
     * @throws WebApplicationException
     */
    @Override
    public ServiceTemplate getCustomizedTemplate(String deviceId)
            throws WebApplicationException {
        LOGGER.debug("Get Default Template for device ID: " + deviceId);

        ServiceTemplate svcTmpl = getDefaultTemplate(ServiceTemplateComponentType.SERVER.name(), false);
        DeviceInventoryEntity device = deviceInventoryDAO.getDeviceInventory(deviceId);
        if (device == null)
            return svcTmpl;

        try {
            if (DeviceType.isServer(device.getDeviceType())) {
                applyServerCustomization(svcTmpl, device);
            }
        } catch (IOException | WSManException | AsmManagerInternalErrorException e) {
            LOGGER.error("Invoke WSMAN for getCustomizedTemplate failed", e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());

        }

        return svcTmpl;
    }

    /**
     * Get default template components refined for selected template ID.
     *
     * @param templateId    Template ID as a source
     * @param componentType Component to customize.
     * @return
     * @throws WebApplicationException
     */
    @Override
    public ServiceTemplate getCustomizedComponentForTemplate(String templateId, String componentType) {
        return getCustomizedComponent(templateId, null, componentType);
    }

    /**
     * Get default template components refined for selected service ID.
     * @param serviceId Service Id
     * @param componentType component type
     * @return
     */
    @Override
    public ServiceTemplate getCustomizedComponentForService(String serviceId, String componentType) {
        return getCustomizedComponent(null, serviceId, componentType);
    }

    /**
     * Get default template components refined for selected template ID.
     * Use either templateId or ServiceId as a spurce fopr template.
     *
     * @param templateId    Template ID as a source, can be null
     * @param serviceId     Service ID as a source, can be null
     * @param componentType Component to customize.
     * @return
     * @throws WebApplicationException
     */
    private ServiceTemplate getCustomizedComponent(String templateId, String serviceId, String componentType)
            throws WebApplicationException {

        ServiceTemplate defaultTemplate = getDefaultTemplate(componentType, false);
        ServiceTemplate refTmpl = null;
        if (templateId != null) {
            refTmpl = getTemplate(templateId, false);
        } else if (serviceId != null) {
            Deployment deployment = ProxyUtil.getDeploymentProxy().getDeployment(serviceId);
            if (deployment != null) {
                refTmpl = deployment.getServiceTemplate();
            }
            if (refTmpl != null && ServiceTemplateComponentType.CLUSTER.getLabel().equalsIgnoreCase(componentType)) {
                // this is scale up case. Connect new cluster to server components by default. It will help "refine" code to
                // show correct options for VDS
                List <ServiceTemplateComponent> serverComponents = new ArrayList<>();
                for (ServiceTemplateComponent component: refTmpl.getComponents()) {
                    if (component.getType() == ServiceTemplateComponentType.SERVER) {
                        serverComponents.add(component);
                    }
                }
                for (ServiceTemplateComponent component: defaultTemplate.getComponents()) {
                    if (component.getType() == ServiceTemplateComponentType.CLUSTER) {
                        for (ServiceTemplateComponent serverComponent: serverComponents) {
                            component.addAssociatedComponentName(serverComponent.getId(), serverComponent.getName());
                        }
                    }
                }
            }
        }

        if (refTmpl == null)
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noServiceTemplateFoundForDeployment());

        ServiceTemplateUtil.refineComponents(refTmpl, defaultTemplate, componentType);

        return defaultTemplate;
    }

    /**
     * Make changes in default template based on IDRAC config for specified server.
     * @param svcTmpl
     * @param device Used to pull required config from WSMAN API.
     */
    private void applyServerCustomization(ServiceTemplate svcTmpl, DeviceInventoryEntity device) throws IOException, WSManException, AsmManagerInternalErrorException {
        if (!DeviceType.isRAServer(device.getDeviceType())) {
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.importConfigNotSupported());
        }

        Server server = ProxyUtil.getDeviceServerProxy().getServer(device.getRefId());

        SystemConfiguration sysConfig = null;
        String configXml = server.getConfig();
        if (configXml!=null) {
            try {
                sysConfig = MarshalUtil.unmarshal(SystemConfiguration.class, configXml);
            }catch(IllegalStateException e) {
                LOGGER.error("Cannot parse config.xml: " + configXml, e);
            }
        }

        if (sysConfig == null) {
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.unableImportConfig());
                    
        }
        processSystemConfiguration(svcTmpl, configXml, device, server);

        ServiceTemplateUtil.refineComponents(svcTmpl,null,ServiceTemplateComponentType.SERVER.getLabel());
    }

    private void applyRaidCustomization(ServiceTemplateSetting setting, DeviceInventoryEntity device, Server server) {

        TemplateRaidConfiguration raidConfiguration = new TemplateRaidConfiguration();
        raidConfiguration.setRaidtype(TemplateRaidConfiguration.RaidTypeUI.basic);
        raidConfiguration.setBasicraidlevel(VirtualDiskConfiguration.UIRaidLevel.raid0);
        raidConfiguration.setEnableglobalhotspares(false);

        String controllerFQDD = null;
        if (server.getControllers()!=null) {
            for (Controller controllerComponent : server.getControllers()) {
                // for each controller lookup for virtual disks
                for (VirtualDisk subComponent : controllerComponent.getVirtualDisks()) {
                    raidConfiguration.setRaidtype(TemplateRaidConfiguration.RaidTypeUI.advanced);
                    controllerFQDD = controllerComponent.getFqdd();

                    String virtualDiskName = subComponent.getFqdd();
                    VirtualDiskConfiguration vd = new VirtualDiskConfiguration();
                    vd.setComparator(VirtualDiskConfiguration.ComparatorValue.exact);
                    vd.setId(virtualDiskName);
                    vd.setNumberofdisks(1); // this is default value. Real value will come from RA.
                    vd.setDisktype(VirtualDiskConfiguration.DiskMediaType.any); // this is default value. Real value will come from RA.

                    vd.setRaidlevel(VirtualDiskConfiguration.UIRaidLevel.valueOf(subComponent.getRaidLevel().toUIValue()));
                    if (subComponent.getPhysicalDisks().size() > 0) {
                        vd.setDisktype(VirtualDiskConfiguration.DiskMediaType.fromServerValue(subComponent.getPhysicalDisks().iterator().next().getMediaType().name()));
                        vd.setNumberofdisks(subComponent.getPhysicalDisks().size());
                    } else {
                        LOGGER.warn("Incomplete inventory: no physical disks for controller " + controllerFQDD + ", VD=" + subComponent.getFqdd() + " on server " + server.getManagementIP());
                    }
                    raidConfiguration.getVirtualdisks().add(vd);
                }

                for (PhysicalDisk pd : controllerComponent.getPhysicalDisks()) {
                    if (pd.getHotSpareStatus() == PhysicalDisk.HotSpareStatus.Global) {
                        raidConfiguration.setEnableglobalhotspares(true);
                        raidConfiguration.setGlobalhotspares(raidConfiguration.getGlobalhotspares() + 1);
                        if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.SSD) {
                            raidConfiguration.setMinimumssd(raidConfiguration.getMinimumssd() + 1);
                        }
                    }
                }
                if (controllerFQDD != null)
                    break; // process only the first found controller with virtual disks

            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            setting.setValue(objectMapper.writeValueAsString(raidConfiguration));
        } catch (JsonProcessingException e) {
            throw new AsmManagerInternalErrorException("applyRaidCustomization", "ServiceTemplateService", e);
        }

    }

    /**
     * Import BIOS attributes into template "BIOS" section from config.xml (will only have 1 choice for each).
     * @param component
     */
    private void importBIOSSettingsFromProfile(ServiceTemplateComponent component, SystemConfiguration sysConfig) {
        ServiceTemplateCategory biosResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE);
        // Need to set all basic bios settings to n/a in case they don't exist in the profile.
        for(ServiceTemplateSetting basicSetting: biosResource.getParameters()){
            basicSetting.setValue("n/a");
        }
        ServiceTemplateSetting biosConfiguration = biosResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_CONFIG_ID);
        //TODO:  Advanced bios settings will be populated, but hidden, until we can implement a better solution for grouping settings
        biosConfiguration.setHideFromTemplate(true);
        biosConfiguration.setGenerated(true); // important! otherwise will be reset to hidden on template update
        biosConfiguration.setValue("advanced");
        biosResource.getParameters().add(0,biosConfiguration);
        ServiceTemplateSetting setting;
        for(Component c: sysConfig.getComponent()) {
            if(c.getFQDD().toLowerCase().startsWith("bios.setup")) {
                for(Attribute a: c.getAttribute()) {
                    setting = biosResource.getParameter(a.getName());
                    if(setting != null) {
                        setting.setValue(a.getValue());
                        setting.setRequired(false);
                    }else{
                        setting = new ServiceTemplateSetting();
                        setting.setId(a.getName());
                        setting.setDisplayName(a.getName());
                        setting.setType(ServiceTemplateSettingType.ENUMERATED);
                        setting.getOptions().add(0, new ServiceTemplateOption(a.getValue(), a.getValue(), null, null));
                        setting.setValue(a.getValue());
                        setting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_CONFIG_ID);
                        setting.setDependencyValue("advanced");
                        //TODO:  Advanced settings will be hidden until we can group, as mentioned above.
                        setting.setHideFromTemplate(true);
                        setting.setRequired(false);
                        setting.setGenerated(true);
                        biosResource.getParameters().add(setting);
                    }
                }
            }
        }
        Collections.sort(biosResource.getParameters(), new GroupComparator());
    }

    /**
     * Import BIOS attributes into template "BIOS" section
     * @param component
     * @param biosList
     */
    private void importBIOSSettings(ServiceTemplateComponent component, List<Map> biosList) {
        if (biosList==null || biosList.size()==0)
            return;

        ServiceTemplateCategory basicBIOS = buildServerBIOSSettings();

        ServiceTemplateCategory biosResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE);

        // Basic or Advanced
        ServiceTemplateSetting biosConfiguration = biosResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_CONFIG_ID);
        biosConfiguration.setValue("advanced");
        biosConfiguration.setGenerated(true); // important! otherwise will be reset to hidden on template update
        biosConfiguration.setHideFromTemplate(false);

        ServiceTemplateSetting setting;
        for (Map ba: biosList) {

            boolean hasDependency = StringUtils.isNotEmpty((CharSequence) ba.get("dependency"));
            setting = biosResource.getParameter((String) ba.get("attributeName"));
            if (setting==null) {
                setting = new ServiceTemplateSetting();
                setting.setId((String) ba.get("attributeName"));
                setting.setDisplayName((String) ba.get("attributeDisplayName"));

                if (hasDependency) {
                    setting.setToolTip("This setting has other settings for which it is dependent");
                    setting.setInfoIcon(true);
                }
            }
            setOptionsFromAttribute(setting, (String) ba.get("possibleValues"), (String) ba.get("possibleValuesDescription"), (String) ba.get("currentValue"));
            setting.setType(ServiceTemplateSettingType.ENUMERATED);

            if ((Boolean)ba.get("readOnly")!=null && (Boolean)ba.get("readOnly")) {
                setting.setValue("n/a");
            }
            else if (setting.getValue() == null){
                setting.setValue((String) ba.get("currentValue"));
            }

            boolean isBasic = false;
            for (ServiceTemplateSetting basicSetting: basicBIOS.getParameters()) {
                if (basicSetting.getId().equals(setting.getId())) {
                    isBasic = true;
                }
            }
            if (!isBasic) {
                setting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_CONFIG_ID);
                setting.setDependencyValue("advanced");
            }

            if (StringUtils.isNotEmpty((CharSequence) ba.get("groupDisplayName"))) {
                setting.setGroup((String) ba.get("groupDisplayName"));
            }
            setting.setRequired(false);
            setting.setGenerated(true); // always override default template
            biosResource.getParameters().add(setting);
        }

        Collections.sort(biosResource.getParameters(), new GroupComparator());
    }

    private void setOptionsFromAttribute(ServiceTemplateSetting setting, String possibleValues, String possibleValuesDescription, String currentValue) {
        if (setting.getOptions()!=null)
            setting.getOptions().clear();
        else
            setting.setOptions(new ArrayList<ServiceTemplateOption>());
        setting.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        if (possibleValues==null || possibleValuesDescription==null)
            return;

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<String> values = mapper.readValue(possibleValues, List.class);
            List<String> names = mapper.readValue(possibleValuesDescription, List.class);
            List<String> curValue = mapper.readValue(currentValue, List.class);



            Iterator<String> it = names.iterator();
            for (String value: values) {
                String name = it.next();
                setting.getOptions().add(new ServiceTemplateOption(name, value, null, null));
            }
            //Need to set this to avoid prompts that require manual intervention to get past.
            if(setting.getId().equals("ErrPrompt")){
                setting.setValue("Disabled");
                setting.setHideFromTemplate(true);
            }
            else {
                setting.setValue(curValue.get(0));
            }

        } catch (IOException e) {
            LOGGER.error("Unable to parse possible values: [" + possibleValues + "] or [" + possibleValuesDescription + "]", e);
        }
    }

    /**
     * We will be parsing the network interfaces, most likely extracted from a device xml, and generating a json string to set the value of the service template setting with it 
     * @param serverSettingNetwork
     * @param networkInterfaces
     */
    protected void applyNetworkCustomization(ServiceTemplateSetting serverSettingNetwork, final TreeMap<String, Component> networkInterfaces, boolean is2x10gby2x1gNicType)
    {
    	if (serverSettingNetwork == null || networkInterfaces == null)
    		return;

    	LOGGER.debug("Beginning to construct network from config xml");

    	//Setup the little helpers we will be using later
        //Create the top level of networkconfiguration
    	com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration
                networkConfigurationResult = new com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration();
        networkConfigurationResult.setId(UUID.randomUUID().toString());
        networkConfigurationResult.setInterfaces(new ArrayList<Fabric>());
    	Fabric lastCard = new Fabric();
    	Interface lastSubInterface = new Interface();
//        String lastSlot = "1";
    	//Fabric lastFabric = new Fabric();
    	//Interface lastFabricInterface = new Interface();
        //We use this comparator to ensure the fabrics show up in the right order in the UI
        //If we don't sort, the fabrics will show up in the order they show up in the config.xml

        ArrayList<String> sortedInterfaces = new ArrayList<String>();
        sortedInterfaces.addAll(networkInterfaces.keySet());
        Collections.sort(sortedInterfaces, new FabricComparator());

        //Loop through sortedInterfaces to map out which nics are in which fabric/slot.
        HashMap<String, String> fabricMap = new HashMap<>();
        int fabricIndex = -1;
        String lastFabricLocation = "0";
        String lastNicType = "";
        // For RACK Servers, 1 Integrated and 7 PCI slots
        String[] fabricIds = {"1", "2", "3", "4", "5", "6", "7", "8"};
        for(String nic: sortedInterfaces){
            String thisLocation = getFabricLocation(nic);
            String thisNicType = nic.split("\\.\\d\\w*")[0];
            if(!thisLocation.equals(lastFabricLocation) || !lastNicType.equals(thisNicType)){
                fabricIndex++;
                lastFabricLocation = thisLocation;
                lastNicType = thisNicType;
            }
            String thisFabricId = fabricIds[fabricIndex];
            fabricMap.put(nic, thisFabricId);
        }

        //Loop through sortedInterfaces to map out which nics are in which fabric/slot.
        //Iterate over all of the nic configurations that were passed in sorted off of their keys
        int firstInterfaceCount = 0;  // is2x10gby2x1gNicType can only be set for ONE interface, must make sure we do not set it for ALL Network Interfaces (Network Cards / Nics)
        for(String key: sortedInterfaces){
    		String regex = "^\\w+\\.\\w+";
    		String min = "0";
    		String max = "100";   
    		
    		//Parse nics that are of the expected naming convention
    		if (key.matches(regex + "\\.\\d\\w?-\\d(-\\d)?"))
    		{
    			LOGGER.debug("Found correctly formatted key: " + key);
    			Component nic = networkInterfaces.get(key);
    			//Get the values from the passed in nic configuration
    			for (Attribute a: nic.getAttribute()) 
            	{
            		if ("minbandwidth".equalsIgnoreCase(a.getName()))
            			min = a.getValue();
            		else if ("maxbandwidth".equalsIgnoreCase(a.getName()))
            			max = a.getValue();
            	}
    			
    			Pattern pattern = Pattern.compile(regex);    		
	    		Matcher m = pattern.matcher(key);
	    		m.find();    		
	    		String nicType = m.group();
                boolean isFcFabric = nicType.toLowerCase().startsWith("fc.");
	    		String numeric = key.split(regex+"\\.")[1];
	    		String[] numericSplit = numeric.split("-");
	    		
	    		//Now parse the numeric portion and construct a network configuation hierarchy from it
	    		//Top level under the network configuration
                String fIndex = fabricMap.get(nic.getFQDD());
	    		String topName = "Interface " + fIndex;

	    		if (!topName.equals(lastCard.getName()))
                {
	    			LOGGER.debug("Creating top level for: " + topName);
                    lastCard = new Fabric();
                    lastCard.setId(UUID.randomUUID().toString());
                    lastCard.setName(topName);
                    lastCard.setEnabled(true);
	    			if(is2x10gby2x1gNicType && firstInterfaceCount < 4){
                        lastCard.setNictype(Interface.NIC_2_X_10GB_2_X_1GB);
	    			}
	    			else
                        lastCard.setNictype(Interface.NIC_2_X_10GB);

                    lastCard.setInterfaces(new ArrayList<Interface>());

                    if (isFcFabric) {
                        lastCard.setFabrictype(Fabric.FC_TYPE);
                    }else{
                        lastCard.setFabrictype(Fabric.ETHERNET_TYPE);
                    }

	    			//Only add the interfaces here if it is rack server
	    			//If not we will be constucting them anyway but just discarding them
       				networkConfigurationResult.getInterfaces().add(lastCard);
	    		}
                //FC fabrics should have no port/partition configuration.
                if(!isFcFabric) {
                    //Middle level
                    String thisSlot = numericSplit[0];
                    String portNumber = numericSplit[1];
                    // Embedded/Integrated nics are weird, in that the nics could be labelled 1-1-1 and 2-1-1, but those are single port cards.
                    // So we want to show double ports, in the same fabric.
                    if((key.contains("Embedded") || key.contains("Integrated")) && !thisSlot.equals("1") ){
                        portNumber = thisSlot;
                    }
                    String middleName = "Port " + portNumber;
                    if (!middleName.equals(lastSubInterface.getName())) {
                        if (Integer.parseInt(numericSplit[1]) > 2) {
                            if(is2x10gby2x1gNicType && firstInterfaceCount < 4){
                                lastCard.setNictype(Interface.NIC_2_X_10GB_2_X_1GB);
                            }
                            else{
                                lastCard.setNictype(Interface.NIC_4_X_10GB);
                            }
                        }

                        LOGGER.debug("Creating middle level for: " + middleName);
                        lastSubInterface = new Interface();
                        lastSubInterface.setId(UUID.randomUUID().toString());
                        lastCard.getInterfaces().add(lastSubInterface);
                        lastSubInterface.setPartitions(new ArrayList<Partition>());
                        lastSubInterface.setName(middleName);
                        lastSubInterface.setPartitioned(true);

                    }

                    //Last level. Can be missed in FQDD.
                    if (numericSplit.length>2) {
                        Partition partition = new Partition();
                        partition.setId(UUID.randomUUID().toString());
                        partition.setName(numericSplit[2]);
                        partition.setMinimum(Integer.parseInt(min));
                        partition.setMaximum(Integer.parseInt(max));
                        partition.setNetworks(new ArrayList<String>());
                        lastSubInterface.getPartitions().add(partition);

                    }
                }
    		}
    		firstInterfaceCount++;
    	}
        fillMaxPortsAndPartitions(networkConfigurationResult.getInterfaces());

        serverSettingNetwork.setValue(ServiceTemplateUtil.serializeNetwork(networkConfigurationResult));
	}

    /**
     * For each card add max ports and partitions (4 and 4)
     * @param cards
     */
    private void fillMaxPortsAndPartitions(List<Fabric> cards){
        int maxInterfaces = 4;
        int maxPart  = 4;

        for (Fabric f: cards) {
            int intSize = f.getInterfaces().size();
            for (int j = 0; j < maxInterfaces; j++) {
                Interface iface = null;

                if (j < intSize) {
                    iface = f.getInterfaces().get(j);

                    //Re-adjust the is partitioned based off of what we actually created while parsing
                    //They will allways have at least 1 partition after getting parsed
                    if (iface.getPartitions() != null && iface.getPartitions().size() > 1)
                        iface.setPartitioned(true);
                    else
                        iface.setPartitioned(false);
                } else {
                    String middleName = "Port " + (j + 1);
                    iface = new Interface();
                    iface.setId(UUID.randomUUID().toString());
                    f.getInterfaces().add(iface);
                    iface.setPartitions(new ArrayList<Partition>());
                    iface.setName(middleName);
                    iface.setPartitioned(false);
                }

                if (iface.getPartitions() == null)
                    iface.setPartitions(new ArrayList<Partition>());

                //for each interface add max partitions
                for (int k = iface.getPartitions().size(); k < maxPart; k++) {
                    Partition partition = new Partition();
                    partition.setId(UUID.randomUUID().toString());
                    partition.setName(k + 1 + "");
                    partition.setMinimum(0);
                    partition.setMaximum(100);
                    partition.setNetworks(new ArrayList<String>());
                    iface.getPartitions().add(partition);
                }
            }
        }
    }


    /**
     * Get default template customized with uploaded config file
     * @param configPath
     * @return
     * @throws WebApplicationException
     */
    @Override
    public ServiceTemplate getUploadedConfigTemplate(String configPath) throws IOException {

        try
        {
            String configXml = "";
            List<String> lines = Files.readAllLines(Paths.get(configPath), Charset.defaultCharset());
            for(String line: lines)
                configXml += line;
            ServiceTemplate svcTmpl = getDefaultTemplate();
            processSystemConfiguration(svcTmpl, configXml, null, null);
            return svcTmpl;
        }
        catch(IOException e)
        {
            LOGGER.error("Could not read imported configuration profile file at " + configPath, e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        catch(WSManException e)
        {
            LOGGER.error("Could not process imported configuration profile", e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        finally
        {
            Files.deleteIfExists(Paths.get(configPath));
        }
    }

    private void processSystemConfiguration(ServiceTemplate svcTmpl, String configXml,
                                            DeviceInventoryEntity device, Server server) throws IOException, WSManException {
        SystemConfiguration sysConfig = MarshalUtil.unmarshal(SystemConfiguration.class, configXml);
        LOGGER.debug("XML found: " + configXml);

        if (sysConfig == null) {
            LOGGER.error("Cannot parse config.xml: " + configXml);
            if(device != null)
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.exportConfigurationFailed());
            else
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.uploadConfigurationFailed());
        }
        boolean is2x10gby2x1gNicType = false;
        if(server != null && server.getNetworkInterfaceList() != null){
            for(LogicalNetworkInterface logicalNetworkInterface : server.getNetworkInterfaceList()){
                if(logicalNetworkInterface.getProductName().contains(RACK_DISCARD_BROADCOM_QUAD)){
                    is2x10gby2x1gNicType = true;
                    break;
                }
            }
        }
        
        for (ServiceTemplateComponent component : svcTmpl.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                applyServerComponentCustomization(svcTmpl, configXml, device, server, component, sysConfig, is2x10gby2x1gNicType);
            }
        }
    }

    private void applyServerComponentCustomization(ServiceTemplate svcTmpl, String configXml,
                                                   DeviceInventoryEntity device, Server server,
                                                   ServiceTemplateComponent component, SystemConfiguration sysConfig,
                                                   boolean is2x10gby2x1gNicType) throws IOException {
        TreeMap<String, Component> networkInterfaces = new TreeMap<String, Component>();
        TreeMap<String, Component> controllers = new TreeMap<String, Component>();

        ServiceTemplateSetting serverSettingTargetBoot = svcTmpl.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
        if (serverSettingTargetBoot != null) {
            serverSettingTargetBoot.setType(ServiceTemplateSettingType.ENUMERATED);

            //Iterate over the settings from the device, not really a better way to do this :(
            String hddSeq = "";
            String integratedRaid = "";
            boolean hasRaidConfig = false;
            for (Component c : sysConfig.getComponent()) {
                if (c.getFQDD() != null && (c.getFQDD().toLowerCase().startsWith("nic") || c.getFQDD().toLowerCase().startsWith("fc")))
                    networkInterfaces.put(c.getFQDD(), c);

                if (c.getFQDD() != null && c.getFQDD().toLowerCase().startsWith("raid.")) {
                    controllers.put(c.getFQDD(), c);
                    for (Component raidComp : c.getComponent()) {
                        if (raidComp.getFQDD().toLowerCase().startsWith("disk.")) {
                            hasRaidConfig = true;
                        }
                    }
                }
                for (Attribute a : c.getAttribute()) {
                    if ("BIOS.Setup.1-1".equals(c.getFQDD())) {
                        if (a.getName().equalsIgnoreCase("HddSeq"))
                            hddSeq = a.getValue();
                        if (a.getName().equalsIgnoreCase("IntegratedRaid"))
                            integratedRaid = a.getValue();
                    }
                }
            }

            if (!StringUtils.isEmpty(hddSeq) && hddSeq.trim().startsWith("Disk.SDInternal") && hasRaidConfig) {
                serverSettingTargetBoot.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID);
            }
            else if (!StringUtils.isEmpty(hddSeq) && hddSeq.trim().startsWith("Disk.SDInternal") && !hasRaidConfig) {
                serverSettingTargetBoot.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD);
            }
            else if (!StringUtils.isEmpty(hddSeq) && hddSeq.trim().startsWith("RAID.Integrated")) {
                serverSettingTargetBoot.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD);
            }
            else if (hasRaidConfig) {
                serverSettingTargetBoot.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID);
            }
            else {
                serverSettingTargetBoot.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE);
            }
        }

        // store config.xml as template setting
        ServiceTemplateCategory cat = svcTmpl.getTemplateResource(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE);
        if (cat != null) {
            ServiceTemplateSetting set = new ServiceTemplateSetting();
            set.setType(ServiceTemplateSettingType.STRING);
            set.setHideFromTemplate(true);
            set.setRequired(false);
            set.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_CONFIG_XML);
            set.setValue(configXml);
            set.setGenerated(true);
            cat.getParameters().add(set);
        }
        // find RAID config in template to set it accordingly
        ServiceTemplateSetting raidSetting = svcTmpl.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID);
        if (raidSetting!= null) {
            String serverType = "";
            //Device is intended to be null if it's not clone from reference server case
            if (device != null) {
                // UI needs these attributes to match component.
                component.setRefId(device.getRefId());
                component.setIP(device.getIpAddress());
                String biosString = server.getBios();
                ObjectMapper mapper = new ObjectMapper();
                List<Map> biosList = mapper.readValue(biosString, List.class);
                importBIOSSettings(component, biosList);
                applyRaidCustomization(raidSetting, device, server);
            } else {
                applyRaidCustomizationFromProfile(raidSetting, controllers);
                importBIOSSettingsFromProfile(component, sysConfig);
            }

        }
        ServiceTemplateSetting serverSettingNetwork = svcTmpl.getTemplateSetting(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        if (serverSettingNetwork != null) {
            applyNetworkCustomization(serverSettingNetwork, networkInterfaces, is2x10gby2x1gNicType);
        }
    }

    //TODO: This could be merged more with applyRaidCustomization
    private void applyRaidCustomizationFromProfile(ServiceTemplateSetting setting, TreeMap<String, Component> controllers)
    {
        TemplateRaidConfiguration raidConfiguration = new TemplateRaidConfiguration();
        raidConfiguration.setRaidtype(TemplateRaidConfiguration.RaidTypeUI.basic);
        raidConfiguration.setBasicraidlevel(VirtualDiskConfiguration.UIRaidLevel.raid0);
        raidConfiguration.setEnableglobalhotspares(false);

        String controllerFQDD = null;
        for (Component controllerComponent: controllers.values()) {
            // for each controller lookup for virtual disks
            for (Component subComponent: controllerComponent.getComponent()) {
                if (subComponent.getFQDD().toLowerCase().startsWith("disk.virtual.")) {
                    raidConfiguration.setRaidtype(TemplateRaidConfiguration.RaidTypeUI.advanced);
                    controllerFQDD = controllerComponent.getFQDD();

                    String virtualDiskName = subComponent.getFQDD();
                    VirtualDiskConfiguration vd = new VirtualDiskConfiguration();
                    vd.setComparator(VirtualDiskConfiguration.ComparatorValue.exact);
                    vd.setId(virtualDiskName);
                    vd.setDisktype(VirtualDiskConfiguration.DiskMediaType.any);

                    raidConfiguration.getVirtualdisks().add(vd);
                    int numOfDisks = 0;
                    for (Attribute attribute : subComponent.getAttribute()) {
                        if (attribute.getName().equals("RAIDTypes")) {
                            vd.setRaidlevel(VirtualDiskConfiguration.UIRaidLevel.fromConfigValue(attribute.getValue()));
                        }
                        else if(attribute.getName().equals("IncludedPhysicalDiskID")){
                            numOfDisks++;
                        }
                    }
                    vd.setNumberofdisks(numOfDisks);
                }else if (subComponent.getFQDD().toLowerCase().startsWith("enclosure.internal.")) {
                    int numOfHotspares = 0;
                    for (Component physicalDiskComponent: subComponent.getComponent()) {
                        for (Attribute attribute : physicalDiskComponent.getAttribute()) {
                            if (attribute.getName().equals("RAIDHotSpareStatus")) {
                                // No, Dedicated, Global
                                if (attribute.getValue().contains("Global")) {
                                    raidConfiguration.setEnableglobalhotspares(true);
                                    numOfHotspares++;
                                }
                            }
                        }
                    }
                    raidConfiguration.setGlobalhotspares(numOfHotspares);
                }
            }
            if (controllerFQDD!=null)
                break; // process only the first found controller with virtual disks
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            setting.setValue(objectMapper.writeValueAsString(raidConfiguration));
        } catch (JsonProcessingException e) {
            throw new AsmManagerInternalErrorException("applyRaidCustomization", "ServiceTemplateService", e);
        }

    }

    /**
     * Some values need to be set required at deployment based on other template components
     * @param svcTemplate
     */
    private void updateRequired(ServiceTemplate svcTemplate) {
        List<ServiceTemplateComponent> components = svcTemplate.getComponents();

        for (ServiceTemplateComponent component : components) {

            if (component.getType().equals(ServiceTemplateComponentType.STORAGE)) {
                // see if it has related server component
                boolean haveServer = false;
                if (component.getAssociatedComponents() != null) {
                    Set<String> associatedComponentsKeys = component.getAssociatedComponents().keySet();
                    for (ServiceTemplateComponent cmp : svcTemplate.getComponents()) {
                        if (cmp.getType() == ServiceTemplateComponentType.SERVER && associatedComponentsKeys.contains(cmp.getId())) {
                            haveServer = true;
                            break;
                        }
                    }
                }
                String authType = ServiceTemplateUtil.findParameterValue(svcTemplate, component.getId(),
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID);

                if (StringUtils.isNotEmpty(authType)) {
                    for (ServiceTemplateCategory resource : component.getResources()) {
                        if (StringUtils.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, resource.getId())) {
                            for (ServiceTemplateSetting param : resource.getParameters()) {

                                if (authType.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_IQNIP_ID)
                                        && StringUtils.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_IQNORIP_ID, param.getId())) {

                                    param.setRequiredAtDeployment(!haveServer);
                                    param.setRequired(!haveServer);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Response deleteTemplate(String templateId)
            throws WebApplicationException {

        LOGGER.debug("Delete Template Entered for templateID: " + templateId);

        try {
            ServiceTemplateEntity templateEntity = templateDao.deleteTemplate(templateId);
            if (templateEntity != null) {
                // Delete encryption ids
                ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class,
                        templateEntity.getMarshalledTemplateData());
                serviceTemplateUtil.deleteEncryptionIds(template);
                ServiceTemplateUtil.deleteAttachments(template.getId());
            }

            logLocalizableMessage(AsmManagerMessages.serviceTemplateDeleted(templateEntity != null
                    ? templateEntity.getName()
                    : ""));
        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while deleting service template "
                            + templateId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while deleting service template "
                    + templateId, e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        LOGGER.debug("Delete Template Finished for templateID: " + templateId);
        return Response.noContent().build();

    }

    @Override
    public ServiceTemplate[] getAllTemplates(String sort, List<String> filter,
                                             Integer offset, Integer limit, Boolean full) {
        List<ServiceTemplate> templatesArray = new ArrayList<>();

        try {
            // TODO: Need to update code to NOT return passwords.

            // Parse the sort parameter.
            // Any sort exceptions are already encased in a
            // WebApplicationException with an Status code=400
            SortParamParser sp = new SortParamParser(sort, validSortColumns);
            List<SortParamParser.SortInfo> sortInfos = sp.parse();

            // Build filter list from filter params ( comprehensive )
            FilterParamParser filterParser = new FilterParamParser(filter,
                    validFilterColumns);
            List<FilterParamParser.FilterInfo> filterInfos = filterParser
                    .parse();

            PaginationParamParser paginationParamParser = new PaginationParamParser(
                    servletRequest, servletResponse, httpHeaders, uriInfo);

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

            // have to run it twice to determine total records based on check permissions result
            List<ServiceTemplateEntity> entities = templateDao
                    .getAllTemplates(sortInfos, filterInfos, -1, -1);

            if (entities != null) {
                if (pageLimit <= 0) {
                    pageLimit = entities.size();
                }
                int index = 0;
                int startIndex = pageOffSet * pageLimit;
                int endIndex = startIndex + pageLimit;

                Long userId = AsmManagerUtil.getUserId();
                User currentUser = this.asmManagerUtil.getCurrentUser(this.servletRequest);
                for (ServiceTemplateEntity entity : entities) {
                    if (checkUserPermissions(entity, userId, currentUser)) {
                        totalRecords++;
                        if (index >= startIndex && index <= endIndex) {
                            ServiceTemplate template = createTemplateDTO(entity, servletRequest, !full);
                            ServiceTemplateUtil.stripPasswords(template, null);
                            template.setAttachments(ServiceTemplateUtil.getAttachments(template.getId()));
                            templatesArray.add(template);
                        }
                        index++;
                    }
                }
            }

            if (servletRequest != null) {
                PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
                        pageOffSet, pageLimit, totalRecords);

                UriBuilder linkBuilder = RestUtil.getProxyBaseURIBuilder(
                        uriInfo, servletRequest, httpHeaders);

                linkBuilder.replaceQuery(null);
                linkBuilder.path("ServiceTemplate");
                if (sort != null) {
                    linkBuilder.queryParam(AsmConstants.QUERY_PARAM_SORT, sort);
                }

                if (filterInfos != null) {
                    for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
                        if (filterInfo.isSimpleFilter()) {
                            // Simple filter take only one value.
                            linkBuilder.queryParam(filterInfo.getColumnName(),
                                    filterInfo.buildValueString());
                        } else {
                            linkBuilder.queryParam(
                                    AsmConstants.QUERY_PARAM_FILTER,
                                    filterInfo.buildValueString());
                        }
                    }
                }

                // Common library to add link headers in response headers
                paginationParamParser.addLinkHeaders(paginationInfo,
                        linkBuilder);

                int count = templatesArray.size();
                LOGGER.debug("Get All ServiceTemplate Done. Count=" + count);
                servletResponse.setHeader(AsmConstants.DELL_TOTAL_COUNT_HEADER,
                        String.valueOf(totalRecords));
            }
            return templatesArray.toArray(new ServiceTemplate[templatesArray.size()]);

        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while getting All templates ",
                    e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while getting All templates ", e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

    }

    /**
     * Returns full unconditional list of templates.
     * @return
     */
    public ServiceTemplate[] getAllTemplates() {
        return getAllTemplates(null, null, 0, -1, true);
    }

    @Override
    public ServiceTemplate copyTemplate(String templateId, ServiceTemplate configuration) 
            throws WebApplicationException {
        LOGGER.debug("Copy Template Entered for template: " + templateId);

        // check for dup templatename
        ServiceTemplate svcTemplate = null;
        try {

            ServiceTemplateEntity copyTemplate = templateDao.getTemplateById(templateId);
            if (copyTemplate == null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.templateNotFound(templateId));
            }

            ServiceTemplateEntity checkDuplicateNameTemplate = 
                    templateDao.getTemplateByName(configuration.getTemplateName());
            if (checkDuplicateNameTemplate != null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.CONFLICT,
                        AsmManagerMessages.duplicateTemplateName(configuration.getTemplateName()));
            }

            // configuration id should be null if clone
            boolean clone = configuration.getId() == null ? true : false;

            svcTemplate = createTemplateDTO(copyTemplate, servletRequest, false);
            svcTemplate.setCreatedBy(null);
            svcTemplate.setCreatedDate(null);
            svcTemplate.setUpdatedBy(null);
            svcTemplate.setUpdatedDate(null);
            svcTemplate.setLastDeployedDate(null);
            if (!clone) {
                svcTemplate.setId(null);
            }
            svcTemplate.setTemplateName(configuration.getTemplateName());        
            svcTemplate.setTemplateDescription(configuration.getTemplateDescription());
            svcTemplate.setTemplateVersion(StringUtils.isNotBlank(configuration.getTemplateVersion()) ? 
                    configuration.getTemplateVersion() : ServiceTemplateUtil.getCurrentTemplateVersion());
            // get the locked value from copy, not from configuration!
            // otherwise fillMissingParams will not work properly
            svcTemplate.setTemplateLocked(copyTemplate.isTemplateLocked());
            svcTemplate.setTemplateValid(ServiceTemplateValid.getDefaultInstance());
            svcTemplate.setCategory(configuration.getCategory());
            svcTemplate.setEnableApps(configuration.isEnableApps());
            svcTemplate.setEnableCluster(configuration.isEnableCluster());
            svcTemplate.setEnableServer(configuration.isEnableServer());
            svcTemplate.setEnableStorage(configuration.isEnableStorage());
            svcTemplate.setEnableVMs(configuration.isEnableVMs());
            svcTemplate.setManageFirmware(configuration.isManageFirmware());
            svcTemplate.setUseDefaultCatalog(configuration.isUseDefaultCatalog());
            svcTemplate.setAllUsersAllowed(configuration.isAllUsersAllowed());
            svcTemplate.setFirmwareRepository(configuration.getFirmwareRepository());

            fillMissingParams(svcTemplate);
            // restore locked attribute
            svcTemplate.setTemplateLocked(configuration.isTemplateLocked());

            serviceTemplateUtil.duplicatePasswords(svcTemplate);
            // ASM-3362 - Fix. We need to make a copy of the template and decrypt the passwords before validating
            // otherwise the validator will compare the encrypted password UUIDs, which should "always" be
            // unique and therefore fail when running the password confirm validation
            String tempXml = MarshalUtil.marshal(svcTemplate);
            ServiceTemplate templateToValidate = MarshalUtil.unmarshal(ServiceTemplate.class, tempXml);
            serviceTemplateUtil.decryptPasswords(templateToValidate);

            // We ONLY validate tempaltes that are not default templates (default templates are locked)
            if (!copyTemplate.isTemplateLocked()) {
                // validate the template update
                serviceTemplateValidator.validateTemplate(templateToValidate,
                        new ServiceTemplateValidator.ValidationOptions(false, true, true));
                // copy the validation info
                serviceTemplateValidator.copyAllServiceTemplateValidations(templateToValidate, svcTemplate);
            }
            // always set to draft
            svcTemplate.setDraft(Boolean.TRUE);

            // if clone do not persist to db.
            if (!clone) {
                ServiceTemplateEntity entityToPersist = createTemplateEntity(svcTemplate);

                ServiceTemplateEntity persisted = templateDao.createTemplate(entityToPersist);
                svcTemplate.setId(persisted.getTemplateId());

                firmwareUtil.manageServiceTemplateFirmware(svcTemplate, entityToPersist);

                // now we have ID and can add user map
                if (configuration.getAssignedUsers() != null) {
                    for (User user : configuration.getAssignedUsers()) {
                        TemplateUserRefEntity de = new TemplateUserRefEntity();
                        de.setUserId(user.getUserSeqId());
                        de.setTemplateId(svcTemplate.getId());
                        entityToPersist.getAssignedUserList().add(de);
                    }
                }

                List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(true);
                // update add on module references.
                updateAddOnModulesOnTemplate(addOnModuleComponentEntities, svcTemplate, entityToPersist);

                templateDao.updateTemplate(entityToPersist);

                logLocalizableMessage(AsmManagerMessages.serviceTemplateCreated(svcTemplate.getTemplateName()));
            } else {
                addServiceTemplateConfiguration(svcTemplate);
            }
        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while copying service template "
                            + templateId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while copying service template "
                    + templateId, e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        LOGGER.debug("Copy Template Finished for template: " + templateId);
        return svcTemplate;

    }

    @Override
    public ServiceTemplate mapToPhysicalResources(String templateId)
            throws WebApplicationException {
        LOGGER.trace("mapToPhysicalResources Entered for template: " + templateId);

        // check for dup templatename
        ServiceTemplate svcTemplate = null;
        try {
            ServiceTemplateEntity te = templateDao.getTemplateById(templateId);
            if (te == null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.templateNotFound(templateId));
            }
        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while copying service template "
                            + templateId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while copying service template "
                    + templateId, e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        LOGGER.trace("mapToPhysicalResources Finished for template: "
                + templateId);
        return svcTemplate;
    }

    public static ServiceTemplateEntity createTemplateEntity(ServiceTemplate templateDTO) {
        ServiceTemplateEntity te = new ServiceTemplateEntity();
        te.setTemplateId(templateDTO.getId());
        te.setName(templateDTO.getTemplateName());
        te.setTemplateDesc(templateDTO.getTemplateDescription());
        te.setTemplateVersion(StringUtils.isBlank(templateDTO.getTemplateVersion()) ?
                ServiceTemplateUtil.getCurrentTemplateVersion() : templateDTO.getTemplateVersion());
        te.setTemplateValid(templateDTO.getTemplateValid().isValid());
        te.setTemplateLocked(templateDTO.isTemplateLocked());
        te.setWizardPageNumber(templateDTO.getWizardPageNumber());
        te.setDraft(templateDTO.isDraft());
        te.setManageFirmware(templateDTO.isManageFirmware());
        te.setUseDefaultCatalog(templateDTO.isUseDefaultCatalog());

        // save the templateDTO from the gui as xml blob
        te.setMarshalledTemplateData(MarshalUtil.marshal(templateDTO));
        te.setAllUsersAllowed(templateDTO.isAllUsersAllowed());
        te.setAssignedUserList(new HashSet<TemplateUserRefEntity>());

        return te;
    }

    /**
     * Unmarshal template data from stored string.
     * @param templateEntity
     * @param servletRequest
     * @param brief If true, do not include data not related to Templates list in UI
     * @return
     */
    public ServiceTemplate createTemplateDTO(
            ServiceTemplateEntity templateEntity,
            HttpServletRequest servletRequest, boolean brief) {
        if (templateEntity == null) {
            return null;
        }

        ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class,
                templateEntity.getMarshalledTemplateData());

        template.setId(templateEntity.getTemplateId());
        template.setCreatedBy(templateEntity.getCreatedBy());
        template.setCreatedDate(templateEntity.getCreatedDate());
        template.setUpdatedBy(templateEntity.getUpdatedBy());
        template.setUpdatedDate(templateEntity.getUpdatedDate());
        template.setLastDeployedDate(templateEntity.getLastDeployedDate());
        template.setAllUsersAllowed(templateEntity.isAllUsersAllowed());
        template.setDraft(templateEntity.isDraft());
        template.setManageFirmware(templateEntity.isManageFirmware());
        template.setUseDefaultCatalog(templateEntity.isUseDefaultCatalog());

        if (!brief && templateEntity.isManageFirmware() && templateEntity.getFirmwareRepositoryEntity() != null) {
            template.setFirmwareRepository(templateEntity.getFirmwareRepositoryEntity().getSimpleFirmwareRepository());
        }
        if (StringUtils.isBlank(template.getTemplateVersion())) { // set as default if missing
            template.setTemplateVersion(ServiceTemplateUtil.getCurrentTemplateVersion());
        }
        if (template.getTemplateValid() == null) { // if template is not set yet use the default
            template.setTemplateValid(ServiceTemplateValid.getDefaultInstance());
        }
        template.setTemplateLocked(templateEntity.isTemplateLocked());

        if (servletRequest != null) {
            //only reset assigned users and update the template if servlet request present
            template.setAssignedUsers(new HashSet<User>());
            IUserResource adminProxy = ProxyUtil.getAdminProxy();
            ProxyUtil.setProxyHeaders(adminProxy, servletRequest);
            if (templateEntity.getAssignedUserList() != null) {
                for (TemplateUserRefEntity ref : templateEntity.getAssignedUserList()) {
                    try {
                        User user = adminProxy.getUser(ref.getUserId());
                        template.getAssignedUsers().add(user);
                    } catch (LocalizedWebApplicationException lwae) {
                        LOGGER.warn("Could not find User in the Users database for user sequence id: " + ref.getUserId(), lwae);
                    }
                }
            }
        }

        for (ServiceTemplateComponent component : template.getComponents()) {
            if (brief) {
                // minimize template content, need only component name
                component.setResources(new ArrayList<ServiceTemplateCategory>());
            }else {
                if (component.checkMinimalServerComponent()) {
                    ServiceTemplateCategory serverCategoryOS = template.getTemplateResource(component, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                    for (ServiceTemplateSetting setting : serverCategoryOS.getParameters()) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID.equals(setting.getDependencyTarget())) {
                            setting.setDependencyTarget(null);
                            setting.setDependencyValue(null);
                        }
                        List<ServiceTemplateOption> options = setting.getOptions();
                        if (options != null) {
                            for (ServiceTemplateOption option : options) {
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID.equals(option.getDependencyTarget())) {
                                    option.setDependencyTarget(null);
                                    option.setDependencyValue(null);
                                }
                            }
                        }
                    }
                }
            }
        }
        return template;
    }

    /**
     * Retrieves a ServiceTemplate from either a JSON or XML file. Will be
     * resolved from the classpath or TomEE config location using
     * ConfigurationUtils.resolvePropertiesFile See that method for more details
     * on file resolution.
     *
     * @return the template
     */
    private ServiceTemplate getDefaultTemplateFromFile() {
        ServiceTemplate ret;
        String path = System.getProperty(PROP_DEFAULT_TEMPLATE_PATH);
        if (StringUtils.isBlank(path)) {
            ret = null;
        } else {
            try {
                URL url = ConfigurationUtils.resolvePropertiesFile(path,
                        ServiceTemplateService.class.getClassLoader());
                String text = IOUtils.toString(url);
                if (text.startsWith("{") || text.startsWith("[")) {
                    ret = MarshalUtil.fromJSON(ServiceTemplate.class, text);
                } else {
                    ret = MarshalUtil.unmarshal(ServiceTemplate.class, text);
                }
            } catch (IllegalStateException e) {
                LOGGER.error("Failed to unmarshal default template file at "
                        + path, e);
                ret = null;
            } catch (IOException e) {
                LOGGER.error("Failed to read sample default template at "
                        + path, e);
                ret = null;
            }
        }

        return ret;
    }

    /**
     * Retrieves a ServiceTemplateComponent from either a JSON or XML file. Will
     * be resolved from the classpath or TomEE config location using
     * ConfigurationUtils.resolvePropertiesFile See that method for more details
     * on file resolution.
     * 
     * @return the component
     */
    protected static ServiceTemplateComponent getComponentFromConfigFile(
            String path) {
        ServiceTemplateComponent ret;
        try {
            URL url = ConfigurationUtils.resolvePropertiesFile(path,
                    ServiceTemplateService.class.getClassLoader());
            String text = IOUtils.toString(url);
            if (text.startsWith("{") || text.startsWith("[")) {
                ret = MarshalUtil
                        .fromJSON(ServiceTemplateComponent.class, text);
            } else {
                ret = MarshalUtil.unmarshal(ServiceTemplateComponent.class,
                        text);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("Failed to unmarshal service component file at "
                    + path, e);
            ret = null;
        } catch (IOException e) {
            LOGGER.error("Failed to read service component at " + path, e);
            ret = null;
        }

        return ret;
    }

    ServiceTemplateCategory buildServerOsSettings(final List<RazorRepo> razorImages,
                                                  final Map<String, String> imageTargets,
                                                  final Map<String, String> repoNames,
                                                  boolean minServer) {
        ServiceTemplateCategory serverCategoryOS = new ServiceTemplateCategory();
        serverCategoryOS.setDisplayName("OS Settings");
        serverCategoryOS.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        if (razorImages != null && razorImages.size() > 0) {
            ServiceTemplateSetting serverSettingGenerateHostName = new ServiceTemplateSetting();
            serverSettingGenerateHostName.setDisplayName("Auto-generate Host Name");
            serverSettingGenerateHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
            serverSettingGenerateHostName.setType(ServiceTemplateSettingType.BOOLEAN);
            serverSettingGenerateHostName.setRequired(false);
            serverSettingGenerateHostName.setRequiredAtDeployment(false);
            serverSettingGenerateHostName.setHideFromTemplate(false);
            serverSettingGenerateHostName.setValue("false");
            serverSettingGenerateHostName.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
            serverSettingGenerateHostName.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN);
            serverSettingGenerateHostName.setToolTip("Use the Host Name Template field to auto-generate " +
                    "host names at deployment time. If not selected, a unique host name will be requested " +
                    "when the template is deployed.");
            serverCategoryOS.getParameters().add(serverSettingGenerateHostName);

            ServiceTemplateSetting serverSettingHostNameTemplate = new ServiceTemplateSetting();
            serverSettingHostNameTemplate.setDisplayName("Host Name Template");
            serverSettingHostNameTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID);
            serverSettingHostNameTemplate.setType(ServiceTemplateSettingType.STRING);
            serverSettingHostNameTemplate.setRequired(true);
            serverSettingHostNameTemplate.setRequiredAtDeployment(false);
            serverSettingHostNameTemplate.setHideFromTemplate(false);
            serverSettingHostNameTemplate.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_DEFAULT_TEMPLATE);
            serverSettingHostNameTemplate.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
            serverSettingHostNameTemplate.setDependencyValue("true");
            serverSettingHostNameTemplate.setToolTip("Template used to generate host names at deployment " +
                    "time. Must contain variables that will produce a unique host name. Allowed variables " +
                    " are ${num} (an auto-generated unique number), ${service_tag}, ${model}, ${vendor}" +
                    " and ${dns} (ASM-assigned static IP will be resolved to hostname by DNS lookup).");
            serverCategoryOS.getParameters().add(serverSettingHostNameTemplate);

            ServiceTemplateSetting serverSettingHostName = new ServiceTemplateSetting();
            serverSettingHostName.setDisplayName("Host Name");
            serverSettingHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
            serverSettingHostName.setType(ServiceTemplateSettingType.STRING);
            serverSettingHostName.setRequired(true);
            serverSettingHostName.setRequiredAtDeployment(true);
            serverSettingHostName.setHideFromTemplate(true);
            serverSettingHostName.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
            serverSettingHostName.setDependencyValue("false");
            serverCategoryOS.getParameters().add(serverSettingHostName);

            ServiceTemplateSetting osType = new ServiceTemplateSetting();
            osType.setDisplayName("OS Image Type");
            osType.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
            osType.setType(ServiceTemplateSettingType.ENUMERATED);
            osType.setRequired(false);
            osType.setHideFromTemplate(true);
            osType.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
            osType.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD + "," +
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN);

            serverCategoryOS.getParameters().add(osType);

            ServiceTemplateSetting osImage = new ServiceTemplateSetting();
            osImage.setDisplayName("OS Image");
            osImage.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_OS_IMAGE_TOOLTIP);
            osImage.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            osImage.setType(ServiceTemplateSettingType.ENUMERATED);
            osImage.setRequired(true);
            osImage.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
            osImage.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN);

            ServiceTemplateUtil.addDefaultSelectOption(osImage);
            ServiceTemplateUtil.addOSImagesAsOptions(razorImages, repoNames, osImage);
            String windowsTargets = StringUtils.defaultIfBlank(
                    Joiner.on(",").skipNulls().join(
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE)),
                    NO_MATCHING_VALUE);
            String allLinuxTargets = StringUtils.defaultIfBlank(
                    Joiner.on(",").skipNulls().join(
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE11_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE12_VALUE)),
                    NO_MATCHING_VALUE);
            serverCategoryOS.getParameters().add(osImage);



            if (!minServer) {
                // Add flag for hyperv deployment
                String windows2012Targets = imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE);
                if(windows2012Targets == null) {
                    windows2012Targets = NO_MATCHING_VALUE;
                }
                ServiceTemplateSetting hypervDeployment = new ServiceTemplateSetting();
                hypervDeployment.setDisplayName("Install HyperV");
                hypervDeployment.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
                hypervDeployment.setType(ServiceTemplateSettingType.BOOLEAN);
                hypervDeployment.setRequired(false);
                hypervDeployment.setValue("false");
                hypervDeployment.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                hypervDeployment.setDependencyValue(windows2012Targets);
                serverCategoryOS.getParameters().add(hypervDeployment);
            }
            ServiceTemplateSetting osSubType = new ServiceTemplateSetting();
            osSubType.setDisplayName("OS Image Version");
            osSubType.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_VERSION_ID);
            osSubType.setType(ServiceTemplateSettingType.ENUMERATED);
            osSubType.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            osSubType.setDependencyValue(windowsTargets);
            ServiceTemplateUtil.addDefaultSelectOption(osSubType);
            addWindowsFlavors(osSubType, imageTargets);
            serverCategoryOS.getParameters().add(osSubType);


            ServiceTemplateSetting serverSettingHypervisorAdminPassword = new ServiceTemplateSetting();
            serverSettingHypervisorAdminPassword
                    .setDisplayName("Administrator password");
            serverSettingHypervisorAdminPassword.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ADM_PASSWORD_TOOLTIP);
            serverSettingHypervisorAdminPassword
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
            serverSettingHypervisorAdminPassword
                    .setType(ServiceTemplateSettingType.PASSWORD);
            serverSettingHypervisorAdminPassword.setRequired(true);
            serverSettingHypervisorAdminPassword.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
            serverSettingHypervisorAdminPassword.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN);
            serverSettingHypervisorAdminPassword.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);

            serverCategoryOS.getParameters().add(serverSettingHypervisorAdminPassword);

            ServiceTemplateSetting serverSettingHypervisorAdminConfirmPassword = new ServiceTemplateSetting();
            serverSettingHypervisorAdminConfirmPassword.setDisplayName("Confirm administrator password");
            serverSettingHypervisorAdminConfirmPassword
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID);
            serverSettingHypervisorAdminConfirmPassword.setType(ServiceTemplateSettingType.PASSWORD);
            serverSettingHypervisorAdminConfirmPassword.setRequired(true);
            serverSettingHypervisorAdminConfirmPassword.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
            serverSettingHypervisorAdminConfirmPassword.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN + ","
                    + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD +
                    "," + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN);
            serverSettingHypervisorAdminConfirmPassword.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);
            
            serverCategoryOS.getParameters().add(
                    serverSettingHypervisorAdminConfirmPassword);

            ServiceTemplateSetting serverSettingHVProdKey = new ServiceTemplateSetting();
            serverSettingHVProdKey.setDisplayName("Product Key");
            serverSettingHVProdKey.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_PROD_KEY_ID);
            serverSettingHVProdKey.setType(ServiceTemplateSettingType.STRING);
            serverSettingHVProdKey.setRequired(true);
            serverSettingHVProdKey.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingHVProdKey.setDependencyValue(windowsTargets);
            serverCategoryOS.getParameters().add(serverSettingHVProdKey);

            ServiceTemplateSetting serverSettingHVTimezone = new ServiceTemplateSetting();
            serverSettingHVTimezone.setDisplayName("Timezone");
            serverSettingHVTimezone.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_TIMEZONE_ID);
            serverSettingHVTimezone.setType(ServiceTemplateSettingType.ENUMERATED);
            populateTimezones(serverSettingHVTimezone);
            serverSettingHVTimezone.setRequired(true);
            serverSettingHVTimezone.setValue("Central Standard Time");
            serverSettingHVTimezone.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
            serverSettingHVTimezone.setDependencyValue("true");
            serverCategoryOS.getParameters().add(serverSettingHVTimezone);

            ServiceTemplateSetting serverSettingTimezone = new ServiceTemplateSetting();
            serverSettingTimezone.setDisplayName("Timezone");
            serverSettingTimezone.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_TIMEZONE_ID);
            serverSettingTimezone.setType(ServiceTemplateSettingType.ENUMERATED);
            populateLinuxTimezones(serverSettingTimezone);
            serverSettingTimezone.setRequired(false);
            serverSettingTimezone.setValue("11"); // Central time
            serverSettingTimezone.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingTimezone.setDependencyValue(allLinuxTargets);
            serverCategoryOS.getParameters().add(serverSettingTimezone);

            String ntpTargets = StringUtils.defaultIfBlank(
                    Joiner.on(",").skipNulls().join(
                            allLinuxTargets,
                            windowsTargets,
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE )),
                    NO_MATCHING_VALUE);
            ServiceTemplateSetting serverSettingLinuxNTP = new ServiceTemplateSetting();
            serverSettingLinuxNTP.setDisplayName("NTP Server");
            serverSettingLinuxNTP.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID);
            serverSettingLinuxNTP.setType(ServiceTemplateSettingType.STRING);
            serverSettingLinuxNTP.setRequired(false);
            serverSettingLinuxNTP.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NTP_TOOLTIP);
            serverSettingLinuxNTP.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingLinuxNTP.setDependencyValue(ntpTargets);

            NTPSetting ntpSet = ProxyUtil.getAlcmNTPProxy().getNTPSettings();
            if (StringUtils.isNotEmpty(ntpSet.getPreferredNTPServer())) {
                serverSettingLinuxNTP.setValue(ntpSet.getPreferredNTPServer());
            }

            serverCategoryOS.getParameters().add(serverSettingLinuxNTP);

            ServiceTemplateSetting serverSettingHVLang = new ServiceTemplateSetting();
            serverSettingHVLang.setDisplayName("Language");
            serverSettingHVLang.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_LANG_ID);
            serverSettingHVLang.setType(ServiceTemplateSettingType.ENUMERATED);
            populateLang(serverSettingHVLang);
            serverSettingHVLang.setRequired(true);
            serverSettingHVLang.setValue("en-US");
            serverSettingHVLang.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
            serverSettingHVLang.setDependencyValue("true");
            serverCategoryOS.getParameters().add(serverSettingHVLang);

            ServiceTemplateSetting serverSettingHVKeyboard = new ServiceTemplateSetting();
            serverSettingHVKeyboard.setDisplayName("Keyboard");
            serverSettingHVKeyboard.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_KEYBOARD_ID);
            serverSettingHVKeyboard.setType(ServiceTemplateSettingType.ENUMERATED);
            populateKeyboard(serverSettingHVKeyboard);
            serverSettingHVKeyboard.setRequired(true);
            serverSettingHVKeyboard.setValue("00000409");
            serverSettingHVKeyboard.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
            serverSettingHVKeyboard.setDependencyValue("true");
            serverCategoryOS.getParameters().add(serverSettingHVKeyboard);

            ServiceTemplateSetting serverSettingHVDN = new ServiceTemplateSetting();
            serverSettingHVDN.setDisplayName("Domain Name");
            serverSettingHVDN.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_DN_ID);
            serverSettingHVDN.setType(ServiceTemplateSettingType.STRING);
            serverSettingHVDN.setRequired(false);
            serverSettingHVDN.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingHVDN.setDependencyValue(windowsTargets);
            serverCategoryOS.getParameters().add(serverSettingHVDN);

            ServiceTemplateSetting serverSettingHVFQDN = new ServiceTemplateSetting();
            serverSettingHVFQDN.setDisplayName("FQ Domain Name");
            serverSettingHVFQDN.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_FQDN_ID);
            serverSettingHVFQDN.setType(ServiceTemplateSettingType.STRING);
            serverSettingHVFQDN.setRequired(false);
            serverSettingHVFQDN.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
            serverSettingHVFQDN.setDependencyValue("true");
            serverCategoryOS.getParameters().add(serverSettingHVFQDN);

            ServiceTemplateSetting serverSettingHVAdminLogin = new ServiceTemplateSetting();
            serverSettingHVAdminLogin.setDisplayName("Domain Admin Username");
            serverSettingHVAdminLogin.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_ADMIN_LOGIN_ID);
            serverSettingHVAdminLogin.setType(ServiceTemplateSettingType.STRING);
            serverSettingHVAdminLogin.setRequired(false);
            serverSettingHVAdminLogin.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingHVAdminLogin.setDependencyValue(windowsTargets);

            serverCategoryOS.getParameters().add(serverSettingHVAdminLogin);

            ServiceTemplateSetting serverSettingHVAdminPass = new ServiceTemplateSetting();
            serverSettingHVAdminPass.setDisplayName("Domain Admin Password");
            serverSettingHVAdminPass.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID);
            serverSettingHVAdminPass.setType(ServiceTemplateSettingType.PASSWORD);
            serverSettingHVAdminPass.setRequired(false);
            serverSettingHVAdminPass.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingHVAdminPass.setDependencyValue(windowsTargets);
            serverSettingHVAdminPass.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);

            serverCategoryOS.getParameters().add(serverSettingHVAdminPass);

            ServiceTemplateSetting serverSettingHVAdminPassConfirm = new ServiceTemplateSetting();
            serverSettingHVAdminPassConfirm.setDisplayName("Domain Admin Password Confirm");
            serverSettingHVAdminPassConfirm.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID);
            serverSettingHVAdminPassConfirm.setType(ServiceTemplateSettingType.PASSWORD);
            serverSettingHVAdminPassConfirm.setRequired(false);
            serverSettingHVAdminPassConfirm.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingHVAdminPassConfirm.setDependencyValue(windowsTargets);
            serverSettingHVAdminPassConfirm.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);

            serverCategoryOS.getParameters().add(serverSettingHVAdminPassConfirm);

            if (!minServer) {
                ServiceTemplateSetting iscsi = new ServiceTemplateSetting();
                iscsi.setDisplayName("Select iSCSI Initiator");
                iscsi.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ISCSI_ID);
                iscsi.setType(ServiceTemplateSettingType.ENUMERATED);
                iscsi.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                iscsi.setDependencyValue(imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE));
                iscsi.setRequired(true);

                ServiceTemplateOption[] options = new ServiceTemplateOption[] {
                        new ServiceTemplateOption("Hardware Initiator",
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ISCSI_HARDWARE_ID, null, null),
                        new ServiceTemplateOption("Software Initiator",
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ISCSI_SOFTWARE_ID, null, null),
                };
                iscsi.getOptions().addAll(Arrays.asList(options));
                ServiceTemplateUtil.addDefaultSelectOption(iscsi);
                serverCategoryOS.getParameters().add(iscsi);

                ServiceTemplateSetting eqlMem = new ServiceTemplateSetting();
                eqlMem.setDisplayName("Install EqualLogic MEM");
                eqlMem.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_EQL_MEM_ID);
                eqlMem.setType(ServiceTemplateSettingType.ENUMERATED);
                eqlMem.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                eqlMem.setDependencyValue(imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE));
                eqlMem.setRequired(true);

                ServiceTemplateUtil.addBooleanSelectOption(eqlMem);
                serverCategoryOS.getParameters().add(eqlMem);

                ServiceTemplateSetting localStorageSetting = new ServiceTemplateSetting();
                localStorageSetting.setDisplayName("Local storage for Vmware vSAN");
                localStorageSetting.setId(ServiceTemplateSettingIDs.LOCAL_STORAGE_ID);
                localStorageSetting.setType(ServiceTemplateSettingType.BOOLEAN);
                localStorageSetting.setRequired(false);
                localStorageSetting.setValue("false");
                localStorageSetting.setToolTip(ServiceTemplateSettingIDs.LOCAL_STORAGE_TOOLTIP);
                localStorageSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                localStorageSetting.setDependencyValue(imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE));

                serverCategoryOS.getParameters().add(localStorageSetting);

                ServiceTemplateSetting localStorageTypeSetting = new ServiceTemplateSetting();
                localStorageTypeSetting.setDisplayName("Local storage type");
                localStorageTypeSetting.setId(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID);
                localStorageTypeSetting.setType(ServiceTemplateSettingType.RADIO);
                localStorageTypeSetting.setRequired(false);

                localStorageTypeSetting.getOptions().add(new ServiceTemplateOption("All flash",
                        ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH, null, null));
                localStorageTypeSetting.getOptions().add(new ServiceTemplateOption("Hybrid",
                        ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_HYBRID, null, null));
                localStorageTypeSetting.setValue(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH);

                localStorageTypeSetting.setDependencyTarget(ServiceTemplateSettingIDs.LOCAL_STORAGE_ID);
                localStorageTypeSetting.setDependencyValue("true");

                serverCategoryOS.getParameters().add(localStorageTypeSetting);

            }

        }
        return serverCategoryOS;
    }

    ServiceTemplateCategory buildServerHardwareSettings(List<DeviceGroupEntity> deviceGroups, boolean hardwareOnly) {
        ServiceTemplateCategory serverCategoryHW = new ServiceTemplateCategory();
        serverCategoryHW.setDisplayName("Hardware Settings");
        serverCategoryHW.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE);

        // add default settings for this physical server, BIOS settings
        ServiceTemplateSetting serverSettingTargetBoot = new ServiceTemplateSetting();
        serverSettingTargetBoot.setDisplayName("Target Boot Device");
        serverSettingTargetBoot
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
        serverSettingTargetBoot.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingTargetBoot.setOptionsSortable(false);
        serverSettingTargetBoot.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_BOOT_TARGET_TOOLTIP);

        if (hardwareOnly) {
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Local Hard Drive",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD, null, null));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("SD with RAID enabled",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID, null, null));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("SD with RAID disabled",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD, null, null));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Boot From SAN (iSCSI)",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI, null, null));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Boot From SAN (FC)",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC, null, null));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("None",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE, null, null));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("None (With RAID Configuration)",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID, null, null));

        }else{
            
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Local Hard Drive",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("SD with RAID enabled",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("SD with RAID disabled",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("SD with RAID enabled for VMWare vSAN",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "true"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Local Disk For VMware vSAN",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "true"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Boot From SAN (iSCSI)",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("Boot From SAN (FC)",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("None",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));
            serverSettingTargetBoot.getOptions().add(new ServiceTemplateOption("None (With RAID Configuration)",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID, ServiceTemplateSettingIDs.LOCAL_STORAGE_ID, "false"));

        }

        serverSettingTargetBoot.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD);
        serverCategoryHW.getParameters().add(serverSettingTargetBoot);

        // RAID
        ServiceTemplateSetting serverSettingRAID = new ServiceTemplateSetting();
        serverSettingRAID.setDisplayName("RAID");
        serverSettingRAID.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_RAID_ID);
        serverSettingRAID.setType(ServiceTemplateSettingType.RAIDCONFIGURATION);
        serverSettingRAID.setValue("");
        serverSettingRAID.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
        serverSettingRAID.setDependencyValue(Joiner.on(',').join(
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID));
        serverCategoryHW.getParameters().add(serverSettingRAID);
        return serverCategoryHW;
    }

    /**
     * BIOS basic settings
     * @return
     */
    ServiceTemplateCategory buildServerBIOSSettings() {
        ServiceTemplateCategory serverCategoryHW = new ServiceTemplateCategory();
        serverCategoryHW.setDisplayName("BIOS Settings");
        serverCategoryHW.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE);

        // Basic or Advanced
        ServiceTemplateSetting biosConfiguration = new ServiceTemplateSetting();
        biosConfiguration.setDisplayName("BIOS Settings");
        biosConfiguration.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_CONFIG_ID);
        biosConfiguration.setType(ServiceTemplateSettingType.BIOSCONFIGURATION);
        biosConfiguration.setValue("basic");
        biosConfiguration.setRequired(false);
        biosConfiguration.setHideFromTemplate(true); // initially hidden
        serverCategoryHW.getParameters().add(0,biosConfiguration); // must go first

        // even though the settings don't have group name here they must be grouped exactly the same way as they appear on import

        // Add HW BIOS setting for System Profile
        ServiceTemplateSetting serverSettingSystemProfile = new ServiceTemplateSetting();
        serverSettingSystemProfile.setDisplayName("System Profile");
        serverSettingSystemProfile.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SYSTEM_PROFILE_ID);
        serverSettingSystemProfile.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingSystemProfile.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingSystemProfile.getOptions().add(new ServiceTemplateOption("Performance Per Watt (DAPC)", "PerfPerWattOptimizedDapc", null, null));
        serverSettingSystemProfile.getOptions().add(new ServiceTemplateOption("Performance Per Watt (OS)", "PerfPerWattOptimizedOs", null, null));
        serverSettingSystemProfile.getOptions().add(new ServiceTemplateOption("Performance", "PerfOptimized", null, null));
        serverSettingSystemProfile.getOptions().add(new ServiceTemplateOption("Dense Configuration", "DenseCfgOptimized", null, null));
        serverSettingSystemProfile.getOptions().add(new ServiceTemplateOption("Custom", "Custom", null, null));
        serverSettingSystemProfile.setValue("PerfOptimized");
        serverCategoryHW.getParameters().add(serverSettingSystemProfile);

        // Add HW BIOS setting for usb ports
        ServiceTemplateSetting serverSettingUSBPorts = new ServiceTemplateSetting();
        serverSettingUSBPorts.setDisplayName("User Accessible USB Ports");
        serverSettingUSBPorts.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_USB_PORTS_ID);
        serverSettingUSBPorts.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingUSBPorts.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingUSBPorts.getOptions().add(new ServiceTemplateOption("All Ports On", "AllOn", null, null));
        serverSettingUSBPorts.getOptions().add(new ServiceTemplateOption("All Ports Off", "AllOff", null, null));
        serverSettingUSBPorts.getOptions().add(new ServiceTemplateOption("Only Back Ports On", "OnlyBackPortsOn", null, null));
        serverSettingUSBPorts.setValue("AllOn");
        serverCategoryHW.getParameters().add(serverSettingUSBPorts);

        // Add HW BIOS setting for num cores
        ServiceTemplateSetting serverSettingNumProcessors = new ServiceTemplateSetting();
        serverSettingNumProcessors.setDisplayName("Number of Cores per Processor");
        serverSettingNumProcessors.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NUM_PROCS_ID);
        serverSettingNumProcessors.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingNumProcessors.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingNumProcessors.getOptions().add(new ServiceTemplateOption("All", "All", null, null));
        serverSettingNumProcessors.getOptions().add(new ServiceTemplateOption("1", "1", null, null));
        serverSettingNumProcessors.getOptions().add(new ServiceTemplateOption("2", "2", null, null));
        serverSettingNumProcessors.getOptions().add(new ServiceTemplateOption("4", "4", null, null));
        serverSettingNumProcessors.getOptions().add(new ServiceTemplateOption("6", "6", null, null));
        serverSettingNumProcessors.setValue("All");
        serverCategoryHW.getParameters().add(serverSettingNumProcessors);

        // Add HW BIOS setting for VT
        ServiceTemplateSetting serverSettingVirtualization = new ServiceTemplateSetting();
        serverSettingVirtualization.setDisplayName("Virtualization Technology");
        serverSettingVirtualization.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_VIRTUALIZATION_ID);
        serverSettingVirtualization.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingVirtualization.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingVirtualization.getOptions().add(new ServiceTemplateOption("Enabled", "Enabled", null, null));
        serverSettingVirtualization.getOptions().add(new ServiceTemplateOption("Disabled", "Disabled", null, null));
        serverSettingVirtualization.setRequired(false);
        serverSettingVirtualization.setValue("Enabled");
        serverCategoryHW.getParameters().add(serverSettingVirtualization);

        // Add HW BIOS setting for logical processors
        ServiceTemplateSetting serverSettingLogicalProc = new ServiceTemplateSetting();
        serverSettingLogicalProc.setDisplayName("Logical Processor");
        serverSettingLogicalProc.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_LOGICAL_PROC_ID);
        serverSettingLogicalProc.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingLogicalProc.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingLogicalProc.getOptions().add(new ServiceTemplateOption("Enabled", "Enabled", null, null));
        serverSettingLogicalProc.getOptions().add(new ServiceTemplateOption("Disabled", "Disabled", null, null));
        serverSettingLogicalProc.setRequired(false);
        serverSettingLogicalProc.setValue("Enabled");
        serverCategoryHW.getParameters().add(serverSettingLogicalProc);

        // Add HW BIOS setting for execute disable
        ServiceTemplateSetting serverSettingExecDisable = new ServiceTemplateSetting();
        serverSettingExecDisable.setDisplayName("Execute Disable");
        serverSettingExecDisable.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_EXECUTE_DISABLE_ID);
        serverSettingExecDisable.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingExecDisable.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingExecDisable.getOptions().add(new ServiceTemplateOption("Enabled", "Enabled", null, null));
        serverSettingExecDisable.getOptions().add(new ServiceTemplateOption("Disabled", "Disabled", null, null));
        serverSettingExecDisable.setRequired(false);
        serverSettingExecDisable.setValue("Enabled");
        serverCategoryHW.getParameters().add(serverSettingExecDisable);

        // Add HW BIOS setting for mem interleave
        ServiceTemplateSetting serverSettingMemNodeInter = new ServiceTemplateSetting();
        serverSettingMemNodeInter.setDisplayName("Node Interleaving");
        serverSettingMemNodeInter.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MEM_NODE_INTERLEAVE_ID);
        serverSettingMemNodeInter.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingMemNodeInter.getOptions().add(0, new ServiceTemplateOption("Not Applicable","n/a",null,null));
        serverSettingMemNodeInter.getOptions().add(new ServiceTemplateOption("Enabled", "Enabled", null, null));
        serverSettingMemNodeInter.getOptions().add(new ServiceTemplateOption("Disabled", "Disabled", null, null));
        serverSettingMemNodeInter.setRequired(false);
        serverSettingMemNodeInter.setValue("Enabled");
        serverCategoryHW.getParameters().add(serverSettingMemNodeInter);

        return serverCategoryHW;
    }

    /**
     * Loop through
     * @param svcTemplate
     */
    private void updateStaticNetworkSettings(ServiceTemplate svcTemplate) {

        Map<String,Network> networksMap = new HashMap<>();
        List<String> allNames = new ArrayList<>();
        for (ServiceTemplateComponent component : svcTemplate.getComponents()) {
            if (ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                    ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                networkingUtil.massageComponentNetworks(component,networkService, allNames, networksMap);
                Set<Network> networks = new LinkedHashSet<>(ServiceTemplateClientUtil.findStaticNetworks(component));
                mapStaticNetworkOSSettings(component, networks);
            }
        }
    }

    private void addServerPoolSetting(ServiceTemplateCategory category, List<DeviceGroupEntity> deviceGroups) {
        ServiceTemplateSetting serverSource = new ServiceTemplateSetting();
        serverSource.setDisplayName("Server Source");
        serverSource.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
        serverSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL);
        serverSource.setType(ServiceTemplateSettingType.ENUMERATED);
        serverSource.setToolTip("Method for server selection");
        serverSource.setRequired(true);
        serverSource.setRequiredAtDeployment(true);
        serverSource.setHideFromTemplate(true);
        ServiceTemplateOption[] options = new ServiceTemplateOption[] {
                new ServiceTemplateOption("Server Pool", ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL, null, null),
                new ServiceTemplateOption("Manual Entry", ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL, null, null),
        };
        serverSource.getOptions().addAll(Arrays.asList(options));
        category.getParameters().add(serverSource);

        ServiceTemplateSetting serverPool = new ServiceTemplateSetting();
        serverPool.setDisplayName("Server Pool");
        serverPool.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_TOOLTIP);
        serverPool.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);
        serverPool.setType(ServiceTemplateSettingType.ENUMERATED);
        serverPool.setRequiredAtDeployment(true);
        serverPool.setRequired(true);
        serverPool.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
        serverPool.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL);
        ServiceTemplateUtil.addServerPoolOptions(deviceGroups,serverPool);
        category.getParameters().add(serverPool);

        // Option to pick a specific server at deployment time
        ServiceTemplateSetting selectServerSetting = new ServiceTemplateSetting(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION, null,
                ServiceTemplateSettingType.ENUMERATED);
        selectServerSetting.setDisplayName("Choose Server");
        selectServerSetting.setToolTip("Select specific server from a drop-down list");
        selectServerSetting.setHideFromTemplate(true);
        selectServerSetting.setRequiredAtDeployment(true);
        selectServerSetting.setRequired(true);
        selectServerSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
        selectServerSetting.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL);

        // Add all servers as options by default. Note that not all will be able to be used because
        // they may fail filtering. This list is really only used in the case where you scale-up
        // a service and select "New component". In all other cases there is a separate call the UI
        // makes to update the server list and enough data is available at that point to only show
        // the servers that would pass filtering.
        Long userId = AsmManagerUtil.getUserId();
        List<DeviceGroupDAO.BriefServerInfo> accessiblePoolServers = deviceGroupDAO.getAccessiblePoolServers(userId,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID);
        int n = Math.min(accessiblePoolServers.size(), MAX_ENUMERATED_LENGTH);
        ServiceTemplateUtil.addDefaultSelectOption(selectServerSetting);
        for (int i = 0; i < n; ++i) {
            DeviceGroupDAO.BriefServerInfo info = accessiblePoolServers.get(i);
            if (DeviceState.READY.equals(info.getState())) {
                selectServerSetting.getOptions().add(new ServiceTemplateOption(
                        info.getServiceTag(), info.getRefId(), null, null));
            }
        }

        category.getParameters().add(selectServerSetting);
    }

    public static ServiceTemplateSetting addDeploymentFailoverSetting(ServiceTemplateCategory category) {
        if (category == null)
            return null;
        ServiceTemplateSetting serverFailover = new ServiceTemplateSetting();
        serverFailover.setDisplayName("Retry On Failure");
        serverFailover.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_MIGRATE_ON_FAIL_TOOLTIP);
        serverFailover.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MIGRATE_ON_FAIL_ID);
        serverFailover.setType(ServiceTemplateSettingType.BOOLEAN);
        serverFailover.setRequiredAtDeployment(true);
        serverFailover.setRequired(false);
        serverFailover.setHideFromTemplate(true);
        serverFailover.setValue("true");
        serverFailover.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
        serverFailover.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL);

        category.getParameters().add(serverFailover);
        return serverFailover;
    }

    public static ServiceTemplateSetting addDeploymentAttemptedSetting(ServiceTemplateCategory category) {
        if (category == null)
            return null;

        ServiceTemplateSetting serverList = new ServiceTemplateSetting();
        serverList.setDisplayName("Attempted Servers");
        serverList.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ATTEMPTED_SERVERS);
        serverList.setType(ServiceTemplateSettingType.STRING);
        serverList.setRequiredAtDeployment(false);
        serverList.setRequired(false);
        serverList.setHideFromTemplate(true);

        category.getParameters().add(serverList);
        return serverList;
    }

    private ServiceTemplateComponent buildServerComponent(List<RazorRepo> razorImages,
                                                          List<DeviceGroupEntity> deviceGroups,
                                                          List<NetworkConfiguration> networks,
                                                          Map<String, String> imageTargets,
                                                          Map<String, String> repoNames) {
        ServiceTemplateComponent server = new ServiceTemplateComponent();
        server.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL);
        server.setName("Server");
        server.setType(ServiceTemplateComponentType.SERVER);

        ServiceTemplateCategory serverCategoryHW = buildServerHardwareSettings(deviceGroups, false);
        addServerPoolSetting(serverCategoryHW, deviceGroups);
        addDeploymentFailoverSetting(serverCategoryHW);
        addDeploymentAttemptedSetting(serverCategoryHW);
        ServiceTemplateCategory serverCategoryOS = buildServerOsSettings(razorImages, imageTargets, repoNames, false);
        ServiceTemplateCategory serverCategoryBIOS = buildServerBIOSSettings();

        ServiceTemplateCategory serverCategoryNetwork = new ServiceTemplateCategory();
        serverCategoryNetwork.setDisplayName("Network Settings");
        serverCategoryNetwork.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);

        ServiceTemplateSetting multiNetworkConfig = new ServiceTemplateSetting();
        multiNetworkConfig.setDisplayName("Network Config");
        multiNetworkConfig.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        multiNetworkConfig.setType(ServiceTemplateSettingType.NETWORKCONFIGURATION);
        multiNetworkConfig.setRequired(false);
        multiNetworkConfig.setValue("");
        serverCategoryNetwork.getParameters().add(multiNetworkConfig);

        ServiceTemplateSetting defaultGateway = setDefaultGatewaySetting(networks, serverCategoryNetwork);

        final Map<String, String> baremetalTargets = ServiceTemplateUtil.processRazorImagesTargets(razorImages, true);
        String osValues = StringUtils.defaultIfBlank(
                Joiner.on(",").skipNulls().join(baremetalTargets.values()),
                null);

        ServiceTemplateSetting mtuSetting = new ServiceTemplateSetting();
        mtuSetting.setDisplayName("MTU size for bonded interfaces:");
        mtuSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MTU_ID);
        mtuSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        mtuSetting.getOptions().add(new ServiceTemplateOption("1500", "1500", null, null));
        mtuSetting.getOptions().add(new ServiceTemplateOption("9000", "9000", null, null));
        mtuSetting.setValue("1500");
        mtuSetting.setToolTip("Allows the Maximum Transfer Unit (MTU) to be set in the server Operating System. " +
                "This will only take effect on bonded interfaces.");
        serverCategoryNetwork.getParameters().add(mtuSetting);

        final Map<String, String> linuxTargets = ServiceTemplateUtil.getLinuxRazorImageTargets(razorImages);
        String linuxOSValues = StringUtils.defaultIfBlank(
                Joiner.on(",").skipNulls().join(linuxTargets.values()),
                null);


        // hidden for ESX or HyperV
        if (osValues != null) {
            defaultGateway.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            defaultGateway.setDependencyValue(osValues);

        }else{
            // no bare metal OS, don't show it at all
            defaultGateway.setHideFromTemplate(true);
        }

        if (linuxOSValues != null) {
            mtuSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            mtuSetting.setDependencyValue(linuxOSValues);
        } else{
            mtuSetting.setHideFromTemplate(true);
        }

        IIOPoolMgr identityPoolsMgr = IOPoolMgr.getInstance();
        List<IOPool> ioPools = identityPoolsMgr.getAllPools();

        if (ioPools != null && ioPools.size() > 0) {
            ServiceTemplateSetting serverSettingIdentityPool = new ServiceTemplateSetting();
            serverSettingIdentityPool.setDisplayName("Identity Pool");
            serverSettingIdentityPool.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ID_POOL_TOOLTIP);
            serverSettingIdentityPool
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDENTITYPOOL_ID);
            serverSettingIdentityPool
                    .setType(ServiceTemplateSettingType.ENUMERATED);
            for (int i = 0; i < ioPools.size(); i++) {
                serverSettingIdentityPool.getOptions().add(
                        new ServiceTemplateOption(ioPools.get(i).getName(), ioPools.get(i).getId(), null, null));
            }
            // set it to the first pool which is the "Global" pool
            serverSettingIdentityPool.setValue(ioPools.get(0).getId());
            serverCategoryNetwork.getParameters()
                    .add(serverSettingIdentityPool);
        }

        // hardware boot device defines OS type, so it goes first.
        server.getResources().add(serverCategoryOS);
        server.getResources().add(serverCategoryHW);
        server.getResources().add(serverCategoryBIOS);
        server.getResources().add(serverCategoryNetwork);

        return server;
    }

    private ServiceTemplateComponent buildMinimalServerComponentOsOnly(List<RazorRepo> razorImages,
                                                                       List<DeviceGroupEntity> deviceGroups,
                                                                       List<NetworkConfiguration> networks,
                                                                       final Map<String, String> imageTargets,
                                                                       final Map<String, String> repoNames) {
        ServiceTemplateComponent server = new ServiceTemplateComponent();
        server.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_OS);
        server.setName("Server (O/S Installation Only)");
        server.setType(ServiceTemplateComponentType.SERVER);

        ServiceTemplateCategory serverCategoryOS = buildServerOsSettings(razorImages, imageTargets, repoNames, true);
        addServerPoolSetting(serverCategoryOS, deviceGroups);
        addDeploymentAttemptedSetting(serverCategoryOS);

        ServiceTemplateCategory serverCategoryNetwork = new ServiceTemplateCategory();
        serverCategoryNetwork.setDisplayName("Network Settings");
        serverCategoryNetwork.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);

        ServiceTemplateSetting serverSettingHypervisorManagementNetwork = new ServiceTemplateSetting();
        serverSettingHypervisorManagementNetwork
                .setDisplayName("Hypervisor Management Network");
        serverSettingHypervisorManagementNetwork
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID);
        serverSettingHypervisorManagementNetwork
                .setType(ServiceTemplateSettingType.ENUMERATED);
        serverSettingHypervisorManagementNetwork.
                setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
        serverSettingHypervisorManagementNetwork.
                setDependencyValue(imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE));

        ServiceTemplateUtil.addDefaultSelectOption(serverSettingHypervisorManagementNetwork);

        if (networks != null && networks.size() > 0) {
            for (NetworkConfiguration network: networks) {
                if (network.getType().compareToIgnoreCase(NetworkType.HYPERVISOR_MANAGEMENT.value()) == 0) {
                    serverSettingHypervisorManagementNetwork.getOptions().add(new ServiceTemplateOption(network.getName(),
                            network.getId(), ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE)));
                }
            }
        }

        serverCategoryNetwork.getParameters().add(
                serverSettingHypervisorManagementNetwork);

        // strip references to boot device (a hardware setting we removed above) from the o/s settings
        for (ServiceTemplateSetting setting : serverCategoryOS.getParameters()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID.equals(setting.getDependencyTarget())) {
                setting.setDependencyTarget(null);
                setting.setDependencyValue(null);
            }
            List<ServiceTemplateOption> options = setting.getOptions();
            if (options != null) {
                for (ServiceTemplateOption option : options) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID.equals(option.getDependencyTarget())) {
                        option.setDependencyTarget(null);
                        option.setDependencyValue(null);
                    }
                }
            }
        }

        server.getResources().add(serverCategoryOS);
        server.getResources().add(serverCategoryNetwork);

        return server;
    }

    private ServiceTemplateComponent buildMinimalServerComponentHardwareOnly(List<RazorRepo> razorImages,
                                                                             List<DeviceGroupEntity> deviceGroups,
                                                                             List<NetworkConfiguration> networks) {
        ServiceTemplateComponent server = new ServiceTemplateComponent();
        server.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_HW);
        server.setName("Server (Hardware Only)");
        server.setType(ServiceTemplateComponentType.SERVER);

        ServiceTemplateCategory serverCategoryHW = buildServerHardwareSettings(deviceGroups, true);
        addServerPoolSetting(serverCategoryHW, deviceGroups);
        addDeploymentAttemptedSetting(serverCategoryHW);
        ServiceTemplateCategory serverCategoryBIOS = buildServerBIOSSettings();

        ServiceTemplateCategory serverCategoryNetwork = new ServiceTemplateCategory();
        serverCategoryNetwork.setDisplayName("Network Settings");
        serverCategoryNetwork.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);

        ServiceTemplateSetting multiNetworkConfig = new ServiceTemplateSetting();
        multiNetworkConfig.setDisplayName("Network Config");
        multiNetworkConfig.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        multiNetworkConfig.setType(ServiceTemplateSettingType.NETWORKCONFIGURATION);
        multiNetworkConfig.setRequired(false);
        multiNetworkConfig.setValue("");
        serverCategoryNetwork.getParameters().add(multiNetworkConfig);

        IIOPoolMgr identityPoolsMgr = IOPoolMgr.getInstance();
        List<IOPool> ioPools = identityPoolsMgr.getAllPools();

        if (ioPools != null && ioPools.size() > 0) {
            ServiceTemplateSetting serverSettingIdentityPool = new ServiceTemplateSetting();
            serverSettingIdentityPool.setDisplayName("Identity Pool");
            serverSettingIdentityPool.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ID_POOL_TOOLTIP);
            serverSettingIdentityPool
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDENTITYPOOL_ID);
            serverSettingIdentityPool
                    .setType(ServiceTemplateSettingType.ENUMERATED);
            for (int i = 0; i < ioPools.size(); i++) {
                serverSettingIdentityPool.getOptions().add(
                        new ServiceTemplateOption(ioPools.get(i).getName(), ioPools.get(i).getId(), null, null));
            }
            // set it to the first pool which is the "Global" pool
            serverSettingIdentityPool.setValue(ioPools.get(0).getId());
            serverCategoryNetwork.getParameters()
                    .add(serverSettingIdentityPool);
        }
        // We add this empty asm::server resource as a workaround for the fact that asm-deployer has no provider that can handle a server without asm::server
        ServiceTemplateCategory emptyServerCategoryOS = new ServiceTemplateCategory();
        emptyServerCategoryOS.setDisplayName("OS Settings");
        emptyServerCategoryOS.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);

        // We give a null value for host name as well to avoid any issues in the backend.
        ServiceTemplateSetting emptyHostNameSetting = new ServiceTemplateSetting();
        emptyHostNameSetting.setDisplayName("Host Name");
        emptyHostNameSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
        emptyHostNameSetting.setType(ServiceTemplateSettingType.STRING);
        emptyHostNameSetting.setRequired(false);
        emptyHostNameSetting.setRequiredAtDeployment(false);
        emptyHostNameSetting.setHideFromTemplate(true);
        emptyHostNameSetting.setDependencyTarget("");
        emptyHostNameSetting.setDependencyValue("");
        emptyServerCategoryOS.getParameters().add(emptyHostNameSetting);

        // hardware boot device defines OS type, so it goes first.
        server.getResources().add(serverCategoryHW);
        server.getResources().add(serverCategoryBIOS);
        server.getResources().add(emptyServerCategoryOS);
        server.getResources().add(serverCategoryNetwork);

        return server;
    }

    /**
     * Build default template with ALL possible components, updated with most recent data.
     * @return
     */
    public ServiceTemplate getDefaultTemplate() {
        return getDefaultTemplate(null, false);
    }

    /**
     * If compnent type is not empty, return only required components. Otherwise return all.
     * @param componentType
     * @return
     */
    private ServiceTemplate getDefaultTemplate(String componentType, boolean includeBrownfieldVmMangers) {
        ServiceTemplate templateFromFile = getDefaultTemplateFromFile();
        if (templateFromFile != null) {
            return templateFromFile;
        }

        ServiceTemplate svc = new ServiceTemplate();
        svc.setId(DEFAULT_TEMPLATE_ID);
        svc.setCreatedBy(AsmConstants.USERROLE_ADMINISTRATOR);
        svc.setUpdatedBy(AsmConstants.USERROLE_ADMINISTRATOR);

        // data used by various components

        // networks
        List<NetworkConfiguration> networks = NetworkConfigurationMgr
                .getInstance()
                .findAllNetworkConfiguration(null, null, null);

        // server pools
        List<DeviceGroupEntity> deviceGroups = null;
        try {
            deviceGroups = deviceGroupDAO.getAllDeviceGroup(null, null, null);
        } catch (AsmManagerCheckedException e) {
            LOGGER.error("Unable to get device groups", e);
        }

        // razor images
        final List<RazorRepo> razorImages = osRepositoryUtil.getRazorOSImages(false);
        final Map<String, String> repoNames = osRepositoryUtil.mapRazorRepoNamesToAsmRepoNames();
        final Map<String, String> imageTargets = ServiceTemplateUtil.processRazorImagesTargets(razorImages, false);

        // vCenter and SCVMM entities
        List<DeviceInventoryEntity> vCenterEntities = serviceTemplateUtil.getVCenterEntities(includeBrownfieldVmMangers);
        List<DeviceInventoryEntity> hypervEntities = serviceTemplateUtil.getHypervEntities();

        svc.setTemplateName("Default Template");
        svc.setTemplateDescription("Default Template Description");
        svc.setTemplateVersion(ServiceTemplateUtil.getCurrentTemplateVersion());
        svc.setTemplateValid(ServiceTemplateValid.getDefaultInstance());
        svc.setDraft(false);

        if (componentType==null || ServiceTemplateComponentType.STORAGE.getLabel().equalsIgnoreCase(componentType)) {
            List<ServiceTemplateComponent> storageComponents = buildDefaultStorageComponents();
            if (!storageComponents.isEmpty()) {
                svc.getComponents().addAll(storageComponents);
            }
        }

        if (componentType == null || ServiceTemplateComponentType.SERVER.getLabel().equalsIgnoreCase(componentType)) {
            svc.getComponents().add(buildServerComponent(razorImages, deviceGroups, networks, imageTargets, repoNames));
            svc.getComponents().add(buildMinimalServerComponentOsOnly(razorImages, deviceGroups, networks, imageTargets, repoNames));
            svc.getComponents().add(buildMinimalServerComponentHardwareOnly(razorImages, deviceGroups, networks));
        }

        if (componentType==null || ServiceTemplateComponentType.CLUSTER.getLabel().equalsIgnoreCase(componentType)
                || ServiceTemplateComponentType.VIRTUALMACHINE.getLabel().equalsIgnoreCase(componentType)) {

            ServiceTemplateComponent cluster = buildVCenterClusterComponent(vCenterEntities);
            svc.getComponents().add(cluster);

            ServiceTemplateComponent hvCluster = buildSCVMMClusterComponent(hypervEntities);
            svc.getComponents().add(hvCluster);

            // vCenter VM
            svc.getComponents().add(createVMComponent(vCenterEntities, razorImages, networks, imageTargets, repoNames, false));
            // Clone vCenter VM
            svc.getComponents().add(createVMComponent(vCenterEntities, razorImages, networks, imageTargets, repoNames, true));
            // Clone hyperv VM
            svc.getComponents().add(createHypervVM(hypervEntities, hvCluster, networks, true));
        }

        if (componentType==null || ServiceTemplateComponentType.SERVICE.getLabel().equalsIgnoreCase(componentType)) {

            List<ServiceTemplateComponent> services = buildServiceComponents();
            if (!services.isEmpty()) {
                svc.getComponents().addAll(services);
            }
        }

        // Add ensure parameter to all resources except app / services
        for (ServiceTemplateComponent component : svc.getComponents()) {
            if (!ServiceTemplateComponentType.SERVICE.equals(component.getType())) {
                for (ServiceTemplateCategory resource : component.getResources()) {
                    ServiceTemplateSetting ensure = new ServiceTemplateSetting();
                    ensure.setId("ensure");
                    ensure.setType(ServiceTemplateSettingType.STRING);
                    ensure.setRequired(true);
                    ensure.setValue("present");
                    ensure.setHideFromTemplate(true);
                    ensure.setGroup("none");
                    resource.getParameters().add(ensure);
                }
            }
        }

        return svc;

    }

    private void populateLang(ServiceTemplateSetting serverSetting) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MS_LOCALE_FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null)
            {
                String cells[] = line.split("\t");
                serverSetting.getOptions().add(new ServiceTemplateOption(cells[1], cells[0], null, null));
            }
            in.close();

        } catch (IOException e) {
            LOGGER.error(e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
    }

    private void populateKeyboard(ServiceTemplateSetting serverSetting) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MS_LAYOUT_FILENAME);

            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null)
            {
                String cells[] = line.split("\t");
                serverSetting.getOptions().add(new ServiceTemplateOption(cells[1], cells[0], null, null));
            }
            in.close();
        } catch (IOException e) {
            LOGGER.error(e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
    }

    private void populateTimezones(ServiceTemplateSetting serverSettingHVTimezone) {
        List<TimeZoneInfoModel> timezones = TimeZoneConfigurationMgr.getInstance().getMicrosoftTimeZone();
        if (timezones != null && timezones.size()>0)
        {
            for (TimeZoneInfoModel tz : timezones)
            {
                serverSettingHVTimezone.getOptions().add(new ServiceTemplateOption(tz.getTimeZone(),tz.getTimeZoneId(),null,null));
            }
        }
    }

    private void populateLinuxTimezones(ServiceTemplateSetting serverSetting) {
        // we actually need zones from here: TimeZoneConfigurationMgr.getInstance().getTimeZonesList();
        // but those are not user friendly. So display the same list as we do for appliance initial config
        // and replace the value on deployment stage
        List<TimeZoneInfoModel> timezones = TimeZoneConfigurationMgr.getInstance().getAvailableTimeZone();

        if (timezones != null && timezones.size()>0)
        {
            for (TimeZoneInfoModel tz : timezones)
            {
                serverSetting.getOptions().add(new ServiceTemplateOption(tz.getTimeZone(),tz.getTimeZoneId(),null,null));
            }
        }
    }

    private List<ServiceTemplateComponent> buildServiceComponents() {
        List<ServiceTemplateComponent> services = new ArrayList<>();
        try {
            List<AddOnModuleComponentEntity> addOnModuleComponentEntities = addOnModuleComponentsDAO.getAll(false);
            for (AddOnModuleComponentEntity entity : addOnModuleComponentEntities) {
                // unmarshal class data
                final ServiceTemplateComponent component =
                        MarshalUtil.unmarshal(ServiceTemplateComponent.class, entity.getMarshalledData());
                services.add(component);
            }
        } catch (Exception e) {
            LOGGER.error("Exception seen while retrieving add on modules", e);
        }
        return services;
    }

    /**
     * vCenter Cluster
     * @param vCenterEntities
     * @return
     */
    private ServiceTemplateComponent buildVCenterClusterComponent(List<DeviceInventoryEntity> vCenterEntities) {
        ServiceTemplateComponent cluster = new ServiceTemplateComponent();
        cluster.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID);
        cluster.setName("VMWare Cluster");
        cluster.setType(ServiceTemplateComponentType.CLUSTER);

        ServiceTemplateCategory clusterCategory = new ServiceTemplateCategory();
        clusterCategory.setDisplayName("Cluster Settings");
        clusterCategory.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID);

        cluster.getResources().add(clusterCategory);

        ServiceTemplateSetting clusterVCenterSetting = new ServiceTemplateSetting();
        clusterVCenterSetting.setDisplayName("Target Virtual Machine Manager");
        clusterVCenterSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
        clusterVCenterSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        ServiceTemplateUtil.addDefaultSelectOption(clusterVCenterSetting);
        ServiceTemplateUtil.addDeviceInventoryOptions(vCenterEntities,clusterVCenterSetting);
        clusterCategory.getParameters().add(clusterVCenterSetting);

        ServiceTemplateSetting clusterDCSetting = new ServiceTemplateSetting();
        clusterDCSetting.setDisplayName("Data Center Name");
        clusterDCSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID);
        clusterDCSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        clusterDCSetting.setRequiredAtDeployment(true);
        clusterCategory.getParameters().add(clusterDCSetting);

        Map<String, ServiceTemplateOption> datacenters = new HashMap<>();
        Map<String, ServiceTemplateOption> clusters = new HashMap<>();
        Map<String, ServiceTemplateOption> vdss = new HashMap<>();
        Map<String, ServiceTemplateOption> portgroups = new HashMap<>();

        if (vCenterEntities != null) {
            for (DeviceInventoryEntity e : vCenterEntities) {
                ManagedObjectDTO vCenter = null;
                try {
                    Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(e.getRefId());
                    vCenter = VcenterInventoryUtils.convertPuppetDeviceDetailsToDto(deviceDetails);
                } catch (Exception e1) {
                    LOGGER.error("Could not find deviceDetails for " + e.getRefId(), e1);
                    continue;
                }
                List<DatacenterDTO> dcSet = vCenter.getDatacenters();

                for (DatacenterDTO dc : dcSet) {
                    String path = dc.getName();
                    ServiceTemplateOption datacenterTemplateOption = datacenters.get(path);
                    if (datacenterTemplateOption != null) { // Use same Option and add a dependency value to it
                        String dependency = datacenterTemplateOption.getDependencyValue() + "," + e.getRefId();
                        datacenterTemplateOption.setDependencyValue(dependency);
                    } else {  // does not exist yet so create
                        datacenterTemplateOption = new ServiceTemplateOption(path, path,
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, e.getRefId());
                        datacenters.put(path, datacenterTemplateOption);
                    }

                    for (ClusterDTO cdto : dc.getClusters()) {
                        String clusterPath = cdto.getName();
                        ServiceTemplateOption clusterTemplateOption = clusters.get(clusterPath);
                        if (clusterTemplateOption != null) {
                            String dependency = clusterTemplateOption.getDependencyValue() + "," + path;
                            clusterTemplateOption.setDependencyValue(dependency);
                        } else {
                            clusterTemplateOption = new ServiceTemplateOption(clusterPath, clusterPath,
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID, path);
                            clusters.put(clusterPath, clusterTemplateOption);
                        }
                    }

                    for (VDSDTO vds : dc.getVDSs()) {
                        String name = vds.getName();
                        ServiceTemplateOption templateOption = vdss.get(name);
                        if (templateOption != null) {
                            String dependency = templateOption.getDependencyValue() + "," + path;
                            templateOption.setDependencyValue(dependency);
                        } else {
                            templateOption = new ServiceTemplateOption(name, name,
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID, path);
                            vdss.put(name, templateOption);
                        }

                        for (PortGroupDTO pdto : vds.getPortGroups()) {
                            String pname = pdto.getName();
                            ServiceTemplateOption pgTemplateOption = portgroups.get(pname);
                            if (pgTemplateOption != null) {
                                String dependency = pgTemplateOption.getDependencyValue() + "," + name;
                                pgTemplateOption.setDependencyValue(dependency);
                            } else {
                                // dependency target will be set later to VDS Name setting
                                pgTemplateOption = new ServiceTemplateOption(pname, pname, "", name);
                                if (pdto.getAttributes() != null && pdto.getAttribute("vlan_id") != null) {
                                    pgTemplateOption.getAttributes().put("vlan_id", String.valueOf(pdto.getAttribute("vlan_id")));
                                }
                                portgroups.put(pname, pgTemplateOption);
                            }
                        }
                    }
                }
            }
        }

        ServiceTemplateUtil.processNewSetting(cluster, datacenters.values(),
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID, "Create New Datacenter...", "New datacenter name", false);

        ServiceTemplateSetting clusterClusterSetting = new ServiceTemplateSetting();
        clusterClusterSetting.setDisplayName("Cluster Name");
        clusterClusterSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID);
        clusterClusterSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        clusterClusterSetting.setRequiredAtDeployment(true);
        clusterCategory.getParameters().add(clusterClusterSetting);

        ServiceTemplateUtil.processNewSetting(cluster, clusters.values(),
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID, "Create New Cluster...", "New cluster name", false);

        ServiceTemplateSetting clusterVDSSetting = new ServiceTemplateSetting();
        clusterVDSSetting.setDisplayName("Switch Type");
        clusterVDSSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID);
        clusterVDSSetting.setRequired(false);
        clusterVDSSetting.setType(ServiceTemplateSettingType.RADIO);
        clusterVDSSetting.getOptions().add(new ServiceTemplateOption("Standard",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_STD_ID, null, null));
        clusterVDSSetting.getOptions().add(new ServiceTemplateOption("Distributed",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID, null, null));
        clusterVDSSetting.setValue("standard");
        clusterCategory.getParameters().add(clusterVDSSetting);

        ServiceTemplateSetting clusterHASetting = new ServiceTemplateSetting();
        clusterHASetting.setDisplayName("Cluster HA Enabled");
        clusterHASetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_HA_ID);
        clusterHASetting.setRequired(false);
        // here and below the group attribute is commented until UI could handle it correctly
        // right now, if dependency is not satisfied, the group name not showing up
        //clusterHASetting.setGroup(ServiceTemplateSettingIDs.VMWARE_FEATURES_GROUP);
        clusterHASetting.setType(ServiceTemplateSettingType.BOOLEAN);
        clusterHASetting.setValue("false");
        clusterHASetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID);
        clusterHASetting.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX);

        clusterCategory.getParameters().add(clusterHASetting);

        ServiceTemplateSetting clusterDRSSetting = new ServiceTemplateSetting();
        //clusterDRSSetting.setGroup(ServiceTemplateSettingIDs.VMWARE_FEATURES_GROUP);
        clusterDRSSetting.setDisplayName("Cluster DRS Enabled");
        clusterDRSSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DRS_ID);
        clusterDRSSetting.setRequired(false);
        clusterDRSSetting.setType(ServiceTemplateSettingType.BOOLEAN);
        clusterDRSSetting.setValue("false");
        clusterDRSSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID);
        clusterDRSSetting.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX);

        clusterCategory.getParameters().add(clusterDRSSetting);

        ServiceTemplateSetting enableVSAN = new ServiceTemplateSetting();
        //enableVSAN.setGroup(ServiceTemplateSettingIDs.VMWARE_FEATURES_GROUP);
        enableVSAN.setDisplayName("Enable VMWare vSAN");
        enableVSAN.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID);
        enableVSAN.setRequired(false);
        enableVSAN.setType(ServiceTemplateSettingType.BOOLEAN);
        enableVSAN.setValue("false");
        clusterCategory.getParameters().add(enableVSAN);

        ServiceTemplateSetting compressionSet = new ServiceTemplateSetting();
        //compressionSet.setGroup(ServiceTemplateSettingIDs.VMWARE_FEATURES_GROUP);
        compressionSet.setDisplayName("Enable Compression and Deduplication");
        compressionSet.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID);
        compressionSet.setRequired(false);
        compressionSet.setType(ServiceTemplateSettingType.BOOLEAN);
        compressionSet.setValue("false");
        compressionSet.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID);
        compressionSet.setDependencyValue("true");
        clusterCategory.getParameters().add(compressionSet);

        ServiceTemplateSetting failureSetting = new ServiceTemplateSetting();
        failureSetting.setDisplayName("Failure Tolerance Method");
        //failureSetting.setGroup(ServiceTemplateSettingIDs.VMWARE_FEATURES_GROUP);
        failureSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID);
        failureSetting.setRequired(false);
        failureSetting.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_TOOLTIP);
        failureSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        failureSetting.getOptions().add(new ServiceTemplateOption("RAID-1 (Mirroring) – Performance",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID1_ID, null, null));
        failureSetting.getOptions().add(new ServiceTemplateOption("RAID-5/6 (Erasure Coding) – Capacity",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID5_ID, null, null));
        ServiceTemplateUtil.addDefaultSelectOption(failureSetting);
        failureSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VSAN_ID);
        failureSetting.setDependencyValue("true");
        clusterCategory.getParameters().add(failureSetting);

        ServiceTemplateSetting numberFailuresSet = new ServiceTemplateSetting();
        //numberFailuresSet.setGroup(ServiceTemplateSettingIDs.VMWARE_FEATURES_GROUP);
        numberFailuresSet.setDisplayName("Number of Failures to Tolerate");
        numberFailuresSet.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID);
        numberFailuresSet.setRequired(true);
        numberFailuresSet.setType(ServiceTemplateSettingType.ENUMERATED);
        ServiceTemplateUtil.addDefaultSelectOption(numberFailuresSet);
        numberFailuresSet.getOptions().add(new ServiceTemplateOption("0", "0",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID1_ID));
        numberFailuresSet.getOptions().add(new ServiceTemplateOption("1", "1", null, null));
        numberFailuresSet.getOptions().add(new ServiceTemplateOption("2", "2", null, null));
        numberFailuresSet.getOptions().add(new ServiceTemplateOption("3", "3",
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID1_ID));

        numberFailuresSet.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID);
        numberFailuresSet.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID1_ID + "," +
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_RAID5_ID);
        clusterCategory.getParameters().add(numberFailuresSet);

        ServiceTemplateSetting storageDRSSetting = new ServiceTemplateSetting();
        storageDRSSetting.setDisplayName("Storage DRS Enabled");
        storageDRSSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID);
        storageDRSSetting.setRequired(false);
        storageDRSSetting.setType(ServiceTemplateSettingType.BOOLEAN);
        storageDRSSetting.setValue("false");
        clusterCategory.getParameters().add(storageDRSSetting);

        ServiceTemplateSetting storageClusterName = new ServiceTemplateSetting();
        storageClusterName.setDisplayName("Storage Cluster Name");
        storageClusterName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID);
        storageClusterName.setRequired(true);
        storageClusterName.setType(ServiceTemplateSettingType.STRING);
        storageClusterName.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID);
        storageClusterName.setDependencyValue("true");
        storageClusterName.setRequiredAtDeployment(true);
        clusterCategory.getParameters().add(storageClusterName);

        ServiceTemplateSetting clusterDatastores = new ServiceTemplateSetting();
        clusterDatastores.setDisplayName("Datastores to Add to Cluster:");
        clusterDatastores.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID);
        clusterDatastores.setRequired(true);
        clusterDatastores.setType(ServiceTemplateSettingType.LIST);
        clusterDatastores.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID);
        clusterDatastores.setDependencyValue("true");
        clusterDatastores.setGenerated(true);
        clusterCategory.getParameters().add(clusterDatastores);

        ServiceTemplateCategory vdsCategory = new ServiceTemplateCategory();
        vdsCategory.setDisplayName("vSphere VDS Settings");
        vdsCategory.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID);
        cluster.getResources().add(vdsCategory);

        ServiceTemplateSetting vdsSet;

        // below are default VDS name and VDS PG. They are not used by real deployment, only as a holder
        // for options
        String vdsGroup = "VDS";
        vdsSet = ServiceTemplateClientUtil.createVDSNameSetting(cluster, vdsCategory,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID,
                "VDS Name", vdsGroup, vdss.values());
        vdsSet.setRequired(false);
        vdsSet.setHideFromTemplate(true);

        // create one port group for each VDS. It always hidden and used as a source of options
        vdsSet = ServiceTemplateClientUtil.createVDSPGSetting(cluster, vdsCategory,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID,
                "Port Group", vdsGroup, ServiceTemplateClientUtil.copyOptions(portgroups.values(), ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID));
        vdsSet.setHideFromTemplate(true);

        return cluster;
    }

    /**
     * SCVMM Cluster
     * @param hypervEntities
     * @return
     */
    private ServiceTemplateComponent buildSCVMMClusterComponent(List<DeviceInventoryEntity> hypervEntities) {

        ServiceTemplateComponent hvCluster = new ServiceTemplateComponent();
        hvCluster.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID);
        hvCluster.setName("Hyper-V Cluster");
        hvCluster.setType(ServiceTemplateComponentType.CLUSTER);

        ServiceTemplateCategory clusterCategory = new ServiceTemplateCategory();
        clusterCategory.setDisplayName("Cluster Settings");
        clusterCategory.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID);

        hvCluster.getResources().add(clusterCategory);

        ServiceTemplateSetting clusterHypervSetting = new ServiceTemplateSetting();
        clusterHypervSetting.setDisplayName("Target Virtual Machine Manager");
        clusterHypervSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
        clusterHypervSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        serviceTemplateUtil.addDefaultSelectOption(clusterHypervSetting);
        ServiceTemplateUtil.addDeviceInventoryOptions(hypervEntities,clusterHypervSetting);
        clusterCategory.getParameters().add(clusterHypervSetting);


        ServiceTemplateSetting hvclusterDCSetting = new ServiceTemplateSetting();
        hvclusterDCSetting.setDisplayName("Host Group");
        hvclusterDCSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_HOSTGROUP_ID);
        hvclusterDCSetting.setType(ServiceTemplateSettingType.ENUMERATED);
        hvclusterDCSetting.setRequired(true);
        hvclusterDCSetting.setRequiredAtDeployment(true);
        clusterCategory.getParameters().add(hvclusterDCSetting);


        Map<String, ServiceTemplateOption> hostGroupsMap = new HashMap<>();
        Map<String, ServiceTemplateOption> hvClustersMap = new HashMap<>();

        for (DeviceInventoryEntity e: hypervEntities) {
            try {
                Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(e.getRefId());

                Map<String, Object> scvmmMap = OBJECT_MAPPER.readValue(deviceDetails.get("value"), HashMap.class);
                List<Map<String,String>> hostGroups = (ArrayList<Map<String,String>>) scvmmMap.get("hostgroup");

                if (hostGroups!= null) {
                    for (Map<String, String> hg: hostGroups) {
                        String path = hg.get("Path");
                        if (path != null) {
                            ServiceTemplateOption hostGroupTemplateOption = hostGroupsMap.get(path);
                            if(hostGroupTemplateOption != null) {
                                String dependency = hostGroupTemplateOption.getDependencyValue() + "," + e.getRefId();
                                hostGroupTemplateOption.setDependencyValue(dependency);
                            }
                            else {
                                hostGroupTemplateOption = new ServiceTemplateOption(path, path, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID, e.getRefId());
                                hostGroupsMap.put(path, hostGroupTemplateOption);
                            }
                        }
                        List<Map<String,String>> hosts = (ArrayList<Map<String,String>>) scvmmMap.get("host");
                        if (hosts!= null) {
                            for (Map<String, String> hs: hosts) {
                                String checkerPath = hs.get("$_.VMHostGroup.Path");
                                String clusterPath = hs.get("$_.HostCluster.ClusterName");
                                if (clusterPath != null && checkerPath != null) {
                                    if(path.equals(checkerPath)) {
                                        ServiceTemplateOption clusterTemplateOption = hvClustersMap.get(clusterPath);
                                        if(clusterTemplateOption != null) {
                                            String dependency = clusterTemplateOption.getDependencyValue() + "," + path;
                                            clusterTemplateOption.setDependencyValue(dependency);
                                        }
                                        else {
                                            clusterTemplateOption = new ServiceTemplateOption(clusterPath, clusterPath, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_HOSTGROUP_ID, path);
                                            hvClustersMap.put(clusterPath, clusterTemplateOption);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception pe) {
                LOGGER.error("Error fetching scvmm data for " + e.getRefId(), pe);
            }
        }

        ServiceTemplateUtil.processNewSetting(hvCluster, hostGroupsMap.values(),
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_HOSTGROUP_ID, "New Host Group...", "New host group name", false);

        ServiceTemplateSetting clusterClusterSetting = new ServiceTemplateSetting();
        clusterClusterSetting.setDisplayName("Cluster Name");
        clusterClusterSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID);
        clusterClusterSetting.setType(ServiceTemplateSettingType.STRING);
        clusterClusterSetting.setRequired(true);
        clusterClusterSetting.setRequiredAtDeployment(true);
        clusterCategory.getParameters().add(clusterClusterSetting);

        ServiceTemplateSetting newCluster = ServiceTemplateUtil.processNewSetting(hvCluster, hvClustersMap.values(),
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID, "New Cluster...", "New cluster name", false);
        if (newCluster!=null)
            newCluster.setMaxLength(15);
        else
            LOGGER.warn(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID + " parameter not found in default template!");

        ServiceTemplateSetting clusterIPSetting = new ServiceTemplateSetting();
        clusterIPSetting.setDisplayName("Cluster IP Address");
        clusterIPSetting
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_IPADDRESS_ID);
        clusterIPSetting.setType(ServiceTemplateSettingType.STRING);
        clusterIPSetting.setRequired(false);
        clusterCategory.getParameters().add(clusterIPSetting);

        return hvCluster;
    }

    /**
     * Support for storages: netapp, eql, compellent, etc
     * @return
     */
    protected List<ServiceTemplateComponent> buildDefaultStorageComponents() {
        List<ServiceTemplateComponent> ret = new ArrayList<>();

        Map<String, List<DeviceInventoryEntity>> storageDeviceMap = serviceTemplateUtil.getStorageDevicesMap();

        ServiceTemplateSetting drsEnabled = getAddToSDRS();

        ServiceTemplateComponent compellent = getComponentFromConfigFile("compellent.json");
        List<ServiceTemplateOption> compVolumes = new ArrayList<>();

        if (compellent != null) {
            List<DeviceInventoryEntity> compellentEntities = storageDeviceMap.get(DeviceType.compellent.name());
            ServiceTemplateCategory resource = compellent.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID);
            resource.getParameters().add(drsEnabled);

            ServiceTemplateSetting compellentDeviceDropdown = new ServiceTemplateSetting();
            compellentDeviceDropdown.setDisplayName("Target Compellent");
            compellentDeviceDropdown.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_TOOLTIP);
            compellentDeviceDropdown.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
            compellentDeviceDropdown.setType(ServiceTemplateSettingType.ENUMERATED);
            ServiceTemplateUtil.addDefaultSelectOption(compellentDeviceDropdown);

            if (compellentEntities != null && compellentEntities.size() > 0) {
                for (DeviceInventoryEntity compellentEntity : compellentEntities) {
                    compellentDeviceDropdown.getOptions().add(new ServiceTemplateOption(compellentEntity.getServiceTag(),
                            compellentEntity.getRefId(), null, null));
                    compVolumes.addAll(serviceTemplateUtil.getCompellentVolumes(compellentEntity.getRefId()));
                }
            }
            resource.getParameters().add(0, compellentDeviceDropdown);

            ServiceTemplateUtil.processStorageVolumeName(resource, compVolumes);

            List <String> exceptions = new ArrayList<>();
            exceptions.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID);
            serviceTemplateUtil.setDependencyOnCreateNew(compellent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, exceptions);
            serviceTemplateUtil.setOtherDependencies(compellent);


            ret.add(compellent);
        }

        List<DeviceInventoryEntity> eqlEntities = null;
        ServiceTemplateComponent equallogic = getComponentFromConfigFile("equallogic.json");
        List<ServiceTemplateOption> eqlVolumes = new ArrayList<>();

        if (equallogic != null) {
            eqlEntities = storageDeviceMap.get(DeviceType.equallogic.name());
            ServiceTemplateCategory resource =
                    equallogic.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);
            resource.getParameters().add(drsEnabled);

            ServiceTemplateSetting eqlDeviceDropdown = new ServiceTemplateSetting();
            eqlDeviceDropdown.setDisplayName("Target EqualLogic");
            eqlDeviceDropdown.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_TOOLTIP);
            eqlDeviceDropdown
            .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
            eqlDeviceDropdown.setType(ServiceTemplateSettingType.ENUMERATED);
            ServiceTemplateUtil.addDefaultSelectOption(eqlDeviceDropdown);
            if (eqlEntities != null && eqlEntities.size() > 0) {
                for (DeviceInventoryEntity equalogicEntity : eqlEntities) {
                    eqlDeviceDropdown.getOptions().add(new ServiceTemplateOption(equalogicEntity.getServiceTag(),
                            equalogicEntity.getRefId(), null, null));
                    eqlVolumes.addAll(serviceTemplateUtil.getEqlVolumes(equalogicEntity.getRefId()));
                }
            }
            resource.getParameters().add(0, eqlDeviceDropdown);
            ServiceTemplateUtil.processStorageVolumeName(resource, eqlVolumes);

            List<String> exceptions = new ArrayList<>();
            exceptions.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_AUTH_TYPE_ID);
            serviceTemplateUtil.setDependencyOnCreateNew(equallogic, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, exceptions);

            ret.add(equallogic);
        }

        List<DeviceInventoryEntity> vnxEntities = null;
        ServiceTemplateComponent vnx = getComponentFromConfigFile("vnx.json");
        List<ServiceTemplateOption> vnxVolumes = new ArrayList<>();
        List<ServiceTemplateOption> vnxPools = new ArrayList<>();

        if (vnx != null) {
            vnxEntities = storageDeviceMap.get(DeviceType.emcvnx.name());
            ServiceTemplateCategory resource =
                vnx.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID);
            resource.getParameters().add(drsEnabled);

            ServiceTemplateSetting vnxDeviceDropdown = new ServiceTemplateSetting();
            vnxDeviceDropdown.setDisplayName("Target VNX");
            vnxDeviceDropdown.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_TOOLTIP);
            vnxDeviceDropdown
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
            vnxDeviceDropdown.setType(ServiceTemplateSettingType.ENUMERATED);

            ServiceTemplateSetting typeSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_TYPE_ID);
            if (typeSetting == null || typeSetting.getOptions() == null) {
                LOGGER.error("Incorrect EMC VNX template - setting not found or has empty options: " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_TYPE_ID);
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.internalError());
            }

            Map<String, ServiceTemplateOption> typesOptMap = new HashMap<>();
            for (ServiceTemplateOption option : typeSetting.getOptions()) {
                option.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                option.setDependencyValue(null);
                typesOptMap.put(option.getValue(), option);
            }

            ServiceTemplateUtil.addDefaultSelectOption(vnxDeviceDropdown);
            if (vnxEntities != null) {
                for (DeviceInventoryEntity entity : vnxEntities) {
                    vnxDeviceDropdown.getOptions().add(new ServiceTemplateOption(entity.getServiceTag(),
                            entity.getRefId(), null, null));

                    serviceTemplateUtil.getVNXDetails(entity, vnxPools, vnxVolumes, typesOptMap);
                }
            }

            // remove options that never used
            Iterator <ServiceTemplateOption> itOptions = typeSetting.getOptions().iterator();
            while (itOptions.hasNext()) {
                ServiceTemplateOption option = itOptions.next();
                if (option.getDependencyValue() == null)
                    itOptions.remove();
            }
            
            resource.getParameters().add(0, vnxDeviceDropdown);

            ServiceTemplateUtil.processStorageVolumeName(resource, vnxVolumes);

            ServiceTemplateSetting poolSet = vnx.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_POOL_ID);
            if (poolSet != null) {
                poolSet.getOptions().addAll(vnxPools);
            } else {
                LOGGER.error("Invalid VNX JSON resource - no pool setting");
            }
            
            List <String> exceptions = new ArrayList<>();
            exceptions.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_POOL_ID);
            exceptions.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CONFIGURE_SAN);
            serviceTemplateUtil.setDependencyOnCreateNew(vnx, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, exceptions);

            ret.add(vnx);
        }

        List<DeviceInventoryEntity> netappEntities = null;
        ServiceTemplateComponent netapp = getComponentFromConfigFile("netapp.json");
        List<ServiceTemplateOption> netappVolumes = new ArrayList<>();

        if (netapp != null) {
            ServiceTemplateCategory resource =
                netapp.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID);
            resource.getParameters().add(drsEnabled);

            ServiceTemplateSetting nfsSet = null;
            ServiceTemplateSetting aggrSet = null;

            for (ServiceTemplateCategory cat: netapp.getResources()) {
                for (ServiceTemplateSetting set: cat.getParameters()) {
                    if (set.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NFS_NETWORK_ID)) {
                        nfsSet = set;
                    }else if (set.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_AGGR_ID)) {
                        aggrSet = set;
                    }

                }
                if (nfsSet!=null && aggrSet!=null) break;
            }

            if (nfsSet==null) {
                LOGGER.error("No nfs_network settings found in netapp template!");
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.internalError());

            }else{
                nfsSet.setType(ServiceTemplateSettingType.ENUMERATED);
            }

            ServiceTemplateUtil.addDefaultSelectOption(nfsSet);

            if (aggrSet==null) {
                LOGGER.error("No aggr settings found in netapp template!");
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.internalError());
            }else{
                aggrSet.setType(ServiceTemplateSettingType.ENUMERATED);
            }

            ServiceTemplateUtil.addDefaultSelectOption(aggrSet);

            netappEntities = storageDeviceMap.get(DeviceType.netapp.name());

            ServiceTemplateSetting deviceDropdown = new ServiceTemplateSetting();
            deviceDropdown.setDisplayName("Target NetApp");
            deviceDropdown.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_TOOLTIP);
            deviceDropdown
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
            deviceDropdown.setType(ServiceTemplateSettingType.ENUMERATED);

            ServiceTemplateUtil.addDefaultSelectOption(deviceDropdown);
            if (netappEntities != null && netappEntities.size() > 0) {
                for (DeviceInventoryEntity netappEntity : netappEntities) {
                    serviceTemplateUtil.processNetappData(netappEntity, deviceDropdown, nfsSet, aggrSet, netappVolumes);
                }
            }
            resource.getParameters().add(0, deviceDropdown);

            ServiceTemplateUtil.processStorageVolumeName(resource, netappVolumes);

            serviceTemplateUtil.setDependencyOnCreateNew(netapp, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, null);
            ret.add(netapp);
        }

        return ret;
    }


    private ServiceTemplateSetting getAddToSDRS() {
        ServiceTemplateSetting addToSDRS = new ServiceTemplateSetting();
        addToSDRS.setDisplayName("Add to VMware Storage DRS Cluster");
        addToSDRS.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ADD_TO_SDRS_ID);
        addToSDRS.setRequired(false);
        addToSDRS.setType(ServiceTemplateSettingType.ENUMERATED);
        addToSDRS.setValue("");
        addToSDRS.getOptions().add(new ServiceTemplateOption("Select Pod...", "", null, null));
        addToSDRS.setGenerated(true);
        addToSDRS.setHideFromTemplate(true);
        return addToSDRS;
    }

    private void logLocalizableMessage(EEMILocalizableMessage msg) {
        if (msg != null) {
            localizableMessageService.logMsg(msg.getDisplayMessage(), getLogSeverity(msg.getSeverity()),
                    LogCategory.TEMPLATE_CONFIGURATION, msg.getCorrelationId(), msg.getAgentId());
        }
    }

    private LogSeverity getLogSeverity(EEMISeverity eemiSeverity) {
        LogSeverity severity = LogSeverity.INFO;
        try {
            severity = LogSeverity.valueOf(eemiSeverity.name());
        } catch (Exception e) {
            // do nothing
        }
        return severity;
    }

    /**
     * Create VM default component.
     * @return
     */
    private ServiceTemplateComponent createVMComponent(List<DeviceInventoryEntity> vCenterEntities,
                                                       List<RazorRepo> razorImages,
                                                       List<NetworkConfiguration> networks,
                                                       final Map<String, String> imageTargets,
                                                       final Map<String, String> repoNames,
                                                       boolean isClone) {

        // create default Virtual Machine settings
        // create default cluster settings
        ServiceTemplateComponent VM = new ServiceTemplateComponent();
        if (isClone) {
            VM.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_VC_CLONE_COMPONENT_ID);
            VM.setName("Clone vCenter Virtual Machine");
        }
        else {
            VM.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_VC_COMPONENT_ID);
            VM.setName("vCenter Virtual Machine");
        }

        VM.setType(ServiceTemplateComponentType.VIRTUALMACHINE);

        ServiceTemplateCategory vmCategory1 = new ServiceTemplateCategory();
        vmCategory1.setDisplayName("Virtual Machine Settings");
        vmCategory1.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE);

        if (isClone) {
            ServiceTemplateSetting serverSettingGenerateHostName = new ServiceTemplateSetting();
            serverSettingGenerateHostName.setDisplayName("Auto-generate Name");
            serverSettingGenerateHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID);
            serverSettingGenerateHostName.setType(ServiceTemplateSettingType.BOOLEAN);
            serverSettingGenerateHostName.setRequired(false);
            serverSettingGenerateHostName.setRequiredAtDeployment(false);
            serverSettingGenerateHostName.setHideFromTemplate(false);
            serverSettingGenerateHostName.setValue("false");
            serverSettingGenerateHostName.setToolTip("Use the VM Name Template field to auto-generate " +
                    "VM names at deployment time. If not selected, a unique name will be requested " +
                    "when the template is deployed.");
            vmCategory1.getParameters().add(serverSettingGenerateHostName);

            ServiceTemplateSetting serverSettingHostNameTemplate = new ServiceTemplateSetting();
            serverSettingHostNameTemplate.setDisplayName("VM Name Template");
            serverSettingHostNameTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_TEMPLATE_ID);
            serverSettingHostNameTemplate.setType(ServiceTemplateSettingType.STRING);
            serverSettingHostNameTemplate.setRequired(true);
            serverSettingHostNameTemplate.setRequiredAtDeployment(false);
            serverSettingHostNameTemplate.setHideFromTemplate(false);
            serverSettingHostNameTemplate.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_DEFAULT_TEMPLATE);
            serverSettingHostNameTemplate.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID);
            serverSettingHostNameTemplate.setDependencyValue("true");
            serverSettingHostNameTemplate.setToolTip("Template used to generate VM names at deployment " +
                    "time. Must contain variables that will produce a unique VM name. Allowed variables " +
                    " are ${num} (an auto-generated unique number).");
            vmCategory1.getParameters().add(serverSettingHostNameTemplate);
            
            // Add the VM_NAME; for non-clone the SERVER_NAME parameter will be used
            ServiceTemplateSetting vmSettingName = new ServiceTemplateSetting();
            vmSettingName.setDisplayName("Name");
            vmSettingName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
            vmSettingName.setType(ServiceTemplateSettingType.STRING);
            vmSettingName.setRequired(true);
            vmSettingName.setRequiredAtDeployment(true);
            vmSettingName.setHideFromTemplate(true);
            vmSettingName.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID);
            vmSettingName.setDependencyValue("false");
            vmCategory1.getParameters().add(vmSettingName);
            

            /*
                Clone Type: [Virtual Machine][Virtual Machine Template]
                Templates: [pick from list of virtual machine templates]
                or
                Virtual Machine: [pick from list of virtual machines]
             */

            ServiceTemplateSetting vmCloneType = new ServiceTemplateSetting();
            vmCloneType.setDisplayName("Clone Type");
            vmCloneType.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CLONE_TYPE);
            vmCloneType.setType(ServiceTemplateSettingType.ENUMERATED);
            vmCloneType.getOptions().add(new ServiceTemplateOption("Virtual Machine", "vm", null, null));
            vmCloneType.getOptions().add(new ServiceTemplateOption("Virtual Machine Template", "template", null, null));
            vmCloneType.setValue("vm");
            vmCloneType.setRequired(false);
            vmCategory1.getParameters().add(vmCloneType);

            ServiceTemplateSetting vmTemplate = new ServiceTemplateSetting();
            vmTemplate.setDisplayName("Source");
            vmTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE);
            vmTemplate.setType(ServiceTemplateSettingType.ENUMERATED);
            vmTemplate.setRequired(true);
            vmCategory1.getParameters().add(vmTemplate);
            ServiceTemplateUtil.addDefaultSelectOption(vmTemplate);
            

            ServiceTemplateSetting vmTemplateDC = new ServiceTemplateSetting();
            vmTemplateDC.setDisplayName("Source Datacenter");
            vmTemplateDC.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE_DC);
            vmTemplateDC.setType(ServiceTemplateSettingType.ENUMERATED);
            vmTemplateDC.setRequired(true);
            vmCategory1.getParameters().add(vmTemplateDC);
            ServiceTemplateUtil.addDefaultSelectOption(vmTemplateDC);

            ServiceTemplateSetting vmCustomSpec = new ServiceTemplateSetting();
            vmCustomSpec.setDisplayName("VM Guest Customization Spec");
            vmCustomSpec.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CUSTOM_SPEC);
            vmCustomSpec.setType(ServiceTemplateSettingType.ENUMERATED);
            vmCustomSpec.setRequired(false);
            vmCustomSpec.setToolTip("Selected VM guest customization spec must match the OS type");
            ServiceTemplateUtil.addDefaultSelectOption(vmCustomSpec);
            vmCategory1.getParameters().add(vmCustomSpec);

            if (vCenterEntities!=null) {
                for (DeviceInventoryEntity e: vCenterEntities) {
                    ManagedObjectDTO vCenter = null;
                    Map<String, String> deviceDetails = null;
                    try {
                        deviceDetails = PuppetModuleUtil.getPuppetDevice(e.getRefId());
                        vCenter = VcenterInventoryUtils.convertPuppetDeviceDetailsToDto(deviceDetails);
                    } catch (Exception e1) {
                        LOGGER.error("Could not find deviceDetails for " + e.getRefId(), e1);
                        continue;
                    }
                    List<VirtualMachineDTO> vmSet = vCenter.getVirtualMachines();
                    List<VMTemplateDTO> templateSet = vCenter.getAllDescendantsByType(VMTemplateDTO.class);
                    List<DatacenterDTO> dcSet = vCenter.getDatacenters();
                    String customSpecsJsonArray = deviceDetails.get("customization_specs");
                    if(StringUtils.isNotEmpty(customSpecsJsonArray)){
                        ArrayList<String> customSpecList;
                        try {
                            customSpecList = new ObjectMapper().readValue(deviceDetails.get("customization_specs"), ArrayList.class);
                            for (String customSpecName : customSpecList){
                                String path = customSpecName;
                                ServiceTemplateOption oPath = new ServiceTemplateOption(path, path, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CUSTOM_SPEC, null);
                                if (!vmCustomSpec.getOptions().contains(oPath)) {
                                    vmCustomSpec.getOptions().add(oPath);
                                }
                            }
                        } catch (Exception e1) {
                            LOGGER.error("Could not find deviceDetails for customization spec list", e1);
                            continue;
                        }
                    }

                    for (VirtualMachineDTO vm: vmSet) {
                        String path = vm.getName();
                        ServiceTemplateOption oPath = new ServiceTemplateOption(path, path, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CLONE_TYPE, "vm");
                        if (!vmTemplate.getOptions().contains(oPath)) {
                            vmTemplate.getOptions().add(oPath);
                        }
                    }

                    for (VMTemplateDTO vm: templateSet) {
                        String path = vm.getName();
                        ServiceTemplateOption oPath = new ServiceTemplateOption(path, path, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CLONE_TYPE, "template");
                        if (!vmTemplate.getOptions().contains(oPath)) {
                            vmTemplate.getOptions().add(oPath);
                        }
                    }

                    for (DatacenterDTO dc: dcSet) {
                        String path = dc.getName();
                        ServiceTemplateOption oPath = new ServiceTemplateOption(path, path, null, null);
                        if (!vmTemplateDC.getOptions().contains(oPath)) {
                            vmTemplateDC.getOptions().add(oPath);
                        }
                    }
                }
            }
        }

        ServiceTemplateSetting vmSettingCPU = new ServiceTemplateSetting();
        vmSettingCPU.setDisplayName("Number of CPUs");
        vmSettingCPU.setValue("1");
        vmSettingCPU.setMin(1);
        vmSettingCPU.setMax(8);
        vmSettingCPU
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NUMBER_OF_CPU_ID);
        vmSettingCPU.setType(ServiceTemplateSettingType.INTEGER);
        vmCategory1.getParameters().add(vmSettingCPU);

        if (!isClone) {
            ServiceTemplateSetting vmDiskSetting = new ServiceTemplateSetting();
            vmDiskSetting.setDisplayName("Virtual Disk(s)");
            vmDiskSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_DISK);
            vmDiskSetting.setRequiredAtDeployment(false);
            vmDiskSetting.setMin(1);
            vmDiskSetting.setMax(2048);
            vmDiskSetting.setRequired(true);
            vmDiskSetting.setType(ServiceTemplateSettingType.VMVIRTUALDISKCONFIGURATION);
            vmCategory1.getParameters().add(vmDiskSetting);
        } else {
            ServiceTemplateSetting vmSettingSize = new ServiceTemplateSetting();
            vmSettingSize.setDisplayName("Virtual Disk Size (GB)");
            vmSettingSize.setValue("32");
            vmSettingSize.setMin(1);
            vmSettingSize.setMax(2048);
            vmSettingSize
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_SIZE);
            vmSettingSize.setType(ServiceTemplateSettingType.INTEGER);
            vmCategory1.getParameters().add(vmSettingSize);
        }

        ServiceTemplateSetting vmSettingMemory = new ServiceTemplateSetting();
        vmSettingMemory.setDisplayName("Memory in MB");
        vmSettingMemory.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_MEMORY_SIZE_TOOLTIP);
        vmSettingMemory.setValue("8192");
        vmSettingMemory.setMin(1024);
        vmSettingMemory.setMax(262144);   //256*1024
        vmSettingMemory.setStep(4);
        vmSettingMemory
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_MEMORY_ID);
        vmSettingMemory.setType(ServiceTemplateSettingType.INTEGER);
        vmCategory1.getParameters().add(vmSettingMemory);

        // Set the VM network selection, filter by public and private
        ServiceTemplateSetting vmSettingNetwork = new ServiceTemplateSetting();
        vmSettingNetwork.setDisplayName("Networks");
        vmSettingNetwork.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORKS_TOOLTIP);
        vmSettingNetwork.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID);
        vmSettingNetwork.setType(ServiceTemplateSettingType.LIST);
        vmSettingNetwork.setRequired(true);
        vmSettingNetwork.setValue("");
        if (networks != null && networks.size() > 0) {
            for (int i = 0; i < networks.size(); i++) {
                if (networks
                        .get(i)
                        .getType()
                        .compareToIgnoreCase(
                                NetworkType.PRIVATE_LAN.value()) == 0
                        || networks
                        .get(i)
                        .getType()
                        .compareToIgnoreCase(
                                NetworkType.PUBLIC_LAN.value()) == 0) {
                    vmSettingNetwork.getOptions().add(new ServiceTemplateOption(networks.get(i).getName(),
                            networks.get(i).getId(), null, null));
                }
            }
        }
        vmCategory1.getParameters().add(vmSettingNetwork);
        
        setDefaultGatewaySetting(networks, vmCategory1);

        if (!isClone)
        {
            ServiceTemplateCategory vmCategory2 = new ServiceTemplateCategory();
            vmCategory2.setDisplayName("Virtual Machine OS Settings");
            vmCategory2.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);

            ServiceTemplateSetting serverSettingGenerateHostName = new ServiceTemplateSetting();
            serverSettingGenerateHostName.setDisplayName("Auto-generate Host Name");
            serverSettingGenerateHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
            serverSettingGenerateHostName.setType(ServiceTemplateSettingType.BOOLEAN);
            serverSettingGenerateHostName.setRequired(false);
            serverSettingGenerateHostName.setRequiredAtDeployment(false);
            serverSettingGenerateHostName.setHideFromTemplate(false);
            serverSettingGenerateHostName.setValue("false");
            serverSettingGenerateHostName.setToolTip("Use the Host Name Template field to auto-generate " +
                    "host names at deployment time. If not selected, a unique host name will be requested " +
                    "when the template is deployed.");
            vmCategory2.getParameters().add(serverSettingGenerateHostName);

            ServiceTemplateSetting serverSettingHostNameTemplate = new ServiceTemplateSetting();
            serverSettingHostNameTemplate.setDisplayName("Host Name Template");
            serverSettingHostNameTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID);
            serverSettingHostNameTemplate.setType(ServiceTemplateSettingType.STRING);
            serverSettingHostNameTemplate.setRequired(true);
            serverSettingHostNameTemplate.setRequiredAtDeployment(false);
            serverSettingHostNameTemplate.setHideFromTemplate(false);
            serverSettingHostNameTemplate.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_HOSTNAME_DEFAULT_TEMPLATE);
            serverSettingHostNameTemplate.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
            serverSettingHostNameTemplate.setDependencyValue("true");
            serverSettingHostNameTemplate.setToolTip("Template used to generate host names at deployment " +
                    "time. Must contain variables that will produce a unique host name. Allowed variables " +
                    " are ${num} (an auto-generated unique number).");
            vmCategory2.getParameters().add(serverSettingHostNameTemplate);

            ServiceTemplateSetting vmSettingHostName = new ServiceTemplateSetting();
            vmSettingHostName.setDisplayName("Host Name");
            vmSettingHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
            vmSettingHostName.setType(ServiceTemplateSettingType.STRING);
            vmSettingHostName.setRequired(true);
            vmSettingHostName.setRequiredAtDeployment(true);
            vmSettingHostName.setHideFromTemplate(true);
            vmSettingHostName.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID);
            vmSettingHostName.setDependencyValue("false");
            vmCategory2.getParameters().add(vmSettingHostName);

            ServiceTemplateSetting vmOSImage = new ServiceTemplateSetting();
            vmOSImage.setDisplayName("OS Image");
            vmOSImage.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_OS_IMAGE_TOOLTIP);
            vmOSImage
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_IMAGE_ID);
            vmOSImage.setType(ServiceTemplateSettingType.ENUMERATED);
            ServiceTemplateUtil.addDefaultSelectOption(vmOSImage);
            ServiceTemplateUtil.addOSImagesAsOptions(razorImages, repoNames, vmOSImage);
            vmCategory2.getParameters().add(vmOSImage);

            String windowsTargets = StringUtils.defaultIfBlank(
                    Joiner.on(",").skipNulls().join(
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE),
                        imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE)),
                    NO_MATCHING_VALUE);

            String allLinuxTargets = StringUtils.defaultIfBlank(
                    Joiner.on(",").skipNulls().join(
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE),
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE),
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE11_VALUE),
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE12_VALUE)),
                    NO_MATCHING_VALUE);
            
            
            String ntpTargets = StringUtils.defaultIfBlank(
                    Joiner.on(",").skipNulls().join(
                            allLinuxTargets,
                            windowsTargets,
                            imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE )),
                    NO_MATCHING_VALUE);

            ServiceTemplateSetting osSubType = new ServiceTemplateSetting();
            osSubType.setDisplayName("OS Image Version");
            osSubType.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_VERSION_ID);
            osSubType.setType(ServiceTemplateSettingType.ENUMERATED);
            osSubType.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            osSubType.setDependencyValue(windowsTargets);

            ServiceTemplateUtil.addDefaultSelectOption(osSubType);
            addWindowsFlavors(osSubType, imageTargets);
            vmCategory2.getParameters().add(osSubType);

            ServiceTemplateSetting osType = new ServiceTemplateSetting();
            osType.setDisplayName("OS Image Type");
            osType.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
            osType.setType(ServiceTemplateSettingType.ENUMERATED);
            osType.setRequired(false);
            osType.setHideFromTemplate(true);

            vmCategory2.getParameters().add(osType);

            
            ServiceTemplateSetting vmSettingHypervisorAdminPassword = new ServiceTemplateSetting();
            vmSettingHypervisorAdminPassword.setDisplayName("Administrator password");
            vmSettingHypervisorAdminPassword.setToolTip(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ADM_PASSWORD_TOOLTIP);
            vmSettingHypervisorAdminPassword
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
            vmSettingHypervisorAdminPassword.setType(ServiceTemplateSettingType.PASSWORD);
            vmSettingHypervisorAdminPassword.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);
            vmCategory2.getParameters().add(vmSettingHypervisorAdminPassword);

            ServiceTemplateSetting vmSettingHypervisorAdminConfirmPassword = new ServiceTemplateSetting();
            vmSettingHypervisorAdminConfirmPassword.setDisplayName("Confirm administrator password");
            vmSettingHypervisorAdminConfirmPassword
                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID);
            vmSettingHypervisorAdminConfirmPassword.setType(ServiceTemplateSettingType.PASSWORD);
            vmSettingHypervisorAdminConfirmPassword.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);
            vmCategory2.getParameters().add(vmSettingHypervisorAdminConfirmPassword);


            ServiceTemplateSetting serverSettingHVProdKey = new ServiceTemplateSetting();
            serverSettingHVProdKey.setDisplayName("Product Key");
            serverSettingHVProdKey.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_PROD_KEY_ID);
            serverSettingHVProdKey.setType(ServiceTemplateSettingType.STRING);
            serverSettingHVProdKey.setRequired(true);
            serverSettingHVProdKey.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingHVProdKey.setDependencyValue(windowsTargets);
            vmCategory2.getParameters().add(serverSettingHVProdKey);

            ServiceTemplateSetting serverSettingNTP = new ServiceTemplateSetting();
            serverSettingNTP.setDisplayName("NTP Server");
            serverSettingNTP.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID);
            serverSettingNTP.setType(ServiceTemplateSettingType.STRING);
            serverSettingNTP.setRequired(false);
            serverSettingNTP.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            serverSettingNTP.setDependencyValue(ntpTargets);

            NTPSetting ntpSet = ProxyUtil.getAlcmNTPProxy().getNTPSettings();
            if (StringUtils.isNotEmpty(ntpSet.getPreferredNTPServer())) {
                serverSettingNTP.setValue(ntpSet.getPreferredNTPServer());
            }

            vmCategory2.getParameters().add(serverSettingNTP);


            //TODO: Perhaps add this back in when there is support for it on the back end.  Removing for now because this currently does not work. 
//            ServiceTemplateSetting customScript = new ServiceTemplateSetting();
//            customScript.setDisplayName("Custom OS Installation script");
//            customScript
//                    .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_CUSTOM_SCRIPT_ID);
//            customScript.setType(ServiceTemplateSettingType.TEXT);
//            customScript.setRequired(false);
//            vmCategory2.getParameters().add(customScript);

            VM.getResources().add(vmCategory2);
        }

        VM.getResources().add(vmCategory1);

        return VM;
    }

	private ServiceTemplateSetting setDefaultGatewaySetting(List<NetworkConfiguration> networks,
			ServiceTemplateCategory vmCategory1) {
		ServiceTemplateSetting defaultGateway = new ServiceTemplateSetting();
        defaultGateway.setDisplayName("Static Network Default Gateway");
        defaultGateway.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_DEFAULT_GATEWAY_ID);
        defaultGateway.setType(ServiceTemplateSettingType.STRING);
        defaultGateway.setRequired(true);
        ServiceTemplateUtil.addDefaultSelectOption(defaultGateway);
        defaultGateway.getOptions().add(new ServiceTemplateOption(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_DHCP_NETWORK_NAME, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_DHCP_NETWORK_ID, null, null));
        defaultGateway.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT_ID);
        if (networks != null && networks.size() > 0) {
            for (NetworkConfiguration netconfig: networks) {
                if (netconfig.isStatic() && (netconfig.getType().compareToIgnoreCase(
                        NetworkType.PRIVATE_LAN.value()) == 0
                        || netconfig.getType().compareToIgnoreCase(
                        NetworkType.PUBLIC_LAN.value()) == 0)) {
                    defaultGateway.getOptions().add(new ServiceTemplateOption(netconfig.getName(), netconfig.getId(), null, null));
                }
            }
        }
        vmCategory1.getParameters().add(defaultGateway);
        return defaultGateway;
	}

    private ServiceTemplateComponent createHypervVM(List<DeviceInventoryEntity> hypervEntities,
                                                    ServiceTemplateComponent hvCluster,
                                                    List<NetworkConfiguration> networks,
                                                    boolean isClone){
        // create default Hyper-V Virtual Machine settings

        ServiceTemplateComponent hypervVM = new ServiceTemplateComponent();
        if (isClone){
            hypervVM.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_CLONE_COMPONENT);
            hypervVM.setName("Clone Hyper-V Virtual Machine");
        }
        else{
            hypervVM.setId("component-virtualmachine-hyperv-1");
            hypervVM.setName("Hyper-V Virtual Machine");
        }
        hypervVM.setType(ServiceTemplateComponentType.VIRTUALMACHINE);

        ServiceTemplateCategory vmCatSettings = new ServiceTemplateCategory();
        vmCatSettings.setDisplayName("Virtual Machine Settings");
        vmCatSettings.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE);

        if (isClone) {
            ServiceTemplateSetting serverSettingGenerateHostName = new ServiceTemplateSetting();
            serverSettingGenerateHostName.setDisplayName("Auto-generate Name");
            serverSettingGenerateHostName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID);
            serverSettingGenerateHostName.setType(ServiceTemplateSettingType.BOOLEAN);
            serverSettingGenerateHostName.setRequired(false);
            serverSettingGenerateHostName.setRequiredAtDeployment(false);
            serverSettingGenerateHostName.setHideFromTemplate(false);
            serverSettingGenerateHostName.setValue("false");
            serverSettingGenerateHostName.setToolTip("Use the VM Name Template field to auto-generate " +
                    "VM names at deployment time. If not selected, a unique name will be requested " +
                    "when the template is deployed.");
            vmCatSettings.getParameters().add(serverSettingGenerateHostName);

            ServiceTemplateSetting serverSettingHostNameTemplate = new ServiceTemplateSetting();
            serverSettingHostNameTemplate.setDisplayName("VM Name Template");
            serverSettingHostNameTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_TEMPLATE_ID);
            serverSettingHostNameTemplate.setType(ServiceTemplateSettingType.STRING);
            serverSettingHostNameTemplate.setRequired(true);
            serverSettingHostNameTemplate.setRequiredAtDeployment(false);
            serverSettingHostNameTemplate.setHideFromTemplate(false);
            serverSettingHostNameTemplate.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_DEFAULT_TEMPLATE);
            serverSettingHostNameTemplate.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID);
            serverSettingHostNameTemplate.setDependencyValue("true");
            serverSettingHostNameTemplate.setToolTip("Template used to generate VM names at deployment " +
                    "time. Must contain variables that will produce a unique VM name. Allowed variables " +
                    " are ${num} (an auto-generated unique number).");
            vmCatSettings.getParameters().add(serverSettingHostNameTemplate);
            
            // Add the VM_NAME; for non-clone the SERVER_NAME parameter will be used
            ServiceTemplateSetting vmSettingName = new ServiceTemplateSetting();
            vmSettingName.setDisplayName("Name");
            vmSettingName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
            vmSettingName.setType(ServiceTemplateSettingType.STRING);
            vmSettingName.setRequired(true);
            vmSettingName.setRequiredAtDeployment(true);
            vmSettingName.setHideFromTemplate(true);
            vmSettingName.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID);
            vmSettingName.setDependencyValue("false");
            vmCatSettings.getParameters().add(vmSettingName);            
            
//            // Add the VM_NAME; for non-clone the SERVER_NAME parameter will be used
//            ServiceTemplateSetting vmSettingName = new ServiceTemplateSetting();
//            vmSettingName.setDisplayName("Name");
//            vmSettingName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME);
//            vmSettingName.setType(ServiceTemplateSettingType.STRING);
//            vmSettingName.setRequired(true);
//            vmSettingName.setRequiredAtDeployment(true);
//            vmCatSettings.getParameters().add(vmSettingName);
            
            ServiceTemplateSetting vmSettingDescr = new ServiceTemplateSetting();
            vmSettingDescr.setDisplayName("Description");
            vmSettingDescr.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_DESCRIPTION);
            vmSettingDescr.setType(ServiceTemplateSettingType.STRING);
            vmSettingDescr.setRequired(false);
            vmCatSettings.getParameters().add(vmSettingDescr);
            
            ServiceTemplateSetting vmSettingTemplate = new ServiceTemplateSetting();
            vmSettingTemplate.setDisplayName("Template");
            vmSettingTemplate.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_TEMPLATE);
            vmSettingTemplate.setType(ServiceTemplateSettingType.ENUMERATED);
            vmSettingTemplate.setRequired(true);
            
            ServiceTemplateUtil.addDefaultSelectOption(vmSettingTemplate);

            if (hypervEntities!=null) {
                for (DeviceInventoryEntity e: hypervEntities) {
                    try {
                        Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(e.getRefId());

                        HashMap<String, Object> scvmmMap = (HashMap<String, Object>) OBJECT_MAPPER.readValue(deviceDetails.get("value"), HashMap.class);
                        ArrayList<Map<String,String>> vms =(ArrayList<Map<String,String>>) scvmmMap.get("template");

                        if (vms!= null) {
                            for (Map<String, String> hg: vms) {
                                String path = hg.get("Name");
                                ServiceTemplateOption oPath = new ServiceTemplateOption(path, path, null, null);
                                if (!vmSettingTemplate.getOptions().contains(oPath)) {
                                    vmSettingTemplate.getOptions().add(oPath);
                                }
                            }
                        }
                    } catch (Exception pe) {
                        LOGGER.error("Error fetching scvmm data for " + e.getRefId(), pe);
                    }
                }
            }
            vmCatSettings.getParameters().add(vmSettingTemplate);
        }

        ServiceTemplateSetting vmSettingPath = new ServiceTemplateSetting();
        vmSettingPath.setDisplayName("Path");
        vmSettingPath.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_PATH);
        vmSettingPath.setType(ServiceTemplateSettingType.STRING);
        vmSettingPath.setRequired(true);
        vmSettingPath.setRequiredAtDeployment(true);
        vmCatSettings.getParameters().add(vmSettingPath);

        ServiceTemplateSetting vmHvSettingNetwork = new ServiceTemplateSetting();
        vmHvSettingNetwork.setDisplayName("Networks");
        vmHvSettingNetwork.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID);
        vmHvSettingNetwork.setType(ServiceTemplateSettingType.LIST);
        vmHvSettingNetwork.setRequired(true);
        vmHvSettingNetwork.setValue("");
        if (networks != null && networks.size() > 0) {
            for (int i = 0; i < networks.size(); i++) {
                if (networks
                        .get(i)
                        .getType()
                        .compareToIgnoreCase(
                                NetworkType.PRIVATE_LAN.value()) == 0
                        || networks
                        .get(i)
                        .getType()
                        .compareToIgnoreCase(
                                NetworkType.PUBLIC_LAN.value()) == 0) {
                    vmHvSettingNetwork.getOptions().add(new ServiceTemplateOption(networks.get(i).getName(),networks.get(i).getId(),null,null));
                }
            }
        }
        vmCatSettings.getParameters().add(vmHvSettingNetwork);
        setDefaultGatewaySetting(networks, vmCatSettings);

        ServiceTemplateSetting vmSettingOpt = new ServiceTemplateSetting();
        vmSettingOpt.setDisplayName("Block Dynamic Optimization");
        vmSettingOpt.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_OPT);
        vmSettingOpt.setType(ServiceTemplateSettingType.BOOLEAN);
        vmSettingOpt.setRequired(false);
        ServiceTemplateUtil.addBooleanSelectOption(vmSettingOpt);
        vmSettingOpt.setValue("false");
        vmCatSettings.getParameters().add(vmSettingOpt);

        ServiceTemplateSetting vmSettingHA = new ServiceTemplateSetting();
        vmSettingHA.setDisplayName("Highly Available");
        vmSettingHA.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_HIGHLY_AVAIL);
        vmSettingHA.setType(ServiceTemplateSettingType.BOOLEAN);
        ServiceTemplateUtil.addBooleanSelectOption(vmSettingHA);
        vmSettingHA.setRequired(false);
        vmSettingHA.setValue("true");
        vmCatSettings.getParameters().add(vmSettingHA);

        ServiceTemplateSetting hvvmSettingCPU = new ServiceTemplateSetting();
        hvvmSettingCPU.setDisplayName("Number of CPUs");
        hvvmSettingCPU.setValue("1");
        hvvmSettingCPU.setMin(1);
        hvvmSettingCPU.setMax(8);
        hvvmSettingCPU
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NUMBER_OF_CPU_ID);
        hvvmSettingCPU.setType(ServiceTemplateSettingType.INTEGER);
        vmCatSettings.getParameters().add(hvvmSettingCPU);


        ServiceTemplateSetting hvvmSettingMemory = new ServiceTemplateSetting();
        hvvmSettingMemory.setDisplayName("Memory in MB");
        hvvmSettingMemory.setValue("8192");
        hvvmSettingMemory.setMin(1024);
        hvvmSettingMemory.setMax(262144);   //256*1024
        hvvmSettingMemory
                .setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_MEMORY_ID);
        hvvmSettingMemory.setType(ServiceTemplateSettingType.INTEGER);
        vmCatSettings.getParameters().add(hvvmSettingMemory);
        
        if (!isClone) {
            ServiceTemplateSetting vmSettingHVDN = new ServiceTemplateSetting();
            vmSettingHVDN.setDisplayName("Domain Name");
            vmSettingHVDN.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_OS_DN_ID);
            vmSettingHVDN.setType(ServiceTemplateSettingType.STRING);
            vmSettingHVDN.setRequired(false);
            vmCatSettings.getParameters().add(vmSettingHVDN);
            
            ServiceTemplateSetting vmSettingDescr = new ServiceTemplateSetting();
            vmSettingDescr.setDisplayName("Description");
            vmSettingDescr.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_DESCRIPTION);
            vmSettingDescr.setType(ServiceTemplateSettingType.STRING);
            vmSettingDescr.setRequired(false);
            vmCatSettings.getParameters().add(vmSettingDescr);

            ServiceTemplateSetting vmSettingHVAdminLogin = new ServiceTemplateSetting();
            vmSettingHVAdminLogin.setDisplayName("Domain Admin Username");
            vmSettingHVAdminLogin.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_HV_ADMIN_LOGIN_ID);
            vmSettingHVAdminLogin.setType(ServiceTemplateSettingType.STRING);
            vmSettingHVAdminLogin.setRequired(false);
            vmCatSettings.getParameters().add(vmSettingHVAdminLogin);

            ServiceTemplateSetting vmSettingHVAdminPass = new ServiceTemplateSetting();
            vmSettingHVAdminPass.setDisplayName("Domain Admin Password");
            vmSettingHVAdminPass.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_DOMAIN_PASSWORD_ID);
            vmSettingHVAdminPass.setType(ServiceTemplateSettingType.PASSWORD);
            vmSettingHVAdminPass.setRequired(false);
            vmSettingHVAdminPass.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);
            vmCatSettings.getParameters().add(vmSettingHVAdminPass);

            ServiceTemplateSetting vmSettingHVAdminPassConfirm = new ServiceTemplateSetting();
            vmSettingHVAdminPassConfirm.setDisplayName("Domain Admin Password Confirm");
            vmSettingHVAdminPassConfirm.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_DOMAIN_CONFIRM_PASSWORD_ID);
            vmSettingHVAdminPassConfirm.setType(ServiceTemplateSettingType.PASSWORD);
            vmSettingHVAdminPassConfirm.setRequired(false);
            vmSettingHVAdminPassConfirm.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);
            vmCatSettings.getParameters().add(vmSettingHVAdminPassConfirm);
        }

        ServiceTemplateSetting vmSettingStartAction = new ServiceTemplateSetting();
        vmSettingStartAction.setDisplayName("Start Action");
        vmSettingStartAction.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_START_ACTION);
        vmSettingStartAction.setType(ServiceTemplateSettingType.ENUMERATED);
        vmSettingStartAction.setRequired(true);

        ServiceTemplateUtil.addDefaultSelectOption(vmSettingStartAction);

        vmSettingStartAction.getOptions().add(new ServiceTemplateOption("Never auto turn on VM",
                "never_auto_turn_on_vm", null, null));
        vmSettingStartAction.getOptions().add(new ServiceTemplateOption("Always auto turn on VM",
                "always_auto_turn_on_vm", null, null));
        vmSettingStartAction.getOptions().add(new ServiceTemplateOption("Turn on VM if running when VS stopped",
                "turn_on_vm_if_running_when_vs_stopped", null, null));

        vmCatSettings.getParameters().add(vmSettingStartAction);

        ServiceTemplateSetting vmSettingStopAction = new ServiceTemplateSetting();
        vmSettingStopAction.setDisplayName("Stop Action");
        vmSettingStopAction.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_STOP_ACTION);
        vmSettingStopAction.setType(ServiceTemplateSettingType.ENUMERATED);
        vmSettingStopAction.setRequired(true);

        ServiceTemplateUtil.addDefaultSelectOption(vmSettingStopAction);

        vmSettingStopAction.getOptions().add(new ServiceTemplateOption("Save VM",
                "save_vm", null, null));

        vmSettingStopAction.getOptions().add(new ServiceTemplateOption("Turn off VM",
                "turn_off_vm", null, null));

        vmSettingStopAction.getOptions().add(new ServiceTemplateOption("Shutdown guest OS",
                "shutdown_guest_os", null, null));

        vmCatSettings.getParameters().add(vmSettingStopAction);

        hypervVM.getResources().add(vmCatSettings);
        return hypervVM;
    }

    private void addWindowsFlavors(ServiceTemplateSetting osSubType, Map<String, String> imageTargets) {
        String windows2012Targets = imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE);
        String windows2008Targets = imageTargets.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE);

        //Don't see a way to specify "no targets", so don't add options if no targets exist.
        if (windows2012Targets != null)
        {
            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 SERVERDATACENTER",
                    "windows2012datacenter",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));

            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 R2 SERVERDATACENTER",
                    "windows2012r2datacenter",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));

            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 R2 SERVERSTANDARD",
                    "windows2012r2standard",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));

            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 SERVERSTANDARD",
                    "windows2012standard",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));

            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 SERVERDATACENTERCORE",
                    "windows2012datacentercore",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));

            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 R2 SERVERDATACENTERCORE",
                    "windows2012r2datacentercore",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));


            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2012 R2 SERVERSTANDARDCORE",
                    "windows2012r2standardcore",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2012Targets));
        }

        if (windows2008Targets != null)
        {
            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2008 R2 Datacenter",
                        "windows2008r2datacenter",
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                        windows2008Targets));

            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2008 R2 Standard",
                    "windows2008r2standard",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2008Targets));


            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2008 R2 Enterprise",
                    "windows2008r2enterprise",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2008Targets));


            osSubType.getOptions().add(new ServiceTemplateOption("Windows Server 2008 R2 Web",
                    "windows2008r2web",
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID,
                    windows2008Targets));
        }

    }

    private boolean isSystemUser() {
        // If servletRequest has not been injected it is because this service has been
        // instantiated outside of JAX-RS, e.g. by the startup code that upgrades templates.
        // In this case we can be considered to be running as the "system" user and do not check
        // permissions.
        return servletRequest == null && servletResponse == null;
    }

    /**
     * Check is logged user is allowed to access this template.
     * @param templateEntity
     * @return
     */
    private boolean checkUserPermissions(ServiceTemplateEntity templateEntity, Long userId, User thisUser) {
        if (isSystemUser()) {
            return true;
        }

        if (userId == null) {
            userId = AsmManagerUtil.getUserId();
        }

        // TODO: remove when asm_deployer gets REST headers
        if (userId == 0) {
            userId = (long) 1;
        }

        if (thisUser == null) {
            thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);
        }

        if (thisUser.getRole().equals(AsmConstants.USERROLE_ADMINISTRATOR) ||
                thisUser.getRole().equals(AsmConstants.USERROLE_READONLY))
            return true;

        if (templateEntity.isDraft())
            return false;

        if (templateEntity.isAllUsersAllowed()) return true;

        for (TemplateUserRefEntity ref: templateEntity.getAssignedUserList()) {
            if (userId.longValue() ==  ref.getUserId()) {
                return true;
            }
        }

        return false;
    }

    private void resetStaticIPValues(ServiceTemplate template) {
        for (ServiceTemplateComponent component : template.getComponents()) {
            if(ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                    ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                ServiceTemplateCategory osResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                if (osResource != null && osResource.getParameters() != null) {
                    for (ServiceTemplateSetting param : osResource.getParameters()) {
                        if (param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE)) {
                            param.setValue(null);
                        }
                    }
                }
            }
        }
    }

    /**
     * For template settings filter out resources by user permissions. I.e. server pool list.
     * @param svc
     */
    private void filterOutResources(ServiceTemplate svc) {
        if (isSystemUser()) {
            return;
        }

        User thisUser = this.asmManagerUtil.getCurrentUser(servletRequest);

        if (thisUser.getRole().equals(AsmConstants.USERROLE_ADMINISTRATOR) ||
                thisUser.getRole().equals(AsmConstants.USERROLE_READONLY))
            return;

        for (ServiceTemplateComponent component: svc.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                // it can be in more than one resource, but always the same ID
                ServiceTemplateSetting set = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID);

                if (set != null && set.getOptions() != null) {
                    Iterator<ServiceTemplateOption> it = set.getOptions().iterator();
                    while (it.hasNext()) {
                        ServiceTemplateOption opt = it.next();

                        if (opt.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID))
                            continue;
                        try {
                            // if for any reason we can't get the server pool details - we are not allowed
                            ProxyUtil.getServerPoolProxy().getDeviceGroup(opt.getValue());
                        } catch (WebApplicationException wex) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    @Override
    public String exportTemplate(ServiceTemplate svc,
                          String encPassword,
                          boolean useEncPwdFromBackup) {
        String fileName = null;
        try {
            if (svc == null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.templateNotFound(null));
            }

            String templateId = svc.getId();

            ServiceTemplateEntity templateEntity = templateDao.getTemplateById(templateId); // find
            // the template in DB.
            if (templateEntity == null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.templateNotFound(templateId));
            }

            // passwords are empty now - get them!
            ServiceTemplate origTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                    templateEntity.getMarshalledTemplateData());
            serviceTemplateUtil.encryptPasswords(svc, origTemplate); // this will bring passwords to the instance
            serviceTemplateUtil.decryptPasswords(svc); // we need those in clear text

            // decrypt
            if (useEncPwdFromBackup) {
                DatabaseBackupSettings databaseBackupSettings = ProxyUtil.getAlcmDBProxy().getDatabaseBackupSettings();
                encPassword = databaseBackupSettings.getEncryptionPassword();
                if (encPassword == null) {
                    throw new LocalizedWebApplicationException(
                            Response.Status.BAD_REQUEST,
                            AsmManagerMessages.noBackupPassword());
                }
            }else{
                if (encPassword == null || encPassword.length() == 0) {
                    throw new LocalizedWebApplicationException(
                            Response.Status.BAD_REQUEST,
                            AsmManagerMessages.PasswordNullEmpty("Encryption Password"));
                }
            }

            fileName = serviceTemplateUtil.exportTemplate(svc, encPassword);

        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while exporting service template "
                            + svc.getTemplateName(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while exporting service template "
                    + svc.getTemplateName(), e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        if (fileName != null) {
            return fileName;
        }else{
            LOGGER.error("Export template produced no content");
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
    }
    
    @Override
    public Response exportAllTemplates() throws WebApplicationException {
        return Response
                .ok(templateDao.new StreamingCSVTemplateOutput(), MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = templates.csv")
                .build();
    }    

    @Override
    public ServiceTemplate uploadTemplate(ServiceTemplateUploadRequest uploadRequest) {
        ServiceTemplate svc = null;
        try {

            if (uploadRequest.getFileData()==null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.noTemplateFile());
            }

            // encrypt
            if (uploadRequest.isUseEncPwdFromBackup()) {
                DatabaseBackupSettings databaseBackupSettings = ProxyUtil.getAlcmDBProxy().getDatabaseBackupSettings();
                String encPassword = databaseBackupSettings.getEncryptionPassword();
                if (encPassword == null) {
                    throw new LocalizedWebApplicationException(
                            Response.Status.BAD_REQUEST,
                            AsmManagerMessages.noBackupPassword());
                }
                uploadRequest.setEncryptionPassword(encPassword);
            }

            try {
                svc = ServiceTemplateUtil.importTemplate(uploadRequest.getFileData(), uploadRequest.getEncryptionPassword());
            } catch (Exception e) {
                LOGGER.error(e);
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.badTemplateFile(e.getMessage()));
            }

            ServiceTemplateEntity serviceTemplateEntity = templateDao.getTemplateById(svc.getId());
            if (serviceTemplateEntity != null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.uploadedServiceTemplateExists());
            }

            if (uploadRequest.getDescription()!=null)
                svc.setTemplateDescription(uploadRequest.getDescription());
            if (uploadRequest.getCategory()!=null && uploadRequest.getCategory().length()>0)
                svc.setCategory(uploadRequest.getCategory());
            if (uploadRequest.getTemplateName()!=null)
                svc.setTemplateName(uploadRequest.getTemplateName());

            svc.setManageFirmware(uploadRequest.isManageFirmware());
            if (uploadRequest.isManageFirmware()) {
                if (uploadRequest.isUseDefaultCatalog()) {
                    svc.setUseDefaultCatalog(true);
                    svc.setFirmwareRepository(null);
                }
                else if (StringUtils.isNotBlank(uploadRequest.getFirmwarePackageId())) {
                    FirmwareRepositoryEntity firmwareRepositoryEntity = firmwareRepositoryDAO.get(uploadRequest.getFirmwarePackageId());
                    if (firmwareRepositoryEntity != null) {
                        svc.setFirmwareRepository(firmwareRepositoryEntity.getSimpleFirmwareRepository());
                    }
                }
            }
            if (uploadRequest.isManagePermissions()) {

                svc.setAllUsersAllowed(uploadRequest.isAllStandardUsers());
                svc.setAssignedUsers(new HashSet<User>());

                IUserResource adminProxy = ProxyUtil.getAdminProxy();
                ProxyUtil.setProxyHeaders(adminProxy, servletRequest);

                List<String> fUser = new ArrayList<>();
                if (uploadRequest != null && uploadRequest.getAssignedUsers() != null) {
                    for (String userName : uploadRequest.getAssignedUsers()) {
                        fUser.clear();
                        fUser.add("eq,userName," + userName);
                        User[] lUsers = adminProxy.getUsers(fUser, null, null, null);
                        if (lUsers != null && lUsers.length == 1) {
                            svc.getAssignedUsers().add(lUsers[0]);
                        }
                    }
                }
            }

            // Add the service template configuration
            addServiceTemplateConfiguration(svc);
            svc.setDraft(true);
            ServiceTemplateUtil.stripPasswords(svc, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);

        } catch (LocalizedWebApplicationException e) {
            LOGGER.error(
                    "LocalizedWebApplicationException while importing service template", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while importing service template", e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        return svc;
    }

    public void fillMissingParams(ServiceTemplate template){
        fillMissingParams (getDefaultTemplate(), template);
    }

    public static void fillMissingParams(ServiceTemplate defaultTemplate, ServiceTemplate template) {
        if (defaultTemplate == null) {
            return;
        }

        for (ServiceTemplateComponent component : template.getComponents()) {
            if (ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(component)) {
                ServiceTemplateComponentUpgrader.assignOriginatingComponentId(component);
            }
            ServiceTemplateComponent parentComponent = defaultTemplate.findComponentById(component.getComponentID());
            List<ServiceTemplateCategory> addResources = new ArrayList<>();
            List<ServiceTemplateCategory> removeResources = new ArrayList<>();
            List<String> defaultResourceIds = new ArrayList<>();

            if (parentComponent == null) {
                return;
            } else {
                //Loop through default template to get all parent Resource ID's
                for (ServiceTemplateCategory parentResource : parentComponent.getResources()) {
                    defaultResourceIds.add(parentResource.getId());
                }

                boolean hasHyperV = false;
                String ntpServer = null;
                Map<String, String> biosSettings = null;
                if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                    ntpServer = getNtpServer(component);
                    biosSettings = mapBiosSettings(component);
                }

                Set<ServiceTemplateSetting> staticIPSettings = new LinkedHashSet<>();
                ServiceTemplateCategory templateResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                if (templateResource != null) {
                    ServiceTemplateSetting templateSetting = templateResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
                    if (templateSetting != null) {
                        if (templateSetting.getValue().equals("true")) {
                            hasHyperV = true;
                        }
                    }
                    for (ServiceTemplateSetting setting : templateResource.getParameters()) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE.equals(setting.getId()) ||
                                setting.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE) ||
                                setting.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE)) {
                            staticIPSettings.add(setting);
                        }
                    }
                }

                for (ServiceTemplateCategory resource : component.getResources()) {
                    ServiceTemplateCategory defaultResource = template.getTemplateResource(parentComponent, resource.getId());

                    // Only merge if resource exists in default template
                    if (defaultResource != null) {
                        List<String> defaultParamIds = new ArrayList<>();
                        // Loop through default template to get all parameters
                        for (ServiceTemplateSetting parentSetting : defaultResource.getParameters()) {
                            defaultParamIds.add(parentSetting.getId());
                        }

                        List<ServiceTemplateSetting> addParams = new ArrayList<>();
                        List<ServiceTemplateSetting> removeParams = new ArrayList<>();
                        //we will need to set the new hyper_v_install later

                        for (ServiceTemplateSetting parameter : resource.getParameters()) {

                            // upgrade parameter if needed
                            ServiceTemplateUtil.upgradeParameter(parameter, template.getTemplateVersion());

                            if (!parameter.isGenerated()) {

                                ServiceTemplateSetting defaultParam = defaultTemplate.getTemplateSetting(defaultResource, parameter.getId());

                                // Only merge if parameter exists in default template and was not marked immutable
                                if (defaultParam != null) {
                                    ServiceTemplateSetting newParam = new ServiceTemplateSetting();

                                    List<String> optionValues = new ArrayList<>();
                                    //get option values for enum and list

                                    if (parameter.getType() == null) {
                                        LOGGER.error("Parameter type is NULL for ID=" + parameter.getId() + ", category=" + resource.getId());
                                        parameter.setType(ServiceTemplateSettingType.STRING);
                                    }

                                    if (parameter.getType() == ServiceTemplateSettingType.LIST ||
                                            parameter.getType() == ServiceTemplateSettingType.ENUMERATED) {
                                        for (ServiceTemplateOption option : defaultParam.getOptions()) {
                                            optionValues.add(option.getValue());
                                        }
                                    }

                                    parameter.setDisplayName(defaultParam.getDisplayName());
                                    parameter.setToolTip(defaultParam.getToolTip());
                                    parameter.setRequired(defaultParam.isRequired());
                                    parameter.setRequiredAtDeployment(defaultParam.isRequiredAtDeployment());
                                    parameter.setHideFromTemplate(defaultParam.isHideFromTemplate());
                                    parameter.setReadOnly(defaultParam.isReadOnly());
                                    parameter.setInfoIcon(defaultParam.isInfoIcon());
                                    parameter.setGroup(defaultParam.getGroup());
                                    parameter.setMin(defaultParam.getMin());
                                    parameter.setMax(defaultParam.getMax());
                                    parameter.setMaxLength(defaultParam.getMaxLength());
                                    parameter.setReadOnly(defaultParam.isReadOnly());
                                    parameter.setInfoIcon(defaultParam.isInfoIcon());
                                    parameter.setStep(defaultParam.getStep());
                                    if (defaultParam.getDependencyTarget() != null) {
                                        parameter.setDependencyTarget(defaultParam.getDependencyTarget());
                                        parameter.setDependencyValue(defaultParam.getDependencyValue());
                                    } else {
                                        parameter.setDependencyTarget(null);
                                        parameter.setDependencyValue(null);
                                    }
                                    //TODO
                                    // If we've changed from an Bool to an enum, and the options
                                    // are enabled & disabled, remap these
                                    if (!parameter.getType().equals(defaultParam.getType()) && defaultParam.getType().equals(ServiceTemplateSettingType.ENUMERATED)) {
                                        for (ServiceTemplateOption defaultOption : defaultParam.getOptions()) {
                                            if (defaultOption.getName().equalsIgnoreCase("enabled")) {
                                                if (parameter.getValue().equalsIgnoreCase("true")) {
                                                    parameter.setValue("Enabled");
                                                } else if (parameter.getValue().equalsIgnoreCase("false")) {
                                                    parameter.setValue("Disabled");
                                                } else {
                                                    parameter.setValue(defaultParam.getValue());
                                                }
                                            }
                                        }
                                        parameter.setType(defaultParam.getType());
                                    }

                                    // do not touch options and values for sample templates
                                    if (!template.isTemplateLocked() && (!parameter.isHideFromTemplate() || !parameter.isRequiredAtDeployment())) {
                                        // Update the options based on the default template, which should
                                        // contain up-to-date values for e.g. storage volumes in inventory.
                                        // Skip for deployment-time only options.

                                        parameter.setOptions(defaultParam.getOptions());
                                        // set parameter value to null if it isn't an option
                                        if (optionValues.size() > 0 && parameter.getValue() != null) {
                                            if (parameter.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID)) {
                                                List<String> params = new ArrayList<String>(Arrays.asList(parameter.getValue().split(",")));
                                                Iterator<String> iterator = params.iterator();
                                                while (iterator.hasNext()) {
                                                    String eachOption = iterator.next();
                                                    if (!optionValues.contains(eachOption)) {
                                                        iterator.remove();
                                                        LOGGER.info("Selected option not in available valid options: " + eachOption);
                                                    }
                                                }
                                                parameter.setValue(StringUtils.join(params, ','));
                                            } else if (!optionValues.contains(parameter.getValue())) {
                                                parameter.setValue(null);
                                            }
                                        }
                                    }

                                    try {
                                        newParam = (ServiceTemplateSetting) BeanUtils.cloneBean(parameter);
                                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                                        LOGGER.error(
                                                "Problem cloning the parameter: " + parameter.getId() + " in service template: " + template.getId(), e
                                        );
                                    }

                                    if (newParam != null) {
                                        try {
                                            // preserve values for some params even if they are null
                                            String oldValue = newParam.getValue();
                                            merge(newParam, defaultParam);
                                            if (oldValue == null &&
                                                    (ServiceTemplateSettingType.PASSWORD.equals(parameter.getType()) ||
                                                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE.equals(parameter.getValue()))) {
                                                newParam.setValue(null);
                                            }

                                        } catch (Exception e) {
                                            LOGGER.warn("Parameters merge error", e);
                                        }
                                    }
                                    addParams.add(newParam);
                                    removeParams.add(parameter);

                                } else {
                                    removeParams.add(parameter);
                                }
                            }
                        }
                        //remove old parameters
                        for (ServiceTemplateSetting removeParam : removeParams) {
                            for (Iterator<ServiceTemplateSetting> iter = resource.getParameters().iterator(); iter.hasNext(); ) {
                                ServiceTemplateSetting setting = iter.next();
                                if (setting == removeParam) {
                                    iter.remove();
                                }
                            }
                        }
                        List<String> paramIds = new ArrayList<>();
                        //add newly merged parameters
                        for (ServiceTemplateSetting addParam : addParams) {
                            resource.getParameters().add(addParam);
                            paramIds.add(addParam.getId());
                        }
                        //add whole parameters
                        for (String defaultParamId : defaultParamIds) {
                            if (!paramIds.contains(defaultParamId)) {
                                ServiceTemplateSetting newSetting = template.getTemplateSetting(defaultResource, defaultParamId);
                                resource.getParameters().add(newSetting);
                            }
                        }

                        ServiceTemplateCategory newResource = new ServiceTemplateCategory();

                        try {
                            newResource = (ServiceTemplateCategory) BeanUtils.cloneBean(resource);
                        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                            LOGGER.error(
                                    "Problem cloning the resource: " + resource.getId() + " in service template: " + template.getId(), e
                            );
                        }

                        try {
                            merge(newResource, defaultResource);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        addResources.add(newResource);
                        removeResources.add(resource);
                    } else {
                        removeResources.add(resource);
                    }
                }
                //remove old resources
                for (ServiceTemplateCategory removeResource : removeResources) {
                    for (Iterator<ServiceTemplateCategory> iter = component.getResources().iterator(); iter.hasNext(); ) {
                        ServiceTemplateCategory resource = iter.next();
                        if (resource == removeResource) {
                            iter.remove();
                        }
                    }
                }
                List<String> resourceIds = new ArrayList<>();
                //add newly merged resources
                for (ServiceTemplateCategory addResource : addResources) {
                    component.getResources().add(addResource);
                    resourceIds.add(addResource.getId());
                }
                //add whole new resources
                for (String defaultResourceId : defaultResourceIds) {
                    if (!resourceIds.contains(defaultResourceId)) {
                        ServiceTemplateCategory newResource = template.getTemplateResource(parentComponent, defaultResourceId);
                        component.getResources().add(newResource);
                    }
                }
                ServiceTemplateCategory resource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                if (resource != null && hasHyperV) {
                    ServiceTemplateSetting setting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
                    if (setting != null) {
                        setting.setValue("true");
                    }
                }
                if (resource != null && StringUtils.isNotBlank(ntpServer)) {
                    ServiceTemplateSetting setting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID);
                    if (setting != null) {
                        setting.setValue(ntpServer);
                    }
                }
                if (resource != null && staticIPSettings.size() > 0) {
                    for (ServiceTemplateSetting setting : staticIPSettings) {
                        resource.getParameters().add(setting);
                    }
                }
                if (biosSettings != null && biosSettings.size() != 0) {
                    remapBiosSettings(biosSettings, component);
                }
            }
        }

        for (ServiceTemplateComponent component : template.getComponents()) {
            // Re-order resources to match the schema template order
            ServiceTemplateComponent schemaComponent = defaultTemplate.findComponentById(component.getComponentID());
            List<ServiceTemplateCategory> orderedResources = new ArrayList<>();
            for (ServiceTemplateCategory schemaResource : schemaComponent.getResources()) {
                orderedResources.add(component.getTemplateResource(schemaResource.getId()));
            }
            // don't forget about new added resources and params - those are not in default template!!!
            List <ServiceTemplateCategory> newResources = new ArrayList<>();
            for (ServiceTemplateCategory curCategory: component.getResources()) {
                if (!orderedResources.contains(curCategory)) {
                    newResources.add(curCategory);
                }
            }
            component.setResources(orderedResources);
            component.getResources().addAll(newResources);

            for (ServiceTemplateCategory resource : component.getResources()) {
                // Re-order parameters to match the schema template order
                ServiceTemplateCategory schemaResource = schemaComponent.getTemplateResource(resource.getId());
                List<ServiceTemplateSetting> orderedParameters = new ArrayList<>();
                for (ServiceTemplateSetting schemaParameter : schemaResource.getParameters()) {
                    orderedParameters.add(resource.getParameter(schemaParameter.getId()));
                }

                List <ServiceTemplateSetting> newParams = new ArrayList<>();
                for (ServiceTemplateSetting curParam: resource.getParameters()) {
                    if (!orderedParameters.contains(curParam)) {
                        newParams.add(curParam);
                    }
                }

                resource.setParameters(orderedParameters);
                mergeParameters(resource.getParameters(), newParams);
            }
        }

        ServiceTemplateUtil.refineComponents(template, null, null);
    }

    /**
     * Update the OS Resource to include static network settings for manually setting the static IP
     * @param component
     * @param networks
     */
    private void mapStaticNetworkOSSettings(ServiceTemplateComponent component, Set<Network> networks) {


        //Map containing current static network settings on the current component
        Map<String, ServiceTemplateSetting> currentSettings = new HashMap<>();
        //Map containing expected static network settings on the current component
        Map<String, ServiceTemplateSetting> expectedSettings = new HashMap<>();

        //Get the OS category on the current component
        ServiceTemplateCategory osCategory = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
        if (osCategory != null) {
            // Recreate list to put things in order
            List<ServiceTemplateSetting> osSettings = new ArrayList<>();
            //Loop through storing current settings in map
            for (ServiceTemplateSetting parameter : osCategory.getParameters()) {
                if (parameter.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE) ||
                        parameter.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE)) {
                    currentSettings.put(parameter.getId(), parameter);
                }
            }

            boolean ipSourceFound = false;
            ServiceTemplateSetting osIPSourceSetting = null;
            for (ServiceTemplateSetting parameter : osCategory.getParameters()) {
                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE.equals(parameter.getId())) {
                    ipSourceFound = true;
                    if (CollectionUtils.isNotEmpty(networks)) {
                        osIPSourceSetting = parameter;
                        osSettings.add(parameter);
                        updateStaticIpSettingsForNetworks(component, osSettings, networks, currentSettings, expectedSettings, ipSourceFound);
                    }
                } else {
                    osSettings.add(parameter);
                }
            }
            if (!ipSourceFound) {
                osIPSourceSetting = updateStaticIpSettingsForNetworks(component, osSettings, networks, currentSettings, expectedSettings, ipSourceFound);
            }

            //Loop through CurrentSettings to find Settings that need to be removed.
            for (Map.Entry<String, ServiceTemplateSetting> entry : currentSettings.entrySet()) {
                if (expectedSettings.get(entry.getKey()) == null) {
                    osSettings.remove(entry.getValue());
                }
            }
            if (osIPSourceSetting != null && expectedSettings.isEmpty()) {
                osSettings.remove(osIPSourceSetting);
            }
            osCategory.setParameters(osSettings);
        }
    }

    private ServiceTemplateSetting updateStaticIpSettingsForNetworks(ServiceTemplateComponent component,
                                                   List<ServiceTemplateSetting> osSettings,
                                                   Set<Network> networks,
                                                   Map<String, ServiceTemplateSetting> currentSettings,
                                                   Map<String, ServiceTemplateSetting> expectedSettings,
                                                   boolean ipSourceFound) {
        ServiceTemplateSetting serverSettingIpSource = null;
        if (CollectionUtils.isNotEmpty(networks)) {
            if (!ipSourceFound) {
                serverSettingIpSource = new ServiceTemplateSetting();
                serverSettingIpSource.setDisplayName("IP Source");
                serverSettingIpSource.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
                serverSettingIpSource.setType(ServiceTemplateSettingType.RADIO);
                serverSettingIpSource.setRequired(true);
                serverSettingIpSource.setRequiredAtDeployment(true);
                serverSettingIpSource.setHideFromTemplate(true);
                ServiceTemplateOption[] options = new ServiceTemplateOption[] {
                        new ServiceTemplateOption("ASM Selected IP", ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_AUTOMATIC, null, null),
                        new ServiceTemplateOption("User Entered IP", ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL, null, null)
                };
                serverSettingIpSource.getOptions().addAll(Arrays.asList(options));
                serverSettingIpSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_AUTOMATIC);
                osSettings.add(serverSettingIpSource);
            }

            //Loop through list of static networks
            for (Network network : networks) {
                //If HyperVisor Server and a workload network
                if (ServiceTemplateComponentType.SERVER.equals(component.getType()) &&
                        ServiceTemplateComponent.ServiceTemplateComponentSubType.HYPERVISOR.equals(component.getSubType()) &&
                        (NetworkType.PRIVATE_LAN.equals(network.getType()) ||
                                NetworkType.PUBLIC_LAN.equals((network.getType())))) {
                    //skip
                    continue;
                }
                if (expectedSettings.get(network.getName()) == null) {

                    ServiceTemplateSetting currentSetting = currentSettings.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
                    // If the current network IP source setting is found then use it
                    if (currentSetting != null) {
                        osSettings.add(currentSetting);
                        expectedSettings.put(currentSetting.getId(), currentSetting);
                    } else {
                        //Create Assign Static Network Setting
                        ServiceTemplateSetting serverSettingStaticIpSource = new ServiceTemplateSetting();
                        serverSettingStaticIpSource.setDisplayName(network.getName() + " IP Source");
                        serverSettingStaticIpSource.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
                        serverSettingStaticIpSource.setType(ServiceTemplateSettingType.ENUMERATED);
                        serverSettingStaticIpSource.setRequired(true);
                        serverSettingStaticIpSource.setRequiredAtDeployment(true);
                        serverSettingStaticIpSource.setHideFromTemplate(true);
                        serverSettingStaticIpSource.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE);
                        serverSettingStaticIpSource.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE_MANUAL);
                        serverSettingStaticIpSource.getOptions().add(new ServiceTemplateOption("ASM Selected IP",
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_AUTOMATIC,
                                null,
                                null));
                        if (NetworkType.HYPERVISOR_MANAGEMENT.equals(network.getType()) ||
                                NetworkType.PUBLIC_LAN.equals(network.getType()) ||
                                NetworkType.PRIVATE_LAN.equals(network.getType())) {
                            serverSettingStaticIpSource.getOptions().add(new ServiceTemplateOption("Hostname DNS Lookup",
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_DNS,
                                    null,
                                    null));
                        }
                        serverSettingStaticIpSource.getOptions().add(new ServiceTemplateOption("Manual Entry",
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL,
                                null,
                                null));
                        serverSettingStaticIpSource.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_AUTOMATIC);
                        osSettings.add(serverSettingStaticIpSource);
                        expectedSettings.put(serverSettingStaticIpSource.getId(), serverSettingStaticIpSource);
                    }

                    currentSetting = currentSettings.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                    // If the current network IP value setting is found then use it
                    if (currentSetting != null) {
                        osSettings.add(currentSetting);
                        expectedSettings.put(currentSetting.getId(), currentSetting);
                    } else {
                        //Create Static IP Address Field for network
                        ServiceTemplateSetting manualIpSetting = new ServiceTemplateSetting();
                        manualIpSetting.setDisplayName(network.getName() + " IP Address");
                        manualIpSetting.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE + network.getName());
                        manualIpSetting.setType(ServiceTemplateSettingType.STRING);
                        manualIpSetting.setRequired(true);
                        manualIpSetting.setRequiredAtDeployment(true);
                        manualIpSetting.setHideFromTemplate(true);
                        manualIpSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE + network.getName());
                        manualIpSetting.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_SOURCE_MANUAL);
                        osSettings.add(manualIpSetting);
                        expectedSettings.put(manualIpSetting.getId(), manualIpSetting);
                    }
                }
            }
        }
        return serverSettingIpSource;
    }

    /**
     * Append new parameters to existing groups. If group is new, add to the bottom.
     * @param parameters Existing parameters, will be modified
     * @param newParams New parameters
     */
    private static void mergeParameters(List<ServiceTemplateSetting> parameters, List<ServiceTemplateSetting> newParams) {
        if (parameters==null || newParams == null) return;

        for (ServiceTemplateSetting parameter: newParams) {
            if (StringUtils.isNotEmpty(parameter.getGroup())) {
                // find the group in existing parameters
                int index = getParametersGroupLastIndex(parameters, parameter.getGroup());
                if (index >=0) {
                    parameters.add(index+1, parameter);
                }else{
                    // new group - just add to the end
                    parameters.add(parameter);
                }
            }else{
                // just add to the end
                parameters.add(parameter);
            }
        }
    }

    /**
     * Will return index of the last parameter that has the same group as specified.
     * we assume parameters are already sorted by group.
     * @param parameters
     * @param group
     * @return
     */
    private static int getParametersGroupLastIndex(List<ServiceTemplateSetting> parameters, String group) {
        boolean groupFound = false;
        for (ServiceTemplateSetting parameter: parameters) {
            if (!groupFound) {
                if (group.equals(parameter.getGroup()))
                    groupFound = true;
            }else{
                if (!group.equals(parameter.getGroup())) {
                    // next group started
                    return parameters.indexOf(parameter)-1;
                }
            }
        }
        // if we still here means our group was the last in the list
        if (groupFound)
            return parameters.size()-1;

        return -1; // group not found
    }

    public static <M> void merge(M target, M source) {
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());

            // Iterate over all the attributes
            for (final PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                // Only copy writable attributes
                if (descriptor.getWriteMethod() != null) {
                    final Object originalValue = descriptor.getReadMethod().invoke(target);
                    // Only copy values values where the destination values is null
                    if (originalValue == null) {
                        final Object defaultValue = descriptor.getReadMethod().invoke(source);
                        descriptor.getWriteMethod().invoke(target, defaultValue);
                    }
                }
            }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void remapBiosSettings(Map<String, String> biosSettings, ServiceTemplateComponent component) {
        ServiceTemplateCategory biosResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_BIOS_RESOURCE);
        if (biosResource != null) {
            for (Map.Entry<String, String> setting : biosSettings.entrySet()) {
                ServiceTemplateSetting newSetting = biosResource.getParameter(setting.getKey());
                if (newSetting != null) {
                    newSetting.setValue(setting.getValue());
                }
            }
        }
    }

    private static Map<String, String> mapBiosSettings(ServiceTemplateComponent component) {
        Map<String,String> biosSettings = new HashMap<>();
        ServiceTemplateCategory resource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE);
        if (resource != null) {
            for (Map.Entry<String,Map<String,String>> setting : OLD_BIOS_TEMPLATE_MAP.entrySet()) {
                String newName = setting.getKey();
                String oldName = setting.getValue().get("name");
                ServiceTemplateSetting oldSetting = resource.getParameter(oldName);
                if (oldSetting != null) {
                    if (setting.getValue().get("remap").equals("true")) {
                        biosSettings.put(newName,setting.getValue().get(oldSetting.getValue()));
                    }
                    else {
                        biosSettings.put(newName, oldSetting.getValue());
                    }
                }
            }
            return biosSettings;
        }
        return null;
    }

    private static String getNtpServer(ServiceTemplateComponent component) {
        ServiceTemplateSetting ntp = component.getTemplateSetting("ntp");
        if (ntp != null) {
            return ntp.getValue();
        }
        return null;
    }

    public class GroupComparator implements java.util.Comparator<ServiceTemplateSetting> {
        @Override
        public int compare(ServiceTemplateSetting x, ServiceTemplateSetting y) {
            if (x.getGroup()==null && y.getGroup()==null)
                return 0;
            else if (x.getGroup()!=null && y.getGroup()==null)
                return 1;
            else if (x.getGroup()==null && y.getGroup()!=null)
                return -1;
            else
                return x.getGroup().compareTo(y.getGroup());
        }
    }

    private static String getFabricLocation(String s){
        String numeric = s.split("^\\w+\\.\\w+\\.")[1];
        String[] numericSplit = numeric.split("-");
        String location = numericSplit[0];
        if(s.contains("NIC.Embedded") || s.contains("NIC.Integrated")){
            return "0";
        }
        if(StringUtils.isNumeric(location)){
            return location;
        }else{
            return location.charAt(location.length() -1) + "";
        }
    }

    public class FabricComparator implements Comparator<String>{
        @Override
        public int compare(final String o1, final String o2){
            //Integrated NICs should always come first, but sometimes they won't with pci-e slots
            //Example:  NIC.ChassisSlot.1-1-1 should be B, but NIC.Integrated.1-1-1 should be A, but both will be assigned to "A" since they are in slot 1.
            if(o1.contains(".Integrated") && !o2.contains(".Integrated"))
                return -1;
            else if (o2.contains(".Integrated") && !o1.contains(".Integrated"))
                return 1;
            else
                return getFabricLocation(o1).compareTo(getFabricLocation(o2));
        }
    };

    /**
     * By templateId find referenced template. This template used as a source to lookup for
     * other components related to passed in "template".
     * Update might include hiding/showing/changing settings depending on linked related components.
     *
     * @param templateId Use template DAO as a source for referenced template
     * @param template  Template with component to update. Usually has 1 component only, but not limited.
     * @throws WebApplicationException
     */
    @Override
    public ServiceTemplate updatedTemplateComponentsByTemplate(String templateId,
                                                     ServiceTemplate template) throws WebApplicationException {
        return updatedTemplateComponents(templateId, null, template);
    }

    /**
     * By serviceId find referenced template. This template used as a source to lookup for
     * other components related to passed in "template".
     * Update might include hiding/showing/changing settings depending on linked related components.
     *
     * @param serviceId Use Deploymenmt DAO as a source for referenced template
     * @param template  Template with component to update. Usually has 1 component only, but not limited.
     * @throws WebApplicationException
     */
    @Override
    public ServiceTemplate updatedTemplateComponentsByService(String serviceId,
                                                               ServiceTemplate template) throws WebApplicationException {
        return updatedTemplateComponents(null, serviceId, template);
    }

    @Override
    public Response deleteUsers(@ApiParam("Valid UserIds") List<String> userIds) throws WebApplicationException {
        if (userIds == null || userIds.size() <= 0) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                    AsmManagerMessages.invalidParam("userIds"));
        }
        Set<String> userIdsSet = new HashSet<>(userIds);
        List<ServiceTemplateEntity> templateEntities = templateDao.getTemplatesForUserIds(userIdsSet);
        if (templateEntities != null && templateEntities.size() > 0) {
            for (ServiceTemplateEntity entity : templateEntities) {
                if (entity.getAssignedUserList() != null && entity.getAssignedUserList().size() > 0) {
                    boolean updateEntity = false;
                    Set<TemplateUserRefEntity> updatedUsers = new HashSet<>();
                    for (TemplateUserRefEntity user : entity.getAssignedUserList()) {
                        if (userIdsSet.contains(Long.toString(user.getUserId()))) {
                            updateEntity = true;
                        } else {
                            updatedUsers.add(user);
                        }
                    }
                    if (updateEntity) {
                        entity.setAssignedUserList(updatedUsers);
                        try {
                            templateDao.updateTemplate(entity);
                        } catch (LocalizedWebApplicationException e) {
                            LOGGER.error(
                                    "LocalizedWebApplicationException while updating service template "
                                            + entity.getName() + " users", e);
                            throw e;
                        } catch (Exception e) {
                            LOGGER.error("Exception while updating service template "
                                    + entity.getName() + " users", e);
                            throw new LocalizedWebApplicationException(
                                    Response.Status.INTERNAL_SERVER_ERROR,
                                    AsmManagerMessages.internalError());
                        }
                    }
                }
            }
        }
        return Response.noContent().build();
    }

    /**
     * By templateId or serviceId find referenced template. This template used as a source to lookup for
     * other components related to passed in "template".
     * Update might include hiding/showing/changing settings depending on linked related components.
     *
     * @param templateId Use template DAO as a source for referenced template
     * @param serviceId  Use deployment DAO as a source for referenced template
     * @param template  Template with component to update. Usually has 1 component only, but not limited.
     * @throws WebApplicationException
     */
    private ServiceTemplate updatedTemplateComponents(String templateId,
                                              String serviceId,
                                              ServiceTemplate template) throws WebApplicationException {

        // find referenced template.

        ServiceTemplate refTmpl = null;
        if (StringUtils.isNotEmpty(templateId)) {
            refTmpl = getTemplate(templateId, false);
        } else if (StringUtils.isNotEmpty(serviceId)) {
            Deployment deployment = ProxyUtil.getDeploymentProxy().getDeployment(serviceId);
            if (deployment != null) {
                refTmpl = deployment.getServiceTemplate();
            }
        }

        if (refTmpl == null)
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.noServiceTemplateFoundForDeployment());

        ServiceTemplateUtil.refineComponents(refTmpl, template, null); // null means update all components
        return template;
    }

    /**
     * Update the associated AddOnModules on the template
     * @param serviceTemplateDTO - The marshalled service template object
     * @param serviceTemplateEntity - The Service Template Entity being updated.
     */
    public static void updateAddOnModulesOnTemplate(List<AddOnModuleComponentEntity> addOnModuleComponentEntities, ServiceTemplate serviceTemplateDTO, ServiceTemplateEntity serviceTemplateEntity) {
        //remove all addOnModules to clean up the list.
        Map<String, AddOnModuleEntity> currentModuleMap = new HashMap<>();
        for (AddOnModuleEntity addOnModuleEntity : serviceTemplateEntity.getAddOnModules()) {
            currentModuleMap.put(addOnModuleEntity.getName(),addOnModuleEntity);
        }
        Set<AddOnModuleEntity> updatedEntites = new HashSet<>();
        for (ServiceTemplateComponent component : serviceTemplateDTO.getComponents()) {
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
        serviceTemplateEntity.getAddOnModules().clear();
        serviceTemplateEntity.getAddOnModules().addAll(updatedEntites);
    }

    /**
     * Parses the ServiceTemplate and adds a ServiceTemplateComponent to the service template in the
     * configuration field for configuration purposes.
     *
     * @param serviceTemplate service template to parse and add configuration
     */
    public void addServiceTemplateConfiguration(ServiceTemplate serviceTemplate) {
        if (serviceTemplate != null) {

            serviceTemplate.setTemplateType(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM);

            // Get networks for this appliance
            Map<NetworkType, Set<ConfigureTemplateOption>> networkTypesMap = ConfigureTemplateUtil.getNetworksMap(networkService);

            //Get OS Images and RepoNames
            final List<RazorRepo> razorImages = osRepositoryUtil.getRazorOSImages(false);
            final Map<String, String> repoNames = osRepositoryUtil.mapRazorRepoNamesToAsmRepoNames();

            //Get vcenter and hyperv entities
            final List<DeviceInventoryEntity> vCenterEntities = serviceTemplateUtil.getVCenterEntities(false);
            final List<DeviceInventoryEntity> hypervEntities = serviceTemplateUtil.getHypervEntities();

            // get server pools
            List<DeviceGroupEntity> deviceGroups = null;
            try {
                deviceGroups = deviceGroupDAO.getAllDeviceGroup(null, null, null);
            } catch (AsmManagerCheckedException e) {
                LOGGER.error("Unable to get device groups", e);
            }

            // get Storage Devices
            final Map<String, List<DeviceInventoryEntity>> storageDeviceMap = serviceTemplateUtil.getStorageDevicesMap();

            ConfigureTemplateUtil.parseServiceTemplate(serviceTemplate,
                                                       networkTypesMap,
                                                       razorImages,
                                                       repoNames,
                                                       vCenterEntities,
                                                       hypervEntities,
                                                       deviceGroups,
                                                       storageDeviceMap);
        }
    }

    public void applyServiceTemplateConfiguration(ServiceTemplate serviceTemplate) {
        if (serviceTemplate != null && serviceTemplate.getConfiguration() != null) {
            final ConfigureTemplate configuration = serviceTemplate.getConfiguration();
            final Map<String, ConfigureTemplateCategory> categoryMap = configuration.getCategoriesMap();

            Map<String, String> networkIdsMap = new HashMap<>();
            parseConfigurationValueMap(categoryMap, ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_NETWORKING_RESOURCE, networkIdsMap);

            Map<String, String> osRepositoryIdsMap = new HashMap<>();
            parseConfigurationValueMap(categoryMap, ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_RESOURCE, osRepositoryIdsMap);

            Map<String, String> clusterIdsMap = new HashMap<>();
            parseConfigurationValueMap(categoryMap, ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_CLUSTER_RESOURCE, clusterIdsMap);

            Map<String, String> serverPoolIdsMap = new HashMap<>();
            parseConfigurationValueMap(categoryMap, ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_SERVER_POOL_RESOURCE, serverPoolIdsMap);

            Map<String, String> storageIdsMap = new HashMap<>();
            parseConfigurationValueMap(categoryMap, ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_STORAGE_RESOURCE, storageIdsMap);

            String osAdministatorPassword = null;
            ConfigureTemplateCategory osPasswordCategory = categoryMap.get(ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_PASSWORD_RESOURCE);
            if (osPasswordCategory != null) {
                for (ConfigureTemplateSetting setting : osPasswordCategory.getParameters()) {
                    if (ConfigureTemplateSettingIDs.CONFIGURE_TEMPLATE_OS_ADMINISTRATOR_PASSWORD.equals(setting.getId())) {
                        osAdministatorPassword = setting.getValue();
                    }
                }
            }

            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                String componentId = component.getComponentID() != null ? component.getComponentID() : component.getId();
                switch (component.getType()) {
                case SERVER:
                    for (ServiceTemplateCategory serverCategory : component.getResources()) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE.equals(serverCategory.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE.equals(serverCategory.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(serverCategory.getId())) {
                            for (ServiceTemplateSetting serverSetting : serverCategory.getParameters()) {
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID.equals(serverSetting.getId())) {
                                    // Check for server pool setting and update value
                                    updateConfigurationSettingValue(serverSetting, serverPoolIdsMap);
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(serverSetting.getId())) {
                                    // Check for networking configuration and update network values
                                    if (StringUtils.isNotBlank(serverSetting.getValue())) {
                                        com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfiguration = ServiceTemplateUtil.deserializeNetwork(serverSetting.getValue());
                                        if (networkConfiguration != null) {
                                            for (Fabric fabric : networkConfiguration.getInterfaces()) {
                                                for (Interface interfaces : fabric.getInterfaces()) {
                                                    for (Partition partition : interfaces.getPartitions()) {
                                                        if (partition.getNetworks() != null) {
                                                            Set<String> networkIds = new HashSet<>();
                                                            for (String networkId : partition.getNetworks()) {
                                                                String newNetworkId = networkIdsMap.get(networkId);
                                                                if (newNetworkId != null) {
                                                                    networkIds.add(newNetworkId);
                                                                }
                                                            }
                                                            partition.setNetworkObjects(null);
                                                            partition.setNetworks(new ArrayList<>(networkIds));
                                                        }
                                                    }
                                                }
                                            }
                                            serverSetting.setValue(ServiceTemplateUtil.serializeNetwork(networkConfiguration));
                                        }
                                    }
                                    serverSetting.setNetworkConfiguration(null);
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals(serverSetting.getId())) {
                                    // Check for hypervisor network setting and update network values
                                    updateNetworkConfigurationSetting(serverSetting, networkIdsMap);
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID.equals(serverSetting.getId())) {
                                    // Check for OS Image and update value
                                    updateConfigurationSettingValue(serverSetting, osRepositoryIdsMap);
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID.equals(serverSetting.getId()) ||
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID.equals(serverSetting.getId())) {
                                    //check for OS Admin Password and update value
                                    updateConfigurationSettingValue(serverSetting, osAdministatorPassword);
                                }
                            }
                        }
                    }
                    break;
                case VIRTUALMACHINE:
                    for (ServiceTemplateCategory category : component.getResources()) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE.equals(category.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE.equals(category.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE.equals(category.getId())) {
                            for (ServiceTemplateSetting setting : category.getParameters()) {
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_OS_IMAGE_ID.equals(setting.getId())) {
                                    //Check for os image setting and update value
                                    updateConfigurationSettingValue(setting, osRepositoryIdsMap);
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID.equals(setting.getId()) ||
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_CONFIRM_PASSWORD_ID.equals(setting.getId())) {
                                    //check for OS Admin Password and update value
                                    updateConfigurationSettingValue(setting, osAdministatorPassword);
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID.equals(setting.getId())) {
                                    updateNetworkConfigurationSetting(setting, networkIdsMap);
                                }
                            }
                        }
                    }
                    break;
                case STORAGE:
                    switch (componentId) {
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID:
                        ServiceTemplateSetting compellentStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (compellentStorageSetting != null && StringUtils.isNotBlank(compellentStorageSetting.getValue())) {
                            updateConfigurationSettingValue(compellentStorageSetting, storageIdsMap);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_EQL_COMP_ID:
                        ServiceTemplateSetting equallogicSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (equallogicSetting != null && StringUtils.isNotBlank(equallogicSetting.getValue())) {
                            updateConfigurationSettingValue(equallogicSetting, storageIdsMap);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_NETAPP_COMP_ID:
                        ServiceTemplateSetting netappStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_NETAPP_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (netappStorageSetting != null && StringUtils.isNotBlank(netappStorageSetting.getValue())) {
                            updateConfigurationSettingValue(netappStorageSetting, storageIdsMap);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VNX_COMP_ID:
                        ServiceTemplateSetting vnxStorageSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_VNX_VOLUME_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (vnxStorageSetting != null && StringUtils.isNotBlank(vnxStorageSetting.getValue())) {
                            updateConfigurationSettingValue(vnxStorageSetting, storageIdsMap);
                        }
                        break;
                    default:
                        break;
                    }
                    break;
                case CLUSTER:
                    ServiceTemplateSetting clusterSetting = null;
                    switch (componentId) {
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID:
                        clusterSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                                                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (clusterSetting != null) {
                            updateConfigurationSettingValue(clusterSetting, clusterIdsMap);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID:
                        clusterSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID,
                                                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                        if (clusterSetting != null) {
                            updateConfigurationSettingValue(clusterSetting, clusterIdsMap);
                        }
                        break;
                    default:
                        break;
                    }
                    break;
                }
            }
            serviceTemplate.setConfiguration(null);
        }
    }

    private void parseConfigurationValueMap(Map<String,ConfigureTemplateCategory> categoryMap, String categoryId, Map<String, String> valuesMap) {
        if (categoryMap != null) {
            ConfigureTemplateCategory category = categoryMap.get(categoryId);
            if (category != null) {
                if (category.getParameters() != null && category.getParameters().size() > 0) {
                    for (ConfigureTemplateSetting setting : category.getParameters()) {
                        if (StringUtils.isNotBlank(setting.getValue())) {
                            valuesMap.put(setting.getId(), setting.getValue());
                        }
                    }
                }
            }
        }
    }

    private void updateConfigurationSettingValue(ServiceTemplateSetting setting, Map<String, String> valuesMap) {
        if (setting != null) {
            String newValue = valuesMap.get(setting.getValue());
            if (newValue != null) {
                setting.setValue(newValue);
            }
        }
    }

    private void updateConfigurationSettingValue(ServiceTemplateSetting setting, String newValue) {
        if (setting != null) {
            if (newValue != null) {
                setting.setValue(newValue);
            }
        }
    }

    private void updateNetworkConfigurationSetting(ServiceTemplateSetting setting, Map<String, String> valuesMap) {
        Set<String> networkIds = new HashSet<>();
        if (setting != null) {
            if ( setting.getNetworks() != null) {
                for (Network network : setting.getNetworks()) {
                    String newNetworkId = valuesMap.get(network.getId());
                    if (newNetworkId != null) {
                        networkIds.add(newNetworkId);
                    }
                }
            } else if (setting.getValue() != null) {
                Set<String> networkIdsSet = new HashSet<>(Arrays.asList(setting.getValue().split(",")));
                for (String networkId : networkIdsSet) {
                    String newNetworkId = valuesMap.get(networkId);
                    if (newNetworkId != null) {
                        networkIds.add(newNetworkId);
                    }
                }
            }
        }
        setting.setNetworks(null);
        setting.setValue(Joiner.on(",").skipNulls().join(networkIds));
    }

    /**
     * The cloneTemplate() method is designed to create a new template from a service template,
     * this includes any attachments on the service template passed in.
     * @param serviceTemplate
     * @return
     * @throws WebApplicationException
     */
    @Override
    public ServiceTemplate cloneTemplate(ServiceTemplate serviceTemplate) throws WebApplicationException {

        LOGGER.debug("Clone Template Entered for template: " + serviceTemplate.getTemplateName());
        ServiceTemplate createdTemplate = null;
        if (serviceTemplate != null) {
            final String originalId = serviceTemplate.getId();
            boolean clone = false;
            if (originalId != null) {
                ServiceTemplateEntity originalTemplateEntity = templateDao.getTemplateById(originalId);
                if (originalTemplateEntity != null) {
                    clone = true;
                }
            }
            //clear out original service template id
            serviceTemplate.setId(null);
            if (clone) {
                serviceTemplateUtil.decryptPasswords(serviceTemplate);
            }
            try {
                // create the new service template
                createdTemplate = createTemplate(serviceTemplate);

                if (clone) {
                    ServiceTemplateUtil.copyAttachments(originalId, createdTemplate.getId());
                } else {
                    ServiceTemplateUtil.moveAttachments(originalId, createdTemplate.getId());
                }
            } catch (WebApplicationException wae) {
                LOGGER.error("An exception was thrown during clone template", wae);
                if (createdTemplate != null && createdTemplate.getId() != null) {
                    deleteTemplate(createdTemplate.getId());
                }
                throw wae;
            } catch (Exception e) {
                if (createdTemplate != null && createdTemplate.getId() != null) {
                    deleteTemplate(createdTemplate.getId());
                }
                LOGGER.error("Exception while cloning service template "
                                     + serviceTemplate.getTemplateName(), e);
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.internalError());
            }
        }
        LOGGER.debug("Clone Template Finished for template: " + serviceTemplate.getTemplateName());
        return createdTemplate;
    }
}
