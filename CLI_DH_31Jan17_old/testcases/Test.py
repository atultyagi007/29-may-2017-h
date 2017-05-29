
import xml.etree.ElementTree as ET
import os

chassisInfo = {
"url": {
"title": "Configuration settings for a chassis.",
"href": "http://172.31.41.1:9080/AsmManager/Chassis/ff8080815a7f0db2015a89595e63094f",
"rel": "self",
"type": "chassisRefType"
},
"refId": "ff8080815a7f0db2015a89595e63094f",
"refType": "chassisRefType",
"displayName": "chassis",
"deviceType": "chassis",
"deviceId": "ff8080815a7f0db2015a89595e63094f",
"managementIP": "172.31.60.78",
"managementIPStatic": True,
"credentialRefId": "ff8080815a464f29015a464fd0b50007",
"serviceTag": "H79DWW1",
"assetTag": "00000",
"health": "GREEN",
"model": "PowerEdge M1000e",
"name": "HCLUniqueChassis",
"datacenter": "HCLUniqueChassisLocation",
"aisle": "HCLSlotLocation",
"rack": "row",
"rackslot": "1",
"dnsName": "HClChassisDNS",
"powerCapPercent": 30,
"defaultPowerCapUpperBoundWatts": 16685,
"defaultPowerCapUpperBoundBTU": 56931,
"defaultPowerCapLowerBoundWatts": 2715,
"defaultPowerCapLowerBoundBTU": 9263,
"midPlaneVersion": "1.1",
"lastFirmwareUpdateTime": "2016-11-14T12:04:00.000+0000",
"lastUpdateTime": "2017-03-29T05:01:49.033+0000",
"iomCount": 2,
"serverCount": 10,
"servers": [
  {
"id": "ff8080815a7f0db2015a89595e630961",
"managementIP": "172.31.60.185",
"managementIPStatic": False,
"serviceTag": "H31MD42",
"health": "YELLOW",
"model": "PowerEdge M630",
"slot": "1",
"slotName": "hclr1server1",
"slotType": "UNKNOWN",
"supported": False
},
  {
"id": "ff8080815a7f0db2015a89595e63095c",
"managementIP": "172.31.60.101",
"managementIPStatic": False,
"serviceTag": "4GHV7Y1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "2",
"slotName": "hclfriserver1",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e63095d",
"managementIP": "172.31.60.87",
"managementIPStatic": False,
"serviceTag": "1GHV7Y1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "3",
"slotName": "hclfps16server1",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e630962",
"managementIP": "172.31.60.93",
"managementIPStatic": False,
"serviceTag": "689DWW1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "4",
"slotName": "server1",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e630960",
"managementIP": "172.31.60.86",
"managementIPStatic": False,
"serviceTag": "489DWW1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "5",
"slotName": "server1",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e63095f",
"managementIP": "172.31.60.96",
"managementIPStatic": False,
"serviceTag": "789DWW1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "6",
"slotName": "hcltup1server1",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e630964",
"managementIP": "172.31.60.97",
"managementIPStatic": False,
"serviceTag": "589DWW1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "7",
"slotName": "server4",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e63095e",
"managementIP": "172.31.60.98",
"managementIPStatic": False,
"serviceTag": "189DWW1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "8",
"slotName": "server3",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e63095b",
"managementIP": "172.31.60.100",
"managementIPStatic": False,
"serviceTag": "D4FJKT1",
"health": "YELLOW",
"model": "PowerEdge M620",
"slot": "11",
"slotName": "server5",
"slotType": "HALF",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e630963",
"managementIP": "172.31.60.138",
"managementIPStatic": False,
"serviceTag": "GQJPD42",
"health": "YELLOW",
"model": "PowerEdge M630",
"slot": "16",
"slotName": "hclpk1server2",
"slotType": "UNKNOWN",
"supported": False
}
],
"ioms": [
  {
"id": "ff8080815a7f0db2015a89595e630952",
"managementIP": "172.31.60.71",
"managementIPStatic": False,
"serviceTag": "SST0003",
"health": "GREEN",
"model": "PowerEdge M I/O Aggregator",
"slot": 1,
"location": "A1",
"supported": True
},
  {
"id": "ff8080815a7f0db2015a89595e630953",
"managementIP": "172.31.60.72",
"managementIPStatic": False,
"serviceTag": "Perf003",
"health": "GREEN",
"model": "PowerEdge M I/O Aggregator",
"slot": 2,
"location": "A2",
"supported": True
}
],
"controllers": [
  {
"id": "ff8080815af56796015b1870fa8b6fcc",
"controllerName": "CMC-2",
"controllerPrimary": False,
"controllerFWVersion": "5.12",
"controllerPresent": True
},
  {
"id": "ff8080815af56796015b1870fa8a6fcb",
"controllerName": "CMC-1",
"controllerPrimary": True,
"controllerFWVersion": "5.12",
"controllerPresent": True
}
],
"powerSupplies": [
  {
"id": "ff8080815af56796015b1870fa8b6fd3",
"slot": "1",
"powerStatus": "ON",
"capacity": "2700 W",
"present": True
},
  {
"id": "ff8080815af56796015b1870fa8b6fd1",
"slot": "2",
"powerStatus": "ON",
"capacity": "2700 W",
"present": True
},
  {
"id": "ff8080815af56796015b1870fa8b6fce",
"slot": "3",
"powerStatus": "ON",
"capacity": "2700 W",
"present": True
}
],
"kvms": [
  {
"id": "ff8080815af56796015b1870fa8b6fcd",
"name": "Avocent iKVM Switch",
"firmwareVersion": "01.00.01.01",
"manufacturer": "DELL",
"present": True
}
],
}
print "Hello"
print chassisInfo['ioms'][0]['managementIP']





# di1= {
# "url": {
# "title": "Configuration settings for a chassis.",
# "href": "http://172.31.41.1:9080/AsmManager/Chassis/ff8080815a7f0db2015a89595e63094f",
# "rel": "self",
# "type": "chassisRefType"
# },
# "refId": "ff8080815a7f0db2015a89595e63094f",
# "refType": "chassisRefType",
# "displayName": "chassis",
# "deviceType": "chassis",
# "deviceId": "ff8080815a7f0db2015a89595e63094f",
# "managementIP": "172.31.60.78",
# "managementIPStatic": "True",
# "credentialRefId": "ff8080815a464f29015a464fd0b50007",
# "serviceTag": "H79DWW1",
# "assetTag": "00000",
# "health": "GREEN",
# "model": "PowerEdge M1000e",
# "name": "HCLUniqueChassis",
# "datacenter": "HCLUniqueChassisLocation",
# "aisle": "HCLSlotLocation",
# "rack": "row"
# }
# temp={}
# for key, value in di1.items():
#     if key in ('deviceType','serviceTag'):
#         temp[key]=value
# print temp
    
# path=os.path.abspath("../../Managed.xml")
# 
# 
# 
# fTest = open("../../TestManaged.xml","r+")
# 
# strRead = fTest.read()
# print strRead
# serviceTag="H79DWW1"
# newServiceTag="H79DWW2"
# st1="<serviceTag>"+serviceTag+"</serviceTag>"
# st2="<serviceTag>"+newServiceTag+"</serviceTag>"
# if st1 in strRead:
#     strRead=strRead.replace(st1,st2)
#     print "After updated"
#     print strRead
# else:
#     print "Not aval"
# 
# 
# 
# 
# 
# manageDevices = ET.fromstring(fTest.read())
# print "before updation paload"
# print manageDevices
# #                 root.findall('country')
# for config in manageDevices.findall("config"):
#     #if config.iselement("config"):
#         print "config element present"
#         manageDevices.remove(config)
#         print "deleted"
#     #else:
#         print "config element not present"
# 
# 
# file = open(path,"r+")
# 
# strFILE=file.read()
# sttt=strFILE.split('\n')
# print sttt
# stTest=""
# fTest = open("../../TestManaged.xml","w+")
# for st in sttt:
#     
#     if "<config>" in st:
#         continue
#     stTest+=(st+"\n")
#     
#     fTest.write(st+"\n")
#     
# fTest.close()    
# print"FINAL PAY"
# print stTest    
# 
# # xmldoc=ET.parse(file)
# manageDevice = ET.fromstring(file)
# print "before updation paload"
# print manageDevice
# if manageDevice.iselement("config"):
#     manageDevice.remove("config")
# else:
#     print "config element not present"
# print "Payload"
# print manageDevice