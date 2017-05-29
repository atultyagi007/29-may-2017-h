'''
Created on Jan 25, 2017

@author: preetam.sethi
Description : Verify a template can set default catalog even when there is none
Test Flow   :   Create a template and select the ASM default catalog.
                Default catalog should be displayed and template can be published without errors.
    
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
import datetime
class Testcase(Manager.Manager): 
    """
    Verify a template can set default catalog even when there is none
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
        volName='HCLVol'+datetime.datetime.now().strftime("%H%M%S")
        storageList=self.getResources("Storage")
        storageNameList=[storage["Resource Name"] for storage in storageList if "EqualLogic" in storage['Manufacturer /Model']]
        deviceName=storageNameList[0]
        self.components='{"Storage1":{"Type":"EqualLogic", "Name":"%s", "VolumeName":"%s", "Size":"10GB","AuthType":"IQN/IP","AuthUser":"grpadmin","AuthPwd":"dell1234","IQNIP":"172.31.64.241"}}'%(deviceName,volName)
        
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
        self.createTemplate(templateName="Template_4793",components=self.components, publishTemplate=True,manageFirmware=True, repositoryName='Use ASM appliance default catalog')
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    