'''
Author: P.Suman
Created Date: Dec 30, 2015
Description: Verify that the Admin is unable to delete specific Standard users, assigned to template/services.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Admin is unable to delete specific Standard users, assigned to template/services.
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
        self.deleteLocalUser(globalVars.standardUser, verifyUser=True)
        #Logout of appliance
        self.logout()
        #Login with Standard User
        self.login(username=globalVars.standardUser, password=globalVars.rosPassword, newInstance=False, negativeScenario=True)
        #Login as Admin
        self.login(username=self.loginUser, password=self.loginPassword, newInstance=False)
        #Verify Templates
        tempList = self.getTemplates(viewType="List")
        if len(tempList) > 0:
            self.succeed("Able to read 'Templates' after deleting Standard User associated to template/service :: %s"%str(tempList))
        else:
            self.failure("Failed to read 'Templates' after deleting Standard User associated to template/service :: %s"%str(tempList), raiseExc=True)
        
        #Verify Services in List form
        svcList = self.getServices()
        if len(svcList) > 0:
            self.succeed("Able to read 'Services' after deleting Standard User associated to template/service :: %s"%str(svcList))
        else:
            self.failure("Failed to read 'Services' after deleting Standard User associated to template/service :: %s"%str(svcList), raiseExc=True)
    
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
                    
