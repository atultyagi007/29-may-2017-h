'''
Created on July 18, 2016

@author: raj.patel
Description :Configure FX Chassis with FNIOA FW Version below 9.9.
Test Flow  :1)Login as a Admin user and show the exists file and allow add new file.
Prerequisites:Firmware catalog must be ASM October 2015 Catalog.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Configure FX Chassis with FNIOA FW Version below 9.9.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        repoDetails=self.getRepositories()
        repoDetails=[repo for repo in repoDetails if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required"]
        if len(repoDetails)==0:
            self.omit("No Catalog in Available State")
        else:
            repoDetails=repoDetails[0]
            repoName=repoDetails['Repository Name']
        self.addSwitch_CustomBundles(option="PE-FN-2210S-IOM", catalogName=repoName)
        self.verifyResourceModelAvailblity("Switches","PE-FN-2210S-IOM")
        resources=self.getResources(resourceType="Switches")
        switchList=[switch for switch in resources if "PE-FN-2210S-IOM" in switch['Manufacturer /Model']]
        switchIp=switchList[0]['IP Address']
        self.checkSwitchVersion(switchIp,9.10)
        chassisList=self.getResources('Dell Chassis')
        chassisIp=chassisList[0]['IP Address']
        chassisName=chassisList[0]['Resource Name']
        self.blockIOMConfiguration(chassisIp, chassisName,upLinkCheked=False ,DefinedUplink=False,ports="")
#        self.blockIOMConfiguration('172.31.60.78', 'HclTestChassis',upLinkCheked=False ,DefinedUplink=False,ports="")
        self.logout()