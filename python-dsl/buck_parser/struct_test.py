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

from __future__ import absolute_import, division, print_function, with_statement

import unittest

from . import struct


class StructTest(unittest.TestCase):
    def testPropertyAccess(self):
        self.assertEqual(struct.struct(foo="bar").foo, "bar")

    def testJsonSerialization(self):
        self.assertEqual(struct.struct(foo="bar").to_json(), '{"foo":"bar"}')

    def testJsonKeysAreSorted(self):
        self.assertEqual(
            struct.struct(c="c", b="b", a="a").to_json(), '{"a":"a","b":"b","c":"c"}'
        )

    def testNestedJsonSerialization(self):
        self.assertEqual(
            struct.struct(foo=struct.struct(bar="baz")).to_json(),
            '{"foo":{"bar":"baz"}}',
        )

    def testCannotMutateAField(self):
        with self.assertRaisesRegexp(AttributeError, "can't set attribute"):
            struct.struct(foo="foo").foo = "bar"

    def testInequality(self):
        x = struct.struct(foo="bar")
        y = struct.struct(foo="baz")
        self.assertNotEqual(x, y)

    def testCanUseAsAHashKey(self):
        x = struct.struct(foo="bar")
        y = struct.struct(foo="baz")
        dictionary = {x: "x", y: "y"}
        self.assertEqual(dictionary[x], "x")
        self.assertNotEqual(dictionary[y], "x")

    def testHasAttrForExistingAttribute(self):
        self.assertTrue(hasattr(struct.struct(foo="bar"), "foo"))

    def testHasAttrForNonExistingAttribute(self):
        self.assertFalse(hasattr(struct.struct(foo="bar"), "does_not_exist"))

    def testGetAttrForExistingAttribute(self):
        x = struct.struct(foo="bar")
        self.assertEqual("bar", getattr(x, "foo"))

    def testGetAttrForNonExistingAttribute(self):
        x = struct.struct(foo="bar")
        self.assertEqual("default", getattr(x, "does_not_exist", "default"))

    def testRepr(self):
        x = struct.struct(foo="bar", bar="baz")
        self.assertEqual("struct(foo='bar', bar='baz')", repr(x))

    def testNestedRepr(self):
        x = struct.struct(foo="bar")
        y = struct.struct(x=x)
        self.assertEqual("struct(x=struct(foo='bar'))", repr(y))

    def testDotsAreNotAllowedInFieldNames(self):
        with self.assertRaisesRegexp(
            ValueError,
            "Field names can only contain alphanumeric characters and underscores: 'foo.bar'",
        ):
            struct.struct(**{"foo.bar": "foo"})

    def testDashesAreNotAllowedInFieldNames(self):
        with self.assertRaisesRegexp(
            ValueError,
            "Field names can only contain alphanumeric characters and underscores: 'foo-bar'",
        ):
            struct.struct(**{"foo-bar": "foo"})

    def testDigitsAreNotAllowedInFieldNameStarts(self):
        with self.assertRaisesRegexp(
            ValueError, "Field names cannot start with a number: '2foo'"
        ):
            struct.struct(**{"2foo": "foo"})

    def testKeywordsAreNotAllowedAsFieldNames(self):
        with self.assertRaisesRegexp(
            ValueError, "Field names cannot be a keyword: 'try'"
        ):
            struct.struct(**{"try": "foo"})
