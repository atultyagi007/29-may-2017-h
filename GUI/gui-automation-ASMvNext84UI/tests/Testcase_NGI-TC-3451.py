'''
Created on Feb 19, 2015

@author: ankit.manglic
Description: FCoE - s5k - Bro - End to end deployment with compellent stoage, ESXi host, VMWare cluster with VDS
Test Flow    : 1) Login as admin.
               2) Navigate to Users page and create a new directory.
               3) Try to import the group and verify exception is generated for the same.
               
Pre-requisite : A group with users and a user should be created.
               
'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    FCoE - s5k - Bro - End to end deployment with compellent stoage, ESXi host, VMWare cluster with VDS
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
      
      
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        #logout of application
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.executeCompleteFlow(self.templateName, self.storageType, self.storageName, self.volumeName, targetStorage="200358", portType="FibreChannel", flowType="converge", fcoe=True)
        #Deploys a Service and deploy now
        self.deployService(self.templateName, self.serviceName)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S') 
        
        self.templateName= "Fcoe_Con_CO_"+currentTime
        self.storageType = "Compellent"
        self.storageName = "HCLTestStorage"+currentTime
        self.volumeName = "HCLTestVolume"+currentTime
        self.serviceName = "S_Exsi_Con_CO_"+currentTime
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        