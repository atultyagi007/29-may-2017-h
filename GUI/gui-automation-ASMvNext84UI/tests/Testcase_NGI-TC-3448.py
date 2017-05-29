'''
Author: P.Suman
Created Date: Oct 1, 2015
Description: Verify that the deleted Standard user is not listed in users or as template/services participant
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Deleted Standard user is not listed in users
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
        #Verify whether logged in User is admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)        
        #Get Service List
        serviceList = self.getServices()        
        if len(serviceList) > 0:
            for service in serviceList:
                serviceName = service["Name"]
                self.deleteService(serviceName)
                #Wait for Deployment to complete
                result = self.getDeploymentStatus(serviceName, deleteStatus=True)
                if len(result) > 0:
                    if result[0]["Status"] == "Deleted":
                        self.succeed("Successfully Deleted Service '%s'"%serviceName)
                    else:
                        self.failure("Failed to Delete Service '%s'"%serviceName, raiseExc=True)
                else:
                    self.failure("Failed to Delete Service '%s' in expected time"%serviceName, raiseExc=True)
        #Get Templates
        tempList = self.getTemplates(option="My Templates")
        for template in tempList:
            templateName = template["Name"]
            self.deleteTemplate(templateName)
            
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
    
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
        #Delete Standard User
        self.deleteLocalUser(userName=globalVars.standardUser, verifyUser=True)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup() 
