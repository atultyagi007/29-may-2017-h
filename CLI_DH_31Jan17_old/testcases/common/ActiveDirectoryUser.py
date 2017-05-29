'''
Created on Oct 27, 2015

@author: waseem.irshad
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
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from utilityModule import UtilBase
import globalVars
from TestCase_22222 import TestCase

class Testcase(UtilBase,TestCase): 
    """
    Import Users from Active Directory to manage ASM
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
    
    
            
                
        
    def cleanUpDirectoryService(self,retnStatus=False):
        """
        Removes all Directory Services
        """
        resUR, statUR = self.getResponse("GET", "DirectoryService")
        if "No information found" in resUR:
            return "No Directory Services found to Remove", True
        if not statUR:
            return resUR, False
        print " Response GET DirectoryService : %s"%str(resUR)
        for temp in resUR:
            result, status = self.getResponse("DELETE", "DirectoryService", refId=str(temp["seqId"]))
            if not status and retnStatus: 
                return result, status
        if retnStatus:
            return "Successfully Removed All Directory Services", True
        
    
    
    
    def createDirectoryService(self):
        """
        Creates Directory Service
        """
        self.log_data(" Validating the Inputs :: ")
        self.ad_name = globalVars.configInfo["ADInfo"]["adname"]
        if not self.ad_name:
            self.log_data("Active Directory Name is not specified in config.ini")
        self.log_data("Active Directory Name : %s"%self.ad_name)
        
        ad_host = globalVars.configInfo["ADInfo"]["adhost"]
        if not ad_host:
            self.log_data("Active Directory Host is not specified in config.ini")
        self.log_data("Active Directory Host : %s"%ad_host)
        
        ad_port = globalVars.configInfo["ADInfo"]["adport"]
        if not ad_port:
            self.log_data("Active Directory Port to connect is not specified in config.ini")
        self.log_data("Active Directory Port : %s"%ad_port)
        
        ad_domain = globalVars.configInfo["ADInfo"]["addomain"]
        if not ad_domain:
            self.log_data("Active Directory Domain Name is not specified in config.ini")
        self.log_data("Active Directory Domain Name : %s"%ad_domain)
        
        ad_de = globalVars.configInfo["ADInfo"]["adde"]
        if not ad_de:
            self.log_data("Active Directory Domain Extension is not specified in config.ini")
        self.log_data("Active Directory Domain Extension : %s"%ad_de)
        
        adUsername = globalVars.configInfo["ADInfo"]["adusername"]
        if not adUsername:
            self.log_data("Active Directory User Name is not specified in config.ini")
        self.log_data("Active Directory User Name : %s"%adUsername)
        ad_binddn = adUsername + "@" + ad_domain + "." + ad_de + ".net"
        
        ad_password = globalVars.configInfo["ADInfo"]["aduserpwd"]
        if not ad_password:
            self.log_data("Active Directory User Password is not specified in config.ini")
        self.log_data("Active Directory User Password : %s"%ad_password)
        
        ad_firstname = globalVars.configInfo["ADInfo"]["adfirstname"]
        if not ad_firstname:
            self.log_data("Active Directory User First Name is not specified in config.ini")
        self.log_data("Active Directory User First Name : %s"%ad_firstname)
        
        ad_lastname = globalVars.configInfo["ADInfo"]["adlastname"]
        if not ad_lastname:
            self.log_data("Active Directory User Last Name is not specified in config.ini")
        self.log_data("Active Directory User Last Name : %s"%ad_lastname)

        payload = self.readFile(globalVars.directoryServicePayload)        
        payload = payload.replace("ad_host",ad_host).replace("ad_port", \
                   ad_port).replace("ad_name", self.ad_name).replace("ad_binddn", \
                   ad_binddn).replace("ad_password", ad_password).replace("ad_domain", \
                   ad_domain).replace("ad_de", ad_de).replace("ad_firstname", \
                   ad_firstname).replace("ad_lastname", ad_lastname).replace("login_user", self.loginUser)
#         resVA, statVA = self.getResponse("POST", "ValidateAD", payload)
#         print " \n Response of ValidateAD : %s"%str(resVA)
#         if not statVA:
#             return resVA, statVA
        return self.getResponse("POST", "DirectoryService", payload)
    
    
    def verifyUser(self):
        """
        Verifies whether Server has discovered or not
        """
        self.log_data("Verification ::: ")
        
        resVU, statVU = self.getUserInfo(self.refId)                
        if not statVU or resVU is None:
            self.log_data("Unable to fetch User Information %s"%str(resVU))
        elif resVU["userName"] != "hcl123":
            self.log_data("Unable to verify User Name Actual: '%s' Result: %s"%(self.userName, resVU["userName"]))
        elif resVU["role"] != "Administrator":
            self.log_data("Unable to verify Role Actual: '%s' Result: %s"%("Administrator", resVU["role"]))
#         elif resVU["domainName"] != self.ad_domain:
#             self.log_data("Unable to verify Domain Actual: '%s' Result: %s"%(self.ad_domain, resVU["domain"]))
#         self.log_data("Able to verify User %s"%self.userName)
    
    def getUserInfo(self, refId):
        """
        Gets the User Information
        """
        return self.getResponse("GET", "User", refId=refId)
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        
        # Create Directory Service
        resDS, statDS = self.createDirectoryService()
        if not statDS:
            self.log_data("Unable to Create Directory Service: %s"%resDS)
        print " \n Directory service POST response : %s"%str(resDS)
        self.dsRefId = str(resDS["seqId"]) 
        self.log_data("Successfully created Directory Service")
        
        #Get Users from Active Directory
        resGU, statGU = self.getUsersFromAD()
        if not statGU:
            self.log_data("Unable to get Users from Active Directory: %s"%str(resGU))         
        self.log_data("Successfully Fetched All Users from AD")
        print "\n\n GET ActiveDirectory Response : %s"%str(resGU)
        #Import All Users
        #userPwd = "asmUser123"        
        userName = globalVars.ad_User
        resCU, statCU = self.createUser(userName, userDomain=self.ad_name)
        if not statCU:
            self.log_data("Failed to Import User: %s"%userName)
        self.log_data("Successfully Imported User: %s"%userName)
            
            #Validate User Creation
        self.refId = str(resCU["userSeqId"])
        self.verifyUser()
        
    
    def createUser(self,userName,userDomain):
        payload = self.readFile(globalVars.userPayload)
        firstName = globalVars.ad_firstname        
        payload = payload.replace("user_name", userName).replace("<password></password>", 
                   "").replace("user_domain", userDomain).replace("login_user", self.loginUser).replace("user_role","Administrator").replace("first_name",firstName)           
        return self.getResponse("POST", "User", payload)
    
    def getUsersFromAD(self):
        """
        Gets Users from AD
        """
        resAD,stat = self.getResponse("GET", "DirectoryService")
        print " \n AD Response : %s"%str(resAD)
        for i in resAD:
            if i["name"]==self.ad_name:
                refId = i["seqId"]  #WASAD/user
        #print " refId in getUsersFromAD : %s"%str(refId)
        return self.getResponse("GET", "DirectoryService", refId = str(refId))
    
                
         
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.cleanUpDirectoryService()
    test.runTestCase()
