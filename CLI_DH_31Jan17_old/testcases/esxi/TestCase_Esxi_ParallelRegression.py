'''
Created on Aug 13, 2015

@author: waseem.irshad
'''
from datetime import time

'''
    ESXI Regression Script for Parallel Execution of Test Cases
'''
from createdeploytemplate.TemplateBaseClass import TemplateTestBase
from discoverresources.DiscoverResourceBaseClass import DiscoverResourceTestBase
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/esxi'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from TemplateBaseClass import TemplateTestBase
import globalVars
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
from testcases.esxi import TestCase_81191_old
import TestCase_81192
import TestCase_81193
import TestCase_81194
import TestCase_81195
import TestCase_81196
import TestCase_81197
import TestCase_81198
import TestCase_81199
import TestCase_82125
import TestCase_82126
import TestCase_82127
import TestCase_82128
import TestCase_82129
import TestCase_82131
import TestCase_88167
import TestCase_88169
import TestCase_88170
import TestCase_102213
import TestCase_102214
import TestCase_102215
import TestCase_102216
import TestCase_102640
import FirmwareUpdate


#import thread
#from thread import start_new_thread
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
        test1 = TestCase_81192.Testcase()
        test1.test_createTemplate()
        test2 = TestCase_81193.Testcase()
        test2.test_createTemplate()
        test3 = TestCase_81194.Testcase()
        test3.test_createTemplate()
        test4 = TestCase_81195.Testcase()
        test4.test_createTemplate()
        test5 = TestCase_81196.Testcase()
        test5.test_createTemplate()
        
    def run_Thread_2(self):
        print threading.currentThread().getName(), 'Starting.......'
        test6 = TestCase_81197.Testcase()
        test6.test_createTemplate()
        test7 = TestCase_81198.Testcase()
        test7.test_createTemplate()
        test8 = TestCase_81199.Testcase()
        test8.test_createTemplate()
        test9 = TestCase_82125.Testcase()
        test9.test_createTemplate()
        test10 = TestCase_82126.Testcase()
        test10.test_createTemplate()
        
    def run_Thread_3(self):
        print threading.currentThread().getName(), 'Starting..........'
        test11 = TestCase_82127.Testcase()
        test11.test_createTemplate()
        test12 = TestCase_82128.Testcase()
        test12.test_createTemplate()
        test13 = TestCase_82129.Testcase()
        test13.test_createTemplate()
        test14 = TestCase_82131.Testcase()
        test14.test_createTemplate()
        test15 = TestCase_88167.Testcase()
        test15.test_createTemplate()
        
    def run_Thread_4(self):
        print threading.currentThread().getName(), 'Starting.......'
        test16 = TestCase_88169.Testcase()
        test16.test_createTemplate()
        test17 = TestCase_88170.TestCase()
        test17.test_createTemplate()
        test18 = TestCase_102213.Testcase()
        test18.test_createTemplate()
        test19 = TestCase_102214.Testcase()
        test19.test_createTemplate()
        test20 = TestCase_102215.Testcase()
        test20.test_createTemplate()
        test21 = TestCase_102216.Testcase()
        test21.test_createTemplate()
        
        
        
if __name__ == "__main__":
    try:
        test = Testcase_parallel()
        test.getCSVHeader()
        test22 = InitialSetup.Testcase()
        test22.doInitialSetup()
        test23 = DefineNetwork.Testcase()
        test23.test_Network()
        test24 = DiscoveryResources.Testcase()
        test24.test_doDiscovery()
        test25 = ConfigureResources.Testcase()
        test25.test_configureResourec()
        test26 = TestCase_102623OSRepo.Test_102623osRepo()
        test26.createOSRepositoryfromISOPath("redhat7", "red7",globalVars.osRepositoryPath_Redhat7)
        time.sleep(30)
        threading.local()
        t1 = threading.Thread(name='ESXI_Thread_1',target=test.run_Thread_1)
        t2 = threading.Thread(name='ESXI_Thread_2',target=test.run_Thread_2)
        t3 = threading.Thread(name='ESXI_Thread_3',target=test.run_Thread_3)
        t4 = threading.Thread(name='ESXI_Thread_4',target=test.run_Thread_4)
        t1.start()
        t2.start()
        t3.start()
        t4.start()
        os.chdir(current_dir)
    except Exception, err:
        print "Error: unable to start thread"
        print(traceback.format_exc())