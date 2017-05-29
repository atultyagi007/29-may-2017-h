'''
Author: P.Suman
Created Date: Dec 31, 2015
Description: Enter invalid path and click on test connection button, should throw error
        
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Enter invalid path and click on test connection button, should throw error
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
        repositoryPath = globalVars.configInfo['Appliance']['catalogpath']
        repositoryUser = globalVars.configInfo['Appliance']['cataloguser']
        repositoryPwd = globalVars.configInfo['Appliance']['catalogpwd']
        if len(repositoryPath) > 0:
            try:
                fileName = os.path.basename(repositoryPath)
                dirName = os.path.dirname(repositoryPath)
                repositoryPath = os.path.join(dirName[:-1], fileName)
            except:
                pass
        #Add Repository
        self.addRepository(addType="Network Path", repoPath=repositoryPath, repoUser=repositoryUser, repoPassword=repositoryPwd,
                    defaultRepository=True, onlyTestConnection=True, negativeScenario=True)
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    