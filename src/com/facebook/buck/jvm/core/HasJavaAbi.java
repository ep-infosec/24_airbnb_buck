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

package com.facebook.buck.jvm.core;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.sourcepath.SourcePath;
import java.util.Optional;

public interface HasJavaAbi {
  JavaAbiInfo getAbiInfo();

  /** @return the {@link SourcePath} representing the ABI Jar for this rule. */
  default Optional<BuildTarget> getAbiJar() {
    return Optional.of(
        getAbiInfo().getBuildTarget().withAppendedFlavors(JavaAbis.CLASS_ABI_FLAVOR));
  }

  default Optional<BuildTarget> getSourceOnlyAbiJar() {
    return Optional.empty();
  }
}
