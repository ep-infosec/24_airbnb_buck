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

package com.facebook.buck.util.config;

import com.facebook.buck.core.filesystems.AbsPath;
import com.facebook.buck.core.util.log.Logger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utility functions for working with {@link Config}s. */
public final class Configs {
  private static final Logger LOG = Logger.get(Configs.class);

  private static final String DEFAULT_BUCK_CONFIG_FILE_NAME = ".buckconfig";
  public static final String DEFAULT_BUCK_CONFIG_OVERRIDE_FILE_NAME = ".buckconfig.local";
  private static final String DEFAULT_BUCK_CONFIG_DIRECTORY_NAME = ".buckconfig.d";

  private static final Path GLOBAL_BUCK_CONFIG_FILE_PATH = Paths.get("/etc/buckconfig");
  private static final Path GLOBAL_BUCK_CONFIG_DIRECTORY_PATH = Paths.get("/etc/buckconfig.d");

  private Configs() {}

  /** Convienence constructor */
  public static Config createDefaultConfig(Path root) throws IOException {
    return createDefaultConfig(root, RawConfig.of(ImmutableMap.of()));
  }

  /**
   * Generates a Buck config by merging configs from specified locations on disk.
   *
   * <p>In order:
   *
   * <ol>
   *   <li>Files (in lexicographical order) in {@code /etc/buckconfig.d}
   *   <li>{@code /etc/buckconfig}
   *   <li>Files (in lexicographical order) in {@code <HOME>/buckconfig.d}
   *   <li>Files (in lexicographical order) in {@code <PROJECT ROOT >/buckconfig.d}
   *   <li>{@code <HOME>/.buckconfig}
   *   <li>{@code <PROJECT ROOT>/.buckconfig}
   *   <li>{@code <PROJECT ROOT>/.buckconfig.local}
   *   <li>Any overrides (usually from the command line)
   * </ol>
   *
   * @param root Project root.
   * @param configOverrides Config overrides to merge in after the other sources.
   * @return the resulting {@code Config}.
   * @throws IOException on any exceptions during the underlying filesystem operations.
   */
  public static Config createDefaultConfig(Path root, RawConfig configOverrides)
      throws IOException {
    ImmutableList<Path> configFiles = getDefaultConfigurationFiles(root);

    return createDefaultConfig(root, configFiles, configOverrides);
  }

  /**
   * Generates a config by merging configs from specified locations on disk in order of the list
   * supplied. The elements at the end of the list have higher precedence.
   *
   * @param root Project root.
   * @param configFiles the files to load configs from, in order of precedence.
   * @param configOverrides Config overrides to merge in after the other sources.
   * @return the resulting {@code Config}.
   * @throws IOException on any exceptions during the underlying filesystem operations.
   */
  public static Config createDefaultConfig(
      Path root, ImmutableList<Path> configFiles, RawConfig configOverrides) throws IOException {
    LOG.debug("Loading configuration for %s", root);

    Config config = createConfigFromFiles(root, configFiles);

    LOG.debug("Adding configuration overrides %s", configOverrides);
    return config.overrideWith(new Config(configOverrides));
  }

  /**
   * Generates a config by merging configs from specified locations on disk in order of the list
   * supplied. The elements at the end of the list have higher precedence.
   *
   * @param root Project root.
   * @param configFiles the files to load configs from, in order of precedence.
   * @return the resulting {@code RawConfig}.
   * @throws IOException on any exceptions during the underlying filesystem operations.
   */
  public static Config createConfigFromFiles(Path root, ImmutableList<Path> configFiles)
      throws IOException {
    Path configFile = getMainConfigurationFile(root);

    ImmutableMap.Builder<Path, RawConfig> configsMapBuilder = ImmutableMap.builder();
    RawConfig.Builder builder = RawConfig.builder();
    for (Path path : configFiles) {
      ImmutableMap<String, ImmutableMap<String, String>> parsedConfiguration =
          Inis.read(path.toUri().toURL());
      if (path.equals(configFile)) {
        LOG.debug("Loaded project configuration file %s", path);
        LOG.verbose("Contents of %s: %s", path, parsedConfiguration);
      } else {
        LOG.debug("Loaded a configuration file %s, %s", path, parsedConfiguration);
      }
      builder.putAll(parsedConfiguration);
      configsMapBuilder.put(path, RawConfig.of(parsedConfiguration));
    }
    return new Config(builder.build(), configsMapBuilder.build());
  }

  private static ImmutableSortedSet<Path> listFiles(Path root) throws IOException {
    if (!Files.isDirectory(root)) {
      return ImmutableSortedSet.of();
    }
    try (DirectoryStream<Path> directory = Files.newDirectoryStream(root)) {
      return ImmutableSortedSet.<Path>naturalOrder().addAll(directory.iterator()).build();
    }
  }

  public static Path getMainConfigurationFile(Path root) {
    return root.resolve(DEFAULT_BUCK_CONFIG_FILE_NAME);
  }

  /**
   * Gets all configuration files that should be used for parsing the project configuration
   *
   * @param root The root of the project to search
   * @return Files that exist that conform to buck's configuration file precedence. Settings from
   *     files at the end of this list should override ones that come before.
   */
  public static ImmutableList<Path> getDefaultConfigurationFiles(Path root) throws IOException {
    ImmutableList.Builder<Path> configFileBuilder = ImmutableList.builder();

    configFileBuilder.addAll(listFiles(GLOBAL_BUCK_CONFIG_DIRECTORY_PATH));
    if (Files.isRegularFile(GLOBAL_BUCK_CONFIG_FILE_PATH)) {
      configFileBuilder.add(GLOBAL_BUCK_CONFIG_FILE_PATH);
    }

    Path homeDirectory = Paths.get(System.getProperty("user.home"));
    Path userConfigDir = homeDirectory.resolve(DEFAULT_BUCK_CONFIG_DIRECTORY_NAME);
    configFileBuilder.addAll(listFiles(userConfigDir));

    Path projectConfigDir = root.resolve(DEFAULT_BUCK_CONFIG_DIRECTORY_NAME);
    configFileBuilder.addAll(listFiles(projectConfigDir));

    Path userConfigFile = homeDirectory.resolve(DEFAULT_BUCK_CONFIG_FILE_NAME);
    if (Files.isRegularFile(userConfigFile)) {
      configFileBuilder.add(userConfigFile);
    }

    configFileBuilder.addAll(getRepoConfigurationFiles(root));

    return configFileBuilder.build();
  }

  /** Abs-path version of {@link #getDefaultConfigurationFiles(Path)}. */
  public static ImmutableList<Path> getDefaultConfigurationFiles(AbsPath root) throws IOException {
    return getDefaultConfigurationFiles(root.getPath());
  }

  /**
   * Gets the repository defined configuration files (excluding configuration directories) that
   * should be used for parsing the project configuration.
   *
   * @param root The root of the project to search
   * @return Files that exist that conform to buck's configuration file precedence. Settings from
   *     files at the end of this list should override ones that come before.
   */
  public static ImmutableList<Path> getRepoConfigurationFiles(Path root) {
    ImmutableList.Builder<Path> configFileBuilder = ImmutableList.builder();

    Path configFile = getMainConfigurationFile(root);
    if (Files.isRegularFile(configFile)) {
      configFileBuilder.add(configFile);
    }
    Path overrideConfigFile = root.resolve(DEFAULT_BUCK_CONFIG_OVERRIDE_FILE_NAME);
    if (Files.isRegularFile(overrideConfigFile)) {
      configFileBuilder.add(overrideConfigFile);
    }

    return configFileBuilder.build();
  }

  public static ImmutableMap<String, ImmutableMap<String, String>> parseConfigFile(Path file)
      throws IOException {
    ImmutableMap<String, ImmutableMap<String, String>> parsedConfiguration =
        Inis.read(file.toUri().toURL());
    LOG.debug("Loaded a configuration file %s: %s", file, parsedConfiguration);
    return parsedConfiguration;
  }
}
