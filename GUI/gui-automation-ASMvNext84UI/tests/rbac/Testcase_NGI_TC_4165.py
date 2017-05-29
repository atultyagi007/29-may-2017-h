'''
Created on Dec 31, 2015

@author: nidhi.aishwarya

Description : Verify that on the Service detail page, the Standard user can perform migration in a service owned by him.
Test Flow : 1) Login as Admin user
            2) Deploy a template
            3) Login as Standard user and perform migration check while deploying the service on dashboard
           
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify that on the Service detail page, the Standard user can perform migration in a service owned by him.
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
            
        self.get_ServicesPage("","Migration")
        
        self.logout()
      
            
     
        
       
        

