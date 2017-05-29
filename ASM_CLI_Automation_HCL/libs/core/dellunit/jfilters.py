#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Additional filters for jinja2
"""

import datetime
import re

def filter_datetime(value):
    """
    translate a timestamp to a datetime object
    """
    value = value.replace('+00:00', 'Z')
    return datetime.datetime(*map(int, re.split('[^\d]', value)[:-1]))

def datetime_format(value, format='%H:%M / %d-%m-%Y'):
    return value.strftime(format)
