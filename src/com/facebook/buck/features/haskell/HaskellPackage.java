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

package com.facebook.buck.features.haskell;

import com.facebook.buck.core.rulekey.AddToRuleKey;
import com.facebook.buck.core.rulekey.AddsToRuleKey;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.SourcePathRuleFinder;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.util.immutables.BuckStyleValueWithBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.stream.Stream;
import org.immutables.value.Value;

/** Represents a Haskell package used as a dependency during compilation. */
@BuckStyleValueWithBuilder
abstract class HaskellPackage implements AddsToRuleKey {

  /** @return the package identifying information (i.e. name, version, identifier). */
  @AddToRuleKey
  public abstract HaskellPackageInfo getInfo();

  /** @return the path to the package DB. */
  @AddToRuleKey
  protected abstract SourcePath getPackageDb();

  /** @return the path to all libraries included in the package. */
  @AddToRuleKey
  @Value.NaturalOrder
  protected abstract ImmutableSortedSet<SourcePath> getLibraries();

  /** @return the path to all interface directories included in the package. */
  @AddToRuleKey
  @Value.NaturalOrder
  protected abstract ImmutableSortedSet<SourcePath> getInterfaces();

  /** @return the path to all interface directories included in the package. */
  @Value.NaturalOrder
  protected abstract ImmutableSortedSet<SourcePath> getObjects();

  /** @return all dependencies required to use this package at build time. */
  public final Stream<BuildRule> getDeps(SourcePathRuleFinder ruleFinder) {
    return Stream.of(ImmutableList.of(getPackageDb()), getLibraries(), getInterfaces())
        .flatMap(input -> ruleFinder.filterBuildRuleInputs(input).stream());
  }

  public final String getIdentifier() {
    return getInfo().getIdentifier();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableHaskellPackage.Builder {}
}
