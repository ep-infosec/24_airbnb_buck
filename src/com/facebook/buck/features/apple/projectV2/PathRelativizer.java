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

package com.facebook.buck.features.apple.projectV2;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.io.file.MorePaths;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

/**
 * Helper class to convert among various relative path-like objects.
 *
 * <p>Methods are named in the form of {@code xToY}, such that invocation of the method will take a
 * path referenced from Y to one that is referenced from X.
 */
final class PathRelativizer {

  private static final Path EMPTY_PATH = Paths.get("");
  private static final Path CURRENT_DIRECTORY = Paths.get(".");

  private final Path outputDirectory;
  private final Function<SourcePath, Path> resolver;

  public PathRelativizer(Path outputDirectory, Function<SourcePath, Path> resolver) {
    this.outputDirectory = outputDirectory;
    this.resolver = resolver;
  }

  /** Path from output directory to a build target's buck file directory. */
  public Path outputPathToBuildTargetPath(BuildTarget target) {
    return outputDirToRootRelative(
        target.getCellRelativeBasePath().getPath().toPath(outputDirectory.getFileSystem()));
  }

  /** Path from output directory to given path that's relative to the root directory. */
  public Path outputDirToRootRelative(Path path) {
    Path result = MorePaths.normalize(MorePaths.relativize(outputDirectory, path));
    if (EMPTY_PATH.equals(result)) {
      result = CURRENT_DIRECTORY;
    }
    return result;
  }

  /** Map a SourcePath to one that's relative to the output directory. */
  public Path outputPathToSourcePath(SourcePath sourcePath) {
    return outputDirToRootRelative(Objects.requireNonNull(resolver.apply(sourcePath)));
  }
}
