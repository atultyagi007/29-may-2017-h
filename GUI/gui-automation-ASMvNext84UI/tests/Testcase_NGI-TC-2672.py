'''
Author: P.Suman
Created Date: Dec 2, 2015
Description: Verify that if the Admin assigns specific users while deploying without creating any user, there is an error message 
            and no failure.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Assign specific users while deploying without creating any user.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)    
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        templateName="Test Template"
        serviceName="Test Service"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=templateName, publishedTemplate=True)
        
        #Check for User creation
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
                
        #Get Service List and Delete 'Test Service' if exists
        serviceList = self.getServices(serviceName=serviceName)
        if len(serviceList) > 0:
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
        
        #Deleting Standard User
        userList = self.getLocalUsers(userName=globalVars.standardUser)
        if len(userList) > 0:
            self.deleteLocalUser(globalVars.standardUser, verifyUser=True)
        
        #Deploy a New Service
        self.deployService(templateName, serviceName, managePermissions=True, userList=[globalVars.standardUser], passExpectation=True)
        
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(serviceName)
        if len(result) > 0:
            if result[0]["Status"] == "Failed":
                self.failure("Service Deployment '%s' Failed"%serviceName, raiseExc=True)                
            else:
                self.succeed("Service Deployment '%s' was Successful"%serviceName)
        else:
            self.failure("Service Deployment '%s' Failed"%serviceName, raiseExc=True)
        
        
                
