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
import RepositoryParamFile

class Test_101854(DiscoverResourceTestBase):
    
    tc_Id=""
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def TestCase_101854(self):
        logger = self.getLoggerInstance()
        statNFS = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_1, name=RepositoryParamFile.OSRepname_1,sourcePath=RepositoryParamFile.OSRepsourcePath_1)
        if statNFS == True:
            statNFS2 = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_2, name=RepositoryParamFile.OSRepname_2,sourcePath=RepositoryParamFile.OSRepsourcePath_2)
        else:
            logger.info(" ==== unable to create  Repository 1 from NFS Share ====")
            print " unable to create Repository from NFS share"
        if statNFS2==True:
            statNFS3 = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_3, name=RepositoryParamFile.OSRepname_3, sourcePath=RepositoryParamFile.OSRepsourcePath_3)
        else:
            logger.info(" ==== unable to create  Repository 2 from NFS Share ====")
            print " Unable to create Repository 2 from NFS share "
        if statNFS3 != True:
            logger.info(" ==== unable to create NFS Repository 3 ====")
            print " Unable to create Repo 3 from NFS share "
        

if __name__ == "__main__":
    test = Test_101854()
    test.TestCase_101854()
    os.chdir(current_dir)