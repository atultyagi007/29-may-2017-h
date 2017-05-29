'''
Created on Nov 25, 2016

@author: raj.patel
Description:On the "Summary" page, verify that all entries including mouseovers are correct.(cluster is yes, servers are unmanaged, and storage reports that attached servers are unmanaged
           Prerequisites:1:Vcenter of existing service must be discovered on Brown field appliance.
                        2:Server must be discovered and Storage of existing service don't be discovered on Brown field appliance.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Brownfield -cluster is yes, servers are unmanaged, and storage reports that attached servers are unmanaged
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
        for resourceIP in self.resourceIPList1:
            status, result = self.findResource(resourceIP)
            if not status:
                self.discoverResource_Type("Storage", resourceIP, resourceIP, "Managed","", "autoStorageEQL", "", "","", "")
        for resourceIP in self.resourceIPList2:
            status, result = self.findResource(resourceIP)
            if not status:
                self.discoverResource_Type("Chassis", resourceIP, resourceIP, "Managed","Dell PowerEdge iDRAC Default", "", "Dell chassis default", "","Dell switch default", "")
 
        self.changeResourceState(self.resourceIPList3)
             
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        self.changeResourceState(self.resourceIPList3,resourceState="Managed")
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        self.brownFieldServiceSchanario(self.brownFieldServiceName,self.brownFieldCompName,self.targetVirtualMachineServiceTag,self.dataCenterName,self.clusterName,storageDiscov=self.storageDiscov,serverDiscov=self.serverDiscov,serverState=self.serverState)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
#         List of Storage ip
        self.resourceIPList1 = ["172.31.32.108"]
#         List of Chassis ip
        self.resourceIPList2 = ["172.31.60.238"]
#         List of Server ip
        self.resourceIPList3 = ["172.31.60.88"]
        
        self.brownFieldServiceName = "BrownFieldService"
        self.brownFieldCompName = "BrownFieldClstr"
        self.targetVirtualMachineServiceTag = "VCENTER_55_UP3"
        self.dataCenterName = "HCLjio1HTs"
        self.clusterName = "HCLjio1CTS"
        self.serverDiscov = True
        self.storageDiscov = True
        self.serverState = True
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        