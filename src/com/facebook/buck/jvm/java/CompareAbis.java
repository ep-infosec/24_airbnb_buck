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

import com.facebook.buck.core.build.buildable.context.BuildableContext;
import com.facebook.buck.core.build.context.BuildContext;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.rulekey.AddToRuleKey;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.attr.BuildOutputInitializer;
import com.facebook.buck.core.rules.attr.InitializableFromDisk;
import com.facebook.buck.core.rules.attr.SupportsInputBasedRuleKey;
import com.facebook.buck.core.rules.impl.AbstractBuildRuleWithDeclaredAndExtraDeps;
import com.facebook.buck.core.sourcepath.ExplicitBuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.io.BuildCellRelativePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.jvm.core.CalculateAbi;
import com.facebook.buck.jvm.core.DefaultJavaAbiInfo;
import com.facebook.buck.jvm.core.JavaAbiInfo;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.CopyStep;
import com.facebook.buck.step.fs.MkdirStep;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;

public class CompareAbis extends AbstractBuildRuleWithDeclaredAndExtraDeps
    implements SupportsInputBasedRuleKey, CalculateAbi, InitializableFromDisk<Object> {
  @AddToRuleKey private final SourcePath correctAbi;
  @AddToRuleKey private final SourcePath experimentalAbi;
  @AddToRuleKey private final JavaBuckConfig.SourceAbiVerificationMode verificationMode;

  private final Path outputPath;
  private final BuildOutputInitializer<Object> buildOutputInitializer;
  private final JavaAbiInfo javaAbiInfo;

  public CompareAbis(
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      BuildRuleParams params,
      SourcePath correctAbi,
      SourcePath experimentalAbi,
      JavaBuckConfig.SourceAbiVerificationMode verificationMode) {
    super(buildTarget, projectFilesystem, params);
    this.correctAbi = correctAbi;
    this.experimentalAbi = experimentalAbi;
    this.verificationMode = verificationMode;

    this.outputPath =
        BuildTargetPaths.getGenPath(getProjectFilesystem(), getBuildTarget(), "%s")
            .resolve(String.format("%s-abi.jar", getBuildTarget().getShortName()));

    this.javaAbiInfo = new DefaultJavaAbiInfo(getSourcePathToOutput());
    buildOutputInitializer = new BuildOutputInitializer<>(getBuildTarget(), this);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    ProjectFilesystem filesystem = getProjectFilesystem();
    SourcePathResolverAdapter sourcePathResolverAdapter = context.getSourcePathResolver();

    Path classAbiPath = sourcePathResolverAdapter.getAbsolutePath(correctAbi);
    Path sourceAbiPath = sourcePathResolverAdapter.getAbsolutePath(experimentalAbi);
    buildableContext.recordArtifact(outputPath);
    return ImmutableList.of(
        MkdirStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(), getProjectFilesystem(), outputPath.getParent())),
        DiffAbisStep.of(classAbiPath, sourceAbiPath, verificationMode),
        // We use the "safe" ABI as our output to prevent ABI generation problems from potentially
        // cascading and resulting in confusing diff results in dependent rules
        CopyStep.forFile(filesystem, classAbiPath, outputPath));
  }

  @Nullable
  @Override
  public SourcePath getSourcePathToOutput() {
    return ExplicitBuildTargetSourcePath.of(getBuildTarget(), outputPath);
  }

  @Override
  public void invalidateInitializeFromDiskState() {
    javaAbiInfo.invalidate();
  }

  @Override
  public Object initializeFromDisk(SourcePathResolverAdapter pathResolver) throws IOException {
    javaAbiInfo.load(pathResolver);
    return new Object();
  }

  @Override
  public BuildOutputInitializer<Object> getBuildOutputInitializer() {
    return buildOutputInitializer;
  }

  @Override
  public JavaAbiInfo getAbiInfo() {
    return javaAbiInfo;
  }
}
