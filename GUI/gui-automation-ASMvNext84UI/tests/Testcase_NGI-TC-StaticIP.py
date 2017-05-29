'''
Author: Saikumar Kalyankrishnan
Created Date: 5/12/2016
Description: ASM-4359: Specify Manual Static IPs for Networking during Deployment
Test-Cases: NGI-TC-4405, NGI-TC-4406, *NGI-TC-4445, NGI-TC-4448, 
            NGI-TC-4449, NGI-TC-4450, NGI-TC-4451, NGI-TC-4452, 
            NGI-TC-4462, NGI-TC-4463, *NGI-TC-4464, NGI-TC-4465, 
            NGI-TC-4466, *NGI-TC-4467, *NGI-TC-4468, *NGI-TC-4488, *NGI-TC-4493 

Execution Steps: 1. Select the template
                 2. Update template with User Entered IPs in Deployment section
                 3. Run this test-case
                 
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
    Description: ASM-4359: Specify Manual Static IPs for Networking during Deployment
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
        fileName = globalVars.jsonMap["skjson"]        
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        
        #Populate globalVars with values from JSONs
        if (globalVars.staticIP == ""):
            globalVars.staticIP = self.components["Deployment"]["StaticIP"]
    
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
        
        self.serviceName = "Auto_Service_" + self.templateName
        
        self.deployService(self.templateName, serviceName=self.serviceName, staticIP=True)   
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #For Testing Purpose providing hard-coded Template Name, will have this value pull from JSON once separate Flow scripts are available
        self.templateName = "SK_ESXI_Auto"

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()