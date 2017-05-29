'''
Created on Jan 29, 2015

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
import TestCase_102623OSRepo
import RepositoryParamFile


'''
    This testcase handles 4 testcases : 101762, 101773, 101775, 101776 
'''

class Test_101762(DiscoverResourceTestBase):
    
    td_Id=""
    
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
                 
        
    def createTestCaseRepository(self):
        logger = self.getLoggerInstance()
        status = self.createOSRepository(imageType = RepositoryParamFile.OSRepimageType,name = RepositoryParamFile.OSRepname,sourcePath=RepositoryParamFile.OSRepsourcePath)
        if not status:
            logger.info(" ==== Failed to Create OS Repository =====")
            print "Failed to create Repository"
        
            
            
    def TestCase_101773(self):
        logger = self.getLoggerInstance()
        status = self.checkIfRepositoryExists(RepositoryParamFile.OSRepname)
        if status == True:
            logger.info(" ==== Repo listed in Repository list ====")
            print " Repo listed in Repository list"
            self.log_TestData(["", "", "",'TestCase_101773','Success','Repo exists '])
        else:
            logger.info(" ==== Repo not listed in Repository list ====")
            print " Repo not found in Repository list"
            self.log_TestData(["", "", "",'TestCase_101773','Failed','Repo not found'])
                
                
    def TestCase_101775(self):
        logger = self.getLoggerInstance()
        response,status = self.getResponse("GET", "osRepository")
        try:
            for repos in response:
                if(repos["name"]==RepositoryParamFile.OSRepname):
                    resDel,statDel = self.getResponse("DELETE", "osRepository", refId=str(repos["id"]))
                    self.log_TestData(["", "", "",'101775','Success','Repository removed '])
                    
        except:
            logger.info(" ==== Unable to remove repository ====")
            print " Unable to remove repository "
            self.log_TestData(["", "", "",'101775','Failure','Unable to remove repository'])
            
            
            
    def TestCase_101776(self):
        logger = self.getLoggerInstance()
        test1 = TestCase_102623OSRepo.Test_102623osRepo()
        test1.cleanUpRepository()
        response,status = self.getResponse("GET", "osRepository")
        if len(response)>0:
            logger.info("==== Unable to remove all files and repo ====")
            print " Unable to remove all files and repo "
        else:
            logger.info(" ==== All files and repo removed and no entry found in DB ====")
            print " All files and repo removed and no entry found in DB"
                            
            
        
        
if __name__ == "__main__":
    test = Test_101762()
    #test.createTestCaseRepository()
    #test.TestCase_101773()
    #test.TestCase_101775()
    test.TestCase_101776()
    os.chdir(current_dir)
    
