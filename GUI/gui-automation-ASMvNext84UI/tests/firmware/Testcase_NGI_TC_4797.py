'''
Created on Feb 21, 2017

@author: preetam.sethi
Description : Verify compliance and report information of a resource in use with no catalog attached to the service then adding one

Test Flow   :   Verify compliance and report information of a resource in use with no catalog attached to the service then adding one


'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
class Testcase(Manager.Manager): 
    """
    Verify compliance and report information of a resource in use with no catalog attached to the service then adding one
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
        fileName = "BAREMETAL_LINUX.json"        
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
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
        self.logout()
            
    def runTestCase(self):        
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        repoList=self.getRepositories()
        repoNameList=[repo['Repository Name'] for repo in repoList if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required"]
        if len(repoNameList)==0:
            self.omit("Not enough catalogs available for the test")
        repoName=repoNameList[0]
        #Create Server Pool
#        instances = self.components["Server"]["Instances"]
#        if instances > 0:
#            self.createServerPool(self.components)
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
                self.failure("Failed to Deploy Service '%s'"%self.serviceName)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=False)
        #Verify server compliance 
        complianceStatus=self.getResourceComplianceStatus(self.components["Server"]["ServersIPForPool"][0])
        if complianceStatus=='Unknown':
            self.succeed('Server Status is : Unknown')
        else:
            self.failure('Server Compliance status is not Unknown')
        
        #change service firmware
        self.editService(serviceName=self.serviceName, managePermissions=False,manageFirmware= True,fimrwareName= repoName)
        complianceStatus=self.getResourceComplianceStatus(self.components["Server"]["ServersIPForPool"][0])
        if complianceStatus in ('Compliant', 'Non-Compliant'):
            self.succeed("Server compliance status is correct")
        else:
            self.failure('Server Compliance status is incorrect')
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    