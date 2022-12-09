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

package com.facebook.buck.cxx;

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.build.execution.context.ExecutionContext;
import com.facebook.buck.core.cell.name.CanonicalCellName;
import com.facebook.buck.core.filesystems.AbsPath;
import com.facebook.buck.core.rules.resolver.impl.TestActionGraphBuilder;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.facebook.buck.rules.args.Arg;
import com.facebook.buck.rules.args.StringArg;
import com.facebook.buck.step.TestExecutionContext;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.hamcrest.Matchers;
import org.junit.Test;

public class CxxWriteArgsToFileStepTest {

  @Test
  public void cxxWriteArgsToFilePassesLinkerOptionsViaArgFile()
      throws IOException, InterruptedException {
    ProjectFilesystem projectFilesystem = new FakeProjectFilesystem();
    AbsPath fileListPath =
        projectFilesystem
            .getRootPath()
            .resolve("/tmp/cxxWriteArgsToFilePassesLinkerOptionsViaArgFile.txt");

    runTestForArgFilePathAndOutputPath(
        fileListPath.getPath(),
        Optional.empty(),
        ImmutableList.of(StringArg.of("-dummy"), StringArg.of("\"")),
        ImmutableList.of("-dummy", "\""),
        CanonicalCellName.rootCell());
  }

  @Test
  public void cxxWriteArgsToFileCreatesDirectoriesIfNeeded()
      throws IOException, InterruptedException {
    ProjectFilesystem projectFilesystem = FakeProjectFilesystem.createRealTempFilesystem();
    AbsPath fileListPath =
        projectFilesystem.getRootPath().resolve("unexisting_parent_folder/filelist.txt");

    runTestForArgFilePathAndOutputPath(
        fileListPath.getPath(),
        Optional.of(input -> "foo".equals(input) ? "bar" : input),
        ImmutableList.of(StringArg.of("-dummy"), StringArg.of("foo")),
        ImmutableList.of("-dummy", "bar"),
        CanonicalCellName.rootCell());

    // cleanup after test
    Files.deleteIfExists(fileListPath.getPath());
    Files.deleteIfExists(fileListPath.getParent().getPath());
  }

  private void runTestForArgFilePathAndOutputPath(
      Path argFilePath,
      Optional<Function<String, String>> escaper,
      ImmutableList<Arg> inputArgs,
      ImmutableList<String> expectedArgFileContents,
      CanonicalCellName currentCellName)
      throws IOException {
    ExecutionContext context = TestExecutionContext.newInstance();

    SourcePathResolverAdapter sourcePathResolverAdapter =
        new TestActionGraphBuilder().getSourcePathResolver();
    CxxWriteArgsToFileStep step =
        CxxWriteArgsToFileStep.create(
            argFilePath, inputArgs, escaper, currentCellName, sourcePathResolverAdapter, false);

    step.execute(context);

    assertThat(Files.exists(argFilePath), Matchers.equalTo(true));

    checkContentsOfFile(argFilePath, expectedArgFileContents);
    Files.deleteIfExists(argFilePath);
  }

  private void checkContentsOfFile(Path file, ImmutableList<String> contents) throws IOException {
    List<String> fileContents = Files.readAllLines(file, StandardCharsets.UTF_8);
    assertThat(fileContents, Matchers.equalTo(contents));
  }
}
