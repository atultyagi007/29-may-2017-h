'''
Created on Feb 9, 2016

@author: rajeev.kumar
Description :Edit bundle should show the file thats already exists, and should allow to
add new file.
Test Flow  :1)Login as a Admin user and show the exists file and allow add new file.
prerequisites:Firmware catloge should be there.


Modified on Dec 7, 2016
@author: preetam.sethi
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Edit bundle should show the file thats already exists, and should allow to Add new
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
        #Reverting back to original bundle path
        self.deleteCustomBundle(self.catalogName, 'S4810')
        self.logout()
            
    def runTestCase(self):        
        """
        Running Test Case
        """
        self.browserObject = globalVars.browserObject
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        repoList = self.getRepositories(repoType="Firmware")
        catalogFound=False
        for repo in repoList:
            if repo["Repository Name"]!="ASM Minimum Required" and repo["State"]=="Available":
                self.catalogName=repo["Repository Name"]
                catalogFound=True
                break
        if not catalogFound:
            self.omit("No Catalog found in Available State")
            
        self.bundleAdded=self.addSwitch_CustomBundles("S4810",self.catalogName)
        self.verifyCustomBundlePath(catalogName=self.catalogName, bundleName=self.bundleAdded)
        newBundlePath=globalVars.switchBundleRepositoy["MXL-10/40GbE"]
        self.editCustomBundlePath(catlogName=self.catalogName, bundleName=self.bundleAdded, newBundlePath=newBundlePath)
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()