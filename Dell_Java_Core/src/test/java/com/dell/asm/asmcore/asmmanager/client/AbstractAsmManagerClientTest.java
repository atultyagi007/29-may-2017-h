package com.dell.asm.asmcore.asmmanager.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateValid;

public abstract class AbstractAsmManagerClientTest {

    protected static class ServiceTemplateBuilder {
        private final String id;
        private boolean templateValid;
        private List<ComponentBuilder> components = new ArrayList<ComponentBuilder>();
        
        public ServiceTemplateBuilder(final String id) {
            this.id = id;
        }
        
        public ServiceTemplateBuilder addTemplateValid(final boolean templateValid) {
            this.templateValid = templateValid;
            return this;
        }

        public ServiceTemplateBuilder addComponent(final ComponentBuilder component) {
            components.add(component);
            return this;
        }
        
        public ServiceTemplate build() {
            final ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.setId(id);
            serviceTemplate.setTemplateValid(ServiceTemplateValid.getDefaultInstance());
            serviceTemplate.getTemplateValid().setValid(templateValid);
            if (serviceTemplate.getComponents() == null) {
                serviceTemplate.setComponents(new ArrayList<ServiceTemplateComponent>());
            }
            for (final ComponentBuilder component : components) {
                serviceTemplate.getComponents().add(component.build());
            }
            return serviceTemplate;
        }
    }
    
    protected static class ComponentBuilder {
        private final String id;
        private String componentId;
        private boolean componentValid;
        private final List<ResourceBuilder> resources = new ArrayList<ResourceBuilder>();
        
        public ComponentBuilder(final String id) {
            this.id = id;
        }
        
        public ComponentBuilder addComponentValid(final boolean componentValid) {
            this.componentValid = componentValid;
            return this;
        }
        
        public ComponentBuilder addComponentId(final String componentId) {
            this.componentId = componentId;
            return this;
        }
        
        public ComponentBuilder addResource(final ResourceBuilder resource) {
            resources.add(resource);
            return this;
        }
        
        public ServiceTemplateComponent build() {
            final ServiceTemplateComponent component = new ServiceTemplateComponent();
            component.setId(id);
            component.setComponentValid(ServiceTemplateValid.getDefaultInstance());
            component.getComponentValid().setValid(componentValid);
            component.setComponentID(componentId);
            if (component.getResources() == null) {
                component.setResources(new ArrayList<ServiceTemplateCategory>());
            }
            for (final ResourceBuilder resource : resources) {
                component.getResources().add(resource.build());
            }
            return component;
        }
    }
    
    protected static class ResourceBuilder {
        private String id;
        private final List<ParameterBuilder> parameters = new ArrayList<ParameterBuilder>();
        
        public ResourceBuilder(final String id) {
            this.id = id;
        }
        
        public ResourceBuilder addParameter(final ParameterBuilder parameter) {
            parameters.add(parameter);
            return this;
        }
        
        public ServiceTemplateCategory build() {
            final ServiceTemplateCategory resource = new ServiceTemplateCategory();
            resource.setId(id);
            if (resource.getParameters() == null) {
                resource.setParameters(new ArrayList<ServiceTemplateSetting>());
            }
            for (final ParameterBuilder parameter : parameters) {
                resource.getParameters().add(parameter.build());
            }
            return resource;
        }
    }
    
    protected static class ParameterBuilder {
        private final String id;
        private String displayName = StringUtils.EMPTY;
        private String value = StringUtils.EMPTY;
        private boolean hideFromTemplate = Boolean.FALSE;
        private boolean required = Boolean.FALSE;
        private String dependencyTarget = StringUtils.EMPTY;
        private String dependencyValue = StringUtils.EMPTY;
        private ServiceTemplateSettingType type;
        
        public ParameterBuilder(final String id) {
            this.id = id;
        }
        
        public ParameterBuilder addDisplayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public ParameterBuilder addValue(final String value) {
            this.value = value;
            return this;
        }
        
        public ParameterBuilder addHideFromTemplate(final boolean hideFromTemplate) {
            this.hideFromTemplate = hideFromTemplate;
            return this;
        }
        
        public ParameterBuilder addRequired(final boolean required) {
            this.required = required;
            return this;
        }
        
        public ParameterBuilder addDependency(final String dependencyTarget, final String dependencyValue) {
            this.dependencyTarget = dependencyTarget;
            this.dependencyValue = dependencyValue;
            return this;
        }       
        
        public ParameterBuilder addType(final ServiceTemplateSettingType type) {
            this.type = type;
            return this;
        }
        
        public ServiceTemplateSetting build() {
            final ServiceTemplateSetting parameter = new ServiceTemplateSetting();
            parameter.setId(id);
            parameter.setDisplayName(displayName);
            parameter.setValue(value);
            parameter.setHideFromTemplate(hideFromTemplate);
            parameter.setRequired(required);
            parameter.setDependencyTarget(dependencyTarget);
            parameter.setDependencyValue(dependencyValue);
            parameter.setType(type);
            return parameter;
        }
    }
}
