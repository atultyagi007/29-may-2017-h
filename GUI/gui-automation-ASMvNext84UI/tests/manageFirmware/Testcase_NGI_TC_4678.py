'''
Author: Preetam.sethi
Created Date: sept 07, 2016
Description: Deploy now with firmware update but fail the firmware update
Deployment status  Deployment with FW update Now    Updating service    Updating service    Deployment Failed   
Firmware status    Not in use                       Non-compliant       Updating            update Failed    
'''
from tests.globalImports import *
from libs.product.pages import Repositories
tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Deploy now with firmware update but fail the firmware update
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
        json_file="NGI_TC_4678.json"     
#         fileName = globalVars.jsonMap["esxieqliscsi"]        
        self.components = self.getTemplateConfiguration(json_file)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(json_file, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(json_file), raiseExc=True)
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
        #Get default repository:
        defaultRepository= self.getDefaultRepository()
        #Get non-compliant not-in-use server
        serverIP= self.getServerIp(compliance="Non-Compliant", deploymentState="Not in use")
        try:
            self.components["Server"]["ServersIPForPool"][0]=serverIP
        except Exception as e:
            print e
        #Create Server Pool
        instances = self.components["Server"]["Instances"]
        if instances > 0:
            self.createServerPool(self.components)
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True)
        #Deploy Service with Firmware update
        #=======================================================================
        self.serviceName = "Service_" + self.templateName
        
        self.deployService(self.templateName, serviceName=self.serviceName, repositoryName=defaultRepository, scheduleDeploymentWait=5, manageFirmware= True, retryOnFailure=self.retryOnFailure)
        time.sleep(10)
        self.verifyDeploymentFirmwareUpdate(serverIP)   
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()