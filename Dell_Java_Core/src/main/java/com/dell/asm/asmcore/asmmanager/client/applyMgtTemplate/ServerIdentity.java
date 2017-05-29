package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ServerIdentity")
public class ServerIdentity {
    
    String idracDnsName;
    String serviceTag;
    

    public String getIdracDnsName() {
        return idracDnsName;
    }

    public void setIdracDnsName(String idracDnsName) {
        this.idracDnsName = idracDnsName;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

}
