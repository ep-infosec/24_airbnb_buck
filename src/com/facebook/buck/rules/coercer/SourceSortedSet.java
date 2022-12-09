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

package com.facebook.buck.rules.coercer;

import com.facebook.buck.core.cell.nameresolver.CellNameResolver;
import com.facebook.buck.core.model.BaseName;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.versions.TargetNodeTranslator;
import com.facebook.buck.versions.TargetTranslatable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import org.immutables.value.Value;

/**
 * A group of sources, stored as either a {@link SortedSet} of unnamed {@link SourcePath}s or a
 * {@link java.util.SortedMap} of names to {@link SourcePath}s. Commonly used to represent the input
 * <code>srcs</code> parameter of rules where source "names" may be important (e.g. to control
 * layout of C++ headers).
 */
@BuckStyleValue
public abstract class SourceSortedSet implements TargetTranslatable<SourceSortedSet> {

  public static final SourceSortedSet EMPTY =
      SourceSortedSet.ofUnnamedSources(ImmutableSortedSet.of());

  public enum Type {
    UNNAMED,
    NAMED,
  }

  public abstract Type getType();

  public abstract Optional<ImmutableSortedSet<SourcePath>> getUnnamedSources();

  public abstract Optional<ImmutableSortedMap<String, SourcePath>> getNamedSources();

  @Value.Check
  protected void check() {
    switch (getType()) {
      case UNNAMED:
        Preconditions.checkArgument(getUnnamedSources().isPresent());
        Preconditions.checkArgument(!getNamedSources().isPresent());
        break;
      case NAMED:
        Preconditions.checkArgument(!getUnnamedSources().isPresent());
        Preconditions.checkArgument(getNamedSources().isPresent());
        break;
    }
  }

  public static SourceSortedSet ofUnnamedSources(ImmutableSortedSet<SourcePath> unnamedSources) {
    return SourceSortedSet.of(Type.UNNAMED, Optional.of(unnamedSources), Optional.empty());
  }

  public static SourceSortedSet ofNamedSources(
      ImmutableSortedMap<String, SourcePath> namedSources) {
    return SourceSortedSet.of(Type.NAMED, Optional.empty(), Optional.of(namedSources));
  }

  public boolean isEmpty() {
    switch (getType()) {
      case UNNAMED:
        return getUnnamedSources().get().isEmpty();
      case NAMED:
        return getNamedSources().get().isEmpty();
      default:
        throw new IllegalStateException("unexpected type: " + getType());
    }
  }

  public ImmutableMap<String, SourcePath> toNameMap(
      BuildTarget buildTarget,
      SourcePathResolverAdapter pathResolver,
      String parameterName,
      Predicate<SourcePath> filter,
      Function<SourcePath, SourcePath> transform) {

    ImmutableMap.Builder<String, SourcePath> sources = ImmutableMap.builder();
    switch (getType()) {
      case NAMED:
        for (Map.Entry<String, SourcePath> ent : getNamedSources().get().entrySet()) {
          if (filter.test(ent.getValue())) {
            sources.put(ent.getKey(), transform.apply(ent.getValue()));
          }
        }
        break;
      case UNNAMED:
        pathResolver
            .getSourcePathNames(
                buildTarget, parameterName, getUnnamedSources().get(), filter, transform)
            .forEach((name, path) -> sources.put(name, transform.apply(path)));
        break;
    }
    return sources.build();
  }

  public ImmutableMap<String, SourcePath> toNameMap(
      BuildTarget buildTarget, SourcePathResolverAdapter pathResolver, String parameterName) {
    return toNameMap(buildTarget, pathResolver, parameterName, x -> true, x -> x);
  }

  public ImmutableList<SourcePath> getPaths() {
    ImmutableList.Builder<SourcePath> sources = ImmutableList.builder();
    switch (getType()) {
      case NAMED:
        sources.addAll(getNamedSources().get().values());
        break;
      case UNNAMED:
        sources.addAll(getUnnamedSources().get());
        break;
    }
    return sources.build();
  }

  @Override
  public Optional<SourceSortedSet> translateTargets(
      CellNameResolver cellPathResolver, BaseName targetBaseName, TargetNodeTranslator translator) {
    Optional<Optional<ImmutableSortedMap<String, SourcePath>>> namedSources =
        translator.translate(cellPathResolver, targetBaseName, getNamedSources());
    Optional<Optional<ImmutableSortedSet<SourcePath>>> unNamedSources =
        translator.translate(cellPathResolver, targetBaseName, getUnnamedSources());
    if (!namedSources.isPresent() && !unNamedSources.isPresent()) {
      return Optional.empty();
    }
    SourceSortedSet set =
        of(
            getType(),
            unNamedSources.orElse(getUnnamedSources()),
            namedSources.orElse(getNamedSources()));
    return Optional.of(set);
  }

  /** Concatenates elements of the given lists into a single list. */
  public static SourceSortedSet concat(Iterable<SourceSortedSet> elements) {
    Type type = findType(elements);

    if (type == Type.UNNAMED) {
      return concatUnnamed(elements);
    } else {
      return concatNamed(elements);
    }
  }

  public static SourceSortedSet of(
      SourceSortedSet.Type type,
      Optional<? extends ImmutableSortedSet<SourcePath>> unnamedSources,
      Optional<? extends ImmutableSortedMap<String, SourcePath>> namedSources) {
    return ImmutableSourceSortedSet.of(type, unnamedSources, namedSources);
  }

  private static Type findType(Iterable<SourceSortedSet> elements) {
    if (!elements.iterator().hasNext()) {
      return Type.UNNAMED;
    }
    return elements.iterator().next().getType();
  }

  private static SourceSortedSet concatUnnamed(Iterable<SourceSortedSet> elements) {
    SortedSet<SourcePath> unnamedSources = new TreeSet<>();

    for (SourceSortedSet element : elements) {
      Preconditions.checkState(
          element.getType().equals(Type.UNNAMED),
          "Expected unnamed source list, got: %s",
          element.getType());
      element.getUnnamedSources().ifPresent(unnamedSources::addAll);
    }

    return ofUnnamedSources(ImmutableSortedSet.copyOf(unnamedSources));
  }

  private static SourceSortedSet concatNamed(Iterable<SourceSortedSet> elements) {
    ImmutableSortedMap.Builder<String, SourcePath> namedSources = ImmutableSortedMap.naturalOrder();

    for (SourceSortedSet element : elements) {
      Preconditions.checkState(
          element.getType().equals(Type.NAMED),
          "Expected named source list, got: %s",
          element.getType());
      element.getNamedSources().ifPresent(namedSources::putAll);
    }

    return ofNamedSources(namedSources.build());
  }
}
