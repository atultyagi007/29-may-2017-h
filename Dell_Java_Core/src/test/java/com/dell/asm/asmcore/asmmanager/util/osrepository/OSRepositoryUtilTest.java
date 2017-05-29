/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.osrepository;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;

/**
 * @author kdaniel
 *
 */
public class OSRepositoryUtilTest {
    private OSRepositoryUtil repoUtil;

    @Before
    public void setup() {
        repoUtil = mockOSRepositoryUtilTest();
    }

    @Test
    public void testGetRazorOSImagesAll() {
        List<RazorRepo> repos = repoUtil.getRazorOSImages(true);
        assertEquals(3, repos.size());
    }

    @Test
    public void testGetRazorOSImagesFiltered() {
        List<RazorRepo> repos = repoUtil.getRazorOSImages(false);
        assertEquals(2, repos.size());
    }

    public static OSRepositoryUtil mockOSRepositoryUtilTest() {
        OSRepositoryUtil rUtil = new OSRepositoryUtil() {
            public String getRazorOsImagesJsonData(String name){
                String rawData = "{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos\","
                        + "\"items\":[{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/repos/esxi-5.1\","
                        + "\"name\":\"esxi-5.1\"},"
                        + "{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/repos/SLES-11-SP4-DVD-x86_64-GM\","
                        + "\"name\":\"SLES-11-SP4-DVD-x86_64-GM\"},"
                        + "{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/repos/CentOS-7.0-x86_64\","
                        + "\"name\":\"CentOS-7.0-x86_64\"}]}";
                String rd0 = "{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/repos/esxi-5.1\","
                        + "\"name\":\"esxi-5.1\",\"iso_url\":\"file:///dev/null\",\"url\":null,"
                        + "\"task\":{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/tasks/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/tasks/vmware_esxi\",\"name\":\"vmware_esxi\"}}";
                String rd1 = "{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/repos/SLES-11-SP4-DVD-x86_64-GM\","
                        + "\"name\":\"SLES-11-SP4-DVD-x86_64-GM\",\"iso_url\":\"file:///dev/null\",\"url\":null,"
                        + "\"task\":{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/tasks/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/tasks/suse11\",\"name\":\"suse11\"}}";
                String rd2 = "{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/repos/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/repos/CentOS-7.0-x86_64\","
                        + "\"name\":\"CentOS-7.0-x86_64\",\"iso_url\":\"file:///dev/null\",\"url\":null,"
                        + "\"task\":{\"spec\":\"http://api.puppetlabs.com/razor/v1/collections/tasks/member\","
                        + "\"id\":\"http://localhost:8081/api/collections/tasks/redhat7\",\"name\":\"redhat7\"}}";
                if (name == null || name.equals("")) {
                    return rawData;
                } else if (name.equals("esxi-5.1")) {
                    return rd0;
                } else if (name.equals("SLES-11-SP4-DVD-x86_64-GM")) {
                    return rd1;
                } else if (name.equals("CentOS-7.0-x86_64")) {
                    return rd2;
                }
                return "";
            }

            public boolean isOSRepositoryValid(String name) {
                if (name.equals("esxi-5.1")) {
                    return true;
                } else if (name.equals("SLES-11-SP4-DVD-x86_64-GM")) {
                    return false;
                } else if (name.equals("CentOS-7.0-x86_64")) {
                    return true;
                }
                return true;
            }
        };
        return rUtil;
    }
}
