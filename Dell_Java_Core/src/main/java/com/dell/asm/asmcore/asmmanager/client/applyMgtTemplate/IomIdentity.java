package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "IomIdentity")
public class IomIdentity {
    
    String hostName;
    String svcTag;

    public String getSvcTag() {
        return svcTag;
    }

    public void setSvcTag(String svcTag) {
        this.svcTag = svcTag;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
  
}
