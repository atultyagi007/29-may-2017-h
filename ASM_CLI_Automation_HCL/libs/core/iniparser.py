#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
I never liked the way ConfigParser from the stdlib handles defaults.  The
built-in thinks of sections in the inifile as identical records and thus has
a flat 'defaults' dictionary that doesn't take sections into account.

Every .ini file I've ever used had different options in each section.  This
parser expects the defaults to be a dictionary of dictionaries of the form:

{section: {option: value, option:value}, section2...}
"""

import ConfigParser



class IniParser(ConfigParser.RawConfigParser):

    """
    IniFile Parser that has section-specific defaults
    """

    def __init__(self, **kwargs):
        defaults = kwargs.get('defaults')
        kwargs['defaults'] = None
        ConfigParser.RawConfigParser.__init__(self, **kwargs)
        self._apply_defaults(defaults)

    def _apply_defaults(self, defaults):
        """
        Apply the defaults before anything else happens
        """

        if defaults:
            for (section, options) in defaults.iteritems():
                self.add_section(section)
                for (key, value) in options.iteritems():
                    self.set(section, key, str(value))

    def get(self, section, option):
        try:
            return ConfigParser.RawConfigParser.get(self, section, option)
        except ConfigParser.NoOptionError:
            return ConfigParser.RawConfigParser.get(self, section, option.lower())

    def to_bool(self, section, option):
        value = self.get(section, option)
        if value.lower() not in self._boolean_states:
            raise ValueError, 'Not a boolean: %s' % value
        value = self._boolean_states[value.lower()]
        self.set(section, option, value)

    def to_tuple(self, section, option):
        value = self.get(section, option)
        if not value:
            value = '()'

        try:
            value = eval(value)
        except:
            value = [v.strip() for v in value.split(',') if v]

        if not value:
            value = ()

        if isinstance(value, list):
            value = tuple(value)
        elif not isinstance(value, tuple):
            value = (value,)

        self.set(section, option, value)

    def transform(self, section, option, func):
        value = self.get(section, option)
        self.set(section, option, func(value))

    def get_dict(self, section=None):
        result = {}
        for option in self.options(section):
            result[option] = self.get(section, option)
        return result

