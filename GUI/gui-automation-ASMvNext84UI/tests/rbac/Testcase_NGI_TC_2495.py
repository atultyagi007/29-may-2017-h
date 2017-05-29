'''
Author: P.Suman
Created Date: Oct 6, 2015
Description: Verify that on the Virtual Appliance Management page, the Read-only user can view 
            Time Zone and NTP Settings, Proxy Settings, SSLCertificates Details and License Information.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can view Time Zone and NTP Settings, Proxy Settings, SSLCertificates Details and License Information
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
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Verify Options
        self.getOptionDetails("VirtualApplianceManagement")
