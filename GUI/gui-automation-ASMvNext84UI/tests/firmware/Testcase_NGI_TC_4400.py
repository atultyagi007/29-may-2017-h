'''
Created on June 13, 2016

@author: preetam.sethi
Description : Validate FN IOA v9.9 on ASM 8.2
Test Flow   :1)Login as a Admin user.  
             2)Verify FX2 servers are discovered or not.
             3)Flash IOA firmware to 9.9
             4)Verify IOAs are in PMUX mode.
             5)Run deployment -- ESXi / Equallogic / iSCSI
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Validate FN IOA v9.9 on ASM 8.2
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
#         self.verifyResourceModelAvailblity("Switches","I/O-Aggregator")
        compliant= False
        globalVars.switchBundleRepositoy["I/O-Aggregator"]="\\10.255.7.219\SELab\LAB\Firmware\Force10\FNIOA\FTOS-FN-9.9.0.0P9.bin"
        repoDetails=self.getRepositories()
        repoDetails=[repo for repo in repoDetails if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required"]
        if len(repoDetails)==0:
            self.omit("No Catalog in Available State")
        else:
            repoDetails=repoDetails[0]
            repoName=repoDetails['Repository Name']
        self.addSwitch_CustomBundles(option="I/O-Aggregator", catalogName=repoName)
        self.updateResource(resourceType='Switches', scheduleWait=None, updateAll=True)
        ComplianceStatusPostBundleAdd=self.getReourceModelComplainceStatus("Switches","I/O-Aggregator")
        for resourceIp,compliance in ComplianceStatusPostBundleAdd.items():
            if compliance=="Compliant":
                compliant= True
        if compliant:
            compliantSwitchIp= ComplianceStatusPostBundleAdd.keys()[ComplianceStatusPostBundleAdd.values().index("Compliant")]
#         
        bootMode= self.getSwitchBootMode(compliantSwitchIp)
        if "programmable-mux" in bootMode:
            self.succeed("Boot Mode of IOA switch is set to PMUX")
        else:
            self.failure("Boot Mode of IOA switch is not set to PMUX")
        
        

    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    