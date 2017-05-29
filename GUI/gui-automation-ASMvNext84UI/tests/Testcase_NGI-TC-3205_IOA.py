'''
Created on June 13, 2016

@author: preetam.sethi
Description : Create user bundle for IOA Switch
Test Flow   :Login as a Admin user and Create user bundle for IOA switch.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Create user bundle for IOA switch 
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
        self.verifyResourceModelAvailblity("Switches","I/O-Aggregator")
        ComplianceStatusPreBundleAdd=self.getReourceModelComplainceStatus("Switches","I/O-Aggregator")
        if ComplianceStatusPreBundleAdd=="Compliant":
            self.succeed("need not add bundle, switches already in compliance state")
        else:
            self.addSwitch_CustomBundles("I/O-Aggregator")
            time.sleep(10)
            ComplianceStatusPostBundleAdd=self.getReourceModelComplainceStatus("Switches","I/O-Aggregator")
            if ComplianceStatusPostBundleAdd==ComplianceStatusPreBundleAdd:
                self.failure("Switch compliance status remains non-compliant after adding bundle",raiseExc= True)
            else:
                self.succeed("Switch compliance status changes from non-compliant to compliant state after adding buddle")
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    