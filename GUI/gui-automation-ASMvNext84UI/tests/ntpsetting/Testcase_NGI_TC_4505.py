'''
Author: preetam.sethi
Created Date: Aug 8, 2016
Description: Verify NTP Server field is Preset in Templates for Servers with NTP Info provided during initial appliance setup.
Test Flow    : 1) Log into the ASM UI and navigate to the Virtual Appliance Management page, 
               2) Ensure a Preferred NTP Server address is provided in NTP Settings during initial appliance setup or under 
                 'Virtual Appliance Management-->NTP Settings'.,
              3) Verify the 'NTP Server' field is preset with NTP Info provided during the initial appliance setup.,
              4) Verify the 'NTP Server' field is editable and you can modify it to provide a new value.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify NTP Server field is Preset in Templates for Servers with NTP Info provided during initial appliance setup.
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
        fileName = "ESXI_EQL_Conv_4505.json"       
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        
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
        #Validate NTP setting
        self.checkNTPSettings()
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True, verifyNTP= True)
       
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()