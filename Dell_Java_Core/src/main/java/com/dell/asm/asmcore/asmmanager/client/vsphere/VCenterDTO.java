package com.dell.asm.asmcore.asmmanager.client.vsphere;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.dell.asm.rest.common.model.Link;
import com.dell.pg.jraf.client.profmgr.JrafDeviceRef;

@XmlRootElement(name = "vcenter")
@XmlType(name = "VCenter", propOrder = {
        "link",
        "ref",
        "id",
        "uuid",
        "name",
        "ipAddress",
        "createdBy",
        "createdDate",
        "updatedBy",
        "updatedDate",
        "root",
})
public class VCenterDTO {
    private Link link;

    private String id;
    private String uuid;
    private String ipAddress;
    private String name;
    private ManagedObjectDTO root;
    private JrafDeviceRef ref = new JrafDeviceRef(id, VcenterUtils.VCENTER_REF_TYPE,
            name, VcenterUtils.DEVICE_TYPE);
    private String createdBy;
    private Date createdDate;
    private String updatedBy;
    private Date updatedDate;

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.ref.setRefId(id);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.ref.setDisplayName(name);
    }

    @XmlElementRef
    public ManagedObjectDTO getRoot() {
        return root;
    }

    public void setRoot(ManagedObjectDTO root) {
        this.root = root;
    }

    public JrafDeviceRef getRef() {
        return ref;
    }

    public void setRef(JrafDeviceRef ref) {
        this.ref = ref;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    private static <T extends ManagedObjectDTO> void getManagedObjectsHelper(Class<T> klazz,
                                                                             ManagedObjectDTO object,
                                                                             Set<T> ret) {        
        if (klazz.isAssignableFrom(object.getClass())) {
            T cast = klazz.cast(object);
            ret.add(cast);
        }

        for (ManagedObjectDTO child : object.getChildren()) {
            getManagedObjectsHelper(klazz, child, ret);
        }
        
    }

    public <T extends ManagedObjectDTO> Set<T> getManagedObjects(Class<T> klazz) {
        Set<T> ret = new HashSet<>();
        if (root != null) {
            getManagedObjectsHelper(klazz, root, ret);
        }
        return ret;
    }
    
    public <T extends ManagedObjectDTO> int getManagedObjectsCount(Class<T> klazz) {
        Set<T> ret = new HashSet<>();
        if (root != null) {
            getManagedObjectsHelper(klazz, root, ret);
        }
        return ret.size();
    }    

    // Helper methods
    public int getNDatacenters() {
        return getManagedObjectsCount(DatacenterDTO.class);
    }

    public int getNClusters() {
        return getManagedObjectsCount(ClusterDTO.class);
    }

    public int getNResourcePools() {
        return getManagedObjects(ResourcePoolDTO.class).size();
    }

    public int getNHosts() {
        return getManagedObjectsCount(HostDTO.class);
    }

    public int getNVirtualMachines() {
        return getManagedObjects(VirtualMachineDTO.class).size();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("uuid", uuid)
                .append("ipAddress", ipAddress)
                .append("name", name)
                .append("root", root)
                .toString();
    }
}
