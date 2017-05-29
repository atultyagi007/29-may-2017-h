package com.dell.asm.asmcore.asmmanager.db.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: J_Fowler
 * Date: 1/7/14
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "deployment_to_vm_map", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = { "vm_id" }))
public class VMRefEntity {
    @Column(name = "deployment_id")
    private String deploymentId;

    @Id
    @Column(name="vm_id", columnDefinition = "vm_id", insertable = true, updatable = true, nullable = false, unique = true)
    private String vmId;
    
    @Column(name = "vm_model")
    private String vmModel;
    
    @Column(name = "vm_ipaddress")
    private String vmIpaddress;
    
    @Column(name = "vm_servicetag")
    private String vmServiceTag;

    @Column(name = "vm_manufacturer")
    private String vmManufacturer;
    
    
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getVmModel() {
        return vmModel;
    }

    public void setVmModel(String vmModel) {
        this.vmModel = vmModel;
    }

    public String getVmIpaddress() {
        return vmIpaddress;
    }

    public void setVmIpaddress(String vmIpaddress) {
        this.vmIpaddress = vmIpaddress;
    }

    public String getVmServiceTag() {
        return vmServiceTag;
    }

    public void setVmServiceTag(String vmServiceTag) {
        this.vmServiceTag = vmServiceTag;
    }

    public String getVmManufacturer() {
        return vmManufacturer;
    }

    public void setVmManufacturer(String vmManufacturer) {
        this.vmManufacturer = vmManufacturer;
    }
}
