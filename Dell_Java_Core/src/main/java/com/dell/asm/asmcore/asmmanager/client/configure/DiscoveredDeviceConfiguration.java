/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configure;

import com.dell.asm.asmcore.asmmanager.client.configure.NetworkIdentity;
import com.wordnik.swagger.annotations.ApiModel;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Device Configuration.
 */
@XmlRootElement(name = "DiscoveredDeviceConfiguration")
@ApiModel()
public class DiscoveredDeviceConfiguration {

    public NetworkIdentity getChassisNetworkIdentity() {
        return chassisNetworkIdentity;
    }

    public void setChassisNetworkIdentity(NetworkIdentity chassisNetworkIdentity) {
        this.chassisNetworkIdentity = chassisNetworkIdentity;
    }

    private NetworkIdentity chassisNetworkIdentity;

    public NetworkIdentity getServerNetworkIdentity() {
        return serverNetworkIdentity;
    }

    public void setServerNetworkIdentity(NetworkIdentity serverNetworkIdentity) {
        this.serverNetworkIdentity = serverNetworkIdentity;
    }

    public NetworkIdentity getIomNetworkIdentity() {
        return iomNetworkIdentity;
    }

    public void setIomNetworkIdentity(NetworkIdentity iomNetworkIdentity) {
        this.iomNetworkIdentity = iomNetworkIdentity;
    }

    public NetworkIdentity getBladeNetworkIdentity() {
        return bladeNetworkIdentity;
    }

    public void setBladeNetworkIdentity(NetworkIdentity bladeNetworkIdentity) {
        this.bladeNetworkIdentity = bladeNetworkIdentity;
    }

    private NetworkIdentity bladeNetworkIdentity;
    private NetworkIdentity iomNetworkIdentity;
    private NetworkIdentity serverNetworkIdentity;

    public String getChassisCredentialId() {
        return chassisCredentialId;
    }

    public void setChassisCredentialId(String chassisCredentialId) {
        this.chassisCredentialId = chassisCredentialId;
    }

    public String getServerCredentialId() {
        return serverCredentialId;
    }

    public void setServerCredentialId(String serverCredentialId) {
        this.serverCredentialId = serverCredentialId;
    }

    public String getIomCredentialId() {
        return iomCredentialId;
    }

    public void setIomCredentialId(String iomCredentialId) {
        this.iomCredentialId = iomCredentialId;
    }

    private String chassisCredentialId ;

    private String serverCredentialId ;

    public String getBladeCredentialId() {
        return bladeCredentialId;
    }

    public void setBladeCredentialId(String bladeCredentialId) {
        this.bladeCredentialId = bladeCredentialId;
    }

    private String bladeCredentialId ;

    private String iomCredentialId ;

}
