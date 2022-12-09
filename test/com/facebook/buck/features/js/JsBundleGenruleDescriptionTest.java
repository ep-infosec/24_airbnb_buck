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

package com.facebook.buck.features.js;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import com.facebook.buck.android.packageable.AndroidPackageableCollector;
import com.facebook.buck.apple.AppleBundleResources;
import com.facebook.buck.apple.SourcePathWithAppleBundleDestination;
import com.facebook.buck.core.build.buildable.context.FakeBuildableContext;
import com.facebook.buck.core.build.context.BuildContext;
import com.facebook.buck.core.build.context.FakeBuildContext;
import com.facebook.buck.core.config.FakeBuckConfig;
import com.facebook.buck.core.exceptions.HumanReadableException;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.facebook.buck.core.model.Flavor;
import com.facebook.buck.core.model.InternalFlavor;
import com.facebook.buck.core.model.impl.BuildPaths;
import com.facebook.buck.core.model.impl.BuildTargetPaths;
import com.facebook.buck.core.model.targetgraph.TargetNode;
import com.facebook.buck.core.rules.ActionGraphBuilder;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.BuildRuleResolver;
import com.facebook.buck.core.rules.resolver.impl.TestActionGraphBuilder;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.core.toolchain.impl.ToolchainProviderBuilder;
import com.facebook.buck.io.BuildCellRelativePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.rules.macros.LocationMacro;
import com.facebook.buck.rules.macros.StringWithMacros;
import com.facebook.buck.rules.macros.StringWithMacrosUtils;
import com.facebook.buck.rules.modern.DefaultOutputPathResolver;
import com.facebook.buck.sandbox.NoSandboxExecutionStrategy;
import com.facebook.buck.shell.GenruleBuildable;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.RmStep;
import com.facebook.buck.util.types.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsBundleGenruleDescriptionTest {
  private static final BuildTarget genruleTarget =
      BuildTargetFactory.newInstance("//:bundle-genrule");
  private static final BuildTarget defaultBundleTarget =
      BuildTargetFactory.newInstance("//js:bundle");
  private TestSetup setup;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    setUp(new Flavor[0]);
  }

  private void setUp(Flavor... extraFlavors) {
    setUp(defaultBundleTarget, extraFlavors);
  }

  private void setUp(BuildTarget bundleTarget, Flavor... extraFlavors) {
    setUpWithOptions(builderOptions(bundleTarget), extraFlavors);
  }

  private void setUpWithRewriteSourceMap(Flavor... extraFlavors) {
    setUpWithOptions(builderOptions().rewriteSourcemap(), extraFlavors);
  }

  private void setUpWithRewriteMiscDir(Flavor... extraFlavors) {
    setUpWithOptions(builderOptions().rewriteMisc(), extraFlavors);
  }

  private void setUpWithRewriteDepsFile(Flavor... extraFlavors) {
    setUpWithOptions(builderOptions().rewriteDepsFile(), extraFlavors);
  }

  private void setupWithSkipResources(Flavor... extraFlavors) {
    setUpWithOptions(builderOptions().skipResources(), extraFlavors);
  }

  private void setUpWithOptions(JsBundleGenruleBuilder.Options options, Flavor... extraFlavors) {
    JsTestScenario scenario =
        JsTestScenario.builder().bundleWithDeps(options.jsBundle).bundleGenrule(options).build();

    setup =
        new TestSetup(scenario, genruleTarget.withAppendedFlavors(extraFlavors), options.jsBundle);
  }

  @Test
  public void dependsOnSpecifiedJsBundle() {
    assertThat(setup.genrule().getBuildDeps(), hasItem(setup.jsBundle()));
  }

  @Test
  public void forwardsFlavorsToJsBundle() {
    Flavor[] extraFlavors = {JsFlavors.IOS, JsFlavors.RELEASE};
    setUp(defaultBundleTarget.withAppendedFlavors(JsFlavors.RAM_BUNDLE_INDEXED), extraFlavors);
    assertThat(setup.genrule().getBuildDeps(), hasItem(setup.jsBundle(extraFlavors)));
  }

  @Test
  public void failsForNonJsBundleTargets() {
    thrown.expect(HumanReadableException.class);
    thrown.expectMessage(
        equalTo(
            "The 'js_bundle' argument of //:bundle-genrule, //js:bundle, must correspond to a js_bundle() rule."));
    JsTestScenario scenario = JsTestScenario.builder().arbitraryRule(defaultBundleTarget).build();

    new JsBundleGenruleBuilder(genruleTarget, defaultBundleTarget, scenario.filesystem)
        .build(scenario.graphBuilder, scenario.filesystem);
  }

  @Test
  public void underlyingJsBundleIsARuntimeDep() {
    assertArrayEquals(
        new BuildTarget[] {defaultBundleTarget},
        setup.genrule().getRuntimeDeps(setup.graphBuilder()).toArray());
  }

  @Test
  public void hasSameBundleNameAsJsBundle() {
    assertEquals(setup.jsBundle().getBundleName(), setup.genrule().getBundleName());
  }

  @Test
  public void addsBundleAndBundleNameAsEnvironmentVariable() {
    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(
        env,
        hasEntry(
            "JS_DIR",
            pathResolver.getAbsolutePath(setup.jsBundle().getSourcePathToOutput()).toString()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME", setup.jsBundle().getBundleName()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME_OUT", setup.jsBundle().getBundleName()));
  }

  @Test
  public void allowsBundleRenaming() {
    String renamedBundle = "bundle-renamed.abc";
    setUpWithOptions(builderOptions().bundleName(renamedBundle));

    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(setup.genrule().getBundleName(), equalTo(renamedBundle));
    assertThat(env, hasEntry("JS_BUNDLE_NAME", setup.jsBundle().getBundleName()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME_OUT", renamedBundle));
  }

  @Test
  public void allowsFlavorDependentRenaming() {
    String releaseFlavorBundleName = "release.bundle";
    ImmutableList<Pair<Flavor, String>> bundleNamesForFlavors =
        ImmutableList.of(
            new Pair<>(InternalFlavor.of("android"), "android.bundle"),
            new Pair<>(InternalFlavor.of("release"), releaseFlavorBundleName));

    setUpWithOptions(builderOptions().bundleNameForFlavor(bundleNamesForFlavors));

    JsBundleGenrule bundleGenrule = setup.genrule(JsFlavors.RELEASE);
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(bundleGenrule);

    assertThat(bundleGenrule.getBundleName(), equalTo(releaseFlavorBundleName));
    assertThat(env, hasEntry("JS_BUNDLE_NAME", setup.jsBundle().getBundleName()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME_OUT", releaseFlavorBundleName));
  }

  @Test
  public void flavorDependentNamesFallBackToNameOfUnderlyingBundle() {
    ImmutableList<Pair<Flavor, String>> bundleNamesForFlavors =
        ImmutableList.of(
            new Pair<>(InternalFlavor.of("android"), "android.bundle"),
            new Pair<>(InternalFlavor.of("release"), "release.bundle"));
    setUpWithOptions(builderOptions().bundleNameForFlavor(bundleNamesForFlavors));

    JsBundleGenrule bundleGenrule = setup.genrule(JsFlavors.IOS);
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(bundleGenrule);

    assertThat(bundleGenrule.getBundleName(), equalTo(setup.jsBundle().getBundleName()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME", setup.jsBundle().getBundleName()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME_OUT", setup.jsBundle().getBundleName()));
  }

  @Test
  public void flavorDependentNamesFallBackToSpecifiedBundleName() {
    String renamedBundle = "bundle-renamed.abc";
    ImmutableList<Pair<Flavor, String>> bundleNamesForFlavors =
        ImmutableList.of(
            new Pair<>(InternalFlavor.of("android"), "android.bundle"),
            new Pair<>(InternalFlavor.of("release"), "release.bundle"));
    setUpWithOptions(
        builderOptions().bundleName(renamedBundle).bundleNameForFlavor(bundleNamesForFlavors));

    JsBundleGenrule bundleGenrule = setup.genrule(JsFlavors.IOS);
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(bundleGenrule);

    assertThat(bundleGenrule.getBundleName(), equalTo(renamedBundle));
    assertThat(env, hasEntry("JS_BUNDLE_NAME", setup.jsBundle().getBundleName()));
    assertThat(env, hasEntry("JS_BUNDLE_NAME_OUT", renamedBundle));
  }

  @Test
  public void exposesReleaseFlavorAsEnvironmentVariable() {
    setUp(JsFlavors.RELEASE);
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(env, hasEntry("RELEASE", "1"));
  }

  @Test
  public void withoutReleaseFlavorEnvVariableIsEmpty() {
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(env, hasEntry("RELEASE", ""));
  }

  @Test
  public void exposesAndroidFlavorAsEnvironmentVariable() {
    setUp(JsFlavors.ANDROID);
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(env, hasEntry("PLATFORM", "android"));
  }

  @Test
  public void exposesIosFlavorAsEnvironmentVariable() {
    setUp(JsFlavors.IOS);
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(env, hasEntry("PLATFORM", "ios"));
  }

  @Test
  public void withoutPlatformFlavorEnvVariableIsEmpty() {
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(env, hasEntry("PLATFORM", ""));
  }

  @Test
  public void addsResourcesDirectoryAsEnvironmentVariable() {
    setUp();

    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> builder = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(
        builder,
        hasEntry(
            "RES_DIR",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToResources()).toString()));
  }

  @Test
  public void addsMiscDirectoryAsEnvironmentVariable() {
    setUp();

    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> builder = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(
        builder,
        hasEntry(
            "MISC_DIR",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToMisc()).toString()));
  }

  @Test
  public void exportsResourcesOfJsBundle() {
    assertEquals(
        setup.jsBundle().getSourcePathToResources(), setup.genrule().getSourcePathToResources());
  }

  @Test
  public void delegatesAndroidPackageableBehaviorToBundle() {
    setUp(defaultBundleTarget.withAppendedFlavors(JsFlavors.ANDROID));

    JsBundleAndroid jsBundleAndroid = setup.jsBundleAndroid();
    BuildRuleResolver ruleResolver = new TestActionGraphBuilder();
    assertEquals(
        jsBundleAndroid.getRequiredPackageables(ruleResolver),
        setup.genrule().getRequiredPackageables(ruleResolver));

    AndroidPackageableCollector collector = packageableCollectorMock(setup);
    setup.genrule().addToCollector(collector);
    verify(collector);
  }

  @Test
  public void doesNotExposePackageablesWithSkipResources() {
    setupWithSkipResources(JsFlavors.ANDROID);

    assertEquals(
        ImmutableList.of(), setup.genrule().getRequiredPackageables(new TestActionGraphBuilder()));
    AndroidPackageableCollector collector = packageableCollectorMock(setup);
    setup.genrule().addToCollector(collector);
    verify(collector);
  }

  @Test
  public void returnsNothingIfUnderlyingBundleIsNotForAndroid() {
    assertEquals(
        ImmutableList.of(), setup.genrule().getRequiredPackageables(new TestActionGraphBuilder()));
  }

  @Test
  public void addAppleBundleResourcesIsDelegatedToUnderlyingBundle() {
    AppleBundleResources.Builder genruleBuilder = AppleBundleResources.builder();
    new JsBundleGenruleDescription(
            new ToolchainProviderBuilder().build(),
            FakeBuckConfig.builder().build(),
            new NoSandboxExecutionStrategy())
        .addAppleBundleResources(
            genruleBuilder,
            setup.targetNode(),
            setup.rule().getProjectFilesystem(),
            setup.graphBuilder());

    AppleBundleResources expected =
        AppleBundleResources.builder()
            .addDirsContainingResourceDirs(
                SourcePathWithAppleBundleDestination.of(setup.genrule().getSourcePathToOutput()))
            .addDirsContainingResourceDirs(
                SourcePathWithAppleBundleDestination.of(
                    setup.jsBundle().getSourcePathToResources()))
            .build();
    assertEquals(expected, genruleBuilder.build());
  }

  @Test
  public void addAppleBundleWithSkipResourcesExposesOnlyJS() {
    setupWithSkipResources();

    AppleBundleResources.Builder resourcesBuilder = AppleBundleResources.builder();
    new JsBundleGenruleDescription(
            new ToolchainProviderBuilder().build(),
            FakeBuckConfig.builder().build(),
            new NoSandboxExecutionStrategy())
        .addAppleBundleResources(
            resourcesBuilder,
            setup.targetNode(),
            setup.rule().getProjectFilesystem(),
            setup.graphBuilder());

    AppleBundleResources expected =
        AppleBundleResources.builder()
            .addDirsContainingResourceDirs(
                SourcePathWithAppleBundleDestination.of(setup.rule().getSourcePathToOutput()))
            .build();
    assertEquals(expected, resourcesBuilder.build());
  }

  @Test
  public void exportsSourceMapOfJsBundle() {
    assertEquals(
        setup.jsBundle().getSourcePathToSourceMap(), setup.genrule().getSourcePathToSourceMap());
  }

  @Test
  public void exportsMiscOfJsBundle() {
    assertEquals(setup.jsBundle().getSourcePathToMisc(), setup.genrule().getSourcePathToMisc());
  }

  @Test
  public void exposesSourceMapOfJsBundleWithSpecialFlavor() {
    setUp(JsFlavors.SOURCE_MAP);

    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.jsBundle().getSourcePathToSourceMap()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void exposesMiscOfJsBundleWithSpecialFlavor() {
    setUp(JsFlavors.MISC);

    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.jsBundle().getSourcePathToMisc()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void createsJsDir() {
    JsBundleGenrule genrule = setup.genrule();
    BuildContext context = FakeBuildContext.withSourcePathResolver(sourcePathResolver());
    FakeBuildableContext buildableContext = new FakeBuildableContext();
    ImmutableList<Step> buildSteps =
        ImmutableList.copyOf(genrule.getBuildSteps(context, buildableContext));

    MkdirStep expectedStep =
        MkdirStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(),
                genrule.getProjectFilesystem(),
                context.getSourcePathResolver().getRelativePath(genrule.getSourcePathToOutput())));
    assertThat(buildSteps, hasItem(expectedStep));

    int mkJsDirIdx = buildSteps.indexOf(expectedStep);
    assertThat(buildSteps.subList(mkJsDirIdx, buildSteps.size()), not(hasItem(any(RmStep.class))));
  }

  @Test
  public void dependsOnTargetsInMacros() {
    BuildTarget locationTarget = BuildTargetFactory.newInstance("//location:target");

    JsTestScenario scenario =
        JsTestScenario.builder()
            .bundleWithDeps(defaultBundleTarget)
            .arbitraryRule(locationTarget)
            .bundleGenrule(
                builderOptions(
                    StringWithMacrosUtils.format("%s", LocationMacro.of(locationTarget))))
            .build();

    BuildRule buildRule = scenario.graphBuilder.requireRule(genruleTarget);
    assertThat(buildRule.getBuildDeps(), hasItem(scenario.graphBuilder.getRule(locationTarget)));
  }

  @Test
  public void exposesRewrittenSourceMap() {
    setUpWithRewriteSourceMap();

    JsBundleGenrule genrule = setup.genrule();
    assertEquals(
        JsUtil.relativeToOutputRoot(
            genrule.getBuildTarget(),
            genrule.getProjectFilesystem(),
            JsUtil.getSourcemapPath(genrule)),
        genrule.getSourcePathToSourceMap());
  }

  @Test
  public void addsSourceMapAsEnvironmentVariable() {
    setUp();

    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> builder = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(
        builder,
        hasEntry(
            "SOURCEMAP",
            pathResolver.getAbsolutePath(setup.jsBundle().getSourcePathToSourceMap()).toString()));
  }

  @Test
  public void addsSourceMapOutAsEnvironmentVariable() {
    setUpWithRewriteSourceMap();

    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> builder = getEnvironmentVariablesInGenrule(setup.genrule());
    assertThat(
        builder,
        hasEntry(
            "SOURCEMAP_OUT",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToSourceMap()).toString()));
  }

  @Test
  public void specialSourceMapTargetPointsToOwnSourceMap() {
    setUpWithRewriteSourceMap(JsFlavors.SOURCE_MAP);

    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.genrule().getSourcePathToSourceMap()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void createsSourcemapDir() {
    setUpWithRewriteSourceMap();

    JsBundleGenrule genrule = setup.genrule();
    BuildContext context = FakeBuildContext.withSourcePathResolver(sourcePathResolver());
    FakeBuildableContext buildableContext = new FakeBuildableContext();
    ImmutableList<Step> buildSteps =
        ImmutableList.copyOf(genrule.getBuildSteps(context, buildableContext));

    MkdirStep expectedStep =
        MkdirStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(),
                genrule.getProjectFilesystem(),
                context
                    .getSourcePathResolver()
                    .getRelativePath(genrule.getSourcePathToSourceMap())
                    .getParent()));
    assertThat(buildSteps, hasItem(expectedStep));

    int mkSourceMapDirIdx = buildSteps.indexOf(expectedStep);
    assertThat(
        buildSteps.subList(mkSourceMapDirIdx, buildSteps.size()), not(hasItem(any(RmStep.class))));
  }

  @Test
  public void exposesRewrittenMiscDir() {
    setUpWithRewriteMiscDir();

    JsBundleGenrule genrule = setup.genrule();
    assertEquals(
        JsUtil.relativeToOutputRoot(
            genrule.getBuildTarget(), genrule.getProjectFilesystem(), "misc"),
        genrule.getSourcePathToMisc());
  }

  @Test
  public void addsMiscAsEnvironmentVariable() {
    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(
        env,
        hasEntry(
            "MISC_DIR",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToMisc()).toString()));
  }

  @Test
  public void addsMiscAndMiscOutAsEnvironmentVariableOnRewrite() {
    setUpWithRewriteMiscDir();

    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(
        env,
        hasEntry(
            "MISC_DIR",
            pathResolver.getAbsolutePath(setup.jsBundle().getSourcePathToMisc()).toString()));
    assertThat(
        env,
        hasEntry(
            "MISC_OUT",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToMisc()).toString()));
  }

  @Test
  public void specialMiscTargetPointsToOwnMiscDir() {
    setUpWithRewriteMiscDir(JsFlavors.MISC);

    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.genrule().getSourcePathToMisc()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void createsMiscDir() {
    setUpWithRewriteMiscDir();

    JsBundleGenrule genrule = setup.genrule();
    BuildContext context = FakeBuildContext.withSourcePathResolver(sourcePathResolver());
    FakeBuildableContext buildableContext = new FakeBuildableContext();
    ImmutableList<Step> buildSteps =
        ImmutableList.copyOf(genrule.getBuildSteps(context, buildableContext));

    MkdirStep expectedStep =
        MkdirStep.of(
            BuildCellRelativePath.fromCellRelativePath(
                context.getBuildCellRootPath(),
                genrule.getProjectFilesystem(),
                context.getSourcePathResolver().getRelativePath(genrule.getSourcePathToMisc())));
    assertThat(buildSteps, hasItem(expectedStep));

    int mkMiscDirIdx = buildSteps.indexOf(expectedStep);
    assertThat(
        buildSteps.subList(mkMiscDirIdx, buildSteps.size()), not(hasItem(any(RmStep.class))));
  }

  @Test
  public void exposeDepsFileOfJsBundleWithSpecialFlavor() {
    setUp(JsFlavors.DEPENDENCY_FILE);
    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.jsBundleDepsFile().getSourcePathToOutput()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void addsDepsFileAsEnvironmentVariable() {
    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(
        env,
        hasEntry(
            "DEPENDENCIES",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToDepsFile()).toString()));
  }

  @Test
  public void addsDepsFileAndDepsFileOutAsEnvironmentVariableOnRewrite() {
    setUpWithRewriteDepsFile();

    SourcePathResolverAdapter pathResolver = sourcePathResolver();
    ImmutableMap<String, String> env = getEnvironmentVariablesInGenrule(setup.genrule());

    assertThat(
        env,
        hasEntry(
            "DEPENDENCIES",
            pathResolver
                .getAbsolutePath(setup.jsBundleDepsFile().getSourcePathToOutput())
                .toString()));
    assertThat(
        env,
        hasEntry(
            "DEPENDENCIES_OUT",
            pathResolver.getAbsolutePath(setup.genrule().getSourcePathToDepsFile()).toString()));
  }

  @Test
  public void specialDepsFileTargetPointsToOwnDepsFile() {
    setUp(JsFlavors.DEPENDENCY_FILE);

    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.genrule().getSourcePathToDepsFile()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));

    assertEquals(
        pathResolver.getRelativePath(setup.jsBundleDepsFile().getSourcePathToOutput()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void specialDepsFileTargetPointsToOwnDepsFileOnRewrite() {
    setUpWithRewriteDepsFile(JsFlavors.DEPENDENCY_FILE);

    SourcePathResolverAdapter pathResolver = sourcePathResolver();

    assertEquals(
        pathResolver.getRelativePath(setup.genrule().getSourcePathToDepsFile()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));

    assertNotEquals(
        pathResolver.getRelativePath(setup.jsBundleDepsFile().getSourcePathToOutput()),
        pathResolver.getRelativePath(setup.rule().getSourcePathToOutput()));
  }

  @Test
  public void dependsOnDepsFile() {
    setUp();
    assertThat(setup.genrule().getBuildDeps(), hasItem(setup.jsBundleDepsFile()));
  }

  @Test
  public void defaultOutputIsJSFolder() {
    setUp();

    Path targetOutputPath =
        BuildPaths.removeHashFrom(
                BuildPaths.getGenDir(setup.scenario.filesystem, setup.target).resolve("js"),
                setup.target)
            .get();
    SourcePath outputPath = setup.genrule().getSourcePathToOutput();
    Path resolvedPath =
        BuildPaths.removeHashFrom(sourcePathResolver().getRelativePath(outputPath), setup.target)
            .get();

    assertThat(resolvedPath, equalTo(targetOutputPath));
  }

  private JsBundleGenruleBuilder.Options builderOptions(
      BuildTarget bundleTarget, StringWithMacros cmd) {
    return JsBundleGenruleBuilder.Options.of(genruleTarget, bundleTarget).setCmd(cmd);
  }

  private JsBundleGenruleBuilder.Options builderOptions(BuildTarget bundleTarget) {
    return builderOptions(bundleTarget, StringWithMacros.ofConstantString("echo"));
  }

  private JsBundleGenruleBuilder.Options builderOptions(StringWithMacros cmd) {
    return builderOptions(defaultBundleTarget, cmd);
  }

  private JsBundleGenruleBuilder.Options builderOptions() {
    return builderOptions(defaultBundleTarget);
  }

  private SourcePathResolverAdapter sourcePathResolver() {
    return setup.graphBuilder().getSourcePathResolver();
  }

  private ImmutableMap<String, String> getEnvironmentVariablesInGenrule(JsBundleGenrule genrule) {
    GenruleBuildable buildable = genrule.getBuildable();
    ProjectFilesystem filesystem = setup.scenario.filesystem;
    BuildTarget target = setup.target;
    Path srcPath = BuildTargetPaths.getGenPath(filesystem, target, "%s__srcs");
    Path tmpPath = BuildTargetPaths.getGenPath(filesystem, target, "%s__tmp");
    ImmutableMap.Builder<String, String> actualEnvVarsBuilder = ImmutableMap.builder();
    buildable.addEnvironmentVariables(
        sourcePathResolver(),
        new DefaultOutputPathResolver(filesystem, target),
        filesystem,
        srcPath,
        tmpPath,
        actualEnvVarsBuilder);
    return actualEnvVarsBuilder.build();
  }

  private static class TestSetup {
    private final JsTestScenario scenario;
    private final BuildTarget target;
    private final BuildTarget bundleTarget;

    TestSetup(JsTestScenario scenario, BuildTarget target, BuildTarget bundleTarget) {
      this.scenario = scenario;
      this.target = target;
      this.bundleTarget = bundleTarget;
    }

    BuildRule rule() {
      return scenario.graphBuilder.requireRule(target);
    }

    JsBundleGenrule genrule(Flavor... flavors) {
      return (JsBundleGenrule)
          scenario.graphBuilder.requireRule(
              target
                  .withoutFlavors(JsFlavors.DEPENDENCY_FILE, JsFlavors.SOURCE_MAP, JsFlavors.MISC)
                  .withAppendedFlavors(flavors));
    }

    @SuppressWarnings("unchecked")
    TargetNode<JsBundleGenruleDescriptionArg> targetNode() {
      TargetNode<?> targetNode = scenario.targetGraph.get(target);
      return (TargetNode<JsBundleGenruleDescriptionArg>) targetNode;
    }

    JsBundleOutputs jsBundle(Flavor... extraFlavors) {
      return (JsBundleOutputs)
          graphBuilder().requireRule(bundleTarget.withAppendedFlavors(extraFlavors));
    }

    JsBundleAndroid jsBundleAndroid() {
      return graphBuilder().getRuleWithType(bundleTarget, JsBundleAndroid.class);
    }

    BuildRule jsBundleDepsFile() {
      return graphBuilder()
          .requireRule(bundleTarget.withAppendedFlavors(JsFlavors.DEPENDENCY_FILE));
    }

    ActionGraphBuilder graphBuilder() {
      return scenario.graphBuilder;
    }
  }

  private static AndroidPackageableCollector packageableCollectorMock(TestSetup setup) {
    AndroidPackageableCollector collector = EasyMock.createMock(AndroidPackageableCollector.class);
    expect(
            collector.addAssetsDirectory(
                setup.rule().getBuildTarget(), setup.genrule().getSourcePathToOutput()))
        .andReturn(collector);
    replay(collector);
    return collector;
  }
}
