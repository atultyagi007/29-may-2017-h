'''
Created on Sep 23, 2014

@author: waseem.irshad
'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from utilityModule import UtilBase
import globalVars

class TestCase(UtilBase):
    
    noOfUsersTOCreate = 5
    tc_Id = ""
    def __init__(self):
        
        UtilBase.__init__(self)
        self.loginUser = "admin"
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        
        self.log_data(" running Pre run setUp : Cleaning Users ")
        #resUR, statUR = runService("GET", "User")
        resUR, statUR = self.cleanUpUsers(retnStatus=True)
        if not statUR:
            self.log_data( " Unable to remove all users : %s"%str(resUR))
        else:
            self.log_data( " Successfully removed all users")
            
    def cleanUpUsers(self,retnStatus=False):
        """
        Removes all Users
        """
        resUR, statUR = self.getResponse("GET", "User")
        if not statUR:
            return resUR, False
        for temp in resUR:            
            if temp["userName"] != "admin":
                result, status = self.getResponse("DELETE", "User", refId=str(temp["userSeqId"]))
                if not status and retnStatus: 
                    return result, status
        if retnStatus:
            return "Successfully Removed All Users", True

        
    def runTestCase(self):
        
        self.log_data( " Running Test Case :: ")
        self.userCount = int(self.noOfUsersTOCreate)
        self.userPwd = "asmUser123"
        userDomain = "ASMLOCAL"
        
        for user in xrange(1, self.userCount + 1):
            self.userName = "asmUser" + str(user)
            resCU, statCU = self.createUSER(self.userName, self.userPwd, userDomain)
            if not statCU:
                self.log_data( " Failed to Create User: %s"%self.userName)
            else:
                self.log_data( "Successfully Created User: %s"%self.userName)
            
            # Validate User Creation
            self.refId = str(resCU["userSeqId"])
            self.verifyUser()

            
    def createUSER(self,userName, userPwd, userDomain):
        
        payload = self.readFile(globalVars.userPayload)        
        payload = payload.replace("user_name", userName).replace("user_pwd", 
                   userPwd).replace("user_domain", userDomain).replace("login_user", self.loginUser)            
        return self.getResponse("POST", "User", payload) 
        
        
    def verifyUser(self):
        
        self.log_data( " Verification :: ")
        
        resVU, statVU = self.getResponse("GET", "User", refId = self.refId)
        if not statVU or resVU is None:
            self.log_data( "Unable to fetch User Information %s"%str(resVU))
        elif resVU["userName"] != self.userName:
            self.log_data( "Unable to verify User Name Actual: '%s' Result: %s"%(self.userName, resVU["userName"]))
        elif resVU["role"] != "Administrator":
            self.log_data ("Unable to verify Role Actual: '%s' Result: %s"%("Administrator", resVU["role"]))
        else:
            self.log_data ("Able to verify User %s"%self.userName)
            

if __name__ == "__main__":
    test = TestCase()
    test.preRunSetup()
    test.runTestCase()
    os.chdir(current_dir)
            
        
        