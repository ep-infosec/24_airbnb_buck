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

package com.facebook.buck.features.ocaml;

import com.facebook.buck.core.model.FlavorDomain;
import com.facebook.buck.core.toolchain.Toolchain;
import com.facebook.buck.core.util.immutables.BuckStyleValue;

/** Container for {@link OcamlPlatform}s. */
@BuckStyleValue
interface OcamlToolchain extends Toolchain {

  String DEFAULT_NAME = "ocaml-toolchain";

  static OcamlToolchain of(
      OcamlPlatform defaultOcamlPlatform, FlavorDomain<OcamlPlatform> ocamlPlatforms) {
    return ImmutableOcamlToolchain.of(defaultOcamlPlatform, ocamlPlatforms);
  }

  OcamlPlatform getDefaultOcamlPlatform();

  FlavorDomain<OcamlPlatform> getOcamlPlatforms();

  @Override
  default String getName() {
    return DEFAULT_NAME;
  }
}
