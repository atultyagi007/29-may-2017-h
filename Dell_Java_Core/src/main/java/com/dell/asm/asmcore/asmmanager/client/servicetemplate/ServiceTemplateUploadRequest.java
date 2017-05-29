
/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ServiceTemplateUploadRequest")
@XmlType(name = "ServiceTemplateUploadRequest", propOrder = {
        "encryptionPassword",
        "useEncPwdFromBackup",
        "templateName",
        "category",
        "createCategory",
        "description",
        "allStandardUsers",
        "assignedUsers",
        "manageFirmware",
        "useDefaultCatalog",
        "firmwarePackageId",
        "managePermissions",
        "content",
        "fileData"
})
public class ServiceTemplateUploadRequest {

    String encryptionPassword;
    boolean useEncPwdFromBackup;
    String templateName;
    String category;
    boolean createCategory;
    String description;
    boolean allStandardUsers;
    List<String> assignedUsers;
    boolean manageFirmware;
    boolean useDefaultCatalog;
    String firmwarePackageId;
    boolean managePermissions;
    String content;
    byte[] fileData;

    public ServiceTemplateUploadRequest()
    {

    }

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    public boolean isUseEncPwdFromBackup() {
        return useEncPwdFromBackup;
    }

    public void setUseEncPwdFromBackup(boolean useEncPwdFromBackup) {
        this.useEncPwdFromBackup = useEncPwdFromBackup;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCreateCategory() {
        return createCategory;
    }

    public void setCreateCategory(boolean createCategory) {
        this.createCategory = createCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirmwarePackageId() {
        return firmwarePackageId;
    }

    public void setFirmwarePackageId(String firmwarePackageId) {
        this.firmwarePackageId = firmwarePackageId;
    }

    public boolean isAllStandardUsers() {
        return allStandardUsers;
    }

    public void setAllStandardUsers(boolean allStandardUsers) {
        this.allStandardUsers = allStandardUsers;
    }

    public List<String> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(List<String> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public boolean isManageFirmware() {
        return manageFirmware;
    }

    public void setManageFirmware(boolean manageFirmware) {
        this.manageFirmware = manageFirmware;
    }

    public boolean isManagePermissions() {
        return managePermissions;
    }

    public void setManagePermissions(boolean managePermissions) {
        this.managePermissions = managePermissions;
    }

    public boolean isUseDefaultCatalog() {
        return useDefaultCatalog;
    }

    public void setUseDefaultCatalog(boolean useDefaultCatalog) {
        this.useDefaultCatalog = useDefaultCatalog;
    }
}
