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

package com.facebook.buck.testutil;

import java.util.Optional;

/** An implementation of {@link UnixUtils} for Linux platforms */
public class LinuxUtils extends UnixUtils {
  private static final String OBJCOPY = "objcopy";

  public LinuxUtils() {}

  @Override
  public Optional<String> getObjcopy() {
    return Optional.of(findExecutable(OBJCOPY));
  }
}
