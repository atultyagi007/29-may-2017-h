#!/usr/bin/python2

__unittest = True

import os

run_dir = os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()

if __name__ == '__main__':
    os.chdir(run_dir)

import sys
import time
import logging

# several of the modules in core expect to be able to import themselves... :(
sys.path.insert(1, os.path.abspath('libs/core'))
sys.path.append(os.path.abspath('libs'))
sys.path.append(os.path.abspath('tests'))


import dellunit
from dellunit import build_report_json, report
from libs.product import hooks
logtime = time.strftime("%Y%m%d_%H%M%S")

if __name__ == '__main__':
    prog = None

    try:
        prog = dellunit.TestProgram(logtime)
        jsonfile = ('report_%s.json'%logtime if (prog.timestamp_report == 1) else 'report.json')
        htmlfile = ('report_%s.html'%logtime if (prog.timestamp_report == 1) else 'report.html')
        JSON_FILE = os.path.abspath(os.path.join('logs', jsonfile))
        REPORT_FILE = os.path.abspath(os.path.join('logs', htmlfile))
    except:
        sys.stdout == sys.__stdout__
        sys.stderr == sys.__stderr__
        logging.exception('Fatal error during test, skipping report generation')
        os.chdir(current_dir)
        sys.exit(1)

    logging.info('Generating the report.json file')
    build_report_json.write_data(JSON_FILE, info=prog.info, logtime=logtime, timestamp_report=prog.timestamp_report)
    
    hooks.addResultURL()
    
    logging.info('Creating the report.html file')
    report.render_report(JSON_FILE, REPORT_FILE)
    
    hooks.resultCopy()
    try:
        if prog.mail_users:
            logging.info('Sending emails...')
            report.render_email(JSON_FILE, prog.mail_server, prog.mail_users)
    except:
        pass

    os.chdir(current_dir)
