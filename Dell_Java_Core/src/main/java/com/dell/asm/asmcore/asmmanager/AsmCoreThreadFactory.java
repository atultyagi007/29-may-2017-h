/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * @author Praharsh_Shah
 * 
 * ASM core ThreadFactory 
 * One can alter the thread's name, thread group, priority, daemon status, etc.
 */
public class AsmCoreThreadFactory implements ThreadFactory{

	 private final ThreadGroup   group;
	 private final String        threadNamePrefix;
	 private final AtomicInteger threadNameSuffix = new AtomicInteger(0);
	 
	 // Constructor.
	 public AsmCoreThreadFactory(String groupName, String threadName)
	 {
	   group = new ThreadGroup(groupName);
	   threadNamePrefix = threadName + "-";	   
	 }
	 	 	 
	 @Override
	 public Thread newThread(Runnable r)
	 {
	  // Create the simple thread.
	  String threadName = threadNamePrefix + threadNameSuffix.incrementAndGet();
	  Thread t = new Thread(group, r, threadName);
	  return t;
	 }
	 
	 public Thread newDaemonThread(Runnable r)
	 {
	  // Create the daemon thread.
	  String threadName = threadNamePrefix + threadNameSuffix.incrementAndGet();
	  Thread t = new Thread(group, r, threadName);
	  t.setDaemon(true);
	  return t;
	 }
	 
}
