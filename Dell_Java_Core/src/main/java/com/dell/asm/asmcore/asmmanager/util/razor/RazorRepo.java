/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.razor;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlRootElement
@XmlType(propOrder = {
        "spec",
        "id",
        "name",
        "task"
})
public class RazorRepo {
    private String spec;
    private String id;
    private String name;
    private String task;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTask() { return task; }

    public void setTask(String task) { this.task = task; }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("spec", spec)
                .append("name", name)
                .append("task", task)
                .toString();
    }
}
