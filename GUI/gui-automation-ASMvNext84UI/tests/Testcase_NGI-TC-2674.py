'''
Author: P.Suman
Created Date: Dec 07, 2015
Description: Verify that the Admin is able to delete services initiated by Standard user, 
            even after the Standard user is deleted/disabled
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Admin able to delete services initiated by Standard user, even after the Standard user is deleted/disabled
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)    
    
    def preRunSetup(self, userOption="Delete"):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Create Template if not exists
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoStandardVolume")
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)       
        #Deploys a Service if does not exist
        self.deploySampleService(self.templateName, self.serviceName, userRole="Standard", loginAsUser=False)
        #Verify whether logged in User is admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        if userOption == "Delete":
            #Delete Standard User
            self.deleteLocalUser(userName=globalVars.standardUser, verifyUser=True)
        else:
            #Disable Standard User
            self.disableLocalUser(userName=globalVars.standardUser, verifyUser=True, useEdit=False, currentPassword=self.loginPassword)
    
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
        #Delete Service
        self.deleteService(self.serviceName)
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=True)
        if len(result) > 0:
            if result[0]["Status"] == "Deleted":
                self.succeed("Successfully Deleted Service '%s'"%self.serviceName)
            else:
                self.failure("Failed to Delete Service '%s'"%self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Delete Service '%s' in expected time"%self.serviceName, raiseExc=True)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.serviceName = "Standard Service"
        self.templateName = "Standard Template"
        self.browserObject = globalVars.browserObject

        self.preRunSetup(userOption="Delete")
        
        self.runTestCase()
        
        self.preRunSetup(userOption="Disable")
        
        self.runTestCase()
        
        self.postRunCleanup()
                    
