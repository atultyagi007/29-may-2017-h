package com.dell.asm.asmcore.asmmanager.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetDiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetModule;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetModuleInput;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetModuleInputType;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetClientUtil;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetDbUtil;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEmcDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEquallogicDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetNetappDevice;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmdeployer.client.AsmDeployerDevice;
import com.dell.asm.asmdeployer.client.AsmDeviceStatusType;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.common.utilities.FileOperationsUtils;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

public class PuppetModuleUtil
{
    private static final Logger logger = Logger.getLogger(PuppetModuleUtil.class);
    
    private static String sOSName = System.getProperty("os.name");
    public static final String PUPPET_CONFDIR_PATH = "/etc/puppetlabs/puppet";
    public static final String DEVICE_PATH = PUPPET_CONFDIR_PATH + "/devices";
    public static final String SCRIPT_PATH = "/opt/Dell/scripts/";
    private static final String PUPPET_PATH = "puppet";
    private static final String ASM_MANAGER_CONFIG_FILE = "asm_manager.properties";
    private static final String SCRIPT_DEVICE_CONNECT_TYPE = "script";
    // NOTE[fcarta] Not a fan of hard-coding this but it was decided to be fine for now. This should probably be read
    // from a config properties file.
    public static final String CURRENT_SUPPORTED_PE_VERSION = "3.3.3";
    public static final String CURRENT_SUPPORTED_PUPPET_VERSION = "3.6.0";
    
    public static String getPuppetModuleName(DiscoverDeviceType discoverDeviceType, DeviceType deviceType) {
        return PuppetClientUtil.getPuppetModuleName(discoverDeviceType, deviceType);
    }

    // Puppet certificates must be all lower-case and they must not be all digits. We
    // tag it with the device type to ensure its not all digits.
    // discoveredDiscoveredType, deviceType and serviceTag
    public static String toCertificateName(DiscoverDeviceType discoverDeviceType, DeviceType deviceType, String serviceTag) {
        return PuppetClientUtil.toCertificateName(discoverDeviceType, deviceType, serviceTag);
    }

    public static String toCertificateName(DeviceInventoryEntity device) {
        return toCertificateName(device.getDiscoverDeviceType(), device.getDeviceType(), device.getServiceTag());
    }

    public static String toCertificateName(ManagedDevice device) {
        return toCertificateName(device.getDiscoverDeviceType(),device.getDeviceType(), device.getServiceTag());
    }

    public static List<PuppetModule> getAllPuppetModules()
    {
        List<PuppetModule> puppetModules = new ArrayList<>();

        String sPuppetModuleJson = "";
        if (sOSName != null && sOSName.contains("Windows")) // for testing ONLY on windows.
        {
            logger.debug("Using dummy values since we are running on windows.");
            sPuppetModuleJson = "{\"/usr/share/puppet/modules\":[],\"/etc/puppet/modules\":"
                    + "[\"Module dummy-activemq(/etc/puppet/modules/activemq)\","
                    + "\"Module dummy-apache(/etc/puppet/modules/apache)\","
                    + "\"Module dummy-apt(/etc/puppet/modules/apt)\","
                    + "\"Module dummy-citrix_xd7(/etc/puppet/modules/citrix_xd7)\","
                    + "\"Module dummy-cloud_provisioner(/etc/puppet/modules/cloud_provisioner)\","
                    + "\"Module dummy-concat(/etc/puppet/modules/concat)\","
                    + "\"Module dummy-tftp(/etc/puppet/modules/tftp)\","
                    + "\"Module dummy-vcenter(/etc/puppet/modules/vcenter)\","
                    + "\"Module dummy-vcsrepo(/etc/puppet/modules/vcsrepo)\","
                    + "\"Module vmware_lib(/etc/puppet/modules/vmware_lib)\","
                    + "\"Module dummy-win_desktop_shortcut(/etc/puppet/modules/win_desktop_shortcut)\","
                    + "\"Module dummy-xinetd(/etc/puppet/modules/xinetd)\"]}";
        }
        else
        {
            CommandResponse cmdresponse;
            ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
            try {
                // executing the importCustomerCert.sh command to upload the certificate
                cmdresponse = cmdRunner.runCommandWithConsoleOutput(new String[] { "/usr/bin/sudo", PUPPET_PATH, "module", "list", "--tree", "--render-as", "json" });
                //logger.debug("Return code: " + cmdresponse.getReturnCode() + " Return message: " + cmdresponse.getReturnMessage());
                sPuppetModuleJson = cmdresponse.getReturnMessage();
            }
            catch (Exception ex)
            {
                logger.error("Failed to list puppet modules", ex);
            }

        }

        try
        {
            puppetModules = parseJson( sPuppetModuleJson );
        }
        catch (Exception e)
        {
            logger.error("Failed to parse puppet modules json", e);
        }

        return puppetModules;
    }

    public static PuppetModule getPuppetModuleDetails(String sModuleName) throws Exception
    {
        PuppetModule module = new PuppetModule();
        module.setName( sModuleName );

        String sPuppetModuleParams;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
        CommandResponse cmdresponse = cmdRunner.runCommandWithConsoleOutput(new String[] { "/usr/bin/sudo", "/opt/Dell/scripts/getPuppetModuleDetails.sh" + sModuleName } );
        logger.debug("Return code: " + cmdresponse.getReturnCode() + " Return message: " + cmdresponse.getReturnMessage());
        sPuppetModuleParams = cmdresponse.getReturnMessage();

        if( sPuppetModuleParams == null )
        {
            return module;
        }

        sPuppetModuleParams = sPuppetModuleParams.trim();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map<String, Object>> params = mapper.readValue(sPuppetModuleParams, Map.class);
        for (String name: params.keySet()) {
            Map<String, Object> param = params.get(name);
            PuppetModuleInput input = new PuppetModuleInput();
            input.setInputParameter(name);

            input.setInputParameterHideFromTemplate(String.valueOf(param.get("hide")));
            input.setInputParameterRequired(String.valueOf(param.get("required")));
            String type = String.valueOf(param.get("type"));
            if (type.endsWith("Integer"))
                input.setInputParameterType(PuppetModuleInputType.INTEGER);
            else if (type.endsWith("Boolean"))
                input.setInputParameterType(PuppetModuleInputType.BOOLEAN);
            else if (type.endsWith("String"))
                input.setInputParameterType(PuppetModuleInputType.STRING);

            input.setInputParameterDefaultValue(String.valueOf(param.get("value")));

            module.getPuppetModuleInputParameters().add(input);
        }
        return module;
    }

    private static List<PuppetModule> parseJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<PuppetModule> ret = new ArrayList<>();
        Map<String, List<String>> parsedJson = mapper.readValue(json, Map.class);

        for (Map.Entry<String, List<String>> parentPathEntry : parsedJson.entrySet()) {
            for (String sModuleName : parentPathEntry.getValue()) {
                sModuleName = sModuleName.trim();
                int sModuleStrSearch = sModuleName.indexOf("Module ");
                if (sModuleStrSearch == 0) {
                    sModuleName = sModuleName.substring(6); // lenght of string "Module "
                    sModuleName = sModuleName.trim();
                }

                String directoryPath = null;
                int openParenSearch = sModuleName.indexOf("(");
                if (openParenSearch > 0) {
                    directoryPath = sModuleName.substring(openParenSearch + 1);
                    sModuleName = sModuleName.substring(0, openParenSearch);
                    sModuleName = sModuleName.trim();
                }

                if (directoryPath != null) {
                    int closeParenSearch = directoryPath.indexOf(")");
                    if (closeParenSearch > 0) {
                        directoryPath = directoryPath.substring(0, closeParenSearch);
                        directoryPath.trim();
                    }
                }

                boolean bAdd = true;
                if (AsmManagerApp.puppetModulesToFilter != null && AsmManagerApp.puppetModulesToFilter.contains(sModuleName)) {
                    bAdd = false;
                }

                if (bAdd) {
                    PuppetModule module = new PuppetModule();
                    module.setName(sModuleName);
                    module.setPath(directoryPath);
                    ret.add(module);
                }
            }
        }
        return ret;
    }

    private static Map<String, String> augmentPuppetFacts(String certName, Map<String, String> facts) {
        // TODO: this is dumb, should change callers not to use these non-existent "facts"
        facts.put("name", certName);
        if (facts.get("Version") != null) {
            facts.put("fwversion", facts.get("Version"));
        }
        return facts;
    }

    public static Map<String, String> discoverAndShowFacts(PuppetDiscoveryRequest request, boolean firstTimeDiscovery) throws Exception {
        String deviceName;
        if(request.getExistingRefId() == null)
            deviceName = request.getPuppetModuleName() + "-" + request.getIpAddress();
        else
            deviceName = request.getExistingRefId();

        logger.debug("In PuppetModuleUtil.buildDeviceConfigFile, request.getCredentialId(): " + request.getCredentialId());
        if (request.getCredentialId() != null) {
            CredentialEntity cred = getCredentialDAO().findById(request.getCredentialId());

            Map<String, String> arguments = new HashMap<>();
            arguments.put("credential_id", request.getCredentialId());
            if ("em".equals(cred.getType())) {
                arguments.put("discovery-type", "EM");
            }else if (("server".equals(cred.getType()) || "chassis".equals(cred.getType()))
                    && request.isQuickDiscovery()) {
                arguments.put("quick_discovery", "");
            }
            // host-ip is primarily used with ESX requests and should be null in other instances/requests
            if (request.getOsIpAddress() != null) {
            	arguments.put("host-ip", request.getOsIpAddress());
            }
            
            AsmDeployerDevice deviceRequest = new AsmDeployerDevice();
            deviceRequest.setCertName(deviceName);
            deviceRequest.setHost(request.getIpAddress());
            deviceRequest.setProvider(request.getPuppetModuleName().toLowerCase());
            deviceRequest.setScheme(request.getConnectType());
            deviceRequest.setArguments(arguments);
            if (StringUtils.equalsIgnoreCase(SCRIPT_DEVICE_CONNECT_TYPE,request.getConnectType())) {
                if(request.getScriptPath() == null || request.getScriptPath().isEmpty() ) {
                    deviceRequest.setPath(getScriptPath(request.getPuppetModuleName().toLowerCase()));
                }
                else {
                    deviceRequest.setPath(request.getScriptPath());
                }
            }
            if ("em".equals(cred.getType())) {
                deviceRequest.setPort("3033");
            }

            // Check if the device already exists
            IAsmDeployerService asmDeployerProxy = ProxyUtil.getAsmDeployerProxy();
            AsmDeployerDevice puppetDevice;
            try {
                puppetDevice = asmDeployerProxy.createDevice(deviceRequest);
            } catch (WebApplicationException e) {
                if (e.getResponse() == null) {
                    throw e;
                } else {
                    switch (e.getResponse().getStatus()) {
                    case 409: // CONFLICT; device already exists
                        puppetDevice = asmDeployerProxy.updateDevice(deviceName, deviceRequest);
                        break;
                    default:
                        throw e;
                    }
                }
            }

            // Poll for completion. Previous create or update will have kicked off the puppet
            // device run. Note that if there was a pre-existing update job running we will end
            // up polling on the previous run.
            int n = 0;
            while (!puppetDevice.getDiscoveryStatus().isTerminalStatus() && n < ProxyUtil.MAX_POLL_ITER) {
                Thread.sleep(ProxyUtil.POLLING_INTERVAL);
                puppetDevice = asmDeployerProxy.getDevice(deviceName);
                ++n;
            }

            if (!puppetDevice.getDiscoveryStatus().equals(AsmDeviceStatusType.SUCCESS)) {
                throw new AsmManagerRuntimeException("Puppet discovery job failed for " + deviceRequest
                        + ": " + puppetDevice);
            }

            return augmentPuppetFacts(deviceName, puppetDevice.getFacts());
        } else {
            throw new AsmManagerRuntimeException("PuppetModuleUtil.buildDeviceConfigFile aborted as no credential ID was passed.");
        }
    }

    private static String getScriptPath(String puppetProvider) {
        String scriptPath;
        String module;
        switch(puppetProvider) {
            case "dell_ftos":
            case "dell_iom" :
                module = "force10";
                break;
            case "brocade_fos" :
                module = "brocade";
                break;
            default :
                module = puppetProvider;
                break;
        }

        if ("cmc".equals(puppetProvider)) {
            scriptPath = "asm/bin/chassis-discovery.rb";
        } else if ("idrac".equals(puppetProvider)) {
            scriptPath = "asm/bin/idrac-discovery.rb";
        } else {
            scriptPath = module + "/bin/discovery.rb";
        }
        return scriptPath;
    }

    public static Map<String, String> getPuppetDevice(String certName) throws Exception {
        return augmentPuppetFacts(certName, ProxyUtil.getAsmDeployerProxy().getDevice(certName).getFacts());
    }

    public static void buildDeviceConfigFile(File file, String deviceName, PuppetDiscoveryRequest request) throws IOException {
        logger.debug("In PuppetModuleUtil.buildDeviceConfigFile, request.getCredentialId(): " + request.getCredentialId());
        if (request.getCredentialId() != null) {
            String provider = request.getPuppetModuleName().toLowerCase();
            String type = StringUtils.equalsIgnoreCase(SCRIPT_DEVICE_CONNECT_TYPE, request.getConnectType())
                    ? SCRIPT_DEVICE_CONNECT_TYPE : provider;

            CredentialEntity cred = getCredentialDAO().findById(request.getCredentialId());
            List<String> fileContents = new LinkedList<>();

            StringBuilder urlBuilder = new StringBuilder(request.getConnectType() + "://" + request.getIpAddress());
            if (SCRIPT_DEVICE_CONNECT_TYPE.equals(type)) {
                urlBuilder.append('/').append(getScriptPath(provider));
            }

            urlBuilder.append("?credential_id=").append(URLEncoder.encode(request.getCredentialId(), "UTF-8"));

            fileContents.add("[" + deviceName + "]");
            fileContents.add("  type " + type);
            fileContents.add("  url " + urlBuilder.toString());
            boolean writeResult = FileOperationsUtils.writeContentsToFile(fileContents, file.getAbsolutePath());
            logger.debug("created file : " + file.getAbsolutePath() + " with result " + writeResult);

            // After writing the config file, update the permissions and group ownership
            updateDeviceConfigFilePermissions(file);
            FileOperationsUtils.changeFileGroup(file, "pe-puppet");
        } else {
            logger.error("PuppetModuleUtil.buildDeviceConfigFile aborted as no credential ID was passed.");
        }
    }

    // TODO: remove this once puppet devices are always created with a single, consistent cert name
    public void saveDeviceConfigFile(ManagedDevice deviceInfo) {
        // create device config file
        String certName = PuppetModuleUtil.toCertificateName(deviceInfo);
        File deviceConf = new File(PuppetModuleUtil.DEVICE_PATH + "/" + certName + ".conf");
        DeviceType deviceType = deviceInfo.getDeviceType();
        DiscoverDeviceType discoverDeviceType = deviceInfo.getDiscoverDeviceType();
        String connectType;
        if (discoverDeviceType == null) {
            logger.warn("discoverDeviceType is null for " + deviceInfo);
            connectType = "https";
        } else {
            connectType = discoverDeviceType.getConnectType();
        }
        String puppetModuleName = getPuppetModuleName(discoverDeviceType, deviceType);
        PuppetDiscoveryRequest req = new PuppetDiscoveryRequest(deviceInfo.getIpAddress(),
                puppetModuleName, deviceInfo.getCredId(), connectType);
        try {
            PuppetModuleUtil.buildDeviceConfigFile(deviceConf, certName, req);
        } catch (IOException e) {
            logger.error("Could not create Puppet Device config file", e);
        }
    }

    /**
     * Changes file permissions for device config to 664
     * @param file File object
     */
    public static void updateDeviceConfigFilePermissions(File file) {
        if (file == null) {
            return;
        }
        try {
            // using PosixFilePermission to set file permissions 777
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();

            // add owners permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);

            // add group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);

            // add others permissions
            perms.add(PosixFilePermission.OTHERS_READ);

            Files.setPosixFilePermissions(Paths.get(file.getPath()), perms);
        }
        catch(IOException e) {
            logger.error("Failed to update device ocnfig permissions", e);
        }
    }


    /**
     * Retrieve puppet agent facts from puppetdb.
     *
     * @param osHostName The O/S hostname from which to calculate the puppet certificate name.
     * @return The facts as a key/value map.
     * @throws IOException if an I/O error occurs
     * @throws com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException if
     *         the agent facts are not found or an invalid response is returned.
     */
    public static Map<String, String> getPuppetAgentFacts(String osHostName) throws IOException {
        String certName = "agent-" + osHostName;
        WebClient client = WebClient.create("http://localhost:7080/v3/nodes/"
                + URLEncoder.encode(certName, Charsets.UTF_8.name()) + "/facts");
        Response response = client.get();
        if (response.getStatus() != 200) {
            throw new AsmManagerRuntimeException("Unable to retrieve facts for "
                    + certName + ": " + response);
        }

        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            try (InputStream input = (InputStream) entity) {
                String json = IOUtils.toString(input, Charsets.UTF_8.name());
                return parsePuppetDbFacts(json);
            }
        } else {
            throw new AsmManagerRuntimeException("Unknown entity returned for "
                    + certName + ": " + entity);
        }
    }

    /**
     * Parse the return value from calling the puppetdb REST API to obtain a node's facts,
     * i.e. http://localhost:7080/v3/nodes/{CERTNAME}/facts
     *
     * @param json The puppetdb facts response string
     * @return The puppet facts as key/value pairs
     */
    private static Map<String, String> parsePuppetDbFacts(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> retVal = new HashMap<>();
        retVal.put("name", "unknown");

        try {
            List<?> parsedJson = mapper.readValue(json, List.class);
            for (Object o : parsedJson) {
                if (!(o instanceof Map)) {
                    logger.warn("Invalid element in puppetdb response: " + o);
                } else {
                    Map<?, ?> elem = (Map<?, ?>) o;
                    Object okey = elem.get("name");
                    if (okey == null || !(okey instanceof String)) {
                        logger.warn("Invalid entry in puppetdb response: " + o);
                    } else {
                        Object oval = elem.get("value");
                        if (oval != null && !(oval instanceof String)) {
                            logger.warn("Invalid value in puppetdb response: " + o);
                        } else {
                            retVal.put((String) okey, (String) oval);
                        }
                    }
                }
            }

            // Populate some expected fields
            retVal.put("name", retVal.get("clientcert"));
            retVal.put("fwversion", retVal.get("Version"));
        } catch (IOException e) {
            logger.warn("Exception thrown parsing puppetdb response: " + json);
            retVal.put("value", json);
        }

        return retVal;
    }

    private static CredentialDAO credentialDAO;
    private static CredentialDAO getCredentialDAO() {
        if (null == credentialDAO) {
            credentialDAO = CredentialDAO.getInstance();
        }
        return credentialDAO;
    }

    /**
     * Retrieves the info from Puppet and then maps the Puppet Facts into a domain object representing an 
     * Equallogic device.
     * 
     * @param certName the certname used to uniquely identify the resource in Puppet.
     * @return an Equallogic device for the given certname or null if it cannot be found in the Puppet inventory.
     * @throws JsonMappingException
     *             if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException
     *             if there is an error parsing the JSON returned by puppet.
     * @throws IOException
     *             if there is an error parsing the JSON return by puppet.
     */
    public static PuppetEquallogicDevice getPuppetEquallogicDevice(String certName)
            throws JsonMappingException, JsonParseException, IOException {

        PuppetEquallogicDevice puppetEquallogicDevice = null;
        if (certName != null) {
            Map<String, String> deviceDetails = augmentPuppetFacts(certName, ProxyUtil
                    .getAsmDeployerProxy().getDevice(certName).getFacts());
            puppetEquallogicDevice = PuppetDbUtil.convertToPuppetEquallogicDevice(deviceDetails);
        }
        return puppetEquallogicDevice;
    }
    
    /**
     * Retrieves the info from Puppet and then maps the Puppet Facts into a domain object representing a 
     * Compellent device.
     * 
     * @param certName the certname used to uniquely identify the resource in Puppet.
     * @return a Compellent device for the given certname or null if it cannot be found in the Puppet inventory.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON returned by puppet.
     */
    public static PuppetCompellentDevice getPuppetCompellentDevice(String certName)
            throws JsonMappingException, JsonParseException, IOException {

        PuppetCompellentDevice puppetCompellentDevice = null;
        
        if (certName != null) {
            Map<String, String> deviceDetails = augmentPuppetFacts(certName, ProxyUtil
                    .getAsmDeployerProxy().getDevice(certName).getFacts());
            puppetCompellentDevice = PuppetDbUtil.convertToPuppetCompellentDevice(deviceDetails);
        }

        return puppetCompellentDevice;
    }
    
    /**
     * Retrieves the info from Puppet and then maps the Puppet Facts into a domain object representing a 
     * Netapp device.
     * 
     * @param certName the certname used to uniquely identify the resource in Puppet.
     * @return a Netapp device for the given certname or null if it cannot be found in the Puppet inventory.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON returned by puppet.
     */
    public static PuppetNetappDevice getPuppetNetappDevice(String certName)
            throws JsonMappingException, JsonParseException, IOException {

        PuppetNetappDevice puppetNetappDevice = null;
        
        if (certName != null) {
            Map<String, String> deviceDetails = augmentPuppetFacts(certName, ProxyUtil
                    .getAsmDeployerProxy().getDevice(certName).getFacts());
            puppetNetappDevice = PuppetDbUtil.convertToPuppetNetappDevice(deviceDetails);
        }

        return puppetNetappDevice;
    }
      
    /**
     * Retrieves the info from Puppet and then maps the Puppet Facts into a domain object representing an 
     * Emc storage device.
     * 
     * @param certName the certname used to uniquely identify the resource in Puppet.
     * @return an Emc device for the given certname or null if it cannot be found in the Puppet inventory.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON returned by puppet.
     */
    public static PuppetEmcDevice getPuppetEmcDevice(String certName)
            throws JsonMappingException, JsonParseException, IOException {

        PuppetEmcDevice puppetEmcDevice = null;
        
        if (certName != null) {
            Map<String, String> deviceDetails = augmentPuppetFacts(certName, ProxyUtil
                    .getAsmDeployerProxy().getDevice(certName).getFacts());
            puppetEmcDevice = PuppetDbUtil.convertToPuppetEmcDevice(deviceDetails);
        }

        return puppetEmcDevice;
    }    
    
    /**
     * Returns a puppet db certificate name for an esxi server from the operating system ip address.
     * 
     * @param osIpaddress ipaddress of the esxi operating system.
     * @return a valid puppdet db certificate name for looking up and return esxi puppet device if the ipaddress is not
     *      null and not empty.  Otherwise returns null.
     */
    public static String generateEsxiCertName(String osIpaddress) {
        if (osIpaddress != null && !osIpaddress.isEmpty()) {
            return "esxi-" + osIpaddress;
        }
        
        return null;
    }
    
}
