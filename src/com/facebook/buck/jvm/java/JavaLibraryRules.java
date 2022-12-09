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

package com.facebook.buck.jvm.java;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.cxx.toolchain.CxxPlatform;
import com.facebook.buck.cxx.toolchain.nativelink.NativeLinkableGroup;
import com.facebook.buck.cxx.toolchain.nativelink.NativeLinkableGroups;
import com.facebook.buck.cxx.toolchain.nativelink.NativeLinkables;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.jvm.core.CalculateAbi;
import com.facebook.buck.jvm.core.HasJavaAbi;
import com.facebook.buck.jvm.core.JavaLibrary;
import com.facebook.buck.rules.modern.BuildCellRelativePathFactory;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Common utilities for working with {@link JavaLibrary} objects. */
public class JavaLibraryRules {

  /** Utility class: do not instantiate. */
  private JavaLibraryRules() {}

  static void addAccumulateClassNamesStep(
      BuildCellRelativePathFactory cellRelativePathFactory,
      ProjectFilesystem filesystem,
      Builder<Step> steps,
      Optional<Path> pathToClasses,
      Path pathToClassHashes) {
    steps.add(MkdirStep.of(cellRelativePathFactory.from(pathToClassHashes.getParent())));
    steps.add(new AccumulateClassNamesStep(filesystem, pathToClasses, pathToClassHashes));
  }

  static JavaLibrary.Data initializeFromDisk(BuildTarget buildTarget, ProjectFilesystem filesystem)
      throws IOException {
    List<String> lines = filesystem.readLines(getPathToClassHashes(buildTarget, filesystem));
    return new JavaLibrary.Data(AccumulateClassNamesStep.parseClassHashes(lines));
  }

  static Path getPathToClassHashes(BuildTarget buildTarget, ProjectFilesystem filesystem) {
    return BuildTargetPaths.getGenPath(filesystem, buildTarget, "%s.classes.txt");
  }

  /**
   * @return all the transitive native libraries a rule depends on, represented as a map from their
   *     system-specific library names to their {@link SourcePath} objects.
   */
  public static ImmutableMap<String, SourcePath> getNativeLibraries(
      Iterable<BuildRule> deps, CxxPlatform cxxPlatform, ActionGraphBuilder graphBuilder) {
    // Allow the transitive walk to find NativeLinkables through the BuildRuleParams deps of a
    // JavaLibrary or CalculateAbi object. The deps may be either one depending if we're compiling
    // against ABI rules or full rules
    ImmutableMap<BuildTarget, NativeLinkableGroup> roots =
        NativeLinkableGroups.getNativeLinkableRoots(
            deps,
            r ->
                r instanceof JavaLibrary
                    ? Optional.of(((JavaLibrary) r).getDepsForTransitiveClasspathEntries())
                    : r instanceof CalculateAbi ? Optional.of(r.getBuildDeps()) : Optional.empty());
    return NativeLinkables.getTransitiveSharedLibraries(
        graphBuilder,
        Iterables.transform(roots.values(), g -> g.getNativeLinkable(cxxPlatform, graphBuilder)),
        true);
  }

  public static ImmutableSortedSet<BuildRule> getAbiRules(
      ActionGraphBuilder graphBuilder, Iterable<BuildRule> inputs) {
    ImmutableSortedSet.Builder<BuildRule> abiRules = ImmutableSortedSet.naturalOrder();
    for (BuildRule input : inputs) {
      if (input instanceof HasJavaAbi && ((HasJavaAbi) input).getAbiJar().isPresent()) {
        Optional<BuildTarget> abiJarTarget = ((HasJavaAbi) input).getAbiJar();
        BuildRule abiJarRule = graphBuilder.requireRule(abiJarTarget.get());
        abiRules.add(abiJarRule);
      }
    }
    return abiRules.build();
  }

  public static ImmutableSortedSet<BuildRule> getSourceOnlyAbiRules(
      ActionGraphBuilder graphBuilder, Iterable<BuildRule> inputs) {
    ImmutableSortedSet.Builder<BuildRule> abiRules = ImmutableSortedSet.naturalOrder();
    for (BuildRule input : inputs) {
      if (input instanceof HasJavaAbi) {
        HasJavaAbi hasAbi = (HasJavaAbi) input;
        Optional<BuildTarget> abiJarTarget = hasAbi.getSourceOnlyAbiJar();
        if (!abiJarTarget.isPresent()) {
          abiJarTarget = hasAbi.getAbiJar();
        }

        if (abiJarTarget.isPresent()) {
          BuildRule abiJarRule = graphBuilder.requireRule(abiJarTarget.get());
          abiRules.add(abiJarRule);
        }
      }
    }
    return abiRules.build();
  }
}
