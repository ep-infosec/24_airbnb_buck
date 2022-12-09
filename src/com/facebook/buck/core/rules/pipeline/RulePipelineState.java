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

package com.facebook.buck.core.rules.pipeline;

/**
 * When {@link com.facebook.buck.core.rules.pipeline.SupportsPipelining} rules are built locally,
 * each is given an instance of a class that implements this interface. The rules in the pipeline
 * share build state through that object.
 */
public interface RulePipelineState extends AutoCloseable {
  /**
   * Called after the pipeline is done (either through success or failure partway through) to
   * release resources.
   */
  @Override
  void close();
}
