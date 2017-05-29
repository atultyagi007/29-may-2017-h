'''
Author: Atul.kumar
Created Date: Oct 1, 2016
Description: Define uplink group with port-channel number and all network settings (hypervisor, PXE, FCoE/iSCSI, workload, migration)  
contain test case 3005
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Define uplink group with port-channel number and all network settings (hypervisor, PXE, FCoE/iSCSI, workload, migration)
     contain test case 3005
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
        self.logDesc("Running Test Case")        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)   
        #Chassis configuration
        #chassisConfigFile= "chassisConfig_4491.json"
        #self.chassisComponent= self.getTemplateConfiguration(chassisConfigFile)
        
        
        
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
        chessIp="172.31.61.196"
        uplinkName="HCLUplink"
        portChannel=128
        self.configUpLinkOnly(chessIp,uplinkName,portChannel)
        
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()