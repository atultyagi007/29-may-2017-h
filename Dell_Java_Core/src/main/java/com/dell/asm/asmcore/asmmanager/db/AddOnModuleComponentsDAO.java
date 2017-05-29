package com.dell.asm.asmcore.asmmanager.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

public class AddOnModuleComponentsDAO {

    private static final Logger logger = Logger.getLogger(AddOnModuleComponentsDAO.class);

    // DB access.
    private BaseDAO dao = BaseDAO.getInstance();

    // Singleton instance.
    private static AddOnModuleComponentsDAO instance;

    private AddOnModuleComponentsDAO() {
    }

    public static synchronized AddOnModuleComponentsDAO getInstance() {
        if (instance == null) {
            instance = new AddOnModuleComponentsDAO();
        }
        return instance;
    }

    public AddOnModuleComponentEntity create(final AddOnModuleComponentEntity addOnModuleComponent) throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;

        // Save the deployment in the db.
        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();
            session.save(addOnModuleComponent);

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
            throw new AsmManagerInternalErrorException("create() addOnModuleComponent", "addOnModuleComponentsDAO", e);
        } finally {
            cleanupSession(session, "create addOnModuleComponent");
        }

        return addOnModuleComponent;
    }

    public AddOnModuleComponentEntity get(final String addOnModuleComponentId) {
        if (StringUtils.isBlank(addOnModuleComponentId)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from AddOnModuleComponentEntity where id = :id";
            final Query query = session.createQuery(hql);
            query.setString("id", addOnModuleComponentId);
            return (AddOnModuleComponentEntity) query.uniqueResult();
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get() addOnModuleComponent", "addOnModuleComponentsDAO", e);
        } finally {
            cleanupSession(session, "get addOnModuleComponent");
        }
    }

    @SuppressWarnings("unchecked")
    public List<AddOnModuleComponentEntity> getAll(boolean includeModule) {
        Session session = null;
        Transaction tx = null;
        final List<AddOnModuleComponentEntity> entities = new ArrayList<AddOnModuleComponentEntity>();

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from AddOnModuleComponentEntity";
            final Query query = session.createQuery(hql);
            entities.addAll(query.list());
            if (includeModule) {
                for (AddOnModuleComponentEntity entity : entities) {
                    Hibernate.initialize(entity.getAddOnModuleEntity());
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
            throw new AsmManagerInternalErrorException("getAll() addOnModuleComponent", "AddOnModuleComponentsDAO", e);
        } finally {
            cleanupSession(session, "getAll addOnModuleComponent");
        }

        return entities;
    }

    public void delete(String addOnModuleComponentId) {
        logger.info("Deleting add on module component: " + addOnModuleComponentId);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from AddOnModuleComponentEntity where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", addOnModuleComponentId);
            AddOnModuleEntity addOnModuleEntity = (AddOnModuleEntity) query.setMaxResults(1).uniqueResult();

            session.delete(addOnModuleEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete addOnModuleComponent: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete addOnModuleComponent: " + ex);
            }
            throw new AsmManagerInternalErrorException("delete() addOnModuleComponent", "AddOnModuleComponentsDAO", e);
        } finally {
            cleanupSession(session, "delete addOnModuleComponent");
        }
    }

    private AddOnModuleComponentEntity findComponentByName(String componetName) {
        List<AddOnModuleComponentEntity> entities = getAll(false);
        for (AddOnModuleComponentEntity entity : entities) {
            if (entity.getName().equals(componetName)) {
                return entity;
            }
        }
        return null;
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
