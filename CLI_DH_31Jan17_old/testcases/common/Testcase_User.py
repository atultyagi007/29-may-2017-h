'''
Created on Feb 22, 2016

@author: raj.patel
Description: Test case to verify  user's URI:
  /User/    get
/User/     post
/User/{userId}    get
/User/{userId}    delete

'''
import os
import sys
import datetime
import requests
import time
import json
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from utilityModule import UtilBase
import globalVars
from TestCase_22222 import TestCase

class Testcase(UtilBase,TestCase): 
    """
    Users  to manage ASM
    """
    tc_Id = ""
    ad_name = ""
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        UtilBase.__init__(self)
        TestCase.__init__(self)
        self.loginUser = "admin"
        self.tc_Id = self.getTestCaseID(__file__)
                
        
    def getAllUsers(self):
        """
        Removes all Directory Services
        """
        resUR, statUR = self.getResponse("GET", "User")
        self.userListLength=len(resUR)
        
        if not statUR:
            self.log_data("No user found")
            return False
        else:
            print resUR
            print "%s :User found"%str(len(resUR))
            self.log_data("User found :%s"%str(len(resUR)))
    
    
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        self.log_data(" Validating the Inputs :: ")
        self.userName =  globalVars.ad_User    
        domainName = globalVars.domainName_user
        
        self.getAllUsers()
        
        resCU, statCU = self.createUser(self.userName,domainName)
        if not statCU:
            self.log_data("Failed to create User: %s"%self.userName)
            
        self.log_data("Successfully created User: %s"%self.userName)
#         self.log_TestData(["", "", "","ServerPool Teardown",'Success',"ServerPool Teardown Successfully with RefID : %s"%str(job["groupSeqId"])])
            #Validate User Creation
        self.refId = str(resCU["userSeqId"])
        self.verifyUser()
        
        resUR, statUR = self.cleanUpUsers()
        if not statUR:
            self.log_data( " Unable to remove all users : %s"%str(resUR))
        else:
            self.log_data( " Successfully removed all users")
        if statUR and statCU:
            print "Successfully tested User URI"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested User URI', 'Success','Successfully Test case passed'])
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
    def verifyUser(self):
        """
        Verifies whether User is created or not
        """
        self.log_data("Verification ::: ")
        
        resVU, statVU = self.getUserInfo(self.refId)                
        if not statVU or resVU is None:
            self.log_data("Unable to fetch User Information %s"%str(resVU))
        elif resVU["userName"] != globalVars.ad_User:
            self.log_data("Unable to verify User Name Actual: '%s' Result: %s"%(self.userName, resVU["userName"]))
        elif resVU["role"] != "Administrator":
            self.log_data("Unable to verify Role Actual: '%s' Result: %s"%("Administrator", resVU["role"]))
        else:
            self.log_data("Successfully verify User Name : '%s'"%(self.userName))
    
    def getUserInfo(self, refId):
        """
        Gets the User Information
        """
        return self.getResponse("GET", "User", refId=refId)    
        
    
    def createUser(self,userName,userDomain="ASMLOCAL"):
        payload = self.readFile(globalVars.userPayload)
        firstName = globalVars.ad_firstname        
        payload = payload.replace("user_name", userName).replace("user_pwd", 
                   "asmUser123").replace("user_domain", userDomain).replace("login_user", self.loginUser).replace("user_role","Administrator").replace("first_name",firstName)           
        
#         self.getResponse("POST", "User", payload)
        return self.getResponse("POST", "User", payload)
    
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.runTestCase()
    os.chdir(current_dir)