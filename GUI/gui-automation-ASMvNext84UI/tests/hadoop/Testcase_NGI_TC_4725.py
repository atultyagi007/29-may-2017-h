'''
Author: Bruce Burden
Created Date: Sept. 29, 2016
Description: Install OS on the first VD, using &#39;First Disks&#39;, selecting &#39;Exactly&#39; # of disks required, with disks populated starting at Slot 1.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Server migration with multiple servers in a deployment
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
#         fileName = globalVars.jsonMap["bareMetalexsi"]
        fileName = "BAREMETAL_LINUX_4725.json"    
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
            result = self.verifyServerPool(self.components)
            if not result:
                self.createServerPool(self.components)
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True)
        #Deploy Service
        #=======================================================================
        self.serviceName = "Service_" + self.templateName
        self.deployService(self.templateName, serviceName=self.serviceName, retryOnFailure=self.retryOnFailure)
        #Wait for Deployment to complete
        """
        ASM-8050 causes puppet to run every 5 minutes during a deployment.
        If the networks are being configured, this can lead to an error
        for the server health status, saying "Could not connect to host".
        I put this loop in to handle that condition.
        """
        attempt = 5
        while attempt:
            result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
            if result:
                if result[0]["Status"] in ("Success"):
                    self.succeed("Successfully Deployed Service %s".format(self.serviceName))
                    break
                else:
                    self.failure("Service %s not showing Success, retrying Result: %s".format(self.serviceName, result))
                    attempt -= 1
            else:
                self.failure("Failed to Deploy Service %s in expected time".format(self.serviceName), raiseExc=True)

            if not attempt:
                self.failure("Failed to Deploy Service %s".format(self.serviceName), raiseExc=True)

        """
        Running Test Case: Validation of Service
        """
        self.checkForValidation(self.serviceName)
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()