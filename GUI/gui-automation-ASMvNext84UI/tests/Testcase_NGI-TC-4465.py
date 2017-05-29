'''
Author: Saikumar Kalyankrishnan
Created Date: 5/12/2016
Description: ASM-4359: Specify Manual Static IPs for Networking during Deployment
Test-Case: NGI-TC-4465 ESXi with iSCSI EqualLogic deployment with static networks for HyperVisor Mgmt and ASM selected IPs for iSCSI

Execution Steps: 1. Update template JSON with User Entered IPs in Deployment section
                 2. Run this test-case
                 
Format for Static IP in JSON:
staticIP = {
            "Hypervisor Management":["172.31.37.xxx","172.31.37.yyy"], 
            "Hypervisor Cluster Private":["172.31.38.xxx"], 
            "SAN [iSCSI]":["172.31.39.xxx","172.31.39.yyy","172.31.39.zzz"],
            "Hypervisor Migration":[], 
            "Public LAN":["172.31.WL.xxx","172.31.WL.yyy"]
            }
            
Explanation:
Default = ASM Selected IP
Network = Static
Inputs are sorted by name i.e first IP in list will point to first Server/VM sorted alphabetically on 'Server Name'/'VM Name'
            "Hypervisor Management":[Server 1, Server 2, ... , Server N]  
            "Hypervisor Cluster Private":[Server 1, Server 2, ... , Server N], 
            "SAN [iSCSI]":[iSCSI39 Server1 IP1, iSCSI39 Server1 IP2, iSCSI39 Server2 IP1, iSCSI39 Server2 IP2, ... , iSCSI39 ServerN IP1, iSCSI39 ServerN IP2],    --> EqualLogic
            "SAN [iSCSI]":[iSCSI39 Server1 IP, iSCSI40 Server1 IP, iSCSI39 Server2 IP, iSCSI40 Server2 IP, ... , iSCSI39 ServerN IP, iSCSI40 ServerN IP],    --> Compellent
            "Hypervisor Migration":[Server 1, Server 2, ... , Server N], 
            "Public LAN":[VM 1,VM 2, ... , VM N] --> Only if Server has static Workload network selected    
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Description: ASM-4359: Specify Manual Static IPs for Networking during Deployment/NGI-TC-4465 ESXi with iSCSI EqualLogic deployment with static networks for HyperVisor Mgmt and ASM selected IPs for iSCSI
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
        
        #Read JSON
        fileName = globalVars.jsonMap["esxieqlstatic"]        
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        
        #Edit Template Name
        self.components["Template"]["Name"] = "Template_ESXI_EQL_StaticHVM_ASMiSCSI" 
            
        #Change Network Type
        self.components["Deployment"]["StaticIP"] = {
                    "Hypervisor Management": ["172.31.37.236"],
                    "Hypervisor Cluster Private": [],
                    "SAN [iSCSI]": [],
                    "Hypervisor Migration": ["172.31.36.236"],
                    "Public LAN": []
                }
        
        #Populate globalVars with values from JSONs
        if (globalVars.staticIP == ""):
            globalVars.staticIP = self.components["Deployment"]["StaticIP"]
            
        #Verify Resource Availability
        self.verifyResourceAvailability(self.components)
        
        #Delete existing Template with same name
        tempList = self.getTemplates(option="My Templates")
        self.templateName = self.components["Template"]["Name"]
        #for template in tempList:
        #    if self.templateName == template["Name"]:
        #        self.deleteTemplate(self.templateName)
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        
        #Setting globalVar back to null
        globalVars.staticIP = ""

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
        self.serviceName = "Service_" + self.templateName
        self.deployService(self.templateName, serviceName=self.serviceName, staticIP=True)
        
        #Waits for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            #Validation of Service
            self.checkForValidation(self.serviceName)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=True)  
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()