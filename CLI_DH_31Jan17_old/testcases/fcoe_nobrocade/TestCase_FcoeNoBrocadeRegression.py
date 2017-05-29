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
sys.path.append(os.path.abspath('../../testcases/fcoe_nobrocade'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from TemplateBaseClass import TemplateTestBase
import json
from util import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
from common import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_102142
import TestCase_102144
import TestCase_102145
import TestCase_102147
import TestCase_102154
import TestCase_102640
import FirmwareUpdate


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Regression test case of the testcase id 102142, 102144, 102145, 102147 and 102154
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
    test12.createOSRepositoryfromISOPath("windows2012", "win-2012r2",globalVars.osRepositoryPathNonR2)
    time.sleep(30)
    test12.createOSRepositoryfromISOPath("redhat", "redhat6",globalVars.osRepositoryPathRedhat)
    time.sleep(30)
    test12.createOSRepositoryfromISOPath("redhat7", "redhat7",globalVars.osRepositoryPath_Redhat7)
    time.sleep(30)
    test15 = TestCase_102640.Test_102640()
    test15.uploadNFSCatalog()
    time.sleep(30)
    test16 = FirmwareUpdate.FirmwareUpdate()
    test16.updateFirmwares()
    time.sleep(30)
    
    test6 = TestCase_102142.Testcase()
    test6.test_createTemplate()
    test7 = TestCase_102144.Testcase()
    test7.test_createTemplate()
    test8 = TestCase_102145.Testcase()
    test8.test_createTemplate()
    test9 = TestCase_102147.Testcase()
    test9.test_createTemplate()
    test10 = TestCase_102154.Testcase()
    test10.test_createTemplate()
    os.chdir(current_dir)
