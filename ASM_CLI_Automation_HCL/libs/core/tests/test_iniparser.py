import unittest
import iniparser

DEFAULTS = {
    'Information': {
        'Name': 'Test Report',
        'Description': '',
    },
    'dellunit': {
        'failfast': 'True',
        'verbosity': 'DEBUG',
        'catchbreak': 'False',
        'buffer': 'True',
        'tests': '()',
        'retry_failed': 'True',
        'separate_output': 'False',
        'mail_server': 'mail.delllabs.net',
        'mail_users': '()'
    },
}

class IniParserTest(unittest.TestCase):
    def setUp(self):
        self.p = iniparser.IniParser(defaults=DEFAULTS)

    def test_tuple_valid_tuple(self):
        self.p.set('dellunit', 'tests', "('tests',)")
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ('tests',))

    def test_tuple_paren_string(self):
        self.p.set('dellunit', 'tests', "('tests')")
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ('tests',))

    def test_plain_string(self):
        self.p.set('dellunit', 'tests', 'tests')
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ('tests',))

    def test_quoted_string(self):
        self.p.set('dellunit', 'tests', '"tests"')
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ('tests',))

    def test_list(self):
        self.p.set('dellunit', 'tests', '["tests"]')
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ('tests',))

    def test_nothing(self):
        self.p.set('dellunit', 'tests', '')
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ())

    def test_quoted_empty(self):
        self.p.set('dellunit', 'tests', '""')
        self.p.to_tuple('dellunit', 'tests')
        value = self.p.get('dellunit', 'tests')
        self.assertEqual(value, ())

    def test_get_dict(self):
        self.assertEqual(self.p.get_dict('dellunit'), DEFAULTS['dellunit'])

if __name__ == '__main__':
    unittest.main()
