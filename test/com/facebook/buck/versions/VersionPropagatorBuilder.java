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

package com.facebook.buck.versions;

import com.facebook.buck.core.description.arg.BuildRuleArg;
import com.facebook.buck.core.description.arg.HasDeclaredDeps;
import com.facebook.buck.core.description.arg.HasTests;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.model.targetgraph.AbstractNodeBuilder;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.BuildRuleCreationContextWithTargetGraph;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.util.immutables.RuleArg;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import org.immutables.value.Value;

public class VersionPropagatorBuilder
    extends AbstractNodeBuilder<
        VersionPropagatorDescriptionArg.Builder,
        VersionPropagatorDescriptionArg,
        VersionPropagatorBuilder.VersionPropagatorDescription,
        BuildRule> {

  public VersionPropagatorBuilder(BuildTarget target) {
    super(new VersionPropagatorDescription(), target);
  }

  public VersionPropagatorBuilder(String target) {
    this(BuildTargetFactory.newInstance(target));
  }

  public VersionPropagatorBuilder setDeps(ImmutableSortedSet<BuildTarget> deps) {
    getArgForPopulating().setDeps(deps);
    return this;
  }

  public VersionPropagatorBuilder setDeps(String... deps) {
    ImmutableSortedSet.Builder<BuildTarget> builder = ImmutableSortedSet.naturalOrder();
    for (String dep : deps) {
      builder.add(BuildTargetFactory.newInstance(dep));
    }
    return setDeps(builder.build());
  }

  public VersionPropagatorBuilder setTests(ImmutableSortedSet<BuildTarget> tests) {
    getArgForPopulating().setTests(tests);
    return this;
  }

  @RuleArg
  interface AbstractVersionPropagatorDescriptionArg
      extends BuildRuleArg, HasDeclaredDeps, HasTests {
    @Value.NaturalOrder
    ImmutableSortedMap<BuildTarget, Optional<Constraint>> getVersionedDeps();
  }

  public static class VersionPropagatorDescription
      implements VersionPropagator<VersionPropagatorDescriptionArg> {

    @Override
    public Class<VersionPropagatorDescriptionArg> getConstructorArgType() {
      return VersionPropagatorDescriptionArg.class;
    }

    @Override
    public BuildRule createBuildRule(
        BuildRuleCreationContextWithTargetGraph context,
        BuildTarget buildTarget,
        BuildRuleParams params,
        VersionPropagatorDescriptionArg args) {
      throw new IllegalStateException();
    }
  }
}
