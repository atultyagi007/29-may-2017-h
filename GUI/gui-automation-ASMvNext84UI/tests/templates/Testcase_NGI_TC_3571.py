'''
Author: Atul
Created Date: Dec 6, 2016
Description:Validate Server- Template with 1 server with 1 2-port NIC and RAID
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Validate Server- Template with 1 server with 1 2-port NIC and RAID
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
        fileName = "BAREMETAL_LINUX_3571.json"        
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        #Verify Resource Availability
#         self.verifyResourceAvailability(self.components)
#        Delete existing server Pool with same name
        
        serverPools=self.getServerPools()
        for serverPool in serverPools:
            if serverPool["Server Pool Name"]==self.components["Server"]["ServerPoolName"]:
                self.deleteServerPool(self.components["Server"]["ServerPoolName"])
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
        self.deleteServerPool(self.components["Server"]["ServerPoolName"])

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        self.createServerPool(components=None, userList=["admin"], self.components["Server"]["ServerPoolName"], serverList=[], 
                         addAllServers=True, addFirstHealthyServer=False)
        
        self.serviceName = "Service_" + self.templateName
        validatePoolAndManualServers=True
        if validatePoolAndManualServers:       
            inValidServerList=self.buildTemplate(self.components, publishTemplate=True,validatePoolAndManualServers=validatePoolAndManualServers)
            self.deployService(self.templateName, serviceName=self.serviceName, retryOnFailure=self.retryOnFailure,checkinValidServerList=inValidServerList)
        else:
            self.buildTemplate(self.components, publishTemplate=True)
            self.deployService(self.templateName, serviceName=self.serviceName, retryOnFailure=self.retryOnFailure)
      
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()