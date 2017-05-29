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
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;

public final class DeviceDiscoverDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(DeviceDiscoverDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static DeviceDiscoverDAO instance;

    private DeviceDiscoverDAO() {
    }

    public static synchronized DeviceDiscoverDAO getInstance() {
        if (instance == null)
            instance = new DeviceDiscoverDAO();
        return instance;
    }

    /**
     * Create DeviceDiscover.
     * 
     * @param deviceDiscover
     * @return the DeviceDiscoverEntity
     */
    public DeviceDiscoverEntity createDeviceDiscover(DeviceDiscoverEntity deviceDiscover) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            deviceDiscover.setCreatedDate(now);
            deviceDiscover.setCreatedBy(_dao.extractUserFromRequest());
            session.save(deviceDiscover);
            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during DeviceDiscover entity creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during device discover entity creation: " + ex);
            }
            if (cve.getConstraintName().contains("id")) {
                // throw new
                // AsmManagerDAOException(AsmManagerDAOException.REASON_CODE.DUPLICATE_JOBID,
                // cve);
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_JOBID, AsmManagerMessages.duplicateRefId(cve
                        .getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during deviceDiscover creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create deviceDiscover: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error Creating deviceDiscover entity", "DeviceDiscoverDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device discover: " + ex);
            }
        }

        return deviceDiscover;
    }

    /**
     * Retrieve DeviceDiscoverEntity based on id.
     * 
     * @return the DeviceDiscoverEntity.
     */
    public DeviceDiscoverEntity getDeviceDiscoverEntityById(String id) throws AsmManagerDAOException {

        Session session = null;
        Transaction tx = null;
        DeviceDiscoverEntity deviceDiscoverEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceDiscoverEntity where id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            deviceDiscoverEntity = (DeviceDiscoverEntity) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get deviceDiscoverEntity for  id: " + id + ", " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get deviceDiscoverEntity: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve deviceDiscoverEntity by Id", "DeviceDiscoverDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get deviceDiscover: " + ex);
            }
        }

        return deviceDiscoverEntity;
    }

    /**
     * 
     * @param sortInfos
     * @param filterInfos
     * @param paginationInfo
     * @return
     */
    public List<DeviceDiscoverEntity> getAllDeviceDiscoverEntities(List<SortParamParser.SortInfo> sortInfos,
            List<FilterParamParser.FilterInfo> filterInfos, PaginationInfo paginationInfo) {
        Session session = null;
        Transaction tx = null;
        List<DeviceDiscoverEntity> entityList = new ArrayList<DeviceDiscoverEntity>();

        try {
            int offset = paginationInfo.getOffset();
            int limit = paginationInfo.getLimit();
            int pageSize = limit;

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeviceDiscoverEntity.class);
            BaseDAO.addSortCriteria(criteria, sortInfos);

            BaseDAO.addFilterCriteria(criteria, filterInfos, DeviceDiscoverEntity.class);

            criteria.setFirstResult(offset);
            criteria.setMaxResults(pageSize);

            entityList = criteria.list();
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get all DeviceDiscoverEntities: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all DeviceDiscoverEntities: " + ex);
            }
            throw new AsmManagerInternalErrorException("Error Getting DeviceDiscoverEntities", "DeviceDiscoverDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all DeviceDiscoverEntities: " + ex);
            }
        }

        return entityList;
    }

    /**
     * Delete DeviceDiscoverEntity by id.
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
            String hql = "delete DeviceDiscoverEntity  where id=:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            int rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" + rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete DeviceDiscoverEntity: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete DeviceDiscoverEntity: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error deleting DeviceDiscoverEntity", "DeviceDiscoverDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete DeviceDiscoverEntity: " + ex);
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

            Criteria criteria = session.createCriteria(DeviceDiscoverEntity.class);
            BaseDAO.addFilterCriteria(criteria, filterInfos, DeviceDiscoverEntity.class);
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

    public boolean updateDeviceDiscoverEntity(DeviceDiscoverEntity deviceDiscoverEntity) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;
        boolean updatedFlag = false;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from DeviceDiscoverEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", deviceDiscoverEntity.getId());

            DeviceDiscoverEntity databaseDeviceDiscoverEntity = (DeviceDiscoverEntity) query.setMaxResults(1).uniqueResult();
            if (databaseDeviceDiscoverEntity == null) {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND,
                        AsmManagerMessages.notFound(deviceDiscoverEntity.getId()));
            }

            databaseDeviceDiscoverEntity.setStatus(deviceDiscoverEntity.getStatus());
            databaseDeviceDiscoverEntity.setUpdatedDate(new GregorianCalendar());
            databaseDeviceDiscoverEntity.setUpdatedBy(_dao.extractUserFromRequest());

            session.saveOrUpdate(databaseDeviceDiscoverEntity);

            // Commit transaction.
            tx.commit();
            updatedFlag = true;
        } catch (Exception e) {
            logger.warn("Caught exception during update device discover entity : " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                    updatedFlag = false;
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update device discover entity : " + ex);
            }
            if (e instanceof AsmManagerCheckedException) {
                throw e;
            }
            throw new AsmManagerInternalErrorException("Update device discover entity", "DeviceDiscoverDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during update device discover entity : " + ex);
            }
        }

        return updatedFlag;
    }

    /**
     * Delete all records from DeviceDiscoverEntity.
     */
    public void deleteAll() {

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // execute command.
            String hql = "delete DeviceDiscoverEntity";
            Query query = session.createQuery(hql);
            int rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" + rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete DeviceDiscoverEntity: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete DeviceDiscoverEntity: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error deleting DeviceDiscoverEntity", "DeviceDiscoverDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete DeviceDiscoverEntity: " + ex);
            }
        }
    }
}
