'''
Created on Jan 29, 2015

@author: waseem.irshad
'''

OSRepimageType = "redhat7"
OSRepimageTypetest = "redhat6"
OSRepname = "red7"
OSReponametest = "osrtest7" 
OSReponametest1 = "tstraj7"
OSRepsourcePath = "ftp://172.24.3.50/Projects/HCL/ISO/RHEL-7.2.iso"
OSRepsourcePathput = "ftp://172.24.3.50/Projects/HCL/ISO/CentOS-7.0.iso"
OSRepsourcePathtest = "ftp://172.24.3.50/Projects/HCL/ISO/RHEL66.iso"

OSRepimageType_1 = "redhat7"
OSRepname_1 = "FTPRepositoryOne"
OSRepsourcePath_1 = "ftp://172.24.3.50/Projects/HCL/ISO/RHEL-7.2.iso"

OSRepimageType_2 = "redhat7"
OSRepname_2 = "FTP_Repository_Two"
OSRepsourcePath_2 = "ftp://172.24.3.50/Projects/HCL/ISO/RHEL-7.2.iso"

OSRepimageType_3 = "win2012r2"
OSRepname_3 = "101780_FTPRepository"
OSRepsourcePath_3 = "ftp://172.24.4.116/Mod_Win2012R2_PK_54.iso"


nfsSourceLocation = r'\\10.255.7.219\SELab\LAB\Catalog\ASMSeptember2016_Full\ASMCatalog.xml'
cfsSourceLocation = r'\\172.17.8.20\cifs\catalog-QA\Catalog.xml'
ftpSourceLocation = 'ftp://ftp.dell.com/catalog/ASMCatalog.cab'
disklocation = r'/tmp/14156987523/catalog-QA/'   #Create the directory 14156987523 under the tmp directory of appliance machine and upload catalog-QA package

OSRepUserName = ""
OSRepPasswd = ""


#### Parameters for Switch Bundle Upload
criticality = "recommended" ##other values are urgent etc.
deviceModel = "DELL_S6000" ##DELL_IOM, DELL_MXL, DELL_S5000 etc. 
userBundlePath = r'\\10.255.7.219\SELab\LAB\Firmware\Force10\S6000\9.7.0.0\FTOS-SI-9.7.0.0.bin'  ## Full path of the bin file that is to be uploaded
BundleVersion = "9.7(0.0)"  ## Version should be in this format only (e.g. 9.7(0.0) , 9.6(0.1) like this
BundleName = "S6000Bundle"  ## can be any name
BundleDescription= 'This bundle is added by HCL Automation Script'
FirmwareRepositoryName= 'ASM July 2015 Catalog' # Firmware Repository Name 
