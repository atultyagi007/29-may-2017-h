'''
Author: P.Suman
Created Date: Jan 5, 2016
Description: Downloading catalog dont select the default checkbox and verify if its not set 
            to default after the catalog is downloaded        
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Downloading catalog dont select the default checkbox and verify if its not set 
    to default after the catalog is downloaded
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
        #Delete Previous Repositories
        repoList = self.getRepositories(repoType="Firmware")
        for repo in repoList:
            if repo["Repository Name"] != "ASM Minimum Required":
                try:
                    self.deleteRepository(repo["Repository Name"])
                except:
                    utility.execLog('Unable to delete catalog %s'%repo["Repository Name"])
                    
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
        #Get Existing Repositories  
        repoList = self.getRepositories(repoType="Firmware")
        self.previousList = [repo["Repository Name"] for repo in repoList]
        #Add Repository
        self.addFirmwareRepository(option="networkPath",networkPath=globalVars.catlogRepository["cifsPath"],makeDefault=False)
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
        #Get Repository Status
        status = self.getRepositoryStatus(self.repositoryName, repoType="Firmware")
        if status[0]["State"] != "Available":
            self.failure("Failed to Add Repository :: Status '%s'"%status, raiseExc=True)
        #Get Default Repository
        repoName = self.getDefaultRepository()
        if repoName == self.repositoryName:
            self.failure("Downloaded Catalog is set as Default Repository without selecting 'Make Default' Checkbox :: Actual :%s Expected:%s"%(repoName, self.repositoryName),
                            raiseExc=True)
        else:
            self.succeed("Downloaded Catalog is not set as Default Repository without selecting 'Make Default' Checkbox :: Actual :%s Expected:%s"%(repoName, self.repositoryName))
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    