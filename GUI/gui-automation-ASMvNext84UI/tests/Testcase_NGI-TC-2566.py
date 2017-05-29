'''
Author: nidhi.aishwarya
Created Date: Nov 26, 2015
Description: Verify that for a Standard user, clicking on individual service link on the Dashboard takes to Service Information page.
Test Flow : 1) Login as Standard user
            2) Click on any individual Services at dashboard page and perform verifications
'''
from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Standard user, clicking on individual service link on the Dashboard takes to Service Information page
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)

        self.get_IndividualServices()
        
        self.logout()
        
       
            
        
        
    
        
