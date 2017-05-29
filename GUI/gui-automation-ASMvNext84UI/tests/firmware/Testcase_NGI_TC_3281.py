'''
Author: nidhi.aishwarya
Created Date: Jan 25,2016
Description:  Select multiple resources of same type and try to update Firmawre at the same time
Test Flow : 1) Login as Admin user
            2) Click on Resources option at Dashbaord  page and Select Servers as the resource type and perform updation             
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Select multiple resources of same type and try to update Firmawre at the same time
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
        
        self.updateResource(resourceType='Switches', scheduleWait=None, updateAll=True)        
        self.logout()
        
        
        
    
        
