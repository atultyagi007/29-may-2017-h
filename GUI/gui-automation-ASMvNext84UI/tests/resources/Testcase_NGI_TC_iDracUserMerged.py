'''
Author: Ankit.Manglic
Created Date: May 18, 2016
Description: This test case contains the verification for following 9 test cases:
2994, 2999, 3000, 3002, 3004, 3008, 3012, 3023, 3027
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    This test case contains the verification for following 9 test cases:
        2994, 2999, 3000, 3002, 3004, 3008, 3012, 3023 & 3027.
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
        
        userNames=["iDracHclUser1", "iDracHclUser2", "iDracHclUser3", "iDracHclUser4", "MaxCharacterUser", "iDracHclUser6", "DisabledUser", "iDracHclUser8", "iDracHclUser9"]
        roles = ["Administrator","Administrator","No Access","Administrator","Administrator","Operator", "Administrator", "Administrator", "Administrator"]
        lanRoles = ["Admin","Admin","Admin","None","Admin","Admin", "Admin", "Read Only", "Operator"]
        enableUser= [True,True,True,True,True,True,False,True,True]
        
        #Create user and verify created
        self.addChassisiDracUser("172.31.60.13", userNames, "pass123", roles, lanRoles, enableUser)
        
        self.postRunCleanup()
