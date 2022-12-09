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

package com.facebook.buck.features.rust;

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.config.FakeBuckConfig;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Test;

public class RustBuckConfigTest {

  @Test
  public void getRustLibraryFlags() {
    RustBuckConfig customConfig =
        new RustBuckConfig(
            FakeBuckConfig.builder()
                .setSections(
                    ImmutableMap.of(
                        "rust#custom", ImmutableMap.of("rustc_flags", "-flag1"),
                        "rust", ImmutableMap.of("rustc_flags", "-flag2")))
                .build());
    assertThat(customConfig.getRustcLibraryFlags("custom"), Matchers.contains("-flag1"));
    assertThat(customConfig.getRustcLibraryFlags("custom2"), Matchers.contains("-flag2"));
  }
}
