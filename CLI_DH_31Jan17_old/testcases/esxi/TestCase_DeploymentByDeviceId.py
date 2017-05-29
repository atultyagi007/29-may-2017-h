'''
Created on April 10, 2017

@author: raj.patel
Prerequisites:One Service must be deployed.
/Deployment/device/{deviceId}    get
/Deployment/serverNetworking/{serviceId}/{serverId}    get
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
        resDevice,status=self.getResponse("GET", "Deploy")
        if status and resDevice > 0:
            for resInfo in resDevice:
                serviceId=resInfo['id']
                print serviceId
                deploymentDeviceList = resInfo['deploymentDevice']
                for deploymentDevice in deploymentDeviceList:
                    deviceId = deploymentDevice['refId']
                    
                    resDevice1,statusInfo=self.getResponse("GET", "DeploymentByDeviceid",refId=deviceId)
                    if statusInfo:
                        self.log_data("Payload:",resDevice1)
                        self.log_data("Successfully tested /Deployment/device/{deviceId} with GET action:")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Deployment/device/{deviceId} with GET action','Test case passed'])

                    else:
                        self.log_data("Test case Failed")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for Deployment/device/{deviceId} with GET action', 'Failed','Test case failed'])
                        return False
                    
                    deviceType = deploymentDevice['deviceType']
                    if "SERVER" in deviceType.upper():
                        serverRefId = serviceId+"/"+deviceId
                        resDevice2,statusInfo1=self.getResponse("GET", "NetworkListByServerid",refId=serverRefId)
                        if statusInfo1:
                            self.log_data("Payload:",resDevice2)
                            self.log_data("Successfully tested /Deployment/serverNetworking/{serviceId}/{serverId} with GET action:")
                            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Deployment/serverNetworking/{serviceId}/{serverId} with GET action','Test case passed'])
                        else:
                            self.log_data("Test case Failed")
                            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for /Deployment/serverNetworking/{serviceId}/{serverId}', 'Failed','Test case failed'])
                            return False

  
                    
            return True
                
        

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

