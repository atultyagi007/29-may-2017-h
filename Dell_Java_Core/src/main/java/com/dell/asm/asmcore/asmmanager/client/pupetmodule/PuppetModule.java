/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.pupetmodule;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.pg.asm.server.client.device.LogicalNetworkInterface;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * The Managed Device exposed through REST interfaces. Used to support the minimal device inventory view.
 */
@XmlRootElement(name = "PuppetModule")
@ApiModel()
public class PuppetModule 
{

    @ApiModelProperty(value = "Name", required = true)
    private String name;

    @ApiModelProperty(value = "Directory Path")
    private String path;

    @ApiModelProperty(value = "List of Input Parameters")
    private List<PuppetModuleInput> puppetModuleInputParameters = new ArrayList<PuppetModuleInput>();
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<PuppetModuleInput> getPuppetModuleInputParameters() {
        return puppetModuleInputParameters;
    }

    public void setPuppetModuleInputParameters(List<PuppetModuleInput> puppetModuleInputParameters) {
        this.puppetModuleInputParameters = puppetModuleInputParameters;
    }

}
