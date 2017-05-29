package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.app.rest.ServiceTemplateService;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.db.DeploymentDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.business.timezonemanager.TimeZoneConfigurationMgr;
import com.dell.asm.common.model.TimeZoneDto;

import org.apache.log4j.Logger;

public class ServiceDeploymentUtil {
    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateService.class);

    // Class Variables
    private FirmwareUtil firmwareUtil = null;
    private DeploymentDAO deploymentDAO = null;
    private DeviceInventoryDAO deviceInventoryDAO = null;
    private DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = null;

    /**
     * Default constructor for the class.
     */
    public ServiceDeploymentUtil() {
        this(new FirmwareUtil(),
             new DeploymentDAO(),
             new DeviceInventoryDAO(),
             new DeviceInventoryComplianceDAO());
    }
    
    /**
     * Constructor that creates the necessary resources for the class.
     */
    public ServiceDeploymentUtil(FirmwareUtil firmwareUtil,
                                 DeploymentDAO deploymentDAO,
                                 DeviceInventoryDAO deviceInventoryDAO, 
                                 DeviceInventoryComplianceDAO deviceInventoryComplianceDAO) {
        this.firmwareUtil = firmwareUtil;
        this.deploymentDAO = deploymentDAO;
        this.deviceInventoryDAO = deviceInventoryDAO;
        this.deviceInventoryComplianceDAO = deviceInventoryComplianceDAO;
    }
    
    
    /**
     * Checks whether given device inventory is a server and exists in one and only one service deployment
     * @param device
     * @return
     */
    public static boolean isDeviceAServerInDeployment(final DeviceInventoryEntity device) {
        if (DeviceType.isServer(device.getDeviceType()) && CollectionUtils.isNotEmpty(device.getDeployments())) {
            assert((device.getDeployments().size() == 1)); // servers should only be in one deployment at a time
            return true;
        }
        
        return false;
    }
    
    /**
     * This method assumes the FirmwareUtil has updated the device_inventory_compliance value properly and the 
     * compliance_map table is in a proper state.
     * 
     * @param deployment
     * @throws AsmManagerCheckedException
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     */
    public final void runServiceComplianceCheck(final DeploymentEntity deployment,
                                                boolean saveDeployment) 
                    throws AsmManagerCheckedException, InvocationTargetException, IllegalAccessException {
        if (deployment != null) {
            if (deployment.isManageFirmware()) {
                // innocent until proven guilty - assume service is compliant
                deployment.setCompliant(true);
                for (DeviceInventoryEntity tempDevice : deployment.getDeployedDevices()) {
                    // compliance is only based on servers in deployment
                    if ((DeviceType.isServer(tempDevice.getDeviceType()) || tempDevice.getDeviceType().isFirmwareComplianceManaged())
                            && !DeviceState.UPDATING.equals(tempDevice.getState())) {
                        // Loads from compliance_map due to shared devices not being in the device_inventory compliance field
                        final DeviceInventoryComplianceEntity deviceCompliance =  
                                deviceInventoryComplianceDAO.get(tempDevice.getRefId(), deployment.getFirmwareRepositoryEntity());
                        if (CompliantState.NONCOMPLIANT.equals(deviceCompliance.getCompliance()) ||
                                CompliantState.UPDATEREQUIRED.equals(deviceCompliance.getCompliance())) {
                            deployment.setCompliant(false);
                        }
                    }
                }
                if (saveDeployment) {
                    deploymentDAO.updateDeployment(deployment);
                }
            }
            // if the deployment is in updating firmware and is not manage firmware then deployment status
            // should be complete and compliant
            // Or if not manage firmware, just set the compliance to true.
            else if (!deployment.isManageFirmware()
                    && DeploymentStatusType.FIRMWARE_UPDATING.equals(deployment.getStatus())) {
                deployment.setStatus(DeploymentStatusType.COMPLETE);
                deployment.setCompliant(true);
                deploymentDAO.updateDeployment(deployment);
                return;
            }
        }
    }

    
    /**
     * Strip un-needed fields and process some other settings
     * @param serviceTemplate
     */
    public static void prepareTemplateForDeployment(ServiceTemplate serviceTemplate) {
        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            Iterator<ServiceTemplateCategory> resourceIterator = component.getResources().iterator();
            while (resourceIterator.hasNext()) {
                ServiceTemplateCategory resource = resourceIterator.next();
                if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID)) {
                    // if no networking is defined, remove category
                    boolean networkingFound = false;
                    for (ServiceTemplateSetting param : resource.getParameters()) {
                        if ((param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID)
                                && StringUtils.isNotEmpty(param.getValue())
                                && ServiceTemplateUtil.checkDependency(component, param))
                                ||
                                (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID)
                                        && ServiceTemplateUtil.checkDependency(component, param))) {
                            networkingFound = true;
                            break;
                        }
                    }

                    if (!networkingFound) {
                        resourceIterator.remove();
                        continue; // next resource
                    }
                }

                Iterator<ServiceTemplateSetting> iterator = resource.getParameters().iterator();
                //Collect all values before we remove them, so we can check any dependent values we might be removing
                Map<String, String> values = new HashMap<>();
                for (ServiceTemplateSetting setting : resource.getParameters()) {
                    values.put(setting.getId(), setting.getValue());
                }
                while (iterator.hasNext()) {
                    ServiceTemplateSetting param = iterator.next();
                    switch (param.getId()) {
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID:
                        if (component.getAsmGUID().equals(param.getValue())) {
                            iterator.remove();
                        }
                        break;
                    // TODO: would be nice to have a data-driven way to mark fields to remove
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_TEMPLATE_ID:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_GENERATE_HOSTNAME_ID:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ID:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IP_SOURCE:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME_TEMPLATE_ID:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_GENERATE_NAME_ID:
                        iterator.remove();
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MIGRATE_ON_FAIL_ID:
                        String serverSource = values.get(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_POOL.equals(serverSource)) {
                            //If the server source is pool instead of manual, we set the dependency target to null, so migrate_on_failure won't be stripped out.
                            param.setDependencyTarget(null);
                        }
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID:
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NAME:
                        // we have to reset this otherwise removeUnrelatedValues will strip it
                        param.setDependencyTarget(null);
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_TIMEZONE_ID:
                        // asm_deployer needs string value
                        if (StringUtils.isNotEmpty(param.getValue())) {
                            TimeZoneDto applianceZone = TimeZoneConfigurationMgr.getInstance().findDeviceZoneByAsmId(Integer.parseInt(param.getValue()));
                            param.setValue(applianceZone.getDisplayString());
                        }
                        // reset options since we dropped the value anyway
                        param.setOptions(new ArrayList<ServiceTemplateOption>());
                        break;
                    case ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CUSTOM_SPEC:
                        if (StringUtils.isEmpty(param.getValue())) {
                            iterator.remove();
                        }
                    }
                    //Special case where id contains more than default settingId name
                    if (param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE) ||
                            param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_STATIC_IP_VALUE)) {
                        iterator.remove();
                    }
                }
            }
        }
        serviceTemplate.removeUnrelatedValues();
        serviceTemplate.removeSelectNewOptions();
        Map<String, String> storageMap = ServiceTemplateUtil.processStoragesForDeployment(serviceTemplate);
        ServiceTemplateUtil.processClustersForDeployment(serviceTemplate, storageMap);
    }

    /**
     * Returns true if component is a Server and related components have a VDS enabled Cluster.
     * @param template
     * @param component
     * @return
     */
    public static boolean isVDSService(ServiceTemplate template, ServiceTemplateComponent component) {
        if (component.getType() != ServiceTemplateComponent.ServiceTemplateComponentType.SERVER)
            return false;

        Set<String> related = component.getAssociatedComponents().keySet();
        if (related.size() > 0) {
            if (CollectionUtils.isNotEmpty(related)) {
                for (ServiceTemplateComponent c : template.getComponents()) {
                    if (c.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER && related.contains(c.getId())) {
                        ServiceTemplateSetting vdsSet = c.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID);
                        if (vdsSet != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID.equals(vdsSet.getValue()))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * For given deployment component, get desired parameter values
     * @param template ServiceTemplate object
     * @param componentGuid reference Id for component whose parameter values are needed
     * @param resourceId resourceId string, as found in constants definitions ending with _RESOURCE in ServiceTemplateSettingsId
     * @param parameterIds set of parameterId(s). Possible value of parameterId can be found in constant definitions ending with _ID in ServiceTemplateSettingsId
     * @return key-value pair, where key is parameterId as passed in parameterIds, and value is the parameter value corresponding to that parameterId.
     */
    public static Map<String, String> getDeployedTemplateParameterValues(ServiceTemplate template,
                                                                         String componentGuid,
                                                                         String resourceId,
                                                                         Set<String> parameterIds ) {
        Map<String,String> values = new HashMap<String, String>();
        int valuesLeft = parameterIds.size();
        if (template != null) {
            ServiceTemplateComponent component = template.findComponentByGUID(componentGuid);
            if (component != null) {
                if(parameterIds.contains(ServiceTemplateSettingIDs.HYPERVISOR_IP_ADDRESS)) {
                    List<Network> networks = ServiceTemplateClientUtil.findStaticManagementNetworks(component);
                    if (networks != null && networks.size() > 0 && networks.get(0).getStaticNetworkConfiguration() != null) {
                        values.put(ServiceTemplateSettingIDs.HYPERVISOR_IP_ADDRESS,
                                   networks.get(0).getStaticNetworkConfiguration().getIpAddress());
                        valuesLeft--;
                        if (valuesLeft == 0) {
                            return values;
                        }
                    }
                }
                resourcesloop:
                for(ServiceTemplateCategory resource : component.getResources()) {
                    if (resource.getId() != null && resource.getId().equals(resourceId)) {
                        for(ServiceTemplateSetting parameter : resource.getParameters()) {
                            String key = parameter.getId();
                            if(parameterIds.contains(key) && !values.containsKey(key)) {
                                values.put(key, parameter.getValue());
                                valuesLeft--;
                                if (valuesLeft == 0) {
                                    // we are done, break
                                    break resourcesloop;
                                }
                            }
                        }
                    }
                }
            }
        }
        return values;
    }
    
    /**
     * Returns a list of servers that have the 'Ready' status.
     * 
     * @param deployment the deployment whose components will be searched for Servers with the status of 'Ready'.
     * @return a list of servers that have the 'Ready' status.
     */
    public List<ServiceTemplateComponent> getScaledUpServerComponents(Deployment deployment) {
        List<ServiceTemplateComponent> readyServerComponents = new ArrayList<ServiceTemplateComponent>();
        
        for (ServiceTemplateComponent component : deployment.getServiceTemplate().getComponents()) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                DeviceInventoryEntity server = deviceInventoryDAO.getDeviceInventory(component.getAsmGUID());
                // Means it has not been deployed and is not in error state
                if (DeviceState.READY.equals(server.getState()) || 
                        DeviceState.PENDING.equals(server.getState()) || // Appears to be 'Pending' before software update
                        DeviceState.DEPLOYING.equals(server.getState())) { // Appears to be 'Deploying' before firmware update 
                    readyServerComponents.add(component);
                }
            }
        }
        
        return readyServerComponents;
    }

    
    /**
     * Updates the compliance of a deployment.  This method assumes the devices compliances have been updated properly 
     * in the device_inventory table.  It will look up shared devices as necessary from the compliance_map table.
     * 
     * @param deployment the deployment whose compliance status will be updated. 
     * @throws AsmManagerCheckedException
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     */
    public final void updateDeploymentCompliance(final DeploymentEntity deployment) 
                    throws AsmManagerCheckedException, InvocationTargetException, IllegalAccessException {
        if (deployment != null) {
            if (deployment.isManageFirmware()) {
                // Compliant until shown otherwise
                deployment.setCompliant(true);

                // Check devices for non-compliance
                for (DeviceInventoryEntity tempDevice : deployment.getDeployedDevices()) {
                    DeviceInventoryEntity device = deviceInventoryDAO.getDeviceInventory(tempDevice.getRefId());
                    CompliantState compliantState = CompliantState.COMPLIANT;
                    if (device.getDeviceType().isFirmwareComplianceManaged()) {
                        if (device.getDeviceType().isSharedDevice()) {
                             DeviceInventoryComplianceEntity deviceCompliance =
                                    deviceInventoryComplianceDAO.get(device, deployment.getFirmwareRepositoryEntity());
                             compliantState = deviceCompliance.getCompliance();
                        }
                        else { // non-shared devices should already be updated.
                            compliantState = CompliantState.fromValue(device.getCompliant());
                        }
                        
                        if (CompliantState.NONCOMPLIANT.equals(compliantState) ||
                                CompliantState.UPDATEREQUIRED.equals(compliantState)) {
                            deployment.setCompliant(false);
                            break; // Once found non-compliant we do not need to continue checking
                        }
                    }
                }
            }
            // if the deployment is in updating firmware and is not manage firmware then deployment status
            // should be complete and compliant
            // Or if not manage firmware, just set the compliance to true.
            else if (!deployment.isManageFirmware()
                    && DeploymentStatusType.FIRMWARE_UPDATING.equals(deployment.getStatus())) {
                deployment.setStatus(DeploymentStatusType.COMPLETE);
                deployment.setCompliant(true);
            }
            
            // Save the new state
            deploymentDAO.updateDeployment(deployment);
        }
    }    
    
    /**
     * Checks to see if a Deployment exists for the DeviceInventoryEntity and update's it's compliance if necessary.
     * 
     * @param devInv the device whose deployment's compliance will be updated accordingly.
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     * @throws AsmManagerCheckedException 
     */
    public void updateDevicesDeploymentsCompliance(DeviceInventoryEntity devInv) 
            throws AsmManagerCheckedException, 
                   InvocationTargetException, 
                   IllegalAccessException {
        if (devInv.getDeploymentCount() > 0) {
            if (devInv.getDeviceType().isFirmwareComplianceManaged()) {
                final DeploymentEntity deployment = deploymentDAO.getDeployment(devInv.getDeployments().get(0).getId(),
                        DeploymentDAO.FIRMWARE_REPOSITORY_ENTITY | DeploymentDAO.DEVICE_INVENTORY_ENTITIES);
                if (deployment.isManageFirmware() && deployment.getFirmwareRepositoryEntity() != null) {
                    this.runServiceComplianceCheck(deployment, true);
                }
            }
        }
    }

    /**
     * When the deployment catalog changes we need to reset the software inventory and then recalculate the compliance.
     * 
     * @param deploymentEntity the deployment whose catalog has changed.
     * @throws AsmManagerCheckedException 
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     */
    public void updateDeploymentComplianceAfterCatalogChange(DeploymentEntity deploymentEntity) 
            throws AsmManagerCheckedException, 
                   InvocationTargetException, 
                   IllegalAccessException {
        this.firmwareUtil.updateCatalogBasedSoftwareDeviceInventory(deploymentEntity);
        this.firmwareUtil.updateFirmwareComplianceForDevices(new ArrayList<DeviceInventoryEntity>(deploymentEntity.getDeployedDevices()), 
                    deploymentEntity.getFirmwareRepositoryEntity());
        this.updateDeploymentCompliance(deploymentEntity);
    }

    /**
     * Loads all of the deploymetns with a default catalog, and then load the current default catalog, and sets it on
     * all of the deployments.
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     * @throws AsmManagerCheckedException 
     */
    public void updateDeploymentComplianceThatAreUsingDefaultCatalog() 
            throws AsmManagerCheckedException, 
                   InvocationTargetException, 
                   IllegalAccessException {
        // get all deployments which are using the default catalog
        List<DeploymentEntity> deploymentEntities = deploymentDAO.getAllDefaultCatalogDeployments();
        if (deploymentEntities != null && deploymentEntities.size() > 0) {
            // get the default repo
            FirmwareRepositoryEntity defaultFirmwareRepo = firmwareUtil.getDefaultRepo();
            for (DeploymentEntity deployment : deploymentEntities) {
                // add new default firmware repository
                deployment.setFirmwareRepositoryEntity(defaultFirmwareRepo);
                deploymentDAO.updateDeployment(deployment);
                
                // Must update the compliance now that the catalog has been changed
                this.updateDeploymentComplianceAfterCatalogChange(deployment);
            }
        }
    }
    
}
