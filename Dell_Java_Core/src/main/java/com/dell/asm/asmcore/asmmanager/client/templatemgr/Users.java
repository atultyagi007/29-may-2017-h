package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name = "Users")
public class Users {

	List<ChassisUser> chassisUsers;
	List<ServerUser> serverUsers;
	
	public List<ChassisUser> getChassisUsers() {
		return chassisUsers;
	}
	public void setChassisUsers(List<ChassisUser> chassisUsers) {
		this.chassisUsers = chassisUsers;
	}
	public List<ServerUser> getServerUsers() {
		return serverUsers;
	}
	public void setServerUsers(List<ServerUser> serverUsers) {
		this.serverUsers = serverUsers;
	}
	
	
}
