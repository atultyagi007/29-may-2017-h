/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

/*
 * @author Praharsh_Shah
 * 
 * ASM core ThreadFactory Test
 * One can alter the thread's name, thread group, priority, daemon status, etc.
 */

package com.dell.asm.asmcore.asmmanager;

import static org.junit.Assert.fail;

import org.junit.Test;

public class AsmCoreThreadFactoryTest {

	@Test
	public void testAsmThreadFactory(){
		
		final String thName = "UNIT";
		
		AsmCoreThreadFactory thFactory = new AsmCoreThreadFactory("TEST", thName);
		
		Thread t = thFactory.newThread(new Runnable(){

			@Override
			public void run() {
//				String Name = Thread.currentThread().getName();								
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}});
		
		
		t.start();
		if(!t.getName().contains(thName))fail();
		t.interrupt();		
	}
}
