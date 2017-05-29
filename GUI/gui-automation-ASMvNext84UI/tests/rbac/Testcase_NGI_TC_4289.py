'''
Created on Jan 14, 2016

@author: nidhi.aishwarya

Description : Verify that the Admin is unable to deploy service from a template with server pool he does not have access to.
Test Flow : 1) Login as Admin user
            2) Delete the existing server if any present with same name
            3) Create a server pool and not assign it to Admin user
            4) Deploy a service with the created Server
            5) Verify the error message             
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify that the Admin is unable to deploy service from a template with server pool he does not have access to.
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True) 
        
        time.sleep(20)
            
        self.get_ServicesPage("Admin","Deployment_without_server_access")
        
        self.logout()
      
            
     
        
       
        

