/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

public class DeviceInventoryComplianceDAO {
    
    private static final Logger logger = Logger.getLogger(DeviceInventoryComplianceDAO.class);
    
    private GenericDAO dao = GenericDAO.getInstance();
    private static DeviceInventoryComplianceDAO instance;
    
    public static synchronized DeviceInventoryComplianceDAO getInstance() {
        if (instance == null) {
            instance = new DeviceInventoryComplianceDAO();
        }
        return instance;
    }

    public DeviceInventoryComplianceEntity saveOrUpdate(final DeviceInventoryComplianceEntity entity) {
        Session session = null;
        Transaction tx = null;
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final String user = dao.extractUserFromRequest();
            final Date now = new Date();
            final DeviceInventoryComplianceEntity persisted = (DeviceInventoryComplianceEntity) 
                    session.get(DeviceInventoryComplianceEntity.class, entity.getDeviceInventoryComplianceId());
            if (persisted == null) {
                entity.setCreatedBy(user);
                entity.setCreatedDate(now);
                entity.setUpdatedBy(user);
                entity.setUpdatedDate(now);
                session.save(entity);
            } else {
                persisted.setUpdatedBy(user);
                persisted.setUpdatedDate(now);
                persisted.setCompliance(entity.getCompliance());
                session.saveOrUpdate(persisted);
            }
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during saveOrUpdate: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during saveOrUpdate: " + ex);
            }
            throw new AsmManagerInternalErrorException("saveOrUpdate", "DeviceInventoryComplianceDAO", e);
        } finally {
            dao.cleanupSession(session, "saveOrUpdate");
        }
        
        return entity;
    }
    
    public DeviceInventoryComplianceEntity get(final DeviceInventoryEntity deviceInventory, 
            final FirmwareRepositoryEntity firmwareRepository) {
        if (deviceInventory == null || firmwareRepository == null) {
            return null;
        }
        return findDeviceInventoryCompliance(deviceInventory.getRefId(), firmwareRepository.getId());
    }
    
    public DeviceInventoryComplianceEntity get(String deviceInventoryId, 
            final FirmwareRepositoryEntity firmwareRepository) {
        if (deviceInventoryId == null || firmwareRepository == null) {
            return null;
        }
        return findDeviceInventoryCompliance(deviceInventoryId, firmwareRepository.getId());
    }
    
    public DeviceInventoryComplianceEntity findDeviceInventoryCompliance(String deviceInventoryId,
                                                                         String firmwareRepositoryId) {
        
        if (StringUtils.isBlank(deviceInventoryId) || StringUtils.isBlank(firmwareRepositoryId)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final DeviceInventoryEntity deviceInventory = (DeviceInventoryEntity) 
                    session.get(DeviceInventoryEntity.class, deviceInventoryId);
            if (deviceInventory == null) {
                return null;
            }
            final FirmwareRepositoryEntity firmwareRepository = (FirmwareRepositoryEntity) 
                    session.get(FirmwareRepositoryEntity.class, firmwareRepositoryId);
            if (firmwareRepository == null) {
                return null;
            }
            
            final String hql = "from DeviceInventoryComplianceEntity dic"
                    + " where dic.deviceInventoryComplianceId.deviceInventory = :deviceInventory"
                    + " and dic.deviceInventoryComplianceId.firmwareRepository = :firmwareRepository";
            final Query query = session.createQuery(hql);
            query.setEntity("deviceInventory", deviceInventory);
            query.setEntity("firmwareRepository", firmwareRepository);
            return (DeviceInventoryComplianceEntity)query.uniqueResult();
            
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during findDeviceInventoryCompliance: " + ex);
            }
            throw new AsmManagerInternalErrorException("findDeviceInventoryCompliance", "DeviceInventoryComplianceDAO", e);
        } finally {
            dao.cleanupSession(session, "findDeviceInventoryCompliance");
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<DeviceInventoryComplianceEntity> getAll() {
        Session session = null;
        Transaction tx = null;
        final List<DeviceInventoryComplianceEntity> entities = new ArrayList<DeviceInventoryComplianceEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from DeviceInventoryComplianceEntity";
            final Query query = session.createQuery(hql); 
            entities.addAll(query.list());
        } catch (Exception e) {
            logger.warn("Caught exception during getAll: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getAll: " + ex);
            }
            throw new AsmManagerInternalErrorException("getAll", "DeviceInventoryComplianceDAO", e);
        } finally {
            dao.cleanupSession(session, "getAll");
        }
        
        return entities;
    }
    
    public List<DeviceInventoryComplianceEntity> findByDeviceInventory(final DeviceInventoryEntity deviceInventory) {
        if (deviceInventory == null) {
            return null;
        }
        return findByDeviceInventory(deviceInventory.getRefId());
    }
    
    @SuppressWarnings("unchecked")
    public List<DeviceInventoryComplianceEntity> findByDeviceInventory(final String deviceInventoryId) {
        if (StringUtils.isBlank(deviceInventoryId)) {
            return Collections.emptyList();
        }
        Session session = null;
        Transaction tx = null;
        final List<DeviceInventoryComplianceEntity> entities = new ArrayList<DeviceInventoryComplianceEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final DeviceInventoryEntity deviceInventory = (DeviceInventoryEntity) 
                    session.get(DeviceInventoryEntity.class, deviceInventoryId);
            if (deviceInventory == null) {
                return Collections.emptyList();
            }
            
            final String hql = "from DeviceInventoryComplianceEntity dic" 
                    + " where dic.deviceInventoryComplianceId.deviceInventory = :deviceInventory";
            final Query query = session.createQuery(hql);
            query.setEntity("deviceInventory", deviceInventory);
            entities.addAll(query.list());
        } catch (Exception e) {
            logger.warn("Caught exception during findByDeviceInventory: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during findByDeviceInventory: " + ex);
            }
            throw new AsmManagerInternalErrorException("findByDeviceInventory", "DeviceInventoryComplianceDAO", e);
        } finally {
            dao.cleanupSession(session, "findByDeviceInventory");
        }
        
        return entities;
    }
    
    public List<DeviceInventoryComplianceEntity> findByFirmwareRepository(
            final FirmwareRepositoryEntity firmwareRepository) {
        if (firmwareRepository == null) {
            return null;
        }
        return findByFirmwareRepository(firmwareRepository.getId());
    }
    
    @SuppressWarnings("unchecked")
    public List<DeviceInventoryComplianceEntity> findByFirmwareRepository(final String firmwareRepositoryId) {
        if (StringUtils.isBlank(firmwareRepositoryId)) {
            return Collections.emptyList();
        }
        Session session = null;
        Transaction tx = null;
        final List<DeviceInventoryComplianceEntity> entities = new ArrayList<DeviceInventoryComplianceEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final FirmwareRepositoryEntity firmwareRepository = (FirmwareRepositoryEntity) 
                    session.get(FirmwareRepositoryEntity.class, firmwareRepositoryId);
            if (firmwareRepository == null) {
                return Collections.emptyList();
            }
            
            final String hql = "from DeviceInventoryComplianceEntity dic"
                    + " where dic.deviceInventoryComplianceId.firmwareRepository = :firmwareRepository";
            final Query query = session.createQuery(hql);
            query.setEntity("firmwareRepository", firmwareRepository);
            entities.addAll(query.list());
        } catch (Exception e) {
            logger.warn("Caught exception during findByFirmwareRepository: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during findByFirmwareRepository: " + ex);
            }
            throw new AsmManagerInternalErrorException("findByFirmwareRepository", "DeviceInventoryComplianceDAO", e);
        } finally {
            dao.cleanupSession(session, "findByFirmwareRepository");
        }
        
        return entities;
    }
    
    public void delete(final DeviceInventoryComplianceEntity deviceInventoryCompliance) {
        if (deviceInventoryCompliance == null) {
            return;
        }
        delete(deviceInventoryCompliance.getDeviceInventory(), 
                deviceInventoryCompliance.getFirmwareRepository());
    }
    
    public void delete(final DeviceInventoryEntity deviceInventory, final FirmwareRepositoryEntity firmwareRepository) {
        if (deviceInventory == null || firmwareRepository == null) {
            return;
        }
        delete(deviceInventory.getRefId(), firmwareRepository.getId());
    }
    
    public void delete(final String deviceInventoryId, final String firmwareRepositoryId) {
        if (StringUtils.isBlank(deviceInventoryId) || StringUtils.isBlank(firmwareRepositoryId)) {
            return;
        }
        logger.info("Deleting device inventory compliance mapping: " + deviceInventoryId + "," + firmwareRepositoryId);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();

            String hql = "delete DeviceInventoryComplianceEntity dic "
                    + " where dic.deviceInventoryComplianceId.deviceInventoryId = :deviceInventoryId"
                    + " and dic.deviceInventoryComplianceId.firmwareRepositoryId = :firmwareRepositoryId";
            Query query = session.createQuery(hql);
            query.setString("deviceInventoryId", deviceInventoryId);
            query.setString("firmwareRepositoryId", firmwareRepositoryId);
            query.executeUpdate();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete", "DeviceInventoryComplianceDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete: " + ex);
            }
        }
    }
}
