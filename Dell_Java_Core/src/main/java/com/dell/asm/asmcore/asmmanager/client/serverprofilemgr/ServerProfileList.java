package com.dell.asm.asmcore.asmmanager.client.serverprofilemgr;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ServerProfileList")
public class ServerProfileList {
	
	 private List<ServerProfile> serverProfileList;
	    
	 private int totalCount;

	@XmlElement(name = "ServerProfile")
	public List<ServerProfile> getServerProfileList() {
		return serverProfileList;
	}

	public void setServerProfileList(List<ServerProfile> serverProfileList) {
		this.serverProfileList = serverProfileList;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	 
	 

}
