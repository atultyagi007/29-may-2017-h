package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ServerUser")
public class ServerUser {

    public enum ServerUserRole {
        ADMINISTRATOR("ADMINISTRATOR"), OPERATOR("OPERATOR"), READ_ONLY("READ_ONLY"), NONE("NONE");

        private final String value;

        private ServerUserRole(String newValue) {
            value = newValue;
        }

        public String value() {
            return value;
        }

        public static ServerUserRole fromValue(String newValue) {
            for (ServerUserRole candidate : ServerUserRole.values()) {
                if (candidate.value.equals(newValue)) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException(newValue);
        }
    }

    @XmlType(name = "TemplateIPMIUserRole")
    public enum IPMIUserRole {
        ADMINISTRATOR("ADMINISTRATOR"), OPERATOR("OPERATOR"), USER("USER"), NONE("NONE");
        private final String value;

        private IPMIUserRole(String newValue) {
            value = newValue;
        }

        public String value() {
            return value;
        }

        public static IPMIUserRole fromValue(String newValue) {
            for (IPMIUserRole candidate : IPMIUserRole.values()) {
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
    private ServerUserRole role = ServerUserRole.NONE;
    private IPMIUserRole ipmiUserRole = IPMIUserRole.NONE;
    private boolean ipmiSOLEnabled;
    private int index;
    
    public ServerUser() {
    }

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

    public ServerUserRole getUserRole() {
        return role;
    }

    public void setUserRole(ServerUserRole role) {
        this.role = role;
    }

    public ServerUserRole getRole() {
        return role;
    }

    public void setRole(ServerUserRole role) {
        this.role = role;
    }

    public IPMIUserRole getIpmiUserRole() {
        return ipmiUserRole;
    }

    public void setIpmiUserRole(IPMIUserRole ipmiUserRole) {
        this.ipmiUserRole = ipmiUserRole;
    }

    @XmlElement(name = "ipmiROLEnabled")
    public boolean isIpmiSOLEnabled() {
        return ipmiSOLEnabled;
    }

    public void setIpmiSOLEnabled(boolean ipmiSOLEnabled) {
        this.ipmiSOLEnabled = ipmiSOLEnabled;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
