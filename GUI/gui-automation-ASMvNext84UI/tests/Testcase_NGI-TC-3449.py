'''
Created on Feb 22, 2015

@author: raj.patel
Description: FC - End to end deployment with VDS enabled for VMWare cluster with compellent stoage, ESXi host, VMs and application
Test Flow    : 1) Login as admin.
               2) Navigate to Template page and create a template.
               3) FC - End to end deployment with VDS enabled for VMWare cluster with compellent storage.
               
Pre-requisite : Server pool serTest with server 172.31.61.111
               
'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    FC - End to end deployment with VDS enabled for VMWare cluster with compellent stoage
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
        self.executeCompleteFlowFCESXI(self.templateName, self.storageType, self.storageName, self.volumeName)
        #Deploys a Service and deploy now
        self.deployService(self.templateName, self.serviceName)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S') 
        
        self.templateName= "TestTemplate_"+currentTime
        self.storageType = "Compellent"
        self.storageName = "TestStorage_"+currentTime
        self.volumeName = "TestVolume_"+currentTime
        self.serviceName = "TestService_"+currentTime
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        