'''
Author: nidhi.aishwarya
Created Date: Dec 1, 2015
Description: Verify that the services visible on Dashboard are either created by the Standard user or given access to by the Admin.
Assumption : Services visible on opening dashboard are considered
Test Flow : 1) Login as Standard user
            2) Verify individual Services on dashboard 
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that the services visible on Dashboard are either created by the Standard user or given access to by the Admin.
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
        
        self.get_View_Link("DashboardServices")
        
        self.logout()
        
       