'''
Created on Jan 8, 2016

@author: HCL

Description : Log into the ASM UI and navigate to the Dashboard page.Verify that the Recent Activity section shows the most recent appliance tasks, beginning with the most recent.For each task, verify:Proper job ID, resource IP, service tag,etc.(dependent upon the individual task).,Verify the task initiator (user) and time/date stamp for accuracy.,Verify that the View All link navigates to the Logs page.

'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Admin user, View all in Recent Activity on the Dashboard displays the Application logs which 
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Administrator')
        self.get_View_Link("recentActivityViewAll")
      
       
        

