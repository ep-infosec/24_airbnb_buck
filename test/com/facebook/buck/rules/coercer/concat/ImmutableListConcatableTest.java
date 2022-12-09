/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.rules.coercer.concat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class ImmutableListConcatableTest {

  private ImmutableListConcatable<String> concatable;

  @Before
  public void setUp() {
    concatable = new ImmutableListConcatable<>();
  }

  @Test
  public void testConcatOfEmptyListCreatesEmptyList() {
    assertTrue(concatable.concat(Collections.emptyList()).isEmpty());
  }

  @Test
  public void testConcatPutsAllElementsInResult() {
    ImmutableList<String> result =
        concatable.concat(Arrays.asList(ImmutableList.of("1", "2"), ImmutableList.of("3", "4")));

    assertEquals(ImmutableList.of("1", "2", "3", "4"), result);
  }
}
