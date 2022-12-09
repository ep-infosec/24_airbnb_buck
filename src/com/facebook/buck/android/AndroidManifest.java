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

import com.facebook.buck.android.apkmodule.APKModule;
import com.facebook.buck.core.build.buildable.context.BuildableContext;
import com.facebook.buck.core.build.context.BuildContext;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.rulekey.AddToRuleKey;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.SourcePathRuleFinder;
import com.facebook.buck.core.rules.common.BuildableSupport;
import com.facebook.buck.core.rules.impl.AbstractBuildRule;
import com.facebook.buck.core.sourcepath.ExplicitBuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.io.BuildCellRelativePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.RmStep;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Path;
import java.util.Collection;
import java.util.SortedSet;
import java.util.function.Supplier;

/**
 * {@link AndroidManifest} is a {@link BuildRule} that can generate an Android manifest from a
 * skeleton manifest and the library manifests from its dependencies.
 *
 * <pre>
 * android_manifest(
 *   name = 'my_manifest',
 *   skeleton = 'AndroidManifestSkeleton.xml',
 *   deps = [
 *     ':sample_manifest',
 *     # Additional dependent android_resource and android_library rules would be listed here,
 *     # as well.
 *   ],
 * )
 * </pre>
 *
 * This will produce a file under buck-out/gen that will be parameterized by the name of the {@code
 * android_manifest} rule. This can be used as follows:
 *
 * <pre>
 * android_binary(
 *   name = 'my_app',
 *   manifest = ':my_manifest',
 *   ...
 * )
 * </pre>
 */
public class AndroidManifest extends AbstractBuildRule {

  @AddToRuleKey private final SourcePath skeletonFile;

  /** These must be sorted so the rule key is stable. */
  @AddToRuleKey private final ImmutableSortedSet<SourcePath> manifestFiles;

  @AddToRuleKey private final APKModule module;

  private final Path pathToOutputFile;
  private final Supplier<SortedSet<BuildRule>> buildDepsSupplier;

  protected AndroidManifest(
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      SourcePathRuleFinder finder,
      SourcePath skeletonFile,
      APKModule module,
      Collection<SourcePath> manifestFiles) {
    super(buildTarget, projectFilesystem);
    this.skeletonFile = skeletonFile;
    this.module = module;
    this.manifestFiles = ImmutableSortedSet.copyOf(manifestFiles);
    this.pathToOutputFile =
        BuildTargetPaths.getGenPath(
            getProjectFilesystem(), buildTarget, "AndroidManifest__%s__.xml");
    this.buildDepsSupplier = BuildableSupport.buildDepsSupplier(this, finder);
  }

  @Override
  public SortedSet<BuildRule> getBuildDeps() {
    return buildDepsSupplier.get();
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {

    ImmutableList.Builder<Step> commands = ImmutableList.builder();

    // Clear out the old file, if it exists.
    commands.add(
        RmStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(), getProjectFilesystem(), pathToOutputFile)));

    // Make sure the directory for the output file exists.
    commands.add(
        MkdirStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(),
                getProjectFilesystem(),
                pathToOutputFile.getParent())));

    commands.add(
        new GenerateManifestStep(
            getProjectFilesystem(),
            context.getSourcePathResolver().getAbsolutePath(skeletonFile),
            module,
            context.getSourcePathResolver().getAllAbsolutePaths(manifestFiles),
            context.getSourcePathResolver().getRelativePath(getSourcePathToOutput()),
            getProjectFilesystem()
                .resolve(
                    BuildTargetPaths.getScratchPath(
                        getProjectFilesystem(), getBuildTarget(), "%s/merge-report.txt"))));

    buildableContext.recordArtifact(pathToOutputFile);
    return commands.build();
  }

  @Override
  public SourcePath getSourcePathToOutput() {
    return ExplicitBuildTargetSourcePath.of(getBuildTarget(), pathToOutputFile);
  }
}
