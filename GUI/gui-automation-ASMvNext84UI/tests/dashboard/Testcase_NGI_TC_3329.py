'''
Created on Jan 6, 2016

@author: Raj.Patel

Description : Log into the ASM UI and navigate to the Dashboard page.Verify that the links under Quick Actions open the appropriate pages:<br><ul><li>Deploy New Service</li></ul><ul><li>Create Template</li></ul><ul><li>Add Existing Service</li></ul>


'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that the links under Quick Actions open the appropriate pages:Deploy New Service,Create Template,Add Existing Service.
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
        
        self.loadDashboardPage()
        
        self.logout()
        
                
        