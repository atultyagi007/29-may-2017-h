'''
Author: P.Suman
Created Date: Jan 5, 2016
Description: Default catalog can be deleted

Modified Date: Dec 7, 2016
@author: preetam.sethi   
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Default catalog cannot be deleted
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
        repolistPrev=self.getRepositories()
        repoListPrev=[repo['Repository Name'] for repo in repolistPrev]
        #Get Default Repository 
        defaultRepo = self.getDefaultRepository()
        if defaultRepo=='Select':
            repoList= self.getRepositories()
            repoToSetDefault=[repo['Repository Name'] for repo in repoList if repo['Source']!='Embedded' and 'Error' not in repo['State']]
            if len(repoToSetDefault):
                self.setDefaultRepository(repoToSetDefault[0])
                defaultRepo=repoToSetDefault[0]
            else:
                self.addFirmwareRepository(option='networkPath', networkPath=globalVars.catlogRepository["nfsPathXml"], makeDefault=True)
                repolistCurrent=self.getRepositories()
                repolistCurrent=[repo['Repository Name'] for repo in repolistCurrent]
                repoName=[repoName for repoName in repolistCurrent if repoName not in repoListPrev]
                repoName=repoName[0]
                if repoName!=self.getDefaultRepository():
                    self.failure('Default Repository not set to %s download'%repoName, raiseExc=True)
                else:
                    defaultRepo=repoName
        #Verify Delete Repo disabled for default repository
        try:
            self.deleteRepository(repositoryName=defaultRepo)
            self.failure('Able to delete default repository', raiseExc=True)
        except:
            self.succeed('Unable to delete default repository')
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    