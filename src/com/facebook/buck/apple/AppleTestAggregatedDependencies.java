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

package com.facebook.buck.apple;

import com.facebook.buck.apple.toolchain.AppleCxxPlatform;
import com.facebook.buck.apple.toolchain.ApplePlatform;
import com.facebook.buck.core.build.buildable.context.BuildableContext;
import com.facebook.buck.core.build.context.BuildContext;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.rulekey.AddToRuleKey;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.impl.AbstractBuildRuleWithDeclaredAndExtraDeps;
import com.facebook.buck.core.sourcepath.ExplicitBuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.toolchain.tool.Tool;
import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.io.BuildCellRelativePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.WriteFileStep;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Creates a directory containing the static resources along with a merged static library containing
 * all of the symbols that a test target depends on. Useful for use with external build systems like
 * Xcode.
 */
public class AppleTestAggregatedDependencies extends AbstractBuildRuleWithDeclaredAndExtraDeps {
  private final Path aggregationRoot;
  private static final Logger LOG = Logger.get(AppleBundle.class);
  @AddToRuleKey private final ImmutableList<SourcePath> staticLibDeps;
  @AddToRuleKey private final AppleBundleResources resources;
  @AddToRuleKey private final ApplePlatform applePlatform;
  @AddToRuleKey private final Tool libTool;
  @AddToRuleKey private final Tool ibTool;

  private static String RESOURCES_BASENAME = "resources";

  private static String CODE_BASENAME = "code";

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    ImmutableList.Builder<Step> stepsBuilder = ImmutableList.builder();
    ImmutableList.Builder<Path> codeSignOnCopyPathsBuilder = ImmutableList.builder();

    stepsBuilder.addAll(
        MakeCleanDirectoryStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(), getProjectFilesystem(), aggregationRoot)));

    Path resourcesDir = aggregationRoot.resolve(RESOURCES_BASENAME);
    stepsBuilder.addAll(
        MakeCleanDirectoryStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(), getProjectFilesystem(), resourcesDir)));

    Path codeDir = aggregationRoot.resolve(CODE_BASENAME);
    stepsBuilder.addAll(
        MakeCleanDirectoryStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(), getProjectFilesystem(), codeDir)));

    AppleResourceProcessing.addStepsToCopyResources(
        context,
        stepsBuilder,
        codeSignOnCopyPathsBuilder,
        resources,
        false,
        resourcesDir,
        AppleBundleDestinations.platformDestinations(applePlatform),
        getProjectFilesystem(),
        ImmutableList.of(),
        false,
        applePlatform,
        LOG,
        ibTool,
        false,
        getBuildTarget(),
        Optional.empty());

    if (staticLibDeps.size() > 0) {
      Path argsFile =
          BuildTargetPaths.getScratchPath(getProjectFilesystem(), getBuildTarget(), "argsfile.tmp");
      String output =
          staticLibDeps.stream()
              .map(t -> context.getSourcePathResolver().getAbsolutePath(t).toString())
              .collect(Collectors.joining("\n"));
      stepsBuilder.add(new WriteFileStep(getProjectFilesystem(), output, argsFile, false));
      stepsBuilder.add(
          new LibtoolStep(
              getProjectFilesystem(),
              libTool.getEnvironment(context.getSourcePathResolver()),
              libTool.getCommandPrefix(context.getSourcePathResolver()),
              argsFile,
              codeDir.resolve("TEST_DEPS.a"),
              ImmutableList.of("-no_warning_for_no_symbols"),
              LibtoolStep.Style.STATIC));
    }

    return stepsBuilder.build();
  }

  AppleTestAggregatedDependencies(
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      BuildRuleParams params,
      Path aggregationRoot,
      AppleBundleResources resources,
      AppleCxxPlatform appleCxxPlatform,
      ImmutableList<SourcePath> staticLibDeps) {
    super(buildTarget, projectFilesystem, params);
    this.aggregationRoot = aggregationRoot;
    this.resources = resources;
    this.applePlatform = appleCxxPlatform.getAppleSdk().getApplePlatform();
    this.libTool = appleCxxPlatform.getLibtool();
    this.ibTool = appleCxxPlatform.getIbtool();
    this.staticLibDeps = ImmutableList.copyOf(staticLibDeps);
  }

  @Override
  public SourcePath getSourcePathToOutput() {
    return ExplicitBuildTargetSourcePath.of(getBuildTarget(), aggregationRoot);
  }
}
