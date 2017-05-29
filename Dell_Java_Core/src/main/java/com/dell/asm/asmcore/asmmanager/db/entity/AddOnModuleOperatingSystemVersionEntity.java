package com.dell.asm.asmcore.asmmanager.db.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "add_on_module_operating_system_version_map")
public class AddOnModuleOperatingSystemVersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    private AddOnModuleOperatingSystemVersionId addOnModuleOperatingSystemVersionId;
    
    public AddOnModuleOperatingSystemVersionEntity() {}
    
    public AddOnModuleOperatingSystemVersionEntity(final AddOnModuleEntity addOnModule, 
            final OperatingSystemVersionEntity operatingSystemVersion) {
        this(new AddOnModuleOperatingSystemVersionId(addOnModule, operatingSystemVersion));
    }
    
    public AddOnModuleOperatingSystemVersionEntity(
            final AddOnModuleOperatingSystemVersionId addOnModuleOperatingSystemVersionId) {
        this.addOnModuleOperatingSystemVersionId = addOnModuleOperatingSystemVersionId;
    }
    
    public AddOnModuleEntity getAddOnModule() {
        return addOnModuleOperatingSystemVersionId.getAddOnModule();
    }
     
    public OperatingSystemVersionEntity getOperatingSystemVersion() {
        return addOnModuleOperatingSystemVersionId.getOperatingSystemVersion();
    }
    
    public AddOnModuleOperatingSystemVersionId getAddOnModuleOperatingSystemVersionId() {
        return addOnModuleOperatingSystemVersionId;
    }

    @Embeddable
    public static class AddOnModuleOperatingSystemVersionId implements Serializable {

        private static final long serialVersionUID = 1L;
        
        @ManyToOne
        @JoinColumn(name = "add_on_module_id", referencedColumnName = "id")
        @ForeignKey(name = "aomosv_map_to_add_on_module_fk")
        private AddOnModuleEntity addOnModule;
        
        @ManyToOne
        @JoinColumn(name = "operating_system_version_id", referencedColumnName = "id")
        @ForeignKey(name = "aomosv_map_to_operating_system_version_fk")
        private OperatingSystemVersionEntity operatingSystemVersion;
        
        public AddOnModuleOperatingSystemVersionId() {}
        
        public AddOnModuleOperatingSystemVersionId(final AddOnModuleEntity addOnModule,
                final OperatingSystemVersionEntity operatingSystemVersion) {
            this.addOnModule = addOnModule;
            this.operatingSystemVersion = operatingSystemVersion;
        }
        
        public AddOnModuleEntity getAddOnModule() {
            return addOnModule;
        }
        
        public OperatingSystemVersionEntity getOperatingSystemVersion() {
            return operatingSystemVersion;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((addOnModule == null) ? 0 : addOnModule.hashCode());
            result = prime * result + ((operatingSystemVersion == null) ? 0 : operatingSystemVersion.hashCode());
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
            if (!(obj instanceof AddOnModuleOperatingSystemVersionId)) {
                return false;
            }
            AddOnModuleOperatingSystemVersionId other = (AddOnModuleOperatingSystemVersionId) obj;
            if (addOnModule == null) {
                if (other.addOnModule != null) {
                    return false;
                }
            } else if (!addOnModule.equals(other.addOnModule)) {
                return false;
            }
            if (operatingSystemVersion == null) {
                if (other.operatingSystemVersion != null) {
                    return false;
                }
            } else if (!operatingSystemVersion.equals(other.operatingSystemVersion)) {
                return false;
            }
            return true;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddOnModuleOperatingSystemVersionEntity)) return false;

        AddOnModuleOperatingSystemVersionEntity entity = (AddOnModuleOperatingSystemVersionEntity) o;

        return getAddOnModuleOperatingSystemVersionId().equals(entity.getAddOnModuleOperatingSystemVersionId());

    }

    @Override
    public int hashCode() {
        return getAddOnModuleOperatingSystemVersionId().hashCode();
    }
}
