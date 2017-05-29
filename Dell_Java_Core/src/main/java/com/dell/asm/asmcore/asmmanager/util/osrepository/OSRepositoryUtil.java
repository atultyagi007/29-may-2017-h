package com.dell.asm.asmcore.asmmanager.util.osrepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.osrepository.OSRepository;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.OSRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil;
import com.dell.asm.asmcore.asmmanager.util.files.DeleteDirectoryVisitor;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OSRepositoryUtil {

    private static final Logger logger = Logger.getLogger(OSRepositoryUtil.class);
    public static final String RAZOR_SCRIPT_PATH = "/opt/Dell/scripts/razor-repo-helper.sh";
    private static final String OS_REPO_PATH = "/var/lib/razor/repo-store/";

    public OSRepositoryUtil() {}

    public void createRazorRepo(OSRepository osRepo) throws RepositoryException, IOException
    {
        LocalizableMessageService.getInstance().logMsg(
                AsmManagerMessages.createOsRepoStarted(osRepo.getName()),
                LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
        CommandResponse cmdResponse = null;
        Path downloadPath = null;
        try {
            downloadPath = Files.createTempDirectory("repo_download_");
            if (downloadPath != null) {
                getRemoteISOImage(osRepo, downloadPath.toString());
                ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
                String filePath = downloadPath.toString() + File.separator + DownloadFileUtil.getFileNameFromPath(osRepo.getSourcePath());
                String[] command = { "sudo", RAZOR_SCRIPT_PATH, osRepo.getRazorName(), osRepo.getImageType(), filePath };
                cmdResponse = cmdRunner.runCommandWithConsoleOutput(command);
                if (!cmdResponse.getReturnCode().equals("0")) {
                    LocalizableMessageService.getInstance().logMsg(
                            AsmManagerMessages.createOsRepoRazorFailed(osRepo.getName()),
                            LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
                    throw new RepositoryException("Could not create Razor repo.  Error: " + cmdResponse.getReturnMessage(), AsmManagerMessages.internalError());
                }
                LocalizableMessageService.getInstance().logMsg(
                        AsmManagerMessages.createOsRepoFinished(osRepo.getName()),
                        LogMessage.LogSeverity.INFO, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
            }
        }
        //This block is just here so we can remove the temp folder in the finally block later.  This exception should just pass through.
        catch(RepositoryException e)
        {
            throw e;
        }
        catch(Exception e){
            throw new RepositoryException("Could not create Razor repo with error: " + cmdResponse.getReturnMessage(), AsmManagerMessages.internalError(), e);
        }
        finally
        {
             try {
                 //Need to ensure the temporary folder was cleaned up
                 if (downloadPath != null && Files.exists(downloadPath)) {
                     Files.walkFileTree(downloadPath, new DeleteDirectoryVisitor());
                 }
             } catch (IOException ioe) {
                 logger.error("Could not cleanup the temp download directory!", ioe);
             }
        }
    }

    public String getRazorOsImagesJsonData(String name){
        WebClient client = WebClient.create(AsmManagerApp.razorApiUrl);
        client.accept("application/json");
        String subPath = "";
        if (name != null && !name.equals("")) {
            subPath = "/" + name;
        }
        String json = client.path("collections/repos" + subPath).get(String.class);
        return json;
    }

    public List<RazorRepo> getRazorOSImages(boolean getAll) {
        ObjectMapper mapper = new ObjectMapper();
        String json = getRazorOsImagesJsonData(null);
        List<RazorRepo> ret = new ArrayList<RazorRepo>();
        try {
            // The JSON format has changed in the latest razor release.
            // Get the repo list from the map
            HashMap<String, Object> repoMap = (HashMap<String, Object>) mapper.readValue(json, HashMap.class);
            List<Object> list = (List<Object>) repoMap.get("items");

            for (Object o : list) {
                Map elem = (Map) o;
                String spec = (String) elem.get("spec");
                String id = (String) elem.get("id");
                String name = (String) elem.get("name");
                if (StringUtils.isEmpty(spec)) {
                    logger.warn("Invalid node element " + elem + " in " + json);
                } else {
                    String repoJson = getRazorOsImagesJsonData(name);
                    Map<String,Object> repoData = (HashMap<String, Object>) mapper.readValue(repoJson, HashMap.class);
                    LinkedHashMap lhmTask = (LinkedHashMap) repoData.get("task");
                    if (lhmTask != null) {
                        String task = (String) lhmTask.get("name");
                        RazorRepo repo = new RazorRepo();
                        repo.setId(id);
                        repo.setName(name);
                        repo.setSpec(spec);
                        repo.setTask(task);
                        if (getAll || isOSRepositoryValid(name)) {
                            ret.add(repo);
                        }
                    }else{
                        logger.warn("No razor task found for repository " + name);
                    }
                }
            }

            return ret;
        } catch (IOException e) {
            logger.error("Unable to get repos from razor", e);
            throw new AsmManagerRuntimeException(e);
        }
    }

    public void validateRazorTaskSupported(String taskName) {
        WebClient client = WebClient.create(AsmManagerApp.razorApiUrl);
        client.accept("application/json");
        try {
            client.path("collections/tasks/" + taskName).get(String.class);
        }catch (WebApplicationException e) {
            logger.error("Path not found: collections/tasks/" + taskName, e);
            throw new LocalizedWebApplicationException(
                    Response.Status.CONFLICT,
                    AsmManagerMessages.unknownOsRepoRazorImageType(taskName));
        }
    }

    private void getRemoteISOImage(OSRepository osRepo, final String downloadDirectory) throws RepositoryException{
        try{
            String type = DownloadFileUtil.getType(osRepo.getSourcePath());
            String fileName = DownloadFileUtil.getFileNameFromPath(osRepo.getSourcePath());
            if (type.equals("CIFS") || type.equals("NFS")) {
                DownloadFileUtil.downloadFileFromShare(
                        osRepo.getSourcePath(),
                        fileName,
                        downloadDirectory,
                        DownloadFileUtil.getDomainfromShareUserName(osRepo.getUsername()),
                        DownloadFileUtil.getUserNamefromShareUserName(osRepo.getUsername()),
                        osRepo.getPassword(),
                        "image");
            } else{
                DownloadFileUtil.downloadFileFromURL(osRepo.getSourcePath(), fileName, downloadDirectory, "image");
            }
        }
        //Just catch the exception so we can add message to the logger
        catch(Exception e){
            LocalizableMessageService.getInstance().logMsg(
                    AsmManagerMessages.createOsRepoDownloadFailed(osRepo.getName()),
                    LogMessage.LogSeverity.ERROR, LogMessage.LogCategory.INFRASTRUCTURE_OR_HARDWARE_MONITORING);
            throw e;
        }
    }

    public void testConnection(OSRepository osRepo) throws AsmCheckedException
    {
        try {
            isValidImageFile(osRepo.getSourcePath());
            String type = DownloadFileUtil.getType(osRepo.getSourcePath());
            if (type.equals("CIFS") || type.equals("NFS")) {
                DownloadFileUtil.testConnectionToShare(
                        osRepo.getSourcePath(),
                        DownloadFileUtil.getDomainfromShareUserName(osRepo.getUsername()),
                        DownloadFileUtil.getUserNamefromShareUserName(osRepo.getUsername()),
                        osRepo.getPassword());
            } else {
                DownloadFileUtil.testConnectionToURL(osRepo.getSourcePath());
            }
        }
        catch(RepositoryException e)
        {
            throw new AsmCheckedException("Could not connect to path specified", AsmManagerMessages.couldNotConnectToPath(osRepo.getSourcePath()));
        }
    }

    public void isValidImageFile(String fullFilePath) throws AsmCheckedException{

        int lastSeparator = fullFilePath.lastIndexOf("\\"); // CIFS share
        if (lastSeparator <= 0) {
            lastSeparator = fullFilePath.lastIndexOf("/"); // NFS share
        }
        String filename = fullFilePath.substring(lastSeparator + 1);

        if (!(filename.endsWith(".iso")||filename.endsWith(".ISO"))) {
            logger.info("Image validation failed, incorrect file format specified");
            throw new AsmCheckedException("Could not connect to path specified", AsmManagerMessages.invalidFileFormat(".iso or .ISO"));
        }
    }

    public Map<String, String> mapRazorRepoNamesToAsmRepoNames(){
        Map<String, String> asmRepoMap = new HashMap<String, String>();
        GenericDAO dao = GenericDAO.getInstance();
        List<OSRepositoryEntity> asmRepos= dao.getAll(OSRepositoryEntity.class);
        for(OSRepositoryEntity osRepoEntity: asmRepos){
            asmRepoMap.put(osRepoEntity.getRazorName(), osRepoEntity.getName());
        }
        return asmRepoMap;
    }
    
    /**
     * Returns a boolean indicating if a Repository exists.  The check looks to see if the path exists and at least 
     * one file exists in the Razor repo path at the following location: /var/lib/razor/repo-store
     * 
     * @param osRepository the OSRepository that will be checked
     * @return true if the repository exists and there's at least one file in the repo or false if the the repo does not 
     *      exist and no file can be found.
     */
    public boolean doesOSRepositoryExist(OSRepositoryEntity osRepository) {
        boolean exists = false;
        
        File osRepoDir = new File(OSRepositoryUtil.OS_REPO_PATH + osRepository.getRazorName());
        if (osRepoDir.isDirectory()) {
            String[] dirContents = osRepoDir.list();
            if(dirContents != null && dirContents.length > 0) {
                exists = true;
            }
        }
        
        return exists;
    }

    public boolean isOSRepositoryValid(String name) {
        GenericDAO dao = GenericDAO.getInstance();
        OSRepositoryEntity osRepository = dao.getByName(name, OSRepositoryEntity.class);
        boolean retval = true;
        if ( osRepository != null &&
                osRepository.getState().equals(OSRepository.STATE_ERRORS) ) {
                retval = false;
        }
        return retval;
    }

}
