/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.IJobManager;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.entity.JobExecutionHistory;
import com.dell.pg.orion.jobmgr.entity.JobExecutionHistoryDetail;
import com.dell.pg.orion.jobmgr.entity.JobExecutionTreeElement;

public class DiscoverIpRangeJobIT
{
 /* ************************************************************************* */
 /*                               Constants                                   */
 /* ************************************************************************* */
 // Time to wait for all jobs to run.
 private static final int SLEEP_SECONDS = 30;
 
 /* ************************************************************************* */
 /*                             Constructor                                   */
 /* ************************************************************************* */
 public DiscoverIpRangeJobIT(){}
 
 /* ************************************************************************* */
 /*                             Public Methods                                */
 /* ************************************************************************* */
 /* ------------------------------------------------------------------------- */
 /* main:                                                                     */
 /* ------------------------------------------------------------------------- */
 public static void main(String[] args) throws Exception
 {
  DiscoverIpRangeJobIT test = new DiscoverIpRangeJobIT();
  test.testProcess();
 }
 
 
	
 /* ------------------------------------------------------------------------- */
 /* testProcess:                                                              */
 /* ------------------------------------------------------------------------- */
 @Test
 public void testProcess() 
  throws Exception
 {
  say("Starting DiscoverIpRangeJobIT...");
  
  // Start parent job.
  String jobName = TestJobUtils.runJob(DiscoverIpRangeJob.class, "192.168.113.1", "192.168.113.20");
  
  // Sleep for enough time for all descendant jobs to run.
  for (int i = 0; i < SLEEP_SECONDS; i++)
    {
     try {Thread.sleep(1000);}
      catch (InterruptedException e){}
     System.out.print(".");
    }
  
  // Check results.
  //checkDBResults(jobName);
  
	String status = TestJobUtils.pollForCompletion(jobName);
	if ("SUCCESSFUL".equalsIgnoreCase(status)) {
		System.out.print("Job Success");
	} else {
		System.out.print("Job Failed");
	}  
	
	TestJobUtils.exerciseDetails(jobName);
	say(" Stopping DiscoverIpRangeJobIT");
 }
 
 

 /* ------------------------------------------------------------------------- */
 /* testProcess:                                                              */
 /* ------------------------------------------------------------------------- */
 @Test
 public void testProcessInvalidIp()  throws Exception
 {
  say("Starting DiscoverIpRangeJobIT...");
  
  // Start parent job.
  String jobName = TestJobUtils.runJob(DiscoverIpRangeJob.class, "192.168.113.254", "192.168.114.257");
  
  // Sleep for enough time for all descendant jobs to run.
  for (int i = 0; i < SLEEP_SECONDS; i++)
    {
     try {Thread.sleep(1000);}
      catch (InterruptedException e){}
     System.out.print(".");
    }
  
  // Check results.
  //checkDBResults(jobName);
  
	String status = TestJobUtils.pollForCompletion(jobName);
	if ("SUCCESSFUL".equalsIgnoreCase(status)) {
		System.out.print("Job Success");
	} else {
		System.out.print("Job Failed");
	}  
	
	TestJobUtils.exerciseDetails(jobName);
	say(" Stopping DiscoverIpRangeJobIT");
 }
 
 /* ************************************************************************* */
 /*                             Private Methods                               */
 /* ************************************************************************* */
 /* ------------------------------------------------------------------------- */
 /* checkDBResults:                                                           */
 /* ------------------------------------------------------------------------- */
 private void checkDBResults(String jobName) 
  throws JobManagerException
 {
  // Create the jm instance.
  IJobManager jm = JobManager.getInstance();
  IJobHistoryManager jhm = jm.getJobHistoryManager();
  
  // Get the jobExecId for the parent job.
  List<JobExecutionHistory> jobExecList = jhm.getExecHistoriesForJobName(jobName);
  Assert.assertEquals(1, jobExecList.size());
  JobExecutionHistory parentExecHistory = jobExecList.get(0);
  
  // Get the parent's tree record.
  Long parentExecId = parentExecHistory.getJobExecutionHistoryId();
  List<JobExecutionTreeElement> childList = jhm.getExecTreeChildren(parentExecId);
  Assert.assertEquals(1, childList.size());
  JobExecutionTreeElement childElem = childList.get(0);
  
  // Check child record.
  Assert.assertEquals(parentExecId, childElem.getParentJobExecutionHistoryId());
  
  // Use the child to retrieve the parent tree element which should not
  // exist because its the top of the tree.
  Long parentExecId2 = childElem.getParentJobExecutionHistoryId();
  JobExecutionTreeElement parentElem = jhm.getExecTreeParent(parentExecId2);
  Assert.assertNotNull(parentElem);
  Assert.assertNull(parentElem.getParentJobExecutionHistoryId());
  
  // Get the grandchildren.
  long childExecId = childElem.getChildJobExecutionHistoryId();
  List<JobExecutionTreeElement> grandChildList = jhm.getExecTreeChildren(childExecId);
  Assert.assertEquals(2, grandChildList.size());
  JobExecutionTreeElement grandChildElem0 = grandChildList.get(0);
  JobExecutionTreeElement grandChildElem1 = grandChildList.get(1);
  
  // Check grandchild 0 record.
  Assert.assertEquals(childExecId, grandChildElem0.getParentJobExecutionHistoryId().longValue());
  
  // Check grandchild 0 record.
  Assert.assertEquals(childExecId, grandChildElem1.getParentJobExecutionHistoryId().longValue());
    
  // ----- Testing History APIs
  // The parent job has no parent in the tree table, so we expect nothing to be returned.
  JobExecutionHistory parentOfParentExecHistory = jhm.getExecTreeParentHistory(parentExecId);
  Assert.assertNull(parentOfParentExecHistory);
  
  // Get the parent history through a child tree lookup.
  JobExecutionHistory parentExecHistory2 = jhm.getExecTreeParentHistory(childExecId);
  Assert.assertEquals(parentExecHistory.getJobExecutionHistoryId(),
                      parentExecHistory2.getJobExecutionHistoryId());
  
  // Get the grandchildren through a child tree lookup.
  List<JobExecutionHistory> grandChildHistoryList = jhm.getExecTreeChildHistories(childExecId);
  Assert.assertEquals(2, grandChildHistoryList.size());
  Assert.assertEquals(grandChildElem0.getChildJobExecutionHistoryId(), 
                      grandChildHistoryList.get(0).getJobExecutionHistoryId());
  Assert.assertEquals(grandChildElem1.getChildJobExecutionHistoryId(), 
                      grandChildHistoryList.get(1).getJobExecutionHistoryId());
 }
 
 
 /* ------------------------------------------------------------------------- */
 /* say:                                                                      */
 /* ------------------------------------------------------------------------- */
 private void say(String s){System.out.println(s);}
}
