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

package com.facebook.buck.parser.temporarytargetuniquenesschecker;

import com.facebook.buck.core.exceptions.DependencyStack;
import com.facebook.buck.core.model.BuildTarget;

/**
 * Check there's only one {@link BuildTarget} for {@link
 * com.facebook.buck.core.model.UnconfiguredBuildTarget}. We do that this until we use target
 * configuration in the output path.
 */
public interface TemporaryUnconfiguredTargetToTargetUniquenessChecker {
  /**
   * Register a target, check if there's already registered target with the same unconfigured
   * target, same flavors but different configuration.
   */
  void addTarget(BuildTarget buildTarget, DependencyStack dependencyStack);

  /**
   * Create a uniqueness checker, deny or no-op depending on hashed buck-out disabled or enabled.
   */
  static TemporaryUnconfiguredTargetToTargetUniquenessChecker create(
      boolean buckOutIncludeTargetConfigHash) {
    return buckOutIncludeTargetConfigHash
        ? TemporaryUnconfiguredTargetToTargetUniquenessCheckerAllow.instance
        : new TemporaryUnconfiguredTargetToTargetUniquenessCheckerDeny();
  }
}
