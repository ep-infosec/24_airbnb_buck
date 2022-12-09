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

import static org.junit.Assert.assertEquals;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.model.targetgraph.TestBuildRuleCreationContextFactory;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.TestBuildRuleParams;
import com.facebook.buck.core.rules.impl.FakeBuildRule;
import com.facebook.buck.core.rules.resolver.impl.TestActionGraphBuilder;
import com.facebook.buck.core.sourcepath.ExplicitBuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Paths;
import org.junit.Test;

public class AndroidManifestDescriptionTest {

  @Test
  public void testGeneratedSkeletonAppearsInDeps() {
    ActionGraphBuilder graphBuilder = new TestActionGraphBuilder();

    BuildRule ruleWithOutput =
        new FakeBuildRule(BuildTargetFactory.newInstance("//foo:bar")) {
          @Override
          public SourcePath getSourcePathToOutput() {
            return ExplicitBuildTargetSourcePath.of(
                getBuildTarget(), Paths.get("buck-out/gen/foo/bar/AndroidManifest.xml"));
          }
        };
    SourcePath skeleton = ruleWithOutput.getSourcePathToOutput();
    graphBuilder.addToIndex(ruleWithOutput);

    AndroidManifestDescriptionArg arg =
        AndroidManifestDescriptionArg.builder().setName("baz").setSkeleton(skeleton).build();

    ProjectFilesystem projectFilesystem = new FakeProjectFilesystem();
    BuildTarget buildTarget = BuildTargetFactory.newInstance("//foo:baz");
    BuildRuleParams params =
        TestBuildRuleParams.create().withDeclaredDeps(graphBuilder.getAllRules(arg.getDeps()));
    BuildRule androidManifest =
        new AndroidManifestDescription(new AndroidManifestFactory())
            .createBuildRule(
                TestBuildRuleCreationContextFactory.create(graphBuilder, projectFilesystem),
                buildTarget,
                params,
                arg);

    assertEquals(ImmutableSortedSet.of(ruleWithOutput), androidManifest.getBuildDeps());
  }
}
