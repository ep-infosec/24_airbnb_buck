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

package com.facebook.buck.features.haskell;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.impl.NoopBuildRuleWithDeclaredAndExtraDeps;
import com.facebook.buck.cxx.CxxPreprocessorDep;
import com.facebook.buck.cxx.toolchain.nativelink.LegacyNativeLinkableGroup;
import com.facebook.buck.io.filesystem.ProjectFilesystem;

public abstract class HaskellLibrary extends NoopBuildRuleWithDeclaredAndExtraDeps
    implements HaskellCompileDep,
        HaskellOmnibusLinkable,
        LegacyNativeLinkableGroup,
        CxxPreprocessorDep {

  public HaskellLibrary(
      BuildTarget buildTarget, ProjectFilesystem projectFilesystem, BuildRuleParams params) {
    super(buildTarget, projectFilesystem, params);
  }
}
