'''
Created on May 12 2017

@author: raj.patel
Description:Mark server as failed, find another available to retry deployment ID 
/Deployment/migrateServer/{serviceId}/{componentId}    put
'''

import os
import sys
from datetime import date, datetime
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)

sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/negativeScenario'))

from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Mark server as failed, find another available to retry deployment ID
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        #self.removelogfile()
        self.log_data("Running Test Case ::: ")
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_MigrateServer)
        
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
            
        globalVars.refIdVCenter = vcenterId
        
        templateResponse,payload= self.createTemplateForSpecificUser(payload,vcenterId,storageId) 
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
            self.log_data( 'Successfully created  and published Template ')
            templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
            self.writeFile(globalVars.publishedTemp_filename, templateResult)
            tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
            root = tree.getroot()
            ET.ElementTree(root).write(globalVars.publishedTemp_filename)
            tempPayload=self.readFile(globalVars.publishedTemp_filename)
            print tempPayload
            self.componentId=""
            resInfos,statausDeployedService = self.test_publishTemplate()
            if statausDeployedService:
                self.log_data("Deployed service status")
                self.log_data(statausDeployedService)
                self.log_data( 'Successfully deployed service')
                serviceId=resInfos['id']
                components=resInfos['serviceTemplate']['components']
                for component in components:
                    if component['type']=="SERVER":
                        self.componentId=component['id']
                        break
                refId=serviceId+"/"+self.componentId
                resInfo,status=self.migrateServer("PUT", "MigrateServer",refId=refId)
                
                if status :
                    self.log_data(resInfo)
                    self.log_data("Successfully tested api /Deployment/migrateServer/{serviceId}/{componentId}with put action")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested api /Deployment/migrateServer/{serviceId}/{componentId} with post action','Success','Test case passed'])
                    return True
                else:
                    self.log_data("Test case Failed")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                    return False
                
            
        else:
            self.log_data( 'Failed to create/published template ')
            self.log_TestData(["", "", "",str(self.tc_Id), 'Template with storage','Failed','Failed to create/published template'])
            return False
    def test_publishTemplate(self,specificstandarduser=None):
        
        testCaseID=self.getTestCaseID(__file__)
        self.log_data(testCaseID)
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename)
        DeplName='TestCase_MigrateServer'
        deplyDesc="Migrating server from service"
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc,specificstandarduser=specificstandarduser)
        if deployResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            deploymentRefId = self.getDeploymentId(DeplName)
            loop = 80
            while loop:
                resDS, statDS = self.getResponse("GET","Deploy",refId=deploymentRefId)
                self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, resDS, "Deployment Job Status is :%s"%str(resDS),'', ""])
                if resDS['status'].lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully'])
                    self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                    return resDS,True       
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()

    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)   

