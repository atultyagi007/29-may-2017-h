'''
Author: P.Suman
Created Date: Jan 5, 2016
Description: Upload iso file, should throw error, since it supports only xml and cab files
        
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Upload iso file, should throw error, since it supports only xml and cab files
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
        self.repositoryPath = globalVars.configInfo['Appliance']['ospath']
        if len(self.repositoryPath) <= 0:
            self.failure("ISO Path is not Provided :: '%s'"%self.repositoryPath, resultCode=BaseClass.OMITTED,raiseExc=True)
        fileName = os.path.basename(self.repositoryPath)
        if not fileName.endswith(".iso"):
            self.failure("ISO File Path is not Provided :: '%s'"%self.repositoryPath, resultCode=BaseClass.OMITTED,raiseExc=True)
        
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
        #Add Repository
        self.addRepository(addType="Network Path", repoPath=self.repositoryPath, repoUser="", repoPassword="",
                    defaultRepository=True, onlyTestConnection=False, negativeScenario=True)
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    