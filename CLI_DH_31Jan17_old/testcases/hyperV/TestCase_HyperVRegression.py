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
sys.path.append(os.path.abspath('../../testcases/hyperV'))

from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_77215
import TestCase_78808
import TestCase_77216
import TestCase_78809
import TestCase_77217
import TestCase_78811
import TestCase_102640
import FirmwareUpdate


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Regression test case of the testcase id 77215, 78808, 77216, 78809, 77217, and 78811
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
    test12.createOSRepositoryfromISOPath("hyperv", "win2012",globalVars.osRepositoryPathR2)
    time.sleep(30)
    test12.createOSRepositoryfromISOPath("hyperv", "win-2012r2",globalVars.osRepositoryPathNonR2)
    time.sleep(30)
    test15 = TestCase_102640.Test_102640()
    test15.uploadNFSCatalog()
    time.sleep(30)
    test16 = FirmwareUpdate.FirmwareUpdate()
    test16.updateFirmwares()
    time.sleep(30)
    
    test6 = TestCase_77215.Testcase()
    test6.test_createTemplate()
    test7 = TestCase_78808.Testcase()
    test7.test_createTemplate()
    test8 = TestCase_77216.Testcase()
    test8.test_createTemplate()
    test9 = TestCase_78809.Testcase()
    test9.test_createTemplate()
    test10 = TestCase_77217.Testcase()
    test10.test_createTemplate()
    test11 = TestCase_78811.Testcase()
    test11.test_createTemplate()
    os.chdir(current_dir)
