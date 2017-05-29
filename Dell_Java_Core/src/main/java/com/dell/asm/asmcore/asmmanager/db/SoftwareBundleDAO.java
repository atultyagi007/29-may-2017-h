package com.dell.asm.asmcore.asmmanager.db;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.firmware.BundleType;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareComponentID;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareBundle;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SystemIDEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.model.LocalizedValidationResult;
import com.dell.pg.orion.common.context.ServiceContext;
import com.dell.pg.orion.common.utilities.VersionUtils;

public class SoftwareBundleDAO {

    // Logger.
    private static final Logger logger = Logger.getLogger(SoftwareBundleDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static SoftwareBundleDAO instance;

    private final GenericDAO genericDAO = GenericDAO.getInstance();

    // For creation of custom 'User' bundles
    private static final String DEFAULT_USER_BUNDLE_COMPONENT_TYPE = "Firmware";

    private SoftwareBundleDAO() {
    }

    public static synchronized SoftwareBundleDAO getInstance() {
        if (instance == null)
            instance = new SoftwareBundleDAO();
        return instance;
    }

    public FirmwareRepositoryEntity createSoftwareBundle(SoftwareBundleEntity softwareBundleEntity, String fwRepId) throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;
        try {
           	FirmwareRepositoryEntity fb = getFirmwareRepositoryEntityBundle(fwRepId);
        	session = _dao._database.getNewSession();
        	tx = session.beginTransaction();
        	session.merge(softwareBundleEntity);
           	if (fb.getSoftwareBundles() != null)
        	{
        		Set<SoftwareBundleEntity> sb = new HashSet<SoftwareBundleEntity> ();
        		sb.add(softwareBundleEntity);
        		fb.setSoftwareBundles(sb);
        	}
        	if (fb.getSoftwareComponents() != null)
        	{
        		Set<SoftwareComponentEntity> sc = new HashSet<SoftwareComponentEntity> ();
			    Collection<SoftwareComponentEntity> components =
			    softwareBundleEntity.getSoftwareComponents();
			    if ( softwareBundleEntity.isUserBundle() ) {
			        // Assert that all components ( 1 ) have valid versions.
			        LocalizedValidationResult result;
			        for ( SoftwareComponentEntity component : components ) {
				        result = validateSoftwareComponentVersion(component);
				        if ( ! result.isValid() ) {
				            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,result.getMessage());
				        }
			        }
			    }
        		sc.addAll(components);
        		fb.setSoftwareComponents(sc);
        	}
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during software bundle creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            if (cve.getConstraintName().contains("name")) {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (LocalizedWebApplicationException e) {
            logger.warn("Caught WebApplication exception during software bundle creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
	    throw e;
        } catch (Exception e) {
            logger.warn("Caught exception during software bundle creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during software bundle creation: " + ex);
            }
            throw new AsmManagerInternalErrorException("Add Software bundle", "SoftwareBundleDAO", e);
        } finally {
        	  try {
                  if (session != null)
                      session.close();
              } catch (Exception e2) {
                  logger.warn("Caught exception during bundle creation" + e2);
              }
        }

      //  return softwareBundleEntity;
        return null;
    }


    public SoftwareBundleEntity getBundleByName(String name) {

        Session session = null;
        Transaction tx = null;
        SoftwareBundleEntity bundleEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from ServiceTemplateEntity where name =:name";
            Query query = session.createQuery(hql);
            query.setString("name", name);
            bundleEntity = (SoftwareBundleEntity) query.setMaxResults(1).uniqueResult();
            if (bundleEntity != null) {
                Hibernate.initialize(bundleEntity.getSoftwareComponents());
            }
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get bundle for software bundle name: " + name + ", " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get software bundle: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create software bundle", "SoftwareBundleDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return bundleEntity;
    }


    public SoftwareBundleEntity updateSoftwareBundle(SoftwareBundleEntity bundleEntity , String fwRepoId) throws AsmManagerCheckedException {
        if ( bundleEntity.isUserBundle() ) {
            // Assert that all components ( 1 ) have valid versions.
            Collection<SoftwareComponentEntity> components =
                    bundleEntity.getSoftwareComponents();
            LocalizedValidationResult result;
            for (SoftwareComponentEntity component : components) {
                result = validateSoftwareComponentVersion(component);
                if (!result.isValid()) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, result.getMessage());
                }
            }
        }
        try {
            genericDAO.update(bundleEntity);
        } catch (Exception e) {
            logger.warn("Caught exception during update software bundle: " + e);
            throw new AsmManagerInternalErrorException("update software bundle", "SoftwareBundleDAO", e);
        }
        return bundleEntity;
    }

    public SoftwareBundleEntity getSoftwareBundleById(String Id) {

        Session session = null;
        Transaction tx = null;
        SoftwareBundleEntity bundleEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from SoftwareBundleEntity where id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", Id);
            bundleEntity = (SoftwareBundleEntity) query.setMaxResults(1).uniqueResult();
            if (bundleEntity != null) {
                Hibernate.initialize(bundleEntity.getSoftwareComponents());
            }
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get bundle for bundle id: " + Id + ", " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get software bundle: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve software by bundle by Id", "SoftwareBundleDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get software bundle : " + ex);
            }
        }

        return bundleEntity;
    }


    public SoftwareBundleEntity toBundleEntityObj(SoftwareBundle bundle, boolean update, String fwRepoId) {
    	SoftwareBundleEntity softwareBundleEntity = null;
    	FirmwareRepositoryEntity fb = null;
    	fb = getFirmwareRepositoryEntityBundle(fwRepoId);
    	if (bundle != null) {
    	 	if (update) {
           		softwareBundleEntity = getSoftwareBundleById(bundle.getId());
            	softwareBundleEntity.setUpdatedBy(ServiceContext.get().getUserName());
            	softwareBundleEntity.setUpdatedDate(ASMCommonsUtils.getUTCDate());
            } else {
                softwareBundleEntity = new SoftwareBundleEntity();
            	softwareBundleEntity.setCreatedBy(ServiceContext.get().getUserName());
            	softwareBundleEntity.setCreatedDate(ASMCommonsUtils.getUTCDate());
            	softwareBundleEntity.setBundleDate(ASMCommonsUtils.getUTCDate());
            }

           	softwareBundleEntity.setFirmwareRepositoryEntity(fb);
        	softwareBundleEntity.setName(bundle.getName());
        	softwareBundleEntity.setVersion(bundle.getVersion());
        	softwareBundleEntity.setDeviceType(bundle.getDeviceType());
        	softwareBundleEntity.setDeviceModel(bundle.getDeviceModel());
        	softwareBundleEntity.setCriticality(bundle.getCriticality());
        	softwareBundleEntity.setDescription(bundle.getDescription());

            // Indicates whether this is a user defined bundle
           	softwareBundleEntity.setUserBundle(bundle.getUserBundle());

        	if (update) {
        	    Collection<SoftwareComponentEntity> softwareComponent = softwareBundleEntity.getSoftwareComponents();
        	    Set<SoftwareComponentEntity> newSc = new HashSet<SoftwareComponentEntity> ();
        	    for (SoftwareComponentEntity comp : softwareComponent) {
		            /* Only update the path if a new path has been supplied */
		            if ( bundle.getUserBundlePath() != null && bundle.getUserBundlePath().length() != 0 ) {
			            softwareBundleEntity.setUserBundlePath(bundle.getUserBundlePath());
                        softwareBundleEntity.setUserBundleHashMd5(bundle.getUserBundleHashMd5());
		            } else {
			            softwareBundleEntity.setUserBundlePath(comp.getPath());
                        softwareBundleEntity.setUserBundleHashMd5(comp.getHashMd5());
		            }
		            SoftwareComponentEntity st = callComponentEnities(softwareBundleEntity, comp, fb);
		            newSc.add(st);
        	    }
        	    softwareBundleEntity.setSoftwareComponents(newSc);
        	} else {
           	    softwareBundleEntity.setUserBundlePath(bundle.getUserBundlePath());
                softwareBundleEntity.setUserBundleHashMd5(bundle.getUserBundleHashMd5());

		        SoftwareComponentEntity comp = new SoftwareComponentEntity();
		        SoftwareComponentEntity s = callComponentEnities(softwareBundleEntity,comp,fb );
		        softwareBundleEntity.getSoftwareComponents().add(s);
        	}
        }
        return softwareBundleEntity;
    }

	private SoftwareComponentEntity callComponentEnities(SoftwareBundleEntity softwareBundleEntity,
                                                         SoftwareComponentEntity comp,
                                                         FirmwareRepositoryEntity fb) {

		if (comp != null)
		{
		   comp.setName(softwareBundleEntity.getName() + " , " + softwareBundleEntity.getVersion());
           comp.setDellVersion(softwareBundleEntity.getVersion());
           comp.setVendorVersion(softwareBundleEntity.getVersion());
            if (softwareBundleEntity.getDeviceModel().contains("DELL_PS"))
                comp.setComponentId(FirmwareComponentID.COMPONENT_EQUALLOGIC.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_SC")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_COMPELLENT.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_S40")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_NETWORKING_S4048_ON.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_S48")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10S4810.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_S5")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10S5000.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_S6")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10S6000.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_IOM")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10IOM.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_MXL")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10IOM.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_FN2210")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10FX2.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_FN410")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_FORCE10FX2.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_N3000")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_POWERCONNECTN3000.getLabel());
            else if ((softwareBundleEntity.getDeviceModel().contains("DELL_N4000")))
                comp.setComponentId(FirmwareComponentID.COMPONENT_POWERCONNECTN4000.getLabel());

            SystemIDEntity systemID = new SystemIDEntity(Integer.parseInt(comp.getComponentId()) + "");
		    comp.getSystemIDs().add(systemID);
		    systemID.setSoftwareComponentEntity(comp);
            comp.setFirmwareRepositoryEntity(fb);

            if ( softwareBundleEntity.isUserBundle() ) {
                comp.setPath(softwareBundleEntity.getUserBundlePath());
                comp.setHashMd5(softwareBundleEntity.getUserBundleHashMd5());
                comp.setComponentType(DEFAULT_USER_BUNDLE_COMPONENT_TYPE);
            }
		}
		return comp;
	}

	private FirmwareRepositoryEntity getFirmwareRepositoryEntityBundle(String id) {

		FirmwareRepositoryEntity fb = genericDAO.get(id, FirmwareRepositoryEntity.class);
		return fb;
	}

	public SoftwareBundle toSoftwareBundleObj(SoftwareBundleEntity bundle) {
		SoftwareBundle  softwareBundle = new SoftwareBundle();
        if (bundle != null) {

        	softwareBundle.setId(bundle.getId());
        	softwareBundle.setName(bundle.getName());
           	softwareBundle.setVersion(bundle.getVersion());
            softwareBundle.setCreatedDate(bundle.getCreatedDate());
            if (BundleType.SOFTWARE.getValue().equals(bundle.getBundleType())) {
                softwareBundle.setBundleType(BundleType.SOFTWARE);
            }
            else {
                softwareBundle.setBundleType(BundleType.FIRMWARE);
            }
        }

        return softwareBundle;
	}


	public void deleteSoftwareBundle(SoftwareBundleEntity bundleEntity) {

		Session session = null;
		Transaction tx = null;

		try {
			removeSystemId(bundleEntity);
			removeSoftwareComponent(bundleEntity);
			String id = bundleEntity.getId();
			session = _dao._database.getNewSession();
			tx = session.beginTransaction();
			// execute command.
			String hql = "delete SoftwareBundleEntity  where id=:id";
			Query query = session.createQuery(hql);
			query.setString("id", id);
			int rowCount = query.executeUpdate();
			logger.debug("Deleted record count=" + rowCount);
			// Commit transaction.
			tx.commit();
		} catch (Exception e) {
			logger.warn("Caught exception during delete softwareBundle: " + e);
			try {
				if (tx != null) {
					tx.rollback();
				}
			} catch (Exception ex) {
				logger.warn("Unable to rollback transaction during delete softwareBundle: "
						+ ex);
			}

			throw new AsmManagerInternalErrorException(
					"Error deleting softwareBundle", "softwareBundleDAO", e);
		} finally {
			try {
				if (session != null) {
					session.close();
				}
			} catch (Exception ex) {
				logger.warn("Unable to close session during delete softwareBundleDAO: "
						+ ex);
			}
		}
	}

	private void removeSystemId(SoftwareBundleEntity bundleEntity) {
		Session session = null;
        Transaction tx = null;
		try
		{

        session = _dao._database.getNewSession();
        tx = session.beginTransaction();
        String softwareComponentEntity = null;

       Collection<SoftwareComponentEntity> sc = bundleEntity.getSoftwareComponents();
       for (SoftwareComponentEntity entity : sc)
       {
    	   softwareComponentEntity = entity.getId();
    	   break;
       }
       String hql = "delete SystemIDEntity  where softwareComponentEntity=:softwareComponentEntity";
       Query query = session.createQuery(hql);
       query.setString("softwareComponentEntity", softwareComponentEntity);
        int rowCount = query.executeUpdate();
        logger.debug("Deleted record count=" + rowCount);
        // Commit transaction.
        tx.commit();

		}  catch (Exception e) {
            logger.warn("Caught exception during delete software system id: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete softwarecomponent: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error deleting softwarecomponent", "softwareBundleDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete softwareBundleDAO: " + ex);
            }
        }

	}

    private void removeSoftwareComponent(SoftwareBundleEntity softwareBundle) {
        Session session = null;
        Transaction tx = null;
        try {

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            SoftwareComponentEntity[] entities = softwareBundle.getSoftwareComponents().toArray(new SoftwareComponentEntity[softwareBundle.getSoftwareComponents().size()]);
            String id = entities[0].getId();
            // execute command.
            String hql = "delete SoftwareComponentEntity  where id=:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            int rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" + rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete software component: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete softwarecomponent: " + ex);
            }

            throw new AsmManagerInternalErrorException("Error deleting softwarecomponent", "softwareBundleDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete softwareBundleDAO: " + ex);
            }
        }

    }

    /*
     * Assert that software component version is > minimum required version already present.
     */
    public static LocalizedValidationResult validateSoftwareComponentVersion(SoftwareComponentEntity component) {

        String componentId = component.getComponentId();
        String newVersion = component.getVendorVersion();
        String minVersion = null;

        FirmwareUtil firmwareUtil = new FirmwareUtil();
        FirmwareRepositoryEntity parentRepo = firmwareUtil.getEmbeddedRepo();

        List<SoftwareComponent> repoComponents =
                firmwareUtil.getSoftwareComponents(componentId,
                        null,
                        null,
                        null,
                        null,
                        parentRepo,
                        null,
                        null, 
                        null, 
                        false);

        /*
         */
        for (SoftwareComponent repoComponent : repoComponents) {
            minVersion = repoComponent.getVendorVersion();
            if (VersionUtils.compareVersions(newVersion, minVersion) >= 0) {
                continue;
            } else {
                return new LocalizedValidationResult(false,
                        AsmManagerMessages.invalidFirmwareBundleVersion(newVersion, minVersion));
            }
        }
        return new LocalizedValidationResult(true);
    }

}
