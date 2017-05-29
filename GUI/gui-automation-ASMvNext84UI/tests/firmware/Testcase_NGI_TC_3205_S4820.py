'''
Created on June 13, 2016

@author: preetam.sethi
Description : Create user bundle for S4820 Switch
Test Flow   :1)Login as a Admin user.  
             2)Verify S4820 servers are discovered or not.
             3)Create user bundle for S4820 switch.
             4)Compliance status of S4820 server shall change form Non-compliant to Compliant on adding bundles
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Create user bundle for switch S4820
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
        self.verifyResourceModelAvailblity("Switches","S4820")
        repoDetails=self.getRepositories()
        repoDetails=[repo for repo in repoDetails if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required"]
        if len(repoDetails)==0:
            self.omit("No Catalog in Available State")
        else:
            repoDetails=repoDetails[0]
            repoName=repoDetails['Repository Name']
        self.addSwitch_CustomBundles(option="S4820", catalogName=repoName)
        nonCompliant=False
        ComplianceStatusPostBundleAdd=self.getReourceModelComplainceStatus("Switches","S4820")
        for resourceIp,compliance in ComplianceStatusPostBundleAdd.items():
            if compliance=="Non-Compliant":
                nonCompliant= True
        if nonCompliant:
            self.updateResource('Switches', scheduleWait=False, updateAll=True)
            ComplianceStatus=self.getReourceModelComplainceStatus("Switches","S4820")
            for resourceIp,compliance in ComplianceStatus.items():
                if compliance=="Non-Compliant":
                    self.failure('Non-Compliant resource %s'%resourceIp, raiseExc=True)
            self.succeed('Switch Bundle add and update completed')
        else:
            self.succeed("Switch in compliant State")
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    