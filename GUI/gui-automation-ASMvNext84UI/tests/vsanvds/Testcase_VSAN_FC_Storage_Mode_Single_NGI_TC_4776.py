'''
Author: Atul
Created Date: NOV 17, 2016
Description: This test case contains the verification for following 3 test cases:
4776,4777,4778

Prerequisite:- FC chessis 
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    This test case contains the verification for following 5 test cases:
        2998, 3001, 3003, 3014, 3021.
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
        StorageMode="Split Single Host"
        self.chassisConfiguration("172.31.61.131", "HclTestChassis", "TestDns", "HclTestDC", "cold", "Rack5", "5", "fxser177-swe", "IOMA1-test",StorageMode=StorageMode)
        
        self.postRunCleanup()
