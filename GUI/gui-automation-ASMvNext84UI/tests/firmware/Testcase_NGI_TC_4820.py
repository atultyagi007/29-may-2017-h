'''
Created on Feb 21, 2017

@author: preetam.sethi
Description :  Verify a FW repo from FTP site can be uploaded

Test Flow  :1)Add a catalog selecting the FTP option. Verify the  connectivity.
            2)The FTP catalog uploads successfully. 
            3)The catalog is good and can be selected as default catalog.
 
prerequisites:Firmware catlog should be there.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify a FW repo from FTP site can be uploaded
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
        repoListPreAdd=self.getRepositories()
        repoListPreAdd=[repo["Repository Name"] for repo in repoListPreAdd]
        self.addFirmwareRepository(option="networkPath",networkPath=globalVars.catlogRepository["ftpSharePath"],testConnection= False,waitAvailable= False)
        repoListPostAdd=self.getRepositories()
        repoListPostAdd=[repo["Repository Name"] for repo in repoListPostAdd]
        repoName= [repo for repo in repoListPostAdd if repo not in repoListPreAdd]
        repoName=repoName[0]
        self.setDefaultRepository(repositoryName=repoName)
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()