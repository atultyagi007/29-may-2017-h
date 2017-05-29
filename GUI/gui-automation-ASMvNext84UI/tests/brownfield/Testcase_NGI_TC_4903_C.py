'''
Created on Nov 25, 2016

@author: raj.patel
Description:On the "Summary" page, verify that all entries including mouseovers are correct.(cluster is yes servers not in inventory, and storage reports not managed and related server not in inventory.
           Prerequisites:1:Vcenter of existing service must be discovered on Brown field appliance.
                        2:Storage must be discovered and server of existing service don't be discovered on Brown field appliance.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Brownfield -cluster is yes servers not in inventory, and storage reports not managed and related server not in inventory
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        for resourceIP in self.resourceIPList:
            status, result = self.findResource(resourceIP)
            if not status:
                self.discoverResource_Type("Storage", resourceIP, resourceIP, "Managed","", "autoStorageEQL", "", "","", "")

        self.changeResourceState(self.resourceIPList)
             
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        self.changeResourceState(self.resourceIPList,resourceState="Managed")
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        self.brownFieldServiceSchanario(self.brownFieldServiceName,self.brownFieldCompName,self.targetVirtualMachineServiceTag,self.dataCenterName,self.clusterName,storageDiscov=self.storageDiscov)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.resourceIPList = ["172.31.32.108"]
        self.brownFieldServiceName = "BrownFieldService"
        self.brownFieldCompName = "BrownFieldClstr"
        self.targetVirtualMachineServiceTag = "VCENTER_55_UP3"
        self.dataCenterName = "HCLjio1HTs"
        self.clusterName = "HCLjio1CTS"
        self.storageDiscov = True
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        