package com.dell.asm.asmcore.asmmanager.db.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;

/**
 * Table to represent a firmware catalog uploaded into ASM system
 */
@Entity
@Table(name = "firmware_repository", schema = "public")
@DynamicUpdate
public class FirmwareRepositoryEntity extends BaseEntityAudit {
    private static final long serialVersionUID = 1L;

    @Column(name = "source_location")
    private String sourceLocation;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "disk_location")
    private String diskLocation;

    @Column(name = "filename")
    private String filename;

    @Column(name = "base_location")
    private String baseLocation;

    @Column(name = "md5_hash")
    private String md5Hash;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "download_status")
    @Enumerated(EnumType.STRING)
    private RepositoryStatus downloadStatus;

    @Column(name = "is_default")
    private boolean isDefault;

    @Column(name = "is_embedded")
    private boolean isEmbedded;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private RepositoryState state;

    @OneToMany(mappedBy = "firmwareRepositoryEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SoftwareComponentEntity> softwareComponents;

    @Column(name = "component_count")
    private int componentCount;

    @OneToMany(mappedBy = "firmwareRepositoryEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SoftwareBundleEntity> softwareBundles;

    @Column(name = "bundle_count")
    private int bundleCount;

    @Formula("(select count(*) from software_bundle b where b.userbundle = 't' and b.firmware_repository = id)")
    private int userBundleCount;

    @OneToMany(mappedBy = "firmwareRepositoryEntity", fetch = FetchType.EAGER, orphanRemoval = false, cascade = {
            CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    private Set<DeploymentEntity> deployments;

    @OneToMany(mappedBy = "firmwareRepositoryEntity", fetch = FetchType.LAZY, orphanRemoval = false, cascade = {
            CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    private Set<ServiceTemplateEntity> templates;

    @OneToMany(mappedBy = "deviceInventoryComplianceId.firmwareRepository", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DeviceInventoryComplianceEntity> deviceInventoryComplianceEntities;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDiskLocation() {
        return diskLocation;
    }

    public void setDiskLocation(String diskLocation) {
        this.diskLocation = diskLocation;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RepositoryStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(RepositoryStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public Set<SoftwareComponentEntity> getSoftwareComponents() {
        if (softwareComponents == null)
            this.softwareComponents = new HashSet<>();

        return softwareComponents;
    }

    public void setSoftwareComponents(Set<SoftwareComponentEntity> softwareComponents) {
        this.softwareComponents = softwareComponents;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
    }

    public RepositoryState getState() {
        return state;
    }

    public void setState(RepositoryState state) {
        this.state = state;
    }

    public Set<SoftwareBundleEntity> getSoftwareBundles() {
        if (this.softwareBundles == null)
            this.softwareBundles = new HashSet<>();
        return softwareBundles;
    }

    public void setSoftwareBundles(Set<SoftwareBundleEntity> softwareBundles) {
        this.softwareBundles = softwareBundles;
    }

    public Set<DeploymentEntity> getDeployments() {
        if (this.deployments == null)
            this.deployments = new HashSet<>();
        return deployments;
    }

    public void setDeployments(Set<DeploymentEntity> deployments) {
        this.getDeployments().clear();
        if (deployments != null) {
            for (DeploymentEntity deployment : deployments)
                deployment.setFirmwareRepositoryEntity(this);
            this.deployments.addAll(deployments);
        }
    }

    public void addDeployment(DeploymentEntity deployment) {
        if (deployment == null)
            return;

        deployment.setFirmwareRepositoryEntity(this);
        this.getDeployments().add(deployment);
    }

    public void removeDeployment(DeploymentEntity deployment) {
        if (deployment == null)
            return;

        deployment.setFirmwareRepositoryEntity(null);
        if (this.getDeployments().contains(deployment)) {
            this.getDeployments().remove(deployment);
        }
    }

    public int getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(int componentCount) {
        this.componentCount = componentCount;
    }

    public int getBundleCount() {
        return bundleCount;
    }

    public void setBundleCount(int bundleCount) {
        this.bundleCount = bundleCount;
    }

    public int getUserBundleCount() {
        return userBundleCount;
    }

    public void setUserBundleCount(int bundleCount) {
        this.userBundleCount = bundleCount;
    }

    public Set<ServiceTemplateEntity> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<ServiceTemplateEntity> templates) {
        this.templates = templates;
    }

    public Set<DeviceInventoryComplianceEntity> getDeviceInventoryComplianceEntities() {
        return deviceInventoryComplianceEntities;
    }

    public void setDeviceInventoryComplianceEntities(
            Set<DeviceInventoryComplianceEntity> deviceInventoryComplianceEntities) {
        this.deviceInventoryComplianceEntities = deviceInventoryComplianceEntities;
    }

    public FirmwareRepository getSimpleFirmwareRepository() {
        FirmwareRepository firmwareRepository = new FirmwareRepository();
        firmwareRepository.setId(this.getId());
        firmwareRepository.setName(this.getName());
        return firmwareRepository;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceLocation == null) ? 0 : sourceLocation.hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
        if (!(obj instanceof FirmwareRepositoryEntity)) {
            return false;
        }
        final FirmwareRepositoryEntity other = (FirmwareRepositoryEntity) obj;
        if (sourceLocation == null) {
            if (other.sourceLocation != null) {
                return false;
            }
        } else if (!sourceLocation.equals(other.sourceLocation)) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

}
