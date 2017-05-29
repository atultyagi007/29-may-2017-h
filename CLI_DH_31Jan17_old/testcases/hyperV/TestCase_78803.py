'''
Created on Aug 18, 2014

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
import testCaseDescriptionMapping
import csv
import time
import xml.etree.cElementTree as ET
import serverPoolValue
import DiscoverResourceBaseClass
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import templateInputReqValueHyperV, networkConfiguration, inputForNetworkType


class Testcase(TemplateTestBase):
    """
    Deploy a HyperV non-R2 instance on an R720
    """ 
    tc_Id = ""
        
    
    def __init__(self):
        TemplateTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def test_createTemplate(self):
        #testCaseID=self.getTestCaseID(__file__)
        self.log_data('TestCase ID is %s'%str(self.tc_Id))
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        logger.info(" Running TestCase :: ")
        logger.info( "testcase id is")
        logger.info(self.tc_Id)
        
        payload = self.readFile(globalVars.filename_TestCase_78803)
        networkType=inputForNetworkType.TestCase_78803_networkType
        statausCreateTemplae = False
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        else:
            if networkType=='RackHperV_2Port':
                payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_2Port)
            elif networkType=='RackHperV_4Port':
                payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_4Port)
            elif networkType=='BladeHyperV_2Port':
                payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_2Port)
            elif networkType=='BladeHyperV_4Port':
                payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_4Port)
            else:
                print " ERROR : missing network configuration for the test case "
                self.log_data( 'ERROR : missing network configuration for the test case ')
                return
        templateResponse= self.createTemplate(payload)
        #print "template response" 
        self.log_data("template response :%s"%str(templateResponse))
#         print templateResponse
#         print "template response code"
#         print templateResponse.status_code
#         print json.loads(templateResponse.text)
        logger.info( "Print create template response" )
        logger.debug(templateResponse.content)
        logger.info("Print status code of the create template : ")    
        logger.info(templateResponse.status_code)  
        oFile = open("testInfo.csv","wb")
        csv_writer = csv.writer(oFile,delimiter=",")
        csv_writer.writerow(["TestCaseId","Status","Description"])
        oFile.close()
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
            
            logger.info( "printing published template ID : " )
            logger.info( globalVars.publishedTemplateID)
            
                        
            self.log_TestData(["", "", "", str(self.tc_Id), testCaseDescriptionMapping.TestCase_78803, 'Success','Template created  and published Successfully','Server : R720 Rackserver'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id), testCaseDescriptionMapping.TestCase_78803, 'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        logger = self.getLoggerInstance()
        logger.info("getting published templateID in test_publishTemplate ")
        logger.info(globalVars.publishedTemplateID)
        
        #testCaseID=self.getTestCaseID(__file__)
#         response = self.authenticate()
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_78803'
        deplyDesc=testCaseDescriptionMapping.TestCase_78803
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        logger.info( "printing the response from the deploy template")
        logger.debug( deployResponse.content)
        logger.info( "printing the status code")
        logger.info( deployResponse.status_code)
        
        statausDeploy = False
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
                        statausDeploy = True
                        self.log_TestData(["", "", "", str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
                    else:
                        self.log_TestData(["", "", "", str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
        
    def test_cleaneServerPool(self):
        
        response = self.cleanServerPool()
        logger = self.getLoggerInstance()
        logger.debug('Cleane Server Pool Response is')
        logger.info(response)
        

    def test_cleanDeployedService(self):
        
        self.log_data( 'Now going to call the teardown of service ')
        response = self.cleanUpServices()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))     
        
    def test_cleanePublishedTemplates(self):
        
        self.log_data( 'Now going to call the teardown of Template ')
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))
        
    def test_cleanDeployedTemplates(self, deploymentRefId):
        
        response = self.teardownServices(deploymentRefId)
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))

        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    test.test_createTemplate()
    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1) 