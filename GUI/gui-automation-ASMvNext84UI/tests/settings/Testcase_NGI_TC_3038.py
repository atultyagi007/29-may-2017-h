'''
Created on Nov 07, 2016

@author: raj.patel
Description:  Network config- enter 4 digits in octet.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Network config- enter 4 digits in octet
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
              
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        self.defineNWSchanario("TestNTW", "networkDescription", "HYPERVISOR_MIGRATION", 23, configureStatic=True,
                      subnet="255.255.255.000", gateway="172.31.23.2544", primaryDNS="172.31.62.1", secondaryDNS="172.31.62.2", dnsSuffix="", 
                      startingIPAddress="172.31.23.76", endingIPAddress="172.31.23.80")
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        