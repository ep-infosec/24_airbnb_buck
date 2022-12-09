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

package com.facebook.buck.cxx.endtoend;

import com.facebook.buck.testutil.ProcessResult;
import com.facebook.buck.testutil.endtoend.ConfigSetBuilder;
import com.facebook.buck.testutil.endtoend.EndToEndEnvironment;
import com.facebook.buck.testutil.endtoend.EndToEndRunner;
import com.facebook.buck.testutil.endtoend.EndToEndTestDescriptor;
import com.facebook.buck.testutil.endtoend.EndToEndWorkspace;
import com.facebook.buck.testutil.endtoend.Environment;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * E2E tests for buck's building process on an environment constructed like:
 *
 * <pre>
 * cxx_binary
 * +
 * +--------+--------+
 * v                 v
 * cxx_library       cxx_library
 * +                 +
 * v                 v
 * output_src        prebuilt_cxx_library
 * +                 +
 * v                 v
 * genrule           genrule
 * +                 +
 * v                 v
 * py_binary         cxx_library
 * +
 * v
 * py_library
 * </pre>
 */
@RunWith(EndToEndRunner.class)
public class CxxDependentOnPyEndToEndTest {
  private static final String mainTarget = "//main_bin:main_bin";

  @Environment
  public static EndToEndEnvironment baseEnvironment() {
    ConfigSetBuilder configSetBuilder = new ConfigSetBuilder();
    return new EndToEndEnvironment()
        .addTemplates("cxx_dependent_on_py")
        .withCommand("build")
        .withTargets(mainTarget)
        .addLocalConfigSet(configSetBuilder.build())
        .addLocalConfigSet(configSetBuilder.addShlibConfigSet().build());
  }

  private ProcessResult checkSuccessfulBuildAndRun(
      String message, EndToEndTestDescriptor test, EndToEndWorkspace workspace) throws Exception {
    ProcessResult result = workspace.runBuckCommand(test);
    result.assertSuccess(String.format(message, "build"));
    ProcessResult targetResult = workspace.runBuiltResult(mainTarget);
    targetResult.assertSuccess(String.format(message, "run"));
    return targetResult;
  }

  /** Determines that buck successfully outputs proper programs */
  @Test
  public void shouldBuildAndRun(EndToEndTestDescriptor test, EndToEndWorkspace workspace)
      throws Exception {
    checkSuccessfulBuildAndRun("Did not successfully %s.", test, workspace);
  }
}
