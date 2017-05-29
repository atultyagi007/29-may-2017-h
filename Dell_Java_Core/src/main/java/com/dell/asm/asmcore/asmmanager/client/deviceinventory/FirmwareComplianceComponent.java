/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "FirmwareComplianceComponent")
public class FirmwareComplianceComponent {
    
    // Member Variables
    private CompliantState compliantState;
    private List<SoftwareComponent> embeddedRepoComponents;
    private List<SoftwareComponent> defaultRepoComponents;
    private List<SoftwareComponent> serviceRepoComponents;
    
    public CompliantState getCompliantState() {
        return compliantState;
    }

    public void setCompliantState(CompliantState compliantState) {
        this.compliantState = compliantState;
    }

    public List<SoftwareComponent> getEmbeddedRepoComponents() {
        return embeddedRepoComponents;
    }

    public void setEmbeddedRepoComponents(List<SoftwareComponent> embeddedRepoComponents) {
        this.embeddedRepoComponents = embeddedRepoComponents;
    }

    public List<SoftwareComponent> getDefaultRepoComponents() {
        return defaultRepoComponents;
    }

    public void setDefaultRepoComponents(List<SoftwareComponent> defaultRepoComponents) {
        this.defaultRepoComponents = defaultRepoComponents;
    }

    public List<SoftwareComponent> getServiceRepoComponents() {
        return serviceRepoComponents;
    }

    public void setServiceRepoComponents(List<SoftwareComponent> serviceRepoComponents) {
        this.serviceRepoComponents = serviceRepoComponents;
    }

}
