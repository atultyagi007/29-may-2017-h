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
from TestCase_22222 import TestCase

class TestADUser(UtilBase,TestCase):
    tc_Id = "" 
    def __init__(self):
                
        UtilBase.__init__(self)
        TestCase.__init__(self)
        self.loginUser = "admin"
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        
        """
        self.log_data("Pre Run Setup ::: ")
        
        # Delete all Users
        resUR, statUR = self.getResponse("GET", "User")
        if not statUR:
            self.log_data(" Unable to remove all users : %s"%str(resUR))
        else:
            self.log_data(" Successfully removed all users")
            
        # Delete all Directory Services
        resDS, statDS = self.cleanUpDirectoryService(retnStatus=True)
        if not statDS:
            self.log_data("Unable to remove All Directory Services %s"%resDS)
        self.log_data("Successfully Removed All Directory Services")
        self.validateInput()
        
    def validateInput(self):
        """
        Validates AD Parameters
        """
        self.ad_name = globalVars.configInfo["ADInfo"]["adname"]
        if not self.ad_name:
            self.log_data("Active Directory Name is not specified in config.ini")
        self.log_data("Active Directory Name : %s"%self.ad_name)
        
        self.ad_host = globalVars.configInfo["ADInfo"]["adhost"]
        if not self.ad_host:
            self.log_data("Active Directory Host is not specified in config.ini")
        self.log_data("Active Directory Host : %s"%self.ad_host)
        
        self.ad_port = globalVars.configInfo["ADInfo"]["adport"]
        if not self.ad_port:
            self.log_data("Active Directory Port to connect is not specified in config.ini")
        self.log_data("Active Directory Port : %s"%self.ad_port)
        
        self.ad_domain = globalVars.configInfo["ADInfo"]["addomain"]
        if not self.ad_domain:
            self.log_data("Active Directory Domain Name is not specified in config.ini")
        self.log_data("Active Directory Domain Name : %s"%self.ad_domain)
        
        self.ad_de = globalVars.configInfo["ADInfo"]["adde"]
        if not self.ad_de:
            self.log_data("Active Directory Domain Extension is not specified in config.ini")
        self.log_data("Active Directory Domain Extension : %s"%self.ad_de)
        
        adUsername = globalVars.configInfo["ADInfo"]["adusername"]
        if not adUsername:
            self.log_data("Active Directory User Name is not specified in config.ini")
        self.log_data("Active Directory User Name : %s"%adUsername)
        self.ad_binddn = adUsername + "@" + self.ad_domain + "." + self.ad_de
        
        self.ad_password = globalVars.configInfo["ADInfo"]["aduserpwd"]
        if not self.ad_password:
            print("Active Directory User Password is not specified in config.ini")
        print("Active Directory User Password : %s"%self.ad_password)
        
        self.ad_firstname = globalVars.configInfo["ADInfo"]["adfirstname"]
        if not self.ad_firstname:
            self.log_data("Active Directory User First Name is not specified in config.ini")
        self.log_data("Active Directory User First Name : %s"%self.ad_firstname)
        
        self.ad_lastname = globalVars.configInfo["ADInfo"]["adlastname"]
        if not self.ad_lastname:
            self.log_data("Active Directory User Last Name is not specified in config.ini")
        self.log_data("Active Directory User Last Name : %s"%self.ad_lastname)
                
        
    
    def cleanUpDirectoryService(self,retnStatus=False):
        """
        Removes all Directory Services
        """
        resUR, statUR = self.getResponse("GET", "DirectoryService")
        if "No information found" in resUR:
            return "No Directory Services found to Remove", True
        if not statUR:
            return resUR, False
        for temp in resUR["configurationList"]:
            result, status = self.getResponse("DELETE", "DirectoryService", refId=str(temp["seqId"]))
            if not status and retnStatus: 
                return result, status
        if retnStatus:
            return "Successfully Removed All Directory Services", True
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        
        # Create Directory Service
        resDS, statDS = self.createDirectoryService()
        if not statDS:
            self.log_data("Unable to Create Directory Service: %s"%resDS)
        self.dsRefId = str(resDS["seqId"]) 
        self.log_data("Successfully created Directory Service for Host: %s, Reference Id: %s"%(self.ad_host, self.dsRefId))
        
        #Get Users from Active Directory
        resGU, statGU = self.getUsersFromAD()
        if not statGU:
            self.log_data("Unable to get Users from Active Directory: %s"%str(resGU))         
        self.log_data("Successfully Fetched All Users from AD : %s"%str(resGU))
        
        
        #Import All Users
        userPwd = "asmUser123"        
        for user in resGU["activeDirectoryUser"]:
            self.userName = user["userId"]
            resCU, statCU = self.createUSER(self.userName, userPwd, userDomain=self.ad_domain)
            print "resCU : %s"%str(resCU)
            if not statCU:
                self.log_data("Failed to Import User: %s"%self.userName)
            self.log_data("Successfully Imported User: %s"%self.userName)
            
            #Validate User Creation
            self.refId = str(resCU["userSeqId"])
            self.verifyUser()
            
            
    def verifyUser(self):
        
        self.log_data(" Verification :: ")
        
        resVU, statVU = self.getResponse("GET", "User", refId = self.refId)
        print " resVU : %s"%str(resVU)
        if not statVU or resVU is None:
            self.log_data("Unable to fetch User Information %s"%str(resVU))
        elif resVU["userName"] != self.userName:
            self.log_data("Unable to verify User Name Actual: '%s' Result: %s"%(self.userName, resVU["userName"]))
        elif resVU["role"] != "Administrator":
            print ("Unable to verify Role Actual: '%s' Result: %s"%("Administrator", resVU["role"]))
        #elif resVU["domainName"] != self.ad_domain:
         #   print("Unable to verify Domain Actual: '%s' Result: %s"%(self.ad_domain, resVU["domain"]))
        else:
            self.log_data("Able to verify User %s"%self.userName)

    def createDirectoryService(self):
        """
        Creates Directory Service
        """
        payload = self.readFile(globalVars.directoryServicePayload)        
        payload = payload.replace("ad_host",self.ad_host).replace("ad_port", \
                   self.ad_port).replace("ad_name", self.ad_name).replace("ad_binddn", \
                   self.ad_binddn).replace("ad_password", self.ad_password).replace("ad_domain", \
                   self.ad_domain).replace("ad_de", self.ad_de).replace("ad_firstname", \
                   self.ad_firstname).replace("ad_lastname", self.ad_lastname).replace("login_user", self.loginUser)
        resVA, statVA = self.getResponse("POST", "ValidateAD", payload)
        if not statVA:
            return resVA, statVA
        return self.getResponse("POST", "DirectoryService", payload)
    
    def getUsersFromAD(self):
        """
        Gets Users from AD
        """
        refId = self.ad_name + "/user" 
        #print " refId in getUsersFromAD : %s"%str(refId)
        return self.getResponse("GET", "DirectoryService", refId = refId)


if __name__ == "__main__":
    test = TestADUser()
    test.preRunSetup()
    test.runTestCase()
    os.chdir(current_dir)
        

        

        