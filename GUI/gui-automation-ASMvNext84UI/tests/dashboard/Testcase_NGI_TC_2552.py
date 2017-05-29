'''
Created on Nov 30, 2015

@author: nidhi.aishwarya

Description : Verify that for a Read-only user, the Recent Activity on the Dashboard lists the correct information, i.e all the activities.
Test Flow : 1) Login as ReadOnly user
            2) Create Service and verify at Recent Activity section at dashboard page and perform verifications
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Read-only user, the Recent Activity on the Dashboard lists the correct information, i.e all the activities.
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
        
        self.get_RecentActivity("ReadOnly", "")
        
        self.logout()
       
        

