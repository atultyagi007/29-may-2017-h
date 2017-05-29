package com.dell.asm.asmcore.asmmanager.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.dell.asm.asmcore.asmmanager.client.setting.Setting;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;

@Entity
@Table(name = "setting", schema = "public")
@DynamicUpdate
public class SettingEntity extends BaseEntityAudit {
    private static final long serialVersionUID = 2L;

    @Column(name = "value")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SettingEntity() {
        this(null);
    }

    public SettingEntity(Setting setting) {
        super();
        if (setting != null) {
            this.setValue(setting.getValue());
            this.setCreatedBy(setting.getCreatedBy());
            this.setCreatedDate(setting.getCreatedDate());
            this.setId(setting.getId());
            this.setName(setting.getName());
            this.setUpdatedBy(setting.getUpdatedBy());
            this.setUpdatedDate(setting.getUpdatedDate());
        }
    }

    public Setting getSetting() {
        Setting setting = new Setting();
        setting.setValue(this.getValue());
        setting.setCreatedBy(this.getCreatedBy());
        setting.setCreatedDate(this.getCreatedDate());
        setting.setId(this.getId());
        setting.setName(this.getName());
        setting.setUpdatedBy(this.getUpdatedBy());
        setting.setUpdatedDate(this.getUpdatedDate());

        return setting;
    }

}
