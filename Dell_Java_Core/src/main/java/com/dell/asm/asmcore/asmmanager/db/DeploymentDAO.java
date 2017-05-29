package com.dell.asm.asmcore.asmmanager.db;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.FilterParamParser.FilterInfo;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;

/**
 * Created with IntelliJ IDEA.
 * User: J_Fowler
 * Date: 1/7/14
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeploymentDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(DeploymentDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static DeploymentDAO instance;
    private static final String SERVER_FILTER_PARAM = "server";

    public static final int NONE = 0;                           // 0000
    public static final int DEVICE_INVENTORY_ENTITIES = 1;      // 0001
    public static final int FIRMWARE_REPOSITORY_ENTITY = 2;     // 0010
    public static final int ADD_ON_MODULE_ENTITIES = 4;         // 0100
    public static final int DEPLOYMENT_NAMES_REF_ENTITIES = 8;  // 1000
    public static final int ALL_ENTITIES = 15;                  // 1111

    public DeploymentDAO() {
    }

    public static synchronized DeploymentDAO getInstance() {
        if (instance == null)
            instance = new DeploymentDAO();
        return instance;
    }

    public DeploymentEntity createDeployment(DeploymentEntity deployment) throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;

        // Save the deployment in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            deployment.setCreatedDate(new GregorianCalendar());
            deployment.setCreatedBy(_dao.extractUserFromRequest());



            session.save(deployment);

            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during deployment inventory creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create deployment: " + ex);
            }
            if (cve.getConstraintName().contains("deployment_id_pkey")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else if (cve.getConstraintName().contains("deployment_name")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_NAME,
                        AsmManagerMessages.duplicateServiceTag(cve.getSQLException().getMessage()));
            } else {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during deployment creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create deployment: " + ex);
            }
            throw new AsmManagerInternalErrorException("Add deployment", "DeploymentDAO", e);
        } finally {
            cleanupSession(session, "create deployment");
        }

        return deployment;
    }

    public DeploymentEntity getDeployment(String refId, Integer entitiesFlag) {

        Session session = null;
        Transaction tx = null;
        DeploymentEntity deploymentEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", refId);
            deploymentEntity = (DeploymentEntity) query.setMaxResults(1).uniqueResult();
            if (deploymentEntity != null)
            {
                initializeEntity(deploymentEntity, entitiesFlag);
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get deployment for refId: " + refId + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get deployment: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve deployment", "DeploymentDAO", e);
        } finally {
            cleanupSession(session, "get deployment");
        }
        return deploymentEntity;
    }

    public DeploymentEntity getDeploymentByName(String name, int entitiesFlag) {

        Session session = null;
        Transaction tx = null;
        DeploymentEntity deploymentEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentEntity where name = :name";
            Query query = session.createQuery(hql);
            query.setString("name", name);
            deploymentEntity = (DeploymentEntity) query.setMaxResults(1).uniqueResult();
            if (deploymentEntity != null)
            {
                initializeEntity(deploymentEntity, entitiesFlag);
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get deployment for name: " + name + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get deployment: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve deployment", "DeploymentDAO", e);
        } finally {
            cleanupSession(session, "get deployment");
        }

        return deploymentEntity;
    }

    /*************************************************************************************
     ***************/
    /**
     * Retrieve all from Device Inventory.
     *
     * @return list of entities
     */
    @SuppressWarnings("unchecked")
    public List<DeploymentEntity> getAllDeployments(List<SortParamParser.SortInfo> sortInfos, List<FilterParamParser.FilterInfo> filterInfos,
                                                    PaginationInfo paginationInfo, int entitiesFlag) throws AsmManagerDAOException {

        Session session = null;
        Transaction tx = null;
        List<DeploymentEntity> entityList = new ArrayList<DeploymentEntity>();

        try {
            List<String> servicesByServers = ListUtils.EMPTY_LIST;

            if (CollectionUtils.isNotEmpty(filterInfos)) {
                for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
                    // if there is a service filter then grab the ref id for the device
                    if (StringUtils.equals(SERVER_FILTER_PARAM, filterInfo.getColumnName())) {
                        servicesByServers = getServicesByServers(filterInfo.getColumnValue());
                    }
                }
            }

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeploymentEntity.class);
            if (sortInfos != null) {
                BaseDAO.addSortCriteria(criteria, sortInfos);
            }

            List<FilterParamParser.FilterInfo> filterCriteriaNotFound = null;
            if (filterInfos != null) {
                filterCriteriaNotFound = BaseDAO.addFilterCriteria(criteria, filterInfos, DeploymentEntity.class);

                if (filterCriteriaNotFound != null && filterCriteriaNotFound.size() > 0) {
                    for (FilterParamParser.FilterInfo filterInfo : filterCriteriaNotFound) {
                        if (StringUtils.equals(SERVER_FILTER_PARAM, filterInfo.getColumnName())) {
                            if (CollectionUtils.isNotEmpty(servicesByServers)) {
                                criteria.add(Restrictions.in("id", servicesByServers));
                            }else{
                                criteria.add(Restrictions.sqlRestriction("(1=0)"));
                            }
                        }
                    }
                }
            }

            if (paginationInfo != null) {
                int offset = paginationInfo.getOffset();
                int limit = paginationInfo.getLimit();
                int pageSize = limit;
                criteria.setFirstResult(offset);
                criteria.setMaxResults(pageSize);
            }
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            entityList = criteria.list();
            intializeDeploymentEntities(entityList,entitiesFlag);
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all deployments: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all deployments: " + ex);
            }
            throw new AsmManagerDAOException("Caught exception during get all deployments: ", e);
        } finally {
            cleanupSession(session, "get all deployments");
        }

        return entityList;
    }
    
    
    public List<DeploymentEntity> getAllDeployment(int entitiesFlag) {

        Session session = null;
        Transaction tx = null;
        List<DeploymentEntity> entityList = new ArrayList<DeploymentEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentEntity";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
                entityList.add((DeploymentEntity) result);
            }
            intializeDeploymentEntities(entityList,entitiesFlag);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all deployments: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all deployments: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all deployments", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "get all deployments");
        }

        return entityList;
    }

    public void updateDeployment(DeploymentEntity updatedDeployment) throws AsmManagerCheckedException, 
            InvocationTargetException, IllegalAccessException {

        logger.info("Deployment is being updated: " + updatedDeployment.getId() + "\n     " 
                + Arrays.toString(Thread.currentThread().getStackTrace()));

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            updatedDeployment.setUpdatedDate(new GregorianCalendar());
            updatedDeployment.setUpdatedBy(_dao.extractUserFromRequest());
            session.saveOrUpdate(updatedDeployment);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during update deployment: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update deployment: " + ex);
            }
            if (e instanceof AsmManagerCheckedException) {
                AsmManagerCheckedException ace = (AsmManagerCheckedException) e;
                throw e;
            }
            throw new AsmManagerInternalErrorException("Update deployment", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "update deployment");
        }
    }

    private void cleanupSession(Session session, String operation) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception ex) {
            logger.warn("Unable to close session during " + operation + ": " + ex);
        }
    }

    /**
     * Delete Device Inventory by refId.
     * @param refId the refId used to delete from inventory.
     */
    public void deleteDeployment(String refId) {

        logger.info("Deleting deployment from inventory: " + refId);
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", refId);
            DeploymentEntity deploymentEntity = (DeploymentEntity) query.setMaxResults(1).uniqueResult();

            session.delete(deploymentEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete deployment: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete deployment: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete deployment", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "delete deployment");
        }
    }

    public Integer getTotalRecords(List<FilterInfo> filterInfos) {
        long totalRecords = 0;
        Session session = null;
        Transaction tx = null;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeploymentEntity.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            BaseDAO.addFilterCriteria(criteria, filterInfos, DeploymentEntity.class);
            totalRecords = (long) criteria.setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
        } catch (Exception e) {
            logger.error("Exception while getting getTotalRecords", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e2) {
                    logger.warn("Error during rollback", e2);
                }
            }
        } finally {
            cleanupSession(session, "session close");
        }

        return (int) totalRecords;
    }

    @SuppressWarnings("unchecked")
    private List<String> getServicesByServers(final List<String> serverIds) {
        final String serverIdsJoined = "'" + StringUtils.join(serverIds, "','") + "'";
        Session session = null;
        Transaction tx;

        List<String> refIds = ListUtils.EMPTY_LIST;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            final String sql =
                    "select deployment_id from deployment_to_device_map where device_id in ( :serverIdsList )";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameterList("serverIdsList", serverIds);
            refIds = query.list();
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception getting deployments for server ids " + serverIdsJoined, e);
            throw new AsmManagerInternalErrorException("getServicesByServers", "DeploymentDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during ref Id retrieval: " + ex);
            }
        }

        return refIds;
    }

    // NOTE: Due to lack of transactions at the service level, logic is being  pushed down into the DAO.  This 
    //       is technical debt that will need to be cleaned up if / when Transactions are supported properly 
    //       at the Service level.
    public DeploymentEntity updateDeploymentStatusToInProgress(String deploymentId) {
        DeploymentEntity deploymentEntity = null; 
        
        logger.info("Deployment's Status is being updated to In_Progress for deploymentId: " + deploymentId + "\n     " 
                + Arrays.toString(Thread.currentThread().getStackTrace()));

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            // Uses a Pessimistic lock to ensure there is row level locking 
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE));
            
            tx = session.beginTransaction();
            
            // Create and execute command.
            String hql = "from DeploymentEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", deploymentId);
            deploymentEntity = (DeploymentEntity) query.setMaxResults(1).uniqueResult();
            if (deploymentEntity != null) {
                  initializeEntity(deploymentEntity, ALL_ENTITIES);
            }
            if (DeploymentStatusType.CANCELLED.equals(deploymentEntity.getStatus()) || 
                DeploymentStatusType.COMPLETE.equals(deploymentEntity.getStatus()) ||    
                DeploymentStatusType.PENDING.equals(deploymentEntity.getStatus()) ||
                DeploymentStatusType.ERROR.equals(deploymentEntity.getStatus())) {

                deploymentEntity.setUpdatedDate(new GregorianCalendar());
                deploymentEntity.setUpdatedBy(_dao.extractUserFromRequest());
                deploymentEntity.setStatus(DeploymentStatusType.IN_PROGRESS);
                session.saveOrUpdate(deploymentEntity);
            }
            else {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.serviceUpdateInProgressError(deploymentEntity.getName()));
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during updateDeploymentStatusToInProgress: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during updateDeploymentStatusToInProgress: " + ex);
            }
            
            if (e instanceof LocalizedWebApplicationException || e instanceof AsmManagerCheckedException) {
                throw e;
            }
            
            throw new AsmManagerInternalErrorException("updateDeploymentStatusToInProgress", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "updateDeploymentStatusToInProgress");
        }
        
        return deploymentEntity;
    }

    public List<DeploymentEntity> getDeploymentsForUserIds(Set<String> userIds) {

        Session session = null;
        Transaction tx = null;
        List<DeploymentEntity> entityList = new ArrayList<DeploymentEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentEntity where id in (select deploymentId from DeploymentUserRefEntity where user_id in ( :userIdsSet ))";
            Query query = session.createQuery(hql);
            query.setParameterList("userIdsSet",userIds);
            for (Object result : query.list()) {
                entityList.add((DeploymentEntity) result);
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all deployments: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all deployments: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all deployments for userIds", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "get all deployments for userIds");
        }

        return entityList;
    }

    private void intializeDeploymentEntities(List<DeploymentEntity> deploymentEntities, int entitiesFlag) {
        if (entitiesFlag == NONE) {
            return;
        }
        if (deploymentEntities != null && deploymentEntities.size() > 0) {
            for (DeploymentEntity deployment : deploymentEntities ) {
                initializeEntity(deployment, entitiesFlag);
            }
        }
    }

    private void initializeEntity(DeploymentEntity entity, int entitiesFlag) {
        if (entitiesFlag == NONE) {
            return;
        }
        if (entity != null) {
            if ((entitiesFlag & DEVICE_INVENTORY_ENTITIES) == DEVICE_INVENTORY_ENTITIES) {
                Hibernate.initialize(entity.getDeployedDevices());
            }
            if ((entitiesFlag & FIRMWARE_REPOSITORY_ENTITY) == FIRMWARE_REPOSITORY_ENTITY) {
                Hibernate.initialize(entity.getFirmwareRepositoryEntity());
            }
            if ((entitiesFlag & ADD_ON_MODULE_ENTITIES) == ADD_ON_MODULE_ENTITIES) {
                Hibernate.initialize(entity.getAddOnModules());
            }
            if ((entitiesFlag & DEPLOYMENT_NAMES_REF_ENTITIES) == DEPLOYMENT_NAMES_REF_ENTITIES) {
                Hibernate.initialize(entity.getNamesRefs());
            }
        }
    }

    public List<DeploymentEntity> getAllDefaultCatalogDeployments() {

        Session session = null;
        Transaction tx = null;
        List<DeploymentEntity> entityList = new ArrayList<DeploymentEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentEntity where useDefaultCatalog = TRUE";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
                DeploymentEntity entity = (DeploymentEntity) result;
                initializeEntity(entity,DEVICE_INVENTORY_ENTITIES + FIRMWARE_REPOSITORY_ENTITY);
                entityList.add(entity);
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during getAllDefaultCatalogDeployments: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getAllDefaultCatalogDeployments: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all default catalog deployments", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "getAllDefaultCatalogDeployments");
        }

        return entityList;
    }


}
