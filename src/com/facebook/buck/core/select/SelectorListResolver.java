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

package com.facebook.buck.core.select;

import com.facebook.buck.core.exceptions.DependencyStack;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.rules.coercer.concat.Concatable;
import javax.annotation.Nullable;

/**
 * A resolver that analyzes {@link SelectorList}, evaluates selectable statements and constructs the
 * final value that is produced by concatenating selected values (the type of the elements
 * determines the process of concatenation.)
 */
public interface SelectorListResolver {

  /**
   * Resolves the elements in a given {@link SelectorList} and returns the concatenated value.
   *
   * @param <T> the type of elements stored in the provided selectable list
   * @param buildTarget the build target that contains the given list in its arguments
   * @param attributeName the name of the attribute that holds the given selector list
   * @param selectorList a list with selectable elements
   * @param concatable to be used when concatenating select items (e. g. lists or strings)
   * @param dependencyStack used to provide error stacks
   * @return an object produced by concatenating resolved elements of the given list or {@code null}
   *     if the list is resolved to an absent element
   */
  @Nullable
  <T> T resolveList(
      SelectableConfigurationContext configurationContext,
      BuildTarget buildTarget,
      String attributeName,
      SelectorList<T> selectorList,
      Concatable<T> concatable,
      DependencyStack dependencyStack);
}
