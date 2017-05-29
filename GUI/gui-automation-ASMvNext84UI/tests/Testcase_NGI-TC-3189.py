'''
Author: P.Suman
Created Date: Jan 6, 2016
Description: Download a catalog, when its in copying state, verify if you can select the copying catalog 
        from services page        
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Download a catalog, when its in copying state, verify if you can select the copying catalog 
    from services page
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
        #Delete Previous Repositories
        repoList = self.getRepositories(repoType="Firmware")
        for repo in repoList:
            if repo["Repository Name"] != "ASM Minimum Required":
                self.deleteRepository(repo["Repository Name"])        
        
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
        #Get Existing Repositories  
        repoList = self.getRepositories(repoType="Firmware")
        self.previousList = [repo["Repository Name"] for repo in repoList]
        #Add Repository
        self.addRepository(addType="Network Path", repoPath=repositoryPath, repoUser=repositoryUser, repoPassword=repositoryPwd,
                    defaultRepository=True, onlyTestConnection=False)
        #Get Current Repositories
        repoList = self.getRepositories(repoType="Firmware")
        self.currentList = [repo["Repository Name"] for repo in repoList]
        self.repositoryName = list(set(self.currentList).difference(set(self.previousList)))
        if len(self.repositoryName) <= 0:
            self.failure("Failed to fetch Current Repository Name :: Previous Repositories '%s' and Current Repositories '%s'"%(self.previousList,
                                                self.currentList), raiseExc=True)
        else:
            self.repositoryName = self.repositoryName[0]
        self.succeed("Current Repository Name :: '%s'"%self.repositoryName)
        #Get Repositories from Template Page
        repoList = self.getFirmwareReposFromService()
        if self.repositoryName in repoList:
            self.succeed("When downloaded catalog is in copying state, we are able to Select the copying catalog from Services page :: Repositories:'%s'"%repoList)
        else:
            self.failure("When downloaded catalog is in copying state, we are not able to Select the copying catalog from Services page :: Repositories:'%s'"%repoList,
                            raiseExc=True)
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    