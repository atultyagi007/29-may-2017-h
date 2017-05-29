package com.dell.asm.asmcore.asmmanager.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentNamesType;

@Entity
@Table(name = "deployment_names_map", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = { "type", "name" }, name = "deployment_names_type_name_constraint"))
public class DeploymentNamesRefEntity {

    @Id
    @Column(name = "id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String id;

    @Column(name = "deployment_id")
    private String deploymentId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private DeploymentNamesType type;

    @Column(name = "name")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public DeploymentNamesType getType() {
        return type;
    }

    public void setType(DeploymentNamesType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeploymentNamesRefEntity that = (DeploymentNamesRefEntity) o;

        if (getType() != that.getType()) return false;
        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }


}
