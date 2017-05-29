/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.ChassisIdentity;
import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.DeviceConfigureRequest;
import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.DeviceIdentity;
import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.IomIdentity;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;
import com.dell.pg.orion.jobmgr.entity.JobExecutionHistory;
import com.dell.pg.orion.jobmgr.entity.JobExecutionHistoryDetail;

class TestJobUtils
{
	
	public static final String DISCOVERIPRANGE_SERVICE_KEY_DATA = "DiscoverIpRange";
	public static final String APPLY_TEMPLATE_SERVICE_KEY_DATA = "ApplyTemplate";
	
 /* ------------------------------------------------------------------------- */
 /* runJob:                                                                   */
 /* ------------------------------------------------------------------------- */
 static String runJob(Class<? extends Job> jobClass, String startIp, String endIp) 
  throws SchedulerException
 {
  // Create the jm instance.
  IJobManager jm = JobManager.getInstance();
  
  //
  jm.getScheduler().start(); 
  
  // Create a simple schedule that trigger every 4 seconds indefinitely.
  SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule();
  
  
	DiscoverIPRangeDeviceRequests deviceRequests = new DiscoverIPRangeDeviceRequests();
	DiscoverIPRangeDeviceRequest  deviceRequest = new DiscoverIPRangeDeviceRequest();
	//"192.168.113.20", "root", "calvin"
	deviceRequest.setDeviceStartIp(startIp);
	deviceRequest.setDeviceEndIp(endIp);
	deviceRequests.getDiscoverIpRangeDeviceRequests().add(deviceRequest);
	
	JobDetail jobDetail = jm.createNamedJob(DiscoverIpRangeJob.class);
	// serialize and store as a string
	String xmlData = MarshalUtil.marshal(deviceRequests);
	jobDetail.getJobDataMap().put(DISCOVERIPRANGE_SERVICE_KEY_DATA, xmlData);

	String mapData = (String) jobDetail.getJobDataMap().getString(DISCOVERIPRANGE_SERVICE_KEY_DATA);
	System.out.println("Discovery data:" + mapData);
	
  // Create a trigger and associate it with the schedule, job, 
  // and some arbitrary information.  The boolean means "start now".
  Trigger trigger = jm.createNamedTrigger(schedBuilder, jobDetail, true);
    
  // Schedule our job using our trigger.
  jm.scheduleJob(jobDetail, trigger);
  
  // Return the job name.
  String jobName = jobDetail.getJobDataMap().getString(JobManager.JM_JOB_HISTORY_JOBNAME);
  return jobName;
 }
 

 /* ------------------------------------------------------------------------- */
 /* completeJob:                                                              */
 /* ------------------------------------------------------------------------- */
 static void completeJob(Long execHistoryId) 
  throws JobManagerException
 {
  JobManager.getInstance().getJobHistoryManager().setExecHistoryStatus(execHistoryId, JobStatus.SUCCESSFUL);
 }
 
	/**
	 * Poll for Job completion
	 * 
	 * @param jobName
	 *            name of Job
	 * @return Job completion status
	 */
	public static String pollForCompletion(String jobName) {
		String status = null;
		while (true) {
			try {
				status = JobManager.getInstance().getJobHistoryManager()
						.getExecHistoryStatus(jobName);
			} catch (JobManagerException e) {
				System.out.println(e.getMessage());
				return "FAILED";
			}
			if (JobStatus.isTerminal(status))
				return status;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				return "FAILED";
			}
		}
	}
	
	
	public static void exerciseDetails(String jobName) throws JobManagerException
	 {
	    IJobHistoryManager jobHistoryMgr = JobManager.getInstance().getJobHistoryManager();
	  
		List<JobExecutionHistory> history = jobHistoryMgr.getExecHistoriesForJobName(jobName);
		long execHistoryId = history.get(0).getJobExecutionHistoryId();
		
	  // Get the empty set of details.
	  List<JobExecutionHistoryDetail> details = jobHistoryMgr.getExecDetails(execHistoryId);
	  System.out.println("==== getExecDetails, received: " + details.size());
	  for (JobExecutionHistoryDetail d : details)
	     System.out.println("====    detail: " + d);
	  
	  // Get the empty map of key/value pairs.
	  Map<String,String> detailsMap = jobHistoryMgr.getExecDetailValues(execHistoryId);
	  System.out.println("==== getExecDetailValues, received: " + detailsMap.size());
	  Iterator it = detailsMap.entrySet().iterator();
	  while (it.hasNext()) {
	      Map.Entry pairs = (Map.Entry)it.next();
	      System.out.println(pairs.getKey() + " = " + pairs.getValue());
	  }
	    
	  // Get the job instance's status.
	  String  status = jobHistoryMgr.getExecHistoryStatus(execHistoryId);
	  System.out.println("==== Expected status SUCCESSFUL, received: " + status);


	 }
	 
}
