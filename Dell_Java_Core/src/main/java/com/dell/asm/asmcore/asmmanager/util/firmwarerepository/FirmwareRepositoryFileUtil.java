package com.dell.asm.asmcore.asmmanager.util.firmwarerepository;

import static com.dell.asm.asmcore.asmmanager.AsmManagerMessages.completedDownloadingFirmwareRepository;
import static com.dell.asm.asmcore.asmmanager.AsmManagerMessages.firmwareDownloadFailed;
import static com.dell.asm.asmcore.asmmanager.AsmManagerMessages.startedDownloadingFirmwareRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.pg.asm.catalogmgr.exceptions.CatalogException;
import com.dell.pg.asm.catalogmgr.exceptions.CatalogMessages;
import com.dell.pg.asm.repositorymgr.common.ConfigurationProperties;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.dell.pg.orion.security.encryption.EncryptionDAO;

public class FirmwareRepositoryFileUtil {
	public static final String FIRMWARE_REPO_BASE_LOCATION = "/var/nfs/firmware";
	public static final String FIRMWARE_REPO_MINIMUM_DIR = "minimum";
	public static final String FIRMWARE_TFTP_BASE_LOCATION = "/var/lib/tftpboot";

    private static final Logger logger = Logger.getLogger(FirmwareRepositoryFileUtil.class);
    private static final Object REPO_COPYING_MONITOR = new Object();
    private static final int N_BINARY_DOWNLOAD_TRIES = 4;
    public static final Pattern SIMPLE_URI_PATTERN = Pattern.compile("^([^:]+)://([^/]+)(/.*)$");

    private final GenericDAO genericDAO;
    private final EncryptionDAO encryptionDAO;
    private final LocalizableMessageService logService;

    public FirmwareRepositoryFileUtil(GenericDAO genericDAO, EncryptionDAO encryptionDAO, LocalizableMessageService logService) {
        this.genericDAO = genericDAO;
        this.encryptionDAO = encryptionDAO;
        this.logService = logService;
    }

    public FirmwareRepositoryFileUtil() {
        this(GenericDAO.getInstance(), EncryptionDAO.getInstance(), LocalizableMessageService.getInstance());
    }

    private CommandResponse runCommand(String[] cmd) throws AsmCheckedException {
        CommandResponse result;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
        try {
            result = cmdRunner.runCommandWithConsoleOutput(cmd);
        } catch (Exception e) {
            logger.error("Cmd=" + cmd + ", Exception: " + e.toString());
            throw new CatalogException("Failed executing system command: " + cmd, CatalogMessages.buildErrorMsg(CatalogMessages.CATALOG_DOWNLOAD_007,
                    ConfigurationProperties.CATALOG_FILE_XML, ConfigurationProperties.CATALOG_FILE_CAB), e);
        }
        return result;
    }

    /**
     * Extracts the specific CAB file into {@code extractionDir}. Examines the results for a valid catalog file
     * and returns that file if found.
     *
     * @param localCatalogFileName The local catalog file path
     * @param extractionDir The directory to extract the CAB file in
     * @return the extracted catalog file
     * @throws CatalogException If the CAB file is invalid or does not contain a valid catalog file.
     */
	public File processCatalogCABFile(String localCatalogFileName, File extractionDir) throws CatalogException {
        try {
            CommandResponse result = runCommand(new String[]{ConfigurationProperties.CABEXTRACT, "-t", localCatalogFileName});
            boolean isValid = false;
            if (result == null || !result.getReturnCode().equalsIgnoreCase("0"))
                throw new CatalogException(CatalogException.REASON_CODE.DOWNLOAD_FAILED, "Failed to download the catalog.",
                        CatalogMessages.buildErrorMsg(CatalogMessages.CATALOG_DOWNLOAD_008, localCatalogFileName));
            String[] commandOutput = result.getReturnMessage().split("\n");
            for (String line : commandOutput) {
                if ((line.contains(ConfigurationProperties.CATALOG_FILE_XML) || line.contains(ConfigurationProperties.CATALOG_FILE_XML.toLowerCase())) &&  (line.contains(ConfigurationProperties.CATALOG_VALID))){                
                        isValid = true;
                        break;                
                }
            }
            if (!isValid)
                throw new CatalogException(CatalogException.REASON_CODE.DOWNLOAD_FAILED, "Failed to download the catalog.",
                        CatalogMessages.buildErrorMsg(CatalogMessages.CATALOG_DOWNLOAD_008, localCatalogFileName));

        } catch (AsmCheckedException e) {
            logger.error("Failed opening local file for writing Catalog.xml", e);
            throw new CatalogException(CatalogException.REASON_CODE.DOWNLOAD_FAILED, "Failed to download the catalog.", e.getEEMILocalizableMessage());
        }

        try {
            CommandResponse result = runCommand(new String[]{ConfigurationProperties.CABEXTRACT, localCatalogFileName, "-d", extractionDir.getAbsolutePath()});
            boolean isValid = false;
            if (result == null || !result.getReturnCode().equalsIgnoreCase("0"))
                throw new CatalogException(CatalogException.REASON_CODE.DOWNLOAD_FAILED, "Failed to download the catalog.",
                        CatalogMessages.buildErrorMsg(CatalogMessages.CATALOG_DOWNLOAD_007, ConfigurationProperties.CATALOG_FILE_XML,
                                localCatalogFileName));
            String[] commandOutput = result.getReturnMessage().split("\n");
            for (String line : commandOutput) {
                if (line.contains(ConfigurationProperties.CABEXTRACT_SUCCESS)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid)
                throw new CatalogException(CatalogException.REASON_CODE.DOWNLOAD_FAILED, "Failed to download the catalog.",
                        CatalogMessages.buildErrorMsg(CatalogMessages.CATALOG_DOWNLOAD_007, ConfigurationProperties.CATALOG_FILE_XML,
                                localCatalogFileName));

            File ret = null;
            File[] files = extractionDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                        ret = file;
                        break;
                    }
                }
            }
            return ret;
        } catch (AsmCheckedException e) {
            logger.error("Failed opening local file for writing Catalog.xml", e);
            throw new CatalogException(CatalogException.REASON_CODE.DOWNLOAD_FAILED, "Failed to download the catalog.", e.getEEMILocalizableMessage());
        }
    }

    /**
     * Atomically change the firmware repository status to {@code COPYING}.
     *
     * @param firmwareRepositoryId The repository id
     * @return The updated repository entity
     * @throws java.util.ConcurrentModificationException if the state is already COPYING.
     */
    private FirmwareRepositoryEntity setRepoToCopying(String firmwareRepositoryId) {
        // TODO: it would be better to use an @Version on FirmwareRepositoryEntity to prevent concurrent
        // modifications of the entity, but I'm having trouble getting that to work. Getting hibernate
        // errors that @Version cannot be used with a subclass.. --gavin_scott 2014/09/19
        synchronized (REPO_COPYING_MONITOR) {
            FirmwareRepositoryEntity entity = genericDAO.get(firmwareRepositoryId, FirmwareRepositoryEntity.class);
            switch (entity.getDownloadStatus()) {
            case COPYING:
                throw new ConcurrentModificationException("Firmware repository " + firmwareRepositoryId
                        + " already copying");
            default:
                entity.setDownloadStatus(RepositoryStatus.COPYING);
                entity.setState(RepositoryState.COPYING);
                return genericDAO.update(entity);
            }
        }
    }

    /**
     * Set the repository status. If the repository is not found in the database, clean up repository
     * binaries on the assumption that it was deleted before this method was called.
     *
     * @param entity The firmware repository
     * @param status The status to set
     */
    private void setStatusOrDeleteBinaries(FirmwareRepositoryEntity entity, RepositoryStatus status) {
        FirmwareRepositoryEntity current = genericDAO.get(entity.getId(), FirmwareRepositoryEntity.class);
        if (current == null) {
            // Maybe repo was deleted while we were executing. Cannot set status, go ahead and clean up binaries
            logger.info("Firmware repository " + entity.getName() + " no longer exists after downloading binaries");
            deleteRepositoryBinaries(entity);
        } else {
            current.setDownloadStatus(status);
            current.setState(RepositoryState.mapStatusToState(status));
            genericDAO.update(current);
        }
    }

    /**
     * Hack to url encode urls that have not been previously url-encoded. E.g. to turn something
     * like "http://foo.com/dir with spaces" into "http://foo.com/dir%20with%20spaces". Note that
     * this is going to cause problems if it was already url-encoded!!
     *
     * @param url The url to encode
     * @return URL encoded string, or the original string if it does not look like a valid url
     */
    String encodeUnescapedUrlIfPossible(String url) {
        Matcher m = SIMPLE_URI_PATTERN.matcher(url);
        if (!m.matches()) {
            return url;
        } else {
            String scheme = m.group(1);
            String host = m.group(2);
            String path = m.group(3);
            try {
                URI uri = new URI(scheme, host, path, null);
                return uri.toString();
            } catch (URISyntaxException e) {
                logger.warn("Invalid url: " + url);
                return url;
            }
        }
    }


    /**
     * Download all firmware binaries for a repository. Will fail fast if the repository
     * is already in copying state.
     *
     * @param firmwareRepositoryId The firmware repository id
     */
    public void syncFirmwareRepository(String firmwareRepositoryId) {
        FirmwareRepositoryEntity entity = setRepoToCopying(firmwareRepositoryId);
        logger.info("Downloading binaries for firmware repository " + entity.getName());

        logService.logMsg(startedDownloadingFirmwareRepository(entity.getName()).getDisplayMessage(),
                LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);

        // Need to figure out repo type and base path
        String baseLocation = entity.getBaseLocation();
        File destination = new File(entity.getDiskLocation());
        if (!destination.isDirectory() || !destination.canWrite()) {
            throw new AsmManagerRuntimeException("Invalid catalog directory " + entity.getDiskLocation());
        }

        // Decrypt password
        String password;
        if (StringUtils.isEmpty(entity.getPassword())) {
            password = null;
        } else {
            password = encryptionDAO.findEncryptedStringById(entity.getPassword()).getString();
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firmwareRepositoryEntity", entity);
        List<SoftwareComponentEntity> components = genericDAO.getForEquals(attributes, SoftwareComponentEntity.class);
        for (SoftwareComponentEntity component : components) {
            if (component.getPath() != null) {
            
                if (component.getPath().contains("ASMNoOp")) {
            		continue;
                }
                
                String[] parts = component.getPath().split("/"); // catalog paths always separated by /

                String filename = parts[parts.length - 1];
                File parent = destination;
                for (int i = 0; i < parts.length - 1; ++i) {
                    parent = new File(parent, parts[i]);
                    if (!parent.exists()) {
                        boolean ret = parent.mkdir();
                        if (!ret) {
                            throw new AsmManagerRuntimeException("Failed to create repository directory " + parent);
                        }
                    } else if (!parent.isDirectory()) {
                        throw new AsmManagerRuntimeException("Invalid repository directory " + parent);
                    }
                }
    
                File file = new File(parent, filename);
                if (!isAlreadyDownloaded(file, component.getHashMd5())) {
                    String remotePath;
                    if (component.getRemoteProtocol() != null && !component.getRemoteProtocol().isEmpty()) { // Means it's either FTP or HTTP
                        remotePath = component.getRemoteProtocol() + StringUtils.join(parts, "/");
                    }
                    else if (baseLocation.startsWith("\\\\")) {
                        remotePath = baseLocation + "\\" + StringUtils.join(parts, "\\");
                    } else {
                        remotePath = baseLocation + "/" + StringUtils.join(parts, "/");
                    }
    
                    // Our paths may have spaces in them that should be excaped for URLs (ftp/http)
                    remotePath = encodeUnescapedUrlIfPossible(remotePath);
    
                    int nTries = 0;
                    while (nTries < N_BINARY_DOWNLOAD_TRIES) {
                        try {
                            logger.debug("Downloading " + remotePath + " for firmware repository " + entity.getName());
                            DownloadFileUtil.downloadFile(remotePath, entity.getUsername(), password, parent, "firmware");
                            nTries = N_BINARY_DOWNLOAD_TRIES; // quit
                        } catch (RepositoryException e) {
                            ++nTries;
                            String msg = "Failed to download file " + file + " from " + remotePath;
                            logger.error(msg + " (attempt #" + nTries + ")", e);
    
                            if (nTries >= N_BINARY_DOWNLOAD_TRIES) {
                                logService.logMsg(firmwareDownloadFailed(entity.getName(), remotePath).getDisplayMessage(),
                                        LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
                                setStatusOrDeleteBinaries(entity, RepositoryStatus.ERROR);
                                throw new AsmManagerRuntimeException(msg, e);
                            } else {
                                try {
                                    Thread.sleep((long)Math.pow(5, nTries)); // 1, 5, 25, 125
                                } catch (InterruptedException e1) {
                                    logger.info("Interrupted", e1);
                                }
                            }
                        }
                    }
                }
            }
        }

        setStatusOrDeleteBinaries(entity, RepositoryStatus.AVAILABLE);

        logService.logMsg(completedDownloadingFirmwareRepository(entity.getName()).getDisplayMessage(),
                LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
        logger.info("Completed downloading firmware binaries for firmware repository " + entity.getName());
    }

    private boolean isAlreadyDownloaded(File file, String expectedHashMd5) {
        if (!file.exists()) {
            return false;
        } else {
            String hashMd5;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                hashMd5 = DigestUtils.md5Hex(fis);
            } catch (FileNotFoundException e) {
                throw new AsmManagerRuntimeException("File disappeared before opening: " + file);
            } catch (IOException e) {
                throw new AsmManagerRuntimeException("Error occurred while reading file " + file, e);
            } finally {
                IOUtils.closeQuietly(fis);
            }

            return StringUtils.equalsIgnoreCase(hashMd5, expectedHashMd5);
        }
    }

    public void deleteRepositoryBinaries(FirmwareRepositoryEntity repo) {
        try {
            File diskLocation = new File(repo.getDiskLocation());
            if (diskLocation.exists()) {
                logger.debug("Deleting catalog binaries directory " + diskLocation);
                FileUtils.deleteDirectory(diskLocation);
            }
            // The puppet firmware modules
            File tftpDir = new File(FirmwareRepositoryFileUtil.FIRMWARE_TFTP_BASE_LOCATION);
            File catalogTftpDir = new File(tftpDir, repo.getId());
            if (catalogTftpDir.exists()) {
                logger.debug("Deleting catalog tftp directory " + catalogTftpDir);
                FileUtils.deleteDirectory(catalogTftpDir);
            }
        } catch (IOException e) {
            logger.error("Unable to delete firmware binaries for repository " + repo.getName()
                    + " (" + repo.getId() + ") from " + repo.getDiskLocation(), e);
        }
    }

    /**
     * Waits until the repository goes to AVAILABLE state
     *
     * @param firmwareRepositoryId the repository id
     * @param max_wait_millis the maximum time to wait in milliseconds
     */
    public void blockUntilAvailable(String firmwareRepositoryId, long max_wait_millis) {
        long start_millis = new Date().getTime();
        long elapsed_millis = 0;
        FirmwareRepositoryEntity entity = genericDAO.get(firmwareRepositoryId, FirmwareRepositoryEntity.class);
        if (entity != null)
        {
	        RepositoryStatus current = entity.getDownloadStatus();
	        while (!RepositoryStatus.AVAILABLE.equals(current) && elapsed_millis < max_wait_millis) {
	            try {
	                Thread.sleep(60 * 1000 /* 1 minute */);
	                elapsed_millis = new Date().getTime() - start_millis;
	            } catch (InterruptedException e) {
	                throw new AsmManagerRuntimeException("Interrupted waiting for repository status change", e);
	            }
	            
	            entity = genericDAO.get(firmwareRepositoryId, FirmwareRepositoryEntity.class);
	            
	            if (entity == null) {
	                throw new AsmManagerRuntimeException("Could not find firmware repository " + firmwareRepositoryId);
	            } else if (RepositoryStatus.ERROR.equals(entity.getDownloadStatus())) {
	                throw new AsmManagerRuntimeException("Firmware repository status is error");
	            } else {
	                current = entity.getDownloadStatus();
	            }
	        }
	
	        if (!RepositoryStatus.AVAILABLE.equals(current)) {
	            throw new AsmManagerRuntimeException("Timed out waiting for firmware repository " + firmwareRepositoryId);
	        }
        }
        else
        	throw new AsmManagerRuntimeException("Could not find firmware repository " + firmwareRepositoryId);
    }
}
