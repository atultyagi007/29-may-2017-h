'''
Created on Nov 26, 2014

@author: waseem.irshad
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
import csv
import time
import requests
import xml.etree.ElementTree as ET
from lxml import etree
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import datetime

'''
    # TestCase : To Tear Down resources from a deployment by providing input at run time.
    Pre-requisite : The ResourceType and name of resource should be provided in TearDown.csv file.
'''

class GenericTearDown(TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id = ""
    root = ""
    deploymentRefId=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def getDeploymentResponse(self):
        logger = self.getLoggerInstance()
        logger.info(" ##### Test Case : Generic TearDown #### ")
        deplName = globalVars.depNametoTearDown
        #Get Deployment Id
        self.deploymentRefId = self.getDeploymentId(deplName)
        logger.info(" Deployment Name : ")
        logger.info(deplName)
        logger.info( " Deployment Ref ID : ")
        logger.info(self.deploymentRefId)
        url = self.buildUrl("Deploy",self.deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + self.deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \n"%(url,'GET',headers), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        self.log_data( " Deployment Response GET : ")
        self.log_data( deploymentResponse)
        self.prepareCommonPayload(deploymentResponse)
        self.prepareFinalPayload()
        self.deleteSelectedResources(self.deploymentRefId)
     
    def deleteSelectedResources(self,deploymentRefId):
        logger = self.getLoggerInstance()
        logger.info(" Going To Tear Down resources...")
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        payload=ET.tostring(self.root)
        startTime = datetime.datetime.now()
        self.log_data( " Payload for teardown : ")
        self.log_data(payload)        
        response = requests.put(url, data=payload, headers=headersPut)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        
            
        
    def prepareFinalPayload(self):
        self.log_data( " in fn prepareFinalPayload : ")
        inputFile = globalVars.teardowncsv
        result,status = self.readCsvFile(inputFile)
        header = result[0]      
        resultDict = [dict(zip(header,result[row])) for row in xrange(1,len(result))]
        for resources in resultDict:
            if resources['DeviceType']=='SERVER':
                self.deleteServerPayload(resources)
            elif resources['DeviceType']=='STORAGE':
                self.deleteStoragePayload(resources)
            elif resources['DeviceType']=='CLUSTER':
                self.deleteClusterPayload(resources)
            else:
                print "No Information found in csv "
        
        
    def  deleteServerPayload(self,resources):
        self.log_data( " in fn deleteServerPayload ")
        serverList = []
        [serverList.extend([k,v])for k,v in resources.items()]
        for st in self.root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                if cmpnt.findtext('type')=='SERVER':
                    name = cmpnt.findtext('name')
                    if name in serverList:
                        foo = cmpnt.find('teardown')
                        foo.text = 'true'
                        
                        
    def deleteStoragePayload(self,resources):
        self.log_data( " in deleteStoragePayload ")
        storageList = []
        [storageList.extend([k,v])for k,v in resources.items()]
        for st in self.root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                if cmpnt.findtext('type')=='STORAGE':
                    name = cmpnt.findtext('name')
                    if name in storageList:
                        foo = cmpnt.find('teardown')
                        foo.text = 'true'
                        
                        
    def deleteClusterPayload(self,resources):
        self.log_data( " in deleteClusterPayload ")
        clusterList = []
        [clusterList.extend([k,v])for k,v in resources.items()]
        for st in self.root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                if cmpnt.findtext('type')=='CLUSTER':
                    name = cmpnt.findtext('name')
                    if name in clusterList:
                        foo = cmpnt.find('teardown')
                        foo.text = 'true'
        
        
            
             
        
    def prepareCommonPayload(self,deploymentResponse):
        
        logger = self.getLoggerInstance()
        logger.info(" Preparing common payload for TearDown...")
        self.root = ET.fromstring(deploymentResponse.content)
        print " ROOT : "
        print ET.tostring(self.root)
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


if __name__ == "__main__":
    test = GenericTearDown()
    test.getDeploymentResponse()
    os.chdir(current_dir)
        