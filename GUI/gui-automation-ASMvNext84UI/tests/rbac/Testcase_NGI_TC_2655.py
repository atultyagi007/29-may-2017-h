'''
Author: nidhi.aishwarya
Created Date: Dec 30, 2015
Description: Verify that, on the Service detail page for an warning service,  the Standard user can only View all settings and Export to file for a service shared by the Admin. 
PreRequisites : Warning Service is present  
Test Flow    : 1) Login as Standard user
               2) Navigate to Warning Services page and perform Verifications        
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that, on the Service detail page for an warning service,  the Standard user can only View all settings and Export to file for a service shared by the Admin.
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
        
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)    

        self.get_ServicesPage("Standard","WarningServices")
        
        self.logout()
