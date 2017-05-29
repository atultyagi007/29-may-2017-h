'''
Author: P.Suman
Created Date: Dec 23, 2015
Description: Verify that there is an export function/button which allows user to export the Template data/information 
            to a CSV file, Export the data and save it to our local drive, Validate the CSV format file 
            is generated and saved to a local folder and Verify that the data in the CSV files match with 
            the data displayed in the ASM UI as intended
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Export Data on Template Page
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
        #Create Template if not exists
        self.createSampleTemplate(templateName=self.templateName, publishedTemplate=True, volumeName="autoVolume")
        
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
        self.getTemplateOptions(actualOptions=["Create Template","Export All", "Upload External Template"], 
                        enableOptions=["Export All"], templateName=self.templateName, 
                        templateType="My Templates", templateDetailOptions=False)
        #Export Data to csv
        self.exportDataToCSV(option="Templates")
        #Verify Exported File 
        self.verifyExportedFile(option="serviceTemplates")
        #Read content of csv file
        templateInfo = self.getExportedData()
        self.succeed("Template Data :: %s"%str(templateInfo))
        #Get Templates
        tempList = self.getTemplates(option="My Templates")
        self.succeed("Template Information from UI :: %s"%str(tempList))
        if len(templateInfo) <= 0:
            self.failure("Exported CSV has no Template Information", raiseExc=True)
        #Compare Columns
        columnMismatch = set(tempList[0].keys()).difference(set(templateInfo[0].keys()))
        if len(columnMismatch) > 0:
            self.failure("Columns Mismatch from Exported CSV Template Information and UI :: %s"%str(columnMismatch), raiseExc=True)
        else:
            self.succeed("Columns Matched from Exported CSV and UI :: CSV '%s' UI '%s'"%(str(templateInfo[0].keys()), str(tempList[0].keys())))
        failList = []
        found = False
        #Compare Data
        for template in tempList:
            found = False
            for tempcsv in templateInfo:
                if template["Name"] == tempcsv["Name"]:
                    found = True
                    for key in template.keys():
                        if key == "Last Deployed On":
                            continue
                        elif key == "Attachments":
                            if tempcsv[key] == "false" and template[key] != "":
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(tempcsv), str(template)))
                                break
                            elif tempcsv[key] == "true" and template[key] == "":
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(tempcsv), str(template)))
                                break
                        else:
                            if template[key] != tempcsv[key]:
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(tempcsv), str(template)))
            if not found:
                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(tempcsv), str(template)))
        if len(failList) > 0:
            self.failure("Mismatch in some Templates Information :: %s"%str(failList), raiseExc=True)
        else:
            self.succeed("Template Information Matched in CSV and UI")
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.templateName = "Test Template"
        self.exportedFile = None
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    