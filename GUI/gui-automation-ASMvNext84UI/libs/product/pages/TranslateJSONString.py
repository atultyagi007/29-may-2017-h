import urllib2
import time
from libs.product.globalVars import browserObject
from libs.thirdparty.selenium.webdriver import DesiredCapabilities
from libs.thirdparty.selenium import webdriver
from libs.thirdparty.selenium.webdriver.common.keys import Keys
from CommonImports import *
from libs.product.pages import Login
import re
#!/usr/bin/env python
# -*- encoding: utf8 -*-

class TranslateJSONString():
    
    def __init__(self, browserObject):
        self.browserObject = browserObject
        self.clusterValues=['ClusterName','DCName']  
        self.storageValues=['New_Volume_Name']
        self.vmValues=['HostNameTemplate']
        self.scaleUpList=['Cluster','Storage','VM']
    # connects to the google translate website, which can translate the JSON string
    def connectWebsite(self,valuetobetranslated,ExpectedForeignLangauge):
        langaugecode=self.getLanguageCode(ExpectedForeignLangauge)
        main_window = self.browserObject.window_handles[0]
        xpathPart1=".//*[@id='result_box']/span["
        xpathPart2="]"
        urlpart1="https://translate.google.com/#en/"
        if(langaugecode=='zh'):
            urlpart2="zh-CN/"
        else:
            urlpart2=langaugecode+"/"
        urlpart3=valuetobetranslated
        self.url=urlpart1+urlpart2+urlpart3
        self.browserObject.execute_script("window.open('%s')"%self.url)
        sub_window=self.browserObject.window_handles[1]
        self.browserObject.switch_to_window(sub_window)
        TranslatedValues =self.browserObject.find_elements_by_xpath(".//*[@id='result_box']/span")
        TranslatedValue =''
        for eachSpan in xrange(1, len(TranslatedValues)+1):
            if(len(TranslatedValue)>0):
                TranslatedValue=(TranslatedValue+((self.browserObject.find_element_by_xpath(xpathPart1+str(eachSpan)+xpathPart2)).text)).replace(" ","")
            else:
                TranslatedValue=(self.browserObject.find_element_by_xpath(xpathPart1+str(eachSpan)+xpathPart2).text).strip()
            
        self.browserObject.close()
        self.browserObject.switch_to_window(main_window)
        return TranslatedValue
    
    def catchLocalizedValue(self,localValue,Language):
        if(Language.lower()=='chinese'):
            if re.findall(ur'[\u4e00-\u9fff]+',localValue):
                return True
        elif(Language.lower()=='japanese'):
            if( re.findall(ur'[\u4e00-\u9fbf]+',localValue) or re.findall(ur'[\u3040-\u309f]+',localValue) or re.findall(ur'[\u30A0-\u30ff]+',localValue)):
                return True
        elif(Language.lower()=='korean'):
            if( re.findall(ur'[\uac00-\ud7af]+',localValue)):
                return True
        else:
            return False
              
    # Description: Gets the Language code for a foreign language 
    def getLanguageCode(self,language):
        languageSupportKeys = {'chinese':'zh-CN','japanese':'ja','korean':'ko'}
        for eachLanguage in languageSupportKeys.keys():
            if(str(language.lower()) == eachLanguage):
                return languageSupportKeys[eachLanguage] 
            
    # main Function, which is being called from the test case. JSON MAP has the localized values 
    def translateLocalizedValue(self,jsonDict,language):
        utility.execLog("Translating JSON values to the '%s'language" %language)
        
        jsonMap ={'Template':['Name','Description'],'Scaleup':['Storage','Cluster','VM'],'Cluster':self.clusterValues,'Storage':self.storageValues,'VM':self.vmValues}
        for eachKey in jsonMap.keys():
            utility.execLog("Translating '%s' under '%s' from JSON"%(eachKey,jsonMap[eachKey]))
            newJson =self.translateImplementation(jsonDict, eachKey, jsonMap[eachKey],language)
        return newJson

    # identifies the outer keys and inner keys from the JSON and translate the inner key value and update the inner key-value pair and the outer key-value pair of the JSON
    def translateImplementation(self,jsonDict,jsonOuterKEY,jsonInnerKEY,ForeignLanguage):
        innerJSONKEYS=[]
        finalUpdate =True
        allKeys=jsonDict.keys()
        for eachJsonKey in allKeys:
            if(eachJsonKey == jsonOuterKEY):
                jsonValue=jsonDict[eachJsonKey]
                jsonSpecificKeys=jsonValue.keys()     
                for eachSpecificJsonKey in jsonSpecificKeys:
                    innerJSONKEYS=jsonInnerKEY
                    for eachInnerKey in innerJSONKEYS:               
                        if(eachSpecificJsonKey==eachInnerKey):
                            utility.execLog("Translating value for '%s'"%(jsonValue[eachSpecificJsonKey]))
                            if(type(jsonValue[eachSpecificJsonKey]) is dict):
                                if(eachSpecificJsonKey in self.scaleUpList):   
                                    finalDict=jsonValue[eachSpecificJsonKey]
                                    finalKeys=finalDict.keys()
                                    for eachfinalKey in finalKeys:
                                        if(eachfinalKey in self.clusterValues or eachfinalKey in self.storageValues or eachfinalKey in self.vmValues):
                                            translateValue=self.connectWebsite(finalDict[eachfinalKey],ForeignLanguage)
                                            if('NUM' in translateValue):
                                                translateValue=translateValue.replace('NUM','num')
                                            finalDict[eachfinalKey]=translateValue
                                jsonValue[eachSpecificJsonKey]=finalDict 
                            else:
                                translateValue=self.connectWebsite(jsonValue[eachSpecificJsonKey],ForeignLanguage)
                                if('NUM' in translateValue):
                                    translateValue=translateValue.replace('NUM','num')
                                jsonValue[eachSpecificJsonKey]=translateValue           
                jsonDict[eachJsonKey]=jsonValue        
        return jsonDict  
    
    def translateJSONPage(self):
        utility.execLog("Translate Function is being trigged for JSON value converstion")
        time.sleep(15)
        return self.browserObject, True, "Successfully navigated to the JSON Translation Page"  
        