'''
Created on Oct 12, 2015

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
import globalVars
from DiscoverResourceBaseClass import DiscoverResourceTestBase
from utilityModule import UtilBase
import chassisConfigParam
import templateInputReqValueHyperV
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping
import json
import time
import xml.etree.cElementTree as ET




class TestCaseDefineTemplate(TemplateTestBase,DiscoverResourceTestBase,UtilBase):
    
    tc_Id=""
    def __init__(self):
        
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        #self.authenticate()
        
        
        
   
        
    def defineTemplate(self):
        StorageComponentCount = globalVars.NoOfStorage
        print StorageComponentCount
        ServerComponentCount = globalVars.NoOfServer
       
            
        finalPayload = ""    
        payload = self.readFile(globalVars.CommonTemplatePayload)
        finalStoragePayload=""
        finalServerPayload=""
        finalClusterPayload=""
        finalVMPayload=""
        finalApplicationPayload=""
        serverRelatedComponentFinalPayload=""
        storageRelatedComponentFinalPayload=""
        clusterRelatedComponentFinalPayload=""
        
        K=0
        
        j =1    
        while StorageComponentCount:
            
            relatedComponentpayload = ""
            relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
            relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Storage_Components_Id+str(j)).replace("$Resource_Name",globalVars.StorageComponent_Name+str(j))
            storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
            
            storageComponentpayload = ""
            storageComponentpayload = self.readFile(globalVars.StorageComponentPayload)
            storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.Storage_Components_Id+str(j)).replace("$Storage_name",globalVars.StorageComponent_Name+str(j)).replace("$Storage_componentID",globalVars.Storage_componentID+str(K)).replace("$VolumeValue","$Volume_"+str(j))
            finalStoragePayload = finalStoragePayload + storageComponentpayload
            StorageComponentCount -= 1
            j +=1
            K +=1
        
        i =1    
        while ServerComponentCount:
            
            relatedComponentpayload = ""
            relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
            relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Server_Components_Id+str(i)).replace("$Resource_Name",globalVars.ServerComponent_Name+str(i))
            serverRelatedComponentFinalPayload = serverRelatedComponentFinalPayload + relatedComponentpayload
            
            serverComponentpayload = ""
            serverComponentpayload = self.readFile(globalVars.ServerComponentPayload)
            serverComponentpayload = serverComponentpayload.replace("$Server_Components_Id",globalVars.Server_Components_Id+str(i)).replace("$ServerComponent_Name",globalVars.ServerComponent_Name+str(i)).replace("$Server_componentID",globalVars.Server_componentID+str(K))
            finalServerPayload = finalServerPayload + serverComponentpayload
            ServerComponentCount -= 1
            i +=1
            K +=1
            
        
       
            
            
        
        finalStoragePayload = finalStoragePayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload)
        finalServerPayload = finalServerPayload.replace("$StorageComponentsEntry",storageRelatedComponentFinalPayload).replace("$ClusterComponentsEntry",clusterRelatedComponentFinalPayload)
        
        finalPayload = payload.replace("$template_name",globalVars.template_name).replace("$template_description",globalVars.template_description).replace("$login_user",globalVars.userName).replace("$StorageComponent",finalStoragePayload).replace("$ServerComponent",finalServerPayload).replace("$ClusterComponent",finalClusterPayload).replace("$VMComponent",finalVMPayload).replace("$ApplicationComponent",finalApplicationPayload)
        
        
        
        print "-------------------------"
        print "final paylaad"
        
        print finalPayload
            
    def test_createTemplate(self):
        
        statausCreateTemplae = False
        response = self.authenticate()
        self.setNetworkList()
        
        StorageComponentCount = globalVars.NoOfStorage
        print StorageComponentCount
        ServerComponentCount = globalVars.NoOfServer
        clusterComponentCount=  globalVars.NoOfCluster
        vmComponentCount= globalVars.NoOfVM
        applicationComponentCount = globalVars.NoOfApplication
       
            
        finalPayload = ""    
        payload = self.readFile(globalVars.CommonTemplatePayload)
        finalStoragePayload=""
        finalServerPayload=""
        finalClusterPayload=""
        finalVMPayload=""
        finalApplicationPayload=""
        serverRelatedComponentFinalPayload=""
        storageRelatedComponentFinalPayload=""
        clusterRelatedComponentFinalPayload=""
        vmRelatedComponentFinalPayload=""
        ApplicationRelatedComponentFinalPayload=""
        
        K=0
        
        j =1    
        while StorageComponentCount:
            
            if globalVars.typeOfStorage == "EqualLogic":
                
                relatedComponentpayload = ""
                relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.EqualLogic_Components_Id+str(j)).replace("$Resource_Name",globalVars.EqualLogicComponent_Name+str(j))
                storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
            
                storageComponentpayload = ""
                storageComponentpayload = self.readFile(globalVars.EquallogicComponentPayload)
                storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.EqualLogic_Components_Id+str(j)).replace("$Storage_name",globalVars.EqualLogicComponent_Name+str(j)).replace("$Storage_componentID",globalVars.EqualLogic_componentID+str(K)).replace("$VolumeValue","$Volume_"+str(j)).replace("$VolumeSize","$VolumeSize_"+str(j))
                finalStoragePayload = finalStoragePayload + storageComponentpayload
                StorageComponentCount -= 1
                
            elif globalVars.typeOfStorage == "Compellent":
                relatedComponentpayload = ""
                relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Compellent_Components_Id+str(j)).replace("$Resource_Name",globalVars.CompellentComponent_Name+str(j))
                storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
            
                storageComponentpayload = ""
                storageComponentpayload = self.readFile(globalVars.CompellentComponentPayload)
                storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.Compellent_Components_Id+str(j)).replace("$Storage_name",globalVars.CompellentComponent_Name+str(j)).replace("$Storage_componentID",globalVars.Compellent_componentID+str(K)).replace("$VolumeValue","$Volume_"+str(j)).replace("$VolumeSize","$VolumeSize_"+str(j))
                finalStoragePayload = finalStoragePayload + storageComponentpayload
                StorageComponentCount -= 1
                
                
            j +=1
            K +=1
        
        i =1    
        while ServerComponentCount:
            
            relatedComponentpayload = ""
            relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
            relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Server_Components_Id+str(i)).replace("$Resource_Name",globalVars.ServerComponent_Name+str(i))
            serverRelatedComponentFinalPayload = serverRelatedComponentFinalPayload + relatedComponentpayload
            
            serverComponentpayload = ""
            serverComponentpayload = self.readFile(globalVars.ServerComponentPayload)
            serverComponentpayload = serverComponentpayload.replace("$Server_Components_Id",globalVars.Server_Components_Id+str(i)).replace("$ServerComponent_Name",globalVars.ServerComponent_Name+str(i)).replace("$Server_componentID",globalVars.Server_componentID+str(K))
            finalServerPayload = finalServerPayload + serverComponentpayload
            ServerComponentCount -= 1
            i +=1
            K +=1
            
        m=1
        while clusterComponentCount:
            
            if globalVars.typeOfCluster == "VMWareCluster":
                
                relatedComponentpayload = ""
                relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.VMWareCluster_Components_Id+str(m)).replace("$Resource_Name",globalVars.VMWareClusterComponent_Name+str(m))
                clusterRelatedComponentFinalPayload = clusterRelatedComponentFinalPayload + relatedComponentpayload
            
                clusterComponentpayload = ""
                clusterComponentpayload = self.readFile(globalVars.VMWareClusterComponentPayload)
                clusterComponentpayload = clusterComponentpayload.replace("$Cluster_Components_Id",globalVars.VMWareCluster_Components_Id+str(m)).replace("$Cluster_name",globalVars.VMWareClusterComponent_Name+str(m)).replace("$Cluster_componentID",globalVars.VMWareCluster_componentID+str(K))
                finalClusterPayload = finalClusterPayload + clusterComponentpayload
                clusterComponentCount -= 1
                
            elif globalVars.typeOfCluster == "HyperVCluster":
                relatedComponentpayload = ""
                relatedComponentpayload = self.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.HyperVCluster_Components_Id+str(m)).replace("$Resource_Name",globalVars.HyperVClusterComponent_Name+str(m))
                clusterRelatedComponentFinalPayload = clusterRelatedComponentFinalPayload + relatedComponentpayload
            
                clusterComponentpayload = ""
                clusterComponentpayload = self.readFile(globalVars.HyperVClusterComponentPayload)
                clusterComponentpayload = storageComponentpayload.replace("$Cluster_Components_Id",globalVars.HyperVCluster_Components_Id+str(m)).replace("$Cluster_name",globalVars.HyperVClusterComponent_Name+str(m)).replace("$Cluster_componentID",globalVars.HyperVCluster_componentID+str(K))
                finalClusterPayload = finalClusterPayload + clusterComponentpayload
                clusterComponentCount -= 1
                
                
            m +=1
            K +=1
        
            
            
        
        finalStoragePayload = finalStoragePayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload)
        finalServerPayload = finalServerPayload.replace("$StorageComponentsEntry",storageRelatedComponentFinalPayload).replace("$ClusterComponentsEntry",clusterRelatedComponentFinalPayload)
        finalClusterPayload = finalClusterPayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload).replace("$VMComponentsEntry",vmRelatedComponentFinalPayload)
        
        finalPayload = payload.replace("$template_name",globalVars.template_name).replace("$template_description",globalVars.template_description).replace("$login_user",globalVars.userName).replace("$StorageComponent",finalStoragePayload).replace("$ServerComponent",finalServerPayload).replace("$ClusterComponent",finalClusterPayload).replace("$VMComponent",finalVMPayload).replace("$ApplicationComponent",finalApplicationPayload)
        
        self.getResources()
        finalPayload=finalPayload.replace("$networkConfiguration",networkConfiguration.Rack_Esxi_2PORT)
    
        networkTypeServer1=globalVars.TestCase_networkType
        
        
        if not networkTypeServer1:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkTypeServer1=='Rack_Esxi_2PORT':
            finalPayload=finalPayload.replace("$networkConfiguration1",networkConfiguration.Rack_Esxi_2PORT)
        elif networkTypeServer1=='Rack_Esxi_4PORT':
            finalPayload=finalPayload.replace("$networkConfiguration1",networkConfiguration.Rack_Esxi_4PORT)
        elif networkTypeServer1=='Blade_Esxi_2PORT':
            finalPayload=finalPayload.replace("$networkConfiguration1",networkConfiguration.Blade_Esxi_2PORT)
        elif networkTypeServer1=='Blade_Esxi_4PORT':
            finalPayload=finalPayload.replace("$networkConfiguration1",networkConfiguration.Blade_Esxi_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( ' ERROR : missing network configuration for the test case ')
            return
        
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        
        if globalVars.typeOfStorage == "EqualLogic":
            self.storageRes = self.getReqResource(limit=1, resourceType='STORAGE', deviceType=None)
            if len(self.storageRes) == 0:
                print "Required no. of Storage not available"
            else:
                storageId = self.storageRes[0]["refid"]
        
        elif globalVars.typeOfStorage == "Compellent":
            self.storageRes = self.getReqResource(limit=1, resourceType='COMPELLENT', deviceType=None)
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
        
        templateResponse= self.createTemplateEsxi(finalPayload,vcenterId,storageId) 
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(self.tc_Id), globalVars.template_description, 'Success','Template created  and published Successfully','Server : Blade Server'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), globalVars.template_description, 'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        statausDeploy = False
        DeplName=globalVars.template_name
        deplyDesc=globalVars.template_description
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
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
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Going to do VCenter Validation before TearDown ')
                        self.doVCenterValidations(globalVars.refIdVCenter)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        self.log_data( 'Going to do VCenter Validation after TearDown')
                        self.doVCenterValidations(globalVars.refIdVCenter)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        self.runWebServiceAPI(str(self.tc_Id), "Fail", "Run in regression test")
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
        
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


    
    def test_JsonFile(self):
        
#         relatedComponentpayload = self.readFile(globalVars.jsonPayload)
#         
#         data = json.loads(relatedComponentpayload.text)
#         result =self.convertUTA(data)
#         #resultDS = result["status"]
#         print result

       
       
#         with open(globalVars.jsonPayload1) as data_file:    
#             data = json.load(data_file)
#             print data

        json_data=open(globalVars.jsonPayload).read()

        data = json.loads(json_data)
        result =self.convertUTA(data)
        print result
        print "=============="
        print result["Server"]["Os_Image_Version"]
        models = result["Server"]["Model"]
        
        print "models[0]: ", models[0]
        for model in models:
            print model
        print result["Template"]["Name"]
        
        print "+++++++++++"
        print result["Scaleup"]["Server"]["Model"]
 
                    

        
        
if __name__=="__main__":
    test = TestCaseDefineTemplate()
    test.test_JsonFile()
    
#     test.defineTemplate()
#     test.getCSVHeader()
#     #test.preRunSetup()
#     status = test.test_createTemplate()
#     if status==True:
#         os.chdir(current_dir)
#         sys.exit(0)
#     else:
#         os.chdir(current_dir)
#         sys.exit(1)  
