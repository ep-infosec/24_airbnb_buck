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

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.model.BuildId;
import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.event.DefaultBuckEventBus;
import com.facebook.buck.event.listener.util.ProgressEstimator;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.facebook.buck.testutil.TemporaryPaths;
import com.facebook.buck.util.json.ObjectMappers;
import com.facebook.buck.util.timing.FakeClock;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

public class ProgressEstimatorTest {

  @Rule public final TemporaryPaths tmp = new TemporaryPaths();

  public BuckEventBus getBuckEventBus() {
    return new DefaultBuckEventBus(FakeClock.doNotCare(), new BuildId(""));
  }

  @Test
  public void testByDefaultProvidesNoProcessingBuckFilesProgress() {
    ProjectFilesystem filesystem = new FakeProjectFilesystem();
    Path p = filesystem.resolve(ProgressEstimator.PROGRESS_ESTIMATIONS_JSON);
    ProgressEstimator e = new ProgressEstimator(Optional.of(p), getBuckEventBus());
    assertThat(
        e.getEstimatedProgressOfParsingBuckFiles().getProgress().isPresent(),
        Matchers.equalTo(false));
  }

  @Test
  public void testByProvidesNoProcessingBuckFilesProgressIfStorageDoesNotExist()
      throws IOException {
    Path storagePath = getStorageForTest();

    ProgressEstimator estimator =
        new ProgressEstimator(Optional.of(storagePath), getBuckEventBus());

    estimator.setCurrentCommand("project", ImmutableList.of("arg1", "arg2"));
    estimator.didParseBuckRules(10);

    estimator.close();

    assertThat(
        estimator.getEstimatedProgressOfParsingBuckFiles().getProgress().isPresent(),
        Matchers.equalTo(false));
  }

  @Test
  public void testProvidesProcessingBuckFilesProgressIfStorageExists() throws IOException {
    Path storagePath = getStorageForTest();

    Map<String, Object> storageContents =
        ImmutableSortedMap.<String, Object>naturalOrder()
            .put(
                "project arg1 arg2",
                ImmutableSortedMap.<String, Number>naturalOrder()
                    .put(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_RULES, 100)
                    .put(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_BUCK_FILES, 10)
                    .build())
            .build();
    String contents = ObjectMappers.WRITER.writeValueAsString(storageContents);
    Files.write(storagePath, contents.getBytes(StandardCharsets.UTF_8));

    // path is 2 levels up folder
    ProgressEstimator estimator =
        new ProgressEstimator(Optional.of(storagePath), getBuckEventBus());

    estimator.setCurrentCommand("project", ImmutableList.of("arg1", "arg2"));
    estimator.didParseBuckRules(10);

    estimator.close();

    assertThat(
        estimator.getEstimatedProgressOfParsingBuckFiles().getProgress().get(),
        Matchers.closeTo(0.1, 0.01));
  }

  @Test
  public void testUpdatesStorageWithParsingEstimationsAfterCommandInvocation() throws IOException {
    Path storagePath = getStorageForTest();

    Map<String, Object> storageContents =
        ImmutableSortedMap.<String, Object>naturalOrder()
            .put(
                "project arg1 arg2",
                ImmutableSortedMap.<String, Number>naturalOrder()
                    .put(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_RULES, 100)
                    .put(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_BUCK_FILES, 10)
                    .build())
            .build();
    String contents = ObjectMappers.WRITER.writeValueAsString(storageContents);
    Files.write(storagePath, contents.getBytes(StandardCharsets.UTF_8));

    // path is 2 levels up folder
    ProgressEstimator estimator =
        new ProgressEstimator(Optional.of(storagePath), getBuckEventBus());

    estimator.setCurrentCommand("project", ImmutableList.of("arg1", "arg2"));
    estimator.didParseBuckRules(10);
    estimator.didParseBuckRules(30);
    estimator.didParseBuckRules(10);
    estimator.didFinishParsing();

    estimator.close();

    Map<String, Map<String, Number>> jsonObject =
        ObjectMappers.READER.readValue(
            ObjectMappers.createParser(Files.readAllBytes(storagePath)),
            new TypeReference<HashMap<String, Map<String, Number>>>() {});

    Map<String, Number> storedValues = jsonObject.get("project arg1 arg2");
    assertThat(
        storedValues.get(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_BUCK_FILES).intValue(),
        Matchers.equalTo(3));
    assertThat(
        storedValues.get(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_RULES).intValue(),
        Matchers.equalTo(50));
  }

  @Test
  public void testProvidesProjectGenerationProgressIfStorageExists() throws IOException {
    Path storagePath = getStorageForTest();

    Map<String, Object> storageContents =
        ImmutableSortedMap.<String, Object>naturalOrder()
            .put(
                "project arg1 arg2",
                ImmutableSortedMap.<String, Number>naturalOrder()
                    .put(ProgressEstimator.EXPECTED_NUMBER_OF_GENERATED_PROJECT_FILES, 10)
                    .build())
            .build();
    String contents = ObjectMappers.WRITER.writeValueAsString(storageContents);
    Files.write(storagePath, contents.getBytes(StandardCharsets.UTF_8));

    // path is 2 levels up folder
    ProgressEstimator estimator =
        new ProgressEstimator(Optional.of(storagePath), getBuckEventBus());

    estimator.setCurrentCommand("project", ImmutableList.of("arg1", "arg2"));
    estimator.didGenerateProjectForTarget();
    estimator.didGenerateProjectForTarget();

    estimator.close();

    assertThat(
        estimator.getEstimatedProgressOfGeneratingProjectFiles().get(),
        Matchers.closeTo(0.2, 0.01));
  }

  @Test
  public void testUpdatesStorageWithProjectGenerationEstimationsAfterCommandInvocation()
      throws IOException {
    Path storagePath = getStorageForTest();

    Map<String, Object> storageContents =
        ImmutableSortedMap.<String, Object>naturalOrder()
            .put(
                "project arg1 arg2",
                ImmutableSortedMap.<String, Number>naturalOrder()
                    .put(ProgressEstimator.EXPECTED_NUMBER_OF_GENERATED_PROJECT_FILES, 10)
                    .build())
            .build();
    String contents = ObjectMappers.WRITER.writeValueAsString(storageContents);
    Files.write(storagePath, contents.getBytes(StandardCharsets.UTF_8));

    // path is 2 levels up folder
    ProgressEstimator estimator =
        new ProgressEstimator(Optional.of(storagePath), getBuckEventBus());

    estimator.setCurrentCommand("project", ImmutableList.of("arg1", "arg2"));
    estimator.didGenerateProjectForTarget();
    estimator.didGenerateProjectForTarget();
    estimator.didGenerateProjectForTarget();
    estimator.didFinishProjectGeneration();

    estimator.close();

    Map<String, Map<String, Number>> jsonObject =
        ObjectMappers.READER.readValue(
            ObjectMappers.createParser(Files.readAllBytes(storagePath)),
            new TypeReference<HashMap<String, Map<String, Number>>>() {});

    Map<String, Number> storedValues = jsonObject.get("project arg1 arg2");
    assertThat(
        storedValues.get(ProgressEstimator.EXPECTED_NUMBER_OF_GENERATED_PROJECT_FILES).intValue(),
        Matchers.equalTo(3));
  }

  @Test
  public void testUpdatesStorageWithParsingAndProjectGenerationEstimationsAfterCommandInvocation()
      throws IOException {
    Path storagePath = getStorageForTest();

    // path is 2 levels up folder
    ProgressEstimator estimator =
        new ProgressEstimator(Optional.of(storagePath), getBuckEventBus());

    estimator.setCurrentCommand("project", ImmutableList.of("arg1", "arg2"));

    estimator.didParseBuckRules(10);
    estimator.didParseBuckRules(20);
    estimator.didParseBuckRules(15);
    estimator.didParseBuckRules(2);
    estimator.didFinishParsing();

    estimator.didGenerateProjectForTarget();
    estimator.didGenerateProjectForTarget();
    estimator.didFinishProjectGeneration();

    estimator.close();

    Map<String, Map<String, Number>> jsonObject =
        ObjectMappers.READER.readValue(
            ObjectMappers.createParser(Files.readAllBytes(storagePath)),
            new TypeReference<HashMap<String, Map<String, Number>>>() {});

    Map<String, Number> storedValues = jsonObject.get("project arg1 arg2");
    assertThat(
        storedValues.get(ProgressEstimator.EXPECTED_NUMBER_OF_GENERATED_PROJECT_FILES).intValue(),
        Matchers.equalTo(2));
    assertThat(
        storedValues.get(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_BUCK_FILES).intValue(),
        Matchers.equalTo(4));
    assertThat(
        storedValues.get(ProgressEstimator.EXPECTED_NUMBER_OF_PARSED_RULES).intValue(),
        Matchers.equalTo(47));
  }

  private Path getStorageForTest() throws IOException {
    return tmp.newFile();
  }

  @Test
  public void testByDefaultProvidesNoBuildProgress() {
    ProjectFilesystem filesystem = new FakeProjectFilesystem();
    Path p = filesystem.resolve(ProgressEstimator.PROGRESS_ESTIMATIONS_JSON);
    ProgressEstimator e = new ProgressEstimator(Optional.of(p), getBuckEventBus());
    assertThat(e.getApproximateBuildProgress().isPresent(), Matchers.equalTo(false));
  }

  @Test
  public void testByProvidesCompleteBuildProgressAfterGettingBuildEvents() {
    ProjectFilesystem filesystem = new FakeProjectFilesystem();
    Path p = filesystem.resolve(ProgressEstimator.PROGRESS_ESTIMATIONS_JSON);
    ProgressEstimator e = new ProgressEstimator(Optional.of(p), getBuckEventBus());

    e.didStartBuild();
    e.setNumberOfRules(10);
    e.didFinishBuild();

    assertThat(e.getApproximateBuildProgress().isPresent(), Matchers.equalTo(true));
    assertThat(e.getApproximateBuildProgress().get(), Matchers.closeTo(1.0, 0.01));
  }

  @Test
  public void testByProvidesPartialBuildProgressAfterGettingBuildEvents() {
    ProjectFilesystem filesystem = new FakeProjectFilesystem();
    Path p = filesystem.resolve(ProgressEstimator.PROGRESS_ESTIMATIONS_JSON);
    ProgressEstimator e = new ProgressEstimator(Optional.of(p), getBuckEventBus());

    e.didStartBuild();
    e.setNumberOfRules(10);

    e.didFinishRule();
    e.didFinishRule();
    e.didFinishRule();
    e.didFinishRule();

    assertThat(e.getApproximateBuildProgress().isPresent(), Matchers.equalTo(true));
    assertThat(e.getApproximateBuildProgress().get(), Matchers.greaterThan(0.0));
    assertThat(e.getApproximateBuildProgress().get(), Matchers.lessThan(1.0));
  }
}
