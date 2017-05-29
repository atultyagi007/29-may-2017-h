'''
Created on Feb 4, 2016

@author: rajeev.kumar
Description :Navigate to the Dashboard page and ensure all the information in accurate and all links work and opens the
appropriate page.
Test Flow  :1)Login as Admin user and Landing Dashboard page.
           :2)Click and verify all links in Dashboard page.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that the links under Dashboard page.
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
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        self.getDashboard_AllLinks()
        self.logout()