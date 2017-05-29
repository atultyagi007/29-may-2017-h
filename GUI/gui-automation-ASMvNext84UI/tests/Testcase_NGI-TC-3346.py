'''
Author: P.Suman
Created Date: Jan 7, 2015
Description: Verify that View details on template allows user to view the template information
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    View details on template
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
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=self.templateName)
        
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
        #Get Template Options
        self.getTemplateOptions(actualOptions=["View Details"], enableOptions=["View Details"], 
                                templateName=self.templateName)
        #Reading Template Information
        tempSettings = self.getTemplateSettings(self.templateName)
        if len(tempSettings) <= 0:
            self.failure("Failed to read Template Settings from 'View Details'", raiseExc=True)
        else:
            self.succeed("Successfully retrieved Template Settings from 'View Details' :: '%s'"%tempSettings)
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        This is the execution starting function
        """
        self.templateName = "Test Template"
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
