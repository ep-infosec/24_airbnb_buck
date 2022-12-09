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

package com.facebook.buck.features.python;

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.config.FakeBuckConfig;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.model.targetgraph.TargetGraphFactory;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.resolver.impl.TestActionGraphBuilder;
import com.facebook.buck.core.sourcepath.FakeSourcePath;
import com.facebook.buck.core.sourcepath.SourceWithFlags;
import com.facebook.buck.cxx.CxxLibraryBuilder;
import com.facebook.buck.cxx.toolchain.CxxPlatformUtils;
import com.facebook.buck.cxx.toolchain.nativelink.NativeLinkStrategy;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;

public class PrebuiltPythonLibraryDescriptionTest {

  @Test
  public void excludingTransitiveNativeDepsUsingMergedNativeLinkStrategy() throws IOException {
    CxxLibraryBuilder cxxDepBuilder =
        new CxxLibraryBuilder(BuildTargetFactory.newInstance("//:dep"))
            .setSrcs(ImmutableSortedSet.of(SourceWithFlags.of(FakeSourcePath.of("dep.c"))));
    CxxLibraryBuilder cxxBuilder =
        new CxxLibraryBuilder(BuildTargetFactory.newInstance("//:cxx"))
            .setSrcs(ImmutableSortedSet.of(SourceWithFlags.of(FakeSourcePath.of("cxx.c"))))
            .setDeps(ImmutableSortedSet.of(cxxDepBuilder.getTarget()));
    PrebuiltPythonLibraryBuilder libBuilder =
        PrebuiltPythonLibraryBuilder.createBuilder(BuildTargetFactory.newInstance("//:lib"))
            .setDeps(ImmutableSortedSet.of(cxxBuilder.getTarget()))
            .setBinarySrc(FakeSourcePath.of("test.whl"))
            .setExcludeDepsFromMergedLinking(true);

    PythonBuckConfig config =
        new PythonBuckConfig(FakeBuckConfig.builder().build()) {
          @Override
          public NativeLinkStrategy getNativeLinkStrategy() {
            return NativeLinkStrategy.MERGED;
          }
        };
    PythonBinaryBuilder binaryBuilder =
        PythonBinaryBuilder.create(
            BuildTargetFactory.newInstance("//:bin"), config, PythonTestUtils.PYTHON_PLATFORMS);
    binaryBuilder.setMainModule("main");
    binaryBuilder.setDeps(ImmutableSortedSet.of(libBuilder.getTarget()));

    ActionGraphBuilder graphBuilder =
        new TestActionGraphBuilder(
            TargetGraphFactory.newInstance(
                cxxDepBuilder.build(),
                cxxBuilder.build(),
                libBuilder.build(),
                binaryBuilder.build()));
    cxxDepBuilder.build(graphBuilder);
    cxxBuilder.build(graphBuilder);
    libBuilder.build(graphBuilder);
    PythonBinary binary = binaryBuilder.build(graphBuilder);
    assertThat(
        Iterables.transform(
            binary
                .getComponents()
                .resolve(graphBuilder.getSourcePathResolver())
                .getAllNativeLibraries()
                .keySet(),
            Object::toString),
        Matchers.containsInAnyOrder("libdep.so", "libcxx.so"));
  }

  @Test
  public void compileOptOut() {
    PrebuiltPythonLibraryBuilder libBuilder =
        PrebuiltPythonLibraryBuilder.createBuilder(BuildTargetFactory.newInstance("//:lib"))
            .setBinarySrc(FakeSourcePath.of("test.whl"))
            .setCompile(false);
    ActionGraphBuilder graphBuilder =
        new TestActionGraphBuilder(TargetGraphFactory.newInstance(libBuilder.build()));
    PrebuiltPythonLibrary library =
        (PrebuiltPythonLibrary) graphBuilder.requireRule(libBuilder.getTarget());
    assertThat(
        library.getPythonBytecode(
            PythonTestUtils.PYTHON_PLATFORM, CxxPlatformUtils.DEFAULT_PLATFORM, graphBuilder),
        Matchers.equalTo(Optional.empty()));
  }
}
