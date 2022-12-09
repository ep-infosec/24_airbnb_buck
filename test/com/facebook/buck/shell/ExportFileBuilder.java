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

import com.facebook.buck.core.config.BuckConfig;
import com.facebook.buck.core.config.FakeBuckConfig;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.targetgraph.AbstractNodeBuilder;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import java.util.Optional;
import javax.annotation.Nullable;

public class ExportFileBuilder
    extends AbstractNodeBuilder<
        ExportFileDescriptionArg.Builder,
        ExportFileDescriptionArg,
        ExportFileDescription,
        ExportFile> {
  public ExportFileBuilder(BuildTarget target) {
    super(new ExportFileDescription(FakeBuckConfig.builder().build()), target);
  }

  public ExportFileBuilder(BuildTarget target, ProjectFilesystem filesystem) {
    super(
        new ExportFileDescription(FakeBuckConfig.builder().setFilesystem(filesystem).build()),
        target,
        filesystem);
  }

  public ExportFileBuilder(BuckConfig buckConfig, BuildTarget target) {
    super(new ExportFileDescription(buckConfig), target);
  }

  public ExportFileBuilder setSrc(@Nullable SourcePath path) {
    getArgForPopulating().setSrc(Optional.ofNullable(path));
    return this;
  }

  public ExportFileBuilder setOut(String out) {
    getArgForPopulating().setOut(out);
    return this;
  }

  public ExportFileBuilder setMode(ExportFileDescription.Mode mode) {
    getArgForPopulating().setMode(mode);
    return this;
  }
}
