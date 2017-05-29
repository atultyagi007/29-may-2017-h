'''
Author: rajeev.kumar
Created Date: Nov 18, 2015
Description: Verify that for a Standard user, links under Learn on the Dashboard display correct information page.
Test Flow  :1)Login as a Admin user & create or Verify Standard user 
            2)Login as a Standard user,click on link under Learn on the Dashboard page 
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Standard user, links under Learn on the Dashboard display correct information page.
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Standard')
        self.getCurrentUser()
    
        # Navigating to Learn Section
        self.getSection_Learn()
        
        #Logout Read-only user
        self.logout()
        
    
        
