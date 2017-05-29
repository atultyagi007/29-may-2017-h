'''
Created on Feb 10, 2016

@author: rajeev.kumar
Description :Click on compliance report and check if its accurate with bundle version.
Test Flow   :1)Login as a Admin user and upload custom bundle
            :2)go to resource and check compliance report
prerequisites:Firmware catloge should be there.

Dec 20, 2016 modified by preetam.sethi
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Click on compliance report and check if its accurate with bundle version
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
        repoDetails=self.getRepositories()
        repoDetails=[repo for repo in repoDetails if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required"]
        if len(repoDetails)==0:
            self.omit("No Catalog in Available State")
        else:
            repoDetails=repoDetails[0]
            repoName=repoDetails['Repository Name']
        self.addSwitch_CustomBundles(option="I/O-Aggregator", catalogName=repoName)
        resourceDetails=self.getResources(resourceType="Switch")
        resourceList= [resource for resource in resourceDetails if 'I/O-Aggregator' in resource['Manufacturer /Model']]
        resourceList= [resource for resource in resourceList if 'Compliant' in resource['Compliance']]
        if not len(resourceList):
            self.omit("No Resource in Compliant state")
        resource=resourceList[0]
        self.verifySwitchFirmwareVersion('I/O-Aggregator', resource['IP Address'])
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()