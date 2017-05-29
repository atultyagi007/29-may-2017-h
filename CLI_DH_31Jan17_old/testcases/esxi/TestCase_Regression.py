'''
Created on March 8, 2017

@author: raj.patel
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
sys.path.append(os.path.abspath('../../testcases/esxi'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/addOnModule'))
sys.path.append(os.path.abspath('../../testcases/chassisconfig'))

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
from testcases.esxi import TestCase_81191_old

import Testcase_AddOnModule
import TestCase_104145_Chassis
import TestCase_DeplList
import Testcase_DeleteByID_DiscReq
import TestCase_81191_ServiceTemplate




class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Regression test case of the P1 suites
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        

       
       
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    globalVars.testCaseFlowName ='exsi'
    try:
        test2 = InitialSetup.Testcase()
        test2.doInitialSetup("","")
    except:
        test2.log_data( 'Exception occurred while doing Intial Set up ')
    try:
        test3 = DefineNetwork.Testcase()
        test3.test_Network()
    except:
        test3.log_data('Exception occurred while defining network')    
    try:
        test4 = DiscoveryResources.Testcase()
        test4.test_doDiscovery()
    except:
        test4.log_data('Exception occurred while discovering resources') 
    try:
        test61 = Testcase_AddOnModule.Testcase()
        test61.runTestCase()
    except:
        test61.log_data('Exception occurred while adding add on module') 
    try:
        test62 = TestCase_104145_Chassis.Testcase()
        test62.test_Login()
        test62.preRunSetUp()
        test62.runTestCase()
        test62.postRunSetUp()
    except:
        test62.log_data('Exception occurred .....')
    try:
        test63 = TestCase_DeplList.Testcase()
        test63.runTestCase()
    except:
        test63.log_data('Exception occurred while getting list of deployments .....')
    try:
        test64 = Testcase_DeleteByID_DiscReq.Testcase()
        test64.test_Login()
        test64.runTestCase()
    except:
        test64.log_data('Exception occurred while deleting job from inventory')
    try:
        test65 = TestCase_81191_ServiceTemplate.Testcase()
        test65.preRunSetup()
        test65 = test.test_createTemplate()
    except:
        test65.log_data('Exception occurred while creating template..')
    os.chdir(current_dir)
    
