"""
Author: P.Suman
Created Date: Dec 03, 2015
Description: Verify that for each service on the Services page, the Standard user can View Details of the service.
"""

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    For each service, Standard user can View Details of the service.
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
        if len(serviceList) > 0:
            self.deleteService(serviceName)
            #Wait for Deployment to complete
            result = self.getDeploymentStatus(serviceName, deleteStatus=True)
            if len(result) > 0:
                if result[0]["Status"] == "Deleted":
                    self.succeed("Successfully Deleted Service '%s'"%serviceName)
                else:
                    self.failure("Failed to Delete Service '%s'"%serviceName, raiseExc=True)
            else:
                self.failure("Failed to Delete Service '%s' in expected time"%serviceName, raiseExc=True)
        
        #Initiate a New Deployment
        self.deployService(templateName, serviceName)
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(serviceName)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'"%serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%serviceName, raiseExc=True)
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Verify Options
        self.getServiceOptions(actualOptions=["Export to File", "View Details", "Update Firmware"], 
                            enableOptions=["View Details"], serviceName=serviceName)
        
        
    