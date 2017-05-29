/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;

public class DownloadFileUtilTest {
    
    public static class GetTypeTests {
        
        @Test
        public void testGoodCifsPaths() throws RepositoryException {           
            String[] goodCIFSPaths = {"\\\\172.23.4.5\\myRepodir\\$thispath1\\test.iso", "\\\\172.23.4.5\\myRepodir\\$thispath1\\test.ISO",
                    "\\\\dell-Asm\\myRepodir\\$thispath1\\test.ISO"};
        
            for(String goodPath: goodCIFSPaths) {
                assertEquals(DownloadFileUtil.getType(goodPath),"CIFS");
            }
        }
        
        @Test
        public void testBadCifsPaths() {
            String[] badCIFSPaths = {"\\\\dell-Asm\\myRepodir/$thispath1\\test.ISO","dell-Asm\\myRepodir\\$thispath1\\test.ISO"};
            for(String badPath: badCIFSPaths) {
                try {
                    DownloadFileUtil.getType(badPath);
                    fail("getType succeeded");
                } catch (RepositoryException e) {
                    assertNotNull(e);
                }                
            }         
        }
        
        @Test
        public void testGoodNfsPaths() throws RepositoryException {
            String[] goodNFSPaths = {"172.23.34.5:/myRepodir/$thispath1/test.iso","172.23.34.5:/myRepodir/$thispath1/test.ISO",
                    "dell-Asm:/myRepodir\\$thispath1/test.ISO", "dell-Asm:/myRepodir/$thispath1/test.ISO","ftp.dell.com:/path/to/foo.iso"};
            for(String goodPath: goodNFSPaths) {
                assertEquals(DownloadFileUtil.getType(goodPath),"NFS");
            }
        }
        
        @Test
        public void testBadNfsPaths() {
            String[] badNFSPaths = {"dell-Asm/myRepodir/$thispath1/test.ISO", ":/myRepodir/$thispath1/test.ISO"};
            for(String badPath: badNFSPaths) {
                try {
                    DownloadFileUtil.getType(badPath);
                    fail("getType succeeded");
                } catch (RepositoryException e) {
                    assertNotNull(e);
                }               
            }                     
        }
        
        @Test
        public void testGoodHttpPaths() throws RepositoryException {
            String[] goodHTTPPaths = {"http://172.23.4.5/myRepodir/$thispath1/test.iso", "http://172.23.4.5/myRepodir/$thispath1/test.ISO",
                    "http://dell-Asm/myRepodir/$thispath1/test.ISO"};     
            for(String goodPath: goodHTTPPaths) {
                assertEquals(DownloadFileUtil.getType(goodPath),"HTTP");
            }
        }
        
        @Test
        public void testBadHttpPaths() {
            String[] badHTTPPaths = {"http://dell-Asm/myRepodir\\$thispath1/test.ISO", "://dell-Asm/myRepodir/$thispath1/test.ISO", 
                    "http//dell-Asm/myRepodir/$thispath1/test.ISO"};    
            
            for(String badPath: badHTTPPaths) {
                try {
                    DownloadFileUtil.getType(badPath);
                    fail("getType succeeded");
                } catch (RepositoryException e) {
                    assertNotNull(e);
                }               
            }
        }
        
        @Test
        public void testGoodFtpPaths() throws RepositoryException {
            
            String[] goodFTPPaths = {"ftp://172.23.4.5/myRepodir/$thispath1/test.iso", "ftp://172.23.4.5/myRepodir/$thispath1/test.ISO",
                    "ftp://dell-Asm/myRepodir/$thispath1/test.ISO"};  
            for(String goodPath: goodFTPPaths) {
                assertEquals(DownloadFileUtil.getType(goodPath),"FTP");
            }

        }
        
        @Test
        public void testBadFtpPaths() {
            String[] badFTPPaths = {"ftp//dell-Asm/myRepodir/$thispath1/test.ISO", "://dell-Asm/myRepodir/$thispath1/test.ISO",
                    "ftp://dell-Asm/myRepodir\\$thispath1/test.ISO"};
            for(String badPath: badFTPPaths) {
                try {
                    DownloadFileUtil.getType(badPath);
                    fail("getType succeeded");
                } catch (RepositoryException e) {
                    assertNotNull(e);
                }               
            }
        }
        
        @Test
        public void testGoodFilePaths() throws RepositoryException {
            String[] goodFilePaths = {"file://thisdir/myRepodir/$thispath1/test.iso","file://thisdir/myRepodir/$thispath1/test.ISO",
                    "file://thisdir/myRepodir/$thispath1/test.ISO"};   
            for(String goodPath: goodFilePaths) {
                assertEquals(DownloadFileUtil.getType(goodPath),"FILE");
            }
        }
        
        @Test
        public void testBadFilePaths() {
            String[] badFilePaths = {"file//thisdir/myRepodir//$thispath1/test.ISO", "://thisdir/myRepodir//$thispath1/test.ISO"};  
            for(String badPath: badFilePaths) {
                try {
                    DownloadFileUtil.getType(badPath);
                    fail("getType succeeded");
                } catch (RepositoryException e) {
                    assertNotNull(e);
                }               
            } 
        }  
    }
}
