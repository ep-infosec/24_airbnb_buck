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

package com.facebook.buck.util.cache;

/** Class that tracks cache statistics, including timings. */
public interface CacheStatsTracker {

  CacheRequest startRequest();

  long getTotalHitCount();

  long getTotalMissCount();

  long getTotalMissMatchCount();

  long getTotalEvictionCount();

  long getTotalInvalidationCount();

  long getTotalLoadSuccessCount();

  long getTotalLoadExceptionCount();

  long getTotalRetrievalTime();

  long getTotalLoadTime();

  long getTotalMissTime();

  long getAverageRetrievalTime();

  long getAverageMissTime();

  long getAverageLoadTime();

  void recordEviction();

  void recordEviction(long num);

  void recordInvalidation();

  void recordInvalidation(long num);

  /** Class that keeps record and timings of a single cache request */
  interface CacheRequest {

    /**
     * Records that a cache hit has occurred and updates the corresponding CacheStatsTracker, and
     * records the time it took for the cache retrieval
     */
    void recordHit();

    /**
     * Records that a cache miss has occurred and updates the corresponding CacheStatsTracker, and
     * starts recording the load time starting at this instant. If no load event occurs, the current
     * time will be recorded as the time it took for a cache miss
     */
    void recordMiss();

    /**
     * Records that a cache miss due to mismatch has occurred and updates the corresponding
     * CacheStatsTracker, and starts recording the load time starting at this instant. If no load
     * event occurs, the current time will be recorded as the time it took for a cache miss
     */
    void recordMissMatch();

    /**
     * Records that a cache load was successful and updates the corresponding CacheStatsTracker, and
     * records the time it took to load the object and updates the total time spent on a cache miss
     */
    void recordLoadSuccess();

    /**
     * Records that a cache load has failed and updates the corresponding CacheStatsTracker, and
     * records the time spent on a cache miss
     */
    void recordLoadFail();
  }
}
