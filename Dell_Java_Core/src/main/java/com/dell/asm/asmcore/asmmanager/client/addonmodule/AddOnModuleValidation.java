package com.dell.asm.asmcore.asmmanager.client.addonmodule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class AddOnModuleValidation {
    
    private final List<String> missingRequiredFields;
    private final List<String> invalidFieldValues;
    private final List<String> missingRequiredFieldValues;
    private final List<String> invalidVersionValues;
    private final List<String> duplicateComponentNames;
    private final List<String> duplicateComponentIds;
    
    private AddOnModuleValidation(final Builder builder) {
        this.missingRequiredFields = builder.missingRequiredFields;
        this.invalidFieldValues = builder.invalidFieldValues;
        this.missingRequiredFieldValues = builder.missingRequiredFieldValues;
        this.invalidVersionValues = builder.invalidVersionValues;
        this.duplicateComponentNames = builder.duplicateComponentNames;
        this.duplicateComponentIds = builder.duplicateComponentIds;
    }
    
    public List<String> getMissingRequiredFields() {
        return missingRequiredFields;
    }
    
    public List<String> getInvalidFieldValues() {
        return invalidFieldValues;
    }
    
    public List<String> getMissingRequiredFieldValues() {
        return missingRequiredFieldValues;
    }

    public List<String> getInvalidVersionValues() {
        return invalidVersionValues;
    }

    public List<String> getDuplicateComponentNames() { return duplicateComponentNames; }

    public List<String> getDuplicateComponentIds() {
        return duplicateComponentIds;
    }

    public boolean hasValidationErrors() {
        return CollectionUtils.isNotEmpty(missingRequiredFields) 
                || CollectionUtils.isNotEmpty(invalidFieldValues)
                || CollectionUtils.isNotEmpty(missingRequiredFieldValues)
                || CollectionUtils.isNotEmpty(invalidVersionValues)
                || CollectionUtils.isNotEmpty(duplicateComponentNames)
                || CollectionUtils.isNotEmpty(duplicateComponentIds);
    }
    
    public static class Builder {
        
        private final List<String> missingRequiredFields = new ArrayList<String>();
        private final List<String> invalidFieldValues = new ArrayList<String>();
        private final List<String> missingRequiredFieldValues = new ArrayList<String>();
        private final List<String> invalidVersionValues = new ArrayList<String>();
        private final List<String> duplicateComponentNames = new ArrayList<>();
        private final List<String> duplicateComponentIds = new ArrayList<>();
        
        public Builder missingRequiredField(final String name) {
            missingRequiredFields.add(name);
            return this;
        }
        
        public Builder invalidFieldValue(final String name) {
            invalidFieldValues.add(name);
            return this;
        }
        
        public Builder missingRequiredFieldValue(final String name, final String requiredValue) {
            missingRequiredFieldValues.add(name + "=" + requiredValue);
            return this;
        }
        
        public Builder invalidVersionValue(final String name, final String version) {
            invalidVersionValues.add(name + "=" + version);
            return this;
        }

        public Builder duplicateComponentNames(final String name) {
            duplicateComponentNames.add(name);
            return this;
        }

        public Builder duplicateComponentIds(final String id) {
            duplicateComponentIds.add(id);
            return this;
        }
        
        public AddOnModuleValidation build() {
            return new AddOnModuleValidation(this);
        }
    }
}
