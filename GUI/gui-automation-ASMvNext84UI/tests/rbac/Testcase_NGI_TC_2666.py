'''
Author: P.Suman
Created Date: Nov 4, 2015
Description: Verify that the Admin can navigate to all pages without any errors after 
            deleting some/all Standard users
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Admin can navigate to all pages without any errors after deleting Standard users.
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
        
        #Verify whether logged in User is admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
        
        #Get Service List
#         serviceList = self.getServices()        
#         if len(serviceList) > 0:
#             for service in serviceList:
#                 serviceName = service["Name"]
#                 self.deleteService(serviceName)
#                 #Wait for Deployment to complete
#                 result = self.getDeploymentStatus(serviceName, deleteStatus=True)
#                 if len(result) > 0:
#                     if result[0]["Status"] == "Deleted":
#                         self.succeed("Successfully Deleted Service '%s'"%serviceName)
#                     else:
#                         self.failure("Failed to Delete Service '%s'"%serviceName, raiseExc=True)
#                 else:
#                     self.failure("Failed to Delete Service '%s' in expected time"%serviceName, raiseExc=True)
#         
#         #Get Templates
#         tempList = self.getTemplates(option="My Templates")
#         for template in tempList:
#             templateName = template["Name"]
#             self.deleteTemplate(templateName)
        
        #Delete Standard User
        self.deleteLocalUser(globalVars.standardUser, verifyUser=True)
         
        #Get All Resources
        resList = self.getResources()
        if len(resList) > 0:
            self.succeed("Able to fetch Resources Info :: %s"%(str(resList)))
        else:
            self.succeed("No Resources were Discovered :: %s"%(str(resList)))
         
        #Get All Server Pools
        poolList = self.getServerPools()
        if len(poolList) > 0:
            self.succeed("Able to fetch Server Pool Info :: %s"%(str(poolList)))
        else:
            self.succeed("There are No Server Pools :: %s"%(str(poolList)))
         
        #Verify Sample Templates in List form
        tempList = self.getTemplates(option="Sample Templates", viewType="List")
        if len(tempList) > 0:
            self.succeed("Able to read 'Sample Templates' in 'List' Form :: %s"%str(tempList))
        else:
            self.succeed("No Templates in 'Sample Templates' Tab to view in 'List' form")
        
        #Verify Services in List form
        svcList = self.getServices()
        if len(svcList) > 0:
            self.succeed("Able to read Services in 'List' Form :: %s"%str(svcList))
        else:
            self.succeed("No Services in 'Services' Page to view in 'List' form")
        
        