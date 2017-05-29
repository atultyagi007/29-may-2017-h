'''
Created on Nov 26, 2015

@author: rajeev.kumar

Description:  Verify that for a Standard user, the following information is displayed
 on Dashboard: Service Overview, Deployed/Error/ In progress Services, Total Server
  Utilization, Total Storage capacity, License Information, Deploy with recently 
  used template, Recent Activity, Learn.

'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Standard user, the following information is displayed on the Dashboard: Service Overview, Deployed/Error/ In progress Services, 
   Total Server Utilization, Total Storage capacity, Recent Activity, Learn

    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        utility.execLog(tc_id)
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
      
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Standard')
        self.get_DashboardOptions()
        time.sleep(5)
