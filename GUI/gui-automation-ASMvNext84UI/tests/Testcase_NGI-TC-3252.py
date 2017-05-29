'''
Created on Jan 14, 2016

@author: rajeev.kumar
Description :While deploying a service, if the Admin selects Schedule Later, he is unable 
to select an past date and time.
Test Flow   :1) Login as admin create a template and deploy service.
            :2) While deploying service schedule later on past time,he is unable to select.
'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While deploying a service, if the Admin select Schedule Later on past time, he is unable to perform.
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
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName=self.storageVolumeName)
     
     
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        #logout of application
        self.logout()
 
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Deploys a Service and schedule later for deploy
        self.deployService_pastTime(self.templateName, self.serviceName, deployNow=False, deploymentScheduleTime=4)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.storageVolumeName = "TestVolume" + currentTime
        self.storageName = "testStorage"
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()