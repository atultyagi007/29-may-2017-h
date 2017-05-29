/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * @author Praharsh_Shah
 * 
 * ASM core TemplateList for infrastructure 
 * 
 */
@XmlRootElement(name = "TemplateList")
public class TemplateList {
    private List<InfrastructureTemplate> templateList;
    
    private int totalCount;

    @XmlElement(name = "Template")
    public List<InfrastructureTemplate> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<InfrastructureTemplate> templateList) {
        this.templateList = templateList;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }	
}
