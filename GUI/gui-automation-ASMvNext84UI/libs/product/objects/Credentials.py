"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 22nd 2017
Description:  Object Repository for Credentials Page
"""

class Credentials():
    def CredentialsObjects (self, key):
        CredentialsObjects = {'title': "//*[@id='page_credentials']//h1",
                              'createCredentials': "new_credential_link",
                              'editCredentials': "edit_credentials_link",
                              'deleteCredentials': "delete_credential_link",
                              'refreshCredentials': "refresh_credentials_link",
                              'credentialsTable': "credentials_table",
                              'credentialsType': "credential_type",
                              'credentialsName': "credential_name",
                              'credentialsUsername': "credential_username_edit",
                              'credentialsPassword': "credential_password",
                              'credentialsConfirmPW': "credential_confirm_password",
                              'credentialsDomain': "credential_domain_edit",
                              'credentialsSNMP': "credential_snmpconfig",
                              'credentialsSave': "submit_credential_form",
                              'credentialsCancel': "cancel_credential_form",
                              'credentialsRefresh': "refresh_credentials_link",
        }
        return CredentialsObjects.get(key)