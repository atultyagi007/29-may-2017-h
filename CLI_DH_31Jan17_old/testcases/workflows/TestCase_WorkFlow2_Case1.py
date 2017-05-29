# -*- coding: utf-8 -*-
'''
Created on Sep 8, 2014

@author: dheeraj_si
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputForWorkFlowCases
import datetime
import requests

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Create a server pool of FC630 blades  
    Create and Publish a template that deploys 2 blade servers to a�HyperV cluster. 
    Give access of the template to Standard user1. Standard user1 Deploy the service�
    Verify that the deployment is successful and the host is added to the cluster with the correct storage
    Standard user1 scales up 2 servers.
    Admin scales up 1 dup server -
    Readonly user2 can login and view running services and templates.\
    Verify that the scaleup deployment is successful.
    Verify that the correct time is set on host and time synch is correct between appliance VM and server
    Scale down a server. -�
    Verify that only the server component is deleted and the deployment is still successful
    Tear down the service.
    Verify that on tear down the service deleted and there are no exception files. The selected components are deleted.
    """ 
    
    
    tc_Id=""
    userSeqId = ""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
    def createStandardUser(self):
        print " Going to create Standard User "
        self.authenticate()
        payload = self.readFile(globalVars.userPayload)        
        payload = payload.replace("user_name", inputForWorkFlowCases.StdUserName_1).replace("user_pwd", inputForWorkFlowCases.Std_User_Password_1).replace("user_domain", 
                    inputForWorkFlowCases.StdUserDomain).replace("user_role",inputForWorkFlowCases.StdUserRole).replace("login_user", "admin")            
        response =  self.getResponse("POST", "User", payload)
        self.userSeqId = response[0]["userSeqId"]
        print " Standard user created with seqID : %s"%str(self.userSeqId)
        
    def preRunSetup(self):
        logger = self.getLoggerInstance()
        logger.info(" Running Pre Run Setup")
        #logger.info(" Cleaning Server Pools.......")
        #respCSP = self.cleanServerPool()
        #logger.info(respCSP)
        logger.info(" Getting the devices : ")
        response =self.getDeviceList()
        discoveredDevicesValue=response[0]
        logger.info( " discoveredDevicesValue : ")
        logger.info(discoveredDevicesValue)
        servers = []
        serverType = serverPoolValue.TestCase_89864
        for device in discoveredDevicesValue:
            devicetype=device['deviceType']
            serverModel=device['model']
            if (devicetype == "BladeServer" and serverType in serverModel):
                servers.append(device)
        
        logger.info(' Servers : ')
        logger.debug(servers)
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
            logger.info('response after createServerPool : ')
            logger.debug(response)
        else:
            print " No BladeServers found of type : '%s'",str(serverType)


        
    def test_createAndPublishTemplate(self):
        print " Going to create and Publish the Template :"
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        
        payload = self.readFile(globalVars.filename_WorkFlow2_Case1)
        self.getResources()
        
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        self.storageRes = self.getReqResource(limit=1, resourceType='STORAGE', deviceType=None)
        if len(self.storageRes) == 0:
            print "Required no. of Storage not available"
        else:
            storageId = self.storageRes[0]["refid"]
        self.vcenterRes = self.getReqResource(limit=1, resourceType='VCENTER', deviceType=None)
        if(len(self.vcenterRes)) == 0:
            print " Required no. of vcenter not found "
        else:
            vcenterId = self.vcenterRes[0]["refid"]
            
        result = self.getResponse("GET", "User", refId=str(self.userSeqId))
        if result is None:
            return " could not get the User information"
        print " Going to replace values for Standard user in payload : "
        payload = payload.replace("$seqId",str(result[0]["userSeqId"])).replace("$StdUserName_1",result[0]["userName"]).replace("$StdUserDomain",result[0]["domainName"]).replace("$StdUserRole",result[0]["role"])
        payload = payload.replace("$serversource",inputForWorkFlowCases.serversource)
        
        serverRefID1=''
        serverRefID2=''
        serverRefID3=''
        serverRefID4=''
        serverRefID5=''
        if inputForWorkFlowCases.serversource == "manual":
            serverList = globalVars.resourceInfo['SERVER']
            print " Going to get refIds for manual selection of servers : "
            print " SERVER LIST : %s"%str(serverList)
            for device in serverList:
                if device['ip']==inputForWorkFlowCases.ServerEntry_20:
                    serverRefID1 = device['refid']
                if device['ip']==inputForWorkFlowCases.ServerEntry_21:
                    serverRefID2 = device['refid']
                if device['ip']==inputForWorkFlowCases.ServerEntry_22:
                    serverRefID3 = device['refid']
                if device['ip']==inputForWorkFlowCases.ServerEntry_23:
                    serverRefID4 = device['refid']
                if device['ip']==inputForWorkFlowCases.ServerEntry_31:
                    serverRefID5 = device['refid']
        
            payload = payload.replace("$ServerEntry_20",serverRefID1).replace("$ServerEntry_21",serverRefID2)
                
        globalVars.refIdVCenter=vcenterId
        globalVars.refIdEQLogic=storageId
        templateResponse= self.createTemplateWorkFlows(payload,vcenterId,storageId) 
        logger.info( "printing the response from the create template")
        logger.debug( templateResponse.content)
        logger.info( "printing the status code")
        logger.info(templateResponse.status_code)
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            ## login by Standard USer
            print " Template published. Going to login by Standard USer "
            self.authenticateForWorkflow(inputForWorkFlowCases.StdUserName_0,inputForWorkFlowCases.Std_User_Password_0)
            
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "", str(testCaseID),'Success','Template created  and published Successfully','Server : Blade Server'])
            self.log_data( 'Successfully created  and published Template ')
            self.test_deployTemplate(serverRefID3, serverRefID4,serverRefID5)
            
        else:
            self.log_TestData(["", "", "", str(testCaseID),'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
    def test_deployTemplate(self, refIdServer3 = None,refIdServer4 = None, refIdServer5=None):
        print " Going to Deploy Template by Standard User : "
        logger = self.getLoggerInstance()
        logger.info("getting published templateID in test_publishTemplate ")
        logger.info(globalVars.publishedTemplateID)
        testCaseID=self.getTestCaseID(__file__) 
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='WorkFlow2_Case1'
        deplyDesc='Deploy 2 Blade server with 2 new Volume to a SCCVM and scale up with 2 server'
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
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID),'Failed','Deployment Service Failed','Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        break
            loop -= 1
            self.log_data( 'Going to call the ScaleUp  for the testcase id : %s'%str(testCaseID))
            self.test_scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc, refIdServer3, refIdServer4, refIdServer5)
            
        
        else:
            self.log_TestData(["", "", "", str(testCaseID),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
    
    def test_scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, refIdServer3 = None,refIdServer4 = None,refIdServer5 = None):
        print " Going to scaleUp Template by Standard User : "
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        payload = self.readFile(globalVars.filename_WorkFlow2_Case1_scaleup)
        payload = payload.replace("$serversource",inputForWorkFlowCases.serversource)
        if (inputForWorkFlowCases.serversource == "manual"):
            payload = payload.replace("$ServerEntry_20",refIdServer3).replace("$ServerEntry_21",refIdServer4)

        scaleUpResponse = self.scaleUpdeployWorkFlowsTemplate(templateID, deploymentRefId, DeplName, deplyDesc, payload)
        
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
                        self.log_TestData(["", "", "",str(testCaseID),'Success',' scaleUp Template by Standard USer Deployed Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Service by Standard User for the Deployment Name : %s'%DeplName)
                        
                        #self.log_data( 'Now going to call the teardown of service ')
                        #self.cleanDeployedService(deploymentRefId)
                        #self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment ScaleUped Service Failed', 'Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
            loop -= 1
            self.log_data( 'Going to ScaleUp by Admin  for the testcase id : %s'%str(testCaseID))
            self.test_scaleUpTemplatebyAdmin(templateID, deploymentRefId, DeplName, deplyDesc,refIdServer5)
        else:
            self.log_TestData(["", "", "",str(testCaseID),'Failed','Failed to deploy scaleUp Service'])
            self.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)
            
    def test_scaleUpTemplatebyAdmin(self, templateID, deploymentRefId, DeplName, deplyDesc, refIdServer5=None):
        print "Going to login by ADMIN to scaleUp Template  : "
        self.authenticate()
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        payload = self.readFile(globalVars.filename_WorkFlow2_Case2_scaleup_admin)
        payload = payload.replace("$serversource",inputForWorkFlowCases.serversource).replace("$ServerEntry_31",refIdServer5)
        
        scaleUpResponse = self.scaleUpdeployWorkFlowsTemplate(templateID, deploymentRefId, DeplName, deplyDesc, payload)
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
                        self.log_TestData(["", "", "",str(testCaseID),'Success',' scaleUp Template by Admin Deployed Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Service by Admin for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to scale down a server ')
                        self.scaleDownOneServer(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment ScaleUped Service Failed', 'Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(testCaseID),'Failed','Failed to deploy scaleUp Service'])
            self.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)

    
    def scaleDownOneServer(self,deploymentRefId):
        print " Going to Scale Down a Server : "
        logger = self.getLoggerInstance()
        isServer = False
        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \n"%(url,'GET',headers), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)        
        root = ET.fromstring(deploymentResponse.content)
        indTD = self.root.find('individualTeardown')
        indTD.text = 'true'
        td = self.root.find('teardown')
        td.text = 'true'
        canCancl = self.root.find('canCancel')
        canCancl.text = 'true'
        canDel = self.root.find('canDelete')
        canDel.text = 'true'
        canDelRes = self.root.find('canDeleteResources')
        canDelRes.text = 'true'
        canEd = self.root.find('canEdit')
        canEd.text = 'true'
        canRet = self.root.find('canRetry')
        canRet.text = 'true'
        canSupAp = self.root.find('canScaleupApplication')
        canSupAp.text = 'true'
        for st in root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                if cmpnt.findtext('type')=='SERVER':
                    foo = cmpnt.find('teardown')
                    foo.text = 'true'
                    isServer=True
                    break
        if isServer==True:
            logger.info(" Going To Tear Down resources...")
            headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            payload=ET.tostring(root)
            startTime = datetime.datetime.now()
            response = requests.put(url, data=payload, headers=headersPut)
            endTime = datetime.datetime.now()
            elapsedTime="%s"%(endTime-startTime)
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
            print " ScaleDown Response code : %s"%str(response.status_code)
            

    
    def test_cleaneServerPool(self):
        
        response = self.cleanServerPool()
        logger = self.getLoggerInstance()
        logger.debug('Clean Server Pool Response is')
        logger.info(response)
        
    def test_cleanDeployedTemplates(self, deploymentRefId):
        
        response = self.teardownServices(deploymentRefId)
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))

                
    
            
        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))

    

if __name__ == "__main__":
    test = Testcase()
    test.createStandardUser()
    #test.preRunSetup()
    test.test_createAndPublishTemplate()  

