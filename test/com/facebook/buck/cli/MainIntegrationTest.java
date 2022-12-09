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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.facebook.buck.testutil.ProcessResult;
import com.facebook.buck.testutil.TemporaryPaths;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.TestDataHelper;
import com.facebook.buck.util.ExitCode;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MainIntegrationTest {

  @Rule public TemporaryPaths tmp = new TemporaryPaths();
  @Rule public ExpectedException thrown = ExpectedException.none();
  @Rule public TestWithBuckd testWithBuckd = new TestWithBuckd(tmp);

  @Test
  public void testBuckNoArgs() throws IOException {
    ProjectWorkspace workspace =
        TestDataHelper.createProjectWorkspaceForScenario(this, "empty_project", tmp);
    workspace.setUp();

    ProcessResult result = workspace.runBuckCommand();

    result.assertExitCode("nothing specified", ExitCode.COMMANDLINE_ERROR);
    assertThat(
        "When the user does not specify any arguments, the usage information should be displayed",
        result.getStderr(),
        containsString(getUsageString()));
  }

  @Test
  public void testBuckHelp() throws IOException {
    ProjectWorkspace workspace =
        TestDataHelper.createProjectWorkspaceForScenario(this, "empty_project", tmp);
    workspace.setUp();

    ProcessResult result = workspace.runBuckCommand();

    result.assertExitCode("nothing specified", ExitCode.COMMANDLINE_ERROR);
    assertThat(
        "Users instinctively try running `buck --help`, so it should print usage info.",
        result.getStderr(),
        containsString(getUsageString()));
  }

  private String getUsageString() {
    return String.join(
        System.lineSeparator(),
        "Description: ",
        "  Buck build tool",
        "",
        "Usage:",
        "  buck [<options>]",
        "  buck <command> --help",
        "  buck <command> [<command-options>]",
        "",
        "Environment variables:",
        "  NO_BUCKD              do not launch buck daemon",
        "  BUCK_DEBUG_MODE       start buck with debugger agent listening on port 8888",
        "  BUCK_EXTRA_JAVA_ARGS  JVM arguments used when launching buck process",
        "  FAKE_JAVA_VERSION     override a version used in rule key computation",
        "",
        "Available commands:",
        "  audit            lists the inputs for the specified target",
        "  build            builds the specified target",
        "  cache            makes calls to the artifact cache",
        "  cachedelete      Delete artifacts from the local and remote cache",
        "  clean            deletes any generated files and caches",
        "  doctor           debug and fix issues of Buck commands",
        "  fetch            downloads remote resources to your local machine",
        "  fix              attempts to fix errors encountered in the previous build",
        "  help             "
            + "shows this screen (or the help page of the specified command) and exits.",
        "  install          builds and installs an application",
        "  kill             kill buckd for the current project",
        "  killall          kill all buckd processes",
        "  perf             various utilities for testing performance of Buck against real codebases "
            + "and configurations. NOTE: This command's interface is unstable and will change "
            + "without warning.",
        "  project          generates project configuration files for an IDE",
        "  publish          builds and publishes a library to a central repository",
        "  query            "
            + "provides facilities to query information about the configured target nodes graph",
        "  rage             debug and fix issues of Buck commands",
        "  root             prints the absolute path to the root of the current buck project",
        "  run              runs a target as a command",
        "  server           query and control the http server",
        "  targets          prints the list of buildable targets",
        "  test             builds and runs the tests for the specified target",
        "  uninstall        uninstalls an APK",
        "  uquery-dont-use  "
            + "provides facilities to query information about the unconfigured target nodes graph",
        "  verify-caches    Verify contents of internal Buck in-memory caches.",
        "",
        "Options:",
        " --flagfile FILE         : File to read command line arguments from.",
        " --help (-h)             : Shows this screen and exits.",
        " --version (-V)          : Show version number.",
        "",
        "");
  }
}
