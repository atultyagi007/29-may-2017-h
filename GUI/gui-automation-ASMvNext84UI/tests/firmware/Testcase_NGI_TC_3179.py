'''
Author: P.Suman
Created Date: Jan 4, 2016
Description: Upload invalid xml file, should throw error  invalid format error
        
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Upload invalid xml file, should throw error  invalid format error
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
                shutil.copy2(os.path.join(dirName, fileName), os.path.join(dirName, "../invalidCatalog.xml"))
                repositoryPath = os.path.join(os.path.dirname(dirName), "invalidCatalog.xml")
                with open(repositoryPath, "a") as wfp:
                    wfp.write("<mxl>")
            except:
                pass
        #Add Repository
        self.addRepository(addType="Network Path", repoPath=repositoryPath, repoUser=repositoryUser, repoPassword=repositoryPwd,
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
    