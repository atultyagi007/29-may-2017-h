'''
Created on Aug 24, 2014

@author: dheeraj_si
'''


workloadID=""
pXEID=""
hypervisorManagementID=""
vMotionID=""
clusterPrivateID=""
iSCSIID=""
FCoE1ID=""
FC_oE2_ID=""
FIPID=""
ISC_1_ID=""
FileshareID=""
templateIdValue=""


############# Input values for cluster  ############################

HyperMgmtClusterValue = "scvmm-172.31.62.3"  # For NON R2 SCVMM

ExistingHost = "Host"

R2HyperVMgmtValue = "scvmm-172.31.62.4"  # For R2 SCVMM



############# Input values for clone VM  ############################
description = "TstHA89864"
hostname = "TstHA89864"
vmtemplatename = "Clone Template"
centosvmtemplatename = "CENTOS TEMPLATE"

#Clone Template
blockdynamicoptimization = "false"
highlyavailable = "true"
cpucount = "1"
memorymb = "8192"
startaction = "always_auto_turn_on_vm"
stopaction = "turn_off_vm"
newhost = "HstFr89864"
path = "C:\ClusterStorage\Volume1"






###### Required input for Server  #######

Adminpassword = "Dell1234"

AdminConfirmPassword = "Dell1234"

domainadminuser = "hypervadmin"

DomainAdminPsswd = "Dell1234"

Domainconfirm = "Dell1234"

target_boot_device_value = "HD"   #HD,SD,FC,iSCSI

OSImage_value = "WINDOWSNONR2"   # for non R2 image
OS_Image_valueR2 = "WINDOWSR2"  # for R2 image

os_image_version = "windows2012datacenter" # for non R2 OS version
os_image_vR2 = "windows2012r2datacenter"    # for R2 OS version 

product_key = "GQRXP-JK2GX-QVWJY-9BCHV-JP479"  # for non R2 image
prod_key_R2 = "FNY7W-693QP-VHGKR-6JPHH-R898Y"  # for  R2 image




domain_name = "ess" #ess

fqdn = "ess.delllabs.net"

GlobalPool ="-1"

serversource = "manual" #manual or pool

ServerEntry = "172.31.61.190"  # Server Entry is vaild when we use manual in serversource

managementip = "automatic"     #automatic,dns,manual

staticIP = "172.31.36.11" 

migration= "false"  #true or false, it is valid when we will use pool in serversource

ntp = "172.20.0.8"

AutogenerateHostName="true"
Volume1_size="400GB"
Volume2_size="512MB"