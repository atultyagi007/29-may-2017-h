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
     Taking Backup Now of Appliance 
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    def BackUpNowWithSettings(self):
        
        payload = self.readFile(globalVars.BackUpandRestorePayloadPath)
        payload = payload.replace("$sharePath",globalVars.sharePath).replace("$userName",globalVars.shareUsername).replace("$password",
                    globalVars.sharePassword).replace("$encypPasswd",globalVars.encryptionPassword)
        
        
        response,status = self.postRequest("backupNowWithSettings", payload=payload)
        if status in (201,202,203,204):
            utility.log_data( " Backup Successful  %s"%str(response))
            return True
        else:
            utility.log_data(" Unable to take Backup %s"%str(response))
            return False
        
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        
        #Login
        self.login()        
                    
        #Performing  Backup Now         
        statusCR = self.BackUpNowWithSettings()
        
        if statusCR:
            self.succeed("Backup Now of Appliance Successfully Completed ")
            
        else:
            self.failure("Failed toBackup Now of Appliance")
              
        time.sleep(120)
            
            
    
        
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Taking Backup Now of Appliance     
        """        
        
        self.runTestCase()
        
        