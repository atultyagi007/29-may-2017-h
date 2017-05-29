'''
Created on Dec 28, 2015

@author: rajeev.kumar
Description :Verify that on the Firmware page, under Services affected in Firmware, the Standard user can view only the services.
he has access to.

'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Firmware page, under Services affected in Firmware, the Standard user can view only the services he has access to.
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
#         self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume")
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName="autoVolume")
        #Deploys a Service if does not exist
        self.deploySampleService(self.templateName, self.serviceName, userRole="Standard", loginAsUser=False)
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.logout()

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)        
        #Get Template Options
        self.browserObject,status,tempSettings = self.viewDeployment_services_Settings(serviceName=self.serviceName)
        if len(tempSettings) <= 0:
            self.failure("Failed to fetch Service Deployment Settings for Service '%s' :: %s"%(self.serviceName, str(tempSettings)), raiseExc=True)
        self.succeed("Successfully fetched Service Deployment Settings for Service '%s' :: %s"%(self.serviceName, str(tempSettings)))
        
        #Verification
        #self.verifyTemplateSettings(tempSettings)        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
#         self.serviceName = "Test Service"
#         self.templateName = "Test Template"
#         
#         self.browserObject = globalVars.browserObject
# 
#         self.preRunSetup()
#         
#         self.runTestCase()
#         
#         self.postRunCleanup()
#         
#         self.logout()
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.storageName = "testStorage"
        self.volumeName = "testVol"+currentTime
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        