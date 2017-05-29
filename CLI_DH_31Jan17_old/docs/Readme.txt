Folder Structure
------------------

--> config folder contains configuration and API's url related information 
	
	-> config.ini : contains configuration information like Appliance IP, different Devices IP (Storage,Server,Chassis,VCENTER etc)
			 NOTE : Add/modify configuration related info in this file (e.g. new Appliance IP/ new device's IP)
			
	-> Services.xml : contains url information for different services of ASM. (e.g. Discovery url, ManageDevice url etc)
		

--> "createdeploytemplate" folder contains template creation and deployment related files.

	 -> inputReqValueESXI.py : parameter file for ESXi template creation/deployment.
			NOTE : we provide runtime parameters for ESXi template in this file.
				E.g. ESXIiamge,Adminpassword, AdminConfirmPassword,target_boot_device_value etc which can be different for different templates
				
	 -> templateInputReqValue.py : parameter file for Hyper-V template creation/deployment.
			NOTE : we provide runtime parameters for Hyper-V template in this file.
				E.g. : OSImage_value,product_key,esxiimagetype,Adminpassword etc which can be different for different templates
			
--> "defineNetwork" : 
		contains the file which contains basic functions being used in Network Discovery
		
--> "discoverresources" : 
		contains the file which contains basic functions being used in Resource Discovery
		
--> "initialsetup" : 
		contains the file which contains basic functions being used in Initial Setup.
		NOTE : contains initialSetupReqValue.py file which contains key and certificate related information for ASM. 
		
--> "input" : This folder contains All the input information needed at different stages of Automation. This folder contains the below floders/files :

		1. "payload" Folder : This folder contains the Discovery/Setup/ManageDevice related payload 
		2. "templates" Folder : This folder contains the payloads for Hyper-V testcases.
		3. "template_esxi" Folder : This folder contains the payloads for ESXi testcases.
		4. "Credential.csv" File : contains the credential related information for devices (Storage,Chassis,Server etc)
			NOTE : change this file for any modification in credential related inputs.
		5. "Network.csv" File : Contains network related information for discovery of networks.
			NOTE : change this file for any changes in network inputs.
			
--> " testcases " : This folder contains the script files for all the testcases (testcase.py). This folder further contains below three folders : 
		
		-> common : contains the common steps scripts.
					->InitialSetup, DefineNetwork, DiscoverResources.
		
		-> esxi : contains the scripts for esxi testcases
		
		-> hyperV : contains the scripts for hyperV testcases
		
		
--> "util" : This folder contains file which contains the utility functions used across the automation process.
		
		NOTE : This folder contains globalVars.py file which is an input parameter file containing the path of different files.
			E.g. This contains the location information of config.ini,Services.xml, all the payload files locations etc.
				The location of any new file added or existing file updated needs to be updated here.

	