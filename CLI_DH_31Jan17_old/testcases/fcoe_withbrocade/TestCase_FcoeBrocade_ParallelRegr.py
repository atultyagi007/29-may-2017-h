'''
Created on Aug 13, 2015

@author: waseem.irshad
'''

import os
import sys
from datetime import time
from util import globalVars
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/fcoe_flex_fc_mxl'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/fcoe_withbrocade'))

'''
    Regression script for parallel execution of FCOE WithBrocade cases
'''

from TemplateBaseClass import TemplateTestBase
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_102118
import TestCase_102120
import TestCase_102121
import TestCase_102123
import TestCase_102130
import TestCase_102132
import TestCase_102133
import TestCase_102135
import TestCase_102640
import FirmwareUpdate
import threading
import traceback

class Testcase_parallel(TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        self.authenticate(self.tc_Id)
        
    def run_Thread_1(self):
        print threading.currentThread().getName(), 'Starting.......'
        test1 = TestCase_102118.Testcase()
        test1.test_createTemplate()
        test2 = TestCase_102120.Testcase()
        test2.test_createTemplate()
        test3 = TestCase_102121.Testcase()
        test3.test_createTemplate()
        
        
    def run_Thread_2(self):
        print threading.currentThread().getName(), 'Starting.......'
        test4 = TestCase_102123.Testcase()
        test4.test_createTemplate()
        test5 = TestCase_102130.Testcase()
        test5.test_createTemplate()
        test6 = TestCase_102132.Testcase()
        test6.test_createTemplate()
                
    def run_Thread_3(self):
        test7 = TestCase_102133.Testcase()
        test7.test_createTemplate()
        test8 = TestCase_102135.Testcase()
        test8.test_createTemplate()

        

if __name__ == "__main__":
    try:
        test = Testcase_parallel()
        test.getCSVHeader()
        test9 = tearDownTest.Testcase()
        test9.doTearDown()
        test10 = InitialSetup.Testcase()
        test10.doInitialSetup()
        test11 = DefineNetwork.Testcase()
        test11.test_Network()
        test12= DiscoveryResources.Testcase()
        test12.test_doDiscovery()
        test13= ConfigureResources.Testcase()
        test13.test_configureResourec()
        test14 = TestCase_102623OSRepo.Test_102623osRepo()
        test14.createOSRepositoryfromISOPath("windows2012", "win-2012",globalVars.osRepositoryPathR2)
        time.sleep(30)
        test14.createOSRepositoryfromISOPath("windows2012", "win-2012r2",globalVars.osRepositoryPathNonR2)
        time.sleep(30)
        test14.createOSRepositoryfromISOPath("redhat", "redhat6",globalVars.osRepositoryPathRedhat)
        time.sleep(30)
        test14.createOSRepositoryfromISOPath("redhat7", "redhat7",globalVars.osRepositoryPath_Redhat7)
        time.sleep(30)
        test15 = TestCase_102640.Test_102640()
        test15.uploadNFSCatalog()
        time.sleep(30)
        test16 = FirmwareUpdate.FirmwareUpdate()
        test16.updateFirmwares()
        time.sleep(30)

        threading.local()
        t1 = threading.Thread(name='FcoeWithBrocade_Thread_1',target=test.run_Thread_1)
        t2 = threading.Thread(name='FcoeWithBrocade_Thread_2',target=test.run_Thread_2)
        t3 = threading.Thread(name='FcoeWithBrocade_Thread_3',target=test.run_Thread_3)
        t1.start()
        t2.start()
        t3.start()
        
    except Exception, err:
        print "Error: unable to start thread"
        print(traceback.format_exc())
        
    os.chdir(current_dir)