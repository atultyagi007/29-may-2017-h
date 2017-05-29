/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.dell.pg.asm.identitypool.api.common.model.NetworkType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "name",
    "description",
    "type",
    "vlanId",
    "_static",
    "staticNetworkConfiguration",
})
@XmlRootElement(name = "Network")
public class Network
    implements Serializable
{

    private final static long serialVersionUID = 100L;
    protected String id;
    protected String name;
    protected String description;
    protected NetworkType type;
    protected Integer vlanId;
    @XmlElement(name = "static")
    protected boolean _static;
    protected StaticNetworkConfiguration staticNetworkConfiguration;

    public Network(com.dell.pg.asm.identitypool.api.network.model.Network n){
        if (n != null) {
            this.id = n.getId();
            this.name = n.getName();
            this.description = n.getDescription();
            this.type = n.getType();
            this.vlanId = n.getVlanId();
            this.setStatic(n.isStatic());

            if (n.getStaticNetworkConfiguration() != null) {
                this.staticNetworkConfiguration = new StaticNetworkConfiguration();
                this.staticNetworkConfiguration.setDnsSuffix(n.getStaticNetworkConfiguration().getDnsSuffix());
                this.staticNetworkConfiguration.setGateway(n.getStaticNetworkConfiguration().getGateway());
                this.staticNetworkConfiguration.setIpAddress(n.getStaticNetworkConfiguration().getIpAddress());
                this.staticNetworkConfiguration.setPrimaryDns(n.getStaticNetworkConfiguration().getPrimaryDns());
                this.staticNetworkConfiguration.setSecondaryDns(n.getStaticNetworkConfiguration().getSecondaryDns());
                this.staticNetworkConfiguration.setSubnet(n.getStaticNetworkConfiguration().getSubnet());

                for (com.dell.pg.asm.identitypool.api.network.model.IpRange ipRange : n.getStaticNetworkConfiguration().getIpRange()) {
                    com.dell.asm.asmcore.asmmanager.client.servicetemplate.IpRange newIpRange = new com.dell.asm.asmcore.asmmanager.client.servicetemplate.IpRange();
                    newIpRange.setId(ipRange.getId());
                    newIpRange.setStartingIp(ipRange.getStartingIp());
                    newIpRange.setEndingIp(ipRange.getEndingIp());
                    this.staticNetworkConfiguration.getIpRange().add(newIpRange);
                }
            }
        }
    }

    /**
     * Copy Constructor
     * @param network
     */
    public Network(Network network) {
        if (network != null) {
            setId(network.getId());
            setName(network.getName());
            setDescription(network.getDescription());
            setType(network.getType());
            setVlanId(network.getVlanId());
            setStatic(network.isStatic());
            if (isStatic()) {
                setStaticNetworkConfiguration(new StaticNetworkConfiguration());
                //add ip class
                getStaticNetworkConfiguration().setDnsSuffix(network.getStaticNetworkConfiguration().getDnsSuffix());
                getStaticNetworkConfiguration().setGateway(network.getStaticNetworkConfiguration().getGateway());
                getStaticNetworkConfiguration().setPrimaryDns(network.getStaticNetworkConfiguration().getPrimaryDns());
                getStaticNetworkConfiguration().setSecondaryDns(network.getStaticNetworkConfiguration().getSecondaryDns());
                getStaticNetworkConfiguration().setSubnet(network.getStaticNetworkConfiguration().getSubnet());
                for(IpRange ipRange : network.getStaticNetworkConfiguration().getIpRange()){
                    IpRange newIpRange = new IpRange();
                    newIpRange.setId(ipRange.getId());
                    newIpRange.setStartingIp(ipRange.getStartingIp());
                    newIpRange.setEndingIp(ipRange.getEndingIp());
                    getStaticNetworkConfiguration().getIpRange().add(newIpRange);
                }
            }
        }
    }
    
    public Network() {
		// TODO Auto-generated constructor stub
	}

	/**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkType }
     *     
     */
    public NetworkType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkType }
     *     
     */
    public void setType(NetworkType value) {
        this.type = value;
    }

    /**
     * Gets the value of the vlanId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVlanId() {
        return vlanId;
    }

    /**
     * Sets the value of the vlanId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVlanId(Integer value) {
        this.vlanId = value;
    }

    /**
     * Gets the value of the static property.
     * 
     */
    public boolean isStatic() {
        return _static;
    }

    /**
     * Sets the value of the static property.
     * 
     */
    public void setStatic(boolean value) {
        this._static = value;
    }

    /**
     * Gets the value of the staticNetworkConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link StaticNetworkConfiguration }
     *     
     */
    public StaticNetworkConfiguration getStaticNetworkConfiguration() {
        return staticNetworkConfiguration;
    }

    /**
     * Sets the value of the staticNetworkConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link StaticNetworkConfiguration }
     *     
     */
    public void setStaticNetworkConfiguration(StaticNetworkConfiguration value) {
        this.staticNetworkConfiguration = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Network network = (Network) o;

        if (id != null ? !id.equals(network.id) : network.id != null)
            return false;
        if (name != null ? !name.equals(network.name) : network.name != null)
            return false;

        if (type != network.type) return false;
        if (vlanId != null ? !vlanId.equals(network.vlanId) : network.vlanId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (vlanId != null ? vlanId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Network{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", vlanId=" + vlanId +
                ", staticNetworkConfiguration=" + staticNetworkConfiguration +
                '}';
    }
}
