'''
Created on Jan 20, 2015

@author: ankit.manglic
Description: Verify memory attributes on m420 and m620 blade server, compare listed memory with memory listed in HW resources in iDRAC for the server.
                Not all attributes are visible through iDRAC, but confirmed those that are able to be queried there, such as speed, size, quantity.. 
Test Flow    : 1) Login as admin.
               2) Get the Blade server information from the main page.
               3) Open the iDRAC for specified server and get the attributes information from iDRAC page.
               4) compare the attributes values from both the pages. 
               
'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify memory attributes on m420 and m620 blade server, compare listed memory with memory listed in HW resources in iDRAC for the server.
                Not all attributes are visible through iDRAC, but confirmed those that are able to be queried there, such as speed, size, quantity..
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
        