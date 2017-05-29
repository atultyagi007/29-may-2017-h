/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.db.entity.MapRazorNodeNameToSerialNumberEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

public final class MapRazorNodeNameToSerialNumberDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(MapRazorNodeNameToSerialNumberDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static MapRazorNodeNameToSerialNumberDAO instance;

    private MapRazorNodeNameToSerialNumberDAO() {
    }

    public static synchronized MapRazorNodeNameToSerialNumberDAO getInstance() {
        if (instance == null)
            instance = new MapRazorNodeNameToSerialNumberDAO();
        return instance;
    }

    /**
     * Create Device Inventory.
     * @param device
     * @return the entity
     */
    public MapRazorNodeNameToSerialNumberEntity createRazorNode(MapRazorNodeNameToSerialNumberEntity device)
    		throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            logger.info("Creating MapRazorNodeNameToSerialNumberEntity in inventory: " + device.getId());
            
            session.save(device);
            
            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during MapRazorNodeNameToSerialNumberEntity inventory creation: " + cve);
            try {
                if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            if (cve.getConstraintName().contains("refid")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else if (cve.getConstraintName().contains("service_tag")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_SERVICETAG,
                        AsmManagerMessages.duplicateServiceTag(cve.getSQLException().getMessage()));
            } else {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during device MapRazorNodeNameToSerialNumberEntity creation: " + e);
            try {
                if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create devices", "MapRazorNodeNameToSerialNumberDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device: " + ex);
            }
        }

        return device;
    }
    
    
    /**
     * Retrieve Device Inventory.
     * @return the entity
     */
    public MapRazorNodeNameToSerialNumberEntity getRazorNodeById(String Id) {

        Session session = null;
        Transaction tx = null;
        MapRazorNodeNameToSerialNumberEntity deviceInventoryEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from MapRazorNodeNameToSerialNumberEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", Id);
            deviceInventoryEntity = (MapRazorNodeNameToSerialNumberEntity) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get MapRazorNodeNameToSerialNumberEntity for refId: " + Id + ", "  + e);
            try {
        	if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve device", "MapRazorNodeNameToSerialNumberDAO", e);
        } finally {
            try {
        	if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return deviceInventoryEntity;
    }

    /**
     * Retrieve Device Inventory.
     * @return the entity
     */
    public MapRazorNodeNameToSerialNumberEntity getRazorNodeBySerialNumber(String serialNumber) {

        Session session = null;
        Transaction tx = null;
        MapRazorNodeNameToSerialNumberEntity deviceInventoryEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from MapRazorNodeNameToSerialNumberEntity where serialNumber = :serialNumber";
            Query query = session.createQuery(hql);
            query.setString("serialNumber", serialNumber);
            deviceInventoryEntity = (MapRazorNodeNameToSerialNumberEntity) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get MapRazorNodeNameToSerialNumberEntity for serialNumber: " + serialNumber + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve device", "MapRazorNodeNameToSerialNumberDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return deviceInventoryEntity;
    }
    
    /**
     * Retrieve all from Device Inventory.
     * @return list of entities
     */
    public List<MapRazorNodeNameToSerialNumberEntity> getAllRazorNodes() {

        Session session = null;
        Transaction tx = null;
        List<MapRazorNodeNameToSerialNumberEntity> entityList = new ArrayList<MapRazorNodeNameToSerialNumberEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from MapRazorNodeNameToSerialNumberEntity";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
            	entityList.add((MapRazorNodeNameToSerialNumberEntity) result);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all devices in inventory: " + e);
            try {
                if (tx != null) {
        	    tx.rollback();
        	}
            } catch (Exception ex) {
        	logger.warn("Unable to rollback transaction during get all devices: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all devices", "DeviceInventoryDAO", e);
        } finally {
            try {
        	if (session != null) {
        	    session.close();
        	}
            } catch (Exception ex) {
        	logger.warn("Unable to close session during get all devices: " + ex);
            }
        }

        return entityList;
    }
 
}