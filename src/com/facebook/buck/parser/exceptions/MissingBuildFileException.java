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

package com.facebook.buck.parser.exceptions;

import java.nio.file.Path;

/** Thrown when build file is missing for the provided target */
public class MissingBuildFileException extends BuildTargetException {

  public MissingBuildFileException(String spec, Path buildFile) {
    super(String.format("No build file at %s when resolving target %s.", buildFile, spec));
  }

  @Override
  public String getHumanReadableErrorMessage() {
    return getMessage();
  }
}
