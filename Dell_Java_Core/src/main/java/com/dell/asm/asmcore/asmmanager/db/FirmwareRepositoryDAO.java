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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

public class FirmwareRepositoryDAO {
    private static final Logger logger = Logger.getLogger(FirmwareRepositoryDAO.class);
    private GenericDAO dao = GenericDAO.getInstance();
    private static FirmwareRepositoryDAO instance;

    private FirmwareRepositoryDAO() {
    }
    
    public static synchronized FirmwareRepositoryDAO getInstance() {
        if (instance == null) {
            instance = new FirmwareRepositoryDAO();
        }
        return instance;
    }
    
    public FirmwareRepositoryEntity saveOrUpdate(final FirmwareRepositoryEntity entity) {
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction(); 
            final String user = dao.extractUserFromRequest();
            final Date now = new Date();
            final FirmwareRepositoryEntity persisted = 
                    (FirmwareRepositoryEntity) session.get(FirmwareRepositoryEntity.class, entity.getId());
            if (persisted == null) {
                entity.setCreatedDate(now);
                entity.setCreatedBy(user);
                entity.setUpdatedDate(now);
                entity.setUpdatedBy(user);
                session.save(entity);
            } else {
                entity.setUpdatedDate(now);
                entity.setUpdatedBy(user);
                session.merge(entity);
            }
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during createFirmwareRepository: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during createFirmwareRepository: " + ex);
            }
            throw new AsmManagerInternalErrorException("createFirmwareRepository", "FirmwareRepositoryDAO", e);
        } finally {
            dao.cleanupSession(session, "createFirmwareRepository");
        }

        return entity;
    }
    
    public FirmwareRepositoryEntity merge(final FirmwareRepositoryEntity original, 
            final FirmwareRepositoryEntity update) {
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            
            if (original != null) {
                final String originalId = original.getId();
                session.load(original, originalId);
                original.getDeployments().clear();
                original.getSoftwareBundles().clear();
                original.getSoftwareComponents().clear();
                original.getTemplates().clear();
                original.getDeviceInventoryComplianceEntities().clear();
                session.flush();
                // reload the original entity in cache so we can merge
                session.get(FirmwareRepositoryEntity.class, originalId);
                update.setId(originalId);
                update.setCreatedDate(original.getCreatedDate());
                update.setCreatedBy(original.getCreatedBy());
                update.setUpdatedDate(new Date());
                update.setUpdatedBy(dao.extractUserFromRequest());
                session.merge(update);
            } else {
                update.setCreatedDate(new Date());
                update.setCreatedBy(dao.extractUserFromRequest());
                update.setUpdatedDate(update.getCreatedDate());
                update.setUpdatedBy(update.getCreatedBy());
                session.saveOrUpdate(update);
            }
            
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during merge: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during merge: " + ex);
            }
            throw new AsmManagerInternalErrorException("merge", "FirmwareRepositoryDAO", e);
        } finally {
            dao.cleanupSession(session, "merge");
        }

        return original;
    }
    
    public FirmwareRepositoryEntity get(final String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from FirmwareRepositoryEntity where id = :id";
            final Query query = session.createQuery(hql); 
            query.setString("id", id);
            return (FirmwareRepositoryEntity) query.uniqueResult();
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get", "FirmwareRepositoryDao", e);
        } finally {
            dao.cleanupSession(session, "get");
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<FirmwareRepositoryEntity> getAll() {
        Session session = null;
        Transaction tx = null;
        final List<FirmwareRepositoryEntity> entities = new ArrayList<FirmwareRepositoryEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from FirmwareRepositoryEntity";
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
            throw new AsmManagerInternalErrorException("getAll", "FirmwareRepositoryDao", e);
        } finally {
            dao.cleanupSession(session, "getAll");
        }
        
        return entities;
    }
    
    public void delete(final FirmwareRepositoryEntity firmwareRepository) {
        if (firmwareRepository == null) {
            return;
        }
        delete(firmwareRepository.getId());
    }
    
    public void delete(final String firmwareRepositoryId) {
        if (StringUtils.isBlank(firmwareRepositoryId)) {
            return;
        }
        logger.info("Deleting firmware repository: " + firmwareRepositoryId);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();

            String hql = "delete FirmwareRepositoryEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", firmwareRepositoryId);
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
            throw new AsmManagerInternalErrorException("Delete", "FirmewareRepositoryDAO", e);
        } finally {

        }
    }

    public FirmwareRepositoryEntity getCompleteFirmware(String id, boolean bundles, boolean components)
    {
        Session session = null;
        Transaction tx = null;
        FirmwareRepositoryEntity firmware = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from FirmwareRepositoryEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            firmware =  (FirmwareRepositoryEntity)query.setMaxResults(1).uniqueResult();
            if (firmware != null)
            {
                hydrateFirmware(firmware,bundles,components);
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get firmwarerepository for id: " + id+ ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get by id: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve firmwarerepository", "GenericDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during getCompleteFirmware: " + ex);
            }
        }

        return firmware;


    }

    public FirmwareRepositoryEntity getFirmwareWithUsedBy(String id)
    {
        Session session = null;
        Transaction tx = null;
        FirmwareRepositoryEntity firmware = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from FirmwareRepositoryEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            firmware =  (FirmwareRepositoryEntity)query.setMaxResults(1).uniqueResult();
            if (firmware != null)
            {
                // initialize templates but not deployments as they are eager fetch.
                Hibernate.initialize(firmware.getTemplates());
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during getFirmwareWithUsedBy for id: " + id+ ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get by id: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve getFirmwareWithUsedBy", "GenericDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during getFirmwareWithUsedBy: " + ex);
            }
        }

        return firmware;


    }

    private void hydrateFirmware(FirmwareRepositoryEntity firmware, boolean bundles, boolean components) {
        if (firmware == null) {
            return;
        }

        try {
            if (bundles) {
                Hibernate.initialize(firmware.getSoftwareBundles());
                // Only user bundles should return the components.  The UI needs the component to work right for user bundles.
                for (SoftwareBundleEntity entity : firmware.getSoftwareBundles()) {
                    if (entity.isUserBundle()) {
                        Hibernate.initialize(entity.getSoftwareComponents());
                    }
                }
            }
            if (components) {
                for (SoftwareBundleEntity entity : firmware.getSoftwareBundles()) {
                    Hibernate.initialize(entity.getSoftwareComponents());
                }
                Hibernate.initialize(firmware.getSoftwareComponents());
            }
        } catch (LazyInitializationException e) {

        }
    }
    
    /**
     * Sets all of the isDefault values for the entire FirmwareRepositoryEntity table. 
     */
    public void updateAllIsDefault(boolean isDefault) {
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction(); 
            
            Query query = session.createSQLQuery("update firmware_repository set is_default = :isDefault");
            query.setParameter("isDefault", isDefault);
            
            int result = query.executeUpdate();

            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during updateAllIsDefault: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during updateAllIsDefault: " + ex);
            }
            throw new AsmManagerInternalErrorException("updateAllIsDefault", "FirmwareRepositoryDAO", e);
        } finally {
            dao.cleanupSession(session, "updateAllIsDefault");
        }
    }
    
    /**
     * Will return a list of SoftwareComponentEntities for the given firmware repository where the operating_system 
     * field is not null.  
     */
    public List<SoftwareComponentEntity> getSoftwareComponentEntitiesWithOperatingSystem(String firmwareRepoId, String operatingSystem) {
        
        ArrayList<SoftwareComponentEntity> softwareComponentEntities = new ArrayList<SoftwareComponentEntity>();
        
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction(); 
            
            String hql = "from SoftwareComponentEntity where firmware_repository = :firmwareRepoId AND operating_system = :operatingSystem";
            Query query = session.createQuery(hql);
            query.setString("firmwareRepoId", firmwareRepoId);
            query.setString("operatingSystem", operatingSystem);
            for (Object result : query.list()) {
                softwareComponentEntities.add((SoftwareComponentEntity) result);
            }

            tx.commit();
        } catch (Exception e) {
            logger.warn("Error in getSoftwareComponentEntitiesForFirmwareRepository for firmwareId of " + firmwareRepoId + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getSoftwareComponentEntitiesForFirmwareRepository" + ex);
            }
            throw new AsmManagerInternalErrorException("getSoftwareComponentEntitiesForFirmwareRepository", "FirmwareRepositoryDAO", e);
        } finally {
            dao.cleanupSession(session, "getSoftwareComponentEntitiesForFirmwareRepository");
        }

        return softwareComponentEntities;
    }

}
