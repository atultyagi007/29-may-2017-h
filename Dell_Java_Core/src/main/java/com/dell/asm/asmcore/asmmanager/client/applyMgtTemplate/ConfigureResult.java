package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ConfigureResult")
public class ConfigureResult {
    
    private String jobId;
    private String statusMsg;
    
    public String getJobId() {
        return jobId;
    }
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    public String getStatusMsg() {
        return statusMsg;
    }
    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }
   

}
