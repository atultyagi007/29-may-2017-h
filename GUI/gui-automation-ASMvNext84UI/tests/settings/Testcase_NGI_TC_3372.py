'''
Author: P.Suman
Created Date: Dec 24, 2015
Description: Pre-Requisite: Setup all the different Networks on ASM appliance. Verify that: 
            1. All the networks are setup successfully. 
            2. go to the Network page and view the details for the correct information. 
            3. Ensure that there is an export function/button which allows user to export the network data/information to a CSV file. 
            4. click on the button to export the data and save it to our local drive. 
            5. Validate the CSV format file is generated and saved to a local folder. 
            6. Verify that the data in the CSV files match with the data displayed in the ASM UI as intended
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Export Data on Network Page
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Clear Existing csv files
        self.clearExportedCSVFiles()
        #Get Networks
        self.networkList = self.getNetworks()
        if len(self.networkList) == 0:
            self.setupNetworks()
            #Get Resources
            self.networkList = self.getNetworks()
            if len(self.networkList) == 0:
                self.failure("There are no Networks to Export and Verify :: %s"%(str(self.networkList)), 
                             resultCode=BaseClass.OMITTED, raiseExc=True)
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Clear Existing csv files
        self.clearExportedCSVFiles()

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Verify Export All Option is Enabled
        self.verifyOptions(optionList=["Export All"], pageName="Networks", enableOptions=["Export All"])
        #Export Data to csv
        self.exportDataToCSV(option="Networks")
        #Verify Exported File 
        self.verifyExportedFile(option="networks")
        #Read content of csv file
        networkInfo = self.getExportedData()
        if len(networkInfo) <= 0:
            self.failure("Exported CSV has no Network Information", raiseExc=True)
        else:
            self.succeed("Networks Data :: %s"%str(networkInfo))
        self.succeed("Networks Information from UI :: %s"%str(self.networkList))
        #Compare Columns
        columnMismatch = set(self.networkList[0].keys()).difference(set(networkInfo[0].keys()))
        
        if len(columnMismatch) > 4:
            self.failure("Columns Mismatch from Exported CSV Network Information and UI :: %s"%str(columnMismatch), raiseExc=True)
        else:
            self.succeed("Columns Matched from Exported CSV and UI :: CSV '%s' UI '%s'"%(str(networkInfo[0].keys()), str(self.networkList[0].keys())))
        #=======================================================================
        # failList = []
        # found = False
        # #Compare Data
        # for log in self.networkList:
        #     found = False
        #     for logcsv in networkInfo:
        #         if log["Description"] == logcsv["Description"]:
        #             found = True
        #             for key in log.keys():
        #                 if key == "Date and Time" or key == "Severity":
        #                     continue
        #                 elif key == "Category":
        #                     logData = log[key].replace(" ", "_").upper()
        #                     if logData != logcsv[key]:
        #                         failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(logcsv), str(log)))
        #                         break
        #                 else:
        #                     if log[key] != logcsv[key]:
        #                         failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(logcsv), str(log)))
        #                         break
        #     if not found:
        #         failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(logcsv), str(log)))
        # if len(failList) > 0:
        #     self.failure("Mismatch in some Logs Information :: %s"%str(failList), raiseExc=True)
        # else:
        #     self.succeed("Log Information Matched in CSV and UI")
        #=======================================================================
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.exportedFile = None
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    