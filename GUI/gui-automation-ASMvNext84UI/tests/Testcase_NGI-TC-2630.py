'''
Author: P.Suman
Created Date: Oct 29, 2015
Description: Verify that for a Standard user, the following options are displayed on the Landing page: Dashboard, 
            Services, Templates, Resources. Settings is not available to a Standard user.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Options on the Landing page for Standard user.
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
        
        #Verify Options
        self.verifyLandingPageOptions(userRole="Standard")
