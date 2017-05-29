'''
Created on May 17, 2016

@author: preetam.sethi
Description : Upload supported file, no error shown 
            : Merged Testcases NGI_TC_3210: Downloading catalog dont select Default catalog and verify
                                NGI_TC_4486: Add a firmware catalog to the ASM repository
                                NGI_TC_3986: download a catalog, verify that it is showing error in repositories page
Test Flow  :1)Login as a Admin user. 
            2)download supported Cab file, no error shown.
            3)Verify catlog file is not set as default.
            4)If error occurred verify Error state shown.
prerequisites:Catalog file should be present in location.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Upload supported file, no error shown; 
    Merged Testcases  NGI-TC-3210, NGI_TC_4486, NGI_TC_3986 
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
        self.addFirmwareRepository(option="networkPath",networkPath=globalVars.catlogRepository["nfsPathCab"],makeDefault=True,testConnection=False)
        repoListCurrent=self.getRepositories()
        repoListCurrentName=[repo['Repository Name'] for repo in repoListCurrent]
         
        catalogAdded= [repo for repo in repoListCurrentName if repo not in repoListPreviousName]
        catalogAdded=catalogAdded[0]
        self.verifyDefaultFirmware(catalogAdded)
       
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()