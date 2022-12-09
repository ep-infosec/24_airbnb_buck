#!/usr/bin/env python
# Copyright (c) Facebook, Inc. and its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


from __future__ import print_function

import contextlib
import json
import optparse
import os
import shutil
import sys
import tempfile
import zipfile


def append_sys_path(buck_root):
    sys.path.insert(0, os.path.join(buck_root, "third-party/py/pex"))
    sys.path.insert(0, os.path.join(buck_root, "third-party/py/setuptools"))


def find_buck_root():
    f = os.path.abspath(__file__)
    while not os.path.exists(os.path.join(f, "third-party/py/pex/BUCK")):
        p = os.path.dirname(f)
        if p == f:  # FS root
            raise Exception("buck root not found for {}".format(__file__))
        f = p
    return f


# This script can be executed either from a pex file or in a standalone mode.
# A pex file should contain all the necessary dependencies, but in standalone mode
# these dependencies need to be configured before importing them.
if not zipfile.is_zipfile(sys.argv[0]):
    append_sys_path(find_buck_root())

import pkg_resources  # noqa isort:skip

from pex.interpreter import PythonIdentity, PythonInterpreter  # noqa isort:skip
from pex.pex_builder import PEXBuilder  # noqa isort:skip


def dereference_symlinks(src):
    """
    Resolve all symbolic references that `src` points to.  Note that this
    is different than `os.path.realpath` as path components leading up to
    the final location may still be symbolic links.
    """

    while os.path.islink(src):
        src = os.path.join(os.path.dirname(src), os.readlink(src))

    return src


@contextlib.contextmanager
def closable_named_temporary_file():
    """
    Due to a bug in python (https://bugs.python.org/issue14243), we need to be able to close() the
    temporary file without deleting it.
    """
    fp = tempfile.NamedTemporaryFile(delete=False)
    try:
        with fp:
            yield fp
    finally:
        os.remove(fp.name)


def copy_package(pex_builder, package, resource="", prefix=""):
    """
    Copies the code of a package to the pex using the pkg_resources API.
    """
    if pkg_resources.resource_isdir(package, resource):
        for entry in pkg_resources.resource_listdir(package, resource):
            copy_package(pex_builder, package, os.path.join(resource, entry), prefix)
    else:
        if not resource.endswith(".py") or "tests" in resource:
            return

        target_path = os.path.join(package, resource)

        with closable_named_temporary_file() as fp:
            with pkg_resources.resource_stream(package, resource) as r:
                shutil.copyfileobj(r, fp)
                fp.close()

                pex_builder.add_source(fp.name, os.path.join(prefix, target_path))


def main():
    parser = optparse.OptionParser(usage="usage: %prog [options] output")
    parser.add_option("--entry-point", default="__main__")
    parser.add_option("--directory", action="store_true", default=False)
    parser.add_option(
        "--no-zip-safe", action="store_false", dest="zip_safe", default=True
    )
    parser.add_option("--python", default="")
    parser.add_option("--python-version", default="")
    parser.add_option("--python-shebang", default=None)
    parser.add_option("--preload", action="append", default=[])
    options, args = parser.parse_args()
    if len(args) == 1:
        output = args[0]
    else:
        parser.error("'output' positional argument is required")
        return 1

    # The manifest is passed via stdin, as it can sometimes get too large
    # to be passed as a CLA.
    manifest = json.load(sys.stdin)

    # The version of pkg_resources.py (from setuptools) on some distros is
    # too old for PEX.  So we keep a recent version in the buck repo and
    # force it into the process by constructing a custom PythonInterpreter
    # instance using it.
    if not options.python:
        options.python = sys.executable
        identity = PythonIdentity.get()
    elif not options.python_version:
        # Note: this is expensive (~500ms). prefer passing --python-version when possible.
        identity = PythonInterpreter.from_binary(options.python).identity
    else:
        # Convert "CPython 2.7" to "CPython 2 7 0"
        python_version = options.python_version.replace(".", " ").split()
        if len(python_version) == 3:
            python_version.append("0")
        identity = PythonIdentity.from_id_string(" ".join(python_version))

    interpreter = PythonInterpreter(options.python, identity, extras={})

    pex_builder = PEXBuilder(
        path=output if options.directory else None, interpreter=interpreter
    )

    if options.python_shebang is not None:
        pex_builder.set_shebang(options.python_shebang)

    # Set whether this PEX as zip-safe, meaning everything will stayed zipped up
    # and we'll rely on python's zip-import mechanism to load modules from
    # the PEX.  This may not work in some situations (e.g. native
    # libraries, libraries that want to find resources via the FS).
    pex_builder.info.zip_safe = options.zip_safe

    # Set the starting point for this PEX.
    pex_builder.info.entry_point = options.entry_point

    # Copy in our version of `pkg_resources` & `_markerlib`.
    copy_package(pex_builder, "pkg_resources", prefix=pex_builder.BOOTSTRAP_DIR)
    copy_package(pex_builder, "_markerlib", prefix=pex_builder.BOOTSTRAP_DIR)

    # Add the sources listed in the manifest.
    for dst, src in manifest["modules"].items():
        # NOTE(agallagher): calls the `add_source` and `add_resource` below
        # hard-link the given source into the PEX temp dir.  Since OS X and
        # Linux behave different when hard-linking a source that is a
        # symbolic link (Linux does *not* follow symlinks), resolve any
        # layers of symlinks here to get consistent behavior.
        try:
            pex_builder.add_source(dereference_symlinks(src), dst)
        except OSError as e:
            raise Exception("Failed to add {}: {}".format(src, e))

    # Add resources listed in the manifest.
    for dst, src in manifest["resources"].items():
        # NOTE(agallagher): see rationale above.
        pex_builder.add_resource(dereference_symlinks(src), dst)

    # Add resources listed in the manifest.
    for dst, src in manifest["nativeLibraries"].items():
        # NOTE(agallagher): see rationale above.
        pex_builder.add_resource(dereference_symlinks(src), dst)

    if options.directory:
        pex_builder.freeze(code_hash=False, bytecode_compile=False)
    else:
        pex_builder.build(output)


sys.exit(main())
