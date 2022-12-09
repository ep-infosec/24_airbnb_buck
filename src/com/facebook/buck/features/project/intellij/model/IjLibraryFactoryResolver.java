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

package com.facebook.buck.features.project.intellij.model;

import com.facebook.buck.core.model.targetgraph.TargetNode;
import com.facebook.buck.core.sourcepath.SourcePath;
import java.nio.file.Path;
import java.util.Optional;

public interface IjLibraryFactoryResolver {
  /** Does it get an absolute path? Does it get a relative path? Who knows! */
  Path getPath(SourcePath path);

  /**
   * @param targetNode node to look up.
   * @return path to the output of target but only if that path points to a .jar.
   */
  Optional<SourcePath> getPathIfJavaLibrary(TargetNode<?> targetNode);
}
