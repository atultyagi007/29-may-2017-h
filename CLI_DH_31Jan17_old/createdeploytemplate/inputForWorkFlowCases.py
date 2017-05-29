'''
Created on May 28, 2015

@author: dheeraj_si
'''

##### Common for SERVER,VM ,BareMetal Server and Minimal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #############

Adminpassword = "Dell1234"
AdminConfirmPassword = "Dell1234"

StdUserName_0 ='Standard User'
StdUserName_1 ='Standrad User1'
StdUserName_2 ='Standrad User2'
Std_User_Password_0 = ''
Std_User_Password_1 = ''
Std_User_Password_2 = ''
StdUserDomain = 'ASMLOCAL'
StdUserRole = 'standard'


##### Common for SERVER and BareMetal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) ############

target_boot_device_value = "SD"
target_boot_device_iSCSI = "iSCSI"
#,SD,FC,iSCSI

###### Common for Server, BareMetal Server and Minimal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #######

ntpserver= "172.20.0.8"

migration= "false"
#true

###### Common for Server, BareMetal Server and Minimal Server( For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #######

GlobalPool ="-1"
serversource = "pool"
#manual or pool
ServerEntry = "ff8080814bc9c539014bca65d3ea0528"
# Server Entry is vaild when we use manual in serversource

############# Server and Minimal Server(ESXI5.1 and ESXI 5.5)  ############################


ESXIiamge_5_1 = "esxi-5.1"
ESXIiamge_5_5 = "esxi-5.5"
# esxi-5.5 or esxi-5.1

esmmem = "false"
# true


managementIp = "automatic"
#automatic,dns,manual

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


#For RHEL VM 

cpucountvalue = "2"
disksizevalue = "64"
memoryvalue = "8192"
OSImage_value = "red7"
# win2012r2 0r win-2012 or redhat or Centos

product_key = "GQRXP-JK2GX-QVWJY-9BCHV-JP479"
#FNY7W-693QP-VHGKR-6JPHH-R898Y - win2012r2
#GQRXP-JK2GX-QVWJY-9BCHV-JP479 -- WIN2012 or win2012

esxiimagetype = "redhat7"
#redhat or redhat7 or windows2012 or hyperv

os_image_version = "windows2012datacenter"
#windows2012r2datacenter



#for Window  VM :-

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


###################


numberOfDeployments="1"
scheduleddeployment='No' # Y for Yes and any other value for No
scheduledTimestamp='<scheduleDate>2015-02-11T04:26:00-06:00</scheduleDate>'



ExistingVol_2=""
ExistingVol_1=''


############# WorkFlow1_Case1 ###############
serversour='manual'
ServerEntry_0=''
ServerEntry_1=''

############# WorkFlow1_Case2 ###############
serversour='pool'
ServerEntry_2=''
ServerEntry_3=''

############# WorkFlow2_Case1###############
serversour='manual'
ServerEntry_20=''
ServerEntry_21=''
ServerEntry_22=''
ServerEntry_23=''
############# WorkFlow2_Case2###############
ServerEntry_24=''
ServerEntry_25=''
############# WorkFlow3_Case1 ###############

ServerEntry_16=''
ServerEntry_17=''
ServerEntry_18=''
ServerEntry_19=''
############# WorkFlow4_Case1 ###############

ServerEntry_4=''
ServerEntry_5=''

############# WorkFlow4_Case2###############

ServerEntry_6=''
ServerEntry_7=''

############# WorkFlow5_Case1 ###############

ServerEntry_8=''
ServerEntry_9=''
ServerEntry_10=''
ServerEntry_11=''

############# WorkFlow5_Case2 ###############

ServerEntry_12=''
############# WorkFlow6_Case1 ###############
ServerEntry_13=''
ServerEntry_14=''

############# WorkFlow6_Case2 ###############
ServerEntry_15=''

############# WorkFlow7_Case1 ###############
ServerEntry_26=''

############# WorkFlow8_Case1 ###############
ServerEntry_27=''
ServerEntry_28=''

############# WorkFlow9_Case1 ###############
ServerEntry_29=''
ServerEntry_30=''


#################################### input values for HyperV Templates ########################


DomainAdminPsswd = "Dell1234"
Domainconfirm = "Dell1234"


OS_Image_valueR2 = "win2012r2"


os_image_vR2 = "windows2012r2datacenter"


prod_key_R2 = "FNY7W-693QP-VHGKR-6JPHH-R898Y"

ntp = "172.24.0.240"
domain_name = "asmtest"
fqdn = "asmtest.local"


HyperMgmtClusterValue = "scvmm-172.21.194.64"
#NewClusterName = "HCLTestCluster"
ClusterIpaddress = "172.21.195.42"

domainadminuser = "hypervuser"

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




#Added for host by payal
ExistingHost = "Host"

R2HyperVMgmtValue = "scvmm-172.31.62.4"

