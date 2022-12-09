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

package com.facebook.buck.features.ocaml;

import com.facebook.buck.core.model.FlavorDomain;
import com.facebook.buck.core.toolchain.tool.impl.CommandTool;
import com.facebook.buck.core.toolchain.toolprovider.impl.ConstantToolProvider;
import com.facebook.buck.cxx.toolchain.CxxPlatformUtils;

public class OcamlTestUtils {

  public static final OcamlPlatform DEFAULT_PLATFORM =
      ImmutableOcamlPlatform.builder()
          .setOcamlCompiler(new ConstantToolProvider(new CommandTool.Builder().build()))
          .setOcamlDepTool(new ConstantToolProvider(new CommandTool.Builder().build()))
          .setYaccCompiler(new ConstantToolProvider(new CommandTool.Builder().build()))
          .setLexCompiler(new ConstantToolProvider(new CommandTool.Builder().build()))
          .setOcamlBytecodeCompiler(new ConstantToolProvider(new CommandTool.Builder().build()))
          .setOcamlDebug(new ConstantToolProvider(new CommandTool.Builder().build()))
          .setCCompiler(CxxPlatformUtils.DEFAULT_PLATFORM.getCc())
          .setCxxCompiler(CxxPlatformUtils.DEFAULT_PLATFORM.getCxx())
          .setCPreprocessor(CxxPlatformUtils.DEFAULT_PLATFORM.getCpp())
          .setCxxPlatform(CxxPlatformUtils.DEFAULT_PLATFORM)
          .build();

  public static final FlavorDomain<OcamlPlatform> DEFAULT_PLATFORMS =
      FlavorDomain.of("OCaml Platform", DEFAULT_PLATFORM);
}
