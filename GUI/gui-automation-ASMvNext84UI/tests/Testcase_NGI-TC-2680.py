'''
Author: P.Suman
Created Date: Nov 30, 2015
Description: Verify that while granting access of service, the Admin is unable to see all the Read-only 
        users defined for the appliance.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While granting access of service, the Admin is unable to see all the Read-only users defined for the appliance
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
        
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=templateName, publishedTemplate=True)
                
        #Check for user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=False)
                
        #Get Service List
        serviceList = self.getServices(serviceName=serviceName)
        if len(serviceList) <= 0:
            self.deployService(templateName, serviceName)
                
        #Deploy Service
        userList = self.getUsersFromDeployService(templateName, serviceName)
        self.succeed("User List while Deploying Service :: %s"%str(userList))
        checkReadonlyUsers = [user for user in userList if user["Role"] == "Read only"]
        if len(checkReadonlyUsers) > 0:
            self.failure("Admin is able to see 'Read only' Users while Deploying Service :: %s"%(str(checkReadonlyUsers)), 
                         raiseExc=True)
        else:
            self.succeed("Admin is not able to see any 'Read only' Users while Deploying Service")