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

package com.facebook.buck.features.apple.common;

import com.facebook.buck.core.util.immutables.BuckStyleValue;
import java.nio.file.Path;

@BuckStyleValue
public abstract class CopyInXcode {

  public static CopyInXcode of(
      CopyInXcode.SourceType sourceType,
      Path sourcePath,
      CopyInXcode.DestinationBase destinationBase,
      Path destinationPath) {
    return ImmutableCopyInXcode.of(sourceType, sourcePath, destinationBase, destinationPath);
  }

  public enum SourceType {
    FILE,
    FOLDER_CONTENTS,
  }

  public enum DestinationBase {
    UNLOCALIZED_RESOURCES,
    TEMPDIR,
  }

  public abstract SourceType getSourceType();

  public abstract Path getSourcePath();

  public abstract DestinationBase getDestinationBase();

  public abstract Path getDestinationPath();
}
