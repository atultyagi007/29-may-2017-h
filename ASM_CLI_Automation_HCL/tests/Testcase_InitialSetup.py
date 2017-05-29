'''
Created on Feb 21, 2016

@author: Dheeraj.Singh
'''
from libs.product import BaseClass
from libs.product import utility
import time

tc_id=utility.get_tc_data(__file__)

class Testcase(BaseClass.TestBase): 
    """
    Setting Initial Setup
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        #Disable Firewall
        self.disablefirewall()
        #Login
        #self.login()        
                    
        #Performing Initial setup         
        resIS, statIS = self.initialSetup()
        if not statIS:
            
            self.failure("Initial Setup Failed %s"%resIS)
        else:
            self.succeed("Initial Setup success %s"%resIS)
            
            
    def disablefirewall(self):
        
        try:
            command1 = 'service iptables save'
            command2 = 'service iptables stop'
            command3 = 'chkconfig iptables off'
            self.executeSudoCommand(command1)
            self.executeSudoCommand(command2)
            self.executeSudoCommand(command3)
            self.succeed("Running Test Case ::: Successfully Disabled firewall")
            time.sleep(30)
        except Exception as e1:
            self.failure("Running Test Case ::: Failed to Disabled firewall %s"%str(e1))
            
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Setting Initial Setup        
        """        
        self.runTestCase() 

        
        