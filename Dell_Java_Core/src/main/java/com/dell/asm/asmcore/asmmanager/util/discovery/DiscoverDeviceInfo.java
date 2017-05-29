package com.dell.asm.asmcore.asmmanager.util.discovery;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DiscoverDeviceInfo {

	private String startIp;
	
	private String endIp;
	
	private String user;
	
	private String password;


	public DiscoverDeviceInfo() {
		
	}

	public DiscoverDeviceInfo(String startIp,  String endIp, String user, String password) {
		this.startIp = startIp;
		this.endIp = endIp;
		this.user = user;
		this.password = password;
	}

	public DiscoverDeviceInfo(String startIp, String user, String password) {
		this.startIp = startIp;
		this.user = user;
		this.password = password;
	}


	public String getStartIp() {
		return startIp;
	}

	public void setStartIp(String startIp) {
		this.startIp = startIp;
	}

	public String getEndIp() {
		return endIp;
	}

	public void setEndIp(String endIp) {
		this.endIp = endIp;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
