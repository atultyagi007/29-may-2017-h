import os
import re
import json
import datetime


from core.dellunit.result import UTC, json_handler
from core.dellunit import __version__

def data_reader(logtime, timestamp_report=False):
    INPUT_FILE = (os.path.join('logs', 'testdata_%s.json'%logtime) if (timestamp_report) else os.path.join('logs', 'testdata.json'))
    with open(INPUT_FILE, 'r') as json_data:
        all_data = '[' + ','.join(json_data.readlines()) + ']'
        results = json.loads(all_data)

    def parse_date_time(ts_string):
        return datetime.datetime(*map(int, re.split('[\D]', ts_string))[:-2], tzinfo=UTC())

    for result in results:
        result['start'] = parse_date_time(result['start'])
        result['end'] = parse_date_time(result['end'])
        yield result


def get_data(**extra):
    """
    Generate the output data
    """

    logtime = extra['logtime']
    timestamp_report = extra['timestamp_report']
    summary = {'pass': 0, 'fail': 0, 'error': 0, 'block': 0, 'omit': 0}
    runtime_data = {}
    last_class = ""
    row = {}
    start_time = None
    end_time = None
    duration = 0
    results = []

    for testresult in data_reader(logtime, timestamp_report):
        runtime_data.update(testresult.pop('runtime_data', {}))
        if not start_time or testresult['start'] < start_time:
            start_time = testresult['start']
        if not end_time or testresult['end'] > end_time:
            end_time = testresult['end']
        summary[testresult['status']] += 1

        testresult['group_urls'] = [tuple(x) for x in testresult['group_urls']]

        if last_class != testresult['class']:
            if row:
                results.append(row)

            row = {'name': testresult['class'],
                   'description': testresult['class_doc'],
                   'urls': testresult['group_urls'],
                   'tests': []}
            last_class = testresult['class']

        if testresult['group_urls']:
            row['urls'] = list(set(row['urls'] + testresult['group_urls']))

        del testresult['class']
        del testresult['group_urls']
        del testresult['class_doc']
        row['tests'].append(testresult)

    if row:
        results.append(row)

    if start_time and end_time:
        duration = end_time - start_time

    returnval = {
        'results': results,
        'runtime': runtime_data,
        'start_time': start_time,
        'stop_time': end_time,
        'duration': duration,
        'summary': summary,
        'dellunit_version': __version__
    }

    returnval.update(extra)
    return returnval

def write_data(filename, **extra):
    data = get_data(logtime=extra['logtime'], timestamp_report=extra['timestamp_report'])
    data.update(extra)

    with open(filename, 'w') as outfile:
        json.dump(data, outfile, default=json_handler, sort_keys=True, indent=4, separators=(',', ': '))
