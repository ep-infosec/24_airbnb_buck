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

package com.facebook.buck.skylark.io;

import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collection;

/** Glob specification includes parameters that affect glob evaluation within a single package. */
@BuckStyleValue
@JsonDeserialize(as = ImmutableGlobSpec.class)
public abstract class GlobSpec {
  /** Wildcards of paths that should be returned. */
  @JsonProperty("include")
  public abstract Collection<String> getInclude();

  /** Wildcards of paths that should be skipped from the glob expansion. */
  @JsonProperty("exclude")
  public abstract Collection<String> getExclude();

  /** Whether directories should be excluded from the glob expansion. */
  @JsonProperty("includeDirectories")
  public abstract boolean getExcludeDirectories();

  public static GlobSpec of(
      Collection<String> include, Collection<String> exclude, boolean excludeDirectories) {
    return ImmutableGlobSpec.of(include, exclude, excludeDirectories);
  }
}
