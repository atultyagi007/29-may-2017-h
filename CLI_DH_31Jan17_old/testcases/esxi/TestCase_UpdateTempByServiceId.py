'''
Created on April 14, 2017

@author: raj.patel
Description:Update Template by analysing related components
/ServiceTemplate/components/service/{serviceId}    put
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
# import xml.etree.cElementTree as ET
import xml.etree.ElementTree as ET
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
        resDevice,status=self.getResponse("GET", "Deploy")
        if status and resDevice > 0:
            for resInfo in resDevice:
                serviceId=resInfo['id']
                print serviceId
                temId=resInfo['serviceTemplate']['id']
                tempName=resInfo['serviceTemplate']['templateName']
                newTempName=tempName+datetime.datetime.now().strftime("%d%m%y")
                resPayloadXML,status1=self.templateComponent("GET", "Template",refId=temId)
                if status1:
                    
                    resPayloadXML=resPayloadXML.replace("<templateName>"+tempName+"</templateName>","<templateName>"+newTempName+"</templateName>")
                    print resPayloadXML
                    resPayloadXML2,status2=self.templateComponent("PUT", "TemplatePutByserviceId",payload=resPayloadXML,refId=serviceId)
                    if status2:
                        print resPayloadXML2
                        self.log_data("Successfully tested /ServiceTemplate/components/service/{serviceId} with PUT action:")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ServiceTemplate/components/service/{serviceId} with PUT action','Test case passed'])
                        return True
                    else:
                        self.log_data("Test case Failed")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for /ServiceTemplate/components/service/{serviceId} with PUT action', 'Failed','Test case failed'])
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

