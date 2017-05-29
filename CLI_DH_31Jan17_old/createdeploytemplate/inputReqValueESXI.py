'''
Created on Sep 22, 2014

@author: Aman
'''

##### Common for SERVER,VM ,BareMetal Server and Minimal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #############

Adminpassword = "Dell1234"
AdminConfirmPassword = "Dell1234"

##### Common for SERVER and BareMetal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) ############

target_boot_device_value = "SD" # for test case TestCase_122411 this value will be iSCSI  and for TestCase_122412 this value will be FC
#HD,SD,FC,iSCSI,iSCSI

###### Common for Server, BareMetal Server and Minimal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #######

ntpserver= "ntp.dell.com"

migration= "false"
#true

###### Common for Server, BareMetal Server and Minimal Server( For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #######

GlobalPool ="-1"

serversource = "pool" #manual or pool

ServerEntry = "172.31.61.190"  # Server Entry is vaild when we use manual in serversource



AutogenerateHostName ='true'
# false

VMAutogenerate ='true'
# false

############# Server and Minimal Server(ESXI5.1 and ESXI 5.5)  ############################


ESXIiamge = "esxi-5.1"
# esxi-5.5 or esxi-5.1

esmmem = "false"
# true

iscsiinitiator = "hardware"
#software

managementIp = "automatic"   #automatic,dns,manual

staticIP = "172.31.36.11" 
# if you selected manual in managemetIP then put staticIP

serverpool = "-1"
server1pool = "-1"
server2pool = "-1"





####################################


workloadID=""
pXEID=""
hypervisorManagementID=""
vMotionID=""
clusterPrivateID=""
iSCSIID=""
templateIdValue=""



#######  STORAGE ####################

Iqnip = "172.31.39.227"
Vol1_Iqnip = "172.31.39.228"
Vol2_Iqnip = "172.31.39.229"


########  CLUSTER ##########


ExistingCluster = "defects"


#For VM1 or Common VM or Minimal server and Baremetal server (for RHEL,Centos,Windows) 

OSImage_value = "rhel72"
# win2012r2 0r win-2012 or redhat or Centos

product_key = "GQRXP-JK2GX-QVWJY-9BCHV-JP479"
#FNY7W-693QP-VHGKR-6JPHH-R898Y - win2012r2
#GQRXP-JK2GX-QVWJY-9BCHV-JP479 -- WIN2012 or win2012

esxiimagetype = "redhat7"
#redhat or redhat7 or windows2012 or hyperv

os_image_version = "windows2012datacenter"
#windows2012r2datacenter


#### For VM1 or common VM #############

cpucountvalue = "2"
disksizevalue = "64"
memoryvalue = "8192"




#for VM2 :-

cpu_count ="4"
disk_size ="32"
mem_val ="8192"
image_value = "red7"
# win2012r2 0r win-2012 or redhat or Centos or WIN2012
os_image_type = "redhat7"
#redhat or redhat7 or windows2012 or hyperv
prod_key= "GQRXP-JK2GX-QVWJY-9BCHV-JP479"
#FNY7W-693QP-VHGKR-6JPHH-R898Y - win2012r2
#GQRXP-JK2GX-QVWJY-9BCHV-JP479 -- WIN2012 or win2012
os_version = "windows2012datacenter"
#windows2012r2datacenter



############# Clone VM ###############

cpuvalue = "4"
diskvalue = "80"
memvalue = "8192"

clonetype = "vm"
# vm or template

vmname = "hclvm125"

sourcedatacenter = "HG125"

DataCenter = "HG125"
ClusterName = "HG125"


################# Application ################

MediaLocation = "\\172.31.54.157\razor\SQLServer2012"
sapwd = "Dell1234"
agtsvcpwd = "Dell1234"
assvcpwd = "Dell1234"
rssvcpwd = "Dell1234"
sqlsvcpwd = "Dell1234"
netdirectory = "\\172.31.54.157\razor\win2012R2\sources\sxs"






###################


numberOfDeployments="1"
scheduleddeployment='No' # Y for Yes and any other value for No
scheduledTimestamp='<scheduleDate>2015-02-11T04:26:00-06:00</scheduleDate>'

############# Import Server ###############

ImportReferenceServerIP=""


#################VolumeSize  ############
VolumeSize_1 = "512MB"
VolumeSize_2 = "512MB"
VolumeSize_3 = "512MB"
