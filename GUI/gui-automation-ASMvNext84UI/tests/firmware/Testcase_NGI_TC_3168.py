'''
Created on June 8, 2016

@author: preetam.sethi
Description : Create user bundle for Compellent
Test Flow   :Login as a Admin user and Create user bundle for Compellent.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Create user bundle for compellent
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
        repoName=self.getDefaultRepository()
        if repoName=='Select':
            repoDetails=self.getRepositories()
            repoDetails=[repo for repo in repoDetails if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required"]
            if len(repoDetails)==0:
                self.omit("No Catalog in Available State")
            else:
                repoDetails=repoDetails[0]
                repoName=repoDetails['Repository Name']
                self.setDefaultFirmware(repoName)
        self.addStorageCustomBunddle("Compellent","Dell SC4020",globalVars.storageBundleVersion["Dell SC4020"])
        self.addStorageCustomBunddle("Compellent","Dell SC8000",globalVars.storageBundleVersion["Dell SC8000"])
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    