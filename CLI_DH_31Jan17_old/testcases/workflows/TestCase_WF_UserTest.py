'''
Created on Jun 3, 2015

@author: waseem.irshad
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
import globalVars
from utilityModule import UtilBase
from TemplateBaseClass import TemplateTestBase
import json
import xml.etree.cElementTree as ET
import time

class TestUser(UtilBase,TemplateTestBase):
    
    tc_Id=""
    userSeqId = ""
    def __init__(self):
        UtilBase.__init__(self)
        TemplateTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def createStandardUser(self):
        self.authenticate()
        userName = globalVars.user_name
        userPwd = globalVars.userPwd
        user_role = globalVars.user_role
        userDomain = globalVars.userDomain
        
        payload = self.readFile(globalVars.userPayload)        
        payload = payload.replace("user_name", userName).replace("user_pwd", userPwd).replace("user_domain", 
                    userDomain).replace("user_role",user_role).replace("login_user", "admin")            
        response =  self.getResponse("POST", "User", payload)
        print " RESPONSE : "
        print response
        self.userSeqId = response[0]["userSeqId"]
        
        
    def createAndPublishTemplate(self):
        #self.authenticateForWorkflow()
        logger = self.getLoggerInstance()
        self.setNetworkList()
        result = self.getResponse("GET", "User", refId=str(self.userSeqId))
        if result is None:
            return " could not get the User information"
        payload = self.readFile(globalVars.workFlowUserPayload)
        payload = payload.replace("$seqId",str(result[0]["userSeqId"])).replace("$StdUserName",result[0]["userName"]).replace("$StdUserDomain",result[0]["domainName"]).replace("$StdUserRole",result[0]["role"])
        templateResponse= self.createTemplateEsxi(payload,"","")
        print " TemplateResponse : "
        print templateResponse
        testCaseID=self.getTestCaseID(__file__)
        
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            self.authenticateForWorkflow()
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
            
            templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
            self.writeFile(globalVars.publishedTemp_filename, templateResult)
            tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
            root = tree.getroot()
            ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
            DeplName='Standard_User_scaleUP'
            deplyDesc='deploy a server with Standard User and scaleUp one server by standard user'
            deployResponse = self.deployTemplate(DeplName,deplyDesc)
            logger.info( "printing the response from the deploy template")
            logger.debug( deployResponse.content)
            logger.info( "printing the status code")
            logger.info( deployResponse.status_code)
            if deployResponse.status_code in (200, 201, 202, 203, 204):
                #Get Deployment Id
                deploymentRefId = self.getDeploymentId(DeplName)
                loop = 60
                deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
                deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
                while loop:
                    resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                    if resDS.lower() in ("in_progress"):
                        time.sleep(120)
                    else:
                        if resDS.lower() in ("complete"):
                            print "Deployment Status: %s"%resDS
                            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully by STANDARD USER','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                            self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                            break
                        else:
                            print "Deployment Status: %s"%resDS
                            self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment Service Failed','Server : Blade Server' , "deploymentLogPath: %s"%deploymentLogPath])
                            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                            break
                loop -= 1
                self.log_data( 'Going to call the ScaleUp  for the testcase id : %s'%str(testCaseID))
                self.test_scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc)
            else:
                self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment Service Failed'])
                self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                    
                    
            
    def test_scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc):
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        scaleUpResponse = self.scaleUpdeployEsxiTemplate(templateID, deploymentRefId, DeplName, deplyDesc, globalVars.Std_User_scaleUp_payload)
        logger.info( "printing the response from the scaleUp deploy template")
        logger.debug( scaleUpResponse.content)
        logger.info( "printing the status code")
        logger.info( scaleUpResponse.status_code)
        if scaleUpResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            #deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(testCaseID),'Success',' scaleUp Template Deployed Successfully by STANDARD USER', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Service for the Deployment Name : %s'%DeplName)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment ScaleUped Service Failed', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(testCaseID),'Failed','Failed to deploy scaleUp Service'])
            self.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)



            
    
if __name__ == "__main__":
    test = TestUser()
    test.createStandardUser()
    test.createAndPublishTemplate()
    


        