"""
Author: P Suman/Saikumar Kalyankrishnan
Created/Modified: Jun 30th 2015/Feb 15th 2017
Description:
"""

__unittest = True

import os
import sys
from libs.product import utility
from libs.product import globalVars
from libs.product.pages import Login, InitialSetup

def setUp():
    
    """
    Function for loading browser , and update common variables to globalVars file.
    """
    # Remove Old Logs
    utility.removePreviousLog()
    
    # Create Execution Log and Readme Location
    execLog = os.path.join(globalVars.logsDir, "exec_log")
    statusLog = os.path.join(globalVars.logsDir, "status_log")
    screenshotLog = os.path.join(globalVars.logsDir, "screen_shots")
    if not os.path.exists(execLog):
        os.makedirs(execLog)
    if not os.path.exists(statusLog):
        os.makedirs(statusLog)
    if not os.path.exists(screenshotLog):
        os.makedirs(screenshotLog)
    
    globalVars.configInfo = utility.readConfig(globalVars.configFile)
    globalVars.browserName = globalVars.configInfo["Appliance"]["browser"]
    globalVars.applianceIP = globalVars.configInfo['Appliance']['ip']
    globalVars.loginUser = globalVars.configInfo['Appliance']['username']
    globalVars.loginPassword = globalVars.configInfo['Appliance']['password']
    
    # Login To Appliance
    landingPageID = loginAppliance()

    if landingPageID == 2:
        # Perform Initial Setup
        utility.execLog("PopUp: Initial Setup Wizard")
        # Get Required Values from config.ini
        timeZone = globalVars.configInfo['Appliance']['timezone']
        primaryNTP = globalVars.configInfo['Appliance']['ntpserver']
        secondaryNTP = globalVars.configInfo['Appliance']['secondaryntp']
        serviceTag = globalVars.configInfo['Appliance']['servicetag']
        # Initiate Initial Setup Objects
        isObject = InitialSetup.InitialSetup(globalVars.browserObject)
        # Initial Setup Wizard Setup Started
        globalVars.browserObject, status, result = isObject.processInitialSetup(timeZone, primaryNTP, secondaryNTP, serviceTag)
        if not status:
            utility.execLog("Failed during Initial Setup --> %s" % result)
            print "Failed during Initial Setup"
            utility.execLog("Continuing with Test-Case Execution.")
            # To Fail Execution due to failure of Initial Setup Wizard, uncomment the following LoCs
            # globalVars.browserObject.close()
            # sys.exit(0)
        else:
            utility.execLog(result)

# Login to The Appliance
def loginAppliance(newInstance = True, browserObject = None):
    pageObject = Login.Login(globalVars.browserName, globalVars.applianceIP, newInstance=newInstance, browserObject=browserObject)
    if newInstance and not browserObject:
        globalVars.browserObject, status, result = pageObject.getBrowserHandle()
        if not status:
            utility.execLog("Failed to Login to Appliance..Try Again")
        else:
            utility.execLog(result)
    utility.execLog("globalVars.loginUser= %s and globalVars.loginPassword= %s" % (str(globalVars.loginUser), str(globalVars.loginPassword)))
    globalVars.browserObject, landingPage, status, result = pageObject.loginApp(globalVars.loginUser, globalVars.loginPassword, False)
    if not status:
        utility.execLog("Failed to Login to Appliance..Try Again")
        print "Failed to Login to Appliance..Try Again" 
        globalVars.browserObject.close()
        sys.exit(0)
    else:
        utility.execLog(result)
        return landingPage
        
def tearDown():
    """
    Custom Tear-Down Method
    """
    if globalVars.browserObject:
        globalVars.browserObject.close()
    
    #  Setting TCID and Iteration to Empty String
    utility.tc_id = ""
    utility.iteration = ""    

if __name__ == '__main__':
    setUp()
    print 'Executed Setup'