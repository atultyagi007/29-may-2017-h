/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A service definition for create a brownfield service for Esxi environments.
 */
@XmlRootElement(name = "esxiServiceDefinition")
@XmlType(name = "AbstractCredential", propOrder = {
        "clusterComponentName",
        "vcenterRefId",
        "datacenterName",
        "clusterName", })
public class EsxiServiceDefinition extends ServiceDefinition {
    
    // Member Variables
    private String clusterComponentName;
    private String vcenterRefId;
    private String datacenterName;
    private String clusterName;

    /**
     * Default constructor for the class.
     */
    public EsxiServiceDefinition(){}

    public String getClusterComponentName() {
        return clusterComponentName;
    }

    public void setClusterComponentName(String clusterComponentName) {
        this.clusterComponentName = clusterComponentName;
    }

    public String getVcenterRefId() {
        return vcenterRefId;
    }

    public void setVcenterRefId(String vcenterRefId) {
        this.vcenterRefId = vcenterRefId;
    }

    public String getDatacenterName() {
        return datacenterName;
    }

    public void setDatacenterName(String datacenterName) {
        this.datacenterName = datacenterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
        
}
