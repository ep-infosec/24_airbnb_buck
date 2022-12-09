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

package com.facebook.buck.features.python.toolchain.impl;

import com.facebook.buck.core.toolchain.ToolchainDescriptor;
import com.facebook.buck.core.toolchain.ToolchainSupplier;
import com.facebook.buck.features.python.toolchain.PexToolProvider;
import com.facebook.buck.features.python.toolchain.PythonInterpreter;
import com.facebook.buck.features.python.toolchain.PythonPlatformsProvider;
import java.util.Arrays;
import java.util.Collection;
import org.pf4j.Extension;

@Extension
public class PythonToolchainsSupplier implements ToolchainSupplier {

  @Override
  public Collection<ToolchainDescriptor<?>> getToolchainDescriptor() {
    return Arrays.asList(
        ToolchainDescriptor.of(
            PexToolProvider.DEFAULT_NAME, PexToolProvider.class, PexToolProviderFactory.class),
        ToolchainDescriptor.of(
            PythonInterpreter.DEFAULT_NAME,
            PythonInterpreter.class,
            PythonInterpreterFactory.class),
        ToolchainDescriptor.of(
            PythonPlatformsProvider.DEFAULT_NAME,
            PythonPlatformsProvider.class,
            PythonPlatformsProviderFactory.class));
  }
}
