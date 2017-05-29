'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that the Standard user is not able to login if disabled by the Admin
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user unable to login if disabled by the Admin
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
        
        #Check if Read only user is already Disabled
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False, enableUser=False, disableUser=True)
        
        #Logout from 'Admin' user
        self.logout()
        
        #Login with Readonly User
        self.login(username=globalVars.standardUser, password=globalVars.rosPassword, newInstance=False, negativeScenario=True)
        
        