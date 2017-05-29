'''
Created on Dec 30, 2015

@author: raj.patel

Description : Verify that if an Admin becomes a Standard user, the change takes effect correctly. Verify all pages and settings

'''

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that if an Admin becomes a Standard user, the change takes effect correctly. Verify all pages and settings
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
        
        #Login as Read only user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        self.createAndEditUser()
        
        self.logout()
        
                
        