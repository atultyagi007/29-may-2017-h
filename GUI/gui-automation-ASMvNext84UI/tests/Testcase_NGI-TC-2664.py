'''
Author: P.Suman
Created Date: Dec 09, 2015
Description: Verify that while editing a template, the Admin can remove specific Standard users to 
        remove access to template
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While editing a template, the Admin can remove specific Standard users to remove access to template.
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
        #Create Template
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume",
                                  managePermissions=True, userList=[globalVars.standardUser], deleteAndCreate=True)
    
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
        tempList = self.getTemplates(option="My Templates", templateName=self.templateName)
        if len(tempList) > 0:
            self.succeed("Standard User has access to Template '%s' after granting access by Admin"%self.templateName)
        else:
            self.failure("Standard User not able to access Template '%s' after granting access by Admin"%self.templateName, raiseExc=True)
        #Edit Template and Remove user Permissions
        self.editTemplate(self.templateName, managePermissions=True, userList=[globalVars.standardUser], deleteUsers=True)
        #Login as Standard User
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Fetch Templates to see if Standard User has Access to Template
        tempList = self.getTemplates(option="My Templates", templateName=self.templateName)
        self.succeed("Available Templates for Standard User :: %s"%str(tempList))
        if len(tempList) > 0:
            self.failure("Standard User able to access Template '%s' after removing access by Admin"%self.templateName, raiseExc=True)
        else:
            self.succeed("Standard User has no access to Template '%s' after removing access by Admin"%self.templateName)
    
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
                    
