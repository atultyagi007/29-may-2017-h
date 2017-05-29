'''
Author: Bruce Burden
Created Date: May 18, 2016
Description: Add an OS Image to the OS Image Repository. Verify the OS Image was added.
'''
from tests.globalImports import *
from libs.product.pages import Repositories

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Add an OS Image to the OS Image Repository.       
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
        failList = []
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)

        utility.execLog("Add Repository class object")
        pageObject  = Repositories.Repositories(self.browserObject)
        utility.execLog("Load the repositories page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

        resourceConfig = utility.readExcel(globalVars.inputFile, "Repositories")
        if not resourceConfig:
            return self.browserObject, False, resourceConfig

        for resource in resourceConfig:
            globalVars.browserObject, status, result = pageObject.deleteOSRepository(resource)
            globalVars.browserObject, status, result = pageObject.addOSRepository(resource)
            if not status:
                self.failure(result)
                failList.append(resource["Repository Name"])
            else:
                globalVars.browserObject, status, result = pageObject.verifyOSRepository(resource)
                if not status:
                    self.succeed(result)
                    failList.append(resource["Repository Name"])

        if len(failList) == 0:
            self.succeed("Successfully Added OS Image Repository for all repositories :: %s" % resourceConfig)
        else:
            self.failure("Failed to Add OS Image Repository :: %s"%(failList), raiseExc=True)

