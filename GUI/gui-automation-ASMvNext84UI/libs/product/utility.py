'''
Created on Aug 1, 2014

@author: P.Suman
'''
import hmac
import hashlib
import base64
import datetime
import time
import os
import collections
import json
import socket
import platform
import struct
import logging
import shutil
import re
import codecs
import math
from subprocess import Popen, PIPE
from ConfigParser import ConfigParser
from csv import reader as csvreader
from traceback import format_exc
from xml.etree import ElementTree as et
from libs.thirdparty import openpyxl
from libs.product import globalVars

ip_to_number = lambda ip: struct.unpack('!I', socket.inet_aton(ip))[0]
number_to_ip = lambda num: socket.inet_ntoa(struct.pack('!I', num))
logsdir = "logs"
downloadDir = "downloads"
iteration = 0
tc_id = ""
logFolderName = ""
outputLog = os.path.join(globalVars.logsDir, "output.log")
errorLog = os.path.join(globalVars.logsDir, "error.log")

def convertUTA(data):
    """
    Converts Unicode data to ASCII
    """
    if isinstance(data, basestring):
        return str(data)
    elif isinstance(data, collections.Mapping):
        return dict(map(convertUTA, data.iteritems()))
    elif isinstance(data, collections.Iterable):
        return type(data)(map(convertUTA, data))
    else:
        return data

def get_tc_data(fileName):
    file_name=os.path.basename(fileName)
    if file_name.endswith(".py"):
        class_name=file_name.replace(".py","")
    elif file_name.endswith(".pyc"):
        class_name=file_name.replace(".pyc","")
    tc_id_str=class_name.replace("Testcase_","")
    return tc_id_str

def getFileLists(osName=None, location=None, filePatt=None, fileEXLLIST=[],
                 dirEXLList=[], recursion=False):
    """
    Get the list of files present in a specific directory or in the current 
    working directory matching the searchStr pattern on the local machine.
    """
    if not location:
        location = os.getcwd()
        
    if not osName:
        osName = platform.system()
        
    fileList = []
    flag = 0
    
    if osName.lower() == "windows":
        flag = re.IGNORECASE
    
    # Build the file exclusion pattern
    patt = ""
    if fileEXLLIST: 
        patt = "|".join(fileEXLLIST)
    
    found = False
    for root, dirs, files in os.walk(location):
        for eachDir in dirEXLList:
            if eachDir in dirs:
                dirs.remove(eachDir)
            
        for eachFile in files:
            fileName = os.path.join(root, eachFile)
            if filePatt:
                if re.search(filePatt, fileName, flag):
                    if patt and re.search(patt, eachFile, re.IGNORECASE):
                            continue     # Do Nothing
                    fileList.append(fileName)
                    found = True
            else:
                found = True
                fileList.append(fileName)
                
        if not recursion:
            break
                
    return fileList, found

def removePreviousLog():
    for root, dirs, files in os.walk(logsdir):
        # Remove the directories
        for eachDir in dirs:
            shutil.rmtree(os.path.join(root, eachDir))
            dirs.remove(eachDir)
            
        # Remove the files
        for eachFile in files:
            if eachFile not in ("task.log", "tbagent.log", "job_info.txt", 
                             "runtime.log", "stdout.txt", "stderr.txt"):
                os.remove(os.path.join(root, eachFile)) 

def execLog(cmd, status="", result="", startTime=None, endTime=None, elapsedTime=None):
    """
    Logs the Message
    """
    log_sv_dir=os.path.join(logsdir,"exec_log")
    if not (os.path.exists(log_sv_dir)):
        os.makedirs(log_sv_dir)

    if tc_id:       
        log_file=os.path.join(log_sv_dir,"%s_%s_log.txt"%(tc_id, iteration))
    else:
        log_file=os.path.join(log_sv_dir,"log.txt")

    f = open(log_file, "a")
    f.write("#"*100 + "\n")
    if tc_id and os.path.exists(os.path.join(log_sv_dir,"log.txt")):
        fl=open(os.path.join(log_sv_dir,"log.txt"))
        init_log_data=fl.read()
        fl.close()
        if os.path.exists(os.path.join(log_sv_dir,"log.txt")):
            os.remove(os.path.join(log_sv_dir,"log.txt"))
        f.write(init_log_data + "\n")

    if cmd:
        f.write(str(cmd) + "\n")

    if result != "":
        f.write("Result: ")
        f.write("\n".join(result) if isinstance(result, list) else result + "\n")
    
    if status != "":
        f.write("\n Status: %s \n" % status)
    
    if elapsedTime is not None:
        f.write("\n")
        f.write("Elapsed Time: %s" % elapsedTime + "\n")
        f.write("Start Time: %s" % startTime + "\n")
        f.write("End Time: %s" % endTime + "\n")
    f.write("Time Stamp: %s" % datetime.datetime.now() + "\n")
    f.write("#"*100 + "\n\n")
    f.close()


def set_tc_id(t_id):
    global tc_id
    tc_id=t_id


def set_Iteration(value):
    global iteration
    iteration = value
    

def get_Iteration():
    return iteration


def get_tc_id():
    return tc_id


def disc_param():
    disc_field={}
    disc_field["Appliance IP"] = globalVars.configInfo["Appliance"]["ip"]
    disc_field["NTP Server IP"] = globalVars.configInfo["Appliance"]["ntpserver"]
    disc_field["Python Version"] = platform.python_version()
    disc_field["Platform"] = platform.system()
    return disc_field

def inIPRange(startIP, endIP, searchIP):
    startIP = ip_to_number(startIP)
    endIP = ip_to_number(endIP)
    found = False
    for i in range(startIP, endIP+1):
        curIP = str(number_to_ip(i))
        if searchIP == curIP:
            found = True
    return found

def getIPRange(startIP, endIP):
    startIP = ip_to_number(startIP)
    endIP = ip_to_number(endIP)
    ipList = []
    for i in range(startIP, endIP+1):
        curIP = str(number_to_ip(i))
        ipList.append(curIP)            
    return ipList
 

def readFile(fileName):
        """
        Reads text file and returns string
        """
        with open(fileName,'r') as rfp:
            data = rfp.read()
        return data
    
    
def readCsvFile(fileName, delimiter=","):
    """
    Reads csv file and returns a List with each row as an element
    """
    if os.path.exists(fileName):
        try:
            filehandle = open(fileName, "rU")
            reader = csvreader(filehandle, delimiter=delimiter)
            retlist = []
            for row in reader:
                if row:
                    retlist.append(row)
            filehandle.close()
            return retlist, True
        except:
            return "ERROR: %s" % str(format_exc()), False
    else:
        return "ERROR: File \"%s\" does not exists." % fileName, False


def readConfig(fileName, sectionName=None):
    """
    Reads Config File and returns a dictionary 
    """ 
    config_dict = {}
    try:   
        config = ConfigParser()
        config.read(fileName)
        if sectionName:
            config_dict[sectionName] = dict(config.items(sectionName))
        else:
            for section in config.sections():
                config_dict[section]=dict(config.items(section))
    except Exception,e:
        print e
    return config_dict


def readExcel(fileName, sheetName):
    """
    Description:
        Reads Excel Sheet and returns a list of dictionaries
    
    """
    workbook = openpyxl.load_workbook(filename = fileName, use_iterators = True)
    sheet = workbook.get_sheet_by_name(sheetName)  
    rowCount = sheet.get_highest_row() + 1
    colCount = sheet.get_highest_column() + 1
    
    header = []
    for row in sheet.get_squared_range(1,1,colCount,1):
        for cell in row:
            header.append(cell.value)
    header = convertUTA(header)
    
    result = []
    for row in sheet.get_squared_range(1,2,colCount,rowCount):        
        tempRow = []
        for cell in row:
            value = ""
            if cell.value is not None:
                value = str(cell.value)
            tempRow.append(value)
            
        tempRow = convertUTA(tempRow)         
        result.append(dict(zip(header,tempRow)))
    return result


def getKeyfromDict(dictElem, searchStr):
    """
    Description:
        API to get the key name from the dictionary whose value field matches 
        the searchStr string.
        
    Input:
        dictElem (Dict): Dictionary from which the key has to be searched whose
            value matches the searchStr
        searchStr (String): The value against which the dictionary values will 
            be matched  
    Output:
        Returns the first key name whose value matches with searchStr pattern.
    
    """
    for k,v in dictElem.iteritems():
        if v == searchStr:
            return k
        

def loadServices():
    """
    Description:
        API to get Service Url's from the Services.xml file.            
    Input:
        None
    Output:
        Updates globalVars.serviceUriInfo dictionary with Service Information
          
    """        
    services = et.parse(globalVars.serviceUriInfoFile)
    root = services.getroot()
    
    for child in root.findall('url'):
        globalVars.serviceUriInfo[child.get('name')] = child.get('uri')


def login(authStatus=False, loginUser="admin", loginPwd="admin", loginDomain="ASMLOCAL"):
    """
    Confirm User access credentials
    """
    payload = readFile(globalVars.loginPayload)        
    payload = payload.replace("$login_user", loginUser).replace("$login_pwd", loginPwd).replace("$login_domain", loginDomain)
    payload = payload.replace('\n','').replace('\t','')                        
#     resLN, statLN = runService("POST", "Login", payload, refId=None, authStatus=authStatus, responseType="json", requestContentType="xml")
#     if not statLN:
#         return resLN, False
#     globalVars.apiKey = str(resLN["apiKey"])
#     globalVars.apiSecret = str(resLN["apiSecret"])                              
#     return resLN, True


def buildUrl(uri):
    """
    Builds a Service Url and Returns 
    """
    return "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port'] + uri


def getTimeStamp():
    """
    Returns EPOC Time for CDT Time Zone
    """
    cdtTime = datetime.datetime.utcnow() + datetime.timedelta(hours=-5)
    return str(long(time.mktime(cdtTime.timetuple())))
    

def generateHeader(uri, httpMethod, apiKey, apiSecret, userAgent):
    """
    Generates a Security Header
    """
    #timestamp = str(long(time.mktime(datetime.datetime.utcnow().timetuple()))) 
    timestamp = str(long(time.time()))
    requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
    signature =  base64.b64encode(hmac.new(apiSecret, msg=requestString, digestmod=hashlib.sha256).digest())
    headers = {"Accept":"application/json","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp}
    return headers
    

def loadInputs(inputType):
    """
    Loads the Credential Information provided in Credential.csv
    """ 
    if inputType == "Network":
        configFile = globalVars.networkConfig
    elif inputType == "Credential":
        configFile = globalVars.credentialConfig       
    try:
        result, status = readCsvFile(configFile)
        if not status:
            return "Unable to read Configuration File: %s"%configFile , False
        header = result[0]      
        return [dict(zip(header,result[row])) for row in xrange(1,len(result))], True
    except:
        return "Columns mismatch in the Configuration File: %s"%configFile, False
    

def run_local_cmd(cmd, input=None):
    """
    Executes command locally
    """    
    startTime = datetime.datetime.now()
    p = Popen(cmd,shell=True,stdin=PIPE, stdout=PIPE,stderr=PIPE)
    #status_code = process.wait()
    if input:
        stdOUT, stdERR = p.communicate(input)
    else:
        stdOUT, stdERR = p.communicate()
    endTime = datetime.datetime.now()
    elapsedTime="%s"%(endTime-startTime)
    execLog(cmd, stdERR, stdOUT, startTime, endTime, elapsedTime)            
    if stdERR:
        return stdERR, False
    return stdOUT, True
   
def checkDir(dirs):
    if (os.path.isdir(dirs)):
        temp = 0
    else:
        os.makedirs(dirs)
    return

def generateLink():
    """
    Generate Result Link
    """
    logFolderType = globalVars.configInfo['LogShare']['log_folder']
    cur_time = datetime.datetime.now()
    timestamp = cur_time.strftime("%d%m%y_%H%M%S")
    logFolderName = timestamp
    shareIP = globalVars.configInfo['LogShare']['ip']          
    resultLink = r"http:\\%s\%s\%s\report.html " % ( shareIP,logFolderType,logFolderName)
    return resultLink, logFolderName

def getFileSize(fileName):
    """
    Return Human Readable File Size
    """
    fileSize = ""
    try:
        size = os.path.getsize(fileName)
        size = abs(size)
        if (size==0):
            return "0B"
        units = ['B','KB','MB','GB','TB','PB','EB','ZB','YB']
        p = math.floor(math.log(size, 2)/10)
        fileSize = "%.1f%s" % (size/math.pow(1024,p),units[int(p)])
    except Exception as e:
        execLog("Exception while converting File Size :: %s"%str(e))
    finally:
        return fileSize

def getTemplateConfiguration(fileName):
    """
    Return Human Readable File Size
    """
    result = {}
    try:
        filePath = os.path.join("config", "jsons", fileName)
        rfp = open(filePath, "r")
        result = json.load(rfp)
        execLog("Able to read JSON File :: '%s' --> Content :: %s"%(fileName, result))
    except Exception as e:
        execLog("Exception while reading JSON File :: '%s' --> Error :: %s"%(fileName, str(e)))
    finally:
        return result
