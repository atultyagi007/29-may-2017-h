'''
Created on Nov 07, 2016

@author: raj.patel
Description: Discover and configure chassis and server at the same time.

'''
from tests.globalImports import *
from libs.product import utility

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Discover and configure chassis and server at the same time
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        resourceConfig = utility.readExcel(globalVars.inputFile, self.excelSheetName)
        for resource in resourceConfig:
            resourceIP = resource["StartIP"]
            self.ipList.append(resourceIP)
            status, result = self.findResource(resourceIP)
            if status:
                self.deleteResource(resourceIP)
      
      
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
#         self.deleteResource(self.resourceIP)
        #logout of application
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        
        self.discoveryChassisAndServer(self.excelSheetName,self.ipList)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.excelSheetName = "Discovery4904"
        self.ipList =[]
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        