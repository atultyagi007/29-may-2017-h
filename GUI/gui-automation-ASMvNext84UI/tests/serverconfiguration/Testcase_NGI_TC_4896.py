'''
Created on Nov 07, 2016

@author: raj.patel
Description:  Discover servers in an IP range.

'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Discover servers in an IP range
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
        #Create and Configure Networks - Refer Input.xlsx
#         self.setupNetworks()
        status, result = self.findResource(self.resourceIP)
        if status:
            self.deleteResource(self.resourceIP)
      
      
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
        
        self.rackServerDiscovSchnario("Server", self.resourceIP ,self.resourceState,self.credentialName,self.userName,self.password,posSchanario=self.posSchanario,ipAddress=self.ipAddress, existingserverIPaddress=self.existingserverIPaddress)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.resourceIP = "172.31.61.81"
        self.credentialName = "Dell PowerEdge iDRAC Default"
        self.resourceState = "Managed"
        self.userName = "root"
        self.password = "calvin"
        self.ipAddress = True
        self.existingserverIPaddress = True
        self.posSchanario = True
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        