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

package com.facebook.buck.shell;

import com.facebook.buck.core.config.BuckConfig;
import com.facebook.buck.core.config.FakeBuckConfig;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.targetgraph.AbstractNodeBuilder;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.toolchain.ToolchainProvider;
import com.facebook.buck.core.toolchain.impl.ToolchainProviderBuilder;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.rules.coercer.SourceSet;
import com.facebook.buck.rules.macros.StringWithMacros;
import com.facebook.buck.rules.macros.StringWithMacrosUtils;
import com.facebook.buck.sandbox.NoSandboxExecutionStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import javax.annotation.Nullable;

public class GenruleBuilder
    extends AbstractNodeBuilder<
        GenruleDescriptionArg.Builder, GenruleDescriptionArg, GenruleDescription, Genrule> {

  private GenruleBuilder(BuildTarget target) {
    super(
        new GenruleDescription(
            createToolchainProvider(),
            FakeBuckConfig.builder().build(),
            new NoSandboxExecutionStrategy()),
        target);
  }

  private GenruleBuilder(BuildTarget target, ToolchainProvider toolchainProvider) {
    super(
        new GenruleDescription(
            toolchainProvider, FakeBuckConfig.builder().build(), new NoSandboxExecutionStrategy()),
        target);
  }

  private GenruleBuilder(BuildTarget target, ProjectFilesystem filesystem) {
    super(
        new GenruleDescription(
            createToolchainProvider(),
            FakeBuckConfig.builder().build(),
            new NoSandboxExecutionStrategy()),
        target,
        filesystem);
  }

  private GenruleBuilder(BuildTarget target, BuckConfig config) {
    super(
        new GenruleDescription(createToolchainProvider(), config, new NoSandboxExecutionStrategy()),
        target);
  }

  private static ToolchainProvider createToolchainProvider() {
    return new ToolchainProviderBuilder().build();
  }

  public static GenruleBuilder newGenruleBuilder(BuildTarget target) {
    return new GenruleBuilder(target);
  }

  public static GenruleBuilder newGenruleBuilder(
      BuildTarget target, ToolchainProvider toolchainProvider) {
    return new GenruleBuilder(target, toolchainProvider);
  }

  public static GenruleBuilder newGenruleBuilder(BuildTarget target, ProjectFilesystem filesystem) {
    return new GenruleBuilder(target, filesystem);
  }

  public static GenruleBuilder newGenruleBuilder(BuildTarget target, BuckConfig config) {
    return new GenruleBuilder(target, config);
  }

  public GenruleBuilder setOut(String out) {
    getArgForPopulating().setOut(out);
    return this;
  }

  public GenruleBuilder setOuts(ImmutableMap<String, ImmutableSet<String>> outs) {
    getArgForPopulating().setOuts(outs);
    return this;
  }

  public GenruleBuilder setBash(@Nullable String bash) {
    getArgForPopulating().setBash(Optional.ofNullable(bash).map(StringWithMacrosUtils::format));
    return this;
  }

  public GenruleBuilder setCmd(@Nullable StringWithMacros cmd) {
    getArgForPopulating().setCmd(Optional.ofNullable(cmd));
    return this;
  }

  public GenruleBuilder setCmd(@Nullable String cmd) {
    getArgForPopulating().setCmd(Optional.ofNullable(cmd).map(StringWithMacrosUtils::format));
    return this;
  }

  public GenruleBuilder setCmdExe(@Nullable String cmdExe) {
    getArgForPopulating().setCmdExe(Optional.ofNullable(cmdExe).map(StringWithMacrosUtils::format));
    return this;
  }

  public GenruleBuilder setType(@Nullable String type) {
    getArgForPopulating().setType(Optional.ofNullable(type));
    return this;
  }

  public GenruleBuilder setSrcs(@Nullable ImmutableList<SourcePath> srcs) {
    return setSrcs(
        SourceSet.ofUnnamedSources(
            ImmutableSortedSet.copyOf(Optional.ofNullable(srcs).orElse(ImmutableList.of()))));
  }

  public GenruleBuilder setSrcs(SourceSet srcs) {
    getArgForPopulating().setSrcs(srcs);
    return this;
  }

  public GenruleBuilder setCacheable(@Nullable Boolean isCacheable) {
    getArgForPopulating().setCacheable(Optional.ofNullable(isCacheable));
    return this;
  }

  public GenruleBuilder setRemote(@Nullable Boolean remote) {
    getArgForPopulating().setRemote(Optional.ofNullable(remote));
    return this;
  }

  public GenruleBuilder setNeedAndroidTools(boolean needAndroidTools) {
    getArgForPopulating().setNeedAndroidTools(needAndroidTools);
    return this;
  }

  public GenruleBuilder setEnvironmentExpansionSeparator(
      @Nullable String environmentExpansionSeparator) {
    getArgForPopulating()
        .setEnvironmentExpansionSeparator(Optional.ofNullable(environmentExpansionSeparator));
    return this;
  }
}
