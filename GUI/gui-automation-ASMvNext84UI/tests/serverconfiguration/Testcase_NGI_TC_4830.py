'''
Created on Nov 07, 2016

@author: raj.patel
Description:  Discover a T630 tower server.

Prerquisites:For hardware management 'hwmngt' network must be configured and assign it a static IP when there are insufficient IP's.

'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Discover a T630 tower server
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
#         status, result = self.findResource(self.resourceIP)
#         if status:
#             self.deleteResource(self.resourceIP)
      
      
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
        
        self.rackServerDiscovSchnario("Server", self.resourceIP ,self.resourceState,self.credentialName,self.userName,self.password,ipAddress=self.ipAddress, existingserverIPaddress=self.existingserverIPaddress,hwnwName=self.hwnwName)
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        dateStr = datetime.now().strftime('%y%m%d%H%M%S')
        self.resourceIP = "172.31.61.100"
        self.credentialName = "Towerser"+dateStr
        self.resourceState = "Managed"
        self.userName = "root"
        self.password = "calvin"
        self.ipAddress = True
        self.existingserverIPaddress = False
        self.hwnwName = "hwmngt"
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        