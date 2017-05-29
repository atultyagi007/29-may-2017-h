'''
Author: Atul.kumar
Created Date: OCT 14, 2016
Description: This test case contains the verification for following 7 test cases:
3007,3311,3025,3006,2995,2997,3013
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    This test case contains the verification for following 7 test cases:
    3007,3311,3025,3006,2995,2997,3013
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
        
        #Create user and verify created
        userNames=["CMCHclAdmin1", "CMCHclPower1", "CMCHclGuest", "CMCHclNone","MaxCharact16User","CMCHclDisabled"]
        groups = ["Administrator","Power User","Guest User","None","Administrator","None"]
        enableUsers= [True,True,True,True,True,False]
        self.addChassisResourceUser("172.31.61.196", userNames, "pass123", enableUsers, groups,negativescenario=True)
        
        self.postRunCleanup()
