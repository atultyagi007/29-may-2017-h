package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name = "ChassisUser")
public class ChassisUser {

    public enum ChassisUserRole {
    	ADMINISTRATOR("ADMINISTRATOR"), POWER_USER("POWER_USER"), GUEST("GUEST"), CUSTOM("CUSTOM"), NONE("NONE");
        
        private final String value;

        private ChassisUserRole(String newValue) {
            value = newValue;
        }

        public String value() {
            return value;
        }

        public static ChassisUserRole fromValue(String newValue) {
            for (ChassisUserRole candidate : ChassisUserRole.values()) {
                if (candidate.value.equals(newValue)) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException(newValue);
        }
    }
    
	private boolean enabled;
	private String userName;
	private String password;
	private int index;
	private ChassisUserRole role = ChassisUserRole.NONE;
	
	public ChassisUser() {	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String iDracUserName) {
		this.userName = iDracUserName;
	}

	public String getPassword() {
		return (password);
	}

	public void setPassword(String iDracPassword) {
		this.password = iDracPassword;
	}

	public ChassisUserRole getUserRole() {
		return role;
	}

	public void setUserRole(ChassisUserRole role) {
		this.role = role;
	}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
	
}
