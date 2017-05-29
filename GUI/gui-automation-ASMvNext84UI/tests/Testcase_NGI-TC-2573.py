'''
Author: nidhi.aishwarya
Created Date: Dec 16, 2015
Description: Verify that, on the Service detail page for an in-progress service, the Standard user is unable to perform any action. 
Test Flow    : 1) Login as Admin user and publish the template with Storage and Server
               2) Login as Standard user and deploy the Service with storage and server
               3) Perform verifications on In-Progress services              
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that, on the Service detail page for an in-progress service, the Standard user is unable to perform any action.
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
        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)      

        self.get_ServicesPage("Standard","Verify In-Progress Services")
        
        self.logout()
