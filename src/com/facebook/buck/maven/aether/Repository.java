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

package com.facebook.buck.maven.aether;

import java.util.Objects;
import javax.annotation.Nullable;

public class Repository {
  private @Nullable String url;
  public @Nullable String user;
  public @Nullable String password;

  public Repository() {}

  public Repository(String url) {
    this.url = url;
  }

  public String getUrl() {
    return Objects.requireNonNull(url);
  }
}
