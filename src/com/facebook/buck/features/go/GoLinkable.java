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

package com.facebook.buck.features.go;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.SourcePathRuleFinder;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;

@BuckStyleValue
abstract class GoLinkable {

  abstract ImmutableMap<Path, SourcePath> getGoLinkInput();

  abstract ImmutableSet<BuildTarget> getExportedDeps();

  public Iterable<BuildRule> getDeps(SourcePathRuleFinder ruleFinder) {
    return ruleFinder.filterBuildRuleInputs(getGoLinkInput().values());
  }

  static GoLinkable of(ImmutableMap<Path, SourcePath> goLinkInput) {
    return of(goLinkInput, ImmutableSet.of());
  }

  static GoLinkable of(
      ImmutableMap<Path, SourcePath> goLinkInput, ImmutableSet<BuildTarget> exportedDeps) {
    return ImmutableGoLinkable.of(goLinkInput, exportedDeps);
  }
}
