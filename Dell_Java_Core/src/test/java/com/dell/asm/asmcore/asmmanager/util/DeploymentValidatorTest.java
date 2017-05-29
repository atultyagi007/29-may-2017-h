package com.dell.asm.asmcore.asmmanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.util.template.ServiceTemplateComponentUpgrader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentNamesType;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentValid;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeploymentNamesRefDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentNamesRefEntity;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeploymentValidatorTest {
    
    private static final Logger LOG = Logger.getLogger(DeploymentValidatorTest.class);

    private static String baseServiceTemplateXml;

    @BeforeClass
    public static void setUpOnce() throws IOException {
        URL url = Resources.getResource("util/deploymentValidatorDefaultTemplate.xml");
        baseServiceTemplateXml = Resources.toString(url, Charsets.UTF_8);
    }
    
    private ServiceTemplate baseServiceTemplate;
    private Deployment deployment;
    private DeploymentNamesRefDAO deploymentNamesRefDAO;
    private DeploymentValidator deploymentValidator = DeploymentValidator.getInstance();
    
    @Before
    public void setUp() throws IOException {
        deploymentNamesRefDAO = mock(DeploymentNamesRefDAO.class);
        deploymentValidator.setDeploymentNamesRefDAO(deploymentNamesRefDAO);
        baseServiceTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                baseServiceTemplateXml);
        updateServerTemplateHostNames("Server1","Server2");
        updateTemplateVMNames("VM1","VM2");
        deployment = new Deployment();
        deployment.setId("deployment1");
        deployment.setDeploymentName("DeploymentValidatorTest Deployment");
        deployment.setServiceTemplate(baseServiceTemplate);

        ServiceTemplateComponentUpgrader.upgradeStorageVolumeSettings(baseServiceTemplate);
    }

    @Test
    public void testCheckDuplicateOSHostNames() {
        DeploymentValid deploymentValid = null;
        List<DeploymentNamesRefEntity> entities = new ArrayList<>();
        when(deploymentNamesRefDAO.getAllDeploymentNamesRefsByType(any(DeploymentNamesType.class))).thenReturn(entities);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());

        updateServerTemplateHostNames("Server1","Server1");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("Unable to perform the operation because the hostname [Server1] is defined more than once in the template or an existing service.",deploymentValid.getMessages().get(0).getDisplayMessage());

        DeploymentNamesRefEntity entity = new DeploymentNamesRefEntity();
        entity.setDeploymentId("1");
        entity.setName("Server3");
        entities.add(entity);

        updateServerTemplateHostNames("Server1","Server2");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());

        updateServerTemplateHostNames("Server1","Server3");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("Unable to perform the operation because the hostname [Server3] is defined more than once in the template or an existing service.",deploymentValid.getMessages().get(0).getDisplayMessage());

    }

    @Test
    public void testCheckDuplicateVMNames() {
        DeploymentValid deploymentValid = null;
        List<DeploymentNamesRefEntity> entities = new ArrayList<>();
        when(deploymentNamesRefDAO.getAllDeploymentNamesRefsByType(any(DeploymentNamesType.class))).thenReturn(entities);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());

        updateTemplateVMNames("VM1","VM1");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("The VM with name [VM1] is already used in existing deployment.",
                deploymentValid.getMessages().get(0).getDisplayMessage());

        DeploymentNamesRefEntity entity = new DeploymentNamesRefEntity();
        entity.setDeploymentId("1");
        entity.setName("VM3");
        entities.add(entity);

        updateTemplateVMNames("VM1","VM2");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());

        updateTemplateVMNames("VM1","VM3");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("The VM with name [VM3] is already used in existing deployment.",deploymentValid.getMessages().get(0).getDisplayMessage());

    }

    @Test
    public void testCheckDuplicateVolumeNames() {
        DeploymentValid deploymentValid = null;
        List<DeploymentNamesRefEntity> entities = new ArrayList<>();
        when(deploymentNamesRefDAO.getAllDeploymentNamesRefsByType(any(DeploymentNamesType.class))).thenReturn(entities);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,true);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());

        updateTemplateVolumeNames(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, "AdamSimpleTest","AdamSimpleTest");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,true);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("Unable to perform the operation because the storage volume name AdamSimpleTest is not unique.",
                deploymentValid.getMessages().get(0).getDisplayMessage());

        DeploymentNamesRefEntity entity = new DeploymentNamesRefEntity();
        entity.setDeploymentId("1");
        entity.setName("AdamSimpleTest3");
        entities.add(entity);

        updateTemplateVolumeNames(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, "","");
        updateTemplateVolumeNames(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, "AdamSimpleTest","AdamSimpleTest2");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,true);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());

        updateTemplateVolumeNames(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID, "AdamSimpleTest","AdamSimpleTest");
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,true);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("Unable to perform the operation because the storage volume name AdamSimpleTest is not unique.",deploymentValid.getMessages().get(0).getDisplayMessage());

    }

    @Test
    public void testFutureDate() {
        DeploymentValid deploymentValid = null;
        List<DeploymentNamesRefEntity> entities = new ArrayList<>();
        when(deploymentNamesRefDAO.getAllDeploymentNamesRefsByType(any(DeploymentNamesType.class))).thenReturn(entities);
        Calendar calendar = Calendar.getInstance();
        calendar.set(1976,04,05);
        deployment.setScheduleDate(calendar.getTime());
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        assertEquals("The schedule date is already past.",deploymentValid.getMessages().get(0).getDisplayMessage());
    }

    @Test
    public void testValidateStorageComponent() {
        DeploymentValid deploymentValid = null;
        List<DeploymentNamesRefEntity> entities = new ArrayList<>();
        when(deploymentNamesRefDAO.getAllDeploymentNamesRefsByType(any(DeploymentNamesType.class))).thenReturn(entities);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertTrue(deploymentValid.isValid());
        Map<String,ServiceTemplateComponent> componentMap = new HashMap<>();
        Set<ServiceTemplateComponent> removeSet = new HashSet<>();
        Set<ServiceTemplateComponent> storageSet = new HashSet<>();
        for (ServiceTemplateComponent component : baseServiceTemplate.getComponents()) {
            componentMap.put(component.getId(),component);
            if ( ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                removeSet.add(component);
            }  else if (ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE.equals(component.getType())) {
                storageSet.add(component);
            }
        }

        for (ServiceTemplateComponent component : storageSet) {
            Set<String> removeAssociated = new HashSet<>();
            for( String associatedId : component.getAssociatedComponents().keySet()) {
                ServiceTemplateComponent associated = componentMap.get(associatedId);
                if (associated != null && ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(associated.getType())) {
                    removeAssociated.add(associatedId);
                }
            }
            component.removeAllAssociatedComponents(removeAssociated);
        }
        baseServiceTemplate.getComponents().removeAll(removeSet);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        for (AsmDetailedMessage message : deploymentValid.getMessages()) {
            assertEquals("Storage component must have IQN/IP values.", message.getDisplayMessage());
        }
    }

    @Test
    public void testCheckForMultipleDeployments() {
        DeploymentValid deploymentValid = null;
        List<DeploymentNamesRefEntity> entities = new ArrayList<>();
        when(deploymentNamesRefDAO.getAllDeploymentNamesRefsByType(any(DeploymentNamesType.class))).thenReturn(entities);
        deployment.setNumberOfDeployments(2);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        for (AsmDetailedMessage message : deploymentValid.getMessages()) {
            assertEquals("Template has a non-server component.", message.getDisplayMessage());
        }

        Set<ServiceTemplateComponent> removeSet = new HashSet<>();
        for (ServiceTemplateComponent component : baseServiceTemplate.getComponents()) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                component.getAssociatedComponents().clear();
                component.getRelatedComponents().clear();
                ServiceTemplateSetting sourceSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE);
                if (sourceSetting != null) {
                    sourceSetting.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SOURCE_MANUAL);
                }
            } else {
                removeSet.add(component);
            }
        }
        baseServiceTemplate.getComponents().removeAll(removeSet);
        try {
            deploymentValid = deploymentValidator.validateDeployment(deployment,false);
        } catch (Exception e) {
            fail("Should not throw an exception");
        }
        assertFalse(deploymentValid.isValid());
        for (AsmDetailedMessage message : deploymentValid.getMessages()) {
            assertEquals("For multiple service deployments Server source may not be set to Manual Entry. Set Server Source to Server Pool and continue deployment.", message.getDisplayMessage());
        }


    }

    private void updateServerTemplateHostNames(String server1HostName, String server2HostName) {
        for (ServiceTemplateComponent component : baseServiceTemplate.getComponents()) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                ServiceTemplateSetting osHostNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                if (osHostNameSetting != null) {
                    if (component.getName().equals("Server")) {
                        osHostNameSetting.setValue(server1HostName);
                    } else if (component.getName().equals("Server (2)")) {
                        osHostNameSetting.setValue(server2HostName);
                    }
                }
            }
        }
    }

    private void updateTemplateVMNames(String vm1VMName, String vm2VMName) {
        for (ServiceTemplateComponent component : baseServiceTemplate.getComponents()) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                ServiceTemplateSetting vmNameSetting = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HOSTNAME_ID);
                if (vmNameSetting != null) {
                    if (component.getName().equals("vCenter Virtual Machine")) {
                        vmNameSetting.setValue(vm1VMName);
                    } else if (component.getName().equals("vCenter Virtual Machine (2)")) {
                        vmNameSetting.setValue(vm2VMName);
                    }
                }
            }
        }
    }

    private void updateTemplateVolumeNames(String categoryId,  String volumeName1, String volumeName2) {
        for (ServiceTemplateComponent component : baseServiceTemplate.getComponents()) {
            if (ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE.equals(component.getType())) {

                ServiceTemplateSetting titleSetting = component.getParameter(categoryId, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID);
                titleSetting.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW);
                ServiceTemplateSetting nameSetting = component.getParameter(categoryId, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW);
                if (component.getName().equals("EqualLogic")) {
                    nameSetting.setValue(volumeName1);
                } else if (component.getName().equals("EqualLogic (2)")) {
                    nameSetting.setValue(volumeName2);
                }
            }
        }
    }


}
