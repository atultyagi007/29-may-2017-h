package com.dell.asm.asmcore.asmmanager.db.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * The persistent class for the groups database table.
 * 
 */
@Entity
@Table(name = "groups")
@NamedQuery(name = "Group.findAll", query = "SELECT g FROM DeviceGroupEntity g")
public class DeviceGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "seq_id")
    @SequenceGenerator(name = "seq_groups_device", sequenceName = "seq_groups", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_groups_device")
    private Long seqId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private GregorianCalendar createdDate;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private GregorianCalendar updatedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @CollectionTable(name = "groups_users", joinColumns = @JoinColumn(name = "groups_seq_id"))
    @Column(name = "user_seq_id")
    private Set<Long> groupsUsers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "groups_device_inventory", joinColumns = { @JoinColumn(name = "groups_seq_id") }, inverseJoinColumns = { @JoinColumn(name = "devices_inventory_seq_id") })
    private List<DeviceInventoryEntity> deviceInventories = new ArrayList<>();

    public DeviceGroupEntity() {
    }

    /**
     * @return the seqId
     */
    public Long getSeqId() {
        return seqId;
    }

    /**
     * @param seqId
     *            the seqId to set
     */
    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy
     *            the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the createdDate
     */
    public GregorianCalendar getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate
     *            the createdDate to set
     */
    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy
     *            the updatedBy to set
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return the updatedDate
     */
    public GregorianCalendar getUpdatedDate() {
        return updatedDate;
    }

    /**
     * @param updatedDate
     *            the updatedDate to set
     */
    public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * @return the groupsUsers
     */
    public Set<Long> getGroupsUsers() {
        return groupsUsers;
    }

    /**
     * @param groupsUsers
     *            the groupsUsers to set
     */
    public void setGroupsUsers(Set<Long> groupsUsers) {
        this.groupsUsers = groupsUsers;
    }

    /**
     * @return the deviceInventories
     */
    public List<DeviceInventoryEntity> getDeviceInventories() {
        return deviceInventories;
    }

    /**
     * @param deviceInventories
     *            the deviceInventories to set
     */
    public void setDeviceInventories(List<DeviceInventoryEntity> deviceInventories) {
        this.deviceInventories = deviceInventories;
    }

    @Override
    public String toString() {
        return "DeviceGroupEntity [seqId=" + seqId + ", name=" + name + "]";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (seqId != null ? seqId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DeviceGroupEntity)) {
            return false;
        }
        DeviceGroupEntity other = (DeviceGroupEntity) object;
        if ((this.seqId == null && other.seqId != null) || (this.seqId != null && !this.seqId.equals(other.seqId))) {
            return false;
        }
        return true;
    }
}