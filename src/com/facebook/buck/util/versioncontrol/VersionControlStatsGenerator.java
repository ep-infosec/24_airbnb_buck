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

package com.facebook.buck.util.versioncontrol;

import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.event.SimplePerfEvent;
import com.facebook.buck.util.Threads;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

public class VersionControlStatsGenerator {

  private static final Logger LOG = Logger.get(VersionControlStatsGenerator.class);

  /**
   * Modes the generator can get stats in, in order from least comprehensive to most comprehensive.
   * Each mode should include all the information present in the previous one.
   */
  public enum Mode {
    /** Do not generate new information, but return whatever is already generated */
    PREGENERATED(false, false, false),
    /** Generate a set of stats that is fast to generate but incomplete */
    FAST(true, false, false),
    /** Generate a set of stats that is slow to generate but incomplete */
    SLOW(true, true, false),
    /** Generate the full set of stats */
    FULL(true, true, true),
    ;

    public final boolean shouldGenerate;
    public final boolean hasPathsChangedInWorkingDirectory;
    public final boolean hasDiff;

    Mode(boolean shouldGenerate, boolean hasPathsChangedInWorkingDirectory, boolean hasDiff) {
      this.shouldGenerate = shouldGenerate;
      this.hasPathsChangedInWorkingDirectory = hasPathsChangedInWorkingDirectory;
      this.hasDiff = hasDiff;
    }
  }

  private static final String REMOTE_MASTER = "remote/master";
  private static final ImmutableSet<String> TRACKED_BOOKMARKS =
      ImmutableSet.of(
          REMOTE_MASTER);

  private final VersionControlCmdLineInterface versionControlCmdLineInterface;

  private final Optional<FastVersionControlStats> pregeneratedVersionControlStats;

  @GuardedBy("this")
  @Nullable
  private FastVersionControlStats fastStats;

  @GuardedBy("this")
  @Nullable
  private ImmutableSet<String> changedFiles;

  @GuardedBy("this")
  @Nullable
  private Optional<VersionControlSupplier<InputStream>> diff;

  public VersionControlStatsGenerator(
      VersionControlCmdLineInterface versionControlCmdLineInterface,
      Optional<FastVersionControlStats> pregeneratedVersionControlStats) {
    this.versionControlCmdLineInterface = versionControlCmdLineInterface;
    this.pregeneratedVersionControlStats = pregeneratedVersionControlStats;
    pregeneratedVersionControlStats.ifPresent(
        x -> {
          synchronized (this) {
            this.fastStats =
                ImmutableFastVersionControlStats.of(
                    x.getCurrentRevisionId(),
                    x.getBaseBookmarks(),
                    x.getBranchedFromMasterRevisionId(),
                    x.getBranchedFromMasterTS());
          }
        });
  }

  /**
   * Generate {@link FullVersionControlStats}. The parameters define which mode is generated
   *
   * @param shouldUploadBuildReport if this buck build will upload a full build report then we
   *     generate {@link FullVersionControlStats}, otherwise just generate {@link
   *     SlowVersionControlStats}.
   * @param shouldPreGenerate if true, return a {@link FastVersionControlStats} regardless of {@code
   *     shouldUploadBuildReport}'s value
   * @param buckEventBus the eventBus to which post the {@link VersionControlStatsEvent}
   * @param executorService to which this future is attached to. Preferably a diskIO executor
   *     Service.
   */
  public ListenableFuture<Optional<FullVersionControlStats>> generateStatsAsync(
      boolean shouldUploadBuildReport,
      boolean shouldPreGenerate,
      BuckEventBus buckEventBus,
      ListeningExecutorService executorService) {
    Mode mode = shouldUploadBuildReport ? Mode.FULL : Mode.SLOW;

    if (shouldPreGenerate) {
      mode = Mode.PREGENERATED;
    }

    return generateStatsAsync(mode, buckEventBus, executorService);
  }

  /**
   * Generate {@code VersionControlStats} in a thread to avoid blocking I/O in the main thread.
   * Depending on the mode given, the type returned is:
   *
   * <ul>
   *   <li>Mode.PREGENERATED => {@link CommonFastVersionControlStats}
   *   <li>Mode.FAST => {@link FastVersionControlStats}
   *   <li>Mode.SLOW => {@link SlowVersionControlStats}
   *   <li>Mode.FULL => {@link FullVersionControlStats}
   * </ul>
   */
  private ListenableFuture<Optional<FullVersionControlStats>> generateStatsAsync(
      Mode mode, BuckEventBus buckEventBus, ListeningExecutorService executorService) {
    return executorService.submit(
        () -> {
          try {
            Optional<FullVersionControlStats> versionControlStats;
            try (SimplePerfEvent.Scope ignored =
                SimplePerfEvent.scope(buckEventBus, "gen_source_control_info")) {
              versionControlStats = generateStats(mode);
            }
            versionControlStats.ifPresent(
                x -> {
                  buckEventBus.post(new FastVersionControlStatsEvent(x));
                  if (mode == Mode.SLOW || mode == Mode.FULL) {
                    buckEventBus.post(new VersionControlStatsEvent(x));
                  }
                });
            return versionControlStats;
          } catch (InterruptedException e) {
            LOG.warn(e, "Failed to generate VC stats due to being interrupted. Skipping..");
            Threads.interruptCurrentThread(); // Re-set interrupt flag
            return Optional.empty();
          }
        });
  }

  public synchronized Optional<FullVersionControlStats> generateStats(Mode mode)
      throws InterruptedException {
    if (!mode.shouldGenerate) {
      return pregeneratedVersionControlStats.map(
          x -> FullVersionControlStats.builder().from(x).build());
    }

    FullVersionControlStats versionControlStats = null;
    LOG.info("Starting generation of version control stats.");
    if (!versionControlCmdLineInterface.isSupportedVersionControlSystem()) {
      LOG.warn("Skipping generation of version control stats as unsupported repository type.");
    } else {
      FullVersionControlStats.Builder versionControlStatsBuilder =
          FullVersionControlStats.builder();
      try {
        if (fastStats == null) {
          fastStats = versionControlCmdLineInterface.fastVersionControlStats();
        }
        versionControlStatsBuilder.setCurrentRevisionId(fastStats.getCurrentRevisionId());
        versionControlStatsBuilder.setBaseBookmarks(
            Sets.intersection(fastStats.getBaseBookmarks(), TRACKED_BOOKMARKS));
        versionControlStatsBuilder.setBranchedFromMasterRevisionId(
            fastStats.getBranchedFromMasterRevisionId());
        versionControlStatsBuilder.setBranchedFromMasterTS(fastStats.getBranchedFromMasterTS());
        // Prepopulate as much as possible before trying to query the VCS: this way if it fails
        // we still have this information.
        if (mode.hasPathsChangedInWorkingDirectory && changedFiles != null) {
          versionControlStatsBuilder.setPathsChangedInWorkingDirectory(changedFiles);
        }
        if (mode.hasDiff && diff != null) {
          versionControlStatsBuilder.setDiff(diff);
        }
        if (mode.hasPathsChangedInWorkingDirectory && changedFiles == null) {
          changedFiles =
              versionControlCmdLineInterface.changedFiles(
                  fastStats.getBranchedFromMasterRevisionId());
          versionControlStatsBuilder.setPathsChangedInWorkingDirectory(changedFiles);
        }
        if (mode.hasDiff && diff == null) {
          diff =
              versionControlCmdLineInterface.diffBetweenRevisions(
                  fastStats.getBranchedFromMasterRevisionId(), fastStats.getCurrentRevisionId());
          versionControlStatsBuilder.setDiff(diff);
        }
      } catch (VersionControlCommandFailedException e) {
        LOG.warn("Failed to gather some source control stats.");
      }
      versionControlStats = versionControlStatsBuilder.build();
      LOG.info("Version Control Stats generated successfully.");
    }
    return Optional.ofNullable(versionControlStats);
  }
}
