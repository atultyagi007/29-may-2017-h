"""
Author: P.Suman
Created Date: Nov 27, 2015
Description: Verify that the Admin is able to edit, delete and retry services initiated by self
"""

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Admin able to edit, delete and retry services initiated by self.
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
        serviceName="Test Service"
        templateName = "Test Template"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Create Template if not exists
        self.createSampleTemplate(templateName=templateName, publishedTemplate=True)
        
        #Get Service List
        serviceList = self.getServices(serviceName=serviceName)
        if len(serviceList) <= 0:
            self.deployService(templateName, serviceName)
        
        #Verify Options
        time.sleep(120)
        self.getServiceOptions(actualOptions=["Export to File", "Delete", "Retry", "View All Settings", "Edit"], 
                    enableOptions=["Edit", "Delete", "Retry"], serviceName=serviceName, serviceDetailOptions=True)
        
        
    