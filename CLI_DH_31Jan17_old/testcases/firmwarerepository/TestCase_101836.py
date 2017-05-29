'''
Created on Jan 23, 2015

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

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import TestCase_101736, TestCase_102623OSRepo
import RepositoryParamFile

class Test_101863(DiscoverResourceTestBase):
    
    tc_Id=""
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def TestCase_101863(self):
        statR2 = self.createOSRepository(imageType = RepositoryParamFile.OSRepimageType,name = RepositoryParamFile.OSRepname,sourcePath=RepositoryParamFile.OSRepsourcePath)
        if statR2 == True:
            print " Repository Created SUCCESSFULLY "
        else:
            print " Unable to create  Repository "


    def TestCase_101847(self):
        logger = self.getLoggerInstance()
        repName = RepositoryParamFile.OSRepname
        resStat = self.checkIfRepositoryExists(repName)
        if resStat == True:
            logger.info(" ==== Repo listed in Repository list ====")
            self.log_TestData(["", "", "",'TestCase_101847','Success','Repo exists '])
        else:
            logger.info(" ==== Repo not listed in Repository list ====")
            self.log_TestData(["", "", "",'TestCase_101847','Failure','Repo does not exists '])
            
            
    def TestCase_101849(self):
        logger = self.getLoggerInstance()
        response,status = self.getResponse("GET", "osRepository")
        self.log_data( " Executing Test Case 101849 ")
        
        try:
            for repos in response:
                if(repos["name"]==RepositoryParamFile.OSRepname):
                    resDel,statDel = self.getResponse("DELETE", "osRepository", refId=str(repos["id"]))
                    self.log_TestData(["", "", "",'101849','Success','Repository removed '])
                    
        except:
            logger.info(" ==== Unable to remove repository ====")
            print " Unable to remove repository "
            self.log_TestData(["", "", "",'101849','Failure','Unable to remove repository'])
            
            
    def TestCase_101850(self):
        logger = self.getLoggerInstance()
        testObj1 = TestCase_102623OSRepo.Test_102623osRepo()
        testObj1.cleanUpRepository()
        response,status = self.getResponse("GET", "osRepository")
        if len(response)>0:
            logger.info("==== Unable to remove all files and repo ====")
            print " Unable to remove all files and repo "
        else:
            logger.info(" ==== All files and repo removed and no entry found in DB ====")
            print " All files and repo removed and no entry found in DB"
        
        

if __name__ == "__main__":
    test = Test_101863()
    test.TestCase_101850()
    test.TestCase_101863()
    test.TestCase_101847()
    test.TestCase_101849()
    os.chdir(current_dir)
    