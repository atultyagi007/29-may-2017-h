'''
Created on April 3, 2017

@author: raj.patel
Prerequisites:One Service must be deployed.
/Deployment/network/{networkId}  get
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

from TemplateBaseClass import TemplateTestBase
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


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Retrieve all Deployments for network
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
    def runTestCase(self):
        testCaseID=self.getTestCaseID(__file__)
        #self.removelogfile()
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        pxeID=templateInputReqValueHyperV.pXEID
        self.log_data(" Network PXE id: %s"%(pxeID))
        resDevice,status=self.getResponse("GET", "DeployListByNWId",refId=pxeID)
        if status:
            self.log_data("Payload:",resDevice)
            self.log_data("Successfully tested /Deployment/network/{networkId} with GET action:" +pxeID)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Deployment/network/{networkId} with GET action for:' +pxeID,'Test case passed'])
            return True        
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        
        
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
#     test.preRunSetup()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)   

