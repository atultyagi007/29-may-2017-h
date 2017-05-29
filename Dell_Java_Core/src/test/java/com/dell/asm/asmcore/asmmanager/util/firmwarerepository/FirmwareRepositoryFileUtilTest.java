package com.dell.asm.asmcore.asmmanager.util.firmwarerepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.util.DownloadFileUtil;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.pg.orion.security.encryption.EncryptionDAO;

/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
public class FirmwareRepositoryFileUtilTest {

    private FirmwareRepositoryFileUtil util;

    @Before
    public void setUp() {
        util = new FirmwareRepositoryFileUtil(mock(GenericDAO.class),
                mock(EncryptionDAO.class), mock(LocalizableMessageService.class));
    }

    @Test
    public void testEncodeUnescapedUrl() throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        String[][] expectations = {
                { "http://foo.com/dir with spaces", "http://foo.com/dir%20with%20spaces" },
                { "ftp://www.dell.com/my/other/site.html", "ftp://www.dell.com/my/other/site.html" },
                { "not an url", "not an url" },
                { "\\\\127.0.0.1\\cifs\\should\\not\\be\\changed", "\\\\127.0.0.1\\cifs\\should\\not\\be\\changed" },
        };

        for (String[] expectation : expectations) {
            String input = expectation[0];
            String expected = expectation[1];

            String got = util.encodeUnescapedUrlIfPossible(input);
            assertEquals(expected, got);
        }
    }
}
