'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that the Read-only user is not able to login if deleted by the Admin
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only not able to login if deleted by the Admin
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
        
        #Verify whether logged in User is admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Check for readonly user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=False)
        
        #Disable Readonly User
        self.deleteLocalUser(userName=globalVars.readOnlyUser, verifyUser=True)
        
        #Logout
        self.logout()
        
        #Login with Readonly User
        self.login(username=globalVars.readOnlyUser, password=globalVars.rosPassword, newInstance=False, negativeScenario=True)
        