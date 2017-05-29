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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * @author Praharsh_Shah
 * 
 * ASM core Template for infrastructure 
 * 
 */
@XmlRootElement(name = "InfrastructureTemplate")
public class InfrastructureTemplate {

    private String templateName;
    private TemplateTypes templateType; // = "infrastructure";
    private String templateDescription;
    protected boolean isDraft;

    // @XmlElement(name = "WizardPageNumber")
    protected Integer wizardPageNumber;

    private GregorianCalendar createdDate;

    private String createdBy;

    private GregorianCalendar updatedDate;

    private String updatedBy;

    // private String displayName;
    private String id;
    // private String deviceType;

    // ip addressing fields go here
    IpAddressing ipAddressing;

    // credential fields go here
    Credentials credentials;

    // user fields go here
    Users users;

    // Future sprint Holdings
    private MonitorsSettings monitors;
    private TimeSettings timeSettings;
    private PowerSettings powerSettings;
    private NetworkSettings networkSettings;
    private List<String> syslogIpAddresesList;

    private FirmwareSettings firmwareSettings;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public TemplateTypes getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateTypes templateType) {
        this.templateType = templateType;
    }

    public String getTemplateDescription() {
        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public Integer getWizardPageNumber() {
        return wizardPageNumber;
    }

    public void setWizardPageNumber(Integer wizardPageNumber) {
        this.wizardPageNumber = wizardPageNumber;
    }

    public GregorianCalendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GregorianCalendar getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

//    public String getDisplayName() {
//        return displayName;
//    }
//
//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public String getDeviceType() {
//        return deviceType;
//    }
//
//    public void setDeviceType(String deviceType) {
//        this.deviceType = deviceType;
//    }

    /*
     * @XmlElement(name = "SNMPTrapSettings") public List<SNMPTrapSettings> getTrapDestinations() { return trapDestinations; }
     * 
     * public void setTrapDestinations(List<SNMPTrapSettings> trapDestinations) { this.trapDestinations = trapDestinations; }
     * 
     * @XmlElement(name = "EmailDestination") public List<EmailDestination> getEmailNotifications() { return emailNotifications; }
     * 
     * public void setEmailNotifications(List<EmailDestination> emailNotifications) { this.emailNotifications = emailNotifications; }
     */
    @XmlElement(name = "syslogIpAddresesList")
    public List<String> getSyslogIpAddreses() {
        return syslogIpAddresesList;
    }

    public void setSyslogIpAddreses(List<String> syslogIpAddreses) {
        this.syslogIpAddresesList = syslogIpAddreses;
    }

    public TimeSettings getTimeSettings() {
        return timeSettings;
    }

    public void setTimeSettings(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    public PowerSettings getPowerSettings() {
        return powerSettings;
    }

    public void setPowerSettings(PowerSettings powerSettings) {
        this.powerSettings = powerSettings;
    }

    public NetworkSettings getNetworkSettings() {
        return networkSettings;
    }

    public void setNetworkSettings(NetworkSettings networkSettings) {
        this.networkSettings = networkSettings;
    }

    public IpAddressing getIpAddressing() {
        return ipAddressing;
    }

    public void setIpAddressing(IpAddressing ipAddressing) {
        this.ipAddressing = ipAddressing;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public MonitorsSettings getMonitors() {
        return monitors;
    }

    public void setMonitors(MonitorsSettings monitors) {
        this.monitors = monitors;
    }

    public FirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    public void setFirmwareSettings(FirmwareSettings firmwareSettings) {
        this.firmwareSettings = firmwareSettings;
    }
}
