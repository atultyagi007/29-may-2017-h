'''
Author: nidhi.aishwarya
Created Date: Jan 4, 2016
Description: Verify that on the Service detail page, the Standard user is not able to adjust any resources  in a service shared by the Admin.         
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that on the Service detail page, the Standard user is not able to adjust any resources  in a service shared by the Admin.
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
        
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)    

        self.get_ServicesPage("","Cannot Adjust Resources")
        
        self.logout()
