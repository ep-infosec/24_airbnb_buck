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

package com.facebook.buck.core.rules.attr;

import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.SourcePathRuleFinder;
import java.util.stream.Stream;

/**
 * Deriving deps directly from the @AddToRuleKey annotated fields of some objects doesn't work
 * correctly or is too slow. When deriving deps from an object that implements HasCustomDepsLogic,
 * the framework (BuildableSupport/ModernBuildRule) will call getDeps() instead of deriving
 * from @AddToRuleKey fields.
 *
 * <p>This is primarily meant to ease the transition to a state where all deps are derived
 * automatically.
 */
@Deprecated
public interface HasCustomDepsLogic {
  Stream<BuildRule> getDeps(SourcePathRuleFinder ruleFinder);
}
