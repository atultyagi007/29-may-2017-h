'''
Created on Nov 30, 2015

@author: rajeev.kumar

Description : Verify that for a Standard user, the Recent Activity on the Dashboard lists only the activities that the Standard user
 perform or has permission to perform.
 Test Flow  :1)Login as a Admin & create/verify standard user
            2)Login as a standard user & verify Recent activity on the dashboard lists
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user can deploy a service from a template shared by the Admin
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        self.get_RecentActivity("Standard", "")
        
        #logout Standard user
        self.logout()