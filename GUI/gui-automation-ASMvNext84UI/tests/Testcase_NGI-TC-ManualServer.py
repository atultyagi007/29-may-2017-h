'''
Author: Saikumar Kalyankrishnan
Created Date: 6/5/2016
Description: Manual Selection of Servers for Deployment
Test-Cases: NGI-TC-3216, NGI-TC-3221, NGI-TC-3233, NGI-TC-3234

Execution Steps: 1. Select the template
                 2. Update template with User Entered IPs in Deployment section
                 3. Run this test-case
                 
Format for Static IP in JSON:
manualServer = [server1, server2, ... , serverN]
            
Explanation:
Default = Server Pool
if manual entry is less than no. of servers, first N number of servers will be manually selected and remaining will be default. 
   
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Description: Manual selection of Servers for Deployment
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
        if (globalVars.manualServer == ""):
            globalVars.manualServer = self.components["Deployment"]["ManualServer"]
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        
        #Setting globalVar back to null
        globalVars.manualServer = ""

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