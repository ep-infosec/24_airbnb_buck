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

package com.facebook.buck.cxx;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.sourcepath.BuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;

/**
 * Generate instances of {@link PreInclude} based on the input parameters, generally coming from a
 * {@link CxxSourceRuleFactory}. Logic for building such subclasses is all encapsulated here.
 *
 * @see CxxPrefixHeader
 * @see CxxPrecompiledHeaderTemplate
 */
@BuckStyleValue
abstract class PreIncludeFactory {

  protected abstract ProjectFilesystem getProjectFilesystem();

  protected abstract BuildTarget getBaseBuildTarget();

  protected abstract ActionGraphBuilder getActionGraphBuilder();

  protected abstract SourcePathResolverAdapter getPathResolver();

  /** NOTE: {@code prefix_header} is incompatible with {@code precompiled_header}. */
  protected abstract Optional<SourcePath> getPrefixHeader();

  /** NOTE: {@code precompiled_header} is incompatible with {@code prefix_header}. */
  protected abstract Optional<SourcePath> getPrecompiledHeader();

  /**
   * Get (possibly creating) the {@link PreInclude} instance corresponding to calling rule's {@code
   * prefix_header} or {@code precompiled_header}, whichever is applicable, or empty if neither is
   * used.
   *
   * <p>If {@code prefix_header} is used: Create a {@link CxxPrefixHeader} rule (which is-a
   * `PreInclude`) for this header file. This PreInclude has no additional deps; those from the
   * rules which use the prefix header should suffice. This header is built on-the-fly if it doesn't
   * exist already. Its target will be the same as the rule being processed in this class, with an
   * additional flavor appended, see {@link CxxPrefixHeader#FLAVOR}.
   *
   * <p>If {@code precompiled_header} is used: Return the existing {@link
   * CxxPrecompiledHeaderTemplate} (also a PreInclude) identified by {@link
   * #getPrecompiledHeader()}, a (possibly-empty) build target reference.
   */
  protected Optional<PreInclude> getPreInclude() {
    if (getPrefixHeader().isPresent()) {
      return Optional.of(
          (CxxPrefixHeader)
              getActionGraphBuilder()
                  .computeIfAbsent(
                      getBaseBuildTarget().withAppendedFlavors(CxxPrefixHeader.FLAVOR),
                      prefixTarget ->
                          new CxxPrefixHeader(
                              prefixTarget,
                              getProjectFilesystem(),
                              ImmutableSortedSet.of(),
                              getPrefixHeader().get(),
                              getPathResolver().getAbsolutePath(getPrefixHeader().get()))));
    }

    if (getPrecompiledHeader().isPresent()) {
      Preconditions.checkState(getPrecompiledHeader().get() instanceof BuildTargetSourcePath);
      BuildTargetSourcePath headerPath = (BuildTargetSourcePath) getPrecompiledHeader().get();
      BuildRule rule = getActionGraphBuilder().requireRule(headerPath.getTarget());
      Preconditions.checkArgument(rule instanceof CxxPrecompiledHeaderTemplate);
      return Optional.of((CxxPrecompiledHeaderTemplate) rule);
    }

    return Optional.empty();
  }
}
