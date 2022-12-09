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

package com.facebook.buck.features.lua;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.impl.NoopBuildRuleWithDeclaredAndExtraDeps;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.cxx.toolchain.CxxPlatform;
import com.facebook.buck.cxx.toolchain.nativelink.NativeLinkTargetGroup;
import com.facebook.buck.io.filesystem.ProjectFilesystem;

public abstract class CxxLuaExtension extends NoopBuildRuleWithDeclaredAndExtraDeps
    implements NativeLinkTargetGroup {

  public CxxLuaExtension(
      BuildTarget buildTarget, ProjectFilesystem projectFilesystem, BuildRuleParams params) {
    super(buildTarget, projectFilesystem, params);
  }

  public abstract String getModule(CxxPlatform cxxPlatform);

  public abstract SourcePath getExtension(CxxPlatform cxxPlatform);
}
