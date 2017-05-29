'''
Created on Nov 28, 2016

@author: rajeev.kumar
Description:VSAN deployment(Hybrid) with VDS enabled using SATADOM as  boot device
           merged testcsaes:
           NGI-TC-N/A--Teardown a VSAN deployment with VDS enabled. 
           NGI-TC-4816--Server and VM scale up on a VSAN VDS deployment
           NGI-TC-4817--Scale up a static Network on VDS VSAN deployement (Host and VM level)
           
Test Flow:  1.Create a VDS VSAN template
            2.Kick off deployment from VSAN tempalte.,
            3.Deployment is sucesfull
            4.Scaleup service with server, VM and Network
            5.Validate Deployment
            6.Teardown Deployment
Prerequisites:In Appliance 2-VSAN(LAN-1008/108 & 208) N/W should be there.    
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    VSAN VDS deployment on R630's with VSAN ready nodes.
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
        #Read Components from JSON file     
        fileName = "VSAN_hybrid_SATADOM_4814.json"      
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        #Verify Resource Availability
#         self.verifyResourceAvailability(self.components)
        #Delete existing Template with same name
        tempList = self.getTemplates(option="My Templates")
        self.templateName = self.components["Template"]["Name"]
        for template in tempList:
            if self.templateName == template["Name"]:
                self.deleteTemplate(self.templateName)
        self.retryOnFailure=self.components["Template"]["retryOnFailure"]
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Create Server Pool
        instances = self.components["Server"]["Instances"]
        if instances > 0:
            self.createServerPool(self.components)
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True)
        #Deploy Service
        #=======================================================================
        self.serviceName = "Service_" + self.templateName
        self.deployService(self.templateName, serviceName=self.serviceName, retryOnFailure=self.retryOnFailure)
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
#         if len(result) > 0:
#             if result[0]["Status"] in ("Success"):
#                 self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
#             else:
#                 self.failure("Failed to Deploy Service '%s'"%self.serviceName, raiseExc=True)
#         else:
#             self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=True)        
        #=======================================================================
        self.scaleUpService(self.serviceName, self.components)
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()