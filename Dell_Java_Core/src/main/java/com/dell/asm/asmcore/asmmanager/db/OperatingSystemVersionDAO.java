package com.dell.asm.asmcore.asmmanager.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dell.asm.asmcore.asmmanager.db.entity.OperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

public class OperatingSystemVersionDAO {

    private static final Logger logger = Logger.getLogger(OperatingSystemVersionDAO.class);

    private GenericDAO dao = GenericDAO.getInstance();
    
    // Singleton instance.
    private static OperatingSystemVersionDAO instance;
    
    public static synchronized OperatingSystemVersionDAO getInstance() {
        if (instance == null) {
            instance = new OperatingSystemVersionDAO();
        }
        return instance;
    }

    private OperatingSystemVersionDAO() {}
    
    public OperatingSystemVersionEntity create(final OperatingSystemVersionEntity operatingSystemVersion) 
            throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            
            session.save(operatingSystemVersion);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during operating system version creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create operating system version: " + ex);
            }
            throw new AsmManagerInternalErrorException("create() OperatingSystemVersion", "OperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "create operatingSystemVersion");
        }

        return operatingSystemVersion;
    }    
    
    private final static String QUERY_BY_ID = "from OperatingSystemVersionEntity osve where osve.id = :id";
    
    public OperatingSystemVersionEntity get(final String operatingSystemVersionId) {
        if (StringUtils.isBlank(operatingSystemVersionId)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final Query query = session.createQuery(QUERY_BY_ID);
            query.setString("id", operatingSystemVersionId);
            return (OperatingSystemVersionEntity) query.uniqueResult();
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get() OperatingSystemVersion", "OperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "get operatingSystemVersion");
        }
    }    
    
    private static final String QUERY_ALL = "from OperatingSystemVersionEntity";
    
    @SuppressWarnings("unchecked")
    public List<OperatingSystemVersionEntity> getAll() {
        Session session = null;
        Transaction tx = null;
        final List<OperatingSystemVersionEntity> entities = new ArrayList<OperatingSystemVersionEntity>();

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final Query query = session.createQuery(QUERY_ALL);
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
            throw new AsmManagerInternalErrorException("getAll() OperatingSystemVersionDAO", "OperatingSystemVersionDAODAO", e);
        } finally {
            dao.cleanupSession(session, "getAll operatingSystemVersion");
        }

        return entities;
    }

    private static final String QUERY_BY_OS = "from OperatingSystemVersionEntity osve" 
            + " where osve.operatingSystem = :operatingSystem";

    public List<OperatingSystemVersionEntity> findByOs(final String operatingSystem) {
        if (StringUtils.isBlank(operatingSystem)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final Query query = session.createQuery(QUERY_BY_OS_VERSION);
            query.setString("operatingSystem", operatingSystem);
            return (List<OperatingSystemVersionEntity>) query.list();
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get() OperatingSystemVersion", "OperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "get operatingSystemVersion");
        }
    }
    
    
    private static final String QUERY_BY_OS_VERSION = "from OperatingSystemVersionEntity osve" 
            + " where osve.operatingSystem = :operatingSystem"
            + " and osve.version = :version";
    
    public OperatingSystemVersionEntity findByOsVersion(final String operatingSystem, final String version) {
        if (StringUtils.isBlank(operatingSystem) || StringUtils.isBlank(version)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final Query query = session.createQuery(QUERY_BY_OS_VERSION);
            query.setString("operatingSystem", operatingSystem);
            query.setString("version", version);
            return (OperatingSystemVersionEntity) query.uniqueResult();
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get() OperatingSystemVersion", "OperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "get operatingSystemVersion");
        }
    }
    
    public void delete(String operatingSystemVersionId) {
        logger.info("Deleting operating system version: " + operatingSystemVersionId);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            Query query = session.createQuery(QUERY_BY_ID);
            query.setString("id", operatingSystemVersionId);
            OperatingSystemVersionEntity operatingSystemVersionEntity = 
                    (OperatingSystemVersionEntity) query.setMaxResults(1).uniqueResult();

            session.delete(operatingSystemVersionEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete operatingSystemVersion: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete operatingSystemVersion: " + ex);
            }
            throw new AsmManagerInternalErrorException("delete() OperatingSystemVersion", 
                    "OperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "delete operatingSystemVersion");
        }
    }    
}
