'''
Created on Dec 21, 2015

@author: nidhi.aishwarya

Description : Verify that while deploying a service, created by self from template by the Admin, the Standard user is able to select only the server pool that he has access to.
Test Flow    : 1) Login as Admin user and assign a server pool access to standard user
               2) Create A template with a server having above Server pool through Admin user
               3) Login as Standard user and Deploy the Service by Selecting above template and perform verifications
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify that while deploying a service, created by self from template by the Admin, the Standard user is able to select only the server pool that he has access to.
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
             
        self.get_ServicesPage("Standard","Deploying Service")
        
        self.logout()
      
            
     
        
       
        

