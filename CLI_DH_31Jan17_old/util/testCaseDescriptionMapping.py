'''
Created on Jun 18, 2015

@author: dheeraj.singh
'''
################################# Hyper-V######################################################

TestCase_78803 ='Deploy a HyperV non-R2 instance on an R720'
TestCase_78804 ='Deploy a HyperV non-R2 instance on an R720 with 2 storage volumes and a cluster'
TestCase_81798='Discover storage as managed, after discovering should be able to unmanage it from resource page'
TestCase_81799='Discover servers as managed, after discovering should be able to unmanage it from resource page'
TestCase_81801='Create server pool with unmanaged servers'
TestCase_77211='Deploy 1 M420 Blade servers with HyperV  non-R2 with 2 storage volumes and a cluster'
TestCase_77215='Deploy 1 M820 Blade servers with HyperV  non-R2 with 2 storage volumes and a cluster'
TestCase_77216='Deploy 1 M620 Blade servers with HyperV  non-R2 with 2 storage volumes and a cluster'
TestCase_78805='Deploy a HyperV non-R2 instance on 2 R720s with 2 storage volumes and a cluster'
TestCase_78806='Deploy a HyperV non-R2 instance on an R620 with 2 storage volumes and a cluster'
TestCase_78807='Deploy a HyperV non-R2 instance on 2 R620s with 2 storage volumes and a cluster'
TestCase_78808='Deploy a HyperV R2 instance on an R720 with 2 storage volumes and a cluster'
TestCase_78809='Deploy a HyperV R2 instance on 2 R720s with 2 storage volumes and a cluster'
TestCase_78810='Deploy a HyperV R2 instance on an R620 with 2 storage volumes and a cluster'
TestCase_78811='Deploy a HyperV R2 instance on 2 R620s with 2 storage volumes and a cluster'
TestCase_78805_Blade='Deploy a HyperV non-R2 instance on 2 R720s with 2 storage volumes and a cluster.'
TestCase_78913='Deploy a HyperV non-R2 instance on 2 R720s with 2 Compellent storage volumes and a cluster'
TestCase_78914='Deploy a HyperV R2 instance on 2 R720s with 2 Compellent storage volumes and a cluster'
TestCase_78915='Deploy a HyperV non-R2 instance on 2 R620s with 2 Compellent storage volumes and a cluster'
TestCase_78916='Deploy a HyperV R2 instance on 2 R620s with 2 Compellent storage volumes and a cluster'
#TestCase_82126='Add 1 servers to the deployment'
#TestCase_82129='Add 2 servers to the deployment'
TestCase_89862='Clone a Windows VM from a Hyper-V cluster to the same datacenter'
TestCase_89864='Clone a linux VM on a Hyper-V cluster to the same datacenter'
TestCase_89916='Add a server to a successful hyper-v deployment with equallogic'
TestCase_89917='Add a server to a successful hyper-v deployment with compellent'
TestCase_77217='Deploy a hyperv instance on 2 M620 blade servers'
TestCase_77218='Deploy a hyperv instance on 2 M420 blade servers'
TestCase_77219='Deploy a hyperv instance on 2 M820 blade servers'
TestCase_77451='Deploy a hyperv instance to a blade server to a new host group and cluster'
TestCase_77452='Deploy a hyperv instance to a blade server to an existing host group and cluster'


################################# ESXI ######################################################
TestCase_serviceTemp='Successfully template updated  and published with uri /ServiceTemplate/ get,post put '
TestCase_81190='deploy a template with 2 storage, 2 hosts and HA enabled in cluster'
TestCase_81191='deploy a template with 1 storage, 1 hosts and HA enabled in cluster'
TestCase_81192='deploy a template with 2 storage, 2 hosts and DRS enabled in cluster'
TestCase_81193='deploy a template with 2 storage, 2 hosts and HA, DRS enabled in cluster'
TestCase_81194='deploy a template with 2 storage, MEM enabled, 2 hosts and HA enabled in cluster'
TestCase_81195='deploy a template with 2 storage, MEM enabled, 2 hosts and DRS enabled in cluster'
TestCase_81196='deploy a template with 2 storage, MEM enabled, 2 hosts HA, DRS enabled in cluster'
TestCase_81197='deploy a template with 2 storage, 2 hosts, HA, DRS enabled in cluster and a red hat VM'
TestCase_81795='Discover storage as unmanaged, after discovering should be able to manage it from resource page'
TestCase_81796='Discover servers as unmanaged, after discovering should be able to manage it from resource page'
TestCase_81797='Discover vcenter as unmanaged, after discovering should be able to manage it from resource page'
TestCase_81798='Discover storage as managed, after discovering should be able to unmanage it from resource page'
TestCase_81799='Discover storage as managed, after discovering should be able to unmanage it from resource page'
TestCase_81800='Discover storage as managed, after discovering should be able to unmanage it from resource page'
TestCase_81801='Create server pool with unmanaged servers'
TestCase_88170='Test storage deployment with IQN/IP'
TestCase_22222='Create users/import AD users(new add on 8/19/2014)'
TestCase_89866='Clone a VM from an ESX cluster to a different datacenter where the host doesnt include the workload network'
TestCase_81198='deploy a template with 2 storage, MEM enabled, 2 hosts, HA, DRS enabled in cluster and a red hat VM'
TestCase_81199='deploy a template with 2 storage, 2 hosts and HA and DRS enabled in cluster and 2 VM'
TestCase_82125='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup a storage volume'
TestCase_82126='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup a server to the deployment'
TestCase_82127='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup a VM'
TestCase_82128='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup 3 storage volume'
TestCase_82129='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup 2 servers'
TestCase_82131='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup 4 VMs'
TestCase_88167='Deploy a template with 1 storage 1 server, 1 cluster and 1 vm and try to scaleup storage, server and vm one after other'
TestCase_88168='Deploy a template with serverpool of 2 servers and try to scaleup a server'
TestCase_88169='Test servers deployment with Mem enabled and scaleup severs'
TestCase_89863='Clone a Windows VM from an ESX cluster to a different datacenter'
TestCase_89865='Clone linux VM on ESX cluster on the same datacenter'
TestCase_102213='deploy a template with 1 storage volume and then try to add 2 volumes to a successful deployment'
TestCase_102215='deploy a template with storage, server and then try to add cluster to a successful deployment'
TestCase_102216='deploy a template with storage, server and cluster, then try to add VM to a successful deployment'
TestCase_102217='deploy a template with storage, server, cluster and VM, then try to add application to a successful deployment'
TestCase_102214='deploy a template with 1 storage volume and then try to add servers to a successful storage deployment'

################################# RAID  TestCases ######################################################

TestCase_112188='Baremetal deployment with Raid0 on H710 controller with hotspare1'
TestCase_112189='Baremetal deployment with Raid0 on H730 controller with hotspare2'
TestCase_112190='Baremetal deployment with Raid1 on H310 controller'
TestCase_112191='Baremetal deployment with Raid1 on H710 controller with hotspare2'
TestCase_112192='Baremetal deployment with Raid1 on H330 controller with hotspare2'
TestCase_112193='Baremetal deployment with Raid1 on H730 controller with hotspare1'
TestCase_112194='Baremetal deployment with Raid5 on H310 controller'
TestCase_112195='Baremetal deployment with Raid5 on H710 controller with hotspare1'
TestCase_112196='Baremetal deployment with Raid5 on H330 controller with hotspare2'
TestCase_112197='Baremetal deployment with Raid5 on H730 controller with hotspare1'
TestCase_112198='Baremetal deployment with Raid6 on H710 controller with hotspare1'
TestCase_112199='Baremetal deployment with Raid6 on H730 controller with hotspare2'
TestCase_112200='Baremetal deployment with Raid10 on H310 controller'
TestCase_112201='Baremetal deployment with Raid10 on H710 controller with hotspare2'
TestCase_112202='Baremetal deployment with Raid10 on H330 controller'
TestCase_112203='Baremetal deployment with Raid10 on H730 controller with hotspare1'
TestCase_112204='Baremetal deployment with Raid50 on H310 controller'
TestCase_112205='Baremetal deployment with Raid50 on H710 controller with hotspare1'
TestCase_112206='Baremetal deployment with Raid50 on H330 controller with hotspare3'
TestCase_112207='Baremetal deployment with Raid50 on H730 controller with hotspare2'
TestCase_112208='Baremetal deployment with Raid60 on H710 controller'
TestCase_112209='Baremetal deployment with Raid60 on H730 controller with hotspare2'
TestCase_112210='Baremetal deployment with Raid0,1,50 on H710 controller with hotspare1'
TestCase_115719='Baremetal deployment with Raid1,50 on H730 controller with hotspare1'
TestCase_115720='Baremetal deployment with Raid6,60 on H710 controller with hotspare1'
TestCase_115721='Baremetal deployment with Raid5,10 on H730 controller with hotspare1'

################################# FCoE_s5000 without Brocade ######################################################

TestCase_102190='Deploy 2 rack servers using ESX 5.5 with 2 compellent volumes to a new cluster'
TestCase_102192='Deploy 2 blade servers using ESX 5.5 with 1 existing compellent volume to a new cluster'
TestCase_102193='Deploy 1 rack server using ESX 5.5 with 2 compellent volumes to an existing cluster'
TestCase_102195='Deploy 1 blade server using ESX 5.5 with 1 compellent volume to a new cluster'
TestCase_102202='Deploy 2 rack servers using ESX 5.1 with 2 existing compellent volumes to a new cluster'
TestCase_102204='Deploy 2 blade servers using ESX 5.1 with 1 compellent volume to a new cluster'
TestCase_102205='Deploy 1 rack server using ESX 5.1 with 2 compellent volumes to an existing cluster'
TestCase_102207='Deploy 1 blade server using ESX 5.1 with 1 compellent volume to an existing cluster'

################################# CiscoNexus_withBrocade_FCoE ######################################################

TestCase_102118='Deploy 2 rack servers using ESX 5.5 with 2 compellent volumes to a new cluster'
TestCase_102120='Deploy 2 blade servers using ESX 5.5 with 1 existing compellent volume to a new cluster'
TestCase_102121='Deploy 1 rack server using ESX 5.5 with 2 compellent volumes to an existing cluster'
TestCase_102123='Deploy 1 blade server using ESX 5.5 with 1 compellent volume to an existing cluster'
TestCase_102130='Deploy 2 rack servers using ESX 5.1 with 2 existing compellent volumes to a new cluster'
TestCase_102132='Deploy 2 blade servers using ESX 5.1 with 1 compellent volume to a new cluster'
TestCase_102133='Deploy 1 rack server using ESX 5.1 with 2 compellent volumes to an existing cluster'
TestCase_102135='Deploy 1 blade server using ESX 5.1 with 1 compellent volume to an existing cluster'


################################# Cisco_NoBrocade_FCoE ######################################################

TestCase_102142='Deploy 2 rack servers using ESX 5.5 with 2 compellent volumes to a new cluster'
TestCase_102144='Deploy 2 blade servers using ESX 5.5 with 1 existing compellent volume to a new cluster'
TestCase_102145='Deploy 1 rack server using ESX 5.5 with 2 compellent volumes to an existing cluster'
TestCase_102147='Deploy 1 blade server using ESX 5.5 with 1 compellent volume to an existing cluster'
TestCase_102154='Deploy 2 rack servers using ESX 5.1 with 2 existing compellent volumes to a new cluster'
TestCase_102156='Deploy 2 blade servers using ESX 5.1 with 1 compellent volume to a new cluster'
TestCase_102157='Deploy 1 rack server using ESX 5.1 with 2 compellent volumes to an existing cluster'
TestCase_102159='Deploy 1 blade server using ESX 5.1 with 1 compellent volume to an existing cluster'

################################# ChassisConfigTestCases ######################################################

TestCase_104145='Discover a chassis and configure the chassis, server and switches with existing credentials and IPs'
TestCase_104146='Discover a chassis and configure the chassis to use static IP for chassis with new credentials'
TestCase_104147='Discover a chassis and configure the chassis to use static IP for servers with existing credentials'
TestCase_104148='Discover a chassis and configure the chassis to use static IP for switches with new credentials'
TestCase_104149='Discover a chassis and configure the chassis to use DHCP for servers with new credentials'
TestCase_104150='Discover a chassis and configure the chassis to use DHCP for switches with existing credentials'

################################# FlexIO_Fc_IOA ######################################################

TestCase_104061='Deploy 1 blade server using ESX 5.5 with 1 new compellent volume to an existing cluster'
TestCase_104062='Deploy 1 blade server using ESX 5.5 with 1 new compellent volume to an existing cluster and scale up compellent volume'
TestCase_104064='Deploy 1 blade server using ESX 5.5 with 2 new compellent volumes to a new cluster and scale up a blade server.'
TestCase_104068='Deploy 2 blade servers using ESX 5.5 with 2 compellent volume to an existing cluster and scale up a blade server'
TestCase_104069='Deploy 1 blade server using ESX 5.1 with 1 new compellent volume to an existing cluster'
TestCase_104070='Deploy 1 blade server using ESX 5.1 with 1 new compellent volume to an existing cluster and scale up compellent volume'
TestCase_104073='Deploy 2 blade servers using ESX 5.1 with 2 existing compellent volume to an existing cluster and scale up a blade server'


################################# FX2  TestCases ######################################################

TestCase_110628='Using FX2 Servers as rack servers for ESXi deployment'
TestCase_110629='Using FX2 Servers as rack servers for Hyper-v deployment'
TestCase_110631='1 server ESX end to end deployment'
TestCase_110632='2 server ESX end to end deployment with EQL'
TestCase_110633='Multi-service ESX end to end deployment with EQL'
TestCase_110634='Min server ESX deployment'
TestCase_110635='1 server deployment with storage scaleup'
TestCase_110636='1 server delayed deployment with server scaleup'
TestCase_110637='1 end to end deployment with server, storage, cluster and VM'

################################# MXL  TestCases ######################################################

TestCase_104080='Deploy 1 blade server using ESX 5.5 with 1 new compellent volume to an existing cluster'
TestCase_104081='Deploy 1 blade server using ESX 5.5 with 1 new compellent volume to an existing cluster and scale up compellent volume'
TestCase_104083='Deploy 1 blade server using ESX 5.5 with 2 new compellent volumes to a new cluster and scale up a blade server.'
TestCase_104087='Deploy 2 blade servers using ESX 5.5 with 2 compellent volume to an existing cluster and scale up a blade server'
TestCase_104088='Deploy 1 blade server using ESX 5.1 with 1 new compellent volume to an existing cluster'
TestCase_104089='Deploy 1 blade server using ESX 5.1 with 1 new compellent volume to an existing cluster and scale up compellent volume'
TestCase_104092 ='Deploy 2 blade servers using ESX 5.1 with 2 existing compellent volume to an existing cluster and scale up a blade server'

################################# BareMetal  TestCases ######################################################

TestCase_107262='Baremetal deployment on blade server with Centos7'
TestCase_107263=' Baremetal deployment on blade server with RHEL7'
TestCase_107264='Baremetal deployment on Rack server with Centos7'
TestCase_107265='Baremetal deployment on Rack server with RHEL7'
TestCase_107266='VM deployment with RHEL7'
TestCase_107267='VM deployment with Centos7'

################################# Minimal  TestCases ######################################################

TestCase_107285='C series deployment with Centos7'
TestCase_107286='C series deployment with RHEL7'
TestCase_107287='C series deployment with Windows'
TestCase_107442='ESXi 5.5 deployment on blade server using minimal server component.'
TestCase_107443='ESXi 5.1 deployment on blade server using minimal server component.'
TestCase_107444='ESXi 5.5 deployment on rack server using minimal server component.'
TestCase_107445='ESXi 5.1 deployment on rack server using minimal server component.'
TestCase_107446='ESXi 5.5 deployment on C series server using minimal server component.'
TestCase_107447='ESXi 5.1 deployment on C series server using minimal server component.'
TestCase_107448='Scale up server component on deployment on blade server using minimal server component.'
TestCase_107449='Scale up server component on deployment on rack server using minimal server component.'
TestCase_107450='Scale up server component on deployment on C series server using minimal server component'
TestCase_109194='1 min server OS deployment with CentOS 7'


################################# Multiple Service  TestCases ######################################################

TestCase_107315='Multiple service deployments on blades/rack using num tag.'
TestCase_107316='Multiple service deployments on blades/rack using service_tag.'
TestCase_107317='Multiple service deployments on C series server using num tag.'
TestCase_107318='Multiple service deployments on C series server using service_tag.'
TestCase_107319='Multiple service deployments without auto generation of host name gives error message.'
TestCase_107457='Multiple service deployments on C series server using auto generation of host names.'
TestCase_107458='Multiple service deployments on C series server using auto generation of host names.'
TestCase_107459='Multiple service deployments having components other than servers.'
TestCase_107460='Multiple service instances greater than the numebr of available resources.'
TestCase_107461='Multiple service deployments using vendor for auto generation of host names.'
TestCase_107462='Multiple service deployments using model for auto generation of host names.'


################################# TearDown  TestCases  ######################################################

TestCase_102696='delete 2 volumes, server and a cluster from a success deployment'
TestCase_102697='delete a volumes and servers from successful deployment'
TestCase_102698='Delete servers from a successful deployment'
TestCase_102699='delete servers, cluster and VM'
TestCase_102700='delete volume, server and vms from a successful deployment'
TestCase_102701='delete volume, server, cluster and vms from a successful deployment'


################################# Firmware Repository TestCases  ######################################################

TestCase_102623='Try to upload a compatible repository from NFS share'
TestCase_102624='Try to upload a compatible repository from FTP share'
TestCase_102629='try to upload a large catalog from FTP share'
TestCase_102630='try to upload a large catalog from NFS share'
TestCase_102637='Try to upload a compatible repository from FTP share'
TestCase_102638='Try to upload a compatible repository from NFS share'
TestCase_102639='try to upload a large catalog from FTP share'
TestCase_102640='try to upload a large catalog from NFS share'


################################# Firmware Repository TestCases  ######################################################

TestCase_122411='Create a template for BFS and Deploy 1 server with Equalogic storage'
TestCase_122412='Create template for BFS and deploy 1 server with Compelent storage'

################################# Add Network TestCases  ######################################################

TestCase_111124='Add network to hosts on a successful hyperV deployment'
TestCase_111125='Add network to vm on a successful hyperV deployment'
TestCase_53263 = 'iS - CO - Con - On a successful VMWare deployment, scale up a network'
TestCase_53289 = 'iS - CO - Div - On a successful VMWare deployment, scale up a network'
TestCase_53302 = 'FC  On a successful VMWare deployment, scale up a network'
TestCase_53265 = 'iS - CO - Con - On a successful HyperV deployment, scale up a network'
TestCase_53264 = 'iS - CO - Div - On a successful HyperV deployment, scale up a network'
TestCase_53315 = 'iS - EQL - On a successful HyperV deployment, scale up a network'
TestCase_53266 = 'iS - EQL - On a successful VMWare deployment, scale up a network'
TestCase_79577 = 'FCoE- Flex - On a successful VMWare deployment, scale up a network'
TestCase_53316 = 'Netapp  On a successful VMware deployment, scale up a network'


