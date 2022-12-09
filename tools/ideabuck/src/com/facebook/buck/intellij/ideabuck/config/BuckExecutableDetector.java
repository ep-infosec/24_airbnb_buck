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

package com.facebook.buck.intellij.ideabuck.config;

import com.facebook.buck.intellij.ideabuck.util.ExecutableFinder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Programmatic discovery of executables needed by the Ideabuck plugin. */
public interface BuckExecutableDetector {

  String getAdbExecutable();

  String getBuildifierExecutable();

  String getBuildozerExecutable();

  String getBuckExecutable();

  String getNamedExecutable(String name);

  static BuckExecutableDetector newInstance() {
    return new Impl();
  }

  String DEFAULT_BUCK_NAME = "buck";
  String DEFAULT_BUILDIFIER_NAME = "buildifier";
  String DEFAULT_BUILDOZER_NAME = "buildozer";
  String DEFAULT_ADB_NAME = "adb";

  /** Default implementation. */
  class Impl implements BuckExecutableDetector {
    private static final Path BUCK_EXECUTABLE = Paths.get(DEFAULT_BUCK_NAME);
    private static final Path BUILDIFIER_EXECUTABLE = Paths.get(DEFAULT_BUILDIFIER_NAME);
    private static final Path BUILDOZER_EXECUTABLE = Paths.get(DEFAULT_BUILDOZER_NAME);
    private static final Path ADB_EXECUTABLE = Paths.get(DEFAULT_ADB_NAME);
    private static final ExecutableFinder EXECUTABLE_FINDER = new ExecutableFinder();

    private Map<String, String> env;

    public Impl() {
      this(ImmutableMap.copyOf(System.getenv()));
    }

    @VisibleForTesting
    Impl(ImmutableMap<String, String> env) {
      this.env = env;
    }

    @Override
    public String getBuckExecutable() {
      return getExecutable(BUCK_EXECUTABLE, env);
    }

    @Override
    public String getAdbExecutable() {
      Map<String, String> modifiedEnv = new HashMap<>(env);
      appendAndroidSdkPlatformToolsAtEndOfPath(modifiedEnv);
      return getExecutable(ADB_EXECUTABLE, ImmutableMap.copyOf(modifiedEnv));
    }

    @Override
    public String getBuildifierExecutable() {
      return getExecutable(BUILDIFIER_EXECUTABLE, env);
    }

    @Override
    public String getBuildozerExecutable() {
      return getExecutable(BUILDOZER_EXECUTABLE, env);
    }

    private void appendAndroidSdkPlatformToolsAtEndOfPath(Map<String, String> env) {
      String androidSdk = env.get("ANDROID_SDK");
      if (androidSdk == null) {
        return;
      }
      Path androidAdkPlatformTools = Paths.get(androidSdk).resolve("platform-tools");
      String path = env.get("PATH");
      if (path == null) {
        path = "";
      } else {
        path = path + File.pathSeparator;
      }
      path += androidAdkPlatformTools.toAbsolutePath().toString();
      env.put("PATH", path);
    }

    @Override
    public String getNamedExecutable(String name) {
      return getExecutable(Paths.get(name), env);
    }

    public String getExecutable(Path suggestedExecutable, Map<String, String> env) {
      return EXECUTABLE_FINDER
          .getExecutable(suggestedExecutable, ImmutableMap.copyOf(env))
          .toString();
    }
  }
}
