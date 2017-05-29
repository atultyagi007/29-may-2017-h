package com.dell.asm.asmcore.asmmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractAsmManagerTest {

    
    protected static class ServiceTemplateBuilder {
        private final String id;
        private List<ComponentBuilder> components = new ArrayList<ComponentBuilder>();

        public ServiceTemplateBuilder(final String id) {
            this.id = id;
        }
        
        public ServiceTemplateBuilder addComponent(final ComponentBuilder component) {
            components.add(component);
            return this;
        }
        
        public ServiceTemplate build() {
            final ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.setId(id);
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
        private final List<ResourceBuilder> resources = new ArrayList<ResourceBuilder>();
        private ServiceTemplateComponent.ServiceTemplateComponentType type;
        private List<Pair<String, String>> associatedComponents = new ArrayList<>();
        
        public ComponentBuilder(final String id) {
            this.id = id;
        }
        
        public ComponentBuilder addComponentId(final String componentId) {
            this.componentId = componentId;
            return this;
        }

        public ComponentBuilder addResource(final ResourceBuilder resource) {
            resources.add(resource);
            return this;
        }

        public ComponentBuilder addComponentType(final ServiceTemplateComponent.ServiceTemplateComponentType componentType) {
            this.type = componentType;
            return this;
        }

        public ComponentBuilder addAssociatedComponent(final String componentId, final String componentName) {
            associatedComponents.add(Pair.of(componentId, componentName));
            return this;
        }

        public ServiceTemplateComponent build() {
            final ServiceTemplateComponent component = new ServiceTemplateComponent();
            component.setId(id);
            component.setComponentID(componentId);
            component.setType(type);
            if (component.getResources() == null) {
                component.setResources(new ArrayList<ServiceTemplateCategory>());
            }
            for (final ResourceBuilder resource : resources) {
                component.getResources().add(resource.build());
            }
            for (Pair<String, String> aComponent : associatedComponents) {
                component.addAssociatedComponentName(aComponent.getLeft(), aComponent.getRight());
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
