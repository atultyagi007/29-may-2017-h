'''
Author: P.Suman
Created Date: Dec 23, 2015
Description: Pre-Requisite: Ensure that logs page has different log data, ex: discovery, service, inventory, etc. 
            Verify that: 
            1. the Logs page has an export function/button which allows user to export the data/information to a CSV file. 
            2. click on the button to export the data and save it to our local drive. 
            3. Validate the CSV format file is generated and saved to a local folder. 
            4. Verify that the data in the CSV files match with the data displayed in the ASM UI as intended
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Export Data on Logs page
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
        self.verifyOptions(optionList=["Export All", "Purge"], pageName="Logs", enableOptions=["Export All"])
        #Export Data to csv
        self.exportDataToCSV(option="Logs")
        #Verify Exported File 
        self.verifyExportedFile(option="userLogs")
        #Read content of csv file
        logInfo = self.getExportedData()
        self.succeed("Log Data :: %s"%str(logInfo))
        #Get Logs
        logList = self.getLogs(readAll=True)
        self.succeed("Logs Information from UI :: %s"%str(logList))
        if len(logInfo) <= 0:
            self.failure("Exported CSV has no Log Information", raiseExc=True)
        #Compare Columns
        columnMismatch = set(logList[0].keys()).difference(set(logInfo[0].keys()))
        if len(columnMismatch) > 0:
            self.failure("Columns Mismatch from Exported CSV Log Information and UI :: %s"%str(columnMismatch), raiseExc=True)
        else:
            self.succeed("Columns Matched from Exported CSV and UI :: CSV '%s' UI '%s'"%(str(logInfo[0].keys()), str(logList[0].keys())))
        failList = []
        found = False
        #Compare Data
        for log in logList:
            found = False
            for logcsv in logInfo:
                if log["Description"] == logcsv["Description"]:
                    found = True
                    for key in log.keys():
                        if key in ("Date and Time","Severity", "User"):
                            continue
                        elif key == "Category":
                            logData = log[key].replace(" ", "_").upper()
                            if logData != logcsv[key]:
                                failList.append("Mismatch in Column '%s' Row CSV :: %s -> UI :: %s"%(key, str(logcsv), str(log)))
                                break
                        else:
                            if log[key] != logcsv[key]:
                                failList.append("Mismatch in Column '%s' Row CSV :: %s -> UI :: %s"%(key, str(logcsv), str(log)))
                                break
            if not found:
                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(logcsv), str(log)))
        if len(failList) > 0:
            self.failure("Mismatch in some Logs Information :: %s"%str(failList), raiseExc=True)
        else:
            self.succeed("Log Information Matched in CSV and UI")
                                
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
    