"""
Author: P.Suman
Created Date: Dec 10, 2015
Description: Verify that on the Services page, the Standard user can Export All to file and generate a file 
        containing information about the services he owns or has access to.
"""

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user can Export All to file.
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
        self.deploySampleService("Standard Template", "Standard Service", userRole="Standard")
    
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
        #Verify Options
        self.getServiceOptions(actualOptions=["Export All", "Deploy New Service"], enableOptions=["Export All"])
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
            
    