"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Jul 15th 2015/Feb 22nd 2017
Description: Collection of all Global Variables
"""

downloadDir = "downloads"
logsDir = "logs"
configFile = "config.ini"
inputFile = "config/Input.xlsx"
serviceUriInfoFile = "config/Services.xml"
serviceUriInfo = {}
loginPayload = ""
networkConfig = ""
credentialConfig = ""
configInfo = {}
defReqWaitTime = 2
defaultWaitTime = 60
preReqStatus = False
linuxGuestOS = "rhel65"
storageAuthType = "chap"
chapUser = "grpadmin"
chapPassword = "dell1234"
credentialTag = {"SERVER":"serverCredential","STORAGE":"storageCredential","SWITCH":"iomCredential",
                 "CHASSIS":"chassisCredential","VCENTER":"vCenterCredential"}
resourceInfo = {"SERVER":[],"STORAGE":[],"VCENTER":[],"CHASSIS":[],"HYPERV":[],"SWITCH":[]}        
browserObject = None
loginObject = None
browserName = ""
applianceIP = ""
loginUser = ""
loginPassword = ""
readOnlyUser = "autoreadonly"
rosPassword = "autopassword"
standardUser = "autostandard"
currentUser = ""
backupPath = ""
backupDirusername = ""
backupDirpassword = ""
encpassword = ""
serviceTag = "ASM-831"
jsonMap = {"Testcase_4499":"Baremetal_Testcase_4499.json","esxieql_managed":"ESXI_EQL_Managed.json","esxieql":"ESXI_EQL_Converged.json","esxicplfc":"ESXI_CPL_FC.json", "esxicpliscsi":"ESXI_CPL_ISCSI_Converged.json", "skjson":"SK_TEST.json", "esxifcoe":"ESXI_FCoE.json", "esxinetapp":"ESXI_NETAPP.json", "hyperVeql":"HYPERV_EQL_Converged.json","hyperVcplfc":"HYPERV_CPL_FC.json", "hyperVcpliscsi":"HYPERV_CPL_ISCSI_Converged.json", "bareMetalexsi":"BAREMETAL_ESXI_LINUX.JSON", "baremetalWINDOWS":"BAREMETAL_WINDOWS.json", "hyperVeqliscsi":"HYPERV_EQL_ISCSI_Converged.json", "esxieqliscsi":"ESXI_EQL_ISCSI_Converged.json","esxieql_chinese":"ESXI_EQL_Chinese.json","esxicpl_chinese":"ESXI_CPL_FC_Chinese.json", "vsanvds":"VSAN_VDS.json", "bfsfc":"BFS_FC.json", "bfsiscsi":"BFS_ISCSI.json", "esxieqlDiver":"ESXI_EQL_Diverged.json", "esxicpliscsiDiver":"ESXI_CPL_ISCSI_Diverged.json", "esxieqliscsiDiver":"ESXI_EQL_ISCSI_Diverged.json", "hyperVcpliscsiDiver":"HYPERV_CPL_ISCSI_Diverged.json", "hyperVeqliscsiDiver":"HYPERV_EQL_ISCSI_Diverged.json", "baremetaladdon":"BAREMETAL_ESXI_Addon.json", "baremetalESXI":"BAREMETAL_ESXI.json", "baremetalLINUX":"BAREMETAL_LINUX.json", "esxieqlstatic":"ESXI_EQL_StaticIP.json", "esxicplstatic":"ESXI_CPL_StaticIP.json", "esxinetappstatic":"ESXI_NETAPP_StaticIP.json", "hyperveqlstatic":"HYPERV_EQL_StaticIP.json", "hypervcplstatic":"HYPERV_CPL_StaticIP.json","emc":"Testcase_NGI-TC-4516_4577.json","multipleapplication":"Multiple_Application_Install.json","hypervclsutervm":"HyperV_Cluster_VM.json","vmwareclustervcentervm":"VMWare_Cluster_vCenter_VM.json"}
networkSettings = "{'interface1':{'enablePartition':'True', 'enableRedundancy':'True', 'Port1':{'1':['autoPXE']}}}"
storageComponent = "{'Storage1':{'Type':'EqualLogic', 'Name':'', 'VolumeName':'', 'Size':'','AuthType':'','AuthUser':'','AuthPwd':'','IQNIP':''}}"
serverComponent = "{'Server1':{'TargetBootDevice':'Local Hard Drive', 'RaidLevel':'RAID 1', 'ServerPool':'Global', 'AutoGenerateHostName':'True', 'HostnamePattern':'server${num}', 'OsImage':'esxi-5.5', 'AdminPassword':'Dell1234', 'NTPServer':'128.138.141.172', 'NetworkOptions':'{}'}}"
noOfDeployments = ""
staticIP = ""
manualServer = ""
chassisIpDiscover = '172.31.61.177'

# NETWORK COMBINATIONS
# Network Combinations for ESXi + EQL
ESXI_EQL_CONVERGED_4_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":["SAN [iSCSI]"]}}}}'
ESXI_EQL_DIVERGED_4_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}}}}'
ESXI_EQL_CONVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","SAN [iSCSI]"]}, "Port2":{"1":["Public LAN"]}}}}'
ESXI_EQL_DIVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["SAN [iSCSI]"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}}}}'
ESXI_EQL_CONVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":["SAN [iSCSI]"]}, "Port3":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port4":{"1":["Public LAN"], "2":["SAN [iSCSI]"]}}}}'
ESXI_EQL_DIVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":[]}, "Port3":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port4":{"1":["Public LAN"], "2":[]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]"], "2":[]}, "Port2":{"1":["SAN [iSCSI]"]}}}}'
ESXI_EQL_CONVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}}'
ESXI_EQL_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port3":{"1":["SAN [iSCSI]"]}, "Port4":{"1":["SAN [iSCSI]"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}, "Port2":{"1":["SAN [iSCSI]"]}}}}'

ESXI_EQL_CONVERGED_2_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]"]}}}}'
ESXI_EQL_DIVERGED_2_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}}}}'
ESXI_EQL_CONVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]"]}}}}'
ESXI_EQL_DIVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}}}}'
ESXI_EQL_CONVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]"]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]"]}}}}'
ESXI_EQL_DIVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}},"interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]"], "2":[], "3":[], "4":[]}, "Port2":{"1":["SAN [iSCSI]"], "2":[], "3":[], "4":[]}}}}'
ESXI_EQL_CONVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]"]}}}}'
ESXI_EQL_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}},"interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}, "Port2":{"1":["SAN [iSCSI]"]}}}}'

# Network Combinations for ESXi + Compellent FC
ESXI_CPL_FC_DIVERGED_2_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}}, "interface2":{"fabricType":"FiberChannel"}}'
ESXI_CPL_FC_DIVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
ESXI_CPL_FC_DIVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}},"interface2":{"fabricType":"FiberChannel"}}'
ESXI_CPL_FC_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}},"interface2":{"fabricType":"FiberChannel"}}'

ESXI_CPL_FC_DIVERGED_4_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]1"]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]2"]}}}}'
ESXI_CPL_FC_DIVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration"]}, "Port2":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
ESXI_CPL_FC_DIVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":[]}, "Port3":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port4":{"1":["Public LAN"], "2":[]}}}, "interface2":{"fabricType":"FiberChannel"}}'
ESXI_CPL_FC_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'

# Network Combinations for ESXi + Compellent iSCSI
ESXI_CPL_ISCSI_CONVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]1"]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["SAN [iSCSI]2"]}}}}'
ESXI_CPL_ISCSI_DIVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]1"], "2":[], "3":[], "4":[]}, "Port2":{"1":["SAN [iSCSI]2"], "2":[], "3":[], "4":[]}}}}'
ESXI_CPL_ISCSI_CONVERGED_2 = {"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]1"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]2"]}}}}
ESXI_CPL_ISCSI_DIVERGED_2 = {"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}},"interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]1"]}, "Port2":{"1":["SAN [iSCSI]2"]}}}}

ESXI_CPL_ISCSI_CONVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":["SAN [iSCSI]1"]}, "Port3":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port4":{"1":["Public LAN"], "2":["SAN [iSCSI]2"]}}}}'
ESXI_CPL_ISCSI_DIVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":[]}, "Port3":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port4":{"1":["Public LAN"], "2":[]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]1"], "2":[]}, "Port2":{"1":["SAN [iSCSI]2"]}}}}'
ESXI_CPL_ISCSI_CONVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]1"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]2"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}}'
ESXI_CPL_ISCSI_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x1Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port3":{"1":["SAN [iSCSI]1"]}, "Port4":{"1":["SAN [iSCSI]2"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]1"]}, "Port2":{"1":["SAN [iSCSI]2"]}}}}'

# Network Combinations for ESXi + FCoE
ESXI_FCoE_CONVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["FIP Snooping","SAN [FCoE]1"], "3":["Public LAN"], "4":["Hypervisor Migration"]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["FIP Snooping","SAN [FCoE]2"], "3":["Public LAN"], "4":["SAN [iSCSI]"]}}}}'
ESXI_FCoE_CONVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "FIP Snooping", "SAN [FCoE]"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "FIP Snooping", "SAN [FCoE]"]}}}}'
ESXI_FCoE_CONVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["FIP Snooping","SAN [FCoE]1"],"3":["Public LAN"],"4":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":["FIP Snooping","SAN [FCoE]2"], "3":["Public LAN"], "4":["SAN [iSCSI]"]}}}}'
ESXI_FCoE_CONVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN", "FIP Snooping", "SAN [FCoE]"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN", "FIP Snooping", "SAN [FCoE]"]}}}}'

# Network Combinations for ESXi + NetApp
ESXI_NETAPP_CONVERGED_2_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["Fileshare"]}}}}' 
ESXI_NETAPP_CONVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","Fileshare"]}}}}'
ESXI_NETAPP_CONVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["Fileshare"]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":["Fileshare"]}}}}'
ESXI_NETAPP_CONVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","Fileshare"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","Fileshare"]}}}}'

ESXI_NETAPP_CONVERGED_4_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":["Fileshare"]}}}}'
ESXI_NETAPP_CONVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Fileshare"]}, "Port2":{"1":["Public LAN"]}}}}'
ESXI_NETAPP_CONVERGED_4_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port2":{"1":["Public LAN"], "2":["Fileshare"]}, "Port3":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"]}, "Port4":{"1":["Public LAN"], "2":["Fileshare"]}}}}'
ESXI_NETAPP_CONVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Fileshare"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Fileshare"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}}'

# Network Combinations for Hyper-V + EQL
HYPERV_EQL_CONVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]", "Hypervisor Cluster Private"]}}}}'
HYPERV_EQL_DIVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}}}}'
HYPERV_EQL_CONVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]", "Hypervisor Cluster Private"]}}}}'
HYPERV_EQL_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}},"interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]","Hypervisor Cluster Private"]}, "Port2":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}}}}'

HYPERV_EQL_CONVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","SAN [iSCSI]", "Hypervisor Cluster Private"]}, "Port2":{"1":["Public LAN"]}}}}'
HYPERV_EQL_DIVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}}}}'
HYPERV_EQL_CONVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]", "Hypervisor Cluster Private"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}}'
HYPERV_EQL_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port3":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}, "Port4":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}, "Port2":{"1":["SAN [iSCSI]", "Hypervisor Cluster Private"]}}}}'

# Network Combinations for Hyper-V + iSCSI
HYPERV_CPL_ISCSI_CONVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]1", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","SAN [iSCSI]2", "Hypervisor Cluster Private"]}}}}'
HYPERV_CPL_ISCSI_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}},"interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]1", "Hypervisor Cluster Private"]}, "Port2":{"1":["SAN [iSCSI]2", "Hypervisor Cluster Private"]}}}}'

HYPERV_CPL_ISCSI_CONVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]1", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "SAN [iSCSI]2", "Hypervisor Cluster Private"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}}'
HYPERV_CPL_ISCSI_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Public LAN"]}, "Port3":{"1":["SAN [iSCSI]1", "Hypervisor Cluster Private"]}, "Port4":{"1":["SAN [iSCSI]2","Hypervisor Cluster Private"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["SAN [iSCSI]1", "Hypervisor Cluster Private"]}, "Port2":{"1":["SAN [iSCSI]2", "Hypervisor Cluster Private"]}}}}'

# Network Combinations for Hyper-V + FC
HYPERV_CPL_FC_DIVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN", "Hypervisor Cluster Private"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
HYPERV_CPL_FC_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","Hypervisor Cluster Private"]}}},"interface2":{"fabricType":"FiberChannel"}}'

HYPERV_CPL_FC_DIVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration", "Hypervisor Cluster Private"]}, "Port2":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
HYPERV_CPL_FC_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Hypervisor Cluster Private"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'

# Network Combinations for VSAN
VSAN_CONVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb,2x1Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["VSAN"]}}}}'
VSAN_CONVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["VSAN"]}}}}'
VSAN_2_CONVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["VSAN","VSAN1"]}}}}'
VSAN_2_CONVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb,2x1Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["VSAN","VSAN1"]}}}}'

# Network Combinations for BAREMETAL
BAREMETAL_ESXI_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"]}, "Port2":{"1":["OS Installation","Hypervisor Management"]}}}}'
BAREMETAL_WINDOWS_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation"]}, "Port2":{"1":["OS Installation"]}}}}'
BAREMETAL_LINUX_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation"]}, "Port2":{"1":["OS Installation"]}}}}'
BAREMETAL_LINUX_2_4730 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Public LAN"]}}}}'
BAREMETAL_RHEL_2_TEAMING = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb,2x1Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}, "Port2":{"1":["Public LAN"]}}}}'
BAREMETAL_LINUX_2_TEAMING = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}, "Port2":{"1":["Public LAN"]}}}}'
BAREMETAL_LINUX_2_TEAMING_REDUDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}}}}'
BAREMETAL_LINUX_2_INVALID_NW = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation"]}, "Port2":{"2":["Public LAN", "Public LAN"]}}}}'
BAREMETAL_LINUX_2_TEAMING_DIVERGED_REDUDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}}}, "interface2":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["Public LAN"]}}}}'
BAREMETAL_LINUX_2_TEAMING_STATICLAN= '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Static Public LAN"]}, "Port2":{"1":["Static Public LAN"]}}}}'
BAREMETAL_LINUX_4_TEAMING = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}, "Port2":{"1":["Public LAN"]}}}}'
BAREMETAL_LINUX_2_TEAMING_PRIVATE = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Private LAN"]}, "Port2":{"1":["Private LAN"]}}}}'
BAREMETAL_RHEL_2_TC_4423 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb,2x1Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}, "Port2":{"1":["Private LAN"]}}}}'
BAREMETAL_NEGATIVE_TEST_4678 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation"]}, "Port2":{"1":["OS Installation"]}}}}'
BAREMETAL_LINUX_4_PORT_DIVERGED ='{"interface1":{"fabricType":"Ethernet","portLayout":"4x10Gb","enablePartition":"False","enableRedundancy":"False","Ports":{"Port1":{"1":["OS Installation","Public LAN"]},"Port2":{"1":["Public LAN"]},"Port3":{"1":["OS Installation","Public LAN"]},"Port4":{"1":["OS Installation","Public LAN"]}}}}'
BAREMETAL_LINUX_2_PORT_DIVERGED_2_INTERFACE_ETHER='{"interface1":{"fabricType":"Ethernet","portLayout":"2x10Gb","enablePartition":"False","enableRedundancy":"False","Ports":{"Port1":{"1":["OS Installation","Public LAN"]},"Port2":{"1":["OS Installation","Public LAN"]}}},"interface2":{"fabricType":"Ethernet","portLayout":"2x10Gb","enablePartition":"False","enableRedundancy":"False","Ports":{"Port1":{"1":["Public LAN"]},"Port2":{"1":["OS Installation","Public LAN"]}}}}'
BAREMETAL_LINUX_2_PORT_DIVERGED_2_INTERFACE_ETHER_FIBER='{"interface1":{"fabricType":"Ethernet","portLayout":"2x10Gb","enablePartition":"False","enableRedundancy":"False","Ports":{"Port1":{"1":["OS Installation","Public LAN"]},"Port2":{"1":["OS Installation","Public LAN"]}}},"interface2":{"fabricType":"FiberChannel"}}'
BAREMETAL_WINDOWS_2_LAN = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation", "Public LAN"]}, "Port2":{"1":["Public LAN"]}}}}'
BAREMETAL_LINUX = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation"]}}}}'

# Network Combination for BFS
BFS_FC ='{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
BFS_ISCSI = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["SAN [iSCSI]"]}}}}'

ESXI_EMC_FC_DIVERGED_2_REDUNDANCY_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}}, "interface2":{"fabricType":"FiberChannel"}}'
ESXI_EMC_FC_DIVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
ESXI_EMC_FC_DIVERGED_2_PARTITION = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"True", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}, "Port2":{"1":["OS Installation","Hypervisor Management"], "2":["Hypervisor Migration"], "3":["Public LAN"], "4":[]}}},"interface2":{"fabricType":"FiberChannel"}}'
ESXI_EMC_FC_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN"]}}},"interface2":{"fabricType":"FiberChannel"}}'
 
HYPERV_EMC_FC_DIVERGED_2_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN", "Hypervisor Cluster Private"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
HYPERV_EMC_FC_DIVERGED_2 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"2x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration","Public LAN","Hypervisor Cluster Private"]}}},"interface2":{"fabricType":"FiberChannel"}}'
 
HYPERV_EMC_FC_DIVERGED_4_REDUNDANCY = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"True", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management","Hypervisor Migration", "Hypervisor Cluster Private"]}, "Port2":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'
HYPERV_EMC_FC_DIVERGED_4 = '{"interface1":{"fabricType":"Ethernet", "portLayout":"4x10Gb", "enablePartition":"False", "enableRedundancy":"False", "Ports":{"Port1":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Hypervisor Cluster Private"]}, "Port2":{"1":["OS Installation","Hypervisor Management", "Hypervisor Migration", "Hypervisor Cluster Private"]}, "Port3":{"1":["Public LAN"]}, "Port4":{"1":["Public LAN"]}}}, "interface2":{"fabricType":"FiberChannel"}}'

# Raid Advance ConFiguration
RAID_ADVANCE='{"InternalVD": [{"raidLevel":"RAID 0", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Require HDD"}], "IntVDGlobalHotspares":{"number":"1", "ssd":"1"}, "ExternalVD": [{"raidLevel":"RAID 0", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Require HDD"}], "ExtVDGlobalHotspares":{"number":"0", "ssd":"1"}}'
RAID_ADVANCE_4730='{"InternalVD": [{"raidLevel":"RAID 0", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Require HDD"}, {"raidLevel":"RAID 5", "noOfDisksType":"Minimum", "noOfDisks":"3", "diskRequirements":"Any Available"}]}'
RAID_ADVANCE_USE_FIRST_TWO_DISKS='{"InternalVD": [{"raidLevel":"RAID 1", "noOfDisksType":"Exactly", "noOfDisks":"2", "diskRequirements":"First Disks"}, {"raidLevel":"Non-RAID", "noOfDisksType":"Exactly", "noOfDisks":"2", "diskRequirements":"Any Available"}], "IntVDGlobalHotspares":{"number":"1", "ssd":"1"}, "ExternalVD": [{"raidLevel":"RAID 0", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Require HDD"}], "ExtVDGlobalHotspares":{"number":"0", "ssd":"0"}}'
RAID_ADVANCE_USE_LAST_REAR_DISKS='{"InternalVD": [{"raidLevel":"RAID 1", "noOfDisksType":"Exactly", "noOfDisks":"2", "diskRequirements":"Last/Rear Disks"}, {"raidLevel":"Non-RAID", "noOfDisksType":"Exactly", "noOfDisks":"2", "diskRequirements":"Any Available"}], "IntVDGlobalHotspares":{"number":"1", "ssd":"1"}, "ExternalVD": [{"raidLevel":"RAID 0", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Require HDD"}], "ExtVDGlobalHotspares":{"number":"0", "ssd":"0"}}'
RAID_ADVANCE_TEST_4731='{"InternalVD": [{"raidLevel":"Non-RAID", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Any Available"}, {"raidLevel":"Non-RAID", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"Any Available"}]}'
RAID_ADVANCE_TEST_4729='{"InternalVD": [{"raidLevel":"RAID 1", "noOfDisksType":"Minimum", "noOfDisks":"1", "diskRequirements":"First Disks"}, {"raidLevel":"RAID 5", "noOfDisksType":"Minimum", "noOfDisks":"3", "diskRequirements":"Any Available"}]}'

# File-Path of License Files
eval_license_file_path = r'\\10.255.7.219\SELab\LAB\John\license\DDSM_30_License_Eval.xml'
perp_license_file_path_for_1 = r'\\10.255.7.219\SELab\LAB\John\license\DDSM_1_License_SW.xml'
perp_license_file_path_for_30 = r'\\10.255.7.219\SELab\LAB\John\license\DDSM_30_License_SW.xml'
perp_license_file_path_for_500 = r'\\10.255.7.219\SELab\LAB\John\license\DDSM_500_License_SW.xml'
perp_license_file_path_for_500_Setup = r'\\10.255.7.219\SELab\LAB\SaiK3\Licenses\DDSM_500_License_SW.xml'

# File-Path for ASM Catalog Files
catalogRepository = {"localPath": r"P:\HCL\ASMDecember2016_Full\ASMCatalog.xml",
                    "ftpSharePath": "ftp://100.64.1.150/Projects/HCL/CATLOG/ASMCatalog.xml",
                    "nfsPathXml":r"\\172.17.8.20\nfs\ASMDecember2016_Full\ASMCatalog.xml",
                    "nfsPathCab":r"\\10.255.7.219\SELab\LAB\Catalog\ASMJune2016\ASMCatalog.cab",
                    "nfsSourceJune":r"\\10.255.7.219\SELab\LAB\Catalog\ASMJune2016_Full\ASMCatalogWithVibs.xml",
                    "cifsPath": r"\\100.64.1.50\HCL\ASMDecember2016_Full\ASMCatalog.xml",
                     "invalidXml":r"\\10.255.7.219\SELab\LAB\Catalog\ASMJanuary2016_Full\ASMCatalog-Test.xml"}
isoRepository = {"ISO": "ftp://172.24.3.50/Projects/HCL/ISO/CentOS7.iso"}

# List of Input values for DHCP Settings
DHCP_Subnet ='172.31.41.0'
DHCP_Netmask ='255.255.255.0'
DHCP_StartingIpAddress ='172.31.41.150'
DHCP_EndingIpAddress ='172.31.41.175'
DHCP_Dns ='172.31.62.1'
DHCP_Gateway ='172.31.41.254'
DHCP_Domain ='ess'

# File-Path for Switch Bundle Files
# switchBundleRepository ={"S4810": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\S4810\FTOS-SE-9.10.0.1P15.bin",
#                        "S5000": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\S5000\FTOS-SH-9.10.0.1P15.bin",
#                        "S6000": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\S6000\FTOS-SI-9.10.0.1P13.bin",
#                        "S4820": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\S4810\FTOS-SE-9.10.0.1P15.bin",
#                        "MXL-10/40GbE": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\MIOA\FTOS-XL-9.10.0.1P13.bin",
#                        "PE-FN-2210S-IOM": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\FNIOA\FTOS-FN-9.10.0.1P15.bin",
#                        "I/O-Aggregator": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\MIOA\FTOS-XL-9.10.0.1P13.bin",
#                        "PE-FN-410S-IOM": r"\\10.255.7.219\SELab\LAB\Firmware\Force10\FNIOA\FTOS-FN-9.10.0.1P15.bin",
#                        "N3000":'',
#                        "N4000":'',
#                        "S4048_ON":r'\\10.255.7.219\SELab\LAB\Firmware\Force10\S4048-ON\FTOS-SK-9.10.0.1P18.bin',
#                        "S4048T-ON":r"\\10.255.7.219\SELab\LAB\Firmware\Force10\S4048-T\FTOS-SK-9.10.0.1P18.bin"
#                        }
switchBundleRepository ={"S4810": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\S4810\FTOS-SE-9.10.0.1P15.bin",
                         "S5000": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\S5000\FTOS-SH-9.10.0.1P15.bin",
                         "S6000": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\S6000\FTOS-SI-9.10.0.1P13.bin",
                         "S4820": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\S4810\FTOS-SE-9.10.0.1P15.bin",
                         "MXL-10/40GbE": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\MIOA\FTOS-XL-9.10.0.1P13.bin",
                         "PE-FN-2210S-IOM": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\FNIOA\FTOS-FN-9.10.0.1P15.bin",
                         "I/O-Aggregator": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\MIOA\FTOS-XL-9.10.0.1P13.bin",
                         "PE-FN-410S-IOM": r"\\100.64.1.150\Projects\ASM\Firmware\Force10\FNIOA\FTOS-FN-9.10.0.1P15.bin",
                         "N3000":'',
                         "N4000":'',
                         "S4048_ON":r'\\100.64.1.150\Projects\ASM\Firmware\Force10\S4048-ON\FTOS-SK-9.10.0.1P18.bin',
                         "S4048T-ON":r"\\100.64.1.150\Projects\ASM\Firmware\Force10\S4048-T\FTOS-SK-9.10.0.1P18.bin"
                         }

# Version Number for Storage Models
storageBundleVersion= {"Dell SC4020":"6.6.5",
                       "Dell SC8000":"6.6.5",
                       "Dell PS6110":"8.1.1" }

# File-Path for ASM Backup
ASMBackupPath={"cifs": "\\\\10.255.7.219\\SELab\\LAB\\ASMBackup"}
