"""
Author: P Suman/Ankit Manglic/Saikumar Kalyankrishnan
Created/Modified: Nov 30th 2015/Feb 16th 2016/Feb 18th 2017
Description: Functions/Operations related to Navigation Menu
"""

import time
from CommonImports import *
from traceback import format_exc
from libs.product import utility
from libs.product import globalVars
from libs.product.pages import UIException
from libs.product.pages.Controller import Controller
from libs.thirdparty.selenium.common.exceptions import TimeoutException
from libs.thirdparty.selenium.webdriver.common.by import By
from libs.thirdparty.selenium.webdriver.support.ui import WebDriverWait
from libs.thirdparty.selenium.webdriver.support import expected_conditions as EC
from libs.product.objects.Navigation import Navigation

class Navigation(Controller, Navigation):
    """
    Description:
        Class which includes all Functions/Operations related to Navigation Menu

        Deprecated starting 8.4.0:
        'Username'
        'GettingStartedMenu'
        'Logout'
    """

    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Navigation class.
        """
        Controller.__init__(self, browserObject)
        utility.execLog("Initializing Navigation...")
        self.loopCount = 3
        self.menuMap = {"Dashboard": self.NavigationObjects('Dashboard'),
                        "Services": self.NavigationObjects('Services'),
                        "Templates": self.NavigationObjects('Templates'),
                        "Resources": self.NavigationObjects('Resources'),
                        "Settings": self.NavigationObjects('Settings'),
                        "Getting Started Main": self.NavigationObjects('GettingStartedMain'),
                        "Quick Actions": self.NavigationObjects('QuickActions'),
                        "Add-On Modules": self.NavigationObjects('AddOnModules'),
                        "Backup and Restore": self.NavigationObjects('BackupRestore'),
                        "Credentials Management": self.NavigationObjects('Credentials'),
                        "Getting Started": self.NavigationObjects('GettingStarted'),
                        "Jobs": self.NavigationObjects('Jobs'),
                        "Logs": self.NavigationObjects('Logs'),
                        "Networks": self.NavigationObjects('Networks'),
                        "Repositories": self.NavigationObjects('Repositories'),
                        "Initial Appliance Setup": self.NavigationObjects('InitialApplianceSetup'),
                        "Users": self.NavigationObjects('Users'),
                        "Virtual Appliance Management": self.NavigationObjects('VirtualApplianceManagement'),
                        "Virtual Identity Pools": self.NavigationObjects('VirtualIdentityPools'),
                        "Close": self.NavigationObjects('Close')
                        }
        self.pageElement = {"Landing Page":"lnkNetworks",
                            "Templates":"viewtemplatelist",
                            "Dashboard":"recentActivityViewAll",
                            "Resources":"tabSummary",
                            "Services":"viewservicelist"
                            }
        
    def selectOption(self, menuOption):
        """
        Description:
            API to Select Menu Option
        """
        try:
            # Settings Menu
            if menuOption in ("Getting Started", "Logs", "Jobs", "Initial Appliance Setup", "Users", 
                              "Networks", "Credentials Management", "Virtual Appliance Management", "Backup and Restore",
                              "Add-On Modules", "Repositories", "Virtual Identity Pools"):
                menuID = self.menuMap[menuOption]
                eleSettings = self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('ExpandSettings'))))
                # Checking if Settings Menu is Expanded or Collapsed
                if "collapsed" in eleSettings.get_attribute("class"):
                    utility.execLog("Settings Menu is Collapsed...Clicking to Expand")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('ExpandSettings'))), action="CLICK")
                else:
                    utility.execLog("Settings Menu is Expanded")
                # Selecting Settings Option once Settings Menu is Expanded
                eleSettingsMenu = self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('SettingsMenu'))))
                if "true" in eleSettingsMenu.get_attribute("aria-expanded"):
                    utility.execLog("Selecting Option '%s' under Settings..." % menuOption)
                    time.sleep(2)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, menuID)), action="CLICK")
                    utility.execLog("Selected Option: '%s'" % menuOption)
            # Main Menu
            elif menuOption in ("Dashboard", "Services", "Templates", "Resources", "Settings"):
                utility.execLog("Selecting Menu Option '%s'..." % menuOption)
                menuID = self.menuMap[menuOption]
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, menuID)), action="CLICK")
                utility.execLog("Selected Menu Option '%s'" % menuOption)
            # Invalid Menu
            else:
                utility.execLog("Invalid Menu Option '%s'" % menuOption)
                return self.browserObject, False, "Invalid Menu Option '%s'" % menuOption
            return self.browserObject, True, "Able to Load '%s' Page" % menuOption
        except Exception as e:
            return self.browserObject, False, "Unable to Load '%s' Page :: Error -> '%s'" % (menuOption, str(e))
    
    def getMainOptions(self):
        """
        Description:
            API to Get Main Menu Options Accessibility
        """
        optionList = {}
        mainMenuOptions = ("Dashboard", "Services", "Templates", "Resources", "Settings", "Quick Actions", "Getting Started Main")
        try:
            for key in mainMenuOptions:            
                utility.execLog("Verifying Main Menu: %s" % key)
                try:
                    menuXPATH = self.menuMap[key]
                    eleMenu = self.handleEvent(EC.visibility_of_element_located((By.XPATH, menuXPATH)))
                    if eleMenu:
                        optionList[key] = "Enabled"
                    else:
                        optionList[key] = "Disabled"
                except:
                    optionList[key] = "Disabled"
                utility.execLog("Verified Main Menu: %s" % key)
            utility.execLog("Navigation Options and Status %s" % str(optionList))
            return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Unable to Load 'Menu' :: Error -> '%s'" % (str(e) + format_exc())
    
    def getSettingsOptions(self):
        """
        Description:
            API to Get Settings Tab Accessibility
        """
        optionList = {}
        settingTabOptions = ("Getting Started", "Logs", "Jobs", "Users", "Networks",
                             "Credentials Management", "Virtual Appliance Management", "Backup and Restore",
                             "Add-On Modules", "Repositories", "Virtual Identity Pools")
        try:
            eleSettings = self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('ExpandSettings'))))
            # Checking if Settings Menu is Expanded or Collapsed
            if "collapsed" in eleSettings.get_attribute("class"):
                utility.execLog("Settings Menu is Collapsed...Clicking to Expand")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('ExpandSettings'))), action="CLICK")
            else:
                utility.execLog("Settings Menu is Expanded")
            # Selecting Settings Option once Settings Menu is Expanded
            eleSettingsMenu = self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('SettingsMenu'))))
            if "true" in eleSettingsMenu.get_attribute("aria-expanded"):
                for option in settingTabOptions:
                    utility.execLog("Verifying Settings Menu: %s" % option)
                    try:
                        menuXPATH = self.menuMap[option]
                        eleMenu = self.handleEvent(EC.visibility_of_element_located((By.XPATH, menuXPATH)))
                        if eleMenu:
                            optionList[option] = "Enabled"
                        else:
                            optionList[option] = "Disabled"
                    except:
                        optionList[option] = "Disabled"
                    utility.execLog("Verified Settings Menu: %s" % option)
                utility.execLog("Settings Tab Options and Status %s" % str(optionList))
                # Collapsing Settings Menu
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NavigationObjects('ExpandSettings'))), action="CLICK")
                return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Unable to Load 'Menu' :: Error -> '%s'" % (str(e) + format_exc())

    def changeMenuToPinView(self):
        try:
            utility.execLog("Checking Menu View: Pin/Collapsed...")
            pinView = self.handleEvent(EC.visibility_of_element_located((By.ID, self.NavigationObjects('Unpin'))))
            if pinView:
                utility.execLog("Menu: Pin View Active")
                return self.browserObject, True, "Menu Already in Pin View"
            else:
                utility.execLog("Menu: Collapsed View Active")
                utility.execLog("Changing to Pin View...")
                utility.execLog("Clicking Menu Bar...")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('ExpandMenu'))), action="CLICK")
                menuExpanded = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.NavigationObjects('MenuBar'))))
                if menuExpanded:
                    utility.execLog("Menu Bar Expanded")
                    utility.execLog("Clicking on Pin...")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('Pin'))), action="CLICK")
                # Validating if Pin View is Active
                pinView = self.handleEvent(EC.visibility_of_element_located((By.ID, self.NavigationObjects('Unpin'))))
                if pinView:
                    utility.execLog("Menu: Pin View Active")
                    return self.browserObject, True, "Menu Changed to Pin View"
                else:
                    return self.browserObject, False, "Menu Could not be changed to Pin View"
        except Exception as e:
            return self.browserObject, False, "Exception generated while changing Menu to Pin View :: Error -> '%s'"%(str(e))

    def changeMenuToNormalView(self):
        try:
            utility.execLog("Checking Menu View: Pin/Collapsed...")
            pinView = self.handleEvent(EC.visibility_of_element_located((By.ID, self.NavigationObjects('Unpin'))))
            if pinView:
                utility.execLog("Menu: Pin View Active")
                utility.execLog("Changing to Normal/Collapsed View...")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('Unpin'))), action="CLICK")
            else:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('ExpandMenu'))), action="CLICK")
                menuExpanded = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.NavigationObjects('MenuBar'))))
                if menuExpanded:
                    utility.execLog("Menu: Normal/Collapsed View Active")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('ExpandMenu'))), action="CLICK")
                    return self.browserObject, True, "Menu Already in Normal/Collapsed View"
            # Validating if Normal/Collapsed View is Active
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('ExpandMenu'))), action="CLICK")
            menuExpanded = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.NavigationObjects('MenuBar'))))
            if menuExpanded:
                utility.execLog("Menu: Collapsed View Active")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('ExpandMenu'))), action="CLICK")
                return self.browserObject, True, "Menu Changed To Normal/Collapsed View"
            else:
                return self.browserObject, False, "Menu Could not be changed to Normal/Collapsed View"
        except Exception as e:
            return self.browserObject, False, "Exception generated while changing menu to Normal/Collapsed View :: Error -> '%s'"%(str(e))

    def verifySubMenu(self, menuName):
        try:
            self.browserObject, status, result = self.changeMenuToNormalView()
            if status:
                utility.execLog("Clicking on Menu...")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NavigationObjects('ExpandMenu'))), action="CLICK")
                utility.execLog("Verifying Sub-Menu: %s" % str(menuName))
                if self.browserObject.find_element_by_xpath("//*[contains(text(),'%s')]" % str(menuName)).is_displayed():
                    return self.browserObject, True, "Sub-Menu '%s' is Enabled" % str(menuName)
                else:
                    return self.browserObject, False, "Sub-Menu '%s' is not Enabled" % str(menuName)
            else:
                return self.browserObject, False, "Error while Verifying Sub-Menu '%s'" % str(menuName)
        except Exception as e:
            return self.browserObject, False, "Exception generated while Verifying Sub-Menu %s :: Error -> '%s'" % (str(menuName), str(e))

    def getOptionStatus(self, optionName):
        """
        Description:
            API to Get Option Accessibility

        Deprecated starting 8.4.0:
        'Username'
        'GettingStartedMenu'
        'Logout'

        Incomplete
        """
        option = ""
        try:
            utility.execLog("Checking whether Menu is expanded")
            mainMenu = self.browserObject.find_element_by_id("navHover")
            if not mainMenu.find_element_by_id("spanBackdrop").is_displayed():
                utility.execLog("Menu is not expanded, so Clicking on Menu")
                mainMenu.click()
                time.sleep(5)
                utility.execLog("Clicked on Menu")
            utility.execLog("Verifying '%s' Option" % optionName)
            try:
                elementIdentity = self.menuMap[optionName]
                if "//" not in elementIdentity:
                    eleOption = self.browserObject.find_element_by_id(elementIdentity)
                else:
                    eleOption = self.browserObject.find_element_by_id("navHover").find_element_by_xpath(
                        elementIdentity).click()
                if eleOption.is_displayed():
                    option = "Enabled"
                else:
                    option = "Disabled"
            except:
                option = "Disabled"
            utility.execLog("'%s' Option Status '%s'" % (optionName, option))
            return self.browserObject, True, option
        except Exception as e:
            return self.browserObject, False, "Unable to get Option '%s' Status :: Error -> '%s'" % (
            optionName, str(e) + format_exc())

    def getLogoutOption(self):
        """
        Description:
            API to Get Logout Option Accessibility

        Deprecated starting 8.4.0

        Incomplete
        """
        # option = ""
        # try:
        #     utility.execLog("Checking whether Menu is expanded")
        #     mainMenu = self.browserObject.find_element_by_id("navHover")
        #     if not mainMenu.find_element_by_id("spanBackdrop").is_displayed():
        #         utility.execLog("Menu is not expanded, so Clicking on Menu")
        #         mainMenu.click()
        #         time.sleep(5)
        #         utility.execLog("Clicked on Menu")
        #     utility.execLog("Verifying 'Logout' Option")
        #     try:
        #         elementId = self.menuMap["Logout"]
        #         eleOption = self.browserObject.find_element_by_id(elementId)
        #         if eleOption.is_displayed():
        #             option = "Enabled"
        #         else:
        #             option = "Disabled"
        #     except:
        #         option = "Disabled"
        #     utility.execLog("'Logout' Option Status '%s'"%option)
        #     return self.browserObject, True, option
        # except Exception as e:
        #     return self.browserObject, False, "Unable to load 'Menu' :: Error -> '%s'"%(str(e) + format_exc())
    