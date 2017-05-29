'''
Author: P.Suman
Created Date: Oct 1, 2015
Description: Verify that the Standard user is able to login and logout if enabled by the Admin
'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user able to login and logout if enabled by the Admin    
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
        
        #Verify Readonly user is able to login if Enabled
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Logout
        self.logout()
