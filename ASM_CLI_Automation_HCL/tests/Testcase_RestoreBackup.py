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
     Restore Backup Now
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    
    def Restore(self):
        
        payload = self.readFile(globalVars.BackUpandRestorePayloadPath)
        payload = payload.replace("$sharePath",globalVars.sharePath).replace("$userName",globalVars.shareUsername).replace("$password",
                    globalVars.sharePassword).replace("$encypPasswd",globalVars.encryptionPassword)
        response,status = self.postRequest("restore", payload=payload)
        if status in (201,202,203,204):
            utility.log_data(" Restarting the Appliance .......  %s"%str(response))
            self.getResponse("POST","reboot",'')
            return True
        else:
            utility.log_data(" Unable to restore the device    %s"%str(response))
            return False
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        
        #Login
        self.login()        
                    
        #Performing Restore Backup Now         
        statusCR = self.Restore()
        
        if statusCR:
            self.succeed("Restarting the Appliance Successfully Completed ")
            
        else:
            self.failure("Failed to Restarting the Appliance")
              
        time.sleep(120)
            
            
    
        
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Restore Backup Now      
        """        
        
        self.runTestCase()
        
        