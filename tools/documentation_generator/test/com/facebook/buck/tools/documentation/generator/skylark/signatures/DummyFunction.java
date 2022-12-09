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

package com.facebook.buck.tools.documentation.generator.skylark.signatures;

import com.google.devtools.build.lib.skylarkinterface.Param;
import com.google.devtools.build.lib.skylarkinterface.SkylarkCallable;
import com.google.devtools.build.lib.syntax.SkylarkList;
import java.util.Collections;

public class DummyFunction {

  @SkylarkCallable(
      name = "dummy",
      doc = "Returns a dummy list of strings.",
      parameters = {
        @Param(name = "seed", type = String.class, doc = "the first element of the returned list."),
      },
      documented = false,
      useAst = true,
      useEnvironment = true)
  public SkylarkList<String> dummy(String seed) {
    return SkylarkList.createImmutable(Collections.singleton(seed));
  }
}
