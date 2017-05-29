'''
Created on May 21, 2015

@author: waseem.irshad
'''

import time
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
import globalVars
import datetime
import requests
import json
from utilityModule import UtilBase

class BackUpAndRestore(UtilBase):
    
    tc_Id = "" 
    
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def postRequest(self,serviceName,payload):
        logger = self.getLoggerInstance()
        url = self.buildUrl(serviceName)
        logger.info("printing  url")
        logger.info(url)
        
        uri = globalVars.serviceUriInfo[serviceName]
        logger.info(uri)
        headers=self.generateHeaderforDiscoverChassis(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("Printing headers information")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=payload, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return self.convertUTA(data), True
            else:
                return "No information found for %s"%str(serviceName), response.status_code
        else:
            return str(response.status_code) + " " + str(self.convertUTA(response.text)), False

        
    def BackUpNowWithSettings(self):
        self.authenticate()
        payload = self.readFile(globalVars.BackUpandRestorePayloadPath)
        payload = payload.replace("$sharePath",globalVars.sharePath).replace("$userName",globalVars.shareUsername).replace("$password",
                    globalVars.sharePassword).replace("$encypPasswd",globalVars.encryptionPassword)
        print " PAYLOAD : "
        print payload
        #response = self.getResponse("POST", "backupNowWithSettings", payload=payload)
        response,status = self.postRequest("backupNowWithSettings", payload=payload)
        if status in (201,202,203,204):
            self.log_data( " Backup Successful")
            return True
        else:
            self.log_data(" Unable to take Backup ")
            return False
        
    def Restore(self):
        self.authenticate()
        payload = self.readFile(globalVars.BackUpandRestorePayloadPath)
        payload = payload.replace("$sharePath",globalVars.sharePath).replace("$userName",globalVars.shareUsername).replace("$password",
                    globalVars.sharePassword).replace("$encypPasswd",globalVars.encryptionPassword)
        response,status = self.postRequest("restore", payload=payload)
        if status in (201,202,203,204):
            self.log_data(" Restarting the Appliance .......")
            self.getResponse("POST","reboot",'')
            return True
        else:
            self.log_data(" Unable to restore the device ")
            return False
            
        
    def BackUpSettings(self):
        #self.authenticate()
        payload = self.readFile(globalVars.BackUpandRestorePayloadPath)
        payload = payload.replace("$sharePath",globalVars.sharePath).replace("$userName",globalVars.shareUsername).replace("$password",globalVars.sharePassword).replace("$encypPasswd",globalVars.encryptionPassword)
        resGET = self.getResponse("GET","backupSettings")
        print " resGET : "
        print resGET
        if resGET[0]['sharePath'] == 'Not determined':
            response,status = self.postRequest("backupSettings", payload=payload)
            if status in (201,202,203,204):
                self.log_data(" Applied the BAckup Settings")
                return status
            else:
                self.log_data(" Unable to apply backup settings ")
                
        else:
            response,status = self.getResponse("PUT","backupSettings", payload=payload)
            
    
    def ScheduleBackup(self):
        self.authenticate()
        retVal = self.BackUpSettings()
        payload = self.readFile(globalVars.ScheduleBackupPayload)
        payload =  payload.replace("$backHour",globalVars.schedulebackupHour).replace("$backupMin",globalVars.schedulebackupMinute).replace("$backUpDay",globalVars.schedulebackupDaysOfWeek)
        response,status = self.postRequest("backupSchedule", payload=payload)
        if status in (201,202,203,204):
            self.log_data(" Successfully scheduled the backup ")
            return True
        else:
            self.log_data(" Unable to schedule backup ")
            return False
                
        
        
if __name__ == "__main__":
    test = BackUpAndRestore()
    #test.BackUpNowWithSettings()
    #test.Restore()
    #test.ScheduleBackup()
        