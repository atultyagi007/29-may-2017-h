/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.ForeignKey;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.pg.asm.identitypoolmgr.common.TableConstants;

@Entity
@Table(name = "device_inventory_compliance_map")
public class DeviceInventoryComplianceEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    private DeviceInventoryComplianceId deviceInventoryComplianceId;
    
    @Column(name = "compliance", nullable = false)
    @Enumerated(EnumType.STRING)
    private CompliantState compliance;
    
    @Column(name = TableConstants.BE_CREATED_BY)
    private String createdBy;

    @Column(name = TableConstants.BE_CREATED_DATE)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = TableConstants.BE_UPDATED_BY)
    private String updatedBy;

    @Column(name = TableConstants.BE_UPDATED_DATE)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;
    
    public DeviceInventoryComplianceEntity() {}
    
    public DeviceInventoryComplianceEntity(final DeviceInventoryEntity deviceInventory, 
            final FirmwareRepositoryEntity firmwareRepository) {
        this(new DeviceInventoryComplianceId(deviceInventory,firmwareRepository));
    }
    
    public DeviceInventoryComplianceEntity(final DeviceInventoryComplianceId deviceInventoryComplianceId) {
        this.deviceInventoryComplianceId = deviceInventoryComplianceId;
    }
    
    public DeviceInventoryComplianceId getDeviceInventoryComplianceId() {
        return deviceInventoryComplianceId;
    }

    public DeviceInventoryEntity getDeviceInventory() {
        return deviceInventoryComplianceId.deviceInventory;
    }

    public FirmwareRepositoryEntity getFirmwareRepository() {
        return deviceInventoryComplianceId.firmwareRepository;
    }

    public CompliantState getCompliance() {
        return compliance;
    }

    public void setCompliance(CompliantState compliance) {
        this.compliance = compliance;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceInventoryComplianceId == null) ? 0 : deviceInventoryComplianceId.hashCode());
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
        if (!(obj instanceof DeviceInventoryComplianceEntity)) {
            return false;
        }
        final DeviceInventoryComplianceEntity other = (DeviceInventoryComplianceEntity) obj;
        if (deviceInventoryComplianceId == null) {
            if (other.deviceInventoryComplianceId != null) {
                return false;
            }
        } else if (!deviceInventoryComplianceId.equals(other.deviceInventoryComplianceId)) {
            return false;
        }
        return true;
    }

    @Embeddable
    public static class DeviceInventoryComplianceId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ManyToOne
        @JoinColumn(name = "device_inventory_id", referencedColumnName = "ref_id")
        @ForeignKey(name = "compliance_map_to_device_inventory_fk")
        private DeviceInventoryEntity deviceInventory;
        
        @ManyToOne
        @JoinColumn(name = "firmware_repository_id",  referencedColumnName = "id")
        @ForeignKey(name = "compliance_map_to_firmware_repo_fk")
        private FirmwareRepositoryEntity firmwareRepository;
        
        public DeviceInventoryComplianceId() {}
        
        public DeviceInventoryComplianceId(final DeviceInventoryEntity deviceInventory, 
                final FirmwareRepositoryEntity firmwareRepository) {
            this.deviceInventory = deviceInventory;
            this.firmwareRepository = firmwareRepository;
        }
            
        public DeviceInventoryEntity getDeviceInventory() {
            return deviceInventory;
        }

        public FirmwareRepositoryEntity getFirmwareRepository() {
            return firmwareRepository;
        }

        @Override
        public boolean equals(Object instance) {
            if (instance == null)
                return Boolean.FALSE;

            if (!(instance instanceof DeviceInventoryComplianceId))
                return Boolean.FALSE;

            final DeviceInventoryComplianceId other = (DeviceInventoryComplianceId) instance;
            if (!(deviceInventory.getRefId().equals(other.getDeviceInventory().getRefId()))) {
                return Boolean.FALSE;
            }

            if (!(firmwareRepository.getId().equals(other.getFirmwareRepository().getId()))) {
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (this.deviceInventory != null ? this.deviceInventory.hashCode() : 0);
            hash = 47 * hash + (this.firmwareRepository != null ? this.firmwareRepository.hashCode() : 0);
            return hash;
        }
    }
}
