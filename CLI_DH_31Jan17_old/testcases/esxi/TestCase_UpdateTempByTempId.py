'''
Created on April 14, 2017

@author: raj.patel
Description:Update Template by analysing related components
/ServiceTemplate/components/template/{templateId}    put
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
import datetime
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
    Retrieve Default Template with components refined for specified template ID 

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
        resDevice,status=self.getResponse("GET", "Template")
        if status and resDevice > 0:
            for resInfo in resDevice:
                tempId=resInfo['id']
                category=resInfo['category']
                tempName=resInfo['templateName']
                print tempName
                newTempName=tempName+datetime.datetime.now().strftime("%d%m%y")
                print newTempName
                if category == "Automation":
                    resPayloadXML,status1=self.templateComponent("GET", "Template",refId=tempId)
                    if status1:
                        print resPayloadXML
                        resPayloadXML=resPayloadXML.replace("<templateName>"+tempName+"</templateName>","<templateName>"+newTempName+"</templateName>")
                        print resPayloadXML
                        resPayloadXML2,status2=self.templateComponent("PUT", "TemplatePutByTemId",payload=resPayloadXML,refId=tempId)
                        if status2:
                            self.log_data("Successfully tested /ServiceTemplate/components/template/{templateId} with PUT action:")
                            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ServiceTemplate/components/template/{templateId} with PUT action','Test case passed'])
                            return True
                        else:
                            self.log_data("Test case Failed")
                            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for /ServiceTemplate/components/template/{templateId} with PUT action', 'Failed','Test case failed'])
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

