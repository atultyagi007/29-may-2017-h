/**************************************************************************
 *   Copyright (c) 2013 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dell.asm.asmcore.asmmanager.tasks.listener.FirmwareUpdateScheduleListener;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.jobs.NativeJob;

import com.dell.asm.asmcore.asmmanager.app.rest.AddOnModuleService;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.app.rest.ServiceTemplateService;
import com.dell.asm.asmcore.asmmanager.client.addonmodule.AddOnModuleComponentType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.UpdateDeviceInventoryResponse;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.asm.asmcore.asmmanager.client.osrepository.OSRepository;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetModule;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ApplicationModule;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ClientUtils;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleComponentsDAO;
import com.dell.asm.asmcore.asmmanager.db.AddOnModuleDAO;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.OSRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SettingEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.CreateOSRepoJob;
import com.dell.asm.asmcore.asmmanager.tasks.DeleteDeviceJob;
import com.dell.asm.asmcore.asmmanager.tasks.DeviceConfigurationJob;
import com.dell.asm.asmcore.asmmanager.tasks.DeviceInventoryJob;
import com.dell.asm.asmcore.asmmanager.tasks.DiscoverIpRangeForChassisJob;
import com.dell.asm.asmcore.asmmanager.tasks.DiscoverIpRangeJob;
import com.dell.asm.asmcore.asmmanager.tasks.FileSystemMaintenanceJob;
import com.dell.asm.asmcore.asmmanager.tasks.FirmwareRepoSyncJob;
import com.dell.asm.asmcore.asmmanager.tasks.FirmwareUpdateJob;
import com.dell.asm.asmcore.asmmanager.tasks.InitialConfigurationJob;
import com.dell.asm.asmcore.asmmanager.tasks.RazorSyncJob;
import com.dell.asm.asmcore.asmmanager.tasks.ScheduledDeploymentSyncStatusJob;
import com.dell.asm.asmcore.asmmanager.tasks.ScheduledInventoryJob;
import com.dell.asm.asmcore.asmmanager.tasks.ServiceDeploymentJob;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateValidator;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareRepositoryFileUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.ReadFirmwareRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader;
import com.dell.asm.db.ASMSetupStatusDAO;
import com.dell.asm.db.entity.ASMSetupStatusEntity;
import com.dell.asm.libext.tomcat.AsmManagerInitLifecycleListener;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.asm.usermanager.DBInit;
import com.dell.asm.usermanager.LocalUserDomain;
import com.dell.asm.usermanager.LocalUserManager;
import com.dell.asm.usermanager.LocalUserManagerFactory;
import com.dell.asm.usermanager.db.entity.UserEntity;
import com.dell.pg.asm.identitypoolmgr.impl.IdentityPoolManager;
import com.dell.pg.asm.identitypoolmgr.network.entity.NetworkConfiguration;
import com.dell.pg.jraf.client.jobmgr.JrafJobInfo;
import com.dell.pg.orion.common.context.ServiceContext;
import com.dell.pg.orion.common.utilities.ConfigurationUtils;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.IOMCredentialEntity;
import com.dell.pg.orion.security.encryption.EncryptionDAO;

/**
 * Servlet implementation class ServerApp. The loadOnStartup parameter forces this servlet to be initialized as soon as
 * it is deployed or whenever the server starts.
 */
//@WebServlet(urlPatterns = "index.html", loadOnStartup = 50)
public class AsmManagerApp extends HttpServlet {
    /* ************************************************************************* */
    /* Constants */
    /* ************************************************************************* */
    // Keep compiler happy...
    private static final long serialVersionUID = 4766356671063379956L;

    // Thread group name for ServerApp application.
    private static final String AsmManagerApp_THREADGROUP_NAME = "AsmManagerAppGroup";
    private static final String AsmManagerApp_INIT_THREAD_NAME = "AsmManagerAppAppInitializationThread";
    private static final String DEFAULT_RAZOR_SYNC_JOB_NAME = "RazorSyncJob";
    private static final String DEFAULT_RAZOR_SYNC_JOB_DESC = "Recurring razor sync job to get inventory of non-dell devices.";

    public static final String RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_NAME = "ScheduledInventoryJob";
    public static final String RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_DESC = "recurring inventory to get the latest device state";
    public static final String ASM_PORTS_TO_PING = "portsToPing";

    private static final String ASM_MANAGER_CONFIG_FILE = "asm_manager.properties";
    private static final String RAZOR_API_URL_PROPERTY = "razor_api_url";
    private static final String RAZOR_REPO_STORE_PROPERTY = "razor_repo_store";
    private static final String RAZOR_JOB_CRON_PROPERTY = "razor_job_cron";
    private static final String SCHEDULEDINVENTORY_JOB_CRON_PROPERTY = "scheduled_inventory_cron_job";
    private static final String FILE_SYSTEM_MAINTENANCE_JOB_CRON_PROPERTY = "file_system_maintenance_cron_job";
    private static final String ASM_DEPLOYER_URL_PROPERTY = "asm_deployer_url";
    private static final String PUPPET_MODULE_FILTER_PROPERTY = "puppet_module_filter_json";
    private static final String PORTS_TO_PING_PROPERTY = "ports_to_ping";
    private static final String DISCOVERY_THREAD_CONNECT_TIMEOUT_PROPERTY = "discovery_connect_timeout_seconds";
    private static final String MULTI_DEPLOYMENTS_STAGGER_SECS_KEY = "multi_deployments_stagger_secs";
    private static final long MAX_INVENTORY_POLL_MILLIS = 3600000;
    private static final long SLEEP_INVENTORY_POLL = 15000;
    private static int MULTI_DEPLOYMENTS_STAGGER_SECS;
    private static int JOB_DELAY_SECS = 5;

    public static String razorApiUrl = "";
    public static String razorRepoStoreLocation = "";
    public static String asmDeployerApiUrl = "";
    public static Set<String> puppetModulesToFilter = new HashSet<>();
    public static int CONNECT_TIMEOUT = 30000;
    public static String ASM_REPO_LOCATION = "ftp://ftp.dell.com/catalog/ASMCatalogWithDrivers.cab";

    private static String razorCron = "";
    private static String scheduledInventoryCron = "";
    private static String fileSystemMaintenanceCron = "";

    // default IOM credential
    private static final String DEFAULT_IOM_CREDENTIAL_USERNAME = "root";
    private static final String DEFAULT_IOM_CREDENTIAL_PASSWORD = "calvin";
    private static final String DEFAULT_CREDENTIAL_COMMUNITY_NAME = "public";
    private static final String DEFAULT_CREDENTIAL_PROTOCOL = "SSH";

    private ServiceTemplateService serviceTemplateService;
    private AddOnModuleService addOnModuleService;
    private ServiceTemplateValidator serviceTemplateValidator;

    private static final Logger _logger = Logger.getLogger(AsmManagerApp.class);
    private static AsmManagerAppConfig asmManagerAppConfig;

	/* ************************************************************************* */
    /* Fields */
	/* ************************************************************************* */

    private ServiceTemplateDAO templateDao;
    private DeploymentDAO deploymentDao;
    private DeviceInventoryDAO deviceInventoryDAO;
    private FirmwareRepositoryDAO firmwareRepositoryDAO;
    private AddOnModuleDAO addOnModuleDAO;
    private AddOnModuleComponentsDAO addOnModuleComponentsDAO;
    private OSRepositoryUtil osRepositoryUtil;

	/* ************************************************************************* */
	/* Constructors */
	/* ************************************************************************* */

    /** @see HttpServlet#HttpServlet() */
    public AsmManagerApp() {
        super();
    }

    /* ************************************************************************* */
	/* Public Methods */
	/* ************************************************************************* */
	@Override
	public void init() {
	    initAsmManagerApp();
	}

	/**
	 * This method gets called when the servlet is shutting down. We perform clean up operations here.
	 */
	@Override
	public void destroy() {

		// Tracing.
		_logger.info("Beginning AsmManagerApp servlet destroy() processing.");

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final Writer writer = response.getWriter();
		writer.append("<html>");
		writer.append("<body>");
		writer.append("<center><h1>AsmManagerApp Servlet</h1></center>");
        writer.append("<center><a href=\"DiscoveryRequest/?_wadl\">Discovery Service WADL</a></center>");
        writer.append("<center><a href=\"credential/?_wadl\">Credential Service WADL</a></center>");
        writer.append("<center><a href=\"DeviceConfigureRequest/?_wadl\">Configure Service WADL</a></center>");
        writer.append("<center><a href=\"ManagedDevice/?_wadl\">Device Service WADL</a></center>");
        writer.append("<center><a href=\"InfrastructureTemplate/?_wadl\">Infrastructure Template Service WADL</a></center>");
        writer.append("<center><a href=\"ServerTemplate/?_wadl\">Server Template Service WADL</a></center>");
        writer.append("<center><a href=\"ServerProfile/?_wadl\">Server Profile Service WADL</a></center>");
        writer.append("<center><a href=\"PuppetModule/?_wadl\">Puppet Module Service WADL</a></center>");
        writer.append("<center><a href=\"ServiceTemplate/?_wadl\">Service Template Service WADL</a></center>");
        writer.append("<center><a href=\"Deployment/?_wadl\">Deployment Service WADL</a></center>");
        writer.append("<center><a href=\"PuppetDevice/?_wadl\">Puppet Device Service WADL</a></center>");
		writer.append("<center><img src=\"images/giraffe5.png\" height=\"800\"; width=\"425\"></center>");
		writer.append("</body>");
		writer.append("</html>");
	}


	/* ------------------------------------------------------------------------- */
	/* doPost: */
	/* ------------------------------------------------------------------------- */
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	}

    /* ************************************************************************* */
    /* Private Methods */
    /* ************************************************************************* */
    private void initAsmManagerApp() {
        try {
            Properties props = ConfigurationUtils.resolveAndReadPropertiesFile(
                    ASM_MANAGER_CONFIG_FILE, this.getClass().getClassLoader());

            ASM_REPO_LOCATION = props.getProperty("asm_repo_location");
            razorApiUrl = props.getProperty(RAZOR_API_URL_PROPERTY);
            razorRepoStoreLocation = props.getProperty(RAZOR_REPO_STORE_PROPERTY);
            _logger.info("razorApiUrl = " + razorApiUrl);
            razorCron = props.getProperty(RAZOR_JOB_CRON_PROPERTY);
            _logger.info("razorCron = " + razorCron);
            asmDeployerApiUrl = props.getProperty(ASM_DEPLOYER_URL_PROPERTY);
            _logger.info("asmDeployerApiUrl = " + asmDeployerApiUrl);
            scheduledInventoryCron = props.getProperty(SCHEDULEDINVENTORY_JOB_CRON_PROPERTY);
            _logger.info("Scheduled Inventory cron expression =" + scheduledInventoryCron);
            fileSystemMaintenanceCron = props.getProperty(FILE_SYSTEM_MAINTENANCE_JOB_CRON_PROPERTY);
            _logger.info("Scheduled File SystemMaintenance cron expression =" + fileSystemMaintenanceCron);


            // must call as soon as we read URLs from property file
            ProxyUtil.initAsmDeployerProxy();

            final GenericDAO genericDAO = GenericDAO.getInstance();
            SettingEntity portsToPingSetting = genericDAO.getByName(ASM_PORTS_TO_PING, SettingEntity.class);
            if (portsToPingSetting == null) {
                String value = props.getProperty(PORTS_TO_PING_PROPERTY);
                if (value == null) {
                    value = "22,80,135";
                }
                portsToPingSetting = new SettingEntity();
                portsToPingSetting.setName(ASM_PORTS_TO_PING);
                portsToPingSetting.setValue(value);
                genericDAO.create(portsToPingSetting);
            }
            List<String> items = Arrays.asList(portsToPingSetting.getValue().split("\\s*,\\s*"));
            _logger.info("Ports to ping configured to " + Arrays.toString(items.toArray()) + " (" + portsToPingSetting.getValue() + ")");

            setAsmManagerAppConfig(new AsmManagerAppConfig());

            String sConnectTimeout = props.getProperty(DISCOVERY_THREAD_CONNECT_TIMEOUT_PROPERTY);
            _logger.info("sConnectTimeout = " + sConnectTimeout);
            try {
                CONNECT_TIMEOUT = Integer.parseInt(sConnectTimeout);
                CONNECT_TIMEOUT = CONNECT_TIMEOUT * 1000;
            } catch (Exception e) {
                _logger.error("Unable to parse CONNECT_TIMEOUT", e);
            }

            String staggerDeploymentsSecs = props.getProperty(MULTI_DEPLOYMENTS_STAGGER_SECS_KEY);
            try {
                MULTI_DEPLOYMENTS_STAGGER_SECS = Integer.parseInt(staggerDeploymentsSecs);
            } catch (NumberFormatException nfe) {
                _logger.error("Unable to parse MULTI_DEPLOYMENTS_STAGGER_SECS_KEY: "
                        + staggerDeploymentsSecs + ", defaulting to 30 minutes");
                MULTI_DEPLOYMENTS_STAGGER_SECS = 30 * 60;
            }

            String sPuppetModulestoFilter = props.getProperty(PUPPET_MODULE_FILTER_PROPERTY);
            _logger.info("sPuppetModulestoFilter = " + sPuppetModulestoFilter);
            String[] parts = sPuppetModulestoFilter.split(",");
            if (parts != null) {
                for (String sModule : parts) {
                    if (sModule != null) {
                        puppetModulesToFilter.add(sModule.trim());
                        _logger.info("Puppet module to filter:" + sModule.trim());
                    }
                }
            }

            // this prevents exceptions on copyProperties with null Date
            ConvertUtilsBean convertUtilsBean = BeanUtilsBean.getInstance().getConvertUtils();
            convertUtilsBean.register(false, true, -1);
        } catch (IOException e) {
            _logger.info("Exception while parsing asmmanager properties file", e);
            throw new AsmManagerRuntimeException(e);
        }

        try {
            registerJobClasses();
            createDefaultRazorSyncJob();
        } catch (Exception t) {
            _logger.error("Unable to initialize AsmManagerApp. Register Jobs Failed.", t);
        }

        // Perform all initialization on a new thread to avoid holding up server initialization.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                _logger.info("Starting AsmManagerApp initialization thread.");

                AsmManagerInitLifecycleListener.setStatus(AsmManagerInitLifecycleListener.UPDATING_INVENTORY);

                // Install Quartz Job Listeners
                try {
                    FirmwareUpdateScheduleListener scheduleListener = new FirmwareUpdateScheduleListener();
                    JobManager.getInstance().getScheduler().getListenerManager().addSchedulerListener(scheduleListener);

                    GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(FirmwareUpdateJob.class.getSimpleName());
                    JobManager.getInstance().getScheduler().getListenerManager().addJobListener(scheduleListener, groupMatcher);

                } catch (Exception t) {
                    _logger.error("Failed to install FirmwareUpdateScheduleListener", t);
                }

                try {
                    setServiceContextUser(DBInit.DEFAULT_USER);
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Set service context default user to Admin failed.", t);
                }

                try {
                    // On Startup - Where we check to see if it exists (reverse) - If file does NOT exist - Run Inventory and create the file
                    if (isRestore() || isRestartAfterApplianceUpdate()) {
                        // Run inventory on all Devices
                        runInventory();
                    }
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp.  Running Inventory Failed.", t);
                }

                AsmManagerInitLifecycleListener.setStatus(AsmManagerInitLifecycleListener.UPDATING_TEMPLATES);

                try {
                    createDefaultIOMCredential();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Creation of Default IOM Credential Failed.", t);
                }

                try {
                    updateExistingAddOnModules();
                    updateAddOnModules();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Updating AddOnModules Failed.", t);
                }

                try {
                    loadDefaultTemplates();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Loading of Default Templates Failed.", t);
                }

                try {
                    updateFirmwareRepositoryBundleComponents();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Updating Firmware Bundle Components Failed.", t);
                }
                try {
                    loadEmbeddedFirmware();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Load embedded Firmware Failed.", t);
                }
                try {
                    failCopyingAndPendingFirmwareRepositories();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Cleanup of rogue Firmware states Failed.", t);
                }

                try {
                    cleanUpDevices();
                    cleanUpJobs();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp. Cleanup of Devices and Jobs Failed.", t);
                }

                try {
                    ensureRazorReposExist();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp.  Ensuring Razor Repos Exist Failed.", t);
                }

                try {
                    syncAppStateVars();
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp.  Sync Application State Variables Failed.", t);
                }

                /**
                 * Important to run this prior to revalidating ServiceTemplates since a broken OS
                 * repository would then invalidate the template, which is the desired behavior.
                 */
                try {
                    cleanUpOsRepositories();
                } catch (Exception t) {
                    _logger.error("Problem initializing AsmManagerApp.  Cleaning up OSRepositories failed.", t);
                }

                try {
                    final ServiceTemplate defaultTemplate = getServiceTemplateService().getDefaultTemplate();
                    //build a map of add on module components for future use.
                    final List<AddOnModuleComponentEntity> addOnModuleComponentEntities = getAddOnModuleComponentsDAO().getAll(true);
                    Map<String, ServiceTemplateComponent> addOnModuleComponentsMap = new HashMap<>();
                    for (AddOnModuleComponentEntity entity : addOnModuleComponentEntities) {
                        ServiceTemplateComponent component = MarshalUtil.unmarshal(ServiceTemplateComponent.class, entity.getMarshalledData());
                        addOnModuleComponentsMap.put(component.getId(), component);
                    }

                    // Correct differences between entity object values and entity marshaledTemplateData values
                    reconcileServiceTemplateEntityData();
                    revalidateStoredTemplates(defaultTemplate, addOnModuleComponentsMap);
                    revalidateDeployedTemplates(defaultTemplate, addOnModuleComponentEntities, addOnModuleComponentsMap);
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp.  Revalidating Templates Failed.", t);
                }

                try {
                    createDefaultScheduledInventoryJob();
                    createDefaultFileSystemMaintenanceJob();
                    // please make sure there that all update deployment DAO calls made _before_ this line!!!
                    createScheduledDeploymentStatusSyncJob(JOB_DELAY_SECS);
                } catch (Exception t) {
                    _logger.error("Unable to initialize AsmManagerApp.  Creation of default jobs failed.", t);
                }

                AsmManagerInitLifecycleListener.setStatus(AsmManagerInitLifecycleListener.READY);
                _logger.info("Finished AsmManagerApp initialization.");

            }
        };

        // Run the initialization code in our thread group.
        Thread initThread = new Thread(new ThreadGroup(AsmManagerApp_THREADGROUP_NAME), runnable);
        initThread.setName(AsmManagerApp_INIT_THREAD_NAME);
        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * Correct discrepancies between the marshaledTemplateData in an entity and the
     * actual entity object values.
     *
     * NOTE: Currently this method only corrects the problem where the draft value is different
     * between the marshaledTemplateData and the actual ServiceTemplateEntity.draft field.
     *
     * This method may expand if/when we find more such discrepancies.
     */
    private void reconcileServiceTemplateEntityData() {
        // Loop over all tenplate entities, update values as necessary and update the entity in the db
        _logger.info("Entering reconcileServiceTemplateEntities");
        List<SortParamParser.SortInfo> sortInfos = new ArrayList<>();
        List<FilterParamParser.FilterInfo> filterInfos = new ArrayList<>();

        List<ServiceTemplateEntity> entities = getTemplateDao().getAllTemplates(sortInfos, filterInfos, -1, -1);

        if (!CollectionUtils.isEmpty(entities)) {
            for (ServiceTemplateEntity entity : entities) {
                boolean entityIsDirty = false;

                String marshaledTemplateData = entity.getMarshalledTemplateData();
                if (!org.apache.commons.lang.StringUtils.isBlank(marshaledTemplateData)) {
                    _logger.info("Reconciling template with id " + entity.getTemplateId());
                    ServiceTemplate unmarshaledTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, marshaledTemplateData);
                    if (unmarshaledTemplate.isDraft() != entity.isDraft()) {
                        entity.setDraft(unmarshaledTemplate.isDraft());
                        entityIsDirty = true;
                    }

                    // Update entity if necessary
                    if (entityIsDirty) {
                        getTemplateDao().updateTemplate(entity);
                        _logger.info("Template draft mode updated for template with id " + entity.getTemplateId());
                    }
                }
                else {
                    _logger.warn("Template with id " + entity.getTemplateId() + " contains no marshaledTemplateData");
                }
            }
        } else {
            _logger.info("No templates found in database to reconcile - none processed");
        }
        _logger.info("Exiting reconcileServiceTemplateEntities");
    }

    /**
     * Add authorized user to security context
     */
    private void setServiceContextUser(String userName) {
        ServiceContext.Context sc = ServiceContext.get();
        if (sc != null) {
            LocalUserManager userManager = LocalUserManagerFactory.getUserManager();
            UserEntity entity = userManager.getUser(userName, LocalUserDomain.DOMAIN_NAME);
            if (entity != null) {
                sc.setUserId(entity.getUserSeqId());
                sc.setUserName(entity.getUserName());
            }else{
                sc.setUserId((long) 1);
                sc.setUserName(DBInit.DEFAULT_USER);
            }
            sc.setApiKey("AsmManagerApp");
        }
    }

    /**
     * Create a default IOM credential if it doesn't exist
     */
    private void createDefaultIOMCredential() {
        CredentialDAO dao = CredentialDAO.getInstance();
        try {
            if (dao.findByLabel(ClientUtils.DEFAULT_IOM_CREDENTIAL_LABEL) != null)
                return;
            IOMCredentialEntity defaultCred = new IOMCredentialEntity();
            defaultCred.setLabel(ClientUtils.DEFAULT_IOM_CREDENTIAL_LABEL);
            defaultCred.setUsername(DEFAULT_IOM_CREDENTIAL_USERNAME);
            defaultCred.setPassword(DEFAULT_IOM_CREDENTIAL_PASSWORD);
            defaultCred.setType(ClientUtils.DEVICE_IOM_TYPE);
            defaultCred.setProtocol(DEFAULT_CREDENTIAL_PROTOCOL);
            defaultCred.setSnmpCommunityString(DEFAULT_CREDENTIAL_COMMUNITY_NAME);

            dao.save(defaultCred);
        } catch (PersistenceException e) {
            // error already logged in CredentialDAO
        }
    }

    public static int getMultiDeploymentsStaggerSecs() {
        return MULTI_DEPLOYMENTS_STAGGER_SECS;
    }

    private void cleanUpDevices() throws AsmManagerCheckedException {
        List<DeviceInventoryEntity> devices = getDeviceInventoryDAO().getAllDeviceInventory();
        if (devices != null) {
            for (DeviceInventoryEntity device : devices) {
                // WORKARUND: ASM-5895 to clear up Devices whose state did not go back to Discovered after teardown
                // Update all Devices that were not cleared during teardown.  
                List<DeploymentEntity> deploymentEntities = device.getDeployments();

                // If not part of a deployment change back to available/discovered
                if (deploymentEntities != null && deploymentEntities.size() == 0) {
                    if (DeviceState.DEPLOYED == device.getState() ||
                            DeviceState.DEPLOYING == device.getState() ||
                            DeviceState.DEPLOYMENT_ERROR == device.getState() ||
                            DeviceState.PENDING == device.getState() ||
                            DeviceState.PENDING_DELETE == device.getState() ||
                            DeviceState.UPDATING == device.getState()) {

                        device.setState(DeviceState.READY);
                        getDeviceInventoryDAO().updateDeviceInventory(device);
                    }
                } // If part of a deployment then change it to deployment_error
                else if (DeviceState.UPDATING == device.getState()) {
                    device.setState(DeviceState.DEPLOYMENT_ERROR);
                    getDeviceInventoryDAO().updateDeviceInventory(device);
                }
            }
        }
    }

    /**
     * Deelete stale records from discovery etc.
     * @throws AsmManagerCheckedException
     */
    private void cleanUpJobs() throws AsmManagerCheckedException
    {
        // we don't need any records from discovery tables
        DiscoveryResultDAO.getInstance().deleteAll();
        DeviceDiscoverDAO.getInstance().deleteAll();

        List<JrafJobInfo> jobList = ProxyUtil.getJobManagerProxy().getScheduledJobInfo(null, null, 999, 0);
        if (jobList!=null) {
            for (JrafJobInfo job : jobList) {
                if (job.getStartDate().before(new Date())) {
                    ProxyUtil.getJobManagerProxy().deleteScheduledJob(job.getJobKey().getName());
                }
            }
        }

    }

    private void runInventory() {
        _logger.debug("Starting Run Inventory on all resources");
        try {
            UpdateDeviceInventoryResponse[] deviceInventoryResponses = ProxyUtil.getInventoryProxy().updateDeviceInventories(null);
            if (deviceInventoryResponses != null) {
                _logger.debug(deviceInventoryResponses.length + " DeviceInventoryJobs have been started.");
            } else {
                _logger.debug("No DeviceInventoryJobs have been started.");
            }
            long start = new Date().getTime();
            long elapsed = 0L;
            IJobManager jobMgr = JobManager.getInstance();
            Set<JobKey> jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupContains(DeviceInventoryJob.class.getSimpleName()));
            while(jobKeySet != null && jobKeySet.size() > 0 && elapsed < MAX_INVENTORY_POLL_MILLIS) {
                _logger.debug(jobKeySet.size() + " DeviceInventoryJobs currently are still running");
                Thread.sleep(SLEEP_INVENTORY_POLL);
                jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupContains(DeviceInventoryJob.class.getSimpleName()));
                elapsed = new Date().getTime() - start;
            }
            _logger.debug("Finished Run Inventory DeviceInventoryJobs Running: " +
                    jobKeySet != null ? jobKeySet.size() : 0 + " elapsed time: " + elapsed);
        } catch (Exception e) {
            _logger.error("Run Inventory during upgrade or restore has failed.",e);
        }
    }

    /**
     * Check and delete the restore state file
     */
    private boolean isRestore() {
        File restoreState = new File("/opt/Dell/ASM/temp/asm.restore");
        if (restoreState.exists()) {
            _logger.debug ("Deleting the restore file.");
            try {
                restoreState.delete();
            } catch (Exception e) {
                String msg = "Unable to delete the restore state file: " + e.getMessage();
                _logger.error(msg, e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isRestartAfterApplianceUpdate() {
        File applianceUpdate = new File("/opt/Dell/ASM/temp/asm.update");
        if (!applianceUpdate.exists()) {
            _logger.debug ("Creating the appliance update file.");
            try {
                FileUtils.touch(applianceUpdate);
            } catch (Exception e) {
                String msg = "Unable to create the appliance update file: " + e.getMessage();
                _logger.error(msg, e);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update state for the following variables used by UI:
     * are templates published
     * are deployments created
     * are networks created
     */
    private void syncAppStateVars() {
        ASMSetupStatusEntity status = ASMSetupStatusDAO.getInstance().getASMSetupStatusEntity();

        List<ServiceTemplateEntity> templates = getAllNonDraftTemplateEntities();
        status.setIsTemplateCompleted(templates!=null && templates.size()>0);

        Integer numDeployments = DeploymentDAO.getInstance().getTotalRecords(new LinkedList<FilterParamParser.FilterInfo>());
        status.setIsDeploymentCompleted(numDeployments>0);

        Long totalNetworks = IdentityPoolManager.getInstance().getTotalRecords(new LinkedList<FilterParamParser.FilterInfo>(), NetworkConfiguration.class);
        status.setIsNetworkCompleted(totalNetworks>0);

        ASMSetupStatusDAO.getInstance().create(status);
    }

    private void revalidateStoredTemplates(final ServiceTemplate defaultTemplate, final Map<String, ServiceTemplateComponent> addOnModuleComponentsMap) {

        for (final ServiceTemplate svcTemplate : this.getServiceTemplateService().getAllTemplates()) {
            _logger.debug("Revalidate service template. Template Name: " + svcTemplate.getTemplateName() +
                    " Template Id: " + svcTemplate.getId());

            // some operations require security context with authorized user
            // i.e. "get server pool list"
            setServiceContextUser(svcTemplate.getCreatedBy());

            // Since older versions of ASM may have templates with components missing the originating componentId
            // from which the template was generated from we need to run this method to map this field appropriately.
            // TODO: shouldn't we just do this in ServiceTemplateService.updateTemplate?
            checkForMissingComponentIdsAndAssignIfMissing(svcTemplate);

            //Update the Associated Components for all components in the template to be populated based
            //on the components Related Components entries
            checkForMissingAssociatedComponents(svcTemplate,addOnModuleComponentsMap);

            // Update from 8.1 to 8.2 requires clearing out the storage volume names in templates
            // TODO: Temporarily removed to revert ASM-5374 for 8.2 release. Will be added back later. Please leave.
            // ServiceTemplateComponentUpgrader.clearOutNewStorageNames(svcTemplate);

            ServiceTemplateComponentUpgrader.setDefaultGateway(svcTemplate);

            cleanupFirmwareRepository(svcTemplate);

            // for 8.1.1 services may need to move network configuration from fabrics to interfaces
            ServiceTemplateComponentUpgrader.upgradeNetworkingTo82(svcTemplate, false);
            ServiceTemplateComponentUpgrader.setDefaultNTPServer(svcTemplate);
            ServiceTemplateComponentUpgrader.upgradeVSANto83(svcTemplate, defaultTemplate);
            ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(svcTemplate);

            try {
                getServiceTemplateService().updateTemplate(svcTemplate, defaultTemplate);
            } catch (WebApplicationException | AsmManagerRuntimeException e) {
                _logger.error("Failed to update template " + svcTemplate);
            }
        }

        setServiceContextUser(DBInit.DEFAULT_USER);
    }

    private void revalidateDeployedTemplates(final ServiceTemplate defaultTemplate, final List<AddOnModuleComponentEntity> addOnModuleComponentEntities, final Map<String, ServiceTemplateComponent> addOnModuleComponentsMap) {

        List<DeploymentEntity> deploymentEntityList = getAllDeploymentEntities();
        // only use ID from the list above and read each record separately to minimize concurrency
        for (DeploymentEntity refIdSource : deploymentEntityList) {

            setServiceContextUser(refIdSource.getCreatedBy());

            final DeploymentEntity deploymentEntity = getDeploymentDao().getDeployment(refIdSource.getId(), DeploymentDAO.ALL_ENTITIES);
            _logger.debug("Revalidate Deployment Service Template. Deployment Name: " + deploymentEntity.getName() +
                    " Deployment Id: "+ deploymentEntity.getId());

            if(!deploymentEntity.isBrownfield()){ // Temporary work around for brownfield until asmDeployer supports Brownfield
                final String unmarshalledTemplateData = deploymentEntity.getMarshalledTemplateData();
                if (StringUtils.isBlank(unmarshalledTemplateData)) {
                    deploymentEntity.setTemplateValid(Boolean.FALSE);
                } else {
                    final ServiceTemplate svcTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                            unmarshalledTemplateData);
                    // Since older versions of ASM may have templates with components missing the originating componentId
                    // from which the template was generated from we need to run this method to map this field appropriately.
                    checkForMissingComponentIdsAndAssignIfMissing(svcTemplate);

                    //Update the Associated Components for all components in the template to be populated based
                    //on the components Related Components entries
                    checkForMissingAssociatedComponents(svcTemplate, addOnModuleComponentsMap);

                    //Check for missing addOnModules
                    DeploymentService.updateAddOnMoulesOnDeployment(addOnModuleComponentEntities, svcTemplate, deploymentEntity);

                    if (deploymentEntity.getNamesRefs() !=  null && deploymentEntity.getNamesRefs().size() <= 0) {
                        Map<String, Set<String>> usedDeploymentNamesRefMap = DeploymentService.parseUsedDeploymentNames(svcTemplate);
                        DeploymentService.updateDeploymentNameRefsOnDeployment(deploymentEntity, usedDeploymentNamesRefMap);
                    }

                    ServiceTemplateComponentUpgrader.setDefaultGateway(svcTemplate);

                    cleanupFirmwareRepository(svcTemplate);

                    // this must go before missing params: new storage for 8.3.1
                    ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(svcTemplate);

                    // Do a template merge prior to validation
                    ServiceTemplateService.fillMissingParams(defaultTemplate, svcTemplate);

                    // for 8.1.1 services may need to move network configuration from fabrics to interfaces
                    ServiceTemplateComponentUpgrader.upgradeNetworkingTo82(svcTemplate, true);

                    ServiceTemplateComponentUpgrader.upgradeVSANto83(svcTemplate, defaultTemplate);


                    // Since older versions of ASM may have templates with components that contain confirm passwords
                    // we need to remove them
                    removeConfirmPasswordsIfPresent(svcTemplate);
                    // validate deployed template and check for errors
                    getServiceTemplateValidator().validateTemplate(svcTemplate,
                            new ServiceTemplateValidator.ValidationOptions(true, false, true));
                    // if there are any validation errors make sure deployment is set to ...
                    if (!svcTemplate.getTemplateValid().isValid()) {
                        if (ServiceTemplateClientUtil.containsUpgradableSettings(svcTemplate)) {
                            deploymentEntity.setTemplateValid(Boolean.FALSE);
                        }
                        else {
                            deploymentEntity.setTemplateValid(Boolean.TRUE);
                        }
                    } else {
                        deploymentEntity.setTemplateValid(Boolean.TRUE);
                    }
                    deploymentEntity.setMarshalledTemplateData(MarshalUtil.marshal(svcTemplate));
                }
            }

            try {
                _logger.debug("Updating deployed service after validation: " + deploymentEntity.getId());
                getDeploymentDao().updateDeployment(deploymentEntity);
            } catch (AsmManagerCheckedException | InvocationTargetException | IllegalAccessException | AsmManagerInternalErrorException e) {
                _logger.error("Error revalidating deployed service:  " + deploymentEntity.getId(), e);
            }
        }

        setServiceContextUser(DBInit.DEFAULT_USER);
    }

    private void checkForMissingComponentIdsAndAssignIfMissing(final ServiceTemplate serviceTemplate) {
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (ServiceTemplateComponentUpgrader.componentIdIsMissingOrUnknown(component)) {
                ServiceTemplateComponentUpgrader.assignOriginatingComponentId(component);
            }
        }
    }

    private void checkForMissingAssociatedComponents(final ServiceTemplate serviceTemplate, final Map<String,ServiceTemplateComponent> addOnModuleComponentsMap) {
        Map<String,ServiceTemplateComponent> componentsMap = new HashMap<>();
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            componentsMap.put(component.getId(),component);
        }
        for (Map.Entry<String,ServiceTemplateComponent> entry : componentsMap.entrySet()) {
            ServiceTemplateComponent component = entry.getValue();
            if (component.getRelatedComponents() != null && component.getRelatedComponents().size() > 0) {
                Integer i = 1;
                for (Map.Entry<String, String> related : component.getRelatedComponents().entrySet()) {
                    if (component.getAssociatedComponents().get(related.getKey()) == null) {
                        component.addAssociatedComponentName(related.getKey(),related.getValue());
                    }
                    ServiceTemplateComponent relatedComponent = componentsMap.get(related.getKey());
                    if (relatedComponent != null && ServiceTemplateComponent.ServiceTemplateComponentType.SERVICE.equals(relatedComponent.getType())) {
                        Map<String,String> associatedComponentMap = component.getAssociatedComponents().get(related.getKey());
                        if (associatedComponentMap != null) {
                            if (!associatedComponentMap.keySet().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPONENT_INSTALL_ORDER)) {
                                associatedComponentMap.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPONENT_INSTALL_ORDER, i.toString());
                                i++;
                            }
                            if (!associatedComponentMap.keySet().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPONENT_SERVICE_TYPE)) {
                                ServiceTemplateComponent addOnModuleComponent = addOnModuleComponentsMap.get(relatedComponent.getComponentID());
                                if (addOnModuleComponent != null && addOnModuleComponent.getSubType() != null) {
                                    associatedComponentMap.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPONENT_SERVICE_TYPE, addOnModuleComponent.getSubType().getLabel());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void cleanupFirmwareRepository(ServiceTemplate serviceTemplate) {
        FirmwareRepository firmwareRepository = serviceTemplate.getFirmwareRepository();
        if (firmwareRepository != null) {
            // FirmwareRepository should only have id and name set, otherwise cleanup
            if ((firmwareRepository.getDeployments() != null && firmwareRepository.getDeployments().size() > 0) ||
                    (firmwareRepository.getSoftwareComponents() != null && firmwareRepository.getSoftwareComponents().size() > 0) ||
                    (firmwareRepository.getSoftwareBundles() != null && firmwareRepository.getSoftwareBundles().size() > 0)) {
                _logger.debug("Cleaning Up Firmware Repository for Service Template " + serviceTemplate.getTemplateName() + " during startup!");
                FirmwareRepository newFirmwareRepository = new FirmwareRepository();
                newFirmwareRepository.setId(firmwareRepository.getId());
                newFirmwareRepository.setName(firmwareRepository.getName());
                serviceTemplate.setFirmwareRepository(newFirmwareRepository);
            }
        }
    }

    private void removeConfirmPasswordsIfPresent(final ServiceTemplate serviceTemplate) {
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            for (final ServiceTemplateCategory resource : component.getResources()) {
                final List<ServiceTemplateSetting> noConfirmPasswordParameters = new ArrayList<>();
                for (final ServiceTemplateSetting parameter : resource.getParameters()) {
                    // if this parameter is not a confirm password parameter then add it to the keep list
                    if (!ServiceTemplateUtil.isConfirmPassword(parameter.getId())) {
                        noConfirmPasswordParameters.add(parameter);
                    }
                }
                resource.getParameters().clear();
                resource.setParameters(noConfirmPasswordParameters);
            }
        }
    }

	/* ------------------------------------------------------------------------- */
	/* registerProviders: */
	/* ------------------------------------------------------------------------- */

    /**
     * Register any Quartz Job instances with the Job Manager
     */
    private void registerJobClasses() {
        IJobManager jobMgr = JobManager.getInstance();
        jobMgr.registerClass(DeviceInventoryJob.class);
        jobMgr.registerClass(DiscoverIpRangeJob.class);
        jobMgr.registerClass(DiscoverIpRangeForChassisJob.class);
        jobMgr.registerClass(DeleteDeviceJob.class);
        jobMgr.registerClass(FirmwareUpdateJob.class);
        jobMgr.registerClass(RazorSyncJob.class);
        jobMgr.registerClass(ServiceDeploymentJob.class);
        jobMgr.registerClass(CreateOSRepoJob.class);
        jobMgr.registerClass(FirmwareRepoSyncJob.class);
        jobMgr.registerClass(InitialConfigurationJob.class);
        jobMgr.registerClass(DeviceConfigurationJob.class);
        jobMgr.registerClass(ScheduledInventoryJob.class);
        jobMgr.registerClass(ScheduledDeploymentSyncStatusJob.class);
        jobMgr.registerClass(FileSystemMaintenanceJob.class);
        jobMgr.registerClass(NativeJob.class);
    }


    private void createDefaultScheduledInventoryJob() {
        _logger.info("Create Default Scheduled Inventory job.");
        IJobManager jobMgr = JobManager.getInstance();
        try
        {
            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(scheduledInventoryCron);
            Set<JobKey> jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(ScheduledInventoryJob.class.getSimpleName()));
            JobDetail jobDetail = null;
            Date nextRun;
            if ((jobKeySet == null) || jobKeySet.isEmpty())
            {
                jobDetail = jobMgr.createNamedJob(ScheduledInventoryJob.class, RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_NAME, null, RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_DESC);
                Trigger trigger = jobMgr.createNamedTrigger(schedBuilder, jobDetail, false);
                nextRun = jobMgr.scheduleJob(jobDetail, trigger);
            }
            else
            {
                _logger.info("ScheduledInventory job is already present.");
                for (JobKey jobKey : jobKeySet) {
                    _logger.info("Found Job Key " + jobKey.getName());
                    if (RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_NAME.equals(jobKey.getName())) {
                        jobDetail = jobMgr.getJobDetail(jobKey);
                    }
                }
                Trigger trigger = jobMgr.createNamedTrigger(schedBuilder, jobDetail, false);
                nextRun = jobMgr.rescheduleJob(RECURRING_CHECK_SCHEDULEDINVENTORY_JOB_KEY_NAME, trigger);
            }
            _logger.info("next run scheduled at: " + nextRun.toString());

            if (!jobMgr.getScheduler().isStarted()) {
                jobMgr.getScheduler().start();
                _logger.info("scheduler started");
            }
        } catch (Exception e) {
            String msg = "Failed to schedule recurring inventory job: " + e.getMessage();
            _logger.error(msg, e);
        }
    }

    private void createDefaultFileSystemMaintenanceJob() {
        _logger.info("Create Default File System Maintenance job.");
        IJobManager jobMgr = JobManager.getInstance();
        try {
            Set<JobKey> jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(FileSystemMaintenanceJob.class.getSimpleName()));
            if (jobKeySet != null && jobKeySet.size() > 0) {
                for (JobKey jobKey : jobKeySet) {
                    jobMgr.deleteJob(jobKey);
                }
            }

            // create and run startup job
            JobDetail startUpJobDetail = jobMgr.createNamedJob(FileSystemMaintenanceJob.class);
            SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
            Trigger simpleTrigger = jobMgr.createNamedTrigger(simpleScheduleBuilder, startUpJobDetail, true);
            Date nextRun = jobMgr.scheduleJob(startUpJobDetail, simpleTrigger);
            _logger.info("Start Up File System Maintenance Job scheduled at: " + nextRun.toString());

            // create cron job to run every Sunday at 1 am
            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(fileSystemMaintenanceCron);
            JobDetail chronJobDetail = jobMgr.createNamedJob(FileSystemMaintenanceJob.class);
            Trigger trigger = jobMgr.createNamedTrigger(schedBuilder, chronJobDetail, false);
            nextRun = jobMgr.scheduleJob(chronJobDetail, trigger);
            _logger.info("next run of File System Maintenance Job scheduled at: " + nextRun.toString());

            if (!jobMgr.getScheduler().isStarted()) {
                jobMgr.getScheduler().start();
                _logger.info("scheduler started");
            }
        } catch (Exception e) {
            String msg = "Failed to schedule recurring file system maintenance job: " + e.getMessage();
            _logger.error(msg, e);
        }
    }

    public static void createScheduledDeploymentStatusSyncJob(int delay) {
        _logger.info("Create Scheduled deployment status sync job.");
        IJobManager jobMgr = JobManager.getInstance();
        try
        {
            Set<JobKey> jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(ScheduledDeploymentSyncStatusJob.class.getSimpleName()));
            if ((jobKeySet != null) && !jobKeySet.isEmpty()) {
                for (JobKey jobKey : jobKeySet) {
                    _logger.info("Found Job Key " + jobKey.getName() + ", deleting");
                    jobMgr.deleteJob(jobKey);
                }
            }

            Date scheduleDate = new Date();
            scheduleDate = DateUtils.addSeconds(scheduleDate, delay);
            SimpleTriggerImpl trigger = new SimpleTriggerImpl();
            trigger.setRepeatCount(0);
            trigger.setName(UUID.randomUUID().toString());
            trigger.setGroup(ScheduledDeploymentSyncStatusJob.class.getSimpleName());
            trigger.setStartTime(scheduleDate);

            JobDetail jobDetail = jobMgr.createNamedJob(ScheduledDeploymentSyncStatusJob.class);
            Date nextRun = jobMgr.scheduleJob(jobDetail, trigger);
            _logger.info("next run for ScheduledDeploymentSyncStatusJob scheduled at: " + nextRun.toString());

            if (!jobMgr.getScheduler().isStarted()) {
                jobMgr.getScheduler().start();
                _logger.info("scheduler started");
            }
        } catch (Exception e) {
            String msg = "Failed to schedule recurring ScheduledDeploymentSyncStatusJob: " + e.getMessage();
            _logger.error(msg, e);
        }
    }

    private void createDefaultRazorSyncJob() {
        _logger.info("Create Default Razor Sync job.");
        IJobManager jobMgr = JobManager.getInstance();
        try {
            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(razorCron);

            Set<JobKey> jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(RazorSyncJob.class.getSimpleName()));
            JobDetail jobDetail = null;
            Date nextRun;
            if ((jobKeySet == null) || jobKeySet.isEmpty()) {
                _logger.info("Razor sync Job is not present.");
                jobDetail = jobMgr.createNamedJob(RazorSyncJob.class, DEFAULT_RAZOR_SYNC_JOB_NAME, null, DEFAULT_RAZOR_SYNC_JOB_DESC);
                Trigger trigger = jobMgr.createNamedTrigger(schedBuilder, jobDetail, false);
                nextRun = jobMgr.scheduleJob(jobDetail, trigger);
            } else {
                _logger.info("Razor sync Job is already present.");
                for (JobKey jobKey : jobKeySet) {
                    _logger.info("Found Job Key " + jobKey.getName());
                    jobDetail = jobMgr.getJobDetail(jobKey);
                }
                Trigger trigger = jobMgr.createNamedTrigger(schedBuilder, jobDetail, false);
                nextRun = jobMgr.rescheduleJob(DEFAULT_RAZOR_SYNC_JOB_NAME, trigger);
            }
            _logger.info("next run scheduled at: " + nextRun.toString());
            //System.out.println("next run scheduled at: " + nextRun.toString());


            if (!jobMgr.getScheduler().isStarted()) {
                jobMgr.getScheduler().start();
                _logger.info("scheduler started");
            }
        } catch (Exception e) {
            String msg = "Failed to schedule razor sync job: " + e.getMessage();
            _logger.error(msg, e);
        }
    }

    private void updateFirmwareRepositoryBundleComponents() {
        _logger.info("Attempting to updateFirmwareRepositoryBundleComponents()");
        List<FirmwareRepositoryEntity> firmwareRepositoryEntities = getFirmwareRepositoryDAO().getAll();
        if (firmwareRepositoryEntities != null && firmwareRepositoryEntities.size() > 0) {
            for (FirmwareRepositoryEntity firmwareRepositoryEntity : firmwareRepositoryEntities) {
                FirmwareRepositoryEntity completeFirmwareRepositoryEntity = getFirmwareRepositoryDAO().getCompleteFirmware(firmwareRepositoryEntity.getId(), true, true);
                Map<String, SoftwareBundleEntity> softwareBundleEntityMap = new HashMap<>();
                if (completeFirmwareRepositoryEntity.getSoftwareBundles() != null && completeFirmwareRepositoryEntity.getSoftwareBundles().size() > 0) {
                    for (SoftwareBundleEntity sbe : completeFirmwareRepositoryEntity.getSoftwareBundles()) {
                        if (sbe.getSoftwareComponents() != null && sbe.getSoftwareComponents().size() >= 0) {
                            softwareBundleEntityMap.put(sbe.getName(), sbe);
                        }
                    }
                }
                if (softwareBundleEntityMap.size() > 0) {
                    Map<String, List<SoftwareComponentEntity>> componentsMap = new HashMap<>();
                    for (SoftwareComponentEntity sce : completeFirmwareRepositoryEntity.getSoftwareComponents()) {
                        ReadFirmwareRepositoryUtil.addToMap(componentsMap, sce);
                    }
                    String filePath = completeFirmwareRepositoryEntity.getDiskLocation();
                    if (!filePath.endsWith(File.separator))
                        filePath += File.separator;
                    filePath += completeFirmwareRepositoryEntity.getFilename();
                    File catalogFile = new File(filePath);
                    ReadFirmwareRepositoryUtil.updateSoftwareBundlesByCatalog(softwareBundleEntityMap, componentsMap, catalogFile);
                    getFirmwareRepositoryDAO().saveOrUpdate(completeFirmwareRepositoryEntity);
                }
            }
        }
    }

    private void loadEmbeddedFirmware() {
        GenericDAO dao = GenericDAO.getInstance();
        HashMap<String, Object> map = new HashMap<>();
        map.put("isEmbedded", Boolean.TRUE);
        List<FirmwareRepositoryEntity> embeddedFirmwareRepositories = dao.getForEquals(map, FirmwareRepositoryEntity.class);
        if (embeddedFirmwareRepositories != null && embeddedFirmwareRepositories.size() > 0) {
            // loop through repositories and check if we need to update
            for (FirmwareRepositoryEntity repository : embeddedFirmwareRepositories) {
                _logger.info("Found embedded repository " + repository.getName());
                if (isEmbeddedFirmwareCatalogToUpdate(repository)) {
                    final File catalogFile = loadEmbeddedFirmwareCatalogFromFileSystem();
                    if (catalogFile != null) {
                        final FirmwareRepositoryEntity updatedEmbeddedFirmwareCatalog =
                                buildEmbeddedFirmwareCatalogFromCatalogFile(catalogFile);
                        // We currently don't have a version available on fw repos
                        // Checking the component count should be fine.
                        // This is a temporary fix and the minimum is only updated when
                        // we add hardware which will always increase the component count.
                        if (updatedEmbeddedFirmwareCatalog != null && repository.getComponentCount() != updatedEmbeddedFirmwareCatalog.getComponentCount()) {
                            _logger.info("The loaded minimum does not match the catalog on disk, updating.");
                            // delete the old catalog and save the new one
                            getFirmwareRepositoryDAO().merge(repository,
                                    updatedEmbeddedFirmwareCatalog);
                            new FirmwareUtil().runFirmwareComplianceCheckForAllRepositories();
                        }
                    }
                }
            }
        } else {
            // load default category from file system or classpath
            File catalogFile = loadEmbeddedFirmwareCatalogFromFileSystem();
            if (catalogFile == null) {
                // if catalog not found on the file system then try the classpath
                catalogFile = loadEmbeddedFirmwareCatalogFromClasspath();
            }

            final FirmwareRepositoryEntity embeddedFirmware = buildEmbeddedFirmwareCatalogFromCatalogFile(catalogFile);
            if (embeddedFirmware != null) {
                dao.create(embeddedFirmware);
            }
        }
    }

    private boolean isEmbeddedFirmwareCatalogToUpdate(final FirmwareRepositoryEntity repository ) {
        final String diskLocation = FirmwareRepositoryFileUtil.FIRMWARE_REPO_BASE_LOCATION + File.separator
                + FirmwareRepositoryFileUtil.FIRMWARE_REPO_MINIMUM_DIR + File.separator;
        return repository.isEmbedded()
                && StringUtils.isNotBlank(repository.getDiskLocation())
                && repository.getDiskLocation().equals(diskLocation);
    }

    private File loadEmbeddedFirmwareCatalogFromFileSystem() {
        File catalogFile = null;

        // First attempt to find a default minimum catalog already on the filesystem
        File baseDir = new File(FirmwareRepositoryFileUtil.FIRMWARE_REPO_BASE_LOCATION
                + File.separator + FirmwareRepositoryFileUtil.FIRMWARE_REPO_MINIMUM_DIR);
        if (baseDir.exists() && baseDir.isDirectory()) {
            File[] files = baseDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                        catalogFile = file;
                        break;
                    }
                }
            }
        }
        return catalogFile;
    }

    private File loadEmbeddedFirmwareCatalogFromClasspath() {
        File catalogFile = null;

        // Fall back to one from classpath; this is probably only applicable in test environments
        URL defTemplateUrl = getClass().getClassLoader().getResource("firmware");
        if (defTemplateUrl == null) {
            _logger.error("Unable to find directory for default embedded firmware");
        } else {
            _logger.debug("Directory for default service templates: " + defTemplateUrl.getPath());
            File folder = new File(defTemplateUrl.getPath());
            File[] files = folder.listFiles();
            if (files != null) {
                for (final File fileEntry : files) {
                    if (fileEntry.isDirectory()) {
                        _logger.debug("Skipping directory: " + fileEntry.getAbsolutePath());
                    } else {
                        catalogFile = fileEntry;
                        break;
                    }
                }
            }
        }
        return catalogFile;
    }

    private FirmwareRepositoryEntity buildEmbeddedFirmwareCatalogFromCatalogFile(final File catalogFile) {

        if (catalogFile == null) {
            _logger.warn("No embedded firmware catalog found");
        } else {
            _logger.info("Found embedded firmware: " + catalogFile.getAbsolutePath());
            try {
                //go ahead and load the default
                FirmwareRepositoryEntity embeddedFirmware =
                        ReadFirmwareRepositoryUtil.loadFirmwareRepositoryFromFile(catalogFile);
                String canonical = catalogFile.getCanonicalPath();
                embeddedFirmware.setDiskLocation(canonical.substring(0, canonical.lastIndexOf(File.separator) + 1));
                embeddedFirmware.setEmbedded(true);
                embeddedFirmware.setSourceLocation("Embedded");
                embeddedFirmware.setDownloadStatus(RepositoryStatus.AVAILABLE);
                embeddedFirmware.setState(RepositoryState.AVAILABLE);
                return embeddedFirmware;
            } catch (Exception e) {
                _logger.error("Exception while embedded firmware: " + catalogFile.getAbsolutePath(), e);
            }
        }

        return null;
    }

    /**
     * Mark all firmware repositories in COPYING or PENDING state as FAILED. This should only be called
     * during application startup where we are assured that no FirmwareRepoSyncJobs are running.
     */
    private void failCopyingAndPendingFirmwareRepositories() {
        failOrphanedFirmwareRepositories(RepositoryStatus.COPYING);
        failOrphanedFirmwareRepositories(RepositoryStatus.PENDING);
    }

    /**
     * Mark all firmware repositories in given state as FAILED. This should only be called
     * during application startup where we are assured that no FirmwareRepoSyncJobs are running.
     */
    private void failOrphanedFirmwareRepositories(RepositoryStatus downloadStatus) {
        try {
            GenericDAO dao = GenericDAO.getInstance();
            HashMap<String, Object> map = new HashMap<>();
            map.put("downloadStatus", downloadStatus);
            List<FirmwareRepositoryEntity> repositories = dao.getForEquals(map, FirmwareRepositoryEntity.class);
            for (FirmwareRepositoryEntity repository : repositories) {
                _logger.warn("Firmware repository in " + downloadStatus + " state on AsmManager startup: " + repository);
                repository.setDownloadStatus(RepositoryStatus.ERROR);
                repository.setState(RepositoryState.ERROR);
                dao.update(repository);
            }
        } catch (Exception e) {
            _logger.error("Failed to set copying repositories to error state", e);
        }
    }

    private void loadDefaultTemplates() {
        ServiceTemplateUtil serviceTemplateUtil = new ServiceTemplateUtil();

        URL defTemplateUrl = getClass().getClassLoader().getResource("default_service_templates");
        if (defTemplateUrl == null) {
            _logger.error("Unable to find directory for default service templates");
            return;
        }
        _logger.debug("Directory for default service templates: " + defTemplateUrl.getPath());
        File folder = new File(defTemplateUrl.getPath());

        if (folder.listFiles() == null) {
            _logger.error("Unable to read directory for default service templates: " + defTemplateUrl.getPath());
        } else {
            for (File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    _logger.debug("Skipping directory: " + fileEntry.getAbsolutePath());
                } else {
                    _logger.info("Processing service template: " + fileEntry.getAbsolutePath());
                    BufferedReader br = null;
                    String sCurrentLine;
                    String sFileContents = "";
                    try {
                        if (fileEntry.getName().endsWith(".xml")) {
                            br = new BufferedReader(new FileReader(fileEntry.getAbsolutePath()));
                            while ((sCurrentLine = br.readLine()) != null) {
                                sFileContents = sFileContents + sCurrentLine;
                                // System.out.println(sCurrentLine);
                            }
                            final ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, sFileContents);
                            if (template != null) {
                                String templateId = null;
                                ServiceTemplateEntity tInDB = getTemplateDao().getTemplateByName(template.getTemplateName());
                                if ((tInDB != null) && areDifferentTemplateVersions(template, tInDB)) {
                                    // if the versions do not match then delete old version for new version to be saved
                                    getTemplateDao().deleteTemplate(tInDB.getTemplateId());
                                    tInDB = null;
                                }

                                // if the template is not found or has been deleted for new version
                                if (tInDB == null) {
                                    template.assignUniqueIDs();
                                    serviceTemplateUtil.encryptPasswords(template);
                                    final ServiceTemplateEntity entity = ServiceTemplateService.createTemplateEntity(template);
                                    getTemplateDao().createTemplate(entity);
                                    templateId = entity.getTemplateId();
                                }else{
                                    templateId = tInDB.getTemplateId();
                                }

                                File attachmentFile = new File (fileEntry.getParentFile().getAbsolutePath() + File.separator +
                                        FilenameUtils.getBaseName(fileEntry.getName()) + ".pdf");
                                ServiceTemplateUtil.addOrReplaceAttachment(templateId, attachmentFile);

                            }
                        }
                    } catch (Exception e) {
                        _logger.error("Exception while saving template: " + fileEntry.getAbsolutePath(), e);
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e) {
                                _logger.error("Exception while closing bufferred reader for file: "
                                        + fileEntry.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean areDifferentTemplateVersions(final ServiceTemplate template,
            final ServiceTemplateEntity templateEntity) {
        return (!StringUtils.equals(template.getTemplateVersion(), templateEntity.getTemplateVersion()));
    }

    private List<ServiceTemplateEntity> getAllNonDraftTemplateEntities() {
        final List<String> filter = new ArrayList<>();
        filter.add("eq,draft,false");
        final FilterParamParser filterParser = new FilterParamParser(filter, ServiceTemplateService.validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        return ServiceTemplateDAO.getInstance().getAllTemplates(
                new LinkedList<SortParamParser.SortInfo>(), filterInfos, -1, -1);
    }

    private void ensureRazorReposExist() throws JSONException {
        List<String> repos = new ArrayList<>();
        for(RazorRepo repo : getOsRepositoryUtil().getRazorOSImages(true)){
            repos.add(repo.getName());
        }
        WebClient razorClient = WebClient.create(razorApiUrl + "/commands/create-repo");//, providers);
        razorClient.type(MediaType.APPLICATION_JSON);
        razorClient.accept(MediaType.APPLICATION_JSON);
        String[] esxiRepos = {"esxi-5.1", "esxi-5.5", "esxi-6.0", "esxi-6.5"};
        for(String esxiRepo : esxiRepos){
            if(!repos.contains(esxiRepo)) {
                InputStream stream = null;
                try {
                    _logger.info("Creating missing Razor repo: " + esxiRepo);
                    JSONObject data = new JSONObject();
                    data.put("name", esxiRepo);
                    data.put("task", "vmware_esxi");
                    data.put("iso-url", "file:///dev/null");
                    Response response = razorClient.post(data.toString());
                    if(response.getStatus() != 200 && response.getStatus() != 202){
                        stream = (InputStream) response.getEntity();
                        String output = IOUtils.toString(stream);
                        stream.close();
                        JSONObject responseObject = new JSONObject(output);
                        throw new Exception((String) responseObject.get("error"));
                    }
                }
                catch(Exception e){
                    _logger.error("Could not create new repo " + esxiRepo, e);
                }
                finally{
                    IOUtils.closeQuietly(stream);
                }
            }
        }
    }

    private void updateExistingAddOnModules() throws IllegalAccessException, AsmManagerCheckedException, InvocationTargetException {
        List<AddOnModuleEntity> addOnModuleEntities = getAddOnModuleDAO().getAll(true);
        for (AddOnModuleEntity entity : addOnModuleEntities) {
            boolean changed = false;
            if (entity.getMarshalledTypesData() != null) {
                ServiceTemplate typesTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, entity.getMarshalledTypesData());
                if (typesTemplate != null && CollectionUtils.isNotEmpty(typesTemplate.getComponents())) {
                    for (ServiceTemplateComponent component : typesTemplate.getComponents()) {
                        AddOnModuleComponentEntity aOMCEComponent = new AddOnModuleComponentEntity();
                        aOMCEComponent.setName(component.getName());
                        aOMCEComponent.setType(AddOnModuleComponentType.TYPE);
                        aOMCEComponent.setAddOnModuleEntity(entity);
                        aOMCEComponent.setMarshalledData(MarshalUtil.marshal(component));
                        entity.getAddOnModuleComponents().add(aOMCEComponent);
                    }
                }
                entity.setMarshalledTypesData(null);
                changed = true;
            }
            if (entity.getMarshalledClassesData() != null) {
                ServiceTemplate classesTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, entity.getMarshalledClassesData());
                if (classesTemplate != null && CollectionUtils.isNotEmpty(classesTemplate.getComponents())) {
                    for (ServiceTemplateComponent component : classesTemplate.getComponents()) {
                        AddOnModuleComponentEntity aOMCEComponent = new AddOnModuleComponentEntity();
                        aOMCEComponent.setName(component.getName());
                        aOMCEComponent.setType(AddOnModuleComponentType.CLASS);
                        aOMCEComponent.setAddOnModuleEntity(entity);
                        aOMCEComponent.setMarshalledData(MarshalUtil.marshal(component));
                        entity.getAddOnModuleComponents().add(aOMCEComponent);
                    }
                }
                entity.setMarshalledClassesData(null);
                changed = true;
            }
            if (changed) {
                getAddOnModuleDAO().update(entity);
            }
        }

    }

    private void updateAddOnModules() {

        Map<String,AddOnModuleEntity> addOnModules = new HashMap<>();
        try {
            List<AddOnModuleEntity> persistedAddOnModules = getAddOnModuleDAO().getAll(false);
            for (AddOnModuleEntity entity : persistedAddOnModules) {
                addOnModules.put(entity.getModulePath(),entity);
            }
        } catch (Exception e) {
            _logger.error("Retrieval of all addOnModules failed.",e);
        }

        try {
            List<PuppetModule> puppetModules = PuppetModuleUtil.getAllPuppetModules();
            for (PuppetModule module : puppetModules) {
                AddOnModuleEntity entity = addOnModules.get(module.getPath());
                if (entity == null) {
                    try {
                        entity = addAddOnModuleFromFileSystem(module);
                        if (entity != null) {
                            addOnModules.put(entity.getModulePath(),entity);
                        }
                    } catch (LocalizedWebApplicationException lwe) {
                        _logger.error("A validation exception occurred parsing module " + module.getName(),lwe);
                    }
                } else {
                    updateAddOnModuleFromFileSystem(entity,module);
                }
            }
        } catch (Exception e) {
            _logger.error("An Error occurred retrieving all puppet modules",e);
        }
     }

    private AddOnModuleEntity addAddOnModuleFromFileSystem(PuppetModule puppetModule) {
        AddOnModuleEntity addOnModuleEntity = null;
        ApplicationModule applicationModule = null;
        String filePath = null;
        Path pathToFile;
        byte[] bytes;
        String addOnModuleName = null;
        String asmInputHash = null;
        String metadataHash = null;
        if (puppetModule.getName() != null && puppetModule.getPath() != null) {
            try {
                filePath = puppetModule.getPath() + File.separator + AddOnModuleService.ASM_INPUT_CONFIG_FILE_NAME;
                pathToFile = Paths.get(filePath);
                if (Files.exists(pathToFile)) {
                    bytes = Files.readAllBytes(pathToFile);
                    if (bytes != null) {
                        asmInputHash = DigestUtils.md5Hex(bytes);
                        applicationModule = AddOnModuleService.OBJECT_MAPPER.readValue(new String(bytes), ApplicationModule.class);
                    }
                }
                filePath = puppetModule.getPath() + File.separator + AddOnModuleService.METADATA_CONFIG_FILE_NAME;
                pathToFile = Paths.get(filePath);
                if (Files.exists(pathToFile)) {
                    bytes = Files.readAllBytes(pathToFile);
                    if (bytes != null) {
                        metadataHash = DigestUtils.md5Hex(bytes);
                        ApplicationModule metadataApplicationModule = AddOnModuleService.OBJECT_MAPPER.readValue(new String(bytes), ApplicationModule.class);
                        if (applicationModule == null) {
                            applicationModule = new ApplicationModule();
                        }
                        if (metadataApplicationModule != null) {
                            applicationModule.merge(metadataApplicationModule);
                        }
                    }
                }
                if (applicationModule != null) {
                    addOnModuleName = AddOnModuleService.parseAddOnModuleName(applicationModule.getName());
                }
            } catch (IOException e) {
                _logger.error("Could not find " + filePath + " on the file system.", e);
            }

            if (applicationModule != null) {
                // validate required configuration fields are present in metadata or asm_input files
                getAddOnModuleService().validateApplicationModule(applicationModule, false);

                boolean defaultModule = false;
                if (puppetModule.getPath().startsWith("/etc/puppetlabs/puppet/modules")) {
                    defaultModule = true;
                }
                addOnModuleEntity = getAddOnModuleService().createAddOnModuleEntity(applicationModule,
                                                                               addOnModuleName,
                                                                               puppetModule.getPath(),
                                                                               defaultModule,
                                                                               asmInputHash,
                                                                               metadataHash);
            }
        }
        return addOnModuleEntity;
    }

    private void updateAddOnModuleFromFileSystem(AddOnModuleEntity addOnModuleEntity, PuppetModule puppetModule) {
        ApplicationModule applicationModule = null;
        String filePath = null;
        if (puppetModule.getName() != null && puppetModule.getPath() != null) {
            try {
                filePath = puppetModule.getPath() + File.separator + AddOnModuleService.ASM_INPUT_CONFIG_FILE_NAME;
                Path pathToFile = Paths.get(filePath);
                byte[] asmInputBytes = null;
                String asmInputHash = null;
                if (Files.exists(pathToFile)) {
                    asmInputBytes = Files.readAllBytes(pathToFile);
                    if (asmInputBytes != null) {
                        asmInputHash = DigestUtils.md5Hex(asmInputBytes);
                    }
                }
                filePath = puppetModule.getPath() + File.separator + AddOnModuleService.METADATA_CONFIG_FILE_NAME;
                pathToFile = Paths.get(filePath);
                byte[] metadataBytes = null;
                String metadataHash = null;
                if (Files.exists(pathToFile)) {
                    metadataBytes = Files.readAllBytes(pathToFile);
                    if (metadataBytes != null) {
                        metadataHash = DigestUtils.md5Hex(metadataBytes);
                    }
                }
                if (!addOnModuleHashesMatch(addOnModuleEntity.getAsmInputHash(), asmInputHash) ||
                        !addOnModuleHashesMatch(addOnModuleEntity.getMetadataHash(), metadataHash)) {
                    if (asmInputBytes != null && asmInputBytes.length > 0) {
                        applicationModule = AddOnModuleService.OBJECT_MAPPER.readValue(new String(asmInputBytes), ApplicationModule.class);
                    }
                    if (metadataBytes != null && metadataBytes.length > 0) {
                        ApplicationModule metadataApplicationModule = AddOnModuleService.OBJECT_MAPPER.readValue(new String(metadataBytes), ApplicationModule.class);
                        if (applicationModule == null) {
                            applicationModule = new ApplicationModule();
                        }
                        if (metadataApplicationModule != null) {
                            applicationModule.merge(metadataApplicationModule);
                        }
                    }
                    if (applicationModule != null) {
                        // validate required configuration fields are present in metadata or asm_input files
                        getAddOnModuleService().validateApplicationModule(applicationModule, true);
                        getAddOnModuleService().updateAddOnModuleEntity(addOnModuleEntity.getId(),
                                                                   applicationModule,
                                                                   puppetModule.getPath(),
                                                                   asmInputHash,
                                                                   metadataHash);
                    }
                }
            } catch (IOException e) {
                _logger.error("Could not find " + filePath + " on the file system.", e);
            } catch (Exception ex) {
                _logger.error("Could not update AddOnModule " + puppetModule.getPath(), ex);
            }
        }
    }

    private boolean addOnModuleHashesMatch(String entityHash, String calculatedHash) {
        if (entityHash == null && calculatedHash == null) {
            return true;
        }
        if (entityHash != null && calculatedHash == null) {
            return false;
        }
        if (calculatedHash != null && entityHash == null) {
            return false;
        }
        return calculatedHash.equals(entityHash);
    }

    private List<DeploymentEntity> getAllDeploymentEntities() {
        return DeploymentDAO.getInstance().getAllDeployment(DeploymentDAO.ALL_ENTITIES);
    }

    public static synchronized AsmManagerAppConfig getAsmManagerAppConfig() {
        return AsmManagerApp.asmManagerAppConfig;
    }

    public static synchronized void setAsmManagerAppConfig(AsmManagerAppConfig asmManagerAppConfig) {
        AsmManagerApp.asmManagerAppConfig = asmManagerAppConfig;
    }

    public static Integer[] getPortsToPing() {
        return getAsmManagerAppConfig().getPortsToPing();
    }

    // Ensure the OS Repositories are in a proper state for use in ASM
    private void cleanUpOsRepositories() {

        GenericDAO genericDAO = GenericDAO.getInstance();
        for (OSRepositoryEntity osRepository : genericDAO.getAll(OSRepositoryEntity.class)) {

            // If anything was in progress or due to be copied set it to error so it can be sync'd by user
            if (OSRepository.STATE_COPYING.equals(osRepository.getState()) ||
                    OSRepository.STATE_PENDING.equals(osRepository.getState())) {
                osRepository.setState(OSRepository.STATE_ERRORS);
                genericDAO.update(osRepository);
            }
            // If anything shows available, make sure at least one file exists so we know we did not lose it
            // in a backup and restore process
            if (OSRepository.STATE_AVAILABLE.equals(osRepository.getState())) {
                if(!getOsRepositoryUtil().doesOSRepositoryExist(osRepository)) {
                    osRepository.setState(OSRepository.STATE_ERRORS);
                    genericDAO.update(osRepository);
                }
            }
        }
    }

    private void cleanupOldJobs(IJobManager jobMgr, String jobSimpleName) throws SchedulerException {
        Set<JobKey> jobKeySet = jobMgr.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(jobSimpleName));
        if ((jobKeySet != null) && !jobKeySet.isEmpty()) {
            for (JobKey jobKey : jobKeySet) {
                _logger.info("Found Job Key " + jobKey.getName() + ", deleting");
                jobMgr.deleteJob(jobKey);
            }
        }
    }

    public ServiceTemplateValidator getServiceTemplateValidator() {
        if (serviceTemplateValidator == null) {
            serviceTemplateValidator = new ServiceTemplateValidator();
        }
        return serviceTemplateValidator;
    }

    public void setServiceTemplateValidator(ServiceTemplateValidator serviceTemplateValidator) {
        this.serviceTemplateValidator = serviceTemplateValidator;
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

    public AddOnModuleService getAddOnModuleService() {
        if (addOnModuleService == null) {
            addOnModuleService = new AddOnModuleService();
        }
        return addOnModuleService;
    }

    public void setAddOnModuleService(AddOnModuleService addOnModuleService) {
        this.addOnModuleService = addOnModuleService;
    }

    public ServiceTemplateDAO getTemplateDao() {
        if (templateDao == null) {
            templateDao = ServiceTemplateDAO.getInstance();
        }
        return templateDao;
    }

    public void setTemplateDao(ServiceTemplateDAO templateDao) {
        this.templateDao = templateDao;
    }

    public DeploymentDAO getDeploymentDao() {
        if (deploymentDao == null) {
            deploymentDao = DeploymentDAO.getInstance();
        }
        return deploymentDao;
    }

    public void setDeploymentDao(DeploymentDAO deploymentDao) {
        this.deploymentDao = deploymentDao;
    }

    public DeviceInventoryDAO getDeviceInventoryDAO() {
        if (deviceInventoryDAO == null) {
            deviceInventoryDAO = new DeviceInventoryDAO();
        }
        return deviceInventoryDAO;
    }

    public void setDeviceInventoryDAO(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO = deviceInventoryDAO;
    }

    public FirmwareRepositoryDAO getFirmwareRepositoryDAO() {
        if (firmwareRepositoryDAO == null) {
            firmwareRepositoryDAO = FirmwareRepositoryDAO.getInstance();
        }
        return firmwareRepositoryDAO;
    }

    public void setFirmwareRepositoryDAO(FirmwareRepositoryDAO firmwareRepositoryDAO) {
        this.firmwareRepositoryDAO = firmwareRepositoryDAO;
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
