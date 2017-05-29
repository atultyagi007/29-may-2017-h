'''
Created on Feb 1, 2016

@author: rajeev.kumar
Description :Discover storage and verify health status is good.
Test Flow   :1)Login as a Admin user & Verify storages health.
'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Discover storage and verify health status is good
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
        #check & update Virtual Appliance
        self.getOptionDetails_DiscoverStorage("Resources")
        
        self.logout()