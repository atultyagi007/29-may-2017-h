'''
Created on Jan 15, 2016

@author: Dheeraj.Singh
'''

logsDir = "logs"
configFile = "config.ini"
inputFile = "config/Input.xlsx"
serviceUriInfoFile = "config/Services.xml"

discPayload = "input/payload/Discovery.xml"
manageDevicePayload = "input/payload/ManageDevice.xml"
editMDPayload = "input/payload/EditManageDevice.xml"
networkPayload = "input/payload/Network.xml"
loginPayload = "input/payload/Login.xml"
serverPoolPayload = "input/payload/CreateServerPool.xml"
credentialPayload = "input/payload/Credential.xml"
timeZonePayload = "input/payload/TimeZone.xml"
ntpPayload = "input/payload/NTP.xml"
proxyPayload = "input/payload/Proxy.xml"
completeWizardPayload = "input/payload/CompleteWizard.xml"
dhcpPayload = "input/payload/DHCP.xml"
userPayload = "input/payload/CreateUser.xml"
WizardPayload= "input/payload/wizard_payload.xml"
directoryServicePayload = "input/payload/DirectoryService.xml"
configureDiscoverPayload = "input/payload/ConfigureDiscover.xml"
configureProcessPayload = "input/payload/ConfigureProcess.xml"


configureResourcePayload = "input/payload/ConfigureResource.xml"

networkConfig = "input/Network.csv"
credentialConfig = "input/Credential.csv"
discovery = "input/Discovery.csv"


editMDPayload = "input/payload/Edit_ManageDevice.xml"     

serverDiscPayload = "input/payload/Discovery_Server.xml"
manageDevicePayload = "input/payload/ManageDevice.xml"
switchDiscPayload = "input/payload/Discovery_Switch.xml"
storageDiscPayload = "input/payload/Discovery_Storage.xml"
chassisDiscPayload = "input/payload/Discover_Chassis.xml"
serverPoolPayload = "input/payload/Create_ServerPool.xml"
deploy_filename= "input/payload/Deployment.xml"
VMDiscPayload = "input/payload/Discovery_SCVMM.xml"
VCenterDiscPayload = "input/payload/Discovery_VCenter.xml"
configResourcePayload= "input/payload/Configure_Resource.xml"
EnterpriseManagerDiscPayload= "input/payload/Discovery_EMC.xml"



uriType= "Internal" # Public, Internal
userAgent ="REST API Client"
#userAgent ="python-requests/2.3.0 CPython/2.7.8 Windows/2008ServerR2"
publishedTemplateID=""
publishedTemp_filename="input/payload/published_template.xml"

serviceUriInfo = {}
publicServiceUriInfo = {}
configInfo = {}
defReqWaitTime = 2
defaultWaitTime = 30
preReqStatus = False
linuxGuestOS = "rhel65"
storageAuthType = "chap"
chapUser="grpadmin"
chapPassword="dell1234"

headers = {"Accept":"application/json", "Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate", "Connection":"keep-alive"}
apiKey = ""
apiSecret = ""
credentialTag = {"SERVER":"serverCredential","STORAGE":"storageCredential","SWITCH":"iomCredential",
                 "CHASSIS":"chassisCredential","VCENTER":"vCenterCredential"}
mdParameters = {"device_type":"deviceType","ip_address":"ipAddress","device_refid":"deviceRefId","device_model":"model",
                "device_serviceTag":"serviceTag","device_vendor":"vendor"}
mdChassisParameters = {"ip_address":"managementIP", "device_model":"model", "device_serviceTag":"serviceTag"}
resourceInfo = {"SERVER":[],"STORAGE":[],"VCENTER":[],"CHASSIS":[],"HYPERV":[],"SWITCH":[],"SCVMM":[],"COMPELLENT":[], "ElementManager":[],"NETAPP":[]}      
networkMap = {}
credentialMap = {}
ipMap = {}
userAgent = "REST API Client"

templateDir = "config/templates"
deploymentTemplatePayload = templateDir + "/Template_Deployment.xml"
scaleUpServerPayload = templateDir + "/scale.xml"


'''
   #########################################################
        <-- Parameters for Backup and restore payload 
    #########################################################
'''

sharePath='172.31.59.1:/var/nfs'      ## Path to store the backup or restore the backup
shareUsername='delladmin'
sharePassword='delladmin'
encryptionPassword='waseem654321'
BackUpandRestorePayloadPath = "input/payload/BackupAndRestore_Payload.xml"
ScheduleBackupPayload = "input/payload/Schedule_Backup_payload.xml"
schedulebackupHour = ''         ## positive integers from 1 to 23
schedulebackupMinute = ''       ## positive integer 0 or 30
schedulebackupDaysOfWeek=''     ## Values shud be : MON,TUE,WED,THU,FRI,SAT,SUN

'''
        #########################################################
        Parameters for Backup and restore payload  END  --> 
        #########################################################
'''






enableTearDownService = False  # True or False
deploymentStatus ='error'
switchModel_TestCase_112126 = 'S4810' #S4810, S5000, S6000
switchModel_TestCase_112127 = 'S4810'
switchModel_TestCase_112128 = 'S4810'
switchModel_TestCase_112129 = 'S4810'


CommonTemplatePayload =  "input/templateComponent/CommonTemplate.xml"
EquallogicComponentPayload= "input/templateComponent/EquallogicComponent.xml"
ServerComponentPayload= "input/templateComponent/ServerComponent.xml"

ServerESXICPLISCSICONVD2PART= "input/templateComponent/Server_ESXI_CPL_ISCSI_CONVERGED_2_PARTITION.xml"
ServerESXICPLISCSICONV2= "input/templateComponent/Server_ESXI_CPL_ISCSI_CONVERGED_2.xml"
ServerESXIEQLCON2PART= "input/templateComponent/Server_ESXI_EQL_CONVERGED_2_PARTITION.xml"
ServerESXIEQLCON2REDPART= "input/templateComponent/Server_ESXI_EQL_CONVERGED_2_REDUNDANCY_PARTITION.xml"
ServerESXIEQLCONV2REDU= "input/templateComponent/Server_ESXI_EQL_CONVERGED_2_REDUNDANCY.xml"
ServESXIEQLCOD2= "input/templateComponent/Server_ESXI_EQL_CONVERGED_2.xml"
ServerESXIFCOECONVE2PART= "input/templateComponent/Server_ESXI_FCOE_CONVERGED_2_PARTITION.xml"
ServerESXIFCOECOND2= "input/templateComponent/Server_ESXI_FCOE_CONVERGED_2.xml"

ServerESXIEQLCON4PART= "input/templateComponent/Server_ESXI_EQL_CONVERGED_4_PARTITION.xml"
ServerESXIEQLCON4REDPART= "input/templateComponent/Server_ESXI_EQL_CONVERGED_4_REDUNDANCY_PARTITION.xml"
ServerESXIEQLCONV4REDU= "input/templateComponent/Server_ESXI_EQL_CONVERGED_4_REDUNDANCY.xml"
ServESXIEQLCOD4= "input/templateComponent/Server_ESXI_EQL_CONVERGED_4.xml"
ServerESXIEQLDIVER4REDU= "input/templateComponent/Server_ESXI_EQL_DIVERGED_4_REDUNDANCY.xml"
ServerESXIEQLDIVER4PART= "input/templateComponent/Server_ESXI_EQL_DIVERGED_4_PARTITION.xml"
ServerESXIEQLDIVER4REDPAR= "input/templateComponent/Server_ESXI_EQL_DIVERGED_4_REDUNDANCY_PARTITION.xml"
ServESXIEQLDIVER4= "input/templateComponent/Server_ESXI_EQL_DIVERGED_4.xml"
ServerESXIEQLDIVER2REDU= "input/templateComponent/Server_ESXI_EQL_DIVERGED_2_REDUNDANCY.xml"
ServerESXIEQLDIVER2PART= "input/templateComponent/Server_ESXI_EQL_DIVERGED_2_PARTITION.xml"
ServerESXIEQLDIVER2REDPAR= "input/templateComponent/Server_ESXI_EQL_DIVERGED_2_REDUNDANCY_PARTITION.xml"
ServESXIEQLDIVER2= "input/templateComponent/Server_ESXI_EQL_DIVERGED_2.xml"

ServerESXICPLFCDIVER2PART= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_2_PARTITION.xml"
ServerESXICPLFCDIVER2REDPAR= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_2_REDUNDANCY_PARTITION.xml"
ServerESXICPLFCDIVER2REDU= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_2_REDUNDANCY.xml"
ServESXICPLFCDIVER2= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_2.xml"
ServerESXICPLFCDIVER4PART= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_4_PARTITION.xml"
ServerESXICPLFCDIVER4REDPAR= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_4_REDUNDANCY_PARTITION.xml"
ServerESXICPLFCDIVER4REDU= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_4_REDUNDANCY.xml"
ServESXICPLFCDIVER4= "input/templateComponent/Server_ESXI_CPL_FC_DIVERGED_4.xml"

ServerESXINETAPPCON2PART= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_2_PARTITION.xml"
ServerESXINETAPPCON2REDPART= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_2_REDUNDANCY_PARTITION.xml"
ServerESXINETAPPCONV2REDU= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_2_REDUNDANCY.xml"
ServESXINETAPPCOD2= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_2.xml"
ServerESXINETAPPCON4PART= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_4_PARTITION.xml"
ServerESXINETAPPCON4REDPART= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_4_REDUNDANCY_PARTITION.xml"
ServerESXINETAPPCONV4REDU= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_4_REDUNDANCY.xml"
ServESXINETAPPCOD4= "input/templateComponent/Server_ESXI_NETAPP_CONVERGED_4.xml"
ServerHyperVEQLCONV2REDU= "input/templateComponent/Server_HyperV_EQL_CONVERGED_2_REDUNDANCY.xml"

ServerHyperVEQLCONV2= "input/templateComponent/Server_HyperV_EQL_CONVERGED_2.xml"
ServerHyperVEQLCONV4REDU= "input/templateComponent/Server_HyperV_EQL_CONVERGED_4_REDUNDANCY.xml"
ServerHyperVEQLCONV4= "input/templateComponent/Server_HyperV_EQL_CONVERGED_4.xml"
ServerHyperVEQLDIVER2REDU= "input/templateComponent/Server_HyperV_EQL_DIVERGED_2_REDUNDANCY.xml"
ServerHyperVEQLDIVER2= "input/templateComponent/Server_HyperV_EQL_DIVERGED_2.xml"
ServerHyperVEQLDIVER4REDU= "input/templateComponent/Server_HyperV_EQL_DIVERGED_4_REDUNDANCY.xml"
ServerHyperVEQLDIVER4= "input/templateComponent/Server_HyperV_EQL_DIVERGED_4.xml"
ServerHyperVCPLISCSIDIVER2= "input/templateComponent/Server_HyperV_CPL_ISCSI_DIVERGED_2.xml"
ServerHyperVCPLISCSIDIVER4= "input/templateComponent/Server_HyperV_CPL_ISCSI_DIVERGED_4.xml"
ServerHyperVCPLISCSICONV2= "input/templateComponent/Server_HyperV_CPL_ISCSI_CONVERGED_2.xml"
ServerHyperVCPLISCSICONV4= "input/templateComponent/Server_HyperV_CPL_ISCSI_CONVERGED_4.xml"
ServerHyperVCPLFCDIVER2REDU= "input/templateComponent/Server_HyperV_CPL_FC_DIVERGED_2_REDUNDANCY.xml"
ServerHyperVCPLFCDIVER2= "input/templateComponent/Server_HyperV_CPL_FC_DIVERGED_2.xml"
ServerHyperVCPLFCDIVER4REDU= "input/templateComponent/Server_HyperV_CPL_FC_DIVERGED_4_REDUNDANCY.xml"
ServerHyperVCPLFCDIVER4= "input/templateComponent/Server_HyperV_CPL_FC_DIVERGED_4.xml"

ServerESXIFCOECONVE4PART= "input/templateComponent/Server_ESXI_FCOE_CONVERGED_4_PARTITION.xml"
ServerESXIFCOECOND4= "input/templateComponent/Server_ESXI_FCOE_CONVERGED_4.xml"
ServerESXICPLISCSICONVD4PART= "input/templateComponent/Server_ESXI_CPL_ISCSI_CONVERGED_4_PARTITION.xml"
ServerESXICPLISCSICONV4= "input/templateComponent/Server_ESXI_CPL_ISCSI_CONVERGED_4.xml"
ServerESXICPLISCSIDIVER2PART= "input/templateComponent/Server_ESXI_CPL_ISCSI_DIVERGED_2_PARTITION.xml"
ServerESXICPLISCSIDIVER2= "input/templateComponent/Server_ESXI_CPL_ISCSI_DIVERGED_2.xml"
ServerESXICPLISCSIDIVER4PART= "input/templateComponent/Server_ESXI_CPL_ISCSI_DIVERGED_4_PARTITION.xml"
ServerESXICPLISCSIDIVER4= "input/templateComponent/Server_ESXI_CPL_ISCSI_DIVERGED_4.xml"

hyperVServerComponentPayload= "input/templateComponent/ServerComponentHyperV.xml"
bareMetalServerComponentPayload= "input/templateComponent/ServerComponentBaremetal.xml"
minimalServerComponentPayload= "input/templateComponent/ServerComponentMinimal.xml"
raidServerComponentPayload= "input/templateComponent/ServerComponentRaid.xml"
RelatedComponentPayload = "input/templateComponent/relatedComponents.xml"
associatedCompPayload = "input/templateComponent/associatedComponents.xml"
VMWareClusterComponentPayload= "input/templateComponent/VMWareCluster.xml"
VMWareExistingClusterComponentPayload= "input/templateComponent/VMWareExistingCluster.xml"
HyperVClusterComponentPayload= "input/templateComponent/HyperVCluster.xml"
CompellentComponentPayload= "input/templateComponent/Compellent_storage.xml"
netAPPComponentPayload= "input/templateComponent/NetAPPComponent.xml"
VMWareVMComponentPayload= "input/templateComponent/VMWareVM.xml"
CloneVMhyperVComponentPayload= "input/templateComponent/CloneVMhyperV.xml"
LinuxPostInstallComponentPayload= "input/templateComponent/LinuxPostInstallAPP.xml"
WindowsPostInstallComponentPayload= "input/templateComponent/WindowsPostInstallApp.xml"
MsSQL2012AppComponentPayload= "input/templateComponent/MsSQL2012App.xml"




EqualLogic_Components_Id = '572D3D5E-307C-4FB2-B877-4F60A94294B'
EqualLogic_componentID= 'component'
EqualLogicComponent_Name = 'EqualLogic'

Compellent_Components_Id = '572D3D5E-307C-4FB2-B877-4F60A94294B'
Compellent_componentID= 'component'
CompellentComponent_Name = 'Compellent'

NetApp_Components_Id = '572D3D5E-337C-4FB2-B877-4F60A94294B'
NetApp_componentID= 'component'
NetAppComponent_Name = 'NetApp'

Server_Components_Id = '08bec8f4-0be3-41c0-9220-b6809a1504d'
Server_componentID= 'component'
ServerComponent_Name = 'Server'

VMWareCluster_Components_Id = 'FF72FBAE-78C7-4BC3-B5D4-6B48E0EEEC0'
VMWareCluster_componentID= 'component'
VMWareClusterComponent_Name = 'VMWare Cluster'

HyperVCluster_Components_Id = 'F711EFB9-4453-487B-915C-0597FF05393'
HyperVCluster_componentID= 'component'
HyperVClusterComponent_Name = 'Hyper-V Cluster'


VMWareVM_Components_Id = '1DDB8A08-2FB5-432F-B343-0D8F7B32100'
VMWareVM_componentID= 'component'
VMWareVMComponent_Name = 'vCenter Virtual Machine'

CloneVMhyperV_Components_Id = '473d5c27-7fca-44c0-8e07-867324271c1'
CloneVMhyperV_componentID= 'component'
CloneVMhyperVComponent_Name = 'Clone Hyper-V Virtual Machine'



LinuxPostInstall_Components_Id = '28F14520-5C7A-4A69-9C42-1043F4A7700'
LinuxPostInstall_componentID= 'component'
LinuxPostInstall_Name = 'linux_postinstall'

WindowsPostInstall_Components_Id = '28F14520-5C7A-4A69-9C42-1043F4A7700'
WindowsPostInstall_componentID= 'component'
WindowsPostInstall_Name = 'windows_postinstall'


MsSQL2012_Components_Id = '28F14520-5C7A-4A69-9C42-1043F4A7700'
MsSQL2012_componentID= 'component'
MsSQL2012_Name = 'mssql2012'



#######################################################################################

Exsi_EQL_ConvJsonPayload= "input/jsonFile/Esxi_EQL_Converged.json"
Exsi_EQL_DiverJsonPayload= "input/jsonFile/Esxi_EQL_Diverged.json"
bareMetalFlowJsonPayload= "input/jsonFile/bareMetalFlowJSON.json"
Exsi_NETAPP_ConvJsonPayload= "input/jsonFile/Esxi_NETAPP_Converged.json"
Exsi_FCoE_No_BrocadeJsonPayload= "input/jsonFile/Esxi_FCoE_No_Brocade.json"
Exsi_FCoE_With_BrocadeJsonPayload= "input/jsonFile/Esxi_FCoE_With_Brocade.json"
Esxi_CPL_ISCSI_ConvPayload= "input/jsonFile/Esxi_CPL_ISCSI_Converged.json"
Esxi_CPL_ISCSI_DiverPayload= "input/jsonFile/Esxi_CPL_ISCSI_Diverged.json"
Esxi_CPL_FC_DiverPayload= "input/jsonFile/Esxi_CPL_FC_Diverged.json"
Exsi_FCoE_FC_Flex_IOAJsonPayload= "input/jsonFile/Esxi_FCoE_FC_Flex_IOA.json"
Exsi_FCoE_FC_Flex_MXLJsonPayload= "input/jsonFile/Esxi_FCoE_FC_Flex_MXL.json"

HyperV_EQL_ConvJsonPayload= "input/jsonFile/HyperV_EQL_Converged.json"
HyperV_EQL_DiverPayload= "input/jsonFile/HyperV_EQL_Diverged.json"
HyperV_CPL_ISCSI_DiverPayload= "input/jsonFile/HyperV_CPL_ISCSI_Diverged.json"
HyperV_CPL_ISCSI_ConvPayload= "input/jsonFile/HyperV_CPL_ISCSI_Converged.json"
HyperV_CPL_FC_DiverPayload= "input/jsonFile/HyperV_CPL_FC_Diverged.json"


hyperVFlowJsonPayload= "input/jsonFile/hyperVFlowJSON.json"
raidFlowJsonPayload= "input/jsonFile/raidFlowJSON.json"
minimalFlowJsonPayload= "input/jsonFile/minimalFlowJSON.json"
bootFromSANFlowJSONPayload= "input/jsonFile/bootFromSANFlowJSON.json"
iSCSICompellentHyperVFlowJSONPayload= "input/jsonFile/iSCSICompellentHyperVFlowJSON.json"

multiServiceEsxiFlowPayload= "input/jsonFile/multiServiceEsxiFlowJSON.json"
multiServiceHyperVFlowPayload= "input/jsonFile/multiServiceHyperVFlowJSON.json"





workloadID=""
workloadName=""
pXEID=""
pXEName=""
hypervisorManagementID=""
hypervisorManagementName=""
vMotionID=""
vMotionName=""
clusterPrivateID=""
clusterPrivateName=""
iSCSIID=""
iSCSIName=""
FCoE1ID=""
FCoE1Name=""
FC_oE2_ID=""
FC_oE2_Name=""
FIPID=""
FIPName=""
ISC_1_ID=""
ISC_1_Name=""
FileshareID=""
FileshareName=""
templateIdValue=""

workloadIDNetwork=""
pXEIDNetwork=""
hypervisorManagementIDNetwork=""
vMotionIDNetwork=""
clusterPrivateIDNetwork=""
iSCSIIDNetwork=""
FCoE1IDNetwork=""
FC_oE2_IDNetwork=""
FIPIDNetwork=""
ISC_1_IDNetwork=""
FileshareIDNetwork=""



template_name= ""
template_description=""
serverPoolName=""
apiKey = ""
apiSecret = ""
userName = "admin"
domain = "ASMLOCAL"
password = "admin"


refIdVCenter=""
refIdEQLogic=""
refIdSCVMM=""


##############################################################################################
#   Datacenter, cluster, VM names for VCenter Validation
##############################################################################################
datacenterName = ''
clusterName = ''
VMs=[]

#datacenter and cluster name for scvmm validation
dcNamescvmm=''
clusterNamescvmm=''
##############################################################################################

##### Common for SERVER,VM ,BareMetal Server and Minimal Server(For RHEL,Centos,Winoows, ESXI5.1 and ESXI5.5) #############

Adminpassword = "Dell1234"
AdminConfirmPassword = "Dell1234"


ntpserver= "172.20.0.8"

migration= "false"
#true

managementIp = "automatic"   #automatic,dns,manual

#staticIP = "172.31.36.11" 
# if you selected manual in managemetIP then put staticIP



GlobalPool ="-1"




#### For VM1 or common VM #############

cpucountvalue = "2"
disksizevalue = "64"
memoryvalue = "8192"




#for VM2 :-

cpu_count ="4"
disk_size ="32"
mem_val ="8192"



numberOfDeployments="1"
scheduleddeployment='No' # Y for Yes and any other value for No
scheduledTimestamp='<scheduleDate>2015-02-11T04:26:00-06:00</scheduleDate>'

scaleupStorageComponent =0
scaleupServerComponent =0
scaleupClusterComponent =0
scaleupVmComponent =0
scaleupApplicationComponent =0

