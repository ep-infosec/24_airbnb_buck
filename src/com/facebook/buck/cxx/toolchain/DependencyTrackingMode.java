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

package com.facebook.buck.cxx.toolchain;

/** Describes the underlying mechanism to track dependencies for a cxx compilation unit. */
public enum DependencyTrackingMode {
  /** MAKEFILE corresponds to `gcc -MD -MF depfile ...` on *nix */
  MAKEFILE,
  /** SHOW_HEADERS corresponds to `clang/gcc -H ...` on *nix */
  SHOW_HEADERS,
  /** SHOW_INCLUDES corresponds to `cl.exe /showIncludes ...` on windows */
  SHOW_INCLUDES,
  /** Some compilers - like ml64 - do not produce information about included files */
  NONE,
}
