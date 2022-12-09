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

package com.facebook.buck.core.model.targetgraph;

import com.facebook.buck.core.exceptions.DependencyStack;
import com.facebook.buck.core.exceptions.ExceptionWithHumanReadableMessage;
import com.facebook.buck.core.model.BuildTarget;

public class NoSuchTargetException extends RuntimeException
    implements ExceptionWithHumanReadableMessage {

  private final DependencyStack dependencyStack;

  public NoSuchTargetException(DependencyStack dependencyStack, BuildTarget buildTarget) {
    super(
        String.format(
            "Required target for rule '%s' was not found in the target graph.",
            buildTarget.getFullyQualifiedName()));
    this.dependencyStack = dependencyStack;
  }

  @Override
  public String getHumanReadableErrorMessage() {
    return getMessage();
  }

  @Override
  public DependencyStack getDependencyStack() {
    return dependencyStack;
  }
}
