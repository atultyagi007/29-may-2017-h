package com.dell.asm.asmcore.asmmanager.db.entity;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.dell.pg.asm.identitypoolmgr.db.BaseEntity;

@Entity
@Table(name = "add_on_module", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = { "name",
        "version" }))
public class AddOnModuleEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "description")
    private String description;

    @Column(name = "module_path")
    private String modulePath;

    @Column(name = "version")
    private String version;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_date")
    private GregorianCalendar uploadedDate;
    
    @Column(name="marshalled_classes_data")
    private String marshalledClassesData;
    
    @Column(name="marshalled_types_data")
    private String marshalledTypesData;    

    @OneToMany(mappedBy="addOnModuleOperatingSystemVersionId.addOnModule", fetch = FetchType.EAGER)
    private Set<AddOnModuleOperatingSystemVersionEntity> addOnModuleOperatingSystemVersions;

    @Column(name="default_module")
    private Boolean defaultModule;

    @OneToMany(mappedBy = "addOnModuleEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AddOnModuleComponentEntity> addOnModuleComponents;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "addOnModules")
    private Set<ServiceTemplateEntity> serviceTemplateEntities = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "addOnModules")
    private Set<DeploymentEntity> deploymentEntities = new HashSet<>();

    @Column(name="asm_input_hash")
    private String asmInputHash;

    @Column(name="metadata_hash")
    private String metadataHash;
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public GregorianCalendar getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(GregorianCalendar uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
    
    public String getMarshalledClassesData() {
        return marshalledClassesData;
    }

    public void setMarshalledClassesData(String marshalledClassesData) {
        this.marshalledClassesData = marshalledClassesData;
    }

    public String getMarshalledTypesData() {
        return marshalledTypesData;
    }

    public void setMarshalledTypesData(String marshalledTypesData) {
        this.marshalledTypesData = marshalledTypesData;
    }

    public Set<AddOnModuleOperatingSystemVersionEntity> getAddOnModuleOperatingSystemVersions() {
        return addOnModuleOperatingSystemVersions;
    }

    public void setAddOnModuleOperatingSystemVersions(
            Set<AddOnModuleOperatingSystemVersionEntity> addOnModuleOperatingSystemVersions) {
        this.addOnModuleOperatingSystemVersions = addOnModuleOperatingSystemVersions;
    }

    public Boolean getDefaultModule() {
        return defaultModule;
    }

    public void setDefaultModule(Boolean defaultModule) {
        this.defaultModule = defaultModule;
    }

    public Set<AddOnModuleComponentEntity> getAddOnModuleComponents() {
        if (addOnModuleComponents == null) {
            addOnModuleComponents = new HashSet<>();
        }
        return addOnModuleComponents;
    }

    public void setAddOnModuleComponents(Set<AddOnModuleComponentEntity> addOnModuleComponents) {
        this.addOnModuleComponents = addOnModuleComponents;
    }

    public Set<ServiceTemplateEntity> getServiceTemplateEntities() {
        return serviceTemplateEntities;
    }

    public void setServiceTemplateEntities(Set<ServiceTemplateEntity> serviceTemplateEntities) {
        this.serviceTemplateEntities = serviceTemplateEntities;
    }

    public Set<DeploymentEntity> getDeploymentEntities() {
        return deploymentEntities;
    }

    public void setDeploymentEntities(Set<DeploymentEntity> deploymentEntities) {
        this.deploymentEntities = deploymentEntities;
    }

    public boolean isActive() {
        boolean result = false;
        if ((Hibernate.isInitialized(this.getDeploymentEntities()) && CollectionUtils.isNotEmpty(this.getDeploymentEntities())) ||
                (Hibernate.isInitialized(this.getServiceTemplateEntities()) && CollectionUtils.isNotEmpty(this.getServiceTemplateEntities()))) {
            result = true;
        }
        return result;
    }

    public String getAsmInputHash() {
        return asmInputHash;
    }

    public void setAsmInputHash(String asmInputHash) {
        this.asmInputHash = asmInputHash;
    }

    public String getMetadataHash() {
        return metadataHash;
    }

    public void setMetadataHash(String metadataHash) {
        this.metadataHash = metadataHash;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        if (!(obj instanceof AddOnModuleEntity)) {
            return false;
        }
        AddOnModuleEntity other = (AddOnModuleEntity) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AddOnModuleEntity [id=" + getId() + ", name=" + getName() + ", description=" + description 
                + ", modulePath=" + modulePath + ", version=" + version + ", defaultModule=" + defaultModule + ", uploadedBy="
                + uploadedBy + ", uploadedDate=" + uploadedDate + "]";
    }

}
