package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "Credentials")
public class Credentials {

	String chassisCredentialRef;
	String serverCredentialRef;
	String iomCredentialRef;
	
	public String getChassisCredentialRef() {
		return chassisCredentialRef;
	}
	public void setChassisCredentialRef(String chassisCredentialRef) {
		this.chassisCredentialRef = chassisCredentialRef;
	}
	public String getServerCredentialRef() {
		return serverCredentialRef;
	}
	public void setServerCredentialRef(String serverCredentialRef) {
		this.serverCredentialRef = serverCredentialRef;
	}
	public String getIomCredentialRef() {
		return iomCredentialRef;
	}
	public void setIomCredentialRef(String iomCredentialRef) {
		this.iomCredentialRef = iomCredentialRef;
	}
	
	
}
