'''
Created on Sep 15, 2016

@author: preetam.sethi
Description: Verify that for 13G server detailed views Correct System Usage, CPU usage, Memomry and I/O usage values 
        are dsiplayed along with graphs and pie charts
        Testcase_NGI_TC_3217 merged to verify CPU Usage Statistics 
Test Flow    : 1) Login as admin.
               2) go to Resource page, select 13G servers and view details
               3) verify all usage category information are displayed
'''

from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for 13G server detailed views Correct System Usage, CPU usage, Memomry and I/O usage values 
    are dsiplayed along with graphs and pie charts
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
        self.verifyUsageStatsDisplayed()
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        