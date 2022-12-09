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

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.util.sha1.Sha1HashCode;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import java.util.Comparator;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Object that represents a {@code .dex.jar} file that knows what {@code .class} files went into it,
 * as well as its estimated dex weight.
 */
public interface DexWithClasses {
  @Nullable
  BuildTarget getSourceBuildTarget();

  /** @return path from the project root where the {@code .dex.jar} file can be found. */
  SourcePath getSourcePathToDexFile();

  /** @return the names of the {@code .class} files that went into the DEX file. */
  ImmutableSet<String> getClassNames();

  /** @return a hash of the {@code .class} files that went into the DEX file. */
  Sha1HashCode getClassesHash();

  /**
   * @return A value that estimates how much space the Dalvik code represented by this object will
   *     take up in a DEX file. The units for this estimate are not important, so long as they are
   *     consistent with those used by {@link PreDexedFilesSorter} to determine how secondary DEX
   *     files should be packed.
   */
  int getWeightEstimate();

  Function<DexProducedFromJavaLibrary, DexWithClasses> TO_DEX_WITH_CLASSES =
      new Function<DexProducedFromJavaLibrary, DexWithClasses>() {
        @Override
        @Nullable
        public DexWithClasses apply(DexProducedFromJavaLibrary preDex) {
          if (!preDex.hasOutput()) {
            return null;
          }

          SourcePath sourcePathToDex = preDex.getSourcePathToDex();
          ImmutableSet<String> classNames = preDex.getClassNamesToHashes().keySet();
          Sha1HashCode classesHash =
              Sha1HashCode.fromHashCode(
                  Hashing.combineOrdered(preDex.getClassNamesToHashes().values()));
          int weightEstimate = preDex.getWeightEstimate();
          return new DexWithClasses() {
            @Override
            public BuildTarget getSourceBuildTarget() {
              return preDex.getBuildTarget();
            }

            @Override
            public SourcePath getSourcePathToDexFile() {
              return sourcePathToDex;
            }

            @Override
            public ImmutableSet<String> getClassNames() {
              return classNames;
            }

            @Override
            public Sha1HashCode getClassesHash() {
              return classesHash;
            }

            @Override
            public int getWeightEstimate() {
              return weightEstimate;
            }
          };
        }
      };

  Comparator<DexWithClasses> DEX_WITH_CLASSES_COMPARATOR =
      Comparator.comparing(DexWithClasses::getSourcePathToDexFile);
}
