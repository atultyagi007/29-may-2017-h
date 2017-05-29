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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.wordnik.swagger.annotations.ApiModel;

/**
 * Defines a Service Definition for the discovery of a brownfield service.
 */
@XmlRootElement(name = "serviceDefinition")
@XmlType(name = "ServiceDefinition", propOrder = {})
@XmlSeeAlso({ EsxiServiceDefinition.class })
@ApiModel()
public abstract class ServiceDefinition {
    

}
