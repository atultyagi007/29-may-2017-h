"""
Author: P.Suman
Created Date: Nov 26, 2015
Description: Verify that on the Service detail page, the Standard user cannot edit, delete or 
        retry a service shared by the Admin.
"""

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user cannot edit, delete or retry a service shared by the Admin.
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
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume")
        #Deploys a Service if does not exist
        self.deploySampleService(self.templateName, self.serviceName, userRole="Standard", loginAsUser=False)
    
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
        self.getServiceOptions(actualOptions=["Export to File", "Delete", "Retry", "View All Settings", "Edit"], 
                    disableOptions=["Edit", "Delete", "Retry"], serviceName=self.serviceName, serviceDetailOptions=True)
    
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
       