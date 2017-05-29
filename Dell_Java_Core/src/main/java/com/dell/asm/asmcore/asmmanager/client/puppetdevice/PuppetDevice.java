/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.puppetdevice;



import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * The Managed Device exposed through REST interfaces. Used to support the minimal device inventory view.
 */
@XmlRootElement(name = "PuppetDevice")
@ApiModel()
public class PuppetDevice 
{       
    @ApiModelProperty(value = "Name", required = true)
    private String name;

    @ApiModelProperty(value = "Data", required = true)
    private Map<String, String> data = new HashMap<String,String>();
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}


}
