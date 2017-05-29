'''
Created on Jan 15, 2015

@author: ankit.manglic
Description: While deploying a service, the Admin can either Deploy Now or Schedule later. 
Test Flow    : 1) Login as admin create a template and deploy service.
               2) While deploying service schedule later and verify it is scheduled but not deployed.
               3) While deploying service schedule now and verify it is deployed. 
'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While deploying a service, the Admin can either Deploy Now or Schedule later.
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
        #Create Template if not exists
#         self.createTemplate(self.templateName, self.storageName, publishTemplate=True, volumeName = self.volumeName)
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName = self.volumeName)
        self.createTemplateBasic(self.templateName1, self.storageName, publishTemplate=True, volumeName = self.volumeName1)
     
     
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        #login with admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Delete service created
        self.deleteService(self.serviceName+"2")
        #Delete Template
        self.deleteTemplate(self.templateName)
        #logout of application
        self.logout()
 
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Deploys a Service and schedule later for deploy
#         self.deployService(self.templateName, self.serviceName, deployNow=False, deploymentScheduleTime=9)
        self.deployServiceBasic(self.templateName, self.serviceName, deployNow=False, deploymentScheduleTime=9)
        
        #Deploys a Service and deploy now
        self.logDesc("Deploying 2nd service")
        self.deployServiceBasic(self.templateName1, self.serviceName+"2")
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.templateName1 = "TestTemplate1" + currentTime
        self.volumeName = "TestVolume" + currentTime
        self.volumeName1 = "TestVol" + currentTime
        self.storageName = "testStorage"
         
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
           
        self.runTestCase()
            
        self.postRunCleanup()
#         self.cleanup_data()