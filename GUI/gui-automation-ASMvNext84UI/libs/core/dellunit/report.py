#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Functionality for generating an HTML Report
"""

import os
from dellunit.jinja2 import Environment, FileSystemLoader
from dellunit import jtests
from dellunit import jfilters
import logging
import StringIO
import mailer
import operator
import json

BASEDIR = os.path.dirname(os.path.abspath(__file__))
PROD_DIR = os.path.join(os.path.dirname(os.path.dirname(BASEDIR)), 'product')
PROD_TEMPLATE = os.path.join(PROD_DIR, 'templates')

if os.path.exists(PROD_TEMPLATE):
    TEMPLATE_DIR = PROD_TEMPLATE
else:
    TEMPLATE_DIR = os.path.join(BASEDIR, 'templates')

TEMPLATE_DIR = os.path.relpath(TEMPLATE_DIR)

LOGGER = logging.getLogger('dellunit')


def _render(template, report_data, output_handle):
    """
    Render the report
    """

    LOGGER.debug('Using report templates from: %s', TEMPLATE_DIR)

    env = Environment(loader=FileSystemLoader(TEMPLATE_DIR),
                      trim_blocks=True, lstrip_blocks=True,
                      autoescape=True)

    env.tests['equals'] = operator.eq
    env.filters['tojson'] = json.dumps
    env.filters['datetime'] = jfilters.filter_datetime
    env.filters['datetime_format'] = jfilters.datetime_format
    template = env.get_template(template)

    output = template.render(report_data)
    output_handle.write(output.encode('utf8'))


def render_email(report_file, server, users):
    """
    Render the report
    """

    output = StringIO.StringIO()
    report_data = {}

    with open(report_file) as infile:
        report_data = json.load(infile)
        _render('email.html', report_data, output)

    summary = []
    keys = report_data['summary'].keys()
    keys.sort()
    for key in keys:
        if report_data['summary'][key] > 0:
            summary.append('%s %d' % (key.title(),
                                      report_data['summary'][key]))
    summary_str = ', '.join(summary)

    message = mailer.Message()
    message.From = 'nobody <nobody@dell.com>'
    message.To = users
    message.Subject = report_data['info']['name'] + ' - ' + summary_str
    message.Html = output.getvalue()

    sender = mailer.Mailer(server)
    sender.send(message)


def render_report(report_file, output_file):
    """
    Render the report
    """

    with open(report_file) as infile:
        with open(output_file, 'w') as output_handle:
            report_data = json.load(infile)
            _render('report.html', report_data, output_handle)
