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

package com.facebook.buck.cxx;

import com.facebook.buck.core.rulekey.AddToRuleKey;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.rules.args.Arg;
import com.facebook.buck.rules.args.HasSourcePath;
import com.google.common.collect.ImmutableList;
import java.util.function.Consumer;

/**
 * An {@link Arg} implementation that represents a thin archive. As opposed to normal archives, thin
 * archives also need to propagate their inputs as build time deps to consumers, since they only
 * embed relative paths to the inputs, which need to exist at build time.
 */
@BuckStyleValue
abstract class ThinArchiveArg implements Arg, HasSourcePath {

  @Override
  @AddToRuleKey
  public abstract SourcePath getPath();

  @AddToRuleKey
  protected abstract ImmutableList<SourcePath> getContents();

  @Override
  public void appendToCommandLine(
      Consumer<String> consumer, SourcePathResolverAdapter pathResolver) {
    consumer.accept(pathResolver.getAbsolutePath(getPath()).toString());
  }
}
