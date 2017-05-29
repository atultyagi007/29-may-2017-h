/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.Arrays;
import java.util.List;

public class OsReleaseInfo {
    private String operatingSystem;
    private List<String> releaseVersions;

    public OsReleaseInfo(String operatingSystem, String ... releaseVersions) {
        setOperatingSystem(operatingSystem);
        setReleaseVersions(Arrays.asList(releaseVersions));
    }

    public OsReleaseInfo() {
        this(null);
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public List<String> getReleaseVersions() {
        return releaseVersions;
    }

    public void setReleaseVersions(List<String> releaseVersions) {
        this.releaseVersions = releaseVersions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operatingSystem == null) ? 0 : operatingSystem.hashCode());
        result = prime * result + ((releaseVersions == null) ? 0 : releaseVersions.hashCode());
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
        if (!(obj instanceof OsReleaseInfo)) {
            return false;
        }
        OsReleaseInfo other = (OsReleaseInfo) obj;
        if (operatingSystem == null) {
            if (other.operatingSystem != null) {
                return false;
            }
        } else if (!operatingSystem.equals(other.operatingSystem)) {
            return false;
        }
        if (releaseVersions == null) {
            if (other.releaseVersions != null) {
                return false;
            }
        } else if (!releaseVersions.equals(other.releaseVersions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OsReleaseInfo [operatingSystem=").append(operatingSystem).append(", releaseVersions=")
                .append(releaseVersions).append("]");
        return builder.toString();
    }

}
