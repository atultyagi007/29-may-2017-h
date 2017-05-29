'''
Author: nidhi.aishwarya
Created Date: Dec 22, 2015
Description: Verify that on the Service detail page for an error service, the Read-only user can only View all settings. 
PreRequisites : Test case first create an error service and then validate   
Test Flow    : 1) Login as Read Only user and Navigate to Services page
               2) Verify that all firmware options should be disabled for ReadOnly user for a service.         
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that on the Service detail page for an error service, the Read-only user can only View all settings.
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
        
        self.serviceName = self.get_ServicesPage("ReadOnly","Error Services")
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject
     
        self.runTestCase()
         
        self.postRunCleanup()
