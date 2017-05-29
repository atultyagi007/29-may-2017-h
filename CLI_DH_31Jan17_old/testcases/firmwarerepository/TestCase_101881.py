'''
Created on Jan 27, 2015

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

class Test_101881(DiscoverResourceTestBase):
    
    tc_Id=""
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def TestCase_101881(self):
        logger = self.getLoggerInstance()
        cleanObj = TestCase_102623OSRepo.Test_102623osRepo()
        cleanObj.cleanUpRepository()
        statusOS = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_1, name=RepositoryParamFile.OSRepname_1,sourcePath=RepositoryParamFile.OSRepsourcePath_1)
        if statusOS == True:
            statNFS = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_2, name=RepositoryParamFile.OSRepname_2,sourcePath=RepositoryParamFile.OSRepsourcePath_2)
        else:
            logger.info(" ==== unable to create OS Repository ====")
            print " Unable to create OS Repository "
        if statNFS == True:
            statCFS = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_3, name=RepositoryParamFile.OSRepname_3, sourcePath=RepositoryParamFile.OSRepsourcePath_3)
        else:
            logger.info("==== unable to create Repository from NFS share ====")
            print " unable to create Repository from NFS share"
        if statCFS != True:
            logger.info("==== unable to create Repository from CIFS share ====")
            print " Unable to create Repo  from CIFS share "
        

if __name__ == "__main__":
    test = Test_101881()
    test.TestCase_101881()
    os.chdir(current_dir)