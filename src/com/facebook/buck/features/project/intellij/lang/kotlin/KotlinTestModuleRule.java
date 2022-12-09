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

package com.facebook.buck.features.project.intellij.lang.kotlin;

import com.facebook.buck.core.model.targetgraph.TargetNode;
import com.facebook.buck.core.rules.DescriptionWithTargetGraph;
import com.facebook.buck.features.project.intellij.BaseIjModuleRule;
import com.facebook.buck.features.project.intellij.IjKotlinHelper;
import com.facebook.buck.features.project.intellij.ModuleBuildContext;
import com.facebook.buck.features.project.intellij.model.IjModuleFactoryResolver;
import com.facebook.buck.features.project.intellij.model.IjModuleType;
import com.facebook.buck.features.project.intellij.model.IjProjectConfig;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.jvm.kotlin.KotlinTestDescription;
import com.facebook.buck.jvm.kotlin.KotlinTestDescriptionArg;

public class KotlinTestModuleRule extends BaseIjModuleRule<KotlinTestDescriptionArg> {

  public KotlinTestModuleRule(
      ProjectFilesystem projectFilesystem,
      IjModuleFactoryResolver moduleFactoryResolver,
      IjProjectConfig projectConfig) {
    super(projectFilesystem, moduleFactoryResolver, projectConfig);
  }

  @Override
  public Class<? extends DescriptionWithTargetGraph<?>> getDescriptionClass() {
    return KotlinTestDescription.class;
  }

  @Override
  public void apply(TargetNode<KotlinTestDescriptionArg> target, ModuleBuildContext context) {
    addDepsAndTestSources(target, false /* wantsPackagePrefix */, context);

    IjKotlinHelper.addKotlinJavaRuntimeLibraryDependencyIfNecessary(target, context);
  }

  @Override
  public IjModuleType detectModuleType(TargetNode<KotlinTestDescriptionArg> targetNode) {
    return IjModuleType.UNKNOWN_MODULE;
  }
}
