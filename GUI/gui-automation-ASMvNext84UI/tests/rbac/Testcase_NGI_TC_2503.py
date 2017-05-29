'''
Author: nidhi.aishwarya
Created Date: Dec 30, 2015
Description: Verify that  on the Service detail page for an warning service, the Read-only user can only View all settings. 
PreRequisites : Warning Service is present 

Test Flow    : 1) Login as Read Only user and Navigate to  Warning Services page
               3) Verify that all options except View All should be disabled for Read Only user               
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that  on the Service detail page for an warning service, the Read-only user can only View all settings.
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
        
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)    

        self.get_ServicesPage("ReadOnly","WarningServices")
        
        self.logout()
