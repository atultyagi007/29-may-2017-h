'''
Author: P.Suman
Created Date: Oct 1, 2015
Description: Verify that the Read-only user is unable to view any steps i.e Initial Setup, Define Networks, 
            Discover Resources and View and Publish Templates
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only user actions on Getting Started page
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
        self.verifyGettingStarted(userRole="Read only", verifyPageLaunch=False)
        