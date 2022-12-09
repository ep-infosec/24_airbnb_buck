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

package com.facebook.buck.core.rules.impl;

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.rules.TestBuildRuleParams;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import org.hamcrest.Matchers;
import org.junit.Test;

public class AbstractBuildRuleWithDeclaredAndExtraDepsTest {

  @Test
  public void getTypeForAnonymousClass() {
    BuildTarget buildTarget = BuildTargetFactory.newInstance("//:rule");
    NoopBuildRuleWithDeclaredAndExtraDeps noopBuildRule =
        new NoopBuildRuleWithDeclaredAndExtraDeps(
            buildTarget, new FakeProjectFilesystem(), TestBuildRuleParams.create()) {};
    assertThat(
        noopBuildRule.getType(), Matchers.equalTo("noop_build_rule_with_declared_and_extra_deps"));
  }
}
