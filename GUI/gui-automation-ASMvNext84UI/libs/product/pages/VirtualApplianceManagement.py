"""
Author: P Suman/Rajeev Kumar/Raj Patel/Saikumar Kalyankrishnan
Created/Modified: Oct 6th 2015/Feb 1st 2016/Feb 4th 2016/Feb 22nd 2017
Description: Functions/Operations related to Virtual Appliance Management Page
"""

from libs.product.pages import *
from CommonImports import *
from time import sleep
from libs.product.objects.Common import Common
from libs.product.objects.VirtualApplianceManagement import VirtualApplianceManagement

class VirtualApplianceManagement(Navigation, Common, VirtualApplianceManagement):
    """
    Description:
        Class which includes all Functions/Operations related to Virtual Appliance Management Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Virtual Appliance Management class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Virtual Appliance Management"
        utility.execLog("Virtual Appliance Management")
        
    def loadPage(self):
        """
        Description:
            API to load Virtual Appliance Management Page
        """
        try:
            utility.execLog("Loading Virtual Appliance Management Page...")
            self.browserObject, status, result = self.selectOption("Virtual Appliance Management")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Virtual Appliance Management Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Virtual Appliance Management Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualApplianceManagementObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualApplianceManagementObjects('title'))), action="GET_TEXT")
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
            API to get Virtual Appliance Management Details. Expandable to return additional details
        Sample Output:
            {'Time Zone': '(UTC-06:00) Central Time (US & Canada)',
            'Preferred NTP Server': '128.138.140.44',
            'Secondary NTP Server': '',
            'Current Virtual Appliance Version': '8.4.0.7799',
            'Available Virtual Appliance Version': '8.4.0.7832',
            'Repository Path': 'http://gtie-artifactory.us.dell.com:8081/artifactory/yum-asm-840-release-rep/centos/6/x86_64/',
            'Release Notes': 'http://dell.com/asmdocs',
            'Proxy Settings': 'Disabled',
            'DHCP Settings': 'Enabled',
            'Service Tag': 'ASM-840',
            'Port Numbers': '22,80,135',
            }

        """
        optionDict = {}
        try:
            utility.execLog("Reading Virtual Appliance Management Details...")
            # Fetching Time Zone and NTP Settings
            self.browserObject, status, result = self.getNTPTimezone()
            if not status:
                return self.browserObject, False, result
            optionDict["Preferred NTP Server"] = result['Preferred']
            optionDict["Secondary NTP Server"] = result['Secondary']
            optionDict["Time Zone"] = result['Timezone']
            # Fetching Update Repository Path
            self.browserObject, status, result = self.getCurrentAvailableApplianceVersions()
            if not status:
                return self.browserObject, False, result
            optionDict["Current Virtual Appliance Version"] = result['Current']
            optionDict["Available Virtual Appliance Version"] = result['Available']
            utility.execLog("Fetching Repository Path...")
            optionDict["Repository Path"] = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentRepoPath'))), action="GET_TEXT")
            utility.execLog("Fetching Release Notes...")
            optionDict["Release Notes"] = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualApplianceManagementObjects('releaseNotes'))), action="GET_TEXT")
            # Fetching Proxy Settings
            utility.execLog("Fetching Proxy Settings...")
            optionDict["Proxy Settings"] = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentProxy'))), action="GET_TEXT")
            # Fetching DHCP Settings
            utility.execLog("Fetching DHCP Settings...")
            optionDict["DHCP Settings"] = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentDHCPStatus'))), action="GET_TEXT")
            # Fetching Service Tag
            utility.execLog("Fetching Service Tag...")
            optionDict["Service Tag"] = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentServiceTag'))), action="GET_TEXT")
            # Fetching Port Numbers Used for IP Verification
            utility.execLog("Fetching Port Numbers Used for IP Verification...")
            optionDict["Port Numbers"] = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentIPPorts'))), action="GET_TEXT")
            utility.execLog("Virtual Appliance Management Details :: %s" % (optionDict))
            return self.browserObject, True, optionDict          
        except Exception as e:
            return self.browserObject, False, "Unable to read Virtual Appliance Management Details :: Error -> %s" % (str(e) + format_exc())
    
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Virtual Appliance Management Page
        """
        optionDict = {}
        try:
            option = {"Update Virtual Appliance": self.VirtualApplianceManagementObjects('updateAppliance'),
                    "Generate Troubleshooting Bundle": self.VirtualApplianceManagementObjects('generateTSB'),
                    "Time Zone and NTP": self.VirtualApplianceManagementObjects('editTZNTP'),
                    "Update Repository Path": self.VirtualApplianceManagementObjects('editRepoPath'),
                    "Proxy": self.VirtualApplianceManagementObjects('editProxy'),
                    "DHCP": self.VirtualApplianceManagementObjects('editDHCP'),
                    "Generate Certificate Signing Request": self.VirtualApplianceManagementObjects('generateCert'),
                    "Upload Certificate": self.VirtualApplianceManagementObjects('uploadCert'),
                    "License Management": self.VirtualApplianceManagementObjects('addLicense'),
                    "ServiceTag": self.VirtualApplianceManagementObjects('editServiceTag')
                    }
            for optName, optValue in option.items():
                utility.execLog("Reading Option: '%s'" % optName)
                # Checking for 'disabled' Attribute
                disabled = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                if "true" in disabled:
                    optionDict[optName] = "Disabled"
                else:
                    optionDict[optName] = "Enabled"
            utility.execLog("Virtual Appliance Management Options :: %s" % (optionDict))
            return self.browserObject, True, optionDict            
        except Exception as e:
            return self.browserObject, False, "Unable to read Virtual Appliance Management Options :: Error -> %s" % (str(e) + format_exc())

    def verifyAllLink(self):
        """
        Verify Settings Page Links
        """
        settingsList = ["Add-On Modules", "Backup and Restore", "Credentials Management", "Jobs", "Logs", "Networks", "Repositories", "Users", "Virtual Appliance Management", "Virtual Identity Pools"]
        currentList = []
        try:
            utility.execLog("Loading Settings Page...")
            self.browserObject, status, result = self.selectOption("Settings")
            if not status:
                return self.browserObject, status, result
            time.sleep(3)
            settings = self.VirtualApplianceManagementObjects('settingsList')
            # Parsing through the Links
            for setting in range(1, 12):
                if setting != 4:
                    getSettings = "//*[@id='%s']//li[%i]/a[contains(@ng-click, 'settingslist')]" % (settings, setting)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, getSettings)), action="CLICK")
                    time.sleep(2)
                    # Get Header/PageTitle
                    if setting in (1, 5, 9):
                        getTitle = "//div[@id='content-main']//h2"
                    else:
                        getTitle = "//div[@id='content-main']//h1"
                    title = self.handleEvent(EC.element_to_be_clickable((By.XPATH, getTitle)), action="GET_TEXT")
                    utility.execLog("Verifying Option: %s" % title)
                    currentList.append(title)
            utility.execLog("Current List: %s" % currentList)
            # Comparing the two list: Expected v/s Actual
            if cmp(settingsList, currentList) == 0:
                return self.browserObject, True, "All Settings Links Verified Successfully :: %s" % currentList
            else:
                return self.browserObject, False, "Failed Verifications of Settings Links :: %s" % (list(set(settingsList)-set(currentList)))
        except Exception as e:
            return self.browserObject, False, "Exception generated while verifying All Settings Link :: Error --> '%s'" % e

    def setNTPTimezone(self, primaryNTP, timeZoneId='11', secondaryNTP=None):
        """
        Description: 
            API to Set Time Zone and NTP
        """
        try:
            utility.execLog("Clicking on Edit Time Zone and NTP Settings...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('editTZNTP'))), action="CLICK")
            utility.execLog("Clicked on Edit Time Zone and NTP Settings")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Update Time Zone and NTP Settings Form"
            # Verifying Dialog PopUp Title Header
            if "Time Zone and NTP Settings" not in currentTitle:
                    utility.execLog("Failed to Verify Update Time Zone and NTP Settings Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Time Zone and NTP Settings"))
                    return self.browserObject, False, "Failed to Verify Update Time Zone and NTP Settings Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Time Zone and NTP Settings")
            utility.execLog("Update Time Zone and NTP Settings Page Loaded and Verified Successfully")
            # Setting Time Zone and NTP
            utility.execLog("Setting Time Zone to (UTC-06:00) Central Time (US & Canada) and Preferred NTP to %s" % primaryNTP)
            utility.execLog("Setting Time Zone ID: '%s'" % timeZoneId)
            # Change Format of Timezone Value as per 8.4.0
            timeZoneID = "string:"+timeZoneId
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updateTimezone'))), action="SELECT", setValue=timeZoneID, selectBy="VALUE")
            utility.execLog("Setting Primary NTP: '%s'" % primaryNTP)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updatePreferredNTP'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updatePreferredNTP'))), action="SET_TEXT", setValue=primaryNTP)
            if secondaryNTP:
                utility.execLog("Setting Secondary NTP: '%s'" % secondaryNTP)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updateSecondaryNTP'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updateSecondaryNTP'))), action="SET_TEXT", setValue=secondaryNTP)
            # Clicking 'Save'
            utility.execLog("Clicking on Save...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualApplianceManagementObjects('VAMSave'))), action="CLICK")
            utility.execLog("Saving...")
            time.sleep(5)
            # Checking for Error Editing Time Zone and NTP
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('VAMCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Update Time Zone and NTP Settings :: Error -> %s" % (errorMessage)
            except:
                # Validation: Checking whether updated Time Zone and NTP Settings are reflected
                self.browserObject, status, result = self.getNTPTimezone()
                if status:
                    # Verifying only Preferred NTP
                    if result['Preferred'] == primaryNTP:
                        return self.browserObject, True, "Successfully Updated Time Zone and NTP Settings"
                    else:
                        return self.browserObject, False, "Failed to Update Time Zone and NTP Settings :: Error -> %s" % ("Validation Error")
                else:
                    return self.browserObject, False, "Failed to Update Time Zone and NTP Settings :: Error -> %s" % result
        except Exception as e:
                return self.browserObject, False, "Exception while updating Time Zone and NTP Settings :: Error -> %s" % (str(e) + format_exc())

    def getNTPTimezone(self):
        """
        Description:
            API to Get Current Time Zone and NTP Servers.
        Output:
            Dict {  'Preferred':
                    'Secondary':
                    'Timezone':
                    }
        """
        ntptz = {}
        try:
            # Fetch Current Preferred NTP Server
            utility.execLog("Fetching Current Preferred NTP Server...")
            pNTP = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentPNTP'))), action="GET_TEXT")
            ntptz['Preferred'] = pNTP
            # Fetch Current Secondary NTP Server
            utility.execLog("Fetching Current Secondary NTP Server...")
            sNTP = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentSNTP'))), action="GET_TEXT")
            ntptz['Secondary'] = sNTP
            # Fetch Current TimeZone
            utility.execLog("Fetching Current TimeZone...")
            timeZone = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentTZ'))), action="GET_TEXT")
            ntptz['Timezone'] = timeZone
            utility.execLog(ntptz)
            # Validation: Checking if Preferred NTP fetched is not Null
            if pNTP and timeZone:
                return self.browserObject, True, ntptz
            else:
                return self.browserObject, False, ntptz
        except Exception as e:
            return self.browserObject, False, "Exception while fetching Current Time Zone and NTP Servers :: Error --> '%s'" % (str(e) + format_exc())

    def getCurrentAvailableApplianceVersions(self):
        """
        Description:
            API to get Virtual Appliance Current and Available Version
            INCOMPLETE: Get Current Version from 'About' and compare with 'currentVersion', not sure if required
        Output:
            {'Current':
            'Available':
            }

        """
        versions = {}
        try:
            utility.execLog("Fetching Appliance Current Version & Available Version...")
            # Get Current Version
            currentVersion = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentVersion'))), action="GET_TEXT")
            utility.execLog("Current Virtual Appliance Version: %s" % currentVersion)
            versions['Current'] = currentVersion
            # Get Available Version
            availableVersion = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('availableVersion'))), action="GET_TEXT")
            utility.execLog("Available Virtual Appliance Version: %s" % availableVersion)
            versions['Available'] = availableVersion
            utility.execLog(versions)
            return self.browserObject, True, versions
        except Exception as e:
            return self.browserObject, False, "Exception while fetching Appliance Current Version & Available Version :: Error -> %s" % str(e)

    def setRepoPath(self):
        """
        Description: Set the Update Repository Path based on the ASM Version
        Input: config.ini
        """
        try:
            # Fetching Repository Path from config.ini
            updatePath = str(globalVars.configInfo['Appliance']['repopath'])
            utility.execLog("Updating Repository Path in Virtual Appliance Management...")
            utility.execLog("Clicking on 'Edit' Repository Path Option...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('editRepoPath'))), action="CLICK")
            utility.execLog("Clicked on Edit Repository Path")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Update Repository Path Form"
            # Verifying Dialog PopUp Title Header
            if "Update Repository Path" not in currentTitle:
                    utility.execLog("Failed to Verify Update Repository Path Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Update Repository Path"))
                    return self.browserObject, False, "Failed to Verify Update Repository Path Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Update Repository Path")
            utility.execLog("Update Repository Path Page Loaded and Verified Successfully")
            # Setting Virtual Appliance Repository Path
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updateRepoPath'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updateRepoPath'))), action="SET_TEXT", setValue=updatePath)
            # Clicking 'Save'
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('VAMSave'))), action="CLICK")
            time.sleep(5)
            # Checking for Error Updating Repository Path
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=30)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('VAMCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Update Virtual Appliance Repository Path :: Error -> %s" % (errorMessage)
            except:
                # Validation: Checking whether updated path is reflected
                getCurrentRepoPath = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentRepoPath'))), action="GET_TEXT")
                if getCurrentRepoPath == updatePath:
                    return self.browserObject, True, "Successfully Updated Virtual Appliance Repository Path: %s" % getCurrentRepoPath
                else:
                    return self.browserObject, False, "Failed to Update Virtual Appliance Repository Path :: Error -> %s" % ("Validation Error")
        except Exception as e:
            return self.browserObject, False, "Exception generated while Updating Virtual Appliance Repository Path  :: Error -> %s" % str(e)

    def upgradeAppliance(self):
        """
        Description:
            API to update and verify Virtual Appliance upgrade from Current Version to Available Version
            INCOMPLETE: Pending Testing
        """
        try:
            upgradePossible = False
            # Verify if the Repository Path is updated
            self.browserObject, status, result = self.setRepoPath()
            utility.execLog(result)
            if not status:
                return self.browserObject, False, result
            # Get Current & Available Versions
            self.browserObject, status, currentVersion, availableVersion = self.getCurrentAvailableApplianceVersions()
            if status:
                currentVersion = currentVersion.replace(".", "")
                availableVersion = availableVersion.replace(".", "")
                if int(availableVersion) > int(currentVersion):
                    upgradePossible = True
                elif int(availableVersion) == int(currentVersion):
                    return self.browserObject, True, "Appliance already upgraded to the latest Version: %s" % currentVersion
                else:
                    return self.browserObject, False, "Invalid Current & Available Versions. Please check the Repository Path."
            else:
                return self.browserObject, False, result
            # Based on 'upgradePossible', update Appliance to 'availableVersion'
            if upgradePossible:
                utility.execLog("Clicking on 'Update Virtual Appliance'...")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('updateAppliance'))), action="CLICK")
                # Verifying whether Dialog PopUp is displayed and fetching the Title Header
                try:
                    currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
                except:
                    return self.browserObject, False, "Unable to Load Update Appliance Confirmation"
                # Verifying Dialog PopUp Title Header
                if "Warning" not in currentTitle:
                    utility.execLog("Failed to Verify Update Appliance Confirmation Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Warning"))
                    return self.browserObject, False, "Failed to Verify Update Appliance Confirmation Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Warning")
                utility.execLog("Update Appliance Confirmation Page Loaded and Verified Successfully")
                # Confirm - 'Yes'
                utility.execLog("Clicking on 'Yes' to 'Update Virtual Appliance'...")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                utility.execLog("Virtual Appliance Update In-Progress...")
                # Wait for Login Page - Max Wait Time --> 60minutes
                start_time = datetime.datetime.now()
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('LoginLogo'))), wait_time=3600, freq=60)
                end_time = datetime.datetime.now()
                diff = end_time - start_time
                utility.execLog("Time to Upgrade Appliance: {} min".format(diff.seconds / 60))
                # Login using admin Username and Password
                if self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginUsername')))):
                    # Enter Username 'admin'
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginUsername'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginUsername'))), action="SET_TEXT", setValue="admin")
                    # Enter Password 'admin'
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginPassword'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginPassword'))), action="SET_TEXT", setValue="admin")
                    # Click Login
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('Login'))), action="CLICK")
                    # Waiting for 'Landing Page'
                    time.sleep(20)
                    # Navigate to & Validate Virtual Appliance Management Page
                    self.browserObject, status, result = self.loadPage()
                    utility.execLog(result)
                    if not status:
                        return self.browserObject, False, result
                    # Get Updated Current & Available Versions
                    self.browserObject, status, currentVersion, availableVersion = self.getCurrentAvailableApplianceVersions()
                    if status:
                        currentVersion = currentVersion.replace(".", "")
                        availableVersion = availableVersion.replace(".", "")
                        if int(availableVersion) == int(currentVersion):
                            return self.browserObject, True, "ASM Appliance successfully updated to the Latest Version: %s" % currentVersion
                        else:
                            return self.browserObject, False, "Failed to update ASM Appliance to the Latest Version: %s :: Error --> %s" % (currentVersion, "Validation Error")
                    else:
                        return self.browserObject, False, result
                else:
                    return self.browserObject, False, "Failed to Load Login Page after ASM Appliance Update"
            else:
                return self.browserObject, False, "Failed to update ASM Appliance to the Latest Version: %s :: Error --> %s" % (currentVersion, "Invalid ASM Appliance Version Error")
        except Exception as e:
            return self.browserObject, False, "Exception generated while Updating ASM Appliance  :: Error -> %s" % str(e)

    def getDHCPStatus(self):
        """
        Description:
            API to Get Current DHCP Status
        """
        try:
            utility.execLog("Fetching Current DHCP Status...")
            getCurrentDHCPStatus = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentDHCPStatus'))), action="GET_TEXT")
            utility.execLog("DHCP Status: '%s'" % getCurrentDHCPStatus)
            # Validation: Checking if Current DHCP Status fetched is not Null
            if getCurrentDHCPStatus:
                return self.browserObject, True, getCurrentDHCPStatus
            else:
                return self.browserObject, False, "Failed to fetch Current DHCP Status"
        except Exception as e:
            return self.browserObject, False, "Exception while fetching Current DHCP Status :: Error -> '%s'" % str(e)

    def editDHCPSetting(self, resourceAction="", subnet="", netMask="", startIPAddress="", endIPAddress="", gateway="", dns="", domain=""):
        """
        Description:
            Enable/Disable DHCP Setting based on 'resourceAction'
            If already Enabled and resourceAction=Enable, doesn't edit already existing values, simply Cancels.
            INCOMPLETE: Pending Testing
        """
        try:
            utility.execLog("Clicking on Edit DHCP Setting")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('editDHCP'))), action="CLICK")
            utility.execLog("Clicked on Edit DHCP Setting")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Update Repository Path Form"
            # Verifying Dialog PopUp Title Header
            if "DHCP Settings" not in currentTitle:
                    utility.execLog("Failed to Verify DHCP Settings Page :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "DHCP Settings")
                    return self.browserObject, False, "Failed to Verify DHCP Settings Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "DHCP Settings")
            utility.execLog("DHCP Settings Page Loaded and Verified Successfully")
            # Get Enable DHCP/PXE Server Value
            checkBoxValue = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('checkEnableDHCP'))), action="IS_SELECTED")
            if resourceAction == 'Enable':
                if not checkBoxValue:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('checkEnableDHCP'))), action="CLICK")
                    # Entering Subnet
                    utility.execLog("Setting Subnet: '%s'" % subnet)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPSubnet'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPSubnet'))), action="SET_TEXT", setValue=subnet)
                    # Entering Netmask
                    utility.execLog("Setting Netmask: '%s'" % netMask)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPNetmask'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPNetmask'))), action="SET_TEXT", setValue=netMask)
                    # Entering Starting IP Address
                    utility.execLog("Setting Starting IP Address: '%s'" % startIPAddress)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPStartIP'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPStartIP'))), action="SET_TEXT", setValue=startIPAddress)
                    utility.execLog("Setting Ending IP Address: '%s'" % endIPAddress)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPEndIP'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPEndIP'))), action="SET_TEXT", setValue=endIPAddress)
                    # Entering Lease Time
                    utility.execLog("Setting Default Lease Time...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPLeaseDays'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPLeaseHours'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPLeaseMinutes'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPLeaseSeconds'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    utility.execLog("Setting Max Lease Time...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPMaxDays'))), action="SELECT", setValue='12', selectBy="VISIBLE_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPMaxHours'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPMaxMinutes'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPMaxSeconds'))), action="SELECT", setValue='03', selectBy="VISIBLE_TEXT")
                    # Entering Default Gateway
                    utility.execLog("Setting Default Gateway: '%s'" % gateway)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPGateway'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPGateway'))), action="SET_TEXT", setValue=gateway)
                    # Entering DNS Server
                    utility.execLog("Setting DNS Server: '%s'" % dns)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPDNS'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPDNS'))), action="SET_TEXT", setValue=dns)
                    # Entering Domain
                    utility.execLog("Setting Domain: '%s'" % domain)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPDomain'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DCHPDomain'))), action="SET_TEXT", setValue=domain)
                    # Clicking 'Save'
                    utility.execLog("Clicking on Save...")
                    self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualApplianceManagementObjects('DHCPSubmit'))), action="CLICK")
                    utility.execLog("Saving...")
                    # Checking for Error Editing DHCP Setting
                    try:
                        errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                        if errorRedBox:
                            errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('VAMCancel'))), action="CLICK")
                            return self.browserObject, False, "Failed to Edit DHCP Setting :: Error -> %s" % (errorMessage)
                    except:
                        time.sleep(3)
                        self.browserObject, status, result = self.getDHCPStatus()
                        if not status:
                            return self.browserObject, status, result
                        else:
                            if "Enabled" in result:
                                return self.browserObject, True, "Successfully Edited & Saved DHCP Setting"
                            else:
                                return self.browserObject, False, "Failed to Edit DHCP Setting :: Error -> Validation Error"
                else:
                    utility.execLog("Clicking on Cancel...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPCancel'))), action="CLICK")
                    return self.browserObject, True, "Successfully Saved Existing DHCP Settings"
            # Disable DHCP/PXE Server Value
            elif resourceAction == 'Disable':
                if checkBoxValue:
                    utility.execLog("Click To Un-Check DHCP/PXE Server Value...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('checkEnableDHCP'))), action="CLICK")
                    # Clicking 'Save'
                    utility.execLog("Clicking on Save...")
                    self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualApplianceManagementObjects('DHCPSubmit'))), action="CLICK")
                    utility.execLog("Saving...")
                    return self.browserObject, True, "Successfully Disabled DHCP Settings"
                else:
                    utility.execLog("Clicking on Cancel...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('DHCPCancel'))), action="CLICK")
                    return self.browserObject, True, "DHCP Settings Already Disabled"
        except Exception as e:
            return self.browserObject, False, "Exception generated while Editing/Saving DHCP Setting :: Error -> %s" % str(e)

    def generateTroubleShootingBundle(self):
        """
        Description: 
            API to Generate TroubleShooting Bundle
        """
        try:
            utility.execLog("Clicking on Generate TroubleShooting Bundle...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('generateTSB'))), action="CLICK")
            utility.execLog("Clicked on Generate TroubleShooting Bundle")
            time.sleep(30)
            # Checking for Error Generating TroubleShooting Bundle
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Generate TroubleShooting Bundle :: Error -> '%s'" % errorMessage
            except:
                return self.browserObject, True, "Successfully initiated Generate TroubleShooting Bundle from Settings --> Virtual Appliance Management"
        except Exception as e:
            return self.browserObject, False, "Exception while Generating TroubleShooting Bundle :: Error -> %s" % (str(e) + format_exc())

    def generateCertificate(self):
        """
        Description:
            API to Generate Certificate Signing Request & Upload Certificate
            INCOMPLETE: Pending Testing
        """
        try:
            self.browserObject, status, validFromDate = self.getCertificateDate()
            if not status:
                return self.browserObject, False, validFromDate
            utility.execLog("Certificate Valid From: %s" % str(validFromDate))
            utility.execLog("Generating Certificate Signing Request...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('generateCert'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Generate Certificate Signing Request Form"
            # Verifying Dialog PopUp Title Header
            if "Generate Certificate Signing Request" not in currentTitle:
                    utility.execLog("Failed to Verify Generate Certificate Signing Request Form :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "Generate Certificate Signing Request")
                    return self.browserObject, False, "Failed to Verify Generate Certificate Signing Request Form :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Generate Certificate Signing Request")
            utility.execLog("Generate Certificate Signing Request Form Loaded and Verified Successfully")
            # Entering Certificate Details
            utility.execLog("Setting Business Name: Dell")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certBusinessName'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certBusinessName'))), action="SET_TEXT", setValue="Dell")
            utility.execLog("Setting Department Name: ASM")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certDeptName'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certDeptName'))), action="SET_TEXT", setValue="ASM")
            utility.execLog("Setting Locality: Round Rock")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certLocality'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certLocality'))), action="SET_TEXT", setValue="Round Rock")
            utility.execLog("Setting State: Texas")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certState'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certState'))), action="SET_TEXT", setValue="Texas")
            utility.execLog("Setting Country: United States")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certCountry'))), action="SELECT", setValue='United States', selectBy="VISIBLE_TEXT")
            utility.execLog("Setting Email: test@test.com")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certEmail'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certEmail'))), action="SET_TEXT", setValue="test@test.com")
            # Clicking 'Generate'
            utility.execLog("Clicking on Generate...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualApplianceManagementObjects('certGenerate'))), action="CLICK")
            utility.execLog("Generating...")

            # ===INCOMPLETE FROM HERE===
            utility.execLog("Successfully generated CSR")
            self.browserObject.find_element_by_xpath("//*[@id='signingRequestForm']//*[@id ='idButtonDownloadCSR']").click()
            utility.execLog("Successfully clicked to download CSR")
            time.sleep(10)
            element1 = self.browserObject.find_element_by_xpath("//*[@id='page_downloadSigningCert']//*[@id='txtCSR']")
            utility.execLog("Successfully clicked to download CSR %s"%str(element1))
            time.sleep(5)
            txt=element1.text
            utility.execLog(txt)
            time.sleep(3)
            self.browserObject.find_element_by_xpath("(.//*[@id='cancelBtn'])[2]").click()
            time.sleep(5)
            self.browserObject.find_element_by_xpath("(.//*[@id='cancelBtn'])[1]").click()
            time.sleep(5)
            
            self.browserObject = globalVars.browserObject
            driver = self.browserObject
            time.sleep(2)
            
            time.sleep(5)
            body = driver.find_element_by_tag_name("body")
            body.send_keys(Keys.CONTROL + 't')
            for h in driver.window_handles[1:]:
                driver.switch_to_window(h)
                time.sleep(10)
            utility.execLog("Switched to next Tab")
            driver.get("http://www.getacert.com")
            driver.find_element_by_xpath("html/body/table/tbody/tr[2]/td[1]/a[2]").click()
            time.sleep(5)
            element2 = driver.find_element_by_xpath("html/body/form/table/tbody/tr[2]/td/textarea")
            utility.execLog("Object of webelement %s"%str(element2))
            element2.send_keys(txt)
            time.sleep(10)
            driver.find_element_by_xpath("html/body/form/table/tbody/tr[3]/td/input").click()
            time.sleep(10)
            driver.find_element_by_xpath("html/body/ul/li[1]/p[1]/a").click()
            time.sleep(5)
            for h in driver.window_handles[2:]:
                driver.switch_to_window(h)
                time.sleep(10)
            
            html_source = self.browserObject.page_source
            htmpl_source1 = html_source[121:]
            htmpl_source2 = htmpl_source1[:-21]
            
            
            f = open('docs\\CSRCertificate.cer', 'w')
            f.write(htmpl_source2)
            utility.execLog("Successfully write the file")
            time.sleep(5)
            f.close()
            driver.close()
            time.sleep(5)
            driver.switch_to_window(driver.window_handles[1])
            driver.close()
            driver.switch_to_window(driver.window_handles[0])
            self.browserObject.find_element_by_id("uploadCertLink").click()
            time.sleep(7)
              
            self.browserObject.find_element_by_css_selector('input[type="file"]').clear()
            file_path = os.path.abspath("docs\\CSRCertificate.cer")
            utility.execLog("File path:")
            utility.execLog(file_path)
            time.sleep(5)
            new_file_path = file_path.replace("\\", "\\\\")
            utility.execLog(new_file_path)
            self.browserObject.find_element_by_css_selector('input[type="file"]').send_keys(new_file_path)
            time.sleep(5)             
            self.browserObject.find_element_by_id("btnSubmit").click()
            time.sleep(7)
            self.browserObject.find_element_by_id("submit_confirm_form").click()
            count=0
            while count<7:
                time.sleep(100)
                try:
                    self.browserObject.find_element_by_id("btnLogin")
                    driver.close()
                    utility.execLog("SSLCertificate generated and uploaded successfully")
                    return self.browserObject, True, validFromDate
                except:
                    count = count + 1
                    utility.execLog("Restoring the appliance please wait count is: %i"%count)
                    continue
        except Exception as e:
            return self.browserObject, False, "Exception while generating and uploading SSLCertificate :: Error -> %s" % str(e)
        
    def getCertificateDate(self):
        """
        Description:
            API to fetch Certificate Date
        """
        try:
            utility.execLog("Fetching Certificate Validity Start Date...")
            validFromDate = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('certValidFrom'))), action="GET_TEXT")
            utility.execLog("Certificate Valid From: %s" % str(validFromDate))
            return self.browserObject, True, validFromDate
        except Exception as e:
            return self.browserObject, False, "Exception while fetching Certificate Details :: Error --> %s" % str(e)
    
    def verifyCertificate(self, oldValidFromDate):
        """
        Description:
            API to verify SSL Certificates
            INCOMPLETE: Pending Testing
        """
        try:
            if os.path.exists("docs\\CSRCertificate.cer"):
                os.remove("docs\\CSRCertificate.cer")
            self.browserObject, status, validFromDate = self.getCertificateDate()
            if not status:
                return self.browserObject, False, validFromDate
            utility.execLog("Certificate Valid From: %s" % str(validFromDate))
            # Checking for updated Certificate Validity
            if(oldValidFromDate != validFromDate):
                utility.execLog("Successfully verified SSL Certificate")
                return self.browserObject, True, "Successfully verified SSL Certificate"
            else:
                utility.execLog("Failed to Verify SSL Certificate")
                return self.browserObject, False, "Failed to Verify SSL Certificate"
        except Exception as e:
            return self.browserObject, False, "Exception while verifying SSL Certificate :: Error -> %s" % str(e)

    def verifyLicenseTypeVirtuApp(self):
        # Updated to verifyLicense
        # ToDo: Update Manager.py
        pass

    def addLicense(self, license_file_path, message=""):
        """
        Description:
            API to Add/Upload License
        """
        try:
            # Get Current No. Of Resources
            numOfCurrentResources = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('mainTotalResources'))), action="GET_TEXT")
            utility.execLog("Number Of Resources:  %s" % numOfCurrentResources)
            utility.execLog("Clicking on Add License...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('addLicense'))), action="CLICK")
            utility.execLog("Clicked on Add License")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Add License Form"
            # Verifying Dialog PopUp Title Header
            if "Add License" not in currentTitle:
                    utility.execLog("Failed to Verify Add License Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Add License"))
                    return self.browserObject, False, "Failed to Verify Add License Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Add License")
            utility.execLog("Add License Page Loaded and Verified Successfully")
            # Setting License File Path
            utility.execLog("Setting License File Path...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('uploadLicense'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('uploadLicense'))), action="SET_TEXT", setValue=license_file_path)
            time.sleep(2)
            # Clicking 'Save'
            utility.execLog("Clicking on Save...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualApplianceManagementObjects('saveLicense'))), action="CLICK")
            utility.execLog("Saving...")
            time.sleep(3)
            # Checking for Error Adding License
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('cancelLicense'))), action="CLICK")
                    return self.browserObject, False, "Failed to Add License from '%s' :: Error -> %s" % (license_file_path, errorMessage)
            except:
                # Validation: Checking if No. Of Resources is not 0 and increased due to addition of License
                self.browserObject, status, licenseResult = self.verifyLicense()
                if status:
                    if int(licenseResult['Resources']) > int(numOfCurrentResources):
                        return self.browserObject, True, "Successfully Added License from '%s'" % license_file_path
                    else:
                        return self.browserObject, False, "Failed to Verify License from '%s' :: Error -> %s" % (license_file_path, "Validation Error")
                else:
                    return self.browserObject, False, "Failed to Verify License from '%s' :: Error -> %s" % (license_file_path, licenseResult)
        except Exception as e:
                return self.browserObject, False, "Exception generated while Adding License from '%s' :: Error -> %s" % (license_file_path, str(e))
        
    def verifyLicense(self):
        """
        Description:
            API to verify License
        Output:
            {'Type':
             'Resources':
             'Used':
             'Available':
             }
        """
        licenseResult = {}
        try:
            utility.execLog("Fetching License Details...")
            # Get License Type
            licenseType = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('mainLicenseType'))), action="GET_TEXT")
            utility.execLog("License Type: %s" % licenseType)
            licenseResult['Type'] = licenseType
            # Get No. Of Resources
            numOfResources = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('mainTotalResources'))), action="GET_TEXT")
            utility.execLog("Number Of Resources:  %s" % numOfResources)
            licenseResult['Resources'] = numOfResources
            # Get No. Of Used Resources
            numOfUsedResources = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('mainResourcesUsed'))), action="GET_TEXT")
            utility.execLog("Number Of Used Resources:  %s" % numOfUsedResources)
            licenseResult['Used'] = numOfUsedResources
            # Get No. Of Available Resources
            numOfAvailResources = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('mainResourcesAvailable'))), action="GET_TEXT")
            utility.execLog("Number Of Available Resources:  %s" % numOfAvailResources)
            licenseResult['Available'] = numOfAvailResources
            # Check for Expiration Date in 8.4.0
            utility.execLog(licenseResult)
            return self.browserObject, True, licenseResult
        except Exception as e:
            return self.browserObject, False, "Exception while fetching/verifying License Details -> %s" % str(e)

    def editServiceTag(self, serviceTag):
        """
        Description:
            API to Edit Service Tag
        """
        try:
            utility.execLog("Clicking on Edit Service Tag...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('editServiceTag'))), action="CLICK")
            utility.execLog("Clicked on Edit Service Tag")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Update Repository Path Form"
            # Verifying Dialog PopUp Title Header
            if "Edit Service Tag" not in currentTitle:
                    utility.execLog("Failed to Verify Edit Service Tag Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Edit Service Tag"))
                    return self.browserObject, False, "Failed to Verify Edit Service Tag Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Edit Service Tag")
            utility.execLog("Edit Service Tag Page Loaded and Verified Successfully")
            # Setting Service Tag
            utility.execLog("Setting Service Tag...")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualApplianceManagementObjects('updateServiceTag'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualApplianceManagementObjects('updateServiceTag'))), action="SET_TEXT", setValue=serviceTag)
            # Clicking 'Save'
            utility.execLog("Clicking on Save...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualApplianceManagementObjects('saveServiceTag'))), action="CLICK")
            utility.execLog("Saving...")
            time.sleep(3)
            # Checking for Error Editing Service Tag
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('VAMCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Edit Service Tag '%s' :: Error -> %s" % (serviceTag, errorMessage)
            except:
                # Validation: Checking whether edited Service Tag is reflected
                getCurrentServiceTag = self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualApplianceManagementObjects('currentServiceTag'))), action="GET_TEXT")
                if getCurrentServiceTag == serviceTag:
                    return self.browserObject, True, "Successfully Edited Service Tag"
                else:
                    return self.browserObject, False, "Failed to Edit Service Tag '%s' :: Error -> %s" % (serviceTag, "Validation Error")
        except Exception as e:
            return self.browserObject, False, "Exception generated while Editing Service Tag '%s' :: Error -> %s" % (serviceTag, str(e))