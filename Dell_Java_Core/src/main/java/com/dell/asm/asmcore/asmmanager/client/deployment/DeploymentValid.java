/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.collections.CollectionUtils;

import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.asm.i18n2.EEMILocalizableMessage;

@XmlType(name = "DeploymentValid", propOrder = {
        "valid",
        "messages"
})
public class DeploymentValid {
    private boolean valid;
    private List<AsmDetailedMessage> messages = new ArrayList<AsmDetailedMessage>();

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public DeploymentValid addMessage(final AsmDetailedMessage message) {
        if (CollectionUtils.isEmpty(messages)) {
            messages = new ArrayList<AsmDetailedMessage>();
        }
        messages.add(message);
        return this;
    }
    
    public DeploymentValid addMessage(final EEMILocalizableMessage message) {
        return addMessage(new AsmDetailedMessage(message));
    }
    
    public List<AsmDetailedMessage> getMessages() {
        if (CollectionUtils.isEmpty(messages)) {
            messages = new ArrayList<AsmDetailedMessage>(); 
        }
        //JAXB doesnt like this Collections.unmodifiableList(messages);
        return messages; 
    }

    public void setMessages(List<AsmDetailedMessage> messages) {
        this.messages = messages;
    }

    public static DeploymentValid getDefaultInstance() {
        return new DefaultDeploymentValid();
    }

    protected static class DefaultDeploymentValid extends DeploymentValid {

        private DefaultDeploymentValid() {
            super();
            this.setValid(Boolean.TRUE);
        }
    }
}
