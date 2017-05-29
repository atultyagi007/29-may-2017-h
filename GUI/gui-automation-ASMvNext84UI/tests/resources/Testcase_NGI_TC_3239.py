'''
Created on Feb 01, 2015

@author: ankit.manglic
Description: Discover a server/blade in power OFF state verify performance not displayed then power ON server, 
                Performance data should be displayed after the server has been powered ON. 
Test Flow    : 1) Login as admin.
               2) Login into iDRAC for given server and verify it is in OFF state.
               3) Discover the server and verify performance data is not displayed.
               4) Again log into iDRAC and ON the server ands verify performance data is displayed. 
               
'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Discover a server/blade in power on state verify performance data then power off server, 
                Performance data should be displayed even after the server has been powered off.
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
        #logout of application
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.changeServerSettings("172.31.32.82", "OFF")
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        