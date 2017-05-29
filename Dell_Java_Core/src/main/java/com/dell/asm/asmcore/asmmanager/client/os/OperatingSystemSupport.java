package com.dell.asm.asmcore.asmmanager.client.os;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "OperatingSystemSupport")
@ApiModel()
public class OperatingSystemSupport {

    protected String operatingSystem;
    protected List<String> versions;
    
    public OperatingSystemSupport() {
        super();
    }
    
    public OperatingSystemSupport(final String operatingSystem, final List<String> versions) {
        this();
        this.operatingSystem = operatingSystem;
        this.versions = versions;
    }
    
    public String getOperatingSystem() {
        return operatingSystem;
    }
    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
    public List<String> getVersions() {
        return versions;
    }
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }
    @Override
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operatingSystem == null) ? 0 : operatingSystem.hashCode());
        result = prime * result + ((versions == null) ? 0 : versions.hashCode());
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
        if (!(obj instanceof OperatingSystemSupport)) {
            return false;
        }
        OperatingSystemSupport other = (OperatingSystemSupport) obj;
        if (operatingSystem == null) {
            if (other.operatingSystem != null) {
                return false;
            }
        } else if (!operatingSystem.equals(other.operatingSystem)) {
            return false;
        }
        if (versions == null) {
            if (other.versions != null) {
                return false;
            }
        } else if (!versions.equals(other.versions)) {
            return false;
        }
        return true;
    }
}
