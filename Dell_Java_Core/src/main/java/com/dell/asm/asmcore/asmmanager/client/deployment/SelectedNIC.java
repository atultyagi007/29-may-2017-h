/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SelectedNIC")
public class SelectedNIC {

    public Integer getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(Integer cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public String getFqdd() {
        return fqdd;
    }

    public void setFqdd(String fqdd) {
        this.fqdd = fqdd;
    }

    private Integer cardNumber;
    private String id;
    private Integer portNumber;
    private String fqdd;

    public SelectedNIC(){
    }

    public SelectedNIC(String interfaceKey, String portNum, String fqdd) throws NumberFormatException{
        this.portNumber = Integer.parseInt(portNum);
        this.fqdd = fqdd;

        String[] keys = interfaceKey.split(":");
        this.cardNumber = Integer.parseInt(keys[0]);
        this.id = keys[1];
    }

    @Override
    public String toString() {
        return fqdd;
    }
}
