/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.pupetmodule;


import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * The Managed Device exposed through REST interfaces. Used to support the minimal device inventory view.
 */
@ApiModel(value = "Puppet Module Input Parameters", description = "Input parameters supported by this puppet module")
@XmlRootElement(name = "PuppetModuleInput")
public class PuppetModuleInput
{

    
    @ApiModelProperty(value = "Input Parameter Type", required = true)
    private PuppetModuleInputType inputParameterType;

    @ApiModelProperty(value = "Input Parameter", required = true)
    private String inputParameter;

    @ApiModelProperty(value = "Input Parameter Default Value", required = false)
    private String inputParameterDefaultValue;

    public String getInputParameterRequired() {
        return inputParameterRequired;
    }

    public void setInputParameterRequired(String inputParameterRequired) {
        this.inputParameterRequired = inputParameterRequired;
    }

    public String getInputParameterHideFromTemplate() {
        return inputParameterHideFromTemplate;
    }

    public void setInputParameterHideFromTemplate(String inputParameterHideFromTemplate) {
        this.inputParameterHideFromTemplate = inputParameterHideFromTemplate;
    }

    @ApiModelProperty(value = "Input Parameter Required", required = false)
    private String inputParameterRequired;

    @ApiModelProperty(value = "Input Parameter Hide in UI", required = false)
    private String inputParameterHideFromTemplate;

    public PuppetModuleInputType getInputParameterType() {
        return inputParameterType;
    }

    public void setInputParameterType(PuppetModuleInputType inputParameterType) {
        this.inputParameterType = inputParameterType;
    }

    public String getInputParameter() {
        return inputParameter;
    }

    public void setInputParameter(String inputParameter) {
        this.inputParameter = inputParameter;
    }

    public String getInputParameterDefaultValue() {
        return inputParameterDefaultValue;
    }

    public void setInputParameterDefaultValue(String inputParameterDefaultValue) {
        this.inputParameterDefaultValue = inputParameterDefaultValue;
    }
    

}
