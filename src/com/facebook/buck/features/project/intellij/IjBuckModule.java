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

package com.facebook.buck.features.project.intellij;

import com.facebook.buck.core.module.BuckModule;
import com.facebook.buck.features.filegroup.FilegroupModule;
import com.facebook.buck.features.python.PythonModule;
import com.facebook.buck.features.zip.rules.ZipRulesModule;

/** Module with project generator for IntelliJ. */
@BuckModule(dependencies = {FilegroupModule.class, PythonModule.class, ZipRulesModule.class})
public class IjBuckModule {}
