"""
Author: Saikumar Kalyankrishnan
Created: Feb 23rd 2017
Description: Object Repository for Logs Page
"""

class Logs():
    def LogsObjects (self, key):
        LogsObjects = {'title': "//*[@id='logsSection']//h1",
                       'exportAllLogs': "exportAllLink",
                       'purgeLogs': "purgeLogsLink",
                       'logsTable': "logsSection",
                       'pageBox': "//*[@id='logsSection']//table//tfoot//input"
                       }
        return LogsObjects.get(key)