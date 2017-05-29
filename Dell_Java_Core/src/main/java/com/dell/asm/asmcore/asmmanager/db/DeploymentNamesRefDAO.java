package com.dell.asm.asmcore.asmmanager.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentNamesType;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentNamesRefEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;

/**
 * DepoymentNamesRefDAO
 */
public class DeploymentNamesRefDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(DeploymentNamesRefDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    public DeploymentNamesRefDAO() {
    }

    public List<DeploymentNamesRefEntity> getAllDeploymentNamesRefsByType(DeploymentNamesType type) {

        Session session = null;
        Transaction tx = null;
        List<DeploymentNamesRefEntity> entityList = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentNamesRefEntity where type = :type";
            Query query = session.createQuery(hql);
            query.setParameter("type", type);
            entityList = query.list();
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get deploymentNamesRefs for type: " + type.toString() + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get deploymentNamesRefs for type: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve DeploymentNamesRefEntity List", "DeploymentNamesRefDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during getAllDeploymentNamesByType: " + ex);
            }
        }

        return entityList;
    }

    public List<String> getAllNamesByType(DeploymentNamesType type) {
        Session session = null;
        Transaction tx = null;
        List<String> nameList = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "select name from DeploymentNamesRefEntity where type = :type";
            Query query = session.createQuery(hql);
            query.setParameter("type", type);
            nameList = query.list();
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get names for type: " + type.toString() + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getAllNamesByType: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve getAllNamesByType", "DeploymentNamesRefDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during getAllNamesByType: " + ex);
            }
        }

        return nameList;
    }

    /**
     * Delete Deployment Names Ref by name and type.  They should be a unique combination so we don't
     * need the deployment id.
     * @param name
     * @param type
     */
    public void deleteDeploymentNamesRef(String name, DeploymentNamesType type) {

        logger.info("Deleting Deployment Names Ref where Name: " + name + " and Type: " + type.toString());
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeploymentNamesRefEntity where name = :name and type = :type";
            Query query = session.createQuery(hql);
            query.setString("name", name);
            query.setParameter("type", type);
            DeploymentNamesRefEntity deploymentNamesRefEntity = (DeploymentNamesRefEntity) query.setMaxResults(1).uniqueResult();

            if (deploymentNamesRefEntity != null) {
                session.delete(deploymentNamesRefEntity);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete deployment names ref: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete deployment names ref: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete deployment names ref", "DeploymentNamesRefDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleteDeploymentNamesRef: " + ex);
            }
        }
    }

    /**
     * Delete orphan Deployment Names Ref - those that don't have deployment ID. These records appear in a certain cases
     * i.e. SQL operation failed on deployment update/delete, or hibernate bug.
     * Unfortunately I don't see a way to prevent it.
     */
    public void deleteOrphanNamesRef() {

        logger.info("Deleting orphan Deployment Names Ref where Name");
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "delete from DeploymentNamesRefEntity where deploymentId is null";
            Query query = session.createQuery(hql);
            query.executeUpdate();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete deployment names ref: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete deployment names ref: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete deployment names ref", "DeploymentNamesRefDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleteOrphanNamesRef: " + ex);
            }
        }
    }

}
