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

package com.facebook.buck.artifact_cache;

import static org.junit.Assert.assertTrue;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.rulekey.RuleKey;
import com.facebook.buck.core.rules.impl.FakeBuildRule;
import com.facebook.buck.event.BuckEventBusForTests;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;

public class ArtifactUploaderIntegrationTest {

  private static final String RULE_KEY = Strings.repeat("a", 40);

  private static final BuildTarget BUILD_TARGET = BuildTargetFactory.newInstance("//foo:bar");

  @Test
  public void testPerformUploadToArtifactCache() throws IOException {
    Path cacheDir = Files.createTempDirectory("root");
    ArtifactCache artifactCache =
        TestArtifactCaches.createDirCacheForTest(cacheDir, Paths.get("cache"));

    ArtifactUploader.performUploadToArtifactCache(
        ImmutableSet.of(new RuleKey(RULE_KEY)),
        artifactCache,
        BuckEventBusForTests.newInstance(),
        ImmutableMap.of(),
        ImmutableSortedSet.of(),
        new FakeBuildRule(BUILD_TARGET, ImmutableSortedSet.of()),
        1000);

    assertTrue(
        cacheDir
            .resolve(
                DirArtifactCacheTestUtil.getPathForRuleKey(
                    artifactCache, new RuleKey(RULE_KEY), Optional.empty()))
            .toFile()
            .exists());
  }
}
