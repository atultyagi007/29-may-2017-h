'''
Created on Nov 25, 2014

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
import globalVars
import xml.etree.ElementTree as ET
from lxml import etree
from DiscoverResourceBaseClass import DiscoverResourceTestBase

'''
    # Test Case : delete a volumes and servers from successful deployment
    # Pre-Requisite : successful deployment has storage and servers 
'''

class test_102697(TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id = ""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def getResponseandTeardown(self):
        logger = self.getLoggerInstance()
        logger.info(" ##### Test Case : 102697 #### ")
        deplName = globalVars.depNametoTearDown
        deploymentResponse,deploymentRefId = self.getResponseFromDeploymentName(deplName)
        payload = self.getTearDownPayload(deploymentResponse)
        responsePUT = self.tearDownDevices(deploymentRefId, payload)
        if responsePUT.status_code in (200, 201, 202, 203, 204):
            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Devices deleted Successfully'])
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to delete devices'])            
        
    def getTearDownPayload(self,deploymentResponse):
        
        logger = self.getLoggerInstance()
        logger.info(" Preparing payload for TearDown...")
        root = ET.fromstring(deploymentResponse.content)
        self.log_data( " ROOT : ")
        self.log_data( ET.tostring(root))
        indTD = root.find('individualTeardown')
        indTD.text = 'true'
        td = root.find('teardown')
        td.text = 'true'
        canCancl = root.find('canCancel')
        canCancl.text = 'true'
        canDel = root.find('canDelete')
        canDel.text = 'true'
        canDelRes = root.find('canDeleteResources')
        canDelRes.text = 'true'
        canEd = root.find('canEdit')
        canEd.text = 'true'
        canRet = root.find('canRetry')
        canRet.text = 'true'
        canSupAp = root.find('canScaleupApplication')
        canSupAp.text = 'true'
        canSupCl = root.find('canScaleupCluster')
        canSupCl.text = 'true'
        canSupVM = root.find('canScaleupVM')
        canSupVM.text = 'true'
        
        for st in root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                if cmpnt.findtext('type')=='SERVER' or cmpnt.findtext('type')=='STORAGE':
                    foo = cmpnt.find('teardown')
                    foo.text = 'true'
        
        payload =  ET.tostring(root)           
        logger.info( " PAYLOAD For PUT Request : ")
        logger.info(payload)
        return payload
        
        
            
        
if __name__ == "__main__":
    test = test_102697()
    test.getResponseandTeardown()
    os.chdir(current_dir)
    
        
