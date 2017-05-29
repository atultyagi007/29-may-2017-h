package com.dell.asm.asmcore.asmmanager.db;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.dell.asm.asmcore.asmmanager.client.addonmodule.AddOnModule;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.orion.common.print.Dump;

public class AddOnModuleDAO {

    private static final Logger logger = Logger.getLogger(AddOnModuleDAO.class);

    // DB access.
    private BaseDAO dao = BaseDAO.getInstance();

    // Singleton instance.
    private static AddOnModuleDAO instance;

    private AddOnModuleDAO() {
    }

    public static synchronized AddOnModuleDAO getInstance() {
        if (instance == null) {
            instance = new AddOnModuleDAO();
        }
        return instance;
    }

    public AddOnModuleEntity create(final AddOnModuleEntity addOnModule) throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;

        // Save the deployment in the db.
        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            addOnModule.setUploadedBy(dao.extractUserFromRequest());
            addOnModule.setUploadedDate(new GregorianCalendar());

            session.save(addOnModule);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during add on module creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create add on module: " + ex);
            }
            throw new AsmManagerInternalErrorException("create() AddOnModule", "AddOnModuleDAO", e);
        } finally {
            cleanupSession(session, "create addOnModule");
        }

        return addOnModule;
    }

    public AddOnModuleEntity get(final String addOnModuleId) {
        if (StringUtils.isBlank(addOnModuleId)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from AddOnModuleEntity where id = :id";
            final Query query = session.createQuery(hql);
            query.setString("id", addOnModuleId);
            AddOnModuleEntity entity =  (AddOnModuleEntity) query.uniqueResult();
            if (entity != null) {
                Hibernate.initialize(entity.getAddOnModuleComponents());
            }
            return entity;
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get() AddOnModule", "AddOnModuleDAO", e);
        } finally {
            cleanupSession(session, "get addOnModule");
        }
    }

    public AddOnModuleEntity getComplete(final String addOnModuleId) {
        if (StringUtils.isBlank(addOnModuleId)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from AddOnModuleEntity where id = :id";
            final Query query = session.createQuery(hql);
            query.setString("id", addOnModuleId);
            AddOnModuleEntity entity =  (AddOnModuleEntity) query.uniqueResult();
            if (entity != null) {
                Hibernate.initialize(entity.getAddOnModuleComponents());
                Hibernate.initialize(entity.getDeploymentEntities());
                Hibernate.initialize(entity.getServiceTemplateEntities());
            }
            return entity;
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get() AddOnModule", "AddOnModuleDAO", e);
        } finally {
            cleanupSession(session, "get addOnModule");
        }
    }


    @SuppressWarnings("unchecked")
    public List<AddOnModuleEntity> getAll(boolean includeComponents) {
        Session session = null;
        Transaction tx = null;
        final List<AddOnModuleEntity> entities = new ArrayList<AddOnModuleEntity>();

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from AddOnModuleEntity";
            final Query query = session.createQuery(hql);
            entities.addAll(query.list());
            if (includeComponents) {
                for (AddOnModuleEntity entity : entities) {
                    Hibernate.initialize(entity.getAddOnModuleComponents());
                }
            }
        } catch (Exception e) {
            logger.warn("Caught exception during getAll: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getAll: " + ex);
            }
            throw new AsmManagerInternalErrorException("getAll() AddOnModule", "AddOnModuleDAO", e);
        } finally {
            cleanupSession(session, "getAll addOnModule");
        }

        return entities;
    }

    @SuppressWarnings("unchecked")
    public List<AddOnModuleEntity> getAllAddOnModules(List<SortParamParser.SortInfo> sortInfos,
                                                      List<FilterParamParser.FilterInfo> filterInfos) {
        Session session = null;
        Transaction tx = null;
        final List<AddOnModuleEntity> entities = new ArrayList<AddOnModuleEntity>();

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(AddOnModuleEntity.class);
            if (sortInfos != null) {
                BaseDAO.addSortCriteria(criteria, sortInfos);
            }

            List<FilterParamParser.FilterInfo> notFound = null;
            if (filterInfos != null) {
                notFound = BaseDAO.addFilterCriteria(criteria, filterInfos, AddOnModuleEntity.class);

                if (notFound != null && notFound.size() > 0) {
                    logger.info("we had a notFound.");
                    criteria.createAlias("AddOnModuleEntity", "addOnModuleEntityAlias");
                    for (FilterParamParser.FilterInfo filterInfo : notFound) {
                        logger.info(Dump.toString(filterInfo) + " was not Found.");
                        criteria.add(Restrictions.eq("addOnModuleEntityAlias.deviceKey", filterInfo.getColumnName()));
                        if (filterInfo.getColumnValue().size() == 1) {
                            criteria.add(Restrictions.eq("addOnModuleEntityAlias.deviceValue", filterInfo.getColumnValue().get(0)));
                        } else if (filterInfo.getColumnValue().size() > 1) {
                            criteria.add(Restrictions.in("addOnModuleEntityAlias.deviceValue", filterInfo.getColumnValue()));
                        }
                    }
                }
            }
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            List<AddOnModuleEntity> entityList = criteria.list();
            //intialize deployments and servicetemplates
            for (AddOnModuleEntity entity : entityList) {
                Hibernate.initialize(entity.getAddOnModuleComponents());
                Hibernate.initialize(entity.getDeploymentEntities());
                Hibernate.initialize(entity.getServiceTemplateEntities());
            }
            entities.addAll(entityList);
        } catch (Exception e) {
            logger.warn("Caught exception during getAll: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getAll: " + ex);
            }
            throw new AsmManagerInternalErrorException("getAll() AddOnModule", "AddOnModuleDAO", e);
        } finally {
            cleanupSession(session, "getAll addOnModule");
        }

        return entities;
    }

    public void update(AddOnModuleEntity updatedAddOnModule) throws AsmManagerCheckedException,
            InvocationTargetException, IllegalAccessException {

        logger.info("AddOnModule is being updated: " + updatedAddOnModule + "\n     "
                + Arrays.toString(Thread.currentThread().getStackTrace()));

        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            session.saveOrUpdate(updatedAddOnModule);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during update deployment: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update addOnModule: " + ex);
            }
            if (e instanceof AsmManagerCheckedException) {
                AsmManagerCheckedException ace = (AsmManagerCheckedException) e;
                throw e;
            }
            throw new AsmManagerInternalErrorException("Update addOnModule", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "update addOnModule");
        }
    }

    public void delete(String addOnModuleId) {
        logger.info("Deleting add on module: " + addOnModuleId);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from AddOnModuleEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", addOnModuleId);
            AddOnModuleEntity addOnModuleEntity = (AddOnModuleEntity) query.setMaxResults(1).uniqueResult();

            session.delete(addOnModuleEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete addOnModule: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete addOnModule: " + ex);
            }
            throw new AsmManagerInternalErrorException("delete() AddOnModule", "AddOnModuleDAO", e);
        } finally {
            cleanupSession(session, "delete addOnModule");
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
}
