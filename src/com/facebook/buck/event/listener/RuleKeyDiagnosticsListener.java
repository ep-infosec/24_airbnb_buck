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

package com.facebook.buck.event.listener;

import com.facebook.buck.core.build.event.BuildRuleEvent;
import com.facebook.buck.core.rulekey.BuildRuleKeys;
import com.facebook.buck.core.rulekey.RuleKey;
import com.facebook.buck.core.rulekey.RuleKeyDiagnosticsMode;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.event.BuckEventListener;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.log.InvocationInfo;
import com.facebook.buck.support.bgtasks.BackgroundTask;
import com.facebook.buck.support.bgtasks.TaskAction;
import com.facebook.buck.support.bgtasks.TaskManagerCommandScope;
import com.facebook.buck.support.build.report.BuildReportFileUploader;
import com.facebook.buck.util.BuckConstant;
import com.facebook.buck.util.ThrowingPrintWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.HashCode;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.GuardedBy;

public class RuleKeyDiagnosticsListener implements BuckEventListener {
  private static final Logger LOG = Logger.get(RuleKeyDiagnosticsListener.class);

  // Flush every 1000 keys or 10 MB whichever comes first (some keys can be as large as 1 MB)
  private static final int DEFAULT_MIN_KEYS_FOR_AUTO_FLUSH = 1000;
  private static final int DEFAULT_MIN_SIZE_FOR_AUTO_FLUSH = 10 * 1024 * 1024; // 10 MB

  private static final String TRACE_KIND_RULE_DIAG_GRAPH = "rule_diag_graph";
  private static final String TRACE_KIND_RULE_DIAG_KEYS = "rule_diag_keys";

  private final ProjectFilesystem projectFilesystem;
  private final InvocationInfo info;
  private final ExecutorService outputExecutor;

  private final Optional<BuildReportFileUploader> buildReportFileUploader;

  private final TaskManagerCommandScope managerScope;

  private final int minDiagKeysForAutoFlush;
  private final int minSizeForAutoFlush;
  private final Object diagKeysLock = new Object();

  @GuardedBy("diagKeysLock")
  private List<String> diagKeys;

  @GuardedBy("diagKeysLock")
  private int diagKeysSize; // total number of characters so far

  private final AtomicInteger nextId = new AtomicInteger();
  private final ConcurrentHashMap<BuildRule, RuleInfo> rulesInfo = new ConcurrentHashMap<>();

  public RuleKeyDiagnosticsListener(
      ProjectFilesystem projectFilesystem,
      InvocationInfo info,
      ExecutorService outputExecutor,
      TaskManagerCommandScope managerScope,
      Optional<BuildReportFileUploader> buildReportFileUploader) {
    this.projectFilesystem = projectFilesystem;
    this.info = info;
    this.outputExecutor = outputExecutor;
    this.minDiagKeysForAutoFlush = DEFAULT_MIN_KEYS_FOR_AUTO_FLUSH;
    this.minSizeForAutoFlush = DEFAULT_MIN_SIZE_FOR_AUTO_FLUSH;
    this.diagKeys = new ArrayList<>();
    this.diagKeysSize = 0;
    this.managerScope = managerScope;
    this.buildReportFileUploader = buildReportFileUploader;
  }

  @Subscribe
  public void onBuildRuleEvent(BuildRuleEvent.Finished event) {
    event
        .getDiagnosticData()
        .ifPresent(
            diagData -> {
              diagData.diagnosticKeys.forEach(
                  result -> {
                    String line = String.format("%s %s", result.ruleKey, result.diagKey);
                    synchronized (diagKeysLock) {
                      diagKeys.add(line);
                      diagKeysSize += line.length();
                    }
                  });
              flushDiagKeysIfNeeded();

              rulesInfo.put(
                  event.getBuildRule(),
                  new RuleInfo(
                      nextId.getAndIncrement(),
                      event.getDuration().getNanoDuration(),
                      event.getRuleKeys(),
                      event.getOutputHash(),
                      diagData.deps));
            });
  }

  /** Diagnostic keys flushing logic. */
  private Path getDiagKeysFilePath() {
    Path logDir = projectFilesystem.resolve(info.getLogDirectoryPath());
    return logDir.resolve(BuckConstant.RULE_KEY_DIAG_KEYS_FILE_NAME);
  }

  private void flushDiagKeysIfNeeded() {
    synchronized (diagKeysLock) {
      if (diagKeys.size() > minDiagKeysForAutoFlush || diagKeysSize > minSizeForAutoFlush) {
        submitFlushDiagKeys();
      }
    }
  }

  private void submitFlushDiagKeys() {
    synchronized (diagKeysLock) {
      List<String> keysToFlush = diagKeys;
      diagKeys = new ArrayList<>();
      diagKeysSize = 0;
      if (!keysToFlush.isEmpty()) {
        outputExecutor.execute(() -> actuallyFlushDiagKeys(keysToFlush));
      }
    }
  }

  private void actuallyFlushDiagKeys(List<String> keysToFlush) {
    Path path = getDiagKeysFilePath();
    try {
      projectFilesystem.createParentDirs(path);
      try (OutputStream os = projectFilesystem.newUnbufferedFileOutputStream(path, true);
          ThrowingPrintWriter out = new ThrowingPrintWriter(os, StandardCharsets.UTF_8)) {
        for (String line : keysToFlush) {
          out.println(line);
        }
      }
    } catch (IOException e) {
      LOG.error(e, "Failed to flush [%d] diagnostic keys to file [%s].", keysToFlush.size(), path);
    }
  }

  /** Diagnostic graph flushing logic. */
  private Path getDiagGraphFilePath() {
    Path logDir = projectFilesystem.resolve(info.getLogDirectoryPath());
    return logDir.resolve(BuckConstant.RULE_KEY_DIAG_GRAPH_FILE_NAME);
  }

  /**
   * Writes the directed acyclic graph of all the diagnosed rules to a file.
   *
   * <p>This is a subgraph of the whole graph where only diagnosed rules are included. What rules
   * get diagnostics is specified with {@link RuleKeyDiagnosticsMode}.
   *
   * <p>The format is as follows. All data is written in a textual format using UTF-8 encoding. The
   * first line contains an integer N that denotes the number of diagnosed rules (nodes). The
   * following N lines describe each diagnosed rule as a space-separated list of values. Those
   * values are in order: node id, duration (ns), rule type, target name, cacheable, default rule
   * key, input key, dep-file key, manifest key, output hash. A line with an integer M that denotes
   * the number of edges follows. Edge represents a dependency relation between two nodes. The
   * following M lines describe each edge as two integers: id of a node and id of its dependency.
   * Node ids are not assigned in any specific way and are not compatible across different builds.
   * The only purpose is to reduce the amount of data required to represent edges by using integers
   * instead of long strings (fully qualified target names).
   */
  private void writeDiagGraph() {
    ImmutableList.Builder<DepEdge> dirtyEdgesBuilder = ImmutableList.builder();
    rulesInfo.forEach(
        (rule, info) -> {
          for (BuildRule dep : info.deps) {
            if (rulesInfo.containsKey(dep)) {
              dirtyEdgesBuilder.add(new DepEdge(rule, dep));
            }
          }
        });
    ImmutableList<DepEdge> dirtyEdges = dirtyEdgesBuilder.build();

    Path path = getDiagGraphFilePath();
    try {
      projectFilesystem.createParentDirs(path);
      try (OutputStream os = projectFilesystem.newUnbufferedFileOutputStream(path, false);
          ThrowingPrintWriter out = new ThrowingPrintWriter(os, StandardCharsets.UTF_8)) {
        out.println(rulesInfo.size());
        for (Map.Entry<BuildRule, RuleInfo> entry : rulesInfo.entrySet()) {
          BuildRule rule = entry.getKey();
          RuleInfo info = entry.getValue();
          out.printf(
              "%d %d %s %s %d %s %s %s %s %s%n",
              info.id,
              info.duration,
              rule.getType(),
              rule.getBuildTarget(),
              rule.isCacheable() ? 1 : 0,
              info.ruleKeys.getRuleKey(),
              info.ruleKeys.getInputRuleKey().map(RuleKey::toString).orElse("null"),
              info.ruleKeys.getDepFileRuleKey().map(RuleKey::toString).orElse("null"),
              info.ruleKeys.getManifestRuleKey().map(RuleKey::toString).orElse("null"),
              info.outputHash.map(HashCode::toString).orElse("null"));
        }
        out.println(dirtyEdges.size());
        for (DepEdge edge : dirtyEdges) {
          out.printf("%d %d%n", rulesInfo.get(edge.rule).id, rulesInfo.get(edge.dep).id);
        }
      }
    } catch (IOException e) {
      LOG.error(e, "Failed to write %s.", path);
    }
  }

  @Override
  public void close() { // todo same issue w/passed function
    submitFlushDiagKeys();
    if (!rulesInfo.isEmpty()) {
      outputExecutor.execute(this::writeDiagGraph);
    }

    Path logDir = info.getLogDirectoryPath();
    RuleKeyDiagnosticsListenerCloseArgs args =
        ImmutableRuleKeyDiagnosticsListenerCloseArgs.of(
            outputExecutor,
            buildReportFileUploader,
            logDir.resolve(BuckConstant.RULE_KEY_DIAG_GRAPH_FILE_NAME),
            logDir.resolve(BuckConstant.RULE_KEY_DIAG_KEYS_FILE_NAME));

    BackgroundTask<RuleKeyDiagnosticsListenerCloseArgs> task =
        BackgroundTask.of(
            "RuleKeyDiagnosticsListener_close", new RuleKeyDiagnosticsListenerCloseAction(), args);
    managerScope.schedule(task);
  }

  /**
   * {@link TaskAction} implementation for close() in {@link RuleKeyDiagnosticsListener}. Waits for
   * listener's output executor to finish and close.
   */
  static class RuleKeyDiagnosticsListenerCloseAction
      implements TaskAction<RuleKeyDiagnosticsListenerCloseArgs> {
    @Override
    public void run(RuleKeyDiagnosticsListenerCloseArgs args) {
      args.getOutputExecutor().shutdown();
      try {
        args.getOutputExecutor().awaitTermination(1, TimeUnit.HOURS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      args.getBuildReportFileUploader()
          .ifPresent(
              uploader -> {
                if (args.getRuleDiagGraphFilePath().toFile().exists()) {
                  uploader.uploadFile(args.getRuleDiagGraphFilePath(), TRACE_KIND_RULE_DIAG_GRAPH);
                } else {
                  LOG.debug(
                      "Not uploading %s. %s doesn't exist.",
                      TRACE_KIND_RULE_DIAG_GRAPH, args.getRuleDiagGraphFilePath().toString());
                }
                if (args.getRuleDiagKeyFilePath().toFile().exists()) {
                  uploader.uploadFile(args.getRuleDiagKeyFilePath(), TRACE_KIND_RULE_DIAG_KEYS);
                } else {
                  LOG.debug(
                      "Not uploading %s. %s doesn't exist.",
                      TRACE_KIND_RULE_DIAG_KEYS, args.getRuleDiagKeyFilePath().toString());
                }
              });
    }
  }

  /** Arguments to {@link RuleKeyDiagnosticsListenerCloseAction}. */
  @BuckStyleValue
  abstract static class RuleKeyDiagnosticsListenerCloseArgs {
    public abstract ExecutorService getOutputExecutor();

    public abstract Optional<BuildReportFileUploader> getBuildReportFileUploader();

    public abstract Path getRuleDiagGraphFilePath();

    public abstract Path getRuleDiagKeyFilePath();
  }

  private static class RuleInfo {
    public final int id;
    public final long duration;
    public final BuildRuleKeys ruleKeys;
    public final Optional<HashCode> outputHash;
    public final SortedSet<BuildRule> deps;

    public RuleInfo(
        int id,
        long duration,
        BuildRuleKeys ruleKeys,
        Optional<HashCode> outputHash,
        SortedSet<BuildRule> deps) {
      this.id = id;
      this.duration = duration;
      this.ruleKeys = ruleKeys;
      this.outputHash = outputHash;
      this.deps = deps;
    }
  }

  private static class DepEdge {
    public final BuildRule rule;
    public final BuildRule dep;

    public DepEdge(BuildRule rule, BuildRule dep) {
      this.rule = rule;
      this.dep = dep;
    }
  }
}
