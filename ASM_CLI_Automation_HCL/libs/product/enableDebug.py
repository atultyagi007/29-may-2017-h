'''
Created on Nov 7, 2014

@author: P.Suman
'''

from fabric.api import *
import os

filePath = os.path.abspath("tools/enable_debug.sh")

def copyFile():    
    put(filePath,'/tmp',mode=777)

def stopFirewall():
    sudo("service iptables stop")
 
def runEnableDebug():
    sudo("/tmp/enable_debug.sh")
    
def checkFile():
    sudo("test -f /tmp/enable_debug.sh && echo 'File exists' || echo 'The File Does Not Exist'")

