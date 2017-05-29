/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

public class ModuleRequirement {
    private String name;
    private String versionRequirement;

    public ModuleRequirement(String name, String versionRequirement) {
        setName(name);
        setVersionRequirement(versionRequirement);
    }

    public ModuleRequirement() {
        this(null, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersionRequirement() {
        return versionRequirement;
    }

    public void setVersionRequirement(String versionRequirement) {
        this.versionRequirement = versionRequirement;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((versionRequirement == null) ? 0 : versionRequirement.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ModuleRequirement)) {
            return false;
        }
        ModuleRequirement other = (ModuleRequirement) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (versionRequirement == null) {
            if (other.versionRequirement != null) {
                return false;
            }
        } else if (!versionRequirement.equals(other.versionRequirement)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ModuleRequirement [name=").append(name).append(", versionRequirement=")
                .append(versionRequirement).append("]");
        return builder.toString();
    }
    
}
