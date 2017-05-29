'''
Author: P.Suman
Created Date: Dec 07, 2015
Description: Verify that on the Services page, the Standard user can view his services and the services shared by the Admin.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Viewable services for Standard user.
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
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Create Template if not exists
        self.createSampleTemplate(templateName="Test Template", publishedTemplate=True, volumeName="autoVolume")
        #Create Template if not exists
        self.createSampleTemplate(templateName="Standard Template", publishedTemplate=True, volumeName="autoStandardVolume")        
        #Deploys a Service if does not exist
        self.deploySampleService("Test Template", "Test Service", userRole="Standard", loginAsUser=False)
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Deploys a Service if does not exist
        self.deploySampleService("Standard Template", "Standard Service", userRole="Standard", loginAsUser=False)
    
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
        #Get Service List
        serviceList = self.getServices()
        if len(serviceList) >= 2:
            self.succeed("'%s' User able to see Services created either by Admin or Standard user :: %s"%(globalVars.standardUser, str(serviceList)))
        else:
            self.failure("'%s' User not able to see Services created either by Admin or Standard user :: %s"%(globalVars.standardUser, str(serviceList)), raiseExc=True)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
                    
