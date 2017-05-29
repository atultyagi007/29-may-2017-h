/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.AsmCoreThreadFactory;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * @author Praharsh Shah
 * 
 * ASM core layer Threads pool for discovery of devices
 */
public class DiscoverySingleTonThreadPool {
	
	private static final String DISCOVERY_THREAD_GROUP = "DISCOVERYGRP";
	private static final String DISCOVERY_THREAD_PREFIX = "DISCOVERY";
	private static LinkedBlockingQueue<Runnable> thPoolqueue = new LinkedBlockingQueue<>();
	private static ThreadPoolExecutor AsmCoreDiscoveryThreadPool = initThreadPool();

	private DiscoverySingleTonThreadPool(){
		
	}
	
	private static ThreadPoolExecutor initThreadPool()
	{
	 final int corePoolSize = 100;
	 final int maximumPoolSize = 100;
	 final long keepAliveTime = 30L; // 30 seconds
	 
	 ThreadPoolExecutor myThreadPoolExecutor = new ThreadPoolExecutor(
	      corePoolSize, 
	      maximumPoolSize,
	      keepAliveTime, // excess threads will be terminated if they have been idle for more than the keepAliveTime
	      TimeUnit.SECONDS,
	      thPoolqueue, // new LinkedBlockingQueue<Runnable>(),
	      new AsmCoreThreadFactory(DISCOVERY_THREAD_GROUP, DISCOVERY_THREAD_PREFIX));
		
	 myThreadPoolExecutor.allowCoreThreadTimeOut(true);
	
	return myThreadPoolExecutor;
	}
	
	public static ThreadPoolExecutor getDiscoverySingleTonThreadPool(){
		return AsmCoreDiscoveryThreadPool;
	}
	
	public static boolean allActiveTasksDone() { 
		return (AsmCoreDiscoveryThreadPool.getActiveCount() == 0);
	}
		

	public static boolean allTasksDone() { 
		return (AsmCoreDiscoveryThreadPool.getActiveCount() == 0 && thPoolqueue.isEmpty());
	}
	
	/*
	 * Returns true if All tasks completed. Completion may be due to normal termination, 
	 * an exception, or cancellation -- in all of these cases, this method will return true.
	 */
	public static boolean tasksDone(List<Future<?>> list){
				
		if(list == null)
			return true;
		
		for (Future<?> future : list) {
			// if(!future.isCancelled())
			if(!future.isDone())
				return false;
		}
		
		return true;
	}
	
}
