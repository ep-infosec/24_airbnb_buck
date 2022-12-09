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

package com.facebook.buck.testrunner;

class CheckDependency {

  private CheckDependency() {
    // Utility class.
  }

  public static void isPresent(String name, String classToLoad) {
    try {
      Class.forName(classToLoad);
    } catch (ClassNotFoundException e) {
      System.err.println(
          "Unable to locate " + name + " on the classpath. Please add as a test dependency.");
      System.exit(1);
    }
  }
}
