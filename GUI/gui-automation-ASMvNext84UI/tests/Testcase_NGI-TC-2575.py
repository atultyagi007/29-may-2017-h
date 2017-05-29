'''
Created on Dec 23, 2015

@author: nidhi.aishwarya

Description : Verify that the Standard user able to view service detail even if he does not have access to the server pool in the service.
Test Flow    : 1) Login as Admin user and un-assign a server pool access to standard user
               2) Create A Service with that Server pool through Admin user
               3) Login as Standard user and verify that Service is visible to Standard user       

'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify that the Standard user able to view service detail even if he does not have access to the server pool in the service.
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
        #Check for current logged in user
        self.templateName = "Test Template"
        
        self.serviceName = self.get_TemplatesPage("Server Template")
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject
     
        self.runTestCase()
         
        self.postRunCleanup()

