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
public enum PuppetModuleInputType {
                
    STRING("String"),
    INTEGER("Integer"),
    BOOLEAN("Boolean");

        private String _label;
        
        private PuppetModuleInputType(String label){_label = label;}
        
        public String getLabel(){return _label;}
           
        public String getValue(){return name();}
           
        @Override
        public String toString(){return _label;} 
}
