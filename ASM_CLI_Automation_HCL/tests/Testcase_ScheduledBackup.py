'''
Created on Feb 21, 2016

@author: Dheeraj.Singh
'''
from libs.product import BaseClass
from libs.product import utility
from libs.product import globalVars
import time

tc_id=utility.get_tc_data(__file__)

class Testcase(BaseClass.TestBase): 
    """
     Automatically Scheduled Backups of Appliance 
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    def BackUpSettings(self):
      
        payload = self.readFile(globalVars.BackUpandRestorePayloadPath)
        payload = payload.replace("$sharePath",globalVars.sharePath).replace("$userName",globalVars.shareUsername).replace("$password",globalVars.sharePassword).replace("$encypPasswd",globalVars.encryptionPassword)
        resGET = self.getResponse("GET","backupSettings")
        
        if resGET[0]['sharePath'] == 'Not determined':
            response,status = self.postRequest("backupSettings", payload=payload)
            if status in (201,202,203,204):
                utility.log_data(" Applied the BAckup Settings    %s"%str(response))
                return status
            else:
                utility.log_data(" Unable to apply backup settings  %s"%str(response))
                
        else:
            self.getResponse("PUT","backupSettings", payload=payload)
    
    def ScheduleBackup(self):
       
        self.BackUpSettings()
        payload = self.readFile(globalVars.ScheduleBackupPayload)
        payload =  payload.replace("$backHour",globalVars.schedulebackupHour).replace("$backupMin",globalVars.schedulebackupMinute).replace("$backUpDay",globalVars.schedulebackupDaysOfWeek)
        response,status = self.postRequest("backupSchedule", payload=payload)
        if status in (201,202,203,204):
            utility.log_data(" Successfully scheduled the backup  %s"%str(response))
            return True
        else:
            utility.log_data(" Unable to schedule backup  %s"%str(response))
            return False
    
    
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        
        #Login
        self.login()        
                    
        #Performing Scheduled Backup          
        statusCR = self.ScheduleBackup()
        
        if statusCR:
            self.succeed("Automatically Scheduled Backups of Appliance Successfully Completed ")
            
        else:
            self.failure("Failed to Automatically Scheduled Backups")
              
        time.sleep(120)
            
            
    
        
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Automatically Scheduled Backups of Appliance     
        """        
        
        self.runTestCase()
        
        