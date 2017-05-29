/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.networkconfiguration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlType(propOrder = { "id", "name", "redundancy", "enabled", "usedforfc", "partitioned", "nictype", "interfaces", "fabrictype" })
@XmlAccessorType(XmlAccessType.FIELD)
/**
 * Fabric is actually a Card.
 * networking configuration -> card -> interfaces -> partitions
 */
public class Fabric {
    private String id;
    private String name;
    private boolean redundancy;
    private boolean enabled;
    private boolean usedforfc;
    private boolean partitioned;
    private List<Interface> interfaces;
    private String nictype;

    @XmlTransient
    private boolean card_index;

    public static String ETHERNET_TYPE = "ethernet";
    public static String FC_TYPE = "fc";

    /**
     * Accepted values are "ethernet", "fc"
     * @return
     */
    public String getFabrictype() {
        return fabrictype;
    }

    public void setFabrictype(String fabrictype) {
        this.fabrictype = fabrictype;
    }

    private String fabrictype;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRedundancy() {
        return redundancy;
    }

    public void setRedundancy(boolean redundancy) {
        this.redundancy = redundancy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Interface> interfaces) {
        this.interfaces = interfaces;
    }

    public String getNictype() {
        return Interface.backwardsCompatibleNicType(nictype);
    }

    public void setNictype(String nictype) {
        this.nictype = Interface.backwardsCompatibleNicType(nictype);
    }

    public int getNPorts() {
        return Interface.parseNictype(nictype);
    }

    public int getMaxPartitions() {
        return Interface.getMaxPartitions(nictype);
    }

    public boolean isPartitioned() {
        return partitioned;
    }

    public void setPartitioned(boolean partitioned) {
        this.partitioned = partitioned;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("redundancy", redundancy)
                .append("enabled", enabled)
                .append("interfaces", interfaces)
                .toString();
    }

    /**
     * @deprecated Use fabrictype instead
     * @return
     */
    public boolean isUsedforfc() {
        return usedforfc;
    }

    /**
     * @deprecated
     * @param usedforfc
     */
    public void setUsedforfc(boolean usedforfc) {
        this.usedforfc = usedforfc;
    }

    public String getNictypeSource() {
        return nictype;
    }

}
