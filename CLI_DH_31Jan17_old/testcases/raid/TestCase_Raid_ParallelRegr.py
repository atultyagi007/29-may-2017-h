'''
Created on Aug 13, 2015

@author: waseem.irshad
'''
import os
import sys
from datetime import time
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/raid'))

from TemplateBaseClass import TemplateTestBase
import globalVars
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_112188
import TestCase_112189
import TestCase_112190
import TestCase_112191
import TestCase_112192
import TestCase_112193
import TestCase_112194
import TestCase_112195
import TestCase_112196
import TestCase_112197
import TestCase_112198
import TestCase_112199
import TestCase_112200
import TestCase_112201
import TestCase_112202
import TestCase_112203
import TestCase_112204
import TestCase_112205
import TestCase_112206
import TestCase_112207
import TestCase_112208
import TestCase_112209
import TestCase_112210
import TestCase_115719
import TestCase_115720
import TestCase_115721
import TestCase_102640
import FirmwareUpdate
import threading
import traceback

'''
    Script for Parallel execution of RAID Cases
'''
class Testcase_parallel(TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        self.authenticate(self.tc_Id)
        
    def run_Thread_1(self):
        print threading.currentThread().getName(), 'Starting.......'
        test1 = TestCase_112188.Testcase()
        test1.test_createTemplate()
        test2 = TestCase_112189.Testcase()
        test2.test_createTemplate()
        test3 = TestCase_112190.Testcase()
        test3.test_createTemplate()
        test4 = TestCase_112191.Testcase()
        test4.test_createTemplate()
        test5 = TestCase_112192.Testcase()
        test5.test_createTemplate()
        
    def run_Thread_2(self):
        print threading.currentThread().getName(), 'Starting.......'
        test6 = TestCase_112193.Testcase()
        test6.test_createTemplate()
        test7 = TestCase_112194.Testcase()
        test7.test_createTemplate()
        test8 = TestCase_112195.Testcase()
        test8.test_createTemplate()
        test9 = TestCase_112196.Testcase()
        test9.test_createTemplate()
        test10 = TestCase_112197.Testcase()
        test10.test_createTemplate()
        
    def run_Thread_3(self):
        print threading.currentThread().getName(), 'Starting..........'
        test11 = TestCase_112198.Testcase()
        test11.test_createTemplate()
        test12 = TestCase_112199.Testcase()
        test12.test_createTemplate()
        test13 = TestCase_112200.Testcase()
        test13.test_createTemplate()
        test14 = TestCase_112201.Testcase()
        test14.test_createTemplate()
        test15 = TestCase_112202.Testcase()
        test15.test_createTemplate()
        
    def run_Thread_4(self):
        print threading.currentThread().getName(), 'Starting.......'
        test16 = TestCase_112203.Testcase()
        test16.test_createTemplate()
        test17 = TestCase_112204.Testcase()
        test17.test_createTemplate()
        test18 = TestCase_112205.Testcase()
        test18.test_createTemplate()
        test19 = TestCase_112206.Testcase()
        test19.test_createTemplate()
        test20 = TestCase_112207.Testcase()
        test20.test_createTemplate()
        
    def run_Thread_5(self):
        print threading.currentThread().getName(), 'Starting.......'
        test27 = TestCase_112208.Testcase()
        test27.test_createTemplate()
        test28 = TestCase_112209.Testcase()
        test28.test_createTemplate()
        test29 = TestCase_112210.Testcase()
        test29.test_createTemplate()
        
    def run_Thread_6(self):
        print threading.currentThread().getName(), 'Starting.......'
        test30 = TestCase_115719.Testcase()
        test30.test_createTemplate()
        test31 = TestCase_115720.Testcase()
        test31.test_createTemplate()
        test32 = TestCase_115721.Testcase()
        test32.test_createTemplate()
        
        
        
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
        t1 = threading.Thread(name='Raid_Thread_1',target=test.run_Thread_1)
        t2 = threading.Thread(name='Raid_Thread_2',target=test.run_Thread_2)
        t3 = threading.Thread(name='Raid_Thread_3',target=test.run_Thread_3)
        t4 = threading.Thread(name='Raid_Thread_4',target=test.run_Thread_4)
        t5 = threading.Thread(name='Raid_Thread_5',target=test.run_Thread_5)
        t6 = threading.Thread(name='Raid_Thread_6',target=test.run_Thread_6)
        t1.start()
        t2.start()
        t3.start()
        t4.start()
        t5.start()
        t6.start()
    except Exception, err:
        print "Error: unable to start thread"
        print(traceback.format_exc())
    os.chdir(current_dir)
