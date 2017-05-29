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

'''
    script for parallel execution of FCOE_Flex_IOA cases
'''

from TemplateBaseClass import TemplateTestBase
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_104061
import TestCase_104062
import TestCase_104064
import TestCase_104068
import TestCase_104069
import TestCase_104070
import TestCase_104073
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
        test1 = TestCase_104061.Testcase()
        test1.test_createTemplate()
        test2 = TestCase_104062.Testcase()
        test2.test_createTemplate()
        test3 = TestCase_104064.Testcase()
        test3.test_createTemplate()
        test4 = TestCase_104068.Testcase()
        test4.test_createTemplate()
        
    def run_Thread_2(self):
        print threading.currentThread().getName(), 'Starting.......'
        test6 = TestCase_104069.Testcase()
        test6.test_createTemplate()
        test7 = TestCase_104070.Testcase()
        test7.test_createTemplate()
        test8 = TestCase_104073.Testcase()
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
        t1 = threading.Thread(name='Flex_IAO_Thread_1',target=test.run_Thread_1)
        t2 = threading.Thread(name='Flex_IAO_Thread_2',target=test.run_Thread_2)
        t1.start()
        t2.start()
        os.chdir(current_dir)
    except Exception, err:
        print "Error: unable to start thread"
        print(traceback.format_exc())