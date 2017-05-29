'''
Author: nidhi.aishwarya
Created Date: Nov 26, 2015
Description: Verify that for a Read-only user, clicking on individual service link on the Dashboard takes to Service Information page.
Test Flow : 1) Login as ReadOnly user
            2) Click on any individual Service type at dashboard page and perform verifications
'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Read-only user, clicking on individual service link on the Dashboard takes to Service Information page.
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

        self.get_IndividualServices()
        
        self.logout()
        
       
            
        
        
    
        
