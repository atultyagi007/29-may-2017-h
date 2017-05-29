package com.dell.asm.asmcore.asmmanager.client.pupetmodule;

import com.dell.pg.orion.common.print.Dump;

/**
 * Created with IntelliJ IDEA.
 * User: J_Fowler
 * Date: 12/16/13
 * Time: 4:04 PM
 */
//@XmlRootElement(name = "ServerDiscoveryRequest")
//@ApiModel(value = "Puppet Device Discovery Request", description = "Captures properties needed to perform a device discover via a puppet module  based on IP address")
public class PuppetDiscoveryRequest {
    public PuppetDiscoveryRequest() {
        super();
    }

    public PuppetDiscoveryRequest(String ipAddress, String puppetModuleName,
                                  String credentialId, String connectType) {
        super();
        this.ipAddress = ipAddress;
        this.puppetModuleName = puppetModuleName;
        this.credentialId = credentialId;
        this.connectType = connectType;
    }

    //@ApiModelProperty(value = "IDRAC IP address", notes = "No format checking performed", required = true)
    private String ipAddress;
    //     @ApiModelProperty(value = "Credential Id", required = true)
    private String credentialId;

    private String puppetModuleName;

    private String connectType;

    private String existingRefId;

    private String scriptPath = null;
    
    private String osIpAddress = null;
    
    private boolean quickDiscovery = false;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getPuppetModuleName() {
        return puppetModuleName;
    }

    public void setPuppetModuleName(String puppetModuleName) {
        this.puppetModuleName = puppetModuleName;
    }

    public String getConnectType() {
        return connectType;
    }

    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }
    
	/**
	 * @return the osIpAddress
	 */
	public String getOsIpAddress() {
		return osIpAddress;
	}

	/**
	 * @param osIpAddress the osIpAddress to set
	 */
	public void setOsIpAddress(String osIpAddress) {
		this.osIpAddress = osIpAddress;
	}

	public String getExistingRefId() { return existingRefId; }

    public void setExistingRefId(String existingRefId) { this.existingRefId = existingRefId; }

    public String getScriptPath() { return scriptPath; }

    public void setScriptPath(String scriptPath) { this.scriptPath = scriptPath; }

    public boolean isQuickDiscovery() {
        return quickDiscovery;
    }

    public void setQuickDiscovery(boolean quickDiscovery) {
        this.quickDiscovery = quickDiscovery;
    }

    // Dump contents.
    @Override
    public String toString() {
        return Dump.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PuppetDiscoveryRequest that = (PuppetDiscoveryRequest) o;

        if (connectType != null ? !connectType.equals(that.connectType) : that.connectType != null)
            return false;
        if (credentialId != null ? !credentialId.equals(that.credentialId) : that.credentialId != null)
            return false;
        if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null)
            return false;
        if (puppetModuleName != null ? !puppetModuleName.equals(that.puppetModuleName) : that.puppetModuleName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ipAddress != null ? ipAddress.hashCode() : 0;
        result = 31 * result + (credentialId != null ? credentialId.hashCode() : 0);
        result = 31 * result + (puppetModuleName != null ? puppetModuleName.hashCode() : 0);
        result = 31 * result + (connectType != null ? connectType.hashCode() : 0);
        return result;
    }
}
