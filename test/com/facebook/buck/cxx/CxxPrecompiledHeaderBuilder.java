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

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.targetgraph.AbstractNodeBuilder;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;

public class CxxPrecompiledHeaderBuilder
    extends AbstractNodeBuilder<
        CxxPrecompiledHeaderDescriptionArg.Builder,
        CxxPrecompiledHeaderDescriptionArg,
        CxxPrecompiledHeaderDescription,
        CxxPrecompiledHeaderTemplate> {

  protected CxxPrecompiledHeaderBuilder(BuildTarget target) {
    super(new CxxPrecompiledHeaderDescription(), target);
  }

  protected CxxPrecompiledHeaderBuilder(BuildTarget target, ProjectFilesystem projectFilesystem) {
    super(new CxxPrecompiledHeaderDescription(), target, projectFilesystem);
  }

  public static CxxPrecompiledHeaderBuilder createBuilder(BuildTarget target) {
    return new CxxPrecompiledHeaderBuilder(target);
  }

  public static CxxPrecompiledHeaderBuilder createBuilder(
      BuildTarget target, ProjectFilesystem projectFilesystem) {
    return new CxxPrecompiledHeaderBuilder(target, projectFilesystem);
  }

  public CxxPrecompiledHeaderBuilder setSrc(SourcePath sourcePath) {
    getArgForPopulating().setSrc(sourcePath);
    return this;
  }
}
