package com.dell.asm.asmcore.asmmanager.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dell.asm.asmcore.asmmanager.app.rest.ServiceTemplateServiceTest;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtilTest;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.Network;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.AbstractAsmManagerTest;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateValid;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ServiceTemplateValidatorTest extends AbstractAsmManagerTest {

    private static final Logger LOG = Logger.getLogger(ServiceTemplateValidatorTest.class);

    private static String requireParametersTemplateXml;
    private static String requiredParametersMissingTemplateXml;
    private static String clusterValidationTemplateXml;
    private static String clusterValidationTemplateFalseXml;
    private static String domainSettingsValidationTemplateXml;
    private static String hypervChapValidationTemplateXml;
    private static String vsanStorageValidationTemplateXml;
    private static String fcStorageValidationTemplateXml;
    private static String fcStorageValidationTemplateValidXml;
    private static String eql_vol_name_invalid;
    private static String eql_vol_name_valid;

    @BeforeClass
    public static void setUpOnce() throws IOException {
        URL url = Resources.getResource("required_parameters_template.xml");
        requireParametersTemplateXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("required_parameters_missing_template.xml");
        requiredParametersMissingTemplateXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("cluster_validation_template.xml");
        clusterValidationTemplateXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("hyperv_chap_validation_template.xml");
        hypervChapValidationTemplateXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("vsan_storage_validation_template.xml");
        vsanStorageValidationTemplateXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("fc_storage_validation_template.xml");
        fcStorageValidationTemplateXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("fc_storage_validation_template_valid.xml");
        fcStorageValidationTemplateValidXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("podname_validation_template.xml");
        clusterValidationTemplateFalseXml = Resources.toString(url, Charsets.UTF_8);
        url = Resources.getResource("windows_domain_settings_template.xml");
        domainSettingsValidationTemplateXml = Resources.toString(url, Charsets.UTF_8);
    }

    private ServiceTemplate requiredParametersTemplate;
    private ServiceTemplate requiredParametersMissingTemplate;
    private ServiceTemplate clusterValidationTemplate;
    private ServiceTemplate hypervChapValidationTemplate;
    private ServiceTemplate vsanStorageValidationTemplate;
    private ServiceTemplate fcStorageValidationTemplate;
    private ServiceTemplate fcStorageValidationTemplateValid;
    private ServiceTemplate clusterValidationTemplateInvalid;
    private ServiceTemplate domainSettingsValidationTemplate;
    private ParameterBuilder parameter1;
    private ParameterBuilder parameter2;
    private ParameterBuilder parameter3;
    private ParameterBuilder parameter4;
    private ParameterBuilder parameter5;
    private ParameterBuilder parameter6;
    private ServiceTemplateValidator serviceTemplateValidator = null;

    @Before
    public void setUp() throws IOException {
        requiredParametersTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                requireParametersTemplateXml);
        requiredParametersMissingTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                requiredParametersMissingTemplateXml);
        clusterValidationTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                clusterValidationTemplateXml);
        hypervChapValidationTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                hypervChapValidationTemplateXml);
        vsanStorageValidationTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                vsanStorageValidationTemplateXml);
        fcStorageValidationTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
                fcStorageValidationTemplateXml);
        fcStorageValidationTemplateValid = MarshalUtil.unmarshal(ServiceTemplate.class,
                fcStorageValidationTemplateValidXml);
        clusterValidationTemplateInvalid = MarshalUtil.unmarshal(ServiceTemplate.class,
                clusterValidationTemplateFalseXml);
        domainSettingsValidationTemplate = MarshalUtil.unmarshal(ServiceTemplate.class,
        		domainSettingsValidationTemplateXml);

        // parameter 1 is not required cause it has unsatisfied parameter dependencies 
        // (should pass validation)
        parameter1 = new ParameterBuilder("parameter1")
                .addRequired(Boolean.TRUE)
                .addDependency("parameter2", "value1,value2")
                .addDisplayName("Parameter 1 Display Name");
        parameter2 = new ParameterBuilder("parameter2")
                .addRequired(Boolean.TRUE)
                .addValue("value2")
                .addDependency("parameter3", "value3")
                .addDisplayName("Parameter 2 Display Name");
        parameter3 = new ParameterBuilder("parameter3")
                .addDisplayName("Parameter 3 Display Name");

        // parameter 4 is required because it has satisfied parameter dependencies 
        // (should not pass validation unless value is added to parameter4)
        parameter4 = new ParameterBuilder("parameter4")
                .addRequired(Boolean.TRUE)
                .addDependency("parameter5", "value5,value2")
                .addDisplayName("Parameter 4 Display Name");
        parameter5 = new ParameterBuilder("parameter5")
                .addRequired(Boolean.TRUE)
                .addValue("value5")
                .addDependency("parameter6", "value6")
                .addDisplayName("Parameter 5 Display Name");
        parameter6 = new ParameterBuilder("parameter6")
                .addValue("value6")
                .addDisplayName("Parameter 6 Display Name");
        eql_vol_name_invalid = "test_123";
        eql_vol_name_valid = "test123";

        serviceTemplateValidator = mockServiceTemplateValidator();
    }

    @Test
    public void testMissingRequiredParameters() {
        final Map<String, ServiceTemplateSetting> emptyRequiredParameters = new HashMap<String, ServiceTemplateSetting>();
        for (ServiceTemplateComponent component : requiredParametersMissingTemplate.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {
                for (ServiceTemplateSetting setting : resource.getParameters()) {
                    if (setting.isRequired() && StringUtils.isBlank(setting.getValue())) {
                        emptyRequiredParameters.put(component.getId() + "_" + setting.getId(), setting);
                        LOG.info("Component Id: " + component.getId() + " Setting Id: " + setting.getId() +
                                " Parameter: " + setting.getDisplayName());
                    }
                }
            }
        }

        serviceTemplateValidator.validateRequiredParameters(requiredParametersMissingTemplate);
        assertFalse(requiredParametersMissingTemplate.getTemplateValid().isValid());
        // validation messages should be at each invalid component
        assertTrue(CollectionUtils.isEmpty(requiredParametersMissingTemplate.getTemplateValid().getMessages()));
        final List<AsmDetailedMessage> validationMessages = new ArrayList<AsmDetailedMessage>();
        for (ServiceTemplateComponent component : requiredParametersMissingTemplate.getComponents()) {
            validationMessages.addAll(component.getComponentValid().getMessages());
        }
        assertTrue(validationMessages.size() == emptyRequiredParameters.size());
    }

    @Test
    public void testRequiredParameters() {
        serviceTemplateValidator.validateRequiredParameters(requiredParametersTemplate);
        assertTrue(requiredParametersTemplate.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(requiredParametersTemplate.getTemplateValid().getMessages()));
        final List<AsmDetailedMessage> validationMessages = new ArrayList<AsmDetailedMessage>();
        for (ServiceTemplateComponent component : requiredParametersMissingTemplate.getComponents()) {
            validationMessages.addAll(component.getComponentValid().getMessages());
        }
        assertTrue(CollectionUtils.isEmpty(validationMessages));
    }

    @Test
    public void testRequiredParametersWhenDependencyChainNotSatisfied() {
        final ServiceTemplate missingParameterValueWithDependencyNotSatisfied =
                new ServiceTemplateBuilder("ServiceTemplateWithUnsatisfiedDependencies")
                        .addComponent(new ComponentBuilder("component1")
                                .addResource(new ResourceBuilder("resource1")
                                        .addParameter(parameter1)
                                        .addParameter(parameter2)
                                        .addParameter(parameter3))).build();

        serviceTemplateValidator.validateRequiredParameters(missingParameterValueWithDependencyNotSatisfied);
        assertNotNull(missingParameterValueWithDependencyNotSatisfied);
        assertNotNull(missingParameterValueWithDependencyNotSatisfied.getTemplateValid());
        assertTrue(missingParameterValueWithDependencyNotSatisfied.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(
                missingParameterValueWithDependencyNotSatisfied.getTemplateValid().getMessages()));
        final ServiceTemplateComponent missingValueComponent1 =
                missingParameterValueWithDependencyNotSatisfied.findComponentById("component1");
        assertNotNull(missingValueComponent1);
        assertNotNull(missingValueComponent1.getComponentValid());
        assertTrue(missingValueComponent1.getComponentValid().isValid());
        assertTrue(CollectionUtils.isEmpty(missingValueComponent1.getComponentValid().getMessages()));

        final ServiceTemplate parameterValueWithDependencyNotSatisfied =
                new ServiceTemplateBuilder("ServiceTemplateWithUnsatisfiedDependencies")
                        .addComponent(new ComponentBuilder("component1")
                                .addResource(new ResourceBuilder("resource1")
                                        .addParameter(parameter1.addValue("value1"))
                                        .addParameter(parameter2)
                                        .addParameter(parameter3))).build();

        serviceTemplateValidator.validateRequiredParameters(parameterValueWithDependencyNotSatisfied);
        assertNotNull(parameterValueWithDependencyNotSatisfied);
        assertNotNull(parameterValueWithDependencyNotSatisfied.getTemplateValid());
        assertTrue(parameterValueWithDependencyNotSatisfied.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(
                parameterValueWithDependencyNotSatisfied.getTemplateValid().getMessages()));
        final ServiceTemplateComponent valueComponent1 =
                parameterValueWithDependencyNotSatisfied.findComponentById("component1");
        assertNotNull(valueComponent1);
        assertNotNull(valueComponent1.getComponentValid());
        assertTrue(valueComponent1.getComponentValid().isValid());
        assertTrue(CollectionUtils.isEmpty(valueComponent1.getComponentValid().getMessages()));
    }

    @Test
    public void testRequiredParametersWhenDependencyChainSatisfied() {
        final ServiceTemplate missingParameterValueWithDependenciesSatisfied =
                new ServiceTemplateBuilder("ServiceTemplateWithSatisfiedDependencies")
                        .addComponent(new ComponentBuilder("component1")
                                .addResource(new ResourceBuilder("resource1")
                                        .addParameter(parameter4)
                                        .addParameter(parameter5)
                                        .addParameter(parameter6))).build();

        serviceTemplateValidator.validateRequiredParameters(missingParameterValueWithDependenciesSatisfied);
        assertNotNull(missingParameterValueWithDependenciesSatisfied);
        assertNotNull(missingParameterValueWithDependenciesSatisfied.getTemplateValid());
        assertFalse(missingParameterValueWithDependenciesSatisfied.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(
                missingParameterValueWithDependenciesSatisfied.getTemplateValid().getMessages()));
        final ServiceTemplateComponent invalidComponent1 =
                missingParameterValueWithDependenciesSatisfied.findComponentById("component1");
        assertNotNull(invalidComponent1);
        assertNotNull(invalidComponent1.getComponentValid());
        assertFalse(invalidComponent1.getComponentValid().isValid());
        assertTrue(CollectionUtils.isNotEmpty(invalidComponent1.getComponentValid().getMessages()));

        final ServiceTemplate missingDependencyParameterValueWithDependenciesSatisfied =
                new ServiceTemplateBuilder("ServiceTemplateWithSatisfiedDependencies")
                        .addComponent(new ComponentBuilder("component1")
                                .addResource(new ResourceBuilder("resource1")
                                        .addParameter(parameter4)
                                        .addParameter(parameter5
                                                .addValue(StringUtils.EMPTY)
                                                .addRequired(Boolean.FALSE))
                                        .addParameter(parameter6))).build();

        serviceTemplateValidator.validateRequiredParameters(missingDependencyParameterValueWithDependenciesSatisfied);
        assertNotNull(missingDependencyParameterValueWithDependenciesSatisfied);
        assertNotNull(missingDependencyParameterValueWithDependenciesSatisfied.getTemplateValid());
        assertTrue(missingDependencyParameterValueWithDependenciesSatisfied.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(
                missingDependencyParameterValueWithDependenciesSatisfied.getTemplateValid().getMessages()));
        final ServiceTemplateComponent missingValidComponent1 =
                missingDependencyParameterValueWithDependenciesSatisfied.findComponentById("component1");
        assertNotNull(missingValidComponent1);
        assertNotNull(missingValidComponent1.getComponentValid());
        assertTrue(missingValidComponent1.getComponentValid().isValid());
        assertTrue(CollectionUtils.isEmpty(missingValidComponent1.getComponentValid().getMessages()));

        final ServiceTemplate validParameterValueWithDependenciesSatisfied =
                new ServiceTemplateBuilder("ServiceTemplateWithSatisfiedDependencies")
                        .addComponent(new ComponentBuilder("component1")
                                .addResource(new ResourceBuilder("resource1")
                                        .addParameter(parameter4.addValue("value4"))
                                        .addParameter(parameter5)
                                        .addParameter(parameter6))).build();
        serviceTemplateValidator.validateRequiredParameters(validParameterValueWithDependenciesSatisfied);
        assertNotNull(validParameterValueWithDependenciesSatisfied);
        assertNotNull(validParameterValueWithDependenciesSatisfied.getTemplateValid());
        assertTrue(validParameterValueWithDependenciesSatisfied.getTemplateValid().isValid());
        assertTrue(CollectionUtils.isEmpty(
                validParameterValueWithDependenciesSatisfied.getTemplateValid().getMessages()));
        final ServiceTemplateComponent validComponent1 =
                validParameterValueWithDependenciesSatisfied.findComponentById("component1");
        assertNotNull(validComponent1);
        assertNotNull(validComponent1.getComponentValid());
        assertTrue(validComponent1.getComponentValid().isValid());
        assertTrue(CollectionUtils.isEmpty(validComponent1.getComponentValid().getMessages()));
    }

    @Test
    public void testMultipleClusterTemplateValidation() {
        //first validate initial template
        serviceTemplateValidator.validateClusters(clusterValidationTemplate);
        assertTrue(clusterValidationTemplate.getTemplateValid().isValid());
        ServiceTemplateComponent scvmm = null;
        ServiceTemplateSetting scvmmASMGuid = null;
        ServiceTemplateSetting scvmmNewName = null;
        ServiceTemplateSetting newSetting1 = null;
        ServiceTemplateComponent esxi = null;
        ServiceTemplateSetting esxiASMGuid = null;
        ServiceTemplateSetting esxiCluster = null;
        ServiceTemplateSetting newSetting2 = null;
        for (ServiceTemplateComponent component : clusterValidationTemplate.getComponents()) {
            assertTrue(component.getComponentValid().isValid());
            if (component.getComponentID().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID)) {
                scvmm = new ServiceTemplateComponent();
                scvmm.setId("SCVMM CLUSTER 2");
                scvmm.setAsmGUID("SCVMM CLUSTER 2");
                scvmm.setPuppetCertName("SCVMM CLUSTER 2");
                scvmm.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER);
                scvmm.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID);
                boolean found = false;
                for (ServiceTemplateCategory resource : component.getResources()) {
                    for (ServiceTemplateSetting parameter : resource.getParameters()) {
                        if (parameter.getId().toLowerCase().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX +
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_CLUSTER_ID)) {
                            scvmmNewName = parameter;
                            found = true;
                            break;
                        } else if (parameter.getId().toLowerCase().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID)) {
                            scvmmASMGuid = parameter;
                        }
                    }
                    if (found) {
                        ServiceTemplateCategory newCategory = new ServiceTemplateCategory();
                        newCategory.setId(resource.getId());
                        newSetting1 = new ServiceTemplateSetting();
                        newSetting1.setId(scvmmNewName.getId());
                        newSetting1.setValue(scvmmNewName.getValue());
                        newCategory.getParameters().add(newSetting1);
                        newCategory.getParameters().add(scvmmASMGuid);
                        scvmm.getResources().add(newCategory);
                        break;
                    }
                }
            } else if (component.getComponentID().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID)) {
                esxi = new ServiceTemplateComponent();
                esxi.setId("ESXI CLUSTER 2");
                esxi.setAsmGUID("ESXI CLUSTER 2");
                esxi.setPuppetCertName("ESXI CLUSTER 2");
                esxi.setType(ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER);
                esxi.setComponentID(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID);
                boolean found = false;
                for (ServiceTemplateCategory resource : component.getResources()) {
                    for (ServiceTemplateSetting parameter : resource.getParameters()) {
                        if (parameter.getId().toLowerCase().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_ID)) {
                            esxiCluster = parameter;

                            found = true;
                            break;
                        } else if (parameter.getId().toLowerCase().equalsIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID)) {
                            esxiASMGuid = parameter;
                        }
                    }
                    if (found) {
                        ServiceTemplateCategory newCategory = new ServiceTemplateCategory();
                        newCategory.setId(resource.getId());
                        newSetting2 = new ServiceTemplateSetting();
                        newSetting2.setId(esxiCluster.getId());
                        newSetting2.setValue(esxiCluster.getValue());
                        newCategory.getParameters().add(newSetting2);
                        newCategory.getParameters().add(esxiASMGuid);
                        esxi.getResources().add(newCategory);
                        break;
                    }
                }
            }
        }

        //Add duplicate scvmm component
        clusterValidationTemplate.getComponents().add(scvmm);
        serviceTemplateValidator.validateClusters(clusterValidationTemplate);
        assertFalse(scvmm.getComponentValid().isValid());
        assertFalse(clusterValidationTemplate.getTemplateValid().isValid());

        //change scvmm cluster name
        resetValid(clusterValidationTemplate);
        newSetting1.setValue("scvvmTemp2");
        serviceTemplateValidator.validateClusters(clusterValidationTemplate);
        assertTrue(clusterValidationTemplate.getTemplateValid().isValid());
        assertTrue(scvmm.getComponentValid().isValid());

        //Add duplicate esxi cluster
        clusterValidationTemplate.addComponent(esxi);
        serviceTemplateValidator.validateClusters(clusterValidationTemplate);
        assertFalse(esxi.getComponentValid().isValid());
        assertFalse(clusterValidationTemplate.getTemplateValid().isValid());

        //change esxi cluster name
        resetValid(clusterValidationTemplate);
        newSetting2.setValue("esxiTemp2");
        serviceTemplateValidator.validateClusters(clusterValidationTemplate);
        assertTrue(clusterValidationTemplate.getTemplateValid().isValid());
        assertTrue(esxi.getComponentValid().isValid());

    }

    private void resetValid(ServiceTemplate template) {
        template.getTemplateValid().setValid(true);
        template.getTemplateValid().getMessages().clear();
        for (ServiceTemplateComponent component : template.getComponents()) {
            component.getComponentValid().setValid(true);
            component.getComponentValid().getMessages().clear();
        }
    }

    @Test
    public void testIsHyperVWithChapStorage() {
        ServiceTemplateComponent serverComponent = null;
        for (ServiceTemplateComponent component : hypervChapValidationTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                serverComponent = component;
                break;
            }
        }
        assertEquals(true, serviceTemplateValidator.isHyperVWithChapStorage(hypervChapValidationTemplate, serverComponent, false));
    }

    @Test
    public void testIsEqlVolNameValid() {
        assertEquals(true, serviceTemplateValidator.isEqlVolNameValid(eql_vol_name_valid));
        assertEquals(false, serviceTemplateValidator.isEqlVolNameValid(eql_vol_name_invalid));
    }

    @Test
    public void testValidateComponentDependencies() {
        ServiceTemplateComponent serverComponent = null;
        for (ServiceTemplateComponent component : vsanStorageValidationTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                serverComponent = component;
                break;
            }
        }
        assertEquals(true, serviceTemplateValidator.checkForMinStorageComponents(serverComponent, true, true, 1));
    }

    @Test
    public void testIsFcStorageWithFcServer() {
        ServiceTemplateComponent storageComponent = null;
        ServiceTemplateComponent storageComponentValid = null;
        for (ServiceTemplateComponent component : fcStorageValidationTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.STORAGE) {
                storageComponent = component;
                break;
            }
        }
        for (ServiceTemplateComponent component : fcStorageValidationTemplateValid.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.STORAGE) {
                storageComponentValid = component;
                break;
            }
        }
        assertFalse(serviceTemplateValidator.isFcStorageWithFcServer(storageComponent, fcStorageValidationTemplate));
        assertTrue(serviceTemplateValidator.isFcStorageWithFcServer(storageComponentValid, fcStorageValidationTemplateValid));

    }

    @Test
    public void testIsDuplicatePodName() {
        serviceTemplateValidator.validateClusters(clusterValidationTemplateInvalid);
        assertFalse("Validation must fail since the template is not valid", clusterValidationTemplateInvalid.getTemplateValid().isValid());
        ServiceTemplateComponent clusterComponent = null;
        String clusterComponentErrorMessageCode = null;
        for (ServiceTemplateComponent component : clusterValidationTemplateInvalid.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.CLUSTER) {
                clusterComponent = component;
                break;
            }
        }
        assertNotNull(clusterComponent);
        assertNotNull(clusterComponent.getComponentValid());
        assertFalse("Validation must fail since the cluster component is not valid", clusterComponent.getComponentValid().isValid());
        assertFalse("Validation must fail since the cluster component has error messages", CollectionUtils.isEmpty(clusterComponent.getComponentValid().getMessages()));
        for (AsmDetailedMessage message : clusterComponent.getComponentValid().getMessages()) {
            if (AsmManagerMessages.MsgCodes.ASM00342.name().equals(message.getMessageCode())) {
                clusterComponentErrorMessageCode = message.getMessageCode();
            }
        }
        assertNotNull(clusterComponentErrorMessageCode);
    }
    
    @Test
    public void testMissingDomainSettings() {
    	//testing for invalid template
    	resetValid(domainSettingsValidationTemplate); 
    	serviceTemplateValidator.validateServerComponents(domainSettingsValidationTemplate, serviceTemplateValidator.getServiceTemplateUtil().mapReposToTasks());
        assertFalse("template is invalid", domainSettingsValidationTemplate.getTemplateValid().isValid());
    	        
        //testing for invalid component and error message code
        ServiceTemplateComponent serverComponent = null;
        String serverComponentErrorMessageCode = null;        
        for (ServiceTemplateComponent component : domainSettingsValidationTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                serverComponent = component;
                break;
            }
        }              
        assertNotNull(serverComponent);
        assertNotNull(serverComponent.getComponentValid());
        assertFalse("Error messages in template", serverComponent.getComponentValid().getMessages().isEmpty());
        assertFalse("Server component is not valid", serverComponent.getComponentValid().isValid());
        assertFalse("Server component has error messages", CollectionUtils.isEmpty(serverComponent.getComponentValid().getMessages()));
        for (AsmDetailedMessage message : serverComponent.getComponentValid().getMessages()) {
            if (AsmManagerMessages.MsgCodes.ASM00345.name().equals(message.getMessageCode())) {
            	serverComponentErrorMessageCode = message.getMessageCode();
            }
        }
        assertNotNull("Cannot find missing domain error message", serverComponentErrorMessageCode);
        
        //testing for valid template
        resetValid(domainSettingsValidationTemplate);
        ServiceTemplateSetting domainSetting = domainSettingsValidationTemplate.getTemplateSetting(serverComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_DN_ID);
        domainSetting.setValue("");
        ServiceTemplateSetting domainSettingPw = domainSettingsValidationTemplate.getTemplateSetting(serverComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_PASSWORD_ID);
        domainSettingPw.setValue("");
        ServiceTemplateSetting domainSettingPwConfirm = domainSettingsValidationTemplate.getTemplateSetting(serverComponent, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_DOMAIN_CONFIRM_PASSWORD_ID);
        domainSettingPwConfirm.setValue("");
        serviceTemplateValidator.validateServerComponents(domainSettingsValidationTemplate, serviceTemplateValidator.getServiceTemplateUtil().mapReposToTasks());
        assertTrue("Template is valid", domainSettingsValidationTemplate.getTemplateValid().isValid());
    }

    // windows server and nonraid
    @Test
    public void testNonraid() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00322.name());
    }

    // windows server and vcenter cluster
    @Test
    public void testASM00324() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00324.name());
    }

    // invalid hostname
    @Test
    public void testASM00123() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00123.name());
    }

    // invalid esx host name
    @Test
    public void testASM00313() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00313.name());
    }

    // invalid esx host name
    @Test
    public void testASM00271() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00271.name());
    }

    // duplicate port group
    @Test
    public void testASM00346() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00346.name());
    }

    // duplicate VDS name
    @Test
    public void testASM00347() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM00347.name());
    }
    
    //invalid size eql vnx and netapp component
    @Test
    public void testASM0098() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM0098.name());
    }

    //invalid size compellent component 
    @Test
    public void testASM0099() throws IOException {
        runTestByMessageCode(AsmManagerMessages.MsgCodes.ASM0099.name());
    }

    private void runTestByMessageCode(String msgCode) throws IOException {
        URL url = ServiceTemplateServiceTest.class.getClassLoader().getResource("ServiceTemplateValidatorTest/"+msgCode+".xml");
        String templateName = Resources.toString(url, Charsets.UTF_8);

        ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, templateName);
        resetValid(template);

        serviceTemplateValidator.validateTemplate(template, new ServiceTemplateValidator.ValidationOptions(false, true, true));
        assertFalse("Expect invalid", template.getTemplateValid().isValid());
        for (ServiceTemplateComponent component: template.getComponents()) {
            if (!component.getComponentValid().isValid()) {
                boolean found = false;
                for (AsmDetailedMessage msg: component.getComponentValid().getMessages()) {
                    if (msg.getMessageCode().equals(msgCode)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("Not found expected message: " + msgCode);
                }
            }
        }
    }

    public static ServiceTemplateValidator mockServiceTemplateValidator() {
        INetworkService networkService = mock(INetworkService.class);
        Network[] networks = new Network[8];

        Network pxe = new Network();
        pxe.setId("PXE");
        pxe.setName("PXE");
        pxe.setType(NetworkType.PXE);
        networks[0] = pxe;

        Network hypervisor = new Network();
        hypervisor.setId("HYPERVISOR_MANAGEMENT");
        hypervisor.setName("HYPERVISOR_MANAGEMENT");
        hypervisor.setType(NetworkType.HYPERVISOR_MANAGEMENT);
        networks[1] = hypervisor;

        Network san = new Network();
        san.setId("STORAGE_ISCSI_SAN");
        san.setName("STORAGE_ISCSI_SAN");
        san.setType(NetworkType.STORAGE_ISCSI_SAN);
        networks[2] = san;

        Network iscsi_dhcp = new Network();
        iscsi_dhcp.setId("ISCSI_DHCP");
        iscsi_dhcp.setName("STORAGE_ISCSI_SAN");
        iscsi_dhcp.setType(NetworkType.STORAGE_ISCSI_SAN);
        iscsi_dhcp.setStatic(false);
        networks[3] = iscsi_dhcp;

        Network iscsi_static = new Network();
        iscsi_static.setId("ISCSI_STATIC");
        iscsi_static.setName("STORAGE_ISCSI_SAN");
        iscsi_static.setType(NetworkType.STORAGE_ISCSI_SAN);
        iscsi_static.setStatic(true);
        networks[4] = iscsi_static;

        Network vmotion = new Network();
        vmotion.setId("HYPERVISOR_MIGRATION");
        vmotion.setName("HYPERVISOR_MIGRATION");
        vmotion.setType(NetworkType.HYPERVISOR_MIGRATION);
        networks[5] = vmotion;

        Network workload1 = new Network();
        workload1.setId("PRIVATE_LAN");
        workload1.setName("PRIVATE_LAN");
        workload1.setType(NetworkType.PRIVATE_LAN);
        networks[6] = workload1;

        Network workload = new Network();
        workload.setId("PUBLIC_LAN");
        workload.setName("PUBLIC_LAN");
        workload.setType(NetworkType.PUBLIC_LAN);
        networks[7] = workload;

        when(networkService.getNetworks("name", null, null, null)).thenReturn(networks);

        when(networkService.getNetworks("name", null, null, null)).thenReturn(networks);
        when(networkService.getNetwork("PUBLIC_LAN")).thenAnswer(returnOfNetwork(workload));
        when(networkService.getNetwork("PRIVATE_LAN")).thenAnswer(returnOfNetwork(workload1));
        when(networkService.getNetwork("ISCSI_STATIC")).thenAnswer(returnOfNetwork(iscsi_static));
        when(networkService.getNetwork("ISCSI_DHCP")).thenAnswer(returnOfNetwork(iscsi_dhcp));
        when(networkService.getNetwork("PXE")).thenAnswer(returnOfNetwork(pxe));
        when(networkService.getNetwork("HYPERVISOR_MANAGEMENT")).thenAnswer(returnOfNetwork(hypervisor));

        EncryptionDAO encryptionDAO = mock(EncryptionDAO.class);

        ServiceTemplateValidator serviceTemplateValidator = new ServiceTemplateValidator();
        serviceTemplateValidator.setNetworkService(networkService);
        serviceTemplateValidator.setServiceTemplateUtil(ServiceTemplateUtilTest.mockServiceTemplateUtil(encryptionDAO, networkService));
        serviceTemplateValidator.setOsRepositoryUtil(OSRepositoryUtilTest.mockOSRepositoryUtilTest());
        return serviceTemplateValidator;
    }

    private static Answer<Network> returnOfNetwork(final Network source) {
        return new Answer<Network>() {
            public Network answer(InvocationOnMock invocation) {
                return cloneNetwork(source);
            }
        };
    }

    private static Network cloneNetwork(Network networkSource) {
        Network network = new Network();
        network.setId(networkSource.getId());
        network.setName(networkSource.getName());
        network.setDescription(networkSource.getDescription());
        network.setType(networkSource.getType());
        network.setVlanId(networkSource.getVlanId());
        network.setStatic(networkSource.isStatic());
        return network;
    }
}
