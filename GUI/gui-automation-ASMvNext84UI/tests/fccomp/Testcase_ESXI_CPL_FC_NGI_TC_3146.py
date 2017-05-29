'''
Author: Dheeraj.Singh
Created Date: Jun 03, 2016
Description: FC - End to end deployment with DRS enable compellent stoage, ESXi host, VMWare cluster, VMs and application
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    FC - End to end deployment with compellent stoage, ESXi host, VMWare cluster, VMs and application
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
        json_file="ESXI_CPL_FC_3146.json"     
#         fileName = globalVars.jsonMap["esxieqliscsi"]        
        self.components = self.getTemplateConfiguration(json_file)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(json_file, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(json_file), raiseExc=True)
        #Verify Resource Availability
        self.verifyResourceAvailability(self.components)
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
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'"%self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=True)        
        
        self.scaleUpService(self.serviceName, self.components)
        
        # Scale down service
        
        self.scaleDownService(self.serviceName, self.components)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()