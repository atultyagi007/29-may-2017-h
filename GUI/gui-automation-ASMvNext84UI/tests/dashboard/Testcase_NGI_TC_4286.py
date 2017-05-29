'''
Created on Nov 25, 2015

@author: rajeev.kumar

Description : Verify that for a Read-only user, View all in Recent Activity on the Dashboard displays the Application 
              logs which includes all the activities.
Test Flow   :1)Login as a Admin user & create or Verify Read-only user
             2)Login as a Read-only user and Landing to Dashboard page & verified View all & navigate to Logs page.
'''

from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Read-only user, View all in Recent Activity on the Dashboard displays the Application logs which 
    includes all the activities.
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
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Read only')
        self.get_View_Link("recentActivityViewAll")
        
        #Logout Read-only user
        self.logout()
      
       
        

