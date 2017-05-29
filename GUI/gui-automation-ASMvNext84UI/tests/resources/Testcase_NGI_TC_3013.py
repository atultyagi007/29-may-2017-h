'''
Author: Ankit.Manglic
Created Date: May 18, 2016
Description: Add new CMC user with maximum characters in the User Name
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Add new CMC user with maximum characters in the User Name
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
        
        #Create user and verify created
        self.addChassisResourceUser("172.31.60.130", "MaxCharacterUser", "pass123", True, "Administrator")
