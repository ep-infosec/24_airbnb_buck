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

import static org.junit.Assert.assertEquals;

import com.facebook.buck.core.build.buildable.context.FakeBuildableContext;
import com.facebook.buck.core.build.context.BuildContext;
import com.facebook.buck.core.build.context.FakeBuildContext;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.resolver.impl.TestActionGraphBuilder;
import com.facebook.buck.core.sourcepath.FakeSourcePath;
import com.facebook.buck.parser.exceptions.NoSuchBuildTargetException;
import com.facebook.buck.step.Step;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

public class KeystoreTest {

  private static Keystore createKeystoreRuleForTest() throws NoSuchBuildTargetException {
    ActionGraphBuilder graphBuilder = new TestActionGraphBuilder();
    return KeystoreBuilder.createBuilder(BuildTargetFactory.newInstance("//keystores:debug"))
        .setStore(FakeSourcePath.of("keystores/debug.keystore"))
        .setProperties(FakeSourcePath.of("keystores/debug.keystore.properties"))
        .build(graphBuilder);
  }

  @Test
  public void testObservers() {
    Keystore keystore = createKeystoreRuleForTest();
    assertEquals("keystore", keystore.getType());

    assertEquals(FakeSourcePath.of("keystores/debug.keystore"), keystore.getPathToStore());
    assertEquals(
        FakeSourcePath.of("keystores/debug.keystore.properties"),
        keystore.getPathToPropertiesFile());
  }

  @Test
  public void testBuildInternal() {
    BuildContext buildContext = FakeBuildContext.NOOP_CONTEXT;

    Keystore keystore = createKeystoreRuleForTest();
    List<Step> buildSteps = keystore.getBuildSteps(buildContext, new FakeBuildableContext());
    assertEquals(ImmutableList.<Step>of(), buildSteps);
  }
}
