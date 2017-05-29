package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.osrepository.IOSRepositoryService;
import com.dell.asm.asmcore.asmmanager.client.osrepository.OSRepository;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.OSRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.CreateOSRepoJob;
import com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.dell.pg.orion.security.encryption.IEncryptedString;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Hash;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/osRepository")
public class OSRepositoryService implements IOSRepositoryService{
    private static final Logger logger = Logger.getLogger(OSRepositoryService.class);
    private GenericDAO genericDAO;
    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();
    private final EncryptionDAO encryptionDAO = EncryptionDAO.getInstance();
    private ServiceTemplateDAO serviceTemplateDAO;
    private DeploymentDAO deploymentDAO;
    private OSRepositoryUtil osRepositoryUtil;


    private static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }

    public OSRepositoryService(GenericDAO genericDAO)
    {
        this.genericDAO = genericDAO;
    }

    public OSRepositoryService()
    {
        this.genericDAO = GenericDAO.getInstance();
    }

    /**
     * Create a map of OS Repo to template names using that repo.
     * @return Map of Os Repository Names to Set of templates Names
     */
    private Map<String, Set<String>> createOSRepoToTemplateMap() {
        Map<String,Set<String>> repoToTemplateMap = new HashMap<>();
        List<ServiceTemplateEntity> allTemplates = getServiceTemplateDAO().getAllTemplates();
        for (ServiceTemplateEntity templateEntity: allTemplates) {
            ServiceTemplate serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                    templateEntity.getMarshalledTemplateData());
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                if (component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.SERVER ||
                        component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE) {
                    String repoName = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                    if (StringUtils.isNotBlank(repoName)) {
                        Set<String> templateNames = repoToTemplateMap.get(repoName);
                        if (templateNames == null) {
                            templateNames = new HashSet<>();
                            repoToTemplateMap.put(repoName, templateNames);
                        }
                        templateNames.add(serviceTemplate.getTemplateName());
                    }
                }
            }
        }
        return repoToTemplateMap;
    }

    /**
     * Create a map of OS Repo to deployment names using that repo.
     * @return Map of Os Repository Names to Set of deployment Names
     */
    private Map<String, Set<String>> createOSRepoToDeploymentMap() {
        Map<String,Set<String>> repoToTemplateMap = new HashMap<>();
        List<DeploymentEntity> allDeployments = getDeploymentDAO().getAllDeployment(DeploymentDAO.NONE);
        for (DeploymentEntity deploymentEntity: allDeployments) {
            ServiceTemplate serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                    deploymentEntity.getMarshalledTemplateData());
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                if (component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.SERVER ||
                        component.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE) {
                    String repoName = component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                    if (StringUtils.isNotBlank(repoName)) {
                        Set<String> deploymentNames = repoToTemplateMap.get(repoName);
                        if (deploymentNames == null) {
                            deploymentNames = new HashSet<>();
                            repoToTemplateMap.put(repoName, deploymentNames);
                        }
                        deploymentNames.add(deploymentEntity.getName());
                    }
                }
            }
        }
        return repoToTemplateMap;
    }

    @Override
    public Response deleteOSRepository(String id) {
        try {
            OSRepositoryEntity osRepo = genericDAO.get(id, OSRepositoryEntity.class);

            // check if the repo is still in progress
            if (OSRepository.STATE_PENDING.equals(osRepo.getState()) || OSRepository.STATE_COPYING.equals(osRepo.getState())) {
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.osRepositoryStillInProgress()
                );
            }

            final Map<String, Set<String>> serviceTemplatesMap = createOSRepoToTemplateMap();
            final Map<String, Set<String>> deploymentsMap = createOSRepoToDeploymentMap();
            final Set<String> templateSet = serviceTemplatesMap.get(osRepo.getName());
            final Set<String> deploymentSet = deploymentsMap.get(osRepo.getName());
            if ((templateSet != null && templateSet.size() > 0) || (deploymentSet != null && deploymentSet.size() > 0)) {
                String templateNames = StringUtils.join(templateSet.toArray(), ",");
                String deploymentNames = StringUtils.join(deploymentSet.toArray(), ",");
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.osRepositoryUsedByTemplate(osRepo.getName(), templateNames, deploymentNames));
            }

            logger.info("starting the OS repository delete");
            ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
            CommandResponse cmdResponse = null;
            String[] cmd = { "sudo", OSRepositoryUtil.RAZOR_SCRIPT_PATH, "-d", osRepo.getRazorName() };
            cmdResponse = cmdRunner.runCommandWithConsoleOutput(cmd);
            String returnCode = cmdResponse.getReturnCode();
            if (!returnCode.equals("0"))
                throw new RepositoryException("Could not delete Razor repo. Error message:" + cmdResponse.getReturnMessage(), AsmManagerMessages.internalError());

            /**
             * Release the encrypted password string
             */
            if (!StringUtils.isEmpty(osRepo.getPassword())) {
                IEncryptedString encryptedString = encryptionDAO.findEncryptedStringById(osRepo.getPassword());
                if (encryptedString != null) {
                    encryptionDAO.delete(encryptedString);
                }
            }

            genericDAO.delete(id, OSRepositoryEntity.class);

            logger.info("completed deleting the repository");
        } catch (LocalizedWebApplicationException le) {
            throw le;
        } catch (RepositoryException e) {
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, e.getEEMILocalizableMessage());
        } catch (Exception e) {
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public List<OSRepository> getOSRepositories() {
        List<OSRepository> repos = new ArrayList<>();
        final Map<String,Set<String>> serviceTemplatesMap = createOSRepoToTemplateMap();
        final Map<String,Set<String>> deploymentsMap = createOSRepoToDeploymentMap();
        for (OSRepositoryEntity entity : genericDAO.getAll(OSRepositoryEntity.class)){
            OSRepository osRepo = entity.toOSRepository();
            Set<String> templateSet = serviceTemplatesMap.get(osRepo.getRazorName());
            Set<String> deploymentSet = deploymentsMap.get(osRepo.getRazorName());
            boolean inUse = false;
            if ((templateSet != null && templateSet.size() > 0) || (deploymentSet != null && deploymentSet.size() > 0)) {
                inUse = true;
            }
            osRepo.setInUse( inUse );
            repos.add(osRepo);
        }
        return repos;
    }

    @Override
    public OSRepository getOSRepositoryById(String id){
        OSRepositoryEntity entity =  genericDAO.get(id, OSRepositoryEntity.class);
        return entity.toOSRepository();
    }

    @Override
    public OSRepository createOSRepository(OSRepository osRepo) throws WebApplicationException {
        return createOrUpdateOSRepository(osRepo, false, true);
    }

    @Override
    public OSRepository updateOSRepository(String id, OSRepository osRepo,
                                           Boolean sync) throws WebApplicationException {
        if (osRepo == null) {
            osRepo = getOSRepositoryById(id);
        } else {
            osRepo.setId(id);
        }
        return createOrUpdateOSRepository(osRepo,true,sync);
    }

    @Override
    public OSRepository syncOSRepositoryById(String id, OSRepository osRepo) {
        try
        {
            OSRepositoryEntity entity = genericDAO.get(id, OSRepositoryEntity.class);

            if (osRepo.getPassword() == null) {
                osRepo.setPassword(entity.decryptedPassword());
            }
            syncRazorRepo(osRepo);
            // The only thing we change on sync is state
            entity.setState(osRepo.getState());
            genericDAO.update(entity);
            return osRepo;
        }
        catch(Exception e)
        {
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

    }

    /**
     * Create or update the Razor repo from the source
     *
     * @param osRepo - OS Repository to be synced
     * @return OSRepository object
     */
    private OSRepository syncRazorRepo(OSRepository osRepo) throws SchedulerException {
        IJobManager jm = JobManager.getInstance();
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
        JobDetail job = jm.createNamedJob(CreateOSRepoJob.class);

        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

        String jsonData = toJson(osRepo);
        job.getJobDataMap().put(CreateOSRepoJob.CreateOSRepoJob_SERVICE_KEY_DATA, jsonData);
        jm.scheduleJob(job, trigger);
        osRepo.setState(OSRepository.STATE_COPYING);
        return osRepo;
    }

    /**
     *
     * @param osRepo - to be created or updated
     * @param update allows for updating existing entity or asserts non-existence of entity
     * @param sync directs whether to create/sync associated folder containing ISO
     * @return OSRepository
     * @throws WebApplicationException
     */
    private OSRepository createOrUpdateOSRepository(OSRepository osRepo,
                                                    boolean update,
                                                    boolean sync) throws WebApplicationException {
        OSRepositoryEntity entity = null;
        try {
            validateInputs(osRepo);
            checkRazorRepoExistence(osRepo, update);

            if (update) {
                entity = genericDAO.get(osRepo.getId(), OSRepositoryEntity.class);

                if (osRepo.getPassword() == null) {
                    osRepo.setPassword(entity.decryptedPassword());
                    getOsRepositoryUtil().testConnection(osRepo);
                    osRepo.setPassword(null);
                } else {
                    getOsRepositoryUtil().testConnection(osRepo);
                }
                entity.update(osRepo, true);  // true indicates to save it
            } else {
                getOsRepositoryUtil().testConnection(osRepo);
                entity = new OSRepositoryEntity(osRepo);
                entity.setRazorName(getScrubbedName(osRepo));
                entity = genericDAO.create(entity);
                osRepo.setId(entity.getId());
            }

            if (sync) {
                syncRazorRepo(osRepo);
            }
            return entity.toOSRepository();
        } catch (LocalizedWebApplicationException e) {
            logger.error("Cannot add or update OS repo", e);
            throw e;
        } catch (AsmCheckedException e) {
            logger.error("Cannot add or update OS repo", e);
            if (entity != null)
                entity.setState(OSRepository.STATE_ERRORS);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, e.getEEMILocalizableMessage());
        } catch (Exception e) {
            logger.error("Cannot add or update OS repo", e);
            if (entity != null)
                entity.setState(OSRepository.STATE_ERRORS);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        } finally {
            if (entity != null)
                genericDAO.update(entity);
        }
    }

    @Override
    public Response testConnection(OSRepository osRepo)
    {
        try {
            validateInputs(osRepo);
            if (osRepo.getPassword() == null && osRepo.getId() != null) {
                OSRepositoryEntity entity = genericDAO.get(osRepo.getId(), OSRepositoryEntity.class);
                osRepo.setPassword(entity.decryptedPassword());
            }
            getOsRepositoryUtil().testConnection(osRepo);
        } catch (AsmCheckedException e) {
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, e.getEEMILocalizableMessage());
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private void validateInputs(OSRepository osRepo)
            throws AsmCheckedException {
        //Support CIFS,NFS,HTTP, or FTP - repoType should just throw an exception for an invalid path
        //but it supports at least one more type than here so checking specifically for NFS, CIFS, HTTP,
        //or FTP
        String repoType = DownloadFileUtil.getType(osRepo.getSourcePath());
        if(repoType.equals("CIFS") || repoType.equals("NFS") || repoType.equals("HTTP") || repoType.equals("FTP")) {
          //Currently username/password is only used for CIFS shares.  Throw exception otherwise.
            if(!DownloadFileUtil.getType(osRepo.getSourcePath()).equals("CIFS") &&
                    (StringUtils.isNotEmpty(osRepo.getPassword()) || StringUtils.isNotEmpty(osRepo.getUsername()))) {
                throw new RepositoryException("Username/Password can only be used for CIFS.", AsmManagerMessages.credentialsNotSupported());
            }
        } else {
            throw new RepositoryException("Enter a valid CIFS, NFS, HTTP, or FTP path.", AsmManagerMessages.unsupportedPathFormat(osRepo.getSourcePath()));
        }
        // Assert this looks like an ISO image
        getOsRepositoryUtil().isValidImageFile(osRepo.getSourcePath());

        getOsRepositoryUtil().validateRazorTaskSupported(osRepo.getImageType());
    }

    /**
     * Assert this entity does NOT exist, unless we explicitly allow for update.
     * Also asserts the associated razor repo is of the same type if it exists.
     * @param osRepo - OS Repository to check for existence
     * @param update disable exception if this is true
     * @throws Exception
     */
    private void checkRazorRepoExistence(OSRepository osRepo, boolean update) throws Exception {
        String repoName = osRepo.getName();
        if(genericDAO.getByName(repoName, OSRepositoryEntity.class) != null && !update ) {
            throw new RepositoryException("OS Repo by the name "+ repoName +" already exists", AsmManagerMessages.razorRepoAlreadyExists(repoName));
        }
        String name = getScrubbedName(osRepo);
        List<RazorRepo> razorImages = getOsRepositoryUtil().getRazorOSImages(true);
        for(RazorRepo repo : razorImages){
            if(repo.getName().equals(name)){
                //If the repo name exists, but the task is different, we will throw an error.  otherwise, we want to "refresh" the repo with the URL specified.
                if(!repo.getTask().equals(osRepo.getImageType())) {
                    throw new RepositoryException("OS Repo by the name " + repoName + " already exists", AsmManagerMessages.razorRepoAlreadyExists(repoName));
                }
            }
        }
    }

    public static String toJson(OSRepository deployment) {
        try {
            StringWriter sw = new StringWriter();
            OBJECT_MAPPER.writeValue(sw, deployment);
            return sw.toString();
        } catch (IOException e) {
            throw new AsmManagerRuntimeException("Failed to marshal deployment", e);
        }
    }

    public static OSRepository fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, OSRepository.class);
        } catch (IOException e) {
            throw new AsmManagerRuntimeException("Failed to unmarshal deployment json", e);
        }
    }

    //Generates a "razor friendly" repo name so the user can put in any name from their wildest imaginations
    private String getScrubbedName(OSRepository newRepo){
        String baseName = newRepo.getName().replaceAll("[^A-Za-z0-9_.-]", "");
        String newName = baseName;
        List<RazorRepo> repos = getOsRepositoryUtil().getRazorOSImages(true);
        //This map is so the existing razor repos are easier to lookup below, as opposed to looping through a list
        Map<String, RazorRepo> repoMap = new HashMap<>();
        for(RazorRepo razorRepo: repos){
            repoMap.put(razorRepo.getName(), razorRepo);
        }
        Map<String,String> razorToAsmRepoNamesMap = getOsRepositoryUtil().mapRazorRepoNamesToAsmRepoNames();
        int nameIndex = 1;
        while(repoMap.containsKey(newName)){
            // If we find a repo with the same name/task, and one doesn't exist in ASM with the scrubbed name, we will use that repo name
            if(razorToAsmRepoNamesMap.get(newName) == null){
                break;
            }
            newName = baseName + "-" +nameIndex;
            nameIndex++;
        }
        return newName;
    }

    public ServiceTemplateDAO getServiceTemplateDAO() {
        if (serviceTemplateDAO == null) {
            serviceTemplateDAO = ServiceTemplateDAO.getInstance();
        }
        return serviceTemplateDAO;
    }

    public void setServiceTemplateDAO(ServiceTemplateDAO serviceTemplateDAO) {
        this.serviceTemplateDAO = serviceTemplateDAO;
    }

    public DeploymentDAO getDeploymentDAO() {
        if (deploymentDAO == null) {
            deploymentDAO = DeploymentDAO.getInstance();
        }
        return deploymentDAO;
    }

    public void setDeploymentDAO(DeploymentDAO deploymentDAO) {
        this.deploymentDAO = deploymentDAO;
    }

    public OSRepositoryUtil getOsRepositoryUtil() {
        if (osRepositoryUtil == null) {
            osRepositoryUtil = new OSRepositoryUtil();
        }
        return osRepositoryUtil;
    }

    public void setOsRepositoryUtil(OSRepositoryUtil osRepositoryUtil) {
        this.osRepositoryUtil = osRepositoryUtil;
    }
}
