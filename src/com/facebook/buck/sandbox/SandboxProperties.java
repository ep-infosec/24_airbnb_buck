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

package com.facebook.buck.sandbox;

import com.facebook.buck.core.util.immutables.BuckStyleValueWithBuilder;
import com.google.common.collect.ImmutableSet;

/** A set of properties for a single execution of a process under a sandbox. */
@BuckStyleValueWithBuilder
public abstract class SandboxProperties {

  /** Paths that are not allowed to be read during process execution. */
  public abstract ImmutableSet<String> getDeniedToReadPaths();

  /** Paths that are allowed to be read during process execution. */
  public abstract ImmutableSet<String> getAllowedToReadPaths();

  /** Paths which metadata is allowed to be read during process execution. */
  public abstract ImmutableSet<String> getAllowedToReadMetadataPaths();

  /** Paths that are allowed to be writen during process execution. */
  public abstract ImmutableSet<String> getAllowedToWritePaths();

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableSandboxProperties.Builder {}
}
