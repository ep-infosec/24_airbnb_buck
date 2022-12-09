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

import com.facebook.buck.core.cell.Cells;
import com.facebook.buck.core.config.BuckConfig;
import com.facebook.buck.core.graph.transformation.executor.DepsAwareExecutor;
import com.facebook.buck.core.graph.transformation.model.ComputeResult;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.model.targetgraph.TargetGraphCreationResult;
import com.facebook.buck.core.parser.buildtargetparser.UnconfiguredBuildTargetViewFactory;
import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.rules.coercer.TypeCoercerFactory;
import com.facebook.buck.util.cache.CacheStats;
import com.facebook.buck.util.cache.CacheStatsTracker;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper class around VersionedTargetGraphCache containing a command specific stats tracker to
 * track performance of the cache
 */
public class InstrumentedVersionedTargetGraphCache {

  private final CacheStatsTracker statsTracker;
  private final VersionedTargetGraphCache cache;

  public InstrumentedVersionedTargetGraphCache(
      VersionedTargetGraphCache cache, CacheStatsTracker statsTracker) {
    this.statsTracker = statsTracker;
    this.cache = cache;
  }

  /**
   * @return a versioned target graph, either generated from the parameters or retrieved from a
   *     cache, with the current CacheStatsTracker.
   */
  public VersionedTargetGraphCache.VersionedTargetGraphCacheResult getVersionedTargetGraph(
      DepsAwareExecutor<? super ComputeResult, ?> depsAwareExecutor,
      TypeCoercerFactory typeCoercerFactory,
      UnconfiguredBuildTargetViewFactory unconfiguredBuildTargetFactory,
      TargetGraphCreationResult targetGraphCreationResult,
      ImmutableMap<String, VersionUniverse> versionUniverses,
      Cells cells)
      throws VersionException, InterruptedException, TimeoutException {
    return cache.toVersionedTargetGraph(
        depsAwareExecutor,
        versionUniverses,
        typeCoercerFactory,
        unconfiguredBuildTargetFactory,
        targetGraphCreationResult,
        statsTracker,
        cells);
  }

  /**
   * @return a versioned target graph, either generated from the parameters or retrieved from a
   *     cache, with the current CacheStatsTracker
   */
  public TargetGraphCreationResult toVersionedTargetGraph(
      DepsAwareExecutor<? super ComputeResult, ?> depsAwareExecutor,
      BuckConfig buckConfig,
      TypeCoercerFactory typeCoercerFactory,
      UnconfiguredBuildTargetViewFactory unconfiguredBuildTargetFactory,
      TargetGraphCreationResult targetGraphCreationResult,
      Optional<TargetConfiguration> targetConfiguration,
      BuckEventBus eventBus,
      Cells cells)
      throws VersionException, InterruptedException {
    return cache
        .getVersionedTargetGraph(
            depsAwareExecutor,
            buckConfig,
            typeCoercerFactory,
            unconfiguredBuildTargetFactory,
            targetGraphCreationResult,
            targetConfiguration,
            statsTracker,
            eventBus,
            cells)
        .getTargetGraphCreationResult();
  }

  /** @return a CacheStats object containing the performance data of this cache */
  public CacheStats getCacheStats() {
    CacheStats.Builder statsBuilder =
        CacheStats.builder()
            .setHitCount(statsTracker.getTotalHitCount())
            .setMissCount(statsTracker.getTotalMissCount())
            .setMissMatchCount(statsTracker.getTotalMissMatchCount());

    if (statsTracker.getTotalHitCount()
            + statsTracker.getTotalMissCount()
            + statsTracker.getTotalMissMatchCount()
        > 0) {
      statsBuilder
          .setRetrievalTime(statsTracker.getAverageRetrievalTime())
          .setTotalMissTime(statsTracker.getAverageMissTime())
          .setTotalLoadTime(statsTracker.getAverageLoadTime());
    }

    return statsBuilder.build();
  }
}
