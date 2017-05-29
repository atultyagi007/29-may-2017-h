'''
Author: P.Suman
Created Date: Nov 19, 2015
Description: Verify that while granting access of template, the Admin is unable to see all the Read-only users 
            defined for the appliance
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While granting access of template, the Admin is unable to see all the Read-only users defined for the appliance
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Get Templates
        templateExists = False
        tempList = self.getTemplates(option="My Templates", templateName="Test Template")
        if len(tempList) > 0:
            templateExists = True
        
        #Deleting Template                
        if templateExists:
            self.succeed("Template with Name 'Test Template' already exists so Deleting 'Test Template'")            
            self.deleteTemplate("Test Template")
            
        #Creating Template
        #Get Resources
        resList = self.getResources("Storage")
        if len(resList) > 0:
            self.succeed("Successfully fetched Storage Resources, Values :: %s"%(str(resList)))
        else:
            self.failure("No Resources of Type 'Storage'", resultCode=BaseClass.OMITTED, raiseExc=True)
        storageName = ""
        for res in resList:
            if "EqualLogic" in res["Manufacturer /Model"]:
                storageName = res["Resource Name"]
        
        #Check for user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=False)
                
        #Create Template
        userList = self.getUsersFromCreateTemplate("Test Template", storageName)
        self.succeed("User List while Creating Template :: %s"%str(userList))
        checkReadonlyUsers = [user for user in userList if user["Role"] == "Read only"]
        if len(checkReadonlyUsers) > 0:
            self.failure("Admin is able to see 'Read only' Users while creating Template :: %s"%(str(checkReadonlyUsers)), 
                         raiseExc=True)
        else:
            self.succeed("Admin is not able to see any 'Read only' Users while creating Template")