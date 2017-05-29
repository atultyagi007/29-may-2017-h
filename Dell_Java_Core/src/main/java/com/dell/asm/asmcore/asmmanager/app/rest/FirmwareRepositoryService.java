package com.dell.asm.asmcore.asmmanager.app.rest;

import static com.dell.asm.asmcore.asmmanager.AsmManagerMessages.createdFirmwareRepository;
import static com.dell.asm.asmcore.asmmanager.AsmManagerMessages.deletedFirmwareRepository;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareBundle;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.asm.asmcore.asmmanager.client.firmware.SourceType;
import com.dell.asm.asmcore.asmmanager.client.firmwarerepository.IFirmwareRepositoryService;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.SoftwareBundleDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SystemIDEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.FirmwareRepoSyncJob;
import com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareRepositoryFileUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.ReadFirmwareRepositoryUtil;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage.LogCategory;
import com.dell.asm.localizablelogger.LogMessage.LogSeverity;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.catalogmgr.exceptions.CatalogException;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.dell.pg.orion.common.utilities.VersionUtils;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.dell.pg.orion.security.encryption.IEncryptedString;

@Path("/firmwareRepository")
public class FirmwareRepositoryService implements IFirmwareRepositoryService {

    private static final Logger logger = Logger.getLogger(FirmwareRepositoryService.class);
    public static final Pattern NAME_DUP_SUFFIX_PATTERN = Pattern.compile("\\s\\(([0-9]+)\\)$");

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    private FirmwareRepositoryFileUtil firmwareRepositoryFileUtil = new FirmwareRepositoryFileUtil();
    private final GenericDAO genericDAO = GenericDAO.getInstance();
    private final FirmwareRepositoryDAO firmwareRepositoryDAO = FirmwareRepositoryDAO.getInstance();
    private final EncryptionDAO encryptionDAO = EncryptionDAO.getInstance();
    private final SoftwareBundleDAO softwareBundleDAO = SoftwareBundleDAO.getInstance();
    private final LocalizableMessageService logService = LocalizableMessageService.getInstance();
    private FirmwareUtil firmwareUtil = null;
    private ServiceDeploymentUtil serviceDeploymentUtil = null;
    
    public FirmwareRepositoryService(FirmwareUtil firmwareUtil, 
                                     ServiceDeploymentUtil servDeployUtil) {
        this.firmwareUtil = firmwareUtil;
        this.serviceDeploymentUtil = servDeployUtil;
    }
    
    public FirmwareRepositoryService() {
        this(new FirmwareUtil(), new ServiceDeploymentUtil());
    }

    
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @Override
    public Response deleteRepository(String id) {

        logger.info("starting the repository delete");

        FirmwareRepositoryEntity repo = firmwareRepositoryDAO.getFirmwareWithUsedBy(id);
        if (repo == null) {
            throw new LocalizedWebApplicationException(
                    Response.Status.NOT_FOUND,
                    AsmManagerMessages.invalidFirmwareRepositoryId(id));
        }

        if (repo.isDefault()) {
            throw new LocalizedWebApplicationException(
                    Response.Status.BAD_REQUEST,
                    AsmManagerMessages.canNotDeleteDefaultFirmwareCatalog());
        }

        if (repo.getTemplates() != null && repo.getTemplates().size() > 0) {
            for (ServiceTemplateEntity template : repo.getTemplates()) {
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.firmwareRepositoryUsedByTemplate(template.getName()));
            }
        }

        if (repo.getDeployments() != null && repo.getDeployments().size() > 0) {
            throw new LocalizedWebApplicationException(
                    Response.Status.BAD_REQUEST,
                    AsmManagerMessages.firmwareRepositoryInUseByService());
        }

        try {
            firmwareRepositoryDAO.delete(repo);

            if (!StringUtils.isEmpty(repo.getPassword())) {
                IEncryptedString encryptedString = encryptionDAO.findEncryptedStringById(repo.getPassword());
                if (encryptedString != null) {
                    encryptionDAO.delete(encryptedString);
                }
            }

            // Delete binaries
            firmwareRepositoryFileUtil.deleteRepositoryBinaries(repo);

            logService.logMsg(deletedFirmwareRepository(repo.getName()).getDisplayMessage(),
                              LogSeverity.INFO, LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
            logger.info("Firmware Repository " + repo.getName() + " has been deleted from the ASM appliance");
        } catch (Exception e) {
            logger.error("An exception occurred attempting to delete " + repo.getName(), e);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    private boolean validateAgainstEmbedded(FirmwareRepositoryEntity firmwareRepository)
    {
    	HashMap<String, Object> attributes = new HashMap<>();
    	attributes.put("isEmbedded", Boolean.TRUE);
    	
    	List<FirmwareRepositoryEntity> repos = genericDAO.getForEquals(attributes, FirmwareRepositoryEntity.class);
    	
    	
    	if (repos != null)
    	{    	
    		for (FirmwareRepositoryEntity embededRepository : repos)
    		{
    			if (embededRepository.getId().equals(firmwareRepository.getId()))
    				return true;
    			
    			attributes.clear();
    			attributes.put("firmwareRepositoryEntity", embededRepository);
    			List<SoftwareComponentEntity> components = genericDAO.getForEquals(attributes, SoftwareComponentEntity.class);
    			
    			for (SoftwareComponentEntity componentToMatch : components)
    			{
    				SoftwareComponentEntity componentToTest = getMatchingComponent(componentToMatch, firmwareRepository.getSoftwareComponents());
    				if (componentToTest != null)
    				{
    				    // This function compares the strBaseVersion with strComparisonVersion
    				    // It returns 0 if both the versions are same
    				    // It returns < 0 if the strOldVersion version is greater than the strNewVersion version
    				    // It returns > 0 if the strOldVersion version is less than the strNewVersion version
    					int versionComparison = VersionUtils.compareVersions(componentToTest.getVendorVersion(), componentToMatch.getVendorVersion());

    					//< 0 means we should not accept this
    					if (versionComparison < 0)
    					{
    						logger.debug("Illegal catalog, version not supported. componentToTest: " + componentToTest.getVendorVersion() + " componentToMatch: " + componentToMatch.getVendorVersion());
    						return false;
    					}
    				}
    			}
    		}
    	}    	
    	
    	return true;
    }

    /**
     * Returns a name similar to {@code desired} but which is not contained in {@code similar}.
     * This is similar to the algorithm used in Windows when creating new folders. E.g. the
     * first created folder will be named "New Folder", the 2nd "New Folder (2)", the third
     * "New Folder (3)", etc.
     *
     * @param desired The user-selected name
     * @param similar List of strings which may conflict with the desired name
     * @return A unique name
     */
    static String getUniqueName(String desired, List<String> similar) {
        boolean matchFound = false;
        Set<Integer> indicesFound = new HashSet<>();
        for (String s : similar) {
            if (s.equals(desired)) {
                matchFound = true;
            } else if (s.startsWith(desired)) {
                String ending = s.substring(desired.length());
                Matcher m = NAME_DUP_SUFFIX_PATTERN.matcher(ending);
                if (m.matches()) {
                    String num = m.group(1);
                    indicesFound.add(Integer.valueOf(num));
                }
            }
        }

        if (!matchFound) {
            return desired;
        } else {
            int i = 2;
            while (indicesFound.contains(i)) {
                i++;
            }
            return desired + " (" + i + ")";
        }
    }

    //the components are not persisted yet so we cant query for them
    private SoftwareComponentEntity getMatchingComponent(SoftwareComponentEntity softwareComponentToMatch, Set<SoftwareComponentEntity> componentSet)
    {
    	if (componentSet != null)
    		for (SoftwareComponentEntity entity : componentSet)
    		{
    			if (softwareComponentToMatch.getComponentId().equals(entity.getComponentId()))
    			{
    				boolean match = true;
    				if ((softwareComponentToMatch.getDeviceId() != null && !softwareComponentToMatch.getDeviceId().equals(entity.getDeviceId())) || entity.getDeviceId() != null)
    					match = false;
    				if ((softwareComponentToMatch.getSubDeviceId() != null && !softwareComponentToMatch.getSubDeviceId().equals(entity.getSubDeviceId())) || entity.getSubDeviceId() != null)
    					match = false;
    				if ((softwareComponentToMatch.getVendorId() != null && !softwareComponentToMatch.getVendorId().equals(entity.getVendorId())) || entity.getVendorId() != null)
    					match = false;
    				if ((softwareComponentToMatch.getSubVendorId() != null && !softwareComponentToMatch.getSubVendorId().equals(entity.getSubVendorId())) || entity.getSubVendorId() != null)
    					match = false;    	
    				
    				HashMap<String, Object> attributes = new HashMap<String, Object>();
    				attributes.put("softwareComponentEntity", softwareComponentToMatch);
    				List<SystemIDEntity> systemIds = genericDAO.getForEquals(attributes, SystemIDEntity.class);
    				if (systemIds == null || systemIds.size() == 0)
    				{
    					if (entity.getSystemIDs() == null || systemIds.size() == 0)
    						match = true;
    				}
    				else
    				{
    					Set<String> comparisonIDs = new HashSet<String>();
    					for (SystemIDEntity sysid : entity.getSystemIDs())
    					{
    						comparisonIDs.add(sysid.getSystemId());    						
    					}
    					
    					for (SystemIDEntity idFromDB : systemIds)
    					{
    						if (!comparisonIDs.contains(idFromDB.getSystemId()))
    						{
    							match = false;
    							break;
    						}
    					}
    				}
    				    				
    				if (match)
    					return entity;
    			}
    		}
    	return null;
    }


    @Override
    public FirmwareRepository createFirmwareRepository(FirmwareRepository firmwareRepository)
            throws WebApplicationException {

        logger.debug("Create firmware repository Entered for firmware: " + firmwareRepository.getName());

        File tmpDir = null;
        try {
            if (firmwareRepository != null) {

                FirmwareRepositoryEntity entity = null;
                boolean isDefaultCatalog = false;

                if (StringUtils.isBlank(firmwareRepository.getId())) {
                    String sourceLocation = firmwareRepository.getSourceLocation();
                    if (FirmwareRepository.ASM_REPOSITORY_LOCATION.equals(sourceLocation)) {
                        firmwareRepository.setSourceLocation(AsmManagerApp.ASM_REPO_LOCATION);
                        sourceLocation = firmwareRepository.getSourceLocation();
                    } else if (FirmwareRepository.DISK.equals(firmwareRepository.getSourceLocation())) {
                        final java.nio.file.Path catalogFilePath = Paths.get(firmwareRepository.getDiskLocation());
                        if (Files.notExists(catalogFilePath)) {
                            // Not user-facing message for same reason as above
                            String msg = "Invalid catalog file " + catalogFilePath.toString();
                            logger.warn(msg);
                            throw new AsmManagerRuntimeException(msg);
                        }
                        sourceLocation = catalogFilePath.toUri().toString();
                    }

                    firmwareRepository.setState(RepositoryState.COPYING);

                    entity = downloadTempCatalogAndCreateEntity(sourceLocation,
                                                                firmwareRepository.getUsername(),
                                                                firmwareRepository.getPassword());

                    tmpDir = new File(entity.getDiskLocation());

                    if (FirmwareRepository.DISK.equals(firmwareRepository.getSourceLocation())) {
                        // Display to user as "Disk" instead of original temp file location
                        entity.setSourceLocation("Disk");
                    }

                    if (!validateAgainstEmbedded(entity)) {
                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.catalogNotSupported());
                    }

                    String plaintext = entity.getPassword();
                    if (StringUtils.isEmpty(plaintext)) {
                        entity.setPassword(null);
                    } else {
                        // Replace the plaintext value with an encryption id
                        IEncryptedString encryptedString = encryptionDAO.encryptAndSave(plaintext);
                        entity.setPassword(encryptedString.getId());
                    }

                    // Generate a unique name
                    List<FirmwareRepositoryEntity> matches = genericDAO.getByNameStartsWith(entity.getName(),
                                                                                            FirmwareRepositoryEntity.class);
                    List<String> matchingNames = new ArrayList<>(matches.size());
                    for (FirmwareRepositoryEntity match : matches) {
                        matchingNames.add(match.getName());
                    }
                    entity.setName(getUniqueName(entity.getName(), matchingNames));

                    // Save whether it is the default so we can change it to default once it is successfully downloaded
                    isDefaultCatalog = firmwareRepository.isDefaultCatalog();
                    entity.setDefault(false);
                    
                    // There is a small window here where a record could be inserted with the same name
                    // just selected above resulting in duplicates. Because the impact of this is low (it's
                    // confusing to the user to have multiple repositories with the same name), not
                    // attempting to fix it at this time.
                    entity = genericDAO.create(entity);

                    firmwareUtil.updateComplianceMapForRepo(entity);

                    logService.logMsg(createdFirmwareRepository(entity.getName()).getDisplayMessage(),
                                      LogSeverity.INFO, LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);

                    // Move the catalog to a permanent location
                    File source = new File(tmpDir, entity.getFilename());
                    File finalDiskLocation = new File(FirmwareRepositoryFileUtil.FIRMWARE_REPO_BASE_LOCATION + File.separator +
                                                              entity.getId());
                    if (!finalDiskLocation.mkdirs()) {
                        String msg = "Failed to create final disk location " + finalDiskLocation;
                        logger.error(msg);
                        firmwareRepository.setState(RepositoryState.ERROR);
                        entity.setState(RepositoryState.ERROR);
                        genericDAO.update(entity);
                        throw new AsmManagerRuntimeException(msg);
                    }
                    File destination = new File(finalDiskLocation, entity.getFilename());
                    Files.copy(source.toPath(), destination.toPath());

                    entity.setDiskLocation(finalDiskLocation.getAbsolutePath());
                    entity.setState(RepositoryState.AVAILABLE);
                    genericDAO.update(entity);

                    firmwareRepository.setId(entity.getId());
                    firmwareRepository.setState(RepositoryState.AVAILABLE);

                } else {
                    entity = firmwareRepositoryDAO.get(firmwareRepository.getId());
                    if (entity == null) {
                        logger.error("Firmware Repository with id: " + firmwareRepository.getId() + " was not found in the database.");
                        throw new LocalizedWebApplicationException(
                                Response.Status.BAD_REQUEST,
                                AsmManagerMessages.invalidRepositoryForResynchronization());
                    }
                }
                scheduleFirmwareRepoSyncJob(entity.getId(), isDefaultCatalog, entity.getCreatedDate());
            }
        } catch (LocalizedWebApplicationException e) {
            logger.error(
                    "LocalizedWebApplicationException while creating firmware repository "
                            + firmwareRepository.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while creating firmware repository "
                                 + firmwareRepository.getName(), e);
            firmwareRepository.setState(RepositoryState.ERROR);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        } finally {
            if (tmpDir != null) {

                // TODO: In the case of "disk" source location we are not deleting the referenced
                // file. Doing so would be a security hole when the API is opened up, potentially
                // allowing callers to maliciously delete files. The controller needs to delete
                // the temp files, which I don't think it's doing right now. --gavin_scott

                try {
                    FileUtils.deleteDirectory(tmpDir);
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary download directory " + tmpDir
                                        + " for " + firmwareRepository);
                }
            }
        }

        logger.debug("Create firmware repository done for: "
                             + firmwareRepository.getName() + ". ID = "
                             + firmwareRepository.getId());

        return firmwareRepository;
    }

    private void scheduleFirmwareRepoSyncJob(String firmwareRepoId, boolean isDefaultCatalog, Date catalogCreationTime) {
        IJobManager jm = JobManager.getInstance();
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
        JobDetail job = jm.createNamedJob(FirmwareRepoSyncJob.class);
        job.getJobDataMap().put(FirmwareRepoSyncJob.FIRMWARE_REPO_ID_KEY, firmwareRepoId);
        job.getJobDataMap().put(FirmwareRepoSyncJob.IS_DEFAULT_CATALOG_KEY, isDefaultCatalog);
        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

        try {
            jm.scheduleJob(job, trigger);
            logger.info("checking and starting the scheduler");
            if (!jm.getScheduler().isStarted()) {
                jm.getScheduler().start();
                logger.info("scheduler started");
            }
        } catch (SchedulerException e) {
            logger.error("Error scheduling FirmwareRepoSyncJob for repository " + firmwareRepoId, e);
        }
    }

    /**
     * Creates a {@code FirmwareRepositoryEntity} from a remote catalog {@code sourceLocation}. The
     * location can be an NFS, CIFS or FTP share. Username and password may be provided for CIFS
     * shares. The XML catalog file will be copied into a temporary directory which will be
     * returned as the entity {@code diskLocation}. If the source is a CAB file all of its
     * contents will be contained in the {@code diskLocation} as well. The caller should delete
     * the {@code diskLocation} directory when done.
     *
     * @param sourceLocation NFS, CIFS or FTP location
     * @param username CIFS username (null if not applicable)
     * @param password CIFS password (null if not applicable)
     * @return the resulting entity
     *
     * @throws CatalogException if an error occurs downloading the catalog file
     * @throws LocalizedWebApplicationException on a user-visible exception
     */
    private FirmwareRepositoryEntity downloadTempCatalogAndCreateEntity(String sourceLocation, String username, String password) {
        String type;
        String filename;
        String extension;

        // Validate source location
        try {
            type = DownloadFileUtil.getType(sourceLocation);
            filename = DownloadFileUtil.getFileNameFromPath(sourceLocation);
            String lowerCasedFilename = filename.toLowerCase();
            if (lowerCasedFilename.endsWith(".cab")) {
                extension = "cab";
            } else if (lowerCasedFilename.endsWith(".xml")) {
                extension = "xml";
            } else {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.invalidCatalogFileName());
            }
        } catch (RepositoryException e) {
            logger.debug("User-supplied catalog source location was invalid: " + sourceLocation);
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, e.getEEMILocalizableMessage());
        }

        try {
            // Download the source file into a temporary directory
            java.nio.file.Path catalog;
            try {
                catalog = Files.createTempDirectory("catalog");
            } catch (IOException e) {
                throw new AsmManagerRuntimeException("Failed to create temporary directory for " + sourceLocation);
            }
            File tmpDir = new File(catalog.toString());
            File catalogFile = DownloadFileUtil.downloadFile(sourceLocation,
                                                             username,
                                                             password,
                                                             tmpDir,
                                                             "catalog");

            if ("cab".equals(extension)) {
                // Need to extract the CAB file and find the contained catalog. Note the CAB is
                // contained in the temporary directory where the contents will be extracted.
                catalogFile = firmwareRepositoryFileUtil.processCatalogCABFile(catalogFile.getAbsolutePath(), tmpDir);
            }

            // Create the associated softwareComponents
            FirmwareRepositoryEntity ret = ReadFirmwareRepositoryUtil.loadFirmwareRepositoryFromFile(catalogFile);

            ret.setSourceType(type);
            ret.setSourceLocation(sourceLocation);
            ret.setDownloadStatus(RepositoryStatus.PENDING);
            ret.setUsername(username);
            ret.setPassword(password);
            ret.setState(RepositoryState.COPYING);

            // The catalog itself may contain a baseLocation such as ftp.dell.com. Otherwise the
            // baseLocation is implicitly the remote directory the catalog file is contained in.
            String baseLocation = ret.getBaseLocation();
            if (StringUtils.isBlank(baseLocation)) {
                DownloadFileUtil.RemoteLocationInfo info = DownloadFileUtil.getRemoteLocationInfo(ret.getSourceLocation());
                ret.setBaseLocation(info.path);
            } else {
                String fullRemotePath = resolveBaseLocation(baseLocation);
                if (!baseLocation.equals(fullRemotePath)) {
                    logger.debug("Resolved base location " + baseLocation + " to " + fullRemotePath);
                    ret.setBaseLocation(fullRemotePath);
                }
            }

            return ret;
        } catch (CatalogException | RepositoryException e) {
            logger.error("error getting remote catalog", e);
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, e.getEEMILocalizableMessage());
        }
    }

    /**
     * Converts a baseLocation field into a fully-qualified source location as understood by
     * {@link com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil}, i.e. a CIFS, NFS or
     * FTP path. If the baseLocation does not already conform to that format it is checked
     * to see whether it is a valid host name, in which case ftp is assumed.
     *
     * @param baseLocation The base location field
     * @return The full remote file location corresponding to the base location
     * @throws com.dell.asm.rest.common.exception.LocalizedWebApplicationException if the
     *         {@code baseLocation} is invalid. This can be passed through as the REST response.
     */
    private static String resolveBaseLocation(String baseLocation) {
        try {
            // Check if the baseLocation already conforms to our path specifications
            DownloadFileUtil.getType(baseLocation);
            return baseLocation;
        } catch (RepositoryException ignored1) {
            // Check if baseLocation is an unsupported URL
            try {
                new URL(baseLocation);

                // If we get here, it is a valid URL, but not supported by ASM or it would have
                // been resolved properly by DownloadFileUtil.getType
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.unsupportedCatalogBaseLocationUrl());
            } catch (MalformedURLException ignored2) {
                // Check if baseLocation is a valid host name
                try {
                    InetAddress byName = InetAddress.getByName(baseLocation);
                    logger.debug("Resolved base location " + baseLocation + " to " + byName);
                } catch (UnknownHostException e) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.catalogBaseLocationHostUnknown(baseLocation));
                }

                // baseLocation is a valid host name, create FTP url
                return "ftp://" + baseLocation;
            }
        }
    }

    @Override
    public List<FirmwareRepository> getFirmwareRepositories(String sort,
                                                            List<String> filter,
                                                            Integer offset,
                                                            Integer limit,
                                                            UriInfo ui,
                                                            boolean related,
                                                            boolean bundles,
                                                            boolean components) {

        List<FirmwareRepository> repoEntityList = new ArrayList<>();

        for (FirmwareRepositoryEntity repoEnt : genericDAO.getAll(FirmwareRepositoryEntity.class)){
            logger.trace("Id is " + repoEnt.getId());
            logger.trace("Name is " + repoEnt.getName());
            logger.trace("Source is " + repoEnt.getSourceLocation());

            if (related) {
                bundles = true;
                components = true;
            }
            if (bundles || components) {
                repoEnt = firmwareRepositoryDAO.getCompleteFirmware(repoEnt.getId(), bundles, components);
            }
            repoEntityList.add(firmwareUtil.entityToDto(repoEnt, Boolean.TRUE));
        }
        return repoEntityList;
    }

    @Override
    public FirmwareRepository getFirmwareRepository(String id,
                                                    boolean related,
                                                    boolean bundles,
                                                    boolean components) {

        logger.debug("Getting firmware repository for firmware id: " + id);
        FirmwareRepositoryEntity firmwareRepositoryEntity;
        if (related) {
            bundles = true;
            components = true;
        }
        if (bundles || components) {
            firmwareRepositoryEntity = firmwareRepositoryDAO.getCompleteFirmware(id, bundles, components);
        } else {
            firmwareRepositoryEntity = genericDAO.get(id, FirmwareRepositoryEntity.class);
        }

        return (firmwareRepositoryEntity != null) ? firmwareUtil.entityToDto(firmwareRepositoryEntity, Boolean.TRUE) : null;
    }
	
	@Override
	public SoftwareBundle getSoftwareBundle(String id) {
    	logger.debug("Getting firmware repository for firmware id: " + id);
    	
    	//No longer enforcing name uniqueness
    	//SoftwareBundleEntity softwareBundle = genericDAO.get(id, SoftwareBundleEntity.class);

        SoftwareBundleEntity softwareBundleEntity = softwareBundleDAO.getSoftwareBundleById(id);
    	if (softwareBundleEntity != null)
    		return softwareBundleEntity.getSoftwareBundle();
    	else
    		return null;
    
	}

    @Override
    public FirmwareRepository update(String id, FirmwareRepository firmwareRepository) {
        logger.debug("Update firmware repository Entered for firmware: " + firmwareRepository.getName());
        try {
            final FirmwareRepositoryEntity entity = firmwareRepositoryDAO.get(id);
            
            if (entity == null) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND,
                                                           AsmManagerMessages.invalidFirmwareRepositoryId(id));
            }

            if(!entity.isDefault() && firmwareRepository.isDefaultCatalog()) {
                // Means the Default catalog was changed and we need to update accordingly
                if(entity.isEmbedded()) { // embedded cannot be set as the default
                    throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                            AsmManagerMessages.embeddedFirmwareRepositoryCannotBeDefault());
                }
                // Else Pull all of the 
                this.firmwareRepositoryDAO.updateAllIsDefault(false);
                entity.setDefault(true);
                firmwareRepositoryDAO.saveOrUpdate(entity);
                firmwareUtil.updateComplianceMapAndDeviceComplianceForRepo(entity);
                serviceDeploymentUtil.updateDeploymentComplianceThatAreUsingDefaultCatalog();
            }
            else { // Just a simple change of the repository
                // Only allow default and name to be changed.
                entity.setDefault(firmwareRepository.isDefaultCatalog());
                entity.setName(firmwareRepository.getName());

                firmwareRepositoryDAO.saveOrUpdate(entity);
            }
        } catch (LocalizedWebApplicationException e) {
            logger.error(
                    "LocalizedWebApplicationException while updating firmware repository "
                            + firmwareRepository.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while updating firmware repository " + firmwareRepository.getName(), e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        logger.debug("Update firmware repository done for: " + firmwareRepository.getName() + ". ID = "
                + firmwareRepository.getId());

        return firmwareRepository;
    }

	/**
	 * Gets the software components for either the default or embedded repository.  <br>
	 * <br>
	 * NOTE:  Thus far this method ONLY returns results for Firmware components, and NOT Software components as it's 
	 * primarily used during the Configure Chassis flow.  If this method needs to support Software components at a 
	 * later time, then a class type of 'SourceType' needs to be added to the method signature.  The SourceType will 
	 * either be a 'Device' (meaning it's Firmware) or 'Catalog' (which means it's going to be Software).  The 
	 * SourceType will need to be passed into the FirmwareUtil.getSoftwareComponents call towards the bottom of 
	 * this method.  
	 */
	@Override
	public List<SoftwareComponent> getSoftwareComponents(String componentId,
			String deviceId, String subDeviceId, String vendorId,
			String subVendorId, String systemId, String type, String operatingSystem) {

    	logger.trace("getsoftware components with componentid: " + componentId + " deviceid: " + deviceId + " subdeviceid: " + subDeviceId + " vendorid: " + vendorId + " subvendorid: " + subVendorId);

    	List<SoftwareComponent> components = new ArrayList<>();
        try 
        {                    
        	HashMap<String, Object> attributeMap = new HashMap<>();
        	if ("embeded".equals(type)) {
        		attributeMap.put("isEmbedded", Boolean.TRUE);
        	}
        	else if ("default".equals(type)) {
        		attributeMap.put("isDefault", Boolean.TRUE);
        	}
        	else { 
        		attributeMap.put("id", type);//if not embeded or default then we're passing in a primarykey of a repo
        	}
        	
        	List<FirmwareRepositoryEntity> repoList = genericDAO.getForEquals(attributeMap, FirmwareRepositoryEntity.class);        	
        	FirmwareRepositoryEntity parentRepo = null;

        	if (repoList != null && repoList.size() > 0) {        	
        		parentRepo = repoList.get(0);
        	}
        	        	        	        
        	if (parentRepo != null) {
        		components = firmwareUtil.getSoftwareComponents(componentId, deviceId, subDeviceId, vendorId, subVendorId, parentRepo, systemId, SourceType.Device, operatingSystem, false);
        	}
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while retrieving software components", e);
            throw e;
        } catch (Exception e) {
            logger.error("LocalizedWebApplicationException while retrieving software components", e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }


		return components;
	}
	
   @Override
    public Response testConnection(FirmwareRepository firmwareRepository)
    {       
        try {
            if ("asm_repo_location".equals(firmwareRepository.getSourceLocation())){
                firmwareRepository.setSourceLocation(AsmManagerApp.ASM_REPO_LOCATION); 
            }            
            firmwareUtil.testConnection(firmwareRepository);
        } catch (AsmCheckedException e) {
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, e.getEEMILocalizableMessage());
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
   
   /**
    * Sets any existing catalog that was default to no longer be the default, then changes the given Firmware 
    * Repository to be the default, and finally updates all of the Deployments who are using the default Catalog to 
    * utilize the newly assigned Catalog. <br />
    * <br />
    * If the checkCreatedTime is set to true, then we will ensure the new default catalog that's passed in was created
    * after the current default catalog was last updated.  This is to handle the multi-thread process and possibility
    * of users selecting multiple default catalogs each time they upload a catalog.
    * 
    * @param firmwareRepoId the id of the new Default Firmware Repository (also known as a 'catalog' in ASM)
    * @param checkCreatedTime indicates whether we should check the createdTime to determine whether the default catalog
    * 	can be updated.
    * @throws AsmManagerCheckedException
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    */
   public void changeToDefaultCatalog(String firmwareRepoId, boolean checkCreatedTime) 
		   throws AsmManagerCheckedException, 
		   		  InvocationTargetException, 
		   		  IllegalAccessException {
		
		FirmwareRepositoryEntity firmwareRepository = this.firmwareRepositoryDAO.get(firmwareRepoId);

		// Change Current Default so it is no longer the default
		FirmwareRepositoryEntity currentDefault = this.firmwareUtil.getDefaultRepo();
		if ((!checkCreatedTime || (checkCreatedTime && 
				firmwareRepository.getCreatedDate().getTime() > currentDefault.getUpdatedDate().getTime())) && 
			RepositoryState.AVAILABLE == firmwareRepository.getState()) { // Only update if it's available / not error'd
			if (currentDefault != null) {
				currentDefault.setDefault(false);
				this.genericDAO.update(currentDefault);
			}
	
			// Set the requested repo to be the default
			firmwareRepository.setDefault(true);
			this.genericDAO.update(firmwareRepository);
			
			// Update the compliance maps accordingly
			this.serviceDeploymentUtil.updateDeploymentComplianceThatAreUsingDefaultCatalog();
		}
	}

}
