
'''
Created on Aug 21, 2015

@author: waseem.irshad
'''
from createdeploytemplate import chassisConfigParam

'''
    Test Case Description : Add new CMC user with Power User group
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

class TestCMCUser(TemplateTestBase,DiscoverResourceTestBase,UtilBase):
    
    tc_Id=""
    def __init__(self):
        
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        self.authenticate()
        
    def createCMCUser(self):
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        print " Managed Device Response : %s"%str(resMD)
        cmcUser = '''[{"username": "Poweruser1","password": "power123","role": "PowerUser" ,"confirmpassword": "","lan": "","idracrole": "","serialoverlan": false,"enabled":true}]'''
        chassisIP = chassisConfigParam.startIP
        for resource in resMD:
            if 'chassis' in str(resource["deviceType"].lower()) and resource['ipAddress']==chassisIP:
                refId = resource['refId']
                chassisServiceTag = resource['serviceTag']
        self.log_data(" CHASSIS IP : %s"%str(chassisIP))
        self.log_data( "chassisServiceTag : %s"%str(chassisServiceTag))
        chassisPuppetName = 'cmc-' + chassisServiceTag.lower()
        response = self.getResponse("GET","Chassis",refId=refId)
        servers = response[0]['servers']
        serverList=[]
        for server in servers:
            if server['managementIP']=="0.0.0.0":
                pass
            else:
                serverList.append({"ID":server['id'],"IP":server['managementIP'],"serviceTag":server['serviceTag'],'puppetCertName':'bladeserver-'+server['serviceTag'].lower()})
        self.log_data(" SERVER LIST : %s"%str(serverList))
        ioms = response[0]['ioms']
        iomList=[]
        for iom in ioms:
            iomList.append({"IP":iom['managementIP'],"serviceTag":iom['serviceTag'],'puppetCertName':'dell_iom-'+iom['managementIP']})
        self.log_data(" IOM LIST : %s"%str(iomList))
        finalPayload = ""
        payload = self.readFile(globalVars.Configure_chassis_payload)
        payload = payload.replace("$chassis_ID","ff8080814f2ad236014f2ad87e190034").replace("$chassis_comp_id",chassisServiceTag).replace("$chassis_cert_name",chassisPuppetName).replace("$chassis_name",chassisServiceTag).replace("$chassis_IP",chassisIP).replace("$chassis_user_value",cmcUser).replace("$chassis_title_value",chassisPuppetName).replace("$chassisAdditionalParameters","").replace("</ServiceTemplate>","")
        finalPayload = finalPayload + payload
        self.log_data( " PAYLOAD AFTER CHASSIS ")
        self.log_data(finalPayload)
        for iomDetail in iomList:
            payloadIOM = ""
            payloadIOM = self.readFile(globalVars.Configure_iom_payload)
            payloadIOM = payloadIOM.replace("$switch_ID", iomDetail['puppetCertName']).replace("$switch_Component_ID",iomDetail['serviceTag']).replace("$switch_Cert_Name",iomDetail['puppetCertName']).replace("$switch_name",iomDetail['serviceTag']).replace("$switch_IP",iomDetail['IP']).replace("$switch_title_value", iomDetail['puppetCertName']).replace("$switchAdditionalParameters","")
            finalPayload = finalPayload + payloadIOM
        self.log_data(" PAYLOAD AFTER ADDING IOM PARAMETERS" )
        self.log_data(finalPayload)    
        
        for serverDetail in serverList:
            payloadServer = ""
            payloadServer = self.readFile(globalVars.Configure_server_payload)
            payloadServer = payloadServer.replace("$ID",serverDetail['ID']).replace("$server_componentID",serverDetail['serviceTag']).replace("$server_Cert_Name",serverDetail['puppetCertName']).replace("$server_name",serverDetail['serviceTag']).replace("$server_ipAddress",serverDetail['IP']).replace("$server_title_Value",serverDetail['puppetCertName']).replace("<value>$server_user_Value</value>","")
            payloadServer = payloadServer.replace("$chassisAdditionalParameters","").replace("$serverAdditionalParameters","")
            finalPayload = finalPayload + payloadServer
        
        self.log_data( " PAYLOAD AFTER ADDING SERVER PARAMETERS ")
        self.log_data(finalPayload) 
           
        finalPayload = finalPayload + "<enableApps>false</enableApps><enableCluster>false</enableCluster><enableServer>false</enableServer><enableStorage>false</enableStorage><enableVMs>false</enableVMs><allUsersAllowed>false</allUsersAllowed><manageFirmware>false</manageFirmware></ServiceTemplate>"
        
        response,status=self.getResponse("POST", "Configure", payload=finalPayload)
        respGETwizard = self.getResponse("GET", "Wizard")
        putwizard,statwiz = self.setCompleteWizard()
        
        

        
        
if __name__=="__main__":
    test = TestCMCUser()
    test.createCMCUser()
