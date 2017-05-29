/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.collections.CollectionUtils;

import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.asm.i18n2.EEMILocalizableMessage;

@XmlType(name = "ServiceTemplateValid", propOrder = { 
        "valid",
        "messages"
})
public class ServiceTemplateValid {
    private boolean valid;
    private List<AsmDetailedMessage> messages = new ArrayList<AsmDetailedMessage>();

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public ServiceTemplateValid addMessage(final AsmDetailedMessage message) {
        if (CollectionUtils.isEmpty(messages)) {
            messages = new ArrayList<AsmDetailedMessage>();
        }
        messages.add(message);
        return this;
    }
    
    public ServiceTemplateValid addMessage(final EEMILocalizableMessage message) {
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

    public static ServiceTemplateValid getDefaultInstance() {
        return new DefaultServiceTemplateValid();
    }

    protected static class DefaultServiceTemplateValid extends ServiceTemplateValid {

        private DefaultServiceTemplateValid() {
            super();
            this.setValid(Boolean.TRUE);
        }
    }
}
