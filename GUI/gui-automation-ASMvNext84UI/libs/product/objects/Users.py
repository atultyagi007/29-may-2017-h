"""
Author: Saikumar Kalyankrishnan
Created: Mar 8th 2017
Description: Object Repository for Users Page
"""

class Users():
    def UsersObjects (self, key):
        UsersObjects = {'title': "//*[@id='page_users']//h2",
                        "newUserlink":"new_user_link",
                        "pageEditUser":"modal-content",
                        "currentPassword":"currentPassword",
                        "userName":"userName",
                        "userPassword":"password",
                        "userConfirmPassword":"confirmationPassword",
                        "userFirstName":"firstName",
                        "userLastname":"lastName",
                        "userRole":"role",
                        "userEmail":"email",
                        "userPhone":"phone",
                        "userEnabled":"enableuser",
                        "submitUserForm":"createuserconfirm",
                        "clarityError":"//span[@ng-bind-html='error.message | htmlSafe']",
                        "validateFormError":"//span[@class='text-danger ng-binding ng-scope']",
                        "cancelUserForm":"controllerEditAssetTagModalCancelButton",
                        "editUserLink":"edit_user_link",
                        "editUserPassword":"Password",
                        "modalContent":"modal-content",
                        "editUserSave":"edituserconfirm",
                        "tabUsers":"usersTab",
                        "tabDirectory":"directoryservicesTab",
                        "lnkNewDirectoryService":"new_directory_link",
                        "DirectoryUserRole":"directoryType",
                        "directoryServerName":"directory_server_name",
                        "directoryBindDN":"directoryUserName",
                        "directoryUserPassword":"directoryPassword",
                        "userPasswordConfirm":"confirmDirectoryPassword",
                        "userHost":"user_host",
                        "userPort":"user_port",
                        "protocol":"protocol",
                        "btnWizardNext":"//button[contains(text(),'Next')]",
                        "directoryBasedn":"directoryBaseDN",
                        "directoryFilter":"directory_filter",
                        "userUname":"user_uname",
                        "fname":"fname",
                        "lname":"lname",
                        "email":"email",
                        "btnWizardFinish":"//button[contains(text(),'Finish')]",
                        "submitConfirmForm":"btnConfirm",
                        "deleteUserLink":"delete_user_link",
                        "submitConfirmForm":"btnConfirm",
                        "***confirm_modal_form":"",
                        "enableUser":"enable_user",
                        "disableUser":"disable_user",
                        "editUserLink":"edit_user_link",
                        "selectAllUsers":"selectAllUsers",
                        "importUsers":"importUsers",
                        "usersTable":"users_table",
                        "editDirectoryLink":"edit_directory_link",
                        "ddlADGroup":"ddlADGroup",
                        "directoryddl":"directoryddl",
                        "searchUserGroups":"searchUserGroups",
                        "shiftToRight":"shiftToRight",
                        "saveImportUserForm":"save_import_user_form",
                        "userRole":"user_role"
        
                    
                        
        
        

                       }
        return UsersObjects.get(key)