'''
Author       : raj.patel
Created Date : Jan 5, 2016
Description  : Verify that, on the Service detail page for an error service, the Standard user can Delete, Retry, View all settings, Adjust Resources and Export to file for a service created by self..
Test Flow    : 1) Login as admin create a template.
               2) Login as standard user and deploy service with settings such that it result into error perform the verifications. 
'''

from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Standard user can Delete, Retry, View all settings, Adjust Resources and Export to file for a service created by self.
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
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName=self.volumeName, volumeSize=self.volumeSize)
        #Deploys a Service if does not exist
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        self.deployService(self.templateName, self.serviceName,volumeName=self.volumeName)
#         self.deploySampleService(self.templateName, self.serviceName, loginAsUser=False)
     
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)

        self.getServiceManagableByStandardUser(serviceName=self.serviceName)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "ErrorTestTemplate" + currentTime
        self.volumeName = "HCLErrorV" + currentTime
        self.volumeSize = "0GB" 
        self.storageName = "testStorage"
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        
        self.logout()
