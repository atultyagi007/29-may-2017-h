'''
Author       : ankit.manglic
Created Date : Dec 30, 2015
Description  : Verify that, on the Service detail page for an error service, the Standard user can only View all settings and
                 Export to file for a service shared by the Admin.
Test Flow    : 1) Login as admin create a template if not created and deploy service with settings such that it result into error.
               2) Login as standard user and perform the verifications. 
'''

from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user can View All settings of the error service and can export to file.
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
        self.deployService(self.templateName, self.serviceName,volumeName=self.volumeName)
        
#         self.deploySampleService(self.templateName, self.serviceName, loginAsUser=False, verifySuccess=False)
     
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
        #Get Template Options
        tempSettings = self.getServiceSettings(serviceName=self.serviceName)
        if len(tempSettings) <= 0:
            self.failure("Failed to fetch Service Deployment Settings for Service '%s' :: %s"%(self.serviceName, str(tempSettings)), raiseExc=True)
        self.succeed("Successfully fetched Service Deployment Settings for Service '%s' :: %s"%(self.serviceName, str(tempSettings)))
        #Verify export file
        self.getServiceExportFile(serviceName=self.serviceName)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "ErrorTestTemplate" + currentTime
        self.volumeName = "HclErVol" + currentTime
        self.volumeSize = "0GB" 
        self.storageName = "testStorage"
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        
        self.logout()
