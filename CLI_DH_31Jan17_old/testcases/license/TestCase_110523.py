'''
Created on Nov 4, 2015

@author: waseem.irshad
'''

import os
import sys
import datetime
import requests
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)

sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
import globalVars
import time
from utilityModule import UtilBase


class TestCase(UtilBase):
    
    '''
        Test Case Descriptipn : Add 500 perpetual to 30 perpetual, it should total to 530 no of resources
        Pre-Requisite : Run test case 110521 first to upload 30 perpetual license
    '''
    
    tc_Id=""
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
    def AddPerpetualLicense(self):
        self.authenticate()
        print " Going to add Perpetual License ::"
        license_file_content = self.readFile(globalVars.perp_license_file_path)
        payload = self.readFile(globalVars.License_payload)
        payload = payload.replace("$perp_license_content",license_file_content)
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
        print response.text
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(10)
        respLicen,status = self.getResponse("GET","License")
        print respLicen
        if not status:
            self.log_data(" Could not get Response from License")
        else:
            count = respLicen['nodes']
            print " Total count of Resources : %s"%str(count)
            self.log_data(" Count : %s"%str(count))
            
        
if __name__ == "__main__":
    test = TestCase()
    test.AddPerpetualLicense()
    
