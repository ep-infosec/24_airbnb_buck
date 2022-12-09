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

package com.facebook.buck.android;

import com.facebook.buck.android.toolchain.AndroidPlatformTarget;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.rulekey.AddToRuleKey;
import com.facebook.buck.core.toolchain.ToolchainProvider;
import com.facebook.buck.jvm.java.ExtraClasspathProvider;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nullable;

public class AndroidClasspathProvider implements ExtraClasspathProvider {
  @AddToRuleKey @Nullable private final AndroidPlatformTarget androidPlatformTarget;

  public AndroidClasspathProvider(
      ToolchainProvider toolchainProvider, TargetConfiguration toolchainTargetConfiguration) {
    this.androidPlatformTarget =
        toolchainProvider.getByName(
            AndroidPlatformTarget.DEFAULT_NAME,
            toolchainTargetConfiguration,
            AndroidPlatformTarget.class);
  }

  @Override
  public Iterable<Path> getExtraClasspath() {
    return Objects.requireNonNull(androidPlatformTarget).getBootclasspathEntries();
  }
}
