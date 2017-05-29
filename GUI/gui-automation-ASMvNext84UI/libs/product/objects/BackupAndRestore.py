"""
Author: Saikumar Kalyankrishnan
Created: Mar 8th 2017
Description: Object Repository for Backup & Restore Page
"""

class BackupAndRestore():
    def BackupAndRestoreObjects (self, key):
        BackupAndRestoreObjects = {'title': "//*[@id='page_backupAndRestore']//h1",
                                   'sections': "//*[@id='page_backupAndRestore']//article",
                                   'sectionRows': "//div[@class='well']//div[@class='row']",
                                   'backupButton': "backupNowLink",
                                   'restoreButton': "restoreNowLink",
                                   'testConnection': "btnTestBackupNowConnection",
                                   'testConnectionCheck': "//div[normalize-space(text())='The test was successful']",
                                   'editSettingDetails': "editSettingsAndDetailsLink",
                                   'editScheduledBackup': "schedulBackupLink",
                                   'backupCheck': "useBackupSettings",
                                   'backupDirPath': "directoryPath",
                                   'backupDirUsername': "directoryUserName",
                                   'backupDirPassword': "directoryPassword",
                                   'backupEncryptionPassword': "encryptionPassword",
                                   'backupEncryptionCPW': "verifyEncryptPassword",
                                   'backupTestConnection': "btnTestBackupNowConnection",
                                   'backupSubmit': "submitBackupNow",
                                   'backupCancel': "cancelBackupNow",
                                   'lastBackupStatus': "lastBackupStatus",
                                   'restorePath': "directoryPath",
                                   'restoreDirUsername': "directoryUserName'",
                                   'restoreDirPassword': "backupLocationPassword",
                                   'restoreEncryptionPassword': "encryptionPassword",
                                   'restoreTestConnection': "btnTestBackupNowConnection",
                                   'restoreSubmit': "submitBackupNow",
                                   'restoreCancel': "cancelBackupNow",
                                   'backupEnable': "backupEnabled",
                                   'backupDisable': "backupDisabled",
                                   'backupSunday': "Sunday",
                                   'backupMonday': "Monday",
                                   'backupTuesday': "Tuesday",
                                   'backupWednesday': "Wednesday",
                                   'backupThursday': "Thursday",
                                   'backupFriday': "Friday",
                                   'backupSaturday': "Saturday",
                                   'backupTime': "txtTimeOfBackup",
                                   'backupScheduleSubmit': "backupScheduleSubmit",
                                   'backupScheduleCancel': "backupModalClose"
                       }
        return BackupAndRestoreObjects.get(key)