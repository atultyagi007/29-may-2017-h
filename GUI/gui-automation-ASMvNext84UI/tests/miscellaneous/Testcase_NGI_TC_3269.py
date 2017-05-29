'''
Created on Jan 29, 2016

@author: Atul.kumar
Description :Verify that correct current and available versions are displayed.
Test Flow   :
1)Verify that there is a path and info on the current and available appliance, and Edit option.
2)Verify the upgrade option is available under Virtual appliance management in Settings
3)Update current path as required for the release.
4)Ensure the available version and current version are different. If its differnt click the upgrade button
5)Keep polling the applaince till the ASM UI comes up and then in ASM UI go to About and verify the applaince is upgraded to the new version
'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Upgrade appliance from Virtual appliance Management.
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
#         xpath = "//table[@id='%s']/tbody/tr[%i]/td[%i]"%(self.repositoryTable, row, col)
#         inUse = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")

        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #check & update Virtual Appliance
        self.applianceUpgrade()
        
        self.logout()