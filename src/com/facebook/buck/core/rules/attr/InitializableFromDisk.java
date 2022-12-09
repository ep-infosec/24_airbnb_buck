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

import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import java.io.IOException;

/**
 * Object that has in-memory data structures that need to be populated as a result of executing its
 * steps. An object that implements this interface will <em>always</em> have its internal data
 * structures initialized through this interface, regardless of whether it was built locally or read
 * from cache. This ensures that a rule is always in the same state once {@code isRuleBuilt()}
 * returns {@code true}.
 *
 * <p>Objects that implement this interface should create getter methods that delegate to {@link
 * BuildOutputInitializer#getBuildOutput()} to access the in-memory data structures rather than have
 * clients invoke {@link BuildOutputInitializer#getBuildOutput()} directly. This ensures that all
 * getters go through any protections provided by {@link BuildOutputInitializer#getBuildOutput()}.
 *
 * <p>
 */
public interface InitializableFromDisk<T> {

  // WARNING: It's difficult to use this class correctly and you are probably better off propagating
  // information to dependents through files. We'll probably get rid of this in the future.

  /**
   * @return an object that has the in-memory data structures that need to be populated as a result
   *     of executing this object's steps.
   */
  T initializeFromDisk(SourcePathResolverAdapter pathResolver) throws IOException;

  BuildOutputInitializer<T> getBuildOutputInitializer();

  default void invalidateInitializeFromDiskState() {}
}
