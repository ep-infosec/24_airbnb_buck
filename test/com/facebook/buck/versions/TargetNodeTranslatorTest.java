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

package com.facebook.buck.versions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.facebook.buck.core.cell.CellPathResolver;
import com.facebook.buck.core.cell.TestCellBuilder;
import com.facebook.buck.core.cell.TestCellPathResolver;
import com.facebook.buck.core.cell.nameresolver.CellNameResolver;
import com.facebook.buck.core.model.BaseName;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.model.targetgraph.TargetNode;
import com.facebook.buck.core.sourcepath.DefaultBuildTargetSourcePath;
import com.facebook.buck.core.sourcepath.SourceWithFlags;
import com.facebook.buck.cxx.CxxLibraryBuilder;
import com.facebook.buck.cxx.CxxLibraryDescriptionArg;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.facebook.buck.rules.coercer.DefaultTypeCoercerFactory;
import com.facebook.buck.util.types.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;

public class TargetNodeTranslatorTest {

  private static final CellPathResolver CELL_PATH_RESOLVER =
      TestCellPathResolver.get(new FakeProjectFilesystem());

  @Test
  public void translate() {
    BuildTarget a = BuildTargetFactory.newInstance("//:a");
    BuildTarget b = BuildTargetFactory.newInstance("//:b");
    BuildTarget c = BuildTargetFactory.newInstance("//:c");
    BuildTarget d = BuildTargetFactory.newInstance("//:d");
    TargetNode<CxxLibraryDescriptionArg> node =
        new CxxLibraryBuilder(a)
            .setDeps(ImmutableSortedSet.of(b))
            .setExportedDeps(ImmutableSortedSet.of(c))
            .build();
    TargetNodeTranslator translator =
        new TargetNodeTranslator(
            new DefaultTypeCoercerFactory(), ImmutableList.of(), new TestCellBuilder().build()) {
          @Override
          public Optional<BuildTarget> translateBuildTarget(BuildTarget target) {
            return Optional.of(d);
          }

          @Override
          public Optional<ImmutableMap<BuildTarget, Version>> getSelectedVersions(
              BuildTarget target) {
            return Optional.empty();
          }
        };
    Optional<TargetNode<CxxLibraryDescriptionArg>> translated = translator.translateNode(node);
    assertThat(translated.get().getBuildTarget(), Matchers.equalTo(d));
    assertThat(translated.get().getDeclaredDeps(), Matchers.equalTo(ImmutableSet.of(d)));
    assertThat(translated.get().getExtraDeps(), Matchers.equalTo(ImmutableSet.of(d)));
    assertThat(
        translated.get().getConstructorArg().getDeps(), Matchers.equalTo(ImmutableSortedSet.of(d)));
    assertThat(
        translated.get().getConstructorArg().getExportedDeps(),
        Matchers.equalTo(ImmutableSortedSet.of(d)));
  }

  @Test
  public void noTranslate() {
    BuildTarget a = BuildTargetFactory.newInstance("//:a");
    BuildTarget b = BuildTargetFactory.newInstance("//:b");
    BuildTarget c = BuildTargetFactory.newInstance("//:c");
    TargetNode<CxxLibraryDescriptionArg> node =
        new CxxLibraryBuilder(a)
            .setDeps(ImmutableSortedSet.of(b))
            .setExportedDeps(ImmutableSortedSet.of(c))
            .build();
    TargetNodeTranslator translator =
        new TargetNodeTranslator(
            new DefaultTypeCoercerFactory(), ImmutableList.of(), new TestCellBuilder().build()) {
          @Override
          public Optional<BuildTarget> translateBuildTarget(BuildTarget target) {
            return Optional.empty();
          }

          @Override
          public Optional<ImmutableMap<BuildTarget, Version>> getSelectedVersions(
              BuildTarget target) {
            return Optional.empty();
          }
        };
    Optional<TargetNode<CxxLibraryDescriptionArg>> translated = translator.translateNode(node);
    assertFalse(translated.isPresent());
  }

  @Test
  public void selectedVersions() {
    TargetNode<VersionPropagatorDescriptionArg> node = new VersionPropagatorBuilder("//:a").build();
    ImmutableMap<BuildTarget, Version> selectedVersions =
        ImmutableMap.of(BuildTargetFactory.newInstance("//:b"), Version.of("1.0"));
    TargetNodeTranslator translator =
        new TargetNodeTranslator(
            new DefaultTypeCoercerFactory(), ImmutableList.of(), new TestCellBuilder().build()) {
          @Override
          public Optional<BuildTarget> translateBuildTarget(BuildTarget target) {
            return Optional.empty();
          }

          @Override
          public Optional<ImmutableMap<BuildTarget, Version>> getSelectedVersions(
              BuildTarget target) {
            return Optional.of(selectedVersions);
          }
        };
    Optional<TargetNode<VersionPropagatorDescriptionArg>> translated =
        translator.translateNode(node);
    assertTrue(translated.isPresent());
    assertThat(
        translated.get().getSelectedVersions(), Matchers.equalTo(Optional.of(selectedVersions)));
  }

  @Test
  public void translatePair() {
    BuildTarget a = BuildTargetFactory.newInstance("//:a");
    BuildTarget b = BuildTargetFactory.newInstance("//:b");
    TargetNodeTranslator translator =
        new TargetNodeTranslator(
            new DefaultTypeCoercerFactory(), ImmutableList.of(), new TestCellBuilder().build()) {
          @Override
          public Optional<BuildTarget> translateBuildTarget(BuildTarget target) {
            return Optional.of(b);
          }

          @Override
          public Optional<ImmutableMap<BuildTarget, Version>> getSelectedVersions(
              BuildTarget target) {
            return Optional.empty();
          }
        };
    assertThat(
        translator.translatePair(
            CELL_PATH_RESOLVER.getCellNameResolver(), BaseName.ROOT, new Pair<>("hello", a)),
        Matchers.equalTo(Optional.of(new Pair<>("hello", b))));
  }

  @Test
  public void translateBuildTargetSourcePath() {
    BuildTarget a = BuildTargetFactory.newInstance("//:a");
    BuildTarget b = BuildTargetFactory.newInstance("//:b");
    TargetNodeTranslator translator =
        new TargetNodeTranslator(
            new DefaultTypeCoercerFactory(), ImmutableList.of(), new TestCellBuilder().build()) {
          @Override
          public Optional<BuildTarget> translateBuildTarget(BuildTarget target) {
            return Optional.of(b);
          }

          @Override
          public Optional<ImmutableMap<BuildTarget, Version>> getSelectedVersions(
              BuildTarget target) {
            return Optional.empty();
          }
        };
    assertThat(
        translator.translateBuildTargetSourcePath(
            CELL_PATH_RESOLVER.getCellNameResolver(),
            BaseName.ROOT,
            DefaultBuildTargetSourcePath.of(a)),
        Matchers.equalTo(Optional.of(DefaultBuildTargetSourcePath.of(b))));
  }

  @Test
  public void translateSourceWithFlags() {
    BuildTarget a = BuildTargetFactory.newInstance("//:a");
    BuildTarget b = BuildTargetFactory.newInstance("//:b");
    TargetNodeTranslator translator =
        new TargetNodeTranslator(
            new DefaultTypeCoercerFactory(), ImmutableList.of(), new TestCellBuilder().build()) {
          @Override
          public Optional<BuildTarget> translateBuildTarget(BuildTarget target) {
            return Optional.of(b);
          }

          @Override
          public Optional<ImmutableMap<BuildTarget, Version>> getSelectedVersions(
              BuildTarget target) {
            return Optional.empty();
          }
        };
    assertThat(
        translator.translateSourceWithFlags(
            CELL_PATH_RESOLVER.getCellNameResolver(),
            BaseName.ROOT,
            SourceWithFlags.of(DefaultBuildTargetSourcePath.of(a), ImmutableList.of("-flag"))),
        Matchers.equalTo(
            Optional.of(
                SourceWithFlags.of(
                    DefaultBuildTargetSourcePath.of(b), ImmutableList.of("-flag")))));
  }

  @Test
  public void translateTargetTranslator() {
    TargetTranslator<Integer> integerTranslator =
        new TargetTranslator<Integer>() {
          @Override
          public Class<Integer> getTranslatableClass() {
            return Integer.class;
          }

          @Override
          public Optional<Integer> translateTargets(
              CellNameResolver cellPathResolver,
              BaseName targetBaseName,
              TargetNodeTranslator translator,
              Integer val) {
            return Optional.of(0);
          }
        };
    TargetNodeTranslator translator =
        new FixedTargetNodeTranslator(
            new DefaultTypeCoercerFactory(),
            ImmutableList.of(integerTranslator),
            ImmutableMap.of(),
            new TestCellBuilder().build());
    assertThat(
        translator.translate(CELL_PATH_RESOLVER.getCellNameResolver(), BaseName.ROOT, 12),
        Matchers.equalTo(Optional.of(0)));
  }
}
