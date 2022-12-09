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

package com.facebook.buck.apple.simulator;

import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.util.string.MoreStrings;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class to parse the output of `xcrun simctl list`. */
public class SimctlListOutputParsing {
  private static final Logger LOG = Logger.get(SimctlListOutputParsing.class);

  private static final String DEVICE_NAME_GROUP = "name";
  private static final String DEVICE_UDID_GROUP = "udid";
  private static final String DEVICE_STATE_GROUP = "state";

  private static final String TERM_ESCS = "(?:\\u001B\\[[;\\d]*[mK])*";

  private static final Pattern SIMCTL_LIST_DEVICES_PATTERN =
      Pattern.compile(
          "(?<"
              + DEVICE_NAME_GROUP
              + ">.+) "
              + "\\((?<"
              + DEVICE_UDID_GROUP
              + ">[0-9A-F-]+)\\) "
              + "\\((?<"
              + DEVICE_STATE_GROUP
              + ">Creating|Booting|Shutting Down|Shutdown|Booted)\\)");

  // Utility class; do not instantiate.
  private SimctlListOutputParsing() {}

  /**
   * Parses each line of {@code output}, adding any available simulators to {@code
   * simulatorsBuilder}.
   */
  public static void parseOutput(
      String output, ImmutableSet.Builder<AppleSimulator> simulatorsBuilder) throws IOException {
    for (String line : MoreStrings.lines(output)) {
      line = line.replaceAll(TERM_ESCS, "").trim();
      parseLine(line, simulatorsBuilder);
    }
  }

  private static void parseLine(
      String line, ImmutableSet.Builder<AppleSimulator> simulatorsBuilder) {
    LOG.debug("Parsing simctl list output line: %s", line);
    Matcher matcher = SIMCTL_LIST_DEVICES_PATTERN.matcher(line);
    if (matcher.matches()) {
      AppleSimulator simulator =
          ImmutableAppleSimulator.of(
              matcher.group(DEVICE_NAME_GROUP),
              matcher.group(DEVICE_UDID_GROUP),
              AppleSimulatorState.fromString(matcher.group(DEVICE_STATE_GROUP)));
      LOG.debug("Got simulator: %s", simulator);
      simulatorsBuilder.add(simulator);
    }
  }
}
