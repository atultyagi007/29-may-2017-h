'''
Author: Ankit.Manglic
Created Date: June 09, 2016
Description: This test case contains the Chassis discover with existing credentials verification for following 3 test cases:
3016, 3018, 3024
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    This test case contains the Chassis discover with existing credentials verification for following 3 test cases:
        3016, 3018, 3024.
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
        
        #Configure Chassis and verify configuration values
        self.discoverChassis("172.31.61.196", "", "Dell chassis default", "Dell PowerEdge iDRAC Default", "Dell switch default")
        
        self.postRunCleanup()