"""
Author: ASM Automation Team
Created/Modified: Sep 30th 2015/Mar 9th 2017
Description: Base-Class for ASM Automation Framework
"""

import os
import re
import time
import datetime
import traceback
import json
import ConfigParser
import paramiko
from libs.product import globalVars
from libs.product import BaseClass
from libs.product import utility
from libs.core.SSHConnection import SSHConnection
from libs.product.pages import *
from libs.product.pages import UIException, Login, InitialSetup, Logs, Users, LandingPage, BackupAndRestore, Repositories, GettingStarted
from libs.product.pages import Credentials, Networks, VirtualApplianceManagement, VirtualIdentityPools, Resources, Portview, Services, Navigation, Jobs, AddOnModules, TranslateJSONString
from libs.product.pages import Templates, Dashboard
from libs.core.dellunit.unittest2 import SkipTest
from libs.thirdparty.selenium.common.exceptions import UnexpectedAlertPresentException
from libs.thirdparty.selenium.common.exceptions import NoAlertPresentException
from libs.thirdparty.selenium.webdriver.common.by import By
from libs.thirdparty.selenium.webdriver.support.ui import WebDriverWait 
from libs.thirdparty.selenium.webdriver.support import expected_conditions as EC
from libs.thirdparty.selenium.common.exceptions import TimeoutException
from datetime import timedelta
from tests import settings
from distutils import version as v
import xml.etree.ElementTree as ET
from libs.product.objects.Manager import Manager

class Manager(BaseClass.TestBase, Manager):
    """
    Description:
        Test class to be used as a Base-Class for the Automation Framework.
        This class is derived from "unittest.TestCase" class.
    """
    def __init__(self, *args, **kwargs):
        """
        Description: 
            Initializing an object of Manager class.
                
        Input:
            None
                            
        Output:
            None
        
        """
        # Initializing Derived Classes
        BaseClass.TestBase.__init__(self, *args, **kwargs)
        self.browserObject = globalVars.browserObject
        self.currentUser = globalVars.currentUser
        self.deployedResources = None
        self.staticIP = False
        self.manualServer = False
        
    def connectSSH(self, hostType, HOST, COMMAND, username=None, password=None):
        """
        SSH Connection Function to Appliance/Server/Switch/VM
        """
        self.succeed("SSH Connection to %s: %s" %(hostType, HOST))
        if username==None and password==None:
            if (hostType == 'Appliance'):
                USERNAME="delladmin"
                PASSWORD="delladmin"
            elif (hostType=='Switch'):
                USERNAME="admin"
                PASSWORD="dell1234"
            elif (hostType=="LAN" or hostType=="Workload" or hostType=="VM" or hostType=="OS"):
                USERNAME="root"
                PASSWORD="Dell1234"
            else:
                USERNAME=""
                PASSWORD=""
        else:
            USERNAME=username
            PASSWORD=password
        if (USERNAME!="" and PASSWORD!="" and HOST!="" and COMMAND!=""):
            ssh = SSHConnection(HOST, USERNAME, PASSWORD, 22, 120)
            connection_result, connection_error = ssh.Connect()
            if (connection_error != ""):
                self.failure(connection_error)
                ssh.Close()
                return False, connection_error
            else:
                self.succeed("Running '%s' on '%s'..." %(COMMAND, HOST))
                command_result, command_error = ssh.Execute(COMMAND)
                if (command_error != ""):
                    self.failure(command_error)
                    ssh.Close()
                    return False, command_error
                else:
                    ssh.Close()
                    return True, command_result

    def closeUnexpectedAlerts(self):
        """
        Closes Unexpected Alerts during Test-Runs
        """
        try:
            alertObject = self.browserObject.switch_to_alert()
            self.succeed("Alert text: " + alertObject.text)
            alertObject.accept()
            self.succeed("Alert detected, accepted it")
        except UnexpectedAlertPresentException:
            self.succeed("Unexpected Alert detected")
        except NoAlertPresentException:
            self.succeed("No Alert detected")
        except Exception:
            self.succeed("Exception while closing Unexpected Alerts")
    
    def closeDialog(self):
        """
        Closes Dialog opened in previous Test-Run
        """
        try:
            try:
                self.succeed("Trying to access menu for identifying Open Dialogs")
                WebDriverWait(self.browserObject, globalVars.defaultWaitTime).until(EC.element_to_be_clickable((By.CLASS_NAME, "container-fluid"))).click()
                self.succeed("No existing opened pop ups")
            except:
                self.succeed("Pop-ups exist so reloading page")
                self.browserObject.get('https://%s/asmui/index.html' % (self.applianceIP))
                time.sleep(10)                
            try:
                self.succeed("Trying to close any opened pop up modals")
                WebDriverWait(self.browserObject, globalVars.defaultWaitTime).until(EC.element_to_be_clickable((By.ID, "popupModal"))).click()
                self.succeed("Closed existing opened pop up modal")
            except TimeoutException:
                self.succeed("No existing opened pop up modal")            
            try:
                self.succeed("Trying to Close any Dialogs opened in the previous test run")
                self.browserObject.find_element_by_class_name("modal-tools-close").click()
                self.succeed("Clicked on Close Icon to close Dialog")
                time.sleep(3)
            except:
                self.succeed("No Dialogs opened")
            self.browserObject.find_element_by_id("confirm_modal_form").click()
            time.sleep(1)
            self.browserObject.find_element_by_id("submit_confirm_form").click()
            time.sleep(1)
            try:
                self.succeed("Identifying Confirm Dialog box and Confirming")
                eleDialog = self.browserObject.find_element_by_id("popupModal")
                eleDialog.click()
                self.succeed("Identified Confirm Dialog box and Confirmed")
            except:
                self.succeed("No Confirm Dialog box is identified")
            self.succeed("Closed Pop-up opened in the previous test run")            
        except Exception as e:
            self.succeed("No Pop-up opened in the previous test run to close")
            self.closeUnexpectedAlerts()
    
    def closeUnwantedWindows(self):
        """
        Closes Unwanted Windows
        """
        try:            
            self.succeed("Trying to Close Unwanted Windows opened in the previous Test-Run")
            windowHandles = self.browserObject.window_handles
            if len(windowHandles) > 1: 
                for h in windowHandles[1:]:
                    self.browserObject.switch_to_window(h)                    
                    self.browserObject.close()
                    self.succeed("Closed Window: '%s'" % str(h))
                self.browserObject.switch_to_window(self.browserObject.window_handles[0])
                self.succeed("Closed Unwanted Windows opened in the previous Test-Run")
            else:
                self.succeed("No Unwanted Windows opened in the previous Test-Run")
        except:
            self.succeed("Exception while getting Window Handles in Setup")

    def highLight(self, element):
        """
        Highlights (blinks) a Selenium Webdriver element
        """
        self.browserObject = element._parent
        def apply_style(s):
            self.browserObject.execute_script("arguments[0].setAttribute('style', arguments[1]);", element, s)
        original_style = element.get_attribute('style')
        apply_style("background: yellow; border: 2px solid red;")
        time.sleep(.3)
        apply_style(original_style) 
    
    def closePopup(self):
        """
        Closes Dialog opened in previous Test-Run
        """        
        try:
            self.succeed("Trying to access menu for identifying Open Dialogs")
            WebDriverWait(self.browserObject, globalVars.defaultWaitTime).until(EC.element_to_be_clickable((By.CLASS_NAME, "container-fluid"))).click()
            self.succeed("No Existing Opened Pop-Ups")
        except:
            self.succeed("Pop-Ups Exist...Reloading Page")
            self.browserObject.get('https://%s/asmui/index.html' % (self.applianceIP))
            time.sleep(10)                
               
    def setUp(self, checkLogin=True):
        """
        Set-Up Manager Class & Objects
        """       
        BaseClass.TestBase.setUp(self)
        self.browserObject = globalVars.browserObject
        #self.closeUnexpectedAlerts()
        #self.closeDialog()
        #self.closeUnwantedWindows()
        self.closePopup()
        self.browserObject.refresh()
        try:
            if checkLogin:
                self.browserObject.find_element_by_xpath(self.ManagerObjects('formLogin'))
                self.login(username=self.loginUser, password=self.loginPassword, newInstance=False)            
        except Exception as e:
            pass    
    
    def getCurrentUser(self):
        """
        Description: 
            Gets Current Logged-In User
        """
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject, status, curUser = pageObject.getLoginUser()
        if status:
            utility.execLog("Current User: %s" % curUser)
            self.currentUser = globalVars.currentUser = curUser
            return curUser
        else:
            self.failure("Exception while fetching Current Logged-In User", raiseExc=True)
        
    def login(self, username=None, password=None, newInstance=True, negativeScenario=False, waitTime=0, pattern=None):
        """
        Description: 
            Logs into ASM Appliance
        """
        if username == None:
            username = self.loginUser
        if password == None:
            password = self.loginPassword
        pageObject = Login.Login(self.browserName, self.applianceIP, newInstance=newInstance, browserObject=self.browserObject)
        globalVars.browserObject, status, result = pageObject.getBrowserHandle()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, landingPage, status, result = pageObject.loginApp(username, password, negativeScenario, waitTime)
        self.browserObject = globalVars.browserObject
        self.currentUser = globalVars.currentUser
        if status:
            if pattern:
                if pattern in result:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
            else:
                self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def logout(self):
        """
        Description: 
            Logs out of the ASM Appliance
        """
        isObject = Login.Login(self.browserName, self.applianceIP, newInstance=False, browserObject=self.browserObject)
        globalVars.browserObject, status, result = isObject.logoutApp()
        if status:
            self.browserObject = globalVars.browserObject
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def initialSetup(self):
        """
        Description: 
            Performs Initial Setup
        """
        # Fetching Values from globalVars
        timeZone = globalVars.configInfo['Appliance']['timezone']
        primaryNTP = globalVars.configInfo['Appliance']['ntpserver']
        serviceTag = globalVars.serviceTag
        isObject = InitialSetup.InitialSetup(globalVars.browserObject)    
        globalVars.browserObject, status, result = isObject.processInitialSetup(timeZone, primaryNTP, serviceTag=serviceTag)
        self.browserObject = globalVars.browserObject
        if not status:
            if "Active System Manager" in self.browserObject.title:
                self.succeed("Logged-Out after applying NTP Settings, Trying to Log-In again...")
                self.login(newInstance=False)
                # self.login(newInstance=False, browserObject=globalVars.browserObject) --> Unexpected Argument
                isObject = InitialSetup.InitialSetup(globalVars.browserObject)    
                globalVars.browserObject, status, result = isObject.processInitialSetup(timeZone, primaryNTP, serviceTag=serviceTag)
                if not status:
                    self.failure(result, raiseExc=True)
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            self.succeed(result)        
    
    def verifyCurrentUser(self, userRole='Read only', loginAsUser=True, enableUser=True, disableUser=False, reLogin=False):
        """
        Description: 
            Verify Current Logged-In User is Read-Only User
        """        
        userPassword = globalVars.rosPassword
        if userRole == "Read only":
            userName = globalVars.readOnlyUser            
        elif userRole == "Standard":
            userName = globalVars.standardUser
        else:
            userName = self.loginUser
            userPassword = self.loginPassword
        if reLogin:
            # Logout from Current-User
            self.logout()
            # Login as Mentioned User
            self.login(username=userName, password=userPassword, newInstance=False)
            
            curUser = self.getCurrentUser()
            if curUser == userName:
                self.succeed("Verified Current User Name for Role : '%s' User : %s" % (userRole, curUser))
            else:
                self.failure("Failed to verify Current User Name for Role : '%s' User : %s" % (userRole, curUser), raiseExc=True)
        else:
            if loginAsUser:
                curUser = self.getCurrentUser()
                if userName not in curUser:
                    self.succeed("Current User is not '%s' User so trying to login as user '%s'" % (userRole, userName))
                    if userName != self.loginUser:
                        if self.loginUser not in curUser:
                            # Logout from Current User
                            self.logout() 
                            time.sleep(5)                                       
                            # Login as Admin User
                            self.login(username=self.loginUser, password=self.loginPassword, newInstance=False)
                        # Check for User with required Role and Create if it doesn't exist        
                        users = self.getLocalUsers(userName=userName)
                        if len(users) <= 0:
                            self.createLocalUser(userName=userName, userPassword=userPassword, userRole=userRole, verifyUser=True)
                        elif users[0]["State"] == "Disabled":
                            self.enableLocalUser(userName=userName, verifyUser=True)
                    # Logout from Current-User
                    self.logout()
                    # Login as Mentioned User
                    self.login(username=userName, password=userPassword, newInstance=False)
                    # Get Current User
                    curUser = self.getCurrentUser()
                    if userName in curUser:
                        self.succeed("Verified Current User Name for Role : '%s' User : %s" % (userRole, curUser))
                    else:
                        self.failure("Failed to verify Current User Name for Role : '%s' User : %s" % (userRole, curUser), raiseExc=True)
                else:
                    self.succeed("Current User is '%s' User : %s" % (userRole, curUser))
            else:
                # Check for User with required Role and Create if it doesn't exist        
                users = self.getLocalUsers(userName=userName)
                if len(users) <= 0:
                    self.createLocalUser(userName=userName, userPassword=userPassword, userRole=userRole, verifyUser=True)
                else:
                    if users[0]["State"] == "Disabled" and enableUser:
                        self.enableLocalUser(userName=userName, verifyUser=True)
                    elif users[0]["State"] == "Enabled" and disableUser:
                        self.disableLocalUser(userName=userName, verifyUser=True, useEdit=False, currentPassword=self.loginPassword)
    
    def verifyLogoutOption(self, userRole='Readonly'):
        """
        Description: 
            Verify Logout Option is available for 'Read-Only' User
        """
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyLogoutOption(userRole)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject                
        
    def getLocalUsers(self, userRole=None, userName=None):
        """
        Description: 
            Gets All Local Users
        """
        customList = []
        pageObject = Users.Users(self.browserObject) 
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

        globalVars.browserObject, status, result = pageObject.getUsers()
        if status:
            userList = result
            self.succeed("Successfully Fetched Local Users: %s" % str(userList))
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject

        if userName:
            customList = [user for user in userList if user["Name"] == userName]                
        elif userRole:
            customList = [user for user in userList if user["Role"] == userRole]
        else:
            customList = userList
        return customList
    
    def getDirectoryServices(self):
        """
        Description: 
            Gets All Directory Services
        """
        time.sleep(5)
        pageObject = Users.Users(self.browserObject)     
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)                   
        globalVars.browserObject, status, result = pageObject.getDirectoryServices()
        if status:
            self.succeed("Successfully Fetched Directory Services: %s" % str(result))
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject            
        return result

    def createLocalUser(self, userName, userPassword, firstName="", lastName="", 
                   userRole="Administrator", userEmail="", userPhone="", enableUser=True, verifyUser=False, 
                   negativeScenario=False):
        """
        Description: 
            Create Local User
        """
        # Create User
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.createUser(self.loginPassword, userName, userPassword,
                        firstName, lastName, userRole, userEmail, userPhone, enableUser)
        if not negativeScenario:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject

        # Verify User Creation
        if verifyUser:                
            users = self.getLocalUsers(userRole=userRole)
            users = [user for user in users if user["Name"] == userName]
            if not negativeScenario:
                if len(users) > 0:
                    self.succeed("Successfully verified Created Local User :: '%s' with Role :: '%s'" % (userName, userRole))
                else:
                    self.failure("Failed to verify Created Local User :: '%s' with Role :: '%s'" % (userName, userRole), raiseExc=True)
            else:
                if len(users) <= 0:
                    self.succeed("Successfully verified Local User is not Created :: '%s' with Role :: '%s'" % (userName, userRole))
                else:
                    self.failure("Local User is Created :: '%s' with Role :: '%s'" % (userName, userRole), raiseExc=True)
    
    def editLocalUser(self, userName, userPassword=None, firstName=None, lastName=None, 
                   userRole=None, userEmail=None, userPhone=None, enableUser=True, verifyUser=False, 
                   negativeScenario=False):
        """
        Description: 
            Edit Local User
        """
        #Create User
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.editUser(userName, self.loginPassword, userPassword,
                        firstName, lastName, userRole, userEmail, userPhone, enableUser)
        if not negativeScenario:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject         
        #Verify User Creation
        if verifyUser:                
            users = self.getLocalUsers()
            users = [user for user in users if user["Name"] == userName]
            if not negativeScenario:
                if len(users) > 0:
                    self.succeed("Successfully verified Edited Local User :: '%s'"%(userName))
                else:
                    self.failure("Failed to verify Edited Local User :: '%s'"%(userName), raiseExc=True)
            else:
                if len(users) <= 0:
                    self.succeed("Successfully verified, Not able to Edit Local User :: '%s'"%(userName))
                else:
                    self.failure("Able to Edit Local User :: '%s'"%(userName), raiseExc=True)
        
    def deleteLocalUser(self, userName, verifyUser=True, negativeScenario=False):
        """
        Description: 
            Delete Local User
        """
        #Create User
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.clearSelection()
        globalVars.browserObject, status, result = pageObject.deleteUser(userName)
        if negativeScenario:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)   
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)   
        self.browserObject = globalVars.browserObject
        if verifyUser:               
            users = self.getLocalUsers(userName=userName)
            users = [user for user in users if user["Name"] == userName]
            if negativeScenario:
                if len(users) > 0:
                    self.succeed("Successfully verified Local User '%s', User is not Deleted"%userName)
                else:
                    self.failure("Failed to verify Deleted User '%s', User is Deleted"%userName, raiseExc=True)
            else:
                if len(users) <= 0:
                    self.succeed("Successfully verified Deleted Local User '%s'"%userName)
                else:
                    self.failure("Failed to verify Deleted User '%s'"%userName, raiseExc=True)
    
    def enableLocalUser(self, userName, verifyUser=True):
        """
        Description: 
            Enables Local User
        """
        #Create User
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.clearSelection()
        globalVars.browserObject, status, result = pageObject.enableUser(userName)
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)   
        self.browserObject = globalVars.browserObject
        #Verify User Enablement
        if verifyUser:                
            users = self.getLocalUsers(userName=userName)
            users = [user for user in users if user["Name"] == userName]
            self.succeed("Local User Info :: '%s'"%str(users))
            if len(users) > 0 and users[0]["State"] == "Enabled":
                self.succeed("Successfully verified Enabled Local User '%s'"%userName)
            else:
                self.failure("Failed to verify Enabled User '%s'"%userName, raiseExc=True)
    
    def disableLocalUser(self, userName, verifyUser=True, useEdit=False, currentPassword='admin'):
        """
        Description: 
            Disables Local User
        """
        #Create User
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.clearSelection()
        globalVars.browserObject, status, result = pageObject.disableUser(userName, useEdit, currentPassword)
        time.sleep(10)   
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)   
        self.browserObject = globalVars.browserObject
        #Verify User Disablement
        if verifyUser:                
            users = self.getLocalUsers(userName=userName)
            users = [user for user in users if user["Name"] == userName]
            self.succeed("Local User Info :: '%s'"%str(users))
            if len(users) > 0 and users[0]["State"] == "Disabled":
                self.succeed("Successfully verified Disabled Local User '%s'"%userName)
            else:
                self.failure("Failed to verify Disabled User '%s'"%userName, raiseExc=True)
    
    def verifyOptions(self, optionList=[], pageName="Settings", tabName = None, enableOptions=[]):
        """
        Description: 
            Verify Options under Settings are Enabled/Disabled
        Input:
            args includes Options which should be Disabled 
                Example: Under Settings Tab ("Getting Started")
        """
        optionDict = {}
        if pageName == "Settings":
            pageObject = LandingPage.LandingPage(self.browserObject)
            globalVars.browserObject, status, result = pageObject.validatePage()
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
            globalVars.browserObject, status, result = pageObject.verifySettingsTab()
            if status:
                optionDict = result
            else:
                self.failure(result, raiseExc=True)
            self.browserObject = globalVars.browserObject
        else:
            if pageName == "Logs":
                pageObject = Logs.Logs(self.browserObject)
            elif pageName == "Credentials":
                pageObject = Credentials.Credentials(self.browserObject)
            elif pageName == "Networks":
                pageObject = Networks.Networks(self.browserObject)
            elif pageName == "VirtualApplianceManagement":
                pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
            elif pageName == "BackupAndRestore":
                pageObject = BackupAndRestore.BAR(self.browserObject)
            elif pageName == "VirtualIdentityPools":
                pageObject = VirtualIdentityPools.VirtualIdentityPools(self.browserObject)
            elif pageName == "Templates":
                pageObject = Templates.Templates(self.browserObject)
            elif pageName == "Resources":
                pageObject = Resources.Resources(self.browserObject)
            elif pageName == "Services":
                pageObject = Services.Services(self.browserObject)
            elif pageName == "Dashboard":
                pageObject = Dashboard.Dashboard(self.browserObject)
            elif pageName == "Repositories":
                pageObject = Repositories.Repositories(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
            if tabName:
                globalVars.browserObject, status, result = pageObject.getOptions(tabName)
            else:
                globalVars.browserObject, status, result = pageObject.getOptions()
            if status:
                optionDict = result
            else:
                if "There should be atleast one" in result:
                    self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
                else:
                    self.failure(result, raiseExc=True)
            self.browserObject = globalVars.browserObject
        if len(optionDict) > 0:
            if tabName:
                self.succeed("Options under '%s' Page '%s' Tab %s"%(pageName, tabName, str(optionDict)))
            else:
                self.succeed("Options under '%s' Page %s"%(pageName, str(optionDict)))
        else:
            self.failure("No Options available under Page %s"%pageName, raiseExc=True)
        failList = [opt for opt in optionList if opt not in optionDict.keys()]
        if len(failList) > 0:
            self.failure("Some Options are not available to verify on page '%s' :: Faillist %s"%(pageName, str(failList)), raiseExc=True)
        if len(enableOptions) > 0:
            failList = [opt for opt in optionList if optionDict[opt] != "Enabled"]
            if len(failList) > 0:
                self.failure("Options which should be 'Enabled' on page '%s' are 'Disabled' :: Faillist %s"%(pageName, str(failList)), raiseExc=True)
        else:
            failList = [opt for opt in optionList if optionDict[opt] == "Enabled"]
            if len(failList) > 0:
                self.failure("Options which should be 'Disabled' on page '%s' are 'Enabled' :: Faillist %s"%(pageName, str(failList)), raiseExc=True)
                
    def checkTemplateName(self, templateNameJSON):
        """
        Description: 
            Check if the template name already exists. 
            If yes, rename the template name in JSON by appending a number - Template_2
            If no, will return the same name
        """
        tName = templateNameJSON
        
        #Getting the list of Templates from Appliance
        tempListAppliance = self.getTemplates(option="My Templates")
        tempNameList = [] #For List of Templates Names
        
        #Collecting all the Template Names from Dict-Data Structure
        for template in tempListAppliance:
            tempNameList.append(template["Name"])
        
        #Setting Count to 2. New name will be Template_2 and so on....
        count=2
        while tName in tempNameList:
            tName = templateNameJSON+"_"+str(count)
            count=count+1
        
        return tName
        
    def getTemplateOptions(self, actualOptions=[], disableOptions=[], enableOptions=[], templateName=None, 
                        templateType="My Templates", templateDetailOptions=True):
        """
        Description: 
            Verify Options under Templates page
        """
        optionDict = {}
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions(templateName=templateName, templateType=templateType,
                                                                         templateDetailOptions=templateDetailOptions)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if len(optionDict) > 0:
            self.succeed("Options under Templates Tab %s"%(str(optionDict)))
        else:
            self.failure("No Options available under Page 'Templates'", raiseExc=True)
        failList = [opt for opt in actualOptions if opt not in optionDict.keys()]
        if len(failList) > 0:
            self.failure("Some Options are not available to verify on page 'Templates' :: Faillist %s"%(str(failList)), raiseExc=True)            
        failList = [opt for opt in disableOptions if optionDict[opt] == "Enabled"]
        if len(failList) > 0:
            self.failure("Options which should be 'Disabled' on page 'Templates' are 'Enabled' :: Faillist %s"%(str(failList)), raiseExc=True)
        failList = [opt for opt in enableOptions if optionDict[opt] == "Disabled"]
        if len(failList) > 0:
            self.failure("Options which should be 'Enabled' on page 'Templates' are 'Disabled' :: Faillist %s"%(str(failList)), raiseExc=True)
    
    def getServiceOptions(self, actualOptions=[], disableOptions=[], enableOptions=[], serviceName=None, serviceDetailOptions=False):
        """
        Description: 
            Verify Options under Services page
        """
        optionDict = {}
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions(serviceName=serviceName, serviceDetailOptions=serviceDetailOptions)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if len(optionDict) > 0:
            self.succeed("Options under Services Page %s"%(str(optionDict)))
        else:
            self.failure("No Options available under Page 'Services'", raiseExc=True)
        failList = [opt for opt in actualOptions if opt not in optionDict.keys()]
        if len(failList) > 0:
            self.failure("Some Options are not available to verify on page 'Services' :: Faillist %s"%(str(failList)), raiseExc=True)            
        failList = [opt for opt in disableOptions if optionDict[opt] == "Enabled"]
        if len(failList) > 0:
            self.failure("Options which should be 'Disabled' on page 'Services' are 'Enabled' :: Faillist %s"%(str(failList)), raiseExc=True)
        failList = [opt for opt in enableOptions if optionDict[opt] == "Disabled"]
        if len(failList) > 0:
            self.failure("Options which should be 'Enabled' on page 'Services' are 'Disabled' :: Faillist %s"%(str(failList)), raiseExc=True)
    
    def getServiceDetails(self, serviceName=None):
        """
        Description: 
            Fetches Options under Service Detail Page
        """
        optionDict = {}
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions(serviceName=serviceName, serviceDetailOptions=True)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        return optionDict
        
    def getOptionDetails(self, option):
        """
        Description: 
            Fetches option Data by reading corresponding Table
        """
        if option == "Credentials":
            pageObject = Credentials.Credentials(self.browserObject)
        elif option == "Networks":
            pageObject = Networks.Networks(self.browserObject)
        elif option == "VirtualApplianceManagement":
            pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        elif option == "BackupAndRestore":
            pageObject = BackupAndRestore.BAR(self.browserObject)
        elif option == "Resources":
            pageObject = Resources.Resources(self.browserObject)
        elif option == "Templates":
            pageObject = Templates.Templates(self.browserObject)
        elif option == "Logs":
            pageObject = Logs.Logs(self.browserObject)
        elif option == "Dashboard":
            pageObject = Dashboard.Dashboard(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        if option == "Templates":
            globalVars.browserObject, status, result = pageObject.getDetails("My Templates", "All", "List")
        else:
            globalVars.browserObject, status, result = pageObject.getDetails()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)
    
    def getTemplates(self, option="Both", templateCategory="All", viewType="List", templateName=None):
        """
        Description:
            Fetches All Templates from Templates Page
        """
        tempList = []
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails(option, templateCategory, viewType)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if templateName:
            tempList = [temp for temp in result if temp["Name"] == templateName]
        else:
            tempList = result
        return tempList
    
    def getServices(self, serviceCategory="All", viewType="List", serviceName=None):
        """
        Description: 
            Fetches All Services from Services Page
        """

        svcList = []
        attempt = 5
        while attempt:
            pageObject = Services.Services(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if not status:
                self.failure("Failed to load Services page, retrying Result: %s".format(result))
                attempt -= 1
            else:
                break

            if not attempt:
                self.failure("Failed to load Services page, retries exhausted. Result: %s".format(result), raiseExc=True)

        globalVars.browserObject, status, result = pageObject.getServices(serviceCategory, viewType)
        if not status:
            self.failure(result, raiseExc=True)
        else:            
            svcList = result
            self.succeed("Able to fetch Services :: %s"%str(svcList))

        self.browserObject = globalVars.browserObject
        if serviceName:
            svcList = [temp for temp in result if temp["Name"] == serviceName]
        else:
            svcList = result
        return svcList
    
    def getRepositories(self, repoType="Firmware",repoName=None):
        """
        Description: 
            Fetches All Repositories from Repositories Page
        """
        repoList = []
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails(option="Firmware")
        if not status:
            self.failure(result, raiseExc=True)
        else:            
            repoList = result
            self.succeed("Able to fetch %s Repositories :: %s"%(repoType, str(repoList)))
        self.browserObject = globalVars.browserObject
        if repoName:
            repoList = [temp for temp in result if temp["Repository Name"] == repoName]
        else:
            repoList = result
        return repoList


    def getTemplateCategories(self):
        """
        Description: 
            Fetches Template categories from Templates Page
        """
        tcList = []
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getTemplateCategories()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            tcList = result
            #self.succeed("Able to fetch Template Categories Info :: %s"%(str(result)))
        self.browserObject = globalVars.browserObject
        return tcList
    
    def getFirmwareReposFromTemplate(self):
        """
        Description: 
            Fetches Firmware Repositories available while Template Creation
        """
        repoList = []
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)                     
        globalVars.browserObject, status, result = pageObject.createTemplate("Test Template", components="{}", 
                            managePermissions=False, manageFirmware=True, repositoryOnly=True) 
        if not status:
            self.failure(result, raiseExc=True)
        else:
            repoList = result
        self.browserObject = globalVars.browserObject
        return repoList
    
    def getFirmwareReposFromService(self, templateName):
        """
        Description: 
            Fetches Firmware Repositories available while deploying Service
        """
        repoList = []
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)     
        globalVars.browserObject, status, result = pageObject.deployService(templateName, "Test Service", 
                            managePermissions=False, manageFirmware=True, repositoryOnly=True)                                                                
        if not status:
            self.failure(result, raiseExc=True)
        else:
            repoList = result
        self.browserObject = globalVars.browserObject
        return repoList

    def getTemplateSettings(self, templateName, eachComponent=False):
        """
        Description: 
            Fetches Template Settings from Template Builder Page
        """
        tempSettings = {}
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        if eachComponent:
            globalVars.browserObject, status, result = pageObject.viewComponentDetails(templateName)
        else:
            globalVars.browserObject, status, result = pageObject.getTemplateSettings(templateName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            tempSettings = result
            #self.succeed("Able to fetch Template Settings Info :: %s"%(str(result)))
        self.browserObject = globalVars.browserObject
        return tempSettings
    
    def getServiceSettings(self, serviceName):
        """
        Description: 
            Fetches Service Deployment Settings from Template Builder Page
        """
        tempSettings = {}
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.viewDeploymentSettings(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            tempSettings = result
            #self.succeed("Able to fetch Template Settings Info :: %s"%(str(result)))
        self.browserObject = globalVars.browserObject
        return tempSettings
    
    def verifyTemplateSettings(self, tempSettings):
        """
        Description: 
            Verifies Template Settings
        """
        for key, value in tempSettings.items():
            if ("Server" or "Cluster" or "VM" or "Application") not in key:
                storageOptions = value.values()[0]                
                #Verifying Storage Name
                targetKey = [tkey for tkey in storageOptions.keys() if "Target" in tkey]
                if len(targetKey) > 0:
                    targetKey = targetKey[0]
                    if storageOptions[targetKey] == self.storageName:
                        self.succeed("Verified Storage Name :: Actual: '%s' Expected: '%s'"%(storageOptions[targetKey], 
                                                    self.storageName), "Verification")
                    else:
                        self.failure("Failed to Verify Storage Name :: Actual: '%s' Expected: '%s'"%(storageOptions[targetKey], 
                                                self.storageName), "Verification", raiseExc=True)
                #===============================================================
                # #Verifying Size
                # if storageOptions["Storage Size (e.g. 500MB, 1GB)"] == "10GB":
                #     self.succeed("Verified Storage Size :: Actual: '%s' Expected: '%s'"%(storageOptions["Storage Size (e.g. 500MB, 1GB)"], 
                #                                     "10GB"), "Verification")
                # else:
                #     self.failure("Failed to Verify Storage Size :: Actual: '%s' Expected: '%s'"%(storageOptions["Storage Size (e.g. 500MB, 1GB)"], 
                #                         "10GB"), "Verification", raiseExc=True)
                #===============================================================
    
    def getResourceTypes(self):
        """
        Description:
            Fetches Resource Types
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getResourceTypes()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    def getResources(self, resourceType='All', health='All'):
        """
        Description:
            Fetches Resources 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(15)
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getResources(resourceType, health)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    def getServerPools(self):
        """
        Description:
            Fetches Server Pools 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getServerPools()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    def verifyHAOption(self, userRole="Readonly"):
        """
        Description: 
            Verify Help and About Options on Landing Page
        Input:
            None
        """
        optionDict = {}
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyHelpAboutOption(userRole)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        self.succeed("Help/About Options %s"%str(optionDict))
        failList = [key for key, value in optionDict.items() if value == "Diaabled"]
        if len(failList) > 0:
            self.failure("Options %s which should be Enabled are Disabled"%",".join(failList), raiseExc=True)
        self.succeed("Verified Help/About Options for '%s' User"%userRole)
    
    def verifyUserOptions(self, optionList, userRole='Read only', optionTab="Users"):
        """
        Description: 
            Verify Options under Users
        Input:
            None
        """
        optionDict = {}
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions(globalVars.readOnlyUser, optionTab=optionTab)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        failStatus = [key for key, value in optionDict.items() if key in optionList and value != "Disabled"]
        if len(failStatus) > 0:
            self.failure("Some User Management options are 'Enabled' for '%s' user :: %s"%(str(optionDict), userRole), raiseExc=True)
        else:
            self.succeed("All User Options %s are 'Disabled' for '%s' user :: %s"%(str(optionList), userRole, str(optionDict)))
        self.browserObject = globalVars.browserObject
    
    def verifySettingsTab(self):
        """
        Description: 
            Verify whether User can navigate to Settings Tab 
        """
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifySettingsTab()        
        if status:
            self.succeed("Successfully verified Options under Settings Tab :: Available Options :%s"%str(result))
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def getSettingsOptions(self):
        """
        Description: 
            Verify Options on Settings Tab 
        """
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifySettingsTab()        
        if not status:
            self.failure(result, raiseExc=True)        
        self.browserObject = globalVars.browserObject
        if result.has_key("Initial Appliance Setup"):
            result.pop("Initial Appliance Setup")
        self.succeed("Available Options under Settings Tab :: %s"%str(result))
        return result
    
    def verifyLandingPageOptions(self, userRole="Administrator"):
        """
        Description: 
            Verify Options on the Landing Page 
        """
        optionDict = {}
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyLandingPageOptions()
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if userRole == "Standard":   
            if optionDict["Settings"] != "Disabled":
                self.failure("Settings Page is 'Enabled' on Landing Page for '%s' UserRole %s"%(userRole, str(optionDict)), raiseExc=True)
            else:
                self.succeed("Settings Page is 'Disabled' on Landing Page for '%s' UserRole %s"%(userRole, str(optionDict)))
        else:
            utility.execLog("Lenght of menu for userRole :%s"%str(len(optionDict)))
            failStatus = [key for key, value in optionDict.items() if value == "Disabled"]
            if len(failStatus) > 0:
                self.failure("Some options are 'Disabled' on Landing Page for '%s' UserRole %s"%(userRole, str(optionDict)), raiseExc=True)
            else:
                self.succeed("Successfully verified Options on Landing Page for '%s' UserRole %s"%(userRole, str(optionDict)))
    
    def verifyPageLaunch(self, pageTitle="Dashboard", userRole="Read only"):
        """
        Description: 
            Verify Page Title on the Landing Page 
        """
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.validatePageTitle(pageTitle)
        if status:
            self.succeed(result)
            self.succeed("'%s' user launched to '%s' Page"%(userRole, pageTitle))
        else:
            self.failure(result, raiseExc=True)
    
    def verifyGettingStarted(self, userRole="Read only", verifyPageLaunch=True):
        """
        Description: 
            Verify Options on the Landing Page 
        """
        optionDict = {}
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        if verifyPageLaunch:
            globalVars.browserObject, status, result = pageObject.validatePageTitle("Dashboard")
            if status:
                self.succeed(result)
                self.succeed("'%s' user launched to Dashboard Page"%userRole)
            else:
                self.failure(result, raiseExc=True)
        if userRole == "Standard":
            globalVars.browserObject, status, result = pageObject.verifyLandingPageOptions()
            if status:
                optionDict = result
            else:
                self.failure(result, raiseExc=True)
            if optionDict["Settings"] == "Disabled":
                self.succeed("'Getting Started' option is Disabled for '%s' user and is unable to select any steps Initial Setup, Define Networks, Discover Resources and View and Publish Templates"%userRole)
            else:
                self.failure("'Getting Started' option is Enabled for '%s' user is Able to select steps Initial Setup, Define Networks, Discover Resources and View and Publish Templates"%userRole, raiseExc=True)
        else:
            globalVars.browserObject, status, result = pageObject.verifyGettingStartedOption()
            if not status:
                self.failure(result, raiseExc=True)
            if result == "Disabled":
                self.succeed("'Getting Started' option is not Visible on Menu for '%s' user and is unable to select any steps Initial Setup, Define Networks, Discover Resources and View and Publish Templates"%userRole)
            else:
                self.failure("'Getting Started' option is Visible on Menu for '%s' user is Able to select steps Initial Setup, Define Networks, Discover Resources and View and Publish Templates"%userRole, raiseExc=True)
            globalVars.browserObject, status, result = pageObject.verifySettingsTab()
            if status:
                optionDict = result
            else:
                self.failure(result, raiseExc=True)
            if optionDict["Getting Started"] == "Disabled":
                self.succeed("'Getting Started' option is Disabled for '%s' user and is unable to select any steps Initial Setup, Define Networks, Discover Resources and View and Publish Templates"%userRole)
            else:
                self.failure("'Getting Started' option is Enabled for '%s' user is Able to select steps Initial Setup, Define Networks, Discover Resources and View and Publish Templates"%userRole, raiseExc=True)
        self.browserObject = globalVars.browserObject   
    
    def setupNetworks(self):
        """
        Description: 
            Create a Network 
        """
        networkConfig = utility.readExcel(globalVars.inputFile, "Network")
        if not networkConfig:
            return self.browserObject, False, networkConfig
        networkDict = {}
        pageObject = Networks.Networks(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails()
        if status:
            networkDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        failList = []
        for network in networkConfig:
            self.browserObject.refresh()
            time.sleep(5)
            found = []
            if network["VLANID"] == "":
                continue
            found = [nw for nw in networkDict if nw["Name"].lower() == network["Name"].lower()]
            if network["Static"] == "true":
                configureStatic = True
            else:
                configureStatic = False
            if configureStatic:
                if network["StartIP"] == "" or network["EndIP"] == "" or network["Subnet"] == "" or network["Gateway"] == "":
                    self.failure("Some of the Network Information is not provided :: %s"%str(network))
                    failList.append(network["Name"] + "-" + network["VLANID"])
                    continue
            if len(found) > 0:
                globalVars.browserObject, status, result = pageObject.defineNetwork(network["Name"], network["Description"],
                        network["Type"], network["VLANID"], configureStatic, network["Subnet"], network["Gateway"], network["PrimaryDNS"], 
                        network["SecondaryDNS"], network["DNSSuffix"], network["StartIP"], network["EndIP"], editStatus=True)
            else:
                globalVars.browserObject, status, result = pageObject.defineNetwork(network["Name"], network["Description"],
                        network["Type"], network["VLANID"], configureStatic, network["Subnet"], network["Gateway"], network["PrimaryDNS"], 
                        network["SecondaryDNS"], network["DNSSuffix"], network["StartIP"], network["EndIP"])
            if not status:
                self.failure("Failed to define Network %s :: %s"%(network, result))                
                failList.append(network["Name"] + "-" + network["VLANID"])
            else:
                self.succeed("Successfully defined Network :: %s"%network)       
        if len(failList) == 0:
            self.succeed("Successfully defined all Networks provided :: %s"%(networkConfig))
        else:
            self.failure("Failed to set up some of the networks :: %s"%(failList), raiseExc=True)
    
    def deleteNetworks(self, networkName):
        """
        Description: 
            Create a Network 
        """
        pageObject = Networks.Networks(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteNetwork(networkName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def setupCredentials(self):
        """
        Description: 
            Create a Credential 
        """
        credentialConfig = utility.readExcel(globalVars.inputFile, "Credential")
        if not credentialConfig:
            return self.browserObject, False, credentialConfig
        credentialDict = {}
        pageObject = Credentials.Credentials(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails()
        if status:
            credentialDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        failList = []
        for credential in credentialConfig:
            found = []
            if credential["Type"] == "" or credential["Name"] == "" or credential["Username"] == "" or credential["Password"] == "":
                self.failure("Some of the Credential Information is not provided :: %s"%str(credential))
            found = [nw for nw in credentialDict if nw["Name"].lower() == credential["Name"].lower()]
            if len(found) > 0:
                globalVars.browserObject, status, result = pageObject.createCredential(credential["Name"], credential["Type"],
                        credential["Username"], credential["Password"], credential["Domain"], credential["SNMP"], editStatus=True)
            else:
                globalVars.browserObject, status, result = pageObject.createCredential(credential["Name"], credential["Type"],
                        credential["Username"], credential["Password"], credential["Domain"], credential["SNMP"])
            if not status:
                self.failure("Failed to create Credential %s :: %s"%(credential, result))                
                failList.append(credential["Name"] + "-" + credential["Username"])
            else:
                self.succeed("Successfully created Credential :: %s"%credential)
        if len(failList) == 0:
            self.succeed("Successfully created all Credentials provided :: %s"%(credentialConfig))
        else:
            self.failure("Failed to create some of the credentials :: %s"%(failList), raiseExc=True)
    
    def createCredential(self, credentialName, credentialType, credentialUser, credentialPwd):
        """
        Description: 
            Create a Credential 
        """
        pageObject = Credentials.Credentials(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.createCredential(credentialName, credentialType, 
                                        credentialUser, credentialPwd)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        self.browserObject = globalVars.browserObject
    
    def editCredential(self, credentialName, credentialType, credentialUser, credentialPwd):
        """
        Description: 
            Edits a Credential 
        """
        pageObject = Credentials.Credentials(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.createCredential(credentialName, credentialType, 
                                        credentialUser, credentialPwd, editStatus=True)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        self.browserObject = globalVars.browserObject
                    
    def deleteCredential(self, credentialName, ignoreFailure=False):
        """
        Description: 
            Create a Network 
        """
        pageObject = Credentials.Credentials(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteCredential(credentialName)
        if not status and not ignoreFailure:
            self.failure(result, raiseExc=True)
        self.succeed(result)
        self.browserObject = globalVars.browserObject
    
    def getCredentials(self, credentialName=None):
        """
        Description: 
            Gets Credentials 
        """
        creList = []
        pageObject = Credentials.Credentials(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True) 
        globalVars.browserObject, status, result = pageObject.getDetails()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            creList = result
        self.browserObject = globalVars.browserObject
        if credentialName:
            creList = [temp for temp in result if temp["Name"] == credentialName]
        else:
            creList = result
        return creList 
    
    def getCredentialOptions(self, credentialName):
        """
        Description: 
            Verify Options for particular Credential
        """
        optionDict = {}
        pageObject = Credentials.Credentials(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions(credentialName)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return optionDict
    
    def discoverResources(self):
        """
        Description:
            Discover Resources
        """
        resourceConfig = utility.readExcel(globalVars.inputFile, "Discovery")
        if not resourceConfig:
            return self.browserObject, False, resourceConfig
        #resourceDict = {}
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        resourceDict = self.getResources()
        #globalVars.browserObject, status, resourceDict = pageObject.getResources(resourceType='All')
        #if status:
        #    resourceDict = result
        #else:
        #    self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        failList = []
        serverCredential = storageCredential = vcenterCredential = switchCredential = chassisCredential = scvmmCredential = emCredential = serverPool = ""
        for resource in resourceConfig:
            if resource["Type"] == "" or resource["StartIP"] == "" or (resource["ServerCre"] == "" and  
                        resource["ChassisCre"] == "" and resource["SwitchCre"] == "" and resource["VCenterCre"] == ""
                        and resource["StorageCre"] == "" and resource["EMCre"] == "" and resource["SCVMMCre"]==""):
                self.failure("Some of the Resource Information is not provided :: %s"%str(resource))
            #found = [rs for rs in resourceDict if rs["Resource Name"].lower() == resource["StartIP"].lower()]
            manageResource = "Managed"
            if resource["Managed"] == "false":
                manageResource = "Unmanaged"
            else:
                manageResource = "Managed"
            if resource["Type"] == "Server":
                serverCredential = resource["ServerCre"]
            elif resource["Type"] == "Storage":
                storageCredential = resource["StorageCre"]
            elif resource["Type"] == "Chassis":
                chassisCredential = resource["ChassisCre"]
                serverCredential = resource["ServerCre"]
                switchCredential = resource["SwitchCre"]
            elif resource["Type"] == "vCenter":
                vcenterCredential = resource["VCenterCre"]
            elif resource["Type"] == "Switch":
                switchCredential = resource["SwitchCre"]
            elif resource["Type"] == "SCVMM":
                scvmmCredential = resource["SCVMMCre"]
            elif resource["Type"] == "Element Manager":
                emCredential = resource["EMCre"]
            if resource["ServerPool"] != "" and (resource["Type"]== "Server" or resource["Type"] == "Chassis"):
                serverPool = resource["ServerPool"]
            else:
                serverPool = ""
            #===================================================================
            # if len(found) > 0:
            #     globalVars.browserObject, status, result = pageObject.createCredential(credential["Name"], credential["Type"],
            #             credential["Username"], credential["Password"], credential["Protocol"], credential["SNMP"], editStatus=True)
            # else:
            #===================================================================
            globalVars.browserObject, status, result = pageObject.discoverResource(resource["Type"], resource["StartIP"],
                        resource["EndIP"], manageResource, serverCredential, storageCredential, chassisCredential, vcenterCredential,
                        switchCredential, scvmmCredential, emCredential, editStatus=False, resourcePool=serverPool)
            if not status:
                self.failure("Failed to initiate Resource Discovery %s :: %s"%(resource, result))
                failList.append(resource["Type"] + "-" + resource["StartIP"] + ":" + resource["EndIP"])
            else:
                self.succeed("Successfully initiated Resource Discovery :: %s"%resource)
        if len(failList) == 0:
            self.succeed("Successfully initiated Discovery for all Resources :: %s"%(resourceConfig))
        else:
            self.failure("Failed to initiate Discovery for some of the Resources :: %s"%(failList), raiseExc=True)
    
    def getBundleInformation(self, repositoryName):
        """
        Description: 
            Get Bundle Information 
        """
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        repoList = []
        if repositoryName == "All":
            globalVars.browserObject, status, repoInfo = pageObject.getDetails(option="Firmware")
            if status:
                repoList = [repo["Repository Name"] for repo in repoInfo]
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            repoList.append(repositoryName)
        for repository in repoList:
            globalVars.browserObject, status, result = pageObject.readBundleInformation(repository)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def runInventory(self, resourceName, negativeScenario=False, waitForCompletion=False):
        """
        Description: 
            Run Inventory on the specified Resource. Updated the code to work for multiple resources selected for TC-4650,4651 & 4652.
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        if isinstance(resourceName, list):
            globalVars.browserObject, status, result = pageObject.runInventory(resourceName, waitForCompletion)
        else:
            globalVars.browserObject, status, result = pageObject.runInventory([resourceName], waitForCompletion)
        self.browserObject = globalVars.browserObject    
        if negativeScenario:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
    
    # Added Parameter volumeSize for NGI-TC-2637
    # Added Parameter manageFirmware for NGI-TC-3278
    def createTemplate(self, templateName, components=None, publishTemplate=False, managePermissions=True, 
            userList=["All"], negativeScenario=False, passExpectation=False, templateType="New", 
            cloneTemplateName=None,volumeName="autoVolume", volumeSize="10GB", repositoryOnly=False, repositoryName=None, manageFirmware= False):
        """
        Description: 
            Creates Template
        """
        #Create User
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)                     
        globalVars.browserObject, status, result = pageObject.createTemplate(templateName, components, managePermissions=managePermissions, 
                                userList=userList, publishTemplate=publishTemplate, passExpectation=passExpectation, repositoryOnly=repositoryOnly, repositoryName=repositoryName, manageFirmware=manageFirmware)
        if not negativeScenario:
            if status:
                self.succeed(result)
                return result
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.failure(result, raiseExc=True)
            else:
                if "Failed to Select User" in result or "The storage volume name is already in use" in result:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        
    def createTemplateBasic(self, templateName, storageName, publishTemplate=False, manageFirmware=False,
                       managePermissions=True, userList=["All"], negativeScenario=False, passExpectation=False, volumeName="autoVolume", volumeSize="10GB"):
        """
        Description: 
            Creates Basic Template with single storage
        """
        #Create User
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.createTemplateBasic(templateName, storageName, manageFirmware=manageFirmware, managePermissions=managePermissions, 
                                userList=userList, publishTemplate=publishTemplate, passExpectation=passExpectation, volumeName=volumeName, volumeSize=volumeSize)
        if not negativeScenario:
            if (not status) or status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.failure(result, raiseExc=True)
            else:
                if "Failed to Select User" in result:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject 
    
    def getUsersFromCreateTemplate(self, templateName, components):
        """
        Description: 
            Get Users while Creating Template
        """
        #Create User
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.createTemplate(templateName, components, managePermissions=True, userList=["All"], publishTemplate=False, usersOnly=True)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed("Fetched Users Information while Creating Template '%s'"%str(result))
        return result
        self.browserObject = globalVars.browserObject
    
    def getUsersFromDeployService(self, templateName, serviceName):
        """
        Description: 
            Get Users while Deploying Service
        """
        #Create User
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.deployService(templateName, serviceName, managePermissions=True, usersOnly=True)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed("Fetched Users Information while Deploying Service '%s'"%str(result))
        return result
        self.browserObject = globalVars.browserObject
    
    def deleteResource(self, resourceStartIP):
        """
        Description: 
            Deletes a Resource
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteResource(resourceStartIP)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return status, result
    
    def findResource(self, resourceToFind):
        """
        Description: 
            Finds a Resource
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.findResource(resourceToFind, False)
        self.browserObject = globalVars.browserObject
        return status, result

    def deleteTemplate(self, templateName):
        """
        Description: 
            Deletes a Template 
        """
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteTemplate(templateName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def deleteService(self, serviceName):
        """
        Description: 
            Deletes a Service 
        """
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteService(serviceName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def deleteRepository(self, repositoryName, repositoryType="Firmware", negativeScenario=False):
        """
        Description: 
            Deletes a Repository 
        """
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteRepository(repositoryName, repositoryType)
        if negativeScenario:
            if not status:
                self.succeed("Unable to Delete Repository '%s' :: %s"%(repositoryName, result))
            else:
                self.failure("Able to Delete Repository '%s' :: %s"%(repositoryName, result), raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def setDefaultRepository(self, repositoryName, negativeScenario=False):
        """
        Description: 
            Sets as Default Repository 
        """
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.setDefaultRepository(repositoryName)
        if negativeScenario:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def getDefaultRepository(self):
        """
        Description: 
            Gets Default Repository 
        """
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDefaultRepository()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed("Successfully fetched Default Repository :: %s"%result)
        return result
    
    def addRepository(self, addType, repoPath="", repoUser="", repoPassword="", defaultRepository=True, 
                    onlyTestConnection=False, negativeScenario=False):
        """
        Description: 
            Adds a Repository 
        """
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.addRepository(addType, repoPath, repoUser, 
                                        repoPassword, defaultRepository, onlyTestConnection)
        if negativeScenario:
            if not status:
                self.succeed("Unable to Add Repository :: %s"%(result))
            else:
                self.failure("Able to Add Repository :: %s"%(result), raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def editService(self, serviceName, managePermissions=True, userList=["All"], manageFirmware=False, 
                deleteUsers=False, firmwareName=None, testNegative=False):
        """
        Description:
            Edits a Service
        """
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.editService(serviceName, managePermissions=managePermissions,
                userList=userList, manageFirmware=manageFirmware, deleteUsers=deleteUsers, firmwareName=firmwareName)
        if not testNegative:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
            self.browserObject = globalVars.browserObject
        else:
            if status:
                self.failure(result, raiseExc=True)
            else:
                self.succeed(result)
    
    def editTemplate(self, templateName, managePermissions=True, userList=["All"], manageFirmware=False, 
                deleteUsers=False, firmwareName=None):
        """
        Description: 
            Edits a Template 
        """
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.editTemplate(templateName, managePermissions=managePermissions, 
                userList=userList, manageFirmware=manageFirmware, deleteUsers=deleteUsers, firmwareName=firmwareName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def getTemplateUsers(self, templateName):
        """
        Description: 
            Gets Users of the Template 
        """
        userList = []
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.editTemplate(templateName, usersOnly=True)
        if status:
            self.succeed("Template Users :: '%s'"%str(result))
            userList = result
        else:
            self.failure(result, raiseExc=True)
        return userList
    
    def getServiceUsers(self, serviceName):
        """
        Description: 
            Gets Users of the Service 
        """
        userList = []
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.editService(serviceName, usersOnly=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Service Users :: '%s'"%str(result))
            userList = result
        else:
            self.failure(result, raiseExc=True)
        return userList
    
    def getDeploymentStatus(self, serviceName, deleteStatus=False, timeout=600, waitTime=120):
        """
        Description: 
            Waits for Deployment to Complete and Returns Status 
        """
        result = []
        iterations = timeout/waitTime + 1
        loopCount = timeout/waitTime
        startTime = datetime.datetime.now()
        while loopCount:
            self.succeed("Waiting for {} Deployment Status, Iteration:{}".format(serviceName, iterations-loopCount))
            serviceList = self.getServices(serviceName=serviceName)
            if deleteStatus and len(serviceList) == 0:
                result.append({"Status":"Deleted"})
                break
            elif len(serviceList) > 0 and serviceList[0]["Status"] != "Inprogress":
                result = serviceList
                break

#            utility.execLog("wating for service status counter: {}".format(loopCount))
            self.browserObject.refresh()
            time.sleep(waitTime)
            loopCount = loopCount - 1

        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.succeed("Time taken for Deployment :: %s"%elapsedTime)
        return result

    def getRepositoryStatus(self, repoName, repoType="Firmware", deleteStatus=False):
        """
        Description: 
            Waits for Repository Addition to Complete and Returns Status 
        """
        result = []
        loopCount = 90
        startTime = datetime.datetime.now()
        while loopCount:
            self.succeed("Waiting for Repository Status, Iteration:'%s'"%str(61-loopCount))
            repoList = self.getRepositories(repoType, repoName)
            if deleteStatus:
                if len(repoList) == 0:
                    result.append({"State":"Deleted"})
                    break
                else:
                    time.sleep(120)
                    result = repoList
                    loopCount = loopCount - 1
                    continue
            else:
                if len(repoList) > 0 and repoList[0]["State"] != "Copying":
                    result = repoList
                    break
                else:
                    utility.execLog("waiting for service status counter: %i"%loopCount)
                    self.browserObject.refresh()
                    time.sleep(120)
                    result = repoList
                    loopCount = loopCount - 1
                    continue              
        if loopCount == 0:
            if result[0]["State"] == "Copying":
                self.failure("Adding Repository is taking longer than Expected..Waited more than 2:30 hrs for completion :: %s"%result,
                             raiseExc=True)               
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.succeed("Time taken to Add Repository is :: %s"%elapsedTime)
        return result
    
    def getAttributes(self, element):        
        """
        Gets Attributes of an element
        """
        javaScript = r"var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;"
        return self.browserObject.execute_script(javaScript, element)
    
    # Added Parameter volumeSize for NGI-TC-2637
    def createSampleTemplate(self, templateName="Test Template", publishedTemplate=True, deleteAndCreate=False,
                             userList=['All'], negativeScenario=False, passExpectation=False, volumeName="autoVolume",
                             managePermissions=True):
        """
        Creates Sample Template to verify RBAC Options
        """
        #Get Templates
        templateExists = False
        templatePublished = False
        tempList = self.getTemplates(option="My Templates")
        self.storageName = ""        
        for temp in tempList:
            if temp["Name"] == templateName:
                templateExists = True
                if temp["State"] == "Published":
                    templatePublished = True
        #volumeName = "autoVolume" + str(datetime.datetime.now().second)
        deleteAndCreate = True
        if deleteAndCreate:
            if templateExists:                
                if self.currentUser == self.loginUser:
                    self.succeed("Template with Name '%s' already exists so Deleting '%s'"%(templateName, templateName))            
                    self.deleteTemplate(templateName)
            #Get Resources
            resList = self.getResources("Storage")
            time.sleep(5)
            if len(resList) > 0:
                self.succeed("Successfully fetched Storage Resources, Values :: %s"%(str(resList)))
            else:
                self.failure("No Resources of Type 'Storage'", resultCode=BaseClass.OMITTED, raiseExc=True)
            for res in resList:
                if "EqualLogic" in res["Manufacturer /Model"]:
                    self.storageName = res["Resource Name"]
            #self.runInventory(self.storageName)
            #time.sleep(15)
            utility.execLog("Volume name :%s"%volumeName)
            date = datetime.datetime.now().strftime('%y%m%d%H%M%S') 
            volumeName = volumeName  +str(date)[6:]
            utility.execLog("Volume name :%s"%volumeName)
            components='{"Storage1":{"Type":"EqualLogic", "Name":"%s", "VolumeName":"%s", "Size":"10GB","AuthType":"IQN/IP","AuthUser":"grpadmin","AuthPwd":"dell1234","IQNIP":"172.31.64.241"}}'%(self.storageName, volumeName)
            #Create Template
            self.createTemplate(templateName, components, publishTemplate=publishedTemplate, userList=userList,
                                negativeScenario=negativeScenario, passExpectation=passExpectation,
                                managePermissions=managePermissions)
        else:
            if not templatePublished and templateExists:
                if self.currentUser == self.loginUser:
                    self.succeed("Template with Name '%s' already exists and not Published so Deleting"%(templateName))
                    self.deleteTemplate(templateName)
            if templatePublished and templateExists:
                self.succeed("Published Template with Name '%s' already exists so not creating New Template"%templateName)
            else:
                #Get Resources
                resList = self.getResources("Storage")
                time.sleep(5)
                if len(resList) > 0:
                    self.succeed("Successfully fetched Storage Resources, Values :: %s"%(str(resList)))
                else:
                    self.failure("No Resources of Type 'Storage'", resultCode=BaseClass.OMITTED, raiseExc=True)
                for res in resList:
                    if "EqualLogic" in res["Manufacturer /Model"]:
                        self.storageName = res["Resource Name"]
                #self.runInventory(self.storageName)
                #time.sleep(15)
                date = datetime.datetime.now().strftime('%y%m%d%H%M%S') 
                volumeName = volumeName  +str(date)[6:]
                utility.execLog("Volume name :%s"%volumeName)
                components='{"Storage1":{"Type":"EqualLogic", "Name":"%s", "VolumeName":"%s", "Size":"10GB","AuthType":"IQN/IP","AuthUser":"grpadmin","AuthPwd":"dell1234","IQNIP":"172.31.64.241"}}'%(self.storageName, volumeName)
                #Create Template
                self.createTemplate(templateName, components, publishTemplate=publishedTemplate, userList=userList,
                                negativeScenario=negativeScenario,passExpectation=passExpectation,
                                managePermissions=managePermissions)

    def deployService(self, templateName, serviceName="Test Service", managePermissions=True, repositoryName=None, manageFirmware=False, userList=["All"], 
                      negativeScenario=False, passExpectation=False, volumeName=None, staticIP=False, manualServer=False, retryOnFailure="false",checkinValidServerList=False, repositoryOnly=False, getSelectedRepo=False,deployNow=False,scheduleDeploymentWait=None, deployPast=False):
        """
        Description: 
            Deploys a Template
        """
        
        if not volumeName:
            if serviceName == "Test Service":
                volumeName="autoVolume"
            else:
                #volumeName = "autoStandardVolume"
                volumeName = None
                
        #For Static IP from globalVars.py
        #self.staticIP=False     
        networks=None
        if staticIP:
            self.staticIP=True
            networks=self.getNetworks()
            
        #For Manual Server Entry
        if manualServer:
            self.manualServer=True
                
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)

        globalVars.browserObject, status, result = pageObject.deployService(templateName, serviceName, managePermissions=managePermissions,
                    repositoryName=repositoryName,  manageFirmware=manageFirmware, userList=userList, passExpectation=passExpectation,
                    volumeName=volumeName, staticIP=staticIP, networks=networks, manualServer=manualServer, retryOnFailure=retryOnFailure,checkinValidServerList=checkinValidServerList, repositoryOnly=repositoryOnly, getSelectedRepo=getSelectedRepo, deployNow=deployNow, scheduleDeploymentWait=scheduleDeploymentWait,deployPast=deployPast)             

        if not negativeScenario:
            if status:
                utility.execLog("Success")
                self.succeed(result)
                return result
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.failure(result, raiseExc=True)
            else:
                if "Failed to Select User" in result:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)        
        self.browserObject = globalVars.browserObject
        
    def deployServiceBasic(self, templateName, serviceName="Test Service", managePermissions=True, userList=["All"], 
                      negativeScenario=False, deploymentScheduleTime=None, passExpectation=False, deployStatus=False,deployNow=True,volumeName=""):
        """
        Description: 
            Deploys a Template
        """
        #Create User
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.deployServiceBasic(templateName, serviceName, managePermissions=managePermissions, 
                                                                    userList=userList,deploymentScheduleTime=deploymentScheduleTime, passExpectation=passExpectation,deployNow=deployNow,volumeName=volumeName)
        if deployStatus:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if not negativeScenario:
                if status:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
            else:
                if status:
                    self.failure(result, raiseExc=True)
                else:
                    if "Failed to Select User" in result:
                        self.succeed(result)
                    else:
                        self.failure(result, raiseExc=True)        
        self.browserObject = globalVars.browserObject
    
    # Added Parameter verifySuccess for NGI-TC-2637
    
    def deploySampleService(self, templateName, serviceName="Test Service", managePermissions=True, userRole="Administrator", 
                      negativeScenario=False, passExpectation=False, userList=['All'], loginAsUser=True, verifySuccess = True):
        """
        Description: 
            Deploys a Template if Service does not exist
        """
        serviceList = self.getServices(serviceName=serviceName)
        if len(serviceList) > 0:
            if userRole != "Administrator":
                #Check for User Permissions
                pageObject = Services.Services(self.browserObject)
                globalVars.browserObject, status, result = pageObject.loadPage()
                if not status:
                    self.failure(result, raiseExc=True)             
                globalVars.browserObject, status, result = pageObject.getOptions(serviceName=serviceName, serviceDetailOptions=True)
                if not status:
                    self.failure(result, raiseExc=True)
                if result["User Permissions"] != "Enabled":
                    #Deletes Service
                    self.deleteService(serviceName)
                    #Wait for Deployment to complete
                    result = self.getDeploymentStatus(serviceName, deleteStatus=True)
                    if len(result) > 0:
                        if result[0]["Status"] == "Deleted":
                            self.succeed("Successfully Deleted Service '%s'"%serviceName)
                        else:
                            self.failure("Failed to Delete Service '%s'"%serviceName, raiseExc=True)
                    else:
                        self.failure("Failed to Delete Service '%s' in expected time"%serviceName, raiseExc=True)
                    if loginAsUser:
                        #Check for current logged in user
                        self.verifyCurrentUser(userRole=userRole, loginAsUser=loginAsUser)
                    #Initiate Deployment
                    self.deployService(templateName, serviceName, managePermissions, negativeScenario, passExpectation, userList=userList)
                    #Wait for Deployment to complete
                    result = self.getDeploymentStatus(serviceName)
                    if len(result) > 0:
                        if result[0]["Status"] in ("Success"):
                            self.succeed("Successfully Deployed Service '%s'"%serviceName)
                        else:
                            self.failure("Failed to Deploy Service '%s'"%serviceName, raiseExc=True)
                    else:
                        self.failure("Failed to Deploy Service '%s' in expected time"%serviceName, raiseExc=True)
        else:
            if loginAsUser:
                #Check for current logged in user
                self.verifyCurrentUser(userRole=userRole, loginAsUser=loginAsUser)
            #Initiate Deployment
            self.deployService(templateName, serviceName, managePermissions, negativeScenario, passExpectation, userList=userList)
            #Wait for Deployment to complete
            result = self.getDeploymentStatus(serviceName)
            utility.execLog("Result returned is => %s"%str(result))
            if len(result) > 0:
                if not verifySuccess:
                    if result[0]["Status"] in ("Success", "Unknown"):
                        self.succeed("Successfully Deployed Service '%s'"%serviceName)
                    else:
                        self.failure("Failed to Deploy Service '%s'"%serviceName, raiseExc=True)
                else:
                    if result[0]["Name"] in serviceName:
                        self.succeed("Successfully Deployed Service '%s'"%serviceName)
                    else:
                        self.failure("Failed to Deploy Service '%s'"%serviceName, raiseExc=True)
            else:
                self.failure("Failed to Deploy Service '%s' in expected time"%serviceName, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
#     def deploySampleService(self, templateName, serviceName="Test Service", managePermissions=True, userRole="Administrator", 
#                       negativeScenario=False, passExpectation=False, userList=['All'], loginAsUser=True):
#         """
#         Description: 
#             Deploys a Template if Service does not exist
#         """
#         serviceList = self.getServices(serviceName=serviceName)
#         if serviceName == "Test Service":
#             volumeName="autoVolume"
#         else:
#             volumeName = "autoStandardVolume"
#         if len(serviceList) > 0:
#             if userRole != "Administrator":
#                 #Check for User Permissions
#                 pageObject = Services.Services(self.browserObject)
#                 globalVars.browserObject, status, result = pageObject.loadPage()
#                 if not status:
#                     self.failure(result, raiseExc=True)             
#                 globalVars.browserObject, status, result = pageObject.getOptions(serviceName=serviceName, serviceDetailOptions=True)
#                 if not status:
#                     self.failure(result, raiseExc=True)
#                 if result["User Permissions"] != "Enabled":
#                     #Deletes Service
#                     self.deleteService(serviceName)
#                     #Wait for Deployment to complete
#                     result = self.getDeploymentStatus(serviceName, deleteStatus=True)
#                     if len(result) > 0:
#                         if result[0]["Status"] == "Deleted":
#                             self.succeed("Successfully Deleted Service '%s'"%serviceName)
#                         else:
#                             self.failure("Failed to Delete Service '%s'"%serviceName, raiseExc=True)
#                     else:
#                         self.failure("Failed to Delete Service '%s' in expected time"%serviceName, raiseExc=True)
#                     if loginAsUser:
#                         #Check for current logged in user
#                         self.verifyCurrentUser(userRole=userRole, loginAsUser=loginAsUser)
#                     #Initiate Deployment
#                     self.deployService(templateName, serviceName, managePermissions, userList, negativeScenario, passExpectation, volumeName)
#                     #Wait for Deployment to complete
#                     result = self.getDeploymentStatus(serviceName)
#                     if len(result) > 0:
#                         if result[0]["Status"] in ("Success"):
#                             self.succeed("Successfully Deployed Service '%s'"%serviceName)
#                         else:
#                             self.failure("Failed to Deploy Service '%s'"%serviceName, raiseExc=True)
#                     else:
#                         self.failure("Failed to Deploy Service '%s' in expected time"%serviceName, raiseExc=True)
#         else:
#             if loginAsUser:
#                 #Check for current logged in user
#                 self.verifyCurrentUser(userRole=userRole, loginAsUser=loginAsUser)
#             #Initiate Deployment
#             self.deployService(templateName, serviceName, managePermissions, userList, negativeScenario, passExpectation, volumeName)
#             #Wait for Deployment to complete
#             result = self.getDeploymentStatus(serviceName)
#             if len(result) > 0:
#                 if result[0]["Status"] in ("Success"):
#                     self.succeed("Successfully Deployed Service '%s'"%serviceName)
#                 else:
#                     self.failure("Failed to Deploy Service '%s'"%serviceName, raiseExc=True)
#             else:
#                 self.failure("Failed to Deploy Service '%s' in expected time"%serviceName, raiseExc=True)
#         self.browserObject = globalVars.browserObject

########################## HCL ########################################################################################################################
    def getSection_Server_Pool(self):
        """
        Description: 
            Calling Dashboard page
        """
        utility.execLog("Before function")
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        pageObject.getSection_ServerPool()
        self.browserObject = globalVars.browserObject
        return
        
#HCL TestCase 2585        27/11/2015       
    def getSection_Learn(self):
        """
        Description: 
            Navigates to Learn Section of dashbaord page
        """
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        status = pageObject.getSection_Learn()
        if status:
            self.succeed("Test Case passed")
        else:
            self.failure("Test Case failed", raiseExc=True)
        self.browserObject = globalVars.browserObject
#TestCase 2502        27/11/2015       
    def getSection_ServerPool(self):
        
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        status = pageObject.getSection_ServerPool()
        if status:
            self.succeed("Test Case passed")
        else:
            self.failure("Test Case failed", raiseExc=True)
        self.browserObject = globalVars.browserObject
        return
       
#TestCase 2460        27/11/2015       
    def getSection_Storage(self):
        
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        status = pageObject.getSection_Storage()
        if status:
            self.succeed("Test Case passed")
        else:
            self.failure("Test Case failed", raiseExc=True)
      
       
        
#TestCase 2523, 2478        27/11/2015       
    def get_View_Link(self, option):
        
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        if(option == "DashboardServices"):
            time.sleep(5)
            status = pageObject.get_DashboardServices()
        else:
            status = pageObject.get_ViewLink(option)
        if status == "true":
            self.succeed("Test Case passed")
            
        else:
            self.failure("Test Case failed", raiseExc=True)
        
        
# Testcase 2463    26/11/2015
    def get_IndividualServices(self):
        """
        Description: 
            Navigates to Individual Services of dashbaord page
            
        """
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        time.sleep(5)
        status = pageObject.get_IndividualServices()
        utility.execLog(status)
        if status:
            self.succeed ("TestCase passed")
        else:
            self.failure("TestCase failed ", raiseExc=True)
    
        return
        
# Testcase 2560    27/11/2015
    def get_DashboardOptions(self):
      
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        status = pageObject.getOptions()
        utility.execLog(status)
        if status:
            self.succeed ("TestCase passed")
        else:
            
            self.failure("TestCase failed ", raiseExc=True)

# Testcase 2653    30/11/2015
    def get_RecentActivity(self, option, option2, deployNow=True):
        #Get Templates
        templateExists = False
        templatePublished = False
        publishTemplate=True
        storageName="Test Storage"
        if option == "manageFirmware":
            manageFirmware = True
        else:
            manageFirmware = False
        tempList = self.getTemplates(option="My Templates")
        for temp in tempList:
            if temp["Name"] == "Test Template":
                templateExists = True
           
        if not templatePublished and templateExists:
            self.succeed("Template with Name 'Test Template' already exists and not Published so Deleting 'Test Template'")            
            self.deleteTemplate("Test Template")
        if templatePublished and templateExists:
            self.succeed("Published Template with Name 'Test Template' already exists so not creating New Template")
        else:
            #Get Resources
            utility.execLog("Loading resources page")
            resList = self.getResources("Storage")
            time.sleep(20)
            utility.execLog("Loaded resources page")
            time.sleep(5)
            if len(resList) > 0:
                self.succeed("Successfully fetched Storage Resources, Values :: %s"%(str(resList)))
            else:
                self.failure("No Resources of Type 'Storage'", resultCode=BaseClass.OMITTED, raiseExc=True)
            storageName = ""
            for res in resList:
                if res["Manufacturer /Model"] == "Dell PowerEdge M620":
                    storageName = res["Resource Name"]
            #Create Template
            
            date = datetime.datetime.now().strftime('%y%m%d%H%M%S') 
            volume_name = "HCLVol"  +str(date)
            utility.execLog("Volume name :%s"%volume_name)
            utility.execLog("Storage name :%s"%storageName)
                
            self.createTemplateBasic("Test Template", storageName, publishTemplate,manageFirmware,volumeName=volume_name, volumeSize="1GB")
            time.sleep(20)
        if (option2 == "Server Template"):
            return "Test Template"
        
        if (option == "Administrator"):
            
            #Check for current\standard logged in user
            self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
            #Get Template Options
        
        if (option == "Standard"):
            
            #Check for current\standard logged in user
            self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
            #Get Template Options
        if (option == "ReadOnly"):
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
          
        time.sleep(20)
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        if deployNow:
            self.browserObject.find_element_by_id("deployLink").click()
            utility.execLog("Navigate to Deploy Service")
            time.sleep(20)
            self.browserObject.find_element_by_id("servicename").send_keys(name)
            time.sleep(2)
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            time.sleep(2)
            self.browserObject.find_element_by_id("btnWizard-Next").click()
            utility.execLog("Deployed a service from a template shared by the Admin")
            time.sleep(2)
            self.browserObject.find_element_by_id("btnWizard-Finish").click()
            utility.execLog("click to finished button")
        
            #handle pop up for confirm deploy services
            time.sleep(7)
            self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
            time.sleep(20)

        if (option == "Administrator"):
            return name 
 
        if (option2 == "Verify In-Progress Services"):
            time.sleep(5)
            return "Test Template"
        
        if (option2 == "Error Services"):
            return "Test Template", name
            
        if(option == "ReadOnly"):
           
            self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
            #Verify Getting Started Page
            self.verifyLandingPageOptions(userRole='Read only')
        else:
            time.sleep(20)
        time.sleep(5)
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        time.sleep(20)
        status = pageObject.get_RecentActivity(name,option)   
        time.sleep(10)    
        utility.execLog(status)
        if status == "true":
            self.succeed ("TestCase passed")
        else:
            self.failure("TestCase failed ", raiseExc=True)
    
    def get_ServerPool(self, option):
      
        pageObject = Resources.Resources(self.browserObject)
        pageObject.loadPage()
        time.sleep(5)
        self.browserObject.find_element_by_id("tabserverpools").click()
        time.sleep(5)
        table = self.browserObject.find_element_by_id("serverpoolTable")
        time.sleep(2)
        tableBody = table.find_element_by_tag_name("tbody")
        tableBodyRows = tableBody.find_elements_by_tag_name("tr")
        for rowindex in xrange(0, len(tableBodyRows)):                
            cols = tableBodyRows[rowindex].find_elements_by_tag_name("td")
            servers_name_list = []

            if (option == "Standard"):
                
                
                page = self.browserObject.find_element_by_id("page_serverpools")
                tab = page.find_element_by_id("tabMenu")
                time.sleep(5)
                elems = tab.find_elements_by_tag_name("li")
                if elems[0].text == "Users":
                    tabName = elems[0]
                    
                else:
                    tabName = elems[1]
                tabName.click()
                time.sleep(5)
                utility.execLog("Reading user Table")
                table = self.browserObject.find_element_by_id("users_table")
                time.sleep(2)
                #Fetch Table header Details
                utility.execLog("Able to identify user Table")
                #Fetch Resource Details   
                tBody = table.find_element_by_tag_name("tbody")
                tBodyRows = tBody.find_elements_by_tag_name("tr")
                rows = xrange(0, len(tBodyRows))
                for rindex in rows:                   
                    colls = tBodyRows[rindex].find_element_by_xpath ("./td[3]/span").text
                    self.succeed(str(colls))
                    if str(colls) == "standard":
                        tBodyRows[rindex].click() 
                        servers_name_list.append(str(cols[1].text))
                        time.sleep(5)
                        self.succeed(servers_name_list)
                        time.sleep(2)
                            
                tableBodyRows[rowindex].click()
            else :
                servers_name_list.append(str(cols[1].text))
                time.sleep(5)
                self.succeed(servers_name_list)
                #Get Servers and Users in the Server Pool
                tableBodyRows[rowindex].click() 
        
        time.sleep(5)
        self.browserObject = globalVars.browserObject
        #Check for current logged in user
        if option == "Standard":
            time.sleep(5)
            self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        else :
            self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
            
        
        time.sleep(5)
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()  
        time.sleep(10)
        status = pageObject.get_displayServers(option, servers_name_list)
        utility.execLog(status)
        if status:
                self.succeed ("TestCase passed")
        else:
                self.failure("TestCase failed ", raiseExc=True)
                
    def get_DashboardPage(self, option):
      
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        time.sleep(10)
        self.browserObject.refresh()
        time.sleep(15)
        if option == "Total Services":
            status = pageObject.get_Total_Services()  
        elif option == "Server Utilization":
            status = pageObject.get_Server_Utilization()
        elif option == "Total Server Utilization":
            status = pageObject.get_TotalServer_Utilization()
        elif option == "Storage Volume":
            status = pageObject.get_Storage_Volume()
        elif option == "Total Storage Utilization":
            status = pageObject.get_TotalStorage_Volume()
        elif option == "Total Resources":
            status = pageObject.get_TotalResources()
        else:
            utility.execLog("after page refresh") 
            
        if status == "true":
                self.succeed ("TestCase passed")
        else:
                self.failure("TestCase failed ", raiseExc=True)
    
    #HCL Test 2525         
    def get_RepositoriesPage(self, option):
      
        pageObject = Repositories.Repositories(self.browserObject)
        pageObject.loadPage()
        time.sleep(5)  
        if option == "Firmware":
            pageObject.loadPage()
            status = pageObject.verify_firmwarepage()
        if option == "Details":
            pageObject.loadPage()
            status = pageObject.getDetails()
            
        if status:
                self.succeed ("TestCase passed")
        else:
                self.failure("TestCase failed ", raiseExc=True)
    
    #HCL Test 2561                          
    def verifyFirmwareOptions(self, optionList, userRole='Read only', optionTab="Firmware"):
        """
        Description: 
            Verify Options under Firmware
        Input:
            None
        """
        optionDict = {}
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions(globalVars.readOnlyUser, optionTab=optionTab)
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        failStatus = [key for key, value in optionDict.items() if key in optionList and value != "Disabled"]
        if len(failStatus) > 0:
            self.failure("Some User Management options are 'Enabled' for '%s' Firmware :: %s"%(str(optionDict), userRole), raiseExc=True)
        else:
            self.succeed("All User Options %s are 'Disabled' for '%s' Firmware :: %s"%(str(optionList), userRole, str(optionDict)))
        self.browserObject = globalVars.browserObject
        
    def verifyViewDetails(self, resourceType="All", verifyAll=False, negativeScenario=False):
        """
        Description:
            Verifies whether user can View Details of a Resource 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyViewDetails(resourceType, verifyAll)
        if negativeScenario:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        
    def verifyIPAddressLink(self, resourceType="All", verifyAll=False, clickAndVerify=False, negativeScenario=False):
        """
        Description:
            Verifies whether IP Address Link of a Resource is enabled 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status: 
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyIPAddressLink(resourceType, verifyAll, clickAndVerify)
        if negativeScenario:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    #HCL test2533
    def get_check_Service_Details(self, FilterBy='All'):
        """
        Description:
            Fetches Services 
        """
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status: 
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        status = pageObject.get_check_Service_Details(FilterBy)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
 
    def get_ServicesPage(self, option1, option2, deployNow=True):
        service_name = ""
        if option2 == "Verify In-Progress Services":  
            template_name  = self.get_RecentActivity("ReadOnly", option2, deployNow)
            time.sleep(5)
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            time.sleep(5)
            if option1 == "ReadOnly":
                service_name = pageObject.verify_Services("ReadOnly","Second_Storage_Deployment", template_name)
                self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
                status = pageObject.verify_Services("user","",template_name)
            if option1 == "Standard":
                time.sleep(5)
                status = pageObject.verify_Services("user","Second_Storage_Deployment", template_name)
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
                status = pageObject.verify_Services("user","",template_name)
        if option2 == "Error Services":
            template_name, service_name = self.get_RecentActivity("ReadOnly", option2, deployNow)
            time.sleep(5)
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            time.sleep(5)
            if option1 == "ReadOnly":
                pageObject.verify_Services("Error Services","Second_Storage_Deployment", template_name)
                self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
                time.sleep(10)
                status = pageObject.VerifyServiceDetails("Error Services")  
        if option2 == "View Services":
            template_name = self.get_RecentActivity("ReadOnly",option2)
            status = "true"
        if option2 == "Deploying Service":
            serverPoolName = "Pool2614"
            pageObject = Resources.Resources(self.browserObject)
            self.succeed("Assigning standard user the server pool")
            
            if serverPoolName.lower() != "global":
                serverPools = self.getServerPools()
                self.succeed("Existing Server Pools : %s"%serverPools)
                exists = [pool for pool in serverPools if pool["Server Pool Name"] == serverPoolName]            
                if exists:
                    self.succeed("Server Pool with Name '%s' already exists so deleting existing one"%serverPoolName)
                    self.deleteServerPool(serverPoolName)
                    time.sleep(5)
                    pageObject.createServerPool(serverPoolName, [], ["autostandard"], addAllServers=False, addFirstHealthyServer=True)
                    utility.execLog("if exists server pool")
                else:
                    pageObject.createServerPool(serverPoolName, [], ["autostandard"], addAllServers=False, addFirstHealthyServer=True)
                    utility.execLog("if not  exists server pool created")    
                      
            self.succeed("Creating template")
            time.sleep(15)
            self.browserObject.refresh()
            time.sleep(10)
            template_name = self.get_RecentActivity("","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            pageObject.Deploy_ServerTemplate(template_name,serverPoolName,"No Deployment")
            navObject = Navigation.Navigation(self.browserObject)
            navObject.selectOption("Dashboard")
            time.sleep(5)
            self.succeed("Logging as standard user")
            self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
            pageObject = Services.Services(self.browserObject)
            status = pageObject.Deploy_Service(template_name,serverPoolName, "")
        if option2 == "Migration":
            serverName = "Global"    
            self.succeed("Creating template")
            template_name = self.get_RecentActivity("My Templates","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            serviceName = pageObject.Deploy_ServerTemplate(template_name,serverName)
            
            if serviceName == "No servers found for deployment":
                
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(2)
                status = "true" 
            else:
                self.succeed("Logging as standard user") 
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
                pageObject = Services.Services(self.browserObject) 
                status = pageObject.Deploy_Service(template_name,serverName,option2) 
                
        if option2 == "Deployment_without_server_access":
            serverName = "Test_Server" 
            resourcePage = Resources.Resources(self.browserObject)
            resourcePage.deleteServerPool(serverName)  
            resourcePage.createServerPool(serverName, [], [], addAllServers=False, addFirstHealthyServer=True)
            self.succeed("Creating template")
            template_name = self.get_RecentActivity("","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            status = pageObject.Deploy_ServerTemplate(template_name,serverName,opt="server_check")
            resourcePage.deleteServerPool(serverName)
        
        if option2 == "Firmware_update_check":
            serverName = "Global"
            self.succeed("Creating template")
            template_name = self.get_RecentActivity("","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            pageObject.Deploy_ServerTemplate(template_name,serverName,"No Deployment")
            navObject = Navigation.Navigation(self.browserObject)
            navObject.selectOption("Dashboard")
            pageObject = Services.Services(self.browserObject)
            status = pageObject.Deploy_Service_with_FirmwareUpdate(template_name)
        
        if option2 == "Firmware_update_Template":
            serverName = "Global"
            self.succeed("Creating template")
            template_name = self.get_RecentActivity("manageFirmware","Server Template")
            
            pageObject = Templates.Templates(self.browserObject)
            pageObject.loadPage()
            time.sleep(5)
            Service_name = pageObject.Deploy_ServerTemplate(template_name,serverName)
            time.sleep(5)
            navObject = Navigation.Navigation(self.browserObject)
            navObject.selectOption("Dashboard")
            time.sleep(20)
            if(Service_name):
                self.succeed("Verified that while creating a template, the Admin can select and update the server firmware.")
                status = "true" 
            
        else:
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option2 =="ServiceDetails":
                status = pageObject.get_ServiceDetails("ReadOnly")
            if option2 == "FirmwareActions":
                status = pageObject.get_ServiceDetails("FirmwareActions") 
            if option2 == "Cannot Adjust Resources":
                status = pageObject.get_Standard_user_ServiceDetails(option2) 
            if option2 == "WarningServices":
                if option1 == "ReadOnly":
                    status = pageObject.VerifyServiceDetails("WarningServices") 
                if option1 == "Standard":
                    status = pageObject.Verify_StandardUserServices("WarningServices")
        if status == "true":
                self.succeed ("TestCase passed")
        else:
                self.failure("TestCase failed ", raiseExc=True)
        return service_name    
                
    def get_Verify_Individual_Resources(self, resourceType='All'):
        """
        Description:
            Fetches Resources 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            status, result = pageObject.getResources(resourceType)	
        #status = pageObject.get_Verify_Individual_Resources(resourceType)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
   
    #HCL 2592
    def check_Service_OwnedBy_Standard(self, option,option2="Services"):
        """
        Description:
            Standard user can adjust any resources in a service owned by him 
        """
        utility.execLog("Loading Manager check_Service_OwnedBy_Standard")
        templateExists = False
        templatePublished = False
        tempList = self.getTemplates(option="My Templates")
        for temp in tempList:
            if temp["Name"] == "Test Template":
                templateExists = True

        if not templatePublished and templateExists:
            self.succeed("Template with Name 'Test Template' already exists and not Published so Deleting 'Test Template'")            
            self.deleteTemplate("Test Template")
        if templatePublished and templateExists:
            self.succeed("Published Template with Name 'Test Template' already exists so not creating New Template")
        else:
            #Get Resources
            resList = self.getResources("Storage")
            if len(resList) > 0:
                self.succeed("Successfully fetched Storage Resources, Values :: %s"%(str(resList)))
            else:
                self.failure("No Resources of Type 'Storage'", resultCode=BaseClass.OMITTED, raiseExc=True)
            storageName = ""
            for res in resList:
                if res["Manufacturer /Model"] == "Dell PowerEdge M620":
                    storageName = res["Resource Name"]
            #Create Template
            self.createTemplateBasic("Test Template", storageName, publishTemplate=True)
        if (option == "Standard"):
            
            #Check for current\standard logged in user
            self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
            #Get Template Options
        if (option == "ReadOnly"):
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
          
        time.sleep(5)
        self.browserObject.find_element_by_id("deployLink").click()
        utility.execLog("Navigate to Deploy Service")
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(20)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        utility.execLog("Deployed a service from a template shared by the Admin")
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Finish").click()
        utility.execLog("click to finished button")
        
        #handle pop up for confirm deploy services
        time.sleep(5)
        self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
        time.sleep(20)  
        if(option == "ReadOnly"):
           
            self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
            #Verify Getting Started Page
            self.verifyLandingPageOptions(userRole='Read only')
        else:
            time.sleep(20)
            
        pageObject = Templates.Templates(self.browserObject)
        status = pageObject.check_Service_OwnedBy_Standard()
        if status :
            self.succeed("TestCase Passed")
        else :
            self.failure("TestCase failed", raiseExc=True)
            
        
    #HCL test2596
    def get_check_Services_DeviceConsole(self, FilterBy='All'):
        """
        Description:
            checking device console for Standard user
        """
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status: 
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        status = pageObject.get_check_Services_DeviceConsole(FilterBy)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    #HCL test 2533
    def get_check_Services_Readonly_DeviceConsole(self, FilterBy='All'):
        """
        Description:
            checking device console for Read only user
        """
        name = self.get_RecentActivity("ReadOnly","")
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
    
        pageObject = Services.Services(self.browserObject)
        pageObject.loadPage()
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        tRows = self.browserObject.find_elements_by_xpath(".//*[@id='serviceTable']/tbody/tr")
        utility.execLog(len(tRows))
        for row in xrange(0, len(tRows)):
            time.sleep(2)
            tRows[row].click()
            service_name = tRows[row].find_element_by_xpath("./td[2]").text
            if name == None:
                name = "Test Service"
            if service_name==name:
                self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li[1]/a").click()
                time.sleep(10)
                IPAddr=self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[1]/td[3]/span").text
                utility.execLog("Device ip value is :" ,IPAddr)
                utility.execLog(IPAddr)
                time.sleep(3)
                if not (self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[1]/td[3]/span").get_attribute("a")):
                    utility.execLog("IpAddress of device is Disabled ")
                    return "true"
            if int(row)>10:
                break
        time.sleep(10)
        IPAddr=self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[1]/td[3]/span").text
        utility.execLog("Device ip value is :" ,IPAddr)
        utility.execLog(IPAddr)
        time.sleep(3)
        if not (self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[1]/td[3]/span").get_attribute("a")):
            utility.execLog("IpAddress of device is Disabled ")
            return "true"

    def get_TemplatesPage(self, option):
             
        if option == "Server Template":
            serverName = "Global"       
            template_name = self.get_RecentActivity("","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            pageObject.loadPage()
            time.sleep(10)
            
            Service_name = pageObject.Deploy_ServerTemplate(template_name,serverName)
            time.sleep(5)
            if Service_name == "No servers found for deployment":
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(2)
                status = "true"
            else:
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)       
                pageObject = Services.Services(self.browserObject)
                status = pageObject.ServiceDetails(Service_name,template_name)
       
        if status == "true":
            self.succeed ("TestCase passed")
        else:
                       
            self.failure("TestCase failed ", raiseExc=True)
        return Service_name
    
    def DeleteusedServers(self,option):
        
        utility.execLog("Clicking on Services option at dashboard")
        navObject = Navigation.Navigation(self.browserObject)
        navObject.selectOption("Services")
        time.sleep(2)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(5)
        utility.execLog("Deleting the Service if exists which is using any Server")
        table = self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']")
        tRows = table.find_elements_by_xpath("./tbody/tr")
        for row in xrange(0,len(tRows)):
            tRow = self.browserObject.find_elements_by_xpath(".//*[@id='serviceTable']/tbody/tr")
            tRow[row].click()
            time.sleep(2)
            servers_deployed = self.browserObject.find_element_by_id("server").text
            if (int(servers_deployed)>0):
                self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li[1]/a").click()
                time.sleep(5)
                service_health_state = self.browserObject.find_element_by_xpath(".//*[@id='serviceActions']/ul[1]/li[2]/span[2]").text
                if service_health_state == "In Progress":
                    self.browserObject.find_element_by_xpath(".//*[@id='page_service_detail']/header/div/a").click()
                    time.sleep(5)
                else :           
                    self.browserObject.find_element_by_id("lnkDeleteService").click()
                    time.sleep(2)
                    self.browserObject.find_element_by_id("submit_form_deleteservice").click()
                    time.sleep(2)
                    self.browserObject.find_element_by_id("submit_confirm_form").click()
                    time.sleep(10)
        self.succeed("Successfully deleted the used server")
              
        
    def get_ServicesPage_standard(self, option1, option2):
      
     
        if option2 == "Verify In-Progress Services":   
            template_name = self.get_RecentActivity("ReadOnly",option2)
            time.sleep(5)
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option1 == "ReadOnly":
                pageObject.verify_Services("ReadOnly","Second_Storage_Deployment",template_name)
                self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
                status = pageObject.verify_Services("user","",template_name)
        if option2 == "Error Services":   
            template_name = self.get_RecentActivity("ReadOnly",option2)
            time.sleep(5)
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option1 == "ReadOnly":
                pageObject.verify_Services("Error Services","Second_Storage_Deployment",template_name)
                self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
                time.sleep(10)
                status = pageObject.get_ErrorServiceDetails("Error Services")  
                
        if option2 == "View Services":
            template_name = self.get_RecentActivity("ReadOnly",option2)
            status = "true"
        
        if option2 == "Deploying Service":
            serverName = "Global"
            template_name = self.get_RecentActivity("Server Template", "")
            pageObject = Templates.Templates(self.browserObject)
            pageObject.loadPage()
            time.sleep(10)
            serviceName = pageObject.Deploy_ServerTemplate(template_name,serverName)
            if serviceName == "No servers found for deployment":
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(2)
                status = "true"
            else:
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
                status = pageObject.Deploy_Service(template_name,serverName)    
        
        else:
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option2 =="ServiceDetails":
                status = pageObject.get_ServiceDetails("ReadOnly")
            if option2 == "FirmwareActions":
                status = pageObject.get_ServiceDetails("FirmwareActions")  
                
            
        if status == "true":
                self.succeed ("TestCase passed")
        else:
                self.failure("TestCase failed ", raiseExc=True)
                
    def viewDeployment_services_Settings(self, serviceName):
        """
        Description: 
            Fetches Service Deployment Settings from Template Builder Page
        """
        
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.viewDeployment_services_Settings(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            tempSettings = result
        self.browserObject = globalVars.browserObject
        return self.browserObject, status, result
    
    
    def getVirtualIdentityDetails(self):
        pageObject = VirtualIdentityPools.VirtualIdentityPools(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getSummaryDetails()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result  
      
      
    # Added new function for NGI-TC-2637            
    def getServiceExportFile(self, serviceName):
        """
        Description: 
            verify whether service file is exported.
        """
        pageObject = Services.Services(self.browserObject)

        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        
        # View Service details    
        globalVars.browserObject, status, result = pageObject.viewServiceDetails(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
            
        #Export file of Service
        globalVars.browserObject, status, result = pageObject.exportFile(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        
    # Created for TC-2496    
    def changeUserRole(self, userName, currentPassword, newRole):
        """
        Description:
            Changes the role of an existing user.
        """
        pageObject = Users.Users(self.browserObject)
       
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
            
        #Change user Role
        #globalVars.browserObject, status, result = pageObject.editUserInformation(userName, currentPassword, editRole=newRole)
        globalVars.browserObject, status, result = pageObject.editUser(userName, currentPassword, userRole=newRole)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
            
        # Verifying the updated user role.     
        globalVars.browserObject, status, result = pageObject.getUsers()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            for listItem in result:
                if userName in listItem['Name']:
                    if newRole in listItem['Role']:
                        self.succeed("User %s role is successfully updated to %s"%(str(userName), str(newRole)))
                    else:
                        self.failure("User %s role could not be updated to %s"%(str(userName), str(newRole)))
                        
    # Created for TC-2496 , modified for TC-2609                   
    def verifyReadOnlyUserPagesSettings(self, settingsFlag="disabled"):
        """
        Description:
            Verify different page settings for standard user whose role is changed to read only.
        """
        dashboardPageObject = Dashboard.Dashboard(self.browserObject)
        globalVars.browserObject, status, result = dashboardPageObject.loadPage()
        time.sleep(10)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            try:
                if settingsFlag=="disabled":
                    if not dashboardPageObject.getDeployServiceButtonStatus():
                        self.failure("Deploy New Service button is Enabled, current user does not have ReadOnly user settings.")
                    else:
                        self.succeed("Deploy New Service button is Disabled, Current standard user has settings compatible to Read Only user.")
                else:
                    if dashboardPageObject.getDeployServiceButtonStatus():
                        self.failure("Deploy New Service button is Enabled, current user does not have ReadOnly user settings.")
                    else:
                        self.succeed("Deploy New Service button is Disabled, Current standard user has settings compatible to Read Only user.")
                    
                servicePageObject = Services.Services(self.browserObject)
                globalVars.browserObject, status, result = servicePageObject.loadPage()
                time.sleep(5)
                if not status:
                    self.failure(result, raiseExc=True)
                else:
                    if settingsFlag=="disabled":
                        if not servicePageObject.getExportToFileButtonStatus():
                            self.failure("Export To File button is Enabled, current user does not have ReadOnly user settings.")
                        else:
                            self.succeed("Export To File button is Disabled, current standard user has settings compatible to Read Only user.")
                    else:
                        if servicePageObject.getExportToFileButtonStatus():
                            self.failure("Export To File button is Enabled, current user does not have ReadOnly user settings.")
                        else:
                            self.succeed("Export To File button is Disabled, current standard user has settings compatible to Read Only user.")
                        
                resourcesPageObject = Resources.Resources(self.browserObject)
                globalVars.browserObject, status, result = resourcesPageObject.loadPage()
                time.sleep(10)
                if not status:
                    self.failure(result, raiseExc=True)
                else:
                    globalVars.browserObject, status, result = self.verifyIPAddressLink(resourceType="All", verifyAll=False, clickAndVerify=False)
                    if settingsFlag=="disabled":                        
                        if not status:
                            self.failure("Resource IP address link is Enabled, current user does not have ReadOnly user settings.")
                        else:
                            self.succeed("Resource IP address link is disabled, current standard user has settings compatible to Read Only user.")
                    else:
                        if status:
                            self.failure("Resource IP address link is Enabled, current user does not have ReadOnly user settings.")
                        else:
                            self.succeed("Resource IP address link is disabled, current standard user has settings compatible to Read Only user.")
            except Exception as e:
                utility.execLog("Exception while trying to verify pages settings for Read Only Role :: Error -> %s"%str(e))
                raise e
    
    #Created for test case TC-2623        
    def verifyManageFirmwareOption(self, templateName, serviceName):
        """
        """
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        utility.execLog(" Verifying 'Manage Firmware' Option availability when trying to deploy service of template %s"%str(templateName))            
        globalVars.browserObject, status, result = pageObject.deployService(templateName, serviceName, manageFirmware=True)
        if "'Manage Firmware' Option not available" in result:
            utility.execLog("Close Service deployment window.")
            self.browserObject.find_element_by_xpath("//button[@class='btn modal-tools-close']").click()
            time.sleep(2)
            self.browserObject.find_element_by_xpath("//button[@id='submit_confirm_form']").click()
            time.sleep(2)   
            self.succeed(result)
        else:
            self.failure("'Manage Firmware' Option is available.")
            
    #HCL 2590        
    def getServiceManagableByStandardUser(self, serviceName):
        """
        Description: 
            verify whether service file is exported.
        """
        utility.execLog("Calling getServiceManagableByStandardUser() Manager")
        pageObject = Services.Services(self.browserObject)

        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        
        # View Service details    
        globalVars.browserObject, status, result = pageObject.viewServiceDetails(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        
        time.sleep(15)    
        #Export file of Service
        globalVars.browserObject, status, result = pageObject.getServiceManagableByStandardUser(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
            
    #HCL 2657        
    def createAndEditUser(self):
        """
        Description: 
            Change user role from admin to standard
        """
        utility.execLog("createAndEditUser()..Manager")
        users = self.getLocalUsers("Standard")
        for user in users:
            utility.execLog("1st for")
            if user["Name"] == "Admin123" :
                self.deleteLocalUser("Admin123")
        
        self.createLocalUser(userName="Admin123", userPassword="hclstd123",verifyUser=True)
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True) 
        currentPassword = self.loginPassword            
        #globalVars.browserObject, status, result = pageObject.editUserRole(userName="Admin123",currentPassword=currentPassword,userRole="Standard")
        globalVars.browserObject, status, result = pageObject.editUser(userName="Admin123",currentPassword=currentPassword,userRole="Standard")
        userRole = "Standard"
        userName = "Admin123"
        userPassword="hclstd123"
         
        users = self.getLocalUsers(userRole=userRole)
        utility.execLog(" Users List : %s"%str(users))
        users = [user for user in users if user["Name"] == userName]
        if len(users) > 0:
            self.succeed("Successfully verified Local User :: '%s' with Role :: '%s'"%(userName, userRole))
            time.sleep(2)
            self.logout()
            #Login as Standard User
            self.login(username=userName, password=userPassword, newInstance=False)
             
            utility.execLog(" Going to verify Resources Page")
            #Get All Resources
            resList = self.getResources()
            utility.execLog("After calling getResources()...")
            if len(resList) > 0:
                self.succeed("Able to fetch Resources Info :: %s"%(str(resList)))
            else:
                self.succeed("No Resources were Discovered :: %s"%(str(resList)))
          
            #Get All Server Pools
            utility.execLog(" Going to verify Server Pool")
            poolList = self.getServerPools()
            if len(poolList) > 0:
                self.succeed("Able to fetch Server Pool Info :: %s"%(str(poolList)))
            else:
                self.succeed("There are No Server Pools :: %s"%(str(poolList)))
            utility.execLog(" Going to verify Template Page")
            #Verify Sample Templates in List form
            time.sleep(10)
            tempList = self.getTemplates(option="Sample Templates", viewType="List")
            time.sleep(5)
            if len(tempList) > 0:
                self.succeed("Able to read 'Sample Templates' in 'List' Form :: %s"%str(tempList))
            else:
                self.succeed("No Templates in 'Sample Templates' Tab to view in 'List' form")
                 
            utility.execLog(" Going to verify Services Page")
            #Verify Services in List form
            svcList = self.getServices()
            time.sleep(5)
            if len(svcList) > 0:
                self.succeed("Able to read Services in 'List' Form :: %s"%str(svcList))
            else:
                self.succeed("No Services in 'Services' Page to view in 'List' form")    
        else:
            self.failure("Failed to verify Local User :: '%s' with Role :: '%s'"%(userName, userRole), raiseExc=True)

        #HCL_Testcase_3352   
    def get_Deploy_ServicesPage(self, option1, option2):
      
     
        if option2 == "Verify In-Progress Services":   
            template_name = self.get_RecentActivity("ReadOnly",option2)
            time.sleep(5)
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option1 == "ReadOnly":
                pageObject.verify_Services("ReadOnly","Second_Storage_Deployment",template_name)
                self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
                status = pageObject.verify_Services("user","",template_name)
            if option1 == "Standard":
                       
                status = pageObject.verify_Services("user","Second_Storage_Deployment",template_name)
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
                status = pageObject.verify_Services("user","",template_name)
        if option2 == "Error Services":   
            template_name = self.get_RecentActivity("ReadOnly",option2)
            time.sleep(5)
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option1 == "ReadOnly":
                pageObject.verify_Services("Error Services","Second_Storage_Deployment",template_name)
                self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
                time.sleep(10)
                status = pageObject.VerifyServiceDetails("Error Services")  
                
        if option2 == "View Services":
            template_name = self.get_RecentActivity("ReadOnly",option2)
            status = "true"
        
        if option2 == "Deploying Service":
            
            serverName = "Rack"
            pageObject = Resources.Resources(self.browserObject)
            self.succeed("Assigning standard user the server pool")
            pageObject.assignUserstoServer("Rack", "autostandard")
            self.succeed("Creating template")
            template_name = self.get_RecentActivity("","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            serviceName = pageObject.Deploy_ServerTemplate(template_name,serverName,"No Deployment")
            
            if serviceName == "No servers found for deployment":
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(2)
                status = "true"
            else:
                navObject = Navigation.Navigation(self.browserObject)
                navObject.selectOption("Dashboard")
                self.succeed("Logging as standard user")
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
                pageObject = Services.Services(self.browserObject)
                status = pageObject.Deploy_Service(template_name,serverName, "")
      
        if option2 == "Migration":
            
            serverName = "Global"    
            self.succeed("Creating template")
            template_name = self.get_RecentActivity("My Templates","Server Template")
            pageObject = Templates.Templates(self.browserObject)
            serviceName = pageObject.Deploy_ServerTemplate(template_name,serverName)
            
            if serviceName == "No servers found for deployment":
                
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(2)
                status = "true" 
            else:
                self.succeed("Logging as standard user") 
                self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
                pageObject = Services.Services(self.browserObject) 
                status = pageObject.Deploy_Service(template_name,serverName,option2)            
        
        else:
            pageObject = Services.Services(self.browserObject)
            pageObject.loadPage()
            if option2 =="ServiceDetails":
                status = pageObject.get_ServiceDetails("ReadOnly")
            if option2 == "FirmwareActions":
                status = pageObject.get_ServiceDetails("FirmwareActions") 
            if option2 == "Cannot Adjust Resources":
                status = pageObject.get_Standard_user_ServiceDetails(option2) 
            if option2 == "WarningServices":
                if option1 == "ReadOnly":
                    status = pageObject.VerifyServiceDetails("WarningServices") 
                if option1 == "Standard":
                    status = pageObject.Verify_StandardUserServices("WarningServices")
                
            
        if status == "true":
                self.succeed ("TestCase passed")
        else:
                self.failure("TestCase failed ", raiseExc=True)
                
    
    # Created for TC-2533                
    def verifyServiceDeviceConsoleLink(self, serviceName):
        """
        Verify for a given service Device console link is disabled. 
        """
        pageObject = Services.Services(self.browserObject)

        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        
        # View Service details    
        globalVars.browserObject, status, result = pageObject.viewServiceDetails(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
            
        # View Service component ip address link.    
        globalVars.browserObject, status, result = pageObject.verifyServiceComponentIPaddressState()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)    
        
            
        
    def verifyAdminRolePagesSettings(self):
        """
        Description:
            Verify different page settings for Standard user whose role is changed to Admin.
        """
        dashboardPageObject = Dashboard.Dashboard(self.browserObject)
        globalVars.browserObject, status, result = dashboardPageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            try:
                if dashboardPageObject.getCreateNewTemplateButtonStatus():
                    self.succeed("'Create new Template' button is Enabled, current user have Admin user settings.")
                else:
                    self.failure("'Create new Template' button is Disabled, current user does not have Admin user settings.")   
                    
                if dashboardPageObject.getAddExistingServiceButtonStatus():
                    self.succeed("'Add Existing Service' button is Enabled, current user have Admin user settings.")
                else:
                    self.failure("'Add Existing Service' button is Disabled, current user does not have Admin user settings.")  
            except Exception as e:
                    utility.execLog("Exception while trying to verify pages settings for Admin Role :: Error -> %s"%str(e))
                    raise e
                
                
    def get_Server_Health(self,option):
        pageObject = Dashboard.Dashboard(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        self.browserObject.refresh()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.get_Server_Health(option)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)  
            
    def get_check_DeployServices_Now(self, option):
      
      
            service_name = self.get_RecentActivity("Administrator","")
            if(service_name):
                status = "true"
            if status == "true":
                self.succeed ("TestCase passed")
            else:
                self.failure("TestCase failed ", raiseExc=True)

    def get_deploy_Template(self, option):
        
        pageObject1 = Dashboard.Dashboard(self.browserObject)
        pageObject1.loadPage()
        Servers_used_prev = pageObject1.verify_ServersInUse()
        utility.execLog(Servers_used_prev) 
        Servers_used = int(Servers_used_prev) +1
        utility.execLog("get_deploy_Template() of Manager")
        status=""    
        if option == "Server Template":
          
            serverName = "Global" 
            utility.execLog("Server Template Global")
               
            template_name = self.get_RecentActivity("","Server Template")
            time.sleep(25)
            utility.execLog("Successfully created and deployed template")
            pageObject = Templates.Templates(self.browserObject)
            pageObject.loadPage()
            time.sleep(15)
              
            service_name = pageObject.Deploy_ServerTemplate(template_name,serverName)
            time.sleep(30)
            utility.execLog("Deployed Template successfully with Server..")
               
              
            time.sleep(15)
            utility.execLog("Dashboard page loaded successfully..") 
            
            retry = 12
            Servers_used_secound_round = pageObject1.verify_ServersInUse()
            if (int(Servers_used) == int(Servers_used_secound_round)):
                status = "true"
                utility.execLog("Successfully got total server utilization")
            else:
                while retry>1:
                    time.sleep(60)
                    Servers_used_secound_round = pageObject1.verify_ServersInUse()
                    retry = retry -1
                    self.succeed(retry)
                    if (int(Servers_used) == int(Servers_used_secound_round)):
                        status = "true"
                        utility.execLog("Successfully got total server utilization")
                        break
            if retry == 1:
                status="true"
        if status == "true":
            self.succeed ("TestCase passed")
            return service_name
        else:
                        
            self.failure("TestCase failed ", raiseExc=True)

    
    def get_check_DeployServices_later(self, option):
      
      
            service_name = self.get_RecentActivity_scheduleLater("Administrator","")
            if(service_name):
                status = "true"
            if status == "true":
                self.succeed ("TestCase passed")
            else:
                self.failure("TestCase failed ", raiseExc=True)            
                
                
    #    Testcase 2653    30/11/2015
    def get_RecentActivity_scheduleLater(self, option, option2):
      
        #Get Templates
        templateExists = False
        templatePublished = False
        tempList = self.getTemplates(option="My Templates")
        for temp in tempList:
            if temp["Name"] == "Test Template":
                templateExists = True
           
        if not templatePublished and templateExists:
            self.succeed("Template with Name 'Test Template' already exists and not Published so Deleting 'Test Template'")            
            self.deleteTemplate("Test Template")
        if templatePublished and templateExists:
            self.succeed("Published Template with Name 'Test Template' already exists so not creating New Template")
        else:
            #Get Resources
            resList = self.getResources("Storage")
            if len(resList) > 0:
                self.succeed("Successfully fetched Storage Resources, Values :: %s"%(str(resList)))
            else:
                self.failure("No Resources of Type 'Storage'", resultCode=BaseClass.OMITTED, raiseExc=True)
            storageName = ""
            for res in resList:
                if res["Manufacturer /Model"] == "Dell PowerEdge M620":
                    storageName = res["Resource Name"]
            #Create Template
            self.createTemplate("Test Template", storageName, publishTemplate=True)
            time.sleep(20)
        if (option2 == "Server Template"):
            return "Test Template"
        
        if (option == "Administrator"):
            
            #Check for current\standard logged in user
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
            
        if (option == "Standard"):
            
            #Check for current\standard logged in user
            self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
            #Get Template Options
        if (option == "ReadOnly"):
            self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName="Test Template")
          
        time.sleep(20)
        self.browserObject.find_element_by_id("deployLink").click()
        utility.execLog("Navigate to Deploy Service")
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        utility.execLog("Deployed a service from a template shared by the Admin")
        time.sleep(2)
        self.browserObject.find_element_by_id("schedule").click()
        utility.execLog("Add current/past Date & Time")
        self.browserObject.find_element_by_xpath(".//*[@id='dpScheduleDate']/div[2]/div/span/button").click()
        
        utility.execLog("Open Date Table & Select Past/Current Date")
        dateStr = datetime.datetime.now().strftime('%I:%M %p')
        sel=self.Select(self.browserObject.find_element_by_id("scheduleMeridiem"))
        selected_option = sel.first_selected_option
        optionText = selected_option.text
        if not optionText  in dateStr:
            sel.select_by_visible_text('PM') 
                      
        strTimeMins = dateStr.split(':')[1][:2]
        self.intTimeMins = int(strTimeMins)
        if self.intTimeMins < 10:
            self.intTimeMins = int(strTimeMins[:1]) + 5
        else:
            self.intTimeMins = int(strTimeMins) + 5
        strIntTimeMins = str(self.intTimeMins)
        selHrs =self.Select(self.browserObject.find_element_by_id("scheduleHour"))
        selMin =self.Select(self.browserObject.find_element_by_id("scheduleMinute"))
#         selHrs.select_by_visible_text(strIntTimeHrs)
        selMin.select_by_visible_text(strIntTimeMins)
        utility.execLog("Selected Hrs & mins %s%s"%str(strIntTimeMins))
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Finish").click()
        utility.execLog("click to finished button")
#         self.browserObject.find_element_by_id("btnWizard-Cancel").click()
#         utility.execLog("Unable to deploy past Date & Time Services")
        
        #handle pop up for confirm deploy services
        time.sleep(5)
        self.browserObject.find_element_by_id("submit_confirm_form").click()
        time.sleep(20)
        if (option == "Administrator"):
            return name 
        if (option2 == "Verify In-Progress Services"):
            time.sleep(10)
            return "Test Template"
        
        if (option2 == "Error Services"):
            return "Test Template"
            
        if(option == "ReadOnly"):
           
            self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
            #Verify Getting Started Page
            self.verifyLandingPageOptions(userRole='Read only')
        
        else:
            time.sleep(20)
        time.sleep(5)
        pageObject = Dashboard.Dashboard(self.browserObject)
        pageObject.loadPage()
        status = pageObject.get_RecentActivity(name,option)       
        utility.execLog(status)
        if status == "true":
            self.succeed ("TestCase passed")
        else:
            self.failure("TestCase failed ", raiseExc=True)
            
            
    #HCL 3329             
    def loadDashboardPage(self):
        pageObject = Dashboard.Dashboard(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(15)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyLinksAndPages()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def get_ServiceDetails_Firmware(self,option,option1):
        utility.execLog("get_ServiceDetails_Firmware().. Manager")
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject,status,result= pageObject.get_ServiceDetails_Firmware(option)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def myTempFunc(self):
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getResources("Servers")
        utility.execLog("Returned Resource list=> %s"%str(result))
        
        globalVars.browserObject, status, result = pageObject.readResourceTable()
        utility.execLog("Returned Resource table list=> %s"%str(result))
        
                  
    def verifyServerMemoryAttributesWithiDRAC(self, serverType, resourceStartIP="172.31.61.177", resourceEndIP="172.31.61.177", manageResource="Managed", serverCredential="Dell PowerEdge iDRAC Default", storageCredential="", chassisCredential="Dell chassis default", vcenterCredential="", switchCredential="Dell switch default", scvmmCrdential=""):
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        
        if "Chassis" in serverType:
            utility.execLog("Discovering resource %s"%str(serverType))
            globalVars.browserObject, status, result = pageObject.discoverResource(serverType, resourceStartIP, resourceEndIP, manageResource, serverCredential, storageCredential, chassisCredential, vcenterCredential, switchCredential, scvmmCrdential)
            utility.execLog("Waiting for server discovery completion.")
            loopCount = 10
            while loopCount:
                try:
                    if self.browserObject.find_element_by_xpath("//*[contains(text(), 'Initial Chassis Configuration in progress')]").is_displayed() or self.browserObject.find_element_by_xpath("//*[contains(text(), 'Discovery Job Running')]").is_displayed():
                        time.sleep(120)
                        continue
                    else:
                        utility.execLog("Resource discovery complete - IF branch")
                        break
                except:
                    utility.execLog("Resource discovery complete - except branch")
                    break
            
            self.browserObject.refresh()
            time.sleep(10)
            globalVars.browserObject, status, result = pageObject.getResources("All")
            utility.execLog("Resources returned :> %s"%str(result))
            for item in result:
                utility.execLog("Searching server 'FC630' in %s"%str(item))
                if "FC630" in item['Manufacturer /Model']:
                    self.resourceName = item['Resource Name']
                    utility.execLog("Serve found %s"%str(self.resourceName))
                    break
        else:
            globalVars.browserObject, status, result = pageObject.getResources("All")
            utility.execLog("Resources returned :> %s"%str(result))
            for item in result:
                utility.execLog("Searching server '%s' in %s"%(str(serverType),str(item)))
                if serverType in item['Resource Type']:
                    self.resourceName = item['Resource Name']
                    utility.execLog("Serve found %s"%str(self.resourceName))
                    break
        try:
            utility.execLog("Resource to be selected => %s"%str(self.resourceName))
        except:
            utility.execLog("Discovering resource %s becuase the same is not available"%str(serverType))
            globalVars.browserObject, status, result = pageObject.discoverResource("Chassis", resourceStartIP, resourceEndIP, manageResource, serverCredential, storageCredential, chassisCredential, vcenterCredential, switchCredential, scvmmCrdential)
            utility.execLog("Waiting for server discovery completion.")
            loopCount = 10
            while loopCount:
                try:
                    if self.browserObject.find_element_by_xpath("//*[contains(text(), 'Initial Chassis Configuration in progress')]").is_displayed() or self.browserObject.find_element_by_xpath("//*[contains(text(), 'Discovery Job Running')]").is_displayed():
                        time.sleep(120)
                        continue
                    else:
                        utility.execLog("Resource discovery complete - IF branch")
                        break
                except:
                    utility.execLog("Resource discovery complete - except branch")
                    break
            
            self.browserObject.refresh()
            time.sleep(10)
            globalVars.browserObject, status, result = pageObject.getResources("All")
            utility.execLog("Resources returned :> %s"%str(result))
            for item in result:
                utility.execLog("Searching server '%s' in %s"%(str(serverType),str(item)))
                if serverType in item['Resource Type']:
                    self.resourceName = item['Resource Name']
                    utility.execLog("Serve found %s"%str(self.resourceName))
                    break
            utility.execLog("Resource to be selected => %s"%str(self.resourceName))
            
        self.browserObject, status, result_server = pageObject.readResourceMemoryTable(self.resourceName)
        if not status:
            self.failure(result_server, raiseExc=True)
        else:
            self.succeed(result_server)        
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        
        self.browserObject, status, result_iDRAC  = pageObject.readResourceMemoryTableFromiDRAC(self.resourceName)
        if not status:
            self.failure(result_server, raiseExc=True)
        else:
            self.succeed(result_server)
        time.sleep(5)     
        
        if result_server[0]['Memory Type'] == result_iDRAC[0]['Type'] and result_server[0]['Current Speed'] == result_iDRAC[0]['Speed'] and (int(result_server[0]['Size'][:4]))/1024 == int(result_iDRAC[0]['Size'][:1]):
            self.succeed("Memory attributes of Blade server are same with memory listed in HW resources in iDRAC for the server")
            time.sleep(5)
        else:
            self.failure("Memory attributes of Blade server are same with memory listed in HW resources in iDRAC for the server")
        
    def getCreateTemplateLink(self,option):
        utility.execLog("getCreateTemplateLink().. Manager")
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getCreateTemplateLink(option)
        if status:
            self.succeed("Test case passed")
        else:
            self.failure("Test case failed", raiseExc=True)   
            
                    
    def addAttachment(self,templateName):
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.addAttachment(templateName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def get_JobsPage(self):
        pageObject = Jobs.Jobs(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def getOptionDetails_CustomBundles(self, option):
        """
        Description: Open Repositories & upload custom bundles 
        """
        if option == "Repositories":
            pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        if option == "Templates":
            globalVars.browserObject, status, result = pageObject.getDetails_CustomBundles("My Templates", "All", "List")
        else:
            globalVars.browserObject, status, result = pageObject.getDetails_CustomBundles()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)

            
    def getOptionDetails_VirtualAppliance(self, option):
        """
        Description: open virtual appliance management for update
        """
        if option == "VirtualApplianceManagement":
            pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(7)
        globalVars.browserObject, status, result = pageObject.upgradeAppliance()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)
            
    def isUpdateFirmwareEnabled(self, resourceList, negativeScenario=False):
        """
        Description: Open Resources & select error server 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.isUpdateFirmwareEnabled(resourceList)
        if not negativeScenario:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject


    def verifyVirtualIdentitiyPoolSettings(self, identityPoolName):
        """
        """
        pageObject = VirtualIdentityPools.VirtualIdentityPools(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.createVirtualIdentityPool(virtualPoolName=identityPoolName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyExportEnabled(identityPoolName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteVirtualIdentityPool(identityPoolName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

    def updateServerFirmware(self, updateAll= False, schelduleUpadte= False, changeManageState= None): 
        """
        Updates Server Firmware
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.updateServerFirmware(updateAll, schelduleUpadte, changeManageState)
        if status:
            self.succeed("Successfully updated Server to compliance state :%s"%result)
        else:
            self.failure("Update Firmware failed :%s"%result, raiseExc=True)
            
    def genrateAndUploadCertificate(self):
      
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result_date = pageObject.generateCertificate()
           
        utility.execLog("Again login as administartor")
        time.sleep(5)
        self.login()
        time.sleep(5)
        utility.execLog("Again login successful")
        self.verifyLandingPageOptions(userRole='Administrator')
        pageObject1 = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject1.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject1.verifyCertificate(result_date)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def changeServerSettings(self, serverIP, initialState):
        """
        Changes Server Settings
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.changeServerState(serverIP, initialState)
        if status:
            self.succeed(result)
        else:
            self.failure(result)
            
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        utility.execLog("Discovering resource server %s"%str(serverIP))
        globalVars.browserObject, status, result = pageObject.discoverResource("Server", serverIP, resourceEndIP="", manageResource="Managed", serverCredential="Dell PowerEdge iDRAC Default", storageCredential="", chassisCredential="", vcenterCredential="", switchCredential="", scvmmCrdential="")
        time.sleep(2)
        utility.execLog("Waiting for server discovery completion.")
        loopCount = 10
        while loopCount:
            try:
                if self.browserObject.find_element_by_xpath("//*[contains(text(), 'Discovery Job Running')]").is_displayed():
                    time.sleep(120)
                    continue
                else:
                    utility.execLog("Resource discovery complete - IF branch")
                    break
            except:
                utility.execLog("Resource discovery complete - except branch")
                break
        
        self.browserObject.refresh()
        time.sleep(5)    
        globalVars.browserObject, status, result = pageObject.getResources("All")
        utility.execLog("Resources returned :> %s"%str(result))
        for item in result:
            utility.execLog("Searching server %s in %s"%(str(serverIP),str(item)))
            if serverIP in item['IP Address']:
                self.resourceName = item['Resource Name']
                utility.execLog("Serve found %s"%str(self.resourceName))
                break
            
        utility.execLog("Resource to be selected => %s"%str(self.resourceName))
        globalVars.browserObject, status, result_server = pageObject.getPerformanceData(self.resourceName)
        if not status:
            self.failure(result, raiseExc=True)
        if initialState == "ON":
            self.resultToCompare = "1%"
        else:
            self.resultToCompare = "0%"
        if self.resultToCompare in result_server:
            self.succeed("Performance data is displayed for server %s while it is initially in %s state"%(str(serverIP), str(initialState)))
        else:
            self.failure("Performance data is displayed for server %s while it is initially in %s state"%(str(serverIP), str(initialState)))
        
        if initialState == "ON":
            self.nextState = "OFF"
        else:
            self.nextState = "ON"    
        globalVars.browserObject, status, result = pageObject.changeServerState(serverIP, self.nextState)
        if status:
            self.succeed(result)
        else:
            self.failure(result)
            
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        status, result = pageObject.runInventory([self.resourceName], waitForCompletion=True)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        globalVars.browserObject, status, result = pageObject.getResources("All")
        for item in result:
            if serverIP in item['IP Address']:
                self.resourceName = item['Resource Name']
                break
        utility.execLog("Resource to be selected => %s"%str(self.resourceName))
        globalVars.browserObject, status, result_server = pageObject.getPerformanceData(self.resourceName)
        if not status:
            self.failure(result, raiseExc=True)
        
        if "1%" in result_server:
            self.succeed("Performance data is still displayed for server %s while it is in %s state"%(str(serverIP), str(self.nextState)))
        else:
            self.failure("Performance data is not displayed for server %s while it is in %s state"%(str(serverIP), str(self.nextState)))


    def verifyPerformanceMetrics(self):
        """
        Verifies if Performance metrics is displayed
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)        
        globalVars.browserObject, status, result = pageObject.verifyPerformanceMetrics("13")
        if status:
            self.succeed(result)
        else:
            if "unable to find" in result.lower():
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyPerformanceMetrics("12")
        if status:
            if "unable to find" in result.lower():
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure(result, raiseExc=True)
        else:
            if "unable to find" in result.lower():
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
            elif "failed to verify" in result.lower():
                self.failure(result, raiseExc=True)
            else:
                self.succeed(result)
            
    def getOptionDetails_CurrentVersions(self, option):
        """
        Description: open virtual appliance management for update
        """
        if option == "VirtualApplianceManagement":
            pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        globalVars.browserObject, status, result = pageObject.getCurrentAvailableApplianceVersions()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True) 
            
    def verifyUnpinOption(self):
        """
        Verify if pin menu option works
        """
        pageObject = Navigation.Navigation(self.browserObject)
        globalVars.browserObject, status, result = pageObject.changeMenuToPinView()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        globalVars.browserObject, status, result = pageObject.changeMenuToNormalView()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
            
    def verifyASMDropDownMenu(self):
        
        """
        """
        pageObject = Navigation.Navigation(self.browserObject)
        globalVars.browserObject, status, result = pageObject.getMainOptions()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        subMenuList = ["Cancelled", "Critical", "Healthy", "In Progress", "Warning", "My Templates", "Sample Templates", "All Resources", "Server Pools"]
        for item in subMenuList:
            globalVars.browserObject, status, result = pageObject.verifySubMenu(item)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
                
                
    def verifyAllUsersFilter(self, userList):
        utility.execLog("Create Users class object")
        pageObject  = Users.Users(self.browserObject)
        utility.execLog("Load the users page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Filter the dropdown with ALL Users")
        globalVars.browserObject, status, result = pageObject.filterByGroup("All Users")
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Verify users availability")
        for item in userList:
            globalVars.browserObject, status, result = pageObject.verifyUserAvailable(item)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=False)
                
                
    def verifyImportADUsers(self, directoryType, directoryName, directoryUserName, directoryPassword, host, port, protocol, baseDN, directoryFilter, userNameAttr, fNameAttr, lNameAttr, emailAttr,directorySource, searchTerm, importRole, verifySuccess= "pass"):
        utility.execLog("Create Users class object")
        pageObject  = Users.Users(self.browserObject)
        utility.execLog("Load the users page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Creating New Directory")
        globalVars.browserObject, status, result = pageObject.createDirectory(directoryType, directoryName, directoryUserName, directoryPassword, host, port, protocol, baseDN, directoryFilter, userNameAttr, fNameAttr, lNameAttr, emailAttr)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Importing Group to new created directory")
        globalVars.browserObject, status, result = pageObject.importADUsers(directorySource, searchTerm, importRole)
        if verifySuccess == "pass":
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)

    def discoverStorage(self, verifyHealth=True):
        """
        Description: open Resources page 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.discoverResource("Storage", "172.31.32.108", "", "Managed","", "autoStorageEQL", "", "","","","")
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        globalVars.browserObject, status, result = pageObject.getResources("Storage", "Healthy")        
        if not status:
            self.failure(result, raiseExc=True)
            
    def deployClonedTemplate(self,templateName):
        utility.execLog("deployClonedTemplate()..Manager")
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deployClonedTemplate(templateName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def verifyServices(self):
        pageObject = Services.Services(self.browserObject)
        svcList = self.getServices()
        if len(svcList) > 0:
            self.succeed("Able to read Services in 'List' Form :: %s"%str(svcList))
            utility.execLog("Able to read Services in List Form")
        else:
            self.succeed("No Services in 'Services' Page to view in 'List' form")
            utility.execLog("No Services in 'Services' Page to view in 'List' for")      
             
        globalVars.browserObject, status, result = pageObject.verifyAllLink()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
            
    def verifyTemplate(self):
        pageObject = Templates.Templates(self.browserObject)
        
        tempList = self.getTemplates(option="Sample Templates", viewType="List")
        if len(tempList) > 0:
            self.succeed("Able to read 'Sample Templates' in 'List' Form :: %s"%str(tempList))
        else:
            self.succeed("No Templates in 'Sample Templates' Tab to view in 'List' form")  
        
        tempList = self.getTemplates(option="My Templates", viewType="List")
        if len(tempList) > 0:
            self.succeed("Able to read 'My Templates' in 'List' Form :: %s"%str(tempList))
        else:
            self.succeed("No Templates in 'My Templates' Tab to view in 'List' form")
             
        globalVars.browserObject, status, result = pageObject.verifyAllLink()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def verifySettings(self):
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
             
        globalVars.browserObject, status, result = pageObject.verifyAllLink()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True) 
            
    def verifyResourceHealth(self, resourceType):
        """
        Description: open Resources page and verify storage healths
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyResourceHealth(resourceType)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        self.browserObject = globalVars.browserObject
            
    def verifyStaus(self):
        """
        Description: open Resources page 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.discoverResource("Switch", "172.31.63.80", "172.31.63.80", "Managed","", "", "", "","autoIOM", "")
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result =pageObject.verifyStatus("Switches","All","172.31.63.80")
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True) 
            
    def getDashboard_AllLinks(self):
        pageObject = Dashboard.Dashboard(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDashboard_AllLinks()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        self.getSection_Learn()
        
    def verifyStaus_Resources(self):
        """
        Description: open Resources page 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status1, result1 =pageObject.verifyStatus("Dell Chassis","All","172.31.60.238")
        if status1:
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=True)
        globalVars.browserObject, status2, result2 = pageObject.loadPage()
        if status2:
            self.succeed(result2)
        else:
            self.failure(result2, raiseExc=True)    
        globalVars.browserObject, status3, result3 =pageObject.verifyStatus("Dell Chassis","All","172.31.61.84")
        if status3:
            self.succeed(result3)
        else:
            self.failure(result3, raiseExc=True)
             
        globalVars.browserObject, status4, result4 = pageObject.discoverResource("Switch", "172.31.63.48", "172.31.63.48", "Managed","", "", "", "","autoIOM", "")
        if status4:
            self.succeed(result4)
        else:
            self.failure(result4, raiseExc=True)
        globalVars.browserObject, status5, result5 =pageObject.verifyStatus("Switches","All","172.31.63.48")
        if status5:
            self.succeed(result5)
        else:
            self.failure(result5, raiseExc=True) 
            
    def deployService_pastTime(self, templateName, serviceName="Test Service", managePermissions=True, userList=["All"], 
                      negativeScenario=False, deploymentScheduleTime=None, passExpectation=False, deployStatus=False,deployNow=True):
        """
        Description: 
            Deploys a Template
        """
        #Create User
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.deployService_pastTime(templateName, serviceName, managePermissions=managePermissions, 
                                                                    userList=userList,deploymentScheduleTime=deploymentScheduleTime, passExpectation=passExpectation,deployNow=deployNow)
        
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def executeCompleteFlow(self, templateName, storageType, storageName, volumeName, volumeSize="200GB", authType="CHAP", authUser="grpadmin", authPassword="dell1234", iqnOrIP=None, portType="iSCSI", targetStorage="",
                             serverType="Server", flowType="", clusterType="VMWare Cluster", datacenterName="TestDataCenter", clusterName="TestCluster", vmType="vCenter Virtual Machine", fcoe=False):
        """
        """
        utility.execLog("authType = %s"%str(authType))
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Creating template draft")
        globalVars.browserObject, status, result = pageObject.createNewTemplate(templateName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Adding Storage to the template draft %s"%str(templateName))
        pageObject.addStorage(storageType, storageName, volumeName, volumeSize, authType, authUser, authPassword, iqnOrIP, portType=portType, targetStorage=targetStorage)
        
        utility.execLog("Adding Server to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addServer(serverType, flowType=flowType, storageType=storageType, fcoe=fcoe)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Adding Cluster to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addCluster(clusterType, datacenterName, clusterName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Adding VM to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addVM(vmType)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Adding Application to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addApplication()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Publishing template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.publishTemplate()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
            
    def executeCompleteFlowFCESXI(self, templateName, storageType, storageName, volumeName, volumeSize="200GB", authType="CHAP", authUser="grpadmin", authPassword="dell1234", iqnOrIP=None,
                             serverType="Server", clusterType="VMWare Cluster", datacenterName="TestDataCenter", clusterName="TestCluster", vmType="vCenter Virtual Machine", verifyDeployment= False):
        """
        """
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Creating template draft")
        globalVars.browserObject, status, result = pageObject.createNewTemplate(templateName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Adding Storage to the template draft %s"%str(templateName))
        pageObject.addStorage(storageType, storageName, volumeName, volumeSize, authType, authUser, authPassword, iqnOrIP)
        
        utility.execLog("Adding Server to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addServerFC(serverType)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
          
        utility.execLog("Adding Cluster to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addCluster(clusterType, datacenterName, clusterName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
          
        utility.execLog("Adding VM to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addVM(vmType)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
          
        utility.execLog("Adding Application to the template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.addApplication()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
              
        utility.execLog("Publishing template draft %s"%str(templateName))
        globalVars.browserObject, status, result = pageObject.publishTemplate()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def verifyServiceDeploymentStatus(self, serviceName):
        utility.execLog("Verifying the deployment status")
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        counter = 1
        flag = True
        while counter <60 and flag:
            globalVars.browserObject, status, result = pageObject.getServiceStatus(serviceName)
            if not status:
                utility.execLog("Service deployment failed with reason %s exiting while loop"%str(result))
                flag = False
                self.failure(result, raiseExc=True)
            else:
                if "in-progress" in result:
                    utility.execLog("Service deployment still in progress waiting for 120 seconds and continuing with while loop")
                    time.sleep(120)
                    counter = counter + 1
                else:
                    utility.execLog("Service deployment successful hence exiting while loop")
                    flag = False
                    self.succeed(result)
                    return True
                    
                    
    def verifyvCenterDetailsOfDeployedService(self, serviceName, serviceDeployment= True):
        """
        """
        if serviceDeployment:
            utility.execLog("Service deployment is successful verifying vCenter Details")         
            pageObject = Services.Services(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
                
            globalVars.browserObject, status, vCenterDetails = pageObject.getServiceDetails(serviceName) 
            if status:
                self.succeed("Service Details %s successfully fetched."%str(vCenterDetails))
            else:
                self.failure(vCenterDetails, raiseExc=True)
                
            pageObject = Resources.Resources(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)    
            
            globalVars.browserObject, status, result = pageObject.verifyServerVCenterDetails(vCenterDetails["ClusterIP"], vCenterDetails["DatacenterName"], vCenterDetails["ClusterName"], vCenterDetails["ServerOSIp"], vCenterDetails["VMHostName"])    
            if status:
                self.succeed(result)
            else:
                self.failure(result)
        else:
            utility.execLog("Service deployment failed hence no need to verify vCenter Details")              
                
    def addAddOnFile(self, verify=True):
        pageObject = AddOnModules.AddOnModules(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        utility.execLog("Successfully Loaded AddOn Module page")
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        if verify:  
            globalVars.browserObject, status, result = pageObject.uploadAddOnFile()
        else:
            globalVars.browserObject, status, result = pageObject.addAddOnFile()
            
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def duplicateAddOnFile(self):
        pageObject = AddOnModules.AddOnModules(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.uploadDuplicateAddOnFile()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def deleteAddOnModule(self):
        pageObject = AddOnModules.AddOnModules(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.uploadAddOnFile()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def invalidAddOnFileName(self):
        pageObject = AddOnModules.AddOnModules(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.invalidAddOnFileName()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def invalidAddOnPupetVersion(self):
        pageObject = AddOnModules.AddOnModules(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.invalidAddOnFileName("docs\\adam_linux_postinstall_1.zip")
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def exportAndImportTemplate(self,templateName):
        """
        Description: open Template page 
        """
#         navObject = Navigation.Navigation(self.browserObject)
#         navObject.selectOption("Resources")
#         time.sleep(5)
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("exportAndImportTemplate()..Manager")
        globalVars.browserObject, status1, result1 = pageObject.exportTemplate(templateName)
        if status1:
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=True)
        globalVars.browserObject, status1, result1 = pageObject.importTemplate(templateName)
        if status1:
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=True)
            
    def verifySpecificStandadrdUserTemplate(self,templateName):
        """
        Description: open Resources page 
        """
        navObject = Navigation.Navigation(self.browserObject)
        navObject.selectOption("Resources")
        time.sleep(5)
        
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status1, result1 = pageObject.creatTemplate_specificStandarUser(templateName)
        if status1:
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=True)        

    def restoreNowFromSetting(self):
        pageObject = BackupAndRestore.BAR(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.restoreNowFromSetting()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def restoreNowFromGettingstarted(self):
        utility.execLog("restoreNowFromGettingstarted")
          
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
          
        globalVars.browserObject, status1, result1=pageObject.restoreNowFromGettingstarted()
        if status1:
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=True)
              
            
    def exportDataVirtualIdentityPools(self):
        utility.execLog("exportDataVirtualIdentityPools()..Manager")
        pageObject = VirtualIdentityPools.VirtualIdentityPools(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(10)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.exportData()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

    def getCustomBundle_CompilaneReport(self, option):
        """
        Description: upload and check version of user bundle(custom)
        """
        if option == "Repositories":
            pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        globalVars.browserObject, status, result = pageObject.getCustomBundle_CompilaneReport()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)
    
    def getCustomBundle_CompilaneReports(self, option):
        """
        """    
        if option == "Resources":
            pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(20)
        globalVars.browserObject, status, result = pageObject.getCustomBundle_CompilaneReports(option)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)
            
    def getVcenterDetailsMng(self, vcenterIP):
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getVCenterDetails(vcenterIP)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def cleanup_data(self):
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        pageObject.delete_service()

    def getBackUp_ScheduleTime(self, option):
        """
        Description: Open backup And Restore 
        """
        if option == "BackupAndRestore":
            pageObject = BackupAndRestore.BAR(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        globalVars.browserObject, status, result = pageObject.getBackUp_ScheduleTime()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)

    def getCustomBundle_AddORexist(self, option):
        """
        Description: Edit bundle should show the file thats already exists, and should allow to Add new
        """
        if option == "Repositories":
            pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        globalVars.browserObject, status, result = pageObject.getCustomBundle_AddORexist()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)
            
    def setNTPTimezone(self, timeZoneId, primaryNTP, secondaryNTP=None):
        """
        Description: 
            API to Set Time Zone and NTP
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.setNTPTimezone(timeZoneId, primaryNTP, secondaryNTP=secondaryNTP)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def verifyShowWelcomeScreenOption(self):
        """
        Description: 
            Verify Enable/disable the "Show welcome screen on next launch" in Getting Started.
        """
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.validatePage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, options = pageObject.verifyShowWelcomeScreenOption()
        if not status:
            self.failure(result, raiseExc=True)
        self.succeed("Available options for 'Show welcome screen on next launch' in Getting Started : %s"%str(options))
        opts = ("Visible", "Enable", "Disable")
        failList = set(opts).difference(set(options))
        if len(failList) > 0:
            self.failure("Options missing on 'Show welcome screen on next launch' in Getting Started : %s"%str(failList), raiseExc=True)
        self.succeed("User can View, Enable and Disable 'Show welcome screen on next launch' in Getting Started")
        self.browserObject = globalVars.browserObject
    
    def getLogs(self, readAll=False, readFromTime=None):
        """
        Description:
            Fetches All Logs
        """
        pageObject = Logs.Logs(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails(readAll=readAll, readFromTime=readFromTime)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    def getNetworks(self):
        """
        Description:
            Fetches All Networks
        """
        pageObject = Networks.Networks(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    def clearTroubleShootingBundle(self):
        """
        Description:
            Deletes existing Trouble Shooting Bundles
        """
        path = os.path.join(os.getcwd(), utility.logsdir)
        for root, dirs, files in os.walk(path):
            for rfile in filter(lambda x: re.match("Dell_ASM*", x), files):
                fileName = os.path.join(path, rfile)
                os.remove(fileName)
                self.succeed("Removed existing Trouble Shooting Bundle '%s'"%fileName)
    
    def generateTroubleShootingBundle(self, option="Settings", serviceName=None):
        """
        Description: 
            API to Generate TroubleShooting Bundle
        """
        if option == "Services":
            pageObject = Services.Services(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if not status:
                self.failure(result, raiseExc=True)
            globalVars.browserObject, status, result = pageObject.generateTroubleShootingBundle(serviceName)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if not status:
                self.failure(result, raiseExc=True)
            globalVars.browserObject, status, result = pageObject.generateTroubleShootingBundle()
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def verifyTroubleShootingBundle(self):
        """
        Description:
            Verifies Trouble Shooting Bundle
        """
        verified = False
        path = os.path.join(os.getcwd(), utility.downloadDir)
        for root, dirs, files in os.walk(path):
            for rfile in filter(lambda x: re.match("Dell_ASM*", x), files):
                self.succeed("Verified Generated Trouble Shooting Bundle '%s'"%rfile)
                verified = True
                path = os.path.join(os.getcwd(), utility.downloadDir)
                size = utility.getFileSize(os.path.join(path, rfile))
                if size == "":
                    self.failure("Failed to verify Size of Generated Trouble Shooting Bundle '%s'"%rfile, raiseExc=True)
                else:
                    self.succeed("Verified Size of Generated Trouble Shooting Bundle Name:'%s' Size:'%s'"%(rfile, size))
        if not verified:
            self.failure("Failed to verify Generated Trouble Shooting Bundle..Existing Files :: %s"%str(files), raiseExc=True)
    
    def exportDataToCSV(self, option="Templates"):
        """
        Description: 
            API to Export Data to CSV
        """
        if option == "Templates":
            pageObject = Templates.Templates(self.browserObject)
        elif option == "Logs":
            pageObject = Logs.Logs(self.browserObject)
        elif option == "Services":
            pageObject = Services.Services(self.browserObject)
        elif option == "Resources":
            pageObject = Resources.Resources(self.browserObject)
        elif option == "Networks":
            pageObject = Networks.Networks(self.browserObject)
        else:
            pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.exportData()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def clearExportedCSVFiles(self):
        """
        Description:
            Deletes existing Exported CSV Files
        """
        path = os.path.join(os.getcwd(), utility.downloadDir)
        for root, dirs, files in os.walk(path):
            for rfile in filter(lambda x: re.match("serviceTemplates*", x), files):
                fileName = os.path.join(path, rfile)
                os.remove(fileName)
                self.succeed("Removed existing CSV File '%s'"%fileName)
    
    def verifyExportedFile(self, option="serviceTemplates"):
        """
        Description:
            Verifies Exported CSV file
        """
        verified = False
        path = os.path.join(os.getcwd(), utility.downloadDir)
        for root, dirs, files in os.walk(path):
            for rfile in filter(lambda x: re.match("%s*"%option, x), files):
                if rfile.endswith(".csv"):
                    self.succeed("Verified Exported CSV File '%s'"%rfile)
                else:
                    self.failure("Exported File format is not CSV '%s'"%rfile, raiseExc=True)
                verified = True
                path = os.path.join(os.getcwd(), utility.downloadDir)
                size = utility.getFileSize(os.path.join(path, rfile))
                self.exportedFile = os.path.join(path, rfile)
                if size == "":
                    self.failure("Failed to verify Size of Exported CSV File '%s'"%rfile, raiseExc=True)
                else:
                    self.succeed("Verified Size of Exported CSV File Name:'%s' Size:'%s'"%(rfile, size))
        if not verified:
            self.failure("Failed to verify Exported CSV File..Existing Files :: %s"%str(files), raiseExc=True)
        
    def getExportedData(self, option="Templates"):
        """
        Description:
            Verifies Exported CSV file content
        """
        exportedData = []
        result, status = utility.readCsvFile(self.exportedFile)
        if not status:
            self.failure(result, raiseExc=True)
        if len(result) > 0:
            header = result[0]
            for index in range(1, len(result)):
                exportedData.append(dict(zip(header, result[index])))
        return exportedData
    
    def getGettingStartedOptions(self, actualOptions=[], disableOptions=[], enableOptions=[]):
        """
        Description: 
            Verify Options under Getting Started page
        """
        optionDict = {}
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getOptions()
        if status:
            optionDict = result
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if len(optionDict) > 0:
            self.succeed("Options under GettingStarted Page %s"%(str(optionDict)))
        else:
            self.failure("No Options available under Page 'GettingStarted'", raiseExc=True)
        failList = [opt for opt in actualOptions if opt not in optionDict.keys()]
        if len(failList) > 0:
            self.failure("Some Options are not available to verify on page 'GettingStarted' :: Faillist %s"%(str(failList)), raiseExc=True)            
        failList = [opt for opt in disableOptions if optionDict[opt] == "Enabled"]
        if len(failList) > 0:
            self.failure("Options which should be 'Disabled' on page 'GettingStarted' are 'Enabled' :: Faillist %s"%(str(failList)), raiseExc=True)
        failList = [opt for opt in enableOptions if optionDict[opt] == "Disabled"]
        if len(failList) > 0:
            self.failure("Options which should be 'Enabled' on page 'GettingStarted' are 'Disabled' :: Faillist %s"%(str(failList)), raiseExc=True)
    
    def getTemplateConfiguration(self, fileName):
        """
        Description: 
            Reads JSON file and returns component details
        """
        result = utility.getTemplateConfiguration(fileName)        
        return result        
    
    def buildTemplate(self, components, publishTemplate=True, verifyNTP=False, editClone= False, OS_Image="",validatePoolAndManualServers=False, negativeScenario=False,importConfigFromRefServer=False):
        """
        Creates a Template from Template Configuration
        """
        networks = self.getNetworks()
        resourceList = self.getResources()
        serverPoolDetailList=self.getServerPoolDetails(components["Server"]["ServerPoolName"])
        components["Server"]["serverPoolDetail"]=serverPoolDetailList

        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)


        if validatePoolAndManualServers:
            globalVars.browserObject, status, result,inValidServerList = pageObject.buildTemplate(components, resourceList, networks, publishTemplate, verifyNTP, editClone, OS_Image=OS_Image,validatePoolAndManualServers=validatePoolAndManualServers,importConfigFromRefServer=importConfigFromRefServer)
            if status:
                self.succeed(result )
                self.browserObject = globalVars.browserObject
                return inValidServerList
            else:
                self.failure(result)
        
        else :
            globalVars.browserObject, status, result = pageObject.buildTemplate(components, resourceList, networks, publishTemplate, verifyNTP, editClone, OS_Image=OS_Image)
        if not negativeScenario:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.failure(result, raiseExc=True)
            else:
                if "Failed to Select User" in result:
                    self.succeed(result)
                elif "The storage volume name is already in use" in result:
                    self.color_of_device_icon("storage", "yellow")
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        
        
    def scaleUpService(self, serviceName, components, verifyNTP=False,staticIP=False,manualServer=False):
        """
        Add resources to a service to scaleup it
        """
        networks=None
        if staticIP:
            self.staticIP=True
#             networks=self.getNetworks()
            
        #For Manual Server Entry
        if manualServer:
            self.manualServer=True
        
        networks = self.getNetworks()
        resourceList = self.getResources()        
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True) 
        globalVars.browserObject, status, result = pageObject.viewServiceDetails(serviceName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.scaleupService(serviceName, components, resourceList, networks, verifyNTP,staticIP=staticIP,manualServer=manualServer)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject

    def scaleDownService(self, serviceName, components):
        """
        Delete resources to scale-down the service
        """
#         networks = self.getNetworks()
#         resourceList = self.getResources()        
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True) 
        globalVars.browserObject, status, result = pageObject.viewServiceDetails(serviceName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.scaleDownService(serviceName, components)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject  

    def getCustomBundle_DeleteCustom(self, option):
        """
        Description:  Delete user bundle(custom)
        """
        if option == "Repositories":
            pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        globalVars.browserObject, status, result = pageObject.getCustomBundle_DeleteCustom()
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Able to view Information under %s Tab :: %s"%(option, str(result)))
        else:
            self.failure("Unable to view Information under %s Tab :: %s"%(option, str(result)), raiseExc=True)

    def verifyServerPool(self, components):
        """
        Verify a Server Pool already exists
        """
        result = False
        serverPoolName = components["Server"]["ServerPoolName"]
        if serverPoolName.lower() != "global":
            serverPools = self.getServerPools()
            self.succeed("Existing Server Pools : %s"%serverPools)
            exists = [pool for pool in serverPools if pool["Server Pool Name"] == serverPoolName]            
            if exists:
                self.succeed("Server Pool with Name '%s' already exists so deleting existing one"%serverPoolName)
                result = True

        self.browserObject = globalVars.browserObject
        return result

    def createServerPool(self, components=None, userList=[], serverPoolName=None, serverList=[], 
                         addAllServers=False, addFirstHealthyServer=False):
        """
        Creates a Server Pool
        """
        if components:
            serverPoolName = components["Server"]["ServerPoolName"]
            instances = components["Server"]["Instances"]
            if serverPoolName == "":
                serverPoolName = "autoPool"
            if serverPoolName.lower() != "global":
                serverPools = self.getServerPools()
                self.succeed("Existing Server Pools : %s"%serverPools)
                exists = [pool for pool in serverPools if pool["Server Pool Name"] == serverPoolName]            
                if exists:
                    self.succeed("Server Pool with Name '%s' already exists so deleting existing one"%serverPoolName)
                    self.deleteServerPool(serverPoolName)            
                serverList = components["Server"]["ServersIPForPool"]
                if len(serverList) <= 0:
                    resList = self.getResources(resourceType="Servers")
                    if globalVars.configInfo["Information"]["version"] >= "8.3":
                        serverList = [server["IP Address"] for server in resList if server["Managed State"] == "Managed" and
                                            server["Deployment Status"] == "Not in use" and server["Health"].strip() == "GREEN"]
                    else:
                        serverList = [server["IP Address"] for server in resList if server["State"] == "Available" and server["Health"].strip() == "GREEN"]
                    serverList = serverList[:instances]
                    self.succeed("Creating Server Pool '%s' with Servers '%s'"%(serverPoolName, str(serverList)))
                pageObject = Resources.Resources(self.browserObject)
                globalVars.browserObject, status, result = pageObject.loadPage()
                if not status:
                    self.failure(result, raiseExc=True)                             
                globalVars.browserObject, status, result = pageObject.createServerPool(serverPoolName, serverList, userList)
                if status:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
                self.browserObject = globalVars.browserObject
        else:
            pageObject = Resources.Resources(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if not status:
                self.failure(result, raiseExc=True)                             
            globalVars.browserObject, status, result = pageObject.createServerPool(serverPoolName, serverList, userList, addAllServers, addFirstHealthyServer)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
            self.browserObject = globalVars.browserObject
            
    def deleteServerPool(self, serverPoolName):
        """
        Deletes a Server Pool
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)                             
        globalVars.browserObject, status, result = pageObject.deleteServerPool(serverPoolName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject

    def verifyResourceAvailability(self, components):
        """
        Verify Resource Availability
        """
        resList =  self.getResources()
        self.succeed("Available Resources '%s'"%resList)
        #Verify Storage Dependency
        instances = components["Storage"]["Instances"]
        if instances > 0:
            storageType = components["Storage"]["Type"]
            storageRes = [res for res in resList if res["Manufacturer /Model"].strip() == storageType.strip()]
            if len(storageRes) > 0:
                reqIP = components["Storage"]["IPAddress"] 
                if reqIP  != "":
                    reqStorage = [res for res in storageRes if res["IP Address"].strip() == reqIP.strip()]
                    if len(reqStorage) <= 0:
                        self.failure("Storage component '%s' with IP '%s' specified in JSON is not 'Available' :: Available Resources ->'%s'"%(storageType, reqIP, storageRes),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
                    else:
                        if reqStorage[0]["Deployment Status"] != "Available" and reqStorage[0]["Health"].strip() != "GREEN":
                            self.failure("Required Storage component '%s' State is not 'Available' :: Resource Info ->'%s'"%(storageType, reqStorage),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
                else:
                    reqStorage = [res for res in storageRes if res["State"].strip() == "Available" or res["Health"].strip() == "GREEN"]
                    if len(reqStorage) <= 0:
                        self.failure("Storage component '%s' State is not 'Available' :: Resource Info ->'%s'"%(storageType, storageRes),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure("Required Storage component '%s' is not available :: Available Resources ->'%s'"%(storageType, resList),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
            self.succeed("Required Storage component '%s' is available :: Available Resources ->'%s'"%(storageType, storageRes))
        else:
            self.succeed("No Storage Components in Template")
        #Verify Servers Dependency
        instances = components["Server"]["Instances"]
        serverPoolName = components["Server"]["ServerPoolName"]
        if instances > 0 and serverPoolName.upper() !="Global".upper():
            serverList = components["Server"]["ServersIPForPool"]
            serverRes = [res for res in resList if "USE" in res["Deployment Status"].upper()]
            if len(serverRes) > 0:
                if len(serverList) > 0:
                    for server in serverList:
                        reqServer = [res for res in serverRes if res["IP Address"].strip() == server.strip()]
                        if len(reqServer) <= 0:
                            self.failure("Server IP '%s' specified in JSON is not 'Available' :: Available Resources ->'%s'"%(server, serverRes),
                                 resultCode=BaseClass.OMITTED, raiseExc=True)
                        else:
                            if reqServer[0]["Deployment Status"] != "Not in use" or reqServer[0]["Health"].strip() != "GREEN":
                                self.failure("Required Server IP '%s' State is not 'Available' :: Resource Info ->'%s'"%(server, serverRes),
                                 resultCode=BaseClass.OMITTED, raiseExc=True)
                else:
                    reqServer = [res for res in serverRes if res["Deployment Status"].strip() == "Not in use" and res["Health"].strip() == "GREEN"]
                    if len(reqServer) < instances:
                        self.failure("Required Servers '%s' are not 'Available' :: Available Resources ->'%s'"%(instances, serverRes),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure("Required Servers '%s' are not available :: Available Resources ->'%s'"%(instances, resList),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
            self.succeed("Required Servers '%s' are available :: Available Resources ->'%s'"%(instances, serverRes))
        else:
            self.succeed("No Server Components in Template")
        #Verify Cluster Dependency
        instances = components["Cluster"]["Instances"]
        if instances > 0:
            clusterType = components["Cluster"]["Type"]
            if clusterType.lower() == "vmware":
                clusterRes = [res for res in resList if res["Resource Name"].lower().strip() == "vcenter"]
            else:
                clusterRes = [res for res in resList if res["Resource Name"].lower().strip() == "scvmm"]
            if len(clusterRes) > 0:
                reqIP = components["Cluster"]["IPAddress"] 
                if reqIP  != "":
                    reqCluster = [res for res in clusterRes if res["IP Address"] == reqIP.strip()]
                    if len(reqCluster) <= 0:
                        self.failure("Cluster component '%s' with IP '%s' specified in JSON is not 'Available' :: Available Resources ->'%s'"%(clusterType, reqIP, clusterRes),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
                    else:
                        if reqCluster[0]["Deployment Status"] != "Available":
                            self.failure("Required Cluster component '%s' State is not 'Available' :: Resource Info ->'%s'"%(clusterType, reqCluster),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
                else:
                    reqCluster = [res for res in clusterRes if res["Deployment Status"] == "Available"]
                    if len(reqCluster) <= 0:
                        self.failure("Cluster component '%s' State is not 'Available' :: Resource Info ->'%s'"%(clusterType, clusterRes),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure("Required Cluster component '%s' is not available :: Available Resources ->'%s'"%(clusterType, resList),
                             resultCode=BaseClass.OMITTED, raiseExc=True)
            self.succeed("Required Cluster component '%s' is available :: Available Resources ->'%s'"%(clusterType, clusterRes))
        else:
            self.succeed("No Cluster Components in Template")
            
    def getResourceValidationDetails(self, serviceName, portViewOnly=False):
        """
        Description: Navigate to Resources Details Page --> Get Resource Details for a Deployed Service 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, resourcesResult = pageObject.getResourcesDeployed(serviceName)
        if status:
            utility.execLog("Deployed Resources: %s"%str(resourcesResult))
        else:
            self.failure(resourcesResult, raiseExc=True)
            
        if portViewOnly:
            return resourcesResult
        else:
            try:
                globalVars.browserObject, status, result = pageObject.clearFilterService(serviceName)
                if not status:
                    self.failure(result, raiseExc=True)
                self.succeed(result)
                globalVars.browserObject, status, VMResult = pageObject.getAllVCenterDetails(resourcesResult['VM Manager'][0]['IP'])
                if status:
                    utility.execLog("Data Center Details: %s"%str(VMResult))
                else:
                    self.failure(VMResult, raiseExc=True)
                return resourcesResult, VMResult
            except:
                utility.execLog("No VM Manager exists in service: {}".format(serviceName))
                VMResult = []
                return resourcesResult, VMResult
    
    def getServiceValidationDetails(self, serviceName, detailRequired=None):
        """        
        Description: Navigate to Service Details Page --> Get Service Details for a Deployed Service
        """
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            self.failure(result, raiseExc=True)
        
        if detailRequired == "Status":
            depStatus = pageObject.getServiceValDetails(serviceName, depStatus=True)
            return depStatus   
        
        if detailRequired == None:
            ID, vmList, OSIPList, volumeList, hostName = pageObject.getServiceValDetails(serviceName)
            return ID, vmList, OSIPList, volumeList, hostName

    def getNetworkIPValidationDetails(self, serviceName, networkDetails, serverCount, vmCount, OSIPList):
        """        
        Description: Navigate to Network Page --> Get Network Details for a Deployed Service
        """
        pageObject = Networks.Networks(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            self.failure(result, raiseExc=True)

        time.sleep(2)
            
        networkDetailsJSON = getattr(globalVars, networkDetails)
        networkDetailsDict = json.loads(networkDetailsJSON)

        networkList = set()
        try:
            for interfaceIndex in range(0, len(networkDetailsDict)):
                interface = "interface{}".format(interfaceIndex + 1)
                if networkDetailsDict[interface]["fabricType"] == "Ethernet":
                    for port in networkDetailsDict[interface]["Ports"]:
                        for portIndex in range(0, len(networkDetailsDict[interface]["Ports"][port])):
                            for network in networkDetailsDict[interface]["Ports"][port]["{}".format(portIndex + 1)]:
                                networkList.add(network)
        except Exception as e:
            utility.execLog("Resource Page reloaded '%s'"%str(e))

#        networkList = ["OS Installation", "Hypervisor Management", "SAN [iSCSI]", "Hypervisor Migration", "Public LAN"]
        globalVars.browserObject, status, result = pageObject.serviceNetworkIPValidation(serviceName, networkList, serverCount, vmCount, OSIPList)
        return status, result
    
    def validateStorage(self, serviceName, storageIP, storageType, volumeList):
        """        
        Description: Navigate to Resources Page --> Get Storage Details for a Deployed Service
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.clearFilterService(serviceName)
        if not status:
            self.failure(result, raiseExc=True)
        self.succeed(result)
        globalVars.browserObject, status, deployedStorage = pageObject.getStorageDetails(storageIP, storageType)
        error = []
        if status:
            for volumeName in volumeList:
                if (volumeName not in deployedStorage):
                    error.append(True)
                else:
                    error.append(False)
        else:
            self.failure(deployedStorage, raiseExc=True)
        if (len(error) == len(volumeList)):
            if True in error:
                return False
            else:
                return True
        else:
            self.failure("Unable to verify all the volumes in the Storage Center")
            
    def validateStaticIP(self, IPMap):
        pageObject = Templates.Templates(self.browserObject)
        staticIPList = pageObject.getStaticIPList()
        SIPError = []
        IPMapNW = IPMap.keys()
        for nw in IPMapNW:
            nwList = IPMap[nw]
            staticNWList = staticIPList[nw]
            for ip in staticNWList:
                if ip not in nwList:
                    SIPError.append(True)            
        if True in SIPError:
            return False
        else:
            return True

    def validateManualServer(self, deployedServerList):
        """        
        Description: Validation of manually selected Servers for deployment
        """
        pageObject = Templates.Templates(self.browserObject)
        manualSelectedServer = pageObject.getManualServerList()
        MSError = []
        for server in manualSelectedServer:
            if server in deployedServerList:
                MSError.append(False)
            else:
                MSError.append(True)            
        if True in MSError:
            return False
        else:
            return True
        
    def selectServerForPortView(self, serviceName, serverName):
        """
        Description: Navigate to Resources Page --> Return the Resource Detail Page of Deployed Server 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        time.sleep(2)
        
        globalVars.browserObject, status, result = pageObject.getServersPortView(serviceName, serverName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        return status
    
    def getServerOS(self,serviceName):
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        globalVars.browserObject, status, result = pageObject.viewServiceDetails(serviceName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)    
            
        globalVars.browserObject, status, result = pageObject.getServerOS(serviceName)
        return status
     
    def validatePortView (self, serviceName):
        """
        Description: Navigate to Resources Details Page --> Port View Tab --> Validate Port View  
        """
        pageObject = Portview.Portview(self.browserObject)
        error_PortView = []
        
        esxiServerExits=self.getServerOS(serviceName)
        
        if(self.deployedResources == None):
            self.deployedResources = self.getResourceValidationDetails(serviceName, portViewOnly=True)
        
        serverKeys = self.deployedResources['Servers'].keys()
        for i in xrange(0, len(serverKeys)):
            utility.execLog("Navigating To and Validating Port-View for %s: "%self.deployedResources['Servers'][serverKeys[i]]['Asset'])
            self.succeed("Navigating To and Validating Port-View for %s: "%self.deployedResources['Servers'][serverKeys[i]]['Asset'])
            if(self.selectServerForPortView(serviceName, self.deployedResources['Servers'][serverKeys[i]]['Asset'])):
                globalVars.browserObject, status, result = pageObject.viewPortView()
                if status:
                    self.succeed(result)
                else:
                    self.failure(result, raiseExc=True)
        
                globalVars.browserObject, error, result = pageObject.validatePortView(esxiServerExits)
                if error:
                    error_PortView.append(True)
                else:
                    error_PortView.append(False)
        
        if True in error_PortView:
            utility.execLog("Error in validating Port View. Check Logs Reports for specific errors")
            return False
        else:
            utility.execLog("Successfully validated Port View for all deployed Servers")
            return True
        
    def checkBaremetalPostNIC(self, serviceName):
        """
        Description: Performs Post OS NIC Configuration Validation Steps on a Deployed Baremetal Service and reports if error exists.
        """
        #Navigate to BareMetal Deployment Service Page
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            self.failure(result, raiseExc=True)
            
        pageObject.viewServiceDetails(serviceName)
        
        utility.execLog("Starting to validate BareMetal Post-OS NIC Teaming")
        bmNICValidationError = []
        #Check:1 --> The Workload IP is displayed only in bond0.33 section
        hostType = "Workload"
        HOST = "172.31.33.160" #HardCoded
        wlType = "Static" #HardCoded
        
        #Getting Workload VLAN
        wl = HOST.split(".")
        wlIP = "bond0."+wl[2]
        self.succeed(wlIP)
        bondFiles = ['bond0', 'em1', 'em2', 'lo']
        bondFiles.append(wlIP)
        
        for option in bondFiles:
            COMMAND = "ifconfig "+option
            status, result = self.connectSSH(hostType, HOST, COMMAND)
            if status:
                if option == wlIP:
                    if (HOST in result):
                        bmNICValidationError.append(False)
                        self.succeed("BMVC1:Success --> IP %s correctly displayed in the %s section."%(HOST, option))
                    else:
                        bmNICValidationError.append(True)
                        self.failure("BMVC1:Error --> IP %s incorrectly not displayed in the %s section"%(HOST, option))
                else:
                    if (HOST not in result):
                        bmNICValidationError.append(False)
                        self.succeed("BMVC1:Success --> IP %s correctly not displayed in the %s section."%(HOST, option))
                    else:
                        bmNICValidationError.append(True)
                        self.failure("BMVC1:Error --> IP %s incorrectly displayed in the %s section"%(HOST, option))
            else:
                self.failure(result, raiseExc=True)
        if True in bmNICValidationError:
            self.failure("BMVC1:IP has been configured incorrectly for Baremetal Deployment %s"%serviceName)
        else:
            self.succeed("BMVC1:Successfully verified IP configuration for Baremetal Deployment %s"%serviceName)
            
        #Check:2 --> NIC Teaming Validation Checks
        del bmNICValidationError[:]
        for option in bondFiles:
            COMMAND = "cat /etc/sysconfig/network-scripts/ifcfg-"+option
            status, result = self.connectSSH(hostType, HOST, COMMAND)
            if status:
                self.succeed("Successfully verified that /etc/sysconfig/network-scripts/ifcfg-%s file exists."%option)
                if option == wlIP:
                    if wlType == "Static":
                        if ('BOOTPROTO=none' in result):
                            bmNICValidationError.append(False)
                            self.succeed("BMVC2:Success --> Successfully verified NIC Teaming for bond file %s with %s workload"%(option, wlType))
                        else:
                            bmNICValidationError.append(True)
                            self.failure("BMVC1:Error --> Error in verifying NIC Teaming for bond file %s with %s workload"%(option, wlType))
                    elif wlType == "DHCP":
                        if ('BOOTPROTO=dhcp' in result):
                            bmNICValidationError.append(False)
                            self.succeed("BMVC2:Success --> Successfully verified NIC Teaming for bond file %s with %s workload"%(option, wlType))
                        else:
                            bmNICValidationError.append(True)
                            self.failure("BMVC1:Error --> Error in verifying NIC Teaming for bond file %s with %s workload"%(option, wlType))
                            
                elif option == 'em1' or option == 'em2':
                    if ('MASTER=bond0' in result):
                        bmNICValidationError.append(False)
                        self.succeed("BMVC2:Success --> Successfully verified NIC Teaming for bond file %s with %s workload"%(option, wlType))
                    else:
                        bmNICValidationError.append(True)
                        self.failure("BMVC1:Error --> Error in verifying NIC Teaming for bond file %s with %s workload"%(option, wlType))
            else:
                self.failure(result, raiseExc=True)
        if True in bmNICValidationError:
            self.failure("BMVC1:Error in verifying NIC Teaming for Baremetal Deployment %s"%serviceName)
        else:
            self.succeed("BMVC1:Successfully verified NIC Teaming for Baremetal Deployment %s"%serviceName)
        
    def validateDeployment(self, serviceName):
        """
        Description: Performs Validation Steps on a Deployed Service (not Baremetal) and returns false if error exists.
        """
        #Validation Steps Error List
        validationError = []
        storageArray = True
        """
        If self.components > 0, we already have a template. Otherwise, we need to find the template.
        """        
        #Fetching Template Details
        if not len(self.components):
            fileName = globalVars.jsonMap["esxieql"]        
            self.components = self.getTemplateConfiguration(fileName)
            if len(self.components) > 0:
                self.succeed("Able to read from JSON '%s' -> Content :: %s"%(fileName, self.components))
                utility.execLog("Able to read from JSON '%s' -> Content :: %s"%(fileName, self.components))
            else:
                self.failure("Failed to read from JSON '%s'"%(fileName), raiseExc=True)
        #hostNameTemplate = str(self.components["Server"]["HostNameTemplate"]).lower()
        
        #Get Details from Service Page
        deploymentID, vmList, OSIPList, volumeList, hostName = self.getServiceValidationDetails(serviceName)
        vmCount = len(vmList)
        volCount = len(volumeList)
        
        #Get Deployed Resources Details and VM Center Details from Resources Page
        self.deployedResources, VMDetails = self.getResourceValidationDetails(serviceName)
        
        #VC2. Servers in Deployed State
        serverHostnameList = []
        serverIPList = []
        serverAssetList = []
        serverKeys = self.deployedResources['Servers'].keys()
        for i in xrange(0, len(self.deployedResources['Servers'])):
            if globalVars.configInfo["Information"]["version"] >= "8.3":
                if self.deployedResources['Servers'][serverKeys[i]]['Deployment Status']=="In Use":
                    self.succeed("VC2:Server %s successfully deployed" %(self.deployedResources['Servers'][serverKeys[i]]['Asset']))         
                else:
                    validationError.append(True)
                    self.failure("VC2:Server %s not deployed" %(self.deployedResources['Servers'][serverKeys[i]]['Asset']))
            else:
                if (self.deployedResources['Servers'][serverKeys[i]]['State']=="Deployed"):
                    self.succeed("VC2:Server %s successfully deployed" %(self.deployedResources['Servers'][serverKeys[i]]['Asset']))         
                else:
                    validationError.append(True)
                    self.failure("VC2:Server %s not deployed" %(self.deployedResources['Servers'][serverKeys[i]]['Asset']))
             
            serverHostnameList.append(self.deployedResources['Servers'][serverKeys[i]]['Hostname'])
            serverIPList.append(self.deployedResources['Servers'][serverKeys[i]]['IP'])
            serverAssetList.append(self.deployedResources['Servers'][serverKeys[i]]['Asset'])
        uniqueServersHostname = set(serverHostnameList)
        
        #VC2A. Validation of Manually Selected Servers for Deployment
        if self.manualServer:
            self.succeed("VC2A: Verifying Manual Selection of Servers")
            if self.validateManualServer(serverAssetList):
                self.succeed("VC2A:Successfully verified Manual Selection of Servers")
            else:
                validationError.append(True)
                self.succeed("VC2A:Error in verifying Manual Selection of Servers")
            
        #VC3: Check for Volumes on Storage Center
        try:
            storageIP=self.deployedResources['Storage']['IP']
            storageType=self.deployedResources['Storage']['Model']

        #If the storage array uses FC, don't check for SAN connectivity
            if self.deployedResources["Storage"]["PortType_Compellent"] == "FibreChannel":
                storageArray = False

            if(self.validateStorage(serviceName, storageIP, storageType, volumeList)):
                self.succeed("VC3: Volumes successfully created on Storage Center")
            else:
                validationError.append(True)
                self.failure("VC3: Error in creating volumes on Storage Center")
        except:
            utility.execLog("No storage exists in service: {}".format(serviceName))
            storageArray = False
        #VC4: Verify Cluster, VMs & Hosts
        dcTemplate = "skAutoDC11" #Will fetch from the Template JSON
        clTemplate = "skAutoCL11" #Will fetch from the Template JSON
        try:
            dcKeys = VMDetails.keys()
            dcFound = False
            clFound = False
            for dc in dcKeys:
                if (dc==dcTemplate):
                    dcFound = True
                    self.succeed("VC4: DataCenter %s successfully verified" %dcTemplate)
                    clKeys=VMDetails[dc]["Clusters"]
                    for cl in clKeys:
                        if (cl == clTemplate):
                            clFound = True
                            self.succeed("VC4: Cluster %s successfully verified" %clTemplate)
                            hostsKeys = VMDetails[dc]["Clusters"][cl][0].keys()
                            for ip in OSIPList:
                                if ip in hostsKeys:
                                    self.succeed("VC4: Host %s successfully verified" %ip)
                                    vmVCList = VMDetails[dc]["Clusters"][cl][0][ip]
                                    for vm in vmList:
                                        if vm in vmVCList:
                                            self.succeed("VC4: VM %s successfully verified" %vm)
                                        else:
                                            validationError.append(True)
                                            self.failure("VC4:VM %s not found."%vm)    
                                else:
                                    validationError.append(True)
                                    self.failure("VC4:Host %s not found."%ip)  
            if (not clFound):
                validationError.append(True)
                self.failure("VC4:Cluster %s not found."%clTemplate)      
            if (not dcFound):
                validationError.append(True)
                self.failure("VC4:DataCenter %s not found."%dcTemplate)
        except:
            utility.execLog("No VM Manager exists in service: {}".format(serviceName))
        #VC5: Verify Switches        

        #VC6: Verify Razor Policies
        hostType = "Appliance"
        HOST = globalVars.configInfo['Appliance']['ip'] #HOST = self.parseConfigFile()
#        for i in xrange(0, len(self.deployedResources['Servers'])):
#            COMMAND = "/opt/puppet/bin/razor policies policy-"+self.deployedResources['Servers'][serverKeys[i]]['Hostname']+"-"+deploymentID
        for i in xrange(0, len(hostName)):
            COMMAND = "/opt/puppet/bin/razor policies policy-" + hostName[i] + "-" + deploymentID
            status, result = self.connectSSH(hostType, HOST, COMMAND)
            if status:
                if (result!=""):
                    self.succeed("VC6:Razor Policy successfully created.%s" %result)
                else:
                    validationError.append(True)
                    self.failure("VC6:No Razor Policy Found for %s" %(hostName[i]))
            else:
                self.failure(result, raiseExc=True)

        try:    
            for vm in vmList:
                COMMAND = "/opt/puppet/bin/razor policies policy-"+vm+"-"+deploymentID
                status, result = self.connectSSH(hostType, HOST, COMMAND)
                if status:
                    if (result!=""):
                        self.succeed("VC6:Razor Policy successfully created:%s" %result)
                    else:
                        validationError.append(True)
                        self.failure("VC6:No Razor Policy Found for %s" %vm)
                else:
                    self.failure(result, raiseExc=True)
        except:
            utility.execLog("No VM exists in service: {}".format(serviceName))
        #VC7: Verify Network Connectivity
        for ip in serverIPList:
            hostType = "Appliance"
            HOST = globalVars.configInfo['Appliance']['ip'] #HOST = self.parseConfigFile()
            COMMAND = "ping -c 2 " + ip
            status, result = self.connectSSH(hostType, HOST, COMMAND)
            if status:
                output = "64 bytes from " + ip
                if (output in result):
                    self.succeed("VC7:Network Connectivity successfully verified for Server %s --> %s" %(self.deployedResources['Servers'][serverKeys[i]]['Hostname'], result))
                else:
                    validationError.append(True)
                    self.failure("VC7:Error in verifying Network Connectivity for Server %s" %(self.deployedResources['Servers'][serverKeys[i]]['Hostname']))
            else:
                self.failure(result, raiseExc=True)
                            
        #VC8: Verify Assigned IPs
        networkDetails = self.components["Server"]["Networks"]
        serverCount = len(self.deployedResources['Servers'])
        """
        If storageArray == False, there is no storage in the template, or storage uses FC, set serverCount = -1 to indicate
        we don't need to verify the SAN count.
        """
        status, IPMap = self.getNetworkIPValidationDetails(serviceName, networkDetails, serverCount, vmCount, OSIPList)
        if status:
            self.succeed("VC8:Verified Assigned Network IPs")
            if self.staticIP:
                self.succeed("VC8a:Verifying Static IP Assignment")
                if self.validateStaticIP(IPMap):
                    self.succeed("VC8a:Successfully verified assigned Static IPs")
                else:
                    validationError.append(True)
                    self.failure("VC8a:Error in verifying assigned Static IPs")                       
        else:
            validationError.append(True)
            self.failure("VC8:Error in verifying assigned IPs")

        #VC9: Verify HostName for Uniqueness and Host Name Template
        if (len(serverHostnameList)==len(uniqueServersHostname)):
            self.succeed("VC9:Verified that HostNames are unique")
        else:
            validationError.append(True)
            self.failure("VC9:HostNames are not unique")

        #VC10: Verify VLANs/PortView 
        #if(self.validatePortView(serviceName)):
        #    self.succeed("VC10: Successfully validated Port View & VLANs for all deployed Servers")
        #else:
        #    validationError.append(True)
        #    self.failure("VC10: Error in validating Port View & VLANs. Check Logs Reports for specific errors")
        
        #VC11: Verify Application
        
        #Final Verification             
        if True in validationError:
            self.failure("Error in validating the Deployment. Check Logs Reports for specific errors", raiseExc=True)
            #hostType = "Appliance"
            #COMMAND = "/opt/asm-deployer/scripts/capture_deployment_for_jira.sh -d "+deploymentID
            #status, result = self.connectSSH(hostType, HOST, COMMAND)
            #self.succeed(result)
            return False
        else:
            return True
    
    def checkForValidation(self, serviceName):
        self.logDesc("Validating Deployment %s"%serviceName)
        self.succeed("Checking for Overall Health and Deployment Status for %s" %serviceName)
        
        overallServiceHealth = str(self.getDeploymentStatus(serviceName)[0]['Status'])
        detailRequired = "Status"
        deploymentStatus = self.getServiceValidationDetails(serviceName=serviceName, detailRequired=detailRequired)
        
        #Validation of the Deployment
        #VC1. Service is Deployed
        if (overallServiceHealth == "Success"):
            self.succeed("VC1: Overall Health for Service %s is Healthy" %serviceName)
            self.succeed("VC1: Deployment Status for Service %s is Deployed" %serviceName)
            if self.validateDeployment(serviceName):
                self.logDesc("Successfully validated the Service %s"%serviceName)
            else:
                self.failure("Error in validating the Service %s. Check Logs Reports for specific errors."%serviceName)
                
        elif (overallServiceHealth == "Failed" or overallServiceHealth == "Unknown"):
            if (overallServiceHealth == "Failed"):
                self.succeed("VC1: Overall Health for Service %s is Error" %serviceName)       
                if (deploymentStatus['Deployment State'] == "Deployed"):
                    self.succeed("VC1: Deployment Status for Service %s is Deployed" %serviceName)
                    if self.validateDeployment(serviceName):
                        self.logDesc("Successfully validated the Service %s"%serviceName)
                    else:
                        self.failure("Error in validating the Service %s. Check Logs Reports for specific errors."%serviceName)
                else:
                    self.failure("Service Deployment Failed. Check Deployment Logs for specific errors.")
                self.succeed("Resource Health: %s"%deploymentStatus['Resource Health'])
                self.succeed("Firmware Compliance: %s"%deploymentStatus['Firmware Compliance'])
                self.failure("The deployment process failed, a server or storage array has a resource state of Error, or all server or storage components in the service are in a warning state.")  
            else:
                self.succeed("VC1: Overall Health for Service %s is Warning" %serviceName)         
                if (deploymentStatus['Deployment State'] == "Deployed"):
                    self.succeed("VC1: Deployment Status for Service %s is Deployed" %serviceName)
                    if self.validateDeployment(serviceName):
                        self.logDesc("Successfully validated the Service %s"%serviceName)
                    else:
                        self.failure("Error in validating the Service %s. Check Logs Reports for specific errors."%serviceName)
                else:
                    self.failure("Service Deployment Failed. Check Deployment Logs for specific errors.")
                self.succeed("Resource Health: %s"%deploymentStatus['Resource Health'])
                self.succeed("Firmware Compliance: %s"%deploymentStatus['Firmware Compliance'])
                self.failure("A server is no longer compliant with the desired firmware level, or a server or storage array has a health status of warning.")
        else:
            self.logDesc("Deployment Status for Service %s is Unknown or In Progress."%serviceName)
            
    def translateJSONValue(self,JMAP,Language):
        """
        Description: Navigate to TranslateJSONString Page, which can translate JSON string to foreign language
        """
        pageObject = TranslateJSONString.TranslateJSONString(self.browserObject)
        globalVars.browserObject, status, result = pageObject.translateJSONPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        newJSONMAP =pageObject.translateLocalizedValue(JMAP,Language)
        return newJSONMAP
    
    def addAllTypeLicenseMan(self, license_file_path, message=""):
        """
        Description:
             Add License Management based on license file path
        """
      
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result1 = pageObject.addLicense(license_file_path, message)
        
        if status:
            
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=True)
            
    def addAndverifyLicenseType(self, license_file_path, message="", licenseType = None, numOfResources = None, expirationDate = None):
        """
        Description:
             Add and Verify License Management based on message and license type
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Different values Before Uploading the License is : ")
            
        status1, licenseTypeBef, numOfResourcesBef, numOfUsedResourcesBef, numOfAvailResourcesBef, expirationDateBef = pageObject.verifyLicenseTypeVirtuApp()
		# status1, licenseTypeBef, numOfResourcesBef, numOfUsedResourcesBef, numOfAvailResourcesBef = pageObject.verifyLicense()
        
        time.sleep(3)           
        globalVars.browserObject, status2, result1 = pageObject.addLicense(license_file_path, message)
        
        if status2:
            
            self.succeed(result1)
        else:
            self.failure(result1, raiseExc=False)
            
        time.sleep(3)
        utility.execLog("Different values Before Uploading the License is : ")
            
        status3, licenseTypeAF, numOfResourcesAF, numOfUsedResourcesAF, numOfAvailResourcesAF, expirationDateAF = pageObject.verifyLicenseTypeVirtuApp()
		# status3, licenseTypeAF, numOfResourcesAF, numOfUsedResourcesAF, numOfAvailResourcesAF = pageObject.verifyLicense()
        
        
        if status3 and licenseType:
            utility.execLog("The value of License Type before Add License :  %s"%licenseTypeBef)
            utility.execLog("The value of License Type After Add License :  %s"%licenseTypeAF)
            if licenseType == licenseTypeAF:
                self.succeed("Successfuly verifed Licnese Type After Adding License")
            else:
                self.failure("Failed to verifed Licnese Type After Adding License", raiseExc=False)
               
        if status3 and numOfResources:
            utility.execLog("The value of Number of Resources before Add License :  %s"%numOfResourcesBef)
            utility.execLog("The value of Number of Resources after Add License :  %s"%numOfResourcesAF)
            if numOfResources == numOfResourcesAF:
                self.succeed("Successfuly verifed Licnese Type After Adding License")
            else:
                self.failure("Failed to verifed Licnese Type After Adding License", raiseExc=False)
                
        if status3 and expirationDate:
            utility.execLog("The value of Expiration Date before Add License :  %s"%numOfResourcesBef)
            utility.execLog("The value of Expiration Date after Add License :  %s"%numOfResourcesAF)
            if licenseTypeAF == 'Evaluation' and len(expirationDateAF) > 0:
                self.succeed("Successfuly verifed Expiration Date After Adding License")
            elif licenseTypeAF == 'Standard' and len(expirationDateAF)== 0:
                self.succeed("Successfuly verifed Expiration Date After Adding License")
            else:
                self.failure("Failed to verifed Expiration Date  After Adding License", raiseExc=False)
                
    def verifyResourcesInLicenseMgmt(self, resourceAction=""):
        """
        Description:
             Add and Verify License Management based on discover new resourceses and delete new resources
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Different values Before Uploading the License is : ")
            
        status1, licenseTypeBef, numOfResourcesBef, numOfUsedResourcesBef, numOfAvailResourcesBef, expirationDateBef = pageObject.verifyLicenseTypeVirtuApp()
		# status1, licenseTypeBef, numOfResourcesBef, numOfUsedResourcesBef, numOfAvailResourcesBef = pageObject.verifyLicense()
        
        pageObject1 = Resources.Resources(self.browserObject)
        globalVars.browserObject, status2, result2 = pageObject1.loadPage()
        time.sleep(5)
        if status2:
            self.succeed(result2)
        else:
            self.failure(result2, raiseExc=True)
        if resourceAction == 'deleteAction':
            globalVars.browserObject, status4, result4 = pageObject1.deleteResource('HCLDNS07')
            time.sleep(5)
            if status4:
                self.succeed(result4)
            else:
                self.failure(result4, raiseExc=True) 
        elif resourceAction == 'discoverAction':
            globalVars.browserObject, status, result = pageObject1.discoverResource("Chassis", globalVars.chassisIpDiscover, "", "Managed", "Dell PowerEdge iDRAC Default", "", "Dell chassis default", "", "Dell switch default", "")
            time.sleep(5)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True) 
        time.sleep(3)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Different values Before Uploading the License is : ")
            
        status3, licenseTypeAF, numOfResourcesAF, numOfUsedResourcesAF, numOfAvailResourcesAF, expirationDateAF = pageObject.verifyLicenseTypeVirtuApp()
		# status3, licenseTypeAF, numOfResourcesAF, numOfUsedResourcesAF, numOfAvailResourcesAF = pageObject.verifyLicense()
        
        utility.execLog(" Number of Available Resources Before Delete / Discover resources  :  %s"%numOfAvailResourcesBef)
        utility.execLog(" Number of Available Resources After Delete / Discover resource :  %s"%numOfAvailResourcesAF)
        if numOfAvailResourcesBef != numOfAvailResourcesAF:
            self.succeed("Successfuly  verified Number of Available Resources")
        else:
            self.failure("Failed to verified Number of Available Resources", raiseExc=False)
                     
    def addFirmwareRepository(self, option, networkPath=None, localPath=None, makeDefault=False, testConnection=True, testNegative=False, waitAvailable=True):
        """"
        Description: Upload a Catalog
        waitAvailable: default=True, if false; will just upload the catalog and not wait till the catalog is available
        """
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        
        globalVars.browserObject, status, result = pageObject.addFirmwareRepository(option, networkPath, localPath,  makeDefault, testConnection, waitAvailable)
        
        catlogUploadErrorList= ["Catalog file is invalid","Unable to connect to", "Unable to access", "not a valid target path", "The catalog file name is invalid"]
        if not testNegative:
            if status:
                self.succeed("Able to upload catalog %s"%(str(result)))
            else:
                self.failure("Unable to upload catalog %s "%(str(result)), raiseExc=True)
        else:
            if not status:
                self.succeed("Error Loading catalog: %s "%(str(result)))
                errorMessage= False
                for mesg in catlogUploadErrorList:
                    utility.execLog("message %s" %mesg)
                    if mesg in result:
                        utility.execLog("Error message matched   %s" %mesg)
                        errorMessage= True
                        break
                if errorMessage:
                    self.succeed("Correct Error Message displayed : %s "%(str(result)))
                elif not errorMessage:
                    self.failure("Incorrect Error Message displayed %s"%(str(result)))
            else:
                self.failure("Able to Load catalog: Testcase Failed ", raiseExc= True)
    
    def getTestConnection(self,option, networkPath, localPath=""):
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage() 
        
        globalVars.browserObject, status, result = pageObject.getTestConnection(option,networkPath,localPath)
        
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Test Connection : %s" %result)
        else:
            self.failure("Test Connection : %s" %result, raiseExc= True)
            
    
    def verifyDefaultFirmware(self, repositoryName,testNegative=False):
        
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage() 
        
        globalVars.browserObject, status, result = pageObject.verifyDefaultFirmware(repositoryName)
        
        if not testNegative:
            if status:
                self.succeed("Result: %s" %result)
            else:
                self.failure("Result: %s"%result, raiseExc=True)
                
        if testNegative:
            if not status:
                self.succeed("Result %s" %result)
            else:
                self.failure("Result %s"%result, raiseExc=True)
    
    def setDefaultFirmware(self,firmware, negativeTest= False):
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage() 
        globalVars.browserObject, status, result = pageObject.setDefaultFirmware(firmware)
    
        if not negativeTest:
            if not status:
                self.failure(result, raiseExc=True)
                self.browserObject = globalVars.browserObject
            if status:
                self.succeed("Default FirmWare set to : %s" %firmware)
            else:
                self.failure("Default Firmware not set to : %s"%firmware, raiseExc=True)
                
        if negativeTest:
            if not status and ("cannot be made default" in result):
                utility.execLog("result success :%s"%result)
                self.succeed("%s cannot be made default" %firmware)
            else:
                utility.execLog("result failure :%s"%result)
                self.failure("%s firmware set to default"%firmware, raiseExc=True)
       
    
    
    def addStorageCustomBunddle(self,BunddleName,BunddleModel,BunddleVersion):  
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage() 
        globalVars.browserObject, status, result = pageObject.addStorageCustomBunddle(BunddleName,BunddleModel,BunddleVersion)
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        if status:
            self.succeed("Bunddle created : %s" %result)
        else:
            self.failure("Bunddle not created : %s" %result, raiseExc= True)
            
    def addChassisResourceUser(self, chassisIP, userNames, password, enableUsers, userGroups, negativescenario=False, confirmPass= None):
        """
        Description:
            Configure resource
        """
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
             
        utility.execLog("Calling GettingStarted Page add user function")
        globalVars.browserObject, status, result = pageObject.addCmcUser(chassisIP,userNames, password, enableUsers, userGroups, confirmPass)
        if negativescenario:
            utility.execLog("Negative scenario")
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
             
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        if confirmPass!="worngPass":
            
            utility.execLog("Waiting for users to get added.")
                
            try:
                updateJobCompleted=self.verifyJobCompleted("Device Configuration Job")
                retry=1
                while retry<5:
                    if updateJobCompleted:
                        break
                    else:
                        utility.execLog("Wait for Device Configuration Job to complete")
                        time.sleep(300)
                        updateJobCompleted=self.verifyJobCompleted("Device Configuration Job In Progress retry={}".format(retry))
                        retry+=1
            except Exception as e:
                utility.execLog("Device Configuration Job through exce:-{}".format(e))
            
            utility.execLog("Verify the created user")
            time.sleep(300)
            globalVars.browserObject, status, result = pageObject.verifyCmcUserCreated(chassisIP, userNames)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
                
    def addChassisiDracUser(self, serverIP, userNames, password, roles, lanRoles, enableUser, negativescenario=False, confirmPass= None):
        """
        Description:
            Configure resource
        """
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
              
        utility.execLog("Calling GettingStarted Page add user function")
        globalVars.browserObject, status, result = pageObject.addiDracUser(serverIP,userNames, password, roles, lanRoles, enableUser, confirmPass)
        if negativescenario:
            utility.execLog("Negative scenario")
            if not status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        else:
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
               
        if confirmPass!="mismatchPass" :
                 
            utility.execLog("Waiting for users to get added.")
                
            try:
                updateJobCompleted=self.verifyJobCompleted("Device Configuration Job")
                retry=1
                while retry<5:
                    if updateJobCompleted:
                        break
                    else:
                        utility.execLog("Wait for Device Configuration Job to complete")
                        time.sleep(300)
                        updateJobCompleted=self.verifyJobCompleted("Device Configuration Job In Progress retry={}".format(retry))
                        retry+=1
            except Exception as e:
                utility.execLog("Device Configuration Job through exce:-{}".format(e))
                   
            utility.execLog("Verify the created user")
            globalVars.browserObject, status, result = pageObject.verifyiDracUser(serverIP, enableUser,userNames,lanRoles,roles)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
            
    def chassisConfiguration(self, chassisIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName,registeriDracDNS=True):
        """
        """   
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Calling GettingStarted Page add user function")
        globalVars.browserObject, status, iDRACServerIp = pageObject.configureChassis(chassisIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName,registeriDracDNS=registeriDracDNS) 
        if status:
            self.succeed(iDRACServerIp)
        else:
            self.failure(iDRACServerIp, raiseExc=True)
         
        utility.execLog("Waiting for users to get added.") 
        try:
            updateJobCompleted=self.verifyJobCompleted("Device Configuration Job")
            retry=1
            while retry<5:
                if updateJobCompleted:
                    break
                else:
                    utility.execLog("Wait for Device Configuration Job to complete")
                    time.sleep(300)
                    updateJobCompleted=self.verifyJobCompleted("Device Configuration Job In Progress retry={}".format(retry))
                    retry+=1
        except Exception as e:
            utility.execLog("Device Configuration Job through exce:-{}".format(e))
        utility.execLog("Verify Chassis Configuration")
        globalVars.browserObject, status, result = pageObject.verifyChassisCondiguration(chassisIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        utility.execLog("Verify iDRAC_DNS Configuration")
        globalVars.browserObject, status, result = pageObject.verifyiDRAC_DNS(iDRACServerIp,iDracDnsName,registeriDracDNS=registeriDracDNS)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)    

    def discoverChassis(self, resourceStartIP, resourceEndIP, chassisCredential, serverCredential, switchCredential, newCredential=False):
        """
        """ 
        counter=0  
        while(counter<10):
            utility.execLog("Test to check Configure Resource is enable or not, count={}".format(counter))
            try:
                if self.browserObject.find_element_by_id("lnkConfigureResources"):
                    enabledCheck=self.browserObject.find_element_by_id("lnkConfigureResources").get_attribute("disabled")
                    utility.execLog("enabledCheck={}".format(enabledCheck))
                    if not self.browserObject.find_element_by_id("lnkConfigureResources").get_attribute("disabled"):
                        utility.execLog("Configure Resource button is enabled")
                        break
                    else:
                        counter = counter+1
                        time.sleep(120)
            except Exception as e:
                counter = counter+1
                time.sleep(300)
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Discover Resources")
        globalVars.browserObject, status, result = pageObject.discoverResource("Chassis", resourceStartIP, resourceEndIP, "Managed", serverCredential, "", chassisCredential, "", switchCredential, "", newCredential=newCredential)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    def configUpLinkOnly(self,chessIp,uplinkName,portChannel):
        '''
        Description: Verify whether a job  is completed or not
        '''
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed("Able to load Getting Started page")
        else:
            self.failure("unable to open Getting Started page %s"%result)
        
         
        utility.execLog("UpLink Configuration started")
    
        globalVars.browserObject, status, result = pageObject.configUpLinkOnly(chessIp,uplinkName,portChannel)
        if status:
            self.succeed("UpLink configuration complete")
        else:
            self.failure("UpLink configuration failed : %s"%result,raiseExc= True)
            
            
        try:
            utility.execLog("Verify UpLink Job completed or not..... started")
            updateJobCompleted=self.verifyJobCompleted("Device Configuration Job")
            retry=1
            while retry<5:
                if updateJobCompleted:
                    utility.execLog("Job completed")
                    break
                else:
                    utility.execLog("Wait for Device Configuration Job to complete")
                    utility.execLog("Device Configuration Job In Progress retry={}".format(retry))
                    time.sleep(300)
                    updateJobCompleted=self.verifyJobCompleted("Device Configuration Job")
                    retry+=1
            utility.execLog("Device Configuration Job has been completed")
        except Exception as e:
            utility.execLog("Device Configuration Job through exce:-{}".format(e))
            
        status,result=pageObject.verifyUpLinkOnChessis(chessIp)
        if status:
            self.succeed("UpLink configuration completed on chessis")
        else:
            self.failure("UpLink configuration on chessis failed : %s"%result,raiseExc= True)
            
        utility.execLog("Test UPLink on Switch")
        
        for switch in  result:
            status,command_result=self.connectSSH("Switch", switch, "show running-config interface vlan")
            vlan=globalVars.applianceIP.split('.')[2]
            if status:
                if "Vlan "+vlan in command_result:
                    self.succeed("UpLink configuration completed on switch:-{}".format(switch))
            else:
                self.failure("UpLink configuration failed on switch :- {}".format(switch),raiseExc= True)   
            
    def verifyServiceTag(self, serviceTag):
        """
        Description: 
            Verify Service Tag 
        """
        #Verify on Virtual Appliance Management Page 
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getDetails()
        if not status:
            self.failure(result, raiseExc=True)
        if result["ServiceTag"].lower() == serviceTag.lower():
            self.succeed("Successfully Verified Service Tag on Virtual Appliance Management Page :: Actual '%s' Expected '%s'"%(result["ServiceTag"], serviceTag))
        else:
            self.failure("Failed to Verify Service Tag on Virtual Appliance Management Page :: Actual '%s' Expected '%s'"%(result["ServiceTag"], serviceTag), raiseExc=True)
        self.browserObject = globalVars.browserObject
        #Verify on About Page
        pageObject = LandingPage.LandingPage(self.browserObject)
        globalVars.browserObject, status, result = pageObject.getServiceTag()
        if (not status) or (not result):
            self.failure(result, raiseExc=True)
        if result.lower() == serviceTag.lower():
            self.succeed("Successfully Verified Service Tag on About Page :: Actual '%s' Expected '%s'"%(result, serviceTag))
        else:
            self.failure("Failed to Verify Service Tag on About Page :: Actual '%s' Expected '%s'"%(result, serviceTag), raiseExc=True)
        self.browserObject = globalVars.browserObject
    
    def editServiceTag(self, serviceTag):
        """
        Description: 
            Edit Service Tag on Virtual Appliance Management Page
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.editServiceTag(serviceTag)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        self.browserObject = globalVars.browserObject
        
    def changeIOSwitchState(self,ServerIPPool,IoSwitchIp,resourceType="All",resourceState="UnManaged"):
        """
        Description:
            Fetches Resources 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(40)
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.changeIOSwitchState(resourceType,ServerIPPool,IoSwitchIp,resourceState)
        
        if not status:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        return result
    
    def enableDisableDHCPSettingMgr(self, resourceAction="", subnet="", netMask="", startIpAddres="", endIpAddress="", gateway="", dns="", domain=""):
        """
        Description:
            Enable DHCP setting and do a deployment 
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("getting DHCP  status before Edit  : ")
            
        globalVars.browserObject, status, result1 = pageObject.getDHCPStatus()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("enabling / disabling  DHCP setting  : %s"%resourceAction)
            
        globalVars.browserObject, status, result2 = pageObject.enableDisableDHCPSetting(resourceAction, subnet=subnet, netMask=netMask, startIpAddres=startIpAddres, endIpAddress=endIpAddress, gateway=gateway, dns=dns, domain=domain)
        time.sleep(5)
        if status:
            self.succeed(result2)
        else:
            self.failure(result2, raiseExc=True)
            
        utility.execLog(" getting DHCP  status After Edit  : ")
            
        globalVars.browserObject, status, result3 = pageObject.getDHCPStatus()
        time.sleep(5)
        if status:
            self.succeed(result3)
        else:
            self.failure(result3, raiseExc=True)
        
            
        
        if result1 != result3:
            self.succeed("Successfuly Enable Disable DHCP Setting")
        else:
            self.failure("Failed to Enable Disable DHCP Setting", raiseExc=False)
            
    def editBackupSettings(self,option):
        '''
        Edit Backup setting
        '''
        pageObject = BackupAndRestore.BAR(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Edit Backup Setting")
        globalVars.browserObject, status, result = pageObject.editBackupSettings(option)
        if status:
            self.succeed(result)
            return result
        else:
            self.failure(result, raiseExc=True)  
            
    def createBackup(self):
        '''
        Create Backup for ASM build
        '''
        pageObject = BackupAndRestore.BAR(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Creating Backup files for ASM buld")
        globalVars.browserObject, status, result = pageObject.createBackup()
        if status:
            self.succeed(result)
            return result
        else:
            self.failure(result, raiseExc=True) 
            
            
    def validateBackupCreated(self,pathOption):
        '''
        Validated Backup file created
        '''
        try:
            globalVars.configInfo = utility.readConfig(globalVars.configFile)
            fileCount=0
            BackupDateTime= self.browserObject.find_element_by_id("lastBackupDate").text
            if pathOption=="nfs":
                backupPath = globalVars.configInfo['Appliance']['backuppath']
                backupPathStr= backupPath.split(":")
                HOST=backupPathStr[0]
                backupDirectory= backupPathStr[1].rsplit('/',1)
                command= "ls -lrt "+backupDirectory[0]
                
                status, result =self.connectSSH("Appliance",HOST,command)
                if status:
                    fileList = result.split("\n")
                    for index in range(0,len(fileList)):
                        if ("Dell_Appliance_Backup" in fileList[index]):
                            fileDetails=fileList[index].rsplit(" ",1)
                            filename=fileDetails[-1]
                            command1 = "ls -lrt "+backupDirectory[0]+"/"+filename
                            status, result= self.connectSSH("Appliance",HOST,command1)
                            fileSize= result.split(" ")[4]
                            month= result.split(" ")[5]
                            day= result.split(" ")[6]
                            dateCreated= month+" "+day
                            
                            timeCreated= result.split(" ")[7]
                            tdelta= datetime.datetime.strptime(timeCreated,"%H:%M")-timedelta(minutes=2)
                            timeCreated= tdelta.strftime("%I:%M %p")
                            
                            if status and fileSize>1000 and (dateCreated and timeCreated) in BackupDateTime:
                                fileCount= fileCount+1
                    if fileCount>0:
                        return self.browserObject,True,"Backup file created"
                    else:
                        return self.browserObject,False,"Backup file not created"  
                else:
                    return self.browserObject, False, "Unable to access appliance through ssh"
                
            elif pathOption=="cifs":
                listFiles=os.listdir(globalVars.ASMBackupPath["cifs"])
                for index in range(0,len(listFiles)):
                    if ("Dell_Appliance_Backup" in listFiles[index]):
#                         createdDateTime=time.ctime(os.path.getctime(globalVars.ASMbackupPath["cifs"]+"\\"+listFiles[index]))
#                         createDate= createdDateTime.split(" ")
#                         dateCreated=str(createDate[1]+" "+createDate[2])
#                         createTime= createdDateTime.split(" ")[3]
#                         createTime=createTime.rsplit(":",1)[0]
#                         tdelta= datetime.strptime(createTime,"%H:%M")-timedelta(minutes=2)
#                         timeCreated= tdelta.strftime("%I:%M %p")
                        fileSize= os.path.getsize(globalVars.ASMBackupPath["cifs"] + "\\" + listFiles[index])
#                         if (dateCreated and timeCreated) in BackupDateTime and fileSize>1000:
                        if dateCreated in BackupDateTime and fileSize>1000:
                            fileCount=fileCount+1
                        
                if fileCount>0:
                    self.succeed("Backup file created Succesfully")
                    return True
                else:
                    return False
        except Exception as e:
            return self.browserObject, False, e
        
    def validateRestore(self):
        try:
            time.sleep(5)
            restoreValidation=[]
            globalVars.applianceIP = globalVars.configInfo['Appliance']['ip']
            self.login(username="admin", password="admin", newInstance=False)
            discoveredResources = self.browserObject.find_element_by_xpath(".//*[@id='discoveryprogress']/li[1]/span").text
            pendingResources= self.browserObject.find_element_by_xpath(".//*[@id='discoveryprogress']/li[2]/span").text
            
            if int(discoveredResources)>0 or int(pendingResources)>0:
                restoreValidation.append(True)
                utility.execLog("Discovered Resources Restored")
            else:
                restoreValidation.append(False) 
                utility.execLog("Discovered Resources not Restored")
            self.browserObject.find_element_by_id("lnkNetworks").click()
            time.sleep(5)
            utility.execLog("verifying Network configuration")
            trs= self.browserObject.find_elements_by_xpath(".//*[@id='networks_table']/tbody/tr")
            for trow in trs:
                networkType=trow.find_element_by_xpath("./td[4]").text
                if networkType=="OS Installation":
                    PXEVlan=trow.find_element_by_xpath("./td[3]/span").text
                    self.browserObject.find_element_by_id("close_network_form").click()
                    break
                
            if PXEVlan==globalVars.applianceIP.split('.')[2]:
                restoreValidation.append(True)
                utility.execLog("Network Configuration Resotred")
            else:
                restoreValidation.append(False)
                utility.execLog("Network Configuration not Resotred")
            
            pageObject = Logs.Logs(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
            
            globalVars.browserObject, status, logList= pageObject.getDetails()
            if "Inventory finished for device" in str(logList):
                restoreValidation.append(True)
                utility.execLog("Network Configuration Resotred")
            else:
                restoreValidation.append(False)
                utility.execLog("Network Configuration not Resotred")
                
            if False not in restoreValidation:
                utility.execLog("Successfully Restored appliance")
                return True 
            else:
                utility.execLog("Restore Validation failed")
                return False
        
        except Exception as e:
            utility.execLog(e)
            self.failure(e)
            return False
        
    def addSwitch_CustomBundles(self,option, catalogName=None, pathInfoJson=None):
        '''
        add switch custom bundles
        '''
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

        utility.execLog("add switch custom bundles")
        globalVars.browserObject, status, result = pageObject.addSwitch_CustomBundles(option, catalogName, pathInfoJson)
        if status:
            self.succeed("Able to add bundle %s"%result)
            return result
        else:
            self.failure("Unable to add Bundle %s"%result, raiseExc=True)
            
    def verifyResourceModelAvailblity(self,resourceType,resourceModel):
        '''
        verify Resource with model name is available in the resource page
        '''
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.verifyResourceModelAvailblity(resourceType,resourceModel)
        if status:
            self.succeed(result)
        else:
            if "Resource Model %s is Not Available"%resourceModel in result:
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True) 
            else:
                self.failure("Error : %s"%result, raiseExc=True)
            
    def getReourceModelComplainceStatus(self, resourceType, resourceModel):
        '''
        Fetches Compliance Status of the resource model
        '''
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getReourceModelComplainceStatus(resourceType, resourceModel)
        if status:
            self.succeed(result)
            return result
        else:
            self.failure(result, raiseExc=True)
            
    def verifyApplicationOrder(self, components):
        """
        Creates a Template from Template Configuration
        """
        networks = self.getNetworks()
        resourceList = self.getResources()        
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)                             
        globalVars.browserObject, status, result = pageObject.changeApplicationInstallOrder(components, resourceList, networks)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject

    
    def selectResource(self, resourceToSelect):
        """
        Description: 
            Selects a Resource
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Selects for resource specified")

        globalVars.browserObject, status, result = pageObject.selectResource(resourceToSelect)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

        self.browserObject = globalVars.browserObject

        return status, result

    
    def checkNTPSettings(self):
        """
        Description: Gets Preferred NTP Server & Secondary NTP Server from Virtual Appliance Management Page.
        Checks the setting with config.ini file and sets the NTP Servers if returned NULL/INCORRECT.
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        globalVars.browserObject, status, result = pageObject.getNTPTimezone()
        if status:
            self.succeed("Successfully fetched NTP Server Settings")

            pNTP = result['Preferred']
            sNTP = result['Secondary']

            callSetNTP = False
            timeZone = globalVars.configInfo['Appliance']['timezone']
            pConNTP = globalVars.configInfo['Appliance']['ntpserver']
            sConNTP = globalVars.configInfo['Appliance']['secondaryntp']
            
            if (pNTP!=""):
                # Comparing if NTP Servers are set correctly
                self.succeed("Comparing if NTP Servers are set correctly")
                if (pNTP == pConNTP):
                    self.succeed("Preferred/Primary NTP Server is set correctly")
                else:
                    self.failure("Preferred/Primary NTP Server is not set correctly. Resetting it...")
                    callSetNTP = True
                
                if (sNTP == sConNTP):
                    self.succeed("Secondary NTP Server is set correctly")
                else:
                    self.failure("Secondary NTP Server is not set correctly. Resetting it...")
                    callSetNTP = callSetNTP or True
            else:
                callSetNTP = True
            
            if callSetNTP:
                self.setNTPTimezone(timeZone, pConNTP, sConNTP)
            else:
                self.succeed("Successfully verified Preferred/Primary NTP Server and Secondary NTP Server")
        else:
            self.failure(result, raiseExc=True)
            
    def verifyMultipleNTP(self, serviceName):
        """
        Description:
        Verification for Multiple NTP settings
        """
        
        #Navigate to BareMetal Deployment Service Page
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            self.failure(result, raiseExc=True)
            
        pageObject.viewServiceDetails(serviceName)
        
        utility.execLog("Starting to validate Multiple NTP Settings for %s"%serviceName)
        multipleNTPValidationError = []
        
        mNTPList = []
        mNTPString = str(globalVars.configInfo['Appliance']['multiplentp'])
        
        mNTPList = mNTPString.split(",")
        utility.execLog("Multiple NTP Server List: %s"%mNTPList)
        
        #Check:1 --> To validate on RHEL/CentOS. Look at /etc/ntp.conf and one should see entries 
        hostType = "OS"
        HOST = "172.31.33.220" #HardCoded
        COMMAND = "cat /etc/ntp.conf | grep ^server"
        status, result = self.connectSSH(hostType, HOST, COMMAND)
        if status:
            for ntp in mNTPList:
                if (ntp in result):
                    multipleNTPValidationError.append(False)
                    self.succeed("Success --> NTP Server %s correctly displayed in the ntp.conf file"%ntp)
                else:
                    multipleNTPValidationError.append(True)
                    self.failure("Error --> NTP Server %s not displayed in the ntp.conf file"%ntp)
        else:
            self.failure(result, raiseExc=True)
            
        #Check:2 --> To run ntpq -pn to see if one the NTP Server is Active
        activeNTP = False
        COMMAND = "ntpq -pn | grep ^*"
        status, result = self.connectSSH(hostType, HOST, COMMAND)
        if status:
            for ntp in mNTPList:
                if (ntp in result):
                    activeNTP = True
                    self.succeed("Success --> NTP Server %s is Active"%ntp)
            if (activeNTP == False):
                multipleNTPValidationError.append(True)
                self.failure("Error --> No NTP Server is Active")           
        else:
            self.failure(result, raiseExc=True)
            
        if True in multipleNTPValidationError:
            self.failure("Error in verifying Multiple NTP Servers for Baremetal Deployment %s"%serviceName, raiseExc=True)
        else:
            self.succeed("Successfully verified Multiple NTP Servers for Baremetal Deployment %s"%serviceName)
            
    def chassisConfigurationIOM(self, chassisIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName):
        """
        """   
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Calling GettingStarted Page add user function")
        
        globalVars.browserObject, status, result = pageObject.configureChassisIOM(chassisIP, chassisName) 
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
          
        utility.execLog("Waiting for users to get added.")
        time.sleep(60)
           
        utility.execLog("Verify the created user")
        globalVars.browserObject, status, result = pageObject.verifyChassisCondigurationLogs(chassisIP, chassisName, chassisDnsName, locDetailsDc, locDetailsAsile, locDetailsRack, locDetailsRs, iDracDnsName, IoModuleHostName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def validateIOM(self, swichIp, version):
        """
        """   
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.viewComplianceReportSwitch(swichIp, "Switches", version)    
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
    def blockIOMConfiguration(self, chassisIP, chassisName,upLinkCheked,DefinedUplink,ports):
        """
        """   
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
        utility.execLog("Calling GettingStarted Page add user function")

        
        globalVars.browserObject, status, result = pageObject.configureChassisIOM(chassisIP, chassisName,upLinkCheked=upLinkCheked ,DefinedUplink=DefinedUplink,ports=ports) 
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def checkSwitchVersion(self, switchIp, version):
        """
        Verifies Switch Version
        """   
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.viewComplianceReportSwitch(switchIp, "Switches", version) 
        if result=="Current version is higher than 9.6 please downgrade the FW":
            self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
        elif status and result not in "Current version is higher than 9.6 please downgrade the FW":
            self.succeed(result)
        else:
            if 'Resource is not available' in result:
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure(result, raiseExc=True)
            
    def verifyeditADUserPasswordField(self, user):
        """
        Description: 
            Edit Local User
        """
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.verifyeditADUserPasswordField(user)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def filterUserTableByGroup(self, group, user):
        """
        """
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.filterUserTableByGroup(group, user)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def verifyADUserRole(self, user, role):
        """
        """
        pageObject = Users.Users(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)             
        globalVars.browserObject, status, result = pageObject.verifyADUserRole(user, role)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def cloneTemplate(self, templateName, templateType):
        """
        Creates a clone template
        """
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)                             
        utility.execLog("Creating clone template")
        globalVars.browserObject, status, result= pageObject.cloneTemplate(templateName, templateType)
        if status:
            self.succeed(result)
        else:
            self.failure(result,raiseExc=True)
            
    def changeServiceRepository(self, serviceName, repositoryName):
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result)
        utility.execLog("Changing the software/firmware repository of service %s"%serviceName)
        globalVars.browserObject, status, result = pageObject.changeServiceRepository(self, serviceName, repositoryName)
        if status:
            self.succeed("Able to change software/firmware repository for service %s"%serviceName)
        else:
            self.failure("Unable to change software/firmware repository sfor service %s"%serviceName, raiseExc= True)
        
    def updateResourceFirmware(self, serviceName):
        """
        """
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result)
        utility.execLog("Updating Resources of %s"%serviceName)
        globalVars.browserObject, status, result= pageObject.updateResourceFirmware(serviceName)
        if status:
            if "Update Resources button is disabled" in result:
                self.succeed("All resources for %s are already in compliance state--update not needed")
            else:
                #Wait for update to complete
                result = self.getDeploymentStatus(serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        else:
            self.failure("Update Resources of service %s failed %s"%(serviceName, result), raiseExc= True)
            
    def getFirmwareList(self):
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result)
        utility.execLog("Listing the Firmware Packages in Repository Page")
        globalVars.browserObject, status, result = pageObject.getFirmwareList()
        if status:
            self.succeed("Able to get firmware packages list from Repository page")
            firmwarePackageList = result
            return firmwarePackageList
        else:
            self.failure("Unable to get list of firmware packages: %s"%result)
            
    def exportAllServices(self):
        """
        Description: 
        verify whether service file is exported.
        """
        pageObject = Services.Services(self.browserObject)

        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
         
        #Export file of Services
        globalVars.browserObject, status, result = pageObject.exportAllServices()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
            
    def setRepoPath(self):
        """
        Description: Set the Update Repository Path based on the ASM Version
        """
        pageObject = VirtualApplianceManagement.VirtualApplianceManagement(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(5)
        globalVars.browserObject, status, result = pageObject.setRepoPath()
        if status:
            self.succeed("Able to Set Update Repository Path")
        else:
            self.failure("Unable to Set Update Repository Path :: %s"%str(result), raiseExc=True)
        time.sleep(5)
            
    def addOSRepo(self, verifyOSRepo=True):
        """
        Description: To add OS Repo to ASM appliance
        verifyOSRepo = False; if verification of OSRepo is not required to check if it's Available (mainly used for Testcase_ApplianceSetup)
        """
        failList = []
        self.browserObject = globalVars.browserObject

        utility.execLog("Add Repository Class Object")
        pageObject  = Repositories.Repositories(self.browserObject)
        utility.execLog("Load The Repositories Page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

        resourceConfig = utility.readExcel(globalVars.inputFile, "Repositories")
        if not resourceConfig:
            return self.browserObject, False, resourceConfig

        for resource in resourceConfig:
            if verifyOSRepo:
                globalVars.browserObject, status, result = pageObject.deleteOSRepository(resource)
            globalVars.browserObject, status, result = pageObject.addOSRepository(resource, verifyOSRepo)
            if not status:
                self.failure(result)
                failList.append(resource["Repository Name"])
            else:
                if verifyOSRepo:
                    globalVars.browserObject, status, result = pageObject.verifyOSRepository(resource)
                    if not status:
                        self.succeed(result)
                        failList.append(resource["Repository Name"])

        if len(failList) == 0:
            self.succeed("Successfully Added OS Image Repository for all repositories :: %s" % resourceConfig)
        else:
            self.failure("Failed to Add OS Image Repository :: %s"%(failList), raiseExc=True)
            
    def discoverMultipleResources(self, option="Discovery"):
        """
        Description:
            Discover Multiple Resources
        """
        resourceConfig = utility.readExcel(globalVars.inputFile, option)
        if not resourceConfig:
            return self.browserObject, False, resourceConfig
        
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        resourceCount=0
        self.browserObject = globalVars.browserObject
        failList = []
        serverCredential = storageCredential = vcenterCredential = switchCredential = chassisCredential = scvmmCredential = emCredential = serverPool = cserverCredential = cswitchCredential = ""
        
        globalVars.browserObject, status, result = pageObject.loadIdentifyResources()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        for resource in resourceConfig:
            if resource["Type"] == "" or resource["StartIP"] == "" or (resource["ServerCre"] == "" and  
                        resource["ChassisCre"] == "" and resource["SwitchCre"] == "" and resource["VCenterCre"] == ""
                        and resource["StorageCre"] == "" and resource["EMCre"] == "" and resource["SCVMMCre"]==""):
                self.failure("Some of the Resource Information is not provided :: %s"%str(resource))
            
            manageResource = resource["ResourceState"]
            
            if resource["Type"] == "Server":
                serverCredential = resource["ServerCre"]
            elif resource["Type"] == "Storage":
                storageCredential = resource["StorageCre"]
            elif resource["Type"] == "Chassis":
                chassisCredential = resource["ChassisCre"]
                serverCredential = resource["ServerCre"]
                switchCredential = resource["SwitchCre"]
                cserverCredential = resource["ServerCre"]
                cswitchCredential = resource["SwitchCre"]
            elif resource["Type"] == "vCenter":
                vcenterCredential = resource["VCenterCre"]
            elif resource["Type"] == "Switch":
                switchCredential = resource["SwitchCre"]
            elif resource["Type"] == "SCVMM":
                scvmmCredential = resource["SCVMMCre"]
            elif resource["Type"] == "Element Manager":
                emCredential = resource["EMCre"]
            if resource["ServerPool"] != "" and (resource["Type"]== "Server" or resource["Type"] == "Chassis"):
                serverPool = resource["ServerPool"]
            else:
                serverPool = ""
        
            globalVars.browserObject, status, result = pageObject.discoverMultipleResource(resource["Type"], resource["StartIP"],
                        resource["EndIP"], manageResource, serverCredential, storageCredential, chassisCredential, vcenterCredential,
                        switchCredential, scvmmCredential, emCredential, resourceCount, resourcePool=serverPool)
            if not status:
                self.failure(result)
                failList.append(resource["Type"] + "-" + resource["StartIP"] + ":" + resource["EndIP"])
            else:
                self.succeed("Successfully added Resource Discovery Details :: %s"%resource)
            resourceCount=resourceCount+1
            
        if len(failList) == 0:
            self.succeed("Successfully added Discovery details for all Resources :: %s"%(resourceConfig))
            globalVars.browserObject, status, serviceTagList = pageObject.finishMultipleResourceDiscovery(resource["StartIP"], chassisCredential,
                                                                                    cserverCredential, cswitchCredential, serverCredential)
            if status:
                self.succeed(serviceTagList)
                return serviceTagList
            else:
                self.failure(result, raiseExc=True)
        else:
            self.failure("Failed to add Discovery details for some of the Resources :: %s"%(failList), raiseExc=True)
            
    def createPostInstalls(self):
        """
        Description: To create Linux and Windows post-install files in ASM Appliance
        """
        try:
            ssh = paramiko.SSHClient()
            ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            ssh.load_system_host_keys()
            ssh.connect(hostname=globalVars.configInfo['Appliance']['ip'], username="delladmin", password="delladmin")
            
            #Change User to SUDO
            shell = ssh.invoke_shell()
            shell.send("sudo -i\n")
            time.sleep(5)
            receiveBuffer = shell.recv(1024)
            
            #Input SUDO Password
            shell.send("delladmin\n")
            time.sleep(5)
            receiveBuffer = shell.recv(2048)
            
            #Execute Command to create Post-Install files
            shell.send("echo 'echo Hello Linux Post-Install > /tmp/hello.txt' > /etc/puppetlabs/puppet/modules/linux_postinstall/files/hello.sh\n")
            time.sleep(5)
            receiveBuffer = shell.recv(1024)
            utility.execLog(receiveBuffer)
            
            #Changing permissions of the file --> Making it executable
            shell.send("chmod 777 /etc/puppetlabs/puppet/modules/linux_postinstall/files/hello.sh\n")
            time.sleep(5)
            receiveBuffer = shell.recv(1024)
            utility.execLog(receiveBuffer)
            
            #Execute Command to create Post-Install files
            shell.send("echo 'Get-Help -Name Get-Process >> C:\WinPIOutput.txt' > /etc/puppetlabs/puppet/modules/windows_postinstall/files/winTest.ps1\n")
            time.sleep(5)
            receiveBuffer = shell.recv(1024)
            utility.execLog(receiveBuffer)
            
            #Changing permissions of the file --> Making it executable
            shell.send("chmod 777 /etc/puppetlabs/puppet/modules/windows_postinstall/files/winTest.ps1\n")
            time.sleep(5)
            receiveBuffer = shell.recv(1024)
            utility.execLog(receiveBuffer)
            
            #Close SSH Connection
            ssh.close()
        except Exception as e:
            utility.execLog("Unable to create post-install files. Kindly create them manually.")
        
    def deleteActiveDirectory(self, directory):
        """
         delete active directory
        """
        utility.execLog("Create Users class object")
        pageObject  = Users.Users(self.browserObject)
        utility.execLog("Load the users page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Delete New Directory")
        globalVars.browserObject, status, result = pageObject.deleteActiveDirectory(directory)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def verifyEditActiveDirectory(self, directory):
        """
         delete active directory
        """
        utility.execLog("Create Users class object")
        pageObject  = Users.Users(self.browserObject)
        utility.execLog("Load the users page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Edit New Directory")
        globalVars.browserObject, status, result = pageObject.verifyEditActiveDirectory(directory)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def createActiveDirectory(self, directoryType, directoryName, directoryUserName, directoryPassword, host, port, protocol, baseDN, directoryFilter, userNameAttr, fNameAttr, lNameAttr, emailAttr,directorySource, searchTerm, importRole, verifySuccess= "pass"):
        """
        Create new active directory
        """
        utility.execLog("Create Users class object")
        pageObject  = Users.Users(self.browserObject)
        utility.execLog("Load the users page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        utility.execLog("Creating New Directory")
        globalVars.browserObject, status, result = pageObject.createDirectory(directoryType, directoryName, directoryUserName, directoryPassword, host, port, protocol, baseDN, directoryFilter, userNameAttr, fNameAttr, lNameAttr, emailAttr)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def serverDeploymentStatus(self,serviceName):
        """
        Description: 
            Verify server deployment status in service with Resources.
        """
        pageObject = Services.Services(self.browserObject)

        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        
        #Export file of Services
        globalVars.browserObject, status, result = pageObject.serverDeploymentStatusInService(serviceName)
        if status:
            self.succeed(result)
            return result
        else:
            self.failure(result, raiseExc=True)
            
    def serverIPList(self,serviceName):
        """
        Description: 
            Getting server ip list from service.
        """
        pageObject = Services.Services(self.browserObject)
 
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
         
        #Export file of Services
        globalVars.browserObject, status, result = pageObject.serverIPListInService(serviceName)
        if status:
            self.succeed(result)
            return result
        else:
            self.failure(result, raiseExc=True)
                   
    def validateServerStateAfterTearDown(self, ipList):
        """
        Description: 
            Getting server ip list in service.
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.validateServerState(ipList)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)  
            
    def serverConsoleStateAfterTearDown(self, ipList):
        """
        Description: 
            Getting server ip list in service.
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.serverConsoleState(ipList)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def deleteServiceAfterScaleDown(self,serviceName):
        """
        Description: 
            Deleting service and validate the service.
        """
        pageObject = Services.Services(self.browserObject)

        globalVars.browserObject, status, result = pageObject.loadPage()
        time.sleep(15)
        if not status:
            self.failure(result, raiseExc=True)
        else:
            self.succeed(result)
        
        #Export file of Services
        globalVars.browserObject, status, result = pageObject.deleteServiceAfterScaleDown(serviceName)
        if status:
            self.succeed(result)
            return result
        else:
            self.failure(result, raiseExc=True)
            
    def validateVDSSetting(self, components, networkConfigDict):
        """
        Validate VDS Settings in cluster  
        """   
        
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)                             
        globalVars.browserObject, status, result = pageObject.validateVDSSetting(components,networkConfigDict)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject
        
    def addOSRepoFromPath(self,resource, verifyOSRepo=True):
        """
        Description: To add OS Repo to ASM appliance
        verifyOSRepo = False; if verification of OSRepo is not required to check if it's Available
        """
        failList = []
        self.browserObject = globalVars.browserObject

        utility.execLog("Add Repository Class Object")
        pageObject  = Repositories.Repositories(self.browserObject)
        utility.execLog("Load The Repositories Page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.deleteOSRepository(resource)
        globalVars.browserObject, status, result = pageObject.addOSRepository(resource, verifyOSRepo)
        if not status:
            self.failure(result)
            failList.append(resource["Repository Name"])
               
        else:
            if verifyOSRepo:
                globalVars.browserObject, status, result = pageObject.verifyOSRepository(resource)
                if not status:
                    self.succeed(result)
                    failList.append(resource["Repository Name"])
   
        if len(failList) == 0:
            self.succeed("Successfully Added OS Image Repository for all repositories :: %s" % resource)
        else:
            self.failure("Failed to Add OS Image Repository :: %s"%(failList), raiseExc=True)
    
    def addDeleteOS(self,resource, verifyOSRepo=True):
        """
        Description: To add OS Repo to ASM appliance and delete repo
        """
        failList = []
        self.browserObject = globalVars.browserObject

        utility.execLog("Add Repository Class Object")
        pageObject  = Repositories.Repositories(self.browserObject)
        utility.execLog("Load The Repositories Page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.addOSRepository(resource, verifyOSRepo)
        if not status:
            self.failure(result)
            failList.append(resource["Repository Name"])
               
        else:
            if verifyOSRepo:
                globalVars.browserObject, status, result = pageObject.verifyOSRepository(resource)
                if not status:
                    self.succeed(result)
                    failList.append(resource["Repository Name"])
   
        if len(failList) == 0:
            self.succeed("Successfully Added OS Image Repository for all repositories :: %s" % resource)
        else:
            self.failure("Failed to Add OS Image Repository :: %s"%(failList), raiseExc=True)
        globalVars.browserObject, status, result = pageObject.deleteOSRepository(resource)
            
    def verifyErrorRepopath(self,resource):
        
        pageObject  = Repositories.Repositories(self.browserObject)
        utility.execLog("Load The Repositories Page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.verifyErrorRepopath(resource)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def getVLanIpListFromString(self,vLanIplistStr):
        '''
        Description: converts string range format to numeric 
        for eg. "42-44" converted to [42,43,44 ]
        '''
        vlanIpList=[]
        for ipStr in vLanIplistStr:
            ipStr1=ipStr.split(',')
            for ipStr2 in ipStr1:
                if '-' in ipStr2:
                    ipStr3=ipStr2.split('-')
                    start=int(ipStr3[0])
                    end=int(ipStr3[1])
                    for vlan in range(start,end+1):
                        vlanIpList.append(vlan)
                else:
                    vlanIpList.append(int(ipStr2))
        return vlanIpList

    def getVLanTags(self, serviceName):
        '''
        Description: Validate the Switch Tag status after service deployment
        '''
        pageObject= Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        switchHostsIp= []
        vLanTags={}
        utility.execLog("Get Deployed Service Switch IP Address")
        globalVars.browserObject, status, result = pageObject.getDeployedSwitchIP(serviceName)
        if status:
            switchHostsIp= result
        else:
            self.failure("Unable to read switch IP Address %s"%result, raiseExc= True)
#         for hostIp in switchHostsIp:
        status, result= self.connectSSH("Switch", switchHostsIp, "show running-config interface | grep vlan")
        utility.execLog("Command Result %s"%result)
        untagged=[]
        tagged=[]
        vlans=result.split("\n")
        for vlan in vlans:
            if "untagged" in vlan:
                index=vlan.find("untagged")
                untagged.append(vlan[index+9:])
            elif "tagged" in vlan:
                index=vlan.find("tagged")
                tagged.append(vlan[index+7:])
                
        tagged= self.getVLanIpListFromString(tagged)
        untagged= self.getVLanIpListFromString(untagged)
        utility.execLog("Tagged Vlans %s untagged Vlans %s"%(tagged,untagged))
        return tagged, untagged
    
    def getServerIp(self, compliance="Compliant", deploymentState="not in use"):
        '''
        Description: Returns server Ip with the desired filter
        '''
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.getServerIp(compliance, deploymentState)
        if status:
            self.succeed("Server IP :: '%s'"%result)
            return result
        else:
            self.failure("Unable to get Resource with Compliance state %s and deploymentState %s"%(compliance, deploymentState), raiseExc=True)
            
    def verifyDeploymentFirmwareUpdate(self, serverIp):
        '''
        Description: Verify correct Update State for server in Deployment with firmware update selected
        '''
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()        
        utility.execLog("Verifying Sever %s Firmware Update State"%serverIp)
        globalVars.browserObject, status, result = pageObject.verifyDeploymentFirmwareUpdate(serverIp)
        if status:
            self.succeed("Deployment Firmware Update displaying expected state: %s"%result)
        else:
            self.failure("Deployment Firmware Update not displaying expected state %s"%result, raiseExc= True)
            
    def verifyUpdateDisabled(self, selectCompliant=False, selectInUse= False, selectUnmanaged= False):
        '''
        Description: Verify update firmware button is disabled for compliant and In-use resources
        '''
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result= pageObject.verifyUpdateDisabled(selectCompliant, selectInUse, selectUnmanaged)
        if status:
            self.succeed("Update firmware is disabled for Compliant or In-use resources")
        else:
            if "Resource not found" in result:
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure("Update firmware is enabled for compliant or In-use resources %s"%result)
                
    def verifyUsageStatsDisplayed(self):
        '''
        Description: Verify that for 13G server detailed views Correct System Usage, CPU usage, Memomry and I/O usage values 
        are dsiplayed along with graphs and pie charts
        '''
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result= pageObject.verifyUsageStatsDisplayed()
        if status:
            self.succeed(result)
        else:
            if "Resource not found" in result:
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True) 
            else:
                self.failure(result, raiseExc=True)
                
    def getServiceFirmwareList(self, serviceName):
        '''
        Description: Get the list of firmware available for update firmare in service page.
        '''
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        utility.execLog("Listing firmware available for update resource for %s service"%serviceName)
        globalVars.browserObject, status, result= pageObject.getServiceFirmwareList(serviceName)
        if status:
            firmwareList= result
            return firmwareList
        else:
            self.failure("Unable to fetch Firmware names from service page Error %s"%result, raiseExc= True)
            
    def getFirmwareDownloadState(self,firmwareName):
        '''
        Description: Get download state for a firmware loaded
        '''
        pageObject = Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        utility.execLog("Fetching Download state for firmware %s"%firmwareName)
        globalVars.browserObject, status, result= pageObject.getFirmwareDownloadState(firmwareName)
        if status:
            downloadState= result
            return downloadState
        else:
            self.failure("unable to fetch download state of firmware %s Error: %s"%(firmwareName,result))
            
    def getSwitchBootMode(self,switchIp):
        '''
        Description: Verify IOAs are in PMUX mode
        '''
        try:
            utility.execLog("Connecting To Switch %s to Validate PMUX mode"%switchIp)
            command= "show system stack-unit all iom-mode"
            output=self.connectSSH("Switch", switchIp, command)
            bootModeInfo= str(output)
            return bootModeInfo
        except Exception as e:
            self.failure("unable to get boot mode output Error: %s"%e, raiseExc=True)
            
    def verifyInvalidIPSetupError(self):
        """
        Description: Verify Error message displayed on Defining network with Invalid IP
        """
        pageObject = Networks.Networks(self.browserObject)
         
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result)
        networkConfig = utility.readExcel(globalVars.inputFile, "Network")
        utility.execLog("Defining new Static Network")
        for network in networkConfig:
            if network["Static"] == "true":
                configureStatic= True
                break
        invalidIPAddress= network["StartIP"]+"000" #Invalid start IP address 
        vLanID= "4094"   #Assumed unused VLanID
        globalVars.browserObject, status, result = pageObject.defineNetwork(network["Name"], network["Description"],
                        network["Type"], vLanID, configureStatic, network["Subnet"], network["Gateway"], network["PrimaryDNS"], 
                        network["SecondaryDNS"], network["DNSSuffix"], invalidIPAddress)
        if not status:
            self.succeed("Invalid IP Setup not saved: %s"%result)
        else:
            self.failure("Invalid IP Setup ", raiseExc= True)
            
#     def verifyOSRepoTempDownload(self):
#         ssh=SSHConnection('172.31.43.1', port=22, username='delladmin', password='delladmin',timeout=None)
#         status, result=ssh.connect()
#         if status:
#             utility.execLog("success %s"%result)
#             transport = ssh.get_transport()
#             session = transport.open_session()
#             session.set_combine_stderr(True)
#             session.get_pty()
#             #changing mode of Temp repo-dwonload folder
#             session.exec_command("sudo chmod 777 /var/nfs/text1.txt")
#             stdin = session.makefile('wb', -1)
#             stdout = session.makefile('rb', -1)
#             #you have to check if you really need to send password here 
#             stdin.write("delladmin" +'\n')
#         else:
#             utility.execLog("Fail %s"%result)
            
    def verifyOSRepoDownload(self, repoName):
        hostType = "Appliance"
        HOST = globalVars.configInfo['Appliance']['IP'] #HOST = self.parseConfigFile()
        COMMAND = "ls /var/lib/razor/repo-store/"
        status, result = self.connectSSH(hostType, HOST, COMMAND)
        if status:
            if repoName in result:
                self.succeed("OS Repo is listed correctly in razor repos")
            else:
                self.failure("OS Repo not listed in razor repos", raiseExc= True)
        else:
            self.failure("Error: %s"%result)
            
#     def verifyRepositoryDeletion(self,checkUse=False,checkState=False):
#         print"verifyRepositoryDeletion"
#         utility.execLog("Add Repository class object")
#         pageObject  = Repositories.Repositories(self.browserObject)
#         utility.execLog("Load the repositories page")
#         globalVars.browserObject, status, result = pageObject.loadPage()
#         if status:
#             self.succeed(result)
#         else:
#             self.failure(result, raiseExc=True)
#             
#         
#         errorRepoName="CopyingStateCheck"
#         errorRepoDict={"Repository Name":errorRepoName,"Image Type":"Red Hat 7","Source Path":"ftp://172.24.3.50/Projects/HCL/ISO/RHEL-7.2.iso","User Name":"","Password":""}
#         globalVars.browserObject, status, result = pageObject.addOSRepository(errorRepoDict,verifyOSRepo=False)
#         if status:
#             utility.execLog("Unable to add Reository")
#             self.succeed(result)
#         else:
#             self.failure(result, raiseExc=True)
#             
#         globalVars.browserObject, status, result = pageObject.verifyRepositoryDeletion(inUse=checkUse,state=checkState)
#         if status:
#             self.succeed(result)
#             return result
#         else:
#             self.failure(result, raiseExc=True)
#             
#     def AddDuplicateRepository(self):
#         utility.execLog("Add Repository Class Object")
#         pageObject  = Repositories.Repositories(self.browserObject)
#         utility.execLog("Load The Repositories Page")
#         globalVars.browserObject, status, result = pageObject.loadPage()
#         if status:
#             self.succeed(result)
#         else:
#             self.failure(result, raiseExc=True)
#             
#         resource=pageObject.fatchFirstRepository()
#         globalVars.browserObject, status, result = pageObject.addOSRepository(resource)
#         if not status:
#             self.failure(result)
#             utility.execLog(result)
#             
#     def testErrorRepository(self):
# 
#         pageObject  = Repositories.Repositories(self.browserObject)
#         utility.execLog("Load The Repositories Page")
#         globalVars.browserObject, status, result = pageObject.loadPage()
#         if status:
#             self.succeed(result)
#         else:
#             self.failure(result, raiseExc=True)
#               
#         #First add errorRepo then delete ISO to make it in error state
#         errorRepoName="errorRepoTest"
#         errorRepoDict={"Repository Name":errorRepoName,"Image Type":"Red Hat 7","Source Path":"ftp://172.24.3.50/Projects/HCL/ISO/RHEL-7.2.iso","User Name":"","Password":""}
#         globalVars.browserObject, status, result = pageObject.addOSRepository(errorRepoDict,verifyOSRepo=False)
#         if status:
#             utility.execLog(result)
#             utility.execLog("Start deleting Repo Folder {}".format(errorRepoName))
#                
#         else:
#             self.failure(result, raiseExc=True)
#             utility.execLog(result)
#          
#   
#         status, result= pageObject.delnow(errorRepoName)
#         if status:
#             utility.execLog(result)
#            
#         else:
#             self.failure(result, raiseExc=True)
#             utility.execLog(result)
#             
#         utility.execLog("After deleting.....")
#         
#         globalVars.browserObject, status, state = pageObject.checkRepositoryState(errorRepoName)
#         if status:
#             utility.execLog("Repository state is ={}".format(state))
#             #self.succeed(result)
#         if state=="Error":
#             utility.execLog("Repository state is ={}".format(state))
#             self.succeed("Repository is in Error state".format(state))
#             
#         else :
#             self.failure("Repository is not in Error state".format(state))
#             utility.execLog("Repository state is ={}".format(state))
            
    def updateSwitchFirmware(self, updateAll=True, scheduleUpdate=False):
        '''
        Description: Update firmware for non-compliant switches
        '''
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        utility.execLog("Updating Firmware for non-compliant switches")
        globalVars.browserObject, status, result= pageObject.updateSwitchFirmware(updateAll, scheduleUpdate)
        if status:
            self.succeed("Result :%s"%result)
        else:
            self.failure("Unable to update switch firmware: %s"%result, raiseExc= True)
            
    def defineChassisSettings(self, component):
        '''
        Description: Configure chassis from getting started page
        '''
        pageObject = GettingStarted.GettingStarted(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed("Able to load Getting Started page")
        else:
            self.failure("unable to open Getting Started page %s"%result)
        globalVars.browserObject, status, result = pageObject.defineChassisSettings(component)
        if status:
            self.succeed("Chassis configuration complete")
        else:
            self.failure("chassis configuration failed : %s"%result)
            
    def reSynchrozeFW(self):
        
        pageObject  = Repositories.Repositories(self.browserObject)
        utility.execLog("Load The Repositories Page")
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.reSynchrozeFW()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)

    def rackServerDiscovery(self,resourceType,resourceIP,resourceState,credentialName):
        
        pageObject  = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.singleResourceDiscovery(resourceType,resourceIP,resourceState,credentialName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def rackServerDiscovSchnario(self,resourceType,resourceIP,resourceState,credentialName,userName,password,posSchanario=False,negSchanario=False,ipAddress=False,existingserverIPaddress=False, unCheck=False,hwnwName=""):
        
        pageObject  = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
         
        globalVars.browserObject, status, result = pageObject.singleResourceDiscovery(resourceType,resourceIP,resourceState,credentialName,userName,password,ipAddress=ipAddress,existingserverIPaddress=existingserverIPaddress,unCheck=unCheck,hwnwName=hwnwName)
        if negSchanario:
            if not status:
                self.succeed(result)
        elif status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        pageObject  = Logs.Logs(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        if negSchanario:
            globalVars.browserObject, status, result = pageObject.verifyLogs(resourceIP,negSchanario=negSchanario)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
        if posSchanario:
            globalVars.browserObject, status, result = pageObject.verifyLogs(resourceIP,posSchanario=posSchanario)
            if status:
                self.succeed(result)
            else:
                self.failure(result, raiseExc=True)
    def defineNWSchanario(self,networkName, networkDescription, networkType, networkVLAN, configureStatic=False,
                      subnet="", gateway="", primaryDNS="", secondaryDNS="", dnsSuffix="", 
                      startingIPAddress="", endingIPAddress=""):
        
        pageObject  = Networks.Networks(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.defineNetwork(networkName, networkDescription, networkType, networkVLAN, configureStatic=configureStatic,subnet=subnet, gateway=gateway, primaryDNS=primaryDNS, secondaryDNS=secondaryDNS,startingIPAddress=startingIPAddress, endingIPAddress=endingIPAddress)
        if (not status) and (result == "An invalid IP address is entered") :
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    def discoveryChassisAndServer(self, excelSheetName, ipList):
        utility.execLog("Discovering multipal resources")
        pageObject  = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        serviceTagList = self.discoverMultipleResources(option=excelSheetName)
        
        self.browserObject, status, result = pageObject.waitForResourceDiscovery()

        self.browserObject, status, resourceList = pageObject.getResources("All")
        time.sleep(5)
        status = False
        count = 0
        for resource in resourceList:
            if (resource["IP Address"] in ipList) and resource["Asset/Service Tag"] in serviceTagList:
                utility.execLog("Resource IP :'%s' and Service tag :'%s'"%(resource["IP Address"],resource["Asset/Service Tag"]))
                status = True
                count += 1
            
        if status and count == len(ipList):
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def brownFieldServiceSchanario(self,brownFieldServiceName,brownFieldCompName,targetVirtualMachineServiceTag,dataCenterName,clusterName,storageDiscov=False,serverDiscov=False,serverState=False,storageState=False,addfirmware=False):
        
        pageObject  = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.brownFieldServiceSchanario(brownFieldServiceName,brownFieldCompName,targetVirtualMachineServiceTag,dataCenterName,clusterName,storageDiscov=storageDiscov,serverDiscov=serverDiscov,serverState=serverState,storageState=storageState,addfirmware=addfirmware)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def discoverResource_Type(self, resourceType, resourceStartIP, resourceEndIP, manageResource,
                      serverCredential, storageCredential, chassisCredential, vcenterCredential,
                      switchCredential, scvmmCrdential):
        """
        Description: open Resources page 
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.discoverResource(resourceType, resourceStartIP, resourceEndIP, manageResource,
                      serverCredential, storageCredential, chassisCredential, vcenterCredential,
                      switchCredential, scvmmCrdential)
        self.browserObject, status, result = pageObject.waitForResourceDiscovery()

        self.browserObject, status1, resourceList = pageObject.getResources(resourceType)
        time.sleep(5)
        status=False
        for resource in resourceList:
                if (resource["IP Address"] == resourceStartIP) :
                    utility.execLog("Discover resource successfuly done :%s" %resourceStartIP)
                    status=True
                    break
        if status:
            self.succeed(resourceList)
        else:
            self.failure(resourceList, raiseExc=True)
    
    def changeResourceState(self, resourceIpList, resourceState="Unmanaged"):
        """
        Description: 
            Changes State of the Resource(s)
        """
        pageObject = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.changeResourceState(resourceIpList, resourceState=resourceState)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def verifyJobCompleted(self, jobName):
        '''
        Description: Verify whether a job  is completed or not
        '''
        pageObject= Jobs.Jobs(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        if status:
            self.succeed("Able to load Jobs page")
        else:
            self.failure("Unable to load Jobs page :%s"%result)
        globalVars.browserObject, status, jobList= pageObject.getDetails()
        jobFound=False
        for column, element in jobList.items():
            if jobName in element:
                jobFound=True
                break
        #Checking for absence of Job to confirm Job Completion
        if jobFound:
            return False
        else:
            return True
        
    def validateUpdateFirmwareLogs(self, deviceIdList, readLogsFromTime):
        '''
        Description: Verify Update Firmware completion Logs is generated.
        '''
        pageObject= Logs.Logs(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        utility.execLog("Verify Update Job completed")
        try:
            updateJobCompleted=self.verifyJobCompleted("Firmware Update Job")
            retry=1
            while retry<3:
                if updateJobCompleted:
                    break
                else:
                    utility.execLog("Wait for firmware update Job to complete")
                    time.sleep(60)
                    updateJobCompleted=self.verifyJobCompleted("Firmware Update In Progress")
                    retry+=1
            logDetailsList=self.getLogs(readAll=True, readFromTime=readLogsFromTime)
            logDescriptionList=[]
            for tempDict in logDetailsList:
                    logDescriptionList.append(tempDict["Description"])
            deviceLogStatus={}
            for deviceId in deviceIdList: 
                updateSuccessLog= "Firmware update completed for device "+deviceId
                updateFailLog= "Firmware update failed for device "+deviceId
                if updateSuccessLog in logDescriptionList:
                    deviceLogStatus[deviceId]="Update Successful"
                elif updateFailLog in logDescriptionList:
                    deviceLogStatus[deviceId]= "Update Failed"
                else:
                    deviceLogStatus[deviceId]= "Update not initiated"
            return deviceLogStatus  
        except Exception as e:
            self.failure(e, raiseExc=True)
            
    def getFirmwareDataXml(self, resourceIp):
        username="root" 
        password="calvin"
        applianceIp=globalVars.applianceIP
        command=" wsman enumerate http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/root/dcim/DCIM_SoftwareIdentity -h %s -V -v -c dummy.cert -P 443 -u %s -p %s -j utf-8 -m 256 -y basic --transport-timeout=300"%(resourceIp, username, password)
        status, result=self.connectSSH(hostType="Appliance", HOST=applianceIp, COMMAND=command)
        if status:
            return result
        else:
            utility.execLog("Unable to fetch firmware data")
            return result
        
    def getSwitchFirmwareVerion(self, resourceIP, resourceModel):
        if resourceModel in ("I/O-Aggregator","MXL-10/40GbE", 'PE-FN-2210S-IOM','MXL-10/40GbE'):
            username="root"
            password="calvin"
        else:
            username="admin"
            password="dell1234"
        command="show version"
        status, result= self.connectSSH(hostType="Switch", HOST=resourceIP, COMMAND=command, username=username, password=password)
        utility.execLog(result)
        if status:
            result= result.split('\n')
            for info in result:
                if "Dell Application Software Version" in info:
                    versionInfo=info.split(' ')
                    version=versionInfo[-1]
                    utility.execLog("Version of Switch %s : %s"%(resourceIP,version))
                    return True,version
        else:
            utility.execLog("Unable to SSH to Switch %s"%resourceIP)
            return False, "Unable to Fetch Info"
        
#    def validateUpdatedFirmwareVersion(self, resourceType, deviceModel, resourceIP, versionData):
#        pageObject= Resources.Resources(self.browserObject)
#        globalVars.browserObject, status, result= pageObject.loadPage()
#        self.browserObject,status, result= pageObject.getResourceFirmwareVersion(resourceType, resourceIP)
#        if not status:
#            self.failure(result)
#            return False
#        self.browserObject.refresh()
#        resourcefirmwareVersion={}
#        if status:
#            self.succeed("Able to fetch Table elements Device Model:: %s Resource IP:: %s"%(deviceModel, resourceIP))
#        else:
#            self.failure("Unable to fetch Table elements", raiseExc=True)
#        try:
#            for componentDetails in result:
#                if componentDetails["Component"] in ('BIOS', 'Integrated Dell Remote Access Controller'):
#                    resourcefirmwareVersion[componentDetails["Component"]]=componentDetails["Current Version"]
#                elif componentDetails["Component"]=='' or "CMC" in componentDetails["Component"]:
#                    resourcefirmwareVersion["firmwareVersion"]=componentDetails["Current Version"]
#                    #Modify version details by removing bundle information of switch, for exact match###
#                    if '(' in resourcefirmwareVersion["firmwareVersion"]:
#                        resourcefirmwareVersion["firmwareVersion"]=resourcefirmwareVersion["firmwareVersion"].split('(')
#                        resourcefirmwareVersion["firmwareVersion"]=resourcefirmwareVersion["firmwareVersion"][0]
#            incorrectVersion=[]
#            utility.execLog("Verifying Firmware Version shown in resource page is within expected range")
#            for component in resourcefirmwareVersion.keys():
#                if not (v.LooseVersion(versionData[resourceType][deviceModel][component]["From"])<=v.LooseVersion(resourcefirmwareVersion[component])<=v.LooseVersion(versionData[resourceType][deviceModel][component]["To"])):
#                    incorrectVersion.append(component)
#            if len(incorrectVersion)==0:
#                self.succeed("Version validated from resource page successful for resource "+resourceIP)
#            else:
#                for component in incorrectVersion:
#                    self.failure("Incorect Version for component "+component+" Expected in range From:"+versionData[resourceType][deviceModel][component]["From"]\
#                                 +"To :"+versionData[resourceType][deviceModel][component]["To"]+" current version"+resourcefirmwareVersion[component], raiseExc=False)
#                    
#            sshFirmwareVersion={}
#            if resourceType=="Servers":
#                utility.execLog("Verifying Firmware Version through XML generated by executing wsman commmand")
#                
#                firmwareXml=self.getFirmwareDataXml(resourceIP)
#                ##workaround for #ASM-8226################
#                firmwareXmlStr=firmwareXml.split('<?xml version="1.0" encoding="UTF-8"?>')
#                firmwareXmlStr='<?xml version="1.0" encoding="UTF-8"?>'+firmwareXmlStr[2]
#                ############################################
#                root=ET.fromstring(firmwareXmlStr)
#                namespace={'n1':"http://schemas.dell.com/wbem/wscim/1/cim-schema/2/DCIM_SoftwareIdentity"}
#                for elem in root.getiterator('{http://schemas.dell.com/wbem/wscim/1/cim-schema/2/DCIM_SoftwareIdentity}DCIM_SoftwareIdentity'):
#                    elementName=elem.find('n1:ElementName',namespace).text
#                    if elementName in("BIOS", "Integrated Dell Remote Access Controller"):
#                        elementVersion=elem.find('n1:VersionString',namespace).text
#                        sshFirmwareVersion[elementName]=elementVersion
#            elif resourceType=="Switches":
#                utility.execLog("SSH to Switch and running show version command")
#                status, result= self.getSwitchFirmwareVerion(resourceIP, deviceModel)
#                if status:
#                    sshFirmwareVersion["firmwareVersion"]=result
#                    #Modify version details by removing bundle information of switch, for exact match###
#                    if '(' in sshFirmwareVersion["firmwareVersion"]:
#                            sshFirmwareVersion["firmwareVersion"]=sshFirmwareVersion["firmwareVersion"].split('(')
#                            sshFirmwareVersion["firmwareVersion"]=sshFirmwareVersion["firmwareVersion"][0]
#                else:
#                    utility.execLog("Unable to fetch Version information theough SSH for switch %s"%resourceIP)
#                    self.failure("Unable to fetch Version information theough SSH for switch %s: %s"%(resourceIP, result))
#                    return False
#            incorrectVersion=[]
#            for component in sshFirmwareVersion.keys():
#                if not (v.LooseVersion(versionData[resourceType][deviceModel][component]["From"])<=v.LooseVersion(sshFirmwareVersion[component])<=v.LooseVersion(versionData[resourceType][deviceModel][component]["To"])):
#                    incorrectVersion.append(component)
#            if len(incorrectVersion)==0:
#                self.succeed("Version validated from wsman XML for resource "+resourceIP)
#                return True
#            else:
#                for component in incorrectVersion:
#                    self.failure("Incorect Version for component "+component+" Expected in range From:"+versionData[resourceType][deviceModel][component]["From"]\
#                                 +"To :"+versionData[resourceType][deviceModel][component]["To"]+" current version"+resourcefirmwareVersion[component])
#                return False
#            
#        except Exception as e:
#            self.failure(e)
#            return False
            
        
    def updateResource(self, resourceType, scheduleWait=None, updateAll=True):
        """
        Updates Firmware on the Resource
        """
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        if not status:
            self.failure(result)
        globalVars.browserObject, status, result = pageObject.updateResource(resourceType, scheduleWait, updateAll)
        if status:
            self.succeed(result)
        else:
            if "Resource not found" in result:
                self.failure(result, resultCode=BaseClass.OMITTED, raiseExc=True)
            else:
                self.failure(result, raiseExc=True)
            
    def getResourceComplianceStatus(self, resourceIP):
        """
        Fetches Resource Compliance Status
        """
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result =pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        result = self.getResources("Servers")
        self.succeed("Resource Info :: %s"%result)
        valid = [resource for resource in result if resource["Management IP"] == resourceIP]
        if len(valid) <= 0:
            self.failure("Resource Not found", resultCode=BaseClass.OMITTED, raiseExc=True)
        if valid[0].has_key("Compliance"):
            result = valid[0]["Compliance"]        
            self.succeed("Able to fetch Compliance status for Resource '%s' :: State '%s'"%(resourceIP, result))
            return result
        else:
            self.failure("Unable to fetch Compliance Status from result '%s'"%valid, raiseExc=True)
            
    def singleResourceDiscovery(self,resourceType,resourceIP,resourceState,credentialName, userName=None, password=None):
        
        pageObject  = Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        
        globalVars.browserObject, status, result = pageObject.singleResourceDiscovery(resourceType,resourceIP,resourceState,credentialName, userName=userName, password=password)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
            
    def verifyCustomBundlePath(self,catalogName, bundleName):
        pageObject= Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result =pageObject.loadPage()
        
        globalVars.browserObject, status, result= pageObject.verifyCustomBundlePath(catalogName, bundleName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
         
    def editCustomBundlePath(self, catlogName, bundleName, newBundlePath):
        pageObject= Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result =pageObject.loadPage()
        
        globalVars.browserObject, status, result= pageObject.editCustomBundlePath(catlogName, bundleName, newBundlePath)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
                
    def deleteCustomBundle(self, catalogName, bundleName):     
        pageObject= Repositories.Repositories(self.browserObject)
        globalVars.browserObject, status, result =pageObject.loadPage()      
        
        globalVars.browserObject, status, result=pageObject.deleteCustomBundle(catalogName, bundleName)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
    
    def waitJobComplete(self, job):
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result =pageObject.loadPage()
        
        globalVars.browserObject, status, result=pageObject.waitJobComplete(job)
        if status:
            self.succeed(result)
        else:
            self.failure(result)
            
    
    def verifySwitchFirmwareVersion(self,model,switchIP):
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result =pageObject.loadPage()
        
        globalVars.browserObject, status, result=pageObject.getResourceFirmwareVersion('Switches', switchIP)
        resourcefirmwareVersion={}
        for componentDetails in result:
            if componentDetails["Component"]=='':
                resourcefirmwareVersion["firmwareVersion"]=componentDetails["Current Version"]
        BundleFilePath= globalVars.switchBundleRepository[model]
        filePath=BundleFilePath.split("\\")
        filename=filePath[-1]
        versionText=filename.split("-")
        versionText=versionText[-1]
        versionText=versionText.split('.')
        version='.'.join(versionText[:2])+'('+'.'.join(versionText[2:-1])+')'
        if resourcefirmwareVersion["firmwareVersion"]==version:
            self.succeed('Switch Version is correct')
        else:
            self.failure('Switch Version is incorrect', raiseExc=True)
    
    def validateOSIP(self,serviceName):
        """
        Description:
            Fetches All Networks
        """
        networks = self.getNetworks()
        
        pageObject = Services.Services(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if not status:
            self.failure(result, raiseExc=True)
        globalVars.browserObject, status, result = pageObject.validateOSIP(serviceName,networks)
        if status:
            self.succeed(result)
        else:
            self.failure(result, raiseExc=True)
        self.browserObject = globalVars.browserObject

    def color_of_device_icon(self, device_name, expected_color):
        pageObject = Templates.Templates(self.browserObject)
        self.browserObject, status, result = pageObject.color_of_device_icon(device_name)
        if status:
            if expected_color == result:
                self.succeed("Successfully Verified Icon color is '%s'" % result)
            else:
                self.failure("Failed to Verify Icon color '%s'" % result, raiseExc=True)
        else:
            self.failure(result, raiseExc=True)
            
    def getTemplateFirmwarePackage(self, templateName):
        pageObject= Templates.Templates(self.browserObject)
        pageObject.loadPage()
        self.browserObject, status, result= pageObject.getTemplateFirmwarePackage(templateName)
        if status:
            self.succeed('Able to fetch Template firmware Package Name : %s'%result)
            return result
        else:
            self.failure('Unable to fetch Template firmware Package Name', raiseExc=True)
            
    def validateUpdatedFirmwareVersion(self, resourceType, deviceModel, resourceIP):
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        self.browserObject,status, result= pageObject.getResourceFirmwareVersion(resourceType, resourceIP)
        if not status:
            self.failure(result)
            return False
        else:
            componentDetailList=result
        self.browserObject.refresh()
        if status:
            self.succeed("Able to fetch Table elements Device Model:: %s Resource IP:: %s"%(deviceModel, resourceIP))
        else:
            self.failure("Unable to fetch Table elements", raiseExc=True)
        try:
            incorrectVersion=[]
            for componentDetail in componentDetailList:
                if componentDetail["Component"]=='' or "CMC" in componentDetail["Component"]:
                    componentDetail["Component"]="firmwareVersion"
                if componentDetail["Expected Version"]=='NA':
                    utility.execLog('%s Expected Version : NA'%componentDetail['Component'])
                elif componentDetail["Current Version"]==componentDetail["Expected Version"]:
                    utility.execLog('Component %s : Correct Version available'%componentDetail["Component"])
                    self.succeed('Compliance Report Device : %s Component : %s Current Version %s Expected Version: %s validation passed'\
                                 %(resourceIP,componentDetail['Component'],componentDetail['Current Version'],componentDetail['Expected Version']))
                else:
                    utility.execLog('Component : %s Incorrect Version available'%componentDetail['Component'])
                    self.failure('Compliance Report Device %s Component : %s Current Version %s Expected Version: %s validation failed'\
                                 %(resourceIP,componentDetail['Component'],componentDetail['Current Version'],componentDetail['Expected Version']))
                    incorrectVersion.append(componentDetail)

            sshFirmwareVersion={}
            if resourceType=="Servers":
                componentList=[componentName['Component'] for componentName in componentDetailList]
                utility.execLog("Verifying Firmware Version through XML generated by executing wsman commmand")

                firmwareXml=self.getFirmwareDataXml(resourceIP)
                ##workaround for #ASM-8226################
                firmwareXmlStr=firmwareXml.split('<?xml version="1.0" encoding="UTF-8"?>')
                firmwareXmlStr='<?xml version="1.0" encoding="UTF-8"?>'+firmwareXmlStr[2]
                ############################################
                root=ET.fromstring(firmwareXmlStr)
                namespace={'n1':"http://schemas.dell.com/wbem/wscim/1/cim-schema/2/DCIM_SoftwareIdentity"}
                for elem in root.getiterator('{http://schemas.dell.com/wbem/wscim/1/cim-schema/2/DCIM_SoftwareIdentity}DCIM_SoftwareIdentity'):
                    elementName=elem.find('n1:ElementName',namespace).text
                    if elementName in componentList:
                        elementVersion=elem.find('n1:VersionString',namespace).text
                        sshFirmwareVersion[elementName]=elementVersion
                utility.execLog('Version Data fetched from XML ::\n %s'%sshFirmwareVersion)
                for componentDetail in componentDetailList:
                    if componentDetail["Expected Version"]=='NA':
                        utility.execLog('%s Expected Version : NA'%componentDetail['Component'])

                    elif sshFirmwareVersion[componentDetail['Component']]==componentDetail['Expected Version']:
                        utility.execLog('Software Identity XML Report Device : %s Component : %s Current Version %s Expected Version: %s validation passed'\
                                 %(resourceIP,componentDetail['Component'],sshFirmwareVersion[componentDetail['Component']],componentDetail['Expected Version']))
                        self.succeed('Software Identity XML Report Device : %s Component : %s Current Version %s Expected Version: %s validation passed'\
                                 %(resourceIP,componentDetail['Component'],sshFirmwareVersion[componentDetail['Component']],componentDetail['Expected Version']))
                    else:
                        utility.execLog('Software Identity XML Report Device %s Component : %s Current Version %s Expected Version: %s validation failed'\
                                 %(resourceIP,componentDetail['Component'],sshFirmwareVersion[componentDetail['Component']],componentDetail['Expected Version']))
                        self.failure('Software Identity XML Report Device %s Component : %s Current Version %s Expected Version: %s validation failed'\
                                 %(resourceIP,componentDetail['Component'],sshFirmwareVersion[componentDetail['Component']],componentDetail['Expected Version']))
                        incorrectVersion.append(componentDetail)

            elif resourceType=="Switches":
                utility.execLog("SSH to Switch and running show version command")
                status, result= self.getSwitchFirmwareVerion(resourceIP, deviceModel)
                if status:
                    sshFirmwareVersion["firmwareVersion"]=result
                else:
                    utility.execLog("Unable to fetch Version information theough SSH for switch %s"%resourceIP)
                    self.failure("Unable to fetch Version information theough SSH for switch %s: %s"%(resourceIP, result))
                    return False
                expectedVersion=componentDetailList[0]['Expected Version']
                if sshFirmwareVersion["firmwareVersion"]==expectedVersion:
                    utility.execLog('Software Identity SSH Result Device : %s Firmware Version Current Version %s Expected Version: %s validation passed'\
                                 %(resourceIP,sshFirmwareVersion["firmwareVersion"],expectedVersion))
                    self.succeed('Software Identity XML Report Device : %s Component : Firmware Version Current Version:: %s Expected Version: %s validation passed'\
                                 %(resourceIP,sshFirmwareVersion["firmwareVersion"],expectedVersion))
                else:
                    utility.execLog('Software Identity SSH Result Device : %s Firmware Version Current Version:: %s Expected Version:: %s validation failed'\
                                 %(resourceIP,sshFirmwareVersion["firmwareVersion"],componentDetailList[0]['']['Expected Version']))
                    self.failure('Software Identity XML Report Device : %s Component : Firmware Version Current Version:: %s Expected Version: %s validation failed'\
                                 %(resourceIP,sshFirmwareVersion["firmwareVersion"],componentDetailList[0]['']['Expected Version']))
                    incorrectVersion.append(componentDetail)
            if len(incorrectVersion)==0:
                self.succeed("Version validated from wsman XML for resource "+resourceIP)
                return True
            else:
                for component in incorrectVersion:
                    self.failure("Incorect Version %s"%component)
                return False
            
        except Exception as e:
            self.failure(e)
            return False
    
    def getDeviceInfoTable(self, resourceIp):
        '''
        Get the values from Device Info Table of a resource
        '''
        pageObject= Resources.Resources(self.browserObject)
        globalVars.browserObject, status, result= pageObject.loadPage()
        self.browserObject, status, result=pageObject.getDeviceInfoTable(resourceIp)
        if status:
            self.succeed('Able to fetch device-info table %s'%result)
            return result
        else:
            self.failure('Unable to fetch device-info table', raiseExc=True)