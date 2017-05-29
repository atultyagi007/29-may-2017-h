'''
Created on Jan 29, 2016

@author: rajeev.kumar
Description :Verify that correct current and available versions are displayed.
Test Flow   :1)Login as a Admin user & verify its current,Available versions.
'''
from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that correct current and available versions are displayed
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
        self.getOptionDetails_CurrentVersions("VirtualApplianceManagement")
        
        self.logout()