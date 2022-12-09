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

package com.facebook.buck.features.lua;

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.model.targetgraph.TargetGraphFactory;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.resolver.impl.TestActionGraphBuilder;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.cxx.toolchain.CxxPlatformUtils;
import com.facebook.buck.rules.args.Arg;
import com.facebook.buck.rules.macros.StringWithMacrosUtils;
import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import org.hamcrest.Matchers;
import org.junit.Test;

public class CxxLuaExtensionDescriptionTest {

  @Test
  public void baseModule() {
    CxxLuaExtensionBuilder builder =
        new CxxLuaExtensionBuilder(BuildTargetFactory.newInstance("//:rule"))
            .setBaseModule("hello.world");
    ActionGraphBuilder graphBuilder =
        new TestActionGraphBuilder(TargetGraphFactory.newInstance(builder.build()));
    CxxLuaExtension extension = builder.build(graphBuilder);
    assertThat(
        Paths.get(extension.getModule(CxxPlatformUtils.DEFAULT_PLATFORM)),
        Matchers.equalTo(Paths.get("hello/world/rule.so")));
  }

  @Test
  public void nativeLinkTargetInput() {
    CxxLuaExtensionBuilder builder =
        new CxxLuaExtensionBuilder(BuildTargetFactory.newInstance("//:rule"));
    builder.setLinkerFlags(ImmutableList.of(StringWithMacrosUtils.format("--flag")));
    ActionGraphBuilder graphBuilder =
        new TestActionGraphBuilder(TargetGraphFactory.newInstance(builder.build()));
    SourcePathResolverAdapter pathResolver = graphBuilder.getSourcePathResolver();
    CxxLuaExtension rule = builder.build(graphBuilder);
    assertThat(
        Arg.stringify(
            rule.getTargetForPlatform(CxxPlatformUtils.DEFAULT_PLATFORM, true)
                .getNativeLinkTargetInput(graphBuilder, pathResolver)
                .getArgs(),
            pathResolver),
        Matchers.hasItems("--flag"));
    assertThat(
        Arg.stringify(
            rule.getTargetForPlatform(CxxPlatformUtils.DEFAULT_PLATFORM, false)
                .getNativeLinkTargetInput(graphBuilder, pathResolver)
                .getArgs(),
            pathResolver),
        Matchers.not(Matchers.hasItems("--flag")));
  }
}
