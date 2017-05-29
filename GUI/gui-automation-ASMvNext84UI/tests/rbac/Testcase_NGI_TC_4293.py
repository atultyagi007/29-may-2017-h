'''
Author:nidhi.aishwarya
Created Date: Dec 21, 2015
Description: Verify that on the Firmware page, under Services affected in Firmware, the Read-only can view all the services.
Test Flow    : 1) Login as Read Only user and Navigate to Services page
               2) Verify that all firmware options should be disabled for ReadOnly user for a service. 
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that on the Firmware page, under Services affected in Firmware, the Read-only can view all the services.
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
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Read only')
    
        self.get_ServicesPage("","FirmwareActions")
        
        self.logout()
        
        
        
    
        
