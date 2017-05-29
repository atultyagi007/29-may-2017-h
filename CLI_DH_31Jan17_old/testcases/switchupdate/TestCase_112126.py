'''
Created on Sep 21, 2015

@author: dheeraj.singh
'''

import os
import sys
import json
import xml.etree.ElementTree as ET
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import globalVars
from encodings import raw_unicode_escape
import time
import RepositoryParamFile

class Testcase(DiscoverResourceTestBase):
    
    """
    Try to update FW on switches S4810/S5000/S6000
    """ 
    
    tc_Id = ""
    refIDNCUR=[]
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
        
    def updateSwitchBundleForSwitch(self):
        respFR, statpFR = self.getResponse("GET", "FirmwareRepository")
        print " Response : %s"%str(respFR)
        fwRepositoryId = ""
        for resspos in respFR:
            if resspos['name'] == "ASM September 2016":
                fwRepositoryId = resspos['id']
        print " fwRepositoryId : %s"%str(fwRepositoryId)
        
        resFR, statFR = self.getResponse("GET", "FirmwareRepository",refId=fwRepositoryId+'?related=true')
        if not statFR:
            print " Could not get response from FirmwareRepository : %s"%str(resFR)
            self.log_data( 'Could not get response from FirmwareRepositor : %s'%str(resFR))
        #print " resMD : "
        #print resFR
        result1 = resFR['softwareBundles']
        refIDBundle =""
        for bundles in result1:
            print bundles['name']
            if bundles['name'] == RepositoryParamFile.BundleName:
                print ">>>>> id >>>>>>>>>>>>>>"
                print bundles['id']
                refIDBundle = bundles['id']
        
        
        payload = self.readFile(globalVars.filename_TestCase_SwitchUpdatePut)
        payload = payload.replace("$criticality",RepositoryParamFile.criticality).replace("$switch_model",RepositoryParamFile.deviceModel).replace("$fwRepositoryId",fwRepositoryId).replace("$name",RepositoryParamFile.BundleName).replace("$bundle_Path",RepositoryParamFile.userBundlePath).replace("$version",RepositoryParamFile.BundleVersion).replace("$SoftBundleDesc",RepositoryParamFile.BundleDescription).replace("$RefID",refIDBundle)
        print " payload : "
        print payload
        response,status = self.getResponse("PUT","softwareBundleFirmware",payload = payload, refId=refIDBundle)
        print response, status
        
        
    def updateFirmwaresSwitch(self):
        self.authenticate()
        self.getResources()
        switchList = globalVars.resourceInfo['SWITCH']
        print " SWITCH LIST : %s"%str(switchList)
        
        if len(switchList) == 0:
            print "Required no. of COMPELLENT not available"
            return
        
        resRefIds = []
        for device in switchList:
            if device['model']==globalVars.switchModel_TestCase_112126:
                resRefIds.append(device['refid'])
            
        
        self.log_data(" RefList : %s" %str(resRefIds))

        for refids in resRefIds:
           
            resGetMD = self.getManagedDeviceRefID(refids)
            print resGetMD.content
            if not resGetMD:
                print " No response found from GET ManagedDevice "
            else:

                xmplRes=resGetMD.content
                root = ET.fromstring(xmplRes)
                compliancelem = root.find('compliance')
                refID=root.find('refId')
#                 print textelem.text                                                                                              
                print "....."
#                 resFC,statFC = self.getResponse("POST", "compliance", payload=resGetMD.content)
#                 if resFC["compliance"]=="NONCOMPLIANT" or resFC["compliance"]=="UPDATEREQUIRED":
#                     self.refIDNCUR.append(resFC["refId"])
                try:
                    if compliancelem.text=="NONCOMPLIANT" or compliancelem.text=="UPDATEREQUIRED":
                        self.refIDNCUR.append(refID.text)
                except Exception as e:
                    print "exception %s"+e

        self.log_data(" NONCOMPLIANT or UPDATEREQUIRED refID : ")
        self.log_data(self.refIDNCUR)   
        refIdList = ""
        for rIds in self.refIDNCUR:
            refIdList = refIdList + "<idList>"+rIds+"</idList>"
        payload=self.readFile(globalVars.firmwareUpdatePayload)
        payload = payload.replace("$exitMaintMode","true").replace("$idList",refIdList).replace("$scheduleUpdate","updatenow")
        
        self.log_data( " UPDATE PAYLOAD : ")
        self.log_data(payload)
        
        result,status = self.getResponse("PUT", "updateFirmware", payload=payload)
        
        self.log_data( " result, status : ")
        self.log_data(result)
        self.log_data(status)
        time.sleep(120)
        self.checkStatusPostUpdate()
        
        
        
    def getFirmwareState(self,resMangDevc):
        resFS=True
        for resrc in resMangDevc:
            state = resrc['state']
            if state.lower()=='updating':
                resFS = False
        return resFS
            
        
    def checkStatusPostUpdate(self):
        logger = self.getLoggerInstance()
        logger.info(" Checking Device state after Update Request...... ")
        if len(self.refIDNCUR) == 0:
            self.log_data( 'No Resource found in UPDATEREQUIRED or NONCOMPLIANT state ')
            return 'No Resource found in UPDATEREQUIRED or NONCOMPLIANT state ' 
            
        ret = True
        while True:
            resMD1,statusMD1 = self.getResponse("GET", "ManagedDevice")
            #logger.info( " Getting resources info : %s"%str(resMD1))
            if not statusMD1:
                print " Unable to get resource information :%s"%str(resMD1)
            ret = self.getFirmwareState(resMD1)
            if ret==False:
                time.sleep(120)
            else:
                break
            
        failedList=[]
        for refs in self.refIDNCUR:
            failedRefs={}
            resGetMD1 = self.getManagedDeviceRefID(refs)
            if not resGetMD1:
                print " No response found for device refIds "
                self.log_data(" No response found for device refIds ")
            else:
                
                xmplRes=resGetMD1.content
                root = ET.fromstring(xmplRes)
                compliancelem = root.find('compliance')
                refID=root.find('refId')
                deviceType=root.find('deviceType')
                print "....."
#                 resFC,statFC = self.getResponse("POST", "compliance", payload=resGetMD.content)
#                 if resFC["compliance"]=="NONCOMPLIANT" or resFC["compliance"]=="UPDATEREQUIRED":
#                     self.refIDNCUR.append(resFC["refId"])
                try:
                    if compliancelem.text=="NONCOMPLIANT" or compliancelem.text=="UPDATEREQUIRED":
                        failedRefs['deviceType'] = deviceType.text
                        failedRefs['serviceTag'] = root.find('serviceTag').text
                        failedList.append(failedRefs)
                except Exception as e:
                    print "exception %s"+e
                
#                 resgetMD1=resGetMD1.text
#                 data = json.loads(resGetMD1.text)
#                 print data
#                 print "....."
#                 try:
#                     if data["compliance"]=="NONCOMPLIANT" or data["compliance"]=="UPDATEREQUIRED":
# #                         self.refIDNCUR.append(data["refId"])
#                         failedRefs['deviceType'] = data['deviceType']
#                         failedRefs['serviceTag'] = data['serviceTag']
#                         failedList.append(failedRefs)
#                 except Exception as e:
#                     print "exception %s"+e
#         if failedList is not None:
        if len(failedList) > 0:    
            self.log_data(" Failed to update devices  : ")
            self.log_data(failedList)
            logger.info(" Failed to update the following devices : ")
            logger.info(failedList)
            self.log_data( 'Failed to update the following devices  : %s'%str(failedList))
            print 'Failed to update the following devices  : %s'%str(failedList) 
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.updateFirmwaresSwitch()
                
        
