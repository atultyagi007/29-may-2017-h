'''
Author       : ankit.manglic
Created Date : Jan 05, 2015
Description  : Verify that while deploying a service, created by self from template by the Admin, the Standard user is unable to update server firmware.
Test Flow    : 1) Login as admin create a template if not created and deploy service with settings such that it result into error.
               2) Login as standard user and perform the verifications. 
'''

from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)


class Testcase(Manager.Manager): 
    """
    When a standard user deploys a service from a template, the option to Manage server firmware should not be available.
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
        #Create Template if not exists
        self.createSampleTemplate(self.templateName, publishedTemplate=True)
     

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Deploys a Service and verify Manage Firmware Option not available.
        self.verifyManageFirmwareOption(self.templateName, self.serviceName)
        
        
    def postRunCleanup(self):
        """
        Cleaning the services and template created for this test case.
        """
        self.logDesc("Post run Cleanup")
        #Logout of application
        self.logout()   
    
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.storageName = "TestStorage" + currentTime
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
         
        self.runTestCase()
        
        self.postRunCleanup()
         