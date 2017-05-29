package com.dell.asm.asmcore.asmmanager.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "template_user_map", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = { "id" }))
public class TemplateUserRefEntity {

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "user_id")
    private long userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String id;

}
