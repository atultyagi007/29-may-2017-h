'''
Author: P.Suman
Created Date: Dec 04, 2015
Description: Verify that, on the Service detail page for a deployed service, the Standard user can Delete, Retry, View all settings, 
        Migrate, Adjust Resources and Export to file for a service created by self
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    For a deployed service, the Standard user can Delete, Retry, View all settings, Migrate, Adjust Resources and Export to file for a service created by self.
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
        serviceName="Standard Service"
        templateName = "Standard Template"
        volumeName = "autoStandardVolume"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Create Template if not exists
        self.createSampleTemplate(templateName=templateName, publishedTemplate=True, volumeName=volumeName)
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Get Service List
        serviceList = self.getServices(serviceName=serviceName)
        if len(serviceList) <= 0:
            self.deployService(templateName, serviceName)
        
        time.sleep(120)
        #Verify Options
        self.getServiceOptions(actualOptions=["Export to File", "Delete", "Retry", "View All Settings", "Edit", "Add Resources",
            "Generate Troubleshooting Bundle", "Delete Resources"], enableOptions=["Delete", "Retry", 
            "View All Settings", "Add Resources", "Delete Resources"], serviceName=serviceName, serviceDetailOptions=True)
                    
                    
