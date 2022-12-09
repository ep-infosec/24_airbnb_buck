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

package com.facebook.buck.core.model.actiongraph;

import com.facebook.buck.core.rules.BuildRule;
import com.google.common.collect.Iterables;

public class ActionGraph {

  private final Iterable<BuildRule> nodes;

  public ActionGraph(Iterable<BuildRule> nodes) {
    this.nodes = nodes;
  }

  /** NOTE: This may contain duplicate nodes. */
  public Iterable<BuildRule> getNodes() {
    return nodes;
  }

  @Override
  public String toString() {
    return Iterables.toString(nodes);
  }

  public int getSize() {
    return Iterables.size(nodes);
  }
}
