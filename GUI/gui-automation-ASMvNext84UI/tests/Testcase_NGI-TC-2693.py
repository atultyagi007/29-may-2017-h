'''
Author: P.Suman
Created Date: Dec 1, 2015
Description: Verify that while granting access of service, the Admin is able to see all the 
            Standard users defined for the appliance.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While granting access of service, the Admin is able to see all the Standard users defined for the appliance.
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
        templateName="Test Template"
        serviceName="Test Service"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=templateName, publishedTemplate=True)
        
        #Check for User creation
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
                
        #Get Service List and Delete 'Test Service' if exists
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
        
        
        #Create Template
        userList = self.getUsersFromDeployService(templateName, serviceName)
        self.succeed("User List while Deploying Service:: %s"%str(userList))
        checkNonStandardUsers = [user for user in userList if user["Role"] != "Standard"]
        if len(checkNonStandardUsers) > 0:
            self.failure("Admin is able to see some Non 'Standard' Users while Deploying Service :: %s"%(str(checkNonStandardUsers)), 
                         raiseExc=True)
        else:
            self.succeed("Admin is not able to see any Non 'Standard' Users while Deploying Service")
        
        checkStandardUsers = [user for user in userList if user["Name"] == globalVars.standardUser]
        if len(checkStandardUsers) <= 0:
            self.failure("Admin is not able to see 'Standard' User while Deploying Service:: Missing User :'%s'"%(globalVars.standardUser), 
                         raiseExc=True)
        else:
            self.succeed("Admin is able to see 'Standard' Users while Deploying Service:: Users :'%s'"%str(checkStandardUsers))
        
        
                
