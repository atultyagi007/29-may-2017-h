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
    Define Networks
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        
        #Login
        self.login()        
                    
        #Performing Initial setup         
        resultNW, statusNW = self.setupNetworks()
        
        if statusNW:
            self.succeed("Define Networks Step Successfully Completed %s"%resultNW)
            
        else:
            self.failure("Failed to Define Networks %s"%resultNW)
              
        time.sleep(120)
            
            
    
        
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Define Networks        
        """        
        
        self.runTestCase()
        
        