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

package com.facebook.buck.cli;

import static com.facebook.buck.event.TestEventConfigurator.configureTestEvent;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import com.facebook.buck.event.CommandEvent;
import com.facebook.buck.util.ExitCode;
import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.util.OptionalLong;
import org.hamcrest.Matchers;
import org.junit.Test;

public class CommandEventTest {
  @Test
  public void testEquals() {
    CommandEvent.Started startedDaemon =
        configureTestEvent(
            CommandEvent.started(
                "build", ImmutableList.of("sample-app"), Paths.get(""), OptionalLong.of(12), 17L));
    CommandEvent.Started startedDaemonTwo =
        configureTestEvent(
            CommandEvent.started(
                "build", ImmutableList.of("sample-app"), Paths.get(""), OptionalLong.of(20), 23L));
    CommandEvent.Started startedNoDaemon =
        configureTestEvent(
            CommandEvent.started(
                "build", ImmutableList.of("sample-app"), Paths.get(""), OptionalLong.empty(), 3L));
    CommandEvent.Started startedDifferentName =
        configureTestEvent(
            CommandEvent.started(
                "test", ImmutableList.of("sample-app"), Paths.get(""), OptionalLong.empty(), 11L));
    CommandEvent finishedDaemon =
        configureTestEvent(CommandEvent.finished(startedDaemon, ExitCode.SUCCESS));
    CommandEvent finishedDaemonFailed =
        configureTestEvent(CommandEvent.finished(startedDaemonTwo, ExitCode.BUILD_ERROR));
    CommandEvent finishedDifferentName =
        configureTestEvent(CommandEvent.finished(startedDifferentName, ExitCode.SUCCESS));

    assertNotEquals(startedDaemon, startedDaemonTwo);
    assertNotEquals(startedDaemon, startedNoDaemon);
    assertNotEquals(startedDaemon, startedDifferentName);
    assertNotEquals(finishedDaemon, startedDaemon);
    assertNotEquals(finishedDaemon, finishedDaemonFailed);
    assertNotEquals(finishedDaemon, finishedDifferentName);
    assertThat(startedDaemon.isRelatedTo(finishedDaemon), Matchers.is(true));
    assertThat(finishedDaemon.isRelatedTo(startedDaemon), Matchers.is(true));
    assertThat(startedDaemon.isRelatedTo(startedDaemonTwo), Matchers.is(false));
  }
}
