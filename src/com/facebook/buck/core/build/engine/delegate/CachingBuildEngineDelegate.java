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

package com.facebook.buck.core.build.engine.delegate;

import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.util.cache.FileHashCache;

/**
 * Functionality used in the {@link com.facebook.buck.core.build.engine.impl.CachingBuildEngine}
 * when running a distributed build.
 */
public interface CachingBuildEngineDelegate {

  FileHashCache getFileHashCache();

  /**
   * Called right before the rule is going to be built. This is when direct inputs to the rule would
   * get materialized on disk.
   *
   * @param buildRule rule that is about to be built.
   */
  void onRuleAboutToBeBuilt(BuildRule buildRule);
}
