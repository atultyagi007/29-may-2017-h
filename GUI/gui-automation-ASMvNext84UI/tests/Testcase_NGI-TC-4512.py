'''
Author: Bruce Burden
Created Date: July 15, 2016
Description: Delete an EMC storage device. Device info will be read from config/Input.xlsx.
'''
from globalImports import *
from libs.product.pages import Resources
from libs.product.pages import Dashboard

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
        failFlag = False

        resourceToDiscover="EMC"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)

        credentials = utility.readExcel(globalVars.inputFile, "Credential")
        if not credentials:
            return self.browserObject, False, credentials

        resourceConfig = utility.readExcel(globalVars.inputFile, "Discovery")
        if not resourceConfig:
            return self.browserObject, False, resourceConfig

        status, result = self.findResource(resourceToDiscover)
        if not status:
            self.failure("Failed to find resource {}, result :: {}!".format(globalVars.deviceId), result, raiseExc=True)

        for resource in resourceConfig:
            if resource["StartIP"] == globalVars.deviceId:
                break
        else:
            for resource in resourceConfig:
                if resource["EndIP"]:
                    if resource["EndIP"] == globalVars.deviceId:
                        break
            else:
                self.failure("Failed to find resource in Input.xlsx file! :: {}".format(globalVars.deviceId), raiseExc=True)

        for credential in credentials:
            if resource["Type"] == credential["Type"]:
                if credential["Name"] in resource.values():
                    break

        utility.execLog("Dashboard class object")
        pageObject = Dashboard.Dashboard(self.browserObject)
        utility.execLog("Load the Dashboard page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
            globalVars.browserObject, status, result = pageObject.numberResourceDiscovered(resource)
            if status:
                dashboardStorageDiscovered = result
            else:
                failFlag = True
        else:
            self.failure("Failed to open Dashboard page :: {}".format(result), raiseExc=True)

        utility.execLog("Attempting to delete resource {}".format(globalVars.deviceId))
        status, result = self.deleteResource(globalVars.deviceId)
        if not status:
            self.failure("Failed to find resource {}, result :: {}!".format(globalVars.deviceId), result, raiseExc=True)
        else:
            utility.execLog("()".format(result))

        utility.execLog("Verify resource {} is not found.".format(globalVars.deviceId))
        status, result = self.findResource(resourceToDiscover)
        if status:
            self.failure("Failed to find resource {}, result :: {}!".format(globalVars.deviceId), result, raiseExc=True)
        else:
            utility.execLog("{}".format(result))

        utility.execLog("Dashboard class object")
        pageObject = Dashboard.Dashboard(self.browserObject)
        utility.execLog("Load the Dashboard page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
            globalVars.browserObject, status, result = pageObject.numberResourceDiscovered(resource)
            if status:
                dashboardStorageDiscovered2 = result
            else:
                failFlag = True
        else:
            self.failure("Failed to open Dashboard page :: {}".format(result), raiseExc=True)

        utility.execLog("Verify exactly one storage array was discovered")
        if int(dashboardStorageDiscovered2) == int(dashboardStorageDiscovered) - 1:
            utility.execLog("Verified exactly one storage array was removed :: before :: {} :: after :: {}"\
                                        .format(dashboardStorageDiscovered, dashboardStorageDiscovered2))
        else:
            utility.execLog("Failed to verify only one storage array was discovered :: before :: {} :: after :: {}"\
                                        .format(dashboardStorageDiscovered, dashboardStorageDiscovered2))
            failFlag = True

        utility.execLog("Verify {} is found in activity message".format(globalVars.assetTag))
        globalVars.browserObject, status, result = pageObject.getActivityMessage(globalVars.assetTag)
        if globalVars.assetTag in result and "Successfully completed the remove operation" in result:
            utility.execLog("Verified {} was found in activity message".format(globalVars.assetTag))
        else:
            utility.execLog("Failed to find {} in activity message".format(globalVars.assetTag))
            failFlag = True

        utility.execLog("Resources class object")
        pageObject = Resources.Resources(self.browserObject)
        utility.execLog("Load the Resources page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure("Failed to open Resources page :: {}".format(result), raiseExc=True)

        globalVars.browserObject, status, result = pageObject.discoverResource2(resource, credential)
        if status:
            globalVars.browserObject, status, result = pageObject.verifyResource(resource)
            if status:
                None
            else:
                self.failure("Discovery verification failed for :: {} :: Reason :: {}".format(resource, result), raiseExc=True)
        else:
            self.failure("Failed to Discover :: {}".format(resource), raiseExc=True)

        if failFlag:
            self.failure("Failed to Discover or Verify :: {}".format(resource), raiseExc=True)
        else:
            self.succeed("Successfully Discovered :: {}".format(resource))
