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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

@XmlType(propOrder = { "id", "name", "partitioned", "partitions", "enabled", "redundancy", "nictype", "fqdd" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Interface {
    private static final Logger logger = Logger.getLogger(Interface.class);

    public static final String NIC_2_X_10GB = "2x10Gb";
    public static final String NIC_4_X_10GB = "4x10Gb";
    public static final String NIC_2_X_10GB_2_X_1GB = "2x10Gb,2x1Gb";

    private String id;
    private String name;
    private boolean partitioned;
    private List<Partition> partitions;
    private boolean enabled;
    private boolean redundancy;
    private String nictype;

    public String getFqdd() {
        return fqdd;
    }

    public void setFqdd(String fqdd) {
        this.fqdd = fqdd;
    }

    private String fqdd;

    /**
     * Return the number of ports on the specific nictype. Example nictype
     * strings are "2x10Gb",

     * @param nictype The nic type string
     * @return The number of ports represented by the nictype
     */
    public static int parseNictype(String nictype) {
        if (nictype == null)
            return 0;

        switch (nictype) {
        case NIC_2_X_10GB:
        case NIC_2_X_10GB_2_X_1GB:
            return 2;
        case NIC_4_X_10GB:
            return 4;
        default:
            logger.warn("Unsupported nic type " + nictype + "; returning 2 port");
            return 2;
        }
    }

    public static int getMaxPartitions(String nictype) {
        if (nictype == null)
            return 0;

        switch (nictype) {
        case NIC_2_X_10GB:
            return 4;
        case NIC_2_X_10GB_2_X_1GB:
            return 2;
        case NIC_4_X_10GB:
            return 2;
        default:
            logger.warn("Unsupported nic type " + nictype + "; returning 4 partitions");
            return 4;
        }
    }

    public static String backwardsCompatibleNicType(String nictype) {
        if (nictype == null)
            return null;
        switch (nictype) {
        case "2":
            return NIC_2_X_10GB;
        case "4":
            return NIC_4_X_10GB;
        default:
            return nictype;
        }
    }

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

    public boolean isPartitioned() {
        return partitioned;
    }

    public void setPartitioned(boolean partitioned) {
        this.partitioned = partitioned;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRedundancy() {
        return redundancy;
    }

    public void setRedundancy(boolean redundancy) {
        this.redundancy = redundancy;
    }

    public String getNictype() {
        return backwardsCompatibleNicType(nictype);
    }

    public void setNictype(String nictype) {
        this.nictype = backwardsCompatibleNicType(nictype);
    }

    public int getMaxPartitions() {
        return Interface.getMaxPartitions(nictype);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("partitioned", partitioned)
                .append("partitions", partitions)
                .append("enabled", enabled)
                .append("redundancy", redundancy)
                .append("nictype", nictype)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Interface) {
            if (((Interface) o).getId() == null &&
                    this.getId() != null)
                return false;

            return ((Interface) o).getId().equals(this.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null)
            return 0;
        return this.getId().hashCode();
    }


    /**
     * Massage networks includes cloning interfaces. If that happened with this object, all corresponding partitions
     * will have the same IDs. This method checks if two given interfaces have sane partitions.
     *
     * @param remoteInterface
     * @return
     */
    public boolean isIdenticalInterface(Interface remoteInterface) {
        if (remoteInterface == null)
            return false;

        if (this.isPartitioned() != remoteInterface.isPartitioned())
            return false;

        if (this.isRedundancy() != remoteInterface.isRedundancy())
            return false;

        for (Partition partition : this.getPartitions()) {
            // lookup for the same partition by name (index)
            Partition remotePartition = null;
            for (Partition rmtPart : remoteInterface.getPartitions()) {
                if (partition.getName().equals(rmtPart.getName())) {
                    remotePartition = rmtPart;
                    break;
                }
            }
            // corner case. Can't be true but better be on safe side
            if (remotePartition == null)
                return false;

            if (!remotePartition.getId().equals(partition.getId())) {
                return false;
            }

            // different network set
            if (!StringUtils.join(partition.getNetworks(), ":").equals(
                    StringUtils.join(remotePartition.getNetworks(), ":"))) {
                return false;
            }

            // stop here if the rest of partitions is not used
            if (!this.isPartitioned()) break;
            // Different nic types have different number of partitions but data may include more that should be ignored
            if (partition.getName().equals(Integer.toString(this.getMaxPartitions()))) {
                break;
            }
        }
        return true;
    }
}
