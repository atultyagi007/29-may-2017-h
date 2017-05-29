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
sys.path.append(os.path.abspath('../../testcases/fcoe_s500withoutbrocade'))

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
import TestCase_102192



class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    SanaityTestCase for FCoE_s5000 without Brocade
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
    time.sleep(300)
    test5 = ConfigureResources.Testcase()
    test5.test_configureResourec()
    test6 = TestCase_102192.Testcase()
    test6.test_sanity()

