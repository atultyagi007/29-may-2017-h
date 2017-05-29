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
sys.path.append(os.path.abspath('../../testcases/hyperV'))

from TemplateBaseClass import TemplateTestBase
import globalVars
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_77211
import TestCase_77215
import TestCase_77216
import TestCase_77217
import TestCase_77218
import TestCase_77219
import TestCase_77452
import TestCase_78803
import TestCase_78804
import TestCase_78805
import TestCase_78806
import TestCase_78807
import TestCase_78808
import TestCase_78809
import TestCase_78810
import TestCase_78811
import TestCase_78913
import TestCase_78914
import TestCase_78915
import TestCase_78916
import TestCase_89862
import TestCase_89864
import TestCase_89917
import TestCase_102640
import FirmwareUpdate
import threading
import traceback

'''
    Regression script for parallel execution of HyperV Cases
'''

class Testcase_parallel(TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def run_Thread_1(self):
        print threading.currentThread().getName(), 'Starting.......'
        test1 = TestCase_77211.Testcase()
        test1.test_createTemplate()
        test2 = TestCase_77215.Testcase()
        test2.test_createTemplate()
        test3 = TestCase_77216.Testcase()
        test3.test_createTemplate()
        test4 = TestCase_77217.Testcase()
        test4.test_createTemplate()
        test5 = TestCase_77218.Testcase()
        test5.test_createTemplate()
        
    def run_Thread_2(self):
        print threading.currentThread().getName(), 'Starting.......'
        test6 = TestCase_77219.Testcase()
        test6.test_createTemplate()
        test7 = TestCase_77452.Testcase()
        test7.test_createTemplate()
        test8 = TestCase_78803.Testcase()
        test8.test_createTemplate()
        test9 = TestCase_78804.Testcase()
        test9.test_createTemplate()
        test10 = TestCase_78805.Testcase()
        test10.test_createTemplate()
        
    def run_Thread_3(self):
        print threading.currentThread().getName(), 'Starting..........'
        test11 = TestCase_78806.Testcase()
        test11.test_createTemplate()
        test12 = TestCase_78807.Testcase()
        test12.test_createTemplate()
        test13 = TestCase_78808.Testcase()
        test13.test_createTemplate()
        test14 = TestCase_78809.Testcase()
        test14.test_createTemplate()
        test15 = TestCase_78810.Testcase()
        test15.test_createTemplate()
        
    def run_Thread_4(self):
        print threading.currentThread().getName(), 'Starting.......'
        test16 = TestCase_78811.Testcase()
        test16.test_createTemplate()
        test17 = TestCase_78913.Testcase()
        test17.test_createTemplate()
        test18 = TestCase_78914.Testcase()
        test18.test_createTemplate()
        test19 = TestCase_78915.Testcase()
        test19.test_createTemplate()
        test20 = TestCase_78916.Testcase()
        test20.test_createTemplate()
        
    def run_Thread_5(self):
        print threading.currentThread().getName(), 'Starting.......'
        test27 = TestCase_89862.Testcase()
        test27.test_createTemplate()
        test28 = TestCase_89864.Testcase()
        test28.test_createTemplate()
        test29 = TestCase_89917.Testcase()
        test29.test_createTemplate()
        
        
        
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
        t1 = threading.Thread(name='HyperV_Thread_1',target=test.run_Thread_1)
        t2 = threading.Thread(name='HyperV_Thread_2',target=test.run_Thread_2)
        t3 = threading.Thread(name='HyperV_Thread_3',target=test.run_Thread_3)
        t4 = threading.Thread(name='HyperV_Thread_4',target=test.run_Thread_4)
        t5 = threading.Thread(name='HyperV_Thread_5',target=test.run_Thread_5)
        t1.start()
        t2.start()
        t3.start()
        t4.start()
        t5.start()
        
    except Exception, err:
        print "Error: unable to start thread"
        print(traceback.format_exc())
    os.chdir(current_dir)
