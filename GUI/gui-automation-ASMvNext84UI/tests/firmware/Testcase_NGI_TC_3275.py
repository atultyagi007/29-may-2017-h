'''
Created on Jan 25, 2016

@author: rajeev.kumar

modified on Feb 23, 2017
@author: preetam.sethi

Description :update FW on 2 updated required server at same time.
Test Flow   :1)Login as Admin user & update firmware on updated required state.
PreRequisties:Updated Required state Servers firmware should be there.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Firmware Update of Server
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
        statusDetailList, updateTime=self.updateResource('Servers', scheduleWait=None, updateAll=True)
        serverIpList=[statusDetail["ResourceIP"] for statusDetail in statusDetailList]
        for serverIp in serverIpList:
            complianceStatus=self.getResourceComplianceStatus(serverIp)
            if complianceStatus=='Compliant':
                self.succeed('Server %s updated to compliant state'%serverIp)
            else:
                self.failure('Server %s firmware update failed'%serverIp, raiseExc=True)
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    