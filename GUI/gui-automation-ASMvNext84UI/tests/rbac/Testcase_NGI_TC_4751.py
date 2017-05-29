'''
Author: P.Suman
Created Date: Nov 27, 2015
Description: Verify that for each service on the Service detail page, the Read-only user 
            can View All settings of the template used for the service.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only able to View All settings of the template used for the service.
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
        serviceName="Test Service2497"
        templateName = "Test Template"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Create Template if not exists
        self.createSampleTemplate(templateName=templateName, deleteAndCreate=True, publishedTemplate=True)
        
        #Get Service List
        serviceList = self.getServices(serviceName=serviceName)
        if len(serviceList) <= 0:
            self.deployService(templateName, serviceName)
         
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Get Template Options
        tempSettings = self.getServiceSettings(serviceName=serviceName)
        if len(tempSettings) <= 0:
            self.failure("Failed to fetch Service Deployment Settings for Service '%s' :: %s"%(serviceName, str(tempSettings)), raiseExc=True)
        self.succeed("Successfully fetched Service Deployment Settings for Service '%s' :: %s"%(serviceName, str(tempSettings)))
        
        #Verification
        self.verifyTemplateSettings(tempSettings)        
                    
                    
