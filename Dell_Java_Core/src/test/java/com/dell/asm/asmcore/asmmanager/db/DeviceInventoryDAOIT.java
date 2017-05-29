/**
 * 
 */
package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.*;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceLastJobStateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.JobType;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;

/**
 * @author Yao_Lu1
 * 
 */
public class DeviceInventoryDAOIT {
    private DeviceInventoryDAO dao = new DeviceInventoryDAO();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(dao);
    }

    /**
     * Test method for
     * {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#createDeviceInventory(com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity)}
     * .
     */
    @Test
    public void testCreateDeviceInventory() {
        DeviceInventoryEntity entity = new DeviceInventoryEntity();
        entity.setRefId("1");
        entity.setDeviceType(DeviceType.ChassisM1000e);
        entity.setServiceTag("ServiceTag");
        entity.setIpAddress("10.128.129.123");
        entity.setModel("Unknown");
        entity.setRefType("Unknown");
        entity.setDisplayName("None");
        entity.setState(DeviceState.READY);
        entity.setDiscoverDeviceType(DiscoverDeviceType.CMC);
        try {
            dao.createDeviceInventory(entity);
        } catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                fail();
            }
        }
        entity.setRefId("2");
        try {
            dao.createDeviceInventory(entity);
        } catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_SERVICETAG) {
                fail();
            }
        }

    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#getDeviceInventory(java.lang.String, java.lang.String)}.
     * @throws AsmManagerCheckedException 
     */
    @Test
    public void testGetDeviceInventory() {
        DeviceInventoryEntity result = dao.getDeviceInventory("1");
        assertNotNull(result);
        assertEquals(DeviceState.READY, result.getState());
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#getAllDeviceInventory()}.
     * @throws AsmManagerCheckedException 
     */
    @Test
    public void testGetAllDeviceInventory() {
        List<DeviceInventoryEntity> results = dao.getAllDeviceInventory();
        assertTrue(results.size() > 0);
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#updateDeviceInventory()}.
     * @throws AsmManagerCheckedException 
     */
    @Test
    public void testUpdateDeviceInventory() throws AsmManagerCheckedException {
        DeviceInventoryEntity entity = dao.getDeviceInventory("1");
        entity.setModel("New Model");
        dao.updateDeviceInventory(entity);
        entity = dao.getDeviceInventory("1");
        assertEquals("New Model", entity.getModel());
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#deleteDeviceInventory()}.
     * @throws AsmManagerCheckedException 
     */
    @Test
    public void testDeleteDeviceInventory() {
        dao.deleteDeviceInventory("1");
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#deleteDeviceInventory()}.
     * @throws AsmManagerCheckedException 
     */
    @Test
    public void testDeleteDeviceInventoryNotFound() {
        dao.deleteDeviceInventory("7");
    }
    
    @Test
    public void testCreateOrUpdateLastJob() throws AsmManagerCheckedException {
        String refId = "1234567";
        DeviceInventoryEntity entity = new DeviceInventoryEntity();
        entity = dao.getDeviceInventory(refId);
        if (entity == null) {
            entity = new DeviceInventoryEntity();
            entity.setRefId(refId);
            entity.setDeviceType(DeviceType.ChassisM1000e);
            entity.setServiceTag("ServiceTag");
            entity.setIpAddress("10.128.129.123");
            entity.setModel("Unknown");
            entity.setRefType("Unknown");
            entity.setDisplayName("None");
            entity.setState(DeviceState.READY);
            entity.setDiscoverDeviceType(DiscoverDeviceType.CMC);
            try {
                dao.createDeviceInventory(entity);
            } catch (AsmManagerCheckedException amde) {
                if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                    fail();
                }
            }
        }
        dao.createOrUpdateLastJob(refId, JobType.Configure, DeviceState.READY, "test");
        DeviceLastJobStateEntity lastJob = dao.getLastJob(refId, JobType.Configure);
        assertEquals(DeviceState.READY, lastJob.getJobState());
        
        dao.createOrUpdateLastJob(refId, JobType.Configure, DeviceState.CONFIGURATION_ERROR, "failed");
        lastJob = dao.getLastJob(refId, JobType.Configure);
        assertEquals(DeviceState.CONFIGURATION_ERROR, lastJob.getJobState());
        
        entity = dao.getDeviceInventory(refId);
        assertEquals(1, entity.getDeviceLastJobList().size());
        
        dao.deleteDeviceInventory(refId);
    }

}
