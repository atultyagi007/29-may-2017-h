'''
Created on Jan 29, 2015

@author: ankit.manglic
Description: Verify memory Memory tab for C series servers, compare listed memory with memory listed in HW resources in iDRAC for the server.
Test Flow    : 1) Login as admin.
               2) Get the c Series server information from the main page.
               3) Open the iDRAC for specified server and get the attributes information from iDRAC page.
               4) compare the attributes values from both the pages. 
               
Note: C Series server are currently not available hence using Rack server.
'''

from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify memory Memory tab for C series servers, compare listed memory with memory listed in HW resources in iDRAC for the server.
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
        self.verifyServerMemoryAttributesWithiDRAC("Rack")
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        