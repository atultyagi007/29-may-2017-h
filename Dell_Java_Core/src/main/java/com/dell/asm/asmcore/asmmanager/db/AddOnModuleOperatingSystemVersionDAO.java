package com.dell.asm.asmcore.asmmanager.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleOperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.OperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

public class AddOnModuleOperatingSystemVersionDAO {

    private static final Logger logger = Logger.getLogger(AddOnModuleOperatingSystemVersionDAO.class);
    
    private GenericDAO dao = GenericDAO.getInstance();
    
    // Singleton instance.
    private static AddOnModuleOperatingSystemVersionDAO instance;
    
    public static synchronized AddOnModuleOperatingSystemVersionDAO getInstance() {
        if (instance == null) {
            instance = new AddOnModuleOperatingSystemVersionDAO();
        }
        return instance;
    }
    
    private AddOnModuleOperatingSystemVersionDAO() {}

    public AddOnModuleOperatingSystemVersionEntity saveOrUpdate(final AddOnModuleOperatingSystemVersionEntity entity) {
        Session session = null;
        Transaction tx = null;
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final AddOnModuleOperatingSystemVersionEntity persisted = (AddOnModuleOperatingSystemVersionEntity) 
                    session.get(AddOnModuleOperatingSystemVersionEntity.class, 
                            entity.getAddOnModuleOperatingSystemVersionId());
            // this is probably overkill for now but in the event we add other fields that may update differently from
            // initial saves - such as updatedDate
            if (persisted == null) {
                session.save(entity);
            } else {
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
            throw new AsmManagerInternalErrorException("saveOrUpdate", "AddOnModuleOperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "saveOrUpdate");
        }
        
        return entity;
    }
    
    public AddOnModuleOperatingSystemVersionEntity get(final AddOnModuleEntity addOnModule, 
            final OperatingSystemVersionEntity operatingSystemVersion) {
        if (addOnModule == null || operatingSystemVersion == null) {
            return null;
        }
        return get(addOnModule.getId(), operatingSystemVersion.getId());
    }
    
    public AddOnModuleOperatingSystemVersionEntity get(final String addOnModuleId, 
            final String operatingSystemVersionId) {
        if (StringUtils.isBlank(addOnModuleId) || StringUtils.isBlank(operatingSystemVersionId)) {
            return null;
        }
        Session session = null;
        Transaction tx = null;
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final AddOnModuleEntity addOnModule = (AddOnModuleEntity) 
                    session.get(AddOnModuleEntity.class, addOnModuleId);
            if (addOnModule == null) {
                return null;
            }
            final OperatingSystemVersionEntity operatingSystemVersion = (OperatingSystemVersionEntity) 
                    session.get(OperatingSystemVersionEntity.class, operatingSystemVersionId);
            if (operatingSystemVersion == null) {
                return null;
            }
            
            final String hql = "from AddOnModuleOperatingSystemVersionEntity aonmosv"
                    + " where aonmosv.addOnModuleComplianceId.addOnModule = :addOnModule"
                    + " and aonmosv.addOnModuleComplianceId.operatingSystemVersion = :operatingSystemVersion";
            final Query query = session.createQuery(hql);
            query.setEntity("addOnModule", addOnModule);
            query.setEntity("operatingSystemVersion", operatingSystemVersion);
            return (AddOnModuleOperatingSystemVersionEntity)query.uniqueResult();
        } catch (Exception e) {
            logger.warn("Caught exception during get: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get: " + ex);
            }
            throw new AsmManagerInternalErrorException("get", "AddOnModuleOperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "get");
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<AddOnModuleOperatingSystemVersionEntity> getAll() {
        Session session = null;
        Transaction tx = null;
        final List<AddOnModuleOperatingSystemVersionEntity> entities = 
                new ArrayList<AddOnModuleOperatingSystemVersionEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final String hql = "from AddOnModuleOperatingSystemVersionEntity";
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
            throw new AsmManagerInternalErrorException("getAll", "AddOnModuleOperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "getAll");
        }
        
        return entities;
    }
    
    public List<AddOnModuleOperatingSystemVersionEntity> findByAddOnModule(final AddOnModuleEntity addOnModule) {
        if (addOnModule == null) {
            return null;
        }
        return findByAddOnModule(addOnModule.getId());
    }
    
    @SuppressWarnings("unchecked")
    public List<AddOnModuleOperatingSystemVersionEntity> findByAddOnModule(final String addOnModuleId) {
        if (StringUtils.isBlank(addOnModuleId)) {
            return Collections.emptyList();
        }
        Session session = null;
        Transaction tx = null;
        final List<AddOnModuleOperatingSystemVersionEntity> entities = 
                new ArrayList<AddOnModuleOperatingSystemVersionEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final AddOnModuleEntity addOnModule = 
                    (AddOnModuleEntity) session.get(AddOnModuleEntity.class, addOnModuleId);
            if (addOnModule == null) {
                return Collections.emptyList();
            }
            
            final String hql = "from AddOnModuleOperatingSystemVersionEntity aonmosv" 
                    + " where aonmosv.addOnModuleOperatingSystemVersionId.addOnModule = :addOnModuleId";
            final Query query = session.createQuery(hql);
            query.setString("addOnModuleId", addOnModuleId);
            entities.addAll(query.list());
        } catch (Exception e) {
            logger.warn("Caught exception during findByAddOnModule: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during findByAddOnModule: " + ex);
            }
            throw new AsmManagerInternalErrorException("findByAddOnModule", "AddOnModuleOperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "findByAddOnModule");
        }
        
        return entities;
    }

    public List<AddOnModuleOperatingSystemVersionEntity> findByOperatingSystemVersion(
            final OperatingSystemVersionEntity operatingSystemVersion) {
        if (operatingSystemVersion == null) {
            return null;
        }
        return findByOperatingSystemVersion(operatingSystemVersion.getId());
    }
    
    @SuppressWarnings("unchecked")
    public List<AddOnModuleOperatingSystemVersionEntity> findByOperatingSystemVersion(
            final String operatingSystemVersionId) {
        if (StringUtils.isBlank(operatingSystemVersionId)) {
            return Collections.emptyList();
        }
        Session session = null;
        Transaction tx = null;
        final List<AddOnModuleOperatingSystemVersionEntity> entities = 
                new ArrayList<AddOnModuleOperatingSystemVersionEntity>();
        
        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();
            final OperatingSystemVersionEntity operatingSystemVersion = (OperatingSystemVersionEntity) 
                    session.get(OperatingSystemVersionEntity.class, operatingSystemVersionId);
            if (operatingSystemVersion == null) {
                return Collections.emptyList();
            }
            
            final String hql = "from AddOnModuleOperatingSystemVersionEntity aonmosv"
                    + " where aonmosv.addOnModuleComplianceId.operatingSystemVersion = :operatingSystemVersion";
            final Query query = session.createQuery(hql);
            query.setEntity("operatingSystemVersion", operatingSystemVersion);
            entities.addAll(query.list());
        } catch (Exception e) {
            logger.warn("Caught exception during findByOperatingSystemVersion: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during findByOperatingSystemVersion: " + ex);
            }
            throw new AsmManagerInternalErrorException("findByOperatingSystemVersion", 
                    "AddOnModuleOperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "findByOperatingSystemVersion");
        }
        
        return entities;
    }
    
    public void delete(final AddOnModuleOperatingSystemVersionEntity entity) {
        if (entity == null) {
            return;
        }
        delete(entity.getAddOnModule(), entity.getOperatingSystemVersion());
    }
    
    public void delete(final AddOnModuleEntity addOnModule, final OperatingSystemVersionEntity operatingSystemVersion) {
        if (addOnModule == null || operatingSystemVersion == null) {
            return;
        }
        delete(addOnModule.getId(), operatingSystemVersion.getId());
    }
    
    public void delete(final String addOnModuleId, final String operatingSystemVersionId) {
        if (StringUtils.isBlank(addOnModuleId) || StringUtils.isBlank(operatingSystemVersionId)) {
            return;
        }
        logger.info("Deleting add on module operating system version mapping: " 
                + addOnModuleId + "," + operatingSystemVersionId);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao.getNewSession();
            tx = session.beginTransaction();

            String hql = "delete AddOnModuleOperatingSystemVersionEntity aonmosv "
                    + " where aonmosv.addOnModuleComplianceId.addOnModuleId = :addOnModuleId"
                    + " and aonmosv.addOnModuleComplianceId.operatingSystemVersionId = :operatingSystemVersionId";
            Query query = session.createQuery(hql);
            query.setString("addOnModuleId", addOnModuleId);
            query.setString("operatingSystemVersionId", operatingSystemVersionId);
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
            throw new AsmManagerInternalErrorException("Delete", "AddOnModuleOperatingSystemVersionDAO", e);
        } finally {
            dao.cleanupSession(session, "delete operatingSystemVersion");
        }
    }
}
