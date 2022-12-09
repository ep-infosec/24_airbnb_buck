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

package com.facebook.buck.android;

import com.facebook.buck.android.aapt.MiniAapt;
import com.facebook.buck.android.toolchain.AndroidPlatformTarget;
import com.facebook.buck.android.toolchain.AndroidTools;
import com.facebook.buck.core.cell.nameresolver.CellNameResolver;
import com.facebook.buck.core.description.arg.BuildRuleArg;
import com.facebook.buck.core.description.arg.HasDeclaredDeps;
import com.facebook.buck.core.description.attr.ImplicitDepsInferringDescription;
import com.facebook.buck.core.exceptions.HumanReadableException;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.Flavor;
import com.facebook.buck.core.model.FlavorSet;
import com.facebook.buck.core.model.Flavored;
import com.facebook.buck.core.model.InternalFlavor;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.BuildRuleCreationContextWithTargetGraph;
import com.facebook.buck.core.rules.BuildRuleParams;
import com.facebook.buck.core.rules.DescriptionWithTargetGraph;
import com.facebook.buck.core.rules.impl.MappedSymlinkTree;
import com.facebook.buck.core.sourcepath.PathSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.toolchain.ToolchainProvider;
import com.facebook.buck.core.toolchain.toolprovider.ToolProvider;
import com.facebook.buck.core.util.immutables.RuleArg;
import com.facebook.buck.io.file.MorePaths;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.util.stream.RichStream;
import com.facebook.buck.util.types.Either;
import com.facebook.buck.util.types.Pair;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Optional;
import org.immutables.value.Value;

public class AndroidResourceDescription
    implements DescriptionWithTargetGraph<AndroidResourceDescriptionArg>,
        Flavored,
        ImplicitDepsInferringDescription<AndroidResourceDescriptionArg> {

  private static final ImmutableSet<String> NON_ASSET_FILENAMES =
      ImmutableSet.of(
          ".gitkeep", ".svn", ".git", ".ds_store", ".scc", "cvs", "thumbs.db", "picasa.ini");

  private final AndroidBuckConfig androidBuckConfig;

  public static final Flavor RESOURCES_SYMLINK_TREE_FLAVOR =
      InternalFlavor.of("resources-symlink-tree");

  public static final Flavor ANDROID_RESOURCE_INDEX_FLAVOR =
      InternalFlavor.of("android-resource-index");

  @VisibleForTesting
  public static final Flavor ASSETS_SYMLINK_TREE_FLAVOR = InternalFlavor.of("assets-symlink-tree");

  public static final Flavor AAPT2_COMPILE_FLAVOR = InternalFlavor.of("aapt2_compile");

  private final ToolchainProvider toolchainProvider;

  public AndroidResourceDescription(
      ToolchainProvider toolchainProvider, AndroidBuckConfig androidBuckConfig) {
    this.toolchainProvider = toolchainProvider;
    this.androidBuckConfig = androidBuckConfig;
  }

  @Override
  public Class<AndroidResourceDescriptionArg> getConstructorArgType() {
    return AndroidResourceDescriptionArg.class;
  }

  @SuppressWarnings("PMD.PrematureDeclaration")
  @Override
  public BuildRule createBuildRule(
      BuildRuleCreationContextWithTargetGraph context,
      BuildTarget buildTarget,
      BuildRuleParams params,
      AndroidResourceDescriptionArg args) {
    ActionGraphBuilder graphBuilder = context.getActionGraphBuilder();
    ProjectFilesystem projectFilesystem = context.getProjectFilesystem();
    FlavorSet flavors = buildTarget.getFlavors();
    if (flavors.contains(RESOURCES_SYMLINK_TREE_FLAVOR)) {
      return createSymlinkTree(buildTarget, projectFilesystem, args.getRes(), "res");
    } else if (flavors.contains(ASSETS_SYMLINK_TREE_FLAVOR)) {
      return createSymlinkTree(buildTarget, projectFilesystem, args.getAssets(), "assets");
    }

    // Only allow android resource and library rules as dependencies.
    Optional<BuildRule> invalidDep =
        params.getDeclaredDeps().get().stream()
            .filter(rule -> !(rule instanceof AndroidResource || rule instanceof AndroidLibrary))
            .findFirst();
    if (invalidDep.isPresent()) {
      throw new HumanReadableException(
          buildTarget
              + " (android_resource): dependency "
              + invalidDep.get().getBuildTarget()
              + " ("
              + invalidDep.get().getType()
              + ") is not of type android_resource or android_library.");
    }

    // We don't handle the resources parameter well in `AndroidResource` rules, as instead of
    // hashing the contents of the entire resources directory, we try to filter out anything that
    // doesn't look like a resource.  This means when our resources are supplied from another rule,
    // we have to resort to some hackery to make sure things work correctly.
    Pair<Optional<MappedSymlinkTree>, Optional<SourcePath>> resInputs =
        collectInputSourcePaths(
            graphBuilder, buildTarget, RESOURCES_SYMLINK_TREE_FLAVOR, args.getRes());
    Pair<Optional<MappedSymlinkTree>, Optional<SourcePath>> assetsInputs =
        collectInputSourcePaths(
            graphBuilder, buildTarget, ASSETS_SYMLINK_TREE_FLAVOR, args.getAssets());

    if (flavors.contains(AAPT2_COMPILE_FLAVOR)) {
      Optional<SourcePath> resDir = resInputs.getSecond();
      Preconditions.checkArgument(
          resDir.isPresent(),
          "Tried to require rule %s, but no resource dir is preset.",
          buildTarget);
      AndroidPlatformTarget androidPlatformTarget =
          context
              .getToolchainProvider()
              .getByName(
                  AndroidPlatformTarget.DEFAULT_NAME,
                  buildTarget.getTargetConfiguration(),
                  AndroidPlatformTarget.class);
      ToolProvider aapt2ToolProvider = androidPlatformTarget.getAapt2ToolProvider();
      return new Aapt2Compile(
          buildTarget,
          projectFilesystem,
          graphBuilder,
          aapt2ToolProvider.resolve(graphBuilder, buildTarget.getTargetConfiguration()),
          resDir.get(),
          androidBuckConfig.getSkipCrunchPngsDefault().orElse(false),
          androidBuckConfig.getFailOnLegacyAaptErrors());
    }

    params =
        params.copyAppendingExtraDeps(
            Iterables.concat(
                resInputs
                    .getSecond()
                    .map(graphBuilder::filterBuildRuleInputs)
                    .orElse(ImmutableSet.of()),
                assetsInputs
                    .getSecond()
                    .map(graphBuilder::filterBuildRuleInputs)
                    .orElse(ImmutableSet.of())));

    if (flavors.contains(ANDROID_RESOURCE_INDEX_FLAVOR)) {
      Optional<SourcePath> resDir = resInputs.getSecond();
      Preconditions.checkArgument(
          resDir.isPresent(),
          "Tried to require rule %s, but no resource dir is preset.",
          buildTarget);
      return new AndroidResourceIndex(buildTarget, projectFilesystem, params, resDir.get());
    }

    return new AndroidResource(
        buildTarget,
        projectFilesystem,
        // We only propagate other AndroidResource rule dependencies, as these are
        // the only deps which should control whether we need to re-run the aapt_package
        // step.
        params.withDeclaredDeps(
            AndroidResourceHelper.androidResOnly(params.getDeclaredDeps().get())),
        graphBuilder,
        graphBuilder.getAllRules(args.getDeps()),
        resInputs.getSecond().orElse(null),
        resInputs.getFirst().map(MappedSymlinkTree::getLinks).orElse(ImmutableSortedMap.of()),
        args.getPackage().orElse(null),
        assetsInputs.getSecond().orElse(null),
        assetsInputs.getFirst().map(MappedSymlinkTree::getLinks).orElse(ImmutableSortedMap.of()),
        args.getManifest().orElse(null),
        args.getHasWhitelistedStrings(),
        args.getResourceUnion(),
        androidBuckConfig.isGrayscaleImageProcessingEnabled());
  }

  private MappedSymlinkTree createSymlinkTree(
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      Optional<Either<SourcePath, ImmutableSortedMap<String, SourcePath>>> symlinkAttribute,
      String outputDirName) {
    ImmutableMap<Path, SourcePath> links = ImmutableMap.of();
    if (symlinkAttribute.isPresent()) {
      if (symlinkAttribute.get().isLeft()) {
        // If our resources are coming from a `PathSourcePath`, we collect only the inputs we care
        // about and pass those in separately, so that that `AndroidResource` rule knows to only
        // hash these into it's rule key.
        // TODO(jakubzika): This is deprecated and should be disabled or removed.
        // Accessing the filesystem during rule creation is problematic because the accesses are
        // not cached or tracked in any way.
        Preconditions.checkArgument(
            symlinkAttribute.get().getLeft() instanceof PathSourcePath,
            "Resource or asset symlink tree can only be built for a PathSourcePath (%s for %s)",
            symlinkAttribute.get().getLeft(),
            buildTarget);
        PathSourcePath path = (PathSourcePath) symlinkAttribute.get().getLeft();
        links = collectInputFiles(path.getFilesystem(), path.getRelativePath());
      } else {
        links =
            RichStream.from(symlinkAttribute.get().getRight().entrySet())
                .map(e -> new AbstractMap.SimpleEntry<>(Paths.get(e.getKey()), e.getValue()))
                .filter(e -> isPossibleResourcePath(e.getKey()))
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
      }
    }
    Path symlinkTreeRoot =
        BuildTargetPaths.getGenPath(projectFilesystem, buildTarget, "%s").resolve(outputDirName);
    return new MappedSymlinkTree(
        "android_res", buildTarget, projectFilesystem, symlinkTreeRoot, links);
  }

  private static Pair<Optional<MappedSymlinkTree>, Optional<SourcePath>> collectInputSourcePaths(
      ActionGraphBuilder graphBuilder,
      BuildTarget resourceRuleTarget,
      Flavor symlinkTreeFlavor,
      Optional<Either<SourcePath, ImmutableSortedMap<String, SourcePath>>> attribute) {
    if (!attribute.isPresent()) {
      return new Pair<>(Optional.empty(), Optional.empty());
    }
    if (attribute.get().isLeft()) {
      SourcePath inputSourcePath = attribute.get().getLeft();
      if (!(inputSourcePath instanceof PathSourcePath)) {
        // If the resources are generated by a rule, we can't inspect the contents of the directory
        // in advance to create a symlink tree.  Instead, we have to pass the source path as is.
        return new Pair<>(Optional.empty(), Optional.of(inputSourcePath));
      }
    }
    BuildTarget symlinkTreeTarget = resourceRuleTarget.withFlavors(symlinkTreeFlavor);
    MappedSymlinkTree symlinkTree;
    symlinkTree = (MappedSymlinkTree) graphBuilder.requireRule(symlinkTreeTarget);
    return new Pair<>(Optional.of(symlinkTree), Optional.of(symlinkTree.getSourcePathToOutput()));
  }

  @VisibleForTesting
  ImmutableSortedMap<Path, SourcePath> collectInputFiles(
      ProjectFilesystem filesystem, Path inputDir) {
    ImmutableSortedMap.Builder<Path, SourcePath> paths = ImmutableSortedMap.naturalOrder();

    // We ignore the same files that mini-aapt and aapt ignore.
    FileVisitor<Path> fileVisitor =
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
            String dirName = dir.getFileName().toString();
            // Special case: directory starting with '_' as per aapt.
            if (dirName.charAt(0) == '_' || !isPossibleResourceName(dirName)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            String filename = file.getFileName().toString();
            if (isPossibleResourceName(filename)) {
              paths.put(MorePaths.relativize(inputDir, file), PathSourcePath.of(filesystem, file));
            }
            return FileVisitResult.CONTINUE;
          }
        };

    try {
      filesystem.walkRelativeFileTree(inputDir, fileVisitor);
    } catch (IOException e) {
      throw new HumanReadableException(
          e, "Error while searching for android resources in directory %s.", inputDir);
    }
    return paths.build();
  }

  @VisibleForTesting
  static boolean isPossibleResourcePath(Path filePath) {
    for (Path component : filePath) {
      if (!isPossibleResourceName(component.toString())) {
        return false;
      }
    }
    Path parentPath = filePath.getParent();
    if (parentPath != null) {
      for (Path component : parentPath) {
        if (component.toString().startsWith("_")) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean isPossibleResourceName(String fileOrDirName) {
    if (NON_ASSET_FILENAMES.contains(fileOrDirName.toLowerCase())) {
      return false;
    }
    if (fileOrDirName.charAt(fileOrDirName.length() - 1) == '~') {
      return false;
    }
    return !MiniAapt.IGNORED_FILE_EXTENSIONS.contains(Files.getFileExtension(fileOrDirName));
  }

  @Override
  public boolean hasFlavors(
      ImmutableSet<Flavor> flavors, TargetConfiguration toolchainTargetConfiguration) {
    if (flavors.isEmpty()) {
      return true;
    }

    if (flavors.size() == 1) {
      Flavor flavor = flavors.iterator().next();
      return flavor.equals(RESOURCES_SYMLINK_TREE_FLAVOR)
          || flavor.equals(ASSETS_SYMLINK_TREE_FLAVOR)
          || flavor.equals(AAPT2_COMPILE_FLAVOR)
          || flavor.equals(ANDROID_RESOURCE_INDEX_FLAVOR);
    }

    return false;
  }

  @Override
  public void findDepsForTargetFromConstructorArgs(
      BuildTarget buildTarget,
      CellNameResolver cellRoots,
      AndroidResourceDescriptionArg constructorArg,
      ImmutableCollection.Builder<BuildTarget> extraDepsBuilder,
      ImmutableCollection.Builder<BuildTarget> targetGraphOnlyDepsBuilder) {
    AndroidTools.addParseTimeDepsToAndroidTools(
        toolchainProvider, buildTarget, targetGraphOnlyDepsBuilder);
  }

  @RuleArg
  interface AbstractAndroidResourceDescriptionArg extends BuildRuleArg, HasDeclaredDeps {
    Optional<Either<SourcePath, ImmutableSortedMap<String, SourcePath>>> getRes();

    Optional<Either<SourcePath, ImmutableSortedMap<String, SourcePath>>> getAssets();

    Optional<Path> getProjectRes();

    Optional<Path> getProjectAssets();

    @Value.Default
    default boolean getHasWhitelistedStrings() {
      return false;
    }

    // For R.java.
    Optional<String> getPackage();

    Optional<SourcePath> getManifest();

    @Value.Default
    default boolean getResourceUnion() {
      return false;
    }
  }
}
