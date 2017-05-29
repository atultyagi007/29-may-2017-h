'''
Author:preetam.sethi
Created Date: Feb 28, 2017
Description: Download a catalog, when its in error state, verify that you can see error catalog from templates page

Steps: create template, in template wizard under reference firmware repository you shouldn't see the catalogs that are in error state 
'''
from tests.globalImports import *
tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Download a catalog, when its in error state, verify that you can see error catalog from templates page
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
        optList=self.createTemplate(templateName="Template_3173", managePermissions=False, repositoryOnly=True, manageFirmware=True)
        #=======================================================================
        firmwareList=self.getRepositories()
        firmwareStateList=[firmware['State'] for firmware in firmwareList if firmware['Repository Name'] in optList]
        if "Error" not in firmwareStateList:
            self.succeed("Service page does not display Error catalog")
        else:
            self.succeed("Service page displays Error catalog", raiseExc=True)
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()