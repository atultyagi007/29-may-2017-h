'''
Created on Nov 20, 2015

@author: rajeev.kumar

Description:Verify that for a Read-only user, links under Learn on the Dashboard display correct information page.
Test Flow  :1)Login as a Admin user & create or Verify Read-only user 
            2)Login as a Read-only user,click on link under Learn on the Dashboard page 
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
      Read-only user, links under Learn on the Dashboard display correct information page.    
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
        self.getCurrentUser()
        
        # Navigating to Learn Section
        self.getSection_Learn()
        
        #Logout Read-only user
        self.logout()     