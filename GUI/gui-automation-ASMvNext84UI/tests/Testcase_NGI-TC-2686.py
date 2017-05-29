'''
Author: P.Suman
Created Date: Dec 09, 2015
Description: Verify that the Admin is able to disable all Standard users, assigned to template/services.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Admin is able to disable all Standard users, assigned to template/services.
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
        #Login as Admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Create Standard User if doesnt exist
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
        #Create Template if not exists
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume", deleteAndCreate=True)        
        #Deploys a Service if does not exist
        self.deploySampleService(self.templateName, self.serviceName, userRole="Standard", 
                                 userList=[globalVars.standardUser], managePermissions=True, loginAsUser=False)            
    
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
                    
