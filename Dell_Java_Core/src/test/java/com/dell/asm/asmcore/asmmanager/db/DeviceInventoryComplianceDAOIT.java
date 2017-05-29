package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;

public class DeviceInventoryComplianceDAOIT {
    
    private DeviceInventoryComplianceDAO deviceInventoryComplianceDao = DeviceInventoryComplianceDAO.getInstance();
    private FirmwareRepositoryDAO firmwareRepositoryDao = FirmwareRepositoryDAO.getInstance();
    private DeviceInventoryDAO deviceInventoryDao = new DeviceInventoryDAO();

    private DeviceInventoryEntity deviceInventory1;
    private DeviceInventoryEntity deviceInventory2;
    private FirmwareRepositoryEntity firmwareRepository1;
    private FirmwareRepositoryEntity firmwareRepository2;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void init() throws Exception {
        cleanUp();
        
        deviceInventory1 = new DeviceInventoryEntity();
        deviceInventory1.setRefId("1");
        deviceInventory1.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory1.setServiceTag("ServiceTag");
        deviceInventory1.setIpAddress("10.128.129.123");
        deviceInventory1.setModel("Unknown");
        deviceInventory1.setRefType("Unknown");
        deviceInventory1.setDisplayName("None");
        deviceInventory1.setState(DeviceState.READY);
        deviceInventory1.setVersion(100L);
        deviceInventoryDao.createDeviceInventory(deviceInventory1);
        
        deviceInventory2 = new DeviceInventoryEntity();
        deviceInventory2.setRefId("2");
        deviceInventory2.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory2.setServiceTag("ServiceTag");
        deviceInventory2.setIpAddress("10.128.129.124");
        deviceInventory2.setModel("Unknown");
        deviceInventory2.setRefType("Unknown");
        deviceInventory2.setDisplayName("None");
        deviceInventory2.setState(DeviceState.READY);
        deviceInventory2.setVersion(101L);
        deviceInventoryDao.createDeviceInventory(deviceInventory2);
        
        firmwareRepository1 = new FirmwareRepositoryEntity();
        firmwareRepository1.setId("1");
        firmwareRepository1.setName("Min Repo");
        firmwareRepository1.setSourceLocation("Embedded");
        firmwareRepository1.setDiskLocation("/var/nfs/firmware/minimum/");
        firmwareRepository1.setDefault(Boolean.FALSE);
        firmwareRepository1.setState(RepositoryState.AVAILABLE);
        firmwareRepositoryDao.saveOrUpdate(firmwareRepository1);
        
        firmwareRepository2 = new FirmwareRepositoryEntity();
        firmwareRepository2.setId("2");
        firmwareRepository2.setName("Generated Repo");
        firmwareRepository2.setDefault(Boolean.TRUE);
        firmwareRepository2.setState(RepositoryState.AVAILABLE);
        firmwareRepositoryDao.saveOrUpdate(firmwareRepository2);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void destroy() throws Exception {
        cleanUp();
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(deviceInventoryComplianceDao);
    }
    
    @Test
    public void testDeviceInventoryComplianceCRUD() {
        // create
        final DeviceInventoryComplianceEntity dic1 = 
                new DeviceInventoryComplianceEntity(deviceInventory1,firmwareRepository1);
        dic1.setCompliance(CompliantState.UPDATEREQUIRED);
        deviceInventoryComplianceDao.saveOrUpdate(dic1);
        
        final DeviceInventoryComplianceEntity dic2 = 
                new DeviceInventoryComplianceEntity(deviceInventory2,firmwareRepository2);
        dic2.setCompliance(CompliantState.COMPLIANT);
        deviceInventoryComplianceDao.saveOrUpdate(dic2);
        
        // read
        final List<DeviceInventoryComplianceEntity> dicGetAll = deviceInventoryComplianceDao.getAll();
        assertNotNull(dicGetAll);
        assertTrue(CollectionUtils.isNotEmpty(dicGetAll));
        assertTrue(dicGetAll.size() == 2);
        
        final DeviceInventoryComplianceEntity dicGet =
                deviceInventoryComplianceDao.get(deviceInventory1, firmwareRepository1);
        assertNotNull(dicGet);
        assertTrue(CompliantState.UPDATEREQUIRED.equals(dicGet.getCompliance()));
        
        final List<DeviceInventoryComplianceEntity> dicFindByDeviceInventory =
                deviceInventoryComplianceDao.findByDeviceInventory(deviceInventory1);
        assertNotNull(dicFindByDeviceInventory);
        assertTrue(CollectionUtils.isNotEmpty(dicFindByDeviceInventory));
        assertTrue(dicFindByDeviceInventory.size() == 1);
        assertTrue(CompliantState.UPDATEREQUIRED.equals(dicFindByDeviceInventory.get(0).getCompliance()));
        
        final List<DeviceInventoryComplianceEntity> dicFindByFirmwareRepository =
                deviceInventoryComplianceDao.findByFirmwareRepository(firmwareRepository2);
        assertNotNull(dicFindByFirmwareRepository);
        assertTrue(CollectionUtils.isNotEmpty(dicFindByFirmwareRepository));
        assertTrue(dicFindByFirmwareRepository.size() == 1);
        assertTrue(CompliantState.COMPLIANT.equals(dicFindByFirmwareRepository.get(0).getCompliance()));
        
        // update
        dic1.setCompliance(CompliantState.COMPLIANT);
        deviceInventoryComplianceDao.saveOrUpdate(dic1);
        
        final DeviceInventoryComplianceEntity dicFind2 =
                deviceInventoryComplianceDao.get(deviceInventory1, firmwareRepository1);
        assertNotNull(dicFind2);
        assertTrue(CompliantState.COMPLIANT.equals(dicFind2.getCompliance()));
        
        // delete
        deviceInventoryComplianceDao.delete(dic1);
        final List<DeviceInventoryComplianceEntity> deleteGetAll = deviceInventoryComplianceDao.getAll();
        assertNotNull(deleteGetAll);
        assertTrue(CollectionUtils.isNotEmpty(deleteGetAll));
        assertTrue(deleteGetAll.size() == 1);
        
        final DeviceInventoryComplianceEntity deleteGet =
                deviceInventoryComplianceDao.get(deviceInventory1, firmwareRepository1);
        assertNull(deleteGet);
        
        // delete testing cascade
        deviceInventoryDao.deleteDeviceInventory(deviceInventory2);
        final List<DeviceInventoryComplianceEntity> deleteCascadeGetAll = deviceInventoryComplianceDao.getAll();
        assertNotNull(deleteCascadeGetAll);
        assertTrue(CollectionUtils.isEmpty(deleteCascadeGetAll));
    }
    
    private void cleanUp() {
        for (final DeviceInventoryEntity deviceInventory : deviceInventoryDao.getAllDeviceInventory()) {
            deviceInventoryDao.deleteDeviceInventory(deviceInventory.getRefId());
        }
        for (final FirmwareRepositoryEntity firmwareRepository : firmwareRepositoryDao.getAll()) {
            firmwareRepositoryDao.delete(firmwareRepository);
        }
        for (final DeviceInventoryComplianceEntity deviceInventoryCompliance : deviceInventoryComplianceDao.getAll()) {
            deviceInventoryComplianceDao.delete(deviceInventoryCompliance);
        }
    }
}
