package com.dell.asm.asmcore.asmmanager.db.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.dell.pg.asm.identitypoolmgr.common.TableConstants;
import com.google.common.base.Objects;

@Entity
@Table(name = "operating_system_version")
public class OperatingSystemVersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = TableConstants.BE_ID, columnDefinition = TableConstants.BE_ID, insertable = true, updatable = false, nullable = false, unique = true)
    private String id;
    
    @Column(name = "operating_system")
    private String operatingSystem;

    @Column(name = "version")
    private String version;
    
    @OneToMany(mappedBy="addOnModuleOperatingSystemVersionId.operatingSystemVersion", fetch = FetchType.EAGER)
    private Set<AddOnModuleOperatingSystemVersionEntity> addOnModuleOperatingSystemVersions;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public Set<AddOnModuleOperatingSystemVersionEntity> getAddOnModuleOperatingSystemVersions() {
        return addOnModuleOperatingSystemVersions;
    }

    public void setAddOnModuleOperatingSystemVersions(
            Set<AddOnModuleOperatingSystemVersionEntity> addOnModuleOperatingSystemVersions) {
        this.addOnModuleOperatingSystemVersions = addOnModuleOperatingSystemVersions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operatingSystem == null) ? 0 : operatingSystem.hashCode());
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
        if (!(obj instanceof OperatingSystemVersionEntity)) {
            return false;
        }
        OperatingSystemVersionEntity other = (OperatingSystemVersionEntity) obj;
        if (operatingSystem == null) {
            if (other.operatingSystem != null) {
                return false;
            }
        } else if (!operatingSystem.equals(other.operatingSystem)) {
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
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("operatingSystem", operatingSystem)
                .add("version", version)
                .toString();
    }    
}
