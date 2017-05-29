/**
 * 
 */
package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;

/**
 * @author Yao_Lu1
 * 
 */
public class DiscoveryResultDAOIT {
    private DiscoveryResultDAO dao = DiscoveryResultDAO.getInstance();

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
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(dao);
    }

    /**
     * Test method for
     * {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#createDiscoveryResult(com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity)}
     * .
     */
//    @Test
//    public void testCreateDiscoveryResult() {
//        DiscoveryResultEntity entity = new DiscoveryResultEntity();
//        
//        DiscoveryJobKey discoveryJobKey = new DiscoveryJobKey();
//        
//
//        discoveryJobKey.setParentJobId("1");
//        discoveryJobKey.setJobId("2");
//        entity.setDiscoveryJobKey(discoveryJobKey);
//        entity.setRefId("1");
//        entity.setDeviceType(DeviceType.chassis);
//        entity.setServiceTag("ServiceTag");
//        entity.setIpAddress("10.128.129.123");
//        entity.setModel("Unknown");
//        entity.setRefType("Unknown");
//        entity.setStatus(DiscoveryStatus.CONNECTED);
//        entity.setStatusMessage("Discovery successul");
//        try {
//            dao.createDiscoveryResult(entity);
//        } catch (AsmManagerDAOException amde) {
//            if (amde.getReasonCode() != AsmManagerDAOException.REASON_CODE.DUPLICATE_REFID) {
//                fail();
//            }
//        }
//        discoveryJobKey.setParentJobId("2");
//        discoveryJobKey.setJobId("3");
//        entity.setDiscoveryJobKey(discoveryJobKey);
//        try {
//            dao.createDiscoveryResult(entity);
//        } catch (AsmManagerDAOException amde) {
//            if (amde.getReasonCode() != AsmManagerDAOException.REASON_CODE.DUPLICATE_SERVICETAG) {
//                fail();
//            }
//        }
//
//    }

/*************************************************************************    
//Commented
**************************************************************************/    
//    /**
//     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getDiscoveryResult(java.lang.String, java.lang.String)}.
//     */
//    @Test
//    public void testGetDiscoveryResult() {
//        //"Job-6f951902-1648-4c4f-a260-8c6a739869ae"
//
//        List<DiscoveryResultEntity> result = dao.getDiscoveryResult("Job-6f951902-1648-4c4f-a260-8c6a739869ae", sortInfos, filterInfos, paginationInfo);
//        assertNotNull(result);
//    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getAllDiscoveryResult()}.
     */
    @Test
    public void testGetAllDiscoveryResult() {
        List<DiscoveryResultEntity> results = dao.getAllDiscoveryResult();
        assertTrue(results.size() > 0);
    }

//    /**
//     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getAllDiscoveryResult()}.
//     */
//    @Test
//    public void testGetDiscoveryResultByJob() {
//    	String parentJobName ="Job-6d5992ff-2b20-4531-a0c4-32484cd5bc61";
//        List<DiscoveryResultEntity> results = dao.getDiscoveryResult(parentJobName);
//        assertTrue(results.size() > 0);
//    }
//
//    
//    /**
//     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#updateDiscoveryResult()}.
//     */
//    @Test
//    public void testUpdateDiscoveryResult() {
//        List<DiscoveryResultEntity> entityList = dao.getDiscoveryResult("1");
//        if (entityList.size() > 0) {
//            DiscoveryResultEntity entity = entityList.get(0);
//            entity.setModel("New Model");
//            dao.updateDiscoveryResult(entity);
//            entity = dao.getDiscoveryResult("1").get(0);
//            assertEquals("New Model", entity.getModel());
//        }
//    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#deleteDiscoveryResult()}.
     */
    @Test
    public void testDeleteDiscoveryResult() {
        dao.deleteDiscoveryResult("1");
        dao.deleteDiscoveryResult("2");
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#deleteDiscoveryResult()}.
     */
    @Test
    public void testDeleteDiscoveryResultNotFound() {
        dao.deleteDiscoveryResult("7");
    }

    
    @Test
    public void testCreateorUpdateDiscoveryResult() {
      DiscoveryResultEntity entity = new DiscoveryResultEntity();
//      entity.setParentJobId("1");
//      entity.setJobId("2");
//      
//      entity.setRefId("1");
//      entity.setDeviceType(DeviceType.chassis);
//      entity.setServiceTag("ServiceTag");
//      entity.setIpAddress("10.128.129.123");
//      entity.setModel("Unknown");
//      entity.setRefType("Unknown");
//      entity.setStatus(DiscoveryStatus.CONNECTED);
//      entity.setStatusMessage("Discovery successul");
//      try {
//          dao.createOrUpdateDiscoveryResult(entity);
//      } catch (AsmManagerDAOException amde) {
//          if (amde.getReasonCode() != AsmManagerDAOException.REASON_CODE.DUPLICATE_REFID) {
//              //fail();
//          }
//      }
     
      entity.setRefId("1");
      entity.setDeviceType(DeviceType.RackServer);
      entity.setServiceTag("ServiceTagUpdated");
      entity.setIpaddress("10.128.129.123u");
      entity.setModel("Unknown");
     entity.setRefType("Unknown");
     entity.setStatus(DiscoveryStatus.FAILED);
     entity.setStatusMessage("Discovery successul U");
      
      try {
          dao.createOrUpdateDiscoveryResult(entity);
      } catch (AsmManagerCheckedException amde) {    	
          if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD)
              fail();
      }
    }
}
