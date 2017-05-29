"""
Author: Raj Patel/Mirlan Kaiyrbaev/Saikumar Kalyankrishnan
Created/Modified: Mar 1st 2016/Mar 6th 2017/Mar 8th 2017
Description: Functions/Operations related to Getting Started Page
"""

from CommonImports import *
from libs.product.pages import *
from libs.product import *
from libs.product import globalVars
from libs.product import BaseClass
from libs.product import utility
from libs.product.pages.Navigation import Navigation
from libs.thirdparty.selenium.webdriver.common.action_chains import ActionChains
from libs.product.objects.GettingStarted import GettingStarted
from libs.product.objects.Common import Common
import time
import datetime


class GettingStarted(Navigation, Common, GettingStarted):
    """
    Description:
        Class which includes all the operations related to Getting Started Page
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
        self.pageTitle = "Welcome to Active System Manager"
        utility.execLog("Getting Started Page")
    
    def loadPage(self):
        """
        Description:
            API to load Getting Started Page
        """
        try:
            utility.execLog("Loading Getting Started Page")
            self.browserObject, status, result = self.selectOption("Getting Started")
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Getting Started Page %s" % e.message

    # NO USAGE
    # def validatePageTitle(self, title=None):
        # """
        # Description:
        #     API to validate Landing Page Title
        # """
        # if title is None:
        #     title = self.pageTitle
        # getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('gettingstarted'))), action="GET_TEXT")
        # if title not in getCurrentTitle:
        #     utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
        #     return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
        # else:
        #     utility.execLog("Successfully validated Page Title: '%s'" % title)
        #     return self.browserObject, True, "Successfully validated Page Title: '%s'" % title

    # NO USAGE
    # def verifyGettingStartedOption(self):
    #     """
    #     Description:
    #         Verify whether User can navigate to Getting Started Option
    #     """
    #     try:
    #         navObj = Navigation(self.browserObject)
    #         return navObj.getOptionStatus("Getting Started Menu")
    #     except Exception as e:
    #         utility.execLog("Failed to fetch Getting Started Option on Menu :: Error -> %s" % e.message)
    #         return self.browserObject, False, "Failed to fetch Getting Started Option on Menu :: Error -> %s" % e.message
    
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Getting Started Page 
        """
        self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.GettingStartedObjects('initHWconf'))))
        optionList = {}
        try:
            options = [self.GettingStartedObjects('lnkNetworks'), self.GettingStartedObjects('lnkDiscoverResources'), self.GettingStartedObjects('lnkConfigureInitialSetup'),
                       self.GettingStartedObjects('lnkConfigureResources'), self.GettingStartedObjects('lnkCreateTemplate')]
            for opt in options:
                eleUser = self.handleEvent(EC.presence_of_element_located((By.ID, opt)))
                result = eleUser.get_attribute('disabled')
                optName = str(eleUser.text)
                if result:
                    optionList[optName] = "Disabled"
                else:
                    optionList[optName] = "Enabled"
            return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Unable to read Options on Getting Started Page :: Error -> %s" % str(e)
        
    def restoreNowFromGettingstarted(self):
        """
        Description:
            API to Restore Now 
        """
        try:
            # Reading Values from config.ini into globalVars.py
            globalVars.configInfo = utility.readConfig(globalVars.configFile)
            globalVars.backupPath = globalVars.configInfo['Appliance']['backuppath']
            # globalVars.backupDirusername = globalVars.configInfo['Appliance']['backupdirusername']
            # globalVars.backupDirpassword = globalVars.configInfo['Appliance']['backupdirpassword']
            globalVars.encpassword = globalVars.configInfo['Appliance']['encpassword']

            # Clicking on 'Restore Now'
            utility.execLog("Clicking on 'Restore Now'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('restoreNowLink'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Restore Now Form"
            # Verifying Dialog PopUp Title Header
            if "Restore Now" not in currentTitle:
                    utility.execLog("Failed to Verify Restore Now Page :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "Restore Now")
                    return self.browserObject, False, "Failed to Verify Restore Now Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Restore Now")
            utility.execLog("Restore Now Page Loaded and Verified Successfully")
            # Setting Directory Path
            utility.execLog("Setting Directory Path: '%s'" % globalVars.backupPath)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('directoryPath'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('directoryPath'))), action="SET_TEXT", setValue=globalVars.backupPath)
            # Setting Directory Username
            utility.execLog("Setting Directory Username: '%s'" % globalVars.backupDirusername)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('directoryUserName'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('directoryUserName'))), action="SET_TEXT", setValue=globalVars.backupDirusername)
            # Setting Directory Password
            utility.execLog("Setting Directory Password: '%s'" % globalVars.backupDirpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('backupLocationPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('backupLocationPassword'))), action="SET_TEXT", setValue=globalVars.backupDirpassword)
            # Setting Encryption Password
            utility.execLog("Setting Encryption Password: '%s'" % globalVars.encpassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('encryptionPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('encryptionPassword'))), action="SET_TEXT", setValue=globalVars.encpassword)
            # Testing Connection
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects('btnTestBackupNowConnection'))), action="CLICK")
            time.sleep(1)
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), action="GET_TEXT", wait_time=10)
                if errorRedBox:
                    utility.execLog("Failed to Restore From Backup :: Error -> '{}'".format(errorRedBox))
                    return self.browserObject, False, "Failed to Restore From Backup :: Error -> %s" % errorRedBox
            except:
                utility.execLog("Testing connection went successfully")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.GettingStartedObjects('closeBtn'))), action="CLICK")
            # Clicking 'Restore Now'
            utility.execLog("Clicking on 'Restore Now'...")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.GettingStartedObjects('submitBackupNow'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.GettingStartedObjects('popUpHeader'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Restore Now Confirmation"
            # Verifying Dialog PopUp Title Header
            if "Warning" not in currentTitle:
                utility.execLog("Failed to Verify Restore Now Confirmation Page :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "Warning")
                return self.browserObject, False, "Failed to Verify Restore Now Confirmation Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Warning")
            utility.execLog("Restore Now Confirmation Page Loaded and Verified Successfully")
            # Confirm - 'Yes'
            utility.execLog("Clicking on 'Yes' to Restore Now'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            # Checking for Error while Restoring from Backup
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), action="GET_TEXT", wait_time=10)
                # INCOMPLETE: Processing Error Messages
                if errorRedBox:
                    utility.execLog("Failed to Restore From Backup :: Error -> '{}'".format(errorRedBox))
                    return self.browserObject, False, "Failed to Restore From Backup :: Error -> %s" % errorRedBox
            except:
                utility.execLog("Restore In-Progress...")
            start_time = datetime.datetime.now()
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('LoginLogo'))), wait_time=3600, freq=60)
            end_time = datetime.datetime.now()
            diff = end_time - start_time
            utility.execLog("Time to Restore Appliance: {} min".format(diff.seconds/60))

            if self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('LoginUsername')))):
                utility.execLog("Successfully Restored Appliance")
                return self.browserObject, True, "Successfully Restored Appliance"
            else:
                return self.browserObject, False, "Failed to Load Login Page after ASM Appliance Restore from Backup"
        except Exception as e:
            return self.browserObject, False, "Exception generated while Restoring Appliance From Backup :: Error -> %s" % str(e)

    # CHASSIS CONFIGURATION
    def navigateToChassisConfigurationTab(self):
        """
        Description: Navigate the application to Chassis configuration page
        """
        
        try:
            utility.execLog("Clicking Configure Resource button")
            counter=0
              
            while(counter<10):
                utility.execLog("Test to check Configure Resource is enable or not, count={}".format(counter))
                try:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects("lnkConfigureResources")))):
                        enabledCheck = self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects("lnkConfigureResources"))), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                        utility.execLog("enabledCheck={}".format(enabledCheck))
                        if not self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects("lnkConfigureResources"))), action="GET_ATTRIBUTE_VALUE", attributeName="disabled"):
                            utility.execLog("Configure Resource button is enabled")
                            break
                        else:
                            utility.execLog("Configure Resource button is disabled")
                            counter += 1
                            time.sleep(120)
                except Exception as e:
                    utility.execLog("Configure Resource button is yet to exist")
                    counter += 1
                    time.sleep(300)
            
            utility.execLog("Click on Configure Resource button")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.GettingStartedObjects("lnkConfigureResources"))), action="CLICK")
            time.sleep(2)
            utility.execLog("Clicking next button on Configure Resource welcome page.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.GettingStartedObjects('btnNext'))), action="CLICK")
            time.sleep(4)
            utility.execLog("Clicking next button on Discovered Resources page.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.GettingStartedObjects('btnNext'))),
                             action="CLICK")
            time.sleep(1)
            utility.execLog("Clicking next button on Default Firmware Repository page.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.GettingStartedObjects('btnNext'))),
                             action="CLICK")
            time.sleep(2)
            counter = 0
            while(counter < 10):
                utility.execLog("counter={}".format(counter))
                if self.browserObject.find_element_by_id("btnWizard-Next").is_enabled():
                    break
                else:
                    counter = counter+1
                    time.sleep(2)
            utility.execLog("Clicking next button on Firmware Compliance page.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.GettingStartedObjects('btnNext'))),
                             action="CLICK")
            time.sleep(1)
        except Exception as e:
            utility.execLog("Exception generated while navigating to Chassis configuration page :: Error -> %s"%str(e))
            raise e

    def addCmcUser(self,chassisIP,userNames, password,enableUsers, userGroups, confirmPass= None):
        """
        Description: Add CMC User at the the given Chassis IP
        """
        try:
            utility.execLog("Calling GettingStarted page navigation to chassis tab function")
            self.navigateToChassisConfigurationTab()
            #Check particular chessis to Add User
            self.selectChassisToConfigure(chassisIP)
            utility.execLog("Navigated to Chassis configuration page.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[1]")), action="CLICK")
            time.sleep(5)
            utility.execLog("Expanded Users tab")
            
            
            x= 0
            for user in userNames:
                
                utility.execLog("Clicking add new CMC user link")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "new_user_link")), action="CLICK")
                time.sleep(5)
                utility.execLog("Entering details on user creation page")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_username")), action="SET_TEXT", setValue=userNames[x])
                utility.execLog("Entered %s username"%str(userNames[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_password")), action="SET_TEXT", setValue=password)
                utility.execLog("Entered %s password"%str(password))
                if confirmPass == None:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=password)
                    utility.execLog("Entered %s user_confirm_password"%str(password))
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=confirmPass)
                    utility.execLog("Entered %s user_confirm_password"%str(confirmPass))
                utility.execLog("Selecting user group %s"%str(userGroups[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "role")), action="SELECT", setValue=userGroups[x])
                time.sleep(1)
                if enableUsers[x] == False:
                    utility.execLog("Unchecking the check-box")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_enabled")), action="CLICK")
                    time.sleep(1)
                else:
                    utility.execLog("Enable user check -box is already checked.")
                utility.execLog("Save the user by clicking save button.")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_user_form")), action="CLICK")
                time.sleep(5)
                
                utility.execLog("Verify if All Fields are correct")
                try:
                    self.browserObject.find_element_by_xpath("//*[@id='edit_user_form']//*[@class='error']")
                    utility.execLog("Fields verification failed.... ")
                    return self.browserObject, False, "Unable to create CMC user :: Error -> Fields verification failed"
                except:
                    utility.execLog("Fields verification Passed ")
                
                utility.execLog("Verify if user is added.")
                try:
                    #self.browserObject.find_element_by_xpath("//*[contains(text(),'HclTestCmcUser')]")
                    self.browserObject.find_element_by_xpath("//*[contains(text(),"+userNames[x]+")]")
                    utility.execLog("User %s successfully added."%str(userNames[x]))
                except:
                    utility.execLog("User %s is not added."%str(userNames[x]))
                x=x+1   
                    
            pageList = ["Chassis Configuration","Unique Chassis Settings","Unique Server Settings", "Unique IO Settings", "IO Configuration"]
            for elem in pageList:
                utility.execLog("Click next on %s page"%str(elem))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
                time.sleep(1)
                
            utility.execLog("Click Finish on Summary page.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
            time.sleep(2)
            
            utility.execLog("Click yes on confirmation modal box.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            time.sleep(2)
            return self.browserObject, True, "Successfully added CMC users:- %s"%str(userNames)
        except Exception as e:
            return self.browserObject, False, "Unable to create CMC users :: Error -> %s"%str(e)

    def addiDracUser(self, chassisIP, userNames, password, roles, lanRoles, enableUser, confirmPass= None):
        """
        Description: Add iDrac User at the the given Chassis IP
        """
        try:
            self.navigateToChassisConfigurationTab()
            #Check particular chessis to Add User
            self.selectChassisToConfigure(chassisIP)
            utility.execLog("Navigated to Chassis configuration page.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[1]")), action="CLICK")
            time.sleep(1)
            utility.execLog("Expanded Users tab")
            x= 0
            for item in userNames:
                utility.execLog("Clicking add new iDrac user link")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "new_idracuser_link")), action="CLICK")
                time.sleep(2)
                utility.execLog("Entering details on user creation page")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_username")), action="SET_TEXT", setValue=userNames[x])
                utility.execLog("Entered %s username"%str(userNames[x]))
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_password")), action="SET_TEXT", setValue=password)
                utility.execLog("Entered %s password"%str(password))
                time.sleep(2)
                if confirmPass == None:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=password)
                    utility.execLog("Entered %s user_confirm_password"%str(password))
                    time.sleep(2)
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=confirmPass)
                    utility.execLog("Entered %s user_confirm_password"%str(confirmPass))
                    time.sleep(2)
                utility.execLog("Selecting role %s"%str(roles[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "lan")), action="SELECT", setValue=roles[x])
                time.sleep(1)
                utility.execLog("Selecting Lan role %s"%str(lanRoles[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "idracrole")), action="SELECT", setValue=lanRoles[x])
                time.sleep(1)
                if enableUser[x] == False:
                    utility.execLog("Unchecking the check-box")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_enabled")), action="CLICK")
                    time.sleep(2)
                else:
                    utility.execLog("Enable user check -box is already checked.")
                utility.execLog("Save the user by clicking save button.")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_user_form")), action="CLICK")
                time.sleep(2)
                utility.execLog("Verify if All Fields are correct")
                try:
                    self.browserObject.find_element_by_xpath("//*[@id='edit_user_form']//*[@class='error']")
                    utility.execLog("Fields verification failed.... ")
                    return self.browserObject, False, "Unable to create iDrac user :: Error -> Fields verification failed"
                except:
                    utility.execLog("Fields verification Passed ")
                    
                utility.execLog("Verify if user is added.")
                try:
                    self.browserObject.find_element_by_xpath("//*[contains(text(),'%s')]"%str(userNames[x]))
                    utility.execLog("User %s successfully added."%str(userNames[x]))
                except:
                    utility.execLog("User %s is not added."%str(userNames[x]))
                x=x+1
            pageList = ["Chassis Configuration","Unique Chassis Settings","Unique Server Settings", "Unique IO Settings", "IO Configuration"]
            for elem in pageList:
                utility.execLog("Click next on %s page"%str(elem))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
                time.sleep(1)
                
            utility.execLog("Click Finish on Summary page.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
            time.sleep(2)
            
            utility.execLog("Click yes on confirmation modal box.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            time.sleep(2)
            return self.browserObject, True, "Successfully added iDrac users %s"%str(userNames)
        except Exception as e:
            return self.browserObject, False, "Unable to create iDrac user :: Error -> %s"%str(e)

    def verifyCmcUser(self, serverIP, userName, password):
        """
        Description: Verify the new cmc user on the given chassis IP 
        """ 
        try:
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s/cgi-bin/webcgi/login"%str(serverIP))
            time.sleep(5)
            self.cmcUsercount = 1
            while self.cmcUsercount < 5:
                newDriver.find_element_by_id("user_id").send_keys(userName)
                newDriver.find_element_by_id("password").send_keys(password)
                newDriver.find_element_by_id("login_submit").click()
                utility.execLog("Logged into Chassis")
                return self.browserObject, True,"CMC user %s is successfully created"%str(userName)
                time.sleep(5)
                try:
                    self.browserObject.switch_to_frame("globalnav")
                    self.browserObject.find_element_by_id("cmcImage")
                    return self.browserObject, True,"CMC user %s is successfully created"%str(userName)
                except Exception as ex:
                    utility.execLog("Exception generated while logging into created cmc user %s :: Error -> %s"%(str(userName), str(ex)))
                    time.sleep(120)
                    self.cmcUsercount = self.cmcUsercount +1
                    continue
        except Exception as e:
            return self.browserObject, False, "Exception generated while verify new created CMC Error -> %s"%str(e)

    def verifyiDracUser(self, serverIP, enableUser,userNames,lanRoles,roles):
        """
        Description: Verify the new cmc user on the given chassis IP 
        """ 
        #lanRol==iDRA,Admin==Administrator
        #roles==LAN,"No Access"==None
        #enableUser:- True==Enabled and False==Disabled [4 if x==1 else x for x in a]
        State=["Enabled" if x==True else "Disabled" for x in enableUser]
        iDRA=["Administrator" if x=="Admin" else x for x in lanRoles]
        LAN=["None" if x=="No Access" else x for x in roles]
        utility.execLog("State={},iDrac={},LAN={}".format(State,iDRA,LAN))
        
        
        try:
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s/cgi-bin/webcgi/login"%str(serverIP))
            time.sleep(5)
            newDriver.find_element_by_id("user_id").send_keys("root")
            newDriver.find_element_by_id("password").send_keys("calvin")
            newDriver.find_element_by_id("login_submit").click()
            utility.execLog("Logged into Chassis with root")
            time.sleep(15)
            utility.execLog("Switching to treelist frame")
            newDriver.switch_to.default_content()
            time.sleep(1)
            newDriver.switch_to_frame(newDriver.find_element_by_name("treelist"))
            time.sleep(1)
            utility.execLog("Clicking the server in tree list of Chassis %s"%str(serverIP))
            newDriver.find_element_by_id("server_4").click()
            utility.execLog("Waiting for contents to load. ie. iDrac Launch button")
            time.sleep(5)
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            time.sleep(1)
            utility.execLog("Switching to 'da' frame to click 'Launch iDrac' button.")
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(1)
            newDriver.find_element_by_xpath("(//*[contains(text(),'Launch iDRAC GUI')])[1]").click()
            utility.execLog("'Launch iDrac GUI' button clicked.")
            utility.execLog("Waiting for new iDrac window to load.")
            time.sleep(10)
            utility.execLog("Fetching new window handle and switching to it.")
            new_window = newDriver.window_handles[1]
            newDriver.switch_to_window(new_window)
            time.sleep(1)
            utility.execLog("Switching to 'treelist_id' frame to fetch user info.")
            newDriver.switch_to_frame(newDriver.find_element_by_id("treelist_id"))
            time.sleep(1)
            newDriver.find_element_by_id("exp_iDRAC").click()
            utility.execLog("iDrac settings menu expanded.")
            time.sleep(1)
            newDriver.find_element_by_id("a_User Authentication ").click()
            utility.execLog("User list loaded.")
            time.sleep(5)
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            utility.execLog("Switching to 'da' frame to verify user.")
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(1)
            self.userFlag = True
            count=0
            while(count<len(userNames)):
                try:
                    xpathToCheck=".//*[@id='userList']/tbody/tr/td[normalize-space()='"+State[count]+"'"+"]/following-sibling:: \
                        td[normalize-space()='"+userNames[count]+"'"+"]/following-sibling::td[normalize-space()='"+iDRA[count]+"'"+"]/following-sibling:: \
                        td[normalize-space()='"+LAN[count]+"']"
                        
                    element=newDriver.find_element_by_xpath(".//*[@id='userList']/tbody/tr/td[normalize-space()='"+State[count]+"'"+"]/following-sibling:: \
                        td[normalize-space()='"+userNames[count]+"'"+"]/following-sibling::td[normalize-space()='"+iDRA[count]+"'"+"]/following-sibling:: \
                        td[normalize-space()='"+LAN[count]+"']")
                    utility.execLog("User {} is present with all privillages xpath={}".format(userNames[count],xpathToCheck))
                except Exception as e1:
                    utility.execLog("User %s is not available at iDrac. xpath=%s :: Error-> %s"%(str(userNames[count]),xpathToCheck, str(e1))) 
                    self.userFlag = False
                count=count+1   
            if self.userFlag == True:
                return self.browserObject, True, "All Users %s are available at iDrac, successfully verified."%str(userNames)
            else:
                return self.browserObject, False, "All Users are not available at iDrac"
        except Exception as e:
            return self.browserObject, False, "Exception generated while verify new created CMC Error -> %s"%str(e)

    def configureChassis(self, chassisIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName,registeriDracDNS=True):
        """
        """
        try:
            self.navigateToChassisConfigurationTab()
            utility.execLog("Navigated to Chassis configuration page.")
            utility.execLog("Selecting the %s chassis"%str(chassisIP))
            chassisIpCheckboxes = self.browserObject.find_elements_by_xpath("//table[@id='discoveredDevicesGrid']/tbody/tr/td[1]/input")
#             utility.execLog("Total number of discovered resources/chassis are %s"%str(chassisIpCheckboxes.length()))
            for item in chassisIpCheckboxes:
                utility.execLog("Unselecting checkbox for %s"%str(item))
                item.click()
                time.sleep(1)
            utility.execLog("Unchecked all chassis ip's.")
            utility.execLog("selecting %s ip chassis"%str(chassisIP))
            self.browserObject.find_element_by_xpath("//table[@id='discoveredDevicesGrid']/tbody/tr/td[2]//span[contains(text(),'%s')]/parent::td/parent::tr/td[1]/input"%str(chassisIP)).click()
            if registeriDracDNS:
                self.browserObject.find_element_by_xpath("//*[@id='chassisConfigPanels']//h2/*[@id='Span5']").click()
                time.sleep(1)
                self.browserObject.find_element_by_xpath("//*[@id='registeriDracDNS']").click()
                time.sleep(1)
            
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
            utility.execLog("Select unique chassis settings checkbox")
            self.browserObject.find_element_by_id("uniqueChassisConfigSelect").click()
            time.sleep(2)
            utility.execLog("Enter Chassis Name %s"%str(chassisName))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.name']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.name']").send_keys(chassisName)
            utility.execLog("Enter Chassis DNS Name %s"%str(chassisDnsName))
            self.browserObject.find_element_by_id("cmcdnsname").clear()
            self.browserObject.find_element_by_id("cmcdnsname").send_keys(chassisDnsName)
            utility.execLog("Enter Chassis Location details")
            utility.execLog("Enter location details Data center value %s"%str(locDetailsDc))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.dataCenter']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.dataCenter']").send_keys(locDetailsDc)
            utility.execLog("Enter location details Asile value %s"%str(locDetailsAsile))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.aisle']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.aisle']").send_keys(locDetailsAsile)
            utility.execLog("Enter location details Rack value %s"%str(locDetailsRack))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rack']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rack']").send_keys(locDetailsRack)
            utility.execLog("Enter location details Rack Slot value %s"%str(locDetailsRs))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rackSlot']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rackSlot']").send_keys(locDetailsRs)
            utility.execLog("click next on Unique chassis configure page")
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
            utility.execLog("Select unique Server settings checkbox")
            self.browserObject.find_element_by_id("uniqueServerConfigSelect").click()
            time.sleep(2)
            utility.execLog("Entering iDrac DNS Name %s"%str(iDracDnsName))
            iDracDnsName_TextField = self.browserObject.find_element_by_xpath("//tbody[@data-bind='foreach: chassisConfiguration.bladeConfiguration']/tr[1]/td[4]/input")
            iDracDnsName_TextField.clear()
            iDracDnsName_TextField.send_keys(iDracDnsName)
            iDRACServerIp=self.browserObject.find_element_by_xpath("//*[@id='form_chassisconfiguration_uniqueserversettings']/div[1]/div/table/tbody/tr[1]/td[3]").text
            utility.execLog("click next on Unique Server configure page")
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
            utility.execLog("Select unique I/O Module settings checkbox")
            self.browserObject.find_element_by_id("uniqueIoModuleConfigSelect").click()
            time.sleep(2)
            utility.execLog("Entering I/O Module host name %s"%str(IoModuleHostName))
            ioHostName_TextField = self.browserObject.find_element_by_xpath("//tbody[@data-bind='foreach: chassisConfiguration.iomConfiguration']/tr[1]/td[4]/input")
            ioHostName_TextField.clear()
            ioHostName_TextField.send_keys(IoModuleHostName)
            utility.execLog("click next on Unique IO Module configure page ")
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
            utility.execLog("click next on IO Module configure page not checked uplink")
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
            utility.execLog("Click Finish on Summary page.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
            time.sleep(2)
            utility.execLog("Click yes on confirmation modal box.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            time.sleep(2)
            return self.browserObject, True,iDRACServerIp
        except Exception as e:
            return self.browserObject, False, "Exception generated while configuring Chassis :: Error-> %s"%str(e)
            
    def verifyiDRAC_DNS(self,iDRACServerIp,iDracDnsName,registeriDracDNS=False):
        try:
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s"%str(iDRACServerIp))
            time.sleep(5)
            newDriver.find_element_by_id("user").send_keys("root")
            newDriver.find_element_by_id("password").send_keys("calvin")
            newDriver.find_element_by_id("submit_lbl").click()
            newDriver.switch_to.default_content()
            time.sleep(1)
            utility.execLog("Switching to 'treelist_id' frame to fetch user info.")
            newDriver.switch_to_frame(newDriver.find_element_by_id("treelist_id"))
            time.sleep(1)
            newDriver.find_element_by_id("exp_iDRAC").click()
            utility.execLog("iDrac settings menu expanded.")
            time.sleep(5)
            newDriver.find_element_by_id("a_Network").click()
            time.sleep(10)
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            time.sleep(1)
            utility.execLog("Switching to 'da' frame to click 'Launch iDrac' button.")
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(1)
            dnsRegistration=newDriver.find_element_by_id('dnsRegistration').is_selected()
            utility.execLog("dnsRegistration={}".format(dnsRegistration))
            time.sleep(1)
            expectediDracDnsName=newDriver.find_element_by_id("racName").get_attribute("value")
            time.sleep(1)
            if expectediDracDnsName==iDracDnsName and dnsRegistration==registeriDracDNS:
                utility.execLog("Successfully verified iDRAC DnS Configuration")
                return self.browserObject, True, "Successfully verified iDRAC DnS Configuration"
            else:
                utility.execLog("Failed to verified iDRAC DnS Configuration")
                return self.browserObject, False, "Failed to verified iDRAC DnS Configuration"
        except Exception as e:
            return self.browserObject, False, "Exception generated while verify new created CMC Error -> %s"%str(e)

    def verifyChassisCondiguration(self, serverIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName):
        """
        Description: Verify the new cmc user on the given chassis IP 
        """ 
        try:
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s/cgi-bin/webcgi/login"%str(serverIP))
            time.sleep(5)
            newDriver.find_element_by_id("user_id").send_keys("root")
            newDriver.find_element_by_id("password").send_keys("calvin")
            newDriver.find_element_by_id("login_submit").click()
            utility.execLog("Logged into Chassis with root")
            time.sleep(30)
            utility.execLog("Switching to snb frame")
            newDriver.switch_to.default_content()
            time.sleep(1)
            newDriver.switch_to_frame(newDriver.find_element_by_name("snb"))
            time.sleep(1)
            utility.execLog("Clicking the setup tab.")
            newDriver.find_element_by_xpath("//*[contains(text(),'Setup')]").click()
            utility.execLog("Waiting for contents to load.")
            time.sleep(10)
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            time.sleep(1)
            utility.execLog("Switching to 'da' frame to click 'Launch iDrac' button.")
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(1)
            textVerificationList=[chassisName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs]
            for item in textVerificationList:
                try:
                    newDriver.find_element_by_xpath("//*[@value='%s']"%str(item))
                except Exception as e:
                    return self.browserObject, False, "Verification for configuration value %s failed"%str(item)
            utility.execLog("Switching to snb frame")
            newDriver.switch_to.default_content()
            time.sleep(1)
            newDriver.switch_to_frame(newDriver.find_element_by_name("snb"))
            time.sleep(1)
            utility.execLog("Clicking the Network tab.")
            newDriver.find_element_by_xpath("//*[contains(text(),'Network')]").click()
            utility.execLog("Waiting for contents to load.")
            time.sleep(5)
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            time.sleep(1)
            utility.execLog("Switching to 'da' frame to click 'Launch iDrac' button.")
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(1)
            try:
                newDriver.find_element_by_xpath("//*[@value='TestDns']")
            except Exception as e:
                return self.browserObject, False, "Verification for Chassis DNS name with value %s failed"%str(chassisDnsName)
            return self.browserObject, True, "All Configuration values %s are available at Chassis, successfully verified."%str(textVerificationList)
        except Exception as e:
            return self.browserObject, False, "Exception generated while verify new created CMC Error -> %s"%str(e)
    
    def verifyChassisCondigurationLogs(self, serverIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName):
        """
        Description: Verify the new cmc user on the given chassis IP 
        """ 
        try:
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s/cgi-bin/webcgi/login"%str(serverIP))
            time.sleep(5)
            newDriver.find_element_by_id("user_id").send_keys("root")
            newDriver.find_element_by_id("password").send_keys("calvin")
            newDriver.find_element_by_id("login_submit").click()
            utility.execLog("Logged into Chassis with root")
            time.sleep(30)
            
            utility.execLog("Switching to snb frame")
            newDriver.switch_to.default_content()
            time.sleep(1)
            newDriver.switch_to_frame(newDriver.find_element_by_name("snb"))
            time.sleep(1)
            utility.execLog("Clicking the Logs tab.")
            newDriver.find_element_by_xpath("//*[contains(text(),'Logs')]").click()
            utility.execLog("Waiting for contents to load.")
            time.sleep(30)
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            time.sleep(1)
            newDriver.switch_to_frame(newDriver.find_element_by_name("lsnb"))
            time.sleep(3)

            utility.execLog("Switching to Chassis Logs")
            newDriver.find_element_by_xpath("//*[contains(text(),'Chassis Log')]").click()
            time.sleep(35)
            utility.execLog("Switched to Chassis Logs")
            utility.execLog("Switching to default content.")
            newDriver.switch_to.default_content()
            time.sleep(1)
            utility.execLog("Switching to 'da' frame to click 'Launch iDrac' button.")
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(3)
            try:
                newDriver.find_element_by_xpath("(//span[contains(@class,'status_information')])[1]")
                time.sleep(2)
                newDriver.close()
                time.sleep(3)
                utility.execLog("Chassis Logs verified successfully")
                return self.browserObject, True, "Chassis Logs verified successfully"
            except Exception as e:
                return self.browserObject, False, "Verification for Chassis Logs failed"
            
            return self.browserObject, True, "All Configuration values are available at Chassis, successfully verified."
        except Exception as e:
            return self.browserObject, False, "Exception generated while verify new created CMC Error -> %s"%str(e)
    
    def configureChassisIOM(self, chassisIP, chassisName,upLinkCheked=True ,DefinedUplink=None, ports=""):
        """
        """
        try:
            self.navigateToChassisConfigurationTab()
            utility.execLog("Navigated to Chassis configuration page.")
            utility.execLog("Selecting the %s chassis"%str(chassisIP))
            chassisIpCheckboxes = self.browserObject.find_elements_by_xpath("//table[@id='discoveredDevicesGrid']/tbody/tr/td[1]/input")
#             utility.execLog("Total number of discovered resources/chassis are %s"%str(chassisIpCheckboxes.length()))
            for item in chassisIpCheckboxes:
                utility.execLog("Unselecting checkbox for %s"%str(item))
                item.click()
                time.sleep(1)
            utility.execLog("Unchecked all chassis ip's.")
            utility.execLog("selecting %s ip chassis"%str(chassisIP))
            self.waitLoading(60)
            self.browserObject.find_element_by_xpath("//table[@id='discoveredDevicesGrid']/tbody/tr/td[2]//span[contains(text(),'%s')]/parent::td/parent::tr/td[1]/input"%str(chassisIP)).click()
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            self.waitLoading(60)
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            self.waitLoading(60)
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            self.waitLoading(60)
            utility.execLog("Select unique I/O Module settings checkbox")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "uniqueIoModuleConfigSelect")), action="CLICK")


            utility.execLog("click next on Unique IO Module configure page ")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            time.sleep(30)
            utility.execLog("click next on IO Module configure page  checked for uplink")
            try:
                utility.execLog("FW version less than 9.9")
                self.browserObject.find_element_by_xpath(".//*[@id='page_configurechassis_iomportsettings']/ul/li[1]/b")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Cancel")), action="CLICK")
                time.sleep(7)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                time.sleep(15)
                utility.execLog("Test case passed..")
                return self.browserObject, True, "Test case passed"
            except:
                utility.execLog("FW version is higher..")
            
            if upLinkCheked:
                self.browserObject.find_element_by_id("configUplinks").click()
                utility.execLog("clicked to checked for uplink")
                time.sleep(5)
                if not DefinedUplink:
                    utility.execLog("notDefinedUplink")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
                    time.sleep(10)
                    xpath="//*[@id='errorMessage']/li/label/strong"
                    acttxt="At least one port on the switch needs to be assigned to an uplink."
                    expctxt=self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    time.sleep(3)
                    if acttxt == expctxt:
                        utility.execLog("Test case passed..")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Cancel")), action="CLICK")
                        time.sleep(7)
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                        time.sleep(15)
                        utility.execLog("Test case passed..")
                        return self.browserObject, True, "Test case passed"
                elif DefinedUplink:
                    utility.execLog("Defined uplink...")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "btnConfigurePortChannels_0")), action="CLICK")
                    time.sleep(5)
                    xpath="//*[@id='configureUplinksTable']/tr/td[2]/input"
                    uplinkName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE",attributeName="value")
                    utility.execLog("Uplink name:%s"%uplinkName)
                    xpath=".//*[@id='configureUplinksTable']/tr/td[3]/select"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue='128', selectBy="VALUE")
                    xpath="//*[contains(@id,'dropdown')]//*[contains(@class,'caret')]"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    
                    networks = self.browserObject.find_elements_by_xpath("//li[contains(@role,'presentation')]/input")
                    networkLength = len(networks)
                    utility.execLog("Network length :%i"%networkLength)
                    for i in xrange(0,networkLength):
                        j=i+1
                        xpath = "(//li[contains(@role,'presentation')]/input)["+str(j)+"]"
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                        time.sleep(2)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "save_configureuplinks")), action="CLICK")
                    time.sleep(5)
                    xpath="//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[2]/div/input"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    xpath=".//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[3]/div/input"   
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    
                    if ports=="10 and 40 G":
                        for i in xrange(0,4):
                            j=i+1
                            try:
                                xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[2]/div[3]/select["+str(j)+"]"
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                                xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[3]/div[3]/select["+str(j)+"]"
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                            except:
                                utility.execLog("Allready configured")
                        self.browserObject.find_element_by_id("btnWizard-Next").click()
                        time.sleep(5)
                        headerWarng="Uplink configuration does not support mixing 10Gb and 40Gb ports"
                        xpath="//*[@id='errorMessage']/li/label/strong"
                        exphdtxt=self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        time.sleep(3)
                        if headerWarng in exphdtxt:
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Cancel")), action="CLICK")
                            time.sleep(7)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                            time.sleep(15)
                            utility.execLog("Test case passed..")
                            return self.browserObject, True, "Test case passed"
                            
                    elif ports=="VLT":
                        utility.execLog("Ports for 10g or 40 g")
                        for i in xrange(0,4):
                            j=i+1
                            xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[2]/div[3]/select["+str(j)+"]"
                            try:
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                            except:
                                utility.execLog("Allready configured")    
                        for i in xrange(0,2):
                            j=i+1    
                            xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[3]/div[3]/select["+str(j)+"]"
                            try:
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                            except:
                                utility.execLog("Allready configured")    
                        for i in xrange(2,4):
                            j=i+1    
                            xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[3]/div[3]/select["+str(j)+"]"
                            try:
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="VLT", selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                            except:
                                utility.execLog("Allready configured")
                        self.browserObject.find_element_by_id("btnWizard-Next").click()
                        time.sleep(5)
                        headerTxt = "Summary"
                        xpath="//*[@id='page_configurechassis_summary']/header/h1"
                        exphdtxt=self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        time.sleep(3)
                        if headerTxt in exphdtxt:
#                             self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
#                             time.sleep(5)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Cancel")), action="CLICK")
                            time.sleep(7)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                            time.sleep(15)
                            
                            utility.execLog("Test case passed..")
                            return self.browserObject, True, "Test case passed"
                    elif ports=="uplink":
                        xpath=".//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[3]/div/input"   
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                        
                        utility.execLog("Ports for 10g")
                        for i in xrange(0,4):
                            j=i+1
                            xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[2]/div[3]/select["+str(j)+"]"
                            try:
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                            except:
                                utility.execLog("Allready configured") 
                        for i in xrange(0,2):
                            j=i+1    
                            xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[3]/div[3]/select["+str(j)+"]"
                            try:
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                                time.sleep(2)
                            except:
                                utility.execLog("Allready configured")
                        self.browserObject.find_element_by_id("btnWizard-Next").click()
                        time.sleep(5)
                        headerTxt = "Summary"
                        xpath="//*[@id='page_configurechassis_summary']/header/h1"
                        exphdtxt=self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        time.sleep(3)
                        if headerTxt in exphdtxt:
#                             self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
#                             time.sleep(5)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Cancel")), action="CLICK")
                            time.sleep(7)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                            time.sleep(15)
                            
                            utility.execLog("Test case passed..")
                            return self.browserObject, True, "Test case passed"
                    
                    else:
                        self.browserObject.find_element_by_id("btnWizard-Next").click()
                        time.sleep(2)     
                    
                    utility.execLog("Uplink defined ...") 
            else:
                utility.execLog("Not checked uplink...")
                self.browserObject.find_element_by_id("btnWizard-Next").click()
                time.sleep(3)
            utility.execLog("Click Finish on Summary page.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
            time.sleep(2)
            utility.execLog("Click yes on confirmation modal box.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            time.sleep(2)
            return self.browserObject, True, "Successfully modified Chassis settings"
        except Exception as e:
            return self.browserObject, False, "Exception generated while configuring Chassis :: Error-> %s"%str(e)
        
# chassis config

    def selectChassisToConfigure(self, chassisIP):
        """
        """
        try:
            utility.execLog("Selecting the %s chassis"%str(chassisIP))
            chassisIpCheckboxes = self.browserObject.find_elements_by_xpath("//table[@id='discoveredDevicesGrid']/tbody/tr/td[1]/input")
#             utility.execLog("Total number of discovered resources/chassis are %s"%str(chassisIpCheckboxes.length()))
            for item in chassisIpCheckboxes:
                utility.execLog("Unselecting checkbox for %s"%str(item))
                item.click()
                time.sleep(1)
            utility.execLog("Unchecked all chassis ip's.")
            utility.execLog("selecting %s ip chassis"%str(chassisIP))
            self.browserObject.find_element_by_xpath("//table[@id='discoveredDevicesGrid']/tbody/tr/td[2]//span[contains(text(),'%s')]/parent::td/parent::tr/td[1]/input"%str(chassisIP)).click()
        except Exception as e:
            return self.browserObject, False, "Exception generated while selecting the chassis to be to be configured. :: Error-> %s"%str(e)
        
    def globalSettings_Users_addCmcUser(self, userName, password, enableUser=True, userGroup="Administrator", confirmPass= None):
        """
        Description: Add CMC User at the the given Chassis IP
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[1]")), action="CLICK")
            time.sleep(1)
            utility.execLog("Expanded Users tab")
            utility.execLog("Clicking add new CMC user link")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "new_user_link")), action="CLICK")
            time.sleep(2)
            utility.execLog("Entering details on user creation page")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "user_username")), action="SET_TEXT", setValue=userName)
            utility.execLog("Entered %s username"%str(userName))
            self.handleEvent(EC.element_to_be_clickable((By.ID, "user_password")), action="SET_TEXT", setValue=password)
            utility.execLog("Entered %s password"%str(password))
            if confirmPass == None:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=password)
                utility.execLog("Entered %s user_confirm_password"%str(password))
            else:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=confirmPass)
                utility.execLog("Entered %s user_confirm_password"%str(confirmPass))
            utility.execLog("Selecting user group %s"%str(userGroup))
            self.handleEvent(EC.element_to_be_clickable((By.ID, "role")), action="SELECT", setValue=userGroup)
            time.sleep(1)
            if enableUser == False:
                utility.execLog("Unchecking the check-box")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_enabled")), action="CLICK")
                time.sleep(1)
            else:
                utility.execLog("Enable user check -box is already checked.")
            utility.execLog("Save the user by clicking save button.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_user_form")), action="CLICK")
            time.sleep(2)
            utility.execLog("Verify if user is added.")
            try:
                self.browserObject.find_element_by_xpath("//*[contains(text(),'HclTestCmcUser')]")
                utility.execLog("User %s successfully added."%str(userName))
            except:
                utility.execLog("User %s is not added."%str(userName))
                
#             pageList = ["Chassis Configuration","Unique Chassis Settings","Unique Server Settings", "Unique IO Settings", "IO Configuration"]
#             for elem in pageList:
#                 utility.execLog("Click next on %s page"%str(elem))
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
#                 time.sleep(1)
#                 
#             utility.execLog("Click Finish on Summary page.")
#             self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
#             time.sleep(2)
#             
#             utility.execLog("Click yes on confirmation modal box.")
#             self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
#             time.sleep(2)
            time.sleep(2)
            return self.browserObject, True, "Successfully added CMC user %s"%str(userName)
        except Exception as e:
            return self.browserObject, False, "Unable to create CMC user :: Error -> %s"%str(e)

    def globalSettings_Users_addiDracUser(self, userNames, password, roles, lanRoles, enableUser, confirmPass= None):
        """
        Description: Add iDrac User at the the given Chassis IP
        """
        try:
            x= 0
            for item in userNames:
                utility.execLog("Clicking add new iDrac user link")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "new_idracuser_link")), action="CLICK")
                time.sleep(2)
                utility.execLog("Entering details on user creation page")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_username")), action="SET_TEXT", setValue=userNames[x])
                utility.execLog("Entered %s username"%str(userNames[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "user_password")), action="SET_TEXT", setValue=password)
                utility.execLog("Entered %s password"%str(password))
                if confirmPass == None:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=password)
                    utility.execLog("Entered %s user_confirm_password"%str(password))
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_confirm_password")), action="SET_TEXT", setValue=confirmPass)
                    utility.execLog("Entered %s user_confirm_password"%str(confirmPass))
                utility.execLog("Selecting role %s"%str(roles[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "lan")), action="SELECT", setValue=roles[x])
                time.sleep(1)
                utility.execLog("Selecting Lan role %s"%str(lanRoles[x]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "idracrole")), action="SELECT", setValue=lanRoles[x])
                time.sleep(1)
                if enableUser == False:
                    utility.execLog("Unchecking the check-box")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "user_enabled")), action="CLICK")
                    time.sleep(1)
                else:
                    utility.execLog("Enable user check -box is already checked.")
                utility.execLog("Save the user by clicking save button.")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_user_form")), action="CLICK")
                time.sleep(2)
                utility.execLog("Verify if user is added.")
                try:
                    self.browserObject.find_element_by_xpath("//*[contains(text(),'%s')]"%str(userNames[x]))
                    utility.execLog("User %s successfully added."%str(userNames[x]))
                except:
                    utility.execLog("User %s is not added."%str(userNames[x]))
                x=x+1
#             pageList = ["Chassis Configuration","Unique Chassis Settings","Unique Server Settings", "Unique IO Settings", "IO Configuration"]
#             for elem in pageList:
#                 utility.execLog("Click next on %s page"%str(elem))
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
#                 time.sleep(1)
#                 
#             utility.execLog("Click Finish on Summary page.")
#             self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
#             time.sleep(2)
#             
#             utility.execLog("Click yes on confirmation modal box.")
#             self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            time.sleep(2)
            return self.browserObject, True, "Successfully added iDrac users %s"%str(userNames)
        except Exception as e:
            return self.browserObject, False, "Unable to create iDrac user :: Error -> %s"%str(e)
            
#     def manage_GlobalSettings_Users(self, user, cmcUserName, cmcPassword, cmcEnableUser=True, cmcUserGroup="Administrator", cmcConfirmPass= None, iDracUserNames, iDracPassword, iDracRoles, iDracLanRoles, iDracEnableUser, iDracConfirmPass= None):
#         """
#         """
#         try:
#             if "cmc" in user:
#                 self.globalSettings_Users_addCmcUser(cmcUserName, cmcPassword, cmcEnableUser, cmcUserGroup, cmcConfirmPass)
#             elif "idrac" in user:
#                 self.globalSettings_Users_addiDracUser(iDracUserNames, iDracPassword, iDracRoles, iDracLanRoles, iDracEnableUser, iDracConfirmPass)
#             else:
#                 self.globalSettings_Users_addCmcUser(cmcUserName, cmcPassword, cmcEnableUser, cmcUserGroup, cmcConfirmPass)  
#                 self.globalSettings_Users_addiDracUser(iDracUserNames, iDracPassword, iDracRoles, iDracLanRoles, iDracEnableUser, iDracConfirmPass)
#         except Exception as e:
#             return self.browserObject, False, "Exception generated while adding users in global settings tab. :: Error-> %s"%str(e)
    
    def manage_GlobalSettings_Monitoring(self, destinationIP, communityString, smtpServer, destinationEmail, name, syslogDestinationIP):
        """
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[2]")), action="CLICK")
            time.sleep(1)
            utility.execLog("Expanded Monitoring tab")
            utility.execLog("Adding Alert destination.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "alertDestinationAdd")), action="CLICK")
            time.sleep(2)
            utility.execLog("Entering destination IP address")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "destinationIpAddressValue")), action="SET_TEXT", setValue=destinationIP)
            utility.execLog("Entering Community String value")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "communityStringValue")), action="SET_TEXT", setValue=communityString)
            utility.execLog("Entering SMTP Server value")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "idsmtpServer")), action="SET_TEXT", setValue=smtpServer)
            utility.execLog("Clicking add Email alert button.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "destinationEmailAdd")), action="CLICK")
            time.sleep(2)
            utility.execLog("Entering destinationemail address")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "destinationEmailEmail")), action="SET_TEXT", setValue=destinationEmail)
            utility.execLog("Entering name field value")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "destinationEmailName")), action="SET_TEXT", setValue=name)
            utility.execLog("Entering syslog destination IP")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "syslogDestination")), action="SET_TEXT", setValue=syslogDestinationIP)
        except Exception as e:
            return self.browserObject, False, "Exception generated while adding Monitoring values in global settings tab. :: Error-> %s"%str(e)
        
    def manage_GlobalSettings_NTP(self, timeZone, ntpServer):
        """
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[3]")), action="CLICK")
            time.sleep(1)
            utility.execLog("Expanded NTP tab")
            utility.execLog("Selecting time zone value")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "timeZone")), action="SELECT", setValue=timeZone)
            time.sleep(1)
            utility.execLog("Enable NTP Server")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "enableNTPServer")), action="CLICK")
            time.sleep(2)
            utility.execLog("Entering NTP server value")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "preferredNTPServer")), action="SET_TEXT", setValue=ntpServer)
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting NTP options in Global settings tab. :: Error-> %s"%str(e)
        
    def manage_GlobalSettings_PowerConfig(self, policy):
        """
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[4]")), action="CLICK")
            time.sleep(1)
            utility.execLog("Expanded Power config tab")
            utility.execLog("Selecting policy value")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "policy")), action="SELECT", setValue=policy)
            time.sleep(1)
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting power config options in Global settings tab. :: Error-> %s"%str(e)
    
    def manage_GlobalSettings_Networking(self, enableChassis, enableiDrac, enaleIpmi):
        """
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//*[@id='chassisConfigPanels']//h2/span)[5]")), action="CLICK")
            time.sleep(1)
            utility.execLog("Expanded networking tab")
            if enableChassis:
                utility.execLog("Enabling Register chassis controller option.")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "registerDNS")), action="CLICK")
            if enableiDrac: 
                utility.execLog("Enabling Register iDRAC on DNS option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "registeriDracDNS")), action="CLICK")
            if enaleIpmi:
                utility.execLog("Enabling Enable IPMI over LAN option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "enableipmi")), action="CLICK")
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting Networking options in Global settings tab. :: Error-> %s"%str(e)
        
    def manage_UniqueChassisSettings(self, chassisName, chassisDnsName, storageMode, inputPowerCap, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs):
        """
        """
        try:
            utility.execLog("Select unique chassis settings checkbox")
            self.browserObject.find_element_by_id("uniqueChassisConfigSelect").click()
            time.sleep(2)
            utility.execLog("Enter Chassis Name %s"%str(chassisName))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.name']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.name']").send_keys(chassisName)
            utility.execLog("Enter Chassis DNS Name %s"%str(chassisDnsName))
            self.browserObject.find_element_by_id("cmcdnsname").clear()
            self.browserObject.find_element_by_id("cmcdnsname").send_keys(chassisDnsName)
            utility.execLog("Selecting storage mode value")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//select[@id='storageMode']")), action="SELECT", setValue=storageMode, selectBy="VALUE")
            except:
                utility.execLog("Storage Mode selection option not available")
            utility.execLog("Selecting Input Power Cap Scale")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "measurementType")), action="SELECT", setValue=inputPowerCap)
            utility.execLog("Enter Chassis Location details")
            utility.execLog("Enter location details Data center value %s"%str(locDetailsDc))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.dataCenter']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.dataCenter']").send_keys(locDetailsDc)
            utility.execLog("Enter location details Asile value %s"%str(locDetailsAsile))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.aisle']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.aisle']").send_keys(locDetailsAsile)
            utility.execLog("Enter location details Rack value %s"%str(locDetailsRack))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rack']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rack']").send_keys(locDetailsRack)
            utility.execLog("Enter location details Rack Slot value %s"%str(locDetailsRs))
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rackSlot']").clear()
            self.browserObject.find_element_by_xpath("//input[@data-bind='value: chassisConfiguration.deviceConfiguration.rackSlot']").send_keys(locDetailsRs)
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting Unique Chassis Settings options in Global settings tab. :: Error-> %s"%str(e)
        
    def manage_UniqueServerSettings(self, iDracDnsName):
        """
        """
        try:
            utility.execLog("Select unique Server settings checkbox")
            self.browserObject.find_element_by_id("uniqueServerConfigSelect").click()
            time.sleep(2)
            utility.execLog("Entering iDrac DNS Name %s"%str(iDracDnsName))
            iDracDnsName_TextField = self.browserObject.find_element_by_xpath("//tbody[@data-bind='foreach: chassisConfiguration.bladeConfiguration']/tr[1]/td[4]/input")
            iDracDnsName_TextField.clear()
            iDracDnsName_TextField.send_keys(iDracDnsName)
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting Unique Server Settings options in Global settings tab. :: Error-> %s"%str(e)
        
    def manage_UniqueIOMOdule(self, IoModuleHostName):
        try:
            utility.execLog("Select unique I/O Module settings checkbox")
            self.browserObject.find_element_by_id("uniqueIoModuleConfigSelect").click()
            time.sleep(2)
            utility.execLog("Entering I/O Module host name %s"%str(IoModuleHostName))
            ioHostName_TextField = self.browserObject.find_element_by_xpath("//tbody[@data-bind='foreach: chassisConfiguration.iomConfiguration']/tr[1]/td[4]/input")
            ioHostName_TextField.clear()
            ioHostName_TextField.send_keys(IoModuleHostName)            
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting Unique Unique IO Module options in Global settings tab. :: Error-> %s"%str(e)
        
    def manage_IOModuleConfiguration(self, uplinkName, portChannel):
        try:
            utility.execLog("Configuring Uplink for I/O Module")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "configUplinks")), action= "CLICK")
            utility.execLog("Selecting Configure Uplink Ports on All Chassis independently")
            self.handleEvent(EC.presence_of_element_located((By.ID,"iomports_independent")), action= "CLICK")
            utility.execLog("Defining new uplink")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnConfigurePortChannels_0")), action= "CLICK")
            time.sleep(2)
            utility.execLog("Setting uplink name")
            xpath= "//*[@id='configureUplinksTable']//*[contains(@data-bind,'uplinkName')]"
            self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLEAR")
            self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="SET_TEXT", setValue=uplinkName)
            utility.execLog("Setting Port Channel")
            port= self.browserObject.find_element_by_xpath("//*[@id='configureUplinksTable']//*[contains(@data-bind, 'portChannel')]")
            select= Select(port)
            select.select_by_value(str(portChannel))
            time.sleep(2)
            xpath="//*[@id='configureUplinksTable']//*[@class='selectNetworkTD']/div[@class='dropdown']"
            networkTypeDropdown= self.browserObject.find_element_by_xpath(xpath)
            networkTypeDropdown.click()
            time.sleep(2)
            networkTypes= networkTypeDropdown.find_elements_by_xpath(".//*[@class='networkTypeCheckbox']")
            for networkType in networkTypes:
                networkType.click()
            time.sleep(2)
            self.browserObject.find_element_by_id("save_configureuplinks").click()
            time.sleep(2)
            selectUplinks=self.browserObject.find_elements_by_xpath(".//select[@class='form-control portModule']")
#             for selectUplink in selectUplinks:
# #                 select=Select(selectUplink)
#                 try:
#                     self.handleEvent(EC.element_to_be_clickable((selectUplink)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
#                     select.select_by_index(1)
#                 except:
#                     try:
#                         select.select_by_index(0)
#                     except:
#                         select.select_by_visible_text("VLT")
                    
            for i in xrange(1,5):
                try:
                    xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[2]/div[3]/select["+str(i)+"]"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                    time.sleep(2)
                    xpath = "//*[@id='uplinkChassisConfigPanels']/div/div/ul/li[4]/div/table/tbody/tr/td[3]/div[3]/select["+str(i)+"]"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=uplinkName, selectBy="VISIBLE_TEXT")
                    time.sleep(2)
                except:
                    utility.execLog("Allready configured")
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
        except Exception as e:
            return self.browserObject, False, "Exception generated while setting Unique Chassis Settings options in Global settings tab. :: Error-> %s"%str(e)
        
    def defineChassisSettings(self, component):
        """
        """
        try:
            self.navigateToChassisConfigurationTab()
            utility.execLog("Navigated to Chassis configuration page.")
            utility.execLog("Selecting the %s chassis"%str(component["chassisSetting"]["chassisIpConfig"]["chassisIP"]))
            self.selectChassisToConfigure(component["chassisSetting"]["chassisIpConfig"]["chassisIP"])
            time.sleep(4)
            self.globalSettings_Users_addCmcUser(component["chassisSetting"]["cmcUser"]["username"],component["chassisSetting"]["cmcUser"]["password"],\
                                                  component["chassisSetting"]["cmcUser"]["enableUser"],component["chassisSetting"]["cmcUser"]["userGroup"])
            time.sleep(4)
            self.globalSettings_Users_addiDracUser(component["chassisSetting"]["idracUser"]["userNames"],component["chassisSetting"]["idracUser"]["password"],component["chassisSetting"]["idracUser"]["roles"], component["chassisSetting"]["idracUser"]["lanRoles"],component["chassisSetting"]["idracUser"]["enableUser"])
                                                    
            time.sleep(4)
            self.manage_GlobalSettings_NTP(component["chassisSetting"]["ntp"]["timezone"],component["chassisSetting"]["ntp"]["ntpServer"] )
            time.sleep(4)
            self.manage_GlobalSettings_PowerConfig(component["chassisSetting"]["powerConfig"]["policy"])
            time.sleep(4)
            self.manage_GlobalSettings_Networking(component["chassisSetting"]["Networking"]["enableChassis"], component["chassisSetting"]["Networking"]["enableiDrac"], \
                                                  component["chassisSetting"]["Networking"]["enaleIpmi"])
            time.sleep(4)
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(4)
            self.manage_UniqueChassisSettings(component["uniqueChassisSetting"]["chassisName"],component["uniqueChassisSetting"]["chassisDnsName"] ,\
                                              component["uniqueChassisSetting"]["storageMode"] ,component["uniqueChassisSetting"]["inputPowerCap"], component["uniqueChassisSetting"]["locDetailsDc"], \
                                              component["uniqueChassisSetting"]["locDetailsAsile"], component["uniqueChassisSetting"]["locDetailsRack"],component["uniqueChassisSetting"]["locDetailsRs"])
                                               
            time.sleep(4)
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(4)
            self.manage_UniqueServerSettings(component["uniqueServerSetting"]["hostname"])
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(4)
            self.manage_UniqueIOMOdule(component["UniqueIOModule"]["IoModuleHostName"])
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(4)
            if component["UniqueIOModule"]["defineUplink"]:
                self.manage_IOModuleConfiguration(component["UniqueIOModule"]["uplinkName"],component["UniqueIOModule"]["portChannel"])
                time.sleep(2)
            self.browserObject.find_element_by_id("btnWizard-Finish").click()
            time.sleep(2)
            self.browserObject.find_element_by_id("submit_confirm_form").click()
            time.sleep(2)
            return self.browserObject, True, "Chassis Configuration Complete"
        except Exception as e:
            return self.browserObject, False, "Error while configuring Chassis %s"%e

    def verifyUpLinkOnChessis(self,serverIP):
        """
        Description: Verify the UpLink on the given chassis IP 
        """ 
        try:
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s/cgi-bin/webcgi/login"%str(serverIP))
            time.sleep(5)
            newDriver.find_element_by_id("user_id").send_keys("root")
            newDriver.find_element_by_id("password").send_keys("calvin")
            newDriver.find_element_by_id("login_submit").click()
            utility.execLog("Logged into Chassis with root")
            time.sleep(15)
            utility.execLog("Switching to treelist frame")
            newDriver.switch_to.default_content()
            time.sleep(1)
            newDriver.switch_to_frame(newDriver.find_element_by_name("treelist"))
            time.sleep(1)
            utility.execLog("Clicking the server in tree list of Chassis %s"%str(serverIP))
            IOModuleList=newDriver.find_elements_by_xpath("//table[@class='component-tree' or @class='']//tbody//tr//td[5]")
            count=0
            swithIpList=[]
            for IOModule in IOModuleList:
                if (IOModule.is_displayed() and IOModule.find_element_by_xpath(".//a//span[2]").text!="Not Installed"):
                    count=count+1
                    IOModule.click()
                    utility.execLog("Waiting for contents to load. ie. iDrac Launch button")
                    time.sleep(5)
                    utility.execLog("Switching to default content.")
                    newDriver.switch_to.default_content()
                    time.sleep(1)
                    utility.execLog("Switching to 'da' frame to click .")
                    newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
                    swithIp=newDriver.find_element_by_xpath("//*[@id='iomIpAddress']").text
                    swithIpList.append(swithIp)
                    utility.execLog("Switching to default content.")
                    newDriver.switch_to.default_content()
                    time.sleep(1)
                    newDriver.switch_to_frame(newDriver.find_element_by_name("treelist"))
                    time.sleep(1)
            #swithIpListSize=len(swithIpList)
            utility.execLog("swithIpList={}".format(swithIpList))
            return True,swithIpList    
        except Exception as e:
            utility.execLog("Exception in verifyUpLinkOnChessis:-".format(e))
            return False,"Exception in verifyUpLinkOnChessis"

    def verifyCmcUserCreated(self, serverIP, userNames):
        """
        Description: Verify the new cmc user on the given chassis IP 
        """ 
        try:
            utility.execLog("Logged into Chassis")
            pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=True, browserObject=None)
            newDriver = pageObject.getChromeBrowser()
            utility.execLog("New browser window opened")
            newDriver.get("https://%s/cgi-bin/webcgi/login"%str(serverIP))
            time.sleep(5)
            newDriver.find_element_by_id("user_id").send_keys("root")
            newDriver.find_element_by_id("password").send_keys("calvin")
            newDriver.find_element_by_id("login_submit").click()
            utility.execLog("Logged into Chassis with root")
            time.sleep(15) 
            newDriver.switch_to.default_content()
            time.sleep(2)
            newDriver.switch_to_frame(newDriver.find_element_by_name("snb"))
            newDriver.find_element_by_xpath("//table[@id='snbtable']//tbody//tr//td//a[contains(text(),'User Authentication')]").click()
            newDriver.switch_to.default_content()
            time.sleep(2)
            newDriver.switch_to_frame(newDriver.find_element_by_name("da"))
            time.sleep(2)
            for user in userNames:
                try:
                    newDriver.find_element_by_xpath("//*[@id='myForm']/div[2]/table/tbody/tr/td[3][contains(text(),'"+user+"')]")
                    utility.execLog("user {} is present on chessis.".format(user))
                except Exception as e:
                    utility.execLog("user {} is not present on chessis.".format(user))
                    return self.browserObject, False, "user {} is not present on chessis".format(user)
                       
            return self.browserObject, True,"All CMC user are present on chisses"           
        except Exception as e:
            return self.browserObject, False, "Exception generated while verify new created CMC Error -> %s"%str(e)