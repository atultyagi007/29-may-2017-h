'''
Created on Nov 3, 2015

@author: waseem.irshad
'''

import os
import sys
import datetime
import requests
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from TemplateBaseClass import TemplateTestBase
import globalVars
import time
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import templateInputReqValueHyperV

class TestCase(TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def createEvalLicense(self):
        self.authenticate()
        print " Going to create Eval License ::"
        self.log_data(" Going to create Eval License ::")
        license_file_content = self.readFile(globalVars.eval_license_file_path)
        payload = self.readFile(globalVars.License_payload)
        payload = payload.replace("$license_content",license_file_content)
        feature = "License"
        basePath = "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port']
        uri = globalVars.serviceUriInfo[feature] 
        url = basePath +  uri + """?store=false&force=true"""
        uri = globalVars.serviceUriInfo[feature]
        headers=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()        
        response = requests.put(url, data=payload,headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        time.sleep(10)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        url = basePath +  uri + """?store=true&force=true"""
        headers=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()        
        response = requests.put(url, data=payload,headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        
    def displayLicenseCount(self):
        print " \n Total License Count  : "
        respLicen,status = self.getResponse("GET","License")
        if not status:
            self.log_data(" Could not get Response from License")
        else:
            count = respLicen['nodes']
            print " Total number of Resources : %s"%str(count)
            self.log_data(" Count : %s"%str(count))
        
        
        
if __name__ == "__main__":
    test = TestCase()
    test.createEvalLicense()
    test.displayLicenseCount()