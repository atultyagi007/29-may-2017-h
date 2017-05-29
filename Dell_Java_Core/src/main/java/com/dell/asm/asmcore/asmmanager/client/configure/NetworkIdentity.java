package com.dell.asm.asmcore.asmmanager.client.configure;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "NetworkIdentity")
public class NetworkIdentity {

    public AddressingMode getAddressingMode() {
        return addressingMode;
    }

    public void setAddressingMode(AddressingMode addressingMode) {
        this.addressingMode = addressingMode;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    private AddressingMode addressingMode;
    private String networkId;

}
