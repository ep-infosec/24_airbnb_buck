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

package com.facebook.buck.features.js;

import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.sourcepath.ExplicitBuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;

/** Represents output paths of JS #dependecy-flavored builds. */
public interface JsDependenciesOutputs extends BuildRule {
  /**
   * @return a {@link SourcePath} to a dependencies graph file associated with a built JavaScript.
   */
  default SourcePath getSourcePathToDepsFile() {
    return ExplicitBuildTargetSourcePath.of(
        getBuildTarget(),
        BuildTargetPaths.getGenPath(getProjectFilesystem(), getBuildTarget(), "%s.deps"));
  }
}
