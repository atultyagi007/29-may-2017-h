package com.dell.asm.asmcore.asmmanager.util.template.adjuster;

import com.dell.asm.asmcore.asmmanager.AbstractAsmManagerTest;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;

import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.identitypool.api.network.model.Network;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ClusterAdjusterTest extends AbstractAsmManagerTest {

    private ComponentBuilder component_cluster;
    private ComponentBuilder component_server;

    private INetworkService networkProxy;
    private Network networkISCSI1;
    private Network networkISCSI2;
    private Network networkHWM;
    private Network networkCluster;
    private Network networkWorkload;
    private Network networkPXE;
    private Network networkVMotion;

    private ServiceTemplate templateDiffISCSI;
    private ServiceTemplate templateSameISCSI;
    private ServiceTemplate templateISCSIOrder;

    @Before
    public void setUp() throws IOException {

        networkISCSI1 = new Network();
        networkISCSI1.setId("ff808081562455c00156287e2c940001");
        networkISCSI1.setName("ISCSI 1");
        networkISCSI1.setDescription("");
        networkISCSI1.setType(NetworkType.STORAGE_ISCSI_SAN);
        networkISCSI1.setVlanId(16);

        networkISCSI2 = new Network();
        networkISCSI2.setId("ff80808155ff50220155ff55af8b009f");
        networkISCSI2.setName("ISCSI 2");
        networkISCSI2.setDescription("");
        networkISCSI2.setType(NetworkType.STORAGE_ISCSI_SAN);
        networkISCSI2.setVlanId(17);

        networkWorkload = new Network();
        networkWorkload.setId("ff80808155ff50220155ff55a6590037");
        networkWorkload.setName("Workload");
        networkWorkload.setDescription("");
        networkWorkload.setType(NetworkType.PUBLIC_LAN);
        networkWorkload.setVlanId(18);

        networkVMotion = new Network();
        networkVMotion.setId("ff80808155ff50220155ff55abb80039");
        networkVMotion.setName("VMotion");
        networkVMotion.setDescription("");
        networkVMotion.setType(NetworkType.HYPERVISOR_MIGRATION);
        networkVMotion.setVlanId(19);

        networkPXE = new Network();
        networkPXE.setId("ff80808155ff50220155ff55abb80039");
        networkPXE.setName("PXE");
        networkPXE.setDescription("");
        networkPXE.setType(NetworkType.PXE);
        networkPXE.setVlanId(20);

        networkCluster = new Network();
        networkCluster.setId("ff80808155ff50220155ff55b8050106");
        networkCluster.setName("Cluster");
        networkCluster.setDescription("");
        networkCluster.setType(NetworkType.HYPERVISOR_CLUSTER_PRIVATE);
        networkCluster.setVlanId(21);

        networkHWM = new Network();
        networkHWM.setId("ff80808155ff50220155ff55bbd90109");
        networkHWM.setName("HYPERVISOR_MANAGEMENT");
        networkHWM.setDescription("");
        networkHWM.setType(NetworkType.HYPERVISOR_MANAGEMENT);
        networkHWM.setVlanId(22);

        networkProxy = mock(INetworkService.class);
        when(networkProxy.getNetwork("ff808081562455c00156287e2c940001")).thenAnswer(returnOfNetwork(networkISCSI1));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55af8b009f")).thenAnswer(returnOfNetwork(networkISCSI2));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55a6590037")).thenAnswer(returnOfNetwork(networkWorkload));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55abb80039")).thenAnswer(returnOfNetwork(networkVMotion));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55b2b70105")).thenAnswer(returnOfNetwork(networkPXE));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55b8050106")).thenAnswer(returnOfNetwork(networkCluster));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55bbd90109")).thenAnswer(returnOfNetwork(networkHWM));

        ClusterAdjuster.getInstance().setNetworkProxy(networkProxy);

        component_cluster = new ComponentBuilder("component_cluster")
                .addComponentType(ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER)
                .addAssociatedComponent("component_server", "")
                .addResource(new ResourceBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID)
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID).addHideFromTemplate(true))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID).addHideFromTemplate(true))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID).addHideFromTemplate(true))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID).addHideFromTemplate(true))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID).addHideFromTemplate(true))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID).addHideFromTemplate(true))
                );

        component_server = new ComponentBuilder("component_server")
                .addComponentType(ServiceTemplateComponent.ServiceTemplateComponentType.SERVER)
                .addAssociatedComponent("component_cluster", "")
                .addAssociatedComponent("component_storage1", "")
                .addAssociatedComponent("component_storage2", "")
                .addResource(new ResourceBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE)
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID)
                                .addValue(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_FLASH))
                );

        URL url = Resources.getResource("util/clusterAdjusterTemplate1.json");
        String xml = Resources.toString(url, Charsets.UTF_8);
        templateDiffISCSI = MarshalUtil.fromJSON(ServiceTemplate.class, xml);

        url = Resources.getResource("util/clusterAdjusterTemplate2.json");
        xml = Resources.toString(url, Charsets.UTF_8);
        templateSameISCSI = MarshalUtil.fromJSON(ServiceTemplate.class, xml);

        url = Resources.getResource("util/clusterAdjusterTemplate3.json");
        xml = Resources.toString(url, Charsets.UTF_8);
        templateISCSIOrder = MarshalUtil.fromJSON(ServiceTemplate.class, xml);

    }

    private void setupVSANNetworks() {
        networkISCSI1 = new Network();
        networkISCSI1.setId("ff808081562455c00156287e2c940001");
        networkISCSI1.setName("VSAN 1");
        networkISCSI1.setDescription("");
        networkISCSI1.setType(NetworkType.VSAN);
        networkISCSI1.setVlanId(16);

        networkISCSI2 = new Network();
        networkISCSI2.setId("ff80808155ff50220155ff55af8b009f");
        networkISCSI2.setName("VSAN 2");
        networkISCSI2.setDescription("");
        networkISCSI2.setType(NetworkType.VSAN);
        networkISCSI2.setVlanId(17);

        when(networkProxy.getNetwork("ff808081562455c00156287e2c940001")).thenAnswer(returnOfNetwork(networkISCSI1));
        when(networkProxy.getNetwork("ff80808155ff50220155ff55af8b009f")).thenAnswer(returnOfNetwork(networkISCSI2));
    }

    Answer<Network> returnOfNetwork(final Network source) {
        return new Answer<Network>() {
            public Network answer(InvocationOnMock invocation) {
                return cloneNetwork(source);
            }
        };
    }

    private Network cloneNetwork(Network networkSource) {
        Network network = new Network();
        network.setId(networkSource.getId());
        network.setName(networkSource.getName());
        network.setDescription(networkSource.getDescription());
        network.setType(networkSource.getType());
        network.setVlanId(network.getVlanId());
        return network;
    }

    private ComponentBuilder buildStorageComponent(String componentID, String title) {
        ComponentBuilder component = new ComponentBuilder(componentID)
                .addComponentType(ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE)
                .addResource(new ResourceBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID)
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID)
                                .addValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW))
                        .addParameter(new ParameterBuilder(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW).addValue(title))
                );
        return component;
    }

    @Test
    public void testAssignMissingComponentId() {
        final ServiceTemplate svcTemplate = new ServiceTemplateBuilder("TemplateComponentIdMissing")
                .addComponent(component_cluster)
                .addComponent(component_server)
                .build();

        ServiceTemplateUtil.refineComponents(svcTemplate, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());

        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID + " is hidden", svcTemplate.findComponentById("component_cluster").
                getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_COMPRESSION_ID).isHideFromTemplate());
        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID + " is hidden", svcTemplate.findComponentById("component_cluster").
                getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURE_ID).isHideFromTemplate());
        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID + " is hidden", svcTemplate.findComponentById("component_cluster").
                getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_FAILURES_NUM_ID).isHideFromTemplate());

    }

    @Test
    public void testRefineClusterForSDRS() {
        final ServiceTemplate clusterTemplate = new ServiceTemplateBuilder("TemplateComponentIdMissing")
                .addComponent(component_cluster)
                .addComponent(component_server)
                .addComponent(buildStorageComponent("component_storage1", "VPOD1"))
                .addComponent(buildStorageComponent("component_storage2", "VPOD2"))
                .build();

        ServiceTemplateUtil.refineComponents(clusterTemplate, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());

        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID + " is hidden", clusterTemplate.findComponentById("component_cluster")
                .getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID).isHideFromTemplate());
        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID + " is hidden", clusterTemplate.findComponentById("component_cluster")
                .getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID).isHideFromTemplate());
        assertFalse("Setting " + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID + " is hidden", clusterTemplate.findComponentById("component_cluster")
                .getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID).isHideFromTemplate());

        List<String> wantedOptions = new ArrayList<>();
        wantedOptions.add("component_storage2");
        wantedOptions.add("component_storage1");

        List<String> foundOptions = new ArrayList<>();

        ServiceTemplateSetting membersParam = clusterTemplate.findComponentById("component_cluster")
                .getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DS_POD_MEMBERS_ID);
        for (ServiceTemplateOption option : membersParam.getOptions()) {
            foundOptions.add(option.getValue());
        }

        assertEquals(wantedOptions, foundOptions);

    }


    @Test
    public void testDiffISCSI() {
        ServiceTemplateUtil.refineComponents(templateDiffISCSI, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());
        ServiceTemplateComponent cluster = templateDiffISCSI.findComponentById("5C33C92D-031F-48CF-B4EF-6C0834DCE639");

        ServiceTemplateSetting iscsiPG1 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f-ff808081562455c00156287e2c940001::ff808081562455c00156287e2c940001::2");
        ServiceTemplateSetting iscsiPG2 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f-ff808081562455c00156287e2c940001::ff80808155ff50220155ff55af8b009f::1");

        assertEquals("Expecting ISCSI 1 Port Group", "ISCSI 1 Port Group", iscsiPG1.getDisplayName());
        assertEquals("Expecting ISCSI 2 Port Group", "ISCSI 2 Port Group", iscsiPG2.getDisplayName());
    }

    @Test
    public void testSameISCSI() {
        ServiceTemplateUtil.refineComponents(templateSameISCSI, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());
        ServiceTemplateComponent cluster = templateSameISCSI.findComponentById("5C33C92D-031F-48CF-B4EF-6C0834DCE639");

        ServiceTemplateSetting iscsiPG1 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f::ff80808155ff50220155ff55af8b009f::1");
        ServiceTemplateSetting iscsiPG2 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f::ff80808155ff50220155ff55af8b009f::2");

        assertEquals("Expecting ISCSI 2 Port Group 1", "ISCSI 2 Port Group 1", iscsiPG1.getDisplayName());
        assertEquals("Expecting ISCSI 2 Port Group 2", "ISCSI 2 Port Group 2", iscsiPG2.getDisplayName());
    }

    @Test
    public void testISCSIOrder() {
        ServiceTemplateUtil.refineComponents(templateISCSIOrder, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());
        ServiceTemplateComponent cluster = templateISCSIOrder.findComponentById("5C33C92D-031F-48CF-B4EF-6C0834DCE639");

        ServiceTemplateCategory vdsCat = cluster.getTemplateResource("asm::cluster::vds");
        assertNotNull("No VDS category", vdsCat);

        int counter = 0;
        for (ServiceTemplateSetting setting: vdsCat.getParameters()) {
            if ("VDS Name".equals(setting.getDisplayName()) &&
                !setting.isHideFromTemplate()) {
                counter ++;
            }
        }
        assertEquals("Unexpected number of VDS Names", 1, counter);
    }

    @Test
    public void testDiffVSAN() {
        setupVSANNetworks();

        ServiceTemplateUtil.refineComponents(templateDiffISCSI, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());
        ServiceTemplateComponent cluster = templateDiffISCSI.findComponentById("5C33C92D-031F-48CF-B4EF-6C0834DCE639");

        ServiceTemplateSetting vsanPG1 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f-ff808081562455c00156287e2c940001::ff808081562455c00156287e2c940001::2");
        ServiceTemplateSetting vsanPG2 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f-ff808081562455c00156287e2c940001::ff80808155ff50220155ff55af8b009f::1");
        assertNull("VSAN 1 has more than 1 port group",vsanPG1);
        assertNull("VSAN 2 has more than 1 port group",vsanPG2);

        vsanPG1 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff808081562455c00156287e2c940001::ff808081562455c00156287e2c940001::1");
        vsanPG2 = cluster.getParameter("asm::cluster::vds", "vds_pg::ff80808155ff50220155ff55af8b009f::ff80808155ff50220155ff55af8b009f::1");

        assertNotNull("VSAN 1 port group does not exist",vsanPG1);
        assertNotNull("VSAN 2 port group does not exist",vsanPG2);
    }


    @Test
    public void testVSANOrder() {
        setupVSANNetworks();

        ServiceTemplateUtil.refineComponents(templateISCSIOrder, null, ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER.getLabel());
        ServiceTemplateComponent cluster = templateISCSIOrder.findComponentById("5C33C92D-031F-48CF-B4EF-6C0834DCE639");

        ServiceTemplateCategory vdsCat = cluster.getTemplateResource("asm::cluster::vds");
        assertNotNull("No VDS category", vdsCat);

        int counter = 0;
        for (ServiceTemplateSetting setting: vdsCat.getParameters()) {
            if ("VDS Name".equals(setting.getDisplayName()) &&
                    !setting.isHideFromTemplate()) {
                counter ++;
            }
        }
        assertEquals("Unexpected number of VDS Names", 2, counter);
    }

}