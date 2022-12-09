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

package com.facebook.buck.android;

import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.XzStep;
import com.facebook.buck.step.fs.ZstdStep;
import java.nio.file.Path;

/** Creates a step with specific compression algorithm */
class CompressionAlgorithmCreator {

  static Step createCompressionStep(
      CompressionAlgorithm compressionAlgorithm,
      ProjectFilesystem projectFilesystem,
      Path libOutputBlob,
      Path libSubdirectory,
      int compressionLevel) {
    switch (compressionAlgorithm) {
      case XZ:
        return new XzStep(
            projectFilesystem,
            libOutputBlob,
            libSubdirectory.resolve("libs.xzs"),
            compressionLevel);
      case ZSTD:
        return new ZstdStep(projectFilesystem, libOutputBlob, libSubdirectory.resolve("libs.zstd"));
    }
    throw new IllegalArgumentException("Unknown compression algorithm: " + compressionAlgorithm);
  }
}
