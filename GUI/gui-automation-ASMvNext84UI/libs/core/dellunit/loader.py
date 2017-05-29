import os
import sys
from core.dellunit.unittest2 import loader
from core.dellunit.unittest2.loader import VALID_MODULE_NAME

from fnmatch import fnmatch

class TestLoader(loader.TestLoader):
    """
    This class is responsible for loading tests according to various criteria
    and returning them wrapped in a TestSuite
    """

    def _find_tests(self, start_dir, pattern):
        """Used by discovery. Yields test suites it loads."""
        paths = os.listdir(start_dir)

        for path in paths:
            full_path = os.path.join(start_dir, path)
            if os.path.isfile(full_path):
                if not VALID_MODULE_NAME.match(path):
                    # valid Python identifiers only
                    continue
                if not self._match_path(path, full_path, pattern):
                    continue
                # if the test file matches, load it
                name = self._get_name_from_path(full_path)
                module = self._get_module_from_name(name)
                mod_file = os.path.abspath(getattr(module, '__file__', full_path))
                realpath = os.path.splitext(mod_file)[0]
                fullpath_noext = os.path.splitext(full_path)[0]
                if realpath.lower() != fullpath_noext.lower():
                    module_dir = os.path.dirname(realpath)
                    mod_name = os.path.splitext(os.path.basename(full_path))[0]
                    expected_dir = os.path.dirname(full_path)
                    msg = ("%r module incorrectly imported from %r. Expected %r. "
                           "Is this module globally installed?")
                    raise ImportError(msg % (mod_name, module_dir, expected_dir))
                yield self.loadTestsFromModule(module)
            elif os.path.isdir(full_path):
                if not os.path.isfile(os.path.join(full_path, '__init__.py')):
                    continue

                load_tests = None
                tests = None
                if fnmatch(path, pattern):
                    # only check load_tests if the package directory itself matches the filter
                    name = self._get_name_from_path(full_path)
                    package = self._get_module_from_name(name)
                    load_tests = getattr(package, 'load_tests', None)
                    tests = self.loadTestsFromModule(package, use_load_tests=False)

                if load_tests is None:
                    if tests is not None:
                        # tests loaded from package file
                        yield tests
                    # recurse into the package
                    for test in self._find_tests(full_path, pattern):
                        yield test
                else:
                    try:
                        yield load_tests(self, tests, pattern)
                    except Exception, e:
                        yield _make_failed_load_tests(package.__name__, e,
                                                      self.suiteClass)


defaultTestLoader = TestLoader()

