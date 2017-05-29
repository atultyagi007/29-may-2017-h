'''
Author: preetam.sethi
Created Date: Jul 12, 2016
Description: Update firmware and software from deployments page and services page on VSAn ready nodes ( R730 XD, FC 430, R630)
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Update firmware and software from deployments page and services page on VSAn ready nodes
    Merged Testcase_NGI_TC_4742
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
        fileName = "VSAN_Clone.json"      
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
        self.deployService(self.templateName, serviceName=self.serviceName)
        # Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'"%self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=True)
        #getting firmware package list before adding firmware
        firmwareList1=self.getFirmwareList()
        self.addFirmwareRepository(option="networkPath", networkPath= globalVars.catlogRepository["cifsPath"])
        #getting firmware package List after adding firmware
        firmwareList2=self.getFirmwareList()
        for firmwarePackage in firmwareList2:
            if firmwarePackage not in firmwareList1:
                firmwarePackageAdded= firmwarePackage
        
        self.changeServiceRepository(self.serviceName, firmwarePackageAdded)
        self.updateResourceFirmware(self.serviceName)
        
        #Wait for update to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully update Service '%s' Resources"%self.serviceName)
            else:
                self.failure("Failed to update Service '%s' Resources"%self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to update Service '%s' Resources in expected time"%self.serviceName, raiseExc=True)  
            
        resourceCompliance=self.getReourceModelComplainceStatus("Servers"," PowerEdge R630")
        nonCompliant= False
        for server in self.components["Server"]["ServersIPForPool"]:
            if resourceCompliance[server]=="Non-Compliant":
                nonCompliant= True
                break
        if nonCompliant:
            self.failure("Server %s remains non-compliant after firmware update from service page"%server, raiseExc= True)
        else:
            self.succeed("Update Resources successful from service page")
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()