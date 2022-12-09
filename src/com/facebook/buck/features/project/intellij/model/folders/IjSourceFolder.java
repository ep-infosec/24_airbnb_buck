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

package com.facebook.buck.features.project.intellij.model.folders;

import com.facebook.buck.core.util.immutables.BuckStyleValue;
import java.nio.file.Path;
import javax.annotation.Nullable;

@BuckStyleValue
public abstract class IjSourceFolder implements Comparable<IjSourceFolder> {
  public abstract String getType();

  public abstract String getUrl();

  public abstract Path getPath();

  public abstract boolean getIsTestSource();

  public abstract boolean getIsResourceFolder();

  public abstract IjResourceFolderType getIjResourceFolderType();

  @Nullable
  public abstract Path getRelativeOutputPath();

  @Nullable
  public abstract String getPackagePrefix();

  @Override
  public int compareTo(IjSourceFolder o) {
    if (this == o) {
      return 0;
    }

    return getUrl().compareTo(o.getUrl());
  }

  public static ImmutableIjSourceFolder of(
      String type,
      String url,
      Path path,
      boolean isTestSource,
      boolean isResourceFolder,
      IjResourceFolderType ijResourceFolderType,
      @Nullable Path relativeOutputPath,
      @Nullable String packagePrefix) {
    return ImmutableIjSourceFolder.of(
        type,
        url,
        path,
        isTestSource,
        isResourceFolder,
        ijResourceFolderType,
        relativeOutputPath,
        packagePrefix);
  }
}
