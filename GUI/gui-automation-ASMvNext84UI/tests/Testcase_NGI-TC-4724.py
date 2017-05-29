'''
Author: Saikumar K
Created Date: July 20, 2016
Description: ASM-6618/7001: Support to Set Multiple NTP Servers on OS - SLES

Steps:
1. Edit the OS Image Value as per the appliance
2. Enter the set of comma separated NTP Servers in the config.ini; if left blank default set of NTP Servers would be used only for this testcase.
Default NTP Servers: 0.centos.pool.ntp.org, 0.pool.ntp.org, 0.us.pool.ntp.org, 129.6.15.30, 98.175.203.200, 198.111.152.100, 131.107.13.100
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    ASM-6618/7001: Support to Set Multiple NTP Servers on OS - SLES
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)   
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the Test scenario 
        """
        self.logDesc("Pre-Run Setup")   
             
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)   
        
        #Check for NTP Settings
        self.checkNTPSettings()
        
        #Read Components from JSON file     
        fileName = globalVars.jsonMap["baremetalLINUX"]        
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
            
        #Change Template Name
        self.components["Template"]["Name"] = "Template_BAREMETAL_SLES_MultipleNTP"
        
        #Change OS Image Value to ESXI Version
        self.components["Server"]["OSImage_Value"] = ["SLES"]
        
        #Check for Multiple NTP
        if (globalVars.configInfo['Appliance']['multiplentp'] == ""):
            globalVars.configInfo['Appliance']['multiplentp'] = "0.centos.pool.ntp.org, 0.pool.ntp.org, 0.us.pool.ntp.org, 129.6.15.30, 98.175.203.200, 198.111.152.100, 131.107.13.100"
    
        #Verify Resource Availability
        self.verifyResourceAvailability(self.components)
        
        #Check for existing Template Name. If exists, rename Template as Template_2. No deletion of Template
        self.templateName = self.checkTemplateName(self.components["Template"]["Name"])
        #Setting/Renaming components - Template Name
        self.components["Template"]["Name"] = self.templateName
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        
        #Clear config.ini
        globalVars.configInfo['Appliance']['multiplentp'] == ""

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
        self.deployService(self.templateName, serviceName=self.serviceName)
        
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
               
                #Validation of Deployment/Test-Case
                #Basic BareMetal Deployment Validation - In Progress
                
                #Post OS NIC Validation - Unsure if required
                #self.checkBaremetalPostNIC(self.serviceName)
                
                #Multiple NTP Verification for BareMetal
                self.verifyMultipleNTP(self.serviceName)
                
            else:
                self.failure("Failed to Deploy Service '%s'"%self.serviceName, raiseExc=True)
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