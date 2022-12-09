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

package com.facebook.buck.apple.toolchain.impl;

import static org.junit.Assert.assertEquals;

import com.facebook.buck.apple.toolchain.AppleDeveloperDirectoryForTestsProvider;
import com.facebook.buck.apple.toolchain.AppleDeveloperDirectoryProvider;
import com.facebook.buck.core.config.BuckConfig;
import com.facebook.buck.core.config.FakeBuckConfig;
import com.facebook.buck.core.model.UnconfiguredTargetConfiguration;
import com.facebook.buck.core.toolchain.ToolchainCreationContext;
import com.facebook.buck.core.toolchain.impl.ToolchainProviderBuilder;
import com.facebook.buck.io.ExecutableFinder;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.facebook.buck.rules.keys.config.TestRuleKeyConfigurationFactory;
import com.facebook.buck.testutil.TemporaryPaths;
import com.facebook.buck.util.FakeProcessExecutor;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;

public class AppleDeveloperDirectoryForTestsProviderFactoryTest {

  @Rule public TemporaryPaths tmp = new TemporaryPaths();

  @Test
  public void testAppleDeveloperDirectoryForTests() throws IOException {
    Path developerDir = tmp.newFolder();
    BuckConfig buckConfig =
        FakeBuckConfig.builder()
            .setSections(
                ImmutableMap.of(
                    "apple", ImmutableMap.of("xcode_developer_dir", developerDir.toString())))
            .build();

    ToolchainCreationContext context =
        ToolchainCreationContext.of(
            ImmutableMap.of(),
            buckConfig,
            new FakeProjectFilesystem(),
            new FakeProcessExecutor(),
            new ExecutableFinder(),
            TestRuleKeyConfigurationFactory.create());

    AppleDeveloperDirectoryProvider appleDeveloperDirectoryProvider =
        new AppleDeveloperDirectoryProviderFactory()
            .createToolchain(
                new ToolchainProviderBuilder().build(),
                context,
                UnconfiguredTargetConfiguration.INSTANCE)
            .get();

    // Developer directory for tests should fall back to developer dir if not separately specified.
    AppleDeveloperDirectoryForTestsProvider appleDeveloperDirectoryForTestsProvider =
        new AppleDeveloperDirectoryForTestsProviderFactory()
            .createToolchain(
                new ToolchainProviderBuilder()
                    .withToolchain(
                        AppleDeveloperDirectoryProvider.DEFAULT_NAME,
                        appleDeveloperDirectoryProvider)
                    .build(),
                context,
                UnconfiguredTargetConfiguration.INSTANCE)
            .get();

    assertEquals(
        developerDir, appleDeveloperDirectoryForTestsProvider.getAppleDeveloperDirectoryForTests());
  }

  @Test
  public void getSpecifiedAppleDeveloperDirectorySupplierForTests() throws IOException {
    Path developerDir = tmp.newFolder();
    Path developerDirForTests = tmp.newFolder();
    BuckConfig buckConfig =
        FakeBuckConfig.builder()
            .setSections(
                ImmutableMap.of(
                    "apple",
                    ImmutableMap.of(
                        "xcode_developer_dir", developerDir.toString(),
                        "xcode_developer_dir_for_tests", developerDirForTests.toString())))
            .build();
    ToolchainCreationContext context =
        ToolchainCreationContext.of(
            ImmutableMap.of(),
            buckConfig,
            new FakeProjectFilesystem(),
            new FakeProcessExecutor(),
            new ExecutableFinder(),
            TestRuleKeyConfigurationFactory.create());

    AppleDeveloperDirectoryProvider appleDeveloperDirectoryProvider =
        new AppleDeveloperDirectoryProviderFactory()
            .createToolchain(
                new ToolchainProviderBuilder().build(),
                context,
                UnconfiguredTargetConfiguration.INSTANCE)
            .get();

    AppleDeveloperDirectoryForTestsProvider appleDeveloperDirectoryForTestsProvider =
        new AppleDeveloperDirectoryForTestsProviderFactory()
            .createToolchain(
                new ToolchainProviderBuilder()
                    .withToolchain(
                        AppleDeveloperDirectoryProvider.DEFAULT_NAME,
                        appleDeveloperDirectoryProvider)
                    .build(),
                context,
                UnconfiguredTargetConfiguration.INSTANCE)
            .get();

    assertEquals(
        developerDirForTests,
        appleDeveloperDirectoryForTestsProvider.getAppleDeveloperDirectoryForTests());
  }
}
