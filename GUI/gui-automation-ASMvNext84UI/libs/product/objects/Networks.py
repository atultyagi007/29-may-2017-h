"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 22nd 2017
Description:  Object Repository for Networks Page
"""

class Networks():  
    def NetworksObjects (self, key):
        NetworksObjects = {'title': "//*[@id='page_networks']//h1",
                           'defineNW': "lnkCreate",
                           'editNW': "lnkEdit",
                           'deleteNW': "delete_network_link",
                           'refreshNW': "refresh_networks_link",
                           'exportAll': "exportAllLink",
                           'nwTable': "networks_table",
                           'networkName': "network_name",
                           'networkDescription': "network_description",
                           'networkType': "network_type",
                           'vlanID': "network_vlanid",
                           'configStaticIP': "ipAddressType",
                           'subnetMask': "subnet",
                           'gateway':  "gateway1",
                           'gatewayHW':  "gateway",
                           'primaryDNS': "primaryDNS",
                           'secondaryDNS': "secondaryDNS",
                           'DNSSuffix': "dnsSuffix",
                           'addIPRange': "btn_add_iprange",
                           'startIPRange': "//table[@id='ipRangesTable']//input[contains(@ng-model, 'startingIpAddress')]",
                           'endIPRange': "//table[@id='ipRangesTable']//input[contains(@ng-model, 'endingIpAddress')]",
                           'saveNW': "submit_network_form",
                           'cancelNW': "cancel_network_form",
                           'exportNWDetails': "exportAllNetworkDetailsLink",
                           'IPRangeView': "ddlStaticIPAddressDetailsView",
                           'nwStaticIPTable': "network_details_static_ip_table",
                           'refreshTable': "refresh_networks_link"
        }
        return NetworksObjects.get(key)