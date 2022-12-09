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

package com.facebook.buck.jvm.java.abi;

import com.facebook.buck.core.exceptions.HumanReadableException;
import com.facebook.buck.jvm.java.abi.source.api.SourceOnlyAbiRuleInfoFactory.SourceOnlyAbiRuleInfo;
import com.facebook.buck.jvm.java.plugin.api.BuckJavacTaskListener;
import com.facebook.buck.jvm.java.plugin.api.BuckJavacTaskProxy;
import com.facebook.buck.jvm.java.plugin.api.PluginClassLoader;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;
import javax.tools.Diagnostic;

public final class SourceBasedAbiStubber {
  public static BuckJavacTaskListener newValidatingTaskListener(
      PluginClassLoader pluginLoader,
      BuckJavacTaskProxy task,
      SourceOnlyAbiRuleInfo ruleInfo,
      Supplier<Boolean> errorsExist,
      Diagnostic.Kind messageKind) {
    try {
      Class<?> validatingTaskListenerClass =
          pluginLoader.loadClass(
              "com.facebook.buck.jvm.java.abi.source.ValidatingTaskListener", Object.class);
      Constructor<?> constructor =
          validatingTaskListenerClass.getConstructor(
              BuckJavacTaskProxy.class,
              SourceOnlyAbiRuleInfo.class,
              Supplier.class,
              Diagnostic.Kind.class);

      return BuckJavacTaskListener.wrapRealTaskListener(
          pluginLoader, constructor.newInstance(task, ruleInfo, errorsExist, messageKind));
    } catch (ReflectiveOperationException e) {
      throw new HumanReadableException(
          e,
          "Could not load source-generated ABI validator. Your compiler might not support this. "
              + "If it doesn't, you may need to disable source-based ABI generation.");
    }
  }

  private SourceBasedAbiStubber() {}
}
