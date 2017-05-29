'''
Author: Dheeraj.Singh
Created Date: Jun 03, 2016
Description: Verify vSphere VDS Settings - 2 server, 1 port, 4 partition.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify vSphere VDS Settings - 2 server, 1 port, 4 partition.
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
        fileName="NGI_TC_4150.json"            
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        #Verify Resource Availability
        #self.verifyResourceAvailability(self.components)
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

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        #Create Server Pool
        
        instances = self.components["Server"]["Instances"]
        if instances > 0:
            self.createServerPool(self.components)
            
        #Expected VDS Setting Details Dictionary
        networkConfigDict={"vdsCount":4, "PXE Port Group":1, "Hypervisor Management Port Group":1,"autoHypervisorMigration Port Group":1,"Public LAN Port Group":1, "autoSANISCSI Port Group":2}  
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True)
        
       # validate Cluster VDS Settings
        self.validateVDSSetting(self.components,networkConfigDict)

    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()