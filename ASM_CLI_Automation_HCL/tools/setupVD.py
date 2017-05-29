'''
Created on Oct 29, 2014

@author: Suman_P
'''
import os
import datetime
import re
import time
import shutil
from subprocess import Popen, PIPE

cwd = os.path.dirname(__file__)
logsDir = os.path.join(cwd,'logs')
logFileName = os.path.join(logsDir, "vdlog.txt")
controllerFQDD = "RAID.Integrated.1-1"
WAIT_TIME_AFTER_REBOOT=300
REBOOT_TIMEOUT=2700
REBOOT_SLEEP=180
raidLevel = "1"

raidDisks = {"0":1,"1":2, "5":3, "6":4, "10":4, "50":6, "60":8}
raidLevelName = {"0":"r0", "1":"r1", "5":"r5", "6":"r6", "10":"r10", "50":"r50", "60":"r60"}
finalPassStatus = {}
finalFailStatus = {}


def clearLogs():
    """
    Clears Logs
    """
    for root, dirs, files in os.walk(logsDir):        
        # Remove the directories
        for eachDir in dirs:
            shutil.rmtree(os.path.join(root, eachDir))
            dirs.remove(eachDir)
        # Remove the files
        for eachFile in files:
            os.remove(os.path.join(root, eachFile)) 
    
def writeLogs(message, cmd = "", startTime=None, endTime=None, elapsedTime=None):
    with open(logFileName,'a') as wfp:        
        wfp.write("#"*100 + "\n")        
        wfp.write("Command: %s \n"%cmd)        
        wfp.write(message + "\n")
        if elapsedTime is not None:            
            wfp.write("\n")
            wfp.write("Elapsed Time: %s" % elapsedTime + "\n")
            wfp.write("Start Time: %s" % startTime + "\n")
            wfp.write("End Time: %s" % endTime + "\n")
        wfp.write("#"*100 + "\n")
        
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
    result = "Output: %s Error: %s"%(stdOUT,stdERR)
    writeLogs(result, cmd, startTime, endTime, elapsedTime)     
    if stdERR:
        return stdERR, False
    return stdOUT, True

def clearJobs(idracIP):
    """
    Clear LC Jobs
    """
    cmd = "racadm -r %s -u %s -p %s jobqueue delete --all"%(idracIP, "root", "calvin")
    result, status = run_local_cmd(cmd)
    if not status:
        return "Failed to clear the job store", False
    if 'ALL job(s) was cancelled' in result:
        return "Successfully cleared job store", True        
    else:
        return "Failed to clear the job store", False

def removeFile(fileName):
    """
    Removes File
    """
    if os.path.exists(fileName): 
        os.remove(fileName)

def writeFile(filePath, content):
    """
    Writes to specified File
    """
    with open(filePath ,'w') as wfp:        
        wfp.writelines(content)

def racReset(idracIP):
    """
    Reset iDRAC
    """    
    cmd = "racadm -r %s -u %s -p %s racreset"%(idracIP, "root", "calvin")
    result, status = run_local_cmd(cmd)
    if not status:        
        return "Failed to Reset Configuration", False
    else:
        return "Successfully completed Reset Configuration", True    

def resetConfiguration(controllerFQDD, idracIP):
    """
    Reset Configuration
    """
    cmd = "racadm -r %s -u %s -p %s raid resetconfig:%s"%(idracIP, "root", "calvin", controllerFQDD)    
    result, status = run_local_cmd(cmd)
    if not status:        
        return "Failed to Reset Configuration", False
    else:
        return "Successfully completed Reset Configuration", True    

def reboot(idracIP):
    """
    Reset Configuration
    """
    cmd = "racadm -r %s -u %s -p %s serveraction powercycle"%(idracIP, "root", "calvin")
    result, status = run_local_cmd(cmd)    
    if not status:        
        return "Failed to Reboot Server", False
    else:
        return "Successfully initiated reboot", True  
        
def waitForReboot(idracIP):
    """
    Wait for system reboot
    """
    result = ""
    for _ in range(0, REBOOT_TIMEOUT, REBOOT_SLEEP):
        time.sleep(REBOOT_SLEEP)        
        cmd = "ping %s -n %s" %(idracIP, 4)
        result, status = run_local_cmd(cmd)
        if ((re.search("Request timed out.",result, re.IGNORECASE)) or (re.search("Destination Host Unreachable", result, re.IGNORECASE)) or (re.search("100% packet loss", result, re.IGNORECASE)) or (re.search("General failure", result, re.IGNORECASE))):
            continue
        else:
            return "Successfully Rebooted Server", True
    return "ERROR: %s" % str(result), False  

def getData(cmd, searchStr='', limit=None, 
                searchHeader=None, *args, **kwargs):
    """
    Description:
        API to get the data matched against searchStr from the output of 
        the command limited to number of results defined by limit as 
        dicitonary
    """
    searchStr = searchStr.strip()        
    result, status = run_local_cmd(cmd)
    if 'STOR0104' in result or 'STOR0110' in result:
        return []
    resList = []       
    tempdict = {}
    result = result.splitlines()
    ptrn = '^'+searchHeader
    for row in result:
        if not row:                                     
            continue            
        if re.search(ptrn,row):                
            if len(tempdict) == 0:
                tempdict["ID"] = str(row).strip()
            else:
                resList.append(tempdict)
                tempdict = {}                    
                tempdict["ID"] = str(row).strip()
        else:
            if len(tempdict) > 0:
                temprow = row.split('=')
                tempdict[str(temprow[0]).strip()] = str(temprow[1]).strip()
            else:
                continue                                           
    #if len(resList) == 0 and tempdict:
    if tempdict:
        resList.append(tempdict)    
    resultSet = []
    if args:
        for data in resList:
            tempDict = [[keyElem, data[keyElem]]for keyElem in args if keyElem in data]
            if tempDict:
                resultSet.append(dict(tempDict))
                
            if limit and len(resultSet) == limit:
                break
    elif kwargs:
        for data in resList:
            flag = 1
            for elems in kwargs: 
                if elems in data:
                    if kwargs[elems].lower() != data[elems].lower():
                        flag = 0
                        break
                else:
                    flag = 0
                    break
                    
            if flag:
                resultSet.append(data)
                
            if limit and len(resultSet) == limit:
                break
    else:
        if limit:
            resultSet = resList[:limit]
        else:
            resultSet = resList

    return resultSet

def getControllerID(idracIP):
    """
    Description:
        Get Controller ID form the Controller Name
    """
    cmd = 'racadm -r %s -u %s -p %s raid get controllers -o'%(idracIP, "root", "calvin")
    getctrls = getData(cmd, searchHeader='RAID.')
    if len(getctrls) > 0:         
        return getctrls[0]["ID"], True
    else:
        return "Failed to get Controller FQDD", False

def getPhysicalDisks(idracIP, limit=None, *args, **kwargs):
    '''
    Returns Physical Disks
    ''' 
    cmd = 'racadm -r %s -u %s -p %s raid get pdisks -o'%(idracIP, "root", "calvin")
    getpds = getData(cmd, limit=limit, searchHeader='Disk.', *args, **kwargs)
    if len(getpds) == 0:
        return "NO existing Physical Disks in the system", False
    else:
        if limit != 0:
            getpds = getpds[:limit]
        return getpds, True

def createVirtualDisk(controllerFQDD, idracIP):
    """
    Create Virtual Disk
    """
    #cntrlId = getControllerID()    
    minDisks = raidDisks[raidLevel]
    result, status = getPhysicalDisks(idracIP, limit=minDisks, **{'Status' : 'Ok', 'State' : 'Ready', "Hotspare" : "No", "MediaType" : "HDD"})
    if not status:
        return "Required Physical Disks not available ==> Required : %s Available: %s"%(minDisks, len(result)), False
    pdiskID = [elem['ID'] for elem in result]
    cmd = "racadm -r %s -u %s -p %s raid createvd:%s -name OS -rl %s -size %s -pdkey:%s"%(idracIP, 
                                "root", "calvin", controllerFQDD, raidLevelName[raidLevel], '100g', ",".join(pdiskID))
    result, status = run_local_cmd(cmd)
    if 'success' not in result:
        return "Failed to create Virtual Disk Job", False
    else:
        return "Successfully created Virtual Disk Job", True
    
def createJob(controllerFQDD, idracIP):
    """
    Create LC Job
    """    
    log = ""
    jobId = ""
    cmd = "racadm -r %s -u %s -p %s jobqueue create %s -s TIME_NOW"%(idracIP, "root", "calvin", controllerFQDD)
    result, status = run_local_cmd(cmd)
    if 'JID_' not in result:
        return "Failed to create LC Job", jobId, False
    else:
        log += "Successfully created Configuration Job \n"
        jobId = re.search("JID = JID_(.+\w)", result, re.DOTALL)
        if jobId:
            jobId= 'JID_'+ jobId.group(1)
            log += "Successfully fetched Job ID \n"
            return log, jobId, True
        else:
            log += "Unable to fetch Job ID \n"
            return log, jobId, False
        reboot(idracIP)

def verifyJobStatus(jobId, idracIP):
    """
    Verify VD Job Status
    """
    cmd = "racadm -r %s -u %s -p %s jobqueue view -i %s"%(idracIP, "root", "calvin", jobId)
    result, status = run_local_cmd(cmd)
    if 'Status=Completed' in result:
        if 'Job completed successfully' in result:
            return "1"
        else:
            return "0"
    else:
        return "2"

def getPowerStatus(idracIP):
    """
    Fetches Power Status of the Server
    """
    cmd = 'racadm -r %s -u %s -p %s serveraction powerstatus'%(idracIP, "root", "calvin")
    result, status = run_local_cmd(cmd)
    if 'Server power status:' in result:
        if 'Server power status: ON' in result:
            return "ON", True
        elif 'Server power status: OFF' in result:
            return "OFF", True
    return "", False

def setPowerStatus(idracIP, state="ON"):
    """
    Sets Power Status of the Server
    """
    if state == "ON":
        cmd = 'racadm -r %s -u %s -p %s serveraction powerup'%(idracIP, "root", "calvin")
    else:
        cmd = 'racadm -r %s -u %s -p %s serveraction powerdown'%(idracIP, "root", "calvin")
    result, status = run_local_cmd(cmd)
    if 'success' in result:
        return "Successfully change power state to: %s"%state, True
    else:
        return "Failed to change power state to: %s"%state, False

def completeVDOperation(serverList):
    """
    Complete VD operation
    """    
    jobIds = {}
    createVDList = []
    
    #Clear Logs
    clearLogs()
    
    for server in serverList:
        resultPass= []
        resultFail= []        
        finalPassStatus[server] = resultPass
        finalFailStatus[server] = resultFail
        
        #Clear All Jobs
        result, status = clearJobs(server)
        print result
        if status:
            resultPass.append(result)
        else:
            resultFail.append(result)
        
        #Reset Configuration
        result, status = resetConfiguration(controllerFQDD, server)
        print result
        if status:
            resultPass.append(result)
        else:
            resultFail.append(result)
        time.sleep(2)
        
        #Create Job
        log, jobId, status = createJob(controllerFQDD, server)
        print log
        if status:
            resultPass.append(log)
            jobIds[server] = jobId
        else:
            resultFail.append(log)
            
        finalPassStatus[server] = resultPass
        finalFailStatus[server] = resultFail
    
    resultPass= []
    resultFail= []
    
    #Verify Job Status
    loop = 10
    while loop > 0:
        for server, jobId in jobIds.items():
            if server not in resultFail and server not in resultPass:        
                status = verifyJobStatus(jobId, server)
                print server, status
                if status == "0":
                    resultFail.append(server)
                elif status == "1":
                    resultPass.append(server)
                    createVDList.append(server)
                else:
                    continue
        time.sleep(30)
        if len(jobIds) == (len(resultPass) + len(resultFail)):
            break
        loop = loop - 1
    
    for server in resultPass:
        x = finalPassStatus[server]
        x.append("Reset Configuration Job completed Successfully IP: %s"%server)
        finalPassStatus[server]  = x
    
    for server in resultFail:
        x = finalFailStatus[server]
        x.append("Reset Configuration Job failed IP: %s"%server)
        finalFailStatus[server]  = x
    
    jobIds = {}
    resultPass= []
    resultFail= []
    
    for server in createVDList:
        #Clear All Jobs
        result, status = clearJobs(server)
        print result
        if status:
            resultPass.append(result)
        else:
            resultFail.append(result)

        #Create VD
        result, status = createVirtualDisk(controllerFQDD, server)
        print result
        if status:
            resultPass.append(result)
            #Create Job
            log, jobId, status = createJob(controllerFQDD, server)
            print log
            if status:
                resultPass.append(log)
                jobIds[server] = jobId
            else:
                resultFail.append(log)
        else:
            resultFail.append(result)
    
    #Verify Job Status
    loop = 10
    while loop > 0:
        for server, jobId in jobIds.items():
            if server not in resultFail and server not in resultPass:        
                status = verifyJobStatus(jobId, server)
                print server, status
                if status == "0":
                    resultFail.append(server)
                elif status == "1":
                    resultPass.append(server)
                    createVDList.append(server)
                else:
                    continue
        time.sleep(30)
        if len(jobIds) == (len(resultPass) + len(resultFail)):
            break
        loop = loop - 1
    
    for server in resultPass:
        x = finalPassStatus[server]
        x.append("Reset Configuration Job completed Successfully IP: %s"%server)
        finalPassStatus[server]  = x
    
    for server in resultFail:
        x = finalFailStatus[server]
        x.append("Reset Configuration Job failed IP: %s"%server)
        finalFailStatus[server]  = x
    
    for server in finalPassStatus.items():
        for msg in finalPassStatus[server]:
            writeLogs(msg)
    
    for server in finalFailStatus.items():
        for msg in finalFailStatus[server]:
            writeLogs(msg)
    
    writeLogs("Passed %s : Count: %s"%(str(finalPassStatus.keys()), len(finalPassStatus)))
    writeLogs("Failed %s : Count: %s"%(str(finalFailStatus.keys()), len(finalFailStatus)))
        
if __name__ == "__main__":
    server126 = ["172.31.64.3", "172.31.64.10", "172.31.64.14", "172.31.64.15", "172.31.64.16", "172.31.64.17", "172.31.64.18","172.31.64.19","172.31.64.20","172.31.64.24", 
                 "172.31.64.25","172.31.64.26", "172.31.64.27","172.31.64.28", "172.31.64.54","172.31.64.55"]
    server127 = ["172.31.64.29","172.31.64.30", "172.31.64.38","172.31.64.39", "172.31.64.40","172.31.64.44", "172.31.64.45","172.31.64.46","172.31.64.47","172.31.64.48", 
                 "172.31.64.49", "172.31.64.50"]
    server77 = ["172.31.80.93", "172.31.80.94","172.31.80.95", "172.31.80.96","172.31.80.97", "172.31.80.99", "172.31.80.100", "172.31.80.101","172.31.80.104", 
                "172.31.80.106","172.31.80.107"] 
    server109 = ["172.31.80.114", "172.31.80.115", "172.31.80.116","172.31.80.117", "172.31.80.118","172.31.80.120", "172.31.80.121", 
                "172.31.80.123", "172.31.80.124","172.31.80.125", "172.31.80.127", "172.31.80.146", "172.31.80.151"]   
    serverList = ["172.31.64.3", "172.31.64.14"]
    #completeVDOperation(serverList)
    #===========================================================================
    # for server in serverList:
    #     print server, setPowerStatus(server, state="OFF")
    #===========================================================================

