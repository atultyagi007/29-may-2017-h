'''
Created on Jan 15, 2016

@author: Dheeraj.Singh
'''

import json
import os
import traceback
from zipfile import ZipFile
from libs.product import globalVars
import utility



def setUp():
    """
    Pre Requisite Setup
    """
    #Remove Old Logs
    utility.removePreviousLog()
    
    # Create Execution Log and Readme Location    
    execLog = os.path.join(globalVars.logsDir, "exec_log")
    statusLog = os.path.join(globalVars.logsDir, "status_log")
    if not os.path.exists(execLog):
        os.makedirs(execLog)
    if not os.path.exists(statusLog):
        os.makedirs(statusLog)
    
    globalVars.configInfo = utility.readConfig(globalVars.configFile)
    utility.loadServices()  
    utility.setupAppliance()
    
     
   
    
    
def tearDown():
    """
    Project level teardown 
    """
    pass


def addFolderToZip(zip_file, folder):
    """
    Zip log files 
    """
    for tempFile in os.listdir(folder):
        full_path = os.path.join(folder, tempFile)
        if os.path.isfile(full_path):
            zip_file.write(full_path)
        elif os.path.isdir(full_path):
            zip_file=addFolderToZip(zip_file, full_path)
    return zip_file


def CreateZipFile(zipfilename):
    """
    Create Zip File
    """
    curr_dir=os.getcwd()
    try:
        folder=os.path.join(globalVars.logsDir)
        os.chdir(folder)
        
        if (os.path.exists(zipfilename)):
            os.remove(os.path.join(folder, zipfilename))
            
        file_list=os.listdir(".")
        file_list=filter(lambda x:not x.startswith("."), file_list)
        zip_file = ZipFile(zipfilename, "w")
        
        for tempFile in file_list:
            if os.path.isfile(tempFile):
                zip_file.write(tempFile)
            else:
                zip_file=addFolderToZip(zip_file, tempFile)
        zip_file.close()
        return True, "SUCCESS: ZIP File Created for the Reports.", zipfilename
    except:
        return False, "ERROR: Unable to create the ZIP File. \n\n%s" % traceback.format_exc(), ""
    finally:
        os.chdir(curr_dir)


def addResultURL():
    """
    Custom atExit method
    
    """
    try:
        logReport = globalVars.configInfo['LogShare']["log_report"]
        resLink = ""    
        if logReport == "1":        
            resLink, logFolder = utility.generateLink()
            utility.logFolderName = logFolder
        #logDir = os.path.join(os.getcwd(), globalVars.logsDir)
        jsonFile = os.path.join(globalVars.logsDir, "report.json") 
        # Updating the JSON file with the result link
        with open(jsonFile) as readfptr:
            data = json.load(readfptr)
            data.update(link=resLink)
        # Writing the updated JSON file to the respective file
        with open(jsonFile, "w") as writefptr:
            json.dump(data, writefptr, sort_keys=True, indent=4, 
                      separators=(',', ': '))
    except:
        print "Unable to generate Result Link"
    

def resultCopy():
    """
    Copies Logs to Server
    
    """
    logReport = globalVars.configInfo['LogShare']["log_report"]
    if logReport == '1':
        #now = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')    
        # Create a zip file
        #zipfilename = "ASM_Automation_Report_%s.zip" % (now)
        #status, message, zipfilename = CreateZipFile(zipfilename)
        sourceLocation = os.path.join(os.getcwd(), globalVars.logsDir)
        logFolderType = globalVars.configInfo['LogShare']['log_folder']      
        shareIP = globalVars.configInfo['LogShare']['ip']
        shareName = globalVars.configInfo['LogShare']['sharename']
        shareUserName = globalVars.configInfo['LogShare']['username']
        sharePassword = globalVars.configInfo['LogShare']['password']
        #resultLink = r"http:\\%s\%s\%s\report.html " % ( shareIP,logFolderType, logFolderName)        
        if os.name=="nt":
            try:
                utility.run_local_cmd( r"net use N: /Delete" )
            except:
                pass
            sourceLocation=sourceLocation.replace("/","\\")            
            utility.run_local_cmd( r"net use N: \\%s\%s /user:localdomain\%s %s" % ( shareIP,shareName,shareUserName,sharePassword ) )
            utility.run_local_cmd( r"xcopy /E /Y /I  %s N:\%s\%s\ " % ( sourceLocation, logFolderType, utility.logFolderName ) )
            utility.run_local_cmd( r"c:" )
            utility.run_local_cmd( r"net use N: /Delete" )
        if os.name=="posix":
            utility.checkDir("/AMLoger")
            utility.run_local_cmd( r"mount -t cifs //%s/%s /AMLoger -o username=%s -o password=%s" % ( shareIP,shareName,shareUserName,sharePassword ) )
            utility.run_local_cmd( r"mkdir -p /AMLoger/%s/%s/ " % ( logFolderType, utility.logFolderName ) )
            utility.run_local_cmd( r"cp -rf %s/*  /AMLoger/%s/%s/ " % ( sourceLocation, logFolderType, utility.logFolderName ) )
            utility.run_local_cmd( r"cd /" )
            utility.run_local_cmd( r"umount /AMLoger" )
