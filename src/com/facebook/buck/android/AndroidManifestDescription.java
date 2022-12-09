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

import com.facebook.buck.core.description.arg.BuildRuleArg;
import com.facebook.buck.core.description.arg.HasDeclaredDeps;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.rules.BuildRuleCreationContextWithTargetGraph;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.DescriptionWithTargetGraph;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.util.immutables.RuleArg;
import com.google.common.collect.ImmutableSortedSet;
import org.immutables.value.Value;

public class AndroidManifestDescription
    implements DescriptionWithTargetGraph<AndroidManifestDescriptionArg> {

  private final AndroidManifestFactory androidManifestFactory;

  public AndroidManifestDescription(AndroidManifestFactory androidManifestFactory) {
    this.androidManifestFactory = androidManifestFactory;
  }

  @Override
  public Class<AndroidManifestDescriptionArg> getConstructorArgType() {
    return AndroidManifestDescriptionArg.class;
  }

  @Override
  public AndroidManifest createBuildRule(
      BuildRuleCreationContextWithTargetGraph context,
      BuildTarget buildTarget,
      BuildRuleParams params,
      AndroidManifestDescriptionArg args) {
    return androidManifestFactory.createBuildRule(
        buildTarget,
        context.getProjectFilesystem(),
        context.getActionGraphBuilder(),
        args.getDeps(),
        args.getSkeleton());
  }

  @RuleArg
  interface AbstractAndroidManifestDescriptionArg extends BuildRuleArg, HasDeclaredDeps {
    SourcePath getSkeleton();

    /**
     * A collection of dependencies that includes android_library rules. The manifest files of the
     * android_library rules will be filtered out to become dependent source files for the {@link
     * AndroidManifest}.
     */
    @Override
    @Value.NaturalOrder
    ImmutableSortedSet<BuildTarget> getDeps();
  }
}
