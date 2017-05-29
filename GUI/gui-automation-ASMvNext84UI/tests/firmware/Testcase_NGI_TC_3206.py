'''
Created on May 17, 2016

@author: preetam.sethi
Description : Downloading catalog select the default checkbox and verify if its set to default
                                             after the catalog is downloaded.
            Merged Testcases : NGI_TC_3194 : upload a catalog from network path--cifs,
                               NGI_TC_3984 : download a catalog, verify that it is showing completed in repositories page
Test Flow  :1)Login as a Admin user and 
            2)Download catalog from firmware tab under repository and click on default option
            3)when download is done verify that the catalog uploaded is set as default

prerequisites:Firmware catlog should be there.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    upload a catalog from network path--cifs, select it as default catlog while uploading and verify after upload.
    Merged Testcases: NGI-TC-3206 
                      NGI-TC-3984
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
        self.addFirmwareRepository(option='networkPath', networkPath=globalVars.catlogRepository["cifsPath"], makeDefault=True)
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