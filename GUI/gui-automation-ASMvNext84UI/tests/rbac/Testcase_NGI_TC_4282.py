'''
Created on Aug 08, 2016

@author: raj.patel
Description: Standard user able to grant access to other Standard users while deploying a service.
'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user able to grant access to other Standard users while deploying a service.
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
        #Login as Admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Create Standard User if doesnt exist
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
        
        users = self.getLocalUsers(userName=self.userName)
        if len(users) <= 0:
            self.createLocalUser(userName=self.userName, userPassword=self.userPassword, userRole='Standard', verifyUser=True)
                         
        #Create Template
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume",
                                  managePermissions=True, userList=[globalVars.standardUser], deleteAndCreate=True)

     
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
        #Login as standard user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Deploy Service
         
        self.deployService(self.templateName, serviceName=self.serviceName, managePermissions=True, userList=[self.userName])
        self.logout()
                            
        #Login as mentioned user
        self.login(username=self.userName, password=self.userPassword, newInstance=False)
        
        serviceList = self.getServices(serviceName=self.serviceName)
        if len(serviceList) > 0:
            self.succeed("Second Standard User has  access to Service '%s' after granting permission by First Standard User"%self.serviceName)
        else:
            self.failure("Second Standard User has not access to Service '%s' after granting permission by First Standard User"%self.serviceName, raiseExc=True)
        
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.userName = "Standard5"
        self.userPassword = "autopassword"
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
           
        self.runTestCase()
            
        self.postRunCleanup()
