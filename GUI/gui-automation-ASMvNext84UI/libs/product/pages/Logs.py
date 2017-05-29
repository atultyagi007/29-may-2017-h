"""
Author: P Suman/Saikumar Kalyankrishnan
Created/Modified: Sep 28th 2015/Feb 23rd 2017
Description: Functions/Operations related to Logs Page
"""

from CommonImports import *
from datetime import datetime
import math
from libs.product.objects.Common import Common
from libs.product.objects.Logs import Logs

class Logs(Navigation, Common, Logs):
    """
    Description:
        Class which includes all the Functions/Operations related to Logs Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Logs class
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Logs"
        utility.execLog("Logs")
        
    def loadPage(self):
        """
        Description:
            API to load Logs Page
        """
        try:
            utility.execLog("Loading Logs Page...")
            self.browserObject, status, result =  self.selectOption("Logs")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Logs Page :: Error --> %s" % (str(e) + format_exc())

    def validatePageTitle(self, title=None):
        """
        Description:
            API to validate Logs Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LogsObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LogsObjects('title'))), action="GET_TEXT")
            # For Log Entries > 1000
            try:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LogsObjects('pageBox'))))
            except:
                utility.execLog("Logs Table Taking More Than Usual Time To Load...Waiting for an additional 30 seconds")
                time.sleep(30)
            if title not in getCurrentTitle:
                utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
                return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
            else:
                utility.execLog("Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LogsObjects('pageBox'))))
                return self.browserObject, True, "Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title)
        except Exception as e:
            return self.browserObject, False, "Exception Validating Page Title :: Exception --> %s" % (str(e) + format_exc())
    
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Logs Page 
        """
        optionList = {}
        opts = {"Export All": self.LogsObjects('exportAllLogs'), "Purge": self.LogsObjects('purgeLogs')}
        try:
            for key, value in opts.items():
                utility.execLog("Reading Option: %s" % key)
                disabled = self.handleEvent(EC.element_to_be_clickable((By.ID, value)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                if "true" in disabled:
                    optionList[key] = "Disabled"
                else:
                    optionList[key] = "Enabled"
                utility.execLog("Able to read option: '%s'" % (key))
            utility.execLog("Logs Options :: %s" % (optionList))
            return self.browserObject, True, optionList                 
        except Exception as e:
            utility.execLog("Unable to read Options on Logs Page :: Error -> %s" % (str(e) + format_exc()))
            return self.browserObject, False, "Unable to read Options on Logs Page :: Error -> %s" % (str(e) + format_exc())
    
    def getDetails(self, readAll=False, readFromTime=None):
        """
        Description:
            API to fetch Existing Logs
        """
        logList = []
        totalPages = 1
        try: 
            utility.execLog("Fetching Logs...")
            utility.execLog("Reading Logs Table...")
            tableName = self.LogsObjects('logsTable')
            # Getting Total No. of Pages
            getLogsCount = "//*[@id='%s']//table//tfoot//span[contains(@ng-if, 'totalItems')]" % tableName
            countLogs = self.handleEvent(EC.element_to_be_clickable((By.XPATH, getLogsCount)), action="GET_TEXT")
            utility.execLog("Total Logs: %s" % countLogs)
            totalPages = float(countLogs)/20
            totalPages = math.ceil(totalPages)
            utility.execLog("Total Pages: %s" % str(totalPages))
            # Processing Columns in Logs Table
            getColumns = "//*[@id='%s']//table//thead//th" % tableName
            # Get No. of Columns i.e. No. of Parameters for a Log
            countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
            utility.execLog("Total Number of Columns: %s" % str(countColumns))
            tableColumns = []
            for col in range(1, countColumns + 1):
                getColumnHeader = "//*[@id='%s']//table//thead//th[%i]" % (tableName, col)
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="IS_DISPLAYED"):
                    columnName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="GET_TEXT")
                    if columnName:
                        tableColumns.append(columnName)
                        utility.execLog("Able to fetch Column Name: '%s'" % columnName)
            tableColumns = [x for x in tableColumns if x != '']
            utility.execLog("Able to fetch '%s' Logs Table Columns '%s'" % (tableName, str(tableColumns)))
            # Read Logs in Existing Page
            readFromTimeComplete = False
            # Fetching recent Logs when realAll = False
            if not readAll:
                totalPages = 1
            for page in range(1, int(totalPages) + 1):
                utility.execLog("Current Page: %s" % str(page))
                getPage = "//*[@id='%s']//table//tfoot//input" % tableName
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, getPage)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, getPage)), action="SET_TEXT", setValue=page)
                time.sleep(3)
                # Processing Rows
                getRows = "//*[@id='%s']//table//tbody//tr" % tableName
                # Get No. of Rows i.e. No. of Logs
                countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
                utility.execLog("Total Number of Rows/Logs: %s" % str(countRows))
                # Parsing through every Column Per Row
                for row in range(1, countRows + 1):
                    tableElements = []
                    for col in range(1, countColumns + 1):
                        getDetail = "//*[@id='%s']//table/tbody/tr[%i]/td[%i]" % (tableName, row, col)
                        if self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="IS_DISPLAYED"):
                            parameterValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="GET_TEXT")
                            tableElements.append(parameterValue)
                    tempDict = dict(zip(tableColumns, tableElements))
                    utility.execLog("Successfully fetched Log Info: '%s'" % str(tempDict))
                    logList.append(tempDict)
                    # Reading Logs Newer than OR Equal To Specified Time
                    if readFromTime:
                        readFromTimeFormatted = datetime.strptime(str(readFromTime), "%B %d, %Y %I:%M %p")
                        timeStamp = tempDict["Date"]
                        timeStamp = datetime.strptime(timeStamp, "%B %d, %Y %I:%M %p")
                        if timeStamp < readFromTimeFormatted:
                            utility.execLog("Reading Logs starting from '%s'" % str(readFromTime))
                            readFromTimeComplete = True
                            logList.pop()
                            break
                if readFromTimeComplete:
                    utility.execLog("Completed Reading Logs starting from '%s'" % str(readFromTime))
                    break
            return self.browserObject, True, logList
        except Exception as e:
            return self.browserObject, False, "Exception generated while fetching Logs :: Error -> %s" % (str(e) + format_exc())

    def exportData(self):
        """
        Description: 
            API to Export All Logs to CSV file
        """
        try:
            utility.execLog("Clicking on 'Export All'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.LogsObjects('exportAllLogs'))), action="CLICK")
            # Location: /downloads
            time.sleep(10)
            utility.execLog("Clicked on 'Export All'")
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Export All Logs :: Error -> %s" % (str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Export All Logs to CSV /downloads"
        except Exception as e:
            return self.browserObject, False, "Exception while Exporting All Logs :: Error -> %s" % (str(e) + format_exc())
        
    def verifyLogs(self, resourceIP, posScenario=False, negScenario=False):
        """
        Description: 
            Verify Logs for Resource Discovery
        """
        try:
            # Fetch Logs
            self.browserObject, status, logs = self.getDetails()
            time.sleep(5)
            if negScenario:
                utility.execLog("Negative Scenario :: Resource Discovery --> Fail")
                verText = "Resource discovery failed: Unable to discover the device with the IP address: %s" % resourceIP
                for log in logs:
                    if verText in log["Description"]:
                        return self.browserObject, True, "Resource Discovery Failed"
            elif posScenario:
                utility.execLog("Positive Scenario :: Resource Discovery --> Pass")
                verText = "Resource Added To Inventory : IDRAC, IP = %s" % resourceIP
                for log in logs:
                    if verText in log["Description"]:
                        return self.browserObject, True, "Resource Discovery Passed"
            utility.execLog("Unable to verify Logs for Resource: %s" % resourceIP)
            return self.browserObject, False, "Unable to verify Logs for Resource: %s" % resourceIP
        except Exception as e:
            return self.browserObject, False, "Exception generated while Verifying Logs :: Error -> %s" % (str(e) + format_exc())
        
    def validateUpdateFirmwareLogs(self, deviceIdList, readLogsFromTime):
        """
        Description: Verify Firmware Update Completion Logs.
        """
        try:
            utility.execLog("Verifying Firmware Update Completion Logs...")
            for deviceId in deviceIdList:
                updateSuccessLog = "Firmware Update Successful for Device '%s'" % deviceId
                updateFailLog = "Firmware Update Failed for Device '%s'" % deviceId
                logDescriptionList = []
                # Fetch Logs
                self.browserObject, status, logs = self.getDetails(readAll=True)
                if status:
                    for tempDict in logs:
                        logDescriptionList.append(tempDict["Description"])
                if updateFailLog in logDescriptionList:
                    utility.execLog(updateFailLog)
                    return self.browserObject, False, updateFailLog
                elif updateSuccessLog in logDescriptionList:
                    return self.browserObject, True, updateSuccessLog
                else:
                    return self.browserObject, False, "Failed to find Logs related to Firmware Update for Resource/Device '%s'" % deviceId
        except Exception as e:
            return self.browserObject, False, "Exception generated while verifying Firmware Update Completion Logs :: Error --> %s" % e