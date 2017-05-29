'''
Created on March 28, 2016

@author: Dheeraj.Singh
'''
from libs.product import BaseClass
from libs.product import utility
from libs.product import globalVars

tc_id=utility.get_tc_data(__file__)

class Testcase(BaseClass.TestBase): 
    """
    Deploy a template HyperV with EQL Converged Flow
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, *args, **kwargs)
    
    
    
    
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        
        """
        self.succeed("Pre Run Setup ::: ")
        self.setEnvironment()
    
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        
        """
        self.succeed("Post Run Cleanup ::: ")
        self.clearEnvironment(deleteRes=True)
    
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        #Login
        self.login()  
        self.succeed("Running Test Case ::: ")
        jsonPaylod = globalVars.HyperV_EQL_ConvJsonPayload
        self.createTemplate(jsonPaylod)
            
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Deploy a template ESXi with EQL Converged Flow       
        """        
        
         
        #self.preRunSetup()
         
        self.runTestCase()
        
        #self.postRunCleanup()   
