'''
Author: P.Suman
Created Date: Dec 30, 2015
Description: Verify that the disabled Standard users are still listed in users or as template/services participants.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Disabled Standard user is listed in templates/services.
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
        #Create Template if not exists
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume",
                                  deleteAndCreate=True, userList=[globalVars.standardUser])
        #Get Services
        serviceList = self.getServices(serviceName=self.serviceName)
        if len(serviceList) > 0:
            #Deletes Service
            self.deleteService(self.serviceName)
        #Deploys a Service if does not exist
        self.deployService(self.templateName, self.serviceName, userList=[globalVars.standardUser])            
    
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
        #Disable Standard User
        self.disableLocalUser(userName=globalVars.standardUser, verifyUser=True, useEdit=False, currentPassword=self.loginPassword)
        #Logout of appliance
        self.logout()
        #Login with Standard User
        self.login(username=globalVars.standardUser, password=globalVars.rosPassword, newInstance=False, negativeScenario=True)
        #Login with Admin User
        self.login(username=self.loginUser, password=self.loginPassword, newInstance=False)
        #Get Template Users
        userList = self.getTemplateUsers(self.templateName)
        if globalVars.standardUser in userList:
            self.succeed("Successfully verified Disabled Standard user is listed in Template '%s' :: Users : '%s'"%(self.templateName, str(userList)))
        else:
            self.failure("Failed to verify Disabled Standard user is listed in Template '%s' :: Users : '%s'"%(self.templateName, str(userList)), raiseExc=True)
        #Get Service Users
        userList = self.getServiceUsers(self.serviceName)
        if globalVars.standardUser in userList:
            self.succeed("Successfully verified Disabled Standard user is listed in Service '%s' :: Users : '%s'"%(self.serviceName, str(userList)))
        else:
            self.failure("Failed to verify Disabled Standard user is listed in Service '%s' :: Users : '%s'"%(self.serviceName, str(userList)), raiseExc=True)
    
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
                    
