package com.dell.asm.asmdeployer.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;

/**
 * IAsmDeployerServiceIT is an integration test of the asm-deployer REST services using the
 * IAsmDeployerService interface. Requires the asm-deployer REST service to be available
 * at the location configured in setupServer()
 */
public class IAsmDeployerServiceIT {

    public static final String DEVICE_CERT_NAME = "equallogic-test-env05";

    @BeforeClass
    public static void setupServer() throws Exception {
        AsmManagerApp.asmDeployerApiUrl = "http://asm-deployer-api:8083";
    }

    /**
     * Uses a "TEST" component to:
     *
     * - create a "deployment" that contains a puppet resource which writes a file to /tmp/IAsmDeployerServiceIT-*.txt
     * - retries the deployment to change the contents of the file
     * - tears down the deployment to remove the file
     * - deletes the deployment
     *
     * @throws InterruptedException
     */
    @Test
    public void testDeployment() throws InterruptedException {
        IAsmDeployerService proxy = ProxyUtil.getAsmDeployerProxy();

        String deploymentId = UUID.randomUUID().toString();
        String templateId = UUID.randomUUID().toString();
        String componentId = UUID.randomUUID().toString();

        Deployment deployment = new Deployment();
        deployment.setId(deploymentId);
        deployment.setDeploymentName(this.getClass().getSimpleName() + " Test " + deploymentId);

        ServiceTemplate template = new ServiceTemplate();
        template.setId(templateId);
        template.setTemplateName(this.getClass().getSimpleName() + " Template " + deploymentId);
        template.setCategory("gavin");

        ServiceTemplateCategory category = new ServiceTemplateCategory();
        category.setId("file");
        ServiceTemplateSetting titleParam = new ServiceTemplateSetting("title",
                "/tmp/" + this.getClass().getSimpleName() + "-" + deploymentId + ".txt",
                ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        category.getParameters().add(titleParam);
        ServiceTemplateSetting ensureParam = new ServiceTemplateSetting("ensure",
                "present", ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        category.getParameters().add(ensureParam);
        ServiceTemplateSetting contentParam = new ServiceTemplateSetting("content",
                "Deployment id " + deploymentId + "\n",
                ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
        category.getParameters().add(contentParam);

        ServiceTemplateComponent component = new ServiceTemplateComponent();
        component.setId(componentId);
        component.setComponentID("component-1");
        component.setPuppetCertName("test-" + deploymentId);
        component.setName("Test Component");
        component.setType(ServiceTemplateComponent.ServiceTemplateComponentType.TEST);
        component.getResources().add(category);

        template.getComponents().add(component);

        deployment.setServiceTemplate(template);

        // Create deployment
        AsmDeployerStatus created = proxy.createDeployment(deployment);
        assertNotNull(created);
        assertEquals(deploymentId, created.getId());
        assertEquals(DeploymentStatusType.IN_PROGRESS, created.getStatus());
        assertNotNull(created.getComponents());
        assertEquals(1, created.getComponents().size());

        // Poll for completion
        pollForCompletion(proxy, deploymentId);

        // Retry deployment
        contentParam.setValue("RETRY: " + contentParam.getValue());

        AsmDeployerStatus retry = proxy.retryDeployment(deploymentId, deployment);
        assertNotNull(retry);
        assertEquals(deploymentId, retry.getId());
        assertEquals(DeploymentStatusType.IN_PROGRESS, retry.getStatus());
        assertNotNull(retry.getComponents());
        assertEquals(1, retry.getComponents().size());

        // Poll for completion
        pollForCompletion(proxy, deploymentId);

        // Tear-down
        deployment.setTeardown(true);
        ensureParam.setValue("absent");
        AsmDeployerStatus teardown = proxy.retryDeployment(deploymentId, deployment);
        assertNotNull(teardown);
        assertEquals(deploymentId, teardown.getId());
        assertEquals(DeploymentStatusType.IN_PROGRESS, teardown.getStatus());
        assertNotNull(teardown.getComponents());
        assertEquals(1, teardown.getComponents().size());

        // Poll for completion
        pollForCompletion(proxy, deploymentId);

        // Delete
        proxy.deleteDeployment(deploymentId);
    }

    private void pollForCompletion(IAsmDeployerService proxy, String deploymentId) throws InterruptedException {
        AsmDeployerStatus status = new AsmDeployerStatus();
        status.setStatus(DeploymentStatusType.IN_PROGRESS);
        while (status.getStatus().equals(DeploymentStatusType.IN_PROGRESS)) {
            Thread.sleep(1000);
            status = proxy.getDeploymentStatus(deploymentId);
            assertEquals(deploymentId, status.getId());
        }

        assertEquals(DeploymentStatusType.COMPLETE, status.getStatus());
        for (AsmDeployerComponentStatus componentStatus : status.getComponents()) {
            assertEquals(DeploymentStatusType.COMPLETE, componentStatus.getStatus());
        }
    }

    /**
     * Exercises asm-deployer device service CRUD operations
     *
     * @throws InterruptedException if interrupted
     */
    @Test
    public void testDevice() throws InterruptedException {
        IAsmDeployerService proxy = ProxyUtil.getAsmDeployerProxy();

        AsmDeployerDevice device = new AsmDeployerDevice();
        device.setCertName(DEVICE_CERT_NAME);
        device.setHost("172.17.5.10");
        device.setUsername("grpadmin");
        device.setPassword("6e458c68f8fe6d2dcfd6baefb989fee1PeL4wiq1jL4X426Ll7PnSw==");
        device.setProvider("equallogic");
        device.setScheme("https");

        try {
            AsmDeployerDevice created = proxy.createDevice(device);
            assertNotNull(created);

            AsmDeployerDevice got = created;
            while (!AsmDeviceStatusType.SUCCESS.equals(got.getDiscoveryStatus())) {
                if (AsmDeviceStatusType.FAILED.equals(got.getDiscoveryStatus())) {
                    throw new RuntimeException("Operation failed: " + got);
                }
                System.out.println();
                Thread.sleep(1000);
                got = proxy.getDevice(device.getCertName());

                // Password shouldn't get passed back
                assertNull(got.getPassword());
                assertNull(got.getEncodedPassword());

                System.out.println("Before update: " + got);
                for (Map.Entry<String, String> entry : got.getFacts().entrySet()) {
                    System.out.println("\"" + entry.getKey() + "\" => \"" + entry.getValue() + "\"");
                }
            }
        } catch (WebApplicationException e) {
            assertEquals(Response.Status.CONFLICT.getStatusCode(), e.getResponse().getStatus());
        }

        device.setHost("172.17.4.10");
        AsmDeployerDevice updated = proxy.updateDevice(device.getCertName(), device);
        assertNotNull(updated);
        assertEquals(device.getHost(), updated.getHost());

        AsmDeployerDevice got = updated;
        while (!AsmDeviceStatusType.SUCCESS.equals(got.getDiscoveryStatus())) {
            assertFalse("Discovery failed", AsmDeviceStatusType.FAILED.equals(got.getDiscoveryStatus()));
            System.out.println();
            Thread.sleep(1000);
            got = proxy.getDevice(device.getCertName());
        }

        got = proxy.getDevice(device.getCertName());
        System.out.println("After update: " + got);
        for (Map.Entry<String, String> entry : got.getFacts().entrySet()) {
            System.out.println("\"" + entry.getKey() + "\" => \"" + entry.getValue() + "\"");
        }

        proxy.deleteDevice(DEVICE_CERT_NAME);

        try {
            got = proxy.getDevice(DEVICE_CERT_NAME);
            long timeoutMillis = 10 * 60 * 1000;
            long start = new Date().getTime();
            long now = new Date().getTime();
            while ((now - start) <= timeoutMillis) {
                if (AsmDeviceStatusType.FAILED.equals(got.getDiscoveryStatus())) {
                    throw new RuntimeException("Operation failed: " + got);
                }
                System.out.println();
                Thread.sleep(1000);
                got = proxy.getDevice(device.getCertName());
            }
            fail("Device should have disappeared after delete");
        } catch (WebApplicationException e) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
        }
    }

    @Test
    public void testGetNonExistentDevice() {
        IAsmDeployerService proxy = ProxyUtil.getAsmDeployerProxy();
        try {
            proxy.getDevice("cert-foo-bar-baz");
            fail("Should not successfully get non-existent device");
        } catch (WebApplicationException e) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
        }
    }
}
