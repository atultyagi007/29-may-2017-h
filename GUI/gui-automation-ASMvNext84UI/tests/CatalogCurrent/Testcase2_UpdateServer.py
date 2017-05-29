'''
Created on Jan 24, 2016

@author: preetam.sethi
Description : Update Non-compliant resources and Validate update
Test Flow  :1)Login as a Admin user. 
            2)Update Non-compliant, not-in use , managed resources
            3)Verify update is successful.
prerequisites:Catalog file should be loaded.
'''
from tests.globalImports import *
import datetime
tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Update Non-compliant resources and Validate update
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
        fileName = "JanuaryCatalogInfo.json"  
        self.catalogInfo = self.getTemplateConfiguration(fileName)
        if len(self.catalogInfo) > 0:
            self.succeed("Able to read Version Data :: '%s' -> Content :: %s"%(fileName, self.catalogInfo))
        else:
            self.failure("Failed to read Version Data :: '%s'"%(fileName), raiseExc=True)
        #Read Components from JSON file for Template    
        templateFileName ="BAREMETAL.json"      
        self.components = self.getTemplateConfiguration(templateFileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(templateFileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(templateFileName), raiseExc=True)
        
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.logout()
        
    def runTestCase(self):        
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Update Non-Compliant Servers
        statusDetailList, readFromTime=self.updateResource("Servers", updateAll=True)
        resourceIdList=[]
        updateSuccessList=[]
        updateFailList=[]
        #List down Update Successful Resources
        for device in statusDetailList:
            resourceIdList.append(device["ResourceTag"])
        #Verify Update Logs
        resourceLogStatus=self.validateUpdateFirmwareLogs(resourceIdList, readFromTime)
        for resourceId, status in resourceLogStatus.items():
            if "Success" in status:
                updateSuccessList.append(resourceId)
                self.succeed(status+resourceId)
            else:
                updateFailList.append(resourceId)
                self.failure(status+resourceId)
        if len(updateFailList):
            for updateFailResource in updateFailList:
                self.failure("Firmware Update Failed for resources"+str(updateFailResource))
        else:
            self.succeed("Firmware Update Successful")
        updateSuccessResourceList=[resource for resource in statusDetailList if resource["ResourceTag"] in updateSuccessList]
        if len(updateSuccessResourceList)==0:
            self.failure("Resource Update failed", raiseExc=True)
        
        #Validate Updated Resource Component Versions  
        versionValidatedList=[]
        for resource in updateSuccessResourceList:  
            versionValidatedResource=self.validateUpdatedFirmwareVersion("Servers", resource["model"], resource["ResourceIP"])
            if versionValidatedResource:
                versionValidatedList.append(resource.copy())
           
        if len(versionValidatedList)==0:
            self.failure("Version Validation Failed for All updated Resources", raiseExc=True)
        rediscoveredList=[]
        #Delete resources
        for resource in versionValidatedList:
            self.deleteResource(resource["ResourceIP"])
        #Run Inventory
        try:
            chassisList=self.getResources("chassis")
            chassisTagList=[chassis["Asset/Service Tag"] for chassis in chassisList]
            self.runInventory(chassisTagList)
            self.waitJobComplete('Inventory')
            self.waitJobComplete('Discovery')
        except:
            self.failure("Unable to update inventory")
        #Re-discover Resources
        for resource in versionValidatedList:
            try:
                self.singleResourceDiscovery("Server",resource["ResourceIP"],"Managed","Dell PowerEdge iDRAC Default")
                rediscoveredList.append(resource.copy())
                self.succeed('Able to re-discover:: %s'%resource["ResourceIP"])
            except:
                self.failure('Unable to re-discover:: %s'%resource["ResourceIP"])
        if len(rediscoveredList)==0:
            self.failure("No Updated resource got rediscovered", raiseExc=True)
         
        rediscoveryCompValList=[]
        for resource in rediscoveredList:    
            complianceStatus=self.getResourceComplianceStatus(resource["ResourceIP"])
            if complianceStatus=="Compliant":
                rediscoveryCompValList.append(resource.copy())
                self.succeed("Re-discovery state compliant for "+resource['ResourceIP'])
            else:
                self.failure("Re-discovery state not compliant for "+resource["ResourceIP"])
        if len(rediscoveryCompValList)==0:
            self.failure("Resource Re-discovery compliance validation failed for all updated resources", raiseExc=True)
              
        #Delete existing Template with same name
        tempList = self.getTemplates(option="My Templates")
        self.templateName = self.components["Template"]["Name"]
        for template in tempList:
            if self.templateName == template["Name"]:
                self.deleteTemplate(self.templateName)
          
#         #Create Server Pool of updated server
        for resource in rediscoveryCompValList:
            utility.execLog('Adding server to server pool with IP %s'%resource['ResourceIP'])
            self.components["Server"]["ServersIPForPool"].append(resource['ResourceIP'])
        self.createServerPool(self.components)
        utility.execLog('Created server pool with servers %s'%self.components["Server"]["ServersIPForPool"])
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True)
#         #Deploy Service
#         #=======================================================================
        self.serviceName = "Service_" + self.templateName
        self.deployService(self.templateName, serviceName=self.serviceName)
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
            else:
                self.omit("Failed to Deploy Service '%s'"%self.serviceName)
        else:
            self.omit("Failed to Deploy Service '%s' in expected time"%self.serviceName)        
        #=======================================================================
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