package com.dell.asm.asmcore.asmmanager.client.addonmodule;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.os.OperatingSystemSupport;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "AddOnModule")
@ApiModel()
public class AddOnModule {

    protected URL uploadUrl;
    protected String id;
    protected String name;
    protected String description;
    protected String modulePath;
    protected String version;
    protected boolean active;
    protected boolean defaultModule;
    protected String uploadedBy;
    protected Date uploadedDate;
    protected List<OperatingSystemSupport> operatingSystemSupport = new ArrayList<OperatingSystemSupport>();
    protected List<ServiceTemplateComponent> classes = new ArrayList<ServiceTemplateComponent>();
    protected List<ServiceTemplateComponent> types = new ArrayList<ServiceTemplateComponent>();
    protected List<ServiceTemplate> templates = new ArrayList<>();
    protected List<Deployment> deployments = new ArrayList<>();
    
    public URL getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(URL uploadUrl) {
        this.uploadUrl = uploadUrl;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefaultModule() {
        return defaultModule;
    }

    public void setDefaultModule(boolean defaultModule) {
        this.defaultModule = defaultModule;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
    
    public List<OperatingSystemSupport> getOperatingSystemSupport() {
        return operatingSystemSupport;
    }

    public void setOperatingSystemSupport(List<OperatingSystemSupport> operatingSystemSupport) {
        this.operatingSystemSupport = operatingSystemSupport;
    }
    
    public void addOperatingSystemSupport(final String operatingSystem, final List<String> versions) {
        this.operatingSystemSupport.add(new OperatingSystemSupport(operatingSystem, versions));
    }
    
    public List<ServiceTemplateComponent> getClasses() {
        return classes;
    }

    public void setClasses(List<ServiceTemplateComponent> classes) {
        this.classes = classes;
    }

    public List<ServiceTemplateComponent> getTypes() {
        return types;
    }

    public void setTypes(List<ServiceTemplateComponent> types) {
        this.types = types;
    }

    public List<ServiceTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<ServiceTemplate> templates) {
        this.templates = templates;
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        if (!(obj instanceof AddOnModule)) {
            return false;
        }
        AddOnModule other = (AddOnModule) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
