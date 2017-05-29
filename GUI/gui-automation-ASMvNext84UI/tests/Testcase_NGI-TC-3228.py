'''
Author: P.Suman
Created Date: Jan 7, 2015
Description: Clone default template
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Clone default template
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
        #Get Templates
        tempList = self.getTemplates(option="My Templates", templateName=self.templateName)
        if len(tempList) > 0:
            self.deleteTemplate(self.templateName)
        
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
        self.createTemplate(self.templateName, publishTemplate=False, managePermissions=False, 
            userList=["All"], templateType="Clone", cloneTemplateName="Deploy OS to Hard Drive")
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        This is the execution starting function
        """
        self.templateName = "Clone Template"
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
