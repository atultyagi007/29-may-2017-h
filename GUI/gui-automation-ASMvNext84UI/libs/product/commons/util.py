from random import randint
from libs.thirdparty import openpyxl
from sys import platform as sysplatform
from subprocess import Popen, PIPE
from traceback import format_exc
from re import match as rematch, search as research, IGNORECASE as reI, findall as refindall
import os,time,re,pickle,csv
import datetime
import shutil
import codecs
import socket
import platform
import ftplib
from StringIO import StringIO
from os import listdir as oslistdir
from os import path as ospath, remove as osremove
import sys
from os import getcwd, environ, popen4, chdir
from ConfigParser import ConfigParser
from libs.core.TelnetConnection import TelnetConnection, TelnetException
from libs.product.commons.FileOperation import readFile, writeFile, readCsvFile, writeCsvFile, renameFile, deleteFile, copyFile, pathExists, pathJoin, pathSplit, remove_dir

try:
    from libs.core.SSHConnection import SSHConnection
except:
    pass

configdir = "config"
logsdir = "logs"
toolsdir = "tools"
omversion = ""

outputLog = pathJoin(logsdir, "output.log")
errorLog = pathJoin(logsdir, "error.log")

wait_time_after_reboot = 300
reboot_timeout = 2700
reboot_sleep = 300
WAIT_TIME_AFTER_OMCONFIG_EXE = 4
WAIT_TIME_AFTER_OMCONFIG_EXE_REMOTE = 10

MASTEREXCEL = "Info.xlsx"
MASTERCONF = pathJoin(configdir, MASTEREXCEL)

DELIMSIGN = ""
DELIMSTR = ""

LINUXTIMEOUT = 180
WINDOWSTIMEOUT = 180
WINDOWSEXTRATIMEOUT = 300

platName = platform.system()

check_int = lambda x: (True if (isinstance(x, int)) else False)
check_str = lambda x: (True if (isinstance(x, str))  or (isinstance(x, unicode)) else False)
check_list = lambda x: (True if (isinstance(x, list)) else False)
check_dict = lambda x: (True if (isinstance(x, dict)) else False)

# Function to define the resulting mapping of excel data
mapExcelData = lambda x: str(x).strip() if isinstance(x, str) or \
               isinstance(x, unicode) else str(int(x)) if isinstance(x, int) or \
               isinstance(x, long) or isinstance(x, float) else "" if x is None else x

usrv_tag = ""
sys_ip = ""
tc_id = ""
ser_no = ""
iteration = 0

# Global remote connections object for persistent connection
telSession = None
sshSession = None

if os.name == 'nt' :
    py_path = os.path.dirname(sys.executable)
    drive = py_path.split(":")
    persist_data_file="%s:/info"%(drive[0])
else:
    from libs.core import pexpect
    persist_data_file="/info"


def setOMEnv(remoteOS=None, host=None, user=None, passwd=None, envList=[]):
    """
    Description:
        Set the OM binaries location in the environment PATH on either remote 
        Machine or local machine
        
    Input:
        remoteOS (String): Remote OS Name. 
                           Must be specified if ENV to be set on remote machine
                           else, None
        host (String): Remote machine IP.
                       Must be specified if ENV to be set on remote machine
                       else, None
        user (String): Remote machine user credential.
                       Must be specified if ENV to be set on remote machine
                       else, None
        passwd (String): Remote Machine password credential.
                         Must be specified if ENV to be set on remote machine
                         else, None
        envList (List): List of strings to be applied as environment.
    
    """
    pathSep = ""
    # Determine the environment Variable to be added
    if not envList:
        if not remoteOS:
            if platName == "Linux":
                envList = ["/opt/dell/srvadmin/sbin"]
            elif platName == "Windows":
                envList = [r"\Program Files\Dell\SysMgt\oma\bin", r"\Program Files (x86)\Dell\SysMgt\oma\bin"]
        else:
            if remoteOS == "Linux":
                envList = ["/opt/dell/srvadmin/sbin"]
            elif remoteOS == "Windows":
                envList = [r"\Program Files\Dell\SysMgt\oma\bin", r"\Program Files (x86)\Dell\SysMgt\oma\bin"]
    else:
        envList = getUniqueList(envList)
        
    # Determining the Path Separator to be used
    if not remoteOS:
        pathSep = os.pathsep  
    else:
        if remoteOS == "Linux":
            pathSep = ":"
        elif remoteOS == "Windows":
            pathSep = ";"
    
    status = 0
    # For local machine
    if not remoteOS:
        if platName == "Linux":
            # Get the current PATH ENV value
            cmd = "echo $PATH"
            res, err, status = run_local_cmd(cmd, shell=True)
            
            modCmd = ""
            res = res.split(pathSep)
            if res:
                envList.extend(res)
                envList = getUniqueList(envList)
                modCmd = "echo 'export PATH=%s' >> /etc/profile" % pathSep.join(envList)
            else:
                modCmd = "echo 'export PATH=%s:$PATH' >> /etc/profile" % pathSep.join(envList)
            
            # Take the backup of the /etc/profile 
            bckCmd = "cp -rf /etc/profile /etc/profile_bck"
            res, err, status = run_local_cmd(bckCmd, shell=True)
            
            # Modify /etc/profile
            if not err:
                res, err, status = run_local_cmd(modCmd, shell=True)
        elif platName == "Windows":
            # Get the current PATH ENV value
            cmd = "echo %PATH%"
            res, err, status = run_local_cmd(cmd, shell=True)
            
            modCmd = ""
            res = res.split(pathSep)
            if res:
                envList.extend(res)
                envList = getUniqueList(envList)
                modCmd = 'setx PATH "%s"' % pathSep.join(envList)
            else:
                modCmd = 'setx PATH "%s;%PATH%"' % pathSep.join(envList)
            
            # Update PATH ENV Variable
            if not err:
                res, err, status = run_local_cmd(modCmd, shell=True)
    # For Remote Machine
    else:
        if remoteOS == "Linux":
            # Get the current PATH ENV value
            cmd = "echo $PATH"
            res, err = run_remote_cmd(host, user, passwd, cmd, remoteOS) 
            
            modCmd = ""
            res = res.split(pathSep)
            if res:
                envList.extend(res)
                envList = getUniqueList(envList)
                modCmd = "echo 'export PATH=%s' >> /etc/profile" % pathSep.join(envList)
            else:
                modCmd = "echo 'export PATH=%s:$PATH' >> /etc/profile" % pathSep.join(envList)
            
            # Take the backup of the /etc/profile 
            bckCmd = "cp -rf /etc/profile /etc/profile_bck"
            res, err = run_remote_cmd(host, user, passwd, bckCmd, remoteOS)
            if err:
                status = 1
            
            # Modify /etc/profile
            if not err:
                res, err = run_remote_cmd(host, user, passwd, modCmd, remoteOS)
                if err:
                    status = 1
        elif remoteOS == "Windows":
            # Get the current PATH ENV value
            cmd = "echo %PATH%"
            res, err = run_remote_cmd(host, user, passwd, cmd, remoteOS)
            
            modCmd = ""
            res = res.split(pathSep)
            if res:
                envList.extend(res)
                envList = getUniqueList(envList)
                modCmd = 'setx PATH "%s"' % pathSep.join(envList)
            else:
                modCmd = 'setx PATH "%s;%PATH%"' % pathSep.join(envList)
            
            # Update PATH ENV Variable
            if not err:
                res, err = run_remote_cmd(host, user, passwd, modCmd, remoteOS)
                if err:
                    status = 1
    return status


def unSetOMEnv(remoteOS=None, host=None, user=None, passwd=None, envList=[]):
    """
    Description:
        Unset the OM binaries location in the environment PATH on either remote 
        Machine or local machine
        
    Input:
        remoteOS (String): Remote OS Name. 
                           Must be specified if ENV to be set on remote machine
                           else, None
        host (String): Remote machine IP.
                       Must be specified if ENV to be set on remote machine
                       else, None
        user (String): Remote machine user credential.
                       Must be specified if ENV to be set on remote machine
                       else, None
        passwd (String): Remote Machine password credential.
                         Must be specified if ENV to be set on remote machine
                         else, None
        envList (List): List of strings to be applied as environment.
    
    """
    pathSep = ""
    # Determine the environment Variable to be added
    if not envList:
        if not remoteOS:
            if platName == "Linux":
                envList = ["/opt/dell/srvadmin/sbin"]
            elif platName == "Windows":
                envList = [r"\Program Files\Dell\SysMgt\oma\bin", r"\Program Files (x86)\Dell\SysMgt\oma\bin"]
        else:
            if remoteOS == "Linux":
                envList = ["/opt/dell/srvadmin/sbin"]
            elif remoteOS == "Windows":
                envList = [r"\Program Files\Dell\SysMgt\oma\bin", r"\Program Files (x86)\Dell\SysMgt\oma\bin"]
    else:
        envList = getUniqueList(envList)
        
    # Determining the Path Separator to be used
    if not remoteOS:
        pathSep = os.pathsep  
    else:
        if remoteOS == "Linux":
            pathSep = ":"
        elif remoteOS == "Windows":
            pathSep = ";"
    
    status = 0
    # For local machine
    if not remoteOS:
        if platName == "Linux":
            # Get the current PATH ENV value
            cmd = "cp -rf /etc/profile_bck /etc/profile"
            res, err, status = run_local_cmd(cmd, shell=True)
            
        elif platName == "Windows":
            # Get the current PATH ENV value
            cmd = "echo %PATH%"
            res, err, status = run_local_cmd(cmd, shell=True)
            
            modCmd = ""
            res = res.split(pathSep)
            if res:
                res = getUniqueList(res)
                for elems in envList:
                    if elems in res:
                        res.remove(elems)
                modCmd = 'setx PATH "%s"' % pathSep.join(res)
            
            # Update PATH ENV Variable
            if not err:
                res, err, status = run_local_cmd(modCmd, shell=True)
    # For Remote Machine
    else:
        if remoteOS == "Linux":
            # Revert the old /etc/profile
            cmd = "cp -rf /etc/profile_bck /etc/profile"
            res, err = run_remote_cmd(host, user, passwd, cmd, remoteOS) 
            if err:
                status = 1
        elif remoteOS == "Windows":
            # Get the current PATH ENV value
            cmd = "echo %PATH%"
            res, err = run_remote_cmd(host, user, passwd, cmd, remoteOS)
            
            modCmd = ""
            res = res.split(pathSep)
            if res:
                res = getUniqueList(res)
                for elems in envList:
                    if elems in res:
                        res.remove(elems)
                modCmd = 'setx PATH "%s"' % pathSep.join(res)
            
            # Update PATH ENV Variable
            if not err:
                res, err = run_remote_cmd(host, user, passwd, modCmd, remoteOS)
                if err:
                    status = 1
    return status
        

def check_Connection_Params(host, user, passwd, port, cmd="", cmdlist=[], timeout=0, extratimeout=0):
    error = []
    if (not check_int(port)):
        error.append("Port number should be an integer value.")
    if (not check_int(timeout)):
        error.append("Timeout should be an integer value.")
    if (not check_int(extratimeout)):
        error.append("Extra timeout should be an integer value.")
    if (not check_str(host)):
        error.append("Host should be a string value.")
    if (not check_str(user)):
        error.append("Username should be a string value.")
    if (not check_str(passwd)):
        error.append("Password should be a string value.")
    if (not check_str(cmd)):
        error.append("Command should be a string value.")
    if (not check_list(cmdlist)):
        error.append("Command list should be a list.")
    ret_error = (("ERROR: " + " ".join(error)) if (error) else "")
    return ret_error


def run_telnet_cmd(host, user, passwd, cmd, port=23, timeout=15, extratimeout=0, 
                   isUser=False, persist=False):
    check_error = check_Connection_Params(host=host, user=user, passwd=passwd, \
                    port=port, cmd=cmd, timeout=timeout, extratimeout=extratimeout)
    if (check_error == ""):
        try:
            global telSession
            if not telSession:
                telSession  = TelnetConnection(host, user, passwd, port, timeout, is_user=isUser)
            result, error = telSession.execute(cmd, extratimeout)
            return result, error
        except TelnetException, e:
            return "", format_exc()
        except Exception, e:
            return "", format_exc()
        finally:
            if not persist:
                if telSession:
                    telSession.close()
                    telSession = None
    else:
        return "", check_error


def run_ssh_cmd(host, user, passwd, cmd, port=22, timeout=30, persist=False):
    check_error = check_Connection_Params(host=host, user=user, passwd=passwd, \
                    port=port, cmd=cmd, timeout=timeout)
    if (check_error == ""):
        try:
            persist = False
            sshSession = SSHConnection(host, user, passwd, port, timeout)
            res1, err1 = sshSession.Connect()
            if (err1 == ""):
                res2, err2 = sshSession.Execute(cmd)
                return res2, err2
            else:
                return res1, err1
        except Exception, e:
            return "", format_exc()
        finally:
            if not persist:
                if sshSession:
                    sshSession.Close()
                    sshSession = None
    else:
        return "", check_error


def run_remote_cmd(host, user, passwd, cmd, osType=None, isUser=False, 
                   persist=False, listFormat=False):
    result, error = ("", "")
    sleepFlag = 0
    startTime = datetime.datetime.now()
    
    if osType=="Linux":
        result, error = run_ssh_cmd(host, user, passwd, cmd, 
                                    timeout=LINUXTIMEOUT, persist=persist)
    else:
        result, error = run_telnet_cmd(host, user, passwd, cmd, 
                                       timeout=WINDOWSTIMEOUT, 
                                       extratimeout=WINDOWSEXTRATIMEOUT, 
                                       isUser=isUser, persist=persist)
    
    # Formatting the results by appending error to the result.
    error_code = 0    
    result = result.replace("\r", "")
    result = result.strip()
    if error:
        error_code = 1
        result = "%s %s".strip() % (result, error)
    elif "omconfig" in cmd:
        sleepFlag = 1
        if "-out" not in cmd and not re.search("(?<!un)success.*", result, re.IGNORECASE) \
        and re.search("error.*", result, re.IGNORECASE):
            error_code = 1
            result = "%s %s".strip() % (result, error)
    
    reobj1 = re.search("-out(c|a).*", cmd, re.IGNORECASE)
    reobj2 = re.search("[^>]> .*", cmd, re.IGNORECASE)
    reobj = reobj1 or reobj2
    if reobj:
        fileName = reobj.group(0).split()[1]
        if osType == "Linux":
            cmd2 = "cat %s" % fileName
            res, err = run_ssh_cmd(host, user, passwd, cmd2, 
                                   timeout=LINUXTIMEOUT, persist=persist)
        elif osType == "Windows":
            cmd2 = "type %s" % fileName
            res, err = run_telnet_cmd(host, user, passwd, cmd2, 
                                      timeout=WINDOWSTIMEOUT, 
                                      extratimeout=WINDOWSEXTRATIMEOUT, 
                                      isUser=isUser, persist=persist)
            
        res = res.replace("\r", "")
        res = res.strip()
        if err or re.search("^Error", res, re.M|re.I):
            error_code = 1
            result = "%s\n%s %s".strip() % (result, res, error)
        elif listFormat:
            result = [elem for elem in csv.reader(StringIO(res), delimiter=str(DELIMSIGN))]
        else:
            result = "%s\n%s".strip() % (result, res)
            
    endTime = datetime.datetime.now()
    elapsed_time = "%s" % (endTime-startTime)
    
    exec_log(cmd, "Not Available", result, startTime, endTime, elapsed_time)
    
    # Sleep for certain time period after executing OMCONFIG command as the 
    # changes will take some time to reflect 
    if sleepFlag:
        time.sleep(WAIT_TIME_AFTER_OMCONFIG_EXE_REMOTE)
    
    return result, error_code


def run_local_cmd(cmd, shell=False):
    if os.name == "posix":
        if ".sh" in cmd.lower():
            # Command is either a install or uninstall command.
            checkInventoryCollector()
    
    result, error = ("", "")
    sleepFlag = 0
    startTime = datetime.datetime.now()
    
    # output Log and Error Log
    outLog = open(outputLog, "w+")
    errLog = open(errorLog, "w+")
    process = Popen(cmd, shell=shell, stdout=outLog, stderr=errLog)
    # Reading Status code, output stream and error stream
    status_code = process.wait()
    endTime = datetime.datetime.now()
    elapsed_time = "%s" % (endTime - startTime)
    outLog.close()
    errLog.close()
    
    # Reading result and error
    with open(outputLog) as fptr1:
        result = fptr1.read() 
    with open(errorLog) as fptr2:
        error = fptr2.read()
    
    # Decoding the output data if encoding is there
    if result.startswith(codecs.BOM_UTF16):
        result = result.decode("UTF-16")
    if error.startswith(codecs.BOM_UTF16):
        error = error.decode("UTF-16")
    
    error_code = 0    
    result = result.replace("\r", "")
    result = result.strip()
    if error:
        error_code = 1
        result = "%s %s".strip() % (result, error)
    elif "omconfig" in cmd:
        sleepFlag = 1
        if "-out" not in cmd and not re.search("(?<!un)success.*", result, re.IGNORECASE):
            error_code = 1
            result = "%s %s".strip() % (result, error)
        
    reobj = re.search("-out(c|a).*.csv", cmd, re.IGNORECASE)
    if reobj:
        fileName = reobj.group(0).split()[1]
        if not pathExists(fileName):
            error_code = 1
        else:
            result = [elem for elem in csv.reader(open(fileName), delimiter=str(DELIMSIGN))]
        
    exec_log(cmd, status_code, result, startTime, endTime, elapsed_time)
    
    # Sleep for certain time period after executing OMCONFIG command as the 
    # changes will take some time to reflect 
    if sleepFlag:
        time.sleep(WAIT_TIME_AFTER_OMCONFIG_EXE)
    
    os.remove(outputLog) 
    os.remove(errorLog)   
    return result, error_code, status_code


def exec_log(cmd, status="", result="", start_time=None, end_time=None, elapsed_time=None):
    log_sv_dir = pathJoin(logsdir, sys_ip)
    if not (pathExists(log_sv_dir)):
        os.makedirs(log_sv_dir)

    if tc_id:
        execLog = pathJoin(log_sv_dir, ser_no, "exec_log")
        if not (pathExists(execLog)):
            os.makedirs(execLog)
        log_file=pathJoin(execLog,"%s_%s_log.txt"%(tc_id, iteration))
    else:
        log_file=pathJoin(log_sv_dir, "log.txt")

    f = codecs.open(log_file, "a", encoding='utf-8')
    f.write("#"*100 + "\n")
    if tc_id and pathExists(pathJoin(log_sv_dir,"log.txt")):
        fl=open(pathJoin(log_sv_dir,"log.txt"))
        init_log_data=fl.read()
        fl.close()
        deleteFile(pathJoin(log_sv_dir,"log.txt"))
        f.write(init_log_data + "\n")

    if cmd:
        f.write(str(cmd) + "\n")

    if result:
        f.write("\n")
        if isinstance(result, list):
            result = "\n".join([DELIMSIGN.join(elem) if isinstance(elem, list) else elem for elem in result]) + "\n"
        
        if isinstance(result, str):
            result = unicode(result, 'utf-8', 'ignore') + "\n"
        else:
            result += "\n"
        f.write(result)
    
    if status != "":
        f.write("\n Status: %s \n" % status)
    
    if elapsed_time is not None:
        f.write("\n")
        f.write("Elapsed Time: %s" % elapsed_time + "\n")
        f.write("Start Time: %s" % start_time + "\n")
        f.write("End Time: %s" % end_time + "\n")
    f.write("#"*100 + "\n\n")
    f.close()


def set_tc_id(t_id):
    global tc_id
    tc_id=t_id


def set_ser_no(s_no):
    global ser_no
    ser_no=s_no
    
    
def set_Delimitor(remoteOS=None, host=None, user=None, passwd=None):
    global DELIMSIGN, DELIMSTR
    
    delimRow = readMapping(MASTERCONF, "Delimiters", "Active", "yes")
    if not delimRow:
        raise ValueError("Delimiter has not been set. Please set the delimiter in config/Info.xls file.")
    else:
        DELIMSTR = delimRow[0][0]
        DELIMSIGN = delimRow[0][1]
        
    # Set the delimiter in OM environment
    cmd = "omconfig preferences cdvformat delimiter=%s" % DELIMSTR
    if remoteOS:
        run_remote_cmd(host, user, passwd, cmd, remoteOS)
    else:
        run_local_cmd(cmd, shell=True)
        
        
def get_DelimSign():
    return DELIMSIGN 


def get_DelimStr():
    return DELIMSTR       


def set_system_ip(ip):
    global sys_ip
    sys_ip = ip


def set_Iteration(value):
    global iteration
    iteration = value
    

def get_Iteration():
    return iteration


def get_tc_id():
    return tc_id


def get_ser_no():
    return ser_no


def readIpmapFile():
    status, data = readCsvFile("Controller.csv")
    if (not status):
        data = []
    return status, data


def saveIpmapFile(data):
    status, result = writeCsvFile(pathJoin(configdir, "ipmap.csv"), data)
    return status, result


def get_controllers_info():
    status, data = readIpmapFile()
    retlist = []
    if (status):
        data=data[1:]
        for row in data:
            retlist.append(row)
        return True, retlist
    else:
        return status, retlist
    
    
def get_system_config_details(ip):
    status, data = readIpmapFile()
    system_config_details=None
    if (status):
        data=data[1:]
        for row in data:
            if row[2]==ip:
                system_config_details=row
                break
    return system_config_details


def GetManagementStationIP():
    ippat = "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}"
    if (sysplatform == "win32"):
        cmd = "ipconfig"
    else:
        cmd = "ifconfig eth0"
    result, error = runcmd(cmd)
    if (error != 0):
        return False, "Unable to get Management Station IP."

    iplist = refindall(ippat, result)
    if (iplist == []):
        return False, "Unable to get Management Station IP."
    else:
        return True, iplist[0]


def getConfDict(CONFIG_FILE):
    confdict = {}
    filepath = CONFIG_FILE
    if (pathExists(filepath)):
        status, data = readFile(filepath)
        if (status):
            try:
                for i in data:
                    idx = i.find(":")
                    k = i[ 0 : idx]
                    v = i[ idx + 1: ]
                    confdict[k] = v
                    #confdict = dict( [ tuple(i.split(":")) for i in data])
            except:
                pass
    return confdict


def getReportTitle():
    reporttitle = 'OMI Test Cases Testsuite'
    return reporttitle


def get_server_name():
    server_name = ""
    cmd = "omreport system summary"
    result,err,status=run_local_cmd(cmd,shell=True)
    if (not err):
        for i in result :
            if re.search("chassis model",i,re.IGNORECASE):
                server_name = i.split(":")[1].strip()
                break
    
    return server_name


def persistData(remoteOS, host, user, passwd):
    servModel = server_model(remoteOS, host, user, passwd)
    opSysName = ""
    opSysArch = ""
    
    if remoteOS == "Linux":
        res, err = run_remote_cmd(host, user, passwd, "uname -m", remoteOS)
        if not err:
            opSysArch = res.strip()

        res1, err1 = run_remote_cmd(host, user, passwd, "cat /etc/redhat-release", remoteOS)
        if not err1 and (res1.splitlines()[0]).strip():
            opSysName = (res1.splitlines()[0]).strip()
        else:
            res2, err2 = run_remote_cmd(host, user, passwd, "cat /etc/issue", remoteOS)
            opSysName = res2.strip()
            
    elif remoteOS == "Windows":
        res, err = run_remote_cmd(host, user, passwd, "systeminfo", remoteOS)
        if not err:
            opSysArch = (re.findall("System Type *: *(.*)", res, re.I)[0]).strip()
            opSysName = (re.findall("OS Name *: *(.*)", res, re.I)[0]).strip()
            
    loc = pathJoin(logsdir, sys_ip)
    if not os.path.exists(loc):
        os.makedirs(loc)
        
    with open(pathJoin(loc, "data"), "wb") as fptr:
        data = {"servModel" : servModel, "osname" : opSysName, 
                "osarch" : opSysArch}
        pickle.dump(data, fptr)


def getReportSUTInfoDict(cntrl_name="", cntrl_id=""):
    #Function to be coded here for all Local Information
    
    ret_dict = {}

    ms_ip = system_ip()
    
    ret_dict["Operating System"] = platform.platform()
    ret_dict["System IP"] = ms_ip
    ret_dict["Python Version"] = platform.python_version()
    ret_dict["Platform"] = platform.system()
    ret_dict["Architecture"] = platform.machine() + ", " + ", ".join(platform.architecture())
    ret_dict["Server Model"] = server_model()
    ret_dict["System"] = "localhost"
    if cntrl_name and cntrl_id:
        ret_dict["Controller Name"] = "%s %s"%(cntrl_name, cntrl_id)

    return ret_dict


def getReportMNInfoDict():
    retDict = {}
    
    from config import globalVars
    with open(pathJoin(logsdir, sys_ip, "data"), "rb") as fptr:
        data = pickle.load(fptr)
        retDict["Server Model"] = data.get("servModel", "")
        retDict["MN Operating System"] = data.get("osname", "")
        retDict["Architecture"] = data.get("osarch", "")
    
    retDict["MN System IP"] = globalVars.SUTIP
    retDict ["MN IDRAC IP"] =  globalVars.IDRACIP
        
    return retDict
        

def parse_ipmi_sys_info(idrac_ip,idrac_user,idrac_pass):
    os_info=""
    product_model=""
    os_name=None
    try:
        cmd="ipmitool -I lanplus -H %s -U %s -P %s delloem sysinfo"%(idrac_ip,idrac_user,idrac_pass)
        result,err,status=run_local_cmd(cmd,shell=True)
        if re.search("System OS Name.*:.*",result,re.IGNORECASE):
            os_name=re.findall("System OS Name.*:(.*)",result)
            product_model=re.findall("Product Model.*:(.*)",result)
            if os_name:
                os_info=os_name[0].strip()
            if product_model:
                product_model=product_model[0].strip()
    except Exception,e:
        pass

    return os_info,product_model


def getOutputFileName(ip, now, filestring="output"):
    ip_dir=pathJoin(logsdir, ip)
    if not (pathExists(ip_dir)):
        os.mkdir(ip_dir)

    filename = r"%s_%s.html" % (filestring,now)
    time.sleep(5)
    f=open(pathJoin(ip_dir,filename),"w")
    f.write("")
    f.close()
    return pathJoin(ip_dir, filename)


def getPercent(total, passed):
    if total <= 0:
        percent = 0
    else:
        percent = float(100*passed)/float(total)
    return "%0.2f" % percent


def getLogDetails(srv_tag=None):
    if (pathExists(pathJoin(logsdir,srv_tag, "logdata.txt"))):
        filehandle = open(pathJoin(logsdir,srv_tag, "logdata.txt"), "r")
        datalist = filehandle.read().splitlines()
        filehandle.close()
        ddll=[]
        for idx,val in enumerate(datalist):
            ddll.append([idx+1] + val.split("|"))
        return ddll
    else:
        return []


def runcmd(cmd, input=None):
    err_code = 0
    p = Popen(cmd,shell=True,stdin=PIPE, stdout=PIPE,stderr=PIPE)
    if (input == None):
        std_out,std_err=p.communicate()
    else:
        std_out,std_err=p.communicate(input)
    if std_err:
        err_code = 1
        std_out = std_err
    return (std_out,err_code)


def getTestcaseInformation(TCID):
    book = openpyxl.load_workbook(MASTERCONF, use_iterators=True, data_only=True)
    sheet = book.get_sheet_by_name("TestcaseInfo")
    TCDetails = ""
    TCName=""
    TCAuthor=""
    counter = 0
    for xlrow in sheet.iter_rows():
        curRow = [elems.value for elems in xlrow]
        curRow = map(mapExcelData, curRow)
        counter += 1
        if counter == 1:
            header = curRow
            continue

        rtcid = None
        if curRow[0]:        
            if isinstance(curRow[0], unicode) or isinstance(curRow[0], str):
                rtcid = curRow[0]
            else:
                rtcid = str(int(curRow[0]))
            
        if rtcid == str(TCID):
            TCFeature = str(curRow[1]).strip()
            TCName = str(curRow[2]).strip()
            TCDetails = str(curRow[3]).strip()
            TCAuthor = str(curRow[6]).strip()
            TCCD = "%s" % curRow[7]
            TCMODIFIED = str(curRow[8]).strip()
            TCMD = "%s" % curRow[9]
            TCDT = str(curRow[10]).strip()
            break
         
    set_tc_id(TCID)
    ret_dict = dict((
                        ("Testcase ID", TCID),
                        ("Testcase Name", TCName),
                        ("Testcase Feature", TCFeature),
                        ("Testcase Details", TCDetails),
                        ("Testcase Author", TCAuthor),
                        ("Testcase Create Date", TCCD),
                        ("Testcase Modified By", TCMODIFIED),
                        ("Testcase Modified Date", TCMD),
                        ("Testcase Development Time", TCDT),
                    ))

    return ret_dict


def read_config(filename,section):
    """
    reading from config
    """
    config_dic={}
    try:
        config=ConfigParser()
        file=pathJoin(configdir, filename)
#        file="config/"+filename
        config.read([file])
        config_dic=dict(config.items(section))
    except Exception,e:
        print e
    return config_dic


def readConf(filename, section, parentDir=None, normalize=True):
    """
    Reading from Configuration files
    
    """
    if not parentDir:
        parentDir = os.curdir
        
    configDict = {}
    try:
        config = ConfigParser()
        if not normalize:
            config.optionxform = lambda x:x
        confFile = pathJoin(parentDir, filename)
        config.read([confFile])
        configDict = dict(config.items(section))
    except Exception,e:
        print e
    return configDict


def readKeyValue(configFile,section,keyName):
    config = ConfigParser()
    configFile=pathJoin(configFile)
    config.read(configFile)
    keyValue = config.get(section,keyName)
    return keyValue


def write_config(config_info, passed, failed, error, omitted, blocked, total, 
                 pass_percentage, starttime, stoptime, srv_tag):
    config=ConfigParser()
    ini_file=pathJoin(logsdir, srv_tag,"test_status.ini")
    file=open(ini_file,"w")
    config.add_section("test_status")
    config.set("test_status",'config_info',config_info)
    config.set("test_status",'total_count',total)
    config.set("test_status",'pass_count',passed)
    config.set("test_status",'fail_count',failed)
    config.set("test_status",'error_count',error)
    config.set("test_status",'omitted_count',omitted)
    config.set("test_status",'blocked_count',blocked)
    config.set("test_status",'pass_percentage',pass_percentage)
    config.set("test_status",'start_time',starttime)
    config.set("test_status",'end_time',stoptime)
    config.write(file)


def run_script(ip,user,password,cmd,timeout=120):
    """
    running shell script in remote machine
    """
    startTime = datetime.datetime.now()
    opmsg=""
    try:
        ssh_newkey = 'Are you sure you want to continue connecting'
        # my ssh command line
        p=pexpect.spawn('ssh %s@%s "%s"'%(user,ip,cmd),timeout=timeout)

        i=p.expect([ssh_newkey,'assword:',pexpect.EOF])
        if i==0:
            p.sendline('yes')
            i=p.expect([ssh_newkey,'assword:',pexpect.EOF])
        if i==1:
            p.sendline(password)
            p.expect([pexpect.EOF, pexpect.TIMEOUT])
        elif i==2:
            print "I either got key or connection timeout"
            pass
        opmsg=p.before # print out the result
    except Exception,e:
        opmsg=e
    finally:
        p.close()

    endTime = datetime.datetime.now()
    elapsed_time="%s"%(endTime-startTime)
    exec_log(cmd, "Not Available", opmsg, startTime, endTime, elapsed_time)
    return opmsg


def remove_directory(path):
    remove_dir(path)


def run_hang_command(ip,user,password,command,timeout=30):
    """
    running shell script in remote machine
    """        
    try:
        ssh_newkey = 'Are you sure you want to continue connecting'
        # my ssh command line
        p=pexpect.spawn('ssh %s@%s %s '%(user,ip,command),timeout=timeout)

        i=p.expect([ssh_newkey,'assword:',pexpect.EOF])
        if i==0:
            p.sendline('yes')
            i=p.expect([ssh_newkey,'assword:',pexpect.EOF])
        if i==1:
            p.sendline(password)
            p.expect(pexpect.EOF)
        elif i==2:
            print "System Hanged succesfully"
            pass
        opmsg=p.before # print out the result
        exec_log(opmsg)
    except Exception,e:
        pass


def removePreviousLog():
    for root, dirs, files in os.walk(logsdir):
        # Remove the directories
        for eachDir in dirs:
            shutil.rmtree(pathJoin(root, eachDir))
            dirs.remove(eachDir)
            
        # Remove the files
        for eachFile in files:
            if re.search("task.log|tbagent.log|job_info.txt|runtime.*.log|stdout.txt|stderr.txt", 
                         eachFile, re.I):
                continue
            else:
                os.remove(pathJoin(root, eachFile)) 
            

def get_persist_val(name):
    value=None
    try:
        pic_file=pathJoin(persist_data_file)
        op=open(pic_file,"rb")
        pic_info=pickle.load(op)
        value=pic_info.get(name)
        op.close()
    except Exception ,e:
        pass
    return value


def runIPMI(iIP, iUser, iPasswd, params):
    """
    Description:
        Running IPMI Command
        
    Input:
        iIP (String): IDRAC IP of the box
        iUser (String): IDRAC User Credentials of the box.
        iPasswd (String): IDRAC Password Credentials of the box.
        params (String): System interface to be probed through IPMI.
        
    Output:
        2-element tuple  
        
    """
    cmd = "ipmitool -I lanplus -H %s -U %s -P %s %s" \
          % (iIP, iUser, iPasswd, params)
    res, err, status = run_local_cmd(cmd, shell=True)
    
    return res, err


def waitForReboot(ip):
    rebooted=False
    for r in range(0,reboot_timeout,reboot_sleep):
        time.sleep(reboot_sleep)
        rebooted,res=ping_ip(ip)
        if rebooted:
            time.sleep(wait_time_after_reboot)
            break
    return rebooted


def ping_ip(ip,nop=4):
    """
    checking the system is accessible
    """
    cmd = "ping -c %s %s" %(nop,ip )
    result,err,status = run_local_cmd(cmd,shell=True)
    if ((re.search("Request timed out.",result, re.IGNORECASE)) or (re.search("Destination Host Unreachable", result, re.IGNORECASE)) or (re.search("100% packet loss", result, re.IGNORECASE))):
        return False, "ERROR: %s" % str(result)
    else:
        return True, result


def checkMacAccess(iIP, iUser, iPasswd, sIP):
    """
    Description:
        Check whether the machine is accessible or not.
        
    Input:
        iIP (String): IDRAC IP of the box
        iUser (String): IDRAC User Credentials of the box.
        iPasswd (String): IDRAC Password Credentials of the box.
        sIP (String) : OS IP of the box
        
    Output:
        return a 2-element tuple about the server status and the messages
        
    """
    serverON = True
    msg = []
    
    exec_log("Checking Machine Accessibility")
    # Check whether the managed node is pinging or not
    pingStatus, pingRes = ping_ip(sIP)
    if not pingStatus:
        serverON = False
        msg.append("Box is not reachable/pingable.")
        # If managed node is not pinging, check the power status of the box.
        ipmiRes, ipmiErr = runIPMI(iIP, iUser, iPasswd, "power status")
        # If the power is on for the box, power cycle the managed node.
        # Else, power on the box
        if re.search("Chassis Power is on", ipmiRes, re.IGNORECASE):
            msg.append("Power Cycle the box.")
            ipmiPCRes, ipmiPCErr = runIPMI(iIP, iUser, iPasswd, "power cycle")
            if re.search("Chassis Power Control: Cycle",ipmiPCRes, re.IGNORECASE):
                if waitForReboot(sIP):
                    msg.append("Box rebooted successfully.")
                    serverON = True
                else:
                    msg.append("Box failed to reboot. Managed Node is inaccessible.")
            else:
                msg.append("IPMI Power Cycle command failed to execute.")
        else:
            # Power on the box
            msg.append("Power On the box.")
            ipmiPURes, ipmiPUErr = runIPMI(iIP, iUser, iPasswd, "power on")
            if re.search("Chassis Power Control: Up/On", ipmiPURes, re.IGNORECASE): 
                if waitForReboot(sIP):
                    msg.append("Box rebooted successfully.")
                    serverON = True
                else:
                    msg.append("Box failed to reboot. Managed Node is inaccessible.")
            else:
                msg.append("IPMI Power On command failed to execute.")
    else:
        msg.append("Managed Node is pinging. Managed node is reachable.")
        
    exec_log("\n".join(msg))
        
    return serverON, msg


def windows_pexpect(host, user, password, command, timeout=120):
    """
    telnet connection through pexpect for long process
    """
    startTime = datetime.datetime.now()
    logined = False
    try:
        child = pexpect.spawn( 'telnet' )
        i = child.expect( [pexpect.TIMEOUT, 'telnet>'] )
        host = 'open ' + host
        child.sendline( host )
        i = child.expect( [pexpect.TIMEOUT, 'login:'] )

        if i == 0: # Unable to connect to remote host.
            print( 'Unable to Connect to remote host :ERROR! Telnet could not login. Here is what Telnet said: ' )
            print( child.before )
            logined = False
        elif i == 1: # Connect to remote host.
            child.send( user + "\r" )
            i = child.expect( [pexpect.TIMEOUT, 'password:'] )
            if i == 1:
                child.send( password + "\r" )
                i = child.expect( [pexpect.TIMEOUT, '>'] )
                if i == 0: # Unable to connect to user
                    print( 'unable to connect to user:ERROR! Telnet could not login. Here is what Telnet said: ' )
                    print( child.before )
                    logined = False
                    #else:
                    #    return child
                else:
                    logined = True
            else:
                logined = True

    except Exception ,e:
        print e
        child.close()
        print( 'Except block :ERROR! Telnet could not login. Here is what Telnet said: ' )

    opmsg=""
    if logined:
        child.send(command + "\r")
        time.sleep(10)
        i = child.expect( [pexpect.TIMEOUT, '>'], timeout=timeout )
        if i == 0: # Unable to connect to user
            opmsg=child.before
            exec_log( 'Invalid command' ,opmsg)
        else:
            opmsg=child.before
        child.close()

    endTime = datetime.datetime.now()
    elapsed_time = "%s"%(endTime-startTime)
    exec_log(command, "Not Available", opmsg, startTime, endTime, elapsed_time)
    return opmsg


def disc_param():
    disc_field={}

    ms_ip = system_ip()

    disc_field["MS OS"] = platform.platform()
    disc_field["MS System IP"] = ms_ip
    return disc_field


def readParamTemplate(full_starttime, full_stoptime, passed, failed, error,
                      omitted, blocked, total):
    duration = str(full_stoptime - full_starttime)
    full_starttime_str=full_starttime.strftime('%Y-%m-%d_%H.%M.%S')
    full_stoptime_str=full_stoptime.strftime('%Y-%m-%d_%H.%M.%S')
    disc_field=disc_param()
    data="""
    <tr>
        <td>
            <table>
                <tr><td class="tbl2">
                <b>Start Time: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Stop Time: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Duration: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Total Tests: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Status: %s Passed, %s Failed %s Error %s Omitted %s Blocked</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Python Version: %s</b>
                </td></tr>
            </table>
        </td>
        <td>
            <table>
                <tr><td class="tbl2">
                <b>Platform: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Managed Station IP: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Managed Station OS: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Architecture: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Service Tag: %s</b>
                </td></tr>
                <tr><td class="tbl2">
                <b>Server Model: %s</b>
                </td></tr>
            </table>
        </td>
    </tr>

    """%(full_starttime_str, full_stoptime_str, duration, total, passed, failed, 
         error, omitted, blocked, disc_field["Python Version"], 
         disc_field["Platform"], disc_field["System IP"], disc_field["OS"], 
         disc_field["Architecture"], disc_field["Service Tag"], 
         disc_field["Server Model"])
    return data


def checkInventoryCollector():
    """
    This function is to address the inventory collector issue.
    It checks the status of "invcol" and ..i
    if it is running it will wait for invcol to finish
    """
    noMins = 0 
    startTime = datetime.datetime.now()

    log_sv_dir = pathJoin(logsdir,sys_ip)
    if not (pathExists(log_sv_dir)):
        os.makedirs(log_sv_dir)

    logFile = pathJoin(log_sv_dir,ser_no,"inv.txt")

    # Execute the command
    while (noMins < 30 ):
        os.system("ps aux | grep inv > %s" % logFile)
        # Check if invcol is running
        finv = open(logFile, "r")
        for i in finv:
            if "invcol" in i.lower():
                # Inventory collector service is running...Wait for a max of 30 Mins
                nowTime = datetime.datetime.now()
                diffTime = nowTime - startTime
                # Covert diffTime to minutes
                noMins = (int(diffTime.seconds))/60
                exec_log("Checking Inventory Collector Completion", "Not Completed", "Wait Time : %s minute(s)" % noMins)
                time.sleep(180)
                break
                # if Inventory collector is running more than 30 mins
            else :
                nowTime = datetime.datetime.now()
                diffTime = nowTime - startTime
                noMins = (int(diffTime.seconds))/60
                exec_log("Checking Inventory Collector Completion", "Completed", "Wait Time : %s minute(s)" % noMins)
                return
        if noMins > 30 :
            nowTime = datetime.datetime.now()
            diffTime = nowTime - startTime
            noMins = (int(diffTime.seconds))/60
            # Exit from the system as Inventory collector is taking more time than usual.
            exec_log("Checking Inventory Collector", "FAILED", "Wait Time : %s minute(s)" % noMins)
            sys.exit(0) # Exit from automation to avoid deadlock.


def executeCommand(command):
    
    if os.name == "posix":
        if ".sh" in command.lower():
            # Command is either a install or uninstall command.
            checkInventoryCollector()
            
    # Excute command     
    dump = open("/dump.txt","w+")
    dumperr = open("/dumperr.txt","w+")
    startTime = datetime.datetime.now()
    runProc  = Popen(command,shell = True, stdout = dump , stderr = dumperr )
    status = runProc.wait() # Wait for process to complete 
    endTime = datetime.datetime.now()
    elapsed_time="%s"%(endTime-startTime)
    dump.close() # Close file after dumping output text 
    dumperr.close() # Close file after dumping output text
    
    # Open the dump file and read the the command output 
    dump = open("/dump.txt","r")
    dumperr = open("/dumperr.txt","r")
    cmd_output = dump.read()
    err_output = dumperr.read()

    dump.close() # Close file after dumping output text
    dumperr.close() # Close file after dumping output text

    exec_log(command,cmd_output+err_output,elapsed_time)
    os.remove("/dump.txt") # remove dump file after use.
    os.remove("/dumperr.txt") # remove dump file after use
    return(status,cmd_output,err_output)


def server_model(remoteOS=None, host=None, user=None, passwd=None):
    serverModel = ""
    cmd = ""
    osType = platName
    
    if remoteOS:
        osType = remoteOS
    
    # Determine the command to be executed based on the OS type
    if osType == "Linux":
        cmd = "dmidecode -s system-product-name"
    elif osType == "Windows":
        cmd = "systeminfo |find \"System Model:\""
    
    if cmd:
        if remoteOS:
            res, err = run_remote_cmd(host, user, passwd, cmd, osType)
        else:
            res, err, status = run_local_cmd(cmd, shell=True)
    
    if cmd and not err:
        if re.search("poweredge|powervault|precision", res, re.IGNORECASE):
            serverModel = re.findall("(poweredge.*|powervault.*|precision.*)", res, re.IGNORECASE)[0]
            serverModel = serverModel.strip()
        else:
            serverModel = ""
        
    return serverModel

    
def service_tag():
    sTag = ""
    cmd = ""
    # Determine the command to be executed based on the OS type
    if platName == "Linux":
        cmd = "dmidecode -s system-serial-number" 
    elif platName == "Windows":
        cmd = "wmic bios get serialnumber /value"
    
    if cmd:
        res, err, status = run_local_cmd(cmd, shell=True)
    
    if cmd and not err :
        if platName == "Linux":
            sTag = re.findall("^(?!#.*).*", res.strip(), re.M)
            if sTag:
                sTag = sTag[0]
        elif platName == "Windows":
            sTag = (res.split("=")[1]).strip()
            
    return sTag


def readConfigFile(configFile, section, keyName):
    config = ConfigParser()
    config.read(configFile)
    keyValue = config.get(section, keyName)
    return keyValue


def get_file_content(file):
    f=open(file)
    result=f.read()
    f.close()
    return result


def system_ip() :
    if is_windows() :
        ip_list = [ip for ip in socket.gethostbyname_ex(socket.gethostname())[2] if not ip.startswith("127.")]
        ms_ip = ip_list[0]
    else :
        cmd="ifconfig|grep inet|grep -v 127.0.0.1"
        process = Popen(cmd, shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
        ip_details, error = process.communicate()
        ms_ip=re.findall("inet .*?(\d+\.\d+\.\d+\.\d+)\s+",ip_details)[0]
        
    return ms_ip


def is_windows():
    is_win=False
    if platform.system()=="Windows":
        is_win=True
    return is_win


def get_tc_data(file):
    file_name=os.path.basename(file)
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
        location = getcwd()
        
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
            fileName = pathJoin(root, eachFile)
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


def convert_raw_string(text):
    escape_dict={'\a':r'\a','\b':r'\b','\c':r'\c','\f':r'\f','\n':r'\n',
           '\r':r'\r','\t':r'\t','\v':r'\v','\'':r'\'','\"':r'\"','\0':r'\0',
           '\1':r'\1','\2':r'\2','\3':r'\3','\4':r'\4','\5':r'\5','\6':r'\6',
           '\7':r'\7','\8':r'\8','\9':r'\9'}
    newStr=''
    for char in text:
        try: newStr += escape_dict[char]
        except KeyError: newStr += char
    return newStr


def convertLinuxPath(inputString):
    """
    Description: 
        Converts the entered string to to Linux Path format
        
    Input: 
        inputString (string) - A String to be converted
    
    Output: 
        tempPath (String) -string converted to Linux path format
         
    """
    inputString = convert_raw_string(inputString) 
    tempPath = inputString.replace("\\","/")
    return tempPath


def convertWindowsPath(inputString):
    """
    Description: 
        Converts the entered string to to Windows Parh  format
    
    Input:
        inputString (string) - A String to be converted
    
    Output: 
        tempPath (String) - String converted to Windows 
    
    """
    inputString = convert_raw_string(inputString) 
    tempPath = inputString.replace("/","\\")
    return tempPath    


def checkValidTestCases(colFilter, testCaseID, sheetName="TestcaseMatrix"):
    """
    Description:
        API which checks whether the given testcase ID is a valid test case to 
        be run on given OS architecture or not by looking into config/Info.xls
        file.
        
    Input:
        colFilter (String): Column Filter name
        testCaseID (String): Test Case ID
    
    Output:
        return True if the test case ID is a valid test case to be run on the 
        given controller else False.
     
    """
    wb = openpyxl.load_workbook(MASTERCONF, use_iterators=True, data_only=True)
    sheet = wb.get_sheet_by_name(sheetName)
    pos = None
    valid = False
    counter = 0
    for xlrow in sheet.iter_rows():
        curRow = map(mapExcelData, [elems.value for elems in xlrow])
        counter += 1
        if counter == 1:
            header = curRow
            pos = header.index(colFilter)
            continue
        
        if pos:
            rtcid = None
            if curRow[0]:        
                if isinstance(curRow[0], unicode) or isinstance(curRow[0], str):
                    rtcid = curRow[0]
                else:
                    rtcid = str(int(curRow[0]))
                    
            if rtcid == str(testCaseID):
                if curRow[pos].lower() == "yes":
                    valid = True
                break

    return valid


def getUniqueList(seq):
    """
    Description
        Getting unique elements from the given seq.
    
    """
    if isinstance(seq, tuple):
        seq = list(seq)
    elif isinstance(seq, list):
        seq = list(seq)
    else:
        raise Exception("Invalid Sequence!!!")
    
    resultantList = []
    while seq:
        elems = seq.pop(0)
        resultantList.append(elems)
        while True:
            try:
                seq.remove(elems)
            except:
                break
    
    return resultantList


def readExcel(fileName, sheetName, rowHead=None, colHead=None, rowPos=None,
              colPos=None, getAll=False, _strict=True):
    """
    Description:
        Fetch the value for given row header against given column header
    
    """
    wb = openpyxl.load_workbook(fileName, data_only=True) 
    sheet = wb.get_sheet_by_name(sheetName)
    
    # Getting row header index
    if rowPos is None and rowHead:
        rowHeader = map(mapExcelData, [elem.value for elem in sheet.columns[0]])
        if _strict:
            if rowHeader.count(rowHead) > 0:
                rowPos = rowHeader.index(rowHead)
        else:
            rowIndex = 0
            for rowElems in rowHeader:
                if rowElems in rowHead or rowHead in rowElems:
                    rowPos = rowIndex
                    break
                rowIndex += 1    
                
    # Getting column header index
    if colPos is None and colHead:
        colHeader = map(mapExcelData, [elem.value for elem in sheet.rows[0]])
        if _strict:
            if colHeader.count(colHead) > 0:
                colPos = colHeader.index(colHead)
        else:
            colIndex = 0
            for colElems in colHeader:
                if colElems in colHead or colHead in colElems:
                    colPos = colIndex
                    break
                colIndex += 1
                                
    if (rowHead and rowPos is None) or (colHead and colPos is None):
        return []
    elif rowPos is None and colPos is None:
        if getAll:
            tempList = []
            for i in xrange(sheet.max_row):
                tempList.append(map(mapExcelData, [elems.value for elems in sheet.rows[i]]))
            return tempList
        return []
    elif rowPos != None and colPos != None:
        return sheet.cell(row=rowPos+1, column=colPos+1).value
    elif rowPos == None:
        return map(mapExcelData, [elems.value for elems in sheet.columns[colPos]])
    else:
        return map(mapExcelData, [elems.value for elems in sheet.rows[rowPos]])
    
    
def readMapping(workBook, sheetName, headerName, colVal, noe=None):
    """
    Description
        Read only enabled data from Excel sheet
        
    Input:
        workBook (String) : Location of the excel 
        sheetName (String) : Name of the sheet to read the data from
        headerName (String) : Name of the column header on which the filter
                              to be applied
        colVal (String) : Value to be Filtered
        noe (Integer) : Number of elements from Leftmost index to be returned as 
                        output.
    
    """
    excelData = readExcel(workBook, sheetName, getAll=True)
    
    # Determine header index on which the filter to be applied
    header = excelData.pop(0)
    try:
        indxPos = header.index(headerName)
    except:
        indxPos = None
    
    if indxPos:
        finalList = []
        for elems in excelData:
            if re.search(colVal, elems[indxPos], re.I):
                if noe:
                    elems = elems[:noe]
                else:
                    elems.pop(indxPos)
                    
                finalList.append(elems)
            
        return finalList
    else:
        return []


def service(osType, action, serviceList=[]):
    """
    Description:
        Common API to start/stop/restart the services
    
    """
    cmd = []
    if not serviceList:
        serviceList = ["Data_Manager", "Connection_Service"]
    actualServiceList = []
    
    fileName = MASTERCONF
    sheetName = "OMServicesMatrix"
    
    for elems in serviceList:
        actualServiceList.append(readExcel(fileName, sheetName, elems, osType+"_Name"))
    
    if osType == "Windows":
        if action.lower() == "restart":
            cmd.append("net %s \"%s\" /y" % ("stop", "%s"))
            cmd.append("net %s \"%s\" /y" % ("start", "%s"))
        else:
            cmd.append("net %s \"%s\" /y" % (action, "%s"))
    elif osType == "Linux":
        cmd.append("service %s \"%s\"" % ("%s", action))
        
    for services in actualServiceList:
        for elems in cmd:
            temp_cmd = elems % services 
            run_local_cmd(temp_cmd, shell=True)
        
        
def getPlatformArch(osType):
    """
    Description:
        Determine the OS Architecture based on the OS Type
    
    """
    osArch = ""
    if osType == "Windows":
        osDrive = os.getenv("systemdrive")
        if os.path.isdir(pathJoin(osDrive, "Program Files (x86)")):
            osArch = 'x86_64'
        else:
            osArch = 'i386'
    elif osType == "Linux":
        if platform.machine() == 'x86_64':
            osArch = 'x86_64'
        elif platform.machine() == 'i686' or platform.machine() == 'i386':
            osArch = 'i386'
            
    return osArch


def isModular(remoteOS=None, host=None, user=None, passwd=None):
    """
    Description:
        Check whether the local/remote server is modular or not
        
    Input: 
        remoteOS (String): OS of the remote machine else None for local machine
        host (String): IP of the remote machine else None for local machine
        user (String): User Credentials of the remote machine else None for 
                       local machine
        passwd (String): Password Credentials of the remote machine else None 
                         for local machine
        
    Output:
        True|False
    
    """
    modular = False
    serverModel = server_model(remoteOS, host, user, passwd)
    if re.search("poweredge|powervault|precision", serverModel, re.IGNORECASE):
        serverModel = serverModel.split()[1] 
        if serverModel.lower().startswith("m") or serverModel.lower().startswith("fc"):
            modular = True
            
    return modular


def getServerGeneration(remoteOS=None, host=None, user=None, passwd=None):
    """
    Description:
        Get Server generation information
        
    Input:
        remoteOS (String): OS of the remote machine else None for local machine
        host (String): IP of the remote machine else None for local machine
        user (String): User Credentials of the remote machine else None for 
                       local machine
        passwd (String): Password Credentials of the remote machine else None 
                         for local machine
        
    Output:
        Returns server generation information as a String.
    
    """
    serverModel = server_model(remoteOS, host, user, passwd)
    if re.search("poweredge|powervault|precision", serverModel, re.IGNORECASE):
        serverModel = serverModel.split()[1] 
        if serverModel.isdigit():
            dgt = serverModel[1]
        elif len(serverModel) == 5:
            dgt = "3"
        else:
            dgt = serverModel[2]
        
        # Finding the Drac type for the box and the default Drac Type
        serverType = readExcel(MASTERCONF, "DracSG", dgt, "ServerGeneration")
        # Getting default drac in case the DRAC was not able to be determined
        if not serverType:
            serverType = readExcel(MASTERCONF, "DracSG", "DEFAULT", "ServerGeneration")
        return serverType
    
    return readExcel(MASTERCONF, "DracSG", "DEFAULT", "ServerGeneration")


def getServerType(remoteOS=None, host=None, user=None, passwd=None):
    """
    Description:
        Get Server Type(Rack(R), Modular(M), Tower(T)) Information
        
    Input:
        remoteOS (String): OS of the remote machine else None for local machine
        host (String): IP of the remote machine else None for local machine
        user (String): User Credentials of the remote machine else None for 
                       local machine
        passwd (String): Password Credentials of the remote machine else None 
                         for local machine
        
    Output:
        Returns server generation information as a String.
    
    """
    serverModel = server_model(remoteOS, host, user, passwd)
    if re.search("poweredge|powervault|precision", serverModel, re.IGNORECASE):
        serverModel = serverModel.split()[1]
        
        # Determine the Server Type
        if serverModel.lower().startswith("m") or serverModel.lower().startswith("fc"):
            return "M" 
        elif serverModel.startswith("R") or serverModel.lower().startswith("c"):
            return "R"
        elif serverModel.startswith("T"):
            return "T"
    else:
        return ""


def runCommand(cmd):
    "executes the give command"
    err_code = 0
    p = Popen(cmd,shell=True,stdin=PIPE, stdout=PIPE, stderr=PIPE)
    std_out, std_err = p.communicate()

    if std_err:
        err_code = 1
        std_out =std_err
    return (std_out, err_code)


def checkDir(dirs):
    if (os.path.isdir(dirs)):
        temp = 0
    else:
        os.makedirs(dirs)
    return


def closeSocket(filter="telnet"):
    """
    Description:
        Closes socket based on the OS Type
    
    """
    try:
        if platName == "Linux":
            cmd = "netstat -ap |grep %s"%filter
            result, error, status = run_local_cmd(cmd,shell=True)
            if result:
                processRunning = result.splitlines()
                for process in processRunning:                     
                    ptrn = re.compile(r'.*:(?P<ctrl_v>\d*) %s.*'%filter)            
                    netResult = ptrn.search(process)
                    if netResult:
                        portNumber = netResult.group("ctrl_v")
                        if portNumber:   
                            cmd = "fuser -k -n tcp %s"%portNumber
                            result, error, status = run_local_cmd(cmd, shell=True)                            
    except:
        pass
    

def getExpressServiceCode(serviceTag):  
        """
        Description:
            Get the Express Service Code. The idea here is as follows:-
            Converts the Service Tag to Express Service Code which is
            a base 10 representation of Service Tag      
                          
        Input:
            serviceTag: Service Tag of Chassis
        
        Output:
            Express Service Code 
        
        """      
        charValue = -1
        expServiceCode = 0      
        revServiceTag = ''
        try:            
            if len(serviceTag) != 7:
                raise ValueError("Service Tag is a 7 characters unique code.")
            
            for i in xrange(0,len(serviceTag)):
                revServiceTag += serviceTag[len(serviceTag) -1 - i]
                
            for i in xrange(0,len(revServiceTag)):
                if revServiceTag[i].isdigit():
                    charValue = int(revServiceTag[i])
                elif revServiceTag[i].isalpha():
                    charValue = ord(revServiceTag[i]) - 55
                expServiceCode = charValue * (36**i) + expServiceCode                           
            return expServiceCode
        except:
            return None
        
        
def checkWindowsService(sName, host, user, passwd, osType, start=False):
    dNameCMD = 'sc getkeyname "%s"' % sName
    resN, errN = run_remote_cmd(host, user, passwd, dNameCMD, osType)
    if not re.search("GetServiceKeyName FAILED", resN, re.I):
        servName = re.findall("Name *= *(.*)", resN, re.I)[0]
        
        cmd = 'sc query "%s"' % servName
        result, error = run_remote_cmd(host, user, passwd, cmd, osType)
        servState = re.findall("STATE.*:(.*)", result)
        if "running" in servState[0].lower():                           
            return "Started"
        else:
            if start:
                sCmd = 'net start "%s"' % sName
                res, err = run_remote_cmd(host, user, passwd, sCmd, osType)
                return checkWindowsService(sName, host, user, passwd, osType, start=False)
            else: 
                return "Stopped"
    else:
        return "Stopped"


def checkLinuxService(sName, host, user, passwd, osType, start=False):
    cmd = "service %s status" % sName
    result, error = run_remote_cmd(host, user, passwd, cmd, osType)
    if not re.search("(stopped)|(dead)", result, re.I):
        if re.search("(running)|((?<!in)active)", result, re.I):
            return "Started"
        else:
            if start:
                sCmd = "service %s start" % sName
                res, err = run_remote_cmd(host, user, passwd, sCmd, osType)
                return checkLinuxService(sName, host, user, passwd, osType, start=False)
            else:
                return "Stopped"
    else:
        if start:
            sCmd = "service %s restart" % sName
            res, err = run_remote_cmd(host, user, passwd, sCmd, osType)
            return checkLinuxService(sName, host, user, passwd, osType, start=False)
        else:
            return "Stopped"
    
    
def checkRemoteServStatus(sName, host, user, passwd, osType, start=False):
    if osType == "Linux":
        return checkLinuxService(sName, host, user, passwd, osType, start)
    elif osType == "Windows":
        return checkWindowsService(sName, host, user, passwd, osType, start)
    
    
def listCombinations(listoflists, curlist=[], parents=[]):
    """
    Description:
        Generator that yields all possible combinations from a list of lists.
    
        >>> a = [[1, 2], [3, 4], [5, 6], [7, 8]]
        >>> for c in listcombinations(a): print c
        ...
        [1, 3, 5, 7]
        [1, 3, 5, 8]
        [1, 3, 6, 7]
        [1, 3, 6, 8]
        [1, 4, 5, 7]
        [1, 4, 5, 8]
        [1, 4, 6, 7]
        [1, 4, 6, 8]
        [2, 3, 5, 7]
        [2, 3, 5, 8]
        [2, 3, 6, 7]
        [2, 3, 6, 8]
        [2, 4, 5, 7]
        [2, 4, 5, 8]
        [2, 4, 6, 7]
        [2, 4, 6, 8]
        
    Input:
        listoflists (List of List): Input sequence from which all the possible 
            combination has to be made.
        curlist (List): Current list under processing
        parents (List): parent lsit from the last recursion call
    
    Output:
        generator function for processing through all the possible combinations
        
    """
    if curlist == []:
        curlist = listoflists[0]
        remlist = listoflists[1:]
    else:
        remlist = listoflists
    for item in curlist:
        if len(remlist) > 0:
            for c in listCombinations(remlist[1:], remlist[0], parents+[item]):
                yield c
        else:
            yield parents+[item]
            
    
