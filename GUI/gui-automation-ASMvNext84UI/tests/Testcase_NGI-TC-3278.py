'''
Created on Jan 21, 2015

@author: nidhi.aishwarya

Description : Verify that while creating a template, the Admin can select and update the server firmware. Verify that the firmware is updated before the deployment is initiated.
Test Flow    : 1) Login as Admin user
               2) Create A template and check the update firmware checkbox and perform verifications
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify that while creating a template, the Admin can select and update the server firmware. Verify that the firmware is updated before the deployment is initiated.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)    
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True) 
             
        self.get_ServicesPage("","Firmware_update_Template")
        
        self.logout()
      
            
     
        
       
        

