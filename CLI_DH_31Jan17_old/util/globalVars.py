'''
Created on Jul 24, 2014

'''

configFile = "../../config/config.ini"
serviceUriInfoFile = "../../config/Services.xml"
publicserviceUriInfoFile = "../../config/Services_Public.xml"
loginPayload = "../../input/payload/login_payload.txt"
loginUserPayload = "../../input/payload/login_user_payload.txt"
licensePayload = "../../input/payload/license_payload.txt"
timezonePayload = "../../input/payload/timezone_payload.txt"
NTPPayload = "../../input/payload/ntp_payload.txt"
ProxyPayload = "../../input/payload/proxy_payload.txt"
WizardPayload= "../../input/payload/wizard_payload.txt"
ServercredPayload ="../../input/payload/server_cred.txt"
networkPayload = "../../input/payload/Network.xml"
credentialPayload = "../../input/payload/Credential.xml"
networkConfig= "../../input/Network.csv"
credentialConfig = "../../input/Credential.csv"
userPayload="../../input/payload/Create_User.xml"
createUserPayload="../../input/payload/Create_User_payload.xml"
directoryServicePayload="../../input/payload/DirectoryService.xml"
completeWizardPayload = "../../input/payload/CompleteWizard.xml"
configureResourcePayload = "../../input/payload/ConfigureResource.xml"
dhcpPayload = "../../input/payload/DHCP.xml"
filename_Import_server_payload="../../input/payload/Importserverpayload.txt"
WF_scaleUp_Admin_payload = "Workflow2_Case1_scaleup_dupserver_admin.txt"

'''
#########################################################
<-- Input Parameters for Create User payload  START
#########################################################
'''
user_name='wasRO'
userPwd='standard12345'
user_role='standard'  ### values can be : Administrator,standard,ReadOnly
userDomain='ASMLOCAL'

user_name2=''
userPwd2=''
user_role2=''  ### values can be : Administrator,standard,ReadOnly
userDomain2=''
aDDOnModulePath='ftp://100.64.1.150/Projects/HCL/AddOnModule/SaySomething.zip'

filename_TestCase_99872 = '../../input/payload/TestCase_99872.txt'

Std_User_scaleUp_payload = '../../input/payload/WF_StdUser_server_scaleup.txt'


'''
        #########################################################
        Input Parameters for Create User payload   END  --> 
        #########################################################
        
    #########################################################
        <-- Parameters for Backup and restore payload 
    #########################################################
'''

sharePath='172.31.59.1:/var/nfs'      ## Path to store the backup or restore the backup
shareUsername='delladmin'
sharePassword='delladmin'
encryptionPassword='waseem654321'
BackUpandRestorePayloadPath = "../../input/payload/BackupAndRestore_Payload.xml"
ScheduleBackupPayload = "../../input/payload/Schedule_Backup_payload.xml"
schedulebackupHour = ''         ## positive integers from 1 to 23
schedulebackupMinute = ''       ## positive integer 0 or 30
schedulebackupDaysOfWeek=''     ## Values shud be : MON,TUE,WED,THU,FRI,SAT,SUN

'''
        #########################################################
        Parameters for Backup and restore payload  END  --> 
        #########################################################
'''


'''
        #########################################################
        <-- Payloads for HyperV Cases  Start 
        #########################################################
'''

filename_TestCase_78803="../../input/templates_hyperV/TestCase_78803.txt"
filename_TestCase_78804="../../input/templates_hyperV/TestCase_78804.txt"
filename_TestCase_78805="../../input/templates_hyperV/TestCase_78805.txt"
filename_TestCase_78806="../../input/templates_hyperV/TestCase_78806.txt"
filename_TestCase_78807="../../input/templates_hyperV/TestCase_78807.txt"
filename_TestCase_78808="../../input/templates_hyperV/TestCase_78808.txt"
filename_TestCase_78809="../../input/templates_hyperV/TestCase_78809.txt"
filename_TestCase_78810="../../input/templates_hyperV/TestCase_78810.txt"
filename_TestCase_78811="../../input/templates_hyperV/TestCase_78811.txt"
filename_TestCase_89862="../../input/templates_hyperV/TestCase_89862.txt"
filename_TestCase_89864="../../input/templates_hyperV/TestCase_89864.txt"
filename_TestCase_82126="../../input/templates_hyperV/TestCase_82126.txt"
filename_TestCase_82129="../../input/templates_hyperV/TestCase_82129.txt"
filename_ScaleUp_82126="../../input/templates_hyperV/TestCase_Scaleup_82126.txt"
filename_ScaleUp_82129="../../input/templates_hyperV/TestCase_Scaleup_82129.txt"

filename_BladeServer_TestCase_78805="../../input/templates_hyperV/BladeServer_TestCase_78805.txt"


filename_TestCase_77216="../../input/templates_hyperV/TestCase_77216.txt"
filename_TestCase_77217="../../input/templates_hyperV/TestCase_77217.txt"
filename_TestCase_77218="../../input/templates_hyperV/TestCase_77218.txt"
filename_TestCase_77219="../../input/templates_hyperV/TestCase_77219.txt"
filename_TestCase_78913="../../input/templates_hyperV/TestCase_78913.txt"
filename_TestCase_78914="../../input/templates_hyperV/TestCase_78914.txt"
filename_TestCase_78915="../../input/templates_hyperV/TestCase_78915.txt"
filename_TestCase_78916="../../input/templates_hyperV/TestCase_78916.txt" 
filename_TestCase_89917="../../input/templates_hyperV/TestCase_89917.txt"
filename_TestCase_77211="../../input/templates_hyperV/TestCase_77211.txt"
filename_TestCase_77215="../../input/templates_hyperV/TestCase_77215.txt"
filename_TestCase_77452="../../input/templates_hyperV/TestCase_77452.txt"


filename_ScaleUp_89917="../../input/templates_hyperV/TestCase_Scaleup_89917.txt"

'''
        #########################################################
        Payloads for HyperV Cases  END  --> 
        #########################################################
'''
filename_TestCase_storage="../../input/templates_esxi/Storagetemplate.txt"
filename_TestCase_exportTemplate="../../input/templates_esxi/ExportTemplate.txt"
filename_TestCase_ESXITemplate="../../input/templates_esxi/TestCase_BasicFlow.txt"
filename_TestCase_MigrateServer="../../input/templates_esxi/TestCase_MigrateServer.txt"
filename_TestCase_BFSMigrateServer="../../input/templates_esxi/ServerMigTest.txt"
filename_TestCase_assignedUser="../../input/templates_esxi/Storagetemplate_assignedUser.txt"
filename_TestCase_assignedUserService="../../input/templates_esxi/Storagetemplate_assignedUserService.txt"
filename_TestCase_storageNoPermissionforStandardUser="../../input/templates_esxi/StoragetempWpermissionStandardUser.txt"
filename_TestCase_updatestorage="../../input/templates_esxi/Updatetemplate.txt"
filename_TestCase_copyTemp="../../input/templates_esxi/TestCase_copyTemp.txt"
filename_TestCase_ServiceTemp_Update="../../input/templates_esxi/serviceTempUpdate.txt"
filename_TestCase_81190="../../input/templates_esxi/TestCase_81191.txt"
filename_TestCase_81191="../../input/templates_esxi/TestCase_81191.txt"
filename_TestCase_Temp="../../input/templates_esxi/TestCase_serviceTemp.txt"
filename_TestCase_81192="../../input/templates_esxi/TestCase_81192.txt"
filename_TestCase_81193="../../input/templates_esxi/TestCase_81193.txt"
filename_TestCase_81194="../../input/templates_esxi/TestCase_81194.txt"
filename_TestCase_81195="../../input/templates_esxi/TestCase_81195.txt"
filename_TestCase_81196="../../input/templates_esxi/TestCase_81196.txt"
filename_TestCase_81197="../../input/templates_esxi/TestCase_81197.txt"
filename_TestCase_81198="../../input/templates_esxi/TestCase_81198.txt"
filename_TestCase_81199="../../input/templates_esxi/TestCase_81199.txt"
filename_TestCase_88170="../../input/templates_esxi/TestCase_88170.txt"
filename_Testcase_0000_before_scaleup="../../input/templates_esxi/Testcase_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_0000_before_scaleup="../../input/templates_esxi/Testcase_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_82125_before_scaleup="../../input/templates_esxi/Testcase_82125_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_82126_before_scaleup="../../input/templates_esxi/Testcase_82126_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_82127_before_scaleup="../../input/templates_esxi/Testcase_82127_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_82128_before_scaleup="../../input/templates_esxi/Testcase_82128_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_82129_before_scaleup="../../input/templates_esxi/Testcase_82129_before_scaleup_server_storage_cluster_vm.txt"
filename_Testcase_82131_before_scaleup="../../input/templates_esxi/Testcase_82131_before_scaleup_server_storage_cluster_vm.txt"

filename_TestCase_82125_scaleup_storage="../../input/templates_esxi/TestCase_82125_after_scaleup_storage.txt"
filename_TestCase_82126_scaleup_server="../../input/templates_esxi/TestCase_82126_after_scaleup_server.txt"
filename_TestCase_82127_scaleup_vm="../../input/templates_esxi/TestCase_82127_after_scaleup_VM.txt"
filename_TestCase_82128_scaleup_3storage="../../input/templates_esxi/TestCase_82128_after_scaleup_3Storage.txt"
filename_TestCase_82129_scaleup_2server="../../input/templates_esxi/TestCase_82129_after_Scaleup_2 server.txt"
filename_TestCase_82131_scaleup_4vm="../../input/templates_esxi/TestCase_82131_after_scaleup_4VM.txt"
filename_TestCase_88167_scaleup_serv_storage="../../input/templates_esxi/TestCase_88167_after_sacleup_server_storage_VM.txt"


filename_Testcase_before_102213_scaleup="../../input/templates_esxi/Testcase_before_102213_scaleup_storage.txt"
filename_Testcase_before_102214_scaleup="../../input/templates_esxi/Testcase_before_102214_scaleup_server.txt"
filename_TestCase_102214_after_scaleup_server="../../input/templates_esxi/TestCase_102214_after_scaleup_server.txt"
filename_TestCase_102213_after_scaleup_2storage="../../input/templates_esxi/TestCase_102213_after_scaleup_2storage.txt"


filename_TestCase_102215_after_scaleup_Cluster="../../input/templates_esxi/TestCase_102215_after_scaleup_Cluster.txt"
filename_Testcase_before_102215_scaleup_Cluster="../../input/templates_esxi/Testcase_before_102215_scaleup_Cluster.txt"


filename_TestCase_102216_after_scaleup_VM="../../input/templates_esxi/TestCase_102216_after_scaleup_VM.txt"
filename_Testcase_before_102216_scaleup_vm="../../input/templates_esxi/Testcase_before_102216_scaleup_vm.txt"


filename_TestCase_88169_after_scaleup_server="../../input/templates_esxi/TestCase_88169_after_scaleup_server.txt"
filename_Testcase_before_88169_scaleup_server="../../input/templates_esxi/Testcase_before_88169_scaleup_server.txt"


filename_TestCase_before_104062_scaleup_compellentstorage="../../input/templates_flexIoa/TestCase_before_104062_scaleup_compellentstorage.txt"
filename_TestCase_104062_after_scaleup_compellentstorage="../../input/templates_flexIoa/TestCase_104062_after_scaleup_compellentstorage.txt"


filename_TestCase_before_104064_scaleup_bladeserver="../../input/templates_flexIoa/TestCase_before_104064_scaleup_bladeserver.txt"
filename_TestCase_104064_after_scaleup_bladeserver="../../input/templates_flexIoa/TestCase_104064_after_scaleup_bladeserver.txt"


filename_TestCase_before_104068_scaleup_bladeserver="../../input/templates_flexIoa/TestCase_before_104068_scaleup_bladeserver.txt"
filename_TestCase_104068_afte_scaleup_bladeserver="../../input/templates_flexIoa/TestCase_104068_afte_scaleup_bladeserver.txt"


filename_TestCase_before_104070_scaleup_compellentstorage="../../input/templates_flexIoa/TestCase_before_104070_scaleup_compellentstorage.txt"
filename_TestCase_104070_after_scaleup_compellentstorage="../../input/templates_flexIoa/TestCase_104070_after_scaleup_compellentstorage.txt"


filename_TestCase_before_104073_scaleup_bladeserver="../../input/templates_flexIoa/TestCase_before_104073_scaleup_bladeserver.txt"
filename_TestCase_104073_afte_scaleup_bladeserver="../../input/templates_flexIoa/TestCase_104073_afte_scaleup_bladeserver.txt"


filename_TestCase_102190="../../input/templates_fcoe_s500withoutbrocade/TestCase_102190.txt"
filename_TestCase_102192="../../input/templates_fcoe_s500withoutbrocade/TestCase_102192.txt"
filename_TestCase_102193="../../input/templates_fcoe_s500withoutbrocade/TestCase_102193.txt"
filename_TestCase_102195="../../input/templates_fcoe_s500withoutbrocade/TestCase_102195.txt"
filename_TestCase_102202="../../input/templates_fcoe_s500withoutbrocade/TestCase_102202.txt"
filename_TestCase_102204="../../input/templates_fcoe_s500withoutbrocade/TestCase_102204.txt"
filename_TestCase_102205="../../input/templates_fcoe_s500withoutbrocade/TestCase_102205.txt"
filename_TestCase_102207="../../input/templates_fcoe_s500withoutbrocade/TestCase_102207.txt"

filename_TestCase_102118="../../input/templates_fcoe_withbrocade/TestCase_102118.txt"
filename_TestCase_102120="../../input/templates_fcoe_withbrocade/TestCase_102120.txt"
filename_TestCase_102121="../../input/templates_fcoe_withbrocade/TestCase_102121.txt"
filename_TestCase_102123="../../input/templates_fcoe_withbrocade/TestCase_102123.txt"
filename_TestCase_102130="../../input/templates_fcoe_withbrocade/TestCase_102130.txt"
filename_TestCase_102132="../../input/templates_fcoe_withbrocade/TestCase_102132.txt"
filename_TestCase_102133="../../input/templates_fcoe_withbrocade/TestCase_102133.txt"
filename_TestCase_102135="../../input/templates_fcoe_withbrocade/TestCase_102135.txt"

filename_TestCase_102142="../../input/templates_fcoe_nobrocade/TestCase_102142.txt"
filename_TestCase_102144="../../input/templates_fcoe_nobrocade/TestCase_102144.txt"
filename_TestCase_102145="../../input/templates_fcoe_nobrocade/TestCase_102145.txt"
filename_TestCase_102147="../../input/templates_fcoe_nobrocade/TestCase_102147.txt"
filename_TestCase_102154="../../input/templates_fcoe_nobrocade/TestCase_102154.txt"
filename_TestCase_102156="../../input/templates_fcoe_nobrocade/TestCase_102156.txt"
filename_TestCase_102157="../../input/templates_fcoe_nobrocade/TestCase_102157.txt"
filename_TestCase_102159="../../input/templates_fcoe_nobrocade/TestCase_102159.txt"

filename_TestCase_104150="../../input/payload/TestCase_104150.txt"
filename_TestCase_104148="../../input/payload/TestCase_104148.txt"
filename_TestCase_104146="../../input/payload/TestCase_104146.txt"
filename_TestCase_104147="../../input/payload/TestCase_104147.txt"
filename_TestCase_104149="../../input/payload/TestCase_104149.txt"
filename_TestCase_104145="../../input/payload/TestCase_104145.txt"
file_name_manageddivice="../../input/payload/ManagedDevice.xml"



filename_TestCase_104061="../../input/templates_flexIoa/TestCase_104061.txt"
filename_TestCase_104069="../../input/templates_flexIoa/TestCase_104069.txt"



filename_TestCase_110628="../../input/templates_fx2/TestCase_110628.txt"
filename_TestCase_110629="../../input/templates_fx2/TestCase_110629.txt"
filename_TestCase_110631="../../input/templates_fx2/TestCase_110631.txt"
filename_TestCase_110632="../../input/templates_fx2/TestCase_110632.txt"
filename_TestCase_110633="../../input/templates_fx2/TestCase_110633.txt"
filename_TestCase_110634="../../input/templates_fx2/TestCase_110634.txt"
filename_TestCase_110635_before_scaleup_storage="../../input/templates_fx2/TestCase_110635_before_scaleup_storage.txt"
filename_TestCase_110635_after_scaleup_storage="../../input/templates_fx2/TestCase_110635_after_scaleup_storage.txt" 
filename_TestCase_110636_before_scaleup_server="../../input/templates_fx2/TestCase_110636_before_scaleup_server.txt"
filename_TestCase_110636_after_scaleup_server="../../input/templates_fx2/TestCase_110636_after_scaleup_server.txt"
filename_TestCase_110637="../../input/templates_fx2/TestCase_110637.txt"




filename_TestCase_89863="../../input/templates_cloning/TestCase_89863.txt"
filename_TestCase_89865="../../input/templates_cloning/TestCase_89865.txt"
filename_TestCase_89866="../../input/templates_cloning/TestCase_89866.txt"



filename_TestCase_112188="../../input/templates_raid/TestCase_112188.txt"
filename_TestCase_112189="../../input/templates_raid/TestCase_112189.txt"
filename_TestCase_112191="../../input/templates_raid/TestCase_112191.txt"
filename_TestCase_112193="../../input/templates_raid/TestCase_112193.txt"
filename_TestCase_112192="../../input/templates_raid/TestCase_112192.txt"
filename_TestCase_112190="../../input/templates_raid/TestCase_112190.txt"
filename_TestCase_112195="../../input/templates_raid/TestCase_112195.txt"
filename_TestCase_112197="../../input/templates_raid/TestCase_112197.txt"
filename_TestCase_112194="../../input/templates_raid/TestCase_112194.txt"
filename_TestCase_112196="../../input/templates_raid/TestCase_112196.txt"
filename_TestCase_112198="../../input/templates_raid/TestCase_112198.txt"
filename_TestCase_112199="../../input/templates_raid/TestCase_112199.txt"
filename_TestCase_112201="../../input/templates_raid/TestCase_112201.txt"
filename_TestCase_112203="../../input/templates_raid/TestCase_112203.txt"
filename_TestCase_112202="../../input/templates_raid/TestCase_112202.txt"
filename_TestCase_112200="../../input/templates_raid/TestCase_112200.txt"
filename_TestCase_112205="../../input/templates_raid/TestCase_112205.txt"
filename_TestCase_112207="../../input/templates_raid/TestCase_112207.txt"
filename_TestCase_112206="../../input/templates_raid/TestCase_11206.txt"
filename_TestCase_112204="../../input/templates_raid/TestCase_112204.txt"
filename_TestCase_112208="../../input/templates_raid/TestCase_112208.txt"
filename_TestCase_112209="../../input/templates_raid/TestCase_112209.txt"
filename_TestCase_112210="../../input/templates_raid/TestCase_112210.txt"
filename_TestCase_115719="../../input/templates_raid/TestCase_115719.txt"
filename_TestCase_115720="../../input/templates_raid/TestCase_115720.txt"
filename_TestCase_115721="../../input/templates_raid/TestCase_115721.txt"


filename_TestCase_107262="../../input/templates_baremetal/TestCase_107262.txt"
filename_TestCase_107263="../../input/templates_baremetal/TestCase_107263.txt"
filename_TestCase_107264="../../input/templates_baremetal/TestCase_107264.txt"
filename_TestCase_107265="../../input/templates_baremetal/TestCase_107265.txt"
filename_TestCase_107266="../../input/templates_baremetal/TestCase_107266.txt"
filename_TestCase_107267="../../input/templates_baremetal/TestCase_107267.txt"



filename_TestCase_107285="../../input/templates_minimalserver/TestCase_107285.txt"
filename_TestCase_107286="../../input/templates_minimalserver/TestCase_107286.txt"
filename_TestCase_107287="../../input/templates_minimalserver/TestCase_107287.txt"
filename_TestCase_107442="../../input/templates_minimalserver/TestCase_107442.txt"
filename_TestCase_107443="../../input/templates_minimalserver/TestCase_107443.txt"
filename_TestCase_107444="../../input/templates_minimalserver/TestCase_107444.txt"
filename_TestCase_107445="../../input/templates_minimalserver/TestCase_107445.txt"
filename_TestCase_107446="../../input/templates_minimalserver/TestCase_107446.txt"
filename_TestCase_107447="../../input/templates_minimalserver/TestCase_107447.txt"
filename_TestCase_107448_before_scaleup_minimalserver="../../input/templates_minimalserver/TestCase_107448_before_scaleup_minimal server.txt"
filename_TestCase_107449_before_scaleup_rackserver="../../input/templates_minimalserver/TestCase_107449_before_scaleup_rackserver.txt"
filename_TestCase_107450_before_scaleup_CSeriesserver="../../input/templates_minimalserver/TestCase_107450_before_scaleup_CSeriesserver.txt"
filename_TestCase_107448_after_scaleup_minimalserver="../../input/templates_minimalserver/TestCase_107448_after_scaleup_minimalserver.txt"
filename_TestCase_107449_after_scaleup_rackserver="../../input/templates_minimalserver/TestCase_107449_after_scaleup_rackserver.txt"
filename_TestCase_107450_after_scaleup_CSeriesserver="../../input/templates_minimalserver/TestCase_107450_after_sacleup_CSeriesserver.txt"
filename_TestCase_109194="../../input/templates_minimalserver/TestCase_109194.txt"



filename_TestCase_107315="../../input/templates_multipleservice/TestCase_107315.txt"
filename_TestCase_107316="../../input/templates_multipleservice/TestCase_107316.txt"
filename_TestCase_107317="../../input/templates_multipleservice/TestCase_107317.txt"
filename_TestCase_107318="../../input/templates_multipleservice/TestCase_107318.txt"
filename_TestCase_107319="../../input/templates_multipleservice/TestCase_107319.txt"
filename_TestCase_107457="../../input/templates_multipleservice/TestCase_107457.txt"
filename_TestCase_107458="../../input/templates_multipleservice/TestCase_107458.txt"
filename_TestCase_107459="../../input/templates_multipleservice/TestCase_107459.txt"
filename_TestCase_107460="../../input/templates_multipleservice/TestCase_107460.txt"
filename_TestCase_107461="../../input/templates_multipleservice/TestCase_107461.txt"
filename_TestCase_107462="../../input/templates_multipleservice/TestCase_107462.txt"



'''
        #########################################################
        <-- Payloads for Workflows Cases  Start 
        #########################################################
'''

filename_WorkFlow1_Case1="../../input/templates_workflows/Workflow1/WorkFlow1_Case1.txt"
filename_WorkFlow1_Case2="../../input/templates_workflows/Workflow1/WorkFlow1_Case2.txt"
filename_WorkFlow1_Case2_scaleup="../../input/templates_workflows/Workflow1/WorkFlow1_Case2_after_sacleup_server_storage_VM.txt"
filename_WorkFlow2_Case1="../../input/templates_workflows/Workflow2/WorkFlow2_Case1.txt"
filename_WorkFlow2_Case2="../../input/templates_workflows/Workflow2/WorkFlow2_Case2.txt"
filename_WorkFlow2_Case1_scaleup="../../input/templates_workflows/Workflow2/WorkFlow2_Case1_scaleup_server_stduser.txt"
filename_WorkFlow2_Case2_scaleup_admin = "../../input/templates_workflows/Workflow2/WorkFlow2_Case1_scaleup_dupserver_admin.txt"
filename_WorkFlow3_Case1="../../input/templates_workflows/Workflow3/WorkFlow3_Case1.txt"
filename_WorkFlow3_Case1_scaleup="../../input/templates_workflows/Workflow3/WorkFlow3_Case1_scaleup_server.txt"
filename_WorkFlow4_Case1="../../input/templates_workflows/Workflow4/WorkFlow4_Case1.txt"
filename_WorkFlow4_Case2="../../input/templates_workflows/Workflow4/WorkFlow4_Case2.txt"
filename_WorkFlow4_Case2_scaleup="../../input/templates_workflows/Workflow4/WorkFlow4_Case2_after_sacleup_server_storage_VM.txt"
filename_WorkFlow5_Case1="../../input/templates_workflows/Workflow5/WorkFlow5_Case1.txt"
filename_WorkFlow5_Case2="../../input/templates_workflows/Workflow5/WorkFlow5_Case2.txt"
filename_WorkFlow5_scaleup_server_admin="../../input/templates_workflows/Workflow5/WorkFlow5_Case2_scaleup_server_adminuser.txt"
filename_WorkFlow5_scaleup_storage_admin="../../input/templates_workflows/Workflow5/WorkFlow5_Case2_scaleup_storage_adminuser.txt"
filename_WorkFlow5_scaleup_storage_stduser="../../input/templates_workflows/Workflow5/WorkFlow5_Case2_scaleup_storage_stduser.txt"
filename_WorkFlow5_scaleup_server_stduser="../../input/templates_workflows/Workflow5/WorkFlow5_Case2_scaleup_server_stduser.txt"
filename_WorkFlow6_Case1="../../input/templates_workflows/Workflow6/WorkFlow6_Case1_esxi.txt"
filename_WorkFlow6_Case1_esxi_scaleup_server_stdsuer="../../input/templates_workflows/Workflow6/WorkFlow6_Case1_esxi_scaleup_server_stdsuer.txt"
filename_WorkFlow6_Case1_esxi_scaleup_storage_stduser="../../input/templates_workflows/Workflow6/WorkFlow6_Case1_esxi_scaleup_storage_stduser.txt"
filename_WorkFlow6_Case2="../../input/templates_workflows/Workflow6/WorkFlow6_Case2_hyperv.txt"
filename_WorkFlow6_Case2_hyperv_scaleup_server_adminuser="../../input/templates_workflows/Workflow6/WorkFlow6_Case2_hyperv_scaleup_server_adminuser.txt"
filename_WorkFlow7_Case1="../../input/templates_workflows/Workflow7/WorkFlow7_Case1.txt"
filename_WorkFlow8_Case1="../../input/templates_workflows/Workflow8/WorkFlow8_Case1.txt"
filename_WorkFlow9_Case1="../../input/templates_workflows/Workflow9/WorkFlow8_Case1.txt"

'''
        #########################################################
        Payloads for Workflows Cases  END  --> 
        #########################################################
'''

filename_TestCase_M1000="../../input/templates_JIRAStory/TestCase_M1000.txt"
filename_TestCase_FX2210="../../input/templates_JIRAStory/TestCase_FX2210.txt"
filename_TestCase_FX410="../../input/templates_JIRAStory/TestCase_FX410.txt"
filename_TestCase_MXL="../../input/templates_JIRAStory/TestCase_MXL.txt"
filename_TestCase_FlexMXL="../../input/templates_JIRAStory/TestCase_FlexMXL.txt"
filename_TestCase_FXPT="../../input/templates_JIRAStory/TestCase_FXPT.txt"

filename_Testcase1_AsmStory4099_Eql="../../input/templates_AsmStory4099/Testcase1_AsmStory4099_Eql.txt"
filename_Testcase1_AsmStory4099_Eql_scaleup="../../input/templates_AsmStory4099/Testcase1_AsmStory4099_Eql_scaleup_server.txt"

filename_Testcase2_AsmStory4099_Eql="../../input/templates_AsmStory4099/Testcase2_AsmStory4099_Eql.txt"
filename_Testcase2_AsmStory4099_Eql_scaleup="../../input/templates_AsmStory4099/Testcase2_AsmStory4099_Eql_scaleup_server.txt"

filename_Testcase3_AsmStory4099_Comp="../../input/templates_AsmStory4099/Testcase3_AsmStory4099_Comp.txt"
filename_Testcase3_AsmStory4099_Comp_scaleup="../../input/templates_AsmStory4099/Testcase3_AsmStory4099_Comp_scaleup_server.txt"

filename_Testcase4_AsmStory4099_Comp="../../input/templates_AsmStory4099/Testcase4_AsmStory4099_Comp.txt"
filename_Testcase4_AsmStory4099_Comp_scaleup="../../input/templates_AsmStory4099/Testcase4_AsmStory4099_Comp_scaleup_server.txt"



'''
        #########################################################
        Payloads for fcoe mxl   Cases  
        #########################################################
'''

filename_TestCase_104080="../../input/templates_fcoe_flex_fc_mxl/TestCase_104080.txt"
filename_TestCase_before_104081_scaleup_compellentstorage="../../input/templates_fcoe_flex_fc_mxl/TestCase_before_104081_scaleup_compellentstorage.txt"
filename_TestCase_104081_after_scaleup_compellentstorage="../../input/templates_fcoe_flex_fc_mxl/TestCase_104081_after_scaleup_compellentstorage.txt"
filename_TestCase_before_104083_scaleup_bladeserver="../../input/templates_fcoe_flex_fc_mxl/TestCase_before_104083_scaleup_bladeserver.txt"
filename_TestCase_104083_after_scaleup_bladeserver="../../input/templates_fcoe_flex_fc_mxl/TestCase_104083_after_scaleup_bladeserver.txt"
filename_TestCase_before_104087_scaleup_bladeserver="../../input/templates_fcoe_flex_fc_mxl/TestCase_before_104087_scaleup_bladeserver.txt"
filename_TestCase_104087_afte_scaleup_bladeserver="../../input/templates_fcoe_flex_fc_mxl/TestCase_104087_afte_scaleup_bladeserver.txt"
filename_TestCase_104088="../../input/templates_fcoe_flex_fc_mxl/TestCase_104088.txt" 
filename_TestCase_before_104089_scaleup_compellentstorage="../../input/templates_fcoe_flex_fc_mxl/TestCase_before_104089_scaleup_compellentstorage.txt"
filename_TestCase_104089_after_scaleup_compellentstorage="../../input/templates_fcoe_flex_fc_mxl/TestCase_104089_after_scaleup_compellentstorage.txt"
filename_TestCase_before_104092_scaleup_bladeserver="../../input/templates_fcoe_flex_fc_mxl/TestCase_before_104092_scaleup_bladeserver.txt"
filename_TestCase_104092_afte_scaleup_bladeserver="../../input/templates_fcoe_flex_fc_mxl/TestCase_104092_afte_scaleup_bladeserver.txt"


'''
        #########################################################
        Payloads for bfs   Cases  
        #########################################################
'''

filename_TestCase_122411 = "../../input/templates_bfs/TestCase_122411.txt"
filename_TestCase_122412 = "../../input/templates_bfs/TestCase_122412.txt"


'''
        #########################################################
        Payloads for fc   Cases  
        #########################################################
'''



filename_TestCase_53291="../../input/templates_FC/Testcase_53291.txt"
filename_TestCase_53292="../../input/templates_FC/Testcase_53292.txt"
filename_Testcase_53293_before_scaleup="../../input/templates_FC/Testcase_53293_before_scaleup_server_storage_cluster_vm.txt"
filename_TestCase_53293_scaleup_server="../../input/templates_FC/TestCase_53293_after_scaleup_server.txt"
filename_Testcase_53294_before_scaleup="../../input/templates_FC/Testcase_53294_before_scaleup_server_storage_cluster_vm.txt"
filename_TestCase_53294_scaleup_storage="../../input/templates_FC/TestCase_53294_after_scaleup_storage.txt"
filename_Testcase_53295_before_scaleup="../../input/templates_FC/Testcase_53295_before_scaleup_server_storage_cluster_vm.txt"
filename_TestCase_53295_scaleup_vm="../../input/templates_FC/TestCase_53295_after_scaleup_VM.txt"
filename_TestCase_53298="../../input/templates_FC/TestCase_53298_before_scaleup_server.txt"
filename_TestCase_53298_scaleup_server="../../input/templates_FC/TestCase_53298_after_scaleup_server.txt"
filename_TestCase_53299="../../input/templates_FC/TestCase_53299_before_scaleup_VM.txt"
filename_TestCase_53299_scaleup_vm="../../input/templates_FC/Testcase_53299_after_scaleup_VM.txt"
filename_TestCase_53300="../../input/templates_FC/TestCase_53300_before_scaleup_app.txt"
filename_TestCase_53300_scaleup_app="../../input/templates_FC/TestCase_53300_after_scaleup_app.txt"
filename_Testcase_53301_before_scaleup="../../input/templates_FC/TestCase_53301_before_scaleup_app.txt"
filename_TestCase_53301_scaleup_app="../../input/templates_FC/TestCase_53301_after_scaleup_app.txt"
filename_TestCase_53338="../../input/templates_FC/TestCase_53338.txt"



'''
        #########################################################
        Payloads of ISCSI COMP  CONVERGED
        #########################################################
'''


filename_TestCase_126299="../../input/templates_ISCSICOMP/Converged/TestCase_126299.txt"
filename_TestCase_126300="../../input/templates_ISCSICOMP/Converged/TestCase_126300.txt"
filename_TestCase_126303_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126303_before_scaleup.txt"
filename_TestCase_126303_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126303_after_scaleup_Server.txt"
filename_TestCase_126304_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126304_before_scaleup.txt"
filename_TestCase_126304_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126304_after_scaleup_Volume.txt"
filename_TestCase_126305_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126305_before_scaleup.txt"
filename_TestCase_126305_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126305_after_scaleup_VM.txt"
filename_TestCase_126308_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126308_before_scaleup.txt"
filename_TestCase_126308_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126308_after_scaleup_Server.txt"
filename_TestCase_126309_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126309_before_scaleup.txt"
filename_TestCase_126309_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126309_after_scaleup_CloneVM.txt"
filename_TestCase_126310_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126310_before_scaleup.txt"
filename_TestCase_126310_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126310_after_scaleup_App.txt"
filename_TestCase_126311_before_scaleup="../../input/templates_ISCSICOMP/Converged/TestCase_126311_before_scaleup.txt"
filename_TestCase_126311_afte_scaleup_Server="../../input/templates_ISCSICOMP/Converged/TestCase_126311_after_scaleup_App.txt"




'''
        #########################################################
        Payloads of ISCSI COMP Diverged
        #########################################################
'''

filename_TestCase_126301="../../input/templates_ISCSICOMP/Diverged/TestCase_126301.txt"
filename_TestCase_126302="../../input/templates_ISCSICOMP/Diverged/TestCase_126302.txt"
filename_TestCase_126312_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126303_before_scaleup.txt"
filename_TestCase_126312_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126312_after_scaleup_CloneVM.txt"
filename_TestCase_126313_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126313_before_scaleup.txt"
filename_TestCase_126313_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126313_after_scaleup_Server.txt"
filename_TestCase_126314_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126314_before_scaleup.txt"
filename_TestCase_126314_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126314_after_scaleup_App.txt"
filename_TestCase_126315_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126315_before_scaleup.txt"
filename_TestCase_126315_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126315_after_scaleup_Server.txt"
filename_TestCase_126316_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126316_before_scaleup.txt"
filename_TestCase_126316_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126316_after_scaleup_VM.txt"
filename_TestCase_126317_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126317_before_scaleup.txt"
filename_TestCase_126317_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126317_after_scaleup_Volume.txt"
filename_TestCase_126318_before_scaleup="../../input/templates_ISCSICOMP/Diverged/TestCase_126318_before_scaleup.txt"
filename_TestCase_126318_afte_scaleup_Server="../../input/templates_ISCSICOMP/Diverged/TestCase_126318_after_scaleup_App.txt"






#########################################
# Payload for Add Network
#########################################

filename_TestCase_111118="../../input/Add_network_payload/TestCase_111118.txt"
filename_TestCase_111119="../../input/Add_network_payload/TestCase_111119.txt"
filename_TestCase_111121="../../input/Add_network_payload/TestCase_111121.txt"
filename_TestCase_111124="../../input/Add_network_payload/TestCase_111124.txt"
filename_vm_networks = "../../input/Add_network_payload/vm_networks_payload.txt"
vm_wl_network = "../../input/Add_network_payload/vm_wl_nw.txt"
vm_wl_static = "../../input/Add_network_payload/vm_wl_static.txt"
filename_TestCase_53263="../../input/Add_network_payload/TestCase_53263.txt"
filename_TestCase_53266="../../input/Add_network_payload/TestCase_53266.txt"
filename_TestCase_53289 = "../../input/Add_network_payload/TestCase_53289.txt"
filename_TestCase_53290 = "../../input/Add_network_payload/TestCase_53290.txt"
filename_TestCase_79577 = "../../input/Add_network_payload/TestCase_79577.txt"
filename_TestCase_53302 = "../../input/Add_network_payload/TestCase_53302.txt"
filename_TestCase_53265 = "../../input/Add_network_payload/TestCase_53265.txt"
filename_TestCase_53264 = "../../input/Add_network_payload/TestCase_53264.txt"
filename_TestCase_53315 = "../../input/Add_network_payload/TestCase_53315.txt"
filename_TestCase_53316 = "../../input/Add_network_payload/TestCase_53316.txt"
filename_TestCase_53303 = "../../input/Add_network_payload/TestCase_53303.txt"






editMDPayload = "../../input/payload/Edit_ManageDevice.xml"     

serverDiscPayload = "../../input/payload/Discovery_Server.xml"
manageDevicePayload = "../../input/payload/ManageDevice.xml"
switchDiscPayload = "../../input/payload/Discovery_Switch.txt"
storageDiscPayload = "../../input/payload/Discovery_Storage.txt"
chassisDiscPayload = "../../input/payload/Discover_Chassis.txt"
serverPoolPayload = "../../input/payload/Create_ServerPool.xml"
deploy_filename= "../../input/payload/Deployment.xml"
deploy_filename_standardUser= "../../input/payload/DeploymentStandardUser.xml"
VMDiscPayload = "../../input/payload/Discovery_SCVMM.txt"
VCenterDiscPayload = "../../input/payload/Discovery_VCenter.txt"
configResourcePayload= "../../input/payload/Configure_Resource.txt"
EnterpriseManagerDiscPayload= "../../input/payload/Discovery_EMC.xml"



repoTestPayload = "../../input/payload/repoTestPayload.txt"
repoTestPayloadput = "../../input/payload/repoTestPayloadput.txt"
firmwareRepoPayload = "../../input/payload/FirmwareRepo_payload.txt"
firmwareLocalDrive = "../../input/payload/FirmRepoLocDrive_payload.txt"

addOnModulePayload = "../../input/payload/AddOnModuleTest.txt"


osRepositoryPathR2 = "172.31.53.1:/var/nfs/newwin2012r2.iso"
osRepositoryPathNonR2 = "172.21.195.1:/var/nfs/newwin2012nonr2.iso"
osRepositoryPathRedhat = "172.21.195.1:/var/nfs/redhat6.iso"
osRepositoryPath_Redhat7 = "172.31.53.1:/var/nfs/CentOS7.iso"

nfsSourceLocationConTest = r'\\10.255.7.219\SELab\LAB\Catalog\ASMSeptember2016_Full\ASMCatalog.xml'
nfsSourceLocation = r'\\172.17.8.20\nfs\catalog-QA\Catalog.xml'
cfsSourceLocation = r'\\172.17.8.20\cifs\catalog-QA\Catalog.xml'
disklocation = r'/tmp/14156987523/catalog-QA/'   #Create the directory 14156987523 under the tmp directory of appliance machine and upload catalog-QA package



filename_TestCase_SwitchUpdatePost = "../../input/payload/SwitchUpdatePostPayload.xml"
filename_TestCase_SwitchUpdatePut = "../../input/payload/SwitchUpdatePutPayload.xml"


userAgent ="REST API Client"
#userAgent ="python-requests/2.3.0 CPython/2.7.8 Windows/2008ServerR2"
publishedTemplateID=""
publishedTemp_filename="../../input/templates_esxi/published_template.xml"
serviceUriInfo = {}
publicServiceUriInfo = {}
configInfo = {}
defReqWaitTime = 5
defaultWaitTime = 60
credentialTag = {"SERVER":"serverCredential","STORAGE":"storageCredential","SWITCH":"iomCredential",
                 "CHASSIS":"chassisCredential","VCENTER":"vCenterCredential", "SCVMM":"SCVMMCredential", "EnterpriseManager":"emCredential"}
headers = {"Accept":"application/json","Content-Type":"application/xml; charset=UTF-8",
                        "Accept-Encoding":"gzip, deflate"}
apiKey = ""
apiSecret = ""
userName = "admin"
domain = "ASMLOCAL"
password = "admin"
standardUser = "autostandard"
standardPwd = "autopassword"
serverPoolName="HCLASMServerPool"
serverPoolNameExsi="HCLASMServerPool"

serverIPListExsi = ["172.31.60.238"] # ["IP_Address_server1", "IP_Address_server2", "IP_Address_server3", "IP_Address_server4"]
serverPoolNameHyperV="HCLASMServerPool"
serverPoolNameMxl="HCLASMServerPool"
serverPoolNameFlexIoa="HCLASMServerPool"
serverPoolNameNoBrocade="HCLASMServerPool"
serverPoolNameWithBrocade="HCLASMServerPool"
serverPoolNameFX2="HCLASMServerPool"
serverPoolNameRaid="HCLASMServerPool"
serverPoolNameMinimal="HCLASMServerPool"
serverPoolNameMultiservice="HCLASMServerPool"
PoolId = ""
resource_SERVERS = []
resource_STORAGE = []
resource_VCENTER = []
resource_CHASSIS = []
resource_HYPERV = []
resource_SWITCH = []
deviceIPMap = {}
headers = {"Accept":"application/json", "Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate", "Connection":"keep-alive"}
refIdVCenter=""
refIdEQLogic=""
refIdSCVMM=""
discovery = "../../input/Discovery.csv"
noOfLoops=21
teardowncsv = "../../input/TearDown.csv"
depNametoTearDown = "VMtest"

filename_TestCase_testScaleup="../../input/templates_esxi/Testcase_before_scaleup_storage.txt"
filename_TestCase_Component="../../input/templates_esxi/Add_storage.txt" 
resourceInfo = {"SERVER":[],"STORAGE":[],"VCENTER":[],"CHASSIS":[],"HYPERV":[],"SWITCH":[],"SCVMM":[],"COMPELLENT":[], "ElementManager":[]}
deploymentRefId=''
ipMap = {}
mdChassisParameters = {"ip_address":"managementIP", "device_model":"model", "device_serviceTag":"serviceTag"}

firmwareUpdatePayload = "../../input/payload/firmware_update_payload.txt"


exportxml = "../../input/export.xml"
yamlfile =   "../../input/bladeserver-3l0slv1.yaml"

wsmancommand = 'wsman invoke http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/root/dcim/DCIM_LCService?SystemCreationClassName="DCIM_ComputerSystem",CreationClassName="DCIM_LCService",SystemName="DCIM:ComputerSystem",Name="DCIM:LCService" -h 172.31.32.150 -V -v -c dummy.cert -P 443 -u root -p calvin -a ExportSystemConfiguration -k IPAddress=172.31.59.1 -k "ShareName=/var/nfs/" -k "ShareType=0" -k "FileName=export.xml" -k "ExportUse=1"'

filename_TestCase_HyperV = "../../input/payload/TestCase_HyperV.txt"
filename_TestCase_VMware_Esxi = "../../input/payload/TestCase_ESXI.txt"
uriType= "Public" # Public, Internal

resourceType= 'CHASSIS' # SERVER, STORAGE, VCENTER, CHASSIS, SCVMM, COMPELLENT, SWITCH, ElementManager
IP_Address_Resource='172.31.60.78'

testCaseFlowName='exsi'

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

compellent_hyperV_blade =''     # COMPELLENT IP Address
compellent_hyperV_rack =''      # COMPELLENT IP Address
compellent_FCoE_nobrocade =''   # COMPELLENT IP Address
compellent_FCoE_withbrocade ='' # COMPELLENT IP Address
compellent_FCoE_IOA =''         # COMPELLENT IP Address
compellent_FCoE_MXL =''         # COMPELLENT IP Address

Configure_chassis_payload = "../../input/payload/Configure_chassis/Configure_Chassis.xml"
Configure_iom_payload = "../../input/payload/Configure_chassis/Configure_switch.xml"
Configure_server_payload = "../../input/payload/Configure_chassis/Configure_server.xml"
Chassis_Addtnl_Params = "../../input/payload/Configure_chassis/chassis_Addtnl_params.xml"
Server_Addtnl_Params = "../../input/payload/Configure_chassis/server_Addtnl_params.xml"
Switch_Addtnl_Params = "../../input/payload/Configure_chassis/switch_Addtnl_params.xml"

Configure_chassis_Uplink = "../../input/payload/Configure_chassis/Configure_Chassis_Uplink.xml"
Configure_iom_Uplink = "../../input/payload/Configure_chassis/Configure_Switch_Uplink.xml"
Uplink_Addtnl_Params = "../../input/payload/Configure_chassis/ Uplink_Addtnl_Params_Switch.xml"



enableTearDownService = True  # True or False
deploymentStatus ='error'
switchModel_TestCase_112126 = 'S4810' #S4810, S5000, S6000
switchModel_TestCase_112127 = 'S4810'
switchModel_TestCase_112128 = 'S4810'
switchModel_TestCase_112129 = 'S4810'


CommonTemplatePayload =  "../../input/CommonTemplate.xml"
EquallogicComponentPayload= "../../input/EquallogicComponent.xml"
ServerComponentPayload= "../../input/ServerComponent.xml"
RelatedComponentPayload = "../../input/relatedComponents.xml"
VMWareClusterComponentPayload= "../../input/VMWareCluster.xml"
HyperVClusterComponentPayload= "../../input/HyperVCluster.xml"





EqualLogic_Components_Id = '572D3D5E-307C-4FB2-B877-4F60A94294B'
EqualLogic_componentID= 'component'
EqualLogicComponent_Name = 'EqualLogic'

Compellent_Components_Id = '572D3D5E-307C-4FB2-B877-4F60A94294B'
Compellent_componentID= 'component'
CompellentComponent_Name = 'Compellent'

Server_Components_Id = '08bec8f4-0be3-41c0-9220-b6809a1504d'
Server_componentID= 'component'
ServerComponent_Name = 'Server'

VMWareCluster_Components_Id = 'FF72FBAE-78C7-4BC3-B5D4-6B48E0EEEC0'
VMWareCluster_componentID= 'component'
VMWareClusterComponent_Name = 'VMWare Cluster'

HyperVCluster_Components_Id = 'F711EFB9-4453-487B-915C-0597FF05393'
HyperVCluster_componentID= 'component'
HyperVClusterComponent_Name = 'Hyper-V Cluster'

#######################################################################################

NoOfStorage=1 # value should be 0 or higher
NoOfServer=1  # value should be 0 or higher
NoOfCluster=0 # value should be 0 or higher
NoOfVM=0     # value should be 0 or higher
NoOfApplication=0 # value should be 0 or higher
jsonPayload= "../../input/POC.json"
jsonPayload1= "../../input/demo.json"


template_name= "DemoTemplateDheeraj"
template_description="Auto Generated Template with 1 storage 1 server"
TestCase_networkType="Blade_Esxi_2PORT" # Possible Values : Rack_Esxi_2PORT, Rack_Esxi_4PORT, Blade_Esxi_2PORT, Blade_Esxi_4PORT
typeOfStorage="EqualLogic" # EqualLogic, Compellent
typeOfCluster= "VMWareCluster"          #HyperVCluster, VMWareCluster
typeOfVM=""                #CloneVM(hpyerV), VM(VMWare)
typeOfApplication=""        #Citrix, LinuxPostInstall, WindowPostInstall, SQLServer


userPayload="../../input/payload/Create_User.xml"
directoryServicePayload="../../input/payload/DirectoryService.xml"

#########################################################
#<-- Input Parameters for AD User payload  START
#########################################################
ad_User = "hcluserraj"
ad_user_update="hcltest1"
domainName_user = "ASMLOCAL"
pwd_standard = "autopassword"
ad_firstname = "HCLUSER"
ad_pwd = ""





#########################################################
#<-- Input Parameters for License payload  START
#########################################################
eval_license_file_path = "../../config/DDSM_30_License_Eval.xml"
License_payload = "../../input/license_payloads/License_payload.xml"
perp_license_file_path = "../../config/DDSM_500_License_SW.xml"
perp_license_file_path_2 = "../../config/DDSM_30_License_SW.xml"

deviceTypetoDelete = "BladeServer" 
#valide device Types : RackServer,BladeServer,Server,FXServer,TOR,AggregatorIOM,MXLIOM,genericswitch,dellswitch,
#                        compellent,equallogic,netapp
countToDelete = 2 #give an integer value

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

serverIPDisc = "172.31.60.87"