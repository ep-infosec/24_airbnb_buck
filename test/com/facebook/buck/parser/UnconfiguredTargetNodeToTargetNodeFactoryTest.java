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

package com.facebook.buck.parser;

import static org.junit.Assert.assertEquals;

import com.facebook.buck.core.cell.Cells;
import com.facebook.buck.core.cell.DefaultCellNameResolverProvider;
import com.facebook.buck.core.cell.TestCellBuilder;
import com.facebook.buck.core.exceptions.DependencyStack;
import com.facebook.buck.core.filesystems.AbsPath;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.ConfigurationBuildTargetFactoryForTests;
import com.facebook.buck.core.model.RuleBasedTargetConfiguration;
import com.facebook.buck.core.model.RuleType;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.model.UnconfiguredBuildTargetFactoryForTests;
import com.facebook.buck.core.model.UnconfiguredTargetConfiguration;
import com.facebook.buck.core.model.impl.MultiPlatformTargetConfigurationTransformer;
import com.facebook.buck.core.model.platform.TargetPlatformResolver;
import com.facebook.buck.core.model.platform.impl.UnconfiguredPlatform;
import com.facebook.buck.core.model.targetgraph.TargetNode;
import com.facebook.buck.core.model.targetgraph.impl.ImmutableUnconfiguredTargetNode;
import com.facebook.buck.core.model.targetgraph.impl.TargetNodeFactory;
import com.facebook.buck.core.model.targetgraph.raw.UnconfiguredTargetNode;
import com.facebook.buck.core.plugin.impl.BuckPluginManagerFactory;
import com.facebook.buck.core.rules.knowntypes.TestKnownRuleTypesProvider;
import com.facebook.buck.core.select.Selector;
import com.facebook.buck.core.select.SelectorKey;
import com.facebook.buck.core.select.SelectorList;
import com.facebook.buck.core.select.TestSelectable;
import com.facebook.buck.core.select.TestSelectableResolver;
import com.facebook.buck.core.select.impl.DefaultSelectorListResolver;
import com.facebook.buck.core.sourcepath.PathSourcePath;
import com.facebook.buck.core.sourcepath.UnconfiguredSourcePathFactoryForTests;
import com.facebook.buck.event.SimplePerfEvent;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.facebook.buck.jvm.java.JavaLibraryDescription;
import com.facebook.buck.jvm.java.JavaLibraryDescriptionArg;
import com.facebook.buck.rules.coercer.DefaultConstructorArgMarshaller;
import com.facebook.buck.rules.coercer.DefaultTypeCoercerFactory;
import com.facebook.buck.rules.coercer.TypeCoercerFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;

public class UnconfiguredTargetNodeToTargetNodeFactoryTest {

  @Test
  public void testTargetNodeCreatedWithAttributes() {
    BuildTarget targetPlatform =
        ConfigurationBuildTargetFactoryForTests.newInstance("//config:platform");
    TargetConfiguration targetConfiguration = RuleBasedTargetConfiguration.of(targetPlatform);
    BuildTarget buildTarget =
        UnconfiguredBuildTargetFactoryForTests.newInstance("//a/b:c")
            .configure(targetConfiguration);
    Path basepath = Paths.get("a").resolve("b");
    Cells cell =
        new TestCellBuilder()
            .setFilesystem(
                new FakeProjectFilesystem(
                    Sets.newHashSet(
                        basepath.resolve("src1"),
                        basepath.resolve("src2"),
                        basepath.resolve("BUCK"))))
            .build();
    SelectorList<?> selectorList =
        new SelectorList<>(
            ImmutableList.of(
                new Selector<>(
                    ImmutableMap.of(
                        SelectorKey.DEFAULT,
                        Optional.of("1"),
                        new SelectorKey(
                            ConfigurationBuildTargetFactoryForTests.newInstance("//x:y")),
                        Optional.of("2")),
                    ImmutableSet.of(),
                    ""),
                new Selector<>(
                    ImmutableMap.of(
                        SelectorKey.DEFAULT,
                        Optional.of("8"),
                        new SelectorKey(
                            ConfigurationBuildTargetFactoryForTests.newInstance("//x:y")),
                        Optional.of("9")),
                    ImmutableSet.of(),
                    "")));
    ImmutableMap<String, Object> attributes =
        ImmutableMap.<String, Object>builder()
            .put("name", "c")
            .put(
                "srcs",
                ImmutableList.of(
                    UnconfiguredSourcePathFactoryForTests.unconfiguredSourcePath("a/b/src1"),
                    UnconfiguredSourcePathFactoryForTests.unconfiguredSourcePath("a/b/src2")))
            .put("source", selectorList)
            .build();
    UnconfiguredTargetNode node =
        ImmutableUnconfiguredTargetNode.of(
            buildTarget.getUnconfiguredBuildTarget(),
            RuleType.of("java_library", RuleType.Kind.BUILD),
            attributes,
            ImmutableSet.of(),
            ImmutableSet.of(),
            Optional.empty(),
            ImmutableList.of());
    TypeCoercerFactory typeCoercerFactory = new DefaultTypeCoercerFactory();
    BuildTarget selectableTarget = ConfigurationBuildTargetFactoryForTests.newInstance("//x:y");
    TargetPlatformResolver targetPlatformResolver =
        (configuration, dependencyStack) -> UnconfiguredPlatform.INSTANCE;
    UnconfiguredTargetNodeToTargetNodeFactory factory =
        new UnconfiguredTargetNodeToTargetNodeFactory(
            typeCoercerFactory,
            TestKnownRuleTypesProvider.create(BuckPluginManagerFactory.createPluginManager()),
            new DefaultConstructorArgMarshaller(),
            new TargetNodeFactory(typeCoercerFactory, new DefaultCellNameResolverProvider(cell)),
            new NoopPackageBoundaryChecker(),
            (file, targetNode) -> {},
            new DefaultSelectorListResolver(
                new TestSelectableResolver(
                    ImmutableList.of(new TestSelectable(selectableTarget, true)))),
            targetPlatformResolver,
            new MultiPlatformTargetConfigurationTransformer(targetPlatformResolver),
            UnconfiguredTargetConfiguration.INSTANCE,
            cell.getRootCell().getBuckConfig(),
            Optional.empty());

    TargetNode<?> targetNode =
        factory
            .createTargetNode(
                cell.getRootCell(),
                AbsPath.of(Paths.get("a/b/BUCK").toAbsolutePath()),
                buildTarget,
                DependencyStack.root(),
                node,
                id -> SimplePerfEvent.scope(Optional.empty(), null, null))
            .assertGetTargetNode(DependencyStack.root());

    assertEquals(JavaLibraryDescription.class, targetNode.getDescription().getClass());
    JavaLibraryDescriptionArg arg = (JavaLibraryDescriptionArg) targetNode.getConstructorArg();
    assertEquals(
        ImmutableSortedSet.of(
            PathSourcePath.of(cell.getRootCell().getFilesystem(), basepath.resolve("src1")),
            PathSourcePath.of(cell.getRootCell().getFilesystem(), basepath.resolve("src2"))),
        arg.getSrcs());
    assertEquals(
        ImmutableSet.of(targetPlatform, selectableTarget), targetNode.getConfigurationDeps());
  }
}
