"""
Author: P Suman/Saikumar Kalyankrishnan
Created/Modified: Sep 28th 2015/Feb 20th 2017
Description: Functions/Operations related to Landing Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.LandingPage import LandingPage

class LandingPage(Navigation, Common, LandingPage):
    """
    Description:
        Class which includes all the operations related to Landing Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of this class.             
                
        Input:
            browserObject (String): Browser Handle 
                
        Output:
            None
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Active System Manager"
        utility.execLog("Landing Page")
            
    def validatePage(self):
        """
        Description:
            API to Load & Validate Landing Page
        """
        try:
            self.browserObject, status, result = self.validatePageTitle()
            if not status:
                return self.browserObject, status, result
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('userButton'))), action="CLICK")
            time.sleep(2)
            eleUsername = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('username'))), action="GET_TEXT")
            # Manipulating UserName - Stripping Whitespaces & Paranthesis
            eleUsername = eleUsername.strip()
            if eleUsername.startswith('(') and eleUsername.endswith(')'):
                eleUsername = eleUsername[1:-1]
            utility.execLog("Able to verify Landing Page :: Current User ->'%s'" % eleUsername)
            # To Collapse User Button
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('userButton'))), action="CLICK")
            return self.browserObject, True, "Able to verify Landing Page :: Current User ->'%s'" % eleUsername
        except Exception as e:
            utility.execLog("Unable to verify Landing Page :: Error -> %s" % (str(e) + format_exc()))
            return self.browserObject, False, "Unable to verify Landing Page :: Error -> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Landing Page Title              
        """
        if not title:
            title = self.pageTitle
        if title in self.browserObject.title:
            utility.execLog("Successfully validated Page Title '%s'" % title)
            return self.browserObject, True, "Successfully validated Page Title '%s'" % title
        else:
            utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (self.browserObject.title, title))
            return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (self.browserObject.title, title)
    
    def getLoginUser(self):
        """
        Description: 
            API to Fetch Current User
        """        
        try:            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('userButton'))), action="CLICK")
            time.sleep(2)
            eleUsername = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('username'))), action="GET_TEXT")
            # Manipulating UserName - Stripping Whitespaces & Paranthesis
            eleUsername = eleUsername.strip()
            if eleUsername.startswith('(') and eleUsername.endswith(')'):
                eleUsername = eleUsername[1:-1]
            utility.execLog("Able to fetch current User: '%s'" % eleUsername)
            # To Collapse User Button
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('userButton'))), action="CLICK")
            return self.browserObject, True, eleUsername
        except Exception as e:
            utility.execLog("Unable to fetch Current User :: Error -> %s" % (str(e) + format_exc()))
            return self.browserObject, False, "Unable to fetch Current User :: Error -> %s" % (str(e) + format_exc())
        
    def verifyLandingPageOptions(self):
        """
        Description: 
            Verify Options on the Landing Page 
        """
        try:
            return self.getMainOptions()
        except Exception as e:
            utility.execLog("Failed to verify Landing Page Options :: Error -> %s" % (str(e) + format_exc()))
            return self.browserObject, False, "Failed to verify Landing Page Options :: Error -> %s" % (str(e) + format_exc())
    
    def verifySettingsTab(self):
        """
        Description: 
            Verify whether User can Navigate to Settings Tab
        """
        try:
            return self.getSettingsOptions()
        except Exception as e:
            utility.execLog("Failed to fetch Settings Tab Options :: Error -> %s" % (str(e) + format_exc()))
            return self.browserObject, False, "Failed to fetch Settings Tab Options :: Error -> %s" % (str(e) + format_exc())

    def verifyHelpAboutOption(self, userRole="admin"):
        """
        Description: 
            Verify Help and About option is available for 'Read-Only' user on Landing Page
            Default: 'admin'
        """
        haOption = {}
        try:
            utility.execLog("Checking whether Help/About Menu is Collapsed or Expanded...")
            eleHelp = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('helpButton'))))
            eleHelpExpanded = eleHelp.get_attribute("aria-expanded")
            if "false" in eleHelpExpanded:
                utility.execLog("Help/About Menu is Collapsed...Clicking to Expand")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('helpButton'))), action="CLICK")
            else:
                utility.execLog("Help/About Menu is Expanded")
            time.sleep(2)
            opts = {"About": self.LandingPageObjects('about'), "Help": self.LandingPageObjects('help')}
            for key, value in opts.items():
                try:
                    utility.execLog("Checking whether '%s' option is available for '%s' user" % (key, userRole))
                    opt = self.handleEvent(EC.element_to_be_clickable((By.XPATH, value)))
                    if opt.is_displayed():
                        utility.execLog("Successfully verified '%s' option for '%s' user" % (key, userRole))
                        haOption[key] = "Enabled"
                    else:
                        utility.execLog("'%s' option is not available for '%s' user" % (key, userRole))
                        haOption[key] = "Disabled"
                except:
                    haOption[key] = "Disabled"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('helpButton'))), action="CLICK")
            return self.browserObject, True, haOption                            
        except Exception as e:
            utility.execLog("Failed to verify 'Help' and 'About' options on Landing Page for user '%s' :: Error -> %s" % (userRole, str(e) + format_exc()))
            return self.browserObject, False, "Failed to verify 'Help' and 'About' options on Landing Page for user '%s' :: Error -> %s" % (userRole, str(e) + format_exc())
    
    def verifyShowWelcomeScreenOption(self):
        """
        Description: 
            Verify Enable/Disable the "Show welcome screen on next Launch" in Getting Started.
        """
        options = []
        try:
            gettingStarted = self.browserObject.find_element_by_id(self.LandingPageObjects('showWelcomeScreen'))
            if gettingStarted.is_displayed():
                utility.execLog("Admin user can View 'Show welcome screen on next launch' in Getting Started.")
                options.append("Visible")
            status = gettingStarted.is_selected()
            if status:
                gettingStarted.click()
                if not gettingStarted.is_selected():
                    utility.execLog("Admin user can Disable 'Show welcome screen on next launch' in Getting Started.")
                    options.append("Disable")
                time.sleep(2)
                gettingStarted.click()
                if gettingStarted.is_selected():
                    utility.execLog("Admin user can Enable 'Show welcome screen on next launch' in Getting Started.")
                    options.append("Enable")
            else:
                gettingStarted.click()
                if gettingStarted.is_selected():
                    utility.execLog("Admin user can Enable 'Show welcome screen on next launch' in Getting Started.")
                    options.append("Enable")
                time.sleep(2)
                gettingStarted.click()
                if not gettingStarted.is_selected():
                    utility.execLog("Admin user can Disable 'Show welcome screen on next launch' in Getting Started.")
                    options.append("Disable")
            return self.browserObject, True, options
        except Exception as e:
            utility.execLog("Failed to fetch Getting Started Option on Menu :: Error -> %s" % (str(e) + format_exc()))
            return self.browserObject, False, "Failed to fetch Getting Started Option on Menu :: Error -> %s" % (str(e) + format_exc())
    
    def getServiceTag(self):
        """
        Description: 
            Fetches Service Tag from About Page

        # INCOMPLETE: Requires information if Service Tag to be visible in the About Dialog
        # Else Deprecated starting 8.4.0
        """
        serviceTag = ""
        try:
            utility.execLog("Checking whether Help/About Menu is Collapsed or Expanded...")
            eleHelp = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('helpButton'))))
            eleHelpExpanded = eleHelp.get_attribute("aria-expanded")
            if "false" in eleHelpExpanded:
                utility.execLog("Help/About Menu is Collapsed...Clicking to Expand")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('helpButton'))), action="CLICK")
            else:
                utility.execLog("Help/About Menu is Expanded")
            utility.execLog("Clicking on About...")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LandingPageObjects('about'))), action="CLICK")
            utility.execLog("Clicked on 'About' Link")
            try:
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.LandingPageObjects('aboutForm'))))
                utility.execLog("Verified 'About' Page")
            except Exception as e:
                utility.execLog("Failed to Load About Page :: Error -> %s" % (str(e) + format_exc()))
                return self.browserObject, False, "Failed to Load About Page :: Error -> %s" % (str(e) + format_exc())

            # Get Service Tag
            # INCOMPLETE: Waiting for Service Tags to be Visible in About
            # utility.execLog("Get Service Tag")
            # xpath = "//span[contains(@data-bind,'serviceTag')]"
            # serviceTag = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
            # return self.browserObject, True, serviceTag
            ###

        except Exception as e:
            utility.execLog("Failed to fetch Service Tag from About Page :: Error -> %s"%(str(e) + format_exc()))
            return self.browserObject, False, "Failed to fetch Service Tag from About Page :: Error -> %s"%(str(e) + format_exc())
        finally:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.LandingPageObjects('aboutClose'))), action="CLICK")
            except:
                pass

    def verifyGettingStartedOption(self):
        """
        Description:
            Verify whether User can Navigate to Getting Started Option
        """
        # INCOMPLETE: Requires information if 'Getting Started' Option to be visible in the Menu Bar
        # Else Deprecated starting 8.4.0
        # try:
        #     return self.getOptionStatus("Getting Started Menu")
        # except Exception as e:
        #     utility.execLog("Failed to fetch Getting Started Option on Menu :: Error -> %s" % (str(e) + format_exc()))
        #     return self.browserObject, False, "Failed to fetch Getting Started Option on Menu :: Error -> %s" % (
        #     str(e) + format_exc())

    def verifyLogoutOption(self, userRole):
        """
        Description:
            Verify Logout option is available for specific User Role on Landing Page
        """
        # INCOMPLETE: Requires information if 'Getting Started' Option to be visible in the Menu Bar
        # Else Deprecated starting 8.4.0
        # try:
        #     time.sleep(2)
        #     self.browserObject, status, result = self.getLogoutOption()
        #     if not status:
        #         return self.browserObject, status, result
        #     if "Enabled" in result:
        #         utility.execLog("Successfully verified 'Logout' option for '%s' user" % userRole)
        #         return self.browserObject, True, "Successfully verified 'Logout' option for '%s' user" % userRole
        #     else:
        #         utility.execLog("'Logout' option is not available for '%s' user" % userRole)
        #         return self.browserObject, False, "'Logout' option is not available for '%s' user" % userRole
        # except Exception as e:
        #     utility.execLog("Failed to verify 'Logout' option on Landing Page for user '%s' :: Error -> %s" % (
        #     userRole, str(e) + format_exc()))
        #     return self.browserObject, False, "Failed to verify 'Logout' option on Landing Page for user '%s' :: Error -> %s" % (
        #     userRole, str(e) + format_exc())