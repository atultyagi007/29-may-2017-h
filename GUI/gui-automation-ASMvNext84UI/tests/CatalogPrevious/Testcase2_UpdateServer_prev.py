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
        fileName = "SeptemberCatalogInfo.json" 
        self.catalogInfo = self.getTemplateConfiguration(fileName)
        if len(self.catalogInfo) > 0:
            self.succeed("Able to read Version Data :: '%s' -> Content :: %s"%(fileName, self.catalogInfo))
        else:
            self.failure("Failed to read Version Data :: '%s'"%(fileName), raiseExc=True)
        
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
        
        statusDetailList, readFromTime=self.updateResource("Servers", updateAll=True)
        resourceIdList=[]
        updateSuccessList=[]
        updateFailList=[]
        for device in statusDetailList:
            resourceIdList.append(device["ResourceTag"])
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
            
        versionValidatedList=[]
        for resource in updateSuccessResourceList:  
            versionValidatedResource=self.validateUpdatedFirmwareVersion("Servers", resource["model"], resource["ResourceIP"])
            if versionValidatedResource:
                versionValidatedList.append(resource.copy())
           
        if len(versionValidatedList)==0:
            self.failure("Version Validation Failed for All updated Resources", raiseExc=True)
        rediscoveredList=[]
        #Delete resource
        for resource in versionValidatedList:
            self.deleteResource(resource["ResourceIP"])
        #Re-discover Resource
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
              
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()