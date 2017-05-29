'''
Created on April 11, 2017

@author: raj.patel
Description: Retrieves an individual Server with serviceTag
/Server/serviceTag/{serviceTag}  get
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

import json
import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping
import templateInputReqValueHyperV


class Testcase(DiscoverResourceTestBase):
    """
    Retrieves an individual Server with serviceTag
    """ 
    
    tc_Id=""
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
    def runTestCase(self):
        testCaseID=self.getTestCaseID(__file__)
        #self.removelogfile()
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        resDevice,status=self.getResponse("GET", "GetManagedDevice")
        if status and resDevice > 0:
            for resInfo in resDevice:
                
                    deviceType = resInfo['deviceType']
                    if "SERVER" in deviceType.upper():
                        serviceTag = resInfo['serviceTag']
                        resDevice2,statusInfo1=self.getResponse("GET", "ServerByServiceTag",refId=serviceTag)
                        if statusInfo1:
                            self.log_data("Payload:",resDevice2)
                            self.log_data("Successfully tested /Server/serviceTag/{serviceTag} with GET action:")
                            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Server/serviceTag/{serviceTag} with GET action','Test case passed'])
                            return True
                        else:
                            self.log_data("Test case Failed")
                            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for /Server/serviceTag/{serviceTag}', 'Failed','Test case failed'])
                            return False

  
                    
            return True
                
        

if __name__ == "__main__":
    test = Testcase()
#     test.getCSVHeader()
#     test.preRunSetup()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)   

