package com.dell.asm.asmcore.asmmanager.app.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.addonmodule.AddOnModule;
import com.dell.asm.asmcore.asmmanager.client.addonmodule.AddOnModuleComponentType;
import com.dell.asm.asmcore.asmmanager.client.addonmodule.AddOnModuleValidation;
import com.dell.asm.asmcore.asmmanager.client.addonmodule.IAddOnModuleService;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ApplicationModule;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ModuleRequirement;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.OsReleaseInfo;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleOperatingSystemVersionDAO;
import com.dell.asm.asmcore.asmmanager.db.OperatingSystemVersionDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleOperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.OperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;

@Path("/addOnModule")
public class AddOnModuleService implements IAddOnModuleService {

    private static final Logger logger = Logger.getLogger(AddOnModuleService.class);

    private static final String ADDON_MODULES_PATH = "/opt/Dell/ASM/modules";
    private static final String ADDON_MODULE_CREATE_SCRIPT_PATH = "/opt/Dell/scripts/add_on_module_create.sh";
    private static final String ADDON_MODULE_REMOVE_SCRIPT_PATH = "/opt/Dell/scripts/add_on_module_remove.sh";

    public static final String ASM_INPUT_CONFIG_FILE_NAME = "asm_input.json";
    public static final String METADATA_CONFIG_FILE_NAME = "metadata.json";

    private static final Set<String> validSortColumns = new HashSet<>();
    static {
        validSortColumns.add("uploadedDate");
        validSortColumns.add("uploadedBy");
        validSortColumns.add("active");
        validSortColumns.add("version");
        validSortColumns.add("filePath");
        validSortColumns.add("fileName");
        validSortColumns.add("description");
        validSortColumns.add("name");
    }

    public static final Set<String> validFilterColumns = new HashSet<>();
    static {
        validFilterColumns.add("uploadedDate");
        validFilterColumns.add("uploadedBy");
        validFilterColumns.add("active");
        validFilterColumns.add("version");
        validFilterColumns.add("filePath");
        validFilterColumns.add("fileName");
        validFilterColumns.add("description");
        validFilterColumns.add("name");
    }

    public  static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        final AnnotationIntrospector ai = new JaxbAnnotationIntrospector(OBJECT_MAPPER.getTypeFactory());
        OBJECT_MAPPER.setAnnotationIntrospector(ai);
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    } 
    
    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    private AddOnModuleDAO addOnModuleDAO;
    private AddOnModuleComponentsDAO addOnModuleComponentsDAO;
    private OperatingSystemVersionDAO operatingSystemVersionDAO;
    private AddOnModuleOperatingSystemVersionDAO addOnModuleOperatingSystemVersionDAO;

    private DeploymentService deploymentService;
    private ServiceTemplateService serviceTemplateService;

    public AddOnModuleService() {
    }

    @Override
    public AddOnModule createAddOnModule(final AddOnModule createAddOnModule) {
        final URL addOnModuleUrl = createAddOnModule.getUploadUrl();
        if (addOnModuleUrl == null) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                    AsmManagerMessages.addOnModuleUploadErrorOrInvalidFormat());
        }
        // create add on module
        ApplicationModule applicationModule = null;
        String addOnModuleName = null;
        File addOnModulePath = null;
        File addOnModuleTmpFile = null;
        String asmInputHash = null;
        String metadatHash = null;
        try { 
            // NOTE[fcarta] - We need to ensure we clean up this tmp file from they file system
            addOnModuleTmpFile = copyStreamToTmpFile(addOnModuleUrl.openStream());
            
            final ZipFile zipFile = new ZipFile(addOnModuleTmpFile);
            final ZipEntry asmInputZipEntry = zipFile.getEntry(ASM_INPUT_CONFIG_FILE_NAME);
            if (asmInputZipEntry == null) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleUploadMissingRequiredFile(ASM_INPUT_CONFIG_FILE_NAME));
            }
            InputStream zipFileInputStream = null;
            byte[] bytes = null;
            try {
                zipFileInputStream = zipFile.getInputStream(asmInputZipEntry);
                bytes = IOUtils.toByteArray(zipFileInputStream);
                if (bytes != null && bytes.length > 0) {
                    asmInputHash = DigestUtils.md5Hex(bytes);
                }
            }
            finally {
                if (null != zipFileInputStream) {
                    zipFileInputStream.close();
                }
            }
            applicationModule = parseJsonInputFile(bytes);
            
            // first check for any metadata
            final ZipEntry metadataZipEntry = zipFile.getEntry(METADATA_CONFIG_FILE_NAME);
            if (metadataZipEntry != null) {
                zipFileInputStream = null;
                try {
                    zipFileInputStream = zipFile.getInputStream(metadataZipEntry);
                    bytes = IOUtils.toByteArray(zipFileInputStream);
                    if (bytes != null && bytes.length > 0) {
                        metadatHash = DigestUtils.md5Hex(zipFileInputStream);
                    }
                } finally {
                    if (null != zipFileInputStream) {
                        zipFileInputStream.close();
                    }
                }
                // first read in the metadata file if present and populate any data here
                final ApplicationModule metadataApplicationModule = 
                        parseJsonInputFile(bytes);
                // copy over any blank or empty fields
                applicationModule.merge(metadataApplicationModule);
            }

            // validate required configuration fields are present in metadata or asm_input files
            validateApplicationModule(applicationModule, false);

            addOnModuleName = parseAddOnModuleName(applicationModule.getName());
            // make sure module doesnt already exist and that the name is unique
            if (moduleExists(addOnModuleName)) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleAlreadyExists(addOnModuleName));
            }
            try {
                // move puppet module files to permanent location
                addOnModulePath = extractToAddOnModuleLocation(zipFile, addOnModuleName);
            } catch (Exception e) {
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                        AsmManagerMessages.internalError());
            } finally {
                if (null != zipFile) {
                    zipFile.close();
                }
            }
        } catch (IOException ioe) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                    AsmManagerMessages.addOnModuleUploadErrorOrInvalidFormat());
        } finally {
            // ensure to clean up tmp file resources
            if (addOnModuleTmpFile != null) {
                try {
                    Files.delete(addOnModuleTmpFile.toPath());
                } catch (IOException ioe) {
                    logger.error("Unable to delete tmp file upload for AddOnModule ", ioe);
                }
            }
        }

        AddOnModuleEntity addOnModuleEntity = createAddOnModuleEntity(applicationModule,addOnModuleName,addOnModulePath.getAbsolutePath(),false, asmInputHash, metadatHash);

        return entityToView(addOnModuleEntity);

    }

    @Override
    public AddOnModule getAddOnModule(String addOnModuleId) {
        final AddOnModuleEntity addOnModuleEntity = getAddOnModuleDAO().get(addOnModuleId);
        if (addOnModuleEntity == null) {
            EEMILocalizableMessage msg = AsmManagerMessages.invalidAddOnModuleId(addOnModuleId);
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, msg);
        }
        return entityToView(addOnModuleEntity);
    }

    @Override
    public List<AddOnModule> getAddOnModules(String sort, List<String> filter, Integer offset, Integer limit) {

        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        //There is not an active column in the database, therefore we have to fake out active sort
        boolean activeSort = false;
        int index = 0;
        SortParamParser.SortInfo activeSortInfo = null;
        for (SortParamParser.SortInfo info : sortInfos) {
            //find sort for active and save the sort info
            if (info.getColumnName().equals("active")) {
                activeSort = true;
                activeSortInfo  = info;
                break;
            }
            index++;
        }
        //remove it from the list
        if (activeSort) {
            sortInfos.remove(index);
        }

        // Build filter list from filter params ( comprehensive )
        FilterParamParser filterParser = new FilterParamParser(filter, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        int pageOffSet;
        if (offset == null) {
            pageOffSet = 0;
        } else {
            pageOffSet = offset;
        }

        int pageLimit;
        if (limit == null) {
            pageLimit = 20;
        } else {
            pageLimit = limit;
        }

        List<AddOnModuleEntity> addOnModuleEntities = getAddOnModuleDAO().getAllAddOnModules(sortInfos,filterInfos);

        //artificially sort by active due to no db column
        if (activeSort) {
            List<AddOnModuleEntity> active = new ArrayList<>();
            List<AddOnModuleEntity> inactive = new ArrayList<>();
            for (AddOnModuleEntity entity : addOnModuleEntities) {
                if (entity.isActive()) {
                    active.add(entity);
                } else {
                    inactive.add(entity);
                }
            }
            addOnModuleEntities = new ArrayList<>();
            if (activeSortInfo.getSortOrder().equals(SortParamParser.SortOrder.DESC)) {
                addOnModuleEntities.addAll(active);
                addOnModuleEntities.addAll(inactive);
            } else {
                addOnModuleEntities.addAll(inactive);
                addOnModuleEntities.addAll(active);
            }
        }

        index = 0;
        int count = 0;
        int totalRecords = 0;
        final List<AddOnModule> addOnModules = new ArrayList<AddOnModule>();
        for (AddOnModuleEntity addOnModuleEntity : addOnModuleEntities) {
            if (!addOnModuleEntity.getDefaultModule()) {
                if (pageOffSet <= index && count < pageLimit) {
                    addOnModules.add(entityToView(addOnModuleEntity));
                    count++;
                }
                index++;
                totalRecords++;
            }
        }

        logger.debug("Get All AddOnModules Done. Count=" + totalRecords);
        servletResponse.setHeader(AsmConstants.DELL_TOTAL_COUNT_HEADER,
                String.valueOf(totalRecords));
        return addOnModules;
    }

    @Override
    public Response deleteAddOnModule(String addOnModuleId) {
        final AddOnModuleEntity addOnModuleEntity = getAddOnModuleDAO().getComplete(addOnModuleId);
        if (addOnModuleEntity == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND,
                    AsmManagerMessages.invalidAddOnModuleId(addOnModuleId));
        }
        if (CollectionUtils.isNotEmpty(addOnModuleEntity.getDeploymentEntities()) ||
                CollectionUtils.isNotEmpty(addOnModuleEntity.getServiceTemplateEntities())) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND,
                                                       AsmManagerMessages.addOnModuleInUse());
        }

        try {
            getAddOnModuleDAO().delete(addOnModuleId);
            removeFromAddOnModuleLocation(addOnModuleEntity.getName());
        } catch (Exception e) {
            logger.error("Exception while deleting add on module " + addOnModuleId, e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        return Response.noContent().build();
    }

    public void validateApplicationModule(final ApplicationModule applicationModule, final boolean update) throws LocalizedWebApplicationException {
        final AddOnModuleValidation.Builder addOnModuleValidation = new AddOnModuleValidation.Builder();

        // first check all required fields
        if (StringUtils.isBlank(applicationModule.getName())) {
            addOnModuleValidation.missingRequiredField("name");
        } else if (!applicationModule.getName().matches("^[\\w\\- ]+$")) {
            // name contains a character other than Alphanumeric, -, _, and space
            addOnModuleValidation.invalidFieldValue("name");
        }

        if (StringUtils.isBlank(applicationModule.getVersion())) {
            addOnModuleValidation.missingRequiredField("version");
        }
        if (CollectionUtils.isEmpty(applicationModule.getOperatingSystemSupport())) {
            addOnModuleValidation.missingRequiredField("operatingSystemSupport");
        }
        if (CollectionUtils.isEmpty(applicationModule.getRequirements())) {
            addOnModuleValidation.missingRequiredField("requirements");
        }
        if (CollectionUtils.isEmpty(applicationModule.getClasses()) && CollectionUtils.isEmpty(applicationModule.getTypes())) {
            addOnModuleValidation.missingRequiredField("classes or types");
        }

        // check validation on operating system support there must be at least one
        // each must have operating system name and version or versions
        for (final OsReleaseInfo osReleaseInfo : applicationModule.getOperatingSystemSupport()) {
            if (StringUtils.isBlank(osReleaseInfo.getOperatingSystem())) {
                addOnModuleValidation.missingRequiredField("operatingSystemSupport");
            }
            if (CollectionUtils.isEmpty(osReleaseInfo.getReleaseVersions())) {
                addOnModuleValidation.missingRequiredField("releaseVersions");
            }
            for (final String releaseVersion : osReleaseInfo.getReleaseVersions()) {
                if (StringUtils.isBlank(releaseVersion)) {
                    addOnModuleValidation.invalidFieldValue("releaseVersions");
                }
            }
        }

        // check that classes and types each have at least one resource
        for (final ServiceTemplateComponent puppetClass : applicationModule.getClasses()) {
            if (CollectionUtils.isEmpty(puppetClass.getResources())) {
                addOnModuleValidation.missingRequiredField("classes:components:resources");
            }
        }
        for (final ServiceTemplateComponent puppetType : applicationModule.getTypes()) {
            if (CollectionUtils.isEmpty(puppetType.getResources())) {
                addOnModuleValidation.missingRequiredField("types:components:resources");
            }
        }

        // ensure puppet requirements are present
        boolean foundRequirementPe = false;
        boolean foundRequirementPuppet = false;
        for (final ModuleRequirement moduleRequirement : applicationModule.getRequirements()) {
            if (moduleRequirement != null) {
                if (StringUtils.endsWithIgnoreCase("pe", moduleRequirement.getName())) {
                    foundRequirementPe = true;
                    try {
                        // since we found the pe version validate that it is currently supported
                        final Version supportedVersionPe = Version.valueOf(PuppetModuleUtil.CURRENT_SUPPORTED_PE_VERSION);
                        if (moduleRequirement.getVersionRequirement() == null || !supportedVersionPe.satisfies(moduleRequirement.getVersionRequirement())) {
                            // this module doesnt support current asm pe version
                            addOnModuleValidation.invalidVersionValue("requirements:name:pe:versionRequirement",
                                    moduleRequirement.getVersionRequirement());
                        }
                    } catch (IllegalArgumentException | ParseException e) {
                        // something is wrong with the format of the pe version
                        addOnModuleValidation.invalidFieldValue("requirements:name:pe:versionRequirement");
                    }
                    continue;
                }
                if (StringUtils.endsWithIgnoreCase("puppet", moduleRequirement.getName())) {
                    foundRequirementPuppet = true;
                    try {
                        // since we found the puppet version validate that it is currently supported
                        final Version supportedVersionPuppet =
                                Version.valueOf(PuppetModuleUtil.CURRENT_SUPPORTED_PUPPET_VERSION);
                        if (moduleRequirement.getVersionRequirement() == null || !supportedVersionPuppet.satisfies(moduleRequirement.getVersionRequirement())) {
                            // this module doesnt support current asm puppet version
                            addOnModuleValidation.invalidVersionValue("requirements:name:puppet:versionRequirement",
                                    moduleRequirement.getVersionRequirement());
                        }
                    } catch (IllegalArgumentException | ParseException e) {
                        // something is wrong with the format of the puppet version
                        addOnModuleValidation.invalidFieldValue("requirements:name:puppet:versionRequirement");
                    }
                    continue;
                }
            }
        }
        if (!foundRequirementPe) {
            addOnModuleValidation.missingRequiredFieldValue("requirements:name", "pe");
        }
        if (!foundRequirementPuppet) {
            addOnModuleValidation.missingRequiredFieldValue("requirements:name", "puppet");
        }

        // if validation for update then don't verify duplicate components
        if (!update) {
           List<AddOnModuleComponentEntity> addOnModuleComponentEntities = getAddOnModuleComponentsDAO().getAll(false);
            Set<String> componentNames = new HashSet<>();
            Set<String> componentIds = new HashSet<>();
            for (AddOnModuleComponentEntity entity : addOnModuleComponentEntities) {
                ServiceTemplateComponent component = MarshalUtil.unmarshal(ServiceTemplateComponent.class, entity.getMarshalledData());
                if (component != null) {
                    componentIds.add(component.getId());
                    componentNames.add(component.getName());
                }
            }
            for (ServiceTemplateComponent component : applicationModule.getClasses()) {
                if (componentNames.contains(component.getName())) {
                    addOnModuleValidation.duplicateComponentNames(component.getName());
                }
                if (componentIds.contains(component.getId())) {
                    addOnModuleValidation.duplicateComponentIds(component.getId());
                }
            }
            for (ServiceTemplateComponent component : applicationModule.getTypes()) {
                if (componentNames.contains(component.getName())) {
                    addOnModuleValidation.duplicateComponentNames(component.getName());
                }
                if (componentIds.contains(component.getId())) {
                    addOnModuleValidation.duplicateComponentIds(component.getId());
                }
            }
        }

        AddOnModuleValidation validation =  addOnModuleValidation.build();

        if (validation.hasValidationErrors()) {
            if (CollectionUtils.isNotEmpty(validation.getMissingRequiredFields())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleUploadMissingRequiredFields(
                                validation.getMissingRequiredFields().toArray(
                                        new String[validation.getMissingRequiredFields().size()])));
            }
            if (CollectionUtils.isNotEmpty(validation.getInvalidFieldValues())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleUploadInvalidFieldValues(
                                validation.getInvalidFieldValues().toArray(
                                        new String[validation.getInvalidFieldValues().size()])));
            }
            if (CollectionUtils.isNotEmpty(validation.getMissingRequiredFieldValues())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleUploadMissingRequiredFieldValues(
                                validation.getMissingRequiredFieldValues().toArray(
                                        new String[validation.getMissingRequiredFieldValues().size()])));
            }
            if (CollectionUtils.isNotEmpty(validation.getInvalidVersionValues())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleUnsupportedVersionRequirements(
                                validation.getInvalidVersionValues().toArray(
                                        new String[validation.getInvalidVersionValues().size()])));
            }
            if (CollectionUtils.isNotEmpty(validation.getDuplicateComponentNames())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleComponentAlreadyExists(
                                validation.getDuplicateComponentNames().toArray(
                                        new String[validation.getDuplicateComponentNames().size()])));
            }
            if (CollectionUtils.isNotEmpty(validation.getDuplicateComponentIds())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.addOnModuleComponentAlreadyExists(
                                validation.getDuplicateComponentIds().toArray(
                                        new String[validation.getDuplicateComponentIds().size()])));
            }


            // unknown validation error
            logger.error("Unknown add on module validation error!");
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

    }

    public AddOnModuleEntity createAddOnModuleEntity(ApplicationModule applicationModule,
                                                     String addOnModuleName,
                                                     String addOnModulePath,
                                                     boolean defaultModule,
                                                     String asmInputHash,
                                                     String metadataHash) throws LocalizedWebApplicationException {
        try {
            // create add on module
            AddOnModuleEntity addOnModuleEntity = new AddOnModuleEntity();
            addOnModuleEntity.setName(addOnModuleName);
            addOnModuleEntity.setDescription(applicationModule.getDescription());
            addOnModuleEntity.setModulePath(addOnModulePath);
            addOnModuleEntity.setVersion(applicationModule.getVersion());
            addOnModuleEntity.setDefaultModule(defaultModule);
            addOnModuleEntity.setAsmInputHash(asmInputHash);
            addOnModuleEntity.setMetadataHash(metadataHash);

            addAddOnModuleComponents(addOnModuleEntity,applicationModule);

            addOnModuleEntity = getAddOnModuleDAO().create(addOnModuleEntity);

            addOperatingSystemSystemVersions(addOnModuleEntity, applicationModule);

            // refresh add on module
            addOnModuleEntity = getAddOnModuleDAO().get(addOnModuleEntity.getId());

            return addOnModuleEntity;
        } catch (AsmManagerCheckedException e) {
            logger.error("Exception while creating add on module.", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
    }

    public void updateAddOnModuleEntity(final String id,
                                        final ApplicationModule applicationModule,
                                        final String addOnModulePath,
                                        final String asmInputHash,
                                        final String metadataHash) {
        if (id == null || applicationModule == null) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                       AsmManagerMessages.addOnModuleUploadErrorOrInvalidFormat());
        }
        try {
            AddOnModuleEntity addOnModuleEntity = getAddOnModuleDAO().get(id);
            if (addOnModuleEntity != null) {
                addOnModuleEntity.setName(parseAddOnModuleName(applicationModule.getName()));
                addOnModuleEntity.setDescription(applicationModule.getDescription());
                addOnModuleEntity.setVersion(applicationModule.getVersion());
                addOnModuleEntity.setAsmInputHash(asmInputHash);
                addOnModuleEntity.setMetadataHash(metadataHash);

                addOnModuleEntity.getAddOnModuleComponents().clear();
                addAddOnModuleComponents(addOnModuleEntity, applicationModule);
                getAddOnModuleDAO().update(addOnModuleEntity);

                Set<AddOnModuleOperatingSystemVersionEntity> addOnOSVersionSet = new HashSet<>(getAddOnModuleOperatingSystemVersionDAO().findByAddOnModule(addOnModuleEntity.getId()));
                if (addOnOSVersionSet != null && addOnOSVersionSet.size() > 0) {
                    Map<AddOnModuleOperatingSystemVersionEntity.AddOnModuleOperatingSystemVersionId, AddOnModuleOperatingSystemVersionEntity> osVersionMap = new HashMap<>();
                    for (AddOnModuleOperatingSystemVersionEntity entity : addOnOSVersionSet) {
                        osVersionMap.put(entity.getAddOnModuleOperatingSystemVersionId(),entity);
                    }
                    HashSet<AddOnModuleOperatingSystemVersionEntity> currentSet = new HashSet<>();
                    for (final OsReleaseInfo osReleaseInfo : applicationModule.getOperatingSystemSupport()) {
                        final String operatingSystem = osReleaseInfo.getOperatingSystem();
                        for (final String releaseVersion : osReleaseInfo.getReleaseVersions()) {
                            OperatingSystemVersionEntity operatingSystemVersionEntity =
                                    getOperatingSystemVersionDAO().findByOsVersion(operatingSystem, releaseVersion);
                            if (operatingSystemVersionEntity == null) {
                                operatingSystemVersionEntity = createOperatingSystemEntity(operatingSystem,releaseVersion);
                            }
                            // create mapping from operating system version and add on module
                            final AddOnModuleOperatingSystemVersionEntity aomosve =
                                    new AddOnModuleOperatingSystemVersionEntity(addOnModuleEntity, operatingSystemVersionEntity);
                            AddOnModuleOperatingSystemVersionEntity currentEntity = osVersionMap.get(aomosve.getAddOnModuleOperatingSystemVersionId());
                            if (currentEntity == null) {
                                // create mapping from operating system version and add on module
                                getAddOnModuleOperatingSystemVersionDAO().saveOrUpdate(aomosve);
                            }
                            currentSet.add(aomosve);
                        }
                    }
                    for (AddOnModuleOperatingSystemVersionEntity entity : addOnOSVersionSet) {
                        if (!currentSet.contains(entity)) {
                            getAddOnModuleOperatingSystemVersionDAO().delete(entity);
                        }
                    }
                } else {
                    addOperatingSystemSystemVersions(addOnModuleEntity, applicationModule);
                }
            }

        } catch (Exception e) {
            logger.error("Exception while updating add on module.", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                                                       AsmManagerMessages.internalError());
        }
    }

    public static String parseAddOnModuleName(String name) {
        int index = name.indexOf("-");
        if (index != -1) {
            name = name.substring(index+1);
        }
        return name.trim();
    }


    private File copyStreamToTmpFile(final InputStream in) throws IOException {
        FileOutputStream fos = null;
        try {
            File tmpFile = File.createTempFile("stream-" + UUID.randomUUID().toString(), "tmp");
            // NOTE[fcarta] - this will only ensure the tmp file is cleaned under normal VM termination
            tmpFile.deleteOnExit();
            fos = new FileOutputStream(tmpFile);
            if (null != fos) {
                IOUtils.copy(in, fos);
                return tmpFile;
            }
            return null;
        } finally {
            if (null != fos) {
                fos.close();
            }
        }
    }
    
    /**
     * Parses the given input stream for name, version, and description fields to populate on the given addOnModule
     * @param in
     *
     * {
          "name": "asm-linux_postinstall",
          "version": "0.1.0",
          "operatingSystemSupport": [
            {
              "operatingSystem": "RedHat",
              "releaseVersions": [ "5", "6", "7" ]
            },
          ],
          "requirements": [
            {
              "name": "pe",
              "versionRequirement": ">= 3.0.0 < 2015.3.0"
            },
            {
              "name": "puppet",
              "versionRequirement": ">= 3.0.0 < 5.0.0"
            }
          ],
          "classes": [ ... ],
          "types": [ ... ]
        }
     * 
     * 
     */
    private ApplicationModule parseJsonInputFile(final byte[] in) {
        try {
            return OBJECT_MAPPER.readValue(new String(in), ApplicationModule.class);
        } catch (IOException e) {
            logger.equals("Error parsing json input file for add on module \n" + e.getMessage());
            throw new RuntimeException("Error parsing json input file for add on module");
        }
    }


    private boolean moduleExists(final String moduleName) {
        final java.nio.file.Path moduleLocation = Paths.get(ADDON_MODULES_PATH + File.separator + moduleName);
        return Files.exists(moduleLocation);
    }
    
    /**
     * Extracts the given zip file / add on module to the add on module location with the given module name
     * @param zipFile
     * @param moduleName
     * @throws IOException 
     */
    private File extractToAddOnModuleLocation(final ZipFile zipFile, final String moduleName) 
            throws Exception {
        final java.nio.file.Path moduleLocation = Paths.get(ADDON_MODULES_PATH + File.separator + moduleName);
        // make sure module doesnt exists already - currently we dont support upgrades so if upgrading then
        // module must be deleted first and then new version needs to be added 
        if(Files.exists(moduleLocation)) {
            throw new AsmRuntimeException(String.format("Module %s already exists!", moduleName), null);
        }
        executeAddOnModuleBashCommand(ADDON_MODULE_CREATE_SCRIPT_PATH, zipFile.getName(), moduleName);
        return moduleLocation.toFile();
    }
    
    /**
     * Removes the add on module directory/files for the given module name
     * @param moduleName
     */
    private void removeFromAddOnModuleLocation(final String moduleName) {
        final java.nio.file.Path moduleLocation = Paths.get(ADDON_MODULES_PATH + File.separator + moduleName);
        if(Files.exists(moduleLocation)) {
            executeAddOnModuleBashCommand(ADDON_MODULE_REMOVE_SCRIPT_PATH, moduleName);
        }
    }
        
    private void executeAddOnModuleBashCommand(final String script, final String... args) {
        // Creating command response object
        CommandResponse cmdResponse;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
        try {
            final List<String> command = new ArrayList<String>();
            command.add("sudo");
            command.add("-u");
            command.add("nobody");
            command.add(script);
            command.addAll(Arrays.asList(args));
            cmdResponse = cmdRunner.runCommandWithConsoleOutput(command.toArray(new String[command.size()]));
            logger.debug("Return code: " + cmdResponse.getReturnCode() 
                    + "\nReturn message: " + cmdResponse.getReturnMessage());
            if (!cmdResponse.getReturnCode().equalsIgnoreCase("0")) {
                throw new RuntimeException("Error while executing system commands for AddOnModule.");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to execute the Bash Script , Exception : " + ex);
        }
    }
    
    private AddOnModule entityToView(final AddOnModuleEntity addOnModuleEntity) {
        final AddOnModule addOnModule = new AddOnModule();
        addOnModule.setId(addOnModuleEntity.getId());
        addOnModule.setName(addOnModuleEntity.getName());
        addOnModule.setDescription(addOnModuleEntity.getDescription());
        addOnModule.setModulePath(addOnModuleEntity.getModulePath());
        addOnModule.setVersion(addOnModuleEntity.getVersion());
        addOnModule.setUploadedBy(addOnModuleEntity.getUploadedBy());
        addOnModule.setUploadedDate(addOnModuleEntity.getUploadedDate().getTime());
        addOnModule.setDefaultModule(addOnModuleEntity.getDefaultModule());
        addOnModule.setActive(false);
        if (addOnModuleEntity.isActive()) {
                addOnModule.setActive(true);
        }
        // loop through the mappings to get all the mapped os and versions
        final Map<String,List<String>> osVersions = new HashMap<String,List<String>>();
        for (final AddOnModuleOperatingSystemVersionEntity aomosv : 
                addOnModuleEntity.getAddOnModuleOperatingSystemVersions()) {
            final String osName = aomosv.getOperatingSystemVersion().getOperatingSystem();
            if (!osVersions.containsKey(osName)) {
                osVersions.put(osName, new ArrayList<String>());
            }
            osVersions.get(osName).add(aomosv.getOperatingSystemVersion().getVersion());
        }
        for (final Map.Entry<String, List<String>> osEntry : osVersions.entrySet()) {
            addOnModule.addOperatingSystemSupport(osEntry.getKey(), osEntry.getValue());
        }

        if (Hibernate.isInitialized(addOnModuleEntity.getAddOnModuleComponents())) {
            for (AddOnModuleComponentEntity entity : addOnModuleEntity.getAddOnModuleComponents()) {
                final ServiceTemplateComponent component = MarshalUtil.unmarshal(ServiceTemplateComponent.class, entity.getMarshalledData());
                switch (entity.getType()) {
                case TYPE:
                    component.setSubType(ServiceTemplateComponent.ServiceTemplateComponentSubType.TYPE);
                    addOnModule.getTypes().add(component);
                    break;
                case CLASS:
                    component.setSubType(ServiceTemplateComponent.ServiceTemplateComponentSubType.CLASS);
                    addOnModule.getTypes().add(component);
                    break;
                default:
                    //do nothing and ignore.
                }
            }
        }

        if (Hibernate.isInitialized(addOnModuleEntity.getServiceTemplateEntities())) {
            for (ServiceTemplateEntity entity : addOnModuleEntity.getServiceTemplateEntities()) {
                addOnModule.getTemplates().add(getServiceTemplateService().createTemplateDTO(entity, null, true));
            }
        }

        if (Hibernate.isInitialized(addOnModuleEntity.getDeploymentEntities())) {
            for (DeploymentEntity entity : addOnModuleEntity.getDeploymentEntities()) {
                addOnModule.getDeployments().add(getDeploymentService().entityToView(entity));
            }
        }
        return addOnModule;
    }

    private void addAddOnModuleComponents(AddOnModuleEntity addOnModuleEntity, ApplicationModule applicationModule) {
        //Parse Classes and add to components
        for (ServiceTemplateComponent component : applicationModule.getClasses()) {
            AddOnModuleComponentEntity entity = new AddOnModuleComponentEntity();
            entity.setAddOnModuleEntity(addOnModuleEntity);
            entity.setName(component.getName());
            entity.setType(AddOnModuleComponentType.CLASS);
            component.setSubType(ServiceTemplateComponent.ServiceTemplateComponentSubType.CLASS);
            entity.setMarshalledData(MarshalUtil.marshal(component));
            addOnModuleEntity.getAddOnModuleComponents().add(entity);
        }

        //Parse Types and add to types
        for (ServiceTemplateComponent component : applicationModule.getTypes()) {
            AddOnModuleComponentEntity entity = new AddOnModuleComponentEntity();
            entity.setAddOnModuleEntity(addOnModuleEntity);
            entity.setName(component.getName());
            entity.setType(AddOnModuleComponentType.TYPE);
            component.setSubType(ServiceTemplateComponent.ServiceTemplateComponentSubType.TYPE);
            entity.setMarshalledData(MarshalUtil.marshal(component));
            addOnModuleEntity.getAddOnModuleComponents().add(entity);
        }
    }

    private void addOperatingSystemSystemVersions(AddOnModuleEntity addOnModuleEntity, ApplicationModule applicationModule) throws AsmManagerCheckedException {
        // TODO[fcarta] should move this to a OperatingSystemSupportService
        // check to see if there are any operating systems support to save
        // not the best implementation but there should only be a handful of iterations and queries here
        for (final OsReleaseInfo osReleaseInfo : applicationModule.getOperatingSystemSupport()) {
            final String operatingSystem = osReleaseInfo.getOperatingSystem();
            for (final String releaseVersion : osReleaseInfo.getReleaseVersions()) {
                OperatingSystemVersionEntity operatingSystemVersionEntity =
                        getOperatingSystemVersionDAO().findByOsVersion(operatingSystem, releaseVersion);
                // if the operating system and version where not found then add
                if (operatingSystemVersionEntity == null) {
                    operatingSystemVersionEntity = createOperatingSystemEntity(operatingSystem,releaseVersion);
                }

                // create mapping from operating system version and add on module
                final AddOnModuleOperatingSystemVersionEntity aomosve =
                        new AddOnModuleOperatingSystemVersionEntity(addOnModuleEntity, operatingSystemVersionEntity);
                getAddOnModuleOperatingSystemVersionDAO().saveOrUpdate(aomosve);
            }
        }
    }

    private OperatingSystemVersionEntity createOperatingSystemEntity(final String operatingSystem, final String releaseVersion) throws AsmManagerCheckedException {
        OperatingSystemVersionEntity operatingSystemVersionEntity = null;
        if (operatingSystem != null && releaseVersion != null) {
            operatingSystemVersionEntity = new OperatingSystemVersionEntity();
            operatingSystemVersionEntity.setOperatingSystem(operatingSystem);
            operatingSystemVersionEntity.setVersion(releaseVersion);
            operatingSystemVersionEntity = getOperatingSystemVersionDAO().create(operatingSystemVersionEntity);
        }
        return operatingSystemVersionEntity;
    }

    public AddOnModuleDAO getAddOnModuleDAO() {
        if (addOnModuleDAO == null) {
            addOnModuleDAO = AddOnModuleDAO.getInstance();
        }
        return addOnModuleDAO;
    }

    public void setAddOnModuleDAO(AddOnModuleDAO addOnModuleDAO) {
        this.addOnModuleDAO = addOnModuleDAO;
    }

    public AddOnModuleComponentsDAO getAddOnModuleComponentsDAO() {
        if (addOnModuleComponentsDAO == null) {
            addOnModuleComponentsDAO = AddOnModuleComponentsDAO.getInstance();
        }
        return addOnModuleComponentsDAO;
    }

    public void setAddOnModuleComponentsDAO(AddOnModuleComponentsDAO addOnModuleComponentsDAO) {
        this.addOnModuleComponentsDAO = addOnModuleComponentsDAO;
    }

    public OperatingSystemVersionDAO getOperatingSystemVersionDAO() {
        if (operatingSystemVersionDAO == null) {
            operatingSystemVersionDAO = OperatingSystemVersionDAO.getInstance();
        }
        return operatingSystemVersionDAO;
    }

    public void setOperatingSystemVersionDAO(OperatingSystemVersionDAO operatingSystemVersionDAO) {
        this.operatingSystemVersionDAO = operatingSystemVersionDAO;
    }

    public AddOnModuleOperatingSystemVersionDAO getAddOnModuleOperatingSystemVersionDAO() {
        if (addOnModuleOperatingSystemVersionDAO == null) {
            addOnModuleOperatingSystemVersionDAO = AddOnModuleOperatingSystemVersionDAO.getInstance();
        }
        return addOnModuleOperatingSystemVersionDAO;
    }

    public void setAddOnModuleOperatingSystemVersionDAO(AddOnModuleOperatingSystemVersionDAO addOnModuleOperatingSystemVersionDAO) {
        this.addOnModuleOperatingSystemVersionDAO = addOnModuleOperatingSystemVersionDAO;
    }

    public DeploymentService getDeploymentService() {
        if (deploymentService == null) {
            deploymentService = new DeploymentService();
        }
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public ServiceTemplateService getServiceTemplateService() {
        if (serviceTemplateService == null) {
            serviceTemplateService = new ServiceTemplateService();
        }
        return serviceTemplateService;
    }

    public void setServiceTemplateService(ServiceTemplateService serviceTemplateService) {
        this.serviceTemplateService = serviceTemplateService;
    }

}
