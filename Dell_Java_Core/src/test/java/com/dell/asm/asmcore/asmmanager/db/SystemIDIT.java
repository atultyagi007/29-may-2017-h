/**
 * 
 */
package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SystemIDEntity;

/**
 * @author Yao_Lu1
 * 
 */
public class SystemIDIT {
    private GenericDAO genericDAO = GenericDAO.getInstance();
    private SoftwareComponentEntity sce = null;
    private FirmwareRepositoryEntity fw = null;
    

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
    	genericDAO.delete(fw.getId(), FirmwareRepositoryEntity.class);
    }
    

    /**
     * Test method for {@link DeviceInventoryDAO#getInstance()}.
     */
    @Test
    public void testPersist() 
    {
        sce = new SoftwareComponentEntity();
        fw = new FirmwareRepositoryEntity();
        fw.getSoftwareComponents().add(sce);
        fw.setName("name");
        fw.setDiskLocation("disk");
        fw.setFilename("fname");
        sce.setFirmwareRepositoryEntity(fw);
        sce.setName("name");
        
        SystemIDEntity sys = new SystemIDEntity("12");
        sce.getSystemIDs().add(sys);
        sys.setSoftwareComponentEntity(sce);
        
        fw = genericDAO.create(fw);
        
        assertTrue("incorrect software component size", fw.getSoftwareComponents().size() == 1);
        sce = (SoftwareComponentEntity) fw.getSoftwareComponents().toArray()[0];
        sce = genericDAO.get(sce.getId(), SoftwareComponentEntity.class);
        assertTrue("incorrect system id count", sce.getSystemIDs().size() == 1);
    }

}
