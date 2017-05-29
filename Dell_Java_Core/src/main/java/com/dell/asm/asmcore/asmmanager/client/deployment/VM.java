package com.dell.asm.asmcore.asmmanager.client.deployment;

/**
 * Created with IntelliJ IDEA.
 * User: J_Fowler
 * Date: 1/17/14
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class VM {
    private String certificateName;
    private String vmModel;
    private String vmIpaddress;
    private String vmManufacturer;
    private String vmServiceTag;
    
    public String getCertificateName() {
        return certificateName;
    }

    public String getVmIpaddress() {
        return vmIpaddress;
    }

    public void setVmIpaddress(String vmIpaddress) {
        this.vmIpaddress = vmIpaddress;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getVmModel() {
        return vmModel;
    }

    public void setVmModel(String vmModel) {
        this.vmModel = vmModel;
    }

    public String getVmManufacturer() {
        return vmManufacturer;
    }

    public void setVmManufacturer(String vmManufacturer) {
        this.vmManufacturer = vmManufacturer;
    }

    public String getVmServiceTag() {
        return vmServiceTag;
    }

    public void setVmServiceTag(String vmServiceTag) {
        this.vmServiceTag = vmServiceTag;
    }
    
    

}
