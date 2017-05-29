'''
Created on Feb 21, 2016

@author: Dheeraj.Singh
'''
from libs.product import BaseClass
from libs.product import utility
import time

tc_id=utility.get_tc_data(__file__)

class Testcase(BaseClass.TestBase): 
    """
    Configure  Resources
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    
    def test_configureResourec(self):
        
        
        try:
            self.setupCredentials()
            self.chassisConfigureResource() 
            time.sleep(60)                          
            self.configureResource()
            
        except Exception as e1:
            self.log_data( 'Exception occurred while Configure Resource ')
            self.log_data(str(e1))
            
        wizardResponse, wizardStatus = self.setUpCompleteWizard() 
        
        time.sleep(10)
        
        return wizardResponse, wizardStatus
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        
        #Login
        self.login()        
                    
        #Performing Configure Resources         
        ResultCR, statusCR = self.test_configureResourec()
        
        if statusCR:
            self.succeed("Configure  Resources Step Successfully Completed %s"%ResultCR)
            
        else:
            self.failure("Failed to Configure  Resources Step  %s"%ResultCR)
              
        time.sleep(120)
            
            
    
        
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Configure  Resources        
        """        
        
        self.runTestCase()
        
        