'''
Created on March 8, 2016

@author: Pavan G / Saikumar Kalyankrishnan
Description: Validate a Port View on a Baremetal CentOS Deployment 
Test Flow    : 1) Login as admin.
               2) Run a baremetal deployment with CENTOS. Execute test case NGI-TC-2902 located under Deployments-Baremetal folder
               3) Once the deployment is successful, navigate to the server IP used in the deployment from resources page. Click on View Details - > PortView
               
Pre-requisite : Successful Deployment
               
'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Validate a Port View on a Baremetal CentOS Deployment 
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
      
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        
        #Logout of ASM Application
        self.logout()
        
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
            
        self.logDesc("Getting Deployment Status")
        #Port View available only for Healthy Deployed Service
        self.logDesc("Getting Deployment Status. If Success, navigating to Port View Page")
        #if self.getDeploymentStatus(self.serviceName)[0]["Status"] == "Success":
            #Port View of an already deployed service
         #   self.logDesc("Deployment Status = Success, navigating to Port View Page")
            
        if(self.validatePortView(self.serviceName)):
            self.succeed("Successfully validated Port View for all deployed Servers")
        else:
            self.failure("Error in validating Port View. Check Logs Reports for specific errors")
        #else:
        #   self.logDesc("Deployment is not in a successful state. Unable to validate Port View")
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S') 

        #For Testing Purpose providing hard-coded Service Name, will have this value pull from JSON once separate Flow scripts are available        
        self.serviceName = "Service_SK"
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
        
         
        self.runTestCase()
          
        self.postRunCleanup()