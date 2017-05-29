'''
Author: Saikumar Kalyankrishnan
Pre-requisite: * Deploy a OVF
               * sshd start
               * Update config.ini with Appliance IP
               * Update Input.xlsx
Execution: * Initial Setup
           * Setup Credentials
           * Configure Networks
           * Add License
           * Discover Resources
           * Update Upgrade Repository Path
           * Add OS Repositories
           * Upload Add-On Modules
           * Set Firmware Path
           * Create Post-Install Files (hello.sh & winTest.ps1)         
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Appliance Setup - Initialization, Configuration & Setup
    """
    def __init__(self, *args, **kwargs):
        """
        Testcase Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the Test-Scenario 
        """
        self.logDesc("Pre Run Setup")        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
         
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")

        #Logout of ASM Application
        self.logout()
        
    def runTestCase(self):
        """
        Running Test Case: Appliance Setup - Initialization, Configuration & Setup
        Initial Setup is performed if Initial Setup Screen pops-up
        """
        
        #Create and Setup Credentials - Refer Input.xlsx
        self.setupCredentials()
        
        #Create and Configure Networks - Refer Input.xlsx
        self.setupNetworks()
        
        #Discover Resources - Refer Input.xlsx
        self.discoverMultipleResources()
        
        #Add License Type
        evalLicensePath = globalVars.perp_license_file_path_for_500_Setup
        self.addAndverifyLicenseType(evalLicensePath, expirationDate = True)
        
        #Update Upgrade Repository Path
        self.setRepoPath()
        
        #Add OS Repositories
        self.addOSRepo(verifyOSRepo=False)

        #Upload Add-On Modules - SaySomething
        self.addAddOnFile(verify=False)
                
        #Set Firmware Path
        self.addFirmwareRepository("networkPath", globalVars.catlogRepository["nfsSourceJune"], "", makeDefault=True, testConnection=True, testNegative=False, waitAvailable=False)
        
        #Create Post-Install Files
        self.createPostInstalls()       
                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        Execution Starting Function
        """ 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()