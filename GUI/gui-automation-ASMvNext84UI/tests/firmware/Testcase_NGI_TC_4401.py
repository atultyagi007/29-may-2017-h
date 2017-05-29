'''
Created on Sep 16, 2016

@author: preetam.sethi
Description : Add HDD firmware support to ASM
Test Flow  :1)Login as a Admin user 
            2)From the Settings/Repositories page, import the most recent firmware catalog into ASM.
            3)From the ASM Resources page, select appropriate servers to initiate firmware updates.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Add HDD firmware support to ASM
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
        self.addFirmwareRepository(option="networkPath",networkPath=globalVars.catlogRepository["nfsPathXml"])
        self.updateResource(resourceType='Servers', scheduleWait=None, updateAll=False)
       
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()