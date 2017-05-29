/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Deployment is represented as a Service in the UI, and you may hear Service/Deployment used interchangeably.   A
 * Deployment is primarly represented by a ServiceTemplate that represents component settings and how components relate 
 * to one another.  A component will represent an individual device, as in the case of a Server, or possibly a shared 
 * component as in the case of a Storage such as Equallogic, Compellent, or Netapp. <br>
 * <br>
 * A Deployment's overall status is represented via the getDeploymentHealthStatusType() method, and indicates whether 
 * the Deployment was successful, if there was an issue during deployment, or if there is another health or firmware 
 * related issue. Further analysis on the state of the Service can be determined by looking at the Deployment's 
 * getStatus() method which indicates the success/failure of deploying the Service, by looking at the 
 * getOverallDeivceHealth() which represents a rolled up status of the health of all Devices utilized in the Deployment, 
 * and the isCompliant() method which indicates whether the Firmware is compliant for the Deployment. <br>
 * <br>
 * If not specific catalog is set for the Service/Deployment, then Firmware compliance is determined by the either the 
 * embedded or the default catalog.  If not default Firmware Catalog has been setup, then the Firmware compliance will
 * be determined by the embedded catalog that ships with ASM.  If a default Firmware Catalog has been setup, then 
 * Firmware compliance will be determined by the default Firmware repository.  If a specific Firmware Repository has 
 * been configured for this Deployment via the setFirmwareRepository(FirmwareRepository) method, then the Firmware
 * compliance will be calculated based on that Firmware repository. <br>
 * <br>
 * A Deployment may be accessed by the person who creates the Deployment and by -any- Admin user.  In addition, if the 
 * setAllUsersAllowed(boolean) is called with a 'true' value, then all users with the Role of 'Standard' will be able
 * to access and manipulate the Deployment.  If specific Users are selected, via the {@link #setAssignedUsers(Set)} method
 * then those users will be able to manipulate the Service/Deployment as well. <br>
 * <br>
 * A Deployment is initially setup on the Java side, stored in the database, and then sent to the Ruby side for the
 * actual deployment of the devices.  Once the deployment has succeeded, or failed, as the case may be, any deployed
 * devices will be represented via the getDeploymentDevice() method which returns a Set&lt;DeploymentDevice&gt;.  These
 * DeploymentDevice classes will indicate whether each Device utilized in the Deployment was successful or failed.
 */
@XmlRootElement(name = "Deployment")
public class Deployment
{

    private String id;
    private String deploymentName;
    private String deploymentDescription;
    private boolean retry;
    private boolean teardown;
    private GregorianCalendar createdDate;
    private String createdBy;
    private GregorianCalendar updatedDate;
    private String updatedBy;
    private GregorianCalendar deploymentStartedDate;
    private GregorianCalendar deploymentFinishedDate;
    private boolean isTemplateValid = Boolean.TRUE; // default value
    private ServiceTemplate serviceTemplate;
    private Date scheduleDate;
    private DeploymentStatusType status = DeploymentStatusType.PENDING; // default value
    private boolean compliant = Boolean.TRUE;
    private Set<DeploymentDevice> deploymentDevice = new HashSet<>();
    private Set<VM> vms;
    private boolean updateServerFirmware;
    private boolean useDefaultCatalog;
    private FirmwareRepository firmwareRepository;
    private String firmwareRepositoryId;
    private boolean individualTeardown;
    private DeploymentHealthStatusType deploymentHealthStatusType;
    private boolean isBrownfield = false;
    private Set<User> assignedUsers;
    private boolean allUsersAllowed;
    private boolean canScaleupStorage;
    private boolean canScaleupServer;
    private boolean canScaleupVM;
    private boolean canScaleupCluster;
    private boolean canScaleupNetwork;
    private String owner;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canCancel;
    private boolean canDeleteResources;
    private boolean canRetry;
    private boolean canScaleupApplication;
    private boolean noOp;
    private boolean firmwareInit;
    private boolean isVDS = false;
    private boolean isScaleUp = false;

    /**
     * Indicates if deployment was updated for configuration settings only: name, description, users, firmware management etc.
     * No components were added / removed.
     * @return
     */
    public boolean isConfigurationChange() {
        return isConfigurationChange;
    }

    public void setConfigurationChange(boolean configurationChange) {
        isConfigurationChange = configurationChange;
    }

    private boolean isConfigurationChange = false;

    // read only as far as rest is concerned.
    private List<AsmDeployerLogEntry> jobDetails;
    private int numberOfDeployments;
    private boolean canMigrate;

    public boolean isVDS() {
        return isVDS;
    }

    public void setVDS(boolean isVDS) {
        this.isVDS = isVDS;
    }

    public DeploymentHealthStatusType getDeploymentHealthStatusType() {
        return deploymentHealthStatusType;
    }

    public void setDeploymentHealthStatusType(DeploymentHealthStatusType deploymentHealthStatusType) {
        this.deploymentHealthStatusType = deploymentHealthStatusType;
    }

    public int getNumberOfDeployments() {
        return numberOfDeployments;
    }

    public void setNumberOfDeployments(int numberOfDeployments) {
        this.numberOfDeployments = numberOfDeployments;
    }

    public String getFirmwareRepositoryId() {
        return firmwareRepositoryId;
    }

    public void setFirmwareRepositoryId(String firmwareRepositoryId) {
        this.firmwareRepositoryId = firmwareRepositoryId;
    }

    public FirmwareRepository getFirmwareRepository() {
        return firmwareRepository;
    }

    public void setFirmwareRepository(FirmwareRepository firmwareRepository) {
        this.firmwareRepository = firmwareRepository;
    }

    public Set<User> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(Set<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public boolean isAllUsersAllowed() {
        return allUsersAllowed;
    }

    public void setAllUsersAllowed(boolean allUsersAllowed) {
        this.allUsersAllowed = allUsersAllowed;
    }

    public boolean isCanMigrate() {
        return canMigrate;
    }

    public void setCanMigrate(boolean canMigrate) {
        this.canMigrate = canMigrate;
    }


    public boolean isCanScaleupStorage() {
        return canScaleupStorage;
    }

    public void setCanScaleupStorage(boolean canScaleupStorage) {
        this.canScaleupStorage = canScaleupStorage;
    }

    public boolean isCanScaleupNetwork() {
        return canScaleupNetwork;
    }

    public void setCanScaleupNetwork(boolean canScaleupNetwork) {
        this.canScaleupNetwork = canScaleupNetwork;
    }

    public boolean isCanScaleupServer() {
        return canScaleupServer;
    }

    public void setCanScaleupServer(boolean canScaleupServer) {
        this.canScaleupServer = canScaleupServer;
    }

    public boolean isCanScaleupVM() {
        return canScaleupVM;
    }

    public void setCanScaleupVM(boolean canScaleupVM) {
        this.canScaleupVM = canScaleupVM;
    }

    public boolean isCanDeleteResources() {
        return canDeleteResources;
    }

    public void setCanDeleteResources(boolean canDeleteResources) {
        this.canDeleteResources = canDeleteResources;
    }

    public boolean isCanRetry() {
        return canRetry;
    }

    public void setCanRetry(boolean canRetry) {
        this.canRetry = canRetry;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public boolean isCanScaleupApplication() {
        return canScaleupApplication;
    }

    public void setCanScaleupApplication(boolean canScaleupApplication) {
        this.canScaleupApplication = canScaleupApplication;
    }

    public boolean isCanScaleupCluster() {
        return canScaleupCluster;
    }

    public void setCanScaleupCluster(boolean canScaleupCluster) {
        this.canScaleupCluster = canScaleupCluster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getDeploymentDescription() {
        return deploymentDescription;
    }

    public void setDeploymentDescription(String deploymentDescription) {
        this.deploymentDescription = deploymentDescription;
    }

    public GregorianCalendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GregorianCalendar getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public GregorianCalendar getDeploymentStartedDate() {
        return deploymentStartedDate;
    }

    public void setDeploymentStartedDate(GregorianCalendar startDate) {
        this.deploymentStartedDate = startDate;
    }

    public GregorianCalendar getDeploymentFinishedDate() {
        return deploymentFinishedDate;
    }

    public void setDeploymentFinishedDate(GregorianCalendar endDate) {
        this.deploymentFinishedDate = endDate;
    }

    public boolean isTemplateValid() {
        return isTemplateValid;
    }

    public void setTemplateValid(boolean isTemplateValid) {
        this.isTemplateValid = isTemplateValid;
    }

    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    public DeploymentStatusType getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatusType status) {
        this.status = status;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public Set<DeploymentDevice> getDeploymentDevice() {
        return deploymentDevice;
    }

    public void setDeploymentDevice(Set<DeploymentDevice> deploymentDevice) {
        this.deploymentDevice = deploymentDevice;
    }

    public Set<VM> getVms() {
        return vms;
    }

    public void setVms(Set<VM> vms) {
        this.vms = vms;
    }

    public List<AsmDeployerLogEntry> getJobDetails() {
        return jobDetails;
    }

    public void setJobDetails(List<AsmDeployerLogEntry> jobDetails) {
        this.jobDetails = jobDetails;
    }

    public boolean isTeardown() {
        return teardown;
    }

    public void setTeardown(boolean teardown) {
        this.teardown = teardown;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public boolean isUpdateServerFirmware() {
        return updateServerFirmware;
    }

    public void setUpdateServerFirmware(boolean updateServerFirmware) {
        this.updateServerFirmware = updateServerFirmware;
    }

    public boolean isUseDefaultCatalog() {
        return useDefaultCatalog;
    }

    public void setUseDefaultCatalog(boolean useDefaultCatalog) {
        this.useDefaultCatalog = useDefaultCatalog;
    }

    public boolean isIndividualTeardown() {
        return individualTeardown;
    }

    public void setIndividualTeardown(boolean individualTeardown) {
        this.individualTeardown = individualTeardown;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }

    /**
     * Sets whether the deployment is brownfield.  All deployments are greenfield by default, thus this value will be
     * false unless set otherwise.
     * 
     * @param brownfield a boolean indicating if the deployment is brownfield.
     */
    public void setBrownfield(boolean brownfield){
        this.isBrownfield = brownfield;
    }
    
    /**
     * Indicates if the deployment is a brownfield deployment versus a normal / greenfield deployment.
     * @return a boolean indicating if the deployment is a brownfield deployment versus a normal / greenfield deployment.
     */
    public boolean isBrownfield(){
        return this.isBrownfield;
    }

    public void setNoOp(boolean noOp) {this.noOp = noOp; }

    public boolean isNoOp() { return this.noOp; }

    public void setFirmwareInit(boolean firmwareInit) { this.firmwareInit = firmwareInit; }

    public boolean isFirmwareInit() { return this.firmwareInit; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deploymentName == null) ? 0 : deploymentName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Deployment)) {
            return false;
        }
        Deployment other = (Deployment) obj;
        if (deploymentName == null) {
            if (other.deploymentName != null) {
                return false;
            }
        } else if (!deploymentName.equals(other.deploymentName)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
    
    /**
     * Sorts the components in the ServiceTemplate based on their DeploymentStatus and their Component Name and 
     * returns a list of DeploymentDevices that are sorted based on the BrownfieldStatus with the newly available
     * components at the top, then the unavailable components, then the components that are currently in a service, and
     * then by the component name. If BrownfieldStatus is not set, then it will simply sort based on the name of the 
     * component.  <br>
     * <br>  
     * WARNING:  Should ONLY be called if the serviceTemplate has been set, otherwise will result in an error!<br>
     * <br>
     * @see com.dell.asm.asmcore.asmmanager.client.deployment.BrownfieldStatus
     * @return a list of DeploymentDevices that are sorted based on the name of the components they represent.
     */
    @JsonIgnore
    public List<DeploymentDevice> sortDeployedDevicesByComponentName(){
        ArrayList<DeploymentDevice> deploymentDeviceList = new ArrayList<DeploymentDevice>();    
        deploymentDeviceList.addAll(this.getDeploymentDevice());
        
        Collections.sort(deploymentDeviceList, new Comparator<DeploymentDevice>(){
            
            public int compare(DeploymentDevice dd1, DeploymentDevice dd2){
                String n1 = getComponentNameForDeployment(dd1);
                String n2 = getComponentNameForDeployment(dd2);
                
                int brownfieldStatus = compareBrownfieldStatus(dd1.getBrownfieldStatus(), dd2.getBrownfieldStatus());
                if(brownfieldStatus == 0) {
                    return n1.compareTo(n2);
                }
                else {
                    return brownfieldStatus;
                }
            }
        });
        
      Collections.sort(this.getServiceTemplate().getComponents(), new Comparator<ServiceTemplateComponent>(){
      
          public int compare(ServiceTemplateComponent stc1, ServiceTemplateComponent stc2){
              String n1 = getSafeComponentName(stc1);
              String n2 = getSafeComponentName(stc2);
              
              DeploymentDevice dd1 = getDeploymentDevice(stc1.getId());
              DeploymentDevice dd2 = getDeploymentDevice(stc2.getId());
              BrownfieldStatus bfs1;
              BrownfieldStatus bfs2;
              if (dd1 != null)
                  bfs1 = dd1.getBrownfieldStatus();
              else
                  bfs1 = BrownfieldStatus.NOT_APPLICABLE;

              if (dd2 != null)
                  bfs2 = dd2.getBrownfieldStatus();
              else
                  bfs2 = BrownfieldStatus.NOT_APPLICABLE;


              int brownfieldStatus = compareBrownfieldStatus(bfs1, bfs2);
              if(brownfieldStatus == 0) {
                  return n1.compareTo(n2);
              }
              else {
                  return brownfieldStatus;
              }
          }
      });
        
        return deploymentDeviceList;
    }
    
    // Returns an empty String if the ServiceTemplateComponent is null or it's name is null 
    private String getSafeComponentName(ServiceTemplateComponent serviceTemplateComponent) {
        String componentName = "";
        if (serviceTemplateComponent != null && serviceTemplateComponent.getName() != null) {
            componentName = serviceTemplateComponent.getName();
        }
        return componentName;
    }

    // Specifically compares the Brownfield Statuses 
    private int compareBrownfieldStatus(BrownfieldStatus bs1, BrownfieldStatus bs2) {
        if (bs1 != null && bs2 != null) {
            return bs1.compareTo(bs2);
        }
        else if(bs1 != null && bs2 == null) {
            return 1;
        }
        else if(bs1 == null && bs2 != null) {
            return -1;
        }

        return 0;
    }
    
    // Returns the Component Name for the DeployedDevice or an empty String if no Component can be found
    private String getComponentNameForDeployment(DeploymentDevice deploymentDevice) {
        String compId = deploymentDevice.getComponentId();
        if (this.getServiceTemplate() == null || compId== null) { 
            return "";
        }
        String componentName = this.getServiceTemplate().findComponentById(compId).getName();
        if(componentName == null){
            componentName = "";
        }
        return componentName;
    }
    
    /**
     * Returns the DeploymentDevice with the matching componentId.  Null will be returned if no match is found or if
     * componentId itself is null.
     * 
     * @param componentId the returned DeploymentDevice will contain.
     * @return the DeploymentDevice with the matching componentId.  Null will be returned if no match is found or if
     *      componentId itself is null.
     */
    public DeploymentDevice getDeploymentDevice(String componentId) {
        DeploymentDevice deploymentDeviceReturn = null;
        
        for(DeploymentDevice deploymentDevice : this.getDeploymentDevice()) {
            if(componentId != null && componentId.equals(deploymentDevice.getComponentId())) {
                deploymentDeviceReturn = deploymentDevice;
                break;
            }
        }
        return deploymentDeviceReturn;
    }
    
    /**
     * Returns the DeploymentDevice with the matching refId.  Null will be returned if no match is found or if
     * refId itself is null.
     * 
     * @param refId the returned DeploymentDevice will contain.
     * @return the DeploymentDevice with the matching asmGuid.  Null will be returned if no match is found or if
     *      asmGuid itself is null.
     */
    public DeploymentDevice getDeploymentDeviceByRefId(String refId) {
        DeploymentDevice deploymentDeviceReturn = null;
        
        for(DeploymentDevice deploymentDevice : this.getDeploymentDevice()) {
            if(refId != null && refId.equals(deploymentDevice.getRefId())) {
                deploymentDeviceReturn = deploymentDevice;
                break;
            }
        }
        return deploymentDeviceReturn;
    }
    
    /**
     * Sets all of the DeploymentDevices availableForBrownfield setting to the given boolean.
     * 
     * @param brownfieldStatus the status that all DeploymentDevices in this Deployment will be set.
     */
    public void setAllDeploymentDevicesBrownfieldStatus(BrownfieldStatus brownfieldStatus) {
        for(DeploymentDevice deploymentDevice : this.getDeploymentDevice()) {
            deploymentDevice.setBrownfieldStatus(brownfieldStatus);
        }
    }
    
    /**
     * Due to movement of available / unavailable components when processing Brownfield scenarios, it's necessary to 
     * renumber ServiceTemplateComponents so their numbers are contiguous.  This method renumbers both Available and 
     * Unavailable Servers and Storages based on their BrownfieldStatus.
     */
    public void renumberAndRenameServiceTemplateComponents(boolean renumberAvailable) {

        List<DeploymentDevice> deployedDevices = this.sortDeployedDevicesByComponentName();

        int availableServers = 1;
        int unavailableServers = 1;
        int availableStorages = 1;
        int unavailableStorages = 1;

        // NOTE: Per discussion with Gavin S. relationship names do not need to be updated as long as Ids are correct
        for (DeploymentDevice deploymentDevice : deployedDevices) {
            ServiceTemplateComponent serviceTemplateComponent = 
                    this.getServiceTemplate().findComponentById(deploymentDevice.getComponentId());
            if(serviceTemplateComponent != null) {
                if(DeviceType.isServer(deploymentDevice.getDeviceType())) {
                    if(BrownfieldStatus.AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) ||
                            BrownfieldStatus.NEWLY_AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) ||
                            BrownfieldStatus.CURRENTLY_DEPLOYED_IN_BROWNFIELD.equals(deploymentDevice.getBrownfieldStatus())) {
                        if(renumberAvailable) {
                            serviceTemplateComponent.setName("Server " + availableServers++);
                        }
                    }
                    else {
                        serviceTemplateComponent.setName("Unavailable Server " + unavailableServers++);
                    }
                }
                else if(DeviceType.isStorage(deploymentDevice.getDeviceType())) {
                    if(BrownfieldStatus.AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) ||
                            BrownfieldStatus.NEWLY_AVAILABLE.equals(deploymentDevice.getBrownfieldStatus()) ||
                            BrownfieldStatus.CURRENTLY_DEPLOYED_IN_BROWNFIELD.equals(deploymentDevice.getBrownfieldStatus())) {
                        if(renumberAvailable) {
                            serviceTemplateComponent.setName("Storage " + availableStorages++);
                        }
                    }
                    else {
                        serviceTemplateComponent.setName("Unavailable Storage " + unavailableStorages++);
                    }
                }
                
            }
        }
    }
    
    /**
     * Returns true if any DeploymentDevice contains the brownfieldStatus passed in or returns false if none contain
     * it.
     * 
     * @param brownfieldStatus that the DeploymentDevices will be checked to contain.
     * @return true if any DeploymentDevice contains the brownfieldStatus passed in or returns false if none contain
     *      it.
     */
    public boolean containsDeployedDeviceWithBrownfieldStatus(BrownfieldStatus brownfieldStatus) {
        boolean containsStatus = false;
        
        for(DeploymentDevice deploymentDevice : this.getDeploymentDevice()) {
            if(brownfieldStatus.equals(deploymentDevice.getBrownfieldStatus())) {
                containsStatus = true;
                break;
            }
        }
        
        return containsStatus;
    }
    
    /**
     * Calculates and returns the overall health of the deployed devices that are part of the deployment.
     * 
     * @return the overall health of the deployed devices that are part of the deployment.
     */
    @XmlTransient    
    public DeviceHealth getOverallDeviceHealth() {
        DeviceHealth deviceHealth = DeviceHealth.GREEN;
        
        for (DeploymentDevice deploymentDevice : this.getDeploymentDevice()) {
            if(deploymentDevice.getDeviceType() != null && 
               deploymentDevice.getDeviceType().isCalculatedForResourceHealth() && 
               deploymentDevice.getDeviceHealth() != null) {
                if(DeviceHealth.YELLOW.equals(deploymentDevice.getDeviceHealth()) || 
                        DeviceHealth.UNKNOWN.equals(deploymentDevice.getDeviceHealth())) {
                    deviceHealth = DeviceHealth.YELLOW;
                }
                else if(DeviceHealth.RED.equals(deploymentDevice.getDeviceHealth())) {
                    deviceHealth = DeviceHealth.RED;
                    break; // No need to keep evaluating, Red is as high as it goes!
                }
            }
        }
        
        return deviceHealth;
    }

    /**
     * Return a boolean indicatinng if the Deployment is scaling up. 
     */
    public boolean isScaleUp() {
        return isScaleUp;
    }

    /**
     * Indicates whether the Deployment is scaling up.  False by default.
     */
    public void setScaleUp(boolean isScaleUp) {
        this.isScaleUp = isScaleUp;
    }
    
    
   
}
