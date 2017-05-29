package com.dell.asm.asmcore.asmmanager.client.serverprofilemgr;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.server.client.identity.ServerIdentity;

@XmlRootElement(name = "ServerProfile")
public class ServerProfile {
	
	String jrafProfileName;
	String jrafTemplateName;
	String jrafProfileDisplayName;
	
	/* for now expose the ServerIdentity from serverRA
	 * we might need to create our own class and remove
	 * fields that are not needed by GUI
	 */
	ServerIdentity serverIdentity;

	public String getJrafProfileName() {
		return jrafProfileName;
	}

	public void setJrafProfileName(String jrafProfileName) {
		this.jrafProfileName = jrafProfileName;
	}

	public String getJrafTemplateName() {
		return jrafTemplateName;
	}

	public void setJrafTemplateName(String jrafTemplateName) {
		this.jrafTemplateName = jrafTemplateName;
	}

	public ServerIdentity getServerIdentity() {
		return serverIdentity;
	}

	public void setServerIdentity(ServerIdentity serverIdentity) {
		this.serverIdentity = serverIdentity;
	}

	public String getJrafProfileDisplayName() {
		return jrafProfileDisplayName;
	}

	public void setJrafProfileDisplayName(String jrafProfileDisplayName) {
		this.jrafProfileDisplayName = jrafProfileDisplayName;
	}
	
	

}
