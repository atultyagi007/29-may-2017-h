'''
Created on Feb 19, 2015

@author: ankit.manglic
Description: iS -  CO - Div - End to end deployment with VDS enabled for VMWare cluster with storage, ESXi host, VMs and application.
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
    iS -  CO - Div - End to end deployment with VDS enabled for VMWare cluster with storage, ESXi host, VMs and application.
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
#         self.logDesc("Running Test Case")
#         self.logDesc("Creating template %s"%str(self.templateName))
#         self.executeCompleteFlow(self.templateName, self.storageType, self.storageName, self.volumeName, targetStorage="200358", flowType="diverge")
#         # Deploys a Service and deploy now
#         self.logDesc("Deploying service %s with template %s"%(str(self.serviceName), str(self.templateName)))
#         self.deployService(self.templateName, self.serviceName)
#         # Verify If service deployed successfully
#         self.logDesc("Verify service %s deployment"%str(self.serviceName))
#         serviceDeployment = self.verifyServiceDeploymentStatus(self.serviceName)
#         # Verify the parameters afters successful deployment
#         self.logDesc("Verifying parameters after service %s is deployed"%str(self.serviceName))
#         self.verifyvCenterDetailsOfDeployedService(self.serviceName, serviceDeployment)
        self.getVcenterDetailsMng("172.31.61.30")   
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
#         currentTime = datetime.now().strftime('%y%m%d%H%M%S') 
# 
#         self.templateName= "Exsi_Div_CO_"+currentTime
#         self.storageType = "Compellent"
#         self.storageName = "HCLTestStorage"+currentTime
#         self.volumeName = "HCLTestVolume"+currentTime
#         self.serviceName = "S_Exsi_Div_CO_"+currentTime
#         
#         self.browserObject = globalVars.browserObject
#  
#         self.preRunSetup()
         
        self.runTestCase()
          
#         self.postRunCleanup()
        