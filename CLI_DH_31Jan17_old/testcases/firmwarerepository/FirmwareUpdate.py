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

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import json
import time
import globalVars


'''
    Discover a chassis and configure the chassis, server and switches with existing credentials and IPs
'''
class FirmwareUpdate(DiscoverResourceTestBase):
    
    tc_Id = ""
    refIDNCUR=[]
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def updateFirmwares(self):
        self.authenticate()
        resMD,statMD =  self.getResponse("GET", "ManagedDevice")
        resRefIds = []
        
        for resource in resMD:
            resRefIds.append(resource["refId"])
        self.log_data(" RefList : %s" %str(resRefIds))
        for refids in resRefIds:
            resGetMD = self.getManagedDeviceRefID(refids)
            if not resGetMD:
                print " No response found from GET ManagedDevice "
            else:
                resFC,statFC = self.getResponse("POST", "compliance", payload=resGetMD.content)
                if resFC["compliance"]=="NONCOMPLIANT" or resFC["compliance"]=="UPDATEREQUIRED":
                    self.refIDNCUR.append(resFC["refId"])
                    
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
                resFC1,statFC1 = self.getResponse("POST", "compliance", payload=resGetMD1.content)
            
                if not statFC1:
                    print " Unable to get Compliance information : %s"%str(resFC1)
                    self.log_data(" Unable to get Compliance information : %s"%str(resFC1))
                else:
                    logger.info( " response for compliance check of resources : %s"%str(resFC1))
                    if resFC1["compliance"]=="NONCOMPLIANT" or resFC1["compliance"]=="UPDATEREQUIRED":
                        failedRefs['deviceType'] = resFC1['deviceType']
                        failedRefs['serviceTag'] = resFC1['serviceTag']
                        failedList.append(failedRefs)
                
            
        if failedList is not None:
            self.log_data(" Failed to update devices  : ")
            self.log_data(failedList)
            logger.info(" Failed to update the following devices : ")
            logger.info(failedList)
            self.log_data( 'Failed to update the following devices  : %s'%str(failedList))
            print 'Failed to update the following devices  : %s'%str(failedList) 
        
        
if __name__ == "__main__":
    test = FirmwareUpdate()
    test.updateFirmwares()
    os.chdir(current_dir)
