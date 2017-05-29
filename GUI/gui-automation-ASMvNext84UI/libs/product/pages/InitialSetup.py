"""
Author: P Suman/Saikumar Kalyankrishnan
Created/Modified: Jun 30th 2015/Feb 15th 2017
Description: Functions/Operations related to Initial Setup Page
"""

from CommonImports import *
from libs.product.pages.Navigation import Navigation
from libs.product.objects.Common import Common
from libs.product.objects.InitialSetup import InitialSetup

class InitialSetup(Navigation, Common, InitialSetup):
    """
    Description:
        Class which includes all the operations related to Initial Setup Wizard Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of this class.             
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Active System Manager"
        self.firstTimeSetup = False
        utility.execLog("Performing Initial Setup...")
        self.loadPage()
    
    def loadPage(self):
        """
        Description:
            API to Load Initial Setup Wizard
        """
        try:
            try:
                setupWizardVisible = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.InitialSetupObjects('checkSetupWizard'))), action="GET_TEXT")
            except:
                utility.execLog("Initial Setup Previously Completed. To change, go to Settings --> Virtual Appliance Management")
            else:
                if "Setup Wizard" in str(setupWizardVisible):
                    utility.execLog("Verified Initial Setup Wizard PopUp")
                    self.firstTimeSetup = True
                    time.sleep(5)
        except Exception as e:
            raise UIException.BaseException("Unable to load Initial Setup Wizard Page %s"%(str(e) + format_exc()))

    def validateTitleHeader(self, header):
        """
        Description:
            API to process and validate Page Title Header
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('titleHeader'))))
        except Exception as e:
            raise UIException.BaseException("Failed to validate %s Page in Initial Setup Wizard %s" % (header, e.message))
        else:
            getTitleHeader = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('titleHeader'))), action="GET_TEXT")
            utility.execLog(getTitleHeader)
            if header not in getTitleHeader:
                raise UIException.BaseException("Failed to validate %s Page in Initial Setup Wizard" % header)

    def processWelcomePage(self, header):
        """
        Description: 
            API to process and validate Welcome Page
        """
        self.validateTitleHeader(header)

    def processLicensePage(self, header, serviceTag=None):
        """
        Description: 
            API to process and validate License Page
        """
        self.validateTitleHeader(header)
        try:
            if serviceTag:
                utility.execLog("Setting Dell ASM Service Tag")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('serviceTag'))), action="SET_TEXT", setValue=serviceTag)
                utility.execLog("Completed:Setting Dell ASM Service Tag")
        except Exception as e:
            raise ("Failed to set Dell ASM Service Tag. Error --> %s" % (str(e) + format_exc()))

    def processTimezonePage(self, header, timeZoneId, primaryNTP, secondaryNTP=None, reset=0):
        """
        Description: 
            API to process and validate Time Zone and NTP Settings Page
        """
        self.validateTitleHeader(header)
        try:
            utility.execLog("Verifying Pre-Population of Time Zone and NTP Settings")
            getTimeZone = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('selectedTimeZone'))), action="GET_TEXT")
            getTimeZoneID = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('selectedTimeZone'))), action="GET_ATTRIBUTE_VALUE", attributeName='value')
            getPNTP = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('pNTPValue'))))

            if reset==0 and getTimeZone and getTimeZoneID and getPNTP:
                utility.execLog("Time Zone and NTP Settings Pre-Populated...Skipping Verification. To change, go to Settings --> Virtual Appliance Management")
                if secondaryNTP:
                    utility.execLog("Setting Secondary NTP to %s" % secondaryNTP)
                    try:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.InitialSetupObjects('secondaryNTP'))), action="SET_TEXT", setValue=secondaryNTP)
                    except Exception as e:
                        raise ("Failed to set Secondary NTP. Error --> %s" % (str(e) + format_exc()))
                return
            else:
                reset=1

            if reset==1:
                utility.execLog("Setting Time Zone to (UTC-06:00) Central Time (US & Canada) and Preferred NTP to %s" % primaryNTP)
                #Change Format of Timezone Value as per 8.4.0
                try:
                    timeZoneId = "string:"+timeZoneId
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.InitialSetupObjects('timeZone'))), action="SELECT", setValue=timeZoneId, selectBy="VALUE")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.InitialSetupObjects('primaryNTP'))), action="SET_TEXT", setValue=primaryNTP)
                except Exception as e:
                    raise ("Failed to set Time Zone & Preferred NTP. Error --> %s" % (str(e) + format_exc()))
                else:
                    utility.execLog("Completed Setting Time Zone to (UTC-06:00) Central Time (US & Canada) and Preferred NTP to %s" % primaryNTP)
        except Exception as e:
            raise UIException.BaseException("Unable to process Time Zone and NTP Settings Page in Initial Setup Wizard %s" % e.message)

    def processProxyPage(self, header):
        """
        Description: 
            API to process and validate Proxy Settings Page
        """
        self.validateTitleHeader(header)

    def processDHCPPage(self, header):
        """
        Description: 
            API to process and validate DHCP Settings Page
        """
        self.validateTitleHeader(header)
    
    def processSummaryPage(self, header):
        """
        Description: 
            API to process and validate Summary Page
        """
        try:
            self.handleEvent(EC.presence_of_element_located((By.ID, self.InitialSetupObjects('validateSummary'))))
        except Exception as e:
            raise UIException.BaseException("Failed to validate %s Page in Initial Setup Wizard %s"%(header, e.message))
            
    def processInitialSetup(self, timeZoneId, primaryNTP, secondaryNTP=None, serviceTag=None):
        """
        Description: 
            API to perform Initial Setup
        
        Input:
            timeZoneId (String): Time Zone Id to set (Required)
            primaryNTP (String): Primary NTP IP Address (Required)
            secondaryNTP (String): Secondary NTP IP Address
            serviceTag (String): Dell ASM Service Tag
                            
        Output:
            None         
        """
        try:
            if self.firstTimeSetup:
                #Welcome
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('pageWelcome'))))
                header = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('wizardWelcome'))), action="GET_TEXT")
                if "Welcome" not in header:
                    return self.browserObject, False, "Failed to validate 'Welcome' Page in Initial Setup Wizard....Actual:'%s' Expected:'%s'"%(header, "Welcome")
                utility.execLog("Initial Setup Wizard: Welcome Page Loaded")
                self.processWelcomePage(header)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('next'))), action="CLICK")
                utility.execLog("Moving to Licensing Page from Welcome Page")

                #Licensing
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('pageLicensing'))))
                header = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('wizardLicensing'))), action="GET_TEXT")
                if "Licensing" not in header:
                    return self.browserObject, False, "Failed to validate 'Licensing' Page in Initial Setup Wizard....Actual:'%s' Expected:'%s'" % (header, "Licensing")
                utility.execLog("Initial Setup Wizard: Licensing Page Loaded")
                self.processLicensePage(header, serviceTag)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('next'))), action="CLICK")
                utility.execLog("Moving to Time Zone and NTP Settings Page from Licensing Page")

                #Time Zone and NTP Settings
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('pageTZNTP'))))
                header = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('wizardTZNTP'))), action="GET_TEXT")
                if "Time Zone and NTP Settings" not in header:
                    return self.browserObject, False, "Failed to validate 'Time Zone and NTP Settings' Page in Initial Setup Wizard....Actual:'%s' Expected:'%s'" % (header, "Time Zone and NTP Settings")
                utility.execLog("Initial Setup Wizard: Time Zone and NTP Settings Page Loaded")
                self.processTimezonePage(header, timeZoneId, primaryNTP, secondaryNTP)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('next'))), action="CLICK")
                #Checking for Error Validating Time Zone and NTP Settings Page
                try:
                    errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('ntpError'))))
                    # INCOMPLETE: Processing Error Messages
                    if errorRedBox:
                        utility.execLog("Error in Validating Time Zone and NTP Settings Page...Retrying")
                        self.processTimezonePage(header, timeZoneId, primaryNTP, secondaryNTP, reset=1)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('next'))), action="CLICK")
                        #Checking for Error Validating Time Zone and NTP Settings Page - Attempt:2
                        try:
                            errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                            if errorRedBox:
                                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                                utility.execLog("Error in Validating Time Zone and NTP Settings Page...Canceling")
                                utility.execLog("Initial Setup Incomplete. To change, go to Settings --> Virtual Appliance Management")
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('cancel'))), action="CLICK")
                                return self.browserObject, False, "Initial Setup Failed..Error => %s" % errorMessage
                        except Exception as e:
                                utility.execLog("No Error Message Found while retrying...Successfully verified Time Zone and NTP Settings")
                except Exception as e:
                    utility.execLog("No Error Message Found...Successfully verified Time Zone and NTP Settings")
                utility.execLog("Moving to Proxy Settings from Time Zone and NTP Settings Page")

                #Proxy Settings
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('pageProxy'))))
                header = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('wizardProxy'))), action="GET_TEXT")
                if "Proxy Settings" not in header:
                    return self.browserObject, False, "Failed to validate 'Licensing' Page in Initial Setup Wizard....Actual:'%s' Expected:'%s'" % (header, "Proxy Settings")
                utility.execLog("Initial Setup Wizard: Proxy Settings Page Loaded")
                self.processProxyPage(header)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('next'))), action="CLICK")
                utility.execLog("Moving to DHCP Settings Page from Proxy Settings Page")

                #DHCP Settings
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('pageDHCP'))))
                header = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('wizardDHCP'))), action="GET_TEXT")
                if "DHCP Settings" not in header:
                    return self.browserObject, False, "Failed to validate 'DHCP Settings' Page in Initial Setup Wizard....Actual:'%s' Expected:'%s'" % (header, "DHCP Settings")
                utility.execLog("Initial Setup Wizard: DHCP Settings Page Loaded")
                self.processDHCPPage(header)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('next'))), action="CLICK")
                utility.execLog("Moving to Summary Page from DHCP Settings Page")

                #Summary
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.InitialSetupObjects('pageSummary'))))
                header = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('wizardSummary'))), action="GET_TEXT")
                if "Summary" not in header:
                    return self.browserObject, False, "Failed to validate 'Summary' Page in Initial Setup Wizard....Actual:'%s' Expected:'%s'" % (header, "Summary")
                utility.execLog("Initial Setup Wizard: Summary Page Loaded")
                self.processSummaryPage(header)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.InitialSetupObjects('finish'))), action="CLICK")
                utility.execLog("Finished Summary and Confirming Initial Setup")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.InitialSetupObjects('confirmButton'))), action="CLICK")
                utility.execLog("Initial Setup Completed")
            else:
                utility.execLog("Initial Setup Previously Completed. To change, go to Settings --> Virtual Appliance Management")
            return self.browserObject, True, "Initial Setup Completed. To change, go to Settings --> Virtual Appliance Management"
        except Exception as e:
            return self.browserObject, False, "Initial Setup Failed..Error => %s"%(str(e) + format_exc())