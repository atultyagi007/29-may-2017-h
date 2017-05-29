"""
Author: Raj Patel/Saikumar Kalyankrishnan
Created/Modified: Jan 21th 2016/Feb 28th 2017
Description: Functions/Operations related to Jobs Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.Jobs import Jobs

class Jobs(Navigation, Common, Jobs):
    """
    Description:
        Class which includes all the Functions/Operations related to Jobs Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Jobs class
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Jobs"
        utility.execLog("Jobs")

    def loadPage(self):
        """
        Description:
            API to load Jobs Page
        """
        try:
            utility.execLog("Loading Jobs Page...")
            self.browserObject, status, result = self.selectOption("Jobs")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Jobs Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Jobs Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.JobsObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.JobsObjects('title'))), action="GET_TEXT")
            if title not in getCurrentTitle:
                utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
                return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
            else:
                utility.execLog("Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title))
                return self.browserObject, True, "Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title)
        except Exception as e:
            return self.browserObject, False, "Exception Validating Page Title :: Exception --> %s" % (str(e) + format_exc())
    
    def getDetails(self):
        """
        Description:
            API to get Jobs Details
        """
        jobsList = []
        try:
            utility.execLog("Reading Jobs Details...")
            tableName = self.JobsObjects('jobsTable')
            # Processing Columns
            getColumns = "//table[@id='%s']//thead//th" % tableName
            # Get No. of Columns i.e. No. of Parameters for a Job
            countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
            utility.execLog("Total Number of Columns: %s" % str(countColumns))
            tableColumns = []
            # Not Adding 'Select Checkbox' and 'Blank Column' to 'tableColumns'
            for col in range(2, countColumns + 1):
                getColumnHeader = "//table[@id='%s']//thead//th[%i]" % (tableName, col)
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="IS_DISPLAYED"):
                    columnName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="GET_TEXT")
                    if columnName:
                        tableColumns.append(columnName)
                        utility.execLog("Able to fetch Column Name: '%s'" % columnName)
            tableColumns = [x for x in tableColumns if x != '']
            utility.execLog("Able to fetch '%s' Jobs Table Columns '%s'" % (tableName, str(tableColumns)))
            # Processing Rows
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Active Jobs
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows/Jobs: %s" % str(countRows))
            # To check for 0 Jobs: 'There are currently no jobs scheduled or running.'
            if countRows == 1:
                getColumns = "//table[@id='%s']//tbody//tr//td" % tableName
                countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
                if countColumns == 1:
                    countRows = 0
                    utility.execLog("There are currently no jobs scheduled or running :: Zero Jobs")
            # Parsing through every Column Per Row
            for row in range(1, countRows + 1):
                tableElements = []
                for col in range(1, countColumns + 1):
                    getDetail = "//table[@id='%s']/tbody/tr[%i]/td[%i]" % (tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="IS_DISPLAYED"):
                        parameterValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="GET_TEXT")
                        if parameterValue:
                            tableElements.append(parameterValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Successfully fetched Jobs Info: '%s'" % str(tempDict))
                # Consolidating All Parameter Values Together
                jobsList.append(tempDict)
            return self.browserObject, True, jobsList
        except Exception as e:
            return self.browserObject, False, "Exception while reading Jobs :: Error -> %s" % (str(e) + format_exc())
