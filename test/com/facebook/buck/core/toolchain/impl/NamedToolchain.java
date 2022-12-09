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

package com.facebook.buck.core.toolchain.impl;

import com.facebook.buck.core.toolchain.Toolchain;
import com.facebook.buck.core.util.immutables.BuckStyleValue;

/**
 * Helper class that assists {@link ToolchainProviderBuilder} with creating toolchains.
 *
 * <p>It should be used whenever a toolchain needs to be passed together with its name.
 *
 * <p>For example, toolchains do not have a name in the interface, but {@link
 * ToolchainProviderBuilder} needs to know a toolchain name when registering a toolchain. This class
 * allows consolidate creation of toolchains in helper methods that return instances of this class
 * and use them in {@link ToolchainProviderBuilder#withToolchain(NamedToolchain)}.
 */
@BuckStyleValue
public interface NamedToolchain {

  String getName();

  Toolchain getToolchain();

  static NamedToolchain of(String name, Toolchain toolchain) {
    return ImmutableNamedToolchain.of(name, toolchain);
  }
}
