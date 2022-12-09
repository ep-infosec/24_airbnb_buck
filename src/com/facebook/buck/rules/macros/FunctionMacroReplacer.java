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

package com.facebook.buck.rules.macros;

import com.facebook.buck.core.macros.MacroMatchResult;
import com.facebook.buck.core.macros.MacroReplacer;
import com.google.common.collect.ImmutableList;
import java.util.function.Function;

/** A @{link MacroReplacer} wrapping a @{link Function}. */
public class FunctionMacroReplacer<T> implements MacroReplacer<T> {

  private final Function<ImmutableList<String>, T> function;

  public FunctionMacroReplacer(Function<ImmutableList<String>, T> function) {
    this.function = function;
  }

  @Override
  public T replace(MacroMatchResult input) {
    return function.apply(input.getMacroInput());
  }
}
