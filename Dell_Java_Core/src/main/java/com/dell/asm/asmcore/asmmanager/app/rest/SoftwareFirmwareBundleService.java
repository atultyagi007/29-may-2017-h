package com.dell.asm.asmcore.asmmanager.app.rest;

import static com.dell.asm.asmcore.asmmanager.AsmManagerMessages.deletedFirmwareRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.ISoftwareFirmwareBundleService;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareBundle;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryComplianceDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.SoftwareBundleDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.i18n2.exception.AsmRuntimeException;
import com.dell.asm.i18n2.exception.AsmValidationException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.localizablelogger.LogMessage.LogCategory;
import com.dell.asm.localizablelogger.LogMessage.LogSeverity;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;


@Path("/softwareBundleFirmware")
public class SoftwareFirmwareBundleService implements ISoftwareFirmwareBundleService {

    @Context
    private UriInfo uriInfo = null;

    @Context
    private HttpHeaders httpHeaders = null;

    @Context
    private HttpServletResponse servletResponse = null;

    @Context
    private HttpServletRequest servletRequest = null;
    
    private FirmwareRepositoryService fwReposervice = new FirmwareRepositoryService();
    
    private final FirmwareRepositoryDAO firmwareRepositoryDAO = FirmwareRepositoryDAO.getInstance();
    private DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();
    private final DeviceInventoryComplianceDAO deviceInventoryComplianceDAO = 
            DeviceInventoryComplianceDAO.getInstance();
    private FirmwareUtil firmwareUtil = new FirmwareUtil();
    
	  private static final Logger logger = Logger.getLogger(SoftwareFirmwareBundleService.class);
	   public static final Set<String> validSortColumns = new HashSet<>();
	    /** Enabling the UserFacing/Audit logs in the REST Service **/
	    private static boolean USEREVENTLOG_FOR_SERVICE = true;
	    private final LocalizableMessageService logService = LocalizableMessageService.getInstance();

	    /** HTTP Status for enabling the UserFacing/Audit logs in the REST Service methods **/
	    private static final Set<Status> USEREVENTLOG_FOR_HTTP_STATUS = new HashSet<>();

	    static {
	        USEREVENTLOG_FOR_HTTP_STATUS.add(Status.BAD_REQUEST);
	        USEREVENTLOG_FOR_HTTP_STATUS.add(Status.CONFLICT);
	        USEREVENTLOG_FOR_HTTP_STATUS.add(Status.NOT_FOUND);
	        USEREVENTLOG_FOR_HTTP_STATUS.add(Status.INTERNAL_SERVER_ERROR);
	    }

	  private SoftwareBundleDAO bundleDAO = SoftwareBundleDAO.getInstance();
	
	    static {
	        validSortColumns.add("name");
	        validSortColumns.add("version");
	        validSortColumns.add("description");
	        validSortColumns.add("bundleDate");
	
	    }

	    public static final Set<String> validFilterColumns = new HashSet<>();

	    static {
	        validFilterColumns.add("name");
	        validFilterColumns.add("bundleDate");
	      }


    @Override
    public SoftwareBundle addSoftwareBundle(SoftwareBundle softwareBundle) {
        logger.debug("Entering add software bundle : "
                + softwareBundle.getName());
        logger.debug("UserBundlePath set to  : "
                + softwareBundle.getUserBundlePath());

        try {
            String fwRepoId = softwareBundle.getFwRepositoryId();
            SoftwareBundleEntity checkDuplicateNameBundle =
                    bundleDAO.getBundleByName(softwareBundle.getName());
            if (checkDuplicateNameBundle != null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.CONFLICT,
                        AsmManagerMessages.duplicateBundleName(softwareBundle.getName()
                        ));
            }
            SoftwareBundleEntity softwareBundleEntity = bundleDAO.toBundleEntityObj(softwareBundle, false, fwRepoId);
            bundleDAO.createSoftwareBundle(softwareBundleEntity, fwRepoId);

            final FirmwareRepositoryEntity firmwareRepoEntity = softwareBundleEntity.getFirmwareRepositoryEntity();
            firmwareUtil.updateComplianceMapAndDeviceComplianceForRepo(firmwareRepoEntity);
            } catch (LocalizedWebApplicationException e) {
            logger.error(
                    "LocalizedWebApplicationException while creating software bundle "
                            + softwareBundle.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while creating software bundle "
                    + softwareBundle.getName(), e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        return softwareBundle;
    }


    @Override
    public Response updateSoftwareBundle(String id, SoftwareBundle softwareBundle) {
        String fwRepoId = softwareBundle.getFwRepositoryId();
        SoftwareBundleEntity bundleEntity = null;

        try {
            if (id == null) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND,
                        AsmManagerMessages.invalidSoftwareBundleId(id));
            }

            bundleEntity = bundleDAO.toBundleEntityObj(softwareBundle, true, fwRepoId);
            bundleDAO.updateSoftwareBundle(bundleEntity, fwRepoId);

            final FirmwareRepositoryEntity firmwareRepoEntity = bundleEntity.getFirmwareRepositoryEntity();
            firmwareUtil.updateComplianceMapAndDeviceComplianceForRepo(firmwareRepoEntity);


        } catch (AsmRuntimeException | AsmValidationException asme) {
            error(asme, "AsmException occured for UpdateSoftwareBundle REST request");
            throw asme;
        } catch (LocalizedWebApplicationException webae) {
            error(webae, "LocalizedWebApplicationException occured for UpdateSoftwareBundle REST request");
            throw webae;
        } catch (Exception e) {
            error(e, "Exception occured for UpdateSoftwareBundle REST request");

        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

	 protected void error(Throwable throwable, String message, Object... objects) {
	       logger.error(String.format(message, objects), throwable);
	    }

	@Override
	public Response deleteSoftwareBundle(String id) {
    	logger.info("delete software bundle  REST request");
            try {
                if (id == null) {
                    throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND,
                            AsmManagerMessages.invalidSoftwareBundleId(id));
                }
                SoftwareBundleEntity bundleEntity = bundleDAO.getSoftwareBundleById(id);
                if (bundleEntity != null) {
                    final FirmwareRepositoryEntity firmwareRepoEntity = bundleEntity.getFirmwareRepositoryEntity();
                    bundleDAO.deleteSoftwareBundle(bundleEntity);
    
                    firmwareUtil.updateComplianceMapAndDeviceComplianceForRepo(firmwareRepoEntity);
    
                    logService.logMsg(deletedFirmwareRepository(bundleEntity.getName()).getDisplayMessage(),
                            LogSeverity.INFO, LogCategory.INFRASTRUCTURE_OR_HARDWARE_CONFIGURATION);
                    logger.info("completed deleting the software bundle");
                }
                return Response.status(Response.Status.NO_CONTENT).build();
    
            } catch (AsmRuntimeException | AsmValidationException asme) {
                error(asme, "AsmException occured for Delete softwareBundle REST request");
                throw asme;
            } catch (LocalizedWebApplicationException webae) {
                error(webae, "LocalizedWebApplicationException occured for delete softwareBundle REST request");
                throw webae;
            } catch (Exception e) {
                error(e, "Exception occured for delete softwarebundle REST request");
    
            }
            return Response.status(Response.Status.NO_CONTENT).build();
	}

	@Override
	public SoftwareBundle getSoftwareBundle(String id) {
		logger.info("get software bundle  " + id);
		SoftwareBundle softwareBundle = null;
		try {
			SoftwareBundleEntity softwareBundleEntity = bundleDAO.getSoftwareBundleById(id);
			if (softwareBundleEntity != null) {
				softwareBundle = bundleDAO.toSoftwareBundleObj(softwareBundleEntity);
				
				
			}
		} catch (AsmRuntimeException | AsmValidationException asme) {
			error(asme, "AsmException occured for get software bundle REST request");
			throw asme;
		} catch (LocalizedWebApplicationException webae) {
			error(webae,
					"LocalizedWebApplicationException occured for getSoftwareBundle  REST request");
			throw webae;
		} catch (Exception e) {
			error(e, "Exception occured for get software bundle REST request");
			
		}
		return softwareBundle;
	}
	

	@Override
	public List<SoftwareBundle> getAllSoftwareBundles(String sort,
			List<String> filter, Integer offset, Integer limit, String fwRepoId) {
		if (fwRepoId == null)
		{
			throw new LocalizedWebApplicationException(
                    Response.Status.NOT_FOUND,
                    AsmManagerMessages.invalidSoftwareBundleId(fwRepoId));
		}
		List<SoftwareBundle> bundles = new ArrayList<SoftwareBundle>();
		FirmwareRepository fwRepo = fwReposervice.getFirmwareRepository(
				fwRepoId, false, true, true);
		Set<SoftwareBundle> bundlesSet = fwRepo.getSoftwareBundles();
		bundles.addAll(bundlesSet);
		return bundles;

	}



   
      
	
}
