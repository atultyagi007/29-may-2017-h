/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.exception;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by Alan_Cooper on 10/9/13.
 */
public class AsmManagerRuntimeExceptionTest {
    private final String MESSAGE = "Exception message test";
    private final Throwable CAUSE = new Throwable(MESSAGE + MESSAGE);

    private final String NULL_MESSAGE = null;
    private final Throwable NULL_CAUSE = null;

    private final boolean ENABLE_SUPPRESSION = true;
    private final boolean WRITABLE_STACKTRACE = true;

    private void validate(AsmManagerRuntimeException AsmManagerRuntimeException)  {
        assertNotNull(AsmManagerRuntimeException);
        assertTrue((AsmManagerRuntimeException.getClass().isAssignableFrom(AsmManagerRuntimeException.class)));
    }

    @Test
    public void defaultConstructorTest() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException();
        validate(AsmManagerRuntimeException);
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest1() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(MESSAGE);
        validate(AsmManagerRuntimeException);
        assertEquals(MESSAGE, AsmManagerRuntimeException.getMessage());
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest2() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(MESSAGE, CAUSE);
        validate(AsmManagerRuntimeException);
        assertEquals(MESSAGE, AsmManagerRuntimeException.getMessage());
        assertNotNull(AsmManagerRuntimeException.getCause());
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest3() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(NULL_MESSAGE, NULL_CAUSE);
        validate(AsmManagerRuntimeException);
        assertEquals(NULL_MESSAGE, AsmManagerRuntimeException.getMessage());
        assertNull(AsmManagerRuntimeException.getCause());
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest4() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(NULL_CAUSE);
        validate(AsmManagerRuntimeException);
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest5() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(MESSAGE, CAUSE, ENABLE_SUPPRESSION, WRITABLE_STACKTRACE);
        validate(AsmManagerRuntimeException);
        assertNotNull(AsmManagerRuntimeException.getCause());
        assertEquals(CAUSE, AsmManagerRuntimeException.getCause());
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest6() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(NULL_MESSAGE, NULL_CAUSE, !ENABLE_SUPPRESSION, !WRITABLE_STACKTRACE);
        validate(AsmManagerRuntimeException);
        assertNull(AsmManagerRuntimeException.getCause());
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest7() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(NULL_MESSAGE, NULL_CAUSE, !ENABLE_SUPPRESSION, WRITABLE_STACKTRACE);
        validate(AsmManagerRuntimeException);
        assertNull(AsmManagerRuntimeException.getCause());
    }

    @Test
    public void asmManagerInternalErrorExceptionConstructorTest8() {
        AsmManagerRuntimeException AsmManagerRuntimeException = new AsmManagerRuntimeException(NULL_MESSAGE, NULL_CAUSE, ENABLE_SUPPRESSION, !WRITABLE_STACKTRACE);
        validate(AsmManagerRuntimeException);
        assertNull(AsmManagerRuntimeException.getCause());
    }
}
