'''
Created on May 17, 2016

@author: preetam.sethi
Description : download a catalog, verify that it is showing copying in repositories page
            
Test Flow  :1)Login as a Admin user and Upload supported Cab file, no error shown
            2)Go to repositories page click on firmware and download a catalog
            3)Catalog shd be in copying state 
prerequisites: catalog file should be present in location.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    download a catalog, verify that it is showing copying in repositories page
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
        self.logout()
        
    def runTestCase(self):        
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        repoListPrevious=self.getRepositories()
        repoListPreviousName=[repo['Repository Name'] for repo in repoListPrevious]
        self.addFirmwareRepository(option="networkPath",networkPath=globalVars.catlogRepository["nfsPathXml"],makeDefault=False,testConnection=False, waitAvailable=False)
        repoListCurrent=self.getRepositories()
        repoListCurrentName=[repo['Repository Name'] for repo in repoListCurrent]
         
        catalogAdded= [repo for repo in repoListCurrentName if repo not in repoListPreviousName]
        catalogAdded=catalogAdded[0]
        catalogStatus=[repo['State'] for repo in repoListCurrent if repo['Repository Name']==catalogAdded]
        catalogStatus=catalogStatus[0]
        if catalogStatus=='Copying':
            self.succeed('Catalog Download initiated Catalog:: %s State:: Copying '%catalogAdded)
        else:
            self.failure('Catalog Download Not initiated', raiseExc=True)
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()