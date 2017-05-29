/**
 * 
 */
package com.dell.asm.asmcore.asmmanager.db;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.db.entity.*;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Yao_Lu1
 * 
 */
public class DeploymentDAOIT {
    private DeploymentDAO dao = DeploymentDAO.getInstance();
    private DeviceInventoryDAO inventoryDao = new DeviceInventoryDAO();

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link DeviceInventoryDAO#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(dao);
    }


    @Test
    public void testCreateDeviceInventory() throws IllegalAccessException, AsmManagerCheckedException, InvocationTargetException {
        List<DeploymentEntity> preexistingDeployments = dao.getAllDeployment(DeploymentDAO.ALL_ENTITIES);

        DeploymentEntity entity = new DeploymentEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName("IAMTHEEGGMAN" + UUID.randomUUID().toString());

        DeviceInventoryEntity createdEntity = createDeviceInventoryEntity(UUID.randomUUID().toString());

        entity.getDeployedDevices().add(createdEntity);

        VMRefEntity vmref = new VMRefEntity();
        //vmref.setDeploymentId(entity.getId());
        vmref.setVmId("VM-10.2.3.6" + UUID.randomUUID().toString());
        entity.getVmList().add(vmref);


        VMRefEntity vmref2 = new VMRefEntity();
        //vmref2.setDeploymentId(entity.getId());
        vmref2.setVmId("VM-10.2.3.5" + UUID.randomUUID().toString());
        entity.getVmList().add(vmref2);

        try {
            dao.createDeployment(entity);
        } catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                fail();
            }
        }

        List<DeploymentEntity> allDeployments = dao.getAllDeployment(DeploymentDAO.ALL_ENTITIES);
        assertEquals(preexistingDeployments.size() + 1, allDeployments.size());

        DeploymentEntity fetchedDeployment = dao.getDeployment(entity.getId(),DeploymentDAO.ALL_ENTITIES);
        assertNotNull(fetchedDeployment);
        assertEquals(2, fetchedDeployment.getVmList().size());
        assertEquals(1, fetchedDeployment.getDeployedDevices().size());


        fetchedDeployment.setExpirationDate(new GregorianCalendar());
        fetchedDeployment.setJobId("9999");
        fetchedDeployment.setMarshalledTemplateData("<template>iamatempalte</template>");

        VMRefEntity toRemove = fetchedDeployment.getVmList().iterator().next();
        //toRemove.setDeploymentId(null);
        fetchedDeployment.getVmList().remove(toRemove);
        assertNotNull(fetchedDeployment);
        assertEquals(1, fetchedDeployment.getVmList().size());

        dao.updateDeployment(fetchedDeployment);

//        dao.deleteDeployment(entity.getId());

//        List<DeploymentEntity> postDeleteDeployments = dao.getAllDeployment();
//        assertEquals(preexistingDeployments.size(), postDeleteDeployments.size());
    }

    private DeviceInventoryEntity createDeviceInventoryEntity(String refId) {
        DeviceInventoryEntity deviceEntity = new DeviceInventoryEntity();
        deviceEntity.setRefId(refId);
        deviceEntity.setDeviceType(DeviceType.ChassisM1000e);
        deviceEntity.setServiceTag(UUID.randomUUID().toString());
        deviceEntity.setIpAddress("10.128.129.123");
        deviceEntity.setModel("Unknown");
        deviceEntity.setRefType("Unknown");
        deviceEntity.setDisplayName("None");
        deviceEntity.setState(DeviceState.READY);
        deviceEntity.setDiscoverDeviceType(DiscoverDeviceType.CMC);

        DeviceInventoryEntity createdEntity = null;
        try {
            createdEntity = inventoryDao.createDeviceInventory(deviceEntity);
        } catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                fail();
            }
        }
        return createdEntity;
    }

    @Test
    public void testUpdateDeviceInventory() throws IllegalAccessException, AsmManagerCheckedException, InvocationTargetException {
        DeploymentEntity entity = new DeploymentEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName("IAMTHEEGGMAN" + UUID.randomUUID().toString());

        String orig2Id = UUID.randomUUID().toString();
        DeviceInventoryEntity orig2 = createDeviceInventoryEntity(orig2Id);
        entity.getDeployedDevices().add(orig2);

        String origId = UUID.randomUUID().toString();
        DeviceInventoryEntity createdEntity = createDeviceInventoryEntity(origId);

        entity.getDeployedDevices().add(createdEntity);

        try {
            dao.createDeployment(entity);
        } catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                fail();
            }
        }

        DeploymentEntity fetchedDeployment = dao.getDeployment(entity.getId(),DeploymentDAO.ALL_ENTITIES);
        assertEquals(2, fetchedDeployment.getDeployedDevices().size());
        DeviceInventoryEntity orig = null;
        for (DeviceInventoryEntity d : fetchedDeployment.getDeployedDevices()) {
            if (d.getRefId().equals(createdEntity.getRefId())) {
                orig = d;
            }
        }
        fetchedDeployment.getDeployedDevices().remove(orig);

        String newId = UUID.randomUUID().toString();
        DeviceInventoryEntity newEntity = createDeviceInventoryEntity(newId);
        fetchedDeployment.getDeployedDevices().add(newEntity);

        dao.updateDeployment(fetchedDeployment);

        fetchedDeployment = dao.getDeployment(entity.getId(),DeploymentDAO.ALL_ENTITIES);
        assertEquals(2, fetchedDeployment.getDeployedDevices().size());
        DeviceInventoryEntity found = null;
        for (DeviceInventoryEntity d : fetchedDeployment.getDeployedDevices()) {
            if (d.getRefId().equals(newEntity.getRefId())) {
                found = d;
            }
            if (d.getRefId().equals(createdEntity.getRefId())) {
                fail("Found original one which was removed");
            }
        }
        assertNotNull(found);
    }
}
