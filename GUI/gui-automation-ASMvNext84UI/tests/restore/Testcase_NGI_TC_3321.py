'''
Author: raj.patel
Created Date: Feb 23, 2016
Jun 30,2016    preetam.sethi    modified to merge Testcase NGI-TC-3138, NGI-TC-3139.

Description: Backup and Restore page,select to Restore Now. Testcase NGI-TC-3138, NGI-TC-3139 are merged in this testcase.
            For NGI-TC-3138-- set Backup file as a previous release backup file.
            For NGI-TC-3139-- set Backup file as same release previous build backup file
            
Test Flow    : 1) Log into the ASM UI and navigate to the Back And Restore page, 
              2) select to Restore Now.
              3) Verify Restore successful.
              4)Prerequisite: BACKUPPATH  must be in same location and specified in config.ini
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Backup and Restore page,select to Restore Now.

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
        
        self.logout()
        
    
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyLandingPageOptions(userRole='Administrator')
        #Get details of Jobs    
        self.restoreNowFromSetting()
        self.logout()
        status= self.validateRestore()
        if status:
            self.succeed("Successfully Restored appliance")
        else:
            self.failure("Restored appliance Validation failed", raiseExc= True)
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
