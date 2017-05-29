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
sys.path.insert(0,os.path.abspath('../../testcases/chassisconfig'))

from TemplateBaseClass import TemplateTestBase
import json
from util import globalVars
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
import TestCase_HyperV
import TestCase_VMware_Esxi



class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
       Looping for HyperV and Exsi Cases
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        

       
       
        

if __name__ == "__main__":
    test = Testcase()
#     test.getCSVHeader()
# #     test1 = tearDownTest.Testcase()
# #     test1.doTearDown()
#     test2 = InitialSetup.Testcase()
#     test2.doInitialSetup()
#     test3 = DefineNetwork.Testcase()
#     test3.test_Network()
#     test4 = DiscoveryResources.Testcase()
#     test4.test_doDiscovery()
#     test5 = ConfigureResources.Testcase()
#     test5.test_configureResourec()
#     test12 = TestCase_102623OSRepo.Test_102623osRepo()
#     test12.createOSRepositoryfromISOPath("windows2012", "win-2012",globalVars.osRepositoryPathR2)
#     time.sleep(30)
    loop = globalVars.noOfLoops
    while loop>0:
        test7 = TestCase_VMware_Esxi.Testcase()
        test7.test_createTemplate()
        test7.test_cleanePublishedTemplates()
        test8 = TestCase_HyperV.Testcase()
        test8.test_createTemplate()
        test8.test_cleanePublishedTemplates()
        loop -= 1
    os.chdir(current_dir)

    
    
