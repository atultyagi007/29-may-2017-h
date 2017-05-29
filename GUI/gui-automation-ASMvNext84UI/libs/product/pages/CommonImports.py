"""
Author: P Suman/Saikumar Kalyankrishnan
Created/Modified: Jul 15th 2014/Feb 15th 2017
Description: List of Common Packages/Library Imports
"""

import os
import json
import time
import platform
import socket
import ConfigParser
from copy import deepcopy
from datetime import datetime
from traceback import format_exc
from libs.product import utility
from libs.product import globalVars
from libs.product.pages import UIException
from libs.product.pages.Controller import Controller
from libs.product.pages.Navigation import Navigation
from libs.product.globalVars import browserObject
from libs.thirdparty.selenium import webdriver
from libs.thirdparty.selenium.common.exceptions import NoSuchElementException
from libs.thirdparty.selenium.common.exceptions import NoSuchFrameException
from libs.thirdparty.selenium.common.exceptions import StaleElementReferenceException
from libs.thirdparty.selenium.common.exceptions import TimeoutException
from libs.thirdparty.selenium.webdriver.common.keys import Keys
from libs.thirdparty.selenium.webdriver.common.by import By
from libs.thirdparty.selenium.webdriver.support.ui import WebDriverWait #Available since 2.4.0
from libs.thirdparty.selenium.webdriver.support import expected_conditions as EC
from libs.thirdparty.selenium.webdriver.firefox.firefox_binary import FirefoxBinary
from libs.thirdparty.selenium.webdriver.support.select import Select
from libs.thirdparty.selenium.webdriver.chrome.options import Options
from libs.thirdparty.selenium.webdriver.common.action_chains import ActionChains
from libs.core.SSHConnection import SSHConnection