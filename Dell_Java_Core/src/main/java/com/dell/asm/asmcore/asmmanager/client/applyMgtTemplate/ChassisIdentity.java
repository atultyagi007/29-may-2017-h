package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ChassisIdentity")
public class ChassisIdentity {
    
    private String cmcDnsName;
    private int powerCapPercentage;
    private String dataCenterCharacter;
    private String aisle;
    private String rack;
    private int rackSlot;
    private Set<IomIdentity> iomIdentities;
    private Set<ServerIdentity> serverIdentities;
    private String displayName;
      
    private String chassisName;
    public String getChassisName() {
        return chassisName;
    }
    public void setChassisName(String chassisName) {
        this.chassisName = chassisName;
    }
    public String getCmcDnsName() {
        return cmcDnsName;
    }
    public void setCmcDnsName(String cmcDnsName) {
        this.cmcDnsName = cmcDnsName;
    }
    public int getPowerCapPercentage() {
        return powerCapPercentage;
    }
    public void setPowerCapPercentage(int powerCapPercentage) {
        this.powerCapPercentage = powerCapPercentage;
    }
    public String getDataCenterCharacter() {
        return dataCenterCharacter;
    }
    public void setDataCenterCharacter(String dataCenterCharacter) {
        this.dataCenterCharacter = dataCenterCharacter;
    }
    public String getAisle() {
        return aisle;
    }
    public void setAisle(String aisle) {
        this.aisle = aisle;
    }
    public String getRack() {
        return rack;
    }
   
    public int getRackSlot() {
        return rackSlot;
    }
    public void setRackSlot(int rackSlot) {
        this.rackSlot = rackSlot;
    }
    public Set<IomIdentity> getIomIdentities() {
        
        if (iomIdentities == null)
            iomIdentities = new HashSet<IomIdentity>();
        return iomIdentities;
    }
    public void setIomIdentities(Set<IomIdentity> iomIdentities) {
        this.iomIdentities = iomIdentities;
    }
    public Set<ServerIdentity> getServerIdentities() {
        
        if (serverIdentities == null)
            serverIdentities = new HashSet<ServerIdentity>();
    
        return serverIdentities;
    }
    public void setServerIdentities(Set<ServerIdentity> serverIdentities) {
        this.serverIdentities = serverIdentities;
    }
    public void setRack(String rack) {
       this.rack = rack;
        
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
      
}
