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

package com.facebook.buck.core.description.attr;

import com.facebook.buck.core.cell.nameresolver.CellNameResolver;
import com.facebook.buck.core.model.BuildTarget;
import com.google.common.collect.ImmutableCollection;

/**
 * While building up the target graph, we infer the implicit dependencies of a rule by parsing all
 * parameters with types {@link com.facebook.buck.core.sourcepath.SourcePath} or {@link
 * com.facebook.buck.core.rules.BuildRule}. However, in some cases like {@link
 * com.facebook.buck.shell.GenruleDescription}, the {@link
 * com.facebook.buck.shell.AbstractGenruleDescription.CommonArg#getCmd} argument contains build
 * targets in a specific format. Any {@link com.facebook.buck.core.description.Description} that
 * implements this interface can modify its implicit deps by poking at the raw build rule params.
 */
public interface ImplicitDepsInferringDescription<T> {

  void findDepsForTargetFromConstructorArgs(
      BuildTarget buildTarget,
      CellNameResolver cellRoots,
      T constructorArg,
      ImmutableCollection.Builder<BuildTarget> extraDepsBuilder,
      ImmutableCollection.Builder<BuildTarget> targetGraphOnlyDepsBuilder);
}
