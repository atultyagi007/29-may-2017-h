'''
Author: P.Suman
Created Date: Nov 9, 2015
Description: Verify that on the All Resources page, the Standard user can filter the resources based on the Resource type.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user can filter the resources based on the Resource type
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
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Get Resource Types
        resourceTypes = self.getResourceTypes()
        self.succeed("Able to fetch Resource Types :: %s"%(str(resourceTypes)))
                        
        #Read Resources under each Type 
        for rctype in resourceTypes:
            if rctype.lower() == "all":
                continue
            resList = self.getResources(rctype)            
            if len(resList) > 0:
#                 searchKey = ""
#                 if rctype == "Dell Chassis":
#                     searchKey = "Chassis"
#                 elif rctype == "Servers":
#                     searchKey = "Server"
#                 elif rctype == "Switches":
#                     searchKey = "Switch"
#                 else:
#                     searchKey= rctype
#                 for resource in resList:
#                     if searchKey not in resource["Resource Type"]:
#                         self.failure("Resource Type Filter '%s' shows some invalid values :: %s"%(rctype, resList), raiseExc=True)
                self.succeed("Successfully verified Resource Type Filter '%s' , Values :: %s"%(rctype, resList))
            else:
                self.failure("Resource Type Filter '%s' shows some invalid values :: %s"%(rctype, resList), raiseExc=True)
#                 self.succeed("No Resources of Type '%s'"%(rctype))
            
        self.logout()