'''
Author: P.Suman
Created Date: Jan 5, 2016
Description: Default catalog can be deleted
        
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Default catalog can be deleted
    """    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
        self.repositoryName = None
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Delete Previous Repositories
        repoList = self.getRepositories(repoType="Firmware")
        additionalRepos = [repo["Repository Name"] for repo in repoList if repo["Repository Name"] != "ASM Minimum Required"]
        if len(additionalRepos) > 0:
            self.repositoryName = additionalRepos[0]
            repoName = self.getDefaultRepository()
            if repoName != self.repositoryName:
                self.setDefaultRepository(self.repositoryName)
        else:
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
        if not self.repositoryName:
            self.failure("Unable to identify Added Repository", raiseExc=True) 
        #Get Default Repository
        repoName = self.getDefaultRepository()
        if repoName != self.repositoryName:
            self.failure("Failed to Set Repository '%s' as Default Repository"%(self.repositoryName),
                            raiseExc=True)
        #Delete Repository
        self.deleteRepository(self.repositoryName)
        #Verification
        repoList = self.getRepositories(repoName=self.repositoryName)
        if len(repoList) <= 0:
            self.succeed("Verified Default Repository '%s' is Deleted :: Repositories %s"%(self.repositoryName, repoList))
        else:
            self.failure("Verified Default Repository '%s' is not Deleted :: Repositories %s"%(self.repositoryName, repoList), raiseExc=True)
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    