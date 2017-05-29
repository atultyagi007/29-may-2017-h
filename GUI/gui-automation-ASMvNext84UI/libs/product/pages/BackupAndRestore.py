"""
Author: P Suman/Raj Patel/Saikumar Kalyankrishnan
Created/Modified: Sep 15th 2015/Feb 24th 2016/Mar 8th 2017
Description: Functions/Operations related to Backup And Restore Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.BackupAndRestore import BackupAndRestore

class BAR(Navigation, Common, BackupAndRestore):
    """
    Description:
        Class which includes Functions/Operations related to Backup And Restore
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of BAR class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Backup and Restore"
        utility.execLog("Backup and Restore")
        
    def loadPage(self):
        """
        Description:
            API to Load Backup And Restore Page
        """
        try:
            utility.execLog("Loading Backup And Restore Page...")
            self.browserObject, status, result = self.selectOption("Backup and Restore")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Backup And Restore Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Backup And Restore Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.BackupAndRestoreObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.BackupAndRestoreObjects('title'))), action="GET_TEXT")
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
            API to fetch Backup And Restore Settings and Details
            Use of Complicated XPATHs: Suggest to create IDs
        """
        optionDict = {}
        try:
            time.sleep(3)
            utility.execLog("Reading Backup And Restore Settings and Details...")
            getSections = self.BackupAndRestoreObjects('sections')
            countSections = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getSections))))
            # Fetching Sections
            for option in range(1, countSections + 1):
                getHeader = self.BackupAndRestoreObjects('sections') + "[%i]//h2" % option
                getCurrentHeader = self.handleEvent(EC.element_to_be_clickable((By.XPATH, getHeader)), action="GET_TEXT")
                # Remove 'Edit' from Header
                getCurrentHeader = getCurrentHeader.splitlines()[0]
                utility.execLog("Reading Info: '%s'" % getCurrentHeader)
                getOptions = self.BackupAndRestoreObjects('sections') + "[%i]" % option + self.BackupAndRestoreObjects('sectionRows')
                countOptions = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getOptions))))
                # Fetching Rows in Sections
                for row in range(1, countOptions + 1):
                    getLabel = getOptions + "[%i]//label" % row
                    key = self.handleEvent(EC.presence_of_element_located((By.XPATH, getLabel)), action="GET_TEXT")
                    getValue = getOptions + "[%i]//div" % row
                    value = self.handleEvent(EC.presence_of_element_located((By.XPATH, getValue)), action="GET_TEXT")
                    optionDict[key] = value
                    utility.execLog("Able to read '%s': %s" % (key, value))
            utility.execLog("Backup And Restore Settings and Details :: %s" % (optionDict))
            return self.browserObject, True, optionDict          
        except Exception as e:
            return self.browserObject, False, "Exception while fetching Backup And Restore Settings & Details :: Error -> %s" % (str(e) + format_exc())
    
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Backup And Restore 
        """
        optionDict = {}
        try:
            time.sleep(3)
            opts = {"Backup Now": self.BackupAndRestoreObjects('backupButton'),
                    "Restore Now": self.BackupAndRestoreObjects('restoreButton'),
                    "Settings And Details": self.BackupAndRestoreObjects('editSettingDetails'),
                    "Scheduled Backups": self.BackupAndRestoreObjects('editScheduledBackup'),
            }
            for key, value in opts.items():
                utility.execLog("Reading Info: %s" % key)
                disabled = self.handleEvent(EC.presence_of_element_located((By.ID, value)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                if "true" in disabled:
                    optionDict[key] = "Disabled"
                else:
                    optionDict[key] = "Enabled"
                utility.execLog("Able to read option: '%s'" % key)
            utility.execLog("Backup and Restore Options :: %s" % optionDict)
            return self.browserObject, True, optionDict            
        except Exception as e:
            return self.browserObject, False, "Exception while reading Backup And Restore Options :: Error -> %s" % str(e)

    def testConnectionCheck(self):
        """
        Description:
            API to Test Connection and return result
        Output:
            True/False
        INCOMPLETE: Pending Testing
        """
        try:
            utility.execLog("Test Connection In-Progress...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('testConnection'))), action="CLICK")
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=20)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Test Connection :: Error --> %s" % str(errorMessage)
            except:
                try:
                    self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.BackupAndRestoreObjects('testConnectionCheck'))))
                    utility.execLog("Success: Test Connection was Successful. Clicking 'Close'...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmNo'))), action="CLICK")
                    return self.browserObject, True, "Success: Test Connection was Successful"
                except Exception as e:
                    utility.execLog("Failed to Test Connection :: Error --> %s" % str(e))
                    return self.browserObject, False, "Failed to Test Connection :: Error --> %s" % str(e)
        except Exception as e:
            return self.browserObject, False, "Exception generated while Testing Connection :: Error --> %s" % e

    def createBackup(self):
        """
        Description:
            API to create Backup
            INCOMPLETE: Backup Wait Logic, Pending Testing
        """
        try:
            utility.execLog("Clicking on Backup Now...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupButton'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Backup Now Form"
            # Verifying Dialog PopUp Title Header
            if "Backup Now" not in currentTitle:
                    utility.execLog("Failed to Verify Backup Now Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Backup Now"))
                    return self.browserObject, False, "Failed to Verify Backup Now Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Backup Now")
            utility.execLog("Backup Now Page Loaded and Verified Successfully")
            # Select Checkbox
            utility.execLog("Selecting 'Use Backup Directory Path and Encryption Password from Backup Settings and Details'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupCheck'))), action="CLICK")
            utility.execLog("Creating Backup Now...")
            # Test Connection
            self.browserObject, status, result = self.testConnectionCheck()
            if not status:
                return self.browserObject, False, result
            # Clicking 'Backup Now'
            time.sleep(3)
            utility.execLog("Clicking on 'Backup Now'...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.BackupAndRestoreObjects('backupSubmit'))), action="CLICK")
            # Checking for Error Creating Backup
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Create Backup :: Error -> %s" % errorMessage
            except:
                utility.execLog("Successfully Initiated Backup")
                # Waiting for 10 minutes for Backup To Complete
                backupStatus = None
                retry = 5
                counter = 120
                while counter > 0:
                    while retry > 0:
                        try:
                            backupStatus = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.BackupAndRestoreObjects('lastBackupStatus'))), action="GET_TEXT")
                            retry = 5
                            break
                        except:
                            time.sleep(2)
                            retry = retry - 1
                        counter = counter - 1
                # Fetching 'Last Backup Status'
                utility.execLog("Backup Status: %s" % backupStatus)
                if backupStatus == "IN_PROGRESS":
                    self.browserObject.refresh()
                    time.sleep(60)
                    return self.browserObject, False, "Backup Status '%s'...Exceeding Usual Time Limit" % backupStatus
                elif backupStatus == "FAILED":
                    return self.browserObject, False, "Failed to Create Backup"
                elif backupStatus == "SUCCESSFUL":
                    return self.browserObject, True, "Successfully Created Backup"
                else:
                    return self.browserObject, False, "Backup Status: %s" % backupStatus
        except Exception as e:
            return self.browserObject, False, "Exception generated while Creating Backup :: Error --> %s" % e

    def editBackupSettings(self, option="nfs"):
        """
        Edit Details of Backup Settings
        INCOMPLETE: Pending Testing
        """
        try:
            # Reading Values from config.ini into globalVars.py
            globalVars.configInfo = utility.readConfig(globalVars.configFile)
            globalVars.backupPath = globalVars.configInfo['Appliance']['backuppath']
            globalVars.backupDirusername = globalVars.configInfo['Appliance']['backupdirusername']
            globalVars.backupDirpassword = globalVars.configInfo['Appliance']['backupdirpassword']
            globalVars.encpassword = globalVars.configInfo['Appliance']['encpassword']
            # Clicking on 'Edit' Settings And Details
            utility.execLog("Clicking on 'Edit' Settings And Details for Backup...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('editSettingDetails'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Settings And Details Form"
            # Verifying Dialog PopUp Title Header
            if "Settings And Details" not in currentTitle:
                    utility.execLog("Failed to Verify Settings And Details Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Settings And Details"))
                    return self.browserObject, False, "Failed to Verify Settings And Details Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Settings And Details")
            utility.execLog("Settings And Details Page Loaded and Verified Successfully")
            utility.execLog("Editing Setting and Details Form...")
            # Setting Directory Path
            utility.execLog("Setting Directory Path: '%s'" % globalVars.backupPath)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupDirPath'))), action="CLEAR")
            # Formatting Directory Path based on 'Option'
            directoryPath = None
            if option == "nfs":
                path = globalVars.backupPath.rsplit('/', 1)
                directoryPath = path[0]
            elif option == "cifs":
                directoryPath = globalVars.ASMBackupPath[option]
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupDirPath'))), action="SET_TEXT", setValue=directoryPath)
            # Setting Directory Username
            utility.execLog("Setting Directory Username: '%s'" % globalVars.backupDirusername)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupDirUsername'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupDirUsername'))), action="SET_TEXT", setValue=globalVars.backupDirusername)
            # Setting Directory Password
            utility.execLog("Setting Directory Password: '%s'" % globalVars.backupDirpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupDirPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupDirPassword'))), action="SET_TEXT", setValue=globalVars.backupDirpassword)
            # Setting Encryption Password
            utility.execLog("Setting Encryption Password: '%s'" % globalVars.encpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupEncryptionPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupEncryptionPassword'))), action="SET_TEXT", setValue=globalVars.encpassword)
            # Setting Confirm Encryption Password
            utility.execLog("Setting Confirm Encryption Password: '%s'" % globalVars.encpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupEncryptionCPW'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupEncryptionCPW'))), action="SET_TEXT", setValue=globalVars.encpassword)
            # Test Connection
            self.browserObject, status, result = self.testConnectionCheck()
            if not status:
                return self.browserObject, False, result
            # Clicking 'Save'
            time.sleep(3)
            utility.execLog("Clicking on 'Save'...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.BackupAndRestoreObjects('backupSubmit'))), action="CLICK")
            # Checking for Editing Settings And Details
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Edit Settings And Details for Backup :: Error -> %s" % (errorMessage)
            except:
                return self.browserObject, True, "Backup Settings Successfully Saved"
        except Exception as e:
            return self.browserObject, False, "Exception generated while Editing Settings And Details for Backup :: Error --> %s" % e

    def getBackUpScheduleTime(self):
        """
        Description:
            API to Get Backup And Restore Schedule Time
            INCOMPLETE: Pending Testing
        """
        try:
            # Edit Details of Backup Settings: Input: config.ini
            # Hard-Coded: Tuesday 17.30
            self.browserObject, status, result = self.editBackupSettings()
            if status:
                utility.execLog(result)
                utility.execLog("Editing 'Automatically Scheduled Backups'...")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('editScheduledBackup'))), action="CLICK")
                # Verifying whether Dialog PopUp is displayed and fetching the Title Header
                try:
                    currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
                except:
                    return self.browserObject, False, "Unable to Load Automatically Scheduled Backups Form"
                # Verifying Dialog PopUp Title Header
                if "Automatically Scheduled Backups" not in currentTitle:
                    utility.execLog("Failed to Verify Automatically Scheduled Backups Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Automatically Scheduled Backups"))
                    return self.browserObject, False, "Failed to Verify Automatically Scheduled Backups Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Automatically Scheduled Backups")
                utility.execLog("Automatically Scheduled Backups Page Loaded and Verified Successfully")
                utility.execLog("Editing Automatically Scheduled Backups Form...")
                # Enabling Backup
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupEnable'))), action="CLICK")
                # Selecting Day
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupTuesday'))), action="CLICK")
                # Selecting Time
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupTime'))), action="SELECT", setValue='17:30', selectBy="VISIBLE_TEXT")
                # Clicking 'Save'
                utility.execLog("Clicking on Save...")
                self.handleEvent(EC.presence_of_element_located((By.ID, self.BackupAndRestoreObjects('backupScheduleSubmit'))), action="CLICK")
                utility.execLog("Saving...")
                # Checking for Error while Automatically Scheduling Backup
                try:
                    errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                    if errorRedBox:
                        errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('backupScheduleCancel'))), action="CLICK")
                        return self.browserObject, False, "Failed to Automatically Schedule Backup :: Error -> %s" % (errorMessage)
                except:
                    return self.browserObject, True, "Successfully Scheduled Backup"
            else:
                return self.browserObject, False, result
        except Exception as e:
            return self.browserObject, False, "Exception generated while Automatically Schedule Backup :: Error -> %s" % str(e)

    def restoreNowFromSetting(self):
        """
        Description:
            API to Restore Now from a Backup
            INCOMPLETE: Test Connection, Pending Testing
        """
        try:
            # Reading Values from config.ini into globalVars.py
            globalVars.configInfo = utility.readConfig(globalVars.configFile)
            globalVars.backupPath = globalVars.configInfo['Appliance']['backuppath']
            globalVars.backupDirusername = globalVars.configInfo['Appliance']['backupdirusername']
            globalVars.backupDirpassword = globalVars.configInfo['Appliance']['backupdirpassword']
            globalVars.encpassword = globalVars.configInfo['Appliance']['encpassword']
            # Clicking on 'Restore Now'
            utility.execLog("Clicking on 'Restore Now'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreButton'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Restore Now Form"
            # Verifying Dialog PopUp Title Header
            if "Restore Now" not in currentTitle:
                    utility.execLog("Failed to Verify Restore Now Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Restore Now"))
                    return self.browserObject, False, "Failed to Verify Restore Now Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Restore Now")
            utility.execLog("Restore Now Page Loaded and Verified Successfully")
            # Setting Directory Path
            utility.execLog("Setting Directory Path: '%s'" % globalVars.backupPath)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restorePath'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restorePath'))), action="SET_TEXT", setValue=globalVars.backupPath)
            # Setting Directory Username
            utility.execLog("Setting Directory Username: '%s'" % globalVars.backupDirusername)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreDirUsername'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreDirUsername'))), action="SET_TEXT", setValue=globalVars.backupDirusername)
            # Setting Directory Password
            utility.execLog("Setting Directory Password: '%s'" % globalVars.backupDirpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreDirPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreDirPassword'))), action="SET_TEXT", setValue=globalVars.backupDirpassword)
            # Setting Encryption Password
            utility.execLog("Setting Encryption Password: '%s'" % globalVars.encpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreEncryptionPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreEncryptionPassword'))), action="SET_TEXT", setValue=globalVars.encpassword)
            # Test Connection
            self.browserObject, status, result = self.testConnectionCheck()
            if not status:
                return self.browserObject, False, result
            # Clicking 'Restore Now'
            time.sleep(3)
            utility.execLog("Clicking on 'Restore Now'...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.BackupAndRestoreObjects('restoreSubmit'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Restore Now Confirmation"
            # Verifying Dialog PopUp Title Header
            if "Confirm" not in currentTitle:
                utility.execLog("Failed to Verify Restore Now Confirmation Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm"))
                return self.browserObject, False, "Failed to Verify Restore Now Confirmation Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")
            utility.execLog("Restore Now Confirmation Page Loaded and Verified Successfully")
            # Confirm - 'Yes'
            utility.execLog("Clicking on 'Yes' to Restore Now'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            # Checking for Error while Restoring from Backup
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.BackupAndRestoreObjects('restoreCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Restore From Backup :: Error -> %s" % (errorMessage)
            except:
                utility.execLog("Restore In-Progress...")
            # Wait for Login Page - Max Wait Time --> 60minutes
            start_time = datetime.datetime.now()
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('LoginLogo'))), wait_time=3600, freq=60)
            end_time = datetime.datetime.now()
            diff = end_time - start_time
            utility.execLog("Time to Restore Appliance: {} min".format(diff.seconds / 60))
            if (self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginUsername'))))):
                utility.execLog("Successfully Restored Appliance")
                return self.browserObject, True, "Successfully Restored Appliance"
            else:
                return self.browserObject, False, "Failed to Load Login Page after ASM Appliance Restore from Backup"
        except Exception as e:
            return self.browserObject, False, "Exception generated while Restoring Appliance From Backup :: Error -> %s" % str(e)

    def getBackUp_ScheduleTime(self):
        # Updated to getCurrentAvailableApplianceVersions
        # ToDo: Update Manager.py
        pass