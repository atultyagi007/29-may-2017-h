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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.ConfigureStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceConfigureEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;

public final class DeviceConfigureDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(DeviceConfigureDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static DeviceConfigureDAO instance;

    private DeviceConfigureDAO() {
    }

    public static synchronized DeviceConfigureDAO getInstance() {
        if (instance == null)
            instance = new DeviceConfigureDAO();
        return instance;
    }

    /**
     * Create DeviceConfigure.
     * 
     * @param deviceConfigure
     * @return the DeviceConfigureEntity
     */
    public DeviceConfigureEntity createDeviceConfigure(DeviceConfigureEntity deviceConfigure) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            session.save(deviceConfigure);
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            deviceConfigure.setCreatedDate(now);
            deviceConfigure.setCreatedBy(_dao.extractUserFromRequest());
            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during deviceConfigure entity creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during deviceConfigure entity creation: " + ex);
            }
            if (cve.getConstraintName().contains("id")) {
                // throw new
                // AsmManagerDAOException(AsmManagerDAOException.REASON_CODE.DUPLICATE_JOBID,
                // cve);
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_JOBID, AsmManagerMessages.duplicateRefId(cve
                        .getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during deviceConfigure creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create deviceConfigure: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error Creating deviceConfigure entity", "DeviceConfigureDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device configure: " + ex);
            }
        }

        return deviceConfigure;
    }

    /**
     * Retrieve DeviceConfigureEntity based on id.
     * 
     * @return the DeviceConfigureEntity.
     */
    public DeviceConfigureEntity getDeviceConfigureEntityById(String id) throws AsmManagerDAOException {

        Session session = null;
        Transaction tx = null;
        DeviceConfigureEntity deviceConfigureEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceConfigureEntity where id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            deviceConfigureEntity = (DeviceConfigureEntity) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get deviceConfigureEntity for  id: " + id + ", " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get deviceConfigureEntity: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve deviceConfigureEntity by Id", "DeviceConfigureDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get deviceConfigure: " + ex);
            }
        }

        return deviceConfigureEntity;
    }

    /**
     * 
     * @param sortInfos
     * @param filterInfos
     * @param paginationInfo
     * @return
     */
    public List<DeviceConfigureEntity> getAllDeviceConfigureEntities(List<SortParamParser.SortInfo> sortInfos,
            List<FilterParamParser.FilterInfo> filterInfos, PaginationInfo paginationInfo) {
        Session session = null;
        Transaction tx = null;
        List<DeviceConfigureEntity> entityList = new ArrayList<DeviceConfigureEntity>();

        try {
            int offset = paginationInfo.getOffset();
            int limit = paginationInfo.getLimit();
            int pageSize = limit;

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeviceConfigureEntity.class);
            BaseDAO.addSortCriteria(criteria, sortInfos);

            BaseDAO.addFilterCriteria(criteria, filterInfos, DeviceConfigureEntity.class);

            criteria.setFirstResult(offset);
            criteria.setMaxResults(pageSize);

            entityList = criteria.list();
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get all DeviceConfigureEntities: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all DeviceConfigureEntities: " + ex);
            }
            throw new AsmManagerInternalErrorException("Error Getting DeviceConfigureEntities", "DeviceConfigureDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all DeviceConfigureEntities: " + ex);
            }
        }

        return entityList;
    }

    /**
     * Delete DeviceConfigureEntity by id.
     * 
     * @param id
     */
    public void deleteDiscoveryResult(String id) {

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // execute command.
            String hql = "delete DeviceConfigureEntity  where id=:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            int rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" + rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete DeviceConfigureEntity: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete DeviceConfigureEntity: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error deleting DeviceConfigureEntity", "DeviceConfigureDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete DeviceConfigureEntity: " + ex);
            }
        }
    }

    /**
     * Helper method to get total number of records with filter parameters
     * 
     * @param filterInfos
     *            - List for holding filtering information parsed from filter query parameter value
     * @return int - total number of records
     */
    public Integer getTotalRecords(List<FilterParamParser.FilterInfo> filterInfos) {
        long totalRecords = 0;
        Session session = null;
        Transaction tx = null;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeviceConfigureEntity.class);
            BaseDAO.addFilterCriteria(criteria, filterInfos, DeviceConfigureEntity.class);
            totalRecords = (long) criteria.setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                try {
                    tx.rollback();
                } catch (Exception e2) {
                    logger.warn("Error during rollback", e2);
                }
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (Exception e2) {
                logger.warn("Error during session close", e2);
            }
        }

        return (int) totalRecords;
    }

    
    public boolean updateDeviceConfigureEntity(DeviceConfigureEntity deviceConfigureEntity, ConfigureStatus status) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;
        boolean updatedFlag = false;
        
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from DeviceConfigureEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", deviceConfigureEntity.getId());
            
            DeviceConfigureEntity databaseDeviceConfigureEntity = (DeviceConfigureEntity) query.setMaxResults(1).uniqueResult();
            if (databaseDeviceConfigureEntity == null) {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND,
                    AsmManagerMessages.notFound(deviceConfigureEntity.getId()));
            }

            databaseDeviceConfigureEntity.setStatus(status);
            databaseDeviceConfigureEntity.setUpdatedDate(new GregorianCalendar());
            databaseDeviceConfigureEntity.setUpdatedBy(_dao.extractUserFromRequest());
            
            session.saveOrUpdate(databaseDeviceConfigureEntity);

            // Commit transaction.
            tx.commit();
            updatedFlag = true;
        } catch (Exception e) {
            logger.warn("Caught exception during update device configure entity : " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                    updatedFlag = false;
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update device configure entity : " + ex);
            }
            if (e instanceof AsmManagerCheckedException) {
                throw e;
            }
            throw new AsmManagerInternalErrorException("Update device", "DeviceConfigureDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
               logger.warn("Unable to close session during update device configure entity : " + ex);
            }
        }
        
        return updatedFlag;
    }
}
