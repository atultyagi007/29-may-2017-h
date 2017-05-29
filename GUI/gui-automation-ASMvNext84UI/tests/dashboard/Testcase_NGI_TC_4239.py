'''
Author: nidhi.aishwarya
Created Date: Dec 1, 2015
Description: Verify that for a Read-only user, all the services are visible on the Dashboard, which are either created by the Standard user or by the Admin.
Assumption : Services are visible on opening dashboard 
Test Flow : 1) Login as ReadOnly user
            2) Click on any individual Services at dashboard page and perform verifications

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Read-only user, all the services are visible on the Dashboard, which are either created by the Standard user or by the Admin.
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
        
        self.get_View_Link("DashboardServices")
        
        self.logout()
        
       