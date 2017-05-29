/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader;

import com.dell.pg.asm.identitypool.api.network.INetworkService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateUserRefEntity;
import com.dell.asm.asmcore.user.model.User;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.dell.pg.orion.security.encryption.IEncryptedString;
import com.google.common.collect.ImmutableSet;

public class ServiceTemplateUtilTest {

    private EncryptionDAO dao;
    private ServiceTemplateUtil util;

    @Before
    public void setUp() {
        dao = mock(EncryptionDAO.class);
        util = mockServiceTemplateUtil(dao, mock(INetworkService.class));
    }

    private ServiceTemplateSetting buildParameter(String id, ServiceTemplateSettingType type, String val) {
        ServiceTemplateSetting ret = new ServiceTemplateSetting();
        ret.setId(id);
        ret.setDisplayName("Name " + id);
        ret.setValue(val);
        ret.setType(type);
        return ret;
    }

    private ServiceTemplateCategory buildResource(String id) {
        ServiceTemplateCategory ret = new ServiceTemplateCategory();
        ret.setId(id);
        ret.setDisplayName("Name " + id);
        return ret;
    }

    private ServiceTemplateComponent buildComponent(String id) {
        ServiceTemplateComponent ret = new ServiceTemplateComponent();
        ret.setId(id);
        ret.setComponentID("component-id-" + id);
        return ret;
    }

    private ServiceTemplate buildServiceTemplate() {
        ServiceTemplate template = new ServiceTemplate();
        ServiceTemplateComponent comp1 = buildComponent("component-1");
        ServiceTemplateCategory resource1 = buildResource("resource-1");
        ServiceTemplateSetting param1 = buildParameter("param-1", ServiceTemplateSettingType.STRING, "foo");
        ServiceTemplateSetting param2 = buildParameter("password1", ServiceTemplateSettingType.PASSWORD, "p@ssword1");
        ServiceTemplateSetting param3 = buildParameter("password2", ServiceTemplateSettingType.PASSWORD, "p@ssword2");
        resource1.getParameters().add(param1);
        resource1.getParameters().add(param2);
        resource1.getParameters().add(param3);
        comp1.getResources().add(resource1);
        template.getComponents().add(comp1);

        ServiceTemplateComponent comp2 = buildComponent("component-2");
        ServiceTemplateCategory resource2 = buildResource("resource-2");
        ServiceTemplateSetting param2_1 = buildParameter("param-1", ServiceTemplateSettingType.STRING, "foo2");
        ServiceTemplateSetting param2_2 = buildParameter("password1", ServiceTemplateSettingType.PASSWORD, "p@ssword3");
        ServiceTemplateSetting param2_3 = buildParameter("password2", ServiceTemplateSettingType.PASSWORD, "p@ssword4");
        resource2.getParameters().add(param2_1);
        resource2.getParameters().add(param2_2);
        resource2.getParameters().add(param2_3);
        comp2.getResources().add(resource2);
        template.getComponents().add(comp2);
        return template;
    }

    private ServiceTemplate buildSDRSTemplate() {
        ServiceTemplate template = new ServiceTemplate();
        ServiceTemplateComponent cluster1 = buildComponent("cluster-1");
        cluster1.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER);

        ServiceTemplateComponent server1 = buildComponent("server-1");
        server1.setType(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER);

        ServiceTemplateComponent storage1 = buildComponent("storage-1");
        storage1.setType(ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);

        ServiceTemplateComponent storage2 = buildComponent("storage-2");
        storage2.setType(ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);

        cluster1.addAssociatedComponentName("server-1", "Server");
        server1.addAssociatedComponentName("storage-1", "Storage");
        server1.addAssociatedComponentName("storage-2", "Storage");
        storage1.addAssociatedComponentName("server-1", "Server");
        storage2.addAssociatedComponentName("server-1", "Server");
        server1.addAssociatedComponentName("cluster-1", "Cluster");
        template.getComponents().add(server1);
        template.getComponents().add(storage1);
        template.getComponents().add(storage2);
        template.getComponents().add(cluster1);

        ServiceTemplateCategory resource = buildResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);
        storage1.getResources().add(resource);
        ServiceTemplateSetting  title = buildParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, ServiceTemplateSettingType.STRING,
                "Volume1");
        title.getOptions().add(new ServiceTemplateOption("Volume1","Volume1",null,null));
        title.getOptions().add(new ServiceTemplateOption("Volume2","Volume2",null,null));
        resource.getParameters().add(title);

        resource = buildResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);
        storage2.getResources().add(resource);
        title = buildParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, ServiceTemplateSettingType.STRING,
                "Volume2");
        title.getOptions().add(new ServiceTemplateOption("Volume1","Volume1",null,null));
        title.getOptions().add(new ServiceTemplateOption("Volume2","Volume2",null,null));
        resource.getParameters().add(title);

        resource = buildResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID);
        cluster1.getResources().add(resource);

        ServiceTemplateSetting  dsNames = buildParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID, ServiceTemplateSettingType.STRING,
                "storage-1,storage-2");

        resource.getParameters().add(dsNames);

        ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(template);
        return template;
    }

    private ServiceTemplate buildStorageTemplate() {
        ServiceTemplate template = new ServiceTemplate();
        ServiceTemplateComponent storage = buildComponent("storage-1");
        storage.setType(ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE);
        template.getComponents().add(storage);
        ServiceTemplateCategory resource = buildResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);
        storage.getResources().add(resource);
        ServiceTemplateSetting  title = buildParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID, ServiceTemplateSettingType.STRING,
                "Volume1");
        title.getOptions().add(new ServiceTemplateOption("Volume1","Volume1",null,null));
        title.getOptions().add(new ServiceTemplateOption("Volume2","Volume2",null,null));
        resource.getParameters().add(title);
        ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(template);
        return template;
    }

    @Test
    public void testFindParameterValue() {
        ServiceTemplate template = buildServiceTemplate();
        assertEquals("foo", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "param-1"));
        assertEquals("p@ssword1", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertEquals("p@ssword2", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals("foo2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "param-1"));
        assertEquals("p@ssword3", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertEquals("p@ssword4", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    @Test
    public void testSetParameterValue() {
        ServiceTemplate template = buildServiceTemplate();
        util.setParameterValue(template, "component-1", "resource-1", "password1", "newValue");
        assertEquals("newValue", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
    }

    @Test
    public void testStripPasswords() throws Exception {
        ServiceTemplate template = buildServiceTemplate();
        ServiceTemplateUtil.stripPasswords(template, null);
        assertEquals("foo", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "param-1"));
        assertNull(ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertNull(ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals("foo2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "param-1"));
        assertNull(ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertNull(ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    IEncryptedString mockEncryptedString(String value, String id) {
        IEncryptedString ret = mock(IEncryptedString.class);
        when(ret.getId()).thenReturn(id);
        when(ret.getString()).thenReturn(value);
        return ret;
    }

    @Test
    public void testEncryptPasswords() throws Exception {
        IEncryptedString encrypted1 = mockEncryptedString("p@ssword1", "0");
        when(dao.encryptAndSave("p@ssword1")).thenReturn(encrypted1);
        IEncryptedString encyrpted2 = mockEncryptedString("p@ssword2", "1");
        when(dao.encryptAndSave("p@ssword2")).thenReturn(encyrpted2);
        IEncryptedString encrypted3 = mockEncryptedString("p@ssword3", "2");
        when(dao.encryptAndSave("p@ssword3")).thenReturn(encrypted3);
        IEncryptedString encrypted4 = mockEncryptedString("p@ssword4", "3");
        when(dao.encryptAndSave("p@ssword4")).thenReturn(encrypted4);

        ServiceTemplate template = buildServiceTemplate();
        util.encryptPasswords(template);
        assertEquals("foo", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "param-1"));
        assertEquals("0", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertEquals("1", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals("foo2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "param-1"));
        assertEquals("2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertEquals("3", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    @Test
    public void testEncryptPasswords1() throws Exception {
        IEncryptedString encrypted1 = mockEncryptedString("p@ssword3_updated", "4");
        when(dao.encryptAndSave("p@ssword3_updated")).thenReturn(encrypted1);
        IEncryptedString encrypted2 = mockEncryptedString("p@ssword4_updated", "5");
        when(dao.encryptAndSave("p@ssword4_updated")).thenReturn(encrypted2);

        ServiceTemplate origTemplate = buildServiceTemplate();
        ServiceTemplate template = buildServiceTemplate();

        // Simulate an update where the first two passwords have not been changed so they
        // are null, and the second two have been changed
        util.setParameterValue(template, "component-1", "resource-1", "password1", null);
        util.setParameterValue(template, "component-1", "resource-1", "password2", null);
        util.setParameterValue(template, "component-2", "resource-2", "password1", "p@ssword3_updated");
        util.setParameterValue(template, "component-2", "resource-2", "password2", "p@ssword4_updated");

        util.encryptPasswords(template, origTemplate);
        assertEquals("foo", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "param-1"));
        assertEquals("p@ssword1", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertEquals("p@ssword2", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals("foo2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "param-1"));
        assertEquals("4", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertEquals("5", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    @Test
    public void testEncryptPasswordsWithClonedComponent() throws Exception {
        IEncryptedString encrypted0 = mockEncryptedString("p@ssword1", "p@ssword1");
        when(dao.findEncryptedStringById("p@ssword1")).thenReturn(encrypted0);

        IEncryptedString encrypted1 = mockEncryptedString("p@ssword1", "6");
        when(dao.encryptAndSave("p@ssword1")).thenReturn(encrypted1);
        IEncryptedString encrypted2 = mockEncryptedString("p@ssword5", "7");
        when(dao.encryptAndSave("p@ssword5")).thenReturn(encrypted2);

        ServiceTemplate origTemplate = buildServiceTemplate();
        ServiceTemplate template = buildServiceTemplate();

        // Simulate an update where a new component has been added that is a clone of component-1.
        // The first password has not been changed so it is null, and should be retrieved from
        // coponent-1. The 2nd password has been changed so it should be encrypted.

        ServiceTemplateComponent comp2 = buildComponent("component-3");
        comp2.setClonedFromId("component-1");
        ServiceTemplateCategory resource2 = buildResource("resource-1");
        ServiceTemplateSetting param1_1 = buildParameter("password1", ServiceTemplateSettingType.PASSWORD, null);
        ServiceTemplateSetting param1_2 = buildParameter("password2", ServiceTemplateSettingType.PASSWORD, "p@ssword5");
        resource2.getParameters().add(param1_1);
        resource2.getParameters().add(param1_2);
        comp2.getResources().add(resource2);
        template.getComponents().add(comp2);

        util.setParameterValue(template, "component-1", "resource-1", "password1", null);
        util.setParameterValue(template, "component-1", "resource-1", "password2", null);
        util.setParameterValue(template, "component-2", "resource-2", "password1", null);
        util.setParameterValue(template, "component-2", "resource-2", "password2", null);

        util.encryptPasswords(template, origTemplate);
        assertEquals("6", ServiceTemplateUtil.findParameterValue(template, "component-3", "resource-1", "password1"));
        assertEquals("7", ServiceTemplateUtil.findParameterValue(template, "component-3", "resource-1", "password2"));
    }

    @Test
    public void testDecryptPasswords() throws Exception {
        ServiceTemplate template = buildServiceTemplate();

        // Set password values to mock encrypted string ids
        util.setParameterValue(template, "component-1", "resource-1", "password1", "0");
        util.setParameterValue(template, "component-1", "resource-1", "password2", "1");
        util.setParameterValue(template, "component-2", "resource-2", "password1", "2");
        util.setParameterValue(template, "component-2", "resource-2", "password2", "3");

        IEncryptedString encrypted1 = mockEncryptedString("p@ssword1", "0");
        when(dao.findEncryptedStringById(encrypted1.getId())).thenReturn(encrypted1);
        IEncryptedString encrypted2 = mockEncryptedString("p@ssword2", "1");
        when(dao.findEncryptedStringById(encrypted2.getId())).thenReturn(encrypted2);
        IEncryptedString encrypted3 = mockEncryptedString("p@ssword3", "2");
        when(dao.findEncryptedStringById(encrypted3.getId())).thenReturn(encrypted3);
        IEncryptedString encrypted4 = mockEncryptedString("p@ssword4", "3");
        when(dao.findEncryptedStringById(encrypted4.getId())).thenReturn(encrypted4);

        util.decryptPasswords(template);

        assertEquals(encrypted1.getString(), ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertEquals(encrypted2.getString(), ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals(encrypted3.getString(), ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertEquals(encrypted4.getString(), ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    @Test
    public void testGetEncryptionIds() throws Exception {
        ServiceTemplate template = buildServiceTemplate();
        Set<String> encryptionIds = util.getEncryptionIds(template);
        Set<String> expectedIds = ImmutableSet.of("p@ssword1", "p@ssword2", "p@ssword3", "p@ssword4");
        assertEquals(expectedIds, encryptionIds);
    }

    @Test
    public void testGetEncryptionIdsIgnoresEmptyValues() throws Exception {
        ServiceTemplate template = buildServiceTemplate();

        // Set first password to empty string
        util.setParameterValue(template, "component-1", "resource-1", "password1", "");

        Set<String> encryptionIds = util.getEncryptionIds(template);
        Set<String> expectedIds = ImmutableSet.of("p@ssword2", "p@ssword3", "p@ssword4");
        assertEquals(expectedIds, encryptionIds);
    }

    @Test
    public void testDuplicatePasswords() throws Exception {
        ServiceTemplate template = buildServiceTemplate();
        util.setParameterValue(template, "component-1", "resource-1", "password1", "1");
        util.setParameterValue(template, "component-1", "resource-1", "password2", "2");
        util.setParameterValue(template, "component-2", "resource-2", "password1", "3");
        util.setParameterValue(template, "component-2", "resource-2", "password2", "4");

        IEncryptedString encrypted1 = mockEncryptedString("p@ssword1", "1");
        when(dao.findEncryptedStringById("1")).thenReturn(encrypted1);
        IEncryptedString encrypted2 = mockEncryptedString("p@ssword2", "2");
        when(dao.findEncryptedStringById("2")).thenReturn(encrypted2);
        IEncryptedString encrypted3 = mockEncryptedString("p@ssword3", "3");
        when(dao.findEncryptedStringById("3")).thenReturn(encrypted3);
        IEncryptedString encrypted4 = mockEncryptedString("p@ssword4", "4");
        when(dao.findEncryptedStringById("4")).thenReturn(encrypted4);

        IEncryptedString enc1 = mockEncryptedString("p@ssword1", "5");
        when(dao.encryptAndSave("p@ssword1")).thenReturn(enc1);
        IEncryptedString enc2 = mockEncryptedString("p@ssword2", "6");
        when(dao.encryptAndSave("p@ssword2")).thenReturn(enc2);
        IEncryptedString enc3 = mockEncryptedString("p@ssword3", "7");
        when(dao.encryptAndSave("p@ssword3")).thenReturn(enc3);
        IEncryptedString enc4 = mockEncryptedString("p@ssword4", "8");
        when(dao.encryptAndSave("p@ssword4")).thenReturn(enc4);

        util.duplicatePasswords(template);

        assertEquals("foo", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "param-1"));
        assertEquals("5", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertEquals("6", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals("foo2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "param-1"));
        assertEquals("7", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertEquals("8", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    @Test
    public void testDuplicatePasswordsIgnoresEmptyPasswords() {
        ServiceTemplate template = buildServiceTemplate();
        util.setParameterValue(template, "component-1", "resource-1", "password1", "1");
        util.setParameterValue(template, "component-1", "resource-1", "password2", "");
        util.setParameterValue(template, "component-2", "resource-2", "password1", "3");
        util.setParameterValue(template, "component-2", "resource-2", "password2", "4");

        IEncryptedString encrypted1 = mockEncryptedString("p@ssword1", "1");
        when(dao.findEncryptedStringById("1")).thenReturn(encrypted1);
        IEncryptedString encrypted3 = mockEncryptedString("p@ssword3", "3");
        when(dao.findEncryptedStringById("3")).thenReturn(encrypted3);
        IEncryptedString encrypted4 = mockEncryptedString("p@ssword4", "4");
        when(dao.findEncryptedStringById("4")).thenReturn(encrypted4);

        IEncryptedString enc1 = mockEncryptedString("p@ssword1", "5");
        when(dao.encryptAndSave("p@ssword1")).thenReturn(enc1);
        IEncryptedString enc3 = mockEncryptedString("p@ssword3", "7");
        when(dao.encryptAndSave("p@ssword3")).thenReturn(enc3);
        IEncryptedString enc4 = mockEncryptedString("p@ssword4", "8");
        when(dao.encryptAndSave("p@ssword4")).thenReturn(enc4);

        util.duplicatePasswords(template);

        assertEquals("foo", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "param-1"));
        assertEquals("5", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password1"));
        assertEquals("", ServiceTemplateUtil.findParameterValue(template, "component-1", "resource-1", "password2"));
        assertEquals("foo2", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "param-1"));
        assertEquals("7", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password1"));
        assertEquals("8", ServiceTemplateUtil.findParameterValue(template, "component-2", "resource-2", "password2"));
    }

    @Test
    public void testDeleteEncryptionIds() throws Exception {
        ServiceTemplate template = buildServiceTemplate();
        util.setParameterValue(template, "component-1", "resource-1", "password1", "1");
        util.setParameterValue(template, "component-1", "resource-1", "password2", "2");
        util.setParameterValue(template, "component-2", "resource-2", "password1", "3");
        util.setParameterValue(template, "component-2", "resource-2", "password2", "4");

        IEncryptedString enc1 = mockEncryptedString(null, "1");
        when(dao.findEncryptedStringById("1")).thenReturn(enc1);
        IEncryptedString enc2 = mockEncryptedString(null, "2");
        when(dao.findEncryptedStringById("2")).thenReturn(enc2);
        IEncryptedString enc3 = mockEncryptedString(null, "3");
        when(dao.findEncryptedStringById("3")).thenReturn(enc3);
        IEncryptedString enc4 = mockEncryptedString(null, "4");
        when(dao.findEncryptedStringById("4")).thenReturn(enc4);

        util.deleteEncryptionIds(template);

        verify(dao).delete(enc1);
        verify(dao).delete(enc2);
        verify(dao).delete(enc3);
        verify(dao).delete(enc4);
    }

    @Test
    public void testDeleteRemovedEncryptionIds() throws Exception {
        ServiceTemplate origTemplate = buildServiceTemplate();
        util.setParameterValue(origTemplate, "component-1", "resource-1", "password1", "1");
        util.setParameterValue(origTemplate, "component-1", "resource-1", "password2", "2");
        util.setParameterValue(origTemplate, "component-2", "resource-2", "password1", "3");
        util.setParameterValue(origTemplate, "component-2", "resource-2", "password2", "4");

        ServiceTemplate template = buildServiceTemplate();
        util.setParameterValue(template, "component-1", "resource-1", "password1", "1");
        util.setParameterValue(template, "component-1", "resource-1", "password2", "2");
        template.getComponents().remove(1); // remove component-2

        // Expected cases for findEncryptedStringById
        IEncryptedString enc3 = mockEncryptedString(null, "3");
        when(dao.findEncryptedStringById("3")).thenReturn(enc3);
        IEncryptedString enc4 = mockEncryptedString(null, "4");
        when(dao.findEncryptedStringById("4")).thenReturn(enc4);

        util.deleteRemovedEncryptionIds(origTemplate, template);

        verify(dao).delete(enc3);
        verify(dao).delete(enc4);
    }
    
    // Check that the asmversion.properties file is being populated by the gradle build
    @Test
    public void testGetCurrentTemplateVersion() {
        String version = ServiceTemplateUtil.getCurrentTemplateVersion();
        assertNotNull(version);

        // WARNING: this will probably fail in your IDE because it will pick up the unfiltered
        assertFalse("The asmversion.properties @VERSION@ token has not been replaced", "@VERSION@".equals(version));
    }

    @Test
    public void testGetAssociatedStorageComponentsFromCluster() {
        ServiceTemplate template = buildSDRSTemplate();
        ServiceTemplateComponent clusterComp = template.findComponentById("cluster-1");
        List<ServiceTemplateComponent> storageComponents = ServiceTemplateUtil.getAssociatedStorageComponentsFromCluster(clusterComp, template);

        Assert.assertEquals("Number of storages", 2, storageComponents.size());
    }

    @Test
    public void testGetAssociatedClusterComponentFromStorage() {
        ServiceTemplate template = buildSDRSTemplate();
        ServiceTemplateComponent storageComp = template.findComponentById("storage-1");
        ServiceTemplateComponent clusterComponent = ServiceTemplateUtil.getAssociatedClusterComponentFromStorage(storageComp, template.fetchComponentsMap());

        Assert.assertEquals("Couldn't find associated cluster", clusterComponent.getId(), "cluster-1");
    }

    @Test
    public void testAddAssignedUsers() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setId("ST1");
        ServiceTemplateEntity entity = new ServiceTemplateEntity();
        ServiceTemplateUtil.addAssignedUsers(serviceTemplate, entity);
        assertNull(serviceTemplate.getAssignedUsers());
        Assert.assertEquals(entity.getAssignedUserList().size(), 0);
        User user = new User();
        user.setUserSeqId(1L);
        Set<User> userSet = new HashSet<>();
        userSet.add(user);
        serviceTemplate.setAssignedUsers(userSet);
        assertNotNull(serviceTemplate.getAssignedUsers());
        assertEquals(serviceTemplate.getAssignedUsers().size(), 1);
        ServiceTemplateUtil.addAssignedUsers(serviceTemplate,entity);
        assertEquals(entity.getAssignedUserList().size(), 1);
        for (TemplateUserRefEntity refEntity : entity.getAssignedUserList()) {
            assertEquals(refEntity.getTemplateId(),"ST1");
            assertEquals(refEntity.getUserId(), 1L);
        }
    }

    @Test
    public void testGetVolumeSet() {
        ServiceTemplate template = buildStorageTemplate();
        ServiceTemplateComponent storageComp = template.findComponentById("storage-1");

        Set<String> volumes = ServiceTemplateUtil.getVolumeSet(storageComp.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID));
        Assert.assertEquals("Number of volumes", 2, volumes.size());
    }

    @Test
    public void testProcessStoragesForDeployment() {
        ServiceTemplate template = buildStorageTemplate();
        ServiceTemplateComponent storageComp = template.findComponentById("storage-1");
        ServiceTemplateCategory resource = storageComp.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);

        ServiceTemplateSetting title = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
        Assert.assertEquals("Volume name before", ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING, title.getValue());

        Map<String, String> volumeMap = ServiceTemplateUtil.processStoragesForDeployment(template);

        Assert.assertEquals("Volume name after", "Volume1", title.getValue());
        Assert.assertEquals("Volume map size", 1, volumeMap.size());
        Assert.assertEquals("Volume map member key", "storage-1", volumeMap.keySet().iterator().next());
        Assert.assertEquals("Volume map member value", "Volume1", volumeMap.values().iterator().next());
    }

    @Test
    public void processClustersForDeployment() {
        ServiceTemplate template = buildSDRSTemplate();
        ServiceTemplateComponent cluster = template.findComponentById("cluster-1");

        Map<String, String> volumeMap = ServiceTemplateUtil.processStoragesForDeployment(template);

        ServiceTemplateSetting dsNames = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID);

        Assert.assertEquals("Volume name before", "storage-1,storage-2", dsNames.getValue());

        ServiceTemplateUtil.processClustersForDeployment(template, volumeMap);

        Assert.assertEquals("Volume name after", "Volume1,Volume2", dsNames.getValue());
    }

    public static ServiceTemplateUtil mockServiceTemplateUtil(EncryptionDAO encryptionDAO, INetworkService networkService) {
        ServiceTemplateUtil serviceTemplateUtil = new ServiceTemplateUtil() {
            @Override
            public String findTask(String repoName) {
                return "junit-task-" + repoName;
            }

            @Override
            public void setHiddenValues(ServiceTemplate serviceTemplate) {

            }

            @Override
            public Map<String, String> mapReposToTasks(){
                List<String> repoName = new ArrayList<>(Arrays.asList("esxi-6.5:vmware_esxi", "esxi-6.0:vmware_esxi",
                        "esxi-5.5:vmware_esxi", "esxi-5.1:vmware_esxi",
                        "redhat6:redhat","redhat7:redhat7",
                        "suse11:suse11","suse12:suse12",
                        "windows2008:windows2008",
                        "win2012r2:windows2012"));
                Map<String, String> repoMap = new HashMap<String, String>();
                for(String name : repoName) {
                    String[] arr = name.split(":");
                    repoMap.put(arr[0], arr[1]);
                }
                return repoMap;
            }
        };
        serviceTemplateUtil.setNetworkService(networkService);
        serviceTemplateUtil.setEncryptionDAO(encryptionDAO);
        return serviceTemplateUtil;
    }
}
