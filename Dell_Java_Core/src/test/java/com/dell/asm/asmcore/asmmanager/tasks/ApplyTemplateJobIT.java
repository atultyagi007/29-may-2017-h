/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import static org.junit.Assert.fail;

import java.util.Properties;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.pg.orion.common.constants.CommonConstants;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import com.dell.pg.orion.queuemanager.QMManager;
import com.dell.pg.orion.queuemanager.connections.QMUtils;

public class ApplyTemplateJobIT
{
    
    private static final String BROKER_NAME = CommonConstants.ASM_JMS_BROKER_NAME;

    // Static fields.
    private static org.apache.activemq.broker.BrokerService _broker;
    private static com.dell.pg.orion.queuemanager.IQMManager _qmgr;
 /* ************************************************************************* */
 /*                               Constants                                   */
 /* ************************************************************************* */
 // Time to wait for all jobs to run.
 private static final int SLEEP_SECONDS = 30;
 
 /* ************************************************************************* */
 /*                             Constructor                                   */
 /* ************************************************************************* */
 public ApplyTemplateJobIT(){}
 
 /* ************************************************************************* */
 /*                             Public Methods                                */
 /* ************************************************************************* */
 /* ------------------------------------------------------------------------- */
 /* main:                                                                     */
 /* ------------------------------------------------------------------------- */
 public static void main(String[] args) {
 
     
     DeviceInventoryDAO dao = new DeviceInventoryDAO();
     DeviceInventoryEntity entity = new DeviceInventoryEntity();
     entity.setRefId("8a22e8aa41a917c30141a91afddb0002");
     entity.setDeviceType(DeviceType.ChassisM1000e);
     entity.setServiceTag("ServiceTag");
     entity.setIpAddress("192.168.113.20");
     entity.setModel("PowerEdge M1000e");     
     entity.setRefType("Unknown");
     entity.setDisplayName("None");
     entity.setState(DeviceState.READY);
     try {
         dao.createDeviceInventory(entity);
     } catch (AsmManagerCheckedException amde) {
         if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
             fail();
         }
     }
     
// try {
//    // startQueueManager();
//     ApplyInfrastructureTemplateService test = new ApplyInfrastructureTemplateService();
//     DeviceConfigureRequest deviceConfigRequest = new DeviceConfigureRequest();
//     deviceConfigRequest.setDeviceType(DeviceType.CHASSIS);
//     ChassisIdentity chassisIdentity = new ChassisIdentity();
//     chassisIdentity.setAisle("aisleTest");
//     chassisIdentity.setChassisName("chassisTest");
//     chassisIdentity.setDataCenterCharacter("datacenterTest");
//     chassisIdentity.setDeviceRefId("8a22e8aa41a917c30141a91afddb0002");
//     chassisIdentity.setRack("rackTest");
//     chassisIdentity.setRackSlot(10);
//     chassisIdentity.setDisplayName("displaynameTest");
//     chassisIdentity.setPowerCapPercentage(10);
//     chassisIdentity.setCmcDnsName("cmcdnsnameTest");
//     
//
//     Set<IomIdentity> iomIds = new HashSet<>();
//
//     IomIdentity iomIdentity = new IomIdentity();
//     iomIdentity.setIpAddress("192.168.113.121");
//     iomIdentity.setHostName("iomIDentity1");
//     iomIds.add(iomIdentity);
//     iomIdentity = new IomIdentity();
//     iomIdentity.setIpAddress("192.168.113.122");
//     iomIdentity.setHostName("iomIDentity2");
//     iomIds.add(iomIdentity);
//
//     chassisIdentity.setIomIdentities(iomIds);
//
//     Set<DeviceIdentity> dIds = new HashSet<>();
//
//     DeviceIdentity deviceId = new DeviceIdentity();
//     deviceId.setChassisIdentity(chassisIdentity);
//     deviceId.setDeviceRef("8a22e8aa41a917c30141a91afddb0002");
//     dIds.add(deviceId);
//
//     deviceConfigRequest.setDeviceType(DeviceType.CHASSIS);
//     deviceConfigRequest.setTemplateGuid("8a22e8aa41a917c30141a91cd3560021");
//     deviceConfigRequest.setDeviceIdentities(dIds);
//
//     test.applyInfrastructureTemplate(deviceConfigRequest);
//     
//     
//     } catch (Exception e) {
////         try {
////            stopQueueManager();
////        } catch (Exception e1) {
////            // TODO Auto-generated catch block
////            e1.printStackTrace();
////        }
//     e.printStackTrace();
// }

 }
 
 /* ------------------------------------------------------------------------- */
/* completeJob:                                                              */
/* ------------------------------------------------------------------------- */
  public static void completeJob(Long execHistoryId)
          throws JobManagerException {
      JobManager.getInstance().getJobHistoryManager().setExecHistoryStatus(execHistoryId, JobStatus.SUCCESSFUL);
  }

  /* ------------------------------------------------------------------------- */
/* startQueueManager:                                                        */
/* ------------------------------------------------------------------------- */
  public static com.dell.pg.orion.queuemanager.IQMManager startQueueManager()
          throws Exception {
      // Get the user home directory.
      String userHome = System.getProperty("user.home");

      // Start ActiveMQ and get the QueueManager instance.
      Properties qmprops = new Properties();
      qmprops.put("qmutils.broker.name", BROKER_NAME);
      qmprops.put("qmutils.broker.data.dir", userHome + "/.dell/orion/activemq");
      qmprops.put("qmutils.broker.connectors", "vm://" + BROKER_NAME);
      qmprops.put("jraf.default.connector", "vm://" + BROKER_NAME);
//qmprops.put("qmutils.broker.connectors", "tcp://localhost:61616,ssl://localhost:61617");
//qmprops.put("jraf.default.connector", "tcp://localhost:61616");
      _broker = QMUtils.startBroker(qmprops);
      com.dell.pg.orion.queuemanager.QMManagerConfig qmManagerConfig = new com.dell.pg.orion.queuemanager.QMManagerConfig (BROKER_NAME, "vm://" + BROKER_NAME);
      _qmgr = QMManager.createInstance(qmManagerConfig);
      return _qmgr;
  }

  /* ------------------------------------------------------------------------- */
/* stopQueueManager:                                                         */
/* ------------------------------------------------------------------------- */
  public static void stopQueueManager()
          throws Exception {
      // Shutdown QueueManager and ActiveMQ.
      _qmgr.destroy();
      _broker.stop();
  }
 
  // Broker name.
  

}
