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

package com.facebook.buck.core.module.annotationprocessor;

import java.io.PrintWriter; // NOPMD see the javadoc
import java.io.StringWriter;

/**
 * This class exists to avoid heavy dependencies on other libraries (like guava) or the rest of Buck
 * classes. Since those classes cannot be used NOPMD is required to allow the usage of {@link
 * PrintWriter}.
 */
class ThrowablesUtils {
  public static String toString(Throwable throwable) {
    StringWriter writer = new StringWriter();
    throwable.printStackTrace(new PrintWriter(writer)); // NOPMD see the javadoc
    return writer.toString();
  }
}
