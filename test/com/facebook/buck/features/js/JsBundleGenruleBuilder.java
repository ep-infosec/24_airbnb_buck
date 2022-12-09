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

import com.facebook.buck.core.config.FakeBuckConfig;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.Flavor;
import com.facebook.buck.core.model.targetgraph.AbstractNodeBuilder;
import com.facebook.buck.core.toolchain.ToolchainProvider;
import com.facebook.buck.core.toolchain.impl.ToolchainProviderBuilder;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.rules.macros.StringWithMacros;
import com.facebook.buck.sandbox.NoSandboxExecutionStrategy;
import com.facebook.buck.util.types.Pair;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

public class JsBundleGenruleBuilder
    extends AbstractNodeBuilder<
        JsBundleGenruleDescriptionArg.Builder,
        JsBundleGenruleDescriptionArg,
        JsBundleGenruleDescription,
        JsBundleGenrule> {
  private static final JsBundleGenruleDescription genruleDescription =
      new JsBundleGenruleDescription(
          createToolchainProvider(),
          FakeBuckConfig.builder().build(),
          new NoSandboxExecutionStrategy());

  private static ToolchainProvider createToolchainProvider() {
    return new ToolchainProviderBuilder().build();
  }

  JsBundleGenruleBuilder(
      BuildTarget target, BuildTarget bundleTarget, ProjectFilesystem projectFilesystem) {
    super(genruleDescription, target, projectFilesystem);
    getArgForPopulating().setJsBundle(bundleTarget);
  }

  JsBundleGenruleBuilder(Options options, ProjectFilesystem projectFilesystem) {
    super(genruleDescription, options.genruleTarget, projectFilesystem);
    getArgForPopulating().setJsBundle(options.jsBundle);
    if (options.rewriteSourcemap) {
      getArgForPopulating().setRewriteSourcemap(true);
    }
    if (options.rewriteMisc) {
      getArgForPopulating().setRewriteMisc(true);
    }
    if (options.rewriteDepsFile) {
      getArgForPopulating().setRewriteDepsFile(true);
    }
    if (options.skipResources) {
      getArgForPopulating().setSkipResources(true);
    }
    if (options.cmd != null) {
      getArgForPopulating().setCmd(options.cmd);
    }
    if (options.bundleName != null) {
      getArgForPopulating().setBundleName(options.bundleName);
    }
    if (options.bundleNameForFlavor != null) {
      getArgForPopulating().setBundleNameForFlavor(options.bundleNameForFlavor);
    }
  }

  public static class Options {
    BuildTarget genruleTarget;
    BuildTarget jsBundle;
    boolean rewriteSourcemap = false;
    boolean rewriteMisc = false;
    boolean rewriteDepsFile = false;
    boolean skipResources = false;
    @Nullable public StringWithMacros cmd = null;
    @Nullable private String bundleName;
    @Nullable private ImmutableList<Pair<Flavor, String>> bundleNameForFlavor;

    public static Options of(BuildTarget genruleTarget, BuildTarget jsBundle) {
      return new Options(genruleTarget, jsBundle);
    }

    public Options rewriteSourcemap() {
      rewriteSourcemap = true;
      return this;
    }

    public Options rewriteMisc() {
      rewriteMisc = true;
      return this;
    }

    public Options rewriteDepsFile() {
      rewriteDepsFile = true;
      return this;
    }

    public Options skipResources() {
      skipResources = true;
      return this;
    }

    public Options setCmd(StringWithMacros cmd) {
      this.cmd = cmd;
      return this;
    }

    private Options(BuildTarget genruleTarget, BuildTarget jsBundle) {
      this.genruleTarget = genruleTarget;
      this.jsBundle = jsBundle;
    }

    public Options bundleName(String bundleName) {
      this.bundleName = bundleName;
      return this;
    }

    public Options bundleNameForFlavor(ImmutableList<Pair<Flavor, String>> bundleNameForFlavor) {
      this.bundleNameForFlavor = bundleNameForFlavor;
      return this;
    }
  }
}
