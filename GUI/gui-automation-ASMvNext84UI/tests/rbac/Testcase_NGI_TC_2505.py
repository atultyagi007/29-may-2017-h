'''
Author: P.Suman
Created Date: Oct 30, 2015
Description: Verify that the Read-only user is unable to view enable/disable the Show welcome screen on next launch.
'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Enable/disable the Show welcome screen on next launch.
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
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', reLogin=True)
        
        #Verify Getting Started Page
        self.verifyGettingStarted(userRole="Read only", verifyPageLaunch=True)
        self.logout()
