'''
Author: Atul Kumar
Created Date: November 8, 2016
Description: Add new iDrac user with mismatched passwords (password and confirm password)
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Add new iDrac user with mismatched passwords (password and confirm password)
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        
        self.logout()
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        userNames=["iDracHclUser1"]
        roles = ["Administrator"]
        lanRoles = ["Admin"]
        enableUser= [True]
        
        #Create user and verify created
        self.addChassisiDracUser("172.31.61.196", userNames, "pass123", roles, lanRoles, enableUser,confirmPass="mismatchPass",negativescenario=True)
        
        self.postRunCleanup()
