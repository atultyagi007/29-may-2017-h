'''
Created on April 25 2017

@author: raj.patel
Description:Get All templates Standard/ReadOnly User, validate templates not assigned to Standard Users is not returned 
Prequisite:Standard user must be created username must be 'autostandard' and password is 'autopassword'

'''

import os
import sys
from datetime import date, datetime
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)

sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/negativeScenario'))

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


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Get All templates Standard/ReadOnly User, validate templates not assigned to Standard Users is not returned
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        #self.removelogfile()
        self.log_data("Running Test Case ::: ")
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_storageNoPermissionforStandardUser)
        
        self.getResources()
        
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        self.storageRes = self.getReqResource(limit=1, resourceType='STORAGE', deviceType=None)
        if len(self.storageRes) == 0:
            print "Required no. of Storage not available"
        else:
            storageId = self.storageRes[0]["refid"]
        self.vcenterRes = self.getReqResource(limit=1, resourceType='VCENTER', deviceType=None)
        if(len(self.vcenterRes)) == 0:
            print " Required no. of vcenter not found "
        else:
            vcenterId = self.vcenterRes[0]["refid"]
            
        globalVars.refIdVCenter = vcenterId
        
        postData = self.getTemplatePayloadEsxi(payload,vcenterId,storageId)
        print postData
        templateResponse= self.createTemplateEsxi(payload,vcenterId,storageId) 
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            self.log_data( 'Successfully created  and published Template ')
            self.log_TestData(["", "", "",str(self.tc_Id), "Successfully created  and published Template by admin without permission of any other users", 'Success','Template created  and published Successfully'])
            
            self.log_data("Login with standard user ::: ")
            response = self.authenticate(role="standard")
            logger = self.getLoggerInstance()
            logger.debug('Login Response for standard user')
            logger.info(response)
            
            resPayload,responseCode,status2=self.getResponseByWithoutAuthentication("GET", "Template",refId=templateIdValue)
            if responseCode==404 and status2:
                print resPayload
                self.log_data("Standard user does not have permission to get template.Test case passed")
                self.log_data(resPayload)
                self.log_TestData(["", "", "",str(self.tc_Id), 'Standard user does not have permission to get template','Success','Test case passed'])
                return True
            else:
                self.log_data("Test case Failed")
                self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                return False
          
            
        else:
            self.log_data( 'Failed to create/published template ')
            self.log_TestData(["", "", "",str(self.tc_Id), 'Template with storage','Failed','Failed to create/published template'])
            return False
        
if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()

    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)   

