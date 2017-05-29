'''
Created on Mar 16, 2016

@author: ankit.manglic
Description: Retrieve vCenter details for a given server IP
               
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Retrieve vCenter details for a given server IP
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.getVcenterDetailsMng("172.31.61.30")   
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.runTestCase()
          
        