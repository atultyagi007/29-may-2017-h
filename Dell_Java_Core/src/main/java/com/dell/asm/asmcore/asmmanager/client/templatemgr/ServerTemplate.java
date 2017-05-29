/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import java.util.GregorianCalendar;

import javax.xml.bind.annotation.XmlRootElement;

// import com.dell.asm.server.client.policy.ServerRequirementsPolicy;
import com.dell.asm.server.client.policy.BIOSPolicy;
import com.dell.asm.server.client.policy.BootInfoPolicy;
import com.dell.asm.server.client.policy.BootOrderPolicy;
import com.dell.asm.server.client.policy.FirmwarePolicy;
import com.dell.asm.server.client.policy.NICPolicy;
import com.dell.asm.server.client.policy.RAIDPolicy;
import com.dell.asm.server.client.policy.ServerRequirementsPolicy;
// import com.dell.pg.jraf.client.profmgr.JrafTemplate;

/*
 * @author Praharsh_Shah
 * 
 * ASM - Template for server 
 * 
 */
@XmlRootElement(name = "ServerTemplate")
public class ServerTemplate { // extends JrafTemplate
 
    private String id;
    private String name;    
    private String displayName;
    private String description;
    
    private GregorianCalendar dateCreated;
    private GregorianCalendar dateModified;
    private String createdBy;
    private String lastModifiedBy;
    
    private ServerRequirementsPolicy applicableServerRequirements;
    private BootOrderPolicy serverBootOrderPolicy;
    private BootInfoPolicy serverBootInfoPolicy;
    private BIOSPolicy serverBiosPolicy;
    private RAIDPolicy serverRaidPolicy;
    private NICPolicy serverNicPolicy;
    private FirmwarePolicy serverFirmwarePolicy;
    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    
    /**
     * @return the id
     */
    public String getId() {
        return id;       
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the dateCreated
     */
    public GregorianCalendar getDateCreated() {
        return dateCreated;
    }
    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(GregorianCalendar dateCreated) {
        this.dateCreated = dateCreated;
    }
    /**
     * @return the dateModified
     */
    public GregorianCalendar getDateModified() {
        return dateModified;
    }
    /**
     * @param dateModified the dateModified to set
     */
    public void setDateModified(GregorianCalendar dateModified) {
        this.dateModified = dateModified;
    }
    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }
    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    /**
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    /**
     * @param lastModifiedBy the lastModifiedBy to set
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
    /**
     * @return the applicableServerRequirements
     */
    public ServerRequirementsPolicy getApplicableServerRequirements() {
        return applicableServerRequirements;
    }
    /**
     * @param applicableServerRequirements the applicableServerRequirements to set
     */
    public void setApplicableServerRequirements(ServerRequirementsPolicy applicableServerRequirements) {
        this.applicableServerRequirements = applicableServerRequirements;
    }
    /**
     * @return the serverBootOrderPolicy
     */
    public BootOrderPolicy getServerBootOrderPolicy() {
        return serverBootOrderPolicy;
    }
    /**
     * @param serverBootOrderPolicy the serverBootOrderPolicy to set
     */
    public void setServerBootOrderPolicy(BootOrderPolicy serverBootOrderPolicy) {
        this.serverBootOrderPolicy = serverBootOrderPolicy;
    }
    /**
     * @return the serverBootInfoPolicy
     */
    public BootInfoPolicy getServerBootInfoPolicy() {
        return serverBootInfoPolicy;
    }
    /**
     * @param serverBootInfoPolicy the serverBootInfoPolicy to set
     */
    public void setServerBootInfoPolicy(BootInfoPolicy serverBootInfoPolicy) {
        this.serverBootInfoPolicy = serverBootInfoPolicy;
    }
    /**
     * @return the serverBiosPolicy
     */
    public BIOSPolicy getServerBiosPolicy() {
        return serverBiosPolicy;
    }
    /**
     * @param serverBiosPolicy the serverBiosPolicy to set
     */
    public void setServerBiosPolicy(BIOSPolicy serverBiosPolicy) {
        this.serverBiosPolicy = serverBiosPolicy;
    }
    /**
     * @return the serverRaidPolicy
     */
    public RAIDPolicy getServerRaidPolicy() {
        return serverRaidPolicy;
    }
    /**
     * @param serverRaidPolicy the serverRaidPolicy to set
     */
    public void setServerRaidPolicy(RAIDPolicy serverRaidPolicy) {
        this.serverRaidPolicy = serverRaidPolicy;
    }
    /**
     * @return the serverNicPolicy
     */
    public NICPolicy getServerNicPolicy() {
        return serverNicPolicy;
    }
    /**
     * @param serverNicPolicy the serverNicPolicy to set
     */
    public void setServerNicPolicy(NICPolicy serverNicPolicy) {
        this.serverNicPolicy = serverNicPolicy;
    }
    /**
     * @return the serverFirmwarePolicy
     */
    public FirmwarePolicy getServerFirmwarePolicy() {
        return serverFirmwarePolicy;
    }
    /**
     * @param serverFirmwarePolicy the serverFirmwarePolicy to set
     */
    public void setServerFirmwarePolicy(FirmwarePolicy serverFirmwarePolicy) {
        this.serverFirmwarePolicy = serverFirmwarePolicy;
    }
    
    
}
