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
 * ASM core DiscoverySingleTonThreadPool Test 
 * 
 */

package com.dell.asm.asmcore.asmmanager.tasks;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;


public class DiscoverySingleTonThreadPoolTest {

    @Test
    public void testDiscoverySTThreadPool() {

        ThreadPoolExecutor AsmCoreDiscoveryThreadPoolInstOne = DiscoverySingleTonThreadPool.getDiscoverySingleTonThreadPool();
        ThreadPoolExecutor AsmCoreDiscoveryThreadPoolInstTwo = DiscoverySingleTonThreadPool.getDiscoverySingleTonThreadPool();

        if (!AsmCoreDiscoveryThreadPoolInstOne.equals(AsmCoreDiscoveryThreadPoolInstTwo))
            fail();

        ThreadPoolExecutor AsmCoreDiscoveryThreadPoolInstThree = DiscoverySingleTonThreadPool.getDiscoverySingleTonThreadPool();


        if (!AsmCoreDiscoveryThreadPoolInstThree.equals(AsmCoreDiscoveryThreadPoolInstTwo))
            fail();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDiscoverySTThreadPoolWithTasks() {

        ThreadPoolExecutor AsmCoreDiscoveryThreadPoolInst = DiscoverySingleTonThreadPool.getDiscoverySingleTonThreadPool();

        @SuppressWarnings("rawtypes")
        final
        Callable worker = new Callable<Object>() {

            @Override
            public Object call() {
                try {
                    myLockMethod();
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                return null;
            }
        };

        Future<?> submit = AsmCoreDiscoveryThreadPoolInst.submit(worker);

        List<Future<?>> submitList = new ArrayList<Future<?>>();
        submitList.add((Future<Object>) submit);


        int i = 0;
        while (!DiscoverySingleTonThreadPool.tasksDone(submitList) && i < 5) {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
            i++;
            if (i == 2) {
//				   if(DiscoverySingleTonThreadPool.AllActiveTasksDone())
//					   fail();
                if (DiscoverySingleTonThreadPool.allTasksDone())
                    fail();
                myNotifyMethod();
            }

        }
    }

    public synchronized void myLockMethod() {
        try {
            wait();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void myNotifyMethod() {
        notify();
    }

}

