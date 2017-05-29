'''
Author: raj.patel
Created Date: Sep 01, 2016
Description: This test case contains the Chassis discover with new credentials verification for following 3 test cases:
3017, 3019, 3020
'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    This test case contains the Chassis discover with existing credentials verification for following 3 test cases:
        3017, 3019, 3020.
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
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')
        self.chassisCredential = "Chassis" + currentTime
        self.serverCredential = "Server" + currentTime
        self.switchCredential = "Switch" + currentTime
        
        
        #Configure Chassis and verify configuration values
        self.discoverChassis("172.31.61.196", "", self.chassisCredential, self.serverCredential, self.switchCredential, newCredential=True)
        
        self.postRunCleanup()