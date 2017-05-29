package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.asmcore.asmmanager.tasks.DiscoverIpRangeForChassisJob;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryResult;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.discovery.IDiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;

import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.tasks.DiscoverIpRangeJob;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.model.Link;
import com.dell.asm.rest.common.model.Link.RelationType;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.RestUtil;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.jraf.client.jobmgr.JrafJobExecStatus;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.entity.JobExecutionHistory;

@Path("/DiscoveryRequest")
public class DiscoverIPRangeDevicesService implements IDiscoverIPRangeDevicesService {

    private static final Logger logger = Logger.getLogger(DiscoverIPRangeDevicesService.class);

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    private DiscoveryResultDAO discoveryResultDAO = DiscoveryResultDAO.getInstance();

    private DeviceDiscoverDAO discoverRequestDAO = DeviceDiscoverDAO.getInstance();
    
    private static final Set<String> validSortColumns = new HashSet<>();

    static {
        validSortColumns.add("id");
        validSortColumns.add("status");
        validSortColumns.add("statusMessage");
    }

    private static final Set<String> validFilterColumns = new HashSet<>();

    static {
        // do not remove parent job id from filter set
        validFilterColumns.add("id");
        validFilterColumns.add("status");
        validFilterColumns.add("statusMessage");
    }

    @Override
    public DiscoveryRequest deviceIPRangeDiscoveryRequest(DiscoveryRequest discoveryRequest) throws WebApplicationException {
        JobDetail job = null;
        String jobName = "";

        DiscoverIPRangeDeviceRequests discoveryRequestList = discoveryRequest.getDiscoveryRequestList();
        DiscoveryJobUtils.validateDiscoveryRequest(discoveryRequestList);

        IJobManager jm = JobManager.getInstance();
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

        job = jm.createNamedJob(DiscoverIpRangeJob.class);

        String xmlRequest = MarshalUtil.marshal(discoveryRequest);
        String xmlData = MarshalUtil.marshal(discoveryRequestList);
        job.getJobDataMap().put(DiscoverIpRangeJob.DISCOVERIPRANGE_SERVICE_KEY_DATA, xmlData);

        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

        // Schedule our job using our trigger.
        try {
            jm.scheduleJob(job, trigger);
            logger.info("checking and starting the scheduler");
            if (!jm.getScheduler().isStarted()) {
                jm.getScheduler().start();
                logger.info("scheduler started");
            }
            // Return the job name.
            jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
            discoveryRequest.setId(jobName);
            discoveryRequest.setStatus(DiscoveryStatus.INPROGRESS);

            try {
                logger.debug("create device discovery request entity.");
                DeviceDiscoverEntity deviceDiscoverEntity = new DeviceDiscoverEntity();
                deviceDiscoverEntity.setId(jobName);
                deviceDiscoverEntity.setStatus(DiscoveryStatus.INPROGRESS);
                deviceDiscoverEntity.setMarshalledDeviceDiscoverData(xmlRequest);

                discoverRequestDAO.createDeviceDiscover(deviceDiscoverEntity);
            } catch (AsmManagerCheckedException e) {
                // DB update failed...
                logger.error("Error in creating device discovery request in database", e);
            }

            if (servletResponse!=null) {
                servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
                Link jobStatusLink = ProxyUtil.buildJobLink("Discover IP range", jobName, servletRequest, uriInfo, httpHeaders);
                servletResponse.setHeader("Link", RestUtil.toLinkHeaderString(jobStatusLink));
            }

            return discoveryRequest;
        } catch (SchedulerException e) {
            logger.error("SchedulerException: device discovery job " + jobName + " failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.discoveryJobFailed(jobName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        } catch (Exception e) {
            logger.error("Exception: device discovery job " + jobName + " failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.discoveryJobFailed(jobName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        }
    }

    @Override
    public DiscoveryRequest deviceIPRangeDiscoveryRequestForChassis(DiscoveryRequest discoveryRequest) throws WebApplicationException {
        JobDetail job = null;
        String jobName = "";

        DiscoverIPRangeDeviceRequests discoveryRequestList = discoveryRequest.getDiscoveryRequestList();
        DiscoveryJobUtils.validateDiscoveryRequest(discoveryRequestList);

        IJobManager jm = JobManager.getInstance();
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();

        job = jm.createNamedJob(DiscoverIpRangeForChassisJob.class);

        String xmlRequest = MarshalUtil.marshal(discoveryRequest);
        String xmlData = MarshalUtil.marshal(discoveryRequestList);
        job.getJobDataMap().put(DiscoverIpRangeForChassisJob.DISCOVERIPRANGE_SERVICE_KEY_DATA, xmlData);

        // Create a trigger and associate it with the schedule, job,
        // and some arbitrary information. The boolean means "start now".
        Trigger trigger = jm.createNamedTrigger(schedBuilder, job, true);

        // Schedule our job using our trigger.
        try {
            jm.scheduleJob(job, trigger);
            logger.info("checking and starting the scheduler");
            if (!jm.getScheduler().isStarted()) {
                jm.getScheduler().start();
                logger.info("scheduler started");
            }
            // Return the job name.
            jobName = job.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
            discoveryRequest.setId(jobName);
            discoveryRequest.setStatus(DiscoveryStatus.INPROGRESS);

            try {
                logger.debug("create device discovery request entity.");
                DeviceDiscoverEntity deviceDiscoverEntity = new DeviceDiscoverEntity();
                deviceDiscoverEntity.setId(jobName);
                deviceDiscoverEntity.setStatus(DiscoveryStatus.INPROGRESS);
                deviceDiscoverEntity.setMarshalledDeviceDiscoverData(xmlRequest);

                discoverRequestDAO.createDeviceDiscover(deviceDiscoverEntity);
            } catch (AsmManagerCheckedException e) {
                // DB update failed...
                logger.error("Error in creating device discovery request in database", e);
            }

            if (servletResponse!=null) {
                servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
                Link jobStatusLink = ProxyUtil.buildJobLink("Discover IP range", jobName, servletRequest, uriInfo, httpHeaders);
                servletResponse.setHeader("Link", RestUtil.toLinkHeaderString(jobStatusLink));
            }

            return discoveryRequest;
        } catch (SchedulerException e) {
            logger.error("SchedulerException: device discovery job " + jobName + " failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.discoveryJobFailed(jobName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        } catch (Exception e) {
            logger.error("Exception: device discovery job " + jobName + " failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.discoveryJobFailed(jobName);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        }
    }

    @Override
    public Response deleteDiscoveryResult(String id) {
        if (id == null || id.trim().length() == 0) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.missingRequired("parentJobName"));
        }
        discoveryResultDAO.deleteDiscoveryResult(id);
        discoverRequestDAO.deleteDiscoveryResult(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public DiscoveryRequest[] getDiscoveryRequests(String sort, List<String> filter, Integer offset, Integer limit) {
        try {
            List<DiscoveryRequest> discoveryRequests = new ArrayList<DiscoveryRequest>();

            // Parse the sort parameter.
            // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
            SortParamParser sp = new SortParamParser(sort, validSortColumns);
            List<SortParamParser.SortInfo> sortInfos = sp.parse();

            // Build filter list from filter params ( comprehensive )
            FilterParamParser filterParser = new FilterParamParser(filter, validFilterColumns);
            List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

            Integer totalRecords = discoverRequestDAO.getTotalRecords(filterInfos);

            if (totalRecords > 0) { // get paginated results only if there are any records

                PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest, servletResponse, httpHeaders, uriInfo);

                int pageOffSet;
                if (offset == null) {
                    pageOffSet = 0;
                } else {
                    pageOffSet = offset;
                }

                int pageLimit;
                if (limit == null) {
                    pageLimit = paginationParamParser.getMaxLimit();
                } else {
                    pageLimit = limit;
                }

                PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, totalRecords);

                List<DeviceDiscoverEntity> deviceDiscoverEntities = discoverRequestDAO.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
                        paginationInfo);

                if (deviceDiscoverEntities != null) {
                    for (DeviceDiscoverEntity entity : deviceDiscoverEntities) {
                        DiscoveryRequest request = this.getDiscoveryRequest(entity.getId());
                        discoveryRequests.add(request);

                    }
                }

                if (httpHeaders!=null) {
                    UriBuilder linkBuilder = RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders);
                    linkBuilder.replaceQuery(null);
                    linkBuilder.path("DiscoveryRequest");
                    if (sort != null) {
                        linkBuilder.queryParam(AsmConstants.QUERY_PARAM_SORT, sort);
                    }

                    if (filterInfos != null) {
                        for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
                            if (filterInfo.isSimpleFilter()) {
                                // Simple filter take only one value.
                                linkBuilder.queryParam(filterInfo.getColumnName(), filterInfo.buildValueString());
                            } else {
                                linkBuilder.queryParam(AsmConstants.QUERY_PARAM_FILTER, filterInfo.buildValueString());
                            }
                        }
                    }
                    // Common library to add link headers in response headers
                    paginationParamParser.addLinkHeaders(paginationInfo, linkBuilder);
                }
            }
            if (servletResponse!=null) {
                servletResponse.setHeader(AsmConstants.DELL_TOTAL_COUNT_HEADER, totalRecords.toString());
            }

            DiscoveryRequest[] returnList = new DiscoveryRequest[discoveryRequests.size()];
            return discoveryRequests.toArray(returnList);
        }catch(LocalizedWebApplicationException le) {
            throw le;
        }catch(Exception e) {
            logger.error("Exception: getDiscoveryRequests failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.internalError();
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        }

    }

    @Override
    public DiscoveryRequest getDiscoveryRequest(String id) {

        try {
            updateDeviceDiscoveryStatus(id);
            DeviceDiscoverEntity deviceDiscoverEntity = discoverRequestDAO.getDeviceDiscoverEntityById(id);

            if (deviceDiscoverEntity == null)
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(id));

            DiscoveryRequest discoveryRequest = MarshalUtil.unmarshal(DiscoveryRequest.class, deviceDiscoverEntity.getMarshalledDeviceDiscoverData());

            discoveryRequest.setId(deviceDiscoverEntity.getId());
            discoveryRequest.setStatus(deviceDiscoverEntity.getStatus());
            discoveryRequest.setStatusMessage(deviceDiscoverEntity.getStatus().toString());

            List<DiscoveredDevices> devices = new ArrayList<DiscoveredDevices>();
            List<DiscoveryResultEntity> entities = discoveryResultDAO.getDiscoveryResult(id);


            for (DiscoveryResultEntity entity : entities) {
                if (entity != null) {

                    //logger.debug(" getDiscoveryResult found IP " + entity.getIpaddress());
                    DiscoveredDevices device = DiscoveryJobUtils.toModel(entity);
                    devices.add(device);
                }
            }

            discoveryRequest.setDevices(devices);
            discoveryRequest.setLink(buildURI(discoveryRequest));

            if (servletResponse!=null) {
                servletResponse.setHeader("Location", discoveryRequest.getLink().getHref());
            }
            return discoveryRequest;

        }catch(LocalizedWebApplicationException le) {
            throw le;
        }catch(Exception e) {
            logger.error("Exception: getDiscoveryRequest failed", e);
            EEMILocalizableMessage eemi = AsmManagerMessages.internalError();
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, eemi);
        }
    }

    private void updateDeviceDiscoveryStatus(String id) {

        DeviceDiscoverEntity deviceDiscoverEntity = discoverRequestDAO.getDeviceDiscoverEntityById(id);
        if( deviceDiscoverEntity == null )
        {
            logger.error( "Unable to get discovery request for ID: " + id);
            return;
        }

        try {
            // First check the JRAF parent job status
            JrafJobExecStatus status = ProxyUtil.getHistoryProxy().pollExecStatus(id);

            JobManager.getInstance().getJobHistoryManager().getExecHistoryStatus(id);

            IJobHistoryManager jobHistoryMgr = JobManager.getInstance().getJobHistoryManager();
            List<JobExecutionHistory> history;

            history = jobHistoryMgr.getExecHistoriesForJobName(id);
            if (history == null || history.size() == 0) {
                logger.warn("Job ID " + id + " does not have a history");
                return;
            }

            long execHistoryId = history.get(0).getJobExecutionHistoryId();

            Map<String, String> detailsMap = jobHistoryMgr.getExecDetailValues(execHistoryId);
            //logger.debug(" getExecDetailValues, received: " + detailsMap.size());

            deviceDiscoverEntity.setId(id);
            if (status == JrafJobExecStatus.SUCCESSFUL)
                deviceDiscoverEntity.setStatus(DiscoveryStatus.SUCCESS);
            else if (status == JrafJobExecStatus.FAILED || status == JrafJobExecStatus.ABNORMAL_TERMINATION)
                deviceDiscoverEntity.setStatus(DiscoveryStatus.FAILED);
            else
                deviceDiscoverEntity.setStatus(DiscoveryStatus.INPROGRESS);

            try {
                discoverRequestDAO.updateDeviceDiscoverEntity(deviceDiscoverEntity);
            } catch (AsmManagerCheckedException e) {
                logger.error("error updating the status for discover job entity");
            }

        } catch (JobManagerException e) {
            logger.error("Error Retrieving the Job status for " + id + ":" + e.getMessage());
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

    }

    private Link buildURI(DiscoveryRequest request) {
        Link retVal = new Link();
        if (uriInfo!=null) {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(IDiscoverIPRangeDevicesService.class)
                    .path(IDiscoverIPRangeDevicesService.class, "getDiscoveryResult");
            URI uri = uriBuilder.build(request.getId());

            retVal.setHref(uri.toString());
            retVal.setRel(RelationType.SELF.getName());
            retVal.setTitle("IP range Discovery Request");
            retVal.setType("IP range DiscoveryRequest");
        }
        return retVal;
    }
 
    /**
     * Gets the discovery result for the ref_id of the server/chassis/whatever.  This column is not actually the ref_id in the discovery result
     * instead it is the deviceref_id column.
     */
	@Override
	public DiscoveryResult getDiscoveryResult(String id) {
        return DiscoveryJobUtils.getDiscoveryResult(id);
	}

}
