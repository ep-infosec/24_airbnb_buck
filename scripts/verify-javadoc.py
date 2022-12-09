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

import sys


WARNING_WHITELIST_URLS = (
    "http://docs.oracle.com/javase/7/docs/api/package-list",
    "https://junit-team.github.io/junit/javadoc/latest/package-list",
)

WARNING_WHITELIST = frozenset(
    [
        "  [javadoc] javadoc: warning - Error fetching URL: " + url
        for url in WARNING_WHITELIST_URLS
    ]
    + ["  [javadoc] 2 warnings"]
)


def main(log_file):
    """Exit with a non-zero return code if line is not in the warning whitelist."""
    errors = []
    with open(log_file) as f:
        for line in f:
            line = line.rstrip()
            # If there is a warning from `javadoc`, check whether it is in the whitelist.
            if "warning" in line.lower() and line not in WARNING_WHITELIST:
                errors.append(line)
    if len(errors):
        print("Unexpected Javadoc errors (%d):" % len(errors))
        for error in errors:
            print(error)
        sys.exit(1)


if __name__ == "__main__":
    main(sys.argv[1])
