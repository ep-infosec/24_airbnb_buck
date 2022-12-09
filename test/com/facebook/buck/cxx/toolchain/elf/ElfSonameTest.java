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

package com.facebook.buck.cxx.toolchain.elf;

import static org.junit.Assert.assertThat;

import com.facebook.buck.cxx.ElfFile;
import com.facebook.buck.testutil.TemporaryPaths;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.TestDataHelper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ElfSonameTest {

  @Rule public TemporaryPaths tmp = new TemporaryPaths();

  private ProjectWorkspace workspace;

  @Before
  public void setUp() throws IOException {
    workspace = TestDataHelper.createProjectWorkspaceForScenario(this, "samples", tmp);
    workspace.setUp();
  }

  @Test
  public void libraryWithoutSonameHasNoSoname() throws IOException {
    Path elfFilePath = workspace.resolve("libfoo.so");
    Elf elf = ElfFile.mapReadOnly(elfFilePath);
    assertThat(ElfFile.getSoname(elf), Matchers.equalTo(Optional.empty()));
  }

  @Test
  public void staticExecutableHasNoSoname() throws IOException {
    Path elfFilePath = workspace.resolve("static_executable");
    Elf elf = ElfFile.mapReadOnly(elfFilePath);
    assertThat(ElfFile.getSoname(elf), Matchers.equalTo(Optional.empty()));
  }

  @Test
  public void libraryWithCustomSonameHasSoname() throws IOException {
    Path elfFilePath = workspace.resolve("soname.so");
    Elf elf = ElfFile.mapReadOnly(elfFilePath);
    assertThat(ElfFile.getSoname(elf), Matchers.equalTo(Optional.of("custom_soname.dso")));
  }
}
