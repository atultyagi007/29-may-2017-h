# -*- coding: utf-8 -*-
'''
Created on Dec 16, 2014

@author: dheeraj.singh
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/workflows'))

from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputForWorkFlowCases

import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo

import TestCase_102640
import FirmwareUpdate
import TestCase_WorkFlow1_Case1
import TestCase_WorkFlow1_Case2
import TestCase_WorkFlow2_Case1
import TestCase_WorkFlow2_Case2
import TestCase_WorkFlow3_Case1
import TestCase_WorkFlow4_Case1
import TestCase_WorkFlow4_Case2
import TestCase_WorkFlow5_Case1
import TestCase_WorkFlow5_Case2
import TestCase_WorkFlow6_Case1
import TestCase_WorkFlow6_Case2
import TestCase_WorkFlow7_Case1
import TestCase_WorkFlow8_Case1


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Regression test case  for all the Workflows
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        

       
       
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    test1 = tearDownTest.Testcase()
    test1.doTearDown()
    test2 = InitialSetup.Testcase()
    test2.doInitialSetup()
    test3 = DefineNetwork.Testcase()
    test3.test_Network()
    test4 = DiscoveryResources.Testcase()
    test4.test_doDiscovery()
    test5 = ConfigureResources.Testcase()
    test5.test_configureResourec()
    test12 = TestCase_102623OSRepo.Test_102623osRepo()
    test12.createOSRepositoryfromISOPath("windows2012", "win-2012",globalVars.osRepositoryPathR2)
    time.sleep(30)
    test12.createOSRepositoryfromISOPath("Centos 7", "Centos 7",globalVars.osRepositoryPath_Redhat7)
    time.sleep(30)
    test15 = TestCase_102640.Test_102640()
    response = test15.uploadNFSCatalog()
    time.sleep(30)
    if response:
        test16 = FirmwareUpdate.FirmwareUpdate()
        test16.updateFirmwares()
 
    test6 = TestCase_WorkFlow1_Case1.Testcase()
    test6.test_createTemplate()
    test7 = TestCase_WorkFlow1_Case2.Testcase()
    test7.test_createTemplate()
    test8 = TestCase_WorkFlow2_Case1.Testcase()
    test8.test_createTemplate()
    test9 = TestCase_WorkFlow2_Case2.Testcase()
    test9.test_createTemplate()
    test10 = TestCase_WorkFlow3_Case1.Testcase()
    test10.test_createTemplate()
    test11 = TestCase_WorkFlow4_Case1.Testcase()
    test11.test_createTemplate()
    test6 = TestCase_WorkFlow4_Case2.Testcase()
    test6.test_createTemplate()
    test6 = TestCase_WorkFlow5_Case1.Testcase()
    test6.test_createTemplate()
    test6 = TestCase_WorkFlow5_Case2.Testcase()
    test6.test_createTemplate()

