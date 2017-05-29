'''
Author: P.Suman
Created Date: Dec 09, 2015
Description: Verify that while editing a service, the Admin can remove specific Standard users to 
            remove access to service.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While editing a service, the Admin can remove specific Standard users to remove access to service.
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
        #Login as Admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Create Standard User if doesnt exist
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
        #Get Services
        serviceList = self.getServices(serviceName=self.serviceName)
        if len(serviceList) > 0:
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
        #Create Template if not exists
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume")                
        #Deploys a Service if does not exist
        self.deploySampleService(self.templateName, self.serviceName, userRole="Administrator", managePermissions=True,
                                 userList=[globalVars.standardUser])
    
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
        #Fetch Services to see if Standard User has Access to Service
        serviceList = self.getServices(serviceName=self.serviceName)
        if len(serviceList) > 0:
            self.succeed("Standard User has access to Service '%s' after granting access by Admin"%self.serviceName)
        else:
            self.failure("Standard User not able to access Service '%s' after granting access by Admin"%self.serviceName, raiseExc=True)
        #Edit Service and Remove user Permissions
        self.editService(self.serviceName, managePermissions=True, userList=[globalVars.standardUser], deleteUsers=True)
        #Login as Standard User
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Fetch Services to see if Standard User has Access to Service
        serviceList = self.getServices(serviceName=self.serviceName)
        if len(serviceList) <= 0:
            self.succeed("Standard User has no access to Service '%s' after removing access by Admin"%self.serviceName)
        else:
            self.failure("Standard User able to access Service '%s' after removing access by Admin"%self.serviceName, raiseExc=True)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.serviceName = "Test Service"
        self.templateName = "Test Template"
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
                    
