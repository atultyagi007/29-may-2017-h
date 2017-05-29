
/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.pg.orion.common.print.Dump;

/**
 * Represents Monitors, a part of infrastructure template. 
 * @author Bapu Patil
 *
 */
@XmlRootElement(name = "MonitorsSettings")
public class MonitorsSettings
{
    List<SNMPTrapSettings> snmpAlertDestination;
    List<EmailDestination> emailAlertDestination;

    private String smtpServer;
    private String sourceEmail;
    
    private boolean enableAuthentication;
    private String UserName;
    private String password;

    public MonitorsSettings() { }
    
    public List<SNMPTrapSettings> getSnmpAlertDestination() {
        return snmpAlertDestination;
    }

    public void setSnmpAlertDestination(List<SNMPTrapSettings> snmpAlertDestinationList) {
        this.snmpAlertDestination = snmpAlertDestinationList;
    }

    public List<EmailDestination> getEmailAlertDestination() {
        return emailAlertDestination;
    }

    public void setEmailAlertDestination(List<EmailDestination> emailAlertDestinationList) {
        this.emailAlertDestination = emailAlertDestinationList;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getSourceEmail() {
        return sourceEmail;
    }

    public void setSourceEmail(String sourceEmail) {
        this.sourceEmail = sourceEmail;
    }

    public boolean isEnableAuthentication() {
        return enableAuthentication;
    }

    public void setEnableAuthentication(boolean enableAuthentication) {
        this.enableAuthentication = enableAuthentication;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Dump contents.
    @Override
    public String toString() {
            return Dump.toString(this);
    }

    @Override
    public boolean equals(Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
    }
}
