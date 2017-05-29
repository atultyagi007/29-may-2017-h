"""
Author: Pavan G/Saikumar Kalyankrishnan
Created/Modified: Mar 9th 2016/Mar 8th 2017
Description: Functions/Operations related to Portview Page
"""

from libs.product.BaseClass import STATUS
from CommonImports import *
from libs.product.globalVars import browserObject
from datetime import datetime
from libs.thirdparty.selenium.webdriver.common.by import By
from libs.product.pages import Login
from libs.thirdparty.selenium.webdriver.common.action_chains import ActionChains
from libs.thirdparty.selenium.webdriver.common.keys import Keys
import socket
from libs.product.objects.Common import Common
from libs.product.objects.Portview import Portview
from libs.core.SSHConnection import SSHConnection
import ConfigParser

class Portview(SSHConnection, Common, Portview):
    """
    Description:
        Class which includes Functions/Operations related to Portview Page
    """
    def __init__(self, browserObject):
        self.browserObject = browserObject
        self.nonesxiServer=""
    
    # Validate the presence of XPATH    
    def check_exists_by_xpath(self,xpath):
        try:
            self.browserObject.find_element_by_xpath(xpath)
        except NoSuchElementException:
            return False
        return True
    
    # Get the index of the specific value in the list
    def getIndexOfList(self,stringValue,listName):
        index =0
        for eachLine in listName:
            if(stringValue in str(eachLine)):
                break
            else:
                index =index+1
        return index
    
    def compareList(self,list1, list2):
        for eachval in list1:
            if eachval in list2:
                return True
        return False
        
    def connectSSH(self, hostType, HOST, COMMAND):
        utility.execLog("SSH Connection to %s : %s" %(hostType, HOST))
        if (hostType == 'ToR'):
            USERNAME="admin"
            PASSWORD="dell1234"
        elif(hostType == 'Brocade'):
            USERNAME="admin"
            PASSWORD="password"
        elif (hostType=='Switch'):
            USERNAME="root"
            PASSWORD="calvin"
        else:
            USERNAME=""
            PASSWORD=""
        
        if (USERNAME!="" and PASSWORD!="" and HOST!="" and COMMAND!=""):
            ssh = SSHConnection(HOST, USERNAME, PASSWORD, 22, 120)
            connection_result, connection_error = ssh.Connect()
            if (connection_error != ""):
                self.failure(connection_error)
                return False, connection_error
            else:
                utility.execLog("Running %s on %s..." %(COMMAND, HOST))
                command_result, command_error = ssh.Execute(COMMAND)
                if (command_error != ""):
                    self.failure(command_error)
                    return False, command_error
                else:
                    return True, command_result
                
        close_result, close_error = ssh.Close()
        
    # Validation of ToR,IO and BROCADE SWITCH  
    def validateConnectionFromSwitch(self,IOValidation=False,ToR=False,Brocade=False): 
        if(IOValidation):
            LinkStatusFromTabularResult=self.getNetworkDetails(IODetails=True)
            IOSwitchKeys=LinkStatusFromTabularResult.keys()
            commandPart1="show vlan id"
            commandPart2="| grep Te"
            command2Part1="show lldp neighbors | grep"
            vlanSelectionPart1 ="//div[@id='portViewDiagram']/ul/li/div/ul/li[contains(.,"
            vlanSelectionPart2=")]"
            for eachSKey in IOSwitchKeys:
                IOSwitchValues=LinkStatusFromTabularResult.get(eachSKey)
                SwitchIPAddress=str(IOSwitchValues.get("IP Address"))
                print SwitchIPAddress
                ServerPort=str(IOSwitchValues.get("Server Port"))
                ServerPortFromPortViewPage=IOSwitchValues.get("Server Port")
                if(self.check_exists_by_xpath(self.PortviewObjects('ioUplinkPorts'))):
                    ioUplinkList =[self.browserObject.find_element_by_xpath(self.PortviewObjects('ioUplinkPorts')).text]
                VLanDetails=self.getNetworkDetails(VLAN=True)
                NPARDetails=self.getNetworkDetails(NPAR=True)
                for eachNPAR in NPARDetails:
                    if(eachNPAR ==1):
                        NDetail=NPARDetails.get(eachNPAR)
                        MacAddress=NDetail.get("MAC Address").lower()
                VlanKeys=VLanDetails.keys()
                for eachVlanKey in VlanKeys:
                    eachRowVLan=VLanDetails.get(eachVlanKey)
                    VLANId=eachRowVLan.get("VLAN")
                    self.browserObject.find_element_by_xpath(vlanSelectionPart1+VLANId+vlanSelectionPart2).click()
                    self.browserObject.implicitly_wait(5)
                    command=str(commandPart1+" "+VLANId+" "+commandPart2)
                    command2=str(command2Part1+" "+ServerPortFromPortViewPage)
                    status, result =self.connectSSH("Switch",SwitchIPAddress,command)
                    status1, result1 =self.connectSSH("Switch",SwitchIPAddress,command2)
                    Fport=[]
                    
                    if(status):
                        outputList =result.splitlines()
                        for eachLine in outputList:
                            # Condition, which is valid for NON ESXI PXE VLAN
                            if("T Po128" in eachLine): 
                                if(self.nonesxiServer and "T Po128" in eachLine):
                                    portList=eachLine.split('/')
                                    finalPort=str(portList[1])
                                    if(')' in str(finalPort)):
                                        finalPort=str(finalPort).replace(')','')
                                    if(str(finalPort) in ioUplinkList):
                                        utility.execLog(VLANId)
                                        utility.execLog("Vlan port information matches with the port information of switch")
                                    else:
                                        utility.execLog("Vlan port information from UI and Switch does not match") 
                            # for all the other VLANS of NON ESXI and all the VLANS of ESXI            
                            elif("U Te" in eachLine or "T Te" in eachLine):
                                portList =eachLine.split()
                                portNumber=portList[2].split('/')
                                finalPort=portNumber[1].split(',')
                                for eachfinalPort in finalPort:
                                    if('-' in str(eachfinalPort)):
                                        Fport = str(eachfinalPort).split('-')
                                if(str(ServerPort) in finalPort or str(ServerPort) in Fport ):
                                    utility.execLog(VLANId)
                                    utility.execLog("Vlan port information matches with the port information of switch")
                                else:
                                    utility.execLog("Vlan port information from UI and Switch does not match")  
                    # Verifies if IO down link port is connected to right NIC or no
                    if(status1):
                        PortResult = False
                        outputList =str(result1).splitlines()
                        for eachOutputline in outputList:
                            if("Ue" in eachOutputline or "Te" in eachOutputline and MacAddress in eachOutputline):
                                utility.execLog("IO down port displayed in the portview is linked to the right NIC")
                                PortResult = True
                        if(PortResult == False):
                            utility.execLog("IO down port displayed in the portview is not linked to the right NIC")
                            
        if(ToR):
            LinkStatusFromTabularResult1=self.getNetworkDetails(ToRDetails=True)
            ToRKeys=LinkStatusFromTabularResult1.keys()
            command1="show lldp neighbors | grep"
            portInformation=[]
            for eachTKey in ToRKeys:
                ToRValues=LinkStatusFromTabularResult1.get(eachTKey)
                DowlinkPort=str(ToRValues.get("Downlink Port"))
                ToRIpAddress=str(ToRValues.get("IP Address"))
                portArray =DowlinkPort.split(',')
                for eachPort in portArray:
                    command=str(command1+" "+eachPort)
                    status, result =self.connectSSH("ToR",ToRIpAddress,command)
                    if(status):
                        outputList =str(result).splitlines()
                        for eachOutputLine in outputList:
                            if('Ethernet' in str(eachOutputLine)):
                                splitLines = eachOutputLine.split()
                                indexNumber=self.getIndexOfList("Ethernet",splitLines)
                                indexNumber=indexNumber+1
                                portValue =splitLines[indexNumber].split('/')
                                portInformation.append(portValue[1])
            ioUplinkList =[self.browserObject.find_element_by_xpath(self.PortviewObjects('ioUplinkPorts')).text]
            print ioUplinkList
            self.browserObject.implicitly_wait(5)
            if(self.compareList(str(ioUplinkList),str(portInformation))):
                print portInformation
                utility.execLog("ToR port infomation from PortView page and ToR switch are the same")
            else:
                utility.execLog("ToR port infomation from PortView page and ToR switch are not the same")                                          
        if(Brocade):
            Brocade=False
            ToRMap=self.getNetworkDetails(ToRDetails=True)
            ToRDetails=self.getIndividualRowsFromTables(ToRMap)
            NicMap=self.getNetworkDetails(NIC=True)
            NicDetails=self.getIndividualRowsFromTables(NicMap)
            WWPN=(NicDetails.get("World Wide Port Name (WWPN)")).lower()
            downlinkport=str(ToRDetails.get("Downlink Port"))
            brocadeipaddress=str(ToRDetails.get("IP Address"))
            commandpart1="portshow"
            commandpart2="| grep"
            command=commandpart1+" "+downlinkport+commandpart2+" "+WWPN
            status, result =self.connectSSH("Brocade",brocadeipaddress,command)
            if(status):
                outputList =str(result).splitlines()
                for eachLine in outputList:
                    if(WWPN in eachLine):
                        utility.execLog("ToR port infomation from PortView page and Brocade switch are the same")
                        Brocade=True
                if(Brocade==False):
                    utility.execLog("ToR port infomation from PortView page and Brocade switch are not the same")           
             
    def selectConnection(self):
        select_box = browserObject.find_element_by_xpath(self.PortviewObjects('portViewSelectConnection'))
        options = [x for x in select_box.find_elements_by_tag_name("option")]
        optionValues=[]
        for element in options:
            optionValues.append(element.text)
        return optionValues
    
    def getToRPort(self):
        TorPorts=self.browserObject.find_element_by_xpath(self.PortviewObjects('toRPorts')).get_attribute('innerHTML')
        return TorPorts 
    
    def getValuesFromTable(self,tableHeadXpath,rowsXpath,xpathTable):
        noOfRows=(self.browserObject.find_elements_by_xpath(rowsXpath))
        noOfHeads=(self.browserObject.find_elements_by_xpath(tableHeadXpath))
        xpathStart = xpathTable
        xpathString1 = "/tbody/tr["
        xpathString2 = "]/td["
        xpathString3 = "]//parent::tr//parent::tbody//parent::table/thead/tr/th["
        xpathString4 = "]"
        rowcount=len(noOfRows)
        headcount=len(noOfHeads) 
        FinalDetails={}
        for eachRow in xrange(1,rowcount+1):
            Details = {}
            for eachTableData in xrange(1,headcount+1):
                if(xpathStart !=self.PortviewObjects('tableXpathToR')):
                    Heading =self.browserObject.find_element_by_xpath(xpathStart+xpathString1+str(eachRow)+xpathString2+str(eachTableData)+xpathString3+str(eachTableData)+xpathString4).text
                    Value = self.browserObject.find_element_by_xpath(xpathStart+xpathString1+str(eachRow)+xpathString2+str(eachTableData)+xpathString4).text
                else:
                    Heading =self.browserObject.find_element_by_xpath(xpathStart+xpathString1+str(eachRow+1)+xpathString2+str(eachTableData)+xpathString3+str(eachTableData)+xpathString4).text
                    Value = self.browserObject.find_element_by_xpath(xpathStart+xpathString1+str(eachRow+1)+xpathString2+str(eachTableData)+xpathString4).text 
                Details[Heading]=Value
            headcount=len(noOfHeads)
            FinalDetails[eachRow]={}
            FinalDetails[eachRow]=Details
        return FinalDetails
        
    def getNetworkDetails(self,NPAR=False,NIC=False,IODetails=False,VLAN=False,ToRDetails=False):
        utility.execLog("Getting connection data from the tabular view")
        nicModel =self.browserObject.find_element_by_xpath("//table[@id='nicDetailsTable']/tbody/tr[1]/td[2]").text
        utility.execLog("nicModel"+nicModel)
        if('Broadcom' in nicModel or 'Intel' in nicModel or 'QLogic' in nicModel):
            if(NPAR):
                NPARDetails = self.getValuesFromTable(self.PortviewObjects('tableHeadXpathNPAR'), self.PortviewObjects('rowsXpathNPAR'),self.PortviewObjects('tableXpathNPAR'))
                print NPARDetails
                return NPARDetails
            if(NIC):
                NICDetails = self.getValuesFromTable(self.PortviewObjects('tableHeadXpathNIC'), self.PortviewObjects('rowsXapthNIC'),self.PortviewObjects('tableXpathNIC'))
                return NICDetails
            if(IODetails):
                IODetails = self.getValuesFromTable(self.PortviewObjects('tableHeadXpathIoDetails'),self.PortviewObjects('rowsXpathIO'),self.PortviewObjects('tableXpathIODetails'))
                return IODetails
            if(ToRDetails):
                ToRDetails = self.getValuesFromTable(self.PortviewObjects('tableHeadXpathToRDetails'),self.PortviewObjects('rowsXpathToR'),self.PortviewObjects('tableXpathToR'))
                return ToRDetails
            if(VLAN):
                VLANDetails = self.getValuesFromTable(self.PortviewObjects('tableHeadXpathVlanDetails'), self.PortviewObjects('rowsXpathVlan'),self.PortviewObjects('tableXpathVLAN'))
                return VLANDetails
    
    def getIndividualRowsFromTables(self,Smap):
        FinalResults ={}
        Keys=Smap.keys()
        for eachKey in Keys:
            FinalResults=Smap.get(eachKey)
        return FinalResults
               
    def compareConnectionDetails(self,esxiServerExits):
        error_exists = []
        self.nonesxiServer=esxiServerExits
        utility.execLog("Getting links information from the port view ")
        ToRDownlinkValue =self.PortviewObjects('toRPorts')
        ToRDownlinkKey ="Downlink Port"
        IoModuleDownlinkValue=self.PortviewObjects('ioModuleDownLinkValue')
        IoModuleDownlinkKey="Server Port"
        NiCInformationValue=self.PortviewObjects('nicInformationValue')
        NicInformationKey="Port"
        LinkMap = {ToRDownlinkKey:ToRDownlinkValue,IoModuleDownlinkKey:IoModuleDownlinkValue,NicInformationKey:NiCInformationValue}
        optionValues=self.selectConnection()
        DownLinkPort = False
        try:
            for count in xrange(1, len(optionValues)): 
                if(optionValues[count]!= "Show All Connections"):
                    Select(self.browserObject.find_element_by_xpath(self.PortviewObjects('portViewSelectConnection'))).select_by_visible_text(optionValues[count])
                    time.sleep(10)
                    
                    #Validate Labels & VLANs
                    status, result = self.validateLabels()
                    if status:
                        utility.execLog(result)
                    else:
                        utility.execLog(result)
                        error_exists.append[True]
                    
                    for eachLinkMapKey in LinkMap:
                        if(self.check_exists_by_xpath(LinkMap[eachLinkMapKey])):
                            # verifies if ToR is  present, if yes ToR details are extracted and verified against port view diagram
                            if(eachLinkMapKey=='Downlink Port' and \
                               self.check_exists_by_xpath(self.PortviewObjects('toRHealthCheck'))):
                                    linkfromPortView=self.getToRPort()
                                    NicMap=self.getNetworkDetails(NIC=True)
                                    NicDetails=self.getIndividualRowsFromTables(NicMap)  
                                    self.browserObject.implicitly_wait(5)
                                    LinkStatusFromTabularResult=self.getNetworkDetails(ToRDetails=True)
                                    self.browserObject.implicitly_wait(10)
                                    if(len(NicDetails.get("World Wide Port Name (WWPN)"))>0):
                                        self.validateConnectionFromSwitch(Brocade=True)
                                    else:
                                        self.validateConnectionFromSwitch(ToR=True)
                                    self.browserObject.implicitly_wait(10)
                                    DownLinkPort =True     
                            # Verifies if IOM is  present, if yes IOM details are extracted and verified against port view diagram
                            elif(eachLinkMapKey=='Server Port'):
                                linkfromPortView=self.browserObject.find_element_by_xpath(LinkMap[eachLinkMapKey]).text
                                self.browserObject.implicitly_wait(3)
                                #goes inside only if ToR is present and IO switch is present with uplink and down link
                                if(DownLinkPort and \
                                   self.check_exists_by_xpath(self.PortviewObjects('ioModuleUplinkCheck')) and \
                                   self.check_exists_by_xpath(self.PortviewObjects('ioModuleDownLinkCheck'))):
                                    LinkStatusFromTabularResult=self.getNetworkDetails(IODetails=True)
                                    self.browserObject.implicitly_wait(10)
                                    self.validateConnectionFromSwitch(IOValidation=True)
                                #goes inside only if IO switch is present with only down link
                                elif(eachLinkMapKey=='Server Port' and \
                                    self.check_exists_by_xpath(self.PortviewObjects('ioModuleDownLinkCheck'))):
                                    LinkStatusFromTabularResult=self.getNetworkDetails(IODetails=True)
                                    self.browserObject.implicitly_wait(10)
                                    self.validateConnectionFromSwitch(IOValidation=True)
                            elif(eachLinkMapKey=='Port' and \
                                 self.check_exists_by_xpath(self.PortviewObjects('nicCardLinkCheck'))):
                                linkfromPortView=self.browserObject.find_element_by_xpath(LinkMap[eachLinkMapKey]).text
                                self.browserObject.implicitly_wait(3)
                                LinkStatusFromTabularResult=self.getNetworkDetails(NIC=True)
                            else:
                                utility.execLog("Link is either not displayed or it is not in Green for '%s'" %eachLinkMapKey)
                                if(eachLinkMapKey=='Downlink Port'):
                                    WarningMessage=self.browserObject.find_element_by_xpath(self.PortviewObjects('toRWarningMessage')).text
                                    utility.execLog("ToR Warning Message'%s'"%WarningMessage)
                                    linkfromPortView=self.getToRPort()
                                    NicMap=self.getNetworkDetails(NIC=True)
                                    NicDetails=self.getIndividualRowsFromTables(NicMap)
                                    if(len(NicDetails.get("World Wide Port Name (WWPN)"))> 0):
                                        WWPNValue = (NicDetails.get("World Wide Port Name (WWPN)")).lower()   
                                    self.browserObject.implicitly_wait(5)
                                    LinkStatusFromTabularResult=self.getNetworkDetails(ToRDetails=True)
                                    self.browserObject.implicitly_wait(10)
                                    if(len(NicDetails.get("World Wide Port Name (WWPN)"))>0):
                                        self.validateConnectionFromSwitch(Brocade=True)
                                    else:
                                        self.validateConnectionFromSwitch(ToR=True)
                                    self.browserObject.implicitly_wait(10)
                                    DownLinkPort =True
                                else:
                                    break
                            FinalResults ={}
                            ToRKeys=LinkStatusFromTabularResult.keys()
                            for eachKey in ToRKeys:
                                FinalResults=LinkStatusFromTabularResult.get(eachKey)
                                if(FinalResults.get(eachLinkMapKey)):
                                    if(FinalResults.get(eachLinkMapKey)==linkfromPortView):
                                        utility.execLog("Link from the port view and tabular view matches for '%s'" %eachLinkMapKey)
                                        error_exists.append(False)
                                    else:
                                        utility.execLog("Link from the port view and tabular view does not matches for '%s'" %eachLinkMapKey)
                                        error_exists.append(True)                
            if True in error_exists:
                return False, "Link from the port view and tabular view does not match"
            else:
                return True, "Link from the port view and tabular view matches"               
        except Exception as e:
            return self.browserObject, False, "Unable to read the link information  :: Error -> %s"%str(e)   
                
    def viewPortView(self):
        try:
            self.browserObject.find_element_by_id("portViewLink").click()
            utility.execLog("Navigating to the 'Port View' Page")
            time.sleep(3)
            return self.browserObject, True, "Successfully navigated to the Port View Page."
        except Exception as e:
            return self.browserObject, False, "Unable to switch to Port View Tab :: Error -> %s"%str(e)
    
    def TORexists(self):
        TORexists = False
        if self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.95 152.6)']").is_displayed():
            TORexists = True
            utility.execLog("Top of Rack (ToR) Switches: %s"%TORexists)
        else:
            utility.execLog("Top of Rack (ToR) Switches: %s"%TORexists)
        return TORexists
    
    def IOMexists(self):
        IOMexists = False
        if self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.95 290.6834)']").is_displayed():
            IOMexists = True
            utility.execLog("I/O Modules: %s"%IOMexists)
        else:
            utility.execLog("I/O Modules: %s"%IOMexists)
        return IOMexists
    
#     def validateVLAN(self):
#         utility.execLog("Starting to validate VLANs in the 'Port View' Page.")
#         try:
#             ports = self.browserObject.find_elements_by_xpath("//div[@id='portViewDiagram']/ul[contains(@class,'vlansList')]//div[contains(@class, 'vlansContainer')]")
#             utility.execLog("No. of Ports: %d"%len(ports)) #len(ports) will always be 1
#             portID=str(ports[0].get_attribute('id'))
#             portIP=None
#             
#             #self.browserObject.find_elements_by_xpath("//div[@id='svgComponents']//*[name()='g' and @class='NICelements']/*[name()='g' and @class, 'PartitionElements')]").click()
#             #//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='IOMElements']/*[name()='g' and @class='IOMSlotElements']/*[name()='g']/*[name()='g' and @class='IOMLabelTop']/*[name()='text']/*[name()='tspan']"
#         
#             return True, "Successfully validated VLANs."
#         except Exception as e:
#             return False, "Error in validating VLANs. Error -> %s" %str(e)
    
    def validateLabels(self):
        utility.execLog("Starting to validate Labels in the 'Port View' Page.")
        vLANexists = None
        TORexists = None
        IOMexists = None
        NICexists = None
        try:
            vLANexists = self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.7 30.6)']").text
            utility.execLog("Label Exists: %s"%vLANexists)
            
            if self.TORexists():
                TORexists = self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.95 152.6)']").text
            else:
                TORexists="Top of Rack (ToR) Switches not discovered/displayed."
            utility.execLog("Label Exists: %s"%TORexists)
            
            if self.IOMexists():
                IOMexists = self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.95 290.6834)']").text
            else:
                IOMexists="I/O Modules (IOMs) Switches not discovered/displayed"
            utility.execLog("Label Exists: %s"%IOMexists)
           
            NICexists = self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.95 478.6833)']").text
            utility.execLog("Label Exists: %s"%NICexists)
            
            NPARexists = self.browserObject.find_element_by_xpath("//div[@id='portViewDiagram']/div[@id='svg']//*[name()='svg']/*[name()='g']/*[name()='g' and @class='LeftColText']/*[name()='text' and @transform='matrix(1 0 0 1 44.95 623.6)']").text
            utility.execLog("Label Exists: %s"%NPARexists)
            
            if ((vLANexists == None) or (TORexists == None) or (IOMexists == None) or (NICexists == None)):
                return False, "Error in validating Labels on the Port-View Page"
            else:
                return True, "Successfully validated the Labels on the Port-View Page"
        except Exception as e:
            utility.execLog("Error in validating Labels on the Port-View Page. Error -> %s" %str(e))

    def validatePortView(self, esxiServerExits):
        error_exists = False
        try:
            status, result = self.compareConnectionDetails(esxiServerExits)
            if status:
                utility.execLog(result)
            else:
                utility.execLog(result)
                error_exists = True 
        
            utility.execLog("Errors Found: %s"%error_exists)
        
            if error_exists:
                return self.browserObject, True, "Error in validating the Port View Page."   
            else:
                return self.browserObject, False, "Successfully validated the Port View Page."
        except Exception as e:
            utility.execLog("Error in validating the Port View Page. Error -> %s" %str(e))