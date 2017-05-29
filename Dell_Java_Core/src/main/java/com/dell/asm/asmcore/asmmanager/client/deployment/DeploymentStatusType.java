/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The DeploymentStatus represents the state a Deployment (otherwise known as a 'Service') in ASM may be in at any 
 * given time. 
 */
public enum DeploymentStatusType {

    /**
     * Deployment is pending for a new Deployment upon creating (in case it was 'scheduled' at a later time) and
     * remains Pending until the the deployment job begins.
     */
    @XmlEnumValue("pending")
    PENDING("Pending"),

    /**
     * Deployment is in progress once the deployment job begins, or when an update occurs (typically for a 
     * scale up or tear down scenario on an existing service).  
     */
    @XmlEnumValue("in_progress")
    IN_PROGRESS("In Progress"),

    /**
     * Deployment is complete once the everything associated with the service is deployed and the firmware is up to
     * date (if firmware is being managed).
     */
    @XmlEnumValue("complete")
    COMPLETE("Deployed"),

    /**
     * Deployment is in error if there was an error while deploying a device or upgrading the firmware.
     */
    @XmlEnumValue("error")
    ERROR("Error"),

    
    @XmlEnumValue("cancelled")
    CANCELLED("Cancelled"),
    
    /**
     * Deployment is updating the firmware when the firmware is being upgraded to a different version.
     */
    @XmlEnumValue("firmware_updating")
    FIRMWARE_UPDATING("Firmware Updating"),

    /**
     * Deployment is updating the software as part of a Deployment.
     */
    @XmlEnumValue("post_deployment_software_updating")
    POST_DEPLOYMENT_SOFTWARE_UPDATING("Post Deployment Software Updating");

    
    private String _label;

    private DeploymentStatusType(String label) {
        _label = label;
    }

    public String getLabel() {
        return _label;
    }

    public String getValue() {
        return name();
    }

    @Override
    public String toString() {
        return _label;
    }
}
