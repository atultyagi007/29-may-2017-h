'''
Author: Saikumar Kalyankrishnan
Created Date: 6/14/2016
Description: Multiple Service Deployments
Test-Cases: NGI-TC-2913, NGI-TC-2916, NGI-TC-2918, NGI-TC-2919, NGI-TC-2920, NGI-TC-2922, NGI-TC-2923, NGI-TC-2924, NGI-TC-3486

Execution Steps: 1. Select the template
                 2. Update template with no. of deployments in Deployment section
                 3. Run this test-case
                 
Format for Static IP in JSON:
e.g.: "NumberOfDeployments":"2"
            
Explanation:
Default --> NumberOfDeployments:1
   
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Description: Multiple Service Deployments
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
        if (self.components["Deployment"]["NumberOfDeployments"] != "" or self.components["Deployment"]["NumberOfDeployments"] != 1):
            globalVars.noOfDeployments = self.components["Deployment"]["NumberOfDeployments"]
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        
        #Setting globalVar back to null
        globalVars.noOfDeployments = ""

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        self.serviceName = "Auto_Service_" + self.templateName
        
        self.deployService(self.templateName, serviceName=self.serviceName, manualServer=True)   
    
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