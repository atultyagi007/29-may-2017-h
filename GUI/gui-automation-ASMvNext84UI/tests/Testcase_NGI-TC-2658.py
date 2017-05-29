'''
Author: P.Suman
Created Date: Dec 03, 2015
Description: Verify that, on the Service detail page for a deployed service, the Standard user 
        can only View all settings and Export to file for a service shared by the Admin
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    For a deployed service, the Standard user can only View all settings and Export to file for a service shared by the Admin
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
         
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Verify Options
        self.getServiceOptions(actualOptions=["Export to File", "Delete", "Retry", "View All Settings", "Edit", "Add Resources",
                    "Generate Troubleshooting Bundle"], disableOptions=["Delete", "Retry", "Edit", "Add Resources", "Generate Troubleshooting Bundle"], 
                    enableOptions=["Export to File", "View All Settings"], serviceName=serviceName, serviceDetailOptions=True)
                    
                    
