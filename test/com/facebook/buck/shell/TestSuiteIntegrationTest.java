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

package com.facebook.buck.shell;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.facebook.buck.testutil.ProcessResult;
import com.facebook.buck.testutil.TemporaryPaths;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.TestDataHelper;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class TestSuiteIntegrationTest {
  @Rule public TemporaryPaths tmp = new TemporaryPaths();

  void assertRegexFind(String pattern, String subject) {
    Assert.assertTrue(
        String.format("Expected to find pattern `%s` in %s", pattern, subject),
        Pattern.compile(pattern, Pattern.MULTILINE).matcher(subject).find());
  }

  @Test
  public void runsDirectlySpecifiedTests() throws IOException {
    ProjectWorkspace workspace =
        TestDataHelper.createProjectWorkspaceForScenario(this, "simple_test_suite", tmp);
    workspace.setUp();

    ProcessResult result = workspace.runBuckCommand("test", "//:suite_ride").assertSuccess();

    assertRegexFind("^TESTING //tests:test1 //tests:test2$", result.getStderr());
    assertRegexFind("^PASS.*//tests:test1$", result.getStderr());
    assertRegexFind("^PASS.*//tests:test2$", result.getStderr());
  }

  @Test
  public void runsChildTestSuites() throws IOException {
    ProjectWorkspace workspace =
        TestDataHelper.createProjectWorkspaceForScenario(this, "simple_test_suite", tmp);
    workspace.setUp();

    ProcessResult result = workspace.runBuckCommand("test", "//:master_test_suite").assertSuccess();

    assertRegexFind("^TESTING //tests:test1 //tests:test2 //tests:test3$", result.getStderr());
    assertRegexFind("^PASS.*//tests:test1$", result.getStderr());
    assertRegexFind("^PASS.*//tests:test2$", result.getStderr());
    assertRegexFind("^PASS.*//tests:test3$", result.getStderr());
  }

  @Test
  public void throwsErrorIfNonTestDependencyProvided() throws IOException {
    ProjectWorkspace workspace =
        TestDataHelper.createProjectWorkspaceForScenario(this, "simple_test_suite", tmp);
    workspace.setUp();
    ProcessResult processResult = workspace.runBuckCommand("build", "//:suite_with_binary");
    processResult.assertFailure();

    assertThat(
        processResult.getStderr(),
        containsString(
            "Non-test rule provided in `tests` in test_suite //:suite_with_binary: //tests:bin1.sh"));
  }

  @Test
  public void throwsShortenedErrorIfNonTestDependencyProvided() throws IOException {
    ProjectWorkspace workspace =
        TestDataHelper.createProjectWorkspaceForScenario(this, "simple_test_suite", tmp);
    workspace.setUp();
    ProcessResult processResult = workspace.runBuckCommand("build", "//:suite_with_binaries");
    processResult.assertFailure();
    assertThat(
        processResult.getStderr(),
        containsString(
            "Non-test rules provided in `tests` in test_suite //:suite_with_binaries: "
                + "//tests:bin1.sh\n"
                + "//tests:bin2.sh\n"
                + "//tests:bin3.sh\n"
                + "//tests:bin4.sh\n"
                + "//tests:bin5.sh"));
  }
}
