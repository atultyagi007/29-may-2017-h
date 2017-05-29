package com.dell.asm.asmcore.asmmanager.db.entity;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;

/**
 * Created with IntelliJ IDEA.
 * User: J_Fowler
 * Date: 1/7/14
 * Time: 9:06 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "deployment", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = { "id" }))
public class DeploymentEntity {
    @Id
    @GeneratedValue(generator = "pg-uuid")
    @GenericGenerator(name = "pg-uuid", strategy = "uuid")    
    @Column(name = "id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String id;

    @Column(name="name")
    private String name;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private DeploymentStatusType status = DeploymentStatusType.PENDING;

    @Column(name="deployment_desc")
    private String deploymentDesc;
    
    @Column(name="created_date")
    private GregorianCalendar createdDate;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="updated_date")
    private GregorianCalendar updatedDate;

    @Column(name="updated_by")
    private String updatedBy;

    @Column(name="deployment_started_date")
    private GregorianCalendar deploymentStartedDate;

    @Column(name="deployment_finished_date")
    private GregorianCalendar deploymentFinishedDate;

    @Column(name="expiration_date")
    private GregorianCalendar expirationDate;

    @Column(name="all_users")
    private boolean allUsersAllowed;

    @OneToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="deployment_to_device_map",
            joinColumns = @JoinColumn( name="deployment_id"),
            inverseJoinColumns = @JoinColumn( name="device_id")
    )
    private Set<DeviceInventoryEntity> deployedDevices = new HashSet<DeviceInventoryEntity>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name="deployment_id")
    private Set<VMRefEntity> vmList = new HashSet<VMRefEntity>();
  
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name="deployment_id")
    private Set<DeploymentUserRefEntity> assignedUserList = new HashSet<>();

    // default to true since the assumption is that deployment checks have already been made on the template
    @Column(name="template_valid")
    private boolean templateValid = Boolean.TRUE;
    
    @Column(name="marshalledTemplateData")
    private String marshalledTemplateData;

    @Column(name="job_id")
    private String jobId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "firmware_repository", referencedColumnName = "id")
    @ForeignKey(name = "deployment_firmware_repository_fk")
    private FirmwareRepositoryEntity firmwareRepositoryEntity;
	
    @Column(name="manage_firmware")
    private boolean manageFirmware;

    @Column(name="use_default_catalog")
    private boolean useDefaultCatalog;
    
    @Column(name="compliant")
    private boolean compliant;
    
    @Column(name="brownfield")
    private boolean isBrownfield;

    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "deployment_add_on_module", joinColumns = { @JoinColumn(name = "deployment_id") }, inverseJoinColumns = { @JoinColumn(name = "add_on_module_id") })
    private Set<AddOnModuleEntity> addOnModules = new HashSet<>();

    @Version
    @Column(name="optlock")
    private Integer version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name="deployment_id")
    private Set<DeploymentNamesRefEntity> namesRefs = new HashSet<>();

    /**
     * Utility method to get a client object.  Currently super simple due to the fact we only need it in an extremely limited fashion.
     * @return
     */
    public Deployment getDeployment()
    {
    	Deployment deployment = new Deployment();
    	deployment.setId(this.id);
    	deployment.setDeploymentName(this.name);

    	return deployment;
    }    

    public Set<DeploymentUserRefEntity> getAssignedUserList() {
        return assignedUserList;
    }

    public void setAssignedUserList(Set<DeploymentUserRefEntity> assignedUserList) {
        this.assignedUserList = assignedUserList;
    }

    public boolean isAllUsersAllowed() {
        return allUsersAllowed;
    }

    public void setAllUsersAllowed(boolean allUsersAllowed) {
        this.allUsersAllowed = allUsersAllowed;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeploymentStatusType getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatusType status) {
        this.status = status;
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

    public GregorianCalendar getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(GregorianCalendar expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Set<DeviceInventoryEntity> getDeployedDevices() {
        return deployedDevices;
    }

    public void setDeployedDevices(Set<DeviceInventoryEntity> deployedDevices) {
        this.deployedDevices = deployedDevices;
    }

    public Set<VMRefEntity> getVmList() {
        return vmList;
    }

    public void setVmList(Set<VMRefEntity> vmList) {
        this.vmList = vmList;
    }

    public boolean isTemplateValid() {
        return templateValid;
    }

    public void setTemplateValid(boolean templateValid) {
        this.templateValid = templateValid;
    }

    public String getMarshalledTemplateData() {
        return marshalledTemplateData;
    }

    public void setMarshalledTemplateData(String marshalledTemplateData) {
        this.marshalledTemplateData = marshalledTemplateData;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getDeploymentDesc() {
        return deploymentDesc;
    }

    public void setDeploymentDesc(String deploymentDesc) {
        this.deploymentDesc = deploymentDesc;
    }

    public FirmwareRepositoryEntity getFirmwareRepositoryEntity() {
        return firmwareRepositoryEntity;
    }

    public void setFirmwareRepositoryEntity(FirmwareRepositoryEntity firmwareRepositoryEntity) {
        this.firmwareRepositoryEntity = firmwareRepositoryEntity;
    }

    public boolean isManageFirmware() {
        return manageFirmware;
    }

    public void setManageFirmware(boolean manageFirmware) {
        this.manageFirmware = manageFirmware;
    }

    public boolean isUseDefaultCatalog() {
        return useDefaultCatalog;
    }

    public void setUseDefaultCatalog(boolean useDefaultCatalog) {
        this.useDefaultCatalog = useDefaultCatalog;
    }

    public boolean isCompliant() {
        return compliant;
    }
    
    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }
    
    public boolean isBrownfield(){
        return this.isBrownfield;
    }
    
    public void setBrownfield(boolean brownfield){
        this.isBrownfield = brownfield;
    }

    public Set<AddOnModuleEntity> getAddOnModules() {
        return addOnModules;
    }

    public void setAddOnModules(Set<AddOnModuleEntity> addOnModules) {
        this.addOnModules = addOnModules;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Set<DeploymentNamesRefEntity> getNamesRefs() {
        return namesRefs;
    }

    public void setNamesRefs(Set<DeploymentNamesRefEntity> namesRefs) {
        this.namesRefs = namesRefs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentEntity)) return false;

        DeploymentEntity that = (DeploymentEntity) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DeploymentEntity [id=" + id + ", name=" + name + ", status=" + status + ", deploymentDesc="
                + deploymentDesc + ", createdDate=" + createdDate + ", createdBy=" + createdBy + ", updatedDate="
                + updatedDate + ", updatedBy=" + updatedBy + ", expirationDate=" + expirationDate
                + ", allUsersAllowed=" + allUsersAllowed + ", deployedDevices=" + deployedDevices + ", vmList="
                + vmList + ", assignedUserList=" + assignedUserList + ", templateValid=" + templateValid
                + ", marshalledTemplateData=" + marshalledTemplateData + ", jobId=" + jobId
                + ", firmwareRepositoryEntity=" + firmwareRepositoryEntity + ", manageFirmware=" + manageFirmware 
                + ", compliant=" + compliant + "]";
    }
    
    
    /**
     * Returns true if a DeviceInventoryEntity with the given refId exists or false if a match cannout be found.
     * 
     * @param refId the matching DeviceInventoryEntity must contain.
     * @return true if a DeviceInventoryEntity with the given refId exists or false if a match cannout be found.
     */
    public boolean containsDeviceInventoryEntity(String refId) {
        boolean containsComponent = false;

        for(DeviceInventoryEntity deviceInventoryEntity : this.deployedDevices) {
            if(refId.equals(deviceInventoryEntity.getRefId())) {
                containsComponent = true;
                break;
            }
        }
        
        return containsComponent;
    }
    
}
