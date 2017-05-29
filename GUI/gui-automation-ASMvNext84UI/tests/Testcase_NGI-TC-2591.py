'''
Author: rajeev.kumar
Created Date: Nov 25, 2015
Description: Verify that for a Standard user, View all in Recent Activity on the Dashboard is unavailable.
Test Flow  :1)Login as a Admin & create/verify standard user
            2)Login as a standard user & View all in Recent Activity on the Dashboard is unavailable.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user, View all in Recent Activity on the Dashboard is unavailable..
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
        # Navigating to Dashboard Page
        self.get_View_Link("View All")
        
        #logout standard user
        self.logout()
        
        
    
        
