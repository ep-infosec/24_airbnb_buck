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

package com.facebook.buck.apple.toolchain;

import com.facebook.buck.core.util.immutables.BuckStyleValueWithBuilder;
import java.nio.file.Path;
import java.util.Optional;

/** Metadata about an Apple toolchain. */
@BuckStyleValueWithBuilder
public interface AppleToolchain {
  /** The identifier of the toolchain. For example: {@code com.apple.dt.XcodeDefault}. */
  String getIdentifier();

  /** The version number of the toolchain. For example: {@code 0630}. */
  Optional<String> getVersion();

  /** The path to the toolchain. For example, {@code .../XcodeDefault.xctoolchain}. */
  Path getPath();

  static Builder builder() {
    return new Builder();
  }

  class Builder extends ImmutableAppleToolchain.Builder {}
}
