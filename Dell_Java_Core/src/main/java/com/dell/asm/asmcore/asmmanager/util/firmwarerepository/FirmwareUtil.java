/**************************************************************************
 *   Copyright (c) 2015 - 2016 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.firmwarerepository;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceComponent;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReport;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReportComponent;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReportComponentVersionInfo;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.ComponentType;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.asm.asmcore.asmmanager.client.firmware.SourceType;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Partition;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetDiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetDbUtil;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetIdracServerDevice;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity.DeviceInventoryComplianceId;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.DeviceInventoryJob;
import com.dell.asm.asmcore.asmmanager.tasks.FirmwareUpdateJob;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.ResourceBundleLocalizableMessage;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage.LogCategory;
import com.dell.asm.localizablelogger.LogMessage.LogSeverity;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.catalogmgr.exceptions.CatalogMessages;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.common.utilities.VersionUtils;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobCreateSpec;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Compliance is a bit complicated.  The primary things you need to understand Firmware is an understanding of
 * a Firmware Catalog (represented as a FirmwareRepository in ASM), Device Inventory (stored in the 
 * firmware_deviceinventory table), and the 'mapping' between these two which is primarily stored in the 
 * 'device_inventory_compliance_map' with a value propagated to the 'device_inventory' compliance field.<br />
 * <br />
 * <b>Firmware\Hardware Compliance</b><br />
 * Firmware compliance was initially calculated only based on the firmware components and their corresponding catalog
 * entries.  Software was only recently added, and required some changes to be supported.  See firmware compliance is 
 * only calculated if there is an entry in the firmware_deviceinventory table and a corresponding component found in
 * the Firmware Catalog. <br />
 * <br />
 * <b>Software Compliance</b><br />
 * Software compliance is calculated the same and yet differently.  When software information is available in the
 * firmware_deviceinventory table, then compliance is calculated the same.  However, when a Catalog is set on a 
 * server, and there are software entries in the catalog, that do not have a matching value in the 
 * firmware_deviceinventory, then we create a 'new' entry in the table, and then mark it's 'source' as Catalog.  Only 
 * software items in the firmware_deviceinventory table should be marked as catalog.  No others.  When a entry in
 * the firmware_deviceinventory table is marked as 'Catalog' for it's source, then it will automatically be considered
 * out of compliance, as no match can be found.<br />  
 * <br />
 * <b>Compliance Mappings</b><br />
 * The compliance_map table in the database contains a compliance mapping of all devices against all repositories.  Once 
 * the compliant_map entries are complete for a device, then, depending on whether a device is part of a service or not, 
 * the appropriate compliance value is propagated to the device_inventory table.  Devices are broken down into 
 * shared and non-shared (think Servers and Storages).  A shared resource (such as a Storage) may be part of multiple 
 * Services/Deployments where as a non-shared device (such as a Server) can only be be part of a single 
 * Service/Deployment at any given point in time.  A non-shared device's compliance in the device_inventory table will
 * either be the default catalog or that of the Service the device part of at the time.  A shared resource's compliance
 * value will always represent the value of the default catalog.  A shared resource will NEVER have a Service's 
 * compliance level propagated to the device_inventory table.  How could it?  There's only one entry there, and a 
 * shared resource could be in a dozen different Services/Deployments.  
 */
public class FirmwareUtil {
    
    // Class Variables
    private static final Logger logger = Logger.getLogger(FirmwareUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();
    private static final String COMPONENT_TYPE_DRIVER = "Driver";
    private static final String NOT_AVAILABLE = "NA";
    private static final Comparator COMPLIANCE_REPORT_COMPARATOR = new Comparator<FirmwareComplianceReport>() {

        public int compare(FirmwareComplianceReport fcr1, FirmwareComplianceReport fcr2){
            if(fcr1.getDeviceType().isStorage() && fcr2.getDeviceType().isServer()) return 1;
            if(fcr1.getDeviceType().isServer() && fcr2.getDeviceType().isStorage()) return -1;
            return fcr1.getIpAddress().compareTo(fcr2.getIpAddress());
        }
    };
    
    // Member Variables
    private FirmwareRepositoryDAO firmwareRepositoryDAO = null;
    private DeviceInventoryDAO deviceInventoryDAO = null;
    private DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = null;
    private DeploymentDAO deploymentDAO = null;
    private GenericDAO genericDAO = null;
    private DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.dateTimeParser();
    private Comparator<SoftwareComponent> componentVersionComparator = new Comparator<SoftwareComponent>() {
        @Override
        public int compare(SoftwareComponent sc1, SoftwareComponent sc2) {
            // Note that sc1 and sc2 are 'reversed' below
            // so it will sort in descending order. This
            // will put the 'latest' version in the first
            // list entry.
            return VersionUtils.compareVersions(sc2.getVendorVersion(), sc1.getVendorVersion());
        }
    };
    private IJobManager jobMgr = null;
    private IJobHistoryManager historyMgr = null;

    
    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }

    /**
     * Default constructor for the class.
     */
    public FirmwareUtil() {
        this(FirmwareRepositoryDAO.getInstance(),
                new DeviceInventoryDAO(),
                new DeviceInventoryComplianceDAO(),
                DeploymentDAO.getInstance(),
                GenericDAO.getInstance(),
                JobManager.getInstance(),
                JobManager.getInstance().getJobHistoryManager());
    }
    
    /**
     * Constructor to set the FirmwareUpdateUtil used by the class.
     */
    public FirmwareUtil(FirmwareRepositoryDAO firmwareRepositoryDAO,
            DeviceInventoryDAO deviceInventoryDAO,
            DeviceInventoryComplianceDAO deviceInventoryComplianceDAO,
            DeploymentDAO deploymentDAO,
            GenericDAO genericDAO,
            IJobManager jobManager,
            IJobHistoryManager jobHistoryManager) {
        this.firmwareRepositoryDAO = firmwareRepositoryDAO;
        this.deviceInventoryDAO = deviceInventoryDAO;
        this.deviceInventoryComplianceDAO = deviceInventoryComplianceDAO;
        this.deploymentDAO = deploymentDAO;
        this.genericDAO = genericDAO;
        this.jobMgr = jobManager;
        this.historyMgr = jobHistoryManager;
    }
    
    public void testConnection(FirmwareRepository firmwareRepository) throws AsmCheckedException {
        try {
            String type = DownloadFileUtil.getType(firmwareRepository.getSourceLocation());
            if (type.equals(DownloadFileUtil.CIFS) || type.equals(DownloadFileUtil.NFS)) {
                DownloadFileUtil.testConnectionToShare(
                        firmwareRepository.getSourceLocation(),
                        DownloadFileUtil.getDomainfromShareUserName(firmwareRepository.getUsername()),
                        DownloadFileUtil.getUserNamefromShareUserName(firmwareRepository.getUsername()),
                        firmwareRepository.getPassword());
            } else {
                DownloadFileUtil.testConnectionToURL(firmwareRepository.getSourceLocation());
            }
        }
        catch(RepositoryException e)
        {
            throw new AsmCheckedException("Could not connect to path specified", AsmManagerMessages.couldNotConnectToPath(firmwareRepository.getSourceLocation()));
        }
    }    
    
    public FirmwareRepository entityToDto(final FirmwareRepositoryEntity entity, final boolean getDeployments) {
        FirmwareRepository repository = new FirmwareRepository();
        repository.setSourceLocation(entity.getSourceLocation());
        repository.setSourceType(entity.getSourceType());
        repository.setDiskLocation(entity.getDiskLocation());
        repository.setFilename(entity.getFilename());
        repository.setMd5Hash(entity.getMd5Hash());
        repository.setUsername(entity.getUsername());
        repository.setPassword(entity.getPassword());
        repository.setDownloadStatus(entity.getDownloadStatus());
        repository.setDefaultCatalog(entity.isDefault());
        repository.setEmbedded(entity.isEmbedded());
        repository.setState(entity.getState());
        repository.setBundleCount(entity.getBundleCount());
        repository.setUserBundleCount(entity.getUserBundleCount());
        repository.setComponentCount(entity.getComponentCount());

        repository.setCreatedBy(entity.getCreatedBy());
        repository.setCreatedDate(entity.getCreatedDate());
        repository.setId(entity.getId());
        repository.setName(entity.getName());
        repository.setUpdatedBy(entity.getUpdatedBy());
        repository.setUpdatedDate(entity.getUpdatedDate());

        if (getDeployments) {
            for (DeploymentEntity d : entity.getDeployments()) {
                repository.getDeployments().add(d.getDeployment());
            }
        }
        
        try {
            for (SoftwareBundleEntity sbe : entity.getSoftwareBundles()) {
                repository.getSoftwareBundles().add(sbe.getSoftwareBundle());
            }
        } catch (LazyInitializationException e) {
            // Avoiding due to excessive logging
            // logger.warn("Error initializing SoftwareBundles in FirmwareUtil's entityToDTO method for repository with name: " + repository.getName());
        }
        
        try {
            for (SoftwareComponentEntity sc : entity.getSoftwareComponents()) {
                repository.getSoftwareComponents().add(sc.getSoftwareComponent());
            }
        } catch (LazyInitializationException e) {
            // Avoiding due to excessive logging
            // logger.warn("Error initializing SoftwareComponents in FirmwareUtil's entityToDTO method for repository with name: " + repository.getName());
        }
        
        return repository;
    }
    
    
    /**
     * Takes a deployment adds all the necessary firmware update components to the service template. Either all servers 
     * in the case of a service deployment or Devices marked as managefirmware from a getting started configuration
     *
     * @param firmwareRepository
     * @param deployment
     * @param allServers
     * @throws AsmManagerCheckedException 
     */
    public List<DeviceInventoryEntity> addFirmwareUpdateComponentsToDeployment(final FirmwareRepositoryEntity firmwareRepository,
                                                                               final Deployment deployment,
                                                                               final boolean allServers) throws AsmManagerCheckedException {

        List<DeviceInventoryEntity> updatedDevices =
                getFirmwareUpdateDevicesInDeployment(firmwareRepository, deployment, allServers, DriverType.ALL);
        List<ServiceTemplateComponent> updateComponents = new ArrayList<ServiceTemplateComponent>();

        for (DeviceInventoryEntity device : updatedDevices) {
            if (device != null) {
                List<FirmwareDeviceInventoryEntity> nonCompliantFirmware =
                        this.getNonCompliantFirmware(device, firmwareRepository, true, DriverType.ALL);
                if (nonCompliantFirmware != null && nonCompliantFirmware.size() > 0) {
                    logger.trace("Adding component for device: " + device.getRefId());
                    ServiceTemplateComponent updateComponent = this.createFirmwareUpdateComponent("component" + (deployment.getServiceTemplate().getComponents().size() + 1), device, nonCompliantFirmware, firmwareRepository, null, true);
                    updateComponents.add(updateComponent);
                    this.setFirmwareUpdateTime(nonCompliantFirmware);

                    device.setState(DeviceState.UPDATING);
                    deviceInventoryDAO.updateDeviceInventory(device);

                } else {
                    // This would be odd since the device is in the list of non-compliant
                    logger.trace("Oddity: Not updating device: " + device.getRefId() + " No noncompliant firmware");
                }
            }
        }
        if (updateComponents != null && updateComponents.size() > 0) {
            deployment.getServiceTemplate().getComponents().addAll(updateComponents);
        }
        return updatedDevices;
    }

    /**
     * Get a list of all devices that need firmware updates. Either all servers in the case
     * of a service deployment or Devices marked as managefirmware from a getting started configuration.
     * In contrast to the add method above this does not alter the deployment object.
     *
     * @param components the components to be searched for servers that need to be updated.
     * @param allServers true when you need to get all servers as part of teh getting started configuration.
     * @return a list of devices that need firmware updates.
     */
    public List<DeviceInventoryEntity> getFirmwareUpdateDevices(List<ServiceTemplateComponent> components, 
                                                                FirmwareRepositoryEntity firmwareRepository,
                                                                boolean allServers, DriverType driverType) {
        List<DeviceInventoryEntity> updatedDevices = new ArrayList<DeviceInventoryEntity>();
        
        for (ServiceTemplateComponent component : components) {
            if ((ServiceTemplateComponentType.SERVER.equals(component.getType()) && allServers) || 
                    component.isManageFirmware()) {                     
                DeviceInventoryEntity device = null;
                if (component.getAsmGUID() != null) {
                    device = deviceInventoryDAO.getDeviceInventory(component.getAsmGUID());
                } else {
                    device = deviceInventoryDAO.getDeviceInventory(component.getId());
                }

                logger.debug("Looking up compliant firmware for asmguid: " + component.getAsmGUID() +
                        "componentid: " + component.getId() + " device is null: " + (device == null));

                if (device != null) {
                    List<FirmwareDeviceInventoryEntity> nonCompliantFirmware =
                            this.getNonCompliantFirmware(device,
                                    firmwareRepository, true, driverType);
                    if (nonCompliantFirmware != null && nonCompliantFirmware.size() > 0) {
                        logger.debug("Adding Firmware Update required for device: " + device.getRefId());
                        updatedDevices.add(device);
                    } else {
                        logger.debug("Not adding device: " + component.getId() + " No noncompliant firmware");
                    }
                }
            }
        }
        return updatedDevices;
    }
    
    
    /**
     * Get a list of all devices that need firmware updates. Either all servers in the case
     * of a service deployment or Devices marked as managefirmware from a getting started configuration.
     * In contrast to the add method above this does not alter the deployment object.
     *
     * @param firmwareRepository
     * @param deployment
     * @param allServers
     * @throws AsmManagerCheckedException
     */
    public List<DeviceInventoryEntity> getFirmwareUpdateDevicesInDeployment(final FirmwareRepositoryEntity firmwareRepository,
                                                                            final Deployment deployment,
                                                                            final boolean allServers,
                                                                            final DriverType driverType) throws AsmManagerCheckedException {
        List<DeviceInventoryEntity> updatedDevices = new ArrayList<DeviceInventoryEntity>();
        if (deployment != null && deployment.getServiceTemplate() != null) {
            if (deployment.getServiceTemplate().getComponents() != null) {
                //get the catalog
                if (firmwareRepository != null) {
                    updatedDevices = this.getFirmwareUpdateDevices(deployment.getServiceTemplate().getComponents(),
                                                                   firmwareRepository,
                                                                   allServers, driverType);
                }
            }
        }
        return updatedDevices;
    }

    public void updateComponentStatus(Deployment deployment, boolean allServers) throws AsmManagerCheckedException {

        if (deployment.getServiceTemplate() != null) {
            if (deployment.getServiceTemplate().getComponents() != null) {                                              
                for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {                
                    if ((ServiceTemplateComponentType.SERVER.equals(component.getType()) 
                            && allServers)  || component.isManageFirmware()) {                           
                        DeviceInventoryEntity device = null;
                        if (component.getAsmGUID() != null) {
                            device = deviceInventoryDAO.getDeviceInventory(component.getAsmGUID());
                        }
                        else {
                            device = deviceInventoryDAO.getDeviceInventory(component.getId());
                        }
                        
                        if (device != null && DeviceState.UPDATING.equals(device.getState())) {
                            logger.debug("Setting device " + device.getServiceTag() + " to the state READY");
                            device.setState(DeviceState.READY);
                            deviceInventoryDAO.updateDeviceInventory(device);
                        }
                    }                       
                }                                   
            }         
        }
    }
    
    /** 
     * Runs an InventoryJob for all devices in the list, and blocks until they are all complete.
     * 
     * @param deviceEntities the devices for which an InventoryJob will be run.
     */
    public void blockUntilInventoryUpdatesAreComplete(List<DeviceInventoryEntity> deviceEntities) {
        if (CollectionUtils.isNotEmpty(deviceEntities)) {
            for (DeviceInventoryEntity deviceEntity : deviceEntities) {
            	try {
					this.runInventoryJobAndBlockUntilComplete(deviceEntity);
				} catch (JobManagerException | InterruptedException e) {
					// Ignore and continue on...
					logger.warn("Error while running InventoryJobs!", e);
				}
            }
        }
    }
    
    /**
     * Utility method to encapsulate the calling of re-running device inventory on a device after updating its firmware.
     * @param deviceEntity
     */
    public JobDetail scheduleInventoryJobImmediately(DeviceInventoryEntity deviceEntity) {
        JobDetail job = null;
        try {
            logger.info("checking and starting the scheduler");
            // NOTE[fcarta] - Note sure this is needed everywhere, this block would be better in the getInstance() method
            if (!jobMgr.getScheduler().isStarted()) {
            	jobMgr.getScheduler().start();
                logger.info("scheduler started");
            }
            job = jobMgr.createNamedJob(DeviceInventoryJob.class);
            final ManagedDevice device = DeviceInventoryUtils.toDTO(deviceEntity, true);
            job.getJobDataMap().put(DeviceInventoryJob.DEVICEINVENTORY_KEY_DATA, MarshalUtil.marshal(device));
            // Create a trigger and associate it with the schedule, job,
            // and some arbitrary information. The boolean means "start now".
            final SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
            final Trigger trigger = jobMgr.createNamedTrigger(schedBuilder, job, true);
            jobMgr.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            logger.error("Failed to schedule inventory update.", e);
        }
        
        return job;
    }

    /**
     * Overloaded to not need to specify: 
     * A related ServiceTemplateComponent
     * A component Name
     * @param device
     * @param firmwareDeviceInventoryEntities
     * @param firmwareRepository
     * @param forceRestart
     * @return
     */
    public ServiceTemplateComponent createFirmwareUpdateComponent(DeviceInventoryEntity device,
                                                                  List<FirmwareDeviceInventoryEntity> firmwareDeviceInventoryEntities,
                                                                  FirmwareRepositoryEntity firmwareRepository,
                                                                  List<String> clusterServerList,
                                                                  boolean forceRestart) {
        return createFirmwareUpdateComponent(null, device, firmwareDeviceInventoryEntities, firmwareRepository, null, clusterServerList, forceRestart);
    }

    /**
     * Overloaded to not need to specify:
     * A related ServiceTemplateComponent
     * @param componentName
     * @param device
     * @param firmwareDeviceInventoryEntities
     * @param firmwareRepository
     * @param forceRestart
     * @return
     */
    public ServiceTemplateComponent createFirmwareUpdateComponent(String componentName,
                                                                  DeviceInventoryEntity device,
                                                                  List<FirmwareDeviceInventoryEntity> firmwareDeviceInventoryEntities,
                                                                  FirmwareRepositoryEntity firmwareRepository,
                                                                  List<String> clusterServerList,
                                                                  boolean forceRestart) {
        return createFirmwareUpdateComponent(componentName, device, firmwareDeviceInventoryEntities, firmwareRepository, null, clusterServerList, forceRestart);
    }

    /**
     * Returns the Embedded Repo that ships with ASM. 
     * @return the Embedded Repo that ships with ASM.
     */
    public FirmwareRepositoryEntity getEmbeddedRepo() {
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put("isEmbedded", Boolean.TRUE);
        FirmwareRepositoryEntity embeddedRepo = null;
        List<FirmwareRepositoryEntity> repos = this.genericDAO.getForEquals(attributeMap, FirmwareRepositoryEntity.class);
        if (repos != null && repos.size() > 0)
            embeddedRepo = repos.get(0);
        
        return embeddedRepo;
    }
    
    /**
     * Returns the Default catalog, or null if the default catalog has not been set.
     * @return the Default catalog, or null if the default catalog has not been set.
     */
    public FirmwareRepositoryEntity getDefaultRepo() {
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put("isDefault", Boolean.TRUE);
        FirmwareRepositoryEntity embeddedRepo = null;
        List<FirmwareRepositoryEntity> repos = this.genericDAO.getForEquals(attributeMap, FirmwareRepositoryEntity.class);
        if (repos != null && repos.size() > 0)
            embeddedRepo = repos.get(0);
        
        return embeddedRepo;
    }
    
    /**
     * If the Device is part of a Deployment, and then returns the Deployment FirmwareRepository if the Deployment is
     * managing the firmware. 
     * @param deviceInventoryEntity
     * @return
     */
    public FirmwareRepositoryEntity getDeploymentRepo(DeviceInventoryEntity deviceInventoryEntity) {
        FirmwareRepositoryEntity deploymentRepo = null;
        if (deviceInventoryEntity.getDeployments() != null && deviceInventoryEntity.getDeployments().size() > 0) {
            if (deviceInventoryEntity.getDeployments().get(0).isManageFirmware()) {
                deploymentRepo = deviceInventoryEntity.getDeployments().get(0).getFirmwareRepositoryEntity();
            }
        }
        
        return deploymentRepo;
    }
    
    /**
     * Returns the FirmwareRepositoryEntity for the Service/Deployment that the given deviceInventoryEntity is deployed
     * to, or null if the deviceInventoryEntity is not deployed. 
     * 
     * @param deviceInventoryEntity the device whose Service's FirmwareRepositoryEntity will be returned.
     * @return the FirmwareRepositoryEntity for the Service/Deployment that the given deviceInventoryEntity is deployed
     * to, or null if the deviceInventoryEntity is not deployed. 
     */
    public FirmwareRepositoryEntity getServiceRepo(DeviceInventoryEntity deviceInventoryEntity) {
        FirmwareRepositoryEntity serviceRepo = null;
        
        // Only non-shared devices should have a ServiceRepository (like Servers)
        if (!deviceInventoryEntity.getDeviceType().isSharedDevice()) { 
            if(deviceInventoryEntity.getDeployments() != null && deviceInventoryEntity.getDeployments().size() > 0) {
                DeploymentEntity deploymentEntity = deviceInventoryEntity.getDeployments().get(0);
                deploymentEntity = deploymentDAO.getDeployment(deploymentEntity.getId(),DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
                if(deploymentEntity.isManageFirmware()) {
                    serviceRepo = deploymentEntity.getFirmwareRepositoryEntity();
                }
            }
        }
        
        return serviceRepo;
    }
    
    /**
     * Creates a service template component that describes a firmware update request for a given device.
     * @param componentName: Name of the component, optional.  If not specified will be component0
     * @param device: The device we want to update firmware on
     * @param outOfComplianceFirmware: The list of non-compliant firmware on this device that we want to update. 
     * @param firmwareRepository: The firmware repository we will be updating against
     * @param templateComponent: A related component that we use for getting the esxi server, username, password.  Not always specified.
     * @param forceRestart: Whether we will be updating immediately or on next boot
     * @return
     */
    public ServiceTemplateComponent createFirmwareUpdateComponent(String componentName,
                                                                  DeviceInventoryEntity device,
                                                                  List<FirmwareDeviceInventoryEntity> outOfComplianceFirmware,
                                                                  FirmwareRepositoryEntity firmwareRepository,
                                                                  ServiceTemplateComponent templateComponent,
                                                                  List<String> clusterServerList,
                                                                  boolean forceRestart) {

        if (device == null || firmwareRepository == null) {
            return null;
        }

        logger.info(" Generating component for device " + device.getRefId() + " for repo: "
                + firmwareRepository.getId());

        final ServiceTemplateComponent component = new ServiceTemplateComponent();
        component.setId(UUID.randomUUID().toString());
        component.setComponentID((componentName == null) ? "component0" : componentName);
        component.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CONFIGURATION);
        component.setPuppetCertName(PuppetModuleUtil.toCertificateName(device));
        component.setAsmGUID(device.getRefId());
        component.setName("firmware");
        
        String categoryId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_COMP_ID; // all device types but servers
        if (device.getDeviceType().isServer()) {
            categoryId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_SERVER_COMP_ID;  // Servers use this value
        }
        
        final ServiceTemplateCategory category = new ServiceTemplateCategory();
        category.setDisplayName("firmware");
        category.setId(categoryId);
        component.getResources().add(category);
         
        final ServiceTemplateSetting product = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_PRODUCT, 
                PuppetModuleUtil.getPuppetModuleName(device.getDiscoverDeviceType(), device.getDeviceType()),
                ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        product.setDisplayName("product");
        category.getParameters().add(product);

        final ServiceTemplateSetting title = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_TITLE, 
                        component.getPuppetCertName(),
                        ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        title.setDisplayName("title");
        category.getParameters().add(title);

        final ServiceTemplateSetting catalog = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_PATH, 
                        firmwareRepository.getDiskLocation() + File.separator + firmwareRepository.getFilename(),
                        ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        catalog.setDisplayName("path");
        category.getParameters().add(catalog);

        Set<FirmwareDeviceInventoryEntity> firmDevInvEnts = 
                this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(device.getRefId());
        if (CollectionUtils.isNotEmpty(firmDevInvEnts)) {
            // Not sure why grabbing the first entry will return the correct component type
            final String type = firmDevInvEnts.toArray(
                    new FirmwareDeviceInventoryEntity[0])[0].getComponentType();// chassis have 2

            // if there is only one firmware entry or this is chassis then the software component for firmware update
            if (firmDevInvEnts.size() == 1 || "chassis".equals(type)) {
                // we could also grab the firmware device inventory from out of compliance passed in
                final FirmwareDeviceInventoryEntity fdi = firmDevInvEnts.toArray(
                        new FirmwareDeviceInventoryEntity[0])[0];
                // try to find the software component with the given repository ... which should be default repo
                SoftwareComponent softwareComponent = findSoftwareComponentInRepo(firmwareRepository, fdi, device);
                String diskLocation = firmwareRepository.getDiskLocation();
                if (softwareComponent == null) {
                    // fallback and try embedded repo to get the software component 
                    final FirmwareRepositoryEntity embeddedRepository = getEmbeddedRepo();
                    softwareComponent = findDefaultEmbeddedSoftwareComponent(embeddedRepository, fdi, device);
                    diskLocation = embeddedRepository.getDiskLocation();
                }
                
                if (softwareComponent != null) {
                    final ServiceTemplateSetting version = 
                            new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_SOFTWARE_VERSION,
                                    softwareComponent.getVendorVersion(), 
                                    ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                    version.setDisplayName("version");
                    category.getParameters().add(version);
    
                    final String binaryPath = diskLocation + File.separator + softwareComponent.getPath();
                    final ServiceTemplateSetting path = 
                            new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_SOFTWARE_PATH, 
                                    binaryPath,
                                    ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                    path.setDisplayName("path");
                    category.getParameters().add(path);
                } else {
                    logger.error("No softwareComponent found that matches device " + device.getRefId());
                    return null;
                }
            }
        }

        // this is for getting the esxi server, username, password
        if (templateComponent != null) {
            ServiceTemplateCategory resource = getResource(templateComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
            if (resource != null) {
                ServiceTemplateSetting imageType = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
                if (imageType != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE.equals(imageType.getValue())) {
                    ServiceTemplateComponent vcenterComponent = getClusterComponent(templateComponent, device, imageType);
                    if (vcenterComponent != null) {
                        String certName = vcenterComponent.getPuppetCertName();
                        ServiceTemplateSetting vcenterCertnameSetting =
                                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_VCENTER_CERT,
                                                           certName, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                        vcenterCertnameSetting.setDisplayName("vcenterCertname");
                        category.getParameters().add(vcenterCertnameSetting);
                        if (haEnabled(vcenterComponent)) {
                            ServiceTemplateSetting vcenterSetting = buildVcenterSetting(vcenterComponent);
                            category.getParameters().add(vcenterSetting);
                        }
                    }

                    ServiceTemplateSetting password = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
                    if (password != null) {
                        ServiceTemplateSetting esxiPassword =
                                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_ESX_PASSWORD,
                                                           password.getValue(), ServiceTemplateSetting.ServiceTemplateSettingType.PASSWORD);
                        esxiPassword.setDisplayName("esxPassword");
                        category.getParameters().add(esxiPassword);
                    }

                    String esxiIp = this.getHostNameOrIpAddress(templateComponent);
                    ServiceTemplateSetting esxiServer =
                            new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_ESX_HOSTNAME,
                                                       esxiIp, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                    esxiServer.setDisplayName("esxServer");
                    category.getParameters().add(esxiServer);
                    
                    if(clusterServerList != null && !clusterServerList.isEmpty()) {
                        String commaSeparatedServerList = getJson(clusterServerList);
                        if (commaSeparatedServerList != null) {
	                    	ServiceTemplateSetting esxiServerList =
	                                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_VCENTER_SERVER_LIST,
	                                		commaSeparatedServerList, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
	                        esxiServer.setDisplayName("Vcenter Server List");
	                        category.getParameters().add(esxiServerList);
                        }
                    }
                }

            }
        }

        ServiceTemplateSetting forceRestartSetting = null;
        if (forceRestart)
            forceRestartSetting = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_FORCE_RESTART, 
                        "true", ServiceTemplateSetting.ServiceTemplateSettingType.BOOLEAN);
        else
            forceRestartSetting = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_FORCE_RESTART, 
                        "false", ServiceTemplateSetting.ServiceTemplateSettingType.BOOLEAN);
        forceRestartSetting.setDisplayName("forceRestart");
        category.getParameters().add(forceRestartSetting);

        ServiceTemplateSetting Asm_hostname = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_ASM_HOSTNAME, 
                        null, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        Asm_hostname.setDisplayName("asm_hostname");
        category.getParameters().add(Asm_hostname);

        if (outOfComplianceFirmware != null) {
            List<FirmwareUpdateInfo> firmwareUpdates = new ArrayList<>();
            List<FirmwareUpdateInfo> softwareUpdates = new ArrayList<>();
            Set<String> binariesSeen = new HashSet<>();
            for (FirmwareDeviceInventoryEntity fdi : outOfComplianceFirmware) {
                // Must pass in a boolean indicating whether it's a Catalog/Software match and thus laods the Drivers
                boolean loadDriverType = !(fdi.getOperatingSystem() == null || 
                        fdi.getOperatingSystem().trim().isEmpty());

                SourceType sourceType = loadDriverType ? SourceType.Catalog : SourceType.fromValue(fdi.getSource());

                List<SoftwareComponent> components = this.getSoftwareComponents(fdi.getComponentID(),
                        fdi.getDeviceID(), fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(),
                        firmwareRepository, device.getSystemId(), sourceType,
                        fdi.getOperatingSystem(), loadDriverType);
                

                String binaryPath = null;
                if (components != null && components.size() > 0) {
                	logger.debug("Found " + components.size() + " SoftwareComponents in createFirmwareUpdateComponent method.");
                	SoftwareComponent foundComponent = components.get(0);
                    binaryPath = firmwareRepository.getDiskLocation() + File.separator + foundComponent.getPath();
                } else {
                	if (firmwareRepository != null) {
                		logger.debug("No Components found for repository " + firmwareRepository.getName() + " in createFirmwareUpdateComponent method!");
                	} 
                	else { // Adding a new Exception here so we can better understand the flow in which this occurs.
                		logger.warn("FirmwareRepository is null in createFirmwareUpdateComponent method, will now use embedded catalog instead!", new Exception());
                	}
                	FirmwareRepositoryEntity embedded = getEmbeddedRepo();
                    components = this.getSoftwareComponents(fdi.getComponentID(), fdi.getDeviceID(),
                            fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(), embedded,
                            device.getSystemId(), SourceType.fromValue(fdi.getSource()), fdi.getOperatingSystem(), false);
                    if (components != null && components.size() > 0) {
                        SoftwareComponent foundComponent = components.get(0);
                        binaryPath = embedded.getDiskLocation() + File.separator + foundComponent.getPath();
                    }
                }

                if (components != null && components.size() > 0) {
                    // Only add to the list of updates if this is a unique binary. There may
                    // be a better way to determine uniqueness of the software component,
                    // but this should do for now. The idrac update will fail if we try
                    // to update the same firmware more than once.
                    if (binariesSeen.add(binaryPath)) {
                        FirmwareUpdateInfo info = new FirmwareUpdateInfo();
                        info.setInstanceId(fdi.getFqdd());
                        info.setComponentId(fdi.getComponentID());
                        info.setUriPath(binaryPath);
                        if (device.getModel().contains("C6220")) {
                            info.setVersion(components.get(0).getVendorVersion());
                        }
                        
                        if (fdi.getOperatingSystem() == null) {
                            firmwareUpdates.add(info); // Firmware does NOT have an operating system
                        }
                        else {
                            softwareUpdates.add(info); // Only Software components will have an operating system
                        }
                    }
                } else {
                    logger.error("Did not find a software component where expecting one.");
                }
            }

            if (firmwareUpdates.isEmpty() && softwareUpdates.isEmpty()) {
                logger.debug("No software or firmware updates required for " + device.getRefId() + " for repo: "
                        + firmwareRepository.getId());
                return null;
            }

            String firmwareValueString = toJson(firmwareUpdates);
            if (device.getModel().contains("C6220")) {
                firmwareValueString = firmwareValueString.replaceAll("component_id", "component_name");
                firmwareValueString = firmwareValueString.replaceAll("uri_path", "location");
            }

            ServiceTemplateSetting server_firmware = 
                    new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_SERVER_FIRMWARE, 
                            firmwareValueString, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
            server_firmware.setDisplayName("server_firmware");
            category.getParameters().add(server_firmware);
            
            if (!softwareUpdates.isEmpty()) {
            
                String softwareValueString = toJson(softwareUpdates);
                if (device.getModel().contains("C6220")) {
                    softwareValueString = softwareValueString.replaceAll("component_id", "component_name");
                    softwareValueString = softwareValueString.replaceAll("uri_path", "location");
                }
                
                ServiceTemplateSetting software_firmware = 
                        new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_FIRMWARE_SERVER_SOFTWARE, 
                                softwareValueString, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                software_firmware.setDisplayName("server_software");
                category.getParameters().add(software_firmware);
            }
            
            
        }

        return component;

    }
    
    private boolean haEnabled(ServiceTemplateComponent vcenterComponent) {
        ServiceTemplateSetting haSetting = vcenterComponent.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_HA_ID);
        return (StringUtils.equalsIgnoreCase("true",haSetting.getValue()));
    }

    //This finds the associated vCenter cluster from a serverComponent
    private ServiceTemplateComponent getClusterComponent(ServiceTemplateComponent serverComponent,
                                                         DeviceInventoryEntity device,
                                                         ServiceTemplateSetting imageTypeSetting) {
        if (device != null) {
            ServiceTemplate oldTemplate = null;
            if (device.getDeployments() != null) {
                for (DeploymentEntity originalDeployment : device.getDeployments()) {
                    oldTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, originalDeployment.getMarshalledTemplateData());
                }
            }
            if (oldTemplate != null &&
                    imageTypeSetting != null &&
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE.equals(imageTypeSetting.getValue())) {
                for (String componentId : serverComponent.getAssociatedComponents().keySet()) {
                    ServiceTemplateComponent component = oldTemplate.findComponentById(componentId);
                    for (ServiceTemplateCategory category : component.getResources()) {
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID.equals(category.getId())) {
                            return component;
                        }
                    }
                }
            }
        }
        return null;
    }

    private ServiceTemplateSetting buildVcenterSetting(ServiceTemplateComponent component) {
        HashMap<String,String> vCenterMap = new HashMap<>();
        String newFlag = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX;
        String clusterId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID;
        String dcId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_DATACENTER_ID;
        String drsId = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DRS_ID;

        String dc = component.getTemplateSetting(dcId).getValue();
        if (StringUtils.equalsIgnoreCase(newFlag, dc)) {
            dc = component.getTemplateSetting(newFlag + dcId).getValue();
        }
        String cluster = component.getTemplateSetting(clusterId).getValue();
        if (StringUtils.equalsIgnoreCase(newFlag, cluster)) {
            cluster = component.getTemplateSetting(newFlag + clusterId).getValue();
        }
        String clusterPath = "/" + dc + "/" + cluster;

        vCenterMap.put("drs_config", component.getTemplateSetting(drsId).getValue());
        vCenterMap.put("cluster_path", clusterPath);
        String mapAsJson = null;
        try {
            mapAsJson = new ObjectMapper().writeValueAsString(vCenterMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ServiceTemplateSetting vcenterSetting = 
                new ServiceTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_HA_CONFIG_ID, 
                        mapAsJson, ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        vcenterSetting.setDisplayName("vCenterHaConfig");
        return vcenterSetting;
    }


    static String toJson(List<FirmwareUpdateInfo> updates) {
        try {
            StringWriter sw = new StringWriter();
            OBJECT_MAPPER.writeValue(sw, updates);
            return sw.toString();
        } catch (IOException e) {
            throw new AsmManagerRuntimeException("Failed to marshal firmware updates", e);
        }
    }
    
    static String getJson(List<String> strings) {
        try {
            StringWriter sw = new StringWriter();
            OBJECT_MAPPER.writeValue(sw, strings);
            return sw.toString();
        } catch (IOException e) {
            throw new AsmManagerRuntimeException("Failed to marshal strings", e);
        }
    }

    public void setFirmwareUpdateTime(List<FirmwareDeviceInventoryEntity> nonCompliantFirmware) {
        if (nonCompliantFirmware != null)
            for (FirmwareDeviceInventoryEntity fdi : nonCompliantFirmware)
            {
                fdi.setLastUpdateTime(new Date());
                this.genericDAO.update(fdi);
            }       
    }
    
    /**
     * Utility method for retreiving the ServiceTemplateCategory that contains a resource with the specified resourceId.
     * @param component
     * @param resourceId
     * @return
     */
    private ServiceTemplateCategory getResource(ServiceTemplateComponent component, String resourceId)
    {
        if (component == null || resourceId == null)
            return null;
        
        if (component.getResources() != null)
            for (ServiceTemplateCategory cat : component.getResources())
            {
                if (resourceId.equals(cat.getId()))
                    return cat;
            }
        
        return null;
    }
    
    /**
     * Returns matching software components within a specified firmware
     * repository, ignoring components of type 'Driver'. See: ASM-3756
     * @param componentId The component id
     * @param deviceId The device id
     * @param subDeviceId The sub-device id
     * @param vendorId The vendor id
     * @param subVendorId The sub-vendor id
     * @param parentRepo The repository to check for matches
     * @return A list of matching components.
     */
    public List<SoftwareComponent> getSoftwareComponents(String componentId, 
                                                         String deviceId, 
                                                         String subDeviceId, 
                                                         String vendorId, 
                                                         String subVendorId,
                                                         FirmwareRepositoryEntity parentRepo,
                                                         String systemId,
                                                         SourceType sourceType,
                                                         String operatingSystem,
                                                         boolean loadDriverType) {

        logger.debug("getsoftware components with componentid: " + componentId + " deviceid: " + deviceId
                + " subdeviceid: " + subDeviceId + " vendorid: " + vendorId + " subvendorid: " + subVendorId 
                + " operatingSystem:" + operatingSystem);

        final List<SoftwareComponent> components = new ArrayList<>();
        try {
            final HashMap<String, Object> attributeMap = new HashMap<>();

            if (parentRepo != null) {
                // We need at least one of the following componentId, deviceId, subDeviceId, vendorId, subVendorId, systemId
                if (StringUtils.isBlank(componentId) && StringUtils.isBlank(deviceId) 
                        && StringUtils.isBlank(subDeviceId) && StringUtils.isBlank(vendorId)
                        && StringUtils.isBlank(subVendorId) && StringUtils.isBlank(systemId)) {
                    return null;
                }

                attributeMap.put("firmwareRepositoryEntity", parentRepo);
                attributeMap.put("componentId", componentId);
                attributeMap.put("deviceId", deviceId);
                attributeMap.put("subDeviceId", subDeviceId);
                attributeMap.put("vendorId", vendorId);
                attributeMap.put("subVendorId", subVendorId);
                attributeMap.put("operatingSystem",  operatingSystem);
                
                // Special case brought on by componentId not being present in
                // the wsman command response for RAID and NICs
                // The alternative is to hard code the componentid in the
                // discovery
                // Spectre has similar logic.
                if (StringUtils.isNotEmpty(deviceId) && StringUtils.isNotEmpty(subDeviceId)
                        && StringUtils.isNotEmpty(vendorId) && StringUtils.isNotEmpty(subVendorId)
                        && StringUtils.isEmpty(componentId)) {
                    attributeMap.remove("componentId");
                }

                
                List<SoftwareComponentEntity> entities = null;
                if (SourceType.Device == sourceType) {
                    entities = this.genericDAO.getForEquals(attributeMap, systemId);
                }
                else { // it's a SourceType.Catalog and systemId does not exist
                    entities = this.genericDAO.getForEquals(attributeMap, (String)null); 
                }
                
                if (entities != null) {
                    for (final SoftwareComponentEntity entity : entities) {
                        final String componentType = entity.getComponentType();
                        // We ONLY ignore if it's Driver and SourceType.Device (versus Catalog)
                        if (!loadDriverType && StringUtils.equalsIgnoreCase(componentType, COMPONENT_TYPE_DRIVER) && SourceType.Device == sourceType) {
                            // Ignore software_components of type 'Driver'
                            logger.trace("Ignoring Driver softwareComponent" + entity.getId());
                            continue;
                        }
                        logger.trace("Found software component " + entity.getId() + " for firmware repo : "
                                + parentRepo.getName() + " " + parentRepo.getId());
                        components.add(entity.getSoftwareComponent());
                        logger.trace("Found softwareComponent " + entity.getId() + " with dellversion "
                                + entity.getDellVersion() + " vendor version " + entity.getVendorVersion());
                    }
                }
            }
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while retrieving software components", e);
            throw e;
        } catch (Exception e) {
            logger.error("LocalizedWebApplicationException while retrieving software components", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        if (CollectionUtils.isNotEmpty(components) && components.size() > 1) {
            Collections.sort(components, componentVersionComparator);
        }

        return components;
    }
    
    /**
     * This method ensure all of the Compliance components necessary to provide a report for the Firmware Compliance
     * at the embedded, default, and service level are loaded properly.  This method actually modified the ManagedDevice
     * by loading it's FirmwareComplianceComponents and then returns the CompliantState as evaluated based on the 
     * existence of the varying repositories (embedded, default, and service). 
     */
    public CompliantState getComplianceState(ManagedDevice device, DeviceInventoryEntity deviceEntity) {

        CompliantState compliance = CompliantState.COMPLIANT;
        try {
            FirmwareRepositoryEntity embeddedRepo = this.getEmbeddedRepo();
            FirmwareRepositoryEntity defaultRepo = this.getDefaultRepo();
            FirmwareRepositoryEntity serviceRepo = this.getServiceRepo(deviceEntity);
            
            final Map<DeviceInventoryComplianceId,DeviceInventoryComplianceEntity> deviceComplianceMap = 
                    new HashMap<DeviceInventoryComplianceId,DeviceInventoryComplianceEntity>();
            for (final DeviceInventoryComplianceEntity complianceItem : 
                deviceInventoryComplianceDAO.findByDeviceInventory(deviceEntity)) {
                deviceComplianceMap.put(complianceItem.getDeviceInventoryComplianceId(), complianceItem);
            }

            Set<FirmwareDeviceInventoryEntity> firmDevInv = this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(device.getRefId());
            
            if (firmDevInv != null) {
                for (FirmwareDeviceInventoryEntity fdi : firmDevInv) {
                    final FirmwareComplianceComponent complianceComponents = 
                            this.getFirmwareComplianceWithComponents(deviceComplianceMap, 
                                                                     embeddedRepo, 
                                                                     defaultRepo, 
                                                                     serviceRepo, 
                                                                     fdi);
                    
                    if (complianceComponents != null) {
                        // ********** NOTE **********  this was setting the firmware compliance components
                        // fdi.setFirmwareComplianceComponents(complianceComponents);
                        CompliantState temp = complianceComponents.getCompliantState();
                        compliance = this.mergeComplianceState(compliance, temp);
                    }
                }
            }
            device.setCompliance(compliance);
            
        } catch (Exception e) {
            logger.error("Error in FirmwareUtil.getComplianceState() call!" + e.getMessage(), e);
        }

        return compliance;
    }
    
    /**
     * This method returns the ComplianceState for a single firmwaredeviceinventroy entity.
     * @param deviceComplianceMap: the device compliance map
     * @param embeddedRepo: The system's embeded repo.
     * @param defaultRepo: The system's default repo.
     * @param fdi: The firmware deviceinventory entity that we are validating
     * @return
     */
    public FirmwareComplianceComponent getFirmwareComplianceWithComponents(final Map<DeviceInventoryComplianceId,DeviceInventoryComplianceEntity> deviceComplianceMap,
                                                                           final FirmwareRepositoryEntity embeddedRepo,
                                                                           final FirmwareRepositoryEntity defaultRepo,
                                                                           final FirmwareRepositoryEntity serviceRepo,
                                                                           final FirmwareDeviceInventoryEntity fdi) {
        logger.trace("Checking firmwaredeviceinventory " + fdi.getId());
        
        final FirmwareComplianceComponent complianceComponents = new FirmwareComplianceComponent();
        final DeviceInventoryEntity deviceEntity = this.deviceInventoryDAO.getDeviceInventory(fdi.getDeviceInventoryId());

        final List<SoftwareComponent> embeddedRepoComponents = getSoftwareComponents(fdi.getComponentID(),
                fdi.getDeviceID(),fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(), embeddedRepo, 
                deviceEntity.getSystemId(), SourceType.valueOf(fdi.getSource()), fdi.getOperatingSystem(), false);
        final List<SoftwareComponent> components = getSoftwareComponents(fdi.getComponentID(), fdi.getDeviceID(), 
                fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(), defaultRepo, 
                deviceEntity.getSystemId(), SourceType.valueOf(fdi.getSource()), fdi.getOperatingSystem(), false);
        final List<SoftwareComponent> serviceRepoComponents = getSoftwareComponents(fdi.getComponentID(), 
                fdi.getDeviceID(), fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(), serviceRepo, 
                deviceEntity.getSystemId(), SourceType.valueOf(fdi.getSource()), fdi.getOperatingSystem(), false);
        

        DeviceInventoryComplianceId complianceKey =
                new DeviceInventoryComplianceId(deviceEntity, embeddedRepo);
        CompliantState embeddedState = deviceComplianceMap.get(complianceKey).getCompliance();

        if (embeddedState != CompliantState.UPDATEREQUIRED && defaultRepo != null) {
            complianceKey = new DeviceInventoryComplianceId(deviceEntity, defaultRepo);
        }

        complianceComponents.setCompliantState(deviceComplianceMap.containsKey(complianceKey) ? 
                deviceComplianceMap.get(complianceKey).getCompliance() : CompliantState.COMPLIANT);

        complianceComponents.setDefaultRepoComponents(components);
        complianceComponents.setEmbeddedRepoComponents(embeddedRepoComponents);
        complianceComponents.setServiceRepoComponents(serviceRepoComponents);

        return complianceComponents;
    }
    
    private void logLocalizedInfoMessage(EEMILocalizableMessage msg) {
        if (msg != null) {
            LocalizableMessageService.getInstance()
                    .logMsg((ResourceBundleLocalizableMessage) msg.getDetailedDescription(), LogSeverity.INFO,
                            LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION, msg.getCorrelationId(), msg.getAgentId());
        }
    }
    
    /**
     * Runs the firmware compliance check / update for all repositories and all devices
     */
    public void runFirmwareComplianceCheckForAllRepositories() {
        logger.trace("runFirmwareComplianceCheckForAll");
        
        for (final FirmwareRepositoryEntity firmwareRepository : firmwareRepositoryDAO.getAll()) {
            updateComplianceMapAndDeviceComplianceForRepo(firmwareRepository);
        }        
    }
    
    /**
     * It will also update compliance status for each device
     * @param firmwareRepository
     */
    public void updateComplianceMapAndDeviceComplianceForRepo(final FirmwareRepositoryEntity firmwareRepository) {
        logger.trace("runFirmwareComplianceCheckForRepository for repository " + firmwareRepository.getId());

        // First update the compliance_map for the device and the repository
        this.updateComplianceMapForRepo(firmwareRepository);
        
        // Update the device_inventory compliance field properly
        FirmwareRepositoryEntity defaultRepo = this.getDefaultRepo();
        FirmwareRepositoryEntity embeddedRepo = this.getEmbeddedRepo();
        
        for (final DeviceInventoryEntity deviceInventory : deviceInventoryDAO.getAllDeviceInventory()) {
            FirmwareRepositoryEntity serviceRepo = this.getServiceRepo(deviceInventory);
            this.updateDeviceInventoryComplianceStatus(deviceInventory, defaultRepo, embeddedRepo, serviceRepo);
        }
    }

    /**
     * Updates the compliance_map table with all of the compliance_maps for devices that are currently in the system.
     * 
     * @param firmwareRepoEntity the newly created Firmware Repository whose compliance_map entries will be created
     *      for all devices that are currently in the system.
     */
    public void updateComplianceMapForRepo(final FirmwareRepositoryEntity firmwareRepoEntity) {
        logger.trace("updateComplianceMapAndDeviceComplianceForNewRepo for repository " + firmwareRepoEntity.getId());

        for (final DeviceInventoryEntity deviceInventory : deviceInventoryDAO.getAllDeviceInventory()) {
            updateFirmwareComplianceMapForDevice(firmwareRepoEntity, deviceInventory);
        }
    }
    
    /**
     * Get the firmware compliance of an individual firmware device relative to the given
     * firmware repository.  If the repository is the embedded minimal, a return value
     * of UPDATEREQUIRED is possible.  For any other repository it can only be COMPLIANT
     * or NONCOMPLIANT
     */
    public CompliantState getFirmwareCompliance(final DeviceInventoryEntity deviceInventory,
                                                final FirmwareRepositoryEntity firmwareRepository,
                                                final FirmwareDeviceInventoryEntity fdi) {
        if (firmwareRepository.isEmbedded()) {
            final SoftwareComponent defaultEmbeddedSC =
                    findDefaultEmbeddedSoftwareComponent(firmwareRepository, fdi, deviceInventory);
            if (defaultEmbeddedSC != null &&
                    VersionUtils.compareVersions(fdi.getVersion(), defaultEmbeddedSC.getVendorVersion()) < 0) {
                logger.info("Device firmware version " + fdi.getVersion() + " is less than minimal repository version "
                        + defaultEmbeddedSC.getVendorVersion() + " therefore update is required.");
                return CompliantState.UPDATEREQUIRED;
            }
        } else {
            final SoftwareComponent defaultSC = findSoftwareComponentInRepo(firmwareRepository, fdi, deviceInventory);
            if (defaultSC != null &&
                    VersionUtils.compareVersions(fdi.getVersion(), defaultSC.getVendorVersion()) != 0) {
                logger.info("Device firmware version " + fdi.getVersion() + " is less than current repository version "
                        + defaultSC.getVendorVersion() + " therefore it is non compliant");
                // since device and repository versions do not match device is non compliant
                return CompliantState.NONCOMPLIANT;
            }
        }
        return CompliantState.COMPLIANT;
    }
    
    
    
    public SoftwareComponent findDefaultEmbeddedSoftwareComponent(final FirmwareRepositoryEntity embeddedRepo,
                                                                  final FirmwareDeviceInventoryEntity fdi,
                                                                  final DeviceInventoryEntity deviceEntity) {
        
        // If the given repo is not the embedded repo then return null
        if (!embeddedRepo.isEmbedded()) {
            logger.error("Given repository is not embeddded repository");
            return null;
        }
        final List<SoftwareComponent> embeddedRepoComponents = this.getSoftwareComponents(
                fdi.getComponentID(), fdi.getDeviceID(), fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(),
                embeddedRepo, deviceEntity.getSystemId(), SourceType.valueOf(fdi.getSource()), 
                fdi.getOperatingSystem(), false);
        if (embeddedRepoComponents == null) {
            logger.error("Found a component is null" );
        }
        if (embeddedRepoComponents != null && embeddedRepoComponents.size() == 1) {
            return embeddedRepoComponents.get(0);
        } else if (embeddedRepoComponents != null && embeddedRepoComponents.size() > 1) {
            String embeddedRepoId = (embeddedRepo == null) ? null : embeddedRepo.getId();
            logger.error("ERROR getting embedded software components: " + embeddedRepoComponents.size() + " for repo "
                    + embeddedRepoId + " too many results for componentid " + fdi.getComponentID() + "deviceID"
                    + fdi.getDeviceID() + " subdeviceid " + fdi.getSubdeviceID() + " vendorid " + fdi.getVendorID()
                    + " subvendorid " + fdi.getSubvendorID() + " systemid " + deviceEntity.getSystemId());
            for (SoftwareComponent component : embeddedRepoComponents) {
                logger.error("Component id: " + component.getId() + " vendor version: " + component.getVendorVersion());
            }
            return embeddedRepoComponents.get(0);
        }
        return null;
    }
    
    /**
     * Finds matching SoftwareComponent in a FirmwareCatalog for the given FirmwareDeviceInventory (firmware component).
     * 
     * @param repo the Firmware Repositor (Catalog) that will be searched for a matching SoftwareComponent.
     * @param fdi the firmwareDeviceInventoryEntity the SoftwareCatalog must match.
     * @param deviceEntity the deviceEntity whose firmware device inventory entity is being matched.
     * @return a matching SoftwareComponent or null if none can be found.
     */
    public SoftwareComponent findSoftwareComponentInRepo(final FirmwareRepositoryEntity repo, 
                                                         final FirmwareDeviceInventoryEntity fdi, 
                                                         final DeviceInventoryEntity deviceEntity) {
        
        // if the given repo is the embedded repo then return null
        if (repo.isEmbedded()) {
            return null;
        }
        
        // Drivers are filtered out by default.  The ONLY time we load a driver is when we are loading software that 
        // uses an OS (vs firmware) components.  Vibs for example are of type DRVR and must be loaded.  
        boolean loadDriverType = !(fdi.getOperatingSystem() == null || 
                fdi.getOperatingSystem().trim().isEmpty());

        SourceType sourceType = loadDriverType ? SourceType.Catalog : SourceType.fromValue(fdi.getSource());

        // Find the matching components and return if there is one or null if there are none.
        final List<SoftwareComponent> components = this.getSoftwareComponents(fdi.getComponentID(), 
                fdi.getDeviceID(), fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(), repo,
                deviceEntity.getSystemId(), sourceType, fdi.getOperatingSystem(),
                loadDriverType);
        return (CollectionUtils.isNotEmpty(components)) ? components.get(0) : null;
    }    
    
    public enum DriverType {
        ALL,
        FIRMWARE,
        SOFTWARE
    }
    
    /**
     * Returns a list of firmware for the given device that are either non-compliant or updaterequired
     * @param deviceInventory The device to check firmware compliance of.
     * @param firmwareRepository
     * @param discardNoOp
     * @param type DriverType enum value to determine the type of non compliant list to return
     * @return
     */
    public List<FirmwareDeviceInventoryEntity> getNonCompliantFirmware(final DeviceInventoryEntity deviceInventory, 
                                                                       final FirmwareRepositoryEntity firmwareRepository, 
                                                                       final boolean discardNoOp,
                                                                       final DriverType type) {
        
        final List<FirmwareDeviceInventoryEntity> firmwareList = new ArrayList<FirmwareDeviceInventoryEntity>();
        if (deviceInventory != null) {
            // check if device/firmware combo is compliant or not 
            final DeviceInventoryComplianceEntity deviceInventoryCompliance =
                    this.deviceInventoryComplianceDAO.get(deviceInventory, firmwareRepository);
            if (deviceInventoryCompliance != null && 
                    !CompliantState.COMPLIANT.equals(deviceInventoryCompliance.getCompliance())) {

                Set<FirmwareDeviceInventoryEntity> firmDevInvEnts = 
                        this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(deviceInventory.getRefId());
                // device/firmware combo is not compliant so loop through the firmware and return only non-compliant
                if (firmDevInvEnts != null) {
                    for (FirmwareDeviceInventoryEntity fdi : firmDevInvEnts) {
                        if (type.equals(DriverType.ALL)
                                || (type.equals(DriverType.FIRMWARE) && fdi.getOperatingSystem() == null)
                                || (type.equals(DriverType.SOFTWARE) && fdi.getOperatingSystem() != null)) {
                            final SoftwareComponent defaultSC = 
                                    findSoftwareComponentInRepo(firmwareRepository, fdi, deviceInventory);
                            if (discardNoOp && defaultSC != null && defaultSC.getPath().contains("ASMNoOp")) {
                                logLocalizedInfoMessage(CatalogMessages.buildInfoMsg(
                                        CatalogMessages.CATALOG_FIRMWARE_NOOP_DISCARD, deviceInventory.getModel()));
                                logger.trace("Not returning firmware device inventory because discardNoOp is set to true");
                            } else {
                                // Means it's part of the Catalog and not installed yet.
                                if (SourceType.Catalog.getValue().equals(fdi.getSource())) { 
                                    firmwareList.add(fdi);
                                } 
                                else {
                                    final CompliantState firmwareCompliance = 
                                            getFirmwareCompliance(deviceInventory, firmwareRepository,fdi);
                                    // if the firmware is not compliant add it to the list
                                    if (!CompliantState.COMPLIANT.equals(firmwareCompliance)) {
                                        firmwareList.add(fdi);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return firmwareList;
    }
    
    /**
     * Returns the Hypervisor IpAddress from a component if it exists. 
     */
    public String getHypervisorIp(ServiceTemplateComponent component) {
        
        String ipAddress = null;
        
        List<ServiceTemplateCategory> resources = component.getResources();
        resourcesLoop:
        for (ServiceTemplateCategory resource : resources) {
            if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(resource.getId())) {
                List<ServiceTemplateSetting> parameters = resource.getParameters();
                for (ServiceTemplateSetting parameter : parameters){
                    if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(parameter.getId())) {
                        NetworkConfiguration networkConfig = parameter.getNetworkConfiguration();

                        if(networkConfig!=null) {
                            List<Interface> interfaces = networkConfig.getUsedInterfaces();
                            ipAddress = fetchIpFromInterfaces(interfaces);
                            break resourcesLoop;
                        }
                    }
                    else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals(parameter.getId())) {
                        List<Network> networks = parameter.getNetworks();
                        for(Network network : networks) {
                            if(network.isStatic()) {
                                if(network.getStaticNetworkConfiguration() != null && network.getStaticNetworkConfiguration().getIpAddress() != null) {
                                    ipAddress = network.getStaticNetworkConfiguration().getIpAddress();
                                    break resourcesLoop;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ipAddress;
    }
    
    public String fetchIpFromInterfaces(List<Interface> interfaceObjectList) {
        if (interfaceObjectList==null) {
            return null;
        }

        for (Interface interfaceObject : interfaceObjectList){
            List<Partition> partitions = interfaceObject.getPartitions();
            if (partitions != null) {
                for (Partition partition : partitions) {
                    List<Network> networkObjects = partition.getNetworkObjects();
                    if (networkObjects != null) {
                        for (Network networkObject : networkObjects) {
                            if(networkObject!=null && NetworkType.HYPERVISOR_MANAGEMENT.equals(networkObject.getType())
                                    && networkObject.getStaticNetworkConfiguration() != null
                                    && networkObject.getStaticNetworkConfiguration().getIpAddress() != null){
                               return networkObject.getStaticNetworkConfiguration().getIpAddress();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public CompliantState mergeComplianceState(CompliantState compliance, CompliantState temp) {
        if (CompliantState.UPDATEREQUIRED.equals(temp)) {
            // this overrides the compliance status for device
            return temp;
        } else if (CompliantState.NONCOMPLIANT.equals(temp)) {
            // only if it is not already set to UPDATEREQUIRED
            if (!CompliantState.UPDATEREQUIRED.equals(compliance)) {
                return temp;
            }
        } else if (!CompliantState.COMPLIANT.equals(temp) &&
                CompliantState.COMPLIANT.equals(compliance)) {
            // any remaining non-compliant state overrides
            return temp;
        }
        return compliance;
    }
    
    /**
     * This method loops through all of the repositories that exist and creates the mapping in the compliance_map table
     * for each and every repository for the device.  Once that is complete, it determines if the device is part of 
     * a service and updates the device_inventory's compliance field with the proper value from the compliance_map 
     * table (for performance reasons).
     * 
     * @param deviceInventoryEntity the device whose compliance_map will be created and whose device_inventory 
     *  compliance will be updated from the compliance_map table.
     */
    public void updateComplianceMapsAndDeviceInventoryCompliance(final DeviceInventoryEntity deviceInventoryEntity) {
        for (final FirmwareRepositoryEntity firmwareRepository : firmwareRepositoryDAO.getAll()) {
            updateFirmwareComplianceMapForDevice(firmwareRepository, deviceInventoryEntity);
        }   
        
        // Update the device_inventory's compliance field from the newly updated compliance_map table
        FirmwareRepositoryEntity defaultRepo = getDefaultRepo();
        FirmwareRepositoryEntity embeddedRepo = getEmbeddedRepo();
        FirmwareRepositoryEntity serviceRepo = this.getServiceRepo(deviceInventoryEntity);
        
        this.updateDeviceInventoryComplianceStatus(deviceInventoryEntity, defaultRepo, embeddedRepo, serviceRepo);
    }
    
    /**
     * Loops through all of the deviceInventoryEntities and updates their compliance_map values for all repositories
     * and then updates the proper compliance to the device_inventory compliance field.
     * 
     * @param deviceInventoryEntities the device's whose compliance_map will be updated for all firmware repositories
     *  and whose compliance firmware status will be updated.
     */
    public void updateComplianceMapsAndDeviceInventoryCompliance(List<DeviceInventoryEntity> deviceInventoryEntities) {
        if (deviceInventoryEntities != null && !deviceInventoryEntities.isEmpty()) {
            for (DeviceInventoryEntity die : deviceInventoryEntities) {
                this.updateComplianceMapsAndDeviceInventoryCompliance(die);
            }
        }
    }
    
    
    /**
     * Returns an Array of FirmwareComplianceReports for all of the Devices that are part of the Deployment (if the
     * device's firmware can be managed).
     * 
     * @param deploymentEntity the Deployment whose devices will be checked and a FirmwareComplianceReport generated
     * @return an Array of FirmwareComplianceReports for all of the Devices that are part of the Deployment (if the
     * device's firmware can be managed).
     */
    public FirmwareComplianceReport[] getFirmwareComplianceReportsForDeployment(DeploymentEntity deploymentEntity) throws AsmCheckedException {
        
        ArrayList<FirmwareComplianceReport> firmwareComplianceReports = new ArrayList<FirmwareComplianceReport>();
        
        FirmwareRepositoryEntity serviceRepoEntity = deploymentEntity.getFirmwareRepositoryEntity();
        // Verify we can do a report and throw error otherwise
        if (serviceRepoEntity == null) {
           throw new AsmCheckedException("No Service Repository found for service with id: " + 
                   deploymentEntity.getId(), AsmManagerMessages.noRepositoryFoundForService(deploymentEntity.getName()));
        }
        if (!deploymentEntity.isManageFirmware()) {
           throw new AsmCheckedException("Service is not managaing firmware for service with id: " +
                   deploymentEntity.getId(), AsmManagerMessages.serviceIsNotManagingFirmware(deploymentEntity.getName()));
        }
        
        
        // If Device is part of a Deployment process the FirmwareRepository for that Service 
        for (DeviceInventoryEntity deviceInventoryEntity : deploymentEntity.getDeployedDevices()) {
            if(deviceInventoryEntity.getDeviceType().isFirmwareComplianceManaged()) {
                FirmwareComplianceReport firmwareComplianceReport = 
                        this.getFirmwareComplianceReportForDevice(deviceInventoryEntity, null, null, serviceRepoEntity);
                if(firmwareComplianceReport != null && // Only load a report if there are components for it!
                        firmwareComplianceReport.getFirmwareComplianceReportComponents() != null && 
                        firmwareComplianceReport.getFirmwareComplianceReportComponents().size() > 0) {
                    firmwareComplianceReport.sortFirmwareComplianceReportComponentsByNameAndStatus();
                    firmwareComplianceReports.add(firmwareComplianceReport);
                }
            }
        }
        
        Collections.sort(firmwareComplianceReports, COMPLIANCE_REPORT_COMPARATOR);
        
        return firmwareComplianceReports.toArray(new FirmwareComplianceReport[firmwareComplianceReports.size()]);
    }
    
    private FirmwareComplianceReport getFirmwareComplianceReportForDevice(DeviceInventoryEntity deviceInventoryEntity,
                                                                          FirmwareRepositoryEntity defaultRepoEntity,
                                                                          FirmwareRepositoryEntity embeddedRepoEntity,
                                                                          FirmwareRepositoryEntity serviceRepoEntity) {

        if (defaultRepoEntity == null &&
                embeddedRepoEntity == null &&
                serviceRepoEntity == null) {
            throw new IllegalArgumentException("A default, embedded, or service firmware repository must be passed in as a parameter.");
        }

        FirmwareComplianceReport firmwareComplianceReport = new FirmwareComplianceReport();
        firmwareComplianceReport.setCompliant(true);

        String catalogType = null;
        FirmwareRepositoryEntity repositoryEntity = null;
        if (defaultRepoEntity != null) {
            catalogType = "Default Catalog - ";
            repositoryEntity = defaultRepoEntity;
        } else if (embeddedRepoEntity != null) {
            catalogType = "Embedded Catalog - ";
            repositoryEntity = embeddedRepoEntity;
        } else if (serviceRepoEntity != null) {
            catalogType = "Service Catalog - ";
            repositoryEntity = serviceRepoEntity;
        }
        firmwareComplianceReport.setFirmwareRepositoryName(catalogType + repositoryEntity.getName());
        firmwareComplianceReport.setServiceTag(deviceInventoryEntity.getServiceTag());
        firmwareComplianceReport.setIpAddress(deviceInventoryEntity.getIpAddress());
        firmwareComplianceReport.setDeviceType(deviceInventoryEntity.getDeviceType());
        firmwareComplianceReport.setModel(deviceInventoryEntity.getModel());
        ManagedDevice device = DeviceInventoryUtils.toDTO(deviceInventoryEntity, true);
        firmwareComplianceReport.setManagedState(device.getManagedState());
        firmwareComplianceReport.setAvailable(!device.isInUse());
        firmwareComplianceReport.setEmbededRepo(repositoryEntity.isEmbedded());
        firmwareComplianceReport.setDeviceState(deviceInventoryEntity.getState());

        List<FirmwareComplianceReportComponent> svrComps = null;
        if (defaultRepoEntity != null || embeddedRepoEntity != null) {
            svrComps = this.getComponentsForDefaultAndEmbeddedRepos(deviceInventoryEntity,
                                                                    embeddedRepoEntity,
                                                                    defaultRepoEntity);
        } else if (serviceRepoEntity != null) {
            svrComps = this.getComponentsForServiceRepo(deviceInventoryEntity, serviceRepoEntity);
        }
        if (svrComps != null) {
            firmwareComplianceReport.setFirmwareComplianceReportComponents(svrComps);
        }

        // Make sure it's compliant
        for (FirmwareComplianceReportComponent fcrc : firmwareComplianceReport.getFirmwareComplianceReportComponents()) {
            if (!fcrc.isCompliant()) {
                firmwareComplianceReport.setCompliant(false);
                break;
            }
        }
        
        return firmwareComplianceReport;
    }

    /**
     * Returns a FirmwareComplianceReport for the given Device.  If a Device is part of a Service, then the report 
     * is generated against the Firmware Repository assigned to the Service.  If a Device is not part of the Service, 
     * then each component will be checked against the default Firmware Repository.  If no component is found in the 
     * default Firmware Repository (or no default is set), then the component will be checked against the embedded
     * Firmware Repository that ships with ASM.  
     * 
     * @param deviceInventoryEntity the Device whom the Firmware Compliance Report is generated against.
     * @return a FirmwareComplianceReport for the given Device.
     */
    public FirmwareComplianceReport getFirmwareComplianceReportForDevice(DeviceInventoryEntity deviceInventoryEntity) {

        FirmwareComplianceReport firmwareComplianceReport = null;

        // If Device is part of a Deployment process the FirmwareRepository for that Service 
        if (deviceInventoryEntity.getDeployments() != null && deviceInventoryEntity.getDeployments().size() > 0 && 
                deviceInventoryEntity.getDeployments().get(0).isManageFirmware() && 
                !deviceInventoryEntity.getDeviceType().isSharedDevice()) { // Shared Devices will be part of multiple services
            DeploymentEntity deploymentEntity = 
                    this.deploymentDAO.getDeployment(deviceInventoryEntity.getDeployments().get(0).getId(), DeploymentDAO.ALL_ENTITIES);
            FirmwareRepositoryEntity serviceRepoEntity = deploymentEntity.getFirmwareRepositoryEntity();
            
            firmwareComplianceReport = getFirmwareComplianceReportForDevice(deviceInventoryEntity, null, null, serviceRepoEntity);
        }
        else {
            FirmwareRepositoryEntity defaultRepo = this.getDefaultRepo();
            FirmwareRepositoryEntity embeddedRepo = this.getEmbeddedRepo();
            firmwareComplianceReport = getFirmwareComplianceReportForDevice(deviceInventoryEntity,
                                                                            defaultRepo,
                                                                            embeddedRepo,
                                                                            null);
        }
        if (firmwareComplianceReport != null) {
            firmwareComplianceReport.sortFirmwareComplianceReportComponentsByNameAndStatus();
        }
        return firmwareComplianceReport;
    }

    public List<FirmwareComplianceReportComponent> getComponentsForDefaultAndEmbeddedRepos(DeviceInventoryEntity deviceInventoryEntity,
                                                                                            FirmwareRepositoryEntity embeddedRepoEntity,
                                                                                            FirmwareRepositoryEntity defaultRepoEntity) {
        ArrayList<FirmwareComplianceReportComponent> firmwareComplianceReportComponents =
                new ArrayList<FirmwareComplianceReportComponent>();

        Set<FirmwareDeviceInventoryEntity> firmDeviceInvEntities =
                this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(deviceInventoryEntity.getRefId());

        for (FirmwareDeviceInventoryEntity firmDevInvEntity : firmDeviceInvEntities) {
            FirmwareComplianceReportComponent firmwareComplianceReportComponent = new FirmwareComplianceReportComponent();
            if(ComponentType.DRIVER.getValue().equals(firmDevInvEntity.getComponentType())) {
                firmwareComplianceReportComponent.setSoftware(true);
                firmwareComplianceReportComponent.setVendor(firmDevInvEntity.getVendorID());
            }
            
            firmwareComplianceReportComponent.setName(firmDevInvEntity.getName());
            firmwareComplianceReportComponent.setId(firmDevInvEntity.getId());
            firmwareComplianceReportComponent.setCompliant(true);

            boolean loadDriverType = !(firmDevInvEntity.getOperatingSystem() == null || 
                                     firmDevInvEntity.getOperatingSystem().trim().isEmpty());
            
            // If loadDriverType is true then it means it's from the Catalog, and there will be no systemId
            SoftwareComponent defaultSoftwareComponent = this.getSoftwareComponent(firmDevInvEntity, 
                                                                                   defaultRepoEntity, 
                                                                                   deviceInventoryEntity.getSystemId(),
                                                                                   loadDriverType);
            SoftwareComponent embeddedSoftwareComponent = this.getSoftwareComponent(firmDevInvEntity, 
                                                                                    embeddedRepoEntity, 
                                                                                    deviceInventoryEntity.getSystemId(),
                                                                                    loadDriverType);
            
            // Unsupported Devices will not have any components returned.
            FirmwareComplianceReportComponentVersionInfo currentVersion = this.getVersionInfo(firmDevInvEntity);
            FirmwareComplianceReportComponentVersionInfo targetVersion = this.getVersionInfo(firmDevInvEntity);
            
            currentVersion.setFirmwareLevel(FirmwareComplianceReportComponentVersionInfo.FIRMWARE_LEVEL_SERVICE);
            targetVersion.setFirmwareLevel(FirmwareComplianceReportComponentVersionInfo.FIRMWARE_LEVEL_SERVICE);
            
            // Set the TargetVersion
            targetVersion.setFirmwareVersion((defaultSoftwareComponent != null) ? defaultSoftwareComponent.getVendorVersion() : 
                (embeddedSoftwareComponent != null) ? embeddedSoftwareComponent.getVendorVersion() : NOT_AVAILABLE);
            
            // Set Compliance for Component
            if (embeddedSoftwareComponent != null) {
                if (VersionUtils.compareVersions(currentVersion.getFirmwareVersion(), 
                                                 embeddedSoftwareComponent.getVendorVersion()) >= 0) {
                    if (defaultSoftwareComponent != null) {
                        // Only ok if the firmware exactly matches the default
                        if (VersionUtils.compareVersions(currentVersion.getFirmwareVersion(), 
                                                         defaultSoftwareComponent.getVendorVersion()) == 0) {
                            firmwareComplianceReportComponent.setCompliant(true);
                        } else {
                            firmwareComplianceReportComponent.setCompliant(false);
                        }
                    } else {
                        // There is no default software component set therefore we default to compliant
                        firmwareComplianceReportComponent.setCompliant(true);
                    }
                } else {
                    // Firmware is non-compliant because it is less than our embedded minimums
                    firmwareComplianceReportComponent.setCompliant(false);
                }
            } else {
                if (defaultSoftwareComponent != null) {
                    // Only ok if the firmware exactly matches the default
                    if (VersionUtils.compareVersions(currentVersion.getFirmwareVersion(), 
                                                     defaultSoftwareComponent.getVendorVersion()) == 0) {
                        firmwareComplianceReportComponent.setCompliant(true);
                    } else {
                        firmwareComplianceReportComponent.setCompliant(false);
                    }
                } else {
                    // there are no known software components therfore we default to compliant
                    firmwareComplianceReportComponent.setCompliant(true);
                }
            }   
            
            firmwareComplianceReportComponent.setCurrentVersion(currentVersion);
            firmwareComplianceReportComponent.setTargetVersion(targetVersion);
            firmwareComplianceReportComponents.add(firmwareComplianceReportComponent);
        }

        return firmwareComplianceReportComponents;
    }
    
    private List<FirmwareComplianceReportComponent> getComponentsForServiceRepo(DeviceInventoryEntity deviceInventoryEntity,
                                                                                FirmwareRepositoryEntity serviceRepoEntity) {
        ArrayList<FirmwareComplianceReportComponent> firmwareComplianceReportComponents =
                new ArrayList<FirmwareComplianceReportComponent>();

        Set<FirmwareDeviceInventoryEntity> firmDeviceInvEntities =
                this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(deviceInventoryEntity.getRefId());

        for (FirmwareDeviceInventoryEntity firmDevInvEntity : firmDeviceInvEntities) {
            FirmwareComplianceReportComponent firmwareComplianceReportComponent = new FirmwareComplianceReportComponent();
            if(ComponentType.DRIVER.getValue().equals(firmDevInvEntity.getComponentType())) {
                firmwareComplianceReportComponent.setSoftware(true);
                firmwareComplianceReportComponent.setVendor(firmDevInvEntity.getVendorID());
            }
            firmwareComplianceReportComponent.setName(firmDevInvEntity.getName());
            firmwareComplianceReportComponent.setId(firmDevInvEntity.getId());
            firmwareComplianceReportComponent.setCompliant(true);

            // Hack so we load things properly for Catalog items
            boolean loadDriverType = !(firmDevInvEntity.getOperatingSystem() == null || 
                                     firmDevInvEntity.getOperatingSystem().trim().isEmpty());
            String systemId = deviceInventoryEntity.getSystemId();
            if (loadDriverType) { // Means it's from the Catalog, and there will be no systemId
                systemId = null;
            }
            
            
            SoftwareComponent softwareComponent = this.getSoftwareComponent(firmDevInvEntity, serviceRepoEntity, systemId, loadDriverType);
            
            // Unsupported Devices will not have any components returned.
            FirmwareComplianceReportComponentVersionInfo currentVersion = this.getVersionInfo(firmDevInvEntity);
            FirmwareComplianceReportComponentVersionInfo targetVersion = this.getVersionInfo(firmDevInvEntity);
            
            currentVersion.setFirmwareLevel(FirmwareComplianceReportComponentVersionInfo.FIRMWARE_LEVEL_SERVICE);
            targetVersion.setFirmwareLevel(FirmwareComplianceReportComponentVersionInfo.FIRMWARE_LEVEL_SERVICE);
            
            if (!SourceType.Catalog.getValue().equals(firmDevInvEntity.getSource())) {
                if(softwareComponent != null) {
                    targetVersion.setFirmwareVersion(softwareComponent.getVendorVersion());
                    
                    if(VersionUtils.compareVersions(currentVersion.getFirmwareVersion(), targetVersion.getFirmwareVersion()) != 0) {
                        firmwareComplianceReportComponent.setCompliant(false);
                    }
                }
                else { // Nothing to match against so considered compliant
                    targetVersion.setFirmwareVersion(NOT_AVAILABLE);
                }
            } else {
                currentVersion.setFirmwareVersion(NOT_AVAILABLE);  // There is no current version for a Catalog as it's not installed yet
                firmwareComplianceReportComponent.setCompliant(false);
            }
            
            firmwareComplianceReportComponent.setCurrentVersion(currentVersion);
            firmwareComplianceReportComponent.setTargetVersion(targetVersion);
            firmwareComplianceReportComponents.add(firmwareComplianceReportComponent);
        }

        return firmwareComplianceReportComponents;
    }

    // Returns a populated VersionInfo object 
    private FirmwareComplianceReportComponentVersionInfo getVersionInfo(
            FirmwareDeviceInventoryEntity firmDevInvEntity) {
        FirmwareComplianceReportComponentVersionInfo versionInfo = new FirmwareComplianceReportComponentVersionInfo();

        versionInfo.setFirmwareLastUpdateTime(firmDevInvEntity.getLastUpdateTime());
        versionInfo.setFirmwareName(firmDevInvEntity.getName());
        versionInfo.setFirmwareType(firmDevInvEntity.getComponentType());
        versionInfo.setFirmwareVersion(
                (StringUtils.isNotBlank(firmDevInvEntity.getVersion())) ? firmDevInvEntity.getVersion() : NOT_AVAILABLE);
        versionInfo.setId(firmDevInvEntity.getId());
        
        return versionInfo;
    }
    
    // Returns the SoftwareComponent for a given firmware repository
    private SoftwareComponent getSoftwareComponent(FirmwareDeviceInventoryEntity fdi,
                                                   FirmwareRepositoryEntity firmRepoEntity,
                                                   String systemId, 
                                                   boolean loadDriverType) {
        
        List<SoftwareComponent> softwareComponents = this.getSoftwareComponents(fdi.getComponentID(), 
                fdi.getDeviceID(), fdi.getSubdeviceID(), fdi.getVendorID(), fdi.getSubvendorID(), firmRepoEntity, 
                systemId, SourceType.valueOf(fdi.getSource()), fdi.getOperatingSystem(), loadDriverType);

        // Adding this check and logging to help identify conditions when more than one software component is 
        // returned. We should always only see one component but there may be some ambiguity in the codebase that allows
        // for more than one software component to be returned in the list.
        if (CollectionUtils.isNotEmpty(softwareComponents) && (softwareComponents.size() > 1)) {
            logger.warn(String.format(
                    "More than one default software component was returned for the given firmware device %s", fdi));
        }
                
        return (CollectionUtils.isNotEmpty(softwareComponents)) ? softwareComponents.get(0) : null;
    }    
 
    
    /**
     * Fetch OS software packages for our server device and save to relevant database
     * @param devInv DeviceInventoryEntity object
     */
    public Set<FirmwareDeviceInventory> fetchVibsFirmwareDeviceInventory(DeviceInventoryEntity devInv, String parentJobId, String jobId) {
        Set<FirmwareDeviceInventory> firmwareDeviceInventory = null;
        
        // Ideally the server should be deployed before we retrieve OS data, but we're going to check every time in 
        // case it's in another state.  We're catching the exception below, so it should recover properly if we're 
        // unable to communicate with the server.  
        try {
            if (devInv != null) {
                logger.debug("Fetching software package inventory for " + devInv.getDisplayName());
                List<DeploymentEntity> deploymentEntities = devInv.getDeployments();
                String osIpAddress = devInv.getOsIpAddress(); 
                String hostIpAddress = devInv.getOsIpAddress(); // host-ip is osIpAddress 
                if (deploymentEntities != null && deploymentEntities.size() == 1) { // For server, there is only one deployment service
                    // For our deployment object, fetch our desired template settings
                    DeploymentEntity entity = deploymentEntities.get(0);
                    HashSet<String> ids = new HashSet<String>();
                    ids.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
                    ids.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
                    if (osIpAddress == null) {
                        ids.add(ServiceTemplateSettingIDs.HYPERVISOR_IP_ADDRESS);
                    }
                    String marshalledTemplateData = entity.getMarshalledTemplateData();
                    if (!StringUtils.isBlank(marshalledTemplateData)) {
                        ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, marshalledTemplateData);
                        Map<String,String> parameterValues =
                                ServiceDeploymentUtil.getDeployedTemplateParameterValues(template, devInv.getRefId(),
                                                                                         ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, ids);
                        String os_type = parameterValues.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);

                        // If the deployed OS is ESXi, fetch packages for ESXi
                        logger.debug("Deployed OS retrieved from service template settings: " + os_type);
                        if (os_type != null && os_type.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE)) {
                            String esxCredentialId = parameterValues.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ADMIN_PASSWORD_ID);
                            // In Brownfield based Services, there will be no no OS Password available, so we need to 
                            // pass in the VCenter Credential instead. 
							if (esxCredentialId == null || esxCredentialId.isEmpty()) {
								DeviceInventoryEntity vcenterDie = this.findAssociatedVCenterDie(template, devInv);
								esxCredentialId = vcenterDie.getCredId();
								hostIpAddress = vcenterDie.getIpAddress(); // needs to equal vcenterIp here
							}
                            if (osIpAddress == null || osIpAddress.isEmpty()) {
                            	ServiceTemplateComponent templateComponent = template.findComponentByGUID(devInv.getRefId());
                            	osIpAddress = this.getHostNameOrIpAddress(templateComponent);
                            }
                            
                            PuppetIdracServerDevice puppetIdracServerDevice = fetchEsxiSoftwareInventory(esxCredentialId, hostIpAddress, osIpAddress);
                            if (puppetIdracServerDevice != null) {
                                firmwareDeviceInventory = getFirmwareDeviceInventory(devInv, puppetIdracServerDevice, parentJobId, jobId);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)  {
            logger.warn("Failed to get software package inventory on installed OS" , e);
        }
        
        return firmwareDeviceInventory;
    }

    /**
     * Fetch ESXi software packages inventory via ESXi puppet device
     * @param credentialId this could be the OS admin password Id of the ESXi host or it could be the VCenter 
     * 		credentialId.  The credentialId should go along with the hostIpAddress.
     * @param hostIpAddress this will either be the ip address of the hostIpAddress or it will be the VCenter IP address
     * 		there was no Credential (password) available of the ESXI OS (as in the case of brownfield)
     * @param osIpAddress IP address of the ESXi Operating System
     * @return String representing json-formatted software inventory data
     * @throws Exception
     */
	private PuppetIdracServerDevice fetchEsxiSoftwareInventory(String credentialId, 
															   String hostIpAddress,
															   String osIpAddress) throws Exception {
        PuppetIdracServerDevice puppetIdracServerDevice = null;
        
        if (credentialId == null || hostIpAddress == null || osIpAddress == null) {
            logger.error("Cannot get OS package inventory due to missing parameters ");
        }
        else {
            DiscoverDeviceType vCenterDeviceType = DiscoverDeviceType.VCENTER;

            PuppetDiscoveryRequest request = new PuppetDiscoveryRequest(hostIpAddress,
                    vCenterDeviceType.getPuppetModuleName(),
                    credentialId,
                    vCenterDeviceType.getConnectType());
            request.setExistingRefId("esxi-" + osIpAddress);
            request.setOsIpAddress(osIpAddress);
            
            request.setScriptPath(vCenterDeviceType.getPuppetModuleName() + "/bin/esx_software_discovery.rb");
            Map<String, String> facts = PuppetModuleUtil.discoverAndShowFacts(request, true);
            if (facts == null || facts.size() <= 0 || facts.get("installed_packages") == null || facts.get("installed_packages").isEmpty()) {
                logger.error("Cannot get OS installed packages from puppet facts");
            }
            else {
                ArrayList<HashMap> deviceDetails = OBJECT_MAPPER.readValue(facts.get("installed_packages"), ArrayList.class);
                if (deviceDetails != null) {
                	logger.debug("VIBS installed_packages: " + deviceDetails.size() + " when calling fetchEsxiSoftwareInventory(String, String)");
                }
                else { // Log and add Exception so we can have a stacktrace to see what flow this is occurring on
                	logger.warn("No VIBS installed_packages available when calling fetchEsxiSoftwareInventory(String, String)!", new Exception());
                }
                puppetIdracServerDevice = PuppetDbUtil.convertToPuppetIdracServerDevice(deviceDetails);
                puppetIdracServerDevice.setEsxVersion(facts.get("esx_version"));
            }
        }
        
        return puppetIdracServerDevice;
    }

    private Set<FirmwareDeviceInventory> getFirmwareDeviceInventory(DeviceInventoryEntity devInvEntity, 
                                                                    PuppetIdracServerDevice puppetIdracServerDevice, 
                                                                    String parentJobId, 
                                                                    String jobId) {
        Set<FirmwareDeviceInventory> currentFirmwareInventory = new HashSet<FirmwareDeviceInventory>();
        ManagedDevice managedDevice = DeviceInventoryUtils.toDTO(devInvEntity, false, false);
        
        String operatingSystemRev = this.getEsxiOperatingSystemRev(puppetIdracServerDevice.getEsxVersion());
        
        // Get the DeviceInventory from the Server itself
        if (puppetIdracServerDevice != null && 
                puppetIdracServerDevice.getVibsInfos() != null && 
                !puppetIdracServerDevice.getVibsInfos().isEmpty()) {
            for (PuppetIdracServerDevice.VibsInfo vibsInfo : puppetIdracServerDevice.getVibsInfos()) {
                FirmwareDeviceInventory frmDeviceInv = new FirmwareDeviceInventory();
                frmDeviceInv.setComponentType(ComponentType.DRIVER.getValue());
                frmDeviceInv.setComponentID(vibsInfo.getName());  
                frmDeviceInv.setDeviceID(null); // No DeviceId for Vibs
                frmDeviceInv.setDeviceInventory(managedDevice);
                frmDeviceInv.setFqdd(vibsInfo.getId());
                frmDeviceInv.setIpaddress(devInvEntity.getIpAddress());
                frmDeviceInv.setJobId(jobId);
                frmDeviceInv.setName(vibsInfo.getName());
                frmDeviceInv.setOperatingSystem(operatingSystemRev);
                frmDeviceInv.setParent_job_id(parentJobId);
                frmDeviceInv.setServicetag(devInvEntity.getServiceTag());
                frmDeviceInv.setSourceType(SourceType.Device);
                frmDeviceInv.setSubdeviceID(null); // No SubDeviceId for Vibs
                frmDeviceInv.setSubvendorID(null); // No SubVendor for Vibs
                frmDeviceInv.setVendorID(vibsInfo.getVendor());
                frmDeviceInv.setVersion(vibsInfo.getVersion());
                frmDeviceInv.setLastUpdateTime(this.getDate(vibsInfo.getCreationDate()));  // Not using Install Date
                currentFirmwareInventory.add(frmDeviceInv);
            }
        }
        
        // Get the SoftwareInventory from the Catalog that's assigned to this Server's Service
        if (devInvEntity.getDeployments() != null && !devInvEntity.getDeployments().isEmpty()) {
            DeploymentEntity deploymentEntity = devInvEntity.getDeployments().get(0);
            // Get deploymentEntity with repository
            deploymentEntity = this.deploymentDAO.getDeployment(deploymentEntity.getId(), DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY);
            if (deploymentEntity != null && deploymentEntity.getFirmwareRepositoryEntity() != null) {

                // Only add the Software ones whose operating system match that of the Server                
                List<SoftwareComponentEntity> softCompEntities = 
                        firmwareRepositoryDAO.getSoftwareComponentEntitiesWithOperatingSystem(deploymentEntity.getFirmwareRepositoryEntity().getId(), operatingSystemRev);
           
                for (SoftwareComponentEntity softCompEntity : softCompEntities) {
                    FirmwareDeviceInventory frmDeviceInv = new FirmwareDeviceInventory();
                    frmDeviceInv.setComponentType(ComponentType.DRIVER.getValue()); // MUST be DRIVER, so safe to set
                    frmDeviceInv.setComponentID(softCompEntity.getComponentId());  
                    frmDeviceInv.setDeviceID(softCompEntity.getDeviceId()); // should be null
                    frmDeviceInv.setDeviceInventory(managedDevice);
                    frmDeviceInv.setFqdd(softCompEntity.getPackageId()); // Not quite the same as vibs id, but close
                    frmDeviceInv.setIpaddress(devInvEntity.getIpAddress());
                    frmDeviceInv.setJobId(jobId);
                    frmDeviceInv.setName(softCompEntity.getName());
                    frmDeviceInv.setOperatingSystem(operatingSystemRev);
                    frmDeviceInv.setParent_job_id(parentJobId);
                    frmDeviceInv.setServicetag(devInvEntity.getServiceTag());
                    frmDeviceInv.setSubdeviceID(null); // No SubDeviceId for Vibs
                    frmDeviceInv.setSubvendorID(null); // No SubVendor for Vibs
                    frmDeviceInv.setVendorID(softCompEntity.getVendorId());
                    frmDeviceInv.setVersion(softCompEntity.getVendorVersion());
                    frmDeviceInv.setLastUpdateTime(softCompEntity.getUpdatedDate());
                    frmDeviceInv.setSourceType(SourceType.Catalog);
                    
                    // If the Component already exists we'll try to update the name to that of the Catalogs
                    FirmwareDeviceInventory matchFrmDevInv = 
                            this.findFirmwareDeviceInventory(currentFirmwareInventory, frmDeviceInv);
                    if (matchFrmDevInv != null) {
                        matchFrmDevInv.setName(frmDeviceInv.getName());
                        matchFrmDevInv.setFqdd(frmDeviceInv.getFqdd());
                    } // if we can't find it, then we'll simply add the catalog to the firmware inventory
                    else {
                        currentFirmwareInventory.add(frmDeviceInv);
                    }
                }
            }
        }
        
        return currentFirmwareInventory;
    }

    private FirmwareDeviceInventory findFirmwareDeviceInventory(Set<FirmwareDeviceInventory> frmDevInvs, 
                                                                FirmwareDeviceInventory frmDevInvToFind) {
        FirmwareDeviceInventory foundFrmDeviceInv = null;

        if (frmDevInvs != null && !frmDevInvs.isEmpty() && frmDevInvToFind != null) {
            for (FirmwareDeviceInventory firmDevInv : frmDevInvs) {
                if (StringUtils.equals(firmDevInv.getComponentID(), frmDevInvToFind.getComponentID()) &&
                        StringUtils.equals(firmDevInv.getComponentType(), frmDevInvToFind.getComponentType()) &&
                        StringUtils.equals(firmDevInv.getVendorID(), frmDevInvToFind.getVendorID()) &&
                        StringUtils.equals(firmDevInv.getServicetag(), frmDevInvToFind.getServicetag()) &&
                        StringUtils.equals(firmDevInv.getIpaddress(), frmDevInvToFind.getIpaddress()) &&
                        StringUtils.equals(firmDevInv.getOperatingSystem(), frmDevInvToFind.getOperatingSystem())) {
                    foundFrmDeviceInv = firmDevInv;
                    break;
                }
            }
        }
        
        return foundFrmDeviceInv;
    }
    
    private Date getDate(String dateStr) {
        Date date = null;
        if (dateStr != null) {
            date = DATE_FORMATTER.parseDateTime(dateStr).toDate();  // example date 2016-06-07
        }
        return date;
    }

    /**
     * This method will clear the Software Inventory for relevant Servers in a deployment and then update them again.
     *
     * @param deploymentEntity
     * @throws AsmManagerCheckedException
     */
    public void updateServersSoftwareDeviceInventory(DeploymentEntity deploymentEntity) throws AsmManagerCheckedException {
        if (deploymentEntity != null && deploymentEntity.getDeployedDevices() != null) {
            String jobId = UUID.randomUUID().toString();
            String parentJobId = UUID.randomUUID().toString();
            for (DeviceInventoryEntity deviceInvEnt : deploymentEntity.getDeployedDevices()) {
                if (deviceInvEnt.getDeviceType().isServer()) {
                    // Remove existing Inventory
                    this.deviceInventoryDAO.deleteSoftwareDeviceInventoryForDevice(deviceInvEnt.getRefId());

                    // Generate the new Inventory
                    Set<FirmwareDeviceInventory> firmDevInvSet = this.fetchVibsFirmwareDeviceInventory(deviceInvEnt, parentJobId, jobId);
                    if (firmDevInvSet != null) {
                        for (FirmwareDeviceInventory firmDevInv : firmDevInvSet) {
                            this.deviceInventoryDAO.createFirmwareDeviceInventory(new FirmwareDeviceInventoryEntity(firmDevInv));
                        }
                    }
                }
            }
        }
    }

    public static JobDetail getUpdateFirmwareJob(final IJobManager jobManager,
                                                 final String deploymentId,
                                                 final boolean exitMaintenance,
                                                 final boolean nextReboot,
                                                 final String updateType,
                                                 final String componentsToUpdate,
                                                 final String firmwareRepositoryId) {
        return getUpdateFirmwareJob(jobManager,deploymentId,exitMaintenance,nextReboot,updateType,componentsToUpdate,firmwareRepositoryId,null);
    }

    public static JobDetail getUpdateFirmwareJob(final IJobManager jobManager,
                                                 final String deploymentId,
                                                 final boolean exitMaintenance,
                                                 final boolean nextReboot,
                                                 final String updateType,
                                                 final String componentsToUpdate,
                                                 final String firmwareRepositoryId,
                                                 final String groupName) {
        JobDetail job;
        if (groupName != null) {
            JobCreateSpec jobspec = new JobCreateSpec(FirmwareUpdateJob.class);
            jobspec.setDescription(groupName);
            jobspec.setSelector(groupName);
            // Setting true will allow us to search jobs by groupName
            job = jobManager.createNamedJob(jobspec,true);
        } else {
            job = jobManager.createNamedJob(FirmwareUpdateJob.class);
        }
        job.getJobDataMap().put(FirmwareUpdateJob.GROUP_SELECTOR_KEY, groupName);
        job.getJobDataMap().put(FirmwareUpdateJob.UPDATE_KEY, deploymentId);
        job.getJobDataMap().put(FirmwareUpdateJob.EXIT_MAINTANACE, exitMaintenance);
        job.getJobDataMap().put(FirmwareUpdateJob.NEXT_REBOOT, nextReboot);
        job.getJobDataMap().put(FirmwareUpdateJob.UPDATE_TYPE_KEY, updateType);
        job.getJobDataMap().put(FirmwareUpdateJob.COMPONENTS_TO_UPDATE,componentsToUpdate);
        if (firmwareRepositoryId != null) {
            job.getJobDataMap().put(FirmwareUpdateJob.FIRMWARE_REPOSITORY, firmwareRepositoryId);
        }
        return job;
    }

    /**
     * Used to reset a deployment to unmanaged firmware state.  It should update the FirmwareRepositoryEntity, but
     * does not update the deployment entity on purpose.
     * @param deploymentEntity deploymentEntity to reset managing firmware
     */
    public void unmanageFirmware(DeploymentEntity deploymentEntity) {
        if (deploymentEntity != null) {
            deploymentEntity.setManageFirmware(false);
            deploymentEntity.setUseDefaultCatalog(false);
            if (deploymentEntity.getFirmwareRepositoryEntity() != null) {
                FirmwareRepositoryEntity firmware = deploymentEntity.getFirmwareRepositoryEntity();
                firmware.removeDeployment(deploymentEntity);
                firmwareRepositoryDAO.saveOrUpdate(firmware);
            }
        }
    }

    public void manageServiceTemplateFirmware(ServiceTemplate serviceTemplate, ServiceTemplateEntity serviceTemplateEntity) {
        if (serviceTemplate != null && serviceTemplateEntity != null) {
            if (serviceTemplate.isManageFirmware()) {
                FirmwareRepositoryEntity firmware = null;
                if (serviceTemplate.isUseDefaultCatalog()) {
                    serviceTemplateEntity.setUseDefaultCatalog(true);
                    serviceTemplateEntity.setFirmwareRepositoryEntity(null);
                } else if (serviceTemplate.getFirmwareRepository() != null) {
                    firmware = firmwareRepositoryDAO.getCompleteFirmware(serviceTemplate.getFirmwareRepository().getId(), true, true);
                    if (firmware != null) {
                        serviceTemplateEntity.setFirmwareRepositoryEntity(firmware);
                    }
                }
            } else {
                serviceTemplate.setUseDefaultCatalog(false);
                serviceTemplate.setFirmwareRepository(null);
                serviceTemplateEntity.setUseDefaultCatalog(false);
                serviceTemplateEntity.setFirmwareRepositoryEntity(null);
            }
        }
    }

    public boolean manageDeploymentFirmware(Deployment deployment, DeploymentEntity deploymentEntity) {
        boolean manageFirmware = false;
        if (deployment != null && deploymentEntity != null) {
            if (deployment.isUpdateServerFirmware()) {
                FirmwareRepositoryEntity firmwarerepoEntity = null;
                if (deployment.isUseDefaultCatalog()) {
                    deploymentEntity.setUseDefaultCatalog(true);
                    firmwarerepoEntity = this.getDefaultRepo();
                } else if (deployment.getFirmwareRepositoryId() != null) {
                    deploymentEntity.setUseDefaultCatalog(false);
                    firmwarerepoEntity = firmwareRepositoryDAO.get(deployment.getFirmwareRepositoryId()); 
                } else if (deployment.getFirmwareRepository() != null) {
                    deploymentEntity.setUseDefaultCatalog(false);
                    firmwarerepoEntity = firmwareRepositoryDAO.get(deployment.getFirmwareRepository().getId());
                }
                if (firmwarerepoEntity != null) {
                    manageFirmware = true;
                    firmwarerepoEntity.addDeployment(deploymentEntity);
                    deploymentEntity.setManageFirmware(true);
                    deployment.setFirmwareRepositoryId(firmwarerepoEntity.getId());
                    deployment.setFirmwareRepository(firmwarerepoEntity.getSimpleFirmwareRepository());
                    deploymentEntity.setFirmwareRepositoryEntity(firmwarerepoEntity);
                } else {
                    logger.error("Could not find specified firmware repository to manage deployment firmware: " + deployment.getFirmwareRepositoryId());
                }
            }  else {
                this.unmanageFirmware(deploymentEntity);
                deployment.setUseDefaultCatalog(false);
                deployment.setFirmwareRepository(null);
                deployment.setFirmwareRepositoryId(null);
            }
        }
        return manageFirmware;
    }
    
    /**
     * This method will clear the Software Inventory from a given Catalog for relevant Servers in a deployment and 
     * then update the Software Inventory with the correct catalog Inventory.
     *
     * @param deploymentEntity the deployment whose servers catalog software device inventory will be updated.
     * @throws AsmManagerCheckedException
     */
    public void updateCatalogBasedSoftwareDeviceInventory(DeploymentEntity deploymentEntity) 
            throws AsmManagerCheckedException {
        if (deploymentEntity != null && deploymentEntity.getDeployedDevices() != null) {
            String jobId = UUID.randomUUID().toString();
            String parentJobId = UUID.randomUUID().toString();
            for (DeviceInventoryEntity deviceInvEnt : deploymentEntity.getDeployedDevices()) {
                if (deviceInvEnt.getDeviceType().isServer()) {
                    // Remove existing Inventory
                    this.deviceInventoryDAO.deleteFirmwareDeviceInventoryForDevice(deviceInvEnt.getRefId(), SourceType.Catalog);

                    // Generate the new Inventory
                    Set<FirmwareDeviceInventory> catalogFirmwareInv = this.getCatalogFirmwareDeviceInventory(deploymentEntity, deviceInvEnt, parentJobId);
                    
                    if (catalogFirmwareInv != null) {
                        for (FirmwareDeviceInventory firmDevInv : catalogFirmwareInv) {
                            this.deviceInventoryDAO.createFirmwareDeviceInventory(new FirmwareDeviceInventoryEntity(firmDevInv));
                        }
                    }
                }
            }
        }
    }
    
    private Set<FirmwareDeviceInventory> getCatalogFirmwareDeviceInventory(DeploymentEntity deploymentEntity, 
                                                                           DeviceInventoryEntity devInvEntity,
                                                                           String parentJobId) {
        
        Set<FirmwareDeviceInventory> currentFirmwareInventory = null; 
        
        if (deploymentEntity != null &&  
                deploymentEntity.getFirmwareRepositoryEntity() != null && 
                devInvEntity != null) {
            try {
                if (deploymentEntity != null) {
                    // For our deployment object, fetch our desired template settings
                    String marshalledTemplateData = deploymentEntity.getMarshalledTemplateData();
                    if (!StringUtils.isBlank(marshalledTemplateData)) {
                        ServiceTemplate serviceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class, marshalledTemplateData);
                
                        String osIpaddress = serviceTemplate.findEsxiOperatingSystemIp(devInvEntity.getRefId());
            
                        if(osIpaddress != null) {
                            ManagedDevice managedDevice = DeviceInventoryUtils.toDTO(devInvEntity, false, false);
                            Map<String, String> facts = PuppetModuleUtil.getPuppetDevice(PuppetModuleUtil.generateEsxiCertName(osIpaddress));

                            if (facts == null || facts.size() <= 0 || facts.get("installed_packages") == null || facts.get("installed_packages").isEmpty()) {
                                logger.error("Cannot get OS installed packages from puppet facts");
                            }
                            else {
                                ArrayList<HashMap> deviceDetails = OBJECT_MAPPER.readValue(facts.get("installed_packages"), ArrayList.class);
                                PuppetIdracServerDevice puppetIdracServerDevice = PuppetDbUtil.convertToPuppetIdracServerDevice(deviceDetails);
                                puppetIdracServerDevice.setEsxVersion(facts.get("esx_version"));
                                
                                currentFirmwareInventory = this.getFirmwareDeviceInventory(devInvEntity, puppetIdracServerDevice, parentJobId, parentJobId); 
                            }
                        }
                    }
                } 
            } catch (Exception e) {
                logger.warn("Unable to generate the catalog invnetory for device.", e);
            }
        }

        return currentFirmwareInventory;
    }
    
    /**
     * Creates or updates DeviceInventoryComplianceEntity for given DeviceInventoryEntity and FirmwareRepositoryEntity
     * 
     * @param firmwareRepository the repository to be checked against.
     * @param deviceInventory the device to be checked.
     */
    private void updateFirmwareComplianceMapForDevice(final FirmwareRepositoryEntity firmwareRepository,
                                                      final DeviceInventoryEntity deviceInventory) {
        DeviceInventoryComplianceEntity deviceInventoryCompliance = 
                new DeviceInventoryComplianceEntity(deviceInventory,firmwareRepository);
        // innocent until proven guilty: by default we assume device compliant
        deviceInventoryCompliance.setCompliance(CompliantState.COMPLIANT); 
        // Loop through all firmware / software component versions and validate they are compliant
        Set<FirmwareDeviceInventoryEntity> firmDevInvs = 
                this.deviceInventoryDAO.getFirmwareDeviceInventoryByRefId(deviceInventory.getRefId());
        for (final FirmwareDeviceInventoryEntity fdi : firmDevInvs) {
            // Check to see if it exists in the catalog, but not the server itself
            if (SourceType.Catalog.getValue().equals(fdi.getSource())) { // This means it does not exist on the server and only in the catalog
                deviceInventoryCompliance.setCompliance(CompliantState.NONCOMPLIANT);
                break;
            }
            // Check against the Default/Embedded for items that exist in the catalog & server
            final CompliantState compliance = getFirmwareCompliance(deviceInventory, firmwareRepository, fdi);
            // if the firmware is not compliant stop the checks and return
            if (!CompliantState.COMPLIANT.equals(compliance)) {
                deviceInventoryCompliance.setCompliance(compliance);
                break;
            }
        }
        deviceInventoryComplianceDAO.saveOrUpdate(deviceInventoryCompliance);
    }        
    
    /**
     *  Looks up the compliance from the compliance_map table for the given service repo and moves it to the 
     *  device_inventory table so it can be used for service compliance (primarily for performance).
     *  
     *  @param deviceInventoryEnts the devices whose compliance will be updated.
     *  @param serviceRepo the Firmware catalog from which the device's compliance will be updated.
     */
    public void updateFirmwareComplianceForDevices(List<DeviceInventoryEntity> deviceInventoryEnts, 
                                                   FirmwareRepositoryEntity serviceRepo) {
        
        if (deviceInventoryEnts != null && !deviceInventoryEnts.isEmpty()) {
            FirmwareRepositoryEntity defaultRepo = this.getDefaultRepo();
            FirmwareRepositoryEntity embeddedRepo = this.getEmbeddedRepo();
            
            for (DeviceInventoryEntity deviceInventory: deviceInventoryEnts) {
                this.updateDeviceInventoryComplianceStatus(deviceInventory, defaultRepo, embeddedRepo, serviceRepo);
            }
        }
    }
    
    /**
     * Updates the Device's compliance status based on rules for the given repositories.
     * 
     * @param deviceInventory the device whose compliance status is being updated.
     * @param defaultRepo the default repository (if it exists)
     * @param embeddedRepo the embedded repository (should always exists, ships with ASM)
     * @param serviceRepo the service repository (if it exists)
     */
    private void updateDeviceInventoryComplianceStatus(final DeviceInventoryEntity deviceInventory,
                                                       final FirmwareRepositoryEntity defaultRepo,
                                                       final FirmwareRepositoryEntity embeddedRepo,
                                                       final FirmwareRepositoryEntity serviceRepo) {
        
        DeviceInventoryComplianceEntity defaultComplianceAssoc =
                deviceInventoryComplianceDAO.get(deviceInventory, defaultRepo);
        DeviceInventoryComplianceEntity embeddedComplianceAssoc =
                deviceInventoryComplianceDAO.get(deviceInventory, embeddedRepo);
        DeviceInventoryComplianceEntity serviceComplianceAssoc =
                deviceInventoryComplianceDAO.get(deviceInventory, serviceRepo);
        
        CompliantState embeddedCompliance = null;
        CompliantState deviceCompliance = null;
        if (embeddedComplianceAssoc != null) {
            embeddedCompliance = embeddedComplianceAssoc.getCompliance();
        } else {
            // If no match is found in any catalog, this will ensure the state is set to uknown for any device.
            embeddedCompliance = CompliantState.UNKNOWN;  
        }

        if (embeddedCompliance != CompliantState.UPDATEREQUIRED &&
                defaultComplianceAssoc != null) {
            deviceCompliance = defaultComplianceAssoc.getCompliance();
        } 
        else {
            if (embeddedCompliance != null) {
                deviceCompliance = embeddedCompliance;
            }
        }
        
        // When there is a service we will use it as the server should not be able to be selected for deployment
        // if it was not initially compliant
        if (serviceComplianceAssoc != null) {
            // Only a non-shared service can have a service compliance in the device_inventory table. This effectively
            // ensures that only a default, or embedded value is ever stored in the device_inventory table's compliance
            // for a shared device.
            if (!deviceInventory.getDeviceType().isSharedDevice()) { 
                deviceCompliance = serviceComplianceAssoc.getCompliance();
            }
        }
        
        if (deviceCompliance != null) {
            try {
                deviceInventory.setCompliant(deviceCompliance.name());
                deviceInventory.setComplianceCheckDate(new GregorianCalendar());
                deviceInventoryDAO.updateDeviceInventory(deviceInventory);
            } catch (AsmManagerCheckedException e) {
                logger.error("Unable to update deviceInventoryDAO for " + deviceInventory.getIpAddress(), e);
            }
        } else {
            logger.error("Cannot get neither Default nor Embedded repo compliance status for device: " + deviceInventory.getIpAddress());
        }
    }       
    
    // Parses the puppetEsxVersion and converts it into the format necessary for the firmware
    private String getEsxiOperatingSystemRev(String puppetEsxVersion) {
        String operatingSystemRev = null;
        
        if (puppetEsxVersion != null) {
            String[] versionInfo = puppetEsxVersion.split("\\.");
            String majorVersion = "0";
            String minorVersion = "0";
            if (versionInfo.length >= 2) {
                if (versionInfo[0] != null && !versionInfo[0].trim().isEmpty()) {
                    majorVersion = versionInfo[0];
                }
                if (versionInfo[1] != null && !versionInfo[1].trim().isEmpty()) {
                    minorVersion = versionInfo[1];
                }
            }
    
            operatingSystemRev = "ESX" + majorVersion + "." + minorVersion;
        }
        
        return operatingSystemRev;
    }
    
    /**
     * Runs an inventory job and blocks until it completes.  Return true if it finishes or false if it timesout.
     * 
     * @param device the device to run and block on the inventory job.
     * @throws JobManagerException
     * @throws InterruptedException
     */
	public boolean runInventoryJobAndBlockUntilComplete(final DeviceInventoryEntity device)
			throws JobManagerException, InterruptedException {
		boolean successful = true;

		logger.debug("FirmwareUtil's runInventoryJobAndBlockUntilComplete starting for " + device.getRefId());
		final JobDetail updateInventoryJob = this.scheduleInventoryJobImmediately(device);
		final String updateInventoryJobName = updateInventoryJob.getKey().getName();
		JobStatus invJobStatus;
		final Calendar now = Calendar.getInstance();
		final Calendar endTime = Calendar.getInstance();
		endTime.add(Calendar.MINUTE, 5); // set the end time to 5 min from now
		do {
			Thread.sleep(20 * 1000); // every 20 seconds
			invJobStatus = JobStatus.valueOf(historyMgr.getExecHistoryStatus(updateInventoryJobName));
			now.setTime(new Date());
		} while ((JobStatus.STARTING.equals(invJobStatus) || JobStatus.IN_PROGRESS.equals(invJobStatus))
				&& (endTime.after(now)));

		if (!JobStatus.SUCCESSFUL.equals(invJobStatus)) {
			// inventory job failed or timed out
			logger.error("Inventory job failed or timed out!");
			successful = false;
		}

		logger.info("FirmwareUtil's runInventoryJobAndBlockUntilComplete is complete for device " + device.getRefId());  
		return successful;
	}
	
	private String getHostNameOrIpAddress(ServiceTemplateComponent templateComponent) {
		String hostNameOrIpAddress = null;
		if (templateComponent != null) {
			ServiceTemplateCategory resource = getResource(templateComponent,
					ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
			if (resource != null) {
				hostNameOrIpAddress = getHypervisorIp(templateComponent);
				if (StringUtils.isBlank(hostNameOrIpAddress)) {
					ServiceTemplateSetting osHostnameIdSetting = resource
							.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
					if (osHostnameIdSetting != null && StringUtils.isNotBlank(osHostnameIdSetting.getValue())) {
						hostNameOrIpAddress = osHostnameIdSetting.getValue();
					}
				}
				if (StringUtils.isBlank(hostNameOrIpAddress)) {
					logger.error("ESXI IP Address is not a valid IP or Hostname");
					throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
							AsmManagerMessages.esxiHostNotFound());
				}
			}
		}

		return hostNameOrIpAddress;
	}
	

	/**
	 * Note that the DeviceInventoryEntity passed in should already be tested to
	 * verify it has an ESXI OS as only an ESXI based server will be connected
	 * to a VCenter/Cluster.
	 */
	private DeviceInventoryEntity findAssociatedVCenterDie(ServiceTemplate serviceTemplate, DeviceInventoryEntity die) {
		DeviceInventoryEntity vcenterDie = null;

		ServiceTemplateComponent component = serviceTemplate.findComponentByGUID(die.getRefId());
		if (component.getAssociatedComponents() != null) {
			Set<String> associatedComponentsKeys = component.getAssociatedComponents().keySet();
			for (ServiceTemplateComponent cmp : serviceTemplate.getComponents()) {
				if (cmp.getType() == ServiceTemplateComponentType.CLUSTER
						&& associatedComponentsKeys.contains(cmp.getId())) {
					// Means we have a cluster associated...
					vcenterDie = this.deviceInventoryDAO.getDeviceInventory(cmp.getAsmGUID());
					break;
				}
			}
		}

		return vcenterDie;
	}

}
