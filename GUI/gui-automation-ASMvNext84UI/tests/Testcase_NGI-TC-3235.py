'''
Author: P.Suman
Created Date: Dec 16, 2015
Description: Cannot Publish a default template
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Cannot Publish a default template
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
        
        #Get All Sample Templates
        tempList = self.getTemplates(option="Sample Templates")
        if len(tempList) <= 0:
            self.failure("There are no Sample Templates for Admin user :: %s"%str(tempList), raiseExc=True)
        self.succeed("Existing Sample Templates :: %s"%str(tempList))
        #Get Template Options
        self.getTemplateOptions(actualOptions=["Edit", "View Details", "Clone", "Publish Template"], 
                disableOptions=["Publish Template"], templateName=tempList[0]["Name"], templateType="Sample Templates", templateDetailOptions=True)
        self.succeed("Cannot Publish Sample Template :: '%s'"%tempList[0]["Name"])        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup() 
